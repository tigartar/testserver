/*
 * Decompiled with CFR 0.152.
 */
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

    public static enum HookType {
        NONE(0, ""),
        WOOD(1, "model.tool.fish.hook."),
        METAL(2, "model.tool.fish.hook."),
        BONE(3, "model.tool.fish.hook.");

        private final byte typeId;
        private final String modelName;
        private static final HookType[] types;

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

        public static HookType fromInt(int id) {
            if (id >= HookType.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        public String getModelName(byte id) {
            return HookType.fromInt(id).getModelName();
        }

        static {
            types = HookType.values();
        }
    }

    public static enum ReelType {
        NONE(0, ""),
        LIGHT(1, "model.fishingreel.light."),
        MEDIUM(2, "model.fishingreel.medium."),
        DEEP_WATER(3, "model.fishingreel.deepwater."),
        PROFESSIONAL(4, "model.fishingreel.professional.");

        private final byte typeId;
        private final String modelName;
        private static final ReelType[] types;

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

        public static ReelType fromInt(int id) {
            if (id >= ReelType.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        public String getModelName(byte id) {
            return ReelType.fromInt(id).getModelName();
        }

        static {
            types = ReelType.values();
        }
    }

    public static enum BaitType {
        NONE(0, ""),
        FLY(1, "model.bait.fly."),
        CHEESE(2, "model.bait.cheese."),
        DOUGH(3, "model.bait.dough."),
        WURM(4, "model.bait.wurm."),
        SARDINE(5, "model.fish.sardine."),
        ROACH(6, "model.fish.roach."),
        PERCH(7, "model.fish.perch."),
        MINNOW(8, "model.fish.cave.minnow."),
        FISH_BAIT(9, "model.bait.fish."),
        GRUB(10, "model.bait.grub."),
        WHEAT(11, "model.bait.wheat."),
        CORN(12, "model.bait.corn.");

        private final byte typeId;
        private final String modelName;
        private static final BaitType[] types;

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

        public static BaitType fromInt(int id) {
            if (id >= BaitType.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        public String getModelName(byte id) {
            return BaitType.fromInt(id).getModelName();
        }

        static {
            types = BaitType.values();
        }
    }

    public static enum FloatType {
        NONE(0, ""),
        FEATHER(1, "model.float.feather."),
        TWIG(2, "model.float.twig."),
        MOSS(3, "model.float.moss."),
        BARK(4, "model.float.bark.");

        private final byte typeId;
        private final String modelName;
        private static final FloatType[] types;

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

        public static FloatType fromInt(int id) {
            if (id >= FloatType.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        public String getModelName(byte id) {
            return FloatType.fromInt(id).getModelName();
        }

        static {
            types = FloatType.values();
        }
    }

    public static enum RodType {
        FISHING_POLE(0, "model.fish.pole.", 786),
        FISHING_ROD_BASIC(1, "model.fish.rod.basic.", 866),
        FISHING_ROD_FINE(2, "model.fish.rod.fine.", 866),
        FISHING_ROD_DEEP_WATER(3, "model.fish.rod.water.", 866),
        FISHING_ROD_DEEP_SEA(4, "model.fish.rod.sea.", 866),
        FISHING_ROD_BASIC_WITH_LINE(5, "model.fish.rod.basic.", 886),
        FISHING_ROD_FINE_WITH_LINE(6, "model.fish.rod.fine.", 886),
        FISHING_ROD_DEEP_WATER_WITH_LINE(7, "model.fish.rod.water.", 886),
        FISHING_ROD_DEEP_SEA_WITH_LINE(8, "model.fish.rod.sea.", 886);

        private final byte typeId;
        private final String modelName;
        private final short icon;
        private static final RodType[] types;

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

        public static RodType fromInt(int id) {
            if (id >= RodType.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        public String getModelName(byte id) {
            return RodType.fromInt(id).getModelName();
        }

        static {
            types = RodType.values();
        }
    }

    public static enum FishType {
        NONE(0, ""),
        ROACH(1, "model.fish.roach."),
        PERCH(2, "model.fish.perch."),
        TROUT(3, "model.fish.trout."),
        PIKE(4, "model.fish.pike."),
        CATFISH(5, "model.fish.catfish."),
        SNOOK(6, "model.fish.snook."),
        HERRING(7, "model.fish.herring."),
        CARP(8, "model.fish.carp."),
        BASS(9, "model.fish.bass."),
        SALMON(10, "model.fish.salmon."),
        OCTOPUS(11, "model.fish.octopus."),
        MARLIN(12, "model.fish.marlin."),
        BLUESHARK(13, "model.fish.blueshark."),
        DORADO(14, "model.fish.dorado."),
        SAILFISH(15, "model.fish.sailfish."),
        WHITESHARK(16, "model.fish.whiteshark."),
        TUNA(17, "model.fish.tuna."),
        MINNOW(18, "model.fish.minnow."),
        LOACH(19, "model.fish.loach."),
        WURMFISH(20, "model.fish.wurmfish."),
        SARDINE(21, "model.fish.sardine."),
        CLAM(22, "model.fish.clam.");

        private final byte typeId;
        private final String modelName;
        private static final FishType[] types;

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

        public static FishType fromInt(int id) {
            if (id >= FishType.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        public String getModelName(byte id) {
            return FishType.fromInt(id).getModelName();
        }

        static {
            types = FishType.values();
        }
    }
}

