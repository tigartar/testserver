package com.wurmonline.server.webinterface;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcSetPower extends WebCommand {
   private static final Logger logger = Logger.getLogger(WcSetPower.class.getName());
   private String playerName;
   private int newPower;
   private String senderName;
   private int senderPower;
   private String response;

   public WcSetPower(String playerName, int newPower, String senderName, int senderPower, String response) {
      this();
      this.playerName = playerName;
      this.newPower = newPower;
      this.senderName = senderName;
      this.senderPower = senderPower;
      this.response = response;
   }

   WcSetPower(WcSetPower copy) {
      this();
      this.playerName = copy.playerName;
      this.newPower = copy.newPower;
      this.senderName = copy.senderName;
      this.senderPower = copy.senderPower;
      this.response = copy.response;
   }

   WcSetPower() {
      super(WurmId.getNextWCCommandId(), (short)33);
   }

   public WcSetPower(long aId, byte[] _data) {
      super(aId, (short)33, _data);
   }

   @Override
   byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;
      byte[] byteArr = null;

      try {
         dos = new DataOutputStream(bos);
         dos.writeUTF(this.playerName);
         dos.writeInt(this.newPower);
         dos.writeUTF(this.senderName);
         dos.writeInt(this.senderPower);
         dos.writeUTF(this.response);
         dos.flush();
         dos.close();
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
               dis = new DataInputStream(new ByteArrayInputStream(WcSetPower.this.getData()));
               WcSetPower.this.playerName = dis.readUTF();
               WcSetPower.this.newPower = dis.readInt();
               WcSetPower.this.senderName = dis.readUTF();
               WcSetPower.this.senderPower = dis.readInt();
               WcSetPower.this.response = dis.readUTF();
               if (!WcSetPower.this.response.equals("")) {
                  try {
                     Player sender = Players.getInstance().getPlayer(WcSetPower.this.senderName);
                     sender.getCommunicator().sendSafeServerMessage(WcSetPower.this.response);
                     return;
                  } catch (Exception var17) {
                  }
               } else {
                  try {
                     Player p = Players.getInstance().getPlayer(WcSetPower.this.playerName);
                     if (p.getPower() > WcSetPower.this.senderPower) {
                        WcSetPower.this.response = "They are more powerful than you. You cannot set their power.";
                     } else {
                        p.setPower((byte)WcSetPower.this.newPower);
                        String powerName = this.getPowerName(WcSetPower.this.newPower);
                        p.getCommunicator().sendSafeServerMessage("Your status has been set by " + WcSetPower.this.senderName + " to " + powerName + "!");
                        WcSetPower.this.response = "You set the power of " + WcSetPower.this.playerName + " to the status of " + powerName;
                     }
                  } catch (NoSuchPlayerException var15) {
                     PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(WcSetPower.this.playerName);

                     try {
                        pinf.load();
                        if (pinf.getPower() > WcSetPower.this.senderPower) {
                           WcSetPower.this.response = "They are more powerful than you. You cannot set their power.";
                        } else {
                           pinf.setPower((byte)WcSetPower.this.newPower);
                           pinf.save();
                           String powerName = this.getPowerName(WcSetPower.this.newPower);
                           WcSetPower.this.response = "You set the power of " + WcSetPower.this.playerName + " to the power of " + powerName;
                        }
                     } catch (IOException var14) {
                        WcSetPower.this.response = "Error trying load or save player information who is currently offline.";
                     }
                  } catch (IOException var16) {
                     WcSetPower.this.response = "Error trying to set the power on the player who is currently online.";
                  }
               }
            } catch (IOException var18) {
               WcSetPower.logger.log(Level.WARNING, "Unpack exception " + var18.getMessage(), (Throwable)var18);
               WcSetPower.this.response = "Something went terribly wrong trying to set the power.";
            } finally {
               StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            }

            if (!WcSetPower.this.response.equals("")) {
               try {
                  WcSetPower wsp = new WcSetPower(WcSetPower.this);
                  wsp.sendToServer(WurmId.getOrigin(WcSetPower.this.getWurmId()));
               } catch (Exception var13) {
                  WcSetPower.logger.log(Level.WARNING, "Could not send response back after setting power", (Throwable)var13);
               }
            }
         }

         private String getPowerName(int power) {
            String powString = "normal adventurer";
            if (WcSetPower.this.newPower == 1) {
               powString = "hero";
            } else if (WcSetPower.this.newPower == 2) {
               powString = "demigod";
            } else if (WcSetPower.this.newPower == 3) {
               powString = "high god";
            } else if (WcSetPower.this.newPower == 4) {
               powString = "arch angel";
            } else if (WcSetPower.this.newPower == 5) {
               powString = "implementor";
            }

            return powString;
         }
      }).start();
   }
}
