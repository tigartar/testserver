/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.Features;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Fish;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.MethodsFishing;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Logger;

public final class Locates {
    private static final Logger logger = Logger.getLogger(Locates.class.getName());

    private Locates() {
    }

    static void locateSpring(Creature performer, Item pendulum, Skill primSkill) {
        int[] closest = Zones.getClosestSpring(performer.getTileX(), performer.getTileY(), (int)(10.0f * Locates.getMaterialPendulumModifier(pendulum.getMaterial())));
        int max = Math.max(closest[0], closest[1]);
        double knowl = primSkill.getKnowledge(pendulum.getCurrentQualityLevel());
        float difficulty = Server.rand.nextFloat() * (float)(max + 3) * 30.0f;
        double result = (double)Server.rand.nextFloat() * knowl * 10.0;
        result -= (double)difficulty;
        Server.getInstance().broadCastAction(performer.getName() + " lets out a mild sigh as " + performer.getHeSheItString() + " starts breathing again.", performer, 5);
        if (closest[0] == -1) {
            performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " seems dead.");
        } else if (result > 0.0) {
            if (max < 1) {
                performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " now swings frantically! There is something here!");
            } else if (max < 2) {
                performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " swings rapidly back and forth! You are close to a water source!");
            } else if (max < 3) {
                performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " is swinging in a circle, there is probably a water source in the ground nearby.");
            } else if (max < 5) {
                performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " is starting to move, indicating a flow of energy somewhere near.");
            } else if (result > 30.0) {
                performer.getCommunicator().sendNormalServerMessage("You think you detect some faint tugs in the " + pendulum.getName() + ".");
            } else {
                performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " seems dead.");
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " seems dead.");
        }
    }

    static void useLocateItem(Creature performer, Item pendulum, Skill primSkill) {
        if (pendulum.getSpellLocChampBonus() > 0.0f) {
            Locates.locateChamp(performer, pendulum, primSkill);
        } else if (pendulum.getSpellLocEnemyBonus() > 0.0f) {
            Locates.locateEnemy(performer, pendulum, primSkill);
        } else if (pendulum.getSpellLocFishBonus() > 0.0f) {
            if (Servers.isThisATestServer() && performer.isOnSurface()) {
                performer.getCommunicator().sendAlertServerMessage("New fishing...");
                Locates.locateFish(performer, pendulum, primSkill, true);
                performer.getCommunicator().sendAlertServerMessage("Old fishing...");
                Locates.locateFish(performer, pendulum, primSkill, false);
            } else {
                Locates.locateFish(performer, pendulum, primSkill, true);
            }
        }
    }

    static void locateChamp(Creature performer, Item pendulum, Skill primSkill) {
        int dist;
        int y;
        int x = performer.getTileX();
        Creature firstChamp = Locates.findFirstCreature(x, y = performer.getTileY(), dist = (int)(pendulum.getSpellLocChampBonus() / 100.0f * (float)Zones.worldTileSizeX / 32.0f * Locates.getMaterialPendulumModifier(pendulum.getMaterial())), performer.isOnSurface(), true, performer);
        if (firstChamp != null) {
            int dy;
            int dx = Math.abs(x - firstChamp.getTileX());
            int maxd = (int)Math.sqrt(dx * dx + (dy = Math.abs(y - firstChamp.getTileY())) * dy);
            if (primSkill.skillCheck((float)maxd / 10.0f, pendulum, 0.0, false, 5.0f) > 0.0) {
                int dir = MethodsCreatures.getDir(performer, firstChamp.getTileX(), firstChamp.getTileY());
                String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                String toReturn = EndGameItems.getDistanceString(maxd, firstChamp.getName(), direction, false);
                performer.getCommunicator().sendNormalServerMessage(toReturn);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
            }
        } else if (primSkill.skillCheck(10.0, pendulum, 0.0, false, 5.0f) > 0.0) {
            performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " doesn't seem to move.");
        } else {
            performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
        }
    }

    static void locateEnemy(Creature performer, Item pendulum, Skill primSkill) {
        int dist;
        int y;
        int x = performer.getTileX();
        Creature firstEnemy = Locates.findFirstCreature(x, y = performer.getTileY(), dist = (int)(pendulum.getSpellLocEnemyBonus() * Locates.getMaterialPendulumModifier(pendulum.getMaterial())), performer.isOnSurface(), false, performer);
        if (firstEnemy != null) {
            int dy;
            int dx = Math.abs(x - firstEnemy.getTileX());
            int maxd = (int)Math.sqrt(dx * dx + (dy = Math.abs(y - firstEnemy.getTileY())) * dy);
            if (primSkill.skillCheck((float)maxd / 10.0f, pendulum, 0.0, false, 5.0f) > 0.0) {
                int dir = MethodsCreatures.getDir(performer, firstEnemy.getTileX(), firstEnemy.getTileY());
                String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                String toReturn = EndGameItems.getDistanceString(maxd, "enemy", direction, false);
                performer.getCommunicator().sendNormalServerMessage(toReturn);
                Locates.locateTraitor(performer, pendulum, primSkill);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
            }
        } else if (primSkill.skillCheck(10.0, pendulum, 0.0, false, 5.0f) > 0.0) {
            if (!Locates.locateTraitor(performer, pendulum, primSkill)) {
                performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " doesn't seem to move.");
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
        }
    }

    static boolean locateTraitor(Creature performer, Item pendulum, Skill primSkill) {
        Creature[] possibleTraitors = EpicServerStatus.getCurrentTraitors();
        if (possibleTraitors != null) {
            int maxDist = (int)(pendulum.getSpellLocEnemyBonus() / 100.0f * (float)Zones.worldTileSizeX / 16.0f * Locates.getMaterialPendulumModifier(pendulum.getMaterial()));
            for (Creature c : possibleTraitors) {
                if (!performer.isWithinDistanceTo(c, (float)maxDist)) continue;
                int dx = Math.abs(performer.getTileX() - c.getTileX());
                int dy = Math.abs(performer.getTileY() - c.getTileY());
                int maxd = (int)Math.sqrt(dx * dx + dy * dy);
                int dir = MethodsCreatures.getDir(performer, c.getTileX(), c.getTileY());
                String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                String toReturn = EndGameItems.getDistanceString(maxd, c.getName(), direction, false);
                performer.getCommunicator().sendNormalServerMessage(toReturn);
                return true;
            }
        }
        return false;
    }

    static void locateFish(Creature performer, Item pendulum, Skill primSkill, boolean newFishing) {
        Point[] points;
        if (!performer.isOnSurface()) {
            performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " does not move.");
            return;
        }
        int maxDist = (int)(pendulum.getSpellLocFishBonus() / 10.0f * Locates.getMaterialPendulumModifier(pendulum.getMaterial()));
        if (newFishing) {
            int season = WurmCalendar.getSeasonNumber();
            points = MethodsFishing.getSpecialSpots(performer.getTileX(), performer.getTileY(), season);
        } else {
            points = Fish.getRareSpots(performer.getTileX(), performer.getTileY());
        }
        boolean found = false;
        for (Point point : points) {
            if (!performer.isWithinTileDistanceTo(point.getX(), point.getY(), 0, maxDist + 5)) continue;
            Locates.sendFishFound(point.getX(), point.getY(), point.getH(), performer, primSkill, pendulum);
            found = true;
        }
        if (!found) {
            performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
        }
    }

    private static final void sendFishFound(int targx, int targy, int fish, Creature performer, Skill primSkill, Item pendulum) {
        int x = performer.getTileX();
        int y = performer.getTileY();
        if (fish > 0) {
            int dy;
            int dx = Math.max(0, Math.abs(x - targx) - 5);
            int maxd = (int)Math.sqrt(dx * dx + (dy = Math.max(0, Math.abs(y - targy) - 5)) * dy);
            double skillCheck = primSkill.skillCheck(maxd, pendulum, 0.0, false, 5.0f);
            if (skillCheck > 0.0) {
                String name;
                int dir = MethodsCreatures.getDir(performer, targx, targy);
                String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                String spot = "fishing spot";
                if (skillCheck > 75.0 && (name = ItemTemplateFactory.getInstance().getTemplateName(fish)).length() > 0) {
                    spot = name + " fishing spot";
                }
                String loc = "";
                if (performer.getPower() >= 2) {
                    loc = " (" + targx + "," + targy + ")";
                }
                String toReturn = EndGameItems.getDistanceString(maxd, spot + loc, direction, false);
                performer.getCommunicator().sendNormalServerMessage(toReturn);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You feel there is something there but cannot determine what.");
            }
        } else if (primSkill.skillCheck(10.0, pendulum, 0.0, false, 5.0f) > 0.0) {
            performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " doesn't seem to move.");
        } else {
            performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
        }
    }

    static final Creature findFirstCreature(int x, int y, int maxdist, boolean surfaced, boolean champ, Creature performer) {
        for (int tdist = 0; tdist <= maxdist; ++tdist) {
            Creature c;
            if (!(tdist == 0 ? (c = Locates.getCreatureOnTile(x, y, tdist, surfaced, champ, performer)) != null : (c = Locates.findCreatureOnRow(x, y, tdist, surfaced, champ, performer)) != null)) continue;
            return c;
        }
        return null;
    }

    private static final Creature findCreatureOnRow(int x, int y, int dist, boolean surfaced, boolean champ, Creature performer) {
        Creature toreturn;
        int ty;
        Creature toReturn;
        int tx;
        for (tx = x; tx < x + dist; ++tx) {
            if (tx >= Zones.worldTileSizeX || tx <= 0 || (toReturn = Locates.getCreatureOnTile(tx, y - dist, dist, surfaced, champ, performer)) == null) continue;
            return toReturn;
        }
        for (ty = y - dist; ty < y; ++ty) {
            if (ty >= Zones.worldTileSizeY || ty <= 0 || (toreturn = Locates.getCreatureOnTile(x + dist, dist, ty, surfaced, champ, performer)) == null) continue;
            return toreturn;
        }
        for (ty = y; ty <= y + dist; ++ty) {
            if (ty >= Zones.worldTileSizeY || ty <= 0 || (toreturn = Locates.getCreatureOnTile(x + dist, dist, ty, surfaced, champ, performer)) == null) continue;
            return toreturn;
        }
        for (tx = x; tx < x + dist; ++tx) {
            if (tx >= Zones.worldTileSizeX || tx <= 0 || (toReturn = Locates.getCreatureOnTile(tx, y + dist, dist, surfaced, champ, performer)) == null) continue;
            return toReturn;
        }
        for (ty = y - dist; ty < y; ++ty) {
            if (ty >= Zones.worldTileSizeY || ty <= 0 || (toreturn = Locates.getCreatureOnTile(x - dist, dist, ty, surfaced, champ, performer)) == null) continue;
            return toreturn;
        }
        for (tx = x - dist; tx < x; ++tx) {
            if (tx >= Zones.worldTileSizeX || tx <= 0 || (toReturn = Locates.getCreatureOnTile(tx, y + dist, dist, surfaced, champ, performer)) == null) continue;
            return toReturn;
        }
        for (ty = y; ty < y + dist; ++ty) {
            if (ty >= Zones.worldTileSizeY || ty <= 0 || (toreturn = Locates.getCreatureOnTile(x - dist, ty, dist, surfaced, champ, performer)) == null) continue;
            return toreturn;
        }
        for (tx = x - dist; tx < x; ++tx) {
            if (tx >= Zones.worldTileSizeX || tx <= 0 || (toReturn = Locates.getCreatureOnTile(tx, y - dist, dist, surfaced, champ, performer)) == null) continue;
            return toReturn;
        }
        return null;
    }

    static final Creature getCreatureOnTile(int x, int y, int dist, boolean surfaced, boolean champ, Creature performer) {
        VolaTile t = Zones.getTileOrNull(x, y, surfaced);
        if (t != null) {
            Creature[] crets;
            for (Creature c : crets = t.getCreatures()) {
                int distReduction;
                int maxDistance;
                float nolocateEnchantPower;
                boolean found;
                if (champ && (c.getStatus().isChampion() || c.isUnique())) {
                    return c;
                }
                if (champ || !c.isPlayer()) continue;
                boolean bl = found = c.getAttitude(performer) == 2;
                if (!found) {
                    boolean bl2 = found = performer.getCitizenVillage() != null && performer.getCitizenVillage().isEnemy(c);
                }
                if (!found || c.isStealth() && dist > 25 || (nolocateEnchantPower = c.getNoLocateItemBonus(false)) > 0.0f && dist > (maxDistance = 100) - (distReduction = (int)(nolocateEnchantPower / 2.0f)) || !(c.getBonusForSpellEffect((byte)29) <= 0.0f)) continue;
                return c;
            }
        }
        return null;
    }

    private static float getMaterialPendulumModifier(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 56: {
                    return 1.15f;
                }
                case 30: {
                    return 1.025f;
                }
                case 31: {
                    return 1.05f;
                }
                case 10: {
                    return 0.95f;
                }
                case 57: {
                    return 1.2f;
                }
                case 7: {
                    return 1.1f;
                }
                case 12: {
                    return 0.9f;
                }
                case 67: {
                    return 1.25f;
                }
                case 8: {
                    return 1.05f;
                }
                case 9: {
                    return 1.025f;
                }
                case 34: {
                    return 0.95f;
                }
                case 13: {
                    return 0.95f;
                }
                case 96: {
                    return 1.075f;
                }
            }
        }
        return 1.0f;
    }
}

