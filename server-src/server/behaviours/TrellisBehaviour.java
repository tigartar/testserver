/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmHarvestables;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.TileTreeBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.zones.Zones;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class TrellisBehaviour
extends ItemBehaviour {
    private static final Logger logger = Logger.getLogger(TrellisBehaviour.class.getName());

    public TrellisBehaviour() {
        super((short)58);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
        toReturn.addAll(this.getBehavioursForTrellis(performer, null, target));
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
        toReturn.addAll(this.getBehavioursForTrellis(performer, source, target));
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short action, float counter) {
        boolean[] ans = this.trellisAction(act, performer, null, target, action, counter);
        if (ans[0]) {
            return ans[1];
        }
        return super.action(act, performer, target, action, counter);
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
        boolean[] ans = this.trellisAction(act, performer, source, target, action, counter);
        if (ans[0]) {
            return ans[1];
        }
        return super.action(act, performer, source, target, action, counter);
    }

    private List<ActionEntry> getBehavioursForTrellis(Creature performer, @Nullable Item source, Item trellis) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        if (trellis.isHarvestable() && source != null && source.getTemplateId() == 267 && Methods.isActionAllowed(performer, (short)152, trellis)) {
            toReturn.add(Actions.actionEntrys[152]);
        }
        LinkedList<ActionEntry> nature = new LinkedList<ActionEntry>();
        FoliageAge age = FoliageAge.getFoliageAge(trellis.getAuxData());
        if (performer.getPower() >= 2 && source != null && source.getTemplateId() == 176) {
            nature.add(Actions.actionEntrys[188]);
        }
        if (age.isPrunable()) {
            nature.add(Actions.actionEntrys[373]);
        }
        if (TileTreeBehaviour.isSproutingAge(age.getAgeId())) {
            nature.add(Actions.actionEntrys[187]);
        }
        if (this.hasFruit(performer, trellis)) {
            nature.add(new ActionEntry(852, "Study", "making notes"));
        }
        if (nature.size() > 0) {
            Collections.sort(nature);
            toReturn.add(new ActionEntry((short)(-nature.size()), "Nature", "nature", emptyIntArr));
            toReturn.addAll(nature);
        }
        return toReturn;
    }

    public boolean[] trellisAction(Action act, Creature performer, @Nullable Item source, Item target, short action, float counter) {
        if (action == 152 && source.getTemplateId() == 267 && target.isPlanted() && (target.isHarvestable() || counter > 1.0f)) {
            return new boolean[]{true, this.harvest(act, performer, source, target, counter)};
        }
        if (action == 188 && performer.getPower() >= 2 && source != null && source.getTemplateId() == 176) {
            target.setLeftAuxData(target.getLeftAuxData() + 1);
            target.updateName();
            return new boolean[]{true, true};
        }
        if (action == 373 && source != null) {
            return new boolean[]{true, TrellisBehaviour.prune(act, performer, source, target, counter)};
        }
        if (action == 187 && source != null && source.getTemplateId() == 267) {
            return new boolean[]{true, this.pickSprout(performer, source, target, counter, act)};
        }
        if (action == 852 && this.hasFruit(performer, target)) {
            return new boolean[]{true, this.study(act, performer, target, action, counter)};
        }
        if (action == 1) {
            super.action(act, performer, target, action, counter);
            this.examine(performer, target);
            return new boolean[]{true, true};
        }
        return new boolean[]{false, false};
    }

    private boolean harvest(Action act, Creature performer, Item tool, Item target, float counter) {
        if (!performer.getInventory().mayCreatureInsertItem()) {
            performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put what you harvest.");
            return true;
        }
        if (!Methods.isActionAllowed(performer, act.getNumber(), target)) {
            return true;
        }
        int templateId = target.getTemplate().getHarvestsTo();
        if (templateId == 411) {
            templateId = target.getTileY() > Zones.worldTileSizeY / 2 ? 411 : 414;
        }
        boolean toReturn = false;
        int time = 150;
        Skill skill = performer.getSkills().getSkillOrLearn(10045);
        time = Actions.getStandardActionTime(performer, skill, tool, 0.0);
        if (counter == 1.0f) {
            int maxSearches = TrellisBehaviour.calcTrellisMaxHarvest(target, skill.getKnowledge(0.0), tool);
            time = Actions.getQuickActionTime(performer, skill, null, 0.0);
            act.setNextTick(time);
            act.setTickCount(1);
            act.setData(0L);
            float totalTime = time * maxSearches;
            act.setTimeLeft((int)totalTime);
            performer.getCommunicator().sendNormalServerMessage("You start to harvest the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to harvest a trellis.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[152].getVerbString(), true, (int)totalTime);
            performer.getStatus().modifyStamina(-500.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (tool != null && act.justTickedSecond()) {
            tool.setDamage(tool.getDamage() + 3.0E-4f * tool.getDamageModifier());
        }
        if (counter * 10.0f >= act.getNextTick()) {
            if (act.getRarity() != 0) {
                performer.playPersonalSound("sound.fx.drumroll");
            }
            int searchCount = act.getTickCount();
            int maxSearches = TrellisBehaviour.calcTrellisMaxHarvest(target, skill.getKnowledge(0.0), tool);
            act.incTickCount();
            act.incNextTick(Actions.getQuickActionTime(performer, skill, null, 0.0));
            int knowledge = (int)skill.getKnowledge(0.0);
            performer.getStatus().modifyStamina(-1500 * searchCount);
            if (searchCount >= maxSearches) {
                toReturn = true;
            }
            act.setData(act.getData() + 1L);
            double power = skill.skillCheck(skill.getKnowledge(0.0) - 5.0, tool, 0.0, false, counter / (float)searchCount);
            try {
                float ql = (float)knowledge + (float)(100 - knowledge) * ((float)power / 500.0f);
                float modifier = 1.0f;
                if (tool.getSpellEffects() != null) {
                    modifier = tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                }
                target.setLastMaintained(WurmCalendar.currentTime);
                ql = Math.min(100.0f, (ql + (float)tool.getRarity()) * modifier);
                Item harvested = ItemFactory.createItem(templateId, Math.max(1.0f, ql), act.getRarity(), null);
                if (ql < 0.0f) {
                    harvested.setDamage(-ql / 2.0f);
                }
                performer.getInventory().insertItem(harvested);
                SoundPlayer.playSound("sound.forest.branchsnap", target.getTileX(), target.getTileY(), true, 3.0f);
                if (searchCount == 1) {
                    target.setHarvestable(false);
                }
                performer.getCommunicator().sendNormalServerMessage("You harvest " + harvested.getNameWithGenus() + " from the " + target.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " harvests " + harvested.getNameWithGenus() + " from a trellis.", performer, 5);
                if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                    performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                    toReturn = true;
                }
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "No template for " + templateId, nst);
                performer.getCommunicator().sendNormalServerMessage("You fail to harvest. You realize something is wrong with the world.");
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, fe.getMessage(), fe);
                performer.getCommunicator().sendNormalServerMessage("You fail to harvest. You realize something is wrong with the world.");
            }
            if (searchCount < maxSearches) {
                act.setRarity(performer.getRarity());
            }
        }
        return toReturn;
    }

    private void examine(Creature performer, Item target) {
        switch (target.getTemplateId()) {
            case 920: {
                String grapetype;
                String string = grapetype = target.getTileY() > Zones.worldTileSizeY / 2 ? "blue grapes." : "green grapes.";
                if (target.isHarvestable()) {
                    performer.getCommunicator().sendNormalServerMessage("The trellis has some juicy " + grapetype);
                    break;
                }
                if (TrellisBehaviour.isAlmostRipe(920)) {
                    performer.getCommunicator().sendNormalServerMessage("The trellis has a couple of immature " + grapetype);
                    break;
                }
                if (!TrellisBehaviour.hasBeenPicked(target, 920)) break;
                performer.getCommunicator().sendNormalServerMessage("The trellis has no grapes left; all have been picked.");
                break;
            }
            case 1018: {
                if (target.isHarvestable()) {
                    performer.getCommunicator().sendNormalServerMessage("The trellis has some beautiful flowers.");
                    break;
                }
                if (TrellisBehaviour.isAlmostRipe(1018)) {
                    performer.getCommunicator().sendNormalServerMessage("The trellis has a couple of promising buds.");
                    break;
                }
                if (!TrellisBehaviour.hasBeenPicked(target, 1018)) break;
                performer.getCommunicator().sendNormalServerMessage("The trellis has no flowers left; all have been picked.");
                break;
            }
            case 1274: {
                if (target.isHarvestable()) {
                    performer.getCommunicator().sendNormalServerMessage("The trellis has some ripe hops, ready to be harvested.");
                    break;
                }
                if (TrellisBehaviour.isAlmostRipe(1274)) {
                    performer.getCommunicator().sendNormalServerMessage("The trellis has a couple of immature hops.");
                    break;
                }
                if (!TrellisBehaviour.hasBeenPicked(target, 1274)) break;
                performer.getCommunicator().sendNormalServerMessage("The trellis has no hops left; all have been picked.");
            }
        }
    }

    private static boolean isAlmostRipe(int type) {
        switch (type) {
            case 920: {
                return WurmHarvestables.Harvestable.GRAPE.isAlmostRipe();
            }
            case 1018: {
                return WurmHarvestables.Harvestable.ROSE.isAlmostRipe();
            }
            case 1274: {
                return WurmHarvestables.Harvestable.HOPS.isAlmostRipe();
            }
        }
        return false;
    }

    private static boolean hasBeenPicked(Item target, int type) {
        switch (type) {
            case 920: {
                if (WurmHarvestables.Harvestable.GRAPE.isHarvestable()) break;
                return false;
            }
            case 1018: {
                if (WurmHarvestables.Harvestable.ROSE.isHarvestable()) break;
                return false;
            }
            case 1274: {
                if (WurmHarvestables.Harvestable.HOPS.isHarvestable()) break;
                return false;
            }
        }
        return !target.isHarvestable();
    }

    static boolean prune(Action action, Creature performer, Item sickle, Item trellis, float counter) {
        boolean toReturn = true;
        if (sickle.getTemplateId() == 267) {
            FoliageAge age = FoliageAge.getFoliageAge(trellis.getAuxData());
            String trellisName = trellis.getName().toLowerCase();
            if (!age.isPrunable()) {
                performer.getCommunicator().sendNormalServerMessage("It does not make sense to prune now.");
                return true;
            }
            toReturn = false;
            int time = 150;
            Skill forestry = performer.getSkills().getSkillOrLearn(10048);
            Skill sickskill = performer.getSkills().getSkillOrLearn(10046);
            if (sickle.getTemplateId() == 267) {
                time = Actions.getStandardActionTime(performer, forestry, sickle, sickskill.getKnowledge(0.0));
            }
            if (counter == 1.0f) {
                performer.getCommunicator().sendNormalServerMessage("You start to prune the " + trellisName + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to prune the " + trellisName + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[373].getVerbString(), true, time);
            }
            if (action.justTickedSecond()) {
                sickle.setDamage(sickle.getDamage() + 3.0E-4f * sickle.getDamageModifier());
            }
            if (counter * 10.0f >= (float)time) {
                double bonus = 0.0;
                double power = 0.0;
                bonus = Math.max(1.0, sickskill.skillCheck(1.0, sickle, 0.0, false, counter));
                power = forestry.skillCheck(forestry.getKnowledge(0.0) - 10.0, sickle, bonus, false, counter);
                toReturn = true;
                SoundPlayer.playSound("sound.forest.branchsnap", trellis.getTileX(), trellis.getTileY(), true, 3.0f);
                if (power < 0.0) {
                    performer.getCommunicator().sendNormalServerMessage("You make a lot of errors and need to take a break.");
                    return toReturn;
                }
                FoliageAge newage = age.getPrunedAge();
                trellis.setLeftAuxData(newage.getAgeId());
                TileEvent.log(trellis.getTileX(), trellis.getTileY(), 0, performer.getWurmId(), 373);
                trellis.updateName();
                performer.getCommunicator().sendNormalServerMessage("You prune the " + trellisName + ".");
                Server.getInstance().broadCastAction(performer.getName() + " prunes the " + trellisName + ".", performer, 5);
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot prune with that.");
            logger.log(Level.WARNING, performer.getName() + " tried to prune with a " + sickle.getName());
        }
        return toReturn;
    }

    private boolean pickSprout(Creature performer, Item sickle, Item trellis, float counter, Action act) {
        boolean toReturn = true;
        if (sickle.getTemplateId() == 267) {
            if (!performer.getInventory().mayCreatureInsertItem()) {
                performer.getCommunicator().sendNormalServerMessage("Your inventory is full. You would have no space to put the sprout.");
                return true;
            }
            int age = trellis.getLeftAuxData();
            if (age == 7 || age == 9 || age == 11 || age == 13) {
                int weight;
                Skill forestry = performer.getSkills().getSkillOrLearn(10048);
                toReturn = false;
                int time = Actions.getStandardActionTime(performer, forestry, sickle, 0.0);
                if (counter == 1.0f) {
                    try {
                        weight = ItemTemplateFactory.getInstance().getTemplate(266).getWeightGrams();
                        if (!performer.canCarry(weight)) {
                            performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the sprout. You need to drop some things first.");
                            return true;
                        }
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, nst.getLocalizedMessage(), nst);
                        return true;
                    }
                    performer.getCommunicator().sendNormalServerMessage("You start cutting a sprout from the trellis.");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to cut a sprout off a trellis.", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[187].getVerbString(), true, time);
                }
                if (counter * 10.0f >= (float)time) {
                    if (act.getRarity() != 0) {
                        performer.playPersonalSound("sound.fx.drumroll");
                    }
                    try {
                        weight = ItemTemplateFactory.getInstance().getTemplate(266).getWeightGrams();
                        if (!performer.canCarry(weight)) {
                            performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the sprout. You need to drop some things first.");
                            return true;
                        }
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, nst.getLocalizedMessage(), nst);
                        return true;
                    }
                    sickle.setDamage(sickle.getDamage() + 0.003f * sickle.getDamageModifier());
                    double bonus = 0.0;
                    double power = 0.0;
                    Skill sickskill = performer.getSkills().getSkillOrLearn(10046);
                    bonus = Math.max(1.0, sickskill.skillCheck(1.0, sickle, 0.0, false, counter));
                    power = forestry.skillCheck(1.0, sickle, bonus, false, counter);
                    toReturn = true;
                    String templateType = "sprout";
                    try {
                        int template = 266;
                        byte material = 0;
                        if (trellis.getTemplateId() == 920) {
                            template = 266;
                            material = 49;
                            templateType = "sprout";
                        } else if (trellis.getTemplateId() == 1018) {
                            template = 266;
                            material = 47;
                            templateType = "sprout";
                        } else if (trellis.getTemplateId() == 919) {
                            template = 917;
                            material = 68;
                            templateType = "seedling";
                        } else if (trellis.getTemplateId() == 1274) {
                            template = 1275;
                            material = 68;
                            templateType = "seedling";
                        }
                        float modifier = 1.0f;
                        if (sickle.getSpellEffects() != null) {
                            modifier = sickle.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                        }
                        Item sprout = ItemFactory.createItem(template, Math.max(1.0f, Math.min(100.0f, (float)power * modifier + (float)sickle.getRarity())), material, act.getRarity(), null);
                        if (power < 0.0) {
                            sprout.setDamage((float)(-power) / 2.0f);
                        }
                        SoundPlayer.playSound("sound.forest.branchsnap", trellis.getTileX(), trellis.getTileY(), true, 2.0f);
                        performer.getInventory().insertItem(sprout);
                        trellis.setLeftAuxData(--age);
                        trellis.updateName();
                        performer.getCommunicator().sendNormalServerMessage("You cut a " + templateType + " from the trellis.");
                        Server.getInstance().broadCastAction(performer.getName() + " cuts a " + templateType + " off a trellis.", performer, 5);
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, "No template for " + templateType + "!", nst);
                        performer.getCommunicator().sendNormalServerMessage("You fail to pick the " + templateType + ". You realize something is wrong with the world.");
                    }
                    catch (FailedException fe) {
                        logger.log(Level.WARNING, fe.getMessage(), fe);
                        performer.getCommunicator().sendNormalServerMessage("You fail to pick the " + templateType + ". You realize something is wrong with the world.");
                    }
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("The trellis has nothing to pick.");
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot pick sprouts with that.");
            logger.log(Level.WARNING, performer.getName() + " tried to pick sprout with a " + sickle.getName());
        }
        return toReturn;
    }

    private boolean study(Action act, Creature performer, Item trellis, short action, float counter) {
        int harvestableId = WurmHarvestables.getHarvestableIdFromTrellis(trellis.getTemplateId());
        WurmHarvestables.Harvestable harvestable = WurmHarvestables.getHarvestable(harvestableId);
        if (harvestable == null) {
            performer.getCommunicator().sendNormalServerMessage("You decide not to study " + trellis.getName() + " as it seems never to be harvestable.");
            return true;
        }
        int time = 0;
        if (counter == 1.0f) {
            time = 500;
            performer.getCommunicator().sendNormalServerMessage("You start to study the " + trellis.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to study the " + trellis.getName() + ".", performer, 5);
            performer.sendActionControl("studying " + trellis.getName(), true, time);
            act.setTimeLeft(time);
            return false;
        }
        time = act.getTimeLeft();
        if (act.mayPlaySound()) {
            Methods.sendSound(performer, "sound.work.foragebotanize");
        }
        TileTreeBehaviour.sendStudyMessages(performer, harvestable, act.currentSecond());
        if (counter * 10.0f >= (float)time) {
            if (performer.getPower() < 2) {
                trellis.setHarvestable(false);
            }
            ((Player)performer).setStudied(WurmHarvestables.getHarvestableIdFromTrellis(trellis.getTemplateId()));
            performer.getCommunicator().sendNormalServerMessage("You finish studying the " + trellis.getName() + ". You now need to record the study results.");
            Server.getInstance().broadCastAction(performer.getName() + " looks pleased with " + performer.getHisHerItsString() + " study results.", performer, 5);
            return true;
        }
        return false;
    }

    private static int calcTrellisMaxHarvest(Item trellis, double currentSkill, Item tool) {
        float extraChance;
        int bonus = 0;
        if (tool.getSpellEffects() != null && (extraChance = tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_FARMYIELD) - 1.0f) > 0.0f && Server.rand.nextFloat() < extraChance) {
            ++bonus;
        }
        return Math.min((int)(trellis.getCurrentQualityLevel() + 1.0f), (int)(currentSkill + 28.0) / 27 + bonus);
    }

    boolean hasFruit(Creature performer, Item trellis) {
        byte age = trellis.getLeftAuxData();
        if (age > FoliageAge.YOUNG_FOUR.getAgeId() && age < FoliageAge.OVERAGED.getAgeId()) {
            if (Servers.isThisATestServer() && performer.getPower() > 1) {
                return true;
            }
            return trellis.isHarvestable();
        }
        return false;
    }
}

