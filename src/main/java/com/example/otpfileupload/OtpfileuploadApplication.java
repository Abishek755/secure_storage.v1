package com.example.otpfileupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class OtpfileuploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtpfileuploadApplication.class, args);
    }

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, String> otpStorage = new HashMap<>();

    @PostMapping("/send-otp")
    public String sendOTP(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return "Invalid email address";
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStorage.put(email, otp);

        try {
            sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);
            return "OTP sent successfully!";
        } catch (MessagingException e) {
            return "Failed to send OTP";
        }
    }

    @PostMapping("/verify-otp")
    public String verifyOTP(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        
        if (email == null || otp == null || !otp.equals(otpStorage.get(email))) {
            return "Invalid OTP";
        }
        otpStorage.remove(email);
        return "OTP verified successfully!";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Please select a file to upload";
        }
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get("uploads/" + file.getOriginalFilename());
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
            return "File uploaded successfully: " + file.getOriginalFilename();
        } catch (IOException e) {
            return "File upload failed";
        }
    }

    @PostMapping("/logout")
    public String logout() {
        return "Logged out successfully!";
    }

    private void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
        helper.setFrom(new InternetAddress("your-email@example.com"));
        mailSender.send(message);
    }
}
