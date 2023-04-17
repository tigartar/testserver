/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.math.Vector3f;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MeshTile;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.combat.ServerProjectile;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class WarmachineBehaviour
extends ItemBehaviour {
    private static final Logger logger = Logger.getLogger(WarmachineBehaviour.class.getName());

    WarmachineBehaviour() {
        super((short)40);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
        this.addToList(performer, toReturn, target, null);
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, source, target));
        this.addToList(performer, toReturn, target, source);
        return toReturn;
    }

    private void addToList(Creature performer, List<ActionEntry> toReturn, Item target, @Nullable Item source) {
        boolean reachable = false;
        if (target.getOwnerId() == -10L) {
            BlockingResult result;
            int distance;
            int n = distance = target.getTemplateId() == 1125 ? 2 : 4;
            if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), distance) && (result = Blocking.getBlockerBetween(performer, target, 4)) == null) {
                reachable = true;
            }
        } else if (target.getOwnerId() == performer.getWurmId()) {
            reachable = true;
        }
        if (target.getTopParent() != target.getWurmId()) {
            return;
        }
        if (reachable) {
            if (target.getTemplateId() == 1125) {
                toReturn.add(Actions.actionEntrys[851]);
                return;
            }
            boolean loaded = false;
            boolean mayload = false;
            boolean maywinch = false;
            boolean mayfire = false;
            short nums = 0;
            if (target.getData() <= 0L) {
                loaded = false;
                if (source != null && source.canBeDropped(true) && !source.isArtifact()) {
                    mayload = true;
                    nums = (short)(nums - 1);
                }
            } else {
                nums = (short)(nums - 1);
                loaded = true;
                if (target.getWinches() < 50 && target.getTemplateId() != 937) {
                    maywinch = true;
                    nums = (short)(nums - 3);
                }
                if ((target.getWinches() > 0 || target.getTemplateId() == 937 && !target.isEmpty(false)) && (target.getTemplateId() != 937 || target.mayFireTrebuchet())) {
                    mayfire = true;
                    nums = (short)(nums - (target.getTemplateId() == 937 ? 1 : 2));
                }
            }
            if (maywinch || mayfire) {
                nums = (short)(nums - 2);
            }
            if (nums < 0 && target.getPosZ() > 0.0f) {
                toReturn.add(new ActionEntry(nums, "Machine", "Machine options"));
                if (!loaded && mayload) {
                    toReturn.add(Actions.actionEntrys[233]);
                } else if (loaded) {
                    toReturn.add(Actions.actionEntrys[234]);
                }
                if (maywinch || mayfire) {
                    toReturn.add(Actions.actionEntrys[849]);
                    toReturn.add(Actions.actionEntrys[850]);
                }
                if (maywinch) {
                    toReturn.add(Actions.actionEntrys[237]);
                    toReturn.add(Actions.actionEntrys[238]);
                    toReturn.add(Actions.actionEntrys[239]);
                }
                if (mayfire) {
                    toReturn.add(Actions.actionEntrys[236]);
                    if (target.getTemplateId() != 937) {
                        toReturn.add(Actions.actionEntrys[235]);
                    }
                }
            }
        }
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short action, float counter) {
        boolean done = false;
        done = action == 237 || action == 239 || action == 238 || action == 234 || action == 236 || action == 235 || action == 849 || action == 850 || action == 851 ? this.action(act, performer, null, target, action, counter) : super.action(act, performer, target, action, counter);
        return done;
    }

    public final boolean mayManouvre(Creature performer, Item warMachine) {
        int tx = warMachine.getTileX();
        int ty = warMachine.getTileY();
        VolaTile wmT = Zones.getOrCreateTile(tx, ty, warMachine.isOnSurface());
        for (int x = tx - 2; x <= tx + 2; ++x) {
            for (int y = ty - 2; y <= ty + 2; ++y) {
                Item[] items;
                VolaTile t = Zones.getTileOrNull(Zones.safeTileX(x), Zones.safeTileY(y), warMachine.isOnSurface());
                if (t == null) continue;
                for (Item item : items = t.getItems()) {
                    if (!item.isUseOnGroundOnly() || item.getWurmId() == warMachine.getWurmId() || !(item.getPos3f().distance(warMachine.getPos3f()) <= 8.0f) || t.getStructure() != wmT.getStructure()) continue;
                    performer.getCommunicator().sendAlertServerMessage("You can't work with the " + warMachine.getName() + ". This area is too crowded.");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean action(Action act, Creature performer, @Nullable Item source, Item target, short action, float counter) {
        boolean done = false;
        boolean reachable = false;
        if (target.getOwnerId() == -10L) {
            int distance;
            int n = distance = target.getTemplateId() == 1125 ? 2 : 4;
            if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), distance)) {
                reachable = true;
            }
        } else if (target.getOwnerId() == performer.getWurmId()) {
            reachable = true;
        }
        if (performer.isOnSurface() != target.isOnSurface()) {
            reachable = false;
        }
        if (!reachable) {
            performer.getCommunicator().sendNormalServerMessage("You cannot reach the " + target.getName() + " from there.");
            return true;
        }
        if (action == 237 || action == 239 || action == 238 || action == 233 || action == 234 || action == 236 || action == 235 || action == 849 || action == 850 || action == 851) {
            if (source != null && source.isBanked()) {
                return true;
            }
            if (!this.mayManouvre(performer, target)) {
                return true;
            }
            if (target.getPosZ() < 0.0f) {
                return true;
            }
            if (reachable) {
                Skills skills = performer.getSkills();
                try {
                    Skill str = skills.getSkill(102);
                    if (str.getKnowledge(0.0) <= 21.0) {
                        String message = StringUtil.format("You are too weak to handle the heavy %s. You need 21 %s.", target.getName(), str.getName());
                        performer.getCommunicator().sendNormalServerMessage(message);
                        return true;
                    }
                }
                catch (NoSuchSkillException nss) {
                    logger.log(Level.WARNING, "Weird, " + performer.getName() + " has no strength!");
                }
                if (action == 233) {
                    return this.loadCatapult(performer, act, source, target, action, counter);
                }
                if (action == 234) {
                    this.unloadCatapult(performer, target);
                    return true;
                }
                if (action == 237 || action == 239 || action == 238) {
                    return this.winchCatapult(performer, act, target, action, counter);
                }
                if (action == 235) {
                    this.unwindCatapult(performer, target);
                    return true;
                }
                if (action == 236) {
                    this.fireCatapult(act, performer, target);
                    return true;
                }
                if (action == 849 || action == 850) {
                    return this.changeFiringAngle(performer, act, target, action, counter);
                }
                if (action == 851) {
                    return this.useBatteringRam(performer, act, target, action, counter);
                }
            }
        } else {
            done = super.action(act, performer, source, target, action, counter);
        }
        return done;
    }

    private boolean useBatteringRam(Creature performer, Action act, Item target, short action, float counter) {
        boolean done;
        if (performer.getStrengthSkill() < 21.0) {
            performer.getCommunicator().sendNormalServerMessage("You are too weak to use the " + target.getName() + " effectively.");
            return true;
        }
        if (Items.isItemDragged(target)) {
            performer.getCommunicator().sendNormalServerMessage("You cannot use the " + target.getName() + " while it is being dragged.");
            return true;
        }
        MeshTile m = new MeshTile(target.isOnSurface() ? Server.surfaceMesh : Server.caveMesh, target.getTileX(), target.getTileY());
        if (m.checkSlopes(30, 45)) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " cannot be used on such a slope.");
            return true;
        }
        target.lastRammed = System.currentTimeMillis();
        target.lastRamUser = performer.getWurmId();
        float swingDistance = 1.0f;
        Vector3f targetPos = new Vector3f((float)((double)swingDistance * Math.cos((double)(target.getRotation() - 90.0f) * Math.PI / 180.0)), (float)((double)swingDistance * Math.sin((double)(target.getRotation() - 90.0f) * Math.PI / 180.0)), 0.0f);
        Vector3f fromPos = target.getPos3f().add(targetPos.mult(2.0f).add(0.0f, 0.0f, (float)target.getTemplate().getSizeY() / 300.0f));
        targetPos = fromPos.add(targetPos.add(0.0f, 0.0f, 0.33f));
        Skill warmachines = performer.getSkills().getSkillOrLearn(1029);
        double skillModifier = 1.0 - warmachines.getKnowledge(0.0) / 300.0;
        boolean bl = done = counter >= 2.0f && (double)counter >= 30.0 * skillModifier;
        if (counter == 1.0f) {
            performer.getCommunicator().sendNormalServerMessage("You start pulling back the beam on the " + target.getName() + ".");
            performer.sendActionControl(act.getActionString(), true, (int)Math.round(300.0 * skillModifier));
        }
        if (done) {
            performer.getCommunicator().sendNormalServerMessage("You swing the beam on the " + target.getName() + " forward.");
            Server.getInstance().broadCastAction(performer.getName() + " swings the beam on the " + target.getName() + " forward.", performer, 5);
            VolaTile t = Zones.getTileOrNull(target.getTilePos(), target.isOnSurface());
            if (t != null) {
                BlockingResult result = Blocking.getBlockerBetween(null, fromPos.getX(), fromPos.getY(), targetPos.getX(), targetPos.getY(), fromPos.getZ(), targetPos.getZ(), target.isOnSurface(), target.isOnSurface(), true, 4, -10L, target.getBridgeId(), -10L, false);
                if (result != null) {
                    VolaTile targetTile = Zones.getOrCreateTile(result.getFirstBlocker().getTileX(), result.getFirstBlocker().getTileY(), target.isOnSurface());
                    Village v = targetTile.getVillage();
                    if (!ServerProjectile.isOkToAttack(targetTile, performer, 10.0f)) {
                        boolean ok = false;
                        if (v != null && (v.isActionAllowed(result.getFirstBlocker() instanceof Fence ? (short)172 : 174, performer, false, 0, 0) || v.isEnemy(performer))) {
                            ok = true;
                        }
                        if (!ok) {
                            performer.getCommunicator().sendNormalServerMessage("You stop the " + target.getName() + " at the last second as you realise you are not allowed to do that here.");
                            return true;
                        }
                    }
                    double power = -1.0;
                    if (result.getFirstBlocker() instanceof Wall) {
                        Wall wall = (Wall)result.getFirstBlocker();
                        power = warmachines.skillCheck(wall.getCurrentQualityLevel() / 3.0f, target, 0.0, false, Math.max(1.0f, wall.getCurrentQualityLevel() / 20.0f));
                    } else if (result.getFirstBlocker() instanceof Fence) {
                        Fence fence = (Fence)result.getFirstBlocker();
                        power = warmachines.skillCheck(fence.getCurrentQualityLevel() / 4.0f, target, 0.0, false, Math.max(1.0f, fence.getCurrentQualityLevel() / 20.0f));
                    } else {
                        power = warmachines.skillCheck(40.0f - result.getFirstBlocker().getDamage(), target, 0.0, false, Math.max(1.0f, 5.0f - result.getFirstBlocker().getDamage() / 20.0f));
                    }
                    float damage = 12.5f;
                    damage = (float)((double)damage * Math.max(0.75, power / 75.0));
                    damage *= 0.8f + target.getCurrentQualityLevel() / 500.0f;
                    if (target.isOnSurface()) {
                        damage *= 0.5f;
                    }
                    damage = Math.min(20.0f, damage);
                    if (power < 0.0) {
                        damage *= 0.2f;
                    }
                    if (v != null) {
                        if (MethodsStructure.isCitizenAndMayPerformAction((short)174, performer, v)) {
                            damage *= 5.0f;
                        } else if (MethodsStructure.isAllyAndMayPerformAction((short)174, performer, v)) {
                            damage *= 2.5f;
                        }
                    }
                    performer.getCommunicator().sendNormalServerMessage("You swing the beam on the " + target.getName() + " forward and it hits a " + result.getFirstBlocker().getName() + (power < 0.0 ? " but deals less damage than you expected." : "."));
                    Server.getInstance().broadCastAction(performer.getName() + " swings the beam on the " + target.getName() + " forward and hits a " + result.getFirstBlocker().getName() + ".", performer, 5);
                    if (Servers.localServer.testServer) {
                        performer.getCommunicator().sendNormalServerMessage("[TEST] Skillcheck: " + power + ", Total Damage: " + damage * result.getFirstBlocker().getDamageModifier());
                    }
                    result.getFirstBlocker().setDamage(result.getFirstBlocker().getDamage() + damage * result.getFirstBlocker().getDamageModifier());
                    target.setDamage(target.getDamage() + damage / 100.0f * target.getDamageModifier());
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The beam of the " + target.getName() + " hits nothing.");
                }
            }
            return true;
        }
        return false;
    }

    private boolean changeFiringAngle(Creature performer, Action act, Item target, short action, float counter) {
        boolean done;
        byte currentAngle = target.getAuxData();
        if (action == 849 && currentAngle >= 8) {
            performer.getCommunicator().sendNormalServerMessage("You cannot increase the firing angle any further.");
            return true;
        }
        if (action == 850 && currentAngle <= -8) {
            performer.getCommunicator().sendNormalServerMessage("You cannot reduce the firing angle any further.");
            return true;
        }
        int skillNum = 10077;
        if (target.getTemplateId() == 936) {
            skillNum = 10093;
        } else if (target.getTemplateId() == 937) {
            skillNum = 10094;
        }
        double speedModifier = 1.0 - performer.getSkills().getSkillOrLearn(skillNum).getKnowledge(0.0) / 300.0;
        boolean bl = done = counter >= 2.0f && counter >= (float)((int)Math.round(5.0 * speedModifier));
        if (counter == 1.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " creaks as you change the firing angle.");
            performer.sendActionControl(act.getActionString(), true, (int)Math.round(50.0 * speedModifier));
        }
        if (done) {
            target.setAuxData((byte)Math.min(8, Math.max(-8, currentAngle + (action == 849 ? 1 : -1))));
            Server.getInstance().broadCastAction("The " + target.getName() + " creaks as " + performer.getName() + " slightly changes the firing angle.", performer, 5);
            float angle = 45.0f + (float)(target.getAuxData() * 5);
            float zHeight = (float)target.getTemplate().getSizeY() * 0.75f / 100.0f;
            float approxDistance = ServerProjectile.getProjectileDistance(target.getPos3f(), zHeight, target.getWinches(), (float)((double)(target.getRotation() - 90.0f) * Math.PI / 180.0), (float)((double)angle * Math.PI / 180.0));
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will now " + (target.getTemplateId() == 936 ? "shoot" : "throw") + " approximately " + Math.round(approxDistance / 4.0f) + " tiles with " + (target.getTemplateId() == 937 ? "a loaded weight of " + target.getWinches() : target.getWinches() + " winches") + " and an angle of around " + angle + " degrees.");
            return true;
        }
        return false;
    }

    private void fireCatapult(Action act, Creature performer, Item target) {
        int sy;
        if (target.getData() <= 0L) {
            return;
        }
        if (target.getWinches() < 10 && target.getTemplateId() != 937) {
            performer.getCommunicator().sendNormalServerMessage("You need to winch the " + target.getName() + " more before it can fire properly.");
            return;
        }
        if (!target.isOnSurface()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot fire the " + target.getName() + " below ground.");
            return;
        }
        int sx = target.getTileX();
        VolaTile t = Zones.getTileOrNull(sx, sy = target.getTileY(), target.isOnSurface());
        if (t != null && t.getStructure() != null && target.onBridge() == -10L) {
            performer.getCommunicator().sendNormalServerMessage("You cannot fire the " + target.getName() + " inside a structure.");
            return;
        }
        if (target.getTemplateId() == 937) {
            if (!target.mayFireTrebuchet()) {
                performer.getCommunicator().sendNormalServerMessage("The trebuchet still hasn't stabilized itself enough to fire again.");
                return;
            }
            if (target.isEmpty(false)) {
                performer.getCommunicator().sendNormalServerMessage("There is no counter weight in the trebuchet, so it will not be able to fire.");
                return;
            }
            if (!Terraforming.isFlat(target.getTileX(), target.getTileY(), target.isOnSurface(), 5)) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " must stand on more level ground in order to be fired.");
                return;
            }
            int weight = 0;
            for (Item i : target.getAllItems(true)) {
                weight += i.getWeightGrams();
            }
            target.setWinches((byte)Math.min(50, weight / 20000));
            if (target.getWinches() < 25) {
                performer.getCommunicator().sendNormalServerMessage(" You cannot fire now. You need more counter weight.");
                return;
            }
        }
        Item projectile = null;
        try {
            projectile = Items.getItem(target.getData());
            if (projectile.isBanked() || projectile.getOwnerId() != -10L) {
                performer.getCommunicator().sendNormalServerMessage("The " + projectile.getName() + " crumbles to dust in mid-air.");
                if (performer.getVisionArea() != null) {
                    performer.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
                }
                return;
            }
            if (projectile.isUnfinished()) {
                performer.getCommunicator().sendNormalServerMessage("The " + projectile.getName() + " can't be fired in this state.");
                return;
            }
        }
        catch (NoSuchItemException nsi) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " was empty.");
            target.setData(0L);
            target.setWinches((short)0);
            if (performer.getVisionArea() != null) {
                performer.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
            }
            return;
        }
        if (target.getWinches() < 10) {
            performer.getCommunicator().sendNormalServerMessage(" You cannot fire now. You need to tighten the rope more.");
            return;
        }
        float dist = target.getWinches() + 1;
        float damdealt = 0.0f;
        if ((projectile != null && projectile.isMetal() || projectile.isStone() || projectile.isCorpse() || projectile.getTemplateId() == 932) && projectile.getTemplateId() != 298 && projectile.getTemplateId() != 26 && !projectile.isEgg()) {
            damdealt = (10.0f + dist / 10.0f) * projectile.getCurrentQualityLevel() / 100.0f;
            if (target.getTemplateId() != 936) {
                damdealt = damdealt * (float)projectile.getWeightGrams() / 10000.0f;
            }
            if (performer.getCitizenVillage() != null) {
                damdealt *= 1.0f + performer.getCitizenVillage().getFaithWarBonus() / 100.0f;
            }
            damdealt *= (float)(1 + act.getRarity());
            damdealt *= (float)(1 + projectile.getRarity());
            damdealt *= 1.0f + (float)target.getRarity() * 0.1f;
            if (projectile.isCorpse()) {
                damdealt *= 0.1f;
            } else if (target.getTemplateId() == 937) {
                damdealt *= 2.0f;
            }
        }
        float rot = target.getRotation();
        rot = Creature.normalizeAngle(rot - 90.0f);
        int newx = (int)(Math.cos((double)rot * Math.PI / 180.0) * (double)dist) + sx;
        int newy = (int)(Math.sin((double)rot * Math.PI / 180.0) * (double)dist) + sy;
        if (performer.getPower() >= 5 && !Features.Feature.NEW_PROJECTILES.isEnabled()) {
            performer.getCommunicator().sendNormalServerMessage("You are firing from " + sx + "," + sy + " dist:" + dist + " with rotation " + rot + " to " + newx + "," + newy + ".");
        }
        try {
            ServerProjectile proj = new ServerProjectile(target, projectile, newx * 4 + 2, newy * 4 + 2, performer, act.getRarity(), damdealt);
            if (!proj.fire(target.isOnSurface())) {
                ServerProjectile.removeProjectile(proj);
            }
            if (performer.getVisionArea() != null) {
                performer.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
            }
        }
        catch (NoSuchZoneException nsz) {
            performer.getCommunicator().sendNormalServerMessage("The catapult malfunctions, and you suspect it will need to be turned or moved.");
        }
    }

    private void unwindCatapult(Creature performer, Item target) {
        if (target.getData() <= 0L) {
            return;
        }
        if (target.getWinches() <= 0) {
            return;
        }
        performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " makes a whirring sound as you release the tension on the winch.");
        Server.getInstance().broadCastAction("The " + target.getName() + " makes a whirring sound as " + performer.getName() + " releases the tension on the winch.", performer, 5);
        target.setWinches((short)0);
        if (performer.getVisionArea() != null) {
            performer.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
        }
    }

    private final boolean winchCatapult(Creature performer, Action act, Item target, short action, float counter) {
        boolean done;
        if (target.getData() <= 0L) {
            return true;
        }
        if (!act.justTickedSecond()) {
            return false;
        }
        if (target.getTemplateId() == 937) {
            performer.getCommunicator().sendNormalServerMessage("The trebuchet may not be winched.", (byte)3);
            return true;
        }
        int max = 50;
        if (Features.Feature.NEW_PROJECTILES.isEnabled()) {
            if (target.getTemplateId() == 936) {
                max = 40;
            } else if (target.getTemplateId() == 445) {
                max = 30;
            }
        }
        if (target.getWinches() >= max) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already winched to max.", (byte)3);
            return true;
        }
        int skillNum = 10077;
        if (target.getTemplateId() == 936) {
            skillNum = 10093;
        } else if (target.getTemplateId() == 937) {
            skillNum = 10094;
        }
        double speedModifier = 1.0 - performer.getSkills().getSkillOrLearn(skillNum).getKnowledge(0.0) / 300.0;
        if (!this.mayManouvre(performer, target)) {
            return true;
        }
        int nums = 1;
        if (action == 238) {
            nums = 5;
        } else if (action == 239) {
            nums = 10;
        }
        boolean bl = done = counter >= 2.0f && counter >= (float)((int)Math.round((double)nums * speedModifier));
        if (counter == 1.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " creaks as you put strain on the ropes.");
            if (!done) {
                performer.sendActionControl("Winding", true, (int)Math.round((double)(nums * 10) * speedModifier));
            }
        }
        if (done) {
            target.setWinches((short)Math.min(max, target.getWinches() + nums));
            float angle = 45.0f + (float)(target.getAuxData() * 5);
            float zHeight = (float)target.getTemplate().getSizeY() * 0.75f / 100.0f;
            float approxDistance = ServerProjectile.getProjectileDistance(target.getPos3f(), zHeight, target.getWinches(), (float)((double)(target.getRotation() - 90.0f) * Math.PI / 180.0), (float)((double)angle * Math.PI / 180.0));
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will now " + (target.getTemplateId() == 936 ? "shoot" : "throw") + " approximately " + Math.round(approxDistance / 4.0f) + " tiles with " + (target.getTemplateId() == 937 ? "a loaded weight of " + target.getWinches() : target.getWinches() + " winches") + " and an angle of around " + angle + " degrees.");
            Server.getInstance().broadCastAction(performer.getName() + " winches the ropes on the " + target.getName() + ".", performer, 5);
            if (performer.getVisionArea() != null) {
                performer.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
            }
        }
        performer.getStatus().modifyStamina(-200.0f);
        return done;
    }

    private void unloadCatapult(Creature performer, Item target) {
        boolean isLoaded;
        boolean bl = isLoaded = target.getData() > 0L;
        if (!isLoaded) {
            return;
        }
        if (performer.getInventory().mayCreatureInsertItem()) {
            try {
                Item it = Items.getItem(target.getData());
                if (!performer.canCarry(it.getWeightGrams())) {
                    performer.getCommunicator().sendNormalServerMessage("You are carrying too much to pick up the item that you're trying to unload.");
                    return;
                }
                if (!it.isBanked() && it.getOwnerId() == -10L) {
                    performer.getInventory().insertItem(it, true);
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The " + it.getName() + " crumbles to dust.");
                }
            }
            catch (NoSuchItemException nsi) {
                performer.getCommunicator().sendNormalServerMessage("Only scrap was found.", (byte)3);
            }
            target.setData(0L);
            if (performer.getVisionArea() != null) {
                performer.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
            }
            performer.getCommunicator().sendNormalServerMessage("You unload the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " unloads the " + target.getName() + ".", performer, 5);
        } else {
            performer.getCommunicator().sendNormalServerMessage("Your inventory is full.", (byte)3);
        }
    }

    private boolean loadCatapult(Creature performer, Action act, Item source, Item target, short action, float counter) {
        boolean isLoaded;
        boolean bl = isLoaded = target.getData() > 0L;
        if (isLoaded) {
            performer.getCommunicator().sendNormalServerMessage(StringUtil.format("Something is already loaded into the %s.", target.getName()), (byte)3);
            return true;
        }
        if (target.getTemplateId() == 936 && source.getTemplateId() != 932) {
            performer.getCommunicator().sendNormalServerMessage("You need to use ballista darts.", (byte)3);
            return true;
        }
        ArrayList<Item> items = new ArrayList<Item>(Arrays.asList(source.getAllItems(true)));
        items.add(source);
        for (Item item : items) {
            boolean mayLoad = false;
            if (WarmachineBehaviour.isItemLoadable(item) && item.getOwnerId() == performer.getWurmId()) {
                mayLoad = true;
            }
            if (!mayLoad) {
                performer.getCommunicator().sendNormalServerMessage(StringUtil.format("The %s may not be loaded into the %s.", item.getName(), target.getName()), (byte)3);
                return true;
            }
            if (!item.isTraded() && item.getOwnerId() == performer.getWurmId()) continue;
            performer.getCommunicator().sendNormalServerMessage("You cannot load that item now.", (byte)3);
            return true;
        }
        if (source.getSizeX() < 50 && source.getSizeY() < 50 && source.getSizeZ() < 50 || source.isCorpse() && source.getWeightGrams() < 100000) {
            boolean done;
            double skillModifier = 1.0 - performer.getSkills().getSkillOrLearn(10094).getKnowledge(0.0) / 300.0;
            boolean bl2 = done = counter >= 2.0f && (double)counter >= 8.0 * skillModifier || target.getTemplateId() != 937;
            if (counter == 1.0f && !done) {
                performer.getCommunicator().sendNormalServerMessage("You start to load the " + target.getName() + " with " + source.getNameWithGenus() + ".");
                performer.sendActionControl(Actions.actionEntrys[233].getActionString(), true, (int)Math.round(80.0 * skillModifier));
            }
            if (!done) {
                return false;
            }
            target.setData(source.getWurmId());
            performer.getCommunicator().sendNormalServerMessage("You load the " + target.getName() + " with " + source.getNameWithGenus() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " loads the " + target.getName() + " with " + source.getNameWithGenus() + ".", performer, 5);
            source.putInVoid();
            if (target.getTemplateId() == 937) {
                int weight = 0;
                for (Item i : target.getAllItems(true)) {
                    weight += i.getWeightGrams();
                }
                target.setWinches((byte)Math.min(50, weight / 20000));
            }
            if (performer.getVisionArea() != null) {
                performer.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " will not fit.", (byte)3);
        }
        return true;
    }

    private static boolean isItemLoadable(Item item) {
        return item != null && !item.isLiquid() && item.canBeDropped(true) && (!item.isBodyPart() || item.getAuxData() == 100) && !item.isArtifact() && !item.isComponentItem();
    }

    public static final boolean isLoaded(Item warmachine) {
        return warmachine.isWarmachine() && warmachine.getData() > 0L;
    }
}

