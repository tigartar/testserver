package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemSpellEffects {
   private static final String GET_ALL_ITEMSPELLEFFECTS = "SELECT * FROM SPELLEFFECTS";
   private final Map<Byte, SpellEffect> spellEffects = new HashMap<>();
   private static final Logger logger = Logger.getLogger(ItemSpellEffects.class.getName());
   private static final Map<Long, ItemSpellEffects> itemSpellEffects = new HashMap<>();

   public ItemSpellEffects(long _itemId) {
      itemSpellEffects.put(new Long(_itemId), this);
   }

   public void addSpellEffect(SpellEffect effect) {
      SpellEffect old = this.getSpellEffect(effect.type);
      if (old != null && old.power > effect.power) {
         effect.delete();
      } else {
         if (old != null) {
            old.delete();
         }

         this.spellEffects.put(effect.type, effect);
      }
   }

   public byte getRandomRuneEffect() {
      for(int i = -128; i <= -51; ++i) {
         if (this.spellEffects.containsKey((byte)i)) {
            return (byte)i;
         }
      }

      return -10;
   }

   public float getRuneEffect(RuneUtilities.ModifierEffect effect) {
      float toReturn = 1.0F;

      for(int i = -128; i <= -51; ++i) {
         if (this.spellEffects.containsKey((byte)i)) {
            toReturn += RuneUtilities.getModifier((byte)i, effect);
         }
      }

      return toReturn;
   }

   public int getNumberOfRuneEffects() {
      int toReturn = 0;

      for(int i = -128; i <= -51; ++i) {
         if (this.spellEffects.containsKey((byte)i)) {
            ++toReturn;
         }
      }

      return toReturn;
   }

   public SpellEffect getSpellEffect(byte type) {
      return this.spellEffects.containsKey(type) ? this.spellEffects.get(type) : null;
   }

   public SpellEffect[] getEffects() {
      return this.spellEffects.values().toArray(new SpellEffect[this.spellEffects.size()]);
   }

   public SpellEffect removeSpellEffect(byte number) {
      SpellEffect old = this.getSpellEffect(number);
      if (old != null) {
         old.delete();
         this.spellEffects.remove(number);
      }

      return old;
   }

   public void destroy() {
      SpellEffect[] effects = this.getEffects();

      for(int x = 0; x < effects.length; ++x) {
         effects[x].delete();
      }

      this.spellEffects.clear();
   }

   public void clear() {
      this.spellEffects.clear();
   }

   public static ItemSpellEffects getSpellEffects(long itemid) {
      return itemSpellEffects.get(new Long(itemid));
   }

   public static void loadSpellEffectsForItems() {
      long start = System.nanoTime();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM SPELLEFFECTS");
         rs = ps.executeQuery();

         int numEffects;
         for(numEffects = 0; rs.next(); ++numEffects) {
            SpellEffect sp = new SpellEffect(
               rs.getLong("WURMID"), rs.getLong("ITEMID"), rs.getByte("TYPE"), rs.getFloat("POWER"), rs.getInt("TIMELEFT"), (byte)9, (byte)0
            );
            Long id = new Long(sp.owner);
            ItemSpellEffects eff = itemSpellEffects.get(id);
            if (eff == null) {
               eff = new ItemSpellEffects(sp.owner);
            }

            eff.addSpellEffect(sp);
         }

         logger.log(Level.INFO, "Loaded " + numEffects + " Spell Effects For Items, that took " + (float)(System.nanoTime() - start) / 1000000.0F + " ms");
      } catch (SQLException var12) {
         logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }
}
