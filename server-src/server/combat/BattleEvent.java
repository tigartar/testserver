package com.wurmonline.server.combat;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.Actions;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;

public final class BattleEvent implements MiscConstants {
   public static final short BATTLEACTION_ATTACK = -1;
   public static final short BATTLEACTION_LEAVE = -2;
   public static final short BATTLEACTION_DIE = -3;
   private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
   private final short act;
   private final String perf;
   private final String rec;
   private String longString;
   private final long time;

   BattleEvent(short action, String performer) {
      this(action, performer, null);
   }

   public BattleEvent(short action, String performer, @Nullable String receiver) {
      this.act = action;
      this.perf = performer;
      this.rec = receiver;
      this.time = System.currentTimeMillis();
   }

   public BattleEvent(short action, String performer, String receiver, String longDesc) {
      this.act = action;
      this.perf = performer;
      this.rec = receiver;
      this.time = System.currentTimeMillis();
      this.longString = longDesc;
   }

   short getAction() {
      return this.act;
   }

   String getPerformer() {
      return this.perf;
   }

   String getReceiver() {
      return this.rec;
   }

   long getTime() {
      return this.time;
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.df.format(new Date(this.time)));
      buf.append(" : ");
      if (this.longString != null) {
         buf.append(this.longString);
      } else if (this.act == -1) {
         if (this.rec != null) {
            buf.append(this.perf);
            buf.append(" attacks ");
            buf.append(this.rec);
            buf.append(".");
         } else {
            buf.append(this.perf);
            buf.append(" joins the fray.");
         }
      } else if (this.act == -2) {
         buf.append(this.perf);
         buf.append(" leaves the battle.");
      } else if (this.act == -3) {
         if (this.rec != null) {
            buf.append(this.perf);
            buf.append(" dies by the hands of ");
            buf.append(this.rec);
            buf.append(".");
         } else {
            buf.append(this.perf);
            buf.append(" dies.");
         }
      } else {
         String verb = Actions.actionEntrys[this.act].getActionString();
         if (this.rec != null) {
            buf.append(this.perf);
            buf.append(" ");
            buf.append(verb);
            buf.append("s at ");
            buf.append(this.rec);
            buf.append(".");
         } else {
            buf.append(this.perf);
            buf.append(" ");
            buf.append(verb);
            buf.append("s ");
            buf.append(".");
         }
      }

      buf.append("<br>");
      return buf.toString();
   }
}
