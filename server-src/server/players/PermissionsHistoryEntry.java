package com.wurmonline.server.players;

import java.text.DateFormat;
import java.util.Date;

public class PermissionsHistoryEntry implements Comparable<PermissionsHistoryEntry> {
   private static final DateFormat df = DateFormat.getDateTimeInstance();
   private final long eventDate;
   private final long playerId;
   public final String performer;
   public final String event;

   public PermissionsHistoryEntry(long eventDate, long playerId, String performer, String event) {
      this.eventDate = eventDate;
      this.playerId = playerId;
      this.performer = performer;
      this.event = event;
   }

   long getEventDate() {
      return this.eventDate;
   }

   String getPlayerName() {
      return this.performer;
   }

   String getEvent() {
      return this.event;
   }

   public String getDate() {
      return df.format(new Date(this.eventDate));
   }

   public long getPlayerId() {
      return this.playerId;
   }

   public String getLongDesc() {
      return this.performer != null && this.performer.length() != 0
         ? this.getDate() + "  " + this.performer + " " + this.event
         : this.getDate() + "  " + this.event;
   }

   public int compareTo(PermissionsHistoryEntry he) {
      return Long.compare(this.eventDate, he.eventDate);
   }

   @Override
   public String toString() {
      return "PermissionsHistoryEntry [" + this.getLongDesc() + ']';
   }
}
