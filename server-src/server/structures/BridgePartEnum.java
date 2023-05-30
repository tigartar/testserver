package com.wurmonline.server.structures;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.BridgePartBehaviour;
import com.wurmonline.server.behaviours.BuildAllMaterials;
import com.wurmonline.server.behaviours.BuildMaterial;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import java.util.ArrayList;
import java.util.List;

public enum BridgePartEnum {
   WOOD_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_NARROW, "Wood abutment with two walls", false, BridgeConstants.BridgeMaterial.WOOD, (short)440
   ),
   WOOD_ABUTMENT_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.ABUTMENT_LEFT, "Wood abutment with wall on left", false, BridgeConstants.BridgeMaterial.WOOD, (short)440
   ),
   WOOD_ABUTMENT_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.ABUTMENT_RIGHT, "Wood abutment with wall on right", false, BridgeConstants.BridgeMaterial.WOOD, (short)440
   ),
   WOOD_CROWN_WITH_2_WALLS(BridgeConstants.BridgeType.CROWN_NARROW, "Wood crown with two walls", false, BridgeConstants.BridgeMaterial.WOOD, (short)441),
   WOOD_CROWN_WITH_1_WALL(BridgeConstants.BridgeType.CROWN_SIDE, "Wood crown with one wall", false, BridgeConstants.BridgeMaterial.WOOD, (short)441),
   WOOD_SUPPORT_WITH_2_WALLS(BridgeConstants.BridgeType.SUPPORT_NARROW, "Wood support with two walls", false, BridgeConstants.BridgeMaterial.WOOD, (short)442),
   WOOD_SUPPORT_WITH_1_WALL(BridgeConstants.BridgeType.SUPPORT_SIDE, "Wood support with one wall", false, BridgeConstants.BridgeMaterial.WOOD, (short)442),
   BRICK_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_NARROW, "Brick abutment with two walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)443
   ),
   BRICK_ABUTMENT_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.ABUTMENT_LEFT, "Brick abutment with wall on left", false, BridgeConstants.BridgeMaterial.BRICK, (short)443
   ),
   BRICK_ABUTMENT_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.ABUTMENT_RIGHT, "Brick abutment with wall on right", false, BridgeConstants.BridgeMaterial.BRICK, (short)443
   ),
   BRICK_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_CENTER, "Brick abutment with no walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)443
   ),
   BRICK_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.BRACING_NARROW, "Brick bracing with two walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)444
   ),
   BRICK_BRACING_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.BRACING_LEFT, "Brick bracing with wall on left", false, BridgeConstants.BridgeMaterial.BRICK, (short)444
   ),
   BRICK_BRACING_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.BRACING_RIGHT, "Brick bracing with wall on right", false, BridgeConstants.BridgeMaterial.BRICK, (short)444
   ),
   BRICK_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.BRACING_CENTER, "Brick bracing with no walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)444
   ),
   BRICK_CROWN_WITH_2_WALLS(BridgeConstants.BridgeType.CROWN_NARROW, "Brick crown with two walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)445),
   BRICK_CROWN_WITH_1_WALL(BridgeConstants.BridgeType.CROWN_SIDE, "Brick crown with one wall", false, BridgeConstants.BridgeMaterial.BRICK, (short)445),
   BRICK_CROWN_WITH_NO_WALLS(BridgeConstants.BridgeType.CROWN_CENTER, "Brick crown with no walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)445),
   BRICK_DOUBLE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.DOUBLE_NARROW, "Brick double bracing with two walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)446
   ),
   BRICK_DOUBLE_BRACING_WITH_1_WALL(
      BridgeConstants.BridgeType.DOUBLE_SIDE, "Brick double bracing with one wall", false, BridgeConstants.BridgeMaterial.BRICK, (short)446
   ),
   BRICK_DOUBLE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.DOUBLE_CENTER, "Brick double bracing with no walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)446
   ),
   BRICK_DOUBLE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.END_NARROW, "Brick double abutment with two walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)447
   ),
   BRICK_DOUBLE_ABUTMENT_WITH_1_WALL(
      BridgeConstants.BridgeType.END_SIDE, "Brick double abutment with one wall", false, BridgeConstants.BridgeMaterial.BRICK, (short)447
   ),
   BRICK_DOUBLE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.END_CENTER, "Brick double abutment with no walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)447
   ),
   BRICK_FLOATING_WITH_2_WALLS(
      BridgeConstants.BridgeType.FLOATING_NARROW, "Brick floating with two walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)448
   ),
   BRICK_FLOATING_WITH_1_WALL(
      BridgeConstants.BridgeType.FLOATING_SIDE, "Brick floating with one wall", false, BridgeConstants.BridgeMaterial.BRICK, (short)448
   ),
   BRICK_FLOATING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.FLOATING_CENTER, "Brick floating with no walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)448
   ),
   BRICK_SUPPORT_WITH_2_WALLS(
      BridgeConstants.BridgeType.SUPPORT_NARROW, "Brick support with two walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)449
   ),
   BRICK_SUPPORT_WITH_1_WALL(BridgeConstants.BridgeType.SUPPORT_SIDE, "Brick support with one wall", false, BridgeConstants.BridgeMaterial.BRICK, (short)449),
   BRICK_SUPPORT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.SUPPORT_CENTER, "Brick support with no walls", false, BridgeConstants.BridgeMaterial.BRICK, (short)449
   ),
   MARBLE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_NARROW, "Marble abutment with two walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)450
   ),
   MARBLE_ABUTMENT_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.ABUTMENT_LEFT, "Marble abutment with wall on left", false, BridgeConstants.BridgeMaterial.MARBLE, (short)450
   ),
   MARBLE_ABUTMENT_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.ABUTMENT_RIGHT, "Marble abutment with wall on right", false, BridgeConstants.BridgeMaterial.MARBLE, (short)450
   ),
   MARBLE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_CENTER, "Marble abutment with no walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)450
   ),
   MARBLE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.BRACING_NARROW, "Marble bracing with two walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)451
   ),
   MARBLE_BRACING_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.BRACING_LEFT, "Marble bracing with wall on left", false, BridgeConstants.BridgeMaterial.MARBLE, (short)451
   ),
   MARBLE_BRACING_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.BRACING_RIGHT, "Marble bracing with wall on right", false, BridgeConstants.BridgeMaterial.MARBLE, (short)451
   ),
   MARBLE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.BRACING_CENTER, "Marble bracing with no walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)451
   ),
   MARBLE_CROWN_WITH_2_WALLS(BridgeConstants.BridgeType.CROWN_NARROW, "Marble crown with two walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)452),
   MARBLE_CROWN_WITH_1_WALL(BridgeConstants.BridgeType.CROWN_SIDE, "Marble crown with one wall", false, BridgeConstants.BridgeMaterial.MARBLE, (short)452),
   MARBLE_CROWN_WITH_NO_WALLS(BridgeConstants.BridgeType.CROWN_CENTER, "Marble crown with no walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)452),
   MARBLE_DOUBLE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.DOUBLE_NARROW, "Marble double bracing with two walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)453
   ),
   MARBLE_DOUBLE_BRACING_WITH_1_WALL(
      BridgeConstants.BridgeType.DOUBLE_SIDE, "Marble double bracing with one wall", false, BridgeConstants.BridgeMaterial.MARBLE, (short)453
   ),
   MARBLE_DOUBLE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.DOUBLE_CENTER, "Marble double bracing with no walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)453
   ),
   MARBLE_DOUBLE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.END_NARROW, "Marble double abutment with two walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)454
   ),
   MARBLE_DOUBLE_ABUTMENT_WITH_1_WALL(
      BridgeConstants.BridgeType.END_SIDE, "Marble double abutment with one wall", false, BridgeConstants.BridgeMaterial.MARBLE, (short)454
   ),
   MARBLE_DOUBLE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.END_CENTER, "Marble double abutment with no walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)454
   ),
   MARBLE_FLOATING_WITH_2_WALLS(
      BridgeConstants.BridgeType.FLOATING_NARROW, "Marble floating with two walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)455
   ),
   MARBLE_FLOATING_WITH_1_WALL(
      BridgeConstants.BridgeType.FLOATING_SIDE, "Marble floating with one wall", false, BridgeConstants.BridgeMaterial.MARBLE, (short)455
   ),
   MARBLE_FLOATING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.FLOATING_CENTER, "Marble floating with no walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)455
   ),
   MARBLE_SUPPORT_WITH_2_WALLS(
      BridgeConstants.BridgeType.SUPPORT_NARROW, "Marble support with two walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)456
   ),
   MARBLE_SUPPORT_WITH_1_WALL(
      BridgeConstants.BridgeType.SUPPORT_SIDE, "Marble support with one wall", false, BridgeConstants.BridgeMaterial.MARBLE, (short)456
   ),
   MARBLE_SUPPORT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.SUPPORT_CENTER, "Marble support with no walls", false, BridgeConstants.BridgeMaterial.MARBLE, (short)456
   ),
   SLATE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_NARROW, "Slate abutment with two walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)430
   ),
   SLATE_ABUTMENT_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.ABUTMENT_LEFT, "Slate abutment with wall on left", false, BridgeConstants.BridgeMaterial.SLATE, (short)430
   ),
   SLATE_ABUTMENT_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.ABUTMENT_RIGHT, "Slate abutment with wall on right", false, BridgeConstants.BridgeMaterial.SLATE, (short)430
   ),
   SLATE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_CENTER, "Slate abutment with no walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)430
   ),
   SLATE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.BRACING_NARROW, "Slate bracing with two walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)431
   ),
   SLATE_BRACING_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.BRACING_LEFT, "Slate bracing with wall on left", false, BridgeConstants.BridgeMaterial.SLATE, (short)431
   ),
   SLATE_BRACING_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.BRACING_RIGHT, "Slate bracing with wall on right", false, BridgeConstants.BridgeMaterial.SLATE, (short)431
   ),
   SLATE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.BRACING_CENTER, "Slate bracing with no walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)431
   ),
   SLATE_CROWN_WITH_2_WALLS(BridgeConstants.BridgeType.CROWN_NARROW, "Slate crown with two walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)432),
   SLATE_CROWN_WITH_1_WALL(BridgeConstants.BridgeType.CROWN_SIDE, "Slate crown with one wall", false, BridgeConstants.BridgeMaterial.SLATE, (short)432),
   SLATE_CROWN_WITH_NO_WALLS(BridgeConstants.BridgeType.CROWN_CENTER, "Slate crown with no walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)432),
   SLATE_DOUBLE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.DOUBLE_NARROW, "Slate double bracing with two walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)433
   ),
   SLATE_DOUBLE_BRACING_WITH_1_WALL(
      BridgeConstants.BridgeType.DOUBLE_SIDE, "Slate double bracing with one wall", false, BridgeConstants.BridgeMaterial.SLATE, (short)433
   ),
   SLATE_DOUBLE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.DOUBLE_CENTER, "Slate double bracing with no walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)433
   ),
   SLATE_DOUBLE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.END_NARROW, "Slate double abutment with two walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)434
   ),
   SLATE_DOUBLE_ABUTMENT_WITH_1_WALL(
      BridgeConstants.BridgeType.END_SIDE, "Slate double abutment with one wall", false, BridgeConstants.BridgeMaterial.SLATE, (short)434
   ),
   SLATE_DOUBLE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.END_CENTER, "Slate double abutment with no walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)434
   ),
   SLATE_FLOATING_WITH_2_WALLS(
      BridgeConstants.BridgeType.FLOATING_NARROW, "Slate floating with two walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)435
   ),
   SLATE_FLOATING_WITH_1_WALL(
      BridgeConstants.BridgeType.FLOATING_SIDE, "Slate floating with one wall", false, BridgeConstants.BridgeMaterial.SLATE, (short)435
   ),
   SLATE_FLOATING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.FLOATING_CENTER, "Slate floating with no walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)435
   ),
   SLATE_SUPPORT_WITH_2_WALLS(
      BridgeConstants.BridgeType.SUPPORT_NARROW, "Slate support with two walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)436
   ),
   SLATE_SUPPORT_WITH_1_WALL(BridgeConstants.BridgeType.SUPPORT_SIDE, "Slate support with one wall", false, BridgeConstants.BridgeMaterial.SLATE, (short)436),
   SLATE_SUPPORT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.SUPPORT_CENTER, "Slate support with no walls", false, BridgeConstants.BridgeMaterial.SLATE, (short)436
   ),
   ROUNDED_STONE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_NARROW, "Rounded stone abutment with two walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)410
   ),
   ROUNDED_STONE_ABUTMENT_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.ABUTMENT_LEFT, "Rounded stone abutment with wall on left", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)410
   ),
   ROUNDED_STONE_ABUTMENT_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.ABUTMENT_RIGHT, "Rounded stone abutment with wall on right", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)410
   ),
   ROUNDED_STONE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_CENTER, "Rounded stone abutment with no walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)410
   ),
   ROUNDED_STONE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.BRACING_NARROW, "Rounded stone bracing with two walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)411
   ),
   ROUNDED_STONE_BRACING_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.BRACING_LEFT, "Rounded stone bracing with wall on left", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)411
   ),
   ROUNDED_STONE_BRACING_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.BRACING_RIGHT, "Rounded stone bracing with wall on right", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)411
   ),
   ROUNDED_STONE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.BRACING_CENTER, "Rounded stone bracing with no walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)411
   ),
   ROUNDED_STONE_CROWN_WITH_2_WALLS(
      BridgeConstants.BridgeType.CROWN_NARROW, "Rounded stone crown with two walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)412
   ),
   ROUNDED_STONE_CROWN_WITH_1_WALL(
      BridgeConstants.BridgeType.CROWN_SIDE, "Rounded stone crown with one wall", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)412
   ),
   ROUNDED_STONE_CROWN_WITH_NO_WALLS(
      BridgeConstants.BridgeType.CROWN_CENTER, "Rounded stone crown with no walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)412
   ),
   ROUNDED_STONE_DOUBLE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.DOUBLE_NARROW, "Rounded stone double bracing with two walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)413
   ),
   ROUNDED_STONE_DOUBLE_BRACING_WITH_1_WALL(
      BridgeConstants.BridgeType.DOUBLE_SIDE, "Rounded stone double bracing with one wall", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)413
   ),
   ROUNDED_STONE_DOUBLE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.DOUBLE_CENTER, "Rounded stone double bracing with no walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)413
   ),
   ROUNDED_STONE_DOUBLE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.END_NARROW, "Rounded stone double abutment with two walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)414
   ),
   ROUNDED_STONE_DOUBLE_ABUTMENT_WITH_1_WALL(
      BridgeConstants.BridgeType.END_SIDE, "Rounded stone double abutment with one wall", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)414
   ),
   ROUNDED_STONE_DOUBLE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.END_CENTER, "Rounded stone double abutment with no walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)414
   ),
   ROUNDED_STONE_FLOATING_WITH_2_WALLS(
      BridgeConstants.BridgeType.FLOATING_NARROW, "Rounded stone floating with two walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)415
   ),
   ROUNDED_STONE_FLOATING_WITH_1_WALL(
      BridgeConstants.BridgeType.FLOATING_SIDE, "Rounded stone floating with one wall", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)415
   ),
   ROUNDED_STONE_FLOATING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.FLOATING_CENTER, "Rounded stone floating with no walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)415
   ),
   ROUNDED_STONE_SUPPORT_WITH_2_WALLS(
      BridgeConstants.BridgeType.SUPPORT_NARROW, "Rounded stone support with two walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)416
   ),
   ROUNDED_STONE_SUPPORT_WITH_1_WALL(
      BridgeConstants.BridgeType.SUPPORT_SIDE, "Rounded stone support with one wall", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)416
   ),
   ROUNDED_STONE_SUPPORT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.SUPPORT_CENTER, "Rounded stone support with no walls", false, BridgeConstants.BridgeMaterial.ROUNDED_STONE, (short)416
   ),
   POTTERY_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_NARROW, "Pottery abutment with two walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)390
   ),
   POTTERY_ABUTMENT_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.ABUTMENT_LEFT, "Pottery abutment with wall on left", false, BridgeConstants.BridgeMaterial.POTTERY, (short)390
   ),
   POTTERY_ABUTMENT_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.ABUTMENT_RIGHT, "Pottery abutment with wall on right", false, BridgeConstants.BridgeMaterial.POTTERY, (short)390
   ),
   POTTERY_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_CENTER, "Pottery abutment with no walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)390
   ),
   POTTERY_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.BRACING_NARROW, "Pottery bracing with two walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)391
   ),
   POTTERY_BRACING_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.BRACING_LEFT, "Pottery bracing with wall on left", false, BridgeConstants.BridgeMaterial.POTTERY, (short)391
   ),
   POTTERY_BRACING_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.BRACING_RIGHT, "Pottery bracing with wall on right", false, BridgeConstants.BridgeMaterial.POTTERY, (short)391
   ),
   POTTERY_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.BRACING_CENTER, "Pottery bracing with no walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)391
   ),
   POTTERY_CROWN_WITH_2_WALLS(
      BridgeConstants.BridgeType.CROWN_NARROW, "Pottery crown with two walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)392
   ),
   POTTERY_CROWN_WITH_1_WALL(BridgeConstants.BridgeType.CROWN_SIDE, "Pottery crown with one wall", false, BridgeConstants.BridgeMaterial.POTTERY, (short)392),
   POTTERY_CROWN_WITH_NO_WALLS(
      BridgeConstants.BridgeType.CROWN_CENTER, "Pottery crown with no walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)392
   ),
   POTTERY_DOUBLE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.DOUBLE_NARROW, "Pottery double bracing with two walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)393
   ),
   POTTERY_DOUBLE_BRACING_WITH_1_WALL(
      BridgeConstants.BridgeType.DOUBLE_SIDE, "Pottery double bracing with one wall", false, BridgeConstants.BridgeMaterial.POTTERY, (short)393
   ),
   POTTERY_DOUBLE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.DOUBLE_CENTER, "Pottery double bracing with no walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)393
   ),
   POTTERY_DOUBLE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.END_NARROW, "Pottery double abutment with two walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)394
   ),
   POTTERY_DOUBLE_ABUTMENT_WITH_1_WALL(
      BridgeConstants.BridgeType.END_SIDE, "Pottery double abutment with one wall", false, BridgeConstants.BridgeMaterial.POTTERY, (short)394
   ),
   POTTERY_DOUBLE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.END_CENTER, "Pottery double abutment with no walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)394
   ),
   POTTERY_FLOATING_WITH_2_WALLS(
      BridgeConstants.BridgeType.FLOATING_NARROW, "Pottery floating with two walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)395
   ),
   POTTERY_FLOATING_WITH_1_WALL(
      BridgeConstants.BridgeType.FLOATING_SIDE, "Pottery floating with one wall", false, BridgeConstants.BridgeMaterial.POTTERY, (short)395
   ),
   POTTERY_FLOATING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.FLOATING_CENTER, "Pottery floating with no walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)395
   ),
   POTTERY_SUPPORT_WITH_2_WALLS(
      BridgeConstants.BridgeType.SUPPORT_NARROW, "Pottery support with two walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)396
   ),
   POTTERY_SUPPORT_WITH_1_WALL(
      BridgeConstants.BridgeType.SUPPORT_SIDE, "Pottery support with one wall", false, BridgeConstants.BridgeMaterial.POTTERY, (short)396
   ),
   POTTERY_SUPPORT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.SUPPORT_CENTER, "Pottery support with no walls", false, BridgeConstants.BridgeMaterial.POTTERY, (short)396
   ),
   SANDSTONE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_NARROW, "Sandstone abutment with two walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)370
   ),
   SANDSTONE_ABUTMENT_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.ABUTMENT_LEFT, "Sandstone abutment with wall on left", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)370
   ),
   SANDSTONE_ABUTMENT_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.ABUTMENT_RIGHT, "Sandstone abutment with wall on right", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)370
   ),
   SANDSTONE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_CENTER, "Sandstone abutment with no walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)370
   ),
   SANDSTONE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.BRACING_NARROW, "Sandstone bracing with two walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)371
   ),
   SANDSTONE_BRACING_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.BRACING_LEFT, "Sandstone bracing with wall on left", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)371
   ),
   SANDSTONE_BRACING_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.BRACING_RIGHT, "Sandstone bracing with wall on right", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)371
   ),
   SANDSTONE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.BRACING_CENTER, "Sandstone bracing with no walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)371
   ),
   SANDSTONE_CROWN_WITH_2_WALLS(
      BridgeConstants.BridgeType.CROWN_NARROW, "Sandstone crown with two walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)372
   ),
   SANDSTONE_CROWN_WITH_1_WALL(
      BridgeConstants.BridgeType.CROWN_SIDE, "Sandstone crown with one wall", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)372
   ),
   SANDSTONE_CROWN_WITH_NO_WALLS(
      BridgeConstants.BridgeType.CROWN_CENTER, "Sandstone crown with no walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)372
   ),
   SANDSTONE_DOUBLE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.DOUBLE_NARROW, "Sandstone double bracing with two walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)373
   ),
   SANDSTONE_DOUBLE_BRACING_WITH_1_WALL(
      BridgeConstants.BridgeType.DOUBLE_SIDE, "Sandstone double bracing with one wall", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)373
   ),
   SANDSTONE_DOUBLE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.DOUBLE_CENTER, "Sandstone double bracing with no walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)373
   ),
   SANDSTONE_DOUBLE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.END_NARROW, "Sandstone double abutment with two walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)374
   ),
   SANDSTONE_DOUBLE_ABUTMENT_WITH_1_WALL(
      BridgeConstants.BridgeType.END_SIDE, "Sandstone double abutment with one wall", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)374
   ),
   SANDSTONE_DOUBLE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.END_CENTER, "Sandstone double abutment with no walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)374
   ),
   SANDSTONE_FLOATING_WITH_2_WALLS(
      BridgeConstants.BridgeType.FLOATING_NARROW, "Sandstone floating with two walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)375
   ),
   SANDSTONE_FLOATING_WITH_1_WALL(
      BridgeConstants.BridgeType.FLOATING_SIDE, "Sandstone floating with one wall", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)375
   ),
   SANDSTONE_FLOATING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.FLOATING_CENTER, "Sandstone floating with no walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)375
   ),
   SANDSTONE_SUPPORT_WITH_2_WALLS(
      BridgeConstants.BridgeType.SUPPORT_NARROW, "Sandstone support with two walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)376
   ),
   SANDSTONE_SUPPORT_WITH_1_WALL(
      BridgeConstants.BridgeType.SUPPORT_SIDE, "Sandstone support with one wall", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)376
   ),
   SANDSTONE_SUPPORT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.SUPPORT_CENTER, "Sandstone support with no walls", false, BridgeConstants.BridgeMaterial.SANDSTONE, (short)376
   ),
   RENDERED_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_NARROW, "Rendered abutment with two walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)350
   ),
   RENDERED_ABUTMENT_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.ABUTMENT_LEFT, "Rendered abutment with wall on left", false, BridgeConstants.BridgeMaterial.RENDERED, (short)350
   ),
   RENDERED_ABUTMENT_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.ABUTMENT_RIGHT, "Rendered abutment with wall on right", false, BridgeConstants.BridgeMaterial.RENDERED, (short)350
   ),
   RENDERED_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.ABUTMENT_CENTER, "Rendered abutment with no walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)350
   ),
   RENDERED_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.BRACING_NARROW, "Rendered bracing with two walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)351
   ),
   RENDERED_BRACING_WITH_WALL_ON_LEFT(
      BridgeConstants.BridgeType.BRACING_LEFT, "Rendered bracing with wall on left", false, BridgeConstants.BridgeMaterial.RENDERED, (short)351
   ),
   RENDERED_BRACING_WITH_WALL_ON_RIGHT(
      BridgeConstants.BridgeType.BRACING_RIGHT, "Rendered bracing with wall on right", false, BridgeConstants.BridgeMaterial.RENDERED, (short)351
   ),
   RENDERED_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.BRACING_CENTER, "Rendered bracing with no walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)351
   ),
   RENDERED_CROWN_WITH_2_WALLS(
      BridgeConstants.BridgeType.CROWN_NARROW, "Rendered crown with two walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)352
   ),
   RENDERED_CROWN_WITH_1_WALL(
      BridgeConstants.BridgeType.CROWN_SIDE, "Rendered crown with one wall", false, BridgeConstants.BridgeMaterial.RENDERED, (short)352
   ),
   RENDERED_CROWN_WITH_NO_WALLS(
      BridgeConstants.BridgeType.CROWN_CENTER, "Rendered crown with no walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)352
   ),
   RENDERED_DOUBLE_BRACING_WITH_2_WALLS(
      BridgeConstants.BridgeType.DOUBLE_NARROW, "Rendered double bracing with two walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)353
   ),
   RENDERED_DOUBLE_BRACING_WITH_1_WALL(
      BridgeConstants.BridgeType.DOUBLE_SIDE, "Rendered double bracing with one wall", false, BridgeConstants.BridgeMaterial.RENDERED, (short)353
   ),
   RENDERED_DOUBLE_BRACING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.DOUBLE_CENTER, "Rendered double bracing with no walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)353
   ),
   RENDERED_DOUBLE_ABUTMENT_WITH_2_WALLS(
      BridgeConstants.BridgeType.END_NARROW, "Rendered double abutment with two walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)354
   ),
   RENDERED_DOUBLE_ABUTMENT_WITH_1_WALL(
      BridgeConstants.BridgeType.END_SIDE, "Rendered double abutment with one wall", false, BridgeConstants.BridgeMaterial.RENDERED, (short)354
   ),
   RENDERED_DOUBLE_ABUTMENT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.END_CENTER, "Rendered double abutment with no walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)354
   ),
   RENDERED_FLOATING_WITH_2_WALLS(
      BridgeConstants.BridgeType.FLOATING_NARROW, "Rendered floating with two walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)355
   ),
   RENDERED_FLOATING_WITH_1_WALL(
      BridgeConstants.BridgeType.FLOATING_SIDE, "Rendered floating with one wall", false, BridgeConstants.BridgeMaterial.RENDERED, (short)355
   ),
   RENDERED_FLOATING_WITH_NO_WALLS(
      BridgeConstants.BridgeType.FLOATING_CENTER, "Rendered floating with no walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)355
   ),
   RENDERED_SUPPORT_WITH_2_WALLS(
      BridgeConstants.BridgeType.SUPPORT_NARROW, "Rendered support with two walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)356
   ),
   RENDERED_SUPPORT_WITH_1_WALL(
      BridgeConstants.BridgeType.SUPPORT_SIDE, "Rendered support with one wall", false, BridgeConstants.BridgeMaterial.RENDERED, (short)356
   ),
   RENDERED_SUPPORT_WITH_NO_WALLS(
      BridgeConstants.BridgeType.SUPPORT_CENTER, "Rendered support with no walls", false, BridgeConstants.BridgeMaterial.RENDERED, (short)356
   ),
   ROPE_ABUTMENT(BridgeConstants.BridgeType.ABUTMENT_NARROW, "Rope abutment", false, BridgeConstants.BridgeMaterial.ROPE, (short)457),
   ROPE_CROWN(BridgeConstants.BridgeType.CROWN_NARROW, "Rope crown", false, BridgeConstants.BridgeMaterial.ROPE, (short)458),
   ROPE_DOUBLE_ABUTMENT(BridgeConstants.BridgeType.END_NARROW, "Rope double abutment", false, BridgeConstants.BridgeMaterial.ROPE, (short)459),
   UNKNOWN(BridgeConstants.BridgeType.ABUTMENT_CENTER, "Unkown", true, BridgeConstants.BridgeMaterial.WOOD, (short)60);

   private final BridgeConstants.BridgeType type;
   private final boolean isFloor;
   private final BridgeConstants.BridgeMaterial material;
   private final short actionId;
   private final String name;
   private final String actionString;
   private final short icon;
   private static int[] emptyArr = new int[0];

   private BridgePartEnum(BridgeConstants.BridgeType type, String name, boolean isFloor, BridgeConstants.BridgeMaterial material, short icon) {
      this.type = type;
      this.isFloor = isFloor;
      this.material = material;
      this.actionId = (short)(20000 + this.material.getCode());
      this.name = name;
      this.actionString = StringUtil.format("%s %s", "Building", StringUtil.toLowerCase(this.name));
      this.icon = icon;
   }

   public final ActionEntry createActionEntry() {
      return ActionEntry.createEntry(this.actionId, this.name, this.actionString, emptyArr);
   }

   public final BridgeConstants.BridgeType getType() {
      return this.type;
   }

   public final BridgeConstants.BridgeMaterial getMaterial() {
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
      if (tool == null) {
         return false;
      } else {
         int[] valid = getValidToolsForMaterial(this.material);

         for(int v : valid) {
            if (v == tool.getTemplateId()) {
               return true;
            }
         }

         return false;
      }
   }

   public static final List<BridgePartEnum> getRoofsByTool(Item tool) {
      List<BridgePartEnum> list = new ArrayList<>();
      if (tool == null) {
         return list;
      } else {
         for(BridgePartEnum en : values()) {
            if (!en.isFloor() && en.isValidTool(tool) && en != UNKNOWN) {
               list.add(en);
            }
         }

         return list;
      }
   }

   public static final BridgePartEnum getByBridgePartType(BridgePart bridgePart) {
      for(BridgePartEnum en : values()) {
         if (en.getType() == bridgePart.getType() && en.getMaterial() == bridgePart.getMaterial()) {
            return en;
         }
      }

      return UNKNOWN;
   }

   public static BuildAllMaterials getMaterialsNeeded(BridgePart bridgePart) {
      BuildAllMaterials billOfMaterial = BridgePartBehaviour.getRequiredMaterials(bridgePart);
      return billOfMaterial.getRemainingMaterialsNeeded();
   }

   public List<BuildMaterial> getTotalMaterialsNeeded() {
      BuildAllMaterials billOfMaterial = BridgePartBehaviour.getRequiredMaterials(this.type, this.material, 0);
      return billOfMaterial.getTotalMaterialsNeeded();
   }

   public static final boolean canBuildBridgePlan(BridgePart bridgePart, BridgePartEnum en, Creature performer) {
      Skill skill = BridgePartBehaviour.getBuildSkill(en.getType(), en.getMaterial(), performer);
      if (skill == null) {
         return false;
      } else {
         return !(skill.getKnowledge(0.0) < 40.0);
      }
   }

   public final int getNeededSkillNumber() {
      return BridgePartBehaviour.getRequiredSkill(this.material);
   }

   public static final BridgePart getBridgePartFromId(long bridgePartId) {
      int x = Tiles.decodeTileX(bridgePartId);
      int y = Tiles.decodeTileY(bridgePartId);
      int layer = Tiles.decodeLayer(bridgePartId);
      BridgePart[] bridgeParts = Zones.getBridgePartsAtTile(x, y, layer == 0);
      return bridgeParts.length > 0 ? bridgeParts[0] : null;
   }

   public static final List<BridgePartEnum> getBridgeByToolAndType(Item tool, BridgeConstants.BridgeType fType) {
      List<BridgePartEnum> list = new ArrayList<>();
      if (tool == null) {
         return list;
      } else {
         for(BridgePartEnum en : values()) {
            if (en != UNKNOWN && en.getType() == fType && en.isValidTool(tool)) {
               list.add(en);
            }
         }

         return list;
      }
   }

   public static final BridgePartEnum getBridgePartByTypeAndMaterial(BridgeConstants.BridgeType type, BridgeConstants.BridgeMaterial material) {
      for(BridgePartEnum en : values()) {
         if (en.getType() == type && en.getMaterial() == material) {
            return en;
         }
      }

      return UNKNOWN;
   }

   public static final int[] getValidToolsForMaterial(BridgeConstants.BridgeMaterial material) {
      switch(material) {
         case ROPE:
            return new int[]{14};
         case BRICK:
         case MARBLE:
         case SLATE:
         case ROUNDED_STONE:
         case POTTERY:
         case SANDSTONE:
         case RENDERED:
            return new int[]{493};
         case WOOD:
            return new int[]{62, 63};
         default:
            return new int[0];
      }
   }
}
