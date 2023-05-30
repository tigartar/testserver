package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcKingdomChat extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcKingdomChat.class.getName());
   private String sender = "unknown";
   private long senderId = -10L;
   private String message = "";
   private byte kingdom = 0;
   private boolean emote = false;
   private int colorR = 0;
   private int colorG = 0;
   private int colorB = 0;

   public WcKingdomChat(long aId, long _senderId, String _sender, String _message, boolean _emote, byte _kingdom, int r, int g, int b) {
      super(aId, (short)13);
      this.sender = _sender;
      this.senderId = _senderId;
      this.message = _message;
      this.emote = _emote;
      this.kingdom = _kingdom;
      this.colorR = r;
      this.colorG = g;
      this.colorB = b;
   }

   public WcKingdomChat(long _id, byte[] _data) {
      super(_id, (short)13, _data);
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
         dos.writeUTF(this.sender);
         dos.writeLong(this.senderId);
         dos.writeUTF(this.message);
         dos.writeBoolean(this.emote);
         dos.writeByte(this.kingdom);
         dos.writeInt(this.colorR);
         dos.writeInt(this.colorG);
         dos.writeInt(this.colorB);
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
   
               try {
                  dis = new DataInputStream(new ByteArrayInputStream(WcKingdomChat.this.getData()));
                  WcKingdomChat.this.sender = dis.readUTF();
                  WcKingdomChat.this.senderId = dis.readLong();
                  WcKingdomChat.this.message = dis.readUTF();
                  WcKingdomChat.this.emote = dis.readBoolean();
                  WcKingdomChat.this.kingdom = dis.readByte();
                  WcKingdomChat.this.colorR = dis.readInt();
                  WcKingdomChat.this.colorG = dis.readInt();
                  WcKingdomChat.this.colorB = dis.readInt();
                  Players.getInstance()
                     .sendGlobalKingdomMessage(
                        null,
                        WcKingdomChat.this.senderId,
                        WcKingdomChat.this.sender,
                        WcKingdomChat.this.message,
                        WcKingdomChat.this.emote,
                        WcKingdomChat.this.kingdom,
                        WcKingdomChat.this.colorR,
                        WcKingdomChat.this.colorG,
                        WcKingdomChat.this.colorB
                     );
               } catch (IOException var6) {
                  WcKingdomChat.logger.log(Level.WARNING, "Unpack exception " + var6.getMessage(), (Throwable)var6);
               } finally {
                  StreamUtilities.closeInputStreamIgnoreExceptions(dis);
               }
            }
         })
         .start();
   }
}
