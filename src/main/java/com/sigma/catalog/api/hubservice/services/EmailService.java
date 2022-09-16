package com.sigma.catalog.api.hubservice.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

@Service
public class EmailService {
   private static Properties emailProperties;

   EmailService() {

      try (InputStream input = new FileInputStream("config/emailconfiguration.properties");) {
         emailProperties = new Properties();
         emailProperties.load(input);
      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   public void sendMail(JobProperites jobrprops, String subject, String emailBody) {
      if (!jobrprops.isSendEmail()) {
         System.out.println("Email is stubed");
         return;
      }
      // Recipient's email ID needs to be mentioned.
      String to = emailProperties.getProperty("to_list", "tejas.shah@hansencx.com");

      // Sender's email ID needs to be mentioned
      String from = emailProperties.getProperty("from", "no-reply.capitool@hansencx.com");
      final String username = emailProperties.getProperty("user_name");
      final String password = emailProperties.getProperty("password");
      // Assuming you are sending email through relay.jangosmtp.net
      String host = emailProperties.getProperty("smtphost");

      Properties props = new Properties();
      props.put("mail.smtp.auth", emailProperties.getProperty("auth", "false"));
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.host", host);
      props.put("mail.smtp.ssl.protocols", "TLSv1.2");
      props.put("mail.smtp.port", emailProperties.getProperty("port", "25"));

      System.out.println(props);
      System.out.println(username + "___" + password);
      // Get the Session object.
      Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
               protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(username, password);
               }
            });

      try {
         // Create a default MimeMessage object.
         Message message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.setRecipients(Message.RecipientType.TO,
               InternetAddress.parse(to));

         // Set Subject: header field
         message.setSubject(subject);

         // Send the actual HTML message, as big as you like

         Multipart multipart = new MimeMultipart();
         BodyPart attachmentBodyPart = new MimeBodyPart();
         String fileName = jobrprops.jobId;
         String emailFile = ".\\emails\\" + jobrprops.jobId;
         saveFile(emailFile, emailBody);
         DataSource source = new FileDataSource(emailFile);
         attachmentBodyPart.setDataHandler(new DataHandler(source));
         attachmentBodyPart.setFileName(fileName);
         multipart.addBodyPart(attachmentBodyPart);

         BodyPart htmlBodyPart = new MimeBodyPart();
         htmlBodyPart.setContent(getEmailTemplate(), "text/html");
         multipart.addBodyPart(htmlBodyPart);

         message.setContent(multipart);
         // Send message
         Transport.send(message);

         System.out.println("Sent message successfully....");

      } catch (Exception e) {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   public static void main(String[] args) {
      EmailService e = new EmailService();
      e.sendMail(new JobProperites("jobId"), "EAAM", "eaaam");
   }

   public void saveFile(String fileNAme, String body) throws IOException {
      File path = new File(fileNAme);

      // passing file instance in filewriter
      FileWriter wr = new FileWriter(path);

      // calling writer.write() method with the string
      wr.write(body);

      // flushing the writer
      wr.flush();

      // closing the writer
      wr.close();
   }

   String getEmailTemplate() {
      String data = "something went  wrong. Please find attached technical detail";
      try {

         File myObj = new File("config/emailTemplate.html");
         Scanner myReader = new Scanner(myObj);
         while (myReader.hasNextLine()) {
            data = data + myReader.nextLine();

         }
         myReader.close();
      } catch (FileNotFoundException e) {
         System.out.println("An error occurred.");
         e.printStackTrace();
      }
      return data;

   }
}
