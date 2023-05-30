package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerJournal;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.shared.util.StringUtilities;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestQuestion extends Question implements TimeConstants {
   private static final Logger logger = Logger.getLogger(TestQuestion.class.getName());
   private static final ConcurrentHashMap<Long, Long> armourCreators = new ConcurrentHashMap<>();

   public TestQuestion(Creature aResponder, long aTarget) {
      super(aResponder, "Testing", "What do you want to do?", 96, aTarget);
   }

   public boolean checkIfMayCreateArmour() {
      if (this.getResponder().getPower() > 0) {
         return true;
      } else {
         Long last = armourCreators.get(this.getResponder().getWurmId());
         if (last != null && System.currentTimeMillis() - last < 300000L) {
            return false;
         } else {
            last = new Long(System.currentTimeMillis());
            armourCreators.put(this.getResponder().getWurmId(), last);
            return true;
         }
      }
   }

   @Override
   public void answer(Properties aAnswers) {
      if (Servers.localServer.testServer) {
         this.getResponder().getBody().healFully();
         this.getResponder().getStatus().modifyStamina2(100.0F);
         String priestTypeString = aAnswers.getProperty("priestType");
         String faithLevelString = aAnswers.getProperty("faithLevel");
         if (priestTypeString != null) {
            int priestType = Integer.parseInt(priestTypeString);
            switch(priestType) {
               case 0:
                  break;
               case 1:
                  if (this.getResponder().getDeity() != null) {
                     try {
                        this.getResponder().setFaith(0.0F);
                        this.getResponder().setFavor(0.0F);
                        this.getResponder().setDeity(null);
                        this.getResponder().getCommunicator().sendNormalServerMessage("You follow no deity.");
                     } catch (IOException var26) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Could not remove deity.");
                     }
                  }
                  break;
               default:
                  int count = 2;

                  for(Deity d : Deities.getDeities()) {
                     if (count == priestType) {
                        try {
                           this.getResponder().setDeity(d);
                           this.getResponder().getCommunicator().sendNormalServerMessage("You are now a follower of " + d.getName() + ".");
                        } catch (IOException var25) {
                           this.getResponder().getCommunicator().sendNormalServerMessage("Could not set deity.");
                        }
                     }

                     ++count;
                  }
            }
         }

         if (faithLevelString != null) {
            int faithLevel = Integer.parseInt(faithLevelString);
            if (faithLevel > 0) {
               faithLevel = Math.min(100, faithLevel);
               if (this.getResponder().getDeity() != null) {
                  try {
                     this.getResponder().getCommunicator().sendNormalServerMessage("Faith set to " + faithLevel + ".");
                     if (faithLevel >= 30 && !this.getResponder().isPriest()) {
                        this.getResponder().setPriest(true);
                        this.getResponder()
                           .getCommunicator()
                           .sendNormalServerMessage("You are now a priest of " + this.getResponder().getDeity().getName() + ".");
                        if (this.getResponder().isPlayer()) {
                           PlayerJournal.sendTierUnlock((Player)this.getResponder(), PlayerJournal.getAllTiers().get((byte)10));
                        }
                     } else if (faithLevel < 30 && this.getResponder().isPriest()) {
                        this.getResponder().setPriest(false);
                        this.getResponder()
                           .getCommunicator()
                           .sendNormalServerMessage("You are no longer a priest of " + this.getResponder().getDeity().getName() + ".");
                     }

                     this.getResponder().setFaith((float)faithLevel);
                  } catch (IOException var32) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("Could not set faith.");
                  }
               }
            }
         }

         String skillLevel = aAnswers.getProperty("skillLevel");
         if (skillLevel != null) {
            try {
               double slevel = Double.parseDouble(skillLevel);
               slevel = Math.min(slevel, 90.0);
               if (slevel > 0.0) {
                  Skills s = this.getResponder().getSkills();
                  if (s != null) {
                     Skill[] skills = s.getSkills();

                     for(Skill sk : skills) {
                        if (sk.getType() != 0 && sk.getType() != 1) {
                           sk.setKnowledge(slevel, false);
                        }
                     }
                  }
               }
            } catch (Exception var31) {
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine("skill bug?");
               }
            }
         }

         String alignLevel = aAnswers.getProperty("alignmentLevel");
         if (alignLevel != null) {
            try {
               float alignment = Float.parseFloat(alignLevel);
               if (alignment != 0.0F) {
                  if (alignment > 99.0F) {
                     alignment = 99.0F;
                  }

                  if (alignment < -99.0F) {
                     alignment = -99.0F;
                  }

                  this.getResponder().setAlignment(alignment);
               }
            } catch (Exception var30) {
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine("alignment update issue");
               }
            }
         }

         String charLevel = aAnswers.getProperty("characteristicsLevel");
         if (charLevel != null) {
            try {
               double slevel = Double.parseDouble(charLevel);
               slevel = Math.min(slevel, 90.0);
               if (slevel > 0.0) {
                  Skills s = this.getResponder().getSkills();
                  if (s != null) {
                     Skill[] skills = s.getSkills();

                     for(Skill sk : skills) {
                        if (sk.getType() == 0 || sk.getType() == 1) {
                           sk.setKnowledge(slevel, false);
                        }
                     }
                  }
               }
            } catch (Exception var29) {
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine("skill bug?");
               }
            }
         }

         String itemtype = aAnswers.getProperty("itemtype");
         if (itemtype != null) {
            String quantity = aAnswers.getProperty("quantity");
            int qty = 0;

            try {
               qty = Integer.parseInt(quantity);
            } catch (NumberFormatException var24) {
               qty = 0;
            }

            if (qty < 0) {
               qty = 0;
            }

            String materialType = aAnswers.getProperty("materialtype");
            byte matType = -1;

            try {
               matType = Byte.parseByte(materialType);
            } catch (NumberFormatException var23) {
               matType = -1;
            }

            if (matType < 0) {
               matType = -1;
            }

            if (matType > 0) {
               if (--matType < MethodsItems.getAllNormalWoodTypes().length) {
                  matType = MethodsItems.getAllNormalWoodTypes()[matType];
               } else {
                  matType = (byte)(matType - MethodsItems.getAllNormalWoodTypes().length);
                  if (matType > MethodsItems.getAllMetalTypes().length) {
                     matType = -1;
                  } else {
                     matType = MethodsItems.getAllMetalTypes()[matType];
                  }
               }
            } else {
               matType = -1;
            }

            String qualityLevel = aAnswers.getProperty("qualitylevel");
            if (qualityLevel != null) {
               try {
                  int ql = Integer.parseInt(qualityLevel);
                  if (ql > 0) {
                     ql = Math.min(ql, 90);

                     try {
                        int num = Integer.parseInt(itemtype);
                        if (num == 0) {
                           return;
                        }

                        if (--num <= 6 && !this.checkIfMayCreateArmour()) {
                           this.getResponder()
                              .getCommunicator()
                              .sendNormalServerMessage("You may only create items every 5 minutes in order to save the database.");
                        }

                        switch(num) {
                           case 0:
                              createAndInsertItems(this.getResponder(), 109, 114, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 109, 109, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 114, 114, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 111, 111, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 779, 779, (float)ql, false, matType);
                              break;
                           case 1:
                              createAndInsertItems(this.getResponder(), 103, 108, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 103, 103, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 105, 105, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 106, 106, (float)ql, false, matType);
                              break;
                           case 2:
                              createAndInsertItems(this.getResponder(), 115, 120, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 119, 119, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 116, 116, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 115, 115, (float)ql, false, matType);
                              break;
                           case 3:
                              createAndInsertItems(this.getResponder(), 274, 279, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 278, 278, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 274, 274, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 277, 277, (float)ql, false, matType);
                              break;
                           case 4:
                              createAndInsertItems(this.getResponder(), 280, 287, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 284, 284, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 280, 280, (float)ql, false, matType);
                              createAndInsertItems(this.getResponder(), 283, 283, (float)ql, false, matType);
                              break;
                           case 5:
                              int drakeColor = this.getRandomDragonColor();
                              createAndInsertItems(this.getResponder(), 468, 473, (float)ql, drakeColor, false);
                              createAndInsertItems(this.getResponder(), 472, 472, (float)ql, drakeColor, false);
                              createAndInsertItems(this.getResponder(), 469, 469, (float)ql, drakeColor, false);
                              createAndInsertItems(this.getResponder(), 468, 468, (float)ql, drakeColor, false);
                              break;
                           case 6:
                              int scaleColor = this.getRandomDragonColor();
                              createAndInsertItems(this.getResponder(), 474, 478, (float)ql, scaleColor, false);
                              createAndInsertItems(this.getResponder(), 478, 478, (float)ql, scaleColor, false);
                              createAndInsertItems(this.getResponder(), 474, 474, (float)ql, scaleColor, false);
                              createAndInsertItems(this.getResponder(), 477, 477, (float)ql, scaleColor, false);
                              break;
                           case 7:
                              createAndInsertItems(this.getResponder(), 80, 80, (float)ql, false, matType);
                              break;
                           case 8:
                              createAndInsertItems(this.getResponder(), 21, 21, (float)ql, false, matType);
                              break;
                           case 9:
                              createAndInsertItems(this.getResponder(), 81, 81, (float)ql, false, matType);
                              break;
                           case 10:
                              createAndInsertItems(this.getResponder(), 291, 291, (float)ql, false, matType);
                              break;
                           case 11:
                              createAndInsertItems(this.getResponder(), 292, 292, (float)ql, false, matType);
                              break;
                           case 12:
                              createAndInsertItems(this.getResponder(), 290, 290, (float)ql, false, matType);
                              break;
                           case 13:
                              createAndInsertItems(this.getResponder(), 3, 3, (float)ql, false, matType);
                              break;
                           case 14:
                              createAndInsertItems(this.getResponder(), 90, 90, (float)ql, false, matType);
                              break;
                           case 15:
                              createAndInsertItems(this.getResponder(), 87, 87, (float)ql, false, matType);
                              break;
                           case 16:
                              createAndInsertItems(this.getResponder(), 706, 706, (float)ql, false, matType);
                              break;
                           case 17:
                              createAndInsertItems(this.getResponder(), 705, 705, (float)ql, false, matType);
                              break;
                           case 18:
                              createAndInsertItems(this.getResponder(), 707, 707, (float)ql, false, matType);
                              break;
                           case 19:
                              createAndInsertItems(this.getResponder(), 86, 86, (float)ql, false, matType);
                              break;
                           case 20:
                              createAndInsertItems(this.getResponder(), 4, 4, (float)ql, false, matType);
                              break;
                           case 21:
                              createAndInsertItems(this.getResponder(), 85, 85, (float)ql, false, matType);
                              break;
                           case 22:
                              createAndInsertItems(this.getResponder(), 82, 82, (float)ql, false, matType);
                              break;
                           case 23:
                              this.createMultiple(this.getResponder(), 25, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 20, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 24, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 480, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 8, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 143, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 7, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 62, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 63, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 493, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 97, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 313, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 296, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 388, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 421, 1, (float)ql, matType);
                              break;
                           case 24:
                              ItemTemplate[] itemtemps = ItemTemplateFactory.getInstance().getTemplates();

                              for(ItemTemplate temp : itemtemps) {
                                 if (temp.isCombine()
                                    && !temp.isFood()
                                    && temp.getTemplateId() != 683
                                    && temp.getTemplateId() != 737
                                    && (temp.getDecayTime() == 86401L || temp.getDecayTime() == 28800L || temp.destroyOnDecay)
                                    && !temp.getModelName().startsWith("model.resource.scrap.")) {
                                    for(int x = 0; x < 5; ++x) {
                                       createAndInsertItems(
                                          this.getResponder(),
                                          temp.getTemplateId(),
                                          temp.getTemplateId(),
                                          (float)(1 + Server.rand.nextInt(ql)),
                                          0,
                                          true,
                                          false,
                                          (byte)-1
                                       );
                                    }
                                 }
                              }
                              break;
                           case 25:
                              this.createOnGround(this.getResponder(), 132, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 26:
                              this.createOnGround(this.getResponder(), 492, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 27:
                              this.createOnGround(this.getResponder(), 146, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 28:
                              this.createOnGround(this.getResponder(), 860, qty == 0 ? 4 : qty, (float)ql, matType);
                              break;
                           case 29:
                              this.createOnGround(this.getResponder(), 188, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 30:
                              this.createOnGround(this.getResponder(), 217, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 31:
                              this.createOnGround(this.getResponder(), 218, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 32:
                              this.createOnGround(this.getResponder(), 22, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 33:
                              this.createOnGround(this.getResponder(), 23, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 34:
                              this.createOnGround(this.getResponder(), 9, qty == 0 ? 4 : qty, (float)ql, matType);
                              break;
                           case 35:
                              this.createOnGround(this.getResponder(), 557, qty == 0 ? 4 : qty, (float)ql, matType);
                              break;
                           case 36:
                              this.createOnGround(this.getResponder(), 558, qty == 0 ? 4 : qty, (float)ql, matType);
                              break;
                           case 37:
                              this.createOnGround(this.getResponder(), 559, qty == 0 ? 4 : qty, (float)ql, matType);
                              break;
                           case 38:
                              this.createOnGround(this.getResponder(), 319, qty == 0 ? 4 : qty, (float)ql, matType);
                              break;
                           case 39:
                              this.createOnGround(this.getResponder(), 786, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 40:
                              this.createOnGround(this.getResponder(), 785, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 41:
                              this.createOnGround(this.getResponder(), 26, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 42:
                              this.createOnGround(this.getResponder(), 130, qty == 0 ? 10 : qty, (float)ql, matType);
                              break;
                           case 43:
                              this.createMultiple(this.getResponder(), 903, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 901, 1, (float)ql, matType);
                              break;
                           case 44:
                              this.createMultiple(this.getResponder(), 711, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 213, 4, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 439, 8, (float)ql, matType);
                              break;
                           case 45:
                              this.createMultiple(this.getResponder(), 221, 5, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 223, 5, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 480, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 23, 3, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 64, 1, (float)ql, matType);
                              break;
                           case 46:
                              if (matType != 8 || matType != 7) {
                                 matType = 8;
                              }

                              this.createMultiple(this.getResponder(), 505, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 507, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 508, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 506, 1, (float)ql, matType);
                              break;
                           case 47:
                              this.createMultiple(this.getResponder(), 376, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 374, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 380, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 382, 1, (float)ql, matType);
                              this.createMultiple(this.getResponder(), 378, 1, (float)ql, matType);
                        }
                     } catch (NumberFormatException var27) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Error: input was " + itemtype + " - failed to parse.");
                     }
                  } else {
                     this.getResponder().getCommunicator().sendNormalServerMessage("No quality level selected so not creating.");
                  }
               } catch (NumberFormatException var28) {
                  this.getResponder().getCommunicator().sendNormalServerMessage("Error: input was " + itemtype + " - failed to parse.");
               }
            }
         }
      }
   }

   private void createOnGround(Creature receiver, int itemTemplate, int howMany, float qualityLevel, byte materialType) {
      for(int x = 0; x < howMany; ++x) {
         createAndInsertItems(receiver, itemTemplate, itemTemplate, qualityLevel, false, materialType);
      }
   }

   private void createMultiple(Creature receiver, int itemTemplate, int howMany, float qualityLevel, byte materialType) {
      for(int x = 0; x < howMany; ++x) {
         createAndInsertItems(receiver, itemTemplate, itemTemplate, qualityLevel, false, materialType);
      }
   }

   public static final void createAndInsertItems(Creature receiver, int itemStart, int itemEnd, float qualityLevel, boolean newbieItem, byte materialType) {
      createAndInsertItems(receiver, itemStart, itemEnd, qualityLevel, 0, false, newbieItem, materialType);
   }

   public static final void createAndInsertItems(Creature receiver, int itemStart, int itemEnd, float qualityLevel, int color, boolean newbieItem) {
      createAndInsertItems(receiver, itemStart, itemEnd, qualityLevel, color, false, newbieItem, (byte)-1);
   }

   private static final void createAndInsertItems(
      Creature receiver, int itemStart, int itemEnd, float qualityLevel, int color, boolean onGround, boolean newbieItem, byte material
   ) {
      if (itemStart > itemEnd) {
         receiver.getCommunicator().sendNormalServerMessage("Error: Bugged test case.");
      } else {
         for(int x = itemStart; x <= itemEnd; ++x) {
            if (x != 110) {
               if (onGround) {
                  try {
                     ItemFactory.createItem(
                        x,
                        qualityLevel,
                        receiver.getPosX(),
                        receiver.getPosY(),
                        Server.rand.nextFloat() * 180.0F,
                        receiver.isOnSurface(),
                        (byte)0,
                        -10L,
                        receiver.getName()
                     );
                  } catch (Exception var11) {
                     receiver.getCommunicator().sendAlertServerMessage(var11.getMessage());
                  }
               } else {
                  try {
                     Item i = ItemFactory.createItem(x, qualityLevel, receiver.getName());
                     if (newbieItem) {
                        i.setAuxData((byte)1);
                     }

                     if (i.isGem()) {
                        i.setData1(qualityLevel <= 0.0F ? 0 : (int)(qualityLevel * 2.0F));
                        i.setDescription("v");
                     }

                     if (i.isDragonArmour()) {
                        i.setMaterial((byte)16);
                        i.setColor(color);
                        String dName = i.getDragonColorNameByColor(color);
                        if (dName != "") {
                           i.setName(dName + " " + i.getName());
                        }
                     }

                     if (material != -1) {
                        i.setMaterial(material);
                     }

                     receiver.getInventory().insertItem(i);
                  } catch (Exception var12) {
                     receiver.getCommunicator().sendAlertServerMessage(var12.getMessage());
                  }
               }
            }
         }

         receiver.wearItems();
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      buf.append("text{text='Create an armour set or a weapon and set skills:'}");
      buf.append("harray{label{text='Item'}dropdown{id='itemtype';options=\"");
      buf.append("Nothing,");
      buf.append("Cloth,");
      buf.append("Leather,");
      buf.append("Studded,");
      buf.append("Chain,");
      buf.append("Plate,");
      buf.append("Drake (random color),");
      buf.append("Dragon Scale (random color),");
      buf.append("Shortsword,");
      buf.append("Longsword,");
      buf.append("Twohanded sword,");
      buf.append("Small maul,");
      buf.append("Med maul,");
      buf.append("Large maul,");
      buf.append("Small axe,");
      buf.append("Large axe,");
      buf.append("Twohanded axe,");
      buf.append("Halberd,");
      buf.append("Long spear,");
      buf.append("Steel spear,");
      buf.append("Large Metal Shield,");
      buf.append("Medium Metal Shield,");
      buf.append("Large Wooden Shield,");
      buf.append("Small Wooden Shield,");
      buf.append("Basic Tools,");
      buf.append("Raw materials,");
      buf.append("#10 Stone Bricks,");
      buf.append("#10 Mortar,");
      buf.append("#10 Rock Shards,");
      buf.append("#4 Wood Beams,");
      buf.append("#10 Iron Ribbons,");
      buf.append("#10 Large Nails,");
      buf.append("#10 Small Nails,");
      buf.append("#10 Planks,");
      buf.append("#10 Shafts,");
      buf.append("#4 Logs,");
      buf.append("#4 Thick Ropes,");
      buf.append("#4 Mooring Ropes,");
      buf.append("#4 Cordage Ropes,");
      buf.append("#4 Normal Ropes,");
      buf.append("#10 Marble Bricks,");
      buf.append("#10 Marble Shards,");
      buf.append("#10 Dirt,");
      buf.append("#10 Clay,");
      buf.append("Bridge Tools,");
      buf.append("Make your own RangePole,");
      buf.append("Make your own Dioptra,");
      buf.append("Statuette Set,");
      buf.append("Vesseled Gems Set,");
      buf.append("\";default=\"0\"}}");
      buf.append("text{text='Select material:'}");
      buf.append("harray{label{text='Material'}dropdown{id='materialtype';options=\"");
      buf.append("Standard.,");

      for(byte material : MethodsItems.getAllNormalWoodTypes()) {
         buf.append(StringUtilities.raiseFirstLetter(Item.getMaterialString(material)) + ",");
      }

      for(byte material : MethodsItems.getAllMetalTypes()) {
         buf.append(StringUtilities.raiseFirstLetter(Item.getMaterialString(material)) + ",");
      }

      buf.append("\";default=\"0\"}}");
      buf.append("harray{label{text='Item qualitylevel (Max 90)'};input{maxchars='2'; id='qualitylevel'; text='50'}}");
      buf.append("harray{label{text='Set skills to (Max 90, 0=no change)'};input{maxchars='2'; id='skillLevel'; text='0'}}");
      buf.append("harray{label{text='Set characteristics to (Max 90, 0=no change)'};input{maxchars='2'; id='characteristicsLevel'; text='0'}}");
      buf.append("harray{label{text='Set Alignment to (Max 99, Min -99, 0=no change)'};input{maxchars='3'; id='alignmentLevel'; text='0'}}");
      buf.append("harray{label{text='Item quantity (0..99, 0 = use default)'};input{maxchars='2'; id='quantity'; text='0'}}");
      buf.append("text{text='Quantity is only used for items with a # before their name, if 0 then the default number after the # is used.'};");
      buf.append("text{text='Set Deity:'}");
      buf.append("harray{label{text='Deity'}dropdown{id='priestType';options=\"");
      buf.append("No Change,");
      buf.append("No Deity,");

      for(Deity d : Deities.getDeities()) {
         buf.append(d.getName() + ",");
      }

      buf.append("\";default=\"0\"}}");
      buf.append("harray{label{text='Faith (Max 100, 0=no change)'};input{maxchars='3'; id='faithLevel'; text='0'}}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   final int getRandomDragonColor() {
      int c = Server.rand.nextInt(5);
      switch(c) {
         case 0:
            return WurmColor.createColor(215, 40, 40);
         case 1:
            return WurmColor.createColor(10, 10, 10);
         case 2:
            return WurmColor.createColor(10, 210, 10);
         case 3:
            return WurmColor.createColor(255, 255, 255);
         case 4:
            return WurmColor.createColor(40, 40, 215);
         default:
            return WurmColor.createColor(100, 100, 100);
      }
   }
}
