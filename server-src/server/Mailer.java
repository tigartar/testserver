/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Servers;
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
    private static String phaseOneMail = Mailer.loadConfirmationMail1();
    private static String phaseTwoMail = Mailer.loadConfirmationMail2();
    private static String passwordMail = Mailer.loadPasswordMail();
    private static String accountDelMail = Mailer.loadAccountDelMail();
    private static String accountDelPreventionMail = Mailer.loadAccountDelPreventionMail();
    private static String premExpiryMail = Mailer.loadPremExpiryMail();
    private static final Logger logger = Logger.getLogger(Mailer.class.getName());
    public static String smtpserver = "localhost";
    private static String smtpuser = "";
    private static String smtppw = "";
    private static final String amaserver = "";

    private Mailer() {
    }

    public static void sendMail(String sender, String receiver, String subject, String text) throws AddressException, MessagingException {
        new Thread(){

            @Override
            public void run() {
                try {
                    Properties props = new Properties();
                    props.setProperty("mail.transport.protocol", "smtp");
                    SMTPAuthenticator pwa = null;
                    if (Servers.localServer.LOGINSERVER) {
                        props.put("mail.host", Mailer.amaserver);
                        props.put("mail.smtp.auth", "true");
                        pwa = new SMTPAuthenticator();
                    } else {
                        props.put("mail.host", smtpserver);
                    }
                    props.put("mail.user", sender);
                    if (Servers.localServer.LOGINSERVER) {
                        props.put("mail.smtp.host", Mailer.amaserver);
                    } else {
                        props.put("mail.smtp.host", smtpserver);
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
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }.start();
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
        }
        catch (Exception uhex) {
            return "localhost";
        }
        return localHostName;
    }

    public static final String getPhaseOneMail() {
        if (phaseOneMail == null) {
            phaseOneMail = Mailer.loadConfirmationMail1();
        }
        return phaseOneMail;
    }

    public static final String getPhaseTwoMail() {
        if (phaseTwoMail == null) {
            phaseTwoMail = Mailer.loadConfirmationMail2();
        }
        return phaseTwoMail;
    }

    public static final String getPasswordMail() {
        if (passwordMail == null) {
            passwordMail = Mailer.loadPasswordMail();
        }
        return passwordMail;
    }

    public static final String getAccountDelPreventionMail() {
        if (accountDelPreventionMail == null) {
            accountDelPreventionMail = Mailer.loadAccountDelPreventionMail();
        }
        return accountDelPreventionMail;
    }

    public static final String getAccountDelMail() {
        if (accountDelMail == null) {
            accountDelMail = Mailer.loadAccountDelMail();
        }
        return accountDelMail;
    }

    public static final String getPremExpiryMail() {
        if (premExpiryMail == null) {
            premExpiryMail = Mailer.loadPremExpiryMail();
        }
        return premExpiryMail;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static final String loadConfirmationMail1() {
        try (BufferedReader in = new BufferedReader(new FileReader(regmailfileName1));){
            String str;
            StringBuilder buf = new StringBuilder();
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            String string = buf.toString();
            return string;
        }
        catch (Exception exception) {
            return amaserver;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static final String loadConfirmationMail2() {
        try (BufferedReader in = new BufferedReader(new FileReader(regmailfileName2));){
            String str;
            StringBuilder buf = new StringBuilder();
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            String string = buf.toString();
            return string;
        }
        catch (Exception exception) {
            return amaserver;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static final String loadPasswordMail() {
        try (BufferedReader in = new BufferedReader(new FileReader(pwfileName));){
            String str;
            StringBuilder buf = new StringBuilder();
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            String string = buf.toString();
            return string;
        }
        catch (Exception exception) {
            return amaserver;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static final String loadAccountDelMail() {
        try (BufferedReader in = new BufferedReader(new FileReader(accountdelw));){
            String str;
            StringBuilder buf = new StringBuilder();
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            String string = buf.toString();
            return string;
        }
        catch (Exception exception) {
            return amaserver;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static final String loadAccountDelPreventionMail() {
        try (BufferedReader in = new BufferedReader(new FileReader(accountdels));){
            String str;
            StringBuilder buf = new StringBuilder();
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            String string = buf.toString();
            return string;
        }
        catch (Exception exception) {
            return amaserver;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static final String loadPremExpiryMail() {
        try (BufferedReader in = new BufferedReader(new FileReader(premexpiryw));){
            String str;
            StringBuilder buf = new StringBuilder();
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            String string = buf.toString();
            return string;
        }
        catch (Exception exception) {
            return amaserver;
        }
    }

    public static void main(String[] args) {
    }

    static /* synthetic */ String access$100(Session x0) {
        return Mailer.getLocalHost(x0);
    }

    static /* synthetic */ Logger access$200() {
        return logger;
    }

    static /* synthetic */ String access$300() {
        return smtpuser;
    }

    static /* synthetic */ String access$400() {
        return smtppw;
    }

    private static final class SMTPAuthenticator
    extends Authenticator {
        private SMTPAuthenticator() {
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            String username = smtpuser;
            String password = smtppw;
            return new PasswordAuthentication(username, password);
        }
    }
}

