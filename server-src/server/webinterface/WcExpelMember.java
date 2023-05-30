package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcExpelMember extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcRemoveFriendship.class.getName());
   private long playerId;
   private byte fromKingdomId;
   private byte toKingdomId;
   private int originServer;

   public WcExpelMember(long aPlayerId, byte aFromKingdomId, byte aToKingdomId, int aOriginServer) {
      super(WurmId.getNextWCCommandId(), (short)30);
      this.playerId = aPlayerId;
      this.fromKingdomId = aFromKingdomId;
      this.toKingdomId = aToKingdomId;
      this.originServer = aOriginServer;
   }

   public WcExpelMember(long aId, byte[] aData) {
      super(aId, (short)30, aData);
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
         dos.writeLong(this.playerId);
         dos.writeByte(this.fromKingdomId);
         dos.writeByte(this.toKingdomId);
         dos.writeInt(this.originServer);
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
      (new Thread() {
            @Override
            public void run() {
               DataInputStream dis = null;
   
               label36: {
                  try {
                     dis = new DataInputStream(new ByteArrayInputStream(WcExpelMember.this.getData()));
                     WcExpelMember.this.playerId = dis.readLong();
                     WcExpelMember.this.fromKingdomId = dis.readByte();
                     WcExpelMember.this.toKingdomId = dis.readByte();
                     WcExpelMember.this.originServer = dis.readInt();
                     break label36;
                  } catch (IOException var6) {
                     WcExpelMember.logger.log(Level.WARNING, "Unpack exception " + var6.getMessage(), (Throwable)var6);
                  } finally {
                     StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                  }
   
                  return;
               }
   
               if (Servers.isThisLoginServer()) {
                  WcExpelMember.this.sendFromLoginServer();
               }
   
               PlayerInfoFactory.expelMember(
                  WcExpelMember.this.playerId, WcExpelMember.this.fromKingdomId, WcExpelMember.this.toKingdomId, WcExpelMember.this.originServer
               );
            }
         })
         .start();
   }
}
