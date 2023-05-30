package com.wurmonline;

import com.wurmonline.server.DbConnector;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.BASE64Encoder;

public final class BatchEncrypter {
   private static final String getPlayers = "select * from PLAYERS";
   private static final String updatePw = "update PLAYERS set PASSWORD=? where NAME=?";
   private static Logger logger = Logger.getLogger(BatchEncrypter.class.getName());
   protected static final String destroyString = "ALTER TABLE PLAYERS DROP COLUMN PASSWORD";
   protected static final String createString = "ALTER TABLE PLAYERS ADD PASSWORD VARCHAR(30)";

   private BatchEncrypter() {
   }

   public static String encrypt(String plaintext) throws Exception {
      MessageDigest md = null;

      try {
         md = MessageDigest.getInstance("SHA");
      } catch (NoSuchAlgorithmException var5) {
         throw new WurmServerException("No such algorithm 'SHA'");
      }

      try {
         md.update(plaintext.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException var4) {
         throw new WurmServerException("No such encoding: UTF-8");
      }

      byte[] raw = md.digest();
      return new BASE64Encoder().encode(raw);
   }

   public static void encryptPasswords() {
      try {
         Connection dbcon = DbConnector.getPlayerDbCon();
         PreparedStatement ps = dbcon.prepareStatement("select * from PLAYERS");
         ResultSet rs = ps.executeQuery();
         PreparedStatement destroy = dbcon.prepareStatement("ALTER TABLE PLAYERS DROP COLUMN PASSWORD");
         destroy.execute();
         destroy.close();
         PreparedStatement create = dbcon.prepareStatement("ALTER TABLE PLAYERS ADD PASSWORD VARCHAR(30)");
         create.execute();
         create.close();

         while(rs.next()) {
            String password = rs.getString("PASSWORD");
            String name = rs.getString("NAME");
            String newPw = "";

            try {
               newPw = encrypt(name + password);
            } catch (Exception var9) {
               logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
            }

            PreparedStatement ps2 = dbcon.prepareStatement("update PLAYERS set PASSWORD=? where NAME=?");
            ps2.setString(1, newPw);
            ps2.setString(2, name);
            ps2.executeUpdate();
            ps2.close();
         }

         ps.close();
         DbConnector.closeAll();
      } catch (Exception var10) {
         logger.log(Level.INFO, var10.getMessage(), (Throwable)var10);
      }
   }
}
