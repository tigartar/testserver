package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
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

public class WcGlobalPM extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcGlobalPM.class.getName());
   public static final byte GETID = 0;
   public static final byte THE_ID = 2;
   public static final byte SEND = 3;
   public static final byte IGNORED = 5;
   public static final byte NA = 6;
   public static final byte AFK = 7;
   private byte action = 3;
   private byte power = 0;
   private long senderId = -10L;
   private String senderName = "unknown";
   private byte kingdom = 0;
   private int targetServerId = 0;
   private long targetId = -10L;
   private String targetName = "unknown";
   private boolean friend = false;
   private String message = "";
   private boolean emote = false;
   private boolean override = false;

   public WcGlobalPM(
      long aId,
      byte _action,
      byte _power,
      long _senderId,
      String _senderName,
      byte _kingdom,
      int _targetServerId,
      long _targetId,
      String _targetName,
      boolean _friend,
      String _message,
      boolean _emote,
      boolean aOverride
   ) {
      super(aId, (short)17);
      this.action = _action;
      this.power = _power;
      this.senderId = _senderId;
      this.senderName = _senderName;
      this.kingdom = _kingdom;
      this.targetServerId = _targetServerId;
      this.targetId = _targetId;
      this.targetName = _targetName;
      this.friend = _friend;
      this.message = _message;
      this.emote = _emote;
      this.override = aOverride;
   }

   public WcGlobalPM(long _id, byte[] _data) {
      super(_id, (short)17, _data);
   }

   @Override
   public boolean autoForward() {
      return false;
   }

   @Override
   public byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;
      byte[] barr = null;

      try {
         dos = new DataOutputStream(bos);
         dos.writeByte(this.action);
         dos.writeByte(this.power);
         dos.writeLong(this.senderId);
         dos.writeUTF(this.senderName);
         dos.writeByte(this.kingdom);
         dos.writeInt(this.targetServerId);
         dos.writeLong(this.targetId);
         dos.writeUTF(this.targetName);
         dos.writeBoolean(this.friend);
         dos.writeUTF(this.message);
         dos.writeBoolean(this.emote);
         dos.writeBoolean(this.override);
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
               DataInputStream dis = null;
   
               label84: {
                  try {
                     dis = new DataInputStream(new ByteArrayInputStream(WcGlobalPM.this.getData()));
                     WcGlobalPM.this.action = dis.readByte();
                     WcGlobalPM.this.power = dis.readByte();
                     WcGlobalPM.this.senderId = dis.readLong();
                     WcGlobalPM.this.senderName = dis.readUTF();
                     WcGlobalPM.this.kingdom = dis.readByte();
                     WcGlobalPM.this.targetServerId = dis.readInt();
                     WcGlobalPM.this.targetId = dis.readLong();
                     WcGlobalPM.this.targetName = dis.readUTF();
                     WcGlobalPM.this.friend = dis.readBoolean();
                     WcGlobalPM.this.message = dis.readUTF();
                     WcGlobalPM.this.emote = dis.readBoolean();
                     WcGlobalPM.this.override = dis.readBoolean();
                     break label84;
                  } catch (IOException var13) {
                     WcGlobalPM.logger.log(Level.WARNING, "Unpack exception " + var13.getMessage(), (Throwable)var13);
                  } finally {
                     StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                  }
   
                  return;
               }
   
               if (WcGlobalPM.this.action == 0) {
                  PlayerInfo pInfo = PlayerInfoFactory.createPlayerInfo(WcGlobalPM.this.targetName);
                  if (pInfo != null) {
                     try {
                        pInfo.load();
                        WcGlobalPM.this.targetId = pInfo.wurmId;
                        WcGlobalPM.this.targetServerId = pInfo.currentServer;
                     } catch (IOException var12) {
                        WcGlobalPM.this.targetId = -10L;
                     }
                  }
   
                  WcGlobalPM wgi = new WcGlobalPM(
                     WurmId.getNextWCCommandId(),
                     (byte)2,
                     WcGlobalPM.this.power,
                     WcGlobalPM.this.senderId,
                     WcGlobalPM.this.senderName,
                     WcGlobalPM.this.kingdom,
                     WcGlobalPM.this.targetServerId,
                     WcGlobalPM.this.targetId,
                     WcGlobalPM.this.targetName,
                     WcGlobalPM.this.friend,
                     WcGlobalPM.this.message,
                     WcGlobalPM.this.emote,
                     WcGlobalPM.this.override
                  );
                  wgi.sendToServer(WurmId.getOrigin(WcGlobalPM.this.getWurmId()));
               } else if (WcGlobalPM.this.action == 3) {
                  PlayerInfo pInfo = PlayerInfoFactory.createPlayerInfo(WcGlobalPM.this.targetName);
                  if (pInfo == null) {
                     WcGlobalPM.logger.log(Level.WARNING, "no player '" + WcGlobalPM.this.targetName + "' Info?");
                     return;
                  }
   
                  WcGlobalPM.this.targetServerId = pInfo.currentServer;
                  if (pInfo.currentServer == Servers.getLocalServerId()) {
                     try {
                        Player p = Players.getInstance().getPlayer(WcGlobalPM.this.targetName);
                        if (!p.sendPM(
                           WcGlobalPM.this.power,
                           WcGlobalPM.this.senderName,
                           WcGlobalPM.this.senderId,
                           WcGlobalPM.this.friend,
                           WcGlobalPM.this.message,
                           WcGlobalPM.this.emote,
                           WcGlobalPM.this.kingdom,
                           WurmId.getOrigin(WcGlobalPM.this.getWurmId()),
                           WcGlobalPM.this.override
                        )) {
                           WcGlobalPM wgi = new WcGlobalPM(
                              WurmId.getNextWCCommandId(),
                              (byte)6,
                              WcGlobalPM.this.power,
                              WcGlobalPM.this.senderId,
                              WcGlobalPM.this.senderName,
                              WcGlobalPM.this.kingdom,
                              WcGlobalPM.this.targetServerId,
                              WcGlobalPM.this.targetId,
                              WcGlobalPM.this.targetName,
                              WcGlobalPM.this.friend,
                              WcGlobalPM.this.message,
                              WcGlobalPM.this.emote,
                              WcGlobalPM.this.override
                           );
                           wgi.sendToServer(WurmId.getOrigin(WcGlobalPM.this.getWurmId()));
                        } else if (p.isAFK()) {
                           WcGlobalPM wgi = new WcGlobalPM(
                              WurmId.getNextWCCommandId(),
                              (byte)7,
                              WcGlobalPM.this.power,
                              WcGlobalPM.this.senderId,
                              WcGlobalPM.this.senderName,
                              WcGlobalPM.this.kingdom,
                              WcGlobalPM.this.targetServerId,
                              WcGlobalPM.this.targetId,
                              WcGlobalPM.this.targetName,
                              WcGlobalPM.this.friend,
                              p.getAFKMessage(),
                              true,
                              WcGlobalPM.this.override
                           );
                           wgi.sendToServer(WurmId.getOrigin(WcGlobalPM.this.getWurmId()));
                        }
                     } catch (NoSuchPlayerException var11) {
                        WcGlobalPM wgi = new WcGlobalPM(
                           WurmId.getNextWCCommandId(),
                           (byte)6,
                           WcGlobalPM.this.power,
                           WcGlobalPM.this.senderId,
                           WcGlobalPM.this.senderName,
                           WcGlobalPM.this.kingdom,
                           WcGlobalPM.this.targetServerId,
                           WcGlobalPM.this.targetId,
                           WcGlobalPM.this.targetName,
                           WcGlobalPM.this.friend,
                           WcGlobalPM.this.message,
                           WcGlobalPM.this.emote,
                           WcGlobalPM.this.override
                        );
                        wgi.sendToServer(WurmId.getOrigin(WcGlobalPM.this.getWurmId()));
                     }
                  } else if (Servers.isThisLoginServer()) {
                     WcGlobalPM wgi = new WcGlobalPM(
                        WcGlobalPM.this.getWurmId(),
                        WcGlobalPM.this.action,
                        WcGlobalPM.this.power,
                        WcGlobalPM.this.senderId,
                        WcGlobalPM.this.senderName,
                        WcGlobalPM.this.kingdom,
                        WcGlobalPM.this.targetServerId,
                        WcGlobalPM.this.targetId,
                        WcGlobalPM.this.targetName,
                        WcGlobalPM.this.friend,
                        WcGlobalPM.this.message,
                        WcGlobalPM.this.emote,
                        WcGlobalPM.this.override
                     );
                     wgi.sendToServer(pInfo.currentServer);
                  } else {
                     WcGlobalPM.logger.log(Level.WARNING, "not on login or " + WcGlobalPM.this.targetName + "'s server!");
                  }
               } else {
                  try {
                     Player p = Players.getInstance().getPlayer(WcGlobalPM.this.senderName);
                     p.sendPM(
                        WcGlobalPM.this.action,
                        WcGlobalPM.this.targetName,
                        WcGlobalPM.this.targetId,
                        WcGlobalPM.this.message,
                        WcGlobalPM.this.emote,
                        WcGlobalPM.this.override
                     );
                  } catch (NoSuchPlayerException var10) {
                  }
               }
            }
         })
         .start();
   }
}
