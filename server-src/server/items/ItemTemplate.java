/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.Behaviours;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.ContainerRestriction;
import com.wurmonline.server.items.InitialContainer;
import com.wurmonline.server.items.ItemSizes;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.util.MaterialUtilities;
import com.wurmonline.shared.util.StringUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemTemplate
implements MiscConstants,
ItemMaterials,
ItemTypes,
ItemSizes,
Comparable<ItemTemplate> {
    public long damUpdates = 0L;
    public long maintUpdates = 0L;
    boolean isDuelRing = false;
    private final String name;
    private final short behaviourType;
    public final short imageNumber;
    private final String itemDescriptionLong;
    private final String itemDescriptionSuperb;
    private final String itemDescriptionNormal;
    private final String itemDescriptionBad;
    private final String itemDescriptionRotten;
    private String concatName;
    final int templateId;
    private final long decayTime;
    public float priceHalfSize = 100.0f;
    private final int centimetersX;
    private final int centimetersY;
    private final int centimetersZ;
    private boolean usesSpecifiedContainerSizes;
    private int containerCentimetersX;
    private int containerCentimetersY;
    private int containerCentimetersZ;
    private boolean calcNutritionValues = true;
    private short calories = (short)1000;
    private short carbs = (short)150;
    private short fats = (short)40;
    private short proteins = (short)25;
    private int grows = 0;
    private int crushsTo = 0;
    private int pickSeeds = 0;
    private int alcoholStrength = 0;
    private boolean canBeCookingOil = false;
    private boolean useRealTemplateIcon = false;
    private boolean canBeRawWrapped = false;
    private boolean canBePapyrusWrapped = false;
    private boolean canBeClothWrapped = false;
    private boolean surfaceonly = false;
    private int dyePrimaryAmountRequired = 0;
    private int dyeSecondaryAmountRequired = 0;
    private String secondaryItemName = "";
    private final int volume;
    private int containerVolume;
    private final int primarySkill;
    private final byte[] bodySpaces;
    private final int combatDamage;
    private final String modelName;
    private final float difficulty;
    private static final Logger logger = Logger.getLogger(ItemTemplate.class.getName());
    private final int weight;
    private final byte material;
    private final int value;
    private final boolean isPurchased;
    private final String plural;
    private final int size;
    private boolean isSharp;
    boolean hollow = false;
    boolean weaponslash = false;
    boolean shield = false;
    boolean armour = false;
    boolean food = false;
    boolean magic = false;
    boolean magicContainer = false;
    boolean fieldtool = false;
    boolean bodypart = false;
    boolean inventory = false;
    boolean inventoryGroup = false;
    boolean unstableRift = false;
    boolean miningtool = false;
    boolean carpentrytool = false;
    boolean smithingtool = false;
    boolean weaponpierce = false;
    boolean weaponcrush = false;
    boolean weaponaxe = false;
    boolean weaponsword = false;
    boolean weaponPolearm = false;
    boolean weaponknife = false;
    boolean weaponmisc = false;
    boolean rechargeable = false;
    boolean bow = false;
    public boolean bowUnstringed = false;
    boolean diggingtool = false;
    boolean seed = false;
    boolean liquid = false;
    boolean melting = false;
    boolean meat = false;
    boolean sign = false;
    boolean fence = false;
    boolean streetlamp = false;
    boolean vegetable = false;
    boolean wood = false;
    private boolean metal = false;
    boolean stone = false;
    boolean notrade = false;
    boolean visibleDecay = false;
    boolean leather = false;
    boolean cloth = false;
    boolean paper = false;
    boolean pottery = false;
    boolean notake = false;
    boolean light = false;
    boolean containerliquid = false;
    boolean liquidinflammable = false;
    boolean weaponmelee = false;
    boolean fish = false;
    boolean weapon = false;
    boolean tool = false;
    boolean lock = false;
    boolean indestructible = false;
    boolean key = false;
    boolean nodrop = false;
    boolean repairable = false;
    boolean lockable = false;
    boolean temporary = false;
    boolean combine = false;
    private boolean canHaveInscription = false;
    boolean hasdata = false;
    boolean hasExtraData = false;
    boolean viewableSubItems = false;
    boolean isContainerWithSubItems = false;
    boolean outsideonly = false;
    boolean coin = false;
    boolean turnable = false;
    boolean decoration = false;
    boolean fullprice = false;
    boolean norename = false;
    private boolean nonutrition = false;
    private boolean lownutrition = false;
    private boolean mediumnutrition = false;
    private boolean goodnutrition = false;
    private boolean highnutrition = false;
    public boolean isDish = false;
    private boolean isFoodMaker = false;
    boolean herb = false;
    boolean spice = false;
    boolean fruit = false;
    boolean poison = false;
    boolean draggable = false;
    boolean villagedeed = false;
    boolean farwalkerItem = false;
    boolean homesteaddeed = false;
    boolean alwayspoll = false;
    boolean protectionTower = false;
    boolean floating = false;
    boolean isButcheredItem = false;
    boolean isNoPut = false;
    boolean isLeadCreature = false;
    boolean isLeadMultipleCreatures = false;
    boolean isFire = false;
    private boolean isCarpet = false;
    boolean domainItem = false;
    boolean useOnGroundOnly = false;
    boolean holyItem = false;
    boolean hugeAltar = false;
    public boolean artifact = false;
    public boolean unique = false;
    boolean destroysHugeAltar = false;
    boolean passFullData = false;
    boolean isForm = false;
    boolean descIsExam = false;
    boolean isServerBound = false;
    boolean isTwohanded = false;
    boolean kingdomMarker = false;
    boolean destroyable = false;
    boolean priceAffectedByMaterial = false;
    private boolean liquidCooking = false;
    boolean positiveDecay = false;
    boolean drinkable = false;
    public boolean isColor = false;
    boolean colorable = false;
    boolean gem = false;
    boolean egg = false;
    boolean newbieItem = false;
    boolean challengeNewbieItem = false;
    boolean isTileAligned = false;
    boolean isDragonArmour = false;
    boolean isCompass = false;
    boolean isToolbelt = false;
    boolean isBelt = false;
    boolean oilConsuming = false;
    boolean candleHolder = false;
    boolean flickeringLight = false;
    boolean namedCreator = false;
    boolean onePerTile = false;
    boolean fourPerTile = false;
    boolean tenPerTile = false;
    Deity deity = null;
    public String sizeString = "";
    int alchemyType = 0;
    boolean healing = false;
    boolean bed = false;
    boolean insideOnly = false;
    boolean nobank = false;
    boolean isRecycled = false;
    public boolean alwaysLoaded = false;
    boolean brightLight = false;
    private boolean isVehicle = false;
    public boolean isChair = false;
    public boolean isCart = false;
    boolean isVehicleDragged = false;
    boolean isMovingItem = false;
    boolean isFlower = false;
    boolean isNaturePlantable = false;
    boolean isImproveItem = false;
    boolean isDeathProtection = false;
    public boolean isRoyal = false;
    public boolean isNoMove = false;
    public boolean isWind = false;
    public boolean isDredgingTool = false;
    public boolean isMineDoor = false;
    public boolean isNoSellBack = false;
    public boolean isSpringFilled = false;
    public boolean destroyOnDecay = false;
    public boolean isServerPortal = false;
    public boolean isTrap = false;
    public boolean isDisarmTrap = false;
    public boolean nonDeedable = false;
    boolean plantedFlowerpot = false;
    boolean ownerDestroyable = false;
    boolean wearableByCreaturesOnly = false;
    boolean puppet = false;
    boolean overrideNonEnchantable = false;
    boolean isMeditation = false;
    boolean isTransmutable = false;
    boolean bulkContainer = false;
    boolean bulk = false;
    boolean missions = false;
    boolean notMissions = false;
    boolean combineCold = false;
    boolean spawnsTrees = false;
    boolean killsTrees = false;
    boolean isKingdomFlag = false;
    boolean useMaterialAndKingdom = false;
    boolean isFlag = false;
    boolean isCrude = false;
    boolean minable = false;
    boolean isEnchantableJewelry = false;
    boolean isEpicTargetItem = false;
    boolean isEpicPortal = false;
    private boolean isMassProduction = false;
    boolean noWorkParent = false;
    boolean alwaysBankable = false;
    int improveItem = -1;
    public boolean alwaysLit = false;
    public boolean isMushroom = false;
    public boolean isWarTarget = false;
    public boolean isSourceSpring = false;
    public boolean isSource = false;
    public boolean isColorComponent = false;
    boolean isTutorialItem = false;
    private boolean isEquipmentSlot = false;
    public boolean isOre = false;
    public boolean isShard = false;
    boolean isAbility = false;
    boolean isAltar = false;
    public boolean isBag = false;
    public boolean isQuiver = false;
    private boolean isMagicStaff = false;
    private boolean isTent = false;
    private boolean improveUsesTypeAsMaterial = false;
    private boolean noDiscard;
    private boolean instaDiscard;
    private boolean isTransportable = false;
    private boolean isWarmachine = false;
    private boolean hideAddToCreationWindow = false;
    private boolean isBrazier = false;
    private boolean isSmearable = false;
    private boolean isItemSpawn = false;
    private boolean noImprove = false;
    private boolean isTapestry = false;
    boolean isUnfinishedNoTake = false;
    boolean isMilk = false;
    boolean isCheese = false;
    boolean isOwnerTurnable = false;
    boolean isOwnerMoveable = false;
    boolean isUnfired = false;
    boolean isPlantable = false;
    boolean isPlantOneAWeek = false;
    boolean isRiftItem = false;
    private boolean isHitchTarget = false;
    private boolean isRiftAltar = false;
    private boolean isRiftLoot = false;
    private boolean hasItemBonus = false;
    private boolean isBracelet = false;
    private boolean isPotable = false;
    private boolean canBeGrownInPot = false;
    private boolean isAlcohol = false;
    private boolean isCrushable = false;
    private boolean hasSeeds = false;
    private boolean isCooker = false;
    private boolean isFoodGroup = false;
    private int inFoodGroup = 0;
    private boolean isCookingTool = false;
    private boolean isRecipeItem = false;
    private boolean isNoCreate = false;
    private boolean usesFoodState = false;
    private boolean canBeFermented = false;
    private boolean canBeDistilled = false;
    private boolean canBeSealed = false;
    private boolean hovers = false;
    private boolean foodBonusHot = false;
    private boolean foodBonusCold = false;
    private boolean isHarvestable = false;
    private int harvestTo = 0;
    private boolean isRune = false;
    private boolean canBePegged = false;
    private boolean decayOnDeed = false;
    private InitialContainer[] initialContainers = null;
    private boolean canShowRaw = false;
    private boolean cannotBeSpellTarget = false;
    private boolean isTrellis = false;
    private boolean containsIngredientsOnly = false;
    private boolean usesRealTemplate = false;
    private boolean canLarder = false;
    private boolean isInsulated = false;
    private boolean isGuardTower = false;
    private boolean isComponentItem = false;
    private boolean parentMustBeOnGround = false;
    private int maxItemCount = -1;
    private int maxItemWeight = -1;
    private boolean isRoadMarker = false;
    private boolean isPaveable = false;
    private boolean isCavePaveable = false;
    public boolean decorationWhenPlanted = false;
    private int fragmentAmount = 3;
    boolean descIsName = false;
    boolean isNotRuneable = false;
    private boolean showsSlopes = false;
    private boolean supportsSecondryColor = false;
    private boolean createsWithLock = false;
    private boolean isFishingReel = false;
    private boolean isFishingLine = false;
    private boolean isFishingFloat = false;
    private boolean isFishingHook = false;
    private boolean isFishingBait = false;
    private ArrayList<ContainerRestriction> containerRestrictions = null;
    boolean isPluralName = false;

    ItemTemplate(int aTemplateId, int aSize, String aName, String aPlural, String aItemDescriptionSuperb, String aItemDescriptionNormal, String aItemDescriptionBad, String aItemDescriptionRotten, String aItemDescriptionLong, short[] aItemTypes, short aImageNumber, short aBehaviourType, int aCombatDamage, long aDecayTime, int aCentimetersX, int aCentimetersY, int aCentimetersZ, int aPrimarySkill, byte[] aBodySpaces, String aModelName, float aDifficulty, int aWeight, byte aMaterial, int aValue, boolean aIsPurchased) {
        this.templateId = aTemplateId;
        this.name = aName;
        this.plural = aPlural;
        this.itemDescriptionSuperb = aItemDescriptionSuperb;
        this.itemDescriptionNormal = aItemDescriptionNormal;
        this.itemDescriptionBad = aItemDescriptionBad;
        this.itemDescriptionRotten = aItemDescriptionRotten;
        this.itemDescriptionLong = aItemDescriptionLong;
        this.imageNumber = aImageNumber;
        this.behaviourType = aBehaviourType;
        this.combatDamage = aCombatDamage;
        this.decayTime = aDecayTime;
        int[] sizes = new int[]{aCentimetersX, aCentimetersY, aCentimetersZ};
        Arrays.sort(sizes);
        this.centimetersX = sizes[0];
        this.centimetersY = sizes[1];
        this.centimetersZ = sizes[2];
        this.volume = aCentimetersX * aCentimetersY * aCentimetersZ;
        this.primarySkill = aPrimarySkill;
        this.bodySpaces = aBodySpaces;
        this.concatName = aName.trim().toLowerCase().replaceAll(" ", "") + ".";
        if (aModelName == null) {
            this.modelName = "UNSET";
            logger.log(Level.WARNING, "Modelname was null for template with id=" + this.templateId);
        } else {
            this.modelName = aModelName;
        }
        this.difficulty = aDifficulty;
        this.weight = aWeight;
        this.material = aMaterial;
        this.value = aValue;
        this.isPurchased = aIsPurchased;
        this.size = aSize;
        this.usesSpecifiedContainerSizes = false;
        this.setSizeString();
        this.assignTypes(aItemTypes);
        this.assignTemplateTypes();
        this.checkHolyItem();
        if (this.weight > 2000) {
            this.setFragmentAmount(Math.max(3, this.weight / 750));
        }
    }

    private void assignTemplateTypes() {
        if (this.templateId == 527 || this.templateId == 525 || this.templateId == 524 || this.templateId == 509) {
            this.farwalkerItem = true;
        }
        if (this.templateId == 516 || this.templateId == 102) {
            this.isBelt = true;
        }
        if (this.templateId == 664 || this.templateId == 665) {
            this.magicContainer = true;
        }
        if (this.templateId == 578 || this.templateId == 579 || this.templateId == 999) {
            this.isKingdomFlag = true;
            this.isFlag = true;
        } else if (this.templateId == 577 || this.templateId == 487) {
            this.isFlag = true;
        } else if (this.templateId == 726 || this.templateId == 728 || this.templateId == 727) {
            this.isDuelRing = true;
            this.nonDeedable = true;
        } else if (this.templateId == 712 || this.templateId == 714 || this.templateId == 713 || this.templateId == 715 || this.templateId == 716 || this.templateId == 717) {
            this.isEpicTargetItem = true;
            this.onePerTile = true;
        } else if (this.templateId == 732 || this.templateId == 733) {
            this.isEpicPortal = true;
            this.onePerTile = true;
        } else if (this.templateId == 969 || this.templateId == 970 || this.templateId == 971) {
            this.isItemSpawn = true;
        }
        if (this.templateId == 931) {
            this.fence = true;
        }
        if (this.templateId == 228 || this.templateId == 844 || this.templateId == 729) {
            this.candleHolder = true;
        }
        if (this.templateId >= 322 && this.templateId <= 328) {
            this.isAltar = true;
        }
        if (this.templateId == 1 || this.templateId == 443 || this.templateId == 2) {
            this.isBag = true;
        }
        if (this.templateId == 462) {
            this.isQuiver = true;
        }
        if (this.templateId == 824) {
            this.inventoryGroup = true;
        }
        if (this.templateId == 939) {
            this.protectionTower = true;
        }
        if (this.templateId == 1026) {
            this.unstableRift = true;
        }
        if (this.templateId >= 1033 && this.templateId <= 1048) {
            this.isRiftItem = true;
        }
    }

    public final boolean isItemSpawn() {
        return this.isItemSpawn;
    }

    public final boolean isUnstableRift() {
        return this.unstableRift;
    }

    public boolean isEpicTargetItem() {
        return this.isEpicTargetItem;
    }

    public void checkHolyItem() {
        Deity[] deities = Deities.getDeities();
        for (int x = 0; x < deities.length; ++x) {
            if (deities[x].holyItem != this.templateId) continue;
            this.deity = deities[x];
            this.holyItem = true;
        }
    }

    public String getConcatName() {
        return this.concatName;
    }

    public int getValue() {
        return this.value;
    }

    private void setSizeString() {
        if (this.size == 1) {
            this.sizeString = "tiny ";
        } else if (this.size == 2) {
            this.sizeString = "small ";
        } else if (this.size == 4) {
            this.sizeString = "large ";
        } else if (this.size == 5) {
            this.sizeString = "huge ";
        }
    }

    public String getName() {
        return this.name;
    }

    public boolean isNamePlural() {
        return this.isPluralName;
    }

    public String getProspectName() {
        if (this.getTemplateId() == 1238) {
            return this.getName();
        }
        return MaterialUtilities.getMaterialString(this.getMaterial());
    }

    public boolean isPurchased() {
        return this.isPurchased;
    }

    public String getNameWithGenus() {
        return StringUtilities.addGenus(this.name, this.isPluralName);
    }

    public byte getMaterial() {
        return this.material;
    }

    public String getPlural() {
        return this.plural;
    }

    public boolean hasPrimarySkill() {
        return (long)this.primarySkill != -10L;
    }

    public int getPrimarySkill() throws NoSuchSkillException {
        if ((long)this.primarySkill == -10L) {
            throw new NoSuchSkillException("No skill needed for item " + this.name);
        }
        return this.primarySkill;
    }

    public String getDescriptionSuperb() {
        return this.itemDescriptionSuperb;
    }

    public float getDifficulty() {
        return this.difficulty;
    }

    public String getModelName() {
        return this.modelName;
    }

    public short getImageNumber() {
        return this.imageNumber;
    }

    public String getDescriptionNormal() {
        return this.itemDescriptionNormal;
    }

    public String getDescriptionBad() {
        return this.itemDescriptionBad;
    }

    public String getDescriptionRotten() {
        return this.itemDescriptionRotten;
    }

    public String getDescriptionLong() {
        return this.itemDescriptionLong;
    }

    public int getDamagePercent() {
        return this.combatDamage;
    }

    public final boolean isMassProduction() {
        return this.isMassProduction;
    }

    public final boolean isMushroom() {
        return this.isMushroom;
    }

    public final boolean hideAddToCreationWindow() {
        return this.hideAddToCreationWindow;
    }

    public void assignTypes(short[] types) {
        block253: for (int x = 0; x < types.length; ++x) {
            switch (types[x]) {
                case 1: {
                    this.hollow = true;
                    continue block253;
                }
                case 2: {
                    this.weapon = true;
                    this.weaponslash = true;
                    continue block253;
                }
                case 3: {
                    this.shield = true;
                    continue block253;
                }
                case 4: {
                    this.armour = true;
                    continue block253;
                }
                case 5: {
                    this.food = true;
                    this.canLarder = true;
                    continue block253;
                }
                case 6: {
                    this.magic = true;
                    continue block253;
                }
                case 7: {
                    this.fieldtool = true;
                    continue block253;
                }
                case 8: {
                    this.bodypart = true;
                    this.temporary = true;
                    continue block253;
                }
                case 9: {
                    this.inventory = true;
                    continue block253;
                }
                case 10: {
                    this.miningtool = true;
                    continue block253;
                }
                case 11: {
                    this.carpentrytool = true;
                    continue block253;
                }
                case 12: {
                    this.smithingtool = true;
                    continue block253;
                }
                case 13: {
                    this.weapon = true;
                    this.weaponpierce = true;
                    continue block253;
                }
                case 14: {
                    this.weapon = true;
                    this.weaponcrush = true;
                    continue block253;
                }
                case 15: {
                    this.weapon = true;
                    this.weaponaxe = true;
                    continue block253;
                }
                case 16: {
                    this.weapon = true;
                    this.weaponsword = true;
                    continue block253;
                }
                case 154: {
                    this.weapon = true;
                    this.weaponPolearm = true;
                    continue block253;
                }
                case 17: {
                    this.weapon = true;
                    this.weaponknife = true;
                    continue block253;
                }
                case 18: {
                    this.weapon = true;
                    this.weaponmisc = true;
                    continue block253;
                }
                case 19: {
                    this.diggingtool = true;
                    continue block253;
                }
                case 20: {
                    this.seed = true;
                    continue block253;
                }
                case 21: {
                    this.wood = true;
                    continue block253;
                }
                case 22: {
                    this.metal = true;
                    continue block253;
                }
                case 23: {
                    this.leather = true;
                    continue block253;
                }
                case 24: {
                    this.cloth = true;
                    continue block253;
                }
                case 25: {
                    this.stone = true;
                    continue block253;
                }
                case 26: {
                    this.liquid = true;
                    continue block253;
                }
                case 27: {
                    this.melting = true;
                    continue block253;
                }
                case 28: {
                    this.meat = true;
                    continue block253;
                }
                case 142: {
                    this.sign = true;
                    continue block253;
                }
                case 143: {
                    this.streetlamp = true;
                    continue block253;
                }
                case 29: {
                    this.vegetable = true;
                    continue block253;
                }
                case 30: {
                    this.pottery = true;
                    continue block253;
                }
                case 31: {
                    this.notake = true;
                    continue block253;
                }
                case 32: {
                    this.light = true;
                    continue block253;
                }
                case 33: {
                    this.containerliquid = true;
                    continue block253;
                }
                case 34: {
                    this.liquidinflammable = true;
                    continue block253;
                }
                case 35: {
                    this.weapon = true;
                    this.weaponmelee = true;
                    continue block253;
                }
                case 36: {
                    this.fish = true;
                    continue block253;
                }
                case 37: {
                    this.weapon = true;
                    continue block253;
                }
                case 38: {
                    this.tool = true;
                    continue block253;
                }
                case 39: {
                    this.lock = true;
                    continue block253;
                }
                case 40: {
                    this.indestructible = true;
                    this.isNotRuneable = true;
                    continue block253;
                }
                case 41: {
                    this.key = true;
                    continue block253;
                }
                case 42: {
                    this.nodrop = true;
                    continue block253;
                }
                case 44: {
                    this.repairable = true;
                    continue block253;
                }
                case 45: {
                    this.temporary = true;
                    continue block253;
                }
                case 46: {
                    this.combine = true;
                    continue block253;
                }
                case 47: {
                    this.lockable = true;
                    continue block253;
                }
                case 159: {
                    this.canHaveInscription = true;
                    continue block253;
                }
                case 48: {
                    this.hasdata = true;
                    continue block253;
                }
                case 49: {
                    this.outsideonly = true;
                    continue block253;
                }
                case 50: {
                    this.coin = true;
                    this.fullprice = true;
                    continue block253;
                }
                case 51: {
                    this.turnable = true;
                    continue block253;
                }
                case 52: {
                    this.decoration = true;
                    continue block253;
                }
                case 53: {
                    this.fullprice = true;
                    continue block253;
                }
                case 54: {
                    this.norename = true;
                    continue block253;
                }
                case 137: {
                    this.nonutrition = true;
                    continue block253;
                }
                case 55: {
                    this.lownutrition = true;
                    continue block253;
                }
                case 74: {
                    this.mediumnutrition = true;
                    continue block253;
                }
                case 75: {
                    this.goodnutrition = true;
                    continue block253;
                }
                case 76: {
                    this.highnutrition = true;
                    continue block253;
                }
                case 77: {
                    this.isFoodMaker = true;
                    continue block253;
                }
                case 56: {
                    this.draggable = true;
                    continue block253;
                }
                case 57: {
                    this.villagedeed = true;
                    continue block253;
                }
                case 58: {
                    this.homesteaddeed = true;
                    continue block253;
                }
                case 59: {
                    this.alwayspoll = true;
                    continue block253;
                }
                case 60: {
                    this.floating = true;
                    continue block253;
                }
                case 61: {
                    this.notrade = true;
                    continue block253;
                }
                case 62: {
                    this.hasdata = true;
                    this.isButcheredItem = true;
                    continue block253;
                }
                case 63: {
                    this.isNoPut = true;
                    continue block253;
                }
                case 64: {
                    this.isLeadCreature = true;
                    continue block253;
                }
                case 198: {
                    this.isLeadMultipleCreatures = true;
                    continue block253;
                }
                case 65: {
                    this.isFire = true;
                    continue block253;
                }
                case 66: {
                    this.domainItem = true;
                    continue block253;
                }
                case 67: {
                    this.useOnGroundOnly = true;
                    continue block253;
                }
                case 68: {
                    this.hugeAltar = true;
                    this.nonDeedable = true;
                    continue block253;
                }
                case 69: {
                    this.artifact = true;
                    this.alwaysLoaded = true;
                    this.isServerBound = true;
                    continue block253;
                }
                case 70: {
                    this.unique = true;
                    this.alwaysLoaded = true;
                    continue block253;
                }
                case 71: {
                    this.destroysHugeAltar = true;
                    continue block253;
                }
                case 72: {
                    this.passFullData = true;
                    continue block253;
                }
                case 73: {
                    this.isForm = true;
                    continue block253;
                }
                case 78: {
                    this.herb = true;
                    continue block253;
                }
                case 205: {
                    this.spice = true;
                    continue block253;
                }
                case 79: {
                    this.poison = true;
                    continue block253;
                }
                case 80: {
                    this.fruit = true;
                    continue block253;
                }
                case 81: {
                    this.descIsExam = true;
                    continue block253;
                }
                case 82: {
                    this.isDish = true;
                    this.namedCreator = true;
                    this.food = true;
                    this.canLarder = true;
                    continue block253;
                }
                case 83: {
                    this.isServerBound = true;
                    continue block253;
                }
                case 84: {
                    this.isTwohanded = true;
                    continue block253;
                }
                case 85: {
                    this.kingdomMarker = true;
                    continue block253;
                }
                case 86: {
                    this.destroyable = true;
                    continue block253;
                }
                case 87: {
                    this.priceAffectedByMaterial = true;
                    continue block253;
                }
                case 88: {
                    this.liquidCooking = true;
                    continue block253;
                }
                case 89: {
                    this.positiveDecay = true;
                    continue block253;
                }
                case 90: {
                    this.drinkable = true;
                    continue block253;
                }
                case 91: {
                    this.isColor = true;
                    continue block253;
                }
                case 92: {
                    this.colorable = true;
                    continue block253;
                }
                case 93: {
                    this.gem = true;
                    continue block253;
                }
                case 94: {
                    this.bow = true;
                    continue block253;
                }
                case 95: {
                    this.bowUnstringed = true;
                    continue block253;
                }
                case 96: {
                    this.egg = true;
                    continue block253;
                }
                case 97: {
                    this.newbieItem = true;
                    continue block253;
                }
                case 189: {
                    this.challengeNewbieItem = true;
                    continue block253;
                }
                case 98: {
                    this.isTileAligned = true;
                    continue block253;
                }
                case 99: {
                    this.isDragonArmour = true;
                    this.setImproveItem();
                    continue block253;
                }
                case 100: {
                    this.isCompass = true;
                    continue block253;
                }
                case 121: {
                    this.isToolbelt = true;
                    continue block253;
                }
                case 101: {
                    this.oilConsuming = true;
                    continue block253;
                }
                case 102: {
                    this.healing = true;
                    this.alchemyType = 1;
                    continue block253;
                }
                case 103: {
                    this.healing = true;
                    this.alchemyType = 2;
                    continue block253;
                }
                case 104: {
                    this.healing = true;
                    this.alchemyType = 3;
                    continue block253;
                }
                case 105: {
                    this.healing = true;
                    this.alchemyType = 4;
                    continue block253;
                }
                case 106: {
                    this.healing = true;
                    this.alchemyType = 5;
                    continue block253;
                }
                case 108: {
                    this.namedCreator = true;
                    continue block253;
                }
                case 109: {
                    this.onePerTile = true;
                    continue block253;
                }
                case 167: {
                    this.fourPerTile = true;
                    continue block253;
                }
                case 166: {
                    this.tenPerTile = true;
                    continue block253;
                }
                case 110: {
                    this.bed = true;
                    continue block253;
                }
                case 111: {
                    this.insideOnly = true;
                    continue block253;
                }
                case 112: {
                    this.nobank = true;
                    continue block253;
                }
                case 155: {
                    this.alwaysBankable = true;
                    this.nobank = false;
                    continue block253;
                }
                case 113: {
                    this.isRecycled = true;
                    this.nobank = true;
                    continue block253;
                }
                case 114: {
                    this.alwaysLoaded = true;
                    continue block253;
                }
                case 115: {
                    this.flickeringLight = true;
                    continue block253;
                }
                case 116: {
                    this.brightLight = true;
                    this.light = true;
                    continue block253;
                }
                case 117: {
                    this.isVehicle = true;
                    continue block253;
                }
                case 197: {
                    this.isChair = true;
                    continue block253;
                }
                case 134: {
                    this.isVehicleDragged = true;
                    continue block253;
                }
                case 193: {
                    this.isCart = true;
                    continue block253;
                }
                case 118: {
                    this.isFlower = true;
                    this.isNaturePlantable = true;
                    continue block253;
                }
                case 186: {
                    this.isNaturePlantable = true;
                    continue block253;
                }
                case 119: {
                    this.isImproveItem = true;
                    continue block253;
                }
                case 120: {
                    this.isDeathProtection = true;
                    continue block253;
                }
                case 122: {
                    this.isRoyal = true;
                    this.isServerBound = true;
                    this.alwaysLoaded = true;
                    continue block253;
                }
                case 123: {
                    this.isNoMove = true;
                    continue block253;
                }
                case 124: {
                    this.isWind = true;
                    this.alwayspoll = true;
                    continue block253;
                }
                case 125: {
                    this.isDredgingTool = true;
                    continue block253;
                }
                case 126: {
                    this.isMineDoor = true;
                    continue block253;
                }
                case 127: {
                    this.isNoSellBack = true;
                    continue block253;
                }
                case 128: {
                    this.isSpringFilled = true;
                    continue block253;
                }
                case 129: {
                    this.destroyOnDecay = true;
                    continue block253;
                }
                case 130: {
                    this.rechargeable = true;
                    continue block253;
                }
                case 131: {
                    this.isServerPortal = true;
                    continue block253;
                }
                case 132: {
                    this.isTrap = true;
                    continue block253;
                }
                case 133: {
                    this.isDisarmTrap = true;
                    continue block253;
                }
                case 135: {
                    this.ownerDestroyable = true;
                    continue block253;
                }
                case 136: {
                    this.wearableByCreaturesOnly = true;
                    continue block253;
                }
                case 138: {
                    this.puppet = true;
                    continue block253;
                }
                case 139: {
                    this.overrideNonEnchantable = true;
                    continue block253;
                }
                case 140: {
                    this.isMeditation = true;
                    continue block253;
                }
                case 141: {
                    this.isTransmutable = true;
                    continue block253;
                }
                case 144: {
                    this.visibleDecay = true;
                    continue block253;
                }
                case 145: {
                    this.bulkContainer = true;
                    continue block253;
                }
                case 146: {
                    this.bulk = true;
                    this.isNotRuneable = true;
                    continue block253;
                }
                case 147: {
                    this.missions = true;
                    continue block253;
                }
                case 157: {
                    this.notMissions = true;
                    continue block253;
                }
                case 148: {
                    this.combineCold = true;
                    continue block253;
                }
                case 149: {
                    this.spawnsTrees = true;
                    continue block253;
                }
                case 150: {
                    this.killsTrees = true;
                    continue block253;
                }
                case 151: {
                    this.isCrude = true;
                    continue block253;
                }
                case 152: {
                    this.minable = true;
                    continue block253;
                }
                case 153: {
                    this.isEnchantableJewelry = true;
                    continue block253;
                }
                case 156: {
                    this.alwaysLit = true;
                    continue block253;
                }
                case 158: {
                    this.isMassProduction = true;
                    continue block253;
                }
                case 160: {
                    this.noWorkParent = true;
                    continue block253;
                }
                case 161: {
                    this.isWarTarget = true;
                    continue block253;
                }
                case 162: {
                    this.isSourceSpring = true;
                    continue block253;
                }
                case 163: {
                    this.isSource = true;
                    continue block253;
                }
                case 164: {
                    this.isColorComponent = true;
                    continue block253;
                }
                case 165: {
                    if (!Servers.localServer.entryServer) continue block253;
                    this.isTutorialItem = true;
                    continue block253;
                }
                case 170: {
                    this.isEquipmentSlot = true;
                    continue block253;
                }
                case 168: {
                    this.isAbility = true;
                    continue block253;
                }
                case 169: {
                    this.plantedFlowerpot = true;
                    continue block253;
                }
                case 172: {
                    this.isMagicStaff = true;
                    continue block253;
                }
                case 173: {
                    this.improveUsesTypeAsMaterial = true;
                    continue block253;
                }
                case 174: {
                    this.noDiscard = true;
                    continue block253;
                }
                case 175: {
                    this.instaDiscard = true;
                    continue block253;
                }
                case 176: {
                    this.isTransportable = true;
                    continue block253;
                }
                case 177: {
                    this.isWarmachine = true;
                    continue block253;
                }
                case 178: {
                    this.hideAddToCreationWindow = true;
                    continue block253;
                }
                case 179: {
                    this.isBrazier = true;
                    continue block253;
                }
                case 180: {
                    this.usesSpecifiedContainerSizes = true;
                    continue block253;
                }
                case 181: {
                    this.setTent(true);
                    continue block253;
                }
                case 182: {
                    this.useMaterialAndKingdom = true;
                    continue block253;
                }
                case 183: {
                    this.setSmearable(true);
                    continue block253;
                }
                case 184: {
                    this.isCarpet = true;
                    continue block253;
                }
                case 191: {
                    this.isMilk = true;
                    continue block253;
                }
                case 192: {
                    this.isCheese = true;
                    continue block253;
                }
                case 187: {
                    this.noImprove = true;
                    continue block253;
                }
                case 188: {
                    this.isTapestry = true;
                    continue block253;
                }
                case 190: {
                    this.isUnfinishedNoTake = true;
                    continue block253;
                }
                case 194: {
                    this.isOwnerTurnable = true;
                    continue block253;
                }
                case 195: {
                    this.isOwnerMoveable = true;
                    continue block253;
                }
                case 196: {
                    this.isUnfired = true;
                    continue block253;
                }
                case 199: {
                    this.isPlantable = true;
                    continue block253;
                }
                case 200: {
                    this.isPlantable = true;
                    this.isPlantOneAWeek = true;
                    continue block253;
                }
                case 201: {
                    this.isHitchTarget = true;
                    continue block253;
                }
                case 206: {
                    this.isPotable = true;
                    continue block253;
                }
                case 221: {
                    this.canBeGrownInPot = true;
                    continue block253;
                }
                case 209: {
                    this.isCooker = true;
                    continue block253;
                }
                case 208: {
                    this.isFoodGroup = true;
                    continue block253;
                }
                case 210: {
                    this.isCookingTool = true;
                    continue block253;
                }
                case 211: {
                    this.isRecipeItem = true;
                    continue block253;
                }
                case 207: {
                    this.isNoCreate = true;
                    continue block253;
                }
                case 212: {
                    this.usesFoodState = true;
                    continue block253;
                }
                case 213: {
                    this.canBeFermented = true;
                    continue block253;
                }
                case 214: {
                    this.canBeDistilled = true;
                    continue block253;
                }
                case 215: {
                    this.canBeSealed = true;
                    continue block253;
                }
                case 236: {
                    this.canBePegged = true;
                    continue block253;
                }
                case 217: {
                    this.canBeCookingOil = true;
                    continue block253;
                }
                case 216: {
                    this.useRealTemplateIcon = true;
                    continue block253;
                }
                case 218: {
                    this.hovers = true;
                    continue block253;
                }
                case 219: {
                    this.foodBonusHot = true;
                    continue block253;
                }
                case 220: {
                    this.foodBonusCold = true;
                    continue block253;
                }
                case 223: {
                    this.canBeRawWrapped = true;
                    continue block253;
                }
                case 222: {
                    this.canBePapyrusWrapped = true;
                    this.usesFoodState = true;
                    continue block253;
                }
                case 224: {
                    this.canBeClothWrapped = true;
                    continue block253;
                }
                case 225: {
                    this.surfaceonly = true;
                    continue block253;
                }
                case 226: {
                    this.isMushroom = true;
                    continue block253;
                }
                case 228: {
                    this.canShowRaw = true;
                    continue block253;
                }
                case 229: {
                    this.cannotBeSpellTarget = true;
                    continue block253;
                }
                case 230: {
                    this.isTrellis = true;
                    continue block253;
                }
                case 231: {
                    this.containsIngredientsOnly = true;
                    continue block253;
                }
                case 232: {
                    this.isComponentItem = true;
                    continue block253;
                }
                case 240: {
                    this.parentMustBeOnGround = true;
                    continue block253;
                }
                case 233: {
                    this.usesRealTemplate = true;
                    continue block253;
                }
                case 234: {
                    this.canLarder = true;
                    continue block253;
                }
                case 235: {
                    this.isRune = true;
                    this.isNotRuneable = true;
                    continue block253;
                }
                case 237: {
                    this.decayOnDeed = true;
                    continue block253;
                }
                case 238: {
                    this.isInsulated = true;
                    continue block253;
                }
                case 239: {
                    this.isGuardTower = true;
                    continue block253;
                }
                case 241: {
                    this.isRoadMarker = true;
                    continue block253;
                }
                case 242: {
                    this.isPaveable = true;
                    continue block253;
                }
                case 243: {
                    this.isCavePaveable = true;
                    continue block253;
                }
                case 244: {
                    this.isPlantable = true;
                    this.decorationWhenPlanted = true;
                    continue block253;
                }
                case 245: {
                    this.descIsName = true;
                    continue block253;
                }
                case 246: {
                    this.isNotRuneable = true;
                    continue block253;
                }
                case 247: {
                    this.showsSlopes = true;
                    continue block253;
                }
                case 248: {
                    this.isPluralName = true;
                    continue block253;
                }
                case 249: {
                    this.supportsSecondryColor = true;
                    continue block253;
                }
                case 250: {
                    this.isFishingReel = true;
                    continue block253;
                }
                case 251: {
                    this.isFishingLine = true;
                    continue block253;
                }
                case 252: {
                    this.isFishingFloat = true;
                    continue block253;
                }
                case 253: {
                    this.isFishingHook = true;
                    continue block253;
                }
                case 254: {
                    this.isFishingBait = true;
                    continue block253;
                }
                case 255: {
                    this.hasExtraData = true;
                    continue block253;
                }
                case 256: {
                    this.viewableSubItems = true;
                    continue block253;
                }
                case 259: {
                    this.viewableSubItems = true;
                    this.isContainerWithSubItems = true;
                    continue block253;
                }
                case 257: {
                    this.createsWithLock = true;
                    continue block253;
                }
                case 258: {
                    this.isBracelet = true;
                    continue block253;
                }
                default: {
                    if (!logger.isLoggable(Level.FINE)) continue block253;
                    logger.fine("Cannot assign type for: " + types[x]);
                }
            }
        }
        this.isMovingItem = this.isVehicle || this.draggable;
        this.setIsSharp();
        this.setIsOre();
        this.setIsShard();
    }

    private void setIsSharp() {
        if (this.isWeaponSlash()) {
            this.isSharp = true;
        }
        switch (this.getTemplateId()) {
            case 8: 
            case 25: 
            case 93: 
            case 121: 
            case 125: 
            case 126: 
            case 258: 
            case 267: 
            case 268: 
            case 269: 
            case 270: {
                this.isSharp = true;
                break;
            }
            default: {
                this.isSharp = false;
            }
        }
    }

    private void setIsOre() {
        switch (this.getTemplateId()) {
            case 38: 
            case 39: 
            case 40: 
            case 41: 
            case 42: 
            case 43: 
            case 207: 
            case 693: 
            case 697: {
                this.isOre = true;
                break;
            }
            default: {
                this.isOre = false;
            }
        }
    }

    private void setIsShard() {
        switch (this.getTemplateId()) {
            case 146: 
            case 770: 
            case 785: 
            case 1116: 
            case 1238: {
                this.isShard = true;
                break;
            }
            default: {
                this.isShard = false;
            }
        }
    }

    private void setImproveItem() {
        if (this.isDragonArmour) {
            this.improveItem = 371;
            if (this.getTemplateId() >= 474) {
                this.improveItem = 372;
            }
        }
    }

    public int getImproveItem() {
        return this.improveItem;
    }

    public long getDecayTime() {
        return this.decayTime;
    }

    public int getSizeX() {
        return this.centimetersX;
    }

    public final int getContainerSizeX() {
        if (this.usesSpecifiedContainerSizes) {
            return this.containerCentimetersX;
        }
        return this.getSizeX();
    }

    public int getSizeY() {
        return this.centimetersY;
    }

    public final int getContainerSizeY() {
        if (this.usesSpecifiedContainerSizes) {
            return this.containerCentimetersY;
        }
        return this.getSizeY();
    }

    public int getSizeZ() {
        return this.centimetersZ;
    }

    public final int getContainerSizeZ() {
        if (this.usesSpecifiedContainerSizes) {
            return this.containerCentimetersZ;
        }
        return this.getSizeZ();
    }

    public ItemTemplate setContainerSize(int x, int y, int z) {
        int[] sizes = new int[]{x, y, z};
        Arrays.sort(sizes);
        this.containerCentimetersX = sizes[0];
        this.containerCentimetersY = sizes[1];
        this.containerCentimetersZ = sizes[2];
        this.containerVolume = x * y * z;
        return this;
    }

    public final int getContainerVolume() {
        if (this.usesSpecifiedContainerSizes) {
            return this.containerVolume;
        }
        return this.getVolume();
    }

    public final boolean usesSpecifiedContainerSizes() {
        return this.usesSpecifiedContainerSizes;
    }

    public final ItemTemplate setDyeAmountGrams(int dyeOverrideAmount) {
        this.dyePrimaryAmountRequired = dyeOverrideAmount;
        return this;
    }

    public final ItemTemplate setDyeAmountGrams(int dyeOverridePrimary, int dyeOverrideSecondary) {
        this.dyePrimaryAmountRequired = dyeOverridePrimary;
        this.dyeSecondaryAmountRequired = dyeOverrideSecondary;
        return this;
    }

    public final int getDyePrimaryAmountGrams() {
        return this.dyePrimaryAmountRequired;
    }

    public final int getDyeSecondaryAmountGrams() {
        return this.dyeSecondaryAmountRequired;
    }

    public final ItemTemplate setSecondryItem(String secondryItemName, int dyeOverrideAmount) {
        this.secondaryItemName = secondryItemName;
        this.dyeSecondaryAmountRequired = dyeOverrideAmount;
        return this;
    }

    public final ItemTemplate setSecondryItem(String secondaryItemName) {
        this.secondaryItemName = secondaryItemName;
        this.dyeSecondaryAmountRequired = 0;
        return this;
    }

    public final String getSecondryItemName() {
        return this.secondaryItemName;
    }

    public ItemTemplate setNutritionValues(int calories, int carbs, int fats, int proteins) {
        this.calcNutritionValues = false;
        this.calories = (short)calories;
        this.carbs = (short)carbs;
        this.fats = (short)fats;
        this.proteins = (short)proteins;
        return this;
    }

    ItemTemplate setAlcoholStrength(int newAlcoholStrength) {
        this.alcoholStrength = newAlcoholStrength;
        this.isAlcohol = true;
        return this;
    }

    ItemTemplate setGrows(int growsTemplateId) {
        this.grows = growsTemplateId;
        return this;
    }

    public int getGrows() {
        if (this.grows == 0) {
            return this.templateId;
        }
        return this.grows;
    }

    ItemTemplate setCrushsTo(int toTemplateId) {
        this.crushsTo = toTemplateId;
        this.isCrushable = true;
        return this;
    }

    public int getCrushsTo() {
        return this.crushsTo;
    }

    ItemTemplate setHarvestsTo(int toTemplateId) {
        this.harvestTo = toTemplateId;
        this.isHarvestable = true;
        return this;
    }

    public boolean isHarvestable() {
        return this.isHarvestable;
    }

    public int getHarvestsTo() {
        return this.harvestTo;
    }

    ItemTemplate setPickSeeds(int seedTemplateId) {
        this.pickSeeds = seedTemplateId;
        this.hasSeeds = true;
        return this;
    }

    public int getPickSeeds() {
        return this.pickSeeds;
    }

    ItemTemplate setFoodGroup(int foodGroupTemplateId) {
        this.inFoodGroup = foodGroupTemplateId;
        return this;
    }

    public int getFoodGroup() {
        if (this.inFoodGroup > 0) {
            return this.inFoodGroup;
        }
        return this.getTemplateId();
    }

    ItemTemplate addContainerRestriction(boolean onlyOneOf, int ... itemTemplateId) {
        if (this.containerRestrictions == null) {
            this.containerRestrictions = new ArrayList();
        }
        this.containerRestrictions.add(new ContainerRestriction(onlyOneOf, itemTemplateId));
        return this;
    }

    ItemTemplate addContainerRestriction(boolean onlyOneOf, String emptySlotName, int ... itemTemplateId) {
        if (this.containerRestrictions == null) {
            this.containerRestrictions = new ArrayList();
        }
        this.containerRestrictions.add(new ContainerRestriction(onlyOneOf, emptySlotName, itemTemplateId));
        return this;
    }

    public ArrayList<ContainerRestriction> getContainerRestrictions() {
        return this.containerRestrictions;
    }

    ItemTemplate setInitialContainers(InitialContainer[] containers) {
        this.initialContainers = containers;
        return this;
    }

    public InitialContainer[] getInitialContainers() {
        return this.initialContainers;
    }

    public boolean calcNutritionValues() {
        return this.calcNutritionValues;
    }

    public short getCalories() {
        return this.calories;
    }

    public short getCarbs() {
        return this.carbs;
    }

    public short getFats() {
        return this.fats;
    }

    public short getProteins() {
        return this.proteins;
    }

    public int getVolume() {
        return this.volume;
    }

    public byte[] getBodySpaces() {
        return this.bodySpaces;
    }

    public int getTemplateId() {
        return this.templateId;
    }

    public short getBehaviourType() {
        return this.behaviourType;
    }

    public Behaviour getBehaviour() throws NoSuchBehaviourException {
        return Behaviours.getInstance().getBehaviour(this.behaviourType);
    }

    public int getWeightGrams() {
        return this.weight;
    }

    public final boolean isVehicle() {
        return this.isVehicle;
    }

    public final boolean isAlwaysBankable() {
        return this.alwaysBankable;
    }

    public boolean isHollow() {
        return this.hollow;
    }

    public boolean isWeaponSlash() {
        return this.weaponslash;
    }

    public boolean isShield() {
        return this.shield;
    }

    public boolean isCrude() {
        return this.isCrude;
    }

    public final boolean isCarpet() {
        return this.isCarpet;
    }

    public boolean isArmour() {
        return this.armour;
    }

    public boolean isBracelet() {
        return this.isBracelet;
    }

    public boolean isFood() {
        return this.food;
    }

    public boolean isFruit() {
        return this.fruit;
    }

    public boolean isMagic() {
        return this.magic;
    }

    public boolean isFieldTool() {
        return this.fieldtool;
    }

    public boolean isBodyPart() {
        return this.bodypart;
    }

    public boolean isBoat() {
        return this.isVehicle() && this.isFloating();
    }

    public boolean isPuppet() {
        return this.puppet;
    }

    public boolean isInventory() {
        return this.inventory;
    }

    public boolean isInventoryGroup() {
        return this.inventoryGroup;
    }

    public boolean isImproveUsingTypeAsMaterial() {
        return this.improveUsesTypeAsMaterial;
    }

    public boolean isMiningtool() {
        return this.miningtool;
    }

    public boolean isCarpentryTool() {
        return this.carpentrytool;
    }

    public boolean isSmithingTool() {
        return this.smithingtool;
    }

    public boolean isWeaponPierce() {
        return this.weaponpierce;
    }

    public boolean isWeaponCrush() {
        return this.weaponcrush;
    }

    public boolean isWeaponAxe() {
        return this.weaponaxe;
    }

    public boolean isWeaponSword() {
        return this.weaponsword;
    }

    public boolean isWeaponPolearm() {
        return this.weaponPolearm;
    }

    public boolean isWeaponKnife() {
        return this.weaponknife;
    }

    public boolean isWeaponMisc() {
        return this.weaponmisc;
    }

    public boolean isRechargeable() {
        return this.rechargeable;
    }

    public boolean isDiggingtool() {
        return this.diggingtool;
    }

    public boolean isSeed() {
        return this.seed;
    }

    public boolean isLiquid() {
        return this.liquid;
    }

    public boolean isLiquidCooking() {
        return this.liquidCooking;
    }

    public boolean isMelting() {
        return this.melting;
    }

    public boolean isMeat() {
        return this.meat;
    }

    public boolean isVegetable() {
        return this.vegetable;
    }

    public boolean isWood() {
        return this.wood;
    }

    public boolean isStone() {
        return this.stone;
    }

    public boolean isMetal() {
        return this.metal;
    }

    public boolean isNoTrade() {
        return this.notrade;
    }

    public boolean isLeather() {
        return this.leather;
    }

    public boolean isCloth() {
        return this.cloth;
    }

    public boolean isPottery() {
        return this.pottery;
    }

    public boolean isPlantedFlowerpot() {
        return this.plantedFlowerpot;
    }

    public boolean isNoTake() {
        return this.notake;
    }

    public boolean isNoImprove() {
        return this.noImprove;
    }

    public boolean isFlower() {
        return this.isFlower;
    }

    public boolean isLight() {
        return this.light;
    }

    public boolean isContainerLiquid() {
        return this.containerliquid;
    }

    public boolean isLiquidInflammable() {
        return this.liquidinflammable;
    }

    public boolean isWeaponMelee() {
        return this.weaponmelee;
    }

    public boolean isFish() {
        return this.fish;
    }

    public boolean isWeapon() {
        return this.weapon;
    }

    public boolean isTool() {
        return this.tool;
    }

    public boolean isLock() {
        return this.lock;
    }

    public boolean isIndestructible() {
        return this.indestructible;
    }

    public boolean isKey() {
        return this.key;
    }

    public boolean isNoDrop() {
        return this.nodrop;
    }

    public boolean isRepairable() {
        return this.repairable;
    }

    public boolean isTemporary() {
        return this.temporary;
    }

    public boolean isCombine() {
        return this.combine;
    }

    public boolean isLockable() {
        return this.lockable;
    }

    public boolean canHaveInscription() {
        return this.canHaveInscription;
    }

    public boolean hasData() {
        return this.hasdata;
    }

    public boolean hasExtraData() {
        return this.hasExtraData;
    }

    public boolean hasViewableSubItems() {
        return this.viewableSubItems;
    }

    public boolean isContainerWithSubItems() {
        return this.isContainerWithSubItems;
    }

    public boolean isOutsideOnly() {
        return this.outsideonly;
    }

    public boolean isSurfaceOnly() {
        return this.surfaceonly;
    }

    public boolean isCoin() {
        return this.coin;
    }

    public boolean isTurnable() {
        return this.turnable;
    }

    public final boolean isTapestry() {
        return this.isTapestry;
    }

    public final boolean isTransportable() {
        return this.isTransportable;
    }

    public boolean isDecoration() {
        return this.decoration;
    }

    public boolean decayOnDeed() {
        return this.decayOnDeed;
    }

    public boolean isFullprice() {
        return this.fullprice;
    }

    public boolean isNoRename() {
        return this.norename;
    }

    public boolean isTutorialItem() {
        return this.isTutorialItem;
    }

    public boolean isLownutrition() {
        return this.lownutrition;
    }

    public boolean isDraggable() {
        return this.draggable;
    }

    public boolean isVillageDeed() {
        return this.villagedeed;
    }

    public final boolean isBarding() {
        switch (this.getTemplateId()) {
            case 702: 
            case 703: 
            case 704: {
                return true;
            }
        }
        return false;
    }

    public final boolean isRope() {
        return this.getTemplateId() == 319 || this.getTemplateId() == 1029;
    }

    public boolean isHomesteadDeed() {
        return this.homesteaddeed;
    }

    public boolean isAlwaysPoll() {
        return this.alwayspoll;
    }

    public boolean isMissionItem() {
        if (this.notMissions) {
            return false;
        }
        if (this.isRiftItem || this.isCrude) {
            return false;
        }
        return this.bulk || this.newbieItem || this.missions;
    }

    public boolean isCombineCold() {
        return this.combineCold;
    }

    public final boolean isBulk() {
        return this.bulk;
    }

    public final boolean isUnfired() {
        return this.isUnfired;
    }

    public boolean isFloating() {
        return this.floating;
    }

    public boolean isButcheredItem() {
        return this.isButcheredItem;
    }

    public boolean isNoPut() {
        return this.isNoPut;
    }

    public boolean isLeadCreature() {
        return this.isLeadCreature;
    }

    public boolean isLeadMultipleCreatures() {
        return this.isLeadMultipleCreatures;
    }

    public final boolean isKingdomMarker() {
        return this.kingdomMarker;
    }

    public boolean isFire() {
        return this.isFire;
    }

    public boolean isDomainItem() {
        return this.domainItem;
    }

    public boolean isCreatureWearableOnly() {
        return this.wearableByCreaturesOnly;
    }

    public boolean isUseOnGroundOnly() {
        return this.useOnGroundOnly;
    }

    public boolean isHolyItem() {
        return this.holyItem;
    }

    public final boolean isBrazier() {
        return this.isBrazier;
    }

    public final boolean isPlantable() {
        return this.isPlantable;
    }

    public final boolean isPlantOneAWeeek() {
        return this.isPlantOneAWeek;
    }

    public String toPipeString() {
        StringBuilder lBuilder = new StringBuilder();
        lBuilder.append(this.templateId);
        lBuilder.append('|').append(this.size);
        lBuilder.append('|').append(this.name);
        lBuilder.append('|').append(this.plural);
        lBuilder.append('|').append(this.itemDescriptionSuperb);
        lBuilder.append('|').append(this.itemDescriptionNormal);
        lBuilder.append('|').append(this.itemDescriptionBad);
        lBuilder.append('|').append(this.itemDescriptionRotten);
        lBuilder.append('|').append(this.itemDescriptionLong);
        lBuilder.append('|').append(this.imageNumber);
        lBuilder.append('|').append(this.behaviourType);
        lBuilder.append('|').append(this.combatDamage);
        lBuilder.append('|').append(this.decayTime);
        lBuilder.append('|').append(this.centimetersX);
        lBuilder.append('|').append(this.centimetersY);
        lBuilder.append('|').append(this.centimetersZ);
        lBuilder.append('|').append(this.primarySkill);
        lBuilder.append('|').append(Arrays.toString(this.bodySpaces));
        lBuilder.append('|').append(this.modelName);
        lBuilder.append('|').append(this.difficulty);
        lBuilder.append('|').append(this.weight);
        lBuilder.append('|').append(Byte.toString(this.material));
        lBuilder.append('|').append(this.value);
        lBuilder.append('|').append(this.isPurchased);
        lBuilder.append('|').append(-1);
        return lBuilder.toString();
    }

    public String toString() {
        StringBuilder lBuilder = new StringBuilder();
        lBuilder.append("ItemTemplate[");
        lBuilder.append("ID: ").append(this.templateId);
        lBuilder.append(", size: ").append(this.size);
        lBuilder.append(", name: ").append(this.name);
        lBuilder.append(", plural: ").append(this.plural);
        lBuilder.append(", itemDescriptionSuperb: ").append(this.itemDescriptionSuperb);
        lBuilder.append(", itemDescriptionNormal: ").append(this.itemDescriptionNormal);
        lBuilder.append(", itemDescriptionBad: ").append(this.itemDescriptionBad);
        lBuilder.append(", itemDescriptionRotten: ").append(this.itemDescriptionRotten);
        lBuilder.append(", itemDescriptionLong: ").append(this.itemDescriptionLong);
        lBuilder.append(", imageNumber: ").append(this.imageNumber);
        lBuilder.append(", behaviourType: ").append(this.behaviourType);
        lBuilder.append(", combatDamage: ").append(this.combatDamage);
        lBuilder.append(", decayTime: ").append(this.decayTime);
        lBuilder.append(", centimetersX: ").append(this.centimetersX);
        lBuilder.append(", centimetersY: ").append(this.centimetersY);
        lBuilder.append(", centimetersZ: ").append(this.centimetersZ);
        lBuilder.append(", primarySkill: ").append(this.primarySkill);
        lBuilder.append(", bodySpaces: ").append(Arrays.toString(this.bodySpaces));
        lBuilder.append(", modelName: ").append(this.modelName);
        lBuilder.append(", difficulty: ").append(this.difficulty);
        lBuilder.append(", weight: ").append(this.weight);
        lBuilder.append(", material: ").append(Byte.toString(this.material));
        lBuilder.append(", value: ").append(this.value);
        lBuilder.append(", isPurchased: ").append(this.isPurchased);
        lBuilder.append(", armourType: ").append(-1);
        lBuilder.append("]");
        return lBuilder.toString();
    }

    @Override
    public int compareTo(ItemTemplate aItemTemplate) {
        return this.getName().compareTo(aItemTemplate.getName());
    }

    public boolean isSharp() {
        return this.isSharp;
    }

    public boolean isEquipmentSlot() {
        return this.isEquipmentSlot;
    }

    public boolean isMagicStaff() {
        return this.isMagicStaff;
    }

    public boolean isInstaDiscard() {
        return this.instaDiscard;
    }

    public void setInstaDiscard(boolean aInstaDiscard) {
        this.instaDiscard = aInstaDiscard;
    }

    public boolean isNoDiscard() {
        return this.noDiscard;
    }

    public final boolean isWarmachine() {
        return this.isWarmachine;
    }

    public void setNoDiscard(boolean aNoDiscard) {
        this.noDiscard = aNoDiscard;
    }

    public boolean isTent() {
        return this.isTent;
    }

    public void setTent(boolean aIsTent) {
        this.isTent = aIsTent;
    }

    public boolean isSmearable() {
        return this.isSmearable;
    }

    public void setSmearable(boolean aIsSmearable) {
        this.isSmearable = aIsSmearable;
    }

    public boolean isHitchTarget() {
        return this.isHitchTarget;
    }

    public boolean isRiftAltar() {
        return this.isRiftAltar;
    }

    public void setRiftAltar(boolean isRiftAltar) {
        this.isRiftAltar = isRiftAltar;
    }

    public boolean isRiftItem() {
        return this.isRiftItem;
    }

    public boolean isRiftStoneDeco() {
        return this.getTemplateId() == 1033 || this.getTemplateId() == 1034 || this.getTemplateId() == 1035 || this.getTemplateId() == 1036;
    }

    public boolean isRiftPlantDeco() {
        return this.getTemplateId() == 1041 || this.getTemplateId() == 1042 || this.getTemplateId() == 1043 || this.getTemplateId() == 1044;
    }

    public boolean isRiftCrystalDeco() {
        return this.getTemplateId() == 1037 || this.getTemplateId() == 1038 || this.getTemplateId() == 1039 || this.getTemplateId() == 1040;
    }

    public boolean isRiftLoot() {
        return this.isRiftLoot;
    }

    public void setRiftLoot(boolean isRiftLoot) {
        this.isRiftLoot = isRiftLoot;
    }

    public boolean isHasItemBonus() {
        return this.hasItemBonus;
    }

    public void setHasItemBonus(boolean hasItemBonus) {
        this.hasItemBonus = hasItemBonus;
    }

    public boolean isPotable() {
        return this.isPotable;
    }

    public boolean canBeGrownInPot() {
        return this.canBeGrownInPot;
    }

    public boolean usesFoodState() {
        return this.usesFoodState;
    }

    public boolean canBeFermented() {
        return this.canBeFermented;
    }

    public boolean canBeDistilled() {
        return this.canBeDistilled;
    }

    public boolean canBeSealed() {
        return this.canBeSealed;
    }

    public boolean canBePegged() {
        return this.canBePegged;
    }

    public boolean hovers() {
        return this.hovers;
    }

    public boolean hasFoodBonusWhenHot() {
        return this.foodBonusHot;
    }

    public boolean hasFoodBonusWhenCold() {
        return this.foodBonusCold;
    }

    public boolean canShowRaw() {
        return this.canShowRaw;
    }

    public boolean cannotBeSpellTarget() {
        return this.cannotBeSpellTarget;
    }

    public boolean isTrellis() {
        return this.isTrellis;
    }

    public boolean containsIngredientsOnly() {
        return this.containsIngredientsOnly;
    }

    public boolean isShelf() {
        return this.isComponentItem && this.parentMustBeOnGround;
    }

    public boolean usesRealTemplate() {
        return this.usesRealTemplate;
    }

    public boolean isCooker() {
        return this.isCooker;
    }

    public boolean isFoodGroup() {
        return this.isFoodGroup;
    }

    public boolean isCookingTool() {
        return this.isCookingTool;
    }

    public boolean isRecipeItem() {
        return this.isRecipeItem;
    }

    public boolean isNoCreate() {
        return this.isNoCreate;
    }

    public boolean isAlcohol() {
        return this.isAlcohol;
    }

    public boolean isCrushable() {
        return this.isCrushable;
    }

    public boolean hasSeeds() {
        return this.hasSeeds;
    }

    public boolean isHerb() {
        return this.herb;
    }

    public boolean isRoadMarker() {
        return this.isRoadMarker;
    }

    public boolean isPaveable() {
        return this.isPaveable;
    }

    public boolean isCavePaveable() {
        return this.isCavePaveable;
    }

    public boolean canBeCookingOil() {
        return this.canBeCookingOil;
    }

    public boolean canBePapyrusWrapped() {
        return this.canBePapyrusWrapped;
    }

    public boolean canBeRawWrapped() {
        return this.canBeRawWrapped;
    }

    public boolean canBeClothWrapped() {
        return this.canBeClothWrapped;
    }

    public boolean useRealTemplateIcon() {
        return this.useRealTemplateIcon;
    }

    public boolean isMilk() {
        return this.isMilk;
    }

    public boolean isSpice() {
        return this.spice;
    }

    public final boolean isNoNutrition() {
        return this.nonutrition;
    }

    public final boolean isLowNutrition() {
        return this.lownutrition;
    }

    public final boolean isMediumNutrition() {
        return this.mediumnutrition;
    }

    public final boolean isHighNutrition() {
        return this.highnutrition;
    }

    public final boolean isGoodNutrition() {
        return this.goodnutrition;
    }

    public boolean isFoodMaker() {
        return this.isFoodMaker;
    }

    public int getAlcoholStrength() {
        return this.alcoholStrength;
    }

    public boolean canLarder() {
        return this.canLarder;
    }

    public boolean isRune() {
        return this.isRune;
    }

    public boolean isInsulated() {
        return this.isInsulated;
    }

    public boolean isGuardTower() {
        return this.isGuardTower;
    }

    public boolean isParentMustBeOnGround() {
        return this.parentMustBeOnGround;
    }

    public boolean isComponentItem() {
        return this.isComponentItem;
    }

    public boolean isAlmanacContainer() {
        return this.templateId == 1127 || this.templateId == 1128;
    }

    public ItemTemplate setMaxItemCount(int count) {
        this.maxItemCount = count;
        return this;
    }

    public ItemTemplate setMaxItemWeight(int grams) {
        this.maxItemWeight = grams;
        return this;
    }

    public int getMaxItemCount() {
        return this.maxItemCount;
    }

    public int getMaxItemWeight() {
        return this.maxItemWeight;
    }

    public ItemTemplate setFragmentAmount(int count) {
        this.fragmentAmount = Math.min(127, count);
        return this;
    }

    public int getFragmentAmount() {
        return this.fragmentAmount;
    }

    public boolean isSaddleBags() {
        return this.templateId == 1333 || this.templateId == 1334;
    }

    public boolean doesShowSlopes() {
        return this.showsSlopes;
    }

    public boolean supportsSecondryColor() {
        return this.supportsSecondryColor;
    }

    public boolean doesCreateWithLock() {
        return this.createsWithLock;
    }

    public boolean isStatue() {
        return this.templateId == 402 || this.templateId == 399 || this.templateId == 400 || this.templateId == 1330 || this.templateId == 1323 || this.templateId == 1328 || this.templateId == 403 || this.templateId == 1325 || this.templateId == 811 || this.templateId == 742 || this.templateId == 1329 || this.templateId == 1327 || this.templateId == 398 || this.templateId == 401 || this.templateId == 1405 || this.templateId == 1407 || this.templateId == 1406 || this.templateId == 1408 || this.templateId == 1324 || this.templateId == 1326 || this.templateId == 1415 || this.templateId == 1416 || this.templateId == 1417 || this.templateId == 1418 || this.templateId == 1419 || this.templateId == 1420 || this.templateId == 1421 || this.templateId == 1430;
    }

    public boolean isMask() {
        return this.templateId == 977 || this.templateId == 973 || this.templateId == 978 || this.templateId == 1099 || this.templateId == 975 || this.templateId == 974 || this.templateId == 976 || this.templateId == 1321 || this.templateId == 1306;
    }

    public boolean isStorageRack() {
        return this.templateId == 1111 || this.templateId == 1315 || this.templateId == 1312 || this.templateId == 1110 || this.templateId == 1109 || this.templateId == 1108 || this.templateId == 1316 || this.templateId == 724 || this.templateId == 758 || this.templateId == 725;
    }

    public boolean isFishingReel() {
        return this.isFishingReel;
    }

    public boolean isFishingLine() {
        return this.isFishingLine;
    }

    public boolean isFishingFloat() {
        return this.isFishingFloat;
    }

    public boolean isFishingHook() {
        return this.isFishingHook;
    }

    public boolean isFishingBait() {
        return this.isFishingBait;
    }

    public boolean isMetalLump() {
        return this.templateId == 694 || this.templateId == 221 || this.templateId == 223 || this.templateId == 47 || this.templateId == 698 || this.templateId == 44 || this.templateId == 46 || this.templateId == 49 || this.templateId == 837 || this.templateId == 45 || this.templateId == 205 || this.templateId == 220 || this.templateId == 48 || this.templateId == 1411;
    }
}

