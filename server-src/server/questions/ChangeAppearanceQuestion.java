/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.Question;
import java.io.IOException;
import java.util.Properties;

public final class ChangeAppearanceQuestion
extends Question {
    private Item mirror;
    private byte gender;

    public ChangeAppearanceQuestion(Creature aResponder, Item aItem) {
        super(aResponder, "Golden Mirror", "This mirror allows you to change your gender and alter your appearance.", 51, aResponder.getWurmId());
        this.mirror = aItem;
        this.gender = (byte)127;
    }

    private void handleGenderChange() {
        if (this.getResponder().getSex() != this.gender) {
            Player player = (Player)this.getResponder();
            try {
                player.getSaveFile().setFace(0L);
            }
            catch (IOException ex) {
                player.getCommunicator().sendAlertServerMessage("Something went wrong changing your gender. You remain as you were.", (byte)3);
                logger.warning("Error setting face for player " + player.getName() + ": " + ex.getMessage());
                return;
            }
            player.setVisible(false);
            this.getResponder().setSex(this.gender, false);
            if (player.getCurrentTile() != null) {
                player.getCurrentTile().setNewFace(player);
            }
            this.getResponder().setModelName("Human");
            player.setVisible(true);
            this.getResponder().getCommunicator().sendNewFace(this.getResponder().getWurmId(), this.getResponder().getFace());
            this.getResponder().getCommunicator().sendSafeServerMessage("You feel a strange sensation as Vynora's power alters your body. You are now " + (this.gender == 1 ? "female" : "male") + ".", (byte)2);
        } else {
            this.getResponder().getCommunicator().sendSafeServerMessage("Your gender remains the same.");
        }
        this.mirror.setAuxData((byte)1);
        this.mirror.sendUpdate();
        this.getResponder().getCommunicator().sendSafeServerMessage("The mirror's glow diminishes slightly as some of the magic is used.", (byte)2);
        this.getResponder().getCommunicator().sendCustomizeFace(this.getResponder().getFace(), this.mirror.getWurmId());
    }

    private void sendConfirmation() {
        if (this.mirror.getAuxData() == 1) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("harray{text{text=''}}text{type='bold';text='Are you sure? This mirror will not allow you to make this choice again.'}harray{text{text=''}}");
        if (this.gender == this.getResponder().getSex()) {
            buf.append("radio{group='confirm';id='yes';text='Yes, I wish to remain " + (this.gender == 1 ? "female" : "male") + "';}");
        } else {
            buf.append("radio{group='confirm';id='yes';text='Yes, I wish to become " + (this.gender == 1 ? "female" : "male") + "';}");
        }
        buf.append("radio{group='confirm';id='no';text='No, I do not wish to make this decision now.'}");
        buf.append("harray{text{text=''}}");
        buf.append(this.createAnswerButton2("Next"));
        this.getResponder().getCommunicator().sendBml(300, 250, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    @Override
    public void answer(Properties answers) {
        if (this.mirror.getOwnerId() != this.getResponder().getWurmId()) {
            this.getResponder().getCommunicator().sendAlertServerMessage("You are no longer in possession of this mirror.", (byte)3);
            return;
        }
        if (answers.getProperty("confirm", "").equals("yes")) {
            this.handleGenderChange();
        } else if (answers.getProperty("gender", "").equals("male") || answers.getProperty("gender", "").equals("female")) {
            ChangeAppearanceQuestion question = new ChangeAppearanceQuestion(this.getResponder(), this.mirror);
            question.gender = answers.getProperty("gender").equals("male") ? (byte)0 : 1;
            question.sendConfirmation();
        } else {
            this.getResponder().getCommunicator().sendSafeServerMessage("You put the mirror away, leaving your body as it was.", (byte)2);
        }
    }

    @Override
    public void sendQuestion() {
        if (this.mirror.getAuxData() == 1) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("harray{text{text=''}}text{text='Before you may change your appearance, you must choose to select a new gender or keep your current one.'}harray{text{text=''}}text{type='bold';text='What will your gender be?'}");
        buf.append(this.femaleOption());
        buf.append(this.maleOption());
        buf.append("harray{text{text=''}}");
        buf.append(this.createAnswerButton2("Next"));
        this.getResponder().getCommunicator().sendBml(300, 250, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private final String maleOption() {
        if (this.getResponder().getSex() == 0) {
            return "harray{text{text=''}radio{ group='gender'; id='male';text='Male (current)';selected='true'}}";
        }
        return "harray{text{text=''}radio{ group='gender'; id='male';text='Male'}}";
    }

    private final String femaleOption() {
        if (this.getResponder().getSex() == 1) {
            return "harray{text{text=''}radio{ group='gender'; id='female';text='Female (current)';selected='true'}}";
        }
        return "harray{text{text=''}radio{ group='gender'; id='female';text='Female'}}";
    }
}

