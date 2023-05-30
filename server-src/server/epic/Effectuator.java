package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.TerraformingTask;
import com.wurmonline.server.bodys.BodyHuman;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.meshgen.IslandAdder;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Effectuator implements CreatureTemplateIds, MiscConstants {
   private static final Random rand = new Random();
   public static final int EFFECT_NONE = 0;
   public static final int EFFECT_SPEED = 1;
   public static final int EFFECT_COMBATRATING = 2;
   public static final int EFFECT_STAMINA_REGAIN = 3;
   public static final int EFFECT_FAVORGAIN = 4;
   public static final int EFFECT_SPAWN = 5;
   private static final String LOAD_KINGDOM_EFFECTS = "SELECT * FROM KINGDOMEFFECTS";
   private static final String INSERT_KINGDOM_EFFECTS = "INSERT INTO KINGDOMEFFECTS (EFFECT,KINGDOM) VALUES(?,?)";
   private static final String UPDATE_KINGDOM_EFFECTS = "UPDATE KINGDOMEFFECTS SET KINGDOM=? WHERE EFFECT=?";
   private static final String LOAD_DEITY_EFFECTS = "SELECT * FROM DEITYEFFECTS";
   private static final String INSERT_DEITY_EFFECTS = "INSERT INTO DEITYEFFECTS (EFFECT,DEITY) VALUES(?,?)";
   private static final String UPDATE_DEITY_EFFECTS = "UPDATE DEITYEFFECTS SET DEITY=? WHERE EFFECT=?";
   private static int kingdomTemplateWithSpeedBonus = 0;
   private static int kingdomTemplateWithCombatRating = 0;
   private static int kingdomTemplateWithStaminaRegain = 0;
   private static int kingdomTemplateWithFavorGain = 0;
   private static int deityWithSpeedBonus = 0;
   private static int deityWithCombatRating = 0;
   private static int deityWithStaminaRegain = 0;
   private static int deityWithFavorGain = 0;
   private static final LinkedBlockingQueue<SynchedEpicEffect> comingEvents = new LinkedBlockingQueue<>();
   private static final Logger logger = Logger.getLogger(Effectuator.class.getName());

   private Effectuator() {
   }

   public static String getSpiritType(int effect) {
      String toReturn;
      switch(effect) {
         case 1:
            toReturn = "fire";
            break;
         case 2:
            toReturn = "forest";
            break;
         case 3:
            toReturn = "mountain";
            break;
         case 4:
            toReturn = "water";
            break;
         default:
            toReturn = "hidden";
      }

      return toReturn;
   }

   public static final void addEpicEffect(SynchedEpicEffect effect) {
      comingEvents.add(effect);
   }

   public static final void pollEpicEffects() {
      for(SynchedEpicEffect effect : comingEvents) {
         effect.run();
      }

      comingEvents.clear();
   }

   public static void doEvent(int eventNum, long deityNumber, int creatureTemplateId, int bonusEffectNum, String eventDesc) {
      if (Servers.localServer.EPIC && !Servers.localServer.LOGINSERVER) {
         setEffectController(4, 0L);
         setEffectController(2, 0L);
         setEffectController(1, 0L);
         setEffectController(3, 0L);
         byte favoredKingdom = Deities.getFavoredKingdom((int)deityNumber);
         boolean doNegative = false;
         switch(rand.nextInt(7)) {
            case 0:
               spawnDefenders(deityNumber, creatureTemplateId);
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
         }
      }
   }

   static void doEvent1(long deityNumber) {
      if (Servers.localServer.EPIC) {
         if (deityNumber == 5L) {
            wurmPunish(4000, 0.0F, 20.0F, (byte)6);
         }

         spawnOwnCreatures(deityNumber, 38, true);
      }
   }

   static void doEvent5(long deityNumber) {
      if (Servers.localServer.EPIC && deityNumber == 5L) {
         wurmPunish(4000, 20.0F, 0.0F, (byte)5);
      }
   }

   static void doEvent7(long deityNumber) {
      if (Servers.localServer.EPIC) {
         if (deityNumber == 5L) {
            crushStructures();
         } else {
            IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
            isl.addOneIsland(Zones.worldTileSizeX, Zones.worldTileSizeY);
         }
      }
   }

   static void doEvent8(long deityNumber) {
      if (Servers.localServer.EPIC) {
         if (deityNumber == 5L) {
            wurmPunish(8000, 0.0F, 0.0F, (byte)9);
         } else {
            doEvent15(deityNumber);
         }
      }
   }

   static void terraform(int task, long deityNumber, int nums) {
      byte favoredKingdom = Deities.getFavoredKingdom((int)deityNumber);
      Deity d = Deities.getDeity((int)deityNumber);
      if (d != null) {
         new TerraformingTask(task, favoredKingdom, d.getName(), (int)deityNumber, nums, true);
      }
   }

   static void doEvent12(long deityNumber) {
      if (Servers.localServer.EPIC) {
         disease(deityNumber);
      }
   }

   static void doEvent14(long deityNumber) {
      if (Servers.localServer.EPIC) {
         slay(deityNumber);
      }
   }

   static void doEvent15(long deityNumber) {
      awardSkill(deityNumber, 103, 0.005F, 20.0F);
   }

   static void doEvent17(long deityNumber) {
      if (Servers.localServer.EPIC) {
         if (deityNumber == 5L) {
            wurmPunish(4000, 0.0F, 0.0F, (byte)9);
         } else {
            awardSkill(deityNumber, 105, 0.005F, 20.0F);
         }
      }
   }

   static void appointAlly(long deityNumber) {
      if (Servers.localServer.EPIC && deityNumber == 5L) {
         wurmPunish(14000, 20.0F, 20.0F, (byte)9);
      }
   }

   static final void promoteImmortal(long deityNumber) {
      if (Servers.localServer.LOGINSERVER && !HexMap.VALREI.elevateDemigod(deityNumber)) {
      }
   }

   static void doEvent20(long deityNumber) {
      punishSkill(deityNumber, 100, 0.5F);
      punishSkill(deityNumber, 102, 0.5F);
      punishSkill(deityNumber, 106, 0.5F);
      punishSkill(deityNumber, 104, 0.5F);
      punishSkill(deityNumber, 101, 0.5F);
      punishSkill(deityNumber, 105, 0.5F);
      lowerFaith(deityNumber);

      for(int x = 0; x < Math.min(20, Players.getInstance().getNumberOfPlayers()); ++x) {
         slay(deityNumber);
      }
   }

   static void doEvent21(long deityNumber) {
      punishSkill(deityNumber, 100, 0.04F);
      punishSkill(deityNumber, 102, 0.04F);
      punishSkill(deityNumber, 106, 0.04F);
      punishSkill(deityNumber, 104, 0.04F);
      punishSkill(deityNumber, 101, 0.04F);
      punishSkill(deityNumber, 105, 0.04F);
      awardSkill(deityNumber, 100, 0.005F, 20.0F);
      awardSkill(deityNumber, 102, 0.005F, 20.0F);
      awardSkill(deityNumber, 106, 0.005F, 20.0F);
      awardSkill(deityNumber, 104, 0.005F, 20.0F);
      awardSkill(deityNumber, 101, 0.005F, 20.0F);
      awardSkill(deityNumber, 105, 0.005F, 20.0F);
      lowerFaith(deityNumber);

      for(int x = 0; x < Math.min(10, Players.getInstance().getNumberOfPlayers()); ++x) {
         slay(deityNumber);
      }
   }

   static void doEvent22(long deityNumber) {
      punishSkill(deityNumber, 100, 0.05F);
      punishSkill(deityNumber, 102, 0.05F);
      punishSkill(deityNumber, 106, 0.05F);
      punishSkill(deityNumber, 105, 0.05F);
      awardSkill(deityNumber, 100, 0.005F, 20.0F);
      awardSkill(deityNumber, 102, 0.005F, 20.0F);
      awardSkill(deityNumber, 106, 0.005F, 20.0F);
      awardSkill(deityNumber, 104, 0.005F, 20.0F);
      awardSkill(deityNumber, 101, 0.005F, 20.0F);
      awardSkill(deityNumber, 105, 0.005F, 20.0F);
      lowerFaith(deityNumber);

      for(int x = 0; x < Math.min(10, Players.getInstance().getNumberOfPlayers()); ++x) {
         slay(deityNumber);
      }
   }

   static void doEvent23(long deityNumber) {
      punishSkill(deityNumber, 100, 0.05F);
      punishSkill(deityNumber, 102, 0.05F);
      punishSkill(deityNumber, 101, 0.05F);
      punishSkill(deityNumber, 105, 0.05F);
      awardSkill(deityNumber, 100, 0.005F, 20.0F);
      awardSkill(deityNumber, 102, 0.005F, 20.0F);
      awardSkill(deityNumber, 106, 0.005F, 20.0F);
      awardSkill(deityNumber, 104, 0.005F, 20.0F);
      awardSkill(deityNumber, 101, 0.005F, 20.0F);
      awardSkill(deityNumber, 105, 0.005F, 20.0F);
      lowerFaith(deityNumber);

      for(int x = 0; x < Math.min(10, Players.getInstance().getNumberOfPlayers()); ++x) {
         slay(deityNumber);
      }
   }

   static void doEvent24(long deityNumber) {
      punishSkill(deityNumber, 103, 0.05F);
      punishSkill(deityNumber, 102, 0.05F);
      punishSkill(deityNumber, 101, 0.05F);
      punishSkill(deityNumber, 105, 0.05F);
      awardSkill(deityNumber, 100, 0.005F, 20.0F);
      awardSkill(deityNumber, 102, 0.005F, 20.0F);
      awardSkill(deityNumber, 106, 0.005F, 20.0F);
      awardSkill(deityNumber, 104, 0.005F, 20.0F);
      awardSkill(deityNumber, 101, 0.005F, 20.0F);
      awardSkill(deityNumber, 105, 0.005F, 20.0F);
      lowerFaith(deityNumber);

      for(int x = 0; x < Math.min(10, Players.getInstance().getNumberOfPlayers()); ++x) {
         slay(deityNumber);
      }
   }

   static void doEvent25(long deityNumber) {
      punishSkill(deityNumber, 103, 0.05F);
      punishSkill(deityNumber, 102, 0.05F);
      punishSkill(deityNumber, 101, 0.05F);
      punishSkill(deityNumber, 105, 0.05F);
      awardSkill(deityNumber, 100, 0.005F, 20.0F);
      awardSkill(deityNumber, 102, 0.005F, 20.0F);
      awardSkill(deityNumber, 106, 0.005F, 20.0F);
      awardSkill(deityNumber, 104, 0.005F, 20.0F);
      awardSkill(deityNumber, 101, 0.005F, 20.0F);
      awardSkill(deityNumber, 105, 0.005F, 20.0F);
      lowerFaith(deityNumber);

      for(int x = 0; x < Math.min(20, Players.getInstance().getNumberOfPlayers()); ++x) {
         slay(deityNumber);
      }
   }

   private static void punishSkill(long deityNum, int skillNum, float toDecrease) {
   }

   private static void disease(long deityNumberSaved) {
      byte friendlyKingdom = Deities.getFavoredKingdom((int)deityNumberSaved);
      Player[] players = Players.getInstance().getPlayers();

      for(int x = 0; x < players.length; ++x) {
         if ((friendlyKingdom == 0 || players[x].getKingdomTemplateId() != friendlyKingdom)
            && (players[x].getDeity() == null || (long)players[x].getDeity().getNumber() != deityNumberSaved)) {
            players[x].getCommunicator().sendAlertServerMessage("An evil aura emanates from valrei. You suddenly feel like vomiting.");
            players[x].setDisease((byte)50);
         }
      }
   }

   private static void awardSkill(long deityNum, int skillNum, float toIncrease, float minNumber) {
      byte friendlyKingdom = Deities.getFavoredKingdom((int)deityNum);
      Player[] players = Players.getInstance().getPlayers();

      for(int x = 0; x < players.length; ++x) {
         if (players[x].getKingdomTemplateId() == friendlyKingdom) {
            try {
               Skill old = players[x].getSkills().getSkill(skillNum);
               old.setKnowledge(old.getKnowledge() + (100.0 - old.getKnowledge()) * (double)toIncrease, false);
            } catch (NoSuchSkillException var9) {
               players[x].getSkills().learn(skillNum, minNumber);
            }
         }
      }
   }

   private static void slay(long deityNum) {
      Player[] players = Players.getInstance().getPlayers();
      if (deityNum == 5L) {
         boolean found = false;

         while(!found) {
            int p = rand.nextInt(players.length);
            if (!players[p].isDead() && players[p].isFullyLoaded() && players[p].getVisionArea() != null) {
               players[p].getCommunicator().sendAlertServerMessage("You feel an abnormal wave of heat coming from Valrei! Wurm has punished you!");
               players[p].die(false, "Valrei Lazer Beams");
               found = true;
            }

            if (!found && players.length < 5 && rand.nextBoolean()) {
               return;
            }
         }
      } else {
         boolean found = false;
         int seeks = 0;
         byte friendlyKingdom = Deities.getFavoredKingdom((int)deityNum);

         while(!found) {
            ++seeks;
            int p = rand.nextInt(players.length);
            if (!players[p].isDead()
               && players[p].isFullyLoaded()
               && players[p].getVisionArea() != null
               && players[p].getKingdomTemplateId() != friendlyKingdom) {
               if (players[p].getDeity() == null || (long)players[p].getDeity().getNumber() == deityNum) {
                  players[p].getCommunicator().sendAlertServerMessage("You suddenly feel yourself immolated in an abnormal wave of heat coming from Valrei!");
                  players[p].die(false, "Valrei Nuclear Blast");
                  found = true;
               } else if ((deityNum != 1L || players[p].getDeity().getNumber() != 3) && (deityNum != 3L || players[p].getDeity().getNumber() != 1)) {
                  players[p].getCommunicator().sendAlertServerMessage("You suddenly feel yourself immolated in an abnormal wave of heat coming from Valrei!");
                  players[p].die(false, "Valrei Bombardment");
                  found = true;
               }
            }

            if (!found && seeks > players.length && rand.nextBoolean()) {
               return;
            }
         }
      }
   }

   private static void lowerFaith(long deityNum) {
      PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfos();

      for(int x = 0; x < infos.length; ++x) {
         byte kingdom = Players.getInstance().getKingdomForPlayer(infos[x].wurmId);
         Kingdom k = Kingdoms.getKingdom(kingdom);
         byte kingdomTemplateId = k.getTemplate();
         byte favoredKingdom = Deities.getFavoredKingdom((int)deityNum);
         if (kingdomTemplateId != favoredKingdom) {
            try {
               if (infos[x].getFaith() > 80.0F) {
                  infos[x].setFaith(infos[x].getFaith() - 1.0F);
               } else if (infos[x].getFaith() > 50.0F) {
                  infos[x].setFaith(infos[x].getFaith() - 3.0F);
               } else if (infos[x].getFaith() > 20.0F) {
                  infos[x].setFaith(infos[x].getFaith() * 0.8F);
               }

               infos[x].setFavor(0.0F);
            } catch (IOException var9) {
            }
         }
      }
   }

   private static void wurmPunish(int damage, float poisondam, float disease, byte woundType) {
      Player[] players = Players.getInstance().getPlayers();
      BodyHuman body = new BodyHuman();

      for(int x = 0; x < players.length; ++x) {
         try {
            CombatEngine.addWound(
               null, players[x], woundType, body.getRandomWoundPos(), (double)damage, 1.0F, "hurts", null, disease, poisondam, false, false, false, false
            );
         } catch (Exception var8) {
         }
      }
   }

   private static void crushStructures() {
      Structure[] structures = Structures.getAllStructures();
      if (structures.length > 0) {
         structures[rand.nextInt(structures.length)].totallyDestroy();
      }
   }

   public static void spawnDefenders(long deityId, int creatureTemplateId) {
      if (!Servers.isThisATestServer() && (Servers.localServer.isChallengeOrEpicServer() || Servers.isThisAChaosServer())) {
         byte friendlyKingdom = Deities.getFavoredKingdom((int)deityId);
         Deity deity = Deities.getDeity((int)deityId);

         try {
            CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(creatureTemplateId);
            if (friendlyKingdom != 0) {
               Kingdom k = Kingdoms.getKingdom(friendlyKingdom);
               if (k != null && k.lastConfrontationTileX > 1 && k.lastConfrontationTileY > 1) {
                  for(int a = 0; a < rand.nextInt(7) + 1; ++a) {
                     int tx = Zones.safeTileX(k.lastConfrontationTileX - 5 + rand.nextInt(10));
                     int ty = Zones.safeTileY(k.lastConfrontationTileY - 5 + rand.nextInt(10));
                     spawnCreatureAt(tx, ty, ctemplate, friendlyKingdom);
                  }
               }
            }

            if (deity != null && deity.lastConfrontationTileX > 1 && deity.lastConfrontationTileY > 1) {
               for(int a = 0; a < rand.nextInt(7) + 1; ++a) {
                  int tx = Zones.safeTileX(deity.lastConfrontationTileX - 5 + rand.nextInt(10));
                  int ty = Zones.safeTileY(deity.lastConfrontationTileY - 5 + rand.nextInt(10));
                  spawnCreatureAt(tx, ty, ctemplate, friendlyKingdom);
               }
            }
         } catch (NoSuchCreatureTemplateException var10) {
            logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
         }
      } else {
         logger.log(Level.INFO, "Spawning defenders");
      }
   }

   public static void spawnOwnCreatures(long deityId, int creatureTemplateId, boolean onlyAtHome) {
      byte friendlyKingdom = Deities.getFavoredKingdom((int)deityId);

      try {
         CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(creatureTemplateId);
         int summoned = 0;
         Player[] players = Players.getInstance().getPlayers();
         int maxplayers = players.length / 10;
         int maxSummoned = (int)(200.0F / ctemplate.baseCombatRating);
         if (creatureTemplateId == 75) {
            maxplayers = 2;
            maxSummoned = 5;
         }

         if (!Servers.localServer.isChallengeOrEpicServer() && !Servers.isThisAChaosServer()) {
            return;
         }

         if (players.length > 10) {
            for(int x = 0; x < maxplayers; ++x) {
               int playint = rand.nextInt(players.length);
               if ((players[playint].getPositionZ() > -1.0F || ctemplate.isSwimming() && players[playint].getPositionZ() < -4.0F)
                  && players[playint].getKingdomTemplateId() != friendlyKingdom
                  && !players[playint].isFriendlyKingdom(friendlyKingdom)) {
                  int centerx = players[playint].getTileX();
                  int centery = players[playint].getTileY();

                  for(int a = 0; (float)a < Math.max(1.0F, 30.0F / ctemplate.baseCombatRating); ++a) {
                     int tx = Zones.safeTileX(centerx - 5 + rand.nextInt(10));
                     int ty = Zones.safeTileY(centery - 5 + rand.nextInt(10));
                     VolaTile t = Zones.getOrCreateTile(tx, ty, true);
                     if (t.getStructure() == null && t.getVillage() == null) {
                        spawnCreatureAt(tx, ty, ctemplate, friendlyKingdom);
                        if (++summoned >= maxSummoned) {
                           break;
                        }
                     }
                  }

                  if (summoned >= maxSummoned) {
                     break;
                  }
               }
            }
         }

         if (!Servers.isThisATestServer()) {
            int tries = 0;

            while(summoned < maxSummoned && tries < 5000) {
               ++tries;
               int centerx = rand.nextInt(Zones.worldTileSizeX);
               int centery = rand.nextInt(Zones.worldTileSizeY);
               if (onlyAtHome && Zones.getKingdom(centerx, centery) == friendlyKingdom || Zones.getKingdom(centerx, centery) != friendlyKingdom) {
                  for(int x = 0; x < 10; ++x) {
                     int tx = Zones.safeTileX(centerx - 5 + rand.nextInt(10));
                     int ty = Zones.safeTileY(centery - 5 + rand.nextInt(10));

                     try {
                        float height = Zones.calculateHeight((float)(tx * 4 + 2), (float)(ty * 4 + 2), true);
                        if (height >= 0.0F || ctemplate.isSwimming() && height < -2.0F) {
                           VolaTile t = Zones.getOrCreateTile(tx, ty, true);
                           if (t.getStructure() == null && t.getVillage() == null) {
                              spawnCreatureAt(tx, ty, ctemplate, friendlyKingdom);
                              ++summoned;
                              break;
                           }
                        }
                     } catch (NoSuchZoneException var18) {
                        logger.log(Level.WARNING, var18.getMessage());
                     }
                  }
               }
            }
         } else {
            logger.log(Level.INFO, "Spawning Own creatures");
         }
      } catch (NoSuchCreatureTemplateException var19) {
         logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
      }
   }

   private static void spawnCreatureAt(int tilex, int tiley, CreatureTemplate ctemplate, byte friendlyKingdom) {
      if (ctemplate != null) {
         try {
            byte sex = ctemplate.getSex();
            if (sex == 0 && !ctemplate.keepSex && Server.rand.nextInt(2) == 0) {
               sex = 1;
            }

            byte ctype = 0;
            int switchi = Server.rand.nextInt(40);
            if (switchi == 0) {
               ctype = 99;
            } else if (switchi == 1) {
               ctype = 1;
            } else if (switchi == 2) {
               ctype = 4;
            } else if (switchi == 4) {
               ctype = 11;
            }

            Zones.flash(tilex, tiley, false);
            Creature.doNew(
               ctemplate.getTemplateId(),
               false,
               (float)(tilex * 4 + 2),
               (float)(tiley * 4 + 2),
               rand.nextFloat() * 360.0F,
               0,
               ctemplate.getName(),
               sex,
               friendlyKingdom,
               ctype,
               false
            );
         } catch (Exception var7) {
            logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
         }
      }
   }

   public static void loadEffects() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (Servers.localServer.PVPSERVER && !Servers.localServer.HOMESERVER) {
         try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement("SELECT * FROM KINGDOMEFFECTS");
            rs = ps.executeQuery();

            int found;
            for(found = 0; rs.next(); ++found) {
               int effect = rs.getInt("EFFECT");
               byte kingdomId = rs.getByte("KINGDOM");
               implementEffectControl(effect, kingdomId);
            }

            if (found == 0) {
               createEffects();
            }
         } catch (SQLException var18) {
            logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }
      } else {
         try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement("SELECT * FROM DEITYEFFECTS");
            rs = ps.executeQuery();

            int found;
            for(found = 0; rs.next(); ++found) {
               int effect = rs.getInt("EFFECT");
               int deityId = rs.getByte("DEITY");
               implementDeityEffectControl(effect, deityId);
            }

            if (found == 0) {
               createEffects();
            }
         } catch (SQLException var16) {
            logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   static void createEffects() {
      initializeEffect(3);
      initializeEffect(4);
      initializeEffect(1);
      initializeEffect(2);
   }

   public static int getKingdomTemplateWithSpeedBonus() {
      return kingdomTemplateWithSpeedBonus;
   }

   public static int getKingdomTemplateWithCombatRating() {
      return kingdomTemplateWithCombatRating;
   }

   public static int getKingdomTemplateWithStaminaRegain() {
      return kingdomTemplateWithStaminaRegain;
   }

   public static int getKingdomTemplateWithFavorGain() {
      return kingdomTemplateWithFavorGain;
   }

   private static void removeEffectFromPlayersWithKingdom(int effectId, int deityId) {
      byte kingdomTemplate = Deities.getFavoredKingdom(deityId);
      Player[] players = Players.getInstance().getPlayers();

      for(Player p : players) {
         if (p.getKingdomTemplateId() == kingdomTemplate) {
            p.sendRemoveDeityEffectBonus(effectId);
         }
      }
   }

   private static void addEffectToPlayersWithKingdom(int effectId, int deityId) {
      byte kingdomTemplate = Deities.getFavoredKingdom(deityId);
      Player[] players = Players.getInstance().getPlayers();

      for(Player p : players) {
         if (p.getKingdomTemplateId() == kingdomTemplate) {
            p.sendAddDeityEffectBonus(effectId);
         }
      }
   }

   static void implementEffectControl(int effectId, int kingdomTemplateId) {
      switch(effectId) {
         case 1:
            if (kingdomTemplateWithSpeedBonus != 0 && kingdomTemplateWithSpeedBonus != kingdomTemplateId) {
               removeEffectFromPlayersWithKingdom(effectId, kingdomTemplateId);
            }

            kingdomTemplateWithSpeedBonus = kingdomTemplateId;
            if (kingdomTemplateWithSpeedBonus != 0) {
               addEffectToPlayersWithKingdom(1, kingdomTemplateWithSpeedBonus);
            }
            break;
         case 2:
            if (kingdomTemplateWithCombatRating != 0 && kingdomTemplateWithCombatRating != kingdomTemplateId) {
               removeEffectFromPlayersWithKingdom(effectId, kingdomTemplateId);
            }

            kingdomTemplateWithCombatRating = kingdomTemplateId;
            if (kingdomTemplateWithCombatRating != 0) {
               addEffectToPlayersWithKingdom(2, kingdomTemplateWithCombatRating);
            }
            break;
         case 3:
            if (kingdomTemplateWithStaminaRegain != 0 && kingdomTemplateWithStaminaRegain != kingdomTemplateId) {
               removeEffectFromPlayersWithKingdom(effectId, kingdomTemplateId);
            }

            kingdomTemplateWithStaminaRegain = kingdomTemplateId;
            if (kingdomTemplateWithStaminaRegain != 0) {
               addEffectToPlayersWithKingdom(3, kingdomTemplateWithStaminaRegain);
            }
            break;
         case 4:
            if (kingdomTemplateWithFavorGain != 0 && kingdomTemplateWithFavorGain != kingdomTemplateId) {
               removeEffectFromPlayersWithKingdom(effectId, kingdomTemplateId);
            }

            kingdomTemplateWithFavorGain = kingdomTemplateId;
            if (kingdomTemplateWithFavorGain != 0) {
               addEffectToPlayersWithKingdom(4, kingdomTemplateWithFavorGain);
            }
      }
   }

   static void initializeEffect(int effectId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      if (Servers.localServer.PVPSERVER && !Servers.localServer.HOMESERVER) {
         try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement("INSERT INTO KINGDOMEFFECTS (EFFECT,KINGDOM) VALUES(?,?)");
            ps.setInt(1, effectId);
            ps.setByte(2, (byte)0);
            ps.executeUpdate();
         } catch (SQLException var16) {
            logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      } else {
         try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement("INSERT INTO DEITYEFFECTS (EFFECT,DEITY) VALUES(?,?)");
            ps.setInt(1, effectId);
            ps.setInt(2, 0);
            ps.executeUpdate();
         } catch (SQLException var14) {
            logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public static int getDeityWithSpeedBonus() {
      return deityWithSpeedBonus;
   }

   public static int getDeityWithCombatRating() {
      return deityWithCombatRating;
   }

   public static int getDeityWithStaminaRegain() {
      return deityWithStaminaRegain;
   }

   public static int getDeityWithFavorGain() {
      return deityWithFavorGain;
   }

   private static void removeEffectFromPlayersWithDeity(int effectId, int deityId) {
      Player[] players = Players.getInstance().getPlayers();

      for(Player p : players) {
         if (p.getDeity() != null && p.getDeity().number == deityId) {
            p.sendRemoveDeityEffectBonus(effectId);
         }
      }
   }

   private static void addEffectToPlayersWithDeity(int effectId, int deityId) {
      Player[] players = Players.getInstance().getPlayers();

      for(Player p : players) {
         if (p.getDeity() != null && p.getDeity().number == deityId) {
            p.sendAddDeityEffectBonus(effectId);
         }
      }
   }

   static void implementDeityEffectControl(int effectId, int deityId) {
      if (!Servers.localServer.PVPSERVER || Servers.localServer.HOMESERVER) {
         switch(effectId) {
            case 1:
               if (deityWithSpeedBonus != 0 && deityWithSpeedBonus != deityId) {
                  removeEffectFromPlayersWithDeity(effectId, deityId);
               }

               deityWithSpeedBonus = deityId;
               if (deityWithSpeedBonus != 0) {
                  addEffectToPlayersWithDeity(1, deityWithSpeedBonus);
               }
               break;
            case 2:
               if (deityWithCombatRating != 0 && deityWithCombatRating != deityId) {
                  removeEffectFromPlayersWithDeity(effectId, deityId);
               }

               deityWithCombatRating = deityId;
               if (deityWithCombatRating != 0) {
                  addEffectToPlayersWithDeity(2, deityWithCombatRating);
               }
               break;
            case 3:
               if (deityWithStaminaRegain != 0 && deityWithStaminaRegain != deityId) {
                  removeEffectFromPlayersWithDeity(effectId, deityId);
               }

               deityWithStaminaRegain = deityId;
               if (deityWithStaminaRegain != 0) {
                  addEffectToPlayersWithDeity(3, deityWithStaminaRegain);
               }
               break;
            case 4:
               if (deityWithFavorGain != 0 && deityWithFavorGain != deityId) {
                  removeEffectFromPlayersWithDeity(effectId, deityId);
               }

               deityWithFavorGain = deityId;
               if (deityWithFavorGain != 0) {
                  addEffectToPlayersWithDeity(4, deityWithFavorGain);
               }
         }
      }
   }

   public static void setEffectController(int effectId, long deityId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      if (Servers.localServer.PVPSERVER && !Servers.localServer.HOMESERVER) {
         byte kingdomId = Deities.getFavoredKingdom((int)deityId);

         try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement("UPDATE KINGDOMEFFECTS SET KINGDOM=? WHERE EFFECT=?");
            ps.setByte(1, kingdomId);
            ps.setInt(2, effectId);
            ps.executeUpdate();
         } catch (SQLException var19) {
            logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }

         implementEffectControl(effectId, kingdomId);
      } else {
         try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement("UPDATE DEITYEFFECTS SET DEITY=? WHERE EFFECT=?");
            ps.setLong(1, deityId);
            ps.setInt(2, effectId);
            ps.executeUpdate();
         } catch (SQLException var17) {
            logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }

         implementDeityEffectControl(effectId, (int)deityId);
      }
   }
}
