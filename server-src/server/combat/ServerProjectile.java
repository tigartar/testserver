/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.combat;

import com.wurmonline.math.Vector2f;
import com.wurmonline.math.Vector3f;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.bodys.TempWound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProjectile
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(ServerProjectile.class.getName());
    public static final float meterPerSecond = 12.0f;
    private static final float gravity = 0.04f;
    private static final float newGravity = -9.8f;
    public static final int TICKS_PER_SECOND = 24;
    private final float posDownX;
    private final float posDownY;
    private final Item projectile;
    private float currentSecondsInAir = 0.0f;
    private long timeAtLanding = 0L;
    private final Creature shooter;
    private final Item weapon;
    private final byte rarity;
    private float damageDealth = 0.0f;
    private BlockingResult result = null;
    private static final CopyOnWriteArraySet<ServerProjectile> projectiles = new CopyOnWriteArraySet();
    private ProjectileInfo projectileInfo = null;
    boolean sentEffect = false;

    public ServerProjectile(Item aWeapon, Item aProjectile, float aPosDownX, float aPosDownY, Creature aShooter, byte actionRarity, float damDealt) throws NoSuchZoneException {
        this.weapon = aWeapon;
        this.projectile = aProjectile;
        this.posDownX = aPosDownX;
        this.posDownY = aPosDownY;
        this.shooter = aShooter;
        this.setDamageDealt(damDealt);
        this.rarity = actionRarity;
        projectiles.add(this);
    }

    public boolean fire(boolean isOnSurface) throws NoSuchZoneException {
        if (Features.Feature.NEW_PROJECTILES.isEnabled() && this.weapon.getTemplateId() != 936) {
            ProjectileInfo projectileInfo;
            float firingAngle = 45.0f + (float)(this.weapon.getAuxData() * 5);
            if (this.weapon.getTemplateId() == 936) {
                firingAngle = 7.5f;
            }
            this.projectileInfo = projectileInfo = ServerProjectile.getProjectileInfo(this.weapon, this.shooter, firingAngle, 15.0f);
            VolaTile t = Zones.getOrCreateTile((int)(projectileInfo.endPosition.x / 4.0f), (int)(projectileInfo.endPosition.y / 4.0f), this.weapon.isOnSurface());
            Village v = Villages.getVillage((int)(projectileInfo.endPosition.x / 4.0f), (int)(projectileInfo.endPosition.y / 4.0f), this.weapon.isOnSurface());
            if (!ServerProjectile.isOkToAttack(t, this.getShooter(), this.getDamageDealt())) {
                boolean ok = false;
                if (v != null && (v.isActionAllowed((short)174, this.getShooter(), false, 0, 0) || v.isEnemy(this.getShooter()))) {
                    ok = true;
                }
                if (!ok) {
                    this.shooter.getCommunicator().sendNormalServerMessage("You cannot fire the " + this.getProjectile().getName() + " to there, you are not allowed.");
                    return false;
                }
            }
            Skill firingSkill = null;
            int skillType = 10077;
            if (this.weapon.getTemplateId() == 936) {
                skillType = 10093;
            } else if (this.weapon.getTemplateId() == 937) {
                skillType = 10094;
            }
            firingSkill = this.shooter.getSkills().getSkillOrLearn(skillType);
            firingSkill.skillCheck(this.weapon.getWinches(), 0.0, false, (float)this.weapon.getWinches() / 5.0f);
            this.weapon.setData(0L);
            this.weapon.setWinches((short)0);
            if (this.weapon.getTemplateId() == 937) {
                int weight = 0;
                for (Item i : this.weapon.getAllItems(true)) {
                    weight += i.getWeightGrams();
                }
                this.weapon.setWinches((byte)Math.min(50, weight / 20000));
            }
            this.timeAtLanding = System.currentTimeMillis() + projectileInfo.timeToImpact;
            VolaTile startTile = Zones.getOrCreateTile((int)(projectileInfo.startPosition.x / 4.0f), (int)(projectileInfo.startPosition.y / 4.0f), isOnSurface);
            startTile.sendNewProjectile(this.getProjectile().getWurmId(), (byte)2, this.getProjectile().getModelName(), this.getProjectile().getName(), this.getProjectile().getMaterial(), projectileInfo.startPosition, projectileInfo.startVelocity, projectileInfo.endPosition, this.weapon.getRotation(), this.weapon.isOnSurface());
            VolaTile endTile = Zones.getOrCreateTile((int)(projectileInfo.endPosition.x / 4.0f), (int)(projectileInfo.endPosition.y / 4.0f), isOnSurface);
            endTile.sendNewProjectile(this.getProjectile().getWurmId(), (byte)2, this.getProjectile().getModelName(), this.getProjectile().getName(), this.getProjectile().getMaterial(), projectileInfo.startPosition, projectileInfo.startVelocity, projectileInfo.endPosition, this.weapon.getRotation(), this.weapon.isOnSurface());
            return true;
        }
        float targetZ = Zones.calculateHeight(this.posDownX, this.posDownY, isOnSurface);
        this.result = ServerProjectile.calculateBlocker(this.weapon, this.posDownX, this.posDownY, targetZ);
        if (this.result == null) {
            logger.log(Level.INFO, "Blocker is null");
            return false;
        }
        if (this.result.getFirstBlocker() != null) {
            Vector2f targPos;
            float newy;
            float newx = this.result.getFirstBlocker().getTileX() * 4 + 2;
            Vector2f projPos = new Vector2f(newx, newy = (float)(this.result.getFirstBlocker().getTileY() * 4 + 2));
            float dist = projPos.subtract(targPos = new Vector2f(this.weapon.getTileX() * 4 + 2, this.weapon.getTileY() * 4 + 2)).length() / 4.0f;
            if (dist < 8.0f) {
                if (this.shooter.getPower() > 0 && Servers.isThisATestServer()) {
                    this.shooter.getCommunicator().sendNormalServerMessage("Calculated block from " + this.weapon.getPosX() + "," + this.weapon.getPosY() + " dist:" + dist + " at " + newx + "," + newy + ".");
                }
                this.shooter.getCommunicator().sendNormalServerMessage(" You cannot fire at such a short range.");
                return false;
            }
            this.weapon.setData(0L);
            this.weapon.setWinches((short)0);
            this.setTimeAtLanding(System.currentTimeMillis() + (long)(this.result.getActualBlockingTime() * 1000.0f));
            VolaTile tile = Zones.getOrCreateTile(this.weapon.getTileX(), this.weapon.getTileY(), isOnSurface);
            tile.sendProjectile(this.getProjectile().getWurmId(), this.weapon.getTemplateId() == 936 ? (byte)9 : 2, this.getProjectile().getModelName(), this.getProjectile().getName(), this.getProjectile().getMaterial(), this.weapon.getPosX(), this.weapon.getPosY(), this.weapon.getPosZ(), this.weapon.getRotation(), (byte)0, projPos.x, projPos.y, targetZ, this.weapon.getWurmId(), -10L, this.result.getEstimatedBlockingTime(), this.result.getActualBlockingTime());
            tile = Zones.getOrCreateTile((int)(projPos.x / 4.0f), (int)(projPos.y / 4.0f), true);
            tile.sendProjectile(this.getProjectile().getWurmId(), this.weapon.getTemplateId() == 936 ? (byte)9 : 2, this.getProjectile().getModelName(), this.getProjectile().getName(), this.getProjectile().getMaterial(), this.weapon.getPosX(), this.weapon.getPosY(), this.weapon.getPosZ(), this.weapon.getRotation(), (byte)0, projPos.x, projPos.y, targetZ, this.weapon.getWurmId(), -10L, this.result.getEstimatedBlockingTime(), this.result.getActualBlockingTime());
            if (this.shooter.getPower() >= 5) {
                this.shooter.getCommunicator().sendNormalServerMessage("You hit tile (" + this.result.getFirstBlocker().getTileX() + "," + this.result.getFirstBlocker().getTileY() + "), distance: " + dist + ".");
            }
            if (this.weapon.getTemplateId() == 937) {
                this.weapon.setLastMaintained(WurmCalendar.currentTime);
            }
            return true;
        }
        logger.log(Level.INFO, "No blocker");
        return false;
    }

    public long getTimeAtLanding() {
        return this.timeAtLanding;
    }

    public void setTimeAtLanding(long aTimeAtLanding) {
        this.timeAtLanding = aTimeAtLanding;
    }

    public static final void clear() {
        for (ServerProjectile projectile : projectiles) {
            projectile.poll(Long.MAX_VALUE);
        }
    }

    public static final boolean isOkToAttack(VolaTile t, Creature performer, float damdealt) {
        Structure structure;
        boolean ok = true;
        Village v = t.getVillage();
        if (v != null && performer.isFriendlyKingdom(v.kingdom)) {
            if (v.isActionAllowed((short)174, performer, false, 0, 0)) {
                ok = true;
            } else if (!v.isEnemy(performer)) {
                performer.setUnmotivatedAttacker();
                ok = false;
                if (t.isInPvPZone()) {
                    v.modifyReputation(performer.getWurmId(), -20, false);
                    if (performer.getKingdomTemplateId() != 3) {
                        performer.setReputation(performer.getReputation() - 30);
                        performer.getCommunicator().sendAlertServerMessage("This is bad for your reputation.");
                        if (performer.getDeity() != null && !performer.getDeity().isLibila() && Server.rand.nextInt(Math.max(1, (int)performer.getFaith())) < 5) {
                            performer.getCommunicator().sendNormalServerMessage(performer.getDeity().name + " has noticed you and is upset at your behaviour!");
                            performer.modifyFaith(-0.25f);
                            performer.maybeModifyAlignment(-1.0f);
                        }
                    } else {
                        ok = true;
                    }
                }
            }
        }
        if ((structure = t.getStructure()) != null && structure.isTypeBridge()) {
            ok = true;
        }
        if (structure != null && structure.isTypeHouse() && damdealt > 0.0f) {
            if (v != null && v.isEnemy(performer)) {
                ok = true;
            }
            if (performer.getKingdomTemplateId() != 3) {
                byte ownerkingdom = Players.getInstance().getKingdomForPlayer(structure.getOwnerId());
                if (performer.isFriendlyKingdom(ownerkingdom)) {
                    ok = false;
                    boolean found = false;
                    if ((!t.isInPvPZone() || v != null) && structure.isFinished() && structure.isLocked()) {
                        boolean bl = found = structure.mayModify(performer) || t.isInPvPZone();
                    }
                    if (found) {
                        ok = true;
                    }
                    if (!found) {
                        performer.setUnmotivatedAttacker();
                        if (t.isInPvPZone()) {
                            ok = true;
                        }
                    }
                } else {
                    ok = true;
                }
                if (structure.mayModify(performer)) {
                    ok = true;
                }
            } else {
                ok = true;
            }
        }
        return ok;
    }

    /*
     * WARNING - void declaration
     */
    public static final boolean setEffects(Item weapon, Item projectile, int newx, int newy, float dist, int floorLevelDown, Creature performer, byte rarity, float damdealt) {
        try {
            VolaTile eastTile;
            float mod;
            Zones.getZone(newx, newy, weapon.isOnSurface());
            VolaTile t = Zones.getOrCreateTile(newx, newy, weapon.isOnSurface());
            String whatishit = "the ground";
            boolean hit = false;
            boolean ok = ServerProjectile.isOkToAttack(t, performer, damdealt);
            double pwr = 0.0;
            int floorLevel = 0;
            Structure structure = t.getStructure();
            Skill cataskill = null;
            boolean doneSkillRoll = false;
            boolean arrowStuck = false;
            int skilltype = 10077;
            if (weapon.getTemplateId() == 936) {
                skilltype = 10093;
            }
            if (weapon.getTemplateId() == 937) {
                skilltype = 10094;
            }
            try {
                cataskill = performer.getSkills().getSkill(skilltype);
            }
            catch (NoSuchSkillException nss) {
                cataskill = performer.getSkills().learn(skilltype, 1.0f);
            }
            if (structure != null && structure.isTypeHouse()) {
                byte ownerkingdom;
                hit = true;
                whatishit = structure.getName();
                if (!t.isInPvPZone() && !ok && performer.getKingdomTemplateId() != 3 && performer.isFriendlyKingdom(ownerkingdom = Players.getInstance().getKingdomForPlayer(structure.getOwnerId()))) {
                    damdealt = 0.0f;
                    hit = false;
                }
                if (hit && !doneSkillRoll) {
                    pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0f);
                    doneSkillRoll = true;
                }
                if (pwr > 0.0) {
                    if (damdealt > 0.0f) {
                        float newdam;
                        int x;
                        Floor[] floors;
                        Fence fence;
                        Wall w;
                        int destroyed = 0;
                        Floor f = t.getTopFloor();
                        if (f != null) {
                            floorLevel = f.getFloorLevel();
                        }
                        if ((w = t.getTopWall()) != null && w.getFloorLevel() > floorLevel) {
                            floorLevel = w.getFloorLevel();
                        }
                        if ((fence = t.getTopFence()) != null && fence.getFloorLevel() > floorLevel) {
                            floorLevel = fence.getFloorLevel();
                        }
                        boolean logged = false;
                        mod = 2.0f;
                        if (floorLevel > 0 && (floors = t.getFloors(floorLevel * 30, floorLevel * 30)).length > 0) {
                            for (x = 0; x < floors.length; ++x) {
                                newdam = floors[x].getDamage() + Math.min(20.0f, floors[x].getDamageModifier() * damdealt / 2.0f);
                                if (newdam >= 100.0f) {
                                    if (!logged) {
                                        logged = true;
                                        TileEvent.log(floors[x].getTileX(), floors[x].getTileY(), 0, performer.getWurmId(), 236);
                                    }
                                    ++destroyed;
                                }
                                if (floors[x].setDamage(newdam)) {
                                    floors[x].getTile().removeFloor(floors[x]);
                                }
                                arrowStuck = true;
                            }
                        }
                        Wall[] warr = t.getWalls();
                        for (x = 0; x < warr.length; ++x) {
                            if (warr[x].getFloorLevel() != floorLevel) continue;
                            newdam = warr[x].getDamage() + Math.min(warr[x].isFinished() ? 20.0f : 100.0f, warr[x].getDamageModifier() * damdealt / 2.0f);
                            if (newdam >= 100.0f) {
                                if (!logged) {
                                    logged = true;
                                    TileEvent.log(warr[x].getTileX(), warr[x].getTileY(), 0, performer.getWurmId(), 236);
                                }
                                ++destroyed;
                            }
                            warr[x].setDamage(newdam);
                            arrowStuck = true;
                        }
                        Floor[] floors2 = t.getFloors();
                        for (int x2 = 0; x2 < floors2.length; ++x2) {
                            if (floors2[x2].getFloorLevel() != floorLevel) continue;
                            float newdam2 = floors2[x2].getDamage() + Math.min(20.0f, floors2[x2].getDamageModifier() * damdealt / 2.0f);
                            if (newdam2 >= 100.0f) {
                                if (!logged) {
                                    logged = true;
                                    TileEvent.log(floors2[x2].getTileX(), floors2[x2].getTileY(), 0, performer.getWurmId(), 236);
                                }
                                ++destroyed;
                            }
                            floors2[x2].setDamage(newdam2);
                            arrowStuck = true;
                        }
                        if (destroyed > 0 && !ok) {
                            performer.getCommunicator().sendNormalServerMessage("You feel very bad about this.");
                            performer.maybeModifyAlignment(-5.0f);
                            performer.punishSkills(0.1 * (double)Math.min(3, destroyed), false);
                        }
                        ServerProjectile.alertGuards(performer, newx, newy, ok, t, destroyed);
                    }
                    if (damdealt > 0.0f) {
                        performer.getCommunicator().sendNormalServerMessage("You seem to have hit " + t.getStructure().getName() + "!" + (Servers.isThisATestServer() ? " Dealt:" + damdealt : ""));
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("You seem to have hit " + t.getStructure().getName() + " but luckily it took no damage!");
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You just missed " + t.getStructure().getName() + ".");
                }
                if (t.getStructure() == null) {
                    performer.achievement(51);
                }
            }
            if (structure != null && structure.isTypeBridge()) {
                hit = true;
                whatishit = structure.getName();
                if (hit && !doneSkillRoll) {
                    pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0f);
                    doneSkillRoll = true;
                }
                if (pwr > 0.0) {
                    if (damdealt > 0.0f) {
                        for (TimeConstants timeConstants : t.getBridgeParts()) {
                            float mod2 = ((BridgePart)timeConstants).getModByMaterial();
                            float newdam = ((BridgePart)timeConstants).getDamage() + Math.min(20.0f, ((BridgePart)timeConstants).getDamageModifier() * damdealt / mod2);
                            TileEvent.log(((BridgePart)timeConstants).getTileX(), ((BridgePart)timeConstants).getTileY(), 0, performer.getWurmId(), 236);
                            ((BridgePart)timeConstants).setDamage(newdam);
                        }
                    }
                    if (damdealt > 0.0f) {
                        performer.getCommunicator().sendNormalServerMessage("You seem to have hit " + t.getStructure().getName() + "!" + (Servers.isThisATestServer() ? " Dealt:" + damdealt : ""));
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("You seem to have hit " + t.getStructure().getName() + " but luckily it took no damage!");
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You just missed " + t.getStructure().getName() + ".");
                }
            }
            for (Fence fence : t.getFencesForLevel(floorLevel)) {
                hit = true;
                whatishit = "the " + fence.getName();
                Village vill = MethodsStructure.getVillageForFence(fence);
                if (!ok && vill != null) {
                    if (vill.isActionAllowed((short)174, performer, false, 0, 0)) {
                        ok = true;
                    } else if (!vill.isEnemy(performer)) {
                        hit = false;
                        damdealt = 0.0f;
                    }
                }
                if (hit && !doneSkillRoll) {
                    pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0f);
                    doneSkillRoll = true;
                }
                if (pwr > 0.0) {
                    if (!(damdealt > 0.0f)) continue;
                    mod = 2.0f;
                    TileEvent.log(fence.getTileX(), fence.getTileY(), 0, performer.getWurmId(), 236);
                    fence.setDamage(fence.getDamage() + Math.min(fence.isFinished() ? 20.0f : 100.0f, fence.getDamageModifier() * damdealt / 2.0f));
                    performer.getCommunicator().sendNormalServerMessage("You seem to have hit " + whatishit + "!");
                    arrowStuck = true;
                    continue;
                }
                performer.getCommunicator().sendNormalServerMessage("You just missed some fences with the " + projectile.getName() + ".");
            }
            VolaTile southTile = Zones.getTileOrNull(newx, newy + 1, true);
            if (southTile != null) {
                void var24_41;
                Fence[] f = southTile.getFencesForLevel(floorLevel);
                int w = f.length;
                boolean bl = false;
                while (var24_41 < w) {
                    Fence fence = f[var24_41];
                    if (fence.isHorizontal()) {
                        hit = true;
                        whatishit = "the " + fence.getName();
                        Village vill = MethodsStructure.getVillageForFence(fence);
                        if (!ok && vill != null) {
                            if (vill.isActionAllowed((short)174, performer, false, 0, 0)) {
                                ok = true;
                            } else if (!vill.isEnemy(performer)) {
                                hit = false;
                                damdealt = 0.0f;
                            }
                        }
                        if (hit && !doneSkillRoll) {
                            pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0f);
                            doneSkillRoll = true;
                        }
                        if (pwr > 0.0) {
                            if (damdealt > 0.0f) {
                                float mod3 = 2.0f;
                                TileEvent.log(fence.getTileX(), fence.getTileY(), 0, performer.getWurmId(), 236);
                                fence.setDamage(fence.getDamage() + Math.min(20.0f, fence.getDamageModifier() * damdealt / 2.0f));
                                performer.getCommunicator().sendNormalServerMessage("You seem to have hit " + whatishit + "!");
                                arrowStuck = true;
                            }
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("You just missed some fences with the " + projectile.getName() + ".");
                        }
                    }
                    ++var24_41;
                }
            }
            if ((eastTile = Zones.getTileOrNull(newx + 1, newy, true)) != null) {
                for (Fence fence : eastTile.getFencesForLevel(floorLevel)) {
                    if (fence.isHorizontal()) continue;
                    hit = true;
                    whatishit = "the " + fence.getName();
                    Village vill = MethodsStructure.getVillageForFence(fence);
                    if (!ok && vill != null) {
                        if (vill.isActionAllowed((short)174, performer, false, 0, 0)) {
                            ok = true;
                        } else if (!vill.isEnemy(performer)) {
                            hit = false;
                            damdealt = 0.0f;
                        }
                    }
                    if (hit && !doneSkillRoll) {
                        pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0f);
                        doneSkillRoll = true;
                    }
                    if (pwr > 0.0) {
                        if (!(damdealt > 0.0f)) continue;
                        float mod4 = 2.0f;
                        TileEvent.log(fence.getTileX(), fence.getTileY(), 0, performer.getWurmId(), 236);
                        fence.setDamage(fence.getDamage() + Math.min(20.0f, fence.getDamageModifier() * damdealt / 2.0f));
                        performer.getCommunicator().sendNormalServerMessage("You seem to have hit " + whatishit + "!");
                        arrowStuck = true;
                        continue;
                    }
                    performer.getCommunicator().sendNormalServerMessage("You just missed some fences with the " + projectile.getName() + ".");
                }
            }
            if (ServerProjectile.testHitCreaturesOnTile(t, performer, projectile, damdealt, dist, floorLevel)) {
                if (weapon.getTemplateId() != 936 || !arrowStuck) {
                    if (weapon.getTemplateId() == 936) {
                        damdealt *= 3.0f;
                    }
                    if (!doneSkillRoll) {
                        pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0f);
                        doneSkillRoll = true;
                    }
                    boolean hit2 = ServerProjectile.hitCreaturesOnTile(t, pwr, performer, projectile, damdealt, dist, floorLevel);
                    if (!hit && hit2) {
                        hit = true;
                    }
                }
                if (!hit) {
                    performer.getCommunicator().sendNormalServerMessage("You hit nothing with the " + projectile.getName() + ".");
                }
            }
            t = Zones.getOrCreateTile(newx, newy, weapon.isOnSurface());
            if (projectile.isEgg()) {
                t.broadCast("A " + projectile.getName() + " comes flying through the air, hits " + whatishit + ", and shatters.");
                performer.getCommunicator().sendNormalServerMessage("The " + projectile.getName() + " shatters.");
                Items.destroyItem(projectile.getWurmId());
            } else if (projectile.setDamage(projectile.getDamage() + projectile.getDamageModifier() * (float)(20 + Server.rand.nextInt(Math.max(1, (int)dist))))) {
                t.broadCast("A " + projectile.getName() + " comes flying through the air, hits " + whatishit + ", and shatters.");
                performer.getCommunicator().sendNormalServerMessage("The " + projectile.getName() + " shatters.");
            } else {
                t.broadCast("A " + projectile.getName() + " comes flying through the air and hits " + whatishit + ".");
            }
        }
        catch (NoSuchZoneException nsz) {
            performer.getCommunicator().sendNormalServerMessage("You hit nothing with the " + projectile.getName() + ".");
            Items.destroyItem(weapon.getData());
            return true;
        }
        return projectile.deleted;
    }

    private static final void alertGuards(Creature performer, int newx, int newy, boolean ok, VolaTile t, int destroyed) {
        block7: {
            try {
                Structure struct;
                if (MethodsItems.mayTakeThingsFromStructure(performer, null, newx, newy) || ok || (struct = t.getStructure()) == null || !struct.isFinished()) break block7;
                for (VirtualZone vz : t.getWatchers()) {
                    try {
                        if (vz.getWatcher() == null || vz.getWatcher().getCurrentTile() == null || !performer.isFriendlyKingdom(vz.getWatcher().getKingdomId())) continue;
                        boolean cares = false;
                        if (vz.getWatcher().isKingdomGuard()) {
                            cares = true;
                        }
                        if (!cares) {
                            cares = struct.isGuest(vz.getWatcher());
                        }
                        if (!cares || Math.abs(vz.getWatcher().getCurrentTile().tilex - newx) > 20 && Math.abs(vz.getWatcher().getCurrentTile().tiley - newy) > 20 || !cares || !(performer.getStealSkill().skillCheck(95 - Math.min(Math.abs(vz.getWatcher().getCurrentTile().tilex - newx), Math.abs(vz.getWatcher().getCurrentTile().tiley - newy)) * 5, 0.0, true, 10.0f) < 0.0) || Servers.localServer.PVPSERVER && destroyed <= 0) continue;
                        performer.setReputation(performer.getReputation() - 10);
                        performer.getCommunicator().sendNormalServerMessage("People notice you. This is bad for your reputation!", (byte)2);
                        break;
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            catch (NoSuchStructureException noSuchStructureException) {
                // empty catch block
            }
        }
    }

    private static final boolean testHitCreaturesOnTile(VolaTile t, Creature performer, Item projectile, float damdealt, float dist, int floorlevel) {
        boolean hit = false;
        Creature[] initialCreatures = t.getCreatures();
        HashSet<Creature> creatureSet = new HashSet<Creature>();
        for (Creature c : initialCreatures) {
            if (t.getBridgeParts().length > 0) {
                if (c.getBridgeId() != t.getBridgeParts()[0].getStructureId()) continue;
                creatureSet.add(c);
                continue;
            }
            if (c.getFloorLevel() != floorlevel) continue;
            creatureSet.add(c);
        }
        Creature[] creatures = creatureSet.toArray(new Creature[creatureSet.size()]);
        if (creatures.length > 0) {
            boolean nonpvp = false;
            int x = Server.rand.nextInt(creatures.length);
            if (!creatures[x].isUnique() && !creatures[x].isInvulnerable() && creatures[x].getPower() == 0) {
                if (!(!performer.isFriendlyKingdom(creatures[x].getKingdomId()) || creatures[x].isOnPvPServer() && performer.isOnPvPServer() || performer.getCitizenVillage() != null && performer.getCitizenVillage().isEnemy(creatures[x].getCitizenVillage()))) {
                    nonpvp = true;
                }
                if (!nonpvp) {
                    try {
                        hit = true;
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, creatures[x].getName() + ex.getMessage(), ex);
                    }
                }
            }
        }
        return hit;
    }

    private static final boolean hitCreaturesOnTile(VolaTile t, double power, Creature performer, Item projectile, float damdealt, float dist, int floorlevel) {
        boolean hit = false;
        Creature[] initialCreatures = t.getCreatures();
        HashSet<Creature> creatureSet = new HashSet<Creature>();
        for (Creature c : initialCreatures) {
            if (t.getBridgeParts().length > 0) {
                if (c.getBridgeId() != t.getBridgeParts()[0].getStructureId()) continue;
                creatureSet.add(c);
                continue;
            }
            if (c.getFloorLevel() != floorlevel) continue;
            creatureSet.add(c);
        }
        Creature[] creatures = creatureSet.toArray(new Creature[creatureSet.size()]);
        if (power > 0.0 && creatures.length > 0) {
            boolean nonpvp = false;
            int x = Server.rand.nextInt(creatures.length);
            if (!creatures[x].isUnique() && !creatures[x].isInvulnerable() && creatures[x].getPower() == 0) {
                if (performer.isFriendlyKingdom(creatures[x].getKingdomId())) {
                    if (!(performer.getCitizenVillage() != null && performer.getCitizenVillage().isEnemy(creatures[x].getCitizenVillage()) || performer.hasBeenAttackedBy(creatures[x].getWurmId()) || creatures[x].getCurrentKingdom() != creatures[x].getKingdomId())) {
                        performer.setUnmotivatedAttacker();
                    }
                    if (!creatures[x].isOnPvPServer() || !performer.isOnPvPServer()) {
                        if (performer.getCitizenVillage() == null || !performer.getCitizenVillage().isEnemy(creatures[x].getCitizenVillage())) {
                            nonpvp = true;
                        }
                    } else if (!(!Servers.localServer.HOMESERVER || performer.isOnHostileHomeServer() || creatures[x].getReputation() < 0 || creatures[x].citizenVillage != null && creatures[x].citizenVillage.isEnemy(performer))) {
                        performer.setReputation(performer.getReputation() - 30);
                        performer.getCommunicator().sendAlertServerMessage("This is bad for your reputation.");
                    }
                }
                if (!nonpvp) {
                    try {
                        creatures[x].getCommunicator().sendAlertServerMessage("You are hit by some " + projectile.getName() + " coming through the air!");
                        if (!creatures[x].isPlayer()) {
                            creatures[x].setTarget(performer.getWurmId(), false);
                            creatures[x].setFleeCounter(20);
                        }
                        if (damdealt > 0.0f) {
                            if (creatures[x].isPlayer()) {
                                boolean dead = creatures[x].addWoundOfType(performer, (byte)0, 1, true, 1.0f, false, Math.min(25000.0f, damdealt * 1000.0f), 0.0f, 0.0f, false, false);
                                performer.achievement(47);
                                if (dead) {
                                    creatures[x].achievement(48);
                                }
                            } else {
                                creatures[x].getBody().addWound(new TempWound(0, creatures[x].getBody().getRandomWoundPos(), Math.min(25000.0f, damdealt * 1000.0f), creatures[x].getWurmId(), 0.0f, 0.0f, false));
                            }
                        }
                        hit = true;
                        performer.getCommunicator().sendNormalServerMessage("You hit " + creatures[x].getNameWithGenus() + "!");
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, creatures[x].getName() + ex.getMessage(), ex);
                    }
                }
            } else if (creatures[x].isVisible()) {
                performer.getCommunicator().sendNormalServerMessage(creatures[x].getNameWithGenus() + " dodges your " + projectile.getName() + " with no problem.");
            }
        }
        return hit;
    }

    public static final BlockingResult calculateBlocker(Item weapon, float estimatedEndPosX, float estimatedEndPosY, float estimatedPosZ) throws NoSuchZoneException {
        Vector3f startPos = new Vector3f(weapon.getTileX() * 4 + 2, weapon.getTileY() * 4 + 2, weapon.getPosZ());
        Vector3f currentPos = startPos.clone();
        Vector3f targetPos = new Vector3f(estimatedEndPosX, estimatedEndPosY, estimatedPosZ);
        Vector3f vector = targetPos.subtract(startPos);
        float length = vector.length();
        Vector3f dir = vector.normalize();
        float totalTimeInAir = length / 12.0f;
        float speed = 0.5f;
        float hVelocity = totalTimeInAir * 24.0f * 0.02f;
        hVelocity += dir.z * 0.5f;
        boolean hitSomething = false;
        float stepLength = 2.0f;
        float secondsPerStep = 0.16666667f;
        float gravityPerStep = 0.16f;
        float lastGroundHeight = Zones.calculateHeight(currentPos.getX(), currentPos.getY(), true);
        BlockingResult toReturn = null;
        float timeMoved = 0.0f;
        while (!hitSomething) {
            timeMoved += 0.16666667f;
            Vector3f lastPos = currentPos.clone();
            currentPos.z += (hVelocity -= 0.16f);
            currentPos.x += dir.x * 2.0f;
            currentPos.y += dir.y * 2.0f;
            float groundHeight = Zones.calculateHeight(currentPos.getX(), currentPos.getY(), true);
            toReturn = Blocking.getBlockerBetween(null, lastPos.getX(), lastPos.getY(), currentPos.getX(), currentPos.getY(), lastPos.getZ(), currentPos.z, true, true, true, 4, -10L, -10L, -10L, false);
            if (currentPos.getZ() < groundHeight - 1.0f) {
                toReturn = new BlockingResult();
                toReturn.addBlocker(new PathTile((int)currentPos.getX() / 4, (int)currentPos.getY() / 4, Server.surfaceMesh.getTile((int)currentPos.getX() / 4, (int)currentPos.getY() / 4), true, 0), currentPos, 100.0f);
                logger.log(Level.INFO, "Hit ground at " + (int)(currentPos.getX() / 4.0f) + "," + (int)(currentPos.getY() / 4.0f) + " height was " + groundHeight + ", compared to " + currentPos.getZ());
                toReturn.setEstimatedBlockingTime(totalTimeInAir);
                toReturn.setActualBlockingTime(timeMoved);
                return toReturn;
            }
            if (toReturn != null) {
                toReturn.setEstimatedBlockingTime(totalTimeInAir);
                toReturn.setActualBlockingTime(timeMoved);
                hitSomething = true;
            }
            lastGroundHeight = groundHeight;
        }
        return toReturn;
    }

    public static final float getProjectileDistance(Vector3f startingPosition, float heightOffset, float power, float rotation, float firingAngle) {
        Vector3f startingVelocity = new Vector3f((float)((double)power * Math.cos(rotation) * Math.cos(firingAngle)), (float)((double)power * Math.sin(rotation) * Math.cos(firingAngle)), (float)((double)power * Math.sin(firingAngle)));
        float offsetModifier = heightOffset / (startingVelocity.z / -9.8f * startingVelocity.z);
        float flightTime = startingVelocity.z * (2.0f - offsetModifier) / -9.8f;
        startingVelocity.z = 0.0f;
        Vector3f endingPosition = startingPosition.add(startingVelocity.mult(flightTime));
        return endingPosition.distance(startingPosition.add(0.0f, 0.0f, heightOffset));
    }

    public static final ProjectileInfo getProjectileInfo(Item weapon, Creature cret, float averageFiringAngle, float firingAngleVariationMax) throws NoSuchZoneException {
        Vector3f landingPosition = new Vector3f();
        int power = weapon.getWinches();
        float angleVar = 1.0f - Math.abs(averageFiringAngle - 45.0f) / 45.0f;
        float tiltAngle = averageFiringAngle - firingAngleVariationMax * angleVar;
        float rotation = (float)((double)(weapon.getRotation() - 90.0f) * Math.PI / 180.0);
        Skill firingSkill = null;
        int skillType = 10077;
        if (weapon.getTemplateId() == 936) {
            skillType = 10093;
            power = Math.min(40, power);
            power = (int)((float)power * 1.5f);
        } else if (weapon.getTemplateId() == 937) {
            skillType = 10094;
            power = Math.min(50, power);
        } else {
            power = Math.min(30, power);
        }
        try {
            firingSkill = cret.getSkills().getSkill(skillType);
        }
        catch (NoSuchSkillException nss) {
            firingSkill = cret.getSkills().learn(skillType, 1.0f);
        }
        double skillModifier = firingSkill.skillCheck(power, 0.0, true, 1.0f) / 100.0;
        float qlModifier = (100.0f - weapon.getCurrentQualityLevel()) / 100.0f;
        tiltAngle = (float)((double)tiltAngle + (double)(firingAngleVariationMax * angleVar * qlModifier) * skillModifier);
        if (skillModifier < 0.0) {
            tiltAngle /= 3.0f;
            power /= 2;
            cret.getCommunicator().sendNormalServerMessage("Something goes wrong when you fire the " + weapon.getName() + " and it doesn't fire as far as you expected.");
        }
        tiltAngle = (float)((double)tiltAngle * (Math.PI / 180));
        Vector3f currentVelocity = new Vector3f((float)((double)power * Math.cos(rotation) * Math.cos(tiltAngle)), (float)((double)power * Math.sin(rotation) * Math.cos(tiltAngle)), (float)((double)power * Math.sin(tiltAngle)));
        Vector3f currentPos = weapon.getPos3f();
        currentPos.z += (float)weapon.getTemplate().getSizeY() * 0.75f / 100.0f;
        Vector3f startingPosition = currentPos.clone();
        Vector3f startingVelocity = currentVelocity.clone();
        Vector3f nextPos = null;
        BlockingResult blocker = null;
        float stepAmount = 2.0f / currentVelocity.length();
        float gravityPerStep = -9.8f * stepAmount;
        long flightTime = 0L;
        boolean landed = false;
        while (!landed) {
            flightTime = (long)((float)flightTime + stepAmount * 1000.0f);
            nextPos = currentPos.add(currentVelocity.mult(stepAmount));
            blocker = Blocking.getBlockerBetween(null, currentPos.getX(), currentPos.getY(), nextPos.getX(), nextPos.getY(), currentPos.getZ() - 0.5f, nextPos.getZ() - 0.5f, weapon.isOnSurface(), weapon.isOnSurface(), true, 4, -10L, weapon.getBridgeId(), -10L, false);
            if (blocker != null) {
                landingPosition.set(blocker.getFirstIntersection());
                landed = true;
            } else {
                float groundHeight = Zones.calculateHeight(nextPos.getX(), nextPos.getY(), weapon.isOnSurface());
                if (nextPos.getZ() <= groundHeight) {
                    landingPosition.set(nextPos.getX(), nextPos.getY(), groundHeight);
                    landed = true;
                }
            }
            currentVelocity.z += gravityPerStep;
            currentPos = nextPos;
        }
        ProjectileInfo toReturn = new ProjectileInfo(startingPosition, startingVelocity, landingPosition, currentVelocity, flightTime);
        return toReturn;
    }

    private final boolean poll(long now) {
        if (Features.Feature.NEW_PROJECTILES.isEnabled() && this.weapon.getTemplateId() != 936) {
            if (now > this.timeAtLanding) {
                float majorRadius = (float)(this.getProjectile().getSizeX(true) + this.getProjectile().getSizeY(true)) / 2.0f / 10.0f;
                float damageMultiplier = this.projectileInfo.endVelocity.length() / 30.0f * (this.weapon.getCurrentQualityLevel() / 300.0f + 0.33f) * ((float)this.getProjectile().getWeightGrams() / 20000.0f);
                float damage = 1.0f * damageMultiplier;
                if (this.getProjectile().isStone() || this.getProjectile().isMetal()) {
                    damage *= 10.0f;
                } else if (this.getProjectile().isCorpse()) {
                    damage *= 2.5f;
                }
                if (this.getProjectile().getTemplateId() == 298 || this.getProjectile().getTemplateId() == 26 || this.getProjectile().isEgg()) {
                    damage /= 15.0f;
                }
                float extraDamage = (damage - 20.0f) / 4.0f;
                float minorRadius = 1.0f + extraDamage / 10.0f;
                damage = Math.min(20.0f, damage);
                float radius = majorRadius * minorRadius;
                int hitCounter = 0;
                int wallCount = 0;
                int fenceCount = 0;
                int floorCount = 0;
                int roofCount = 0;
                int bridgeCount = 0;
                int itemCount = 0;
                ArrayList<MiscConstants> itemHitList = null;
                ArrayList<Structure> structureHitList = null;
                for (int i = (int)((this.projectileInfo.endPosition.x - radius) / 4.0f); i <= (int)((this.projectileInfo.endPosition.x + radius) / 4.0f); ++i) {
                    for (int j = (int)((this.projectileInfo.endPosition.y - radius) / 4.0f); j <= (int)((this.projectileInfo.endPosition.y + radius) / 4.0f); ++j) {
                        float actualDam;
                        float distance;
                        VolaTile tileInRadius = Zones.getOrCreateTile(i, j, this.weapon.isOnSurface());
                        if (tileInRadius == null) continue;
                        for (Creature creature : tileInRadius.getCreatures()) {
                            if (creature.isUnique() || creature.isInvulnerable() || creature.getPower() > 0 || !creature.isOnPvPServer() && (creature.isHitched() || creature.isCaredFor() || creature.isBranded() && !creature.mayManage(this.shooter)) || !((distance = creature.getPos3f().distance(this.projectileInfo.endPosition)) <= radius)) continue;
                            if (creature.isPlayer() && this.shooter.isFriendlyKingdom(creature.getKingdomId()) && creature != this.shooter) {
                                if (this.shooter.getCitizenVillage() == null || !this.shooter.getCitizenVillage().isEnemy(creature.getCitizenVillage())) {
                                    if (!this.shooter.hasBeenAttackedBy(creature.getWurmId()) && creature.getCurrentKingdom() == creature.getKingdomId()) {
                                        this.shooter.setUnmotivatedAttacker();
                                    }
                                    if (!this.shooter.isOnPvPServer() || !creature.isOnPvPServer()) continue;
                                }
                                if (!(!Servers.localServer.HOMESERVER || this.shooter.isOnHostileHomeServer() || creature.getReputation() < 0 || creature.citizenVillage != null && creature.citizenVillage.isEnemy(this.shooter))) {
                                    this.shooter.setReputation(this.shooter.getReputation() - 30);
                                    this.shooter.getCommunicator().sendAlertServerMessage("This is bad for your reputation.");
                                }
                            }
                            if (damage > 0.0f) {
                                boolean dead;
                                creature.getCommunicator().sendAlertServerMessage("You are hit by " + this.projectile.getName() + " coming through the air!");
                                if (!creature.isPlayer()) {
                                    creature.setTarget(this.shooter.getWurmId(), false);
                                    creature.setFleeCounter(20);
                                }
                                this.shooter.getCommunicator().sendNormalServerMessage("You hit " + creature.getNameWithGenus() + "!");
                                float f = actualDam = distance > majorRadius ? Math.min(10.0f, extraDamage) : damage;
                                if (creature.isPlayer()) {
                                    dead = creature.addWoundOfType(this.shooter, (byte)0, 1, true, 1.0f, true, Math.min(25000.0f, actualDam * 1000.0f), 0.0f, 0.0f, false, false);
                                    this.shooter.achievement(47);
                                    if (dead) {
                                        creature.achievement(48);
                                        if (this.weapon.getTemplateId() == 445) {
                                            this.shooter.achievement(573);
                                        }
                                    }
                                } else {
                                    dead = creature.addWoundOfType(this.shooter, (byte)0, 1, true, 1.0f, true, Math.min(25000.0f, actualDam * 1000.0f), 0.0f, 0.0f, false, false);
                                    if (dead && this.weapon.getTemplateId() == 445) {
                                        this.shooter.achievement(573);
                                    }
                                }
                            }
                            ++hitCounter;
                        }
                        if (!Servers.localServer.PVPSERVER && tileInRadius.getStructure() != null && !tileInRadius.getStructure().isActionAllowed(this.shooter, (short)174)) continue;
                        for (MiscConstants miscConstants : tileInRadius.getWalls()) {
                            if (((Wall)miscConstants).isWallPlan() || !(Math.abs(((Wall)miscConstants).getCenterPoint().z - this.projectileInfo.endPosition.z) <= 1.5f)) continue;
                            distance = ((Wall)miscConstants).isHorizontal() ? Math.abs(((Wall)miscConstants).getCenterPoint().y - this.projectileInfo.endPosition.y) : Math.abs(((Wall)miscConstants).getCenterPoint().x - this.projectileInfo.endPosition.x);
                            float f = actualDam = distance > majorRadius ? Math.min(10.0f, extraDamage) : damage;
                            if (!(distance <= radius)) continue;
                            ++wallCount;
                            ++hitCounter;
                            if (Servers.localServer.testServer) {
                                this.shooter.getCommunicator().sendSafeServerMessage(((Wall)miscConstants).getName() + " hit for " + ((Wall)miscConstants).getDamageModifier() * actualDam);
                            }
                            ((Wall)miscConstants).setDamage(((Wall)miscConstants).getDamage() + ((Wall)miscConstants).getDamageModifier() * actualDam);
                            try {
                                if (structureHitList == null) {
                                    structureHitList = new ArrayList<Structure>();
                                }
                                if (structureHitList.contains(Structures.getStructure(((Wall)miscConstants).getStructureId()))) continue;
                                structureHitList.add(Structures.getStructure(((Wall)miscConstants).getStructureId()));
                            }
                            catch (NoSuchStructureException noSuchStructureException) {
                                // empty catch block
                            }
                        }
                        for (MiscConstants miscConstants : tileInRadius.getFences()) {
                            if (!(Math.abs(((Fence)miscConstants).getCenterPoint().z - this.projectileInfo.endPosition.z) <= 1.5f)) continue;
                            distance = ((Fence)miscConstants).isHorizontal() ? Math.abs(((Fence)miscConstants).getCenterPoint().y - this.projectileInfo.endPosition.y) : Math.abs(((Fence)miscConstants).getCenterPoint().x - this.projectileInfo.endPosition.x);
                            float f = actualDam = distance > majorRadius ? Math.min(10.0f, extraDamage) : damage;
                            if (!(distance <= radius)) continue;
                            ++fenceCount;
                            ++hitCounter;
                            if (Servers.localServer.testServer) {
                                this.shooter.getCommunicator().sendSafeServerMessage(((Fence)miscConstants).getName() + " hit for " + ((Fence)miscConstants).getDamageModifier() * actualDam);
                            }
                            ((Fence)miscConstants).setDamage(((Fence)miscConstants).getDamage() + ((Fence)miscConstants).getDamageModifier() * actualDam);
                        }
                        for (MiscConstants miscConstants : tileInRadius.getFloors()) {
                            if (((Floor)miscConstants).isAPlan()) continue;
                            distance = Math.abs(((Floor)miscConstants).getCenterPoint().z - this.projectileInfo.endPosition.z);
                            float f = actualDam = distance > majorRadius ? Math.min(10.0f, extraDamage) : damage;
                            if (!(distance <= radius)) continue;
                            if (((Floor)miscConstants).isRoof()) {
                                ++roofCount;
                            } else {
                                ++floorCount;
                            }
                            ++hitCounter;
                            if (Servers.localServer.testServer) {
                                this.shooter.getCommunicator().sendSafeServerMessage(((Floor)miscConstants).getName() + " hit for " + ((Floor)miscConstants).getDamageModifier() * actualDam);
                            }
                            ((Floor)miscConstants).setDamage(((Floor)miscConstants).getDamage() + ((Floor)miscConstants).getDamageModifier() * actualDam);
                            try {
                                if (structureHitList == null) {
                                    structureHitList = new ArrayList();
                                }
                                if (structureHitList.contains(Structures.getStructure(((Floor)miscConstants).getStructureId()))) continue;
                                structureHitList.add(Structures.getStructure(((Floor)miscConstants).getStructureId()));
                            }
                            catch (NoSuchStructureException noSuchStructureException) {
                                // empty catch block
                            }
                        }
                        for (MiscConstants miscConstants : tileInRadius.getBridgeParts()) {
                            if (((BridgePart)miscConstants).isAPlan() || !(((BridgePart)miscConstants).getCenterPoint().distance(this.projectileInfo.endPosition) <= 4.0f)) continue;
                            ++bridgeCount;
                            ++hitCounter;
                            ((BridgePart)miscConstants).setDamage(((BridgePart)miscConstants).getDamage() + ((BridgePart)miscConstants).getDamageModifier() * damage);
                        }
                        for (MiscConstants miscConstants : tileInRadius.getItems()) {
                            Village village;
                            if (((Item)miscConstants).isIndestructible() || ((Item)miscConstants).isRoadMarker() || ((Item)miscConstants).isLocked() || ((Item)miscConstants).isVehicle() || ((Item)miscConstants).isOwnerDestroyable() && ((Item)miscConstants).lastOwner != this.shooter.getWurmId() && !this.shooter.isOnPvPServer() && ((village = tileInRadius.getVillage()) == null || !village.isActionAllowed((short)83, this.shooter)) || (long)((Item)miscConstants).getZoneId() != -10L && ((Item)miscConstants).isKingdomMarker() && ((Item)miscConstants).getKingdom() == this.shooter.getKingdomId() && this.shooter.getWurmId() != ((Item)miscConstants).lastOwner && (tileInRadius.getVillage() == null || tileInRadius.getVillage() != this.shooter.getCitizenVillage()) || !(((Item)miscConstants).getPos3f().distance(this.projectileInfo.endPosition) <= radius)) continue;
                            ++itemCount;
                            if (itemHitList == null) {
                                itemHitList = new ArrayList<MiscConstants>();
                            }
                            itemHitList.add(miscConstants);
                        }
                    }
                }
                if (itemHitList != null) {
                    ++hitCounter;
                    Item t = (Item)itemHitList.get(Server.rand.nextInt(itemHitList.size()));
                    float actualDam = t.getPos3f().distance(this.projectileInfo.endPosition) > majorRadius ? Math.min(10.0f, extraDamage) : damage;
                    t.setDamage(t.getDamage() + t.getDamageModifier() * actualDam / 25.0f);
                }
                if (hitCounter == 0) {
                    this.shooter.getCommunicator().sendNormalServerMessage("It doesn't sound like the " + this.getProjectile().getName() + " hit anything.");
                } else {
                    StringBuilder targetList = new StringBuilder();
                    targetList.append("It sounds as though the " + this.getProjectile().getName() + " hit ");
                    if (wallCount > 0) {
                        targetList.append((wallCount > 1 ? Integer.valueOf(wallCount) : "a") + " wall" + (wallCount > 1 ? "s" : "") + ", ");
                    }
                    if (fenceCount > 0) {
                        targetList.append((fenceCount > 1 ? Integer.valueOf(fenceCount) : "a") + " fence" + (fenceCount > 1 ? "s" : "") + ", ");
                    }
                    if (roofCount > 0) {
                        targetList.append((roofCount > 1 ? Integer.valueOf(roofCount) : "a") + " roof" + (roofCount > 1 ? "s" : "") + ", ");
                    }
                    if (floorCount > 0) {
                        targetList.append((floorCount > 1 ? Integer.valueOf(floorCount) : "a") + " floor" + (floorCount > 1 ? "s" : "") + ", ");
                    }
                    if (bridgeCount > 0) {
                        targetList.append((bridgeCount > 1 ? Integer.valueOf(bridgeCount) : "a") + " bridge part" + (bridgeCount > 1 ? "s" : "") + ", ");
                    }
                    if (itemCount > 0) {
                        targetList.append("an item, ");
                    }
                    targetList.append("and nothing else.");
                    this.shooter.getCommunicator().sendNormalServerMessage(targetList.toString());
                    if (structureHitList != null && !structureHitList.isEmpty()) {
                        for (Structure struct : structureHitList) {
                            if (!struct.isDestroyed()) continue;
                            struct.totallyDestroy();
                            if (this.weapon.getTemplateId() != 445) continue;
                            this.shooter.achievement(51);
                        }
                        targetList.delete(0, targetList.length());
                        targetList.append("You managed to hit ");
                        for (int id = 0; id < structureHitList.size(); ++id) {
                            boolean hasMore = id < structureHitList.size() - 1;
                            targetList.append((hasMore && id > 0 ? "" : (id == 0 ? "" : "and ")) + ((Structure)structureHitList.get(id)).getName());
                            if (hasMore && id + 1 < structureHitList.size() - 1) {
                                targetList.append(", ");
                                continue;
                            }
                            if (hasMore) {
                                targetList.append(" ");
                                continue;
                            }
                            targetList.append(".");
                        }
                        this.shooter.getCommunicator().sendNormalServerMessage(targetList.toString());
                    }
                }
                boolean projDestroyed = this.getProjectile().setDamage(this.getProjectile().getDamage() + damage);
                if (!projDestroyed) {
                    try {
                        this.getProjectile().setPosXYZ(this.projectileInfo.endPosition.x, this.projectileInfo.endPosition.y, this.projectileInfo.endPosition.z);
                        Zone z = Zones.getZone(this.getProjectile().getTileX(), this.getProjectile().getTileY(), this.weapon.isOnSurface());
                        z.addItem(this.getProjectile());
                    }
                    catch (NoSuchZoneException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.shooter.getCommunicator().sendNormalServerMessage("The " + this.getProjectile().getName() + " crumbles to dust as it lands.");
                }
                if (Servers.localServer.testServer) {
                    this.shooter.getCommunicator().sendNormalServerMessage("[TEST] Projectile " + this.getProjectile().getName() + " landed, damage multiplier: " + damageMultiplier + ", damage: " + damage + " total things hit: " + hitCounter + ". Total distance: " + this.projectileInfo.startPosition.distance(this.projectileInfo.endPosition) + "m or " + this.projectileInfo.startPosition.distance(this.projectileInfo.endPosition) / 4.0f + " tiles.");
                }
                return true;
            }
            if (this.timeAtLanding - now <= 500L && !this.sentEffect) {
                EffectFactory.getInstance().createGenericTempEffect("dust03", this.projectileInfo.endPosition.x, this.projectileInfo.endPosition.y, this.projectileInfo.endPosition.z, this.weapon.isOnSurface(), -1.0f, 0.0f);
                this.sentEffect = true;
            }
            return false;
        }
        if (now > this.timeAtLanding) {
            float newx = this.getPosDownX();
            float newy = this.getPosDownY();
            if (this.result != null && this.result.getFirstBlocker() != null) {
                newx = this.result.getFirstBlocker().getTileX() * 4 + 2;
                newy = this.result.getFirstBlocker().getTileY() * 4 + 2;
            }
            Skill cataskill = null;
            int skilltype = 10077;
            if (this.weapon.getTemplateId() == 936) {
                skilltype = 10093;
            }
            if (this.weapon.getTemplateId() == 937) {
                skilltype = 10094;
            }
            try {
                cataskill = this.getShooter().getSkills().getSkill(skilltype);
            }
            catch (NoSuchSkillException nss) {
                cataskill = this.getShooter().getSkills().learn(skilltype, 1.0f);
            }
            Vector2f targPos = new Vector2f(this.weapon.getPosX(), this.weapon.getPosY());
            Vector2f projPos = new Vector2f(newx, newy);
            float dist = Math.abs(projPos.subtract(targPos).length() / 4.0f);
            double power = 0.0;
            VolaTile droptile = Zones.getOrCreateTile((int)(newx / 4.0f), (int)(newy / 4.0f), true);
            int dropFloorLevel = 0;
            boolean hit = false;
            boolean itemDestroyed = false;
            try {
                Item i = this.getProjectile();
                if (this.result != null) {
                    if (this.result.getFirstBlocker() != null) {
                        dropFloorLevel = droptile.getDropFloorLevel(this.result.getFirstBlocker().getFloorLevel());
                        if (this.result.getFirstBlocker().isTile()) {
                            itemDestroyed = ServerProjectile.setEffects(this.getWeapon(), i, (int)(newx / 4.0f), (int)(newy / 4.0f), dist, this.result.getFirstBlocker().getFloorLevel(), this.getShooter(), this.getRarity(), this.getDamageDealt());
                        } else {
                            boolean hadSkillGainChance = false;
                            boolean messageSent = false;
                            String whatishit = "the " + this.result.getFirstBlocker().getName();
                            Village vill = MethodsStructure.getVillageForBlocker(this.result.getFirstBlocker());
                            VolaTile t = Zones.getOrCreateTile(this.result.getFirstBlocker().getTileX(), this.result.getFirstBlocker().getTileY(), true);
                            boolean ok = ServerProjectile.isOkToAttack(t, this.getShooter(), this.getDamageDealt());
                            if (!ok && vill != null) {
                                if (vill.isActionAllowed((short)174, this.getShooter(), false, 0, 0)) {
                                    ok = true;
                                } else if (!vill.isEnemy(this.getShooter())) {
                                    ok = false;
                                }
                            }
                            if (ok) {
                                power = cataskill.skillCheck((double)dist - 9.0, this.weapon, 0.0, false, 10.0f);
                                hadSkillGainChance = true;
                                boolean bl = hit = power > 0.0;
                                if (hit && this.getDamageDealt() > 0.0f) {
                                    int fl = this.result.getFirstBlocker().getFloorLevel();
                                    float f = 1.0f;
                                    float newDam = this.result.getFirstBlocker().getDamage() + Math.min(20.0f, this.result.getFirstBlocker().getDamageModifier() * this.getDamageDealt() / 1.0f);
                                    whatishit = this.result.getFirstBlocker().getName();
                                    this.getShooter().getCommunicator().sendNormalServerMessage("You seem to have hit " + whatishit + "!" + (Servers.isThisATestServer() ? " Dam:" + this.getDamageDealt() + " NewDam:" + newDam : ""));
                                    if (!this.result.getFirstBlocker().isFloor()) {
                                        VolaTile t2;
                                        t.damageFloors(fl, fl + 1, Math.min(20.0f, this.result.getFirstBlocker().getDamageModifier() * this.getDamageDealt()));
                                        if (this.result.getFirstBlocker().isHorizontal() && (t2 = Zones.getTileOrNull(this.result.getFirstBlocker().getTileX() - 1, this.result.getFirstBlocker().getTileY(), true)) != null) {
                                            t2.damageFloors(fl, fl + 1, Math.min(20.0f, this.result.getFirstBlocker().getDamageModifier() * this.getDamageDealt()));
                                        }
                                    }
                                    this.result.getFirstBlocker().setDamage(newDam);
                                    if (newDam >= 100.0f) {
                                        TileEvent.log(this.result.getFirstBlocker().getTileX(), this.result.getFirstBlocker().getTileY(), 0, this.getShooter().getWurmId(), 236);
                                        if (this.result.getFirstBlocker().isFloor()) {
                                            t.removeFloor(this.result.getFirstBlocker());
                                        }
                                    }
                                }
                                if (itemDestroyed = this.projectile.setDamage(this.projectile.getDamage() + this.projectile.getDamageModifier() * (float)(20 + Server.rand.nextInt(Math.max(1, (int)dist))))) {
                                    t.broadCast("A " + this.projectile.getName() + " comes flying through the air, hits " + whatishit + ", and shatters.");
                                } else {
                                    t.broadCast("A " + this.projectile.getName() + " comes flying through the air and hits " + whatishit + ".");
                                }
                            } else {
                                this.getShooter().getCommunicator().sendNormalServerMessage("You seem to miss with the " + this.projectile.getName() + ".");
                                messageSent = true;
                            }
                            if (ServerProjectile.testHitCreaturesOnTile(droptile, this.getShooter(), i, this.getDamageDealt(), dist, dropFloorLevel)) {
                                if (!hadSkillGainChance) {
                                    power = cataskill.skillCheck((double)dist - 9.0, this.weapon, 0.0, false, 10.0f);
                                }
                                hit = ServerProjectile.hitCreaturesOnTile(droptile, power, this.getShooter(), i, this.getDamageDealt(), dist, dropFloorLevel);
                            }
                            if (!hit && !messageSent) {
                                this.getShooter().getCommunicator().sendNormalServerMessage("You just missed with the " + this.projectile.getName() + ".");
                            }
                        }
                    }
                    if (!itemDestroyed) {
                        i.setPosXYZ(newx, newy, Zones.calculateHeight(newx, newy, this.result.getFirstBlocker().getFloorLevel() >= 0) + (float)(Math.max(0, dropFloorLevel) * 3));
                        VolaTile vt = Zones.getOrCreateTile((int)(newx / 4.0f), (int)(newy / 4.0f), this.weapon.isOnSurface());
                        if (vt.getBridgeParts().length > 0) {
                            i.setOnBridge(vt.getBridgeParts()[0].getStructureId());
                        }
                        Zone z = Zones.getZone(i.getTileX(), i.getTileY(), this.result.getFirstBlocker().getFloorLevel() >= 0);
                        z.addItem(i);
                        logger.log(Level.INFO, "Adding " + i.getName() + " at " + (int)(newx / 4.0f) + "," + (int)(newy / 4.0f));
                    }
                } else if (!ServerProjectile.setEffects(this.getWeapon(), i, (int)(this.getPosDownX() / 4.0f), (int)(this.getPosDownY() / 4.0f), dist, 0, this.getShooter(), this.getRarity(), this.getDamageDealt())) {
                    VolaTile vt = Zones.getOrCreateTile((int)(newx / 4.0f), (int)(newy / 4.0f), this.weapon.isOnSurface());
                    float newz = 0.0f;
                    if (vt.getBridgeParts().length > 0) {
                        i.setOnBridge(-10L);
                    } else {
                        newz = Zones.calculateHeight(this.getPosDownX(), this.getPosDownY(), false);
                    }
                    i.setPosXYZ(this.getPosDownX(), this.getPosDownY(), newz);
                    Zone z = Zones.getZone(i.getTileX(), i.getTileY(), true);
                    z.addItem(i);
                    logger.log(Level.INFO, "Adding " + i.getName() + " at " + (int)(this.getPosDownX() / 4.0f) + "," + (int)(this.getPosDownY() / 4.0f));
                }
            }
            catch (NoSuchZoneException nsz) {
                logger.log(Level.INFO, this.getProjectile().getModelName() + " projectile with id " + this.getProjectile().getWurmId() + " shot outside the map");
            }
            return true;
        }
        return false;
    }

    public static void pollAll() {
        long now = System.currentTimeMillis();
        for (ServerProjectile projectile : projectiles) {
            if (!projectile.poll(now)) continue;
            projectiles.remove(projectile);
        }
    }

    public static final void removeProjectile(ServerProjectile projectile) {
        projectiles.remove(projectile);
    }

    public float getPosDownX() {
        return this.posDownX;
    }

    public float getPosDownY() {
        return this.posDownY;
    }

    public float getCurrentSecondsInAir() {
        return this.currentSecondsInAir;
    }

    public void setCurrentSecondsInAir(float aCurrentSecondsInAir) {
        this.currentSecondsInAir = aCurrentSecondsInAir;
    }

    public Item getWeapon() {
        return this.weapon;
    }

    public byte getRarity() {
        return this.rarity;
    }

    public BlockingResult getResult() {
        return this.result;
    }

    public void setResult(BlockingResult aResult) {
        this.result = aResult;
    }

    public Item getProjectile() {
        return this.projectile;
    }

    public float getDamageDealt() {
        return this.damageDealth;
    }

    public void setDamageDealt(float aDamageDealth) {
        this.damageDealth = aDamageDealth;
    }

    public Creature getShooter() {
        return this.shooter;
    }

    static class ProjectileInfo {
        public final Vector3f startPosition;
        public final Vector3f startVelocity;
        public final Vector3f endPosition;
        public final Vector3f endVelocity;
        public final long timeToImpact;

        ProjectileInfo(Vector3f startPosition, Vector3f startVelocity, Vector3f endPosition, Vector3f endVelocity, long timeToImpact) {
            this.startPosition = startPosition.clone();
            this.startVelocity = startVelocity.clone();
            this.endPosition = endPosition.clone();
            this.endVelocity = endVelocity.clone();
            this.timeToImpact = timeToImpact;
        }
    }
}

