package com.wurmonline.server.combat;

import com.wurmonline.math.Vector2f;
import com.wurmonline.math.Vector3f;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProjectile implements MiscConstants {
   private static final Logger logger = Logger.getLogger(ServerProjectile.class.getName());
   public static final float meterPerSecond = 12.0F;
   private static final float gravity = 0.04F;
   private static final float newGravity = -9.8F;
   public static final int TICKS_PER_SECOND = 24;
   private final float posDownX;
   private final float posDownY;
   private final Item projectile;
   private float currentSecondsInAir = 0.0F;
   private long timeAtLanding = 0L;
   private final Creature shooter;
   private final Item weapon;
   private final byte rarity;
   private float damageDealth = 0.0F;
   private BlockingResult result = null;
   private static final CopyOnWriteArraySet<ServerProjectile> projectiles = new CopyOnWriteArraySet<>();
   private ServerProjectile.ProjectileInfo projectileInfo = null;
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
         float firingAngle = 45.0F + (float)(this.weapon.getAuxData() * 5);
         if (this.weapon.getTemplateId() == 936) {
            firingAngle = 7.5F;
         }

         ServerProjectile.ProjectileInfo projectileInfo = getProjectileInfo(this.weapon, this.shooter, firingAngle, 15.0F);
         this.projectileInfo = projectileInfo;
         VolaTile t = Zones.getOrCreateTile((int)(projectileInfo.endPosition.x / 4.0F), (int)(projectileInfo.endPosition.y / 4.0F), this.weapon.isOnSurface());
         Village v = Villages.getVillage((int)(projectileInfo.endPosition.x / 4.0F), (int)(projectileInfo.endPosition.y / 4.0F), this.weapon.isOnSurface());
         if (!isOkToAttack(t, this.getShooter(), this.getDamageDealt())) {
            boolean ok = false;
            if (v != null && (v.isActionAllowed((short)174, this.getShooter(), false, 0, 0) || v.isEnemy(this.getShooter()))) {
               ok = true;
            }

            if (!ok) {
               this.shooter
                  .getCommunicator()
                  .sendNormalServerMessage("You cannot fire the " + this.getProjectile().getName() + " to there, you are not allowed.");
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
         firingSkill.skillCheck((double)this.weapon.getWinches(), 0.0, false, (float)this.weapon.getWinches() / 5.0F);
         this.weapon.setData(0L);
         this.weapon.setWinches((short)0);
         if (this.weapon.getTemplateId() == 937) {
            int weight = 0;

            for(Item i : this.weapon.getAllItems(true)) {
               weight += i.getWeightGrams();
            }

            this.weapon.setWinches((short)((byte)Math.min(50, weight / 20000)));
         }

         this.timeAtLanding = System.currentTimeMillis() + projectileInfo.timeToImpact;
         VolaTile startTile = Zones.getOrCreateTile((int)(projectileInfo.startPosition.x / 4.0F), (int)(projectileInfo.startPosition.y / 4.0F), isOnSurface);
         startTile.sendNewProjectile(
            this.getProjectile().getWurmId(),
            (byte)2,
            this.getProjectile().getModelName(),
            this.getProjectile().getName(),
            this.getProjectile().getMaterial(),
            projectileInfo.startPosition,
            projectileInfo.startVelocity,
            projectileInfo.endPosition,
            this.weapon.getRotation(),
            this.weapon.isOnSurface()
         );
         VolaTile endTile = Zones.getOrCreateTile((int)(projectileInfo.endPosition.x / 4.0F), (int)(projectileInfo.endPosition.y / 4.0F), isOnSurface);
         endTile.sendNewProjectile(
            this.getProjectile().getWurmId(),
            (byte)2,
            this.getProjectile().getModelName(),
            this.getProjectile().getName(),
            this.getProjectile().getMaterial(),
            projectileInfo.startPosition,
            projectileInfo.startVelocity,
            projectileInfo.endPosition,
            this.weapon.getRotation(),
            this.weapon.isOnSurface()
         );
         return true;
      } else {
         float targetZ = Zones.calculateHeight(this.posDownX, this.posDownY, isOnSurface);
         this.result = calculateBlocker(this.weapon, this.posDownX, this.posDownY, targetZ);
         if (this.result == null) {
            logger.log(Level.INFO, "Blocker is null");
            return false;
         } else if (this.result.getFirstBlocker() != null) {
            float newx = (float)(this.result.getFirstBlocker().getTileX() * 4 + 2);
            float newy = (float)(this.result.getFirstBlocker().getTileY() * 4 + 2);
            Vector2f targPos = new Vector2f((float)(this.weapon.getTileX() * 4 + 2), (float)(this.weapon.getTileY() * 4 + 2));
            Vector2f projPos = new Vector2f(newx, newy);
            float dist = projPos.subtract(targPos).length() / 4.0F;
            if (dist < 8.0F) {
               if (this.shooter.getPower() > 0 && Servers.isThisATestServer()) {
                  this.shooter
                     .getCommunicator()
                     .sendNormalServerMessage(
                        "Calculated block from " + this.weapon.getPosX() + "," + this.weapon.getPosY() + " dist:" + dist + " at " + newx + "," + newy + "."
                     );
               }

               this.shooter.getCommunicator().sendNormalServerMessage(" You cannot fire at such a short range.");
               return false;
            } else {
               this.weapon.setData(0L);
               this.weapon.setWinches((short)0);
               this.setTimeAtLanding(System.currentTimeMillis() + (long)(this.result.getActualBlockingTime() * 1000.0F));
               VolaTile tile = Zones.getOrCreateTile(this.weapon.getTileX(), this.weapon.getTileY(), isOnSurface);
               tile.sendProjectile(
                  this.getProjectile().getWurmId(),
                  (byte)(this.weapon.getTemplateId() == 936 ? 9 : 2),
                  this.getProjectile().getModelName(),
                  this.getProjectile().getName(),
                  this.getProjectile().getMaterial(),
                  this.weapon.getPosX(),
                  this.weapon.getPosY(),
                  this.weapon.getPosZ(),
                  this.weapon.getRotation(),
                  (byte)0,
                  projPos.x,
                  projPos.y,
                  targetZ,
                  this.weapon.getWurmId(),
                  -10L,
                  this.result.getEstimatedBlockingTime(),
                  this.result.getActualBlockingTime()
               );
               tile = Zones.getOrCreateTile((int)(projPos.x / 4.0F), (int)(projPos.y / 4.0F), true);
               tile.sendProjectile(
                  this.getProjectile().getWurmId(),
                  (byte)(this.weapon.getTemplateId() == 936 ? 9 : 2),
                  this.getProjectile().getModelName(),
                  this.getProjectile().getName(),
                  this.getProjectile().getMaterial(),
                  this.weapon.getPosX(),
                  this.weapon.getPosY(),
                  this.weapon.getPosZ(),
                  this.weapon.getRotation(),
                  (byte)0,
                  projPos.x,
                  projPos.y,
                  targetZ,
                  this.weapon.getWurmId(),
                  -10L,
                  this.result.getEstimatedBlockingTime(),
                  this.result.getActualBlockingTime()
               );
               if (this.shooter.getPower() >= 5) {
                  this.shooter
                     .getCommunicator()
                     .sendNormalServerMessage(
                        "You hit tile ("
                           + this.result.getFirstBlocker().getTileX()
                           + ","
                           + this.result.getFirstBlocker().getTileY()
                           + "), distance: "
                           + dist
                           + "."
                     );
               }

               if (this.weapon.getTemplateId() == 937) {
                  this.weapon.setLastMaintained(WurmCalendar.currentTime);
               }

               return true;
            }
         } else {
            logger.log(Level.INFO, "No blocker");
            return false;
         }
      }
   }

   public long getTimeAtLanding() {
      return this.timeAtLanding;
   }

   public void setTimeAtLanding(long aTimeAtLanding) {
      this.timeAtLanding = aTimeAtLanding;
   }

   public static final void clear() {
      for(ServerProjectile projectile : projectiles) {
         projectile.poll(Long.MAX_VALUE);
      }
   }

   public static final boolean isOkToAttack(VolaTile t, Creature performer, float damdealt) {
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
                     performer.modifyFaith(-0.25F);
                     performer.maybeModifyAlignment(-1.0F);
                  }
               } else {
                  ok = true;
               }
            }
         }
      }

      Structure structure = t.getStructure();
      if (structure != null && structure.isTypeBridge()) {
         ok = true;
      }

      if (structure != null && structure.isTypeHouse() && damdealt > 0.0F) {
         if (v != null && v.isEnemy(performer)) {
            ok = true;
         }

         if (performer.getKingdomTemplateId() != 3) {
            byte ownerkingdom = Players.getInstance().getKingdomForPlayer(structure.getOwnerId());
            if (!performer.isFriendlyKingdom(ownerkingdom)) {
               ok = true;
            } else {
               ok = false;
               boolean found = false;
               if ((!t.isInPvPZone() || v != null) && structure.isFinished() && structure.isLocked()) {
                  found = structure.mayModify(performer) || t.isInPvPZone();
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

   public static final boolean setEffects(
      Item weapon, Item projectile, int newx, int newy, float dist, int floorLevelDown, Creature performer, byte rarity, float damdealt
   ) {
      try {
         Zones.getZone(newx, newy, weapon.isOnSurface());
         VolaTile t = Zones.getOrCreateTile(newx, newy, weapon.isOnSurface());
         String whatishit = "the ground";
         boolean hit = false;
         boolean ok = isOkToAttack(t, performer, damdealt);
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
         } catch (NoSuchSkillException var31) {
            cataskill = performer.getSkills().learn(skilltype, 1.0F);
         }

         if (structure != null && structure.isTypeHouse()) {
            hit = true;
            whatishit = structure.getName();
            if (!t.isInPvPZone() && !ok && performer.getKingdomTemplateId() != 3) {
               byte ownerkingdom = Players.getInstance().getKingdomForPlayer(structure.getOwnerId());
               if (performer.isFriendlyKingdom(ownerkingdom)) {
                  damdealt = 0.0F;
                  hit = false;
               }
            }

            if (hit && !doneSkillRoll) {
               pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0F);
               doneSkillRoll = true;
            }

            if (pwr > 0.0) {
               if (damdealt > 0.0F) {
                  int destroyed = 0;
                  Floor f = t.getTopFloor();
                  if (f != null) {
                     floorLevel = f.getFloorLevel();
                  }

                  Wall w = t.getTopWall();
                  if (w != null && w.getFloorLevel() > floorLevel) {
                     floorLevel = w.getFloorLevel();
                  }

                  Fence fence = t.getTopFence();
                  if (fence != null && fence.getFloorLevel() > floorLevel) {
                     floorLevel = fence.getFloorLevel();
                  }

                  boolean logged = false;
                  float mod = 2.0F;
                  if (floorLevel > 0) {
                     Floor[] floors = t.getFloors(floorLevel * 30, floorLevel * 30);
                     if (floors.length > 0) {
                        for(int x = 0; x < floors.length; ++x) {
                           float newdam = floors[x].getDamage() + Math.min(20.0F, floors[x].getDamageModifier() * damdealt / 2.0F);
                           if (newdam >= 100.0F) {
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
                  }

                  Wall[] warr = t.getWalls();

                  for(int x = 0; x < warr.length; ++x) {
                     if (warr[x].getFloorLevel() == floorLevel) {
                        float newdam = warr[x].getDamage() + Math.min(warr[x].isFinished() ? 20.0F : 100.0F, warr[x].getDamageModifier() * damdealt / 2.0F);
                        if (newdam >= 100.0F) {
                           if (!logged) {
                              logged = true;
                              TileEvent.log(warr[x].getTileX(), warr[x].getTileY(), 0, performer.getWurmId(), 236);
                           }

                           ++destroyed;
                        }

                        warr[x].setDamage(newdam);
                        arrowStuck = true;
                     }
                  }

                  Floor[] floors = t.getFloors();

                  for(int x = 0; x < floors.length; ++x) {
                     if (floors[x].getFloorLevel() == floorLevel) {
                        float newdam = floors[x].getDamage() + Math.min(20.0F, floors[x].getDamageModifier() * damdealt / 2.0F);
                        if (newdam >= 100.0F) {
                           if (!logged) {
                              logged = true;
                              TileEvent.log(floors[x].getTileX(), floors[x].getTileY(), 0, performer.getWurmId(), 236);
                           }

                           ++destroyed;
                        }

                        floors[x].setDamage(newdam);
                        arrowStuck = true;
                     }
                  }

                  if (destroyed > 0 && !ok) {
                     performer.getCommunicator().sendNormalServerMessage("You feel very bad about this.");
                     performer.maybeModifyAlignment(-5.0F);
                     performer.punishSkills(0.1 * (double)Math.min(3, destroyed), false);
                  }

                  alertGuards(performer, newx, newy, ok, t, destroyed);
               }

               if (damdealt > 0.0F) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You seem to have hit " + t.getStructure().getName() + "!" + (Servers.isThisATestServer() ? " Dealt:" + damdealt : "")
                     );
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage("You seem to have hit " + t.getStructure().getName() + " but luckily it took no damage!");
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
               pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0F);
               doneSkillRoll = true;
            }

            if (!(pwr > 0.0)) {
               performer.getCommunicator().sendNormalServerMessage("You just missed " + t.getStructure().getName() + ".");
            } else {
               if (damdealt > 0.0F) {
                  for(BridgePart bp : t.getBridgeParts()) {
                     float mod = bp.getModByMaterial();
                     float newdam = bp.getDamage() + Math.min(20.0F, bp.getDamageModifier() * damdealt / mod);
                     TileEvent.log(bp.getTileX(), bp.getTileY(), 0, performer.getWurmId(), 236);
                     bp.setDamage(newdam);
                  }
               }

               if (damdealt > 0.0F) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You seem to have hit " + t.getStructure().getName() + "!" + (Servers.isThisATestServer() ? " Dealt:" + damdealt : "")
                     );
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage("You seem to have hit " + t.getStructure().getName() + " but luckily it took no damage!");
               }
            }
         }

         for(Fence fence : t.getFencesForLevel(floorLevel)) {
            hit = true;
            whatishit = "the " + fence.getName();
            Village vill = MethodsStructure.getVillageForFence(fence);
            if (!ok && vill != null) {
               if (vill.isActionAllowed((short)174, performer, false, 0, 0)) {
                  ok = true;
               } else if (!vill.isEnemy(performer)) {
                  hit = false;
                  damdealt = 0.0F;
               }
            }

            if (hit && !doneSkillRoll) {
               pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0F);
               doneSkillRoll = true;
            }

            if (pwr > 0.0) {
               if (damdealt > 0.0F) {
                  float mod = 2.0F;
                  TileEvent.log(fence.getTileX(), fence.getTileY(), 0, performer.getWurmId(), 236);
                  fence.setDamage(fence.getDamage() + Math.min(fence.isFinished() ? 20.0F : 100.0F, fence.getDamageModifier() * damdealt / 2.0F));
                  performer.getCommunicator().sendNormalServerMessage("You seem to have hit " + whatishit + "!");
                  arrowStuck = true;
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You just missed some fences with the " + projectile.getName() + ".");
            }
         }

         VolaTile southTile = Zones.getTileOrNull(newx, newy + 1, true);
         if (southTile != null) {
            for(Fence fence : southTile.getFencesForLevel(floorLevel)) {
               if (fence.isHorizontal()) {
                  hit = true;
                  whatishit = "the " + fence.getName();
                  Village vill = MethodsStructure.getVillageForFence(fence);
                  if (!ok && vill != null) {
                     if (vill.isActionAllowed((short)174, performer, false, 0, 0)) {
                        ok = true;
                     } else if (!vill.isEnemy(performer)) {
                        hit = false;
                        damdealt = 0.0F;
                     }
                  }

                  if (hit && !doneSkillRoll) {
                     pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0F);
                     doneSkillRoll = true;
                  }

                  if (pwr > 0.0) {
                     if (damdealt > 0.0F) {
                        float mod = 2.0F;
                        TileEvent.log(fence.getTileX(), fence.getTileY(), 0, performer.getWurmId(), 236);
                        fence.setDamage(fence.getDamage() + Math.min(20.0F, fence.getDamageModifier() * damdealt / 2.0F));
                        performer.getCommunicator().sendNormalServerMessage("You seem to have hit " + whatishit + "!");
                        arrowStuck = true;
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You just missed some fences with the " + projectile.getName() + ".");
                  }
               }
            }
         }

         VolaTile eastTile = Zones.getTileOrNull(newx + 1, newy, true);
         if (eastTile != null) {
            for(Fence fence : eastTile.getFencesForLevel(floorLevel)) {
               if (!fence.isHorizontal()) {
                  hit = true;
                  whatishit = "the " + fence.getName();
                  Village vill = MethodsStructure.getVillageForFence(fence);
                  if (!ok && vill != null) {
                     if (vill.isActionAllowed((short)174, performer, false, 0, 0)) {
                        ok = true;
                     } else if (!vill.isEnemy(performer)) {
                        hit = false;
                        damdealt = 0.0F;
                     }
                  }

                  if (hit && !doneSkillRoll) {
                     pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0F);
                     doneSkillRoll = true;
                  }

                  if (pwr > 0.0) {
                     if (damdealt > 0.0F) {
                        float mod = 2.0F;
                        TileEvent.log(fence.getTileX(), fence.getTileY(), 0, performer.getWurmId(), 236);
                        fence.setDamage(fence.getDamage() + Math.min(20.0F, fence.getDamageModifier() * damdealt / 2.0F));
                        performer.getCommunicator().sendNormalServerMessage("You seem to have hit " + whatishit + "!");
                        arrowStuck = true;
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You just missed some fences with the " + projectile.getName() + ".");
                  }
               }
            }
         }

         if (testHitCreaturesOnTile(t, performer, projectile, damdealt, dist, floorLevel)) {
            if (weapon.getTemplateId() != 936 || !arrowStuck) {
               if (weapon.getTemplateId() == 936) {
                  damdealt *= 3.0F;
               }

               if (!doneSkillRoll) {
                  pwr = cataskill.skillCheck((double)dist - 9.0, weapon, 0.0, false, 10.0F);
                  doneSkillRoll = true;
               }

               boolean hit2 = hitCreaturesOnTile(t, pwr, performer, projectile, damdealt, dist, floorLevel);
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
      } catch (NoSuchZoneException var32) {
         performer.getCommunicator().sendNormalServerMessage("You hit nothing with the " + projectile.getName() + ".");
         Items.destroyItem(weapon.getData());
         return true;
      }

      return projectile.deleted;
   }

   private static final void alertGuards(Creature performer, int newx, int newy, boolean ok, VolaTile t, int destroyed) {
      try {
         if (!MethodsItems.mayTakeThingsFromStructure(performer, null, newx, newy) && !ok) {
            Structure struct = t.getStructure();
            if (struct != null && struct.isFinished()) {
               for(VirtualZone vz : t.getWatchers()) {
                  try {
                     if (vz.getWatcher() != null && vz.getWatcher().getCurrentTile() != null && performer.isFriendlyKingdom(vz.getWatcher().getKingdomId())) {
                        boolean cares = false;
                        if (vz.getWatcher().isKingdomGuard()) {
                           cares = true;
                        }

                        if (!cares) {
                           cares = struct.isGuest(vz.getWatcher());
                        }

                        if (cares
                           && (Math.abs(vz.getWatcher().getCurrentTile().tilex - newx) <= 20 || Math.abs(vz.getWatcher().getCurrentTile().tiley - newy) <= 20)
                           && cares
                           && performer.getStealSkill()
                                 .skillCheck(
                                    (double)(
                                       95
                                          - Math.min(
                                                Math.abs(vz.getWatcher().getCurrentTile().tilex - newx),
                                                Math.abs(vz.getWatcher().getCurrentTile().tiley - newy)
                                             )
                                             * 5
                                    ),
                                    0.0,
                                    true,
                                    10.0F
                                 )
                              < 0.0
                           && (!Servers.localServer.PVPSERVER || destroyed > 0)) {
                           performer.setReputation(performer.getReputation() - 10);
                           performer.getCommunicator().sendNormalServerMessage("People notice you. This is bad for your reputation!", (byte)2);
                           break;
                        }
                     }
                  } catch (Exception var12) {
                     logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
                  }
               }
            }
         }
      } catch (NoSuchStructureException var13) {
      }
   }

   private static final boolean testHitCreaturesOnTile(VolaTile t, Creature performer, Item projectile, float damdealt, float dist, int floorlevel) {
      boolean hit = false;
      Creature[] initialCreatures = t.getCreatures();
      Set<Creature> creatureSet = new HashSet<>();

      for(Creature c : initialCreatures) {
         if (t.getBridgeParts().length > 0) {
            if (c.getBridgeId() == t.getBridgeParts()[0].getStructureId()) {
               creatureSet.add(c);
            }
         } else if (c.getFloorLevel() == floorlevel) {
            creatureSet.add(c);
         }
      }

      Creature[] creatures = creatureSet.toArray(new Creature[creatureSet.size()]);
      if (creatures.length > 0) {
         boolean nonpvp = false;
         int x = Server.rand.nextInt(creatures.length);
         if (!creatures[x].isUnique() && !creatures[x].isInvulnerable() && creatures[x].getPower() == 0) {
            if (performer.isFriendlyKingdom(creatures[x].getKingdomId())
               && (!creatures[x].isOnPvPServer() || !performer.isOnPvPServer())
               && (performer.getCitizenVillage() == null || !performer.getCitizenVillage().isEnemy(creatures[x].getCitizenVillage()))) {
               nonpvp = true;
            }

            if (!nonpvp) {
               try {
                  hit = true;
               } catch (Exception var13) {
                  logger.log(Level.WARNING, creatures[x].getName() + var13.getMessage(), (Throwable)var13);
               }
            }
         }
      }

      return hit;
   }

   private static final boolean hitCreaturesOnTile(VolaTile t, double power, Creature performer, Item projectile, float damdealt, float dist, int floorlevel) {
      boolean hit = false;
      Creature[] initialCreatures = t.getCreatures();
      Set<Creature> creatureSet = new HashSet<>();

      for(Creature c : initialCreatures) {
         if (t.getBridgeParts().length > 0) {
            if (c.getBridgeId() == t.getBridgeParts()[0].getStructureId()) {
               creatureSet.add(c);
            }
         } else if (c.getFloorLevel() == floorlevel) {
            creatureSet.add(c);
         }
      }

      Creature[] creatures = creatureSet.toArray(new Creature[creatureSet.size()]);
      if (power > 0.0 && creatures.length > 0) {
         boolean nonpvp = false;
         int x = Server.rand.nextInt(creatures.length);
         if (!creatures[x].isUnique() && !creatures[x].isInvulnerable() && creatures[x].getPower() == 0) {
            if (performer.isFriendlyKingdom(creatures[x].getKingdomId())) {
               if ((performer.getCitizenVillage() == null || !performer.getCitizenVillage().isEnemy(creatures[x].getCitizenVillage()))
                  && !performer.hasBeenAttackedBy(creatures[x].getWurmId())
                  && creatures[x].getCurrentKingdom() == creatures[x].getKingdomId()) {
                  performer.setUnmotivatedAttacker();
               }

               if (creatures[x].isOnPvPServer() && performer.isOnPvPServer()) {
                  if (Servers.localServer.HOMESERVER
                     && !performer.isOnHostileHomeServer()
                     && creatures[x].getReputation() >= 0
                     && (creatures[x].citizenVillage == null || !creatures[x].citizenVillage.isEnemy(performer))) {
                     performer.setReputation(performer.getReputation() - 30);
                     performer.getCommunicator().sendAlertServerMessage("This is bad for your reputation.");
                  }
               } else if (performer.getCitizenVillage() == null || !performer.getCitizenVillage().isEnemy(creatures[x].getCitizenVillage())) {
                  nonpvp = true;
               }
            }

            if (!nonpvp) {
               try {
                  creatures[x].getCommunicator().sendAlertServerMessage("You are hit by some " + projectile.getName() + " coming through the air!");
                  if (!creatures[x].isPlayer()) {
                     creatures[x].setTarget(performer.getWurmId(), false);
                     creatures[x].setFleeCounter(20);
                  }

                  if (damdealt > 0.0F) {
                     if (creatures[x].isPlayer()) {
                        boolean dead = creatures[x]
                           .addWoundOfType(performer, (byte)0, 1, true, 1.0F, false, (double)Math.min(25000.0F, damdealt * 1000.0F), 0.0F, 0.0F, false, false);
                        performer.achievement(47);
                        if (dead) {
                           creatures[x].achievement(48);
                        }
                     } else {
                        creatures[x]
                           .getBody()
                           .addWound(
                              new TempWound(
                                 (byte)0,
                                 creatures[x].getBody().getRandomWoundPos(),
                                 Math.min(25000.0F, damdealt * 1000.0F),
                                 creatures[x].getWurmId(),
                                 0.0F,
                                 0.0F,
                                 false
                              )
                           );
                     }
                  }

                  hit = true;
                  performer.getCommunicator().sendNormalServerMessage("You hit " + creatures[x].getNameWithGenus() + "!");
               } catch (Exception var15) {
                  logger.log(Level.WARNING, creatures[x].getName() + var15.getMessage(), (Throwable)var15);
               }
            }
         } else if (creatures[x].isVisible()) {
            performer.getCommunicator()
               .sendNormalServerMessage(creatures[x].getNameWithGenus() + " dodges your " + projectile.getName() + " with no problem.");
         }
      }

      return hit;
   }

   public static final BlockingResult calculateBlocker(Item weapon, float estimatedEndPosX, float estimatedEndPosY, float estimatedPosZ) throws NoSuchZoneException {
      Vector3f startPos = new Vector3f((float)(weapon.getTileX() * 4 + 2), (float)(weapon.getTileY() * 4 + 2), weapon.getPosZ());
      Vector3f currentPos = startPos.clone();
      Vector3f targetPos = new Vector3f(estimatedEndPosX, estimatedEndPosY, estimatedPosZ);
      Vector3f vector = targetPos.subtract(startPos);
      float length = vector.length();
      Vector3f dir = vector.normalize();
      float totalTimeInAir = length / 12.0F;
      float speed = 0.5F;
      float hVelocity = totalTimeInAir * 24.0F * 0.02F;
      hVelocity += dir.z * 0.5F;
      boolean hitSomething = false;
      float stepLength = 2.0F;
      float secondsPerStep = 0.16666667F;
      float gravityPerStep = 0.16F;
      float lastGroundHeight = Zones.calculateHeight(currentPos.getX(), currentPos.getY(), true);
      BlockingResult toReturn = null;
      float timeMoved = 0.0F;

      while(!hitSomething) {
         timeMoved += 0.16666667F;
         Vector3f lastPos = currentPos.clone();
         hVelocity -= 0.16F;
         currentPos.z += hVelocity;
         currentPos.x += dir.x * 2.0F;
         currentPos.y += dir.y * 2.0F;
         float groundHeight = Zones.calculateHeight(currentPos.getX(), currentPos.getY(), true);
         toReturn = Blocking.getBlockerBetween(
            null,
            lastPos.getX(),
            lastPos.getY(),
            currentPos.getX(),
            currentPos.getY(),
            lastPos.getZ(),
            currentPos.z,
            true,
            true,
            true,
            4,
            -10L,
            -10L,
            -10L,
            false
         );
         if (currentPos.getZ() < groundHeight - 1.0F) {
            toReturn = new BlockingResult();
            toReturn.addBlocker(
               new PathTile(
                  (int)currentPos.getX() / 4,
                  (int)currentPos.getY() / 4,
                  Server.surfaceMesh.getTile((int)currentPos.getX() / 4, (int)currentPos.getY() / 4),
                  true,
                  0
               ),
               currentPos,
               100.0F
            );
            logger.log(
               Level.INFO,
               "Hit ground at "
                  + (int)(currentPos.getX() / 4.0F)
                  + ","
                  + (int)(currentPos.getY() / 4.0F)
                  + " height was "
                  + groundHeight
                  + ", compared to "
                  + currentPos.getZ()
            );
            toReturn.setEstimatedBlockingTime(totalTimeInAir);
            toReturn.setActualBlockingTime(timeMoved);
            return toReturn;
         }

         if (toReturn != null) {
            toReturn.setEstimatedBlockingTime(totalTimeInAir);
            toReturn.setActualBlockingTime(timeMoved);
            hitSomething = true;
         }
      }

      return toReturn;
   }

   public static final float getProjectileDistance(Vector3f startingPosition, float heightOffset, float power, float rotation, float firingAngle) {
      Vector3f startingVelocity = new Vector3f(
         (float)((double)power * Math.cos((double)rotation) * Math.cos((double)firingAngle)),
         (float)((double)power * Math.sin((double)rotation) * Math.cos((double)firingAngle)),
         (float)((double)power * Math.sin((double)firingAngle))
      );
      float offsetModifier = heightOffset / (startingVelocity.z / -9.8F * startingVelocity.z);
      float flightTime = startingVelocity.z * (2.0F - offsetModifier) / -9.8F;
      startingVelocity.z = 0.0F;
      Vector3f endingPosition = startingPosition.add(startingVelocity.mult(flightTime));
      return endingPosition.distance(startingPosition.add(0.0F, 0.0F, heightOffset));
   }

   public static final ServerProjectile.ProjectileInfo getProjectileInfo(Item weapon, Creature cret, float averageFiringAngle, float firingAngleVariationMax) throws NoSuchZoneException {
      Vector3f landingPosition = new Vector3f();
      int power = weapon.getWinches();
      float angleVar = 1.0F - Math.abs(averageFiringAngle - 45.0F) / 45.0F;
      float tiltAngle = averageFiringAngle - firingAngleVariationMax * angleVar;
      float rotation = (float)((double)(weapon.getRotation() - 90.0F) * Math.PI / 180.0);
      Skill firingSkill = null;
      int skillType = 10077;
      if (weapon.getTemplateId() == 936) {
         skillType = 10093;
         power = Math.min(40, power);
         power = (int)((float)power * 1.5F);
      } else if (weapon.getTemplateId() == 937) {
         skillType = 10094;
         power = Math.min(50, power);
      } else {
         power = Math.min(30, power);
      }

      try {
         firingSkill = cret.getSkills().getSkill(skillType);
      } catch (NoSuchSkillException var26) {
         firingSkill = cret.getSkills().learn(skillType, 1.0F);
      }

      double skillModifier = firingSkill.skillCheck((double)power, 0.0, true, 1.0F) / 100.0;
      float qlModifier = (100.0F - weapon.getCurrentQualityLevel()) / 100.0F;
      tiltAngle = (float)((double)tiltAngle + (double)(firingAngleVariationMax * angleVar * qlModifier) * skillModifier);
      if (skillModifier < 0.0) {
         tiltAngle /= 3.0F;
         power /= 2;
         cret.getCommunicator()
            .sendNormalServerMessage("Something goes wrong when you fire the " + weapon.getName() + " and it doesn't fire as far as you expected.");
      }

      tiltAngle = (float)((double)tiltAngle * (Math.PI / 180.0));
      Vector3f currentVelocity = new Vector3f(
         (float)((double)power * Math.cos((double)rotation) * Math.cos((double)tiltAngle)),
         (float)((double)power * Math.sin((double)rotation) * Math.cos((double)tiltAngle)),
         (float)((double)power * Math.sin((double)tiltAngle))
      );
      Vector3f currentPos = weapon.getPos3f();
      currentPos.z += (float)weapon.getTemplate().getSizeY() * 0.75F / 100.0F;
      Vector3f startingPosition = currentPos.clone();
      Vector3f startingVelocity = currentVelocity.clone();
      Vector3f nextPos = null;
      BlockingResult blocker = null;
      float stepAmount = 2.0F / currentVelocity.length();
      float gravityPerStep = -9.8F * stepAmount;
      long flightTime = 0L;

      for(boolean landed = false; !landed; currentPos = nextPos) {
         flightTime = (long)((float)flightTime + stepAmount * 1000.0F);
         nextPos = currentPos.add(currentVelocity.mult(stepAmount));
         blocker = Blocking.getBlockerBetween(
            null,
            currentPos.getX(),
            currentPos.getY(),
            nextPos.getX(),
            nextPos.getY(),
            currentPos.getZ() - 0.5F,
            nextPos.getZ() - 0.5F,
            weapon.isOnSurface(),
            weapon.isOnSurface(),
            true,
            4,
            -10L,
            weapon.getBridgeId(),
            -10L,
            false
         );
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
      }

      ServerProjectile.ProjectileInfo toReturn = new ServerProjectile.ProjectileInfo(
         startingPosition, startingVelocity, landingPosition, currentVelocity, flightTime
      );
      return toReturn;
   }

   private final boolean poll(long now) {
      if (Features.Feature.NEW_PROJECTILES.isEnabled() && this.weapon.getTemplateId() != 936) {
         if (now <= this.timeAtLanding) {
            if (this.timeAtLanding - now <= 500L && !this.sentEffect) {
               EffectFactory.getInstance()
                  .createGenericTempEffect(
                     "dust03",
                     this.projectileInfo.endPosition.x,
                     this.projectileInfo.endPosition.y,
                     this.projectileInfo.endPosition.z,
                     this.weapon.isOnSurface(),
                     -1.0F,
                     0.0F
                  );
               this.sentEffect = true;
            }

            return false;
         } else {
            float majorRadius = (float)(this.getProjectile().getSizeX(true) + this.getProjectile().getSizeY(true)) / 2.0F / 10.0F;
            float damageMultiplier = this.projectileInfo.endVelocity.length()
               / 30.0F
               * (this.weapon.getCurrentQualityLevel() / 300.0F + 0.33F)
               * ((float)this.getProjectile().getWeightGrams() / 20000.0F);
            float damage = 1.0F * damageMultiplier;
            if (this.getProjectile().isStone() || this.getProjectile().isMetal()) {
               damage *= 10.0F;
            } else if (this.getProjectile().isCorpse()) {
               damage *= 2.5F;
            }

            if (this.getProjectile().getTemplateId() == 298 || this.getProjectile().getTemplateId() == 26 || this.getProjectile().isEgg()) {
               damage /= 15.0F;
            }

            float extraDamage = (damage - 20.0F) / 4.0F;
            float minorRadius = 1.0F + extraDamage / 10.0F;
            damage = Math.min(20.0F, damage);
            float radius = majorRadius * minorRadius;
            int hitCounter = 0;
            int wallCount = 0;
            int fenceCount = 0;
            int floorCount = 0;
            int roofCount = 0;
            int bridgeCount = 0;
            int itemCount = 0;
            ArrayList<Item> itemHitList = null;
            ArrayList<Structure> structureHitList = null;

            for(int i = (int)((this.projectileInfo.endPosition.x - radius) / 4.0F); i <= (int)((this.projectileInfo.endPosition.x + radius) / 4.0F); ++i) {
               for(int j = (int)((this.projectileInfo.endPosition.y - radius) / 4.0F); j <= (int)((this.projectileInfo.endPosition.y + radius) / 4.0F); ++j) {
                  VolaTile tileInRadius = Zones.getOrCreateTile(i, j, this.weapon.isOnSurface());
                  if (tileInRadius != null) {
                     for(Creature c : tileInRadius.getCreatures()) {
                        if (!c.isUnique()
                           && !c.isInvulnerable()
                           && c.getPower() <= 0
                           && (c.isOnPvPServer() || !c.isHitched() && !c.isCaredFor() && (!c.isBranded() || c.mayManage(this.shooter)))) {
                           float distance = c.getPos3f().distance(this.projectileInfo.endPosition);
                           if (distance <= radius) {
                              if (c.isPlayer() && this.shooter.isFriendlyKingdom(c.getKingdomId()) && c != this.shooter) {
                                 if (this.shooter.getCitizenVillage() == null || !this.shooter.getCitizenVillage().isEnemy(c.getCitizenVillage())) {
                                    if (!this.shooter.hasBeenAttackedBy(c.getWurmId()) && c.getCurrentKingdom() == c.getKingdomId()) {
                                       this.shooter.setUnmotivatedAttacker();
                                    }

                                    if (!this.shooter.isOnPvPServer() || !c.isOnPvPServer()) {
                                       continue;
                                    }
                                 }

                                 if (Servers.localServer.HOMESERVER
                                    && !this.shooter.isOnHostileHomeServer()
                                    && c.getReputation() >= 0
                                    && (c.citizenVillage == null || !c.citizenVillage.isEnemy(this.shooter))) {
                                    this.shooter.setReputation(this.shooter.getReputation() - 30);
                                    this.shooter.getCommunicator().sendAlertServerMessage("This is bad for your reputation.");
                                 }
                              }

                              if (damage > 0.0F) {
                                 c.getCommunicator().sendAlertServerMessage("You are hit by " + this.projectile.getName() + " coming through the air!");
                                 if (!c.isPlayer()) {
                                    c.setTarget(this.shooter.getWurmId(), false);
                                    c.setFleeCounter(20);
                                 }

                                 this.shooter.getCommunicator().sendNormalServerMessage("You hit " + c.getNameWithGenus() + "!");
                                 float actualDam = distance > majorRadius ? Math.min(10.0F, extraDamage) : damage;
                                 if (c.isPlayer()) {
                                    boolean dead = c.addWoundOfType(
                                       this.shooter, (byte)0, 1, true, 1.0F, true, (double)Math.min(25000.0F, actualDam * 1000.0F), 0.0F, 0.0F, false, false
                                    );
                                    this.shooter.achievement(47);
                                    if (dead) {
                                       c.achievement(48);
                                       if (this.weapon.getTemplateId() == 445) {
                                          this.shooter.achievement(573);
                                       }
                                    }
                                 } else {
                                    boolean dead = c.addWoundOfType(
                                       this.shooter, (byte)0, 1, true, 1.0F, true, (double)Math.min(25000.0F, actualDam * 1000.0F), 0.0F, 0.0F, false, false
                                    );
                                    if (dead && this.weapon.getTemplateId() == 445) {
                                       this.shooter.achievement(573);
                                    }
                                 }
                              }

                              ++hitCounter;
                           }
                        }
                     }

                     if (Servers.localServer.PVPSERVER
                        || tileInRadius.getStructure() == null
                        || tileInRadius.getStructure().isActionAllowed(this.shooter, (short)174)) {
                        for(Wall w : tileInRadius.getWalls()) {
                           if (!w.isWallPlan() && Math.abs(w.getCenterPoint().z - this.projectileInfo.endPosition.z) <= 1.5F) {
                              float distance = w.isHorizontal()
                                 ? Math.abs(w.getCenterPoint().y - this.projectileInfo.endPosition.y)
                                 : Math.abs(w.getCenterPoint().x - this.projectileInfo.endPosition.x);
                              float actualDam = distance > majorRadius ? Math.min(10.0F, extraDamage) : damage;
                              if (distance <= radius) {
                                 ++wallCount;
                                 ++hitCounter;
                                 if (Servers.localServer.testServer) {
                                    this.shooter.getCommunicator().sendSafeServerMessage(w.getName() + " hit for " + w.getDamageModifier() * actualDam);
                                 }

                                 w.setDamage(w.getDamage() + w.getDamageModifier() * actualDam);

                                 try {
                                    if (structureHitList == null) {
                                       structureHitList = new ArrayList<>();
                                    }

                                    if (!structureHitList.contains(Structures.getStructure(w.getStructureId()))) {
                                       structureHitList.add(Structures.getStructure(w.getStructureId()));
                                    }
                                 } catch (NoSuchStructureException var30) {
                                 }
                              }
                           }
                        }

                        for(Fence f : tileInRadius.getFences()) {
                           if (Math.abs(f.getCenterPoint().z - this.projectileInfo.endPosition.z) <= 1.5F) {
                              float distance = f.isHorizontal()
                                 ? Math.abs(f.getCenterPoint().y - this.projectileInfo.endPosition.y)
                                 : Math.abs(f.getCenterPoint().x - this.projectileInfo.endPosition.x);
                              float actualDam = distance > majorRadius ? Math.min(10.0F, extraDamage) : damage;
                              if (distance <= radius) {
                                 ++fenceCount;
                                 ++hitCounter;
                                 if (Servers.localServer.testServer) {
                                    this.shooter.getCommunicator().sendSafeServerMessage(f.getName() + " hit for " + f.getDamageModifier() * actualDam);
                                 }

                                 f.setDamage(f.getDamage() + f.getDamageModifier() * actualDam);
                              }
                           }
                        }

                        for(Floor f : tileInRadius.getFloors()) {
                           if (!f.isAPlan()) {
                              float distance = Math.abs(f.getCenterPoint().z - this.projectileInfo.endPosition.z);
                              float actualDam = distance > majorRadius ? Math.min(10.0F, extraDamage) : damage;
                              if (distance <= radius) {
                                 if (f.isRoof()) {
                                    ++roofCount;
                                 } else {
                                    ++floorCount;
                                 }

                                 ++hitCounter;
                                 if (Servers.localServer.testServer) {
                                    this.shooter.getCommunicator().sendSafeServerMessage(f.getName() + " hit for " + f.getDamageModifier() * actualDam);
                                 }

                                 f.setDamage(f.getDamage() + f.getDamageModifier() * actualDam);

                                 try {
                                    if (structureHitList == null) {
                                       structureHitList = new ArrayList<>();
                                    }

                                    if (!structureHitList.contains(Structures.getStructure(f.getStructureId()))) {
                                       structureHitList.add(Structures.getStructure(f.getStructureId()));
                                    }
                                 } catch (NoSuchStructureException var29) {
                                 }
                              }
                           }
                        }

                        for(BridgePart bp : tileInRadius.getBridgeParts()) {
                           if (!bp.isAPlan() && bp.getCenterPoint().distance(this.projectileInfo.endPosition) <= 4.0F) {
                              ++bridgeCount;
                              ++hitCounter;
                              bp.setDamage(bp.getDamage() + bp.getDamageModifier() * damage);
                           }
                        }

                        for(Item it : tileInRadius.getItems()) {
                           if (!it.isIndestructible() && !it.isRoadMarker() && !it.isLocked() && !it.isVehicle()) {
                              if (it.isOwnerDestroyable() && it.lastOwner != this.shooter.getWurmId() && !this.shooter.isOnPvPServer()) {
                                 Village village = tileInRadius.getVillage();
                                 if (village == null || !village.isActionAllowed((short)83, this.shooter)) {
                                    continue;
                                 }
                              }

                              if ((
                                    (long)it.getZoneId() == -10L
                                       || !it.isKingdomMarker()
                                       || it.getKingdom() != this.shooter.getKingdomId()
                                       || this.shooter.getWurmId() == it.lastOwner
                                       || tileInRadius.getVillage() != null && tileInRadius.getVillage() == this.shooter.getCitizenVillage()
                                 )
                                 && it.getPos3f().distance(this.projectileInfo.endPosition) <= radius) {
                                 ++itemCount;
                                 if (itemHitList == null) {
                                    itemHitList = new ArrayList<>();
                                 }

                                 itemHitList.add(it);
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (itemHitList != null) {
               ++hitCounter;
               Item t = itemHitList.get(Server.rand.nextInt(itemHitList.size()));
               float actualDam = t.getPos3f().distance(this.projectileInfo.endPosition) > majorRadius ? Math.min(10.0F, extraDamage) : damage;
               t.setDamage(t.getDamage() + t.getDamageModifier() * actualDam / 25.0F);
            }

            if (hitCounter == 0) {
               this.shooter.getCommunicator().sendNormalServerMessage("It doesn't sound like the " + this.getProjectile().getName() + " hit anything.");
            } else {
               StringBuilder targetList = new StringBuilder();
               targetList.append("It sounds as though the " + this.getProjectile().getName() + " hit ");
               if (wallCount > 0) {
                  targetList.append((wallCount > 1 ? wallCount : "a") + " wall" + (wallCount > 1 ? "s" : "") + ", ");
               }

               if (fenceCount > 0) {
                  targetList.append((fenceCount > 1 ? fenceCount : "a") + " fence" + (fenceCount > 1 ? "s" : "") + ", ");
               }

               if (roofCount > 0) {
                  targetList.append((roofCount > 1 ? roofCount : "a") + " roof" + (roofCount > 1 ? "s" : "") + ", ");
               }

               if (floorCount > 0) {
                  targetList.append((floorCount > 1 ? floorCount : "a") + " floor" + (floorCount > 1 ? "s" : "") + ", ");
               }

               if (bridgeCount > 0) {
                  targetList.append((bridgeCount > 1 ? bridgeCount : "a") + " bridge part" + (bridgeCount > 1 ? "s" : "") + ", ");
               }

               if (itemCount > 0) {
                  targetList.append("an item, ");
               }

               targetList.append("and nothing else.");
               this.shooter.getCommunicator().sendNormalServerMessage(targetList.toString());
               if (structureHitList != null && !structureHitList.isEmpty()) {
                  for(Structure struct : structureHitList) {
                     if (struct.isDestroyed()) {
                        struct.totallyDestroy();
                        if (this.weapon.getTemplateId() == 445) {
                           this.shooter.achievement(51);
                        }
                     }
                  }

                  targetList.delete(0, targetList.length());
                  targetList.append("You managed to hit ");

                  for(int id = 0; id < structureHitList.size(); ++id) {
                     boolean hasMore = id < structureHitList.size() - 1;
                     targetList.append((hasMore && id > 0 ? "" : (id == 0 ? "" : "and ")) + structureHitList.get(id).getName());
                     if (hasMore && id + 1 < structureHitList.size() - 1) {
                        targetList.append(", ");
                     } else if (hasMore) {
                        targetList.append(" ");
                     } else {
                        targetList.append(".");
                     }
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
               } catch (NoSuchZoneException var28) {
                  var28.printStackTrace();
               }
            } else {
               this.shooter.getCommunicator().sendNormalServerMessage("The " + this.getProjectile().getName() + " crumbles to dust as it lands.");
            }

            if (Servers.localServer.testServer) {
               this.shooter
                  .getCommunicator()
                  .sendNormalServerMessage(
                     "[TEST] Projectile "
                        + this.getProjectile().getName()
                        + " landed, damage multiplier: "
                        + damageMultiplier
                        + ", damage: "
                        + damage
                        + " total things hit: "
                        + hitCounter
                        + ". Total distance: "
                        + this.projectileInfo.startPosition.distance(this.projectileInfo.endPosition)
                        + "m or "
                        + this.projectileInfo.startPosition.distance(this.projectileInfo.endPosition) / 4.0F
                        + " tiles."
                  );
            }

            return true;
         }
      } else if (now > this.timeAtLanding) {
         float newx = this.getPosDownX();
         float newy = this.getPosDownY();
         if (this.result != null && this.result.getFirstBlocker() != null) {
            newx = (float)(this.result.getFirstBlocker().getTileX() * 4 + 2);
            newy = (float)(this.result.getFirstBlocker().getTileY() * 4 + 2);
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
         } catch (NoSuchSkillException var32) {
            cataskill = this.getShooter().getSkills().learn(skilltype, 1.0F);
         }

         Vector2f targPos = new Vector2f(this.weapon.getPosX(), this.weapon.getPosY());
         Vector2f projPos = new Vector2f(newx, newy);
         float dist = Math.abs(projPos.subtract(targPos).length() / 4.0F);
         double power = 0.0;
         VolaTile droptile = Zones.getOrCreateTile((int)(newx / 4.0F), (int)(newy / 4.0F), true);
         int dropFloorLevel = 0;
         boolean hit = false;
         boolean itemDestroyed = false;

         try {
            Item i = this.getProjectile();
            if (this.result != null) {
               if (this.result.getFirstBlocker() != null) {
                  dropFloorLevel = droptile.getDropFloorLevel(this.result.getFirstBlocker().getFloorLevel());
                  if (this.result.getFirstBlocker().isTile()) {
                     itemDestroyed = setEffects(
                        this.getWeapon(),
                        i,
                        (int)(newx / 4.0F),
                        (int)(newy / 4.0F),
                        dist,
                        this.result.getFirstBlocker().getFloorLevel(),
                        this.getShooter(),
                        this.getRarity(),
                        this.getDamageDealt()
                     );
                  } else {
                     boolean hadSkillGainChance = false;
                     boolean messageSent = false;
                     String whatishit = "the " + this.result.getFirstBlocker().getName();
                     Village vill = MethodsStructure.getVillageForBlocker(this.result.getFirstBlocker());
                     VolaTile t = Zones.getOrCreateTile(this.result.getFirstBlocker().getTileX(), this.result.getFirstBlocker().getTileY(), true);
                     boolean ok = isOkToAttack(t, this.getShooter(), this.getDamageDealt());
                     if (!ok && vill != null) {
                        if (vill.isActionAllowed((short)174, this.getShooter(), false, 0, 0)) {
                           ok = true;
                        } else if (!vill.isEnemy(this.getShooter())) {
                           ok = false;
                        }
                     }

                     if (ok) {
                        power = cataskill.skillCheck((double)dist - 9.0, this.weapon, 0.0, false, 10.0F);
                        hadSkillGainChance = true;
                        hit = power > 0.0;
                        if (hit && this.getDamageDealt() > 0.0F) {
                           int fl = this.result.getFirstBlocker().getFloorLevel();
                           float mod = 1.0F;
                           float newDam = this.result.getFirstBlocker().getDamage()
                              + Math.min(20.0F, this.result.getFirstBlocker().getDamageModifier() * this.getDamageDealt() / 1.0F);
                           whatishit = this.result.getFirstBlocker().getName();
                           this.getShooter()
                              .getCommunicator()
                              .sendNormalServerMessage(
                                 "You seem to have hit "
                                    + whatishit
                                    + "!"
                                    + (Servers.isThisATestServer() ? " Dam:" + this.getDamageDealt() + " NewDam:" + newDam : "")
                              );
                           if (!this.result.getFirstBlocker().isFloor()) {
                              t.damageFloors(fl, fl + 1, Math.min(20.0F, this.result.getFirstBlocker().getDamageModifier() * this.getDamageDealt()));
                              if (this.result.getFirstBlocker().isHorizontal()) {
                                 VolaTile t2 = Zones.getTileOrNull(
                                    this.result.getFirstBlocker().getTileX() - 1, this.result.getFirstBlocker().getTileY(), true
                                 );
                                 if (t2 != null) {
                                    t2.damageFloors(fl, fl + 1, Math.min(20.0F, this.result.getFirstBlocker().getDamageModifier() * this.getDamageDealt()));
                                 }
                              }
                           }

                           this.result.getFirstBlocker().setDamage(newDam);
                           if (newDam >= 100.0F) {
                              TileEvent.log(
                                 this.result.getFirstBlocker().getTileX(), this.result.getFirstBlocker().getTileY(), 0, this.getShooter().getWurmId(), 236
                              );
                              if (this.result.getFirstBlocker().isFloor()) {
                                 t.removeFloor(this.result.getFirstBlocker());
                              }
                           }
                        }

                        itemDestroyed = this.projectile
                           .setDamage(
                              this.projectile.getDamage() + this.projectile.getDamageModifier() * (float)(20 + Server.rand.nextInt(Math.max(1, (int)dist)))
                           );
                        if (itemDestroyed) {
                           t.broadCast("A " + this.projectile.getName() + " comes flying through the air, hits " + whatishit + ", and shatters.");
                        } else {
                           t.broadCast("A " + this.projectile.getName() + " comes flying through the air and hits " + whatishit + ".");
                        }
                     } else {
                        this.getShooter().getCommunicator().sendNormalServerMessage("You seem to miss with the " + this.projectile.getName() + ".");
                        messageSent = true;
                     }

                     if (testHitCreaturesOnTile(droptile, this.getShooter(), i, this.getDamageDealt(), dist, dropFloorLevel)) {
                        if (!hadSkillGainChance) {
                           power = cataskill.skillCheck((double)dist - 9.0, this.weapon, 0.0, false, 10.0F);
                        }

                        hit = hitCreaturesOnTile(droptile, power, this.getShooter(), i, this.getDamageDealt(), dist, dropFloorLevel);
                     }

                     if (!hit && !messageSent) {
                        this.getShooter().getCommunicator().sendNormalServerMessage("You just missed with the " + this.projectile.getName() + ".");
                     }
                  }
               }

               if (!itemDestroyed) {
                  i.setPosXYZ(
                     newx,
                     newy,
                     Zones.calculateHeight(newx, newy, this.result.getFirstBlocker().getFloorLevel() >= 0) + (float)(Math.max(0, dropFloorLevel) * 3)
                  );
                  VolaTile vt = Zones.getOrCreateTile((int)(newx / 4.0F), (int)(newy / 4.0F), this.weapon.isOnSurface());
                  if (vt.getBridgeParts().length > 0) {
                     i.setOnBridge(vt.getBridgeParts()[0].getStructureId());
                  }

                  Zone z = Zones.getZone(i.getTileX(), i.getTileY(), this.result.getFirstBlocker().getFloorLevel() >= 0);
                  z.addItem(i);
                  logger.log(Level.INFO, "Adding " + i.getName() + " at " + (int)(newx / 4.0F) + "," + (int)(newy / 4.0F));
               }
            } else if (!setEffects(
               this.getWeapon(),
               i,
               (int)(this.getPosDownX() / 4.0F),
               (int)(this.getPosDownY() / 4.0F),
               dist,
               0,
               this.getShooter(),
               this.getRarity(),
               this.getDamageDealt()
            )) {
               VolaTile vt = Zones.getOrCreateTile((int)(newx / 4.0F), (int)(newy / 4.0F), this.weapon.isOnSurface());
               float newz = 0.0F;
               if (vt.getBridgeParts().length > 0) {
                  i.setOnBridge(-10L);
               } else {
                  newz = Zones.calculateHeight(this.getPosDownX(), this.getPosDownY(), false);
               }

               i.setPosXYZ(this.getPosDownX(), this.getPosDownY(), newz);
               Zone z = Zones.getZone(i.getTileX(), i.getTileY(), true);
               z.addItem(i);
               logger.log(Level.INFO, "Adding " + i.getName() + " at " + (int)(this.getPosDownX() / 4.0F) + "," + (int)(this.getPosDownY() / 4.0F));
            }
         } catch (NoSuchZoneException var31) {
            logger.log(Level.INFO, this.getProjectile().getModelName() + " projectile with id " + this.getProjectile().getWurmId() + " shot outside the map");
         }

         return true;
      } else {
         return false;
      }
   }

   public static void pollAll() {
      long now = System.currentTimeMillis();

      for(ServerProjectile projectile : projectiles) {
         if (projectile.poll(now)) {
            projectiles.remove(projectile);
         }
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
