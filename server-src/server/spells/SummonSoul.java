package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.SummonSoulQuestion;
import com.wurmonline.server.skills.Skill;

public class SummonSoul extends ReligiousSpell {
   public static final int RANGE = 40;

   public SummonSoul() {
      super("Summon Soul", 934, 30, 100, 10, 80, 0L);
      this.targetCreature = true;
      this.targetItem = true;
      this.targetTile = true;
      this.description = "summons a willing player to your location";
      this.type = 2;
   }

   public static boolean mayCastSummonSoul(Creature performer) {
      if (Servers.isThisAPvpServer() && performer.getEnemyPresense() > 0) {
         performer.getCommunicator().sendNormalServerMessage("Enemies are nearby, you cannot cast Summon Soul right now.");
         return false;
      } else {
         return true;
      }
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
      return mayCastSummonSoul(performer);
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer, int heightOffset, Tiles.TileBorderDirection dir) {
      return mayCastSummonSoul(performer);
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      return mayCastSummonSoul(performer);
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Item target) {
      return mayCastSummonSoul(performer);
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      SummonSoulQuestion ssq = new SummonSoulQuestion(performer, "Summon Soul", "Which soul do you wish to summon?", performer.getWurmId());
      ssq.sendQuestion();
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Item target) {
      SummonSoulQuestion ssq = new SummonSoulQuestion(performer, "Summon Soul", "Which soul do you wish to summon?", performer.getWurmId());
      ssq.sendQuestion();
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      SummonSoulQuestion ssq = new SummonSoulQuestion(performer, "Summon Soul", "Which soul do you wish to summon?", performer.getWurmId());
      ssq.sendQuestion();
   }
}
