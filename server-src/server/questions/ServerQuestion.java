/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.shared.constants.CounterTypes;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public final class ServerQuestion
extends Question
implements CounterTypes {
    private final List<ServerEntry> serverEntries = new LinkedList<ServerEntry>();
    private final List<ServerEntry> transferEntries = new LinkedList<ServerEntry>();

    public ServerQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 43, aTarget);
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseServerQuestion(this);
    }

    @Override
    public void sendQuestion() {
        int x;
        ServerEntry[] entries;
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.getResponder().getPower() > 0) {
            if (WurmId.getType(this.target) == 0) {
                try {
                    Player p = Players.getInstance().getPlayer(this.target);
                    buf.append("text{text='Careful! This will send " + p.getName() + " to another server:'}");
                }
                catch (NoSuchPlayerException p) {
                    // empty catch block
                }
            }
            entries = Servers.localServer.entryServer || Servers.isThisATestServer() ? Servers.getAllServers() : Servers.getAllNeighbours();
            buf.append("harray{label{type=\"bold\";text='Transfer to: '}dropdown{id='transferTo';default='0';options='");
            buf.append("None");
            for (x = 0; x < entries.length; ++x) {
                if (!entries[x].isAvailable(this.getResponder().getPower(), this.getResponder().isPaying())) continue;
                buf.append(",");
                this.transferEntries.add(entries[x]);
                buf.append(entries[x].name);
            }
            if (Servers.localServer.id != Servers.loginServer.id) {
                buf.append(",");
                this.transferEntries.add(Servers.loginServer);
                buf.append(Servers.loginServer.name);
            }
            buf.append("'}};");
        }
        if (this.getResponder().getPower() >= 5) {
            buf.append("text{text='Manage servers. Add neighbours and other servers.'}");
            Servers.loadAllServers(true);
            buf.append("text{text='Reloaded all servers from database.'}");
            buf.append("table{rows=\"1\";cols=\"6\";");
            buf.append("label{type=\"bolditalic\";text=\"Type\"}label{type=\"bolditalic\";text=\"Name\"}label{type=\"bolditalic\";text=\"PvP\"}label{type=\"bolditalic\";text=\"Epic\"}label{type=\"bolditalic\";text=\"Home\"}label{type=\"bolditalic\";text=\"Chaos\"}");
            buf.append(this.getServerEntryData("Current", Servers.localServer));
            if (Servers.localServer.serverNorth != null) {
                buf.append(this.getServerEntryData("NORTH", Servers.localServer.serverNorth));
            }
            if (Servers.localServer.serverEast != null) {
                buf.append(this.getServerEntryData("EAST", Servers.localServer.serverEast));
            }
            if (Servers.localServer.serverSouth != null) {
                buf.append(this.getServerEntryData("SOUTH", Servers.localServer.serverSouth));
            }
            if (Servers.localServer.serverWest != null) {
                buf.append(this.getServerEntryData("WEST", Servers.localServer.serverWest));
            }
            if (Servers.loginServer != null) {
                buf.append(this.getServerEntryData("LOGIN", Servers.loginServer));
            }
            buf.append("}");
            buf.append("text{text=\"\"}");
            entries = Servers.getAllServers();
            buf.append("harray{label{type=\"bold\";text='Add neighbour: '}dropdown{id='neighbourServer';default='0';options='");
            buf.append("None");
            for (x = 0; x < entries.length; ++x) {
                buf.append(",");
                this.serverEntries.add(entries[x]);
                buf.append(entries[x].name);
            }
            buf.append("'}dropdown{id='direction';options='NORTH,EAST,SOUTH,WEST'}}");
            buf.append("harray{label{type=\"bold\";text='Remove server entry: '}dropdown{id='deleteServer';default='0';options='None");
            for (x = 0; x < entries.length; ++x) {
                buf.append(",");
                buf.append(entries[x].name);
            }
            buf.append("'}}");
            buf.append("label{type='bold';text='Add server entry: '}");
            buf.append("harray{label{text='Server Id: '}input{maxchars='3'; id='addid'}}");
            buf.append("harray{label{text='Name: '}input{maxchars='20'; id='addname'}}");
            buf.append("checkbox{text='Home Server?';id='addhome';selected='true'}");
            buf.append("checkbox{text='Payment Server?';id='addpayment';selected='true'}");
            buf.append("checkbox{text='Login Server?';id='addlogin;selected='false'}");
            buf.append("harray{label{text='Kingdom: '}dropdown{id='addkingdom';default='0';options='NONE,Jenn-Kellon,Mol Rehan,Horde of the Summoned'}}");
            buf.append("harray{label{text='StartJennX'}input{maxchars='4'; id='addsjx'}}");
            buf.append("harray{label{text='StartJennY'}input{maxchars='4'; id='addsjy'}}");
            buf.append("harray{label{text='StartMolX'}input{maxchars='4'; id='addsmx'}}");
            buf.append("harray{label{text='StartMolY'}input{maxchars='4'; id='addsmy'}}");
            buf.append("harray{label{text='StartLibX'}input{maxchars='4'; id='addslx'}}");
            buf.append("harray{label{text='StartLibY'}input{maxchars='4'; id='addsly'}}");
            buf.append("harray{label{text='External Ip'}input{maxchars='20'; id='addextip'}}");
            buf.append("harray{label{text='External Port'}input{maxchars='5'; id='addextport'}}");
            buf.append("harray{label{text='Internal Ip'}input{maxchars='20'; id='addintip'}}");
            buf.append("harray{label{text='Internal Port'}input{maxchars='5'; id='addintport'}}");
            buf.append("harray{label{text='Internal Password'}input{maxchars='50'; id='addintpass'}}");
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private String getServerEntryData(String serverType, ServerEntry server) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"" + serverType + "\"}label{text=\"" + server.name + "\"}label{text=\"" + server.PVPSERVER + "\"}label{text=\"" + server.EPIC + "\"}label{text=\"" + server.HOMESERVER + "\"}label{text=\"" + server.isChaosServer() + "\"}");
        return buf.toString();
    }

    ServerEntry getServerEntry(int aPosition) {
        return this.serverEntries.get(aPosition);
    }

    ServerEntry getTransferEntry(int aPosition) {
        return this.transferEntries.get(aPosition);
    }
}

