/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Behaviours;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.shared.constants.SoundNames;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

public class Behaviour
implements SoundNames {
    private static List<ActionEntry> emptyActionList;
    private short behaviourType = 0;
    public static final int[] emptyIntArr;

    public Behaviour() {
        Behaviours.getInstance().addBehaviour(this);
    }

    public Behaviour(short type) {
        this.behaviourType = type;
        Behaviours.getInstance().addBehaviour(this);
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, long target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item object, int tilex, int tiley, boolean onSurface, int tile) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(Actions.getDefaultTileActions());
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, int tilex, int tiley, boolean onSurface, int tile) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(Actions.getDefaultTileActions());
        return toReturn;
    }

    public static final List<ActionEntry> getEmptyActionList() {
        return emptyActionList;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item object, int tilex, int tiley, boolean onSurface, int tile, int dir) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(Actions.getDefaultTileActions());
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, int tilex, int tiley, boolean onSurface, int tile, int dir) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(Actions.getDefaultTileActions());
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item object, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, boolean border, int heightOffset) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, boolean border, int heightOffset) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item object, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    public boolean action(@Nonnull Action act, @Nonnull Creature performer, @Nonnull Item source, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset, short action, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action act, @Nonnull Creature performer, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset, short action, float counter) {
        return true;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item subject, @Nonnull Skill skill) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Skill skill) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item subject, @Nonnull Item target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Wound target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item subject, @Nonnull Wound target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Creature target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item subject, @Nonnull Creature target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item subject, @Nonnull Wall target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Wall target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item subject, @Nonnull Fence target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Fence target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item object, int planetId) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, int planetId) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, int tilex, int tiley, boolean onSurface, int tile, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, int tilex, int tiley, boolean onSurface, int tile, int dir, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, int dir, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, int planetId, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, int planetId, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, @Nonnull Item target, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Wound target, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, @Nonnull Wound target, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item target, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, @Nonnull Creature target, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Creature target, short num, float counter) {
        return true;
    }

    final short getType() {
        return this.behaviourType;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, @Nonnull Wall target, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Wall target, short num, float counter) {
        return true;
    }

    static void addEmotes(List<ActionEntry> list) {
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, boolean onSurface, @Nonnull Fence target, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, boolean onSurface, @Nonnull Fence target, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action act, @Nonnull Creature performer, @Nonnull Item source, @Nonnull Skill skill, short action, float counter) {
        return this.action(act, performer, skill, action, counter);
    }

    public boolean action(@Nonnull Action act, @Nonnull Creature performer, @Nonnull Skill skill, short action, float counter) {
        return true;
    }

    @Nullable
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, boolean onSurface, @Nonnull Floor floor) {
        return null;
    }

    @Nullable
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature creature, @Nonnull Item item, boolean onSurface, Floor floor) {
        return null;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item source, boolean onSurface, Floor target, int encodedTile, short num, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action act, @Nonnull Creature performer, boolean onSurface, @Nonnull Floor floor, int encodedTile, short action, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action aAct, @Nonnull Creature aPerformer, @Nonnull Item aSource, int aTilex, int aTiley, boolean onSurface, int aHeightOffset, Tiles.TileBorderDirection aDir, long borderId, short aAction, float aCounter) {
        return true;
    }

    public boolean action(@Nonnull Action aAct, @Nonnull Creature aPerformer, int aTilex, int aTiley, boolean onSurface, Tiles.TileBorderDirection aDir, long borderId, short aAction, float aCounter) {
        return true;
    }

    public boolean action(@Nonnull Action action, @Nonnull Creature performer, @Nonnull Item[] targets, short num, float counter) {
        return true;
    }

    public int hashCode() {
        int prime = 31;
        return 31 * this.getType();
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Behaviour)) {
            return false;
        }
        Behaviour other = (Behaviour)obj;
        return this.getType() == other.getType();
    }

    @Nonnull
    public String toString() {
        return "Behaviour [behaviourType=" + this.getType() + "]";
    }

    @Nullable
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature aPerformer, boolean aOnSurface, @Nonnull BridgePart aBridgePart) {
        return null;
    }

    @Nullable
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature aPerformer, @Nonnull Item item, boolean aOnSurface, @Nonnull BridgePart aBridgePart) {
        return null;
    }

    public boolean action(@Nonnull Action act, @Nonnull Creature performer, boolean onSurface, @Nonnull BridgePart aBridgePart, int encodedTile, short action, float counter) {
        return true;
    }

    public boolean action(@Nonnull Action act, @Nonnull Creature performer, @Nonnull Item item, boolean onSurface, @Nonnull BridgePart aBridgePart, int encodedTile, short action, float counter) {
        return true;
    }

    static {
        emptyIntArr = new int[0];
    }
}

