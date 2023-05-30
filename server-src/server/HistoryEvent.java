package com.wurmonline.server;

import java.text.DateFormat;
import java.util.Date;
import net.jcip.annotations.Immutable;

@Immutable
public final class HistoryEvent implements Comparable<HistoryEvent> {
   private final DateFormat df = DateFormat.getDateTimeInstance();
   public final long time;
   public final String performer;
   public final String event;
   public final int identifier;

   public HistoryEvent(long aTime, String aPerformer, String aEvent, int aIdentifier) {
      this.time = aTime;
      this.performer = aPerformer;
      this.event = aEvent;
      this.identifier = aIdentifier;
   }

   public String getDate() {
      return this.df.format(new Date(this.time));
   }

   public String getLongDesc() {
      return this.performer != null && this.performer.length() != 0
         ? this.getDate() + "  " + this.performer + " " + this.event
         : this.getDate() + "  " + this.event;
   }

   public int compareTo(HistoryEvent he) {
      return Long.compare(this.time, he.time);
   }

   @Override
   public String toString() {
      return "HistoryEvent [" + this.getLongDesc() + ']';
   }
}
