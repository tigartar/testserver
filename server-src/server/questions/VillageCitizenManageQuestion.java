/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VillageCitizenManageQuestion
extends Question
implements VillageStatus {
    private static final Logger logger = Logger.getLogger(VillageCitizenManageQuestion.class.getName());
    private boolean selecting = false;
    private String allowedLetters = "abcdefghijklmnopqrstuvwxyz";
    private final Map<Integer, Long> idMap = new HashMap<Integer, Long>();

    public VillageCitizenManageQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 8, aTarget);
    }

    public VillageCitizenManageQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, boolean aSelecting) {
        super(aResponder, aTitle, aQuestion, 8, aTarget);
        this.selecting = aSelecting;
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseVillageCitizenManageQuestion(this);
    }

    @Override
    public void sendQuestion() {
        try {
            Village village;
            if (this.target == -10L) {
                village = this.getResponder().getCitizenVillage();
                if (village == null) {
                    throw new NoSuchVillageException("You are not a citizen of any village (on this server).");
                }
            } else {
                Item deed = Items.getItem(this.target);
                int villageId = deed.getData2();
                village = Villages.getVillage(villageId);
            }
            StringBuilder buf = new StringBuilder(this.getBmlHeader());
            buf.append("text{text=\"Manage citizens. Assign them roles and titles.\"}");
            Object[] citizens = village.getCitizens();
            String ch = village.unlimitedCitizens ? ";selected='true'" : "";
            buf.append("text{type=\"bold\";text=\"Unlimited citizens:\"}");
            if (!Servers.isThisAPvpServer()) {
                buf.append("text{type=\"italic\";text=\"The maximum number of branded animals is " + village.getMaxCitizens() + "\"}");
            }
            buf.append("checkbox{id=\"unlimitC\"" + ch + ";text=\"Mark this if you want to be able to recruit more than " + village.getMaxCitizens() + " citizens.\"}text{text=\"Your upkeep costs are doubled as long as you have more than that amount of citizens.\"}");
            if (this.selecting) {
                if (citizens.length > 40) {
                    buf.append("label{text=\"Select the range of citizens to manage:\"}");
                    buf.append("dropdown{id=\"selectRange\";options=\"A-F,G-L,M-R,S-Z\"}");
                    buf.append(this.createAnswerButton2());
                    this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
                    return;
                }
                this.selecting = false;
            }
            buf.append("text{text=\"\"}");
            VillageRole[] roles = village.getRoles();
            Arrays.sort(citizens);
            buf.append("table{rows=\"" + (citizens.length - 1) + "\";cols=\"7\";");
            for (int x = 0; x < citizens.length; ++x) {
                if (this.allowedLetters.indexOf(((Citizen)citizens[x]).getName().substring(0, 1).toLowerCase()) < 0) continue;
                this.idMap.put(x, new Long(((Citizen)citizens[x]).getId()));
                VillageRole role = ((Citizen)citizens[x]).getRole();
                int roleid = role.getId();
                if (role.getStatus() != 4 && role.getStatus() != 5 && role.getStatus() != 6) {
                    int defaultrole = 0;
                    buf.append("label{text=\"" + ((Citizen)citizens[x]).getName() + "\"}label{text=\"role:\"}dropdown{id=\"" + x + "\";options=\"");
                    int added = 0;
                    for (int r = 0; r < roles.length; ++r) {
                        String name;
                        if (roles[r].getStatus() == 4 || roles[r].getStatus() == 5 || roles[r].getStatus() == 1 || roles[r].getStatus() == 6) continue;
                        if (added > 0 && r != roles.length) {
                            buf.append(",");
                        }
                        if ((name = roles[r].getName()).length() == 0) {
                            name = "[blank]";
                        }
                        buf.append(name.substring(0, Math.min(name.length(), 10)));
                        if (roleid == roles[r].getId()) {
                            defaultrole = added;
                        }
                        ++added;
                    }
                    buf.append("\";default=\"" + defaultrole + "\"}");
                    PlayerState cState = PlayerInfoFactory.getPlayerState(((Citizen)citizens[x]).getId());
                    if (cState == null) {
                        buf.append("label{text=\"\"}");
                        buf.append("label{text=\"\"}");
                        buf.append("label{text=\"\"}");
                    } else {
                        String sColour = "";
                        String sState = "";
                        long changedDate = 0L;
                        if (cState.getState() == PlayerOnlineStatus.ONLINE) {
                            sColour = "66,225,66";
                            sState = "Online";
                            changedDate = cState.getLastLogin();
                        } else {
                            sColour = "255,66,66";
                            sState = "Offline";
                            changedDate = cState.getLastLogout();
                        }
                        buf.append("label{color=\"" + sColour + "\";text=\"" + sState + "\"}");
                        buf.append("label{text=\"" + VillageCitizenManageQuestion.convertTime(changedDate) + "\"}");
                        buf.append("label{text=\"" + cState.getServerName() + "\"}");
                    }
                    if (!village.isDemocracy() && ((Citizen)citizens[x]).getId() != this.getResponder().getWurmId() && ((Citizen)citizens[x]).getRole().getStatus() != 2) {
                        buf.append("checkbox{id=\"" + x + "revoke\";selected=\"false\";text=\" Revoke citizenship \"}");
                        continue;
                    }
                    buf.append("label{text=\"\"}");
                    continue;
                }
                if (((Citizen)citizens[x]).getRole().getStatus() != 6) continue;
                Wagoner wagoner = Wagoner.getWagoner(((Citizen)citizens[x]).getId());
                buf.append("label{text=\"" + ((Citizen)citizens[x]).getName() + "\"}label{text=\"role:\"};label{text=\"" + ((Citizen)citizens[x]).getRole().getName() + "\"};label{text=\"" + wagoner.getStateName() + "\"};label{text=\"\"};label{text=\"\"};label{text=\"\"}");
            }
            buf.append("}text{text=\"\"}");
            buf.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(500, 300, true, true, buf.toString(), 200, 200, 200, this.title);
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, "Failed to locate village/homestead deed with id " + this.target, nsi);
            this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the deed item for that request. Please contact administration.");
        }
        catch (NoSuchVillageException nsv) {
            logger.log(Level.WARNING, "Failed to locate the village/homestead for the deed with id " + this.target, nsv);
            this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the village for that deed. Please contact administration.");
        }
    }

    boolean isSelecting() {
        return this.selecting;
    }

    public void setSelecting(boolean aSelecting) {
        this.selecting = aSelecting;
    }

    void setAllowedLetters(String aAllowedLetters) {
        this.allowedLetters = aAllowedLetters;
    }

    Map<Integer, Long> getIdMap() {
        return this.idMap;
    }

    private static String convertTime(long time) {
        String fd = new SimpleDateFormat("dd/MMM/yyyy HH:mm").format(new Date(time));
        return fd;
    }
}

