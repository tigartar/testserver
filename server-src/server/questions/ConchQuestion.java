/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.players.Abilities;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zones;
import java.util.LinkedList;
import java.util.Properties;

public class ConchQuestion
extends Question {
    Item conch;
    int nextTemplateId = 0;

    public ConchQuestion(Creature aResponder, long aTarget) {
        super(aResponder, "The Conch Speaks", "As you listen, you hear voices from beyond", 12800, aTarget);
        try {
            this.conch = Items.getItem(aTarget);
        }
        catch (NoSuchItemException nsi) {
            this.conch = null;
        }
    }

    private static final boolean sendAchievementNeeded(Creature responder, StringBuilder buf, Item conch) {
        Achievements ach = Achievements.getAchievementObject(responder.getWurmId());
        if (conch.getAuxData() == 10 || ach.getAchievement(1) == null) {
            return ConchQuestion.sendSixthBml(responder, buf, 1, conch);
        }
        if (ach.getAchievement(52) != null) {
            conch.setData(14L);
            return false;
        }
        if (conch.getAuxData() == 12 || ach.getAchievement(52) == null) {
            return ConchQuestion.sendSeventhBml(responder, buf, 52, conch);
        }
        return false;
    }

    private static final boolean sendItemNeeded(Creature responder, StringBuilder buf, Item conch) {
        if (responder.getKingdomTemplateId() == 2) {
            if (conch.getAuxData() == 0 || !responder.hasAbility(Abilities.getAbilityForItem(809, responder))) {
                return ConchQuestion.sendFirstBml(responder, buf, 809, conch);
            }
            if (conch.getAuxData() == 2 || !responder.hasAbility(Abilities.getAbilityForItem(808, responder))) {
                return ConchQuestion.sendSecondBml(responder, buf, 808, conch);
            }
            if (conch.getAuxData() == 4 || !responder.hasAbility(Abilities.getAbilityForItem(798, responder))) {
                return ConchQuestion.sendThirdBml(responder, buf, 798, conch);
            }
            if (conch.getAuxData() == 6 || !responder.hasAbility(Abilities.getAbilityForItem(810, responder))) {
                return ConchQuestion.sendFourthBml(responder, buf, 810, conch);
            }
            if (conch.getAuxData() == 8 || !responder.hasAbility(Abilities.getAbilityForItem(807, responder))) {
                return ConchQuestion.sendFifthBml(responder, buf, 807, conch);
            }
        } else if (responder.getKingdomTemplateId() == 3) {
            if (conch.getAuxData() == 0 || !responder.hasAbility(Abilities.getAbilityForItem(808, responder))) {
                return ConchQuestion.sendFirstBml(responder, buf, 808, conch);
            }
            if (conch.getAuxData() == 2 || !responder.hasAbility(Abilities.getAbilityForItem(809, responder))) {
                return ConchQuestion.sendSecondBml(responder, buf, 809, conch);
            }
            if (conch.getAuxData() == 4 || !responder.hasAbility(Abilities.getAbilityForItem(807, responder))) {
                return ConchQuestion.sendThirdBml(responder, buf, 807, conch);
            }
            if (conch.getAuxData() == 6 || !responder.hasAbility(Abilities.getAbilityForItem(810, responder))) {
                return ConchQuestion.sendFourthBml(responder, buf, 810, conch);
            }
            if (conch.getAuxData() == 8 || !responder.hasAbility(Abilities.getAbilityForItem(798, responder))) {
                return ConchQuestion.sendFifthBml(responder, buf, 798, conch);
            }
        } else {
            if (conch.getAuxData() == 0 || !responder.hasAbility(Abilities.getAbilityForItem(807, responder))) {
                return ConchQuestion.sendFirstBml(responder, buf, 807, conch);
            }
            if (conch.getAuxData() == 2 || !responder.hasAbility(Abilities.getAbilityForItem(808, responder))) {
                return ConchQuestion.sendSecondBml(responder, buf, 808, conch);
            }
            if (conch.getAuxData() == 4 || !responder.hasAbility(Abilities.getAbilityForItem(810, responder))) {
                return ConchQuestion.sendThirdBml(responder, buf, 810, conch);
            }
            if (conch.getAuxData() == 6 || !responder.hasAbility(Abilities.getAbilityForItem(809, responder))) {
                return ConchQuestion.sendFourthBml(responder, buf, 809, conch);
            }
            if (conch.getAuxData() == 8 || !responder.hasAbility(Abilities.getAbilityForItem(798, responder))) {
                return ConchQuestion.sendFifthBml(responder, buf, 798, conch);
            }
        }
        if (conch.getAuxData() < 10) {
            conch.setAuxData((byte)10);
        }
        return false;
    }

    @Override
    public void answer(Properties answers) {
        if (this.conch == null) {
            this.getResponder().getCommunicator().sendAlertServerMessage("The Conch is gone!");
            return;
        }
        String key = "listen";
        String val = answers.getProperty("listen");
        if (Boolean.parseBoolean(val)) {
            if (this.conch.getAuxData() == 0) {
                this.conch.setAuxData((byte)1);
            } else if (this.conch.getAuxData() % 2 == 0) {
                this.conch.setAuxData((byte)(this.conch.getAuxData() + 1));
            }
            ConchQuestion newq = new ConchQuestion(this.getResponder(), this.conch.getWurmId());
            newq.sendQuestionAfter();
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.getResponder().getPower() > 0) {
            buf.append("text{text='All you hear is muffled sounds.'}text{text=''}");
        } else if (this.conch != null) {
            ConchQuestion.addBml(this.getResponder(), this.conch, buf);
            buf.append("text{text='Do you wish to continue listening to the voices?'}text{text=''}");
            buf.append("radio{ group='listen'; id='true';text='Ok'}");
            buf.append("radio{ group='listen'; id='false';text='Not now';selected='true'}");
        } else {
            buf.append("text{text='The Conch is gone!'}text{text=''}");
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    public void sendQuestionAfter() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.getResponder().getPower() > 0) {
            buf.append("text{text='All you hear is muffled sounds.'}text{text=''}");
        } else if (this.conch != null) {
            if (this.conch.getAuxData() == 1) {
                this.sendAfterFirstBml(this.getResponder(), buf, this.conch);
            } else if (this.conch.getAuxData() == 3) {
                this.sendAfterSecondBml(this.getResponder(), buf, this.conch);
            } else if (this.conch.getAuxData() == 5) {
                this.sendAfterThirdBml(this.getResponder(), buf, this.conch);
            } else if (this.conch.getAuxData() == 7) {
                this.sendAfterFourthBml(this.getResponder(), buf, this.conch);
            } else if (this.conch.getAuxData() == 9) {
                this.sendAfterFifthBml(this.getResponder(), buf, this.conch);
            } else if (this.conch.getAuxData() == 11) {
                ConchQuestion.sendAfterSixthBml(this.getResponder(), buf, this.conch);
            } else if (this.conch.getAuxData() == 13) {
                ConchQuestion.sendAfterSeventhBml(this.getResponder(), buf, this.conch);
            } else if (this.conch.getAuxData() == 15) {
                ConchQuestion.addFinalBml(this.getResponder(), buf, this.conch);
            } else {
                buf.append("text{text='The conch grows silent in anticipation.'}text{text=''}");
            }
        } else {
            buf.append("text{text='The conch is gone!'}text{text=''}");
        }
        buf.append(this.createOkAnswerButton());
        this.getResponder().getCommunicator().sendBml(300, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private static final void addBml(Creature responder, Item conch, StringBuilder buf) {
        if (!ConchQuestion.sendItemNeeded(responder, buf, conch) && !ConchQuestion.sendAchievementNeeded(responder, buf, conch)) {
            Achievements ach = Achievements.getAchievementObject(responder.getWurmId());
            if (ach.getAchievement(322) == null) {
                buf.append("text{text='It seems the conch is trying to get your attention.'}text{text=''}");
            } else {
                buf.append("text{text='All you hear is muffled sounds but maybe you can make something out of it.'}text{text=''}");
            }
        }
    }

    private static final boolean addFinalBml(Creature responder, StringBuilder buf, Item conch) {
        if (ConchQuestion.isThisAdventureServer()) {
            buf.append("text{text=\"The " + ConchQuestion.addSpiritVoiceType(responder) + " says: \"}text{text=\"\"}");
            buf.append("text{text=\"'You seem to be ready. Ceyer, Brightberry and even Zampooklidin would have been proud of you.\"}text{text=\"\"}");
            buf.append("text{text=\"Find the Key in the darkness and if you so choose, use it. May your soul rule the Heavens wisely.\"}text{text=\"\"}");
            buf.append("text{text=\"Know however, that you will remain here and only the essence of your being travels.\"}text{text=\"\"}");
            buf.append("text{text=\"We hope you don't feel cheated by us. We really need help and hope that your deity will find a way to return us to Valrei.\"}text{text=\"\"}");
            buf.append("text{text=\"We will show you the way now.'\"}text{text=\"\"}");
            ConchQuestion.sendSignalToEntrance(responder, conch);
        }
        return true;
    }

    private static final boolean sendFirstBml(Creature responder, StringBuilder buf, int templateId, Item conch) {
        conch.setAuxData((byte)0);
        conch.setData1(templateId);
        buf.append("text{text=\"A faint " + ConchQuestion.addSpiritVoiceType(responder) + " is heard over the muffled sounds of the conch shell: \"}text{text=\"\"}");
        buf.append("text{text=\"'Can you hear me?\"}text{text=\"\"}");
        buf.append("text{text=\"I am a spirit of the nature. We can only see living beings as if through a veil and you sound very distant.\"}text{text=\"\"}");
        buf.append("text{text=\"We will show you the way to a powerful item if you want.\"}text{text=\"\"}");
        if (responder.hasAbility(Abilities.getAbilityForItem(templateId, responder))) {
            buf.append("text{text=\"You already are " + Abilities.getAbilityString(Abilities.getAbilityForItem(templateId, responder)) + ". That will speed things up!'\"}text{text=\"\"}");
            conch.setAuxData((byte)2);
        } else {
            buf.append("text{text=\"The item will teach you how to become " + Abilities.getAbilityString(Abilities.getAbilityForItem(templateId, responder)) + ".\"}text{text=\"\"}");
            buf.append("text{text=\"We will explain more afterwards.'\"}text{text=\"\"}");
        }
        return true;
    }

    public final void sendAfterFirstBml(Creature responder, StringBuilder buf, Item conch) {
        String tome = "tome";
        try {
            ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(conch.getData1());
            tome = template.getName();
        }
        catch (Exception template) {
            // empty catch block
        }
        buf.append("text{text=\"'You should look for a " + tome + ".\"}text{text=\"\"}");
        Item item = this.getItemToSend(conch.getData1());
        if (item == null) {
            buf.append("text{text=\"We could not locate that item. It seems you have to do missions for the gods and hope for the best.'\"}text{text=\"\"}");
        } else {
            this.sendSignalToItemTemplate(item);
            buf.append("text{text=\"'Look in the sky. We have created a colored spirit light for you to follow', the " + ConchQuestion.addSpiritVoiceType(responder) + " whispers.\"}text{text=\"\"}");
        }
        buf.append("text{text=\"'You need to prepare carefully for the journey.\"}text{text=\"\"}");
        buf.append("text{text=\"Take your time. It will be a hard trip. Build up your strength and resources.\"}text{text=\"\"}");
        buf.append("text{text=\"Listen to this shell again if you require guidance.\"}text{text=\"\"}");
        buf.append("text{text=\"Safe journeys.'\"}text{text=\"\"}");
        buf.append("text{text=\"The voice grows silent.\"}text{text=\"\"}");
    }

    private static final boolean sendSecondBml(Creature responder, StringBuilder buf, int templateId, Item conch) {
        conch.setData1(templateId);
        conch.setAuxData((byte)2);
        buf.append("text{text=\"As before, a " + ConchQuestion.addSpiritVoiceType(responder) + " is heard from the shell: \"}text{text=\"\"}");
        buf.append("text{text=\"'We were once servants to the Gods on the Moon of Valrei. Immortal and powerful.\"}text{text=\"\"}");
        buf.append("text{text=\"We heard and saw too much. Too much misery and.. pain. We were cast out. To this place.. this spirit world.\"}text{text=\"\"}");
        buf.append("text{text=\"Here we will remain forever.\"}text{text=\"\"}");
        buf.append("text{text=\"Unless, perhaps.. something dramatic happens on Valrei. We can only hope.\"}text{text=\"\"}");
        buf.append("text{text=\"At least there is a way.\"}text{text=\"\"}");
        if (responder.hasAbility(Abilities.getAbilityForItem(templateId, responder))) {
            buf.append("text{text=\"As you already are " + Abilities.getAbilityString(Abilities.getAbilityForItem(templateId, responder)) + " we will show you the next tome.'\"}text{text=\"\"}");
            conch.setAuxData((byte)4);
        } else {
            buf.append("text{text=\"For now, let us show you where you can become " + Abilities.getAbilityString(Abilities.getAbilityForItem(templateId, responder)) + ".'\"}text{text=\"\"}");
        }
        return true;
    }

    public final void sendAfterSecondBml(Creature responder, StringBuilder buf, Item conch) {
        String tome = "tome";
        try {
            ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(conch.getData1());
            tome = template.getName();
        }
        catch (Exception template) {
            // empty catch block
        }
        buf.append("text{text=\"'You should look for a " + tome + ".\"}text{text=\"\"}");
        Item item = this.getItemToSend(conch.getData1());
        if (item == null) {
            buf.append("text{text=\"'We could not locate that item. It seems you have to do missions for the gods and hope for the best.'\"}text{text=\"\"}");
        } else {
            this.sendSignalToItemTemplate(item);
            buf.append("text{text=\"'We have created a colored spirit light for you to follow', the " + ConchQuestion.addSpiritVoiceType(responder) + " whispers.\"}text{text=\"\"}");
        }
        buf.append("text{text=\"'Listen to this shell again if you require guidance.\"}text{text=\"\"}");
        buf.append("text{text=\"Good luck on your travels.'\"}text{text=\"\"}");
        buf.append("text{text=\"The voice grows silent.\"}text{text=\"\"}");
    }

    private static final boolean sendThirdBml(Creature responder, StringBuilder buf, int templateId, Item conch) {
        conch.setData1(templateId);
        conch.setAuxData((byte)4);
        buf.append("text{text=\"Again, the " + ConchQuestion.addSpiritVoiceType(responder) + " whispers from the shell: \"}text{text=\"\"}");
        buf.append("text{text=\"'There was a war. But before the war there was unity.\"}text{text=\"\"}");
        buf.append("text{text=\"The three masters Ceyer, Brightberry and Zampooklidin all were friends.\"}text{text=\"\"}");
        buf.append("text{text=\"The gods wanted different and handed down a Key to the Heavens, well aware of the impact this would have on the friendship.\"}text{text=\"\"}");
        buf.append("text{text=\"Ceyer was the one who received it and he consulted the others.\"}text{text=\"\"}");
        buf.append("text{text=\"Things did not turn out well.\"}text{text=\"\"}");
        if (responder.hasAbility(Abilities.getAbilityForItem(templateId, responder))) {
            buf.append("text{text=\"You already are " + Abilities.getAbilityString(Abilities.getAbilityForItem(templateId, responder)) + ". That will make the journey quicker.'\"}text{text=\"\"}");
            conch.setAuxData((byte)6);
        } else {
            buf.append("text{text=\"We will tell you more when you are " + Abilities.getAbilityString(Abilities.getAbilityForItem(templateId, responder)) + ".'\"}text{text=\"\"}");
        }
        return true;
    }

    public final void sendAfterThirdBml(Creature responder, StringBuilder buf, Item conch) {
        String tome = "tome";
        try {
            ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(conch.getData1());
            tome = template.getName();
        }
        catch (Exception template) {
            // empty catch block
        }
        buf.append("text{text=\"'You should look for a " + tome + ".\"}text{text=\"\"}");
        Item item = this.getItemToSend(conch.getData1());
        if (item == null) {
            buf.append("text{text=\"'We could not locate that item. It seems you have to do missions for the gods and hope for the best.'\"}text{text=\"\"}");
        } else {
            this.sendSignalToItemTemplate(item);
            buf.append("text{text=\"'Look to the skies. The spirit light should guide you, says the " + ConchQuestion.addSpiritVoiceType(responder) + ".\"}text{text=\"\"}");
        }
        buf.append("text{text=\"We wish you all the best.'\"}text{text=\"\"}");
        buf.append("text{text=\"The voice grows silent.\"}text{text=\"\"}");
    }

    private static final boolean sendFourthBml(Creature responder, StringBuilder buf, int templateId, Item conch) {
        conch.setData1(templateId);
        conch.setAuxData((byte)6);
        buf.append("text{text=\"From the shell, a " + ConchQuestion.addSpiritVoiceType(responder) + " continues the story: \"}text{text=\"\"}");
        buf.append("text{text=\"'As Ceyer, Zampooklidin and Brightberry discussed who should use the Key and ascend to Valrei a stranger approached.\"}text{text=\"\"}");
        buf.append("text{text=\"It was Malinkaan, a powerful Arch Mage who had found out about the Key. Rumours have it that Vynora gave him divination.\"}text{text=\"\"}");
        buf.append("text{text=\"As he made a lunge at Ceyer with his sword, Brightberry stepped in the way and shed her blood instead.\"}text{text=\"\"}");
        buf.append("text{text=\"Ceyer ran while Zampooklidin stayed and vanquished Malinkaan at Grimoleth Peak.\"}text{text=\"\"}");
        buf.append("text{text=\"Thoughts started to grow in Zampooklidins mind that he now had the right to the key, no doubt without the influence of Libila.\"}text{text=\"\"}");
        if (responder.hasAbility(Abilities.getAbilityForItem(templateId, responder))) {
            buf.append("text{text=\"You are a " + Abilities.getAbilityString(Abilities.getAbilityForItem(templateId, responder)) + " already indeed. We will show you the next place.'\"}text{text=\"\"}");
            conch.setAuxData((byte)8);
        } else {
            buf.append("text{text=\"Now you should become " + Abilities.getAbilityString(Abilities.getAbilityForItem(templateId, responder)) + " as well.'\"}text{text=\"\"}");
        }
        return true;
    }

    public final void sendAfterFourthBml(Creature responder, StringBuilder buf, Item conch) {
        String tome = "tome";
        try {
            ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(conch.getData1());
            tome = template.getName();
        }
        catch (Exception template) {
            // empty catch block
        }
        buf.append("text{text=\"'You should look for a " + tome + ".\"}text{text=\"\"}");
        Item item = this.getItemToSend(conch.getData1());
        if (item == null) {
            buf.append("text{text=\"'We could not locate that item. It seems you have to do missions for the gods and hope for the best.'\"}text{text=\"\"}");
        } else {
            this.sendSignalToItemTemplate(item);
            buf.append("text{text=\"'Find the spirit light we have created', advices the " + ConchQuestion.addSpiritVoiceType(responder) + ".\"}text{text=\"\"}");
        }
        buf.append("text{text=\"'And stay out of harms way.'\"}text{text=\"\"}");
        buf.append("text{text=\"The voice grows silent.\"}text{text=\"\"}");
    }

    private static final boolean sendFifthBml(Creature responder, StringBuilder buf, int templateId, Item conch) {
        conch.setData1(templateId);
        conch.setAuxData((byte)8);
        buf.append("text{text=\"'Darkness ascended on the lands as Zampooklidin declared war on Ceyer, and soon Brightberrys people craved the Key as well for their loss.'\"}text{text=\"\"}");
        buf.append("text{text=\"'Ceyer was too proud to disregard his cowardness', the " + ConchQuestion.addSpiritVoiceType(responder) + " declares.\"}text{text=\"\"}");
        buf.append("text{text=\"'He decided that someone else was worthy of the Key, but not even Zampooklidin.\"}text{text=\"\"}");
        buf.append("text{text=\"As he hid the key, he put a magical enchantment on the container which now only the most worthy of persons can open.\"}text{text=\"\"}");
        buf.append("text{text=\"You are soon that person.\"}text{text=\"\"}");
        if (responder.hasAbility(Abilities.getAbilityForItem(templateId, responder))) {
            buf.append("text{text=\"You are a " + Abilities.getAbilityString(Abilities.getAbilityForItem(templateId, responder)) + " already indeed. We would ask two things more from you before we show where to find the container.'\"}text{text=\"\"}");
        } else {
            buf.append("text{text=\"After you become " + Abilities.getAbilityString(Abilities.getAbilityForItem(templateId, responder)) + " we will ask two small matters more before we show you the way to the container'.\"}text{text=\"\"}");
        }
        return true;
    }

    public final void sendAfterFifthBml(Creature responder, StringBuilder buf, Item conch) {
        String tome = "tome";
        try {
            ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(conch.getData1());
            tome = template.getName();
        }
        catch (Exception template) {
            // empty catch block
        }
        buf.append("text{text=\"'You should look for a " + tome + ".\"}text{text=\"\"}");
        Item item = this.getItemToSend(conch.getData1());
        if (item == null) {
            buf.append("text{text=\"'We could not locate that item. It seems you have to do missions for the gods and hope for the best.'\"}text{text=\"\"}");
        } else {
            this.sendSignalToItemTemplate(item);
            buf.append("text{text=\"'This spirit light will show you to the last thing you need to find.', says the " + ConchQuestion.addSpiritVoiceType(responder) + ".\"}text{text=\"\"}");
        }
        buf.append("text{text=\"'Godspeed.'\"}text{text=\"\"}");
        buf.append("text{text=\"The voice grows silent.\"}text{text=\"\"}");
    }

    private static final boolean sendSixthBml(Creature responder, StringBuilder buf, int achievementId, Item conch) {
        conch.setData1(achievementId);
        conch.setAuxData((byte)10);
        buf.append("text{text=\"'Before we show you the hidden entrance, we will ask you to show your worth in the Hunt Of The Ancients.'\"}text{text=\"\"}");
        buf.append("text{text=\"'It is a test we do to honour Ceyer since he himself understood his cowardness', the " + ConchQuestion.addSpiritVoiceType(responder) + " explains.\"}text{text=\"\"}");
        buf.append("text{text=\"'All we ask is that you conquer a pillar in the Hunt.\"}text{text=\"\"}");
        Achievements ach = Achievements.getAchievementObject(responder.getWurmId());
        if (ach.getAchievement(achievementId) != null) {
            buf.append("text{text=\"You have already done that, so let us move on to the next thing.'\"}text{text=\"\"}");
            conch.setAuxData((byte)12);
        } else {
            buf.append("text{text=\"You can find out when the next Hunt begins at a settlement token.\"}text{text=\"\"}");
            buf.append("text{text=\"After you conquer the pillar, there is only one small matter left.'\"}text{text=\"\"}");
        }
        return true;
    }

    public static final void sendAfterSixthBml(Creature responder, StringBuilder buf, Item conch) {
        ConchQuestion.sendSignalToHota(conch, responder);
        buf.append("text{text=\"There should be a light guiding your way', the " + ConchQuestion.addSpiritVoiceType(responder) + " says. 'Let us show you the way to the area where the Hunt Of The Ancients take place.'\"}text{text=\"\"}");
        buf.append("text{text=\"'Good Hunting!'\"}text{text=\"\"}");
        buf.append("text{text=\"The voice grows silent.\"}text{text=\"\"}");
    }

    private static final boolean sendSeventhBml(Creature responder, StringBuilder buf, int achievementId, Item conch) {
        conch.setData1(achievementId);
        conch.setAuxData((byte)12);
        buf.append("text{text=\"'The hidden entrance to the place where the container with Ceyer's Key lies hidden is one task away now', concludes " + ConchQuestion.addSpiritVoiceType(responder) + ".\"}text{text=\"\"}");
        buf.append("text{text=\"'If you have not already tried to help the deities out by doing a mission for them, now is the time.\"}text{text=\"\"}");
        buf.append("text{text=\"If you are to ascend to Valrei, you must be aware of how those missions will effect you up there. We have noticed you have a Valrei map available.\"}text{text=\"\"}");
        buf.append("text{text=\"By doing those missions, the Deity you help will move quicker on the map and achieve its goals faster.\"}text{text=\"\"}");
        buf.append("text{text=\"If a Deity achieves those goals, their helpers will usually receive rewards.\"}text{text=\"\"}");
        Achievements ach = Achievements.getAchievementObject(responder.getWurmId());
        if (ach.getAchievement(achievementId) != null) {
            buf.append("text{text=\"You have already done that, which pleases us greatly.'\"}text{text=\"\"}");
            conch.setAuxData((byte)14);
        } else {
            buf.append("text{text=\"Once you have helped in a mission of your choice, we will show you the entrance.'\"}text{text=\"\"}");
        }
        return true;
    }

    public static final void sendAfterSeventhBml(Creature responder, StringBuilder buf, Item conch) {
        buf.append("text{text=\"There will be no light this time', says the " + ConchQuestion.addSpiritVoiceType(responder) + ". 'We will save our energy for the final push.'\"}text{text=\"\"}");
        buf.append("text{text=\"'Best of luck.'\"}text{text=\"\"}");
        buf.append("text{text=\"The voice grows silent.\"}text{text=\"\"}");
    }

    public static final boolean isThisAdventureServer() {
        try {
            Item i = Items.getItem(5390755858690L);
            if (i.getTemplateId() == 664) {
                return true;
            }
        }
        catch (NoSuchItemException noSuchItemException) {
            // empty catch block
        }
        return false;
    }

    public static final String addSpiritVoiceType(Creature responder) {
        int spirit = Zones.getSpiritsForTile(responder.getTileX(), responder.getTileY(), responder.isOnSurface());
        String sname = "distant voice";
        if (spirit == 4) {
            sname = "echoing voice";
        }
        if (spirit == 2) {
            sname = "voice with undertones of running water";
        }
        if (spirit == 3) {
            sname = "metallic voice";
        }
        if (spirit == 1) {
            sname = "crackling voice";
        }
        return sname;
    }

    public static final void sendSignalToEntrance(Creature responder, Item conch) {
        if (ConchQuestion.isThisAdventureServer() && responder.isPlayer()) {
            Player presp = (Player)responder;
            int x = 822;
            int y = 493;
            presp.addItemEffect(conch.getWurmId(), 822, 493, 10.0f);
            return;
        }
        responder.getCommunicator().sendNormalServerMessage("'Oops', the shell declares. 'We couldn't find that place..'");
    }

    public static final void sendSignalToHota(Item conch, Creature responder) {
        FocusZone hota = FocusZone.getHotaZone();
        if (responder.isPlayer()) {
            Player presp = (Player)responder;
            if (hota != null) {
                int cx = hota.getStartX() + (hota.getEndX() - hota.getStartX()) / 2;
                int cy = hota.getStartY() + (hota.getEndY() - hota.getStartY()) / 2;
                float posz = 10.0f;
                try {
                    posz = Zones.calculateHeight(cx * 4, cy * 4, true);
                }
                catch (NoSuchZoneException noSuchZoneException) {
                    // empty catch block
                }
                presp.addItemEffect(conch.getWurmId(), cx, cy, posz);
                return;
            }
        }
        responder.getCommunicator().sendNormalServerMessage("'Oops', the shell declares. 'We couldn't find the HOTA zone..'");
        conch.setAuxData((byte)12);
    }

    private final Item getItemToSend(int templateId) {
        LinkedList<Item> availItems = new LinkedList<Item>();
        for (Item item : Items.getAllItems()) {
            if (item.getTemplateId() != templateId) continue;
            availItems.add(item);
        }
        if (availItems.size() == 1) {
            return (Item)availItems.get(0);
        }
        if (availItems.size() > 0) {
            int num = Server.rand.nextInt(availItems.size());
            return (Item)availItems.get(num);
        }
        return null;
    }

    public final void sendSignalToItemTemplate(Item item) {
        if (this.getResponder().isPlayer() && item != null) {
            Player presp = (Player)this.getResponder();
            presp.addItemEffect(item.getWurmId(), item.getTileX(), item.getTileY(), item.getPosZ());
            return;
        }
        this.getResponder().getCommunicator().sendNormalServerMessage("'Oops', the shell declares. 'We couldn't find the item.. You have to do some deity missions..'");
    }
}

