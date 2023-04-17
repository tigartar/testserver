/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.TicketUpdateQuestion;
import com.wurmonline.server.support.Ticket;
import com.wurmonline.server.support.Tickets;
import java.util.LinkedList;
import java.util.List;

public class TicketBehaviour
extends Behaviour
implements MiscConstants {
    TicketBehaviour() {
        super((short)50);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int ticketId) {
        return this.getBehavioursFor(performer, ticketId);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, int ticketId) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        Ticket ticket = Tickets.getTicket(ticketId);
        if (ticket == null) {
            return toReturn;
        }
        Player player = (Player)performer;
        if (player.mayHearDevTalk()) {
            if (ticket.isOpen()) {
                toReturn.add(new ActionEntry(-4, "Forward", "forward", emptyIntArr));
                toReturn.add(Actions.actionEntrys[596]);
                toReturn.add(Actions.actionEntrys[591]);
                toReturn.add(Actions.actionEntrys[592]);
                toReturn.add(Actions.actionEntrys[593]);
                if (ticket.getResponderName().equalsIgnoreCase(performer.getName())) {
                    toReturn.add(Actions.actionEntrys[594]);
                }
                if (ticket.getCategoryCode() != 11) {
                    toReturn.add(Actions.actionEntrys[589]);
                }
                toReturn.add(Actions.actionEntrys[590]);
                if (!ticket.getResponderName().equalsIgnoreCase(performer.getName())) {
                    toReturn.add(Actions.actionEntrys[595]);
                }
            } else if (ticket.hasFeedback()) {
                toReturn.add(Actions.actionEntrys[597]);
            } else if (ticket.getStateCode() == 2) {
                toReturn.add(Actions.actionEntrys[599]);
            }
            toReturn.add(Actions.actionEntrys[587]);
        } else if (ticket.getPlayerId() == player.getWurmId()) {
            if (ticket.isOpen()) {
                if (player.mayHearMgmtTalk() && ticket.getLevelCode() == 1) {
                    toReturn.add(new ActionEntry(-1, "Forward", "forward", emptyIntArr));
                    toReturn.add(Actions.actionEntrys[591]);
                }
                toReturn.add(Actions.actionEntrys[587]);
                toReturn.add(Actions.actionEntrys[588]);
            } else {
                toReturn.add(new ActionEntry(587, "View", "viewing", emptyIntArr));
                toReturn.add(Actions.actionEntrys[597]);
            }
        } else if (player.mayHearMgmtTalk()) {
            if (ticket.isOpen() && player.mayMute()) {
                toReturn.add(new ActionEntry(-2, "Forward", "forward", emptyIntArr));
                toReturn.add(Actions.actionEntrys[596]);
                toReturn.add(Actions.actionEntrys[591]);
                if (ticket.getResponderName().equalsIgnoreCase(performer.getName())) {
                    toReturn.add(Actions.actionEntrys[594]);
                }
                toReturn.add(Actions.actionEntrys[589]);
                toReturn.add(Actions.actionEntrys[590]);
            }
            toReturn.add(Actions.actionEntrys[587]);
        }
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, int ticketId, short action, float counter) {
        Ticket ticket = Tickets.getTicket(ticketId);
        Player player = (Player)performer;
        if (player.mayHearDevTalk()) {
            if (ticket.isOpen()) {
                if (action == 596) {
                    this.updateTicket(performer, ticketId, action);
                } else if (action == 591) {
                    this.updateTicket(performer, ticketId, action);
                } else if (action == 592) {
                    this.updateTicket(performer, ticketId, action);
                } else if (action == 593) {
                    this.updateTicket(performer, ticketId, action);
                } else if (ticket.getResponderName().equalsIgnoreCase(performer.getName()) && action == 594) {
                    this.updateTicket(performer, ticketId, action);
                } else if (action == 589) {
                    player.respondGMTab(ticket.getPlayerName(), String.valueOf(ticket.getTicketId()));
                    if (performer.getPower() >= 2) {
                        ticket.addNewTicketAction((byte)3, performer.getName(), "GM " + performer.getName() + " responded.", (byte)0);
                    } else {
                        ticket.addNewTicketAction((byte)2, performer.getName(), "CM " + performer.getName() + " responded.", (byte)0);
                    }
                } else if (action == 590) {
                    this.updateTicket(performer, ticketId, action);
                } else if (action == 595) {
                    ticket.addNewTicketAction((byte)11, performer.getName(), performer.getName() + " took ticket.", (byte)1);
                }
            } else if (action == 597) {
                this.updateTicket(performer, ticketId, action);
            } else if (action == 599 && ticket.getStateCode() == 2) {
                this.updateTicket(performer, ticketId, action);
            }
            if (action == 587) {
                this.updateTicket(performer, ticketId, action);
            }
        } else if (ticket.getPlayerId() == player.getWurmId()) {
            if (player.mayHearMgmtTalk() && ticket.getLevelCode() == 1) {
                this.updateTicket(performer, ticketId, action);
            } else if (ticket.isOpen() && action == 588) {
                this.updateTicket(performer, ticketId, action);
            } else if (action == 587) {
                this.updateTicket(performer, ticketId, action);
            } else if (action == 597) {
                this.updateTicket(performer, ticketId, action);
            }
        } else if (player.mayHearMgmtTalk()) {
            if (ticket.isOpen()) {
                if (action == 596) {
                    this.updateTicket(performer, ticketId, action);
                } else if (action == 591) {
                    this.updateTicket(performer, ticketId, action);
                } else if (ticket.getResponderName().equalsIgnoreCase(performer.getName()) && action == 594) {
                    this.updateTicket(performer, ticketId, action);
                } else if (action == 589) {
                    player.respondGMTab(ticket.getPlayerName(), String.valueOf(ticket.getTicketId()));
                    ticket.addNewTicketAction((byte)2, performer.getName(), "CM " + performer.getName() + " responded.", (byte)0);
                } else if (action == 590) {
                    this.updateTicket(performer, ticketId, action);
                }
            }
            if (action == 587) {
                this.updateTicket(performer, ticketId, action);
            }
        }
        return true;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, int ticketId, short action, float counter) {
        return this.action(act, performer, ticketId, action, counter);
    }

    private void updateTicket(Creature performer, int ticketId, short action) {
        TicketUpdateQuestion tuq = new TicketUpdateQuestion(performer, ticketId, action);
        tuq.sendQuestion();
    }
}

