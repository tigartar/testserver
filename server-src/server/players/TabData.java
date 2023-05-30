package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TabData implements MiscConstants {
   private final long wurmId;
   private final String name;
   private final byte power;
   private final boolean isVisible;
   private final int serverId;

   public TabData(long wurmid, String name, byte power, boolean isVisible) {
      this.wurmId = wurmid;
      this.name = name;
      this.isVisible = isVisible;
      this.power = power;
      this.serverId = Servers.getLocalServerId();
   }

   public TabData(DataInputStream dis) throws IOException {
      this.wurmId = dis.readLong();
      this.name = dis.readUTF();
      this.isVisible = dis.readBoolean();
      this.power = dis.readByte();
      this.serverId = dis.readInt();
   }

   public void pack(DataOutputStream dos) throws IOException {
      dos.writeLong(this.wurmId);
      dos.writeUTF(this.name);
      dos.writeBoolean(this.isVisible);
      dos.writeByte(this.power);
      dos.writeInt(this.serverId);
   }

   public long getWurmId() {
      return this.wurmId;
   }

   public String getName() {
      if (this.serverId != Servers.getLocalServerId()) {
         ServerEntry se = Servers.getServerWithId(this.serverId);
         return se != null ? this.name + " (" + se.getAbbreviation() + ")" : this.name + " (" + this.serverId + ")";
      } else {
         return this.name;
      }
   }

   public byte getPower() {
      return this.power;
   }

   public boolean isVisible() {
      return this.isVisible;
   }

   public int getServerId() {
      return this.serverId;
   }

   @Override
   public String toString() {
      return "(TabData:" + this.wurmId + "," + this.name + "," + this.power + "," + this.isVisible + "," + this.serverId + ")";
   }
}
