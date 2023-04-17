/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PortalQuestion
extends Question
implements MonetaryConstants,
TimeConstants {
    private final Item portal;
    private static final Logger logger = Logger.getLogger(PortalQuestion.class.getName());
    private static final int maxItems = 200;
    private static final int standardBodyInventoryItems = 12;
    public static final int PORTAL_FREEDOM_ID = 100000;
    public static final int PORTAL_EPIC_ID = 100001;
    public static final int PORTAL_CHALLENGE_ID = 100002;
    public static final boolean allowPortalToLatestServer = true;
    private int step = 0;
    private int selectedServer = 100000;
    private byte selectedKingdom = 0;
    private int selectedSpawn = -1;
    public static boolean epicPortalsEnabled = true;
    private String cyan = "66,200,200";
    private String green = "66,225,66";
    private String orange = "255,156,66";
    private String purple = "166,166,66";
    private String red = "255,66,66";

    public PortalQuestion(Creature aResponder, String aTitle, String aQuestion, Item _portal) {
        super(aResponder, aTitle, aQuestion, 76, _portal.getWurmId());
        this.portal = _portal;
    }

    @Override
    public void answer(Properties aAnswers) {
        String val2;
        String val = aAnswers.getProperty("portalling");
        this.getResponder().sendToLoggers(" at A: " + val + " selectedServer=" + this.selectedServer);
        if (val != null && val.equals("true")) {
            if (this.portal != null) {
                byte targetKingdom = 0;
                int data1 = this.portal.getData1();
                if (this.step == 1) {
                    data1 = this.selectedServer;
                }
                this.getResponder().sendToLoggers(" at A: " + val + " selectedServer=" + data1);
                ServerEntry entry = Servers.getServerWithId(data1);
                if (entry != null) {
                    boolean newTutorial;
                    this.getResponder().sendToLoggers(" at 1: " + data1);
                    if (entry.id == Servers.loginServer.id) {
                        entry = Servers.loginServer;
                    }
                    boolean changingCluster = false;
                    boolean bl = newTutorial = this.portal.getTemplateId() == 855;
                    if (Servers.localServer.EPIC != entry.EPIC && !newTutorial) {
                        changingCluster = true;
                        if (!this.portal.isEpicPortal()) {
                            this.getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. This is not an epic portal.");
                            return;
                        }
                        if (!epicPortalsEnabled && this.getResponder().getPower() == 0) {
                            this.getResponder().getCommunicator().sendNormalServerMessage("The portal won't let you just yet.");
                            return;
                        }
                    } else if (Servers.localServer.EPIC && this.getResponder().isChampion() && !this.portal.isEpicPortal()) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. You could not use this portal since you are a champion.");
                        return;
                    }
                    if (this.getResponder().getEnemyPresense() > 0) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. You sense a disturbance.");
                        return;
                    }
                    if (this.getResponder().hasBeenAttackedWithin(300)) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. You sense a disturbance - maybe your are not calm enough yet.");
                        return;
                    }
                    if (Servers.localServer.isChallengeServer()) {
                        changingCluster = true;
                    }
                    if (Servers.localServer.entryServer) {
                        changingCluster = false;
                    }
                    if (changingCluster && this.getResponder().isChampion() && !Servers.localServer.EPIC && !this.portal.isEpicPortal()) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. You could not use this portal since you are a champion.");
                        return;
                    }
                    if (this.getResponder().getPower() == 0 && entry.entryServer && !Servers.localServer.testServer) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Nothing happens.");
                        return;
                    }
                    if (this.portal.isEpicPortal()) {
                        if (!changingCluster && !Servers.localServer.entryServer) {
                            this.getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. Actually this shouldn't be possible.");
                            return;
                        }
                        long time = System.currentTimeMillis() - ((Player)this.getResponder()).getSaveFile().lastUsedEpicPortal;
                        if (this.getResponder().getEpicServerKingdom() == 0) {
                            String kingdomid = "kingdid";
                            String kval = aAnswers.getProperty("kingdid");
                            if (kval != null) {
                                try {
                                    targetKingdom = Byte.parseByte(kval);
                                }
                                catch (NumberFormatException nfe) {
                                    logger.log(Level.WARNING, "Failed to parse " + kval + " to a valid byte.");
                                    this.getResponder().getCommunicator().sendAlertServerMessage("An error occured with the target kingdom. You can't select that kingdom.");
                                    return;
                                }
                            }
                        } else {
                            targetKingdom = this.getResponder().getEpicServerKingdom();
                            if (Servers.isThisAChaosServer()) {
                                logger.log(Level.INFO, this.getResponder().getName() + " joining " + targetKingdom);
                            }
                        }
                        ((Player)this.getResponder()).getSaveFile().setEpicLocation(targetKingdom, entry.id);
                        this.getResponder().setRotation(270.0f);
                        int targetTileX = entry.SPAWNPOINTJENNX;
                        int targetTileY = entry.SPAWNPOINTJENNY;
                        if (targetKingdom == 2) {
                            this.getResponder().setRotation(90.0f);
                            targetTileX = entry.SPAWNPOINTMOLX;
                            targetTileY = entry.SPAWNPOINTMOLY;
                        } else if (targetKingdom == 3) {
                            this.getResponder().setRotation(1.0f);
                            targetTileX = entry.SPAWNPOINTLIBX;
                            targetTileY = entry.SPAWNPOINTLIBY;
                        }
                        if (Servers.localServer.entryServer && this.getResponder().isPlayer()) {
                            ((Player)this.getResponder()).addTitle(Titles.Title.Educated);
                        }
                        if (this.getResponder().isPlayer()) {
                            ((Player)this.getResponder()).getSaveFile().setBed(this.portal.getWurmId());
                        }
                        this.getResponder().sendTransfer(Server.getInstance(), entry.INTRASERVERADDRESS, Integer.parseInt(entry.INTRASERVERPORT), entry.INTRASERVERPASSWORD, entry.id, targetTileX, targetTileY, true, this.getResponder().getPower() <= 0, targetKingdom);
                        return;
                    }
                    if (entry.HOMESERVER) {
                        targetKingdom = entry.KINGDOM != 0 ? entry.KINGDOM : (this.selectedKingdom == 0 ? this.getResponder().getKingdomId() : this.selectedKingdom);
                    } else {
                        String kingdomid = "kingdid";
                        String kval = aAnswers.getProperty("kingdid");
                        if (kval != null) {
                            try {
                                targetKingdom = Byte.parseByte(kval);
                                this.getResponder().sendToLoggers(" at kingdid: " + entry.getName() + " selected kingdom " + targetKingdom);
                            }
                            catch (NumberFormatException nfe) {
                                targetKingdom = this.getResponder().getKingdomId();
                            }
                        } else {
                            targetKingdom = this.selectedKingdom == 0 ? this.getResponder().getKingdomId() : this.selectedKingdom;
                        }
                    }
                    this.getResponder().sendToLoggers(" at 1: " + entry.getName() + " target kingdom " + targetKingdom);
                    if (entry.isAvailable(this.getResponder().getPower(), this.getResponder().isReallyPaying())) {
                        if (!entry.ISPAYMENT || this.getResponder().isReallyPaying()) {
                            int numitems = 0;
                            int stayBehind = 0;
                            Item[] inventoryItems = this.getResponder().getInventory().getAllItems(true);
                            for (int x = 0; x < inventoryItems.length; ++x) {
                                if (inventoryItems[x].willLeaveServer(true, changingCluster, this.getResponder().getPower() > 0)) continue;
                                ++stayBehind;
                                this.getResponder().getCommunicator().sendNormalServerMessage("The " + inventoryItems[x].getName() + " stays behind.");
                            }
                            Item[] bodyItems = this.getResponder().getBody().getAllItems();
                            for (int x = 0; x < bodyItems.length; ++x) {
                                if (bodyItems[x].willLeaveServer(true, changingCluster, this.getResponder().getPower() > 0)) continue;
                                ++stayBehind;
                                this.getResponder().getCommunicator().sendNormalServerMessage("The " + bodyItems[x].getName() + " stays behind.");
                            }
                            if (this.getResponder().getPower() == 0) {
                                numitems = inventoryItems.length + bodyItems.length - stayBehind - 12;
                            }
                            if (numitems < 200) {
                                this.getResponder().getCommunicator().sendNormalServerMessage("You step through the portal. Will you ever return?");
                                if (this.getResponder().getPower() == 0 && changingCluster) {
                                    try {
                                        this.getResponder().setLastKingdom();
                                        this.getResponder().getStatus().setKingdom(targetKingdom);
                                    }
                                    catch (IOException iox) {
                                        this.getResponder().getCommunicator().sendNormalServerMessage("A sudden strong wind blows through the portal, throwing you back!");
                                        logger.log(Level.WARNING, iox.getMessage(), iox);
                                        return;
                                    }
                                }
                                if (changingCluster) {
                                    if (this.getResponder().getPower() <= 0) {
                                        double newskill;
                                        double x;
                                        try {
                                            Skill fs = this.getResponder().getSkills().getSkill(1023);
                                            if (fs.getKnowledge() > 50.0) {
                                                x = 100.0 - fs.getKnowledge();
                                                x -= x * 0.95;
                                                newskill = fs.getKnowledge() - x;
                                                fs.setKnowledge(newskill, false);
                                                this.getResponder().getCommunicator().sendAlertServerMessage("Your group fighting skill has been set to " + fs.getKnowledge(0.0) + "!");
                                            }
                                        }
                                        catch (NoSuchSkillException fs) {
                                            // empty catch block
                                        }
                                        try {
                                            Skill as = this.getResponder().getSkills().getSkill(1030);
                                            if (as.getKnowledge() > 50.0) {
                                                x = 100.0 - as.getKnowledge();
                                                x -= x * 0.95;
                                                newskill = as.getKnowledge() - x;
                                                as.setKnowledge(newskill, false);
                                                this.getResponder().getCommunicator().sendAlertServerMessage("Your archery skill has been set to " + as.getKnowledge(0.0) + "!");
                                            }
                                        }
                                        catch (NoSuchSkillException as) {
                                            // empty catch block
                                        }
                                    }
                                    this.getResponder().setLastChangedCluster();
                                }
                                int targetTileX = entry.SPAWNPOINTJENNX;
                                int targetTileY = entry.SPAWNPOINTJENNY;
                                if (targetKingdom == 2) {
                                    targetTileX = entry.SPAWNPOINTMOLX;
                                    targetTileY = entry.SPAWNPOINTMOLY;
                                } else if (targetKingdom == 3) {
                                    targetTileX = entry.SPAWNPOINTLIBX;
                                    targetTileY = entry.SPAWNPOINTLIBY;
                                }
                                this.getResponder().sendToLoggers("Before spawnpoints: " + this.selectedSpawn + ", server=" + this.selectedServer + ",kingdom=" + this.selectedKingdom + " entry name=" + entry.getName());
                                Spawnpoint[] spawns = entry.getSpawns();
                                if (spawns != null) {
                                    String kval = aAnswers.getProperty("spawnpoint");
                                    this.getResponder().sendToLoggers("Inside spawns. Length is " + spawns.length + " kval=" + kval);
                                    byte spnum = -1;
                                    if (kval != null) {
                                        kval = kval.replace("spawn", "");
                                        try {
                                            spnum = Integer.parseInt(kval);
                                        }
                                        catch (NumberFormatException nfe) {
                                            spnum = this.selectedSpawn;
                                        }
                                    } else {
                                        spnum = this.selectedSpawn;
                                    }
                                    this.getResponder().sendToLoggers("Before loop. " + spnum);
                                    for (Spawnpoint sp : spawns) {
                                        if (!entry.HOMESERVER && spnum < 0 && sp.kingdom == targetKingdom) {
                                            this.selectedSpawn = sp.number;
                                            this.getResponder().sendToLoggers("Inside spawnpoints. Just selected " + this.selectedSpawn + " AT RANDOM, server=" + this.selectedServer + ",kingdom=" + this.selectedKingdom);
                                            targetTileX = sp.tilex - 2 + Server.rand.nextInt(5);
                                            targetTileY = sp.tiley - 2 + Server.rand.nextInt(5);
                                            break;
                                        }
                                        if (sp.number == this.selectedSpawn) {
                                            this.getResponder().sendToLoggers("Using selected spawn " + this.selectedSpawn);
                                            targetTileX = sp.tilex - 2 + Server.rand.nextInt(5);
                                            targetTileY = sp.tiley - 2 + Server.rand.nextInt(5);
                                            break;
                                        }
                                        if (spnum != sp.number) continue;
                                        this.selectedSpawn = sp.number;
                                        this.getResponder().sendToLoggers("Inside spawnpoints. Just selected " + this.selectedSpawn + ", server=" + this.selectedServer + ",kingdom=" + this.selectedKingdom);
                                        if (this.getResponder().getPower() <= 0 && targetKingdom == 0) {
                                            targetKingdom = sp.kingdom;
                                        }
                                        targetTileX = sp.tilex - 2 + Server.rand.nextInt(5);
                                        targetTileY = sp.tiley - 2 + Server.rand.nextInt(5);
                                        break;
                                    }
                                }
                                this.getResponder().sendToLoggers(" at 4: " + entry.getName() + " target kingdom " + targetKingdom + "tx=" + targetTileX + ", ty=" + targetTileY);
                                if (Servers.localServer.entryServer) {
                                    this.getResponder().setRotation(270.0f);
                                    if (this.getResponder().isPlayer()) {
                                        ((Player)this.getResponder()).addTitle(Titles.Title.Educated);
                                    }
                                }
                                if (newTutorial) {
                                    this.getResponder().setFlag(76, false);
                                }
                                this.getResponder().sendTransfer(Server.getInstance(), entry.INTRASERVERADDRESS, Integer.parseInt(entry.INTRASERVERPORT), entry.INTRASERVERPASSWORD, entry.id, targetTileX, targetTileY, true, entry.isChallengeServer(), targetKingdom);
                            } else {
                                this.getResponder().getCommunicator().sendNormalServerMessage("The portal does not work. You are probably carrying too much. Try 200 items on body and in inventory.");
                            }
                        } else {
                            this.getResponder().getCommunicator().sendNormalServerMessage("Alas! A trifle stops you from entering the portal. You need to purchase some nice premium time in order to enter the portal.");
                        }
                    } else if (entry.maintaining) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("The portal is shut but a flicker indicates that it may open soon. You may try later.");
                    } else if (entry.isFull()) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("The portal is shut. " + entry.currentPlayers + " people are on the other side of the portal but only " + entry.pLimit + " are allowed. Please note that we are adding new servers as soon as possible when all available servers are full.");
                    } else {
                        this.getResponder().getCommunicator().sendNormalServerMessage("The portal is shut. The lands beyond are not available at the moment.");
                    }
                } else {
                    this.getResponder().getCommunicator().sendNormalServerMessage("The portal is shut. No matter what you try nothing happens.");
                }
            } else {
                this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to step through the portal.");
            }
        } else if (this.step == 1 && ((val2 = aAnswers.getProperty("sid")) != null || val == null)) {
            try {
                String kval;
                Spawnpoint[] spawns;
                ServerEntry entry;
                int spnum = this.selectedSpawn;
                this.getResponder().sendToLoggers("At 1: " + this.selectedSpawn + ", server=" + this.selectedServer + ", val2=" + val2 + " kingdom=" + this.selectedKingdom);
                if (val2 != null) {
                    this.selectedServer = Integer.parseInt(val2);
                    this.getResponder().sendToLoggers("At 2: val 2 is not null server=" + this.selectedServer + ", val2=" + val2);
                }
                if ((entry = Servers.getServerWithId(this.selectedServer)) != null && (spawns = entry.getSpawns()) != null) {
                    this.getResponder().sendToLoggers("At 2.5: server=" + this.selectedServer + " spawn " + spnum);
                    kval = aAnswers.getProperty("spawnpoint");
                    if (kval != null) {
                        this.getResponder().sendToLoggers("At 2.6: server=" + this.selectedServer + " spawn kval " + kval);
                        kval = kval.replace("spawn", "");
                        try {
                            spnum = Integer.parseInt(kval);
                            this.getResponder().sendToLoggers("At 2.7: server=" + this.selectedServer + " spawn spnum " + spnum);
                            for (Spawnpoint sp : spawns) {
                                if (sp.number != spnum) continue;
                                this.getResponder().sendToLoggers("At 2.8: spawn " + sp.name);
                                this.selectedKingdom = sp.kingdom;
                                break;
                            }
                        }
                        catch (NumberFormatException numitems) {
                            // empty catch block
                        }
                    }
                }
                String kingdomid = "kingdid";
                kval = aAnswers.getProperty("kingdid");
                if (kval != null) {
                    try {
                        this.selectedKingdom = Byte.parseByte(kval);
                        this.getResponder().sendToLoggers("At 3: " + spnum + ", server=" + this.selectedServer + ", val2=" + val2 + " selected kingdom=" + this.selectedKingdom);
                    }
                    catch (NumberFormatException nfe) {
                        this.selectedKingdom = this.getResponder().getKingdomId();
                    }
                }
                PortalQuestion pq = new PortalQuestion(this.getResponder(), "Entering portal", "Go ahead!", this.portal);
                pq.step = 1;
                pq.selectedServer = this.selectedServer;
                pq.selectedSpawn = spnum;
                pq.selectedKingdom = this.selectedKingdom;
                pq.sendQuestion();
            }
            catch (NumberFormatException nfe) {
                logger.log(Level.WARNING, nfe.getMessage() + ": " + val2);
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.portal != null) {
            ServerEntry entry;
            byte targetKingdom = this.selectedKingdom;
            int data1 = this.portal.getData1();
            int epicServerId = this.getResponder().getEpicServerId();
            if (this.step == 1) {
                data1 = this.selectedServer;
            } else if (this.portal.isEpicPortal()) {
                if (epicServerId > 0 && epicServerId != Servers.localServer.id) {
                    data1 = epicServerId;
                    entry = Servers.getServerWithId(data1);
                    if (entry != null && entry.EPIC == Servers.localServer.EPIC) {
                        data1 = 100001;
                    }
                } else {
                    data1 = 100001;
                }
            }
            entry = Servers.getServerWithId(data1);
            if (entry != null) {
                if (entry.id == Servers.loginServer.id) {
                    entry = Servers.loginServer;
                }
                if (this.getResponder().getPower() == 0 && !Servers.isThisATestServer() && (entry.entryServer || Servers.localServer.isChallengeServer() || entry.isChallengeServer() && !Servers.localServer.entryServer)) {
                    buf.append("text{type='bold';text=\"The portal looks dormant.\"};");
                } else if (this.portal.isEpicPortal()) {
                    if (epicServerId == entry.id) {
                        if (entry.isAvailable(this.getResponder().getPower(), this.getResponder().isReallyPaying())) {
                            this.step = 1;
                            this.selectedServer = entry.id;
                            if (entry.EPIC) {
                                buf.append("text{text=\"This portal leads to the Epic server " + entry.name + " where you last left it.\"}");
                            } else if (entry.isChallengeServer()) {
                                buf.append("text{text=\"This portal leads to the Challenge server '" + entry.name + "'.\"}");
                            } else if (entry.PVPSERVER) {
                                buf.append("text{text=\"This portal leads back to the Wild server " + entry.name + " where you last left it.\"}");
                            } else {
                                buf.append("text{text=\"This portal leads to back the Freedom server " + entry.name + " where you last left it.\"}");
                            }
                        } else {
                            buf.append("text{text=\"The " + entry.name + " server is currently unavailable to you.\"}");
                        }
                    } else if (entry.isAvailable(this.getResponder().getPower(), this.getResponder().isReallyPaying())) {
                        if (entry.EPIC) {
                            buf.append("text{text=\"This portal leads to the Epic server " + entry.name + ". Please select a kingdom to join:\"}");
                            PortalQuestion.addKingdoms(entry, buf);
                        } else if (entry.PVPSERVER) {
                            buf.append("text{text=\"This portal leads to the Wild server " + entry.name + ". Please select a kingdom to join:\"}");
                            PortalQuestion.addKingdoms(entry, buf);
                        } else {
                            buf.append("text{text=\"This portal leads to the Freedom server " + entry.name + ". You will join:\"}");
                            PortalQuestion.addKingdoms(entry, buf);
                        }
                    } else {
                        buf.append("text{text=\"The " + entry.name + " server is currently unavailable to you.\"}");
                    }
                    if (!entry.ISPAYMENT || this.getResponder().isReallyPaying()) {
                        if (Servers.localServer.entryServer && this.getResponder().getPower() == 0) {
                            buf.append("text{text=\"Do you wish to enter this portal never to return?\"};");
                        } else {
                            buf.append("text{text=\"Do you wish to enter this portal?\"};");
                        }
                        buf.append("radio{ group='portalling'; id='true';text='Yes'}");
                        buf.append("radio{ group='portalling'; id='false';text='No';selected='true'}");
                    } else {
                        buf.append("text{text=\"Alas! A trifle stops you from entering the portal. You need to purchase some nice premium time in order to enter the portal.\"}");
                    }
                } else {
                    if (!entry.PVPSERVER) {
                        buf.append("text{text='This portal leads to the safe lands of " + Kingdoms.getNameFor(entry.KINGDOM) + ".'}");
                        if (!entry.PVPSERVER && this.getResponder().getDeity() != null && this.getResponder().getDeity().number == 4) {
                            buf.append("text{text=\"You will lose connection with " + this.getResponder().getDeity().name + " if you enter the portal.\"}");
                        }
                        targetKingdom = entry.KINGDOM != 0 ? entry.KINGDOM : this.getResponder().getKingdomId();
                    } else if (entry.KINGDOM != 0 && this.getResponder().getPower() == 0 && Servers.localServer.entryServer && targetKingdom == 0) {
                        targetKingdom = entry.KINGDOM;
                    } else if (targetKingdom == 0) {
                        this.getResponder().sendToLoggers("Not setting kingdom at 12");
                        targetKingdom = this.getResponder().getKingdomId();
                    } else {
                        this.getResponder().sendToLoggers("Keeping kingdom at 12:" + targetKingdom);
                    }
                    if (entry.isAvailable(this.getResponder().getPower(), this.getResponder().isReallyPaying())) {
                        boolean changingCluster = false;
                        boolean changingEpicCluster = false;
                        if (Servers.localServer.PVPSERVER != entry.PVPSERVER) {
                            changingCluster = true;
                        } else if (Servers.localServer.EPIC != entry.EPIC) {
                            changingCluster = true;
                            changingEpicCluster = true;
                            buf.append("text{text=\"You will not be able to use this portal. You must use an Epic Portal which you can build yourself using stones and logs.\"};");
                        } else if (targetKingdom == 3) {
                            buf.append("text{text=\"The portal comes to life! You may pass to " + Kingdoms.getNameFor((byte)3) + "!\"}");
                        }
                        if (Servers.localServer.entryServer) {
                            changingCluster = false;
                        }
                        if (changingCluster && !changingEpicCluster) {
                            if (this.getResponder().isChampion() && !Servers.localServer.EPIC) {
                                buf.append("text{text=\"You will not be able to use this portal since you are a champion.\"};");
                            }
                            if (this.getResponder().getLastChangedCluster() + 3600000L > System.currentTimeMillis()) {
                                buf.append("text{text=\"You will not be able to use this portal since you may only change cluster once per hour.\"};");
                            }
                            if (this.getResponder().getPower() <= 0) {
                                try {
                                    Skill fs = this.getResponder().getSkills().getSkill(1023);
                                    if (fs.getKnowledge(0.0) > 50.0) {
                                        buf.append("text{text=\"Your new group fighting skill will become " + fs.getKnowledge(0.0) * (double)0.95f + "!\"};");
                                    }
                                }
                                catch (NoSuchSkillException fs) {
                                    // empty catch block
                                }
                                try {
                                    Skill as = this.getResponder().getSkills().getSkill(1030);
                                    if (as.getKnowledge(0.0) > 50.0) {
                                        buf.append("text{text=\"Your new group archery skill will become " + as.getKnowledge(0.0) * (double)0.95f + "!\"};");
                                    }
                                }
                                catch (NoSuchSkillException as) {
                                    // empty catch block
                                }
                            }
                        }
                        int numitems = 0;
                        if (!changingEpicCluster) {
                            int stayBehind = 0;
                            Item[] inventoryItems = this.getResponder().getInventory().getAllItems(true);
                            for (int x = 0; x < inventoryItems.length; ++x) {
                                if (inventoryItems[x].willLeaveServer(false, changingCluster, this.getResponder().getPower() > 0)) continue;
                                ++stayBehind;
                                buf.append("text{text=\"The " + inventoryItems[x].getName() + " will stay behind.\"};");
                                if (!Servers.localServer.entryServer || inventoryItems[x].getTemplateId() != 166) continue;
                                buf.append("text{text=\"The structure will be destroyed.\"};");
                            }
                            Item[] bodyItems = this.getResponder().getBody().getAllItems();
                            for (int x = 0; x < bodyItems.length; ++x) {
                                if (bodyItems[x].willLeaveServer(false, changingCluster, this.getResponder().getPower() > 0)) continue;
                                ++stayBehind;
                                buf.append("text{text=\"The " + bodyItems[x].getName() + " will stay behind.\"};");
                                if (!Servers.localServer.entryServer || bodyItems[x].getTemplateId() != 166) continue;
                                buf.append("text{text=\"The structure will be destroyed.\"};");
                            }
                            if (stayBehind > 0) {
                                buf.append("text{text=\"Items that stay behind will normally be available again when you return here.\"};");
                            }
                            if (this.getResponder().getPower() == 0) {
                                numitems = inventoryItems.length + bodyItems.length - stayBehind - 12;
                            }
                        }
                        if (numitems > 200) {
                            buf.append("text{text=\"The portal seems to become unresponsive as you approach. You are carrying too much. Try removing " + (numitems - 200) + " items from body and inventory.\"};");
                        } else if (!entry.ISPAYMENT || this.getResponder().isReallyPaying()) {
                            if (Servers.localServer.entryServer && this.getResponder().getPower() == 0) {
                                buf.append("text{text=\"Do you wish to enter this portal never to return?\"};");
                            } else {
                                buf.append("text{text=\"Do you wish to enter this portal?\"};");
                            }
                            if (this.getResponder().getPower() == 0 && Servers.localServer.entryServer) {
                                buf.append("text{type='bold';text=\"Note that you will automatically convert to a " + Kingdoms.getNameFor(targetKingdom) + "!\"};");
                            }
                            buf.append("radio{ group='portalling'; id='true';text='Yes'}");
                            buf.append("radio{ group='portalling'; id='false';text='No';selected='true'}");
                        } else {
                            buf.append("text{text=\"Alas! A trifle stops you from entering the portal. You need to purchase some nice premium time in order to enter the portal.\"}");
                        }
                    } else if (entry.maintaining) {
                        buf.append("text{text=\"The portal is shut but a flicker indicates that it may open soon. You may try later.\"}");
                    } else if (entry.isFull()) {
                        buf.append("text{text=\"The portal is shut. " + entry.currentPlayers + " people are on the other side of the portal but only " + entry.pLimit + " are allowed.\"}");
                    } else {
                        buf.append("text{text=\"The portal is shut. The lands beyond are not available at the moment.\"}");
                    }
                }
            } else {
                if (data1 == 100000 || data1 == 100001 || data1 == 100002) {
                    buf.setLength(0);
                    this.sendQuestion2(data1);
                    return;
                }
                buf.append("text{text=\"The portal is shut. No matter what you try nothing happens.\"}");
            }
        } else {
            buf.append("text{text=\"The portal fades from view and becomes immaterial. No matter what you try nothing happens.\"}");
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(700, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    public final void sendQuestion2(int portalNumber) {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        this.step = 1;
        boolean selected = true;
        if (portalNumber != 100000 && portalNumber != 100001 && portalNumber != 100002) {
            this.selectedServer = portalNumber;
        }
        List<ServerEntry> entries = Servers.getServerList(portalNumber);
        if (this.portal.isEpicPortal() && !epicPortalsEnabled && this.getResponder().getPower() == 0) {
            entries.clear();
        }
        if (entries.size() == 0) {
            buf.append("text{text=\"The portal is shut. No matter what you try nothing happens.\"}");
        } else {
            Object[] entryArr = entries.toArray(new ServerEntry[entries.size()]);
            Arrays.sort(entryArr);
            for (Object sentry : entryArr) {
                if (this.getResponder().getPower() <= 0 && ((ServerEntry)sentry).entryServer && !Servers.localServer.testServer) continue;
                String desc = "";
                String colour = "";
                switch (((ServerEntry)sentry).id) {
                    case 1: {
                        desc = " - This is the tutorial server.";
                        colour = this.purple;
                        break;
                    }
                    case 3: {
                        desc = " - This is an old and large PvP server in the Freedom cluster. Custom kingdoms can be formed here.";
                        colour = this.orange;
                        break;
                    }
                    case 5: {
                        desc = " - This is the oldest large PvE server in the Freedom cluster.";
                        colour = this.green;
                        break;
                    }
                    case 6: 
                    case 7: 
                    case 8: {
                        desc = " - This is a standard sized, well developed PvE server in the Freedom cluster.";
                        colour = this.green;
                        break;
                    }
                    case 9: {
                        desc = " - This is the Jenn-Kellon Home PvP server in the Epic cluster. Home servers have large bonuses against attackers.";
                        colour = this.orange;
                        break;
                    }
                    case 10: {
                        desc = " - This is the Mol Rehan Home PvP server in the Epic cluster. Home servers have large bonuses against attackers.";
                        colour = this.orange;
                        break;
                    }
                    case 11: {
                        desc = " - This is the Horde of The Summoned Home PvP server in the Epic cluster. Home servers have large bonuses against attackers.";
                        colour = this.orange;
                        break;
                    }
                    case 12: {
                        desc = " - This is the central PvP server in the Epic cluster. This is where the kingdoms clash, and custom kingdoms are formed.";
                        colour = this.red;
                        break;
                    }
                    case 13: 
                    case 14: {
                        desc = " - This is a standard sized, fairly well developed PvE server in the Freedom cluster.";
                        colour = this.green;
                        break;
                    }
                    case 15: {
                        desc = " - The most recent Land Rush server. It is bigger than all the other servers together.";
                        colour = this.green;
                        break;
                    }
                    case 20: {
                        desc = " - This is the Challenge server. Very quick skillgain, small and compact providing lots of action. Full loot PvP with highscore lists and prizes. Resets after a while.";
                        colour = this.cyan;
                        break;
                    }
                    default: {
                        String kingdomname = Kingdoms.getNameFor(((ServerEntry)sentry).KINGDOM);
                        String pvp = " Pvp Kingdoms ";
                        String kingdoms = " (" + kingdomname + "): ";
                        if (!((ServerEntry)sentry).PVPSERVER) {
                            pvp = " Non-Pvp";
                        } else if (((ServerEntry)sentry).HOMESERVER) {
                            pvp = " Pvp Home";
                        } else {
                            kingdoms = ": ";
                        }
                        desc = " - Test Server. " + pvp + kingdoms;
                        colour = this.cyan;
                    }
                }
                if (((ServerEntry)sentry).id == Servers.localServer.id) continue;
                boolean full = ((ServerEntry)sentry).isFull();
                if (((ServerEntry)sentry).isAvailable(this.getResponder().getPower(), this.getResponder().isReallyPaying())) {
                    if (entryArr.length == 1) {
                        buf.append("harray{radio{group='sid';id='" + ((ServerEntry)sentry).id + "';selected='true'}label{color='" + colour + "';text='" + ((ServerEntry)sentry).name + desc + (full ? " (Full)" : "") + "'}}");
                        buf.append("text{text=''}");
                        buf.append("text{text='You will join the following kingdom:'}");
                        PortalQuestion.addKingdoms((ServerEntry)sentry, buf);
                    } else {
                        buf.append("harray{radio{group='sid';id='" + ((ServerEntry)sentry).id + "';selected='" + selected + "'}label{color='" + colour + "';text='" + ((ServerEntry)sentry).name + desc + (full ? " (Full)" : "") + "'}}");
                    }
                    selected = false;
                    continue;
                }
                String reason = "unavailable";
                if (full && ((ServerEntry)sentry).isConnected()) {
                    reason = "full";
                }
                if (((ServerEntry)sentry).maintaining) {
                    reason = "maintenance";
                }
                buf.append("label{color=\"" + colour + "\";text=\"    " + ((ServerEntry)sentry).name + desc + " Unavailable: " + reason + ".\"}");
            }
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(700, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private static final void addVillages(ServerEntry entry, StringBuilder buf, byte selectedKingdom) {
        Spawnpoint[] spawns = entry.getSpawns();
        if (spawns != null && spawns.length > 0) {
            buf.append("text{text=\"Also, please select a start village:\"}");
            int numSelected = Server.rand.nextInt(spawns.length);
            int curr = 0;
            for (Spawnpoint spawn : spawns) {
                if (selectedKingdom == 0 || spawn.kingdom != selectedKingdom) continue;
                buf.append("radio{group=\"spawnpoint\";id=\"spawn" + spawn.number + "\"; text=\"" + spawn.name + " (" + spawn.description + ")\";selected=\"" + (numSelected == curr++) + "\"}");
            }
        }
    }

    private static final void addKingdoms(ServerEntry entry, StringBuilder buf) {
        Set<Byte> kingdoms = entry.getExistingKingdoms();
        if (entry.HOMESERVER) {
            Kingdom kingd = Kingdoms.getKingdom(entry.KINGDOM);
            if (kingd != null) {
                buf.append("radio{group=\"kingdid\";id=\"" + entry.KINGDOM + "\"; text=\"" + kingd.getName() + "\";selected=\"" + true + "\"}");
            }
            buf.append("text{text=\"\"}");
            PortalQuestion.addVillages(entry, buf, entry.KINGDOM);
        } else if (entry.isChallengeServer()) {
            Spawnpoint[] spawns = entry.getSpawns();
            if (spawns != null && spawns.length > 0) {
                int numSelected = Server.rand.nextInt(spawns.length);
                int curr = 0;
                for (Spawnpoint spawn : spawns) {
                    Kingdom kingd = Kingdoms.getKingdom(spawn.kingdom);
                    if (kingd == null || !kingd.acceptsTransfers()) continue;
                    buf.append("radio{group=\"spawnpoint\";id=\"spawn" + spawn.number + "\"; text=\"" + spawn.name + " in " + kingd.getName() + " (" + spawn.description + ")\";selected=\"" + (numSelected == curr) + "\"}");
                    ++curr;
                }
            }
            buf.append("text{text=\"\"}");
        } else {
            boolean selected = true;
            for (Byte k : kingdoms) {
                Kingdom kingd = Kingdoms.getKingdom(k);
                if (kingd == null || !kingd.acceptsTransfers()) continue;
                buf.append("radio{group=\"kingdid\";id=\"" + k + "\"; text=\"" + kingd.getName() + " '" + kingd.getFirstMotto() + " " + kingd.getSecondMotto() + "'\";selected=\"" + selected + "\"}");
                selected = false;
            }
        }
    }
}

