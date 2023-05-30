package com.wurmonline.server.utils;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;

public class CreatureLineSegment extends MulticolorLineSegment {
   private static final String YOU_STRING = "you";
   private Creature creature;

   public CreatureLineSegment(Creature c) {
      super(c == null ? "something" : c.getName(), (byte)0);
      this.creature = c;
   }

   public String getText(Creature sendingTo) {
      return sendingTo != this.creature ? this.getText() : "you";
   }

   public byte getColor(Creature sendingTo) {
      if (this.creature != null && sendingTo != null) {
         switch(this.creature.getAttitude(sendingTo)) {
            case 0:
               return 12;
            case 1:
            case 5:
               return 9;
            case 2:
            case 4:
               return 4;
            case 3:
            case 6:
               return 8;
            case 7:
               return 14;
            default:
               return this.getColor();
         }
      } else {
         return this.getColor();
      }
   }

   public static ArrayList<MulticolorLineSegment> cloneLineList(ArrayList<MulticolorLineSegment> list) {
      ArrayList<MulticolorLineSegment> toReturn = new ArrayList<>(list.size());

      for(MulticolorLineSegment s : list) {
         if (s instanceof CreatureLineSegment) {
            toReturn.add(new CreatureLineSegment(((CreatureLineSegment)s).creature));
         } else {
            toReturn.add(new MulticolorLineSegment(s.getText(), s.getColor()));
         }
      }

      return toReturn;
   }
}
