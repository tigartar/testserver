package com.wurmonline.server.players;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.server.creatures.Communicator;

public class PlayerCommunicator extends Communicator {
   public PlayerCommunicator(Player aPlayer, SocketConnection aConn) {
      super(aPlayer, aConn);
   }
}
