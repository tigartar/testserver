/*
 * Decompiled with CFR 0.152.
 */
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
            return (int)(-Zones.calculateHeight(posx, posy, isOnSurface) * 10.0f);
        }
        catch (NoSuchZoneException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return 5;
        }
    }

    public static enum FishData {
        NONE(0, "unknown", 0, true, FeedHeight.NONE, 0, 0, false, 0, 0, "model.creature.fish", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, new byte[0], new int[]{1, 1, 1, 1}, new int[0], new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}),
        ROACH(1, "roach", 162, true, FeedHeight.BOTTOM, 0, 30, false, 1, 30, "model.creature.fish.roach", 1.5f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, new byte[]{2, 3, 7}, new int[]{4, 4, 3, 1}, new int[]{705, 1343, 1344, 1372}, new int[]{8, 5, 1, 5, 8, 1, 0, 0, 1, 5, 10, 5, 5}),
        PERCH(2, "perch", 163, true, FeedHeight.ANY, 0, 40, false, 1, 40, "model.creature.fish.perch", 1.5f, 1.0f, 2.0f, 2.0f, 2.0f, 2.0f, new byte[]{2, 3, 7}, new int[]{4, 3, 3, 2}, new int[]{705, 1343, 1344, 1372}, new int[]{8, 5, 5, 10, 5, 5, 1, 0, 5, 5, 8, 5, 5}),
        TROUT(3, "brook trout", 165, true, FeedHeight.BOTTOM, 60, 150, false, 2, 50, "model.creature.fish.trout", 1.0f, 1.0f, 4.0f, 4.0f, 4.0f, 4.0f, new byte[]{2, 3}, new int[]{4, 2, 4, 2}, new int[]{705, 1373, 1374}, new int[]{1, 10, 1, 5, 5, 5, 5, 5, 5, 1, 5, 1, 5}),
        PIKE(4, "pike", 157, true, FeedHeight.BOTTOM, 10, 100, false, 4, 50, "model.creature.fish.pike", 0.74f, 1.0f, 8.0f, 10.0f, 5.0f, 2.0f, new byte[]{3}, new int[]{3, 2, 4, 4}, new int[]{1372, 1373}, new int[]{1, 5, 1, 1, 1, 5, 5, 5, 5, 1, 1, 10, 1}),
        CATFISH(5, "catfish", 160, true, FeedHeight.BOTTOM, 20, 100, false, 4, 50, "model.creature.fish.catfish", 0.85f, 1.0f, 20.0f, 22.0f, 13.0f, 5.0f, new byte[]{3, 4}, new int[]{4, 4, 3, 2}, new int[]{705, 1372, 1373, 1374}, new int[]{1, 5, 5, 5, 5, 5, 7, 5, 5, 10, 5, 5, 5}),
        SNOOK(6, "snook", 161, true, FeedHeight.TIME, 10, 250, false, 5, 50, "model.creature.fish.snook", 0.85f, 1.0f, 15.0f, 17.0f, 14.0f, 8.0f, new byte[]{4}, new int[]{4, 2, 3, 4}, new int[]{1372, 1373, 1374}, new int[]{0, 1, 1, 1, 1, 1, 1, 5, 5, 5, 1, 1, 10}),
        HERRING(7, "herring", 159, true, FeedHeight.TIME, 10, 150, false, 1, 50, "model.creature.fish.herring", 2.35f, 1.0f, 10.0f, 15.0f, 10.0f, 7.0f, new byte[]{3, 4}, new int[]{2, 3, 4, 2}, new int[]{1372, 1373}, new int[]{1, 5, 5, 5, 1, 5, 5, 1, 1, 1, 10, 1, 1}),
        CARP(8, "carp", 164, true, FeedHeight.ANY, 5, 200, false, 3, 50, "model.creature.fish.carp", 0.5f, 1.0f, 13.0f, 18.0f, 11.0f, 8.0f, new byte[]{2, 3}, new int[]{4, 4, 3, 3}, new int[]{705, 1344, 1372, 1373, 1374, 1375}, new int[]{10, 5, 5, 5, 5, 1, 1, 1, 1, 5, 5, 5, 5}),
        BASS(9, "smallmouth bass", 158, true, FeedHeight.BOTTOM, 0, 60, false, 2, 50, "model.creature.fish.bass", 1.03f, 1.0f, 15.0f, 21.0f, 14.0f, 11.0f, new byte[]{3, 4}, new int[]{2, 2, 4, 4}, new int[]{1344, 1372, 1373}, new int[]{1, 1, 1, 1, 10, 1, 1, 1, 1, 5, 1, 1, 1}),
        SALMON(10, "salmon", 1335, true, FeedHeight.TIME, 0, 75, false, 3, 50, "model.creature.fish.salmon", 1.0f, 1.0f, 25.0f, 30.0f, 30.0f, 15.0f, new byte[]{3, 4}, new int[]{4, 1, 4, 2}, new int[]{705, 1372, 1373}, new int[]{1, 10, 1, 1, 5, 5, 5, 5, 5, 1, 5, 1, 1}),
        OCTOPUS(11, "octopus", 572, true, FeedHeight.ANY, 200, 800, true, 3, 50, "model.creature.fish.octopus.black", 1.0f, 1.0f, 30.0f, 40.0f, 45.0f, 14.0f, new byte[]{4}, new int[]{2, 4, 1, 4}, new int[]{1374, 1375}, new int[]{0, 0, 10, 1, 1, 5, 1, 1, 1, 5, 1, 1, 1}),
        MARLIN(12, "marlin", 569, true, FeedHeight.TOP, 250, 1000, true, 6, 50, "model.creature.fish.marlin", 0.343f, 1.0f, 50.0f, 50.0f, 45.0f, 18.0f, new byte[]{4}, new int[]{4, 2, 4, 3}, new int[]{1375}, new int[]{0, 0, 1, 1, 1, 5, 5, 5, 10, 0, 1, 1, 1}),
        BLUESHARK(13, "blue shark", 570, true, FeedHeight.ANY, 250, 1000, true, 5, 50, "model.creature.fish.blueshark", 1.0f, 1.0f, 45.0f, 50.0f, 45.0f, 14.0f, new byte[]{4}, new int[]{4, 3, 4, 2}, new int[]{1375}, new int[]{0, 0, 1, 1, 1, 10, 5, 5, 5, 0, 1, 1, 1}),
        DORADO(14, "dorado", 574, true, FeedHeight.TOP, 150, 600, true, 4, 50, "model.creature.fish.dorado", 1.0f, 1.0f, 30.0f, 50.0f, 45.0f, 13.0f, new byte[]{4}, new int[]{4, 2, 4, 3}, new int[]{1374, 1375}, new int[]{0, 1, 1, 1, 10, 5, 5, 5, 5, 0, 5, 1, 1}),
        SAILFISH(15, "sailfish", 573, true, FeedHeight.TOP, 200, 800, true, 4, 50, "model.creature.fish.sailfish", 1.0f, 1.0f, 40.0f, 50.0f, 45.0f, 15.0f, new byte[]{4}, new int[]{4, 2, 4, 3}, new int[]{1375}, new int[]{0, 0, 0, 1, 1, 5, 5, 5, 5, 0, 10, 0, 0}),
        WHITESHARK(16, "white shark", 571, true, FeedHeight.ANY, 150, 1000, true, 5, 50, "model.creature.fish.whiteshark", 1.0f, 1.0f, 42.0f, 50.0f, 45.0f, 14.0f, new byte[]{4}, new int[]{4, 3, 4, 2}, new int[]{1375}, new int[]{0, 0, 1, 1, 1, 5, 8, 10, 5, 0, 1, 1, 1}),
        TUNA(17, "tuna", 575, true, FeedHeight.TOP, 150, 600, true, 2, 50, "model.creature.fish.tuna", 1.0f, 1.0f, 40.0f, 50.0f, 45.0f, 20.0f, new byte[]{4}, new int[]{4, 2, 4, 3}, new int[]{1374, 1375}, new int[]{0, 1, 1, 5, 1, 5, 10, 5, 5, 0, 1, 0, 1}),
        MINNOW(18, "minnow", 1338, false, FeedHeight.ANY, 0, 20, false, 1, 10, "model.creature.fish.minnow", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, new byte[]{2, 3, 7}, new int[]{4, 2, 4, 1}, new int[]{1344, 1343, 1372}, new int[]{5, 10, 1, 1, 1, 0, 0, 0, 0, 1, 5, 1, 1}),
        LOACH(19, "loach", 1339, false, FeedHeight.ANY, 10, 50, false, 2, 40, "model.creature.fish.loach", 1.0f, 1.0f, 5.0f, 7.0f, 3.0f, 2.0f, new byte[]{2, 3}, new int[]{1, 4, 2, 3}, new int[]{705, 1344, 1372, 1373}, new int[]{5, 5, 1, 1, 5, 1, 0, 0, 1, 5, 1, 1, 10}),
        WURMFISH(20, "wurmfish", 1340, false, FeedHeight.BOTTOM, 40, 1000, false, 3, 50, "model.creature.fish.wurmfish", 1.0f, 1.0f, 21.0f, 33.0f, 11.0f, 4.0f, new byte[]{2, 3}, new int[]{2, 3, 1, 4}, new int[]{705, 1344, 1372, 1373}, new int[]{1, 5, 1, 1, 5, 5, 5, 1, 5, 5, 1, 10, 1}),
        SARDINE(21, "sardine", 1337, true, FeedHeight.NONE, 0, 20, false, 1, 10, "model.creature.fish.sardine", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, new byte[]{2, 1, 3, 7}, new int[]{4, 3, 4, 2}, new int[]{1343}, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}),
        CLAM(22, "clam", 1394, true, FeedHeight.ANY, 0, 0, false, 1, 0, "model.creature.fish.clam", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, new byte[0], new int[]{1, 1, 1, 1}, new int[0], new int[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10});

        private final byte typeId;
        private String name;
        private final int templateId;
        private ItemTemplate template = null;
        private final boolean onSurface;
        private final FeedHeight feedHeight;
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
        private static final FishData[] types;
        private static final Map<Integer, FishData> byTemplateId;

        private FishData(byte typeId, String name, int templateId, boolean onSurface, FeedHeight feedHeight, int minDepth, int maxDepth, boolean specialFish, int damageMod, int minWeight, String modelName, float scale, float baseSpeed, float bodyStrength, float bodyStamina, float bodyControl, float mindSpeed, byte[] depths, int[] feeds, int[] reels, int[] baits) {
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

        public FeedHeight getFeedHeight() {
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
            return 1.0f / this.scale;
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
            block7: for (byte depth : depths) {
                switch (depth) {
                    case 1: {
                        this.inWater = true;
                        continue block7;
                    }
                    case 2: {
                        this.inPond = true;
                        continue block7;
                    }
                    case 3: {
                        this.inLake = true;
                        continue block7;
                    }
                    case 4: {
                        this.inSea = true;
                        continue block7;
                    }
                    case 7: {
                        this.inShallows = true;
                    }
                }
            }
        }

        private void assignReels(int[] reels) {
            block9: for (int reel : reels) {
                switch (reel) {
                    case 1343: {
                        this.useFishingNet = true;
                        continue block9;
                    }
                    case 705: {
                        this.useSpear = true;
                        continue block9;
                    }
                    case 1344: {
                        this.useFishingPole = true;
                        continue block9;
                    }
                    case 1372: {
                        this.useReelBasic = true;
                        continue block9;
                    }
                    case 1373: {
                        this.useReelFine = true;
                        continue block9;
                    }
                    case 1374: {
                        this.useReelWater = true;
                        continue block9;
                    }
                    case 1375: {
                        this.useReelProfessional = true;
                    }
                }
            }
        }

        @Nullable
        public ItemTemplate getTemplate() {
            if (this.templateId == 0) {
                return null;
            }
            if (this.template != null) {
                return this.template;
            }
            try {
                this.template = ItemTemplateFactory.getInstance().getTemplate(this.templateId);
            }
            catch (NoSuchTemplateException noSuchTemplateException) {
                // empty catch block
            }
            return this.template;
        }

        public float getTemplateDifficulty() {
            if (this.getTemplate() != null) {
                return this.template.getDifficulty();
            }
            return 100.0f;
        }

        private float addDifficultyDepth(float posx, float posy, boolean isOnSurface) {
            int tilex = (int)posx >> 2;
            int tiley = (int)posy >> 2;
            byte waterType = WaterType.getWaterType(tilex, tiley, isOnSurface);
            float extraWaterTypeDifficulty = 0.0f;
            switch (waterType) {
                case 1: 
                case 2: {
                    if (this.inPond()) break;
                    extraWaterTypeDifficulty = 10.0f;
                    break;
                }
                case 3: {
                    if (this.inLake()) break;
                    extraWaterTypeDifficulty = 15.0f;
                    break;
                }
                case 4: {
                    if (this.inSea()) break;
                    extraWaterTypeDifficulty = 20.0f;
                    break;
                }
                case 5: 
                case 6: {
                    if (this.inShallows()) break;
                    extraWaterTypeDifficulty = 10.0f;
                }
            }
            int waterDepth = FishEnums.getWaterDepth(posx, posy, isOnSurface);
            int heightDiff = 0;
            if (waterDepth < this.minDepth) {
                heightDiff = Math.min(Math.abs(this.minDepth - waterDepth), 250);
                return extraWaterTypeDifficulty + Math.min((float)heightDiff / 10.0f, 1.0f);
            }
            if (waterDepth > this.maxDepth) {
                heightDiff = Math.min(Math.abs(waterDepth - this.maxDepth), 250);
                return extraWaterTypeDifficulty + Math.min((float)heightDiff / 10.0f, 1.0f);
            }
            return extraWaterTypeDifficulty;
        }

        private float addDifficultyFeeding(Item fishingFloat) {
            if (fishingFloat == null) {
                return 10.0f;
            }
            switch (this.getFeedHeight()) {
                case TOP: {
                    switch (fishingFloat.getTemplateId()) {
                        case 1352: {
                            return -5.0f;
                        }
                        case 1354: {
                            return 8.0f;
                        }
                        case 1353: {
                            return 0.0f;
                        }
                        case 1355: {
                            return 8.0f;
                        }
                    }
                }
                case BOTTOM: {
                    switch (fishingFloat.getTemplateId()) {
                        case 1352: {
                            return 8.0f;
                        }
                        case 1354: {
                            return -5.0f;
                        }
                        case 1353: {
                            return 0.0f;
                        }
                        case 1355: {
                            return 8.0f;
                        }
                    }
                }
                case ANY: {
                    switch (fishingFloat.getTemplateId()) {
                        case 1352: {
                            return 8.0f;
                        }
                        case 1354: {
                            return 8.0f;
                        }
                        case 1353: {
                            return -5.0f;
                        }
                        case 1355: {
                            return 0.0f;
                        }
                    }
                }
                case TIME: {
                    TimeOfDay tod = TimeOfDay.getTimeOfDay();
                    switch (tod) {
                        case MORNING: {
                            switch (fishingFloat.getTemplateId()) {
                                case 1352: {
                                    return -5.0f;
                                }
                                case 1354: {
                                    return 8.0f;
                                }
                                case 1353: {
                                    return 0.0f;
                                }
                                case 1355: {
                                    return -5.0f;
                                }
                            }
                        }
                        case AFTERNOON: {
                            switch (fishingFloat.getTemplateId()) {
                                case 1352: {
                                    return 8.0f;
                                }
                                case 1354: {
                                    return 8.0f;
                                }
                                case 1353: {
                                    return -5.0f;
                                }
                                case 1355: {
                                    return -5.0f;
                                }
                            }
                        }
                        case EVENING: {
                            switch (fishingFloat.getTemplateId()) {
                                case 1352: {
                                    return 8.0f;
                                }
                                case 1354: {
                                    return -5.0f;
                                }
                                case 1353: {
                                    return 0.0f;
                                }
                                case 1355: {
                                    return -5.0f;
                                }
                            }
                        }
                        case NIGHT: {
                            switch (fishingFloat.getTemplateId()) {
                                case 1352: {
                                    return 8.0f;
                                }
                                case 1354: {
                                    return 8.0f;
                                }
                                case 1353: {
                                    return -5.0f;
                                }
                                case 1355: {
                                    return -5.0f;
                                }
                            }
                        }
                    }
                }
            }
            return 15.0f;
        }

        private float addDifficultyTimeOfDay() {
            TimeOfDay tod = TimeOfDay.getTimeOfDay();
            int feed = this.feeds[tod.typeId];
            int diff = (4 - feed) * 5;
            return diff;
        }

        private float addDifficultyRod(Item rod, Item reel, Item line, Item hook) {
            if (rod.getTemplateId() == 1343) {
                if (this.useFishingNet) {
                    return 0.0f;
                }
                return 1000.0f;
            }
            if (rod.getTemplateId() == 705 || rod.getTemplateId() == 707) {
                if (this.useSpear) {
                    return rod.getTemplateId() == 705 ? 2.0f : 0.0f;
                }
                return 1000.0f;
            }
            if (line == null || hook == null) {
                return 1000.0f;
            }
            float diff = 1000.0f;
            if (rod.getTemplateId() == 1344) {
                diff = this.useFishingPole ? -10.0f : (this.useReelBasic ? 5.0f : (this.useReelFine ? 10.0f : 30.0f));
            } else {
                switch (reel.getTemplateId()) {
                    case 1372: {
                        if (this.useReelBasic) {
                            diff = -10.0f;
                            break;
                        }
                        if (this.useFishingPole) {
                            diff = 5.0f;
                            break;
                        }
                        if (this.useReelFine) {
                            diff = 10.0f;
                            break;
                        }
                        diff = 30.0f;
                        break;
                    }
                    case 1373: {
                        if (this.useReelFine) {
                            diff = -10.0f;
                            break;
                        }
                        if (this.useReelBasic) {
                            diff = 5.0f;
                            break;
                        }
                        if (this.useReelWater) {
                            diff = 10.0f;
                            break;
                        }
                        diff = 30.0f;
                        break;
                    }
                    case 1374: {
                        if (this.useReelWater) {
                            diff = -10.0f;
                            break;
                        }
                        if (this.useReelFine) {
                            diff = 5.0f;
                            break;
                        }
                        if (this.useReelProfessional) {
                            diff = 10.0f;
                            break;
                        }
                        if (this.useReelBasic) {
                            diff = 15.0f;
                            break;
                        }
                        diff = 30.0f;
                        break;
                    }
                    case 1375: {
                        diff = this.useReelProfessional ? -10.0f : (this.useReelWater ? 5.0f : (this.useReelFine ? 15.0f : 50.0f));
                    }
                }
            }
            if (diff > 0.0f) {
                switch (hook.getTemplateId()) {
                    case 1357: {
                        break;
                    }
                    case 1358: {
                        diff *= 1.1f;
                        break;
                    }
                    case 1356: {
                        diff *= 1.2f;
                    }
                }
            }
            return diff;
        }

        private float addDifficultyBait(Item bait) {
            byte baitId = BaitType.fromItem(bait).getTypeId();
            return 10.0f - (float)this.baits[baitId];
        }

        public float getDifficulty(float skill, float posX, float posY, boolean onSurface, Item rod, Item reel, Item line, Item fishingFloat, Item hook, Item bait) {
            if (this.getTypeId() == CLAM.getTypeId()) {
                return skill - 10.0f;
            }
            float difficulty = this.getTemplateDifficulty();
            difficulty += this.addDifficultyDepth(posX, posY, onSurface);
            difficulty += this.addDifficultyFeeding(fishingFloat);
            difficulty += this.addDifficultyTimeOfDay();
            difficulty += this.addDifficultyRod(rod, reel, line, hook);
            difficulty += this.addDifficultyBait(bait);
            difficulty = Math.min(Math.max(difficulty, -50.0f), 100.0f);
            return difficulty;
        }

        private float getChanceDefault(float skill) {
            float diff = 0.0f;
            if (this.getTemplate() != null) {
                diff = this.template.getDifficulty();
            }
            if (diff > 0.0f) {
                float flip = 110.0f - diff;
                float smd = skill - diff;
                double rad = Math.toRadians(smd);
                float sin = (float)Math.sin(rad);
                float mult = 1.0f + sin;
                return flip * mult;
            }
            return 50.0f;
        }

        private float multChanceDepth(float posx, float posy, boolean isOnSurface) {
            int tilex = (int)posx >> 2;
            int tiley = (int)posy >> 2;
            byte waterType = WaterType.getWaterType(tilex, tiley, isOnSurface);
            switch (waterType) {
                case 1: 
                case 2: {
                    if (this.inPond()) break;
                    return 0.0f;
                }
                case 3: {
                    if (this.inLake()) break;
                    return 0.0f;
                }
                case 4: {
                    if (this.inSea()) break;
                    return 0.0f;
                }
                case 6: {
                    if (this.inLake() || this.inShallows()) break;
                    return 0.0f;
                }
                case 5: {
                    if (this.inSea() || this.inShallows()) break;
                    return 0.0f;
                }
            }
            int waterDepth = FishEnums.getWaterDepth(posx, posy, isOnSurface);
            int heightDiff = 0;
            if (waterDepth < this.minDepth) {
                heightDiff = Math.min(Math.abs(this.minDepth - waterDepth), 250);
                return 1.0f - Math.min((float)heightDiff / 300.0f, 1.0f);
            }
            if (waterDepth > this.maxDepth) {
                heightDiff = Math.min(Math.abs(waterDepth - this.maxDepth), 250);
                return 1.0f - Math.min((float)heightDiff / 500.0f, 1.0f);
            }
            return 1.0f;
        }

        private float multChanceFeeding(Item fishingFloat) {
            if (fishingFloat == null) {
                return 0.5f;
            }
            switch (this.getFeedHeight()) {
                case TOP: {
                    switch (fishingFloat.getTemplateId()) {
                        case 1352: {
                            return 1.2f;
                        }
                        case 1354: {
                            return 0.8f;
                        }
                        case 1353: {
                            return 1.0f;
                        }
                        case 1355: {
                            return 0.8f;
                        }
                    }
                }
                case BOTTOM: {
                    switch (fishingFloat.getTemplateId()) {
                        case 1352: {
                            return 0.8f;
                        }
                        case 1354: {
                            return 1.2f;
                        }
                        case 1353: {
                            return 1.0f;
                        }
                        case 1355: {
                            return 0.8f;
                        }
                    }
                }
                case ANY: {
                    switch (fishingFloat.getTemplateId()) {
                        case 1352: {
                            return 0.8f;
                        }
                        case 1354: {
                            return 0.8f;
                        }
                        case 1353: {
                            return 1.2f;
                        }
                        case 1355: {
                            return 1.0f;
                        }
                    }
                }
                case TIME: {
                    TimeOfDay tod = TimeOfDay.getTimeOfDay();
                    switch (tod) {
                        case MORNING: {
                            switch (fishingFloat.getTemplateId()) {
                                case 1352: {
                                    return 1.2f;
                                }
                                case 1354: {
                                    return 0.8f;
                                }
                                case 1353: {
                                    return 1.0f;
                                }
                                case 1355: {
                                    return 1.2f;
                                }
                            }
                        }
                        case AFTERNOON: {
                            switch (fishingFloat.getTemplateId()) {
                                case 1352: {
                                    return 0.8f;
                                }
                                case 1354: {
                                    return 0.8f;
                                }
                                case 1353: {
                                    return 1.2f;
                                }
                                case 1355: {
                                    return 1.2f;
                                }
                            }
                        }
                        case EVENING: {
                            switch (fishingFloat.getTemplateId()) {
                                case 1352: {
                                    return 0.8f;
                                }
                                case 1354: {
                                    return 1.2f;
                                }
                                case 1353: {
                                    return 1.0f;
                                }
                                case 1355: {
                                    return 1.2f;
                                }
                            }
                        }
                        case NIGHT: {
                            switch (fishingFloat.getTemplateId()) {
                                case 1352: {
                                    return 0.8f;
                                }
                                case 1354: {
                                    return 0.8f;
                                }
                                case 1353: {
                                    return 1.2f;
                                }
                                case 1355: {
                                    return 1.2f;
                                }
                            }
                        }
                    }
                }
            }
            return 0.5f;
        }

        private float multChanceTimeOfDay() {
            TimeOfDay tod = TimeOfDay.getTimeOfDay();
            int feed = this.feeds[tod.typeId];
            int chance = 1 + (feed - 4) / 8;
            return chance;
        }

        private float multChanceRod(Item rod, Item reel, Item line, Item hook, Item bait) {
            if (rod.getTemplateId() == 1343) {
                if (this.useFishingNet) {
                    return 1.0f;
                }
                return 0.0f;
            }
            if (rod.getTemplateId() == 705 || rod.getTemplateId() == 707) {
                if (this.useSpear) {
                    return rod.getTemplateId() == 705 ? 0.8f : 1.0f;
                }
                return 0.0f;
            }
            if (line == null || hook == null) {
                return 0.0f;
            }
            float chance = 0.0f;
            if (rod.getTemplateId() == 1344) {
                chance = this.useFishingPole ? 1.0f : (this.useReelBasic ? 0.75f : (this.useReelFine ? 0.5f : 0.1f));
            } else {
                switch (reel.getTemplateId()) {
                    case 1372: {
                        if (this.useReelBasic) {
                            chance = 1.0f;
                            break;
                        }
                        if (this.useFishingPole) {
                            chance = 0.75f;
                            break;
                        }
                        if (this.useReelFine) {
                            chance = 0.75f;
                            break;
                        }
                        if (this.useReelWater) {
                            chance = 0.25f;
                            break;
                        }
                        chance = 0.1f;
                        break;
                    }
                    case 1373: {
                        if (this.useReelFine) {
                            chance = 1.0f;
                            break;
                        }
                        if (this.useReelBasic) {
                            chance = 0.75f;
                            break;
                        }
                        if (this.useFishingPole) {
                            chance = 0.25f;
                            break;
                        }
                        if (this.useReelWater) {
                            chance = 0.5f;
                            break;
                        }
                        chance = 0.1f;
                        break;
                    }
                    case 1374: {
                        if (this.useReelWater) {
                            chance = 1.0f;
                            break;
                        }
                        if (this.useReelFine) {
                            chance = 0.75f;
                            break;
                        }
                        if (this.useReelProfessional) {
                            chance = 0.5f;
                            break;
                        }
                        chance = 0.1f;
                        break;
                    }
                    case 1375: {
                        chance = this.useReelProfessional ? 1.0f : (this.useReelWater ? 0.85f : (this.useReelFine ? 0.45f : 0.1f));
                    }
                }
            }
            if (chance > 0.0f) {
                switch (hook.getTemplateId()) {
                    case 1357: {
                        break;
                    }
                    case 1358: {
                        chance *= 0.9f;
                        break;
                    }
                    case 1356: {
                        chance *= 0.8f;
                    }
                }
            }
            return chance * this.multChanceBait(bait);
        }

        private float multChanceBait(Item bait) {
            byte baitId = BaitType.fromItem(bait).getTypeId();
            if (this.typeId == -1) {
                System.out.println(this.name + "(bait):" + baitId + " " + this.baits[baitId]);
            }
            switch (this.baits[baitId]) {
                case 0: {
                    return 0.0f;
                }
                case 1: {
                    return 0.8f;
                }
                case 2: {
                    return 0.82f;
                }
                case 3: {
                    return 0.84f;
                }
                case 4: {
                    return 0.86f;
                }
                case 5: {
                    return 0.88f;
                }
                case 6: {
                    return 0.9f;
                }
                case 7: {
                    return 0.925f;
                }
                case 8: {
                    return 0.95f;
                }
                case 9: {
                    return 0.975f;
                }
                case 10: {
                    return 1.0f;
                }
            }
            return 0.0f;
        }

        public Point getSpecialSpot(int zoneX, int zoneY, int season) {
            Random r = new Random(this.getTypeId() * 5 + Servers.localServer.id * 100 + season);
            int rx = zoneX * 128 + 5 + r.nextInt(118);
            int ry = zoneY * 128 + 5 + r.nextInt(118);
            return new Point(rx, ry, this.getTemplateId());
        }

        public float getChance(float skill, Item rod, Item reel, Item line, Item fishingFloat, Item hook, Item bait, float posX, float posY, boolean onSurface) {
            if (this.onSurface() != onSurface) {
                return 0.0f;
            }
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
            if (this.isSpecialFish && chance > 0.0f) {
                int tilex = (int)posX >> 2;
                int tiley = (int)posY >> 2;
                int season = WurmCalendar.getSeasonNumber();
                int zoneX = tilex / 128;
                int zoneY = tiley / 128;
                Point specialSpot = this.getSpecialSpot(zoneX, zoneY, season);
                int farAwayX = Math.abs(specialSpot.getX() - tilex);
                int farAwayY = Math.abs(specialSpot.getY() - tiley);
                int farAway = Math.max(farAwayX, farAwayY);
                float rt2 = 15.0f;
                float nc = 0.0f;
                float ht = 0.0f;
                try {
                    ht = Zones.calculateHeight(posX, posY, onSurface) * 10.0f;
                }
                catch (NoSuchZoneException noSuchZoneException) {
                    // empty catch block
                }
                if ((float)farAway <= 15.0f && ht < -100.0f) {
                    float dpt = 6.0f;
                    double rad = Math.toRadians((float)farAway * 6.0f);
                    nc = (float)Math.cos(rad);
                }
                chance *= nc;
                if (this.typeId == -1) {
                    System.out.println(this.name + "(special):" + farAway + " " + chance + " " + nc);
                }
            }
            return chance;
        }

        public static final int getLength() {
            return types.length;
        }

        public static FishData fromInt(int id) {
            if (id >= FishData.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        @Nullable
        public static FishData fromName(String name) {
            for (FishData fd : types) {
                if (!fd.getName().equalsIgnoreCase(name)) continue;
                return fd;
            }
            return null;
        }

        public static FishData fromItem(@Nullable Item fish) {
            if (fish == null) {
                return NONE;
            }
            FishData fishData = byTemplateId.get(fish.getTemplateId());
            if (fishData == null) {
                return NONE;
            }
            return fishData;
        }

        static {
            byTemplateId = new HashMap<Integer, FishData>();
            for (FishData fd : types = FishData.values()) {
                byTemplateId.put(fd.getTemplateId(), fd);
            }
        }
    }

    public static enum BaitType {
        NONE(0, -1, 1.0f),
        FLY(1, 1359, 0.1f),
        CHEESE(2, 1360, 1.5f),
        DOUGH(3, 1361, 2.0f),
        WURM(4, 1362, 1.0f),
        SARDINE(5, 1337, 2.2f),
        ROACH(6, 162, 2.8f),
        PERCH(7, 163, 3.0f),
        MINNOW(8, 1338, 2.5f),
        FISH_BAIT(9, 1363, 0.2f),
        GRUB(10, 1364, 0.5f),
        WHEAT(11, 1365, 0.2f),
        CORN(12, 1366, 0.1f);

        private final byte typeId;
        private final int templateId;
        private final float crumbles;
        private static final BaitType[] types;
        private static final Map<Integer, BaitType> byTemplateId;

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

        public static BaitType fromInt(int id) {
            if (id >= BaitType.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        public static BaitType fromItem(@Nullable Item bait) {
            if (bait == null) {
                return NONE;
            }
            BaitType baitType = byTemplateId.get(bait.getTemplateId());
            if (baitType == null) {
                return NONE;
            }
            return baitType;
        }

        static {
            byTemplateId = new HashMap<Integer, BaitType>();
            for (BaitType bt : types = BaitType.values()) {
                byTemplateId.put(bt.getTemplateId(), bt);
            }
        }
    }

    public static enum HookType {
        NONE(0, 0),
        WOOD(1, 1356),
        METAL(2, 1357),
        BONE(3, 1358);

        private final byte typeId;
        private final int templateId;
        private static final HookType[] types;
        private static final Map<Integer, HookType> byTemplateId;

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

        public static HookType fromInt(int id) {
            if (id >= HookType.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        public static HookType fromItem(@Nullable Item hook) {
            if (hook == null) {
                return NONE;
            }
            for (HookType ft : types) {
                if (ft.getTemplateId() != hook.getTemplateId()) continue;
                return ft;
            }
            return NONE;
        }

        static {
            byTemplateId = new HashMap<Integer, HookType>();
            for (HookType ht : types = HookType.values()) {
                byTemplateId.put(ht.getTemplateId(), ht);
            }
        }
    }

    public static enum FloatType {
        NONE(0, 0),
        FEATHER(1, 1352),
        TWIG(2, 1353),
        MOSS(3, 1354),
        BARK(4, 1355);

        private final byte typeId;
        private final int templateId;
        private static final FloatType[] types;
        private static final Map<Integer, FloatType> byTemplateId;

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

        public static FloatType fromInt(int id) {
            if (id >= FloatType.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        public static FloatType fromItem(@Nullable Item afloat) {
            if (afloat == null) {
                return NONE;
            }
            FloatType floatType = byTemplateId.get(afloat.getTemplateId());
            if (floatType == null) {
                return NONE;
            }
            return floatType;
        }

        static {
            byTemplateId = new HashMap<Integer, FloatType>();
            for (FloatType ft : types = FloatType.values()) {
                byTemplateId.put(ft.getTemplateId(), ft);
            }
        }
    }

    public static enum ReelType {
        NONE(0, 0, 1347),
        LIGHT(1, 1372, 1348),
        MEDIUM(2, 1373, 1349),
        DEEP_WATER(3, 1374, 1350),
        PROFESSIONAL(4, 1375, 1351);

        private final byte typeId;
        private final int templateId;
        private final int associatedLine;
        private static final ReelType[] types;
        private static final Map<Integer, ReelType> byTemplateId;

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

        public static ReelType fromInt(int id) {
            if (id >= ReelType.getLength()) {
                return types[0];
            }
            return types[id & 0xFF];
        }

        public static ReelType fromItem(@Nullable Item reel) {
            if (reel == null) {
                return NONE;
            }
            ReelType reelType = byTemplateId.get(reel.getTemplateId());
            if (reelType == null) {
                return NONE;
            }
            return reelType;
        }

        static {
            byTemplateId = new HashMap<Integer, ReelType>();
            for (ReelType rt : types = ReelType.values()) {
                byTemplateId.put(rt.getTemplateId(), rt);
            }
        }
    }

    public static enum TimeOfDay {
        MORNING(0),
        AFTERNOON(1),
        EVENING(2),
        NIGHT(3);

        private final byte typeId;

        private TimeOfDay(byte id) {
            this.typeId = id;
        }

        public int getTypeId() {
            return this.typeId;
        }

        public static final TimeOfDay getTimeOfDay() {
            if (WurmCalendar.getHour() < 4 || WurmCalendar.getHour() == 4 && WurmCalendar.getMinute() <= 30) {
                return NIGHT;
            }
            if (WurmCalendar.getHour() < 10 || WurmCalendar.getHour() == 10 && WurmCalendar.getMinute() <= 30) {
                return MORNING;
            }
            if (WurmCalendar.getHour() < 16 || WurmCalendar.getHour() == 16 && WurmCalendar.getMinute() <= 30) {
                return AFTERNOON;
            }
            if (WurmCalendar.getHour() < 22 || WurmCalendar.getHour() == 22 && WurmCalendar.getMinute() <= 30) {
                return EVENING;
            }
            return NIGHT;
        }
    }

    public static enum FeedHeight {
        NONE(0),
        TOP(1),
        BOTTOM(2),
        ANY(3),
        TIME(4);

        private final byte typeId;

        private FeedHeight(byte id) {
            this.typeId = id;
        }

        public int getTypeId() {
            return this.typeId;
        }
    }
}

