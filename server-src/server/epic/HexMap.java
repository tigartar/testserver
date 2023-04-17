/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Features;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.epic.Effectuator;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.EpicMapListener;
import com.wurmonline.server.epic.EpicMissionEnum;
import com.wurmonline.server.epic.EpicScenario;
import com.wurmonline.server.epic.EpicXmlWriter;
import com.wurmonline.server.epic.MapHex;
import com.wurmonline.server.epic.Valrei;
import com.wurmonline.server.epic.ValreiMapData;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WcEpicEvent;
import com.wurmonline.server.webinterface.WcEpicKarmaCommand;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class HexMap
implements MiscConstants,
CreatureTemplateIds,
TimeConstants {
    private static final Logger logger = Logger.getLogger(HexMap.class.getName());
    public static Valrei VALREI;
    public static final String VALREINAME = "Valrei";
    private static final String UPDATE_ENTITY_CONTROLLER = "UPDATE CONTROLLERS SET CONTROLLER=? WHERE CREATURE=?";
    private static final String CREATE_ENTITY_CONTROLLER = "INSERT INTO CONTROLLERS (CREATURE) VALUES (?)";
    private static final String LOAD_ENTITY_CONTROLLERS = "SELECT * FROM CONTROLLERS";
    private static final String LOAD_ALL_VISITED_HEX = "SELECT * FROM VISITED";
    static final Random rand;
    private final Map<Integer, EpicMapListener> eventListeners = new ConcurrentHashMap<Integer, EpicMapListener>();
    private final Map<Integer, MapHex> hexmap = new ConcurrentHashMap<Integer, MapHex>();
    private final Map<Long, LinkedList<Integer>> controllers = new ConcurrentHashMap<Long, LinkedList<Integer>>();
    private final Map<Long, EpicEntity> entities = new ConcurrentHashMap<Long, EpicEntity>();
    private final EpicScenario currentScenario;
    private final String name;
    private static Random nameRand;

    HexMap(String _name) {
        this.name = _name;
        this.currentScenario = new EpicScenario();
        VALREI = this.name.equals(VALREINAME) ? (Valrei)this : new Valrei();
    }

    final String getName() {
        return this.name;
    }

    public final MapHex getMapHex(int id) {
        return this.hexmap.get(id);
    }

    final MapHex getMapHex(Integer id) {
        return this.hexmap.get(id);
    }

    MapHex getSpawnHex(EpicEntity entity) {
        for (MapHex hm : this.hexmap.values()) {
            if (!hm.isSpawnFor(entity.getId())) continue;
            return hm;
        }
        return null;
    }

    final void addEntity(EpicEntity entity) {
        if (entity.isPlayerGod()) {
            return;
        }
        this.entities.put(entity.getId(), entity);
    }

    void destroyEntity(EpicEntity entity) {
        entity.dropAll(false);
        entity.setHexMap(null);
        this.removeEntity(entity);
        if (entity.getCarrier() != null) {
            entity.setCarrier(null, true, false, false);
        }
        entity.deleteEntity();
    }

    final void removeEntity(EpicEntity entity) {
        this.entities.remove(entity.getId());
    }

    public final void loadAllEntities() {
        if (this.entities.isEmpty()) {
            this.generateEntities();
        }
    }

    void generateEntities() {
    }

    final MapHex getRandomHex() {
        int toget = rand.nextInt(this.hexmap.size());
        int x = 0;
        for (MapHex hm : this.hexmap.values()) {
            if (x == toget) {
                return hm;
            }
            ++x;
        }
        return null;
    }

    final void addMapHex(MapHex mh) {
        this.hexmap.put(mh.getId(), mh);
    }

    public final EpicEntity[] getAllEntities() {
        return this.entities.values().toArray(new EpicEntity[this.entities.size()]);
    }

    public final void pollAllEntities(boolean testing) {
        EpicEntity[] entityArr;
        for (EpicEntity entity : entityArr = this.getAllEntities()) {
            entity.poll();
            if (testing) {
                entity.setHelped(true, false);
            }
            if (entity.checkWinCondition()) break;
        }
    }

    final int getReasonAndEffectInt() {
        return this.currentScenario.getReasonPlusEffect();
    }

    final void win(EpicEntity entity, String collName, int nums) {
        this.setWinEffects(entity, collName, nums);
        this.checkSpecialMapWinCases(entity);
        this.nextScenario();
        WcEpicKarmaCommand.clearKarma();
        PlayerInfoFactory.resetScenarioKarma();
    }

    void setWinEffects(EpicEntity entity, String collName, int nums) {
    }

    public final void setEntityHelped(long entityId, byte missionType, int missionDifficulty) {
        EpicEntity entity = this.getEntity(entityId);
        if (entity != null) {
            float current = 0.0f;
            switch (rand.nextInt(7)) {
                case 0: {
                    current = entity.getCurrentSkill(102);
                    entity.setSkill(102, current + (100.0f - current) / ((float)(500 + (7 - missionDifficulty) * 50) + rand.nextFloat() * (float)((7 - missionDifficulty) * 50)));
                    break;
                }
                case 1: {
                    current = entity.getCurrentSkill(103);
                    entity.setSkill(103, current + (100.0f - current) / ((float)(500 + (7 - missionDifficulty) * 50) + rand.nextFloat() * (float)((7 - missionDifficulty) * 50)));
                    break;
                }
                case 2: {
                    current = entity.getCurrentSkill(104);
                    entity.setSkill(104, current + (100.0f - current) / ((float)(500 + (7 - missionDifficulty) * 50) + rand.nextFloat() * (float)((7 - missionDifficulty) * 50)));
                    break;
                }
                case 3: {
                    current = entity.getCurrentSkill(100);
                    entity.setSkill(100, current + (100.0f - current) / ((float)(500 + (7 - missionDifficulty) * 50) + rand.nextFloat() * (float)((7 - missionDifficulty) * 50)));
                    break;
                }
                case 4: {
                    current = entity.getCurrentSkill(101);
                    entity.setSkill(101, current + (100.0f - current) / ((float)(500 + (7 - missionDifficulty) * 50) + rand.nextFloat() * (float)((7 - missionDifficulty) * 50)));
                    break;
                }
                case 5: {
                    current = entity.getCurrentSkill(105);
                    entity.setSkill(105, current + (100.0f - current) / ((float)(500 + (7 - missionDifficulty) * 50) + rand.nextFloat() * (float)((7 - missionDifficulty) * 50)));
                    break;
                }
                case 6: {
                    current = entity.getCurrentSkill(106);
                    entity.setSkill(106, current + (100.0f - current) / ((float)(500 + (7 - missionDifficulty) * 50) + rand.nextFloat() * (float)((7 - missionDifficulty) * 50)));
                }
            }
            entity.setHelped(true, false);
            long timeToLeave = entity.modifyTimeToLeave(-EpicMissionEnum.getTimeReductionForMission(missionType, missionDifficulty));
            if (timeToLeave < System.currentTimeMillis()) {
                int effect = Server.rand.nextInt(4) + 1;
                WcEpicEvent wce = new WcEpicEvent(WurmId.getNextWCCommandId(), effect, entity.getId(), 0, 3, entity.getName() + "s followers now have the attention of the " + Effectuator.getSpiritType(effect) + " spirits.", false);
                wce.sendFromLoginServer();
                if (rand.nextInt(20) == 0) {
                    int template = 72 + Server.rand.nextInt(6);
                    this.setCreatureController(template, entity.getId());
                    try {
                        CreatureTemplate c = CreatureTemplateFactory.getInstance().getTemplate(template);
                        this.broadCast(entity.getName() + " now controls the " + c.getName() + "s.");
                    }
                    catch (NoSuchCreatureTemplateException nst) {
                        logger.log(Level.WARNING, nst.getMessage(), nst);
                    }
                }
            }
            entity.setShouldCreateMission(true, true);
        }
    }

    void checkSpecialMapWinCases(EpicEntity winner) {
    }

    void nextScenario() {
        this.currentScenario.saveScenario(false);
    }

    final boolean winCondition(boolean isWurm, int currentCollectibles, boolean isAtSpawn, int currentHex) {
        return (!this.currentScenario.isSpawnPointRequiredToWin() && currentHex == this.currentScenario.getHexNumRequiredToWin() || isAtSpawn && this.currentScenario.isSpawnPointRequiredToWin()) && (isWurm ? currentCollectibles >= this.currentScenario.getCollectiblesForWurmToWin() : currentCollectibles >= this.currentScenario.getCollectiblesToWin());
    }

    public void addDemigod(String _name, long id, long companion, float initialBStr, float initialBSta, float initialBCon, float initialML, float initialMS, float initialSS, float initialSD) {
        EpicEntity compa;
        EpicEntity newDemi = new EpicEntity(this, id, _name, 7, -1.0f, -1.0f);
        boolean foundHex = false;
        while (!foundHex) {
            MapHex hex = this.getRandomHex();
            if (hex == null || hex.isSpawn()) continue;
            hex.setHomeEntityId(id);
            hex.setSpawnEntityId(id);
            newDemi.createEntity(hex.getId());
            foundHex = true;
            logger.log(Level.INFO, _name + " will spawn " + hex.getPrepositionString() + " " + hex.getName());
        }
        newDemi.addSkill(102, initialBStr);
        newDemi.addSkill(103, initialBSta);
        newDemi.addSkill(104, initialBCon);
        newDemi.addSkill(100, initialML);
        newDemi.addSkill(101, initialMS);
        newDemi.addSkill(105, initialSS);
        newDemi.addSkill(106, initialSD);
        newDemi.createAndSaveSkills();
        if (companion != 0L && (compa = this.getEntity(companion)) != null) {
            newDemi.setCompanion(compa);
            compa.setDemigodPlusForEntity((byte)(compa.getDemigodsToAppoint() - 1));
        }
        newDemi.spawn();
        if (Features.Feature.VALREI_MAP.isEnabled()) {
            ValreiMapData.updateFromEpicEntity(newDemi);
            ValreiMapData.lastPolled = System.currentTimeMillis() - 1860000L;
            ValreiMapData.lastUpdatedTime = System.currentTimeMillis() - 2460000L;
        }
    }

    EpicEntity getDemiGodFor(EpicEntity entity) {
        for (EpicEntity e : this.entities.values()) {
            if (!e.isDemigod() || e.getCompanion() != entity) continue;
            return e;
        }
        return null;
    }

    public boolean elevateDemigod(long deityNum) {
        return this.elevateDemigod(deityNum, null);
    }

    public boolean elevateDemigod(long deityNum, @Nullable String name) {
        EpicEntity god = this.getEntity(deityNum);
        logger.log(Level.INFO, "Checking elev for " + deityNum);
        if (god != null) {
            logger.log(Level.INFO, "Checking elev at 2 for " + god.getId());
            EpicEntity e = this.getDemiGodFor(god);
            if (e != null) {
                logger.log(Level.INFO, "Found entity demigod " + e.getName() + ". Number is " + e.getId() + ".");
                if (name == null || e.getName().toLowerCase().equals(name.toLowerCase())) {
                    Deity d = Deities.getDeity((int)e.getId());
                    logger.log(Level.INFO, "Setting deity power " + d.getName() + " id=" + d.number);
                    d.setPower((byte)3);
                    e.setType(0);
                    float rest = e.getInitialAttack() - 6.0f;
                    float att = Math.min(6.0f, e.getInitialAttack());
                    if (rest > 0.0f) {
                        att += rest / 10.0f;
                    }
                    rest = e.getInitialVitality() - 6.0f;
                    float vit = Math.min(6.0f, e.getInitialVitality());
                    if (rest > 0.0f) {
                        vit += rest / 10.0f;
                    }
                    Servers.ascend(d.getNumber(), d.name, e.getId(), (byte)deityNum, d.sex, (byte)3, e.getCurrentSkill(102), e.getCurrentSkill(103), e.getCurrentSkill(104), e.getCurrentSkill(100), e.getCurrentSkill(101), e.getCurrentSkill(105), e.getCurrentSkill(106));
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    void generateRandomScenario() {
        EpicEntity.toggleXmlDump(false);
        this.destroyCollectables();
        this.destroySources();
        this.respawnEntities();
        int maxCollectables = 1 + rand.nextInt(10);
        int reasonAndEffect = this.getRandomReason();
        boolean spawnPoint = rand.nextBoolean();
        String firstPartOfName = HexMap.generateFirstName();
        String secondPartOfName = HexMap.generateSecondName();
        EpicEntity questHolder = this.getRandomEntityMonster();
        int hexNum = 0;
        if (!spawnPoint) {
            hexNum = rand.nextInt(this.hexmap.size()) + 1;
        }
        if (reasonAndEffect == 15) {
            hexNum = 5;
            spawnPoint = false;
        }
        String missionName = "";
        String instigator = this.getRandomInstigator();
        String hide = this.generateHideWord();
        String reasonString = this.getReason(reasonAndEffect, maxCollectables > 1);
        String missionDescription = instigator + ' ' + hide + " the " + firstPartOfName + ' ' + secondPartOfName + '.' + ' ' + reasonString;
        if (maxCollectables == 1) {
            missionName = this.generateMissionName(firstPartOfName + ' ' + secondPartOfName, questHolder);
            if (questHolder != null && rand.nextBoolean()) {
                missionDescription = instigator + ' ' + hide + ' ' + questHolder.getName() + "'s " + firstPartOfName + ' ' + secondPartOfName + '.' + ' ' + reasonString;
            }
        } else {
            missionName = rand.nextBoolean() ? this.generateMissionName(firstPartOfName + ' ' + secondPartOfName + "s", questHolder) : this.generateMissionName(secondPartOfName + "s", questHolder);
            missionDescription = rand.nextBoolean() ? instigator + ' ' + hide + ' ' + questHolder.getName() + "'s " + HexMap.getNameForNumber(maxCollectables) + ' ' + firstPartOfName + ' ' + secondPartOfName + "s" + '.' + ' ' + reasonString : instigator + ' ' + hide + " the " + firstPartOfName + ' ' + secondPartOfName + "s" + '.' + ' ' + reasonString;
        }
        missionDescription = missionDescription + ' ' + this.getMapSpecialWinEffect();
        int srcfrags = 2 + rand.nextInt(4);
        this.generateCollectables(maxCollectables, firstPartOfName + ' ' + secondPartOfName, 2);
        this.generateCollectables(srcfrags, "Source " + HexMap.generateSecondName(), 1);
        this.setWinCondition(Math.max(1, maxCollectables / 2), maxCollectables, spawnPoint, hexNum, missionName, missionDescription, reasonAndEffect);
        EpicEntity.toggleXmlDump(true);
        EpicXmlWriter.dumpEntities(this);
    }

    String getMapSpecialWinEffect() {
        return "";
    }

    int getRandomReason() {
        int num = rand.nextInt(21);
        if (num == 20) {
            num = 20 + rand.nextInt(5);
        }
        return num;
    }

    String getReason(int reasonId, boolean many) {
        return "Those are dangerous!";
    }

    String getRandomInstigator() {
        String firstPart;
        int r = rand.nextInt(20);
        switch (r) {
            case 0: {
                firstPart = "The Morbid One";
                break;
            }
            case 1: {
                firstPart = "The vengeful Sea Spirits";
                break;
            }
            case 2: {
                firstPart = "The mischievous Forest Spirits";
                break;
            }
            case 3: {
                firstPart = "The immobile Frozen One";
                break;
            }
            case 4: {
                firstPart = "The unfathomable Stargazer";
                break;
            }
            case 5: {
                firstPart = "The mysterious Drakespirit";
                break;
            }
            case 6: {
                firstPart = "The evil Deathcrawler";
                break;
            }
            case 7: {
                firstPart = "Ethereal thunderstorms";
                break;
            }
            case 8: {
                firstPart = "An emissary from the void";
                break;
            }
            case 9: {
                firstPart = "A deadly starburst";
                break;
            }
            case 10: {
                firstPart = "A heavy chaos eruption";
                break;
            }
            case 11: {
                firstPart = "An unnatural meteor storm";
                break;
            }
            case 12: {
                firstPart = "A sudden surge in source energy";
                break;
            }
            case 13: {
                firstPart = "A physical storm of emotions";
                break;
            }
            case 14: {
                firstPart = "A quake of world-shattering proportions";
                break;
            }
            case 15: {
                firstPart = "The Shift";
                break;
            }
            case 16: {
                firstPart = "An eruption of Fire Spirits from Firejaw";
                break;
            }
            case 17: {
                firstPart = "Uttacha who left her depths in desperation";
                break;
            }
            case 18: {
                firstPart = "A portal to Seris opened. The dead souls";
                break;
            }
            case 19: {
                firstPart = "Demons from Sol";
                break;
            }
            default: {
                firstPart = "";
                logger.warning("Somehow rand.nextInt(20) returned an int that was not between 0 and 19");
            }
        }
        return firstPart;
    }

    EpicEntity getRandomEntityMonster() {
        EpicEntity[] allArr = this.getAllEntities();
        LinkedList<EpicEntity> mons = new LinkedList<EpicEntity>();
        for (EpicEntity ep : allArr) {
            if (!ep.isWurm() && !ep.isDeity() && !ep.isSentinelMonster() && !ep.isAlly()) continue;
            mons.add(ep);
        }
        if (mons.size() > 0) {
            return (EpicEntity)mons.get(rand.nextInt(mons.size()));
        }
        return null;
    }

    String generateHideWord() {
        String secondPart;
        int r = rand.nextInt(14);
        switch (r) {
            case 0: {
                secondPart = "hid";
                break;
            }
            case 1: {
                secondPart = "scattered";
                break;
            }
            case 2: {
                secondPart = "dispersed";
                break;
            }
            case 3: {
                secondPart = "dug down";
                break;
            }
            case 4: {
                secondPart = "brought";
                break;
            }
            case 5: {
                secondPart = "stole";
                break;
            }
            case 6: {
                secondPart = "dropped";
                break;
            }
            case 7: {
                secondPart = "misplaced";
                break;
            }
            case 8: {
                secondPart = "invented";
                break;
            }
            case 9: {
                secondPart = "created";
                break;
            }
            case 10: {
                secondPart = "spread out";
                break;
            }
            case 11: {
                secondPart = "revealed the existance of";
                break;
            }
            case 12: {
                secondPart = "rained";
                break;
            }
            case 13: {
                secondPart = "separated";
                break;
            }
            default: {
                secondPart = "";
                logger.warning("Somehow rand.nextInt(14) returned an int that was not between 0 and 13");
            }
        }
        int prep = rand.nextInt(4);
        if (prep == 0) {
            return "has " + secondPart;
        }
        if (prep == 1) {
            return "just " + secondPart;
        }
        if (prep == 2) {
            return "recently " + secondPart;
        }
        return secondPart;
    }

    String generateMissionName(String firstPart, EpicEntity questHolder) {
        String secondPart;
        String monsterName = questHolder != null ? questHolder.getName() : "The Spirits";
        int r = rand.nextInt(16);
        switch (r) {
            case 0: {
                secondPart = "Hunt for the " + firstPart;
                break;
            }
            case 1: {
                secondPart = "Looking for " + firstPart;
                break;
            }
            case 2: {
                secondPart = "The lost " + firstPart;
                break;
            }
            case 3: {
                secondPart = "Quest of the " + firstPart;
                break;
            }
            case 4: {
                secondPart = "Revenge of " + monsterName;
                break;
            }
            case 5: {
                secondPart = monsterName + "'s hunt";
                break;
            }
            case 6: {
                secondPart = monsterName + " lost";
                break;
            }
            case 7: {
                secondPart = firstPart + " lost";
                break;
            }
            case 8: {
                secondPart = monsterName + " in peril";
                break;
            }
            case 9: {
                secondPart = monsterName + "'s mystery";
                break;
            }
            case 10: {
                secondPart = monsterName + " fall";
                break;
            }
            case 11: {
                secondPart = "The missing " + firstPart;
                break;
            }
            case 12: {
                secondPart = "Lost the " + firstPart;
                break;
            }
            case 13: {
                secondPart = "Who hid the " + firstPart;
                break;
            }
            case 14: {
                secondPart = monsterName + "'s " + firstPart;
                break;
            }
            case 15: {
                secondPart = monsterName + " and the " + firstPart;
                break;
            }
            default: {
                secondPart = "";
                logger.warning("Somehow rand.nextInt(16) returned an int that was not between 0 and 15");
            }
        }
        return secondPart;
    }

    final void setWinCondition(int collectiblesRequired, int collectiblesRequiredForWurm, boolean atSpawnPointRequired, int hexNumRequired, String newScenarioName, String newScenarioQuest, int reasonAndEffect) {
        this.currentScenario.setCollectiblesToWin(collectiblesRequired);
        this.currentScenario.setCollectiblesForWurmToWin(collectiblesRequiredForWurm);
        this.currentScenario.setSpawnPointRequiredToWin(atSpawnPointRequired);
        if (this.currentScenario.isSpawnPointRequiredToWin()) {
            this.currentScenario.setHexNumRequiredToWin(0);
        } else {
            this.currentScenario.setHexNumRequiredToWin(hexNumRequired);
        }
        this.currentScenario.setScenarioName(newScenarioName);
        this.currentScenario.setScenarioQuest(newScenarioQuest);
        this.currentScenario.setReasonPlusEffect(reasonAndEffect);
        logger.log(Level.INFO, this.currentScenario.getScenarioName() + ':');
        logger.log(Level.INFO, this.currentScenario.getScenarioQuest());
        this.currentScenario.saveScenario(true);
    }

    public final void broadCast(String event) {
        for (EpicMapListener listener : this.eventListeners.values()) {
            listener.broadCastEpicEvent(event);
        }
        logger.log(Level.INFO, event);
    }

    final EpicScenario getCurrentScenario() {
        return this.currentScenario;
    }

    final String getScenarioQuestString() {
        return this.currentScenario.getScenarioQuest();
    }

    final String getScenarioName() {
        return this.currentScenario.getScenarioName();
    }

    final void addAttackTo(long entityId, float addedValue) {
        EpicEntity entity = this.entities.get(entityId);
        if (entity != null) {
            entity.setAttack(entity.getAttack() + addedValue);
        }
    }

    final void addVitalityTo(long entityId, float addedValue) {
        EpicEntity entity = this.entities.get(entityId);
        if (entity != null) {
            entity.setVitality(entity.getVitality() + addedValue);
        }
    }

    public final EpicEntity getEntity(long eid) {
        return this.entities.get(eid);
    }

    final void addEntity(String newEntityName, long newid, float attack, float vitality, long masterId, int deityType) {
        EpicEntity newent = new EpicEntity(this, newid, newEntityName, deityType, attack, vitality);
        EpicEntity masterEntity = this.entities.get(masterId);
        if (masterEntity != null) {
            MapHex mh = this.getSpawnHex(masterEntity);
            MapHex newSpawn = this.getMapHex(mh.getId() + (masterId == 3L ? 2 : 1));
            newSpawn.setSpawnEntityId(newid);
            newent.setCompanion(masterEntity);
            this.broadCast(newEntityName + " has joined the side of " + masterEntity.getName() + " on " + this.name + '.');
            this.broadCast(newEntityName + " set up home " + newSpawn.getPrepositionString() + newSpawn.getName() + '.');
        } else {
            boolean searching = true;
            while (searching) {
                logger.log(Level.INFO, "Looking for free spawnpoint for " + newEntityName);
                MapHex mh = this.getRandomHex();
                if (mh.isSpawn()) continue;
                mh.setSpawnEntityId(newid);
                searching = false;
                this.broadCast(newEntityName + " has entered " + this.name + '.');
                this.broadCast(newEntityName + " set up home " + mh.getPrepositionString() + mh.getName() + '.');
            }
        }
    }

    final void broadCastEpicWinCondition(String _scenarioname, String _scenarioQuest) {
        for (EpicMapListener listener : this.eventListeners.values()) {
            listener.broadCastEpicWinCondition(_scenarioname, _scenarioQuest);
        }
    }

    public final void removeListener(EpicMapListener listener) {
        this.eventListeners.remove(listener.hashCode());
    }

    public final void addListener(EpicMapListener listener) {
        this.eventListeners.put(listener.hashCode(), listener);
    }

    public static final String generateFirstName(int randId) {
        nameRand.setSeed(randId);
        return HexMap.getFirstNameForNumber(nameRand.nextInt(20));
    }

    public static final String generateFirstName() {
        int r = rand.nextInt(20);
        return HexMap.getFirstNameForNumber(r);
    }

    static final String getFirstNameForNumber(int r) {
        String firstPart;
        switch (r) {
            case 0: {
                firstPart = "Golden";
                break;
            }
            case 1: {
                firstPart = "Frozen";
                break;
            }
            case 2: {
                firstPart = "Silvery";
                break;
            }
            case 3: {
                firstPart = "Ornamented";
                break;
            }
            case 4: {
                firstPart = "Shiny";
                break;
            }
            case 5: {
                firstPart = "Beautiful";
                break;
            }
            case 6: {
                firstPart = "Burning";
                break;
            }
            case 7: {
                firstPart = "Fire";
                break;
            }
            case 8: {
                firstPart = "Glowing";
                break;
            }
            case 9: {
                firstPart = "Lustrous";
                break;
            }
            case 10: {
                firstPart = "Charming";
                break;
            }
            case 11: {
                firstPart = "Deadly";
                break;
            }
            case 12: {
                firstPart = "Wild";
                break;
            }
            case 13: {
                firstPart = "Soulstruck";
                break;
            }
            case 14: {
                firstPart = "Black";
                break;
            }
            case 15: {
                firstPart = "Shadow";
                break;
            }
            case 16: {
                firstPart = "Rotten";
                break;
            }
            case 17: {
                firstPart = "Marble";
                break;
            }
            case 18: {
                firstPart = "Powerful";
                break;
            }
            case 19: {
                firstPart = "Holy";
                break;
            }
            default: {
                firstPart = "";
                logger.warning("Method argument was an int that was not between 0 and 19");
            }
        }
        return firstPart;
    }

    static final String getNameForNumber(int number) {
        String numString;
        switch (number) {
            case 0: {
                numString = "zero";
                break;
            }
            case 1: {
                numString = "one";
                break;
            }
            case 2: {
                numString = "two";
                break;
            }
            case 3: {
                numString = "three";
                break;
            }
            case 4: {
                numString = "four";
                break;
            }
            case 5: {
                numString = "five";
                break;
            }
            case 6: {
                numString = "six";
                break;
            }
            case 7: {
                numString = "seven";
                break;
            }
            case 8: {
                numString = "eight";
                break;
            }
            case 9: {
                numString = "nine";
                break;
            }
            case 10: {
                numString = "ten";
                break;
            }
            case 11: {
                numString = "eleven";
                break;
            }
            case 12: {
                numString = "twelve";
                break;
            }
            case 13: {
                numString = "thirteen";
                break;
            }
            case 14: {
                numString = "fourteen";
                break;
            }
            case 15: {
                numString = "fifteen";
                break;
            }
            case 16: {
                numString = "sixteen";
                break;
            }
            case 17: {
                numString = "seventeen";
                break;
            }
            case 18: {
                numString = "eighteen";
                break;
            }
            case 19: {
                numString = "nineteen";
                break;
            }
            case 20: {
                numString = "twenty";
                break;
            }
            default: {
                numString = number + "";
            }
        }
        return numString;
    }

    public static final String generateSecondName(int randId) {
        nameRand.setSeed(randId);
        return HexMap.getSecondNameForNumber(nameRand.nextInt(20));
    }

    public static final String generateSecondName() {
        int r = rand.nextInt(20);
        return HexMap.getSecondNameForNumber(r);
    }

    static final String getSecondNameForNumber(int r) {
        String secondPart;
        switch (r) {
            case 0: {
                secondPart = "Feather";
                break;
            }
            case 1: {
                secondPart = "Token";
                break;
            }
            case 2: {
                secondPart = "Totem";
                break;
            }
            case 3: {
                secondPart = "Crystal";
                break;
            }
            case 4: {
                secondPart = "Shard";
                break;
            }
            case 5: {
                secondPart = "Opal";
                break;
            }
            case 6: {
                secondPart = "Diamond";
                break;
            }
            case 7: {
                secondPart = "Fragment";
                break;
            }
            case 8: {
                secondPart = "Jar";
                break;
            }
            case 9: {
                secondPart = "Quill";
                break;
            }
            case 10: {
                secondPart = "Harp";
                break;
            }
            case 11: {
                secondPart = "Orb";
                break;
            }
            case 12: {
                secondPart = "Sceptre";
                break;
            }
            case 13: {
                secondPart = "Spirit";
                break;
            }
            case 14: {
                secondPart = "Jewel";
                break;
            }
            case 15: {
                secondPart = "Corpse";
                break;
            }
            case 16: {
                secondPart = "Eye";
                break;
            }
            case 17: {
                secondPart = "Circlet";
                break;
            }
            case 18: {
                secondPart = "Band";
                break;
            }
            case 19: {
                secondPart = "Strand";
                break;
            }
            default: {
                secondPart = "";
                logger.warning("Method argument was an int that was not between 0 and 19");
            }
        }
        return secondPart;
    }

    final void destroyCollectables() {
        EpicEntity[] entityArr;
        for (EpicEntity e : entityArr = this.getAllEntities()) {
            if (!e.isCollectable()) continue;
            this.destroyEntity(e);
        }
    }

    final void destroySources() {
        EpicEntity[] entityArr;
        for (EpicEntity e : entityArr = this.getAllEntities()) {
            if (!e.isSource()) continue;
            this.destroyEntity(e);
        }
    }

    final void respawnEntities() {
        EpicEntity[] entityArr;
        for (EpicEntity e : entityArr = this.getAllEntities()) {
            if (e.isCollectable() || e.isSource()) continue;
            int numSteps = e.resetSteps();
            logger.log(Level.INFO, e.getName() + " took " + numSteps + " steps.");
            e.spawn();
        }
    }

    final void generateCollectables(int nums, String cname, int type) {
        for (int x = -1; x >= -nums; --x) {
            int id = x;
            if (type == 1) {
                id = -100 - x;
            }
            EpicEntity collectable = new EpicEntity(this, id, cname, type);
            MapHex hex = this.getRandomHex();
            collectable.createEntity(0);
            hex.addEntity(collectable);
        }
    }

    boolean doesEntityExist(int entityId) {
        return this.getEntities().containsKey(entityId);
    }

    void setImpossibleWinConditions() {
        this.currentScenario.setCollectiblesToWin(100);
        this.currentScenario.setCollectiblesForWurmToWin(100);
        this.currentScenario.setSpawnPointRequiredToWin(false);
        this.currentScenario.setHexNumRequiredToWin(0);
        this.currentScenario.setScenarioName("");
        this.currentScenario.setScenarioQuest("");
    }

    public int getCollictblesRequiredToWin() {
        return this.currentScenario.getCollectiblesToWin();
    }

    public int getCollictblesRequiredForWurmToWin() {
        return this.currentScenario.getCollectiblesForWurmToWin();
    }

    boolean isSpawnPointRequiredToWin() {
        return this.currentScenario.isSpawnPointRequiredToWin();
    }

    int getHexNumRequiredToWin() {
        return this.currentScenario.getHexNumRequiredToWin();
    }

    int getScenarioNumber() {
        return this.currentScenario.getScenarioNumber();
    }

    void incrementScenarioNumber() {
        this.currentScenario.incrementScenarioNumber();
    }

    Map<Long, EpicEntity> getEntities() {
        return this.entities;
    }

    void sendDemigodRequest(long deityNum, String dname) {
        Servers.requestDemigod((byte)deityNum, dname);
    }

    boolean spawnCreatures(EpicEntity entity) {
        boolean delayedSpawn = false;
        LinkedList<Integer> creatureTemplates = this.controllers.get(entity.getId());
        if (creatureTemplates != null && creatureTemplates.size() > 0) {
            Integer toSpawn = creatureTemplates.get(rand.nextInt(creatureTemplates.size()));
            try {
                String summonString;
                CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(toSpawn);
                switch (Server.rand.nextInt(5)) {
                    case 0: {
                        summonString = "sends forth";
                        break;
                    }
                    case 1: {
                        summonString = "summons";
                        break;
                    }
                    case 2: {
                        summonString = "commands";
                        break;
                    }
                    case 3: {
                        summonString = "brings";
                        break;
                    }
                    case 4: {
                        summonString = "lets loose";
                        break;
                    }
                    default: {
                        summonString = "summons";
                    }
                }
                if (toSpawn == 75) {
                    delayedSpawn = true;
                }
                String effectDesc = entity.getName() + " " + summonString + " the " + ct.getName() + "s.";
                WcEpicEvent wce = new WcEpicEvent(WurmId.getNextWCCommandId(), 0, entity.getId(), toSpawn, 5, effectDesc, false);
                wce.sendFromLoginServer();
                wce.sendToServer(3);
                this.broadCast(effectDesc);
            }
            catch (NoSuchCreatureTemplateException nst) {
                logger.log(Level.WARNING, nst.getMessage());
            }
        }
        return delayedSpawn;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void loadControllers() {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block6: {
            dbcon = null;
            ps = null;
            rs = null;
            try {
                dbcon = DbConnector.getDeityDbCon();
                ps = dbcon.prepareStatement(LOAD_ENTITY_CONTROLLERS);
                rs = ps.executeQuery();
                int found = 0;
                while (rs.next()) {
                    int creatureTemplateId = rs.getInt("CREATURE");
                    long controller = rs.getLong("CONTROLLER");
                    LinkedList<Integer> list = this.controllers.get(controller);
                    if (list == null) {
                        list = new LinkedList();
                    }
                    list.add(creatureTemplateId);
                    this.controllers.put(controller, list);
                    ++found;
                }
                if (found != 0) break block6;
                this.createControlledCreatures();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Problem loading entity controllers due to " + sqx.getMessage(), sqx);
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

    void createControlledCreatures() {
        this.initializeCreatureController(72);
        this.initializeCreatureController(73);
        this.initializeCreatureController(74);
        this.initializeCreatureController(75);
        this.initializeCreatureController(76);
        this.initializeCreatureController(77);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void initializeCreatureController(int creatureTemplateId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(CREATE_ENTITY_CONTROLLER);
            ps.setInt(1, creatureTemplateId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem creating entity controller for creature template " + creatureTemplateId + " due to " + sqx.getMessage(), sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void setCreatureController(int creatureTemplateId, long controller) {
        LinkedList<Integer> list;
        for (Map.Entry<Long, LinkedList<Integer>> me : this.controllers.entrySet()) {
            LinkedList<Integer> creatures = me.getValue();
            if (!creatures.contains(creatureTemplateId)) continue;
            if (me.getKey() == controller) {
                return;
            }
            creatures.remove((Object)creatureTemplateId);
            break;
        }
        if ((list = this.controllers.get(controller)) == null) {
            list = new LinkedList();
        }
        list.add(creatureTemplateId);
        this.controllers.put(controller, list);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(UPDATE_ENTITY_CONTROLLER);
            ps.setLong(1, controller);
            ps.setInt(2, creatureTemplateId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem updating entity controller for creature template " + creatureTemplateId + " due to " + sqx.getMessage(), sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void loadVisitedHexes() {
        logger.info("Starting to load visited hexes for " + this.name);
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int found = 0;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(LOAD_ALL_VISITED_HEX);
            rs = ps.executeQuery();
            while (rs.next()) {
                int hexid = rs.getInt("HEXID");
                long entityId = rs.getLong("ENTITYID");
                MapHex h = this.getMapHex(hexid);
                EpicEntity e = this.getEntity(entityId);
                if (e != null) {
                    h.addVisitedBy(e, true);
                }
                ++found;
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem loading visited hexes due to " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loading " + found + " visited hexes took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loading " + found + " visited hexes took " + (float)(end - start) / 1000000.0f + " ms");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loading " + found + " visited hexes took " + (float)(end - start) / 1000000.0f + " ms");
    }

    static {
        rand = new Random();
        nameRand = new Random();
    }
}

