/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.MethodsFishing;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public final class ActionStack
implements TimeConstants {
    private final LinkedList<Action> quickActions = new LinkedList();
    private final LinkedList<Action> slowActions = new LinkedList();
    private boolean clearing = false;
    private long lastPolledStunned = 0L;

    public void addAction(Action action) {
        int maxPrio = 1;
        if (action.isQuick()) {
            if (this.quickActions.size() < 10) {
                this.quickActions.addLast(action);
            } else {
                action.getPerformer().getCommunicator().sendSafeServerMessage("You can't remember that many things to do in advance.");
            }
        } else if (!this.slowActions.isEmpty()) {
            if (!Action.isStackable(action.getNumber())) {
                action.getPerformer().getCommunicator().sendNormalServerMessage("You're too busy.");
                return;
            }
            if (this.slowActions.size() > 1 && !Action.isStackableFight(action.getNumber())) {
                ListIterator it = this.slowActions.listIterator();
                while (it.hasNext()) {
                    Action curr = (Action)it.next();
                    if (curr.getNumber() != action.getNumber()) continue;
                    action.getPerformer().getCommunicator().sendNormalServerMessage("You're too busy.");
                    return;
                }
            }
            boolean insertedAndShouldPoll = false;
            ListIterator<Action> it = this.slowActions.listIterator();
            while (it.hasNext()) {
                Action curr = (Action)it.next();
                if (maxPrio >= curr.getPriority()) continue;
                maxPrio = curr.getPriority();
                if (action.getPriority() <= maxPrio) continue;
                it.previous();
                if (action.getNumber() == 114) {
                    insertedAndShouldPoll = true;
                    it.add(action);
                    break;
                }
                it.add(action);
                return;
            }
            if (insertedAndShouldPoll) {
                action.poll();
                return;
            }
            if (action.getPerformer().isPlayer() && this.slowActions.size() > action.getPerformer().getMaxNumActions()) {
                if (!Action.isActionAttack(action.getNumber())) {
                    action.getPerformer().getCommunicator().sendNormalServerMessage("You're too busy.");
                }
            } else {
                if (Actions.actionEntrys[this.slowActions.getLast().getNumber()] == Actions.actionEntrys[action.getNumber()] && !Action.isActionAttack(action.getNumber())) {
                    action.getPerformer().getCommunicator().sendNormalServerMessage("After you " + Actions.actionEntrys[this.slowActions.getLast().getNumber()].getVerbFinishString() + " you will " + Actions.actionEntrys[action.getNumber()].getVerbStartString() + " again.");
                } else if (!this.slowActions.getLast().isOffensive() && !Action.isActionAttack(action.getNumber())) {
                    action.getPerformer().getCommunicator().sendNormalServerMessage("After you " + Actions.actionEntrys[this.slowActions.getLast().getNumber()].getVerbFinishString() + " you will " + Actions.actionEntrys[action.getNumber()].getVerbStartString() + ".");
                } else if (this.slowActions.getLast().isOffensive() && action.isSpell()) {
                    action.getPerformer().getCommunicator().sendCombatNormalMessage("After you " + Actions.actionEntrys[this.slowActions.getLast().getNumber()].getVerbFinishString() + " you will " + Actions.actionEntrys[action.getNumber()].getVerbStartString() + ".");
                }
                this.slowActions.addLast(action);
            }
        } else if (action.getNumber() == 114) {
            if (!action.poll()) {
                this.slowActions.add(action);
            }
        } else {
            this.slowActions.add(action);
        }
    }

    private void removeAction(Action action) {
        this.quickActions.remove(action);
        this.slowActions.remove(action);
    }

    public String stopCurrentAction(boolean farAway) throws NoSuchActionException {
        String toReturn = "";
        Action current = this.getCurrentAction();
        if (current.getNumber() == 136) {
            current.getPerformer().setStealth(current.getPerformer().isStealth());
        }
        toReturn = current.stop(farAway);
        if (current.getNumber() == 160) {
            MethodsFishing.playerOutOfRange(current.getPerformer(), current);
        }
        if (current.getNumber() == 925 || current.getNumber() == 926) {
            current.getPerformer().getCommunicator().sendCancelPlacingItem();
            toReturn = "";
        }
        this.removeAction(current);
        return toReturn;
    }

    public Action getCurrentAction() throws NoSuchActionException {
        if (!this.quickActions.isEmpty()) {
            return this.quickActions.getFirst();
        }
        if (!this.slowActions.isEmpty()) {
            return this.slowActions.getFirst();
        }
        throw new NoSuchActionException("No Current Action");
    }

    public boolean poll(Creature owner) {
        boolean toReturn = true;
        if (owner.getStatus().getStunned() > 0.0f && !owner.isDead()) {
            if (this.lastPolledStunned == 0L) {
                this.lastPolledStunned = System.currentTimeMillis();
            }
            toReturn = false;
            float delta = (float)(System.currentTimeMillis() - this.lastPolledStunned) / 1000.0f;
            owner.getStatus().setStunned(owner.getStatus().getStunned() - delta, false);
            this.lastPolledStunned = owner.getStatus().getStunned() <= 0.0f ? 0L : System.currentTimeMillis();
        } else if (!this.quickActions.isEmpty()) {
            while (!this.quickActions.isEmpty()) {
                if (!this.quickActions.getFirst().poll()) continue;
                this.quickActions.removeFirst();
            }
        } else if (!this.slowActions.isEmpty()) {
            Action first = this.slowActions.getFirst();
            if (first.poll()) {
                if (!this.slowActions.isEmpty()) {
                    this.slowActions.removeFirst();
                }
                if (!this.slowActions.isEmpty()) {
                    first = this.slowActions.getFirst();
                    if (first.getCounterAsFloat() >= 1.0f && first.getNumber() != 114 && first.getNumber() != 160) {
                        owner.sendActionControl(first.getActionString(), true, first.getTimeLeft());
                    } else if (first.getNumber() != 160) {
                        owner.sendActionControl("", false, 0);
                    }
                } else {
                    owner.sendActionControl("", false, 0);
                }
            } else {
                toReturn = false;
            }
        }
        return toReturn;
    }

    public void removeAttacks(Creature owner) {
        if (!this.clearing) {
            ListIterator lit = this.slowActions.listIterator();
            while (lit.hasNext()) {
                Action act = (Action)lit.next();
                if (act.getNumber() != 114) continue;
                lit.remove();
            }
        }
    }

    public void removeTarget(long wurmid) {
        if (!this.clearing) {
            ListIterator lit = this.slowActions.listIterator();
            while (lit.hasNext()) {
                Action act = (Action)lit.next();
                if (act.getTarget() != wurmid) continue;
                try {
                    if (act == this.getCurrentAction()) {
                        act.getPerformer().getCommunicator().sendNormalServerMessage(act.stop(false));
                        act.getPerformer().sendActionControl("", false, 0);
                    }
                }
                catch (NoSuchActionException noSuchActionException) {
                    // empty catch block
                }
                lit.remove();
            }
        }
    }

    public void replaceTarget(long wurmid) {
        if (!this.clearing) {
            ListIterator lit = this.slowActions.listIterator();
            while (lit.hasNext()) {
                Action act = (Action)lit.next();
                if (!act.isOffensive()) continue;
                act.setTarget(wurmid);
            }
        }
    }

    public void clear() {
        this.clearing = true;
        this.quickActions.clear();
        for (Action actionToStop : this.slowActions) {
            actionToStop.stop(false);
        }
        this.slowActions.clear();
        this.clearing = false;
    }

    public Action getLastSlowAction() {
        if (!this.clearing && !this.slowActions.isEmpty()) {
            try {
                return this.slowActions.getLast();
            }
            catch (NoSuchElementException nse) {
                return null;
            }
        }
        return null;
    }
}

