/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FlowerpotBehaviour
extends ItemBehaviour {
    private static final Logger logger = Logger.getLogger(FlowerpotBehaviour.class.getName());

    public FlowerpotBehaviour() {
        super((short)47);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
        if (source.isFlower() && (target.getTemplateId() == 813 || target.getTemplateId() == 1001)) {
            toReturn.add(Actions.actionEntrys[564]);
        } else if (source.isContainerLiquid()) {
            Item[] items = source.getItemsAsArray();
            for (int i = 0; i < items.length; ++i) {
                if (items[i].getTemplateId() != 128) continue;
                toReturn.add(Actions.actionEntrys[565]);
                break;
            }
        }
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short action, float counter) {
        boolean toReturn = super.action(act, performer, target, action, counter);
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
        if (action == 1) {
            return this.action(act, performer, target, action, counter);
        }
        if (action == 564) {
            return FlowerpotBehaviour.plantFlowerInPot(act, performer, source, target, counter);
        }
        if (action == 565) {
            return FlowerpotBehaviour.waterFlower(act, performer, source, target, counter);
        }
        return super.action(act, performer, source, target, action, counter);
    }

    private static final boolean waterFlower(Action act, Creature performer, Item waterSource, Item pot, float counter) {
        int time = 0;
        Skill gardening = null;
        Item water = null;
        Item[] items = waterSource.getItemsAsArray();
        for (int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() != 128) continue;
            water = items[i];
            break;
        }
        if (water == null) {
            performer.getCommunicator().sendNormalServerMessage("You need water to water the flowers.", (byte)3);
            return true;
        }
        if (water.getWeightGrams() < 100) {
            performer.getCommunicator().sendNormalServerMessage("You need more water in order to water the flowers.", (byte)3);
            return true;
        }
        if (pot.getDamage() == 0.0f) {
            performer.getCommunicator().sendNormalServerMessage("The flowers are in no need of watering.", (byte)3);
            return true;
        }
        try {
            gardening = performer.getSkills().getSkill(10045);
        }
        catch (NoSuchSkillException nse) {
            gardening = performer.getSkills().learn(10045, 1.0f);
        }
        if (counter == 1.0f) {
            time = Actions.getStandardActionTime(performer, gardening, pot, 0.0);
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start watering the flowers.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to water some flowers.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[565].getVerbString(), true, time);
            return false;
        }
        time = act.getTimeLeft();
        if (counter * 10.0f > (float)time) {
            double power = gardening.skillCheck(15.0, 0.0, false, counter);
            if (power > 0.0) {
                float dmgChange = 20.0f * (float)(power / 100.0);
                pot.setDamage(Math.max(0.0f, pot.getDamage() - dmgChange));
                water.setWeight(water.getWeightGrams() - 100, true);
                performer.getCommunicator().sendNormalServerMessage("You successfully watered the flowers, they look healthier.", (byte)2);
                return true;
            }
            int waterReduction = 100;
            if (power >= -20.0) {
                performer.getCommunicator().sendNormalServerMessage("You accidentally miss the pot and pour the water on the ground instead.", (byte)3);
            } else if (power > -50.0 && power < -20.0) {
                performer.getCommunicator().sendNormalServerMessage("You spill water all over your clothes.", (byte)3);
            } else {
                performer.getCommunicator().sendNormalServerMessage("For some inexplicable reason you poured all of the water on the ground, how you thought it would help you will never know.");
                waterReduction = Math.min(water.getWeightGrams(), 200);
            }
            water.setWeight(water.getWeightGrams() - waterReduction, true);
            return true;
        }
        return false;
    }

    private static final int getFlowerpotIdFromFlower(Item flower, Item pot) {
        if (pot.getTemplateId() == 813) {
            int templateId = flower.getTemplateId();
            if (templateId == 498) {
                return 814;
            }
            if (templateId == 499) {
                return 818;
            }
            if (templateId == 500) {
                return 816;
            }
            if (templateId == 501) {
                return 817;
            }
            if (templateId == 502) {
                return 815;
            }
            if (templateId == 503) {
                return 819;
            }
            return 820;
        }
        int templateId = flower.getTemplateId();
        if (templateId == 498) {
            return 1002;
        }
        if (templateId == 499) {
            return 1006;
        }
        if (templateId == 500) {
            return 1004;
        }
        if (templateId == 501) {
            return 1005;
        }
        if (templateId == 502) {
            return 1003;
        }
        if (templateId == 503) {
            return 1007;
        }
        return 1008;
    }

    private static boolean plantFlowerInPot(Action act, Creature performer, Item flower, Item pot, float counter) {
        int time = 0;
        if (counter == 1.0f) {
            Skill gardening = null;
            try {
                gardening = performer.getSkills().getSkill(10045);
            }
            catch (NoSuchSkillException nss) {
                gardening = performer.getSkills().learn(10045, 1.0f);
            }
            time = Actions.getStandardActionTime(performer, gardening, flower, 0.0);
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start planting the flowers.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to plant some flowers.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[564].getVerbString(), true, time);
            return false;
        }
        time = act.getTimeLeft();
        if (counter * 10.0f > (float)time) {
            float ql = flower.getQualityLevel() + pot.getQualityLevel();
            ql /= 2.0f;
            float dmg = flower.getDamage() + pot.getDamage();
            dmg /= 2.0f;
            Skill gardening = null;
            try {
                gardening = performer.getSkills().getSkill(10045);
            }
            catch (NoSuchSkillException nss) {
                gardening = performer.getSkills().learn(10045, 1.0f);
            }
            try {
                int toCreate = FlowerpotBehaviour.getFlowerpotIdFromFlower(flower, pot);
                ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(toCreate);
                double power = gardening.skillCheck(template.getDifficulty() + dmg, ql, false, counter);
                if (power > 0.0) {
                    try {
                        Item newPot = ItemFactory.createItem(toCreate, pot.getQualityLevel(), pot.getRarity(), performer.getName());
                        newPot.setDamage(pot.getDamage());
                        newPot.setLastOwnerId(pot.getLastOwnerId());
                        newPot.setDescription(pot.getDescription());
                        Items.destroyItem(pot.getWurmId());
                        performer.getInventory().insertItem(newPot, true);
                        performer.getCommunicator().sendNormalServerMessage("You finished planting the flowers in the pot.");
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, nst.getMessage(), nst);
                    }
                    catch (FailedException fe) {
                        logger.log(Level.WARNING, fe.getMessage(), fe);
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("Sadly, the fragile flowers do not survive despite your best efforts.", (byte)3);
                }
                Items.destroyItem(flower.getWurmId());
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, nst.getMessage(), nst);
            }
            return true;
        }
        return false;
    }
}

