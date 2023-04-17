/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.WarDeclaration;
import com.wurmonline.server.zones.FocusZone;
import java.util.Arrays;
import java.util.Properties;

public final class ManageAllianceQuestion
extends Question
implements TimeConstants {
    private static final String NOCHANGE = "No change";
    private Village[] allies = null;

    public ManageAllianceQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 19, aTarget);
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseManageAllianceQuestion(this);
    }

    public final Village[] getAllies() {
        return this.allies;
    }

    @Override
    public void sendQuestion() {
        Village village = this.getResponder().getCitizenVillage();
        if (village != null) {
            this.allies = village.getAllies();
            Arrays.sort(this.allies);
            StringBuilder buf = new StringBuilder();
            buf.append(this.getBmlHeader());
            PvPAlliance pvpAll = PvPAlliance.getPvPAlliance(village.getAllianceNumber());
            if (pvpAll != null) {
                buf.append("text{text=\"You are in the " + pvpAll.getName() + ".\"}");
                if (FocusZone.getHotaZone() != null) {
                    buf.append("text{text=\"" + pvpAll.getName() + " has won the Hunt of the Ancients " + pvpAll.getNumberOfWins() + " times.\"}");
                }
                if (village.getId() == village.getAllianceNumber()) {
                    if (village.getMayor().getId() == this.getResponder().getWurmId()) {
                        buf.append("text{text=\"" + village.getName() + " is the capital in the alliance which means your diplomats are responsible for ousting other settlements. The mayor may change name, disband or set another village as the alliance capital:\"};");
                        buf.append("harray{label{text=\"Alliance name:\"};input{id=\"allName\"; text=\"" + pvpAll.getName() + "\";maxchars=\"20\"}}");
                        buf.append("harray{label{text='Alliance capital:'}dropdown{id=\"masterVill\";options=\"");
                        for (int x = 0; x < this.allies.length; ++x) {
                            buf.append(this.allies[x].getName() + ",");
                        }
                        buf.append(NOCHANGE);
                        buf.append("\";default=\"" + this.allies.length + "\"}}");
                        buf.append("harray{checkbox{text=\"Check this if you wish to disband this alliance: \";id=\"disbandAll\"; selected=\"false\"}}");
                    }
                    for (Village ally : this.allies) {
                        if (ally == village) continue;
                        buf.append("harray{label{text=\"Check to break alliance with " + ally.getName() + ":\"}checkbox{id=\"break" + ally.getId() + "\";text=' '}}");
                    }
                } else {
                    buf.append("harray{label{text=\"Check to break alliance with " + pvpAll.getName() + ":\"}checkbox{id=\"break" + pvpAll.getId() + "\";text=' '}}");
                }
                buf.append("text{type=\"bold\";text=\"Alliance message of the day:\"}");
                buf.append("input{maxchars=\"200\";id=\"motd\";text=\"" + pvpAll.getMotd() + "\"}");
            }
            if (this.allies.length == 0) {
                buf.append("text{text='You have no allies.'}");
            }
            buf.append("text{text=''}");
            buf.append("text{text=''}");
            if (village.warDeclarations != null) {
                buf.append("text{type='bold'; text='The current village war declarations:' }");
                for (WarDeclaration declaration2 : village.warDeclarations.values()) {
                    if (declaration2.declarer == village) {
                        if (Servers.isThisAChaosServer() && System.currentTimeMillis() - declaration2.time > 86400000L) {
                            declaration2.accept();
                            buf.append("harray{label{text=\"" + declaration2.receiver.getName() + " has now automatically accepted your declaration.\"}}");
                            continue;
                        }
                        buf.append("harray{label{text=\"Check to withdraw declaration to " + declaration2.receiver.getName() + ":\"}checkbox{id'decl" + declaration2.receiver.getId() + "';text=' '}}");
                        continue;
                    }
                    if (Servers.isThisAChaosServer()) {
                        if (System.currentTimeMillis() - declaration2.time < 86400000L) {
                            buf.append("harray{label{text=\"You have " + Server.getTimeFor(System.currentTimeMillis() - declaration2.time) + " until you automatically accept the declaration of war.\"}}");
                            buf.append("harray{label{text=\"Check to accept declaration from " + declaration2.declarer.getName() + ":\"}checkbox{id='recv" + declaration2.declarer.getId() + "';text=' '}}");
                            continue;
                        }
                        declaration2.accept();
                        buf.append("harray{label{text=\"" + declaration2.receiver.getName() + " has now automatically accepted the war declaration from " + declaration2.declarer.getName() + ".\"}}");
                        continue;
                    }
                    buf.append("harray{label{text=\"Check to accept declaration from " + declaration2.declarer.getName() + ":\"}checkbox{id='recv" + declaration2.declarer.getId() + "';text=' '}}");
                }
                buf.append("text{text=''}");
                buf.append("text{text=''}");
            } else if (Servers.localServer.PVPSERVER) {
                buf.append("text{text='You have no pending war declarations.'}");
            }
            Object[] enemies = village.getEnemies();
            if (enemies.length > 0) {
                buf.append("harray{text{type='bold'; text='We are at war with: '}text{text=\" ");
                Arrays.sort(enemies);
                for (int x = 0; x < enemies.length; ++x) {
                    if (x == enemies.length - 1) {
                        buf.append(((Village)enemies[x]).getName());
                        continue;
                    }
                    if (x == enemies.length - 2) {
                        buf.append(((Village)enemies[x]).getName() + " and ");
                        continue;
                    }
                    buf.append(((Village)enemies[x]).getName() + ", ");
                }
                buf.append(".\"}}");
            } else if (Servers.localServer.PVPSERVER) {
                buf.append("text{text='You are not at war with any particular settlement.'}");
            }
            buf.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
        }
    }
}

