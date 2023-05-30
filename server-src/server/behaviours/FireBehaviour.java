package com.wurmonline.server.behaviours;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.shared.constants.ItemMaterials;
import java.util.LinkedList;
import java.util.List;

final class FireBehaviour extends ItemBehaviour implements ItemMaterials {
   FireBehaviour() {
      super((short)18);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = new LinkedList<>();

      try {
         toReturn.addAll(super.getBehavioursFor(performer, source, target));
         if (target.getTemplateId() == 1243) {
            if ((source.getTemplateId() == 169 || source.getTemplateId() == 36 || source.isLiquidInflammable() || source.isPaper())
               && !source.isNewbieItem()
               && !source.isChallengeNewbieItem()
               && !source.isIndestructible()) {
               BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
               if (result == null) {
                  toReturn.add(new ActionEntry((short)117, "Fuel", "Fueling"));
               }
            }
         } else if ((source.isWood() || source.isCloth() || source.isMelting() || source.isLiquidInflammable() || source.isPaper())
            && !source.isNewbieItem()
            && !source.isChallengeNewbieItem()
            && !source.isIndestructible()
            && !source.isComponentItem()
            && source.getTemplateId() != 1392
            && performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F)
            && target.getTemplateId() != 74) {
            BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
            if (result == null) {
               toReturn.add(Actions.actionEntrys[117]);
            }
         }
      } catch (Exception var6) {
      }

      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      boolean done = true;
      if (action == 1) {
         int temperature = target.getTemperature();
         StringBuilder sendString = new StringBuilder(target.examine(performer));
         if (target.isPlanted()) {
            PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(target.lastOwner);
            String plantedBy = "someone";
            if (pInfo != null) {
               plantedBy = pInfo.getName();
            }

            sendString.append(" The " + target.getName() + " has been firmly secured to the ground by " + plantedBy + ".");
         }

         String s = target.getSignature();
         if (s != null && !s.isEmpty()) {
            sendString.append(" You can barely make out the signature of its maker,  '" + s + "'.");
         }

         if (target.color != -1) {
            sendString.append(MethodsItems.getColorDesc(target.color));
         }

         if (temperature < 1000) {
            sendString.append(" The fire is not lit.");
         } else if (target.getTemplateId() == 1243) {
            sendString.append(" There are wisps of smoke steadily coming out of the nozzle.");
         } else if (temperature < 2000) {
            sendString.append(" A few red glowing coals can be found under a bed of ashes.");
         } else if (temperature < 3500) {
            sendString.append(" A layer of ashes is starting to form on the glowing coals.");
         } else if (temperature < 4000) {
            sendString.append(" Only a hot, red glowing bed of coal remains of the fire now.");
         } else if (temperature < 5000) {
            sendString.append(" A few flames still dance on the fire but soon they too will die.");
         } else if (temperature < 7000) {
            sendString.append(" The fire is starting to fade.");
         } else if (temperature < 9000) {
            sendString.append(" The fire burns with wild flames and still has much unburnt material.");
         } else {
            sendString.append(" The fire burns steadily and will still burn for a long time.");
         }

         performer.getCommunicator().sendNormalServerMessage(sendString.toString());
         target.sendEnchantmentStrings(performer.getCommunicator());
      } else {
         done = super.action(act, performer, target, action, counter);
      }

      return done;
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      boolean done = false;
      if (action == 117) {
         if (source.getTemplateId() == 1276) {
            performer.getCommunicator()
               .sendNormalServerMessage("You cannot fuel " + target.getNameWithGenus() + " with " + source.getNameWithGenus() + " silly.");
            return true;
         }

         if (source.isMagicContainer()) {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " will not burn.");
            return true;
         }

         if (source.isHollow() && !source.isEmpty(true)) {
            boolean skip = false;

            for(Item item : source.getAllItems(true)) {
               if (item.getTemplateId() != 1392 || !item.isComponentItem()) {
                  skip = true;
               }
            }

            if (skip) {
               performer.getCommunicator()
                  .sendNormalServerMessage("The " + source.getName() + " must contain no items before using it to fuel the " + target.getName() + ".");
               return true;
            }
         }

         if (source.isComponentItem() || source.getTemplateId() == 1392) {
            performer.getCommunicator().sendNormalServerMessage("You cannot fuel " + target.getNameWithGenus() + " with " + source.getNameWithGenus() + ".");
            return true;
         }

         if (target.getTemplateId() == 1243) {
            if (target.getAuxData() > 100) {
               performer.getCommunicator().sendNormalServerMessage("The bee smoker is already full.");
               return true;
            }

            if ((source.getTemplateId() == 169 || source.getTemplateId() == 36 || source.isLiquidInflammable() || source.isPaper())
               && !source.isNewbieItem()
               && !source.isChallengeNewbieItem()
               && !source.isIndestructible()) {
               BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
               if (result == null) {
                  performer.getCommunicator().sendNormalServerMessage("You fuel the " + target.getName() + " with " + source.getNameWithGenus() + ".");
                  Server.getInstance()
                     .broadCastAction(performer.getName() + " fuels the " + target.getName() + " with " + source.getNameWithGenus() + ".", performer, 5);
                  double newTemp = (double)(source.getWeightGrams() * Item.fuelEfficiency(source.getMaterial()));
                  if (target.getTemperature() > 1000) {
                     short maxTemp = 30000;
                     short newPTemp = (short)((int)Math.min(30000.0, (double)target.getTemperature() + newTemp));
                     target.setTemperature(newPTemp);
                  }

                  Items.destroyItem(source.getWurmId());
                  target.setAuxData((byte)((int)Math.min(127.0, (double)target.getAuxData() + newTemp / 100.0)));
               }
            }
         } else if ((source.isWood() || source.isCloth() || source.isMelting() || source.isLiquidInflammable() || source.isPaper())
            && !source.isNewbieItem()
            && !source.isChallengeNewbieItem()
            && !source.isIndestructible()) {
            BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
            if (result == null && target.getTemplateId() != 74) {
               if (target.getTemperature() <= 1000) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is not burning.");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You fuel the " + target.getName() + " with " + source.getNameWithGenus() + ".");
                  Server.getInstance()
                     .broadCastAction(performer.getName() + " fuels the " + target.getName() + " with " + source.getNameWithGenus() + ".", performer, 5);
                  short maxTemp = 30000;
                  double newTemp = (double)(source.getWeightGrams() * Item.fuelEfficiency(source.getMaterial()));
                  target.setTemperature((short)((int)Math.min(30000.0, (double)target.getTemperature() + newTemp)));

                  for(Item item : source.getAllItems(true)) {
                     if (item.isComponentItem() || item.getTemplateId() == 1392) {
                        Items.destroyItem(item.getWurmId());
                     }
                  }

                  Items.destroyItem(source.getWurmId());
                  if (target.getTemplateId() == 889) {
                     target.setAuxData((byte)((int)Math.min(127.0, (double)target.getAuxData() + newTemp / 1000.0)));
                  }
               }
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot burn the " + target.getName() + ".");
         }

         done = true;
      } else {
         if (action == 1) {
            return this.action(act, performer, target, action, counter);
         }

         if (action == 12) {
            if (source.getTemplateId() == 176 && performer.getPower() >= 2) {
               done = MethodsItems.setFire(performer, target);
            } else {
               done = MethodsItems.startFire(performer, source, target, counter);
            }
         } else {
            done = super.action(act, performer, source, target, action, counter);
         }
      }

      return done;
   }
}
