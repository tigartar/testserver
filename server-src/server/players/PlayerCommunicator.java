/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.players.Player;

public class PlayerCommunicator
extends Communicator {
    public PlayerCommunicator(Player aPlayer, SocketConnection aConn) {
        super(aPlayer, aConn);
    }
}

