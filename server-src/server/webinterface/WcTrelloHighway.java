package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.support.Trello;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcTrelloHighway extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcTrelloHighway.class.getName());
   private String server = "";
   private String title = "";
   private String description = "";

   WcTrelloHighway(long aId, byte[] _data) {
      super(aId, (short)32, _data);
   }

   public WcTrelloHighway(String title, String description) {
      super(WurmId.getNextWCCommandId(), (short)32);
      this.server = Servers.localServer.getAbbreviation();
      this.title = title;
      this.description = description;
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
         dos.writeUTF(this.server);
         dos.writeUTF(this.title);
         dos.writeUTF(this.description);
         dos.flush();
         dos.close();
      } catch (Exception var8) {
         logger.log(Level.WARNING, "Pack exception " + var8.getMessage(), (Throwable)var8);
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
      DataInputStream dis = null;

      try {
         dis = new DataInputStream(new ByteArrayInputStream(this.getData()));
         this.server = dis.readUTF();
         this.title = dis.readUTF();
         this.description = dis.readUTF();
         Trello.addHighwayMessage(this.server, this.title, this.description);
      } catch (IOException var6) {
         logger.log(Level.WARNING, "Unpack exception " + var6.getMessage(), (Throwable)var6);
      } finally {
         StreamUtilities.closeInputStreamIgnoreExceptions(dis);
      }
   }
}
