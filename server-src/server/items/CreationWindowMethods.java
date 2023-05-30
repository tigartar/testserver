package com.wurmonline.server.items;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchEntryException;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.BuildMaterial;
import com.wurmonline.server.behaviours.CaveWallBehaviour;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.structures.BridgePartEnum;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.RoofFloorEnum;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.structures.WallEnum;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.WallConstants;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CreationWindowMethods implements ProtoConstants, MiscConstants {
   private static final Logger logger = Logger.getLogger(CreationWindowMethods.class.getName());
   private static final String CHARSET_ENCODING_FOR_COMMS = "UTF-8";

   public static final boolean createWallBuildingBuffer(SocketConnection connection, @Nonnull Wall wall, @Nonnull Player player, long toolId) {
      Item tool = null;
      if (toolId != -10L) {
         Optional<Item> optTool = Items.getItemOptional(toolId);
         if (!optTool.isPresent()) {
            return false;
         }

         tool = optTool.get();
      }

      WallEnum wallEnum = WallEnum.WALL_PLAN;
      wallEnum = WallEnum.getWall(wall.getType(), wall.getMaterial());
      if (wallEnum == WallEnum.WALL_PLAN) {
         return false;
      } else {
         boolean sendNeededTool = tool == null || !WallEnum.isCorrectTool(wallEnum, player, tool);
         ByteBuffer buffer = connection.getBuffer();
         addPartialRequestHeader(buffer);
         buffer.put((byte)1);
         buffer.putShort((short)(sendNeededTool ? 2 : 1));
         if (sendNeededTool && !addToolsNeededForWall(buffer, wallEnum, player)) {
            connection.clearBuffer();
            return false;
         } else {
            addStringToBuffer(buffer, "Item(s) needed in inventory", false);
            int[] needed = WallEnum.getMaterialsNeeded(wall);
            buffer.putShort((short)(needed.length / 2));

            for(int i = 0; i < needed.length; i += 2) {
               ItemTemplate template = getItemTemplate(needed[i]);
               if (template == null) {
                  connection.clearBuffer();
                  return false;
               }

               String name = getFenceMaterialName(template);
               addStringToBuffer(buffer, name, false);
               buffer.putShort(template.getImageNumber());
               short chance = (short)needed[i + 1];
               buffer.putShort(chance);
               buffer.putShort(wallEnum.getActionId());
            }

            return true;
         }
      }
   }

   private static boolean addToolsNeededForWall(ByteBuffer buffer, WallEnum wallEnum, @Nonnull Player player) {
      addStringToBuffer(buffer, "Needed tool in crafting window", false);
      List<Integer> list = WallEnum.getToolsForWall(wallEnum, player);
      buffer.putShort((short)list.size());

      for(Integer tid : list) {
         ItemTemplate template = getItemTemplate(tid);
         if (template == null) {
            return false;
         }

         String name = getFenceMaterialName(template);
         addStringToBuffer(buffer, name, false);
         buffer.putShort(template.getImageNumber());
         short chance = 1;
         buffer.putShort((short)1);
         buffer.putShort(wallEnum.getActionId());
      }

      return true;
   }

   private static final String getFenceMaterialName(ItemTemplate template) {
      if (template.getTemplateId() == 218) {
         return "small iron " + template.getName();
      } else {
         return template.getTemplateId() == 217 ? "large iron " + template.getName() : template.getName();
      }
   }

   public static final boolean createWallPlanBuffer(
      SocketConnection connection, @Nonnull Structure structure, @Nonnull Wall wall, @Nonnull Player player, long toolId
   ) {
      if (toolId == -10L) {
         return false;
      } else {
         Optional<Item> optTool = Items.getItemOptional(toolId);
         if (!optTool.isPresent()) {
            return false;
         } else {
            Item tool = optTool.get();
            List<WallEnum> wallList = WallEnum.getWallsByTool(player, tool, structure.needsDoor(), MethodsStructure.hasInsideFence(wall));
            if (wallList.size() == 0) {
               return false;
            } else {
               ByteBuffer buffer = connection.getBuffer();
               addPartialRequestHeader(buffer);
               buffer.put((byte)0);
               buffer.putShort((short)1);
               addStringToBuffer(buffer, "Walls", false);
               buffer.putShort((short)wallList.size());

               for(WallEnum en : wallList) {
                  addStringToBuffer(buffer, en.getName(), false);
                  buffer.putShort(en.getIcon());
                  boolean canBuild = WallEnum.canBuildWall(wall, en.getMaterial(), player);
                  short chance = (short)(canBuild ? 100 : 0);
                  buffer.putShort(chance);
                  buffer.putShort(en.getActionId());
               }

               return true;
            }
         }
      }
   }

   public static final boolean createCaveCladdingBuffer(SocketConnection connection, int tilex, int tiley, int tile, byte type, Player player, long toolId) {
      Item tool = null;
      if (toolId != -10L) {
         Optional<Item> optTool = Items.getItemOptional(toolId);
         if (!optTool.isPresent()) {
            return false;
         }

         tool = optTool.get();
      }

      boolean sendNeededTool = tool == null || !CaveWallBehaviour.isCorrectTool(type, player, tool);
      ByteBuffer buffer = connection.getBuffer();
      addPartialRequestHeader(buffer);
      buffer.put((byte)1);
      buffer.putShort((short)(sendNeededTool ? 3 : 2));
      if (sendNeededTool && !addToolsNeededForWall(buffer, type, player)) {
         connection.clearBuffer();
         return false;
      } else {
         addStringToBuffer(buffer, "Item(s) needed in inventory", false);
         short action = CaveWallBehaviour.actionFromWallType(type);
         int[] needed = CaveWallBehaviour.getMaterialsNeeded(tilex, tiley, type);
         buffer.putShort((short)(needed.length / 2));

         for(int i = 0; i < needed.length; i += 2) {
            ItemTemplate template = getItemTemplate(needed[i]);
            if (template == null) {
               connection.clearBuffer();
               return false;
            }

            String name = getFenceMaterialName(template);
            addStringToBuffer(buffer, name, false);
            buffer.putShort(template.getImageNumber());
            short chance = 1;
            buffer.putShort((short)1);
            buffer.putShort(action);
         }

         addStringToBuffer(buffer, "Total materials needed", false);
         if (needed.length == 1 && needed[0] == -1) {
            buffer.putShort((short)0);
         } else {
            buffer.putShort((short)(needed.length / 2));

            for(int i = 0; i < needed.length; i += 2) {
               ItemTemplate template = getItemTemplate(needed[i]);
               String name = getFenceMaterialName(template);
               addStringToBuffer(buffer, name, false);
               buffer.putShort(template.getImageNumber());
               short chance = (short)needed[i + 1];
               buffer.putShort(chance);
               buffer.putShort(action);
            }
         }

         return true;
      }
   }

   private static boolean addToolsNeededForWall(ByteBuffer buffer, byte type, @Nonnull Player player) {
      addStringToBuffer(buffer, "Needed tool in crafting window", false);
      List<Integer> list = CaveWallBehaviour.getToolsForType(type, player);
      buffer.putShort((short)list.size());

      for(Integer tid : list) {
         ItemTemplate template = getItemTemplate(tid);
         if (template == null) {
            return false;
         }

         String name = getFenceMaterialName(template);
         addStringToBuffer(buffer, name, false);
         buffer.putShort(template.getImageNumber());
         short chance = 1;
         buffer.putShort((short)1);
         buffer.putShort(CaveWallBehaviour.actionFromWallType(type));
      }

      return true;
   }

   public static final boolean createCaveReinforcedBuffer(SocketConnection connection, Player player, long toolId) {
      if (toolId == -10L) {
         return false;
      } else {
         Optional<Item> optTool = Items.getItemOptional(toolId);
         if (!optTool.isPresent()) {
            return false;
         } else {
            Item tool = optTool.get();
            byte[] canMake = CaveWallBehaviour.getMaterialsFromToolType(player, tool);
            if (canMake.length == 0) {
               return false;
            } else {
               ByteBuffer buffer = connection.getBuffer();
               addPartialRequestHeader(buffer);
               buffer.put((byte)0);
               buffer.putShort((short)1);
               addStringToBuffer(buffer, "CaveWalls", false);
               buffer.putShort((short)canMake.length);

               for(byte type : canMake) {
                  Tiles.Tile theTile = Tiles.getTile(type);
                  addStringToBuffer(buffer, theTile.getName(), false);
                  buffer.putShort((short)theTile.getIconId());
                  boolean canBuild = CaveWallBehaviour.canCladWall(type, player);
                  short chance = (short)(canBuild ? 100 : 0);
                  buffer.putShort(chance);
                  buffer.putShort(CaveWallBehaviour.actionFromWallType(type));
               }

               return true;
            }
         }
      }
   }

   public static final boolean createHedgeCreationBuffer(SocketConnection connection, @Nonnull Item sprout, long borderId, @Nonnull Player player) {
      StructureConstantsEnum hedgeType = Fence.getLowHedgeType(sprout.getMaterial());
      if (hedgeType == StructureConstantsEnum.FENCE_PLAN_WOODEN) {
         return false;
      } else {
         int x = Tiles.decodeTileX(borderId);
         int y = Tiles.decodeTileY(borderId);
         Tiles.TileBorderDirection dir = Tiles.decodeDirection(borderId);
         Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(x, y, dir, true);
         if (structure != null) {
            return false;
         } else if (!player.isOnSurface()) {
            return false;
         } else {
            ByteBuffer buffer = connection.getBuffer();
            addPartialRequestHeader(buffer);
            buffer.put((byte)0);
            buffer.putShort((short)1);
            addStringToBuffer(buffer, "Hedges", false);
            buffer.putShort((short)1);
            String name = WallConstants.getName(hedgeType);
            addStringToBuffer(buffer, name, false);
            buffer.putShort((short)60);
            Skill gardening = player.getSkills().getSkillOrLearn(10045);
            short chance = (short)((int)gardening.getChance((double)(1.0F + sprout.getDamage()), null, (double)sprout.getQualityLevel()));
            buffer.putShort(chance);
            buffer.putShort(Actions.actionEntrys[186].getNumber());
            return true;
         }
      }
   }

   public static final boolean createFlowerbedBuffer(SocketConnection connection, @Nonnull Item tool, long borderId, @Nonnull Player player) {
      StructureConstantsEnum flowerbedType = Fence.getFlowerbedType(tool.getTemplateId());
      int x = Tiles.decodeTileX(borderId);
      int y = Tiles.decodeTileY(borderId);
      Tiles.TileBorderDirection dir = Tiles.decodeDirection(borderId);
      Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(x, y, dir, true);
      if (structure != null) {
         return false;
      } else if (!player.isOnSurface()) {
         return false;
      } else {
         ByteBuffer buffer = connection.getBuffer();
         addPartialRequestHeader(buffer);
         buffer.put((byte)0);
         buffer.putShort((short)1);
         addStringToBuffer(buffer, "Flowerbeds", false);
         buffer.putShort((short)1);
         String name = WallConstants.getName(flowerbedType);
         addStringToBuffer(buffer, name, false);
         buffer.putShort((short)60);
         Skill gardening = player.getSkills().getSkillOrLearn(10045);
         short chance = (short)((int)gardening.getChance((double)(1.0F + tool.getDamage()), null, (double)tool.getQualityLevel()));
         buffer.putShort(chance);
         buffer.putShort(Actions.actionEntrys[563].getNumber());
         return true;
      }
   }

   public static final boolean createFenceListBuffer(SocketConnection connection, long borderId) {
      int x = Tiles.decodeTileX(borderId);
      int y = Tiles.decodeTileY(borderId);
      Tiles.TileBorderDirection dir = Tiles.decodeDirection(borderId);
      int heightOffset = Tiles.decodeHeightOffset(borderId);
      boolean onSurface = true;
      boolean hasArch = false;
      if (MethodsStructure.doesTileBorderContainWallOrFence(x, y, heightOffset, dir, true, false)) {
         hasArch = true;
      }

      Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(x, y, dir, true);
      Map<String, List<ActionEntry>> fenceList = createFenceCreationList(structure != null, false, hasArch);
      if (Items.getMarker(x, y, true, 0, -10L) != null) {
         return false;
      } else if (dir == Tiles.TileBorderDirection.DIR_HORIZ && Items.getMarker(x + 1, y, true, 0, -10L) != null) {
         return false;
      } else if (dir == Tiles.TileBorderDirection.DIR_DOWN && Items.getMarker(x, y + 1, true, 0, -10L) != null) {
         return false;
      } else if (fenceList.size() == 0) {
         return false;
      } else {
         ByteBuffer buffer = connection.getBuffer();
         addPartialRequestHeader(buffer);
         buffer.put((byte)0);
         buffer.putShort((short)fenceList.size());

         for(String category : fenceList.keySet()) {
            addStringToBuffer(buffer, category, false);
            List<ActionEntry> fences = fenceList.get(category);
            buffer.putShort((short)fences.size());

            for(ActionEntry ae : fences) {
               StructureConstantsEnum type = Fence.getFencePlanType(ae.getNumber());
               String name = WallConstants.getName(Fence.getFenceForPlan(type));
               addStringToBuffer(buffer, name, false);
               buffer.putShort((short)60);
               short chance = 100;
               buffer.putShort((short)100);
               buffer.putShort(ae.getNumber());
            }
         }

         return true;
      }
   }

   private static final Map<String, List<ActionEntry>> createFenceCreationList(boolean inStructure, boolean showAll, boolean borderHasArch) {
      Map<String, List<ActionEntry>> list = new HashMap<>();
      if (!inStructure || showAll) {
         list.put("Log", new ArrayList<>());
      }

      list.put("Plank", new ArrayList<>());
      list.put("Rope", new ArrayList<>());
      list.put("Shaft", new ArrayList<>());
      list.put("Woven", new ArrayList<>());
      list.put("Stone", new ArrayList<>());
      list.put("Iron", new ArrayList<>());
      list.put("Slate", new ArrayList<>());
      list.put("Rounded stone", new ArrayList<>());
      list.put("Pottery", new ArrayList<>());
      list.put("Sandstone", new ArrayList<>());
      list.put("Marble", new ArrayList<>());
      if (!inStructure || showAll) {
         list.get("Log").add(Actions.actionEntrys[165]);
         list.get("Log").add(Actions.actionEntrys[167]);
      }

      list.get("Plank").add(Actions.actionEntrys[166]);
      list.get("Plank").add(Actions.actionEntrys[168]);
      list.get("Plank").add(Actions.actionEntrys[520]);
      list.get("Plank").add(Actions.actionEntrys[528]);
      if (inStructure && !borderHasArch || showAll) {
         list.get("Plank").add(Actions.actionEntrys[516]);
      }

      list.get("Rope").add(Actions.actionEntrys[543]);
      list.get("Rope").add(Actions.actionEntrys[544]);
      list.get("Shaft").add(Actions.actionEntrys[526]);
      list.get("Shaft").add(Actions.actionEntrys[527]);
      list.get("Shaft").add(Actions.actionEntrys[529]);
      list.get("Woven").add(Actions.actionEntrys[478]);
      if (!inStructure || showAll) {
         list.get("Stone").add(Actions.actionEntrys[163]);
      }

      if (!inStructure || !borderHasArch || showAll) {
         list.get("Stone").add(Actions.actionEntrys[164]);
      }

      if (!inStructure && !borderHasArch || showAll) {
         list.get("Stone").add(Actions.actionEntrys[654]);
      }

      list.get("Stone").add(Actions.actionEntrys[541]);
      list.get("Stone").add(Actions.actionEntrys[542]);
      if (inStructure && !borderHasArch || showAll) {
         list.get("Stone").add(Actions.actionEntrys[517]);
      }

      list.get("Iron").add(Actions.actionEntrys[477]);
      list.get("Iron").add(Actions.actionEntrys[479]);
      if (!inStructure || !borderHasArch || showAll) {
         list.get("Iron").add(Actions.actionEntrys[545]);
         list.get("Iron").add(Actions.actionEntrys[546]);
      }

      list.get("Iron").add(Actions.actionEntrys[611]);
      if (inStructure || showAll) {
         list.get("Iron").add(Actions.actionEntrys[521]);
      }

      list.get("Slate").add(Actions.actionEntrys[832]);
      list.get("Slate").add(Actions.actionEntrys[833]);
      list.get("Slate").add(Actions.actionEntrys[834]);
      if (!inStructure || !borderHasArch || showAll) {
         list.get("Slate").add(Actions.actionEntrys[870]);
      }

      if (!inStructure && !borderHasArch || showAll) {
         list.get("Slate").add(Actions.actionEntrys[871]);
      }

      if (!inStructure || !borderHasArch || showAll) {
         list.get("Slate").add(Actions.actionEntrys[872]);
         list.get("Slate").add(Actions.actionEntrys[873]);
      }

      if (inStructure && !borderHasArch || showAll) {
         list.get("Slate").add(Actions.actionEntrys[874]);
      }

      list.get("Slate").add(Actions.actionEntrys[875]);
      list.get("Rounded stone").add(Actions.actionEntrys[835]);
      list.get("Rounded stone").add(Actions.actionEntrys[836]);
      list.get("Rounded stone").add(Actions.actionEntrys[837]);
      if (!inStructure || !borderHasArch || showAll) {
         list.get("Rounded stone").add(Actions.actionEntrys[876]);
      }

      if (!inStructure && !borderHasArch || showAll) {
         list.get("Rounded stone").add(Actions.actionEntrys[877]);
      }

      if (!inStructure || !borderHasArch || showAll) {
         list.get("Rounded stone").add(Actions.actionEntrys[878]);
         list.get("Rounded stone").add(Actions.actionEntrys[879]);
      }

      if (inStructure && !borderHasArch || showAll) {
         list.get("Rounded stone").add(Actions.actionEntrys[880]);
      }

      list.get("Rounded stone").add(Actions.actionEntrys[881]);
      list.get("Pottery").add(Actions.actionEntrys[838]);
      list.get("Pottery").add(Actions.actionEntrys[839]);
      list.get("Pottery").add(Actions.actionEntrys[840]);
      if (!inStructure || !borderHasArch || showAll) {
         list.get("Pottery").add(Actions.actionEntrys[894]);
      }

      if (!inStructure && !borderHasArch || showAll) {
         list.get("Pottery").add(Actions.actionEntrys[895]);
      }

      if (!inStructure || !borderHasArch || showAll) {
         list.get("Pottery").add(Actions.actionEntrys[896]);
         list.get("Pottery").add(Actions.actionEntrys[897]);
      }

      if (inStructure && !borderHasArch || showAll) {
         list.get("Pottery").add(Actions.actionEntrys[898]);
      }

      list.get("Pottery").add(Actions.actionEntrys[899]);
      list.get("Sandstone").add(Actions.actionEntrys[841]);
      list.get("Sandstone").add(Actions.actionEntrys[842]);
      list.get("Sandstone").add(Actions.actionEntrys[843]);
      if (!inStructure || !borderHasArch || showAll) {
         list.get("Sandstone").add(Actions.actionEntrys[882]);
      }

      if (!inStructure && !borderHasArch || showAll) {
         list.get("Sandstone").add(Actions.actionEntrys[883]);
      }

      if (!inStructure || !borderHasArch || showAll) {
         list.get("Sandstone").add(Actions.actionEntrys[884]);
         list.get("Sandstone").add(Actions.actionEntrys[885]);
      }

      if (inStructure && !borderHasArch || showAll) {
         list.get("Sandstone").add(Actions.actionEntrys[886]);
      }

      list.get("Sandstone").add(Actions.actionEntrys[887]);
      list.get("Marble").add(Actions.actionEntrys[844]);
      list.get("Marble").add(Actions.actionEntrys[845]);
      list.get("Marble").add(Actions.actionEntrys[846]);
      if (!inStructure || !borderHasArch || showAll) {
         list.get("Marble").add(Actions.actionEntrys[900]);
      }

      if (!inStructure && !borderHasArch || showAll) {
         list.get("Marble").add(Actions.actionEntrys[901]);
      }

      if (!inStructure || !borderHasArch || showAll) {
         list.get("Marble").add(Actions.actionEntrys[902]);
         list.get("Marble").add(Actions.actionEntrys[903]);
      }

      if (inStructure && !borderHasArch || showAll) {
         list.get("Marble").add(Actions.actionEntrys[904]);
      }

      list.get("Marble").add(Actions.actionEntrys[905]);
      return list;
   }

   private static final List<ActionEntry> createCaveWallCreationList() {
      List<ActionEntry> list = new ArrayList<>();
      list.add(Actions.actionEntrys[856]);
      list.add(Actions.actionEntrys[857]);
      list.add(Actions.actionEntrys[858]);
      list.add(Actions.actionEntrys[859]);
      list.add(Actions.actionEntrys[860]);
      list.add(Actions.actionEntrys[861]);
      list.add(Actions.actionEntrys[862]);
      return list;
   }

   public static final boolean createCreationListBuffer(SocketConnection connection, @Nonnull Item source, @Nonnull Item target, @Nonnull Player player) {
      Map<String, Map<CreationEntry, Integer>> map = GeneralUtilities.getCreationList(source, target, player);
      if (map.size() == 0) {
         Recipe recipe = Recipes.getRecipeFor(player.getWurmId(), (byte)2, source, target, true, false);
         if (recipe == null) {
            return false;
         } else {
            ByteBuffer buffer = connection.getBuffer();
            addPartialRequestHeader(buffer);
            buffer.put((byte)0);
            buffer.putShort((short)1);
            addStringToBuffer(buffer, "Cooking", false);
            buffer.putShort((short)1);
            Item realSource = source;
            Item realTarget = target;
            if (recipe.hasActiveItem() && source != null && recipe.getActiveItem().getTemplateId() != source.getTemplateId()) {
               realSource = target;
               realTarget = source;
            }

            ItemTemplate template = recipe.getResultTemplate(realTarget);
            if (template == null) {
               connection.clearBuffer();
               return false;
            } else {
               addStringToBuffer(buffer, recipe.getSubMenuName(realTarget), false);
               buffer.putShort(template.getImageNumber());
               buffer.putShort((short)((int)recipe.getChanceFor(realSource, realTarget, player)));
               buffer.putShort(recipe.getMenuId());
               return true;
            }
         }
      } else {
         ByteBuffer buffer = connection.getBuffer();
         addPartialRequestHeader(buffer);
         buffer.put((byte)0);
         buffer.putShort((short)map.size());

         for(String category : map.keySet()) {
            addStringToBuffer(buffer, category, false);
            Map<CreationEntry, Integer> entries = map.get(category);
            buffer.putShort((short)entries.size());
            if (!addCreationEntriesToPartialList(buffer, entries)) {
               connection.clearBuffer();
               return false;
            }
         }

         return true;
      }
   }

   public static final boolean createUnfinishedCreationListBuffer(SocketConnection connection, @Nonnull Item source, @Nonnull Player player) {
      AdvancedCreationEntry entry = getAdvancedCreationEntry(source.getRealTemplateId());
      if (entry == null) {
         return false;
      } else {
         List<String> itemNames = new ArrayList<>();
         List<Integer> numberOfItemsNeeded = new ArrayList<>();
         List<Short> icons = new ArrayList<>();
         if (!fillRequirmentsLists(entry, source, itemNames, numberOfItemsNeeded, icons)) {
            return false;
         } else {
            ByteBuffer buffer = connection.getBuffer();
            addPartialRequestHeader(buffer);
            buffer.put((byte)1);
            buffer.putShort((short)1);
            String category = "Needed items";
            addStringToBuffer(buffer, "Needed items", false);
            buffer.putShort((short)numberOfItemsNeeded.size());

            for(int i = 0; i < numberOfItemsNeeded.size(); ++i) {
               String itemName = itemNames.get(i);
               addStringToBuffer(buffer, itemName, false);
               buffer.putShort(icons.get(i));
               short count = numberOfItemsNeeded.get(i).shortValue();
               buffer.putShort(count);
               buffer.putShort((short)0);
            }

            return true;
         }
      }
   }

   private static final boolean fillRequirmentsLists(
      AdvancedCreationEntry entry, Item source, List<String> itemNames, List<Integer> numberOfItemsNeeded, List<Short> icons
   ) {
      CreationRequirement[] requirements = entry.getRequirements();
      if (requirements.length == 0) {
         return false;
      } else {
         for(CreationRequirement requirement : requirements) {
            int remaining = requirement.getResourceNumber() - AdvancedCreationEntry.getStateForRequirement(requirement, source);
            if (remaining > 0) {
               int templateNeeded = requirement.getResourceTemplateId();
               ItemTemplate needed = getItemTemplate(templateNeeded);
               if (needed == null) {
                  return false;
               }

               itemNames.add(buildTemplateName(needed, null, (byte)0));
               icons.add(needed.getImageNumber());
               numberOfItemsNeeded.add(remaining);
            }
         }

         return true;
      }
   }

   private static final AdvancedCreationEntry getAdvancedCreationEntry(int id) {
      try {
         return CreationMatrix.getInstance().getAdvancedCreationEntry(id);
      } catch (NoSuchEntryException var2) {
         logger.log(Level.WARNING, "No advanced creation entry with id: " + id, (Throwable)var2);
         return null;
      }
   }

   private static final boolean addCreationEntriesToPartialList(ByteBuffer buffer, Map<CreationEntry, Integer> entries) {
      for(CreationEntry entry : entries.keySet()) {
         ItemTemplate template = getItemTemplate(entry.getObjectCreated());
         if (template == null) {
            return false;
         }

         String entryName = buildTemplateName(template, entry, (byte)0);
         addStringToBuffer(buffer, entryName, false);
         buffer.putShort(template.getImageNumber());
         short chance = entries.get(entry).shortValue();
         buffer.putShort(chance);
         buffer.putShort((short)(10000 + entry.getObjectCreated()));
      }

      return true;
   }

   private static final ItemTemplate getItemTemplate(int templateId) {
      try {
         return ItemTemplateFactory.getInstance().getTemplate(templateId);
      } catch (NoSuchTemplateException var2) {
         logger.log(Level.WARNING, "Unable to find item template with id: " + templateId, (Throwable)var2);
         return null;
      }
   }

   private static final String buildTemplateCaptionName(ItemTemplate toCreate, ItemTemplate source, ItemTemplate target) {
      String nameFormat = "%s %s";
      String materialFormat = "%s, %s";
      String name = toCreate.getName();
      String sourceMaterial = Item.getMaterialString(source.getMaterial());
      String targetMaterial = Item.getMaterialString(target.getMaterial());
      String createMaterial = Item.getMaterialString(toCreate.getMaterial());
      if (toCreate.sizeString.length() > 0) {
         name = StringUtil.format("%s %s", toCreate.sizeString.trim(), name);
      }

      if (toCreate.isMetal()) {
         if (!name.equals("lump") && !name.equals("sheet")) {
            if (!source.isTool() && source.isMetal() && !sourceMaterial.equals("unknown")) {
               name = StringUtil.format("%s, %s", name, sourceMaterial);
            } else if (!target.isTool() && target.isMetal() && !targetMaterial.equals("unknown")) {
               name = StringUtil.format("%s, %s", name, targetMaterial);
            }
         } else if (!createMaterial.equals("unknown")) {
            name = StringUtil.format("%s %s", createMaterial, name);
         }
      } else if (toCreate.isLiquidCooking()) {
         if (target.isFood()) {
            name = StringUtil.format("%s, %s", name, target.getName());
         }
      } else if (toCreate.getTemplateId() == 74) {
         if (!createMaterial.equals("unknown")) {
            name = StringUtil.format("%s %s", createMaterial, name);
         }
      } else if (toCreate.getTemplateId() == 891) {
         name = StringUtil.format("%s %s", "wooden", toCreate.getName());
      } else if (toCreate.getTemplateId() == 404) {
         name = StringUtil.format("%s %s", "stone", toCreate.getName());
      } else if (toCreate.isStone()) {
         if (name.equals("shards") && !createMaterial.equals("unknown")) {
            name = StringUtil.format("%s %s", createMaterial, name);
         } else if (name.equals("altar")) {
            name = StringUtil.format("%s %s", "stone", name);
         } else if (toCreate.getTemplateId() == 593) {
            name = StringUtil.format("%s %s", "stone", name);
         }
      } else if (toCreate.getTemplateId() == 322) {
         if (name.equals("altar")) {
            name = StringUtil.format("%s %s", "wooden", name);
         }
      } else if (toCreate.getTemplateId() == 592) {
         name = StringUtil.format("%s %s", "plank", name);
      }

      return name;
   }

   private static final String buildTemplateName(ItemTemplate template, @Nullable CreationEntry entry, byte materialOverride) {
      String nameFormat = "%s %s";
      String materialFormat = "%s, %s";
      String name = template.getName();
      String material = Item.getMaterialString(template.getMaterial());
      if (template.sizeString.length() > 0) {
         name = StringUtil.format("%s %s", template.sizeString.trim(), name);
      }

      if (!template.isMetal() || !name.equals("lump") && !name.equals("sheet")) {
         if (materialOverride != 0) {
            name = StringUtil.format("%s, %s", name, Materials.convertMaterialByteIntoString(materialOverride));
         } else if (name.equals("barding")) {
            if (template.isCloth()) {
               name = StringUtil.format("%s %s", "cloth", name);
            } else if (template.isMetal()) {
               name = StringUtil.format("%s %s", "chain", name);
            } else {
               name = StringUtil.format("%s %s", material, name);
            }
         } else if (name.equals("rock")) {
            name = StringUtil.format("%s, %s", name, "iron");
         } else if (template.getTemplateId() == 216 || template.getTemplateId() == 215) {
            name = StringUtil.format("%s, %s", name, material);
         } else if (template.isStone()) {
            if (name.equals("shards") && !material.equals("unknown")) {
               name = StringUtil.format("%s, %s", name, material);
            }
         } else if (name.equals("fur")) {
            if (entry != null) {
               if (entry.getObjectCreated() == 846) {
                  name = "black bear fur";
               } else if (entry.getObjectCreated() == 847) {
                  name = "brown bear fur";
               } else if (entry.getObjectCreated() == 849) {
                  name = "black wolf fur";
               }
            }
         } else if (name.equals("pelt") && entry != null && entry.getObjectCreated() == 848) {
            name = "mountain lion pelt";
         }
      } else {
         name = StringUtil.format("%s %s", material, name);
      }

      return name;
   }

   private static void addStringToBuffer(ByteBuffer buffer, String string, boolean shortLength) {
      byte[] bytes = getEncodedBytesFromString(string);
      if (!shortLength) {
         buffer.put((byte)bytes.length);
      } else {
         buffer.putShort((short)bytes.length);
      }

      buffer.put(bytes);
   }

   private static final byte[] getEncodedBytesFromString(String string) {
      try {
         return string.getBytes("UTF-8");
      } catch (UnsupportedEncodingException var2) {
         logger.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
         return new byte[0];
      }
   }

   private static void addPartialRequestHeader(ByteBuffer buffer) {
      buffer.put((byte)-46);
      buffer.put((byte)0);
   }

   public static boolean sendAllCraftingRecipes(SocketConnection connection, @Nonnull Player player) {
      CreationWindowMethods.RecipesListParameter params = new CreationWindowMethods.RecipesListParameter();
      short numberOfEntries = buildCreationsList(params);
      if (!sendCreationListCategories(connection, params, numberOfEntries)) {
         player.setLink(false);
         return false;
      } else if (!sendCreationRecipes(connection, player, params)) {
         return false;
      } else if (!sendFenceRecipes(connection, player, params)) {
         return false;
      } else if (!sendHedgeRecipes(connection, player, params)) {
         return false;
      } else if (!sendFlowerbedRecipes(connection, player, params)) {
         return false;
      } else if (!sendWallRecipes(connection, player, params)) {
         return false;
      } else if (!sendRoofFloorRecipes(connection, player, params)) {
         return false;
      } else if (!sendBridgePartRecipes(connection, player, params)) {
         return false;
      } else {
         return sendCaveWallRecipes(connection, player, params);
      }
   }

   private static final boolean sendRoofFloorRecipes(SocketConnection connection, @Nonnull Player player, CreationWindowMethods.RecipesListParameter params) {
      for(String category : params.getRoofs_floors().keySet()) {
         for(RoofFloorEnum entry : params.getRoofs_floors().get(category)) {
            int[] tools = RoofFloorEnum.getValidToolsForMaterial(entry.getMaterial());

            for(int tool : tools) {
               ByteBuffer buffer = connection.getBuffer();
               addCreationRecipesMessageHeaders(buffer);
               addCategoryIdToBuffer(params, category, buffer);
               addRoofFloorRecipeInfoToBuffer(entry, buffer);
               ItemTemplate toolTemplate = getItemTemplate(tool);
               if (toolTemplate == null) {
                  logger.log(Level.WARNING, "sendRoofFlorRecipes() No item template found with id: " + tool);
                  connection.clearBuffer();
                  return false;
               }

               addRoofFloorToolInfoToBuffer(buffer, toolTemplate);
               addWallPlanInfoToBuffer(buffer, entry);
               if (!addAdditionalMaterialsForRoofsFloors(buffer, entry)) {
                  connection.clearBuffer();
                  return false;
               }

               try {
                  connection.flush();
               } catch (IOException var16) {
                  logger.log(Level.WARNING, "Failed to flush floor|roof recipes!", (Throwable)var16);
                  player.setLink(false);
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static final boolean sendBridgePartRecipes(SocketConnection connection, @Nonnull Player player, CreationWindowMethods.RecipesListParameter params) {
      for(String category : params.getBridgeParts().keySet()) {
         for(BridgePartEnum entry : params.getBridgeParts().get(category)) {
            int[] tools = BridgePartEnum.getValidToolsForMaterial(entry.getMaterial());

            for(int tool : tools) {
               ByteBuffer buffer = connection.getBuffer();
               addCreationRecipesMessageHeaders(buffer);
               addCategoryIdToBuffer(params, category, buffer);
               addBridgePartRecipeInfoToBuffer(entry, buffer);
               ItemTemplate toolTemplate = getItemTemplate(tool);
               if (toolTemplate == null) {
                  logger.log(Level.WARNING, "sendRoofFlorRecipes() No item template found with id: " + tool);
                  connection.clearBuffer();
                  return false;
               }

               addRoofFloorToolInfoToBuffer(buffer, toolTemplate);
               buffer.putShort((short)60);
               addStringToBuffer(buffer, entry.getName() + " plan", true);
               if (!addTotalMaterialsForBridgeParts(buffer, entry)) {
                  connection.clearBuffer();
                  return false;
               }

               try {
                  connection.flush();
               } catch (IOException var16) {
                  logger.log(Level.WARNING, "Failed to flush bridge part recipes!", (Throwable)var16);
                  player.setLink(false);
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static final boolean addAdditionalMaterialsForRoofsFloors(ByteBuffer buffer, RoofFloorEnum entry) {
      List<BuildMaterial> list = entry.getTotalMaterialsNeeded();
      buffer.putShort((short)list.size());

      for(BuildMaterial bMat : list) {
         ItemTemplate mat = getItemTemplate(bMat.getTemplateId());
         if (mat == null) {
            logger.log(Level.WARNING, "Unable to find item template with id: " + bMat.getTemplateId());
            return false;
         }

         buffer.putShort(mat.getImageNumber());
         addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
         buffer.putShort((short)bMat.getNeededQuantity());
      }

      return true;
   }

   private static final boolean addTotalMaterialsForBridgeParts(ByteBuffer buffer, BridgePartEnum entry) {
      List<BuildMaterial> list = entry.getTotalMaterialsNeeded();
      buffer.putShort((short)list.size());

      for(BuildMaterial bMat : list) {
         ItemTemplate mat = getItemTemplate(bMat.getTemplateId());
         if (mat == null) {
            logger.log(Level.WARNING, "Unable to find item template with id: " + bMat.getTemplateId());
            return false;
         }

         buffer.putShort(mat.getImageNumber());
         addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
         buffer.putShort((short)bMat.getNeededQuantity());
      }

      return true;
   }

   private static void addRoofFloorToolInfoToBuffer(ByteBuffer buffer, ItemTemplate toolTemplate) {
      buffer.putShort(toolTemplate.getImageNumber());
      addStringToBuffer(buffer, toolTemplate.getName(), true);
   }

   private static void addRoofFloorRecipeInfoToBuffer(RoofFloorEnum entry, ByteBuffer buffer) {
      buffer.putShort((short)60);
      addStringToBuffer(buffer, entry.getName(), true);
      addStringToBuffer(buffer, SkillSystem.getNameFor(entry.getNeededSkillNumber()), true);
   }

   private static void addBridgePartRecipeInfoToBuffer(BridgePartEnum entry, ByteBuffer buffer) {
      buffer.putShort((short)60);
      addStringToBuffer(buffer, entry.getName(), true);
      addStringToBuffer(buffer, SkillSystem.getNameFor(entry.getNeededSkillNumber()), true);
   }

   private static final boolean sendWallRecipes(SocketConnection connection, @Nonnull Player player, CreationWindowMethods.RecipesListParameter params) {
      for(String category : params.getWalls().keySet()) {
         for(WallEnum entry : params.getWalls().get(category)) {
            for(Integer tool : WallEnum.getToolsForWall(entry, null)) {
               ByteBuffer buffer = connection.getBuffer();
               addCreationRecipesMessageHeaders(buffer);
               addCategoryIdToBuffer(params, category, buffer);
               addWallInfoToBuffer(entry, buffer);
               ItemTemplate toolTemplate = getItemTemplate(tool);
               if (toolTemplate == null) {
                  connection.clearBuffer();
                  logger.log(Level.WARNING, "Unable to find tool with id: " + tool);
                  return false;
               }

               addWallToolIInfoToBuffer(buffer, toolTemplate);
               addWallPlanInfoToBuffer(buffer);
               if (!addAdditionalMaterialsForWall(buffer, entry)) {
                  connection.clearBuffer();
                  return false;
               }

               try {
                  connection.flush();
               } catch (IOException var14) {
                  logger.log(Level.WARNING, "Failed to flush well recipe", (Throwable)var14);
                  player.setLink(false);
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static final boolean addAdditionalMaterialsForWall(ByteBuffer buffer, WallEnum entry) {
      int[] needed = entry.getTotalMaterialsNeeded();
      buffer.putShort((short)(needed.length / 2));

      for(int i = 0; i < needed.length; i += 2) {
         ItemTemplate mat = getItemTemplate(needed[i]);
         if (mat == null) {
            return false;
         }

         buffer.putShort(mat.getImageNumber());
         addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
         buffer.putShort((short)needed[i + 1]);
      }

      return true;
   }

   private static void addWallPlanInfoToBuffer(ByteBuffer buffer) {
      buffer.putShort((short)60);
      addStringToBuffer(buffer, WallEnum.WALL_PLAN.getName(), true);
   }

   private static void addWallPlanInfoToBuffer(ByteBuffer buffer, RoofFloorEnum entry) {
      buffer.putShort((short)60);
      String planString = entry.isFloor() ? "planned floor" : "planned roof";
      addStringToBuffer(buffer, planString, true);
   }

   private static void addWallToolIInfoToBuffer(ByteBuffer buffer, ItemTemplate toolTemplate) {
      buffer.putShort(toolTemplate.getImageNumber());
      addStringToBuffer(buffer, toolTemplate.getName(), true);
   }

   private static void addWallInfoToBuffer(WallEnum entry, ByteBuffer buffer) {
      buffer.putShort((short)60);
      addStringToBuffer(buffer, entry.getName(), true);
      addStringToBuffer(buffer, SkillSystem.getNameFor(WallEnum.getSkillNumber(entry.getMaterial())), true);
   }

   private static final boolean sendFlowerbedRecipes(SocketConnection connection, @Nonnull Player player, CreationWindowMethods.RecipesListParameter params) {
      for(String category : params.getFlowerbeds().keySet()) {
         for(short entry : params.getFlowerbeds().get(category)) {
            String name = WallConstants.getName(StructureConstantsEnum.getEnumByValue(entry));
            int flowerType = Fence.getFlowerTypeByFlowerbedType(StructureConstantsEnum.getEnumByValue(entry));
            ByteBuffer buffer = connection.getBuffer();
            addCreationRecipesMessageHeaders(buffer);
            addCategoryIdToBuffer(params, category, buffer);
            addFlowerbedInfoToBuffer(name, buffer);
            ItemTemplate flower = getItemTemplate(flowerType);
            if (flower == null) {
               connection.clearBuffer();
               return false;
            }

            addWallToolIInfoToBuffer(buffer, flower);
            addTileBorderToBuffer(buffer);
            if (!addAdditionalMaterialsForFlowerbed(buffer, flower)) {
               connection.clearBuffer();
               return false;
            }

            try {
               connection.flush();
            } catch (IOException var14) {
               logger.log(Level.WARNING, "IO Exception when sending flowerbed recipes.", (Throwable)var14);
               player.setLink(false);
               return false;
            }
         }
      }

      return true;
   }

   private static final boolean addAdditionalMaterialsForFlowerbed(ByteBuffer buffer, ItemTemplate flower) {
      int[] needed = new int[]{flower.getTemplateId(), 4, 22, 3, 218, 1, 26, 1};
      buffer.putShort((short)(needed.length / 2));

      for(int i = 0; i < needed.length; i += 2) {
         ItemTemplate mat = null;
         if (needed[i] == flower.getTemplateId()) {
            mat = flower;
         } else {
            mat = getItemTemplate(needed[i]);
            if (mat == null) {
               return false;
            }
         }

         buffer.putShort((short)60);
         addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
         buffer.putShort((short)needed[i + 1]);
      }

      return true;
   }

   private static void addFlowerbedInfoToBuffer(String name, ByteBuffer buffer) {
      buffer.putShort((short)60);
      addStringToBuffer(buffer, name, true);
      addStringToBuffer(buffer, SkillSystem.getNameFor(10045), true);
   }

   private static final boolean sendHedgeRecipes(SocketConnection connection, @Nonnull Player player, CreationWindowMethods.RecipesListParameter params) {
      for(String category : params.getHedges().keySet()) {
         for(short entry : params.getHedges().get(category)) {
            ByteBuffer buffer = connection.getBuffer();
            addCreationRecipesMessageHeaders(buffer);
            addCategoryIdToBuffer(params, category, buffer);
            addHedgeInfoToBuffer(StructureConstantsEnum.getEnumByValue(entry), buffer);
            ItemTemplate sprout = getItemTemplate(266);
            if (sprout == null) {
               connection.clearBuffer();
               return false;
            }

            byte materialType = Fence.getMaterialForLowHedge(StructureConstantsEnum.getEnumByValue(entry));
            String materialString = Item.getMaterialString(materialType);
            addSproutInfoToBuffer(sprout, materialString, buffer);
            addTileBorderToBuffer(buffer);
            addAdditionalMaterialsForHedge(buffer, sprout, materialString);

            try {
               connection.flush();
            } catch (IOException var14) {
               logger.log(Level.WARNING, "IO Exception when sending hedge recipes.", (Throwable)var14);
               player.setLink(false);
               return false;
            }
         }
      }

      return true;
   }

   private static final void addAdditionalMaterialsForHedge(ByteBuffer buffer, ItemTemplate template, String material) {
      buffer.putShort((short)1);
      buffer.putShort(template.getImageNumber());
      addStringToBuffer(buffer, StringUtil.format("%s, %s", template.getName(), material), true);
      buffer.putShort((short)4);
   }

   private static void addSproutInfoToBuffer(ItemTemplate sprout, String material, ByteBuffer buffer) {
      buffer.putShort(sprout.getImageNumber());
      String sproutName = StringUtil.format("%s, %s", sprout.getName(), material);
      addStringToBuffer(buffer, sproutName, true);
   }

   private static void addHedgeInfoToBuffer(StructureConstantsEnum hedgeType, ByteBuffer buffer) {
      buffer.putShort((short)60);
      addStringToBuffer(buffer, WallConstants.getName(hedgeType), true);
      addStringToBuffer(buffer, SkillSystem.getNameFor(10045), true);
   }

   private static final boolean sendFenceRecipes(SocketConnection connection, @Nonnull Player player, CreationWindowMethods.RecipesListParameter params) {
      for(String category : params.getFences().keySet()) {
         for(ActionEntry entry : params.getFences().get(category)) {
            StructureConstantsEnum originalFenceType = Fence.getFencePlanType(entry.getNumber());
            StructureConstantsEnum fenceType = Fence.getFenceForPlan(originalFenceType);
            int[] correctTools = MethodsStructure.getCorrectToolsForBuildingFences();

            for(int i = 0; i < correctTools.length; ++i) {
               ByteBuffer buffer = connection.getBuffer();
               addCreationRecipesMessageHeaders(buffer);
               addCategoryIdToBuffer(params, category, buffer);
               addCreatedFenceToBuffer(originalFenceType, fenceType, buffer);
               if (!addFenceToolToBuffer(buffer, correctTools[i])) {
                  connection.clearBuffer();
                  return false;
               }

               addTileBorderToBuffer(buffer);
               if (!addAdditionalMaterialsForFence(buffer, originalFenceType)) {
                  connection.clearBuffer();
                  return false;
               }

               try {
                  connection.flush();
               } catch (IOException var14) {
                  logger.log(Level.WARNING, "IO Exception when sending fence recipes.", (Throwable)var14);
                  player.setLink(false);
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static final boolean addAdditionalMaterialsForFence(ByteBuffer buffer, StructureConstantsEnum fence) {
      int[] items = Fence.getItemTemplatesNeededForFenceTotal(fence);
      if (items.length < 2) {
         buffer.putShort((short)0);
      } else {
         buffer.putShort((short)(items.length / 2));

         for(int i = 0; i < items.length; i += 2) {
            ItemTemplate mat = getItemTemplate(items[i]);
            if (mat == null) {
               return false;
            }

            buffer.putShort(mat.getImageNumber());
            addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
            buffer.putShort((short)items[i + 1]);
         }
      }

      return true;
   }

   private static void addTileBorderToBuffer(ByteBuffer buffer) {
      buffer.putShort((short)60);
      addStringToBuffer(buffer, "Tile Border", true);
   }

   private static boolean addFenceToolToBuffer(ByteBuffer buffer, int toolId) {
      ItemTemplate toolTemplate = getItemTemplate(toolId);
      if (toolTemplate == null) {
         logger.log(Level.WARNING, "Unable to find tool template with id: " + toolId);
         return false;
      } else {
         buffer.putShort(toolTemplate.imageNumber);
         addStringToBuffer(buffer, toolTemplate.getName(), true);
         return true;
      }
   }

   private static void addCreatedFenceToBuffer(StructureConstantsEnum originalFenceType, StructureConstantsEnum fenceType, ByteBuffer buffer) {
      buffer.putShort((short)60);
      String fenceName = WallConstants.getName(fenceType);
      addStringToBuffer(buffer, fenceName, true);
      int skillNumber = Fence.getSkillNumberNeededForFence(originalFenceType);
      addStringToBuffer(buffer, SkillSystem.getNameFor(skillNumber), true);
   }

   private static void addReinforcedWallToBuffer(ByteBuffer buffer) {
      buffer.putShort((short)60);
      addStringToBuffer(buffer, Tiles.Tile.TILE_CAVE_WALL_REINFORCED.getName(), true);
   }

   private static void addCreatedReinforcedWallToBuffer(byte partCladType, byte cladType, ByteBuffer buffer) {
      buffer.putShort((short)60);
      String fenceName = Tiles.getTile(cladType).getName();
      addStringToBuffer(buffer, fenceName, true);
      int skillNumber = CaveWallBehaviour.getSkillNumberNeededForCladding((short)partCladType);
      addStringToBuffer(buffer, SkillSystem.getNameFor(skillNumber), true);
   }

   private static final boolean addAdditionalMaterialsForReinforcedWall(ByteBuffer buffer, short action) {
      int[] items = CaveWallBehaviour.getMaterialsNeededTotal(action);
      if (items.length < 2) {
         buffer.putShort((short)0);
      } else {
         buffer.putShort((short)(items.length / 2));

         for(int i = 0; i < items.length; i += 2) {
            ItemTemplate mat = getItemTemplate(items[i]);
            if (mat == null) {
               return false;
            }

            buffer.putShort(mat.getImageNumber());
            addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
            buffer.putShort((short)items[i + 1]);
         }
      }

      return true;
   }

   private static final boolean sendCreationRecipes(SocketConnection connection, @Nonnull Player player, CreationWindowMethods.RecipesListParameter params) {
      for(String category : params.getCreationEntries().keySet()) {
         for(CreationEntry entry : params.getCreationEntries().get(category)) {
            ByteBuffer buffer = connection.getBuffer();
            addCreationRecipesMessageHeaders(buffer);
            addCategoryIdToBuffer(params, category, buffer);
            ItemTemplate created = getItemTemplate(entry.getObjectCreated());
            ItemTemplate source = getItemTemplate(entry.getObjectSource());
            ItemTemplate target = getItemTemplate(entry.getObjectTarget());
            if (created == null || source == null || target == null) {
               connection.clearBuffer();
               return false;
            }

            addItemCreatedToRecipesBuffer(entry, buffer, created, source, target);
            addInitialItemUsedToRecipesBuffer(entry, buffer, source, entry.getObjectSourceMaterial());
            addInitialItemUsedToRecipesBuffer(entry, buffer, target, entry.getObjectTargetMaterial());
            if (!addAditionalMaterialsForAdvancedEntries(buffer, entry)) {
               connection.clearBuffer();
               return false;
            }

            try {
               connection.flush();
            } catch (IOException var13) {
               logger.log(Level.WARNING, "Failed to send creation entries to recipes list", (Throwable)var13);
               player.setLink(false);
               return false;
            }
         }
      }

      return true;
   }

   private static final boolean addAditionalMaterialsForAdvancedEntries(ByteBuffer buffer, CreationEntry entry) {
      if (entry instanceof AdvancedCreationEntry) {
         AdvancedCreationEntry adv = (AdvancedCreationEntry)entry;
         CreationRequirement[] reqs = adv.getRequirements();
         buffer.putShort((short)reqs.length);

         for(CreationRequirement req : reqs) {
            int id = req.getResourceTemplateId();
            ItemTemplate mat = getItemTemplate(id);
            if (mat == null) {
               return false;
            }

            buffer.putShort(mat.getImageNumber());
            addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
            buffer.putShort((short)req.getResourceNumber());
         }
      } else {
         buffer.putShort((short)0);
      }

      return true;
   }

   private static void addInitialItemUsedToRecipesBuffer(CreationEntry entry, ByteBuffer buffer, ItemTemplate item, byte materialOverride) {
      buffer.putShort(item.getImageNumber());
      addStringToBuffer(buffer, buildTemplateName(item, entry, materialOverride), true);
   }

   private static void addItemCreatedToRecipesBuffer(CreationEntry entry, ByteBuffer buffer, ItemTemplate created, ItemTemplate source, ItemTemplate target) {
      buffer.putShort(created.getImageNumber());
      addStringToBuffer(buffer, buildTemplateCaptionName(created, source, target), true);
      String skillName = SkillSystem.getNameFor(entry.getPrimarySkill());
      addStringToBuffer(buffer, skillName, true);
   }

   private static void addCategoryIdToBuffer(CreationWindowMethods.RecipesListParameter params, String category, ByteBuffer buffer) {
      buffer.putShort(params.getCategoryIds().get(category).shortValue());
   }

   private static void addCreationRecipesMessageHeaders(ByteBuffer buffer) {
      buffer.put((byte)-46);
      buffer.put((byte)3);
   }

   private static final boolean sendCreationListCategories(
      SocketConnection connection, CreationWindowMethods.RecipesListParameter params, short numberOfEntries
   ) {
      ByteBuffer buffer = connection.getBuffer();
      addRecipesCategoryListMessageHeadersToBuffer(buffer);
      buffer.putShort((short)params.getTotalCategories());
      addCategoryToBuffer(buffer, params.getCreationEntries().keySet(), params.getCategoryIds());
      addCategoryToBuffer(buffer, params.getFences().keySet(), params.getCategoryIds());
      addCategoryToBuffer(buffer, params.getHedges().keySet(), params.getCategoryIds());
      addCategoryToBuffer(buffer, params.getFlowerbeds().keySet(), params.getCategoryIds());
      addCategoryToBuffer(buffer, params.getWalls().keySet(), params.getCategoryIds());
      addCategoryToBuffer(buffer, params.getRoofs_floors().keySet(), params.getCategoryIds());
      addCategoryToBuffer(buffer, params.getBridgeParts().keySet(), params.getCategoryIds());
      addCategoryToBuffer(buffer, params.getCaveWalls().keySet(), params.getCategoryIds());
      buffer.putShort(numberOfEntries);

      try {
         connection.flush();
         return true;
      } catch (IOException var5) {
         logger.log(Level.WARNING, "An error occured while flushing the categories for the recipes list.", (Throwable)var5);
         connection.clearBuffer();
         return false;
      }
   }

   private static void addCategoryToBuffer(ByteBuffer buffer, Set<String> categories, Map<String, Integer> categoryIds) {
      for(String categoryName : categories) {
         buffer.putShort(categoryIds.get(categoryName).shortValue());
         addStringToBuffer(buffer, categoryName, true);
      }
   }

   private static void addRecipesCategoryListMessageHeadersToBuffer(ByteBuffer buffer) {
      buffer.put((byte)-46);
      buffer.put((byte)4);
   }

   private static short addCraftingRecipesToRecipesList(CreationWindowMethods.RecipesListParameter params, CreationEntry[] toAdd, boolean isSimple) {
      short numberOfEntries = 0;

      for(CreationEntry entry : toAdd) {
         if (!isSimple || !CreationMatrix.getInstance().getAdvancedEntriesMap().containsKey(entry.getObjectCreated()) && entry.getObjectTarget() != 672) {
            String categoryName = entry.getCategory().getCategoryName();
            List<CreationEntry> entries = null;
            if (!params.getCreationEntries().containsKey(categoryName)) {
               params.getCreationEntries().put(categoryName, new ArrayList<>());
            }

            assignCategoryId(categoryName, params);
            entries = params.getCreationEntries().get(categoryName);
            entries.add(entry);
            ++numberOfEntries;
         }
      }

      return numberOfEntries;
   }

   private static final short addFencesToCraftingRecipesList(CreationWindowMethods.RecipesListParameter param) {
      Map<String, List<ActionEntry>> flist = createFenceCreationList(true, true, false);
      int[] cTools = MethodsStructure.getCorrectToolsForBuildingFences();
      short numberOfEntries = 0;

      for(String name : flist.keySet()) {
         String categoryName = StringUtil.format("%s %s", name, "fences");
         if (!param.getFences().containsKey(categoryName)) {
            param.getFences().put(categoryName, new ArrayList<>());
         }

         assignCategoryId(categoryName, param);
         List<ActionEntry> entries = param.getFences().get(categoryName);

         for(ActionEntry entry : flist.get(name)) {
            entries.add(entry);
            numberOfEntries = (short)(numberOfEntries + cTools.length);
         }
      }

      return numberOfEntries;
   }

   private static final short addGenericRecipesToList(
      Map<String, List<Short>> list, CreationWindowMethods.RecipesListParameter param, short[] toAdd, String categoryToAdd
   ) {
      short numberOfEntries = 0;
      assignCategoryId(categoryToAdd, param);

      for(int i = 0; i < toAdd.length; ++i) {
         if (!list.containsKey(categoryToAdd)) {
            list.put(categoryToAdd, new ArrayList<>());
         }

         List<Short> entries = list.get(categoryToAdd);
         entries.add(toAdd[i]);
         ++numberOfEntries;
      }

      return numberOfEntries;
   }

   private static void assignCategoryId(String category, CreationWindowMethods.RecipesListParameter params) {
      if (!params.getCategoryIds().containsKey(category)) {
         params.getCategoryIds().put(category, params.getCategoryIdsSize() + 1);
      }
   }

   private static final short addWallsToTheCraftingList(CreationWindowMethods.RecipesListParameter param) {
      short numberOfEntries = 0;
      String wallsCategory = "Walls";
      assignCategoryId("Walls", param);

      for(WallEnum en : WallEnum.values()) {
         if (en != WallEnum.WALL_PLAN) {
            if (!param.getWalls().containsKey("Walls")) {
               param.getWalls().put("Walls", new ArrayList<>());
            }

            List<WallEnum> entries = param.getWalls().get("Walls");
            entries.add(en);
            numberOfEntries = (short)(numberOfEntries + WallEnum.getToolsForWall(en, null).size());
         }
      }

      return numberOfEntries;
   }

   private static final short addBridgePartsToTheCraftingList(CreationWindowMethods.RecipesListParameter param) {
      short numberOfEntries = 0;

      for(BridgePartEnum en : BridgePartEnum.values()) {
         if (en != BridgePartEnum.UNKNOWN) {
            String typeName = StringUtil.toLowerCase(en.getMaterial().getName());
            typeName = StringUtil.format("%s %s", "bridge,", typeName);
            String categoryName = LoginHandler.raiseFirstLetter(typeName);
            assignCategoryId(categoryName, param);
            if (!param.getBridgeParts().containsKey(categoryName)) {
               param.getBridgeParts().put(categoryName, new ArrayList<>());
            }

            List<BridgePartEnum> entries = param.getBridgeParts().get(categoryName);
            entries.add(en);
            numberOfEntries = (short)(numberOfEntries + BridgePartEnum.getValidToolsForMaterial(en.getMaterial()).length);
         }
      }

      return numberOfEntries;
   }

   private static final short addCaveWallsToTheCraftingList(CreationWindowMethods.RecipesListParameter param) {
      String wallsCategory = "Cave walls";
      assignCategoryId("Cave walls", param);
      List<ActionEntry> flist = createCaveWallCreationList();
      short numberOfEntries = 0;
      if (!param.getCaveWalls().containsKey("Cave walls")) {
         param.getCaveWalls().put("Cave walls", new ArrayList<>());
      }

      List<ActionEntry> entries = param.getCaveWalls().get("Cave walls");

      for(ActionEntry entry : flist) {
         entries.add(entry);
         numberOfEntries = (short)(numberOfEntries + CaveWallBehaviour.getCorrectToolsForCladding(entry.getNumber()).length);
      }

      return numberOfEntries;
   }

   private static final short addRoofsFloorsToTheCraftingList(CreationWindowMethods.RecipesListParameter param) {
      short numberOfEntries = 0;

      for(RoofFloorEnum en : RoofFloorEnum.values()) {
         if (en != RoofFloorEnum.UNKNOWN) {
            String typeName = en.getType().getName();
            if (typeName.contains("opening")) {
               typeName = StringUtil.format("%s %s%s", "floor", typeName, "s");
            } else if (typeName.contains("staircase,")) {
               typeName = StringUtil.format("%s", typeName.replace("se,", "ses,"));
            } else {
               typeName = StringUtil.format("%s%s", typeName, "s");
            }

            String categoryName = LoginHandler.raiseFirstLetter(typeName);
            assignCategoryId(categoryName, param);
            if (!param.getRoofs_floors().containsKey(categoryName)) {
               param.getRoofs_floors().put(categoryName, new ArrayList<>());
            }

            List<RoofFloorEnum> entries = param.getRoofs_floors().get(categoryName);
            entries.add(en);
            numberOfEntries = (short)(numberOfEntries + RoofFloorEnum.getValidToolsForMaterial(en.getMaterial()).length);
         }
      }

      return numberOfEntries;
   }

   private static final boolean sendCaveWallRecipes(SocketConnection connection, @Nonnull Player player, CreationWindowMethods.RecipesListParameter params) {
      for(String category : params.getCaveWalls().keySet()) {
         for(ActionEntry entry : params.getCaveWalls().get(category)) {
            byte partCladType = CaveWallBehaviour.getPartReinforcedWallFromAction(entry.getNumber());
            byte cladType = CaveWallBehaviour.getReinforcedWallFromAction(entry.getNumber());
            int[] correctTools = CaveWallBehaviour.getCorrectToolsForCladding(entry.getNumber());

            for(int i = 0; i < correctTools.length; ++i) {
               ByteBuffer buffer = connection.getBuffer();
               addCreationRecipesMessageHeaders(buffer);
               addCategoryIdToBuffer(params, category, buffer);
               addCreatedReinforcedWallToBuffer(partCladType, cladType, buffer);
               if (!addFenceToolToBuffer(buffer, correctTools[i])) {
                  connection.clearBuffer();
                  return false;
               }

               addReinforcedWallToBuffer(buffer);
               if (!addAdditionalMaterialsForReinforcedWall(buffer, entry.getNumber())) {
                  connection.clearBuffer();
                  return false;
               }

               try {
                  connection.flush();
               } catch (IOException var14) {
                  logger.log(Level.WARNING, "IO Exception when sending fence recipes.", (Throwable)var14);
                  player.setLink(false);
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static final short buildCreationsList(CreationWindowMethods.RecipesListParameter param) {
      short numberOfEntries = 0;
      numberOfEntries = (short)(numberOfEntries + addCraftingRecipesToRecipesList(param, CreationMatrix.getInstance().getSimpleEntries(), true));
      numberOfEntries = (short)(numberOfEntries + addCraftingRecipesToRecipesList(param, CreationMatrix.getInstance().getAdvancedEntries(), false));
      numberOfEntries = (short)(numberOfEntries + addFencesToCraftingRecipesList(param));
      numberOfEntries = (short)(numberOfEntries + addGenericRecipesToList(param.getHedges(), param, Fence.getAllLowHedgeTypes(), "Hedges"));
      numberOfEntries = (short)(numberOfEntries + addGenericRecipesToList(param.getFlowerbeds(), param, Fence.getAllFlowerbeds(), "Flowerbeds"));
      numberOfEntries = (short)(numberOfEntries + addWallsToTheCraftingList(param));
      numberOfEntries = (short)(numberOfEntries + addRoofsFloorsToTheCraftingList(param));
      numberOfEntries = (short)(numberOfEntries + addBridgePartsToTheCraftingList(param));
      return (short)(numberOfEntries + addCaveWallsToTheCraftingList(param));
   }

   public static class RecipesListParameter {
      private Map<String, List<CreationEntry>> creationEntries = new HashMap<>();
      private Map<String, Integer> categoryIds = new HashMap<>();
      private Map<String, List<ActionEntry>> fences = new HashMap<>();
      private Map<String, List<Short>> hedges = new HashMap<>();
      private Map<String, List<Short>> flowerbeds = new HashMap<>();
      private Map<String, List<WallEnum>> walls = new HashMap<>();
      private Map<String, List<RoofFloorEnum>> roofs_floors = new HashMap<>();
      private Map<String, List<BridgePartEnum>> bridgeParts = new HashMap<>();
      private Map<String, List<ActionEntry>> cavewalls = new HashMap<>();

      public Map<String, List<CreationEntry>> getCreationEntries() {
         return this.creationEntries;
      }

      public final int getCreationEntriesSize() {
         return this.creationEntries.size();
      }

      public Map<String, Integer> getCategoryIds() {
         return this.categoryIds;
      }

      public final int getCategoryIdsSize() {
         return this.categoryIds.size();
      }

      public Map<String, List<ActionEntry>> getFences() {
         return this.fences;
      }

      public final int getFencesSize() {
         return this.fences.size();
      }

      public Map<String, List<Short>> getHedges() {
         return this.hedges;
      }

      public final int getHedgesSize() {
         return this.hedges.size();
      }

      public Map<String, List<Short>> getFlowerbeds() {
         return this.flowerbeds;
      }

      public final int getFlowerbedsSize() {
         return this.flowerbeds.size();
      }

      public Map<String, List<WallEnum>> getWalls() {
         return this.walls;
      }

      public final int getWallsSize() {
         return this.walls.size();
      }

      public Map<String, List<RoofFloorEnum>> getRoofs_floors() {
         return this.roofs_floors;
      }

      public final int getRoofs_floorsSize() {
         return this.roofs_floors.size();
      }

      public Map<String, List<BridgePartEnum>> getBridgeParts() {
         return this.bridgeParts;
      }

      public final int getBridgePartsSize() {
         return this.bridgeParts.size();
      }

      public Map<String, List<ActionEntry>> getCaveWalls() {
         return this.cavewalls;
      }

      public final int getCaveWallsSize() {
         return this.cavewalls.size();
      }

      public final int getTotalCategories() {
         return this.getCreationEntriesSize()
            + this.getFencesSize()
            + this.getHedgesSize()
            + this.getFlowerbedsSize()
            + this.getWallsSize()
            + this.getRoofs_floorsSize()
            + this.getBridgePartsSize()
            + this.getCaveWallsSize();
      }
   }
}
