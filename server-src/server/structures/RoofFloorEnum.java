/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.BuildMaterial;
import com.wurmonline.server.behaviours.FloorBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstants;
import java.util.ArrayList;
import java.util.List;

public enum RoofFloorEnum {
    WOOD_SHINGLE_ROOF(StructureConstants.FloorType.ROOF, "Wood shingle", false, StructureConstants.FloorMaterial.WOOD, 60),
    SLATE_SHINGLE_ROOF(StructureConstants.FloorType.ROOF, "Slate shingle", false, StructureConstants.FloorMaterial.SLATE_SLAB, 60),
    CLAY_SHIGLE_ROOF(StructureConstants.FloorType.ROOF, "Pottery shingle", false, StructureConstants.FloorMaterial.CLAY_BRICK, 60),
    TATCHED_ROOF(StructureConstants.FloorType.ROOF, "Thatched", false, StructureConstants.FloorMaterial.THATCH, 60),
    WOODEN_PLANK_FLOOR(StructureConstants.FloorType.FLOOR, "Wooden plank", true, StructureConstants.FloorMaterial.WOOD, 60),
    WOODEN_PLANK_OPENING(StructureConstants.FloorType.OPENING, "Wooden plank", true, StructureConstants.FloorMaterial.WOOD, 60),
    CLAY_FLOOR(StructureConstants.FloorType.FLOOR, "Pottery brick", true, StructureConstants.FloorMaterial.CLAY_BRICK, 60),
    CLAY_OPENING(StructureConstants.FloorType.OPENING, "Pottery brick", true, StructureConstants.FloorMaterial.CLAY_BRICK, 60),
    SANDSTONE_SLAB_FLOOR(StructureConstants.FloorType.FLOOR, "Sandstone slab", true, StructureConstants.FloorMaterial.SANDSTONE_SLAB, 60),
    SANDSTONE_SLAB_OPENING(StructureConstants.FloorType.OPENING, "Sandstone slab", true, StructureConstants.FloorMaterial.SANDSTONE_SLAB, 60),
    STONE_SLAB_FLOOR(StructureConstants.FloorType.FLOOR, "Stone slab", true, StructureConstants.FloorMaterial.STONE_SLAB, 60),
    STONE_SLAB_OPENING(StructureConstants.FloorType.OPENING, "Stone slab", true, StructureConstants.FloorMaterial.STONE_SLAB, 60),
    STONE_BRICK_FLOOR(StructureConstants.FloorType.FLOOR, "Stone brick", true, StructureConstants.FloorMaterial.STONE_BRICK, 60),
    STONE_BRICK_OPENING(StructureConstants.FloorType.OPENING, "Stone brick", true, StructureConstants.FloorMaterial.STONE_BRICK, 60),
    SLATE_SLAB_FLOOR(StructureConstants.FloorType.FLOOR, "Slate slab", true, StructureConstants.FloorMaterial.SLATE_SLAB, 60),
    SLATE_SLAB_OPENING(StructureConstants.FloorType.OPENING, "Slate slab", true, StructureConstants.FloorMaterial.SLATE_SLAB, 60),
    MARBLE_FLOOR(StructureConstants.FloorType.FLOOR, "Marble slab", true, StructureConstants.FloorMaterial.MARBLE_SLAB, 60),
    MARBLE_OPENING(StructureConstants.FloorType.OPENING, "Marble slab", true, StructureConstants.FloorMaterial.MARBLE_SLAB, 60),
    WOODEN_PLANK_STAIRCASE(StructureConstants.FloorType.STAIRCASE, "Wooden plank", true, StructureConstants.FloorMaterial.WOOD, 60),
    CLAY_STAIRCASE(StructureConstants.FloorType.STAIRCASE, "Pottery brick", true, StructureConstants.FloorMaterial.CLAY_BRICK, 60),
    SANDSTONE_SLAB_STAIRCASE(StructureConstants.FloorType.STAIRCASE, "Sandstone slab", true, StructureConstants.FloorMaterial.SANDSTONE_SLAB, 60),
    STONE_SLAB_STAIRCASE(StructureConstants.FloorType.STAIRCASE, "Stone slab", true, StructureConstants.FloorMaterial.STONE_SLAB, 60),
    STONE_BRICK_STAIRCASE(StructureConstants.FloorType.STAIRCASE, "Stone brick", true, StructureConstants.FloorMaterial.STONE_BRICK, 60),
    SLATE_SLAB_STAIRCASE(StructureConstants.FloorType.STAIRCASE, "Slate slab", true, StructureConstants.FloorMaterial.SLATE_SLAB, 60),
    MARBLE_STAIRCASE(StructureConstants.FloorType.STAIRCASE, "Marble slab", true, StructureConstants.FloorMaterial.MARBLE_SLAB, 60),
    STANDALONE_STAIRCASE(StructureConstants.FloorType.STAIRCASE, "Standalone", true, StructureConstants.FloorMaterial.STANDALONE, 60),
    WIDE_STAIRCASE(StructureConstants.FloorType.WIDE_STAIRCASE, "No banisters", true, StructureConstants.FloorMaterial.STANDALONE, 60),
    WIDE_STAIRCASE_RIGHT(StructureConstants.FloorType.WIDE_STAIRCASE_RIGHT, "Right banisters", true, StructureConstants.FloorMaterial.STANDALONE, 60),
    WIDE_STAIRCASE_LEFT(StructureConstants.FloorType.WIDE_STAIRCASE_LEFT, "Left banisters", true, StructureConstants.FloorMaterial.STANDALONE, 60),
    WIDE_STAIRCASE_BOTH(StructureConstants.FloorType.WIDE_STAIRCASE_BOTH, "Both banisters", true, StructureConstants.FloorMaterial.STANDALONE, 60),
    WOODEN_PLANK_RIGHT_STAIRCASE(StructureConstants.FloorType.RIGHT_STAIRCASE, "Wooden plank", true, StructureConstants.FloorMaterial.WOOD, 60),
    CLAY_RIGHT_STAIRCASE(StructureConstants.FloorType.RIGHT_STAIRCASE, "Pottery brick", true, StructureConstants.FloorMaterial.CLAY_BRICK, 60),
    SANDSTONE_SLAB_RIGHT_STAIRCASE(StructureConstants.FloorType.RIGHT_STAIRCASE, "Sandstone slab", true, StructureConstants.FloorMaterial.SANDSTONE_SLAB, 60),
    STONE_SLAB_RIGHT_STAIRCASE(StructureConstants.FloorType.RIGHT_STAIRCASE, "Stone slab", true, StructureConstants.FloorMaterial.STONE_SLAB, 60),
    STONE_BRICK_RIGHT_STAIRCASE(StructureConstants.FloorType.RIGHT_STAIRCASE, "Stone brick", true, StructureConstants.FloorMaterial.STONE_BRICK, 60),
    SLATE_SLAB_RIGHT_STAIRCASE(StructureConstants.FloorType.RIGHT_STAIRCASE, "Slate slab", true, StructureConstants.FloorMaterial.SLATE_SLAB, 60),
    MARBLE_RIGHT_STAIRCASE(StructureConstants.FloorType.RIGHT_STAIRCASE, "Marble slab", true, StructureConstants.FloorMaterial.MARBLE_SLAB, 60),
    STANDALONE_RIGHT_STAIRCASE(StructureConstants.FloorType.RIGHT_STAIRCASE, "Standalone", true, StructureConstants.FloorMaterial.STANDALONE, 60),
    WOODEN_PLANK_LEFT_STAIRCASE(StructureConstants.FloorType.LEFT_STAIRCASE, "Wooden plank", true, StructureConstants.FloorMaterial.WOOD, 60),
    CLAY_LEFT_STAIRCASE(StructureConstants.FloorType.LEFT_STAIRCASE, "Pottery brick", true, StructureConstants.FloorMaterial.CLAY_BRICK, 60),
    SANDSTONE_SLAB_LEFT_STAIRCASE(StructureConstants.FloorType.LEFT_STAIRCASE, "Sandstone slab", true, StructureConstants.FloorMaterial.SANDSTONE_SLAB, 60),
    STONE_SLAB_LEFT_STAIRCASE(StructureConstants.FloorType.LEFT_STAIRCASE, "Stone slab", true, StructureConstants.FloorMaterial.STONE_SLAB, 60),
    STONE_BRICK_LEFT_STAIRCASE(StructureConstants.FloorType.LEFT_STAIRCASE, "Stone brick", true, StructureConstants.FloorMaterial.STONE_BRICK, 60),
    SLATE_SLAB_LEFT_STAIRCASE(StructureConstants.FloorType.LEFT_STAIRCASE, "Slate slab", true, StructureConstants.FloorMaterial.SLATE_SLAB, 60),
    MARBLE_LEFT_STAIRCASE(StructureConstants.FloorType.LEFT_STAIRCASE, "Marble slab", true, StructureConstants.FloorMaterial.MARBLE_SLAB, 60),
    STANDALONE_LEFT_STAIRCASE(StructureConstants.FloorType.LEFT_STAIRCASE, "Standalone", true, StructureConstants.FloorMaterial.STANDALONE, 60),
    WOODEN_PLANK_CLOCKWISE_STAIRCASE(StructureConstants.FloorType.CLOCKWISE_STAIRCASE, "Wooden plank", true, StructureConstants.FloorMaterial.WOOD, 60),
    WOODEN_PLANK_ANTICLOCKWISE_STAIRCASE(StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE, "Wooden plank", true, StructureConstants.FloorMaterial.WOOD, 60),
    WOODEN_PLANK_CLOCKWISE_STAIRCASE_WITH(StructureConstants.FloorType.CLOCKWISE_STAIRCASE_WITH, "Wooden plank", true, StructureConstants.FloorMaterial.WOOD, 60),
    WOODEN_PLANK_ANTICLOCKWISE_STAIRCASE_WITH(StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE_WITH, "Wooden plank", true, StructureConstants.FloorMaterial.WOOD, 60),
    UNKNOWN(StructureConstants.FloorType.OPENING, "Unkown", true, StructureConstants.FloorMaterial.WOOD, 60);

    private final StructureConstants.FloorType type;
    private final boolean isFloor;
    private final StructureConstants.FloorMaterial material;
    private final short actionId;
    private final String name;
    private final String actionString;
    private final short icon;
    private static int[] emptyArr;

    private RoofFloorEnum(StructureConstants.FloorType type, String name, boolean isFloor, StructureConstants.FloorMaterial material, short icon) {
        this.type = type;
        this.isFloor = isFloor;
        this.material = material;
        this.actionId = (short)(20000 + this.material.getCode());
        this.name = StringUtil.format("%s %s", name, this.type.getName());
        this.actionString = StringUtil.format("%s %s", "Building", StringUtil.toLowerCase(this.name));
        this.icon = icon;
    }

    public final ActionEntry createActionEntry() {
        return ActionEntry.createEntry(this.actionId, this.name, this.actionString, emptyArr);
    }

    public final StructureConstants.FloorType getType() {
        return this.type;
    }

    public final StructureConstants.FloorMaterial getMaterial() {
        return this.material;
    }

    public final String getName() {
        return this.name;
    }

    public final boolean isFloor() {
        return this.isFloor;
    }

    public final short getIcon() {
        return this.icon;
    }

    public final short getActionId() {
        return this.actionId;
    }

    public final boolean isValidTool(Item tool) {
        int[] valid;
        if (tool == null) {
            return false;
        }
        if (tool.getTemplateId() == 176 || tool.getTemplateId() == 315) {
            return true;
        }
        for (int v : valid = RoofFloorEnum.getValidToolsForMaterial(this.material)) {
            if (v != tool.getTemplateId()) continue;
            return true;
        }
        return false;
    }

    public static final List<RoofFloorEnum> getRoofsByTool(Item tool) {
        ArrayList<RoofFloorEnum> list = new ArrayList<RoofFloorEnum>();
        if (tool == null) {
            return list;
        }
        for (RoofFloorEnum en : RoofFloorEnum.values()) {
            if (en.isFloor() || !en.isValidTool(tool) || en == UNKNOWN) continue;
            list.add(en);
        }
        return list;
    }

    public static final RoofFloorEnum getByFloorType(Floor floor) {
        for (RoofFloorEnum en : RoofFloorEnum.values()) {
            if (en.getType() != floor.getType() || en.getMaterial() != floor.getMaterial()) continue;
            return en;
        }
        return UNKNOWN;
    }

    public static List<BuildMaterial> getMaterialsNeeded(Floor floor) {
        List<BuildMaterial> billOfMaterial = FloorBehaviour.getRequiredMaterialsAtState(floor);
        ArrayList<BuildMaterial> needed = new ArrayList<BuildMaterial>();
        for (BuildMaterial mat : billOfMaterial) {
            if (mat.getNeededQuantity() <= 0) continue;
            needed.add(mat);
        }
        return needed;
    }

    public List<BuildMaterial> getTotalMaterialsNeeded() {
        List<BuildMaterial> billOfMaterial = this.type == StructureConstants.FloorType.ROOF ? FloorBehaviour.getRequiredMaterialsForRoof(this.material) : FloorBehaviour.getRequiredMaterialsForFloor(this.type, this.material);
        return billOfMaterial;
    }

    public static final boolean canBuildFloorRoof(Floor floor, RoofFloorEnum en, Creature performer) {
        Skill skill = FloorBehaviour.getBuildSkill(en.getType(), en.getMaterial(), performer);
        if (skill == null) {
            return false;
        }
        if (skill.getKnowledge(0.0) < (double)FloorBehaviour.getRequiredBuildSkillForFloorType(en.getMaterial())) {
            return false;
        }
        if (!FloorBehaviour.mayPlanAtLevel(performer, floor.getFloorLevel(), skill, floor.isRoof(), false)) {
            return false;
        }
        return !((double)FloorBehaviour.getRequiredBuildSkillForFloorLevel(floor.getFloorLevel(), floor.isRoof()) > skill.getKnowledge(0.0));
    }

    public final int getNeededSkillNumber() {
        if (this.type == StructureConstants.FloorType.ROOF) {
            return FloorBehaviour.getSkillForRoof(this.material);
        }
        return FloorBehaviour.getSkillForFloor(this.material);
    }

    public static final Floor getFloorOrRoofFromId(long floorId) {
        byte layer;
        int heightOffset;
        int y;
        short x = Tiles.decodeTileX(floorId);
        Floor[] floors = Zones.getFloorsAtTile((int)x, y = Tiles.decodeTileY(floorId), heightOffset = Tiles.decodeHeightOffset(floorId), heightOffset, layer = Tiles.decodeLayer(floorId));
        if (floors != null && floors.length > 0) {
            return floors[0];
        }
        return null;
    }

    public static final List<RoofFloorEnum> getFloorByToolAndType(Item tool, StructureConstants.FloorType fType) {
        ArrayList<RoofFloorEnum> list = new ArrayList<RoofFloorEnum>();
        if (tool == null) {
            return list;
        }
        for (RoofFloorEnum en : RoofFloorEnum.values()) {
            if (!en.isFloor() || en == UNKNOWN || en.getType() != fType || !en.isValidTool(tool)) continue;
            list.add(en);
        }
        return list;
    }

    public static final RoofFloorEnum getFloorRoofByTypeAndMaterial(StructureConstants.FloorType type, StructureConstants.FloorMaterial material) {
        for (RoofFloorEnum en : RoofFloorEnum.values()) {
            if (en.getType() != type || en.getMaterial() != material) continue;
            return en;
        }
        return UNKNOWN;
    }

    public static final int[] getValidToolsForMaterial(StructureConstants.FloorMaterial material) {
        switch (material) {
            case METAL_STEEL: {
                return new int[]{62, 63};
            }
            case CLAY_BRICK: {
                return new int[]{493};
            }
            case SLATE_SLAB: {
                return new int[]{493};
            }
            case STONE_BRICK: {
                return new int[]{493};
            }
            case THATCH: {
                return new int[]{62, 63};
            }
            case WOOD: {
                return new int[]{62, 63};
            }
            case STONE_SLAB: {
                return new int[]{493};
            }
            case METAL_COPPER: {
                return new int[]{62, 63};
            }
            case METAL_IRON: {
                return new int[]{62, 63};
            }
            case SANDSTONE_SLAB: {
                return new int[]{493};
            }
            case MARBLE_SLAB: {
                return new int[]{493};
            }
            case METAL_GOLD: {
                return new int[]{62, 63};
            }
            case METAL_SILVER: {
                return new int[]{62, 63};
            }
            case STANDALONE: {
                return new int[]{62, 63};
            }
        }
        return new int[0];
    }

    static {
        emptyArr = new int[0];
    }
}

