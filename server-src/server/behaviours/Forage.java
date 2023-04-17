/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.ModifiedBy;
import com.wurmonline.server.creatures.Creature;

public enum Forage {
    GSHORT_BLUEBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    GSHORT_CORN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 32, 0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_COTTON(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 15),
    GSHORT_LINGONBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    GSHORT_ONION(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_POTATO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_PUMPKIN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 33, 0, 15, 15, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_STRAWBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 571, 362, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GSHORT_WEMP_PLANT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 316, 0, 10, 10, -5, -5, ModifiedBy.NO_TREES, 10),
    GSHORT_EASTER_EGG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 466, 0, 0, 0, -5, -5, ModifiedBy.EASTER, 20),
    GSHORT_RICE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 40),
    GSHORT_IVY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 917, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_HOPS(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 1275, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_ROCK(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 684, 0, 4, 0, 0, 4, ModifiedBy.NOTHING, 0),
    GSHORT_SPROUT_HAZELNUT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 266, 71, 5, 1, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GSHORT_SPROUT_ORANGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 266, 88, 5, 1, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GSHORT_SPROUT_RASPBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 266, 90, 10, 10, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GSHORT_SPROUT_BLUEBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 266, 91, 10, 10, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GSHORT_CAROT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 1133, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_CABBAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 1134, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_TOMATO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 1135, 0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_SUGARBEET(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 1136, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_LETTUCE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 1137, 0, 2, 5, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_PEAPOD(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 1138, 0, 5, 2, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_COCOABEAN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 1155, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GSHORT_RASPBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GSHORT_CUCUMBER(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 569, 1247, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GSHORT_EGG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, 570, 464, 0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_BLUEBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    GMED_CORN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 32, 0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_COTTON(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    GMED_LINGONBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    GMED_ONION(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_POTATO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_PUMPKIN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 33, 0, 15, 15, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_STRAWBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 571, 362, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GMED_WEMP_PLANT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 316, 0, 10, 10, -5, -5, ModifiedBy.NO_TREES, 10),
    GMED_EASTER_EGG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 571, 466, 0, 0, 0, -5, -5, ModifiedBy.EASTER, 20),
    GMED_RICE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 30),
    GMED_IVY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 917, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_HOPS(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 1275, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_ROCK(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 684, 0, 4, 0, 0, 4, ModifiedBy.NOTHING, 0),
    GMED_CAROT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 1133, 0, 3, 2, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_CABBAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 1134, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_TOMATO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 1135, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_SUGARBEET(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 1136, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_LETTUCE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 1137, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_PEAPOD(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 1138, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GMAD_COCOABEAN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 1155, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_RASPBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GMED_CUCUMBER(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 569, 1247, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GMED_EGG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 464, 0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
    GMED_SPROUT_HAZELNUT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 266, 71, 5, 1, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GMED_SPROUT_ORANGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 266, 88, 5, 1, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GMED_SPROUT_RASPBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 266, 90, 10, 10, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GMED_SPROUT_BLUEBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, 570, 266, 91, 10, 10, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GTALL_BLUEBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    GTALL_CORN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 32, 0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_COTTON(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    GTALL_LINGONBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    GTALL_ONION(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_POTATO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_PUMPKIN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 33, 0, 15, 15, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_STRAWBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 571, 362, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GTALL_WEMP_PLANT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 316, 0, 10, 10, -5, -5, ModifiedBy.NO_TREES, 10),
    GTALL_EASTER_EGG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 571, 466, 0, 0, 0, -5, -5, ModifiedBy.EASTER, 20),
    GTALL_RICE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 20),
    GTALL_IVY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 917, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_HOPS(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 1275, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_ROCK(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 684, 0, 4, 0, 0, 4, ModifiedBy.NOTHING, 0),
    GTALL_CAROT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 1133, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_CABBAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 1134, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_TOMATO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 1135, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_SUGARBEET(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 1136, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_LETTUCE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 1137, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_PEAPOD(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 1138, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_COCOABEAN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 1155, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_RASPBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GTALL_CUCUMBER(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 569, 1247, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GTALL_EGG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 464, 0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
    GTALL_SPROUT_HAZELNUT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 266, 71, 5, 1, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GTALL_SPROUT_ORANGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 266, 88, 5, 1, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GTALL_SPROUT_RASPBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 266, 90, 10, 10, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GTALL_SPROUT_BLUEBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, 570, 266, 91, 10, 10, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GWILD_BLUEBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    GWILD_CORN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 32, 0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_COTTON(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    GWILD_LINGONBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    GWILD_ONION(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_POTATO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_PUMPKIN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 33, 0, 15, 15, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_STRAWBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 571, 362, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GWILD_WEMP_PLANT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 316, 0, 10, 10, -5, -5, ModifiedBy.NO_TREES, 10),
    GWILD_EASTER_EGG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 571, 466, 0, 0, 0, -5, -5, ModifiedBy.EASTER, 20),
    GWILD_RICE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 10),
    GWILD_IVY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 917, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_HOPS(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 1275, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_ROCK(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 684, 0, 4, 0, 0, 4, ModifiedBy.NOTHING, 0),
    GWILD_CAROT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 1133, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_CABBAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 1134, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_TOMATO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 1135, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_SUGARBEET(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 1136, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_LETTUCE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 1137, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_PEAPOD(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 1138, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_COCOABEAN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 1155, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_RASPBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GWILD_CUCUMBER(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 569, 1247, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    GWILD_EGG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 464, 0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
    GWILD_SPROUT_HAZELNUT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 266, 71, 5, 1, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GWILD_SPROUT_ORANGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 266, 88, 5, 1, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GWILD_SPROUT_RASPBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 266, 90, 10, 10, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    GWILD_SPROUT_BLUEBERRY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, 570, 266, 91, 10, 10, -10, -10, ModifiedBy.NEAR_BUSH, 10),
    STEPPE_BLUEBERRY(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    STEPPE_CORN(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 32, 0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_COTTON(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    STEPPE_LINGONBERRY(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    STEPPE_ONION(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_POTATO(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_PUMPKIN(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 33, 0, 15, 15, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_STRAWBERRY(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 571, 362, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    STEPPE_WEMP_PLANT(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 570, 316, 0, 10, 10, -5, -5, ModifiedBy.NO_TREES, 10),
    STEPPE_RICE(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 20),
    STEPPE_CAROT(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 1133, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_CABBAGE(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 1134, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_TOMATO(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 1135, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_SUGARBEET(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 1136, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_LETTUCE(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 1137, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_PEAPOD(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 1138, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_COCOABEAN(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 570, 1155, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    STEPPE_RASPBERRY(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    STEPPE_CUCUMBER(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, 569, 1247, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    TUNDRA_BLUEBERRY(Tiles.Tile.TILE_TUNDRA.id, GrassData.GrowthStage.SHORT, 571, 364, 0, 25, 25, 10, 10, ModifiedBy.NO_TREES, 10),
    TUNDRA_COTTON(Tiles.Tile.TILE_TUNDRA.id, GrassData.GrowthStage.SHORT, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    TUNDRA_LINGONBERRY(Tiles.Tile.TILE_TUNDRA.id, GrassData.GrowthStage.SHORT, 571, 367, 0, 20, 20, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    TUNDRA_STRAWBERRY(Tiles.Tile.TILE_TUNDRA.id, GrassData.GrowthStage.SHORT, 571, 362, 0, 15, 15, -5, -5, ModifiedBy.HUNGER, 10),
    TUNDRA_COCOABEAN(Tiles.Tile.TILE_TUNDRA.id, GrassData.GrowthStage.SHORT, 570, 1155, 0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
    TUNDRA_RASPBERRY(Tiles.Tile.TILE_TUNDRA.id, GrassData.GrowthStage.SHORT, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    MARSH_BLUEBERRY(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    MARSH_CORN(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 569, 32, 0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
    MARSH_COTTON(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    MARSH_MUSHROOM_RED(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 569, 251, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    MARSH_ONION(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    MARSH_POTATO(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    MARSH_PUMPKIN(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 569, 33, 0, 15, 15, -5, -5, ModifiedBy.NOTHING, 0),
    MARSH_STRAWBERRY(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 571, 362, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 20),
    MARSH_WEMP_PLANT(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 570, 316, 0, 10, 10, -5, -5, ModifiedBy.NO_TREES, 10),
    MARSH_RICE(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 50),
    MARSH_SUGARBEET(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 569, 1136, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    MARSH_COCOABEAN(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 570, 1155, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    MARSH_RASPBERRY(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    MARSH_CUCUMBER(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, 569, 1247, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    TSHORT_BLUEBERRY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    TSHORT_BRANCH(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 570, 688, 0, 18, 0, 0, 18, ModifiedBy.NOTHING, 0),
    TSHORT_CORN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 569, 32, 0, 12, 12, -5, -5, ModifiedBy.NOTHING, 0),
    TSHORT_COTTON(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    TSHORT_LINGONBERRY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    TSHORT_MUSHROOM_BLACK(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 569, 247, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TSHORT_MUSHROOM_BLUE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 569, 250, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TSHORT_MUSHROOM_BROWN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 569, 248, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TSHORT_MUSHROOM_GREEN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 569, 246, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TSHORT_MUSHROOM_RED(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 569, 251, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TSHORT_MUSHROOM_YELLOW(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 569, 249, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TSHORT_ONION(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    TSHORT_POTATO(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    TSHORT_RICE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 20),
    TSHORT_COCOABEAN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 570, 1155, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    TSHORT_RASPBERRY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    TMED_BLUEBERRY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    TMED_BRANCH(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 570, 688, 0, 18, 0, 0, 18, ModifiedBy.NOTHING, 0),
    TMED_CORN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 569, 32, 0, 12, 12, -5, -5, ModifiedBy.NOTHING, 0),
    TMED_COTTON(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    TMED_LINGONBERRY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    TMED_MUSHROOM_BLACK(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 569, 247, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TMED_MUSHROOM_BLUE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 569, 250, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TMED_MUSHROOM_BROWN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 569, 248, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TMED_MUSHROOM_GREEN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 569, 246, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TMED_MUSHROOM_RED(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 569, 251, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TMED_MUSHROOM_YELLOW(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 569, 249, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TMED_ONION(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    TMED_POTATO(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    TMED_RICE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 20),
    TMED_COCOABEAN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 570, 1155, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    TMED_RASPBERRY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    TTALL_BLUEBERRY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    TTALL_BRANCH(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 570, 688, 0, 18, 0, 0, 18, ModifiedBy.NOTHING, 0),
    TTALL_CORN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 569, 32, 0, 12, 12, -5, -5, ModifiedBy.NOTHING, 0),
    TTALL_COTTON(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    TTALL_LINGONBERRY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    TTALL_MUSHROOM_BLACK(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 569, 247, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TTALL_MUSHROOM_BLUE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 569, 250, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TTALL_MUSHROOM_BROWN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 569, 248, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TTALL_MUSHROOM_GREEN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 569, 246, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TTALL_MUSHROOM_RED(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 569, 251, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TTALL_MUSHROOM_YELLOW(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 569, 249, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    TTALL_ONION(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    TTALL_POTATO(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    TTALL_RICE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 20),
    TTALL_COCOABEAN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 570, 1155, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    TTALL_RASPBERRY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    BSHORT_BLUEBERRY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    BSHORT_CORN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 32, 0, 12, 12, -5, -5, ModifiedBy.NOTHING, 0),
    BSHORT_COTTON(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    BSHORT_LINGONBERRY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    BSHORT_MUSHROOM_BLACK(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 247, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BSHORT_MUSHROOM_BLUE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 250, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BSHORT_MUSHROOM_BROWN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 248, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BSHORT_MUSHROOM_GREEN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 246, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BSHORT_MUSHROOM_RED(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 251, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BSHORT_MUSHROOM_YELLOW(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 249, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BSHORT_ONION(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    BSHORT_POTATO(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    BSHORT_RICE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 20),
    BSHORT_TOMATO(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 1135, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    BSHORT_SUGARBEET(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 1136, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    BSHORT_PEAPOD(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 569, 1138, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    BSHORT_COCOABEAN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 570, 1155, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    BSHORT_RASPBERRY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    BMED_BLUEBERRY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    BMED_CORN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 32, 0, 12, 12, -5, -5, ModifiedBy.NOTHING, 0),
    BMED_COTTON(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    BMED_LINGONBERRY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    BMED_MUSHROOM_BLACK(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 247, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BMED_MUSHROOM_BLUE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 250, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BMED_MUSHROOM_BROWN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 248, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BMED_MUSHROOM_GREEN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 246, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BMED_MUSHROOM_RED(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 251, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BMED_MUSHROOM_YELLOW(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 249, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BMED_ONION(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    BMED_POTATO(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    BMED_RICE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 20),
    BMED_TOMATO(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 1135, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    BMED_SUGARBEET(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 1136, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    BMED_PEAPOD(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 569, 1138, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    BMED_COCOABEAN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 570, 1155, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    BMED_RASPBERRY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24),
    BTALL_BLUEBERRY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 571, 364, 0, 5, 5, 10, 10, ModifiedBy.NOTHING, 0),
    BTALL_CORN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 32, 0, 12, 12, -5, -5, ModifiedBy.NOTHING, 0),
    BTALL_COTTON(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 570, 144, 0, 5, 5, -5, -5, ModifiedBy.WOUNDED, 12),
    BTALL_LINGONBERRY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 571, 367, 0, 6, 6, -5, -5, ModifiedBy.NEAR_BUSH, 12),
    BTALL_MUSHROOM_BLACK(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 247, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BTALL_MUSHROOM_BLUE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 250, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BTALL_MUSHROOM_BROWN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 248, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BTALL_MUSHROOM_GREEN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 246, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BTALL_MUSHROOM_RED(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 251, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BTALL_MUSHROOM_YELLOW(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 249, 0, 0, 0, -5, -5, ModifiedBy.NEAR_TREE, 12),
    BTALL_ONION(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 355, 0, 1, 20, -5, -5, ModifiedBy.NOTHING, 0),
    BTALL_POTATO(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 35, 0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
    BTALL_RICE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 746, 0, 1, 1, -10, -10, ModifiedBy.NEAR_WATER, 20),
    BTALL_TOMATO(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 1135, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    BTALL_SUGARBEET(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 1136, 0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
    BTALL_PEAPOD(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 569, 1138, 0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
    BTALL_COCOABEAN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 570, 1155, 0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
    BTALL_RASPBERRY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, 571, 1196, 0, 5, 5, -5, -5, ModifiedBy.HUNGER, 24);

    private final byte tileType;
    private final GrassData.GrowthStage grassLength;
    private final short category;
    private final int itemType;
    private final byte material;
    private final int chanceAt1;
    private final int chanceAt100;
    private final int difficultyAt1;
    private final int difficultyAt100;
    private final ModifiedBy modifiedBy;
    private final int chanceModifier;

    private Forage(byte aTileType, GrassData.GrowthStage aGrassLength, short aCategory, int aItemType, byte aMaterial, int aChanceAt1, int aChanceAt100, int aDifficultyAt1, int aDifficultyAt100, ModifiedBy aModifiedBy, int aChanceModifier) {
        this.tileType = aTileType;
        this.grassLength = aGrassLength;
        this.category = aCategory;
        this.itemType = aItemType;
        this.material = aMaterial;
        this.chanceAt1 = aChanceAt1;
        this.chanceAt100 = aChanceAt100;
        this.difficultyAt1 = aDifficultyAt1;
        this.difficultyAt100 = aDifficultyAt100;
        this.modifiedBy = aModifiedBy;
        this.chanceModifier = aChanceModifier;
    }

    public int getItem() {
        return this.itemType;
    }

    public float getDifficultyAt(int knowledge) {
        float diff = this.difficultyAt1 + (this.difficultyAt100 - this.difficultyAt1) / 100 * knowledge;
        if (diff < 0.0f) {
            return (float)knowledge + diff;
        }
        return diff;
    }

    public static float getQL(double power, int knowledge) {
        return Math.min(100.0f, (float)knowledge + (float)(100 - knowledge) * ((float)power / 500.0f));
    }

    private float getChanceAt(Creature performer, int knowledge, int tilex, int tiley) {
        float chance = this.chanceAt1 + (this.chanceAt100 - this.chanceAt1) / 100 * knowledge;
        return chance + this.modifiedBy.chanceModifier(performer, this.chanceModifier, tilex, tiley);
    }

    public byte getMaterial() {
        return this.material;
    }

    public static Forage getRandomForage(Creature performer, byte aTileType, GrassData.GrowthStage aGrassLength, short aCategory, int knowledge, int tilex, int tiley) {
        byte checkType = aTileType;
        Tiles.Tile theTile = Tiles.getTile(aTileType);
        if (theTile.isMycelium()) {
            checkType = Tiles.Tile.TILE_GRASS.id;
        }
        if (theTile.isNormalTree() || theTile.isMyceliumTree()) {
            checkType = Tiles.Tile.TILE_TREE.id;
        }
        if (theTile.isNormalBush() || theTile.isMyceliumBush()) {
            checkType = Tiles.Tile.TILE_BUSH.id;
        }
        float totalChance = 0.0f;
        for (Forage f : Forage.values()) {
            float chance;
            if (f.tileType != checkType || f.grassLength != aGrassLength || !((chance = f.getChanceAt(performer, knowledge, tilex, tiley)) >= 0.0f)) continue;
            totalChance += chance;
        }
        if (totalChance == 0.0f) {
            return null;
        }
        int rndChance = Server.rand.nextInt((int)totalChance);
        float runningChance = 0.0f;
        for (Forage f : Forage.values()) {
            float chance;
            if (f.tileType != checkType || checkType == Tiles.Tile.TILE_GRASS.id && f.grassLength != aGrassLength || !((chance = f.getChanceAt(performer, knowledge, tilex, tiley)) >= 0.0f) || !((float)rndChance < (runningChance += chance))) continue;
            if (aCategory == 223 || aCategory == f.category) {
                return f;
            }
            return null;
        }
        return null;
    }

    public static float getDifficulty(int templateId, int knowledge) {
        for (Forage f : Forage.values()) {
            if (f.tileType != Tiles.Tile.TILE_GRASS.id || f.grassLength != GrassData.GrowthStage.SHORT || f.itemType != templateId) continue;
            return f.getDifficultyAt(knowledge);
        }
        return -1.0f;
    }
}

