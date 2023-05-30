package com.wurmonline.server.webinterface;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Player;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcRedeemKey extends WebCommand {
   private static final Logger logger = Logger.getLogger(WcRedeemKey.class.getName());

   public WcRedeemKey(long playerId, String coupon, byte reply) {
      super(WurmId.getNextWCCommandId(), (short)26);
   }

   public WcRedeemKey(long aId, byte[] aData) {
      super(aId, (short)26, aData);
   }

   @Override
   public boolean autoForward() {
      return false;
   }

   @Override
   byte[] encode() {
      return null;
   }

   @Override
   public void execute() {
   }

   private static final Player getRedeemingPlayer(long wurmId) {
      try {
         return Players.getInstance().getPlayer(wurmId);
      } catch (NoSuchPlayerException var3) {
         logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
         return null;
      }
   }
}
