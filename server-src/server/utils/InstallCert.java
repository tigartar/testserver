package com.wurmonline.server.utils;

import com.wurmonline.shared.util.StringUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class InstallCert {
   private static final Logger logger = Logger.getLogger(InstallCert.class.getName());

   private InstallCert() {
   }

   public static void installCert(String host, int port, String password, String keystoreName) throws Exception {
      char[] passphrase = password.toCharArray();
      char SEP = File.separatorChar;
      File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
      File file = new File(dir, keystoreName);
      if (!file.isFile()) {
         file = new File(dir, "cacerts");
      }

      logger.log(Level.INFO, "Loading KeyStore " + file + "...");
      InputStream in = new FileInputStream(file);
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(in, passphrase);
      in.close();

      try {
         logger.log(Level.INFO, "Loaded Keystore size: " + ks.size());
      } catch (KeyStoreException var23) {
         logger.log(Level.INFO, "Keystore has not been initalized");
      }

      SSLContext context = SSLContext.getInstance("TLS");
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ks);
      X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];
      InstallCert.SavingTrustManager tm = new InstallCert.SavingTrustManager(defaultTrustManager);
      context.init(null, new TrustManager[]{tm}, null);
      SSLSocketFactory factory = context.getSocketFactory();
      logger.log(Level.INFO, "Opening connection to " + host + ":" + port + "...");
      SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
      socket.setSoTimeout(10000);

      try {
         logger.log(Level.INFO, "Starting SSL handshake...");
         socket.startHandshake();
         socket.close();
         logger.log(Level.INFO, "No errors, certificate is already trusted");
      } catch (SSLException var24) {
         logger.log(Level.INFO, "Received SSLException. Untrusted cert. Installing.");
         X509Certificate[] chain = tm.chain;
         if (chain == null) {
            logger.log(Level.INFO, "Could not obtain server certificate chain");
         } else {
            logger.log(Level.INFO, "Server sent " + chain.length + " certificate(s):");
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            for(int i = 0; i < chain.length; ++i) {
               X509Certificate cert = chain[i];
               logger.log(Level.INFO, " " + (i + 1) + " Subject " + cert.getSubjectDN());
               logger.log(Level.INFO, "   Issuer  " + cert.getIssuerDN());
               sha1.update(cert.getEncoded());
               logger.log(Level.INFO, "   sha1    " + StringUtilities.toHexString(sha1.digest()));
               md5.update(cert.getEncoded());
               logger.log(Level.INFO, "   md5     " + StringUtilities.toHexString(md5.digest()));
            }

            int k = chain.length - 1;
            X509Certificate cert = chain[k];
            String alias = host + "-" + (k + 1);
            ks.setCertificateEntry(alias, cert);
            OutputStream out = new FileOutputStream(file);
            ks.store(out, passphrase);
            out.close();
            logger.log(Level.INFO, cert.toString());
            logger.log(Level.INFO, "Added certificate to keystore '" + file.getAbsolutePath() + "' using alias '" + alias + "'");
         }
      }
   }

   private static class SavingTrustManager implements X509TrustManager {
      private final X509TrustManager tm;
      private X509Certificate[] chain;

      SavingTrustManager(X509TrustManager aTm) {
         this.tm = aTm;
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
         throw new UnsupportedOperationException();
      }

      @Override
      public void checkClientTrusted(X509Certificate[] aChain, String authType) throws CertificateException {
         throw new UnsupportedOperationException();
      }

      @Override
      public void checkServerTrusted(X509Certificate[] aChain, String authType) throws CertificateException {
         this.chain = aChain;
         this.tm.checkServerTrusted(aChain, authType);
      }
   }
}
