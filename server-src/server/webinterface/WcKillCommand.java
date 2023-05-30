package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcKillCommand extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcKillCommand.class.getName());
   private long wurmID;

   public WcKillCommand(long _id, long _wurmID) {
      super(_id, (short)36);
      this.wurmID = _wurmID;
   }

   WcKillCommand(long _id, byte[] _data) {
      super(_id, (short)36, _data);
   }

   @Override
   public boolean autoForward() {
      return false;
   }

   @Override
   byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;

      byte[] barr;
      try {
         dos = new DataOutputStream(bos);
         dos.writeLong(this.wurmID);
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
      new Thread(() -> {
         DataInputStream dis = null;

         label45: {
            try {
               dis = new DataInputStream(new ByteArrayInputStream(this.getData()));
               this.wurmID = dis.readLong();
               break label45;
            } catch (IOException var8) {
               logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            } finally {
               StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            }

            return;
         }

         try {
            Creature animal = Creatures.getInstance().getCreature(this.wurmID);
            animal.die(true, "Died on another server.", true);
         } catch (NoSuchCreatureException var7) {
         }

         if (Servers.isThisLoginServer()) {
            WcKillCommand wkc = new WcKillCommand(this.getWurmId(), this.wurmID);
            wkc.sendFromLoginServer();
         }
      }).start();
   }
}
