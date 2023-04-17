/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ReputationQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(ReputationQuestion.class.getName());
    private final Map<Long, Integer> itemMap = new HashMap<Long, Integer>();

    public ReputationQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 24, aTarget);
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseReputationQuestion(this);
    }

    public Map<Long, Integer> getItemMap() {
        return this.itemMap;
    }

    @Override
    public void sendQuestion() {
        try {
            Village village;
            int ids = 0;
            if (this.target == -10L) {
                village = this.getResponder().getCitizenVillage();
            } else {
                Item deed = Items.getItem(this.target);
                int villageId = deed.getData2();
                village = Villages.getVillage(villageId);
            }
            if (village == null) {
                this.getResponder().getCommunicator().sendNormalServerMessage("No settlement found.");
            } else {
                Reputation[] reputations = village.getReputations();
                boolean kos = village.isKosAllowed();
                StringBuilder buf = new StringBuilder(this.getBmlHeader());
                buf.append("text{type=\"bold\";text=\"Reputations for " + village.getName() + "\"}text{text=\"\"}");
                buf.append("text{text=\"Permanent means that it will not change. If set to 0 it will go away though.\"}");
                buf.append("text{text=\"Use permanent with care and normally to point out enemies, since it effectively overrides any settlement role settings.\"}");
                buf.append("text{text=\"Max is 100 and min is -100. The guards attack at -30.\"}");
                buf.append("text{text=\"\"}");
                boolean showlist = true;
                if (Features.Feature.HIGHWAYS.isEnabled() && !kos) {
                    if (village.hasHighway()) {
                        buf.append("text{color=\"155,155,50\";text=\"KOS is disabled for this settlement as there is highway in it.\"}");
                    } else {
                        buf.append("text{text=\"Note: KOS is not enabled for this settlement, to change this, use settlement settings.\"}");
                    }
                    buf.append("text{text=\"\"}");
                    boolean bl = showlist = reputations.length > 0;
                }
                if (Features.Feature.HIGHWAYS.isEnabled() && kos && reputations.length == 0) {
                    buf.append("text{color=\"155,155,50\";text=\"If you add anyone to KOS, you will not be able to add a highway in this village.\"}");
                }
                int szy = 300;
                if (showlist) {
                    buf.append("table{rows=\"" + (Math.min(100, reputations.length) + 2) + "\";cols=\"3\";");
                    buf.append("label{text=\"Creature name\"};label{text=\"Reputation\"};label{text=\"Permanent\"}");
                    if (!Features.Feature.HIGHWAYS.isEnabled() || kos) {
                        buf.append("harray{input{maxchars=\"40\";id=\"nn\"};label{text=\" \"}}");
                        buf.append("harray{input{maxchars=\"4\"; id=\"nr\";text=\"-100\"};label{text=\" \"}}");
                        buf.append("checkbox{id=\"np\";selected=\"false\";text=\" \"}");
                    }
                    szy = 400;
                    if (reputations.length > 10) {
                        szy = 500;
                    }
                    for (int x = 0; x < reputations.length; ++x) {
                        if (ids >= 100) continue;
                        long wid = reputations[x].getWurmId();
                        try {
                            buf.append("label{text=\"" + reputations[x].getNameFor() + "\"};");
                            buf.append("harray{input{maxchars=\"4\"; id=\"" + ++ids + "r\"; text=\"" + reputations[x].getValue() + "\"};label{text=\" \"}}");
                            String ch = reputations[x].isPermanent() ? "selected=\"true\";" : "";
                            buf.append("checkbox{id=\"" + ids + "p\";" + ch + "text= \" \"}");
                            this.itemMap.put(new Long(wid), ids);
                            continue;
                        }
                        catch (NoSuchPlayerException nsp) {
                            village.removeReputation(wid);
                        }
                    }
                    buf.append("}");
                    if (ids >= 99) {
                        buf.append("text{text=\"The list was truncated. Some reputations are missing.\"}");
                    }
                    buf.append("text{text=\"\"}");
                }
                buf.append(this.createAnswerButton2());
                this.getResponder().getCommunicator().sendBml(500, szy, true, true, buf.toString(), 200, 200, 200, this.title);
            }
        }
        catch (NoSuchItemException nsi) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No such item.");
            logger.log(Level.WARNING, this.getResponder().getName(), nsi);
        }
        catch (NoSuchVillageException nsp) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No such settlement.");
            logger.log(Level.WARNING, this.getResponder().getName(), nsp);
        }
    }
}

