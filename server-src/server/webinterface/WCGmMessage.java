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

public final class WCGmMessage extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WCGmMessage.class.getName());
   private String sender = "unknown";
   private String message = "";
   private boolean emote = false;
   private int colourR = -1;
   private int colourG = -1;
   private int colourB = -1;

   WCGmMessage(long aId, byte[] _data) {
      super(aId, (short)1, _data);
   }

   public WCGmMessage(long aId, String _sender, String _message, boolean _emote) {
      super(aId, (short)1);
      this.sender = _sender;
      this.message = _message;
      this.emote = _emote;
   }

   public WCGmMessage(long aId, String _sender, String _message, boolean _emote, int red, int green, int blue) {
      super(aId, (short)1);
      this.sender = _sender;
      this.message = _message;
      this.emote = _emote;
      this.colourR = red;
      this.colourG = green;
      this.colourB = blue;
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
         dos.writeUTF(this.message);
         dos.writeBoolean(this.emote);
         dos.writeInt(this.colourR);
         dos.writeInt(this.colourG);
         dos.writeInt(this.colourB);
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
         this.sender = dis.readUTF();
         this.message = dis.readUTF();
         this.emote = dis.readBoolean();
         this.colourR = dis.readInt();
         this.colourG = dis.readInt();
         this.colourB = dis.readInt();
         Players.getInstance().sendGmMessage(null, this.sender, this.message, this.emote, this.colourR, this.colourG, this.colourB);
      } catch (IOException var6) {
         logger.log(Level.WARNING, "Unpack exception " + var6.getMessage(), (Throwable)var6);
      } finally {
         StreamUtilities.closeInputStreamIgnoreExceptions(dis);
      }
   }
}
