package com.wurmonline.server.webinterface;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcEpicKarmaCommand extends WebCommand {
   private static final Logger logger = Logger.getLogger(WcEpicKarmaCommand.class.getName());
   private long[] pids;
   private int[] karmas;
   private int deity;
   private static final String CLEAR_KARMA = "DELETE FROM HELPERS";

   public WcEpicKarmaCommand(long _id, long[] playerids, int[] karmaValues, int _deity) {
      super(_id, (short)16);
      this.pids = playerids;
      this.karmas = karmaValues;
      this.deity = _deity;
   }

   public WcEpicKarmaCommand(long _id, byte[] _data) {
      super(_id, (short)16, _data);
   }

   @Override
   public boolean autoForward() {
      return false;
   }

   @Override
   byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;
      byte[] barr = null;

      try {
         dos = new DataOutputStream(bos);
         dos.writeInt(this.pids.length);
         dos.writeInt(this.deity);

         for(int x = 0; x < this.pids.length; ++x) {
            dos.writeLong(this.pids[x]);
            dos.writeInt(this.karmas[x]);
         }

         dos.flush();
         dos.close();
      } catch (Exception var8) {
         logger.log(Level.WARNING, "Problem encoding for Deity " + this.deity + " - " + var8.getMessage(), (Throwable)var8);
      } finally {
         StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
         barr = bos.toByteArray();
         StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
         this.setData(barr);
      }

      return barr;
   }

   @Override
   public void execute() {
      (new Thread() {
         @Override
         public void run() {
            DataInputStream dis = null;

            try {
               dis = new DataInputStream(new ByteArrayInputStream(WcEpicKarmaCommand.this.getData()));
               int nums = dis.readInt();
               int lDeity = dis.readInt();
               Deity d = Deities.getDeity(lDeity == 3 ? 1 : lDeity);

               for(int x = 0; x < nums; ++x) {
                  long pid = dis.readLong();
                  int val = dis.readInt();
                  if (d != null) {
                     d.setPlayerKarma(pid, val);
                  }
               }
            } catch (IOException var12) {
               WcEpicKarmaCommand.logger.log(Level.WARNING, "Unpack exception " + var12.getMessage(), (Throwable)var12);
            } finally {
               StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            }
         }
      }).start();
   }

   public static void clearKarma() {
      for(Deity deity : Deities.getDeities()) {
         deity.clearKarma();
      }

      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("DELETE FROM HELPERS");
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void loadAllKarmaHelpers() {
      for(Deity deity : Deities.getDeities()) {
         deity.loadAllKarmaHelpers();
      }
   }
}
