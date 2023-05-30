package com.wurmonline.server.combat;

import com.wurmonline.math.Vector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MessageServer;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class Arrows implements MiscConstants, ItemMaterials, SoundNames, TimeConstants {
   private static Logger logger = Logger.getLogger(Arrows.class.getName());
   private static final Set<Arrows> arrows = new HashSet<>();
   private final Item arrow;
   private final Creature performer;
   private Creature defender;
   private boolean hitTarget;
   private final int tileArrowDownX;
   private final int tileArrowDownY;
   private final Arrows.ArrowHitting arrowHitting;
   private final Arrows.ArrowDestroy arrowDestroy;
   private final Item bow;
   private double damage;
   private float damMod;
   private float armourMod;
   private byte pos;
   private final float speed;
   private final float totalTime;
   private float time;
   private final boolean hittingCreature;
   private Item item;
   private String scoreString;
   private boolean dryRun;
   private double difficulty;
   private double bonus;

   public Arrows(
      Item aArrow, Creature aDefender, Arrows.ArrowHitting aArrowHitting, Arrows.ArrowDestroy aArrowDestroy, Item aBow, double aDamage, byte aPos, byte proj
   ) {
      this.hittingCreature = true;
      this.arrow = aArrow;
      this.defender = aDefender;
      this.arrowHitting = aArrowHitting;
      this.arrowDestroy = aArrowDestroy;
      this.bow = aBow;
      this.damage = aDamage;
      this.damMod = 0.0F;
      this.armourMod = 0.0F;
      this.tileArrowDownX = -1;
      this.tileArrowDownY = -1;
      this.performer = null;
      this.pos = aPos;
      this.dryRun = true;
      this.difficulty = 0.0;
      this.bonus = 0.0;
      this.time = 0.0F;
      aArrow.setPosXYZ(aBow.getPosX(), aBow.getPosY(), aBow.getPosZ() + 2.0F);
      if (aArrowHitting != Arrows.ArrowHitting.HIT
         && aArrowHitting != Arrows.ArrowHitting.HIT_NO_DAMAGE
         && aArrowHitting != Arrows.ArrowHitting.SHIELD
         && aArrowHitting != Arrows.ArrowHitting.EVASION) {
         this.hitTarget = false;
      } else {
         this.hitTarget = true;
      }

      float length = 1.0F;
      if (this.hitTarget) {
         float x = aDefender.getPosX() - aBow.getPosX();
         float y = aDefender.getPosY() - aBow.getPosY();
         float h = aDefender.getPositionZ() + aDefender.getAltOffZ() - (aBow.getPosZ() + 2.0F);
         Vector vector = new Vector(x, y, h);
         length = vector.length();
      }

      this.speed = this.arrow.getMaterial() == 21 ? 13.0F : 45.0F;
      this.totalTime = length / this.speed * 1000.0F;
      if (this.hitTarget) {
         double newrot = Math.atan2(
            (double)(aDefender.getPosY() - (float)((int)aArrow.getPosY())), (double)(aDefender.getPosX() - (float)((int)aArrow.getPosX()))
         );
         float attAngle = (float)(newrot * (180.0 / Math.PI)) - 90.0F;
         attAngle = Creature.normalizeAngle(attAngle);
         VolaTile tile = Zones.getOrCreateTile(aBow.getTileX(), aBow.getTileY(), aBow.isOnSurface());
         if (tile != null) {
            tile.sendProjectile(
               aArrow.getWurmId(),
               proj,
               aArrow.getModelName(),
               aArrow.getName(),
               aArrow.getMaterial(),
               aBow.getPosX(),
               aBow.getPosY(),
               aBow.getPosZ() + 2.0F,
               attAngle,
               (byte)(aBow.isOnSurface() ? 0 : -1),
               (float)((int)aDefender.getPosX()),
               (float)((int)aDefender.getPosY()),
               aDefender.getPositionZ() + aDefender.getAltOffZ(),
               aBow.getWurmId(),
               aDefender.getWurmId(),
               this.totalTime,
               this.totalTime
            );
         }

         tile = aDefender.getCurrentTile();
         if (tile != null) {
            tile.sendProjectile(
               aArrow.getWurmId(),
               proj,
               aArrow.getModelName(),
               aArrow.getName(),
               aArrow.getMaterial(),
               aBow.getPosX(),
               aBow.getPosY(),
               aBow.getPosZ() + 2.0F,
               attAngle,
               (byte)(aBow.isOnSurface() ? 0 : -1),
               (float)((int)aDefender.getPosX()),
               (float)((int)aDefender.getPosY()),
               aDefender.getPositionZ() + aDefender.getAltOffZ(),
               aBow.getWurmId(),
               aDefender.getWurmId(),
               this.totalTime,
               this.totalTime
            );
         }
      }
   }

   public Arrows(
      Item aArrow,
      Creature aPerformer,
      Creature aDefender,
      int aTileArrowDownX,
      int aTileArrowDownY,
      Arrows.ArrowHitting aArrowHitting,
      Arrows.ArrowDestroy aArrowDestroy,
      Item aBow1,
      double aDamage,
      float aDamMod,
      float aArmourMod,
      byte aPos,
      boolean dry,
      double diff,
      double bon
   ) {
      this.hittingCreature = true;
      this.arrow = aArrow;
      this.performer = aPerformer;
      this.defender = aDefender;
      this.tileArrowDownX = aTileArrowDownX;
      this.tileArrowDownY = aTileArrowDownY;
      this.arrowHitting = aArrowHitting;
      this.arrowDestroy = aArrowDestroy;
      this.bow = aBow1;
      this.damage = aDamage;
      this.damMod = aDamMod;
      this.armourMod = aArmourMod;
      this.pos = aPos;
      this.dryRun = dry;
      this.difficulty = diff;
      this.bonus = bon;
      this.time = 0.0F;
      aArrow.setPosXYZ(aPerformer.getPosX(), aPerformer.getPosY(), aPerformer.getPositionZ());
      if (aArrowHitting != Arrows.ArrowHitting.HIT
         && aArrowHitting != Arrows.ArrowHitting.HIT_NO_DAMAGE
         && aArrowHitting != Arrows.ArrowHitting.SHIELD
         && aArrowHitting != Arrows.ArrowHitting.EVASION) {
         this.hitTarget = false;
      } else {
         this.hitTarget = true;
      }

      float length = 1.0F;
      if (this.hitTarget) {
         float x = aDefender.getPosX() - aPerformer.getPosX();
         float y = aDefender.getPosY() - aPerformer.getPosY();
         float h = aDefender.getPositionZ() + aDefender.getAltOffZ() - aPerformer.getPositionZ() + aPerformer.getAltOffZ();
         Vector vector = new Vector(x, y, h);
         length = vector.length();
      } else {
         float x = (float)((aTileArrowDownX << 2) + 2) - aPerformer.getPosX();
         float y = (float)((aTileArrowDownY << 2) + 2) - aPerformer.getPosY();
         float h = aDefender.getPositionZ() + aDefender.getAltOffZ() - aPerformer.getPositionZ() + aPerformer.getAltOffZ();
         Vector vector = new Vector(x, y, h);
         length = vector.length();
      }

      this.speed = 45.0F;
      this.totalTime = length / this.speed * 1000.0F;
      if (this.hitTarget) {
         VolaTile tile = aPerformer.getCurrentTile();
         if (tile != null) {
            tile.sendProjectile(
               aArrow.getWurmId(),
               (byte)1,
               aArrow.getModelName(),
               aArrow.getName(),
               aArrow.getMaterial(),
               aPerformer.getPosX(),
               aPerformer.getPosY(),
               aPerformer.getPositionZ() + aPerformer.getAltOffZ(),
               aPerformer.getStatus().getRotation(),
               (byte)aPerformer.getLayer(),
               (float)((int)aDefender.getPosX()),
               (float)((int)aDefender.getPosY()),
               aDefender.getPositionZ() + aDefender.getAltOffZ(),
               aPerformer.getWurmId(),
               aDefender.getWurmId(),
               this.totalTime,
               this.totalTime
            );
         }

         tile = aDefender.getCurrentTile();
         if (tile != null) {
            tile.sendProjectile(
               aArrow.getWurmId(),
               (byte)1,
               aArrow.getModelName(),
               aArrow.getName(),
               aArrow.getMaterial(),
               aPerformer.getPosX(),
               aPerformer.getPosY(),
               aPerformer.getPositionZ() + aPerformer.getAltOffZ(),
               aPerformer.getStatus().getRotation(),
               (byte)aPerformer.getLayer(),
               (float)((int)aDefender.getPosX()),
               (float)((int)aDefender.getPosY()),
               aDefender.getPositionZ() + aDefender.getAltOffZ(),
               aPerformer.getWurmId(),
               aDefender.getWurmId(),
               this.totalTime,
               this.totalTime
            );
         }
      } else {
         VolaTile tile = aPerformer.getCurrentTile();
         if (tile != null) {
            tile.sendProjectile(
               aArrow.getWurmId(),
               (byte)1,
               aArrow.getModelName(),
               aArrow.getName(),
               aArrow.getMaterial(),
               aPerformer.getPosX(),
               aPerformer.getPosY(),
               aPerformer.getPositionZ() + aPerformer.getAltOffZ(),
               aPerformer.getStatus().getRotation(),
               (byte)aPerformer.getLayer(),
               (float)((aTileArrowDownX << 2) + 2),
               (float)((aTileArrowDownY << 2) + 2),
               aDefender.getPositionZ() + aDefender.getAltOffZ(),
               aPerformer.getWurmId(),
               -2L,
               this.totalTime,
               this.totalTime
            );
         }

         tile = aDefender.getCurrentTile();
         if (tile != null) {
            tile.sendProjectile(
               aArrow.getWurmId(),
               (byte)1,
               aArrow.getModelName(),
               aArrow.getName(),
               aArrow.getMaterial(),
               aPerformer.getPosX(),
               aPerformer.getPosY(),
               aPerformer.getPositionZ() + aPerformer.getAltOffZ(),
               aPerformer.getStatus().getRotation(),
               (byte)aPerformer.getLayer(),
               (float)((aTileArrowDownX << 2) + 2),
               (float)((aTileArrowDownY << 2) + 2),
               aDefender.getPositionZ() + aDefender.getAltOffZ(),
               aPerformer.getWurmId(),
               -2L,
               this.totalTime,
               this.totalTime
            );
         }
      }
   }

   public Arrows(
      Item aArrow,
      Creature aPerformer,
      Item aItem,
      int aTileArrowDownX,
      int aTileArrowDownY,
      Item aBow,
      Arrows.ArrowHitting aArrowHitting,
      Arrows.ArrowDestroy aArrowDestroy,
      String aScoreString
   ) {
      this.hittingCreature = false;
      this.arrow = aArrow;
      this.performer = aPerformer;
      this.item = aItem;
      this.tileArrowDownX = aTileArrowDownX;
      this.tileArrowDownY = aTileArrowDownY;
      this.arrowHitting = aArrowHitting;
      this.arrowDestroy = aArrowDestroy;
      this.bow = aBow;
      this.scoreString = aScoreString;
      this.time = 0.0F;
      aArrow.setPosXYZ(aPerformer.getPosX(), aPerformer.getPosY(), aPerformer.getPositionZ() + aPerformer.getAltOffZ());
      if (aArrowHitting != Arrows.ArrowHitting.HIT
         && aArrowHitting != Arrows.ArrowHitting.HIT_NO_DAMAGE
         && aArrowHitting != Arrows.ArrowHitting.SHIELD
         && aArrowHitting != Arrows.ArrowHitting.EVASION) {
         this.hitTarget = false;
      } else {
         this.hitTarget = true;
      }

      float length = 1.0F;
      if (this.hitTarget) {
         float x = aItem.getPosX() - aPerformer.getPosX();
         float y = aItem.getPosY() - aPerformer.getPosY();
         float h = aItem.getPosZ() + 1.0F - aPerformer.getPositionZ() + aPerformer.getAltOffZ();
         Vector vector = new Vector(x, y, h);
         length = vector.length();
      } else {
         float x = (float)((aTileArrowDownX << 2) + 2) - aPerformer.getPosX();
         float y = (float)((aTileArrowDownY << 2) + 2) - aPerformer.getPosY();
         float h = aItem.getPosZ() - aPerformer.getPositionZ() + aPerformer.getAltOffZ();
         Vector vector = new Vector(x, y, h);
         length = vector.length();
      }

      this.speed = 45.0F;
      this.totalTime = length / this.speed * 1000.0F;
      if (this.hitTarget) {
         VolaTile tile = aPerformer.getCurrentTile();
         if (tile != null) {
            tile.sendProjectile(
               aArrow.getWurmId(),
               (byte)1,
               aArrow.getModelName(),
               aArrow.getName(),
               aArrow.getMaterial(),
               aPerformer.getPosX(),
               aPerformer.getPosY(),
               aPerformer.getPositionZ() + aPerformer.getAltOffZ(),
               aPerformer.getStatus().getRotation(),
               (byte)aPerformer.getLayer(),
               (float)((int)aItem.getPosX()),
               (float)((int)aItem.getPosY()),
               aItem.getPosZ(),
               aPerformer.getWurmId(),
               aItem.getWurmId(),
               this.totalTime,
               this.totalTime
            );
         }
      } else {
         VolaTile tile = aPerformer.getCurrentTile();
         if (tile != null) {
            tile.sendProjectile(
               aArrow.getWurmId(),
               (byte)1,
               aArrow.getModelName(),
               aArrow.getName(),
               aArrow.getMaterial(),
               aPerformer.getPosX(),
               aPerformer.getPosY(),
               aPerformer.getPositionZ() + aPerformer.getAltOffZ(),
               aPerformer.getStatus().getRotation(),
               (byte)aPerformer.getLayer(),
               (float)((aTileArrowDownX << 2) + 2),
               (float)((aTileArrowDownY << 2) + 2),
               aItem.getPosZ(),
               aPerformer.getWurmId(),
               -2L,
               this.totalTime,
               this.totalTime
            );
         }
      }
   }

   public static void pollAll(float elapsedTime) {
      Iterator<Arrows> i = arrows.iterator();

      while(i.hasNext()) {
         Arrows arrow = i.next();
         if (arrow.hittingCreature) {
            if (!arrow.pollHitCreature(elapsedTime)) {
               i.remove();
            }
         } else if (!arrow.pollHitItem(elapsedTime)) {
            i.remove();
         }
      }
   }

   public boolean pollHitCreature(float elapsedTime) {
      this.time += elapsedTime;
      if (this.defender.isDead()) {
         return false;
      } else if (!(this.time > this.totalTime)) {
         return true;
      } else {
         boolean tooLate = this.arrow.getMaterial() == 21
            && !this.defender.isWithinDistanceTo(this.bow.getPosX(), this.bow.getPosY(), this.bow.getPosZ(), 12.0F);
         if (tooLate) {
            Items.destroyItem(this.arrow.getWurmId());
            return false;
         } else {
            if (this.arrowHitting == Arrows.ArrowHitting.HIT) {
               if (this.performer == null) {
                  byte type = 2;
                  if (this.bow.isEnchantedTurret()) {
                     switch(this.bow.getTemplateId()) {
                        case 940:
                           type = 10;
                           break;
                        case 941:
                           type = 4;
                           break;
                        case 942:
                           type = 4;
                           break;
                        case 968:
                           type = 8;
                     }
                  }

                  ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                  segments.add(new MulticolorLineSegment(this.arrow.getNameWithGenus() + " from the ", (byte)7));
                  segments.add(new MulticolorLineSegment(this.bow.getName(), (byte)2));
                  segments.add(new MulticolorLineSegment(" hits you in the " + this.defender.getBody().getWoundLocationString(this.pos) + ".", (byte)7));
                  this.defender.getCommunicator().sendColoredMessageCombat(segments);
                  this.defender.addWoundOfType(this.performer, type, this.pos, false, 1.0F, true, this.damage, 0.0F, 0.0F, false, false);
               } else {
                  Archery.hit(
                     this.defender,
                     this.performer,
                     this.arrow,
                     this.bow,
                     this.damage,
                     this.damMod,
                     this.armourMod,
                     this.pos,
                     true,
                     this.dryRun,
                     this.difficulty,
                     this.bonus
                  );
               }
            } else if (this.arrowHitting == Arrows.ArrowHitting.HIT_NO_DAMAGE) {
               if (this.performer != null) {
                  ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                  segments.add(new MulticolorLineSegment("Your arrow glances off ", (byte)0));
                  segments.add(new CreatureLineSegment(this.defender));
                  segments.add(new MulticolorLineSegment(" and does no damage.", (byte)0));
                  this.performer.getCommunicator().sendColoredMessageCombat(segments);
               }

               this.defender
                  .getCommunicator()
                  .sendCombatSafeMessage("An arrow hits you on the " + this.defender.getBody().getWoundLocationString(this.pos) + " but does no damage.");
            } else if (this.arrowHitting == Arrows.ArrowHitting.SHIELD) {
               if (this.performer != null) {
                  ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                  segments.add(new MulticolorLineSegment("Your arrow glances off ", (byte)0));
                  segments.add(new CreatureLineSegment(this.defender));
                  segments.add(new MulticolorLineSegment("'s shield.", (byte)0));
                  this.performer.getCommunicator().sendColoredMessageCombat(segments);
               }

               this.defender.getCommunicator().sendCombatSafeMessage("You instinctively block an arrow with your shield.");
            } else if (this.arrowHitting == Arrows.ArrowHitting.EVASION) {
               if (this.performer != null) {
                  ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                  segments.add(new MulticolorLineSegment("Your arrow glances off ", (byte)0));
                  segments.add(new CreatureLineSegment(this.defender));
                  segments.add(new MulticolorLineSegment("'s armour.", (byte)0));
                  this.performer.getCommunicator().sendColoredMessageCombat(segments);
               }

               this.defender
                  .getCommunicator()
                  .sendCombatSafeMessage(
                     "An arrow hits you on the " + this.defender.getBody().getWoundLocationString(this.pos) + " but glances off your armour."
                  );
            } else if (this.arrowHitting == Arrows.ArrowHitting.TREE) {
               SoundPlayer.playSound("sound.arrow.stuck.wood", this.tileArrowDownX, this.tileArrowDownY, this.defender.isOnSurface(), 0.0F);
            } else if (this.arrowHitting == Arrows.ArrowHitting.FENCE_STONE) {
               SoundPlayer.playSound("sound.work.masonry", this.tileArrowDownX, this.tileArrowDownY, this.defender.isOnSurface(), 0.0F);
            } else if (this.arrowHitting == Arrows.ArrowHitting.FENCE_TREE) {
               SoundPlayer.playSound("sound.arrow.stuck.wood", this.tileArrowDownX, this.tileArrowDownY, this.defender.isOnSurface(), 0.0F);
            } else if (this.arrowHitting == Arrows.ArrowHitting.GROUND) {
               SoundPlayer.playSound("sound.arrow.stuck.ground", this.tileArrowDownX, this.tileArrowDownY, this.defender.isOnSurface(), 0.0F);
            }

            if (this.arrowDestroy == Arrows.ArrowDestroy.BREAKS) {
               Items.destroyItem(this.arrow.getWurmId());
               if (this.performer != null) {
                  this.performer.getCommunicator().sendCombatNormalMessage("The arrow breaks.");
               }
            } else if (this.arrowDestroy == Arrows.ArrowDestroy.NORMAL) {
               Items.destroyItem(this.arrow.getWurmId());
            } else if (this.arrowDestroy == Arrows.ArrowDestroy.WATER) {
               Items.destroyItem(this.arrow.getWurmId());
               if (this.performer != null) {
                  this.performer.getCommunicator().sendCombatNormalMessage("The arrow disappears from your view.");
               }
            } else if (this.arrowDestroy == Arrows.ArrowDestroy.NOT && this.arrowHitting != Arrows.ArrowHitting.HIT) {
               try {
                  Zone z = Zones.getZone(this.tileArrowDownX, this.tileArrowDownY, this.defender.isOnSurface());
                  VolaTile t = z.getOrCreateTile(this.tileArrowDownX, this.tileArrowDownY);
                  t.addItem(this.arrow, false, false);
               } catch (NoSuchZoneException var5) {
                  logger.log(Level.WARNING, var5.getMessage());
               }
            }

            return false;
         }
      }
   }

   public boolean pollHitItem(float elapsedTime) {
      this.time += elapsedTime;
      if (this.time > this.totalTime) {
         if (this.arrowHitting == Arrows.ArrowHitting.HIT) {
            SoundPlayer.playSound("sound.arrow.hit.wood", this.item, 1.6F);
            this.item
               .setDamage(this.item.getDamage() + Math.max(Server.rand.nextFloat() * 0.4F, (float)(this.damage / 5000.0) * this.item.getDamageModifier()));
            if (this.item.getTemplateId() == 458) {
               this.performer.getCommunicator().sendSafeServerMessage("You score " + this.scoreString);
               Server.getInstance().broadCastAction(this.performer.getName() + " scores " + this.scoreString, this.performer, 5);
            }
         } else if (this.arrowHitting == Arrows.ArrowHitting.NOT) {
            SoundPlayer.playSound("sound.arrow.miss", this.item, 1.6F);
         }

         if (this.arrowDestroy == Arrows.ArrowDestroy.BREAKS) {
            Items.destroyItem(this.arrow.getWurmId());
            this.performer.getCommunicator().sendCombatNormalMessage("The arrow breaks.");
         } else if (this.arrowDestroy == Arrows.ArrowDestroy.NORMAL) {
            Items.destroyItem(this.arrow.getWurmId());
         } else if (this.arrowDestroy == Arrows.ArrowDestroy.WATER) {
            Items.destroyItem(this.arrow.getWurmId());
            this.performer.getCommunicator().sendCombatNormalMessage("The arrow disappears from your view.");
         } else if (this.arrowDestroy == Arrows.ArrowDestroy.NOT) {
            try {
               Zone z = Zones.getZone(this.tileArrowDownX, this.tileArrowDownY, this.performer.isOnSurface());
               VolaTile t = z.getOrCreateTile(this.tileArrowDownX, this.tileArrowDownY);
               t.addItem(this.arrow, false, false);
               if (this.arrowHitting == Arrows.ArrowHitting.NOT) {
                  SoundPlayer.playSound("sound.arrow.stuck.ground", this.tileArrowDownX, this.tileArrowDownY, this.item.isOnSurface(), 0.0F);
               }
            } catch (NoSuchZoneException var4) {
               logger.log(Level.WARNING, var4.getMessage());
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static final void hitCreature(Item projectile, Item source, Creature performer, Creature defender, int damage, Arrows.ArrowDestroy arrowDestroy) {
      try {
         arrows.add(
            new Arrows(
               projectile,
               performer,
               defender,
               -1,
               -1,
               Arrows.ArrowHitting.HIT,
               arrowDestroy,
               source,
               (double)damage,
               1.0F,
               0.0F,
               defender.getBody().getRandomWoundPos(),
               false,
               0.0,
               0.0
            )
         );
      } catch (Exception var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      }
   }

   public static final void shootCreature(Item shooter, Creature defender, int damage) {
      byte proj = 1;
      int template = 830;
      if (!shooter.isEnchantedTurret()) {
         SoundPlayer.playSound("sound.arrow.shot", shooter, (float)shooter.getSizeZ() / 2.0F);
      } else {
         switch(shooter.getTemplateId()) {
            case 940:
               proj = 6;
               break;
            case 941:
               proj = 7;
               break;
            case 942:
               proj = 8;
               break;
            case 968:
               proj = 5;
               break;
            default:
               proj = 1;
         }
      }

      try {
         Item arrow = ItemFactory.createItem(830, 50.0F + shooter.getQualityLevel() / 2.0F, null);
         if (shooter.isEnchantedTurret()) {
            arrow.setName("ball of energy");
         }

         boolean parriedShield = false;
         Item defShield = defender.getShield();
         if (defShield != null && defender.getStatus().getStamina() >= 300) {
            Skill defShieldSkill = null;
            Skills defenderSkills = defender.getSkills();
            double defCheck = 0.0;
            int skillnum = -10;

            try {
               skillnum = defShield.getPrimarySkill();
               defShieldSkill = defenderSkills.getSkill(skillnum);
            } catch (NoSuchSkillException var16) {
               if (skillnum != -10) {
                  defShieldSkill = defenderSkills.learn(skillnum, 1.0F);
               }
            }

            if (defShieldSkill != null) {
               double sdiff = Math.max(
                  defShieldSkill.getKnowledge() - 10.0,
                  (double)Math.max(20.0F, defender.isMoving() ? shooter.getQualityLevel() / 2.0F + 20.0F : shooter.getQualityLevel() / 2.0F)
               );
               sdiff -= (double)((float)(defShield.getSizeY() + defShield.getSizeZ()) / 3.0F);
               defCheck = defShieldSkill.skillCheck(sdiff, defShield, 0.0, false, 1.0F);
            }

            if (defCheck > 0.0) {
               parriedShield = true;
            }

            defender.getStatus().modifyStamina(-300.0F);
         }

         try {
            if (parriedShield) {
               arrows.add(
                  new Arrows(
                     arrow, defender, Arrows.ArrowHitting.SHIELD, Arrows.ArrowDestroy.NORMAL, shooter, 0.0, defender.getBody().getRandomWoundPos(), proj
                  )
               );
            } else {
               arrows.add(
                  new Arrows(
                     arrow,
                     defender,
                     Arrows.ArrowHitting.HIT,
                     Arrows.ArrowDestroy.NORMAL,
                     shooter,
                     (double)damage,
                     defender.getBody().getRandomWoundPos(),
                     proj
                  )
               );
            }
         } catch (Exception var15) {
            logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
         }
      } catch (FailedException var17) {
         logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
      } catch (NoSuchTemplateException var18) {
         logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
      }
   }

   public static final void addToHitCreature(
      Item arrow,
      Creature performer,
      Creature defender,
      float counter,
      Action act,
      int trees,
      Item bow,
      Skill bowskill,
      Skill archery,
      boolean isAttackingPenned,
      int tileArrowDownX,
      int tileArrowDownY,
      int treetilex,
      int treetiley,
      @Nullable Fence fence,
      boolean limitFail
   ) {
      Arrows.ArrowHitting arrowHitting = Arrows.ArrowHitting.NOT;
      Arrows.ArrowDestroy arrowDestroy = Arrows.ArrowDestroy.NOT;
      double damage = 0.0;
      float armourMod = 0.0F;
      byte pos = 0;
      ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
      segments.add(new CreatureLineSegment(performer));
      segments.add(new MulticolorLineSegment(" lets an arrow fly.", (byte)0));
      MessageServer.broadcastColoredAction(segments, performer, 5, true);
      segments.get(1).setText(" let the arrow fly.");
      performer.getCommunicator().sendColoredMessageCombat(segments);
      SoundPlayer.playSound("sound.arrow.shot", performer, 1.6F);
      double diff = Archery.getBaseDifficulty(act.getNumber());
      if (WurmCalendar.getHour() > 19) {
         diff += (double)(WurmCalendar.getHour() - 19);
      } else if (WurmCalendar.getHour() < 6) {
         diff += (double)(6 - WurmCalendar.getHour());
      }

      diff += (double)trees;
      diff += (double)Zones.getCoverHolder();
      double deviation = Archery.getRangeDifficulty(performer, bow.getTemplateId(), defender.getPosX(), defender.getPosY());
      diff += deviation;
      diff -= (double)arrow.getMaterialArrowDifficulty();
      float damMod = arrow.getMaterialArrowDamageModifier();
      diff -= (double)(bow.getRarity() * 5);
      diff -= (double)(arrow.getRarity() * 5);
      float bon = bow.getSpellNimbleness();
      if (bon > 0.0F) {
         diff -= (double)(bon / 10.0F);
      }

      if (deviation > 5.0) {
         damMod *= 0.9F;
      }

      if (deviation > 10.0) {
         damMod *= Servers.localServer.isChallengeServer() ? 0.6F : 0.9F;
      }

      if (deviation > 20.0) {
         damMod *= Servers.localServer.isChallengeServer() ? 0.4F : 0.9F;
      }

      int nums = 1;
      diff -= (double)bow.getMaterialBowDifficulty();
      diff = Math.max(1.0, diff);
      if (arrow.getTemplateId() == 456) {
         diff += 3.0;
      } else if (arrow.getTemplateId() == 454) {
         diff += 5.0;
      }

      try {
         Skill bcontrol = defender.getSkills().getSkill(104);
         diff += bcontrol.getKnowledge(0.0) / 5.0;
      } catch (NoSuchSkillException var49) {
         logger.log(Level.WARNING, defender.getWurmId() + ", " + defender.getName() + " no body control.");
      }

      damage = Archery.getDamage(performer, defender, bow, arrow, archery);
      if (damage < 3000.0 || arrow.getTemplateId() == 454) {
         nums = 0;
      }

      if (defender.getPositionZ() + defender.getAltOffZ() + (float)defender.getCentimetersHigh() / 100.0F < -1.0F) {
         diff += 40.0;
      }

      diff *= 1.0 - defender.getMovementScheme().armourMod.getModifier();
      diff *= 1.0 + performer.getMovementScheme().armourMod.getModifier();
      boolean dryRun = defender.isNoSkillgain()
         || defender.isNoSkillFor(performer)
         || defender.isSentinel()
         || isAttackingPenned
         || defender.getCitizenVillage() != null && defender.getCitizenVillage() == performer.getCitizenVillage();
      if (defender.isPlayer() && (!defender.isPaying() || defender.isNewbie())) {
         dryRun = true;
      }

      if (!defender.isPlayer() && (defender.getHitched() != null || defender.isRidden() || defender.getDominator() != null)) {
         dryRun = Server.rand.nextInt(3) == 0;
      }

      float armourBonusMultiplier = 1.0F + (performer.getArmourLimitingFactor() > 0.0F ? performer.getArmourLimitingFactor() : 0.0F);
      double bonus = bowskill.skillCheck(diff, bow, (double)arrow.getCurrentQualityLevel(), dryRun, (float)nums);
      if (defender.isMoving()) {
         if (defender.isPlayer()) {
            diff += 6.0;
         } else {
            diff += 4.0;
         }
      }

      if (performer.getArmourLimitingFactor() < 0.0F) {
         diff += (double)(Math.abs(performer.getArmourLimitingFactor()) * 20.0F);
      }

      if (act.getNumber() == 126 || act.getNumber() == 127 || act.getNumber() == 131) {
         bonus -= 30.0;
      }

      if (Servers.localServer.HOMESERVER && performer.getKingdomId() != Servers.localServer.KINGDOM) {
         bonus -= 30.0;
      }

      double power = archery.skillCheck(diff, bow, bonus / 5.0 * (double)armourBonusMultiplier, dryRun, (float)nums);
      double defCheck = 0.0;
      boolean parriedShield = false;
      if (power > 0.0) {
         Item defShield = defender.getShield();
         if (defShield != null && defender.getStatus().getStamina() >= 300 && Archery.willParryWithShield(performer, defender)) {
            Skill defShieldSkill = null;
            Skills defenderSkills = defender.getSkills();
            int skillnum = -10;

            try {
               skillnum = defShield.getPrimarySkill();
               defShieldSkill = defenderSkills.getSkill(skillnum);
            } catch (NoSuchSkillException var52) {
               if (skillnum != -10) {
                  defShieldSkill = defenderSkills.learn(skillnum, 1.0F);
               }
            }

            if (defShieldSkill != null) {
               double sdiff = Math.max(20.0, defender.isMoving() ? power + 20.0 : power);
               sdiff -= (double)((float)(defShield.getSizeY() + defShield.getSizeZ()) * (defShield.getCurrentQualityLevel() / 100.0F) / 10.0F);
               defCheck = defShieldSkill.skillCheck(sdiff, defShield, 0.0, dryRun, 1.0F);
            }

            if (defCheck > 0.0) {
               parriedShield = true;
            }

            defender.getStatus().modifyStamina(-300.0F);
         }

         defender.addAttacker(performer);
         power = Math.max(power, 5.0);
         pos = Archery.getWoundPos(defender, act.getNumber());
         pos = (byte)CombatEngine.getRealPosition(pos);
         armourMod = defender.getArmourMod();
         float evasionChance = 0.0F;
         if (defCheck > 0.0) {
            arrowHitting = Arrows.ArrowHitting.SHIELD;
         } else if (armourMod == 1.0F || defender.isVehicle()) {
            try {
               byte bodyPosition = ArmourTemplate.getArmourPosition(pos);
               Item armour = defender.getArmour(bodyPosition);
               armourMod = ArmourTemplate.calculateDR(armour, (byte)2);
               armour.setDamage(
                  armour.getDamage()
                     + (float)(damage * (double)armourMod / 300000.0) * armour.getDamageModifier() * ArmourTemplate.getArmourDamageModFor(armour, (byte)2)
               );
               CombatEngine.checkEnchantDestruction(arrow, armour, defender);
               evasionChance = ArmourTemplate.calculateArrowGlance(armour, arrow);
            } catch (NoArmourException var50) {
               if (!CombatEngine.isEye(pos) || defender.isUnique()) {
                  evasionChance = 1.0F - defender.getArmourMod();
               }
            } catch (NoSpaceException var51) {
               logger.log(Level.WARNING, defender.getName() + " no armour space on loc " + pos, (Throwable)var51);
            }

            if (defender.getBonusForSpellEffect((byte)22) > 0.0F) {
               if (armourMod >= 1.0F) {
                  armourMod = 0.2F + (1.0F - defender.getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F;
               } else {
                  armourMod = Math.min(armourMod, 0.2F + (1.0F - defender.getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F);
               }
            }
         }

         if (!defender.isPlayer() && !performer.isInvulnerable()) {
            defender.setTarget(performer.getWurmId(), false);
         }

         if (defender.isUnique()) {
            evasionChance = 0.5F;
            damage *= (double)armourMod;
         }

         boolean dropattile = false;
         if (defCheck > 0.0) {
            dropattile = true;
         } else if (Server.rand.nextFloat() < evasionChance) {
            dropattile = true;
            arrowHitting = Arrows.ArrowHitting.EVASION;
         } else if (damage > 500.0) {
            arrowHitting = Arrows.ArrowHitting.HIT;
         } else {
            dropattile = true;
            arrowHitting = Arrows.ArrowHitting.HIT_NO_DAMAGE;
         }

         if (dropattile) {
            if (defCheck > 0.0 && parriedShield) {
               tileArrowDownX = defender.getCurrentTile().tilex;
               tileArrowDownY = defender.getCurrentTile().tiley;
               float damageMod = 1.0E-5F;
               if (defender.isPlayer()) {
                  defShield.setDamage(defShield.getDamage() + Math.max(0.01F, 1.0E-5F * (float)damage * defShield.getDamageModifier()));
               }

               if (defShield.isWood()) {
                  SoundPlayer.playSound("sound.arrow.hit.wood", defender, 1.6F);
               } else if (defShield.isMetal()) {
                  SoundPlayer.playSound("sound.arrow.hit.metal", defender, 1.6F);
               }
            }

            if (Server.rand.nextInt(Math.max(1, (int)arrow.getCurrentQualityLevel())) < 2) {
               arrowDestroy = Arrows.ArrowDestroy.NORMAL;
            } else if (!arrow.setDamage(arrow.getDamage() + 5.0F * damMod)) {
               tileArrowDownX = defender.getCurrentTile().tilex;
               tileArrowDownY = defender.getCurrentTile().tiley;

               try {
                  Zones.getZone(tileArrowDownX, tileArrowDownY, performer.isOnSurface());
               } catch (NoSuchZoneException var48) {
                  arrowDestroy = Arrows.ArrowDestroy.WATER;
               }
            }
         }
      } else if (Server.rand.nextInt(Math.max(1, (int)arrow.getCurrentQualityLevel())) < 2) {
         arrowDestroy = Arrows.ArrowDestroy.BREAKS;
         arrowHitting = Arrows.ArrowHitting.HIT_NO_DAMAGE;
      } else if (arrow.setDamage(arrow.getDamage() + 5.0F * damMod)) {
         arrowDestroy = Arrows.ArrowDestroy.BREAKS;
         arrowHitting = Arrows.ArrowHitting.HIT_NO_DAMAGE;
      } else {
         if (trees > 0 && treetilex > 0) {
            tileArrowDownX = treetilex;
            tileArrowDownY = treetiley;
         }

         boolean hitdef = false;
         if (tileArrowDownX == -1) {
            if (defender.opponent != null
               && performer.getKingdomId() == defender.opponent.getKingdomId()
               && power < -20.0
               && (double)Server.rand.nextInt(100) < Math.abs(power)
               && defender.opponent.isPlayer()
               && defender.opponent != performer) {
               pos = Archery.getWoundPos(defender.opponent, act.getNumber());
               pos = (byte)CombatEngine.getRealPosition(pos);
               armourMod = defender.opponent.getArmourMod();
               damage = Archery.getDamage(performer, defender, bow, arrow, archery);
               if (armourMod == 1.0F || defender.isVehicle()) {
                  try {
                     byte bodyPosition = ArmourTemplate.getArmourPosition(pos);
                     Item armour = defender.opponent.getArmour(bodyPosition);
                     armourMod = ArmourTemplate.calculateDR(armour, (byte)2);
                     armour.setDamage(
                        armour.getDamage()
                           + (float)(damage * (double)armourMod / 300000.0)
                              * armour.getDamageModifier()
                              * ArmourTemplate.getArmourDamageModFor(armour, (byte)2)
                     );
                     CombatEngine.checkEnchantDestruction(arrow, armour, defender.opponent);
                  } catch (NoArmourException var46) {
                  } catch (NoSpaceException var47) {
                     logger.log(Level.WARNING, defender.getName() + " no armour space on loc " + pos);
                  }
               }

               arrowHitting = Arrows.ArrowHitting.HIT;
               hitdef = true;
            }

            if (!hitdef) {
               tileArrowDownX = defender.getCurrentTile().tilex;
               tileArrowDownY = defender.getCurrentTile().tiley;
            }
         }

         if (!hitdef) {
            try {
               Zones.getZone(tileArrowDownX, tileArrowDownY, performer.isOnSurface());
               if (treetilex > 0) {
                  arrowHitting = Arrows.ArrowHitting.TREE;
               } else if (fence != null) {
                  if (fence.isStone()) {
                     arrowHitting = Arrows.ArrowHitting.FENCE_STONE;
                  } else {
                     arrowHitting = Arrows.ArrowHitting.FENCE_TREE;
                  }
               } else {
                  arrowHitting = Arrows.ArrowHitting.GROUND;
               }
            } catch (NoSuchZoneException var45) {
               arrowDestroy = Arrows.ArrowDestroy.WATER;
            }
         }
      }

      arrows.add(
         new Arrows(
            arrow, performer, defender, tileArrowDownX, tileArrowDownY, arrowHitting, arrowDestroy, bow, damage, damMod, armourMod, pos, dryRun, diff, bonus
         )
      );
   }

   public static void addToHitItem(
      Item arrow,
      Creature performer,
      Item target,
      float counter,
      Skill bowskill,
      Item bow,
      int tileArrowDownX,
      int tileArrowDownY,
      double deviation,
      double diff,
      int trees,
      int treetilex,
      int treetiley
   ) {
      Arrows.ArrowHitting arrowHitting = Arrows.ArrowHitting.NOT;
      Arrows.ArrowDestroy arrowDestroy = Arrows.ArrowDestroy.NOT;
      String scoreString = "outside the rings.";
      Server.getInstance().broadCastAction(performer.getName() + " lets an arrow fly.", performer, 5);
      performer.getCommunicator().sendNormalServerMessage("You let the arrow fly.");
      SoundPlayer.playSound("sound.arrow.aim", performer, 1.0F);
      diff -= (double)arrow.getMaterialArrowDifficulty();
      float damMod = arrow.getMaterialArrowDamageModifier();
      if (deviation > 5.0) {
         damMod *= 0.9F;
      }

      if (deviation > 10.0) {
         damMod *= 0.9F;
      }

      if (deviation > 20.0) {
         damMod *= 0.9F;
      }

      diff -= (double)bow.getMaterialBowDifficulty();
      diff = Math.max(1.0, diff);
      if (arrow.getTemplateId() == 456) {
         diff += 3.0;
      } else if (arrow.getTemplateId() == 454) {
         diff += 5.0;
      }

      boolean dryrun = false;
      if (bowskill.getRealKnowledge() >= 30.0) {
         if (Server.rand.nextInt(10) == 0) {
            performer.getCommunicator().sendNormalServerMessage("You don't learn anything from this type of shooting any more.");
         }

         dryrun = true;
      }

      if (target.getTemplateId() != 458 && !target.isBoat()) {
         dryrun = true;
      }

      Skill archery = null;

      try {
         archery = performer.getSkills().getSkill(1030);
      } catch (NoSuchSkillException var31) {
         archery = performer.getSkills().learn(1030, 1.0F);
      }

      if (archery.getKnowledge() >= 40.0) {
         if (!dryrun && Server.rand.nextInt(10) == 0) {
            performer.getCommunicator().sendNormalServerMessage("You don't learn anything from this type of shooting any more.");
         }

         dryrun = true;
      }

      double bonus = bowskill.skillCheck(diff, bow, (double)arrow.getCurrentQualityLevel(), dryrun, counter);
      double power = archery.skillCheck(diff, bow, bonus, dryrun, counter);
      if (target.isBoat() && target.getDamage() > 50.0F) {
         power = 0.0;
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is too hard to hit because of existing damage.");
      }

      if (power > 0.0) {
         power = Math.max(power, 5.0);
         double damage = (double)((float)bow.getDamagePercent() * bow.getCurrentQualityLevel() / 100.0F);
         damage *= 1.0 + (performer.getStrengthSkill() - 20.0) / 100.0;
         damage *= (double)(1.0F + arrow.getCurrentQualityLevel() / 100.0F);
         if (arrow.getTemplateId() == 456) {
            damage *= 1.2F;
         } else if (arrow.getTemplateId() == 454) {
            damage *= 0.3F;
         }

         target.setDamage((float)((double)target.getDamage() + damage / 1000000.0));
         boolean dropattile = false;
         if (target.getTemplateId() != 458) {
            performer.getCommunicator().sendSafeServerMessage("Your arrow hits the " + target.getName() + ".");
            Server.getInstance().broadCastAction("An arrow hits " + target.getNameWithGenus() + ".", performer, 5);
         } else {
            scoreString = "outside the rings.";
            int points = (int)(power / 10.0);
            if (points == 10) {
               scoreString = "a perfect ten!";
            } else if (points == 9) {
               scoreString = "a fine 9!";
            } else if (points == 8) {
               scoreString = "a skilled 8.";
            } else if (points > 2) {
               scoreString = "a " + points + ".";
            } else {
               scoreString = "a measly " + points + ".";
            }
         }

         arrowHitting = Arrows.ArrowHitting.HIT;
         if (Server.rand.nextInt(Math.max(1, (int)arrow.getCurrentQualityLevel())) < 2) {
            arrowDestroy = Arrows.ArrowDestroy.BREAKS;
         } else if (!arrow.setDamage(arrow.getDamage() + 5.0F * damMod)) {
            if (target.isHollow()) {
               target.insertItem(arrow, true);
            } else {
               dropattile = true;
            }
         }

         if (dropattile) {
            try {
               tileArrowDownX = target.getTileX();
               tileArrowDownY = target.getTileY();
               Zones.getZone(tileArrowDownX, tileArrowDownY, performer.isOnSurface());
            } catch (NoSuchZoneException var30) {
               arrowDestroy = Arrows.ArrowDestroy.WATER;
            }
         }
      } else {
         arrowHitting = Arrows.ArrowHitting.NOT;
         if (Server.rand.nextInt(Math.max(1, (int)arrow.getCurrentQualityLevel())) < 2) {
            arrowDestroy = Arrows.ArrowDestroy.BREAKS;
         } else if (arrow.setDamage(arrow.getDamage() + 5.0F * damMod)) {
            arrowDestroy = Arrows.ArrowDestroy.BREAKS;
         } else {
            if (trees > 0) {
               if (treetilex > 0) {
                  tileArrowDownX = treetilex;
                  tileArrowDownY = treetiley;
               }
            } else {
               tileArrowDownX = target.getTileX();
               tileArrowDownY = target.getTileY();
            }

            try {
               Zones.getZone(tileArrowDownX, tileArrowDownY, performer.isOnSurface());
            } catch (NoSuchZoneException var29) {
               arrowDestroy = Arrows.ArrowDestroy.WATER;
            }
         }
      }

      arrows.add(new Arrows(arrow, performer, target, tileArrowDownX, tileArrowDownY, bow, arrowHitting, arrowDestroy, scoreString));
   }

   public static enum ArrowDestroy {
      NOT,
      NORMAL,
      WATER,
      BREAKS,
      DO_NOTHING;
   }

   public static enum ArrowHitting {
      NOT,
      SHIELD,
      EVASION,
      HIT,
      HIT_NO_DAMAGE,
      TREE,
      FENCE_STONE,
      FENCE_TREE,
      GROUND;
   }
}
