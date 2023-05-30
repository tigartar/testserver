package com.wurmonline.server.creatures;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.PlonkData;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.modifiers.FixedDoubleValueModifier;
import com.wurmonline.server.modifiers.ModifierTypes;
import com.wurmonline.server.modifiers.ValueModifiedListener;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.Enchants;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.util.MovementChecker;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class MovementScheme extends MovementChecker implements ModifierTypes, ValueModifiedListener, ProtoConstants, CounterTypes, TimeConstants, Enchants {
   private final Creature creature;
   boolean halted = false;
   private boolean encumbered = false;
   public Item draggedItem;
   private static final Logger logger = Logger.getLogger(MovementScheme.class.getName());
   private Set<DoubleValueModifier> modifiers;
   private float baseModifier = 1.0F;
   private static final DoubleValueModifier dragMod = new FixedDoubleValueModifier(-0.5);
   private static final DoubleValueModifier ramDragMod = new FixedDoubleValueModifier(-0.75);
   private static final DoubleValueModifier combatMod = new FixedDoubleValueModifier(-0.7);
   private static final DoubleValueModifier drunkMod = new FixedDoubleValueModifier(-0.6);
   private static final DoubleValueModifier mooreMod = new FixedDoubleValueModifier(-5.0);
   private static final DoubleValueModifier farwalkerMod = new FixedDoubleValueModifier(0.5);
   private boolean webArmoured = false;
   private boolean hasSpiritSpeed = false;
   private final DoubleValueModifier webArmourMod = new DoubleValueModifier(7, 0.0);
   private boolean justWebSlowArmour = false;
   private static final DoubleValueModifier chargeMod = new FixedDoubleValueModifier(0.1F);
   DoubleValueModifier stealthMod;
   private static final DoubleValueModifier freezeMod = new FixedDoubleValueModifier(-5.0);
   private static final long NOID = -10L;
   public final DoubleValueModifier armourMod = new DoubleValueModifier(0.0);
   private final List<Float> movementSpeeds;
   private final List<Byte> windImpacts;
   private final List<Short> mountSpeeds;
   private final Set<Integer> intraports = new HashSet<>();
   public int samePosCounts = 0;
   private static Vehicle vehic;
   private static Creature cretVehicle;
   public static Item itemVehicle;
   private static Player passenger;
   private int climbSkill = 10;
   Map<Long, Float> oldmoves = new HashMap<>();
   private int changedTileCounter = 0;
   private int errors = 0;
   private boolean hasWetFeet = false;
   private boolean outAtSea = false;
   private boolean m300m = false;
   private boolean m700m = false;
   private boolean m1400m = false;
   private boolean m2180m = false;
   static Creature toRemove = null;

   MovementScheme(Creature _creature) {
      this.creature = _creature;
      this.movementSpeeds = new ArrayList<>();
      this.windImpacts = new ArrayList<>();
      this.mountSpeeds = new ArrayList<>();
      if (!this.creature.isPlayer()) {
         this.onGround = true;
      } else {
         this.halted = true;
      }

      this.setLog(true);
   }

   public void initalizeModifiersWithTemplate() {
      this.addModifier(this.armourMod);
      this.armourMod.addListener(this);
   }

   @Override
   public float getTileSteepness(int tilex, int tiley, int clayer) {
      if (this.creature != null && this.creature.getBridgeId() > 0L) {
         return 0.0F;
      } else {
         float highest = -100.0F;
         float lowest = 32000.0F;

         for(int x = 0; x <= 1; ++x) {
            for(int y = 0; y <= 1; ++y) {
               if (tilex + x < Zones.worldTileSizeX && tiley + y < Zones.worldTileSizeY) {
                  if (clayer >= 0) {
                     float height = Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(tilex + x, tiley + y));
                     if (height > highest) {
                        highest = height;
                     }

                     if (height < lowest) {
                        lowest = height;
                     }
                  } else {
                     float height = Tiles.decodeHeightAsFloat(Server.caveMesh.getTile(tilex + x, tiley + y));
                     if (height > highest) {
                        highest = height;
                     }

                     if (height < lowest) {
                        lowest = height;
                     }
                  }
               }
            }
         }

         return highest - lowest;
      }
   }

   @Override
   protected byte getTextureForTile(int xTile, int yTile, int layer, long bridgeId) {
      if (bridgeId > 0L) {
         VolaTile vt = Zones.getTileOrNull(xTile, yTile, layer == 0);
         if (vt != null) {
            for(BridgePart bp : vt.getBridgeParts()) {
               if (bp.getStructureId() == bridgeId) {
                  if (bp.getMaterial() != BridgeConstants.BridgeMaterial.WOOD && bp.getMaterial() != BridgeConstants.BridgeMaterial.ROPE) {
                     if (bp.getMaterial() == BridgeConstants.BridgeMaterial.BRICK) {
                        return Tiles.Tile.TILE_COBBLESTONE.id;
                     }

                     return Tiles.Tile.TILE_STONE_SLABS.id;
                  }

                  return Tiles.Tile.TILE_PLANKS.id;
               }
            }
         }
      }

      MeshIO mesh = Server.surfaceMesh;
      if (layer < 0) {
         if (xTile < 0 || xTile > Zones.worldTileSizeX || yTile < 0 || yTile > Zones.worldTileSizeY) {
            return Tiles.Tile.TILE_ROCK.id;
         }

         mesh = Server.caveMesh;
      } else {
         if (this.creature.hasOpenedMineDoor(xTile, yTile)) {
            return Tiles.Tile.TILE_HOLE.id;
         }

         if (xTile < 0 || xTile > Zones.worldTileSizeX || yTile < 0 || yTile > Zones.worldTileSizeY) {
            return Tiles.Tile.TILE_DIRT.id;
         }
      }

      return Tiles.decodeType(mesh.getTile(xTile, yTile));
   }

   @Override
   public final float getHeightOfBridge(int xTile, int yTile, int layer) {
      VolaTile vt = Zones.getTileOrNull(xTile, yTile, layer == 0);
      if (vt != null) {
         BridgePart[] var5 = vt.getBridgeParts();
         int var6 = var5.length;
         byte var7 = 0;
         if (var7 < var6) {
            BridgePart bp = var5[var7];
            return (float)bp.getHeight() / 10.0F;
         }
      }

      return -1000.0F;
   }

   public boolean isIntraTeleporting() {
      return !this.intraports.isEmpty();
   }

   public void addIntraTeleport(int teleportNumber) {
      this.intraports.add(teleportNumber);
   }

   public boolean removeIntraTeleport(int teleportNumber) {
      this.intraports.remove(teleportNumber);
      return this.intraports.isEmpty();
   }

   public void clearIntraports() {
      this.intraports.clear();
   }

   @Override
   protected float getCeilingForNode(int xTile, int yTile) {
      return (float)Tiles.decodeData(Server.caveMesh.getTile(xTile, yTile) & 0xFF) / 10.0F;
   }

   public boolean removeWindMod(byte impact) {
      ListIterator<Byte> it = this.windImpacts.listIterator();

      while(it.hasNext()) {
         Byte b = it.next();
         it.remove();
         if (b == impact) {
            if (this.creature.isPlayer()) {
               ((Player)this.creature).sentWind = 0L;
            }

            return true;
         }
      }

      return false;
   }

   public void setWindMod(byte impact) {
      this.setWindImpact((float)impact / 200.0F);
   }

   public boolean addWindImpact(byte impact) {
      if (!this.windImpacts.isEmpty() && this.windImpacts.get(this.windImpacts.size() - 1) == impact) {
         return false;
      } else {
         this.windImpacts.add(impact);
         this.creature.getCommunicator().sendWindImpact(impact);
         return true;
      }
   }

   public void resendMountSpeed() {
      if (this.mountSpeeds.size() > 0) {
         this.creature.getCommunicator().sendMountSpeed(this.mountSpeeds.get(0));
      }
   }

   public boolean removeMountSpeed(short speed) {
      ListIterator<Short> it = this.mountSpeeds.listIterator();

      while(it.hasNext()) {
         Short b = it.next();
         it.remove();
         if (b == speed) {
            if (this.creature.isPlayer()) {
               ((Player)this.creature).sentMountSpeed = 0L;
            }

            return true;
         }
      }

      return false;
   }

   public void setMountSpeed(short newMountSpeed) {
      if (this.commandingBoat) {
         this.setMountSpeed((float)newMountSpeed / 1000.0F);
      } else {
         this.setMountSpeed((float)newMountSpeed / 200.0F);
      }
   }

   public boolean addMountSpeed(short speed) {
      if (!this.mountSpeeds.isEmpty() && this.mountSpeeds.get(this.mountSpeeds.size() - 1) == speed) {
         return false;
      } else {
         this.mountSpeeds.add(speed);
         this.creature.getCommunicator().sendMountSpeed(speed);
         return true;
      }
   }

   boolean removeSpeedMod(float speedmod) {
      ListIterator<Float> it = this.movementSpeeds.listIterator();

      while(it.hasNext()) {
         Float f = it.next();
         it.remove();
         if (f == speedmod) {
            return true;
         }
      }

      return false;
   }

   public void sendSpeedModifier() {
      if (this.addSpeedMod(Math.max(0.0F, this.getSpeedModifier()))) {
         this.creature.getCommunicator().sendSpeedModifier(Math.max(0.0F, this.getSpeedModifier()));
      }
   }

   private boolean addSpeedMod(float speedmod) {
      this.oldmoves.put(new Long(System.currentTimeMillis()), new Float(speedmod));
      if (this.oldmoves.size() > 20) {
         Long[] longs = this.oldmoves.keySet().toArray(new Long[this.oldmoves.size()]);

         for(int x = 0; x < 10; ++x) {
            this.oldmoves.remove(longs[x]);
         }
      }

      if (!this.movementSpeeds.isEmpty() && this.movementSpeeds.get(this.movementSpeeds.size() - 1) == speedmod) {
         return false;
      } else {
         this.movementSpeeds.add(speedmod);
         return true;
      }
   }

   @Override
   public void hitGround(float speed) {
      if (this.creature instanceof Player
         && !this.creature.isDead()
         && this.creature.isOnSurface()
         && this.creature.getVisionArea() != null
         && this.creature.getVisionArea().isInitialized()
         && !this.creature.getCommunicator().isInvulnerable()
         && !this.creature.getCommunicator().stillLoggingIn()
         && this.creature.getFarwalkerSeconds() <= 0
         && (this.creature.getPower() < 1 || this.creature.loggerCreature1 > 0L)
         && speed > 0.5F) {
         if (this.creature.getLayer() >= 0) {
            float distFromBorder = 0.09F;
            float tilex = (float)((int)this.getX() / 4);
            float tiley = (float)((int)this.getY() / 4);
            if (this.getX() / 4.0F - tilex < distFromBorder) {
               VolaTile current = Zones.getTileOrNull((int)tilex, (int)tiley, true);
               if (current != null) {
                  Wall[] walls = current.getWalls();

                  for(Wall w : walls) {
                     if (w.getFloorLevel() == this.creature.getFloorLevel() && (float)w.getTileX() == tilex && !w.isHorizontal()) {
                        return;
                     }
                  }

                  Fence[] fences = current.getFences();

                  for(Fence f : fences) {
                     if ((float)f.getTileX() == tilex && !f.isHorizontal() && f.getFloorLevel() == this.creature.getFloorLevel()) {
                        return;
                     }
                  }
               }
            }

            if (this.getY() / 4.0F - tiley < distFromBorder) {
               VolaTile current = Zones.getTileOrNull((int)tilex, (int)tiley, true);
               if (current != null) {
                  Wall[] walls = current.getWalls();

                  for(Wall w : walls) {
                     if ((float)w.getTileY() == tiley && w.isHorizontal() && w.getFloorLevel() == this.creature.getFloorLevel()) {
                        return;
                     }
                  }

                  Fence[] fences = current.getFences();

                  for(Fence f : fences) {
                     if ((float)f.getTileY() == tiley && f.isHorizontal() && f.getFloorLevel() == this.creature.getFloorLevel()) {
                        return;
                     }
                  }
               }
            }

            if (tilex + 1.0F - this.getX() / 4.0F < distFromBorder) {
               VolaTile current = Zones.getTileOrNull((int)tilex + 1, (int)tiley, true);
               if (current != null) {
                  Wall[] walls = current.getWalls();

                  for(Wall w : walls) {
                     if ((float)w.getTileX() == tilex + 1.0F && !w.isHorizontal() && w.getFloorLevel() == this.creature.getFloorLevel()) {
                        return;
                     }
                  }

                  Fence[] fences = current.getFences();

                  for(Fence f : fences) {
                     if ((float)f.getTileX() == tilex + 1.0F && !f.isHorizontal() && f.getFloorLevel() == this.creature.getFloorLevel()) {
                        return;
                     }
                  }
               }
            }

            if (tiley + 1.0F - this.getY() / 4.0F < distFromBorder) {
               VolaTile current = Zones.getTileOrNull((int)tilex, (int)tiley + 1, true);
               if (current != null) {
                  Wall[] walls = current.getWalls();

                  for(Wall w : walls) {
                     if ((float)w.getTileY() == tiley + 1.0F && w.isHorizontal() && w.getFloorLevel() == this.creature.getFloorLevel()) {
                        return;
                     }
                  }

                  Fence[] fences = current.getFences();

                  for(Fence f : fences) {
                     if ((float)f.getTileY() == tiley + 1.0F && f.isHorizontal() && f.getFloorLevel() == this.creature.getFloorLevel()) {
                        return;
                     }
                  }
               }
            }
         }

         float baseDam = 1.0F + speed;

         try {
            float damMod = 20.0F;
            float dam = baseDam * baseDam * baseDam * 24.0F * 60.0F * 20.0F / 15.0F;
            dam = Math.max(dam, 300.0F);
            this.creature.getCommunicator().sendNormalServerMessage("Ouch! That hurt!");
            this.creature
               .sendToLoggers(
                  "Speed=" + speed + ", baseDam=" + baseDam + " damMod=" + 20.0F + " weightCarried=" + this.creature.getCarriedWeight() + " dam=" + dam
               );
            this.creature.achievement(88);
            if (!PlonkData.FALL_DAMAGE.hasSeenThis(this.creature)) {
               PlonkData.FALL_DAMAGE.trigger(this.creature);
            }

            this.creature.addWoundOfType(null, (byte)0, 1, true, 1.0F, false, (double)dam, 0.0F, 0.0F, false, false);
         } catch (Exception var12) {
         }
      }
   }

   @Override
   protected float getHeightForNode(int xNode, int yNode, int layer) {
      return Zones.getHeightForNode(xNode, yNode, layer);
   }

   @Override
   protected float[] getNodeHeights(int xNode, int yNode, int layer, long bridgeId) {
      return Zones.getNodeHeights(xNode, yNode, layer, bridgeId);
   }

   @Override
   protected boolean handleWrongLayer(int clientInputLayer, int expectedLayer) {
      if (this.creature.getVehicle() != -10L) {
         return false;
      } else {
         if (this.creature.getPower() >= 2
            && Tiles.decodeType(Server.caveMesh.getTile(this.creature.getTileX(), this.creature.getTileY())) != Tiles.Tile.TILE_CAVE_EXIT.id) {
            this.creature
               .getCommunicator()
               .sendAlertServerMessage(
                  "You were detected to be on a different layer from what is shown in your client, setting layer to the one in your client."
               );
         }

         return true;
      }
   }

   @Override
   protected void handlePlayerInRock() {
      if (!this.creature.isDead()) {
         int tilex = this.creature.getTileX();
         int tiley = this.creature.getTileY();
         if (Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)) == Tiles.Tile.TILE_CAVE.id
            || Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
            ((Player)this.creature)
               .intraTeleport(
                  this.creature.getPosX(),
                  this.creature.getPosY(),
                  this.creature.getPositionZ(),
                  this.creature.getStatus().getRotation(),
                  this.creature.getLayer(),
                  "in rock commanding=" + this.creature.isVehicleCommander() + " height=" + this.creature.getPositionZ()
               );
            if (this.creature.isVehicleCommander()) {
               Vehicle creatureVehicle = Vehicles.getVehicleForId(this.creature.getVehicle());
               if (creatureVehicle != null) {
                  if (!creatureVehicle.isCreature()) {
                     try {
                        Item ivehicle = Items.getItem(this.creature.getVehicle());
                        if (!ivehicle.isOnSurface()) {
                           itemVehicle.newLayer = (byte)this.creature.getLayer();

                           try {
                              Zone z1 = Zones.getZone(ivehicle.getTileX(), ivehicle.getTileY(), false);
                              z1.removeItem(ivehicle);
                              ivehicle.setPosXY(this.creature.getPosX(), this.creature.getPosY());
                              Zone z2 = Zones.getZone(ivehicle.getTileX(), ivehicle.getTileY(), false);
                              z2.addItem(ivehicle);
                           } catch (NoSuchZoneException var10) {
                           }

                           itemVehicle.newLayer = -128;
                        }
                     } catch (NoSuchItemException var11) {
                     }
                  } else {
                     try {
                        Creature cvehicle = Creatures.getInstance().getCreature(this.creature.getVehicle());
                        if (!cvehicle.isOnSurface()) {
                           try {
                              Zone z1 = Zones.getZone(cvehicle.getTileX(), cvehicle.getTileY(), false);
                              z1.removeCreature(cvehicle, true, false);
                              cvehicle.setPositionX(this.creature.getPosX());
                              cvehicle.setPositionY(this.creature.getPosY());
                              Zone z2 = Zones.getZone(cvehicle.getTileX(), cvehicle.getTileY(), false);
                              z2.addCreature(cvehicle.getWurmId());
                           } catch (NoSuchZoneException var7) {
                           } catch (NoSuchPlayerException var8) {
                           }
                        }
                     } catch (NoSuchCreatureException var9) {
                     }
                  }
               }
            }
         } else if (this.creature.getVehicle() == -10L) {
            for(int x = -1; x <= 1; ++x) {
               for(int y = -1; y <= 1; ++y) {
                  byte type = Tiles.decodeType(Server.caveMesh.getTile(Zones.safeTileX(x + tilex), Zones.safeTileY(y + tiley)));
                  Tiles.Tile tempTile = Tiles.getTile(type);
                  if (type == Tiles.Tile.TILE_CAVE_EXIT.id) {
                     this.creature.setTeleportPoints((short)Zones.safeTileX(x + tilex), (short)Zones.safeTileY(y + tiley), 0, 0);
                     this.creature.startTeleporting();
                     this.creature.getCommunicator().sendTeleport(false, false, (byte)0);
                     return;
                  }

                  if (!tempTile.isSolidCave() && tempTile != null) {
                     this.creature.setTeleportPoints((short)Zones.safeTileX(x + tilex), (short)Zones.safeTileY(y + tiley), -1, 0);
                     this.creature.startTeleporting();
                     this.creature.getCommunicator().sendTeleport(false, false, (byte)0);
                     return;
                  }
               }
            }

            this.creature.getCommunicator().sendAlertServerMessage("You manage to become stuck in the rock, and quickly suffocate.");
            this.creature.die(false, "Suffocated in Rock (2)");
         }
      }
   }

   @Override
   protected void setLayer(int layer) {
      this.creature.setLayer(layer, false);
   }

   @Override
   protected boolean handleMoveTooFar(float clientInput, float expectedDistance) {
      if (!this.creature.isDead()
         && (this.creature.getPower() < 1 || this.creature.loggerCreature1 != -10L)
         && clientInput - expectedDistance * 1.1F > 0.0F
         && this.changedTileCounter == 0) {
         this.setErrors(this.getErrors() + 1);
         if (this.getErrors() > 4) {
            this.creature.getStatus().setNormalRegen(false);
            logger.log(
               Level.WARNING,
               this.creature.getName()
                  + " TOO FAR, input="
                  + clientInput
                  + ", expected="
                  + expectedDistance
                  + " :  "
                  + this.getCurrx()
                  + "("
                  + this.getXold()
                  + "); "
                  + this.getCurry()
                  + "("
                  + this.getYold()
                  + ") bridge="
                  + this.creature.getBridgeId()
            );
            ((Player)this.creature)
               .intraTeleport(
                  this.creature.getPosX(),
                  this.creature.getPosY(),
                  this.creature.getPositionZ(),
                  this.creature.getStatus().getRotation(),
                  this.creature.getLayer(),
                  "Moved too far"
               );
            this.setAbort(true);
            if (this.getErrors() > 10) {
               Players.getInstance()
                  .sendGlobalGMMessage(
                     this.creature, " movement too far (" + (clientInput - expectedDistance) + ") at " + this.getCurrx() / 4.0F + "," + this.getCurry() / 4.0F
                  );
            }

            return true;
         }
      }

      return false;
   }

   public void setServerClimbing(boolean climb) {
      this.setClimbing(climb);
   }

   @Override
   protected boolean handleMoveTooShort(float clientInput, float expectedDistance) {
      if (!this.creature.isTeleporting()
         && !this.isIntraTeleporting()
         && !this.creature.isDead()
         && (this.creature.getCommunicator().hasReceivedTicks() || !this.isClimbing())
         && this.creature.getPower() < 2
         && this.changedTileCounter == 0
         && this.creature.getCurrentTile() != null) {
      }

      return false;
   }

   @Override
   public final boolean movedOnStair() {
      VolaTile t = this.creature.getCurrentTile();
      if (t != null && (t.hasStair(this.creature.getFloorLevel() + 1) || t.hasStair(this.creature.getFloorLevel()))) {
         this.setBridgeCounter(10);
         this.wasOnStair = true;
      }

      return this.wasOnStair;
   }

   @Override
   protected boolean handleZError(float clientInput, float expectedPosition) {
      if (this.changedTileCounter == 0
         && this.creature.getPower() < 2
         && !this.creature.isTeleporting()
         && !this.creature.isDead()
         && (this.creature.getCommunicator().hasReceivedTicks() || !this.isClimbing())) {
         if (this.getTargetGroundOffset() != this.getGroundOffset()) {
            return false;
         }

         if (this.creature.getVisionArea() != null && this.creature.getVisionArea().isInitialized()) {
            this.setErrors(this.getErrors() + 1);
            if (this.getErrors() > 4) {
               ((Player)this.creature)
                  .intraTeleport(
                     this.creature.getPosX(),
                     this.creature.getPosY(),
                     this.creature.getPositionZ(),
                     this.creature.getStatus().getRotation(),
                     this.creature.getLayer(),
                     "Error in z=" + clientInput + ", expected=" + expectedPosition
                  );
               this.setAbort(true);
               this.setErrors(this.getErrors() + 1);
               if (this.getErrors() > 10) {
                  Players.getInstance()
                     .sendGlobalGMMessage(
                        this.creature,
                        " movement too high (" + (clientInput - expectedPosition) + ") at " + this.getCurrx() / 4.0F + "," + this.getCurry() / 4.0F
                     );
               }

               return true;
            }
         }
      }

      return false;
   }

   public static void movePassengers(Vehicle aVehicle, @Nullable Creature driver, boolean isCreature) {
      if (isCreature) {
         for(int x = 0; x < aVehicle.seats.length; ++x) {
            if (aVehicle.seats[x].type == 1 && aVehicle.seats[x].isOccupied()) {
               try {
                  passenger = Players.getInstance().getPlayer(aVehicle.seats[x].occupant);
                  float r = (-cretVehicle.getStatus().getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                  float s = (float)Math.sin((double)r);
                  float c = (float)Math.cos((double)r);
                  float xo = s * -aVehicle.seats[x].offx - c * -aVehicle.seats[x].offy;
                  float yo = c * -aVehicle.seats[x].offx + s * -aVehicle.seats[x].offy;
                  float newposx = cretVehicle.getPosX() + xo;
                  float newposy = cretVehicle.getPosY() + yo;
                  newposx = Math.max(3.0F, newposx);
                  newposx = Math.min(Zones.worldMeterSizeX - 3.0F, newposx);
                  newposy = Math.max(3.0F, newposy);
                  newposy = Math.min(Zones.worldMeterSizeY - 3.0F, newposy);
                  int diffx = ((int)newposx >> 2) - passenger.getTileX();
                  int diffy = ((int)newposy >> 2) - passenger.getTileY();
                  boolean move = true;
                  if (diffy != 0 || diffx != 0) {
                     BlockingResult result = Blocking.getBlockerBetween(
                        passenger,
                        passenger.getStatus().getPositionX(),
                        passenger.getStatus().getPositionY(),
                        newposx,
                        newposy,
                        passenger.getPositionZ(),
                        passenger.getPositionZ(),
                        passenger.isOnSurface(),
                        passenger.isOnSurface(),
                        false,
                        6,
                        -1L,
                        passenger.getBridgeId(),
                        passenger.getBridgeId(),
                        cretVehicle.followsGround()
                     );
                     if (result != null) {
                        Blocker first = result.getFirstBlocker();
                        if ((!first.isDoor() || !first.canBeOpenedBy(passenger, false) && driver != null && !first.canBeOpenedBy(driver, false))
                           && !(first instanceof BridgePart)) {
                           if (driver != null) {
                              newposx = driver.getPosX();
                              newposy = driver.getPosY();
                              diffx = ((int)newposx >> 2) - passenger.getTileX();
                              diffy = ((int)newposy >> 2) - passenger.getTileY();
                           } else {
                              move = false;
                              passenger.disembark(false);
                           }
                        }
                     }

                     if (move
                        && passenger.getLayer() < 0
                        && (
                           Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newposx >> 2, (int)newposy >> 2)))
                              || Blocking.isDiagonalRockBetween(passenger, passenger.getTileX(), passenger.getTileY(), (int)newposx >> 2, (int)newposy >> 2)
                                 != null
                        )
                        && driver != null) {
                        newposx = driver.getPosX();
                        newposy = driver.getPosY();
                        diffx = ((int)newposx >> 2) - passenger.getTileX();
                        diffy = ((int)newposy >> 2) - passenger.getTileY();
                     }

                     if (passenger.getStatus().isTrading()) {
                        Trade trade = passenger.getStatus().getTrade();
                        Creature lOpponent = null;
                        if (trade.creatureOne == passenger) {
                           lOpponent = trade.creatureTwo;
                        } else {
                           lOpponent = trade.creatureOne;
                        }

                        if (Creature.rangeTo(passenger, lOpponent) > 6) {
                           trade.end(passenger, false);
                        }
                     }
                  }

                  if (move) {
                     passenger.getStatus().setPositionXYZ(newposx, newposy, cretVehicle.getPositionZ() + aVehicle.seats[x].offz);
                     passenger.getMovementScheme()
                        .setPosition(
                           passenger.getStatus().getPositionX(),
                           passenger.getStatus().getPositionY(),
                           passenger.getStatus().getPositionZ(),
                           passenger.getStatus().getRotation(),
                           passenger.getLayer()
                        );
                     if (diffy != 0 || diffx != 0) {
                        try {
                           if (passenger.hasLink() && passenger.getVisionArea() != null) {
                              passenger.getVisionArea().move(diffx, diffy);
                              passenger.getVisionArea().linkZones(diffy, diffx);
                           }

                           Zone z = Zones.getZone(passenger.getTileX(), passenger.getTileY(), passenger.isOnSurface());
                           passenger.getStatus().savePosition(passenger.getWurmId(), true, z.getId(), false);
                        } catch (IOException var24) {
                           logger.log(Level.WARNING, var24.getMessage(), (Throwable)var24);
                           passenger.setLink(false);
                        } catch (NoSuchZoneException var25) {
                           logger.log(Level.WARNING, var25.getMessage(), (Throwable)var25);
                           passenger.setLink(false);
                        } catch (Exception var26) {
                        }
                     }

                     diffx = ((int)newposx >> 2) - passenger.getCurrentTile().tilex;
                     diffy = ((int)newposy >> 2) - passenger.getCurrentTile().tiley;
                     if (diffy != 0 || diffx != 0) {
                        try {
                           passenger.getCurrentTile().creatureMoved(passenger.getWurmId(), 0.0F, 0.0F, 0.0F, diffx, diffy, true);
                        } catch (NoSuchPlayerException var22) {
                        } catch (NoSuchCreatureException var23) {
                        }
                     }
                  }
               } catch (NoSuchPlayerException var28) {
               }
            }
         }
      } else {
         for(int x = 0; x < aVehicle.seats.length; ++x) {
            if (aVehicle.seats[x].type == 1 && aVehicle.seats[x].isOccupied()) {
               try {
                  passenger = Players.getInstance().getPlayer(aVehicle.seats[x].occupant);
                  float r = (-itemVehicle.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                  float s = (float)Math.sin((double)r);
                  float c = (float)Math.cos((double)r);
                  float xo = s * -aVehicle.seats[x].offx - c * -aVehicle.seats[x].offy;
                  float yo = c * -aVehicle.seats[x].offx + s * -aVehicle.seats[x].offy;
                  float newposx = itemVehicle.getPosX() + xo;
                  float newposy = itemVehicle.getPosY() + yo;
                  newposx = Math.max(3.0F, newposx);
                  newposx = Math.min(Zones.worldMeterSizeX - 3.0F, newposx);
                  newposy = Math.max(3.0F, newposy);
                  newposy = Math.min(Zones.worldMeterSizeY - 3.0F, newposy);
                  int diffx = ((int)newposx >> 2) - passenger.getTileX();
                  int diffy = ((int)newposy >> 2) - passenger.getTileY();
                  boolean move = true;
                  if (diffy != 0 || diffx != 0) {
                     if (passenger.isOnSurface()) {
                        BlockingResult result = Blocking.getBlockerBetween(
                           passenger,
                           passenger.getStatus().getPositionX(),
                           passenger.getStatus().getPositionY(),
                           newposx,
                           newposy,
                           passenger.getPositionZ(),
                           passenger.getPositionZ(),
                           passenger.isOnSurface(),
                           passenger.isOnSurface(),
                           false,
                           6,
                           -1L,
                           passenger.getBridgeId(),
                           passenger.getBridgeId(),
                           itemVehicle.getFloorLevel() == 0 && itemVehicle.getBridgeId() <= 0L
                        );
                        if (result != null) {
                           Blocker first = result.getFirstBlocker();
                           if ((!first.isDoor() || !first.canBeOpenedBy(passenger, false) && driver != null && !first.canBeOpenedBy(driver, false))
                              && !(first instanceof BridgePart)) {
                              if (driver != null) {
                                 newposx = driver.getPosX();
                                 newposy = driver.getPosY();
                                 diffx = ((int)newposx >> 2) - passenger.getTileX();
                                 diffy = ((int)newposy >> 2) - passenger.getTileY();
                              } else {
                                 move = false;
                                 passenger.disembark(false);
                              }
                           }
                        }
                     }

                     if (move
                        && passenger.getLayer() < 0
                        && (
                           Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newposx >> 2, (int)newposy >> 2)))
                              || Blocking.isDiagonalRockBetween(passenger, passenger.getTileX(), passenger.getTileY(), (int)newposx >> 2, (int)newposy >> 2)
                                 != null
                        )
                        && driver != null) {
                        newposx = driver.getPosX();
                        newposy = driver.getPosY();
                        diffx = ((int)newposx >> 2) - passenger.getTileX();
                        diffy = ((int)newposy >> 2) - passenger.getTileY();
                     }

                     if (passenger.getStatus().isTrading()) {
                        Trade trade = passenger.getStatus().getTrade();
                        Creature lOpponent = null;
                        if (trade.creatureOne == passenger) {
                           lOpponent = trade.creatureTwo;
                        } else {
                           lOpponent = trade.creatureOne;
                        }

                        if (Creature.rangeTo(passenger, lOpponent) > 6) {
                           trade.end(passenger, false);
                        }
                     }
                  }

                  if (move) {
                     passenger.getStatus().setPositionXYZ(newposx, newposy, itemVehicle.getPosZ() + aVehicle.seats[x].offz);
                     passenger.getMovementScheme()
                        .setPosition(
                           passenger.getStatus().getPositionX(),
                           passenger.getStatus().getPositionY(),
                           passenger.getStatus().getPositionZ(),
                           passenger.getStatus().getRotation(),
                           passenger.getLayer()
                        );
                     if (diffy != 0 || diffx != 0) {
                        try {
                           if (passenger.hasLink() && passenger.getVisionArea() != null) {
                              passenger.getVisionArea().move(diffx, diffy);
                              passenger.getVisionArea().linkZones(diffy, diffx);
                           }

                           Zone z = Zones.getZone(passenger.getTileX(), passenger.getTileY(), passenger.isOnSurface());
                           passenger.getStatus().savePosition(passenger.getWurmId(), true, z.getId(), false);
                        } catch (IOException var19) {
                           passenger.setLink(false);
                        } catch (NoSuchZoneException var20) {
                           passenger.setLink(false);
                        } catch (Exception var21) {
                        }
                     }

                     diffx = ((int)newposx >> 2) - passenger.getCurrentTile().tilex;
                     diffy = ((int)newposy >> 2) - passenger.getCurrentTile().tiley;
                     if (diffy != 0 || diffx != 0) {
                        try {
                           passenger.getCurrentTile().creatureMoved(passenger.getWurmId(), 0.0F, 0.0F, 0.0F, diffx, diffy, true);
                        } catch (NoSuchPlayerException var17) {
                        } catch (NoSuchCreatureException var18) {
                        }
                     }
                  }
               } catch (NoSuchPlayerException var27) {
               }
            }
         }
      }
   }

   public void moveVehicle(float diffX, float diffY, float diffZ) {
      if (!vehic.isChair()) {
         if (!vehic.creature) {
            try {
               itemVehicle = Items.getItem(vehic.wurmid);
               if (this.creature.isOnSurface() == itemVehicle.isOnSurface()) {
                  this.moveItemVehicleSameLevel();
               } else {
                  this.moveItemVehicleOtherLevel();
               }

               movePassengers(vehic, this.creature, false);
            } catch (NoSuchItemException var19) {
            }
         } else {
            try {
               cretVehicle = Creatures.getInstance().getCreature(vehic.wurmid);
               if (this.creature.isOnSurface() == cretVehicle.isOnSurface()) {
                  VolaTile t = Zones.getTileOrNull(cretVehicle.getTileX(), cretVehicle.getTileY(), cretVehicle.isOnSurface());
                  if (t == null) {
                     try {
                        Zone z = Zones.getZone(cretVehicle.getTileX(), cretVehicle.getTileY(), cretVehicle.isOnSurface());
                        z.removeCreature(cretVehicle, false, false);
                        z.addCreature(vehic.wurmid);
                     } catch (NoSuchZoneException var18) {
                     }

                     this.creature.disembark(true);
                     return;
                  }

                  Seat driverseat = vehic.getPilotSeat();
                  float diffrot = Creature.normalizeAngle(this.getVehicleRotation()) - cretVehicle.getStatus().getRotation();
                  cretVehicle.setRotation(Creature.normalizeAngle(this.getVehicleRotation()));
                  float _r = (-cretVehicle.getStatus().getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                  float _s = (float)Math.sin((double)_r);
                  float _c = (float)Math.cos((double)_r);
                  float xo = _s * -driverseat.offx - _c * -driverseat.offy;
                  float yo = _c * -driverseat.offx + _s * -driverseat.offy;
                  float nPosX = this.creature.getPosX() - xo;
                  float nPosY = this.creature.getPosY() - yo;
                  float nPosZ = this.creature.getPositionZ() - driverseat.offz;
                  nPosX = Math.max(3.0F, nPosX);
                  nPosX = Math.min(Zones.worldMeterSizeX - 3.0F, nPosX);
                  nPosY = Math.max(3.0F, nPosY);
                  nPosY = Math.min(Zones.worldMeterSizeY - 3.0F, nPosY);
                  if (!cretVehicle.isOnSurface()
                     && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)nPosX >> 2, (int)nPosY >> 2)))
                     && Tiles.decodeType(Server.caveMesh.getTile(cretVehicle.getTileX(), cretVehicle.getTileY())) != 201) {
                     return;
                  }

                  diffX = nPosX - cretVehicle.getPosX();
                  diffY = nPosY - cretVehicle.getPosY();
                  if (diffX != 0.0F || diffY != 0.0F) {
                     int dtx = ((int)nPosX >> 2) - cretVehicle.getTileX();
                     int dty = ((int)nPosY >> 2) - cretVehicle.getTileY();
                     cretVehicle.setPositionX(nPosX);
                     cretVehicle.setPositionY(nPosY);
                     cretVehicle.setPositionZ(nPosZ);

                     try {
                        cretVehicle.getVisionArea().move(dtx, dty);
                        cretVehicle.getVisionArea().linkZones(dtx, dty);
                     } catch (IOException var22) {
                     }

                     t.creatureMoved(vehic.wurmid, diffX, diffY, diffZ, dtx, dty);
                  } else if (diffrot != 0.0F) {
                     t.creatureMoved(vehic.wurmid, 0.0F, 0.0F, diffZ, 0, 0);
                  }
               } else {
                  Zone zone = null;

                  try {
                     zone = Zones.getZone(cretVehicle.getTileX(), cretVehicle.getTileY(), cretVehicle.isOnSurface());
                     zone.removeCreature(cretVehicle, false, false);
                  } catch (NoSuchZoneException var21) {
                  }

                  Seat driverseat = vehic.getPilotSeat();
                  cretVehicle.setRotation(Creature.normalizeAngle(this.getVehicleRotation()));
                  float _r = (-cretVehicle.getStatus().getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                  float _s = (float)Math.sin((double)_r);
                  float _c = (float)Math.cos((double)_r);
                  float xo = _s * -driverseat.offx - _c * -driverseat.offy;
                  float yo = _c * -driverseat.offx + _s * -driverseat.offy;
                  float nPosX = cretVehicle.getPosX() - xo;
                  float nPosY = cretVehicle.getPosY() - yo;
                  nPosX = Math.max(3.0F, nPosX);
                  nPosX = Math.min(Zones.worldMeterSizeX - 3.0F, nPosX);
                  nPosY = Math.max(3.0F, nPosY);
                  nPosY = Math.min(Zones.worldMeterSizeY - 3.0F, nPosY);
                  cretVehicle.setPositionX(nPosX);
                  cretVehicle.setPositionY(nPosY);
                  cretVehicle.setLayer(this.creature.getLayer(), false);

                  try {
                     zone = Zones.getZone(cretVehicle.getTileX(), cretVehicle.getTileY(), this.creature.isOnSurface());
                     zone.addCreature(cretVehicle.getWurmId());
                  } catch (NoSuchZoneException var20) {
                  }
               }

               movePassengers(vehic, this.creature, true);
            } catch (NoSuchCreatureException var23) {
            } catch (NoSuchPlayerException var24) {
            }
         }
      }
   }

   public void moveItemVehicleOtherLevel() {
      Seat driverseat = vehic.getPilotSeat();
      float _r = (-itemVehicle.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
      float _s = (float)Math.sin((double)_r);
      float _c = (float)Math.cos((double)_r);
      float xo = _s * -driverseat.offx - _c * -driverseat.offy;
      float yo = _c * -driverseat.offx + _s * -driverseat.offy;
      float nPosX = itemVehicle.getPosX() - xo;
      float nPosY = itemVehicle.getPosY() - yo;
      nPosX = Math.max(3.0F, nPosX);
      nPosX = Math.min(Zones.worldMeterSizeX - 3.0F, nPosX);
      nPosY = Math.max(3.0F, nPosY);
      nPosY = Math.min(Zones.worldMeterSizeY - 3.0F, nPosY);
      if (!this.creature.isOnSurface()) {
         int caveTile = Server.caveMesh.getTile((int)nPosX >> 2, (int)nPosY >> 2);
         if (Tiles.isSolidCave(Tiles.decodeType(caveTile))) {
            this.moveItemVehicleSameLevel();
            return;
         }
      }

      Zone zone = null;
      itemVehicle.newLayer = (byte)this.creature.getLayer();

      try {
         zone = Zones.getZone((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2, itemVehicle.isOnSurface());
         zone.removeItem(itemVehicle, true, true);
      } catch (NoSuchZoneException var19) {
      }

      itemVehicle.setPosXY(nPosX, nPosY);

      try {
         zone = Zones.getZone((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2, itemVehicle.newLayer >= 0);
         zone.addItem(itemVehicle, false, false, false);
      } catch (NoSuchZoneException var18) {
      }

      itemVehicle.newLayer = -128;
      Seat[] seats = vehic.hitched;
      if (seats != null) {
         for(int x = 0; x < seats.length; ++x) {
            if (seats[x] != null && seats[x].occupant != -10L) {
               try {
                  Creature c = Server.getInstance().getCreature(seats[x].occupant);
                  c.getStatus().setLayer(itemVehicle.isOnSurface() ? 0 : -1);
                  c.getCurrentTile().newLayer(c);
               } catch (NoSuchPlayerException var16) {
               } catch (NoSuchCreatureException var17) {
               }
            }
         }
      }

      Seat[] pseats = vehic.seats;
      if (pseats != null) {
         for(int x = 0; x < pseats.length; ++x) {
            if (x > 0 && pseats[x] != null && pseats[x].occupant != -10L) {
               try {
                  Creature c = Server.getInstance().getCreature(pseats[x].occupant);
                  logger.log(Level.INFO, c.getName() + " Setting to new layer " + (itemVehicle.isOnSurface() ? 0 : -1));
                  c.getStatus().setLayer(itemVehicle.isOnSurface() ? 0 : -1);
                  c.getCurrentTile().newLayer(c);
                  if (c.isPlayer()) {
                     if (itemVehicle.isOnSurface()) {
                        c.getCommunicator().sendNormalServerMessage("You leave the cave.");
                     } else {
                        c.getCommunicator().sendNormalServerMessage("You enter the cave.");
                        if (c.getVisionArea() != null) {
                           c.getVisionArea().initializeCaves();
                        }
                     }
                  }
               } catch (NoSuchPlayerException var14) {
               } catch (NoSuchCreatureException var15) {
               }
            }
         }
      }
   }

   public void moveItemVehicleSameLevel() {
      VolaTile t = Zones.getTileOrNull(itemVehicle.getTileX(), itemVehicle.getTileY(), itemVehicle.isOnSurface());
      if (t == null) {
         try {
            Zone z = Zones.getZone(itemVehicle.getTileX(), itemVehicle.getTileY(), itemVehicle.isOnSurface());
            z.removeItem(itemVehicle);
            z.addItem(itemVehicle);
         } catch (NoSuchZoneException var15) {
         }

         this.creature.disembark(true);
      } else {
         Seat driverseat = vehic.getPilotSeat();
         if (driverseat == null) {
            logger.warning("Driverseat null for " + this.creature.getName());
            this.creature.disembark(true);
         } else {
            float _r = (-itemVehicle.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
            float _s = (float)Math.sin((double)_r);
            float _c = (float)Math.cos((double)_r);
            float xo = _s * -driverseat.offx - _c * -driverseat.offy;
            float yo = _c * -driverseat.offx + _s * -driverseat.offy;
            float nPosX = this.creature.getPosX() - xo;
            float nPosY = this.creature.getPosY() - yo;
            float nPosZ = this.creature.getPositionZ() - driverseat.offz;
            nPosX = Math.max(3.0F, nPosX);
            nPosX = Math.min(Zones.worldMeterSizeX - 3.0F, nPosX);
            nPosY = Math.max(3.0F, nPosY);
            nPosY = Math.min(Zones.worldMeterSizeY - 3.0F, nPosY);
            int diffdecx = (int)(nPosX * 100.0F - itemVehicle.getPosX() * 100.0F);
            int diffdecy = (int)(nPosY * 100.0F - itemVehicle.getPosY() * 100.0F);
            if (diffdecx != 0 || diffdecy != 0) {
               nPosX = itemVehicle.getPosX() + (float)diffdecx * 0.01F;
               nPosY = itemVehicle.getPosY() + (float)diffdecy * 0.01F;
            }

            if (itemVehicle.isOnSurface()
               || !Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)nPosX >> 2, (int)nPosY >> 2)))
               || Tiles.decodeType(Server.caveMesh.getTile((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2)) == 201) {
               t.moveItem(
                  itemVehicle, nPosX, nPosY, nPosZ, Creature.normalizeAngle(this.getVehicleRotation()), itemVehicle.isOnSurface(), itemVehicle.getPosZ()
               );
               if (vehic.draggers != null) {
                  for(Creature c : vehic.draggers) {
                     if (c.isDead()) {
                        toRemove = c;
                     } else {
                        this.moveDragger(c);
                     }
                  }

                  if (toRemove != null) {
                     vehic.removeDragger(toRemove);
                     toRemove = null;
                  }
               }
            }
         }
      }
   }

   public void moveDragger(Creature c) {
      Vehicle v = c.getHitched();
      Seat seat = v.getHitchSeatFor(c.getWurmId());
      float _r = (-itemVehicle.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
      float _s = (float)Math.sin((double)_r);
      float _c = (float)Math.cos((double)_r);
      float xo = _s * -seat.offx - _c * -seat.offy;
      float yo = _c * -seat.offx + _s * -seat.offy;
      float nPosX = this.creature.getPosX() + xo;
      float nPosY = this.creature.getPosY() + yo;
      nPosX = Math.max(3.0F, nPosX);
      nPosX = Math.min(Zones.worldMeterSizeX - 3.0F, nPosX);
      nPosY = Math.max(3.0F, nPosY);
      nPosY = Math.min(Zones.worldMeterSizeY - 3.0F, nPosY);
      int diffdecx = (int)(nPosX * 100.0F - itemVehicle.getPosX() * 100.0F);
      int diffdecy = (int)(nPosY * 100.0F - itemVehicle.getPosY() * 100.0F);
      if (diffdecx != 0 || diffdecy != 0) {
         nPosX = itemVehicle.getPosX() + (float)diffdecx * 0.01F;
         nPosY = itemVehicle.getPosY() + (float)diffdecy * 0.01F;
      }

      this.moveDragger(c, nPosX, nPosY, itemVehicle.getPosZ(), Creature.normalizeAngle(this.getVehicleRotation()), false);
   }

   public void moveDragger(Creature c, float nPosX, float nPosY, float nPosZ, float newRot, boolean addRemove) {
      int diffx = ((int)nPosX >> 2) - c.getCurrentTile().tilex;
      int diffy = ((int)nPosY >> 2) - c.getCurrentTile().tiley;
      if (c.getLayer() < 0 && itemVehicle.isOnSurface()) {
         if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)nPosX >> 2, (int)nPosY >> 2)))) {
            c.setLayer(0, false);
         } else {
            byte typeSurf = Tiles.decodeType(Server.surfaceMesh.getTile((int)nPosX >> 2, (int)nPosY >> 2));
            if (typeSurf != 0) {
               c.setLayer(0, false);
            }
         }
      }

      c.getStatus().setPositionXYZ(nPosX, nPosY, nPosZ);
      c.getMovementScheme().setPosition(c.getStatus().getPositionX(), c.getStatus().getPositionY(), c.getStatus().getPositionZ(), newRot, c.getLayer());
      if (Math.abs(c.getStatus().getRotation() - newRot) > 10.0F) {
         c.setRotation(newRot);
         c.moved(0.0F, 0.0F, 0.0F, 0, 0);
      }

      if (diffy != 0 || diffx != 0) {
         try {
            if (c.getVisionArea() != null) {
               c.getVisionArea().move(diffx, diffy);
               c.getVisionArea().linkZones(diffy, diffx);
            }

            Zone z = Zones.getZone(c.getTileX(), c.getTileY(), c.isOnSurface());
            c.getStatus().savePosition(c.getWurmId(), c.isPlayer(), z.getId(), true);

            try {
               c.getCurrentTile().creatureMoved(c.getWurmId(), 0.0F, 0.0F, 0.0F, diffx, diffy, true);
            } catch (NoSuchPlayerException var13) {
            } catch (NoSuchCreatureException var14) {
            }
         } catch (IOException var15) {
         } catch (NoSuchZoneException var16) {
         }
      }

      if (addRemove) {
         diffx = ((int)nPosX >> 2) - passenger.getCurrentTile().tilex;
         diffy = ((int)nPosY >> 2) - passenger.getCurrentTile().tiley;
         if (diffy != 0 || diffx != 0) {
            try {
               passenger.getCurrentTile().creatureMoved(passenger.getWurmId(), 0.0F, 0.0F, 0.0F, diffx, diffy, true);
            } catch (NoSuchPlayerException var11) {
            } catch (NoSuchCreatureException var12) {
            }
         }
      }
   }

   public void move(float diffX, float diffY, float diffZ) {
      vehic = null;
      if (!this.isIntraTeleporting()) {
         int weight = this.creature.getCarriedWeight();
         if (this.draggedItem != null) {
            weight += this.draggedItem.getWeightGrams() / 100;
         }

         double moveDiff = Math.sqrt((double)(diffX * diffX + diffY * diffY)) * 15.0;
         double beforeZMod = moveDiff;
         if (this.isClimbing() && this.creature.getVehicle() == -10L && !this.creature.isUsingLastGasp()) {
            if (weight <= 10000) {
               weight = 10000;
            }

            if (this.creature.getLayer() <= 0) {
               short[] steepness = Creature.getTileSteepness(this.creature.getTileX(), this.creature.getTileY(), this.creature.isOnSurface());
               if (diffZ != 0.0F && (steepness[1] > 23 || steepness[1] < -23)) {
                  float gsbon = this.creature.getBonusForSpellEffect((byte)38) / 20.0F;
                  if (gsbon > 0.0F) {
                     moveDiff += (double)Math.max(1.0F, Math.abs(diffZ) * 500.0F / Math.max(1.0F, gsbon));
                  } else {
                     moveDiff += (double)Math.max(1.0F, Math.abs(diffZ) * 500.0F) / Math.max(1.0, Math.pow((double)(this.climbSkill / 10), 0.3));
                  }

                  if (Server.rand.nextInt(this.climbSkill * 2) == 0) {
                     this.climbSkill = Math.max(10, (int)this.creature.getClimbingSkill().getKnowledge(0.0));
                     if (this.creature.getStatus().getStamina() < 10000) {
                        int stam = (int)(101.0F - Math.max(10.0F, (float)this.creature.getStatus().getStamina() / 100.0F));
                        if ((gsbon <= 0.0F || this.creature.getStatus().getStamina() < 25 && Server.rand.nextInt(this.climbSkill) == 0)
                           && this.creature
                                 .getClimbingSkill()
                                 .skillCheck((double)stam, 0.0, this.creature.getStatus().getStamina() < 25, (float)Math.max(1, this.climbSkill / 20))
                              < 0.0) {
                           try {
                              this.creature.getCommunicator().sendNormalServerMessage("You need to catch your breath, and stop climbing.");
                              this.creature.setClimbing(false);
                              this.creature.getCommunicator().sendToggle(0, this.creature.isClimbing());
                           } catch (IOException var26) {
                              logger.log(Level.WARNING, this.creature.getName() + ' ' + var26.getMessage(), (Throwable)var26);
                           }
                        }
                     } else {
                        this.creature
                           .getClimbingSkill()
                           .skillCheck(
                              (double)Math.max(
                                 (float)(-this.climbSkill / 2), Math.min((float)this.climbSkill * 1.25F, (float)(steepness[1] - this.climbSkill * 2))
                              ),
                              0.0,
                              gsbon > 0.0F,
                              (float)Math.max(1, this.climbSkill / 20)
                           );
                     }
                  }
               }
            }
         } else if (diffZ < 0.0F) {
            moveDiff += Math.max(-moveDiff, (double)Math.min(-1.0F, diffZ * 100.0F));
         } else if (diffZ != 0.0F && moveDiff > 0.0) {
            moveDiff += Math.max(0.5, (double)(diffZ * 50.0F));
         }

         if (this.creature.isStealth()) {
            if (weight <= 10000) {
               weight = 10000;
            }

            weight += 5000;
         }

         if (diffX == 0.0F && diffY == 0.0F && diffZ == 0.0F) {
            if (this.getBitMask() == 0) {
               this.creature.getStatus().setMoving(false);
            }

            if (this.creature.isVehicleCommander()) {
               vehic = Vehicles.getVehicleForId(this.creature.getVehicle());
               if (vehic != null) {
                  this.moveVehicle(diffX, diffY, diffZ);
               }
            }
         } else {
            this.creature.getStatus().setMoving(true);
            MeshIO mesh = Server.surfaceMesh;
            if (this.creature.getLayer() < 0) {
               mesh = Server.caveMesh;
            }

            int tile = mesh.getTile(this.creature.getCurrentTile().tilex, this.creature.getCurrentTile().tiley);
            if (this.creature.isPlayer()) {
               short height = Tiles.decodeHeight(tile);
               if (height > 21800) {
                  if (!this.m2180m) {
                     this.m2180m = true;
                     this.creature.achievement(81);
                  }
               } else if (height > 14000) {
                  if (!this.m1400m) {
                     this.m1400m = true;
                     this.creature.achievement(80);
                  }
               } else if (height > 7000) {
                  if (!this.m700m) {
                     this.m700m = true;
                     this.creature.achievement(79);
                  }
               } else if (height > 3000) {
                  if (!this.m300m) {
                     this.m300m = true;
                     this.creature.achievement(78);
                  }
               } else if (height < -300 && !this.outAtSea) {
                  this.outAtSea = true;
                  this.creature.achievement(77);
               }
            }

            if (this.creature.isStealth() && Terraforming.isRoad(Tiles.decodeType(tile)) && Server.rand.nextInt(20) == 0) {
               this.creature.setStealth(false);
            }

            VolaTile vtile = Zones.getTileOrNull(this.creature.getCurrentTile().tilex, this.creature.getCurrentTile().tiley, this.creature.isOnSurface());
            if (vtile != null
               && (this.creature.getPower() < 2 || this.creature.loggerCreature1 > 0L)
               && vtile.hasOnePerTileItem(this.creature.getFloorLevel())
               && Servers.localServer.PVPSERVER) {
               Item barrier = vtile.getOnePerTileItem(this.creature.getFloorLevel());
               if (barrier != null && barrier.getTemplateId() == 938 && this.creature.getFarwalkerSeconds() <= 0) {
                  Rectangle rect = new Rectangle(this.creature.getCurrentTile().tilex * 4, this.creature.getCurrentTile().tiley * 4 + 1, 4, 1);
                  AffineTransform transform = new AffineTransform();
                  transform.rotate((double)(barrier.getRotation() - 90.0F), rect.getX() + (double)(rect.width / 2), rect.getY() + (double)(rect.height / 2));
                  Shape transformed = transform.createTransformedShape(rect);
                  if (transformed.contains((double)this.creature.getPosX(), (double)this.creature.getPosY())) {
                     Wound wound = null;
                     boolean dead = false;

                     try {
                        byte pos = this.creature.getBody().getRandomWoundPos();
                        if (Server.rand.nextInt(10) <= 6 && this.creature.getBody().getWounds() != null) {
                           wound = this.creature.getBody().getWounds().getWoundAtLocation(pos);
                           if (wound != null) {
                              dead = wound.modifySeverity(
                                 (int)(1000.0F + (float)Server.rand.nextInt(4000) * (100.0F - this.creature.getSpellDamageProtectBonus()) / 100.0F)
                              );
                              wound.setBandaged(false);
                              this.creature.setWounded();
                           }

                           barrier.setDamage(barrier.getDamage() + Server.rand.nextFloat() * 5.0F);
                        }

                        if (wound == null) {
                           dead = this.creature
                              .addWoundOfType(
                                 null,
                                 (byte)2,
                                 pos,
                                 false,
                                 1.0F,
                                 true,
                                 (double)(500.0F + (float)Server.rand.nextInt(6000) * (100.0F - this.creature.getSpellDamageProtectBonus()) / 100.0F),
                                 0.0F,
                                 0.0F,
                                 false,
                                 false
                              );
                        }

                        Server.getInstance().broadCastAction(this.creature.getNameWithGenus() + " is pierced by the barrier.", this.creature, 2);
                        this.creature.getCommunicator().sendAlertServerMessage("You are pierced by the barrier!");
                        if (dead) {
                           this.creature.achievement(143);
                           return;
                        }
                     } catch (Exception var25) {
                     }
                  }
               }
            }

            if (Tiles.decodeType(tile) == Tiles.Tile.TILE_LAVA.id) {
               if (this.creature.getPower() < 1
                  && (this.creature.getDeity() == null || !this.creature.getDeity().isMountainGod() || this.creature.getFaith() < 35.0F)
                  && this.creature.getFarwalkerSeconds() <= 0
                  && Server.rand.nextInt(10) == 0) {
                  Wound wound = null;
                  boolean dead = false;

                  try {
                     byte pos = 15;
                     if (Server.rand.nextBoolean()) {
                        pos = 16;
                     }

                     if (Server.rand.nextInt(10) <= 6 && this.creature.getBody().getWounds() != null) {
                        wound = this.creature.getBody().getWounds().getWoundAtLocation(pos);
                        if (wound != null) {
                           dead = wound.modifySeverity(
                              (int)(1000.0F + (float)Server.rand.nextInt(4000) * (100.0F - this.creature.getSpellDamageProtectBonus()) / 100.0F)
                           );
                           wound.setBandaged(false);
                           this.creature.setWounded();
                        }
                     }

                     if (wound == null && this.creature.isPlayer()) {
                        dead = this.creature
                           .addWoundOfType(
                              null,
                              (byte)4,
                              pos,
                              false,
                              1.0F,
                              true,
                              (double)(1000.0F + (float)Server.rand.nextInt(4000) * (100.0F - this.creature.getSpellDamageProtectBonus()) / 100.0F),
                              0.0F,
                              0.0F,
                              false,
                              false
                           );
                     }

                     this.creature.getCommunicator().sendAlertServerMessage("You are burnt by lava!");
                     if (dead) {
                        this.creature.achievement(142);
                        return;
                     }
                  } catch (Exception var24) {
                  }
               }
            } else if (this.creature.getDeity() != null && this.creature.getDeity().isForestGod() && !(this.creature.getFaith() < 35.0F)) {
               if (this.creature.getDeity() != null) {
                  if (this.creature.getDeity().isForestGod() && this.creature.getFaith() >= 70.0F) {
                     byte type = Tiles.decodeType(tile);
                     Tiles.Tile theTile = Tiles.getTile(type);
                     if (theTile.isNormalTree()
                        || theTile.isMyceliumTree()
                        || type == Tiles.Tile.TILE_GRASS.id
                        || type == Tiles.Tile.TILE_FIELD.id
                        || type == Tiles.Tile.TILE_FIELD2.id
                        || type == Tiles.Tile.TILE_DIRT.id
                        || type == Tiles.Tile.TILE_TUNDRA.id) {
                        weight = (int)((double)weight * 0.5);
                     }
                  } else if (this.creature.getDeity().isRoadProtector() && this.creature.getFaith() >= 60.0F && this.creature.getFavor() > 30.0F) {
                     byte type = Tiles.decodeType(tile);
                     if (Terraforming.isRoad(type)) {
                        weight = (int)((double)weight * 0.5);
                     }
                  } else if (this.creature.getDeity().isMountainGod() && this.creature.getFaith() >= 60.0F && this.creature.getFavor() > 30.0F) {
                     byte type = Tiles.decodeType(tile);
                     if (type == Tiles.Tile.TILE_ROCK.id || type == Tiles.Tile.TILE_SAND.id && this.creature.getKingdomId() == 2) {
                        weight = (int)((double)weight * 0.5);
                     }
                  } else if (this.creature.getDeity().isHateGod() && this.creature.getFaith() >= 60.0F && this.creature.getFavor() > 30.0F) {
                     byte type = Tiles.decodeType(tile);
                     Tiles.Tile theTile = Tiles.getTile(type);
                     if (type == Tiles.Tile.TILE_MYCELIUM.id || theTile.isMyceliumTree()) {
                        weight = (int)((double)weight * 0.5);
                     }
                  }
               }
            } else if (this.creature.getPower() < 1) {
               Tiles.Tile theTile = Tiles.getTile(Tiles.decodeType(tile));
               if (theTile.isNormalBush() || theTile.isMyceliumBush()) {
                  byte data = Tiles.decodeData(tile);
                  byte age = FoliageAge.getAgeAsByte(data);
                  if (theTile.isThorn(data) && age > FoliageAge.OLD_TWO.getAgeId() && this.creature.getFarwalkerSeconds() <= 0 && Server.rand.nextInt(10) == 0
                     )
                   {
                     Wound wound = null;
                     boolean dead = false;

                     try {
                        byte pos = this.creature.getBody().getRandomWoundPos();
                        if (Server.rand.nextInt(10) <= 6 && this.creature.getBody().getWounds() != null) {
                           wound = this.creature.getBody().getWounds().getWoundAtLocation(pos);
                           if (wound != null) {
                              if (Tiles.getTile(Tiles.decodeType(tile)).isMyceliumBush() && this.creature.getKingdomTemplateId() == 3) {
                                 dead = wound.modifySeverity(
                                    (int)(500.0F + (float)Server.rand.nextInt(2000) * (100.0F - this.creature.getSpellDamageProtectBonus()) / 100.0F)
                                 );
                              } else {
                                 dead = wound.modifySeverity(
                                    (int)(1000.0F + (float)Server.rand.nextInt(4000) * (100.0F - this.creature.getSpellDamageProtectBonus()) / 100.0F)
                                 );
                              }

                              wound.setBandaged(false);
                              this.creature.setWounded();
                           }
                        }

                        if (wound == null && WurmId.getType(this.creature.getWurmId()) == 0) {
                           if (Tiles.getTile(Tiles.decodeType(tile)).isMyceliumBush() && this.creature.getKingdomId() == 3) {
                              dead = this.creature
                                 .addWoundOfType(
                                    null,
                                    (byte)2,
                                    pos,
                                    false,
                                    1.0F,
                                    true,
                                    (double)(500.0F + (float)Server.rand.nextInt(4000) * (100.0F - this.creature.getSpellDamageProtectBonus()) / 100.0F),
                                    0.0F,
                                    0.0F,
                                    false,
                                    false
                                 );
                           } else {
                              dead = this.creature
                                 .addWoundOfType(
                                    null,
                                    (byte)2,
                                    pos,
                                    false,
                                    1.0F,
                                    true,
                                    (double)(500.0F + (float)Server.rand.nextInt(6000) * (100.0F - this.creature.getSpellDamageProtectBonus()) / 100.0F),
                                    0.0F,
                                    0.0F,
                                    false,
                                    false
                                 );
                           }
                        }

                        this.creature.getCommunicator().sendAlertServerMessage("You are pierced by the sharp thorns!");
                        if (dead) {
                           this.creature.achievement(143);
                           return;
                        }
                     } catch (Exception var27) {
                     }
                  }
               }
            }

            if (this.draggedItem != null) {
               try {
                  if (this.moveDraggedItem() && this.draggedItem != null) {
                     vehic = Vehicles.getVehicleForId(this.draggedItem.getWurmId());
                     if (vehic != null) {
                        if (vehic.creature) {
                           try {
                              cretVehicle = Server.getInstance().getCreature(vehic.wurmid);
                           } catch (NoSuchCreatureException var21) {
                           } catch (NoSuchPlayerException var22) {
                           }
                        } else {
                           try {
                              itemVehicle = Items.getItem(vehic.wurmid);
                           } catch (NoSuchItemException var20) {
                           }
                        }

                        movePassengers(vehic, this.creature, vehic.creature);
                     }

                     weight += this.draggedItem.getFullWeight() / 10;
                     if (moveDiff > 0.0) {
                        weight *= 2;
                     }
                  }
               } catch (NoSuchZoneException var23) {
                  Items.stopDragging(this.draggedItem);
               }
            }

            if (this.creature.isVehicleCommander()) {
               vehic = Vehicles.getVehicleForId(this.creature.getVehicle());
               if (vehic != null && !vehic.isChair()) {
                  this.moveVehicle(diffX, diffY, diffZ);
               }

               try {
                  weight += Items.getItem(this.creature.getVehicle()).getWeightGrams() / 100;
               } catch (NoSuchItemException var19) {
               }
            }

            if (this.creature.getPower() < 2 && (double)this.creature.getPositionZ() < -1.3 && this.creature.getVehicle() == -10L) {
               if (!this.hasWetFeet) {
                  this.hasWetFeet = true;
                  this.creature.achievement(70);
               }

               if (!PlonkData.SWIMMING.hasSeenThis(this.creature)) {
                  PlonkData.SWIMMING.trigger(this.creature);
               }

               ++moveDiff;
               weight += 20000;
               if (this.creature.getStatus().getStamina() < 50 && !this.creature.isSubmerged() && !this.creature.isUndead() && Server.rand.nextInt(100) == 0) {
                  this.creature
                     .addWoundOfType(
                        null,
                        (byte)7,
                        2,
                        false,
                        1.0F,
                        false,
                        (double)((4000.0F + Server.rand.nextFloat() * 3000.0F) * ItemBonus.getDrownDamReduction(this.creature)),
                        0.0F,
                        0.0F,
                        false,
                        false
                     );
                  this.creature.getCommunicator().sendAlertServerMessage("You are drowning!");
               }
            }
         }

         if ((beforeZMod > 0.0 || moveDiff > 0.0) && (this.creature.getVehicle() == -10L || this.isMovingVehicle() || this.draggedItem != null)) {
            this.creature.getStatus().setNormalRegen(false);
            if (moveDiff > 0.0) {
               this.creature.getStatus().modifyStamina((float)((int)(-moveDiff * (double)weight / 5000.0)));
            }
         }
      }
   }

   public final void setHasSpiritSpeed(boolean hasSpeed) {
      this.hasSpiritSpeed = hasSpeed;
   }

   public float getSpeedModifier() {
      if (this.halted) {
         return 0.0F;
      } else {
         double bonus = 0.0;
         if (this.modifiers != null) {
            double webHurtModifier = 0.0;

            for(DoubleValueModifier lDoubleValueModifier : this.modifiers) {
               if (lDoubleValueModifier.getType() == 7) {
                  if (lDoubleValueModifier.getModifier() < webHurtModifier) {
                     webHurtModifier = lDoubleValueModifier.getModifier();
                  }
               } else {
                  bonus += lDoubleValueModifier.getModifier();
               }
            }

            bonus += webHurtModifier;
         }

         if (bonus < -4.0) {
            return 0.0F;
         } else if (this.encumbered) {
            return 0.05F;
         } else {
            if (this.hasSpiritSpeed) {
               bonus *= 1.05F;
            }

            return (float)Math.max(0.05F, (double)this.baseModifier + bonus);
         }
      }
   }

   public void setEncumbered(boolean enc) {
      if (this.creature.getVehicle() != -10L) {
         this.encumbered = false;
      } else {
         this.encumbered = enc;
      }
   }

   public void setBaseModifier(float base) {
      if (this.creature.getVehicle() != -10L) {
         this.baseModifier = 1.0F;
      } else {
         this.baseModifier = base;
      }

      this.update();
   }

   public void update() {
      if (!this.creature.isPlayer()) {
         if (this.creature.isRidden()) {
            this.creature.forceMountSpeedChange();
         }
      } else {
         if (!this.halted) {
            this.sendSpeedModifier();
         }
      }
   }

   public void haltSpeedModifier() {
      this.addSpeedMod(0.0F);
      this.creature.getCommunicator().sendSpeedModifier(0.0F);
      this.halted = true;
   }

   public void resumeSpeedModifier() {
      this.halted = false;
      this.sendSpeedModifier();
   }

   public void stopSendingSpeedModifier() {
      this.halted = true;
   }

   public Item getDraggedItem() {
      return this.draggedItem;
   }

   public void setDraggedItem(@Nullable Item dragged) {
      if (this.draggedItem != null && !this.draggedItem.equals(dragged)) {
         if (dragged != null) {
            Items.stopDragging(this.draggedItem);
         } else {
            this.creature.getCommunicator().sendNormalServerMessage("You stop dragging " + this.draggedItem.getNameWithGenus() + '.');
            if (this.draggedItem.getTemplateId() == 1125) {
               this.removeModifier(ramDragMod);
            } else {
               this.removeModifier(dragMod);
            }
         }
      }

      this.draggedItem = dragged;
      if (this.draggedItem != null) {
         this.creature.getCommunicator().sendNormalServerMessage("You start dragging " + this.draggedItem.getNameWithGenus() + '.');
         if (this.draggedItem.getTemplateId() == 1125) {
            this.addModifier(ramDragMod);
         } else {
            this.addModifier(dragMod);
         }
      }
   }

   public void setFightMoveMod(boolean fighting) {
      if (fighting) {
         this.addModifier(combatMod);
      } else {
         this.removeModifier(combatMod);
      }
   }

   public void setFarwalkerMoveMod(boolean add) {
      if (add) {
         this.addModifier(farwalkerMod);
      } else {
         this.removeModifier(farwalkerMod);
      }
   }

   public boolean setWebArmourMod(boolean add, float power) {
      if (add) {
         if (!this.webArmoured) {
            this.webArmoured = true;
            this.justWebSlowArmour = true;
            this.webArmourMod.setModifier((double)(-power / 200.0F));
            this.addModifier(this.webArmourMod);
         }
      } else if (this.webArmoured) {
         if (this.justWebSlowArmour) {
            this.justWebSlowArmour = false;
         } else {
            this.webArmoured = false;
            this.removeModifier(this.webArmourMod);
            this.webArmourMod.setModifier(0.0);
         }
      }

      return this.webArmoured;
   }

   public double getWebArmourMod() {
      return this.webArmoured ? this.webArmourMod.getModifier() : 0.0;
   }

   public void setChargeMoveMod(boolean add) {
      if (add) {
         this.addModifier(chargeMod);
      } else {
         this.removeModifier(chargeMod);
      }
   }

   public void setDrunkMod(boolean drunk) {
      if (drunk) {
         this.addModifier(drunkMod);
      } else {
         this.removeModifier(drunkMod);
      }
   }

   public void setMooredMod(boolean moored) {
      if (moored) {
         this.addModifier(mooreMod);
      } else {
         this.removeModifier(mooreMod);
      }
   }

   public void setStealthMod(boolean stealth) {
      if (stealth) {
         this.addModifier(this.stealthMod);
      } else {
         this.removeModifier(this.stealthMod);
      }
   }

   public void setFreezeMod(boolean frozen) {
      if (frozen) {
         this.addModifier(freezeMod);
      } else {
         this.removeModifier(freezeMod);
      }
   }

   private final float getDragDistanceMod(int templateId) {
      switch(templateId) {
         case 539:
            return -2.0F;
         case 853:
         case 1410:
            return -3.0F;
         default:
            return -1.5F;
      }
   }

   protected boolean moveDraggedItem() throws NoSuchZoneException {
      int weight = this.draggedItem.getFullWeight(true);
      int left = this.creature.getCarryingCapacityLeft();
      if ((this.draggedItem.getTemplateId() != 539 || weight >= left) && weight >= left * 10) {
         this.creature.getCommunicator().sendNormalServerMessage("The " + this.draggedItem.getName() + " is too heavy.");
         Items.stopDragging(this.draggedItem);
         return false;
      } else {
         float iposx = this.creature.getStatus().getPositionX();
         float iposy = this.creature.getStatus().getPositionY();
         float rot = this.creature.getStatus().getRotation();
         float oldPosZ = this.creature.getPositionZ();
         float distMod = this.getDragDistanceMod(this.draggedItem.getTemplateId());
         float xPosMod = (float)Math.sin((double)(rot * (float) (Math.PI / 180.0))) * distMod;
         float yPosMod = -((float)Math.cos((double)(rot * (float) (Math.PI / 180.0)))) * distMod;
         float newPosX = iposx + xPosMod;
         float newPosY = iposy + yPosMod;
         if (!this.creature.isOnSurface()
            && (
               Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newPosX >> 2, (int)newPosY >> 2)))
                  || Blocking.isDiagonalRockBetween(this.creature, (int)iposx >> 2, (int)iposy >> 2, (int)newPosX >> 2, (int)newPosY >> 2) != null
            )) {
            if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)iposx >> 2, (int)iposy >> 2)))) {
               Items.stopDragging(this.draggedItem);
               return false;
            }

            newPosX = iposx;
            newPosY = iposy;
         }

         if (this.draggedItem.onBridge() > 0L) {
            int ntx = (int)newPosX >> 2;
            int nty = (int)newPosY >> 2;
            if (this.draggedItem.getTileX() != ntx || this.draggedItem.getTileY() != nty) {
               VolaTile newTile = Zones.getOrCreateTile(ntx, nty, this.draggedItem.isOnSurface());
               if (newTile == null || newTile.getStructure() == null || newTile.getStructure().getWurmId() != this.draggedItem.onBridge()) {
                  VolaTile oldTile = Zones.getOrCreateTile(this.draggedItem.getTileX(), this.draggedItem.getTileY(), this.draggedItem.isOnSurface());
                  boolean leavingOnSide = false;
                  if (oldTile != null) {
                     BridgePart[] bridgeParts = oldTile.getBridgeParts();
                     if (bridgeParts != null) {
                        int var19 = bridgeParts.length;
                        byte var20 = 0;
                        if (var20 < var19) {
                           BridgePart bp = bridgeParts[var20];
                           if ((bp.getDir() == 0 || bp.getDir() == 4) && this.draggedItem.getTileX() != ntx) {
                              leavingOnSide = true;
                           } else if ((bp.getDir() == 2 || bp.getDir() == 6) && this.draggedItem.getTileY() != nty) {
                              leavingOnSide = true;
                           }
                        }
                     }
                  }

                  if (leavingOnSide) {
                     newPosX = iposx;
                     newPosY = iposy;
                  }
               }
            }
         }

         float newPosZ = Zones.calculatePosZ(newPosX, newPosY, null, this.draggedItem.isOnSurface(), false, oldPosZ, null, this.draggedItem.onBridge());
         float maxDepth = -6.0F;
         if (this.draggedItem.isVehicle() && !this.draggedItem.isBoat()) {
            Vehicle lVehicle = Vehicles.getVehicle(this.draggedItem);
            maxDepth = lVehicle.getMaxDepth();
         }

         if (this.draggedItem.isFloating() && (double)newPosZ > 0.3) {
            this.creature.getCommunicator().sendAlertServerMessage("The " + this.draggedItem.getName() + " gets stuck in the ground.", (byte)3);
            Items.stopDragging(this.draggedItem);
            return false;
         } else if (!this.draggedItem.isFloating() && newPosZ < maxDepth && this.draggedItem.onBridge() <= 0L) {
            this.creature.getCommunicator().sendAlertServerMessage("The " + this.draggedItem.getName() + " gets stuck on the bottom.", (byte)3);
            Items.stopDragging(this.draggedItem);
            return false;
         } else {
            if (this.creature.isOnSurface() == this.draggedItem.isOnSurface()) {
               VolaTile t = Zones.getTileOrNull(this.draggedItem.getTileX(), this.draggedItem.getTileY(), this.draggedItem.isOnSurface());
               if (t == null) {
                  Items.stopDragging(this.draggedItem);
                  return false;
               }

               if (!this.draggedItem.isOnSurface()
                  && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newPosX >> 2, (int)newPosY >> 2)))
                  && Tiles.decodeType(Server.caveMesh.getTile(this.draggedItem.getTileX(), this.draggedItem.getTileY())) != 201) {
                  return false;
               }

               t.moveItem(this.draggedItem, newPosX, newPosY, newPosZ, Creature.normalizeAngle(rot), this.creature.isOnSurface(), oldPosZ);
            } else {
               Zone zone = null;
               if (this.creature.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newPosX >> 2, (int)newPosY >> 2)))) {
                  newPosX = iposx;
                  newPosY = iposy;
               }

               try {
                  zone = Zones.getZone(this.draggedItem.getTileX(), this.draggedItem.getTileY(), this.draggedItem.isOnSurface());
                  zone.removeItem(this.draggedItem);
               } catch (NoSuchZoneException var24) {
               }

               this.draggedItem.setPosXYZ(newPosX, newPosY, newPosZ);
               this.draggedItem.newLayer = (byte)(this.creature.isOnSurface() ? 0 : -1);
               zone = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, this.creature.isOnSurface());
               zone.addItem(this.draggedItem, true, this.creature.isOnSurface(), false);
               this.draggedItem.newLayer = -128;
               if (this.draggedItem.isVehicle()) {
                  Vehicle vehicle = Vehicles.getVehicleForId(this.draggedItem.getWurmId());
                  if (vehicle != null) {
                     Seat[] seats = vehicle.getSeats();
                     if (seats != null) {
                        for(int x = 0; x < seats.length; ++x) {
                           if (seats[x] != null && seats[x].occupant != -10L) {
                              try {
                                 Creature c = Server.getInstance().getCreature(seats[x].occupant);
                                 c.setLayer(this.creature.getLayer(), false);
                                 c.refreshVisible();
                                 c.getCommunicator().attachCreature(-1L, this.draggedItem.getWurmId(), seats[x].offx, seats[x].offy, seats[x].offz, x);
                              } catch (NoSuchPlayerException var22) {
                              } catch (NoSuchCreatureException var23) {
                              }
                           }
                        }
                     }
                  }
               }
            }

            return true;
         }
      }
   }

   public void addModifier(DoubleValueModifier modifier) {
      if (this.creature.isPlayer() || this.creature.isVehicle()) {
         if (this.modifiers == null) {
            this.modifiers = new HashSet<>();
         }

         if (!this.modifiers.contains(modifier)) {
            this.modifiers.add(modifier);
         }

         this.update();
      }
   }

   public void removeModifier(DoubleValueModifier modifier) {
      if (this.modifiers != null) {
         this.modifiers.remove(modifier);
      }

      this.update();
   }

   @Override
   public void valueChanged(double oldValue, double newValue) {
      this.update();
   }

   public final void touchFreeMoveCounter() {
      this.changedTileCounter = 5;
   }

   public final void decreaseFreeMoveCounter() {
      if (this.changedTileCounter > 0) {
         --this.changedTileCounter;
      }
   }

   @Override
   public int getMaxTargetGroundOffset(int suggestedOffset) {
      if (this.creature.getPower() > 0) {
         return suggestedOffset;
      } else {
         float xPos = this.getCurrx();
         float yPos = this.getCurry();
         if (xPos == 0.0F && yPos == 0.0F) {
            xPos = this.getX();
            yPos = this.getY();
         }

         VolaTile t = Zones.getOrCreateTile((int)xPos / 4, (int)yPos / 4, this.getLayer() >= 0);
         return t == null ? 0 : t.getMaxFloorLevel() * 30 + 30;
      }
   }

   public int getErrors() {
      return this.errors;
   }

   public void setErrors(int errors) {
      this.errors = errors;
   }
}
