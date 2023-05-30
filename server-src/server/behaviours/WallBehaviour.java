package com.wurmonline.server.behaviours;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.DemolishCheckQuestion;
import com.wurmonline.server.questions.ManageObjectList;
import com.wurmonline.server.questions.ManagePermissions;
import com.wurmonline.server.questions.MissionManager;
import com.wurmonline.server.questions.PermissionsHistory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.DbDoor;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchLockException;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.structures.WallEnum;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class WallBehaviour extends Behaviour implements MiscConstants, ItemTypes {
   private static final Logger logger = Logger.getLogger(WallBehaviour.class.getName());
   private static Item[] inventoryItems = new Item[0];
   private static Item[] bodyItems = new Item[0];
   private static int[] foundTemplates = new int[0];

   WallBehaviour() {
      super((short)20);
   }

   private static boolean isValidModifyableWall(Wall wall, Item tool) {
      int templateId = tool.getTemplateId();
      if (wall.getMaterial() == StructureMaterialEnum.STONE) {
         return templateId == 219;
      } else if (wall.getMaterial() == StructureMaterialEnum.PLAIN_STONE && (templateId == 62 || templateId == 63)) {
         return wall.getType() != StructureTypeEnum.NARROW_WINDOW && wall.getType() != StructureTypeEnum.BARRED;
      } else {
         return false;
      }
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Wall wall) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, source, wall));
      VolaTile wallTile = Zones.getOrCreateTile(wall.getTileX(), wall.getTileY(), wall.isOnSurface());
      Structure structure = Structures.getStructureOrNull(wall.getStructureId());
      if (structure != null && structure.isFinalized() && !wall.isFinished()) {
         toReturn.add(Actions.actionEntrys[607]);
      }

      if (isValidModifyableWall(wall, source)) {
         if (source.getTemplateId() == 219) {
            toReturn.add(new ActionEntry((short)647, "Remove decorations", "removing decorations"));
         } else {
            toReturn.add(new ActionEntry((short)647, "Add decorations", "adding decorations"));
         }
      }

      if (MethodsStructure.isCorrectToolForBuilding(performer, source.getTemplateId())) {
         if (structure != null) {
            boolean hasMarker = StructureBehaviour.hasMarker(wall.getStartX(), wall.getStartY(), wall.isOnSurface(), wall.getDir(), wall.getHeight());
            if (!structure.isFinalized()) {
               toReturn.add(Actions.actionEntrys[58]);
            } else if (wall.getType() == StructureTypeEnum.PLAN && hasMarker) {
               toReturn.addAll(addBuildMenu(performer, source, wall, hasMarker));
            } else if (wall.getType() == StructureTypeEnum.PLAN && structure.needsDoor()) {
               toReturn.addAll(addBuildMenu(performer, source, wall, hasMarker));
            } else if (!wall.isFinished()) {
               if (wall.getState() == StructureStateEnum.INITIALIZED) {
                  toReturn.addAll(addBuildMenu(performer, source, wall, hasMarker));
               } else {
                  WallEnum typeOfWall = WallEnum.getWall(wall.getType(), wall.getMaterial());
                  StructureTypeEnum type = wall.getType();
                  if (typeOfWall != WallEnum.WALL_PLAN) {
                     if (type == StructureTypeEnum.DOOR) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue door", "building door", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.DOUBLE_DOOR) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue door", "building double door", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.ARCHED) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue arched wall", "building arched wall", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.ARCHED_LEFT) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue left arch", "building arched wall", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.ARCHED_RIGHT) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue right arch", "building arched wall", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.ARCHED_T) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue T arch", "building arched wall", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.SOLID) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue wall", "building wall", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.PORTCULLIS) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue portcullis", "building portcullis", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.BARRED) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue barred wall", "building barred wall", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.BALCONY) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue balcony", "building balcony", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.JETTY) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue jetty", "building jetty", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.ORIEL) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue oriel", "building oriel", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.CANOPY_DOOR) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue canopy door", "building canopy", new int[]{43}, 2));
                     } else if (type == StructureTypeEnum.WIDE_WINDOW) {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue wide window", "building wide widnow", new int[]{43}, 2));
                     } else {
                        toReturn.add(new ActionEntry(typeOfWall.getActionId(), "Continue window", "building window", new int[]{43}, 2));
                     }
                  }
               }
            }
         }
      } else if (source.isLock() && source.getTemplateId() == 167) {
         if ((
               wall.getType() == StructureTypeEnum.DOOR
                  || wall.getType() == StructureTypeEnum.DOUBLE_DOOR
                  || wall.getType() == StructureTypeEnum.PORTCULLIS
                  || wall.getType() == StructureTypeEnum.CANOPY_DOOR
            )
            && wall.isFinished()
            && structure != null
            && structure.mayModify(performer)) {
            Door[] doors = structure.getAllDoors();

            for(int x = 0; x < doors.length; ++x) {
               try {
                  if (doors[x].getWall() == wall) {
                     try {
                        doors[x].getLockId();
                        toReturn.add(new ActionEntry((short)161, "Change lock", "changing lock", emptyIntArr));
                     } catch (NoSuchLockException var13) {
                        toReturn.add(Actions.actionEntrys[161]);
                     }
                     break;
                  }
               } catch (NoSuchWallException var14) {
                  logger.log(Level.WARNING, "No inner wall");
               }
            }
         }
      } else if (source.getTemplateId() == 463) {
         if ((
               wall.getType() == StructureTypeEnum.DOOR
                  || wall.getType() == StructureTypeEnum.DOUBLE_DOOR
                  || wall.getType() == StructureTypeEnum.PORTCULLIS
                  || wall.getType() == StructureTypeEnum.CANOPY_DOOR
            )
            && (wall.isOnPvPServer() || Servers.isThisATestServer())
            && wall.isFinished()) {
            Door door = wall.getDoor();
            if (door != null) {
               try {
                  Item lock = door.getLock();
                  if (performer.isWithinDistanceTo(wall.getTileX(), wall.getTileY(), 1)) {
                     MethodsStructure.addLockPickEntry(performer, source, door, false, lock, toReturn);
                  }
               } catch (NoSuchLockException var12) {
               }
            }
         }
      } else if (source.getTemplateId() == wall.getRepairItemTemplate()) {
         if (wall.getDamage() > 0.0F) {
            if (!wall.isNoRepair() && (!Servers.localServer.challengeServer || performer.getEnemyPresense() <= 0)) {
               toReturn.add(Actions.actionEntrys[193]);
            }
         } else if (wall.isFinished() && wall.getQualityLevel() < 100.0F && !wall.isNoImprove()) {
            toReturn.add(Actions.actionEntrys[192]);
         }
      } else if (source.isColor()) {
         if (wall.isFinished() && !wall.isNotPaintable()) {
            toReturn.add(Actions.actionEntrys[231]);
         }
      } else if (source.getTemplateId() == 441) {
         if (wall.getColor() != -1 && !wall.isNotPaintable()) {
            toReturn.add(Actions.actionEntrys[232]);
         }
      } else if (source.getTemplateId() == 676 && source.getOwnerId() == performer.getWurmId()) {
         toReturn.add(Actions.actionEntrys[472]);
      }

      boolean addedDestroy = false;
      if (performer.getPower() >= 2) {
         if (structure != null) {
            int num = 0;
            if (structure.isFinalized() && wall.getType() != StructureTypeEnum.PLAN) {
               if (wall.getDamage() > 0.0F) {
                  ++num;
               } else if (wall.getQualityLevel() < 100.0F) {
                  ++num;
               }

               if (performer.getPower() >= 5) {
                  ++num;
               }

               toReturn.add(new ActionEntry((short)(-(num + 3)), "Wall", "wall"));
               if (wall.getDamage() > 0.0F) {
                  if (!Servers.localServer.challengeServer || performer.getEnemyPresense() <= 0) {
                     toReturn.add(Actions.actionEntrys[193]);
                  }
               } else if (wall.getQualityLevel() < 100.0F) {
                  toReturn.add(Actions.actionEntrys[192]);
               }

               toReturn.add(Actions.actionEntrys[180]);
               toReturn.add(new ActionEntry((short)-1, "Annihilate", "Annihilate"));
               toReturn.add(Actions.actionEntrys[82]);
               toReturn.add(Actions.actionEntrys[662]);
               if (performer.getPower() >= 5) {
                  toReturn.add(Actions.actionEntrys[90]);
               }
            } else if (structure.isFinalized() && wall.getType() == StructureTypeEnum.PLAN && performer.getPower() >= 4) {
               toReturn.add(Actions.actionEntrys[866]);
            } else if ((source.getTemplateId() == 176 || source.getTemplateId() == 315) && WurmPermissions.mayUseGMWand(performer)) {
               if (!addedDestroy) {
                  toReturn.add(new ActionEntry((short)-1, "Annihilate", "Annihilate"));
                  toReturn.add(Actions.actionEntrys[82]);
                  addedDestroy = true;
               }

               toReturn.add(Actions.actionEntrys[662]);
            }

            toReturn.add(Actions.actionEntrys[78]);
         }

         if ((source.getTemplateId() == 315 || source.getTemplateId() == 176) && performer.getPower() >= 2) {
            toReturn.add(Actions.actionEntrys[684]);
         }
      }

      Skills skills = performer.getSkills();

      try {
         Skill str = skills.getSkill(102);
         if (str.getRealKnowledge() > 21.0) {
            if (!wall.isWallPlan()) {
               if (!wall.isRubble()) {
                  toReturn.add(new ActionEntry((short)-1, "Wall", "Wall"));
                  toReturn.add(Actions.actionEntrys[174]);
               }
            } else if (structure != null && !structure.hasWalls() && !addedDestroy) {
               toReturn.add(new ActionEntry((short)-1, "Structure", "Structure"));
               toReturn.add(Actions.actionEntrys[82]);
               addedDestroy = true;
            }
         }
      } catch (NoSuchSkillException var11) {
         logger.log(Level.WARNING, "Weird, " + performer.getName() + " has no strength!");
      }

      if (isIndoorWallPlan(wall)) {
         toReturn.add(Actions.actionEntrys[57]);
      }

      if (source.isTrellis() && performer.getFloorLevel() == 0) {
         toReturn.add(new ActionEntry((short)-3, "Plant", "Plant options"));
         toReturn.add(Actions.actionEntrys[746]);
         toReturn.add(new ActionEntry((short)176, "In center", "planting"));
         toReturn.add(Actions.actionEntrys[747]);
      }

      MissionTrigger[] m2 = MissionTriggers.getMissionTriggersWith(source.getTemplateId(), 473, wall.getId());
      if (m2.length > 0) {
         toReturn.add(Actions.actionEntrys[473]);
      }

      MissionTrigger[] m3 = MissionTriggers.getMissionTriggersWith(source.getTemplateId(), 474, wall.getId());
      if (m3.length > 0) {
         toReturn.add(Actions.actionEntrys[474]);
      }

      addWarStuff(toReturn, performer, wall);
      toReturn.addAll(addManage(performer, structure, wall));
      if (wall.isFinished() && MethodsStructure.mayModifyStructure(performer, structure, wallTile, (short)683)) {
         toReturn.add(Actions.actionEntrys[683]);
         if (!wall.isPlainStone() || source.getTemplateId() != 130 && (!source.isWand() || performer.getPower() < 4)) {
            if (wall.isPlastered() && (source.getTemplateId() == 1115 || source.isWand() && performer.getPower() >= 4)) {
               toReturn.add(new ActionEntry((short)847, "Remove render", "removing"));
            }
         } else {
            toReturn.add(Actions.actionEntrys[847]);
         }

         if (wall.isLRArch() && (source.getTemplateId() == 1115 || source.isWand() && performer.getPower() >= 4)) {
            toReturn.add(Actions.actionEntrys[848]);
         }
      }

      return toReturn;
   }

   private static final List<ActionEntry> addBuildMenu(Creature performer, Item source, Wall wall, boolean hasMarker) {
      List<ActionEntry> hlist = new LinkedList<>();
      List<List<ActionEntry>> alist = new LinkedList<>();
      StructureMaterialEnum[] materials = WallEnum.getMaterialsFromToolType(source, performer);

      for(StructureMaterialEnum material : materials) {
         List<ActionEntry> mlist = new LinkedList<>();
         List<WallEnum> wallTypes = WallEnum.getWallsByToolAndMaterial(performer, source, false, hasMarker || MethodsStructure.hasInsideFence(wall), material);
         if (wallTypes.size() > 0) {
            hlist.add(new ActionEntry((short)(-wallTypes.size()), Wall.getMaterialName(material), "building"));

            for(int i = 0; i < wallTypes.size(); ++i) {
               mlist.add(wallTypes.get(i).createActionEntry());
            }

            Collections.sort(mlist);
            alist.add(mlist);
         }
      }

      List<ActionEntry> toReturn = new LinkedList<>();
      switch(hlist.size()) {
         case 0:
            break;
         case 1:
            toReturn.add(new ActionEntry((short)(-alist.get(0).size()), "Build", "building"));
            toReturn.addAll(alist.get(0));
            break;
         default:
            toReturn.add(new ActionEntry((short)(-hlist.size()), "Build", "building"));
            int count = 0;

            for(List<ActionEntry> zlist : alist) {
               toReturn.add(hlist.get(count++));
               toReturn.addAll(zlist);
            }
      }

      return toReturn;
   }

   private static final void addWarStuff(List<ActionEntry> toReturn, Creature performer, Wall wall) {
      Village targVill = wall.getTile().getVillage();
      Village village = performer.getCitizenVillage();
      if (village != null && village.mayDoDiplomacy(performer) && targVill != null && village != targVill) {
         boolean atPeace = village.mayDeclareWarOn(targVill);
         if (atPeace) {
            toReturn.add(new ActionEntry((short)-1, "Village", "Village options", emptyIntArr));
            toReturn.add(Actions.actionEntrys[209]);
         }
      }
   }

   public static final List<ActionEntry> addManage(Creature performer, Structure structure, Wall wall) {
      List<ActionEntry> toReturn = new LinkedList<>();
      List<ActionEntry> permissions = new LinkedList<>();
      if (structure.isFinalized()) {
         if (structure.mayManage(performer)) {
            permissions.add(Actions.actionEntrys[673]);
         }

         if (structure.mayShowPermissions(performer) || structure.isActualOwner(performer.getWurmId())) {
            permissions.add(Actions.actionEntrys[664]);
         }

         Door door = wall.getDoor();
         if (door != null && (door.mayShowPermissions(performer) || structure.mayManage(performer) || performer.getPower() > 1)) {
            permissions.add(Actions.actionEntrys[666]);
         }

         if (structure.maySeeHistory(performer)) {
            permissions.add(Actions.actionEntrys[691]);
            if (wall.isFinished() && door != null) {
               permissions.add(Actions.actionEntrys[692]);
            }
         }

         if (!permissions.isEmpty()) {
            if (permissions.size() > 1) {
               Collections.sort(permissions);
               toReturn.add(new ActionEntry((short)(-permissions.size()), "Permissions", "viewing"));
            }

            toReturn.addAll(permissions);
         }

         if (door != null && door.mayLock(performer) && door.hasLock()) {
            if (door.isLocked()) {
               toReturn.add(Actions.actionEntrys[102]);
            } else {
               toReturn.add(Actions.actionEntrys[28]);
            }
         }
      }

      return toReturn;
   }

   private static boolean mayLockDoor(Creature performer, Wall wall, Door door) {
      return wall.isFinished() && door != null ? door.mayLock(performer) : false;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Wall wall) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, wall));
      VolaTile wallTile = Zones.getOrCreateTile(wall.getTileX(), wall.getTileY(), wall.getLayer() >= 0);
      Structure structure = Structures.getStructureOrNull(wall.getStructureId());
      if (structure == null) {
         toReturn.addAll(Actions.getDefaultItemActions());
         return toReturn;
      } else {
         if (this.canRemoveWallPlan(performer, wall)) {
            toReturn.add(Actions.actionEntrys[57]);
         }

         if (structure.isFinalized() && !wall.isFinished()) {
            toReturn.add(Actions.actionEntrys[607]);
         }

         toReturn.addAll(addManage(performer, structure, wall));
         if (wall.isFinished() && MethodsStructure.mayModifyStructure(performer, structure, wallTile, (short)683)) {
            toReturn.add(Actions.actionEntrys[683]);
         }

         return toReturn;
      }
   }

   private static String getConstructionMessage(Wall wall) {
      String genus;
      String type;
      switch(wall.getType()) {
         case SOLID:
            genus = "a";
            type = "wall";
            break;
         case WINDOW:
            genus = "a";
            type = "window";
            break;
         case WIDE_WINDOW:
            genus = "a";
            type = "wide window";
            break;
         case DOOR:
         case DOUBLE_DOOR:
         case CANOPY_DOOR:
            genus = "a";
            type = "door";
            break;
         case ARCHED:
         case ARCHED_LEFT:
         case ARCHED_RIGHT:
         case ARCHED_T:
            genus = "an";
            type = "arched wall";
            break;
         case NARROW_WINDOW:
            genus = "a";
            type = "narrow window";
            break;
         case PORTCULLIS:
            genus = "a";
            type = "portcullis";
            break;
         case BARRED:
            genus = "a";
            type = "barred wall";
            break;
         case JETTY:
            genus = "a";
            type = "jetty wall";
            break;
         case BALCONY:
            genus = "a";
            type = "balcony";
            break;
         case ORIEL:
            genus = "an";
            type = "oriel wall";
            break;
         default:
            genus = "a";
            type = "wall";
      }

      String msg = StringUtil.format("You see %s %s under construction. The %s needs ", genus, type, type);
      int[] neededMats = WallEnum.getMaterialsNeeded(wall);
      String part2 = "";

      for(int i = 0; i < neededMats.length; i += 2) {
         try {
            ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(neededMats[i]);
            int count = neededMats[i + 1];
            if (part2.length() > 0) {
               part2 = part2 + ", ";
            }

            part2 = part2 + count + " " + template.sizeString + template.getPlural();
         } catch (NoSuchTemplateException var9) {
            logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
         }
      }

      if (part2.length() > 0) {
         part2 = part2 + ".";
      }

      return msg + part2;
   }

   static final void sendQlString(Communicator comm, Wall wall) {
      comm.sendNormalServerMessage("QL=" + wall.getQualityLevel() + ", dam=" + wall.getDamage());
      if (comm.player != null && comm.player.getPower() > 0) {
         try {
            Structure struct = Structures.getStructure(wall.getStructureId());
            String ownerName = "unknown";
            PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(struct.getOwnerId());
            if (info != null) {
               ownerName = info.getName();
            }

            comm.sendNormalServerMessage("Structure=" + wall.getStructureId() + ", owner=" + ownerName + " (" + struct.getOwnerId() + ")");
            comm.sendNormalServerMessage("Structure finished=" + struct.isFinished());
            if (comm.player.getPower() >= 5) {
               comm.sendNormalServerMessage(
                  "wall= "
                     + wall.getNumber()
                     + " start["
                     + wall.getStartX()
                     + ","
                     + wall.getStartY()
                     + "] end=["
                     + wall.getEndX()
                     + ","
                     + wall.getEndY()
                     + "] state="
                     + wall.getState()
                     + " color="
                     + wall.getColor()
                     + " material="
                     + wall.getMaterial()
                     + " type="
                     + wall.getType()
                     + " cover="
                     + wall.getCover()
                     + " walltile=["
                     + wall.getTile().tilex
                     + ","
                     + wall.getTile().tiley
                     + "] finished="
                     + wall.getTile().getStructure().isFinished()
                     + " isIndoor="
                     + wall.isIndoor()
                     + " height="
                     + wall.getHeight()
                     + " layer="
                     + wall.getLayer()
                     + ")"
               );
            }
         } catch (NoSuchStructureException var5) {
         }
      }
   }

   @Override
   public boolean action(Action act, Creature performer, Wall wall, short action, float counter) {
      boolean done = true;
      Structure structure = Structures.getStructureOrNull(wall.getStructureId());
      Door door = wall.getDoor();
      if (action == 1) {
         Communicator comm = performer.getCommunicator();
         StructureTypeEnum type = wall.getType();
         comm.sendNormalServerMessage("Material " + wall.getMaterialString());
         if (type != StructureTypeEnum.DOOR
            && type != StructureTypeEnum.DOUBLE_DOOR
            && type != StructureTypeEnum.PORTCULLIS
            && type != StructureTypeEnum.CANOPY_DOOR) {
            if (type == StructureTypeEnum.NARROW_WINDOW) {
               if (wall.isFinished()) {
                  comm.sendNormalServerMessage("You see a narrow window.");
                  sendQlString(comm, wall);
               } else {
                  comm.sendNormalServerMessage(getConstructionMessage(wall));
                  sendQlString(comm, wall);
               }
            } else if (Wall.isArched(type)) {
               if (wall.isFinished()) {
                  comm.sendNormalServerMessage("You see an arched wall opening.");
                  sendQlString(comm, wall);
               } else {
                  comm.sendNormalServerMessage(getConstructionMessage(wall));
                  sendQlString(comm, wall);
               }
            } else if (type == StructureTypeEnum.SOLID) {
               if (wall.isFinished()) {
                  comm.sendNormalServerMessage("You see a wall.");
                  sendQlString(comm, wall);
               } else {
                  comm.sendNormalServerMessage(getConstructionMessage(wall));
                  sendQlString(comm, wall);
               }
            } else if (type == StructureTypeEnum.BARRED) {
               if (wall.isFinished()) {
                  comm.sendNormalServerMessage("You see a barred wall.");
                  sendQlString(comm, wall);
               } else {
                  comm.sendNormalServerMessage(getConstructionMessage(wall));
                  sendQlString(comm, wall);
               }
            } else if (type == StructureTypeEnum.BALCONY) {
               if (wall.isFinished()) {
                  comm.sendNormalServerMessage("You see a balcony.");
                  sendQlString(comm, wall);
               } else {
                  comm.sendNormalServerMessage(getConstructionMessage(wall));
                  sendQlString(comm, wall);
               }
            } else if (type == StructureTypeEnum.ORIEL) {
               if (wall.isFinished()) {
                  comm.sendNormalServerMessage("You see an oriel wall.");
                  sendQlString(comm, wall);
               } else {
                  comm.sendNormalServerMessage(getConstructionMessage(wall));
                  sendQlString(comm, wall);
               }
            } else if (type == StructureTypeEnum.JETTY) {
               if (wall.isFinished()) {
                  comm.sendNormalServerMessage("You see a jetty wall.");
                  sendQlString(comm, wall);
               } else {
                  comm.sendNormalServerMessage(getConstructionMessage(wall));
                  sendQlString(comm, wall);
               }
            } else if (type == StructureTypeEnum.WINDOW) {
               if (wall.isFinished()) {
                  comm.sendNormalServerMessage("You see a window.");
                  sendQlString(comm, wall);
               } else {
                  comm.sendNormalServerMessage(getConstructionMessage(wall));
                  sendQlString(comm, wall);
               }
            } else if (type == StructureTypeEnum.WIDE_WINDOW) {
               if (wall.isFinished()) {
                  comm.sendNormalServerMessage("You see a wide window");
                  sendQlString(comm, wall);
               } else {
                  comm.sendNormalServerMessage(getConstructionMessage(wall));
                  sendQlString(comm, wall);
               }
            } else {
               comm.sendNormalServerMessage("You see some markers for a new structure.");
            }
         } else {
            if (!wall.isFinished()) {
               comm.sendNormalServerMessage(getConstructionMessage(wall));
               sendQlString(comm, wall);
               return true;
            }

            if (structure == null) {
               logger.log(Level.WARNING, "This wall has no structure: " + wall.getId());
               comm.sendNormalServerMessage("This wall has a problem with its data. Please report this.");
            } else {
               Floor[] floors = structure.getFloors();

               for(Floor floor : floors) {
                  if (floor.getFloorLevel() == 0 && performer.getPower() > 0) {
                     comm.sendNormalServerMessage(
                        "State=" + floor.getState() + "  x=" + floor.getTileX() + ", " + floor.getTileY() + " finished=" + floor.isFinished()
                     );
                  }
               }

               Door[] doors = structure.getAllDoors();

               for(int x = 0; x < doors.length; ++x) {
                  try {
                     if (doors[x].getWall() == wall) {
                        if (performer.getPower() > 0) {
                           comm.sendNormalServerMessage(
                              "State="
                                 + wall.getState()
                                 + " inner x="
                                 + doors[x].getInnerTile().getTileX()
                                 + ", "
                                 + doors[x].getInnerTile().getTileY()
                                 + ", outer: "
                                 + doors[x].getOuterTile().getTileX()
                                 + ", y="
                                 + doors[x].getOuterTile().getTileY()
                           );
                        }

                        try {
                           Item lock = doors[x].getLock();
                           String lockStrength = lock.getLockStrength();
                           comm.sendNormalServerMessage("You see a door with a lock. The lock is of " + lockStrength + " quality.");
                           if (performer.getPower() >= 5) {
                              comm.sendNormalServerMessage("Lock WurmId=" + lock.getWurmId() + ", dam=" + lock.getDamage());
                           }

                           if (wall.getColor() != -1) {
                              comm.sendNormalServerMessage(
                                 "Colors: R="
                                    + WurmColor.getColorRed(wall.getColor())
                                    + ", G="
                                    + WurmColor.getColorGreen(wall.getColor())
                                    + ", B="
                                    + WurmColor.getColorBlue(wall.getColor())
                                    + "."
                              );
                           }

                           if (doors[x].getLockCounter() > 0) {
                              comm.sendNormalServerMessage("The door is picked open and will shut in " + doors[x].getLockCounterTime());
                           } else if (lock.isLocked()) {
                              comm.sendNormalServerMessage("It is locked.");
                           } else {
                              comm.sendNormalServerMessage("It is unlocked.");
                           }

                           sendQlString(comm, wall);
                           return true;
                        } catch (NoSuchLockException var16) {
                           comm.sendNormalServerMessage("You see a door. The door has no lock.");
                           if (wall.getColor() != -1) {
                              comm.sendNormalServerMessage(
                                 "Colors: R="
                                    + WurmColor.getColorRed(wall.getColor())
                                    + ", G="
                                    + WurmColor.getColorGreen(wall.getColor())
                                    + ", B="
                                    + WurmColor.getColorBlue(wall.getColor())
                                    + "."
                              );
                           }

                           if (doors[x].getLockCounter() > 0) {
                              comm.sendNormalServerMessage("The door is picked open and will shut in " + doors[x].getLockCounter() / 2 + " seconds.");
                           }

                           sendQlString(comm, wall);
                           return true;
                        }
                     }
                  } catch (NoSuchWallException var17) {
                     logger.log(Level.WARNING, "No inner wall");
                  }
               }
            }
         }

         if (wall.getColor() != -1) {
            comm.sendNormalServerMessage(
               "Colors: R="
                  + WurmColor.getColorRed(wall.getColor())
                  + ", G="
                  + WurmColor.getColorGreen(wall.getColor())
                  + ", B="
                  + WurmColor.getColorBlue(wall.getColor())
                  + "."
            );
         }

         if (performer.getPower() >= 2) {
            if (structure != null) {
               comm.sendNormalServerMessage(
                  "State=" + wall.getState() + ", wall id=" + wall.getId() + ", structure id=" + wall.getStructureId() + " writid=" + structure.getWritId()
               );
               comm.sendNormalServerMessage("Planned by " + structure.getPlanner() + ".");
            } else {
               comm.sendNormalServerMessage("No Such structure " + wall.getStructureId());
            }
         } else if (performer.getPower() > 1) {
            comm.sendNormalServerMessage("State=" + wall.getState());
         }
      } else if (action == 607) {
         performer.getCommunicator().sendAddWallToCreationWindow(wall, -10L);
      } else if (action == 57) {
         if (this.canRemoveWallPlan(performer, wall)) {
            wall.destroy();
            performer.getCommunicator().sendNormalServerMessage("You remove a plan for a new wall.");
            Server.getInstance().broadCastAction(performer.getName() + " removes a plan for a new wall.", performer, 3);
         } else {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to do that!");
         }
      } else if (action == 82) {
         DemolishCheckQuestion dcq = new DemolishCheckQuestion(performer, "Demolish Building", "A word of warning!", wall.getStructureId());
         dcq.sendQuestion();
      } else {
         if (action == 683 && !wall.isNotTurnable()) {
            return rotateWall(performer, wall, act, counter);
         }

         if (action == 209) {
            if (performer.getCitizenVillage() != null) {
               if (wall.getTile() != null && wall.getTile().getVillage() != null) {
                  if (performer.getCitizenVillage().mayDeclareWarOn(wall.getTile().getVillage())) {
                     Methods.sendWarDeclarationQuestion(performer, wall.getTile().getVillage());
                  } else {
                     performer.getCommunicator().sendAlertServerMessage(wall.getTile().getVillage().getName() + " is already at war with your village.");
                  }
               }
            } else {
               performer.getCommunicator().sendAlertServerMessage("You are no longer a citizen of a village.");
            }
         } else if (action == 664) {
            this.manageBuilding(performer, structure, wall);
         } else if (action == 666) {
            this.manageDoor(performer, structure, wall);
         } else if (action == 673) {
            this.manageAllDoors(performer, structure, wall);
         } else if (action == 691) {
            this.historyBuilding(performer, structure, wall);
         } else if (action == 692) {
            this.historyDoor(performer, wall, door);
         } else if (action == 102 && mayLockDoor(performer, wall, door)) {
            if (door != null && door.hasLock() && door.isLocked() && !door.isNotLockable()) {
               door.unlock(true);
               PermissionsHistories.addHistoryEntry(door.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), "Unlocked door");
            }
         } else if (action == 28 && mayLockDoor(performer, wall, door) && door != null && door.hasLock() && !door.isLocked() && !door.isNotLockable()) {
            door.lock(true);
            PermissionsHistories.addHistoryEntry(door.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), "Locked door");
         }
      }

      return true;
   }

   void manageBuilding(Creature performer, Structure structure, Wall wall) {
      if (structure != null) {
         if (structure.getWritId() != -10L && structure.isActualOwner(performer.getWurmId())) {
            Items.destroyItem(structure.getWritId());
            structure.setWritid(-10L, true);
         }

         if (structure.mayShowPermissions(performer) || structure.isActualOwner(performer.getWurmId())) {
            ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.BUILDING, structure, false, -10L, false, null, "");
            mp.sendQuestion();
         }
      }
   }

   void manageDoor(Creature performer, Structure structure, Wall wall) {
      if (wall.isFinished()) {
         Door door = wall.getDoor();
         if (door != null && (door.mayShowPermissions(performer) || structure.mayManage(performer) || performer.getPower() > 1)) {
            ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.DOOR, door, false, -10L, false, null, "");
            mp.sendQuestion();
         }
      }
   }

   void manageAllDoors(Creature performer, Structure structure, Wall wall) {
      if (structure != null && structure.mayManage(performer)) {
         ManageObjectList mol = new ManageObjectList(performer, ManageObjectList.Type.DOOR, wall.getStructureId(), false, 1, "", true);
         mol.sendQuestion();
      }
   }

   void historyBuilding(Creature performer, Structure structure, Wall wall) {
      if (structure != null && (structure.isOwner(performer) || performer.getPower() > 0)) {
         PermissionsHistory ph = new PermissionsHistory(performer, wall.getStructureId());
         ph.sendQuestion();
      }
   }

   void historyDoor(Creature performer, Wall wall, Door door) {
      if (wall.isFinished() && door != null) {
         PermissionsHistory ph = new PermissionsHistory(performer, wall.getId());
         ph.sendQuestion();
      }
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Wall wall, short action, float counter) {
      boolean done = true;
      Structure structure = Structures.getStructureOrNull(wall.getStructureId());
      Door door = wall.getDoor();
      if (act.isBuildHouseWallAction() && performer.isFighting()) {
         performer.getCommunicator().sendNormalServerMessage("You cannot do that while in combat.");
         performer.getCommunicator().sendActionResult(false);
         return true;
      } else {
         if (action == 1) {
            done = this.action(act, performer, wall, action, counter);
         } else {
            if (action == 647) {
               return modifyWall(performer, source, wall, act, counter);
            }

            if (action == 683) {
               return this.action(act, performer, wall, action, counter);
            }

            if (action == 847 && wall.isPlainStone() && (source.getTemplateId() == 130 || source.isWand() && performer.getPower() >= 4)) {
               return toggleRenderWall(performer, source, wall, act, counter);
            }

            if (action == 847 && wall.isPlastered() && (source.getTemplateId() == 1115 || source.isWand() && performer.getPower() >= 4)) {
               return toggleRenderWall(performer, source, wall, act, counter);
            }

            if (action == 848 && wall.isLRArch() && (source.getTemplateId() == 1115 || source.isWand() && performer.getPower() >= 4)) {
               return toggleLeftRightArch(performer, source, wall, act, counter);
            }

            if (!act.isBuildHouseWallAction()) {
               if (action == 58) {
                  int tilex = wall.getTileX();
                  int tiley = wall.getTileY();
                  MethodsStructure.tryToFinalize(performer, tilex, tiley);
               } else if (action == 57) {
                  if (this.canRemoveWallPlan(performer, wall)) {
                     wall.destroy();
                     performer.getCommunicator().sendNormalServerMessage("You remove a plan for a new wall.");
                     Server.getInstance().broadCastAction(performer.getName() + " removes a plan for a new wall.", performer, 3);
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("This would cause a section of the structure to crash down since it lacks support.");
                  }
               } else if (action == 209) {
                  done = true;
                  if (performer.getCitizenVillage() != null) {
                     if (wall.getTile() != null && wall.getTile().getVillage() != null) {
                        if (performer.getCitizenVillage().mayDeclareWarOn(wall.getTile().getVillage())) {
                           Methods.sendWarDeclarationQuestion(performer, wall.getTile().getVillage());
                        } else {
                           performer.getCommunicator().sendAlertServerMessage(wall.getTile().getVillage().getName() + " is already at war with your village.");
                        }
                     }
                  } else {
                     performer.getCommunicator().sendAlertServerMessage("You are no longer a citizen of a village.");
                  }
               } else if (action == 161 && source.isLock() && source.getTemplateId() == 167) {
                  if (source.isLocked()) {
                     performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " is already in use.");
                     return true;
                  }

                  if (wall.getType() != StructureTypeEnum.DOOR
                     && wall.getType() != StructureTypeEnum.DOUBLE_DOOR
                     && wall.getType() != StructureTypeEnum.PORTCULLIS
                     && wall.getType() != StructureTypeEnum.CANOPY_DOOR) {
                     performer.getCommunicator().sendNormalServerMessage("You can only attach locks to doors and fence gates.");
                  } else {
                     done = false;
                     Skill carpentry = null;

                     try {
                        carpentry = performer.getSkills().getSkill(1005);
                     } catch (NoSuchSkillException var43) {
                        carpentry = performer.getSkills().learn(1005, 1.0F);
                     }

                     int time = 10;
                     if (counter == 1.0F) {
                        if (structure != null) {
                           if (!structure.mayModify(performer)) {
                              return true;
                           }
                        } else {
                           logger.log(Level.WARNING, "This wall has no structure: " + wall.getId());
                           performer.getCommunicator().sendNormalServerMessage("This wall has a problem with its data. Please report this.");
                        }

                        time = (int)Math.max(100.0, (100.0 - carpentry.getKnowledge(source, 0.0)) * 3.0);

                        try {
                           performer.getCurrentAction().setTimeLeft(time);
                        } catch (NoSuchActionException var42) {
                           logger.log(Level.INFO, "This action does not exist?", (Throwable)var42);
                        }

                        performer.getCommunicator().sendNormalServerMessage("You start to attach the lock.");
                        Server.getInstance().broadCastAction(performer.getName() + " starts to attach a lock.", performer, 5);
                        performer.sendActionControl(Actions.actionEntrys[161].getVerbString(), true, time);
                     } else {
                        try {
                           time = performer.getCurrentAction().getTimeLeft();
                        } catch (NoSuchActionException var41) {
                           logger.log(Level.INFO, "This action does not exist?", (Throwable)var41);
                        }

                        if (counter * 10.0F > (float)time) {
                           carpentry.skillCheck((double)(100.0F - source.getCurrentQualityLevel()), 0.0, false, counter);
                           done = true;
                           if (structure != null) {
                              long parentId = source.getParentId();
                              if (parentId != -10L) {
                                 try {
                                    Items.getItem(parentId).dropItem(source.getWurmId(), false);
                                 } catch (NoSuchItemException var40) {
                                    logger.log(Level.INFO, performer.getName() + " tried to attach nonexistant lock or lock with no parent.");
                                 }
                              } else {
                                 logger.log(Level.INFO, performer.getName() + " tried to attach lock with no parent.");
                                 performer.getCommunicator().sendNormalServerMessage("You may not use that lock.");
                              }

                              source.addKey(structure.getWritId());
                              Door[] doors = structure.getAllDoors();

                              for(int x = 0; x < doors.length; ++x) {
                                 try {
                                    if (doors[x].getWall() == wall) {
                                       if (!doors[x].isNotLockable()) {
                                          try {
                                             Item oldlock = doors[x].getLock();
                                             oldlock.removeKey(structure.getWritId());
                                             oldlock.unlock();
                                             performer.getInventory().insertItem(oldlock);
                                          } catch (NoSuchLockException var39) {
                                          }

                                          doors[x].setLock(source.getWurmId());
                                          source.lock();
                                          PermissionsHistories.addHistoryEntry(
                                             doors[x].getWurmId(),
                                             System.currentTimeMillis(),
                                             performer.getWurmId(),
                                             performer.getName(),
                                             "Attached lock to door"
                                          );
                                          Server.getInstance().broadCastAction(performer.getName() + " attaches the lock.", performer, 5);
                                          performer.getCommunicator().sendNormalServerMessage("You attach the lock and lock the door.");
                                       }
                                       break;
                                    }
                                 } catch (NoSuchWallException var49) {
                                    logger.log(Level.WARNING, "No inner wall");
                                 }
                              }
                           } else {
                              logger.log(Level.WARNING, "This wall has no structure: " + wall.getId());
                              performer.getCommunicator().sendNormalServerMessage("This wall has a problem with its data. Please report this.");
                           }
                        }
                     }
                  }
               } else if (action == 101) {
                  if ((wall.isOnPvPServer() || Servers.isThisATestServer())
                     && (
                        wall.getType() == StructureTypeEnum.DOOR
                           || wall.getType() == StructureTypeEnum.DOUBLE_DOOR
                           || wall.getType() == StructureTypeEnum.PORTCULLIS
                           || wall.getType() == StructureTypeEnum.CANOPY_DOOR
                     )
                     && wall.isFinished()
                     && !wall.isNotLockpickable()) {
                     if (structure != null) {
                        Door[] doors = structure.getAllDoors();

                        for(int x = 0; x < doors.length; ++x) {
                           try {
                              if (doors[x].getWall() == wall) {
                                 done = false;
                                 done = MethodsStructure.picklock(performer, source, doors[x], wall.getName(), counter, act);
                                 break;
                              }
                           } catch (NoSuchWallException var48) {
                              logger.log(Level.WARNING, "No inner wall");
                           }
                        }
                     } else {
                        logger.log(Level.WARNING, "This wall has no structure: " + wall.getId());
                        performer.getCommunicator().sendNormalServerMessage("This wall has a problem with its data. Please report this.");
                     }
                  }
               } else if (action == 193) {
                  if ((!Servers.localServer.challengeServer || performer.getEnemyPresense() <= 0) && !wall.isNoRepair()) {
                     done = MethodsStructure.repairWall(act, performer, source, wall, counter);
                  } else {
                     done = true;
                  }
               } else if (action == 192) {
                  if (source != null && !wall.isNoImprove()) {
                     done = MethodsStructure.improveWall(act, performer, source, wall, counter);
                  } else {
                     done = true;
                  }
               } else if (action == 180) {
                  if (performer.getPower() >= 2) {
                     performer.getLogger().log(Level.INFO, performer.getName() + " destroyed a wall at " + wall.getTileX() + ", " + wall.getTileY());
                     wall.setDamage(100.0F);
                     done = true;
                     performer.getCommunicator().sendNormalServerMessage("You deal a lot of damage to the wall!");
                  }
               } else if (action == 174 && !wall.isIndestructible()) {
                  if (wall.isRubble()) {
                     performer.getCommunicator().sendNormalServerMessage("The rubble will clear by itself soon.");
                     return true;
                  }

                  int tilex = wall.getStartX();
                  int tiley = wall.getStartY();
                  VolaTile wallTile = null;

                  for(int xx = 1; xx >= -1; --xx) {
                     for(int yy = 1; yy >= -1; --yy) {
                        try {
                           Zone zone = Zones.getZone(tilex + xx, tiley + yy, wall.isOnSurface());
                           VolaTile tile = zone.getTileOrNull(tilex + xx, tiley + yy);
                           if (tile != null) {
                              Wall[] walls = tile.getWalls();

                              for(int s = 0; s < walls.length; ++s) {
                                 if (walls[s].getId() == wall.getId()) {
                                    wallTile = tile;
                                    break;
                                 }
                              }
                           }
                        } catch (NoSuchZoneException var47) {
                        }
                     }
                  }

                  if (wallTile == null) {
                     performer.getCommunicator().sendNormalServerMessage("You fail to destroy the wall.");
                     return true;
                  }

                  done = MethodsStructure.destroyWall(action, performer, source, wall, false, counter);
               } else if (action == 231) {
                  if (!wall.isFinished()) {
                     performer.getCommunicator().sendNormalServerMessage("Finish the wall first.");
                     return true;
                  }

                  if (!Methods.isActionAllowed(performer, action, wall.getTileX(), wall.getTileY())) {
                     performer.getCommunicator().sendNormalServerMessage("You are not allowed to paint this wall.");
                     return true;
                  }

                  if (wall.isNotPaintable()) {
                     performer.getCommunicator().sendNormalServerMessage("You are not allowed to paint this wall.");
                     return true;
                  }

                  done = MethodsStructure.colorWall(performer, source, wall, act);
               } else if (action == 232) {
                  if (!Methods.isActionAllowed(performer, action, wall.getTileX(), wall.getTileY())) {
                     performer.getCommunicator().sendNormalServerMessage("You are not allowed to remove the paint from this wall.");
                     return true;
                  }

                  done = MethodsStructure.removeColor(performer, source, wall, act);
               } else if (action == 82) {
                  DemolishCheckQuestion dcq = new DemolishCheckQuestion(performer, "Demolish Building", "A word of warning!", wall.getStructureId());
                  dcq.sendQuestion();
               } else {
                  if (action == 662) {
                     if (performer.getPower() >= 2) {
                        wall.setIndoor(!wall.isIndoor());
                        performer.getCommunicator().sendNormalServerMessage("Wall toggled and now is " + (wall.isIndoor() ? "Inside" : "Outside"));
                        if (structure != null) {
                           structure.updateStructureFinishFlag();
                        } else {
                           performer.getCommunicator().sendNormalServerMessage("The structural integrity of the building is at risk.");
                           logger.log(
                              Level.WARNING, "Structure not found while trying to toggle a wall at [" + wall.getStartX() + "," + wall.getStartY() + "]"
                           );
                        }
                     }

                     return true;
                  }

                  if (action == 78) {
                     if (performer.getPower() >= 2) {
                        try {
                           Structure struct = Structures.getStructure(wall.getStructureId());

                           try {
                              Items.getItem(struct.getWritId());
                              performer.getCommunicator().sendNormalServerMessage("Writ item exists for structure.");
                           } catch (NoSuchItemException var37) {
                              performer.getCommunicator().sendNormalServerMessage("Writ item does not exist for structure. Replacing.");

                              try {
                                 Item newWrit = ItemFactory.createItem(166, 80.0F + Server.rand.nextFloat() * 20.0F, performer.getName());
                                 newWrit.setDescription(struct.getName());
                                 performer.getInventory().insertItem(newWrit);
                                 struct.setWritid(newWrit.getWurmId(), true);
                              } catch (NoSuchTemplateException var35) {
                                 performer.getCommunicator().sendNormalServerMessage("Failed replace:" + var35.getMessage());
                              } catch (FailedException var36) {
                                 performer.getCommunicator().sendNormalServerMessage("Failed replace:" + var36.getMessage());
                              }
                           }
                        } catch (NoSuchStructureException var38) {
                           logger.log(Level.WARNING, var38.getMessage(), (Throwable)var38);
                           performer.getCommunicator().sendNormalServerMessage("No such structure. Bug. Good luck.");
                        }
                     }
                  } else if (action == 472) {
                     done = true;
                     if (source.getTemplateId() == 676 && source.getOwnerId() == performer.getWurmId()) {
                        MissionManager m = new MissionManager(performer, "Manage missions", "Select action", wall.getId(), wall.getName(), source.getWurmId());
                        m.sendQuestion();
                     }
                  } else if (action == 90) {
                     if (performer.getPower() < 4) {
                        logger.log(
                           Level.WARNING,
                           "Possible hack attempt by " + performer.getName() + " calling Actions.POLL on wall in WallBehaviour without enough power."
                        );
                        return true;
                     }

                     int tilex = wall.getStartX();
                     int tiley = wall.getStartY();
                     VolaTile wallTile = Zones.getOrCreateTile(tilex, tiley, true);
                     if (wallTile != null) {
                        Structure struct = null;

                        try {
                           struct = Structures.getStructure(wall.getStructureId());
                        } catch (NoSuchStructureException var34) {
                           logger.log(Level.WARNING, var34.getMessage(), (Throwable)var34);
                        }

                        if (struct == null) {
                           performer.getCommunicator().sendNormalServerMessage("Couldn't find structure for wall '" + wall.getId() + "'.");
                           return true;
                        }

                        wall.poll(struct.getCreationDate() + 604800000L, wallTile, struct);
                        performer.getCommunicator().sendNormalServerMessage("Poll performed for wall '" + wall.getId() + "'.");
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("Unexpectedly missing a tile for " + tilex + "," + tiley + ".");
                     }
                  } else if (action == 664) {
                     this.manageBuilding(performer, structure, wall);
                  } else if (action == 666) {
                     this.manageDoor(performer, structure, wall);
                  } else if (action == 673) {
                     this.manageAllDoors(performer, structure, wall);
                  } else if (action == 102 && mayLockDoor(performer, wall, door)) {
                     if (door != null && door.hasLock() && door.isLocked() && !door.isNotLockable()) {
                        door.unlock(true);
                        PermissionsHistories.addHistoryEntry(
                           door.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), "Unlocked door"
                        );
                     }
                  } else if (action == 28 && mayLockDoor(performer, wall, door)) {
                     if (door != null && door.hasLock() && !door.isLocked() && !door.isNotLockable()) {
                        door.lock(true);
                        PermissionsHistories.addHistoryEntry(
                           door.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), "Locked door"
                        );
                     }
                  } else {
                     if (action == 866) {
                        if (performer.getPower() >= 4) {
                           Methods.sendGmBuildAllWallsQuestion(performer, structure);
                        }

                        return true;
                     }

                     if (action == 684) {
                        if ((source.getTemplateId() == 315 || source.getTemplateId() == 176) && performer.getPower() >= 2) {
                           Methods.sendItemRestrictionManagement(performer, wall, wall.getId());
                        } else {
                           logger.log(
                              Level.WARNING,
                              performer.getName() + " hacking the protocol by trying to set the restrictions of " + wall + ", counter: " + counter + '!'
                           );
                        }

                        return true;
                     }

                     if (source.isTrellis() && (action == 176 || action == 746 || action == 747)) {
                        done = Terraforming.plantTrellis(
                           performer, source, wall.getMinX(), wall.getMinY(), wall.isOnSurface(), wall.getDir(), action, counter, act
                        );
                     }
                  }
               }
            } else {
               WallEnum targetWallType = WallEnum.getWallByActionId(action);
               done = false;
               String buildString = "wall";
               if (act.isBuildWindowAction()) {
                  buildString = "window";
               } else if (act.isBuildDoorAction()) {
                  buildString = "door";
               } else if (act.isBuildDoubleDoorAction()) {
                  buildString = "double door";
               } else if (act.isBuildArchedWallAction()) {
                  buildString = "arched wall";
               } else if (act.isBuildPortcullisAction()) {
                  buildString = "portcullis";
               } else if (act.isBuildBarredWall()) {
                  buildString = "barred wall";
               } else if (act.isBuildBalcony()) {
                  buildString = "balcony";
               } else if (act.isBuildJetty()) {
                  buildString = "jetty";
               } else if (act.isBuildOriel()) {
                  buildString = "oriel";
               } else if (act.isBuildCanopyDoor()) {
                  buildString = "canopy";
               }

               int tilex = wall.getStartX();
               int tiley = wall.getStartY();
               VolaTile wallTile = null;
               Wall orig = null;
               boolean usesRightItem = false;
               if (MethodsStructure.isCorrectToolForBuilding(performer, source.getTemplateId()) && targetWallType.isCorrectToolForType(source, performer)) {
                  usesRightItem = true;
               }

               if (!usesRightItem) {
                  performer.getCommunicator().sendNormalServerMessage("You can't use that.");
                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               if (structure == null) {
                  logger.log(Level.WARNING, "Structure with id " + wall.getStructureId() + " not found!");
                  performer.getCommunicator().sendActionResult(false);
                  return done;
               }

               boolean hasMarker = StructureBehaviour.hasMarker(tilex, tiley, wall.isOnSurface(), wall.getDir(), wall.getHeight());
               if (hasMarker
                  && targetWallType.getType() != StructureTypeEnum.ARCHED_LEFT
                  && targetWallType.getType() != StructureTypeEnum.ARCHED_RIGHT
                  && targetWallType.getType() != StructureTypeEnum.ARCHED_T) {
                  performer.getCommunicator().sendNormalServerMessage("You can't build those over a highway.");
                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               if (wall.getType() == StructureTypeEnum.PLAN
                  && structure.needsDoor()
                  && !act.isBuildDoorAction()
                  && !act.isBuildDoubleDoorAction()
                  && !act.isBuildArchedWallAction()
                  && !act.isBuildPortcullisAction()
                  && !act.isBuildCanopyDoor()) {
                  performer.getCommunicator().sendNormalServerMessage("Houses need at least one door. Build a door first.");
                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               for(int xx = 1; xx >= -1; --xx) {
                  for(int yy = 1; yy >= -1; --yy) {
                     try {
                        Zone zone = Zones.getZone(tilex + xx, tiley + yy, performer.isOnSurface());
                        VolaTile tile = zone.getTileOrNull(tilex + xx, tiley + yy);
                        if (tile != null) {
                           Wall[] walls = tile.getWalls();

                           for(int s = 0; s < walls.length; ++s) {
                              if (walls[s].getId() == wall.getId()) {
                                 wallTile = tile;
                                 orig = walls[s];
                                 if (tile.getStructure() != null && !tile.getStructure().isFinalized()) {
                                    performer.getCommunicator().sendNormalServerMessage("You need to finalize the build plan before you start building.");
                                    performer.getCommunicator().sendActionResult(false);
                                    return done;
                                 }
                                 break;
                              }
                           }
                        }
                     } catch (NoSuchZoneException var52) {
                     }
                  }
               }

               if (orig == null) {
                  performer.getCommunicator().sendNormalServerMessage("No structure is planned there at the moment.");
                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               if (orig.isFinished()) {
                  performer.getCommunicator().sendNormalServerMessage("You need to destroy the " + orig.getName() + " before modifying it.");
                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               if (!MethodsStructure.mayModifyStructure(performer, structure, wallTile, action)) {
                  performer.getCommunicator().sendNormalServerMessage("You need permission in order to make modifications to this structure.");
                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               StructureMaterialEnum material = targetWallType.getMaterial();
               StructureTypeEnum actionType = targetWallType.getType();
               int primskillTemplate = targetWallType.getSkillNumber();
               if (StructureStateEnum.INITIALIZED != wall.getState() && StructureStateEnum.FINISHED != wall.getState()) {
                  if (material != wall.getMaterial()) {
                     if (source.getTemplateId() != 176 || !WurmPermissions.mayUseGMWand(performer)) {
                        performer.getCommunicator().sendNormalServerMessage("You may not change the material of the wall now that you are building it.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                     }

                     material = wall.getMaterial();
                     performer.getCommunicator()
                        .sendNormalServerMessage("You use the power of your " + source.getName() + " to change the material of the wall!");
                  }

                  if (wall.getType() != actionType) {
                     if (source.getTemplateId() != 176 || !WurmPermissions.mayUseGMWand(performer)) {
                        performer.getCommunicator().sendNormalServerMessage("You may not change the type of wall now that you are building it.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                     }

                     wall.setType(actionType);
                     performer.getCommunicator()
                        .sendNormalServerMessage("You use the power of your " + source.getName() + " to change the structure of the wall!");
                  }
               } else if (StructureStateEnum.INITIALIZED == wall.getState()) {
                  wall.setMaterial(material);
               }

               Skill carpentry = null;
               Skill hammer = null;

               try {
                  carpentry = performer.getSkills().getSkill(primskillTemplate);
                  if (primskillTemplate == 1013 && carpentry.getKnowledge(0.0) < 30.0) {
                     performer.getCommunicator().sendNormalServerMessage("You need at least 30 masonry to build stone house walls.");
                     performer.getCommunicator().sendActionResult(false);
                     return true;
                  }
               } catch (NoSuchSkillException var51) {
                  if (primskillTemplate == 1013) {
                     performer.getCommunicator().sendNormalServerMessage("You need at least 30 masonry to build stone house walls.");
                     performer.getCommunicator().sendActionResult(false);
                     return true;
                  }

                  carpentry = performer.getSkills().learn(primskillTemplate, 1.0F);
               }

               if ((double)FloorBehaviour.getRequiredBuildSkillForFloorLevel(wall.getFloorLevel(), false) > carpentry.getKnowledge(0.0)) {
                  performer.getCommunicator().sendNormalServerMessage("Construction of walls is reserved for craftsmen of higher rank than yours.");
                  if (Servers.localServer.testServer) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "You have "
                              + carpentry.getKnowledge(0.0)
                              + " and need "
                              + FloorBehaviour.getRequiredBuildSkillForFloorLevel(wall.getFloorLevel(), false)
                        );
                  }

                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               try {
                  hammer = performer.getSkills().getSkill(source.getPrimarySkill());
               } catch (NoSuchSkillException var46) {
                  try {
                     hammer = performer.getSkills().learn(source.getPrimarySkill(), 1.0F);
                  } catch (NoSuchSkillException var45) {
                  }
               }

               int time = 10;
               double bonus = 0.0;
               StructureTypeEnum oldType = orig.getType();
               boolean immediate = source.getTemplateId() == 176 && WurmPermissions.mayUseGMWand(performer)
                  || source.getTemplateId() == 315 && performer.getPower() >= 2 && Servers.isThisATestServer();
               if (oldType == actionType && orig.isFinished()) {
                  performer.getCommunicator().sendNormalServerMessage("The wall is finished already.");
                  performer.getCommunicator().sendActionResult(false);
                  return true;
               }

               if (counter == 1.0F && !immediate) {
                  time = Actions.getSlowActionTime(performer, carpentry, source, 0.0);
                  if (checkWallItem2(performer, wall, buildString, time, act)) {
                     performer.getCommunicator().sendActionResult(false);
                     return true;
                  }

                  act.setTimeLeft(time);
                  if (oldType == actionType) {
                     performer.getCommunicator().sendNormalServerMessage("You continue to build a " + buildString + ".");
                     Server.getInstance().broadCastAction(performer.getName() + " continues to build a " + buildString + ".", performer, 5);
                  }

                  performer.sendActionControl("Building " + buildString, true, time);
                  performer.getStatus().modifyStamina(-1000.0F);
                  if (source.getTemplateId() == 63) {
                     source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
                  } else if (source.getTemplateId() == 62) {
                     source.setDamage(source.getDamage() + 3.0E-4F * source.getDamageModifier());
                  } else if (source.getTemplateId() == 493) {
                     source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
                  }
               } else {
                  time = act.getTimeLeft();
                  if (Math.abs(performer.getPosX() - (float)(wall.getEndX() << 2)) > 8.0F
                     || Math.abs(performer.getPosX() - (float)(wall.getStartX() << 2)) > 8.0F
                     || Math.abs(performer.getPosY() - (float)(wall.getEndY() << 2)) > 8.0F
                     || Math.abs(performer.getPosY() - (float)(wall.getStartY() << 2)) > 8.0F) {
                     performer.getCommunicator().sendAlertServerMessage("You are too far from the end.");
                     performer.getCommunicator().sendActionResult(false);
                     return true;
                  }

                  if (act.currentSecond() % 5 == 0) {
                     if (!wall.isStone()
                        && !wall.isPlainStone()
                        && !wall.isSlate()
                        && !wall.isRoundedStone()
                        && !wall.isPottery()
                        && !wall.isSandstone()
                        && !wall.isMarble()) {
                        SoundPlayer.playSound(
                           Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2",
                           tilex,
                           tiley,
                           performer.isOnSurface(),
                           1.6F
                        );
                     } else {
                        SoundPlayer.playSound("sound.work.masonry", tilex, tiley, performer.isOnSurface(), 1.6F);
                     }

                     performer.getStatus().modifyStamina(-10000.0F);
                     if (source.getTemplateId() == 63) {
                        source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
                     } else if (source.getTemplateId() == 62) {
                        source.setDamage(source.getDamage() + 3.0E-4F * source.getDamageModifier());
                     } else if (source.getTemplateId() == 493) {
                        source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
                     }
                  }
               }

               if (counter * 10.0F > (float)time || immediate) {
                  if (!immediate && !depleteWallItems2(performer, wall, act)) {
                     performer.getCommunicator().sendActionResult(false);
                     return true;
                  }

                  if (hammer != null) {
                     hammer.skillCheck(10.0, source, 0.0, false, counter);
                     bonus = hammer.getKnowledge(source, 0.0) / 10.0;
                  }

                  carpentry.skillCheck(10.0, source, bonus, false, counter);
                  done = true;

                  try {
                     float oldql = wall.getQualityLevel();
                     float qlevel = MethodsStructure.calculateNewQualityLevel(act.getPower(), carpentry.getKnowledge(0.0), oldql, wall.getFinalState().state);
                     qlevel = Math.max(1.0F, qlevel);
                     if (immediate) {
                        qlevel = 50.0F;
                     }

                     boolean updateOrig = false;
                     if (oldType != actionType) {
                        orig.setType(actionType);
                        orig.setDamage(0.0F);
                        qlevel = MethodsStructure.calculateNewQualityLevel(act.getPower(), carpentry.getKnowledge(0.0), 0.0F, wall.getFinalState().state);
                        orig.setState(StructureStateEnum.INITIALIZED);
                        updateOrig = true;
                     }

                     StructureStateEnum oldState = orig.getState();
                     StructureStateEnum state = oldState;
                     if (oldState.state < 127) {
                        state = StructureStateEnum.getStateByValue((byte)(oldState.state + 1));
                        if (WurmPermissions.mayUseGMWand(performer)
                           && (source.getTemplateId() == 315 || source.getTemplateId() == 176)
                           && Servers.isThisATestServer()) {
                           state = StructureStateEnum.FINISHED;
                           qlevel = 80.0F;
                        } else if (performer.getPower() >= 4 && source.getTemplateId() == 176) {
                           state = StructureStateEnum.FINISHED;
                           qlevel = 80.0F;
                        }
                     }

                     orig.setState(state);
                     orig.setQualityLevel(qlevel);
                     orig.setDamage(0.0F);
                     orig.setMaterial(material);
                     if (updateOrig || orig.isFinished()) {
                        wallTile.updateWall(orig);
                        if (performer.getDeity() != null && performer.getDeity().number == 3) {
                           performer.maybeModifyAlignment(1.0F);
                        }

                        if (wall.isFinished() && (wall.isStone() || wall.isPlainStone())) {
                           performer.achievement(525);
                        }

                        if (wall.isFinished() && wall.getFloorLevel() == 1) {
                           performer.achievement(539);
                        }
                     }

                     if (orig.isFinished()) {
                        performer.getCommunicator().sendRemoveFromCreationWindow(orig.getId());
                     } else {
                        performer.getCommunicator().sendAddWallToCreationWindow(wall, orig.getId());
                     }

                     if (wall.isHalfArch() && oldState == StructureStateEnum.INITIALIZED) {
                        String beam = !wall.isWood() && !wall.isTimberFramed() ? "an iron bar" : "a beam";
                        Server.getInstance().broadCastAction(performer.getName() + " add " + beam + " as reinforcement to the arch.", performer, 5);
                        performer.getCommunicator().sendNormalServerMessage("You add " + beam + " as reinforcement to the arch.");
                     } else if (wall.isWood()) {
                        Server.getInstance().broadCastAction(performer.getName() + " nails a plank to the wall.", performer, 5);
                        performer.getCommunicator().sendNormalServerMessage("You nail a plank to the wall.");
                     } else if (wall.isTimberFramed()) {
                        if (state.state < 7) {
                           Server.getInstance().broadCastAction(performer.getName() + " affixes a beam to the frame.", performer, 5);
                           performer.getCommunicator().sendNormalServerMessage("You affix a beam to the frame.");
                        } else if (state.state < 17) {
                           Server.getInstance().broadCastAction(performer.getName() + " adds some clay and mixed grass to the wall.", performer, 5);
                           performer.getCommunicator().sendNormalServerMessage("You add some clay and mixed grass to the wall.");
                        } else {
                           Server.getInstance().broadCastAction(performer.getName() + " reinforces the wall with more clay.", performer, 5);
                           performer.getCommunicator().sendNormalServerMessage("You reinforce the wall with more clay.");
                        }
                     } else {
                        String brickType = wall.getBrickName();
                        Server.getInstance().broadCastAction(performer.getName() + " adds a " + brickType + " and some mortar to the wall.", performer, 5);
                        performer.getCommunicator().sendNormalServerMessage("You add a " + brickType + " and some mortar to the wall.");
                     }

                     performer.getCommunicator().sendActionResult(true);

                     try {
                        orig.save();
                     } catch (IOException var44) {
                        logger.log(Level.WARNING, "Failed to save wall with id " + orig.getId());
                     }

                     if ((!structure.isFinished() || !structure.isFinalFinished()) && structure.updateStructureFinishFlag()) {
                        performer.achievement(216);
                        if (!structure.isOnSurface()) {
                           performer.achievement(571);
                        }
                     }

                     if (oldType == StructureTypeEnum.DOOR || oldType == StructureTypeEnum.DOUBLE_DOOR || oldType == StructureTypeEnum.CANOPY_DOOR) {
                        Door[] doors = structure.getAllDoors();

                        for(int x = 0; x < doors.length; ++x) {
                           if (doors[x].getWall() == wall) {
                              structure.removeDoor(doors[x]);
                              doors[x].removeFromTiles();
                           }
                        }
                     }

                     if ((act.isBuildDoorAction() || act.isBuildDoubleDoorAction() || act.isBuildPortcullisAction() || act.isBuildCanopyDoor())
                        && orig.isFinished()) {
                        Door newDoor = new DbDoor(orig);
                        newDoor.setStructureId(structure.getWurmId());
                        structure.addDoor(newDoor);
                        newDoor.setIsManaged(true, (Player)performer);
                        newDoor.save();
                        newDoor.addToTiles();
                     }
                  } catch (Exception var50) {
                     logger.log(Level.WARNING, "Error when building wall:", (Throwable)var50);
                     performer.getCommunicator().sendNormalServerMessage("An error occured on the server when building wall. Please tell the administrators.");
                     performer.getCommunicator().sendActionResult(false);
                  }
               }
            }
         }

         return done;
      }
   }

   static final int getItemTemplateWeightInGrams(int itemTemplateId) {
      int neededTemplateWeightGrams;
      try {
         neededTemplateWeightGrams = ItemTemplateFactory.getInstance().getTemplate(itemTemplateId).getWeightGrams();
      } catch (NoSuchTemplateException var3) {
         switch(itemTemplateId) {
            case 22:
               neededTemplateWeightGrams = 2000;
               break;
            case 132:
               neededTemplateWeightGrams = 150000;
               break;
            case 217:
               neededTemplateWeightGrams = 300;
               break;
            case 492:
               neededTemplateWeightGrams = 2000;
               break;
            default:
               neededTemplateWeightGrams = 150000;
         }
      }

      return neededTemplateWeightGrams;
   }

   static final boolean toggleRenderWall(Creature performer, Item tool, Wall wall, Action act, float counter) {
      boolean insta = tool.isWand() && performer.getPower() >= 4;
      VolaTile wallTile = getWallTile(wall);
      if (wallTile == null) {
         return true;
      } else {
         Structure structure = wallTile.getStructure();
         if (!insta && structure != null && !MethodsStructure.mayModifyStructure(performer, structure, wallTile, (short)683)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to modify the structure.");
            return true;
         } else if (!Methods.isActionAllowed(performer, (short)116, wallTile.getTileX(), wallTile.getTileY())) {
            return true;
         } else if (wall.isPlainStone() && !insta && tool.getWeightGrams() < 20000) {
            performer.getCommunicator().sendNormalServerMessage("It takes 20kg of " + tool.getName() + " to render the " + wall.getName() + ".");
            return true;
         } else {
            int time = 40;
            if (counter == 1.0F) {
               String render = wall.isPlainStone() ? "render" : "remove the render from";
               String action = wall.isPlainStone() ? "rendering wall" : "removing the wall render";
               act.setTimeLeft(time);
               performer.sendActionControl(action, true, time);
               performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You start to " + render + " the %s.", wall.getName()));
               Server.getInstance()
                  .broadCastAction(StringUtil.format("%s starts to " + render + " the %s.", performer.getName(), wall.getName()), performer, 5);
               return false;
            } else {
               time = act.getTimeLeft();
               if (!(counter * 10.0F > (float)time) && !insta) {
                  return false;
               } else {
                  String render = wall.isPlainStone() ? "render" : "remove the render from";
                  String renders = wall.isPlainStone() ? "renders" : "removes the render from";
                  performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You " + render + " the %s.", wall.getName()));
                  Server.getInstance().broadCastAction(StringUtil.format("%s " + renders + " the %s.", performer.getName(), wall.getName()), performer, 5);
                  if (wall.isPlainStone() && !insta) {
                     tool.setWeight(tool.getWeightGrams() - 20000, true);
                  }

                  if (wall.isPlainStone()) {
                     wall.setMaterial(StructureMaterialEnum.RENDERED);
                  } else {
                     wall.setMaterial(StructureMaterialEnum.PLAIN_STONE);
                  }

                  try {
                     wall.save();
                  } catch (IOException var12) {
                     logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
                  }

                  wallTile.updateWall(wall);
                  return true;
               }
            }
         }
      }
   }

   static final boolean toggleLeftRightArch(Creature performer, Item tool, Wall wall, Action act, float counter) {
      boolean insta = tool.isWand() && performer.getPower() >= 4;
      VolaTile wallTile = getWallTile(wall);
      if (wallTile == null) {
         return true;
      } else {
         Structure structure = wallTile.getStructure();
         if (!insta && !MethodsStructure.mayModifyStructure(performer, structure, wallTile, (short)683)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to modify the structure.");
            return true;
         } else if (!Methods.isActionAllowed(performer, (short)116, wallTile.getTileX(), wallTile.getTileY())) {
            return true;
         } else {
            int time = 40;
            if (counter == 1.0F) {
               act.setTimeLeft(time);
               performer.sendActionControl("Moving Arch", true, time);
               performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You start to move the %s.", wall.getName()));
               Server.getInstance().broadCastAction(StringUtil.format("%s starts to move the %s.", performer.getName(), wall.getName()), performer, 5);
               return false;
            } else {
               time = act.getTimeLeft();
               if (!(counter * 10.0F > (float)time) && !insta) {
                  return false;
               } else {
                  performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You move the %s.", wall.getName()));
                  Server.getInstance().broadCastAction(StringUtil.format("%s move the %s.", performer.getName(), wall.getName()), performer, 5);
                  if (wall.getType() == StructureTypeEnum.ARCHED_LEFT) {
                     wall.setType(StructureTypeEnum.ARCHED_RIGHT);
                  } else {
                     wall.setType(StructureTypeEnum.ARCHED_LEFT);
                  }

                  try {
                     wall.save();
                  } catch (IOException var10) {
                     logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
                  }

                  wallTile.updateWall(wall);
                  return true;
               }
            }
         }
      }
   }

   static final boolean rotateWall(Creature performer, Wall wall, Action act, float counter) {
      boolean insta = performer.getPower() >= 4;
      VolaTile wallTile = getWallTile(wall);
      if (wallTile == null) {
         return true;
      } else {
         Structure structure = wallTile.getStructure();
         if (!insta && !MethodsStructure.mayModifyStructure(performer, structure, wallTile, (short)683)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to modify the structure.");
            return true;
         } else if (!Methods.isActionAllowed(performer, (short)116, wallTile.getTileX(), wallTile.getTileY())) {
            return true;
         } else {
            int time = 40;
            if (counter == 1.0F) {
               act.setTimeLeft(time);
               performer.sendActionControl("Rotating wall", true, time);
               performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You start to rotate the %s.", wall.getName()));
               Server.getInstance().broadCastAction(StringUtil.format("%s starts to rotate the %s.", performer.getName(), wall.getName()), performer, 5);
               return false;
            } else {
               time = act.getTimeLeft();
               if (!(counter * 10.0F > (float)time) && !insta) {
                  return false;
               } else {
                  performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You rotate the %s.", wall.getName()));
                  Server.getInstance().broadCastAction(StringUtil.format("%s rotates the %s.", performer.getName(), wall.getName()), performer, 5);
                  wall.setWallOrientation(!wall.getWallOrientationFlag());
                  return true;
               }
            }
         }
      }
   }

   @Nullable
   static final VolaTile getWallTile(Wall wall) {
      int tilex = wall.getStartX();
      int tiley = wall.getStartY();

      for(int xx = 1; xx >= -1; --xx) {
         for(int yy = 1; yy >= -1; --yy) {
            try {
               Zone zone = Zones.getZone(tilex + xx, tiley + yy, wall.isOnSurface());
               VolaTile tile = zone.getTileOrNull(tilex + xx, tiley + yy);
               if (tile != null) {
                  Wall[] walls = tile.getWalls();

                  for(int s = 0; s < walls.length; ++s) {
                     if (walls[s].getId() == wall.getId()) {
                        return tile;
                     }
                  }
               }
            } catch (NoSuchZoneException var9) {
            }
         }
      }

      return null;
   }

   static final boolean modifyWall(Creature performer, Item tool, Wall wall, Action act, float counter) {
      if (!isValidModifyableWall(wall, tool)) {
         return true;
      } else {
         int tilex = wall.getStartX();
         int tiley = wall.getStartY();
         VolaTile wallTile = null;

         for(int xx = 1; xx >= -1; --xx) {
            for(int yy = 1; yy >= -1; --yy) {
               try {
                  Zone zone = Zones.getZone(tilex + xx, tiley + yy, performer.isOnSurface());
                  VolaTile tile = zone.getTileOrNull(tilex + xx, tiley + yy);
                  if (tile != null) {
                     Wall[] walls = tile.getWalls();

                     for(int s = 0; s < walls.length; ++s) {
                        if (walls[s].getId() == wall.getId()) {
                           wallTile = tile;
                           if (tile.getStructure() != null && !tile.getStructure().isFinalized()) {
                              performer.getCommunicator().sendNormalServerMessage("You need to finalize the build plan before you start building.");
                              performer.getCommunicator().sendActionResult(false);
                              return true;
                           }
                           break;
                        }
                     }
                  }
               } catch (NoSuchZoneException var14) {
               }
            }
         }

         if (wallTile == null) {
            return true;
         } else {
            Structure structure = wallTile.getStructure();
            if (!MethodsStructure.mayModifyStructure(performer, structure, wallTile, act.getNumber())) {
               performer.getCommunicator().sendNormalServerMessage("You are not allowed to modify the structure.");
               return true;
            } else if (!Methods.isActionAllowed(performer, (short)116, wallTile.getTileX(), wallTile.getTileY())) {
               return true;
            } else {
               int time = 40;
               if (counter == 1.0F) {
                  String action = tool.getTemplateId() == 219 ? "removing decoration" : "adding decoration";
                  String modify = tool.getTemplateId() == 219 ? "remove the decorations from the " : "add decorations to the ";
                  act.setTimeLeft(time);
                  performer.sendActionControl(action, true, time);
                  performer.getCommunicator().sendNormalServerMessage("You start to " + modify + wall.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to " + modify + wall.getName() + ".", performer, 5);
                  return false;
               } else {
                  time = act.getTimeLeft();
                  if (counter * 10.0F > (float)time) {
                     String modify = tool.getTemplateId() == 219 ? "removed the decorations from the " : "added decorations to the ";
                     performer.getCommunicator().sendNormalServerMessage("You " + modify + wall.getName() + ".");
                     Server.getInstance().broadCastAction(performer.getName() + modify + wall.getName() + ".", performer, 5);
                     wall.setMaterial(wall.getMaterial() == StructureMaterialEnum.STONE ? StructureMaterialEnum.PLAIN_STONE : StructureMaterialEnum.STONE);
                     wallTile.updateWall(wall);
                     return true;
                  } else {
                     return false;
                  }
               }
            }
         }
      }
   }

   static final boolean checkWallItem2(Creature performer, Wall wall, String buildString, int time, Action act) {
      inventoryItems = performer.getInventory().getAllItems(false);
      bodyItems = performer.getBody().getAllItems();
      int[] neededTemplates = wall.getTemplateIdsNeededForNextState(WallEnum.getWallByActionId(act.getNumber()).getType());
      resetFoundTemplates(neededTemplates);

      for(int x = 0; x < inventoryItems.length; ++x) {
         for(int n = 0; n < neededTemplates.length; ++n) {
            if (inventoryItems[x].getTemplateId() == neededTemplates[n]) {
               int neededTemplateWeightGrams = getItemTemplateWeightInGrams(inventoryItems[x].getTemplateId());
               if (neededTemplateWeightGrams <= inventoryItems[x].getWeightGrams()) {
                  foundTemplates[n] = neededTemplates[n];
               }
            }
         }
      }

      boolean found = true;

      for(int f = 0; f < foundTemplates.length; ++f) {
         if (foundTemplates[f] == -1) {
            found = false;
         }
      }

      if (!found) {
         for(int x = 0; x < bodyItems.length; ++x) {
            for(int n = 0; n < neededTemplates.length; ++n) {
               if (bodyItems[x].getTemplateId() == neededTemplates[n]) {
                  int neededTemplateWeightGrams = getItemTemplateWeightInGrams(bodyItems[x].getTemplateId());
                  if (neededTemplateWeightGrams <= bodyItems[x].getWeightGrams()) {
                     foundTemplates[n] = neededTemplates[n];
                  }
               }
            }
         }

         found = true;

         for(int f = 0; f < foundTemplates.length; ++f) {
            if (foundTemplates[f] == -1) {
               found = false;
            }
         }
      }

      for(int n = 0; n < foundTemplates.length; ++n) {
         if (foundTemplates[n] == -1) {
            try {
               if (neededTemplates[n] == 217) {
                  performer.getCommunicator().sendNormalServerMessage("You need large iron nails.");
               } else {
                  ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(neededTemplates[n]);
                  if (template.isCombine()) {
                     performer.getCommunicator().sendNormalServerMessage("You need " + template.getName() + ".");
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You need " + template.getNameWithGenus() + ".");
                  }
               }
            } catch (NoSuchTemplateException var10) {
               logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
            }

            found = false;
         }
      }

      if (!found) {
         return true;
      } else {
         if (wall.getState() == StructureStateEnum.INITIALIZED) {
            String a_an = buildString.charAt(0) == 'a' ? "an" : "a";
            Server.getInstance().broadCastAction(performer.getName() + " starts to build " + a_an + " " + buildString + ".", performer, 5);
            performer.getCommunicator().sendNormalServerMessage("You start to build " + a_an + " " + buildString + ".");
            performer.sendActionControl("Building " + buildString, true, time);
         }

         return false;
      }
   }

   static final void resetFoundTemplates(int[] needed) {
      foundTemplates = new int[needed.length];

      for(int x = 0; x < foundTemplates.length; ++x) {
         foundTemplates[x] = -1;
      }
   }

   static final boolean depleteWallItems2(Creature performer, Wall wall, Action act) {
      inventoryItems = performer.getInventory().getAllItems(false);
      bodyItems = performer.getBody().getAllItems();
      float qlevel = 0.0F;
      int[] neededTemplates = wall.getTemplateIdsNeededForNextState(WallEnum.getWallByActionId(act.getNumber()).getType());
      resetFoundTemplates(neededTemplates);
      Item[] depleteItems = new Item[neededTemplates.length];

      for(int i = 0; i < neededTemplates.length; ++i) {
         if (foundTemplates[i] == -1) {
            for(int j = 0; j < inventoryItems.length; ++j) {
               if (inventoryItems[j].getTemplateId() == neededTemplates[i]) {
                  int neededTemplateWeightGrams = getItemTemplateWeightInGrams(inventoryItems[j].getTemplateId());
                  if (neededTemplateWeightGrams <= inventoryItems[j].getWeightGrams()) {
                     depleteItems[i] = inventoryItems[j];
                     foundTemplates[i] = neededTemplates[i];
                     break;
                  }
               }
            }
         }
      }

      boolean allInitialized = true;

      for(int i = 0; i < foundTemplates.length; ++i) {
         if (foundTemplates[i] == -1) {
            allInitialized = false;
            break;
         }
      }

      if (!allInitialized) {
         for(int i = 0; i < neededTemplates.length; ++i) {
            if (foundTemplates[i] == -1) {
               for(int j = 0; j < bodyItems.length; ++j) {
                  if (bodyItems[j].getTemplateId() == neededTemplates[i]) {
                     int neededTemplateWeightGrams = getItemTemplateWeightInGrams(bodyItems[j].getTemplateId());
                     if (neededTemplateWeightGrams <= bodyItems[j].getWeightGrams()) {
                        depleteItems[i] = bodyItems[j];
                        foundTemplates[i] = neededTemplates[i];
                        break;
                     }
                  }
               }
            }
         }

         allInitialized = true;

         for(int i = 0; i < foundTemplates.length; ++i) {
            if (foundTemplates[i] == -1) {
               allInitialized = false;
               break;
            }
         }
      }

      if (!allInitialized) {
         for(int i = 0; i < foundTemplates.length; ++i) {
            if (foundTemplates[i] == -1) {
               try {
                  ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(neededTemplates[i]);
                  performer.getCommunicator().sendNormalServerMessage("You did not have enough " + template.getPlural() + ".");
               } catch (NoSuchTemplateException var10) {
                  logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
               }
            }
         }

         return false;
      } else {
         for(int i = 0; i < depleteItems.length; ++i) {
            try {
               Items.getItem(depleteItems[i].getWurmId());
            } catch (NoSuchItemException var11) {
               performer.getCommunicator()
                  .sendAlertServerMessage("ERROR: " + depleteItems[i].getName() + " not found, WurmID: " + depleteItems[i].getWurmId());
               return false;
            }

            if (depleteItems[i].isCombine()) {
               depleteItems[i].setWeight(depleteItems[i].getWeightGrams() - depleteItems[i].getTemplate().getWeightGrams(), true);
            } else {
               Items.destroyItem(depleteItems[i].getWurmId());
            }

            qlevel += depleteItems[i].getCurrentQualityLevel() / 21.0F;
         }

         act.setPower(qlevel);
         return true;
      }
   }

   private static boolean isIndoorWallPlan(Wall aWall) {
      if (!aWall.isIndoor()) {
         return false;
      } else {
         return aWall.isWallPlan();
      }
   }

   private boolean canRemoveWallPlan(Creature aPerformer, Wall aWall) {
      if (!isIndoorWallPlan(aWall)) {
         return false;
      } else if (!Methods.isActionAllowed(aPerformer, (short)57, aWall.getTileX(), aWall.getTileY())) {
         return false;
      } else {
         try {
            Structure struct = Structures.getStructure(aWall.getStructureId());
            return !struct.wouldCreateFlyingStructureIfRemoved(aWall);
         } catch (NoSuchStructureException var4) {
            return true;
         }
      }
   }
}
