package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.shared.util.StringUtilities;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.Set;

public class SelectSpellQuestion extends Question implements TimeConstants {
   private static final boolean useSets = false;

   public SelectSpellQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 80, aTarget);
   }

   private String getDeityPassives(Deity deity, int level) {
      StringBuilder buf = new StringBuilder();
      if (!deity.isHateGod()) {
         buf.append("text{text=\"You are aligned with the benevolent.\"};");
      } else {
         buf.append("text{text=\"You are aligned with the malevolent.\"};");
      }

      if (deity.isClayAffinity()) {
         buf.append("text{text=\"" + deity.getName() + " is interested in pottery.\"};");
      }

      if (deity.isClothAffinity()) {
         buf.append("text{text=\"" + deity.getName() + " is interested in cloth.\"};");
      }

      if (deity.isMetalAffinity()) {
         buf.append("text{text=\"" + deity.getName() + " is interested in metal.\"};");
      }

      if (deity.isWoodAffinity()) {
         buf.append("text{text=\"" + deity.getName() + " is interested in wood.\"};");
      }

      if (deity.isMeatAffinity()) {
         buf.append("text{text=\"" + deity.getName() + " is interested in the products of death.\"};");
      }

      if (deity.isFoodAffinity()) {
         buf.append("text{text=\"" + deity.getName() + " is interested in food.\"};");
      }

      buf.append("text{text=\"\"};");
      if (deity.isLearner() && level >= 20) {
         buf.append("text{text=\"You feel capable of learning swiftly.\"};");
      }

      if (deity.isWarrior() && level >= 20) {
         buf.append("text{text=\"You feel a higher aptitude for combat.\"};");
      }

      if (deity.isStaminaBonus() && level >= 20) {
         buf.append("text{text=\"You feel able to catch your breath quickly.\"};");
      }

      if (deity.isFoodBonus() && level >= 20) {
         buf.append("text{text=\"You feel less hungry than normal.\"};");
      }

      if (deity.isHealer() && level >= 20) {
         buf.append("text{text=\"Your wounds heal more quickly.\"};");
      }

      if (deity.isForestGod() && level >= 35) {
         buf.append("text{text=\"You feel thorns no longer pierce you.\"};");
      }

      if (deity.isMountainGod() && level >= 35) {
         buf.append("text{text=\"You feel lava no longer burns you.\"};");
      }

      if (deity.isFavorRegenerator() && level >= 35) {
         buf.append("text{text=\"You feel your favor comes back to you faster than normal.\"};");
      }

      if (deity.isWarrior() && level >= 40) {
         buf.append("text{text=\"You feel capable of striking harder in combat.\"};");
      }

      if (deity.isBefriendCreature() && level >= 60) {
         buf.append("text{text=\"You feel attuned with animals.\"};");
      }

      if (deity.isBefriendMonster() && level >= 60) {
         buf.append("text{text=\"You feel attuned with monsters.\"};");
      }

      if (deity.isRoadProtector() && level >= 60) {
         buf.append("text{text=\"You feel light footed on pavement.\"};");
      }

      if (deity.isHateGod() && level >= 60) {
         buf.append("text{text=\"You feel light footed on mycelium.\"};");
      }

      if (deity.isMountainGod() && level >= 60) {
         buf.append("text{text=\"You feel light footed on rock and cliffs.\"};");
      }

      if (deity.isDeathProtector() && level >= 60) {
         buf.append("text{text=\"You feel that " + deity.getName() + " may protect your skills in death.\"};");
      }

      if (deity.isItemProtector() && level >= 70) {
         buf.append("text{text=\"You feel your possessions resist the elements.\"};");
      }

      if (deity.isForestGod() && level >= 70) {
         buf.append("text{text=\"You feel light footed on natural land.\"};");
      }

      if (deity.isDeathItemProtector() && level >= 70) {
         buf.append("text{text=\"You feel that " + deity.getName() + " may protect your items in death.\"};");
      }

      if (deity.isRepairer() && level >= 80) {
         buf.append("text{text=\"You feel more skilled at improvement.\"};");
      }

      if (deity.isFo() && level >= 70) {
         buf.append("text{text=\"" + deity.getName() + " makes you more capable in combat when fighting in the wild.\"};");
      }

      if ((deity.isMagranon() || deity.isLibila()) && level >= 70) {
         buf.append("text{text=\"" + deity.getName() + " makes you more capable in combat when on the offensive.\"};");
      }

      if (deity.isVynora() && level >= 70) {
         buf.append("text{text=\"" + deity.getName() + " makes you more capable in combat when on the defensive while on pavement or at sea.\"};");
      }

      return buf.toString();
   }

   private String getDeityConnection(int level) {
      if (level < 40) {
         return "faint";
      } else if (level < 50) {
         return "mild";
      } else if (level < 60) {
         return "moderate";
      } else if (level < 70) {
         return "good";
      } else if (level < 80) {
         return "strong";
      } else if (level < 90) {
         return "powerful";
      } else {
         return level < 100 ? "deep" : "perfect";
      }
   }

   @Override
   public void answer(Properties aAnswers) {
   }

   private String addSpell(int id, Spell spell) {
      StringBuilder buf = new StringBuilder();
      buf.append(
         "row{id=\"e"
            + id
            + "\";hover=\""
            + StringUtilities.raiseFirstLetterOnly(spell.getDescription())
            + "\";name=\""
            + spell.getName()
            + "\";rarity=\"0\";children=\"0\";col{text=\""
            + spell.level
            + "\"};col{text=\""
            + spell.getCost()
            + "\"};col{text=\""
            + spell.getDifficulty(false)
            + "\"};col{text=\""
            + getTargets(spell)
            + "\"};col{text=\""
            + StringUtilities.raiseFirstLetterOnly(spell.getDescription())
            + "\"}}"
      );
      return buf.toString();
   }

   @Override
   public void sendQuestion() {
      String lHtml = "border{scroll{vertical='true';horizontal='false';varray{rescale='true';passthrough{id='id';text='" + this.getId() + "'}";
      StringBuilder buf = new StringBuilder(lHtml);
      if (this.getResponder().getDeity() != null) {
         Deity deity = this.getResponder().getDeity();
         int level = (int)this.getResponder().getFaith();
         Set<Spell> spellset = this.getResponder().getDeity().getSpells();
         Spell[] spells = spellset.toArray(new Spell[spellset.size()]);
         Arrays.sort(spells, new Comparator<Spell>() {
            public int compare(Spell o1, Spell o2) {
               return o1.level - o2.level == 0 ? o1.name.compareTo(o2.name) : o1.level - o2.level;
            }
         });
         int available = 0;

         for(Spell s : spells) {
            if (s.level <= level && (!s.isRitual || deity.getFavor() > 100000 || Servers.isThisATestServer())) {
               ++available;
            }
         }

         String deityConnection;
         if (!deity.isCustomDeity()) {
            deityConnection = "You have a " + this.getDeityConnection(level) + " connection to " + deity.getName() + ".";
         } else {
            Deity templateGod = Deities.getDeity(deity.getTemplateDeity());
            deityConnection = "You have a "
               + this.getDeityConnection(level)
               + " connection to "
               + deity.getName()
               + ", demigod of "
               + templateGod.getName()
               + ".";
         }

         buf.append("text{text=\"" + deityConnection + "\";type=\"bold\"}");
         buf.append("text{text=\"\"}");
         buf.append(this.getDeityPassives(deity, level));
         buf.append("text{text=\"\"}");
         buf.append("text{text=\"The valid targets in the following table are (T)ile, (Wo)und, (C)reature.\";type=\"bold\"}");
         buf.append("text{text=\"Item-specific targets: (I)tem [any], (W)eapon, (A)rmour, (J)ewelry, (P)endulum.\";type=\"bold\"}");
         int rowNumb = 0;
         int height = 16 + 16 * available;
         buf.append(
            "tree{id=\"t1\";cols=\"5\";showheader=\"true\";height=\""
               + height
               + "\"col{text=\"Level\";width=\"50\"};col{text=\"Favor\";width=\"50\"};col{text=\"Difficulty\";width=\"50\"};col{text=\"Targets\";width=\"50\"};col{text=\"Description\";width=\"300\"};"
         );

         for(Spell s : spells) {
            if (s.level <= level && (!s.isRitual || deity.getFavor() > 100000 || Servers.isThisATestServer())) {
               buf.append(this.addSpell(++rowNumb, s));
            }
         }

         buf.append("}");
      } else {
         buf.append("text{text=\"Fool, you don't even have a deity?\"}");
      }

      buf.append(this.createAnswerButton3());
      this.getResponder().getCommunicator().sendBml(700, 800, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   public static String getTargets(Spell s) {
      boolean addComma = false;
      StringBuilder tbuf = new StringBuilder();
      if (s.isTargetTile()) {
         tbuf.append("T");
         addComma = true;
      }

      if (s.isTargetCreature()) {
         if (addComma) {
            tbuf.append(",C");
         } else {
            tbuf.append("C");
         }

         addComma = true;
      }

      if (s.isTargetItem()) {
         if (addComma) {
            tbuf.append(",I");
         } else {
            tbuf.append("I");
         }

         addComma = true;
      }

      if (s.isTargetWeapon()) {
         if (addComma) {
            tbuf.append(",W");
         } else {
            tbuf.append("W");
         }

         addComma = true;
      }

      if (s.isTargetArmour()) {
         if (addComma) {
            tbuf.append(",A");
         } else {
            tbuf.append("A");
         }

         addComma = true;
      }

      if (s.isTargetJewelry()) {
         if (addComma) {
            tbuf.append(",J");
         } else {
            tbuf.append("J");
         }

         addComma = true;
      }

      if (s.isTargetPendulum()) {
         if (addComma) {
            tbuf.append(",P");
         } else {
            tbuf.append("P");
         }

         addComma = true;
      }

      if (s.isTargetWound()) {
         if (addComma) {
            tbuf.append(",Wo");
         } else {
            tbuf.append("Wo");
         }
      }

      return tbuf.toString();
   }
}
