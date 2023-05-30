package com.wurmonline.server.webinterface;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Servers;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcDeleteKingdom extends WebCommand {
   private static final Logger logger = Logger.getLogger(WcDeleteKingdom.class.getName());
   private byte kingdomId;

   public WcDeleteKingdom(long aId, byte kingdomToDelete) {
      super(aId, (short)8);
      this.kingdomId = kingdomToDelete;
   }

   public WcDeleteKingdom(long aId, byte[] aData) {
      super(aId, (short)8, aData);
   }

   @Override
   public boolean autoForward() {
      return true;
   }

   @Override
   byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;
      byte[] barr = null;

      try {
         dos = new DataOutputStream(bos);
         dos.writeByte(this.kingdomId);
         dos.flush();
         dos.close();
      } catch (Exception var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
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
      DataInputStream dis = null;

      try {
         dis = new DataInputStream(new ByteArrayInputStream(this.getData()));
         this.kingdomId = dis.readByte();
         Servers.removeKingdomInfo(this.kingdomId);
         Kingdom k = Kingdoms.getKingdomOrNull(this.kingdomId);
         if (k != null && k.isCustomKingdom()) {
            k.delete();
            Kingdoms.removeKingdom(this.kingdomId);
            HistoryManager.addHistory(k.getName(), "has faded and is no more.");
         }
      } catch (IOException var6) {
         logger.log(Level.WARNING, "Unpack exception " + var6.getMessage(), (Throwable)var6);
      } finally {
         StreamUtilities.closeInputStreamIgnoreExceptions(dis);
      }
   }
}
