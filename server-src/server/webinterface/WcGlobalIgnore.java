package com.wurmonline.server.webinterface;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Communicator;
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

public final class WcGlobalIgnore extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcGlobalIgnore.class.getName());
   private long senderWurmId;
   private String ignorerName;
   private long targetWurmId;
   private String ignoreTarget;
   private boolean response = false;
   private boolean cant = false;
   private boolean triggerMute = false;
   private boolean startIgnore = true;
   private boolean startUnIgnore = false;
   private byte ignorerKingdom = 0;

   public WcGlobalIgnore(
      long _id,
      long senderId,
      String ignorer,
      long targetId,
      String ignored,
      boolean isResponseCommand,
      boolean cannot,
      boolean muting,
      boolean ignoring,
      boolean unIgnoring,
      byte kingdomId
   ) {
      super(_id, (short)15);
      this.ignorerName = LoginHandler.raiseFirstLetter(ignorer);
      this.ignoreTarget = LoginHandler.raiseFirstLetter(ignored);
      this.senderWurmId = senderId;
      this.targetWurmId = targetId;
      this.response = isResponseCommand;
      this.cant = cannot;
      this.triggerMute = muting;
      this.startIgnore = ignoring;
      this.startUnIgnore = unIgnoring;
      this.ignorerKingdom = kingdomId;
   }

   public WcGlobalIgnore(long _id, byte[] _data) {
      super(_id, (short)15, _data);
   }

   @Override
   public boolean autoForward() {
      return false;
   }

   @Override
   byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;
      byte[] barr = null;

      try {
         dos = new DataOutputStream(bos);
         dos.writeBoolean(this.response);
         dos.writeBoolean(this.cant);
         dos.writeLong(this.senderWurmId);
         dos.writeUTF(this.ignorerName);
         dos.writeLong(this.targetWurmId);
         dos.writeUTF(this.ignoreTarget);
         dos.writeBoolean(this.triggerMute);
         dos.writeBoolean(this.startIgnore);
         dos.writeBoolean(this.startUnIgnore);
         dos.writeByte(this.ignorerKingdom);
         dos.flush();
         dos.close();
      } catch (Exception var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
         barr = bos.toByteArray();
         StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
         this.setData(barr);
      }

      return barr;
   }

   @Override
   public void execute() {
      (new Thread() {
            @Override
            public void run() {
               if (Servers.isThisATestServer()) {
                  WcGlobalIgnore.logger.log(Level.INFO, "Starting a global ignore.");
               }
   
               DataInputStream dis = null;
   
               label165: {
                  try {
                     dis = new DataInputStream(new ByteArrayInputStream(WcGlobalIgnore.this.getData()));
                     WcGlobalIgnore.this.response = dis.readBoolean();
                     WcGlobalIgnore.this.cant = dis.readBoolean();
                     WcGlobalIgnore.this.senderWurmId = dis.readLong();
                     WcGlobalIgnore.this.ignorerName = dis.readUTF();
                     WcGlobalIgnore.this.targetWurmId = dis.readLong();
                     WcGlobalIgnore.this.ignoreTarget = dis.readUTF();
                     WcGlobalIgnore.this.triggerMute = dis.readBoolean();
                     WcGlobalIgnore.this.startIgnore = dis.readBoolean();
                     WcGlobalIgnore.this.startUnIgnore = dis.readBoolean();
                     WcGlobalIgnore.this.ignorerKingdom = dis.readByte();
                     break label165;
                  } catch (IOException var11) {
                     WcGlobalIgnore.logger.log(Level.WARNING, "Unpack exception " + var11.getMessage(), (Throwable)var11);
                  } finally {
                     StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                  }
   
                  return;
               }
   
               if (Servers.isThisATestServer()) {
                  WcGlobalIgnore.logger
                     .log(
                        Level.INFO,
                        WcGlobalIgnore.this.senderWurmId
                           + "("
                           + WcGlobalIgnore.this.ignorerName
                           + ") attempting ignore for "
                           + WcGlobalIgnore.this.targetWurmId
                           + " ("
                           + WcGlobalIgnore.this.ignoreTarget
                           + "), response="
                           + WcGlobalIgnore.this.response
                           + ", Ignore="
                           + WcGlobalIgnore.this.startIgnore
                           + ", UnIgnore="
                           + WcGlobalIgnore.this.startUnIgnore
                     );
               }
   
               if (!WcGlobalIgnore.this.response) {
                  PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(WcGlobalIgnore.this.targetWurmId);
   
                  try {
                     if (pinf == null) {
                        pinf = PlayerInfoFactory.createPlayerInfo(WcGlobalIgnore.this.ignoreTarget);
                     }
   
                     pinf.load();
                  } catch (IOException var10) {
                  }
   
                  if (pinf.loaded && (!Servers.isThisLoginServer() || pinf.getCurrentServer() == Servers.getLocalServerId())) {
                     if (WcGlobalIgnore.this.startUnIgnore) {
                        WcGlobalIgnore.logger
                           .log(
                              Level.INFO,
                              WcGlobalIgnore.this.senderWurmId
                                 + " ("
                                 + WcGlobalIgnore.this.ignorerName
                                 + ") unignoring "
                                 + WcGlobalIgnore.this.targetWurmId
                                 + " ("
                                 + WcGlobalIgnore.this.ignoreTarget
                                 + ")"
                           );
                        WcGlobalIgnore wgi = new WcGlobalIgnore(
                           WurmId.getNextWCCommandId(),
                           WcGlobalIgnore.this.senderWurmId,
                           WcGlobalIgnore.this.ignorerName,
                           pinf.wurmId,
                           WcGlobalIgnore.this.ignoreTarget,
                           true,
                           false,
                           WcGlobalIgnore.this.triggerMute,
                           WcGlobalIgnore.this.startIgnore,
                           WcGlobalIgnore.this.startUnIgnore,
                           WcGlobalIgnore.this.ignorerKingdom
                        );
                        wgi.sendToServer(WurmId.getOrigin(WcGlobalIgnore.this.getWurmId()));
                        return;
                     }
   
                     if (pinf.getPower() > 1 || pinf.mayMute) {
                        WcGlobalIgnore.logger
                           .log(
                              Level.INFO,
                              WcGlobalIgnore.this.senderWurmId
                                 + " ("
                                 + WcGlobalIgnore.this.ignorerName
                                 + ") Cannot ignore "
                                 + WcGlobalIgnore.this.targetWurmId
                                 + " ("
                                 + WcGlobalIgnore.this.ignoreTarget
                                 + ") as they can mute."
                           );
                        WcGlobalIgnore wgi = new WcGlobalIgnore(
                           WurmId.getNextWCCommandId(),
                           WcGlobalIgnore.this.senderWurmId,
                           WcGlobalIgnore.this.ignorerName,
                           pinf.wurmId,
                           WcGlobalIgnore.this.ignoreTarget,
                           true,
                           true,
                           WcGlobalIgnore.this.triggerMute,
                           WcGlobalIgnore.this.startIgnore,
                           WcGlobalIgnore.this.startUnIgnore,
                           WcGlobalIgnore.this.ignorerKingdom
                        );
                        wgi.sendToServer(WurmId.getOrigin(WcGlobalIgnore.this.getWurmId()));
                        return;
                     }
   
                     if (WcGlobalIgnore.this.triggerMute) {
                        WcGlobalIgnore.logger
                           .log(
                              Level.INFO,
                              WcGlobalIgnore.this.senderWurmId
                                 + " ("
                                 + WcGlobalIgnore.this.ignorerName
                                 + ") triggered muting for "
                                 + WcGlobalIgnore.this.ignoreTarget
                                 + "="
                                 + WcGlobalIgnore.this.triggerMute
                           );
                        Communicator.attemptMuting(WcGlobalIgnore.this.ignorerKingdom, pinf);
                     }
   
                     WcGlobalIgnore.logger
                        .log(
                           Level.INFO,
                           WcGlobalIgnore.this.senderWurmId
                              + " ("
                              + WcGlobalIgnore.this.ignorerName
                              + ") sending response back to server "
                              + WurmId.getOrigin(WcGlobalIgnore.this.getWurmId())
                        );
                     WcGlobalIgnore wgi = new WcGlobalIgnore(
                        WurmId.getNextWCCommandId(),
                        WcGlobalIgnore.this.senderWurmId,
                        WcGlobalIgnore.this.ignorerName,
                        pinf.wurmId,
                        WcGlobalIgnore.this.ignoreTarget,
                        true,
                        false,
                        WcGlobalIgnore.this.triggerMute,
                        WcGlobalIgnore.this.startIgnore,
                        WcGlobalIgnore.this.startUnIgnore,
                        WcGlobalIgnore.this.ignorerKingdom
                     );
                     wgi.sendToServer(WurmId.getOrigin(WcGlobalIgnore.this.getWurmId()));
                  } else if (Servers.isThisLoginServer() && pinf.loaded) {
                     WcGlobalIgnore.logger
                        .log(
                           Level.INFO,
                           WcGlobalIgnore.this.senderWurmId
                              + " ("
                              + WcGlobalIgnore.this.ignorerName
                              + ") redirecting of  "
                              + WcGlobalIgnore.this.targetWurmId
                              + " ("
                              + WcGlobalIgnore.this.ignoreTarget
                              + ") to "
                              + pinf.getCurrentServer()
                        );
                     WcGlobalIgnore wgi = new WcGlobalIgnore(
                        WcGlobalIgnore.this.getWurmId(),
                        WcGlobalIgnore.this.senderWurmId,
                        WcGlobalIgnore.this.ignorerName,
                        WcGlobalIgnore.this.targetWurmId,
                        WcGlobalIgnore.this.ignoreTarget,
                        false,
                        false,
                        WcGlobalIgnore.this.triggerMute,
                        WcGlobalIgnore.this.startIgnore,
                        WcGlobalIgnore.this.startUnIgnore,
                        WcGlobalIgnore.this.ignorerKingdom
                     );
                     wgi.sendToServer(pinf.getCurrentServer());
                  } else if (!pinf.loaded) {
                     WcGlobalIgnore.logger
                        .log(
                           Level.INFO,
                           WcGlobalIgnore.this.senderWurmId
                              + " ("
                              + WcGlobalIgnore.this.ignorerName
                              + ") sending response back to server as cannot find player "
                              + WurmId.getOrigin(WcGlobalIgnore.this.getWurmId())
                        );
                     WcGlobalIgnore wgi = new WcGlobalIgnore(
                        WurmId.getNextWCCommandId(),
                        WcGlobalIgnore.this.senderWurmId,
                        WcGlobalIgnore.this.ignorerName,
                        WcGlobalIgnore.this.targetWurmId,
                        WcGlobalIgnore.this.ignoreTarget,
                        true,
                        false,
                        WcGlobalIgnore.this.triggerMute,
                        WcGlobalIgnore.this.startUnIgnore,
                        WcGlobalIgnore.this.startUnIgnore,
                        WcGlobalIgnore.this.ignorerKingdom
                     );
                     wgi.sendToServer(WurmId.getOrigin(WcGlobalIgnore.this.getWurmId()));
                  }
               } else {
                  WcGlobalIgnore.logger
                     .log(
                        Level.INFO,
                        WcGlobalIgnore.this.senderWurmId
                           + " ("
                           + WcGlobalIgnore.this.ignorerName
                           + ") receiving ignore response for "
                           + WcGlobalIgnore.this.targetWurmId
                     );
                  PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(WcGlobalIgnore.this.senderWurmId);
                  String toSend = "";
                  if (WcGlobalIgnore.this.cant) {
                     if (WcGlobalIgnore.this.startIgnore) {
                        toSend = "You may not ignore " + WcGlobalIgnore.this.ignoreTarget + '.';
                     } else {
                        toSend = "You may not snipe " + WcGlobalIgnore.this.ignoreTarget + '.';
                     }
                  } else if (WcGlobalIgnore.this.startIgnore) {
                     try {
                        pinf.addIgnored(WcGlobalIgnore.this.targetWurmId, false);
                        toSend = "You now ignore " + WcGlobalIgnore.this.ignoreTarget + '.';
                     } catch (IOException var9) {
                        WcGlobalIgnore.logger.log(Level.WARNING, "Failed to add ignored for " + WcGlobalIgnore.this.ignoreTarget, (Throwable)var9);
                        toSend = "Failed to add ignored for " + WcGlobalIgnore.this.ignoreTarget + '.';
                     }
                  } else if (WcGlobalIgnore.this.startUnIgnore) {
                     pinf.removeIgnored(WcGlobalIgnore.this.targetWurmId);
                     toSend = "You no longer ignore " + WcGlobalIgnore.this.ignoreTarget + '.';
                  } else {
                     toSend = "You have sniped " + WcGlobalIgnore.this.ignoreTarget + '.';
                  }
   
                  if (Servers.isThisATestServer()) {
                     toSend = "(from:" + WurmId.getOrigin(WcGlobalIgnore.this.getWurmId()) + ") " + toSend;
                  }
   
                  Message mess = new Message(null, (byte)17, ":Event", toSend);
                  mess.setReceiver(WcGlobalIgnore.this.senderWurmId);
                  Server.getInstance().addMessage(mess);
               }
            }
         })
         .start();
   }
}
