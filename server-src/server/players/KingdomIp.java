package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.TimeConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KingdomIp implements TimeConstants, MiscConstants {
   private long lastLogout;
   private final String ipaddress;
   private byte currentKingdom;
   private final long timeBetweenKingdomSwitches = 600000L;
   private static final Map<String, KingdomIp> ips = new ConcurrentHashMap<>();
   private static int pruneCounter = 0;
   private static final Logger logger = Logger.getLogger(KingdomIp.class.getName());

   public KingdomIp(String ipAddress, byte activeKingdom) {
      this.ipaddress = ipAddress;
      this.currentKingdom = activeKingdom;
   }

   public final void logoff() {
      if (!Players.existsPlayerWithIp(this.ipaddress)) {
         this.lastLogout = System.currentTimeMillis();
      }
   }

   public static final KingdomIp[] getAllKips() {
      return ips.values().toArray(new KingdomIp[ips.size()]);
   }

   public final void logon(byte newKingdom) {
      this.lastLogout = 0L;
      this.currentKingdom = newKingdom;
   }

   public final String getIpAddress() {
      return this.ipaddress;
   }

   public final byte getKingdom() {
      return this.currentKingdom;
   }

   public final void setKingdom(byte newKingdom) {
      this.currentKingdom = newKingdom;
   }

   public static final KingdomIp getKIP(String ipAddress) {
      return ipAddress == null ? null : ips.get(ipAddress.replace("/", ""));
   }

   public final long getLastLogout() {
      return this.lastLogout;
   }

   public final long mayLogonKingdom(byte kingdomChecked) {
      if (kingdomChecked == this.currentKingdom) {
         return 1L;
      } else if (this.lastLogout == 0L) {
         return -1L;
      } else {
         return System.currentTimeMillis() - this.lastLogout > 600000L ? 1L : System.currentTimeMillis() - this.lastLogout;
      }
   }

   public static final KingdomIp getKIP(String ipAddress, byte kingdom) {
      ++pruneCounter;
      if (pruneCounter == 300) {
         pruneCounter = 0;

         for(KingdomIp kp : ips.values()) {
            if (kp.lastLogout > 0L) {
               if (System.currentTimeMillis() - kp.lastLogout > 3600000L) {
                  logger.log(Level.INFO, "Pruning kip " + kp.getIpAddress());
                  ips.remove(kp.getIpAddress());
               }
            } else if (!Players.existsPlayerWithIp(kp.getIpAddress())) {
               logger.log(Level.INFO, "Detected non existing address for logged on ip when pruning kip " + kp.getIpAddress());
               ips.remove(kp.getIpAddress());
            }
         }
      }

      KingdomIp kip = ips.get(ipAddress.replace("/", ""));
      if (kip == null && kingdom != 0) {
         kip = new KingdomIp(ipAddress.replace("/", ""), kingdom);
         ips.put(ipAddress.replace("/", ""), kip);
      }

      return kip;
   }
}
