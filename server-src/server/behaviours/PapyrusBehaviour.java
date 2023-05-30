package com.wurmonline.server.behaviours;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmHarvestables;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.RecipesByPlayer;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.CookBookQuestion;
import com.wurmonline.server.questions.GMSelectHarvestable;
import com.wurmonline.server.questions.ShowHarvestableInfo;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.questions.TextInputQuestion;
import com.wurmonline.server.questions.WriteRecipeQuestion;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.shared.util.MaterialUtilities;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class PapyrusBehaviour extends ItemBehaviour {
   private static final Logger logger = Logger.getLogger(PapyrusBehaviour.class.getName());

   PapyrusBehaviour() {
      super((short)44);
   }

   List<ActionEntry> getPapyrusBehaviours(Creature performer, @Nullable Item source, Item target) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (source != null) {
         if (!source.isContainerLiquid() && source.getTemplateId() != 14 && source.getTemplateId() != 272) {
            if (source.getTemplateId() == 176 && performer.getPower() > 2 && target.getInscription() == null) {
               toReturn.add(new ActionEntry((short)742, "Write recipe", "writing recipe"));
               toReturn.add(new ActionEntry((short)853, "Record harvestable info", "Recording info"));
            }
         } else {
            if (target.getInscription() == null) {
               toReturn.add(Actions.actionEntrys[505]);
            }

            if (target.getInscription() == null) {
               Player player = (Player)performer;
               Recipe recipe = player.getViewingRecipe();
               if (recipe != null) {
                  toReturn.add(new ActionEntry((short)742, "Write recipe \"" + recipe.getName() + "\"", "writing recipe"));
               }

               if (player.getStudied() > 0) {
                  WurmHarvestables.Harvestable harvestable = WurmHarvestables.getHarvestable(player.getStudied());
                  if (harvestable != null) {
                     toReturn.add(new ActionEntry((short)853, "Record " + harvestable.getName() + " info", "Recording info"));
                  }
               }
            }
         }
      }

      if (target.canHaveInscription()) {
         InscriptionData ins = target.getInscription();
         if (ins != null && ins.hasBeenInscribed()) {
            if (target.getAuxData() == 0) {
               toReturn.add(Actions.actionEntrys[506]);
            } else if (target.getAuxData() == 1) {
               toReturn.add(Actions.actionEntrys[743]);
            } else if (target.getAuxData() > 8) {
               WurmHarvestables.Harvestable harvestable = WurmHarvestables.getHarvestable(target.getAuxData() - 8);
               if (harvestable != null) {
                  toReturn.add(new ActionEntry((short)17, "Read " + harvestable.getName() + " info", "reading"));
               }
            }
         } else if (source != null && source.getTemplateId() == 1254 && target.getAuxData() == 0) {
            toReturn.add(new ActionEntry((short)118, "Wax", "waxing"));
         }
      }

      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
      toReturn.addAll(this.getPapyrusBehaviours(performer, null, target));
      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
      toReturn.addAll(this.getPapyrusBehaviours(performer, source, target));
      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      return this.performPapyrusAction(act, performer, null, target, action, counter);
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      return this.performPapyrusAction(act, performer, source, target, action, counter);
   }

   boolean performPapyrusAction(Action act, Creature performer, @Nullable Item source, Item target, short action, float counter) {
      InscriptionData inscriptionData = target.getInscription();
      if (action == 505) {
         if (source == null) {
            performer.getCommunicator().sendNormalServerMessage("You fumble with the " + target.getName() + " but you cannot figure out how it works.");
            return true;
         } else if (!target.canHaveInscription()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot inscribe on that!");
            return true;
         } else if (inscriptionData != null && (performer.getPower() < 2 || target.getAuxData() != 0) && inscriptionData.hasBeenInscribed()) {
            performer.getCommunicator()
               .sendNormalServerMessage("This " + target.getName() + " has already been used by " + inscriptionData.getInscriber() + ".");
            return true;
         } else if (target.getAuxData() == 2) {
            performer.getCommunicator().sendNormalServerMessage("This " + target.getName() + " has a waxy surface and therefore cannot be used to write on.");
            return true;
         } else if (source.getTemplateId() == 14) {
            performer.getCommunicator().sendNormalServerMessage("You push your hand against the " + target.getName() + " in hopes of producing an imprint.");
            Server.getInstance()
               .broadCastAction(performer.getName() + " presses a hand against the " + target.getName() + " in hopes of producing an imprint.", performer, 5);
            target.setInscription("The dirty hand of " + performer.getName() + " vaguely appears on the " + target.getName() + ".", performer.getName());
            return true;
         } else if (source.getTemplateId() == 272) {
            performer.getCommunicator()
               .sendNormalServerMessage(
                  "You push the bloody face of " + source.getName() + " against the " + target.getName() + " in hopes of producing an imprint."
               );
            Server.getInstance()
               .broadCastAction(
                  performer.getName()
                     + " pushes the bloody face of "
                     + source.getName()
                     + " against the "
                     + target.getName()
                     + " in hopes of producing an imprint.",
                  performer,
                  5
               );
            target.setInscription(
               "The smeared bloody faceprint of " + source.getName() + " vaguely appears on the " + target.getName() + ".", performer.getName()
            );
            return true;
         } else if (!source.isContainerLiquid()) {
            performer.getCommunicator().sendNormalServerMessage("You have no container with liquid active.");
            return true;
         } else {
            Item liquid = null;
            Item contained = null;

            for(Item var30 : source.getItems()) {
               if (!isValidColorant(var30.getTemplateId())) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You cannot inscribe with " + var30.getName() + ". You need to use something special for writing on " + target.getName() + "."
                     );
                  return true;
               }

               liquid = var30;
            }

            if (liquid == null) {
               performer.getCommunicator()
                  .sendNormalServerMessage("You need a colorant to be able to inscribe with the " + source.getName() + " on the " + target.getName() + ".");
               return true;
            } else {
               short numChars = 0;
               float inkQlBonus = liquid.getQualityLevel();
               float targetQlBonus = target.getQualityLevel();
               switch(source.getTemplateId()) {
                  case 5:
                     numChars = (short)((int)(4.0F + source.getQualityLevel() + inkQlBonus));
                     numChars = (short)(numChars + target.getRarity() * 10);
                     break;
                  case 76:
                     numChars = (short)((int)(2.0 + 0.5 * (double)source.getQualityLevel() + 0.5 * (double)inkQlBonus + 0.5 * (double)targetQlBonus));
                     numChars = (short)(numChars + target.getRarity() * 10);
                     break;
                  case 78:
                     numChars = (short)((int)(4.0 + 0.5 * (double)source.getQualityLevel() + 0.5 * (double)inkQlBonus + 0.5 * (double)targetQlBonus));
                     numChars = (short)(numChars + target.getRarity() * 10);
                     break;
                  case 653:
                     numChars = (short)((int)(4.0 + 0.5 * (double)source.getQualityLevel() + 0.5 * (double)inkQlBonus + 0.5 * (double)targetQlBonus));
                     numChars = (short)(numChars + target.getRarity() * 10);
                     break;
                  case 749:
                     numChars = (short)(numChars + getNumCharBonusForInk(liquid.getTemplateId()));
                     numChars = (short)(numChars + (short)((int)(2.0F * source.getQualityLevel() + 2.0F * inkQlBonus)));
                     numChars = (short)(numChars + source.getRarity() * 40);
                     numChars = (short)((int)((float)numChars + targetQlBonus * 2.0F));
                     break;
                  default:
                     numChars = 0;
               }

               if (numChars <= 0) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You would only make a mess with the "
                           + source.getName()
                           + " on the "
                           + target.getName()
                           + ". Try to use a more delicate tool when inscribing."
                     );
                  return true;
               } else {
                  numChars = (short)Math.min(500, numChars / 2);
                  TextInputQuestion tiq = new TextInputQuestion(
                     performer,
                     "Inscribing a message on " + target.getName() + ".",
                     "Inscribing is an irreversible process. Enter your important message here:",
                     2,
                     target.getWurmId(),
                     numChars,
                     false
                  );
                  Server.getInstance()
                     .broadCastAction(
                        performer.getName() + " starts to inscribe with " + source.getName() + " on " + target.getNameWithGenus() + ".", performer, 5
                     );
                  tiq.setLiquid(liquid);
                  tiq.sendQuestion();
                  return true;
               }
            }
         }
      } else if (action == 506) {
         if (inscriptionData != null) {
            SimplePopup pp = new SimplePopup(performer, target.getName(), inscriptionData);
            performer.getCommunicator().sendNormalServerMessage("You read the " + target.getName() + ".");
            pp.sendQuestion("Close");
         } else {
            performer.getCommunicator().sendNormalServerMessage("There was no inscription to read.");
         }

         return true;
      } else if (action != 83 && action != 180) {
         if (action == 185) {
            if (performer.getPower() >= 0) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "It is made from " + MaterialUtilities.getMaterialString(target.getMaterial()) + " (" + target.getMaterial() + ") " + "."
                  );
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "WurmId:"
                        + target.getWurmId()
                        + ", posx="
                        + target.getPosX()
                        + "("
                        + ((int)target.getPosX() >> 2)
                        + "), posy="
                        + target.getPosY()
                        + "("
                        + ((int)target.getPosY() >> 2)
                        + "), posz="
                        + target.getPosZ()
                        + ", rot"
                        + target.getRotation()
                        + " layer="
                        + (target.isOnSurface() ? 0 : -1)
                  );
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "Ql:"
                        + target.getQualityLevel()
                        + ", damage="
                        + target.getDamage()
                        + ", weight="
                        + target.getWeightGrams()
                        + ", temp="
                        + target.getTemperature()
                  );
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "parentid="
                        + target.getParentId()
                        + " ownerid="
                        + target.getOwnerId()
                        + " zoneid="
                        + target.getZoneId()
                        + " sizex="
                        + target.getSizeX()
                        + ", sizey="
                        + target.getSizeY()
                        + " sizez="
                        + target.getSizeZ()
                        + "."
                  );
               if (inscriptionData != null) {
                  performer.getCommunicator().sendNormalServerMessage("Inscription: '" + inscriptionData.getInscription() + "'");
                  performer.getCommunicator().sendNormalServerMessage("Inscriber: '" + inscriptionData.getInscriber() + "'");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("Inscription: NO INSCRIPTION!");
                  if (target.getAuxData() == 2) {
                     performer.getCommunicator().sendNormalServerMessage("This " + target.getName() + " has a waxy surface.");
                     return true;
                  }
               }

               long timeSince = WurmCalendar.currentTime - target.getLastMaintained();
               String timeString = Server.getTimeFor(timeSince * 1000L);
               performer.getCommunicator().sendNormalServerMessage("Last maintained " + timeString + " ago.");
               String lastOwnerS = String.valueOf(target.lastOwner);
               PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(target.getLastOwnerId());
               if (p != null) {
                  lastOwnerS = p.getName();
               } else {
                  try {
                     Creature c = Creatures.getInstance().getCreature(target.lastOwner);
                     lastOwnerS = c.getName();
                  } catch (NoSuchCreatureException var20) {
                     lastOwnerS = "dead " + lastOwnerS;
                  }
               }

               performer.getCommunicator().sendNormalServerMessage("lastownerid=" + lastOwnerS + ", Model=" + target.getModelName());
               if (performer.getPower() >= 5) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "Zoneid="
                           + target.getZoneId()
                           + " real zid="
                           + target.zoneId
                           + " Counter="
                           + WurmId.getNumber(target.getWurmId())
                           + " origin="
                           + WurmId.getOrigin(target.getWurmId())
                     );
               }

               if (target.hasData()) {
                  performer.getCommunicator()
                     .sendNormalServerMessage("data=" + target.getData() + ", data1=" + target.getData1() + " data2=" + target.getData2());
               }

               String creator = ", creator=" + target.creator;
               if (target.creator == null || target.creator.length() == 0) {
                  creator = "";
               }

               performer.getCommunicator().sendNormalServerMessage("auxdata=" + target.getAuxData() + creator);
               if (target.isKey()) {
                  performer.getCommunicator().sendNormalServerMessage("lock id=" + target.getLockId());
               }

               if (target.isLock()) {
                  long[] keys = target.getKeyIds();
                  performer.getCommunicator().sendNormalServerMessage("Keys:");

                  for(long lKey : keys) {
                     performer.getCommunicator().sendNormalServerMessage(String.valueOf(lKey));
                  }
               }
            }

            return true;
         } else if (action == 1) {
            StringBuilder sendString = new StringBuilder(target.examine(performer));
            if (inscriptionData != null && inscriptionData.hasBeenInscribed()) {
               if (target.getAuxData() > 8) {
                  sendString.append(" It appears to be a " + target.getName() + ", which should contain its harvest times.");
               } else {
                  sendString.append(" It appears the " + target.getName() + " has something written on it.");
               }

               if (target.getQualityLevel() > 20.0F && inscriptionData.getInscriber() != null) {
                  sendString.append(" The author appears to be someone named '" + inscriptionData.getInscriber() + "'.");
               }
            } else {
               sendString.append(" It is possible to inscribe on the " + target.getName() + " if you know how.");
            }

            String s = target.getSignature();
            if (s != null && s.length() > 0) {
               sendString.append("You can barely make out the signature of its maker,  '" + s + "'.");
            }

            performer.getCommunicator().sendNormalServerMessage(sendString.toString());
            return true;
         } else if (action == 742) {
            return this.writeRecipe(act, performer, source, target, action, counter, inscriptionData);
         } else if (action == 853) {
            return this.recordInfo(act, performer, source, target, action, counter, inscriptionData);
         } else if (action == 743) {
            if (inscriptionData != null && target.getAuxData() == 1) {
               Recipe recipe = inscriptionData.getRecipe();
               if (recipe != null) {
                  performer.getCommunicator().sendNormalServerMessage("You read the " + target.getName() + ".");
                  CookBookQuestion pp = new CookBookQuestion(performer, (byte)13, recipe, true, target.getWurmId(), inscriptionData.getInscriber());
                  pp.sendQuestion();
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You could not decipher the recipe.");
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("There was no recipe to read.");
            }

            return true;
         } else if (action != 17) {
            if (action != 118 || source.getTemplateId() != 1254 || !target.canHaveInscription()) {
               return source == null ? super.action(act, performer, target, action, counter) : super.action(act, performer, source, target, action, counter);
            } else if (inscriptionData != null && inscriptionData.hasBeenInscribed()) {
               performer.getCommunicator()
                  .sendNormalServerMessage("This " + target.getName() + " has already been used by " + inscriptionData.getInscriber() + ".");
               return true;
            } else if (target.getAuxData() == 2) {
               performer.getCommunicator().sendNormalServerMessage("This " + target.getName() + " has a waxy surface and does not need more wax.");
               return true;
            } else if (inscriptionData == null && target.getAuxData() == 0) {
               if (counter == 1.0F) {
                  int time = 50;
                  performer.getCommunicator().sendNormalServerMessage("You start to wax the " + target.getActualName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to wax something.", performer, 5);
                  performer.sendActionControl("Waxing " + target.getActualName(), true, 50);
                  performer.getStatus().modifyStamina(-400.0F);
                  act.setTimeLeft(50);
               }

               if (counter * 10.0F > (float)act.getTimeLeft()) {
                  performer.getCommunicator().sendNormalServerMessage("You finish waxing the " + target.getActualName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " stops waxing.", performer, 5);
                  target.setAuxData((byte)2);
                  target.updateName();
                  source.setWeight(source.getWeightGrams() - 10, true);
                  return true;
               } else {
                  return false;
               }
            } else {
               return true;
            }
         } else {
            if (inscriptionData != null && target.getAuxData() > 8) {
               WurmHarvestables.Harvestable harvestable = WurmHarvestables.getHarvestable(target.getAuxData() - 8);
               if (harvestable != null) {
                  performer.getCommunicator().sendNormalServerMessage("You read the " + target.getName() + ".");
                  ShowHarvestableInfo pp = new ShowHarvestableInfo(performer, target, harvestable);
                  pp.sendQuestion();
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You could not decipher the report.");
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("There was no report to read.");
            }

            return true;
         }
      } else {
         return !performer.mayDestroy(target) && performer.getPower() <= 0
            ? true
            : MethodsItems.destroyItem(action, performer, source, target, false, counter);
      }
   }

   private boolean writeRecipe(
      Action act, Creature performer, @Nullable Item source, Item target, short action, float counter, InscriptionData inscriptionData
   ) {
      if (source == null) {
         performer.getCommunicator().sendNormalServerMessage("You fumble with the " + target.getName() + " but you cannot figure out how it works.");
         return true;
      } else if (!target.canHaveInscription()) {
         performer.getCommunicator().sendNormalServerMessage("You cannot write a recipe on that!");
         return true;
      } else if (inscriptionData != null && inscriptionData.hasBeenInscribed()) {
         performer.getCommunicator().sendNormalServerMessage("This " + target.getName() + " has already been used by " + inscriptionData.getInscriber() + ".");
         return true;
      } else if (target.getAuxData() == 2) {
         performer.getCommunicator().sendNormalServerMessage("This " + target.getName() + " has a waxy surface and therefore cannot be used to write on.");
         return true;
      } else if (target.getAuxData() > 8) {
         performer.getCommunicator().sendNormalServerMessage("This " + target.getName() + " has some study results on and therefore cannot be used again.");
         return true;
      } else if (source.getTemplateId() == 176) {
         WriteRecipeQuestion pp = new WriteRecipeQuestion(performer, target);
         pp.sendQuestion();
         return true;
      } else if (!source.isContainerLiquid()) {
         performer.getCommunicator().sendNormalServerMessage("You have no container with liquid active.");
         return true;
      } else {
         Item liquid = null;
         Item contained = null;

         for(Item var13 : source.getItems()) {
            if (!isValidColorant(var13.getTemplateId())) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "You cannot write a recipe with " + var13.getName() + ". You need to use something special for writing on " + target.getName() + "."
                  );
               return true;
            }

            liquid = var13;
         }

         if (liquid == null) {
            performer.getCommunicator()
               .sendNormalServerMessage("You need a colorant to be able to write a recipe with the " + source.getName() + " on the " + target.getName() + ".");
            return true;
         } else {
            Recipe recipe = ((Player)performer).getViewingRecipe();
            if (recipe == null) {
               performer.getCommunicator().sendNormalServerMessage("You need to be viewing a recipe to be able to write it.");
               return true;
            } else if (recipe.isKnown()) {
               performer.getCommunicator().sendNormalServerMessage("Everyone knows that recipe, seems a waste of time to write it down.");
               return true;
            } else if (recipe.isLootable()) {
               performer.getCommunicator().sendNormalServerMessage("You cannot bring yourself to write that recipe down.");
               return true;
            } else {
               if (counter == 1.0F) {
                  ((Player)performer).setIsWritingRecipe(true);
                  int time = 50;
                  performer.getCommunicator()
                     .sendNormalServerMessage("You start to write the recipe for \"" + recipe.getName() + "\" on the " + target.getActualName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to write a recipe down.", performer, 5);
                  performer.sendActionControl("Writing " + recipe.getName(), true, 50);
                  performer.getStatus().modifyStamina(-400.0F);
                  act.setTimeLeft(50);
               }

               if (counter * 10.0F > (float)act.getTimeLeft()) {
                  target.setInscription(recipe, performer.getName(), liquid.getColor());
                  performer.getCommunicator().sendNormalServerMessage("You carefully finish writing the recipe \"" + recipe.getName() + "\" and sign it.");
                  Server.getInstance().broadCastAction(performer.getName() + " stops writing.", performer, 5);
                  if (liquid != null) {
                     liquid.setWeight(liquid.getWeightGrams() - 10, true);
                  }

                  ((Player)performer).setIsWritingRecipe(false);
                  return true;
               } else {
                  return false;
               }
            }
         }
      }
   }

   private boolean recordInfo(Action act, Creature performer, @Nullable Item source, Item target, short action, float counter, InscriptionData inscriptionData) {
      Player player = (Player)performer;
      if (source == null) {
         performer.getCommunicator().sendNormalServerMessage("You fumble with the " + target.getName() + " but you cannot figure out how it works.");
         return true;
      } else if (!target.canHaveInscription()) {
         performer.getCommunicator().sendNormalServerMessage("You cannot write a recipe on that!");
         return true;
      } else if (inscriptionData != null && inscriptionData.hasBeenInscribed()) {
         performer.getCommunicator().sendNormalServerMessage("This " + target.getName() + " has already been used by " + inscriptionData.getInscriber() + ".");
         return true;
      } else if (target.getAuxData() == 2) {
         performer.getCommunicator().sendNormalServerMessage("This " + target.getName() + " has a waxy surface and therefore cannot be used to write on.");
         return true;
      } else if (target.getAuxData() > 8) {
         performer.getCommunicator()
            .sendNormalServerMessage("This " + target.getName() + " already has some study results on and therefore cannot be used again.");
         return true;
      } else {
         int studied = player.getStudied();
         if (studied == 0) {
            if (source.getTemplateId() == 176) {
               GMSelectHarvestable pp = new GMSelectHarvestable(performer, target);
               pp.sendQuestion();
               return true;
            } else {
               performer.getCommunicator().sendNormalServerMessage("You need to have just studied something to be able to record the results.");
               return true;
            }
         } else if (!source.isContainerLiquid()) {
            performer.getCommunicator().sendNormalServerMessage("You have no container with liquid active.");
            return true;
         } else {
            Item liquid = null;
            Item contained = null;

            for(Item var22 : source.getItems()) {
               if (!isValidColorant(var22.getTemplateId())) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You cannot write a study report with "
                           + var22.getName()
                           + ". You need to use something special for writing on "
                           + target.getName()
                           + "."
                     );
                  return true;
               }

               liquid = var22;
            }

            if (liquid == null) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "You need a colorant to be able to write a recipe with the " + source.getName() + " on the " + target.getName() + "."
                  );
               return true;
            } else {
               WurmHarvestables.Harvestable harvestable = WurmHarvestables.getHarvestable(player.getStudied());
               if (harvestable == null) {
                  performer.getCommunicator().sendNormalServerMessage("You need to have studied something to be able to write a report about it.");
                  return true;
               } else {
                  if (counter == 1.0F) {
                     int time = 50;
                     performer.getCommunicator()
                        .sendNormalServerMessage("You start to write a report for \"" + harvestable.getName() + "\" on the " + target.getActualName() + ".");
                     Server.getInstance().broadCastAction(performer.getName() + " starts to write a report.", performer, 5);
                     performer.sendActionControl("Writing " + harvestable.getName(), true, 50);
                     performer.getStatus().modifyStamina(-400.0F);
                     act.setTimeLeft(50);
                  }

                  if (counter * 10.0F > (float)act.getTimeLeft()) {
                     Skill forestry = performer.getSkills().getSkillOrLearn(10048);
                     float skillMultiplier = Math.min(Math.max(1.0F, counter / 3.0F), 20.0F);
                     float alc = 0.0F;
                     if (performer.isPlayer()) {
                        alc = ((Player)performer).getAlcohol();
                     }

                     int diff = harvestable.getReportDifficulty();
                     float power = (float)forestry.skillCheck((double)((float)diff + alc), target, 0.0, false, skillMultiplier);
                     float knowledge = (float)forestry.getKnowledge(0.0);
                     float modify = (100.0F - knowledge) * power / 1000.0F - alc / 10.0F;
                     float newQL = Math.min(100.0F, Math.max(1.0F, knowledge + modify));
                     performer.getCommunicator()
                        .sendNormalServerMessage("You carefully finish writing the report about \"" + harvestable.getName() + "\" and sign it.");
                     Server.getInstance().broadCastAction(performer.getName() + " stops writing.", performer, 5);
                     player.setStudied(0);
                     target.setAuxData((byte)(studied + 8));
                     target.setOriginalQualityLevel(newQL);
                     target.setQualityLevel(newQL);
                     target.setDamage(0.0F);
                     target.setInscription(harvestable.getName() + " report", player.getName(), liquid.getColor());
                     target.setName(harvestable.getName() + " report", true);
                     if (liquid != null) {
                        liquid.setWeight(liquid.getWeightGrams() - 10, true);
                     }

                     return true;
                  } else {
                     return false;
                  }
               }
            }
         }
      }
   }

   private static boolean isValidColorant(int templateId) {
      switch(templateId) {
         case 431:
         case 432:
         case 433:
         case 434:
         case 435:
         case 438:
         case 753:
            return true;
         default:
            return false;
      }
   }

   private static short getNumCharBonusForInk(int templateId) {
      switch(templateId) {
         case 431:
            return 200;
         case 432:
         case 433:
         case 434:
         case 435:
            return 20;
         case 438:
            return 20;
         case 753:
            return 200;
         default:
            return 0;
      }
   }

   static List<ActionEntry> getPapyrusBehavioursFor(Creature performer, Item source) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (source.canHaveInscription() && source.getParentId() != -10L) {
         InscriptionData inscriptionData = source.getInscription();
         if (inscriptionData != null && source.getAuxData() == 1) {
            Recipe recipe = inscriptionData.getRecipe();
            if (recipe != null) {
               toReturn.add(new ActionEntry((short)744, "Add \"" + recipe.getName() + "\" to cookbook", "adding recipe"));
            }
         }
      }

      return toReturn;
   }

   static boolean addToCookbook(Action act, Creature performer, Item source, Item target, short action, float counter) {
      InscriptionData inscriptionData = source.getInscription();
      if (inscriptionData != null && source.getAuxData() == 1) {
         Recipe recipe = inscriptionData.getRecipe();
         if (recipe != null) {
            if (RecipesByPlayer.isKnownRecipe(performer.getWurmId(), recipe.getRecipeId())) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "As you look for a place to add the recipe in your cookbook, you notice that recipe already exists in your cookbook, therefore decide to stop!"
                  );
               return true;
            }

            if (counter == 1.0F) {
               int time = 50;
               performer.getCommunicator().sendNormalServerMessage("You start to add the recipe " + source.getName() + " into your cookbook.");
               Server.getInstance().broadCastAction(performer.getName() + " starts to write a recipe down.", performer, 5);
               performer.sendActionControl("Writing " + recipe.getName(), true, 50);
               performer.getStatus().modifyStamina(-400.0F);
               act.setTimeLeft(50);
            }

            if (counter * 10.0F > (float)act.getTimeLeft()) {
               if (RecipesByPlayer.addRecipe(performer, recipe)) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You finish adding the " + source.getName() + " into your cookbook, just in time, as the recipe has decayed away."
                     );
                  Server.getInstance().broadCastAction(performer.getName() + " stops writing.", performer, 5);
                  Items.destroyItem(source.getWurmId());
               }

               return true;
            }

            return false;
         }
      }

      performer.getCommunicator().sendNormalServerMessage("That " + source.getName() + " does not have a recipe on it");
      return true;
   }
}
