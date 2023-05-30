package com.wurmonline.server.spells;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SpellEffect implements MiscConstants {
   private static final String DELETE_EFFECT = "DELETE FROM SPELLEFFECTS WHERE WURMID=?";
   private static final String DELETE_EFFECTS_FOR_PLAYER = "DELETE FROM SPELLEFFECTS WHERE OWNER=?";
   private static final String DELETE_EFFECTS_FOR_ITEM = "DELETE FROM SPELLEFFECTS WHERE ITEMID=?";
   private static final String UPDATE_POWER = "UPDATE SPELLEFFECTS SET POWER=? WHERE WURMID=?";
   private static final String UPDATE_TIMELEFT = "UPDATE SPELLEFFECTS SET TIMELEFT=? WHERE WURMID=?";
   private static final String GET_EFFECTS_FOR_PLAYER = "SELECT * FROM SPELLEFFECTS WHERE OWNER=?";
   private static final String CREATE_EFFECT = "INSERT INTO SPELLEFFECTS (WURMID, OWNER,TYPE,POWER,TIMELEFT,EFFTYPE,INFLUENCE) VALUES(?,?,?,?,?,?,?)";
   private static final String CREATE_ITEM_EFFECT = "INSERT INTO SPELLEFFECTS (WURMID, ITEMID,TYPE,POWER,TIMELEFT) VALUES(?,?,?,?,?)";
   private static final Logger logger = Logger.getLogger(SpellEffect.class.getName());
   public final long id;
   public float power = 0.0F;
   public int timeleft = 0;
   public final long owner;
   public final byte type;
   private final boolean isplayer;
   private final boolean isItem;
   private final byte effectType;
   private final byte influence;
   private boolean persistant = true;

   public SpellEffect(long aOwner, byte aType, float aPower, int aTimeleft) {
      this(aOwner, aType, aPower, aTimeleft, (byte)9, (byte)0, true);
   }

   public SpellEffect(long aOwner, byte aType, float aPower, int aTimeleft, byte effType, byte influenceType, boolean persist) {
      this.owner = aOwner;
      this.type = aType;
      this.power = aPower;
      this.timeleft = aTimeleft;
      this.effectType = effType;
      this.influence = influenceType;
      this.persistant = persist;
      this.id = WurmId.getNextSpellId();
      if (WurmId.getType(aOwner) == 0) {
         this.isplayer = true;
         this.isItem = false;
      } else if (WurmId.getType(aOwner) != 2 && WurmId.getType(aOwner) != 19 && WurmId.getType(aOwner) != 20) {
         this.isplayer = false;
         this.isItem = false;
      } else {
         this.isplayer = false;
         this.isItem = true;
      }

      if ((this.isplayer || this.isItem) && this.persistant) {
         this.save();
      }
   }

   public SpellEffect(long aId, long aOwner, byte aType, float aPower, int aTimeleft, byte efftype, byte influ) {
      this.id = aId;
      this.owner = aOwner;
      this.type = aType;
      this.power = aPower;
      this.timeleft = aTimeleft;
      this.effectType = efftype;
      this.influence = influ;
      this.persistant = true;
      if (WurmId.getType(aOwner) == 0) {
         this.isplayer = true;
         this.isItem = false;
      } else if (WurmId.getType(aOwner) == 2) {
         this.isplayer = false;
         this.isItem = true;
      } else {
         this.isplayer = false;
         this.isItem = false;
      }
   }

   public byte getSpellEffectType() {
      return this.effectType;
   }

   public byte getSpellInfluenceType() {
      return this.influence;
   }

   public final boolean isSmeared() {
      return this.type >= 77 && this.type <= 92;
   }

   public String getName() {
      if (this.type == 22 && this.getPower() > 70.0F) {
         return "Thornshell";
      } else if (this.type == 73) {
         return "Newbie agg range buff";
      } else if (this.type == 74) {
         return "Newbie food and drink buff";
      } else if (this.type == 75) {
         return "Newbie healing buff";
      } else if (this.type == 64) {
         return "Hunted";
      } else if (this.type == 72) {
         return "Illusion";
      } else if (this.type == 78) {
         return "potion of the ropemaker";
      } else if (this.type == 79) {
         return "potion of mining";
      } else if (this.type == 77) {
         return "oil of the weapon smith";
      } else if (this.type == 80) {
         return "ointment of tailoring";
      } else if (this.type == 81) {
         return "oil of the armour smith";
      } else if (this.type == 82) {
         return "fletching potion";
      } else if (this.type == 83) {
         return "oil of the blacksmith";
      } else if (this.type == 84) {
         return "potion of leatherworking";
      } else if (this.type == 85) {
         return "potion of shipbuilding";
      } else if (this.type == 86) {
         return "ointment of stonecutting";
      } else if (this.type == 87) {
         return "ointment of masonry";
      } else if (this.type == 88) {
         return "potion of woodcutting";
      } else if (this.type == 89) {
         return "potion of carpentry";
      } else if (this.type == 99) {
         return "potion of butchery";
      } else if (this.type == 94) {
         return "Incineration";
      } else if (this.type == 98) {
         return "Shatter Protection";
      } else {
         return (long)this.type < -10L ? RuneUtilities.getRuneName(this.type) : Spells.getEnchantment(this.type).name;
      }
   }

   public String getLongDesc() {
      if (this.type == 78) {
         return "improves rope making max ql";
      } else if (this.type == 79) {
         return "improves mining max ql";
      } else if (this.type == 77) {
         return "improves weapon smithing max ql";
      } else if (this.type == 80) {
         return "improves tailoring max ql";
      } else if (this.type == 81) {
         return "improves armour smithing max ql";
      } else if (this.type == 82) {
         return "improves fletching max ql";
      } else if (this.type == 83) {
         return "improves blacksmithing max ql";
      } else if (this.type == 84) {
         return "improves leather working max ql";
      } else if (this.type == 85) {
         return "improves ship building max ql";
      } else if (this.type == 86) {
         return "improves stone cutting max ql";
      } else if (this.type == 87) {
         return "improves masonry max ql";
      } else if (this.type == 88) {
         return "improves wood cutting max ql";
      } else if (this.type == 89) {
         return "improves carpentry max ql";
      } else if (this.type == 99) {
         return "improves butchery product max ql";
      } else if (this.type == 98) {
         return "protects against damage when spells are cast upon it";
      } else {
         return (long)this.type < -10L ? "will " + RuneUtilities.getRuneLongDesc(this.type) : Spells.getEnchantment(this.type).effectdesc;
      }
   }

   private void save() {
      if (this.isplayer && this.persistant) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("INSERT INTO SPELLEFFECTS (WURMID, OWNER,TYPE,POWER,TIMELEFT,EFFTYPE,INFLUENCE) VALUES(?,?,?,?,?,?,?)");
            ps.setLong(1, this.id);
            ps.setLong(2, this.owner);
            ps.setByte(3, this.type);
            ps.setFloat(4, this.power);
            ps.setInt(5, this.timeleft);
            ps.setByte(6, this.effectType);
            ps.setByte(7, this.influence);
            ps.executeUpdate();
         } catch (SQLException var16) {
            logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      } else if (this.isItem && this.persistant) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement("INSERT INTO SPELLEFFECTS (WURMID, ITEMID,TYPE,POWER,TIMELEFT) VALUES(?,?,?,?,?)");
            ps.setLong(1, this.id);
            ps.setLong(2, this.owner);
            ps.setByte(3, this.type);
            ps.setFloat(4, this.power);
            ps.setInt(5, this.timeleft);
            ps.executeUpdate();
         } catch (SQLException var14) {
            logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public float getPower() {
      return this.power;
   }

   public void setPower(float newpower) {
      if (this.power != newpower) {
         this.power = newpower;
         if (this.persistant) {
            if (this.isplayer) {
               Connection dbcon = null;
               PreparedStatement ps = null;

               try {
                  dbcon = DbConnector.getPlayerDbCon();
                  ps = dbcon.prepareStatement("UPDATE SPELLEFFECTS SET POWER=? WHERE WURMID=?");
                  ps.setFloat(1, this.power);
                  ps.setLong(2, this.id);
                  ps.executeUpdate();
               } catch (SQLException var17) {
                  logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
               } finally {
                  DbUtilities.closeDatabaseObjects(ps, null);
                  DbConnector.returnConnection(dbcon);
               }
            } else if (this.isItem) {
               Connection dbcon = null;
               PreparedStatement ps = null;

               try {
                  dbcon = DbConnector.getItemDbCon();
                  ps = dbcon.prepareStatement("UPDATE SPELLEFFECTS SET POWER=? WHERE WURMID=?");
                  ps.setFloat(1, this.power);
                  ps.setLong(2, this.id);
                  ps.executeUpdate();
               } catch (SQLException var15) {
                  logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
               } finally {
                  DbUtilities.closeDatabaseObjects(ps, null);
                  DbConnector.returnConnection(dbcon);
               }
            }
         }
      }
   }

   public void improvePower(Creature performer, float newpower) {
      float mod = 5.0F * (1.0F - this.power / (performer.hasFlag(82) ? 105.0F : 100.0F));
      this.setPower(mod + newpower);
   }

   public void setTimeleft(int newTimeleft) {
      if (this.timeleft != newTimeleft) {
         this.timeleft = newTimeleft;
         if (this.isplayer && this.persistant) {
            Connection dbcon = null;
            PreparedStatement ps = null;

            try {
               dbcon = DbConnector.getPlayerDbCon();
               ps = dbcon.prepareStatement("UPDATE SPELLEFFECTS SET TIMELEFT=? WHERE WURMID=?");
               ps.setInt(1, this.timeleft);
               ps.setLong(2, this.id);
               ps.executeUpdate();
            } catch (SQLException var8) {
               logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            } finally {
               DbUtilities.closeDatabaseObjects(ps, null);
               DbConnector.returnConnection(dbcon);
            }
         }
      }
   }

   private void saveTimeleft() {
      if (this.isplayer && this.persistant) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("UPDATE SPELLEFFECTS SET TIMELEFT=? WHERE WURMID=?");
            ps.setInt(1, this.timeleft);
            ps.setLong(2, this.id);
            ps.executeUpdate();
         } catch (SQLException var7) {
            logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public void delete() {
      if (this.persistant) {
         if (this.isplayer) {
            Connection dbcon = null;
            PreparedStatement ps = null;

            try {
               try {
                  dbcon = DbConnector.getPlayerDbCon();
                  ps = dbcon.prepareStatement("DELETE FROM SPELLEFFECTS WHERE WURMID=?");
                  ps.setLong(1, this.id);
                  ps.executeUpdate();
               } catch (SQLException var16) {
                  logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
               }
            } finally {
               DbUtilities.closeDatabaseObjects(ps, null);
               DbConnector.returnConnection(dbcon);
            }
         } else if (this.isItem) {
            Connection dbcon = null;
            PreparedStatement ps = null;

            try {
               try {
                  dbcon = DbConnector.getItemDbCon();
                  ps = dbcon.prepareStatement("DELETE FROM SPELLEFFECTS WHERE WURMID=?");
                  ps.setLong(1, this.id);
                  ps.executeUpdate();
               } catch (SQLException var14) {
                  logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
               }
            } finally {
               DbUtilities.closeDatabaseObjects(ps, null);
               DbConnector.returnConnection(dbcon);
            }
         }
      }
   }

   public boolean poll(SpellEffects effects) {
      --this.timeleft;
      if (this.timeleft <= 0) {
         effects.removeSpellEffect(this);
         return true;
      } else {
         if (this.timeleft % 60 == 0) {
            this.saveTimeleft();
         }

         return false;
      }
   }

   public static final SpellEffect[] loadEffectsForPlayer(long wurmid) {
      SpellEffect[] spells = new SpellEffect[0];
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM SPELLEFFECTS WHERE OWNER=?");
         ps.setLong(1, wurmid);
         rs = ps.executeQuery();
         Set<SpellEffect> spset = new HashSet<>();

         while(rs.next()) {
            SpellEffect sp = new SpellEffect(
               rs.getLong("WURMID"),
               rs.getLong("OWNER"),
               rs.getByte("TYPE"),
               rs.getFloat("POWER"),
               rs.getInt("TIMELEFT"),
               rs.getByte("EFFTYPE"),
               rs.getByte("INFLUENCE")
            );
            spset.add(sp);
         }

         if (spset.size() > 0) {
            spells = spset.toArray(new SpellEffect[spset.size()]);
         }
      } catch (SQLException var11) {
         logger.log(Level.WARNING, wurmid + ": " + var11.getMessage(), (Throwable)var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return spells;
   }

   public static final void deleteEffectsForPlayer(long playerid) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Deleting Effects for Player ID: " + playerid);
         }

         ps = dbcon.prepareStatement("DELETE FROM SPELLEFFECTS WHERE OWNER=?");
         ps.setLong(1, playerid);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Problem deleting effects for playerid: " + playerid + " due to " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final void deleteEffectsForItem(long itemid) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("DELETE FROM SPELLEFFECTS WHERE ITEMID=?");
         ps.setLong(1, itemid);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Problem deleting effects for itemid: " + itemid + " due to " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}
