/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.ManageMenu;
import com.wurmonline.server.creatures.Creature;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

final class MenuRequestBehaviour
extends Behaviour {
    private static final Logger logger = Logger.getLogger(MenuRequestBehaviour.class.getName());

    MenuRequestBehaviour() {
        super((short)53);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, int menuId) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        if (menuId == 0) {
            toReturn.addAll(ManageMenu.getBehavioursFor(performer));
        }
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, int menuId, short action, float counter) {
        boolean done = true;
        if (menuId == 0 && ManageMenu.isManageAction(performer, action)) {
            return ManageMenu.action(act, performer, action, counter);
        }
        return true;
    }
}

