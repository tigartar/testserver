package com.wurmonline.server.behaviours;

import com.wurmonline.server.Features;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.WagonerDeliveriesQuestion;
import com.wurmonline.server.questions.WagonerSetupDeliveryQuestion;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class WagonerContainerBehaviour extends ItemBehaviour {
   private static final Logger logger = Logger.getLogger(WagonerContainerBehaviour.class.getName());

   WagonerContainerBehaviour() {
      super((short)61);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
      toReturn.addAll(this.getBehavioursForWagonerContainer(performer, null, target));
      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
      toReturn.addAll(this.getBehavioursForWagonerContainer(performer, source, target));
      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      boolean[] ans = this.wagonerContainerActions(act, performer, null, target, action, counter);
      return ans[0] ? ans[1] : super.action(act, performer, target, action, counter);
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      boolean[] ans = this.wagonerContainerActions(act, performer, source, target, action, counter);
      return ans[0] ? ans[1] : super.action(act, performer, source, target, action, counter);
   }

   private List<ActionEntry> getBehavioursForWagonerContainer(Creature performer, @Nullable Item source, Item container) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (Features.Feature.WAGONER.isEnabled()) {
         if (container.isPlanted() && !container.isSealedByPlayer() && !container.isEmpty(false)) {
            toReturn.add(Actions.actionEntrys[915]);
         }

         if (container.isSealedByPlayer()) {
            Delivery delivery = Delivery.canViewDelivery(container, performer);
            if (delivery != null) {
               toReturn.add(Actions.actionEntrys[918]);
            }

            if (Delivery.canUnSealContainer(container, performer)) {
               toReturn.add(Actions.actionEntrys[740]);
            }
         }
      }

      return toReturn;
   }

   public boolean[] wagonerContainerActions(Action act, Creature performer, @Nullable Item source, Item container, short action, float counter) {
      if (Features.Feature.WAGONER.isEnabled()) {
         if (action == 915 && container.isPlanted() && !container.isSealedByPlayer() && !container.isEmpty(false)) {
            WagonerSetupDeliveryQuestion wsdq = new WagonerSetupDeliveryQuestion(performer, container);
            wsdq.sendQuestion();
            return new boolean[]{true, true};
         }

         Delivery delivery = Delivery.canViewDelivery(container, performer);
         if (delivery != null && action == 918 && container.isSealedByPlayer()) {
            WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(performer, delivery.getDeliveryId(), false);
            wdq.sendQuestion2();
            return new boolean[]{true, true};
         }

         if (Delivery.canUnSealContainer(container, performer)) {
            container.setIsSealedByPlayer(false);
         }
      }

      return new boolean[]{false, false};
   }
}
