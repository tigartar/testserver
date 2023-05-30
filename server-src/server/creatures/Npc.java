package com.wurmonline.server.creatures;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.BehaviourDispatcher;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.ai.ChatManager;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

public class Npc extends Creature {
   static final Random faceRandom = new Random();
   int lastX = 0;
   int lastY = 0;
   final ChatManager chatManager;
   LongTarget longTarget;
   int longTargetAttempts = 0;
   int passiveCounter = 0;
   int MAXSEED = 100;

   public Npc() throws Exception {
      this.chatManager = new ChatManager(this);
   }

   public Npc(CreatureTemplate aTemplate) throws Exception {
      super(aTemplate);
      this.chatManager = new ChatManager(this);
   }

   public Npc(long aId) throws Exception {
      super(aId);
      this.chatManager = new ChatManager(this);
   }

   public final ChatManager getChatManager() {
      return this.chatManager;
   }

   @Override
   public final byte getKingdomId() {
      return this.isAggHuman() ? 0 : this.status.kingdom;
   }

   @Override
   public final boolean isAggHuman() {
      return this.status.modtype == 2;
   }

   @Override
   public final void pollNPCChat() {
      this.getChatManager().checkChats();
   }

   @Override
   public final void pollNPC() {
      this.checkItemSpawn();
      if (this.passiveCounter-- == 0) {
         this.doSomething();
      }
   }

   private final void doSomething() {
      if (!this.isFighting() && this.target == -10L) {
         if (!this.capturePillar()) {
            if (!this.performLongTargetAction()) {
               if (this.getStatus().getPath() == null && Server.rand.nextBoolean()) {
                  this.startPathing(0);
                  this.setPassiveCounter(120);
               } else {
                  int seed = Server.rand.nextInt(this.MAXSEED);
                  if (seed < 10) {
                     if (this.getStatus().damage > 0) {
                        Wound[] wounds = this.getBody().getWounds().getWounds();
                        if (wounds.length > 0) {
                           Wound rand = wounds[Server.rand.nextInt(wounds.length)];
                           if (Server.rand.nextBoolean()) {
                              rand.setBandaged(true);
                              if (Server.rand.nextBoolean()) {
                                 rand.setHealeff((byte)(Server.rand.nextInt(70) + 30));
                              }
                           } else {
                              rand.heal();
                           }
                        }
                     }

                     this.setPassiveCounter(30);
                  }

                  if (seed < 20) {
                     Item[] allItems = this.getInventory().getAllItems(false);
                     if (allItems.length > 0) {
                        Item rand = allItems[Server.rand.nextInt(allItems.length)];

                        try {
                           if (rand.isFood()) {
                              BehaviourDispatcher.action(this, this.communicator, -10L, rand.getWurmId(), (short)182);
                           } else if (rand.isLiquid()) {
                              BehaviourDispatcher.action(this, this.communicator, -10L, rand.getWurmId(), (short)183);
                           } else {
                              BehaviourDispatcher.action(this, this.communicator, -10L, rand.getWurmId(), (short)118);
                           }
                        } catch (Exception var15) {
                        }
                     }

                     this.setPassiveCounter(30);
                  }

                  if (seed < 30) {
                     if (this.getCurrentTile() != null) {
                        try {
                           Item[] allItems = this.getInventory().getAllItems(false);
                           Item[] groundItems = this.getCurrentTile().getItems();
                           Item rand = null;
                           if (allItems.length > 0) {
                              rand = allItems[Server.rand.nextInt(allItems.length)];
                           }

                           if (groundItems.length > 0) {
                              Item targ = groundItems[Server.rand.nextInt(groundItems.length)];
                              if (!Server.rand.nextBoolean() || this.getCurrentTile().getVillage() != null) {
                                 BehaviourDispatcher.action(this, this.communicator, -10L, targ.getWurmId(), (short)162);
                              } else if (Server.rand.nextInt(4) == 0 && targ != null && targ.isHollow() && targ.testInsertItem(rand)) {
                                 targ.insertItem(rand);
                              } else if (this.canCarry(targ.getWeightGrams())) {
                                 BehaviourDispatcher.action(this, this.communicator, -10L, targ.getWurmId(), (short)6);
                              } else if (targ.isHollow()) {
                                 Item[] containeds = targ.getAllItems(false);
                                 if (containeds.length > 0) {
                                    Item targ2 = containeds[Server.rand.nextInt(containeds.length)];
                                    if (!targ2.isBodyPart() && !targ2.isNoTake()) {
                                       BehaviourDispatcher.action(this, this.communicator, -10L, targ2.getWurmId(), (short)6);
                                       this.wearItems();
                                    }
                                 }
                              }
                           } else {
                              BehaviourDispatcher.action(this, this.communicator, -10L, rand.getWurmId(), (short)162);
                           }

                           this.setPassiveCounter(30);
                        } catch (Exception var20) {
                        }
                     }

                     this.setPassiveCounter(10);
                  }

                  if (seed < 40) {
                     if (this.getCurrentTile() != null) {
                        try {
                           Item[] allItems = this.getInventory().getAllItems(false);
                           Item[] groundItems = this.getCurrentTile().getItems();
                           Item rand = null;
                           if (allItems.length > 0) {
                              rand = allItems[Server.rand.nextInt(allItems.length)];
                           }

                           if (groundItems.length > 0) {
                              Item targ = groundItems[Server.rand.nextInt(groundItems.length)];
                              if (Server.rand.nextBoolean() && this.getCurrentTile().getVillage() == null) {
                                 if (Server.rand.nextInt(4) == 0 && targ != null && targ.isHollow() && targ.testInsertItem(rand)) {
                                    targ.insertItem(rand);
                                 } else if (this.canCarry(targ.getWeightGrams())) {
                                    BehaviourDispatcher.action(this, this.communicator, -10L, targ.getWurmId(), (short)6);
                                 } else if (targ.isHollow()) {
                                    Item[] containeds = targ.getAllItems(false);
                                    if (containeds.length > 0) {
                                       Item targ2 = containeds[Server.rand.nextInt(containeds.length)];
                                       if (!targ2.isBodyPart() && !targ2.isNoTake()) {
                                          BehaviourDispatcher.action(this, this.communicator, -10L, targ2.getWurmId(), (short)6);
                                          this.wearItems();
                                       }
                                    }
                                 }
                              } else {
                                 if (targ.isHollow()) {
                                    Item[] containeds = targ.getAllItems(false);
                                    if (containeds.length > 0 && Server.rand.nextBoolean()) {
                                       targ = containeds[Server.rand.nextInt(containeds.length)];
                                    }
                                 }

                                 Behaviour behaviour = Action.getBehaviour(targ.getWurmId(), this.isOnSurface());
                                 BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForItemsBodyIdsCoinIds(
                                    this, targ.getWurmId(), rand, behaviour
                                 );
                                 List<ActionEntry> actions = param.getAvailableActions();
                                 if (actions.size() > 0) {
                                    ActionEntry ae = actions.get(Server.rand.nextInt(actions.size()));
                                    if (ae.getNumber() > 0) {
                                       BehaviourDispatcher.action(this, this.communicator, rand.getWurmId(), targ.getWurmId(), ae.getNumber());
                                    }
                                 }
                              }
                           } else if (rand != null) {
                              BehaviourDispatcher.action(this, this.communicator, -10L, rand.getWurmId(), (short)162);
                           }

                           this.setPassiveCounter(30);
                        } catch (Exception var19) {
                        }
                     }

                     this.setPassiveCounter(10);
                  }

                  if (seed < 50) {
                     if (this.getCurrentTile() != null) {
                        try {
                           Item[] allItems = this.getInventory().getAllItems(false);
                           Item rand = null;
                           if (allItems.length <= 0) {
                              if (this.isOnSurface()) {
                                 long targTile = Tiles.getTileId(this.getTileX(), this.getTileY(), 0);
                                 Behaviour behaviour = Action.getBehaviour(targTile, this.isOnSurface());
                                 BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForTiles(this, targTile, true, null, behaviour);
                                 List<ActionEntry> actions = param.getAvailableActions();
                                 if (actions.size() > 0) {
                                    ActionEntry ae = actions.get(Server.rand.nextInt(actions.size()));
                                    if (ae.getNumber() > 0) {
                                       BehaviourDispatcher.action(this, this.communicator, -10L, targTile, ae.getNumber());
                                    }
                                 }
                              }
                           } else {
                              boolean abilused = false;

                              for(Item abil : allItems) {
                                 if (abil.isAbility() && Server.rand.nextBoolean()) {
                                    BehaviourDispatcher.action(this, this.communicator, -10L, abil.getWurmId(), (short)118);
                                    abilused = true;
                                    break;
                                 }
                              }

                              if (!abilused) {
                                 rand = allItems[Server.rand.nextInt(allItems.length)];
                                 if (Server.rand.nextInt(5) == 0
                                    && !rand.isEpicTargetItem()
                                    && rand.isUnique()
                                    && !rand.isAbility()
                                    && !rand.isMagicStaff()
                                    && !rand.isRoyal()) {
                                    rand.putItemInfrontof(this);
                                 } else if (this.isOnSurface()) {
                                    long targTile = Tiles.getTileId(this.getTileX(), this.getTileY(), 0);
                                    Behaviour behaviour = Action.getBehaviour(targTile, this.isOnSurface());
                                    BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForTiles(this, targTile, true, rand, behaviour);
                                    List<ActionEntry> actions = param.getAvailableActions();
                                    if (actions.size() > 0) {
                                       ActionEntry ae = actions.get(Server.rand.nextInt(actions.size()));
                                       if (ae.getNumber() > 0) {
                                          BehaviourDispatcher.action(this, this.communicator, rand == null ? -10L : rand.getWurmId(), targTile, ae.getNumber());
                                       }
                                    }
                                 }
                              }
                           }

                           this.setPassiveCounter(30);
                        } catch (Exception var18) {
                        }
                     }

                     this.setPassiveCounter(10);
                  }

                  if (seed < 70) {
                     if (this.getCurrentTile() != null) {
                        try {
                           Item[] allItems = this.getInventory().getAllItems(false);
                           Item rand = null;
                           if (allItems.length > 0) {
                              rand = allItems[Server.rand.nextInt(allItems.length)];
                           }

                           boolean found = false;
                           Creature[] crets = null;

                           for(int x = -2; x <= 2; ++x) {
                              for(int y = -2; y <= 2; ++y) {
                                 VolaTile t = Zones.getTileOrNull(
                                    Zones.safeTileX(this.getTileX() + x), Zones.safeTileY(this.getTileY() + y), this.isOnSurface()
                                 );
                                 if (t != null) {
                                    crets = t.getCreatures();
                                    if (crets.length > 0) {
                                       Creature targC = crets[Server.rand.nextInt(crets.length)];
                                       Behaviour behaviour = Action.getBehaviour(targC.getWurmId(), this.isOnSurface());
                                       BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForCreaturesPlayers(
                                          this, targC.getWurmId(), rand, targC.isPlayer() ? 0 : 1, behaviour
                                       );
                                       List<ActionEntry> actions = param.getAvailableActions();
                                       if (actions.size() > 0) {
                                          ActionEntry ae = actions.get(Server.rand.nextInt(actions.size()));
                                          if (ae.getNumber() > 0) {
                                             BehaviourDispatcher.action(
                                                this, this.communicator, rand == null ? -10L : rand.getWurmId(), targC.getWurmId(), ae.getNumber()
                                             );
                                          }

                                          this.setPassiveCounter(30);
                                          found = true;
                                          break;
                                       }
                                    }
                                 }
                              }
                           }

                           if (!found) {
                              long targTile = Tiles.getTileId(
                                 this.getTileX() - 1 + Server.rand.nextInt(2), this.getTileY() - 1 + Server.rand.nextInt(2), 0, this.isOnSurface()
                              );
                              Behaviour behaviour = Action.getBehaviour(targTile, this.isOnSurface());
                              BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForTiles(this, targTile, true, rand, behaviour);
                              List<ActionEntry> actions = param.getAvailableActions();
                              if (actions.size() > 0) {
                                 ActionEntry ae = actions.get(Server.rand.nextInt(actions.size()));
                                 if (ae.getNumber() > 0) {
                                    BehaviourDispatcher.action(this, this.communicator, rand == null ? -10L : rand.getWurmId(), targTile, ae.getNumber());
                                 }
                              }
                           }

                           this.setPassiveCounter(30);
                        } catch (Exception var17) {
                        }
                     }

                     this.setPassiveCounter(10);
                  }

                  if (seed < 80) {
                     Creature[] crets = null;

                     for(int x = -2; x <= 2; ++x) {
                        for(int y = -2; y <= 2; ++y) {
                           VolaTile t = Zones.getTileOrNull(Zones.safeTileX(this.getTileX() + x), Zones.safeTileY(this.getTileY() + y), this.isOnSurface());
                           if (t != null) {
                              crets = t.getCreatures();
                              if (crets.length > 0) {
                                 try {
                                    Creature targC = crets[Server.rand.nextInt(crets.length)];
                                    Behaviour behaviour = Action.getBehaviour(targC.getWurmId(), this.isOnSurface());
                                    BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForCreaturesPlayers(
                                       this, targC.getWurmId(), null, targC.isPlayer() ? 0 : 1, behaviour
                                    );
                                    List<ActionEntry> actions = param.getAvailableActions();
                                    if (actions.size() > 0) {
                                       ActionEntry ae = actions.get(Server.rand.nextInt(actions.size()));
                                       if ((!ae.isOffensive() || !this.isFriendlyKingdom(targC.getKingdomId())) && ae.getNumber() > 0) {
                                          BehaviourDispatcher.action(this, this.communicator, -10L, targC.getWurmId(), ae.getNumber());
                                       }
                                       break;
                                    }
                                 } catch (Exception var16) {
                                 }
                              }
                           }
                        }
                     }
                  }

                  try {
                     Item[] allItems = this.getInventory().getAllItems(false);
                     if (allItems.length > 2) {
                        Item rand1 = allItems[Server.rand.nextInt(allItems.length)];
                        Item rand2 = allItems[Server.rand.nextInt(allItems.length)];
                        Behaviour behaviour = Action.getBehaviour(rand2.getWurmId(), this.isOnSurface());
                        BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForItemsBodyIdsCoinIds(
                           this, rand2.getWurmId(), rand1, behaviour
                        );
                        List<ActionEntry> actions = param.getAvailableActions();
                        if (actions.size() > 0) {
                           ActionEntry ae = actions.get(Server.rand.nextInt(actions.size()));
                           if (ae.getNumber() > 0) {
                              BehaviourDispatcher.action(this, this.communicator, rand1 == null ? -10L : rand1.getWurmId(), rand2.getWurmId(), ae.getNumber());
                           }
                        }

                        this.setPassiveCounter(30);
                     }
                  } catch (Exception var14) {
                  }
               }
            } else {
               this.setPassiveCounter(180);
            }
         } else {
            this.setPassiveCounter(30);
         }
      }
   }

   private void clearLongTarget() {
      this.longTarget = null;
      this.longTargetAttempts = 0;
   }

   public boolean isOnLongTargetTile() {
      if (this.getStatus() == null) {
         return false;
      } else {
         return this.longTarget.getTileX() == (int)this.status.getPositionX() >> 2 && this.longTarget.getTileY() == (int)this.status.getPositionY() >> 2;
      }
   }

   @Override
   public final Path findPath(int targetX, int targetY, @Nullable PathFinder pathfinder) throws NoPathException {
      Path path = null;
      PathFinder pf = pathfinder != null ? pathfinder : new PathFinder();
      this.setPathfindcounter(this.getPathfindCounter() + 1);
      if (this.getPathfindCounter() >= 10 && this.target == -10L && this.getPower() <= 0) {
         throw new NoPathException("No pathing now");
      } else {
         path = pf.findPath(this, this.getTileX(), this.getTileY(), targetX, targetY, this.isOnSurface(), 20);
         if (path != null) {
            this.setPathfindcounter(0);
         }

         return path;
      }
   }

   private final boolean capturePillar() {
      if (this.getCitizenVillage() != null) {
         FocusZone hota = FocusZone.getHotaZone();
         if (hota != null && hota.covers(this.getTileX(), this.getTileY())) {
            for(Item i : this.getCurrentTile().getItems()) {
               if (i.getTemplateId() == 739 && i.getData1() != this.getCitizenVillage().getId()) {
                  try {
                     BehaviourDispatcher.action(this, this.communicator, -10L, i.getWurmId(), (short)504);
                  } catch (Exception var7) {
                  }

                  return true;
               }
            }
         }
      }

      return false;
   }

   private final boolean performLongTargetAction() {
      if (this.longTarget != null && this.longTarget.getMissionTrigger() > 0) {
         MissionTrigger trigger = MissionTriggers.getTriggerWithId(this.longTarget.getMissionTrigger());
         if (trigger != null && Math.abs(this.longTarget.getTileX() - this.getTileX()) < 3 && Math.abs(this.longTarget.getTileY() - this.getTileY()) < 3) {
            Item found = null;
            if (trigger.getItemUsedId() > 0) {
               if (trigger.getOnActionPerformed() == 148) {
                  CreationEntry ce = CreationMatrix.getInstance().getCreationEntry(trigger.getItemUsedId());
                  if (ce != null && !ce.isAdvanced()) {
                     try {
                        found = ItemFactory.createItem(trigger.getItemUsedId(), 20.0F + Server.rand.nextFloat() * 20.0F, this.getName());
                        this.getInventory().insertItem(found, true);
                        if (found.getWeightGrams() > 20000) {
                           found.putItemInfrontof(this);
                        } else {
                           this.wearItems();
                        }

                        MissionTriggers.activateTriggers(this, found, 148, 0L, 1);
                        this.clearLongTarget();
                        return true;
                     } catch (Exception var15) {
                     }
                  }
               }

               for(Item item : this.getAllItems()) {
                  if (item.getTemplateId() == trigger.getItemUsedId()) {
                     found = item;
                  }
               }

               if (found == null) {
                  try {
                     found = ItemFactory.createItem(trigger.getItemUsedId(), 20.0F + Server.rand.nextFloat() * 20.0F, this.getName());
                     this.getInventory().insertItem(found, true);
                  } catch (Exception var12) {
                  }
               }
            }

            if (WurmId.getType(trigger.getTarget()) == 1 || WurmId.getType(trigger.getTarget()) == 0) {
               try {
                  Creature c = Server.getInstance().getCreature(trigger.getTarget());
                  if (c == null || c.isDead()) {
                     this.clearLongTarget();
                     return true;
                  }
               } catch (NoSuchCreatureException var13) {
                  this.clearLongTarget();
                  return true;
               } catch (NoSuchPlayerException var14) {
                  this.clearLongTarget();
                  return true;
               }
            }

            if (WurmId.getType(trigger.getTarget()) == 3 && trigger.getOnActionPerformed() == 492) {
               int tilenum = Server.surfaceMesh.getTile(this.getTileX(), this.getTileY());
               if (!Tiles.isTree(Tiles.decodeType(tilenum))) {
                  return true;
               }

               if (found == null) {
                  for(Item axe : this.getBody().getBodyItem().getAllItems(false)) {
                     if (axe.isWeaponAxe() || axe.isWeaponSlash()) {
                        found = axe;
                     }
                  }
               }

               if (found == null) {
                  for(Item axe : this.getInventory().getAllItems(false)) {
                     if (axe.isWeaponAxe() || axe.isWeaponSlash()) {
                        found = axe;
                     }
                  }
               }

               if (found == null) {
                  try {
                     found = ItemFactory.createItem(7, 10.0F, this.getName());
                  } catch (Exception var11) {
                  }
               }

               if (found != null && found.isWeaponAxe() || found.isWeaponSlash()) {
                  try {
                     BehaviourDispatcher.action(this, this.communicator, found.getWurmId(), trigger.getTarget(), (short)96);
                  } catch (Exception var10) {
                  }
               }
            }

            MissionTriggers.activateTriggers(this, found, trigger.getOnActionPerformed(), trigger.getTarget(), 1);
            this.clearLongTarget();
            return true;
         }
      } else if (this.longTarget != null && this.isOnLongTargetTile()) {
         Item[] currentItems = this.getCurrentTile().getItems();

         for(Item current : currentItems) {
            if (current.isCorpse() && current.getLastOwnerId() == this.getWurmId()) {
               for(Item incorpse : current.getAllItems(false)) {
                  if (!incorpse.isBodyPart()) {
                     this.getInventory().insertItem(incorpse);
                  }
               }

               this.wearItems();
               Items.destroyItem(current.getWurmId());
            }
         }

         this.clearLongTarget();
         return true;
      }

      return false;
   }

   @Override
   public final PathTile getMoveTarget(int seed) {
      if (this.getStatus() == null) {
         return null;
      } else {
         float lPosX = this.status.getPositionX();
         float lPosY = this.status.getPositionY();
         boolean hasTarget = false;
         int tilePosX = (int)lPosX >> 2;
         int tilePosY = (int)lPosY >> 2;
         int tx = tilePosX;
         int ty = tilePosY;
         if (!this.isAggHuman() && this.getCitizenVillage() != null) {
            if (this.longTarget == null) {
               if (Server.rand.nextInt(100) == 0) {
                  Player[] players = Players.getInstance().getPlayers();

                  for(int x = 0; x < 10; ++x) {
                     Player p = players[Server.rand.nextInt(players.length)];
                     if (p.isWithinDistanceTo(this, 200.0F) && p.getPower() == 0) {
                        int tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
                        if (!p.isOnSurface()) {
                           tile = Server.caveMesh.getTile(tilePosX, tilePosY);
                        }

                        this.longTarget = new LongTarget(p.getTileX(), p.getTileY(), tile, p.isOnSurface(), p.getFloorLevel(), this);
                        if (p.isFriendlyKingdom(this.getKingdomId())) {
                           this.getChatManager().createAndSendMessage(p, "Oi.", false);
                        } else {
                           this.getChatManager().createAndSendMessage(p, "Coming for you.", false);
                        }
                        break;
                     }
                  }
               }

               if (this.longTarget == null && Server.rand.nextInt(10) == 0) {
                  Item[] allIts = Items.getAllItems();

                  for(Item corpse : allIts) {
                     if (corpse.getZoneId() > 0
                        && corpse.getTemplateId() == 272
                        && corpse.getLastOwnerId() == this.getWurmId()
                        && corpse.getName().toLowerCase().contains(this.getName().toLowerCase())) {
                        Item[] contained = corpse.getAllItems(false);
                        if (contained.length > 4) {
                           boolean surf = corpse.isOnSurface();
                           int tile = Server.surfaceMesh.getTile(corpse.getTileX(), corpse.getTileY());
                           if (!surf) {
                              tile = Server.caveMesh.getTile(corpse.getTileX(), corpse.getTileY());
                           }

                           this.longTarget = new LongTarget(corpse.getTileX(), corpse.getTileY(), tile, surf, surf ? 0 : -1, this);
                        }
                     }
                  }
               }

               if (this.longTarget == null && Server.rand.nextInt(10) == 0) {
                  EpicMission[] ems = EpicServerStatus.getCurrentEpicMissions();

                  for(EpicMission em : ems) {
                     if (em.isCurrent()) {
                        Deity deity = Deities.getDeity(em.getEpicEntityId());
                        if (deity != null && deity.getFavoredKingdom() == this.getKingdomId()) {
                           for(MissionTrigger trig : MissionTriggers.getAllTriggers()) {
                              if (trig.getMissionRequired() == em.getMissionId()) {
                                 long target = trig.getTarget();
                                 if (WurmId.getType(target) == 3 || WurmId.getType(target) == 17) {
                                    int x2 = Tiles.decodeTileX(target);
                                    int y2 = Tiles.decodeTileY(target);
                                    boolean surf = WurmId.getType(target) == 3;
                                    int tile = Server.surfaceMesh.getTile(x2, y2);
                                    if (!surf) {
                                       tile = Server.caveMesh.getTile(x2, y2);
                                    }

                                    this.longTarget = new LongTarget(x2, y2, tile, surf, surf ? 0 : -1, this);
                                 } else if (WurmId.getType(target) == 2) {
                                    try {
                                       Item i = Items.getItem(target);
                                       int tile = Server.surfaceMesh.getTile(i.getTileX(), i.getTileY());
                                       if (!i.isOnSurface()) {
                                          tile = Server.caveMesh.getTile(i.getTileX(), i.getTileY());
                                       }

                                       this.longTarget = new LongTarget(i.getTileX(), i.getTileY(), tile, i.isOnSurface(), i.getFloorLevel(), this);
                                    } catch (NoSuchItemException var27) {
                                    }
                                 } else if (WurmId.getType(target) == 1 || WurmId.getType(target) == 0) {
                                    try {
                                       Creature c = Server.getInstance().getCreature(target);
                                       int tile = Server.surfaceMesh.getTile(c.getTileX(), c.getTileY());
                                       if (!c.isOnSurface()) {
                                          tile = Server.caveMesh.getTile(c.getTileX(), c.getTileY());
                                       }

                                       this.longTarget = new LongTarget(c.getTileX(), c.getTileY(), tile, c.isOnSurface(), c.getFloorLevel(), this);
                                    } catch (NoSuchCreatureException var25) {
                                    } catch (NoSuchPlayerException var26) {
                                    }
                                 } else if (WurmId.getType(target) == 5) {
                                    int x = (int)(target >> 32) & 65535;
                                    int y = (int)(target >> 16) & 65535;
                                    Wall wall = Wall.getWall(target);
                                    if (wall != null) {
                                       int tile = Server.surfaceMesh.getTile(x, y);
                                       this.longTarget = new LongTarget(x, y, tile, true, wall.getFloorLevel(), this);
                                    }
                                 }

                                 if (this.longTarget != null) {
                                    this.longTarget.setMissionTrigger(trig.getId());
                                    this.longTarget.setEpicMission(em.getMissionId());
                                    this.longTarget.setMissionTarget(target);
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (this.longTarget == null && Server.rand.nextInt(10) == 0) {
                  if (this.getCitizenVillage() != null) {
                     if (getTileRange(this, this.getCitizenVillage().getTokenX(), this.getCitizenVillage().getTokenY()) > 300.0) {
                        int tile = Server.surfaceMesh.getTile(this.getCitizenVillage().getTokenX(), this.getCitizenVillage().getTokenY());
                        this.longTarget = new LongTarget(this.getCitizenVillage().getTokenX(), this.getCitizenVillage().getTokenY(), tile, true, 0, this);
                     }
                  } else {
                     for(Village v : Villages.getVillages()) {
                        if (v.isPermanent && v.kingdom == this.getKingdomId() && getTileRange(this, v.getTokenX(), v.getTokenY()) > 300.0) {
                           int tile = Server.surfaceMesh.getTile(v.getTokenX(), v.getTokenY());
                           this.longTarget = new LongTarget(v.getTokenX(), v.getTokenY(), tile, true, 0, this);
                        }
                     }
                  }

                  if (this.longTarget != null) {
                     int seedh = Server.rand.nextInt(5);
                     String mess = "Think I'll head home again...";
                     switch(seedh) {
                        case 0:
                           mess = "Time to go home!";
                           break;
                        case 1:
                           mess = "Enough of this. Home Sweet Home.";
                           break;
                        case 2:
                           mess = "Heading home. Are you coming?";
                           break;
                        case 3:
                           mess = "I will go home now.";
                           break;
                        case 4:
                           mess = "That's it. I'm going home.";
                           break;
                        default:
                           mess = "Think I'll go home for a while.";
                     }

                     if (this.getCurrentTile() != null) {
                        Message m = new Message(this, (byte)0, ":Local", "<" + this.getName() + "> " + mess);
                        this.getCurrentTile().broadCastMessage(m);
                     }
                  }
               }

               if (this.longTarget == null && Server.rand.nextInt(100) == 0 && this.getCitizenVillage() != null) {
                  FocusZone hota = FocusZone.getHotaZone();
                  if (hota != null && !hota.covers(this.getTileX(), this.getTileY())) {
                     int hx = hota.getStartX() + Server.rand.nextInt(hota.getEndX() - hota.getStartX());
                     int hy = hota.getStartY() + Server.rand.nextInt(hota.getEndY() - hota.getStartY());
                     int tile = Server.surfaceMesh.getTile(hx, hy);
                     this.longTarget = new LongTarget(hx, hy, tile, true, 0, this);
                     int seedh = Server.rand.nextInt(5);
                     String mess = "Think I'll go hunt for some pillars a bit...";
                     switch(seedh) {
                        case 0:
                           mess = "Anyone in the Hunt of the Ancients is in trouble now!";
                           break;
                        case 1:
                           mess = "Going to check out what happens in the Hunt.";
                           break;
                        case 2:
                           mess = "Heading to join the Hunt. Coming with me?";
                           break;
                        case 3:
                           mess = "Going to head to the Hunt of the Ancients. You interested?";
                           break;
                        case 4:
                           mess = "I want to do some gloryhunting in the HOTA.";
                           break;
                        default:
                           mess = "Think I'll go join the hunt a bit...";
                     }

                     if (this.getCurrentTile() != null) {
                        Message m = new Message(this, (byte)0, ":Local", "<" + this.getName() + "> " + mess);
                        this.getCurrentTile().broadCastMessage(m);
                     }
                  }
               }

               if (this.longTarget != null) {
                  return this.longTarget;
               }
            } else {
               boolean clear = false;
               if (this.longTarget.getCreatureTarget() != null && this.longTarget.getTileX() != this.longTarget.getCreatureTarget().getTileX()) {
                  this.longTarget.setTileX(this.longTarget.getCreatureTarget().getTileX());
               }

               if (this.longTarget.getCreatureTarget() != null && this.longTarget.getTileY() != this.longTarget.getCreatureTarget().getTileY()) {
                  this.longTarget.setTileY(this.longTarget.getCreatureTarget().getTileY());
               }

               if (this.longTarget.getEpicMission() > 0) {
                  EpicMission em = EpicServerStatus.getEpicMissionForMission(this.longTarget.getEpicMission());
                  if (em == null || !em.isCurrent() || em.isCompleted()) {
                     clear = true;
                  }
               }

               if (Math.abs(this.longTarget.getTileX() - tilePosX) < 20 && Math.abs(this.longTarget.getTileY() - tilePosY) < 20) {
                  if (Math.abs(this.longTarget.getTileX() - tilePosX) < 10
                     && Math.abs(this.longTarget.getTileY() - tilePosY) < 10
                     && this.longTarget.getCreatureTarget() != null
                     && !this.longTarget.getCreatureTarget().isFriendlyKingdom(this.getKingdomId())) {
                     this.setTarget(this.longTarget.getCreatureTarget().getWurmId(), false);
                     clear = true;
                  }

                  if (!this.isOnLongTargetTile() && this.longTargetAttempts++ <= 50) {
                     return this.longTarget;
                  }

                  clear = true;
               } else if (System.currentTimeMillis() - this.longTarget.getStartTime() > 3600000L) {
                  clear = true;
               }

               if (clear) {
                  this.clearLongTarget();
               }
            }
         }

         boolean flee = false;
         if ((this.target == -10L || this.fleeCounter > 0) && (this.isTypeFleeing() || this.fleeCounter > 0) && this.isOnSurface()) {
            if (Server.rand.nextBoolean()) {
               if (this.getCurrentTile() != null && this.getCurrentTile().getVillage() != null) {
                  Long[] crets = this.getVisionArea().getSurface().getCreatures();

                  for(Long lCret : crets) {
                     try {
                        Creature cret = Server.getInstance().getCreature(lCret);
                        if (cret.getPower() == 0 && (cret.isPlayer() || cret.isAggHuman() || cret.isCarnivore() || cret.isMonster())) {
                           if (cret.getPosX() > this.getPosX()) {
                              tilePosX -= Server.rand.nextInt(6);
                           } else {
                              tilePosX += Server.rand.nextInt(6);
                           }

                           if (cret.getPosY() > this.getPosY()) {
                              tilePosY -= Server.rand.nextInt(6);
                           } else {
                              tilePosY += Server.rand.nextInt(6);
                           }

                           flee = true;
                           break;
                        }
                     } catch (Exception var28) {
                     }
                  }
               }
            } else {
               for(Player p : Players.getInstance().getPlayers()) {
                  if ((p.getPower() == 0 || Servers.localServer.testServer)
                     && p.getVisionArea() != null
                     && p.getVisionArea().getSurface() != null
                     && p.getVisionArea().getSurface().containsCreature(this)) {
                     if (p.getPosX() > this.getPosX()) {
                        tilePosX -= Server.rand.nextInt(6);
                     } else {
                        tilePosX += Server.rand.nextInt(6);
                     }

                     if (p.getPosY() > this.getPosY()) {
                        tilePosY -= Server.rand.nextInt(6);
                     } else {
                        tilePosY += Server.rand.nextInt(6);
                     }

                     flee = true;
                     break;
                  }
               }
            }
         }

         if (!flee && !hasTarget) {
            VolaTile currTile = this.getCurrentTile();
            if (currTile != null) {
               int rand = Server.rand.nextInt(9);
               int tpx = currTile.getTileX() + 4 - rand;
               rand = Server.rand.nextInt(9);
               int tpy = currTile.getTileY() + 4 - rand;
               totx += currTile.getTileX() - tpx;
               toty += currTile.getTileY() - tpy;
               if (this.longTarget != null) {
                  if (Math.abs(this.longTarget.getTileX() - this.getTileX()) < 20) {
                     tpx = this.longTarget.getTileX();
                  } else {
                     tpx = this.getTileX() + 5 + Server.rand.nextInt(6);
                     if (this.getTileX() > this.longTarget.getTileX()) {
                        tpx = this.getTileX() - 5 - Server.rand.nextInt(6);
                     }
                  }

                  if (Math.abs(this.longTarget.getTileY() - this.getTileY()) < 20) {
                     tpy = this.longTarget.getTileY();
                  } else {
                     tpy = this.getTileY() + 5 + Server.rand.nextInt(6);
                     if (this.getTileY() > this.longTarget.getTileY()) {
                        tpy = this.getTileY() - 5 - Server.rand.nextInt(6);
                     }
                  }
               } else if (this.getCitizenVillage() != null) {
                  FocusZone hota = FocusZone.getHotaZone();
                  if (hota != null && hota.covers(this.getTileX(), this.getTileY())) {
                     for(Item pillar : Hota.getHotaItems()) {
                        if (pillar.getTemplateId() == 739
                           && pillar.getZoneId() > 0
                           && pillar.getData1() != this.getCitizenVillage().getId()
                           && getTileRange(this, pillar.getTileX(), pillar.getTileY()) < 20.0) {
                           tpx = pillar.getTileX();
                           tpy = pillar.getTileY();
                        }
                     }
                  }
               }

               tpx = Zones.safeTileX(tpx);
               tpy = Zones.safeTileY(tpy);
               VolaTile t = Zones.getOrCreateTile(tpx, tpy, this.isOnSurface());
               if (this.isOnSurface()) {
                  boolean stepOnBridge = false;
                  if (Server.rand.nextInt(5) == 0) {
                     for(VolaTile stile : this.currentTile.getThisAndSurroundingTiles(1)) {
                        if (stile.getStructure() != null && stile.getStructure().isTypeBridge()) {
                           if (stile.getStructure().isHorizontal()) {
                              if ((stile.getStructure().getMaxX() == stile.getTileX() || stile.getStructure().getMinX() == stile.getTileX())
                                 && this.getTileY() == stile.getTileY()) {
                                 tilePosX = stile.getTileX();
                                 tilePosY = stile.getTileY();
                                 stepOnBridge = true;
                                 break;
                              }
                           } else if ((stile.getStructure().getMaxY() == stile.getTileY() || stile.getStructure().getMinY() == stile.getTileY())
                              && this.getTileX() == stile.getTileX()) {
                              tilePosX = stile.getTileX();
                              tilePosY = stile.getTileY();
                              stepOnBridge = true;
                              break;
                           }
                        }
                     }
                  }

                  if (!stepOnBridge && (t == null || t.getCreatures().length < 3)) {
                     tilePosX = tpx;
                     tilePosY = tpy;
                  }
               } else if (t == null || t.getCreatures().length < 3) {
                  tilePosX = tpx;
                  tilePosY = tpy;
               }
            }
         }

         Creature targ = this.getTarget();
         if (targ != null) {
            if (targ.getCultist() != null && targ.getCultist().hasFearEffect()) {
               this.setTarget(-10L, true);
            }

            VolaTile currTile = targ.getCurrentTile();
            if (currTile != null) {
               tilePosX = currTile.tilex;
               tilePosY = currTile.tiley;
               if (seed == 100) {
                  tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                  tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
               }

               int targGroup = targ.getGroupSize();
               int myGroup = this.getGroupSize();
               if (this.isOnSurface() != currTile.isOnSurface()) {
                  boolean changeLayer = false;
                  if (this.getCurrentTile().isTransition) {
                     changeLayer = true;
                  }

                  VolaTile t = this.getCurrentTile();
                  if ((this.isAggHuman() || this.isHunter() || this.isDominated())
                     && (!currTile.isGuarded() || t != null && t.isGuarded())
                     && this.isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) {
                     if (!changeLayer) {
                        int[] tiles = new int[]{tilePosX, tilePosY};
                        if (this.isOnSurface()) {
                           tiles = this.findRandomCaveEntrance(tiles);
                        } else {
                           tiles = this.findRandomCaveExit(tiles);
                        }

                        tilePosX = tiles[0];
                        tilePosY = tiles[1];
                     }
                  } else {
                     this.setTarget(-10L, true);
                  }

                  if (changeLayer
                     && (!Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty))) || MineDoorPermission.getPermission(tx, ty).mayPass(this))) {
                     this.setLayer(this.isOnSurface() ? -1 : 0, true);
                  }
               }

               if (targ.getCultist() != null && targ.getCultist().hasFearEffect()) {
                  if (Server.rand.nextBoolean()) {
                     tilePosX = Math.max(currTile.getTileX() + 10, this.getTileX());
                  } else {
                     tilePosX = Math.min(currTile.getTileX() - 10, this.getTileX());
                  }

                  if (Server.rand.nextBoolean()) {
                     tilePosX = Math.max(currTile.getTileY() + 10, this.getTileY());
                  } else {
                     tilePosX = Math.min(currTile.getTileY() - 10, this.getTileY());
                  }
               } else {
                  VolaTile t = this.getCurrentTile();
                  if (targGroup <= myGroup * this.getMaxGroupAttackSize()
                     && (this.isAggHuman() || this.isHunter())
                     && (!currTile.isGuarded() || t != null && t.isGuarded())) {
                     if (this.isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) {
                        if (targ.getKingdomId() == 0
                           || this.isFriendlyKingdom(targ.getKingdomId())
                           || !this.isDefendKingdom() && (!this.isAggWhitie() || targ.getKingdomTemplateId() == 3)) {
                           if (seed == 100) {
                              tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                              tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                           } else {
                              tilePosX = currTile.getTileX();
                              tilePosY = currTile.getTileY();
                              if (this.getSize() < 5 && targ.getBridgeId() != -10L && this.getBridgeId() < 0L) {
                                 int[] tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), targ.getBridgeId());
                                 if (tiles[0] > 0) {
                                    tilePosX = tiles[0];
                                    tilePosY = tiles[1];
                                    if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                       tilePosX = currTile.tilex;
                                       tilePosY = currTile.tiley;
                                    }
                                 }
                              } else if (this.getBridgeId() != targ.getBridgeId()) {
                                 int[] tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), this.getBridgeId());
                                 if (tiles[0] > 0) {
                                    tilePosX = tiles[0];
                                    tilePosY = tiles[1];
                                    if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                       tilePosX = currTile.tilex;
                                       tilePosY = currTile.tiley;
                                    }
                                 }
                              }
                           }
                        } else if (!this.isFighting()) {
                           if (seed == 100) {
                              tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                              tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                           } else {
                              tilePosX = currTile.getTileX();
                              tilePosY = currTile.getTileY();
                              this.setTarget(targ.getWurmId(), false);
                           }
                        }
                     } else if (!this.isFighting()) {
                        this.setTarget(-10L, true);
                     }
                  } else if (!this.isFighting()) {
                     this.setTarget(-10L, true);
                  }
               }
            }
         }

         if (tilePosX == tx && tilePosY == ty) {
            return null;
         } else {
            tilePosX = Zones.safeTileX(tilePosX);
            tilePosY = Zones.safeTileY(tilePosY);
            if (!this.isOnSurface()) {
               int tile = Server.caveMesh.getTile(tilePosX, tilePosY);
               if (!Tiles.isSolidCave(Tiles.decodeType(tile))
                  && (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters() || this.isSwimming() || this.isSubmerged())) {
                  return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), -1);
               }
            } else {
               int tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
               if (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters() || this.isSwimming() || this.isSubmerged()) {
                  return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), this.getFloorLevel());
               }
            }

            this.setTarget(-10L, true);
            if (this.isDominated() && this.hasOrders()) {
               this.removeOrder(this.getFirstOrder());
            }

            return null;
         }
      }
   }

   private final void setPassiveCounter(int counter) {
      this.passiveCounter = counter;
   }

   private final void checkItemSpawn() {
      if (this.lastX == 0) {
         this.lastX = this.getTileX();
      }

      if (this.lastY == 0) {
         this.lastY = this.getTileY();
      }

      if (this.lastX - this.getTileX() > 50 || this.lastY - this.getTileY() > 50) {
         this.lastX = this.getTileX();
         this.lastY = this.getTileY();
         if (Server.rand.nextInt(10) == 0 && this.getBody().getContainersAndWornItems().length < 10) {
            try {
               int templateId = Server.rand.nextInt(1437);
               ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
               if ((template.isArmour() || template.isWeapon()) && !template.isRoyal && !template.artifact) {
                  try {
                     Item toInsert = ItemFactory.createItem(templateId, Server.rand.nextFloat() * 80.0F + 20.0F, this.getName());
                     this.getInventory().insertItem(toInsert, true);
                     this.wearItems();
                     if (toInsert.getParentId() == this.getInventory().getWurmId()) {
                        Items.destroyItem(toInsert.getWurmId());
                     }
                  } catch (FailedException var4) {
                  }
               }
            } catch (NoSuchTemplateException var5) {
            }
         }
      }
   }

   @Override
   public final boolean isMoveLocal() {
      return this.hasTrait(8) ? true : this.template.isMoveLocal();
   }

   @Override
   public final boolean isSentinel() {
      return this.hasTrait(9) ? true : this.template.isSentinel();
   }

   @Override
   public final boolean isMoveGlobal() {
      return this.hasTrait(1) ? true : this.template.isMoveGlobal();
   }

   @Override
   public boolean isNpc() {
      return true;
   }

   @Override
   public long getFace() {
      faceRandom.setSeed(this.getWurmId());
      return faceRandom.nextLong();
   }

   @Override
   public float getSpeed() {
      return this.getVehicle() > -10L && WurmId.getType(this.getVehicle()) == 1 ? 1.7F : 1.1F;
   }

   @Override
   public boolean isTypeFleeing() {
      return this.getStatus().modtype == 10 || this.getStatus().damage > 45000;
   }

   @Override
   public boolean isRespawn() {
      return !this.hasTrait(19);
   }

   @Override
   public final boolean isDominatable(Creature aDominator) {
      if (this.getLeader() != null && this.getLeader() != aDominator) {
         return false;
      } else {
         return !this.isRidden() && this.hitchedTo == null ? this.hasTrait(22) : false;
      }
   }

   @Override
   public final float getBaseCombatRating() {
      double fskill = 1.0;

      try {
         fskill = this.skills.getSkill(1023).getKnowledge();
      } catch (NoSuchSkillException var4) {
         this.skills.learn(1023, 1.0F);
         fskill = 1.0;
      }

      return this.getLoyalty() > 0.0F
         ? (float)Math.max(1.0, (double)(this.isReborn() ? 0.7F : 0.5F) * fskill / 5.0 * (double)this.status.getBattleRatingTypeModifier())
            * Servers.localServer.getCombatRatingModifier()
         : (float)Math.max(1.0, fskill / 5.0 * (double)this.status.getBattleRatingTypeModifier()) * Servers.localServer.getCombatRatingModifier();
   }
}
