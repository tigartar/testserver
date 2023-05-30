package com.wurmonline.server.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.shared.constants.ItemMaterials;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CreationEntry implements ItemTypes, ItemMaterials, MiscConstants {
   final int primarySkill;
   final int objectSource;
   protected byte objectSourceMaterial = 0;
   final int objectTarget;
   protected byte objectTargetMaterial = 0;
   final int objectCreated;
   final boolean destroyTarget;
   final boolean useCapacity;
   final float percentageLost;
   final int minTimeSeconds;
   final boolean destroyBoth;
   private static final Logger logger = Logger.getLogger(CreationEntry.class.getName());
   final boolean createOnGround;
   public boolean depleteEqually;
   public boolean depleteSource = false;
   public boolean depleteTarget = false;
   public static final float TUTORIALCHANCE = 90.0F;
   public boolean isOnlyCreateEpicTargetMission = false;
   public boolean isCreateEpicTargetMission = true;
   private boolean hasCustomChanceCutoff = false;
   private int customCreationChanceCutOff = 0;
   private boolean hasMinimumSkillRequirement = false;
   private double minimumSkill = 0.0;
   private int depleteFromSource = 0;
   private int depleteFromTarget = 0;
   private CreationCategories category;
   private int deity = 0;
   private boolean useTemplateWeight = false;
   private boolean colouringCreation = false;
   private boolean useFinalMaterial = false;
   private byte finalMaterial = 0;
   protected static final CreationRequirement[] emptyReqs = new CreationRequirement[0];

   CreationEntry(
      int aPrimarySkill,
      int aObjectSource,
      int aObjectTarget,
      int aObjectCreated,
      boolean aDestroyTarget,
      boolean aUseCapacity,
      float aPercentageLost,
      int aMinTimeSeconds,
      boolean aDestroyBoth,
      boolean aCreateOnGround,
      CreationCategories aCategory
   ) {
      this.primarySkill = aPrimarySkill;
      this.objectSource = aObjectSource;
      this.objectTarget = aObjectTarget;
      this.objectCreated = aObjectCreated;
      this.destroyTarget = aDestroyTarget;
      this.depleteTarget = this.destroyTarget;
      this.useCapacity = aUseCapacity;
      this.depleteSource = this.useCapacity;
      this.percentageLost = aPercentageLost;
      this.minTimeSeconds = aMinTimeSeconds;
      this.destroyBoth = aDestroyBoth;
      this.depleteEqually = this.destroyBoth;
      this.createOnGround = aCreateOnGround;
      this.category = aCategory;
   }

   public boolean isAdvanced() {
      return false;
   }

   CreationEntry(
      int aPrimarySkill,
      int aObjectSource,
      int aObjectTarget,
      int aObjectCreated,
      boolean aDepleteSource,
      boolean aDepleteTarget,
      boolean aDepleteEqually,
      float aPercentageLost,
      boolean aCreateOnGround,
      CreationCategories aCategory
   ) {
      this.primarySkill = aPrimarySkill;
      this.objectSource = aObjectSource;
      this.objectTarget = aObjectTarget;
      this.objectCreated = aObjectCreated;
      this.depleteTarget = aDepleteTarget;
      this.destroyTarget = this.depleteTarget;
      this.depleteSource = aDepleteSource;
      this.percentageLost = aPercentageLost;
      this.depleteEqually = false;
      if (aDepleteEqually) {
         this.depleteTarget = true;
         this.depleteSource = true;
      }

      this.destroyBoth = aDepleteEqually;
      this.minTimeSeconds = 5;
      this.useCapacity = this.depleteSource;
      this.createOnGround = aCreateOnGround;
      this.category = aCategory;
   }

   CreationEntry(
      int aPrimarySkill,
      int aObjectSource,
      int aObjectTarget,
      int aObjectCreated,
      boolean aDepleteSource,
      boolean aDepleteTarget,
      boolean aDepleteEqually,
      float aPercentageLost,
      boolean aCreateOnGround,
      int aCustomCutOffChance,
      double aMinimumSkill,
      CreationCategories aCategory
   ) {
      this(
         aPrimarySkill,
         aObjectSource,
         aObjectTarget,
         aObjectCreated,
         aDepleteSource,
         aDepleteTarget,
         aDepleteEqually,
         aPercentageLost,
         aCreateOnGround,
         aCategory
      );
      if (aCustomCutOffChance != 0) {
         this.customCreationChanceCutOff = aCustomCutOffChance;
         this.hasCustomChanceCutoff = true;
      }

      if (aMinimumSkill != 0.0) {
         this.minimumSkill = aMinimumSkill;
         this.hasMinimumSkillRequirement = true;
      }
   }

   CreationEntry(
      int aPrimarySkill,
      int aObjectSource,
      int aObjectTarget,
      int aObjectCreated,
      boolean aDepleteSource,
      boolean aDepleteTarget,
      float aPercentageLost,
      boolean aDepleteEqually,
      boolean aCreateOnGround,
      CreationCategories aCategory
   ) {
      this.primarySkill = aPrimarySkill;
      this.objectSource = aObjectSource;
      this.objectTarget = aObjectTarget;
      this.objectCreated = aObjectCreated;
      this.depleteTarget = aDepleteTarget;
      this.destroyTarget = this.depleteTarget;
      this.depleteSource = aDepleteSource;
      this.percentageLost = aPercentageLost;
      this.depleteEqually = aDepleteEqually;
      this.destroyBoth = this.depleteEqually;
      this.minTimeSeconds = 5;
      this.useCapacity = this.depleteSource;
      this.createOnGround = aCreateOnGround;
      this.category = aCategory;
   }

   CreationEntry(
      int aPrimarySkill,
      int aObjectSource,
      int aObjectTarget,
      int aObjectCreated,
      boolean aDepleteSource,
      boolean aDepleteTarget,
      float aPercentageLost,
      boolean aDepleteEqually,
      boolean aCreateOnGround,
      int aCustomCutOffChance,
      double aMinimumSkill,
      CreationCategories aCategory
   ) {
      this(
         aPrimarySkill,
         aObjectSource,
         aObjectTarget,
         aObjectCreated,
         aDepleteSource,
         aDepleteTarget,
         aPercentageLost,
         aDepleteEqually,
         aCreateOnGround,
         aCategory
      );
      if (aCustomCutOffChance != 0) {
         this.customCreationChanceCutOff = aCustomCutOffChance;
         this.hasCustomChanceCutoff = true;
      }

      if (aMinimumSkill != 0.0) {
         this.minimumSkill = aMinimumSkill;
         this.hasMinimumSkillRequirement = true;
      }
   }

   public int getObjectCreated() {
      return this.objectCreated;
   }

   public final int getCustomCutOffChance() {
      return this.customCreationChanceCutOff;
   }

   public final CreationCategories getCategory() {
      return this.category;
   }

   public int getTotalNumberOfItems() {
      return this.depleteSource && this.depleteTarget ? 2 : 1;
   }

   public CreationRequirement[] getRequirements() {
      return emptyReqs;
   }

   public int getObjectSource() {
      return this.objectSource;
   }

   public byte getObjectSourceMaterial() {
      return this.objectSourceMaterial;
   }

   public void setObjectSourceMaterial(byte sourceMaterial) {
      this.objectSourceMaterial = sourceMaterial;
   }

   public int getObjectTarget() {
      return this.objectTarget;
   }

   public byte getObjectTargetMaterial() {
      return this.objectTargetMaterial;
   }

   public void setObjectTargetMaterial(byte targetMaterial) {
      this.objectTargetMaterial = targetMaterial;
   }

   public int getPrimarySkill() {
      return this.primarySkill;
   }

   boolean isDepleteSourceAndTarget() {
      return this.depleteEqually;
   }

   void setDepleteSourceAndTarget(boolean aDepleteSourceAndTarget) {
      this.depleteEqually = aDepleteSourceAndTarget;
   }

   boolean isDestroyTarget() {
      return this.destroyTarget;
   }

   public final boolean hasCustomCreationChanceCutOff() {
      return this.hasCustomChanceCutoff;
   }

   public final boolean hasCustomDepleteFromSource() {
      return this.depleteFromSource != 0;
   }

   public final boolean hasCustomDepleteFromTarget() {
      return this.depleteFromTarget != 0;
   }

   public void setDepleteFromSource(int toDeplete) {
      this.depleteFromSource = toDeplete;
   }

   public void setDepleteFromTarget(int toDeplete) {
      this.depleteFromTarget = toDeplete;
   }

   public final int getDepleteFromTarget() {
      return this.depleteFromTarget;
   }

   public final int getDepleteFromSource() {
      return this.depleteFromSource;
   }

   public final boolean isRestrictedToDeityFollower() {
      return this.deity != 0;
   }

   public void setDeityRestriction(int deity) {
      this.deity = deity;
   }

   public final int getDeityRestriction() {
      return this.deity;
   }

   public final boolean usesFinalMaterial() {
      return this.useFinalMaterial;
   }

   public void setFinalMaterial(byte material) {
      this.finalMaterial = material;
      if (material == 0) {
         this.useFinalMaterial = false;
      } else {
         this.useFinalMaterial = true;
      }
   }

   public final byte getFinalMaterial() {
      return this.finalMaterial;
   }

   boolean isUseCapacity() {
      return this.useCapacity;
   }

   float getPercentageLost() {
      return this.percentageLost;
   }

   public void setUseTemplateWeight(boolean templateWeight) {
      this.useTemplateWeight = templateWeight;
   }

   public final boolean getUseTempalateWeight() {
      return this.useTemplateWeight;
   }

   public final boolean isColouringCreation() {
      return this.colouringCreation;
   }

   public void setColouringCreation(boolean addsColour) {
      this.colouringCreation = addsColour;
   }

   public void setIsEpicBuildMissionTarget(boolean target_ok) {
      this.isCreateEpicTargetMission = target_ok;
   }

   int getMinTimeSeconds() {
      return this.minTimeSeconds;
   }

   public final double getMinimumSkillRequirement() {
      return this.minimumSkill;
   }

   public final boolean hasMinimumSkillRequirement() {
      return this.hasMinimumSkillRequirement;
   }

   boolean isDestroyBoth() {
      return this.destroyBoth;
   }

   public float getDifficultyFor(Item source, Item target, Creature performer) throws NoSuchTemplateException {
      Item realSource = source;
      if (source.getTemplateId() == this.objectTarget && target.getTemplateId() == this.objectSource && source.getTemplateId() != target.getTemplateId()) {
         realSource = target;
      }

      Skills skills = performer.getSkills();
      Skill primSkill = null;
      Skill secondarySkill = null;
      double bonus = 0.0;

      try {
         primSkill = skills.getSkill(this.primarySkill);
         if (this.hasMinimumSkillRequirement() && this.getMinimumSkillRequirement() > primSkill.getKnowledge(0.0)) {
            return 0.0F;
         }
      } catch (Exception var14) {
      }

      try {
         secondarySkill = skills.getSkill(realSource.getPrimarySkill());
      } catch (Exception var13) {
      }

      ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(this.objectCreated);
      if (secondarySkill != null) {
         bonus = Math.max(1.0, secondarySkill.getKnowledge(realSource, 0.0) / 10.0);
      }

      float chance = 0.0F;
      if (template.isRune()) {
         boolean godBonus = false;
         if (performer.getDeity() == null) {
            if (this.objectCreated == 1293) {
               godBonus = true;
            }
         } else if (performer.getDeity().isMountainGod() && this.objectCreated == 1289
            || performer.getDeity().isForestGod() && this.objectCreated == 1290
            || performer.getDeity().isWaterGod() && this.objectCreated == 1291
            || performer.getDeity().isHateGod() && this.objectCreated == 1292) {
            if (performer.getFaith() >= 20.0F) {
               godBonus = true;
            }
         } else if (performer.getFaith() < 20.0F && this.objectCreated == 1293) {
            godBonus = true;
         }

         if (godBonus) {
            bonus += 100.0;
         }
      }

      if (template.isTutorialItem()) {
         return 90.0F;
      } else {
         if (primSkill != null) {
            chance = (float)primSkill.getChance((double)template.getDifficulty(), realSource, bonus);
         } else {
            chance = 1.0F / (1.0F + template.getDifficulty()) * 100.0F;
         }

         return chance;
      }
   }

   public boolean isCreateOnGround() {
      return this.createOnGround;
   }

   public static final int getScrapMaterial(byte material) {
      if (Materials.isWood(material)) {
         return 169;
      } else if (material == 11) {
         return 46;
      } else if (material == 17) {
         return 171;
      } else if (material == 2 || material == 22) {
         return 173;
      } else if (material == 16) {
         return 172;
      } else if (material == 10) {
         return 47;
      } else if (material == 7) {
         return 44;
      } else if (material == 8) {
         return 45;
      } else if (material == 13) {
         return 48;
      } else if (material == 12) {
         return 49;
      } else if (material == 30) {
         return 221;
      } else if (material == 31) {
         return 223;
      } else if (material == 34) {
         return 220;
      } else if (material == 9) {
         return 205;
      } else if (material == 56) {
         return 694;
      } else if (material == 57) {
         return 698;
      } else if (material == 26) {
         return 634;
      } else if (material == 67) {
         return 837;
      } else {
         return material == 96 ? 1411 : -1;
      }
   }

   protected final void checkSaneAmounts(
      Item realSource, int sourceWeightToRemove, Item realTarget, int targetWeightToRemove, ItemTemplate template, Creature performer, boolean advancedItem
   ) throws NoSuchItemException {
      if ((!this.depleteSource || sourceWeightToRemove > 0) && (!this.depleteTarget || targetWeightToRemove > 0)) {
         if (!advancedItem
            && (this.depleteSource && this.depleteTarget || this.depleteEqually)
            && realSource.getWeightGrams(false) + realTarget.getWeightGrams(false) < template.getWeightGrams()) {
            if (!realSource.isCombine() && !realSource.isLiquid() && !realTarget.isLiquid() && !realTarget.isCombine()) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "The "
                        + realSource.getName()
                        + " and the "
                        + realTarget.getName()
                        + " contains too little material to create "
                        + template.getNameWithGenus()
                        + ". You need to find larger parts."
                  );
            } else {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "The "
                        + realSource.getName()
                        + " and the "
                        + realTarget.getName()
                        + " contains too little material to create "
                        + template.getNameWithGenus()
                        + ".  Try to combine any of them with a similar object to get larger pieces."
                  );
            }

            throw new NoSuchItemException("Too little material.");
         } else {
            if (!advancedItem
               && (this.depleteSource && this.depleteTarget || this.depleteEqually)
               && (realTarget.isCombine() || realTarget.isLiquid())
               && (realSource.isCombine() || realSource.isLiquid())) {
               int sourceMax = sourceWeightToRemove <= realSource.getWeightGrams() ? 1 : 0;
               int targetMax = targetWeightToRemove <= realTarget.getWeightGrams() ? 1 : 0;
               if (template.isCombine() && this.objectCreated != 73) {
                  sourceMax = (int)((float)realSource.getWeightGrams() / ((float)template.getWeightGrams() / 2.0F));
                  targetMax = (int)((float)realTarget.getWeightGrams() / ((float)template.getWeightGrams() / 2.0F));
               }

               if (sourceMax == 0 || targetMax == 0) {
                  performer.getCommunicator().sendNormalServerMessage("The amount of materials is too low to produce anything.");
                  throw new NoSuchItemException("Bad amounts of combined items.");
               }
            }

            if (realSource.getWeightGrams(false) < sourceWeightToRemove) {
               if (realSource.isCombine()) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "The "
                           + realSource.getName()
                           + " contains too little material to create "
                           + template.getNameWithGenus()
                           + ".  Try to combine it with a similar object to get a larger amount."
                     );
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage("The " + realSource.getName() + " contains too little material to create " + template.getNameWithGenus() + ".");
               }

               throw new NoSuchItemException("Too little material.");
            } else if (realTarget.getWeightGrams(false) < targetWeightToRemove) {
               if (realTarget.isCombine()) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "The "
                           + realTarget.getName()
                           + " contains too little material to create "
                           + template.getNameWithGenus()
                           + ".  Try to combine it with a similar object to get a larger amount."
                     );
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage("The " + realTarget.getName() + " contains too little material to create " + template.getNameWithGenus() + ".");
               }

               throw new NoSuchItemException("Too little material.");
            } else {
               if (realSource.isLiquid() && realTarget.isLiquid()) {
                  int sourceMax = realSource.getWeightGrams() / realSource.getTemplate().getWeightGrams();
                  int targetMax = realTarget.getWeightGrams() / realTarget.getTemplate().getWeightGrams();
                  if (sourceMax < 1 || targetMax < 1 || sourceMax / targetMax > 2 || targetMax / sourceMax > 2) {
                     if (sourceMax < 1) {
                        performer.getCommunicator().sendNormalServerMessage("You need more " + realSource.getName() + ".");
                     }

                     if (targetMax < 1) {
                        performer.getCommunicator().sendNormalServerMessage("You need more " + realTarget.getName() + ".");
                     } else if (sourceMax != targetMax) {
                        if (sourceMax < targetMax) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("You need to add more " + realSource.getName() + " or remove some " + realTarget.getName() + ".");
                        } else {
                           performer.getCommunicator()
                              .sendNormalServerMessage("You need to add more " + realTarget.getName() + " or remove some " + realSource.getName() + ".");
                        }
                     }

                     throw new NoSuchItemException("Not balanced.");
                  }

                  Item parent = null;
                  if (template.isLiquid()) {
                     try {
                        parent = realSource.getParent();
                     } catch (NoSuchItemException var12) {
                     }

                     try {
                        Item parent2 = Items.getItem(realTarget.getParentId());
                        if ((parent == null || !parent.isContainerLiquid()) && parent2.isEmpty(false)) {
                           parent = parent2;
                        }
                     } catch (NoSuchItemException var13) {
                     }

                     if (parent != null && parent.getVolume() < realSource.getWeightGrams() + realTarget.getWeightGrams()) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("Not all the liquid will fit in the " + parent.getName() + " so some will be lost.");
                     }
                  }
               }
            }
         }
      } else {
         performer.getCommunicator()
            .sendNormalServerMessage(
               "The "
                  + realSource.getName()
                  + " or the "
                  + realTarget.getName()
                  + " contains too little material to create "
                  + template.getNameWithGenus()
                  + ". You need to find more."
            );
         throw new NoSuchItemException("Too little material.");
      }
   }

   public int getSourceWeightToRemove(Item realSource, Item realTarget, ItemTemplate template, boolean advancedEntry) {
      int weightToRemove = 0;
      if (this.hasCustomDepleteFromSource()) {
         weightToRemove = this.getDepleteFromSource();
      } else if (this.depleteEqually) {
         if (advancedEntry) {
            weightToRemove = realSource.getTemplate().getWeightGrams();
         } else {
            weightToRemove = template.getWeightGrams() / 2;
         }
      } else if (template.isLiquid()) {
         int nums = 1;
         if (this.depleteTarget && this.depleteSource) {
            nums = this.getTemplateNumbers(realSource, realTarget, template);
            weightToRemove = nums * realSource.getTemplate().getWeightGrams();
         } else if (this.depleteSource) {
            nums = this.getTemplateNumbersForSource(realSource, template);
            weightToRemove = nums * realSource.getTemplate().getWeightGrams();
         } else {
            boolean var8 = false;
         }
      } else if (this.depleteSource
         && this.depleteTarget
         && (realTarget.isCombine() || realTarget.isLiquid())
         && (realSource.isCombine() || realSource.isLiquid())) {
         int nums = 1;
         if (advancedEntry) {
            nums = 1;
         }

         weightToRemove = nums * realSource.getTemplate().getWeightGrams();
         if (realSource.getTemplateId() == 73) {
            weightToRemove /= 10;
         }
      } else if (this.depleteSource) {
         weightToRemove = realSource.getTemplate().getWeightGrams();
         if (this.depleteTarget) {
            if (!advancedEntry && !realTarget.isCombine() && realSource.isCombine()) {
               weightToRemove = Math.max(realSource.getTemplate().getWeightGrams(), template.getWeightGrams() - realTarget.getWeightGrams());
            }

            if (realSource.getTemplateId() == 9 && (float)realSource.getWeightGrams() > (float)realSource.getTemplate().getWeightGrams() * 0.7F) {
               weightToRemove = realSource.getWeightGrams();
            }

            if (realSource.getTemplateId() == 73) {
               weightToRemove /= 10;
            }
         }
      }

      return weightToRemove;
   }

   public int getTargetWeightToRemove(Item realSource, Item realTarget, ItemTemplate template, boolean advancedEntry) {
      int weightToRemove = 0;
      if (this.hasCustomDepleteFromTarget()) {
         weightToRemove = this.getDepleteFromTarget();
      } else if (this.depleteEqually) {
         if (advancedEntry) {
            weightToRemove = realTarget.getTemplate().getWeightGrams();
         } else {
            weightToRemove = template.getWeightGrams() / 2;
         }
      } else if (template.isLiquid()) {
         int nums = 1;
         if (this.depleteTarget && this.depleteSource) {
            nums = this.getTemplateNumbers(realSource, realTarget, template);
         } else if (this.depleteTarget) {
            nums = this.getTemplateNumbersForTarget(realTarget, template);
         } else {
            nums = 0;
         }

         weightToRemove = nums * realTarget.getTemplate().getWeightGrams();
      } else if (this.depleteSource
         && this.depleteTarget
         && (realTarget.isCombine() || realTarget.isLiquid())
         && (realSource.isCombine() || realSource.isLiquid())) {
         int nums = 1;
         if (advancedEntry) {
            nums = 1;
         }

         weightToRemove = nums * (realTarget.getTemplate().getWeightGrams() + this.getExtraWeight(template));
         int weightCap = (int)((float)(template.getWeightGrams() + this.getExtraWeight(template)) * 1.5F);
         if (weightToRemove > weightCap) {
            weightToRemove = weightCap;
         }
      } else if (this.depleteTarget) {
         if (!realTarget.isCombine()) {
            if (!this.depleteSource) {
               if (advancedEntry) {
                  weightToRemove = realTarget.getTemplate().getWeightGrams() + this.getExtraWeight(template);
               } else {
                  weightToRemove = template.getWeightGrams() + this.getExtraWeight(template);
               }
            } else {
               weightToRemove = realTarget.getTemplate().getWeightGrams();
            }
         } else if (this.depleteSource) {
            if (advancedEntry) {
               weightToRemove = realTarget.getTemplate().getWeightGrams() + this.getExtraWeight(template);
            } else {
               weightToRemove = template.getWeightGrams() - realSource.getTemplate().getWeightGrams() + this.getExtraWeight(template);
            }
         } else {
            weightToRemove = template.getWeightGrams() + this.getExtraWeight(template);
         }
      }

      if (weightToRemove < 0) {
         logger.log(Level.WARNING, template.getName() + " when created depletes less than 0.");
         weightToRemove = realTarget.getTemplate().getWeightGrams() + this.getExtraWeight(template);
      }

      return weightToRemove;
   }

   protected final int getTemplateNumbersForTarget(Item realTarget, ItemTemplate template) {
      return Math.max(1, realTarget.getWeightGrams() / (realTarget.getTemplate().getWeightGrams() + this.getExtraWeight(template)));
   }

   protected final int getTemplateNumbersForSource(Item realSource, ItemTemplate template) {
      return Math.max(1, realSource.getWeightGrams() / (realSource.getTemplate().getWeightGrams() + this.getExtraWeight(template)));
   }

   protected final int getTemplateNumbers(Item realSource, Item realTarget, ItemTemplate template) {
      return Math.max(
         1,
         Math.min(
            realSource.getWeightGrams() / realSource.getTemplate().getWeightGrams(),
            realTarget.getWeightGrams() / (realTarget.getTemplate().getWeightGrams() + this.getExtraWeight(template))
         )
      );
   }

   protected final int getExtraWeight(ItemTemplate template) {
      if (template.isRune()) {
         return 0;
      } else {
         return this.percentageLost > 0.0F ? (int)(this.percentageLost / 100.0F * (float)template.getWeightGrams()) : 0;
      }
   }

   public final boolean meetsCreatureRestriction(Item source, Item target) {
      if (this.objectCreated == 848) {
         int data = 14;
         return source.getData2() == 14 || target.getData2() == 14;
      } else if (this.objectCreated == 847) {
         int data = 12;
         return source.getData2() == 12 || target.getData2() == 12;
      } else if (this.objectCreated == 846) {
         int data = 42;
         return source.getData2() == 42 || target.getData2() == 42;
      } else if (this.objectCreated != 849) {
         return true;
      } else {
         int data = 10;
         return source.getData2() == 10 || target.getData2() == 10;
      }
   }

   public abstract Item run(Creature var1, Item var2, long var3, float var5) throws FailedException, NoSuchSkillException, NoSuchItemException;

   abstract CreationEntry cloneAndRevert();
}
