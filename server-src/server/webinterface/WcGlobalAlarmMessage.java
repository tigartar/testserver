package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcGlobalAlarmMessage extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcGlobalAlarmMessage.class.getName());
   private String alertMessage3 = "";
   private long timeBetweenAlertMess3 = Long.MAX_VALUE;
   private String alertMessage4 = "";
   private long timeBetweenAlertMess4 = Long.MAX_VALUE;

   public WcGlobalAlarmMessage(String aMess3, long aInterval3, String aMess4, long aInterval4) {
      super(WurmId.getNextWCCommandId(), (short)22);
      this.alertMessage3 = aMess3;
      this.timeBetweenAlertMess3 = aInterval3;
      this.alertMessage4 = aMess4;
      this.timeBetweenAlertMess4 = aInterval4;
   }

   WcGlobalAlarmMessage(long aId, byte[] _data) {
      super(aId, (short)22, _data);
   }

   @Override
   public boolean autoForward() {
      return true;
   }

   @Override
   public byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;
      byte[] barr = null;

      try {
         dos = new DataOutputStream(bos);
         dos.writeUTF(this.alertMessage3);
         dos.writeLong(this.timeBetweenAlertMess3);
         dos.writeUTF(this.alertMessage4);
         dos.writeLong(this.timeBetweenAlertMess4);
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
         this.alertMessage3 = dis.readUTF();
         this.timeBetweenAlertMess3 = dis.readLong();
         this.alertMessage4 = dis.readUTF();
         this.timeBetweenAlertMess4 = dis.readLong();
         Server.alertMessage3 = this.alertMessage3;
         Server.timeBetweenAlertMess3 = this.timeBetweenAlertMess3;
         if (this.alertMessage3.length() == 0) {
            Server.lastAlertMess3 = Long.MAX_VALUE;
         }

         Server.alertMessage4 = this.alertMessage4;
         Server.timeBetweenAlertMess4 = this.timeBetweenAlertMess4;
         if (this.alertMessage4.length() == 0) {
            Server.lastAlertMess4 = Long.MAX_VALUE;
         }
      } catch (IOException var6) {
         logger.log(Level.WARNING, "Unpack exception " + var6.getMessage(), (Throwable)var6);
      } finally {
         StreamUtilities.closeInputStreamIgnoreExceptions(dis);
      }
   }
}
