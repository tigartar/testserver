/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.TerraformingTask;
import com.wurmonline.server.bodys.BodyHuman;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.SynchedEpicEffect;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.meshgen.IslandAdder;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Effectuator
implements CreatureTemplateIds,
MiscConstants {
    private static final Random rand = new Random();
    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_SPEED = 1;
    public static final int EFFECT_COMBATRATING = 2;
    public static final int EFFECT_STAMINA_REGAIN = 3;
    public static final int EFFECT_FAVORGAIN = 4;
    public static final int EFFECT_SPAWN = 5;
    private static final String LOAD_KINGDOM_EFFECTS = "SELECT * FROM KINGDOMEFFECTS";
    private static final String INSERT_KINGDOM_EFFECTS = "INSERT INTO KINGDOMEFFECTS (EFFECT,KINGDOM) VALUES(?,?)";
    private static final String UPDATE_KINGDOM_EFFECTS = "UPDATE KINGDOMEFFECTS SET KINGDOM=? WHERE EFFECT=?";
    private static final String LOAD_DEITY_EFFECTS = "SELECT * FROM DEITYEFFECTS";
    private static final String INSERT_DEITY_EFFECTS = "INSERT INTO DEITYEFFECTS (EFFECT,DEITY) VALUES(?,?)";
    private static final String UPDATE_DEITY_EFFECTS = "UPDATE DEITYEFFECTS SET DEITY=? WHERE EFFECT=?";
    private static int kingdomTemplateWithSpeedBonus = 0;
    private static int kingdomTemplateWithCombatRating = 0;
    private static int kingdomTemplateWithStaminaRegain = 0;
    private static int kingdomTemplateWithFavorGain = 0;
    private static int deityWithSpeedBonus = 0;
    private static int deityWithCombatRating = 0;
    private static int deityWithStaminaRegain = 0;
    private static int deityWithFavorGain = 0;
    private static final LinkedBlockingQueue<SynchedEpicEffect> comingEvents = new LinkedBlockingQueue();
    private static final Logger logger = Logger.getLogger(Effectuator.class.getName());

    private Effectuator() {
    }

    public static String getSpiritType(int effect) {
        String toReturn;
        switch (effect) {
            case 1: {
                toReturn = "fire";
                break;
            }
            case 2: {
                toReturn = "forest";
                break;
            }
            case 3: {
                toReturn = "mountain";
                break;
            }
            case 4: {
                toReturn = "water";
                break;
            }
            default: {
                toReturn = "hidden";
            }
        }
        return toReturn;
    }

    public static final void addEpicEffect(SynchedEpicEffect effect) {
        comingEvents.add(effect);
    }

    public static final void pollEpicEffects() {
        for (SynchedEpicEffect effect : comingEvents) {
            effect.run();
        }
        comingEvents.clear();
    }

    public static void doEvent(int eventNum, long deityNumber, int creatureTemplateId, int bonusEffectNum, String eventDesc) {
        if (Servers.localServer.EPIC && !Servers.localServer.LOGINSERVER) {
            Effectuator.setEffectController(4, 0L);
            Effectuator.setEffectController(2, 0L);
            Effectuator.setEffectController(1, 0L);
            Effectuator.setEffectController(3, 0L);
            byte favoredKingdom = Deities.getFavoredKingdom((int)deityNumber);
            boolean doNegative = false;
            switch (rand.nextInt(7)) {
                case 0: {
                    Effectuator.spawnDefenders(deityNumber, creatureTemplateId);
                    break;
                }
                case 1: {
                    break;
                }
                case 2: {
                    break;
                }
                case 3: {
                    break;
                }
                case 4: {
                    break;
                }
                case 5: {
                    break;
                }
                case 6: {
                    break;
                }
            }
        }
    }

    static void doEvent1(long deityNumber) {
        if (Servers.localServer.EPIC) {
            if (deityNumber == 5L) {
                Effectuator.wurmPunish(4000, 0.0f, 20.0f, (byte)6);
            }
            Effectuator.spawnOwnCreatures(deityNumber, 38, true);
        }
    }

    static void doEvent5(long deityNumber) {
        if (Servers.localServer.EPIC && deityNumber == 5L) {
            Effectuator.wurmPunish(4000, 20.0f, 0.0f, (byte)5);
        }
    }

    static void doEvent7(long deityNumber) {
        if (Servers.localServer.EPIC) {
            if (deityNumber == 5L) {
                Effectuator.crushStructures();
            } else {
                IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
                isl.addOneIsland(Zones.worldTileSizeX, Zones.worldTileSizeY);
            }
        }
    }

    static void doEvent8(long deityNumber) {
        if (Servers.localServer.EPIC) {
            if (deityNumber == 5L) {
                Effectuator.wurmPunish(8000, 0.0f, 0.0f, (byte)9);
            } else {
                Effectuator.doEvent15(deityNumber);
            }
        }
    }

    static void terraform(int task, long deityNumber, int nums) {
        byte favoredKingdom = Deities.getFavoredKingdom((int)deityNumber);
        Deity d = Deities.getDeity((int)deityNumber);
        if (d != null) {
            new TerraformingTask(task, favoredKingdom, d.getName(), (int)deityNumber, nums, true);
        }
    }

    static void doEvent12(long deityNumber) {
        if (Servers.localServer.EPIC) {
            Effectuator.disease(deityNumber);
        }
    }

    static void doEvent14(long deityNumber) {
        if (Servers.localServer.EPIC) {
            Effectuator.slay(deityNumber);
        }
    }

    static void doEvent15(long deityNumber) {
        Effectuator.awardSkill(deityNumber, 103, 0.005f, 20.0f);
    }

    static void doEvent17(long deityNumber) {
        if (Servers.localServer.EPIC) {
            if (deityNumber == 5L) {
                Effectuator.wurmPunish(4000, 0.0f, 0.0f, (byte)9);
            } else {
                Effectuator.awardSkill(deityNumber, 105, 0.005f, 20.0f);
            }
        }
    }

    static void appointAlly(long deityNumber) {
        if (Servers.localServer.EPIC && deityNumber == 5L) {
            Effectuator.wurmPunish(14000, 20.0f, 20.0f, (byte)9);
        }
    }

    static final void promoteImmortal(long deityNumber) {
        if (!Servers.localServer.LOGINSERVER || !HexMap.VALREI.elevateDemigod(deityNumber)) {
            // empty if block
        }
    }

    static void doEvent20(long deityNumber) {
        Effectuator.punishSkill(deityNumber, 100, 0.5f);
        Effectuator.punishSkill(deityNumber, 102, 0.5f);
        Effectuator.punishSkill(deityNumber, 106, 0.5f);
        Effectuator.punishSkill(deityNumber, 104, 0.5f);
        Effectuator.punishSkill(deityNumber, 101, 0.5f);
        Effectuator.punishSkill(deityNumber, 105, 0.5f);
        Effectuator.lowerFaith(deityNumber);
        for (int x = 0; x < Math.min(20, Players.getInstance().getNumberOfPlayers()); ++x) {
            Effectuator.slay(deityNumber);
        }
    }

    static void doEvent21(long deityNumber) {
        Effectuator.punishSkill(deityNumber, 100, 0.04f);
        Effectuator.punishSkill(deityNumber, 102, 0.04f);
        Effectuator.punishSkill(deityNumber, 106, 0.04f);
        Effectuator.punishSkill(deityNumber, 104, 0.04f);
        Effectuator.punishSkill(deityNumber, 101, 0.04f);
        Effectuator.punishSkill(deityNumber, 105, 0.04f);
        Effectuator.awardSkill(deityNumber, 100, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 102, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 106, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 104, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 101, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 105, 0.005f, 20.0f);
        Effectuator.lowerFaith(deityNumber);
        for (int x = 0; x < Math.min(10, Players.getInstance().getNumberOfPlayers()); ++x) {
            Effectuator.slay(deityNumber);
        }
    }

    static void doEvent22(long deityNumber) {
        Effectuator.punishSkill(deityNumber, 100, 0.05f);
        Effectuator.punishSkill(deityNumber, 102, 0.05f);
        Effectuator.punishSkill(deityNumber, 106, 0.05f);
        Effectuator.punishSkill(deityNumber, 105, 0.05f);
        Effectuator.awardSkill(deityNumber, 100, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 102, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 106, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 104, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 101, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 105, 0.005f, 20.0f);
        Effectuator.lowerFaith(deityNumber);
        for (int x = 0; x < Math.min(10, Players.getInstance().getNumberOfPlayers()); ++x) {
            Effectuator.slay(deityNumber);
        }
    }

    static void doEvent23(long deityNumber) {
        Effectuator.punishSkill(deityNumber, 100, 0.05f);
        Effectuator.punishSkill(deityNumber, 102, 0.05f);
        Effectuator.punishSkill(deityNumber, 101, 0.05f);
        Effectuator.punishSkill(deityNumber, 105, 0.05f);
        Effectuator.awardSkill(deityNumber, 100, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 102, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 106, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 104, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 101, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 105, 0.005f, 20.0f);
        Effectuator.lowerFaith(deityNumber);
        for (int x = 0; x < Math.min(10, Players.getInstance().getNumberOfPlayers()); ++x) {
            Effectuator.slay(deityNumber);
        }
    }

    static void doEvent24(long deityNumber) {
        Effectuator.punishSkill(deityNumber, 103, 0.05f);
        Effectuator.punishSkill(deityNumber, 102, 0.05f);
        Effectuator.punishSkill(deityNumber, 101, 0.05f);
        Effectuator.punishSkill(deityNumber, 105, 0.05f);
        Effectuator.awardSkill(deityNumber, 100, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 102, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 106, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 104, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 101, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 105, 0.005f, 20.0f);
        Effectuator.lowerFaith(deityNumber);
        for (int x = 0; x < Math.min(10, Players.getInstance().getNumberOfPlayers()); ++x) {
            Effectuator.slay(deityNumber);
        }
    }

    static void doEvent25(long deityNumber) {
        Effectuator.punishSkill(deityNumber, 103, 0.05f);
        Effectuator.punishSkill(deityNumber, 102, 0.05f);
        Effectuator.punishSkill(deityNumber, 101, 0.05f);
        Effectuator.punishSkill(deityNumber, 105, 0.05f);
        Effectuator.awardSkill(deityNumber, 100, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 102, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 106, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 104, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 101, 0.005f, 20.0f);
        Effectuator.awardSkill(deityNumber, 105, 0.005f, 20.0f);
        Effectuator.lowerFaith(deityNumber);
        for (int x = 0; x < Math.min(20, Players.getInstance().getNumberOfPlayers()); ++x) {
            Effectuator.slay(deityNumber);
        }
    }

    private static void punishSkill(long deityNum, int skillNum, float toDecrease) {
    }

    private static void disease(long deityNumberSaved) {
        byte friendlyKingdom = Deities.getFavoredKingdom((int)deityNumberSaved);
        Player[] players = Players.getInstance().getPlayers();
        for (int x = 0; x < players.length; ++x) {
            if (friendlyKingdom != 0 && players[x].getKingdomTemplateId() == friendlyKingdom || players[x].getDeity() != null && (long)players[x].getDeity().getNumber() == deityNumberSaved) continue;
            players[x].getCommunicator().sendAlertServerMessage("An evil aura emanates from valrei. You suddenly feel like vomiting.");
            players[x].setDisease((byte)50);
        }
    }

    private static void awardSkill(long deityNum, int skillNum, float toIncrease, float minNumber) {
        byte friendlyKingdom = Deities.getFavoredKingdom((int)deityNum);
        Player[] players = Players.getInstance().getPlayers();
        for (int x = 0; x < players.length; ++x) {
            if (players[x].getKingdomTemplateId() != friendlyKingdom) continue;
            try {
                Skill old = players[x].getSkills().getSkill(skillNum);
                old.setKnowledge(old.getKnowledge() + (100.0 - old.getKnowledge()) * (double)toIncrease, false);
                continue;
            }
            catch (NoSuchSkillException nss) {
                players[x].getSkills().learn(skillNum, minNumber);
            }
        }
    }

    private static void slay(long deityNum) {
        Player[] players = Players.getInstance().getPlayers();
        if (deityNum == 5L) {
            boolean found = false;
            while (!found) {
                int p = rand.nextInt(players.length);
                if (!players[p].isDead() && players[p].isFullyLoaded() && players[p].getVisionArea() != null) {
                    players[p].getCommunicator().sendAlertServerMessage("You feel an abnormal wave of heat coming from Valrei! Wurm has punished you!");
                    players[p].die(false, "Valrei Lazer Beams");
                    found = true;
                }
                if (found || players.length >= 5 || !rand.nextBoolean()) continue;
                return;
            }
        } else {
            boolean found = false;
            int seeks = 0;
            byte friendlyKingdom = Deities.getFavoredKingdom((int)deityNum);
            while (!found) {
                ++seeks;
                int p = rand.nextInt(players.length);
                if (!players[p].isDead() && players[p].isFullyLoaded() && players[p].getVisionArea() != null && players[p].getKingdomTemplateId() != friendlyKingdom) {
                    if (players[p].getDeity() != null && (long)players[p].getDeity().getNumber() != deityNum) {
                        if (!(deityNum == 1L && players[p].getDeity().getNumber() == 3 || deityNum == 3L && players[p].getDeity().getNumber() == 1)) {
                            players[p].getCommunicator().sendAlertServerMessage("You suddenly feel yourself immolated in an abnormal wave of heat coming from Valrei!");
                            players[p].die(false, "Valrei Bombardment");
                            found = true;
                        }
                    } else {
                        players[p].getCommunicator().sendAlertServerMessage("You suddenly feel yourself immolated in an abnormal wave of heat coming from Valrei!");
                        players[p].die(false, "Valrei Nuclear Blast");
                        found = true;
                    }
                }
                if (found || seeks <= players.length || !rand.nextBoolean()) continue;
                return;
            }
        }
    }

    private static void lowerFaith(long deityNum) {
        PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfos();
        for (int x = 0; x < infos.length; ++x) {
            byte favoredKingdom;
            byte kingdom = Players.getInstance().getKingdomForPlayer(infos[x].wurmId);
            Kingdom k = Kingdoms.getKingdom(kingdom);
            byte kingdomTemplateId = k.getTemplate();
            if (kingdomTemplateId == (favoredKingdom = Deities.getFavoredKingdom((int)deityNum))) continue;
            try {
                if (infos[x].getFaith() > 80.0f) {
                    infos[x].setFaith(infos[x].getFaith() - 1.0f);
                } else if (infos[x].getFaith() > 50.0f) {
                    infos[x].setFaith(infos[x].getFaith() - 3.0f);
                } else if (infos[x].getFaith() > 20.0f) {
                    infos[x].setFaith(infos[x].getFaith() * 0.8f);
                }
                infos[x].setFavor(0.0f);
                continue;
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    private static void wurmPunish(int damage, float poisondam, float disease, byte woundType) {
        Player[] players = Players.getInstance().getPlayers();
        BodyHuman body = new BodyHuman();
        for (int x = 0; x < players.length; ++x) {
            try {
                CombatEngine.addWound(null, players[x], woundType, body.getRandomWoundPos(), damage, 1.0f, "hurts", null, disease, poisondam, false, false, false, false);
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private static void crushStructures() {
        Structure[] structures = Structures.getAllStructures();
        if (structures.length > 0) {
            structures[rand.nextInt(structures.length)].totallyDestroy();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void spawnDefenders(long deityId, int creatureTemplateId) {
        if (!Servers.isThisATestServer() && (Servers.localServer.isChallengeOrEpicServer() || Servers.isThisAChaosServer())) {
            byte friendlyKingdom = Deities.getFavoredKingdom((int)deityId);
            Deity deity = Deities.getDeity((int)deityId);
            try {
                Kingdom k;
                CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(creatureTemplateId);
                if (friendlyKingdom != 0 && (k = Kingdoms.getKingdom(friendlyKingdom)) != null && k.lastConfrontationTileX > 1 && k.lastConfrontationTileY > 1) {
                    for (int a = 0; a < rand.nextInt(7) + 1; ++a) {
                        int tx = Zones.safeTileX(k.lastConfrontationTileX - 5 + rand.nextInt(10));
                        int ty = Zones.safeTileY(k.lastConfrontationTileY - 5 + rand.nextInt(10));
                        Effectuator.spawnCreatureAt(tx, ty, ctemplate, friendlyKingdom);
                    }
                }
                if (deity == null || deity.lastConfrontationTileX <= 1 || deity.lastConfrontationTileY <= 1) return;
                for (int a = 0; a < rand.nextInt(7) + 1; ++a) {
                    int tx = Zones.safeTileX(deity.lastConfrontationTileX - 5 + rand.nextInt(10));
                    int ty = Zones.safeTileY(deity.lastConfrontationTileY - 5 + rand.nextInt(10));
                    Effectuator.spawnCreatureAt(tx, ty, ctemplate, friendlyKingdom);
                }
                return;
            }
            catch (NoSuchCreatureTemplateException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
                return;
            }
        } else {
            logger.log(Level.INFO, "Spawning defenders");
        }
    }

    public static void spawnOwnCreatures(long deityId, int creatureTemplateId, boolean onlyAtHome) {
        byte friendlyKingdom = Deities.getFavoredKingdom((int)deityId);
        try {
            VolaTile t;
            CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(creatureTemplateId);
            int summoned = 0;
            Player[] players = Players.getInstance().getPlayers();
            int maxplayers = players.length / 10;
            int maxSummoned = (int)(200.0f / ctemplate.baseCombatRating);
            if (creatureTemplateId == 75) {
                maxplayers = 2;
                maxSummoned = 5;
            }
            if (!Servers.localServer.isChallengeOrEpicServer() && !Servers.isThisAChaosServer()) {
                return;
            }
            if (players.length > 10) {
                for (int x = 0; x < maxplayers; ++x) {
                    int playint = rand.nextInt(players.length);
                    if (!(players[playint].getPositionZ() > -1.0f) && (!ctemplate.isSwimming() || !(players[playint].getPositionZ() < -4.0f)) || players[playint].getKingdomTemplateId() == friendlyKingdom || players[playint].isFriendlyKingdom(friendlyKingdom)) continue;
                    int centerx = players[playint].getTileX();
                    int centery = players[playint].getTileY();
                    int a = 0;
                    while ((float)a < Math.max(1.0f, 30.0f / ctemplate.baseCombatRating)) {
                        int ty;
                        int tx = Zones.safeTileX(centerx - 5 + rand.nextInt(10));
                        t = Zones.getOrCreateTile(tx, ty = Zones.safeTileY(centery - 5 + rand.nextInt(10)), true);
                        if (t.getStructure() == null && t.getVillage() == null) {
                            Effectuator.spawnCreatureAt(tx, ty, ctemplate, friendlyKingdom);
                            if (++summoned >= maxSummoned) break;
                        }
                        ++a;
                    }
                    if (summoned >= maxSummoned) break;
                }
            }
            if (!Servers.isThisATestServer()) {
                int tries = 0;
                block6: while (summoned < maxSummoned && tries < 5000) {
                    ++tries;
                    int centerx = rand.nextInt(Zones.worldTileSizeX);
                    int centery = rand.nextInt(Zones.worldTileSizeY);
                    if ((!onlyAtHome || Zones.getKingdom(centerx, centery) != friendlyKingdom) && Zones.getKingdom(centerx, centery) == friendlyKingdom) continue;
                    for (int x = 0; x < 10; ++x) {
                        int tx = Zones.safeTileX(centerx - 5 + rand.nextInt(10));
                        int ty = Zones.safeTileY(centery - 5 + rand.nextInt(10));
                        try {
                            float height = Zones.calculateHeight(tx * 4 + 2, ty * 4 + 2, true);
                            if (!(height >= 0.0f) && (!ctemplate.isSwimming() || !(height < -2.0f)) || (t = Zones.getOrCreateTile(tx, ty, true)).getStructure() != null || t.getVillage() != null) continue;
                            Effectuator.spawnCreatureAt(tx, ty, ctemplate, friendlyKingdom);
                            ++summoned;
                            continue block6;
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.WARNING, nsz.getMessage());
                        }
                    }
                }
            } else {
                logger.log(Level.INFO, "Spawning Own creatures");
            }
        }
        catch (NoSuchCreatureTemplateException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private static void spawnCreatureAt(int tilex, int tiley, CreatureTemplate ctemplate, byte friendlyKingdom) {
        if (ctemplate != null) {
            try {
                byte sex = ctemplate.getSex();
                if (sex == 0 && !ctemplate.keepSex && Server.rand.nextInt(2) == 0) {
                    sex = 1;
                }
                byte ctype = 0;
                int switchi = Server.rand.nextInt(40);
                if (switchi == 0) {
                    ctype = 99;
                } else if (switchi == 1) {
                    ctype = 1;
                } else if (switchi == 2) {
                    ctype = 4;
                } else if (switchi == 4) {
                    ctype = 11;
                }
                Zones.flash(tilex, tiley, false);
                Creature.doNew(ctemplate.getTemplateId(), false, tilex * 4 + 2, tiley * 4 + 2, rand.nextFloat() * 360.0f, 0, ctemplate.getName(), sex, friendlyKingdom, ctype, false);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadEffects() {
        block11: {
            ResultSet rs;
            PreparedStatement ps;
            Connection dbcon;
            block12: {
                block13: {
                    block10: {
                        dbcon = null;
                        ps = null;
                        rs = null;
                        if (!Servers.localServer.PVPSERVER || Servers.localServer.HOMESERVER) break block13;
                        try {
                            dbcon = DbConnector.getDeityDbCon();
                            ps = dbcon.prepareStatement(LOAD_KINGDOM_EFFECTS);
                            rs = ps.executeQuery();
                            int found = 0;
                            while (rs.next()) {
                                int effect = rs.getInt("EFFECT");
                                byte kingdomId = rs.getByte("KINGDOM");
                                Effectuator.implementEffectControl(effect, kingdomId);
                                ++found;
                            }
                            if (found != 0) break block10;
                            Effectuator.createEffects();
                        }
                        catch (SQLException sqx) {
                            try {
                                logger.log(Level.WARNING, sqx.getMessage(), sqx);
                            }
                            catch (Throwable throwable) {
                                DbUtilities.closeDatabaseObjects(ps, rs);
                                DbConnector.returnConnection(dbcon);
                                throw throwable;
                            }
                            DbUtilities.closeDatabaseObjects(ps, rs);
                            DbConnector.returnConnection(dbcon);
                            break block11;
                        }
                    }
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    break block11;
                }
                try {
                    dbcon = DbConnector.getDeityDbCon();
                    ps = dbcon.prepareStatement(LOAD_DEITY_EFFECTS);
                    rs = ps.executeQuery();
                    int found = 0;
                    while (rs.next()) {
                        int effect = rs.getInt("EFFECT");
                        byte deityId = rs.getByte("DEITY");
                        Effectuator.implementDeityEffectControl(effect, deityId);
                        ++found;
                    }
                    if (found != 0) break block12;
                    Effectuator.createEffects();
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, sqx.getMessage(), sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, rs);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }

    static void createEffects() {
        Effectuator.initializeEffect(3);
        Effectuator.initializeEffect(4);
        Effectuator.initializeEffect(1);
        Effectuator.initializeEffect(2);
    }

    public static int getKingdomTemplateWithSpeedBonus() {
        return kingdomTemplateWithSpeedBonus;
    }

    public static int getKingdomTemplateWithCombatRating() {
        return kingdomTemplateWithCombatRating;
    }

    public static int getKingdomTemplateWithStaminaRegain() {
        return kingdomTemplateWithStaminaRegain;
    }

    public static int getKingdomTemplateWithFavorGain() {
        return kingdomTemplateWithFavorGain;
    }

    private static void removeEffectFromPlayersWithKingdom(int effectId, int deityId) {
        Player[] players;
        byte kingdomTemplate = Deities.getFavoredKingdom(deityId);
        for (Player p : players = Players.getInstance().getPlayers()) {
            if (p.getKingdomTemplateId() != kingdomTemplate) continue;
            p.sendRemoveDeityEffectBonus(effectId);
        }
    }

    private static void addEffectToPlayersWithKingdom(int effectId, int deityId) {
        Player[] players;
        byte kingdomTemplate = Deities.getFavoredKingdom(deityId);
        for (Player p : players = Players.getInstance().getPlayers()) {
            if (p.getKingdomTemplateId() != kingdomTemplate) continue;
            p.sendAddDeityEffectBonus(effectId);
        }
    }

    static void implementEffectControl(int effectId, int kingdomTemplateId) {
        switch (effectId) {
            case 3: {
                if (kingdomTemplateWithStaminaRegain != 0 && kingdomTemplateWithStaminaRegain != kingdomTemplateId) {
                    Effectuator.removeEffectFromPlayersWithKingdom(effectId, kingdomTemplateId);
                }
                if ((kingdomTemplateWithStaminaRegain = kingdomTemplateId) == 0) break;
                Effectuator.addEffectToPlayersWithKingdom(3, kingdomTemplateWithStaminaRegain);
                break;
            }
            case 4: {
                if (kingdomTemplateWithFavorGain != 0 && kingdomTemplateWithFavorGain != kingdomTemplateId) {
                    Effectuator.removeEffectFromPlayersWithKingdom(effectId, kingdomTemplateId);
                }
                if ((kingdomTemplateWithFavorGain = kingdomTemplateId) == 0) break;
                Effectuator.addEffectToPlayersWithKingdom(4, kingdomTemplateWithFavorGain);
                break;
            }
            case 1: {
                if (kingdomTemplateWithSpeedBonus != 0 && kingdomTemplateWithSpeedBonus != kingdomTemplateId) {
                    Effectuator.removeEffectFromPlayersWithKingdom(effectId, kingdomTemplateId);
                }
                if ((kingdomTemplateWithSpeedBonus = kingdomTemplateId) == 0) break;
                Effectuator.addEffectToPlayersWithKingdom(1, kingdomTemplateWithSpeedBonus);
                break;
            }
            case 2: {
                if (kingdomTemplateWithCombatRating != 0 && kingdomTemplateWithCombatRating != kingdomTemplateId) {
                    Effectuator.removeEffectFromPlayersWithKingdom(effectId, kingdomTemplateId);
                }
                if ((kingdomTemplateWithCombatRating = kingdomTemplateId) == 0) break;
                Effectuator.addEffectToPlayersWithKingdom(2, kingdomTemplateWithCombatRating);
                break;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static void initializeEffect(int effectId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        if (Servers.localServer.PVPSERVER && !Servers.localServer.HOMESERVER) {
            try {
                dbcon = DbConnector.getDeityDbCon();
                ps = dbcon.prepareStatement(INSERT_KINGDOM_EFFECTS);
                ps.setInt(1, effectId);
                ps.setByte(2, (byte)0);
                ps.executeUpdate();
                return;
            }
            catch (SQLException sqx) {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
                return;
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(INSERT_DEITY_EFFECTS);
            ps.setInt(1, effectId);
            ps.setInt(2, 0);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return;
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        return;
    }

    public static int getDeityWithSpeedBonus() {
        return deityWithSpeedBonus;
    }

    public static int getDeityWithCombatRating() {
        return deityWithCombatRating;
    }

    public static int getDeityWithStaminaRegain() {
        return deityWithStaminaRegain;
    }

    public static int getDeityWithFavorGain() {
        return deityWithFavorGain;
    }

    private static void removeEffectFromPlayersWithDeity(int effectId, int deityId) {
        Player[] players;
        for (Player p : players = Players.getInstance().getPlayers()) {
            if (p.getDeity() == null || p.getDeity().number != deityId) continue;
            p.sendRemoveDeityEffectBonus(effectId);
        }
    }

    private static void addEffectToPlayersWithDeity(int effectId, int deityId) {
        Player[] players;
        for (Player p : players = Players.getInstance().getPlayers()) {
            if (p.getDeity() == null || p.getDeity().number != deityId) continue;
            p.sendAddDeityEffectBonus(effectId);
        }
    }

    static void implementDeityEffectControl(int effectId, int deityId) {
        if (!Servers.localServer.PVPSERVER || Servers.localServer.HOMESERVER) {
            switch (effectId) {
                case 3: {
                    if (deityWithStaminaRegain != 0 && deityWithStaminaRegain != deityId) {
                        Effectuator.removeEffectFromPlayersWithDeity(effectId, deityId);
                    }
                    if ((deityWithStaminaRegain = deityId) == 0) break;
                    Effectuator.addEffectToPlayersWithDeity(3, deityWithStaminaRegain);
                    break;
                }
                case 4: {
                    if (deityWithFavorGain != 0 && deityWithFavorGain != deityId) {
                        Effectuator.removeEffectFromPlayersWithDeity(effectId, deityId);
                    }
                    if ((deityWithFavorGain = deityId) == 0) break;
                    Effectuator.addEffectToPlayersWithDeity(4, deityWithFavorGain);
                    break;
                }
                case 1: {
                    if (deityWithSpeedBonus != 0 && deityWithSpeedBonus != deityId) {
                        Effectuator.removeEffectFromPlayersWithDeity(effectId, deityId);
                    }
                    if ((deityWithSpeedBonus = deityId) == 0) break;
                    Effectuator.addEffectToPlayersWithDeity(1, deityWithSpeedBonus);
                    break;
                }
                case 2: {
                    if (deityWithCombatRating != 0 && deityWithCombatRating != deityId) {
                        Effectuator.removeEffectFromPlayersWithDeity(effectId, deityId);
                    }
                    if ((deityWithCombatRating = deityId) == 0) break;
                    Effectuator.addEffectToPlayersWithDeity(2, deityWithCombatRating);
                    break;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setEffectController(int effectId, long deityId) {
        block11: {
            PreparedStatement ps;
            Connection dbcon;
            block10: {
                dbcon = null;
                ps = null;
                if (!Servers.localServer.PVPSERVER || Servers.localServer.HOMESERVER) break block10;
                byte kingdomId = Deities.getFavoredKingdom((int)deityId);
                try {
                    dbcon = DbConnector.getDeityDbCon();
                    ps = dbcon.prepareStatement(UPDATE_KINGDOM_EFFECTS);
                    ps.setByte(1, kingdomId);
                    ps.setInt(2, effectId);
                    ps.executeUpdate();
                }
                catch (SQLException sqx) {
                    logger.log(Level.WARNING, sqx.getMessage(), sqx);
                }
                finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
                Effectuator.implementEffectControl(effectId, kingdomId);
                break block11;
            }
            try {
                dbcon = DbConnector.getDeityDbCon();
                ps = dbcon.prepareStatement(UPDATE_DEITY_EFFECTS);
                ps.setLong(1, deityId);
                ps.setInt(2, effectId);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, sqx.getMessage(), sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            Effectuator.implementDeityEffectControl(effectId, (int)deityId);
        }
    }
}

