package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Mailer;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WebInterfaceImpl;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PendingAccount {
   private static final String GET_ALL_PENDING_ACCOUNTS = "SELECT * FROM PENDINGACCOUNTS";
   private static final String CREATE_PENDING_ACCOUNT = "INSERT INTO PENDINGACCOUNTS(NAME,EMAIL,EXPIRATIONDATE,HASH) VALUES(?,?,?,?)";
   private static final String DELETE_PENDING_ACCOUNT = "DELETE FROM PENDINGACCOUNTS WHERE NAME=?";
   private static final Logger logger = Logger.getLogger(PendingAccount.class.getName());
   public static final Map<String, PendingAccount> accounts = new HashMap<>();
   public String accountName = "Unknown";
   public String emailAddress = "";
   public long expiration = 0L;
   public String password = "";

   public static void loadAllPendingAccounts() throws IOException {
      long start = System.nanoTime();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM PENDINGACCOUNTS");
         rs = ps.executeQuery();

         while(rs.next()) {
            PendingAccount pacc = new PendingAccount();
            pacc.accountName = rs.getString("NAME");
            pacc.emailAddress = rs.getString("EMAIL");
            pacc.expiration = rs.getLong("EXPIRATIONDATE");
            pacc.password = rs.getString("HASH");
            if (System.currentTimeMillis() > pacc.expiration) {
               pacc.delete(dbcon);
            } else {
               addPendingAccount(pacc);
            }
         }
      } catch (SQLException var12) {
         logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
         throw new IOException(var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.nanoTime();
         logger.info("Loaded " + accounts.size() + " pending accounts from the database took " + (float)(end - start) / 1000000.0F + " ms");
      }
   }

   public void delete() {
      Connection dbcon = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         this.delete(dbcon);
      } catch (SQLException var6) {
         logger.log(Level.WARNING, "Failed to delete pending account " + this.accountName, (Throwable)var6);
      } finally {
         DbConnector.returnConnection(dbcon);
      }
   }

   private void delete(Connection dbcon) {
      PreparedStatement ps = null;

      try {
         ps = dbcon.prepareStatement("DELETE FROM PENDINGACCOUNTS WHERE NAME=?");
         ps.setString(1, this.accountName);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to delete pending account " + this.accountName, (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
      }

      accounts.remove(this.accountName);
   }

   private static boolean addPendingAccount(PendingAccount acc) {
      if (accounts.containsKey(acc.accountName)) {
         return false;
      } else {
         accounts.put(acc.accountName, acc);
         return true;
      }
   }

   public boolean create() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      boolean var4;
      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("INSERT INTO PENDINGACCOUNTS(NAME,EMAIL,EXPIRATIONDATE,HASH) VALUES(?,?,?,?)");
         ps.setString(1, this.accountName);
         ps.setString(2, this.emailAddress);
         ps.setLong(3, this.expiration);
         ps.setString(4, this.password);
         ps.executeUpdate();
         return addPendingAccount(this);
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to add pending account " + this.accountName, (Throwable)var8);
         var4 = false;
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      return var4;
   }

   public static boolean doesPlayerExist(String name) {
      return accounts.containsKey(name);
   }

   public static void poll() {
      Connection dbcon = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         PendingAccount[] paddarr = accounts.values().toArray(new PendingAccount[accounts.size()]);

         for(int x = 0; x < paddarr.length; ++x) {
            if (paddarr[x].expiration < System.currentTimeMillis()) {
               paddarr[x].delete(dbcon);
            }
         }
      } catch (SQLException var6) {
         logger.log(Level.WARNING, "Failed to delete pending accounts. " + var6.getMessage(), (Throwable)var6);
      } finally {
         DbConnector.returnConnection(dbcon);
      }
   }

   public static String[] getAccountsForEmail(String email) {
      Set<String> set = new HashSet<>();

      for(PendingAccount info : accounts.values()) {
         if (info.emailAddress.equals(email)) {
            set.add(info.accountName);
         }
      }

      return set.toArray(new String[set.size()]);
   }

   public static PendingAccount getAccount(String name) {
      return accounts.get(name);
   }

   public static final void resendMails(String contains) {
      PendingAccount[] paddarr = accounts.values().toArray(new PendingAccount[accounts.size()]);

      for(int x = 0; x < paddarr.length; ++x) {
         if (contains == null || paddarr[x].emailAddress.contains(contains)) {
            try {
               String email = Mailer.getPhaseOneMail();
               email = email.replace("@pname", paddarr[x].accountName);
               email = email.replace("@email", URLEncoder.encode(paddarr[x].emailAddress, "UTF-8"));
               email = email.replace("@expiration", GeneralUtilities.toGMTString(paddarr[x].expiration));
               email = email.replace("@password", paddarr[x].password);
               Mailer.sendMail(WebInterfaceImpl.mailAccount, paddarr[x].emailAddress, "Wurm Online character creation request", email);
               logger.log(Level.INFO, "Resent " + paddarr[x].emailAddress + " for " + paddarr[x].accountName);
            } catch (Exception var4) {
            }
         }
      }
   }
}
