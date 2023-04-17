/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.NewKingQuestion;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KingdomFoundationQuestion
extends Question {
    public static final int playersNeeded = 1;
    private int playersFound = 0;
    private final Set<Creature> creaturesToConvert = new HashSet<Creature>();
    private static Logger logger = Logger.getLogger(KingdomFoundationQuestion.class.getName());

    public KingdomFoundationQuestion(Creature aResponder, long aTarget) {
        super(aResponder, "Declaring independence", "Do you wish to found a new kingdom?", 89, aTarget);
    }

    public boolean checkMovingPlayers(Creature resp, int x, int y, boolean surfaced) {
        Creature[] crets;
        VolaTile t = Zones.getTileOrNull(x, y, surfaced);
        if (t != null && (crets = t.getCreatures()).length > 0) {
            for (int c = 0; c < crets.length; ++c) {
                if (crets[c].isPlayer()) {
                    if (crets[c].getPower() != 0) continue;
                    if (crets[c].isFighting()) {
                        resp.getCommunicator().sendNormalServerMessage(crets[c].getName() + " was moving or fighting. Everyone has to stand still and honor the moment.");
                        return false;
                    }
                    if (crets[c].getCitizenVillage() != null && crets[c].getCitizenVillage().getMayor().wurmId == crets[c].getWurmId() && crets[c].getWurmId() != resp.getWurmId()) {
                        resp.getCommunicator().sendNormalServerMessage(crets[c].getName() + " is the mayor of another settlement. You have to ask " + crets[c].getHimHerItString() + " to leave the area and be converted later.");
                        return false;
                    }
                    if (crets[c].isChampion()) {
                        resp.getCommunicator().sendNormalServerMessage(crets[c].getName() + " is champion of a deity. You have to ask " + crets[c].getHimHerItString() + " to leave the area and be converted later.");
                        return false;
                    }
                    if (crets[c].isPaying()) {
                        ++this.playersFound;
                    }
                    this.creaturesToConvert.add(crets[c]);
                    continue;
                }
                if (!crets[c].isSpiritGuard() && !crets[c].isKingdomGuard() && !(crets[c].getLoyalty() > 0.0f)) continue;
                this.creaturesToConvert.add(crets[c]);
            }
        }
        return true;
    }

    @Override
    public void answer(Properties aAnswers) {
        this.setAnswer(aAnswers);
        Creature resp = this.getResponder();
        if (resp.isChampion()) {
            resp.getCommunicator().sendAlertServerMessage("Champions are not able to rule kingdoms.");
            return;
        }
        if (!Kingdoms.mayCreateKingdom()) {
            resp.getCommunicator().sendAlertServerMessage("There are too many kingdoms already.");
            return;
        }
        try {
            Item declaration2 = Items.getItem(this.target);
            if (declaration2.deleted) {
                resp.getCommunicator().sendAlertServerMessage("The declaration is gone!");
                return;
            }
            if (declaration2.isTraded() || declaration2.getOwnerId() != resp.getWurmId()) {
                resp.getCommunicator().sendAlertServerMessage("The declaration is not under your control any longer!");
                return;
            }
        }
        catch (NoSuchItemException nsi) {
            resp.getCommunicator().sendAlertServerMessage("The declaration is gone!");
            return;
        }
        if (resp.getCitizenVillage() == null) {
            resp.getCommunicator().sendNormalServerMessage("You need to be mayor of a settlement.");
            return;
        }
        if (resp.getCitizenVillage() != resp.getCurrentVillage()) {
            resp.getCommunicator().sendNormalServerMessage("You need to be standing in your settlement.");
            return;
        }
        int sx = Zones.safeTileX(resp.getCitizenVillage().getStartX() - resp.getCitizenVillage().getPerimeterSize() - 5);
        int ex = Zones.safeTileX(resp.getCitizenVillage().getEndX() + resp.getCitizenVillage().getPerimeterSize() + 5);
        int sy = Zones.safeTileY(resp.getCitizenVillage().getStartY() - resp.getCitizenVillage().getPerimeterSize() - 5);
        int ey = Zones.safeTileY(resp.getCitizenVillage().getEndY() + resp.getCitizenVillage().getPerimeterSize() + 5);
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                if (!this.checkMovingPlayers(resp, x, y, true)) {
                    return;
                }
                if (this.checkMovingPlayers(resp, x, y, false)) continue;
                return;
            }
        }
        if (resp.getCitizenVillage() != null) {
            Player[] players;
            for (Player p : players = Players.getInstance().getPlayers()) {
                if (p.getCitizenVillage() != resp.getCitizenVillage()) continue;
                if (!this.creaturesToConvert.contains(p) && p.isPaying()) {
                    ++this.playersFound;
                }
                this.creaturesToConvert.add(p);
            }
        }
        if (this.playersFound < 1 && resp.getPower() < 3) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Only " + this.playersFound + " premium players were found in the village, on deed and in perimeter. You need " + 1 + ".");
            return;
        }
        boolean created = false;
        String kingdomName = "";
        String password = "";
        byte templateId = 0;
        String key = "kingdomName";
        String val = aAnswers.getProperty(key);
        if (val != null && val.length() > 0) {
            Kingdom kc;
            Kingdom k;
            if ((val = val.trim()).length() < 2) {
                this.getResponder().getCommunicator().sendNormalServerMessage("The name is too short.");
                return;
            }
            if (val.length() > 20) {
                this.getResponder().getCommunicator().sendNormalServerMessage("The name is too long.");
                return;
            }
            if (QuestionParser.containsIllegalVillageCharacters(val)) {
                this.getResponder().getCommunicator().sendNormalServerMessage("The name contains illegal characters.");
                return;
            }
            kingdomName = val;
            key = "passw";
            val = aAnswers.getProperty(key);
            if (val != null && val.length() > 0) {
                if (val.length() < 5) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("The password is too short.");
                    return;
                }
                password = val;
            }
            if ((val = aAnswers.getProperty(key = "templateid")) != null && val.length() > 0) {
                if (val.equals("0")) {
                    templateId = 1;
                } else if (val.equals("1")) {
                    templateId = 2;
                } else if (val.equals("2")) {
                    templateId = 3;
                } else {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Illegal template: " + val);
                    return;
                }
            }
            if ((k = Kingdoms.getKingdomWithName(kingdomName)) != null) {
                if (k.existsHere()) {
                    Item[] _items;
                    King existingRuler = King.getKing(k.getId());
                    if (existingRuler != null) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("A kingdom with that name already exists in these lands ruled by " + existingRuler.kingName + ".");
                        return;
                    }
                    boolean crownExists = false;
                    for (Item lItem : _items = Items.getAllItems()) {
                        if (!lItem.isRoyal() || lItem.getKingdom() != k.getId() || lItem.getTemplateId() != 536 && lItem.getTemplateId() != 530 && lItem.getTemplateId() != 533) continue;
                        crownExists = true;
                    }
                    if (crownExists) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("A kingdom with that name already exists in these lands. You need to find the crown.");
                        return;
                    }
                }
                if (!k.getPassword().equals(password)) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("The password you provided was wrong.");
                    return;
                }
                if (templateId != k.getTemplate()) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("You can not use that template for this kingdom since it already exists. Change template");
                    return;
                }
            }
            String mottoOne = "Friendly";
            String mottoTwo = "Peasants";
            val = aAnswers.getProperty("mottoone");
            if (val != null && val.length() > 0) {
                if (LoginHandler.containsIllegalCharacters(val = val.trim())) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("The first motto contains illegal characters.");
                    return;
                }
                mottoOne = val;
            }
            if ((val = aAnswers.getProperty("mottotwo")) != null && val.length() > 0) {
                if (LoginHandler.containsIllegalCharacters(val = val.trim())) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("The second motto contains illegal characters.");
                    return;
                }
                mottoTwo = val;
            }
            boolean allowPortal = true;
            val = aAnswers.getProperty("allowPortal");
            allowPortal = val != null && val.equals("true");
            String chatName = kingdomName.substring(0, Math.min(11, kingdomName.length()));
            if (Kingdoms.getKingdomWithChatTitle(chatName) != null && chatName.contains(" ") && (kc = Kingdoms.getKingdomWithChatTitle(chatName = kingdomName.replace(" ", "").substring(0, Math.min(11, kingdomName.length())))) != null) {
                this.getResponder().getCommunicator().sendNormalServerMessage("That name is too similar to the kingdom " + kc.getName() + ".");
                return;
            }
            String suffix = kingdomName.replace(" ", "").substring(0, Math.min(4, kingdomName.length())) + ".";
            if (Kingdoms.getKingdomWithSuffix(suffix = suffix.toLowerCase()) != null) {
                suffix = (Server.rand.nextBoolean() ? "z" : "y") + kingdomName.substring(0, Math.min(3, kingdomName.length())) + ".";
                Kingdom kc2 = Kingdoms.getKingdomWithSuffix(suffix = suffix.toLowerCase());
                if (kc2 != null) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("That name is too similar to the kingdom " + kc2.getName() + ".");
                    return;
                }
            }
            int allnum = resp.getCitizenVillage().getAllianceNumber();
            String aname = resp.getCitizenVillage().getAllianceName();
            if (allnum > 0) {
                PvPAlliance pvpAll;
                if (allnum == resp.getCitizenVillage().getAllianceNumber()) {
                    pvpAll = PvPAlliance.getPvPAlliance(allnum);
                    for (Village v : pvpAll.getVillages()) {
                        v.setAllianceNumber(0);
                        v.broadCastAlert(aname + " alliance has been disbanded.");
                    }
                    if (pvpAll.exists()) {
                        pvpAll.delete();
                        pvpAll.sendClearAllianceAnnotations();
                        pvpAll.deleteAllianceMapAnnotations();
                    }
                } else {
                    pvpAll = PvPAlliance.getPvPAlliance(allnum);
                    resp.getCitizenVillage().broadCastAlert(resp.getCitizenVillage().getName() + " leaves the " + aname + " alliance.");
                    resp.getCitizenVillage().setAllianceNumber(0);
                    if (!pvpAll.exists()) {
                        pvpAll.delete();
                    }
                }
            }
            Kingdom newkingdom = new Kingdom(Kingdoms.getNextAvailableKingdomId(), templateId, kingdomName, password, chatName, suffix, mottoOne, mottoTwo, allowPortal);
            Kingdoms.addKingdom(newkingdom);
            LoginServerWebConnection lsw = new LoginServerWebConnection();
            lsw.setKingdomInfo(newkingdom.getId(), templateId, kingdomName, password, chatName, suffix, mottoOne, mottoTwo, allowPortal);
            lsw.kingdomExists(Servers.localServer.id, newkingdom.getId(), true);
            try {
                this.getResponder().setKingdomId(newkingdom.getId(), true);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, this.getResponder().getName() + ": " + iox.getMessage(), iox);
            }
            try {
                resp.getCitizenVillage().setKingdom(newkingdom.getId());
                resp.getCitizenVillage().setKingdomInfluence();
                for (Creature c : this.creaturesToConvert) {
                    try {
                        c.setKingdomId(newkingdom.getId(), true);
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, iox.getMessage(), iox);
                    }
                }
                this.creaturesToConvert.clear();
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
            }
            Kingdoms.convertTowersWithin(sx, sy, ex, ey, newkingdom.getId());
            created = true;
            King king = King.createKing(newkingdom.getId(), resp.getName(), resp.getWurmId(), resp.getSex());
            king.setCapital(resp.getCitizenVillage().getName(), true);
            Methods.rewardRegalia(resp);
            NewKingQuestion nk = new NewKingQuestion(resp, "New ruler!", "Congratulations!", resp.getWurmId());
            nk.sendQuestion();
            Items.destroyItem(this.target);
            try {
                Item contract = ItemFactory.createItem(299, 50.0f + Server.rand.nextFloat() * 50.0f, this.getResponder().getName());
                this.getResponder().getInventory().insertItem(contract);
            }
            catch (Exception ex2) {
                logger.log(Level.INFO, ex2.getMessage(), ex2);
            }
        }
        if (!created) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You decide to do nothing.");
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (!Servers.localServer.PVPSERVER) {
            buf.append("text{text=\"You may not use this here.\"}");
        } else if (this.getResponder().isKing()) {
            buf.append("text{text=\"What a foolish idea. You are already the king! Imagine the laughter if your loyal subjects knew!\"}");
        } else {
            if (this.getResponder().getCitizenVillage() != null) {
                Player[] players;
                for (Player p : players = Players.getInstance().getPlayers()) {
                    if (p.getCitizenVillage() != this.getResponder().getCitizenVillage()) continue;
                    p.getCommunicator().sendAlertServerMessage(this.getResponder().getName() + " may be forming a new kingdom with your village in it! Make sure you are outside of enemy areas.");
                }
            }
            buf.append("text{text=\"You are ready to declare your independence from " + Kingdoms.getNameFor(this.getResponder().getKingdomId()) + "!\"}");
            buf.append("text{text=\"\"}");
            buf.append("text{text=\"In order to succeed with this, you need to be mayor of and stand in your future capital.\"}");
            buf.append("text{text=\"Any alliances you have with other village will be disbanded.\"}");
            buf.append("text{text=\"Everyone has to stand still to honor this event.\"}");
            buf.append("text{text=\"\"}");
            buf.append("text{text=\"In case the name of the kingdom exists on other servers, you have to provide the password for that kingdom.\"}");
            buf.append("harray{label{text='Name your new kingdom: '};input{id='kingdomName'; text='';maxchars='20'}}");
            buf.append("text{text=\"\"}");
            buf.append("harray{label{text='Provide a password for multiple servers (min 6 letters): '};input{id='passw'; text='';maxchars='10'}}");
            buf.append("text{text=\"\"}");
            buf.append("text{text=\"You have to select the kingdom that will serve as your example when it comes to special titles, combat moves, creatures and deities.\"}");
            buf.append("text{text=\"If your new kingdom is dissolved for any reason, any remaining people will revert to this template kingdom.\"}");
            buf.append("text{text=\"Note that the king must stay premium at all time.\"}");
            buf.append("harray{label{text='Template kingdom: '};dropdown{id='templateid';options=\"Jenn-Kellon,Mol Rehan,Horde of the Summoned\"}}");
            buf.append("text{text=\"\"}");
            buf.append("checkbox{id='allowPortal';text='Allow people to join the kingdom via portals?';selected=\"true\"}");
            buf.append("text{text=\"Finally, provide two words that you think will describe your kingdom. You can change these later.\"}");
            buf.append("harray{input{id='mottoone'; maxchars='10'; text=\"Friendly\"}label{text=\" Description one\"}}");
            buf.append("harray{input{id='mottotwo'; maxchars='10'; text=\"Peasants\"}label{text=\" Description two\"}}");
            buf.append("text{text=\"\"}");
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(700, 530, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

