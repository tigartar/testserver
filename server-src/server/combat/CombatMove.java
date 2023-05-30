package com.wurmonline.server.combat;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.CreatureBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.SoundNames;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CombatMove implements CombatConstants, MiscConstants, SoundNames {
   private static final Logger logger = Logger.getLogger(CombatMove.class.getName());
   private final String name;
   private final String actionString;
   private final int number;
   public static final int SWEEP = 1;
   public static final int EARTHSHAKE = 2;
   public static final int FIREBREATH = 3;
   public static final int DOUBLE_FIST = 4;
   public static final int STOMP = 5;
   public static final int THROW = 6;
   public static final int STUN = 7;
   public static final int BASH = 8;
   public static final int ACIDBREATH = 9;
   public static final int HELLHORSEFIRE = 10;
   public static final int PHASE = 11;
   private final float difficulty;
   private final float basedam;
   private final float rarity;
   private final byte woundType;
   private static final Map<Integer, CombatMove> moves = new HashMap<>();

   public static CombatMove getCombatMove(int number) {
      return moves.get(number);
   }

   private CombatMove(int _number, String _name, float _difficulty, String aActionString, float aBaseDamage, float aRarity, byte aWoundType) {
      this.number = _number;
      this.name = _name;
      this.difficulty = _difficulty;
      this.actionString = aActionString;
      this.basedam = aBaseDamage;
      this.rarity = aRarity;
      this.woundType = aWoundType;
      moves.put(this.number, this);
   }

   public void perform(Creature creature) {
      if (!creature.isUnique() || creature.getHugeMoveCounter() >= 2) {
         creature.playAnimation("fight_" + this.getName(), false);
      }

      switch(this.number) {
         case 1:
            this.sweep(creature);
            break;
         case 2:
            this.shakeEarth(creature);
            break;
         case 3:
         case 10:
            this.breatheFire(creature);
            break;
         case 4:
            this.doubleFist(creature);
            break;
         case 5:
            this.stomp(creature);
            break;
         case 6:
            this.throwOpponent(creature);
            break;
         case 7:
            this.stun(creature);
            break;
         case 8:
            this.bashOpponent(creature);
            break;
         case 9:
            this.breathAcid(creature);
            break;
         case 11:
            this.phaseOpponent(creature);
            break;
         default:
            logger.warning("Perform an unknown CombatMove: " + this.number);
      }
   }

   private final void sweep(Creature creature) {
      float damage = this.basedam + Server.rand.nextFloat() * this.basedam;
      int x = creature.getCurrentTile().tilex;
      int y = creature.getCurrentTile().tiley;
      Server.getInstance().broadCastAction(creature.getNameWithGenus() + this.actionString, creature, 5, true);

      for(int a = Math.max(0, x - 1); a < Math.min(Zones.worldTileSizeX - 1, x + 1); ++a) {
         for(int b = Math.max(0, y - 1); b < Math.min(Zones.worldTileSizeY - 1, y + 1); ++b) {
            VolaTile tile = Zones.getTileOrNull(a, b, creature.isOnSurface());
            if (tile != null) {
               Creature[] crets = tile.getCreatures();
               if (crets.length > 0) {
                  for(int l = 0; l < crets.length; ++l) {
                     if (crets[l] != creature && !crets[l].isUnique() && (crets[l].isPlayer() && crets[l].getPower() <= 0 || crets[l].isDominated())) {
                        crets[l].addWoundOfType(creature, this.woundType, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false);
                     }
                  }
               }
            }
         }
      }
   }

   private final void stun(Creature creature) {
      float damage = this.basedam + Server.rand.nextFloat() * this.basedam;
      int x = creature.getCurrentTile().tilex;
      int y = creature.getCurrentTile().tiley;

      for(int a = Math.max(0, x - 1); a < Math.min(Zones.worldTileSizeX - 1, x + 1); ++a) {
         for(int b = Math.max(0, y - 1); b < Math.min(Zones.worldTileSizeY - 1, y + 1); ++b) {
            VolaTile tile = Zones.getTileOrNull(a, b, creature.isOnSurface());
            if (tile != null) {
               Creature[] crets = tile.getCreatures();
               if (crets.length > 0) {
                  for(int l = 0; l < crets.length; ++l) {
                     if (crets[l] != creature && !crets[l].isUnique() && (crets[l].isPlayer() && crets[l].getPower() <= 0 || crets[l].isDominated())) {
                        Server.getInstance()
                           .broadCastAction(
                              creature.getNameWithGenus() + replace(this.actionString, "@defender", crets[l].getNameWithGenus()), creature, 5, true
                           );
                        crets[l].getStatus().setStunned((float)(Server.rand.nextInt(5) + 4));
                        crets[l].addWoundOfType(creature, this.woundType, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false);
                     }
                  }
               }
            }
         }
      }
   }

   private final void stomp(Creature creature) {
      float damage = this.basedam + Server.rand.nextFloat() * this.basedam;
      int x = creature.getCurrentTile().tilex;
      int y = creature.getCurrentTile().tiley;
      Server.getInstance().broadCastAction(creature.getNameWithGenus() + this.actionString, creature, 5, true);

      for(int a = Math.max(0, x - 2); a <= Math.min(Zones.worldTileSizeX - 1, x + 2); ++a) {
         for(int b = Math.max(0, y - 2); b <= Math.min(Zones.worldTileSizeY - 1, y + 2); ++b) {
            VolaTile tile = Zones.getTileOrNull(a, b, creature.isOnSurface());
            if (tile != null) {
               Creature[] crets = tile.getCreatures();
               if (crets.length > 0) {
                  for(int l = 0; l < crets.length; ++l) {
                     if (crets[l] != creature && !crets[l].isUnique() && (crets[l].isPlayer() && crets[l].getPower() <= 0 || crets[l].isDominated())) {
                        crets[l].addWoundOfType(creature, this.woundType, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false);
                     }
                  }
               }
            }
         }
      }
   }

   private final void shakeEarth(Creature creature) {
      float damage = this.basedam + Server.rand.nextFloat() * this.basedam;
      int x = creature.getCurrentTile().tilex;
      int y = creature.getCurrentTile().tiley;
      Server.getInstance().broadCastAction(creature.getNameWithGenus() + this.actionString, creature, 5, true);

      for(int a = Math.max(0, x - 2); a < Math.min(Zones.worldTileSizeX - 1, x + 2); ++a) {
         for(int b = Math.max(0, y - 2); b < Math.min(Zones.worldTileSizeY - 1, y + 2); ++b) {
            VolaTile tile = Zones.getTileOrNull(a, b, creature.isOnSurface());
            if (tile != null) {
               Creature[] crets = tile.getCreatures();
               if (crets.length > 0) {
                  for(int l = 0; l < crets.length; ++l) {
                     if (crets[l] != creature && !crets[l].isUnique() && (crets[l].isPlayer() && crets[l].getPower() <= 0 || crets[l].isDominated())) {
                        crets[l].addWoundOfType(creature, this.woundType, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false);
                     }
                  }
               }
            }
         }
      }
   }

   private final void doubleFist(Creature creature) {
      float damage = this.basedam + Server.rand.nextFloat() * this.basedam;
      int x1 = creature.getCurrentTile().tilex;
      int y1 = creature.getCurrentTile().tiley;
      int x2 = creature.getCurrentTile().tilex;
      int y2 = creature.getCurrentTile().tiley;
      Server.getInstance()
         .broadCastAction(creature.getNameWithGenus() + replace(this.actionString, "@hisher", creature.getHisHerItsString()), creature, 5, true);
      float attAngle = Creature.normalizeAngle(creature.getStatus().getRotation());
      byte dir = 0;
      float degree = 22.5F;
      if (!((double)attAngle >= 337.5) && !(attAngle < 22.5F)) {
         for(int x = 0; x < 8; ++x) {
            if (attAngle < 22.5F + (float)(45 * x)) {
               dir = (byte)x;
               break;
            }
         }
      } else {
         dir = 0;
      }

      if (dir == 0) {
         --x1;
         --y1;
         ++x2;
         --y2;
      } else if (dir == 7) {
         --y1;
         --x2;
      } else if (dir == 6) {
         --x1;
         --y1;
         --x2;
         ++y2;
      } else if (dir == 5) {
         --x1;
         ++y2;
      } else if (dir == 4) {
         --x1;
         ++y1;
         ++x2;
         ++y2;
      } else if (dir == 3) {
         ++y1;
         ++x2;
      } else if (dir == 2) {
         ++x1;
         --y1;
         ++x2;
         ++y2;
      } else if (dir == 1) {
         --y1;
         ++x2;
      }

      VolaTile tile = Zones.getTileOrNull(x1, y1, creature.isOnSurface());

      try {
         ItemFactory.createItem(
            344, 20.0F, (float)((x1 << 2) + 2), (float)((y1 << 2) + 2), attAngle, creature.isOnSurface(), (byte)0, creature.getBridgeId(), creature.getName()
         );
      } catch (Exception var14) {
         logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
      }

      if (tile != null) {
         Creature[] crets = tile.getCreatures();
         if (crets.length > 0) {
            for(int l = 0; l < crets.length; ++l) {
               if (crets[l] != creature && !crets[l].isUnique() && (crets[l].isPlayer() && crets[l].getPower() <= 0 || crets[l].isDominated())) {
                  crets[l].addWoundOfType(creature, this.woundType, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false);
               }
            }
         }
      }

      tile = Zones.getTileOrNull(x2, y2, creature.isOnSurface());

      try {
         ItemFactory.createItem(
            344, 20.0F, (float)((x2 << 2) + 2), (float)((y2 << 2) + 2), attAngle, creature.isOnSurface(), (byte)0, creature.getBridgeId(), creature.getName()
         );
      } catch (Exception var13) {
         logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
      }

      if (tile != null) {
         Creature[] crets = tile.getCreatures();
         if (crets.length > 0) {
            for(int l = 0; l < crets.length; ++l) {
               if (crets[l] != creature && !crets[l].isUnique() && (crets[l].isPlayer() && crets[l].getPower() <= 0 || crets[l].isDominated())) {
                  crets[l].addWoundOfType(creature, this.woundType, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false);
               }
            }
         }
      }
   }

   public void playDamageSound(Creature defender, int defdamage) {
      int numsound = Server.rand.nextInt(3);
      if (defdamage > 10000) {
         if (numsound == 0) {
            SoundPlayer.playSound("sound.combat.fleshbone1", defender, 1.6F);
         }

         if (numsound == 1) {
            SoundPlayer.playSound("sound.combat.fleshbone2", defender, 1.6F);
         }

         if (numsound == 2) {
            SoundPlayer.playSound("sound.combat.fleshbone3", defender, 1.6F);
         }
      } else {
         if (numsound == 0) {
            SoundPlayer.playSound("sound.combat.fleshhit1", defender, 1.6F);
         }

         if (numsound == 1) {
            SoundPlayer.playSound("sound.combat.fleshhit2", defender, 1.6F);
         }

         if (numsound == 2) {
            SoundPlayer.playSound("sound.combat.fleshhit3", defender, 1.6F);
         }
      }

      SoundPlayer.playSound(defender.getHitSound(), defender, 1.6F);
   }

   private final void breathAcid(Creature creature) {
      long[] ids = creature.getLatestAttackers();
      if (ids.length > 0) {
         long targetcret = ids[Server.rand.nextInt(ids.length)];

         try {
            Creature c = Server.getInstance().getCreature(targetcret);
            if (!c.isDead()) {
               creature.turnTowardsCreature(c);
            }
         } catch (NoSuchCreatureException var12) {
         } catch (NoSuchPlayerException var13) {
         }
      }

      int x1 = creature.getCurrentTile().tilex;
      int y1 = creature.getCurrentTile().tiley;
      byte[] tilearr = null;
      Server.getInstance()
         .broadCastAction(creature.getNameWithGenus() + replace(this.actionString, "@hisher", creature.getHisHerItsString()), creature, 5, true);
      float attAngle = Creature.normalizeAngle(creature.getStatus().getRotation());
      byte dir = 0;
      float degree = 22.5F;
      if (!((double)attAngle >= 337.5) && !(attAngle < 22.5F)) {
         for(int x = 0; x < 8; ++x) {
            if (attAngle < 22.5F + (float)(45 * x)) {
               dir = (byte)x;
               break;
            }
         }
      } else {
         dir = 0;
      }

      if (dir == 0) {
         x1 -= 2;
         y1 -= 5;
         tilearr = BreathWeapon.fiveNorth;
      } else if (dir == 7) {
         x1 -= 5;
         y1 -= 5;
         tilearr = BreathWeapon.fiveNWest;
      } else if (dir == 6) {
         x1 -= 5;
         y1 -= 2;
         tilearr = BreathWeapon.fiveWest;
      } else if (dir == 5) {
         x1 -= 5;
         ++y1;
         tilearr = BreathWeapon.fiveSWest;
      }

      if (dir == 4) {
         x1 -= 2;
         ++y1;
         tilearr = BreathWeapon.fiveSouth;
      } else if (dir == 3) {
         ++x1;
         ++y1;
         tilearr = BreathWeapon.fiveSEast;
      } else if (dir == 2) {
         ++x1;
         y1 -= 2;
         tilearr = BreathWeapon.fiveEast;
      } else if (dir == 1) {
         ++x1;
         y1 -= 5;
         tilearr = BreathWeapon.fiveNEast;
      }

      if (tilearr != null) {
         int num = 0;

         for(int y = 0; y < 5; ++y) {
            for(int x = 0; x < 5; ++x) {
               if (tilearr[num] > 0) {
                  this.breathAcid(Zones.getOrCreateTile(x1 + x, y1 + y, creature.isOnSurface()), creature);
               }

               ++num;
            }
         }
      } else {
         logger.log(Level.WARNING, "Facing " + creature.getStatus().getRotation() + " no tilarr");
      }
   }

   private void breathAcid(VolaTile t, Creature creature) {
      float damage = this.basedam + Server.rand.nextFloat() * this.basedam;
      if (t != null) {
         Creature[] crets = t.getCreatures();
         if (crets.length > 0) {
            for(int l = 0; l < crets.length; ++l) {
               if (crets[l] != creature && !crets[l].isUnique() && (crets[l].isPlayer() || crets[l].isDominated())) {
                  crets[l].addWoundOfType(creature, (byte)10, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false);
               }
            }
         }
      }
   }

   private final void breatheFire(Creature creature) {
      long[] ids = creature.getLatestAttackers();
      if (ids.length > 0) {
         long targetcret = ids[Server.rand.nextInt(ids.length)];

         try {
            Creature c = Server.getInstance().getCreature(targetcret);
            if (!c.isDead()) {
               creature.turnTowardsCreature(c);
            }
         } catch (NoSuchCreatureException var12) {
         } catch (NoSuchPlayerException var13) {
         }
      }

      int x1 = creature.getCurrentTile().tilex;
      int y1 = creature.getCurrentTile().tiley;
      byte[] tilearr = null;
      Server.getInstance()
         .broadCastAction(creature.getNameWithGenus() + replace(this.actionString, "@hisher", creature.getHisHerItsString()), creature, 5, true);
      float attAngle = Creature.normalizeAngle(creature.getStatus().getRotation());
      byte dir = 0;
      float degree = 22.5F;
      if (!((double)attAngle >= 337.5) && !(attAngle < 22.5F)) {
         for(int x = 0; x < 8; ++x) {
            if (attAngle < 22.5F + (float)(45 * x)) {
               dir = (byte)x;
               break;
            }
         }
      } else {
         dir = 0;
      }

      if (dir == 0) {
         x1 -= 2;
         y1 -= 5;
         tilearr = BreathWeapon.fiveNorth;
      } else if (dir == 7) {
         x1 -= 5;
         y1 -= 5;
         tilearr = BreathWeapon.fiveNWest;
      } else if (dir == 6) {
         x1 -= 5;
         y1 -= 2;
         tilearr = BreathWeapon.fiveWest;
      } else if (dir == 5) {
         x1 -= 5;
         ++y1;
         tilearr = BreathWeapon.fiveSWest;
      }

      if (dir == 4) {
         x1 -= 2;
         ++y1;
         tilearr = BreathWeapon.fiveSouth;
      } else if (dir == 3) {
         ++x1;
         ++y1;
         tilearr = BreathWeapon.fiveSEast;
      } else if (dir == 2) {
         ++x1;
         y1 -= 2;
         tilearr = BreathWeapon.fiveEast;
      } else if (dir == 1) {
         ++x1;
         y1 -= 5;
         tilearr = BreathWeapon.fiveNEast;
      }

      if (tilearr != null) {
         int num = 0;

         for(int y = 0; y < 5; ++y) {
            for(int x = 0; x < 5; ++x) {
               if (tilearr[num] > 0) {
                  this.breatheFire(Zones.getOrCreateTile(x1 + x, y1 + y, creature.isOnSurface()), creature);
               }

               ++num;
            }
         }
      } else {
         logger.log(Level.WARNING, "Facing " + creature.getStatus().getRotation() + " no tilarr");
      }
   }

   private void breatheFire(VolaTile t, Creature creature) {
      float damage = this.basedam + Server.rand.nextFloat() * this.basedam;

      try {
         ItemFactory.createItem(
            520,
            20.0F,
            (float)((t.getTileX() << 2) + 2),
            (float)((t.getTileY() << 2) + 2),
            Server.rand.nextFloat() * 180.0F,
            t.isOnSurface(),
            (byte)0,
            creature.getBridgeId(),
            ""
         );
      } catch (Exception var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      }

      if (t != null) {
         Creature[] crets = t.getCreatures();
         if (crets.length > 0) {
            for(int l = 0; l < crets.length; ++l) {
               if (crets[l] != creature && !crets[l].isUnique() && (crets[l].isPlayer() && crets[l].getPower() <= 0 || crets[l].isDominated())) {
                  byte woundType = getBreathDamageType(creature);
                  crets[l].addWoundOfType(creature, (byte)4, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false);
               }
            }
         }
      }
   }

   private static final byte getBreathDamageType(Creature attacker) {
      byte type = 4;
      int cid = attacker.getTemplate().getTemplateId();
      if (CreatureTemplate.isDragon(cid)) {
         switch(cid) {
            case 16:
               type = 4;
               break;
            case 89:
               type = 10;
               break;
            case 90:
               type = 5;
               break;
            case 91:
               type = 6;
               break;
            case 92:
               type = 8;
               break;
            default:
               type = 4;
         }
      }

      return type;
   }

   private final void throwOpponent(Creature creature) {
      if (creature.opponent != null && (creature.opponent.getPower() <= 0 || Servers.isThisATestServer() || creature.opponent.isDominated())) {
         if (creature.opponent.isUnique()) {
            return;
         }

         if (!creature.isOnSurface()) {
            return;
         }

         BlockingResult result = Blocking.getBlockerBetween(creature, creature.opponent, 4);
         if (result != null) {
            return;
         }

         int x = creature.getCurrentTile().tilex;
         int y = creature.getCurrentTile().tiley;
         int targetX = Zones.safeTileX(x + 10 - Server.rand.nextInt(20));
         int targetY = Zones.safeTileY(y + 10 - Server.rand.nextInt(20));
         VolaTile t = Zones.getTileOrNull(targetX, targetY, true);
         if (t != null) {
            if (t.getStructure() != null) {
               return;
            }

            if (t.getVillage() != null) {
               return;
            }
         }

         if (creature.opponent.getVehicle() != -10L) {
            try {
               Item vehicle = Items.getItem(creature.opponent.getVehicle());
               if (vehicle.isBoat() && vehicle.getSizeY() > 130 && creature.getPositionZ() <= 0.0F) {
                  return;
               }
            } catch (NoSuchItemException var10) {
            }
         }

         Creature opp = creature.opponent;
         float damage = this.basedam + Server.rand.nextFloat() * this.basedam;
         Server.getInstance()
            .broadCastAction(creature.getNameWithGenus() + replace(this.actionString, "@defender", creature.opponent.getNameWithGenus()), creature, 5, true);
         if (!opp.addWoundOfType(creature, this.woundType, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false)) {
            Creatures.getInstance().setCreatureDead(opp);
            Players.getInstance().setCreatureDead(opp);
            opp.setTeleportPoints((short)targetX, (short)targetY, opp.getLayer(), 0);
            opp.startTeleporting();
            opp.getCommunicator().sendAlertServerMessage("OUCH! " + creature.getNameWithGenus() + " throws you!");
            opp.getCommunicator().sendTeleport(false);
            if (!opp.isPlayer()) {
               opp.getMovementScheme().resumeSpeedModifier();
            }

            opp.achievement(50);
         }
      }
   }

   private final void bashOpponent(Creature creature) {
      if (creature.opponent != null && (creature.opponent.getPower() <= 0 || creature.opponent.isDominated())) {
         if (creature.opponent.isUnique()) {
            return;
         }

         Creature opp = creature.opponent;
         BlockingResult result = Blocking.getBlockerBetween(creature, opp, 4);
         if (result != null) {
            return;
         }

         float damage = this.basedam + Server.rand.nextFloat() * this.basedam;
         Server.getInstance()
            .broadCastAction(creature.getNameWithGenus() + replace(this.actionString, "@defender", creature.opponent.getNameWithGenus()), creature, 5, true);

         try {
            opp.getCommunicator().sendAlertServerMessage(creature.getNameWithGenus() + " bashes you!");
            opp.getStatus().setStunned((float)(Server.rand.nextInt(5) + 4));
            creature.opponent.addWoundOfType(creature, this.woundType, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false);
         } catch (Exception var6) {
            logger.log(Level.WARNING, opp.getName() + ": " + var6, (Throwable)var6);
         }
      }
   }

   private final void phaseOpponent(Creature creature) {
      if (creature.opponent != null && (creature.opponent.getPower() <= 0 || creature.opponent.isDominated())) {
         if (creature.opponent.isUnique()) {
            return;
         }

         Creature opp = creature.opponent;
         BlockingResult result = Blocking.getBlockerBetween(creature, opp, 4);
         if (result != null) {
            return;
         }

         float damage = this.basedam + Server.rand.nextFloat() * this.basedam;
         Server.getInstance()
            .broadCastAction(creature.getNameWithGenus() + replace(this.actionString, "@defender", creature.opponent.getNameWithGenus()), creature, 5, true);

         try {
            opp.getCommunicator().sendAlertServerMessage(creature.getNameWithGenus() + " confuses you!");
            opp.getStatus().setStunned((float)((byte)Server.rand.nextInt(5) + 4));
            creature.opponent.addWoundOfType(creature, this.woundType, 1, true, 1.0F, true, (double)damage, 0.0F, 0.0F, false, false);
         } catch (Exception var8) {
            logger.log(Level.WARNING, opp.getName() + ": " + var8, (Throwable)var8);
         }

         if (creature.getLayer() >= 0) {
            creature.setTarget(-10L, true);
            creature.setOpponent(null);
            opp.setOpponent(null);
            opp.setTarget(-10L, true);
            float newPosX = creature.getPosX() - 20.0F + Server.rand.nextFloat() * 41.0F;
            float newPosY = creature.getPosY() - 20.0F + Server.rand.nextFloat() * 41.0F;
            float newPosZ = Zones.calculatePosZ(
               newPosX, newPosY, null, creature.isOnSurface(), false, creature.getPositionZ(), creature, creature.getBridgeId()
            );
            CreatureBehaviour.blinkTo(creature, newPosX, newPosY, creature.getLayer(), newPosZ, creature.getBridgeId(), creature.getFloorLevel());
         }
      }
   }

   private static String replace(String target, String from, String to) {
      int start = target.indexOf(from);
      if (start == -1) {
         return target;
      } else {
         int lf = from.length();
         char[] targetChars = target.toCharArray();
         StringBuilder buffer = new StringBuilder();

         int copyFrom;
         for(copyFrom = 0; start != -1; start = target.indexOf(from, copyFrom)) {
            buffer.append(targetChars, copyFrom, start - copyFrom);
            buffer.append(to);
            copyFrom = start + lf;
         }

         buffer.append(targetChars, copyFrom, targetChars.length - copyFrom);
         return buffer.toString();
      }
   }

   public float getRarity() {
      return this.rarity;
   }

   public final String getName() {
      return this.name;
   }

   static {
      new CombatMove(1, "sweep", 20.0F, " makes a circular powerful sweep!", 25000.0F, 0.01F, (byte)1);
      new CombatMove(2, "earthshake", 20.0F, " shakes the earth!", 23000.0F, 0.013F, (byte)0);
      new CombatMove(3, "firebreath", 20.0F, " breathes fire!", 27000.0F, 0.011F, (byte)4);
      new CombatMove(4, "double fist", 20.0F, " throws down @hisher powerful fists!", 30000.0F, 0.01F, (byte)0);
      new CombatMove(5, "stomp", 20.0F, " stomps!", 10000.0F, 0.02F, (byte)0);
      new CombatMove(6, "throws", 20.0F, " picks up and throws @defender!", 5000.0F, 0.05F, (byte)0);
      new CombatMove(7, "stuns", 30.0F, " stuns @defender!", 24000.0F, 0.1F, (byte)9);
      new CombatMove(8, "bashes", 10.0F, " bashes @defender!", 25000.0F, 0.1F, (byte)0);
      new CombatMove(9, "acidbreath", 20.0F, " breathes acid!", 20000.0F, 0.011F, (byte)10);
      new CombatMove(10, "firebreath", 20.0F, " breathes fire!", 7000.0F, 0.003F, (byte)4);
      new CombatMove(11, "phase", 20.0F, " phases!", 5000.0F, 0.011F, (byte)9);
   }
}
