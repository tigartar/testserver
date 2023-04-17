/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.ShardBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.zones.LongPosition;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zones;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

public class MoleSenses
extends ReligiousSpell {
    private static Logger logger = Logger.getLogger(MoleSenses.class.getName());
    public static final int RANGE = 4;

    public MoleSenses() {
        super("Mole Senses", 439, 30, 60, 40, 65, 0L);
        this.targetTile = true;
        this.description = "smell ores and rock depth";
        this.type = 1;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        if (performer.getLayer() < 0) {
            performer.getCommunicator().sendNormalServerMessage("This spell does not work below ground. Your senses would become overwhelmed.", (byte)3);
            return false;
        }
        return true;
    }

    private void sendOres(Creature performer, double power, int tilex, int tiley) {
        int maxRadius = Math.max(2, 1 + ShardBehaviour.calcMaxRadius(power) / 2);
        HashSet<String> ores = new HashSet<String>();
        for (int dx = maxRadius * -1; dx < maxRadius; ++dx) {
            for (int dy = maxRadius * -1; dy < maxRadius; ++dy) {
                int type = Tiles.decodeType(Server.caveMesh.getTile(tilex + dx, tiley + dy)) & 0xFF;
                if (type == 205 && power > 40.0) {
                    ores.add("slate");
                    continue;
                }
                if (type == 206 && power > 40.0) {
                    ores.add("marble");
                    continue;
                }
                if (type == 220 && power > 60.0) {
                    ores.add("gold");
                    continue;
                }
                if (type == 221 && power > 50.0) {
                    ores.add("silver");
                    continue;
                }
                if (type == 227 && power > 70.0) {
                    ores.add("adamantine");
                    continue;
                }
                if (type == 228 && power > 80.0) {
                    ores.add("glimmersteel");
                    continue;
                }
                if (type == 222) {
                    ores.add("iron");
                    continue;
                }
                if (type == 223) {
                    ores.add("copper");
                    continue;
                }
                if (type == 224) {
                    ores.add("lead");
                    continue;
                }
                if (type == 225) {
                    ores.add("zinc");
                    continue;
                }
                if (type != 226) continue;
                ores.add("tin");
            }
        }
        Iterator it = ores.iterator();
        if (ores.size() == 1) {
            performer.getCommunicator().sendNormalServerMessage("You smell traces of " + (String)it.next() + " in the dirt.");
        } else if (ores.size() > 1) {
            String s = "You smell traces of ";
            for (int i = 0; i < ores.size() - 1; ++i) {
                s = i == ores.size() - 2 ? s + (String)it.next() : s + (String)it.next() + ", ";
            }
            s = s + " and " + (String)it.next() + " in the dirt.";
            performer.getCommunicator().sendNormalServerMessage(s);
        }
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage("For a short while you go almost blind, but you smell the earth with incredible accuracy.");
        try {
            float rockheight = Zones.calculateRockHeight(performer.getPosX(), performer.getPosY());
            float surfheight = Zones.calculateHeight(performer.getPosX(), performer.getPosY(), true);
            float diff = surfheight - rockheight;
            if (diff >= 0.0f && diff < 20.0f) {
                if (diff < 1.0f) {
                    performer.getCommunicator().sendNormalServerMessage("You smell the rock less than a meter below you.");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You smell the rock less than " + (int)diff + " meters below you.");
                }
                Tiles.Tile tile = Tiles.getTile(Tiles.decodeType(Server.caveMesh.getTile(performer.getTileX(), performer.getTileY())));
                String tname = tile.tiledesc.toLowerCase();
                if (tile.id == Tiles.Tile.TILE_CAVE_WALL.id) {
                    tname = "rock";
                }
                if (diff > 0.0f) {
                    this.sendOres(performer, power, tilex, tiley);
                }
            } else if (diff > 20.0f) {
                performer.getCommunicator().sendNormalServerMessage("You fail to smell the rock here. It is probably too deep down.", (byte)3);
            }
        }
        catch (NoSuchZoneException nsz) {
            performer.getCommunicator().sendNormalServerMessage("You fail to smell the rock here. It is probably too deep down.", (byte)3);
        }
        float rot = Creature.normalizeAngle(performer.getStatus().getRotation());
        int numtiles = (int)Math.max(1.0, power / 10.0) + performer.getNumLinks() * 2;
        for (int nt = 1; nt <= numtiles; ++nt) {
            LongPosition lp = Zones.getEndTile(performer.getPosX(), performer.getPosY(), rot, nt);
            int tile = Server.caveMesh.getTile(lp.getTilex(), lp.getTiley());
            logger.info("Checking tile " + lp.getTilex() + ", " + lp.getTiley() + ", cave is " + Tiles.getTile((byte)Tiles.decodeType((int)tile)).tiledesc.toLowerCase());
            Tiles.Tile t = Tiles.getTile(Tiles.decodeType(tile));
            if (!Tiles.isOreCave(t.id)) continue;
            performer.getCommunicator().sendNormalServerMessage("You sniff " + t.tiledesc.toLowerCase() + " " + nt + " tiles away in your facing direction.");
        }
    }
}
