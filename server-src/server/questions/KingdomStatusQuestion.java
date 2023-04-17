/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WcKingdomInfo;
import com.wurmonline.server.zones.Zones;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class KingdomStatusQuestion
extends Question
implements TimeConstants {
    private static String inOffice = "";
    private final LinkedList<Kingdom> askedAllies = new LinkedList();
    private static final DecimalFormat twoDecimals = new DecimalFormat("##0.00");
    private static Logger logger = Logger.getLogger(KingdomStatusQuestion.class.getName());
    private final LinkedList<Village> villages = new LinkedList();

    public KingdomStatusQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 65, aTarget);
    }

    public final LinkedList<Village> getVillages() {
        return this.villages;
    }

    @Override
    public void answer(Properties answers) {
        if (this.getResponder().isKing()) {
            String cap = answers.getProperty("capital");
            if (cap != null) {
                Village[] vills = Villages.getVillages();
                for (int x = 0; x < vills.length; ++x) {
                    if (!vills[x].getName().equalsIgnoreCase(cap)) continue;
                    if (vills[x].kingdom == this.getResponder().getKingdomId()) {
                        King k = King.getKing(this.getResponder().getKingdomId());
                        if (cap.equalsIgnoreCase(k.capital) || k.setCapital(cap, false)) continue;
                        this.getResponder().getCommunicator().sendNormalServerMessage("You must wait 6 hours between capital changes. Also the server must have been up that long.");
                        continue;
                    }
                    this.getResponder().getCommunicator().sendNormalServerMessage("Please set your capital to a settlement in your own kingdom.");
                }
            }
            boolean changed = false;
            Kingdom kingd = Kingdoms.getKingdom(this.getResponder().getKingdomId());
            String val = answers.getProperty("mottoone");
            if (val != null && val.length() > 0 && !val.equals(kingd.getFirstMotto())) {
                if (!QuestionParser.containsIllegalVillageCharacters(val)) {
                    kingd.setFirstMotto(val);
                    changed = true;
                } else {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Invalid characters in the motto.");
                }
            }
            if ((val = answers.getProperty("mottotwo")) != null && val.length() > 0 && !val.equals(kingd.getSecondMotto())) {
                if (!QuestionParser.containsIllegalVillageCharacters(val)) {
                    kingd.setSecondMotto(val);
                    changed = true;
                } else {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Invalid characters in the motto.");
                }
            }
            boolean allowPortal = true;
            val = answers.getProperty("allowPortal");
            allowPortal = val != null && val.equals("true");
            if (allowPortal != kingd.acceptsTransfers()) {
                kingd.setAcceptsTransfers(allowPortal);
                changed = true;
            }
            if (kingd.isCustomKingdom() && this.getResponder().isKing()) {
                String ostra = answers.getProperty("expel");
                if (ostra != null && ostra.length() > 0) {
                    kingd.expelMember(this.getResponder(), ostra);
                }
                for (Village v : this.villages) {
                    val = answers.getProperty("revRej" + v.getId());
                    if (val == null || !val.equals("true")) continue;
                    v.pmkKickDate = 0L;
                    for (Village v2 : Villages.getVillages()) {
                        if (v2.kingdom != this.getResponder().getKingdomId()) continue;
                        v2.broadCastSafe(v.getName() + " is no longer being ousted from " + kingd.getName() + ".");
                    }
                }
            }
            if (KingdomStatusQuestion.mayAlly(this.getResponder().getKingdomId())) {
                this.checkAlliances(answers, kingd);
            }
            if (changed) {
                kingd.update();
                WcKingdomInfo wck = new WcKingdomInfo(WurmId.getNextWCCommandId(), true, this.getResponder().getKingdomId());
                wck.encode();
                Servers.sendWebCommandToAllServers((short)7, wck, wck.isEpicOnly());
            }
        }
    }

    public void checkAlliances(Properties answers, Kingdom kingd) {
        String val;
        Kingdom[] kingdoms;
        for (Kingdom kingdz : kingdoms = Kingdoms.getAllKingdoms()) {
            Player otherKing2;
            King other;
            if (!kingdz.existsHere()) continue;
            val = answers.getProperty("rev" + kingdz.getId());
            if (val != null && val.equals("true") && kingdz.isAllied(kingd.getId())) {
                kingdz.setAlliance(kingd.getId(), (byte)0);
                kingd.setAlliance(kingdz.getId(), (byte)0);
                this.getResponder().getCommunicator().sendNormalServerMessage("You break the alliance with " + kingdz.getName() + ".");
                other = King.getKing(kingdz.getId());
                try {
                    otherKing2 = Players.getInstance().getPlayer(other.kingid);
                    otherKing2.getCommunicator().sendNormalServerMessage("Your alliance with " + kingd.getName() + " has ended.");
                }
                catch (NoSuchPlayerException otherKing2) {
                    // empty catch block
                }
                if (Server.rand.nextBoolean()) {
                    HistoryManager.addHistory(kingd.getName(), "is no longer allied with " + kingdz.getName());
                } else {
                    HistoryManager.addHistory(kingdz.getName(), "is no longer allied with " + kingd.getName());
                }
            }
            if ((val = answers.getProperty("acc" + kingdz.getId())) != null && val.equals("true") && kingdz.hasSentRequestingAlliance(kingd.getId())) {
                kingdz.setAlliance(kingd.getId(), (byte)1);
                kingd.setAlliance(kingdz.getId(), (byte)1);
                this.getResponder().getCommunicator().sendNormalServerMessage("You accept the request for alliance with " + kingdz.getName() + ".");
                other = King.getKing(kingdz.getId());
                try {
                    otherKing2 = Players.getInstance().getPlayer(other.kingid);
                    otherKing2.getCommunicator().sendNormalServerMessage("You are now allied to " + kingd.getName() + "!");
                }
                catch (NoSuchPlayerException otherKing3) {
                    // empty catch block
                }
                if (Server.rand.nextBoolean()) {
                    HistoryManager.addHistory(kingd.getName(), "forms alliance with " + kingdz.getName());
                } else {
                    HistoryManager.addHistory(kingdz.getName(), "forms alliance with " + kingd.getName());
                }
            }
            if ((val = answers.getProperty("rem" + kingdz.getId())) == null || !val.equals("true") || !kingd.hasSentRequestingAlliance(kingdz.getId())) continue;
            kingdz.setAlliance(kingd.getId(), (byte)0);
            kingd.setAlliance(kingdz.getId(), (byte)1);
            this.getResponder().getCommunicator().sendNormalServerMessage("You revoke your request for alliance with " + kingdz.getName() + ".");
            other = King.getKing(kingdz.getId());
            try {
                otherKing2 = Players.getInstance().getPlayer(other.kingid);
                otherKing2.getCommunicator().sendNormalServerMessage(kingd.getName() + " has withdrawn their request for an alliance.");
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
        val = answers.getProperty("askAlliance");
        if (val != null && val.length() > 0) {
            try {
                Kingdom kingdz;
                int index = Integer.parseInt(val);
                if (index > 0 && !kingd.hasSentRequestingAlliance((kingdz = this.askedAllies.get(index - 1)).getId()) && !kingdz.hasSentRequestingAlliance(kingd.getId())) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("You invite " + kingdz.getName() + " to join you in an alliance.");
                    King other = King.getKing(kingdz.getId());
                    if (other != null) {
                        try {
                            Player otherKing = Players.getInstance().getPlayer(other.kingid);
                            otherKing.getCommunicator().sendNormalServerMessage(kingd.getName() + " invites you to enter a mutual alliance.");
                        }
                        catch (NoSuchPlayerException noSuchPlayerException) {}
                    } else {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Seems " + kingdz.getName() + " has no ruler to ally with.");
                    }
                    kingd.setAlliance(kingdz.getId(), (byte)2);
                }
            }
            catch (NumberFormatException nnf) {
                logger.log(Level.WARNING, val + ": " + nnf.getMessage(), nnf);
            }
        }
    }

    public static final boolean mayAlly(byte kingdom) {
        return false;
    }

    public void append(byte kingdom, StringBuilder buf) {
        King k = King.getKing(kingdom);
        Kingdom kingd = Kingdoms.getKingdom(kingdom);
        String motone = "The " + (k == null ? "ruler" : k.getRulerTitle());
        String mottwo = "rules";
        if (kingd != null) {
            motone = kingd.getFirstMotto();
            mottwo = kingd.getSecondMotto();
        }
        buf.append("text{text=\"" + Kingdoms.getNameFor(kingdom) + " '" + motone + " " + mottwo + "' (" + twoDecimals.format(Zones.getPercentLandForKingdom(kingdom)) + "% land)\"}");
        if (k != null) {
            buf.append("text{text=\" under the rule of " + k.getRulerTitle() + " " + k.kingName + ":\"}");
            float sperc = k.getLandSuccessPercent();
            if (kingd != null && k.kingid == this.getResponder().getWurmId()) {
                long nc;
                buf.append("harray{input{id='capital'; maxchars='40'; text=\"" + k.capital + "\"}label{text=\"capital\"}}");
                if (kingd.isCustomKingdom()) {
                    buf.append("text{text=\"Password used to become ruler on other servers: " + kingd.getPassword() + "\"}");
                    buf.append("text{text=\"\"}");
                    buf.append("checkbox{id='allowPortal';text='Allow people to join the kingdom via portals?';selected=\"" + kingd.acceptsTransfers() + "\"}");
                    for (Village v : Villages.getVillages()) {
                        if (v.kingdom != k.kingdom || v.isCapital()) continue;
                        this.villages.add(v);
                    }
                    if (Players.getInstance().getPlayersFromKingdom(kingdom) > 10 || Servers.localServer.testServer) {
                        buf.append("harray{input{id='expel'; maxchars='30'; text=\"\"}label{text=\" Expel this person from the kingdom. Must be online.\"}}");
                    } else {
                        buf.append("text{text=\"Your kingdom is too small to expel people (more than 10 members required).\"}");
                    }
                    if (this.villages.size() > 0) {
                        for (Village v : this.villages) {
                            if (v.pmkKickDate <= 0L) continue;
                            buf.append("text{text=\"\"}");
                            buf.append("checkbox{id=\"revRej" + v.getId() + "\";text=\"" + v.getName() + " is being kicked out in " + Server.getTimeFor(v.pmkKickDate - System.currentTimeMillis()) + ". Remove kicking? \";selected=\"false\"};");
                        }
                    }
                }
                buf.append("harray{input{id='mottoone'; maxchars='10'; text=\"" + kingd.getFirstMotto() + "\"}label{text=\" Description one\"}}");
                buf.append("harray{input{id='mottotwo'; maxchars='10'; text=\"" + kingd.getSecondMotto() + "\"}label{text=\" Description two\"}}");
                if (sperc == 100.0f) {
                    buf.append("text{text=\"Your kingdom share lies steady at " + String.format("%.2f%%", Float.valueOf(k.currentLand)) + " of the available land.\"}");
                } else if (sperc > 100.0f) {
                    buf.append("text{text=\"You have increased your kingdom share from " + String.format("%.2f%%", Float.valueOf(k.startLand)) + " to " + String.format("%.2f%%", Float.valueOf(k.currentLand)) + " of the available land, which is an increase by " + String.format("%.2f%%", Float.valueOf(sperc - 100.0f)) + ".\"}");
                } else {
                    buf.append("text{text=\"Your share of the available lands has decreased from " + String.format("%.2f%%", Float.valueOf(k.startLand)) + " to " + String.format("%.2f%%", Float.valueOf(k.currentLand)) + " of the available land, so you have lost " + String.format("%.2f%%", Float.valueOf(100.0f - sperc)) + ".\"}");
                }
                if (Servers.localServer.isChallengeOrEpicServer() && this.getResponder().hasCustomKingdom()) {
                    if (this.getResponder().isEligibleForKingdomBonus()) {
                        buf.append("text{text=\"You are eligible for royal combat rating bonuses since you have more than 2.0% land.\"}");
                    } else {
                        buf.append("text{text=\"You are not eligible for royal combat rating bonuses since you have less than 2.0% land.\"}");
                    }
                }
                buf.append("text{text=\"You have appointed " + k.appointed + " levels of titles, orders and offices.\"}");
                buf.append("text{text=\"Your subjects have slain " + k.levelskilled + " appointment levels of enemy nobles, and your enemies have slain " + k.levelslost + " levels of your nobles, which equals " + String.format("%d%%", (int)k.getAppointedSuccessPercent()) + " success.\"}");
                buf.append("text{text=\"This ranks you as " + k.getFullTitle() + ".\"}");
                buf.append("text{text=\"\"}");
                if (k.getChallengeDate() > 0L) {
                    long nca = k.getChallengeDate();
                    String sa = Server.getTimeFor(System.currentTimeMillis() - nca);
                    buf.append("text{text=\"You were challenged " + sa + " ago.\"}");
                }
                if (k.getChallengeAcceptedDate() > 0L) {
                    long nca = k.getChallengeAcceptedDate();
                    String sa = Server.getTimeFor(nca - System.currentTimeMillis());
                    buf.append("text{text=\"You must show up in the duelling ring in " + sa + ".\"}");
                }
                if ((nc = k.getNextChallenge()) > System.currentTimeMillis()) {
                    String s = Server.getTimeFor(nc - System.currentTimeMillis());
                    buf.append("text{text=\"Next challenge avail in " + s + ".\"}");
                }
                if (k.hasFailedAllChallenges()) {
                    buf.append("text{text=\"You have failed all challenges. Voting is in progress.\"}");
                }
                if (this.getResponder().getPower() >= 3) {
                    buf.append("text{text=\" Challenges: " + k.getChallengeSize() + " Declined: " + k.getDeclinedChallengesNumber() + " Votes: " + k.getVotes() + ".\"}");
                }
                buf.append("text{text=\"\"}");
                if (KingdomStatusQuestion.mayAlly(kingdom)) {
                    Kingdom[] kingdoms;
                    boolean allAllies = true;
                    for (Kingdom kingdz : kingdoms = Kingdoms.getAllKingdoms()) {
                        if (!kingdz.existsHere() || !KingdomStatusQuestion.mayAlly(kingdz.getId())) continue;
                        if (kingdz.isAllied(kingdom)) {
                            buf.append("checkbox{id=\"rev" + kingdz.getId() + "\";text=\"" + kingdz.getName() + " is an ally. Revoke? \";selected=\"false\"};");
                            continue;
                        }
                        if (kingdz.hasSentRequestingAlliance(kingdom)) {
                            buf.append("checkbox{id=\"acc" + kingdz.getId() + "\";text=\"" + kingdz.getName() + " is asking to become an ally. Accept? \";selected=\"false\"};");
                            continue;
                        }
                        if (kingd.hasSentRequestingAlliance(kingdz.getId())) {
                            buf.append("checkbox{id=\"rem" + kingdz.getId() + "\";text=\" You are asking " + kingdz.getName() + " to become an ally. Revoke? \";selected=\"false\"};");
                            continue;
                        }
                        allAllies = false;
                    }
                    buf.append("text{text=\"\"}");
                    buf.append("text{text=\"Potential allies. Select one to invite to an alliance:\"}");
                    buf.append("dropdown{id='askAlliance';options=\"");
                    buf.append("No thanks");
                    if (!allAllies) {
                        for (Kingdom kingdz : kingdoms) {
                            if (!kingdz.existsHere() || kingdz.getId() == 0 || kingdz.isAllied(kingdom) || kingdz.getId() == kingdom || !KingdomStatusQuestion.mayAlly(kingdz.getId())) continue;
                            buf.append("," + kingdz.getName());
                            this.askedAllies.add(kingdz);
                        }
                    }
                    buf.append("\"}");
                }
            } else {
                if (kingdom == this.getResponder().getKingdomId()) {
                    long nc;
                    String sa;
                    long nca;
                    if (k.getChallengeDate() > 0L) {
                        nca = k.getChallengeDate();
                        sa = Server.getTimeFor(System.currentTimeMillis() - nca);
                        buf.append("text{text=\"The ruler was challenged " + sa + " ago.\"}");
                    }
                    if (k.getChallengeAcceptedDate() > 0L) {
                        nca = k.getChallengeAcceptedDate();
                        sa = Server.getTimeFor(nca - System.currentTimeMillis());
                        buf.append("text{text=\"The ruler must show up in " + sa + ".\"}");
                    }
                    if ((nc = k.getNextChallenge()) > System.currentTimeMillis()) {
                        String s = Server.getTimeFor(nc - System.currentTimeMillis());
                        buf.append("text{text=\"Next challenge avail in " + s + ".\"}");
                    }
                    if (k.hasFailedAllChallenges()) {
                        buf.append("text{text=\"The " + k.getRulerTitle() + " has failed all challenges. Voting is in progress.\"}");
                        if (((Player)this.getResponder()).getSaveFile().votedKing) {
                            buf.append("text{text=\"You have already voted.\"}");
                        } else {
                            buf.append("text{text=\"You may head to the duelling ring and vote for removal of the current ruler.\"}");
                        }
                    }
                    if (this.getResponder().getPower() >= 3) {
                        buf.append("text{text=\" Challenges: " + k.getChallengeSize() + " Declined: " + k.getDeclinedChallengesNumber() + " Votes: " + k.getVotes() + ".\"}");
                    }
                    buf.append("text{text=\"\"}");
                    if (Servers.localServer.isChallengeOrEpicServer() && this.getResponder().hasCustomKingdom()) {
                        if (this.getResponder().isEligibleForKingdomBonus()) {
                            buf.append("text{text=\"The " + kingd.getName() + " people are eligible for royal combat rating bonuses since you have more than " + 2.0f + "% land.\"}");
                        } else {
                            buf.append("text{text=\"The " + kingd.getName() + " people are not eligible for royal combat rating bonuses since you have less than " + 2.0f + "% land.\"}");
                        }
                    }
                }
                if (sperc == 100.0f) {
                    buf.append("text{text=\"Kingdom share lies steady at " + String.format("%.2f%%", Float.valueOf(k.currentLand)) + " of the available land.\"}");
                } else if (sperc > 100.0f) {
                    buf.append("text{text=\"" + k.kingName + " has increased the kingdom share from " + String.format("%.2f%%", Float.valueOf(k.startLand)) + " to " + String.format("%.2f%%", Float.valueOf(k.currentLand)) + " of the available land, which is an increase by " + String.format("%.2f%%", Float.valueOf(sperc - 100.0f)) + ".\"}");
                } else {
                    buf.append("text{text=\"During the reign of " + k.kingName + ", the share of the available lands has decreased from " + String.format("%.2f%%", Float.valueOf(k.startLand)) + " to " + String.format("%.2f%%", Float.valueOf(k.currentLand)) + " of the available land, so the kingdom has lost " + String.format("%.2f%%", Float.valueOf(100.0f - sperc)) + ".\"}");
                }
                buf.append("text{text=\"" + k.kingName + " has appointed " + k.appointed + " levels of titles, orders and offices.\"}");
                buf.append("text{text=\"The subjects of " + Kingdoms.getNameFor(kingdom) + " have slain " + k.levelskilled + " appointment levels of enemy nobles. Their enemies have slain " + k.levelslost + " levels of " + Kingdoms.getNameFor(kingdom) + " nobles, which equals " + String.format("%d%%", (int)k.getAppointedSuccessPercent()) + " success.\"}");
                buf.append("text{text=\"This ranks " + k.kingName + " as " + k.getFullTitle() + ".\"}");
            }
            buf.append("text{text=\"\"}");
            this.addApps(buf, kingdom, false);
        } else if (kingdom != 0) {
            buf.append("text{text=\"Ruler unknown.\"}");
        }
        float crbon = Players.getInstance().getCRBonus(kingdom);
        if (crbon > 0.0f) {
            if (crbon < 2.0f) {
                buf.append("text{text=\"Because of the low active population, the subjects are known to fight a bit more fierce than their enemies.\"}");
            } else {
                buf.append("text{text=\"Because of the low active population, the subjects are known to fight quite a bit more fierce than their enemies.\"}");
            }
        }
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"\"}");
    }

    private void addApps(StringBuilder buf, byte kingdom, boolean isResponder) {
        Appointments apps = King.getCurrentAppointments(kingdom);
        if (apps != null) {
            if (isResponder) {
                String titles = apps.getOffices(this.getResponder().getWurmId(), this.getResponder().getSex() == 0);
                if (titles.length() > 0) {
                    buf.append("text{text=\"");
                    buf.append("You are the ");
                    buf.append(titles);
                    buf.append(" of ");
                    buf.append(Kingdoms.getNameFor(this.getResponder().getKingdomId()));
                    buf.append(".");
                    buf.append("\"}");
                    buf.append("text{text=\"\"}");
                }
                if ((titles = apps.getTitles(this.getResponder().getAppointments(), this.getResponder().getSex() == 0)).length() > 0) {
                    buf.append("text{text=\"");
                    buf.append(" You are ");
                    buf.append(titles);
                    buf.append(" of ");
                    buf.append(Kingdoms.getNameFor(this.getResponder().getKingdomId()));
                    buf.append(".");
                    buf.append("\"}");
                    buf.append("text{text=\"\"}");
                }
                if ((titles = apps.getOrders(this.getResponder().getAppointments(), this.getResponder().getSex() == 0)).length() > 0) {
                    buf.append("text{text=\"");
                    buf.append("You have received the ");
                    buf.append(titles);
                    buf.append(".");
                    buf.append("\"}");
                    buf.append("text{text=\"\"}");
                }
            } else {
                boolean isSecretPolice = false;
                Appointments rap = King.getCurrentAppointments(this.getResponder().getKingdomId());
                if (rap != null) {
                    long secretP = rap.getOfficialForId(1500);
                    if (secretP == this.getResponder().getWurmId()) {
                        isSecretPolice = true;
                    }
                    long[] offices = apps.officials;
                    for (int x = 0; x < offices.length; ++x) {
                        if (offices[x] == 0L) continue;
                        inOffice = "";
                        Appointment app = apps.getAppointment(1500 + x);
                        if (app == null) continue;
                        String name = "Unknown";
                        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(offices[x]);
                        if (pinf != null) {
                            name = pinf.getName();
                        }
                        if (isSecretPolice) {
                            try {
                                Player p = Players.getInstance().getPlayer(offices[x]);
                                inOffice = "(in office)";
                            }
                            catch (NoSuchPlayerException noSuchPlayerException) {
                                // empty catch block
                            }
                        }
                        buf.append("label{text=\"" + app.getNameForGender((byte)0) + "\"};label{text=\"" + name + " " + inOffice + "\"};");
                        buf.append("text{text=\"\"}");
                    }
                }
            }
            buf.append("text{text=\"\"}");
        }
    }

    @Override
    public void sendQuestion() {
        Kingdom[] kingdoms;
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("center{image{src='img.gui.kingdoms';size='512,128'}}");
        buf.append("text{text=\"\"}");
        this.addApps(buf, this.getResponder().getKingdomId(), true);
        for (Kingdom k : kingdoms = Kingdoms.getAllKingdoms()) {
            if (!k.existsHere()) continue;
            this.append(k.getId(), buf);
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(700, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

