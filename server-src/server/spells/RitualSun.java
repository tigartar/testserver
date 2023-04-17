/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.Features;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.spells.RiteEvent;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class RitualSun
extends ReligiousSpell {
    public static final int RANGE = 4;

    public RitualSun() {
        super("Ritual of the Sun", 401, 100, 300, 60, 50, 43200000L);
        this.isRitual = true;
        this.targetItem = true;
        this.description = "damage in your gods domains is removed";
        this.type = 0;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        if (performer.getDeity() != null) {
            Deity deity = performer.getDeity();
            Deity templateDeity = Deities.getDeity(deity.getTemplateDeity());
            if (templateDeity.getFavor() < 100000 && !Servers.isThisATestServer()) {
                performer.getCommunicator().sendNormalServerMessage(deity.getName() + " can not grant that power right now.", (byte)3);
                return false;
            }
            if (target.getBless() == deity && target.isDomainItem()) {
                return true;
            }
            performer.getCommunicator().sendNormalServerMessage(String.format("You need to cast this spell at an altar of %s.", deity.getName()), (byte)3);
        }
        return false;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        Deity deity = performer.getDeity();
        Deity templateDeity = Deities.getDeity(deity.getTemplateDeity());
        performer.getCommunicator().sendNormalServerMessage(performer.getDeity().getName() + " increases protection in the lands by mending the broken!");
        Server.getInstance().broadCastSafe("As the Ritual of the Sun is completed, followers of " + deity.getName() + " may now receive a blessing!");
        HistoryManager.addHistory(performer.getName(), "casts " + this.name + ". " + performer.getDeity().getName() + " mends protections in the lands.");
        templateDeity.setFavor(templateDeity.getFavor() - 100000);
        performer.achievement(635);
        for (Creature c : performer.getLinks()) {
            c.achievement(635);
        }
        new RiteEvent.RiteOfTheSunEvent(-10, performer.getWurmId(), this.getNumber(), deity.getNumber(), System.currentTimeMillis(), 86400000L);
        if (Features.Feature.NEWDOMAINS.isEnabled()) {
            for (FaithZone f : Zones.getFaithZones()) {
                if (f == null || f.getCurrentRuler() == null || f.getCurrentRuler().getTemplateDeity() != deity.getTemplateDeity()) continue;
                try {
                    if (Zones.getFaithZone(f.getCenterX(), f.getCenterY(), true) != f) {
                    }
                }
                catch (NoSuchZoneException e) {}
                continue;
                for (int tx = f.getStartX(); tx < f.getEndX(); ++tx) {
                    for (int ty = f.getStartY(); ty < f.getEndY(); ++ty) {
                        this.effectTile(tx, ty);
                    }
                }
            }
        } else {
            FaithZone[][] surfaceZones = Zones.getFaithZones(true);
            for (int x = 0; x < Zones.faithSizeX; ++x) {
                for (int y = 0; y < Zones.faithSizeY; ++y) {
                    if (surfaceZones[x][y].getCurrentRuler().getTemplateDeity() != deity.getTemplateDeity()) continue;
                    for (int tx = surfaceZones[x][y].getStartX(); tx <= surfaceZones[x][y].getEndX(); ++tx) {
                        for (int ty = surfaceZones[x][y].getStartY(); ty <= surfaceZones[x][y].getEndY(); ++ty) {
                            this.effectTile(tx, ty);
                        }
                    }
                }
            }
        }
    }

    private void effectTile(int tx, int ty) {
        VolaTile t = Zones.getTileOrNull(tx, ty, true);
        if (t != null) {
            Wall[] walls = t.getWalls();
            for (Wall wall : walls) {
                wall.setDamage(0.0f);
            }
            for (TimeConstants timeConstants : t.getFloors()) {
                ((Floor)timeConstants).setDamage(0.0f);
            }
            for (TimeConstants timeConstants : t.getFences()) {
                ((Fence)timeConstants).setDamage(0.0f);
            }
            for (TimeConstants timeConstants : t.getBridgeParts()) {
                ((BridgePart)timeConstants).setDamage(0.0f);
            }
        }
    }
}

