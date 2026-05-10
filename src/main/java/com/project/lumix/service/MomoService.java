package com.project.lumix.service;

import com.project.lumix.dto.request.CreateMomoRequest;
import com.project.lumix.dto.request.CreatePaymentRequest;
import com.project.lumix.dto.request.MomoIPNRequest;
import com.project.lumix.dto.request.QueryMomoRequest;
import com.project.lumix.dto.response.CreateMomoResponse;
import com.project.lumix.dto.response.PaymentAdminResponse;
import com.project.lumix.dto.response.PaymentResponse;
import com.project.lumix.dto.response.PaymentStatsResponse;
import com.project.lumix.dto.response.QueryMomoResponse;
import com.project.lumix.entity.Payment;
import com.project.lumix.entity.User;
import com.project.lumix.enums.PaymentStatus;
import com.project.lumix.enums.PlanType;
import com.project.lumix.exception.AppException;
import com.project.lumix.exception.ErrorCode;
import com.project.lumix.repository.MomoApi;
import com.project.lumix.repository.PaymentRepository;
import com.project.lumix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoService {

    // ==================== CONFIG ====================

    @Value("${momo.partner-code}")
    private String PARTNER_CODE;

    @Value("${momo.access-key}")
    private String ACCESS_KEY;

    @Value("${momo.secret-key}")
    private String SECRET_KEY;

    @Value("${momo.return-url}")
    private String REDIRECT_URL;

    @Value("${momo.ipn-url}")
    private String IPN_URL;

    @Value("${momo.request-type}")
    private String REQUEST_TYPE;

    // ==================== DEPENDENCIES ====================

    private final MomoApi momoApi;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ==================== PLAN CONFIG ====================

    /** Bảng giá gói hội viên (đơn vị VNĐ) */
    private static final long PRICE_MONTHLY    = 59_000L;
    private static final long PRICE_QUARTERLY  = 129_000L;
    private static final long PRICE_YEARLY     = 499_000L;

    // ==================== PUBLIC METHODS ====================

    /**
     * Tạo QR thanh toán MoMo cho gói hội viên Premium.
     * Amount và orderInfo được tính từ planType, không nhận từ Frontend.
     *
     * @param request thông tin đăng ký gói (planType, userId)
     * @return PaymentResponse chứa payUrl, deeplink, qrCodeUrl
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // 1. Xác định amount và mô tả theo planType
        PlanType planType = request.getPlanType() != null ? request.getPlanType() : PlanType.MONTHLY;
        long amount;
        String orderInfo;

        switch (planType) {
            case QUARTERLY -> {
                amount = PRICE_QUARTERLY;
                orderInfo = "Dang ky goi Hoi Vien 3 Thang - Lumix Premium";
            }
            case YEARLY -> {
                amount = PRICE_YEARLY;
                orderInfo = "Dang ky goi Hoi Vien 1 Nam - Lumix Premium";
            }
            default -> {
                amount = PRICE_MONTHLY;
                orderInfo = "Dang ky goi Hoi Vien 1 Thang - Lumix Premium";
            }
        }

        // 2. Sinh orderId và requestId
        String orderId  = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();

        // 3. Tạo và ký rawSignature
        String rawSignature = buildRawSignature(orderId, requestId, orderInfo, amount);
        String signature;
        try {
            signature = signHmacSHA256(rawSignature, SECRET_KEY);
            log.info("signature: {}", signature);
        } catch (Exception e) {
            log.error("Loi khi ky HMAC-SHA256: {}", e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 4. Tìm user
        User user = null;
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        // 5. Lưu đơn hàng PENDING vào DB
        Payment payment = Payment.builder()
                .orderId(orderId)
                .requestId(requestId)
                .amount(amount)
                .orderInfo(orderInfo)
                .planType(planType)
                .status(PaymentStatus.PENDING)
                .user(user)
                .build();
        paymentRepository.save(payment);
        log.info("Da luu don hang PENDING: orderId={}, planType={}", orderId, planType);

        // 6. Gọi API MoMo để lấy payUrl / qrCodeUrl
        CreateMomoRequest momoRequest = CreateMomoRequest.builder()
                .partnerCode(PARTNER_CODE)
                .requestType(REQUEST_TYPE)
                .ipnUrl(IPN_URL)
                .redirectUrl(REDIRECT_URL)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .requestId(requestId)
                .extraData("")
                .amount(amount)
                .signature(signature)
                .lang("vi")
                .build();

        CreateMomoResponse momoResponse = momoApi.createMomoQr(momoRequest);

        if (momoResponse == null || momoResponse.getResultCode() != 0) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setMessage(momoResponse != null ? momoResponse.getMessage() : "Khong nhan duoc phan hoi tu MoMo");
            paymentRepository.save(payment);
            log.error("MoMo tra ve loi: resultCode={}, message={}",
                    momoResponse != null ? momoResponse.getResultCode() : "null",
                    momoResponse != null ? momoResponse.getMessage() : "null");
            throw new AppException(ErrorCode.MOMO_CREATE_PAYMENT_FAILED);
        }

        // 7. Lưu payUrl / deeplink / qrCodeUrl vào DB
        payment.setPayUrl(momoResponse.getPayUrl());
        payment.setDeeplink(momoResponse.getDeeplink());
        payment.setQrCodeUrl(momoResponse.getQrCodeUrl());
        payment.setResultCode(momoResponse.getResultCode());
        payment.setMessage(momoResponse.getMessage());
        paymentRepository.save(payment);
        log.info("Da cap nhat payUrl cho don hang: orderId={}", orderId);

        // 8. Trả về response
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(orderId)
                .amount(amount)
                .orderInfo(orderInfo)
                .status(PaymentStatus.PENDING)
                .payUrl(momoResponse.getPayUrl())
                .deeplink(momoResponse.getDeeplink())
                .qrCodeUrl(momoResponse.getQrCodeUrl())
                .planType(planType)
                .createdAt(payment.getCreatedAt())
                .build();
    }

    /**
     * Xử lý IPN (Instant Payment Notification) từ MoMo.
     * MoMo gọi POST sang endpoint này sau khi giao dịch hoàn tất.
     *
     * QUY TRÌNH:
     * 1. Verify chữ ký (signature) từ MoMo — BẮT BUỘC, tránh bị giả mạo
     * 2. Tìm đơn hàng trong DB theo orderId
     * 3. Cập nhật trạng thái SUCCESS / FAILED tuỳ resultCode
     *
     * @param ipnRequest dữ liệu MoMo POST sang
     */
    @Transactional
    public void handleIPN(MomoIPNRequest ipnRequest) {
        log.info("Nhan IPN tu MoMo: orderId={}, resultCode={}", ipnRequest.getOrderId(), ipnRequest.getResultCode());

        // 1. Xây dựng lại rawSignature từ dữ liệu MoMo gửi sang và xác minh
        String expectedSignature = buildIPNRawSignature(ipnRequest);
        try {
            expectedSignature = signHmacSHA256(expectedSignature, SECRET_KEY);
        } catch (Exception e) {
            log.error("Loi khi ky HMAC-SHA256 trong IPN handler: {}", e.getMessage());
            throw new AppException(ErrorCode.MOMO_INVALID_SIGNATURE);
        }

        if (!expectedSignature.equals(ipnRequest.getSignature())) {
            log.error("❌ Chu ky IPN khong hop le! orderId={}. Expected: {}, Received: {}", 
                    ipnRequest.getOrderId(), expectedSignature, ipnRequest.getSignature());
            throw new AppException(ErrorCode.MOMO_INVALID_SIGNATURE);
        }
        log.info("✅ Chu ky IPN hop le cho orderId={}", ipnRequest.getOrderId());

        // 2. Tìm đơn hàng trong DB
        Payment payment = paymentRepository.findByOrderId(ipnRequest.getOrderId())
                .orElseThrow(() -> {
                    log.error("Khong tim thay don hang: orderId={}", ipnRequest.getOrderId());
                    return new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                });

        // 3. Tránh xử lý trùng lặp (idempotent)
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Don hang da duoc xu ly truoc do: orderId={}, status={}", payment.getOrderId(),
                    payment.getStatus());
            return;
        }

        // 4. Cập nhật trạng thái dựa trên resultCode từ MoMo
        // resultCode == 0 => thanh toán thành công
        // resultCode != 0 => thất bại (chi tiết xem tài liệu MoMo)
        payment.setResultCode(ipnRequest.getResultCode());
        payment.setMessage(ipnRequest.getMessage());
        payment.setTransId(ipnRequest.getTransId());

        if (ipnRequest.getResultCode() == 0) {
            payment.setStatus(PaymentStatus.SUCCESS);
            log.info("🎉 Thanh toan THANH CONG (resultCode=0): orderId={}, transId={}", payment.getOrderId(), ipnRequest.getTransId());

            // Kích hoạt gói hội viên Premium cho user
            activatePremiumMembership(payment);

            messagingTemplate.convertAndSend("/topic/payments", payment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("❌ Thanh toan THAT BAI (resultCode={}): orderId={}, message={}",
                    ipnRequest.getResultCode(), payment.getOrderId(), ipnRequest.getMessage());
        }

        paymentRepository.save(payment);
    }

    /**
     * Kích hoạt gói hội viên Premium cho user sau khi thanh toán thành công.
     * Nếu user đang còn hạn, thời gian sẽ được cộng thêm vào thay vì ghi đè.
     *
     * @param payment đơn hàng vừa thanh toán thành công
     */
    private void activatePremiumMembership(Payment payment) {
        if (payment.getUser() == null) {
            log.warn("Don hang khong co userId, khong the kich hoat Premium: orderId={}", payment.getOrderId());
            return;
        }

        User user = payment.getUser();
        PlanType planType = payment.getPlanType() != null ? payment.getPlanType() : PlanType.MONTHLY;

        // Tính ngày bắt đầu: nếu user đang còn hạn thì cộng thêm từ ngày hết hạn cũ
        LocalDateTime baseDate = (user.getPremiumExpiredAt() != null && user.getPremiumExpiredAt().isAfter(LocalDateTime.now()))
                ? user.getPremiumExpiredAt()
                : LocalDateTime.now();

        LocalDateTime newExpiredAt = switch (planType) {
            case QUARTERLY -> baseDate.plusMonths(3);
            case YEARLY    -> baseDate.plusYears(1);
            default        -> baseDate.plusMonths(1);
        };

        user.setPremium(true);
        user.setPremiumExpiredAt(newExpiredAt);
        userRepository.save(user);

        log.info("Da kich hoat Premium cho userId={}, planType={}, het han={}", user.getUserId(), planType, newExpiredAt);
    }

    /**
     * Kiểm tra trạng thái thanh toán theo orderId.
     * Frontend gọi sau khi người dùng được redirect về từ trang MoMo.
     *
     * @param orderId mã đơn hàng
     * @return PaymentResponse với status hiện tại
     */
    @Transactional
    public PaymentResponse checkPaymentStatus(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        // Chủ động hỏi MoMo nếu database vẫn là PENDING
        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment = syncStatusWithMomo(payment);
        }

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .orderInfo(payment.getOrderInfo())
                .status(payment.getStatus())
                .payUrl(payment.getPayUrl())
                .deeplink(payment.getDeeplink())
                .qrCodeUrl(payment.getQrCodeUrl())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    /**
     * [DEV ONLY] Giả lập thanh toán thành công mà không cần quét QR / verify signature.
     * CHỈ dùng trong môi trường development để test luồng UI.
     *
     * @param orderId mã đơn hàng đang PENDING cần chuyển sang SUCCESS
     * @return PaymentResponse với status = SUCCESS
     */
    @Transactional
    public PaymentResponse simulatePaymentSuccess(String orderId) {
        log.warn("[DEV] Simulate payment success cho orderId={}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("[DEV] Khong tim thay don hang: orderId={}", orderId);
                    return new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                });

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("[DEV] Don hang da SUCCESS roi: orderId={}", orderId);
        } else {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setResultCode(0);
            payment.setMessage("Thanh cong (simulated).");
            payment.setTransId(System.currentTimeMillis());
            paymentRepository.save(payment);
            log.info("[DEV] Da cap nhat trang thai SUCCESS cho orderId={}", orderId);
            // Kích hoạt Premium membership (giống IPN thật)
            activatePremiumMembership(payment);
            messagingTemplate.convertAndSend("/topic/payments", payment);
        }

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .orderInfo(payment.getOrderInfo())
                .status(payment.getStatus())
                .payUrl(payment.getPayUrl())
                .deeplink(payment.getDeeplink())
                .qrCodeUrl(payment.getQrCodeUrl())
                .planType(payment.getPlanType())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    // ==================== ADMIN METHODS ====================

    /**
     * [ADMIN] Lấy toàn bộ lịch sử giao dịch, sắp xếp mới nhất trước.
     */
    @Transactional(readOnly = true)
    public List<PaymentAdminResponse> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    /**
     * [ADMIN] Lọc giao dịch theo trạng thái.
     *
     * @param status SUCCESS | PENDING | FAILED
     */
    @Transactional(readOnly = true)
    public List<PaymentAdminResponse> getPaymentsByStatus(PaymentStatus status) {

        return paymentRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    /**
     * [ADMIN] Thống kê tổng hợp: tổng đơn, doanh thu, phân loại status.
     */
    @Transactional(readOnly = true)
    public PaymentStatsResponse getPaymentStats() {
        long total   = paymentRepository.count();
        long success = paymentRepository.countByStatus(PaymentStatus.SUCCESS);
        long pending = paymentRepository.countByStatus(PaymentStatus.PENDING);
        long failed  = paymentRepository.countByStatus(PaymentStatus.FAILED);
        long revenue = paymentRepository.sumSuccessAmount();

        return PaymentStatsResponse.builder()
                .totalOrders(total)
                .successOrders(success)
                .pendingOrders(pending)
                .failedOrders(failed)
                .totalRevenue(revenue)
                .build();
    }

    /**
     * [ADMIN / USER] Lấy lịch sử giao dịch của một user cụ thể.
     *
     * @param userId ID của user cần tra
     */
    @Transactional(readOnly = true)
    public List<PaymentAdminResponse> getPaymentsByUser(String userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    // ==================== PRIVATE HELPERS ====================

    /**
     * Chủ động gọi API truy vấn trạng thái của MoMo nếu IPN bị miss.
     */
    private Payment syncStatusWithMomo(Payment payment) {
        try {
            String rawSignature = "accessKey=" + ACCESS_KEY 
                    + "&orderId=" + payment.getOrderId() 
                    + "&partnerCode=" + PARTNER_CODE 
                    + "&requestId=" + payment.getRequestId();
            String signature = signHmacSHA256(rawSignature, SECRET_KEY);

            QueryMomoRequest request = QueryMomoRequest.builder()
                    .partnerCode(PARTNER_CODE)
                    .requestId(payment.getRequestId())
                    .orderId(payment.getOrderId())
                    .signature(signature)
                    .lang("vi")
                    .build();

            QueryMomoResponse response = momoApi.queryPayment(request);

            if (response != null && response.getResultCode() != null) {
                if (response.getResultCode() == 0) {
                    payment.setStatus(PaymentStatus.SUCCESS);
                    log.info("🔍 Dong bo MoMo: Thanh toan THANH CONG: orderId={}", payment.getOrderId());
                    activatePremiumMembership(payment);
                    paymentRepository.save(payment);
                } else if (response.getResultCode() != 1000) { 
                    // 1000: Giao dịch đã khởi tạo, chờ người dùng thanh toán
                    payment.setStatus(PaymentStatus.FAILED);
                    log.info("🔍 Dong bo MoMo: Thanh toan THAT BAI (resultCode={}): orderId={}", response.getResultCode(), payment.getOrderId());
                    paymentRepository.save(payment);
                }
            }
        } catch (Exception e) {
            log.error("❌ Loi khi query status tu MoMo: {}", e.getMessage());
        }
        return payment;
    }

    /** Map Payment entity → PaymentAdminResponse */
    private PaymentAdminResponse toAdminResponse(Payment p) {
        String userId    = p.getUser() != null ? p.getUser().getUserId()  : null;
        String userEmail = p.getUser() != null ? p.getUser().getEmail()   : null;
        String username  = p.getUser() != null ? p.getUser().getUsername(): null;

        return PaymentAdminResponse.builder()
                .paymentId(p.getId())
                .orderId(p.getOrderId())
                .amount(p.getAmount())
                .orderInfo(p.getOrderInfo())
                .status(p.getStatus())
                .planType(p.getPlanType())
                .userId(userId)
                .userEmail(userEmail)
                .username(username)
                .transId(p.getTransId())
                .resultCode(p.getResultCode())
                .message(p.getMessage())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }


    /**
     * Xây dựng rawSignature khi TẠO đơn thanh toán (gọi API MoMo).
     * Thứ tự các trường BẮT BUỘC theo đúng tài liệu MoMo, sắp xếp alphabet.
     */
    private String buildRawSignature(String orderId, String requestId, String orderInfo, long amount) {
        return "accessKey=" + ACCESS_KEY
                + "&amount=" + amount
                + "&extraData="
                + "&ipnUrl=" + IPN_URL
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + PARTNER_CODE
                + "&redirectUrl=" + REDIRECT_URL
                + "&requestId=" + requestId
                + "&requestType=" + REQUEST_TYPE;
    }

    /**
     * Xây dựng rawSignature khi XÁC MINH chữ ký IPN từ MoMo.
     * Thứ tự các trường BẮT BUỘC theo đúng tài liệu MoMo callback spec.
     */
    private String buildIPNRawSignature(MomoIPNRequest req) {
        return "accessKey=" + ACCESS_KEY
                + "&amount=" + req.getAmount()
                + "&extraData=" + (req.getExtraData() != null ? req.getExtraData() : "")
                + "&message=" + req.getMessage()
                + "&orderId=" + req.getOrderId()
                + "&orderInfo=" + req.getOrderInfo()
                + "&orderType=" + req.getOrderType()
                + "&partnerCode=" + req.getPartnerCode()
                + "&payType=" + req.getPayType()
                + "&requestId=" + req.getRequestId()
                + "&responseTime=" + req.getResponseTime()
                + "&resultCode=" + req.getResultCode()
                + "&transId=" + req.getTransId();
    }

    /**
     * Ký chuỗi data bằng HMAC-SHA256 với key cho trước.
     *
     * @param data chuỗi cần ký
     * @param key  secret key
     * @return chuỗi hex của chữ ký
     */
    private String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKey);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}