package com.wurmonline.server.highways;

import com.wurmonline.server.Items;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerMessageToSend {
   private static final Logger logger = Logger.getLogger(PlayerMessageToSend.class.getName());
   private final Player player;
   private final String text;

   PlayerMessageToSend(Player player, String text) {
      this.player = player;
      this.text = text;
   }

   void send() {
      this.player.getCommunicator().sendNormalServerMessage(this.text);

      for(Item waystone : Items.getWaystones()) {
         VolaTile vt = Zones.getTileOrNull(waystone.getTileX(), waystone.getTileY(), waystone.isOnSurface());
         if (vt != null) {
            for(VirtualZone vz : vt.getWatchers()) {
               try {
                  if (vz.getWatcher().getWurmId() == this.player.getWurmId()) {
                     this.player.getCommunicator().sendWaystoneData(waystone);
                     break;
                  }
               } catch (Exception var11) {
                  logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
               }
            }
         }
      }
   }
}
