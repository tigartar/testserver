package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MeshTile;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.LinkedList;
import java.util.List;

final class TileBorderBehaviour extends Behaviour implements MiscConstants {
   TileBorderBehaviour() {
      super((short)32);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(
      Creature performer, Item subject, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, boolean border, int heightOffset
   ) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (hasHoleEachSide(tilex, tiley, onSurface, dir)) {
         return toReturn;
      } else {
         toReturn.addAll(super.getBehavioursFor(performer, subject, tilex, tiley, onSurface, dir, border, heightOffset));
         toReturn.add(Actions.actionEntrys[607]);
         int templateId = subject.getTemplateId();
         if (onSurface && (subject.isMineDoor() || subject.getTemplateId() == 315 || subject.getTemplateId() == 176)) {
            int[] opening = Terraforming.getCaveOpeningCoords(tilex, tiley);
            if (opening[0] != -1 && opening[1] != -1 && !isWideEntrance(opening[0], opening[1])) {
               toReturn.add(Actions.actionEntrys[363]);
            }
         }

         boolean hasMarker = hasMarker(tilex, tiley, onSurface, dir);
         if (!hasMarker && MethodsStructure.isCorrectToolForBuilding(performer, templateId)) {
            boolean ok = onSurface;
            if (!onSurface) {
               VolaTile vt = Zones.getOrCreateTile(tilex, tiley, onSurface);
               if (vt.getVillage() != null) {
                  ok = true;
               }
            }

            if (ok) {
               toReturn.add(new ActionEntry((short)-12, "Fence", "Fence options"));
               toReturn.add(new ActionEntry((short)-5, "Iron", "Fence options"));
               toReturn.add(Actions.actionEntrys[611]);
               toReturn.add(Actions.actionEntrys[477]);
               toReturn.add(Actions.actionEntrys[479]);
               toReturn.add(Actions.actionEntrys[545]);
               toReturn.add(Actions.actionEntrys[546]);
               toReturn.add(new ActionEntry((short)-2, "Log", "Fence options"));
               toReturn.add(Actions.actionEntrys[165]);
               toReturn.add(Actions.actionEntrys[167]);
               toReturn.add(new ActionEntry((short)-8, "Marble", "Fence options"));
               toReturn.add(Actions.actionEntrys[844]);
               toReturn.add(Actions.actionEntrys[845]);
               toReturn.add(Actions.actionEntrys[846]);
               toReturn.add(Actions.actionEntrys[900]);
               toReturn.add(Actions.actionEntrys[901]);
               toReturn.add(Actions.actionEntrys[902]);
               toReturn.add(Actions.actionEntrys[903]);
               toReturn.add(Actions.actionEntrys[905]);
               toReturn.add(new ActionEntry((short)-4, "Plank", "Fence options"));
               toReturn.add(Actions.actionEntrys[520]);
               toReturn.add(Actions.actionEntrys[528]);
               toReturn.add(Actions.actionEntrys[166]);
               toReturn.add(Actions.actionEntrys[168]);
               toReturn.add(new ActionEntry((short)-8, "Pottery", "Fence options"));
               toReturn.add(Actions.actionEntrys[838]);
               toReturn.add(Actions.actionEntrys[839]);
               toReturn.add(Actions.actionEntrys[840]);
               toReturn.add(Actions.actionEntrys[894]);
               toReturn.add(Actions.actionEntrys[895]);
               toReturn.add(Actions.actionEntrys[896]);
               toReturn.add(Actions.actionEntrys[897]);
               toReturn.add(Actions.actionEntrys[899]);
               toReturn.add(new ActionEntry((short)-2, "Rope", "Rope options"));
               toReturn.add(Actions.actionEntrys[544]);
               toReturn.add(Actions.actionEntrys[543]);
               toReturn.add(new ActionEntry((short)-8, "Rounded stone", "Fence options"));
               toReturn.add(Actions.actionEntrys[835]);
               toReturn.add(Actions.actionEntrys[836]);
               toReturn.add(Actions.actionEntrys[837]);
               toReturn.add(Actions.actionEntrys[876]);
               toReturn.add(Actions.actionEntrys[877]);
               toReturn.add(Actions.actionEntrys[878]);
               toReturn.add(Actions.actionEntrys[879]);
               toReturn.add(Actions.actionEntrys[881]);
               toReturn.add(new ActionEntry((short)-8, "Sandstone", "Fence options"));
               toReturn.add(Actions.actionEntrys[841]);
               toReturn.add(Actions.actionEntrys[842]);
               toReturn.add(Actions.actionEntrys[843]);
               toReturn.add(Actions.actionEntrys[882]);
               toReturn.add(Actions.actionEntrys[883]);
               toReturn.add(Actions.actionEntrys[884]);
               toReturn.add(Actions.actionEntrys[885]);
               toReturn.add(Actions.actionEntrys[887]);
               toReturn.add(new ActionEntry((short)-3, "Shaft", "Fence options"));
               toReturn.add(Actions.actionEntrys[527]);
               toReturn.add(Actions.actionEntrys[526]);
               toReturn.add(Actions.actionEntrys[529]);
               toReturn.add(new ActionEntry((short)-8, "Slate", "Fence options"));
               toReturn.add(Actions.actionEntrys[832]);
               toReturn.add(Actions.actionEntrys[833]);
               toReturn.add(Actions.actionEntrys[834]);
               toReturn.add(Actions.actionEntrys[870]);
               toReturn.add(Actions.actionEntrys[871]);
               toReturn.add(Actions.actionEntrys[872]);
               toReturn.add(Actions.actionEntrys[873]);
               toReturn.add(Actions.actionEntrys[875]);
               toReturn.add(new ActionEntry((short)-5, "Stone", "Fence options"));
               toReturn.add(Actions.actionEntrys[542]);
               toReturn.add(Actions.actionEntrys[163]);
               toReturn.add(Actions.actionEntrys[654]);
               toReturn.add(Actions.actionEntrys[541]);
               toReturn.add(Actions.actionEntrys[164]);
               toReturn.add(new ActionEntry((short)-1, "Woven", "Fence options"));
               toReturn.add(Actions.actionEntrys[478]);
            }
         }

         if (onSurface && subject.getTemplateId() == 266) {
            if (!hasMarker) {
               toReturn.add(Actions.actionEntrys[186]);
            }
         } else if (onSurface && subject.isTrellis()) {
            toReturn.add(new ActionEntry((short)-3, "Plant", "Plant options"));
            toReturn.add(Actions.actionEntrys[746]);
            toReturn.add(new ActionEntry((short)176, "In center", "planting"));
            toReturn.add(Actions.actionEntrys[747]);
         } else if (onSurface && subject.isFlower()) {
            if (!hasMarker) {
               toReturn.add(Actions.actionEntrys[563]);
            }
         } else if (onSurface && subject.isDiggingtool()) {
            toReturn.add(Actions.actionEntrys[533]);
            toReturn.add(Actions.actionEntrys[865]);
         }

         if (subject.isMagicStaff() || templateId == 176 && performer.getPower() >= 4 && Servers.isThisATestServer()) {
            List<ActionEntry> slist = new LinkedList<>();
            if (performer.knowsKarmaSpell(556)) {
               slist.add(Actions.actionEntrys[556]);
            }

            if (performer.knowsKarmaSpell(557)) {
               slist.add(Actions.actionEntrys[557]);
            }

            if (performer.knowsKarmaSpell(558)) {
               slist.add(Actions.actionEntrys[558]);
            }

            if (performer.getPower() >= 4) {
               toReturn.add(new ActionEntry((short)(-slist.size()), "Sorcery", "casting"));
            }

            toReturn.addAll(slist);
         }

         if (onSurface && (templateId == 176 || templateId == 315) && performer.getPower() >= 2) {
            toReturn.add(Actions.actionEntrys[64]);
         }

         return toReturn;
      }
   }

   @Override
   public List<ActionEntry> getBehavioursFor(
      Creature performer, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, boolean border, int heightOffset
   ) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (hasHoleEachSide(tilex, tiley, onSurface, dir)) {
         return toReturn;
      } else {
         toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, dir, border, heightOffset));
         toReturn.add(Actions.actionEntrys[607]);
         return toReturn;
      }
   }

   @Override
   public boolean action(
      Action act,
      Creature performer,
      Item source,
      int tilex,
      int tiley,
      boolean onSurface,
      int heightOffset,
      Tiles.TileBorderDirection dir,
      long borderId,
      short action,
      float counter
   ) {
      boolean done = true;
      if (action == 1) {
         done = this.action(act, performer, tilex, tiley, onSurface, dir, borderId, action, counter);
      } else if (!hasHoleEachSide(tilex, tiley, onSurface, dir)) {
         if (Actions.isActionBuildFence(action)) {
            boolean ok = onSurface;
            if (!onSurface) {
               VolaTile vt = Zones.getOrCreateTile(tilex, tiley, onSurface);
               if (vt.getVillage() != null) {
                  ok = true;
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You are not allowed to make a fence in a cave when not on a deed.");
               }
            }

            if (ok) {
               if (!hasMarker(tilex, tiley, onSurface, dir)) {
                  done = MethodsStructure.buildFence(act, performer, source, tilex, tiley, onSurface, heightOffset, dir, borderId, action, counter);
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You are not allowed to make a fence across a highway.");
               }
            }
         } else if (onSurface && action == 533 && source.isDiggingtool()) {
            done = Flattening.flattenTileBorder(borderId, performer, source, tilex, tiley, dir, counter, act);
         } else if (onSurface && action == 865 && source.isDiggingtool()) {
            done = Flattening.flattenTileBorder(borderId, performer, source, tilex, tiley, dir, counter, act);
         } else if (onSurface && action == 186) {
            if (!hasMarker(tilex, tiley, onSurface, dir)) {
               done = Terraforming.plantHedge(performer, source, tilex, tiley, onSurface, dir, counter, act);
            } else {
               performer.getCommunicator().sendNormalServerMessage("You are not allowed to plant a hedge across a highway.");
            }
         } else if (!onSurface || !source.isTrellis() || action != 176 && action != 746 && action != 747) {
            if (onSurface && action == 563) {
               if (!hasMarker(tilex, tiley, onSurface, dir)) {
                  done = Terraforming.plantFlowerbed(performer, source, tilex, tiley, onSurface, dir, counter, act);
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You are not allowed to plant a flowerbed across a highway.");
               }
            } else if (onSurface && action == 363) {
               if (!hasMarker(tilex, tiley, onSurface, dir)) {
                  int[] opening = Terraforming.getCaveOpeningCoords(tilex, tiley);
                  if (opening[0] != -1 && opening[1] != -1) {
                     if (!isWideEntrance(opening[0], opening[1])) {
                        done = Terraforming.buildMineDoor(performer, source, act, opening[0], opening[1], onSurface, counter);
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You are not allowed to add a minedoor on wide mine entrances.");
                     }
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You are not allowed to add a minedoor across a highway.");
               }
            } else if (action == 64) {
               if (performer.getPower() >= 5 && source.getTemplateId() == 176) {
                  try {
                     Zone z = Zones.getZone(tilex, tiley, onSurface);

                     for(int x = z.getStartX(); x < z.getStartX() + z.getSize(); ++x) {
                        try {
                           ItemFactory.createItem(
                              344,
                              2.0F,
                              (float)((x << 2) + 2),
                              (float)((z.getStartY() << 2) + 2),
                              1.0F,
                              true,
                              (byte)0,
                              performer.getBridgeId(),
                              performer.getName()
                           );
                        } catch (FailedException var24) {
                        } catch (NoSuchTemplateException var25) {
                        }
                     }

                     for(int x = z.getStartX(); x < z.getStartX() + z.getSize(); ++x) {
                        try {
                           ItemFactory.createItem(
                              344,
                              2.0F,
                              (float)((x << 2) + 2),
                              (float)((z.getEndY() << 2) + 2),
                              1.0F,
                              true,
                              (byte)0,
                              performer.getBridgeId(),
                              performer.getName()
                           );
                        } catch (FailedException var22) {
                        } catch (NoSuchTemplateException var23) {
                        }
                     }

                     for(int x = z.getStartY(); x < z.getStartY() + z.getSize(); ++x) {
                        if (x != z.getStartY()) {
                           try {
                              ItemFactory.createItem(
                                 344,
                                 2.0F,
                                 (float)((z.getStartX() << 2) + 2),
                                 (float)((x << 2) + 2),
                                 1.0F,
                                 true,
                                 (byte)0,
                                 performer.getBridgeId(),
                                 performer.getName()
                              );
                           } catch (FailedException var20) {
                           } catch (NoSuchTemplateException var21) {
                           }
                        }
                     }

                     for(int x = z.getStartY(); x < z.getStartY() + z.getSize(); ++x) {
                        if (x != z.getStartY()) {
                           try {
                              ItemFactory.createItem(
                                 344,
                                 2.0F,
                                 (float)((z.getEndX() << 2) + 2),
                                 (float)((x << 2) + 2),
                                 1.0F,
                                 true,
                                 (byte)0,
                                 performer.getBridgeId(),
                                 performer.getName()
                              );
                           } catch (FailedException var18) {
                           } catch (NoSuchTemplateException var19) {
                           }
                        }
                     }
                  } catch (NoSuchZoneException var26) {
                     performer.getCommunicator().sendNormalServerMessage("No zone at " + tilex + ", " + tiley + "," + onSurface + ".");
                  }
               }
            } else if (act.isSpell()) {
               Spell spell = Spells.getSpell(action);
               int layer = onSurface ? 0 : -1;
               if (source.isMagicStaff() || source.getTemplateId() == 176 && performer.getPower() >= 2 && Servers.isThisATestServer()) {
                  if (Methods.isActionAllowed(performer, (short)547)) {
                     done = Methods.castSpell(performer, spell, tilex, tiley, layer, heightOffset, dir, counter);
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You need to use a magic staff.");
                  done = true;
               }
            } else {
               if (!onSurface || heightOffset != 0 || !source.isMiningtool() || action != 145) {
                  return this.action(act, performer, tilex, tiley, onSurface, dir, borderId, action, counter);
               }

               int digTilex = (int)performer.getStatus().getPositionX() + 2 >> 2;
               int digTiley = (int)performer.getStatus().getPositionY() + 2 >> 2;
               int tile = Server.surfaceMesh.getTile(digTilex, digTiley);
               byte type = Tiles.decodeType(tile);
               if (type == Tiles.Tile.TILE_ROCK.id) {
                  done = TileRockBehaviour.mine(act, performer, source, tilex, tiley, action, counter, digTilex, digTiley);
               }
            }
         } else {
            done = Terraforming.plantTrellis(performer, source, tilex, tiley, onSurface, dir, action, counter, act);
         }
      }

      return done;
   }

   @Override
   public boolean action(
      Action act, Creature performer, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, long borderId, short action, float counter
   ) {
      if (action == 1) {
         handle_EXAMINE(performer, tilex, tiley, onSurface, dir);
      } else {
         if (hasHoleEachSide(tilex, tiley, onSurface, dir)) {
            return true;
         }

         if (action == 607) {
            performer.getCommunicator().sendAddTileBorderToCreationWindow(borderId);
         }
      }

      return true;
   }

   private static void handle_EXAMINE(Creature performer, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir) {
      Communicator comm = performer.getCommunicator();
      if (hasHoleEachSide(tilex, tiley, onSurface, dir)) {
         comm.sendNormalServerMessage("This is in the middle of a wide cave entrance.");
      } else {
         comm.sendNormalServerMessage("This outlines where fences and walls may be built.");
      }

      if (tilex - 2 < 0 || tilex + 2 > 1 << Constants.meshSize || tiley - 2 < 0 || tiley + 2 > 1 << Constants.meshSize) {
         comm.sendNormalServerMessage("The water is too deep to measure.");
      } else if (performer.isWithinTileDistanceTo(tilex, tiley, 20, 3)) {
         MeshIO mesh;
         if (onSurface) {
            mesh = Server.surfaceMesh;
         } else {
            mesh = Server.caveMesh;
         }

         short height = Tiles.decodeHeight(mesh.getTile(tilex, tiley));
         boolean away = false;
         short endheight = height;
         int diff = 0;
         if (dir == Tiles.TileBorderDirection.DIR_HORIZ) {
            int endx = tilex + 1;
            endheight = Tiles.decodeHeight(mesh.getTile(endx, tiley));
            float posx = performer.getPosX();
            diff = Math.abs(height - endheight);
            if (Math.abs(posx - (float)(endx << 2)) > Math.abs(posx - (float)(tilex << 2))) {
               away = true;
            }
         } else if (dir == Tiles.TileBorderDirection.DIR_DOWN) {
            int endy = tiley + 1;
            endheight = Tiles.decodeHeight(mesh.getTile(tilex, endy));
            float posy = performer.getPosY();
            diff = Math.abs(height - endheight);
            if (Math.abs(posy - (float)(endy << 2)) > Math.abs(posy - (float)(tiley << 2))) {
               away = true;
            }
         }

         String dist = "up.";
         if (away && height > endheight || !away && endheight > height) {
            dist = "down.";
         }

         if (diff != 0) {
            comm.sendNormalServerMessage("The border is " + diff + " slope " + dist);
         } else {
            comm.sendNormalServerMessage("The border is level.");
         }
      }
   }

   private static boolean hasHoleEachSide(int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir) {
      if (!onSurface) {
         return false;
      } else {
         MeshTile mTileCurrent = new MeshTile(Server.surfaceMesh, tilex, tiley);
         if (dir == Tiles.TileBorderDirection.DIR_HORIZ) {
            MeshTile mTileNorth = mTileCurrent.getNorthMeshTile();
            if (mTileCurrent.isHole() && mTileNorth.isHole()) {
               return true;
            }
         } else {
            MeshTile mTileWest = mTileCurrent.getWestMeshTile();
            if (mTileCurrent.isHole() && mTileWest.isHole()) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean isWideEntrance(int tilex, int tiley) {
      MeshTile mTileCurrent = new MeshTile(Server.surfaceMesh, tilex, tiley);
      if (mTileCurrent.isHole()) {
         MeshTile mTileNorth = mTileCurrent.getNorthMeshTile();
         if (mTileNorth.isHole()) {
            return true;
         }

         MeshTile mTileWest = mTileCurrent.getWestMeshTile();
         if (mTileWest.isHole()) {
            return true;
         }

         MeshTile mTileSouth = mTileCurrent.getSouthMeshTile();
         if (mTileSouth.isHole()) {
            return true;
         }

         MeshTile mTileEast = mTileCurrent.getEastMeshTile();
         if (mTileEast.isHole()) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasMarker(int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir) {
      if (Items.getMarker(tilex, tiley, onSurface, 0, -10L) != null) {
         return true;
      } else if (dir == Tiles.TileBorderDirection.DIR_HORIZ && Items.getMarker(tilex + 1, tiley, onSurface, 0, -10L) != null) {
         return true;
      } else if (dir == Tiles.TileBorderDirection.DIR_DOWN && Items.getMarker(tilex, tiley + 1, onSurface, 0, -10L) != null) {
         return true;
      } else {
         if (Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)) == Tiles.Tile.TILE_HOLE.id) {
            if (Items.getMarker(tilex, tiley, !onSurface, 0, -10L) != null) {
               return true;
            }

            if (dir == Tiles.TileBorderDirection.DIR_HORIZ && Items.getMarker(tilex + 1, tiley, !onSurface, 0, -10L) != null) {
               return true;
            }

            if (dir == Tiles.TileBorderDirection.DIR_DOWN && Items.getMarker(tilex, tiley + 1, !onSurface, 0, -10L) != null) {
               return true;
            }
         }

         return false;
      }
   }
}
