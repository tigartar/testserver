/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementList;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.JournalReward;
import com.wurmonline.server.players.JournalTier;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class PlayerJournal
implements AchievementList {
    protected static final Logger logger = Logger.getLogger(PlayerJournal.class.getName());
    public static final byte JOUR_TIER_TUT = 0;
    public static final byte JOUR_TIER_BASIC1 = 1;
    public static final byte JOUR_TIER_BASIC2 = 2;
    public static final byte JOUR_TIER_BASIC3 = 3;
    public static final byte JOUR_TIER_INTERMEDIATE1 = 4;
    public static final byte JOUR_TIER_INTERMEDIATE2 = 5;
    public static final byte JOUR_TIER_INTERMEDIATE3 = 6;
    public static final byte JOUR_TIER_EXPERT1 = 7;
    public static final byte JOUR_TIER_EXPERT2 = 8;
    public static final byte JOUR_TIER_EXPERT3 = 9;
    public static final byte JOUR_TIER_PRIEST1 = 10;
    public static final byte JOUR_TIER_PRIEST2 = 11;
    public static final byte JOUR_TIER_PRIEST3 = 12;
    private static final HashMap<Byte, JournalTier> allTiers = new HashMap();

    public static HashMap<Byte, JournalTier> getAllTiers() {
        return allTiers;
    }

    public static void achievementTriggered(long playerId, int achievementId) {
        Optional<JournalTier> theTier = allTiers.values().stream().filter(t -> t.containsAchievement(achievementId)).findFirst();
        if (theTier.isPresent() && theTier.get().isVisible(playerId)) {
            Optional<Player> p = Players.getInstance().getPlayerOptional(playerId);
            p.ifPresent(ply -> {
                ply.getCommunicator().sendPersonalJournalAchvUpdate(((JournalTier)theTier.get()).getTierId(), achievementId, true);
                if (((JournalTier)theTier.get()).shouldUnlockNextTier(playerId)) {
                    JournalTier nextTier = ((JournalTier)theTier.get()).getNextTier();
                    PlayerJournal.sendTierUnlock(ply, nextTier);
                }
            });
            if (theTier.get().shouldUnlockReward(playerId)) {
                theTier.get().awardReward(playerId);
                p.ifPresent(ply -> ply.getCommunicator().sendPersonalJournalTierUpdate(((JournalTier)theTier.get()).getTierId(), true, ((JournalTier)theTier.get()).getRewardString()));
            }
        }
    }

    public static void subAchievementCounterTick(long playerId, int subAchievementId) {
        Optional<JournalTier> subAchTier = allTiers.values().stream().filter(t -> t.containsAchievement(subAchievementId)).findFirst();
        if (subAchTier.isPresent() && subAchTier.get().isVisible(playerId)) {
            Optional<Player> p = Players.getInstance().getPlayerOptional(playerId);
            p.ifPresent(ply -> ply.getCommunicator().sendPersonalJournalAchvUpdate(((JournalTier)subAchTier.get()).getTierId(), subAchievementId, Achievements.getAchievementObject(playerId).getAchievement(subAchievementId) != null));
        }
    }

    public static void sendTierUnlock(@Nullable Player p, @Nullable JournalTier toUnlock) {
        if (p == null || toUnlock == null) {
            return;
        }
        Achievements ach = Achievements.getAchievementObject(p.getWurmId());
        int[] achievementIds = toUnlock.getAchievementList().stream().mapToInt(i -> i).toArray();
        boolean[] achievementsCompleted = new boolean[achievementIds.length];
        for (int i2 = 0; i2 < achievementIds.length; ++i2) {
            achievementsCompleted[i2] = ach != null ? ach.getAchievement(achievementIds[i2]) != null : false;
        }
        p.getCommunicator().sendSafeServerMessage("Congratulations, you have now unlocked " + toUnlock.getTierName() + " in your journal.", (byte)2);
        p.getCommunicator().sendPersonalJournalTier(toUnlock.getTierId(), toUnlock.getTierName(), toUnlock.getRewardString(), toUnlock.isRewardUnlocked(p.getWurmId()) || toUnlock.hasBeenAwarded(p.getWurmId()), achievementIds, achievementsCompleted);
        if (toUnlock.isNextTierUnlocked(p.getWurmId())) {
            if (toUnlock.isRewardUnlocked(p.getWurmId()) && !toUnlock.hasBeenAwarded(p.getWurmId())) {
                toUnlock.awardReward(p.getWurmId());
            }
            PlayerJournal.sendTierUnlock(p, toUnlock.getNextTier());
        }
    }

    public static void sendPersonalJournal(@Nullable Player plr) {
        if (plr == null) {
            return;
        }
        for (JournalTier tier : allTiers.values()) {
            if (!tier.isVisible(plr.getWurmId())) continue;
            if (tier.isRewardUnlocked(plr.getWurmId()) && !tier.hasBeenAwarded(plr.getWurmId())) {
                tier.awardReward(plr.getWurmId());
            }
            Achievements ach = Achievements.getAchievementObject(plr.getWurmId());
            int[] achievementIds = tier.getAchievementList().stream().mapToInt(i -> i).toArray();
            boolean[] achievementsCompleted = new boolean[achievementIds.length];
            for (int i2 = 0; i2 < achievementIds.length; ++i2) {
                achievementsCompleted[i2] = ach != null ? ach.getAchievement(achievementIds[i2]) != null : false;
            }
            plr.getCommunicator().sendPersonalJournalTier(tier.getTierId(), tier.getTierName(), tier.getRewardString(), tier.isRewardUnlocked(plr.getWurmId()) || tier.hasBeenAwarded(plr.getWurmId()), achievementIds, achievementsCompleted);
        }
    }

    public static void sendJournalInfoBML(@Nullable Player p, long targetId) {
        if (p == null) {
            return;
        }
        if (targetId == -10L) {
            return;
        }
        BMLBuilder toSend = BMLBuilder.createVertArrayNode(false);
        Achievements ach = Achievements.getAchievementObject(targetId);
        for (JournalTier tier : allTiers.values()) {
            int i2;
            int[] achievementIds = tier.getAchievementList().stream().mapToInt(i -> i).toArray();
            boolean[] achievementsCompleted = new boolean[achievementIds.length];
            for (i2 = 0; i2 < achievementIds.length; ++i2) {
                achievementsCompleted[i2] = ach != null ? ach.getAchievement(achievementIds[i2]) != null : false;
            }
            toSend.addLabel(tier.getTierName(), tier.getRewardString(), BMLBuilder.TextType.BOLD, tier.isRewardUnlocked(targetId) ? (tier.hasBeenAwarded(targetId) ? Color.GREEN : Color.YELLOW) : (tier.isVisible(targetId) ? Color.LIGHT_GRAY : Color.GRAY));
            for (i2 = 0; i2 < achievementIds.length; ++i2) {
                AchievementTemplate t = Achievement.getTemplate(achievementIds[i2]);
                if (t == null) {
                    logger.warning("AchievementTemplate for ID# " + achievementIds[i2] + " is null");
                    continue;
                }
                toSend.addLabel("  - " + t.getName(), achievementsCompleted[i2] ? t.getDescription() : t.getRequirement(), null, achievementsCompleted[i2] ? Color.GREEN : (tier.isVisible(targetId) ? Color.LIGHT_GRAY : Color.GRAY));
            }
        }
        toSend.addText("\r\n\r\nKey:", null, BMLBuilder.TextType.BOLD, Color.WHITE);
        toSend.addText("Not visible to player", null, null, Color.GRAY);
        toSend.addText("Visible to player, incomplete", null, null, Color.LIGHT_GRAY);
        toSend.addText("Completed", null, null, Color.GREEN);
        toSend.addText("Completed but unrewarded (should reward on login)", null, null, Color.YELLOW, 300, 40);
        p.getCommunicator().sendBml(300, 500, true, true, BMLBuilder.createBMLBorderPanel(null, null, BMLBuilder.createScrollPanelNode(true, false).addString(toSend.toString()), null, null).toString(), 200, 200, 200, "Journal Info: " + PlayerInfoFactory.getPlayerName(targetId));
    }

    static {
        JournalTier tutorialTier = new JournalTier(0, "First Steps", -1, 1, 6, 64, 513, 514, 515, 516, 517, 518, 519, 520, 521, 522, 523, Features.Feature.HIGHWAYS.isEnabled() ? 524 : 576);
        tutorialTier.setReward(new JournalReward("Apprentice Title & 1hr Sleep Bonus"){

            @Override
            public void runReward(Player p) {
                p.addTitle(Titles.Title.Journal_T0);
                p.getSaveFile().addToSleep(3600);
            }
        });
        JournalTier basicTier1 = new JournalTier(1, "Finding the Path", 0, 2, 5, 65, 525, 526, 529, 530, 531, 532, 533, 534, 535, 536, 537);
        basicTier1.setReward(new JournalReward("Learned Title & 1hr Sleep Bonus"){

            @Override
            public void runReward(Player p) {
                p.addTitle(Titles.Title.Journal_T1);
                p.getSaveFile().addToSleep(3600);
            }
        });
        JournalTier basicTier2 = new JournalTier(2, "Gathering Stride", 1, 3, 7, 66, 538, 539, 540, 541, 542, 231, 543, 544, 545, 546, 301, 547, 548, 549);
        basicTier2.setReward(new JournalReward("Experienced Title & 2hr Sleep Bonus"){

            @Override
            public void runReward(Player p) {
                p.addTitle(Titles.Title.Journal_T2);
                p.getSaveFile().addToSleep(7200);
            }
        });
        JournalTier basicTier3 = new JournalTier(3, "Paved with Stone", 2, 4, 7, 67, 550, 551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561, 562, 563);
        basicTier3.setReward(new JournalReward("Skilled Title & 2hr Sleep Bonus"){

            @Override
            public void runReward(Player p) {
                p.addTitle(Titles.Title.Journal_T3);
                p.getSaveFile().addToSleep(7200);
            }
        });
        JournalTier intermediateTier1 = new JournalTier(4, "On the Highway", 3, 5, 6, 68, 564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575);
        intermediateTier1.setReward(new JournalReward("Accomplished Title, Glimmersteel or Adamantine Lump & 3hr Sleep Bonus"){

            @Override
            public void runReward(Player p) {
                p.addTitle(Titles.Title.Journal_T4);
                p.getSaveFile().addToSleep(10800);
                try {
                    int templateId = 698;
                    if (Server.rand.nextBoolean()) {
                        templateId = 694;
                    }
                    Item lump = ItemFactory.createItem(templateId, Server.rand.nextFloat() * 50.0f + 40.0f, p.getName());
                    p.getInventory().insertItem(lump, true);
                }
                catch (FailedException | NoSuchTemplateException wurmServerException) {
                    // empty catch block
                }
            }
        });
        JournalTier intermediateTier2 = new JournalTier(5, "Picking up Speed", 4, 6, 6, 69, 586, 588, 578, 581, 580, 579, 584, 583, 587, 582, 585, 577);
        intermediateTier2.setReward(new JournalReward("Proficient Title, Choice of Affinity & 3hr Sleep Bonus"){

            @Override
            public void runReward(Player p) {
                p.addTitle(Titles.Title.Journal_T5);
                p.getSaveFile().addToSleep(10800);
                try {
                    Item token = ItemFactory.createItem(1438, 80.0f, p.getName());
                    p.getInventory().insertItem(token, true);
                }
                catch (FailedException | NoSuchTemplateException wurmServerException) {
                    // empty catch block
                }
            }
        });
        JournalTier intermediateTier3 = new JournalTier(6, "The Winding Road", 5, 7, 6, 70, 590, 591, 594, 589, 592, 596, 597, 600, 599, 595, 593, 598);
        intermediateTier3.setReward(new JournalReward("Talented Title, Increased Max Sleep Bonus & 6hr Sleep Bonus"){

            @Override
            public void runReward(Player p) {
                p.addTitle(Titles.Title.Journal_T6);
                p.setFlag(77, true);
                p.getSaveFile().addToSleep(21600);
            }
        });
        JournalTier priestTier1 = new JournalTier(10, "Dedication", -1, 11, 6, 78, new int[]{604, 606, 609, 608, 614, 615, 605, 607, 612, 611, 610, 613}){

            @Override
            public boolean isVisible(long playerId) {
                Optional<PlayerInfo> playerInfo = PlayerInfoFactory.getPlayerInfoOptional(playerId);
                return playerInfo.isPresent() && playerInfo.get().isPriest;
            }
        };
        priestTier1.setReward(new JournalReward("Blessed Title, 2h Sleep Bonus"){

            @Override
            public void runReward(Player p) {
                p.addTitle(Titles.Title.Journal_P1);
                p.getSaveFile().addToSleep(7200);
            }
        });
        JournalTier priestTier2 = new JournalTier(11, "Approbation", 10, 12, 6, 79, 617, 619, 627, 616, 625, 623, 622, 621, 626, 618, 624, 620);
        priestTier2.setReward(new JournalReward("Angelic Title, Increased Max Faith Gains Per Day & 3hr Sleep Bonus"){

            @Override
            public void runReward(Player p) {
                p.addTitle(Titles.Title.Journal_P2);
                p.setFlag(81, true);
                p.getSaveFile().addToSleep(10800);
            }
        });
        JournalTier priestTier3 = new JournalTier(12, "Benediction", 11, -1, 5, 80, 628, 630, 635, 634, 636, 632, 633, 631, 637, 629);
        priestTier3.setReward(new JournalReward("Divine Title, +5 Power to Spell Casts & 5hr Sleep Bonus"){

            @Override
            public void runReward(Player p) {
                p.addTitle(Titles.Title.Journal_P3);
                p.setFlag(82, true);
                p.getSaveFile().addToSleep(18000);
            }
        });
        allTiers.put((byte)0, tutorialTier);
        allTiers.put((byte)1, basicTier1);
        allTiers.put((byte)2, basicTier2);
        allTiers.put((byte)3, basicTier3);
        allTiers.put((byte)4, intermediateTier1);
        allTiers.put((byte)5, intermediateTier2);
        allTiers.put((byte)6, intermediateTier3);
        allTiers.put((byte)10, priestTier1);
        allTiers.put((byte)11, priestTier2);
        allTiers.put((byte)12, priestTier3);
    }
}

