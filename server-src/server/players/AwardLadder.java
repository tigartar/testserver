/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.Titles;
import com.wurmonline.shared.constants.ItemMaterials;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AwardLadder
implements MiscConstants,
ItemMaterials {
    private final String name;
    private final String imageUrl;
    private final int monthsRequiredSinceReset;
    private final int totalMonthsRequired;
    private final int silversRequired;
    private final int titleAwarded;
    private final int itemAwarded;
    private final int achievementAwarded;
    private final float qlOfItemAward;
    private static final Logger logger = Logger.getLogger(AwardLadder.class.getName());
    private static final Map<Integer, AwardLadder> resetLadder = new ConcurrentHashMap<Integer, AwardLadder>();
    private static final Map<Integer, AwardLadder> totalLadder = new ConcurrentHashMap<Integer, AwardLadder>();
    private static final Map<Integer, AwardLadder> silverLadder = new ConcurrentHashMap<Integer, AwardLadder>();
    private static final Map<Player, Set<Item>> itemsToAward = new ConcurrentHashMap<Player, Set<Item>>();

    public AwardLadder(String _name, String _imageUrl, int _totalMonthsRequired, int _monthsRequiredSinceReset, int _silversRequired, int _titleAwarded, int _itemAwarded, int _achievementAwarded, float qlAwarded) {
        this.name = _name;
        this.imageUrl = _imageUrl;
        this.monthsRequiredSinceReset = _monthsRequiredSinceReset;
        this.totalMonthsRequired = _totalMonthsRequired;
        this.silversRequired = _silversRequired;
        this.titleAwarded = _titleAwarded;
        this.itemAwarded = _itemAwarded;
        this.achievementAwarded = _achievementAwarded;
        this.qlOfItemAward = qlAwarded;
        if (this.monthsRequiredSinceReset > 0) {
            resetLadder.put(this.monthsRequiredSinceReset, this);
        }
        if (this.totalMonthsRequired > 0) {
            totalLadder.put(this.totalMonthsRequired, this);
        }
        if (this.silversRequired > 0) {
            silverLadder.put(this.silversRequired, this);
        }
    }

    public static final AwardLadder getLadderStepForReset(int months) {
        return resetLadder.get(months);
    }

    public static final AwardLadder[] getLadderStepsForTotal(int months) {
        HashSet<AwardLadder> steps = new HashSet<AwardLadder>();
        for (AwardLadder step : totalLadder.values()) {
            if (step.getTotalMonthsRequired() > months) continue;
            steps.add(step);
        }
        return steps.toArray(new AwardLadder[steps.size()]);
    }

    public static final AwardLadder getLadderStepForSilver(int silver) {
        return silverLadder.get(silver);
    }

    public String getName() {
        return this.name;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public int getMonthsRequiredReset() {
        return this.monthsRequiredSinceReset;
    }

    public int getTotalMonthsRequired() {
        return this.totalMonthsRequired;
    }

    public int getSilversRequired() {
        return this.silversRequired;
    }

    public int getTitleNumberAwarded() {
        return this.titleAwarded;
    }

    public int getItemNumberAwarded() {
        return this.itemAwarded;
    }

    public int getAchievementNumberAwarded() {
        return this.achievementAwarded;
    }

    public Titles.Title getTitleAwarded() {
        return Titles.Title.getTitle(this.titleAwarded);
    }

    public AchievementTemplate getAchievementAwarded() {
        return Achievement.getTemplate(this.achievementAwarded);
    }

    public ItemTemplate getItemAwarded() {
        try {
            return ItemTemplateFactory.getInstance().getTemplate(this.itemAwarded);
        }
        catch (NoSuchTemplateException nst) {
            return null;
        }
    }

    public static final AwardLadder getNextTotalAward(int totalMonthsSinceReset) {
        AwardLadder next = null;
        for (Map.Entry<Integer, AwardLadder> entry : resetLadder.entrySet()) {
            if (next == null) {
                if (entry.getKey() <= totalMonthsSinceReset) continue;
                next = entry.getValue();
                continue;
            }
            if (entry.getKey() <= totalMonthsSinceReset || entry.getKey() >= next.getMonthsRequiredReset()) continue;
            next = entry.getValue();
        }
        return next;
    }

    public static final void clearItemAwards() {
        for (Map.Entry<Player, Set<Item>> entry : itemsToAward.entrySet()) {
            for (Item i : entry.getValue()) {
                entry.getKey().getInventory().insertItem(i, true);
                if (entry.getKey().getCommunicator() == null) continue;
                entry.getKey().getCommunicator().sendSafeServerMessage("You receive " + i.getNameWithGenus() + " as premium bonus!");
            }
        }
        itemsToAward.clear();
    }

    public static final void awardTotalLegacy(PlayerInfo p) {
        if (p.awards != null) {
            try {
                AwardLadder[] total = AwardLadder.getLadderStepsForTotal(p.awards.getMonthsPaidEver());
                HashSet<Item> itemSet = new HashSet<Item>();
                for (AwardLadder step : total) {
                    if (step.getItemNumberAwarded() <= 0) continue;
                    if (step.getItemNumberAwarded() == 229) {
                        int numawarded = 274 + Server.rand.nextInt(14);
                        try {
                            Player player = Players.getInstance().getPlayer(p.wurmId);
                            Item i = ItemFactory.createItem(numawarded, step.getQlOfItemAward(), (byte)67, (byte)0, "");
                            itemSet.add(i);
                            itemsToAward.put(player, itemSet);
                        }
                        catch (NoSuchPlayerException nsp) {
                            long inventoryId = DbCreatureStatus.getInventoryIdFor(p.wurmId);
                            Item i = ItemFactory.createItem(numawarded, step.getQlOfItemAward(), (byte)67, (byte)0, "");
                            i.setParentId(inventoryId, true);
                            i.setOwnerId(p.wurmId);
                        }
                        continue;
                    }
                    try {
                        Player player = Players.getInstance().getPlayer(p.wurmId);
                        Item i = ItemFactory.createItem(step.getItemNumberAwarded(), step.getQlOfItemAward(), "");
                        itemSet.add(i);
                        itemsToAward.put(player, itemSet);
                    }
                    catch (NoSuchPlayerException nsp) {
                        long inventoryId = DbCreatureStatus.getInventoryIdFor(p.wurmId);
                        Item i = ItemFactory.createItem(step.getItemNumberAwarded(), step.getQlOfItemAward(), "");
                        i.setParentId(inventoryId, true);
                        i.setOwnerId(p.wurmId);
                    }
                }
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage() + " " + p.getName() + " " + p.awards.getMonthsPaidSinceReset() + ": " + p.awards.getMonthsPaidInARow(), ex);
            }
        }
    }

    public static final float consecutiveItemQL(int consecutiveMonths) {
        return Math.min(100.0f, (float)(consecutiveMonths * 16));
    }

    public static final void award(PlayerInfo p, boolean tickedMonth) {
        block16: {
            if (p.awards != null) {
                try {
                    if (tickedMonth) {
                        Player player3;
                        Item i;
                        float ql = AwardLadder.consecutiveItemQL(p.awards.getMonthsPaidInARow());
                        try {
                            Player player2 = Players.getInstance().getPlayer(p.wurmId);
                            Item inventory = player2.getInventory();
                            i = ItemFactory.createItem(834, ql, "");
                            inventory.insertItem(i, true);
                            player2.getCommunicator().sendSafeServerMessage("You receive " + i.getNameWithGenus() + " at ql " + ql + " for staying premium!");
                        }
                        catch (NoSuchPlayerException nsp) {
                            long inventoryId = DbCreatureStatus.getInventoryIdFor(p.wurmId);
                            Item i2 = ItemFactory.createItem(834, ql, "");
                            i2.setParentId(inventoryId, true);
                            i2.setOwnerId(p.wurmId);
                        }
                        AwardLadder sinceReset = AwardLadder.getLadderStepForReset(p.awards.getMonthsPaidSinceReset());
                        if (sinceReset == null) break block16;
                        if (sinceReset.getTitleAwarded() != null) {
                            p.addTitle(sinceReset.getTitleAwarded());
                            try {
                                player3 = Players.getInstance().getPlayer(p.wurmId);
                                player3.getCommunicator().sendSafeServerMessage("You receive the title " + sinceReset.getTitleAwarded().getName(player3.getSex() == 0) + " for staying premium!");
                            }
                            catch (NoSuchPlayerException player3) {
                                // empty catch block
                            }
                        }
                        if (sinceReset.getAchievementNumberAwarded() > 0) {
                            Achievements.triggerAchievement(p.wurmId, sinceReset.getAchievementNumberAwarded());
                        }
                        if (sinceReset.getItemNumberAwarded() > 0) {
                            try {
                                player3 = Players.getInstance().getPlayer(p.wurmId);
                                i = ItemFactory.createItem(sinceReset.getItemNumberAwarded(), sinceReset.getQlOfItemAward(), "");
                                player3.getInventory().insertItem(i, true);
                            }
                            catch (NoSuchPlayerException nsp) {
                                long inventoryId = DbCreatureStatus.getInventoryIdFor(p.wurmId);
                                Item i3 = ItemFactory.createItem(sinceReset.getItemNumberAwarded(), sinceReset.getQlOfItemAward(), "");
                                i3.setParentId(inventoryId, true);
                                i3.setOwnerId(p.wurmId);
                            }
                        }
                        break block16;
                    }
                    AwardLadder silvers = AwardLadder.getLadderStepForSilver(p.awards.getSilversPaidEver());
                    if (silvers != null) {
                        if (silvers.getTitleAwarded() != null) {
                            p.addTitle(silvers.getTitleAwarded());
                        }
                        if (silvers.getAchievementNumberAwarded() > 0) {
                            Achievements.triggerAchievement(p.wurmId, silvers.getAchievementNumberAwarded());
                        }
                    }
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage() + " " + p.getName() + " " + p.awards.getMonthsPaidSinceReset() + ": " + p.awards.getMonthsPaidInARow(), ex);
                }
            }
        }
    }

    private static final void generateLadder() {
        new AwardLadder("Title: Soldier of Lomaner", "", 0, 1, 0, 254, 0, 0, 0.0f);
        new AwardLadder("Achievement: Landed", "", 0, 1, 0, 0, 0, 343, 0.0f);
        new AwardLadder("Achievement: Survived", "", 0, 3, 0, 0, 0, 344, 0.0f);
        new AwardLadder("Title: Rider of Lomaner", "", 0, 4, 0, 255, 0, 0, 0.0f);
        new AwardLadder("Achievement: Scouted", "", 0, 6, 0, 0, 0, 345, 0.0f);
        new AwardLadder("Title: Chieftain of Lomaner", "", 0, 7, 0, 256, 0, 0, 0.0f);
        new AwardLadder("Achievement: Experienced", "", 0, 9, 0, 0, 0, 346, 0.0f);
        new AwardLadder("Title: Ambassador of Lomaner", "", 0, 10, 0, 257, 0, 0, 0.0f);
        new AwardLadder("Item: Spyglass", "", 0, 12, 0, 0, 489, 0, 70.0f);
        new AwardLadder("Achievement: Owning", "", 0, 13, 0, 0, 0, 347, 0.0f);
        new AwardLadder("Title: Baron of Lomaner", "", 0, 14, 0, 258, 0, 0, 0.0f);
        new AwardLadder("Achievement: Shined", "", 0, 16, 0, 0, 0, 348, 0.0f);
        new AwardLadder("Title: Jarl of Lomaner", "", 0, 18, 0, 259, 0, 0, 0.0f);
        new AwardLadder("Achievement: Glittered", "", 0, 20, 0, 0, 0, 349, 0.0f);
        new AwardLadder("Title: Duke of Lomaner", "", 0, 23, 0, 260, 0, 0, 0.0f);
        new AwardLadder("Achievement: Highly Illuminated", "", 0, 26, 0, 0, 0, 350, 0.0f);
        new AwardLadder("Title: Provost of Lomaner", "", 0, 30, 0, 261, 0, 0, 0.0f);
        new AwardLadder("Achievement: Foundation Pillar", "", 0, 36, 0, 0, 0, 351, 0.0f);
        new AwardLadder("Title: Marquis of Lomaner", "", 0, 40, 0, 262, 0, 0, 0.0f);
        new AwardLadder("Achievement: Revered One", "", 0, 48, 0, 0, 0, 352, 0.0f);
        new AwardLadder("Title: Grand Duke of Lomaner", "", 0, 54, 0, 263, 0, 0, 0.0f);
        new AwardLadder("Achievement: Patron Of The Net", "", 0, 60, 0, 0, 0, 353, 0.0f);
        new AwardLadder("Title: Viceroy of Lomaner", "", 0, 70, 0, 264, 0, 0, 0.0f);
        new AwardLadder("Achievement: Myth Or Legend?", "", 0, 80, 0, 0, 0, 354, 0.0f);
        new AwardLadder("Title: Prince of Lomaner", "", 0, 100, 0, 265, 0, 0, 0.0f);
        new AwardLadder("Achievement: Atlas Reincarnated", "", 0, 120, 0, 0, 0, 355, 0.0f);
        new AwardLadder("1 month", "", 1, 0, 0, 0, 834, 0, 50.0f);
        new AwardLadder("3 months", "", 3, 0, 0, 0, 700, 0, 80.0f);
        new AwardLadder("6 months", "", 6, 0, 0, 0, 466, 0, 99.0f);
        new AwardLadder("9 months", "", 9, 0, 0, 0, 837, 0, 80.0f);
        new AwardLadder("12 months", "", 12, 0, 0, 0, 229, 0, 30.0f);
        new AwardLadder("15 months", "", 15, 0, 0, 0, 837, 0, 90.0f);
        new AwardLadder("24 months", "", 24, 0, 0, 0, 229, 0, 90.0f);
        new AwardLadder("30 months", "", 30, 0, 0, 0, 837, 0, 90.0f);
        new AwardLadder("36 months", "", 36, 0, 0, 0, 668, 0, 40.0f);
        new AwardLadder("42 months", "", 42, 0, 0, 0, 837, 0, 93.0f);
        new AwardLadder("48 months", "", 48, 0, 0, 0, 837, 0, 94.0f);
        new AwardLadder("54 months", "", 54, 0, 0, 0, 837, 0, 95.0f);
        new AwardLadder("60 months", "", 60, 0, 0, 0, 837, 0, 96.0f);
        logger.info("Finished generating AwardLadder");
    }

    public float getQlOfItemAward() {
        return this.qlOfItemAward;
    }

    static {
        AwardLadder.generateLadder();
    }
}

