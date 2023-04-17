/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;

public class CreatureLineSegment
extends MulticolorLineSegment {
    private static final String YOU_STRING = "you";
    private Creature creature;

    public CreatureLineSegment(Creature c) {
        super(c == null ? "something" : c.getName(), (byte)0);
        this.creature = c;
    }

    public String getText(Creature sendingTo) {
        if (sendingTo != this.creature) {
            return this.getText();
        }
        return YOU_STRING;
    }

    public byte getColor(Creature sendingTo) {
        if (this.creature == null || sendingTo == null) {
            return this.getColor();
        }
        switch (this.creature.getAttitude(sendingTo)) {
            case 2: 
            case 4: {
                return 4;
            }
            case 1: 
            case 5: {
                return 9;
            }
            case 7: {
                return 14;
            }
            case 0: {
                return 12;
            }
            case 3: 
            case 6: {
                return 8;
            }
        }
        return this.getColor();
    }

    public static ArrayList<MulticolorLineSegment> cloneLineList(ArrayList<MulticolorLineSegment> list) {
        ArrayList<MulticolorLineSegment> toReturn = new ArrayList<MulticolorLineSegment>(list.size());
        for (MulticolorLineSegment s : list) {
            if (s instanceof CreatureLineSegment) {
                toReturn.add(new CreatureLineSegment(((CreatureLineSegment)s).creature));
                continue;
            }
            toReturn.add(new MulticolorLineSegment(s.getText(), s.getColor()));
        }
        return toReturn;
    }
}

