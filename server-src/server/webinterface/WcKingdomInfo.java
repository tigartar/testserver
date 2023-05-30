package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
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

public class WcKingdomInfo extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcKingdomInfo.class.getName());
   private boolean sendSingleKingdom = false;
   private byte singleKingdomId;

   public WcKingdomInfo(long aId, boolean singleKingdom, byte kingdomId) {
      super(aId, (short)7);
      this.sendSingleKingdom = singleKingdom;
      this.singleKingdomId = kingdomId;
   }

   public WcKingdomInfo(long aId, byte[] aData) {
      super(aId, (short)7, aData);
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
         dos.writeBoolean(this.sendSingleKingdom);
         if (this.sendSingleKingdom) {
            dos.writeInt(1);
            Kingdom k = Kingdoms.getKingdom(this.singleKingdomId);
            dos.writeByte(k.getId());
            dos.writeUTF(k.getName());
            dos.writeUTF(k.getPassword());
            dos.writeUTF(k.getChatName());
            dos.writeUTF(k.getSuffix());
            dos.writeUTF(k.getFirstMotto());
            dos.writeUTF(k.getSecondMotto());
            dos.writeByte(k.getTemplate());
            dos.writeBoolean(k.acceptsTransfers());
         } else {
            Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
            dos.writeInt(kingdoms.length);

            for(Kingdom k : kingdoms) {
               dos.writeByte(k.getId());
               dos.writeUTF(k.getName());
               dos.writeUTF(k.getPassword());
               dos.writeUTF(k.getChatName());
               dos.writeUTF(k.getSuffix());
               dos.writeUTF(k.getFirstMotto());
               dos.writeUTF(k.getSecondMotto());
               dos.writeByte(k.getTemplate());
               dos.writeBoolean(k.acceptsTransfers());
            }
         }

         dos.flush();
         dos.close();
      } catch (Exception var12) {
         logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
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
               dis = new DataInputStream(new ByteArrayInputStream(WcKingdomInfo.this.getData()));
               WcKingdomInfo.this.sendSingleKingdom = dis.readBoolean();
               int numKingdoms = dis.readInt();
               if (!WcKingdomInfo.this.sendSingleKingdom) {
                  Kingdoms.markAllKingdomsForDeletion();
               }

               for(int x = 0; x < numKingdoms; ++x) {
                  byte id = dis.readByte();
                  String name = dis.readUTF();
                  String password = dis.readUTF();
                  String chatName = dis.readUTF();
                  String suffix = dis.readUTF();
                  String firstMotto = dis.readUTF();
                  String secondMotto = dis.readUTF();
                  byte templateKingdom = dis.readByte();
                  boolean acceptsTransfers = dis.readBoolean();
                  Kingdom kingdom = new Kingdom(id, templateKingdom, name, password, chatName, suffix, firstMotto, secondMotto, acceptsTransfers);
                  if (Kingdoms.addKingdom(kingdom)) {
                     WcKingdomInfo.logger.log(Level.INFO, "Received " + name + " in WcKingdomInfo.");
                  }
               }

               if (!WcKingdomInfo.this.sendSingleKingdom) {
                  Kingdoms.trimKingdoms();
               }
            } catch (IOException var17) {
               WcKingdomInfo.logger.log(Level.WARNING, "Unpack exception " + var17.getMessage(), (Throwable)var17);
            } finally {
               StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            }
         }
      }).start();
   }
}
