/*
 * Decompiled with CFR 0.152.
 */
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
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.CreationRequirement;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.Recipes;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CreationWindowMethods
implements ProtoConstants,
MiscConstants {
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
        }
        boolean sendNeededTool = tool == null || !WallEnum.isCorrectTool(wallEnum, player, tool);
        ByteBuffer buffer = connection.getBuffer();
        CreationWindowMethods.addPartialRequestHeader(buffer);
        buffer.put((byte)1);
        buffer.putShort((short)(sendNeededTool ? 2 : 1));
        if (sendNeededTool && !CreationWindowMethods.addToolsNeededForWall(buffer, wallEnum, player)) {
            connection.clearBuffer();
            return false;
        }
        CreationWindowMethods.addStringToBuffer(buffer, "Item(s) needed in inventory", false);
        int[] needed = WallEnum.getMaterialsNeeded(wall);
        buffer.putShort((short)(needed.length / 2));
        for (int i = 0; i < needed.length; i += 2) {
            ItemTemplate template = CreationWindowMethods.getItemTemplate(needed[i]);
            if (template == null) {
                connection.clearBuffer();
                return false;
            }
            String name = CreationWindowMethods.getFenceMaterialName(template);
            CreationWindowMethods.addStringToBuffer(buffer, name, false);
            buffer.putShort(template.getImageNumber());
            short chance = (short)needed[i + 1];
            buffer.putShort(chance);
            buffer.putShort(wallEnum.getActionId());
        }
        return true;
    }

    private static boolean addToolsNeededForWall(ByteBuffer buffer, WallEnum wallEnum, @Nonnull Player player) {
        CreationWindowMethods.addStringToBuffer(buffer, "Needed tool in crafting window", false);
        List<Integer> list = WallEnum.getToolsForWall(wallEnum, player);
        buffer.putShort((short)list.size());
        for (Integer tid : list) {
            ItemTemplate template = CreationWindowMethods.getItemTemplate(tid);
            if (template == null) {
                return false;
            }
            String name = CreationWindowMethods.getFenceMaterialName(template);
            CreationWindowMethods.addStringToBuffer(buffer, name, false);
            buffer.putShort(template.getImageNumber());
            boolean chance = true;
            buffer.putShort((short)1);
            buffer.putShort(wallEnum.getActionId());
        }
        return true;
    }

    private static final String getFenceMaterialName(ItemTemplate template) {
        if (template.getTemplateId() == 218) {
            return "small iron " + template.getName();
        }
        if (template.getTemplateId() == 217) {
            return "large iron " + template.getName();
        }
        return template.getName();
    }

    public static final boolean createWallPlanBuffer(SocketConnection connection, @Nonnull Structure structure, @Nonnull Wall wall, @Nonnull Player player, long toolId) {
        if (toolId == -10L) {
            return false;
        }
        Optional<Item> optTool = Items.getItemOptional(toolId);
        if (!optTool.isPresent()) {
            return false;
        }
        Item tool = optTool.get();
        List<WallEnum> wallList = WallEnum.getWallsByTool(player, tool, structure.needsDoor(), MethodsStructure.hasInsideFence(wall));
        if (wallList.size() == 0) {
            return false;
        }
        ByteBuffer buffer = connection.getBuffer();
        CreationWindowMethods.addPartialRequestHeader(buffer);
        buffer.put((byte)0);
        buffer.putShort((short)1);
        CreationWindowMethods.addStringToBuffer(buffer, "Walls", false);
        buffer.putShort((short)wallList.size());
        for (WallEnum en : wallList) {
            CreationWindowMethods.addStringToBuffer(buffer, en.getName(), false);
            buffer.putShort(en.getIcon());
            boolean canBuild = WallEnum.canBuildWall(wall, en.getMaterial(), player);
            short chance = (short)(canBuild ? 100 : 0);
            buffer.putShort(chance);
            buffer.putShort(en.getActionId());
        }
        return true;
    }

    public static final boolean createCaveCladdingBuffer(SocketConnection connection, int tilex, int tiley, int tile, byte type, Player player, long toolId) {
        short chance;
        String name;
        ItemTemplate template;
        int i;
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
        CreationWindowMethods.addPartialRequestHeader(buffer);
        buffer.put((byte)1);
        buffer.putShort((short)(sendNeededTool ? 3 : 2));
        if (sendNeededTool && !CreationWindowMethods.addToolsNeededForWall(buffer, type, player)) {
            connection.clearBuffer();
            return false;
        }
        CreationWindowMethods.addStringToBuffer(buffer, "Item(s) needed in inventory", false);
        short action = CaveWallBehaviour.actionFromWallType(type);
        int[] needed = CaveWallBehaviour.getMaterialsNeeded(tilex, tiley, type);
        buffer.putShort((short)(needed.length / 2));
        for (i = 0; i < needed.length; i += 2) {
            template = CreationWindowMethods.getItemTemplate(needed[i]);
            if (template == null) {
                connection.clearBuffer();
                return false;
            }
            name = CreationWindowMethods.getFenceMaterialName(template);
            CreationWindowMethods.addStringToBuffer(buffer, name, false);
            buffer.putShort(template.getImageNumber());
            chance = 1;
            buffer.putShort((short)1);
            buffer.putShort(action);
        }
        CreationWindowMethods.addStringToBuffer(buffer, "Total materials needed", false);
        if (needed.length == 1 && needed[0] == -1) {
            buffer.putShort((short)0);
        } else {
            buffer.putShort((short)(needed.length / 2));
            for (i = 0; i < needed.length; i += 2) {
                template = CreationWindowMethods.getItemTemplate(needed[i]);
                name = CreationWindowMethods.getFenceMaterialName(template);
                CreationWindowMethods.addStringToBuffer(buffer, name, false);
                buffer.putShort(template.getImageNumber());
                chance = (short)needed[i + 1];
                buffer.putShort(chance);
                buffer.putShort(action);
            }
        }
        return true;
    }

    private static boolean addToolsNeededForWall(ByteBuffer buffer, byte type, @Nonnull Player player) {
        CreationWindowMethods.addStringToBuffer(buffer, "Needed tool in crafting window", false);
        List<Integer> list = CaveWallBehaviour.getToolsForType(type, player);
        buffer.putShort((short)list.size());
        for (Integer tid : list) {
            ItemTemplate template = CreationWindowMethods.getItemTemplate(tid);
            if (template == null) {
                return false;
            }
            String name = CreationWindowMethods.getFenceMaterialName(template);
            CreationWindowMethods.addStringToBuffer(buffer, name, false);
            buffer.putShort(template.getImageNumber());
            boolean chance = true;
            buffer.putShort((short)1);
            buffer.putShort(CaveWallBehaviour.actionFromWallType(type));
        }
        return true;
    }

    public static final boolean createCaveReinforcedBuffer(SocketConnection connection, Player player, long toolId) {
        if (toolId == -10L) {
            return false;
        }
        Optional<Item> optTool = Items.getItemOptional(toolId);
        if (!optTool.isPresent()) {
            return false;
        }
        Item tool = optTool.get();
        byte[] canMake = CaveWallBehaviour.getMaterialsFromToolType(player, tool);
        if (canMake.length == 0) {
            return false;
        }
        ByteBuffer buffer = connection.getBuffer();
        CreationWindowMethods.addPartialRequestHeader(buffer);
        buffer.put((byte)0);
        buffer.putShort((short)1);
        CreationWindowMethods.addStringToBuffer(buffer, "CaveWalls", false);
        buffer.putShort((short)canMake.length);
        for (byte type : canMake) {
            Tiles.Tile theTile = Tiles.getTile(type);
            CreationWindowMethods.addStringToBuffer(buffer, theTile.getName(), false);
            buffer.putShort((short)theTile.getIconId());
            boolean canBuild = CaveWallBehaviour.canCladWall(type, player);
            short chance = (short)(canBuild ? 100 : 0);
            buffer.putShort(chance);
            buffer.putShort(CaveWallBehaviour.actionFromWallType(type));
        }
        return true;
    }

    public static final boolean createHedgeCreationBuffer(SocketConnection connection, @Nonnull Item sprout, long borderId, @Nonnull Player player) {
        Tiles.TileBorderDirection dir;
        int y;
        StructureConstantsEnum hedgeType = Fence.getLowHedgeType(sprout.getMaterial());
        if (hedgeType == StructureConstantsEnum.FENCE_PLAN_WOODEN) {
            return false;
        }
        short x = Tiles.decodeTileX(borderId);
        Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(x, y = Tiles.decodeTileY(borderId), dir = Tiles.decodeDirection(borderId), true);
        if (structure != null) {
            return false;
        }
        if (!player.isOnSurface()) {
            return false;
        }
        ByteBuffer buffer = connection.getBuffer();
        CreationWindowMethods.addPartialRequestHeader(buffer);
        buffer.put((byte)0);
        buffer.putShort((short)1);
        CreationWindowMethods.addStringToBuffer(buffer, "Hedges", false);
        buffer.putShort((short)1);
        String name = WallConstants.getName(hedgeType);
        CreationWindowMethods.addStringToBuffer(buffer, name, false);
        buffer.putShort((short)60);
        Skill gardening = player.getSkills().getSkillOrLearn(10045);
        short chance = (short)gardening.getChance(1.0f + sprout.getDamage(), null, sprout.getQualityLevel());
        buffer.putShort(chance);
        buffer.putShort(Actions.actionEntrys[186].getNumber());
        return true;
    }

    public static final boolean createFlowerbedBuffer(SocketConnection connection, @Nonnull Item tool, long borderId, @Nonnull Player player) {
        Tiles.TileBorderDirection dir;
        int y;
        StructureConstantsEnum flowerbedType = Fence.getFlowerbedType(tool.getTemplateId());
        short x = Tiles.decodeTileX(borderId);
        Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(x, y = Tiles.decodeTileY(borderId), dir = Tiles.decodeDirection(borderId), true);
        if (structure != null) {
            return false;
        }
        if (!player.isOnSurface()) {
            return false;
        }
        ByteBuffer buffer = connection.getBuffer();
        CreationWindowMethods.addPartialRequestHeader(buffer);
        buffer.put((byte)0);
        buffer.putShort((short)1);
        CreationWindowMethods.addStringToBuffer(buffer, "Flowerbeds", false);
        buffer.putShort((short)1);
        String name = WallConstants.getName(flowerbedType);
        CreationWindowMethods.addStringToBuffer(buffer, name, false);
        buffer.putShort((short)60);
        Skill gardening = player.getSkills().getSkillOrLearn(10045);
        short chance = (short)gardening.getChance(1.0f + tool.getDamage(), null, tool.getQualityLevel());
        buffer.putShort(chance);
        buffer.putShort(Actions.actionEntrys[563].getNumber());
        return true;
    }

    public static final boolean createFenceListBuffer(SocketConnection connection, long borderId) {
        Structure structure;
        short x = Tiles.decodeTileX(borderId);
        int y = Tiles.decodeTileY(borderId);
        Tiles.TileBorderDirection dir = Tiles.decodeDirection(borderId);
        int heightOffset = Tiles.decodeHeightOffset(borderId);
        boolean onSurface = true;
        boolean hasArch = false;
        if (MethodsStructure.doesTileBorderContainWallOrFence(x, y, heightOffset, dir, true, false)) {
            hasArch = true;
        }
        Map<String, List<ActionEntry>> fenceList = CreationWindowMethods.createFenceCreationList((structure = MethodsStructure.getStructureOrNullAtTileBorder(x, y, dir, true)) != null, false, hasArch);
        if (Items.getMarker(x, y, true, 0, -10L) != null) {
            return false;
        }
        if (dir == Tiles.TileBorderDirection.DIR_HORIZ && Items.getMarker(x + 1, y, true, 0, -10L) != null) {
            return false;
        }
        if (dir == Tiles.TileBorderDirection.DIR_DOWN && Items.getMarker(x, y + 1, true, 0, -10L) != null) {
            return false;
        }
        if (fenceList.size() == 0) {
            return false;
        }
        ByteBuffer buffer = connection.getBuffer();
        CreationWindowMethods.addPartialRequestHeader(buffer);
        buffer.put((byte)0);
        buffer.putShort((short)fenceList.size());
        for (String category : fenceList.keySet()) {
            CreationWindowMethods.addStringToBuffer(buffer, category, false);
            List<ActionEntry> fences = fenceList.get(category);
            buffer.putShort((short)fences.size());
            for (ActionEntry ae : fences) {
                StructureConstantsEnum type = Fence.getFencePlanType(ae.getNumber());
                String name = WallConstants.getName(Fence.getFenceForPlan(type));
                CreationWindowMethods.addStringToBuffer(buffer, name, false);
                buffer.putShort((short)60);
                int chance = 100;
                buffer.putShort((short)100);
                buffer.putShort(ae.getNumber());
            }
        }
        return true;
    }

    private static final Map<String, List<ActionEntry>> createFenceCreationList(boolean inStructure, boolean showAll, boolean borderHasArch) {
        HashMap<String, List<ActionEntry>> list = new HashMap<String, List<ActionEntry>>();
        if (!inStructure || showAll) {
            list.put("Log", new ArrayList());
        }
        list.put("Plank", new ArrayList());
        list.put("Rope", new ArrayList());
        list.put("Shaft", new ArrayList());
        list.put("Woven", new ArrayList());
        list.put("Stone", new ArrayList());
        list.put("Iron", new ArrayList());
        list.put("Slate", new ArrayList());
        list.put("Rounded stone", new ArrayList());
        list.put("Pottery", new ArrayList());
        list.put("Sandstone", new ArrayList());
        list.put("Marble", new ArrayList());
        if (!inStructure || showAll) {
            ((List)list.get("Log")).add(Actions.actionEntrys[165]);
            ((List)list.get("Log")).add(Actions.actionEntrys[167]);
        }
        ((List)list.get("Plank")).add(Actions.actionEntrys[166]);
        ((List)list.get("Plank")).add(Actions.actionEntrys[168]);
        ((List)list.get("Plank")).add(Actions.actionEntrys[520]);
        ((List)list.get("Plank")).add(Actions.actionEntrys[528]);
        if (inStructure && !borderHasArch || showAll) {
            ((List)list.get("Plank")).add(Actions.actionEntrys[516]);
        }
        ((List)list.get("Rope")).add(Actions.actionEntrys[543]);
        ((List)list.get("Rope")).add(Actions.actionEntrys[544]);
        ((List)list.get("Shaft")).add(Actions.actionEntrys[526]);
        ((List)list.get("Shaft")).add(Actions.actionEntrys[527]);
        ((List)list.get("Shaft")).add(Actions.actionEntrys[529]);
        ((List)list.get("Woven")).add(Actions.actionEntrys[478]);
        if (!inStructure || showAll) {
            ((List)list.get("Stone")).add(Actions.actionEntrys[163]);
        }
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Stone")).add(Actions.actionEntrys[164]);
        }
        if (!inStructure && !borderHasArch || showAll) {
            ((List)list.get("Stone")).add(Actions.actionEntrys[654]);
        }
        ((List)list.get("Stone")).add(Actions.actionEntrys[541]);
        ((List)list.get("Stone")).add(Actions.actionEntrys[542]);
        if (inStructure && !borderHasArch || showAll) {
            ((List)list.get("Stone")).add(Actions.actionEntrys[517]);
        }
        ((List)list.get("Iron")).add(Actions.actionEntrys[477]);
        ((List)list.get("Iron")).add(Actions.actionEntrys[479]);
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Iron")).add(Actions.actionEntrys[545]);
            ((List)list.get("Iron")).add(Actions.actionEntrys[546]);
        }
        ((List)list.get("Iron")).add(Actions.actionEntrys[611]);
        if (inStructure || showAll) {
            ((List)list.get("Iron")).add(Actions.actionEntrys[521]);
        }
        ((List)list.get("Slate")).add(Actions.actionEntrys[832]);
        ((List)list.get("Slate")).add(Actions.actionEntrys[833]);
        ((List)list.get("Slate")).add(Actions.actionEntrys[834]);
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Slate")).add(Actions.actionEntrys[870]);
        }
        if (!inStructure && !borderHasArch || showAll) {
            ((List)list.get("Slate")).add(Actions.actionEntrys[871]);
        }
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Slate")).add(Actions.actionEntrys[872]);
            ((List)list.get("Slate")).add(Actions.actionEntrys[873]);
        }
        if (inStructure && !borderHasArch || showAll) {
            ((List)list.get("Slate")).add(Actions.actionEntrys[874]);
        }
        ((List)list.get("Slate")).add(Actions.actionEntrys[875]);
        ((List)list.get("Rounded stone")).add(Actions.actionEntrys[835]);
        ((List)list.get("Rounded stone")).add(Actions.actionEntrys[836]);
        ((List)list.get("Rounded stone")).add(Actions.actionEntrys[837]);
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Rounded stone")).add(Actions.actionEntrys[876]);
        }
        if (!inStructure && !borderHasArch || showAll) {
            ((List)list.get("Rounded stone")).add(Actions.actionEntrys[877]);
        }
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Rounded stone")).add(Actions.actionEntrys[878]);
            ((List)list.get("Rounded stone")).add(Actions.actionEntrys[879]);
        }
        if (inStructure && !borderHasArch || showAll) {
            ((List)list.get("Rounded stone")).add(Actions.actionEntrys[880]);
        }
        ((List)list.get("Rounded stone")).add(Actions.actionEntrys[881]);
        ((List)list.get("Pottery")).add(Actions.actionEntrys[838]);
        ((List)list.get("Pottery")).add(Actions.actionEntrys[839]);
        ((List)list.get("Pottery")).add(Actions.actionEntrys[840]);
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Pottery")).add(Actions.actionEntrys[894]);
        }
        if (!inStructure && !borderHasArch || showAll) {
            ((List)list.get("Pottery")).add(Actions.actionEntrys[895]);
        }
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Pottery")).add(Actions.actionEntrys[896]);
            ((List)list.get("Pottery")).add(Actions.actionEntrys[897]);
        }
        if (inStructure && !borderHasArch || showAll) {
            ((List)list.get("Pottery")).add(Actions.actionEntrys[898]);
        }
        ((List)list.get("Pottery")).add(Actions.actionEntrys[899]);
        ((List)list.get("Sandstone")).add(Actions.actionEntrys[841]);
        ((List)list.get("Sandstone")).add(Actions.actionEntrys[842]);
        ((List)list.get("Sandstone")).add(Actions.actionEntrys[843]);
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Sandstone")).add(Actions.actionEntrys[882]);
        }
        if (!inStructure && !borderHasArch || showAll) {
            ((List)list.get("Sandstone")).add(Actions.actionEntrys[883]);
        }
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Sandstone")).add(Actions.actionEntrys[884]);
            ((List)list.get("Sandstone")).add(Actions.actionEntrys[885]);
        }
        if (inStructure && !borderHasArch || showAll) {
            ((List)list.get("Sandstone")).add(Actions.actionEntrys[886]);
        }
        ((List)list.get("Sandstone")).add(Actions.actionEntrys[887]);
        ((List)list.get("Marble")).add(Actions.actionEntrys[844]);
        ((List)list.get("Marble")).add(Actions.actionEntrys[845]);
        ((List)list.get("Marble")).add(Actions.actionEntrys[846]);
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Marble")).add(Actions.actionEntrys[900]);
        }
        if (!inStructure && !borderHasArch || showAll) {
            ((List)list.get("Marble")).add(Actions.actionEntrys[901]);
        }
        if (!inStructure || !borderHasArch || showAll) {
            ((List)list.get("Marble")).add(Actions.actionEntrys[902]);
            ((List)list.get("Marble")).add(Actions.actionEntrys[903]);
        }
        if (inStructure && !borderHasArch || showAll) {
            ((List)list.get("Marble")).add(Actions.actionEntrys[904]);
        }
        ((List)list.get("Marble")).add(Actions.actionEntrys[905]);
        return list;
    }

    private static final List<ActionEntry> createCaveWallCreationList() {
        ArrayList<ActionEntry> list = new ArrayList<ActionEntry>();
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
            ItemTemplate template;
            Recipe recipe = Recipes.getRecipeFor(player.getWurmId(), (byte)2, source, target, true, false);
            if (recipe == null) {
                return false;
            }
            ByteBuffer buffer = connection.getBuffer();
            CreationWindowMethods.addPartialRequestHeader(buffer);
            buffer.put((byte)0);
            buffer.putShort((short)1);
            CreationWindowMethods.addStringToBuffer(buffer, "Cooking", false);
            buffer.putShort((short)1);
            Item realSource = source;
            Item realTarget = target;
            if (recipe.hasActiveItem() && source != null && recipe.getActiveItem().getTemplateId() != realSource.getTemplateId()) {
                realSource = target;
                realTarget = source;
            }
            if ((template = recipe.getResultTemplate(realTarget)) == null) {
                connection.clearBuffer();
                return false;
            }
            CreationWindowMethods.addStringToBuffer(buffer, recipe.getSubMenuName(realTarget), false);
            buffer.putShort(template.getImageNumber());
            buffer.putShort((short)recipe.getChanceFor(realSource, realTarget, player));
            buffer.putShort(recipe.getMenuId());
            return true;
        }
        ByteBuffer buffer = connection.getBuffer();
        CreationWindowMethods.addPartialRequestHeader(buffer);
        buffer.put((byte)0);
        buffer.putShort((short)map.size());
        for (String category : map.keySet()) {
            CreationWindowMethods.addStringToBuffer(buffer, category, false);
            Map<CreationEntry, Integer> entries = map.get(category);
            buffer.putShort((short)entries.size());
            if (CreationWindowMethods.addCreationEntriesToPartialList(buffer, entries)) continue;
            connection.clearBuffer();
            return false;
        }
        return true;
    }

    public static final boolean createUnfinishedCreationListBuffer(SocketConnection connection, @Nonnull Item source, @Nonnull Player player) {
        AdvancedCreationEntry entry = CreationWindowMethods.getAdvancedCreationEntry(source.getRealTemplateId());
        if (entry == null) {
            return false;
        }
        ArrayList<String> itemNames = new ArrayList<String>();
        ArrayList<Integer> numberOfItemsNeeded = new ArrayList<Integer>();
        ArrayList<Short> icons = new ArrayList<Short>();
        if (!CreationWindowMethods.fillRequirmentsLists(entry, source, itemNames, numberOfItemsNeeded, icons)) {
            return false;
        }
        ByteBuffer buffer = connection.getBuffer();
        CreationWindowMethods.addPartialRequestHeader(buffer);
        buffer.put((byte)1);
        buffer.putShort((short)1);
        String category = "Needed items";
        CreationWindowMethods.addStringToBuffer(buffer, "Needed items", false);
        buffer.putShort((short)numberOfItemsNeeded.size());
        for (int i = 0; i < numberOfItemsNeeded.size(); ++i) {
            String itemName = (String)itemNames.get(i);
            CreationWindowMethods.addStringToBuffer(buffer, itemName, false);
            buffer.putShort((Short)icons.get(i));
            short count = ((Integer)numberOfItemsNeeded.get(i)).shortValue();
            buffer.putShort(count);
            buffer.putShort((short)0);
        }
        return true;
    }

    private static final boolean fillRequirmentsLists(AdvancedCreationEntry entry, Item source, List<String> itemNames, List<Integer> numberOfItemsNeeded, List<Short> icons) {
        CreationRequirement[] requirements = entry.getRequirements();
        if (requirements.length == 0) {
            return false;
        }
        for (CreationRequirement requirement : requirements) {
            int remaining = requirement.getResourceNumber() - AdvancedCreationEntry.getStateForRequirement(requirement, source);
            if (remaining <= 0) continue;
            int templateNeeded = requirement.getResourceTemplateId();
            ItemTemplate needed = CreationWindowMethods.getItemTemplate(templateNeeded);
            if (needed == null) {
                return false;
            }
            itemNames.add(CreationWindowMethods.buildTemplateName(needed, null, (byte)0));
            icons.add(needed.getImageNumber());
            numberOfItemsNeeded.add(remaining);
        }
        return true;
    }

    private static final AdvancedCreationEntry getAdvancedCreationEntry(int id) {
        try {
            return CreationMatrix.getInstance().getAdvancedCreationEntry(id);
        }
        catch (NoSuchEntryException nse) {
            logger.log(Level.WARNING, "No advanced creation entry with id: " + id, nse);
            return null;
        }
    }

    private static final boolean addCreationEntriesToPartialList(ByteBuffer buffer, Map<CreationEntry, Integer> entries) {
        for (CreationEntry entry : entries.keySet()) {
            ItemTemplate template = CreationWindowMethods.getItemTemplate(entry.getObjectCreated());
            if (template == null) {
                return false;
            }
            String entryName = CreationWindowMethods.buildTemplateName(template, entry, (byte)0);
            CreationWindowMethods.addStringToBuffer(buffer, entryName, false);
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
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, "Unable to find item template with id: " + templateId, nst);
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
        if (template.isMetal() && (name.equals("lump") || name.equals("sheet"))) {
            name = StringUtil.format("%s %s", material, name);
        } else if (materialOverride != 0) {
            name = StringUtil.format("%s, %s", name, Materials.convertMaterialByteIntoString(materialOverride));
        } else if (name.equals("barding")) {
            name = template.isCloth() ? StringUtil.format("%s %s", "cloth", name) : (template.isMetal() ? StringUtil.format("%s %s", "chain", name) : StringUtil.format("%s %s", material, name));
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
        return name;
    }

    private static void addStringToBuffer(ByteBuffer buffer, String string, boolean shortLength) {
        byte[] bytes = CreationWindowMethods.getEncodedBytesFromString(string);
        if (!shortLength) {
            buffer.put((byte)bytes.length);
        } else {
            buffer.putShort((short)bytes.length);
        }
        buffer.put(bytes);
    }

    private static final byte[] getEncodedBytesFromString(String string) {
        try {
            return string.getBytes(CHARSET_ENCODING_FOR_COMMS);
        }
        catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return new byte[0];
        }
    }

    private static void addPartialRequestHeader(ByteBuffer buffer) {
        buffer.put((byte)-46);
        buffer.put((byte)0);
    }

    public static boolean sendAllCraftingRecipes(SocketConnection connection, @Nonnull Player player) {
        RecipesListParameter params = new RecipesListParameter();
        short numberOfEntries = CreationWindowMethods.buildCreationsList(params);
        if (!CreationWindowMethods.sendCreationListCategories(connection, params, numberOfEntries)) {
            player.setLink(false);
            return false;
        }
        if (!CreationWindowMethods.sendCreationRecipes(connection, player, params)) {
            return false;
        }
        if (!CreationWindowMethods.sendFenceRecipes(connection, player, params)) {
            return false;
        }
        if (!CreationWindowMethods.sendHedgeRecipes(connection, player, params)) {
            return false;
        }
        if (!CreationWindowMethods.sendFlowerbedRecipes(connection, player, params)) {
            return false;
        }
        if (!CreationWindowMethods.sendWallRecipes(connection, player, params)) {
            return false;
        }
        if (!CreationWindowMethods.sendRoofFloorRecipes(connection, player, params)) {
            return false;
        }
        if (!CreationWindowMethods.sendBridgePartRecipes(connection, player, params)) {
            return false;
        }
        return CreationWindowMethods.sendCaveWallRecipes(connection, player, params);
    }

    private static final boolean sendRoofFloorRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params) {
        for (String category : params.getRoofs_floors().keySet()) {
            List<RoofFloorEnum> entries = params.getRoofs_floors().get(category);
            for (RoofFloorEnum entry : entries) {
                int[] tools;
                for (int tool : tools = RoofFloorEnum.getValidToolsForMaterial(entry.getMaterial())) {
                    ByteBuffer buffer = connection.getBuffer();
                    CreationWindowMethods.addCreationRecipesMessageHeaders(buffer);
                    CreationWindowMethods.addCategoryIdToBuffer(params, category, buffer);
                    CreationWindowMethods.addRoofFloorRecipeInfoToBuffer(entry, buffer);
                    ItemTemplate toolTemplate = CreationWindowMethods.getItemTemplate(tool);
                    if (toolTemplate == null) {
                        logger.log(Level.WARNING, "sendRoofFlorRecipes() No item template found with id: " + tool);
                        connection.clearBuffer();
                        return false;
                    }
                    CreationWindowMethods.addRoofFloorToolInfoToBuffer(buffer, toolTemplate);
                    CreationWindowMethods.addWallPlanInfoToBuffer(buffer, entry);
                    if (!CreationWindowMethods.addAdditionalMaterialsForRoofsFloors(buffer, entry)) {
                        connection.clearBuffer();
                        return false;
                    }
                    try {
                        connection.flush();
                    }
                    catch (IOException ex) {
                        logger.log(Level.WARNING, "Failed to flush floor|roof recipes!", ex);
                        player.setLink(false);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static final boolean sendBridgePartRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params) {
        for (String category : params.getBridgeParts().keySet()) {
            List<BridgePartEnum> entries = params.getBridgeParts().get(category);
            for (BridgePartEnum entry : entries) {
                int[] tools;
                for (int tool : tools = BridgePartEnum.getValidToolsForMaterial(entry.getMaterial())) {
                    ByteBuffer buffer = connection.getBuffer();
                    CreationWindowMethods.addCreationRecipesMessageHeaders(buffer);
                    CreationWindowMethods.addCategoryIdToBuffer(params, category, buffer);
                    CreationWindowMethods.addBridgePartRecipeInfoToBuffer(entry, buffer);
                    ItemTemplate toolTemplate = CreationWindowMethods.getItemTemplate(tool);
                    if (toolTemplate == null) {
                        logger.log(Level.WARNING, "sendRoofFlorRecipes() No item template found with id: " + tool);
                        connection.clearBuffer();
                        return false;
                    }
                    CreationWindowMethods.addRoofFloorToolInfoToBuffer(buffer, toolTemplate);
                    buffer.putShort((short)60);
                    CreationWindowMethods.addStringToBuffer(buffer, entry.getName() + " plan", true);
                    if (!CreationWindowMethods.addTotalMaterialsForBridgeParts(buffer, entry)) {
                        connection.clearBuffer();
                        return false;
                    }
                    try {
                        connection.flush();
                    }
                    catch (IOException ex) {
                        logger.log(Level.WARNING, "Failed to flush bridge part recipes!", ex);
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
        for (BuildMaterial bMat : list) {
            ItemTemplate mat = CreationWindowMethods.getItemTemplate(bMat.getTemplateId());
            if (mat == null) {
                logger.log(Level.WARNING, "Unable to find item template with id: " + bMat.getTemplateId());
                return false;
            }
            buffer.putShort(mat.getImageNumber());
            CreationWindowMethods.addStringToBuffer(buffer, CreationWindowMethods.buildTemplateName(mat, null, (byte)0), true);
            buffer.putShort((short)bMat.getNeededQuantity());
        }
        return true;
    }

    private static final boolean addTotalMaterialsForBridgeParts(ByteBuffer buffer, BridgePartEnum entry) {
        List<BuildMaterial> list = entry.getTotalMaterialsNeeded();
        buffer.putShort((short)list.size());
        for (BuildMaterial bMat : list) {
            ItemTemplate mat = CreationWindowMethods.getItemTemplate(bMat.getTemplateId());
            if (mat == null) {
                logger.log(Level.WARNING, "Unable to find item template with id: " + bMat.getTemplateId());
                return false;
            }
            buffer.putShort(mat.getImageNumber());
            CreationWindowMethods.addStringToBuffer(buffer, CreationWindowMethods.buildTemplateName(mat, null, (byte)0), true);
            buffer.putShort((short)bMat.getNeededQuantity());
        }
        return true;
    }

    private static void addRoofFloorToolInfoToBuffer(ByteBuffer buffer, ItemTemplate toolTemplate) {
        buffer.putShort(toolTemplate.getImageNumber());
        CreationWindowMethods.addStringToBuffer(buffer, toolTemplate.getName(), true);
    }

    private static void addRoofFloorRecipeInfoToBuffer(RoofFloorEnum entry, ByteBuffer buffer) {
        buffer.putShort((short)60);
        CreationWindowMethods.addStringToBuffer(buffer, entry.getName(), true);
        CreationWindowMethods.addStringToBuffer(buffer, SkillSystem.getNameFor(entry.getNeededSkillNumber()), true);
    }

    private static void addBridgePartRecipeInfoToBuffer(BridgePartEnum entry, ByteBuffer buffer) {
        buffer.putShort((short)60);
        CreationWindowMethods.addStringToBuffer(buffer, entry.getName(), true);
        CreationWindowMethods.addStringToBuffer(buffer, SkillSystem.getNameFor(entry.getNeededSkillNumber()), true);
    }

    private static final boolean sendWallRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params) {
        for (String category : params.getWalls().keySet()) {
            List<WallEnum> entries = params.getWalls().get(category);
            for (WallEnum entry : entries) {
                List<Integer> tools = WallEnum.getToolsForWall(entry, null);
                for (Integer tool : tools) {
                    ByteBuffer buffer = connection.getBuffer();
                    CreationWindowMethods.addCreationRecipesMessageHeaders(buffer);
                    CreationWindowMethods.addCategoryIdToBuffer(params, category, buffer);
                    CreationWindowMethods.addWallInfoToBuffer(entry, buffer);
                    ItemTemplate toolTemplate = CreationWindowMethods.getItemTemplate(tool);
                    if (toolTemplate == null) {
                        connection.clearBuffer();
                        logger.log(Level.WARNING, "Unable to find tool with id: " + tool);
                        return false;
                    }
                    CreationWindowMethods.addWallToolIInfoToBuffer(buffer, toolTemplate);
                    CreationWindowMethods.addWallPlanInfoToBuffer(buffer);
                    if (!CreationWindowMethods.addAdditionalMaterialsForWall(buffer, entry)) {
                        connection.clearBuffer();
                        return false;
                    }
                    try {
                        connection.flush();
                    }
                    catch (IOException iex) {
                        logger.log(Level.WARNING, "Failed to flush well recipe", iex);
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
        for (int i = 0; i < needed.length; i += 2) {
            ItemTemplate mat = CreationWindowMethods.getItemTemplate(needed[i]);
            if (mat == null) {
                return false;
            }
            buffer.putShort(mat.getImageNumber());
            CreationWindowMethods.addStringToBuffer(buffer, CreationWindowMethods.buildTemplateName(mat, null, (byte)0), true);
            buffer.putShort((short)needed[i + 1]);
        }
        return true;
    }

    private static void addWallPlanInfoToBuffer(ByteBuffer buffer) {
        buffer.putShort((short)60);
        CreationWindowMethods.addStringToBuffer(buffer, WallEnum.WALL_PLAN.getName(), true);
    }

    private static void addWallPlanInfoToBuffer(ByteBuffer buffer, RoofFloorEnum entry) {
        buffer.putShort((short)60);
        String planString = entry.isFloor() ? "planned floor" : "planned roof";
        CreationWindowMethods.addStringToBuffer(buffer, planString, true);
    }

    private static void addWallToolIInfoToBuffer(ByteBuffer buffer, ItemTemplate toolTemplate) {
        buffer.putShort(toolTemplate.getImageNumber());
        CreationWindowMethods.addStringToBuffer(buffer, toolTemplate.getName(), true);
    }

    private static void addWallInfoToBuffer(WallEnum entry, ByteBuffer buffer) {
        buffer.putShort((short)60);
        CreationWindowMethods.addStringToBuffer(buffer, entry.getName(), true);
        CreationWindowMethods.addStringToBuffer(buffer, SkillSystem.getNameFor(WallEnum.getSkillNumber(entry.getMaterial())), true);
    }

    private static final boolean sendFlowerbedRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params) {
        for (String category : params.getFlowerbeds().keySet()) {
            List<Short> entries = params.getFlowerbeds().get(category);
            Iterator<Short> iterator = entries.iterator();
            while (iterator.hasNext()) {
                short entry;
                short bedType = entry = iterator.next().shortValue();
                String name = WallConstants.getName(StructureConstantsEnum.getEnumByValue(bedType));
                int flowerType = Fence.getFlowerTypeByFlowerbedType(StructureConstantsEnum.getEnumByValue(bedType));
                ByteBuffer buffer = connection.getBuffer();
                CreationWindowMethods.addCreationRecipesMessageHeaders(buffer);
                CreationWindowMethods.addCategoryIdToBuffer(params, category, buffer);
                CreationWindowMethods.addFlowerbedInfoToBuffer(name, buffer);
                ItemTemplate flower = CreationWindowMethods.getItemTemplate(flowerType);
                if (flower == null) {
                    connection.clearBuffer();
                    return false;
                }
                CreationWindowMethods.addWallToolIInfoToBuffer(buffer, flower);
                CreationWindowMethods.addTileBorderToBuffer(buffer);
                if (!CreationWindowMethods.addAdditionalMaterialsForFlowerbed(buffer, flower)) {
                    connection.clearBuffer();
                    return false;
                }
                try {
                    connection.flush();
                }
                catch (IOException ex) {
                    logger.log(Level.WARNING, "IO Exception when sending flowerbed recipes.", ex);
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
        for (int i = 0; i < needed.length; i += 2) {
            ItemTemplate mat = null;
            if (needed[i] == flower.getTemplateId()) {
                mat = flower;
            } else {
                mat = CreationWindowMethods.getItemTemplate(needed[i]);
                if (mat == null) {
                    return false;
                }
            }
            buffer.putShort((short)60);
            CreationWindowMethods.addStringToBuffer(buffer, CreationWindowMethods.buildTemplateName(mat, null, (byte)0), true);
            buffer.putShort((short)needed[i + 1]);
        }
        return true;
    }

    private static void addFlowerbedInfoToBuffer(String name, ByteBuffer buffer) {
        buffer.putShort((short)60);
        CreationWindowMethods.addStringToBuffer(buffer, name, true);
        CreationWindowMethods.addStringToBuffer(buffer, SkillSystem.getNameFor(10045), true);
    }

    private static final boolean sendHedgeRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params) {
        for (String category : params.getHedges().keySet()) {
            List<Short> entries = params.getHedges().get(category);
            Iterator<Short> iterator = entries.iterator();
            while (iterator.hasNext()) {
                short entry;
                short hedgeType = entry = iterator.next().shortValue();
                ByteBuffer buffer = connection.getBuffer();
                CreationWindowMethods.addCreationRecipesMessageHeaders(buffer);
                CreationWindowMethods.addCategoryIdToBuffer(params, category, buffer);
                CreationWindowMethods.addHedgeInfoToBuffer(StructureConstantsEnum.getEnumByValue(hedgeType), buffer);
                ItemTemplate sprout = CreationWindowMethods.getItemTemplate(266);
                if (sprout == null) {
                    connection.clearBuffer();
                    return false;
                }
                byte materialType = Fence.getMaterialForLowHedge(StructureConstantsEnum.getEnumByValue(hedgeType));
                String materialString = Item.getMaterialString(materialType);
                CreationWindowMethods.addSproutInfoToBuffer(sprout, materialString, buffer);
                CreationWindowMethods.addTileBorderToBuffer(buffer);
                CreationWindowMethods.addAdditionalMaterialsForHedge(buffer, sprout, materialString);
                try {
                    connection.flush();
                }
                catch (IOException ex) {
                    logger.log(Level.WARNING, "IO Exception when sending hedge recipes.", ex);
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
        CreationWindowMethods.addStringToBuffer(buffer, StringUtil.format("%s, %s", template.getName(), material), true);
        buffer.putShort((short)4);
    }

    private static void addSproutInfoToBuffer(ItemTemplate sprout, String material, ByteBuffer buffer) {
        buffer.putShort(sprout.getImageNumber());
        String sproutName = StringUtil.format("%s, %s", sprout.getName(), material);
        CreationWindowMethods.addStringToBuffer(buffer, sproutName, true);
    }

    private static void addHedgeInfoToBuffer(StructureConstantsEnum hedgeType, ByteBuffer buffer) {
        buffer.putShort((short)60);
        CreationWindowMethods.addStringToBuffer(buffer, WallConstants.getName(hedgeType), true);
        CreationWindowMethods.addStringToBuffer(buffer, SkillSystem.getNameFor(10045), true);
    }

    private static final boolean sendFenceRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params) {
        for (String category : params.getFences().keySet()) {
            List<ActionEntry> entries = params.getFences().get(category);
            for (ActionEntry entry : entries) {
                StructureConstantsEnum originalFenceType = Fence.getFencePlanType(entry.getNumber());
                StructureConstantsEnum fenceType = Fence.getFenceForPlan(originalFenceType);
                int[] correctTools = MethodsStructure.getCorrectToolsForBuildingFences();
                for (int i = 0; i < correctTools.length; ++i) {
                    ByteBuffer buffer = connection.getBuffer();
                    CreationWindowMethods.addCreationRecipesMessageHeaders(buffer);
                    CreationWindowMethods.addCategoryIdToBuffer(params, category, buffer);
                    CreationWindowMethods.addCreatedFenceToBuffer(originalFenceType, fenceType, buffer);
                    if (!CreationWindowMethods.addFenceToolToBuffer(buffer, correctTools[i])) {
                        connection.clearBuffer();
                        return false;
                    }
                    CreationWindowMethods.addTileBorderToBuffer(buffer);
                    if (!CreationWindowMethods.addAdditionalMaterialsForFence(buffer, originalFenceType)) {
                        connection.clearBuffer();
                        return false;
                    }
                    try {
                        connection.flush();
                        continue;
                    }
                    catch (IOException ex) {
                        logger.log(Level.WARNING, "IO Exception when sending fence recipes.", ex);
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
            for (int i = 0; i < items.length; i += 2) {
                ItemTemplate mat = CreationWindowMethods.getItemTemplate(items[i]);
                if (mat == null) {
                    return false;
                }
                buffer.putShort(mat.getImageNumber());
                CreationWindowMethods.addStringToBuffer(buffer, CreationWindowMethods.buildTemplateName(mat, null, (byte)0), true);
                buffer.putShort((short)items[i + 1]);
            }
        }
        return true;
    }

    private static void addTileBorderToBuffer(ByteBuffer buffer) {
        buffer.putShort((short)60);
        CreationWindowMethods.addStringToBuffer(buffer, "Tile Border", true);
    }

    private static boolean addFenceToolToBuffer(ByteBuffer buffer, int toolId) {
        ItemTemplate toolTemplate = CreationWindowMethods.getItemTemplate(toolId);
        if (toolTemplate == null) {
            logger.log(Level.WARNING, "Unable to find tool template with id: " + toolId);
            return false;
        }
        buffer.putShort(toolTemplate.imageNumber);
        CreationWindowMethods.addStringToBuffer(buffer, toolTemplate.getName(), true);
        return true;
    }

    private static void addCreatedFenceToBuffer(StructureConstantsEnum originalFenceType, StructureConstantsEnum fenceType, ByteBuffer buffer) {
        buffer.putShort((short)60);
        String fenceName = WallConstants.getName(fenceType);
        CreationWindowMethods.addStringToBuffer(buffer, fenceName, true);
        int skillNumber = Fence.getSkillNumberNeededForFence(originalFenceType);
        CreationWindowMethods.addStringToBuffer(buffer, SkillSystem.getNameFor(skillNumber), true);
    }

    private static void addReinforcedWallToBuffer(ByteBuffer buffer) {
        buffer.putShort((short)60);
        CreationWindowMethods.addStringToBuffer(buffer, Tiles.Tile.TILE_CAVE_WALL_REINFORCED.getName(), true);
    }

    private static void addCreatedReinforcedWallToBuffer(byte partCladType, byte cladType, ByteBuffer buffer) {
        buffer.putShort((short)60);
        String fenceName = Tiles.getTile(cladType).getName();
        CreationWindowMethods.addStringToBuffer(buffer, fenceName, true);
        int skillNumber = CaveWallBehaviour.getSkillNumberNeededForCladding(partCladType);
        CreationWindowMethods.addStringToBuffer(buffer, SkillSystem.getNameFor(skillNumber), true);
    }

    private static final boolean addAdditionalMaterialsForReinforcedWall(ByteBuffer buffer, short action) {
        int[] items = CaveWallBehaviour.getMaterialsNeededTotal(action);
        if (items.length < 2) {
            buffer.putShort((short)0);
        } else {
            buffer.putShort((short)(items.length / 2));
            for (int i = 0; i < items.length; i += 2) {
                ItemTemplate mat = CreationWindowMethods.getItemTemplate(items[i]);
                if (mat == null) {
                    return false;
                }
                buffer.putShort(mat.getImageNumber());
                CreationWindowMethods.addStringToBuffer(buffer, CreationWindowMethods.buildTemplateName(mat, null, (byte)0), true);
                buffer.putShort((short)items[i + 1]);
            }
        }
        return true;
    }

    private static final boolean sendCreationRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params) {
        for (String category : params.getCreationEntries().keySet()) {
            List<CreationEntry> entries = params.getCreationEntries().get(category);
            for (CreationEntry entry : entries) {
                ByteBuffer buffer = connection.getBuffer();
                CreationWindowMethods.addCreationRecipesMessageHeaders(buffer);
                CreationWindowMethods.addCategoryIdToBuffer(params, category, buffer);
                ItemTemplate created = CreationWindowMethods.getItemTemplate(entry.getObjectCreated());
                ItemTemplate source = CreationWindowMethods.getItemTemplate(entry.getObjectSource());
                ItemTemplate target = CreationWindowMethods.getItemTemplate(entry.getObjectTarget());
                if (created == null || source == null || target == null) {
                    connection.clearBuffer();
                    return false;
                }
                CreationWindowMethods.addItemCreatedToRecipesBuffer(entry, buffer, created, source, target);
                CreationWindowMethods.addInitialItemUsedToRecipesBuffer(entry, buffer, source, entry.getObjectSourceMaterial());
                CreationWindowMethods.addInitialItemUsedToRecipesBuffer(entry, buffer, target, entry.getObjectTargetMaterial());
                if (!CreationWindowMethods.addAditionalMaterialsForAdvancedEntries(buffer, entry)) {
                    connection.clearBuffer();
                    return false;
                }
                try {
                    connection.flush();
                }
                catch (IOException iex) {
                    logger.log(Level.WARNING, "Failed to send creation entries to recipes list", iex);
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
            for (CreationRequirement req : reqs) {
                int id = req.getResourceTemplateId();
                ItemTemplate mat = CreationWindowMethods.getItemTemplate(id);
                if (mat == null) {
                    return false;
                }
                buffer.putShort(mat.getImageNumber());
                CreationWindowMethods.addStringToBuffer(buffer, CreationWindowMethods.buildTemplateName(mat, null, (byte)0), true);
                buffer.putShort((short)req.getResourceNumber());
            }
        } else {
            buffer.putShort((short)0);
        }
        return true;
    }

    private static void addInitialItemUsedToRecipesBuffer(CreationEntry entry, ByteBuffer buffer, ItemTemplate item, byte materialOverride) {
        buffer.putShort(item.getImageNumber());
        CreationWindowMethods.addStringToBuffer(buffer, CreationWindowMethods.buildTemplateName(item, entry, materialOverride), true);
    }

    private static void addItemCreatedToRecipesBuffer(CreationEntry entry, ByteBuffer buffer, ItemTemplate created, ItemTemplate source, ItemTemplate target) {
        buffer.putShort(created.getImageNumber());
        CreationWindowMethods.addStringToBuffer(buffer, CreationWindowMethods.buildTemplateCaptionName(created, source, target), true);
        String skillName = SkillSystem.getNameFor(entry.getPrimarySkill());
        CreationWindowMethods.addStringToBuffer(buffer, skillName, true);
    }

    private static void addCategoryIdToBuffer(RecipesListParameter params, String category, ByteBuffer buffer) {
        buffer.putShort(params.getCategoryIds().get(category).shortValue());
    }

    private static void addCreationRecipesMessageHeaders(ByteBuffer buffer) {
        buffer.put((byte)-46);
        buffer.put((byte)3);
    }

    private static final boolean sendCreationListCategories(SocketConnection connection, RecipesListParameter params, short numberOfEntries) {
        ByteBuffer buffer = connection.getBuffer();
        CreationWindowMethods.addRecipesCategoryListMessageHeadersToBuffer(buffer);
        buffer.putShort((short)params.getTotalCategories());
        CreationWindowMethods.addCategoryToBuffer(buffer, params.getCreationEntries().keySet(), params.getCategoryIds());
        CreationWindowMethods.addCategoryToBuffer(buffer, params.getFences().keySet(), params.getCategoryIds());
        CreationWindowMethods.addCategoryToBuffer(buffer, params.getHedges().keySet(), params.getCategoryIds());
        CreationWindowMethods.addCategoryToBuffer(buffer, params.getFlowerbeds().keySet(), params.getCategoryIds());
        CreationWindowMethods.addCategoryToBuffer(buffer, params.getWalls().keySet(), params.getCategoryIds());
        CreationWindowMethods.addCategoryToBuffer(buffer, params.getRoofs_floors().keySet(), params.getCategoryIds());
        CreationWindowMethods.addCategoryToBuffer(buffer, params.getBridgeParts().keySet(), params.getCategoryIds());
        CreationWindowMethods.addCategoryToBuffer(buffer, params.getCaveWalls().keySet(), params.getCategoryIds());
        buffer.putShort(numberOfEntries);
        try {
            connection.flush();
            return true;
        }
        catch (IOException iex) {
            logger.log(Level.WARNING, "An error occured while flushing the categories for the recipes list.", iex);
            connection.clearBuffer();
            return false;
        }
    }

    private static void addCategoryToBuffer(ByteBuffer buffer, Set<String> categories, Map<String, Integer> categoryIds) {
        for (String categoryName : categories) {
            buffer.putShort(categoryIds.get(categoryName).shortValue());
            CreationWindowMethods.addStringToBuffer(buffer, categoryName, true);
        }
    }

    private static void addRecipesCategoryListMessageHeadersToBuffer(ByteBuffer buffer) {
        buffer.put((byte)-46);
        buffer.put((byte)4);
    }

    private static short addCraftingRecipesToRecipesList(RecipesListParameter params, CreationEntry[] toAdd, boolean isSimple) {
        short numberOfEntries = 0;
        for (CreationEntry entry : toAdd) {
            if (isSimple && (CreationMatrix.getInstance().getAdvancedEntriesMap().containsKey(entry.getObjectCreated()) || entry.getObjectTarget() == 672)) continue;
            String categoryName = entry.getCategory().getCategoryName();
            List<CreationEntry> entries = null;
            if (!params.getCreationEntries().containsKey(categoryName)) {
                params.getCreationEntries().put(categoryName, new ArrayList());
            }
            CreationWindowMethods.assignCategoryId(categoryName, params);
            entries = params.getCreationEntries().get(categoryName);
            entries.add(entry);
            numberOfEntries = (short)(numberOfEntries + 1);
        }
        return numberOfEntries;
    }

    private static final short addFencesToCraftingRecipesList(RecipesListParameter param) {
        Map<String, List<ActionEntry>> flist = CreationWindowMethods.createFenceCreationList(true, true, false);
        int[] cTools = MethodsStructure.getCorrectToolsForBuildingFences();
        short numberOfEntries = 0;
        for (String name : flist.keySet()) {
            String categoryName = StringUtil.format("%s %s", name, "fences");
            if (!param.getFences().containsKey(categoryName)) {
                param.getFences().put(categoryName, new ArrayList());
            }
            CreationWindowMethods.assignCategoryId(categoryName, param);
            List<ActionEntry> entries = param.getFences().get(categoryName);
            for (ActionEntry entry : flist.get(name)) {
                entries.add(entry);
                numberOfEntries = (short)(numberOfEntries + cTools.length);
            }
        }
        return numberOfEntries;
    }

    private static final short addGenericRecipesToList(Map<String, List<Short>> list, RecipesListParameter param, short[] toAdd, String categoryToAdd) {
        short numberOfEntries = 0;
        CreationWindowMethods.assignCategoryId(categoryToAdd, param);
        for (int i = 0; i < toAdd.length; ++i) {
            if (!list.containsKey(categoryToAdd)) {
                list.put(categoryToAdd, new ArrayList());
            }
            List<Short> entries = list.get(categoryToAdd);
            entries.add(toAdd[i]);
            numberOfEntries = (short)(numberOfEntries + 1);
        }
        return numberOfEntries;
    }

    private static void assignCategoryId(String category, RecipesListParameter params) {
        if (!params.getCategoryIds().containsKey(category)) {
            params.getCategoryIds().put(category, params.getCategoryIdsSize() + 1);
        }
    }

    private static final short addWallsToTheCraftingList(RecipesListParameter param) {
        short numberOfEntries = 0;
        String wallsCategory = "Walls";
        CreationWindowMethods.assignCategoryId("Walls", param);
        for (WallEnum en : WallEnum.values()) {
            if (en == WallEnum.WALL_PLAN) continue;
            if (!param.getWalls().containsKey("Walls")) {
                param.getWalls().put("Walls", new ArrayList());
            }
            List<WallEnum> entries = param.getWalls().get("Walls");
            entries.add(en);
            numberOfEntries = (short)(numberOfEntries + WallEnum.getToolsForWall(en, null).size());
        }
        return numberOfEntries;
    }

    private static final short addBridgePartsToTheCraftingList(RecipesListParameter param) {
        short numberOfEntries = 0;
        for (BridgePartEnum en : BridgePartEnum.values()) {
            if (en == BridgePartEnum.UNKNOWN) continue;
            String typeName = StringUtil.toLowerCase(en.getMaterial().getName());
            typeName = StringUtil.format("%s %s", "bridge,", typeName);
            String categoryName = LoginHandler.raiseFirstLetter(typeName);
            CreationWindowMethods.assignCategoryId(categoryName, param);
            if (!param.getBridgeParts().containsKey(categoryName)) {
                param.getBridgeParts().put(categoryName, new ArrayList());
            }
            List<BridgePartEnum> entries = param.getBridgeParts().get(categoryName);
            entries.add(en);
            numberOfEntries = (short)(numberOfEntries + BridgePartEnum.getValidToolsForMaterial(en.getMaterial()).length);
        }
        return numberOfEntries;
    }

    private static final short addCaveWallsToTheCraftingList(RecipesListParameter param) {
        String wallsCategory = "Cave walls";
        CreationWindowMethods.assignCategoryId("Cave walls", param);
        List<ActionEntry> flist = CreationWindowMethods.createCaveWallCreationList();
        short numberOfEntries = 0;
        if (!param.getCaveWalls().containsKey("Cave walls")) {
            param.getCaveWalls().put("Cave walls", new ArrayList());
        }
        List<ActionEntry> entries = param.getCaveWalls().get("Cave walls");
        for (ActionEntry entry : flist) {
            entries.add(entry);
            numberOfEntries = (short)(numberOfEntries + CaveWallBehaviour.getCorrectToolsForCladding(entry.getNumber()).length);
        }
        return numberOfEntries;
    }

    private static final short addRoofsFloorsToTheCraftingList(RecipesListParameter param) {
        short numberOfEntries = 0;
        for (RoofFloorEnum en : RoofFloorEnum.values()) {
            if (en == RoofFloorEnum.UNKNOWN) continue;
            String typeName = en.getType().getName();
            typeName = typeName.contains("opening") ? StringUtil.format("%s %s%s", "floor", typeName, "s") : (typeName.contains("staircase,") ? StringUtil.format("%s", typeName.replace("se,", "ses,")) : StringUtil.format("%s%s", typeName, "s"));
            String categoryName = LoginHandler.raiseFirstLetter(typeName);
            CreationWindowMethods.assignCategoryId(categoryName, param);
            if (!param.getRoofs_floors().containsKey(categoryName)) {
                param.getRoofs_floors().put(categoryName, new ArrayList());
            }
            List<RoofFloorEnum> entries = param.getRoofs_floors().get(categoryName);
            entries.add(en);
            numberOfEntries = (short)(numberOfEntries + RoofFloorEnum.getValidToolsForMaterial(en.getMaterial()).length);
        }
        return numberOfEntries;
    }

    private static final boolean sendCaveWallRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params) {
        for (String category : params.getCaveWalls().keySet()) {
            List<ActionEntry> entries = params.getCaveWalls().get(category);
            for (ActionEntry entry : entries) {
                byte partCladType = CaveWallBehaviour.getPartReinforcedWallFromAction(entry.getNumber());
                byte cladType = CaveWallBehaviour.getReinforcedWallFromAction(entry.getNumber());
                int[] correctTools = CaveWallBehaviour.getCorrectToolsForCladding(entry.getNumber());
                for (int i = 0; i < correctTools.length; ++i) {
                    ByteBuffer buffer = connection.getBuffer();
                    CreationWindowMethods.addCreationRecipesMessageHeaders(buffer);
                    CreationWindowMethods.addCategoryIdToBuffer(params, category, buffer);
                    CreationWindowMethods.addCreatedReinforcedWallToBuffer(partCladType, cladType, buffer);
                    if (!CreationWindowMethods.addFenceToolToBuffer(buffer, correctTools[i])) {
                        connection.clearBuffer();
                        return false;
                    }
                    CreationWindowMethods.addReinforcedWallToBuffer(buffer);
                    if (!CreationWindowMethods.addAdditionalMaterialsForReinforcedWall(buffer, entry.getNumber())) {
                        connection.clearBuffer();
                        return false;
                    }
                    try {
                        connection.flush();
                        continue;
                    }
                    catch (IOException ex) {
                        logger.log(Level.WARNING, "IO Exception when sending fence recipes.", ex);
                        player.setLink(false);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static final short buildCreationsList(RecipesListParameter param) {
        short numberOfEntries = 0;
        numberOfEntries = (short)(numberOfEntries + CreationWindowMethods.addCraftingRecipesToRecipesList(param, CreationMatrix.getInstance().getSimpleEntries(), true));
        numberOfEntries = (short)(numberOfEntries + CreationWindowMethods.addCraftingRecipesToRecipesList(param, CreationMatrix.getInstance().getAdvancedEntries(), false));
        numberOfEntries = (short)(numberOfEntries + CreationWindowMethods.addFencesToCraftingRecipesList(param));
        numberOfEntries = (short)(numberOfEntries + CreationWindowMethods.addGenericRecipesToList(param.getHedges(), param, Fence.getAllLowHedgeTypes(), "Hedges"));
        numberOfEntries = (short)(numberOfEntries + CreationWindowMethods.addGenericRecipesToList(param.getFlowerbeds(), param, Fence.getAllFlowerbeds(), "Flowerbeds"));
        numberOfEntries = (short)(numberOfEntries + CreationWindowMethods.addWallsToTheCraftingList(param));
        numberOfEntries = (short)(numberOfEntries + CreationWindowMethods.addRoofsFloorsToTheCraftingList(param));
        numberOfEntries = (short)(numberOfEntries + CreationWindowMethods.addBridgePartsToTheCraftingList(param));
        numberOfEntries = (short)(numberOfEntries + CreationWindowMethods.addCaveWallsToTheCraftingList(param));
        return numberOfEntries;
    }

    public static class RecipesListParameter {
        private Map<String, List<CreationEntry>> creationEntries = new HashMap<String, List<CreationEntry>>();
        private Map<String, Integer> categoryIds = new HashMap<String, Integer>();
        private Map<String, List<ActionEntry>> fences = new HashMap<String, List<ActionEntry>>();
        private Map<String, List<Short>> hedges = new HashMap<String, List<Short>>();
        private Map<String, List<Short>> flowerbeds = new HashMap<String, List<Short>>();
        private Map<String, List<WallEnum>> walls = new HashMap<String, List<WallEnum>>();
        private Map<String, List<RoofFloorEnum>> roofs_floors = new HashMap<String, List<RoofFloorEnum>>();
        private Map<String, List<BridgePartEnum>> bridgeParts = new HashMap<String, List<BridgePartEnum>>();
        private Map<String, List<ActionEntry>> cavewalls = new HashMap<String, List<ActionEntry>>();

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
            return this.getCreationEntriesSize() + this.getFencesSize() + this.getHedgesSize() + this.getFlowerbedsSize() + this.getWallsSize() + this.getRoofs_floorsSize() + this.getBridgePartsSize() + this.getCaveWallsSize();
        }
    }
}

