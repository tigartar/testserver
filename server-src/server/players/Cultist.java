/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Cultist
implements MiscConstants,
TimeConstants {
    private final long wurmid;
    private long lastMeditated = 0L;
    private long lastReceivedLevel = 0L;
    private long lastAppointedLevel = 0L;
    private long lastEnlightened = 0L;
    private long cooldown1 = 0L;
    private long cooldown2 = 0L;
    private long cooldown3 = 0L;
    private long cooldown4 = 0L;
    private long cooldown5 = 0L;
    private long cooldown6 = 0L;
    private long cooldown7 = 0L;
    private byte skillgainCount = 0;
    private byte level = 0;
    private byte path = 0;
    private boolean sendUseBody = false;
    private boolean bLoveEff = false;
    private boolean bWarDam = false;
    private boolean bStructDam = false;
    private boolean bFear = false;
    private boolean bNoElem = false;
    private boolean bTraps = false;
    private static final Logger logger = Logger.getLogger(Cultist.class.getName());
    private static final String GET_ALL_CULTISTS = "SELECT * FROM CULT";
    private static final String UPDATE_CULTIST = "UPDATE CULT SET LASTMEDITATED=?, LASTRECEIVEDLEVEL=?, LASTAPPOINTEDLEVEL=?, LEVEL=?, PATH=?, COOLDOWN1=?, COOLDOWN2=?, COOLDOWN3=?, COOLDOWN4=?,COOLDOWN5=?,COOLDOWN6=?,COOLDOWN7=? WHERE WURMID=?";
    private static final String CREATE_CULTIST = "INSERT INTO CULT (LASTMEDITATED, LASTRECEIVEDLEVEL, LASTAPPOINTEDLEVEL, LEVEL, PATH,COOLDOWN1,COOLDOWN2,COOLDOWN3,COOLDOWN4,COOLDOWN5,    COOLDOWN6,COOLDOWN7, WURMID) VALUES (?,?,?,?,?,?,?,?,?,?,   ?,?,?)";
    private static final String DELETE_CULTIST = "DELETE FROM CULT WHERE WURMID=?";
    private static final Map<Long, Cultist> CULTISTS = new HashMap<Long, Cultist>();

    public Cultist(long _wurmid, byte _path) {
        this.wurmid = _wurmid;
        this.path = _path;
        this.lastReceivedLevel = System.currentTimeMillis();
        this.skillgainCount = 1;
        try {
            this.saveCultist(true);
            CULTISTS.put(this.wurmid, this);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
    }

    public Cultist(long _wurmid, long _lastMeditated, long _lastReceivedLevel, long _lastAppointedLevel, byte _level, byte _path, long _cd1, long _cd2, long _cd3, long _cd4, long _cd5, long _cd6, long _cd7) {
        this.wurmid = _wurmid;
        this.lastMeditated = _lastMeditated;
        this.lastReceivedLevel = _lastReceivedLevel;
        this.lastAppointedLevel = _lastAppointedLevel;
        this.level = _level;
        this.path = _path;
        this.cooldown1 = _cd1;
        this.cooldown2 = _cd2;
        this.cooldown3 = _cd3;
        this.cooldown4 = _cd4;
        this.cooldown5 = _cd5;
        this.cooldown6 = _cd6;
        this.cooldown7 = _cd7;
        CULTISTS.put(this.wurmid, this);
        this.bLoveEff = this.hasLoveEffect();
        this.bWarDam = this.doubleWarDamage();
        this.bStructDam = this.doubleStructDamage();
        this.bFear = this.hasFearEffect();
        this.bNoElem = this.hasNoElementalDamage();
        this.bTraps = this.ignoresTraps();
    }

    public long getLastEnlightened() {
        return this.lastEnlightened;
    }

    public void setLastEnlightened(long aLastEnlightened) {
        this.lastEnlightened = aLastEnlightened;
    }

    public void sendBuffs() {
        try {
            int timeLeft;
            Creature cultist = Server.getInstance().getCreature(this.wurmid);
            if (this.hasLoveEffect()) {
                int leLeft = this.getLoveEffectTimeLeftSeconds();
                cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.LOVE_EFFECT, leLeft, 100.0f);
            }
            if (this.doubleWarDamage()) {
                timeLeft = this.getDoubleWarDamageTimeLeftSeconds();
                cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.HATE_DOUBLE_WAR, timeLeft, 100.0f);
            }
            if (this.doubleStructDamage()) {
                timeLeft = this.getDoubleStructDamageTimeLeftSeconds();
                cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.HATE_DOUBLE_STRUCT, timeLeft, 100.0f);
            }
            if (this.hasFearEffect()) {
                timeLeft = this.getFearEffectTimeLeftSeconds();
                cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.HATE_FEAR_EFFECT, timeLeft, 100.0f);
            }
            if (this.hasNoElementalDamage()) {
                timeLeft = this.getElementalImmunityTimeLeftSeconds();
                cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POWER_NO_ELEMENTAL, timeLeft, 100.0f);
            }
            if (this.ignoresTraps()) {
                int trapsLeft = this.getIgnoreTrapsTimeLeftSeconds();
                cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POWER_IGNORE_TRAPS, trapsLeft, 100.0f);
            }
            this.sendPassiveBuffs(cultist, false, false, false, false, false, false);
        }
        catch (NoSuchPlayerException noSuchPlayerException) {
        }
        catch (NoSuchCreatureException noSuchCreatureException) {
            // empty catch block
        }
    }

    private void sendPassiveBuffs(Creature cultist, boolean sentRegeneration, boolean sentSpellImmunity, boolean sentNoStaminaUse, boolean sentNoDecay, boolean sentIncreasedSkilGain, boolean sentShieldGone) {
        if (this.healsFaster() && !sentRegeneration) {
            cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.LOVE_HEALING_HANDS, -1, 100.0f);
        }
        if (this.ignoresSpells() && !sentSpellImmunity) {
            cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.HATE_SPELL_IMMUNITY, -1, 100.0f);
        }
        if (this.usesNoStamina() && !sentNoStaminaUse) {
            cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POWER_USES_LESS_STAMINA, -1, 100.0f);
        }
        if (this.isNoDecay() && !sentNoDecay) {
            cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.KNOWLEDGE_NO_DECAY, -1, 100.0f);
        }
        if (this.levelElevenSkillgain() && !sentIncreasedSkilGain) {
            cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.KNOWLEDGE_INCREASED_SKILL_GAIN, -1, 100.0f);
        }
        if (this.getHalfDamagePercentage() > 0.0f && !sentShieldGone) {
            cultist.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.INSANITY_SHIELD_GONE, -1, 100.0f * this.getHalfDamagePercentage());
        }
    }

    public final int getDoubleStructDamageTimeLeftSeconds() {
        return (int)(900000L - (System.currentTimeMillis() - this.cooldown2)) / 1000;
    }

    public final int getDoubleWarDamageTimeLeftSeconds() {
        return (int)(900000L - (System.currentTimeMillis() - this.cooldown1)) / 1000;
    }

    public final int getFearEffectTimeLeftSeconds() {
        return (int)(180000L - (System.currentTimeMillis() - this.cooldown3)) / 1000;
    }

    public final int getElementalImmunityTimeLeftSeconds() {
        return (int)(1800000L - (System.currentTimeMillis() - this.cooldown1)) / 1000;
    }

    public final int getIgnoreTrapsTimeLeftSeconds() {
        return (int)(1800000L - (System.currentTimeMillis() - this.cooldown3)) / 1000;
    }

    public final int getLoveEffectTimeLeftSeconds() {
        return (int)(180000L - (System.currentTimeMillis() - this.cooldown3)) / 1000;
    }

    public long getWurmId() {
        return this.wurmid;
    }

    public long getTimeLeftToIncreasePath(long currentTime, double meditationSkill) {
        long time = currentTime - this.lastReceivedLevel;
        float modifier = (double)(this.level * 15) - meditationSkill < 0.0 ? 0.5f : 1.0f;
        long neededTime = 0L;
        switch (this.level) {
            case 0: {
                if (this.lastReceivedLevel > 0L) {
                    neededTime = 43200000L;
                    break;
                }
                neededTime = 0L;
                break;
            }
            case 1: {
                neededTime = 86400000L;
                break;
            }
            default: {
                neededTime = Math.min(1555200000L, (long)((float)(this.level * this.level) / 2.0f * 8.64E7f));
            }
        }
        return (long)((float)neededTime * modifier - (float)time);
    }

    public static final Map<Integer, Set<Cultist>> getCultistLeaders(byte inpath, int kingdom) {
        TreeMap<Integer, Set<Cultist>> toReturn = new TreeMap<Integer, Set<Cultist>>();
        for (Cultist cultist : CULTISTS.values()) {
            PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(cultist.getWurmId());
            if (cultist.path != inpath || cultist.level <= 3) continue;
            boolean show = false;
            if (pInfo != null) {
                try {
                    byte kingdomId;
                    pInfo.load();
                    if (pInfo.currentServer == Servers.localServer.id && (kingdomId = Players.getInstance().getKingdomForPlayer(pInfo.wurmId)) == kingdom) {
                        show = true;
                    }
                }
                catch (IOException kingdomId) {
                    // empty catch block
                }
            }
            if (!show) continue;
            HashSet<Cultist> subSet = (HashSet<Cultist>)toReturn.get(cultist.level);
            if (subSet == null) {
                subSet = new HashSet<Cultist>();
            }
            subSet.add(cultist);
            toReturn.put(Integer.valueOf(cultist.level), subSet);
        }
        return toReturn;
    }

    public String getCultistTitle() {
        return Cults.getNameForLevel(this.path, this.level) + " of " + Cults.getPathNameFor(this.path);
    }

    public String getCultistTitleShort() {
        return "the " + Cults.getNameForLevel(this.path, this.level);
    }

    public void failedToLevel() {
        this.lastReceivedLevel = System.currentTimeMillis();
        try {
            this.saveCultist(false);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
    }

    public void increaseLevel() {
        boolean sentNoStamina = this.usesNoStamina();
        boolean sentSpellImmunity = this.ignoresSpells();
        boolean sentRegeneration = this.healsFaster();
        boolean sentNoDecay = this.isNoDecay();
        boolean sentSkillGain = this.levelElevenSkillgain();
        boolean sentShieldGone = this.getHalfDamagePercentage() > 0.0f;
        this.setLevel((byte)(this.level + 1));
        this.lastReceivedLevel = System.currentTimeMillis();
        try {
            String lg;
            Creature cultist = Server.getInstance().getCreature(this.wurmid);
            cultist.getCommunicator().sendSafeServerMessage("Congratulations! You have now reached the level of " + this.getCultistTitle() + "!", (byte)2);
            Server.getInstance().broadCastAction(cultist.getName() + " has reached the level of " + this.getCultistTitle() + "!", cultist, 5);
            cultist.refreshVisible();
            cultist.getCommunicator().sendOwnTitles();
            if (this.level == 4) {
                cultist.achievement(570);
            }
            if (this.level == 7) {
                cultist.achievement(578);
            }
            if (this.level == 9) {
                cultist.achievement(599);
            }
            if (!(lg = this.getLevelGainString()).equals("")) {
                cultist.getCommunicator().sendSafeServerMessage(lg);
                if (this.sendUseBody) {
                    cultist.getCommunicator().sendSafeServerMessage("Use your body to activate this knowledge.");
                }
                this.sendUseBody = false;
                this.sendPassiveBuffs(cultist, sentRegeneration, sentSpellImmunity, sentNoStamina, sentNoDecay, sentSkillGain, sentShieldGone);
            }
        }
        catch (NoSuchCreatureException noSuchCreatureException) {
        }
        catch (NoSuchPlayerException noSuchPlayerException) {
            // empty catch block
        }
    }

    void setLastMeditated() {
        this.lastMeditated = System.currentTimeMillis();
    }

    public long getLastMeditated() {
        return this.lastMeditated;
    }

    public void setLastMeditated(long aLastMeditated) {
        this.lastMeditated = aLastMeditated;
    }

    void increaseSkillgain() {
        this.skillgainCount = (byte)(this.skillgainCount + 1);
    }

    void decreaseSkillGain() {
        this.skillgainCount = (byte)(this.skillgainCount - 1);
    }

    public static final Cultist getCultist(long wid) {
        return CULTISTS.get(wid);
    }

    public static final void resetSkillGain() throws IOException {
        for (Cultist c : CULTISTS.values()) {
            c.skillgainCount = 0;
        }
    }

    public static final void loadAllCultists() throws IOException {
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_ALL_CULTISTS);
            rs = ps.executeQuery();
            while (rs.next()) {
                new Cultist(rs.getLong("WURMID"), rs.getLong("LASTMEDITATED"), rs.getLong("LASTRECEIVEDLEVEL"), rs.getLong("LASTAPPOINTEDLEVEL"), rs.getByte("LEVEL"), rs.getByte("PATH"), rs.getLong("COOLDOWN1"), rs.getLong("COOLDOWN2"), rs.getLong("COOLDOWN3"), rs.getLong("COOLDOWN4"), rs.getLong("COOLDOWN5"), rs.getLong("COOLDOWN6"), rs.getLong("COOLDOWN7"));
            }
        }
        catch (SQLException sqex) {
            try {
                throw new IOException("Failed to load cultists", sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                logger.log(Level.INFO, "Loaded all cultists. It took " + lElapsedTime + " millis.");
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
        logger.log(Level.INFO, "Loaded all cultists. It took " + lElapsedTime + " millis.");
    }

    public final void saveCultist(boolean createNew) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = createNew ? dbcon.prepareStatement(CREATE_CULTIST) : dbcon.prepareStatement(UPDATE_CULTIST);
            ps.setLong(1, this.lastMeditated);
            ps.setLong(2, this.lastReceivedLevel);
            ps.setLong(3, this.lastAppointedLevel);
            ps.setByte(4, this.level);
            ps.setByte(5, this.path);
            ps.setLong(6, this.cooldown1);
            ps.setLong(7, this.cooldown2);
            ps.setLong(8, this.cooldown3);
            ps.setLong(9, this.cooldown4);
            ps.setLong(10, this.cooldown5);
            ps.setLong(11, this.cooldown6);
            ps.setLong(12, this.cooldown7);
            ps.setLong(13, this.wurmid);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                throw new IOException("Failed to save cultist " + this.wurmid, sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    public final void deleteCultist() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_CULTIST);
            ps.setLong(1, this.wurmid);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                throw new IOException("Failed to save cultist " + this.wurmid, sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        CULTISTS.remove(this.wurmid);
    }

    public final void touchCooldown1() {
        this.cooldown1 = System.currentTimeMillis();
        try {
            this.saveCultist(false);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, this.wurmid + " " + iox.getMessage(), iox);
        }
        this.bWarDam = this.doubleWarDamage();
        this.bNoElem = this.hasNoElementalDamage();
    }

    public final void touchCooldown2() {
        this.cooldown2 = System.currentTimeMillis();
        try {
            this.saveCultist(false);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, this.wurmid + " " + iox.getMessage(), iox);
        }
        this.bStructDam = this.doubleStructDamage();
    }

    public final void touchCooldown3() {
        this.cooldown3 = System.currentTimeMillis();
        try {
            this.saveCultist(false);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, this.wurmid + " " + iox.getMessage(), iox);
        }
        this.bLoveEff = this.hasLoveEffect();
        this.bFear = this.hasFearEffect();
        this.bTraps = this.ignoresTraps();
    }

    public final void touchCooldown4() {
        this.cooldown4 = System.currentTimeMillis();
        try {
            this.saveCultist(false);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, this.wurmid + " " + iox.getMessage(), iox);
        }
    }

    public final void touchCooldown5() {
        this.cooldown5 = System.currentTimeMillis();
        try {
            this.saveCultist(false);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, this.wurmid + " " + iox.getMessage(), iox);
        }
    }

    public final void touchCooldown6() {
        this.cooldown6 = System.currentTimeMillis();
        try {
            this.saveCultist(false);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, this.wurmid + " " + iox.getMessage(), iox);
        }
    }

    public final void touchCooldown7() {
        this.cooldown7 = System.currentTimeMillis();
        try {
            this.saveCultist(false);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, this.wurmid + " " + iox.getMessage(), iox);
        }
    }

    private boolean skipsCooldown() {
        try {
            Creature cultist = Server.getInstance().getCreature(this.wurmid);
            if (Servers.localServer.testServer && cultist.getPower() >= 5) {
                return true;
            }
        }
        catch (NoSuchPlayerException | NoSuchCreatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean mayRefresh() {
        return this.path == 1 && this.level > 3 && (System.currentTimeMillis() - this.cooldown1 > 64800000L || this.skipsCooldown());
    }

    public boolean mayEnchantNature() {
        return this.path == 1 && this.level > 6 && (System.currentTimeMillis() - this.cooldown2 > 64800000L || this.skipsCooldown());
    }

    public boolean hasLoveEffect() {
        return this.path == 1 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 < 180000L;
    }

    public boolean mayStartLoveEffect() {
        return this.path == 1 && this.level > 8 && (System.currentTimeMillis() - this.cooldown3 > 64800000L || this.skipsCooldown());
    }

    public boolean healsFaster() {
        return this.path == 1 && this.level > 10;
    }

    public boolean doubleWarDamage() {
        return this.path == 2 && this.level > 6 && System.currentTimeMillis() - this.cooldown1 < 900000L;
    }

    public boolean mayStartDoubleWarDamage() {
        return this.path == 2 && this.level > 6 && (System.currentTimeMillis() - this.cooldown1 > 64800000L || this.skipsCooldown());
    }

    public boolean doubleStructDamage() {
        return this.path == 2 && this.level > 3 && System.currentTimeMillis() - this.cooldown2 < 900000L;
    }

    public boolean mayStartDoubleStructDamage() {
        return this.path == 2 && this.level > 3 && (System.currentTimeMillis() - this.cooldown2 > 64800000L || this.skipsCooldown());
    }

    public boolean hasFearEffect() {
        return this.path == 2 && this.level > 8 && System.currentTimeMillis() - this.cooldown3 < 180000L;
    }

    public boolean mayStartFearEffect() {
        return this.path == 2 && this.level > 8 && (System.currentTimeMillis() - this.cooldown3 > 64800000L || this.skipsCooldown());
    }

    public boolean ignoresSpells() {
        return this.path == 2 && this.level > 10;
    }

    public boolean hasNoElementalDamage() {
        return this.path == 5 && this.level > 8 && System.currentTimeMillis() - this.cooldown1 < 1800000L;
    }

    public boolean mayStartNoElementalDamage() {
        return this.path == 5 && this.level > 8 && (System.currentTimeMillis() - this.cooldown1 > 64800000L || this.skipsCooldown());
    }

    public boolean maySpawnVolcano() {
        return this.path == 5 && this.level > 6 && (System.currentTimeMillis() - this.cooldown2 > 64800000L || this.skipsCooldown());
    }

    public boolean ignoresTraps() {
        return this.path == 5 && this.level > 3 && System.currentTimeMillis() - this.cooldown3 < 1800000L;
    }

    public boolean mayStartIgnoreTraps() {
        return this.path == 5 && this.level > 3 && (System.currentTimeMillis() - this.cooldown3 > 64800000L || this.skipsCooldown());
    }

    public boolean usesNoStamina() {
        return this.path == 5 && this.level > 10;
    }

    public boolean mayCreatureInfo() {
        return this.path == 3 && this.level > 3 && (System.currentTimeMillis() - this.cooldown1 > 64800000L || this.skipsCooldown());
    }

    public boolean mayInfoLocal() {
        return this.path == 3 && this.level > 6 && (System.currentTimeMillis() - this.cooldown2 > 64800000L || this.skipsCooldown());
    }

    public boolean isNoDecay() {
        return this.path == 3 && this.level > 8;
    }

    public boolean levelElevenSkillgain() {
        return this.path == 3 && this.level > 10;
    }

    public boolean mayCleanWounds() {
        return this.path == 4 && this.level > 3 && (System.currentTimeMillis() - this.cooldown1 > 3600000L || this.skipsCooldown());
    }

    public boolean mayRecall() {
        return this.level > 11 && System.currentTimeMillis() - this.cooldown4 > 3600000L * (long)Math.max(1, 12 - (this.level - 12));
    }

    public boolean mayDealFinalBreath() {
        return this.level > 12 && ((float)(System.currentTimeMillis() - this.cooldown5) > 3600000.0f * Math.max(1.0f, 2.0f - (float)Math.max(0, this.level - 13) * 0.1f) || this.skipsCooldown());
    }

    public boolean mayFillup() {
        return this.path == 4 && this.level > 6 && (System.currentTimeMillis() - this.cooldown2 > 64800000L || this.skipsCooldown());
    }

    public boolean mayTeleport() {
        return this.path == 4 && this.level > 8 && (System.currentTimeMillis() - this.cooldown3 > 3600000L || this.skipsCooldown());
    }

    public void sendEffectEnd(String toSend, SpellEffectsEnum effect) {
        try {
            Creature cultist = Server.getInstance().getCreature(this.wurmid);
            cultist.getCommunicator().sendAlertServerMessage(toSend);
            cultist.getCommunicator().sendRemoveSpellEffect(effect);
        }
        catch (NoSuchCreatureException noSuchCreatureException) {
        }
        catch (NoSuchPlayerException noSuchPlayerException) {
            // empty catch block
        }
    }

    public void poll() {
        if (this.bLoveEff && !this.hasLoveEffect()) {
            this.bLoveEff = false;
            this.sendEffectEnd("The stream of love fades.", SpellEffectsEnum.LOVE_EFFECT);
        }
        if (this.bWarDam && !this.doubleWarDamage()) {
            this.bWarDam = false;
            this.sendEffectEnd("You calm down.", SpellEffectsEnum.HATE_DOUBLE_WAR);
        }
        if (this.bStructDam && !this.doubleStructDamage()) {
            this.bStructDam = false;
            this.sendEffectEnd("Your rage goes away.", SpellEffectsEnum.HATE_DOUBLE_STRUCT);
        }
        if (this.bFear && !this.hasFearEffect()) {
            this.bFear = false;
            this.sendEffectEnd("You are no longer as fearful.", SpellEffectsEnum.HATE_FEAR_EFFECT);
        }
        if (this.bNoElem && !this.hasNoElementalDamage()) {
            this.bNoElem = false;
            this.sendEffectEnd("You are no longer protected from the elements.", SpellEffectsEnum.POWER_NO_ELEMENTAL);
        }
        if (this.bTraps && !this.ignoresTraps()) {
            this.bTraps = false;
            this.sendEffectEnd("You no longer focus on traps.", SpellEffectsEnum.POWER_IGNORE_TRAPS);
        }
    }

    public boolean hasHalfDamage() {
        return this.path == 4 && this.level > 10;
    }

    public float getHalfDamagePercentage() {
        if (this.path != 4 || this.level < 7) {
            return 0.0f;
        }
        return Math.min(1.0f, Math.max(0.0f, (float)(this.level - 6) * 20.0f) / 100.0f);
    }

    public String getLevelGainString() {
        String toReturn = "";
        this.sendUseBody = false;
        block0 : switch (this.path) {
            case 1: {
                switch (this.level) {
                    case 4: {
                        toReturn = "You may now refresh people.";
                        break block0;
                    }
                    case 7: {
                        toReturn = "You may now enchant nature.";
                        break block0;
                    }
                    case 9: {
                        toReturn = "Your love may now protect you from most vile enemies for a short while.";
                        this.sendUseBody = true;
                        break block0;
                    }
                    case 11: {
                        toReturn = "You now heal faster.";
                        break block0;
                    }
                    case 12: {
                        toReturn = "You may now recall home.";
                        break block0;
                    }
                    case 13: {
                        toReturn = "Your willpower now gives you the ability to deal a powerful short range blow to your enemies.";
                        break block0;
                    }
                }
                toReturn = "";
                break;
            }
            case 2: {
                switch (this.level) {
                    case 4: {
                        toReturn = "You may now harm structures more for a while.";
                        this.sendUseBody = true;
                        break block0;
                    }
                    case 7: {
                        toReturn = "You may now rage, doing more harm in combat.";
                        this.sendUseBody = true;
                        break block0;
                    }
                    case 9: {
                        toReturn = "You may now spread fear for a short while, protecting you.";
                        this.sendUseBody = true;
                        break block0;
                    }
                    case 11: {
                        toReturn = "You now ignore aggressive spells targeted directly at you.";
                        break block0;
                    }
                    case 12: {
                        toReturn = "You may now recall home.";
                        break block0;
                    }
                    case 13: {
                        toReturn = "Your willpower now gives you the ability to deal a powerful short range blow to your enemies.";
                        break block0;
                    }
                }
                toReturn = "";
                break;
            }
            case 4: {
                switch (this.level) {
                    case 4: {
                        toReturn = "You find a new interest in cleaning dirty wounds.";
                        break block0;
                    }
                    case 7: {
                        toReturn = "You come to the conclusion that you need to eat less now.";
                        this.sendUseBody = true;
                        break block0;
                    }
                    case 9: {
                        toReturn = "You realize that you can fly.";
                        this.sendUseBody = true;
                        break block0;
                    }
                    case 11: {
                        toReturn = "You now stand above physical damage.";
                        break block0;
                    }
                    case 12: {
                        toReturn = "You may now recall home.";
                        break block0;
                    }
                    case 13: {
                        toReturn = "Your willpower now gives you the ability to deal a powerful short range blow to your enemies.";
                        break block0;
                    }
                }
                toReturn = "";
                break;
            }
            case 3: {
                switch (this.level) {
                    case 4: {
                        toReturn = "You have received deep insights in physiology.";
                        break block0;
                    }
                    case 7: {
                        toReturn = "You are now attuned to the surrounding area.";
                        break block0;
                    }
                    case 9: {
                        toReturn = "You understand how to cement your knowledge, never forgetting anything. You also feel the skills of creatures.";
                        break block0;
                    }
                    case 11: {
                        toReturn = "You now have mastered the learning process, and learn immensely fast.";
                        break block0;
                    }
                    case 12: {
                        toReturn = "You may now recall home.";
                        break block0;
                    }
                    case 13: {
                        toReturn = "Your willpower now gives you the ability to deal a powerful short range blow to your enemies.";
                        break block0;
                    }
                }
                toReturn = "";
                break;
            }
            case 5: {
                switch (this.level) {
                    case 4: {
                        toReturn = "You may heighten your senses for a while, avoiding traps.";
                        this.sendUseBody = true;
                        break block0;
                    }
                    case 7: {
                        toReturn = "You attune to the earth, and may spawn magma.";
                        break block0;
                    }
                    case 9: {
                        toReturn = "You may now sometimes ignore elemental damage such as from fire, ice and even water.";
                        this.sendUseBody = true;
                        break block0;
                    }
                    case 11: {
                        toReturn = "You can now work tirelessly.";
                        break block0;
                    }
                    case 12: {
                        toReturn = "You may now recall home.";
                        break block0;
                    }
                    case 13: {
                        toReturn = "Your willpower now gives you the ability to deal a powerful short range blow to your enemies.";
                        break block0;
                    }
                }
                toReturn = "";
                break;
            }
            default: {
                toReturn = "";
            }
        }
        return toReturn;
    }

    public long getLastReceivedLevel() {
        return this.lastReceivedLevel;
    }

    public void setLastReceivedLevel(long aLastReceivedLevel) {
        this.lastReceivedLevel = aLastReceivedLevel;
    }

    public long getLastAppointedLevel() {
        return this.lastAppointedLevel;
    }

    public void setLastAppointedLevel(long aLastAppointedLevel) {
        this.lastAppointedLevel = aLastAppointedLevel;
    }

    public long getCooldown1() {
        return this.cooldown1;
    }

    public void setCooldown1(long aCooldown1) {
        this.cooldown1 = aCooldown1;
    }

    public long getCooldown2() {
        return this.cooldown2;
    }

    public void setCooldown2(long aCooldown2) {
        this.cooldown2 = aCooldown2;
    }

    public long getCooldown3() {
        return this.cooldown3;
    }

    public void setCooldown3(long aCooldown3) {
        this.cooldown3 = aCooldown3;
    }

    public long getCooldown4() {
        return this.cooldown4;
    }

    public void setCooldown4(long aCooldown4) {
        this.cooldown4 = aCooldown4;
    }

    public long getCooldown5() {
        return this.cooldown5;
    }

    public void setCooldown5(long aCooldown5) {
        this.cooldown5 = aCooldown5;
    }

    public long getCooldown6() {
        return this.cooldown6;
    }

    public void setCooldown6(long aCooldown6) {
        this.cooldown6 = aCooldown6;
    }

    public long getCooldown7() {
        return this.cooldown7;
    }

    public void setCooldown7(long aCooldown7) {
        this.cooldown7 = aCooldown7;
    }

    public byte getSkillgainCount() {
        return this.skillgainCount;
    }

    public void setSkillgainCount(byte aSkillgainCount) {
        this.skillgainCount = aSkillgainCount;
    }

    public byte getLevel() {
        return this.level;
    }

    public void setLevel(byte aLevel) {
        if (aLevel > 127) {
            aLevel = (byte)127;
        }
        if (aLevel < 0) {
            aLevel = 0;
        }
        this.level = aLevel;
    }

    public byte getPath() {
        return this.path;
    }

    public void setPath(byte aPath) {
        this.path = aPath;
    }
}

