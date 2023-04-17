/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.MethodsReligion;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.EntityMoveQuestion;
import com.wurmonline.server.questions.SelectSpellQuestion;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import java.util.ArrayList;
import java.util.List;

class DomainItemBehaviour
extends ItemBehaviour {
    DomainItemBehaviour() {
        super((short)33);
    }

    DomainItemBehaviour(short type) {
        super(type);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        ArrayList<ActionEntry> toReturn = new ArrayList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, target));
        boolean reachable = true;
        if (target.getOwnerId() == -10L) {
            BlockingResult result;
            reachable = false;
            if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0f) && (result = Blocking.getBlockerBetween(performer, target, 4)) == null) {
                reachable = true;
            }
        }
        if (reachable) {
            if (Servers.localServer.EPIC && performer.getDeity() != null) {
                toReturn.add(Actions.actionEntrys[610]);
            }
            toReturn.add(Actions.actionEntrys[141]);
            if (performer.getFaith() >= 10.0f && target.getBless() != null) {
                toReturn.add(Actions.actionEntrys[142]);
                toReturn.add(Actions.actionEntrys[143]);
                if (performer.isPriest()) {
                    toReturn.add(Actions.actionEntrys[452]);
                }
            }
        }
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        ArrayList<ActionEntry> toReturn = new ArrayList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, source, target));
        boolean reachable = true;
        if (target.getOwnerId() == -10L) {
            BlockingResult result;
            reachable = false;
            if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0f) && (result = Blocking.getBlockerBetween(performer, target, 4)) == null) {
                reachable = true;
            }
        }
        if (reachable) {
            if (Servers.localServer.EPIC && performer.getDeity() != null) {
                toReturn.add(Actions.actionEntrys[610]);
            }
            toReturn.add(Actions.actionEntrys[141]);
            if (performer.getFaith() >= 10.0f) {
                if (target.getBless() != null) {
                    toReturn.add(Actions.actionEntrys[142]);
                    toReturn.add(Actions.actionEntrys[143]);
                }
                if (performer.isPriest()) {
                    toReturn.add(Actions.actionEntrys[452]);
                    if (source.isHolyItem()) {
                        toReturn.add(Actions.actionEntrys[216]);
                    }
                }
            }
            if (source.isRechargeable()) {
                toReturn.add(Actions.actionEntrys[370]);
            }
        }
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short action, float counter) {
        boolean done = true;
        if (action == 141) {
            done = MethodsReligion.pray(act, performer, target, counter);
        } else if (action == 142) {
            done = performer.getFaith() >= 10.0f ? MethodsReligion.sacrifice(act, performer, target) : true;
        } else if (action == 143) {
            done = performer.getFaith() >= 10.0f ? MethodsReligion.desecrate(act, performer, null, target) : true;
        } else if (action == 452) {
            if (performer.isPriest()) {
                done = true;
                SelectSpellQuestion spq = new SelectSpellQuestion(performer, "Spell set", "These are your available spells", target.getWurmId());
                spq.sendQuestion();
            }
        } else if (action == 610) {
            done = true;
            if (Servers.localServer.EPIC && performer.getDeity() != null) {
                EntityMoveQuestion emq = new EntityMoveQuestion(performer);
                emq.sendQuestion();
            }
        } else {
            done = super.action(act, performer, target, action, counter);
        }
        return done;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
        boolean done = false;
        boolean reachable = true;
        if (action == 141) {
            done = this.action(act, performer, target, action, counter);
        } else if (action == 142) {
            done = performer.getFaith() >= 10.0f ? MethodsReligion.sacrifice(act, performer, target) : true;
        } else if (action == 143) {
            done = performer.getFaith() >= 10.0f ? MethodsReligion.desecrate(act, performer, source, target) : true;
        } else if (action == 216) {
            done = true;
            if (performer.isPriest() && source.isHolyItem()) {
                if (target.getParentId() != -10L) {
                    performer.getCommunicator().sendNormalServerMessage("The altar needs to be on the ground to be used.");
                } else {
                    done = MethodsReligion.holdSermon(performer, target, source, act, counter);
                }
            }
        } else if (action == 370 && source.isRechargeable()) {
            if (target.getParentId() != -10L) {
                performer.getCommunicator().sendNormalServerMessage("The altar needs to be on the ground to be used.");
            } else {
                done = MethodsReligion.sendRechargeQuestion(performer, source);
            }
        } else if (action == 452) {
            done = this.action(act, performer, target, action, counter);
        } else if (action == 610) {
            done = true;
            if (Servers.localServer.EPIC && performer.getDeity() != null) {
                EntityMoveQuestion emq = new EntityMoveQuestion(performer);
                emq.sendQuestion();
            }
        } else {
            done = super.action(act, performer, source, target, action, counter);
        }
        return done;
    }
}

