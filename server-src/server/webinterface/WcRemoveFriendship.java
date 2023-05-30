package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
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

public final class WcRemoveFriendship extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcRemoveFriendship.class.getName());
   private String playerName;
   private long playerWurmId;
   private String friendName;
   private long friendWurmId;

   public WcRemoveFriendship(String aPlayerName, long aPlayerWurmId, String aFriendName, long aFriendWurmId) {
      super(WurmId.getNextWCCommandId(), (short)4);
      this.playerName = aPlayerName;
      this.playerWurmId = aPlayerWurmId;
      this.friendName = aFriendName;
      this.friendWurmId = aFriendWurmId;
   }

   public WcRemoveFriendship(long aId, byte[] aData) {
      super(aId, (short)4, aData);
   }

   @Override
   public boolean autoForward() {
      return true;
   }

   @Override
   byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;
      byte[] barr = null;

      try {
         dos = new DataOutputStream(bos);
         dos.writeUTF(this.playerName);
         dos.writeLong(this.playerWurmId);
         dos.writeUTF(this.friendName);
         dos.writeLong(this.friendWurmId);
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
   
               label46: {
                  try {
                     dis = new DataInputStream(new ByteArrayInputStream(WcRemoveFriendship.this.getData()));
                     WcRemoveFriendship.this.playerName = dis.readUTF();
                     WcRemoveFriendship.this.playerWurmId = dis.readLong();
                     WcRemoveFriendship.this.friendName = dis.readUTF();
                     WcRemoveFriendship.this.friendWurmId = dis.readLong();
                     break label46;
                  } catch (IOException var6) {
                     WcRemoveFriendship.logger.log(Level.WARNING, "Unpack exception " + var6.getMessage(), (Throwable)var6);
                  } finally {
                     StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                  }
   
                  return;
               }
   
               if (Servers.isThisLoginServer()) {
                  if (WcRemoveFriendship.this.friendWurmId == -10L) {
                     PlayerInfo fInfo = PlayerInfoFactory.getPlayerInfoWithName(WcRemoveFriendship.this.friendName);
                     if (fInfo != null) {
                        WcRemoveFriendship.this.friendWurmId = fInfo.wurmId;
                     }
                  }
   
                  WcRemoveFriendship.this.sendFromLoginServer();
               }
   
               PlayerInfoFactory.breakFriendship(
                  WcRemoveFriendship.this.playerName,
                  WcRemoveFriendship.this.playerWurmId,
                  WcRemoveFriendship.this.friendName,
                  WcRemoveFriendship.this.friendWurmId
               );
            }
         })
         .start();
   }
}
