package com.wurmonline.server.webinterface;

import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Player;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcGetHeroes extends WebCommand {
   private static final Logger logger = Logger.getLogger(WcSetPower.class.getName());
   private long sender;
   private byte powerToCheck;
   private String response;

   public WcGetHeroes() {
      super(WurmId.getNextWCCommandId(), (short)34);
   }

   public WcGetHeroes(WcGetHeroes copy) {
      this();
      this.sender = copy.sender;
      this.powerToCheck = copy.powerToCheck;
      this.response = copy.response;
   }

   public WcGetHeroes(long _id, byte[] _data) {
      super(_id, (short)34, _data);
   }

   public WcGetHeroes(long sender, byte powerToCheck) {
      this();
      this.sender = sender;
      this.powerToCheck = powerToCheck;
      this.response = "";
   }

   @Override
   byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;
      byte[] byteArr = null;

      try {
         dos = new DataOutputStream(bos);
         dos.writeLong(this.sender);
         dos.writeByte(this.powerToCheck);
         dos.writeUTF(this.response);
      } catch (Exception var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
         byteArr = bos.toByteArray();
         StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
         this.setData(byteArr);
      }

      return byteArr;
   }

   @Override
   public boolean autoForward() {
      return false;
   }

   @Override
   public void execute() {
      (new Thread() {
         @Override
         public void run() {
            DataInputStream dis = null;

            try {
               dis = new DataInputStream(new ByteArrayInputStream(WcGetHeroes.this.getData()));
               WcGetHeroes.this.sender = dis.readLong();
               WcGetHeroes.this.powerToCheck = dis.readByte();
               WcGetHeroes.this.response = dis.readUTF();
               if (this.isResponse(WcGetHeroes.this.response)) {
                  this.sendResponseToPlayer();
               } else {
                  WcGetHeroes.this.response = WcGetHeroes.getHeroes(WcGetHeroes.this.powerToCheck);
                  WcGetHeroes wcg = new WcGetHeroes(WcGetHeroes.this);
                  wcg.sendToServer(WurmId.getOrigin(WcGetHeroes.this.getWurmId()));
               }
            } catch (IOException var6) {
               WcGetHeroes.logger.log(Level.WARNING, "Unpack exception " + var6.getMessage(), (Throwable)var6);
            } finally {
               StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            }
         }

         private boolean isResponse(String response) {
            return !response.equals("");
         }

         private void sendResponseToPlayer() {
            try {
               Player senderPlayer = Players.getInstance().getPlayer(WcGetHeroes.this.sender);
               senderPlayer.getCommunicator().sendSafeServerMessage(WcGetHeroes.this.response);
            } catch (Exception var2) {
            }
         }
      }).start();
   }

   private static String getPowerName(byte power) {
      String powerName = "heroes";
      if (power == 2) {
         powerName = "demigods";
      } else if (power == 3) {
         powerName = "high gods";
      } else if (power == 4) {
         powerName = "archangels";
      } else if (power == 5) {
         powerName = "implementors";
      }

      return powerName;
   }

   public static String getHeroes(byte powerToCheck) {
      String[] result = Players.getInstance().getHeros(powerToCheck);
      if (result.length == 0) {
         return Servers.localServer.getName() + " reports no " + getPowerName(powerToCheck);
      } else {
         StringBuilder sb = new StringBuilder(Servers.localServer.getName() + " reports the following " + getPowerName(powerToCheck) + ": ");

         for(int i = 0; i < result.length - 1; ++i) {
            sb.append(result[i]);
            sb.append(", ");
         }

         sb.append(result[result.length - 1]);
         return sb.toString();
      }
   }
}
