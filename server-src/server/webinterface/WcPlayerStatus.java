package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcPlayerStatus extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcPlayerStatus.class.getName());
   public static final byte DO_NOTHING = 0;
   public static final byte WHOS_ONLINE = 1;
   public static final byte STATUS_CHANGE = 2;
   private byte type = 0;
   private String playerName;
   private long playerWurmId;
   private long lastLogin;
   private long lastLogout;
   private int currentServerId;
   private PlayerOnlineStatus status;

   public WcPlayerStatus() {
      super(WurmId.getNextWCCommandId(), (short)19);
      this.type = 1;
   }

   public WcPlayerStatus(PlayerState pState) {
      super(WurmId.getNextWCCommandId(), (short)19);
      this.type = 2;
      this.playerName = pState.getPlayerName();
      this.playerWurmId = pState.getPlayerId();
      this.lastLogin = pState.getLastLogin();
      this.lastLogout = pState.getLastLogout();
      this.currentServerId = pState.getServerId();
      this.status = pState.getState();
   }

   public WcPlayerStatus(String aPlayerName, long aPlayerWurmId, long aLastLogin, long aLastLogout, int aCurrentServerId, PlayerOnlineStatus aStatus) {
      super(WurmId.getNextWCCommandId(), (short)19);
      this.type = 2;
      this.playerName = aPlayerName;
      this.playerWurmId = aPlayerWurmId;
      this.lastLogin = aLastLogin;
      this.lastLogout = aLastLogout;
      this.currentServerId = aCurrentServerId;
      this.status = aStatus;
   }

   public WcPlayerStatus(long aId, byte[] aData) {
      super(aId, (short)19, aData);
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
         dos.writeByte(this.type);
         switch(this.type) {
            case 2:
               dos.writeUTF(this.playerName);
               dos.writeLong(this.playerWurmId);
               dos.writeLong(this.lastLogin);
               dos.writeLong(this.lastLogout);
               dos.writeInt(this.currentServerId);
               dos.writeByte(this.status.getId());
            case 1:
            default:
               dos.flush();
               dos.close();
         }
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
   
               label54: {
                  try {
                     dis = new DataInputStream(new ByteArrayInputStream(WcPlayerStatus.this.getData()));
                     WcPlayerStatus.this.type = dis.readByte();
                     switch(WcPlayerStatus.this.type) {
                        case 1:
                        default:
                           break label54;
                        case 2:
                           WcPlayerStatus.this.playerName = dis.readUTF();
                           WcPlayerStatus.this.playerWurmId = dis.readLong();
                           WcPlayerStatus.this.lastLogin = dis.readLong();
                           WcPlayerStatus.this.lastLogout = dis.readLong();
                           WcPlayerStatus.this.currentServerId = dis.readInt();
                           WcPlayerStatus.this.status = PlayerOnlineStatus.playerOnlineStatusFromId(dis.readByte());
                           break label54;
                     }
                  } catch (IOException var6) {
                     WcPlayerStatus.logger.log(Level.WARNING, "Unpack exception " + var6.getMessage(), (Throwable)var6);
                  } finally {
                     StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                  }
   
                  return;
               }
   
               if (WcPlayerStatus.this.type == 1) {
                  if (!Servers.isThisLoginServer()) {
                     PlayerInfoFactory.whosOnline();
                  }
               } else if (WcPlayerStatus.this.type == 2) {
                  PlayerState pState = new PlayerState(
                     WcPlayerStatus.this.currentServerId,
                     WcPlayerStatus.this.playerWurmId,
                     WcPlayerStatus.this.playerName,
                     WcPlayerStatus.this.lastLogin,
                     WcPlayerStatus.this.lastLogout,
                     WcPlayerStatus.this.status
                  );
                  PlayerInfoFactory.updatePlayerState(pState);
                  if (Servers.isThisLoginServer()) {
                     WcPlayerStatus.this.sendFromLoginServer();
                  }
               }
            }
         })
         .start();
   }
}
