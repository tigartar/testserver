/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Crops;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import java.util.Properties;

public final class TileDataQuestion
extends Question {
    private final int tilex;
    private final int tiley;

    public TileDataQuestion(Creature aResponder, String aTitle, String aQuestion, int aTilex, int aTiley, long aTarget) {
        super(aResponder, aTitle, aQuestion, 35, aTarget);
        this.tilex = aTilex;
        this.tiley = aTiley;
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        int surfResource = Server.getWorldResource(this.tilex, this.tiley);
        int surfMesh = Server.surfaceMesh.getTile(this.tilex, this.tiley);
        byte surfData = Tiles.decodeData(surfMesh);
        byte surfType = Tiles.decodeType(surfMesh);
        short surfHeight = Tiles.decodeHeight(surfMesh);
        Tiles.Tile surfTile = Tiles.getTile(surfType);
        byte surfClientFlags = Server.getClientSurfaceFlags(this.tilex, this.tiley);
        byte surfServerFlags = Server.getServerSurfaceFlags(this.tilex, this.tiley);
        int caveResource = Server.getCaveResource(this.tilex, this.tiley);
        int caveMesh = Server.caveMesh.getTile(this.tilex, this.tiley);
        byte caveData = Tiles.decodeData(caveMesh);
        byte caveType = Tiles.decodeType(caveMesh);
        short caveHeight = Tiles.decodeHeight(caveMesh);
        Tiles.Tile caveTile = Tiles.getTile(caveType);
        byte caveClientFlags = Server.getClientCaveFlags(this.tilex, this.tiley);
        byte caveServerFlags = Server.getServerCaveFlags(this.tilex, this.tiley);
        int rockMesh = Server.rockMesh.getTile(this.tilex, this.tiley);
        short rockHeight = Tiles.decodeHeight(rockMesh);
        String botUnused = " (unused)";
        String forUnused = " (unused)";
        String colUnused = " (unused)";
        String txUnused = " (unused)";
        String noGUnused = " (unused)";
        boolean canHaveGrubs = false;
        if (surfTile.isBush() || surfTile.isTree()) {
            buf.append("harray{label{text=\"Surface resource - Damage\"};input{id=\"surf\";maxchars=\"6\";text=\"" + surfResource + "\"};}");
            if (!surfTile.isEnchanted()) {
                if (surfType != Tiles.Tile.TILE_BUSH_LINGONBERRY.id) {
                    botUnused = "";
                    colUnused = "";
                }
                forUnused = "";
            }
            txUnused = "";
            if (surfTile.isTree() && (surfData >>> 4 & 0xF) == 15) {
                noGUnused = "";
                canHaveGrubs = true;
            }
            if (surfTile.isBush() && (surfData >>> 4 & 0xE) == 14) {
                noGUnused = "";
                canHaveGrubs = true;
            }
            if ((surfType == Tiles.Tile.TILE_TREE_BIRCH.id || surfType == Tiles.Tile.TILE_ENCHANTED_TREE_BIRCH.id || surfType == Tiles.Tile.TILE_MYCELIUM_TREE_BIRCH.id) && (surfData >>> 4 & 0xE) == 14) {
                noGUnused = "";
                canHaveGrubs = true;
            }
        } else if (surfType == Tiles.Tile.TILE_FIELD.id || surfType == Tiles.Tile.TILE_FIELD2.id) {
            buf.append("harray{label{text=\"Surface resource - \"};");
            buf.append("label{text=\"Farmed:\"};");
            buf.append("input{id=\"count\";maxchars=\"2\";text=\"" + (surfResource >>> 11 & 0x1F) + "\"};");
            buf.append("label{text=\"Yield chance:\"};");
            buf.append("input{id=\"surf\";maxchars=\"4\";text=\"" + (surfResource & 0x3FF) + "\"};");
            buf.append("}");
            txUnused = "";
        } else if (surfType == Tiles.Tile.TILE_SAND.id || surfType == Tiles.Tile.TILE_GRASS.id || surfType == Tiles.Tile.TILE_MYCELIUM.id || surfType == Tiles.Tile.TILE_STEPPE.id || surfType == Tiles.Tile.TILE_CLAY.id || surfType == Tiles.Tile.TILE_PEAT.id || surfType == Tiles.Tile.TILE_TAR.id || surfType == Tiles.Tile.TILE_TUNDRA.id || surfType == Tiles.Tile.TILE_MOSS.id) {
            int qlCnt = surfResource >>> 8 & 0xFF;
            int dugCnt = surfResource & 0xFF;
            buf.append("harray{label{text=\"Surface resource - Transform count\"};input{id=\"qlcnt\";maxchars=\"3\";text=\"" + qlCnt + "\"};");
            String ls = "Spare";
            if (surfType == Tiles.Tile.TILE_CLAY.id) {
                ls = "Dug Count";
            }
            buf.append("label{text=\" " + ls + "\"};input{id=\"surf\";maxchars=\"3\";text=\"" + dugCnt + "\"};label{text=\" (255 = not used)\"};}");
            if (surfType == Tiles.Tile.TILE_GRASS.id || surfType == Tiles.Tile.TILE_MYCELIUM.id || surfType == Tiles.Tile.TILE_STEPPE.id) {
                botUnused = "";
                forUnused = "";
                colUnused = "";
            } else if (surfType == Tiles.Tile.TILE_PEAT.id || surfType == Tiles.Tile.TILE_MOSS.id) {
                botUnused = "";
                forUnused = "";
            } else if (surfType == Tiles.Tile.TILE_TUNDRA.id) {
                forUnused = "";
            }
            if (surfType != Tiles.Tile.TILE_SAND.id) {
                txUnused = "";
            }
            if (surfType == Tiles.Tile.TILE_GRASS.id || surfType == Tiles.Tile.TILE_MYCELIUM.id) {
                noGUnused = "";
                canHaveGrubs = true;
            }
        } else if (surfTile.isCaveDoor()) {
            buf.append("harray{label{text=\"Surface resource - Door Strength\"};input{id=\"surf\";maxchars=\"6\";text=\"" + surfResource + "\"};}");
        } else {
            if (surfType == Tiles.Tile.TILE_DIRT.id) {
                noGUnused = "";
                canHaveGrubs = true;
            }
            buf.append("harray{label{text=\"Surface resource \"};input{id=\"surf\";maxchars=\"6\";text=\"" + surfResource + "\"};}");
        }
        buf.append("harray{label{text=\"Cave resource \"}input{id=\"cave\";maxchars=\"6\";text=\"" + caveResource + "\"};}");
        if (surfTile.isGrass()) {
            buf.append("harray{label{text=\"Grass tile data \"};");
            buf.append("label{text=\" Growth:\"};input{id=\"growth\";maxchars=\"1\";text=\"" + (surfData >>> 6 & 3) + "\"};");
            buf.append("label{text=\" GrassType(not used):\"};input{id=\"grasstype\";maxchars=\"1\";text=\"" + (surfData >>> 4 & 3) + "\"};");
            buf.append("label{text=\" Flower:\"};input{id=\"flower\";maxchars=\"2\";text=\"" + (surfData & 0xF) + "\"};");
            buf.append("}");
        } else if (surfType == Tiles.Tile.TILE_MYCELIUM.id) {
            buf.append("harray{label{text=\"Mycelium tile data \"};");
            buf.append("label{text=\" Growth:\"};input{id=\"growth\";maxchars=\"1\";text=\"" + (surfData >>> 6 & 3) + "\"};");
            buf.append("label{text=\" GrassType(not used):\"};input{id=\"grasstype\";maxchars=\"1\";text=\"" + (surfData >>> 4 & 3) + "\"};");
            buf.append("label{text=\" Flower (not used):\"};input{id=\"flower\";maxchars=\"2\";text=\"" + (surfData & 0xF) + "\"};");
            buf.append("}");
        } else if (surfType == Tiles.Tile.TILE_BUSH.id || surfType == Tiles.Tile.TILE_ENCHANTED_BUSH.id || surfType == Tiles.Tile.TILE_TREE.id || surfType == Tiles.Tile.TILE_ENCHANTED_TREE.id || surfType == Tiles.Tile.TILE_MYCELIUM_BUSH.id || surfType == Tiles.Tile.TILE_MYCELIUM_TREE.id) {
            buf.append("harray{label{text=\"Tree/Bush tile data \"};");
            buf.append("label{text=\" Age:\"};input{id=\"age\";maxchars=\"3\";text=\"" + (surfData >>> 4 & 0xF) + "\"};");
            buf.append("label{text=\" Type:\"};input{id=\"type\";maxchars=\"3\";text=\"" + (surfData & 0xF) + "\"};");
            buf.append("}");
        } else if (surfTile.usesNewData()) {
            buf.append("harray{label{text=\"Tree/Bush (new) tile data \"};");
            buf.append("label{text=\" Age:\"};input{id=\"age\";maxchars=\"3\";text=\"" + (surfData >>> 4 & 0xF) + "\"};");
            buf.append("label{text=\" Fruit:\"};input{id=\"harvestable\";maxchars=\"1\";text=\"" + (surfData >>> 3 & 1) + "\"};");
            buf.append("label{text=\" Centre:\"};input{id=\"incentre\";maxchars=\"1\";text=\"" + (surfData >>> 2 & 1) + "\"};");
            buf.append("label{text=\" Grass Length:\"};input{id=\"growth\";maxchars=\"1\";text=\"" + (surfData & 3) + "\"};");
            buf.append("}");
        } else if (surfType == Tiles.Tile.TILE_FIELD.id || surfType == Tiles.Tile.TILE_FIELD2.id) {
            buf.append("harray{label{text=\"Field" + (surfType == Tiles.Tile.TILE_FIELD2.id ? "2" : "") + " tile data \"};");
            buf.append("label{text=\" Tended:\"};checkbox{id=\"tended\";selected=\"" + Crops.decodeFieldState(surfData) + "\"};");
            buf.append("label{text=\" Age:\"};input{id=\"age\";maxchars=\"3\";text=\"" + Crops.decodeFieldAge(surfData) + "\"};");
            buf.append("label{text=\" Crop:\"};input{id=\"crop\";maxchars=\"3\";text=\"" + (surfData & 0xF) + "\"};");
            buf.append("}");
        } else if (Tiles.isRoadType(surfType)) {
            buf.append("harray{label{text=\"Surface tile data \"};");
            buf.append("label{text=\" (not used):\"};input{id=\"unused\";maxchars=\"2\";text=\"" + (surfData >>> 3 & 0x1F) + "\"};");
            buf.append("label{text=\" Dir:\"};dropdown{id='dir';options=\"none,NW,NE,SE,SW\";default=\"" + (surfData & 7) + "\"}");
            buf.append("}");
            if (surfType == Tiles.Tile.TILE_STONE_SLABS.id || surfType == Tiles.Tile.TILE_POTTERY_BRICKS.id || surfType == Tiles.Tile.TILE_SANDSTONE_BRICKS.id || surfType == Tiles.Tile.TILE_SANDSTONE_SLABS.id || surfType == Tiles.Tile.TILE_SLATE_BRICKS.id || surfType == Tiles.Tile.TILE_SLATE_SLABS.id || surfType == Tiles.Tile.TILE_MARBLE_BRICKS.id || surfType == Tiles.Tile.TILE_MARBLE_SLABS.id) {
                colUnused = "";
            }
        } else {
            buf.append("harray{label{text=\"Surface tile data \"};");
            buf.append("input{id=\"surftiledata\";maxchars=\"3\";text=\"" + surfData + "\"};");
            buf.append("}");
        }
        buf.append("harray{label{text=\"Surface Height \"};input{id=\"surfaceheight\";maxchars=\"6\";text=\"" + surfHeight + "\"}};");
        buf.append("harray{label{text=\"Rock Height    \"};input{id=\"rockheight\";maxchars=\"6\";text=\"" + rockHeight + "\"}};");
        buf.append("harray{label{text=\"Cave Height    \"};input{id=\"caveheight\";maxchars=\"6\";text=\"" + caveHeight + "\"}label{text=\"  Cave ceiling:\"};input{id=\"caveceiling\";maxchars=\"3\";text=\"" + caveData + "\"}label{text=\"  (Default is -100, 0)\"}};");
        buf.append("label{text=\"Surface Flags:\"}");
        buf.append("checkbox{id=\"bot\";selected=\"" + Server.isBotanizable(this.tilex, this.tiley) + "\";text=\"Botanize" + botUnused + "\"};");
        buf.append("checkbox{id=\"forage\";selected=\"" + Server.isForagable(this.tilex, this.tiley) + "\";text=\"Forage" + forUnused + "\"};");
        buf.append("checkbox{id=\"collect\";selected=\"" + Server.isGatherable(this.tilex, this.tiley) + "\";text=\"Collect" + colUnused + "\"};");
        buf.append("checkbox{id=\"transforming\";selected=\"" + Server.isBeingTransformed(this.tilex, this.tiley) + "\";text=\"Being Transformed" + txUnused + "\"};");
        buf.append("checkbox{id=\"transformed\";selected=\"" + Server.wasTransformed(this.tilex, this.tiley) + "\";text=\"Been Transformed" + txUnused + "\"};");
        buf.append("checkbox{id=\"hive\";selected=\"" + Server.isCheckHive(this.tilex, this.tiley) + "\";text=\"Check Hive\";hover=\"Used exclusivly by the hive generation code.\"};");
        buf.append("checkbox{id=\"hasGrubs\";enabled=\"" + canHaveGrubs + "\";selected=\"" + (canHaveGrubs && Server.hasGrubs(this.tilex, this.tiley)) + "\";text=\"Has Grubs (or Wurms)" + noGUnused + "\"};");
        buf.append("harray{label{text=\"Cave Flags:\"};label{text=\" Server:\"}input{id=\"caveserverflag\";maxchars=\"3\";text=\"" + (Server.getServerCaveFlags(this.tilex, this.tiley) & 0xFF) + "\"}label{text=\" Client:\"}input{id=\"caveclientflag\";maxchars=\"3\";text=\"" + (Server.getClientCaveFlags(this.tilex, this.tiley) & 0xFF) + "\"}};");
        if (this.getResponder().getPower() >= 2 && this.getResponder().getPower() < 5) {
            buf.append("label{type=\"bold\";text=\"Take care when changing values that you do not understand!\"}");
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(420, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        if (this.getResponder().getPower() >= 4 || Servers.isThisATestServer()) {
            QuestionParser.parseTileDataQuestion(this);
        }
    }

    int getTilex() {
        return this.tilex;
    }

    int getTiley() {
        return this.tiley;
    }
}

