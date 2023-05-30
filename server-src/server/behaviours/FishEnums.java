package com.wurmonline.server.behaviours;

import com.wurmonline.server.Point;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.WaterType;
import com.wurmonline.server.zones.Zones;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class FishEnums {
   private static final Logger logger = Logger.getLogger(FishEnums.class.getName());
   private static final byte testTypeId = -1;
   public static final int MIN_DEPTH_SPECIAL_FISH = -100;

   static int getWaterDepth(float posx, float posy, boolean isOnSurface) {
      try {
         return (int)(-Zones.calculateHeight(posx, posy, isOnSurface) * 10.0F);
      } catch (NoSuchZoneException var4) {
         logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
         return 5;
      }
   }

   public static enum BaitType {
      NONE((byte)0, -1, 1.0F),
      FLY((byte)1, 1359, 0.1F),
      CHEESE((byte)2, 1360, 1.5F),
      DOUGH((byte)3, 1361, 2.0F),
      WURM((byte)4, 1362, 1.0F),
      SARDINE((byte)5, 1337, 2.2F),
      ROACH((byte)6, 162, 2.8F),
      PERCH((byte)7, 163, 3.0F),
      MINNOW((byte)8, 1338, 2.5F),
      FISH_BAIT((byte)9, 1363, 0.2F),
      GRUB((byte)10, 1364, 0.5F),
      WHEAT((byte)11, 1365, 0.2F),
      CORN((byte)12, 1366, 0.1F);

      private final byte typeId;
      private final int templateId;
      private final float crumbles;
      private static final FishEnums.BaitType[] types = values();
      private static final Map<Integer, FishEnums.BaitType> byTemplateId = new HashMap<>();

      private BaitType(byte id, int templateId, float crumbles) {
         this.typeId = id;
         this.templateId = templateId;
         this.crumbles = crumbles;
      }

      public byte getTypeId() {
         return this.typeId;
      }

      public int getTemplateId() {
         return this.templateId;
      }

      public float getCrumbleFactor() {
         return this.crumbles;
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishEnums.BaitType fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      public static FishEnums.BaitType fromItem(@Nullable Item bait) {
         if (bait == null) {
            return NONE;
         } else {
            FishEnums.BaitType baitType = byTemplateId.get(bait.getTemplateId());
            return baitType == null ? NONE : baitType;
         }
      }

      static {
         for(FishEnums.BaitType bt : types) {
            byTemplateId.put(bt.getTemplateId(), bt);
         }
      }
   }

   public static enum FeedHeight {
      NONE((byte)0),
      TOP((byte)1),
      BOTTOM((byte)2),
      ANY((byte)3),
      TIME((byte)4);

      private final byte typeId;

      private FeedHeight(byte id) {
         this.typeId = id;
      }

      public int getTypeId() {
         return this.typeId;
      }
   }

   public static enum FishData {
      NONE(
         (byte)0,
         "unknown",
         0,
         true,
         FishEnums.FeedHeight.NONE,
         0,
         0,
         false,
         0,
         0,
         "model.creature.fish",
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         new byte[0],
         new int[]{1, 1, 1, 1},
         new int[0],
         new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
      ),
      ROACH(
         (byte)1,
         "roach",
         162,
         true,
         FishEnums.FeedHeight.BOTTOM,
         0,
         30,
         false,
         1,
         30,
         "model.creature.fish.roach",
         1.5F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         new byte[]{2, 3, 7},
         new int[]{4, 4, 3, 1},
         new int[]{705, 1343, 1344, 1372},
         new int[]{8, 5, 1, 5, 8, 1, 0, 0, 1, 5, 10, 5, 5}
      ),
      PERCH(
         (byte)2,
         "perch",
         163,
         true,
         FishEnums.FeedHeight.ANY,
         0,
         40,
         false,
         1,
         40,
         "model.creature.fish.perch",
         1.5F,
         1.0F,
         2.0F,
         2.0F,
         2.0F,
         2.0F,
         new byte[]{2, 3, 7},
         new int[]{4, 3, 3, 2},
         new int[]{705, 1343, 1344, 1372},
         new int[]{8, 5, 5, 10, 5, 5, 1, 0, 5, 5, 8, 5, 5}
      ),
      TROUT(
         (byte)3,
         "brook trout",
         165,
         true,
         FishEnums.FeedHeight.BOTTOM,
         60,
         150,
         false,
         2,
         50,
         "model.creature.fish.trout",
         1.0F,
         1.0F,
         4.0F,
         4.0F,
         4.0F,
         4.0F,
         new byte[]{2, 3},
         new int[]{4, 2, 4, 2},
         new int[]{705, 1373, 1374},
         new int[]{1, 10, 1, 5, 5, 5, 5, 5, 5, 1, 5, 1, 5}
      ),
      PIKE(
         (byte)4,
         "pike",
         157,
         true,
         FishEnums.FeedHeight.BOTTOM,
         10,
         100,
         false,
         4,
         50,
         "model.creature.fish.pike",
         0.74F,
         1.0F,
         8.0F,
         10.0F,
         5.0F,
         2.0F,
         new byte[]{3},
         new int[]{3, 2, 4, 4},
         new int[]{1372, 1373},
         new int[]{1, 5, 1, 1, 1, 5, 5, 5, 5, 1, 1, 10, 1}
      ),
      CATFISH(
         (byte)5,
         "catfish",
         160,
         true,
         FishEnums.FeedHeight.BOTTOM,
         20,
         100,
         false,
         4,
         50,
         "model.creature.fish.catfish",
         0.85F,
         1.0F,
         20.0F,
         22.0F,
         13.0F,
         5.0F,
         new byte[]{3, 4},
         new int[]{4, 4, 3, 2},
         new int[]{705, 1372, 1373, 1374},
         new int[]{1, 5, 5, 5, 5, 5, 7, 5, 5, 10, 5, 5, 5}
      ),
      SNOOK(
         (byte)6,
         "snook",
         161,
         true,
         FishEnums.FeedHeight.TIME,
         10,
         250,
         false,
         5,
         50,
         "model.creature.fish.snook",
         0.85F,
         1.0F,
         15.0F,
         17.0F,
         14.0F,
         8.0F,
         new byte[]{4},
         new int[]{4, 2, 3, 4},
         new int[]{1372, 1373, 1374},
         new int[]{0, 1, 1, 1, 1, 1, 1, 5, 5, 5, 1, 1, 10}
      ),
      HERRING(
         (byte)7,
         "herring",
         159,
         true,
         FishEnums.FeedHeight.TIME,
         10,
         150,
         false,
         1,
         50,
         "model.creature.fish.herring",
         2.35F,
         1.0F,
         10.0F,
         15.0F,
         10.0F,
         7.0F,
         new byte[]{3, 4},
         new int[]{2, 3, 4, 2},
         new int[]{1372, 1373},
         new int[]{1, 5, 5, 5, 1, 5, 5, 1, 1, 1, 10, 1, 1}
      ),
      CARP(
         (byte)8,
         "carp",
         164,
         true,
         FishEnums.FeedHeight.ANY,
         5,
         200,
         false,
         3,
         50,
         "model.creature.fish.carp",
         0.5F,
         1.0F,
         13.0F,
         18.0F,
         11.0F,
         8.0F,
         new byte[]{2, 3},
         new int[]{4, 4, 3, 3},
         new int[]{705, 1344, 1372, 1373, 1374, 1375},
         new int[]{10, 5, 5, 5, 5, 1, 1, 1, 1, 5, 5, 5, 5}
      ),
      BASS(
         (byte)9,
         "smallmouth bass",
         158,
         true,
         FishEnums.FeedHeight.BOTTOM,
         0,
         60,
         false,
         2,
         50,
         "model.creature.fish.bass",
         1.03F,
         1.0F,
         15.0F,
         21.0F,
         14.0F,
         11.0F,
         new byte[]{3, 4},
         new int[]{2, 2, 4, 4},
         new int[]{1344, 1372, 1373},
         new int[]{1, 1, 1, 1, 10, 1, 1, 1, 1, 5, 1, 1, 1}
      ),
      SALMON(
         (byte)10,
         "salmon",
         1335,
         true,
         FishEnums.FeedHeight.TIME,
         0,
         75,
         false,
         3,
         50,
         "model.creature.fish.salmon",
         1.0F,
         1.0F,
         25.0F,
         30.0F,
         30.0F,
         15.0F,
         new byte[]{3, 4},
         new int[]{4, 1, 4, 2},
         new int[]{705, 1372, 1373},
         new int[]{1, 10, 1, 1, 5, 5, 5, 5, 5, 1, 5, 1, 1}
      ),
      OCTOPUS(
         (byte)11,
         "octopus",
         572,
         true,
         FishEnums.FeedHeight.ANY,
         200,
         800,
         true,
         3,
         50,
         "model.creature.fish.octopus.black",
         1.0F,
         1.0F,
         30.0F,
         40.0F,
         45.0F,
         14.0F,
         new byte[]{4},
         new int[]{2, 4, 1, 4},
         new int[]{1374, 1375},
         new int[]{0, 0, 10, 1, 1, 5, 1, 1, 1, 5, 1, 1, 1}
      ),
      MARLIN(
         (byte)12,
         "marlin",
         569,
         true,
         FishEnums.FeedHeight.TOP,
         250,
         1000,
         true,
         6,
         50,
         "model.creature.fish.marlin",
         0.343F,
         1.0F,
         50.0F,
         50.0F,
         45.0F,
         18.0F,
         new byte[]{4},
         new int[]{4, 2, 4, 3},
         new int[]{1375},
         new int[]{0, 0, 1, 1, 1, 5, 5, 5, 10, 0, 1, 1, 1}
      ),
      BLUESHARK(
         (byte)13,
         "blue shark",
         570,
         true,
         FishEnums.FeedHeight.ANY,
         250,
         1000,
         true,
         5,
         50,
         "model.creature.fish.blueshark",
         1.0F,
         1.0F,
         45.0F,
         50.0F,
         45.0F,
         14.0F,
         new byte[]{4},
         new int[]{4, 3, 4, 2},
         new int[]{1375},
         new int[]{0, 0, 1, 1, 1, 10, 5, 5, 5, 0, 1, 1, 1}
      ),
      DORADO(
         (byte)14,
         "dorado",
         574,
         true,
         FishEnums.FeedHeight.TOP,
         150,
         600,
         true,
         4,
         50,
         "model.creature.fish.dorado",
         1.0F,
         1.0F,
         30.0F,
         50.0F,
         45.0F,
         13.0F,
         new byte[]{4},
         new int[]{4, 2, 4, 3},
         new int[]{1374, 1375},
         new int[]{0, 1, 1, 1, 10, 5, 5, 5, 5, 0, 5, 1, 1}
      ),
      SAILFISH(
         (byte)15,
         "sailfish",
         573,
         true,
         FishEnums.FeedHeight.TOP,
         200,
         800,
         true,
         4,
         50,
         "model.creature.fish.sailfish",
         1.0F,
         1.0F,
         40.0F,
         50.0F,
         45.0F,
         15.0F,
         new byte[]{4},
         new int[]{4, 2, 4, 3},
         new int[]{1375},
         new int[]{0, 0, 0, 1, 1, 5, 5, 5, 5, 0, 10, 0, 0}
      ),
      WHITESHARK(
         (byte)16,
         "white shark",
         571,
         true,
         FishEnums.FeedHeight.ANY,
         150,
         1000,
         true,
         5,
         50,
         "model.creature.fish.whiteshark",
         1.0F,
         1.0F,
         42.0F,
         50.0F,
         45.0F,
         14.0F,
         new byte[]{4},
         new int[]{4, 3, 4, 2},
         new int[]{1375},
         new int[]{0, 0, 1, 1, 1, 5, 8, 10, 5, 0, 1, 1, 1}
      ),
      TUNA(
         (byte)17,
         "tuna",
         575,
         true,
         FishEnums.FeedHeight.TOP,
         150,
         600,
         true,
         2,
         50,
         "model.creature.fish.tuna",
         1.0F,
         1.0F,
         40.0F,
         50.0F,
         45.0F,
         20.0F,
         new byte[]{4},
         new int[]{4, 2, 4, 3},
         new int[]{1374, 1375},
         new int[]{0, 1, 1, 5, 1, 5, 10, 5, 5, 0, 1, 0, 1}
      ),
      MINNOW(
         (byte)18,
         "minnow",
         1338,
         false,
         FishEnums.FeedHeight.ANY,
         0,
         20,
         false,
         1,
         10,
         "model.creature.fish.minnow",
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         new byte[]{2, 3, 7},
         new int[]{4, 2, 4, 1},
         new int[]{1344, 1343, 1372},
         new int[]{5, 10, 1, 1, 1, 0, 0, 0, 0, 1, 5, 1, 1}
      ),
      LOACH(
         (byte)19,
         "loach",
         1339,
         false,
         FishEnums.FeedHeight.ANY,
         10,
         50,
         false,
         2,
         40,
         "model.creature.fish.loach",
         1.0F,
         1.0F,
         5.0F,
         7.0F,
         3.0F,
         2.0F,
         new byte[]{2, 3},
         new int[]{1, 4, 2, 3},
         new int[]{705, 1344, 1372, 1373},
         new int[]{5, 5, 1, 1, 5, 1, 0, 0, 1, 5, 1, 1, 10}
      ),
      WURMFISH(
         (byte)20,
         "wurmfish",
         1340,
         false,
         FishEnums.FeedHeight.BOTTOM,
         40,
         1000,
         false,
         3,
         50,
         "model.creature.fish.wurmfish",
         1.0F,
         1.0F,
         21.0F,
         33.0F,
         11.0F,
         4.0F,
         new byte[]{2, 3},
         new int[]{2, 3, 1, 4},
         new int[]{705, 1344, 1372, 1373},
         new int[]{1, 5, 1, 1, 5, 5, 5, 1, 5, 5, 1, 10, 1}
      ),
      SARDINE(
         (byte)21,
         "sardine",
         1337,
         true,
         FishEnums.FeedHeight.NONE,
         0,
         20,
         false,
         1,
         10,
         "model.creature.fish.sardine",
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         new byte[]{2, 1, 3, 7},
         new int[]{4, 3, 4, 2},
         new int[]{1343},
         new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
      ),
      CLAM(
         (byte)22,
         "clam",
         1394,
         true,
         FishEnums.FeedHeight.ANY,
         0,
         0,
         false,
         1,
         0,
         "model.creature.fish.clam",
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         new byte[0],
         new int[]{1, 1, 1, 1},
         new int[0],
         new int[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10}
      );

      private final byte typeId;
      private String name;
      private final int templateId;
      private ItemTemplate template = null;
      private final boolean onSurface;
      private final FishEnums.FeedHeight feedHeight;
      private final int minDepth;
      private final int maxDepth;
      private final boolean isSpecialFish;
      private final int damageMod;
      private final int minWeight;
      private final String modelName;
      private final float scale;
      private final float baseSpeed;
      private final float bodyStrength;
      private final float bodyStamina;
      private final float bodyControl;
      private final float mindSpeed;
      private boolean inWater = false;
      private boolean inPond = false;
      private boolean inLake = false;
      private boolean inSea = false;
      private boolean inShallows = false;
      private boolean useFishingPole;
      private boolean useFishingNet = false;
      private boolean useSpear = false;
      private boolean useReelBasic = false;
      private boolean useReelFine = false;
      private boolean useReelWater = false;
      private boolean useReelProfessional = false;
      private final int[] feeds;
      private final int[] baits;
      private static final FishEnums.FishData[] types = values();
      private static final Map<Integer, FishEnums.FishData> byTemplateId = new HashMap<>();

      private FishData(
         byte typeId,
         String name,
         int templateId,
         boolean onSurface,
         FishEnums.FeedHeight feedHeight,
         int minDepth,
         int maxDepth,
         boolean specialFish,
         int damageMod,
         int minWeight,
         String modelName,
         float scale,
         float baseSpeed,
         float bodyStrength,
         float bodyStamina,
         float bodyControl,
         float mindSpeed,
         byte[] depths,
         int[] feeds,
         int[] reels,
         int[] baits
      ) {
         this.typeId = typeId;
         this.name = name;
         this.templateId = templateId;
         this.onSurface = onSurface;
         this.feedHeight = feedHeight;
         this.minDepth = minDepth;
         this.maxDepth = maxDepth;
         this.isSpecialFish = specialFish;
         this.damageMod = damageMod;
         this.minWeight = minWeight;
         this.modelName = modelName;
         this.scale = scale;
         this.baseSpeed = baseSpeed;
         this.bodyStrength = bodyStrength;
         this.bodyStamina = bodyStamina;
         this.bodyControl = bodyControl;
         this.mindSpeed = mindSpeed;
         this.assignDepths(depths);
         this.feeds = feeds;
         if (typeId > 0) {
            this.assignReels(reels);
         }

         this.baits = baits;
      }

      public int getTypeId() {
         return this.typeId;
      }

      public String getName() {
         return this.name;
      }

      public int getTemplateId() {
         return this.templateId;
      }

      public boolean inCave() {
         return !this.onSurface;
      }

      public boolean onSurface() {
         return this.onSurface;
      }

      public boolean inWater() {
         return this.inWater;
      }

      public boolean inPond() {
         return this.inPond;
      }

      public boolean inLake() {
         return this.inLake;
      }

      public boolean inSea() {
         return this.inSea;
      }

      public boolean inShallows() {
         return this.inShallows;
      }

      public FishEnums.FeedHeight getFeedHeight() {
         return this.feedHeight;
      }

      public int getMinDepth() {
         return this.minDepth;
      }

      public int getMaxDepth() {
         return this.maxDepth;
      }

      public String getModelName() {
         return this.modelName;
      }

      public float getScaleMod() {
         return 1.0F / this.scale;
      }

      public float getBaseSpeed() {
         return this.baseSpeed;
      }

      public boolean isSpecialFish() {
         return this.isSpecialFish;
      }

      public float getBodyStrength() {
         return this.bodyStrength;
      }

      public float getBodyStamina() {
         return this.bodyStamina;
      }

      public float getBodyControl() {
         return this.bodyControl;
      }

      public float getMindSpeed() {
         return this.mindSpeed;
      }

      public int getDamageMod() {
         return this.damageMod;
      }

      public int getMinWeight() {
         return this.minWeight;
      }

      public boolean useFishingPole() {
         return this.useFishingPole;
      }

      public boolean useFishingNet() {
         return this.useFishingNet;
      }

      public boolean useSpear() {
         return this.useSpear;
      }

      public boolean useReelBasic() {
         return this.useReelBasic;
      }

      public boolean useReelFine() {
         return this.useReelFine;
      }

      public boolean useReelWater() {
         return this.useReelWater;
      }

      public boolean useReelProfessional() {
         return this.useReelProfessional;
      }

      public int[] feeds() {
         return this.feeds;
      }

      public int[] baits() {
         return this.baits;
      }

      private void assignDepths(byte[] depths) {
         for(int depth : depths) {
            switch(depth) {
               case 1:
                  this.inWater = true;
                  break;
               case 2:
                  this.inPond = true;
                  break;
               case 3:
                  this.inLake = true;
                  break;
               case 4:
                  this.inSea = true;
               case 5:
               case 6:
               default:
                  break;
               case 7:
                  this.inShallows = true;
            }
         }
      }

      private void assignReels(int[] reels) {
         for(int reel : reels) {
            switch(reel) {
               case 705:
                  this.useSpear = true;
                  break;
               case 1343:
                  this.useFishingNet = true;
                  break;
               case 1344:
                  this.useFishingPole = true;
                  break;
               case 1372:
                  this.useReelBasic = true;
                  break;
               case 1373:
                  this.useReelFine = true;
                  break;
               case 1374:
                  this.useReelWater = true;
                  break;
               case 1375:
                  this.useReelProfessional = true;
            }
         }
      }

      @Nullable
      public ItemTemplate getTemplate() {
         if (this.templateId == 0) {
            return null;
         } else if (this.template != null) {
            return this.template;
         } else {
            try {
               this.template = ItemTemplateFactory.getInstance().getTemplate(this.templateId);
            } catch (NoSuchTemplateException var2) {
            }

            return this.template;
         }
      }

      public float getTemplateDifficulty() {
         return this.getTemplate() != null ? this.template.getDifficulty() : 100.0F;
      }

      private float addDifficultyDepth(float posx, float posy, boolean isOnSurface) {
         int tilex = (int)posx >> 2;
         int tiley = (int)posy >> 2;
         byte waterType = WaterType.getWaterType(tilex, tiley, isOnSurface);
         float extraWaterTypeDifficulty = 0.0F;
         switch(waterType) {
            case 1:
            case 2:
               if (!this.inPond()) {
                  extraWaterTypeDifficulty = 10.0F;
               }
               break;
            case 3:
               if (!this.inLake()) {
                  extraWaterTypeDifficulty = 15.0F;
               }
               break;
            case 4:
               if (!this.inSea()) {
                  extraWaterTypeDifficulty = 20.0F;
               }
               break;
            case 5:
            case 6:
               if (!this.inShallows()) {
                  extraWaterTypeDifficulty = 10.0F;
               }
         }

         int waterDepth = FishEnums.getWaterDepth(posx, posy, isOnSurface);
         int heightDiff = 0;
         if (waterDepth < this.minDepth) {
            heightDiff = Math.min(Math.abs(this.minDepth - waterDepth), 250);
            return extraWaterTypeDifficulty + Math.min((float)heightDiff / 10.0F, 1.0F);
         } else if (waterDepth > this.maxDepth) {
            heightDiff = Math.min(Math.abs(waterDepth - this.maxDepth), 250);
            return extraWaterTypeDifficulty + Math.min((float)heightDiff / 10.0F, 1.0F);
         } else {
            return extraWaterTypeDifficulty;
         }
      }

      private float addDifficultyFeeding(Item fishingFloat) {
         if (fishingFloat == null) {
            return 10.0F;
         } else {
            switch(this.getFeedHeight()) {
               case TOP:
                  switch(fishingFloat.getTemplateId()) {
                     case 1352:
                        return -5.0F;
                     case 1353:
                        return 0.0F;
                     case 1354:
                        return 8.0F;
                     case 1355:
                        return 8.0F;
                  }
               case BOTTOM:
                  switch(fishingFloat.getTemplateId()) {
                     case 1352:
                        return 8.0F;
                     case 1353:
                        return 0.0F;
                     case 1354:
                        return -5.0F;
                     case 1355:
                        return 8.0F;
                  }
               case ANY:
                  switch(fishingFloat.getTemplateId()) {
                     case 1352:
                        return 8.0F;
                     case 1353:
                        return -5.0F;
                     case 1354:
                        return 8.0F;
                     case 1355:
                        return 0.0F;
                  }
               case TIME:
                  FishEnums.TimeOfDay tod = FishEnums.TimeOfDay.getTimeOfDay();
                  switch(tod) {
                     case MORNING:
                        switch(fishingFloat.getTemplateId()) {
                           case 1352:
                              return -5.0F;
                           case 1353:
                              return 0.0F;
                           case 1354:
                              return 8.0F;
                           case 1355:
                              return -5.0F;
                        }
                     case AFTERNOON:
                        switch(fishingFloat.getTemplateId()) {
                           case 1352:
                              return 8.0F;
                           case 1353:
                              return -5.0F;
                           case 1354:
                              return 8.0F;
                           case 1355:
                              return -5.0F;
                        }
                     case EVENING:
                        switch(fishingFloat.getTemplateId()) {
                           case 1352:
                              return 8.0F;
                           case 1353:
                              return 0.0F;
                           case 1354:
                              return -5.0F;
                           case 1355:
                              return -5.0F;
                        }
                     case NIGHT:
                        switch(fishingFloat.getTemplateId()) {
                           case 1352:
                              return 8.0F;
                           case 1353:
                              return -5.0F;
                           case 1354:
                              return 8.0F;
                           case 1355:
                              return -5.0F;
                        }
                  }
               case NONE:
               default:
                  return 15.0F;
            }
         }
      }

      private float addDifficultyTimeOfDay() {
         FishEnums.TimeOfDay tod = FishEnums.TimeOfDay.getTimeOfDay();
         int feed = this.feeds[tod.typeId];
         int diff = (4 - feed) * 5;
         return (float)diff;
      }

      private float addDifficultyRod(Item rod, Item reel, Item line, Item hook) {
         if (rod.getTemplateId() == 1343) {
            return this.useFishingNet ? 0.0F : 1000.0F;
         } else if (rod.getTemplateId() != 705 && rod.getTemplateId() != 707) {
            if (line != null && hook != null) {
               float diff = 1000.0F;
               if (rod.getTemplateId() == 1344) {
                  if (this.useFishingPole) {
                     diff = -10.0F;
                  } else if (this.useReelBasic) {
                     diff = 5.0F;
                  } else if (this.useReelFine) {
                     diff = 10.0F;
                  } else {
                     diff = 30.0F;
                  }
               } else {
                  switch(reel.getTemplateId()) {
                     case 1372:
                        if (this.useReelBasic) {
                           diff = -10.0F;
                        } else if (this.useFishingPole) {
                           diff = 5.0F;
                        } else if (this.useReelFine) {
                           diff = 10.0F;
                        } else {
                           diff = 30.0F;
                        }
                        break;
                     case 1373:
                        if (this.useReelFine) {
                           diff = -10.0F;
                        } else if (this.useReelBasic) {
                           diff = 5.0F;
                        } else if (this.useReelWater) {
                           diff = 10.0F;
                        } else {
                           diff = 30.0F;
                        }
                        break;
                     case 1374:
                        if (this.useReelWater) {
                           diff = -10.0F;
                        } else if (this.useReelFine) {
                           diff = 5.0F;
                        } else if (this.useReelProfessional) {
                           diff = 10.0F;
                        } else if (this.useReelBasic) {
                           diff = 15.0F;
                        } else {
                           diff = 30.0F;
                        }
                        break;
                     case 1375:
                        if (this.useReelProfessional) {
                           diff = -10.0F;
                        } else if (this.useReelWater) {
                           diff = 5.0F;
                        } else if (this.useReelFine) {
                           diff = 15.0F;
                        } else {
                           diff = 50.0F;
                        }
                  }
               }

               if (diff > 0.0F) {
                  switch(hook.getTemplateId()) {
                     case 1356:
                        diff *= 1.2F;
                     case 1357:
                     default:
                        break;
                     case 1358:
                        diff *= 1.1F;
                  }
               }

               return diff;
            } else {
               return 1000.0F;
            }
         } else if (this.useSpear) {
            return rod.getTemplateId() == 705 ? 2.0F : 0.0F;
         } else {
            return 1000.0F;
         }
      }

      private float addDifficultyBait(Item bait) {
         byte baitId = FishEnums.BaitType.fromItem(bait).getTypeId();
         return 10.0F - (float)this.baits[baitId];
      }

      public float getDifficulty(
         float skill, float posX, float posY, boolean onSurface, Item rod, Item reel, Item line, Item fishingFloat, Item hook, Item bait
      ) {
         if (this.getTypeId() == CLAM.getTypeId()) {
            return skill - 10.0F;
         } else {
            float difficulty = this.getTemplateDifficulty();
            difficulty += this.addDifficultyDepth(posX, posY, onSurface);
            difficulty += this.addDifficultyFeeding(fishingFloat);
            difficulty += this.addDifficultyTimeOfDay();
            difficulty += this.addDifficultyRod(rod, reel, line, hook);
            difficulty += this.addDifficultyBait(bait);
            return Math.min(Math.max(difficulty, -50.0F), 100.0F);
         }
      }

      private float getChanceDefault(float skill) {
         float diff = 0.0F;
         if (this.getTemplate() != null) {
            diff = this.template.getDifficulty();
         }

         if (diff > 0.0F) {
            float flip = 110.0F - diff;
            float smd = skill - diff;
            double rad = Math.toRadians((double)smd);
            float sin = (float)Math.sin(rad);
            float mult = 1.0F + sin;
            return flip * mult;
         } else {
            return 50.0F;
         }
      }

      private float multChanceDepth(float posx, float posy, boolean isOnSurface) {
         int tilex = (int)posx >> 2;
         int tiley = (int)posy >> 2;
         byte waterType = WaterType.getWaterType(tilex, tiley, isOnSurface);
         switch(waterType) {
            case 1:
            case 2:
               if (!this.inPond()) {
                  return 0.0F;
               }
               break;
            case 3:
               if (!this.inLake()) {
                  return 0.0F;
               }
               break;
            case 4:
               if (!this.inSea()) {
                  return 0.0F;
               }
               break;
            case 5:
               if (!this.inSea() && !this.inShallows()) {
                  return 0.0F;
               }
               break;
            case 6:
               if (!this.inLake() && !this.inShallows()) {
                  return 0.0F;
               }
         }

         int waterDepth = FishEnums.getWaterDepth(posx, posy, isOnSurface);
         int heightDiff = 0;
         if (waterDepth < this.minDepth) {
            heightDiff = Math.min(Math.abs(this.minDepth - waterDepth), 250);
            return 1.0F - Math.min((float)heightDiff / 300.0F, 1.0F);
         } else if (waterDepth > this.maxDepth) {
            heightDiff = Math.min(Math.abs(waterDepth - this.maxDepth), 250);
            return 1.0F - Math.min((float)heightDiff / 500.0F, 1.0F);
         } else {
            return 1.0F;
         }
      }

      private float multChanceFeeding(Item fishingFloat) {
         if (fishingFloat == null) {
            return 0.5F;
         } else {
            switch(this.getFeedHeight()) {
               case TOP:
                  switch(fishingFloat.getTemplateId()) {
                     case 1352:
                        return 1.2F;
                     case 1353:
                        return 1.0F;
                     case 1354:
                        return 0.8F;
                     case 1355:
                        return 0.8F;
                  }
               case BOTTOM:
                  switch(fishingFloat.getTemplateId()) {
                     case 1352:
                        return 0.8F;
                     case 1353:
                        return 1.0F;
                     case 1354:
                        return 1.2F;
                     case 1355:
                        return 0.8F;
                  }
               case ANY:
                  switch(fishingFloat.getTemplateId()) {
                     case 1352:
                        return 0.8F;
                     case 1353:
                        return 1.2F;
                     case 1354:
                        return 0.8F;
                     case 1355:
                        return 1.0F;
                  }
               case TIME:
                  FishEnums.TimeOfDay tod = FishEnums.TimeOfDay.getTimeOfDay();
                  switch(tod) {
                     case MORNING:
                        switch(fishingFloat.getTemplateId()) {
                           case 1352:
                              return 1.2F;
                           case 1353:
                              return 1.0F;
                           case 1354:
                              return 0.8F;
                           case 1355:
                              return 1.2F;
                        }
                     case AFTERNOON:
                        switch(fishingFloat.getTemplateId()) {
                           case 1352:
                              return 0.8F;
                           case 1353:
                              return 1.2F;
                           case 1354:
                              return 0.8F;
                           case 1355:
                              return 1.2F;
                        }
                     case EVENING:
                        switch(fishingFloat.getTemplateId()) {
                           case 1352:
                              return 0.8F;
                           case 1353:
                              return 1.0F;
                           case 1354:
                              return 1.2F;
                           case 1355:
                              return 1.2F;
                        }
                     case NIGHT:
                        switch(fishingFloat.getTemplateId()) {
                           case 1352:
                              return 0.8F;
                           case 1353:
                              return 1.2F;
                           case 1354:
                              return 0.8F;
                           case 1355:
                              return 1.2F;
                        }
                  }
               case NONE:
               default:
                  return 0.5F;
            }
         }
      }

      private float multChanceTimeOfDay() {
         FishEnums.TimeOfDay tod = FishEnums.TimeOfDay.getTimeOfDay();
         int feed = this.feeds[tod.typeId];
         int chance = 1 + (feed - 4) / 8;
         return (float)chance;
      }

      private float multChanceRod(Item rod, Item reel, Item line, Item hook, Item bait) {
         if (rod.getTemplateId() == 1343) {
            return this.useFishingNet ? 1.0F : 0.0F;
         } else if (rod.getTemplateId() != 705 && rod.getTemplateId() != 707) {
            if (line != null && hook != null) {
               float chance = 0.0F;
               if (rod.getTemplateId() == 1344) {
                  if (this.useFishingPole) {
                     chance = 1.0F;
                  } else if (this.useReelBasic) {
                     chance = 0.75F;
                  } else if (this.useReelFine) {
                     chance = 0.5F;
                  } else {
                     chance = 0.1F;
                  }
               } else {
                  switch(reel.getTemplateId()) {
                     case 1372:
                        if (this.useReelBasic) {
                           chance = 1.0F;
                        } else if (this.useFishingPole) {
                           chance = 0.75F;
                        } else if (this.useReelFine) {
                           chance = 0.75F;
                        } else if (this.useReelWater) {
                           chance = 0.25F;
                        } else {
                           chance = 0.1F;
                        }
                        break;
                     case 1373:
                        if (this.useReelFine) {
                           chance = 1.0F;
                        } else if (this.useReelBasic) {
                           chance = 0.75F;
                        } else if (this.useFishingPole) {
                           chance = 0.25F;
                        } else if (this.useReelWater) {
                           chance = 0.5F;
                        } else {
                           chance = 0.1F;
                        }
                        break;
                     case 1374:
                        if (this.useReelWater) {
                           chance = 1.0F;
                        } else if (this.useReelFine) {
                           chance = 0.75F;
                        } else if (this.useReelProfessional) {
                           chance = 0.5F;
                        } else {
                           chance = 0.1F;
                        }
                        break;
                     case 1375:
                        if (this.useReelProfessional) {
                           chance = 1.0F;
                        } else if (this.useReelWater) {
                           chance = 0.85F;
                        } else if (this.useReelFine) {
                           chance = 0.45F;
                        } else {
                           chance = 0.1F;
                        }
                  }
               }

               if (chance > 0.0F) {
                  switch(hook.getTemplateId()) {
                     case 1356:
                        chance *= 0.8F;
                     case 1357:
                     default:
                        break;
                     case 1358:
                        chance *= 0.9F;
                  }
               }

               return chance * this.multChanceBait(bait);
            } else {
               return 0.0F;
            }
         } else if (this.useSpear) {
            return rod.getTemplateId() == 705 ? 0.8F : 1.0F;
         } else {
            return 0.0F;
         }
      }

      private float multChanceBait(Item bait) {
         byte baitId = FishEnums.BaitType.fromItem(bait).getTypeId();
         if (this.typeId == -1) {
            System.out.println(this.name + "(bait):" + baitId + " " + this.baits[baitId]);
         }

         switch(this.baits[baitId]) {
            case 0:
               return 0.0F;
            case 1:
               return 0.8F;
            case 2:
               return 0.82F;
            case 3:
               return 0.84F;
            case 4:
               return 0.86F;
            case 5:
               return 0.88F;
            case 6:
               return 0.9F;
            case 7:
               return 0.925F;
            case 8:
               return 0.95F;
            case 9:
               return 0.975F;
            case 10:
               return 1.0F;
            default:
               return 0.0F;
         }
      }

      public Point getSpecialSpot(int zoneX, int zoneY, int season) {
         Random r = new Random((long)(this.getTypeId() * 5 + Servers.localServer.id * 100 + season));
         int rx = zoneX * 128 + 5 + r.nextInt(118);
         int ry = zoneY * 128 + 5 + r.nextInt(118);
         return new Point(rx, ry, this.getTemplateId());
      }

      public float getChance(float skill, Item rod, Item reel, Item line, Item fishingFloat, Item hook, Item bait, float posX, float posY, boolean onSurface) {
         if (this.onSurface() != onSurface) {
            return 0.0F;
         } else {
            float chance = this.getChanceDefault(skill);
            if (this.typeId == -1) {
               System.out.println(this.name + "(default):" + chance);
            }

            chance *= this.multChanceDepth(posX, posY, onSurface);
            if (this.typeId == -1) {
               System.out.println(this.name + "(depth):" + chance);
            }

            chance *= this.multChanceFeeding(fishingFloat);
            if (this.typeId == -1) {
               System.out.println(this.name + "(feed):" + chance);
            }

            chance *= this.multChanceTimeOfDay();
            if (this.typeId == -1) {
               System.out.println(this.name + "(time):" + chance);
            }

            chance *= this.multChanceRod(rod, reel, line, hook, bait);
            if (this.typeId == -1) {
               System.out.println(this.name + "(rod):" + chance + " " + (bait == null));
            }

            if (this.isSpecialFish && chance > 0.0F) {
               int tilex = (int)posX >> 2;
               int tiley = (int)posY >> 2;
               int season = WurmCalendar.getSeasonNumber();
               int zoneX = tilex / 128;
               int zoneY = tiley / 128;
               Point specialSpot = this.getSpecialSpot(zoneX, zoneY, season);
               int farAwayX = Math.abs(specialSpot.getX() - tilex);
               int farAwayY = Math.abs(specialSpot.getY() - tiley);
               int farAway = Math.max(farAwayX, farAwayY);
               float rt2 = 15.0F;
               float nc = 0.0F;
               float ht = 0.0F;

               try {
                  ht = Zones.calculateHeight(posX, posY, onSurface) * 10.0F;
               } catch (NoSuchZoneException var27) {
               }

               if ((float)farAway <= 15.0F && ht < -100.0F) {
                  float dpt = 6.0F;
                  double rad = Math.toRadians((double)((float)farAway * 6.0F));
                  nc = (float)Math.cos(rad);
               }

               chance *= nc;
               if (this.typeId == -1) {
                  System.out.println(this.name + "(special):" + farAway + " " + chance + " " + nc);
               }
            }

            return chance;
         }
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishEnums.FishData fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      @Nullable
      public static FishEnums.FishData fromName(String name) {
         for(FishEnums.FishData fd : types) {
            if (fd.getName().equalsIgnoreCase(name)) {
               return fd;
            }
         }

         return null;
      }

      public static FishEnums.FishData fromItem(@Nullable Item fish) {
         if (fish == null) {
            return NONE;
         } else {
            FishEnums.FishData fishData = byTemplateId.get(fish.getTemplateId());
            return fishData == null ? NONE : fishData;
         }
      }

      static {
         for(FishEnums.FishData fd : types) {
            byTemplateId.put(fd.getTemplateId(), fd);
         }
      }
   }

   public static enum FloatType {
      NONE((byte)0, 0),
      FEATHER((byte)1, 1352),
      TWIG((byte)2, 1353),
      MOSS((byte)3, 1354),
      BARK((byte)4, 1355);

      private final byte typeId;
      private final int templateId;
      private static final FishEnums.FloatType[] types = values();
      private static final Map<Integer, FishEnums.FloatType> byTemplateId = new HashMap<>();

      private FloatType(byte id, int templateId) {
         this.typeId = id;
         this.templateId = templateId;
      }

      public byte getTypeId() {
         return this.typeId;
      }

      public int getTemplateId() {
         return this.templateId;
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishEnums.FloatType fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      public static FishEnums.FloatType fromItem(@Nullable Item afloat) {
         if (afloat == null) {
            return NONE;
         } else {
            FishEnums.FloatType floatType = byTemplateId.get(afloat.getTemplateId());
            return floatType == null ? NONE : floatType;
         }
      }

      static {
         for(FishEnums.FloatType ft : types) {
            byTemplateId.put(ft.getTemplateId(), ft);
         }
      }
   }

   public static enum HookType {
      NONE((byte)0, 0),
      WOOD((byte)1, 1356),
      METAL((byte)2, 1357),
      BONE((byte)3, 1358);

      private final byte typeId;
      private final int templateId;
      private static final FishEnums.HookType[] types = values();
      private static final Map<Integer, FishEnums.HookType> byTemplateId = new HashMap<>();

      private HookType(byte id, int templateId) {
         this.typeId = id;
         this.templateId = templateId;
      }

      public byte getTypeId() {
         return this.typeId;
      }

      public int getTemplateId() {
         return this.templateId;
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishEnums.HookType fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      public static FishEnums.HookType fromItem(@Nullable Item hook) {
         if (hook == null) {
            return NONE;
         } else {
            for(FishEnums.HookType ft : types) {
               if (ft.getTemplateId() == hook.getTemplateId()) {
                  return ft;
               }
            }

            return NONE;
         }
      }

      static {
         for(FishEnums.HookType ht : types) {
            byTemplateId.put(ht.getTemplateId(), ht);
         }
      }
   }

   public static enum ReelType {
      NONE((byte)0, 0, 1347),
      LIGHT((byte)1, 1372, 1348),
      MEDIUM((byte)2, 1373, 1349),
      DEEP_WATER((byte)3, 1374, 1350),
      PROFESSIONAL((byte)4, 1375, 1351);

      private final byte typeId;
      private final int templateId;
      private final int associatedLine;
      private static final FishEnums.ReelType[] types = values();
      private static final Map<Integer, FishEnums.ReelType> byTemplateId = new HashMap<>();

      private ReelType(byte id, int templateId, int associatedLine) {
         this.typeId = id;
         this.templateId = templateId;
         this.associatedLine = associatedLine;
      }

      public byte getTypeId() {
         return this.typeId;
      }

      public int getTemplateId() {
         return this.templateId;
      }

      public int getAssociatedLineTemplateId() {
         return this.associatedLine;
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishEnums.ReelType fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      public static FishEnums.ReelType fromItem(@Nullable Item reel) {
         if (reel == null) {
            return NONE;
         } else {
            FishEnums.ReelType reelType = byTemplateId.get(reel.getTemplateId());
            return reelType == null ? NONE : reelType;
         }
      }

      static {
         for(FishEnums.ReelType rt : types) {
            byTemplateId.put(rt.getTemplateId(), rt);
         }
      }
   }

   public static enum TimeOfDay {
      MORNING((byte)0),
      AFTERNOON((byte)1),
      EVENING((byte)2),
      NIGHT((byte)3);

      private final byte typeId;

      private TimeOfDay(byte id) {
         this.typeId = id;
      }

      public int getTypeId() {
         return this.typeId;
      }

      public static final FishEnums.TimeOfDay getTimeOfDay() {
         if (WurmCalendar.getHour() >= 4 && (WurmCalendar.getHour() != 4 || WurmCalendar.getMinute() > 30)) {
            if (WurmCalendar.getHour() >= 10 && (WurmCalendar.getHour() != 10 || WurmCalendar.getMinute() > 30)) {
               if (WurmCalendar.getHour() >= 16 && (WurmCalendar.getHour() != 16 || WurmCalendar.getMinute() > 30)) {
                  return WurmCalendar.getHour() >= 22 && (WurmCalendar.getHour() != 22 || WurmCalendar.getMinute() > 30) ? NIGHT : EVENING;
               } else {
                  return AFTERNOON;
               }
            } else {
               return MORNING;
            }
         } else {
            return NIGHT;
         }
      }
   }
}
