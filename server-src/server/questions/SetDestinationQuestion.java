/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.Question;
import java.util.Properties;

public class SetDestinationQuestion
extends Question
implements TimeConstants {
    private static final int WIDTH = 350;
    private static final int HEIGHT = 250;
    private static final boolean RESIZEABLE = true;
    private static final boolean CLOSEABLE = true;
    private static final int[] RGB = new int[]{200, 200, 200};
    private static final String key = "dest";
    private static final String title = "Plot a course";
    private static final String question = "Plot a course for your boat:";
    private static final int CLEAR = 65536;
    private static final String cyan = "66,200,200";
    private static final String orange = "255,156,66";
    private static final String red = "255,66,66";
    private static final String white = "255,255,255";
    private Vehicle vehicle;

    public SetDestinationQuestion(Creature aResponder, Item aTarget) {
        super(aResponder, title, question, 130, aTarget.getWurmId());
        if (aTarget.isBoat()) {
            this.vehicle = Vehicles.getVehicle(aTarget);
        }
    }

    @Override
    public void answer(Properties answers) {
        if (!this.getResponder().isVehicleCommander() || this.getResponder().getVehicle() == -10L) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You must be embarked as the commander of a boat to plot a course. Try dragging the boat inland before embarking again.");
            return;
        }
        String val = answers.getProperty(key);
        if (val != null) {
            int serverId = Integer.parseInt(val);
            if (serverId == 65536) {
                if (this.vehicle.hasDestinationSet()) {
                    this.vehicle.clearDestination();
                    this.getResponder().getCommunicator().sendNormalServerMessage("This boat no longer has a course plotted.");
                    this.alertPassengers();
                    return;
                }
                this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to plot a course.");
                return;
            }
            if (this.vehicle.hasDestinationSet() && serverId == this.vehicle.getDestinationServer().getId()) {
                this.getResponder().getCommunicator().sendNormalServerMessage("You decide to keep your course set to " + this.vehicle.getDestinationServer().getName() + ".");
                return;
            }
            ServerEntry entry = Servers.getServerWithId(serverId);
            if (entry != null) {
                if (Servers.isAvailableDestination(this.getResponder(), entry)) {
                    this.vehicle.setDestination(entry);
                    this.getResponder().getCommunicator().sendNormalServerMessage("You plot a course to " + entry.getName() + ".");
                    this.vehicle.checkPassengerPermissions(this.getResponder());
                    this.alertPassengers();
                    if (!entry.EPIC || Server.getInstance().isPS()) {
                        this.vehicle.alertPassengersOfKingdom(entry, true);
                        if (entry.PVPSERVER && !Servers.localServer.PVPSERVER || entry.isChaosServer()) {
                            this.vehicle.alertAllPassengersOfEnemies(entry);
                        }
                    }
                } else {
                    this.getResponder().getCommunicator().sendNormalServerMessage("The waters between here and " + entry.getName() + " are too rough to navigate.");
                }
            } else {
                this.getResponder().getCommunicator().sendNormalServerMessage("You decide to not plot a course.");
            }
        } else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You decide to not plot a course.");
        }
    }

    @Override
    public void sendQuestion() {
        String restriction;
        if (this.vehicle == null) {
            return;
        }
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        ServerEntry[] servers = Servers.getDestinations(this.getResponder());
        long cooldown = this.vehicle.getPlotCourseCooldowns();
        boolean isPvPBlocking = this.vehicle.isPvPBlocking();
        if (Servers.localServer.PVPSERVER && (restriction = this.vehicle.checkCourseRestrictions()) != "") {
            buf.append("label{type='bold'; color='255,156,66'; text='Course Restrictions'};");
            buf.append("text{text='" + restriction + "'};");
            buf.append("text{text=''};");
            buf.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(350, 250 + servers.length * 20, true, true, buf.toString(), RGB[0], RGB[1], RGB[2], title);
            return;
        }
        if (this.vehicle.hasDestinationSet()) {
            String color = this.vehicle.getDestinationServer().PVPSERVER || this.vehicle.getDestinationServer().isChaosServer() ? red : cyan;
            String name = this.vehicle.getDestinationServer().PVPSERVER || this.vehicle.getDestinationServer().isChaosServer() ? this.vehicle.getDestinationServer().getName() + " [PvP]" : this.vehicle.getDestinationServer().getName();
            buf.append("harray{label{type='bold'; color='255,255,255'; text='Current destination: '};");
            buf.append("label{color='" + color + "'; text='" + name + "'}}");
        }
        buf.append("text{text=''};");
        if (servers.length == 0 || servers.length == 1 && servers[0] == Servers.localServer) {
            buf.append("text{text='There are no available destinations.'};");
        } else {
            for (ServerEntry lServer : servers) {
                if (lServer == Servers.localServer || lServer.LOGINSERVER && !Server.getInstance().isPS()) continue;
                boolean selected = false;
                if (this.vehicle.hasDestinationSet() && this.vehicle.getDestinationServer() == lServer) {
                    selected = true;
                }
                if (lServer.PVPSERVER || lServer.isChaosServer()) {
                    if (isPvPBlocking) {
                        buf.append("label{color='255,66,66' text='" + lServer.getName() + " [PvP] (PvP travel blocked)'};");
                        continue;
                    }
                    if (cooldown > 0L) {
                        buf.append("label{color='255,66,66' text='" + lServer.getName() + " [PvP] (Available in " + Server.getTimeFor(cooldown) + ")'};");
                        continue;
                    }
                    buf.append(this.createRadioWithLabel(key, String.valueOf(lServer.getId()), lServer.getName() + " [PvP]", red, selected));
                    continue;
                }
                buf.append(this.createRadioWithLabel(key, String.valueOf(lServer.getId()), lServer.getName(), cyan, selected));
            }
            if (this.vehicle.hasDestinationSet()) {
                buf.append(this.createRadioWithLabel(key, String.valueOf(65536), "Clear destination", white, false));
            } else {
                buf.append(this.createRadioWithLabel(key, String.valueOf(65536), "No destination", white, true));
            }
            buf.append("text{text=''};");
            buf.append("text{text='Plotting a course will send you to that server when you sail across any border of " + Servers.localServer.getName() + ".'};");
            buf.append("text{text=''};");
            buf.append("text{text='You will appear on the opposite side of the selected server. For example, if you cross the northern border, you will appear on the southern side of the server you have selected.'};");
            if (isPvPBlocking) {
                buf.append("text{text=''};");
                buf.append("text{text='You or a passenger has PvP travel blocked. This option can be toggled in the Profile.'};");
            }
        }
        buf.append("text{text=''};");
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(350, 250 + servers.length * 20, true, true, buf.toString(), RGB[0], RGB[1], RGB[2], title);
    }

    private String createRadioWithLabel(String group2, String id, String message, String color, boolean selected) {
        String toReturn = "harray{radio{group='" + group2 + "'; id='" + id + "'; selected='" + selected + "'}";
        toReturn = toReturn + "label{color='" + color + "'; text='" + message + "'}}";
        return toReturn;
    }

    private void alertPassengers() {
        if (this.vehicle.seats != null) {
            for (Seat lSeat : this.vehicle.seats) {
                if (!lSeat.isOccupied() || lSeat == this.vehicle.getPilotSeat()) continue;
                try {
                    Player passenger = Players.getInstance().getPlayer(lSeat.getOccupant());
                    if (!this.vehicle.hasDestinationSet()) {
                        passenger.getCommunicator().sendNormalServerMessage(this.getResponder().getName() + " has cleared the plotted course.");
                        continue;
                    }
                    ServerEntry entry = this.vehicle.getDestinationServer();
                    String msg = this.getResponder().getName() + " has plotted a course to " + entry.getName();
                    if (!Servers.mayEnterServer(passenger, entry)) {
                        msg = msg + ", but you will not be able to travel with " + this.getResponder().getHimHerItString();
                    }
                    passenger.getCommunicator().sendAlertServerMessage(msg + ".");
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
        }
    }
}

