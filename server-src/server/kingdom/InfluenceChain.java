package com.wurmonline.server.kingdom;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class InfluenceChain {
   protected static Logger logger = Logger.getLogger(InfluenceChain.class.getName());
   public static final int MAX_TOWER_CHAIN_DISTANCE = 120;
   protected static HashMap<Byte, InfluenceChain> influenceChains = new HashMap<>();
   protected ArrayList<Item> chainMarkers = new ArrayList<>();
   protected int chainedMarkers = 0;
   protected byte kingdom;

   public InfluenceChain(byte kingdom) {
      this.kingdom = kingdom;
      Village capital = Villages.getCapital(kingdom);
      if (capital != null) {
         try {
            this.chainMarkers.add(capital.getToken());
         } catch (NoSuchItemException var8) {
            logger.warning(String.format("Influence Chain Error: No token found for village %s.", capital.getName()));
         }
      } else {
         for(Village v : Villages.getVillages()) {
            if (v.kingdom == kingdom) {
               logger.info(
                  String.format(
                     "Because kingdom %s has no capital, the village %s has been selected as it's influence chain start.",
                     Kingdoms.getKingdom(kingdom).getName(),
                     v.getName()
                  )
               );
               capital = v;
               break;
            }
         }

         if (capital != null) {
            try {
               this.chainMarkers.add(capital.getToken());
            } catch (NoSuchItemException var7) {
               logger.warning(String.format("Influence Chain Error: No token found for village %s.", capital.getName()));
            }
         } else {
            logger.warning(
               String.format(
                  "Influence Chain Error: There is no compatible villages for kingdom %s to start an influence chain.", Kingdoms.getKingdom(kingdom).getName()
               )
            );
         }
      }
   }

   public ArrayList<Item> getChainMarkers() {
      return this.chainMarkers;
   }

   public void pulseChain(Item marker) {
      for(Item otherMarker : this.chainMarkers) {
         if (!otherMarker.isChained()) {
            int distX = Math.abs(marker.getTileX() - otherMarker.getTileX());
            int distY = Math.abs(marker.getTileY() - otherMarker.getTileY());
            int maxDist = Math.max(distX, distY);
            if (maxDist <= 120) {
               otherMarker.setChained(true);
               ++this.chainedMarkers;
               this.pulseChain(otherMarker);
            }
         }
      }
   }

   public void recalculateChain() {
      for(Item marker : this.chainMarkers) {
         marker.setChained(false);
      }

      Item capitalToken = this.chainMarkers.get(0);
      capitalToken.setChained(true);
      this.chainedMarkers = 1;

      for(Village v : Villages.getVillages()) {
         if (v.kingdom == this.kingdom && v.isPermanent) {
            try {
               Item villageToken = v.getToken();
               villageToken.setChained(true);
               ++this.chainedMarkers;
               this.pulseChain(villageToken);
            } catch (NoSuchItemException var7) {
               logger.warning(String.format("Influence Chain Error: No token found for village %s.", v.getName()));
            }
         }
      }

      this.pulseChain(capitalToken);
   }

   public static InfluenceChain getInfluenceChain(byte kingdom) {
      if (influenceChains.containsKey(kingdom)) {
         return influenceChains.get(kingdom);
      } else {
         InfluenceChain newChain = new InfluenceChain(kingdom);
         influenceChains.put(kingdom, newChain);
         return newChain;
      }
   }

   public void addToken(Item token) {
      if (this.chainMarkers.contains(token)) {
         logger.info(String.format("Token at %d, %d already exists in the influence chain.", token.getTileX(), token.getTileY()));
      }

      this.chainMarkers.add(token);
      this.recalculateChain();
      logger.info(
         String.format(
            "Added new village token to %s, which now has %d markers ad %d successfully linked.",
            Kingdoms.getKingdom(this.kingdom).getName(),
            this.chainMarkers.size(),
            this.chainedMarkers
         )
      );
   }

   public static void addTokenToChain(byte kingdom, Item token) {
      InfluenceChain kingdomChain = getInfluenceChain(kingdom);
      kingdomChain.addToken(token);
   }

   public void addTower(Item tower) {
      if (this.chainMarkers.contains(tower)) {
         logger.info(String.format("Tower at %d, %d already exists in the influence chain.", tower.getTileX(), tower.getTileY()));
      } else {
         this.chainMarkers.add(tower);
         this.recalculateChain();
         logger.info(
            String.format(
               "Added new tower to %s, which now has %d markers and %d successfully linked.",
               Kingdoms.getKingdom(this.kingdom).getName(),
               this.chainMarkers.size(),
               this.chainedMarkers
            )
         );
      }
   }

   public static void addTowerToChain(byte kingdom, Item tower) {
      InfluenceChain kingdomChain = getInfluenceChain(kingdom);
      kingdomChain.addTower(tower);
   }

   public void removeTower(Item tower) {
      this.chainMarkers.remove(tower);
      this.recalculateChain();
      logger.info(
         String.format(
            "Removed tower from %s, which now has %d markers and %d successfully linked.",
            Kingdoms.getKingdom(this.kingdom).getName(),
            this.chainMarkers.size(),
            this.chainedMarkers
         )
      );
   }

   public static void removeTowerFromChain(byte kingdom, Item tower) {
      InfluenceChain kingdomChain = getInfluenceChain(kingdom);
      kingdomChain.removeTower(tower);
   }
}
