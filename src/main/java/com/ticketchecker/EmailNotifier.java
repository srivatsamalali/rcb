package com.ticketchecker;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailNotifier {

    public static void sendEmailAlert(String senderEmail, String appPassword, String receiverEmail) {
        System.out.println("Attempting to send an alert email...");
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail));
            message.setSubject("URGENT: RCB Match Tickets Available!");
            message.setText("The 'Buy Now' button has been detected on the RCB ticket booking website.\n\n"
                    + "Quickly go to: https://shop.royalchallengers.com/ticket to book your tickets now!\n\n"
                    + "Automated notification by RCBTicketChecker.");

            Transport.send(message);

            System.out.println("Alert email successfully sent to " + receiverEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send email. Ensure the App Password is correct.");
            e.printStackTrace();
        }
    }
}
