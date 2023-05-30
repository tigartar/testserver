package com.wurmonline.shared.constants;

public class FishingEnums {
   public static final int FISH_SPOT_RADIUS = 5;
   public static final int FISH_SPOT_ZONE_SIZE = 128;
   public static final byte FISH_TYPE_NONE = 0;
   public static final byte FISH_TYPE_ROACH = 1;
   public static final byte FISH_TYPE_PERCH = 2;
   public static final byte FISH_TYPE_TROUT = 3;
   public static final byte FISH_TYPE_PIKE = 4;
   public static final byte FISH_TYPE_CATFISH = 5;
   public static final byte FISH_TYPE_SNOOK = 6;
   public static final byte FISH_TYPE_HERRING = 7;
   public static final byte FISH_TYPE_CARP = 8;
   public static final byte FISH_TYPE_BASS = 9;
   public static final byte FISH_TYPE_SALMON = 10;
   public static final byte FISH_TYPE_OCTOPUS = 11;
   public static final byte FISH_TYPE_MARLIN = 12;
   public static final byte FISH_TYPE_BLUESHARK = 13;
   public static final byte FISH_TYPE_DORADO = 14;
   public static final byte FISH_TYPE_SAILFISH = 15;
   public static final byte FISH_TYPE_WHITESHARK = 16;
   public static final byte FISH_TYPE_TUNA = 17;
   public static final byte FISH_TYPE_MINNOW = 18;
   public static final byte FISH_TYPE_LOACH = 19;
   public static final byte FISH_TYPE_WURMFISH = 20;
   public static final byte FISH_TYPE_SARDINE = 21;
   public static final byte FISH_TYPE_CLAM = 22;
   public static final byte ROD_TYPE_FISHING_POLE = 0;
   public static final byte ROD_TYPE_FISHING_ROD_BASIC = 1;
   public static final byte ROD_TYPE_FISHING_ROD_FINE = 2;
   public static final byte ROD_TYPE_FISHING_ROD_DEEP_WATER = 3;
   public static final byte ROD_TYPE_FISHING_ROD_DEEP_SEA = 4;
   public static final byte ROD_TYPE_FISHING_ROD_BASIC_WITH_LINE = 5;
   public static final byte ROD_TYPE_FISHING_ROD_FINE_WITH_LINE = 6;
   public static final byte ROD_TYPE_FISHING_ROD_DEEP_WATER_WITH_LINE = 7;
   public static final byte ROD_TYPE_FISHING_ROD_DEEP_SEA_WITH_LINE = 8;
   public static final byte FLOAT_TYPE_NONE = 0;
   public static final byte FLOAT_TYPE_FEATHER = 1;
   public static final byte FLOAT_TYPE_TWIG = 2;
   public static final byte FLOAT_TYPE_MOSS = 3;
   public static final byte FLOAT_TYPE_BARK = 4;
   public static final byte BAIT_TYPE_NONE = 0;
   public static final byte BAIT_TYPE_FLY = 1;
   public static final byte BAIT_TYPE_CHEESE = 2;
   public static final byte BAIT_TYPE_DOUGH = 3;
   public static final byte BAIT_TYPE_WURM = 4;
   public static final byte BAIT_TYPE_SARDINE = 5;
   public static final byte BAIT_TYPE_ROACH = 6;
   public static final byte BAIT_TYPE_PERCH = 7;
   public static final byte BAIT_TYPE_MINNOW = 8;
   public static final byte BAIT_TYPE_FISH_BAIT = 9;
   public static final byte BAIT_TYPE_GRUB = 10;
   public static final byte BAIT_TYPE_WHEAT = 11;
   public static final byte BAIT_TYPE_CORN = 12;
   public static final byte REEL_TYPE_NONE = 0;
   public static final byte REEL_TYPE_LIGHT = 1;
   public static final byte REEL_TYPE_MEDIUM = 2;
   public static final byte REEL_TYPE_DEEP_WATER = 3;
   public static final byte REEL_TYPE_PROFESSIONAL = 4;
   public static final byte HOOK_TYPE_NONE = 0;
   public static final byte HOOK_TYPE_WOOD = 1;
   public static final byte HOOK_TYPE_METAL = 2;
   public static final byte HOOK_TYPE_BONE = 3;

   private FishingEnums() {
   }

   public static enum BaitType {
      NONE((byte)0, ""),
      FLY((byte)1, "model.bait.fly."),
      CHEESE((byte)2, "model.bait.cheese."),
      DOUGH((byte)3, "model.bait.dough."),
      WURM((byte)4, "model.bait.wurm."),
      SARDINE((byte)5, "model.fish.sardine."),
      ROACH((byte)6, "model.fish.roach."),
      PERCH((byte)7, "model.fish.perch."),
      MINNOW((byte)8, "model.fish.cave.minnow."),
      FISH_BAIT((byte)9, "model.bait.fish."),
      GRUB((byte)10, "model.bait.grub."),
      WHEAT((byte)11, "model.bait.wheat."),
      CORN((byte)12, "model.bait.corn.");

      private final byte typeId;
      private final String modelName;
      private static final FishingEnums.BaitType[] types = values();

      private BaitType(byte id, String modelName) {
         this.typeId = id;
         this.modelName = modelName;
      }

      public byte getTypeId() {
         return this.typeId;
      }

      public String getModelName() {
         return this.modelName;
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishingEnums.BaitType fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      public String getModelName(byte id) {
         return fromInt(id).getModelName();
      }
   }

   public static enum FishType {
      NONE((byte)0, ""),
      ROACH((byte)1, "model.fish.roach."),
      PERCH((byte)2, "model.fish.perch."),
      TROUT((byte)3, "model.fish.trout."),
      PIKE((byte)4, "model.fish.pike."),
      CATFISH((byte)5, "model.fish.catfish."),
      SNOOK((byte)6, "model.fish.snook."),
      HERRING((byte)7, "model.fish.herring."),
      CARP((byte)8, "model.fish.carp."),
      BASS((byte)9, "model.fish.bass."),
      SALMON((byte)10, "model.fish.salmon."),
      OCTOPUS((byte)11, "model.fish.octopus."),
      MARLIN((byte)12, "model.fish.marlin."),
      BLUESHARK((byte)13, "model.fish.blueshark."),
      DORADO((byte)14, "model.fish.dorado."),
      SAILFISH((byte)15, "model.fish.sailfish."),
      WHITESHARK((byte)16, "model.fish.whiteshark."),
      TUNA((byte)17, "model.fish.tuna."),
      MINNOW((byte)18, "model.fish.minnow."),
      LOACH((byte)19, "model.fish.loach."),
      WURMFISH((byte)20, "model.fish.wurmfish."),
      SARDINE((byte)21, "model.fish.sardine."),
      CLAM((byte)22, "model.fish.clam.");

      private final byte typeId;
      private final String modelName;
      private static final FishingEnums.FishType[] types = values();

      private FishType(byte typeId, String modelName) {
         this.typeId = typeId;
         this.modelName = modelName;
      }

      public int getTypeId() {
         return this.typeId;
      }

      public String getModelName() {
         return this.modelName;
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishingEnums.FishType fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      public String getModelName(byte id) {
         return fromInt(id).getModelName();
      }
   }

   public static enum FloatType {
      NONE((byte)0, ""),
      FEATHER((byte)1, "model.float.feather."),
      TWIG((byte)2, "model.float.twig."),
      MOSS((byte)3, "model.float.moss."),
      BARK((byte)4, "model.float.bark.");

      private final byte typeId;
      private final String modelName;
      private static final FishingEnums.FloatType[] types = values();

      private FloatType(byte id, String modelName) {
         this.typeId = id;
         this.modelName = modelName;
      }

      public byte getTypeId() {
         return this.typeId;
      }

      public String getModelName() {
         return this.modelName;
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishingEnums.FloatType fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      public String getModelName(byte id) {
         return fromInt(id).getModelName();
      }
   }

   public static enum HookType {
      NONE((byte)0, ""),
      WOOD((byte)1, "model.tool.fish.hook."),
      METAL((byte)2, "model.tool.fish.hook."),
      BONE((byte)3, "model.tool.fish.hook.");

      private final byte typeId;
      private final String modelName;
      private static final FishingEnums.HookType[] types = values();

      private HookType(byte id, String modelName) {
         this.typeId = id;
         this.modelName = modelName;
      }

      public byte getTypeId() {
         return this.typeId;
      }

      public String getModelName() {
         return this.modelName;
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishingEnums.HookType fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      public String getModelName(byte id) {
         return fromInt(id).getModelName();
      }
   }

   public static enum ReelType {
      NONE((byte)0, ""),
      LIGHT((byte)1, "model.fishingreel.light."),
      MEDIUM((byte)2, "model.fishingreel.medium."),
      DEEP_WATER((byte)3, "model.fishingreel.deepwater."),
      PROFESSIONAL((byte)4, "model.fishingreel.professional.");

      private final byte typeId;
      private final String modelName;
      private static final FishingEnums.ReelType[] types = values();

      private ReelType(byte id, String modelName) {
         this.typeId = id;
         this.modelName = modelName;
      }

      public byte getTypeId() {
         return this.typeId;
      }

      public String getModelName() {
         return this.modelName;
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishingEnums.ReelType fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      public String getModelName(byte id) {
         return fromInt(id).getModelName();
      }
   }

   public static enum RodType {
      FISHING_POLE((byte)0, "model.fish.pole.", (short)786),
      FISHING_ROD_BASIC((byte)1, "model.fish.rod.basic.", (short)866),
      FISHING_ROD_FINE((byte)2, "model.fish.rod.fine.", (short)866),
      FISHING_ROD_DEEP_WATER((byte)3, "model.fish.rod.water.", (short)866),
      FISHING_ROD_DEEP_SEA((byte)4, "model.fish.rod.sea.", (short)866),
      FISHING_ROD_BASIC_WITH_LINE((byte)5, "model.fish.rod.basic.", (short)886),
      FISHING_ROD_FINE_WITH_LINE((byte)6, "model.fish.rod.fine.", (short)886),
      FISHING_ROD_DEEP_WATER_WITH_LINE((byte)7, "model.fish.rod.water.", (short)886),
      FISHING_ROD_DEEP_SEA_WITH_LINE((byte)8, "model.fish.rod.sea.", (short)886);

      private final byte typeId;
      private final String modelName;
      private final short icon;
      private static final FishingEnums.RodType[] types = values();

      private RodType(byte id, String modelName, short icon) {
         this.typeId = id;
         this.modelName = modelName;
         this.icon = icon;
      }

      public byte getTypeId() {
         return this.typeId;
      }

      public String getModelName() {
         return this.modelName;
      }

      public int getIcon() {
         return this.icon;
      }

      public static final int getLength() {
         return types.length;
      }

      public static FishingEnums.RodType fromInt(int id) {
         return id >= getLength() ? types[0] : types[id & 0xFF];
      }

      public String getModelName(byte id) {
         return fromInt(id).getModelName();
      }
   }
}
