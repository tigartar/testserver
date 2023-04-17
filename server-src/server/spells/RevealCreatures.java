/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;

public class RevealCreatures
extends ReligiousSpell {
    public static final int RANGE = 4;

    public RevealCreatures() {
        super("Reveal Creatures", 444, 40, 30, 25, 30, 0L);
        this.targetTile = true;
        this.description = "locates creatures nearby";
        this.type = (byte)2;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        Zone[] zones;
        performer.getCommunicator().sendNormalServerMessage("You receive insights about the area.");
        int sx = Zones.safeTileX(performer.getTileX() - 40 - performer.getNumLinks() * 5);
        int sy = Zones.safeTileY(performer.getTileY() - 40 - performer.getNumLinks() * 5);
        int ex = Zones.safeTileX(performer.getTileX() + 40 + performer.getNumLinks() * 5);
        int ey = Zones.safeTileY(performer.getTileY() + 40 + performer.getNumLinks() * 5);
        for (Zone lZone : zones = Zones.getZonesCoveredBy(sx, sy, ex, ey, performer.isOnSurface())) {
            Creature[] crets;
            for (Creature cret : crets = lZone.getAllCreatures()) {
                if (cret.getPower() > performer.getPower() || cret == performer || !(cret.getBonusForSpellEffect((byte)29) <= 0.0f)) continue;
                int mindist = Math.max(Math.abs(cret.getTileX() - performer.getTileX()), Math.abs(cret.getTileY() - performer.getTileY()));
                int dir = MethodsCreatures.getDir(performer, cret.getTileX(), cret.getTileY());
                String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                String toReturn = DbCreatureStatus.getIsLoaded(cret.getWurmId()) == 0 ? EndGameItems.getDistanceString(mindist, cret.getName(), direction, false) : "";
                performer.getCommunicator().sendNormalServerMessage(toReturn);
            }
        }
    }
}

