package com.wurmonline.server.behaviours;

import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.BodyHuman;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.util.List;

public final class PracticeDollBehaviour extends ItemBehaviour {
   private static String[] typeString;

   PracticeDollBehaviour() {
      super((short)31);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
      if (target.getParentId() == -10L) {
         toReturn.add(Actions.actionEntrys[211]);
      }

      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
      if (target.getParentId() == -10L) {
         toReturn.add(Actions.actionEntrys[211]);
      }

      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      boolean done = true;
      if (action == 211) {
         if (target.getParentId() == -10L) {
            done = CombatEngine.attack(performer, target, counter, act);
         } else {
            performer.getCommunicator().sendNormalServerMessage("The practice doll must be on the ground.");
         }
      } else if (action == 1) {
         done = true;
         target.sendEnchantmentStrings(performer.getCommunicator());
      } else {
         done = super.action(act, performer, source, target, action, counter);
      }

      return done;
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      boolean done = true;
      if (action == 211) {
         if (target.getParentId() == -10L) {
            done = CombatEngine.attack(performer, target, counter, act);
         } else {
            performer.getCommunicator().sendNormalServerMessage("The practice doll must be on the ground.");
         }
      } else if (action == 1) {
         done = true;
         performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
         target.sendEnchantmentStrings(performer.getCommunicator());
      } else {
         done = super.action(act, performer, target, action, counter);
      }

      return done;
   }

   public static String getWoundLocationString(int location) {
      return typeString[location];
   }

   public static byte getRandomWoundPos() throws Exception {
      int rand = Server.rand.nextInt(100);
      if (rand < 3) {
         return 1;
      } else if (rand < 8) {
         return 5;
      } else if (rand < 13) {
         return 6;
      } else if (rand < 18) {
         return 7;
      } else if (rand < 23) {
         return 8;
      } else if (rand < 28) {
         return 9;
      } else if (rand < 32) {
         return 10;
      } else if (rand < 37) {
         return 11;
      } else if (rand < 42) {
         return 12;
      } else if (rand < 46) {
         return 13;
      } else if (rand < 50) {
         return 14;
      } else if (rand < 54) {
         return 15;
      } else if (rand < 58) {
         return 16;
      } else if (rand < 60) {
         return 17;
      } else if (rand < 61) {
         return 18;
      } else if (rand < 62) {
         return 19;
      } else if (rand < 73) {
         return 21;
      } else if (rand < 78) {
         return 22;
      } else if (rand < 83) {
         return 23;
      } else if (rand < 89) {
         return 24;
      } else if (rand < 90) {
         return 25;
      } else if (rand < 95) {
         return 26;
      } else if (rand < 100) {
         return 27;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   static {
      typeString = (new BodyHuman()).typeString;
   }
}
