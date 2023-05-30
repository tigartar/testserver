package com.wurmonline.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class Mailer {
   private static final String pwfileName = "passwordmail.html";
   private static final String regmailfileName1 = "registrationphase1.html";
   private static final String regmailfileName2 = "registrationphase2.html";
   private static final String premexpiryw = "premiumexpirywarning.html";
   private static final String accountdelw = "accountdeletionwarning.html";
   private static final String accountdels = "accountdeletionsilvers.html";
   private static String phaseOneMail = loadConfirmationMail1();
   private static String phaseTwoMail = loadConfirmationMail2();
   private static String passwordMail = loadPasswordMail();
   private static String accountDelMail = loadAccountDelMail();
   private static String accountDelPreventionMail = loadAccountDelPreventionMail();
   private static String premExpiryMail = loadPremExpiryMail();
   private static final Logger logger = Logger.getLogger(Mailer.class.getName());
   public static String smtpserver = "localhost";
   private static String smtpuser = "";
   private static String smtppw = "";
   private static final String amaserver = "";

   private Mailer() {
   }

   public static void sendMail(final String sender, final String receiver, final String subject, final String text) throws AddressException, MessagingException {
      (new Thread() {
         @Override
         public void run() {
            try {
               Properties props = new Properties();
               props.setProperty("mail.transport.protocol", "smtp");
               Mailer.SMTPAuthenticator pwa = null;
               if (Servers.localServer.LOGINSERVER) {
                  props.put("mail.host", "");
                  props.put("mail.smtp.auth", "true");
                  pwa = new Mailer.SMTPAuthenticator();
               } else {
                  props.put("mail.host", Mailer.smtpserver);
               }

               props.put("mail.user", sender);
               if (Servers.localServer.LOGINSERVER) {
                  props.put("mail.smtp.host", "");
               } else {
                  props.put("mail.smtp.host", Mailer.smtpserver);
               }

               props.put("mail.smtp.port", "25");
               Session session = Session.getDefaultInstance(props, pwa);
               Properties properties = session.getProperties();
               String key = "mail.smtp.localhost";
               String prop = properties.getProperty("mail.smtp.localhost");
               if (prop == null) {
                  prop = Mailer.getLocalHost(session);
                  properties.put("mail.smtp.localhost", prop);
               }

               MimeMessage msg = new MimeMessage(session);
               msg.setContent(text, "text/html");
               msg.setSubject(subject);
               msg.setFrom(new InternetAddress(sender));
               msg.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiver));
               msg.saveChanges();
               Transport transport = session.getTransport("smtp");
               transport.connect();
               transport.sendMessage(msg, msg.getAllRecipients());
               transport.close();
            } catch (Exception var9) {
               Mailer.logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
            }
         }
      }).start();
   }

   private static final String getLocalHost(Session session) {
      String localHostName = null;
      String name = "smtp";

      try {
         if (localHostName == null || localHostName.length() <= 0) {
            localHostName = InetAddress.getLocalHost().getHostName();
         }

         if (localHostName == null || localHostName.length() <= 0) {
            localHostName = session.getProperty("mail.smtp.localhost");
         }

         return localHostName;
      } catch (Exception var4) {
         return "localhost";
      }
   }

   public static final String getPhaseOneMail() {
      if (phaseOneMail == null) {
         phaseOneMail = loadConfirmationMail1();
      }

      return phaseOneMail;
   }

   public static final String getPhaseTwoMail() {
      if (phaseTwoMail == null) {
         phaseTwoMail = loadConfirmationMail2();
      }

      return phaseTwoMail;
   }

   public static final String getPasswordMail() {
      if (passwordMail == null) {
         passwordMail = loadPasswordMail();
      }

      return passwordMail;
   }

   public static final String getAccountDelPreventionMail() {
      if (accountDelPreventionMail == null) {
         accountDelPreventionMail = loadAccountDelPreventionMail();
      }

      return accountDelPreventionMail;
   }

   public static final String getAccountDelMail() {
      if (accountDelMail == null) {
         accountDelMail = loadAccountDelMail();
      }

      return accountDelMail;
   }

   public static final String getPremExpiryMail() {
      if (premExpiryMail == null) {
         premExpiryMail = loadPremExpiryMail();
      }

      return premExpiryMail;
   }

   private static final String loadConfirmationMail1() {
      try (BufferedReader in = new BufferedReader(new FileReader("registrationphase1.html"))) {
         StringBuilder buf = new StringBuilder();

         String str;
         while((str = in.readLine()) != null) {
            buf.append(str);
         }

         in.close();
         return buf.toString();
      } catch (Exception var16) {
         return "";
      }
   }

   private static final String loadConfirmationMail2() {
      try (BufferedReader in = new BufferedReader(new FileReader("registrationphase2.html"))) {
         StringBuilder buf = new StringBuilder();

         String str;
         while((str = in.readLine()) != null) {
            buf.append(str);
         }

         in.close();
         return buf.toString();
      } catch (Exception var16) {
         return "";
      }
   }

   private static final String loadPasswordMail() {
      try (BufferedReader in = new BufferedReader(new FileReader("passwordmail.html"))) {
         StringBuilder buf = new StringBuilder();

         String str;
         while((str = in.readLine()) != null) {
            buf.append(str);
         }

         in.close();
         return buf.toString();
      } catch (Exception var16) {
         return "";
      }
   }

   private static final String loadAccountDelMail() {
      try (BufferedReader in = new BufferedReader(new FileReader("accountdeletionwarning.html"))) {
         StringBuilder buf = new StringBuilder();

         String str;
         while((str = in.readLine()) != null) {
            buf.append(str);
         }

         in.close();
         return buf.toString();
      } catch (Exception var16) {
         return "";
      }
   }

   private static final String loadAccountDelPreventionMail() {
      try (BufferedReader in = new BufferedReader(new FileReader("accountdeletionsilvers.html"))) {
         StringBuilder buf = new StringBuilder();

         String str;
         while((str = in.readLine()) != null) {
            buf.append(str);
         }

         in.close();
         return buf.toString();
      } catch (Exception var16) {
         return "";
      }
   }

   private static final String loadPremExpiryMail() {
      try (BufferedReader in = new BufferedReader(new FileReader("premiumexpirywarning.html"))) {
         StringBuilder buf = new StringBuilder();

         String str;
         while((str = in.readLine()) != null) {
            buf.append(str);
         }

         in.close();
         return buf.toString();
      } catch (Exception var16) {
         return "";
      }
   }

   public static void main(String[] args) {
   }

   private static final class SMTPAuthenticator extends Authenticator {
      private SMTPAuthenticator() {
      }

      @Override
      public PasswordAuthentication getPasswordAuthentication() {
         String username = Mailer.smtpuser;
         String password = Mailer.smtppw;
         return new PasswordAuthentication(username, password);
      }
   }
}
