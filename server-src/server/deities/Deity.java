/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.deities;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Deity
implements MiscConstants {
    public static final byte TYPE_MISSIONAIRY = 0;
    public static final byte TYPE_CHAPLAIN = 1;
    public final int number;
    public final String name;
    public int alignment;
    public final byte sex;
    byte power;
    double faith;
    int favor;
    float attack;
    float vitality;
    private static Logger logger = Logger.getLogger(Deity.class.getName());
    public final int holyItem;
    public String[] convertText1 = new String[10];
    public String[] altarConvertText1 = new String[10];
    private final Set<Spell> spells = new HashSet<Spell>();
    private final Set<Spell> creatureSpells = new HashSet<Spell>();
    private final Set<Spell> itemSpells = new HashSet<Spell>();
    private final Set<Spell> woundSpells = new HashSet<Spell>();
    private final Set<Spell> tileSpells = new HashSet<Spell>();
    private static final String insertKarma = "INSERT INTO HELPERS (WURMID,KARMA,DEITY) VALUES (?,?,?)";
    private static final String updateKarma = "UPDATE HELPERS SET KARMA=?,DEITY=? WHERE WURMID=?";
    private static final String loadKarma = "SELECT * FROM HELPERS WHERE DEITY=?";
    private final ConcurrentHashMap<Long, Float> karmavals = new ConcurrentHashMap();
    private int templateDeity = 0;
    private boolean roadProtector = false;
    private float buildWallBonus = 0.0f;
    private boolean warrior = false;
    private boolean befriendCreature = false;
    private boolean befriendMonster = false;
    private boolean staminaBonus = false;
    private boolean foodBonus = false;
    private boolean healer = false;
    private boolean deathProtector = false;
    private boolean deathItemProtector = false;
    private boolean favorRegenerator = false;
    private boolean allowsButchering = false;
    private boolean woodAffinity = false;
    private boolean metalAffinity = false;
    private boolean clothAffinity = false;
    private boolean clayAffinity = false;
    private boolean meatAffinity = false;
    private boolean foodAffinity = false;
    private boolean learner = false;
    private boolean itemProtector = false;
    private boolean repairer = false;
    private boolean waterGod = false;
    private boolean mountainGod = false;
    private boolean forestGod = false;
    private boolean hateGod = false;
    public int lastConfrontationTileX;
    public int lastConfrontationTileY;
    private int activeFollowers = 0;
    private final Random rand;
    private byte favoredKingdom = 0;

    Deity(int num, String nam, byte align, byte aSex, byte pow, double aFaith, int holyitem, int _favor, float _attack, float _vitality, boolean create) {
        this.number = num;
        this.name = nam;
        this.alignment = align;
        this.sex = aSex;
        this.power = pow;
        this.faith = aFaith;
        this.holyItem = holyitem;
        this.favor = _favor;
        this.attack = _attack;
        this.vitality = _vitality;
        this.rand = new Random(this.number * 1001);
    }

    public final String getHeSheItString() {
        if (this.sex == 0) {
            return "he";
        }
        if (this.sex == 1) {
            return "she";
        }
        return "it";
    }

    public final String getCapHeSheItString() {
        if (this.sex == 0) {
            return "He";
        }
        if (this.sex == 1) {
            return "She";
        }
        return "It";
    }

    public final String getHisHerItsString() {
        if (this.sex == 0) {
            return "his";
        }
        if (this.sex == 1) {
            return "her";
        }
        return "its";
    }

    public final String getHimHerItString() {
        if (this.sex == 0) {
            return "him";
        }
        if (this.sex == 1) {
            return "her";
        }
        return "it";
    }

    public final int getTemplateDeity() {
        return this.templateDeity;
    }

    public final void setTemplateDeity(int templateDeity) {
        this.templateDeity = templateDeity;
    }

    public final boolean accepts(float align) {
        return align >= (float)(this.alignment - 100) && align <= (float)(this.alignment + 100);
    }

    public final boolean isActionFaithful(Action action) {
        short num = action.getNumber();
        return this.isActionFaithful(num);
    }

    final boolean isActionFaithful(int num) {
        if (num == 191) {
            return !this.roadProtector;
        }
        if (num == 174 || num == 172 || num == 524) {
            return this.buildWallBonus <= 0.0f;
        }
        return true;
    }

    public final void punishCreature(Creature performer, int actionNumber) {
        float lPower = 0.0f;
        if (actionNumber == 191) {
            lPower = 0.05f;
        } else if (actionNumber == 174 || actionNumber == 172) {
            lPower = 0.05f;
        } else if (actionNumber == 221) {
            lPower = 0.5f;
        }
        if (lPower > 0.0f) {
            performer.modifyFaith(-lPower);
            try {
                performer.setFavor(performer.getFavor() - lPower);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, performer.getName() + " " + this.name, iox);
            }
        }
    }

    public final boolean performActionOkey(Creature performer, Action action) {
        if (!this.isActionFaithful(action) && Server.rand.nextInt(100) <= 10) {
            this.punishCreature(performer, action.getNumber());
            return false;
        }
        return true;
    }

    public final void removeSpell(Spell spell) {
        this.spells.remove(spell);
        if (spell.isTargetCreature()) {
            this.creatureSpells.remove(spell);
        }
        if (spell.isTargetAnyItem()) {
            this.itemSpells.remove(spell);
        }
        if (spell.isTargetWound()) {
            this.woundSpells.remove(spell);
        }
        if (spell.isTargetTile()) {
            this.tileSpells.remove(spell);
        }
    }

    public final void addSpell(Spell spell) {
        this.spells.add(spell);
        if (spell.isTargetCreature()) {
            this.creatureSpells.add(spell);
        }
        if (spell.isTargetAnyItem()) {
            this.itemSpells.add(spell);
        }
        if (spell.isTargetWound()) {
            this.woundSpells.add(spell);
        }
        if (spell.isTargetTile()) {
            this.tileSpells.add(spell);
        }
    }

    public final boolean hasSpell(Spell spell) {
        return this.spells.contains(spell);
    }

    public final Set<Spell> getSpells() {
        return this.spells;
    }

    public final Spell[] getSpellsTargettingCreatures(int level) {
        HashSet<Spell> toReturn = new HashSet<Spell>();
        for (Spell s : this.creatureSpells) {
            if (s.level > level || s.isRitual && this.getFavor() <= 100000) continue;
            toReturn.add(s);
        }
        Object[] spells = toReturn.toArray(new Spell[toReturn.size()]);
        Arrays.sort(spells);
        return spells;
    }

    public final Spell[] getSpellsTargettingWounds(int level) {
        HashSet<Spell> toReturn = new HashSet<Spell>();
        for (Spell s : this.woundSpells) {
            if (s.level > level || s.isRitual && this.getFavor() <= 100000) continue;
            toReturn.add(s);
        }
        Object[] spells = toReturn.toArray(new Spell[toReturn.size()]);
        Arrays.sort(spells);
        return spells;
    }

    public final Spell[] getSpellsTargettingItems(int level) {
        HashSet<Spell> toReturn = new HashSet<Spell>();
        for (Spell s : this.itemSpells) {
            if (s.level > level || s.isRitual && this.getFavor() <= 100000 && !Servers.isThisATestServer()) continue;
            toReturn.add(s);
        }
        Object[] spells = toReturn.toArray(new Spell[toReturn.size()]);
        Arrays.sort(spells);
        return spells;
    }

    public final Spell[] getSpellsTargettingTiles(int level) {
        HashSet<Spell> toReturn = new HashSet<Spell>();
        for (Spell s : this.tileSpells) {
            if (s.level > level || s.isRitual && this.getFavor() <= 100000) continue;
            toReturn.add(s);
        }
        Object[] spells = toReturn.toArray(new Spell[toReturn.size()]);
        Arrays.sort(spells);
        return spells;
    }

    public int getFavor() {
        return this.favor;
    }

    public void increaseFavor() {
        this.setFavor(this.favor + 1);
    }

    abstract void save() throws IOException;

    abstract void setFaith(double var1) throws IOException;

    public abstract void setFavor(int var1);

    public final String toString() {
        return "Deity [Name: " + this.name + ", Number: " + this.number + ']';
    }

    public boolean isLibila() {
        return this.number == 4;
    }

    public boolean isMagranon() {
        return this.number == 2;
    }

    public boolean isCustomDeity() {
        return this.number > 4;
    }

    public boolean isFo() {
        return this.number == 1;
    }

    public boolean isVynora() {
        return this.number == 3;
    }

    public void setActiveFollowers(int followers) {
        this.activeFollowers = followers;
    }

    public int getActiveFollowers() {
        return this.activeFollowers;
    }

    public double getFaithPerFollower() {
        return this.faith / (double)Math.max(1.0f, (float)this.activeFollowers);
    }

    public int getNumber() {
        return this.number;
    }

    public String getName() {
        return this.name;
    }

    public int getAlignment() {
        return this.alignment;
    }

    public byte getSex() {
        return this.sex;
    }

    public abstract void setPower(byte var1);

    byte getPower() {
        return this.power;
    }

    double getFaith() {
        return this.faith;
    }

    float getAttack() {
        return this.attack;
    }

    float getVitality() {
        return this.vitality;
    }

    public int getHolyItem() {
        return this.holyItem;
    }

    public String[] getConvertText1() {
        return this.convertText1;
    }

    public String[] getAltarConvertText1() {
        return this.altarConvertText1;
    }

    public boolean isRoadProtector() {
        return this.roadProtector;
    }

    public void setRoadProtector(boolean roadProtector) {
        this.roadProtector = roadProtector;
    }

    public float getBuildWallBonus() {
        return this.buildWallBonus;
    }

    public void setBuildWallBonus(float buildWallBonus) {
        this.buildWallBonus = buildWallBonus;
    }

    public boolean isWarrior() {
        return this.warrior;
    }

    public void setWarrior(boolean warrior) {
        this.warrior = warrior;
    }

    public boolean isFavorRegenerator() {
        return this.favorRegenerator;
    }

    public void setFavorRegenerator(boolean favorRegenerator) {
        this.favorRegenerator = favorRegenerator;
    }

    public boolean isBefriendCreature() {
        return this.befriendCreature;
    }

    public void setBefriendCreature(boolean befriendCreature) {
        this.befriendCreature = befriendCreature;
    }

    public boolean isBefriendMonster() {
        return this.befriendMonster;
    }

    public void setBefriendMonster(boolean befriendMonster) {
        this.befriendMonster = befriendMonster;
    }

    public boolean isStaminaBonus() {
        return this.staminaBonus;
    }

    public void setStaminaBonus(boolean staminaBonus) {
        this.staminaBonus = staminaBonus;
    }

    public boolean isFoodBonus() {
        return this.foodBonus;
    }

    public void setFoodBonus(boolean foodBonus) {
        this.foodBonus = foodBonus;
    }

    public boolean isHealer() {
        return this.healer;
    }

    public void setHealer(boolean healer) {
        this.healer = healer;
    }

    public boolean isDeathProtector() {
        return this.deathProtector;
    }

    public void setDeathProtector(boolean deathProtector) {
        this.deathProtector = deathProtector;
    }

    public boolean isDeathItemProtector() {
        return this.deathItemProtector;
    }

    public void setDeathItemProtector(boolean deathItemProtector) {
        this.deathItemProtector = deathItemProtector;
    }

    public boolean isAllowsButchering() {
        return this.allowsButchering;
    }

    public void setAllowsButchering(boolean allowsButchering) {
        this.allowsButchering = allowsButchering;
    }

    public boolean isWoodAffinity() {
        return this.woodAffinity;
    }

    public void setWoodAffinity(boolean woodAffinity) {
        this.woodAffinity = woodAffinity;
    }

    public boolean isMetalAffinity() {
        return this.metalAffinity;
    }

    public void setMetalAffinity(boolean metalAffinity) {
        this.metalAffinity = metalAffinity;
    }

    public boolean isClothAffinity() {
        return this.clothAffinity;
    }

    public void setClothAffinity(boolean clothAffinity) {
        this.clothAffinity = clothAffinity;
    }

    public boolean isClayAffinity() {
        return this.clayAffinity;
    }

    public void setClayAffinity(boolean clayAffinity) {
        this.clayAffinity = clayAffinity;
    }

    public boolean isMeatAffinity() {
        return this.meatAffinity;
    }

    public void setMeatAffinity(boolean meatAffinity) {
        this.meatAffinity = meatAffinity;
    }

    public boolean isFoodAffinity() {
        return this.foodAffinity;
    }

    public void setFoodAffinity(boolean foodAffinity) {
        this.foodAffinity = foodAffinity;
    }

    public boolean isLearner() {
        return this.learner;
    }

    public void setLearner(boolean learner) {
        this.learner = learner;
    }

    public boolean isItemProtector() {
        return this.itemProtector;
    }

    public void setItemProtector(boolean itemProtector) {
        this.itemProtector = itemProtector;
    }

    public boolean isRepairer() {
        return this.repairer;
    }

    public void setRepairer(boolean repairer) {
        this.repairer = repairer;
    }

    public boolean isWaterGod() {
        return this.waterGod;
    }

    public void setWaterGod(boolean waterGod) {
        this.waterGod = waterGod;
    }

    public boolean isMountainGod() {
        return this.mountainGod;
    }

    public void setMountainGod(boolean mountainGod) {
        this.mountainGod = mountainGod;
    }

    public boolean isForestGod() {
        return this.forestGod;
    }

    public void setForestGod(boolean forestGod) {
        this.forestGod = forestGod;
    }

    public boolean isHateGod() {
        return this.hateGod;
    }

    public void setHateGod(boolean hateGod) {
        this.hateGod = hateGod;
    }

    public int getLastConfrontationTileX() {
        return this.lastConfrontationTileX;
    }

    public int getLastConfrontationTileY() {
        return this.lastConfrontationTileY;
    }

    public final void clearKarma() {
        this.karmavals.clear();
    }

    public final ConcurrentHashMap<Long, Float> getHelpers() {
        return this.karmavals;
    }

    public final long getBestHelper(boolean wipeKarma) {
        int totalTickets = 0;
        for (float i : this.karmavals.values()) {
            if (!(i >= 300.0f)) continue;
            totalTickets += (int)(i / 300.0f);
        }
        int currentTicket = 0;
        long[] tickets = new long[totalTickets];
        for (Map.Entry<Long, Float> entry : this.karmavals.entrySet()) {
            if (!(entry.getValue().floatValue() >= 300.0f)) continue;
            int totalNum = (int)(entry.getValue().floatValue() / 300.0f);
            for (int i = 0; i < totalNum; ++i) {
                tickets[currentTicket++] = entry.getKey();
            }
        }
        int winningTicket = Server.rand.nextInt(totalTickets < 1 ? 1 : totalTickets);
        if (winningTicket < tickets.length) {
            if (wipeKarma) {
                this.karmavals.replace(tickets[winningTicket], Float.valueOf(0.0f));
            }
            return tickets[winningTicket];
        }
        return -10L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void loadAllKarmaHelpers() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(loadKarma);
            ps.setInt(1, this.number);
            rs = ps.executeQuery();
            while (rs.next()) {
                this.karmavals.put(rs.getLong("WURMID"), Float.valueOf(rs.getInt("KARMA")));
            }
            ps.close();
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
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void setPlayerKarma(long pid, int value) {
        block8: {
            PreparedStatement ps;
            Connection dbcon;
            Float karmaValue;
            Long playerId;
            block9: {
                playerId = pid;
                karmaValue = Float.valueOf(value);
                dbcon = null;
                ps = null;
                if (!((ConcurrentHashMap.KeySetView)this.karmavals.keySet()).contains(playerId)) break block9;
                this.karmavals.put(playerId, karmaValue);
                try {
                    dbcon = DbConnector.getDeityDbCon();
                    ps = dbcon.prepareStatement(updateKarma);
                    ps.setInt(1, value);
                    ps.setInt(2, this.number);
                    ps.setLong(3, pid);
                    ps.executeUpdate();
                    ps.close();
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
                    break block8;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                break block8;
            }
            this.karmavals.put(playerId, karmaValue);
            try {
                dbcon = DbConnector.getDeityDbCon();
                ps = dbcon.prepareStatement(insertKarma);
                ps.setLong(1, pid);
                ps.setInt(2, value);
                ps.setInt(3, this.number);
                ps.executeUpdate();
                ps.close();
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
        }
    }

    public Random initializeAndGetRand() {
        this.rand.setSeed(this.number * 1001);
        return this.rand;
    }

    public Random getRand() {
        return this.rand;
    }

    public final void setMaxKingdom() {
        Player[] players = Players.getInstance().getPlayers();
        if (players.length > 10 || Servers.localServer.testServer) {
            HashMap<Byte, Integer> maxWorshippers = new HashMap<Byte, Integer>();
            for (Player lPlayer : players) {
                if (!lPlayer.isPaying() || lPlayer.getDeity() == null || lPlayer.getDeity().number != this.getNumber()) continue;
                Integer curr = (Integer)maxWorshippers.get(lPlayer.getKingdomId());
                curr = curr == null ? Integer.valueOf(1) : Integer.valueOf(curr + 1);
                maxWorshippers.put(lPlayer.getKingdomId(), curr);
            }
            byte maxKingdom = 0;
            int maxNums = 0;
            for (Map.Entry me : maxWorshippers.entrySet()) {
                int nums = (Integer)me.getValue();
                if (nums <= maxNums) continue;
                maxNums = nums;
                maxKingdom = (Byte)me.getKey();
            }
            Kingdom k = Kingdoms.getKingdom(maxKingdom);
            if (k != null) {
                this.setFavoredKingdom(k.getTemplate());
            }
            this.setFavoredKingdom(maxKingdom);
        }
    }

    public byte getFavoredKingdom() {
        return this.favoredKingdom;
    }

    public void setFavoredKingdom(byte fKingdom) {
        EpicMission mission;
        if (fKingdom != this.favoredKingdom && (mission = EpicServerStatus.getEpicMissionForEntity(this.getNumber())) != null) {
            Players.getInstance().sendUpdateEpicMission(mission);
        }
        this.favoredKingdom = fKingdom;
    }
}

