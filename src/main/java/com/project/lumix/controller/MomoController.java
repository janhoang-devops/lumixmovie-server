package com.project.lumix.controller;

import com.project.lumix.dto.request.CreatePaymentRequest;
import com.project.lumix.dto.request.MomoIPNRequest;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.PaymentAdminResponse;
import com.project.lumix.dto.response.PaymentResponse;
import com.project.lumix.dto.response.PaymentStatsResponse;
import com.project.lumix.enums.PaymentStatus;
import com.project.lumix.service.MomoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/momo")
@RequiredArgsConstructor
@Slf4j
public class MomoController {

    private final MomoService momoService;

    /**
     * POST /api/momo/create
     * Frontend gọi để khởi tạo phiên thanh toán và nhận payUrl / qrCodeUrl.
     *
     * @param request thông tin đơn hàng (planType, userId)
     * @return PaymentResponse chứa payUrl để redirect người dùng
     */
    @PostMapping("/create")
    public ApiResponse<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        PaymentResponse response = momoService.createPayment(request);
        return ApiResponse.<PaymentResponse>builder()
                .code(1000)
                .message("Tao QR thanh toan thanh cong")
                .result(response)
                .build();
    }

    /**
     * POST /api/momo/ipn-handler
     * MoMo tự động gọi endpoint này (server-to-server) sau khi giao dịch hoàn tất.
     * KHÔNG dùng cho FE. Endpoint này phải PUBLIC (không cần JWT).
     *
     * Quy ước: trả về HTTP 204 No Content để MoMo biết đã nhận thành công.
     */
    @PostMapping("/ipn-handler")
    public ResponseEntity<Void> handleMomoIPN(@RequestBody MomoIPNRequest ipnRequest) {
        log.info("[IPN] Nhan callback tu MoMo: orderId={}, resultCode={}",
                ipnRequest.getOrderId(), ipnRequest.getResultCode());
        momoService.handleIPN(ipnRequest);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/momo/status/{orderId}
     * Frontend gọi sau khi người dùng được redirect về từ trang MoMo
     * (qua return-url) để biết kết quả thanh toán.
     *
     * @param orderId mã đơn hàng
     * @return PaymentResponse với trạng thái hiện tại (PENDING / SUCCESS / FAILED)
     */
    @GetMapping("/status/{orderId}")
    public ApiResponse<PaymentResponse> getPaymentStatus(@PathVariable String orderId) {
        PaymentResponse response = momoService.checkPaymentStatus(orderId);
        return ApiResponse.<PaymentResponse>builder()
                .code(1000)
                .message("Lay trang thai thanh toan thanh cong")
                .result(response)
                .build();
    }

    /**
     * POST /api/momo/dev/simulate/{orderId}
     * [DEV ONLY] Giả lập thanh toán thành công mà không cần quét QR.
     * Trực tiếp cập nhật DB thành SUCCESS, KHÔNG verify signature MoMo.
     * Endpoint này phải PUBLIC và CHỈ dùng trong môi trường development.
     *
     * @param orderId mã đơn hàng cần cập nhật
     * @return PaymentResponse với status = SUCCESS
     */
    @PostMapping("/dev/simulate/{orderId}")
    public ApiResponse<PaymentResponse> simulatePaymentSuccess(@PathVariable String orderId) {
        log.warn("[DEV] Nhan yeu cau simulate thanh cong tu FE cho orderId={}", orderId);
        PaymentResponse response = momoService.simulatePaymentSuccess(orderId);
        return ApiResponse.<PaymentResponse>builder()
                .code(1000)
                .message("[DEV] Simulate thanh toan thanh cong")
                .result(response)
                .build();
    }

    // ==================== ADMIN APIs ====================

    /**
     * GET /api/momo/admin/all
     * [ADMIN] Lấy toàn bộ lịch sử giao dịch, mới nhất trước.
     * Tuỳ chọn: lọc theo status qua query param ?status=SUCCESS|PENDING|FAILED
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<PaymentAdminResponse>> getAllPayments(
            @RequestParam(required = false) PaymentStatus status) {

        List<PaymentAdminResponse> payments = (status != null)
                ? momoService.getPaymentsByStatus(status)
                : momoService.getAllPayments();

        return ApiResponse.<List<PaymentAdminResponse>>builder()
                .code(1000)
                .message("Lay danh sach giao dich thanh cong")
                .result(payments)
                .build();
    }

    /**
     * GET /api/momo/admin/stats
     * [ADMIN] Thống kê tổng: tổng đơn, doanh thu, phân loại trạng thái.
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<PaymentStatsResponse> getPaymentStats() {
        return ApiResponse.<PaymentStatsResponse>builder()
                .code(1000)
                .message("Lay thong ke giao dich thanh cong")
                .result(momoService.getPaymentStats())
                .build();
    }

    /**
     * GET /api/momo/admin/user/{userId}
     * [ADMIN] Lấy lịch sử giao dịch của một user cụ thể.
     */
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<PaymentAdminResponse>> getPaymentsByUser(@PathVariable String userId) {
        return ApiResponse.<List<PaymentAdminResponse>>builder()
                .code(1000)
                .message("Lay giao dich cua user thanh cong")
                .result(momoService.getPaymentsByUser(userId))
                .build();
    }
}