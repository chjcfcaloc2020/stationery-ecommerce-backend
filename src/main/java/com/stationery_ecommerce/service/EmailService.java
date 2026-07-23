package com.stationery_ecommerce.service;

import com.stationery_ecommerce.dto.response.OrderResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    public void sendOrderConfirmationEmail(String toEmail, OrderResponse orderDetails) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "Stationery Store");
            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đơn hàng #" + orderDetails.getOrderNumber() + " thành công!");

            StringBuilder htmlMsg = new StringBuilder();
            htmlMsg.append("<h2>Cảm ơn bạn đã đặt hàng tại Stationery Store!</h2>");
            htmlMsg.append("<p>Mã đơn hàng của bạn là: <strong>").append(orderDetails.getOrderNumber()).append("</strong></p>");
            htmlMsg.append("<p>Tổng tiền: <strong>").append(orderDetails.getTotalAmount()).append(" VNĐ</strong></p>");

            htmlMsg.append("<h3>Chi tiết sản phẩm:</h3><ul>");
            for (OrderResponse.OrderItemDto item: orderDetails.getItems()) {
                htmlMsg.append("<li>")
                        .append(item.getProductName())
                        .append(" - Số lượng: ").append(item.getQuantity())
                        .append(" - Giá: ").append(item.getPrice()).append(" VNĐ")
                        .append("</li>");
            }
            htmlMsg.append("</ul>");
            htmlMsg.append("<p>Chúng tôi sẽ sớm chuẩn bị và giao hàng cho bạn.</p>");

            helper.setText(htmlMsg.toString(), true);

            mailSender.send(message);
            log.info("Đã gửi email xác nhận thành công tới {}", toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Lỗi khi gửi email xác nhận đơn hàng cho {}: {}", toEmail, e.getMessage());
        }
    }
}
