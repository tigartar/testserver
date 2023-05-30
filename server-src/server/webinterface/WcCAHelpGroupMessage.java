package com.wurmonline.server.webinterface;

import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcCAHelpGroupMessage extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcCAHelpGroupMessage.class.getName());
   private byte groupId = -1;
   private byte kingdom = 4;
   private String name = "";
   private String msg = "";
   private boolean emote = false;
   private int colourR = -1;
   private int colourG = -1;
   private int colourB = -1;

   public WcCAHelpGroupMessage(byte caHelpGroup, byte kingdom, String playerName, String message, boolean aEmote, int red, int green, int blue) {
      super(WurmId.getNextWCCommandId(), (short)23);
      this.groupId = caHelpGroup;
      this.kingdom = kingdom;
      this.name = playerName;
      this.msg = message;
      this.emote = aEmote;
      this.colourR = red;
      this.colourG = green;
      this.colourB = blue;
   }

   public WcCAHelpGroupMessage(byte caHelpGroup) {
      super(WurmId.getNextWCCommandId(), (short)23);
      this.groupId = caHelpGroup;
   }

   WcCAHelpGroupMessage(long aId, byte[] _data) {
      super(aId, (short)23, _data);
   }

   @Override
   public boolean autoForward() {
      return false;
   }

   @Override
   public byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;
      byte[] barr = null;

      try {
         dos = new DataOutputStream(bos);
         dos.writeByte(this.groupId);
         dos.writeByte(this.kingdom);
         dos.writeUTF(this.name);
         dos.writeUTF(this.msg);
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
         this.groupId = dis.readByte();
         this.kingdom = dis.readByte();
         this.name = dis.readUTF();
         this.msg = dis.readUTF();
         this.emote = dis.readBoolean();
         this.colourR = dis.readInt();
         this.colourG = dis.readInt();
         this.colourB = dis.readInt();
         if (this.groupId != -1 && this.msg.length() > 0 && Servers.isThisLoginServer()) {
            WcCAHelpGroupMessage wchgm = new WcCAHelpGroupMessage(
               this.groupId, this.kingdom, this.name, this.msg, this.emote, this.colourR, this.colourG, this.colourB
            );

            for(ServerEntry se : Servers.getAllServers()) {
               if (se.getCAHelpGroup() == this.groupId && se.getId() != Servers.getLocalServerId() && se.getId() != WurmId.getOrigin(this.getWurmId())) {
                  wchgm.sendToServer(se.getId());
               }
            }
         }

         if (this.msg.length() == 0) {
            Servers.localServer.updateCAHelpGroup(this.groupId);
         } else if (Servers.localServer.getCAHelpGroup() == this.groupId) {
            String chan = Players.getKingdomHelpChannelName(this.kingdom);
            if (chan.length() > 0) {
               Message mess;
               if (this.emote) {
                  mess = new Message(null, (byte)6, chan, this.msg, this.colourR, this.colourG, this.colourB);
               } else {
                  mess = new Message(null, (byte)12, chan, "<" + this.name + "> " + this.msg, this.colourR, this.colourG, this.colourB);
               }

               if (this.kingdom == 4) {
                  Players.getInstance().sendPaMessage(mess);
               } else {
                  Players.getInstance().sendCaMessage(this.kingdom, mess);
               }
            }
         }
      } catch (IOException var10) {
         logger.log(Level.WARNING, "Unpack exception " + var10.getMessage(), (Throwable)var10);
      } finally {
         StreamUtilities.closeInputStreamIgnoreExceptions(dis);
      }
   }
}
