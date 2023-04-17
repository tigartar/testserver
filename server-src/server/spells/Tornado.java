/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.DamageSpell;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import java.awt.geom.Ellipse2D;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tornado
extends DamageSpell
implements AttitudeConstants {
    private static Logger logger = Logger.getLogger(Tornado.class.getName());
    public static final int RANGE = 24;
    public static final double BASE_DAMAGE = 4000.0;
    public static final double DAMAGE_PER_POWER = 80.0;
    public static final int RADIUS = 2;

    public Tornado() {
        super("Tornado", 413, 15, 50, 30, 40, 120000L);
        this.targetTile = true;
        this.offensive = true;
        this.description = "covers an area with extreme winds that can damage enemies and trees";
        this.type = (byte)2;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        if (performer.getLayer() < 0) {
            performer.getCommunicator().sendNormalServerMessage("You must be above ground to cast this spell.", (byte)3);
            return false;
        }
        VolaTile t = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
        if (t != null && t.getStructure() != null) {
            performer.getCommunicator().sendNormalServerMessage("You can't cast this inside.", (byte)3);
            return false;
        }
        return true;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage("You call upon the wind of Vynora.");
        int radiusBonus = (int)(power / 40.0);
        int sx = Zones.safeTileX(tilex - 2 - radiusBonus - performer.getNumLinks());
        int sy = Zones.safeTileY(tiley - 2 - radiusBonus - performer.getNumLinks());
        int ex = Zones.safeTileX(tilex + 2 + radiusBonus + performer.getNumLinks());
        int ey = Zones.safeTileY(tiley + 2 + radiusBonus + performer.getNumLinks());
        Ellipse2D.Float circle = new Ellipse2D.Float(sx, sy, ex - sx, ey - sy);
        for (int x = sx; x < ex; ++x) {
            block5: for (int y = sy; y < ey; ++y) {
                if (!circle.contains(x, y)) continue;
                int tile = Server.surfaceMesh.getTile(x, y);
                byte type = Tiles.decodeType(tile);
                Tiles.Tile theTile = Tiles.getTile(type);
                byte data = Tiles.decodeData(tile);
                if (type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id) {
                    int worldResource = Server.getWorldResource(x, y);
                    int farmedCount = worldResource >>> 11;
                    int farmedChance = worldResource & 0x7FF;
                    farmedChance = (int)Math.min((double)farmedChance - power / 7.0, 2047.0);
                    Server.setWorldResource(x, y, (farmedCount << 11) + farmedChance);
                } else if (theTile.isNormalTree() || theTile.isMyceliumTree()) {
                    byte treeAge = FoliageAge.getAgeAsByte(data);
                    TreeData.TreeType treeType = theTile.getTreeType(data);
                    if (treeAge == 15 || Server.rand.nextInt(16 - treeAge) != 0) {
                        byte newt = Tiles.Tile.TILE_GRASS.id;
                        if (theTile.isMyceliumTree()) {
                            newt = Tiles.Tile.TILE_MYCELIUM.id;
                        }
                        Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), newt, (byte)0);
                        Server.setWorldResource(x, y, 0);
                        int templateId = 9;
                        if (treeAge >= FoliageAge.OLD_ONE.getAgeId() && treeAge < FoliageAge.SHRIVELLED.getAgeId() && !treeType.isFruitTree()) {
                            templateId = 385;
                        }
                        double sizeMod = (double)treeAge / 15.0;
                        if (!treeType.isFruitTree()) {
                            sizeMod *= 0.25;
                        }
                        double lNewRotation = Math.atan2((y << 2) + 2 - ((y << 2) + 2), (x << 2) + 2 - ((x << 2) + 2));
                        float rot = (float)(lNewRotation * 57.29577951308232);
                        try {
                            Item newItem = ItemFactory.createItem(templateId, (float)power / 5.0f, x * 4 + Server.rand.nextInt(4), y * 4 + Server.rand.nextInt(4), rot, performer.isOnSurface(), treeType.getMaterial(), (byte)0, -10L, null, treeAge);
                            newItem.setWeight((int)Math.max(1000.0, sizeMod * (double)newItem.getWeightGrams()), true);
                            newItem.setLastOwnerId(performer.getWurmId());
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        Players.getInstance().sendChangedTile(x, y, true, false);
                    }
                }
                VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
                if (t == null || t.getStructure() != null || t.getFences().length > 0) continue;
                Creature[] crets = t.getCreatures();
                int affected = 0;
                for (int c = 0; c < crets.length; ++c) {
                    if (!crets[c].isGhost() && !crets[c].isDead() && crets[c].getAttitude(performer) == 2) {
                        try {
                            byte pos = crets[c].getBody().getRandomWoundPos();
                            double damage = this.calculateDamage(crets[c], power, 4000.0, 80.0);
                            CombatEngine.addWound(performer, crets[c], (byte)0, pos, damage, 1.0f, "assault", performer.getBattle(), 0.0f, 0.0f, false, false, false, true);
                            performer.getStatus().setStunned(5.0f);
                            ++affected;
                        }
                        catch (Exception exe) {
                            logger.log(Level.WARNING, exe.getMessage(), exe);
                        }
                    }
                    if ((double)affected > power / 10.0 + (double)performer.getNumLinks()) continue block5;
                }
            }
        }
    }
}

