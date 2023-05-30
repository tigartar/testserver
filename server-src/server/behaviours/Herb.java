package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;

public enum Herb {
   GSHORT_ACORN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)575, 436, (byte)0, 5, 5, 20, 15, ModifiedBy.NEAR_OAK, 50),
   GSHORT_BARLEY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 10),
   GSHORT_BASIL(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)573, 359, (byte)0, 3, 8, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_BELLADONNA(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)573, 361, (byte)0, 3, 5, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_GALIC(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)575, 356, (byte)0, 2, 4, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_LOVAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)573, 353, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 10),
   GSHORT_NETTLES(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)575, 365, (byte)0, 2, 5, -5, -5, ModifiedBy.WOUNDED, 5),
   GSHORT_OAT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_OREGANO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)573, 357, (byte)0, 6, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_PARSLEY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)573, 358, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 5),
   GSHORT_ROSMARY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)573, 363, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 10),
   GSHORT_RYE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_SAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)573, 354, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 15),
   GSHORT_SASSAFRAS(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)573, 366, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 10),
   GSHORT_THYME(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)573, 360, (byte)0, 6, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_WHEAT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 5),
   GSHORT_WOAD(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)575, 440, (byte)0, 1, 6, 20, 15, ModifiedBy.NO_TREES, 10),
   GSHORT_CUMIN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)720, 1140, (byte)0, 4, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_GINGER(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)720, 1141, (byte)0, 5, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_NUTMEG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)720, 1142, (byte)0, 2, 8, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_PAPRIKA(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)720, 1143, (byte)0, 4, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_TURMERIC(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)720, 1144, (byte)0, 4, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_MINT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)573, 1130, (byte)0, 4, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GSHORT_FENNEL(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.SHORT, (short)575, 1132, (byte)0, 5, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_ACORN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)575, 436, (byte)0, 10, 10, 20, 10, ModifiedBy.NEAR_OAK, 50),
   GMED_BARLEY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 10),
   GMED_BASIL(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)573, 359, (byte)0, 3, 8, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_BELLADONNA(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)573, 361, (byte)0, 3, 5, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_GALIC(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)575, 356, (byte)0, 3, 5, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_LOVAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)573, 353, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 20),
   GMED_NETTLES(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)575, 365, (byte)0, 2, 5, -5, -5, ModifiedBy.WOUNDED, 10),
   GMED_OAT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_OREGANO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)573, 357, (byte)0, 6, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_PARSLEY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)573, 358, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 8),
   GMED_ROSMARY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)573, 363, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 15),
   GMED_RYE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_SAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)573, 354, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 10),
   GMED_SASSAFRAS(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)573, 366, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 10),
   GMED_THYME(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)573, 360, (byte)0, 6, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_WHEAT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 8),
   GMED_WOAD(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)575, 440, (byte)0, 1, 10, 20, 10, ModifiedBy.NO_TREES, 20),
   GMED_CUMIN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)720, 1140, (byte)0, 4, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_GINGER(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)720, 1141, (byte)0, 5, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_NUTMEG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)720, 1142, (byte)0, 2, 8, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_PAPRIKA(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)720, 1143, (byte)0, 4, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_TURMERIC(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)720, 1144, (byte)0, 4, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_MINT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)573, 1130, (byte)0, 4, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GMED_FENNEL(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.MEDIUM, (short)575, 1132, (byte)0, 5, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_ACORN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)575, 436, (byte)0, 15, 15, 20, 5, ModifiedBy.NEAR_OAK, 50),
   GTALL_BARLEY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 10),
   GTALL_BASIL(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)573, 359, (byte)0, 3, 8, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_BELLADONNA(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)573, 361, (byte)0, 3, 5, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_GALIC(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)575, 356, (byte)0, 5, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_LOVAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)573, 353, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 15),
   GTALL_NETTLES(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)575, 365, (byte)0, 2, 5, -5, -5, ModifiedBy.WOUNDED, 10),
   GTALL_OAT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_OREGANO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)573, 357, (byte)0, 6, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_PARSLEY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)573, 358, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 10),
   GTALL_ROSMARY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)573, 363, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 20),
   GTALL_RYE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_SAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)573, 354, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 12),
   GTALL_SASSAFRAS(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)573, 366, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 12),
   GTALL_THYME(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)573, 360, (byte)0, 7, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_WHEAT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 10),
   GTALL_WOAD(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)575, 440, (byte)0, 1, 20, 20, 5, ModifiedBy.NO_TREES, 30),
   GTALL_CUMIN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)720, 1140, (byte)0, 4, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_GINGER(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)720, 1141, (byte)0, 5, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_NUTMEG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)720, 1142, (byte)0, 2, 7, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_PAPRIKA(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)720, 1143, (byte)0, 4, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_TURMERIC(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)720, 1144, (byte)0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_MINT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)573, 1130, (byte)0, 6, 5, -5, -5, ModifiedBy.NOTHING, 0),
   GTALL_FENNEL(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.TALL, (short)575, 1132, (byte)0, 5, 2, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_ACORN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)575, 436, (byte)0, 20, 20, 20, 1, ModifiedBy.NEAR_OAK, 50),
   GWILD_BARLEY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 5),
   GWILD_BASIL(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)573, 359, (byte)0, 3, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_BELLADONNA(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)573, 361, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_GALIC(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)575, 356, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_LOVAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)573, 353, (byte)0, 2, 6, -5, -5, ModifiedBy.WOUNDED, 15),
   GWILD_NETTLES(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)575, 365, (byte)0, 3, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   GWILD_OAT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_OREGANO(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)573, 357, (byte)0, 5, 3, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_PARSLEY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)573, 358, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 7),
   GWILD_ROSMARY(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)573, 363, (byte)0, 6, 5, -5, -5, ModifiedBy.WOUNDED, 10),
   GWILD_RYE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_SAGE(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)573, 354, (byte)0, 6, 10, -5, -5, ModifiedBy.WOUNDED, 15),
   GWILD_SASSAFRAS(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)573, 366, (byte)0, 4, 2, -5, -5, ModifiedBy.WOUNDED, 9),
   GWILD_THYME(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)573, 360, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_WHEAT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 6),
   GWILD_WOAD(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)575, 440, (byte)0, 1, 40, 20, 1, ModifiedBy.NO_TREES, 40),
   GWILD_CUMIN(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)720, 1140, (byte)0, 2, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_GINGER(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)720, 1141, (byte)0, 2, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_NUTMEG(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)720, 1142, (byte)0, 2, 4, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_PAPRIKA(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)720, 1143, (byte)0, 2, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_TURMERIC(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)720, 1144, (byte)0, 3, 1, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_MINT(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)573, 1130, (byte)0, 6, 10, -5, -5, ModifiedBy.NOTHING, 0),
   GWILD_FENNEL(Tiles.Tile.TILE_GRASS.id, GrassData.GrowthStage.WILD, (short)575, 1132, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   STEPPE_ACORN(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)575, 436, (byte)0, 5, 5, 20, 10, ModifiedBy.NEAR_OAK, 50),
   STEPPE_BARLEY(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)572, 28, (byte)0, 1, 7, -5, -5, ModifiedBy.WOUNDED, 7),
   STEPPE_BASIL(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)573, 359, (byte)0, 8, 5, -5, -5, ModifiedBy.NOTHING, 0),
   STEPPE_BELLADONNA(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)573, 361, (byte)0, 8, 15, -5, -5, ModifiedBy.NOTHING, 0),
   STEPPE_GALIC(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)575, 356, (byte)0, 8, 3, -5, -5, ModifiedBy.WOUNDED, 10),
   STEPPE_LOVAGE(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)573, 353, (byte)0, 3, 7, -5, -5, ModifiedBy.WOUNDED, 5),
   STEPPE_NETTLES(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)575, 365, (byte)0, 2, 2, -5, -5, ModifiedBy.WOUNDED, 10),
   STEPPE_OAT(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   STEPPE_OREGANO(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)573, 357, (byte)0, 3, 6, -5, -5, ModifiedBy.NOTHING, 0),
   STEPPE_PARSLEY(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)573, 358, (byte)0, 6, 3, -5, -5, ModifiedBy.WOUNDED, 10),
   STEPPE_ROSMARY(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)573, 363, (byte)0, 8, 5, -5, -5, ModifiedBy.WOUNDED, 5),
   STEPPE_RYE(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   STEPPE_SAGE(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)573, 354, (byte)0, 6, 8, -5, -5, ModifiedBy.WOUNDED, 5),
   STEPPE_SASSAFRAS(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)573, 366, (byte)0, 8, 5, -5, -5, ModifiedBy.WOUNDED, 8),
   STEPPE_THYME(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)573, 360, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   STEPPE_WHEAT(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 9),
   STEPPE_WOAD(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)575, 440, (byte)0, 10, 46, 20, 1, ModifiedBy.NO_TREES, 50),
   STEPPE_CUMIN(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)720, 1140, (byte)0, 5, 2, -5, -5, ModifiedBy.NOTHING, 0),
   STEPPE_NUTMEG(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)720, 1142, (byte)0, 2, 5, -5, -5, ModifiedBy.NOTHING, 0),
   STEPPE_PAPRIKA(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)720, 1143, (byte)0, 3, 6, -5, -5, ModifiedBy.NOTHING, 0),
   STEPPE_MINT(Tiles.Tile.TILE_STEPPE.id, GrassData.GrowthStage.SHORT, (short)573, 1130, (byte)0, 7, 6, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_ACORN(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)575, 436, (byte)0, 5, 5, 10, 10, ModifiedBy.NEAR_OAK, 50),
   MARSH_BARLEY(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 3),
   MARSH_BASIL(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)573, 359, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_BELLADONNA(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)573, 361, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_GALIC(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)575, 356, (byte)0, 4, 8, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_LOVAGE(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)573, 353, (byte)0, 6, 6, -5, -5, ModifiedBy.WOUNDED, 4),
   MARSH_NETTLES(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)575, 365, (byte)0, 5, 4, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_OAT(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_OREGANO(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)573, 357, (byte)0, 5, 4, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_PARSLEY(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)573, 358, (byte)0, 4, 5, -5, -5, ModifiedBy.WOUNDED, 8),
   MARSH_ROSMARY(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)573, 363, (byte)0, 10, 15, -5, -5, ModifiedBy.WOUNDED, 17),
   MARSH_RYE(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_SAGE(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)573, 354, (byte)0, 3, 10, -5, -5, ModifiedBy.WOUNDED, 10),
   MARSH_SASSAFRAS(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)573, 366, (byte)0, 3, 10, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_THYME(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)573, 360, (byte)0, 7, 3, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_WHEAT(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.WOUNDED, 5),
   MARSH_WOAD(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)575, 440, (byte)0, 6, 26, 20, 20, ModifiedBy.NO_TREES, 20),
   MARSH_GINGER(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)720, 1141, (byte)0, 4, 8, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_NUTMEG(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)720, 1142, (byte)0, 6, 8, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_TURMERIC(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)720, 1144, (byte)0, 10, 15, -5, -5, ModifiedBy.NOTHING, 0),
   MARSH_FENNEL(Tiles.Tile.TILE_MARSH.id, GrassData.GrowthStage.SHORT, (short)575, 1132, (byte)0, 6, 10, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_ACORN(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)575, 436, (byte)0, 5, 5, 20, 20, ModifiedBy.NEAR_OAK, 50),
   MOSS_BARLEY(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_BASIL(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)573, 359, (byte)0, 4, 8, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_BELLADONNA(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)573, 361, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_GALIC(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)575, 356, (byte)0, 4, 8, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_LOVAGE(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)573, 353, (byte)0, 16, 16, -5, -5, ModifiedBy.WOUNDED, 20),
   MOSS_NETTLES(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)575, 365, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_OAT(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_OREGANO(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)573, 357, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_PARSLEY(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)573, 358, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_ROSMARY(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)573, 363, (byte)0, 9, 9, -5, -5, ModifiedBy.WOUNDED, 5),
   MOSS_RYE(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_SAGE(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)573, 354, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_SASSAFRAS(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)573, 366, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_THYME(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)573, 360, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_WHEAT(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_WOAD(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)575, 440, (byte)0, 6, 26, 20, 20, ModifiedBy.NO_TREES, 32),
   MOSS_CUMIN(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)720, 1140, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_GINGER(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)720, 1141, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_NUTMEG(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)720, 1142, (byte)0, 9, 3, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_PAPRIKA(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)720, 1143, (byte)0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_TURMERIC(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)720, 1144, (byte)0, 3, 2, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_MINT(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)573, 1130, (byte)0, 1, 10, -5, -5, ModifiedBy.NOTHING, 0),
   MOSS_FENNEL(Tiles.Tile.TILE_MOSS.id, GrassData.GrowthStage.SHORT, (short)575, 1132, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_ACORN(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)575, 436, (byte)0, 5, 15, 20, 20, ModifiedBy.NEAR_OAK, 40),
   PEAT_BARLEY(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_BASIL(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)573, 359, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_BELLADONNA(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)573, 361, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_GALIC(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)575, 356, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_LOVAGE(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)573, 353, (byte)0, 16, 16, -5, -5, ModifiedBy.WOUNDED, 10),
   PEAT_NETTLES(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)575, 365, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_OAT(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_OREGANO(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)573, 357, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_PARSLEY(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)573, 358, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_ROSMARY(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)573, 363, (byte)0, 15, 15, -5, -5, ModifiedBy.WOUNDED, 10),
   PEAT_RYE(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_SAGE(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)573, 354, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_SASSAFRAS(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)573, 366, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_THYME(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)573, 360, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_WHEAT(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_WOAD(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)575, 440, (byte)0, 6, 6, 20, 20, ModifiedBy.NO_TREES, 40),
   PEAT_CUMIN(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)720, 1140, (byte)0, 6, 6, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_NUTMEG(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)720, 1142, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_PAPRIKA(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)720, 1143, (byte)0, 6, 2, -5, -5, ModifiedBy.NOTHING, 0),
   PEAT_MINT(Tiles.Tile.TILE_PEAT.id, GrassData.GrowthStage.SHORT, (short)573, 1130, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_ACORN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)575, 436, (byte)0, 5, 5, 20, 10, ModifiedBy.NEAR_OAK, 50),
   TSHORT_BARLEY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_BASIL(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)573, 359, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_BELLADONNA(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)573, 361, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_GALIC(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)575, 356, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_LOVAGE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)573, 353, (byte)0, 6, 6, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_NETTLES(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)575, 365, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   TSHORT_OAT(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_OREGANO(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)573, 357, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_PARSLEY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)573, 358, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_ROSMARY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)573, 363, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_RYE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_SAGE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)573, 354, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   TSHORT_SASSAFRAS(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)573, 366, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_THYME(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)573, 360, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_WHEAT(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_WOAD(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)575, 440, (byte)0, 6, 16, 30, 20, ModifiedBy.NOTHING, 0),
   TSHORT_CUMIN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)720, 1140, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_NUTMEG(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)720, 1142, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_PAPRIKA(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)720, 1142, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TSHORT_MINT(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.SHORT, (short)573, 1130, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_ACORN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)575, 436, (byte)0, 5, 10, 20, 5, ModifiedBy.NEAR_OAK, 50),
   TMED_BARLEY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_BASIL(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)573, 359, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_BELLADONNA(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)573, 361, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_GALIC(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)575, 356, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_LOVAGE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)573, 353, (byte)0, 6, 6, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_NETTLES(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)575, 365, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   TMED_OAT(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_OREGANO(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)573, 357, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_PARSLEY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)573, 358, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_ROSMARY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)573, 363, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_RYE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_SAGE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)573, 354, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   TMED_SASSAFRAS(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)573, 366, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_THYME(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)573, 360, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_WHEAT(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_WOAD(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)575, 440, (byte)0, 6, 16, 30, 10, ModifiedBy.NOTHING, 0),
   TMED_CUMIN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)720, 1140, (byte)0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_NUTMEG(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)720, 1142, (byte)0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_PAPRIKA(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)720, 1143, (byte)0, 2, 2, -5, -5, ModifiedBy.NOTHING, 0),
   TMED_MINT(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.MEDIUM, (short)573, 1130, (byte)0, 5, 8, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_ACORN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)575, 436, (byte)0, 5, 15, 20, 1, ModifiedBy.NEAR_OAK, 50),
   TTALL_BARLEY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_BASIL(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)573, 359, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_BELLADONNA(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)573, 361, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_GALIC(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)575, 356, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_LOVAGE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)573, 353, (byte)0, 6, 6, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_NETTLES(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)575, 365, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   TTALL_OAT(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_OREGANO(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)573, 357, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_PARSLEY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)573, 358, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_ROSMARY(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)573, 363, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_RYE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_SAGE(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)573, 354, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   TTALL_SASSAFRAS(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)573, 366, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_THYME(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)573, 360, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_WHEAT(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_WOAD(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)575, 440, (byte)0, 6, 16, 30, 1, ModifiedBy.NOTHING, 0),
   TTALL_CUMIN(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)720, 1140, (byte)0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_NUTMEG(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)720, 1142, (byte)0, 4, 1, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_PAPRIKA(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)720, 1143, (byte)0, 4, 8, -5, -5, ModifiedBy.NOTHING, 0),
   TTALL_MINT(Tiles.Tile.TILE_TREE.id, GrassData.GrowthStage.TALL, (short)573, 1130, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_ACORN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)575, 436, (byte)0, 5, 5, 20, 15, ModifiedBy.NEAR_OAK, 50),
   BSHORT_BARLEY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_BASIL(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)573, 359, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_BELLADONNA(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)573, 361, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_GALIC(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)575, 356, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_LOVAGE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)573, 353, (byte)0, 6, 6, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_NETTLES(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)575, 365, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   BSHORT_OAT(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_OREGANO(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)573, 357, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_PARSLEY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)573, 358, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_ROSMARY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)573, 363, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_RYE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_SAGE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)573, 354, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   BSHORT_SASSAFRAS(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)573, 366, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_THYME(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)573, 360, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_WHEAT(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_WOAD(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.SHORT, (short)575, 440, (byte)0, 6, 16, 20, 10, ModifiedBy.NOTHING, 0),
   BSHORT_GINGER(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)720, 1141, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_NUTMEG(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)720, 1142, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_TURMERIC(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)720, 1144, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_MINT(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 1130, (byte)0, 5, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BSHORT_FENNEL(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)575, 1132, (byte)0, 10, 5, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_ACORN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)575, 436, (byte)0, 5, 10, 20, 10, ModifiedBy.NEAR_OAK, 50),
   BMED_BARLEY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_BASIL(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)573, 359, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_BELLADONNA(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)573, 361, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_GALIC(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)575, 356, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_LOVAGE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)573, 353, (byte)0, 6, 6, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_NETTLES(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)575, 365, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   BMED_OAT(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_OREGANO(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)573, 357, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_PARSLEY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)573, 358, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_ROSMARY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)573, 363, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_RYE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_SAGE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)573, 354, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   BMED_SASSAFRAS(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)573, 366, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_THYME(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)573, 360, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_WHEAT(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_WOAD(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)575, 440, (byte)0, 6, 16, 20, 5, ModifiedBy.NOTHING, 0),
   BMED_GINGER(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)720, 1141, (byte)0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_NUTMEG(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)720, 1142, (byte)0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_TURMERIC(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)720, 1144, (byte)0, 4, 8, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_MINT(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)573, 1130, (byte)0, 8, 4, -5, -5, ModifiedBy.NOTHING, 0),
   BMED_FENNEL(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.MEDIUM, (short)575, 1132, (byte)0, 4, 4, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_ACORN(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)575, 436, (byte)0, 5, 15, 20, 1, ModifiedBy.NEAR_OAK, 50),
   BTALL_BARLEY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)572, 28, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_BASIL(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 359, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_BELLADONNA(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 361, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_GALIC(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)575, 356, (byte)0, 8, 8, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_LOVAGE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 353, (byte)0, 6, 6, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_NETTLES(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)575, 365, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   BTALL_OAT(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)572, 31, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_OREGANO(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 357, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_PARSLEY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 358, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_ROSMARY(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 363, (byte)0, 5, 5, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_RYE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)572, 30, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_SAGE(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 354, (byte)0, 10, 10, -5, -5, ModifiedBy.WOUNDED, 20),
   BTALL_SASSAFRAS(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 366, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_THYME(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 360, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_WHEAT(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)572, 29, (byte)0, 1, 1, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_WOAD(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)575, 440, (byte)0, 6, 16, 20, 1, ModifiedBy.NOTHING, 0),
   BTALL_GINGER(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)720, 1141, (byte)0, 10, 10, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_NUTMEG(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)720, 1142, (byte)0, 7, 7, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_TURMERIC(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)720, 1144, (byte)0, 7, 5, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_MINT(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)573, 1130, (byte)0, 5, 3, -5, -5, ModifiedBy.NOTHING, 0),
   BTALL_FENNEL(Tiles.Tile.TILE_BUSH.id, GrassData.GrowthStage.TALL, (short)575, 1132, (byte)0, 6, 8, -5, -5, ModifiedBy.NOTHING, 0);

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

   private Herb(
      byte aTileType,
      GrassData.GrowthStage aGrassLength,
      short aCategory,
      int aItemType,
      byte aMaterial,
      int aChanceAt1,
      int aChanceAt100,
      int aDifficultyAt1,
      int aDifficultyAt100,
      ModifiedBy aModifiedBy,
      int aChanceModifier
   ) {
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
      float diff = (float)(this.difficultyAt1 + (this.difficultyAt100 - this.difficultyAt1) / 100 * knowledge);
      return diff < 0.0F ? (float)knowledge + diff : diff;
   }

   public static float getQL(double power, int knowledge) {
      return Math.min(100.0F, (float)knowledge + (float)(100 - knowledge) * ((float)power / 500.0F));
   }

   private float getChanceAt(Creature performer, int knowledge, int tilex, int tiley) {
      float chance = (float)(this.chanceAt1 + (this.chanceAt100 - this.chanceAt1) / 100 * knowledge);
      return chance + this.modifiedBy.chanceModifier(performer, this.chanceModifier, tilex, tiley);
   }

   public byte getMaterial() {
      return this.material;
   }

   public static Herb getRandomHerb(
      Creature performer, byte aTileType, GrassData.GrowthStage aGrassLength, short aCategory, int knowledge, int tilex, int tiley
   ) {
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

      float totalChance = 0.0F;

      for(Herb h : values()) {
         if (h.tileType == checkType && h.grassLength == aGrassLength) {
            float chance = h.getChanceAt(performer, knowledge, tilex, tiley);
            if (chance >= 0.0F) {
               totalChance += chance;
            }
         }
      }

      if (totalChance == 0.0F) {
         return null;
      } else {
         int rndChance = Server.rand.nextInt((int)totalChance);
         float runningChance = 0.0F;

         for(Herb h : values()) {
            if (h.tileType == checkType && (checkType != Tiles.Tile.TILE_GRASS.id || h.grassLength == aGrassLength)) {
               float chance = h.getChanceAt(performer, knowledge, tilex, tiley);
               if (chance >= 0.0F) {
                  runningChance += chance;
                  if ((float)rndChance < runningChance) {
                     if (aCategory != 224 && aCategory != h.category) {
                        return null;
                     }

                     return h;
                  }
               }
            }
         }

         return null;
      }
   }

   public static float getDifficulty(int templateId, int knowledge) {
      for(Herb h : values()) {
         if (h.tileType == Tiles.Tile.TILE_GRASS.id && h.grassLength == GrassData.GrowthStage.SHORT && h.itemType == templateId) {
            return h.getDifficultyAt(knowledge);
         }
      }

      return -1.0F;
   }
}
