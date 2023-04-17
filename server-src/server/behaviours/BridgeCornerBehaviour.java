/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.Features;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.MarkerBehaviour;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.BridgePart;
import java.util.LinkedList;
import java.util.List;

final class BridgeCornerBehaviour
extends Behaviour
implements MiscConstants {
    BridgeCornerBehaviour() {
        super((short)60);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, boolean onSurface, BridgePart bridgePart) {
        HighwayPos highwayPos;
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        if (source.isSign() || source.isStreetLamp()) {
            toReturn.add(Actions.actionEntrys[176]);
        } else if (Features.Feature.HIGHWAYS.isEnabled() && source.isRoadMarker() && (highwayPos = MethodsHighways.getHighwayPos(bridgePart)) != null && MethodsHighways.middleOfHighway(highwayPos) && !MethodsHighways.containsMarker(highwayPos, (byte)0)) {
            byte pLinks = MethodsHighways.getPossibleLinksFrom(highwayPos, source);
            if (MethodsHighways.canPlantMarker(null, highwayPos, source, pLinks)) {
                toReturn.add(new ActionEntry(176, "Plant", "planting"));
            }
            toReturn.add(new ActionEntry(759, "View possible protected tiles", "viewing"));
            if (pLinks != 0) {
                toReturn.add(new ActionEntry(748, "View possible links", "viewing"));
            }
        }
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, boolean onSurface, BridgePart bridgePart) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public boolean action(Action act, Creature performer, Item source, boolean onSurface, BridgePart bridgePart, int encodedTile, short action, float counter) {
        boolean done = true;
        if (action == 1) {
            return this.action(act, performer, onSurface, bridgePart, encodedTile, action, counter);
        }
        if (action == 176 && (source.isSign() || source.isStreetLamp())) {
            if (performer.getPower() <= 0) return MethodsItems.plantSign(performer, source, counter, true, bridgePart.getTileX(), bridgePart.getTileY(), performer.isOnSurface(), performer.getBridgeId(), false, -1L);
            return MethodsItems.plantSignFinish(performer, source, true, bridgePart.getTileX(), bridgePart.getTileY(), performer.isOnSurface(), performer.getBridgeId(), false, -1L);
        }
        if (action == 176 && source.isRoadMarker() && Features.Feature.HIGHWAYS.isEnabled()) {
            HighwayPos highwayPos = MethodsHighways.getHighwayPos(bridgePart);
            if (highwayPos == null) {
                performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
                return true;
            }
            if (MethodsHighways.middleOfHighway(highwayPos) && !MethodsHighways.containsMarker(highwayPos, (byte)0)) {
                byte pLinks = MethodsHighways.getPossibleLinksFrom(highwayPos, source);
                done = !MethodsHighways.canPlantMarker(performer, highwayPos, source, pLinks) ? true : (performer.getPower() > 0 ? MethodsItems.plantSignFinish(performer, source, true, highwayPos.getTilex(), highwayPos.getTiley(), highwayPos.isOnSurface(), highwayPos.getBridgeId(), false, -1L) : MethodsItems.plantSign(performer, source, counter, true, highwayPos.getTilex(), highwayPos.getTiley(), highwayPos.isOnSurface(), highwayPos.getBridgeId(), false, -1L));
                if (!done) return done;
                if (!source.isPlanted()) return done;
                MethodsHighways.autoLink(source, pLinks);
                return done;
            }
            performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
            return true;
        }
        if (action == 748 && source.isRoadMarker() && Features.Feature.HIGHWAYS.isEnabled()) {
            HighwayPos highwayPos = MethodsHighways.getHighwayPos(bridgePart);
            if (highwayPos == null) {
                performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
                return true;
            }
            if (MethodsHighways.middleOfHighway(highwayPos) && !MethodsHighways.containsMarker(highwayPos, (byte)0)) {
                return MarkerBehaviour.showLinks(performer, source, act, counter, highwayPos);
            }
            performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
            return true;
        }
        if (action != 759) return this.action(act, performer, onSurface, bridgePart, encodedTile, action, counter);
        if (!source.isRoadMarker()) return this.action(act, performer, onSurface, bridgePart, encodedTile, action, counter);
        if (!Features.Feature.HIGHWAYS.isEnabled()) return this.action(act, performer, onSurface, bridgePart, encodedTile, action, counter);
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(bridgePart);
        if (highwayPos == null) {
            performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
            return true;
        }
        if (MethodsHighways.middleOfHighway(highwayPos) && !MethodsHighways.containsMarker(highwayPos, (byte)0)) {
            return MarkerBehaviour.showProtection(performer, source, act, counter, highwayPos);
        }
        performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
        return true;
    }

    @Override
    public boolean action(Action act, Creature performer, boolean onSurface, BridgePart bridgePart, int encodedTile, short action, float counter) {
        if (action == 1) {
            HighwayPos highwayPos = MethodsHighways.getHighwayPos(bridgePart);
            if (highwayPos != null && MethodsHighways.middleOfHighway(highwayPos)) {
                performer.getCommunicator().sendNormalServerMessage("This outlines where signs and road markers could be planted.");
            } else {
                performer.getCommunicator().sendNormalServerMessage("This outlines where signs can be planted.");
            }
        }
        return true;
    }
}

