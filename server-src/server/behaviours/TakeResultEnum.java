package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.utils.StringUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum TakeResultEnum {
   SUCCESS(false, false, false),
   TARGET_HAS_NO_OWNER(false, false, false),
   PERFORMER_IS_OWNER(false, false, false),
   TARGET_IS_UNREACHABLE("You can't reach that now.", true, true, false),
   TARGET_IS_LIQUID("You need to pour that into container.", true, false, false),
   TARGET_FILLED_BULK_CONTAINER("It is too heavy now.", true, false, false),
   MAY_NOT_LOOT_THAT_ITEM("You may not loot that item.", true, false, false),
   VEHICLE_IS_WATCHED("The %s is being watched too closely. You cannot take items from it.", true, true, true),
   NEEDS_TO_STEAL("You have to steal the %s.", true, true, true),
   IN_LEGAL_MODE(false, true, false),
   MAY_NOT_STEAL("You need more body control to steal things.", true, true, false),
   NEED_TO_BE_EMPTY_BEFORE_THEFT("You must empty the %s before you steal it.", true, true, true),
   PREVENTED_THEFT(false, true, false),
   TOO_FAR_AWAY("You are now too far away to get the %s.", true, true, true, true),
   TARGET_BLOCKED("You can't reach the %s through the %s.", true, true, true, true),
   INVENTORY_FULL("Your inventory contains too many items already.", true, true, false),
   INVENTORY_FULL_PLACED("Your inventory contains too many items to carry all of the placed items on the %s.", true, true, false),
   CARRYING_TOO_MUCH("You are carrying too much to pick up the %s.", true, false, true, true),
   HITCHED("There are hitched creatures.", true, true, true, true),
   TARGET_IN_WATER("You can't put that in the water.", true, false, false),
   TARGET_WEIRD("For some weird reason the %s won't budge.", true, false, false),
   TARGET_BULK_ITEM("You have to use drag and drop instead.", true, true, false),
   TARGET_IN_USE("You cannot take the %s as it is in use.", true, true, true),
   UNKNOWN_FAILURE(false, true, false);

   private final String message;
   private final boolean print;
   private final boolean abortTakeFromPile;
   private final boolean formatted;
   private final boolean safePrint;
   private final Map<Long, String[]> texts = new ConcurrentHashMap<>();

   private TakeResultEnum(boolean print, boolean abortTakeFromPile, boolean formatted) {
      this.print = print;
      this.abortTakeFromPile = abortTakeFromPile;
      this.formatted = formatted;
      this.safePrint = false;
      this.message = "";
   }

   private TakeResultEnum(boolean print, boolean abortTakeFromPile, boolean formatted, boolean safePrint) {
      this.print = print;
      this.abortTakeFromPile = abortTakeFromPile;
      this.formatted = formatted;
      this.safePrint = safePrint;
      this.message = "";
   }

   private TakeResultEnum(String message, boolean print, boolean abortTakeFromPile, boolean formatted) {
      this.message = message;
      this.print = print;
      this.abortTakeFromPile = abortTakeFromPile;
      this.formatted = formatted;
      this.safePrint = false;
   }

   private TakeResultEnum(String message, boolean print, boolean abortTakeFromPile, boolean formatted, boolean safePrint) {
      this.message = message;
      this.print = print;
      this.abortTakeFromPile = abortTakeFromPile;
      this.formatted = formatted;
      this.safePrint = safePrint;
   }

   public final String getResultStringFormattedWith(String text) {
      return StringUtil.format(this.message, text);
   }

   public final String getMessage(long performerId) {
      if (this.haveMessageParameters() && this.texts.containsKey(performerId)) {
         String[] vars = (String[])this.texts.get(performerId);
         this.texts.remove(performerId);
         return StringUtil.format(this.message, vars);
      } else {
         return this.message;
      }
   }

   public final boolean haveMessageParameters() {
      return this.formatted;
   }

   public void setIndexText(long performerId, String... strings) {
      this.texts.put(performerId, strings);
   }

   public final boolean shouldPrint() {
      return this.print;
   }

   public final boolean safePrint() {
      return this.safePrint;
   }

   public final boolean abortsTakeFromPile() {
      return this.abortTakeFromPile;
   }

   public void sendToPerformer(Creature performer) {
      if (this.shouldPrint()) {
         if (this.safePrint) {
            performer.getCommunicator().sendSafeServerMessage(this.getMessage(performer.getWurmId()));
         } else {
            performer.getCommunicator().sendNormalServerMessage(this.getMessage(performer.getWurmId()));
         }
      }
   }
}
