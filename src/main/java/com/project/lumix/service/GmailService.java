package com.project.lumix.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class GmailService {
    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = baseUrl + "/lumix/auth/verify?token=" + token;

        Email from = new Email(fromEmail, "Lumix");
        Email to = new Email(toEmail);
        String subject = "Xác thực tài khoản Lumix của bạn";

        String htmlContent = """
                <html>
                  <body>
                    <h3>Cảm ơn bạn đã đăng ký tài khoản tại Lumix.</h3>
                    <p>Vui lòng nhấp vào đường link dưới đây để kích hoạt tài khoản của bạn:</p>
                    <p><a href="%s">Kích hoạt tài khoản của bạn</a></p>
                    <p>Đường link sẽ hết hạn sau 30 phút.</p>
                    <p>Trân trọng,<br>Đội ngũ Lumix</p>
                  </body>
                </html>
                """.formatted(verificationUrl);

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            log.info("Đã gửi email xác thực tới {} (status: {})", toEmail, response.getStatusCode());
        } catch (IOException e) {
            log.error("Lỗi khi gửi email xác thực tới {}: {}", toEmail, e.getMessage());
        }
    }
}

