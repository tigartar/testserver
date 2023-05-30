package com.wurmonline.server.creatures;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.spells.SpellEffect;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SpellEffects {
   private Creature creature;
   private final Map<Byte, SpellEffect> spellEffects;
   private static final Logger logger = Logger.getLogger(SpellEffects.class.getName());
   private static final SpellEffect[] EMPTY_SPELLS = new SpellEffect[0];

   SpellEffects(long _creatureId) {
      try {
         this.creature = Server.getInstance().getCreature(_creatureId);
      } catch (NoSuchCreatureException var5) {
      } catch (NoSuchPlayerException var6) {
         logger.log(Level.INFO, var6.getMessage(), (Throwable)var6);
      }

      this.spellEffects = new HashMap<>();
      if (WurmId.getType(_creatureId) == 0) {
         SpellEffect[] speffs = SpellEffect.loadEffectsForPlayer(_creatureId);

         for(int x = 0; x < speffs.length; ++x) {
            this.addSpellEffect(speffs[x]);
         }
      }
   }

   public Creature getCreature() {
      return this.creature;
   }

   public void addSpellEffect(SpellEffect effect) {
      SpellEffect old = this.getSpellEffect(effect.type);
      if (old != null && old.power > effect.power) {
         effect.delete();
      } else {
         if (old != null) {
            old.delete();
            if (this.creature != null) {
               this.creature.sendUpdateSpellEffect(effect);
            }
         } else if (this.creature != null) {
            this.creature.sendAddSpellEffect(effect);
         }

         this.spellEffects.put(effect.type, effect);
         if (effect.type == 22 && this.creature.getCurrentTile() != null) {
            this.creature.getCurrentTile().setNewRarityShader(this.creature);
         }
      }
   }

   public void sendAllSpellEffects() {
      if (this.creature != null) {
         for(SpellEffect sp : this.getEffects()) {
            this.creature.sendAddSpellEffect(sp);
         }
      }
   }

   public SpellEffect getSpellEffect(byte type) {
      Byte key = type;
      return this.spellEffects.containsKey(key) ? this.spellEffects.get(key) : null;
   }

   public SpellEffect[] getEffects() {
      return this.spellEffects.size() > 0 ? this.spellEffects.values().toArray(new SpellEffect[this.spellEffects.size()]) : EMPTY_SPELLS;
   }

   public void poll() {
      SpellEffect[] effects = this.getEffects();

      for(int x = 0; x < effects.length; ++x) {
         if (effects[x].type == 94 && Server.rand.nextInt(10) == 0) {
            Creature c = this.getCreature();

            try {
               c.addWoundOfType(
                  null,
                  (byte)4,
                  c.getBody().getRandomWoundPos(),
                  false,
                  0.0F,
                  true,
                  (double)(Math.max(20.0F, effects[x].getPower()) * 50.0F),
                  0.0F,
                  0.0F,
                  false,
                  true
               );
               c.getCommunicator().sendAlertServerMessage("The pain from the heat is excruciating!");
            } catch (Exception var5) {
               logger.log(Level.WARNING, c.getName() + ": " + var5.getMessage());
            }
         }

         if (effects[x].poll(this) && effects[x].type == 22) {
            Creature c = this.getCreature();
            if (c.getCurrentTile() != null) {
               c.getCurrentTile().setNewRarityShader(c);
            }
         }
      }
   }

   public SpellEffect removeSpellEffect(SpellEffect old) {
      if (old != null) {
         if (this.creature != null) {
            this.creature.removeSpellEffect(old);
         }

         old.delete();
         this.spellEffects.remove(old.type);
      }

      return old;
   }

   void destroy(boolean keepHunted) {
      SpellEffect[] effects = this.getEffects();
      SpellEffect hunted = null;

      for(int x = 0; x < effects.length; ++x) {
         if (effects[x].type == 64 && keepHunted) {
            if (effects[x].type == 64) {
               hunted = effects[x];
            }
         } else {
            if (this.creature != null && this.creature.getCommunicator() != null) {
               SpellEffectsEnum spellEffect = SpellEffectsEnum.getEnumByName(effects[x].getName());
               if (spellEffect != SpellEffectsEnum.NONE) {
                  this.creature.getCommunicator().sendRemoveSpellEffect(effects[x].id, spellEffect);
               }
            }

            effects[x].delete();
         }
      }

      this.spellEffects.clear();
      if (hunted == null) {
         if (!keepHunted) {
            this.creature = null;
         }
      } else {
         this.spellEffects.put((byte)64, hunted);
      }
   }

   public void sleep() {
      this.spellEffects.clear();
      this.creature = null;
   }
}
