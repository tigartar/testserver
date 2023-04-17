/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector2f;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmHarvestables;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ArtifactBehaviour;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.CargoTransportationMethods;
import com.wurmonline.server.behaviours.CreatureBehaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.behaviours.TerraformingTask;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.behaviours.TileTreeBehaviour;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.combat.Arrows;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateCreator;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.creatures.Traits;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.ChickenCoops;
import com.wurmonline.server.items.CoinDbStrings;
import com.wurmonline.server.items.ContainerRestriction;
import com.wurmonline.server.items.DbStrings;
import com.wurmonline.server.items.FrozenItemDbStrings;
import com.wurmonline.server.items.Ingredient;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.ItemData;
import com.wurmonline.server.items.ItemDbStrings;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMealData;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.Recipes;
import com.wurmonline.server.items.RecipesByPlayer;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.items.TempStates;
import com.wurmonline.server.items.TradingWindow;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.meshgen.IslandAdder;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.NewKingQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.statistics.ChallengePointEnum;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.utils.CoordUtils;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.ItemTransfer;
import com.wurmonline.server.utils.logging.ItemTransferDatabaseLogger;
import com.wurmonline.server.villages.DeadVillage;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.EffectConstants;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.util.MaterialUtilities;
import com.wurmonline.shared.util.MulticolorLineSegment;
import com.wurmonline.shared.util.StringUtilities;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Item
implements ItemTypes,
TimeConstants,
MiscConstants,
CounterTypes,
EffectConstants,
ItemMaterials,
SoundNames,
MonetaryConstants,
ProtoConstants,
Comparable<Item>,
Permissions.IAllow,
PermissionsPlayerList.ISettings,
CreatureTemplateIds {
    long id = -10L;
    protected boolean surfaced = true;
    ItemTemplate template;
    private static final Logger logger = Logger.getLogger(Item.class.getName());
    public static final int FISHING_REEL = 0;
    public static final int FISHING_LINE = 1;
    public static final int FISHING_FLOAT = 2;
    public static final int FISHING_HOOK = 3;
    public static final int FISHING_BAIT = 4;
    private static final ItemTransferDatabaseLogger itemLogger = new ItemTransferDatabaseLogger("Item transfer logger", 500);
    boolean isBusy = false;
    @Nullable
    Set<Effect> effects;
    @Nullable
    Set<Long> keys;
    @Nullable
    Set<Creature> watchers;
    Set<Item> items;
    public long lastMaintained;
    float qualityLevel;
    float originalQualityLevel;
    int sizeX;
    int sizeY;
    int sizeZ;
    float posX;
    float posY;
    float posZ;
    float rotation;
    long parentId = -10L;
    long ownerId = -10L;
    public int zoneId = -10;
    @Nullable
    InscriptionData inscription;
    String name = "";
    String description = "";
    short place;
    boolean locked = false;
    float damage;
    ItemData data;
    @Nullable
    TradingWindow tradeWindow = null;
    int weight = 0;
    short temperature = (short)200;
    byte material;
    long lockid = -10L;
    public static final int maxSizeMod = 4;
    int price = 0;
    int tempChange = 0;
    byte bless = 0;
    boolean banked = false;
    public byte enchantment = 0;
    public long lastOwner = -10L;
    public boolean deleted = false;
    private int ticksSinceLastDecay = 0;
    public boolean mailed = false;
    @Nonnull
    private static final Effect[] emptyEffects = new Effect[0];
    static final Item[] emptyItems = new Item[0];
    public byte newLayer = (byte)-128;
    private static long lastPolledWhiteAltar = 0L;
    private static long lastPolledBlackAltar = 0L;
    public boolean transferred = false;
    private static final int REPLACE_SEED = 102539;
    private static final char dotchar = '.';
    static final float visibleDecayLimit = 50.0f;
    static final float visibleWornLimit = 25.0f;
    public long lastParentId = -10L;
    public boolean hatching = false;
    byte mailTimes = 0;
    byte auxbyte = 0;
    public long creationDate;
    public byte creationState = 0;
    public int realTemplate = -10;
    public boolean wornAsArmour = false;
    public int color = -1;
    public int color2 = -1;
    public boolean female = false;
    public String creator = "";
    int creatorMaxLength = 40;
    private static final String lit = " (lit)";
    private static final String modelit = ".lit";
    public boolean hidden = false;
    public byte rarity = 0;
    public static final long TRASHBIN_TICK = 3600L;
    private static final long TREBUCHET_RELOAD_TIME = 120L;
    public static final int MAX_CONTAINED_ITEMS_ITEMCRATE_SMALL = 150;
    public static final int MAX_CONTAINED_ITEMS_ITEMCRATE_LARGE = 300;
    public long onBridge = -10L;
    Permissions permissions = new Permissions();
    public static final byte FOOD_STATE_RAW = 0;
    public static final byte FOOD_STATE_FRIED = 1;
    public static final byte FOOD_STATE_GRILLED = 2;
    public static final byte FOOD_STATE_BOILED = 3;
    public static final byte FOOD_STATE_ROASTED = 4;
    public static final byte FOOD_STATE_STEAMED = 5;
    public static final byte FOOD_STATE_BAKED = 6;
    public static final byte FOOD_STATE_COOKED = 7;
    public static final byte FOOD_STATE_CANDIED = 8;
    public static final byte FOOD_STATE_CHOCOLATE_COATED = 9;
    public static final byte FOOD_STATE_CHOPPED_BIT = 16;
    public static final byte FOOD_STATE_MASHED_BIT = 32;
    public static final byte FOOD_STATE_WRAPPED_BIT = 64;
    public static final byte FOOD_STATE_FRESH_BIT = -128;
    public static final byte FOOD_STATE_CHOPPED_MASK = -17;
    public static final byte FOOD_STATE_MASHED_MASK = -33;
    public static final byte FOOD_STATE_WRAPPED_MASK = -65;
    public static final byte FOOD_STATE_FRESH_MASK = 127;
    private int internalVolume = 0;
    private long whenRented = 0L;
    private boolean isLightOverride = false;
    private short warmachineWinches = 0;
    public static final long DRAG_AFTER_RAM_TIME = 30000L;
    public long lastRammed = 0L;
    public long lastRamUser = -10L;
    private long lastPolled = 0L;
    private boolean wagonerWagon = false;
    private boolean replacing = false;
    private boolean isSealedOverride = false;
    private String whatHappened = "";
    private long wasBrandedTo = -10L;
    private long lastAuxPoll;
    protected boolean placedOnParent = false;
    protected boolean isChained = false;

    Item() {
    }

    Item(long wurmId, String aName, ItemTemplate aTemplate, float aQLevel, byte aMaterial, byte aRarity, long bridgeId, @Nullable String aCreator) throws IOException {
        this.id = wurmId == -10L ? Item.getNextWurmId(aTemplate) : wurmId;
        this.template = aTemplate;
        this.qualityLevel = aQLevel;
        this.originalQualityLevel = aQLevel;
        this.weight = aTemplate.getWeightGrams();
        this.name = aName;
        this.material = aMaterial;
        this.rarity = aRarity;
        this.onBridge = bridgeId;
        if (this.isNamed() && aCreator != null && aCreator.length() > 0) {
            this.creator = aCreator.substring(0, Math.min(aCreator.length(), this.creatorMaxLength));
        }
        if (!aTemplate.isBodyPart()) {
            this.create(this.qualityLevel, WurmCalendar.currentTime);
        }
        Items.putItem(this);
    }

    Item(String aName, short aPlace, ItemTemplate aTemplate, float aQualityLevel, byte aMaterial, byte aRarity, long bridgeId, String aCreator) throws IOException {
        this(-10L, aName, aTemplate, aQualityLevel, aMaterial, aRarity, bridgeId, aCreator);
        this.setPlace(aPlace);
    }

    Item(String aName, ItemTemplate aTemplate, float aQualityLevel, float aPosX, float aPosY, float aPosZ, float aRotation, byte aMaterial, byte aRarity, long bridgeId, String aCreator) throws IOException {
        this(-10L, aName, aTemplate, aQualityLevel, aMaterial, aRarity, bridgeId, aCreator);
        this.setPos(aPosX, aPosY, aPosZ, aRotation, bridgeId);
    }

    @Override
    public int compareTo(Item otherItem) {
        return this.getName().compareTo(otherItem.getName());
    }

    public static DbStrings getDbStrings(int templateNum) {
        if (templateNum >= 10 && templateNum <= 19) {
            Item.logWarn("THIS HAPPENS AT ", new Exception());
            return null;
        }
        if (templateNum >= 50 && templateNum <= 61) {
            return CoinDbStrings.getInstance();
        }
        return ItemDbStrings.getInstance();
    }

    public static DbStrings getDbStringsByWurmId(long wurmId) {
        if (WurmId.getType(wurmId) == 19) {
            Item.logWarn("THIS HAPPENS AT ", new Exception());
            return null;
        }
        if (WurmId.getType(wurmId) == 20) {
            return CoinDbStrings.getInstance();
        }
        return ItemDbStrings.getInstance();
    }

    private static long getNextWurmId(ItemTemplate template) {
        if (template.isTemporary()) {
            return WurmId.getNextTempItemId();
        }
        if (template.isCoin()) {
            return WurmId.getNextCoinId();
        }
        return WurmId.getNextItemId();
    }

    public final boolean mayLockItems() {
        return this.isLock() && this.getTemplateId() != 167 && this.getTemplateId() != 252 && this.getTemplateId() != 568;
    }

    public final boolean isBoatLock() {
        return this.getTemplateId() == 568;
    }

    public final boolean isBrazier() {
        return this.template.isBrazier();
    }

    public final boolean isAnchor() {
        return this.getTemplateId() == 565;
    }

    public final boolean isTrap() {
        return this.template.isTrap;
    }

    public final boolean isDisarmTrap() {
        return this.template.isDisarmTrap;
    }

    public final boolean isOre() {
        return this.template.isOre;
    }

    public final boolean isShard() {
        return this.template.isShard;
    }

    public final boolean isBeingWorkedOn() {
        if (this.isBusy) {
            return true;
        }
        if (this.isHollow() && this.items != null) {
            for (Item item : this.items) {
                if (!item.isBeingWorkedOn()) continue;
                return true;
            }
        }
        return this.isBusy;
    }

    public final boolean combine(Item target, Creature performer) throws FailedException {
        if (this.equals(target)) {
            return false;
        }
        Item parent = null;
        if (this.parentId != -10L && target.getParentId() != this.parentId) {
            try {
                parent = Items.getItem(this.parentId);
                if (!parent.hasSpaceFor(target.getVolume())) {
                    throw new FailedException("The container could not contain the combined items.");
                }
            }
            catch (NoSuchItemException nsi) {
                Item.logInfo("Strange, combining item without parent: " + this.id);
                throw new FailedException("The container could not contain the combined items.");
            }
        }
        if (this.ownerId == -10L || target.getOwnerId() == -10L) {
            throw new FailedException("You need to carry both items to combine them.");
        }
        if (!this.isCombineCold() && this.isMetal() && target.getTemplateId() != 204 && performer.getPower() == 0 && (this.temperature < 3500 || target.getTemperature() < 3500)) {
            throw new FailedException("Metal needs to be glowing hot to be combined.");
        }
        if (this.getTemplateId() == target.getTemplateId() && this.isCombine()) {
            if (this.getMaterial() == target.getMaterial() || this.isWood() && target.isWood()) {
                int allWeight = this.getWeightGrams() + target.getWeightGrams();
                if (this.isLiquid() && !parent.hasSpaceFor(allWeight)) {
                    throw new FailedException("The " + parent.getName() + " cannot contain that much " + this.getName() + ".");
                }
                float maxW = ItemFactory.isMetalLump(this.getTemplateId()) ? Math.max(this.template.getWeightGrams() * 4 * 4 * 4, 64000) : this.template.getWeightGrams() * 4 * 4 * 4;
                if ((float)allWeight <= maxW) {
                    if (parent != null) {
                        try {
                            parent.dropItem(this.id, false);
                        }
                        catch (NoSuchItemException nsi) {
                            Item.logWarn("This item doesn't exist: " + this.id, nsi);
                            return false;
                        }
                    }
                    float newQl = (this.getCurrentQualityLevel() * (float)this.getWeightGrams() + target.getCurrentQualityLevel() * (float)target.getWeightGrams()) / (float)allWeight;
                    if (allWeight > 0) {
                        if (target.isColor() && this.isColor()) {
                            this.setColor(WurmColor.mixColors(this.color, this.getWeightGrams(), target.color, target.getWeightGrams(), newQl));
                        }
                        if (this.getRarity() > target.getRarity()) {
                            if (Server.rand.nextInt(allWeight) > this.getWeightGrams() / 4) {
                                this.setRarity(target.getRarity());
                            }
                        } else if (target.getRarity() > this.getRarity() && Server.rand.nextInt(allWeight) > target.getWeightGrams() / 4) {
                            this.setRarity(target.getRarity());
                        }
                        this.setWeight(allWeight, false);
                        this.setQualityLevel(newQl);
                        this.setDamage(0.0f);
                        Items.destroyItem(target.getWurmId());
                        if (parent != null && !parent.insertItem(this)) {
                            try {
                                long powner = parent.getOwner();
                                Creature pown = Server.getInstance().getCreature(powner);
                                pown.getInventory().insertItem(this);
                            }
                            catch (NoSuchCreatureException nsc) {
                                Item.logWarn(this.getName() + ", " + this.getWurmId() + nsc.getMessage(), nsc);
                            }
                            catch (NoSuchPlayerException nsc) {
                                Item.logWarn(this.getName() + ", " + this.getWurmId() + nsc.getMessage(), nsc);
                            }
                            catch (NotOwnedException no) {
                                VolaTile tile = Zones.getOrCreateTile((int)this.getPosX() >> 2, (int)this.getPosY() >> 2, this.isOnSurface());
                                tile.addItem(this, false, false);
                                Item.logWarn("The combined " + this.getName() + " was created on ground. This should not happen.");
                            }
                        }
                    } else {
                        Items.destroyItem(this.id);
                    }
                    return true;
                }
                throw new FailedException("The combined item would be too large to handle.");
            }
            throw new FailedException("The items are of different materials.");
        }
        return false;
    }

    public boolean isKingdomFlag() {
        return this.template.isKingdomFlag;
    }

    @Nullable
    public InscriptionData getInscription() {
        return this.inscription;
    }

    public String getHoverText() {
        return "";
    }

    @Override
    public String getName() {
        return this.getName(true);
    }

    public String getName(boolean showWrapped) {
        String stoSend;
        StringBuilder builder;
        block120: {
            block128: {
                block127: {
                    block126: {
                        block125: {
                            block124: {
                                int templateId;
                                block123: {
                                    String description;
                                    block122: {
                                        block121: {
                                            Item reel;
                                            builder = new StringBuilder();
                                            templateId = this.template.getTemplateId();
                                            description = this.getDescription();
                                            stoSend = "";
                                            if (this.descIsName() && !description.isEmpty()) {
                                                builder.append('\"');
                                                builder.append(description);
                                                builder.append('\"');
                                                return builder.toString();
                                            }
                                            if (templateId == 1300) {
                                                if (this.getAuxData() == 1) {
                                                    return "faintly glowing " + this.name;
                                                }
                                                return "brightly glowing " + this.name;
                                            }
                                            if (templateId == 1423 && this.getAuxBit(7)) {
                                                return "small " + this.name + " token";
                                            }
                                            if (templateId == 1422) {
                                                return this.name + " hidden cache";
                                            }
                                            if (templateId == 1307) {
                                                if (this.getData1() > 0) {
                                                    if (this.getRealTemplate() != null) {
                                                        return this.getRealTemplate().sizeString + this.getRealTemplate().getName() + " " + this.name + " [" + this.getAuxData() + "/" + this.getRealTemplate().getFragmentAmount() + "]";
                                                    }
                                                } else {
                                                    builder.append("unidentified ");
                                                    if (this.getRealTemplate() != null && this.getAuxData() >= 65) {
                                                        if (this.getRealTemplate().isWeapon()) {
                                                            builder.append("weapon ");
                                                        } else if (this.getRealTemplate().isArmour()) {
                                                            builder.append("armour ");
                                                        } else if (this.getRealTemplate().isTool()) {
                                                            builder.append("tool ");
                                                        } else if (this.getRealTemplate().isStatue()) {
                                                            builder.append("statue ");
                                                        } else if (this.getRealTemplate().isHollow()) {
                                                            builder.append("container ");
                                                        } else if (this.getRealTemplate().isRiftLoot()) {
                                                            builder.append("rift ");
                                                        } else if (this.getRealTemplate().isMetal()) {
                                                            builder.append("metal ");
                                                        } else if (this.getRealTemplate().isWood()) {
                                                            builder.append("wooden ");
                                                        }
                                                    }
                                                    builder.append(this.name);
                                                    return builder.toString();
                                                }
                                            }
                                            if (templateId == 854) {
                                                return description.isEmpty() ? "sign" : description;
                                            }
                                            if (this.isLight() && this.getTemplateId() != 37) {
                                                if (this.name.endsWith(lit)) {
                                                    this.name = this.name.replace(lit, "");
                                                }
                                                if (this.isOnFire() && !this.name.endsWith(lit) && !this.isLightOverride) {
                                                    stoSend = lit;
                                                }
                                            }
                                            if (this.template.getTemplateId() == 1243 && this.isOnFire()) {
                                                stoSend = " (smoking)";
                                            }
                                            if (templateId == 1346 && (reel = this.getFishingReel()) != null) {
                                                if (reel.getTemplateId() == 1372) {
                                                    return "light fishing rod";
                                                }
                                                if (reel.getTemplateId() == 1373) {
                                                    return "medium fishing rod";
                                                }
                                                if (reel.getTemplateId() == 1374) {
                                                    return "deep water fishing rod";
                                                }
                                                if (reel.getTemplateId() == 1375) {
                                                    return "professional fishing rod";
                                                }
                                            }
                                            if ((this.isWind() || this.template.isKingdomFlag) && this.getTemplateId() != 487) {
                                                if ((this.getTemplateId() == 579 || this.getTemplateId() == 578 || this.getTemplateId() == 999) && this.getKingdom() != 0) {
                                                    builder.append(Kingdoms.getNameFor(this.getKingdom()));
                                                    builder.append(' ');
                                                }
                                                builder.append(this.template.getName());
                                                return builder.toString();
                                            }
                                            if (this.template.isRune() && this.getRealTemplate() != null) {
                                                switch (this.getRealTemplate().getTemplateId()) {
                                                    case 1104: {
                                                        builder.append("wooden");
                                                        break;
                                                    }
                                                    case 1103: {
                                                        builder.append("crystal");
                                                        break;
                                                    }
                                                    case 1102: {
                                                        builder.append("stone");
                                                        break;
                                                    }
                                                    default: {
                                                        builder.append("unknown");
                                                    }
                                                }
                                                builder.append(' ');
                                            }
                                            if (this.isBulkItem()) {
                                                int nums = this.getBulkNums();
                                                if (nums > 0) {
                                                    try {
                                                        ItemTemplate it = ItemTemplateFactory.getInstance().getTemplate(this.getRealTemplateId());
                                                        builder.append(it.sizeString);
                                                        if (this.getAuxData() != 0 && it.usesFoodState()) {
                                                            builder.append(this.getFoodAuxByteName(it, false, true));
                                                        }
                                                        if (!this.getActualName().equalsIgnoreCase("bulk item")) {
                                                            builder.append(this.getActualName());
                                                        } else if (nums > 1) {
                                                            builder.append(it.getPlural());
                                                        } else {
                                                            builder.append(it.getName());
                                                        }
                                                        return builder.toString();
                                                    }
                                                    catch (NoSuchTemplateException nst) {
                                                        Item.logWarn(this.getWurmId() + " bulk nums=" + this.getBulkNums() + " but template is " + this.getBulkTemplateId());
                                                    }
                                                }
                                            } else {
                                                if (this.isInventoryGroup()) {
                                                    if (!description.isEmpty()) {
                                                        this.name = description;
                                                        this.setDescription("");
                                                    }
                                                    return this.name;
                                                }
                                                if (templateId == 853 && this.getItemCount() > 0) {
                                                    Item ship = this.getItemsAsArray()[0];
                                                    stoSend = " [" + ship.getName() + "]";
                                                }
                                            }
                                            if (this.name.equals("")) {
                                                if (templateId == 179) {
                                                    try {
                                                        ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(AdvancedCreationEntry.getTemplateId(this));
                                                        builder.append("unfinished ");
                                                        builder.append(temp.sizeString);
                                                        builder.append(temp.getName());
                                                        return builder.toString();
                                                    }
                                                    catch (NoSuchTemplateException nst) {
                                                        Item.logWarn(nst.getMessage(), nst);
                                                    }
                                                } else if (templateId == 177) {
                                                    int lData = this.getData1();
                                                    try {
                                                        if (lData != -1) {
                                                            ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(lData);
                                                            builder.append("Pile of ");
                                                            builder.append(temp.sizeString);
                                                            builder.append(temp.getName());
                                                            return builder.toString();
                                                        }
                                                    }
                                                    catch (NoSuchTemplateException nst) {
                                                        Item.logInfo("Inconsistency: " + lData + " does not exist as templateid.");
                                                    }
                                                } else {
                                                    if (templateId == 918 || templateId == 917 || templateId == 1017) {
                                                        builder.append(this.template.getName());
                                                        return builder.toString();
                                                    }
                                                    if (this.isWood()) {
                                                        builder.append(this.template.sizeString);
                                                        builder.append(this.template.getName());
                                                        return builder.toString();
                                                    }
                                                    if ((this.isSign() || this.isFlag()) && this.getTemplateId() != 835) {
                                                        if (this.isPlanted() && !description.isEmpty()) {
                                                            builder.append('\"');
                                                            builder.append(description);
                                                            builder.append('\"');
                                                            return builder.toString();
                                                        }
                                                    } else if (templateId == 518) {
                                                        if (!description.isEmpty()) {
                                                            builder.append("Colossus of ");
                                                            builder.append(LoginHandler.raiseFirstLetter(description));
                                                            return builder.toString();
                                                        }
                                                    } else if (this.isDecoration() || this.isMetal()) {
                                                        try {
                                                            ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
                                                            builder.append(temp.sizeString);
                                                            builder.append(this.template.getName());
                                                            builder.append(stoSend);
                                                            return builder.toString();
                                                        }
                                                        catch (NoSuchTemplateException nst) {
                                                            Item.logInfo("Inconsistency: " + templateId + " does not exist as templateid.");
                                                        }
                                                    } else if (this.isBodyPart()) {
                                                        if (templateId == 11) {
                                                            if (this.place == 3) {
                                                                builder.append("left ");
                                                            } else {
                                                                builder.append("right ");
                                                            }
                                                            builder.append(this.template.getName());
                                                            return builder.toString();
                                                        }
                                                        if (templateId == 14) {
                                                            if (this.place == 13) {
                                                                builder.append("left ");
                                                            } else {
                                                                builder.append("right ");
                                                            }
                                                            builder.append(this.template.getName());
                                                            return builder.toString();
                                                        }
                                                        if (templateId == 15) {
                                                            if (this.place == 15) {
                                                                builder.append("left ");
                                                            } else {
                                                                builder.append("right ");
                                                            }
                                                            builder.append(this.template.getName());
                                                            return builder.toString();
                                                        }
                                                    } else if (this.isButcheredItem()) {
                                                        try {
                                                            CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(this.getData2());
                                                            builder.append(temp.getName());
                                                            builder.append(' ');
                                                            builder.append(this.template.getName());
                                                            return builder.toString();
                                                        }
                                                        catch (Exception nst) {
                                                            Item.logInfo(this.getWurmId() + " unknown butchered creature: " + nst.getMessage(), nst);
                                                        }
                                                    }
                                                }
                                                builder.append(this.template.sizeString);
                                                builder.append(this.template.getName());
                                                return builder.toString();
                                            }
                                            if ((!this.isSign() || this.getTemplateId() == 835) && !this.isFlag()) break block121;
                                            if (this.isPlanted() && !description.isEmpty()) {
                                                builder.append('\"');
                                                builder.append(description);
                                                builder.append('\"');
                                                return builder.toString();
                                            }
                                            break block120;
                                        }
                                        if (templateId != 518) break block122;
                                        if (!description.isEmpty()) {
                                            builder.append('\"');
                                            builder.append("Colossus of ");
                                            builder.append(LoginHandler.raiseFirstLetter(description));
                                            builder.append('\"');
                                            return builder.toString();
                                        }
                                        break block120;
                                    }
                                    if (this.isVehicle() && !this.isChair() && !description.isEmpty() || this.isChair() && !description.isEmpty() && this.getParentId() == -10L) {
                                        builder.append('\"');
                                        builder.append(description);
                                        builder.append('\"');
                                        return builder.toString() + stoSend;
                                    }
                                    if (templateId != 654 || this.getAuxData() <= 0) break block123;
                                    if (this.getBless() != null) {
                                        builder.append("Active ");
                                    } else {
                                        builder.append("Passive ");
                                    }
                                    break block120;
                                }
                                if (templateId != 1239 && templateId != 1175) break block124;
                                if (WurmCalendar.isSeasonWinter()) {
                                    if (this.hasQueen()) {
                                        builder.append("dormant ");
                                    } else {
                                        builder.append("empty ");
                                    }
                                } else if (this.hasTwoQueens()) {
                                    builder.append("noisy ");
                                } else if (this.hasQueen()) {
                                    builder.append("active ");
                                } else {
                                    builder.append("empty ");
                                }
                                break block120;
                            }
                            if (!this.usesFoodState()) break block125;
                            builder.append(this.getFoodAuxByteName(this.template, false, showWrapped));
                            break block120;
                        }
                        if (this.getTemplateId() != 729 || this.getAuxData() <= 0) break block126;
                        builder.append("birthday ");
                        break block120;
                    }
                    if (!this.isSealedByPlayer()) break block127;
                    builder.append("sealed ");
                    for (Item item : this.getItemsAsArray()) {
                        if (item.isLiquid() && item.isDye()) {
                            int red = WurmColor.getColorRed(item.getColor());
                            int green = WurmColor.getColorGreen(item.getColor());
                            int blue = WurmColor.getColorBlue(item.getColor());
                            stoSend = " [" + item.getName() + "] (" + red + "/" + green + "/" + blue + ")";
                        } else {
                            if (!item.isLiquid()) continue;
                            stoSend = " [" + item.getName() + "]";
                        }
                        break block120;
                    }
                    break block120;
                }
                if (this.getTemplateId() != 1162 || this.getParentId() == -10L) break block128;
                ItemTemplate rt = this.getRealTemplate();
                if (rt == null) break block120;
                stoSend = " [" + rt.getName().replace(" ", "") + "]";
                break block120;
            }
            if (this.getTemplateId() == 748 || this.getTemplateId() == 1272) {
                switch (this.getAuxData()) {
                    case 0: {
                        InscriptionData ins = this.getInscription();
                        if (ins != null && ins.hasBeenInscribed()) {
                            builder.append("inscribed ");
                            break;
                        }
                        builder.append("blank ");
                        break;
                    }
                    case 1: {
                        builder.append("recipe ");
                        break;
                    }
                    case 2: {
                        builder.append("waxed ");
                        break;
                    }
                    default: {
                        WurmHarvestables.Harvestable harvestable = WurmHarvestables.getHarvestable(this.getAuxData() - 8);
                        if (harvestable == null) {
                            builder.append("ruined ");
                        }
                        break;
                    }
                }
            }
        }
        if (this.isTrellis()) {
            FoliageAge age = FoliageAge.fromByte(this.getLeftAuxData());
            stoSend = " (" + age.getAgeName() + ")";
        }
        builder.append(this.name);
        builder.append(stoSend);
        return builder.toString();
    }

    public String getFoodAuxByteName(ItemTemplate it, boolean full, boolean showWrapped) {
        StringBuilder builder = new StringBuilder();
        if (this.getTemplateId() == 128) {
            if (this.isSalted()) {
                builder.append("salty ");
            }
            return builder.toString();
        }
        if (this.isFresh()) {
            builder.append("fresh ");
        } else if (this.getDamage() > 90.0f) {
            builder.append("rotten ");
        } else if (this.getDamage() > 75.0f) {
            builder.append("moldy ");
        }
        if (full) {
            if (this.isSalted()) {
                builder.append("salted ");
            }
            if (this.isFish() && this.isUnderWeight()) {
                builder.append("underweight ");
            }
        }
        if (this.isWrapped()) {
            if (it.canBeDistilled()) {
                builder.append("undistilled ");
            } else if (showWrapped) {
                builder.append("wrapped ");
            }
        }
        if (full && builder.length() == 0) {
            builder.append("(none) ");
        }
        switch (this.getRightAuxData()) {
            case 1: {
                builder.append("fried ");
                break;
            }
            case 2: {
                builder.append("grilled ");
                break;
            }
            case 3: {
                builder.append("boiled ");
                break;
            }
            case 4: {
                builder.append("roasted ");
                break;
            }
            case 5: {
                builder.append("steamed ");
                break;
            }
            case 6: {
                builder.append("baked ");
                break;
            }
            case 7: {
                builder.append("cooked ");
                break;
            }
            case 8: {
                builder.append("candied ");
                break;
            }
            case 9: {
                builder.append("chocolate coated ");
                break;
            }
            default: {
                if (it.canShowRaw()) {
                    builder.append("raw ");
                    break;
                }
                if (!full) break;
                builder.append("(raw) ");
            }
        }
        if (this.isChopped()) {
            if (it.isHerb() || it.isVegetable() || it.isFish() || it.isMushroom) {
                builder.append("chopped ");
            } else if (it.isMeat()) {
                builder.append("diced ");
            } else if (it.isSpice()) {
                builder.append("ground ");
            } else if (it.canBeFermented()) {
                builder.append("unfermented ");
            } else if (it.getTemplateId() == 1249) {
                builder.append("whipped ");
            } else {
                builder.append("zombified ");
            }
        }
        if (this.isMashedBitSet()) {
            if (it.isMeat()) {
                builder.append("minced ");
            } else if (it.isVegetable()) {
                builder.append("mashed ");
            } else if (it.canBeFermented()) {
                builder.append("fermenting ");
            } else if (this.getTemplateId() == 1249) {
                builder.append("clotted ");
            }
        }
        return builder.toString();
    }

    public String getActualName() {
        return this.name;
    }

    public boolean isNamePlural() {
        return this.template.isNamePlural();
    }

    public final String getSignature() {
        if (this.creator != null && this.creator.length() > 0 && this.getTemplateId() != 651) {
            String toReturn = this.creator;
            int ql = (int)this.getCurrentQualityLevel();
            if (ql < 20) {
                return "";
            }
            if (ql < 90) {
                toReturn = Item.obscureWord(this.creator, ql);
            }
            return toReturn;
        }
        return this.creator;
    }

    public static String obscureWord(String word, int ql) {
        int containfactor = ql / 10;
        char[] cword = word.toCharArray();
        Random r = new Random();
        r.setSeed(102539L);
        for (int x = 0; x < word.length(); ++x) {
            cword[x] = r.nextInt(containfactor) > 0 ? word.charAt(x) : (char)46;
        }
        return String.valueOf(cword);
    }

    public String getNameWithGenus() {
        return StringUtilities.addGenus(this.getName(), this.isNamePlural());
    }

    public final float getNutritionLevel() {
        boolean hasBonus = this.template.hasFoodBonusWhenCold() && this.temperature < 300 || this.template.hasFoodBonusWhenHot() && this.temperature > 1000;
        float ql = this.getCurrentQualityLevel();
        if (this.isHighNutrition()) {
            return 0.56f + ql / 300.0f + (hasBonus ? 0.09f : 0.0f);
        }
        if (this.isGoodNutrition()) {
            return 0.4f + ql / 500.0f + (hasBonus ? 0.1f : 0.0f);
        }
        if (this.isMediumNutrition()) {
            return 0.3f + ql / 1000.0f + (hasBonus ? 0.1f : 0.0f);
        }
        if (this.isLowNutrition()) {
            return 0.1f + ql / 1000.0f + (hasBonus ? 0.1f : 0.0f);
        }
        return 0.01f + ql / 1000.0f + (hasBonus ? 0.05f : 0.0f);
    }

    public final int getTowerModel() {
        Kingdom k;
        byte kingdomTemplateId = this.getAuxData();
        if ((this.getAuxData() < 0 || this.getAuxData() > 4) && (k = Kingdoms.getKingdom(this.getAuxData())) != null) {
            kingdomTemplateId = k.getTemplate();
        }
        if (kingdomTemplateId == 3) {
            return 430;
        }
        if (kingdomTemplateId == 2) {
            return 528;
        }
        if (kingdomTemplateId == 4) {
            return 638;
        }
        return 384;
    }

    public final String getModelName() {
        StringBuilder builder = new StringBuilder();
        int templateId = this.template.getTemplateId();
        if (templateId == 177) {
            int lData = this.getData1();
            try {
                if (lData != -1) {
                    builder.append(ItemTemplateFactory.getInstance().getTemplate(lData).getName());
                    builder.append(".");
                    builder.append(Item.getMaterialString(this.getMaterial()));
                    StringBuilder b2 = new StringBuilder();
                    b2.append(this.template.getModelName());
                    b2.append(builder.toString().replaceAll(" ", ""));
                    return b2.toString();
                }
            }
            catch (NoSuchTemplateException nst) {
                Item.logInfo("Inconsistency: " + lData + " does not exist as templateid.");
            }
        } else {
            if (this.isDragonArmour()) {
                builder.append(this.getTemplate().getModelName());
                String matString = MaterialUtilities.getMaterialString(this.getMaterial()) + ".";
                builder.append(matString);
                String text = this.getDragonColorNameByColor(this.getColor());
                builder.append(text);
                return builder.toString();
            }
            if (templateId == 854) {
                builder.append(this.getTemplate().getModelName());
                String text = this.getAuxData() + ".";
                builder.append(text);
                return builder.toString();
            }
            if (templateId == 385) {
                builder.append("model.fallen.");
                builder.append(TileTreeBehaviour.getTreenameForMaterial(this.getMaterial()));
                if (this.auxbyte >= 100) {
                    builder.append(".animatedfalling");
                }
                builder.append(".seasoncycle");
                return builder.toString();
            }
            if (templateId == 386) {
                try {
                    ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate);
                    if (this.template.wood) {
                        builder.append(temp.getModelName());
                        builder.append("unfinished.");
                        builder.append(Item.getMaterialString(this.getMaterial()));
                        return builder.toString();
                    }
                    builder.append(temp.getModelName());
                    builder.append("unfinished.");
                    builder.append(Item.getMaterialString(this.getMaterial()));
                    return builder.toString();
                }
                catch (NoSuchTemplateException nst) {
                    Item.logWarn(this.realTemplate + ": " + nst.getMessage(), nst);
                }
            } else if (templateId == 179) {
                try {
                    ItemTemplate temp;
                    int tempmodel = AdvancedCreationEntry.getTemplateId(this);
                    if (tempmodel == 384) {
                        tempmodel = this.getTowerModel();
                    }
                    if ((temp = ItemTemplateFactory.getInstance().getTemplate(tempmodel)).isTapestry()) {
                        builder.append("model.furniture.tapestry.unfinished");
                        return builder.toString();
                    }
                    if (temp.wood) {
                        builder.append(temp.getModelName());
                        builder.append("unfinished.");
                        if (this.isVisibleDecay()) {
                            if (this.damage >= 50.0f) {
                                builder.append("decayed.");
                            } else if (this.damage >= 25.0f) {
                                builder.append("worn.");
                            }
                        }
                        builder.append(Item.getMaterialString(this.getMaterial()));
                        return builder.toString();
                    }
                    builder.append(temp.getModelName());
                    builder.append("unfinished.");
                    if (this.isVisibleDecay()) {
                        if (this.damage >= 50.0f) {
                            builder.append("decayed.");
                        } else if (this.damage >= 25.0f) {
                            builder.append("worn.");
                        }
                    }
                    builder.append(Item.getMaterialString(this.getMaterial()));
                    return builder.toString();
                }
                catch (NoSuchTemplateException nst) {
                    Item.logWarn(this.realTemplate + ": " + nst.getMessage(), nst);
                }
            } else {
                if (templateId == 1307) {
                    builder.append(this.template.getModelName());
                    if (this.getData1() > 0) {
                        if (this.getMaterial() == 93) {
                            builder.append("iron");
                        } else if (this.getMaterial() == 94 || this.getMaterial() == 95) {
                            builder.append("steel");
                        } else {
                            builder.append(Item.getMaterialString(this.getMaterial()));
                        }
                    } else {
                        builder.append("unidentified.");
                    }
                    return builder.toString();
                }
                if (templateId == 1346) {
                    builder.append(this.template.getModelName());
                    Item reel = this.getFishingReel();
                    if (reel != null) {
                        if (reel.getTemplateId() == 1372) {
                            builder.append("light.");
                        } else if (reel.getTemplateId() == 1373) {
                            builder.append("medium.");
                        } else if (reel.getTemplateId() == 1374) {
                            builder.append("deepwater.");
                        } else if (reel.getTemplateId() == 1375) {
                            builder.append("professional.");
                        }
                    }
                    builder.append(Item.getMaterialString(this.getMaterial()));
                    return builder.toString();
                }
                if (templateId == 272) {
                    try {
                        Kingdom k;
                        CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(this.getData1());
                        builder.append(this.template.getModelName());
                        builder.append(temp.getCorpsename());
                        if (this.getDescription().length() > 0) {
                            if (this.getDescription().contains("[")) {
                                String desc = this.getDescription().replace(" ", "");
                                desc = desc.substring(0, desc.indexOf("["));
                                builder.append(desc);
                            } else {
                                builder.append(this.getDescription().replace(" ", ""));
                            }
                            builder.append(".");
                        }
                        if (this.isButchered()) {
                            builder.append("butchered.");
                        }
                        if (this.female) {
                            builder.append("female.");
                        }
                        if ((k = Kingdoms.getKingdom(this.getKingdom())) != null && k.getTemplate() != this.getKingdom()) {
                            builder.append(Kingdoms.getSuffixFor(k.getTemplate()));
                        }
                        builder.append(Kingdoms.getSuffixFor(this.getKingdom()));
                        builder.append(WurmCalendar.getSpecialMapping(false));
                        return builder.toString();
                    }
                    catch (NoSuchCreatureTemplateException temp) {}
                } else {
                    if (templateId == 853 || this.isWagonerWagon() || templateId == 1410) {
                        builder.append(this.getTemplate().getModelName());
                        if (this.isWagonerWagon()) {
                            builder.append("wagoner.");
                        }
                        if (this.getItemCount() > 0) {
                            builder.append("loaded.");
                        } else {
                            builder.append("unloaded.");
                        }
                        builder.append(WurmCalendar.getSpecialMapping(false));
                        builder.append(Item.getMaterialString(this.getMaterial()));
                        return builder.toString();
                    }
                    if (templateId == 651 || templateId == 1097 || templateId == 1098) {
                        builder.append(this.getTemplate().getModelName());
                        switch (this.auxbyte) {
                            case 0: {
                                builder.append("green");
                                break;
                            }
                            case 1: {
                                builder.append("blue");
                                break;
                            }
                            case 2: {
                                builder.append("striped");
                                break;
                            }
                            case 3: {
                                builder.append("candy");
                                break;
                            }
                            case 4: {
                                builder.append("holly");
                                break;
                            }
                            default: {
                                builder.append("green");
                            }
                        }
                        return builder.toString();
                    }
                    if (this.isSign()) {
                        builder.append(this.getTemplate().getModelName());
                        if (this.isVisibleDecay()) {
                            if (this.damage >= 50.0f) {
                                builder.append("decayed.");
                            } else if (this.damage >= 25.0f) {
                                builder.append("worn.");
                            }
                        }
                        if (this.getTemplateId() == 656) {
                            builder.append(this.auxbyte);
                            builder.append('.');
                        }
                        builder.append(Item.getMaterialString(this.getMaterial()));
                        return builder.toString();
                    }
                    if (templateId == 521) {
                        try {
                            CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(this.getData1());
                            builder.append(this.template.getModelName());
                            builder.append(temp.getCorpsename());
                            builder.append(WurmCalendar.getSpecialMapping(false));
                            return builder.toString();
                        }
                        catch (NoSuchCreatureTemplateException temp) {}
                    } else if (templateId == 387) {
                        try {
                            ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate);
                            if (this.template.wood) {
                                builder.append(temp.getModelName());
                                if (this.isVisibleDecay()) {
                                    if (this.damage >= 50.0f) {
                                        builder.append("decayed.");
                                    } else if (this.damage >= 25.0f) {
                                        builder.append("worn.");
                                    }
                                }
                                builder.append(Item.getMaterialString(this.getMaterial()));
                                return builder.toString();
                            }
                            builder.append(temp.getModelName());
                            builder.append(Item.getMaterialString(this.getMaterial()));
                            return builder.toString();
                        }
                        catch (NoSuchTemplateException nst) {
                            Item.logWarn(this.realTemplate + ": " + nst.getMessage(), nst);
                        }
                    } else {
                        if (templateId == 791) {
                            builder.append(this.template.getModelName());
                            builder.append(Item.getMaterialString(this.getMaterial()));
                            if (WurmCalendar.isChristmas()) {
                                builder.append(".red");
                            } else {
                                builder.append(".grey");
                            }
                            return builder.toString();
                        }
                        if (templateId == 518) {
                            builder.append(this.template.getModelName());
                            builder.append(Item.getMaterialString(this.getMaterial()));
                            builder.append(WurmCalendar.getSpecialMapping(true));
                            builder.append(".");
                            builder.append(this.getDescription().toLowerCase());
                            return builder.toString().replaceAll(" ", "");
                        }
                        if (templateId == 538) {
                            builder.append(this.template.getModelName());
                            if (King.getKing((byte)2) == null) {
                                builder.append("occupied.");
                            }
                            return builder.toString();
                        }
                        if (this.isItemSpawn()) {
                            builder.append(this.template.getModelName());
                            builder.append(this.getAuxData());
                            return builder.toString();
                        }
                    }
                }
            }
        }
        if (this.isWind() || this.template.isKingdomFlag || this.isProtectionTower()) {
            builder.append(this.template.getModelName());
            if (this.getKingdom() != 0) {
                builder.append(Kingdoms.getSuffixFor(this.getKingdom()));
            }
            return builder.toString();
        }
        if (this.isTent() || this.template.useMaterialAndKingdom) {
            builder.append(this.template.getModelName());
            builder.append(Item.getMaterialString(this.getMaterial()));
            builder.append(".");
            if (this.getKingdom() != 0) {
                builder.append(Kingdoms.getSuffixFor(this.getKingdom()));
            }
            return builder.toString();
        }
        if (this.template.templateId == 850) {
            builder.append(this.template.getModelName());
            if (this.getData1() != 0) {
                builder.append(Kingdoms.getSuffixFor((byte)this.getData1()));
            }
            builder.append(WurmCalendar.getSpecialMapping(true));
            builder.append(Item.getMaterialString(this.getMaterial()));
            return builder.toString();
        }
        if (this.isFire()) {
            builder.append(this.template.getModelName());
            String cook = "";
            boolean foundCook = false;
            String meat = "";
            boolean foundMeat = false;
            String spit = "";
            for (Item i : this.getItems()) {
                if (i.isFoodMaker() && !foundCook) {
                    cook = i.getConcatName();
                    foundCook = true;
                }
                if (!(i.getTemplateId() != 368 && i.getTemplateId() != 92 || foundMeat)) {
                    meat = "meat";
                    foundMeat = true;
                }
                if (i.getTemplateId() == 369 && !foundMeat) {
                    meat = "fish.fillet";
                    foundMeat = true;
                }
                if (i.isFish() && !foundMeat) {
                    meat = "fish";
                    foundMeat = true;
                }
                if (!i.getTemplate().getModelName().contains(".spit.")) continue;
                spit = i.getTemplate().getModelName().substring(11);
                if (!i.isRoasted()) continue;
                spit = spit + "roasted.";
            }
            if (spit.length() > 0) {
                builder.append(spit);
            } else if (foundCook) {
                builder.append(cook);
            } else if (foundMeat) {
                builder.append(meat);
                builder.append(".");
            }
            if (!this.isOnFire()) {
                builder.append("unlit");
            }
            return builder.toString();
        }
        if (this.getTemplateId() == 1301) {
            builder.append(this.template.getModelName());
            switch (this.getAuxData()) {
                case 1: 
                case 2: 
                case 11: 
                case 16: {
                    builder.append("meat.");
                    break;
                }
                case 3: {
                    builder.append("fish.fillet.");
                    break;
                }
                case 4: 
                case 5: 
                case 14: 
                case 19: {
                    builder.append("fish.");
                    break;
                }
                case 6: 
                case 7: {
                    builder.append("fryingpan.");
                    break;
                }
                case 8: 
                case 9: 
                case 10: {
                    builder.append("potterybowl.");
                    break;
                }
                case 12: 
                case 13: {
                    builder.append("spit.pig.");
                    break;
                }
                case 15: {
                    builder.append("spit.lamb.");
                    break;
                }
                case 17: 
                case 18: {
                    builder.append("spit.pig.roasted.");
                    break;
                }
                case 20: {
                    builder.append("spit.lamb.roasted.");
                }
            }
            if (!this.isOnFire()) {
                builder.append("unlit");
            }
            return builder.toString();
        }
        if (this.isRoadMarker()) {
            builder.append(this.template.getModelName());
            if (this.template.templateId == 1114) {
                int possibleRoutes = MethodsHighways.numberOfSetBits(this.getAuxData());
                if (possibleRoutes == 1) {
                    builder.append("red.");
                } else if (possibleRoutes == 2) {
                    if (Routes.isCatseyeUsed(this)) {
                        builder.append("green.");
                    } else {
                        builder.append("blue.");
                    }
                }
            }
            return builder.toString();
        }
        if (this.template.templateId == 1342) {
            builder.append(this.template.getModelName());
            if (this.isPlanted()) {
                builder.append("planted.");
            }
            builder.append(WurmCalendar.getSpecialMapping(false));
            builder.append(Item.getMaterialString(this.getMaterial()) + ".");
            return builder.toString();
        }
        if (this.template.wood) {
            builder.append(this.template.getModelName());
            if (this.getTemplateId() == 1432) {
                boolean chicks = false;
                boolean eggs = false;
                for (Item item : this.getAllItems(true)) {
                    if (item.getTemplateId() == 1436) {
                        chicks = !item.isEmpty(true);
                    }
                    if (item.getTemplateId() != 1433) continue;
                    eggs = !item.isEmpty(true);
                }
                if (eggs && chicks) {
                    builder.append("chicken.egg.");
                }
                if (!eggs && chicks) {
                    builder.append("chicken.");
                }
                if (eggs && !chicks) {
                    builder.append("egg.");
                }
                if (!eggs && !chicks) {
                    builder.append("empty.");
                }
                if (this.isUnfinished()) {
                    builder.append("unfinished.");
                }
                if (this.damage >= 60.0f) {
                    builder.append("decayed.");
                }
            }
            if (this.getTemplateId() == 1311) {
                if (this.isEmpty(true) && !this.isUnfinished()) {
                    this.setAuxData((byte)0);
                }
                switch (this.getAuxData()) {
                    case 0: {
                        builder.append("empty.");
                        break;
                    }
                    case 64: {
                        builder.append("horse.");
                        break;
                    }
                    case 82: {
                        builder.append("bison.");
                        break;
                    }
                    case 83: {
                        builder.append("hellhorse.");
                        break;
                    }
                    case 3: {
                        builder.append("cow.");
                        break;
                    }
                    case 49: {
                        builder.append("bull.");
                        break;
                    }
                    case 102: {
                        builder.append("ram.");
                        break;
                    }
                    case 65: {
                        builder.append("foal.");
                        break;
                    }
                    case 96: {
                        builder.append("sheep.");
                        break;
                    }
                    case 117: {
                        builder.append("hellhorse.foal.");
                        break;
                    }
                    default: {
                        builder.append("generic.");
                    }
                }
                if (this.isEmpty(true)) {
                    builder.append("empty.");
                }
                if (this.isUnfinished()) {
                    builder.append("unfinished.");
                }
                if (this.damage >= 60.0f) {
                    builder.append("decayed.");
                }
            }
            if ((templateId == 1309 || this.isCrate()) && (this.isSealedOverride() || this.isSealedByPlayer())) {
                builder.append("sealed.");
            }
            if (this.isBulkContainer() || this.getTemplateId() == 670) {
                if (!this.isCrate()) {
                    if (this.isFull()) {
                        builder.append("full.");
                    } else {
                        builder.append("empty.");
                    }
                } else if (this.getItemCount() > 0) {
                    builder.append("full.");
                } else {
                    builder.append("empty.");
                }
            }
            if (templateId == 724 || templateId == 725 || templateId == 758 || templateId == 759 || this.isBarrelRack() || templateId == 1312 || templateId == 1309 || templateId == 1315 || templateId == 1393) {
                if (this.isEmpty(false)) {
                    builder.append("empty.");
                } else {
                    builder.append("full.");
                }
            }
            if (templateId == 580 && this.isMerchantAbsentOrEmpty()) {
                builder.append("empty.");
            }
            if (!(templateId != 1239 && templateId != 1175 || this.getAuxData() <= 0 || WurmCalendar.isSeasonWinter())) {
                builder.append("queen.");
            }
            if (this.isHarvestable()) {
                builder.append("harvestable.");
            }
            if (this.isVisibleDecay()) {
                if (this.damage >= 50.0f) {
                    builder.append("decayed.");
                } else if (this.damage >= 25.0f) {
                    builder.append("worn.");
                }
            }
            if (this.damage >= 80.0f && templateId == 321) {
                builder.append("decay.");
            }
            if (templateId == 1396 && this.isPlanted()) {
                builder.append("planted.");
                if (this.isOnFire()) {
                    builder.append("lit.");
                }
            }
            builder.append(WurmCalendar.getSpecialMapping(false));
            builder.append(Item.getMaterialString(this.getMaterial()) + ".");
            return builder.toString();
        }
        if (this.isDuelRing()) {
            builder.append(this.template.getModelName());
            builder.append(Item.getMaterialString(this.getMaterial()));
            builder.append(".");
            if (this.getKingdom() != 0) {
                builder.append(Kingdoms.getSuffixFor(this.getKingdom()));
            }
            return builder.toString();
        }
        if (this.getTemplateId() == 742) {
            String hotaModel = "model.decoration.statue.hota.";
            builder.append("model.decoration.statue.hota.");
            switch (this.getAuxData() % 10) {
                case 0: {
                    if (this.getData1() == 1) {
                        builder.append("femalefightinganaconda.");
                        break;
                    }
                    builder.append("dogsfightingboar.");
                    break;
                }
                case -1: 
                case 1: {
                    builder.append("wolffightingbison.");
                    break;
                }
                case -2: 
                case 2: {
                    builder.append("deer.");
                    break;
                }
                case -3: 
                case 3: {
                    builder.append("bearfightingbull.");
                    break;
                }
                case -4: 
                case 4: {
                    builder.append("blackdragon.");
                    break;
                }
                case -5: 
                case 5: {
                    builder.append("ladylake.");
                    break;
                }
                case -6: 
                case 6: {
                    builder.append("nogump.");
                    break;
                }
                case -7: 
                case 7: {
                    builder.append("manfightingbear.");
                    break;
                }
                case -8: 
                case 8: {
                    builder.append("soldemon.");
                    break;
                }
                case -9: 
                case 9: {
                    builder.append("scorpion.");
                    break;
                }
                default: {
                    builder.append("dogsfightingboar.");
                }
            }
            builder.append(WurmCalendar.getSpecialMapping(false));
            builder.append(Item.getMaterialString(this.getMaterial()));
            return builder.toString();
        }
        if (this.getTemplateId() == 821 || this.getTemplateId() == 822) {
            builder.append(this.template.getModelName());
            if (this.damage >= 50.0f) {
                builder.append("decayed");
            }
            return builder.toString();
        }
        if (this.getTemplateId() == 302) {
            builder.append(this.template.getModelName());
            if (this.getName().equalsIgnoreCase("Black bear fur")) {
                builder.append("blackbear");
            } else if (this.getName().equalsIgnoreCase("Brown bear fur")) {
                builder.append("brownbear");
            } else if (this.getName().equalsIgnoreCase("Black wolf fur")) {
                builder.append("wolf");
            } else {
                builder.append(Item.getMaterialString(this.getMaterial()));
            }
            builder.append(".");
            return builder.toString();
        }
        if (this.getTemplateId() == 1162) {
            builder.append(this.template.getModelName());
            ItemTemplate rt = this.getRealTemplate();
            if (rt != null) {
                builder.append(rt.getName().replace(" ", ""));
                int age = this.getAuxData() & 0x7F;
                if (age == 0) {
                    builder.append(".0");
                } else if (age < 5) {
                    builder.append(".1");
                } else if (age < 10) {
                    builder.append(".2");
                } else if (age < 65) {
                    builder.append(".3");
                } else if (age < 75) {
                    builder.append(".4");
                } else if (age < 95) {
                    builder.append(".5");
                } else {
                    builder.append(".6");
                }
            }
            return builder.toString();
        }
        builder.append(this.template.getModelName());
        String rtName = "";
        if ((long)this.getRealTemplateId() != -10L && !this.isLight()) {
            rtName = this.getRealTemplate().getName() + ".".replace(" ", "");
        }
        if (this.usesFoodState()) {
            switch (this.getRightAuxData()) {
                case 1: {
                    builder.append("fried.");
                    break;
                }
                case 2: {
                    builder.append("grilled.");
                    break;
                }
                case 3: {
                    builder.append("boiled.");
                    break;
                }
                case 4: {
                    builder.append("roasted.");
                    break;
                }
                case 5: {
                    builder.append("steamed.");
                    break;
                }
                case 6: {
                    builder.append("baked.");
                    break;
                }
                case 7: {
                    builder.append("cooked.");
                    break;
                }
                case 8: {
                    builder.append("candied.");
                    break;
                }
                case 9: {
                    builder.append("chocolate.");
                }
            }
            if (this.isChoppedBitSet()) {
                if (this.isHerb() || this.isVegetable() || this.isFish() || this.template.isMushroom) {
                    builder.append("chopped.");
                } else if (this.isMeat()) {
                    builder.append("diced.");
                } else if (this.isSpice()) {
                    builder.append("ground.");
                } else if (this.canBeFermented()) {
                    builder.append("unfermented.");
                } else if (this.getTemplateId() == 1249) {
                    builder.append("whipped.");
                } else {
                    builder.append("zombified.");
                }
            } else if (this.isMashedBitSet()) {
                if (this.isMeat()) {
                    builder.append("minced.");
                } else if (this.isVegetable()) {
                    builder.append("mashed.");
                } else if (this.canBeFermented()) {
                    builder.append("fermenting.");
                } else if (this.getTemplateId() == 1249) {
                    builder.append("clotted.");
                } else if (this.isFish()) {
                    builder.append("underweight.");
                }
            }
            if (this.isWrappedBitSet()) {
                if (this.canBeDistilled()) {
                    builder.append("undistilled.");
                } else {
                    builder.append("wrapped.");
                }
            }
            if (this.isFreshBitSet()) {
                if (this.isHerb() || this.isSpice()) {
                    builder.append("fresh.");
                }
                if (this.isFish()) {
                    builder.append("live.");
                }
            }
            builder.append(rtName);
            builder.append(Item.getMaterialString(this.getMaterial()));
        } else if (this.getTemplateId() == 1281) {
            builder.append(rtName);
            builder.append(Item.getMaterialString(this.getMaterial()));
        } else if (this.getTemplateId() == 729) {
            if (this.getAuxData() > 0) {
                builder.append("birthday.");
            }
            builder.append(rtName);
            builder.append(Item.getMaterialString(this.getMaterial()));
        } else {
            builder.append(rtName);
            builder.append(Item.getMaterialString(this.getMaterial()));
            if ((templateId == 178 || templateId == 180 || this.isFireplace() || this.isBrazier() || templateId == 1178 || templateId == 1301) && this.isOnFire()) {
                builder.append(modelit);
            } else if (templateId == 1243 && this.isOnFire()) {
                builder.append(".smoke");
            }
        }
        return builder.toString();
    }

    public boolean isFull() {
        return this.getFreeVolume() < this.getVolume() / 2;
    }

    private boolean isMerchantAbsentOrEmpty() {
        try {
            TilePos tilePos = this.getTilePos();
            Zone zone = Zones.getZone(tilePos, this.surfaced);
            VolaTile tile = zone.getTileOrNull(tilePos);
            if (tile == null) {
                Item.logWarn("No tile found in zone.");
                return true;
            }
            for (Creature creature : tile.getCreatures()) {
                if (!creature.isNpcTrader()) continue;
                Shop shop = creature.getShop();
                if (shop != null) {
                    return shop.getOwnerId() != -10L && shop.getNumberOfItems() == 0;
                }
                break;
            }
        }
        catch (NoSuchZoneException nsze) {
            Item.logWarn(nsze.getMessage(), nsze);
        }
        return true;
    }

    public final void setBusy(boolean busy) {
        this.isBusy = busy;
        if (this.getTemplateId() == 1344 || this.getTemplateId() == 1346) {
            this.setIsNoPut(busy);
            Item[] fishingItems = this.getFishingItems();
            if (fishingItems[0] != null) {
                fishingItems[0].setBusy(busy);
                fishingItems[0].setIsNoPut(busy);
            }
            if (fishingItems[1] != null) {
                fishingItems[1].setBusy(busy);
                fishingItems[1].setIsNoPut(busy);
            }
            if (fishingItems[2] != null) {
                fishingItems[2].setBusy(busy);
            }
            if (fishingItems[3] != null) {
                fishingItems[3].setBusy(busy);
                fishingItems[3].setIsNoPut(busy);
            }
            if (fishingItems[4] != null) {
                fishingItems[4].setBusy(busy);
            }
        }
    }

    public final String getConcatName() {
        return this.template.getConcatName();
    }

    public final boolean isBusy() {
        return this.isBusy || this.tradeWindow != null;
    }

    private float getDifficulty() {
        return this.template.getDifficulty();
    }

    public boolean hasPrimarySkill() {
        return this.template.hasPrimarySkill();
    }

    public final int getPrimarySkill() throws NoSuchSkillException {
        return this.template.getPrimarySkill();
    }

    public final byte getAuxData() {
        if (this.getTemplateId() == 621 && this.auxbyte == 0 && this.getItemCount() > 0) {
            for (Item i : this.getItems()) {
                if (i.getTemplateId() == 1333) {
                    return 1;
                }
                if (i.getTemplateId() != 1334) continue;
                return 2;
            }
        }
        return this.auxbyte;
    }

    public final byte getActualAuxData() {
        return this.auxbyte;
    }

    public final short getTemperature() {
        return this.temperature;
    }

    public final boolean isPurchased() {
        return this.template.isPurchased();
    }

    public void checkSaveDamage() {
    }

    public final void addEffect(Effect effect) {
        if (this.effects == null) {
            this.effects = new HashSet<Effect>();
        }
        if (!this.effects.contains(effect)) {
            this.effects.add(effect);
        }
    }

    private void deleteEffect(Effect effect) {
        if (this.effects != null && effect != null) {
            this.effects.remove(effect);
            EffectFactory.getInstance().deleteEffect(effect.getId());
            if (this.effects.isEmpty()) {
                this.effects = null;
            }
        }
    }

    public final void deleteAllEffects() {
        if (this.effects != null) {
            for (Effect toremove : this.effects) {
                if (toremove == null) continue;
                EffectFactory.getInstance().deleteEffect(toremove.getId());
            }
        }
        this.effects = null;
    }

    @Nonnull
    public final Effect[] getEffects() {
        if (this.effects != null) {
            return this.effects.toArray(new Effect[this.effects.size()]);
        }
        return emptyEffects;
    }

    private float getMaterialRepairTimeMod() {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (this.getMaterial()) {
                case 56: {
                    return 0.9f;
                }
                case 31: {
                    return 0.975f;
                }
                case 10: {
                    return 1.075f;
                }
                case 57: {
                    return 0.95f;
                }
                case 7: {
                    return 1.05f;
                }
                case 12: {
                    return 1.1f;
                }
                case 67: {
                    return 0.95f;
                }
                case 9: {
                    return 0.975f;
                }
                case 34: {
                    return 1.025f;
                }
                case 13: {
                    return 1.05f;
                }
            }
        }
        return 1.0f;
    }

    public final short getRepairTime(Creature mender) {
        int time = Short.MAX_VALUE;
        Skills skills = mender.getSkills();
        Skill repair = null;
        try {
            repair = skills.getSkill(10035);
        }
        catch (NoSuchSkillException nss) {
            repair = skills.learn(10035, 1.0f);
        }
        if (repair == null) {
            return (short)time;
        }
        float cq = this.getCurrentQualityLevel();
        float diffcq = this.originalQualityLevel - cq;
        float weightmod = 1.0f;
        double diff = this.template.getDifficulty();
        if (this.realTemplate > 0) {
            diff = this.getRealTemplate().getDifficulty();
        }
        if (this.getWeightGrams() > 100000) {
            weightmod = Math.min(3, this.getWeightGrams() / 100000);
        }
        time = (int)Math.max(20.0, (double)((this.damage + diffcq) * weightmod / 4.0f) * (100.0 - repair.getChance(diff, null, 0.0)));
        time = (int)((float)time * this.getMaterialRepairTimeMod());
        time = Math.min(Short.MAX_VALUE, time);
        return (short)time;
    }

    public final double repair(@Nonnull Creature mender, short aTimeleft, float initialDamage) {
        float timeleft = (float)Math.max(1.0, Math.floor((float)aTimeleft / 10.0f));
        Skills skills = mender.getSkills();
        Skill repair = skills.getSkillOrLearn(10035);
        double power = repair.skillCheck(this.getDifficulty(), 0.0, false, 1.0f);
        float cq = this.getCurrentQualityLevel();
        float diffcq = Math.max(this.originalQualityLevel, this.qualityLevel) - cq;
        float runeModifier = 1.0f;
        if (this.getSpellEffects() != null) {
            runeModifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_REPAIRQL);
        }
        float newOrigcq = this.getQualityLevel() - (float)((double)diffcq * (100.0 - power) / 125.0) / timeleft / ((float)this.getRarity() + 1.0f) * runeModifier;
        this.setQualityLevel(Math.max(1.0f, newOrigcq));
        this.setOriginalQualityLevel(Math.max(1.0f, newOrigcq));
        float oldDamage = this.getDamage();
        this.setDamage(this.damage - initialDamage / timeleft);
        this.sendUpdate();
        if (this.isVisibleDecay() && (this.damage < 50.0f && oldDamage >= 50.0f || this.damage < 25.0f && oldDamage >= 25.0f)) {
            this.updateModelNameOnGroundItem();
        }
        return power;
    }

    public final long getTopParent() {
        if (this.getParentId() == -10L) {
            return this.id;
        }
        try {
            if (this.getParentId() == this.id) {
                Item.logWarn(this.getName() + " has itself as parent!:" + this.id);
                return this.id;
            }
            Item parent = Items.getItem(this.getParentId());
            return parent.getTopParent();
        }
        catch (NoSuchItemException nsi) {
            Item.logWarn("Item " + this.id + "," + this.getName() + " has parentid " + this.getParentId() + " but that doesn't exist?", new Exception());
            return -10L;
        }
    }

    public final Item getTopParentOrNull() {
        long topId = this.getTopParent();
        if (topId == -10L) {
            return null;
        }
        try {
            return Items.getItem(topId);
        }
        catch (NoSuchItemException nsi) {
            String message = StringUtil.format("Unable to find top parent with ID: %d.", topId);
            Item.logWarn(message, nsi);
            return null;
        }
    }

    private boolean hasNoParent() {
        return this.parentId == -10L;
    }

    private boolean hasSameOwner(Item item) {
        return item.getOwnerId() == this.ownerId;
    }

    public final int getValue() {
        if (this.isCoin() || this.isFullprice()) {
            return this.template.getValue();
        }
        if (this.isChallengeNewbieItem()) {
            return 0;
        }
        int val = this.template.getValue();
        if (this.isCombine()) {
            float nums = (float)this.getWeightGrams() / (float)this.template.getWeightGrams();
            val = (int)(nums * ((float)this.template.getValue() * this.getQualityLevel() * this.getQualityLevel() / 10000.0f * ((100.0f - this.getDamage()) / 100.0f)));
        } else {
            val = (int)((float)this.template.getValue() * this.getQualityLevel() * this.getQualityLevel() / 10000.0f * ((100.0f - this.getDamage()) / 100.0f));
        }
        if (this.template.priceAffectedByMaterial) {
            val = (int)((float)val * this.getMaterialPriceModifier());
        }
        if (this.rarity > 0) {
            val *= this.rarity;
        }
        return val;
    }

    private float getMaterialPriceModifier() {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (this.getMaterial()) {
                case 56: {
                    return 8.0f;
                }
                case 30: {
                    return 6.0f;
                }
                case 31: {
                    return 5.0f;
                }
                case 10: {
                    return 5.0f;
                }
                case 57: {
                    return 10.0f;
                }
                case 7: {
                    return 10.0f;
                }
                case 12: {
                    return 0.75f;
                }
                case 67: {
                    return 12.0f;
                }
                case 8: {
                    return 8.0f;
                }
                case 9: {
                    return 2.5f;
                }
                case 13: {
                    return 0.9f;
                }
                case 96: {
                    return 9.0f;
                }
            }
        } else {
            if ((long)this.material == -10L) {
                return 1.0f;
            }
            if (this.material == 7) {
                return 10.0f;
            }
            if (this.material == 8) {
                return 8.0f;
            }
            if (this.material == 31 || this.material == 30) {
                return 6.0f;
            }
            if (this.material == 10 || this.material == 9) {
                return 5.0f;
            }
            return 1.0f;
        }
        return 1.0f;
    }

    public final ArmourTemplate.ArmourType getArmourType() {
        ArmourTemplate armourTemplate = ArmourTemplate.getArmourTemplate(this);
        if (armourTemplate != null) {
            return armourTemplate.getArmourType();
        }
        return null;
    }

    /*
     * Unable to fully structure code
     */
    public final boolean moveToItem(Creature mover, long targetId, boolean lastMove) throws NoSuchItemException, NoSuchPlayerException, NoSuchCreatureException {
        block286: {
            block287: {
                block285: {
                    block283: {
                        block284: {
                            target = Items.getItem(targetId);
                            if (!(!this.isNoTake() || this.getParent().isVehicle() && target.isVehicle())) {
                                return false;
                            }
                            if (this.isComponentItem()) {
                                return false;
                            }
                            if (this.isBodyPartAttached()) {
                                return false;
                            }
                            if (!(this.parentId != -10L && this.getParent().isInventory() && target.getTemplateId() == 1315 || this.parentId != -10L && this.getParent().getTemplateId() == 1315 && target.isInventory() || !this.getTemplate().isTransportable() || this.getTopParent() == this.getWurmId() || (this.getParent().isVehicle() || this.getParent().getTemplateId() == 1312 || this.getParent().getTemplateId() == 1309) && (target.isVehicle() || target.getTemplateId() == 1312 || target.getTemplateId() == 1309))) {
                                mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                                return false;
                            }
                            toReturn = false;
                            lOwnerId = this.getOwnerId();
                            itemOwner = null;
                            targetOwnerId = target.getOwnerId();
                            if (target.getTemplateId() == 1309) {
                                if (!target.isPlanted()) {
                                    mover.getCommunicator().sendNormalServerMessage("The wagoner container must be planted so it can accept crates.");
                                    return false;
                                }
                                if (target.getTopParent() != target.getWurmId()) {
                                    mover.getCommunicator().sendNormalServerMessage(StringUtil.format("You are not allowed to do that! The %s must be on the ground.", new Object[]{target.getName()}));
                                    return false;
                                }
                                if (!this.isCrate()) {
                                    mover.getCommunicator().sendNormalServerMessage("Only crates fit in the wagoner container.");
                                    return false;
                                }
                            }
                            if (target.isTent()) {
                                if (target.getTopParent() != target.getWurmId()) {
                                    mover.getCommunicator().sendNormalServerMessage(StringUtil.format("You are not allowed to do that! The %s must be on the ground.", new Object[]{target.getName()}));
                                    return false;
                                }
                                if (target.isTent() && target.isNewbieItem() && target.getLastOwnerId() != mover.getWurmId() && !Servers.localServer.PVPSERVER) {
                                    mover.getCommunicator().sendNormalServerMessage("You don't want to put things in other people's tents since you aren't allowed to remove them.");
                                    return false;
                                }
                            }
                            if (target.banked || target.mailed) {
                                return false;
                            }
                            targetTopParent = target.getTopParentOrNull();
                            if (targetTopParent != null && (targetTopParent.getTemplateId() == 853 || targetTopParent.getTemplateId() == 1410)) {
                                return false;
                            }
                            if (targetTopParent != null && targetTopParent.isHollow() && !targetTopParent.isInventory() && this.isTent() && this.isNewbieItem()) {
                                mover.getCommunicator().sendNormalServerMessage("You want to keep your tent easily retrievable.");
                                return false;
                            }
                            if (lOwnerId != -10L) {
                                itemOwner = Server.getInstance().getCreature(lOwnerId);
                                if (this.id == itemOwner.getBody().getBodyItem().getWurmId() || this.id == itemOwner.getPossessions().getInventory().getWurmId()) {
                                    return false;
                                }
                                if (!(targetOwnerId != -10L && targetOwnerId == lOwnerId || itemOwner.getPower() >= 3 || this.canBeDropped(true))) {
                                    if (this.isHollow()) {
                                        if (itemOwner.equals(mover)) {
                                            itemOwner.getCommunicator().sendSafeServerMessage("You are not allowed to drop that. It may contain a non-droppable item.");
                                        }
                                    } else if (itemOwner.equals(mover)) {
                                        itemOwner.getCommunicator().sendSafeServerMessage("You are not allowed to drop that.");
                                    }
                                    return false;
                                }
                            }
                            pickup = false;
                            targetOwner = null;
                            if (targetOwnerId != -10L && lOwnerId != targetOwnerId) {
                                lWeight = this.getFullWeight();
                                if (this.isLiquid() && target.isContainerLiquid()) {
                                    lWeight = Math.min(lWeight, target.getFreeVolume());
                                }
                                if (!(targetOwner = Server.getInstance().getCreature(targetOwnerId)).canCarry(lWeight) && lWeight != 0) {
                                    if (targetOwner.equals(mover)) {
                                        targetOwner.getCommunicator().sendSafeServerMessage("You cannot carry that much.");
                                    }
                                    return false;
                                }
                                if (lOwnerId == -10L) {
                                    pickup = true;
                                    try {
                                        v0 = ok = targetOwner.isKingdomGuard() != false && targetOwner.getKingdomId() == mover.getKingdomId();
                                        if (!Servers.isThisAPvpServer() && targetOwner.isBranded()) {
                                            ok = ok != false || targetOwner.mayAccessHold(mover) != false;
                                        } else {
                                            v1 = ok = ok != false || targetOwner.getDominator() == mover;
                                        }
                                        if (mover.getWurmId() != targetOwnerId && !ok) {
                                            mover.getCommunicator().sendNormalServerMessage("You can't give the " + this.getName() + " to " + targetOwner.getName() + " like that.");
                                            return false;
                                        }
                                        zone = Zones.getZone((int)this.getPosX() >> 2, (int)this.getPosY() >> 2, this.isOnSurface());
                                        tile = zone.getTileOrNull((int)this.getPosX() >> 2, (int)this.getPosY() >> 2);
                                        if (tile != null) {
                                            struct = tile.getStructure();
                                            tile2 = targetOwner.getCurrentTile();
                                            if (tile2 != null) {
                                                if (tile.getStructure() == struct) ** GOTO lbl101
                                                targetOwner.getCommunicator().sendNormalServerMessage("You can't reach the " + this.getName() + " through the wall.");
                                                return false;
                                            }
                                            if (struct == null) ** GOTO lbl101
                                            targetOwner.getCommunicator().sendNormalServerMessage("You can't reach the " + this.getName() + " through the wall.");
                                            return false;
                                        }
                                        Item.logWarn("No tile found in zone.");
                                        return false;
                                    }
                                    catch (NoSuchZoneException nsz) {
                                        if (itemOwner != null) {
                                            Item.logWarn(itemOwner.getName() + ":" + nsz.getMessage(), nsz);
                                            return false;
                                        }
                                        if (this.parentId != -10L) {
                                            Item.logInfo("Parent is not NOID Look at the following exception:");
                                            p = Items.getItem(this.parentId);
                                            Item.logWarn(nsz.getMessage() + " id=" + this.id + ' ' + this.getName() + ' ' + (WurmId.getType(this.parentId) == 6) + " parent=" + p.getName() + " ownerid=" + lOwnerId);
                                        }
                                        Item.logInfo(targetOwner.getName() + " trying to scam ZONEID=" + this.getZoneId() + ", Parent=NOID " + nsz.getMessage() + " id=" + this.id + ' ' + this.getName() + " ownerid=" + lOwnerId + ". Close these windows sometime.");
                                        return false;
                                    }
                                } else if (this.isCreatureWearableOnly() && mover.getVehicle() != -10L && itemOwner.getWurmId() == mover.getWurmId()) {
                                    mover.getCommunicator().sendNormalServerMessage("You need to be standing on the ground to do that.");
                                    return false;
                                }
                            }
lbl101:
                            // 7 sources

                            if ((pid = this.getParentId()) == target.getWurmId() || this.id == target.getParentId()) {
                                return false;
                            }
                            if (!target.isHollow() && pid == target.getParentId()) {
                                return false;
                            }
                            if (target.isBodyPart()) {
                                found = false;
                                for (x = 0; x < this.getBodySpaces().length; ++x) {
                                    if (this.getBodySpaces()[x] != target.getPlace()) continue;
                                    found = true;
                                }
                                if (!(found || target.getPlace() == 13 || target.getPlace() == 14 || target.getPlace() >= 35 && target.getPlace() < 48)) {
                                    if (itemOwner != null) {
                                        itemOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                                    } else if (targetOwner != null) {
                                        targetOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                                    } else if (mover != null) {
                                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                                    }
                                    return false;
                                }
                            }
                            targetParent = null;
                            parent = null;
                            if (this.parentId != -10L) {
                                parent = Items.getItem(this.parentId);
                            }
                            if (target.hasNoParent()) {
                                if (target.isNoPut() && !Item.insertOverrideNoPut(this, target)) {
                                    return false;
                                }
                            } else {
                                if (target.isNoPut() && !Item.insertOverrideNoPut(this, target)) {
                                    return false;
                                }
                                targetParent = Items.getItem(target.getParentId());
                                if (targetParent.isNoPut() && !Item.insertOverrideNoPut(this, targetParent)) {
                                    return false;
                                }
                                if (targetParent.isMailBox()) {
                                    mover.getCommunicator().sendNormalServerMessage("The spirits refuse to let you do that now.");
                                    return false;
                                }
                                topParent = null;
                                try {
                                    topParent = Items.getItem(target.getTopParent());
                                    if (topParent.isNoPut() && !Item.insertOverrideNoPut(this, (Item)topParent)) {
                                        return false;
                                    }
                                    if (topParent.isMailBox()) {
                                        mover.getCommunicator().sendNormalServerMessage("The spirits refuse to let you do that now.");
                                        return false;
                                    }
                                }
                                catch (NoSuchItemException nsi) {
                                    Item.logWarn(nsi.getMessage(), nsi);
                                    return false;
                                }
                            }
                            if (target.getTemplateId() == 1023) {
                                if (!this.isUnfired()) {
                                    mover.getCommunicator().sendNormalServerMessage("Only unfired clay items can be put into a kiln.");
                                    return false;
                                }
                                if (targetParent != null) {
                                    mover.getCommunicator().sendNormalServerMessage("You cannot reach that whilst it is in a kiln.");
                                    return false;
                                }
                            }
                            if (target.getTemplateId() == 1028 && !this.isOre()) {
                                mover.getCommunicator().sendNormalServerMessage("Only ore can be put into a smelter.");
                                return false;
                            }
                            if (target.isComponentItem() && targetParent == null) {
                                mover.getCommunicator().sendNormalServerMessage("You cannot put items in the " + target.getName() + " as it does not seem to be in anything.");
                                return false;
                            }
                            if (target.getTemplateId() == 1435) {
                                if (this.getTemplateId() != 128) {
                                    mover.getCommunicator().sendNormalServerMessage("You can only put water into the drinker.");
                                    return false;
                                }
                            } else if (target.getTemplateId() == 1434 && !this.isSeed()) {
                                mover.getCommunicator().sendNormalServerMessage("You can only put seeds into the feeder.");
                                return false;
                            }
                            if (target.getTemplateId() == 1432) {
                                mover.getCommunicator().sendNormalServerMessage("You can't put that there.");
                                return false;
                            }
                            if (target.isParentMustBeOnGround() && targetParent.getParentId() != -10L) {
                                mover.getCommunicator().sendNormalServerMessage("You cannot put items on the " + target.getName() + " whilst the " + targetParent.getName() + " is not on the ground.");
                                return false;
                            }
                            if (target.getTemplateId() == 1278 && this.getTemplateId() != 1276) {
                                mover.getCommunicator().sendNormalServerMessage("Only snowballs can be put into an icebox.");
                                return false;
                            }
                            if (target.getTemplateId() == 1108 && this.getTemplateId() != 768) {
                                mover.getCommunicator().sendNormalServerMessage("Only wine barrels can be put on that rack.");
                                return false;
                            }
                            if (target.getTemplateId() == 1109 && this.getTemplateId() != 189) {
                                mover.getCommunicator().sendNormalServerMessage("Only small barrels can be put into that rack.");
                                return false;
                            }
                            if (target.getTemplateId() == 1110 && this.getTemplateId() != 1161 && this.getTemplateId() != 1162) {
                                mover.getCommunicator().sendNormalServerMessage("Only herb and spice planters can be put into that rack.");
                                return false;
                            }
                            if (target.getTemplateId() == 1111 && this.getTemplateId() != 1022 && this.getTemplateId() != 1020) {
                                mover.getCommunicator().sendNormalServerMessage("Only amphora can be put into that rack.");
                                return false;
                            }
                            if (!(target.getTemplateId() != 1279 || this.canLarder() || this.usesFoodState() && this.getAuxData() == 0)) {
                                mover.getCommunicator().sendNormalServerMessage("Only processed food items can be put onto the shelf.");
                                return false;
                            }
                            if (target.getTemplateId() == 1120 && this.isBarrelRack()) {
                                mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit onto the shelf.");
                                return false;
                            }
                            if (target.isAlmanacContainer() && !this.isHarvestReport()) {
                                mover.getCommunicator().sendNormalServerMessage("Only harvest reports can be put in " + target.getTemplate().getNameWithGenus() + ".");
                                return false;
                            }
                            if (target.getTemplateId() == 1312 && !this.isCrate()) {
                                mover.getCommunicator().sendNormalServerMessage("Only crates can be put into that rack.");
                                return false;
                            }
                            if (!(target.getTemplateId() != 1315 || this.getTemplateId() == 662 && this.isEmpty(false))) {
                                mover.getCommunicator().sendNormalServerMessage("Only empty bsb can be put into that rack.");
                                return false;
                            }
                            if (target.getTemplateId() == 1341) {
                                topParent = target.getItemsAsArray();
                                nsi = ((Item[])topParent).length;
                                for (var21_30 = 0; var21_30 < nsi; ++var21_30) {
                                    compartment = topParent[var21_30];
                                    if (!compartment.doesContainerRestrictionsAllowItem(this)) continue;
                                    target = compartment;
                                    break;
                                }
                            }
                            if (!target.canHold(this)) {
                                mover.getCommunicator().sendNormalServerMessage("There isn't enough room to fit " + this.getNameWithGenus() + " in " + target.getNameWithGenus() + ".");
                                return false;
                            }
                            if (!(target.getTemplateId() != 1295 || this.canLarder() || this.usesFoodState() && this.getAuxData() == 0)) {
                                mover.getCommunicator().sendNormalServerMessage("Only processed food items can be put into the food tin.");
                                return false;
                            }
                            if (!this.isLiquid() && target.getTemplateId() == 1294) {
                                mover.getCommunicator().sendNormalServerMessage("Only liquids may be put in a thermos.");
                                return false;
                            }
                            if (!this.isLiquid() && target.getTemplateId() == 1118) {
                                mover.getCommunicator().sendNormalServerMessage("Only liquids may be put into an alchemy storage vial.");
                                return false;
                            }
                            if (target.containsIngredientsOnly() && !this.isFood() && !this.isLiquid() && !this.isRecipeItem()) {
                                mover.getCommunicator().sendNormalServerMessage("Only ingredients that are used to make food can be put onto " + target.getNameWithGenus() + ".");
                                return false;
                            }
                            if (target.getTemplateId() == 1284 && targetParent != null && targetParent.getTemplateId() == 1178 && targetParent.getParentId() != -10L) {
                                mover.getCommunicator().sendNormalServerMessage("You can only put liquids into the boiler when the still is not on the ground.");
                                return false;
                            }
                            if (!this.isLiquid()) break block283;
                            if (!target.isContainerLiquid() && target.getTemplateId() != 75) {
                                target = targetParent;
                            }
                            if (target == null || !target.isContainerLiquid() && target.getTemplateId() != 75) break block284;
                            if (target.getTemplateId() == 1284 && target.isEmpty(false) && (topParent = target.getTopParentOrNull()) != null && topParent.getTemplateId() == 1178) {
                                condenser = null;
                                for (Item contained : topParent.getItems()) {
                                    if (contained.getTemplateId() != 1285) continue;
                                    condenser = contained;
                                }
                                if (condenser != null && (contents = condenser.getItemsAsArray()).length != 0 && (contents[0].getTemplateId() != this.getTemplateId() || contents[0].getRealTemplateId() != this.getRealTemplateId() || contents[0].getRarity() != this.getRarity())) {
                                    mover.getCommunicator().sendNormalServerMessage("That would destroy the " + contents[0].getName() + ".");
                                    return false;
                                }
                            }
                            contained = null;
                            liquid = null;
                            volAvail = target.getContainerVolume();
                            for (Item contained : target.getItems()) {
                                if (MethodsItems.wouldDestroyLiquid(target, contained, this)) {
                                    mover.getCommunicator().sendNormalServerMessage("That would destroy the liquid.");
                                    return false;
                                }
                                if (contained.isLiquid()) {
                                    if (!target.isContainerLiquid() && target.getTemplateId() != 75 || contained.getTemplateId() == this.getTemplateId() && contained.getRealTemplateId() == this.getRealTemplateId() && contained.getLeftAuxData() == this.getLeftAuxData()) {
                                        liquid = contained;
                                    }
                                    if (contained.isSalted() != this.isSalted()) {
                                        liquid = contained;
                                    }
                                    volAvail -= contained.getWeightGrams();
                                    continue;
                                }
                                volAvail -= contained.getVolume();
                            }
                            contained = liquid;
                            if (contained != null) {
                                if (this.getRarity() != contained.getRarity()) {
                                    if (mover != null) {
                                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " or the " + contained.getName() + " would lose its rarity.");
                                    }
                                    return false;
                                }
                                if (this.isSalted() != contained.isSalted()) {
                                    if (mover != null) {
                                        mover.getCommunicator().sendNormalServerMessage("Cannot mix salty water with non-salty water.");
                                    }
                                    return false;
                                }
                            }
                            if (volAvail <= 0) ** GOTO lbl347
                            if (volAvail < this.getWeightGrams()) {
                                if (contained == null) {
                                    try {
                                        splitItem = MethodsItems.splitLiquid(this, volAvail, mover);
                                        target.insertItem(splitItem);
                                    }
                                    catch (FailedException fe) {
                                        Item.logWarn(fe.getMessage(), fe);
                                        return false;
                                    }
                                    catch (NoSuchTemplateException nst) {
                                        Item.logWarn(nst.getMessage(), nst);
                                        return false;
                                    }
                                } else {
                                    if ((contained.getTemplateId() == 417 || this.getTemplateId() == 417) && contained.getRealTemplateId() != this.getRealTemplateId()) {
                                        name1 = "fruit";
                                        name2 = "fruit";
                                        t = contained.getRealTemplate();
                                        if (t != null) {
                                            name1 = t.getName();
                                        }
                                        if ((t2 = this.getRealTemplate()) != null) {
                                            name2 = t2.getName();
                                        }
                                        if (!name1.equals(name2)) {
                                            contained.setName(name1 + " and " + name2 + " juice");
                                        }
                                        contained.setRealTemplate(-10);
                                    }
                                    this.setWeight(this.getWeightGrams() - volAvail, true, targetOwner != itemOwner);
                                    allWeight = contained.getWeightGrams() + volAvail;
                                    newQl = (this.getCurrentQualityLevel() * (float)volAvail + contained.getCurrentQualityLevel() * (float)contained.getWeightGrams()) / (float)allWeight;
                                    if (contained.isColor() && this.color != -1) {
                                        contained.setColor(WurmColor.mixColors(contained.color, contained.getWeightGrams(), this.color, volAvail, newQl));
                                    }
                                    contained.setQualityLevel(newQl);
                                    contained.setWeight(contained.getWeightGrams() + volAvail, true, targetOwner != itemOwner);
                                    contained.setDamage(0.0f);
                                }
                            } else {
                                if (contained != null) {
                                    if ((contained.getTemplateId() == 417 || this.getTemplateId() == 417) && contained.getRealTemplateId() != this.getRealTemplateId()) {
                                        name1 = "fruit";
                                        name2 = "fruit";
                                        t = contained.getRealTemplate();
                                        if (t != null) {
                                            name1 = t.getName();
                                        }
                                        if ((t2 = this.getRealTemplate()) != null) {
                                            name2 = t2.getName();
                                        }
                                        if (!name1.equals(name2)) {
                                            contained.setName(name1 + " and " + name2 + " juice");
                                        }
                                        contained.setRealTemplate(-10);
                                    }
                                    allWeight = this.getWeightGrams() + contained.getWeightGrams();
                                    newQl = (this.getCurrentQualityLevel() * (float)this.getWeightGrams() + contained.getCurrentQualityLevel() * (float)contained.getWeightGrams()) / (float)allWeight;
                                    if (contained.isColor() && this.color != -1) {
                                        contained.setColor(WurmColor.mixColors(contained.color, contained.getWeightGrams(), this.color, this.getWeightGrams(), newQl));
                                    }
                                    contained.setQualityLevel(newQl);
                                    contained.setDamage(0.0f);
                                    contained.setWeight(allWeight, true);
                                    Items.destroyItem(this.id);
                                    SoundPlayer.playSound("sound.liquid.fillcontainer", this, 0.1f);
                                    return false;
                                }
                                if (target.testInsertItem(this)) {
                                    if (parent != null) {
                                        if (!this.hasSameOwner(target)) {
                                            parent.dropItem(this.id, false);
                                        } else {
                                            parent.removeItem(this.id, false, false, false);
                                        }
                                    }
                                    this.setLastOwnerId(mover.getWurmId());
                                    target.insertItem(this);
                                } else {
                                    return false;
lbl347:
                                    // 1 sources

                                    return false;
                                }
                            }
                            SoundPlayer.playSound("sound.liquid.fillcontainer", this, 0.1f);
                            return true;
                        }
                        return false;
                    }
                    if (target.isContainerLiquid() || targetParent != null && targetParent.isContainerLiquid() && !target.isHollow()) {
                        if (!target.isContainerLiquid()) {
                            target = targetParent;
                        }
                        if (target.getSizeX() >= this.getSizeX() && target.getSizeY() >= this.getSizeY() && target.getSizeZ() > this.getSizeZ()) {
                            if (target.getItems().size() > 0) {
                                contained = null;
                                liquid = null;
                                for (Item contained : target.getItems()) {
                                    if (!contained.isLiquid()) continue;
                                    if (!this.isFood() && !this.isRecipeItem()) {
                                        mover.getCommunicator().sendNormalServerMessage("That would destroy the liquid.");
                                        return false;
                                    }
                                    liquid = contained;
                                }
                                if (liquid != null) {
                                    used = target.getUsedVolume();
                                    size = liquid.getWeightGrams();
                                    free = target.getVolume() - used;
                                    if (free < this.getVolume()) {
                                        if (free + size <= this.getVolume()) {
                                            if (itemOwner != null) {
                                                itemOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                                            } else if (targetOwner != null) {
                                                targetOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                                            } else if (mover != null) {
                                                mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                                            }
                                            return false;
                                        }
                                        leftNeeded = this.getVolume() - free;
                                        if (leftNeeded < size && leftNeeded > 0) {
                                            liquid.setWeight(size - leftNeeded, true);
                                            mover.getCommunicator().sendNormalServerMessage("You spill some " + liquid.getName() + ".");
                                        } else if (leftNeeded == size) {
                                            Items.destroyItem(liquid.getWurmId());
                                            mover.getCommunicator().sendNormalServerMessage("You spill the " + liquid.getName() + ".");
                                        }
                                    }
                                }
                            }
                        } else {
                            if (itemOwner != null) {
                                itemOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                            } else if (targetOwner != null) {
                                targetOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                            } else if (mover != null) {
                                mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                            }
                            return false;
                        }
                    }
                    if (target.isLockable() && target.getLockId() != -10L) {
                        try {
                            lock = Items.getItem(target.getLockId());
                            keyIds = lock.getKeyIds();
                            for (x = 0; x < keyIds.length; ++x) {
                                if (this.id == keyIds[x]) {
                                    return false;
                                }
                                if (this.items == null) continue;
                                for (Item itkey : this.items) {
                                    if (itkey.getWurmId() != keyIds[x]) continue;
                                    mover.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is locked with a key inside the " + this.getName() + ".");
                                    return false;
                                }
                            }
                        }
                        catch (NoSuchItemException nsi) {
                            Item.logWarn(target.getWurmId() + ": item has a set lock but the lock does not exist?:" + target.getLockId(), nsi);
                            return false;
                        }
                    }
                    if (targetParent != null && targetParent.isLockable() && targetParent.getLockId() != -10L) {
                        try {
                            lock = Items.getItem(targetParent.getLockId());
                            keyIds = lock.getKeyIds();
                            for (x = 0; x < keyIds.length; ++x) {
                                if (this.id == keyIds[x]) {
                                    return false;
                                }
                                if (this.items == null) continue;
                                for (Item itkey : this.items) {
                                    if (itkey.getWurmId() != keyIds[x]) continue;
                                    mover.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is locked with a key inside the " + this.getName() + ".");
                                    return false;
                                }
                            }
                        }
                        catch (NoSuchItemException nsi) {
                            Item.logWarn(targetParent.getWurmId() + ": item has a set lock but the lock does not exist?:" + targetParent.getLockId(), nsi);
                            return false;
                        }
                    }
                    if (targetParent != null && targetParent.isBulkContainer()) {
                        target = targetParent;
                    }
                    if (target.isBulkContainer()) {
                        if (this.isEnchanted() && this.getSpellFoodBonus() == 0.0f) {
                            if (mover != null) {
                                mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " would lose its enchants.");
                            }
                            return false;
                        }
                        if (this.getRarity() > 0) {
                            if (mover != null) {
                                mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " would lose its rarity.");
                            }
                            return false;
                        }
                        topParent = target.getTopParentOrNull();
                        if (target.getTopParent() != target.getWurmId() && !target.isCrate() && !targetParent.isVehicle() && targetParent.getTemplateId() != 1316 || topParent != null && (topParent.getTemplateId() == 853 || topParent.getTemplateId() == 1410)) {
                            if (mover != null) {
                                message = StringUtil.format("The %s needs to be on the ground.", new Object[]{target.getName()});
                                mover.getCommunicator().sendNormalServerMessage(message);
                            }
                            return false;
                        }
                        if (this.canHaveInscription()) {
                            if (this.inscription != null && this.inscription.hasBeenInscribed()) {
                                if (mover != null) {
                                    if (this.getAuxData() == 1 || this.getAuxData() > 8) {
                                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " would be destroyed.");
                                    } else {
                                        mover.getCommunicator().sendNormalServerMessage("The inscription on the " + this.getName() + " would be destroyed.");
                                    }
                                }
                                return false;
                            }
                            if (this.getAuxData() != 0) {
                                if (mover != null) {
                                    mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " would be destroyed.");
                                }
                                return false;
                            }
                        }
                        if (!this.isBulk()) {
                            if (mover != null) {
                                mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " would be destroyed.");
                            }
                            return false;
                        }
                        if (this.isFood()) {
                            if (target.getTemplateId() != 661 && !target.isCrate()) {
                                if (mover != null) {
                                    mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " would be destroyed.");
                                }
                                return false;
                            }
                            if (this.isDish() || this.usesFoodState() && this.isFreshBitSet() && this.isChoppedBitSet()) {
                                if (mover != null) {
                                    mover.getCommunicator().sendNormalServerMessage("Only unprocessed food items can be stored that way.");
                                }
                                return false;
                            }
                        } else if (target.getTemplateId() != 662 && target.getTemplateId() != 1317 && !target.isCrate()) {
                            if (mover != null) {
                                mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " would be destroyed.");
                            }
                            return false;
                        }
                        if ((mover == null || this.getTopParent() != mover.getInventory().getWurmId()) && MethodsItems.checkIfStealing(this, mover, null)) {
                            if (mover != null) {
                                mover.getCommunicator().sendNormalServerMessage("You're not allowed to put things into this " + target.getName() + ".");
                            }
                            return false;
                        }
                        if (target.isLocked()) {
                            if (mover != null && !target.mayAccessHold(mover)) {
                                mover.getCommunicator().sendNormalServerMessage("You're not allowed to put things into this " + target.getName() + ".");
                                return false;
                            }
                        } else if (mover != null && !Methods.isActionAllowed(mover, (short)7)) {
                            return false;
                        }
                        if (this.isFish() && this.isUnderWeight()) {
                            if (mover != null) {
                                mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " is not whole, and therefore is not allowed into the " + target.getName() + ".");
                            }
                            return false;
                        }
                        if (target.isCrate() && target.canAddToCrate(this)) {
                            return this.AddBulkItemToCrate(mover, target);
                        }
                        if (!target.isCrate() && target.hasSpaceFor(this.getVolume())) {
                            return this.AddBulkItem(mover, target);
                        }
                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                        return false;
                    }
                    if (target.getTemplateId() == 725 && !this.isWeaponPolearm()) {
                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                        return false;
                    }
                    if (target.getTemplateId() == 724 && (!this.isWeapon() || this.isWeaponPolearm())) {
                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                        return false;
                    }
                    if (target.getTemplateId() == 758 && !this.isWeaponBow() && !this.isBowUnstringed()) {
                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                        return false;
                    }
                    if (target.getTemplateId() == 759 && !this.isArmour()) {
                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                        return false;
                    }
                    if (target.getTemplateId() == 892 && !this.isArmour() && this.getTemplateId() != 831) {
                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                        return false;
                    }
                    if (target.getTemplateId() == 757 && this.getTemplateId() != 418) {
                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " would be destroyed.");
                        return false;
                    }
                    if (target.isSaddleBags()) {
                        if (this.isArtifact()) {
                            mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                            return false;
                        }
                        if (this.isHollow() && this.containsItem() && this.containsArtifact()) {
                            mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                            return false;
                        }
                    }
                    if (this.isArtifact() && target.isInside(new int[]{1333, 1334})) {
                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                        return false;
                    }
                    if (target.getTemplateId() != 177) break block285;
                    if (this.isDecoration() || !target.mayCreatureInsertItem()) {
                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                        return false;
                    }
                    if (parent != null) {
                        if (parent.getTemplateId() == 177) {
                            parent.dropItem(this.id, false);
                            parent.removeFromPile(this, lastMove == false);
                            if (parent.getItemCount() < 3 && lastMove) {
                                for (Creature iwatcher : parent.getWatchers()) {
                                    iwatcher.getCommunicator().sendCloseInventoryWindow(parent.getWurmId());
                                }
                            } else if (lastMove) {
                                parent.updatePile();
                            }
                        } else {
                            parent.dropItem(this.id, false);
                        }
                    }
                    this.setLastOwnerId(mover.getWurmId());
                    target.insertIntoPile(this);
                    toReturn = false;
                    if (itemOwner != null) {
                        itemOwner.addItemDropped(this);
                    }
                    break block286;
                }
                if (target.isHollow() || targetParent == null || targetParent.getTemplateId() != 177) break block287;
                if (this.isDecoration() || !targetParent.mayCreatureInsertItem() && mover.getPower() == 0) {
                    mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " doesn't fit.");
                    return false;
                }
                if (parent != null) {
                    parent.dropItem(this.id, false);
                }
                this.setLastOwnerId(mover.getWurmId());
                targetParent.insertIntoPile(this);
                toReturn = false;
                if (itemOwner != null) {
                    itemOwner.addItemDropped(this);
                }
                break block286;
            }
            if (target.testInsertItem(this)) {
                insertTarget = target.getInsertItem();
                v2 = mayInsert = insertTarget == null ? false : insertTarget.mayCreatureInsertItem();
                if ((insertTarget.isInventory() || insertTarget.isInventoryGroup()) && (p = this.getParentOrNull()) != null && (p.isInventory() || p.isInventoryGroup())) {
                    mayInsert = true;
                }
                if (target.getTemplateId() == 1404 && (this.getTemplateId() == 1272 || this.getTemplateId() == 748) && this.getCurrentQualityLevel() < 30.0f) {
                    if (itemOwner != null) {
                        itemOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " is too low quality to be used as a report.");
                    } else if (targetOwner != null) {
                        targetOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " is too low quality to be used as a report.");
                    } else if (mover != null) {
                        mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " is too low quality to be used as a report.");
                    }
                    return false;
                }
                if (mayInsert || itemOwner != null && itemOwner.getPower() > 0 || targetOwner != null && targetOwner.getPower() > 0) {
                    if (parent != null) {
                        if (!this.hasSameOwner(target)) {
                            parent.dropItem(this.id, false);
                            if (parent.getTemplateId() == 177) {
                                parent.removeFromPile(this, lastMove == false);
                                if (parent.getItemCount() < 3 && lastMove) {
                                    for (Creature iwatcher : parent.getWatchers()) {
                                        iwatcher.getCommunicator().sendCloseInventoryWindow(parent.getWurmId());
                                    }
                                } else if (lastMove) {
                                    parent.updatePile();
                                }
                            }
                            if (targetOwner != null) {
                                targetOwner.addItemTaken(this);
                                if (parent.isItemSpawn()) {
                                    targetOwner.addChallengeScore(ChallengePointEnum.ChallengePoint.ITEMSLOOTED.getEnumtype(), 0.1f);
                                }
                                if (pickup) {
                                    MissionTriggers.activateTriggers(mover, this, 6, -10L, 1);
                                }
                            } else if (itemOwner != null) {
                                itemOwner.addItemDropped(this);
                            }
                        } else {
                            if (parent.getTemplateId() == 177) {
                                parent.dropItem(this.id, false);
                                parent.removeFromPile(this, lastMove == false);
                                if (parent.getItemCount() < 3 && lastMove) {
                                    for (Creature iwatcher : parent.getWatchers()) {
                                        iwatcher.getCommunicator().sendCloseInventoryWindow(parent.getWurmId());
                                    }
                                } else if (lastMove) {
                                    parent.updatePile();
                                }
                            } else if (this.getTopParentOrNull() != null && this.getTopParentOrNull().getTemplateId() == 177) {
                                this.getTopParentOrNull().dropItem(this.id, false);
                            }
                            parent.removeItem(this.id, false, false, false);
                        }
                    } else {
                        if (targetOwner != null) {
                            targetOwner.addItemTaken(this);
                            if (pickup) {
                                MissionTriggers.activateTriggers(mover, this, 6, -10L, 1);
                            }
                        }
                        try {
                            z = Zones.getZone(this.getTilePos(), this.isOnSurface());
                            z.removeItem(this);
                        }
                        catch (NoSuchZoneException var21_38) {
                            // empty catch block
                        }
                    }
                    if (!this.isLocked()) {
                        this.setLastOwnerId(mover.getWurmId());
                    }
                    target.insertItem(this);
                    return true;
                }
                if (insertTarget != null) {
                    if (itemOwner != null) {
                        itemOwner.getCommunicator().sendNormalServerMessage("The " + insertTarget.getName() + " contains too many items already.");
                    } else if (targetOwner != null) {
                        targetOwner.getCommunicator().sendNormalServerMessage("The " + insertTarget.getName() + " contains too many items already.");
                    }
                }
                return false;
            }
            if (target.isHollow()) {
                if (itemOwner != null) {
                    itemOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                } else if (targetOwner != null) {
                    targetOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                } else if (mover != null) {
                    mover.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + target.getName() + ".");
                }
                return false;
            }
            cont = Items.getItem(target.getParentId());
            if (cont.isBodyPart()) break block286;
            if (!cont.mayCreatureInsertItem() && (itemOwner == null || itemOwner.getPower() <= 0) && (targetOwner == null || targetOwner.getPower() <= 0)) ** GOTO lbl712
            if (cont.testInsertItem(this)) {
                if (parent != null) {
                    if (!this.hasSameOwner(target)) {
                        parent.dropItem(this.id, false);
                        if (parent.getTemplateId() == 177) {
                            parent.removeFromPile(this);
                        }
                        if (targetOwner != null) {
                            targetOwner.addItemTaken(this);
                            if (pickup) {
                                MissionTriggers.activateTriggers(mover, this, 6, -10L, 1);
                            }
                        } else if (itemOwner != null) {
                            itemOwner.addItemDropped(this);
                        }
                    } else {
                        if (parent.getTemplateId() == 177) {
                            parent.removeFromPile(this);
                        }
                        parent.removeItem(this.id, false, false, false);
                        toReturn = true;
                    }
                } else {
                    if (targetOwner != null) {
                        targetOwner.addItemTaken(this);
                        if (pickup) {
                            MissionTriggers.activateTriggers(mover, this, 6, -10L, 1);
                        }
                    }
                    try {
                        z = Zones.getZone(this.getTilePos(), this.isOnSurface());
                        z.removeItem(this);
                    }
                    catch (NoSuchZoneException var20_29) {
                        // empty catch block
                    }
                }
                this.setLastOwnerId(mover.getWurmId());
                cont.insertItem(this);
            } else {
                if (itemOwner != null) {
                    itemOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + cont.getName() + ".");
                } else if (targetOwner != null) {
                    targetOwner.getCommunicator().sendNormalServerMessage("The " + this.getName() + " will not fit in the " + cont.getName() + ".");
                }
                return false;
lbl712:
                // 1 sources

                if (itemOwner != null) {
                    itemOwner.getCommunicator().sendNormalServerMessage("The " + cont.getName() + " contains too many items already.");
                } else if (targetOwner != null) {
                    targetOwner.getCommunicator().sendNormalServerMessage("The " + cont.getName() + " contains too many items already.");
                }
                return false;
            }
        }
        return toReturn;
    }

    public boolean AddBulkItemToCrate(Creature mover, Item target) {
        Item toaddTo;
        int remainingSpaces = target.getRemainingCrateSpace();
        if (remainingSpaces <= 0) {
            return false;
        }
        byte auxToCheck = 0;
        if (this.usesFoodState()) {
            auxToCheck = this.isFresh() || this.isLive() ? (byte)(this.getAuxData() & 0x7F) : this.getAuxData();
        }
        if ((toaddTo = target.getItemWithTemplateAndMaterial(this.getTemplateId(), this.getMaterial(), auxToCheck, this.getRealTemplateId())) != null) {
            if (MethodsItems.checkIfStealing(toaddTo, mover, null)) {
                int tilex = (int)toaddTo.getPosX() >> 2;
                int tiley = (int)toaddTo.getPosY() >> 2;
                Village vil = Zones.getVillage(tilex, tiley, mover.isOnSurface());
                if (mover.isLegal() && vil != null) {
                    mover.getCommunicator().sendNormalServerMessage("That would be illegal here. You can check the settlement token for the local laws.");
                    return false;
                }
                if (mover.getDeity() != null && !mover.getDeity().isLibila() && mover.faithful) {
                    mover.getCommunicator().sendNormalServerMessage("Your deity would never allow stealing.");
                    return false;
                }
            }
            float percent = 1.0f;
            if (!this.isFish() || this.getTemplateId() == 369) {
                percent = (float)this.getWeightGrams() / (float)this.template.getWeightGrams();
            }
            boolean destroyOriginal = true;
            if (percent > (float)remainingSpaces) {
                percent = Math.min((float)remainingSpaces, percent);
                destroyOriginal = false;
            }
            int templWeight = this.template.getWeightGrams();
            Item tempItem = null;
            if (!destroyOriginal) {
                try {
                    int newWeight = (int)((float)templWeight * percent);
                    tempItem = ItemFactory.createItem(this.template.templateId, this.getCurrentQualityLevel(), this.getMaterial(), (byte)0, null);
                    tempItem.setWeight(newWeight, true);
                    if (this.usesFoodState()) {
                        tempItem.setAuxData(auxToCheck);
                    }
                    this.setWeight(this.getWeightGrams() - newWeight, true);
                }
                catch (NoSuchTemplateException nst) {
                    Item.logWarn("Adding to crate failed (missing template?).");
                    Item.logWarn(nst.getMessage(), nst);
                    return false;
                }
                catch (FailedException fe) {
                    Item.logWarn("Adding to crate failed to create temp item.");
                    Item.logWarn(fe.getMessage(), fe);
                    return false;
                }
            }
            if (tempItem == null) {
                tempItem = this;
            }
            float existingNumsBulk = toaddTo.getBulkNumsFloat(false);
            float percentAdded = percent / (existingNumsBulk + percent);
            float qlDiff = toaddTo.getQualityLevel() - this.getCurrentQualityLevel();
            float qlChange = percentAdded * qlDiff;
            if (qlDiff > 0.0f) {
                float newQl = toaddTo.getQualityLevel() - qlChange * 1.1f;
                toaddTo.setQualityLevel(Math.max(1.0f, newQl));
            } else if (qlDiff < 0.0f) {
                float newQl = toaddTo.getQualityLevel() - qlChange * 0.9f;
                toaddTo.setQualityLevel(Math.max(1.0f, newQl));
            }
            toaddTo.setWeight(toaddTo.getWeightGrams() + (int)(percent * (float)this.template.getVolume()), true);
            if (destroyOriginal) {
                Items.destroyItem(this.getWurmId());
            } else {
                Items.destroyItem(tempItem.getWurmId());
            }
            mover.achievement(167, 1);
            target.updateModelNameOnGroundItem();
            return true;
        }
        try {
            toaddTo = ItemFactory.createItem(669, this.getCurrentQualityLevel(), this.getMaterial(), (byte)0, null);
            toaddTo.setRealTemplate(this.getTemplateId());
            if (this.usesFoodState()) {
                toaddTo.setAuxData(auxToCheck);
                if (this.getRealTemplateId() != -10) {
                    toaddTo.setData1(this.getRealTemplateId());
                }
                toaddTo.setName(this.getActualName());
                ItemMealData imd = ItemMealData.getItemMealData(this.getWurmId());
                if (imd != null) {
                    ItemMealData.save(toaddTo.getWurmId(), imd.getRecipeId(), imd.getCalories(), imd.getCarbs(), imd.getFats(), imd.getProteins(), imd.getBonus(), imd.getStages(), imd.getIngredients());
                }
            }
            float percent = 1.0f;
            if (!this.isFish() || this.getTemplateId() == 369) {
                percent = (float)this.getWeightGrams() / (float)this.template.getWeightGrams();
            }
            boolean destroy = true;
            if (percent > (float)remainingSpaces) {
                percent = Math.min((float)remainingSpaces, percent);
                destroy = false;
            }
            if (!toaddTo.setWeight((int)(percent * (float)this.template.getVolume()), true)) {
                target.insertItem(toaddTo, true);
            }
            if (destroy) {
                Items.destroyItem(this.getWurmId());
            } else {
                int remove = (int)((float)this.template.getWeightGrams() * percent);
                this.setWeight(this.getWeightGrams() - remove, true);
            }
            mover.achievement(167, 1);
            target.updateModelNameOnGroundItem();
            toaddTo.setLastOwnerId(mover.getWurmId());
            return true;
        }
        catch (FailedException | NoSuchTemplateException e) {
            Item.logWarn(e.getMessage(), e);
            return false;
        }
    }

    private static boolean insertOverrideNoPut(Item item, Item target) {
        if ((item.isShard() || item.isOre()) && target.isWarmachine()) {
            return true;
        }
        return item.getTemplateId() == 1139 && target.getTemplateId() == 1175;
    }

    public boolean AddBulkItem(Creature mover, Item target) {
        Item toaddTo;
        boolean full = target.isFull();
        byte auxToCheck = 0;
        if (this.usesFoodState()) {
            auxToCheck = this.isFresh() || this.isLive() ? (byte)(this.getAuxData() & 0x7F) : this.getAuxData();
        }
        if ((toaddTo = target.getItemWithTemplateAndMaterial(this.getTemplateId(), this.getMaterial(), auxToCheck, this.getRealTemplateId())) != null) {
            if (MethodsItems.checkIfStealing(toaddTo, mover, null)) {
                int tilex = (int)toaddTo.getPosX() >> 2;
                int tiley = (int)toaddTo.getPosY() >> 2;
                Village vil = Zones.getVillage(tilex, tiley, mover.isOnSurface());
                if (mover.isLegal() && vil != null) {
                    mover.getCommunicator().sendNormalServerMessage("That would be illegal here. You can check the settlement token for the local laws.");
                    return false;
                }
                if (mover.getDeity() != null && !mover.getDeity().isLibila() && mover.faithful) {
                    mover.getCommunicator().sendNormalServerMessage("Your deity would never allow stealing.");
                    return false;
                }
            }
            float existingNumsBulk = toaddTo.getBulkNumsFloat(false);
            float percent = 1.0f;
            if (!this.isFish() || this.getTemplateId() == 369) {
                percent = (float)this.getWeightGrams() / (float)this.template.getWeightGrams();
            }
            float percentAdded = percent / (existingNumsBulk + percent);
            float qlDiff = toaddTo.getQualityLevel() - this.getCurrentQualityLevel();
            float qlChange = percentAdded * qlDiff;
            if (qlDiff > 0.0f) {
                float newQl = toaddTo.getQualityLevel() - qlChange * 1.1f;
                toaddTo.setQualityLevel(Math.max(1.0f, newQl));
            } else if (qlDiff < 0.0f) {
                float newQl = toaddTo.getQualityLevel() - qlChange * 0.9f;
                toaddTo.setQualityLevel(Math.max(1.0f, newQl));
            }
            toaddTo.setWeight(toaddTo.getWeightGrams() + (int)(percent * (float)this.template.getVolume()), true);
            Items.destroyItem(this.getWurmId());
            mover.achievement(167, 1);
            if (full != target.isFull()) {
                target.updateModelNameOnGroundItem();
            }
            return true;
        }
        try {
            toaddTo = ItemFactory.createItem(669, this.getCurrentQualityLevel(), this.getMaterial(), (byte)0, null);
            toaddTo.setRealTemplate(this.getTemplateId());
            if (this.usesFoodState()) {
                toaddTo.setAuxData(auxToCheck);
                if (this.getRealTemplateId() != -10) {
                    toaddTo.setData1(this.getRealTemplateId());
                }
                toaddTo.setName(this.getActualName());
                ItemMealData imd = ItemMealData.getItemMealData(this.getWurmId());
                if (imd != null) {
                    ItemMealData.save(toaddTo.getWurmId(), imd.getRecipeId(), imd.getCalories(), imd.getCarbs(), imd.getFats(), imd.getProteins(), imd.getBonus(), imd.getStages(), imd.getIngredients());
                }
            }
            float percent = 1.0f;
            if (!this.isFish() || this.getTemplateId() == 369) {
                percent = (float)this.getWeightGrams() / (float)this.template.getWeightGrams();
            }
            if (!toaddTo.setWeight((int)(percent * (float)this.template.getVolume()), true)) {
                target.insertItem(toaddTo, true);
            }
            Items.destroyItem(this.getWurmId());
            mover.achievement(167, 1);
            if (full != target.isFull()) {
                target.updateModelNameOnGroundItem();
            }
            toaddTo.setLastOwnerId(mover.getWurmId());
            return true;
        }
        catch (NoSuchTemplateException nst) {
            Item.logWarn(nst.getMessage(), nst);
        }
        catch (FailedException fe) {
            Item.logWarn(fe.getMessage(), fe);
        }
        return false;
    }

    public void removeFromPile(Item item) {
        try {
            Zone zone = Zones.getZone((int)this.getPosX() >> 2, (int)this.getPosY() >> 2, this.isOnSurface());
            zone.removeItem(item);
        }
        catch (NoSuchZoneException nsz) {
            Item.logWarn("Removed from nonexistant zone " + this.id);
        }
    }

    public void removeFromPile(Item item, boolean moving) {
        try {
            Zone zone = Zones.getZone((int)this.getPosX() >> 2, (int)this.getPosY() >> 2, this.isOnSurface());
            zone.removeItem(item, moving, false);
        }
        catch (NoSuchZoneException nsz) {
            Item.logWarn("Removed from nonexistant zone " + this.id);
        }
    }

    public void updatePile() {
        try {
            Zone zone = Zones.getZone((int)this.getPosX() >> 2, (int)this.getPosY() >> 2, this.isOnSurface());
            zone.updatePile(this);
        }
        catch (NoSuchZoneException nsz) {
            Item.logWarn("Removed from nonexistant zone " + this.id);
        }
    }

    public final void putInVoid() {
        if (this.parentId != -10L) {
            try {
                Item parent = Items.getItem(this.parentId);
                parent.dropItem(this.id, false);
            }
            catch (NoSuchItemException nsi) {
                Item.logWarn(this.id + " had a parent that could not be found.", nsi);
            }
        }
        if (this.zoneId != -10) {
            try {
                Zone zone = Zones.getZone((int)this.getPosX() >> 2, (int)this.getPosY() >> 2, this.isOnSurface());
                zone.removeItem(this);
            }
            catch (NoSuchZoneException nsz) {
                Item.logWarn("No such zone: " + ((int)this.getPosX() >> 2) + "," + ((int)this.getPosY() >> 2) + "," + this.isOnSurface() + "?", nsz);
            }
        }
    }

    private void insertIntoPile(Item item) {
        try {
            item.setPosXYZ(this.getPosX(), this.getPosY(), this.getPosZ());
            Zone zone = Zones.getZone((int)this.getPosX() >> 2, (int)this.getPosY() >> 2, this.isOnSurface());
            zone.addItem(item);
        }
        catch (NoSuchZoneException nsz) {
            Item.logWarn("added to nonexistant zone " + this.id);
        }
    }

    public final long getOwner() throws NotOwnedException {
        long lOwnerId = this.getOwnerId();
        if (lOwnerId == -10L) {
            throw new NotOwnedException("Not owned item");
        }
        return lOwnerId;
    }

    private Creature getOwnerOrNull() {
        block3: {
            try {
                return Server.getInstance().getCreature(this.getOwnerId());
            }
            catch (NoSuchCreatureException nsc) {
                Item.logWarn(nsc.getMessage(), nsc);
            }
            catch (NoSuchPlayerException nsp) {
                PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(this.getOwnerId());
                if (info != null) break block3;
                Item.logWarn(nsp.getMessage(), nsp);
            }
        }
        return null;
    }

    public int getSurfaceArea() {
        float modifier = 1.0f;
        if (this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
        }
        if (this.isLiquid()) {
            return (int)((float)this.getWeightGrams() * modifier);
        }
        if (!this.template.usesSpecifiedContainerSizes()) {
            return (int)((float)(this.getSizeX() * this.getSizeY() * 2 + this.getSizeY() * this.getSizeZ() * 2) + (float)(this.getSizeX() * this.getSizeZ() * 2) * modifier);
        }
        return (int)((float)(this.template.getSizeX() * this.template.getSizeY() * 2 + this.template.getSizeY() * this.template.getSizeZ() * 2) + (float)(this.template.getSizeX() * this.template.getSizeZ() * 2) * modifier);
    }

    public int getVolume() {
        float modifier = 1.0f;
        if (this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_VOLUME);
        }
        if (this.isLiquid()) {
            return (int)((float)this.getWeightGrams() * modifier);
        }
        if (this.internalVolume != 0) {
            return (int)((float)this.internalVolume * modifier);
        }
        if (!this.template.usesSpecifiedContainerSizes()) {
            return (int)((float)(this.getSizeX() * this.getSizeY() * this.getSizeZ()) * modifier);
        }
        return (int)((float)(this.template.getSizeX() * this.template.getSizeY() * this.template.getSizeZ()) * modifier);
    }

    public int setInternalVolumeFromAuxByte() {
        int newVolume = 10;
        switch (this.getAuxData()) {
            case 12: {
                newVolume = 1;
                break;
            }
            case 11: {
                newVolume = 2;
                break;
            }
            case 10: {
                newVolume = 5;
                break;
            }
            case 9: {
                newVolume = 10;
                break;
            }
            case 8: {
                newVolume = 20;
                break;
            }
            case 7: {
                newVolume = 50;
                break;
            }
            case 6: {
                newVolume = 100;
                break;
            }
            case 5: {
                newVolume = 200;
                break;
            }
            case 4: {
                newVolume = 500;
                break;
            }
            case 3: {
                newVolume = 1000;
                break;
            }
            case 2: {
                newVolume = 2000;
                break;
            }
            case 1: {
                newVolume = 5000;
                break;
            }
            default: {
                newVolume = 10000;
            }
        }
        this.internalVolume = newVolume;
        return this.internalVolume;
    }

    public int getContainerVolume() {
        float modifier = 1.0f;
        if (this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_VOLUME);
        }
        if (this.internalVolume != 0) {
            return (int)((float)this.internalVolume * modifier);
        }
        if (this.template.usesSpecifiedContainerSizes()) {
            return (int)((float)this.template.getContainerVolume() * modifier);
        }
        return this.getVolume();
    }

    public final float getSizeMod() {
        float minMod;
        float f = minMod = this.getTemplateId() == 344 ? 0.3f : 0.8f;
        if (this.getTemplateId() == 272 || this.isFish() || this.getTemplateId() == 344 || this.isCombine() && this.getWeightGrams() > 5000) {
            return Math.max(minMod, Math.min(5.0f, (float)Item.cubeRoot((float)this.getVolume() / (float)this.template.getVolume())));
        }
        TreeData.TreeType ttype = Materials.getTreeTypeForWood(this.getMaterial());
        if (!(ttype == null || ttype.isFruitTree() || this.getTemplateId() != 731 && this.getTemplateId() != 385)) {
            float ageScale = (float)((this.getAuxData() >= 100 ? this.getAuxData() - 100 : this.getAuxData()) + 1) / 16.0f;
            return ageScale * 16.0f;
        }
        float modifier = 1.0f;
        if (this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
        }
        return 1.0f * modifier;
    }

    public static double cubeRoot(double x) {
        return Math.pow(x, 0.3333333333333333);
    }

    private int getUsedVolume() {
        int used = 0;
        for (Item i : this.getItems()) {
            if (!i.isInventoryGroup()) {
                if (i.isLiquid() || i.isBulkItem()) {
                    used += i.getWeightGrams();
                    continue;
                }
                used += i.getVolume();
                continue;
            }
            used += i.getUsedVolume();
        }
        return used;
    }

    public boolean hasSpaceFor(int volume) {
        if (this.isInventory()) {
            return true;
        }
        return this.getContainerVolume() - this.getUsedVolume() > volume;
    }

    public boolean canAddToCrate(Item toAdd) {
        if (!this.isCrate()) {
            return false;
        }
        if (!toAdd.isBulk()) {
            return false;
        }
        return this.getRemainingCrateSpace() > 0;
    }

    public final int getRemainingCrateSpace() {
        Item[] cargo;
        int count = 0;
        for (Item cargoItem : cargo = this.getItemsAsArray()) {
            count += cargoItem.getBulkNums();
        }
        if (this.template.templateId == 852) {
            return 300 - count;
        }
        if (this.template.templateId == 851) {
            return 150 - count;
        }
        return 0;
    }

    public final ItemTemplate getTemplate() {
        return this.template;
    }

    private boolean containsItem() {
        for (Item next : this.getItems()) {
            if (next.isBodyPart()) continue;
            return true;
        }
        return false;
    }

    private boolean containsItemAndIsSameTypeOfItem(Item item) {
        for (Item next : this.getItems()) {
            if (next.isBodyPart() || item.getTemplateId() != next.getTemplateId() && (!item.isBarding() || !next.isBarding())) continue;
            return true;
        }
        return false;
    }

    private boolean containsArtifact() {
        for (Item item : this.getAllItems(true, true)) {
            if (!item.isArtifact()) continue;
            return true;
        }
        return false;
    }

    private boolean containsArmor() {
        for (Item next : this.getItems()) {
            if (next.isBodyPart() || !next.isArmour()) continue;
            return true;
        }
        return false;
    }

    private boolean canHoldItem(Item item) {
        try {
            short lHeld;
            short rHeld;
            Creature owner = Server.getInstance().getCreature(this.ownerId);
            if (this.auxbyte != 1 && this.auxbyte != 0 && owner.isPlayer()) {
                return false;
            }
            Item rightWeapon = owner.getRighthandWeapon();
            Item leftWeapon = owner.getLefthandWeapon();
            Item shield = owner.getShield();
            if (rightWeapon == null && leftWeapon == null && shield == null) {
                return true;
            }
            if (rightWeapon != null && item.isTwoHanded()) {
                return false;
            }
            short s = rHeld = owner.isPlayer() ? (short)38 : 14;
            if (rightWeapon != null && this.place == rHeld) {
                return false;
            }
            if (rightWeapon != null && rightWeapon.isTwoHanded()) {
                return false;
            }
            short s2 = lHeld = owner.isPlayer() ? (short)37 : 13;
            if (leftWeapon != null && this.place == lHeld) {
                return false;
            }
            if (leftWeapon != null && item.isTwoHanded()) {
                return false;
            }
            if (leftWeapon != null && leftWeapon.isTwoHanded()) {
                return false;
            }
            if (shield != null && item.isTwoHanded()) {
                return false;
            }
            return shield == null || this.place != lHeld;
        }
        catch (Exception ex) {
            return false;
        }
    }

    private boolean testInsertShield(Item item, Creature owner) {
        Item leftWeapon;
        if (owner.isPlayer()) {
            if (this.getPlace() != 44) {
                return false;
            }
            if (this.containsItem()) {
                return false;
            }
        } else if (this.containsItemAndIsSameTypeOfItem(item)) {
            return false;
        }
        if ((leftWeapon = owner.getLefthandWeapon()) != null) {
            return false;
        }
        Item rightWeapon = owner.getRighthandWeapon();
        return rightWeapon == null || !rightWeapon.isWeaponBow() && !rightWeapon.isTwoHanded();
    }

    private boolean testInsertItemIntoSlot(Item item, Creature owner) {
        byte[] slots = item.getBodySpaces();
        for (int i = 0; i < slots.length; ++i) {
            if (this.place != slots[i]) continue;
            return !this.containsItem();
        }
        return false;
    }

    private boolean testInsertItemIntoAnimalSlot(Item item, Creature owner) {
        byte[] slots = item.getBodySpaces();
        for (int i = 0; i < slots.length; ++i) {
            if (this.place != slots[i]) continue;
            return !this.containsItemAndIsSameTypeOfItem(item);
        }
        return false;
    }

    private boolean testInsertWeapon(Item item, Creature owner) {
        if (!this.canHoldItem(item) || this.containsItem() && !this.containsArmor()) {
            return false;
        }
        if (!owner.hasHands()) {
            boolean found = false;
            byte[] armourplaces = item.getBodySpaces();
            for (int x = 0; x < armourplaces.length; ++x) {
                if (armourplaces[x] != this.place) continue;
                found = true;
                break;
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private boolean testInsertItemPlayer(Item item, Creature owner) {
        if (this.isBodyPart() && this.isEquipmentSlot()) {
            if (item.isArmour() && !item.isShield()) {
                return false;
            }
            if (item.isShield()) {
                return this.testInsertShield(item, owner);
            }
            if (item.getDamagePercent() > 0) {
                return this.testInsertWeapon(item, owner);
            }
            if (this.place == 37 || this.place == 38) {
                return !this.containsItem() && this.canHoldItem(item) && !item.isInventoryGroup();
            }
            return this.testInsertItemIntoSlot(item, owner);
        }
        if (this.isBodyPart() && !this.isEquipmentSlot()) {
            if (item.isBarding()) {
                return false;
            }
            if (!item.isArmour() && !item.isBracelet() || item.isShield()) {
                return false;
            }
            return this.testInsertItemIntoSlot(item, owner);
        }
        if (this.isHollow()) {
            return this.testInsertHollowItem(item, false);
        }
        Item insertTarget = this.getInsertItem();
        if (insertTarget == null) {
            return false;
        }
        return insertTarget.testInsertItem(item);
    }

    private boolean bodyPartIsWeaponSlotNPC() {
        return this.place == 13 || this.place == 14;
    }

    private boolean testInsertHollowItem(Item item, boolean testItemCount) {
        if (this.isNoDrop() && item.isArtifact()) {
            return false;
        }
        if (Features.Feature.FREE_ITEMS.isEnabled() && item.isChallengeNewbieItem() && this.ownerId == -10L && (item.isArmour() || item.isWeapon() || item.isShield())) {
            return false;
        }
        int freevol = this.getFreeVolume();
        if (this.itemCanBeInserted(item) && (this.getTemplateId() == 177 || this.getTemplateId() == 0 || freevol >= item.getVolume() || this.doesContainerRestrictionsAllowItem(item))) {
            Item parent;
            if (this.getTemplateId() == 621 && item.isSaddleBags() && (parent = this.getParentOrNull()) != null && parent.isSaddleBags()) {
                return false;
            }
            if (testItemCount) {
                return this.mayCreatureInsertItem();
            }
            return true;
        }
        return false;
    }

    private boolean testInsertHumanoidNPC(Item item, Creature owner) {
        if (this.isBodyPart()) {
            if (item.isShield() && this.place == 3) {
                return this.testInsertShield(item, owner);
            }
            if (item.getDamagePercent() > 0 && this.bodyPartIsWeaponSlotNPC()) {
                return this.testInsertWeapon(item, owner);
            }
            return this.testInsertItemIntoAnimalSlot(item, owner);
        }
        if (this.isHollow()) {
            return this.testInsertHollowItem(item, false);
        }
        if (!this.isHollow()) {
            Item insertTarget = this.getInsertItem();
            if (insertTarget == null) {
                return false;
            }
            return insertTarget.testInsertItem(item);
        }
        return false;
    }

    private boolean testInsertAnimal(Item item, Creature owner) {
        if (this.isBodyPart()) {
            return this.testInsertItemIntoAnimalSlot(item, owner);
        }
        if (this.isHollow()) {
            return this.testInsertHollowItem(item, false);
        }
        if (!this.isHollow()) {
            Item insertTarget = this.getInsertItem();
            if (insertTarget == null) {
                return false;
            }
            return insertTarget.testInsertItem(item);
        }
        return false;
    }

    public final Item getInsertItem() {
        if (this.isBodyPart() || this.isEquipmentSlot() || this.isHollow()) {
            return this;
        }
        try {
            return this.getParent().getInsertItem();
        }
        catch (NoSuchItemException nsi) {
            return null;
        }
    }

    public final boolean testInsertItem(Item item) {
        if (item == this) {
            return false;
        }
        Creature owner = null;
        try {
            owner = Server.getInstance().getCreature(this.ownerId);
            if (owner.isPlayer()) {
                return this.testInsertItemPlayer(item, owner);
            }
            if (owner.isAnimal()) {
                return this.testInsertAnimal(item, owner);
            }
            return this.testInsertHumanoidNPC(item, owner);
        }
        catch (NoSuchPlayerException nsp) {
            if (Features.Feature.FREE_ITEMS.isEnabled() && item.isChallengeNewbieItem() && (item.isArmour() || item.isWeapon() || item.isShield())) {
                return false;
            }
            if (this.isHollow()) {
                return this.testInsertHollowItem(item, true);
            }
            Item insertTarget = this.getInsertItem();
            if (insertTarget == null) {
                return false;
            }
            return insertTarget.testInsertItem(item);
        }
        catch (NoSuchCreatureException nsc) {
            String msg = "Unable to find owner for body part (creature). Part: " + this.name + " ownerID: " + this.ownerId;
            Item.logWarn(msg, nsc);
            return false;
        }
    }

    public int getFreeVolume() {
        return this.getContainerVolume() - this.getUsedVolume();
    }

    public final Item getFirstContainedItem() {
        Item[] contained = this.getItemsAsArray();
        if (contained == null || contained.length == 0) {
            return null;
        }
        return contained[0];
    }

    public final boolean insertItem(Item item) {
        return this.insertItem(item, false, false);
    }

    public final boolean insertItem(Item item, boolean unconditionally) {
        return this.insertItem(item, unconditionally, false);
    }

    public final boolean insertItem(Item item, boolean unconditionally, boolean checkItemCount) {
        return this.insertItem(item, unconditionally, checkItemCount, false);
    }

    public final boolean insertItem(Item item, boolean unconditionally, boolean checkItemCount, boolean isPlaced) {
        boolean toReturn = false;
        if (item == this) {
            Item.logWarn("Tried to insert same item into an item: ", new Exception());
            return false;
        }
        if (this.isBodyPart()) {
            Item armour = null;
            Item held = null;
            if (!item.isBodyPartAttached()) {
                Item leftWeapon;
                Item rightWeapon;
                Creature owner2;
                short lPlace = this.getPlace();
                if (!(item.getDamagePercent() <= 0 || lPlace != 38 && lPlace != 37 || unconditionally)) {
                    try {
                        owner2 = Server.getInstance().getCreature(this.ownerId);
                        rightWeapon = owner2.getRighthandWeapon();
                        if (rightWeapon != null && item.isTwoHanded()) {
                            return false;
                        }
                        if (rightWeapon != null && lPlace == 38) {
                            return false;
                        }
                        if (rightWeapon != null && rightWeapon.isTwoHanded()) {
                            return false;
                        }
                        leftWeapon = owner2.getLefthandWeapon();
                        if (leftWeapon != null && lPlace == 37) {
                            return false;
                        }
                        if (leftWeapon != null && item.isTwoHanded()) {
                            return false;
                        }
                        if (leftWeapon != null && leftWeapon.isTwoHanded()) {
                            return false;
                        }
                        Item shield = owner2.getShield();
                        if (shield != null && item.isTwoHanded()) {
                            return false;
                        }
                        if (shield != null && lPlace == 37) {
                            return false;
                        }
                        if (!owner2.hasHands()) {
                            boolean found = false;
                            byte[] armourplaces = item.getBodySpaces();
                            for (int x = 0; x < armourplaces.length; ++x) {
                                if (armourplaces[x] != lPlace) continue;
                                found = true;
                                break;
                            }
                            if (!found) {
                                return false;
                            }
                        }
                    }
                    catch (NoSuchPlayerException owner2) {
                    }
                    catch (NoSuchCreatureException owner2) {
                        // empty catch block
                    }
                }
                if (item.isShield()) {
                    if (lPlace == 44) {
                        if (!unconditionally) {
                            try {
                                owner2 = Server.getInstance().getCreature(this.ownerId);
                                rightWeapon = owner2.getRighthandWeapon();
                                if (rightWeapon != null && rightWeapon.isTwoHanded()) {
                                    return false;
                                }
                                leftWeapon = owner2.getLefthandWeapon();
                                if (leftWeapon != null) {
                                    return false;
                                }
                            }
                            catch (NoSuchPlayerException owner3) {
                            }
                            catch (NoSuchCreatureException owner3) {}
                        }
                    } else if (lPlace == 13 || lPlace == 14) {
                        try {
                            owner2 = Server.getInstance().getCreature(this.ownerId);
                            owner2.getCommunicator().sendNormalServerMessage("You need to wear the " + item.getName() + " on the left arm.");
                            return false;
                        }
                        catch (NoSuchPlayerException owner4) {
                        }
                        catch (NoSuchCreatureException owner4) {}
                    }
                } else if (item.isBelt() && lPlace == 43) {
                    try {
                        owner2 = Server.getInstance().getCreature(this.ownerId);
                        if (owner2.getWornBelt() != null) {
                            return false;
                        }
                    }
                    catch (NoSuchPlayerException owner5) {
                    }
                    catch (NoSuchCreatureException owner5) {
                        // empty catch block
                    }
                }
                if (this.place != 2 || item.getTemplateId() != 740) {
                    for (Item tocheck : this.getItems()) {
                        if (tocheck.isBodyPart()) continue;
                        if (tocheck.isArmour()) {
                            if (armour == null) {
                                byte[] armourplaces = tocheck.getBodySpaces();
                                for (int x = 0; x < armourplaces.length; ++x) {
                                    if (armourplaces[x] != lPlace) continue;
                                    armour = tocheck;
                                }
                                if (armour != null) continue;
                                held = tocheck;
                                continue;
                            }
                            if (held == null) {
                                held = tocheck;
                                continue;
                            }
                            if (unconditionally) continue;
                            return false;
                        }
                        if (held == null) {
                            held = tocheck;
                            continue;
                        }
                        if (unconditionally) continue;
                        return false;
                    }
                }
            }
            if (item.isArmour()) {
                byte[] armourplaces;
                boolean worn;
                if (this.place == 13 || this.place == 14) {
                    worn = false;
                    if (armour == null) {
                        armourplaces = item.getBodySpaces();
                        for (int x = 0; x < armourplaces.length; ++x) {
                            if (armourplaces[x] != this.place) continue;
                            worn = true;
                            this.sendWear(item, (byte)this.place);
                        }
                    }
                    if (!worn) {
                        if (held != null) {
                            return false;
                        }
                        this.sendHold(item);
                    }
                } else if (armour == null) {
                    worn = false;
                    armourplaces = item.getBodySpaces();
                    for (int x = 0; x < armourplaces.length; ++x) {
                        Kingdom kingdom;
                        if (armourplaces[x] != this.place) continue;
                        if (item.isHollow() && !item.isEmpty(false)) {
                            try {
                                Creature owner = Server.getInstance().getCreature(this.ownerId);
                                owner.getCommunicator().sendNormalServerMessage("There is not enough room in the " + item.getName() + " for your " + owner.getBody().getWoundLocationString(this.place) + " and all the other items in it!");
                            }
                            catch (NoSuchPlayerException owner) {
                            }
                            catch (NoSuchCreatureException owner) {
                                // empty catch block
                            }
                            return false;
                        }
                        worn = true;
                        this.sendWear(item, (byte)this.place);
                        if (!item.isRoyal() || this.place != 1 && this.place != 28 || item.getTemplateId() != 530 && item.getTemplateId() != 533 && item.getTemplateId() != 536 || item.getKingdom() == 0 || (kingdom = Kingdoms.getKingdom(item.getKingdom())) == null || !kingdom.existsHere() || !kingdom.isCustomKingdom()) continue;
                        try {
                            King k;
                            Creature owner = Server.getInstance().getCreature(this.getOwnerId());
                            if (owner.getKingdomId() != item.getKingdom() || owner.isChampion() || (k = King.getKing(item.getKingdom())) != null && k.kingid == this.getOwnerId()) continue;
                            King.createKing(item.getKingdom(), owner.getName(), owner.getWurmId(), owner.getSex());
                            NewKingQuestion nk = new NewKingQuestion(owner, "New ruler!", "Congratulations!", owner.getWurmId());
                            nk.sendQuestion();
                            continue;
                        }
                        catch (NoSuchCreatureException nsc) {
                            Item.logWarn(item.getName() + ": " + nsc.getMessage(), nsc);
                            continue;
                        }
                        catch (NoSuchPlayerException nsp) {
                            Item.logWarn(item.getName() + ": " + nsp.getMessage(), nsp);
                        }
                    }
                    if (!worn && !unconditionally) {
                        return false;
                    }
                } else if (!unconditionally) {
                    return false;
                }
            } else if (item.isShield()) {
                if (held != null && !unconditionally) {
                    return false;
                }
                if (this.place == 13 || this.place == 14) {
                    this.sendHold(item);
                } else if (this.place == 44) {
                    this.sendWearShield(item);
                }
            } else if (item.isBelt()) {
                if (held != null && !unconditionally) {
                    return false;
                }
                if (this.place == 13 || this.place == 14) {
                    this.sendHold(item);
                } else if (this.place == 43) {
                    this.sendWear(item, (byte)this.place);
                }
            } else {
                if (held != null && !unconditionally) {
                    return false;
                }
                if (this.place == 37 || this.place == 38) {
                    this.sendHold(item);
                } else {
                    this.sendWear(item, (byte)this.place);
                }
            }
            this.addItem(item, false);
            this.setThisAsParentFor(item, false);
            toReturn = true;
        } else if (this.isHollow()) {
            if (this.isNoDrop() && item.isArtifact()) {
                return false;
            }
            int freevol = this.getFreeVolume();
            if (unconditionally || this.itemCanBeInserted(item)) {
                boolean canInsert = true;
                if (checkItemCount && !unconditionally) {
                    canInsert = this.mayCreatureInsertItem();
                }
                if (unconditionally || this.getTemplateId() == 177 || (this.getTemplateId() == 0 || freevol >= item.getVolume() || this.doesContainerRestrictionsAllowItem(item)) && canInsert) {
                    Item parent;
                    if (this.getTemplateId() == 621 && item.isSaddleBags() && (parent = this.getParentOrNull()) != null && parent.isSaddleBags()) {
                        return false;
                    }
                    if (this.getTemplateId() == 1404 && (item.getTemplateId() == 1272 || item.getTemplateId() == 748)) {
                        item.setTemplateId(1403);
                        item.setName("blank report");
                        item.sendUpdate();
                    }
                    item.setPlacedOnParent(isPlaced);
                    this.addItem(item, false);
                    this.setThisAsParentFor(item, true);
                    this.updatePileMaterial();
                    toReturn = true;
                } else if (freevol <= item.getWeightGrams() * 10) {
                    Item.logInfo(this.getName() + " freevol(" + freevol + ")<=" + item.getName() + ".getWeightGrams()*10 (" + item.getWeightGrams() + ")", new Exception());
                }
            }
        } else {
            Item insertTarget = this.getInsertItem();
            if (insertTarget == null) {
                return false;
            }
            return insertTarget.insertItem(item, unconditionally);
        }
        return toReturn;
    }

    public boolean doesContainerRestrictionsAllowItem(Item item) {
        if (this.getTemplateId() == 1404 && (item.getTemplateId() == 748 || item.getTemplateId() == 1272 || item.getTemplateId() == 1403)) {
            return true;
        }
        if (this.getTemplate().getContainerRestrictions() == null) {
            return false;
        }
        for (ContainerRestriction cRest : this.getTemplate().getContainerRestrictions()) {
            if (!cRest.canInsertItem(this.getItemsAsArray(), item)) continue;
            return true;
        }
        return false;
    }

    public boolean itemCanBeInserted(Item item) {
        if (this.getTemplate().getContainerRestrictions() != null) {
            return this.doesContainerRestrictionsAllowItem(item);
        }
        if (this.getTemplateId() == 1409 && item.getTemplateId() != 748 && item.getTemplateId() != 1272) {
            return false;
        }
        if (this.getTemplateId() == 1404) {
            if (item.getTemplateId() != 748 && item.getTemplateId() != 1272 && item.getTemplateId() != 1403) {
                return false;
            }
            if (item.getTemplateId() != 1403 && (item.getAuxData() > 0 || item.getInscription() != null)) {
                return false;
            }
            return !((double)this.getItemCount() >= Math.max(22.0, Math.floor(this.getCurrentQualityLevel())));
        }
        if ((item.isSaddleBags() || item.getTemplateId() == 621) && (this.isSaddleBags() || this.isInside(1333, 1334))) {
            return false;
        }
        if (this.getContainerSizeX() >= item.getSizeX() && this.getContainerSizeY() >= item.getSizeY() && this.getContainerSizeZ() > item.getSizeZ()) {
            return true;
        }
        if (this.getTemplateId() == 177) {
            return true;
        }
        if (this.getTemplateId() == 0) {
            return true;
        }
        if (item.isHollow()) {
            return false;
        }
        return (item.isCombine() || item.isFood() || item.isLiquid()) && this.getFreeVolume() >= item.getVolume();
    }

    private boolean isMultipleMaterialPileTemplate(int templateId) {
        return templateId == 9;
    }

    private void updatePileMaterial() {
        if (this.getTemplateId() == 177) {
            boolean multipleMaterials = false;
            byte currentMaterial = 0;
            Item[] itemsArray = this.getItemsAsArray();
            byte firstMaterial = 0;
            int firstItem = 0;
            int currentItem = 0;
            for (int i = 0; i < itemsArray.length; ++i) {
                currentMaterial = itemsArray[i].getMaterial();
                currentItem = itemsArray[i].getTemplateId();
                if (i == 0) {
                    firstMaterial = currentMaterial;
                    firstItem = currentItem;
                }
                if (currentItem != firstItem) {
                    multipleMaterials = true;
                    break;
                }
                if (currentMaterial == firstMaterial || this.isMultipleMaterialPileTemplate(currentItem)) continue;
                multipleMaterials = true;
                break;
            }
            if (multipleMaterials) {
                boolean changed = false;
                if (this.getData1() != -1) {
                    this.setData1(-1);
                    changed = true;
                }
                if (this.getMaterial() != 0) {
                    this.setMaterial((byte)0);
                    changed = true;
                }
                if (changed) {
                    this.updateModelNameOnGroundItem();
                }
            }
        }
    }

    private void sendHold(Item item) {
        String holdst = "hold";
        Creature owner = this.getOwnerOrNull();
        if (owner == null) {
            return;
        }
        String hand = "right hand";
        if (this.place == 37) {
            hand = "left hand";
        }
        if (item.isTwoHanded()) {
            hand = "two hands";
        }
        if (!owner.getCommunicator().stillLoggingIn()) {
            owner.getCommunicator().sendNormalServerMessage("You hold " + item.getNameWithGenus().toLowerCase() + " in your " + hand + ".");
            PlayerTutorial.firePlayerTrigger(owner.getWurmId(), PlayerTutorial.PlayerTrigger.EQUIPPED_ITEM);
        }
        boolean send = true;
        if (item.isArmour()) {
            byte[] armourplaces = item.getBodySpaces();
            for (int x = 0; x < armourplaces.length; ++x) {
                if (armourplaces[x] != this.place) continue;
                send = false;
            }
        }
        if (item.isWeapon() && item.getSpellEffects() != null) {
            owner.achievement(581);
        }
        if (send) {
            owner.getCurrentTile().sendWieldItem(this.getOwnerId(), this.place == 37 ? (byte)0 : 1, item.getModelName(), item.getRarity(), WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), WurmColor.getColorBlue(item.getColor()), WurmColor.getColorRed(item.getColor2()), WurmColor.getColorGreen(item.getColor2()), WurmColor.getColorBlue(item.getColor2()));
            byte equipementSlot = BodyTemplate.convertToItemEquipementSlot((byte)this.place);
            owner.getCurrentTile().sendWearItem(this.getOwnerId(), item.getTemplateId(), equipementSlot, WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), WurmColor.getColorBlue(item.getColor()), WurmColor.getColorRed(item.getColor2()), WurmColor.getColorGreen(item.getColor2()), WurmColor.getColorBlue(item.getColor2()), item.getMaterial(), item.getRarity());
            owner.getCombatHandler().setCurrentStance(-1, (byte)0);
        }
    }

    private void sendWear(Item item, byte bodyPart) {
        if (!item.isBodyPartAttached()) {
            byte equipmentSlot;
            Creature owner = this.getOwnerOrNull();
            if (owner == null) {
                return;
            }
            if (!owner.getCommunicator().stillLoggingIn()) {
                owner.getCommunicator().sendNormalServerMessage("You wear " + item.getNameWithGenus().toLowerCase() + ".");
                PlayerTutorial.firePlayerTrigger(owner.getWurmId(), PlayerTutorial.PlayerTrigger.EQUIPPED_ITEM);
            }
            byte by = equipmentSlot = item.isArmour() ? BodyTemplate.convertToArmorEquipementSlot(bodyPart) : BodyTemplate.convertToItemEquipementSlot(bodyPart);
            if (owner.isAnimal() && owner.isVehicle()) {
                owner.getCurrentTile().sendHorseWear(this.getOwnerId(), item.getTemplateId(), item.getMaterial(), equipmentSlot, item.getAuxData());
            } else {
                owner.getCurrentTile().sendWearItem(this.getOwnerId(), item.getTemplateId(), equipmentSlot, WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), WurmColor.getColorBlue(item.getColor()), WurmColor.getColorRed(item.getColor2()), WurmColor.getColorGreen(item.getColor2()), WurmColor.getColorBlue(item.getColor2()), item.getMaterial(), item.getRarity());
            }
            if (item.isArmour()) {
                item.setWornAsArmour(true, this.getOwnerId());
            }
            if (item.getTemplateId() == 330) {
                owner.setHasCrownEffect(true);
            }
            if (item.hasItemBonus() && !Servers.localServer.PVPSERVER) {
                ItemBonus.calcAndAddBonus(item, owner);
            }
            if (item.isPriceEffectedByMaterial()) {
                if (item.getTemplateId() == 297) {
                    owner.achievement(94);
                } else if (item.getTemplateId() == 230) {
                    owner.achievement(95);
                } else if (item.getTemplateId() == 231) {
                    owner.achievement(96);
                }
            }
            if (item.isWeapon() && item.getSpellEffects() != null) {
                owner.achievement(581);
            }
        }
    }

    private void sendWearShield(Item item) {
        if (!item.isBodyPartAttached()) {
            Creature owner = this.getOwnerOrNull();
            if (owner == null) {
                return;
            }
            if (!owner.getCommunicator().stillLoggingIn()) {
                owner.getCommunicator().sendNormalServerMessage("You wear " + item.getNameWithGenus().toLowerCase() + " as shield.");
                PlayerTutorial.firePlayerTrigger(owner.getWurmId(), PlayerTutorial.PlayerTrigger.EQUIPPED_ITEM);
            }
            byte equipementSlot = BodyTemplate.convertToItemEquipementSlot((byte)this.place);
            owner.getCurrentTile().sendWearItem(this.getOwnerId(), item.getTemplateId(), equipementSlot, WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), WurmColor.getColorBlue(item.getColor()), WurmColor.getColorRed(item.getColor2()), WurmColor.getColorGreen(item.getColor2()), WurmColor.getColorBlue(item.getColor2()), item.getMaterial(), item.getRarity());
            owner.getCommunicator().sendToggleShield(true);
        }
    }

    public final boolean isEmpty(boolean checkInitialContainers) {
        if (checkInitialContainers && this.getTemplate().getInitialContainers() != null) {
            for (Item item : this.getItemsAsArray()) {
                if (item.isEmpty(false)) continue;
                return false;
            }
            return true;
        }
        if (this.items == null || this.items.isEmpty()) {
            return true;
        }
        for (Item item : this.getItemsAsArray()) {
            if (item.isTemporary()) continue;
            return false;
        }
        return true;
    }

    public final void addCreationWindowWatcher(Player creature) {
        if (this.watchers == null) {
            this.watchers = new HashSet<Creature>();
        }
        if (!this.watchers.contains(creature)) {
            this.watchers.add(creature);
        }
    }

    public final void addWatcher(long inventoryWindow, Creature creature) {
        if (this.watchers == null) {
            this.watchers = new HashSet<Creature>();
        }
        if (!this.watchers.contains(creature)) {
            this.watchers.add(creature);
        }
        if (inventoryWindow >= 1L && inventoryWindow <= 4L) {
            if (this.tradeWindow != null && this.tradeWindow.getWurmId() == inventoryWindow) {
                if (this.parentId != -10L) {
                    try {
                        if (Items.getItem(this.parentId).isViewableBy(creature)) {
                            creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
                        }
                        if (this.isBodyPart()) {
                            this.sendContainedItems(inventoryWindow, creature);
                        }
                    }
                    catch (NoSuchItemException nsi) {
                        Item.logWarn(this.id + " has parent " + this.parentId + " but " + nsi.getMessage(), nsi);
                    }
                } else if (this.getTemplateId() == 0 || this.isBodyPart()) {
                    creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
                    this.sendContainedItems(inventoryWindow, creature);
                }
            }
        } else if (this.parentId != -10L) {
            try {
                if (Items.getItem(this.parentId).isViewableBy(creature)) {
                    Item parentWindow;
                    if (this.isInside(1333, 1334)) {
                        Item parentBags = this.getFirstParent(1333, 1334);
                        creature.getCommunicator().sendAddToInventory(this, parentBags.getWurmId(), parentBags.getWurmId(), -1);
                    }
                    if ((parentWindow = this.recursiveParentCheck()) != null && parentWindow != this) {
                        creature.getCommunicator().sendAddToInventory(this, parentWindow.getWurmId(), parentWindow.getWurmId(), -1);
                    }
                    creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
                }
                if (this.isBodyPart()) {
                    this.sendContainedItems(inventoryWindow, creature);
                }
            }
            catch (NoSuchItemException nsi) {
                Item.logWarn(this.id + " has parent " + this.parentId + " but " + nsi.getMessage(), nsi);
            }
        } else if (this.getTemplateId() == 0) {
            creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
        } else if (this.isBodyPart()) {
            creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
            this.sendContainedItems(inventoryWindow, creature);
        } else if (this.isHollow()) {
            VolaTile t;
            if (this.isBanked()) {
                creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
            }
            if (this.watchers.size() == 1 && (t = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface())) != null && this.getTopParent() == this.getWurmId()) {
                t.sendAnimation(creature, this, "open", false, false);
            }
        } else {
            creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
        }
    }

    public final void sendContainedItems(long inventoryWindow, Creature creature) {
        if (!this.isHollow()) {
            return;
        }
        if (!this.isViewableBy(creature)) {
            return;
        }
        int sentCount = 0;
        for (Item item : this.getItems()) {
            Item[] cargo;
            if (sentCount >= 1000) break;
            if (!this.isCrate() || !item.isBulkItem()) {
                item.addWatcher(inventoryWindow, creature);
                ++sentCount;
                continue;
            }
            int storageSpace = this.template.templateId == 852 ? 300 : 150;
            for (Item cargoItem : cargo = this.getItemsAsArray()) {
                ItemTemplate itemp;
                int count = cargoItem.getBulkNums();
                if (count <= storageSpace || (itemp = cargoItem.getRealTemplate()) == null) continue;
                String cargoName = cargoItem.getName();
                int newSize = itemp.getVolume() * storageSpace;
                int oldSize = cargoItem.getVolume();
                String toSend = "Trimming size of " + cargoName + " to " + newSize + " instead of " + oldSize + " at " + this.getTileX() + "," + this.getTileY();
                Item.logInfo(toSend);
                Message mess = new Message(null, 11, "GM", "<System> " + toSend);
                Server.getInstance().addMessage(mess);
                Players.addGmMessage("System", "Trimming crate size of " + cargoName + " to " + newSize + " instead of " + oldSize);
                cargoItem.setWeight(newSize, true);
            }
            item.addWatcher(inventoryWindow, creature);
            ++sentCount;
        }
        if (this.getItemCount() > 0) {
            creature.getCommunicator().sendIsEmpty(inventoryWindow, this.getWurmId());
        }
    }

    public final void removeWatcher(Creature creature, boolean send) {
        this.removeWatcher(creature, send, false);
    }

    public final void removeWatcher(Creature creature, boolean send, boolean recursive) {
        if (this.watchers != null && this.watchers.contains(creature)) {
            this.watchers.remove(creature);
            if (this.parentId != -10L && send) {
                creature.getCommunicator().sendRemoveFromInventory(this);
            }
            if (this.isHollow()) {
                VolaTile t;
                if (this.items != null) {
                    for (Item item : this.items) {
                        item.removeWatcher(creature, false, true);
                    }
                }
                if (this.watchers.isEmpty() && !recursive && (t = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface())) != null && (this.getTopParent() == this.getWurmId() || this.isPlacedOnParent())) {
                    t.sendAnimation(creature, this, "close", false, false);
                }
            }
        }
    }

    public final Set<Creature> getWatcherSet() {
        return this.watchers;
    }

    @Nonnull
    public final Creature[] getWatchers() throws NoSuchCreatureException {
        if (this.watchers != null) {
            return this.watchers.toArray(new Creature[this.watchers.size()]);
        }
        throw new NoSuchCreatureException("Not watched");
    }

    public final boolean isViewableBy(Creature creature) {
        if (this.parentId == this.id) {
            Item.logWarn("This shouldn't happen!");
            return true;
        }
        if (this.isLockable() && this.getLockId() != -10L) {
            try {
                Item lock = Items.getItem(this.lockid);
                if (creature.hasKeyForLock(lock) || this.isDraggable() && MethodsItems.mayUseInventoryOfVehicle(creature, this) || this.getTemplateId() == 850 && MethodsItems.mayUseInventoryOfVehicle(creature, this) || this.isLocked() && this.mayAccessHold(creature)) {
                    if (this.parentId != -10L) {
                        return this.getParent().isViewableBy(creature);
                    }
                    return true;
                }
                return false;
            }
            catch (NoSuchItemException nsi) {
                Item.logWarn(this.id + " is locked but lock " + this.lockid + " can not be found.", nsi);
                try {
                    if (this.parentId != -10L) {
                        return this.getParent().isViewableBy(creature);
                    }
                    return true;
                }
                catch (NoSuchItemException nsa) {
                    Item.logWarn(this.id + " has parent " + this.parentId + " but " + nsa.getMessage(), nsa);
                }
            }
        }
        if (this.parentId == -10L) {
            return true;
        }
        try {
            return this.getParent().isViewableBy(creature);
        }
        catch (NoSuchItemException nsi) {
            Item.logWarn(this.id + " has parent " + this.parentId + " but " + nsi.getMessage(), nsi);
            return true;
        }
    }

    public final Item getParent() throws NoSuchItemException {
        if (this.parentId != -10L) {
            return Items.getItem(this.parentId);
        }
        throw new NoSuchItemException("No parent.");
    }

    final long getLastOwner() {
        return this.lastOwner;
    }

    public final long getLastParentId() {
        return this.lastParentId;
    }

    private void setThisAsParentFor(Item item, boolean forceUpdateParent) {
        if (item.getDbStrings() instanceof FrozenItemDbStrings) {
            item.returnFromFreezer();
            item.deleteInDatabase();
            item.setDbStrings(ItemDbStrings.getInstance());
            Item.logInfo("Returning from frozen: " + item.getName() + " " + item.getWurmId(), new Exception());
        }
        if (item.getWatcherSet() != null) {
            try {
                for (Creature watcher : item.getWatchers()) {
                    if (this.watchers == null || this.watchers.contains(watcher)) continue;
                    item.removeWatcher(watcher, true);
                }
            }
            catch (NoSuchCreatureException noSuchCreatureException) {
                // empty catch block
            }
        }
        if (forceUpdateParent) {
            try {
                Item oldParent = item.getParent();
                if (!oldParent.hasSameOwner(item)) {
                    oldParent.dropItem(item.getWurmId(), false);
                } else if (this != oldParent) {
                    oldParent.removeItem(item.getWurmId(), false, false, false);
                }
            }
            catch (NoSuchItemException oldParent) {
                // empty catch block
            }
        }
        if (this.getTemplateId() == 621 && item.isSaddleBags()) {
            try {
                Creature owner = Server.getInstance().getCreature(this.ownerId);
                if (owner.isAnimal() && owner.isVehicle()) {
                    byte equipmentSlot = BodyTemplate.convertToItemEquipementSlot((byte)this.getParent().place);
                    owner.getCurrentTile().sendHorseWear(this.ownerId, this.getTemplateId(), this.getMaterial(), equipmentSlot, this.getAuxData());
                }
            }
            catch (NoSuchItemException | NoSuchPlayerException | NoSuchCreatureException owner) {
                // empty catch block
            }
        }
        item.setParentId(this.id, this.isOnSurface());
        item.lastParentId = this.id;
        long itemOwnerId = -10L;
        long lOwnerId = this.getOwnerId();
        try {
            itemOwnerId = item.getOwner();
            if (itemOwnerId != lOwnerId) {
                item.setOwner(lOwnerId, true);
            } else if (this.watchers != null) {
                for (Creature watcher : this.watchers) {
                    if (item.watchers != null && item.watchers.contains(watcher)) continue;
                    item.addWatcher(this.getTopParent(), watcher);
                }
            }
        }
        catch (NotOwnedException ex) {
            try {
                item.setOwner(lOwnerId, true);
                if (lOwnerId == -10L) {
                    VolaTile t;
                    if (this.watchers != null) {
                        for (Creature watcher : this.watchers) {
                            long inventoryWindow = item.getTopParent();
                            item.addWatcher(inventoryWindow, watcher);
                        }
                    }
                    if (this.isFire()) {
                        if ((item.isFoodMaker() || item.isFood()) && (t = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface())) != null) {
                            t.renameItem(this);
                        }
                    } else if ((this.isWeaponContainer() || this.isBarrelRack()) && (t = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface())) != null) {
                        t.renameItem(this);
                    }
                }
            }
            catch (Exception ex2) {
                Item.logWarn("Failed to set ownerId to " + lOwnerId + " for item " + item.getWurmId(), ex2);
            }
        }
    }

    public final void setOwner(long newOwnerId, boolean startWatching) {
        this.setOwner(newOwnerId, -10L, startWatching);
    }

    public final void setOwner(long newOwnerId, long newParent, boolean startWatching) {
        long oldOwnerId = this.getOwnerId();
        if (this.isCoin() && this.getValue() >= 1000000) {
            Item.logInfo("COINLOG " + newOwnerId + ", " + this.getWurmId() + " banked " + this.banked + " mailed=" + this.mailed, new Exception());
        }
        if (newOwnerId != -10L) {
            if (oldOwnerId == -10L || oldOwnerId != newOwnerId) {
                int timesSinceLastUsed;
                if (oldOwnerId == -10L && (timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / this.template.getDecayTime())) > 0) {
                    this.setLastMaintained(WurmCalendar.currentTime);
                }
                this.setZoneId(-10, this.surfaced);
                this.setOwnerId(newOwnerId);
                this.watchers = null;
                Creature owner = null;
                try {
                    owner = Server.getInstance().getCreature(newOwnerId);
                    if (Constants.useItemTransferLog) {
                        ItemTransfer transfer = new ItemTransfer(this.id, this.name, oldOwnerId, String.valueOf(oldOwnerId), newOwnerId, owner.getName(), System.currentTimeMillis());
                        itemLogger.addToQueue(transfer);
                    }
                    if (this.isCoin()) {
                        Server.getInstance().transaction(this.id, oldOwnerId, newOwnerId, owner.getName(), this.getValue());
                        owner.addCarriedWeight(this.getWeightGrams());
                    } else if (this.isBodyPart()) {
                        if (this.isBodyPartRemoved()) {
                            owner.addCarriedWeight(this.getWeightGrams());
                        }
                    } else {
                        owner.addCarriedWeight(this.getWeightGrams());
                    }
                    if (startWatching) {
                        try {
                            Creature[] watcherArr = this.getParent().getWatchers();
                            long tp = this.getTopParent();
                            for (Creature c : watcherArr) {
                                if (c == owner) {
                                    this.addWatcher(-1L, owner);
                                    continue;
                                }
                                this.addWatcher(tp, c);
                            }
                        }
                        catch (NoSuchItemException nsi) {
                            this.addWatcher(-1L, owner);
                        }
                    }
                    if (this.isArtifact() || this.isUnique() && !this.isRoyal()) {
                        owner.getCommunicator().sendNormalServerMessage("You will drop the " + this.getName() + " when you leave the world.");
                        if (this.getTemplateId() == 329) {
                            owner.getCombatHandler().setRodEffect(true);
                        }
                        if (this.getTemplateId() == 335) {
                            owner.setHasFingerEffect(true);
                        }
                        if (this.getTemplateId() == 331 || this.getTemplateId() == 330) {
                            owner.getCommunicator().sendAlertServerMessage("Also, when you drop the " + this.getName() + ", any aggressive pet you have will become enraged and lose its loyalty.");
                        }
                        if (this.getTemplateId() == 338) {
                            owner.getCommunicator().sendAlertServerMessage("Also, when you drop the " + this.getName() + ", any pet you have will become enraged and lose its loyalty.");
                        }
                        if (this.isArtifact() && this.getAuxData() > 30) {
                            if (Servers.isThisATestServer() && Servers.isThisAPvpServer() && owner.getPower() >= 2) {
                                owner.getCommunicator().sendNormalServerMessage("Old power = " + this.getAuxData() + ".");
                            }
                            this.setAuxData((byte)30);
                            if (Servers.isThisATestServer() && Servers.isThisAPvpServer() && owner.getPower() >= 2) {
                                owner.getCommunicator().sendNormalServerMessage("New power = " + this.getAuxData() + ".");
                            }
                        }
                    }
                    if (this.isKey()) {
                        owner.addKey(this, false);
                    }
                }
                catch (NoSuchPlayerException nsi) {
                }
                catch (NoSuchCreatureException nsi) {}
            } else if ((long)this.zoneId != -10L) {
                Item.logWarn(this.getName() + " new owner " + newOwnerId + " zone id was " + this.zoneId, new Exception());
                this.setZoneId(-10, true);
            }
        } else if (oldOwnerId != -10L) {
            Creature creature = null;
            try {
                long decayt;
                int timesSinceLastUsed;
                this.setZoneId(-10, true);
                creature = Server.getInstance().getCreature(oldOwnerId);
                if (Constants.useItemTransferLog && !this.isBodyPartAttached() && !this.isInventory()) {
                    ItemTransfer transfer = new ItemTransfer(this.id, this.name, oldOwnerId, creature.getName(), newOwnerId, "" + this.ownerId, System.currentTimeMillis());
                    itemLogger.addToQueue(transfer);
                }
                if (!this.isLocked()) {
                    this.setLastOwnerId(oldOwnerId);
                }
                if (this.isCoin()) {
                    if (this.getParentId() != -10L) {
                        Server.getInstance().transaction(this.id, oldOwnerId, newOwnerId, creature.getName(), this.getValue());
                        if (!creature.removeCarriedWeight(this.getWeightGrams())) {
                            Item.logWarn(this.getName() + " removed " + this.getWeightGrams(), new Exception());
                        }
                    }
                } else if (this.isBodyPart()) {
                    if (this.isBodyPartRemoved() && !creature.removeCarriedWeight(this.getWeightGrams())) {
                        Item.logWarn(this.getName() + " removed " + this.getWeightGrams(), new Exception());
                    }
                } else if (!creature.removeCarriedWeight(this.getWeightGrams())) {
                    Item.logWarn(this.getName() + " removed " + this.getWeightGrams(), new Exception());
                }
                if (this.isArmour()) {
                    this.setWornAsArmour(false, oldOwnerId);
                }
                if (this.isArtifact()) {
                    if (this.getTemplateId() == 329) {
                        creature.getCombatHandler().setRodEffect(false);
                    }
                    if (this.getTemplateId() == 335) {
                        creature.setHasFingerEffect(false);
                    }
                    if (this.getTemplateId() == 330) {
                        creature.setHasCrownEffect(false);
                    }
                    if (this.getTemplateId() == 331 || this.getTemplateId() == 330 || this.getTemplateId() == 338) {
                        boolean untame = false;
                        if (newParent != -10L) {
                            Item newParentItem = Items.getItem(newParent);
                            if (newParentItem.getOwnerId() != oldOwnerId) {
                                untame = true;
                            }
                        } else {
                            untame = true;
                        }
                        if (creature.getPet() != null && untame) {
                            Creature pet = creature.getPet();
                            creature.getCommunicator().sendAlertServerMessage("As you drop the " + this.getName() + ", you feel rage and confusion from the " + pet.getName() + ".");
                            creature.setPet(-10L);
                            pet.setLoyalty(0.0f);
                            pet.setDominator(-10L);
                        }
                    }
                }
                this.removeWatcher(creature, true);
                if (this.isKey()) {
                    creature.removeKey(this, false);
                }
                if (this.isLeadCreature() && creature.isItemLeading(this)) {
                    creature.dropLeadingItem(this);
                }
                if (!this.isFood() && !this.isAlwaysPoll() && (timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / (decayt = this.template.getDecayTime()))) > 0) {
                    this.setLastMaintained(WurmCalendar.currentTime);
                }
            }
            catch (NoSuchPlayerException nsp) {
                Item.logWarn("Removing object from unknown player: ", nsp);
            }
            catch (NoSuchCreatureException cnf) {
                Item.logWarn("Removing object from unknown creature: ", cnf);
            }
            catch (Exception ex) {
                Item.logWarn("Failed to save creature when dropping item with id " + this.id, ex);
            }
            this.setOwnerId(-10L);
        } else {
            this.setOwnerId(-10L);
        }
        if (this.isHollow() && this.items != null) {
            for (Item item : this.items) {
                if (this.isSealedByPlayer() && item.getTemplateId() == 169) continue;
                if (item != this) {
                    item.setOwner(newOwnerId, false);
                    continue;
                }
                Item.logWarn("Item with id " + this.id + " has itself in the inventory!");
            }
        }
    }

    public final Item dropItem(long aId, boolean setPosition) throws NoSuchItemException {
        return this.dropItem(aId, -10L, setPosition, false);
    }

    public final Item dropItem(long aId, long newParent, boolean setPosition) throws NoSuchItemException {
        return this.dropItem(aId, newParent, setPosition, false);
    }

    public final Item dropItem(long aId, boolean setPosition, boolean skipPileRemoval) throws NoSuchItemException {
        return this.dropItem(aId, -10L, setPosition, skipPileRemoval);
    }

    public final Item dropItem(long aId, long newParent, boolean setPosition, boolean skipPileRemoval) throws NoSuchItemException {
        Item toReturn = this.removeItem(aId, setPosition, true, skipPileRemoval);
        toReturn.setOwner(-10L, newParent, false);
        toReturn.setParentId(-10L, this.surfaced);
        return toReturn;
    }

    public static int[] getDropTile(Creature creature) throws NoSuchZoneException {
        float yPosMod;
        float newPosY;
        float lPosX = creature.getStatus().getPositionX();
        float lPosY = creature.getStatus().getPositionY();
        if (creature.getBridgeId() != -10L) {
            int newTileX = CoordUtils.WorldToTile(lPosX);
            int newTileY = CoordUtils.WorldToTile(lPosY);
            return new int[]{newTileX, newTileY};
        }
        float rot = creature.getStatus().getRotation();
        float xPosMod = (float)Math.sin(rot * ((float)Math.PI / 180)) * (1.0f + Server.rand.nextFloat());
        float newPosX = lPosX + xPosMod;
        BlockingResult result = Blocking.getBlockerBetween(creature, lPosX, lPosY, newPosX, newPosY = lPosY + (yPosMod = -((float)Math.cos(rot * ((float)Math.PI / 180))) * (1.0f + Server.rand.nextFloat())), creature.getPositionZ(), creature.getPositionZ(), creature.isOnSurface(), creature.isOnSurface(), false, 4, -1L, creature.getBridgeId(), creature.getBridgeId(), false);
        if (result != null) {
            newPosX = lPosX + (float)Math.sin(rot * ((float)Math.PI / 180)) * (-1.0f + Server.rand.nextFloat());
            newPosY = lPosY - (float)Math.cos(rot * ((float)Math.PI / 180)) * (-1.0f + Server.rand.nextFloat());
        }
        int newTileX = CoordUtils.WorldToTile(newPosX);
        int newTileY = CoordUtils.WorldToTile(newPosY);
        return new int[]{newTileX, newTileY};
    }

    public final void putItemInCorner(Creature creature, int cornerX, int cornerY, boolean onSurface, long bridgeId, boolean atFeet) throws NoSuchItemException {
        float lRotation = this.isRoadMarker() ? 0.0f : (this.isTileAligned() ? 90.0f * Creature.normalizeAngle((int)((creature.getStatus().getRotation() + 45.0f) / 90.0f)) : Creature.normalizeAngle(creature.getStatus().getRotation()));
        long lParentId = this.getParentId();
        if (lParentId != -10L) {
            Item parent = Items.getItem(lParentId);
            parent.dropItem(this.getWurmId(), false);
        }
        float newPosX = CoordUtils.TileToWorld(cornerX);
        float newPosY = CoordUtils.TileToWorld(cornerY);
        if (atFeet) {
            newPosX = creature.getPosX();
            newPosY = creature.getPosY();
        } else if (!this.isRoadMarker()) {
            newPosX += 0.005f;
            newPosY += 0.005f;
            if (creature.getTileX() < cornerX) {
                newPosX -= 0.01f;
            }
            if (creature.getTileY() < cornerY) {
                newPosY -= 0.01f;
            }
        }
        newPosX = Math.max(0.0f, newPosX);
        newPosY = Math.max(0.0f, newPosY);
        newPosY = Math.min(Zones.worldMeterSizeY, newPosY);
        newPosX = Math.min(Zones.worldMeterSizeX, newPosX);
        this.setOnBridge(bridgeId);
        this.setSurfaced(onSurface);
        float npsz = Zones.calculatePosZ(newPosX, newPosY, null, onSurface, this.isFloating() && this.getCurrentQualityLevel() > 10.0f, this.getPosZ(), creature, this.onBridge());
        this.setPos(newPosX, newPosY, npsz, lRotation, this.onBridge());
        try {
            Zone zone = Zones.getZone(Zones.safeTileX((int)newPosX >> 2), Zones.safeTileY((int)newPosY >> 2), onSurface);
            zone.addItem(this);
        }
        catch (NoSuchZoneException sex) {
            Item.logWarn(sex.getMessage(), sex);
            creature.getInventory().insertItem(this, true);
            creature.getCommunicator().sendNormalServerMessage("Unable to drop there.");
        }
    }

    public final void putItemInfrontof(Creature creature) throws NoSuchCreatureException, NoSuchItemException, NoSuchPlayerException, NoSuchZoneException {
        this.putItemInfrontof(creature, 1.0f);
    }

    public final void putItemInfrontof(Creature creature, float distance) throws NoSuchCreatureException, NoSuchItemException, NoSuchPlayerException, NoSuchZoneException {
        BlockingResult result;
        CreatureStatus creatureStatus = creature.getStatus();
        float lPosX = creatureStatus.getPositionX();
        float lPosY = creatureStatus.getPositionY();
        float rot = Creature.normalizeAngle(creatureStatus.getRotation());
        float xPosMod = (float)Math.sin(rot * ((float)Math.PI / 180)) * (distance + Server.rand.nextFloat() * distance);
        float yPosMod = -((float)Math.cos(rot * ((float)Math.PI / 180))) * (distance + Server.rand.nextFloat() * distance);
        float newPosX = lPosX + xPosMod;
        float newPosY = lPosY + yPosMod;
        boolean onSurface = creature.isOnSurface();
        if (distance != 0.0f && (result = Blocking.getBlockerBetween(creature, lPosX, lPosY, newPosX, newPosY, creature.getPositionZ(), creature.getPositionZ(), onSurface, onSurface, false, 4, -1L, creature.getBridgeId(), creature.getBridgeId(), false)) != null) {
            newPosX = lPosX + (float)Math.sin(rot * ((float)Math.PI / 180)) * (-1.0f + Server.rand.nextFloat());
            newPosY = lPosY - (float)Math.cos(rot * ((float)Math.PI / 180)) * (-1.0f + Server.rand.nextFloat());
        }
        this.setOnBridge(creatureStatus.getBridgeId());
        if (this.onBridge() != -10L) {
            newPosX = lPosX;
            newPosY = lPosY;
        }
        if (!onSurface && distance != 0.0f && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(Zones.safeTileX((int)newPosX >> 2), Zones.safeTileY((int)newPosY >> 2))))) {
            newPosX = lPosX;
            newPosY = lPosY;
        }
        newPosX = Math.max(0.0f, newPosX);
        newPosY = Math.max(0.0f, newPosY);
        newPosY = Math.min(Zones.worldMeterSizeY, newPosY);
        newPosX = Math.min(Zones.worldMeterSizeX, newPosX);
        float lRotation = this.isTileAligned() ? 90.0f * Creature.normalizeAngle((int)((creatureStatus.getRotation() + 45.0f) / 90.0f)) : Creature.normalizeAngle(creatureStatus.getRotation());
        long lParentId = this.getParentId();
        if (lParentId != -10L) {
            Item parent = Items.getItem(lParentId);
            parent.dropItem(this.getWurmId(), false);
        }
        float npsz = Zones.calculatePosZ(newPosX, newPosY, null, onSurface, this.isFloating() && this.getCurrentQualityLevel() > 10.0f, this.getPosZ(), creature, this.onBridge());
        this.setPos(newPosX, newPosY, npsz, lRotation, this.onBridge());
        this.setSurfaced(onSurface);
        try {
            Zone zone = Zones.getZone(Zones.safeTileX((int)newPosX >> 2), Zones.safeTileY((int)newPosY >> 2), onSurface);
            zone.addItem(this);
            if (creature.getPower() == 5 && this.isBoat()) {
                creature.getCommunicator().sendNormalServerMessage("Adding to zone " + zone.getId() + " at " + Zones.safeTileX((int)newPosX >> 2) + ", " + Zones.safeTileY((int)newPosY >> 2) + ", surf=" + onSurface);
            }
        }
        catch (NoSuchZoneException sex) {
            Item.logWarn(sex.getMessage(), sex);
            creature.getInventory().insertItem(this, true);
            creature.getCommunicator().sendNormalServerMessage("Unable to drop there.");
        }
    }

    public final float calculatePosZ(VolaTile tile, @Nullable Creature creature) {
        boolean floating = this.isFloating() && this.getCurrentQualityLevel() > 10.0f;
        return Zones.calculatePosZ(this.getPosX(), this.getPosY(), tile, this.isOnSurface(), floating, this.getPosZ(), creature, this.onBridge());
    }

    public final void updatePosZ(VolaTile tile) {
        this.setPosZ(this.calculatePosZ(tile, null));
    }

    public final boolean isWarmachine() {
        return this.template.isWarmachine();
    }

    private boolean isWeaponContainer() {
        int templateId = this.getTemplateId();
        switch (templateId) {
            case 724: 
            case 725: 
            case 758: 
            case 759: 
            case 892: {
                return true;
            }
        }
        return false;
    }

    private Item removeItem(long _id, boolean setPosition, boolean ignoreWatchers, boolean skipPileRemoval) throws NoSuchItemException {
        Object t;
        VolaTile vt;
        Creature owner;
        if (!this.isHollow()) {
            throw new NoSuchItemException(String.valueOf(_id));
        }
        Item item = Items.getItem(_id);
        if (item.isBodyPart() && item.isBodyPartAttached()) {
            throw new NoSuchItemException("Can't remove parts from a live body.");
        }
        long ownerId = this.getOwnerId();
        if (this.place > 0) {
            try {
                byte equipmentSlot = BodyTemplate.convertToItemEquipementSlot((byte)this.place);
                if (item.isArmour() && item.wornAsArmour) {
                    equipmentSlot = BodyTemplate.convertToArmorEquipementSlot((byte)this.place);
                }
                if ((owner = Server.getInstance().getCreature(ownerId)).isAnimal() && owner.isVehicle()) {
                    owner.getCurrentTile().sendRemoveHorseWear(ownerId, item.getTemplateId(), equipmentSlot);
                } else {
                    owner.getCurrentTile().sendRemoveWearItem(ownerId, equipmentSlot);
                }
                if (item.hasItemBonus() && owner.isPlayer()) {
                    ItemBonus.removeBonus(item, owner);
                }
                if (item.getTemplateId() == 330) {
                    owner.setHasCrownEffect(false);
                }
            }
            catch (Exception equipmentSlot) {
                // empty catch block
            }
        }
        this.removeItem(item);
        if (this.getTemplateId() == 621 && item.isSaddleBags()) {
            try {
                Creature owner2 = Server.getInstance().getCreature(ownerId);
                if (owner2.isAnimal() && owner2.isVehicle()) {
                    byte equipmentSlot = BodyTemplate.convertToItemEquipementSlot((byte)this.getParent().place);
                    owner2.getCurrentTile().sendHorseWear(ownerId, this.getTemplateId(), this.getMaterial(), equipmentSlot, this.getAuxData());
                }
            }
            catch (NoSuchPlayerException | NoSuchCreatureException owner2) {
                // empty catch block
            }
        }
        if (this.getTemplate().hasViewableSubItems() && (!this.getTemplate().isContainerWithSubItems() || item.isPlacedOnParent()) && (vt = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface())) != null) {
            for (VirtualZone vz : vt.getWatchers()) {
                vz.getWatcher().getCommunicator().sendRemoveItem(item);
            }
        }
        if (item.wornAsArmour) {
            item.setWornAsArmour(false, ownerId);
        }
        boolean send = true;
        if (item.isArmour()) {
            byte[] armourplaces;
            for (byte armourplace : armourplaces = item.getBodySpaces()) {
                if (armourplace != this.place) continue;
                send = false;
            }
        } else if (this.isFire() && (item.isFoodMaker() || item.isFood()) && (t = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface())) != null) {
            ((VolaTile)t).renameItem(this);
        }
        if ((this.isWeaponContainer() || this.isBarrelRack()) && this.isEmpty(false) && (t = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface())) != null) {
            ((VolaTile)t).renameItem(this);
        }
        if (this.getTemplate().getContainerRestrictions() != null && !this.getTemplate().isNoPut()) {
            for (ContainerRestriction cRest : this.getTemplate().getContainerRestrictions()) {
                if (!cRest.doesItemOverrideSlot(item)) continue;
                boolean skipAdd = false;
                for (Item i : this.getItems()) {
                    if (i.getTemplateId() == 1392 && cRest.contains(i.getRealTemplateId())) {
                        skipAdd = true;
                        continue;
                    }
                    if (!cRest.contains(i.getTemplateId())) continue;
                    skipAdd = true;
                }
                if (skipAdd) continue;
                try {
                    Item tempSlotItem = ItemFactory.createItem(1392, 100.0f, this.getCreatorName());
                    tempSlotItem.setRealTemplate(cRest.getEmptySlotTemplateId());
                    tempSlotItem.setName(cRest.getEmptySlotName());
                    this.insertItem(tempSlotItem, true);
                }
                catch (FailedException | NoSuchTemplateException tempSlotItem) {}
            }
        }
        if (send) {
            if (this.ownerId > 0L && (this.place == 37 || this.place == 38) && this.isBodyPartAttached()) {
                try {
                    owner = Server.getInstance().getCreature(ownerId);
                    owner.getCurrentTile().sendWieldItem(ownerId, this.place == 37 ? (byte)0 : 1, "", (byte)0, 0, 0, 0, 0, 0, 0);
                }
                catch (NoSuchCreatureException owner3) {
                }
                catch (NoSuchPlayerException nsp) {
                    Item.logWarn(nsp.getMessage(), nsp);
                }
            } else if (this.place == 3 && this.ownerId > 0L && item.isShield()) {
                try {
                    owner = Server.getInstance().getCreature(ownerId);
                    owner.getCommunicator().sendToggleShield(false);
                }
                catch (NoSuchCreatureException owner4) {
                }
                catch (NoSuchPlayerException nsp) {
                    Item.logWarn(nsp.getMessage(), nsp);
                }
            }
        }
        long topParent = this.getTopParent();
        if (this.isEmpty(false)) {
            if (this.watchers != null) {
                for (Creature watcher : this.watchers) {
                    boolean isOwner = ownerId == watcher.getWurmId();
                    long inventoryWindow = isOwner ? -1L : topParent;
                    Communicator watcherComm = watcher.getCommunicator();
                    if (!(item.getTopParentOrNull() == null || item.getTopParentOrNull().isEquipmentSlot() || item.getTopParentOrNull().isBodyPart() || item.getTopParentOrNull().isInventory())) {
                        watcherComm.sendRemoveFromInventory(item, inventoryWindow);
                    }
                    watcherComm.sendIsEmpty(inventoryWindow, this.getWurmId());
                }
            }
            if (this.getTemplateId() == 177 && !skipPileRemoval) {
                try {
                    Zone z = Zones.getZone((int)this.getPosX() >> 2, (int)this.getPosY() >> 2, this.isOnSurface());
                    z.removeItem(this);
                }
                catch (NoSuchZoneException noSuchZoneException) {
                    // empty catch block
                }
            }
            if ((this.getTemplateId() == 995 || this.getTemplateId() == 1422) && this.isEmpty(false)) {
                Items.destroyItem(this.getWurmId());
                item.parentId = -10L;
            }
        } else if (!(item.getTopParent() != topParent || this.isEmpty(false) || item.getTopParentOrNull() == null || item.getTopParentOrNull().isInventory() || item.getTopParentOrNull().isBodyPart() || item.getTopParentOrNull().isEquipmentSlot())) {
            if (this.watchers != null && !ignoreWatchers) {
                for (Creature watcher : this.watchers) {
                    boolean isOwner = ownerId == watcher.getWurmId();
                    long inventoryWindow = isOwner ? -1L : topParent;
                    watcher.getCommunicator().sendRemoveFromInventory(item, inventoryWindow);
                }
            }
        } else if (item.getTopParent() != topParent && ownerId != -10L && item.getOwnerId() == ownerId && this.watchers != null) {
            for (Creature watcher : this.watchers) {
                Item.logInfo(watcher.getName() + " checking if stopping to watch " + item.getName());
                if (item.watchers != null && item.watchers.contains(watcher)) continue;
                Item.logInfo("Removing watcher " + watcher + " in new method");
                item.removeWatcher(watcher, true);
            }
        }
        if (setPosition) {
            item.setPosXYZ(this.getPosX(), this.getPosY(), this.getPosZ());
        }
        return item;
    }

    @Override
    public final long getWurmId() {
        return this.id;
    }

    public final void removeAndEmpty() {
        ProtoConstants[] itarr;
        this.deleted = true;
        if (this.items != null && this.isHollow()) {
            VolaTile t = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Item[] contained = this.getAllItems(true);
            if (this.isBulkContainer()) {
                for (Item aContained : contained) {
                    Items.destroyItem(aContained.getWurmId());
                }
            } else {
                for (Item item : contained) {
                    if (item.getTemplateId() == 1392) {
                        Items.destroyItem(item.getWurmId());
                        continue;
                    }
                    item.setPosXYZ(this.getPosX(), this.getPosY(), item.calculatePosZ(t, null));
                }
            }
        }
        try {
            Item parent = this.getParent();
            boolean pile = parent.getTemplateId() == 177;
            int x = (int)this.getPosX() >> 2;
            int y = (int)this.getPosY() >> 2;
            parent.dropItem(this.id, false);
            if (pile) {
                parent.removeFromPile(this);
            }
            Set<Item> its = this.getItems();
            itarr = its.toArray(new Item[its.size()]);
            for (Item item : itarr) {
                this.dropItem(item.getWurmId(), false);
                if (item.isTransferred()) continue;
                if (pile) {
                    if (item.isLiquid()) {
                        Items.decay(item.getWurmId(), item.getDbStrings());
                        continue;
                    }
                    try {
                        Zone currentZone = Zones.getZone(x, y, this.isOnSurface());
                        currentZone.addItem(item);
                    }
                    catch (NoSuchZoneException nsz) {
                        Item.logWarn(this.getName() + " id:" + this.id + " at " + x + ", " + y, nsz);
                    }
                    continue;
                }
                if (item.isLiquid()) {
                    Items.decay(item.getWurmId(), item.getDbStrings());
                    continue;
                }
                Item topParent = parent.getTopParentOrNull();
                boolean dropToGround = false;
                if (item.isUseOnGroundOnly() && topParent != null && topParent.getTemplateId() != 0) {
                    dropToGround = true;
                }
                if (!dropToGround && parent.isBodyPartAttached()) {
                    dropToGround = true;
                }
                if (!(dropToGround || parent.isBodyPartAttached() || parent.insertItem(item, false, true))) {
                    dropToGround = true;
                }
                if (dropToGround) {
                    try {
                        Zone currentZone = Zones.getZone(x, y, this.isOnSurface());
                        currentZone.addItem(item);
                    }
                    catch (NoSuchZoneException nsz) {
                        Item.logWarn(this.getName() + " id:" + this.id + " at " + x + ", " + y, nsz);
                    }
                    continue;
                }
                try {
                    Creature owner = Server.getInstance().getCreature(this.ownerId);
                    owner.getInventory().insertItem(item);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        catch (NoSuchItemException nsi) {
            if (this.getParentId() != -10L) {
                return;
            }
            if ((long)this.zoneId == -10L) {
                return;
            }
            try {
                int x = (int)this.getPosX() >> 2;
                int y = (int)this.getPosY() >> 2;
                Zone currentZone = Zones.getZone(x, y, this.isOnSurface());
                currentZone.removeItem(this);
                if (!this.isHollow()) {
                    return;
                }
                try {
                    Creature[] iwatchers = this.getWatchers();
                    itarr = iwatchers;
                    int n = itarr.length;
                    for (int i = 0; i < n; ++i) {
                        ProtoConstants iwatcher = itarr[i];
                        ((Creature)iwatcher).removeItemWatched(this);
                        ((Creature)iwatcher).getCommunicator().sendCloseInventoryWindow(this.getWurmId());
                        this.removeWatcher((Creature)iwatcher, true);
                    }
                }
                catch (NoSuchCreatureException iwatchers) {
                    // empty catch block
                }
                Set<Item> its = this.getItems();
                for (ProtoConstants protoConstants : itarr = its.toArray(new Item[its.size()])) {
                    try {
                        this.dropItem(((Item)protoConstants).getWurmId(), false, true);
                        if (!((Item)protoConstants).isTransferred()) {
                            if (((Item)protoConstants).isLiquid()) {
                                Items.decay(((Item)protoConstants).getWurmId(), ((Item)protoConstants).getDbStrings());
                            } else {
                                currentZone.addItem((Item)protoConstants);
                            }
                        }
                    }
                    catch (NoSuchItemException nsi2) {
                        Item.logWarn(this.getName() + " id:" + this.id + " at " + x + ", " + y + " failed to drop item " + ((Item)protoConstants).getWurmId(), nsi2);
                    }
                    this.items.getClass();
                }
            }
            catch (NoSuchZoneException nsz) {
                Item.logWarn(this.getName() + " id:" + this.id, nsz);
            }
        }
    }

    public final boolean isTypeRecycled() {
        return this.template.isRecycled;
    }

    public final boolean hideAddToCreationWindow() {
        return this.template.hideAddToCreationWindow();
    }

    private void hatch() {
        if (this.isEgg() && this.getData1() > 0) {
            try {
                CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(this.getData1());
                byte sex = temp.getSex();
                if (sex == 0 && Server.rand.nextInt(2) == 0) {
                    sex = 1;
                }
                if ((temp.isUnique() || Server.rand.nextInt(10) == 0) && (temp.isUnique() || Creatures.getInstance().getNumberOfCreatures() < Servers.localServer.maxCreatures)) {
                    CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(this.getData1());
                    String cname = "";
                    String description = this.getDescription();
                    if (!description.isEmpty()) {
                        cname = LoginHandler.raiseFirstLetter(description.substring(0, Math.min(description.length(), 10)) + " the " + ct.getName());
                    }
                    Creature c = Creature.doNew(this.getData1(), false, this.getPosX(), this.getPosY(), Server.rand.nextInt(360), this.isOnSurface() ? 0 : -1, cname, sex, (byte)0, (byte)0, false, (byte)1);
                    if (temp.isUnique()) {
                        Item.logInfo("Player/creature with wurmid " + this.getLastOwnerId() + " hatched " + c.getName() + " at " + (int)this.posX / 4 + "," + (int)this.posY / 4);
                    }
                    if (Servers.isThisATestServer()) {
                        Players.getInstance().sendGmMessage(null, "System", "Debug: Player/creature with wurmid " + this.getLastOwnerId() + " hatched " + c.getName() + " at " + (int)this.posX / 4 + "," + (int)this.posY / 4, false);
                    }
                    if (this.getData1() == 48) {
                        switch (Server.rand.nextInt(3)) {
                            case 0: {
                                break;
                            }
                            case 1: {
                                c.getStatus().setTraitBit(15, true);
                                break;
                            }
                            case 2: {
                                c.getStatus().setTraitBit(16, true);
                            }
                        }
                    }
                }
            }
            catch (Exception ex) {
                Item.logWarn(ex.getMessage() + ' ' + this.getData1());
            }
            this.setData1(-1);
        } else if (this.getTemplateId() == 466 && this.ownerId == -10L) {
            try {
                int x = (int)this.getPosX() >> 2;
                int y = (int)this.getPosY() >> 2;
                Zone currentZone = Zones.getZone(x, y, this.isOnSurface());
                Item i = TileRockBehaviour.createRandomGem();
                if (i != null) {
                    i.setLastOwnerId(this.lastOwner);
                    i.setPosXY(this.getPosX(), this.getPosY());
                    i.setRotation(Server.rand.nextFloat() * 180.0f);
                    currentZone.addItem(i);
                }
            }
            catch (Exception ex) {
                Item.logWarn(ex.getMessage() + ' ' + this.getData1());
            }
        }
    }

    final boolean checkDecay() {
        if (this.isHugeAltar()) {
            return false;
        }
        if (this.qualityLevel > 0.0f && this.damage < 100.0f) {
            return false;
        }
        boolean decayed = true;
        if (this.ownerId != -10L) {
            Creature owner = null;
            try {
                owner = Server.getInstance().getCreature(this.getOwnerId());
                if (this.hasItemBonus() && owner.isPlayer()) {
                    ItemBonus.removeBonus(this, owner);
                }
                try {
                    Action act = owner.getCurrentAction();
                    if (act.getSubjectId() == this.id) {
                        act.stop(false);
                    }
                }
                catch (NoSuchActionException act) {
                    // empty catch block
                }
                Communicator ownerComm = owner.getCommunicator();
                if (this.isEgg()) {
                    Item i;
                    if (this.getTemplateId() == 466 || this.getData1() > 0) {
                        ownerComm.sendNormalServerMessage(LoginHandler.raiseFirstLetter(this.getNameWithGenus()) + " hatches!");
                    }
                    if (Servers.isThisATestServer() && this.getData1() > 0) {
                        Players.getInstance().sendGmMessage(null, "System", "Debug: decayed a fertile egg at " + (int)this.posX / 4 + "," + (int)this.posY / 4 + ", Data1=" + this.getData1(), false);
                    }
                    this.hatch();
                    if (this.getTemplateId() == 466 && (i = TileRockBehaviour.createRandomGem()) != null) {
                        owner.getInventory().insertItem(i, true);
                        ownerComm.sendNormalServerMessage(LoginHandler.raiseFirstLetter("You find something in the " + this.getName()) + "!");
                    }
                } else if (!this.isFishingBait()) {
                    ownerComm.sendNormalServerMessage(LoginHandler.raiseFirstLetter(this.getNameWithGenus()) + " is useless and you throw it away.");
                }
            }
            catch (NoSuchPlayerException | NoSuchCreatureException ownerComm) {}
        } else {
            this.sendDecayMess();
            if (this.isEgg()) {
                this.hatch();
            }
            if (this.hatching) {
                if (this.getTemplateId() == 805) {
                    IslandAdder adder = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
                    Map<Integer, Set<Integer>> changes = adder.forceIsland(50, 50, this.getTileX() - 25, this.getTileY() - 25);
                    if (changes != null) {
                        for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet()) {
                            Integer x = me.getKey();
                            Set<Integer> set = me.getValue();
                            for (Integer y : set) {
                                Players.getInstance().sendChangedTile(x, y, true, true);
                            }
                        }
                    }
                } else if (this.getTemplateId() == 1009) {
                    TerraformingTask task = new TerraformingTask(0, 0, this.creator, 2, 0, true);
                    task.setCoordinates();
                    task.setSXY(this.getTileX(), this.getTileY());
                }
            }
        }
        Items.destroyItem(this.id);
        return decayed;
    }

    private void sendDecayMess() {
        String msgSuff = "";
        int dist = 0;
        if (this.isEgg()) {
            if (this.getData1() > 0) {
                msgSuff = " cracks open!";
                dist = 10;
            } else if (this.getTemplateId() == 466) {
                msgSuff = " cracks open! Something is inside!";
                dist = 5;
            }
        } else if (!this.isTemporary()) {
            msgSuff = " crumbles to dust.";
            dist = 2;
        }
        if (msgSuff.isEmpty()) {
            return;
        }
        String fullMsgStr = LoginHandler.raiseFirstLetter(this.getNameWithGenus()) + msgSuff;
        if (this.watchers != null) {
            for (Creature watcher : this.watchers) {
                watcher.getCommunicator().sendNormalServerMessage(fullMsgStr);
            }
            return;
        }
        if (this.parentId != -10L && WurmId.getType(this.parentId) == 6 || this.isRepairable()) {
            try {
                TilePos tilePos = this.getTilePos();
                Zone currentZone = Zones.getZone(tilePos, this.isOnSurface());
                Server.getInstance().broadCastMessage(fullMsgStr, tilePos.x, tilePos.y, currentZone.isOnSurface(), dist);
            }
            catch (NoSuchZoneException nsz) {
                Item.logWarn(this.getName() + " id:" + this.id, nsz);
            }
        }
    }

    public final TilePos getTilePos() {
        return CoordUtils.WorldToTile(this.getPos2f());
    }

    public final int getTileX() {
        return CoordUtils.WorldToTile(this.getPosX());
    }

    public final int getTileY() {
        return CoordUtils.WorldToTile(this.getPosY());
    }

    public final boolean isCorpse() {
        return this.template.templateId == 272;
    }

    public final boolean isCrate() {
        return this.template.templateId == 852 || this.template.templateId == 851;
    }

    public final boolean isBarrelRack() {
        return this.template.templateId == 1108 || this.template.templateId == 1109 || this.template.templateId == 1111 || this.template.templateId == 1110;
    }

    public final boolean isCarpet() {
        return this.template.isCarpet();
    }

    public final void pollCoolingItems(Creature owner, long timeSinceLastCooled) {
        if (this.isHollow() && this.items != null) {
            Item[] itarr;
            for (Item anItarr : itarr = this.items.toArray(new Item[this.items.size()])) {
                if (anItarr.deleted) continue;
                anItarr.pollCoolingItems(owner, timeSinceLastCooled);
            }
        }
        this.coolInventoryItem(timeSinceLastCooled);
    }

    public final boolean pollOwned(Creature owner) {
        Item bestBeeSmoker;
        Item bestTackleBox;
        Item bestCompass;
        Item parent2;
        boolean decayed = false;
        short oldTemperature = this.getTemperature();
        long maintenanceTimeDelta = WurmCalendar.currentTime - this.lastMaintained;
        if (this.isFood() || this.isAlwaysPoll() && !this.isFlag() || this.isCorpse() || this.isPlantedFlowerpot() || this.getTemplateId() == 1276 || this.isInTacklebox()) {
            int timesSinceLastUsed;
            if (this.hatching) {
                return this.pollHatching();
            }
            long decayt = this.template.getDecayTime();
            if (this.template.templateId == 386) {
                try {
                    decayt = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDecayTime();
                }
                catch (NoSuchTemplateException nst) {
                    Item.logInfo("No template for " + this.getName() + ", id=" + this.realTemplate);
                }
            } else if (this.template.templateId == 339 && ArtifactBehaviour.getOrbActivation() > 0L && System.currentTimeMillis() - ArtifactBehaviour.getOrbActivation() > 21000L && WurmCalendar.currentTime - this.getData() < 360000L) {
                ArtifactBehaviour.resetOrbActivation();
                Server.getInstance().broadCastAction("A deadly field surges through the air from the location of " + owner.getName() + " and the " + this.getName() + "!", owner, 25);
                ArtifactBehaviour.markOrbRecipients(owner, false, 0.0f, 0.0f, 0.0f);
            }
            if (decayt == 28800L) {
                decayt = this.damage == 0.0f ? 1382400L + (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f)) : (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f));
            }
            float lunchboxMod = 0.0f;
            if (this.isInLunchbox()) {
                Item lunchbox = this.getParentOuterItemOrNull();
                if (lunchbox != null && lunchbox.getTemplateId() == 1296) {
                    lunchboxMod = 8.0f;
                } else if (lunchbox != null && lunchbox.getTemplateId() == 1297) {
                    lunchboxMod = 9.0f;
                }
                decayt *= (long)(this.getRarity() / 4 + 2);
            }
            if (this.isInTacklebox()) {
                lunchboxMod = 7.0f;
                decayt *= (long)(this.getRarity() / 4 + 2);
            }
            int adjDelta = (int)(maintenanceTimeDelta / decayt);
            int n = timesSinceLastUsed = this.isLight() ? Math.min(1, adjDelta) : adjDelta;
            if (timesSinceLastUsed > 0) {
                float decayMin = 0.5f;
                if (this.isFood() && owner.getDeity() != null && owner.getDeity().isItemProtector()) {
                    if (owner.getFaith() >= 70.0f && owner.getFavor() >= 35.0f || this.isCorpse()) {
                        if (Server.rand.nextInt(5) == 0) {
                            decayed = this.template.destroyOnDecay ? this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(1.0f, 10.0f - lunchboxMod)) : this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(0.5f, this.getDamageModifier(true) - lunchboxMod));
                        }
                    } else {
                        decayed = this.template.destroyOnDecay ? this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(1.0f, 10.0f - lunchboxMod)) : this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(0.5f, this.getDamageModifier(true) - lunchboxMod));
                    }
                } else {
                    decayed = this.template.destroyOnDecay ? this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(1.0f, 10.0f - lunchboxMod)) : this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(0.5f, this.getDamageModifier(true) - lunchboxMod));
                    if (this.isPlantedFlowerpot() && decayed) {
                        try {
                            int revertType = -1;
                            revertType = this.isPotteryFlowerPot() ? 813 : (this.isMarblePlanter() ? 1001 : -1);
                            if (revertType != -1) {
                                Item pot = ItemFactory.createItem(revertType, this.getQualityLevel(), this.creator);
                                pot.setLastOwnerId(this.getLastOwnerId());
                                pot.setDescription(this.getDescription());
                                pot.setDamage(this.getDamage());
                                owner.getInventory().insertItem(pot);
                            }
                        }
                        catch (FailedException | NoSuchTemplateException e) {
                            Item.logWarn(e.getMessage(), e);
                        }
                    }
                }
                if (!decayed && this.lastMaintained != WurmCalendar.currentTime) {
                    this.setLastMaintained(WurmCalendar.currentTime);
                }
            }
        } else if (Features.Feature.SADDLEBAG_DECAY.isEnabled() && owner != null && !owner.isPlayer() && !owner.isNpc() && this.isInside(1333, 1334)) {
            int timesSinceLastUsed;
            long decayt = this.template.getDecayTime();
            boolean decayQl = false;
            if (decayt == 28800L) {
                decayt = this.damage == 0.0f ? 1382400L + (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f)) : (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f));
                decayQl = true;
            }
            int adjDelta = (int)(maintenanceTimeDelta / decayt);
            int n = timesSinceLastUsed = this.isLight() ? Math.min(1, adjDelta) : adjDelta;
            if (timesSinceLastUsed > 0) {
                if (decayQl || Server.rand.nextInt(6) == 0) {
                    float decayMin = 0.2f;
                    decayed = this.template.destroyOnDecay ? this.setDamage(this.damage + (float)timesSinceLastUsed * 4.0f) : this.setDamage(this.damage + (float)timesSinceLastUsed * (0.2f * this.getDamageModifier(true)));
                }
                if (!decayed && this.lastMaintained != WurmCalendar.currentTime) {
                    this.setLastMaintained(WurmCalendar.currentTime);
                }
            }
        } else if (this.isHollow()) {
            if (this.items != null) {
                Item[] itarr;
                for (Item anItarr : itarr = this.items.toArray(new Item[this.items.size()])) {
                    if (anItarr.deleted) continue;
                    anItarr.pollOwned(owner);
                }
            }
        } else if (this.template.templateId == 166 && maintenanceTimeDelta > 2419200L) {
            this.setLastMaintained(WurmCalendar.currentTime);
        }
        try {
            parent2 = this.getParent();
            if (parent2.isBodyPartAttached()) {
                ItemBonus.checkDepleteAndRename(this, owner);
            }
        }
        catch (NoSuchItemException parent2) {
            // empty catch block
        }
        if (decayed) {
            return true;
        }
        if (this.isCompass() && ((bestCompass = owner.getBestCompass()) == null || bestCompass != this && bestCompass.getCurrentQualityLevel() < this.getCurrentQualityLevel())) {
            owner.setBestCompass(this);
        }
        if (this.getTemplateId() == 1341 && ((bestTackleBox = owner.getBestTackleBox()) == null || bestTackleBox != this && bestTackleBox.getCurrentQualityLevel() < this.getCurrentQualityLevel())) {
            owner.setBestTackleBox(this);
        }
        if (this.isToolbelt()) {
            try {
                Item bestBelt;
                parent2 = this.getParent();
                if (parent2.getPlace() == 43 && parent2.isBodyPartAttached() && ((bestBelt = owner.getBestToolbelt()) == null || bestBelt != this && bestBelt.getCurrentQualityLevel() < this.getCurrentQualityLevel())) {
                    owner.setBestToolbelt(this);
                }
            }
            catch (NoSuchItemException parent3) {
                // empty catch block
            }
        }
        if (this.getTemplateId() == 1243 && this.getTemperature() >= 10000 && ((bestBeeSmoker = owner.getBestBeeSmoker()) == null || bestBeeSmoker != this && bestBeeSmoker.getCurrentQualityLevel() < this.getCurrentQualityLevel())) {
            owner.setBestBeeSmoker(this);
        }
        this.coolInventoryItem();
        if (this.isLight() && this.isOnFire()) {
            if (owner.getBestLightsource() != null) {
                if (!owner.getBestLightsource().isLightBright() && this.isLightBright()) {
                    owner.setBestLightsource(this, false);
                } else if (owner.getBestLightsource() != this && owner.getBestLightsource().getCurrentQualityLevel() < this.getCurrentQualityLevel()) {
                    owner.setBestLightsource(this, false);
                }
            } else {
                owner.setBestLightsource(this, false);
            }
            decayed = this.pollLightSource();
        } else if (this.getTemplateId() == 1243) {
            decayed = this.pollLightSource();
        }
        if (this.getTemperatureState(oldTemperature) != this.getTemperatureState(this.temperature)) {
            this.notifyWatchersTempChange();
        }
        return decayed;
    }

    public final void attackEnemies(boolean watchTowerpoll) {
        if (!Servers.localServer.PVPSERVER) {
            return;
        }
        int tileX = this.getTileX();
        int tileY = this.getTileY();
        if (watchTowerpoll) {
            int dist = 10;
            int x1 = Zones.safeTileX(tileX - 10);
            int x2 = Zones.safeTileX(tileX + 10);
            int y1 = Zones.safeTileY(tileY - 10);
            int y2 = Zones.safeTileY(tileY + 10);
            for (TilePos tPos : TilePos.areaIterator(x1, y1, x2, y2)) {
                int x = tPos.x;
                int y = tPos.y;
                VolaTile t = Zones.getTileOrNull(x, y, true);
                if (t == null || this.getKingdom() != t.getKingdom()) continue;
                for (Creature c : t.getCreatures()) {
                    VolaTile[] tiles;
                    if (c.getPower() > 0 || c.isUnique() || c.isInvulnerable() || c.getKingdomId() == 0 || !c.getTemplate().isTowerBasher() || c.isFriendlyKingdom(this.getKingdom()) || Server.rand.nextFloat() * 200.0f >= this.getCurrentQualityLevel()) continue;
                    for (VolaTile tile : tiles = Zones.getTilesSurrounding(x, y, c.isOnSurface(), 5)) {
                        tile.broadCast("The " + this.getName() + " fires at " + c.getNameWithGenus() + ".");
                    }
                    float mod = 1.0f / c.getArmourMod();
                    Arrows.shootCreature(this, c, (int)(mod * 10000.0f));
                }
            }
        } else {
            boolean isArcheryTower;
            boolean bl = isArcheryTower = this.getTemplateId() == 939;
            if (!this.isEnchantedTurret() && !isArcheryTower) {
                return;
            }
            if (this.getOwnerId() != -10L || this.getParentId() != -10L) {
                return;
            }
            if (this.isEnchantedTurret() && !this.isPlanted()) {
                return;
            }
            VolaTile ts = Zones.getTileOrNull(tileX, tileY, true);
            if (ts == null) {
                return;
            }
            if ((double)(WurmCalendar.getCurrentTime() - this.lastMaintained) < 320.0 * (1.0 - (double)(this.getCurrentQualityLevel() / 200.0f))) {
                return;
            }
            this.lastMaintained = WurmCalendar.getCurrentTime();
            HashSet<Creature> targets = new HashSet<Creature>();
            float distanceModifier = this.getCurrentQualityLevel() / 100.0f * 5.0f;
            int dist = (int)((float)(isArcheryTower ? 5 : 3) * distanceModifier);
            int x1 = Zones.safeTileX(tileX - dist);
            int x2 = Zones.safeTileX(tileX + dist);
            int y1 = Zones.safeTileY(tileY - dist);
            int y2 = Zones.safeTileY(tileY + dist);
            for (TilePos tPos : TilePos.areaIterator(x1, y1, x2, y2)) {
                int x = tPos.x;
                int y = tPos.y;
                VolaTile t = Zones.getTileOrNull(x, y, true);
                if (t == null || this.getKingdom() != t.getKingdom() || Zones.getCurrentTurret(x, y, true) != this) continue;
                for (Creature c : t.getCreatures()) {
                    if (c.isUnique() || c.isInvulnerable() || c.getKingdomId() == 0 || !c.isPlayer() && !c.getTemplate().isTowerBasher()) continue;
                    Village v = Villages.getVillageWithPerimeterAt(tileX, tileY, true);
                    if (c.isFriendlyKingdom(this.getKingdom()) && (!Servers.localServer.PVPSERVER || v == null || !v.isEnemy(c)) || c.getCurrentTile() == null) continue;
                    targets.add(c);
                }
            }
            if (!targets.isEmpty()) {
                VolaTile t;
                Creature[] crets = targets.toArray(new Creature[targets.size()]);
                Creature c = crets[Server.rand.nextInt(crets.length)];
                if (Server.rand.nextFloat() * 200.0f < this.getCurrentQualityLevel()) {
                    BlockingResult result = Blocking.getBlockerBetween(null, this.getPosX(), this.getPosY(), c.getPosX(), c.getPosY(), this.getPosZ() + (float)this.getTemplate().getSizeY() * 0.85f / 100.0f - 0.5f, c.getPositionZ() + (float)c.getCentimetersHigh() * 0.75f / 100.0f - 0.5f, this.isOnSurface(), c.isOnSurface(), true, 4, c.getWurmId(), this.getBridgeId(), c.getBridgeId(), false);
                    if (result != null) {
                        for (Blocker b : result.getBlockerArray()) {
                            if (!(b.getBlockPercent(c) >= 100.0f)) continue;
                            return;
                        }
                        if (result.getTotalCover() > 0.0f) {
                            return;
                        }
                    }
                    float mod = 1.0f + c.getArmourMod();
                    float distToCret = 1.0f - c.getPos3f().distance(this.getPos3f()) / 150.0f;
                    float enchDamMod = this.getCurrentQualityLevel() * distToCret;
                    if (this.isEnchantedTurret() && (enchDamMod = this.getSpellCourierBonus()) == 0.0f) {
                        Item.logInfo("Reverted turret at " + tileX + "," + this.getTileY());
                        this.setTemplateId(934);
                    }
                    Arrows.shootCreature(this, c, (int)(mod * 75.0f * enchDamMod));
                } else if (this.isEnchantedTurret() && (t = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface())) != null) {
                    t.sendAnimation(c, this, "shoot", false, false);
                }
            }
        }
    }

    private boolean poll(Item parent, int parentTemp, boolean insideStructure, boolean deeded, boolean saveLastMaintained, boolean inMagicContainer, boolean inTrashbin) {
        boolean decayed;
        block92: {
            block90: {
                block89: {
                    int timesSinceLastUsed;
                    boolean decaytimeql;
                    block93: {
                        block91: {
                            block88: {
                                int timesSinceLastUsed2;
                                if (this.hatching) {
                                    return this.pollHatching();
                                }
                                decayed = false;
                                if (Features.Feature.TRANSPORTABLE_CREATURES.isEnabled()) {
                                    long delay = System.currentTimeMillis() - 900000L;
                                    if (this.getTemplateId() == 1310 && parent.getTemplateId() == 1311 && delay > parent.getData()) {
                                        this.pollCreatureCages(parent);
                                    }
                                }
                                if (Features.Feature.FREE_ITEMS.isEnabled() && this.isChallengeNewbieItem() && (this.isArmour() || this.isWeapon() || this.isShield()) && this.ownerId == -10L) {
                                    Items.destroyItem(this.getWurmId());
                                    return true;
                                }
                                if (this.isHollow() && this.isSealedByPlayer()) {
                                    if (this.getTemplateId() == 768) {
                                        this.pollAging(insideStructure, deeded);
                                        if (Server.rand.nextInt(20) == 0) {
                                            this.pollFermenting();
                                        }
                                    }
                                    return false;
                                }
                                if (this.getTemplateId() == 70 || this.getTemplateId() == 1254) {
                                    this.modTemp(parent, parentTemp, insideStructure);
                                    return false;
                                }
                                if (this.template.getDecayTime() == Long.MAX_VALUE) break block90;
                                decaytimeql = false;
                                if (!this.isFood() && !this.isHollow() && (!this.isAlwaysPoll() || this.isFlag())) break block91;
                                if (this.template.templateId == 339 && ArtifactBehaviour.getOrbActivation() > 0L && System.currentTimeMillis() - ArtifactBehaviour.getOrbActivation() > 21000L && WurmCalendar.currentTime - this.getData() < 360000L) {
                                    ArtifactBehaviour.resetOrbActivation();
                                    Server.getInstance().broadCastMessage("A deadly field surges through the air from the location of the " + this.getName() + "!", this.getTileX(), this.getTileY(), this.isOnSurface(), 25);
                                    ArtifactBehaviour.markOrbRecipients(null, false, this.getPosX(), this.getPosY(), this.getPosZ());
                                }
                                if (this.template.getTemplateId() == 1175 && parent.isVehicle() && this.hasQueen() && WurmCalendar.currentTime - this.lastMaintained > 604800L) {
                                    if (this.hasTwoQueens()) {
                                        if (this.removeQueen()) {
                                            if (Servers.isThisATestServer()) {
                                                Players.getInstance().sendGmMessage(null, "System", "Debug: Removed second queen from " + this.getWurmId() + " as travelling.", false);
                                            } else {
                                                logger.info("Removed second queen from " + this.getWurmId() + " as travelling.");
                                            }
                                        }
                                    } else {
                                        Item[] sugar = this.getSugar();
                                        if (sugar != null) {
                                            Items.destroyItem(sugar.getWurmId());
                                        } else {
                                            Item honey = this.getHoney();
                                            if (honey != null) {
                                                honey.setWeight(Math.max(0, honey.getWeightGrams() - 10), true);
                                            } else if (Server.rand.nextInt(3) == 0 && this.removeQueen()) {
                                                if (Servers.isThisATestServer()) {
                                                    Players.getInstance().sendGmMessage(null, "System", "Debug: Removed queen from " + this.getWurmId() + " as travelling and No Honey!", false);
                                                } else {
                                                    logger.info("Removed queen from " + this.getWurmId() + " as travelling and No Honey!");
                                                }
                                            }
                                        }
                                    }
                                }
                                if (this.isHollow()) {
                                    if (deeded && this.isCrate() && parent.getTemplateId() == 1312) {
                                        this.setLastMaintained(WurmCalendar.currentTime);
                                        for (Item i : this.getAllItems(true)) {
                                            if (!i.isBulkItem()) continue;
                                            i.setLastMaintained(WurmCalendar.currentTime);
                                        }
                                        return false;
                                    }
                                    if (deeded && this.getTemplateId() == 662 && parent.getTemplateId() == 1315) {
                                        this.setLastMaintained(WurmCalendar.currentTime);
                                        return false;
                                    }
                                    if (this.items != null) {
                                        Item[] itarr;
                                        for (Item item : itarr = this.items.toArray(new Item[this.items.size()])) {
                                            if (item.deleted) continue;
                                            item.poll(this, this.getTemperature(), insideStructure, deeded, saveLastMaintained, inMagicContainer || this.isMagicContainer(), false);
                                        }
                                    }
                                }
                                long decayTime = 1382400L;
                                if (WurmCalendar.currentTime <= this.creationDate + 1382400L && !inTrashbin && this.template.getDecayTime() >= 3600L) break block92;
                                long decayt = this.template.getDecayTime();
                                if (this.template.templateId == 386) {
                                    try {
                                        decayt = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDecayTime();
                                    }
                                    catch (NoSuchTemplateException nst) {
                                        Item.logInfo("No template for " + this.getName() + ", id=" + this.realTemplate);
                                    }
                                }
                                if (decayt == 28800L) {
                                    decayt = this.damage == 0.0f ? 1382400L + (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f)) : (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f));
                                    decaytimeql = true;
                                }
                                if (!(!inTrashbin || this.isHollow() && this.isLocked())) {
                                    decayt = Math.min(decayt, 28800L);
                                }
                                if ((timesSinceLastUsed2 = (int)((WurmCalendar.currentTime - this.lastMaintained) / decayt)) <= 0) break block92;
                                if (!(!inTrashbin || this.isHollow() && this.isLocked())) {
                                    if (this.getDamage() > 0.0f) {
                                        Items.destroyItem(this.getWurmId());
                                        return true;
                                    }
                                    return this.setDamage(this.getDamage() + 0.1f);
                                }
                                int num = 2;
                                float decayMin = 0.5f;
                                if (this.isFood()) {
                                    decayMin = 1.0f;
                                }
                                if (!this.isBulk() && this.template.templateId != 74 && !this.isLight()) {
                                    if (insideStructure) {
                                        num = 10;
                                    }
                                    if (deeded) {
                                        num += 4;
                                    }
                                }
                                boolean decay = true;
                                float dm = this.getDecayMultiplier();
                                if (dm > 1.0f) {
                                    this.ticksSinceLastDecay += timesSinceLastUsed2;
                                    timesSinceLastUsed2 = (int)((float)this.ticksSinceLastDecay / dm);
                                    if (timesSinceLastUsed2 > 0) {
                                        this.ticksSinceLastDecay -= (int)((float)timesSinceLastUsed2 * dm);
                                    } else {
                                        decay = false;
                                        this.setLastMaintained(WurmCalendar.currentTime);
                                    }
                                }
                                if (decay && (decaytimeql || this.isBulkItem() || Server.rand.nextInt(num) == 0)) {
                                    if (this.template.positiveDecay) {
                                        if (this.getTemplateId() == 738) {
                                            this.setQualityLevel(Math.min(100.0f, this.qualityLevel + (100.0f - this.qualityLevel) * (100.0f - this.qualityLevel) / 10000.0f));
                                            this.checkGnome();
                                        }
                                    } else if (this.isMagicContainer() || !inMagicContainer || (this.isLight() || this.isFireplace()) && this.isOnFire()) {
                                        if ((this.isLight() || this.isFireplace()) && this.isOnFire()) {
                                            this.pollLightSource();
                                        }
                                        if (this.template.destroyOnDecay) {
                                            decayed = this.setDamage(this.damage + (float)(timesSinceLastUsed2 * 10));
                                        } else if (this.isBulkItem() && this.getBulkNums() > 0) {
                                            try {
                                                ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(this.getRealTemplateId());
                                                if (this.getWeightGrams() < t.getVolume()) {
                                                    Items.destroyItem(this.getWurmId());
                                                    decayed = true;
                                                    break block88;
                                                }
                                                float mod = 0.05f;
                                                decayed = this.setWeight((int)((float)this.getWeightGrams() - (float)(this.getWeightGrams() * timesSinceLastUsed2) * 0.05f), true);
                                            }
                                            catch (NoSuchTemplateException nst) {
                                                Items.destroyItem(this.getWurmId());
                                                decayed = true;
                                            }
                                        } else {
                                            decayed = this.setDamage(this.damage + (float)timesSinceLastUsed2 * Math.max(decayMin, this.getDamageModifier(true)));
                                        }
                                    }
                                }
                            }
                            if (decayed || this.lastMaintained == WurmCalendar.currentTime) break block92;
                            this.setLastMaintained(WurmCalendar.currentTime);
                            break block92;
                        }
                        if (this.getTemplateId() != 1162) break block93;
                        if (WurmCalendar.currentTime - this.lastMaintained > 604800L) {
                            this.advancePlanterWeek();
                        }
                        break block92;
                    }
                    if (WurmCalendar.currentTime - this.creationDate <= 1382400L && !inTrashbin && this.template.getDecayTime() >= 3600L) break block92;
                    long decayt = this.template.getDecayTime();
                    if (this.template.templateId == 386) {
                        try {
                            decayt = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDecayTime();
                        }
                        catch (NoSuchTemplateException nst) {
                            Item.logInfo("No template for " + this.getName() + ", id=" + this.realTemplate);
                        }
                    }
                    if (decayt == 28800L) {
                        decayt = this.damage == 0.0f ? 1382400L + (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f)) : (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f));
                        decaytimeql = true;
                    }
                    if (!(!inTrashbin || this.isHollow() && this.isLocked())) {
                        decayt = Math.min(decayt, 28800L);
                    }
                    if ((timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / decayt)) <= 0) break block92;
                    if (!(!inTrashbin || this.isHollow() && this.isLocked())) {
                        if (this.getDamage() > 0.0f) {
                            Items.destroyItem(this.getWurmId());
                            return true;
                        }
                        return this.setDamage(this.getDamage() + 0.1f);
                    }
                    int num = 2;
                    if (!this.isBulk() && this.template.templateId != 74) {
                        if (insideStructure && !this.template.positiveDecay) {
                            num = 10;
                        }
                        if (deeded) {
                            num += 4;
                        }
                    }
                    boolean decay = true;
                    if (this.getDecayMultiplier() > 1.0f) {
                        this.ticksSinceLastDecay += timesSinceLastUsed;
                        timesSinceLastUsed = (int)((float)this.ticksSinceLastDecay / this.getDecayMultiplier());
                        if (timesSinceLastUsed > 0) {
                            this.ticksSinceLastDecay -= (int)((float)timesSinceLastUsed * this.getDecayMultiplier());
                        } else {
                            decay = false;
                            this.setLastMaintained(WurmCalendar.currentTime);
                        }
                    }
                    if (decay && (decaytimeql || this.isBulkItem() || Server.rand.nextInt(num) == 0)) {
                        if (this.template.positiveDecay && !inTrashbin) {
                            if (this.getTemplateId() == 738) {
                                this.setQualityLevel(Math.min(100.0f, this.qualityLevel + (100.0f - this.qualityLevel) * (100.0f - this.qualityLevel) / 10000.0f));
                                this.checkGnome();
                            }
                        } else if (this.isMagicContainer() || !inMagicContainer) {
                            if (this.template.destroyOnDecay) {
                                decayed = this.setDamage(this.damage + (float)(timesSinceLastUsed * 10));
                            } else if (this.isBulkItem() && this.getBulkNums() > 0) {
                                try {
                                    float mod;
                                    ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(this.getRealTemplateId());
                                    if (this.getWeightGrams() < t.getVolume()) {
                                        Items.destroyItem(this.getWurmId());
                                        decayed = true;
                                        break block89;
                                    }
                                    VolaTile tile = Zones.getOrCreateTile(this.getTileX(), this.getTileY(), true);
                                    if (tile.getVillage() != null) {
                                        mod = 0.0f;
                                        decay = false;
                                        this.setLastMaintained(WurmCalendar.currentTime);
                                    } else {
                                        mod = 0.05f;
                                    }
                                    decayed = this.setWeight((int)((float)this.getWeightGrams() - (float)(this.getWeightGrams() * timesSinceLastUsed) * mod), true);
                                }
                                catch (NoSuchTemplateException nst) {
                                    Items.destroyItem(this.getWurmId());
                                    decayed = true;
                                }
                            } else {
                                decayed = this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(1.0f, this.getDamageModifier(true)));
                            }
                        }
                    }
                }
                if (decayed || this.lastMaintained == WurmCalendar.currentTime) break block92;
                this.setLastMaintained(WurmCalendar.currentTime);
                break block92;
            }
            if (saveLastMaintained && this.lastMaintained - WurmCalendar.currentTime > 1209600L && !this.isRiftLoot()) {
                this.setLastMaintained(WurmCalendar.currentTime);
            }
            if (this.isHollow() && this.items != null) {
                Item[] itarr;
                for (Item item : itarr = this.items.toArray(new Item[this.items.size()])) {
                    if (item.deleted) continue;
                    item.poll(this, this.getTemperature(), insideStructure, deeded, saveLastMaintained, inMagicContainer || this.isMagicContainer(), false);
                }
            }
        }
        if (Features.Feature.CHICKEN_COOPS.isEnabled()) {
            ChickenCoops.poll(this);
        }
        if (!decayed) {
            this.modTemp(parent, parentTemp, insideStructure);
        }
        return decayed;
    }

    private void pollCreatureCages(Item parent) {
        parent.setDamage(parent.damage + 10.0f / parent.getCurrentQualityLevel());
        parent.setData(System.currentTimeMillis());
        if (parent.getDamage() >= 80.0f) {
            try {
                int layer = this.isOnSurface() ? 0 : -1;
                parent.setName("creature cage [Empty]");
                Creature getCreature = Creatures.getInstance().getCreature(this.getData());
                Creatures cstat = Creatures.getInstance();
                getCreature.getStatus().setDead(false);
                cstat.removeCreature(getCreature);
                cstat.addCreature(getCreature, false);
                getCreature.putInWorld();
                CreatureBehaviour.blinkTo(getCreature, this.getPosX(), this.getPosY(), layer, this.getPosZ(), this.getBridgeId(), this.getFloorLevel());
                parent.setAuxData((byte)0);
                Items.destroyItem(this.getWurmId());
                CargoTransportationMethods.updateItemModel(parent);
                DbCreatureStatus.setLoaded(0, getCreature.getWurmId());
            }
            catch (NoSuchCreatureException | IOException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    public final void ageTrellis() {
        if (System.currentTimeMillis() - this.lastPolled < 86400000L) {
            return;
        }
        this.lastPolled = System.currentTimeMillis();
        int age = this.getLeftAuxData();
        if (age != 15) {
            int chance = Server.rand.nextInt(225);
            if (!(chance > (16 - age) * (16 - age) && this.isPlanted() || Server.rand.nextInt(5) != 0)) {
                ++age;
                if (chance > 8) {
                    if (WurmCalendar.isNight()) {
                        SoundPlayer.playSound("sound.birdsong.owl.short", this.getTileX(), this.getTileY(), true, 4.0f);
                    } else {
                        SoundPlayer.playSound("sound.ambient.day.crickets", this.getTileX(), this.getTileY(), true, 0.0f);
                    }
                }
                this.setLeftAuxData(age);
                this.updateName();
            }
        } else {
            int chance = Server.rand.nextInt(15);
            if (chance == 1) {
                this.setLeftAuxData(0);
                this.updateName();
            }
        }
    }

    public final boolean poll(boolean insideStructure, boolean deeded, long seed) {
        boolean decayed = false;
        int templateId = -1;
        templateId = this.getTemplateId();
        if (Features.Feature.FREE_ITEMS.isEnabled() && this.isChallengeNewbieItem() && (this.isArmour() || this.isWeapon() || this.isShield()) && this.ownerId == -10L) {
            Items.destroyItem(this.getWurmId());
            return true;
        }
        if (templateId == 339 && ArtifactBehaviour.getOrbActivation() > 0L && System.currentTimeMillis() - ArtifactBehaviour.getOrbActivation() > 21000L && WurmCalendar.currentTime - this.getData() < 360000L) {
            ArtifactBehaviour.resetOrbActivation();
            Server.getInstance().broadCastMessage("A deadly field surges through the air from the location of the " + this.getName() + "!", this.getTileX(), this.getTileY(), this.isOnSurface(), 25);
            ArtifactBehaviour.markOrbRecipients(null, false, this.getPosX(), this.getPosY(), this.getPosZ());
        }
        if (this.hatching) {
            return this.pollHatching();
        }
        if (this.getTemplateId() == 1437 && WurmCalendar.getCurrentTime() - this.lastMaintained > 604800L) {
            this.addSnowmanItem();
            this.setLastMaintained(WurmCalendar.getCurrentTime());
        }
        if (this.isHollow() && this.isSealedByPlayer()) {
            if (templateId == 768) {
                this.pollAging(insideStructure, deeded);
                if (Server.rand.nextInt(20) == 0) {
                    this.pollFermenting();
                }
            }
            return false;
        }
        if (this.template.getDecayTime() != Long.MAX_VALUE) {
            if (this.isHollow() || this.isFood() || this.isAlwaysPoll()) {
                long decayt = this.template.getDecayTime();
                if (templateId == 386) {
                    try {
                        decayt = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDecayTime();
                    }
                    catch (NoSuchTemplateException nst) {
                        Item.logInfo("No template for " + this.getName() + ", id=" + this.realTemplate);
                    }
                }
                if (decayt == 28800L) {
                    decayt = this.damage == 0.0f ? 1382400L + (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f)) : (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f));
                }
                if (deeded && this.isDecoration() && templateId != 74) {
                    decayt *= 3L;
                }
                int timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / decayt);
                if (this.isHollow() && !this.isSealedByPlayer()) {
                    boolean lastm;
                    boolean bl = lastm = seed == 1L;
                    if (this.items != null) {
                        Item[] pollItems1 = this.items.toArray(new Item[this.items.size()]);
                        int destroyed = 0;
                        boolean trashBin = templateId == 670;
                        long lasto = 0L;
                        for (Item pollItem : pollItems1) {
                            if (pollItem.deleted) continue;
                            if (!trashBin) {
                                pollItem.poll(this, this.getTemperature(), insideStructure, deeded, lastm, this.isMagicContainer(), false);
                                continue;
                            }
                            if (lasto > 0L && lasto != pollItem.getLastOwnerId() && destroyed > 0) {
                                try {
                                    Creature lLastOwner = Server.getInstance().getCreature(lasto);
                                    lLastOwner.achievement(160, destroyed);
                                    destroyed = 0;
                                }
                                catch (NoSuchPlayerException | NoSuchCreatureException nsc) {
                                    Achievements.triggerAchievement(lasto, 160, destroyed);
                                    destroyed = 0;
                                }
                            }
                            lasto = pollItem.getLastOwnerId();
                            if (pollItem.isHollow()) {
                                for (Item it : pollItem.getItemsAsArray()) {
                                    if (!it.poll(pollItem, this.getTemperature(), insideStructure, deeded, lastm, this.isMagicContainer(), true)) continue;
                                    ++destroyed;
                                }
                            }
                            if (pollItem.poll(this, this.getTemperature(), insideStructure, deeded, lastm, this.isMagicContainer(), true)) {
                                ++destroyed;
                            }
                            if (destroyed >= 100) break;
                        }
                        if (destroyed > 0 && lasto > 0L) {
                            try {
                                Creature lastoner = Server.getInstance().getCreature(lasto);
                                lastoner.achievement(160, destroyed);
                                destroyed = 0;
                            }
                            catch (NoSuchPlayerException | NoSuchCreatureException nsc) {
                                Achievements.triggerAchievement(lasto, 160, destroyed);
                                destroyed = 0;
                            }
                        }
                    } else if (this.isCorpse()) {
                        if (this.getData1() == 67 || this.getData1() == 36 || this.getData1() == 35 || this.getData1() == 34) {
                            decayed = this.setDamage(100.0f);
                        } else if (Servers.localServer.isChallengeServer() && this.getData1() != 1 && WurmCalendar.currentTime - this.creationDate > 28800L) {
                            decayed = this.setDamage(100.0f);
                        }
                    }
                    this.checkDrift();
                }
                this.attackEnemies(false);
                if (this.isSpringFilled()) {
                    if (this.isSourceSpring()) {
                        int volAvail;
                        if (Server.rand.nextInt(100) == 0 && (volAvail = this.getFreeVolume()) > 0) {
                            Item liquid = null;
                            for (Item next : this.getItems()) {
                                if (!next.isLiquid()) continue;
                                liquid = next;
                            }
                            if (liquid != null) {
                                if (liquid.getTemplateId() == 763) {
                                    liquid.setWeight(liquid.getWeightGrams() + 10, true);
                                }
                            } else {
                                try {
                                    Random r = new Random(this.getWurmId());
                                    Item source = ItemFactory.createItem(763, 80.0f + r.nextFloat() * 20.0f, "");
                                    this.insertItem(source, true);
                                }
                                catch (NoSuchTemplateException nst) {
                                    Item.logInfo(nst.getMessage(), nst);
                                }
                                catch (FailedException fe) {
                                    Item.logInfo(fe.getMessage(), fe);
                                }
                            }
                        }
                    } else if (Zone.hasSpring(this.getTileX(), this.getTileY()) || this.isAutoFilled()) {
                        MethodsItems.fillContainer(this, null, false);
                    }
                }
                if (timesSinceLastUsed > 0 && !decayed && !this.hasNoDecay()) {
                    if (templateId == 74 && this.isOnFire()) {
                        for (int i = 0; i < timesSinceLastUsed; ++i) {
                            this.createDaleItems();
                            decayed = this.setDamage(this.damage + 1.0f * this.getDamageModifier(true));
                            if (decayed) break;
                        }
                        if (!decayed && this.lastMaintained != WurmCalendar.currentTime) {
                            this.setLastMaintained(WurmCalendar.currentTime);
                        }
                        return decayed;
                    }
                    if ((templateId != 37 || this.getTemperature() <= 200) && (WurmCalendar.currentTime > this.creationDate + 1382400L || this.isAlwaysPoll() || this.template.getDecayTime() < 3600L || Servers.localServer.isChallengeOrEpicServer() && this.template.destroyOnDecay)) {
                        float dm;
                        float decayMin = 0.5f;
                        boolean decay = true;
                        if (deeded && this.getTemplateId() == 1311 && this.isEmpty(true)) {
                            decay = false;
                        }
                        if (!Servers.isThisAPvpServer() && deeded && this.isEnchantedTurret()) {
                            decay = false;
                        }
                        if ((this.isSign() || this.isStreetLamp() || this.isFlag() || this.isDecoration()) && (this.isPlanted() || this.isDecoration() && !this.template.decayOnDeed()) && deeded && (!this.isAlwaysPoll() || this.isFlag() || this.template.isCooker())) {
                            decay = false;
                        }
                        if ((dm = this.getDecayMultiplier()) > 1.0f) {
                            this.ticksSinceLastDecay += timesSinceLastUsed;
                            timesSinceLastUsed = (int)((float)this.ticksSinceLastDecay / dm);
                            if (timesSinceLastUsed > 0) {
                                this.ticksSinceLastDecay -= (int)((float)timesSinceLastUsed * dm);
                            } else {
                                decay = false;
                                this.setLastMaintained(WurmCalendar.currentTime);
                            }
                        }
                        if (decay) {
                            if (insideStructure) {
                                if (this.isFood()) {
                                    decayMin = 1.0f;
                                }
                                if (this.template.destroyOnDecay) {
                                    decayed = this.setDamage(this.damage + (float)(timesSinceLastUsed * 10));
                                } else if (Server.rand.nextInt(deeded ? 12 : 8) == 0) {
                                    decayed = this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(decayMin, this.getDamageModifier(true)));
                                }
                            } else {
                                if (this.isFood()) {
                                    decayMin = 2.0f;
                                }
                                decayed = this.template.destroyOnDecay ? (Servers.localServer.isChallengeServer() ? this.setDamage(100.0f) : this.setDamage(this.damage + (float)(timesSinceLastUsed * 10))) : this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(decayMin, this.getDamageModifier(true)));
                            }
                        } else {
                            long l = this.lastMaintained = WurmCalendar.currentTime + (long)Server.rand.nextInt(10) == 0L ? 1L : 0L;
                        }
                        if (!decayed && this.lastMaintained != WurmCalendar.currentTime && !this.isRiftLoot()) {
                            this.setLastMaintained(WurmCalendar.currentTime);
                        }
                    }
                }
            } else if (!this.hasNoDecay()) {
                if (this.getTemplateId() == 1162) {
                    if (WurmCalendar.currentTime - this.lastMaintained > 604800L) {
                        this.advancePlanterWeek();
                    }
                } else if (WurmCalendar.currentTime > this.creationDate + 1382400L || this.template.getDecayTime() < 3600L) {
                    int timesSinceLastUsed;
                    templateId = this.getTemplateId();
                    long decayt = this.template.getDecayTime();
                    if (templateId == 386) {
                        try {
                            decayt = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDecayTime();
                        }
                        catch (NoSuchTemplateException nst) {
                            Item.logInfo("No template for " + this.getName() + ", id=" + this.realTemplate);
                        }
                    }
                    float decayMin = 0.5f;
                    if (decayt == 28800L) {
                        decayt = this.damage == 0.0f ? 1382400L + (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f)) : (long)(28800.0f * Math.max(1.0f, this.qualityLevel / 3.0f));
                        decayMin = 1.0f;
                    }
                    if (!this.isBulk()) {
                        if (deeded) {
                            decayt *= 2L;
                        }
                        if (insideStructure) {
                            decayt *= 2L;
                        }
                        if (this.isRoadMarker() && !deeded && MethodsHighways.numberOfSetBits(this.getAuxData()) < 2) {
                            decayt = Math.max(1L, decayt / 10L);
                        }
                    }
                    if ((timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / decayt)) > 0) {
                        float dm;
                        boolean decay = true;
                        if (this.isRoadMarker()) {
                            if (this.isPlanted() && (deeded || MethodsHighways.numberOfSetBits(this.getAuxData()) >= 2)) {
                                decay = false;
                                this.setLastMaintained(WurmCalendar.currentTime);
                            }
                        } else if ((this.isSign() || this.isStreetLamp() || this.isFlag() || this.isDecoration()) && (this.isPlanted() || this.isDecoration() && !this.template.decayOnDeed())) {
                            if (deeded && (!this.isAlwaysPoll() || this.isFlag() || this.getTemplateId() == 1178)) {
                                decay = false;
                            } else if (this.isStreetLamp() && this.getBless() != null && MethodsHighways.onHighway(this)) {
                                decay = false;
                            }
                        }
                        if ((dm = this.getDecayMultiplier()) > 1.0f) {
                            this.ticksSinceLastDecay += timesSinceLastUsed;
                            timesSinceLastUsed = (int)((float)this.ticksSinceLastDecay / dm);
                            if (timesSinceLastUsed > 0) {
                                this.ticksSinceLastDecay -= (int)((float)timesSinceLastUsed * dm);
                            } else {
                                decay = false;
                                this.setLastMaintained(WurmCalendar.currentTime);
                            }
                        }
                        if (decay) {
                            if (insideStructure) {
                                if (this.template.destroyOnDecay) {
                                    decayed = this.setDamage(this.damage + (float)(timesSinceLastUsed * 40));
                                } else if (Server.rand.nextInt(deeded ? 12 : 8) == 0) {
                                    if (this.template.positiveDecay) {
                                        if (this.getTemplateId() == 738) {
                                            this.setQualityLevel(Math.min(100.0f, this.qualityLevel + (100.0f - this.qualityLevel) * (100.0f - this.qualityLevel) / 10000.0f));
                                            this.checkGnome();
                                        }
                                    } else {
                                        decayed = this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(decayMin, this.getDamageModifier(true)));
                                    }
                                }
                            } else {
                                decayed = this.template.destroyOnDecay ? this.setDamage(this.damage + (float)(timesSinceLastUsed * 10)) : this.setDamage(this.damage + (float)timesSinceLastUsed * Math.max(decayMin, this.getDamageModifier(true)));
                            }
                        }
                        if (this.isPlantedFlowerpot() && decayed) {
                            try {
                                int revertType = -1;
                                revertType = this.isPotteryFlowerPot() ? 813 : (this.isMarblePlanter() ? 1001 : -1);
                                if (revertType != -1) {
                                    Item pot = ItemFactory.createItem(revertType, this.getQualityLevel(), this.creator);
                                    pot.setLastOwnerId(this.getLastOwnerId());
                                    pot.setDescription(this.getDescription());
                                    pot.setDamage(this.getDamage());
                                    pot.setPosXYZ(this.getPosX(), this.getPosY(), this.getPosZ());
                                    VolaTile tile = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
                                    if (tile != null) {
                                        tile.addItem(pot, false, false);
                                    }
                                }
                            }
                            catch (FailedException | NoSuchTemplateException e) {
                                Item.logWarn(e.getMessage(), e);
                            }
                        }
                        if (!decayed && this.lastMaintained != WurmCalendar.currentTime) {
                            this.setLastMaintained(WurmCalendar.currentTime);
                        }
                    }
                }
            }
        } else if (this.template.hugeAltar) {
            if (this.isHollow()) {
                boolean lastm = true;
                if (this.items != null) {
                    Item[] itarr;
                    for (Item it : itarr = this.items.toArray(new Item[this.items.size()])) {
                        if (it.deleted) continue;
                        it.poll(this, this.getTemperature(), insideStructure, deeded, true, true, false);
                    }
                }
            }
            this.pollHugeAltar();
        } else if (templateId == 521) {
            if (!this.isOnSurface()) {
                this.setDamage(this.getDamage() + 0.1f);
                Item.logInfo(this.getName() + " at " + this.getTilePos() + " on cave tile. Dealing damage.");
            } else {
                VolaTile t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
                if (t != null) {
                    if (t.isTransition) {
                        this.setDamage(this.getDamage() + 0.1f);
                        Item.logInfo(this.getName() + " at " + this.getTilePos() + " on surface transition tile. Dealing damage.");
                    }
                } else {
                    Item.logWarn(this.getName() + " at " + this.getTilePos() + " no tile on surface. Zone no. is " + this.getZoneId());
                }
            }
        } else if (templateId == 236) {
            this.checkItemSpawn();
        }
        if (templateId != 74) {
            this.coolOutSideItem(this.isAlwaysPoll(), insideStructure);
        }
        if (templateId == 445 && this.getData() > 0L) {
            try {
                Item contained = Items.getItem(this.getData());
                if (contained.poll(insideStructure, deeded, seed)) {
                    this.setData(0L);
                }
            }
            catch (NoSuchItemException contained) {
                // empty catch block
            }
        }
        if (this.spawnsTrees()) {
            for (int n = 0; n < 10; ++n) {
                int tile;
                int x = Zones.safeTileX(this.getTileX() - 18 + Server.rand.nextInt(36));
                int y = Zones.safeTileY(this.getTileY() - 18 + Server.rand.nextInt(36));
                boolean spawn = true;
                block21: for (int xx = x - 1; xx <= x + 1; ++xx) {
                    for (int yy = y - 1; yy <= y + 1; ++yy) {
                        int meshTile = Server.surfaceMesh.getTile(Zones.safeTileX(xx), Zones.safeTileY(yy));
                        if (!Tiles.getTile(Tiles.decodeType(meshTile)).isNormalTree()) continue;
                        spawn = false;
                        continue block21;
                    }
                }
                VolaTile t = Zones.getTileOrNull(x, y, this.isOnSurface());
                if (t != null) {
                    Item[] its;
                    for (Item i : its = t.getItems()) {
                        if (!i.isDestroyedOnDecay()) continue;
                        Items.destroyItem(i.getWurmId());
                    }
                }
                if (!spawn || !((double)Tiles.decodeHeight(tile = Server.surfaceMesh.getTile(x, y)) > 0.3) || !Tiles.canSpawnTree(Tiles.decodeType(tile))) continue;
                int age = 8 + Server.rand.nextInt(6);
                byte tree = (byte)Server.rand.nextInt(9);
                if (TreeData.TreeType.fromInt(tree).isFruitTree()) {
                    tree = (byte)(tree + 4);
                }
                byte newData = (byte)(age << 4);
                byte newType = TreeData.TreeType.fromInt(tree).asNormalTree();
                newData = (byte)(newData + 1 & 0xFF);
                Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), newType, newData);
                Players.getInstance().sendChangedTile(x, y, true, false);
            }
        }
        if (this.killsTrees()) {
            TilePos currPos = this.getTilePos();
            TilePos minPos = Zones.safeTile(currPos.add(-10, -10, null));
            TilePos maxPos = Zones.safeTile(currPos.add(10, 10, null));
            for (TilePos tPos : TilePos.areaIterator(minPos, maxPos)) {
                int tile = Server.surfaceMesh.getTile(tPos);
                byte tttype = Tiles.decodeType(tile);
                Tiles.Tile theTile = Tiles.getTile(tttype);
                if (!theTile.isNormalTree() && tttype != Tiles.Tile.TILE_GRASS.id && tttype != Tiles.Tile.TILE_DIRT.id && tttype != Tiles.Tile.TILE_KELP.id && tttype != Tiles.Tile.TILE_REED.id) continue;
                Server.setSurfaceTile(tPos, Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM.id, (byte)0);
                Players.getInstance().sendChangedTile(tPos, true, false);
                break;
            }
        }
        if (this.isWind() && !insideStructure && this.getParentId() == -10L && this.isOnSurface()) {
            float rot = Creature.normalizeAngle(Server.getWeather().getWindRotation() + 180.0f);
            if (this.getRotation() != this.ladderRotate(rot)) {
                this.setRotation(rot);
            }
        }
        if (!decayed && !insideStructure && this.isFlickering() && this.isOnFire() && Server.rand.nextFloat() * 10.0f < Server.getWeather().getRain()) {
            this.setTemperature((short)200);
        }
        if (this.getTemplateId() == 1178 && Server.rand.nextInt(20) == 0) {
            this.pollDistilling();
        }
        if (!decayed && this.isTrellis()) {
            this.ageTrellis();
        }
        return decayed;
    }

    void pollAging(boolean insideStructure, boolean deeded) {
        if (this.items != null && this.items.size() == 1) {
            int timesSinceLastUsed;
            int num = 2;
            if (!this.isOnSurface()) {
                num += 7;
            }
            if (insideStructure) {
                num += 4;
            }
            if (deeded) {
                num += 2;
            }
            Item[] itarr = this.items.toArray(new Item[this.items.size()]);
            Item item = itarr[0];
            if (!item.deleted && item.getTemplate().positiveDecay && item.isLiquid() && item.getAuxData() == 0 && (timesSinceLastUsed = (int)((WurmCalendar.currentTime - item.getLastMaintained()) / item.getTemplate().getDecayTime())) > 0 && Server.rand.nextInt(16 - num) == 0) {
                float bonus = this.getMaterialAgingModifier();
                if (Servers.isThisATestServer()) {
                    logger.info("Positive Decay added to" + item.getName() + " (" + item.getWurmId() + ") in " + this.getName() + " (" + this.id + ")");
                }
                item.setQualityLevel(Math.min(100.0f, item.getQualityLevel() + (100.0f - item.getQualityLevel()) * (100.0f - item.getQualityLevel()) / 10000.0f * bonus));
                item.setLastMaintained(WurmCalendar.currentTime);
            }
        }
    }

    void pollFermenting() {
        if (this.getItemsAsArray().length != 2) {
            return;
        }
        long lastMaintained = 0L;
        Item liquid = null;
        Item scrap = null;
        long lastowner = -10L;
        for (Item item : this.getItemsAsArray()) {
            if (lastMaintained < item.getLastMaintained()) {
                lastMaintained = item.getLastMaintained();
            }
            if (item.isLiquid()) {
                liquid = item;
                continue;
            }
            scrap = item;
            lastowner = scrap.lastOwner;
        }
        if (lastMaintained < WurmCalendar.currentTime - (Servers.isThisATestServer() ? 86400L : 2419200L)) {
            Recipe recipe = Recipes.getRecipeFor(lastowner, (byte)0, null, this, true, true);
            if (recipe == null) {
                return;
            }
            Skill primSkill = null;
            Creature lastown = null;
            float alc = 0.0f;
            boolean chefMade = false;
            double bonus = 0.0;
            boolean showOwner = false;
            try {
                lastown = Server.getInstance().getCreature(lastowner);
                bonus = lastown.getVillageSkillModifier();
                alc = ((Player)lastown).getAlcohol();
                Skills skills = lastown.getSkills();
                primSkill = skills.getSkillOrLearn(recipe.getSkillId());
                if (lastown.isRoyalChef()) {
                    chefMade = true;
                }
                showOwner = primSkill.getKnowledge(0.0) > 70.0;
            }
            catch (NoSuchCreatureException skills) {
            }
            catch (NoSuchPlayerException skills) {
                // empty catch block
            }
            int diff = recipe.getDifficulty(this);
            float power = 10.0f;
            if (primSkill != null) {
                power = (float)primSkill.skillCheck((float)diff + alc, null, bonus, false, recipe.getIngredientCount() + diff);
            }
            double ql = Math.min(99.0f, Math.max(1.0f, liquid.getCurrentQualityLevel() + power / 10.0f));
            if (chefMade) {
                ql = Math.max(30.0, ql);
            }
            if ((ql = primSkill != null ? Math.max(1.0, Math.min(primSkill.getKnowledge(0.0), ql)) : Math.max(1.0, Math.min((double)Math.max(scrap.getAuxData(), 20), ql))) > 70.0) {
                ql -= (double)Math.min(20.0f, (100.0f - liquid.getCurrentQualityLevel()) / 5.0f);
            }
            if (this.getRarity() > 0 || liquid.getRarity() > 0 || this.rarity > 0) {
                ql = GeneralUtilities.calcRareQuality(ql, this.getRarity(), liquid.getRarity(), this.rarity);
            }
            byte material = recipe.getResultMaterial(this);
            try {
                ItemTemplate it = recipe.getResultTemplate(this);
                String owner = showOwner ? PlayerInfoFactory.getPlayerName(lastowner) : null;
                Item newItem = ItemFactory.createItem(it.getTemplateId(), (float)ql, material, (byte)0, owner);
                newItem.setWeight(liquid.getWeightGrams(), true);
                newItem.setLastOwnerId(lastowner);
                if (RecipesByPlayer.saveRecipe(lastown, recipe, lastowner, null, this) && lastown != null) {
                    lastown.getCommunicator().sendServerMessage("Recipe \"" + recipe.getName() + "\" added to your cookbook.", 216, 165, 32, (byte)2);
                }
                newItem.calculateAndSaveNutrition(null, this, recipe);
                newItem.setName(recipe.getResultName(this));
                ItemTemplate rit = recipe.getResultRealTemplate(this);
                if (rit != null) {
                    newItem.setRealTemplate(rit.getTemplateId());
                }
                if (recipe.hasResultState()) {
                    newItem.setAuxData(recipe.getResultState());
                }
                if (lastown != null) {
                    recipe.addAchievements(lastown, newItem);
                } else {
                    recipe.addAchievementsOffline(lastowner, newItem);
                }
                for (Item item : this.getItemsAsArray()) {
                    Items.destroyItem(item.getWurmId());
                }
                this.insertItem(newItem);
                this.updateName();
            }
            catch (FailedException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            catch (NoSuchTemplateException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    void pollDistilling() {
        if (this.getTemperature() < 1500) {
            return;
        }
        Item boiler = null;
        Item condenser = null;
        for (Item item : this.getItemsAsArray()) {
            if (item.getTemplateId() == 1284) {
                boiler = item;
                continue;
            }
            if (item.getTemplateId() != 1285) continue;
            condenser = item;
        }
        if (boiler == null || condenser == null) {
            logger.warning("Still broken " + this.getWurmId());
            return;
        }
        if (boiler.getTemperature() < 1500) {
            return;
        }
        Item[] boilerItems = boiler.getItemsAsArray();
        if (boilerItems.length != 1) {
            return;
        }
        Item undistilled = boilerItems[0];
        long lastowner = undistilled.lastOwner;
        if (undistilled.getTemperature() < 1500) {
            return;
        }
        if (condenser.getFreeVolume() <= 0) {
            return;
        }
        if (undistilled.lastMaintained > WurmCalendar.currentTime - 600L) {
            return;
        }
        Recipe recipe = Recipes.getRecipeFor(lastowner, (byte)0, null, boiler, true, true);
        if (recipe == null) {
            return;
        }
        Skill primSkill = null;
        Creature lastown = null;
        float alc = 0.0f;
        boolean chefMade = false;
        double bonus = 0.0;
        boolean showOwner = false;
        try {
            lastown = Server.getInstance().getCreature(lastowner);
            bonus = lastown.getVillageSkillModifier();
            alc = ((Player)lastown).getAlcohol();
            Skills skills = lastown.getSkills();
            primSkill = skills.getSkillOrLearn(recipe.getSkillId());
            if (lastown.isRoyalChef()) {
                chefMade = true;
            }
            showOwner = primSkill.getKnowledge(0.0) > 70.0;
        }
        catch (NoSuchCreatureException skills) {
        }
        catch (NoSuchPlayerException skills) {
            // empty catch block
        }
        int diff = recipe.getDifficulty(boiler);
        float power = 0.0f;
        if (primSkill != null) {
            power = (float)primSkill.skillCheck((float)diff + alc, null, bonus, Server.rand.nextInt(60) != 0, recipe.getIngredientCount() + diff);
        }
        double ql = Math.min(99.0f, Math.max(1.0f, undistilled.getCurrentQualityLevel() + power / 10.0f));
        if (chefMade) {
            ql = Math.max(30.0, ql);
        }
        if ((ql = primSkill != null ? Math.max(1.0, Math.min(primSkill.getKnowledge(0.0), ql)) : Math.max(1.0, Math.min((double)Math.max(undistilled.getCurrentQualityLevel(), 20.0f), ql))) > 70.0) {
            ql -= (double)Math.min(20.0f, (100.0f - undistilled.getCurrentQualityLevel()) / 5.0f);
        }
        if (this.getRarity() > 0 || undistilled.getRarity() > 0 || this.rarity > 0) {
            ql = GeneralUtilities.calcRareQuality(ql, this.getRarity(), undistilled.getRarity(), this.rarity);
        }
        undistilled.setLastMaintained(WurmCalendar.currentTime);
        int oldWeight = undistilled.getWeightGrams();
        int usedWeight = Math.min(10, oldWeight);
        Item distilled = null;
        Item[] condenserItems = condenser.getItemsAsArray();
        if (condenserItems.length == 0) {
            byte material = recipe.getResultMaterial(boiler);
            try {
                ItemTemplate it = recipe.getResultTemplate(boiler);
                String owner = showOwner ? PlayerInfoFactory.getPlayerName(lastowner) : null;
                distilled = ItemFactory.createItem(it.getTemplateId(), (float)ql, material, (byte)0, owner);
                distilled.setLastOwnerId(lastowner);
                distilled.setWeight(usedWeight, true);
                distilled.setTemperature((short)1990);
                if (RecipesByPlayer.saveRecipe(lastown, recipe, lastowner, null, boiler) && lastown != null) {
                    lastown.getCommunicator().sendServerMessage("Recipe \"" + recipe.getName() + "\" added to your cookbook.", 216, 165, 32, (byte)2);
                }
                distilled.calculateAndSaveNutrition(null, undistilled, recipe);
                distilled.setName(recipe.getResultName(boiler));
                if (lastown != null) {
                    recipe.addAchievements(lastown, distilled);
                } else {
                    recipe.addAchievementsOffline(lastowner, distilled);
                }
                ItemTemplate rit = recipe.getResultRealTemplate(boiler);
                if (rit != null) {
                    distilled.setRealTemplate(rit.getTemplateId());
                }
                if (recipe.hasResultState()) {
                    distilled.setAuxData(recipe.getResultState());
                }
                undistilled.setWeight(oldWeight - usedWeight, true);
                condenser.insertItem(distilled);
            }
            catch (FailedException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            catch (NoSuchTemplateException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } else {
            distilled = condenserItems[0];
            int newUndistilledWeight = Math.max(oldWeight - usedWeight, 0);
            float newQl = (distilled.getCurrentQualityLevel() * (float)distilled.getWeightGrams() + (float)ql * (float)usedWeight) / (float)(distilled.getWeightGrams() + usedWeight);
            int newTemp = 1990;
            undistilled.setWeight(newUndistilledWeight, true);
            distilled.setQualityLevel(newQl);
            distilled.setTemperature((short)1990);
            distilled.setWeight(distilled.getWeightGrams() + usedWeight, true);
        }
        distilled.setLastMaintained(WurmCalendar.currentTime);
    }

    public void advancePlanterWeek() {
        Item parent = this.getParentOrNull();
        Item topParent = this.getTopParentOrNull();
        if (parent != null && parent.getTemplateId() != 1110) {
            return;
        }
        if (topParent != null && topParent != this && topParent != parent) {
            return;
        }
        int age = this.getAuxData() & 0x7F;
        int newAge = age + 4;
        if (newAge >= 100) {
            try {
                Item newPot = ItemFactory.createItem(1161, this.getCurrentQualityLevel(), this.getRarity(), this.creator);
                newPot.setLastOwnerId(this.getLastOwnerId());
                newPot.setDescription(this.getDescription());
                if (parent == null) {
                    newPot.setPosXYZRotation(this.getPosX(), this.getPosY(), this.getPosZ(), this.getRotation());
                    newPot.setIsPlanted(this.isPlanted());
                    VolaTile tile = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
                    if (tile != null) {
                        tile.addItem(newPot, false, false);
                    }
                } else {
                    parent.insertItem(newPot, true);
                }
                Items.destroyItem(this.getWurmId());
            }
            catch (FailedException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            catch (NoSuchTemplateException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } else {
            if (newAge > 5 && newAge < 95) {
                newAge += 128;
            }
            this.setAuxData((byte)newAge);
            this.setLastMaintained(WurmCalendar.currentTime);
        }
    }

    private void checkGnome() {
        boolean found = false;
        if (this.getItems() != null) {
            for (Item i : this.getItemsAsArray()) {
                if (i.getTemplateId() != 373) continue;
                found = true;
                this.setAuxData((byte)Math.min(100, this.getAuxData() + i.getWeightGrams() / 20));
                Items.destroyItem(i.getWurmId());
            }
        }
        if (found) {
            return;
        }
        this.setAuxData((byte)Math.max(0, this.getAuxData() - 10));
        if (this.qualityLevel > 30.0f && this.getAuxData() == 0) {
            this.doGnomeTrick();
        }
    }

    private float getParentDecayMultiplier(boolean includeMajor, boolean includeRune) {
        Item parent;
        float toReturn = 1.0f;
        if (includeMajor && (this.getTemplateId() == 1020 || this.getTemplateId() == 1022)) {
            toReturn = (float)((double)toReturn * 1.5);
            includeMajor = false;
        }
        if (includeRune && this.getSpellEffects() != null) {
            toReturn *= this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_INTERNAL_DECAY);
            includeRune = false;
        }
        if ((parent = this.getParentOrNull()) != null && (includeMajor || includeRune)) {
            toReturn *= parent.getParentDecayMultiplier(includeMajor, includeRune);
        }
        return toReturn;
    }

    private float getDecayMultiplier() {
        Item topParent;
        boolean isWrapped;
        float mult = this.getMaterialDecayTimeModifier();
        boolean bl = isWrapped = this.isWrapped() && (this.canBePapyrusWrapped() || this.canBeClothWrapped());
        float wrapMod = isWrapped ? 5.0f : (this.isSalted() ? 1.5f : 1.0f);
        mult *= wrapMod;
        Item parent = this.getParentOrNull();
        if (parent != null) {
            Item topContainer = parent.getParentOuterItemOrNull();
            if (this.isInLunchbox()) {
                Item lunchbox = topContainer;
                if (lunchbox != null) {
                    if (lunchbox.getTemplateId() == 1297) {
                        mult = (float)((double)mult * 1.5);
                    } else if (lunchbox.getTemplateId() == 1296) {
                        mult = (float)((double)mult * 1.25);
                    }
                }
            } else if (this.isLiquid() && topContainer != null && topContainer.getTemplateId() == 1117) {
                mult = (float)((double)mult * 2.0);
            }
            mult *= parent.getParentDecayMultiplier(true, true);
        }
        if ((topParent = this.getTopParentOrNull()) != null && topParent.getTemplateId() == 1277) {
            for (Item container : topParent.getItemsAsArray()) {
                if (container.getTemplateId() != 1278) continue;
                if (container.getItemsAsArray().length <= 0) break;
                mult *= (float)(1 + container.getItemsAsArray().length / 5);
                break;
            }
        }
        return mult;
    }

    public final boolean isItemSpawn() {
        return this.template.isItemSpawn();
    }

    private void spawnItemSpawn(int[] templateTypes, float startQl, float qlValRange, int maxNums, boolean onGround) {
        if (this.ownerId != -10L) {
            return;
        }
        Item[] currentItems = this.getAllItems(true);
        boolean[] hasTypes = new boolean[templateTypes.length];
        block2: for (Item item : currentItems) {
            for (int x = 0; x < templateTypes.length; ++x) {
                if (templateTypes[x] != item.getTemplateId()) continue;
                hasTypes[x] = true;
                continue block2;
            }
        }
        for (int x = 0; x < hasTypes.length; ++x) {
            if (hasTypes[x]) continue;
            for (int nums = 0; nums < maxNums; ++nums) {
                try {
                    byte rrarity;
                    int templateType = templateTypes[x];
                    if (onGround) {
                        ItemFactory.createItem(templateType, startQl + Server.rand.nextFloat() * qlValRange, this.getPosX() + 0.3f, this.getPosY() + 0.3f, 65.0f, this.isOnSurface(), (byte)0, -10L, "");
                        continue;
                    }
                    boolean isBoneCollar = templateType == 867;
                    byte by = rrarity = Server.rand.nextInt(100) == 0 || isBoneCollar ? (byte)1 : 0;
                    if (rrarity > 0) {
                        byte by2 = rrarity = Server.rand.nextInt(100) == 0 && isBoneCollar ? (byte)2 : 1;
                    }
                    if (rrarity > 1) {
                        rrarity = Server.rand.nextInt(100) == 0 && isBoneCollar ? (byte)3 : 2;
                    }
                    float newql = startQl + Server.rand.nextFloat() * qlValRange;
                    Item toInsert = ItemFactory.createItem(templateType, newql, rrarity, "");
                    if (templateType == 465) {
                        toInsert.setData1(CreatureTemplateCreator.getRandomDragonOrDrakeId());
                    }
                    if (templateType == 371) {
                        toInsert.setData1(CreatureTemplateCreator.getRandomDrakeId());
                    }
                    this.insertItem(toInsert, true);
                    continue;
                }
                catch (FailedException | NoSuchTemplateException e) {
                    Item.logWarn(e.getMessage(), e);
                }
            }
        }
    }

    public final void fillTreasureChest() {
        if (this.getAuxData() >= 0) {
            int[] templateTypes = new int[]{46, 72, 144, 316};
            this.spawnItemSpawn(templateTypes, 60.0f, 20.0f, 3, false);
        }
        if (this.getAuxData() >= 1) {
            int[] templateTypes2 = new int[]{204, 463};
            this.spawnItemSpawn(templateTypes2, 60.0f, 20.0f, 1, false);
        }
        if (this.getAuxData() >= 2) {
            int[] templateTypes3 = new int[]{765, 693, 697, 456};
            this.spawnItemSpawn(templateTypes3, 80.0f, 20.0f, 1, false);
        }
        if (this.getAuxData() >= 3) {
            int[] templateTypes4 = new int[]{374 + Server.rand.nextInt(10), Server.rand.nextInt(10) == 0 ? 525 : 837};
            this.spawnItemSpawn(templateTypes4, 80.0f, 20.0f, 1, false);
        }
        if (this.getAuxData() >= 4) {
            int[] templateTypes5 = new int[]{374 + Server.rand.nextInt(10), 610 + Server.rand.nextInt(10)};
            this.spawnItemSpawn(templateTypes5, 50.0f, 50.0f, 1, false);
        }
        if (this.getAuxData() >= 5) {
            int[] templateTypes6 = new int[]{349, 456, 694};
            this.spawnItemSpawn(templateTypes6, 50.0f, 40.0f, 1, false);
        }
        if (this.getAuxData() >= 6) {
            int[] templateTypes7 = new int[]{456, 204};
            this.spawnItemSpawn(templateTypes7, 80.0f, 20.0f, 5, false);
        }
        if (this.getAuxData() >= 7) {
            int valrei;
            int n = valrei = Server.rand.nextInt(5) == 0 ? 56 : (Server.rand.nextBoolean() ? 524 : 867);
            if (Server.rand.nextInt(Players.getInstance().getNumberOfPlayers() > 200 ? 10 : 40) == 0) {
                valrei = 795 + Server.rand.nextInt(16);
            }
            if (Server.rand.nextInt(1000) == 0) {
                valrei = 465;
            }
            int[] templateTypes5 = new int[]{valrei};
            this.spawnItemSpawn(templateTypes5, 99.0f, 1.0f, 1, false);
        }
    }

    /*
     * WARNING - void declaration
     */
    public final void checkItemSpawn() {
        if (this.ownerId != -10L) {
            return;
        }
        int templateId = this.template.getTemplateId();
        if (templateId == 236) {
            if (Servers.localServer.isChallengeServer() && WurmCalendar.getCurrentTime() > this.lastMaintained + 28800L) {
                VolaTile t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
                if (t != null && t.getItems().length < 50) {
                    int[] templateTypes = new int[]{46, 144};
                    this.spawnItemSpawn(templateTypes, 1.0f, 80.0f, 1, true);
                }
                this.lastMaintained = WurmCalendar.getCurrentTime();
            }
            return;
        }
        if (System.currentTimeMillis() > this.getData()) {
            int playerMod = Players.getInstance().getNumberOfPlayers() / 100;
            boolean haveManyPlayers = Players.getInstance().getNumberOfPlayers() > 200;
            switch (templateId) {
                case 969: {
                    int mins2;
                    int baseMins;
                    int[] nArray = new int[]{46, 72, 144, 316};
                    this.spawnItemSpawn(nArray, 60.0f, 20.0f, 3 + playerMod, false);
                    int[] templateTypes2 = new int[]{204};
                    this.spawnItemSpawn(templateTypes2, 60.0f, 20.0f, 3 + playerMod, false);
                    if (Servers.localServer.testServer) {
                        baseMins = 3;
                        mins2 = 1;
                    } else {
                        baseMins = 5;
                        int n = haveManyPlayers ? 20 : 40;
                        mins2 = Server.rand.nextInt(n);
                    }
                    this.setData(System.currentTimeMillis() + 60000L * (long)(baseMins + mins2));
                    this.deleteAllItemspawnEffects();
                    break;
                }
                case 970: {
                    if (Servers.localServer.isChallengeServer()) {
                        int n;
                        int n2;
                        int[] nArray = new int[]{46};
                        this.spawnItemSpawn(nArray, 80.0f, 20.0f, 3 + playerMod, false);
                        int[] templateTypes3 = new int[]{204, 144};
                        this.spawnItemSpawn(templateTypes3, 80.0f, 20.0f, 5 + playerMod, false);
                        int[] templateTypes2 = new int[]{765, 693, 697, 456};
                        this.spawnItemSpawn(templateTypes2, 80.0f, 20.0f, 4 + playerMod, false);
                        int[] templateTypes4 = new int[]{374 + Server.rand.nextInt(10), Server.rand.nextInt(3) == 0 ? 372 : 371};
                        this.spawnItemSpawn(templateTypes4, 80.0f, 20.0f, 3, false);
                        int n3 = n2 = Server.rand.nextInt(5) == 0 ? 56 : (Server.rand.nextBoolean() ? 524 : 867);
                        if (Server.rand.nextInt(haveManyPlayers ? 10 : 40) == 0) {
                            int n4 = 795 + Server.rand.nextInt(17);
                        }
                        if (Server.rand.nextInt(1000) == 0) {
                            n = 465;
                        }
                        int[] templateTypes5 = new int[]{n};
                        this.spawnItemSpawn(templateTypes5, 99.0f, 1.0f, 1, false);
                        this.setData(System.currentTimeMillis() + 3600000L + 60000L * (long)Server.rand.nextInt(haveManyPlayers ? 20 : 60));
                    } else {
                        int valrei;
                        int[] nArray = new int[]{693, 697, 456};
                        this.spawnItemSpawn(nArray, 80.0f, 20.0f, 4 + playerMod, false);
                        int[] templateTypes4 = new int[]{374 + Server.rand.nextInt(10)};
                        this.spawnItemSpawn(templateTypes4, 80.0f, 20.0f, 1, false);
                        int num = Server.rand.nextInt(7);
                        if (num == 0) {
                            valrei = 867;
                            int[] nArray2 = new int[]{valrei};
                            this.spawnItemSpawn(nArray2, 99.0f, 1.0f, 1, false);
                        } else if (num == 1) {
                            valrei = 973 + Server.rand.nextInt(6);
                            int[] nArray3 = new int[]{valrei};
                            this.spawnItemSpawn(nArray3, 99.0f, 1.0f, 1, false);
                        } else if (num == 2) {
                            valrei = 623;
                            int[] nArray4 = new int[]{valrei};
                            this.spawnItemSpawn(nArray4, 80.0f, 10.0f, 4, false);
                        } else if (num == 3) {
                            valrei = 666;
                            int[] nArray5 = new int[]{valrei};
                            this.spawnItemSpawn(nArray5, 80.0f, 10.0f, 1, false);
                        }
                        this.setData(System.currentTimeMillis() + 86400000L + 3600000L + 60000L * (long)Server.rand.nextInt(haveManyPlayers ? 20 : 60));
                    }
                    for (Player player : Players.getInstance().getPlayers()) {
                        player.playPersonalSound("sound.spawn.item.central");
                    }
                    this.deleteAllItemspawnEffects();
                    break;
                }
                case 971: {
                    int valrei;
                    int[] nArray = new int[]{44, 46, 132, 218, 38, 72, 30, 29, 32, 28, 35, 349, 456, 45, 694, 144, 316};
                    this.spawnItemSpawn(nArray, 50.0f, 40.0f, 50, false);
                    int[] templateTypes3 = new int[]{204};
                    this.spawnItemSpawn(templateTypes3, 50.0f, 40.0f, 100, false);
                    int[] templateTypes4 = new int[]{374 + Server.rand.nextInt(10), 610 + Server.rand.nextInt(10)};
                    this.spawnItemSpawn(templateTypes4, 50.0f, 50.0f, 10, false);
                    int n = Server.rand.nextBoolean() ? 867 : (valrei = Server.rand.nextBoolean() ? 524 : 525);
                    if (Server.rand.nextInt(1000) == 0) {
                        valrei = 465;
                    }
                    if (Server.rand.nextInt(10) == 0) {
                        int[] nArray6 = new int[]{765, 693, 697, 785, 456, 597, valrei};
                        this.spawnItemSpawn(nArray6, 50.0f, 40.0f, 1, false);
                    }
                    this.setData(System.currentTimeMillis() + 43200000L);
                    for (Player player : Players.getInstance().getPlayers()) {
                        player.playPersonalSound("sound.spawn.item.perimeter");
                    }
                    this.deleteAllItemspawnEffects();
                    break;
                }
            }
        } else {
            void var4_15;
            long timeout;
            switch (templateId) {
                case 970: {
                    String string = "central";
                    timeout = 600000L;
                    break;
                }
                case 971: {
                    String string = "perimeter";
                    timeout = 1800000L;
                    break;
                }
                default: {
                    return;
                }
            }
            if (System.currentTimeMillis() <= this.getData() - timeout) {
                return;
            }
            for (Effect effect : this.getEffects()) {
                if (effect.getType() != 19) continue;
                return;
            }
            Item.logInfo("Spawning " + (String)var4_15 + " effect since it doesn't exist: " + this.getEffects().length);
            Effect eff = EffectFactory.getInstance().createSpawnEff(this.getWurmId(), this.getPosX(), this.getPosY(), this.getPosZ(), this.isOnSurface());
            this.addEffect(eff);
        }
    }

    private void deleteAllItemspawnEffects() {
        for (Effect eff : this.getEffects()) {
            if (eff.getType() != 19) continue;
            this.deleteEffect(eff);
        }
    }

    private void doGnomeTrick() {
        Village current = Zones.getVillage(this.getTileX(), this.getTileY(), true);
        int dist = (int)Math.max(1.0f, this.qualityLevel - 30.0f);
        int dist2 = dist * dist;
        Item itemToPinch = null;
        for (int x = 0; x < dist; ++x) {
            block3: for (int y = 0; y < dist; ++y) {
                Item[] titems;
                VolaTile t;
                if (Server.rand.nextInt(dist2) != 0 || (t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface())) == null || current == null || t.getVillage() != current || (titems = t.getItems()).length <= 0) continue;
                for (Item tt : titems) {
                    if (tt == this) continue;
                    if (this.testInsertItem(tt)) {
                        itemToPinch = tt;
                        continue block3;
                    }
                    if (tt.isHollow()) {
                        Item[] pitems;
                        for (Item pt : pitems = tt.getAllItems(false)) {
                            if (!this.testInsertItem(pt)) continue;
                            itemToPinch = pt;
                            break;
                        }
                    }
                    if (itemToPinch != null) continue block3;
                }
            }
        }
        if (!(itemToPinch == null || itemToPinch.getSizeX() > this.getSizeX() || itemToPinch.getSizeY() > this.getSizeY() || itemToPinch.getSizeZ() > this.getSizeZ() || itemToPinch.isBulkItem() && itemToPinch.isLiquid() && itemToPinch.isHollow() && itemToPinch.isNoTake() && itemToPinch.isDecoration() || itemToPinch.getWeightGrams() > this.getWeightGrams())) {
            if (this.getAllItems(true).length >= 100) {
                return;
            }
            if (itemToPinch.getParentId() != -10L) {
                return;
            }
            if (this.getParentId() != -10L) {
                return;
            }
            try {
                Item.logInfo(this.getName() + " " + this.getWurmId() + " pinching " + itemToPinch.getName());
                itemToPinch.getParent().dropItem(itemToPinch.getWurmId(), false);
                this.insertItem(itemToPinch, true);
                this.setAuxData((byte)20);
            }
            catch (NoSuchItemException nsi) {
                Item.logWarn("Unexpected " + itemToPinch.getName() + " " + nsi.getMessage());
            }
        }
    }

    public final float ladderRotate(float rot) {
        int num = (int)((double)rot / 11.25);
        return (float)((double)(num /= 2) * 22.5);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void checkDrift() {
        Vehicle vehic;
        if (!this.isBoat() || !this.isOnSurface()) {
            return;
        }
        if (this.parentId != -10L) {
            return;
        }
        Vector3f worldPos = this.getPos3f();
        if (worldPos.z >= -1.0f) {
            return;
        }
        if (this.isMooredBoat()) {
            if (!Features.Feature.METALLIC_ITEMS.isEnabled()) return;
            try {
                Item anchor = Items.getItem(this.getData());
                if (anchor.isAnchor()) {
                    float driftChance = Item.getMaterialAnchorBonus(anchor.getMaterial());
                    if (Server.rand.nextFloat() < driftChance) {
                        return;
                    }
                }
            }
            catch (NoSuchItemException e) {
                return;
            }
        }
        if ((vehic = Vehicles.getVehicleForId(this.id)) == null) {
            return;
        }
        if (this.getCurrentQualityLevel() < 10.0f) {
            return;
        }
        if (vehic.pilotId != -10L) {
            return;
        }
        VolaTile t = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
        if (t == null) {
            Item.logWarn(this.getName() + " drifting, item has no tile at " + this.getTilePos() + " surfaced=" + this.isOnSurface());
            return;
        }
        float nPosX = worldPos.x + Server.getWeather().getXWind();
        float nPosY = worldPos.y + Server.getWeather().getYWind();
        float worldMetersX = CoordUtils.TileToWorld(Zones.worldTileSizeX);
        float worldMetersY = CoordUtils.TileToWorld(Zones.worldTileSizeY);
        if (nPosX <= 5.0f || nPosX >= worldMetersX - 10.0f || nPosY <= 5.0f || nPosY >= worldMetersY - 10.0f) {
            return;
        }
        int diffdecx = (int)(nPosX * 100.0f - worldPos.x * 100.0f);
        int diffdecy = (int)(nPosY * 100.0f - worldPos.y * 100.0f);
        if (diffdecx == 0 && diffdecy == 0) {
            return;
        }
        nPosX = worldPos.x + (float)diffdecx * 0.01f;
        nPosY = worldPos.y + (float)diffdecy * 0.01f;
        TilePos testPos = CoordUtils.WorldToTile(nPosX, nPosY);
        int meshTile = Server.caveMesh.getTile(testPos);
        byte tileType = Tiles.decodeType(meshTile);
        if (Tiles.isSolidCave(tileType) && (Tiles.decodeType(Server.caveMesh.getTile(this.getTilePos())) & 0xFF) != 201) {
            Item.logInfo(this.getName() + " drifting in rock at " + this.getTilePos() + ".");
            return;
        }
        try {
            if (Zones.calculateHeight(nPosX, nPosY, true) <= vehic.maxHeight) {
                t.moveItem(this, nPosX, nPosY, worldPos.z, Creature.normalizeAngle(this.getRotation()), true, worldPos.z);
                MovementScheme.itemVehicle = this;
                MovementScheme.movePassengers(vehic, null, false);
            }
            if (!(Zones.calculateHeight(worldPos.x, worldPos.y, true) >= vehic.maxHeight)) return;
            this.setDamage(this.getDamage() + (float)(diffdecx + diffdecy) * 0.1f);
            return;
        }
        catch (NoSuchZoneException nsz) {
            Items.destroyItem(this.id);
            Item.logInfo("ItemVehic " + this.getName() + " destroyed.");
        }
    }

    private boolean pollHatching() {
        if (this.isAbility()) {
            if (this.isPlanted()) {
                if ((int)this.damage == 3) {
                    Server.getInstance().broadCastMessage("The " + this.getName() + " starts to emanate a weird worrying sound.", this.getTileX(), this.getTileY(), this.isOnSurface(), 50);
                    this.setRarity((byte)2);
                }
                if ((int)this.damage == 50) {
                    Server.getInstance().broadCastMessage("The " + this.getName() + " starts to pulsate with a bright light, drawing from the ground.", this.getTileX(), this.getTileY(), this.isOnSurface(), 50);
                    this.setRarity((byte)3);
                }
                if ((int)this.damage == 75) {
                    Server.getInstance().broadCastMessage("The ground around " + this.getName() + " is shivering and heaving! Something big is going to happen here soon! You have to get far away!", this.getTileX(), this.getTileY(), this.isOnSurface(), 50);
                } else if ((int)this.damage == 95) {
                    Server.getInstance().broadCastMessage(LoginHandler.raiseFirstLetter(this.getName() + " is now completely covered in cracks. Run!"), this.getTileX(), this.getTileY(), this.isOnSurface(), 50);
                } else if ((int)this.damage == 99) {
                    Server.getInstance().broadCastMessage(LoginHandler.raiseFirstLetter(this.getNameWithGenus() + " is gonna explode! Too late to run..."), this.getTileX(), this.getTileY(), this.isOnSurface(), 20);
                }
            }
        } else if ((int)this.damage == 85) {
            Server.getInstance().broadCastMessage("Cracks are starting to form on " + this.getNameWithGenus() + ".", this.getTileX(), this.getTileY(), this.isOnSurface(), 20);
        } else if ((int)this.damage == 95) {
            Server.getInstance().broadCastMessage(LoginHandler.raiseFirstLetter(this.getNameWithGenus() + " is now completely covered in cracks."), this.getTileX(), this.getTileY(), this.isOnSurface(), 20);
        } else if ((int)this.damage == 99) {
            Server.getInstance().broadCastMessage(LoginHandler.raiseFirstLetter(this.getNameWithGenus() + " stirs as something emerges from it!"), this.getTileX(), this.getTileY(), this.isOnSurface(), 20);
        }
        return this.setDamage(this.damage + 1.0f);
    }

    private boolean pollLightSource() {
        Village village;
        if (this.isLightOverride) {
            return false;
        }
        float fuelModifier = 1.0f;
        if (this.getSpellEffects() != null) {
            fuelModifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_FUELUSE);
        }
        if (!this.isOnFire()) {
            if (this.getTemperature() > 600) {
                this.setTemperature((short)200);
            }
            return false;
        }
        if (this.getTemplateId() == 1301) {
            this.pollWagonerCampfire(WurmCalendar.getHour());
            return false;
        }
        if (this.getTemplateId() == 1243) {
            if (Server.getSecondsUptime() % (int)(600.0f * fuelModifier) == 0) {
                int aux = this.getAuxData();
                if (aux < 0) {
                    aux = 127;
                }
                this.setAuxData((byte)Math.max(0, aux - 1));
                if (this.getAuxData() <= 0) {
                    this.setTemperature((short)200);
                }
            }
            return false;
        }
        if (this.getTemplateId() == 1396) {
            if (this.getBless() != null) {
                return false;
            }
            if (!this.isPlanted()) {
                this.setTemperature((short)200);
                return false;
            }
        }
        if (!(this.isOilConsuming() || this.isCandleHolder() || this.isFireplace())) {
            if (!this.isIndestructible() && Server.getSecondsUptime() % 60 == 0) {
                return this.setQualityLevel(this.getQualityLevel() - 0.5f);
            }
            return false;
        }
        if (!(this.isStreetLamp() || this.isBrazier() || this.isFireplace())) {
            if (Server.getSecondsUptime() % (int)(300.0f * fuelModifier) == 0) {
                this.setAuxData((byte)Math.max(0, this.getAuxData() - 1));
                if (this.getAuxData() <= 0) {
                    this.setTemperature((short)200);
                }
            }
            return false;
        }
        boolean onSurface = this.isOnSurface();
        int hour = WurmCalendar.getHour();
        boolean autoSnuffMe = this.isAutoLit();
        VolaTile vt = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.surfaced);
        Structure structure = vt == null ? null : vt.getStructure();
        Village village2 = village = vt == null ? null : vt.getVillage();
        if (!autoSnuffMe) {
            boolean bl = autoSnuffMe = village != null || this.onBridge != -10L;
            if (!autoSnuffMe && this.getBless() != null) {
                boolean bl2 = autoSnuffMe = this.onBridge() != -10L;
                if (!autoSnuffMe && structure == null) {
                    if (onSurface) {
                        int encodedTile = Server.surfaceMesh.getTile(this.getTilePos());
                        autoSnuffMe = this.getBless() != null && Tiles.isRoadType(encodedTile);
                    } else {
                        int encodedTile = Server.caveMesh.getTile(this.getTilePos());
                        boolean bl3 = autoSnuffMe = this.getBless() != null && Tiles.isReinforcedFloor(Tiles.decodeType(encodedTile));
                    }
                }
            }
        }
        if (Server.rand.nextFloat() <= 0.16f * fuelModifier) {
            if (!autoSnuffMe) {
                this.setAuxData((byte)Math.max(0, this.getAuxData() - 1));
                if (this.getAuxData() <= 10) {
                    this.refuelLampFromClosestVillage();
                }
            }
            if (this.isFireplace() && village != null && structure != null && structure.isTypeHouse() && structure.isFinished() && this.getAuxData() <= 10) {
                this.fillFromVillage(village, true);
            }
            if (this.getAuxData() <= 0) {
                this.setTemperature((short)200);
                if (this.isFireplace()) {
                    this.deleteFireEffect();
                }
                return false;
            }
        }
        if (onSurface && hour > 4 && hour < 16 && autoSnuffMe) {
            this.setTemperature((short)200);
            if (this.isFireplace()) {
                this.deleteFireEffect();
            }
        }
        return false;
    }

    public void pollWagonerCampfire(int hour) {
        boolean onSurface = this.isOnSurface();
        boolean atCamp = false;
        Wagoner wagoner = Wagoner.getWagoner(this.lastOwner);
        if (wagoner != null && wagoner.isIdle()) {
            atCamp = true;
        }
        boolean isLit = this.getTemperature() > 200;
        boolean snuff = false;
        boolean light = false;
        int aux = 0;
        if (atCamp) {
            switch (hour) {
                case 0: 
                case 1: 
                case 2: 
                case 3: 
                case 4: 
                case 23: {
                    aux = 0;
                    snuff = true;
                    break;
                }
                case 5: {
                    if (this.getAuxData() == 0) {
                        aux = Server.rand.nextInt(10) + 1;
                    }
                    light = true;
                    wagoner.say(Wagoner.Speech.BREAKFAST);
                    break;
                }
                case 6: 
                case 7: 
                case 8: 
                case 9: 
                case 10: {
                    aux = 0;
                    snuff = onSurface;
                    light = !onSurface;
                    break;
                }
                case 11: {
                    if (this.getAuxData() == 0) {
                        aux = Server.rand.nextInt(10) + 1;
                    }
                    light = true;
                    wagoner.say(Wagoner.Speech.LUNCH);
                    break;
                }
                case 12: 
                case 13: 
                case 14: 
                case 15: 
                case 16: {
                    aux = 0;
                    snuff = onSurface;
                    light = !onSurface;
                    break;
                }
                case 17: {
                    if (this.getAuxData() == 0) {
                        aux = Server.rand.nextInt(5) + 11;
                    } else if (this.getAuxData() >= 11 && this.getAuxData() <= 15) {
                        aux = this.getAuxData() + 5;
                    }
                    light = true;
                    wagoner.say(Wagoner.Speech.DINNER);
                    break;
                }
                case 18: 
                case 19: 
                case 20: 
                case 21: 
                case 22: {
                    aux = 0;
                    light = true;
                }
            }
        } else {
            snuff = true;
        }
        if (snuff && isLit) {
            this.setTemperature((short)200);
            this.deleteFireEffect();
            this.setAuxData((byte)aux);
            this.updateIfGroundItem();
        } else if (light && !isLit) {
            this.setTemperature((short)10000);
            this.deleteFireEffect();
            Effect effect = EffectFactory.getInstance().createFire(this.getWurmId(), this.getPosX(), this.getPosY(), this.getPosZ(), this.isOnSurface());
            this.addEffect(effect);
            this.setAuxData((byte)aux);
            this.updateIfGroundItem();
        } else if (aux != this.getAuxData()) {
            this.setAuxData((byte)aux);
            this.updateIfGroundItem();
        }
    }

    private void refuelLampFromClosestVillage() {
        int startx = this.getTileX();
        int starty = this.getTileY();
        int stepSize = 7;
        for (int step = 1; step < 8; ++step) {
            Village vill;
            int distance = step * 7;
            for (int yOffs = -distance; yOffs <= distance; yOffs += 14) {
                int ys = Zones.safeTileY(starty + yOffs);
                for (int x = startx - distance; x <= startx + distance; x += 7) {
                    vill = Villages.getVillage(Zones.safeTileX(x), ys, true);
                    if (!this.fillFromVillage(vill, false)) continue;
                    return;
                }
            }
            for (int xOffs = -distance; xOffs <= distance; xOffs += 14) {
                int xs = Zones.safeTileX(startx + xOffs);
                for (int y = starty - distance; y < starty + distance; y += 7) {
                    vill = Villages.getVillage(xs, Zones.safeTileY(y), true);
                    if (!this.fillFromVillage(vill, false)) continue;
                    return;
                }
            }
        }
    }

    private boolean fillFromVillage(@Nullable Village vill, boolean onDeed) {
        if (vill != null) {
            int received = vill.getOilAmount(110, onDeed);
            if (received > 0) {
                this.setAuxData((byte)(this.getAuxData() + received));
            }
            return true;
        }
        return false;
    }

    private void checkIfLightStreetLamp() {
        Village village;
        boolean isValidFireplace;
        boolean isValidStreetLamp = this.isStreetLamp() && this.isPlanted() && this.getAuxData() > 0;
        boolean isValidBrazier = this.isBrazier() && this.getAuxData() > 0;
        boolean bl = isValidFireplace = this.isFireplace() && this.getAuxData() > 0;
        if (!(isValidStreetLamp || isValidBrazier || isValidFireplace || this.getTemplateId() == 1301)) {
            return;
        }
        if (this.getTemperature() > 200) {
            return;
        }
        if (this.getTemplateId() == 1301) {
            this.pollWagonerCampfire(WurmCalendar.getHour());
            return;
        }
        boolean onSurface = this.isOnSurface();
        int hour = WurmCalendar.getHour();
        boolean autoLightMe = this.isAutoLit();
        VolaTile vt = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.surfaced);
        Structure structure = vt == null ? null : vt.getStructure();
        Village village2 = village = vt == null ? null : vt.getVillage();
        if (!autoLightMe) {
            boolean bl2 = autoLightMe = village != null || this.onBridge != -10L;
            if (!autoLightMe && this.getBless() != null) {
                boolean bl3 = autoLightMe = this.onBridge() != -10L;
                if (!autoLightMe && structure == null) {
                    int encodedTile;
                    if (onSurface) {
                        encodedTile = Server.surfaceMesh.getTile(this.getTilePos());
                        autoLightMe = this.getBless() != null && Tiles.isRoadType(encodedTile);
                    } else {
                        encodedTile = Server.caveMesh.getTile(this.getTilePos());
                        boolean bl4 = autoLightMe = this.getBless() != null && Tiles.isReinforcedFloor(Tiles.decodeType(encodedTile));
                    }
                }
            }
        }
        if (onSurface && (hour < 4 || hour > 16) && autoLightMe) {
            this.setTemperature((short)10000);
            if (this.isFireplace()) {
                this.deleteFireEffect();
                Effect effect = EffectFactory.getInstance().createFire(this.getWurmId(), this.getPosX(), this.getPosY(), this.getPosZ(), this.isOnSurface());
                this.addEffect(effect);
            }
        }
    }

    private void pollHugeAltar() {
        if (this.damage <= 0.0f) {
            return;
        }
        boolean heal = false;
        if (this.getTemplateId() == 328) {
            if (System.currentTimeMillis() - lastPolledBlackAltar > 600000L) {
                heal = true;
                lastPolledBlackAltar = System.currentTimeMillis();
            }
        } else if (System.currentTimeMillis() - lastPolledWhiteAltar > 600000L) {
            heal = true;
            lastPolledWhiteAltar = System.currentTimeMillis();
        }
        if (heal) {
            this.setDamage(this.damage - 1.0f);
            if (this.damage > 0.0f) {
                Server.getInstance().broadCastNormal("You have a sudden vision of " + this.getName() + " being under attack.");
            }
        }
    }

    private void coolItem(int ticks) {
        if (this.getTemperature() > 200 && (!this.isLight() || this.isLightOverride()) && this.getTemplateId() != 1243 && this.getTemplateId() != 1301) {
            int degrees = 5;
            if (this.isInsulated()) {
                Item outer = this.getParentOuterItemOrNull();
                if (outer != null && Server.rand.nextInt(99) < 70 + outer.getRarity() * 2) {
                    return;
                }
                degrees = 1;
            }
            short oldTemperature = this.getTemperature();
            this.setTemperature((short)Math.max(200, this.getTemperature() - ticks * degrees));
            if (this.getTemperatureState(oldTemperature) != this.getTemperatureState(this.getTemperature())) {
                this.notifyWatchersTempChange();
            }
        }
    }

    private void coolInventoryItem() {
        this.coolItem(1);
    }

    private void coolInventoryItem(long timeSinceLastCool) {
        int ticks = (int)Math.min(timeSinceLastCool / 1000L, 0x19999998L);
        this.coolItem(ticks);
    }

    public final boolean isOilConsuming() {
        return this.template.oilConsuming;
    }

    public boolean isCandleHolder() {
        return this.template.candleHolder;
    }

    public boolean isFireplace() {
        return this.getTemplateId() == 889;
    }

    private void coolOutSideItem(boolean everySecond, boolean insideStructure) {
        if (this.temperature > 200) {
            float speed = 1.0f;
            if (insideStructure) {
                speed *= 0.75f;
            } else if ((double)Server.getWeather().getRain() > 0.2) {
                speed *= 2.0f;
            }
            if (this.getRarity() > 0) {
                speed = (float)((double)speed * Math.pow(0.9f, this.getRarity()));
            }
            int templateId = this.template.getTemplateId();
            if (this.getSpellEffects() != null && (templateId == 180 || templateId == 1023 || templateId == 1028 || templateId == 1178 || templateId == 37 || templateId == 178) && Server.rand.nextFloat() < this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_FUELUSE) - 1.0f) {
                speed = 0.0f;
            }
            if (this.getTemplateId() == 180 || this.getTemplateId() == 178 || this.getTemplateId() == 1023 || this.getTemplateId() == 1028) {
                if (System.currentTimeMillis() - 60000L > this.lastAuxPoll && this.getTemperature() > 200 && this.getAuxData() < 30) {
                    this.setAuxData((byte)(this.getAuxData() + 1));
                    this.lastAuxPoll = System.currentTimeMillis();
                }
                if (this.getAuxData() > 30) {
                    this.setAuxData((byte)30);
                }
            }
            if (templateId == 180 || templateId == 1023 || templateId == 1028) {
                this.setTemperature((short)Math.max(200.0f, (float)this.temperature - speed * Math.max(1.0f, 11.0f - Math.max(1.0f, 20.0f * Math.max(30.0f, this.getCurrentQualityLevel()) / 200.0f))));
            } else if (templateId == 1178) {
                this.setTemperature((short)Math.max(200.0f, (float)this.temperature - speed * 0.5f * Math.max(1.0f, 11.0f - Math.max(1.0f, 10.0f * Math.max(30.0f, this.getCurrentQualityLevel()) / 200.0f))));
            } else if (templateId == 37 || templateId == 178) {
                this.setTemperature((short)Math.max(200.0f, (float)this.temperature - speed * Math.max(1.0f, 11.0f - Math.max(1.0f, 10.0f * Math.max(30.0f, this.getCurrentQualityLevel()) / 200.0f))));
                if (templateId == 37 && this.temperature <= 210) {
                    if (this.getItems().isEmpty()) {
                        float ql = this.getCurrentQualityLevel();
                        try {
                            ItemFactory.createItem(141, ql, this.getPosX(), this.getPosY(), this.getRotation(), this.isOnSurface(), this.getRarity(), this.getBridgeId(), null);
                        }
                        catch (NoSuchTemplateException nst) {
                            Item.logWarn("No template for ash?" + nst.getMessage(), nst);
                        }
                        catch (FailedException fe) {
                            Item.logWarn("What's this: " + fe.getMessage(), fe);
                        }
                    }
                    this.setQualityLevel(0.0f);
                    this.deleteFireEffect();
                }
            } else if (this.isLight() && !this.isLightOverride() || this.isFireplace() || this.getTemplateId() == 1243 || this.getTemplateId() == 1301) {
                this.pollLightSource();
            } else if (everySecond) {
                this.setTemperature((short)Math.max(200.0f, (float)this.temperature - speed * 20.0f));
            } else {
                this.setTemperature((short)Math.max(200.0f, (float)this.temperature - speed * 800.0f * 5.0f));
            }
        }
        if (!this.isOnFire()) {
            if (this.isStreetLamp() || this.isBrazier() || this.isFireplace() || this.getTemplateId() == 1301) {
                this.checkIfLightStreetLamp();
            } else {
                this.deleteFireEffect();
            }
        }
    }

    private void modTemp(Item parent, int parentTemp, boolean insideStructure) {
        short oldTemperature = this.temperature;
        if (parentTemp > 200) {
            short diff;
            short oldTemp;
            float qualityModifier = 1.0f;
            int parentTemplateId = parent.getTemplateId();
            float tempMod = 10.0f + 10.0f * (float)Math.min(10.0, Server.getModifiedPercentageEffect(this.getCurrentQualityLevel()) / 100.0);
            boolean dealDam = Server.rand.nextInt(30) == 0;
            Item top = null;
            try {
                top = Items.getItem(this.getTopParent());
                int tp = top.getTemplateId();
                if (tp == 180 || tp == 1023 || tp == 1028) {
                    tempMod = (float)((double)tempMod + (double)(10 + top.getRarity()) * Math.min(10.0, Server.getModifiedPercentageEffect(top.getCurrentQualityLevel()) / 70.0));
                    if (this.isFood() && Server.rand.nextInt(5) == 0) {
                        dealDam = true;
                    }
                    if (this.isBulk() && this.isFood()) {
                        qualityModifier = 0.8f;
                    }
                } else if (tp == 37 || tp == 178 || tp == 1178) {
                    tempMod = (float)((double)tempMod + (double)(5 + top.getRarity()) * Math.min(10.0, Server.getModifiedPercentageEffect(top.getCurrentQualityLevel()) / 70.0));
                    if (this.isBulk()) {
                        if (this.isFood()) {
                            if (tp == 37) {
                                qualityModifier = 0.8f;
                            }
                        } else {
                            qualityModifier = 0.8f;
                        }
                    }
                }
            }
            catch (NoSuchItemException tp) {
                // empty catch block
            }
            if (!parent.equals(top)) {
                tempMod = (float)((double)tempMod + (double)(7 + parent.getRarity()) * Math.min(10.0, Server.getModifiedPercentageEffect(parent.getCurrentQualityLevel()) / 100.0));
            }
            tempMod = Math.max(10.0f, tempMod);
            dealDam = parentTemplateId != 74;
            short newTemp = oldTemp = this.getTemperature();
            if (this.isFood() && oldTemp > 1500) {
                tempMod = 1.0f;
            }
            if ((diff = (short)(parentTemp - oldTemp)) > 0) {
                newTemp = (short)Math.min((float)parentTemp, (float)oldTemp + Math.min((float)diff, tempMod));
            } else if (diff < 0) {
                newTemp = (short)Math.max((float)parentTemp, (float)oldTemp + Math.max((float)diff, -tempMod));
            }
            if (this.isBurnable()) {
                if (newTemp > 1000 && !this.isIndestructible()) {
                    if (dealDam) {
                        if (this.isRepairable()) {
                            this.setQualityLevel(this.getQualityLevel() - Math.max(2.0f, tempMod / 10.0f));
                        } else {
                            this.setDamage(this.getDamage() + Math.max(2.0f, tempMod / 10.0f));
                        }
                    }
                    if (this.getDamage() >= 100.0f) {
                        int w = this.getWeightGrams() * Item.fuelEfficiency(this.getMaterial());
                        int newt = parentTemp + w;
                        if (top != null && this.getTemplateId() == 1276 && top.isOnFire() && top.getTemplateId() != 1178) {
                            Player[] lastOwner;
                            for (Player player : lastOwner = Players.getInstance().getPlayers()) {
                                if (this.getLastOwner() != player.getWurmId()) continue;
                                player.getCommunicator().sendNormalServerMessage("The " + this.getName() + " melts and puts out the flames, why did you do that?");
                            }
                            top.setTemperature((short)200);
                            top.deleteFireEffect();
                            return;
                        }
                        if (this.getTemplateId() != 1276) {
                            parent.setTemperature((short)Math.min(30000, newt));
                        }
                        return;
                    }
                    if (this.getQualityLevel() <= 1.0E-4f) {
                        int w = this.getWeightGrams() * Item.fuelEfficiency(this.getMaterial());
                        int newt = parentTemp + w;
                        parent.setTemperature((short)Math.min(30000, newt));
                        return;
                    }
                }
                this.temperature = newTemp;
            } else {
                if (this.getTemplateId() == 1285) {
                    newTemp = (short)Math.min(300, newTemp);
                }
                this.setTemperature(newTemp);
                if (this.isEgg() && newTemp > 400 && this.getData1() > 0) {
                    this.setData1(-1);
                }
                if (this.getTemplateId() == 128 && this.isSalted() && newTemp > 3000 && parent.getItemCount() == 1) {
                    try {
                        int salts = this.getWeightGrams() / 1000 / 10;
                        Skill skill = null;
                        try {
                            Creature performer = Server.getInstance().getCreature(this.getLastOwnerId());
                            skill = performer.getSkills().getSkillOrLearn(10038);
                        }
                        catch (Exception performer) {
                            // empty catch block
                        }
                        float ql = 20.0f;
                        if (skill != null) {
                            float result = (float)skill.skillCheck(50.0, this, 0.0, false, salts / 10);
                            ql = 10.0f + (result + 100.0f) * 0.45f;
                        }
                        for (int x = 0; x < salts; ++x) {
                            Item salt = ItemFactory.createItem(349, ql, (byte)36, this.getRarity(), this.creator);
                            salt.setLastOwnerId(this.getLastOwnerId());
                            salt.setTemperature(newTemp);
                            parent.insertItem(salt, true);
                        }
                        this.setWeight(this.getWeightGrams() - (salts + 1) * 100, true);
                        this.setIsSalted(false);
                    }
                    catch (FailedException e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                    catch (NoSuchTemplateException e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                } else if (!this.isLight() || this.isLightOverride()) {
                    TempStates.checkForChange(parent, this, oldTemp, newTemp, qualityModifier);
                }
            }
        } else if (this.temperature > 200) {
            if (!this.isLight() || this.isLightOverride()) {
                float speed = 1.0f;
                if (insideStructure) {
                    speed *= 0.75f;
                } else if ((double)Server.getWeather().getRain() > 0.5) {
                    speed *= 1.5f;
                }
                if (this.isInLunchbox()) {
                    this.coolItem(1);
                } else if (parent.isAlwaysPoll()) {
                    this.setTemperature((short)Math.max(200.0f, (float)this.temperature - 20.0f * speed));
                } else {
                    this.setTemperature((short)Math.max(200.0f, (float)this.temperature - (float)Zones.numberOfZones / 3.0f * speed));
                }
            } else {
                this.pollLightSource();
            }
        }
        if (this.getTemperatureState(oldTemperature) != this.getTemperatureState(this.temperature)) {
            this.notifyWatchersTempChange();
        }
    }

    public boolean isBurnable() {
        return !this.isLocked() && (this.isWood() || this.isCloth() || this.isMelting() || this.isLiquidInflammable() || this.isPaper()) && !this.isLiquidCooking() && this.getTemplateId() != 651 && this.getTemplateId() != 1097 && this.getTemplateId() != 1098 && this.getTemplateId() != 1392;
    }

    void notifyWatchersTempChange() {
        if (this.watchers == null) {
            return;
        }
        for (Creature watcher : this.watchers) {
            watcher.getCommunicator().sendUpdateInventoryItemTemperature(this);
        }
    }

    public byte getTemperatureState(short theTemperature) {
        int result = theTemperature < 0 ? -1 : (theTemperature < 400 ? 0 : (theTemperature < 1000 ? 1 : (theTemperature < 2000 ? 2 : (this.isLiquid() ? 3 : (theTemperature < 3500 ? 4 : 5)))));
        return (byte)result;
    }

    private void createDaleItems() {
        try {
            float creationQL = this.getQualityLevel() * this.getMaterialDaleModifier();
            Item coal = ItemFactory.createItem(204, creationQL, null);
            coal.setLastOwnerId(this.lastOwner);
            if (this.getRarity() > 0 && Server.rand.nextInt(10) == 0) {
                coal.setRarity(this.getRarity());
            }
            this.insertItem(coal);
            Item tar = ItemFactory.createItem(153, creationQL, null);
            tar.setLastOwnerId(this.lastOwner);
            if (this.getRarity() > 0 && Server.rand.nextInt(10) == 0) {
                tar.setRarity(this.getRarity());
            }
            this.insertItem(tar);
            Item ash = ItemFactory.createItem(141, creationQL, null);
            ash.setLastOwnerId(this.lastOwner);
            this.insertItem(ash);
            if (this.getRarity() > 0 && Server.rand.nextInt(10) == 0) {
                ash.setRarity(this.getRarity());
            }
        }
        catch (NoSuchTemplateException nst) {
            Item.logWarn("No template for ash?" + nst.getMessage(), nst);
        }
        catch (FailedException fe) {
            Item.logWarn("What's this: " + fe.getMessage(), fe);
        }
    }

    public void deleteFireEffect() {
        Effect toDelete = null;
        if (this.effects != null) {
            for (Effect eff : this.effects) {
                if (eff.getType() != 0) continue;
                toDelete = eff;
            }
            if (toDelete != null) {
                this.deleteEffect(toDelete);
            }
        }
    }

    private void sendStatus() {
        this.sendUpdate();
    }

    public final boolean isOnFire() {
        if (this.isAlwaysLit()) {
            return true;
        }
        if (this.temperature < 600) {
            return false;
        }
        if (this.getTemplateId() != 729) {
            return true;
        }
        return this.getAuxData() > 0;
    }

    public final Behaviour getBehaviour() throws NoSuchBehaviourException {
        return this.template.getBehaviour();
    }

    public final short getImageNumber() {
        if (this.getTemplateId() == 1310) {
            try {
                Creature storedCreature = Creatures.getInstance().getCreature(this.getData());
                if (storedCreature.getTemplateId() == 45) {
                    return 404;
                }
            }
            catch (NoSuchCreatureException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        if (this.getTemplateId() == 1307) {
            if (this.getData1() <= 0) {
                return 1460;
            }
            switch (this.getMaterial()) {
                case 62: {
                    return 1461;
                }
                case 15: {
                    return 1462;
                }
                case 89: {
                    return 1463;
                }
                case 61: {
                    return 1464;
                }
                case 19: {
                    return 1465;
                }
                case 11: {
                    return 1467;
                }
                case 7: 
                case 96: {
                    return 1468;
                }
                case 8: {
                    return 1469;
                }
                case 10: {
                    return 1470;
                }
                case 30: {
                    return 1471;
                }
                case 13: {
                    return 1472;
                }
                case 12: {
                    return 1473;
                }
                case 34: {
                    return 1474;
                }
            }
            if (MaterialUtilities.isMetal(this.getMaterial())) {
                return 1467;
            }
            if (MaterialUtilities.isWood(this.getMaterial())) {
                return 1466;
            }
            if (MaterialUtilities.isClay(this.getMaterial())) {
                return 1465;
            }
            if (this.getRealTemplate() != null) {
                return this.getRealTemplate().getImageNumber();
            }
            return 1460;
        }
        if (this.getTemplateId() == 1346) {
            if (this.isEmpty(false)) {
                return 846;
            }
            Item reel = this.items.iterator().next();
            if (reel.isEmpty(false)) {
                return 866;
            }
            return 886;
        }
        if (this.getTemplateId() == 387) {
            try {
                ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(this.getData1());
                return temp.getImageNumber();
            }
            catch (NoSuchTemplateException temp) {}
        } else {
            if (this.realTemplate > 0 && this.getTemplate().useRealTemplateIcon()) {
                if (!(this.getRealTemplateId() != 92 && this.getRealTemplateId() != 368 || this.isRaw())) {
                    return 523;
                }
                ItemTemplate realTemplate = this.getRealTemplate();
                assert (realTemplate != null);
                return realTemplate.getImageNumber();
            }
            if (!(this.getTemplateId() != 92 && this.getTemplateId() != 368 || this.isRaw())) {
                return 523;
            }
        }
        Recipe recipe = this.getRecipe();
        if (recipe != null && !recipe.getResultItem().isFoodGroup()) {
            return recipe.getResultItem().getIcon();
        }
        return this.template.getImageNumber();
    }

    public final boolean isOnSurface() {
        if ((long)this.getZoneId() != -10L) {
            return this.surfaced;
        }
        if (this.getOwnerId() == -10L) {
            return this.surfaced;
        }
        try {
            Creature owner = Server.getInstance().getCreature(this.getOwnerId());
            return owner.isOnSurface();
        }
        catch (NoSuchCreatureException owner) {
        }
        catch (NoSuchPlayerException nsp) {
            Item.logInfo(this.id + " strange. Owner " + this.getOwnerId() + " is no creature or player.");
        }
        return this.surfaced;
    }

    private final String containsExamine(Creature performer) {
        String toReturn = " ";
        if (this.isFishingReel()) {
            toReturn = toReturn + "The rod contains a " + this.getName() + ". ";
            Item fishingLine = this.getFishingLine();
            if (fishingLine == null) {
                String lineName = this.getFishingLineName();
                toReturn = toReturn + "Requires a " + lineName + " to be able to be used for fishing.";
            } else {
                toReturn = toReturn + "The reel contains a " + fishingLine.getName() + ".";
                toReturn = toReturn + fishingLine.containsExamine(performer);
            }
        } else if (this.isFishingLine()) {
            Item fishingFloat = this.getFishingFloat();
            Item fishingHook = this.getFishingHook();
            if (fishingFloat == null && fishingHook == null) {
                toReturn = toReturn + "The line requires a float and a fishing hook to be able to be used for fishing.";
            } else {
                toReturn = fishingFloat == null ? toReturn + "The line requires a float to be able to be used for fishing." : toReturn + fishingFloat.containsExamine(performer);
                toReturn = fishingHook == null ? toReturn + "The line requires a fishing hook to be able to be used for fishing." : toReturn + fishingHook.containsExamine(performer);
            }
        } else if (this.isFishingFloat()) {
            toReturn = toReturn + "There is a " + this.getName() + " float on the line.";
        } else if (this.isFishingHook()) {
            toReturn = toReturn + "There is a " + this.getName() + " on the end of the line. ";
            Item fishingBait = this.getFishingBait();
            toReturn = fishingBait == null ? toReturn + "There is no bait, but then again some fish like that!" : toReturn + fishingBait.containsExamine(performer);
        } else if (this.isFishingBait()) {
            toReturn = toReturn + "There is " + this.getName() + " as bait on the fishing hook.";
        }
        return toReturn;
    }

    public final String examine(Creature performer) {
        Wagoner wagoner;
        Kingdom k;
        String toReturn = this.template.getDescriptionLong();
        if (this.getTemplateId() == 1311) {
            toReturn = !this.isEmpty(true) ? this.template.getDescriptionLong() + " There is a creature in this cage." : this.template.getDescriptionLong() + " This cage is empty.";
        }
        if (this.template.templateId == 133 && this.getRealTemplateId() == 1254) {
            toReturn = toReturn.replace("tallow", "beeswax");
        }
        boolean gotDesc = false;
        Recipe recipe = this.getRecipe();
        if (recipe != null) {
            String desc = recipe.getResultItem().getResultDescription(this);
            if (desc.length() > 0 && !desc.startsWith("Any ")) {
                toReturn = desc;
                gotDesc = true;
            }
        } else {
            String animal;
            if (this.getTemplateId() == 1344) {
                toReturn = toReturn + " It is made from " + Item.getMaterialString(this.getMaterial()) + ".";
                toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
                Item fishingLine = this.getFishingLine();
                if (fishingLine == null) {
                    toReturn = toReturn + " Requires a basic fishing line to be able to be used for fishing.";
                } else {
                    toReturn = toReturn + " The pole has a " + fishingLine.getName() + " attached to the end of it.";
                    toReturn = toReturn + fishingLine.containsExamine(performer);
                }
                toReturn = toReturn + MethodsItems.getRarityDesc(this.getRarity());
                return toReturn;
            }
            if (this.getTemplateId() == 1346) {
                toReturn = toReturn + " It is made from " + Item.getMaterialString(this.getMaterial()) + ".";
                toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
                Item fishingReel = this.getFishingReel();
                toReturn = fishingReel == null ? toReturn + " Requires a fishing reel to be able to be used for fishing." : toReturn + fishingReel.containsExamine(performer);
                toReturn = toReturn + MethodsItems.getRarityDesc(this.getRarity());
                return toReturn;
            }
            if (this.isFishingReel()) {
                toReturn = toReturn + " It is made from " + Item.getMaterialString(this.getMaterial()) + ".";
                toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
                Item fishingLine = this.getFishingLine();
                if (fishingLine == null) {
                    String lineName = this.getFishingLineName();
                    toReturn = toReturn + " Requires a " + lineName + " to be able to be used for fishing.";
                } else {
                    toReturn = toReturn + " The reel contains a " + fishingLine.getName() + ".";
                    toReturn = toReturn + fishingLine.containsExamine(performer);
                }
                toReturn = toReturn + MethodsItems.getRarityDesc(this.getRarity());
                return toReturn;
            }
            if (this.isFishingLine()) {
                toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
                Item fishingFloat = this.getFishingFloat();
                Item fishingHook = this.getFishingHook();
                if (fishingFloat == null && fishingHook == null) {
                    toReturn = toReturn + " The line requires a float and a fishing hook to be able to be used for fishing.";
                } else {
                    toReturn = fishingFloat == null ? toReturn + " The line requires a float to be able to be used for fishing." : toReturn + fishingFloat.containsExamine(performer);
                    toReturn = fishingHook == null ? toReturn + " The line requires a fishing hook to be able to be used for fishing." : toReturn + fishingHook.containsExamine(performer);
                }
                toReturn = toReturn + MethodsItems.getRarityDesc(this.getRarity());
                return toReturn;
            }
            if (this.isFishingHook()) {
                toReturn = toReturn + " It is made from " + Item.getMaterialString(this.getMaterial()) + ".";
                toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
                Item fishingBait = this.getFishingBait();
                toReturn = fishingBait == null ? toReturn + "There is no bait, but then again some fish like that!" : toReturn + fishingBait.containsExamine(performer);
                toReturn = toReturn + MethodsItems.getRarityDesc(this.getRarity());
                return toReturn;
            }
            if (this.getTemplateId() == 1310) {
                Creature[] storedanimal;
                for (Creature cret : storedanimal = Creatures.getInstance().getCreatures()) {
                    long careTaker;
                    PlayerInfo info;
                    if (cret.getWurmId() != this.getData()) continue;
                    String exa = cret.examine();
                    performer.getCommunicator().sendNormalServerMessage(exa);
                    Brand brand = Creatures.getInstance().getBrand(this.getData());
                    if (brand != null) {
                        try {
                            Village v = Villages.getVillage((int)brand.getBrandId());
                            performer.getCommunicator().sendNormalServerMessage("It has been branded by and belongs to the settlement of " + v.getName() + ".");
                        }
                        catch (NoSuchVillageException nsv) {
                            brand.deleteBrand();
                        }
                    }
                    if (cret.isCaredFor() && (info = PlayerInfoFactory.getPlayerInfoWithWurmId(careTaker = cret.getCareTakerId())) != null) {
                        performer.getCommunicator().sendNormalServerMessage("It is being taken care of by " + info.getName() + ".");
                    }
                    performer.getCommunicator().sendNormalServerMessage(StringUtilities.raiseFirstLetter(cret.getStatus().getBodyType()));
                    if (cret.isDomestic() && System.currentTimeMillis() - cret.getLastGroomed() > 172800000L) {
                        performer.getCommunicator().sendNormalServerMessage("This creature could use some grooming.");
                    }
                    if (cret.hasTraits()) {
                        try {
                            Skill breeding = performer.getSkills().getSkill(10085);
                            double knowl = performer.getPower() > 0 ? 99.99 : breeding.getKnowledge(0.0);
                            if (knowl > 20.0) {
                                StringBuilder buf = new StringBuilder();
                                for (int x = 0; x < 64; ++x) {
                                    String l;
                                    if (!cret.hasTrait(x) || !(knowl - 20.0 > (double)x) || (l = Traits.getTraitString(x)).length() <= 0) continue;
                                    buf.append(l);
                                    buf.append(' ');
                                }
                                if (buf.toString().length() > 0) {
                                    performer.getCommunicator().sendNormalServerMessage(buf.toString());
                                }
                            }
                        }
                        catch (NoSuchSkillException breeding) {
                            // empty catch block
                        }
                    }
                    if (cret.isPregnant()) {
                        Offspring o = cret.getOffspring();
                        Random rand = new Random(cret.getWurmId());
                        int left = o.getDaysLeft() + rand.nextInt(3);
                        performer.getCommunicator().sendNormalServerMessage(LoginHandler.raiseFirstLetter(cret.getHeSheItString()) + " will deliver in about " + left + (left != 1 ? " days." : " day."));
                    }
                    String motherfather = "";
                    if (cret.getMother() != -10L) {
                        try {
                            Creature mother = Server.getInstance().getCreature(cret.getMother());
                            motherfather = motherfather + StringUtilities.raiseFirstLetter(cret.getHisHerItsString()) + " mother is " + mother.getNameWithGenus() + ". ";
                        }
                        catch (NoSuchPlayerException | NoSuchCreatureException mother) {
                            // empty catch block
                        }
                    }
                    if (cret.getFather() != -10L) {
                        try {
                            Creature father = Server.getInstance().getCreature(cret.getFather());
                            motherfather = motherfather + StringUtilities.raiseFirstLetter(cret.getHisHerItsString()) + " father is " + father.getNameWithGenus() + ". ";
                        }
                        catch (NoSuchPlayerException | NoSuchCreatureException wurmServerException) {
                            // empty catch block
                        }
                    }
                    if (motherfather.length() > 0) {
                        performer.getCommunicator().sendNormalServerMessage(motherfather);
                    }
                    if (cret.getStatus().getBody().getWounds() != null) {
                        performer.getCommunicator().sendNormalServerMessage("This creature seems to be injured.");
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("This creature seems healthy without any noticeable ailments.");
                    }
                    if (!cret.isHorse()) continue;
                    performer.getCommunicator().sendNormalServerMessage("Its colour is " + cret.getColourName() + ".");
                }
            } else if (this.getTemplateId() == 92 && this.isCooked()) {
                animal = this.getMaterial() == 2 ? "animal" : Item.getMaterialString(this.getMaterial());
                toReturn = "Cooked meat that originally came from some kind of " + animal + ".";
                gotDesc = true;
            } else if (this.getTemplateId() == 368 && this.isCooked()) {
                animal = this.getMaterial() == 2 ? "animal" : Item.getMaterialString(this.getMaterial());
                toReturn = "Cooked fillet of meat that originally came from some kind of " + animal + ".";
                gotDesc = true;
            } else if (this.getTemplate().getFoodGroup() == 1201 && this.isSteamed()) {
                toReturn = "Steamed " + this.template.getName() + " with all its flavours sealed in.";
                gotDesc = true;
            } else if (this.getTemplateId() == 369 && this.isSteamed()) {
                ItemTemplate it = this.getRealTemplate();
                String fish = it == null ? "fish fillet" : it.getName() + " fillet";
                toReturn = "Steamed " + fish + " with all its flavours sealed in.";
                gotDesc = true;
            } else if (this.getTemplate().getFoodGroup() == 1156 && this.isSteamed()) {
                toReturn = "Steamed " + this.template.getName() + " with all its flavours sealed in.";
                gotDesc = true;
            }
        }
        if (!gotDesc && this.template.descIsExam && this.description.length() > 0) {
            toReturn = this.description;
        }
        if (this.template.templateId == 386) {
            try {
                toReturn = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDescriptionLong();
            }
            catch (NoSuchTemplateException nst) {
                Item.logInfo("No template for " + this.getName() + ", id=" + this.realTemplate);
            }
        } else if (this.material == 0) {
            if (this.realTemplate > 0 && this.template.templateId != 1307) {
                try {
                    toReturn = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDescriptionLong();
                }
                catch (NoSuchTemplateException nst) {
                    Item.logInfo("No template for " + this.getName() + ", id=" + this.realTemplate);
                }
            }
        } else if (this.getTemplateId() == 861) {
            if (Servers.localServer.PVPSERVER) {
                toReturn = toReturn + " Anyone may take the stuff inside but at least you can lock it.";
                toReturn = toReturn + " You may tie a creature to it but that does not keep it safe.";
            } else {
                toReturn = toReturn + " Nobody may take the things inside even if it is unlocked.";
                toReturn = toReturn + " You may tie a creature to it and nobody will be able to steal it.";
            }
            toReturn = toReturn + " If left on the ground, an undamaged tent will decay within a few weeks.";
        } else if (this.id == 636451406482434L) {
            toReturn = "A simple but beautiful shirt made from the finest threads of cloth. It's as black as night, except the yellow emblem which contains the symbol of a bat.";
        } else if (this.id == 1810562858091778L) {
            toReturn = "A long and slender pen. This pen is mightier than a sword.";
        } else if (this.id == 357896990754562L) {
            toReturn = "A relic of the days of old, with a tear of Libila stained on the blade.";
        } else if (this.id == 156637289513474L) {
            toReturn = "An exquisite wave pattern glimmers down the blade of this famous katana.";
        } else if (this.id == 2207297627489538L) {
            toReturn = "A well-worn shovel that trumps them all.";
        } else if (this.id == 3343742719231234L) {
            toReturn = "For some reason, your feel tears come to your eyes as you look upon it. Or is it blood?";
        } else if (this.isUnstableRift()) {
            toReturn = toReturn + " It is unstable and will disappear in about " + Server.getTimeFor(Math.max(0L, 1482227988600L - System.currentTimeMillis())) + ".";
        }
        if (this.getTemplateId() == 1307) {
            if (this.getData1() <= 0) {
                toReturn = this.getAuxData() < 65 ? toReturn + " A chisel" : toReturn + " A metal brush";
                toReturn = toReturn + " would be useful to clear away some dirt and rock from it.";
            } else if (this.getRealTemplate() != null) {
                toReturn = "A small fragment from " + this.getRealTemplate().getNameWithGenus() + ". You think you could recreate the " + this.getRealTemplate().getName() + " if you had a bit more material.";
            }
        }
        if (this.getRarity() > 0) {
            toReturn = toReturn + MethodsItems.getRarityDesc(this.getRarity());
        }
        if (this.isInventory()) {
            int itemCount = this.getItemCount();
            toReturn = this.template.getDescriptionLong() + " Your inventory is ";
            toReturn = itemCount <= 25 ? toReturn + this.template.getDescriptionRotten() + "." : (itemCount <= 50 ? toReturn + this.template.getDescriptionBad() + "." : (itemCount <= 75 ? toReturn + this.template.getDescriptionNormal() + "." : (itemCount >= 100 ? toReturn + "full." : toReturn + this.template.getDescriptionSuperb() + ".")));
            toReturn = itemCount != 1 ? toReturn + " It contains " + itemCount + " items." : toReturn + " It contains " + itemCount + " item.";
            return toReturn;
        }
        if (this.getTemplateId() == 387) {
            try {
                ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(this.getData1());
                toReturn = temp.getDescriptionLong() + " It looks strangely crooked.";
            }
            catch (NoSuchTemplateException nst) {
                toReturn = toReturn + " It looks strangely crooked.";
                Item.logWarn(this.getData1() + ": " + nst.getMessage(), nst);
            }
        } else if (this.getTemplateId() != 74 && this.getTemplateId() != 37) {
            String tempString = "";
            switch (this.getTemperatureState(this.temperature)) {
                case -1: {
                    tempString = " It is frozen.";
                    break;
                }
                case 2: {
                    tempString = " It is hot.";
                    break;
                }
                case 1: {
                    tempString = " It is very warm.";
                    break;
                }
                case 4: {
                    tempString = " It is searing hot.";
                    break;
                }
                case 3: {
                    tempString = " It is boiling.";
                    break;
                }
                case 5: {
                    tempString = " It is glowing from the heat.";
                    break;
                }
                case 0: {
                    break;
                }
            }
            toReturn = toReturn + tempString;
        }
        if (this.template.templateId == 1272 || this.template.templateId == 748) {
            switch (this.getAuxData()) {
                case 0: {
                    InscriptionData ins = this.getInscription();
                    if (ins != null && ins.hasBeenInscribed()) {
                        toReturn = toReturn + " It has a message on it!";
                        break;
                    }
                    toReturn = toReturn + " It is blank.";
                    break;
                }
                case 1: {
                    toReturn = toReturn + " It has a recipe on it!";
                    break;
                }
                case 2: {
                    toReturn = toReturn + " It has a waxed coating!";
                }
            }
        }
        if (this.getTemplateId() == 481) {
            String healString = this.auxbyte <= 0 ? " It is useless to heal with." : (this.auxbyte < 5 ? " It will help some against wounds." : (this.auxbyte < 10 ? " It will be pretty efficient against wounds." : (this.auxbyte < 15 ? " It will be good against wounds." : (this.auxbyte < 20 ? " It will be very good against wounds." : " It is supreme against wounds."))));
            toReturn = toReturn + healString;
        }
        if (this.isBowUnstringed() || this.isWeaponBow()) {
            if (this.getMaterial() == 40) {
                toReturn = toReturn + " The willow wood used in this bow gives it good strength yet supreme flexibility.";
            }
        } else if (this.getTemplateId() == 455 || this.getTemplateId() == 454 || this.getTemplateId() == 456) {
            if (this.getMaterial() == 39) {
                toReturn = toReturn + " Cedar arrows are straighter and smoother than other arrows.";
            } else if (this.getMaterial() == 41) {
                toReturn = toReturn + " Maple arrows are smooth, uniform and take less damage than other arrows.";
            }
        } else if (this.getTemplateId() == 526) {
            toReturn = toReturn + " It has " + this.getAuxData() + " charges left.";
        } else if (this.isAbility()) {
            toReturn = this.getTemplateId() == 794 ? toReturn + " If solved, something very dramatic will happen." : (this.getAuxData() == 2 ? toReturn + " It has one charge left." : toReturn + " It has " + (3 - this.getAuxData()) + " charges left.");
        } else if (this.getTemplateId() == 726) {
            if (this.auxbyte != 0 && (k = Kingdoms.getKingdom(this.auxbyte)) != null) {
                toReturn = toReturn + " This is where the people of " + k.getName() + " meet and resolve disputes.";
                King king = King.getKing(this.auxbyte);
                if (king != null) {
                    long nc;
                    String sa;
                    if (king.getChallengeAcceptedDate() > 0L) {
                        long nca = king.getChallengeAcceptedDate();
                        sa = Server.getTimeFor(nca - System.currentTimeMillis());
                        toReturn = toReturn + " The ruler must show up in " + sa + ".";
                    }
                    if (king.getChallengeDate() > 0L) {
                        long nca = king.getChallengeDate();
                        sa = Server.getTimeFor(System.currentTimeMillis() - nca);
                        toReturn = toReturn + " The ruler was challenged " + sa + " ago.";
                    }
                    if ((nc = king.getNextChallenge()) > System.currentTimeMillis()) {
                        String s = Server.getTimeFor(nc - System.currentTimeMillis());
                        toReturn = toReturn + " Next challenge avail in " + s + ".";
                    }
                    if (king.hasFailedAllChallenges()) {
                        toReturn = toReturn + " The " + king.getRulerTitle() + " has failed all challenges. Voting for removal is in progress.";
                        toReturn = ((Player)performer).getSaveFile().votedKing ? toReturn + " You have already voted." : toReturn + " You may now vote for removal of the current ruler.";
                    }
                    if (performer.getPower() > 0) {
                        performer.getLogger().log(Level.INFO, performer.getName() + " examining " + k.getName() + " duel ring.");
                        toReturn = toReturn + " Challenges: " + king.getChallengeSize() + " Declined: " + king.getDeclinedChallengesNumber() + " Votes: " + king.getVotes() + ".";
                    }
                } else {
                    toReturn = toReturn + " There is no ruler.";
                }
            }
        } else if (this.getTemplateId() == 740) {
            toReturn = toReturn + " It has the head of a ";
            switch (this.getAuxData() % 10) {
                case 0: {
                    toReturn = toReturn + "dog";
                    break;
                }
                case 1: {
                    toReturn = toReturn + "pheasant";
                    break;
                }
                case 2: {
                    toReturn = toReturn + "stag";
                    break;
                }
                case 3: {
                    toReturn = toReturn + "bull";
                    break;
                }
                case 4: {
                    toReturn = toReturn + "dragon";
                    break;
                }
                case 5: {
                    toReturn = toReturn + "nymph";
                    break;
                }
                case 6: {
                    toReturn = toReturn + "two-headed giant";
                    break;
                }
                case 7: {
                    toReturn = toReturn + "bear";
                    break;
                }
                case 8: {
                    toReturn = toReturn + "demon";
                    break;
                }
                case 9: {
                    toReturn = toReturn + "rabbit";
                    break;
                }
                default: {
                    toReturn = toReturn + "dog";
                }
            }
            toReturn = toReturn + " on it.";
        } else if (this.getTemplateId() == 1076) {
            if (this.getData1() > 0) {
                toReturn = toReturn + " It has a";
                switch (this.getData1()) {
                    case 1: {
                        if (this.getData2() > 50) {
                            toReturn = toReturn + " star sapphire inserted in the socket.";
                            break;
                        }
                        toReturn = toReturn + " sapphire inserted in the socket.";
                        break;
                    }
                    case 2: {
                        if (this.getData2() > 50) {
                            toReturn = toReturn + " star emerald inserted in the socket.";
                            break;
                        }
                        toReturn = toReturn + "n emerald inserted in the socket.";
                        break;
                    }
                    case 3: {
                        if (this.getData2() > 50) {
                            toReturn = toReturn + " star ruby inserted in the socket.";
                            break;
                        }
                        toReturn = toReturn + " ruby inserted in the socket.";
                        break;
                    }
                    case 4: {
                        if (this.getData2() > 50) {
                            toReturn = toReturn + " black opal inserted in the socket.";
                            break;
                        }
                        toReturn = toReturn + "n opal inserted in the socket.";
                        break;
                    }
                    case 5: {
                        toReturn = this.getData2() > 50 ? toReturn + " star diamond inserted in the socket." : toReturn + " diamond inserted in the socket.";
                    }
                }
            } else {
                toReturn = toReturn + " You could add a gem in the empty socket.";
            }
        } else if (this.getTemplateId() == 1077 && this.getData1() > 0) {
            toReturn = toReturn + " It will improve skillgain for " + SkillSystem.getNameFor(this.getData1()) + ".";
        }
        if (!(this.getColor() == -1 || this.isDragonArmour() && this.getColor2() != -1)) {
            toReturn = toReturn + " ";
            if (this.isWood()) {
                toReturn = toReturn + "Wood ";
                toReturn = toReturn + MethodsItems.getColorDesc(this.getColor()).toLowerCase();
            } else {
                toReturn = toReturn + MethodsItems.getColorDesc(this.getColor());
            }
        }
        if (this.supportsSecondryColor() && this.getColor2() != -1) {
            toReturn = toReturn + " ";
            if (this.isDragonArmour()) {
                toReturn = toReturn + MethodsItems.getColorDesc(this.getColor2());
            } else {
                toReturn = toReturn + LoginHandler.raiseFirstLetter(this.getSecondryItemName());
                toReturn = toReturn + MethodsItems.getColorDesc(this.getColor2()).toLowerCase();
            }
        }
        if (this.lockid != -10L && !this.isKey()) {
            try {
                Item lock = Items.getItem(this.lockid);
                toReturn = lock.isLocked() ? toReturn + " It is locked with a lock of " + lock.getLockStrength() + " quality." : toReturn + " It has a lock of " + lock.getLockStrength() + " quality, which is unlocked.";
            }
            catch (NoSuchItemException nsi) {
                Item.logWarn(this.id + " has a lock that can't be found: " + this.lockid, nsi);
            }
        }
        if (this.getBless() != null && performer.getFaith() > 20.0f) {
            toReturn = performer.getFaith() < 30.0f ? toReturn + " It has an interesting aura." : (performer.getFaith() < 40.0f ? (this.getBless().isHateGod() ? toReturn + " It has a malevolent aura." : toReturn + " It has a benevolent aura.") : toReturn + " It bears an aura of " + this.getBless().name + ".");
        }
        if (this.isWood() && !this.isSeedling()) {
            toReturn = toReturn + " It is made from " + Item.getMaterialString(this.getMaterial()) + ".";
        }
        if (this.isRoyal() && (k = Kingdoms.getKingdom(this.getKingdom())) != null) {
            toReturn = toReturn + " It belongs to the " + k.getName() + ".";
        }
        if (this.getTemplate().isRune()) {
            toReturn = RuneUtilities.isEnchantRune(this) ? toReturn + " It can be attached to " + RuneUtilities.getAttachmentTargets(this) + " and will " + RuneUtilities.getRuneLongDesc(RuneUtilities.getEnchantForRune(this)) + "." : (RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(this), RuneUtilities.ModifierEffect.SINGLE_COLOR) > 0.0f || RuneUtilities.getSpellForRune(this) != null && RuneUtilities.getSpellForRune(this).isTargetAnyItem() && !RuneUtilities.getSpellForRune(this).isTargetTile() ? toReturn + " It can be used on " + RuneUtilities.getAttachmentTargets(this) + " and will " + RuneUtilities.getRuneLongDesc(RuneUtilities.getEnchantForRune(this)) + "." : toReturn + " It will " + RuneUtilities.getRuneLongDesc(RuneUtilities.getEnchantForRune(this)) + ".");
        }
        if (this.getTemplateId() == 1423 && this.getData() != -1L && this.getAuxData() != 0) {
            DeadVillage dv = Villages.getDeadVillage(this.getData());
            toReturn = toReturn + dv.getDeedName();
            if (this.getAuxBit(1)) {
                toReturn = toReturn + " was founded by " + dv.getFounderName();
                toReturn = this.getAuxBit(3) ? toReturn + " and was inhabited for about " + DeadVillage.getTimeString(dv.getTotalAge(), false) + "." : toReturn + ".";
            } else if (this.getAuxBit(3)) {
                toReturn = toReturn + " was inhabited for about " + DeadVillage.getTimeString(dv.getTotalAge(), false) + ".";
            }
            if (this.getAuxBit(2)) {
                if (this.getAuxBit(1) || this.getAuxBit(3)) {
                    toReturn = toReturn + " It";
                }
                toReturn = toReturn + " has been abandoned for roughly " + DeadVillage.getTimeString(dv.getTimeSinceDisband(), false);
                toReturn = this.getAuxBit(0) ? toReturn + " and was last mayored by " + dv.getMayorName() + "." : toReturn + ".";
            } else {
                if (this.getAuxBit(1) || this.getAuxBit(3)) {
                    toReturn = toReturn + " It";
                }
                toReturn = toReturn + " was last mayored by " + dv.getMayorName() + ".";
            }
        }
        if (!this.isNewbieItem() && !this.isChallengeNewbieItem() && this.getTemplateId() != 1310) {
            toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
        }
        if (this.isArtifact()) {
            toReturn = toReturn + " It may drop on the ground if you log out.";
        }
        if (this.getTemplateId() == 937 || this.getTemplateId() == 445) {
            toReturn = toReturn + " It has been " + (this.getTemplateId() == 937 ? "weighted " : "winched ") + this.getWinches() + " times and currently has a firing angle of about " + (45 + this.getAuxData() * 5) + " degrees.";
        }
        if ((this.isDecoration() || this.isNoTake()) && this.ownerId == -10L && this.getTemplateId() != 1310) {
            toReturn = toReturn + " Ql: " + this.qualityLevel + ", Dam: " + this.damage + ".";
        }
        if (this.getTemplateId() == 866) {
            try {
                toReturn = toReturn + " This came from the " + CreatureTemplateFactory.getInstance().getTemplate(this.getData2()).getName() + ".";
            }
            catch (NoSuchCreatureTemplateException e) {
                logger.warning(String.format("Item %s [id %s] does not have valid blood data.", this.getName(), this.getWurmId()));
            }
        }
        if (this.isGem() && this.getData1() > 0) {
            int d = this.getData1();
            toReturn = d < 10 ? toReturn + " It emits faint power." : (d < 20 ? toReturn + " It emits some power." : (d < 50 ? toReturn + " It emits power." : (d < 100 ? toReturn + " It emits quite a lot of power." : (d < 150 ? toReturn + " It emits very much power." : toReturn + " It emits huge amounts of power."))));
        }
        if (this.isArtifact()) {
            toReturn = toReturn + " It ";
            int powerPercent = (int)Math.floor((float)this.auxbyte * 1.0f / 30.0f * 100.0f);
            if (!ArtifactBehaviour.mayUseItem(this, null)) {
                toReturn = toReturn + "seems dormant but ";
            }
            toReturn = powerPercent > 99 ? toReturn + "emits an enormous sense of power." : (powerPercent > 82 ? toReturn + "emits a huge sense of power." : (powerPercent > 65 ? toReturn + "emits a strong sense of power." : (powerPercent > 48 ? toReturn + "emits a fair sense of power." : (powerPercent > 31 ? toReturn + "emits some sense of power." : (powerPercent > 14 ? toReturn + "emits a weak sense of power." : toReturn + "emits almost no sense of power.")))));
            if (this.auxbyte <= 20) {
                toReturn = this.auxbyte > 10 ? toReturn + " It will need to be recharged at the huge altar eventually." : (this.auxbyte <= 10 && this.auxbyte > 0 ? toReturn + " It will need to be recharged at the huge altar soon." : toReturn + " It will need to be recharged at the huge altar immediately or it will disappear.");
            }
            if (performer.getPower() > 0) {
                toReturn = toReturn + " " + this.auxbyte + " charges remain. (" + powerPercent + "%)";
            }
        }
        if (this.getTemplateId() == 538) {
            if (King.getKing((byte)2) == null) {
                toReturn = toReturn + " It is occupied by a sword.";
            }
        } else if (this.getTemplateId() == 654) {
            boolean needBless = this.getBless() == null;
            switch (this.getAuxData()) {
                case 1: {
                    toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a sand tile to a clay tile.";
                    break;
                }
                case 2: {
                    toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a grass or mycelium tile to a peat tile.";
                    break;
                }
                case 3: {
                    toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a steppe tile to a tar tile.";
                    break;
                }
                case 4: {
                    toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a clay tile to a dirt tile.";
                    break;
                }
                case 5: {
                    toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a peat tile to a dirt tile.";
                    break;
                }
                case 6: {
                    toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a tar tile to a dirt tile.";
                }
            }
        } else if (this.getTemplateId() == 1101) {
            toReturn = this.getAuxData() < 10 ? toReturn + " The bottle has something like " + (10 - this.getAuxData()) + " drinks left." : toReturn + " The bottle is empty.";
        }
        if (this.getTemplateId() == 1162) {
            String growing = "unknown";
            try {
                growing = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getName();
            }
            catch (NoSuchTemplateException nst) {
                Item.logInfo("No template for " + this.getName() + ", id=" + this.realTemplate);
            }
            int age = this.getAuxData() & 0x7F;
            toReturn = age == 0 ? toReturn + " you see bare dirt, maybe its too early to see whats growing." : (age < 5 ? toReturn + " you see some shoots poking through the dirt." : (age < 10 ? toReturn + " you see some shoots of " + growing + "." : (age < 65 ? toReturn + " you see some " + growing + " growing, looks in its prime of life." : (age < 75 ? toReturn + " you see some " + growing + " growing, looks a bit old now." : (age < 95 ? toReturn + " you see some " + growing + " growing, looks ready to be picked." : toReturn + " you see woody " + growing + ", looks like it needs replacing.")))));
        }
        if ((this.getTemplateId() == 490 || this.getTemplateId() == 491) && this.getExtra() != -1L) {
            toReturn = toReturn + " It has a keep net attached to it for storing freshly caught fish.";
        }
        if (this.isMooredBoat()) {
            toReturn = toReturn + " It is moored here.";
        }
        if (this.getTemplateId() == 464) {
            toReturn = this.getData1() > 0 ? toReturn + " You sense it could be fertile." : toReturn + " You sense that it is infertile.";
        }
        if (this.isFood() || this.isLiquid()) {
            float nut = this.getNutritionLevel();
            if (this.isWrapped()) {
                if (this.canBePapyrusWrapped() || this.canBeClothWrapped()) {
                    toReturn = toReturn + " It has been wrapped to reduce decay.";
                    return toReturn;
                }
                if (this.canBeRawWrapped()) {
                    toReturn = toReturn + " It has been wrapped ready to cook in a cooker of some kind.";
                    return toReturn;
                }
            }
            toReturn = (double)nut > 0.9 ? toReturn + " This has a high nutrition value." : ((double)nut > 0.7 ? toReturn + " This has a good nutrition value." : ((double)nut > 0.5 ? toReturn + " This has a medium nutrition value." : ((double)nut > 0.3 ? toReturn + " This has a poor nutrition value." : toReturn + " This has a very low nutrition value.")));
            if (this.isSalted()) {
                toReturn = toReturn + " Tastes like it has some salt in it.";
            }
            if (recipe != null && performer.getSkills().getSkillOrLearn(recipe.getSkillId()).getKnowledge(0.0) > 30.0) {
                String creat = this.getCreatorName();
                toReturn = creat.length() > 0 ? toReturn + " Made by " + creat + " on " + WurmCalendar.getDateFor(this.creationDate) : toReturn + " Created on " + WurmCalendar.getDateFor(this.creationDate);
            }
            if (performer.getPower() > 1 || performer.hasFlag(51)) {
                toReturn = toReturn + " (testers only: Calories:" + this.getCaloriesByWeight() + ", Carbs:" + this.getCarbsByWeight() + ", Fats:" + this.getFatsByWeight() + ", Proteins:" + this.getProteinsByWeight() + ", Bonus:" + (this.getBonus() & 0xFF) + ", Nutrition:" + (int)(nut * 100.0f) + "%" + (recipe != null ? ", Recipe:" + recipe.getName() + " (" + recipe.getRecipeId() + ")" : "") + ", Stages:" + this.getFoodStages() + ", Ingredients:" + this.getFoodIngredients() + ".)";
            }
        }
        if (this.getTemplateId() == 1175 || this.getTemplateId() == 1239) {
            switch (this.getAuxData()) {
                case 0: {
                    toReturn = toReturn + " You cannot hear or see any activity around the hive.";
                    break;
                }
                case 1: {
                    toReturn = toReturn + " You see and hear bees flying in and around the hive.";
                    break;
                }
                case 2: {
                    toReturn = toReturn + " You see and hear there is more than the usual activity in the hive, could be there is a queen about to leave.";
                }
            }
        }
        if (WurmId.getType(this.lastOwner) == 1 && (wagoner = Wagoner.getWagoner(this.lastOwner)) != null) {
            toReturn = toReturn + " This is owned by " + wagoner.getName() + ".";
            if (this.getTemplateId() == 1112) {
                this.setData(-10L);
            }
        }
        return toReturn;
    }

    public final byte getEnchantmentDamageType() {
        if (this.enchantment == 91 || this.enchantment == 90 || this.enchantment == 92) {
            return this.enchantment;
        }
        return 0;
    }

    public final void sendColoredSalveImbue(Communicator comm, String salveName, String damageType, byte color) {
        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
        segments.add(new MulticolorLineSegment("It is imbued with special abilities from a ", 0));
        segments.add(new MulticolorLineSegment(salveName, 16));
        segments.add(new MulticolorLineSegment(" and will deal ", 0));
        segments.add(new MulticolorLineSegment(damageType, color));
        segments.add(new MulticolorLineSegment(" damage.", 0));
        comm.sendColoredMessageEvent(segments);
    }

    public final void sendColoredDemise(Communicator comm, String demiseName, String targetName) {
        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
        segments.add(new MulticolorLineSegment("It is enchanted with ", 0));
        segments.add(new MulticolorLineSegment(demiseName, 16));
        segments.add(new MulticolorLineSegment(" and will be more effective against ", 0));
        segments.add(new MulticolorLineSegment(targetName, 17));
        segments.add(new MulticolorLineSegment(".", 0));
        comm.sendColoredMessageEvent(segments);
    }

    public final void sendColoredSmear(Communicator comm, SpellEffect eff) {
        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
        segments.add(new MulticolorLineSegment("It has been smeared with a ", 0));
        segments.add(new MulticolorLineSegment(eff.getName(), 16));
        segments.add(new MulticolorLineSegment(", and it ", 0));
        segments.add(new MulticolorLineSegment(eff.getLongDesc(), 17));
        segments.add(new MulticolorLineSegment(" [", 0));
        segments.add(new MulticolorLineSegment(String.format("%d", (int)eff.getPower()), 18));
        segments.add(new MulticolorLineSegment("]", 0));
        comm.sendColoredMessageEvent(segments);
    }

    public final void sendColoredRune(Communicator comm, SpellEffect eff) {
        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
        segments.add(new MulticolorLineSegment("A ", 0));
        segments.add(new MulticolorLineSegment(eff.getName(), 16));
        segments.add(new MulticolorLineSegment(" has been attached, so it ", 0));
        segments.add(new MulticolorLineSegment(eff.getLongDesc(), 17));
        comm.sendColoredMessageEvent(segments);
    }

    public final void sendColoredEnchant(Communicator comm, SpellEffect eff) {
        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
        segments.add(new MulticolorLineSegment(eff.getName(), 16));
        segments.add(new MulticolorLineSegment(" has been cast on it, so it ", 0));
        segments.add(new MulticolorLineSegment(eff.getLongDesc(), 17));
        segments.add(new MulticolorLineSegment(" [", 0));
        segments.add(new MulticolorLineSegment(String.format("%d", (int)eff.getPower()), 18));
        segments.add(new MulticolorLineSegment("]", 0));
        comm.sendColoredMessageEvent(segments);
    }

    public final void sendEnchantmentStrings(Communicator comm) {
        ItemSpellEffects eff;
        if (this.enchantment != 0) {
            if (this.enchantment == 91) {
                this.sendColoredSalveImbue(comm, "salve of fire", "fire", (byte)20);
            } else if (this.enchantment == 92) {
                this.sendColoredSalveImbue(comm, "salve of frost", "frost", (byte)21);
            } else if (this.enchantment == 90) {
                this.sendColoredSalveImbue(comm, "potion of acid", "acid", (byte)19);
            } else {
                Spell ench = Spells.getEnchantment(this.enchantment);
                if (ench != null) {
                    if (ench == Spells.SPELL_DEMISE_ANIMAL) {
                        this.sendColoredDemise(comm, ench.getName(), "animals");
                    } else if (ench == Spells.SPELL_DEMISE_LEGENDARY) {
                        this.sendColoredDemise(comm, ench.getName(), "legendary creatures");
                    } else if (ench == Spells.SPELL_DEMISE_MONSTER) {
                        this.sendColoredDemise(comm, ench.getName(), "monsters");
                    } else if (ench == Spells.SPELL_DEMISE_HUMAN) {
                        this.sendColoredDemise(comm, ench.getName(), "humans");
                    } else {
                        comm.sendNormalServerMessage("It is enchanted with " + ench.name + ", and " + ench.effectdesc);
                    }
                }
            }
        }
        if ((eff = this.getSpellEffects()) != null) {
            SpellEffect[] speffs;
            for (SpellEffect speff : speffs = eff.getEffects()) {
                if (speff.isSmeared()) {
                    this.sendColoredSmear(comm, speff);
                    continue;
                }
                if ((long)speff.type < -10L) {
                    this.sendColoredRune(comm, speff);
                    continue;
                }
                this.sendColoredEnchant(comm, speff);
            }
        }
    }

    public final void sendExtraStrings(Communicator comm) {
        if (Features.Feature.TOWER_CHAINING.isEnabled() && (this.getTemplateId() == 236 || this.isKingdomMarker())) {
            if (this.isChained()) {
                comm.sendNormalServerMessage(String.format("The %s is chained to the kingdom influence.", this.getName()));
            } else {
                comm.sendNormalServerMessage(String.format("The %s is not chained to the kingdom influence.", this.getName()));
            }
        }
    }

    public final float getSpellEffectPower(byte aEnchantment) {
        SpellEffect skillgain;
        ItemSpellEffects eff = this.getSpellEffects();
        if (eff != null && (skillgain = eff.getSpellEffect(aEnchantment)) != null) {
            return skillgain.getPower();
        }
        return 0.0f;
    }

    public final float getSkillSpellImprovement(int skillNum) {
        switch (skillNum) {
            case 1014: {
                return this.getSpellEffectPower((byte)78);
            }
            case 1008: {
                return this.getSpellEffectPower((byte)79);
            }
            case 1016: 
            case 10010: 
            case 10011: {
                return this.getSpellEffectPower((byte)77);
            }
            case 10016: {
                return this.getSpellEffectPower((byte)80);
            }
            case 10012: 
            case 10013: 
            case 10014: {
                return this.getSpellEffectPower((byte)81);
            }
            case 1031: 
            case 1032: {
                return this.getSpellEffectPower((byte)82);
            }
            case 10015: {
                return this.getSpellEffectPower((byte)83);
            }
            case 10017: {
                return this.getSpellEffectPower((byte)84);
            }
            case 10082: {
                return this.getSpellEffectPower((byte)85);
            }
            case 10074: {
                return this.getSpellEffectPower((byte)86);
            }
            case 1013: {
                return this.getSpellEffectPower((byte)87);
            }
            case 1007: {
                return this.getSpellEffectPower((byte)88);
            }
            case 1005: 
            case 10044: {
                return this.getSpellEffectPower((byte)89);
            }
            case 10059: {
                return this.getSpellEffectPower((byte)99);
            }
        }
        return 0.0f;
    }

    public final String getExamineAsBml(Creature performer) {
        ItemSpellEffects eff;
        Spell ench;
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"" + this.examine(performer) + "\"};");
        if (this.enchantment != 0 && (ench = Spells.getEnchantment(this.enchantment)) != null) {
            buf.append("text{text=\"It is enchanted with " + ench.name + ", and " + ench.effectdesc + "\"};");
        }
        if ((eff = this.getSpellEffects()) != null) {
            SpellEffect[] speffs;
            for (SpellEffect speff : speffs = eff.getEffects()) {
                buf.append("text{text=\"" + speff.getName() + " has been cast on it, so it " + speff.getLongDesc() + " [" + (int)speff.power + "]\"};");
            }
        }
        return buf.toString();
    }

    public final float getSpellSkillBonus() {
        if (this.isArtifact() && this.isWeapon()) {
            return 99.0f;
        }
        if (this.getBonusForSpellEffect((byte)13) > 0.0f) {
            return this.getBonusForSpellEffect((byte)13);
        }
        return this.getBonusForSpellEffect((byte)47);
    }

    public final float getWeaponSpellDamageBonus() {
        if (this.isArtifact() && this.getTemplateId() == 340) {
            return 99.0f;
        }
        return this.getBonusForSpellEffect((byte)18) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
    }

    public final float getSpellRotModifier() {
        if (this.isArtifact() && this.getTemplateId() == 340) {
            return 99.0f;
        }
        return this.getBonusForSpellEffect((byte)18) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
    }

    public final float getSpellFrostDamageBonus() {
        return this.getBonusForSpellEffect((byte)33) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
    }

    public final float getSpellExtraDamageBonus() {
        return this.getBonusForSpellEffect((byte)45) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
    }

    public final float getSpellEssenceDrainModifier() {
        return this.getBonusForSpellEffect((byte)63);
    }

    public final float getSpellLifeTransferModifier() {
        if (this.isArtifact() && this.getTemplateId() == 337) {
            return 99.0f;
        }
        return this.getBonusForSpellEffect((byte)26);
    }

    public final float getSpellMindStealModifier() {
        return this.getBonusForSpellEffect((byte)31);
    }

    public final float getSpellNimbleness() {
        if (this.isArtifact() && this.isWeapon()) {
            return 99.0f;
        }
        return this.getBonusForSpellEffect((byte)32);
    }

    public final float getSpellDamageBonus() {
        if (this.isArtifact() && this.isWeaponSword()) {
            return 99.0f;
        }
        return this.getBonusForSpellEffect((byte)14) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
    }

    public final float getSpellVenomBonus() {
        return this.getBonusForSpellEffect((byte)27) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
    }

    public final float getSpellPainShare() {
        if (this.isArtifact() && (this.isShield() || this.isArmour())) {
            return 80.0f;
        }
        return this.getBonusForSpellEffect((byte)17);
    }

    public final float getNolocateBonus() {
        if (this.isArtifact() && this.getTemplateId() == 329) {
            return 99.0f;
        }
        return this.getBonusForSpellEffect((byte)29);
    }

    public final float getSpellSlowdown() {
        if (this.isArtifact() && this.isShield() || this.isRoyal() && this.isArmour()) {
            return 99.0f;
        }
        return this.getBonusForSpellEffect((byte)46);
    }

    public final float getSpellFoodBonus() {
        return this.getBonusForSpellEffect((byte)15);
    }

    public final float getSpellLocFishBonus() {
        return this.getBonusForSpellEffect((byte)48);
    }

    public final float getSpellLocEnemyBonus() {
        return this.getBonusForSpellEffect((byte)50);
    }

    public final float getSpellLocChampBonus() {
        return this.getBonusForSpellEffect((byte)49);
    }

    public final boolean isLocateItem() {
        return this.getBonusForSpellEffect((byte)50) > 0.0f || this.getBonusForSpellEffect((byte)48) > 0.0f || this.getBonusForSpellEffect((byte)49) > 0.0f;
    }

    public final float getSpellSpeedBonus() {
        if (this.getBonusForSpellEffect((byte)16) > 0.0f) {
            return this.getBonusForSpellEffect((byte)16);
        }
        return this.getBonusForSpellEffect((byte)47);
    }

    public final float getSpellCourierBonus() {
        if (this.getBonusForSpellEffect((byte)20) <= 0.0f) {
            return this.getBonusForSpellEffect((byte)44);
        }
        return this.getBonusForSpellEffect((byte)20);
    }

    public final float getSpellDarkMessengerBonus() {
        return this.getBonusForSpellEffect((byte)44);
    }

    public final float getBonusForSpellEffect(byte aEnchantment) {
        SpellEffect skillgain;
        ItemSpellEffects eff = this.getSpellEffects();
        if (eff != null && (skillgain = eff.getSpellEffect(aEnchantment)) != null) {
            return skillgain.power;
        }
        return 0.0f;
    }

    public final SpellEffect getSpellEffect(byte aEnchantment) {
        SpellEffect skillgain;
        ItemSpellEffects eff = this.getSpellEffects();
        if (eff != null && (skillgain = eff.getSpellEffect(aEnchantment)) != null) {
            return skillgain;
        }
        return null;
    }

    public final int getDamagePercent() {
        if (this.getWeaponSpellDamageBonus() > 0.0f) {
            return this.template.getDamagePercent() + (int)(5.0f * this.getWeaponSpellDamageBonus() / 100.0f);
        }
        return this.template.getDamagePercent();
    }

    public final int getFullWeight() {
        return this.getFullWeight(false);
    }

    public final int getFullWeight(boolean calcCorrectBulkWeight) {
        int lWeight = this.getWeightGrams();
        if (calcCorrectBulkWeight && this.isBulkItem()) {
            float nums = this.getBulkNumsFloat(true);
            ItemTemplate temp = this.getRealTemplate();
            if (temp != null) {
                lWeight = (int)((float)temp.getWeightGrams() * nums);
            }
        }
        if (this.isHollow()) {
            Set<Item> allItems = this.getItems();
            for (Item it : allItems) {
                if (it != this) {
                    lWeight += it.getFullWeight(calcCorrectBulkWeight);
                    continue;
                }
                Item.logWarn(this.getName() + " Wurmid=" + this.getWurmId() + " contains itself!");
            }
        }
        return lWeight;
    }

    public final byte[] getBodySpaces() {
        return this.template.getBodySpaces();
    }

    public final long[] getKeyIds() {
        if (this.keys == null || this.keys.isEmpty()) {
            return EMPTY_LONG_PRIMITIVE_ARRAY;
        }
        long[] keyids = new long[this.keys.size()];
        int x = 0;
        for (Long key : this.keys) {
            keyids[x] = key;
            ++x;
        }
        return keyids;
    }

    public final void addKey(long keyid) {
        if (this.keys == null) {
            this.keys = new HashSet<Long>();
        }
        if (!this.keys.contains(keyid)) {
            this.keys.add(keyid);
            this.addNewKey(keyid);
        }
    }

    public final void removeKey(long keyid) {
        if (this.keys == null) {
            return;
        }
        if (!this.keys.contains(keyid)) {
            return;
        }
        this.keys.remove(keyid);
        this.removeNewKey(keyid);
    }

    public final boolean isUnlockedBy(long keyId) {
        if (this.keys != null && keyId != -10L) {
            return this.keys.contains(keyId);
        }
        return false;
    }

    public final void lock() {
        this.setLocked(true);
    }

    public final void unlock() {
        this.setLocked(false);
    }

    public boolean isEquipmentSlot() {
        return this.template.isEquipmentSlot();
    }

    public final boolean isLocked() {
        if (this.isKey()) {
            return false;
        }
        if (this.isLock()) {
            return this.getLocked();
        }
        if (!this.isLockable() || this.lockid == -10L) {
            return false;
        }
        try {
            boolean isAffectedLock;
            Item lock = Items.getItem(this.lockid);
            boolean bl = isAffectedLock = lock.getTemplateId() == 568 || lock.getTemplateId() == 194 || lock.getTemplateId() == 193;
            if (!lock.isLocked() && isAffectedLock) {
                Item.logInfo(this.getName() + "(" + this.getWurmId() + ") had lock (" + lock.getWurmId() + ") that was unlocked. So was auto-locked as should not have been in that state.");
                lock.setLocked(true);
            }
            return lock.isLocked();
        }
        catch (NoSuchItemException e) {
            Item.logWarn(this.getName() + "," + this.getWurmId() + ":" + e.getMessage(), e);
            return false;
        }
    }

    public boolean isServerPortal() {
        return this.template.isServerPortal;
    }

    public final boolean isColor() {
        return this.template.isColor;
    }

    public final boolean templateIsColorable() {
        return this.template.colorable;
    }

    public final boolean isPuppet() {
        return this.template.puppet;
    }

    public final boolean isOverrideNonEnchantable() {
        return this.template.overrideNonEnchantable;
    }

    public final int getDragonColor() {
        int creatureTemplate = this.getData2();
        switch (creatureTemplate) {
            case 16: 
            case 103: {
                return WurmColor.createColor(215, 40, 40);
            }
            case 18: 
            case 89: {
                return WurmColor.createColor(10, 10, 10);
            }
            case 17: 
            case 90: {
                return WurmColor.createColor(10, 210, 10);
            }
            case 19: 
            case 92: {
                return WurmColor.createColor(255, 255, 255);
            }
            case 91: 
            case 104: {
                return WurmColor.createColor(40, 40, 215);
            }
        }
        return WurmColor.createColor(100, 100, 100);
    }

    public final String getDragonColorNameByColor(int color) {
        if (color == WurmColor.createColor(215, 40, 40)) {
            return "red";
        }
        if (color == WurmColor.createColor(10, 10, 10)) {
            return "black";
        }
        if (color == WurmColor.createColor(10, 210, 10)) {
            return "green";
        }
        if (color == WurmColor.createColor(255, 255, 255)) {
            return "white";
        }
        if (color == WurmColor.createColor(40, 40, 215)) {
            return "blue";
        }
        return "";
    }

    public final String getDragonColorName() {
        try {
            CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(this.getData2());
            String pre = temp.getName();
            StringTokenizer st = new StringTokenizer(pre);
            pre = st.nextToken();
            return pre.toLowerCase();
        }
        catch (Exception exception) {
            return "";
        }
    }

    public final String getLockStrength() {
        String lockStrength = "fantastic";
        float lQualityLevel = this.getCurrentQualityLevel();
        int qlDivTen = (int)lQualityLevel / 10;
        switch (qlDivTen) {
            case 0: {
                lockStrength = "very poor";
                break;
            }
            case 1: {
                lockStrength = "poor";
                break;
            }
            case 2: {
                lockStrength = "below average";
                break;
            }
            case 3: {
                lockStrength = "okay";
                break;
            }
            case 4: {
                lockStrength = "above average";
                break;
            }
            case 5: {
                lockStrength = "pretty good";
                break;
            }
            case 6: {
                lockStrength = "good";
                break;
            }
            case 7: {
                lockStrength = "very good";
                break;
            }
            case 8: {
                lockStrength = "exceptional";
                break;
            }
            default: {
                lockStrength = "fantastic";
            }
        }
        return lockStrength;
    }

    public final void setSizes(int aSizeX, int aSizeY, int aSizeZ) {
        if (aSizeX == 0 || aSizeY == 0 || aSizeZ == 0) {
            Items.destroyItem(this.id);
            return;
        }
        this.setSizeX(aSizeX);
        this.setSizeY(aSizeY);
        this.setSizeZ(aSizeZ);
        this.sendStatus();
    }

    final boolean depleteSizeWith(int aSizeX, int aSizeY, int aSizeZ) {
        int prevSizeX = this.getSizeX();
        int prevSizeY = this.getSizeY();
        int prevSizeZ = this.getSizeZ();
        int prevol = prevSizeX * prevSizeY * prevSizeZ;
        int vol = aSizeX * aSizeY * aSizeZ;
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("id: " + this.id + ", vol: " + vol + " prevol: " + prevol + ' ' + prevSizeX + ' ' + prevSizeY + ' ' + prevSizeZ);
        }
        if (vol >= prevol) {
            Items.destroyItem(this.id);
            return true;
        }
        float factor = (float)vol / (float)prevol;
        prevSizeX = Math.max(1, prevSizeX - (int)((float)prevSizeX * factor));
        prevSizeY = Math.max(1, prevSizeY - (int)((float)prevSizeY * factor));
        int newPrevSz = prevSizeZ = Math.max(1, prevSizeZ - (int)((float)prevSizeZ * factor));
        int newPrevSy = prevSizeY;
        int newPrevSx = prevSizeX;
        if (prevSizeZ < prevSizeY) {
            newPrevSz = prevSizeY;
            newPrevSy = prevSizeZ;
        }
        if (prevSizeZ < prevSizeX) {
            newPrevSx = prevSizeZ;
            newPrevSy = prevSizeX;
        }
        if (prevSizeY < prevSizeX) {
            newPrevSy = prevSizeY;
            newPrevSz = prevSizeX;
        }
        this.setSizeX(newPrevSx);
        this.setSizeY(newPrevSy);
        this.setSizeZ(newPrevSz);
        this.sendStatus();
        return false;
    }

    public final void sleep(Creature sleeper, boolean epicServer) throws IOException {
        if (this.template.artifact && sleeper != null) {
            if (this.getOwnerId() == sleeper.getWurmId()) {
                sleeper.dropItem(this);
            }
            return;
        }
        if (this.isHollow()) {
            Item[] allItems;
            for (Item allit : allItems = this.getAllItems(true, false)) {
                if (allit.hasDroppableItem(epicServer)) {
                    allit.sleep(sleeper, epicServer);
                    continue;
                }
                allit.sleepNonRecursive(sleeper, epicServer);
            }
        }
        if (!this.template.alwaysLoaded) {
            Items.removeItem(this.id);
        }
    }

    public final boolean hasDroppableItem(boolean epicServer) {
        if (!this.isHollow()) {
            return false;
        }
        for (Item i : this.items) {
            if (!i.isArtifact()) continue;
            return true;
        }
        return false;
    }

    public final void sleepNonRecursive(Creature sleeper, boolean epicServer) throws IOException {
        if (this.template.artifact && sleeper != null) {
            if (this.getOwnerId() == sleeper.getWurmId()) {
                sleeper.dropItem(this);
            }
            return;
        }
        if (!this.template.alwaysLoaded) {
            Items.removeItem(this.id);
        }
    }

    public final Item[] getAllItems(boolean getLockedItems) {
        return this.getAllItems(getLockedItems, true);
    }

    @Nonnull
    public final Item[] getAllItems(boolean getLockedItems, boolean loadArtifacts) {
        if (!this.isHollow()) {
            return emptyItems;
        }
        if (this.lockid != -10L && !getLockedItems) {
            return emptyItems;
        }
        if (!loadArtifacts && this.template.artifact) {
            return emptyItems;
        }
        HashSet<Item> allItems = new HashSet<Item>();
        for (Item item : this.getItems()) {
            allItems.add(item);
            Collections.addAll(allItems, item.getAllItems(getLockedItems, loadArtifacts));
        }
        return allItems.toArray(new Item[allItems.size()]);
    }

    public final Item findFirstContainedItem(int templateid) {
        Item[] its = this.getAllItems(false);
        for (int x = 0; x < its.length; ++x) {
            if (its[x].getTemplateId() != templateid) continue;
            return its[x];
        }
        return null;
    }

    @Nullable
    public final Item findItem(int templateid) {
        for (Item item : this.getItems()) {
            if (item.getTemplateId() != templateid) continue;
            return item;
        }
        return null;
    }

    public final Item findItem(int templateid, boolean searchInGroups) {
        for (Item item : this.getItems()) {
            Item inGroup;
            if (item.getTemplateId() == templateid) {
                return item;
            }
            if (!item.isInventoryGroup() || !searchInGroups || (inGroup = item.findItem(templateid, false)) == null) continue;
            return inGroup;
        }
        return null;
    }

    public final float getCurrentQualityLevel() {
        return this.qualityLevel * Math.max(1.0f, 100.0f - this.damage) / 100.0f;
    }

    public final byte getRadius() {
        float modifier;
        if (this.getSpellEffects() != null && (modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_GLOW) - 1.0f) > 0.0f) {
            return (byte)Math.min(127.0f, 127.0f * modifier - 20.0f);
        }
        float ql = this.getCurrentQualityLevel();
        boolean qlAbove20 = ql > 20.0f;
        float baseRange = ql / (qlAbove20 ? 100.0f : 20.0f);
        if (this.isLightBright()) {
            return (byte)(qlAbove20 ? baseRange * 127.0f : (baseRange - 1.0f) * 32.0f);
        }
        return (byte)(qlAbove20 ? baseRange * 64.0f : (baseRange - 1.0f) * 64.0f);
    }

    public final boolean isCrystal() {
        return this.material == 52 || this.isDiamond();
    }

    public final boolean isDiamond() {
        return this.material == 54;
    }

    private float getMaterialDaleModifier() {
        switch (this.getMaterial()) {
            case 64: {
                return 1.1f;
            }
        }
        return 1.0f;
    }

    private float getMaterialDamageModifier() {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (this.getMaterial()) {
                case 56: {
                    return 0.4f;
                }
                case 30: {
                    return 0.95f;
                }
                case 31: {
                    return 0.9f;
                }
                case 10: {
                    return 1.15f;
                }
                case 57: {
                    return 0.6f;
                }
                case 7: {
                    return 1.2f;
                }
                case 12: {
                    return 1.3f;
                }
                case 67: {
                    return 0.5f;
                }
                case 8: {
                    return 1.025f;
                }
                case 9: {
                    return 0.8f;
                }
                case 34: {
                    return 1.2f;
                }
                case 13: {
                    return 1.25f;
                }
                case 96: {
                    return 0.9f;
                }
                case 38: {
                    return 0.8f;
                }
                case 35: {
                    return 0.2f;
                }
            }
        } else {
            if (this.getMaterial() == 9) {
                return 0.8f;
            }
            if (this.getMaterial() == 57) {
                return 0.6f;
            }
            if (this.getMaterial() == 56) {
                return 0.4f;
            }
        }
        if (this.isFishingLine()) {
            switch (this.getTemplateId()) {
                case 1347: {
                    return 1.2f;
                }
                case 1348: {
                    return 1.0f;
                }
                case 1349: {
                    return 0.9f;
                }
                case 1350: {
                    return 0.8f;
                }
                case 1351: {
                    return 0.7f;
                }
            }
        }
        return 1.0f;
    }

    private float getMaterialDecayModifier() {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (this.getMaterial()) {
                case 56: {
                    return 0.4f;
                }
                case 30: {
                    return 0.95f;
                }
                case 31: {
                    return 0.85f;
                }
                case 10: {
                    return 0.95f;
                }
                case 57: {
                    return 0.6f;
                }
                case 7: {
                    return 0.4f;
                }
                case 12: {
                    return 0.8f;
                }
                case 67: {
                    return 0.5f;
                }
                case 8: {
                    return 0.7f;
                }
                case 9: {
                    return 0.7f;
                }
                case 34: {
                    return 0.925f;
                }
                case 13: {
                    return 1.2f;
                }
                case 96: {
                    return 0.8f;
                }
                case 38: {
                    return 0.8f;
                }
                case 35: {
                    return 0.9f;
                }
            }
        } else {
            if (this.getMaterial() == 9) {
                return 0.8f;
            }
            if (this.getMaterial() == 57) {
                return 0.6f;
            }
            if (this.getMaterial() == 56) {
                return 0.4f;
            }
        }
        return 1.0f;
    }

    private float getMaterialFlexibiltyModifier() {
        switch (this.getMaterial()) {
            case 40: {
                return 0.7f;
            }
        }
        return 1.0f;
    }

    private float getMaterialDecayTimeModifier() {
        switch (this.getMaterial()) {
            case 39: {
                return 1.5f;
            }
        }
        return 1.0f;
    }

    public int getMaterialBowDifficulty() {
        switch (this.getMaterial()) {
            case 40: {
                return 5;
            }
        }
        return 0;
    }

    public int getMaterialArrowDifficulty() {
        switch (this.getMaterial()) {
            case 39: {
                return 5;
            }
            case 41: {
                return 3;
            }
        }
        return 0;
    }

    public float getMaterialArrowDamageModifier() {
        switch (this.getMaterial()) {
            case 41: {
                return 0.8f;
            }
        }
        return 1.0f;
    }

    private float getMaterialAgingModifier() {
        switch (this.getMaterial()) {
            case 38: {
                return 1.1f;
            }
        }
        return 1.05f;
    }

    public float getMaterialFragrantModifier() {
        switch (this.getMaterial()) {
            case 39: {
                return 0.9f;
            }
            case 65: {
                return 0.85f;
            }
            case 37: {
                return 0.95f;
            }
            case 43: {
                return 0.75f;
            }
            case 88: {
                return 0.75f;
            }
            case 42: {
                return 0.8f;
            }
            case 51: {
                return 0.8f;
            }
        }
        return 1.0f;
    }

    public final float getDamageModifier() {
        return this.getDamageModifier(false);
    }

    public final float getDamageModifier(boolean decayDamage) {
        return this.getDamageModifier(decayDamage, false);
    }

    public final float getDamageModifier(boolean decayDamage, boolean flexibilityDamage) {
        Item parent;
        float rotMod = 1.0f;
        float materialMod = 1.0f;
        materialMod = decayDamage ? (materialMod *= this.getMaterialDecayModifier()) : (flexibilityDamage ? (materialMod *= this.getMaterialFlexibiltyModifier()) : (materialMod *= this.getMaterialDamageModifier()));
        if (this.getSpellRotModifier() > 0.0f) {
            rotMod += this.getSpellRotModifier() / 100.0f;
        }
        if (this.isCrude()) {
            rotMod *= 10.0f;
        }
        if (this.isCrystal()) {
            rotMod *= 0.1f;
        } else if (this.isFood()) {
            if (this.isHighNutrition()) {
                rotMod += (float)(this.isSalted() ? 5 : 10);
            }
            if (this.isGoodNutrition()) {
                rotMod += (float)(this.isSalted() ? 2 : 5);
            }
            if (this.isMediumNutrition()) {
                rotMod = (float)((double)rotMod + (this.isSalted() ? 1.5 : 3.0));
            }
        }
        if (this.isInTacklebox()) {
            rotMod *= 0.5f;
        }
        if ((parent = this.getParentOrNull()) != null && parent.getTemplateId() == 1342) {
            rotMod *= 0.5f;
        }
        if (this.getRarity() > 0) {
            rotMod = (float)((double)rotMod * Math.pow(0.9, this.getRarity()));
        }
        if (this.getSpellEffects() != null) {
            rotMod *= this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_DECAY);
        }
        return 100.0f * rotMod / Math.max(1.0f, this.qualityLevel * (100.0f - this.damage) / 100.0f) * materialMod;
    }

    public final boolean isTraded() {
        return this.tradeWindow != null;
    }

    public final void setTradeWindow(@Nullable TradingWindow _tradeWindow) {
        this.tradeWindow = _tradeWindow;
    }

    public final TradingWindow getTradeWindow() {
        return this.tradeWindow;
    }

    public final String getTasteString() {
        String toReturn = "royal, noble, and utterly delicious!";
        float ql = this.getCurrentQualityLevel();
        if (ql < 5.0f) {
            toReturn = "rotten, bad, evil and dangerous.";
        } else if (ql < 20.0f) {
            toReturn = "extremely bad.";
        } else if (ql < 30.0f) {
            toReturn = "pretty bad.";
        } else if (ql < 40.0f) {
            toReturn = "okay.";
        } else if (ql < 50.0f) {
            toReturn = "pretty good.";
        } else if (ql < 60.0f) {
            toReturn = "good.";
        } else if (ql < 70.0f) {
            toReturn = "very good.";
        } else if (ql < 80.0f) {
            toReturn = "extremely good.";
        } else if (ql < 90.0f) {
            toReturn = "so good you almost feel like singing.";
        }
        return toReturn;
    }

    public static byte fuelEfficiency(byte material) {
        switch (material) {
            case 14: {
                return 2;
            }
            case 58: {
                return 8;
            }
            case 59: {
                return 4;
            }
        }
        return 1;
    }

    public static String getMaterialString(byte material) {
        return MaterialUtilities.getMaterialString(material);
    }

    public final boolean isHollow() {
        return this.template.isHollow();
    }

    public final boolean isWeaponSlash() {
        return this.template.weaponslash;
    }

    public final boolean isShield() {
        return this.template.shield;
    }

    public final boolean isArmour() {
        return this.template.armour;
    }

    public final boolean isBracelet() {
        return this.template.isBracelet();
    }

    public final boolean isFood() {
        return this.template.isFood();
    }

    public boolean isNamed() {
        if (this.realTemplate > 0 && !this.isDish()) {
            ItemTemplate realTemplate = this.getRealTemplate();
            assert (realTemplate != null);
            return realTemplate.namedCreator;
        }
        return this.template.namedCreator;
    }

    public final boolean isOnePerTile() {
        return this.template.onePerTile;
    }

    public final boolean isFourPerTile() {
        return this.template.fourPerTile;
    }

    public final boolean isTenPerTile() {
        return this.template.tenPerTile;
    }

    public final boolean isTrellis() {
        return this.template.isTrellis();
    }

    final boolean isMagic() {
        return this.template.magic;
    }

    public final boolean isEgg() {
        return this.template.egg;
    }

    public final boolean isFieldTool() {
        return this.template.fieldtool;
    }

    public final boolean isBodyPart() {
        return this.template.bodypart;
    }

    public final boolean isBodyPartAttached() {
        return this.template.bodypart && this.auxbyte != 100;
    }

    public final boolean isBodyPartRemoved() {
        return this.template.bodypart && this.auxbyte == 100;
    }

    public final boolean isInventory() {
        return this.template.inventory;
    }

    public final boolean isInventoryGroup() {
        return this.template.isInventoryGroup();
    }

    public final boolean isDragonArmour() {
        return this.template.isDragonArmour;
    }

    public int getImproveItem() {
        return this.template.getImproveItem();
    }

    public final boolean isMiningtool() {
        return this.template.miningtool;
    }

    public final boolean isWand() {
        return this.getTemplateId() == 315 || this.getTemplateId() == 176;
    }

    final boolean isCompass() {
        return this.template.isCompass;
    }

    final boolean isToolbelt() {
        return this.template.isToolbelt;
    }

    public final boolean isBelt() {
        return this.template.isBelt;
    }

    public final boolean isCarpentryTool() {
        return this.template.carpentrytool;
    }

    final boolean isSmithingTool() {
        return this.template.smithingtool;
    }

    public final boolean isWeaponBow() {
        return this.template.bow;
    }

    public final boolean isArrow() {
        switch (this.getTemplateId()) {
            case 454: 
            case 455: 
            case 456: {
                return true;
            }
        }
        return false;
    }

    public final boolean isBowUnstringed() {
        return this.template.bowUnstringed;
    }

    public final boolean isWeaponPierce() {
        return this.template.weaponpierce;
    }

    public final boolean isWeaponCrush() {
        return this.template.weaponcrush;
    }

    public final boolean isWeaponAxe() {
        return this.template.weaponaxe;
    }

    public final boolean isWeaponSword() {
        return this.template.weaponsword;
    }

    public final boolean isWeaponPolearm() {
        return this.template.weaponPolearm;
    }

    public final boolean isWeaponKnife() {
        return this.template.weaponknife;
    }

    public final boolean isWeaponMisc() {
        return this.template.weaponmisc;
    }

    public final boolean isDiggingtool() {
        return this.template.diggingtool;
    }

    public final boolean isNoTrade() {
        return this.template.notrade;
    }

    public final boolean isSeed() {
        return this.template.seed;
    }

    public final boolean isSeedling() {
        switch (this.getTemplateId()) {
            case 917: 
            case 918: 
            case 1017: {
                return true;
            }
        }
        return false;
    }

    public final boolean isAbility() {
        return this.template.isAbility;
    }

    public final boolean isLiquid() {
        return this.template.liquid;
    }

    public final boolean isDye() {
        switch (this.getTemplateId()) {
            case 431: 
            case 432: 
            case 433: 
            case 434: 
            case 435: 
            case 438: {
                return true;
            }
        }
        return false;
    }

    public final boolean isLightBright() {
        return this.template.brightLight;
    }

    public static byte getRLight(int brightness) {
        return (byte)(255 * brightness / 255);
    }

    public static byte getGLight(int brightness) {
        return (byte)(239 * brightness / 255);
    }

    public static byte getBLight(int brightness) {
        return (byte)(173 * brightness / 255);
    }

    public final long getDecayTime() {
        return this.template.getDecayTime();
    }

    public final boolean isRefreshedOnUse() {
        return this.template.getDecayTime() == 28800L;
    }

    public final boolean isDish() {
        return this.template.isDish;
    }

    public final boolean isMelting() {
        return this.template.melting;
    }

    public final boolean isMeat() {
        return this.template.meat;
    }

    public final boolean isSign() {
        return this.template.sign;
    }

    public final boolean isFence() {
        return this.template.fence;
    }

    public final boolean isVegetable() {
        return this.template.vegetable;
    }

    public final boolean isRoadMarker() {
        return this.template.isRoadMarker();
    }

    public final boolean isPaveable() {
        return this.template.isPaveable();
    }

    public final boolean isCavePaveable() {
        return this.template.isCavePaveable();
    }

    public final boolean containsIngredientsOnly() {
        return this.template.containsIngredientsOnly();
    }

    public final boolean isShelf() {
        return this.template.isShelf();
    }

    public final boolean isComponentItem() {
        return this.template.isComponentItem();
    }

    public final boolean isParentMustBeOnGround() {
        return this.template.isParentMustBeOnGround();
    }

    final boolean isVillageRecruitmentBoard() {
        return this.template.templateId == 835;
    }

    public final boolean isBed() {
        return this.template.bed;
    }

    public final boolean isNewbieItem() {
        return this.template.newbieItem && this.auxbyte > 0;
    }

    public final boolean isMilk() {
        return this.template.isMilk;
    }

    public final boolean isCheese() {
        return this.template.isCheese;
    }

    public final boolean isChallengeNewbieItem() {
        return this.template.challengeNewbieItem && this.auxbyte > 0;
    }

    public final boolean isWood() {
        if (this.material == 0) {
            if (this.realTemplate > 0) {
                ItemTemplate realTemplate = this.getRealTemplate();
                assert (realTemplate != null);
                return realTemplate.wood;
            }
            return this.template.wood;
        }
        return Materials.isWood(this.material);
    }

    public final boolean isStone() {
        if (this.material == 0) {
            return this.template.stone;
        }
        return Materials.isStone(this.material);
    }

    public final boolean isCombineCold() {
        return this.template.isCombineCold();
    }

    public final boolean isGem() {
        return this.template.gem;
    }

    final boolean isFlickering() {
        return this.template.flickeringLight;
    }

    public final ItemTemplate getRealTemplate() {
        try {
            return ItemTemplateFactory.getInstance().getTemplate(this.realTemplate);
        }
        catch (NoSuchTemplateException noSuchTemplateException) {
            return null;
        }
    }

    public final boolean isMetal() {
        if (this.material == 0) {
            if (this.realTemplate > 0) {
                ItemTemplate realTemplate = this.getRealTemplate();
                assert (realTemplate != null);
                return realTemplate.isMetal();
            }
            return this.template.isMetal();
        }
        return Materials.isMetal(this.material);
    }

    public final boolean isLeather() {
        if (this.material == 0) {
            if (this.realTemplate > 0) {
                ItemTemplate realTemplate = this.getRealTemplate();
                assert (realTemplate != null);
                return realTemplate.leather;
            }
            return this.template.leather;
        }
        return Materials.isLeather(this.material);
    }

    public final boolean isPaper() {
        if (this.material == 0) {
            if (this.realTemplate > 0) {
                ItemTemplate realTemplate = this.getRealTemplate();
                assert (realTemplate != null);
                return realTemplate.paper;
            }
            return this.template.paper;
        }
        return Materials.isPaper(this.material);
    }

    public final boolean isCloth() {
        if (this.material == 0) {
            if (this.realTemplate > 0) {
                ItemTemplate realTemplate = this.getRealTemplate();
                assert (realTemplate != null);
                return realTemplate.cloth;
            }
            return this.template.cloth;
        }
        return Materials.isCloth(this.material);
    }

    public final boolean isWool() {
        if (this.material == 0) {
            if (this.realTemplate > 0) {
                ItemTemplate realTemplate = this.getRealTemplate();
                assert (realTemplate != null);
                return realTemplate.getMaterial() == 69;
            }
            return this.template.getMaterial() == 69;
        }
        return this.material == 69;
    }

    public final boolean isPottery() {
        if (this.material == 0) {
            if (this.realTemplate > 0) {
                ItemTemplate realTemplate = this.getRealTemplate();
                assert (realTemplate != null);
                return realTemplate.pottery;
            }
            return this.template.pottery;
        }
        return Materials.isPottery(this.material);
    }

    public final boolean isPlantedFlowerpot() {
        return this.template.isPlantedFlowerpot();
    }

    public final boolean isPotteryFlowerPot() {
        int tempId = this.getTemplateId();
        switch (tempId) {
            case 812: 
            case 813: 
            case 814: 
            case 815: 
            case 816: 
            case 817: 
            case 818: 
            case 819: 
            case 820: {
                return true;
            }
        }
        return false;
    }

    public final boolean isMarblePlanter() {
        int tempId = this.getTemplateId();
        switch (tempId) {
            case 1001: 
            case 1002: 
            case 1003: 
            case 1004: 
            case 1005: 
            case 1006: 
            case 1007: 
            case 1008: {
                return true;
            }
        }
        return false;
    }

    public final boolean isMagicStaff() {
        return this.template.isMagicStaff();
    }

    public final boolean isImproveUsingTypeAsMaterial() {
        return this.template.isImproveUsingTypeAsMaterial();
    }

    public final boolean isLight() {
        return this.template.light || this.isLightOverride;
    }

    public final boolean isContainerLiquid() {
        return this.template.containerliquid;
    }

    public final boolean isLiquidInflammable() {
        return this.template.liquidinflammable;
    }

    public final boolean isHealingSalve() {
        return this.template.getTemplateId() == 650;
    }

    public final boolean isForgeOrOven() {
        return this.template.getTemplateId() == 180 || this.template.getTemplateId() == 178;
    }

    public final boolean isSpawnPoint() {
        return this.template.getTemplateId() == 1016;
    }

    final boolean isWeaponMelee() {
        return this.template.weaponmelee;
    }

    public final boolean isFish() {
        return this.template.fish;
    }

    public final boolean isMailBox() {
        return this.template.templateId >= 510 && this.template.templateId <= 513;
    }

    public final boolean isUnenchantedTurret() {
        return this.template.templateId == 934;
    }

    public final boolean isEnchantedTurret() {
        switch (this.template.templateId) {
            case 940: 
            case 941: 
            case 942: 
            case 968: {
                return true;
            }
        }
        return false;
    }

    public final boolean isMarketStall() {
        return this.template.templateId == 580;
    }

    public final boolean isWeapon() {
        return this.template.weapon;
    }

    public final boolean isTool() {
        return this.template.tool;
    }

    public final boolean isCookingTool() {
        return this.template.isCookingTool();
    }

    public final boolean isLock() {
        return this.template.lock;
    }

    public final boolean templateIndestructible() {
        return this.template.indestructible;
    }

    public final boolean isKey() {
        return this.template.key;
    }

    public final boolean isBulkContainer() {
        return this.template.bulkContainer;
    }

    public final boolean isTopParentPile() {
        Item item = this.getTopParentOrNull();
        if (item == null) {
            return false;
        }
        return item.getTemplateId() == 177;
    }

    public final Item getItemWithTemplateAndMaterial(int stemplateId, int smaterial, byte auxByte, int srealTemplateId) {
        for (Item i : this.getItems()) {
            if (i.getRealTemplateId() != stemplateId || smaterial != i.getMaterial() || i.getAuxData() != auxByte || (srealTemplateId != -10 || i.getData1() != -1) && i.getData1() != srealTemplateId) continue;
            return i;
        }
        return null;
    }

    public final boolean isBulkItem() {
        return this.getTemplateId() == 669;
    }

    public final boolean isBulk() {
        return this.template.bulk;
    }

    public final boolean isFire() {
        return this.template.isFire;
    }

    public final boolean canBeDropped(boolean checkTraded) {
        if (this.isNoDrop()) {
            return false;
        }
        if (checkTraded && this.isTraded()) {
            return false;
        }
        if (this.items == null || !this.isHollow()) {
            return true;
        }
        for (Item item : this.items) {
            if (!item.canBeDropped(true)) {
                return false;
            }
            if (!item.isNoTrade()) continue;
            return false;
        }
        return true;
    }

    public final boolean isWind() {
        return this.template.isWind;
    }

    public final boolean isFlag() {
        return this.template.isFlag;
    }

    public final boolean isRepairable() {
        if (this.permissions.hasPermission(Permissions.Allow.NO_REPAIR.getBit())) {
            return false;
        }
        return this.isRepairableDefault();
    }

    public final boolean isRepairableDefault() {
        if (this.realTemplate > 0 && this.getTemplateId() != 1307) {
            ItemTemplate realTemplate = this.getRealTemplate();
            assert (realTemplate != null);
            return realTemplate.repairable || realTemplate.templateId == 74 || realTemplate.templateId == 480;
        }
        if (this.getTemplateId() == 179) {
            return true;
        }
        return this.template.repairable;
    }

    public final boolean isRoyal() {
        return this.template.isRoyal;
    }

    public final boolean isTemporary() {
        return this.template.temporary;
    }

    public final boolean isCombine() {
        return this.template.combine;
    }

    public final boolean templateIsLockable() {
        return this.template.lockable;
    }

    public final boolean isUnfired() {
        return this.template.isUnfired;
    }

    public final boolean canHaveInscription() {
        return this.template.canHaveInscription();
    }

    public final boolean isAlmanacContainer() {
        return this.template.isAlmanacContainer();
    }

    public final boolean isHarvestReport() {
        if (this.getTemplateId() == 1272 || this.getTemplateId() == 748) {
            return this.getAuxData() > 8;
        }
        return false;
    }

    @Nullable
    public final WurmHarvestables.Harvestable getHarvestable() {
        return WurmHarvestables.getHarvestable(this.getAuxData() - 8);
    }

    public final boolean hasData() {
        return this.template.hasdata;
    }

    public final boolean hasExtraData() {
        return this.template.hasExtraData();
    }

    public final boolean isDraggable() {
        return this.template.draggable && !this.isNoDrag();
    }

    public final boolean isVillageDeed() {
        return this.template.villagedeed;
    }

    public final boolean isTransmutable() {
        return this.template.isTransmutable;
    }

    public final boolean isFarwalkerItem() {
        return this.template.farwalkerItem;
    }

    public final boolean isHomesteadDeed() {
        return this.template.homesteaddeed;
    }

    public final boolean isNoRename() {
        return this.template.norename;
    }

    public final boolean isNoNutrition() {
        return this.template.isNoNutrition();
    }

    public final boolean isLowNutrition() {
        return this.template.isLowNutrition();
    }

    public final boolean isMediumNutrition() {
        return this.template.isMediumNutrition();
    }

    public final boolean isHighNutrition() {
        return this.template.isHighNutrition();
    }

    public final boolean isGoodNutrition() {
        return this.template.isGoodNutrition();
    }

    public final boolean isFoodMaker() {
        return this.template.isFoodMaker();
    }

    public final boolean canLarder() {
        return this.template.canLarder();
    }

    public final boolean templateAlwaysLit() {
        return this.template.alwaysLit;
    }

    public final boolean isEpicTargetItem() {
        if (this.realTemplate > 0) {
            ItemTemplate realTemplate = this.getRealTemplate();
            assert (realTemplate != null);
            return realTemplate.isEpicTargetItem;
        }
        return this.template.isEpicTargetItem;
    }

    private final boolean checkPlantedPermissions(Creature creature) {
        VolaTile vt = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.surfaced);
        if (vt != null) {
            Structure structure = vt.getStructure();
            if (structure != null && structure.isTypeHouse()) {
                return structure.isActionAllowed(creature, (short)685);
            }
            Village village = vt.getVillage();
            if (village != null) {
                return village.isActionAllowed((short)685, creature);
            }
        }
        return false;
    }

    public final boolean isTurnable(@Nonnull Creature turner) {
        if (this.getParentId() != -10L && (this.getParentOrNull() != this.getTopParentOrNull() || !this.getParentOrNull().getTemplate().hasViewableSubItems() || this.getParentOrNull().getTemplate().isContainerWithSubItems() && !this.isPlacedOnParent())) {
            return false;
        }
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (this.isTurnable() && !this.isPlanted()) {
            return true;
        }
        if (turner.getPower() >= 2) {
            return true;
        }
        if (this.isPlanted() && this.checkPlantedPermissions(turner)) {
            return true;
        }
        return (this.isOwnerTurnable() || this.isPlanted()) && this.lastOwner == turner.getWurmId();
    }

    public final boolean isMoveable(@Nonnull Creature mover) {
        if (this.getParentId() != -10L) {
            return false;
        }
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (this.isEpicTargetItem() && EpicServerStatus.getRitualMissionForTarget(this.getWurmId()) != null) {
            return false;
        }
        if (!this.isNoMove() && !this.isPlanted()) {
            return true;
        }
        if (mover.getPower() >= 2) {
            return true;
        }
        if (this.isPlanted() && this.checkPlantedPermissions(mover)) {
            return true;
        }
        return (this.isOwnerMoveable() || this.isPlanted()) && this.lastOwner == mover.getWurmId();
    }

    public final boolean isGuardTower() {
        return this.template.isGuardTower();
    }

    public final boolean isHerb() {
        return this.template.herb;
    }

    public final boolean isSpice() {
        return this.template.spice;
    }

    final boolean isFruit() {
        return this.template.fruit;
    }

    public final boolean templateIsNoMove() {
        return this.template.isNoMove;
    }

    final boolean isPoison() {
        return this.template.poison;
    }

    public final boolean isOutsideOnly() {
        return this.template.outsideonly;
    }

    public final boolean isInsideOnly() {
        return this.template.insideOnly;
    }

    public final boolean isCoin() {
        return this.template.coin;
    }

    public final int getRentCost() {
        switch (this.auxbyte) {
            case 0: {
                return 0;
            }
            case 1: {
                return 100;
            }
            case 2: {
                return 1000;
            }
            case 3: {
                return 10000;
            }
            case 4: {
                return 100000;
            }
            case 5: {
                return 10;
            }
            case 6: {
                return 25;
            }
            case 7: {
                return 50;
            }
        }
        return 0;
    }

    public final boolean isDecoration() {
        if (this.realTemplate > 0 && this.getTemplateId() != 1162 && !this.isPlanted() && this.getTemplateId() != 1307) {
            ItemTemplate realTemplate = this.getRealTemplate();
            assert (realTemplate != null);
            VolaTile v = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            if (v != null && v.getNumberOfDecorations(this.getFloorLevel()) >= 15) {
                return false;
            }
            return realTemplate.decoration;
        }
        if (this.template.decoration) {
            return true;
        }
        return this.template.decorationWhenPlanted && this.isPlanted();
    }

    public final boolean isBag() {
        return this.template.isBag;
    }

    public final boolean isQuiver() {
        return this.template.isQuiver;
    }

    public final boolean isFullprice() {
        return this.template.fullprice;
    }

    public final boolean isNoSellback() {
        return this.template.isNoSellBack;
    }

    public final boolean isBarding() {
        return this.template.isBarding();
    }

    public final boolean isRope() {
        return this.template.isRope();
    }

    public final boolean isAlwaysPoll() {
        return this.template.alwayspoll;
    }

    public final boolean isProtectionTower() {
        return this.template.protectionTower;
    }

    public final boolean isFloating() {
        if (this.template.templateId == 1396 && this.isPlanted()) {
            return true;
        }
        return this.template.floating;
    }

    final boolean isButcheredItem() {
        return this.template.isButcheredItem;
    }

    public final boolean isNoWorkParent() {
        return this.template.noWorkParent;
    }

    public final boolean isNoBank() {
        return this.template.nobank;
    }

    public final boolean isAlwaysBankable() {
        return this.template.alwaysBankable;
    }

    public final boolean isLeadCreature() {
        return this.template.isLeadCreature;
    }

    public final boolean isLeadMultipleCreatures() {
        return this.template.isLeadMultipleCreatures;
    }

    public final boolean descIsExam() {
        return this.template.descIsExam;
    }

    public final int getPrice() {
        return this.price;
    }

    public final boolean isDomainItem() {
        return this.template.domainItem;
    }

    public final boolean isCrude() {
        return this.template.isCrude();
    }

    public final boolean isMinable() {
        return this.template.minable;
    }

    public final boolean isEnchantableJewelry() {
        return this.template.isEnchantableJewelry;
    }

    public final boolean isUseOnGroundOnly() {
        if (this.realTemplate > 0 && this.getTemplateId() != 1307) {
            ItemTemplate realTemplate = this.getRealTemplate();
            assert (realTemplate != null);
            return realTemplate.useOnGroundOnly;
        }
        return this.template.useOnGroundOnly;
    }

    public final boolean isHugeAltar() {
        return this.template.hugeAltar;
    }

    public final boolean isDestroyHugeAltar() {
        return this.template.destroysHugeAltar;
    }

    public final boolean isArtifact() {
        return this.template.artifact;
    }

    public final boolean isUnique() {
        return this.template.unique;
    }

    public final boolean isTwoHanded() {
        return this.template.isTwohanded;
    }

    public final boolean isServerBound() {
        return this.template.isServerBound;
    }

    public final boolean isKingdomMarker() {
        return this.template.kingdomMarker;
    }

    public final boolean isDestroyable(long destroyerId) {
        block6: {
            if (this.template.destroyable) {
                return true;
            }
            if (!this.template.ownerDestroyable) {
                return false;
            }
            long lockId = this.getLockId();
            if (lockId == -10L) {
                return this.lastOwner == destroyerId;
            }
            try {
                Item lock = Items.getItem(lockId);
                if (lock.lastOwner == destroyerId) {
                    return true;
                }
            }
            catch (NoSuchItemException nsi) {
                if (this.lastOwner != destroyerId) break block6;
                return true;
            }
        }
        return false;
    }

    public final boolean isDrinkable() {
        return this.template.drinkable;
    }

    public final boolean isVehicle() {
        return this.template.isVehicle();
    }

    public final boolean isChair() {
        return this.template.isChair;
    }

    public final boolean isCart() {
        return this.template.isCart;
    }

    public final boolean isWagonerWagon() {
        return this.wagonerWagon;
    }

    public final void setWagonerWagon(boolean isWagonerWagon) {
        this.wagonerWagon = isWagonerWagon;
    }

    public final boolean isBoat() {
        return this.template.isVehicle() && this.template.isFloating();
    }

    public final boolean isMooredBoat() {
        return this.isBoat() && this.getData() != -1L;
    }

    public final boolean isRechargeable() {
        return this.template.isRechargeable();
    }

    public final boolean isMineDoor() {
        return this.template.isMineDoor;
    }

    public final boolean isOwnerDestroyable() {
        return this.template.ownerDestroyable;
    }

    public final boolean isHitchTarget() {
        return this.template.isHitchTarget();
    }

    public final boolean isRiftAltar() {
        return this.template.isRiftAltar();
    }

    public final boolean isRiftItem() {
        return this.template.isRiftItem();
    }

    public final boolean isRiftLoot() {
        return this.template.isRiftLoot();
    }

    public final boolean hasItemBonus() {
        return this.template.isHasItemBonus();
    }

    final boolean isPriceEffectedByMaterial() {
        return this.template.priceAffectedByMaterial;
    }

    public final boolean isDeathProtection() {
        return this.template.isDeathProtection;
    }

    public final boolean isInPvPZone() {
        return Zones.isOnPvPServer(this.getTilePos());
    }

    public final void getContainedItems() {
        if (!this.isHollow() && !this.isBodyPart()) {
            return;
        }
        Set<Item> set = Items.getContainedItems(this.id);
        if (set == null) {
            return;
        }
        for (Item item : set) {
            if (item.getOwnerId() != this.ownerId) {
                Item.logWarn(item.getName() + " at " + ((int)item.getPosX() >> 2) + ", " + ((int)item.getPosY() >> 2) + " with id " + item.getWurmId() + " doesn't have the same owner as " + this.getName() + " with id " + this.id + ". Deleting.");
                Items.decay(item.getWurmId(), item.getDbStrings());
                continue;
            }
            this.addItem(item, true);
        }
    }

    public final boolean isHolyItem() {
        if (!this.template.holyItem) {
            return false;
        }
        switch (this.getMaterial()) {
            case 7: 
            case 8: {
                return true;
            }
            case 67: 
            case 96: {
                if (!Features.Feature.METALLIC_ITEMS.isEnabled()) break;
                return true;
            }
        }
        return false;
    }

    public final boolean isHolyItem(Deity deity) {
        return deity != null && deity.holyItem == this.getTemplateId();
    }

    final boolean isPassFullData() {
        return this.template.passFullData;
    }

    public final boolean isMeditation() {
        return this.template.isMeditation;
    }

    public final boolean isTileAligned() {
        if (this.realTemplate > 0) {
            ItemTemplate realTemplate = this.getRealTemplate();
            assert (realTemplate != null);
            return realTemplate.isTileAligned;
        }
        return this.template.isTileAligned;
    }

    public final boolean isNewDeed() {
        return this.getTemplateId() == 663;
    }

    public final boolean isOldDeed() {
        return (this.isVillageDeed() || this.isHomesteadDeed()) && !this.isNewDeed();
    }

    final boolean isLiquidCooking() {
        return this.template.isLiquidCooking();
    }

    final boolean isForm() {
        return this.template.isForm;
    }

    public final boolean isFlower() {
        return this.template.isFlower;
    }

    public final boolean isNaturePlantable() {
        return this.template.isNaturePlantable;
    }

    public final boolean isBanked() {
        if (this.banked && this.parentId != -10L && !Banks.isItemBanked(this.getWurmId())) {
            logger.warning("Bugged item showing as banked: " + this.toString());
            this.setBanked(false);
        }
        return this.banked;
    }

    public final boolean isAltar() {
        return this.template.isAltar;
    }

    public final Deity getBless() {
        Deity deity;
        if (this.bless > 0 && (deity = Deities.getDeity(this.bless)) != null) {
            return deity;
        }
        return null;
    }

    public final void setData(long aData) {
        if (aData == -10L) {
            this.setData(-1, -1);
        } else {
            this.setData((int)(aData >> 32), (int)aData);
        }
    }

    public final long getData() {
        int data1 = this.getData1();
        int data2 = this.getData2();
        if (data1 == -1 || data2 == -1) {
            return -1L;
        }
        return ((long)data2 & 0xFFFFFFFFL) + BigInteger.valueOf((long)data1 & 0xFFFFFFFFL).shiftLeft(32).longValue();
    }

    public final void setExtra(long aExtra) {
        if (aExtra == -10L) {
            this.setExtra(-1, -1);
        } else {
            this.setExtra((int)(aExtra >> 32), (int)aExtra);
        }
    }

    public final long getExtra() {
        int extra1 = this.getExtra1();
        int extra2 = this.getExtra2();
        if (extra1 == -1 || extra2 == -1) {
            return -1L;
        }
        return ((long)extra2 & 0xFFFFFFFFL) + BigInteger.valueOf((long)extra1 & 0xFFFFFFFFL).shiftLeft(32).longValue();
    }

    public final void setDataXY(int aTileX, int aTileY) {
        this.setData1(aTileX << 16 | aTileY);
    }

    public final short getDataX() {
        int data1 = this.getData1();
        if (data1 == -1) {
            return -1;
        }
        return (short)(data1 >> 16 & 0xFFFF);
    }

    public final short getDataY() {
        int data1 = this.getData1();
        if (data1 == -1) {
            return -1;
        }
        return (short)(this.getData1() & 0xFFFF);
    }

    public final void setSizes(int aWeight) {
        ItemTemplate lTemplate = this.getTemplate();
        int sizeX = lTemplate.getSizeX();
        int sizeY = lTemplate.getSizeY();
        int sizeZ = lTemplate.getSizeZ();
        float mod = (float)aWeight / (float)lTemplate.getWeightGrams();
        if (mod > 64.0f) {
            this.setSizeZ(sizeZ * 4);
            this.setSizeY(sizeY * 4);
            this.setSizeX(sizeX * 4);
        } else if (mod > 16.0f) {
            this.setSizeZ(sizeZ * 4);
            this.setSizeY(sizeY * 4);
            mod = mod / 4.0f * 4.0f;
            this.setSizeX((int)((float)sizeX * mod));
        } else if (mod > 4.0f) {
            this.setSizeZ(sizeZ * 4);
            this.setSizeY((int)((float)sizeY * (mod /= 4.0f)));
            this.setSizeX(sizeX);
        } else {
            this.setSizes(Math.max(1, (int)((float)sizeX * mod)), Math.max(1, (int)((float)sizeY * mod)), Math.max(1, (int)((float)sizeZ * mod)));
        }
    }

    public final ItemSpellEffects getSpellEffects() {
        return ItemSpellEffects.getSpellEffects(this.id);
    }

    public final float getDamageModifierForItem(Item item) {
        float mod = 0.0f;
        if (this.isStone()) {
            if (item.getTemplateId() == 20) {
                mod = 0.007f;
            } else if (item.isWeaponCrush()) {
                mod = 0.003f;
            } else if (item.isWeaponAxe()) {
                mod = 0.0015f;
            } else if (item.isWeaponSlash()) {
                mod = 0.001f;
            } else if (item.isWeaponPierce()) {
                mod = 0.001f;
            } else if (item.isWeaponMisc()) {
                mod = 0.001f;
            }
        } else if (this.getMaterial() == 38) {
            if (item.isWeaponAxe()) {
                mod = 0.007f;
            } else if (item.isWeaponCrush()) {
                mod = 0.003f;
            } else if (item.isWeaponSlash()) {
                mod = 0.005f;
            } else if (item.isWeaponPierce()) {
                mod = 0.002f;
            } else if (item.isWeaponMisc()) {
                mod = 0.001f;
            }
        } else if (this.isWood() || this.isCloth() || this.isFood()) {
            if (item.isWeaponAxe()) {
                mod = 0.003f;
            } else if (item.isWeaponCrush()) {
                mod = 0.002f;
            } else if (item.isWeaponSlash()) {
                mod = 0.0015f;
            } else if (item.isWeaponPierce()) {
                mod = 0.001f;
            } else if (item.isWeaponMisc()) {
                mod = 7.0E-4f;
            }
        } else if (this.isMetal()) {
            if (item.isWeaponAxe()) {
                mod = 0.001f;
            } else if (item.isWeaponCrush()) {
                mod = 0.0015f;
            } else if (item.isWeaponSlash()) {
                mod = 0.001f;
            } else if (item.isWeaponPierce()) {
                mod = 5.0E-4f;
            } else if (item.isWeaponMisc()) {
                mod = 0.001f;
            }
        } else if (item.isWeaponAxe()) {
            mod = 0.001f;
        } else if (item.isWeaponCrush()) {
            mod = 0.0015f;
        } else if (item.isWeaponSlash()) {
            mod = 0.001f;
        } else if (item.isWeaponPierce()) {
            mod = 5.0E-4f;
        } else if (item.isWeaponMisc()) {
            mod = 0.001f;
        }
        if (this.isTent()) {
            mod *= 50.0f;
        }
        return mod;
    }

    public final Vector2f getPos2f() {
        if (this.parentId == -10L && !this.isBodyPartAttached() && !this.isInventory()) {
            return new Vector2f(this.posX, this.posY);
        }
        if (this.ownerId != -10L) {
            try {
                Creature creature = Server.getInstance().getCreature(this.ownerId);
                return creature.getPos2f();
            }
            catch (NoSuchCreatureException e) {
                if (!Items.isItemLoaded(this.parentId)) {
                    return new Vector2f(this.posX, this.posY);
                }
                try {
                    Item parent = Items.getItem(this.parentId);
                    return parent.getPos2f();
                }
                catch (NoSuchItemException e1) {
                    Item.logWarn("This REALLY shouldn't happen!", e1);
                }
            }
            catch (NoSuchPlayerException ignored) {
                if (!Items.isItemLoaded(this.parentId)) {
                    return new Vector2f(this.posX, this.posY);
                }
                try {
                    Item parent = Items.getItem(this.parentId);
                    return parent.getPos2f();
                }
                catch (NoSuchItemException e) {
                    Item.logWarn("This REALLY shouldn't happen!", e);
                }
            }
        }
        if (Items.isItemLoaded(this.parentId)) {
            try {
                Item parent = Items.getItem(this.parentId);
                return parent.getPos2f();
            }
            catch (NoSuchItemException nsi2) {
                Item.logWarn("This REALLY shouldn't happen!", nsi2);
            }
        }
        return new Vector2f(this.posX, this.posY);
    }

    @Nonnull
    public final Vector3f getPos3f() {
        if (this.parentId == -10L && !this.isBodyPartAttached() && !this.isInventory()) {
            return new Vector3f(this.posX, this.posY, this.posZ);
        }
        if (this.ownerId != -10L) {
            try {
                Creature creature = Server.getInstance().getCreature(this.ownerId);
                return creature.getPos3f();
            }
            catch (NoSuchCreatureException nsc) {
                if (!Items.isItemLoaded(this.parentId)) {
                    return new Vector3f(this.posX, this.posY, this.posZ);
                }
                try {
                    Item parent = Items.getItem(this.parentId);
                    return parent.getPos3f();
                }
                catch (NoSuchItemException nsi) {
                    Item.logWarn("This REALLY shouldn't happen!", nsi);
                }
            }
            catch (NoSuchPlayerException nsp) {
                if (!Items.isItemLoaded(this.parentId)) {
                    return new Vector3f(this.posX, this.posY, this.posZ);
                }
                try {
                    Item parent = Items.getItem(this.parentId);
                    return parent.getPos3f();
                }
                catch (NoSuchItemException nsi) {
                    Item.logWarn("This REALLY shouldn't happen!", nsi);
                }
            }
        }
        if (Items.isItemLoaded(this.parentId)) {
            try {
                Item parent = Items.getItem(this.parentId);
                return parent.getPos3f();
            }
            catch (NoSuchItemException nsi2) {
                Item.logWarn("This REALLY shouldn't happen!", nsi2);
            }
        }
        return new Vector3f(this.posX, this.posY, this.posZ);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public final float getPosX() {
        if (this.parentId == -10L && !this.isBodyPartAttached() && !this.isInventory()) {
            return this.posX;
        }
        if (this.ownerId != -10L) {
            try {
                Creature creature = Server.getInstance().getCreature(this.ownerId);
                return creature.getStatus().getPositionX();
            }
            catch (NoSuchCreatureException nsc) {
                if (!Items.isItemLoaded(this.parentId)) return this.posX;
                try {
                    Item parent = Items.getItem(this.parentId);
                    return parent.getPosX();
                }
                catch (NoSuchItemException nsi) {
                    Item.logWarn("This REALLY shouldn't happen!", nsi);
                    return this.posX;
                }
            }
            catch (NoSuchPlayerException nsp) {
                if (!Items.isItemLoaded(this.parentId)) return this.posX;
                try {
                    Item parent = Items.getItem(this.parentId);
                    return parent.getPosX();
                }
                catch (NoSuchItemException nsi) {
                    Item.logWarn("This REALLY shouldn't happen!", nsi);
                    return this.posX;
                }
            }
        } else {
            if (!Items.isItemLoaded(this.parentId)) return this.posX;
            try {
                Item parent = Items.getItem(this.parentId);
                if (parent.isTemporary()) return this.posX;
                return parent.getPosX();
            }
            catch (NoSuchItemException nsi) {
                Item.logWarn("This REALLY shouldn't happen!", nsi);
            }
        }
        return this.posX;
    }

    public final float getPosXRaw() {
        return this.posX;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public final float getPosY() {
        if (this.parentId == -10L && !this.isBodyPartAttached() && !this.isInventory()) {
            return this.posY;
        }
        if (this.ownerId != -10L) {
            try {
                Creature creature = Server.getInstance().getCreature(this.ownerId);
                return creature.getStatus().getPositionY();
            }
            catch (NoSuchCreatureException nsc) {
                if (!Items.isItemLoaded(this.parentId)) return this.posY;
                try {
                    Item parent = Items.getItem(this.parentId);
                    return parent.getPosY();
                }
                catch (NoSuchItemException nsi) {
                    Item.logWarn("This REALLY shouldn't happen!", nsi);
                    return this.posY;
                }
            }
            catch (NoSuchPlayerException nsp) {
                if (!Items.isItemLoaded(this.parentId)) return this.posY;
                try {
                    Item parent = Items.getItem(this.parentId);
                    return parent.getPosY();
                }
                catch (NoSuchItemException nsi) {
                    Item.logWarn("This REALLY shouldn't happen!", nsi);
                    return this.posY;
                }
            }
        } else {
            if (!Items.isItemLoaded(this.parentId)) return this.posY;
            try {
                Item parent = Items.getItem(this.parentId);
                return parent.getPosY();
            }
            catch (NoSuchItemException nsi) {
                Item.logWarn("This REALLY shouldn't happen!", nsi);
            }
        }
        return this.posY;
    }

    public final float getPosYRaw() {
        return this.posY;
    }

    /*
     * Unable to fully structure code
     */
    public final float getPosZ() {
        if (this.parentId != -10L || this.isBodyPartAttached() || this.isInventory()) {
            if (this.ownerId != -10L) {
                try {
                    creature = Server.getInstance().getCreature(this.ownerId);
                    return creature.getStatus().getPositionZ() + creature.getAltOffZ();
                }
                catch (NoSuchCreatureException nsc) {
                    if (!Items.isItemLoaded(this.parentId)) ** GOTO lbl28
                    try {
                        parent = Items.getItem(this.parentId);
                        return parent.getPosZ();
                    }
                    catch (NoSuchItemException nsi) {
                        Item.logWarn("This REALLY shouldn't happen!", nsi);
                    }
                }
                catch (NoSuchPlayerException nsp) {
                    if (!Items.isItemLoaded(this.parentId)) ** GOTO lbl28
                    try {
                        parent = Items.getItem(this.parentId);
                        return parent.getPosZ();
                    }
                    catch (NoSuchItemException nsi) {
                        Item.logWarn("This REALLY shouldn't happen!", nsi);
                    }
                }
            } else if (Items.isItemLoaded(this.parentId)) {
                try {
                    parent = Items.getItem(this.parentId);
                    return parent.getPosZ();
                }
                catch (NoSuchItemException nsi) {
                    Item.logWarn("This REALLY shouldn't happen!", nsi);
                }
            }
        }
lbl28:
        // 9 sources

        if (this.isFloating()) {
            return Math.max(0.0f, this.posZ);
        }
        return this.posZ;
    }

    public final float getPosZRaw() {
        return this.posZ;
    }

    public final boolean isEdibleBy(Creature creature) {
        if (creature.getTemplate().getTemplateId() == 93) {
            return this.isFish();
        }
        if (this.isDish() || this.isMeat() && (!this.isBodyPart() || this.isBodyPartRemoved()) || this.isFish()) {
            return creature.isCarnivore() || creature.isOmnivore();
        }
        if (this.isSeed() || this.getTemplateId() == 620) {
            return creature.isHerbivore() || creature.isOmnivore();
        }
        if (this.isVegetable()) {
            return creature.isHerbivore() || creature.isOmnivore();
        }
        if (this.isFood()) {
            return creature.isOmnivore();
        }
        return false;
    }

    public final byte getKingdom() {
        if (this.isRoyal() || this.getTemplateId() == 272) {
            return this.getAuxData();
        }
        if (this.isKingdomMarker() || this.isWind() || this.isVehicle() || this.template.isKingdomFlag || this.isDuelRing() || this.isEpicTargetItem() || this.isWarTarget() || this.isTent() || this.template.useMaterialAndKingdom || this.isEnchantedTurret() || this.isProtectionTower()) {
            return this.getAuxData();
        }
        return 0;
    }

    public final boolean isWithin(int startX, int endX, int startY, int endY) {
        return this.getTileX() >= startX && this.getTileX() <= endX && this.getTileY() >= startY && this.getTileY() <= endY;
    }

    public boolean isDuelRing() {
        return this.template.isDuelRing;
    }

    public final long getBridgeId() {
        return this.onBridge;
    }

    public final boolean mayCreatureInsertItem() {
        if (this.isInventoryGroup()) {
            Item parent = this.getParentOrNull();
            return parent != null && parent.mayCreatureInsertItem();
        }
        if (this.isInventory() && this.getNumItemsNotCoins() < 100) {
            return true;
        }
        return this.getItemCount() < 100;
    }

    public final Item getParentOrNull() {
        try {
            return this.getParent();
        }
        catch (NoSuchItemException nsi) {
            return null;
        }
    }

    public final int getNumItemsNotCoins() {
        if (this.items == null) {
            return 0;
        }
        int toReturn = 0;
        for (Item nItem : this.items) {
            if (!nItem.isCoin() && !nItem.isInventoryGroup() && nItem.getTemplateId() != 666) {
                ++toReturn;
                continue;
            }
            if (!nItem.isInventoryGroup()) continue;
            toReturn += nItem.getNumItemsNotCoins();
        }
        return toReturn;
    }

    public int getNumberCages() {
        if (this.items == null) {
            return 0;
        }
        int toReturn = 0;
        for (Item nItem : this.getAllItems(true)) {
            if (nItem.getTemplateId() != 1311) continue;
            ++toReturn;
        }
        return toReturn;
    }

    public final int getFat() {
        return this.getData2() >> 1 & 0xFF;
    }

    public final boolean isHealing() {
        return this.template.healing;
    }

    public final int getAlchemyType() {
        return this.template.alchemyType;
    }

    public final boolean isButchered() {
        return (this.getData2() & 1) == 1;
    }

    public final boolean isMovingItem() {
        return this.template.isMovingItem;
    }

    public final boolean spawnsTrees() {
        return this.template.spawnsTrees;
    }

    public final boolean killsTrees() {
        return this.template.killsTrees;
    }

    public final boolean isNonDeedable() {
        return this.template.nonDeedable;
    }

    public final int getBulkTemplateId() {
        return this.getData1();
    }

    public final int getBulkNums() {
        if (this.isBulkItem()) {
            ItemTemplate itemp = this.getRealTemplate();
            if (itemp != null) {
                return Math.max(1, this.getWeightGrams() / itemp.getVolume());
            }
            return 0;
        }
        return this.getData2();
    }

    public final float getBulkNumsFloat(boolean useMaxOne) {
        if (this.isBulkItem()) {
            ItemTemplate itemp = this.getRealTemplate();
            if (itemp != null) {
                if (useMaxOne) {
                    return Math.max(1.0f, (float)this.getWeightGrams() / (float)itemp.getVolume());
                }
                return (float)this.getWeightGrams() / (float)itemp.getVolume();
            }
            return 0.0f;
        }
        return this.getData2();
    }

    public final int getPlacedItemCount() {
        int itemsCount = 0;
        boolean normalContainer = this.getTemplate().isContainerWithSubItems();
        for (Item item : this.getItems()) {
            if ((!normalContainer || !item.isPlacedOnParent()) && normalContainer) continue;
            ++itemsCount;
        }
        return itemsCount;
    }

    public final int getItemCount() {
        int itemsCount = 0;
        for (Item item : this.getItems()) {
            if (!item.isInventoryGroup()) {
                ++itemsCount;
                continue;
            }
            itemsCount += item.getItemCount();
        }
        return itemsCount;
    }

    public final void setBulkTemplateId(int newid) {
        this.setData1(newid);
    }

    public final void updateName() {
        if (this.getParentId() != -10L) {
            this.sendUpdate();
            return;
        }
        if (this.zoneId <= 0 || this.parentId != -10L) {
            return;
        }
        VolaTile t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
        if (t != null) {
            t.renameItem(this);
        }
    }

    public final void setButchered() {
        this.setData2(1);
        if (this.ownerId != -10L) {
            return;
        }
        if (this.zoneId == -10) {
            return;
        }
        try {
            Zone z = Zones.getZone(this.zoneId);
            z.removeItem(this);
            z.addItem(this);
        }
        catch (NoSuchZoneException nsz) {
            Item.logWarn("Zone at " + ((int)this.getPosX() >> 2) + "," + ((int)this.getPosY() >> 2) + ",surf=" + this.isOnSurface() + " no such zone.");
        }
    }

    final void updateParents() {
        this.sendUpdate();
        try {
            Item parent = this.getParent();
            parent.updateParents();
        }
        catch (NoSuchItemException noSuchItemException) {
            // empty catch block
        }
    }

    public void sendUpdate() {
        if (this.watchers == null) {
            return;
        }
        for (Creature watcher : this.watchers) {
            watcher.getCommunicator().sendUpdateInventoryItem(this);
        }
    }

    public boolean isCreatureWearableOnly() {
        return this.template.isCreatureWearableOnly();
    }

    public final boolean isUnfinished() {
        return this.template.getTemplateId() == 386 || this.template.getTemplateId() == 179;
    }

    public final void updateIfGroundItem() {
        if (this.getParentId() != -10L || this.zoneId == -10) {
            return;
        }
        try {
            Zone z = Zones.getZone(this.zoneId);
            z.removeItem(this);
            z.addItem(this);
        }
        catch (NoSuchZoneException nsz) {
            Item.logWarn("Zone at " + ((int)this.getPosX() >> 2) + "," + ((int)this.getPosY() >> 2) + ",surf=" + this.isOnSurface() + " no such zone. Item: " + this);
        }
    }

    public final void updateModelNameOnGroundItem() {
        if (this.getParentId() != -10L || this.zoneId == -10) {
            return;
        }
        try {
            Zone z = Zones.getZone(this.zoneId);
            z.updateModelName(this);
        }
        catch (NoSuchZoneException nsz) {
            Item.logWarn("Zone at " + ((int)this.getPosX() >> 2) + "," + ((int)this.getPosY() >> 2) + ",surf=" + this.isOnSurface() + " no such zone. Item: " + this);
        }
    }

    public final void updatePos() {
        block8: {
            if (this.getParentId() != -10L) {
                try {
                    Item parent = this.getParent();
                    if (parent.getTemplateId() == 177) {
                        parent.removeFromPile(this);
                        parent.insertIntoPile(this);
                        break block8;
                    }
                    parent.dropItem(this.id, false);
                    parent.insertItem(this, true);
                }
                catch (NoSuchItemException nsi) {
                    Item.logWarn("Item with id " + this.getWurmId() + " has no parent item with id " + this.getParentId(), new Exception());
                }
            } else if (this.zoneId != -10) {
                try {
                    Zone z = Zones.getZone(this.zoneId);
                    z.removeItem(this);
                    z.addItem(this);
                }
                catch (NoSuchZoneException nsz) {
                    Item.logWarn("Zone at " + ((int)this.getPosX() >> 2) + "," + ((int)this.getPosY() >> 2) + ",surf=" + this.isOnSurface() + " no such zone. Item: " + this);
                }
            }
        }
    }

    public final boolean isStreetLamp() {
        return this.template.streetlamp;
    }

    public final boolean isSaddleLarge() {
        return this.template.getTemplateId() == 622;
    }

    public final boolean isSaddleNormal() {
        return this.template.getTemplateId() == 621;
    }

    public final boolean isHorseShoe() {
        return this.template.getTemplateId() == 623;
    }

    public final boolean isBridle() {
        return this.template.getTemplateId() == 624;
    }

    public final void setTempPositions(float posx, float posy, float posz, float rot) {
        this.posX = posx;
        this.posY = posy;
        this.posZ = posz;
        this.rotation = rot;
    }

    public final void setTempXPosition(float posx) {
        this.posX = posx;
    }

    public final void setTempYPosition(float posy) {
        this.posY = posy;
    }

    public final void setTempZandRot(float posz, float rot) {
        this.posZ = posz;
        this.rotation = rot;
    }

    public final byte getLeftAuxData() {
        return (byte)(this.auxbyte >> 4 & 0xF);
    }

    public final byte getRightAuxData() {
        return (byte)(this.auxbyte & 0xF);
    }

    public final boolean isDestroyedOnDecay() {
        return this.template.destroyOnDecay;
    }

    public final boolean isWarTarget() {
        return this.template.isWarTarget;
    }

    public final boolean isVisibleDecay() {
        return this.template.visibleDecay;
    }

    public final boolean isNoDiscard() {
        return this.isCoin() || this.isNewbieItem() || this.isChallengeNewbieItem() || this.isLiquid() || this.template.isNoDiscard() || this.isBodyPart() || this.template.getTemplateId() == 862 || this.template.getValue() > 5000 || this.getValue() > 100 || this.isIndestructible() || this.getSpellEffects() != null || this.enchantment != 0 || this.isMagic() || this.isNoDrop() || this.isInventory() || this.isNoTrade() || this.getRarity() > 0 || this.isHollow() && !this.isEmpty(false);
    }

    public final boolean isInstaDiscard() {
        return this.template.isInstaDiscard();
    }

    public final void setLeftAuxData(int ldata) {
        this.setAuxData((byte)(this.getRightAuxData() + (ldata << 4 & 0xF0)));
    }

    public final void setRightAuxData(int rdata) {
        this.setAuxData((byte)((this.getLeftAuxData() << 4) + (rdata & 0xF)));
    }

    public final boolean isEpicPortal() {
        return this.template.isEpicPortal;
    }

    public final boolean isUnstableRift() {
        return this.template.isUnstableRift();
    }

    public final void setSurfaced(boolean newValue) {
        this.surfaced = newValue;
        if (this.isHollow() && this.items != null) {
            for (Item item : this.getAllItems(true, true)) {
                item.setSurfacedNotRecursive(this.surfaced);
            }
        }
    }

    private final void setSurfacedNotRecursive(boolean newValue) {
        this.surfaced = newValue;
    }

    abstract void create(float var1, long var2) throws IOException;

    abstract void load() throws Exception;

    public abstract void loadEffects();

    public abstract void bless(int var1);

    public abstract void enchant(byte var1);

    abstract void setPlace(short var1);

    public abstract short getPlace();

    public abstract void setLastMaintained(long var1);

    public abstract long getLastMaintained();

    public abstract long getOwnerId();

    public abstract boolean setOwnerId(long var1);

    public abstract boolean getLocked();

    public abstract void setLocked(boolean var1);

    @Override
    public abstract int getTemplateId();

    public abstract void setTemplateId(int var1);

    public abstract void setZoneId(int var1, boolean var2);

    public abstract int getZoneId();

    public abstract boolean setDescription(@Nonnull String var1);

    @Nonnull
    public abstract String getDescription();

    public abstract boolean setInscription(@Nonnull String var1, @Nonnull String var2);

    public abstract boolean setInscription(@Nonnull String var1, @Nonnull String var2, int var3);

    public abstract void setName(@Nonnull String var1);

    public abstract void setName(String var1, boolean var2);

    public abstract float getRotation();

    public abstract void setPosXYZRotation(float var1, float var2, float var3, float var4);

    public abstract void setPosXYZ(float var1, float var2, float var3);

    public abstract void setPosXY(float var1, float var2);

    public abstract void setPosX(float var1);

    public abstract void setPosY(float var1);

    public abstract void setPosZ(float var1);

    public abstract void setPos(float var1, float var2, float var3, float var4, long var5);

    public abstract void savePosition();

    public abstract void setRotation(float var1);

    public abstract Set<Item> getItems();

    public abstract Item[] getItemsAsArray();

    public abstract void setParentId(long var1, boolean var3);

    public abstract long getParentId();

    abstract void setSizeX(int var1);

    abstract void setSizeY(int var1);

    abstract void setSizeZ(int var1);

    public abstract int getSizeX();

    public int getSizeX(boolean useModifier) {
        if (useModifier) {
            return this.getSizeX();
        }
        return this.sizeX;
    }

    public abstract int getSizeY();

    public int getSizeY(boolean useModifier) {
        if (useModifier) {
            return this.getSizeY();
        }
        return this.sizeY;
    }

    public abstract int getSizeZ();

    public int getSizeZ(boolean useModifier) {
        if (useModifier) {
            return this.getSizeZ();
        }
        return this.sizeZ;
    }

    public int getContainerSizeX() {
        float modifier = 1.0f;
        if (this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
        }
        if (this.template.usesSpecifiedContainerSizes()) {
            return (int)((float)this.template.getContainerSizeX() * modifier);
        }
        return this.getSizeX();
    }

    public int getContainerSizeY() {
        float modifier = 1.0f;
        if (this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
        }
        if (this.template.usesSpecifiedContainerSizes()) {
            return (int)((float)this.template.getContainerSizeY() * modifier);
        }
        return this.getSizeY();
    }

    public int getContainerSizeZ() {
        float modifier = 1.0f;
        if (this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
        }
        if (this.template.usesSpecifiedContainerSizes()) {
            return (int)((float)this.template.getContainerSizeZ() * modifier);
        }
        return this.getSizeZ();
    }

    public abstract int getWeightGrams();

    public int getWeightGrams(boolean useModifier) {
        if (useModifier) {
            return this.getWeightGrams();
        }
        return this.weight;
    }

    public abstract boolean setWeight(int var1, boolean var2, boolean var3);

    public abstract boolean setWeight(int var1, boolean var2);

    @Override
    public abstract void setOriginalQualityLevel(float var1);

    public abstract float getOriginalQualityLevel();

    public abstract boolean setDamage(float var1, boolean var2);

    public abstract void setData1(int var1);

    public abstract void setData2(int var1);

    public abstract void setData(int var1, int var2);

    public abstract int getData1();

    public abstract int getData2();

    public abstract void setExtra1(int var1);

    public abstract void setExtra2(int var1);

    public abstract void setExtra(int var1, int var2);

    public abstract int getExtra1();

    public abstract int getExtra2();

    public abstract void setAllData(int var1, int var2, int var3, int var4);

    public abstract void setTemperature(short var1);

    public abstract byte getMaterial();

    public abstract void setMaterial(byte var1);

    public abstract long getLockId();

    public abstract void setLockId(long var1);

    public abstract void setPrice(int var1);

    abstract void addItem(@Nullable Item var1, boolean var2);

    abstract void removeItem(Item var1);

    public abstract void setBanked(boolean var1);

    public abstract void setLastOwnerId(long var1);

    public final long getLastOwnerId() {
        return this.lastOwner;
    }

    public abstract void setAuxData(byte var1);

    final byte getCreationState() {
        return this.creationState;
    }

    public abstract void setCreationState(byte var1);

    public final int getRealTemplateId() {
        return this.realTemplate;
    }

    public abstract void setRealTemplate(int var1);

    final boolean isWornAsArmour() {
        return this.wornAsArmour;
    }

    abstract void setWornAsArmour(boolean var1, long var2);

    public final int getColor() {
        if ((this.isStreetLamp() || this.isLight() || this.isLightBright()) && this.color == WurmColor.createColor(0, 0, 0)) {
            this.setColor(WurmColor.createColor(1, 1, 1));
        }
        if (this.getTemplateId() == 531) {
            return WurmColor.createColor(40, 40, 215);
        }
        if (this.getTemplateId() == 534) {
            return WurmColor.createColor(215, 40, 40);
        }
        if (this.getTemplateId() == 537) {
            return WurmColor.createColor(10, 10, 10);
        }
        return this.color;
    }

    public final int getColor2() {
        if (this.getTemplateId() == 531) {
            return WurmColor.createColor(0, 130, 0);
        }
        if (this.getTemplateId() == 534) {
            return WurmColor.createColor(255, 255, 0);
        }
        if (this.getTemplateId() == 537) {
            return WurmColor.createColor(110, 0, 150);
        }
        return this.color2;
    }

    public final String getSecondryItemName() {
        return this.template.getSecondryItemName();
    }

    public final byte getMailTimes() {
        return this.mailTimes;
    }

    public abstract void setColor(int var1);

    public abstract void setColor2(int var1);

    final boolean isFemale() {
        return this.female;
    }

    public final boolean isMushroom() {
        return this.template.isMushroom();
    }

    public abstract void setFemale(boolean var1);

    public final boolean isTransferred() {
        return this.transferred;
    }

    public final boolean isMagicContainer() {
        return this.template.magicContainer;
    }

    public final boolean willLeaveServer(boolean leaving, boolean changingCluster, boolean ownerDeity) {
        if (this.isBodyPartAttached()) {
            return true;
        }
        if (this.isServerBound()) {
            if (this.isVillageDeed() || this.isHomesteadDeed()) {
                if (this.getData2() > 0) {
                    if (leaving) {
                        this.setTransferred(true);
                    }
                    return false;
                }
                if (leaving) {
                    this.setTransferred(false);
                }
            } else {
                if (this.getTemplateId() == 166) {
                    if (leaving) {
                        this.setTransferred(true);
                    }
                    return false;
                }
                if (this.getTemplateId() == 300 || this.getTemplateId() == 1129) {
                    if (this.getData() > 0L) {
                        if (leaving) {
                            this.setTransferred(true);
                        }
                        return false;
                    }
                    if (leaving) {
                        this.setTransferred(false);
                    }
                } else {
                    if (this.isRoyal()) {
                        if (leaving) {
                            this.setTransferred(true);
                        }
                        return false;
                    }
                    if (this.isArtifact()) {
                        if (leaving) {
                            try {
                                String act;
                                this.getParent().dropItem(this.getWurmId(), false);
                                switch (Server.rand.nextInt(6)) {
                                    case 0: {
                                        act = "is reported to have disappeared.";
                                        break;
                                    }
                                    case 1: {
                                        act = "is gone missing.";
                                        break;
                                    }
                                    case 2: {
                                        act = "returned to the depths.";
                                        break;
                                    }
                                    case 3: {
                                        act = "seems to have decided to leave.";
                                        break;
                                    }
                                    case 4: {
                                        act = "has found a new location.";
                                        break;
                                    }
                                    default: {
                                        act = "has vanished.";
                                    }
                                }
                                HistoryManager.addHistory("The " + this.getName(), act);
                                int onethird = Zones.worldTileSizeX / 3;
                                int ntx = onethird + Server.rand.nextInt(onethird);
                                int nty = onethird + Server.rand.nextInt(onethird);
                                float npx = ntx * 4 + 2;
                                float npy = nty * 4 + 2;
                                this.setPosXY(npx, npy);
                                Zone z = Zones.getZone(ntx, nty, true);
                                z.addItem(this);
                            }
                            catch (NoSuchItemException nsi) {
                                Item.logWarn(this.getName() + ", " + this.getWurmId() + " no parent " + nsi.getMessage(), nsi);
                            }
                            catch (NoSuchZoneException nz) {
                                Item.logWarn(this.getName() + ", " + this.getWurmId() + " no zone " + nz.getMessage(), nz);
                            }
                        }
                        return false;
                    }
                    if (leaving) {
                        this.setTransferred(true);
                    }
                    return false;
                }
            }
        }
        if (changingCluster && !ownerDeity) {
            if (this.isNewbieItem()) {
                return true;
            }
            if (leaving) {
                this.setTransferred(true);
            }
            return false;
        }
        if (this.isTransferred()) {
            this.setTransferred(false);
        }
        return true;
    }

    public final int getFloorLevel() {
        try {
            Vector2f pos2f = this.getPos2f();
            TilePos tilePos = CoordUtils.WorldToTile(pos2f);
            Zone zone = Zones.getZone(tilePos, this.isOnSurface());
            VolaTile tile = zone.getOrCreateTile(tilePos);
            if (tile.getStructure() == null) {
                return 0;
            }
            float posZ = this.getPosZ();
            long bridgeId = this.getBridgeId();
            float z2 = bridgeId > 0L ? Zones.calculatePosZ(pos2f.x, pos2f.y, tile, this.isOnSurface(), false, posZ, null, bridgeId) : Zones.calculateHeight(pos2f.x, pos2f.y, this.isOnSurface());
            int floor = (int)(Math.max(0.0f, posZ - z2 + 0.5f) * 10.0f) / 30;
            return floor;
        }
        catch (NoSuchZoneException snz) {
            return 0;
        }
    }

    public abstract void setTransferred(boolean var1);

    abstract void addNewKey(long var1);

    abstract void removeNewKey(long var1);

    public final boolean isMailed() {
        return this.mailed;
    }

    public abstract void setMailed(boolean var1);

    abstract void clear(long var1, String var3, float var4, float var5, float var6, float var7, String var8, String var9, float var10, byte var11, byte var12, long var13);

    final boolean isHidden() {
        return this.hidden;
    }

    public final boolean isSpringFilled() {
        return this.template.isSpringFilled;
    }

    public final boolean isTurnable() {
        if (this.permissions.hasPermission(Permissions.Allow.NOT_TURNABLE.getBit())) {
            return false;
        }
        return this.templateTurnable();
    }

    public final boolean templateTurnable() {
        return this.template.turnable;
    }

    public final boolean templateNoTake() {
        if (this.isUnfinished() && this.realTemplate > 0) {
            ItemTemplate realTemplate = this.getRealTemplate();
            assert (realTemplate != null);
            return realTemplate.isUnfinishedNoTake;
        }
        return this.template.notake;
    }

    public final boolean isColorable() {
        if (this.permissions.hasPermission(Permissions.Allow.NOT_PAINTABLE.getBit())) {
            return false;
        }
        return this.templateIsColorable();
    }

    public final boolean isSourceSpring() {
        return this.template.isSourceSpring;
    }

    public final boolean isSource() {
        return this.template.isSource;
    }

    public byte getRarity() {
        return this.rarity;
    }

    public final boolean isDredgingTool() {
        return this.template.isDredgingTool;
    }

    public final boolean isTent() {
        return this.template.isTent();
    }

    public final boolean isUseMaterialAndKingdom() {
        return this.template.useMaterialAndKingdom;
    }

    public final void setProtected(boolean isProtected) {
        Items.setProtected(this.getWurmId(), isProtected);
    }

    public final boolean isCorpseLootable() {
        return !Items.isProtected(this);
    }

    public static ItemTransferDatabaseLogger getItemlogger() {
        return itemLogger;
    }

    public final boolean isColorComponent() {
        return this.template.isColorComponent;
    }

    public abstract void setHidden(boolean var1);

    public abstract void setOwnerStuff(ItemTemplate var1);

    public abstract boolean setRarity(byte var1);

    public long onBridge() {
        return this.onBridge;
    }

    public void setOnBridge(long theBridge) {
        this.onBridge = theBridge;
    }

    public final void setSettings(int newSettings) {
        this.permissions.setPermissionBits(newSettings);
    }

    public final Permissions getSettings() {
        return this.permissions;
    }

    public final int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (int)(this.id ^ this.id >>> 32);
        return result;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Item)) {
            return false;
        }
        Item other = (Item)obj;
        return this.id == other.id;
    }

    @Nonnull
    public String toString() {
        String toReturn = "Item [ID: " + this.getWurmId() + ", Name: " + this.getName();
        if (this.getTemplate() != null && this.getTemplate().getName() != null) {
            toReturn = toReturn + ", Template: " + this.getTemplate().getName();
        }
        if (this.getRealTemplate() != null && this.getRealTemplate().getName() != null) {
            toReturn = toReturn + ", Realtemplate: " + this.getRealTemplate().getName();
        }
        toReturn = toReturn + ", QL: " + this.getQualityLevel() + ", Rarity: " + this.getRarity();
        toReturn = toReturn + ", Tile: " + this.getTileX() + ',' + this.getTileY() + ']';
        return toReturn;
    }

    public boolean isInTheNorthWest() {
        return this.getTileX() < Zones.worldTileSizeX / 3 && this.getTileY() < Zones.worldTileSizeY / 3;
    }

    public boolean isInTheNorthEast() {
        return this.getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3 && this.getTileY() < Zones.worldTileSizeY / 3;
    }

    public boolean isInTheSouthEast() {
        return this.getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3 && this.getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3;
    }

    public boolean isInTheSouthWest() {
        return this.getTileX() < Zones.worldTileSizeX / 3 && this.getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3;
    }

    public boolean isInTheNorth() {
        return this.getTileY() < Zones.worldTileSizeY / 3;
    }

    public boolean isInTheEast() {
        return this.getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3;
    }

    public boolean isInTheSouth() {
        return this.getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3;
    }

    public boolean isInTheWest() {
        return this.getTileX() < Zones.worldTileSizeX / 3;
    }

    public int getGlobalMapPlacement() {
        if (this.isInTheNorthWest()) {
            return 7;
        }
        if (this.isInTheNorthEast()) {
            return 1;
        }
        if (this.isInTheSouthEast()) {
            return 3;
        }
        if (this.isInTheSouthWest()) {
            return 5;
        }
        return -1;
    }

    public boolean isOwnedByWagoner() {
        if (WurmId.getType(this.lastOwner) == 1) {
            return Wagoner.getWagoner(this.lastOwner) != null;
        }
        return false;
    }

    public final boolean isWagonerCamp() {
        long data = this.getData();
        if (data == -1L) {
            return false;
        }
        if (WurmId.getType(data) == 1 && Wagoner.getWagoner(data) != null) {
            return true;
        }
        this.setData(-10L);
        return false;
    }

    public abstract void setDbStrings(DbStrings var1);

    public abstract DbStrings getDbStrings();

    public abstract void setMailTimes(byte var1);

    public abstract void moveToFreezer();

    public abstract void returnFromFreezer();

    public abstract void deleteInDatabase();

    public final int getPotionTemplateIdForBlood() {
        int toReturn = -1;
        switch (this.getData2()) {
            case 27: {
                toReturn = 881;
                break;
            }
            case 26: {
                toReturn = 874;
                break;
            }
            case 16: {
                toReturn = 872;
                break;
            }
            case 103: {
                toReturn = 871;
                break;
            }
            case 18: {
                toReturn = 876;
                break;
            }
            case 17: {
                toReturn = 886;
                break;
            }
            case 19: {
                toReturn = 888;
                break;
            }
            case 22: {
                toReturn = 879;
                break;
            }
            case 20: {
                toReturn = 883;
                break;
            }
            case 70: {
                toReturn = 880;
                break;
            }
            case 89: {
                toReturn = 884;
                break;
            }
            case 92: {
                toReturn = 878;
                break;
            }
            case 91: {
                toReturn = 1413;
                break;
            }
            case 104: {
                toReturn = 877;
                break;
            }
            case 90: {
                toReturn = 875;
                break;
            }
            default: {
                toReturn = 884;
            }
        }
        return toReturn;
    }

    public static final int getRandomImbuePotionTemplateId() {
        int toReturn = -1;
        switch (Server.rand.nextInt(17)) {
            case 0: {
                toReturn = 881;
                break;
            }
            case 1: {
                toReturn = 874;
                break;
            }
            case 2: {
                toReturn = 872;
                break;
            }
            case 3: {
                toReturn = 871;
                break;
            }
            case 4: {
                toReturn = 876;
                break;
            }
            case 5: {
                toReturn = 886;
                break;
            }
            case 6: {
                toReturn = 888;
                break;
            }
            case 7: {
                toReturn = 879;
                break;
            }
            case 8: {
                toReturn = 883;
                break;
            }
            case 9: {
                toReturn = 880;
                break;
            }
            case 10: {
                toReturn = 882;
                break;
            }
            case 11: {
                toReturn = 878;
                break;
            }
            case 12: {
                toReturn = 873;
                break;
            }
            case 13: {
                toReturn = 877;
                break;
            }
            case 14: {
                toReturn = 875;
                break;
            }
            case 15: {
                toReturn = 1413;
                break;
            }
            default: {
                toReturn = 884;
            }
        }
        return toReturn;
    }

    public final boolean isSmearable() {
        return this.template.isSmearable();
    }

    public final boolean canBePapyrusWrapped() {
        return this.template.canBePapyrusWrapped();
    }

    public final boolean canBeRawWrapped() {
        return this.template.canBeRawWrapped();
    }

    public final boolean canBeClothWrapped() {
        return this.template.canBeClothWrapped();
    }

    public final byte getEnchantForPotion() {
        byte toReturn = -1;
        switch (this.getTemplateId()) {
            case 881: {
                toReturn = 86;
                break;
            }
            case 874: {
                toReturn = 79;
                break;
            }
            case 871: {
                toReturn = 77;
                break;
            }
            case 876: {
                toReturn = 81;
                break;
            }
            case 886: {
                toReturn = 90;
                break;
            }
            case 888: {
                toReturn = 92;
                break;
            }
            case 879: {
                toReturn = 84;
                break;
            }
            case 883: {
                toReturn = 88;
                break;
            }
            case 880: {
                toReturn = 85;
                break;
            }
            case 872: {
                toReturn = 78;
                break;
            }
            case 873: {
                toReturn = 76;
                break;
            }
            case 875: {
                toReturn = 80;
                break;
            }
            case 877: {
                toReturn = 82;
                break;
            }
            case 878: {
                toReturn = 83;
                break;
            }
            case 887: {
                toReturn = 91;
                break;
            }
            case 884: {
                toReturn = 89;
                break;
            }
            case 882: {
                toReturn = 87;
                break;
            }
            case 1413: {
                toReturn = 99;
                break;
            }
            case 1091: {
                toReturn = 98;
                break;
            }
            default: {
                toReturn = -1;
            }
        }
        return toReturn;
    }

    public final boolean mayFireTrebuchet() {
        return WurmCalendar.currentTime - this.getLastMaintained() > 120L;
    }

    @Override
    public final boolean canBeAlwaysLit() {
        return this.isLight() && !this.templateAlwaysLit();
    }

    @Override
    public final boolean canBeAutoFilled() {
        return this.isSpringFilled();
    }

    @Override
    public final boolean canBeAutoLit() {
        return this.isLight() && !this.templateAlwaysLit();
    }

    @Override
    public final boolean canBePlanted() {
        return this.template.isPlantable();
    }

    public final boolean isPlantOneAWeek() {
        return this.template.isPlantOneAWeeek();
    }

    public final boolean descIsName() {
        return this.template.descIsName;
    }

    @Override
    public final boolean canBeSealedByPlayer() {
        if (!this.template.canBeSealed()) {
            return false;
        }
        Item[] items = this.getItemsAsArray();
        return items.length == 1 && items[0].isLiquid();
    }

    @Override
    public final boolean canBePeggedByPlayer() {
        if (!this.template.canBePegged()) {
            return false;
        }
        Item[] items = this.getItemsAsArray();
        return items.length == 1 && items[0].isLiquid() && !items[0].isFermenting();
    }

    @Override
    public boolean canChangeCreator() {
        return true;
    }

    @Override
    public final boolean canDisableDecay() {
        return true;
    }

    @Override
    public final boolean canDisableDestroy() {
        return !this.templateIndestructible();
    }

    @Override
    public boolean canDisableDrag() {
        return this.template.draggable;
    }

    @Override
    public boolean canDisableDrop() {
        return !this.template.nodrop;
    }

    @Override
    public boolean canDisableEatAndDrink() {
        return this.isFood() || this.isLiquid();
    }

    @Override
    public boolean canDisableImprove() {
        return !this.template.isNoImprove();
    }

    @Override
    public final boolean canDisableLocking() {
        return this.templateIsLockable();
    }

    @Override
    public final boolean canDisableLockpicking() {
        return this.templateIsLockable();
    }

    @Override
    public final boolean canDisableMoveable() {
        return !this.templateIsNoMove();
    }

    @Override
    public final boolean canDisableOwnerMoveing() {
        return !this.template.isOwnerMoveable;
    }

    @Override
    public final boolean canDisableOwnerTurning() {
        return !this.template.isOwnerTurnable;
    }

    @Override
    public final boolean canDisablePainting() {
        return this.templateIsColorable();
    }

    @Override
    public boolean canDisablePut() {
        return !this.template.isNoPut;
    }

    @Override
    public boolean canDisableRepair() {
        return this.isRepairableDefault();
    }

    @Override
    public boolean canDisableRuneing() {
        return !this.template.isNotRuneable;
    }

    @Override
    public final boolean canDisableSpellTarget() {
        return !this.template.cannotBeSpellTarget();
    }

    @Override
    public final boolean canDisableTake() {
        return !this.templateNoTake();
    }

    @Override
    public final boolean canDisableTurning() {
        return this.templateTurnable();
    }

    @Override
    public final boolean canHaveCourier() {
        return this.isMailBox() || this.isSpringFilled() || this.isUnenchantedTurret() || this.isPuppet();
    }

    @Override
    public final boolean canHaveDakrMessenger() {
        return this.isMailBox() || this.isSpringFilled() || this.isUnenchantedTurret() || this.isPuppet();
    }

    @Override
    @Nonnull
    public final String getCreatorName() {
        if (this.creator != null && !this.creator.isEmpty()) {
            return this.creator;
        }
        return "";
    }

    @Override
    public abstract float getDamage();

    @Override
    public abstract float getQualityLevel();

    @Override
    public final boolean hasCourier() {
        return this.permissions.hasPermission(Permissions.Allow.HAS_COURIER.getBit());
    }

    @Override
    public final boolean hasDarkMessenger() {
        return this.permissions.hasPermission(Permissions.Allow.HAS_DARK_MESSENGER.getBit());
    }

    @Override
    public final boolean hasNoDecay() {
        return this.permissions.hasPermission(Permissions.Allow.DECAY_DISABLED.getBit());
    }

    @Override
    public final boolean isAlwaysLit() {
        if (this.permissions.hasPermission(Permissions.Allow.ALWAYS_LIT.getBit())) {
            return true;
        }
        return this.templateAlwaysLit();
    }

    @Override
    public final boolean isAutoFilled() {
        return this.permissions.hasPermission(Permissions.Allow.AUTO_FILL.getBit());
    }

    @Override
    public final boolean isAutoLit() {
        return this.permissions.hasPermission(Permissions.Allow.AUTO_LIGHT.getBit());
    }

    @Override
    public final boolean isIndestructible() {
        if (this.permissions.hasPermission(Permissions.Allow.NO_BASH.getBit())) {
            return true;
        }
        if (this.getTemplateId() == 1112 && (this.getData() != -1L || Items.isWaystoneInUse(this.getWurmId()))) {
            return true;
        }
        if (this.getTemplateId() == 1309 && this.isSealedByPlayer()) {
            return true;
        }
        return this.templateIndestructible();
    }

    @Override
    public final boolean isNoDrag() {
        if (this.permissions.hasPermission(Permissions.Allow.NO_DRAG.getBit())) {
            return true;
        }
        return !this.template.draggable;
    }

    @Override
    public final boolean isNoDrop() {
        if (this.permissions.hasPermission(Permissions.Allow.NO_DROP.getBit())) {
            return true;
        }
        if (this.realTemplate > 0) {
            ItemTemplate realTemplate = this.getRealTemplate();
            assert (realTemplate != null);
            return realTemplate.nodrop;
        }
        return this.template.nodrop;
    }

    @Override
    public boolean isNoEatOrDrink() {
        return this.permissions.hasPermission(Permissions.Allow.NO_EAT_OR_DRINK.getBit());
    }

    @Override
    public final boolean isNoImprove() {
        if (this.permissions.hasPermission(Permissions.Allow.NO_IMPROVE.getBit())) {
            return true;
        }
        return this.template.isNoImprove();
    }

    @Override
    public final boolean isNoMove() {
        if (this.permissions.hasPermission(Permissions.Allow.NOT_MOVEABLE.getBit())) {
            return true;
        }
        return this.templateIsNoMove();
    }

    @Override
    public final boolean isNoPut() {
        if (this.permissions.hasPermission(Permissions.Allow.NO_PUT.getBit())) {
            return true;
        }
        if (this.getTemplateId() == 1342) {
            return !this.isPlanted() && this.getData() == -1L;
        }
        return this.template.isNoPut;
    }

    @Override
    public boolean isNoRepair() {
        return !this.isRepairable();
    }

    @Override
    public final boolean isNoTake() {
        if (this.permissions.hasPermission(Permissions.Allow.NO_TAKE.getBit())) {
            return true;
        }
        if (this.templateNoTake()) {
            return true;
        }
        if (!(this.getTemplateId() != 1312 && this.getTemplateId() != 1309 || this.isEmpty(false))) {
            return true;
        }
        return this.getTemplateId() == 1315 && !this.isEmpty(false);
    }

    public final boolean isNoTake(Creature creature) {
        if (this.isNoTake()) {
            return true;
        }
        if (this.getTemplateId() == 272 && this.wasBrandedTo != -10L) {
            return !this.mayCommand(creature);
        }
        return false;
    }

    @Override
    public final boolean isNotLockable() {
        return !this.isLockable();
    }

    public final boolean isLockable() {
        if (this.permissions.hasPermission(Permissions.Allow.NOT_LOCKABLE.getBit())) {
            return false;
        }
        return this.templateIsLockable();
    }

    @Override
    public final boolean isNotLockpickable() {
        return !this.isLockpickable();
    }

    public final boolean isLockpickable() {
        if (this.permissions.hasPermission(Permissions.Allow.NOT_LOCKPICKABLE.getBit())) {
            return false;
        }
        return this.templateIsLockable();
    }

    @Override
    public final boolean isNotPaintable() {
        return !this.isColorable();
    }

    @Override
    public boolean isNotRuneable() {
        if (this.permissions.hasPermission(Permissions.Allow.NOT_RUNEABLE.getBit())) {
            return true;
        }
        return this.template.isNotRuneable;
    }

    @Override
    public final boolean isNotSpellTarget() {
        if (this.permissions.hasPermission(Permissions.Allow.NO_SPELLS.getBit())) {
            return true;
        }
        return this.template.cannotBeSpellTarget();
    }

    @Override
    public final boolean isNotTurnable() {
        return !this.isTurnable();
    }

    @Override
    public boolean isOwnerMoveable() {
        if (this.permissions.hasPermission(Permissions.Allow.OWNER_MOVEABLE.getBit())) {
            return true;
        }
        return this.template.isOwnerMoveable;
    }

    @Override
    public boolean isOwnerTurnable() {
        if (this.permissions.hasPermission(Permissions.Allow.OWNER_TURNABLE.getBit())) {
            return true;
        }
        return this.template.isOwnerTurnable;
    }

    @Override
    public final boolean isPlanted() {
        return this.permissions.hasPermission(Permissions.Allow.PLANTED.getBit());
    }

    @Override
    public final boolean isSealedByPlayer() {
        if (this.isSealedOverride) {
            return true;
        }
        return this.permissions.hasPermission(Permissions.Allow.SEALED_BY_PLAYER.getBit());
    }

    @Override
    public abstract void setCreator(String var1);

    @Override
    public abstract boolean setDamage(float var1);

    @Override
    public final void setHasCourier(boolean aCourier) {
        this.permissions.setPermissionBit(Permissions.Allow.HAS_COURIER.getBit(), aCourier);
        this.savePermissions();
    }

    @Override
    public final void setHasDarkMessenger(boolean aDarkmessenger) {
        this.permissions.setPermissionBit(Permissions.Allow.HAS_DARK_MESSENGER.getBit(), aDarkmessenger);
        this.savePermissions();
    }

    @Override
    public final void setHasNoDecay(boolean aNoDecay) {
        this.permissions.setPermissionBit(Permissions.Allow.DECAY_DISABLED.getBit(), aNoDecay);
    }

    @Override
    public final void setIsAlwaysLit(boolean aAlwaysLit) {
        this.permissions.setPermissionBit(Permissions.Allow.ALWAYS_LIT.getBit(), aAlwaysLit);
    }

    @Override
    public final void setIsAutoFilled(boolean aAutoFill) {
        this.permissions.setPermissionBit(Permissions.Allow.AUTO_FILL.getBit(), aAutoFill);
    }

    @Override
    public final void setIsAutoLit(boolean aAutoLight) {
        this.permissions.setPermissionBit(Permissions.Allow.AUTO_LIGHT.getBit(), aAutoLight);
    }

    @Override
    public final void setIsIndestructible(boolean aNoDestroy) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_BASH.getBit(), aNoDestroy);
    }

    @Override
    public void setIsNoDrag(boolean aNoDrag) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_DRAG.getBit(), aNoDrag);
    }

    @Override
    public void setIsNoDrop(boolean aNoDrop) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_DROP.getBit(), aNoDrop);
    }

    @Override
    public void setIsNoEatOrDrink(boolean aNoEatOrDrink) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_EAT_OR_DRINK.getBit(), aNoEatOrDrink);
    }

    @Override
    public void setIsNoImprove(boolean aNoImprove) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_IMPROVE.getBit(), aNoImprove);
    }

    @Override
    public final void setIsNoMove(boolean aNoMove) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_MOVEABLE.getBit(), aNoMove);
    }

    @Override
    public void setIsNoPut(boolean aNoPut) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_PUT.getBit(), aNoPut);
    }

    @Override
    public void setIsNoRepair(boolean aNoRepair) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_REPAIR.getBit(), aNoRepair);
    }

    @Override
    public final void setIsNoTake(boolean aNoTake) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_TAKE.getBit(), aNoTake);
    }

    @Override
    public final void setIsNotLockable(boolean aNoLock) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKABLE.getBit(), aNoLock);
    }

    @Override
    public final void setIsNotLockpickable(boolean aNoLockpick) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKPICKABLE.getBit(), aNoLockpick);
    }

    @Override
    public final void setIsNotPaintable(boolean aNoPaint) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_PAINTABLE.getBit(), aNoPaint);
    }

    @Override
    public void setIsNotRuneable(boolean aNoRune) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_RUNEABLE.getBit(), aNoRune);
    }

    @Override
    public final void setIsNotSpellTarget(boolean aNoSpells) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_SPELLS.getBit(), aNoSpells);
    }

    @Override
    public final void setIsNotTurnable(boolean aNoTurn) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_TURNABLE.getBit(), aNoTurn);
    }

    @Override
    public void setIsOwnerMoveable(boolean aOwnerMove) {
        this.permissions.setPermissionBit(Permissions.Allow.OWNER_MOVEABLE.getBit(), aOwnerMove);
    }

    @Override
    public void setIsOwnerTurnable(boolean aOwnerTurn) {
        this.permissions.setPermissionBit(Permissions.Allow.OWNER_TURNABLE.getBit(), aOwnerTurn);
    }

    @Override
    public final void setIsPlanted(boolean aPlant) {
        boolean wasPlanted = this.isPlanted();
        this.permissions.setPermissionBit(Permissions.Allow.PLANTED.getBit(), aPlant);
        if (this.isRoadMarker()) {
            if (!aPlant && wasPlanted) {
                MethodsHighways.removeLinksTo(this);
                this.setIsNoTake(false);
            } else if (aPlant && !wasPlanted) {
                this.setIsNoTake(true);
                this.replacing = false;
            }
        }
        if (this.getTemplateId() == 1396) {
            if (!aPlant && wasPlanted) {
                this.setTemperature((short)200);
                this.updateIfGroundItem();
            } else if (aPlant && !wasPlanted) {
                this.updateIfGroundItem();
            }
        }
        if (this.getTemplateId() == 1342) {
            if (!aPlant && wasPlanted) {
                this.setIsNoMove(false);
            } else if (aPlant && !wasPlanted) {
                this.setIsNoMove(true);
            }
        }
        if (this.getTemplateId() == 677) {
            if (!aPlant && wasPlanted) {
                Items.removeGmSign(this);
                this.setIsNoTake(false);
            } else if (aPlant && !wasPlanted) {
                Items.addGmSign(this);
                this.setIsNoTake(true);
            }
        }
        if (this.getTemplateId() == 1309) {
            if (!aPlant && wasPlanted) {
                Items.removeWagonerContainer(this);
                this.setData(-1L);
            } else if (!aPlant || !wasPlanted) {
                // empty if block
            }
        }
        this.savePermissions();
    }

    @Override
    public void setIsSealedByPlayer(boolean aSealed) {
        this.permissions.setPermissionBit(Permissions.Allow.SEALED_BY_PLAYER.getBit(), aSealed);
        this.isSealedOverride = false;
        if (!aSealed) {
            for (Item item : this.getItemsAsArray()) {
                item.setLastMaintained(WurmCalendar.getCurrentTime());
            }
            if (this.getTemplateId() == 1309) {
                Delivery.freeContainer(this.getWurmId());
            }
        }
        this.updateName();
        this.setIsNoPut(aSealed);
        this.savePermissions();
        Item topParent = this.getTopParentOrNull();
        if (topParent != null && topParent.isHollow()) {
            if (this.watchers == null) {
                return;
            }
            long inventoryWindow = this.getTopParent();
            if (topParent.isInventory()) {
                inventoryWindow = -1L;
            }
            for (Creature watcher : this.watchers) {
                watcher.getCommunicator().sendRemoveFromInventory(this, inventoryWindow);
                watcher.getCommunicator().sendAddToInventory(this, inventoryWindow, -1L, -1);
            }
        }
    }

    @Override
    public abstract boolean setQualityLevel(float var1);

    @Override
    public abstract void savePermissions();

    @Override
    public String getObjectName() {
        return this.getDescription().replace("\"", "'");
    }

    @Override
    public String getTypeName() {
        return this.getActualName().replace("\"", "'");
    }

    @Override
    public boolean setObjectName(String newName, Creature creature) {
        return this.setDescription(newName);
    }

    @Override
    public boolean isActualOwner(long playerId) {
        return playerId == this.lastOwner;
    }

    @Override
    public boolean isOwner(Creature creature) {
        return this.isOwner(creature.getWurmId());
    }

    @Override
    public boolean isOwner(long playerId) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (this.isBed()) {
            Structure structure;
            VolaTile vt = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Structure structure2 = structure = vt != null ? vt.getStructure() : null;
            if (structure != null && structure.isTypeHouse() && structure.isFinished()) {
                return structure.isOwner(playerId);
            }
            return false;
        }
        return this.isActualOwner(playerId);
    }

    @Override
    public boolean canChangeName(Creature creature) {
        if (this.getTemplateId() == 272) {
            return false;
        }
        return this.isOwner(creature) || creature.getPower() >= 2;
    }

    @Override
    public boolean canChangeOwner(Creature creature) {
        if (this.isBed() || this.isNoTrade() || this.getTemplateId() == 272) {
            return false;
        }
        return creature.getPower() > 1 || this.isOwner(creature);
    }

    @Override
    public boolean setNewOwner(long playerId) {
        if (ItemSettings.exists(this.getWurmId())) {
            ItemSettings.remove(this.getWurmId());
            PermissionsHistories.addHistoryEntry(this.getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
        }
        this.setLastOwnerId(playerId);
        return true;
    }

    @Override
    public String getOwnerName() {
        return PlayerInfoFactory.getPlayerName(this.lastOwner);
    }

    @Override
    public String getWarning() {
        if (this.isOwnedByWagoner()) {
            return "WAGONER OWNS THIS";
        }
        if (this.isBed()) {
            Structure structure;
            VolaTile vt = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Structure structure2 = structure = vt != null ? vt.getStructure() : null;
            if (structure == null || !structure.isTypeHouse()) {
                return "BED NEEDS TO BE INSIDE A BUILDING TO WORK";
            }
            if (!structure.isFinished()) {
                return "BED NEEDS TO BE IN FINISHED BUILDING TO WORK";
            }
            return "";
        }
        if (this.getTemplateId() == 1271) {
            return "";
        }
        if (this.getTemplateId() == 272) {
            if (this.wasBrandedTo != -10L) {
                return "VIEW ONLY";
            }
            return "NEEDS TO HAVE BEEN BRANDED TO SEE PERMISSIONS";
        }
        if (this.getLockId() == -10L) {
            return "NEEDS TO HAVE A LOCK FOR PERMISSIONS TO WORK";
        }
        if (!this.isLocked()) {
            return "NEEDS TO BE LOCKED OTHERWISE EVERYONE CAN USE THIS";
        }
        return "";
    }

    @Override
    public PermissionsPlayerList getPermissionsPlayerList() {
        return ItemSettings.getPermissionsPlayerList(this.getWurmId());
    }

    @Override
    public boolean isManaged() {
        return this.permissions.hasPermission(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit());
    }

    @Override
    public boolean isManageEnabled(Player player) {
        return false;
    }

    @Override
    public void setIsManaged(boolean newIsManaged, Player player) {
    }

    @Override
    public String mayManageText(Player aPlayer) {
        return "";
    }

    @Override
    public String mayManageHover(Player aPlayer) {
        return "";
    }

    @Override
    public String messageOnTick() {
        return "";
    }

    @Override
    public String questionOnTick() {
        return "";
    }

    @Override
    public String messageUnTick() {
        return "";
    }

    @Override
    public String questionUnTick() {
        return "";
    }

    @Override
    public String getSettlementName() {
        if (this.isOwnedByWagoner()) {
            return "";
        }
        if (this.isBed()) {
            Structure structure;
            VolaTile vt = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Structure structure2 = structure = vt != null ? vt.getStructure() : null;
            if (structure != null && structure.isTypeHouse() && structure.isFinished()) {
                return structure.getSettlementName();
            }
            return "";
        }
        if (this.getTemplateId() == 272) {
            if (this.wasBrandedTo != -10L) {
                try {
                    Village lbVillage = Villages.getVillage((int)this.wasBrandedTo);
                    return "Citizens of \"" + lbVillage.getName() + "\"";
                }
                catch (NoSuchVillageException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } else {
            Village loVillage = Villages.getVillageForCreature(this.lastOwner);
            if (loVillage != null) {
                return "Citizens of \"" + loVillage.getName() + "\"";
            }
        }
        return "";
    }

    @Override
    public String getAllianceName() {
        if (this.isOwnedByWagoner()) {
            return "";
        }
        if (this.isBed()) {
            Structure structure;
            VolaTile vt = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
            Structure structure2 = structure = vt != null ? vt.getStructure() : null;
            if (structure != null && structure.isTypeHouse() && structure.isFinished()) {
                return structure.getAllianceName();
            }
            return "";
        }
        if (this.getTemplateId() == 272) {
            if (this.wasBrandedTo != -10L) {
                try {
                    Village lbVillage = Villages.getVillage((int)this.wasBrandedTo);
                    if (lbVillage != null && lbVillage.getAllianceNumber() > 0) {
                        return "Alliance of \"" + lbVillage.getAllianceName() + "\"";
                    }
                }
                catch (NoSuchVillageException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } else {
            Village loVillage = Villages.getVillageForCreature(this.lastOwner);
            if (loVillage != null && loVillage.getAllianceNumber() > 0) {
                return "Alliance of \"" + loVillage.getAllianceName() + "\"";
            }
        }
        return "";
    }

    @Override
    public String getKingdomName() {
        String toReturn = "";
        byte kingdom = this.getKingdom();
        if (this.isVehicle() && kingdom != 0) {
            toReturn = "Kingdom of \"" + Kingdoms.getNameFor(kingdom) + "\"";
        }
        return toReturn;
    }

    @Override
    public boolean canAllowEveryone() {
        return true;
    }

    @Override
    public String getRolePermissionName() {
        return "";
    }

    @Override
    public boolean isCitizen(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (this.isBed()) {
            Structure structure;
            VolaTile vt = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Structure structure2 = structure = vt != null ? vt.getStructure() : null;
            if (structure != null && structure.isTypeHouse() && structure.isFinished()) {
                return structure.isCitizen(creature);
            }
            return false;
        }
        if (this.getTemplateId() == 272) {
            if (this.wasBrandedTo != -10L) {
                try {
                    Village lbVillage = Villages.getVillage((int)this.wasBrandedTo);
                    if (lbVillage != null) {
                        return lbVillage.isCitizen(creature);
                    }
                }
                catch (NoSuchVillageException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } else {
            Village ownerVillage = Villages.getVillageForCreature(this.getLastOwnerId());
            if (ownerVillage != null) {
                return ownerVillage.isCitizen(creature);
            }
        }
        return false;
    }

    @Override
    public boolean isAllied(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (this.isBed()) {
            Structure structure;
            VolaTile vt = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Structure structure2 = structure = vt != null ? vt.getStructure() : null;
            if (structure != null && structure.isTypeHouse() && structure.isFinished()) {
                return structure.isAllied(creature);
            }
            return false;
        }
        if (this.getTemplateId() == 272) {
            if (this.wasBrandedTo != -10L) {
                try {
                    Village lbVillage = Villages.getVillage((int)this.wasBrandedTo);
                    if (lbVillage != null) {
                        return lbVillage.isAlly(creature);
                    }
                }
                catch (NoSuchVillageException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } else {
            Village ownerVillage = Villages.getVillageForCreature(this.getLastOwnerId());
            if (ownerVillage != null) {
                return ownerVillage.isAlly(creature);
            }
        }
        return false;
    }

    @Override
    public boolean isSameKingdom(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (this.isBed()) {
            Structure structure;
            VolaTile vt = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Structure structure2 = structure = vt != null ? vt.getStructure() : null;
            if (structure != null && structure.isTypeHouse() && structure.isFinished()) {
                return structure.isSameKingdom(creature);
            }
            return false;
        }
        if (this.isVehicle() && this.getKingdom() != 0) {
            return this.getKingdom() == creature.getKingdomId();
        }
        return Players.getInstance().getKingdomForPlayer(this.getLastOwnerId()) == creature.getKingdomId();
    }

    @Override
    public void addGuest(long guestId, int settings) {
        ItemSettings.addPlayer(this.id, guestId, settings);
    }

    @Override
    public void removeGuest(long guestId) {
        ItemSettings.removePlayer(this.id, guestId);
    }

    @Override
    public void addDefaultCitizenPermissions() {
    }

    @Override
    public boolean isGuest(Creature creature) {
        return this.isGuest(creature.getWurmId());
    }

    @Override
    public boolean isGuest(long playerId) {
        return ItemSettings.isGuest((PermissionsPlayerList.ISettings)this, playerId);
    }

    @Override
    public void save() throws IOException {
    }

    @Override
    public int getMaxAllowed() {
        return ItemSettings.getMaxAllowed();
    }

    @Override
    public final boolean canHavePermissions() {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        return this.isLockable() || this.isBed() || this.getTemplateId() == 1271 || this.getTemplateId() == 272 && this.wasBrandedTo != -10L;
    }

    @Override
    public final boolean mayShowPermissions(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (this.isBed()) {
            VolaTile vt = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Structure structure = vt != null ? vt.getStructure() : null;
            return structure != null && structure.isTypeHouse() && structure.isFinished() && this.mayManage(creature);
        }
        if (this.getTemplateId() == 1271) {
            return this.mayManage(creature);
        }
        if (this.getTemplateId() == 272) {
            return this.wasBrandedTo != -10L && creature.getPower() > 1;
        }
        return this.isLocked() && this.mayManage(creature);
    }

    public final boolean canManage(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (ItemSettings.isExcluded(this, creature)) {
            return false;
        }
        return ItemSettings.canManage(this, creature);
    }

    public final boolean mayManage(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (this.isBed()) {
            VolaTile vt = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Structure structure = vt != null ? vt.getStructure() : null;
            return structure != null && structure.isTypeHouse() && structure.isFinished() && structure.mayManage(creature);
        }
        if (creature.getPower() > 1) {
            return true;
        }
        return this.canManage(creature);
    }

    public final boolean maySeeHistory(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (creature.getPower() > 1) {
            return true;
        }
        return this.isOwner(creature);
    }

    public final boolean mayCommand(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (ItemSettings.isExcluded(this, creature)) {
            return false;
        }
        return this.canHavePermissions() && ItemSettings.mayCommand(this, creature);
    }

    public final boolean mayPassenger(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (ItemSettings.isExcluded(this, creature)) {
            return false;
        }
        if (this.isChair() && this.getData() == creature.getWurmId()) {
            return true;
        }
        return this.canHavePermissions() && ItemSettings.mayPassenger(this, creature);
    }

    public final boolean mayAccessHold(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (ItemSettings.isExcluded(this, creature)) {
            return false;
        }
        if (!this.canHavePermissions()) {
            return true;
        }
        return ItemSettings.mayAccessHold(this, creature);
    }

    public final boolean mayUseBed(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (!ItemSettings.exists(this.getWurmId())) {
            VolaTile vt = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Structure structure = vt != null ? vt.getStructure() : null;
            return structure != null && structure.isTypeHouse();
        }
        if (ItemSettings.isExcluded(this, creature)) {
            return false;
        }
        return ItemSettings.mayUseBed(this, creature);
    }

    public final boolean mayFreeSleep(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (!ItemSettings.exists(this.getWurmId())) {
            VolaTile vt = Zones.getTileOrNull(this.getTilePos(), this.isOnSurface());
            Structure structure = vt != null ? vt.getStructure() : null;
            return structure != null && structure.isTypeHouse() && structure.mayPass(creature);
        }
        if (ItemSettings.isExcluded(this, creature)) {
            return false;
        }
        return ItemSettings.mayFreeSleep(this, creature);
    }

    public final boolean mayDrag(Creature creature) {
        if (this.isOwnedByWagoner()) {
            return false;
        }
        if (creature.isOnPvPServer() && !this.isMooredBoat() && !this.isLocked()) {
            return true;
        }
        if (ItemSettings.isExcluded(this, creature)) {
            return false;
        }
        return this.canHavePermissions() && ItemSettings.mayDrag(this, creature);
    }

    public final boolean mayPostNotices(Creature creature) {
        if (ItemSettings.isExcluded(this, creature)) {
            return false;
        }
        return this.canHavePermissions() && ItemSettings.mayPostNotices(this, creature);
    }

    public final boolean mayAddPMs(Creature creature) {
        if (ItemSettings.isExcluded(this, creature)) {
            return false;
        }
        return this.canHavePermissions() && ItemSettings.mayAddPMs(this, creature);
    }

    public long getWhenRented() {
        return this.whenRented;
    }

    public void setWhenRented(long when) {
        this.whenRented = when;
    }

    private static void logInfo(String msg) {
        logger.info(msg);
    }

    private static void logInfo(String msg, Throwable thrown) {
        logger.log(Level.INFO, msg, thrown);
    }

    private static void logWarn(String msg) {
        logger.warning(msg);
    }

    private static void logWarn(String msg, Throwable thrown) {
        logger.log(Level.WARNING, msg, thrown);
    }

    public boolean isPotable() {
        return this.template.isPotable();
    }

    public boolean usesFoodState() {
        return this.template.usesFoodState();
    }

    public boolean isRecipeItem() {
        return this.template.isRecipeItem();
    }

    public boolean isAlcohol() {
        return this.template.isAlcohol();
    }

    public boolean canBeDistilled() {
        return this.template.canBeDistilled();
    }

    public boolean canBeFermented() {
        return this.template.canBeFermented();
    }

    public boolean isCrushable() {
        return this.template.isCrushable();
    }

    public boolean isSurfaceOnly() {
        return this.template.isSurfaceOnly();
    }

    public boolean hasSeeds() {
        return this.template.hasSeeds();
    }

    public int getAlcoholStrength() {
        return this.template.getAlcoholStrength();
    }

    public void closeAll() {
        if (this.isHollow() && this.watchers != null) {
            Creature[] watcherArray;
            for (Creature watcher : watcherArray = this.watchers.toArray(new Creature[this.watchers.size()])) {
                this.close(watcher);
            }
        }
    }

    public void close(Creature performer) {
        if (this.isHollow() && performer.getCommunicator().sendCloseInventoryWindow(this.getWurmId())) {
            if (this.getParentId() == -10L) {
                this.removeWatcher(performer, true);
            } else {
                boolean found = false;
                try {
                    Creature[] crets = this.getParent().getWatchers();
                    for (int x = 0; x < crets.length; ++x) {
                        if (crets[x].getWurmId() != performer.getWurmId()) continue;
                        found = true;
                        break;
                    }
                }
                catch (NoSuchItemException noSuchItemException) {
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                    // empty catch block
                }
                if (!found) {
                    this.removeWatcher(performer, true);
                }
            }
        }
    }

    public boolean hasQueen() {
        return this.getAuxData() > 0;
    }

    public boolean hasTwoQueens() {
        return this.getAuxData() > 1;
    }

    public void addQueen() {
        byte queens = this.getAuxData();
        if (queens > 1) {
            return;
        }
        this.setAuxData((byte)(queens + 1));
        this.updateHiveModel();
        if (queens == 0) {
            Zones.addHive(this, false);
        }
    }

    public boolean removeQueen() {
        byte queens = this.getAuxData();
        if (queens == 0) {
            return false;
        }
        this.setAuxData((byte)(queens - 1));
        this.updateHiveModel();
        if (queens == 1) {
            Zones.removeHive(this, false);
        }
        return true;
    }

    void updateHiveModel() {
        if (this.getTemplateId() == 1239 || this.hasTwoQueens()) {
            this.updateName();
        } else {
            try {
                Zone z = Zones.getZone(this.zoneId);
                z.removeItem(this);
                z.addItem(this);
            }
            catch (NoSuchZoneException nsz) {
                Item.logWarn("Zone at " + ((int)this.getPosX() >> 2) + "," + ((int)this.getPosY() >> 2) + ",surf=" + this.isOnSurface() + " no such zone. Item: " + this);
            }
        }
    }

    public boolean isPStateNone() {
        if (this.usesFoodState()) {
            return this.getLeftAuxData() == 0;
        }
        return true;
    }

    public void setRaw() {
        if (this.usesFoodState()) {
            this.setRightAuxData(0);
        }
    }

    public boolean isRaw() {
        if (this.usesFoodState()) {
            return this.getRightAuxData() == 0;
        }
        return false;
    }

    public void setIsFried() {
        if (this.usesFoodState()) {
            this.setRightAuxData(1);
        }
    }

    public boolean isFried() {
        if (this.usesFoodState()) {
            return this.getRightAuxData() == 1;
        }
        return false;
    }

    public void setIsGrilled() {
        if (this.usesFoodState()) {
            this.setRightAuxData(2);
        }
    }

    public boolean isGrilled() {
        if (this.usesFoodState()) {
            return this.getRightAuxData() == 2;
        }
        return false;
    }

    public void setIsBoiled() {
        if (this.usesFoodState()) {
            this.setRightAuxData(3);
        }
    }

    public boolean isBoiled() {
        if (this.usesFoodState()) {
            return this.getRightAuxData() == 3;
        }
        return false;
    }

    public void setIsRoasted() {
        if (this.usesFoodState()) {
            this.setRightAuxData(4);
        }
    }

    public boolean isRoasted() {
        if (this.usesFoodState()) {
            return this.getRightAuxData() == 4;
        }
        return false;
    }

    public void setIsSteamed() {
        if (this.usesFoodState()) {
            this.setRightAuxData(5);
        }
    }

    public boolean isSteamed() {
        if (this.usesFoodState()) {
            return this.getRightAuxData() == 5;
        }
        return false;
    }

    public void setIsBaked() {
        if (this.usesFoodState()) {
            this.setRightAuxData(6);
        }
    }

    public boolean isBaked() {
        if (this.usesFoodState()) {
            return this.getRightAuxData() == 6;
        }
        return false;
    }

    public void setIsCooked() {
        if (this.usesFoodState()) {
            this.setRightAuxData(7);
        }
    }

    public boolean isCooked() {
        if (this.usesFoodState()) {
            return this.getRightAuxData() == 7;
        }
        return false;
    }

    public void setIsCandied() {
        if (this.usesFoodState()) {
            this.setRightAuxData(8);
        }
    }

    public boolean isCandied() {
        if (this.usesFoodState()) {
            return this.getRightAuxData() == 8;
        }
        return false;
    }

    public void setIsChocolateCoated() {
        if (this.usesFoodState()) {
            this.setRightAuxData(9);
        }
    }

    public boolean isChocolateCoated() {
        if (this.usesFoodState()) {
            return this.getRightAuxData() == 9;
        }
        return false;
    }

    private boolean isChoppedBitSet() {
        return (this.getAuxData() & 0x10) != 0;
    }

    private void setIsChoppedBit(boolean setBit) {
        this.setAuxData((byte)((this.getAuxData() & 0xFFFFFFEF) + (setBit ? 16 : 0)));
    }

    private boolean isMashedBitSet() {
        return (this.getAuxData() & 0x20) != 0;
    }

    private void setIsMashedBit(boolean setBit) {
        this.setAuxData((byte)((this.getAuxData() & 0xFFFFFFDF) + (setBit ? 32 : 0)));
    }

    private boolean isWrappedBitSet() {
        return (this.getAuxData() & 0x40) != 0;
    }

    private void setIsWrappedBit(boolean setBit) {
        this.setAuxData((byte)((this.getAuxData() & 0xFFFFFFBF) + (setBit ? 64 : 0)));
    }

    private boolean isFreshBitSet() {
        return (this.getAuxData() & 0xFFFFFF80) != 0;
    }

    private void setIsFreshBit(boolean setBit) {
        this.setAuxData((byte)((this.getAuxData() & 0x7F) + (setBit ? -128 : 0)));
    }

    public void setIsChopped(boolean isChopped) {
        if (this.usesFoodState()) {
            this.setIsChoppedBit(isChopped);
        }
    }

    public boolean isChopped() {
        if (this.usesFoodState()) {
            return this.isChoppedBitSet();
        }
        return false;
    }

    public void setIsDiced(boolean isDiced) {
        if (this.usesFoodState()) {
            this.setIsChoppedBit(isDiced);
        }
    }

    public boolean isDiced() {
        if (this.usesFoodState()) {
            return this.isChoppedBitSet();
        }
        return false;
    }

    public void setIsGround(boolean isGround) {
        if (this.usesFoodState()) {
            this.setIsChoppedBit(isGround);
        }
    }

    public boolean isGround() {
        if (this.usesFoodState()) {
            return this.isChoppedBitSet();
        }
        return false;
    }

    public void setIsUnfermented(boolean isUnfermented) {
        if (this.usesFoodState()) {
            this.setIsChoppedBit(isUnfermented);
        }
    }

    public boolean isUnfermented() {
        if (this.usesFoodState()) {
            return this.isChoppedBitSet();
        }
        return false;
    }

    public void setIsZombiefied(boolean isZombiefied) {
        if (this.usesFoodState()) {
            this.setIsChoppedBit(isZombiefied);
        }
    }

    public boolean isZombiefied() {
        if (this.usesFoodState()) {
            return this.isChoppedBitSet();
        }
        return false;
    }

    public void setIsWhipped(boolean isWhipped) {
        if (this.usesFoodState() && this.getTemplateId() == 1249) {
            this.setIsChoppedBit(isWhipped);
        }
    }

    public boolean isWhipped() {
        if (this.usesFoodState() && this.getTemplateId() == 1249) {
            return this.isChoppedBitSet();
        }
        return false;
    }

    public void setIsMashed(boolean isMashed) {
        if (this.usesFoodState() && this.isVegetable()) {
            this.setIsMashedBit(isMashed);
        }
    }

    public boolean isMashed() {
        if (this.usesFoodState() && this.isVegetable()) {
            return this.isMashedBitSet();
        }
        return false;
    }

    public void setIsMinced(boolean isMinced) {
        if (this.usesFoodState() && this.isMeat()) {
            this.setIsMashedBit(isMinced);
        }
    }

    public boolean isMinced() {
        if (this.usesFoodState() && this.isMeat()) {
            return this.isMashedBitSet();
        }
        return false;
    }

    public void setIsFermenting(boolean isFermenting) {
        if (this.usesFoodState() && this.canBeFermented()) {
            this.setIsMashedBit(this.isFermenting());
        }
    }

    public boolean isFermenting() {
        if (this.usesFoodState() && this.canBeFermented()) {
            return this.isMashedBitSet();
        }
        return false;
    }

    public void setIsUnderWeight(boolean isUnderWeight) {
        if (this.usesFoodState() && this.isFish()) {
            this.setIsMashedBit(isUnderWeight);
        }
    }

    public boolean isUnderWeight() {
        if (this.usesFoodState() && this.isFish()) {
            return this.isMashedBitSet();
        }
        return false;
    }

    public void setIsClotted(boolean isClotted) {
        if (this.usesFoodState() && this.getTemplateId() == 1249) {
            this.setIsMashedBit(isClotted);
        }
    }

    public boolean isClotted() {
        if (this.usesFoodState() && this.getTemplateId() == 1249) {
            return this.isMashedBitSet();
        }
        return false;
    }

    public void setIsWrapped(boolean isWrapped) {
        if (this.usesFoodState()) {
            this.setIsWrappedBit(isWrapped);
        }
    }

    public boolean isWrapped() {
        if (this.usesFoodState()) {
            return this.isWrappedBitSet();
        }
        return false;
    }

    public void setIsUndistilled(boolean isUndistilled) {
        if (this.usesFoodState()) {
            this.setIsWrappedBit(isUndistilled);
        }
    }

    public boolean isUndistilled() {
        if (this.usesFoodState()) {
            return this.isWrappedBitSet();
        }
        return false;
    }

    public void setIsFresh(boolean isFresh) {
        if (this.isHerb() || this.isSpice()) {
            this.setIsFreshBit(isFresh);
        }
    }

    public boolean isFresh() {
        if (this.isHerb() || this.isSpice()) {
            return this.isFreshBitSet();
        }
        return false;
    }

    public void setIsSalted(boolean isSalted) {
        if (this.usesFoodState() && (this.isDish() || this.isLiquid())) {
            this.setIsFreshBit(isSalted);
            this.updateName();
        }
    }

    public boolean isSalted() {
        if (this.usesFoodState() && (this.isDish() || this.isLiquid())) {
            return this.isFreshBitSet();
        }
        return false;
    }

    public void setIsLive(boolean isLive) {
        if (this.isFish()) {
            this.setIsFreshBit(isLive);
        }
    }

    public boolean isLive() {
        if (this.isFish()) {
            return this.isFreshBitSet();
        }
        return false;
    }

    public boolean isCorrectFoodState(byte cookState, byte physicalState) {
        if (cookState != -1 && ((cookState & 0xF) == 7 ? this.getRightAuxData() == 0 : this.getRightAuxData() != (cookState & 0xF))) {
            return false;
        }
        if (physicalState != -1) {
            if ((physicalState & 0x7F) == 0) {
                return (this.getLeftAuxData() & 7) == (physicalState >>> 4 & 7);
            }
            return this.getLeftAuxData() == (physicalState >>> 4 & 0xF);
        }
        return true;
    }

    public byte getFoodStages() {
        ItemMealData imd = ItemMealData.getItemMealData(this.getWurmId());
        if (imd != null) {
            return imd.getStages();
        }
        return 0;
    }

    public byte getFoodIngredients() {
        ItemMealData imd = ItemMealData.getItemMealData(this.getWurmId());
        if (imd != null) {
            return imd.getIngredients();
        }
        return 0;
    }

    public float getFoodComplexity() {
        float stageDif = (float)this.getFoodStages() / 10.0f;
        float ingredientDif = (float)this.getFoodIngredients() / 30.0f;
        float totalDif = stageDif * ingredientDif * 2.0f;
        return totalDif;
    }

    float calcFoodPercentage() {
        float rarityMod = 1.0f + (float)(this.getRarity() * this.getRarity()) * 0.1f;
        float percentage = this.getCurrentQualityLevel() / 100.0f * rarityMod;
        return percentage;
    }

    public float getCaloriesByWeight() {
        return this.getCaloriesByWeight(this.getWeightGrams());
    }

    public float getCaloriesByWeight(int weight) {
        return (float)(this.getCalories() * weight) / 1000.0f;
    }

    public short getCalories() {
        if (this.getTemplateId() == 488 && this.getRealTemplateId() == 488) {
            return 0;
        }
        float percentage = this.calcFoodPercentage();
        if (!this.template.calcNutritionValues()) {
            return (short)((float)this.template.getCalories() * percentage);
        }
        ItemMealData imd = ItemMealData.getItemMealData(this.getWurmId());
        if (imd != null) {
            return (short)((float)imd.getCalories() * percentage);
        }
        return (short)((float)this.template.getCalories() * percentage);
    }

    public float getCarbsByWeight() {
        return this.getCarbsByWeight(this.getWeightGrams());
    }

    public float getCarbsByWeight(int weight) {
        return (float)(this.getCarbs() * weight) / 1000.0f;
    }

    public short getCarbs() {
        if (this.getTemplateId() == 488 && this.getRealTemplateId() == 488) {
            return 0;
        }
        float percentage = this.calcFoodPercentage();
        if (!this.template.calcNutritionValues()) {
            return (short)((float)this.template.getCarbs() * percentage);
        }
        if (!this.template.calcNutritionValues()) {
            return this.template.getCarbs();
        }
        ItemMealData imd = ItemMealData.getItemMealData(this.getWurmId());
        if (imd != null) {
            return (short)((float)imd.getCarbs() * percentage);
        }
        return (short)((float)this.template.getCarbs() * percentage);
    }

    public float getFatsByWeight() {
        return this.getFatsByWeight(this.getWeightGrams());
    }

    public float getFatsByWeight(int weight) {
        return (float)(this.getFats() * weight) / 1000.0f;
    }

    public short getFats() {
        if (this.getTemplateId() == 488 && this.getRealTemplateId() == 488) {
            return 0;
        }
        float percentage = this.calcFoodPercentage();
        if (!this.template.calcNutritionValues()) {
            return (short)((float)this.template.getFats() * percentage);
        }
        ItemMealData imd = ItemMealData.getItemMealData(this.getWurmId());
        if (imd != null) {
            return (short)((float)imd.getFats() * percentage);
        }
        return (short)((float)this.template.getFats() * percentage);
    }

    public float getProteinsByWeight() {
        return this.getProteinsByWeight(this.getWeightGrams());
    }

    public float getProteinsByWeight(int weight) {
        return (float)(this.getProteins() * weight) / 1000.0f;
    }

    public short getProteins() {
        if (this.getTemplateId() == 488 && this.getRealTemplateId() == 488) {
            return 0;
        }
        float percentage = this.calcFoodPercentage();
        if (!this.template.calcNutritionValues()) {
            return (short)((float)this.template.getProteins() * percentage);
        }
        ItemMealData imd = ItemMealData.getItemMealData(this.getWurmId());
        if (imd != null) {
            return (short)((float)imd.getProteins() * percentage);
        }
        return (short)((float)this.template.getProteins() * percentage);
    }

    public int getBonus() {
        if (this.getTemplateId() == 488 && this.getRealTemplateId() == 488) {
            return -1;
        }
        ItemMealData imd = ItemMealData.getItemMealData(this.getWurmId());
        if (imd != null) {
            return imd.getBonus() & 0xFF;
        }
        return -1;
    }

    @Nullable
    public Recipe getRecipe() {
        if (this.getTemplateId() == 488 && this.getRealTemplateId() == 488) {
            return null;
        }
        ItemMealData imd = ItemMealData.getItemMealData(this.getWurmId());
        if (imd != null && imd.getRecipeId() > -1) {
            return Recipes.getRecipeById(imd.getRecipeId());
        }
        return null;
    }

    public void calculateAndSaveNutrition(@Nullable Item source, Item target, Recipe recipe) {
        Item cooker;
        byte stages = 1;
        byte ingredients = 0;
        if (source != null && !source.isCookingTool()) {
            stages = (byte)(stages + source.getFoodStages());
            ingredients = (byte)(ingredients + (source.getFoodIngredients() + 1));
        }
        if (target.isFoodMaker()) {
            ConcurrentHashMap<Integer, Item> items = new ConcurrentHashMap<Integer, Item>();
            for (Item item : target.getItemsAsArray()) {
                items.put(item.getTemplateId(), item);
            }
            for (Item item : items.values()) {
                stages = (byte)(stages + item.getFoodStages());
                ingredients = (byte)(ingredients + (item.getFoodIngredients() + 1));
            }
        } else {
            stages = (byte)(stages + target.getFoodStages());
            ingredients = (byte)(ingredients + (target.getFoodIngredients() + 1));
        }
        short calories = this.template.getCalories();
        short carbs = this.template.getCarbs();
        short fats = this.template.getFats();
        short proteins = this.template.getProteins();
        if (this.template.calcNutritionValues()) {
            int iweight;
            Object ingredient;
            float caloriesTotal = 0.0f;
            float carbsTotal = 0.0f;
            float fatsTotal = 0.0f;
            float proteinsTotal = 0.0f;
            int weight = 0;
            if (source != null && !source.isCookingTool()) {
                ingredient = recipe.getActiveItem();
                iweight = ingredient == null ? source.getWeightGrams() : (int)((float)source.getWeightGrams() * ((100.0f - (float)((Ingredient)ingredient).getLoss()) / 100.0f));
                caloriesTotal += source.getCaloriesByWeight(iweight);
                carbsTotal += source.getCarbsByWeight(iweight);
                fatsTotal += source.getFatsByWeight(iweight);
                proteinsTotal += source.getProteinsByWeight(iweight);
                weight += iweight;
            }
            if (target.isFoodMaker()) {
                for (Item item : target.getItemsAsArray()) {
                    Ingredient ingredient2 = recipe.findMatchingIngredient(item);
                    int iweight2 = ingredient2 == null ? item.getWeightGrams() : (int)((float)item.getWeightGrams() * ((100.0f - (float)ingredient2.getLoss()) / 100.0f));
                    caloriesTotal += item.getCaloriesByWeight(iweight2);
                    carbsTotal += item.getCarbsByWeight(iweight2);
                    fatsTotal += item.getFatsByWeight(iweight2);
                    proteinsTotal += item.getProteinsByWeight(iweight2);
                    weight += iweight2;
                }
            } else {
                ingredient = recipe.getTargetItem();
                iweight = ingredient == null ? target.getWeightGrams() : (int)((float)target.getWeightGrams() * ((100.0f - (float)((Ingredient)ingredient).getLoss()) / 100.0f));
                caloriesTotal += target.getCaloriesByWeight(iweight);
                carbsTotal += target.getCarbsByWeight(iweight);
                fatsTotal += target.getFatsByWeight(iweight);
                proteinsTotal += target.getProteinsByWeight(iweight);
                weight += iweight;
            }
            float rarityMod = 1.0f + (float)(this.getRarity() * this.getRarity()) * 0.1f;
            calories = (short)(caloriesTotal * 1000.0f / (float)weight * rarityMod);
            carbs = (short)(carbsTotal * 1000.0f / (float)weight * rarityMod);
            fats = (short)(fatsTotal * 1000.0f / (float)weight * rarityMod);
            proteins = (short)(proteinsTotal * 1000.0f / (float)weight * rarityMod);
        }
        int ibonus = 0;
        if (source != null && recipe.hasActiveItem() && recipe.getActiveItem().getTemplateId() != 14) {
            ibonus += source.getTemplateId();
            if (!Server.getInstance().isPS()) {
                ibonus += source.getRarity();
            }
        }
        if (recipe.hasCooker() && (cooker = target.getTopParentOrNull()) != null) {
            ibonus += cooker.getTemplateId();
            if (!Server.getInstance().isPS()) {
                ibonus += cooker.getRarity();
            }
        }
        ibonus += target.getTemplateId();
        if (target.isFoodMaker()) {
            for (Item item : target.getItemsAsArray()) {
                ibonus += item.getTemplateId();
                if (item.usesFoodState()) {
                    ibonus += item.getAuxData();
                }
                ibonus += item.getMaterial();
                ibonus += item.getRealTemplateId();
                if (Server.getInstance().isPS()) continue;
                ibonus += item.getRarity();
            }
        } else {
            if (target.usesFoodState()) {
                ibonus += target.getAuxData();
            }
            ibonus += target.getMaterial();
            ibonus += target.getRealTemplateId();
            if (this.getTemplateId() == 272) {
                ibonus += target.getData1();
            }
            if (!Server.getInstance().isPS()) {
                ibonus += target.getRarity();
            }
        }
        byte bonus = (byte)(ibonus % SkillSystem.getNumberOfSkillTemplates());
        ItemMealData.save(this.getWurmId(), recipe.getRecipeId(), calories, carbs, fats, proteins, bonus, stages, ingredients);
    }

    public void setHarvestable(boolean harvestable) {
        if (this.template.isHarvestable()) {
            if (harvestable && this.isPlanted()) {
                this.setRightAuxData(1);
            } else {
                this.setRightAuxData(0);
            }
            this.updateModelNameOnGroundItem();
        }
    }

    public boolean isHarvestable() {
        return this.template.isHarvestable() && this.getRightAuxData() == 1 && this.isPlanted() && this.getLeftAuxData() > FoliageAge.YOUNG_FOUR.getAgeId() && this.getLeftAuxData() < FoliageAge.OVERAGED.getAgeId();
    }

    public Item getHoney() {
        Item[] items;
        for (Item item : items = this.getItemsAsArray()) {
            if (item.getTemplateId() != 70) continue;
            return item;
        }
        return null;
    }

    public Item getSugar() {
        Item[] items;
        for (Item item : items = this.getItemsAsArray()) {
            if (item.getTemplateId() != 1139) continue;
            return item;
        }
        return null;
    }

    public int getWaxCount() {
        Item[] items;
        int waxCount = 0;
        for (Item item : items = this.getItemsAsArray()) {
            if (item.getTemplateId() != 1254) continue;
            ++waxCount;
        }
        return waxCount;
    }

    public Item getVinegar() {
        Item[] items;
        for (Item item : items = this.getItemsAsArray()) {
            if (item.getTemplateId() != 1246) continue;
            return item;
        }
        return null;
    }

    abstract boolean saveInscription();

    public boolean setInscription(Recipe recipe, String theInscriber, int thePenColour) {
        this.inscription = new InscriptionData(this.getWurmId(), recipe, theInscriber, thePenColour);
        this.setAuxData((byte)1);
        this.setName("\"" + recipe.getName() + "\"", true);
        this.setRarity(recipe.getLootableRarity());
        return this.saveInscription();
    }

    public final boolean isMoonMetal() {
        switch (this.getMaterial()) {
            case 56: 
            case 57: 
            case 67: {
                return true;
            }
        }
        return false;
    }

    public final boolean isAlloyMetal() {
        switch (this.getMaterial()) {
            case 9: 
            case 30: 
            case 31: 
            case 96: {
                return true;
            }
        }
        return false;
    }

    public final String debugLog(int depth) {
        StringBuilder buf = new StringBuilder();
        buf.append(this.toString()).append("\n");
        buf.append("Last Owner: ").append(this.lastOwner).append(" | Owner: ").append(this.ownerId).append(" | Parent: ").append(this.parentId).append("\n");
        buf.append("Material: ").append(Item.getMaterialString(this.getMaterial())).append(" | Rarity: ").append(this.getRarity()).append(" | Description: ").append(this.getDescription()).append("\n");
        try {
            StackTraceElement[] steArr = new Throwable().getStackTrace();
            for (int i = 1; i < steArr.length && i < depth; ++i) {
                buf.append(steArr[i].getClassName()).append(".").append(steArr[i].getMethodName()).append(":").append(steArr[i].getLineNumber()).append("\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    public final boolean isSorceryItem() {
        switch (this.getTemplateId()) {
            case 794: 
            case 795: 
            case 796: 
            case 797: 
            case 798: 
            case 799: 
            case 800: 
            case 801: 
            case 802: 
            case 803: 
            case 804: 
            case 805: 
            case 806: 
            case 807: 
            case 808: 
            case 809: 
            case 810: {
                return true;
            }
        }
        return false;
    }

    public final boolean isEnchanted() {
        return this.getSpellEffects() != null && this.getSpellEffects().getEffects().length > 0;
    }

    public final boolean isDebugLogged() {
        return this.rarity > 0 || this.isMoonMetal() || this.isEnchanted() || this.isSorceryItem() || !this.getDescription().isEmpty() && !this.isBulkItem() && !this.isCorpse() && !this.isTemporary() || !this.isBodyPart() && !this.isFood() && !this.isMushroom() && !this.isTypeRecycled() && !this.isCorpse() && !this.isInventory() && !this.isBulk() && !this.isBulkItem() && this.getTemplateId() != 177 && this.getLastOwnerId() > 0L && this.getTemplateId() != 314 && this.getTemplateId() != 1392;
    }

    public void setLightOverride(boolean lightOverride) {
        this.isLightOverride = lightOverride;
    }

    public boolean isLightOverride() {
        return this.isLightOverride;
    }

    public Item getOuterItemOrNull() {
        if (!this.getTemplate().isComponentItem()) {
            return this;
        }
        if (this.getParentOrNull() == null) {
            return null;
        }
        return this.getParentOrNull().getOuterItemOrNull();
    }

    public Item getParentOuterItemOrNull() {
        Item parent = this.getParentOrNull();
        if (parent != null) {
            return parent.getOuterItemOrNull();
        }
        return null;
    }

    public int getMaxItemCount() {
        Item outer;
        if (this.getTemplateId() == 1295 && (outer = this.getOuterItemOrNull()) != null) {
            return outer.getMaxItemCount();
        }
        return this.getTemplate().getMaxItemCount();
    }

    public int getMaxItemWeight() {
        Item outer;
        if (this.getTemplateId() == 1295 && (outer = this.getOuterItemOrNull()) != null) {
            return outer.getMaxItemWeight();
        }
        return this.getTemplate().getMaxItemWeight();
    }

    public boolean canHold(Item target) {
        if (this.getMaxItemCount() > -1 && this.getItemCount() >= this.getMaxItemCount()) {
            return false;
        }
        return this.getMaxItemWeight() <= -1 || this.getFullWeight() - this.getWeightGrams() + target.getFullWeight() <= this.getMaxItemWeight();
    }

    public boolean isInsulated() {
        Item parent = this.getParentOrNull();
        return parent != null && parent.getTemplate().isInsulated();
    }

    public boolean isInLunchbox() {
        if (!this.canLarder() && !this.isLiquid()) {
            return false;
        }
        Item lunchbox = this.getParentOuterItemOrNull();
        return lunchbox != null && (lunchbox.getTemplateId() == 1296 || lunchbox.getTemplateId() == 1297);
    }

    public boolean isInTacklebox() {
        Item parent = this.getParentOrNull();
        if (parent == null || parent.getTemplateId() == 1341) {
            return false;
        }
        Item container = this.getParentOuterItemOrNull();
        return container != null && container.getTemplateId() == 1341;
    }

    public short getWinches() {
        return this.warmachineWinches;
    }

    public void setWinches(short newWinches) {
        this.warmachineWinches = newWinches;
    }

    public void setReplacing(boolean replacing) {
        this.replacing = replacing;
    }

    public boolean isReplacing() {
        return this.replacing;
    }

    public void setIsSealedOverride(boolean overrideSealed) {
        this.isSealedOverride = overrideSealed;
        this.closeAll();
        this.updateName();
    }

    public boolean isSealedOverride() {
        return this.isSealedOverride;
    }

    public void setWhatHappened(String destroyReason) {
        this.whatHappened = destroyReason;
    }

    public String getWhatHappened() {
        return this.whatHappened;
    }

    public void setWasBrandedTo(long wasBrandedTo) {
        this.wasBrandedTo = wasBrandedTo;
    }

    public long getWasBrandedTo() {
        return this.wasBrandedTo;
    }

    public boolean isSaddleBags() {
        return this.getTemplate().isSaddleBags();
    }

    public boolean isFishingReel() {
        return this.getTemplate().isFishingReel();
    }

    public boolean isFishingLine() {
        return this.getTemplate().isFishingLine();
    }

    public boolean isFishingFloat() {
        return this.getTemplate().isFishingFloat();
    }

    public boolean isFishingHook() {
        return this.getTemplate().isFishingHook();
    }

    public boolean isFishingBait() {
        return this.getTemplate().isFishingBait();
    }

    public Item[] getFishingItems() {
        Item fishingReel = null;
        Item fishingLine = null;
        Item fishingFloat = null;
        Item fishingHook = null;
        Item fishingBait = null;
        if (this.getTemplateId() == 1344 || this.getTemplateId() == 1346) {
            if (this.getTemplateId() == 1344) {
                fishingLine = this.getFishingLine();
            } else {
                fishingReel = this.getFishingReel();
                if (fishingReel != null) {
                    fishingLine = fishingReel.getFishingLine();
                }
            }
            if (fishingLine != null) {
                fishingFloat = fishingLine.getFishingFloat();
                fishingHook = fishingLine.getFishingHook();
            }
            if (fishingHook != null) {
                fishingBait = fishingHook.getFishingBait();
            }
        }
        return new Item[]{fishingReel, fishingLine, fishingFloat, fishingHook, fishingBait};
    }

    @Nullable
    public Item getFishingReel() {
        Item[] containsItems = this.getItemsAsArray();
        if (containsItems.length > 0) {
            for (Item contains : containsItems) {
                if (!contains.isFishingReel()) continue;
                return contains;
            }
        }
        return null;
    }

    @Nullable
    public Item getFishingLine() {
        Item[] containsItems = this.getItemsAsArray();
        if (containsItems.length > 0) {
            for (Item contains : containsItems) {
                if (!contains.isFishingLine()) continue;
                return contains;
            }
        }
        return null;
    }

    public String getFishingLineName() {
        String lineName = "";
        switch (this.getTemplateId()) {
            case 1372: {
                lineName = "light fishing line";
                break;
            }
            case 1373: {
                lineName = "medium fishing line";
                break;
            }
            case 1374: {
                lineName = "heavy fishing line";
                break;
            }
            case 1375: {
                lineName = "braided fishing line";
            }
        }
        return lineName;
    }

    @Nullable
    public Item getFishingHook() {
        Item[] containsItems = this.getItemsAsArray();
        if (containsItems.length > 0) {
            for (Item contains : containsItems) {
                if (!contains.isFishingHook()) continue;
                return contains;
            }
        }
        return null;
    }

    @Nullable
    public Item getFishingFloat() {
        Item[] containsItems = this.getItemsAsArray();
        if (containsItems.length > 0) {
            for (Item contains : containsItems) {
                if (!contains.isFishingFloat()) continue;
                return contains;
            }
        }
        return null;
    }

    @Nullable
    public Item getFishingBait() {
        Item[] containsItems = this.getItemsAsArray();
        if (containsItems.length > 0) {
            for (Item contains : containsItems) {
                if (!contains.isFishingBait()) continue;
                return contains;
            }
        }
        return null;
    }

    public boolean isFlyTrap() {
        if (this.getTemplateId() != 76) {
            return false;
        }
        Item[] items = this.getItemsAsArray();
        if (items.length >= 1) {
            boolean hasHoney = false;
            boolean hasVinegar = false;
            int count = 0;
            boolean contaminated = false;
            for (Item contained : items) {
                if (contained.getTemplateId() == 70) {
                    hasHoney = true;
                    continue;
                }
                if (contained.getTemplateId() == 1246) {
                    hasVinegar = true;
                    continue;
                }
                if (contained.getTemplateId() == 1359) {
                    ++count;
                    continue;
                }
                contaminated = true;
            }
            if (hasHoney && hasVinegar) {
                contaminated = true;
            }
            return (hasHoney || hasVinegar) && !contaminated && count < 99;
        }
        return false;
    }

    public boolean isInside(int ... itemTemplateIds) {
        Item parent = this.getParentOrNull();
        if (parent != null) {
            for (int itemTemplateId : itemTemplateIds) {
                if (parent.getTemplateId() != itemTemplateId) continue;
                return true;
            }
            return parent.isInside(itemTemplateIds);
        }
        return false;
    }

    public Item getFirstParent(int ... itemTemplateIds) {
        Item parent = this.getParentOrNull();
        if (parent != null) {
            for (int itemTemplateId : itemTemplateIds) {
                if (parent.getTemplateId() != itemTemplateId) continue;
                return parent;
            }
            return parent.getFirstParent(itemTemplateIds);
        }
        return null;
    }

    public boolean isInsidePlacedContainer() {
        if (this.getParentOrNull() != null) {
            if (this.getParentOrNull().isPlacedOnParent()) {
                return true;
            }
            return this.getParentOrNull().isInsidePlacedContainer();
        }
        return false;
    }

    public boolean isInsidePlaceableContainer() {
        return this.getParentOrNull() != null && this.getParentOrNull().getTemplate().hasViewableSubItems() && (!this.getParentOrNull().getTemplate().isContainerWithSubItems() || this.isPlacedOnParent()) && this.getTopParent() == this.getParentId();
    }

    public boolean isProcessedFood() {
        return this.getName().contains("mashed ") || this.getName().contains("chopped ");
    }

    public boolean doesShowSlopes() {
        return this.getTemplate().doesShowSlopes();
    }

    public boolean supportsSecondryColor() {
        return this.getTemplate().supportsSecondryColor();
    }

    public float getMaterialImpBonus() {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (this.getMaterial()) {
                case 30: {
                    return 1.025f;
                }
                case 10: {
                    return 1.05f;
                }
                case 12: {
                    return 1.1f;
                }
                case 34: {
                    return 1.025f;
                }
                case 13: {
                    return 1.075f;
                }
            }
        }
        return 1.0f;
    }

    public static float getMaterialCreationBonus(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 30: {
                    return 0.1f;
                }
                case 31: {
                    return 0.05f;
                }
                case 12: {
                    return 0.05f;
                }
                case 34: {
                    return 0.05f;
                }
            }
        }
        return 0.0f;
    }

    public static float getMaterialLockpickBonus(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 56: {
                    return 0.05f;
                }
                case 10: {
                    return -0.05f;
                }
                case 57: {
                    return 0.05f;
                }
                case 7: {
                    return -0.025f;
                }
                case 12: {
                    return -0.05f;
                }
                case 67: {
                    return 0.05f;
                }
                case 8: {
                    return 0.025f;
                }
                case 9: {
                    return 0.05f;
                }
                case 34: {
                    return -0.025f;
                }
                case 13: {
                    return -0.025f;
                }
            }
        } else {
            if (material == 10) {
                return -0.05f;
            }
            if (material == 9) {
                return 0.05f;
            }
        }
        return 0.0f;
    }

    public static float getMaterialAnchorBonus(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 56: {
                    return 1.5f;
                }
                case 30: {
                    return 0.9f;
                }
                case 31: {
                    return 0.85f;
                }
                case 10: {
                    return 0.95f;
                }
                case 57: {
                    return 1.25f;
                }
                case 7: {
                    return 1.7f;
                }
                case 11: {
                    return 0.85f;
                }
                case 12: {
                    return 1.0f;
                }
                case 67: {
                    return 1.25f;
                }
                case 8: {
                    return 0.975f;
                }
                case 9: {
                    return 0.85f;
                }
                case 34: {
                    return 0.8f;
                }
                case 13: {
                    return 0.75f;
                }
                case 96: {
                    return 1.0f;
                }
            }
        }
        return 1.0f;
    }

    public int getMaxPlaceableItems() {
        return (this.getContainerSizeY() + this.getContainerSizeZ()) / 10;
    }

    public boolean getAuxBit(int bitLoc) {
        return (this.getAuxData() >> bitLoc & 1) == 1;
    }

    public void setAuxBit(int bitLoc, boolean value) {
        if (!value) {
            this.setAuxData((byte)(this.getAuxData() & ~(1 << bitLoc)));
        } else {
            this.setAuxData((byte)(this.getAuxData() | 1 << bitLoc));
        }
    }

    public boolean isPlacedOnParent() {
        return this.placedOnParent;
    }

    public abstract void setPlacedOnParent(boolean var1);

    @Nullable
    public Item recursiveParentCheck() {
        Item parent = this.getParentOrNull();
        if (parent != null) {
            if (parent.getTemplate().hasViewableSubItems() && (!parent.getTemplate().isContainerWithSubItems() || this.isPlacedOnParent())) {
                return this;
            }
            return parent.recursiveParentCheck();
        }
        return null;
    }

    public void setChained(boolean chained) {
        this.isChained = chained;
    }

    public boolean isChained() {
        return this.isChained;
    }

    public void addSnowmanItem() {
        if (this.getTemplateId() != 1437) {
            return;
        }
        if (!this.isEmpty(true)) {
            return;
        }
        int snowId = 1276;
        float rand = Server.rand.nextFloat() * 100.0f;
        if (rand < 1.0E-4f) {
            snowId = 381;
        } else if (rand < 0.001f) {
            snowId = 380;
        } else if (rand < 0.01f) {
            snowId = 1397;
        } else if (rand < 0.02f) {
            snowId = 205;
        }
        try {
            Item toPlace = ItemFactory.createItem(snowId, Server.rand.nextFloat() * 50.0f + 50.0f, (byte)0, null);
            toPlace.setPos(0.0f, 0.0f, 0.8f, Server.rand.nextFloat() * 360.0f, this.onBridge());
            toPlace.setLastOwnerId(this.getWurmId());
            if (this.insertItem(toPlace, false, false, true)) {
                VolaTile vt = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
                if (vt != null) {
                    for (VirtualZone vz : vt.getWatchers()) {
                        if (!vz.isVisible(this, vt)) continue;
                        vz.getWatcher().getCommunicator().sendItem(toPlace, -10L, false);
                        if (toPlace.isLight() && toPlace.isOnFire()) {
                            vt.addLightSource(toPlace);
                        }
                        if (toPlace.getEffects().length > 0) {
                            for (Effect e : toPlace.getEffects()) {
                                vz.addEffect(e, false);
                            }
                        }
                        if (toPlace.getColor() != -1) {
                            vz.sendRepaint(toPlace.getWurmId(), (byte)WurmColor.getColorRed(toPlace.getColor()), (byte)WurmColor.getColorGreen(toPlace.getColor()), (byte)WurmColor.getColorBlue(toPlace.getColor()), (byte)-1, (byte)0);
                        }
                        if (toPlace.getColor2() == -1) continue;
                        vz.sendRepaint(toPlace.getWurmId(), (byte)WurmColor.getColorRed(toPlace.getColor2()), (byte)WurmColor.getColorGreen(toPlace.getColor2()), (byte)WurmColor.getColorBlue(toPlace.getColor2()), (byte)-1, (byte)1);
                    }
                }
            } else {
                Items.destroyItem(toPlace.getWurmId());
            }
        }
        catch (FailedException | NoSuchTemplateException wurmServerException) {
            // empty catch block
        }
    }
}

