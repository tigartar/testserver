package com.wurmonline.server.webinterface;

import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.PortalQuestion;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcOpenEpicPortal extends WebCommand {
   private static final Logger logger = Logger.getLogger(WcOpenEpicPortal.class.getName());
   private boolean open = true;

   public WcOpenEpicPortal(long _id, boolean toggleOpen) {
      super(_id, (short)12);
      this.open = toggleOpen;
   }

   public WcOpenEpicPortal(long _id, byte[] _data) {
      super(_id, (short)12, _data);
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
         dos.writeBoolean(this.open);
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
         this.open = dis.readBoolean();
         PortalQuestion.epicPortalsEnabled = this.open;
         Player[] players = Players.getInstance().getPlayers();

         for(Player p : players) {
            SoundPlayer.playSound("sound.music.song.mountaintop", p, 2.0F);
         }

         if (Servers.localServer.LOGINSERVER) {
            WcOpenEpicPortal wccom = new WcOpenEpicPortal(WurmId.getNextWCCommandId(), PortalQuestion.epicPortalsEnabled);
            wccom.sendFromLoginServer();
         }
      } catch (IOException var10) {
         logger.log(Level.WARNING, "Unpack exception " + var10.getMessage(), (Throwable)var10);
      } finally {
         StreamUtilities.closeInputStreamIgnoreExceptions(dis);
      }
   }
}
