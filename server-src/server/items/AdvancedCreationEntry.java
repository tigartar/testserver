/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.EpicTargetItems;
import com.wurmonline.server.epic.MissionHelper;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationRequirement;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemContainerRequirement;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemRequirement;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemVicinityRequirement;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AdvancedCreationEntry
extends CreationEntry {
    private static final Logger logger = Logger.getLogger(AdvancedCreationEntry.class.getName());
    private Set<CreationRequirement> requirements;

    public AdvancedCreationEntry(int aPrimarySkill, int aObjectSource, int aObjectTarget, int aObjectCreated, boolean aDestroyTarget, boolean aUseCapacity, float aPercentageLost, int aMinTimeSeconds, boolean aDestroyBoth, boolean aCreateOnGround, CreationCategories aCategory) {
        super(aPrimarySkill, aObjectSource, aObjectTarget, aObjectCreated, aDestroyTarget, aUseCapacity, aPercentageLost, aMinTimeSeconds, aDestroyBoth, aCreateOnGround, aCategory);
    }

    public AdvancedCreationEntry(int aPrimarySkill, int aObjectSource, int aObjectTarget, int aObjectCreated, boolean aDepleteSource, boolean aDepleteTarget, float aPercentageLost, boolean aDepleteBoth, boolean aCreateOnGround, CreationCategories aCategory) {
        super(aPrimarySkill, aObjectSource, aObjectTarget, aObjectCreated, aDepleteSource, aDepleteTarget, aDepleteBoth, aPercentageLost, aCreateOnGround, aCategory);
    }

    public AdvancedCreationEntry(int aPrimarySkill, int aObjectSource, int aObjectTarget, int aObjectCreated, boolean aDepleteSource, boolean aDepleteTarget, float aPercentageLost, boolean aDepleteBoth, boolean aCreateOnGround, int aCustomCutOffChance, double aMinimumSkill, CreationCategories aCategory) {
        super(aPrimarySkill, aObjectSource, aObjectTarget, aObjectCreated, aDepleteSource, aDepleteTarget, aDepleteBoth, aPercentageLost, aCreateOnGround, aCustomCutOffChance, aMinimumSkill, aCategory);
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public CreationEntry cloneAndRevert() {
        AdvancedCreationEntry toReturn = new AdvancedCreationEntry(this.getPrimarySkill(), this.getObjectTarget(), this.getObjectSource(), this.getObjectCreated(), this.isDestroyTarget(), this.isUseCapacity(), this.getPercentageLost(), this.getMinTimeSeconds(), this.isDestroyBoth(), this.isCreateOnGround(), this.getCategory());
        return toReturn;
    }

    public AdvancedCreationEntry addRequirement(CreationRequirement requirement) {
        if (this.requirements == null) {
            this.requirements = new HashSet<CreationRequirement>();
        }
        this.requirements.add(requirement);
        return this;
    }

    @Override
    public CreationRequirement[] getRequirements() {
        if (this.requirements == null) {
            return emptyReqs;
        }
        return this.requirements.toArray(new CreationRequirement[this.requirements.size()]);
    }

    public boolean isRequirementFilled(CreationRequirement requirement, Creature creature) {
        boolean toReturn = false;
        return false;
    }

    public boolean areRequirementsFilled(Item target) {
        CreationRequirement[] reqs;
        for (CreationRequirement lReq : reqs = this.getRequirements()) {
            if (AdvancedCreationEntry.getStateForRequirement(lReq, target) >= lReq.getResourceNumber()) continue;
            return false;
        }
        return true;
    }

    public boolean isItemNeeded(Item item, Item target) {
        CreationRequirement[] reqs;
        for (CreationRequirement lReq : reqs = this.getRequirements()) {
            if (item.getTemplateId() != lReq.getResourceTemplateId()) continue;
            return AdvancedCreationEntry.getStateForRequirement(lReq, target) < lReq.getResourceNumber();
        }
        return false;
    }

    public CreationRequirement getRequirementForItem(Item item) {
        CreationRequirement[] reqs;
        for (CreationRequirement lReq : reqs = this.getRequirements()) {
            if (item.getTemplateId() != lReq.getResourceTemplateId()) continue;
            return lReq;
        }
        return null;
    }

    public boolean runThroughRequirement(CreationRequirement req, Creature performer, Item target) {
        boolean toReturn = false;
        int state = AdvancedCreationEntry.getStateForRequirement(req, target);
        if (state != req.getResourceNumber() && req.runOnce(performer)) {
            AdvancedCreationEntry.setStateForRequirement(req, ++state, target);
        }
        return false;
    }

    public static int getTemplateId(Item target) {
        if (target.realTemplate > 0) {
            return target.realTemplate;
        }
        int data = target.getData1();
        int tid = data >> 16 & 0xFFFF;
        return tid;
    }

    public static void setTemplateId(Item target, int templateId) {
        int data = target.getData1();
        int reqOne = data & 0xFFFF;
        int toSet = templateId << 16;
        data = toSet + reqOne;
        target.setData1(data);
        target.setRealTemplate(templateId);
    }

    public String getItemsLeft(Item target) {
        ItemTemplate template = null;
        try {
            template = ItemTemplateFactory.getInstance().getTemplate(this.objectCreated);
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, "No template with id " + this.objectCreated);
        }
        CreationRequirement[] reqs = this.getRequirements();
        StringBuilder buf = new StringBuilder();
        buf.append("The ");
        buf.append(template != null ? template.getName() : "");
        if (template != null && template.getName().charAt(template.getName().length() - 1) == 's') {
            buf.append(" need ");
        } else {
            buf.append(" needs ");
        }
        int itemsNeeded = 0;
        for (CreationRequirement lReq : reqs) {
            int rest = lReq.getResourceNumber() - AdvancedCreationEntry.getStateForRequirement(lReq, target);
            if (rest <= 0) continue;
            int templateNeeded = lReq.getResourceTemplateId();
            ItemTemplate needed = null;
            try {
                needed = ItemTemplateFactory.getInstance().getTemplate(templateNeeded);
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "No template with id " + templateNeeded);
                return "You can't figure out what is needed to complete this object.";
            }
            if (itemsNeeded > 0) {
                buf.append(", and ");
            }
            buf.append(rest);
            buf.append(" ");
            buf.append(needed.sizeString);
            if (needed.isMetal() && needed.getTemplateId() != 897) {
                if (needed.getMaterial() != 0) {
                    buf.append(Item.getMaterialString(needed.getMaterial()));
                    buf.append(" ");
                } else {
                    buf.append("metal ");
                }
            }
            buf.append(needed.getName());
            ++itemsNeeded;
        }
        if (itemsNeeded == 0) {
            buf.append("no more items");
        }
        buf.append(" to be finished.");
        return buf.toString();
    }

    public static int getStateForRequirement(CreationRequirement req, Item target) {
        int data = -1;
        int numberDone = -1;
        if (req.getNumber() == 1) {
            data = target.getData1();
            data = Math.max(0, data);
            numberDone = data & 0xFFFF;
        } else if (req.getNumber() == 2) {
            data = target.getData2();
            data = Math.max(0, data);
            numberDone = data >> 16 & 0xFFFF;
        } else if (req.getNumber() == 3) {
            data = target.getData2();
            data = Math.max(0, data);
            numberDone = data & 0xFFFF;
        } else {
            return ItemRequirement.getStateForRequirement(req.getResourceTemplateId(), target.getWurmId());
        }
        return numberDone;
    }

    private static void setStateForRequirement(CreationRequirement req, int state, Item target) {
        int data = -1;
        if (req.getNumber() == 1) {
            data = target.getData1();
            int tid = data >> 16 & 0xFFFF;
            int toSet = state & 0xFFFF;
            data = ((tid & 0xFFFF) << 16) + toSet;
            target.setData1(data);
        } else if (req.getNumber() == 2) {
            data = target.getData2();
            int reqthree = data & 0xFFFF;
            int toSet = (state & 0xFFFF) << 16;
            data = reqthree + toSet;
            target.setData2(data);
        } else if (req.getNumber() == 3) {
            data = target.getData2();
            int reqtwo = data >> 16 & 0xFFFF;
            int toSet = state & 0xFFFF;
            data = ((reqtwo & 0xFFFF) << 16) + toSet;
            target.setData2(data);
        } else {
            ItemRequirement.setRequirements(target.getWurmId(), req.getResourceTemplateId(), state, true, false);
        }
    }

    @Override
    public int getTotalNumberOfItems() {
        int toReturn = 0;
        if (this.requirements != null) {
            for (CreationRequirement req : this.requirements) {
                toReturn += req.getResourceNumber();
            }
        }
        ++toReturn;
        if (this.depleteTarget && this.depleteSource) {
            ++toReturn;
        } else if (this.destroyBoth) {
            ++toReturn;
        }
        return toReturn;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Item cont(Creature performer, Item source, long targetId, float counter) throws FailedException, NoSuchSkillException, NoSuchItemException, IllegalArgumentException {
        Item newItem;
        boolean failed;
        block131: {
            int time;
            float failSecond;
            Action act;
            Skill primSkill;
            int state;
            CreationRequirement req;
            ItemTemplate template;
            Item realTarget;
            Item realSource;
            block130: {
                block134: {
                    int maxwest;
                    int maxsouth;
                    int maxnorth;
                    int n;
                    Item[] itemArray;
                    VolaTile tile;
                    block135: {
                        block132: {
                            Item target;
                            block133: {
                                VolaTile t;
                                target = Items.getItem(targetId);
                                realSource = source;
                                realTarget = target;
                                template = null;
                                failed = false;
                                try {
                                    template = ItemTemplateFactory.getInstance().getTemplate(this.objectCreated);
                                }
                                catch (NoSuchTemplateException nst) {
                                    logger.log(Level.WARNING, performer.getName() + " - no template with id " + this.objectCreated);
                                    performer.getCommunicator().sendAlertServerMessage("You cannot continue your work, since nobody knows how to create these any longer.");
                                    throw new NoSuchItemException("No such template");
                                }
                                if (this.isRestrictedToDeityFollower() && (performer.getDeity() == null || performer.getDeity().getTemplateDeity() != this.getDeityRestriction())) {
                                    String deityName = Deities.getDeityName(this.getDeityRestriction());
                                    String message = "You must be a follower of %s or one of their demigods to continue on this item.";
                                    performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You must be a follower of %s or one of their demigods to continue on this item.", deityName));
                                    throw new NoSuchItemException("Incorrect deity");
                                }
                                int templateId = -1;
                                if (target.getTemplateId() == 179) {
                                    templateId = AdvancedCreationEntry.getTemplateId(target);
                                    if (templateId == this.objectCreated) {
                                        realTarget = target;
                                        realSource = source;
                                    }
                                } else if (source.getTemplateId() == 179 && (templateId = AdvancedCreationEntry.getTemplateId(source)) == this.objectCreated) {
                                    realTarget = source;
                                    realSource = target;
                                }
                                if ((req = this.getRequirementForItem(realSource)) == null) {
                                    performer.getCommunicator().sendNormalServerMessage("This item will not benefit from adding " + realSource.getNameWithGenus() + ".");
                                    throw new NoSuchItemException("This item will not benefit from adding " + realSource.getNameWithGenus() + ".");
                                }
                                state = AdvancedCreationEntry.getStateForRequirement(req, realTarget);
                                if (state >= req.getResourceNumber()) {
                                    performer.getCommunicator().sendNormalServerMessage("This item will not benefit from adding " + realSource.getNameWithGenus() + ".");
                                    throw new NoSuchItemException("This item will not benefit from adding " + realSource.getNameWithGenus() + ".");
                                }
                                if (realTarget.getDamage() > 0.0f) {
                                    String message = "You must repair the " + realTarget.getName() + " before continuing.";
                                    performer.getCommunicator().sendNormalServerMessage(message);
                                    throw new NoSuchItemException(message);
                                }
                                Skills skills = performer.getSkills();
                                primSkill = null;
                                act = null;
                                try {
                                    act = performer.getCurrentAction();
                                }
                                catch (NoSuchActionException nsa) {
                                    logger.log(Level.WARNING, "This action doesn't exist? " + performer.getName(), nsa);
                                    throw new NoSuchItemException("An error occured on the server. This action was not found. Please report.");
                                }
                                failSecond = Float.MAX_VALUE;
                                try {
                                    primSkill = skills.getSkill(this.primarySkill);
                                }
                                catch (Exception ex) {
                                    primSkill = skills.learn(this.primarySkill, 1.0f);
                                }
                                if (this.hasMinimumSkillRequirement() && primSkill.getKnowledge(0.0) < this.getMinimumSkillRequirement()) {
                                    performer.getCommunicator().sendNormalServerMessage("You are not skilled enough to continue building the " + template.getName() + ".");
                                    throw new NoSuchItemException("Not skilled enough.");
                                }
                                time = 10;
                                if (counter != 1.0f) break block132;
                                if ((template.onePerTile || this.createOnGround) && !MethodsItems.mayDropOnTile(performer)) {
                                    performer.getCommunicator().sendNormalServerMessage("You cannot create that item here, since there is not enough space in front of you.");
                                    throw new NoSuchItemException("Already contains a large item.");
                                }
                                if (template.isOutsideOnly()) {
                                    t = performer.getCurrentTile();
                                    if (t != null && t.getStructure() != null) {
                                        if (!t.getStructure().isTypeBridge()) {
                                            performer.getCommunicator().sendNormalServerMessage("You cannot create that item inside.");
                                            throw new NoSuchItemException("Can't create inside.");
                                        }
                                        if (performer.getBridgeId() != -10L) {
                                            performer.getCommunicator().sendNormalServerMessage("You cannot create that item on a bridge.");
                                            throw new NoSuchItemException("Can't create on a bridge.");
                                        }
                                    }
                                } else if (template.insideOnly && ((t = performer.getCurrentTile()) == null || t.getStructure() == null)) {
                                    performer.getCommunicator().sendNormalServerMessage("You must create that item inside.");
                                    throw new NoSuchItemException("Must create inside.");
                                }
                                if (template.isDomainItem() && performer.getDeity() == null) {
                                    performer.getCommunicator().sendAlertServerMessage("You cannot continue your work, since you lack the faith needed.", (byte)3);
                                    throw new NoSuchItemException("No deity.");
                                }
                                if (template.nonDeedable && performer.getCurrentVillage() != null) {
                                    performer.getCommunicator().sendAlertServerMessage("You can't continue the " + template.getName() + " now; you can't build this in a settlement.", (byte)3);
                                    throw new NoSuchItemException("In settlement.");
                                }
                                if (!template.isGuardTower()) break block133;
                                GuardTower.canConstructTower(performer, realTarget);
                                break block134;
                            }
                            if (!template.protectionTower) break block134;
                            if (!performer.isOnSurface() || !target.isOnSurface()) {
                                performer.getCommunicator().sendAlertServerMessage("You can't continue the tower now; you can't build this below surface.");
                                throw new NoSuchItemException("Below surface.");
                            }
                            VolaTile targTile = Zones.getTileOrNull(realTarget.getTileX(), realTarget.getTileY(), true);
                            if (targTile != null && targTile.isTransition()) {
                                performer.getCommunicator().sendAlertServerMessage("You can't continue the tower here - the foundation is not stable enough.");
                                throw new NoSuchItemException("On cave opening.");
                            }
                            tile = performer.getCurrentTile();
                            if (tile == null) break block134;
                            if (Terraforming.isTileUnderWater(1, tile.tilex, tile.tiley, performer.isOnSurface())) {
                                performer.getCommunicator().sendAlertServerMessage("You can't continue the tower now; the ground is not solid here.");
                                throw new NoSuchItemException("Too wet.");
                            }
                            int mindist = Kingdoms.minKingdomDist;
                            if (Zones.getKingdom(tile.tilex, tile.tiley) == performer.getKingdomId()) {
                                mindist = 60;
                            }
                            if (Kingdoms.isTowerTooNear(tile.tilex, tile.tiley, tile.isOnSurface(), template.protectionTower)) {
                                performer.getCommunicator().sendAlertServerMessage("You can't continue the tower now; another tower is too near.");
                                throw new NoSuchItemException("Too close to another tower.");
                            }
                            if (!template.protectionTower && !Zones.isKingdomBlocking(tile.tilex - mindist, tile.tiley - mindist, tile.tilex + mindist, tile.tiley + mindist, performer.getKingdomId())) {
                                performer.getCommunicator().sendAlertServerMessage("You can't continue the tower now; another kingdom is too near.");
                                throw new NoSuchItemException("Too close to another kingdom.");
                            }
                            if (Servers.localServer.HOMESERVER) break block134;
                            if (Terraforming.isTileModBlocked(performer, tile.tilex, tile.tiley, performer.isOnSurface())) {
                                throw new NoSuchItemException("Tile protected by the deities in this area.");
                            }
                            itemArray = Items.getWarTargets();
                            n = itemArray.length;
                            break block135;
                        }
                        time = act.getTimeLeft();
                        if (act.mayPlaySound()) {
                            MethodsItems.sendImproveSound(performer, realSource, realTarget, this.primarySkill);
                        }
                        break block130;
                    }
                    for (int i = 0; i < n; ++i) {
                        Item targ = itemArray[i];
                        int maxnorth2 = Math.max(0, tile.tiley - 60);
                        int maxsouth2 = Math.min(Zones.worldTileSizeY, tile.tiley + 60);
                        int maxwest2 = Math.max(0, tile.tilex - 60);
                        int maxeast = Math.min(Zones.worldTileSizeX, tile.tilex + 60);
                        if ((int)targ.getPosX() >> 2 <= maxwest2 || (int)targ.getPosX() >> 2 >= maxeast || (int)targ.getPosY() >> 2 >= maxsouth2 || (int)targ.getPosY() >> 2 <= maxnorth2) continue;
                        performer.getCommunicator().sendSafeServerMessage("You cannot found the tower here, since this is an active battle ground.");
                        throw new NoSuchItemException("Too close to a war target.");
                    }
                    EndGameItem alt = EndGameItems.getEvilAltar();
                    if (alt != null) {
                        maxnorth = Math.max(0, tile.tiley - 100);
                        maxsouth = Math.min(Zones.worldTileSizeY, tile.tiley + 100);
                        int maxeast = Math.max(0, tile.tilex - 100);
                        maxwest = Math.min(Zones.worldTileSizeX, tile.tilex + 100);
                        if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                            throw new NoSuchItemException("You cannot place a tower here, since this is holy ground.");
                        }
                    }
                    if ((alt = EndGameItems.getGoodAltar()) != null) {
                        maxnorth = Math.max(0, tile.tiley - 100);
                        maxsouth = Math.min(Zones.worldTileSizeY, tile.tiley + 100);
                        int maxeast = Math.max(0, tile.tilex - 100);
                        maxwest = Math.min(Zones.worldTileSizeX, tile.tilex + 100);
                        if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                            throw new NoSuchItemException("You cannot place a tower here, since this is holy ground.");
                        }
                    }
                }
                Item parent = null;
                try {
                    parent = realTarget.getParent();
                }
                catch (NoSuchItemException tile) {
                    // empty catch block
                }
                if (parent != null) {
                    if (parent.isNoWorkParent()) {
                        performer.getCommunicator().sendNormalServerMessage("You can't work with the " + realTarget.getName() + " in the " + parent.getName() + ".");
                        throw new NoSuchItemException("The " + realTarget.getName() + " can't be modified in the " + parent.getName() + ".");
                    }
                    if (template.isHollow() && (template.getSizeZ() >= parent.getSizeZ() || template.getSizeY() >= parent.getSizeY() || template.getSizeX() >= parent.getSizeX())) {
                        performer.getCommunicator().sendNormalServerMessage("The " + realTarget.getName() + " will not fit in the " + parent.getName() + ".");
                        throw new NoSuchItemException("The " + realTarget.getName() + " will not fit in the " + parent.getName() + ".");
                    }
                }
                try {
                    if (realSource.getTemplateId() == 9) {
                        if ((double)realSource.getWeightGrams(false) < (double)realSource.getTemplate().getWeightGrams() * 0.7) {
                            performer.getCommunicator().sendNormalServerMessage("The " + realSource.getName() + " you try to use contains too little material. You need a heavier " + realSource.getName() + ".");
                            throw new NoSuchItemException("Too little material");
                        }
                    } else {
                        if (realSource.getWeightGrams(false) < realSource.getTemplate().getWeightGrams()) {
                            if (realSource.isCombine()) {
                                performer.getCommunicator().sendNormalServerMessage("The " + realSource.getName() + " contains too little material. Please combine with other of the same kind or select another one.");
                                throw new NoSuchItemException("Too little material");
                            }
                            performer.getCommunicator().sendNormalServerMessage("The " + realSource.getName() + " contains too little material. Please select another one.");
                            throw new NoSuchItemException("Too little material");
                        }
                        if (realTarget.getWeightGrams(false) < realTarget.getTemplate().getWeightGrams()) {
                            if (realTarget.isCombine()) {
                                performer.getCommunicator().sendNormalServerMessage("The " + realTarget.getName() + " contains too little material to create " + template.getNameWithGenus() + ". Please combine with other of the same kind or select another one.");
                                throw new NoSuchItemException("Too little material.");
                            }
                            performer.getCommunicator().sendNormalServerMessage("The " + realTarget.getName() + " contains too little material. Please select another one.");
                            throw new NoSuchItemException("Too little material.");
                        }
                    }
                    realTarget.setBusy(true);
                    time = Actions.getItemCreationTime(200, performer, primSkill, this, realSource, realTarget, template.isMassProduction());
                    act.setTimeLeft(time);
                    double bonus = performer.getVillageSkillModifier();
                    float alc = 0.0f;
                    if (performer.isPlayer()) {
                        alc = ((Player)performer).getAlcohol();
                    }
                    float power = (float)primSkill.skillCheck(template.getDifficulty() + alc, realSource, bonus, true, 5.0f);
                    if (performer.isRoyalSmith() && template.isMetal() && power < 0.0f && power > -20.0f) {
                        power = 10 + Server.rand.nextInt(10);
                    }
                    if (power < 0.0f) {
                        int sec = (int)((float)time * (100.0f - Math.abs(power)) / 100.0f) / 10;
                        act.setFailSecond(sec);
                        act.setPower(power);
                    } else {
                        act.setPower(power);
                    }
                    performer.sendActionControl(Actions.actionEntrys[148].getVerbString() + " " + template.getName(), true, time);
                    if (realSource.isNoTake()) {
                        performer.getCommunicator().sendNormalServerMessage("You start to work with the " + template.getName() + " on the " + realSource.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getName() + " starts working with the " + template.getName() + " on the " + realSource.getName() + ".", performer, 5);
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("You start to work with the " + realSource.getName() + " on the " + template.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getName() + " starts working with the " + realSource.getName() + " on the " + template.getName() + ".", performer, 5);
                    }
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "no template for creating " + this.objectCreated, nst);
                    performer.getCommunicator().sendSafeServerMessage("You cannot create that item right now. Please contact administrators.");
                    throw new NoSuchItemException("Failed to locate template");
                }
            }
            if (!(counter > (failSecond = act.getFailSecond()))) {
                if (!(counter * 10.0f > (float)time)) throw new FailedException("Failed skillcheck.");
            }
            if (act.getRarity() != 0 || realSource.getRarity() > 0) {
                performer.playPersonalSound("sound.fx.drumroll");
            }
            try {
                ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(this.objectCreated);
                if ((temp.onePerTile || this.createOnGround) && !MethodsItems.mayDropOnTile(performer)) {
                    performer.getCommunicator().sendNormalServerMessage("You cannot create that item here, since there is not enough space in front of you.");
                    throw new NoSuchItemException("Already contains a large item.");
                }
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "No itemtemplate for objectCreated=" + this.objectCreated, nst);
                throw new NoSuchItemException("No template.");
            }
            float skillMultiplier = Math.max(1.0f, counter / 2.0f);
            if (Servers.localServer.isChallengeOrEpicServer() && primSkill.hasLowCreationGain()) {
                skillMultiplier /= 3.0f;
            }
            primSkill.skillCheck(template.getDifficulty(), realSource, performer.getVillageSkillModifier(), false, skillMultiplier);
            newItem = null;
            try {
                block136: {
                    String mess;
                    block138: {
                        float qlevel;
                        block137: {
                            qlevel = 0.0f;
                            double imbueEnhancement = 1.0 + 0.23047 * (double)source.getSkillSpellImprovement(primSkill.getNumber()) / 100.0;
                            qlevel = (float)((double)act.getPower() * imbueEnhancement / (double)this.getTotalNumberOfItems());
                            performer.sendToLoggers("Power is " + act.getPower() + " total num items is " + this.getTotalNumberOfItems() + " = qlevel " + qlevel);
                            boolean sourceIsStructure = realSource.isNoTake() && realSource.getOwnerId() == -10L;
                            act.setDestroyedItem(null);
                            byte material = realSource.getMaterial();
                            byte rarity = realTarget.getRarity();
                            if (realSource.rarity > rarity && Server.rand.nextInt(this.getTotalNumberOfItems()) == 0) {
                                rarity = realSource.rarity;
                            }
                            if (qlevel > 0.0f) {
                                if (act.getRarity() > rarity && Server.rand.nextInt(this.getTotalNumberOfItems()) == 0) {
                                    rarity = act.getRarity();
                                }
                                int weight = realSource.getWeightGrams();
                                performer.sendToLoggers("trimming qlevel to min " + qlevel + " or " + realSource.getCurrentQualityLevel() / (float)this.getTotalNumberOfItems());
                                qlevel = !realSource.isWool() && this.getTotalNumberOfItems() > 2 ? Math.min(qlevel, realSource.getCurrentQualityLevel() / (float)this.getTotalNumberOfItems()) : Math.min(qlevel, realSource.getCurrentQualityLevel() / (float)(this.getTotalNumberOfItems() + 6));
                                if (template.isWood() && realSource.isWood()) {
                                    realTarget.setMaterial(realSource.getMaterial());
                                }
                                if (!sourceIsStructure) {
                                    if (weight <= realSource.getTemplate().getWeightGrams()) {
                                        Items.destroyItem(realSource.getWurmId());
                                    } else {
                                        realSource.setWeight(weight - realSource.getTemplate().getWeightGrams(), false);
                                        weight = realSource.getTemplate().getWeightGrams();
                                    }
                                }
                                AdvancedCreationEntry.setStateForRequirement(req, ++state, realTarget);
                                if (realTarget.isEpicTargetItem() || this.getTotalNumberOfItems() > 1000) {
                                    MissionHelper helper = MissionHelper.getOrCreateHelper(performer.getWurmId());
                                    helper.increaseHelps(realTarget.getWurmId());
                                }
                                boolean create = this.areRequirementsFilled(realTarget);
                                float itq = realTarget.getCurrentQualityLevel();
                                float dam = realTarget.getDamage();
                                if (dam > 0.0f) {
                                    realTarget.setDamage(dam - dam / (float)this.getTotalNumberOfItems());
                                }
                                if (create) {
                                    float endQl;
                                    int obc;
                                    byte mat = template.getMaterial();
                                    if (realTarget.getTemplateId() == 179) {
                                        mat = realTarget.getMaterial();
                                    } else if (realSource.getTemplateId() == 179) {
                                        mat = realSource.getMaterial();
                                    }
                                    if (template.isWood()) {
                                        if (realTarget.isWood()) {
                                            mat = realTarget.getMaterial();
                                        } else if (realSource.isWood()) {
                                            mat = realSource.getMaterial();
                                        }
                                    }
                                    if (template.isMetal()) {
                                        if (realTarget.isMetal()) {
                                            mat = realTarget.getMaterial();
                                        } else if (realSource.isMetal()) {
                                            mat = realSource.getMaterial();
                                        }
                                    }
                                    if ((obc = this.objectCreated) == 384) {
                                        Kingdom K = Kingdoms.getKingdom(realTarget.getAuxData());
                                        byte ktId = 1;
                                        if (K != null) {
                                            ktId = K.getTemplate();
                                            if (ktId == 3) {
                                                obc = 430;
                                            } else if (ktId == 2) {
                                                obc = 528;
                                            } else if (ktId == 4) {
                                                obc = 638;
                                            }
                                        }
                                    }
                                    if ((newItem = ItemFactory.createItem(obc, endQl = Math.min(Math.max(1.0f, itq + qlevel), 99.0f), mat, rarity, performer.getName())).isDomainItem() && performer.getDeity() != null) {
                                        newItem.bless(performer.getDeity().number);
                                        newItem.setName(newItem.getName() + " of " + performer.getDeity().name);
                                    }
                                    if (newItem.isKingdomMarker() || newItem.isEpicTargetItem() || newItem.isTent() || newItem.isUseMaterialAndKingdom() || newItem.isProtectionTower()) {
                                        if (template.isEpicTargetItem) {
                                            newItem.setAuxData(performer.getKingdomTemplateId());
                                        } else {
                                            newItem.setAuxData(performer.getKingdomId());
                                        }
                                    }
                                    if (newItem.getTemplateId() == 850) {
                                        newItem.setData1(performer.getKingdomId());
                                    }
                                    if (newItem.isVehicle() || newItem.getTemplate().doesCreateWithLock()) {
                                        newItem.setAuxData(performer.getKingdomId());
                                        if (newItem.isLockable()) {
                                            int lockTemplate = newItem.isBoat() ? 568 : 193;
                                            Item lock = ItemFactory.createItem(lockTemplate, 1.0f, (byte)11, (byte)0, performer.getName());
                                            lock.setLastOwnerId(performer.getWurmId());
                                            newItem.setLockId(lock.getWurmId());
                                            lock.setLocked(true);
                                        }
                                    }
                                    if (newItem.isEpicTargetItem()) {
                                        MissionHelper.moveGlobalMissionId(realTarget.getWurmId(), newItem.getWurmId());
                                    }
                                    if (realTarget.getOwnerId() <= 0L) {
                                        newItem.setLastOwnerId(realTarget.lastOwner);
                                    }
                                    if (realTarget.getDescription().length() > 0) {
                                        newItem.setDescription(realTarget.getDescription());
                                    }
                                    Items.destroyItem(realTarget.getWurmId());
                                    if (newItem.getTemplateId() == 186) {
                                        performer.achievement(532);
                                        break block131;
                                    }
                                    if (newItem.getTemplateId() == 180 || newItem.getTemplateId() == 178) {
                                        performer.achievement(533);
                                        break block131;
                                    }
                                    if (newItem.getTemplateId() == 539) {
                                        performer.achievement(538);
                                        break block131;
                                    }
                                    if (newItem.isBoat()) {
                                        performer.achievement(540);
                                        break block131;
                                    }
                                    if (newItem.getTemplateId() == 1029) {
                                        performer.achievement(561);
                                        break block131;
                                    }
                                    if (newItem.isGuardTower()) {
                                        performer.achievement(574);
                                        break block131;
                                    }
                                    if (newItem.getTemplateId() == 850) {
                                        performer.achievement(583);
                                    }
                                    break block131;
                                }
                                if (!sourceIsStructure) {
                                    if (realSource.isLiquid()) {
                                        performer.getCommunicator().sendNormalServerMessage("You wash the " + template.getName() + " with " + realSource.getName() + ".");
                                        Server.getInstance().broadCastAction(performer.getName() + " washes " + template.getNameWithGenus() + " with the " + realSource.getName() + ".", performer, 5);
                                    } else if (realSource.isColorComponent() && this.isColouringCreation()) {
                                        performer.getCommunicator().sendNormalServerMessage("You use the " + realSource.getName() + " as pigment to change the colour of the " + template.getName() + ".");
                                        Server.getInstance().broadCastAction(performer.getName() + " uses " + realSource.getNameWithGenus() + " as pigment to change the colour of the " + template.getName() + ".", performer, 5);
                                    } else {
                                        performer.getCommunicator().sendNormalServerMessage("You attach the " + realSource.getName() + " to the " + template.getName() + ".");
                                        Server.getInstance().broadCastAction(performer.getName() + " attaches " + realSource.getNameWithGenus() + " to the " + template.getName() + ".", performer, 5);
                                    }
                                    if (!this.getUseTempalateWeight()) {
                                        realTarget.setWeight(realTarget.getWeightGrams() + weight, false);
                                    }
                                    performer.sendToLoggers("adds " + qlevel + " to " + itq);
                                    realTarget.setQualityLevel(itq + qlevel);
                                    if (rarity == realTarget.getRarity()) break block131;
                                    realTarget.setRarity(rarity);
                                    if (rarity > 2) {
                                        performer.achievement(300);
                                        break block131;
                                    }
                                    if (rarity > 1) {
                                        performer.achievement(302);
                                        break block131;
                                    }
                                    if (rarity > 0) {
                                        performer.achievement(301);
                                    }
                                    break block131;
                                }
                                performer.getCommunicator().sendNormalServerMessage("You use the " + realSource.getName() + " on the " + template.getName() + ".");
                                Server.getInstance().broadCastAction(performer.getName() + " uses " + realSource.getNameWithGenus() + " on the " + template.getName() + ".", performer, 5);
                                break block131;
                            }
                            if (sourceIsStructure) break block136;
                            boolean destroyed = false;
                            qlevel = Math.max(-90.0f, qlevel * (float)this.getTotalNumberOfItems());
                            mess = "You make a small mistake. The " + realSource.getName() + " is damaged a bit.";
                            float dam = -qlevel / 10.0f;
                            failed = true;
                            if (realSource.isRepairable()) {
                                dam /= 10.0f;
                            }
                            if (!(destroyed = realSource.setDamage(realSource.getDamage() + dam))) break block137;
                            int itc = AdvancedCreationEntry.getScrapMaterial(material);
                            if (itc != -1) {
                                float qMod = 100.0f + qlevel;
                                int we = realSource.getWeightGrams();
                                if (we > 10) {
                                    newItem = ItemFactory.createItem(itc, qMod / 10.0f, null);
                                    newItem.setSizes(realSource.getSizeX(), realSource.getSizeY(), realSource.getSizeZ());
                                    newItem.setWeight(we, false);
                                    newItem.setTemperature(realSource.getTemperature());
                                    newItem.setMaterial(material);
                                }
                            }
                            break block138;
                        }
                        if (qlevel > -20.0f) {
                            mess = "You almost made it, but the " + realSource.getName() + " is damaged.";
                        } else if (qlevel > -40.0f) {
                            mess = "This could very well work next time, but the " + realSource.getName() + " receives some damage.";
                        } else if (qlevel > -60.0f) {
                            mess = "Too many problems solved in the wrong way damages the " + realSource.getName() + " severely.";
                        } else if (qlevel > -90.0f) {
                            mess = "You fail miserably with the " + realSource.getName() + ".";
                        }
                        failed = true;
                    }
                    performer.getCommunicator().sendNormalServerMessage(mess);
                    Server.getInstance().broadCastAction(performer.getName() + " fails with the " + realSource.getName() + ".", performer, 5);
                    break block131;
                }
                performer.getCommunicator().sendNormalServerMessage("You fail to use the " + realSource.getName() + " on the " + template.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " fails to use " + realSource.getNameWithGenus() + " on the " + template.getName() + ".", performer, 5);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, performer.getName() + " failed to create item.", ex);
            }
        }
        performer.getStatus().modifyStamina(-counter * 1000.0f);
        if (newItem != null) {
            if (newItem.getTemplateId() != 899) return newItem;
            performer.achievement(370);
            return newItem;
        }
        if (!failed) throw new NoSuchItemException("Not done yet.");
        throw new NoSuchItemException("Failed.");
    }

    public static final void onEpicItemCreated(Creature performer, Item newItem, int _objectCreated, boolean generateName) {
        if (generateName) {
            String newname = "";
            String firstPart = EpicTargetItems.getTypeNamePartString(_objectCreated);
            String secondPart = EpicTargetItems.getSymbolNamePartString(performer);
            newname = Server.rand.nextBoolean() ? (_objectCreated == 716 || firstPart.toLowerCase().contains("way") || firstPart.toLowerCase().contains("path") || firstPart.toLowerCase().contains("passage") || firstPart.toLowerCase().contains("gate") || firstPart.toLowerCase().contains("door") ? firstPart + " To " + secondPart : firstPart + " Of " + secondPart) : (Server.rand.nextBoolean() ? (!secondPart.endsWith("s") ? secondPart + "s " + firstPart : secondPart + "' " + firstPart) : secondPart + " " + firstPart);
            newItem.setName(newname);
        }
        if (EpicTargetItems.isEpicItemWithMission(newItem)) {
            performer.getCommunicator().sendSafeServerMessage("The " + newItem.getName() + " is complete! It has become an Epic focus point!");
            HistoryManager.addHistory(performer.getName(), "completes the " + newItem.getName() + "!");
            MissionTriggers.activateTriggers(performer, newItem, 148, 0L, 1);
            MissionHelper.addKarmaForItem(newItem.getWurmId());
        } else {
            performer.getCommunicator().sendAlertServerMessage("The " + newItem.getName() + " is complete! However, it failed to fulfil the requirements to become an Epic focus point.");
        }
    }

    public void consume(Item realTarget, Creature performer, ItemTemplate template, Action act) {
        try {
            boolean destroy = false;
            if (this.useCapacity) {
                int weight = realTarget.getWeightGrams();
                int extraWeight = (int)(this.percentageLost / 100.0f * (float)template.getWeightGrams());
                boolean bl = destroy = weight <= template.getWeightGrams() + extraWeight;
                if (!destroy) {
                    byte material;
                    int itc;
                    realTarget.setWeight(weight - template.getWeightGrams() - extraWeight, false);
                    if (extraWeight > 0 && (itc = AdvancedCreationEntry.getScrapMaterial(material = realTarget.getMaterial())) != -1 && extraWeight > 10) {
                        Item newItem = ItemFactory.createItem(itc, realTarget.getCurrentQualityLevel() / 10.0f, material, (byte)0, performer.getName());
                        newItem.setWeight(extraWeight, true);
                        newItem.setTemperature(realTarget.getTemperature());
                        performer.getInventory().insertItem(newItem);
                    }
                }
            }
            if ((this.destroyTarget || destroy || this.destroyBoth) && (destroy || act.getPower() > 0.0f)) {
                act.setDestroyedItem(realTarget);
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to delete items.", ex);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Item run(Creature performer, Item source, long targetId, float counter) throws FailedException, NoSuchSkillException, NoSuchItemException, IllegalArgumentException {
        Item newItem;
        block144: {
            Item target = Items.getItem(targetId);
            Item realSource = source;
            Item realTarget = target;
            if (this.isRestrictedToDeityFollower() && (performer.getDeity() == null || performer.getDeity().getTemplateDeity() != this.getDeityRestriction())) {
                String deityName = Deities.getDeityName(this.getDeityRestriction());
                String message = "You must be a follower of %s or one of their demigods to create this item.";
                performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You must be a follower of %s or one of their demigods to create this item.", deityName));
                throw new NoSuchItemException("Incorrect deity");
            }
            if (performer.getVehicle() != -10L) {
                performer.getCommunicator().sendNormalServerMessage("You need to be on solid ground to do that.");
                throw new NoSuchItemException("Need to be on solid ground.");
            }
            try {
                int chance = (int)this.getDifficultyFor(realSource, realTarget, performer);
                if (chance == 0 || chance <= 5) {
                    performer.getCommunicator().sendNormalServerMessage("This is impossible, perhaps you are not skilled enough.");
                    throw new NoSuchItemException("Not enough skill.");
                }
            }
            catch (NoSuchTemplateException nst) {
                throw new NoSuchItemException(nst.getMessage(), nst);
            }
            boolean create = false;
            if (source.getTemplateId() == this.objectSource && target.getTemplateId() == this.objectTarget) {
                create = true;
            } else if (source.getTemplateId() == this.objectTarget && target.getTemplateId() == this.objectSource) {
                create = true;
                realTarget = source;
                realSource = target;
            }
            if (realSource == realTarget || realSource.getWurmId() == realTarget.getWurmId()) {
                create = false;
                performer.getCommunicator().sendNormalServerMessage("You try to create something out of folding " + realSource.getName() + " around itself. Doesn't work.");
                throw new NoSuchItemException("Can't create on itself.");
            }
            if (this.objectSourceMaterial != 0 && realSource.getMaterial() != this.objectSourceMaterial) {
                create = false;
                performer.getCommunicator().sendNormalServerMessage("Incorrect source material!");
                throw new NoSuchItemException("Incorrect source material!");
            }
            if (this.objectTargetMaterial != 0 && realTarget.getMaterial() != this.objectTargetMaterial) {
                create = false;
                performer.getCommunicator().sendNormalServerMessage("Incorrect target material!");
                throw new NoSuchItemException("Incorrect target material!");
            }
            if (realSource.getTemplateId() == 1344 && !realSource.isEmpty(false)) {
                create = false;
                performer.getCommunicator().sendNormalServerMessage("Fishing pole must be empty to be able to make a fishing rod.");
                throw new NoSuchItemException("Must be Empty!");
            }
            if (realSource.getTemplateId() == 1409 && realSource.getItemCount() > 0) {
                create = false;
                performer.getCommunicator().sendNormalServerMessage("You should remove any pages from the book before using it in creation.");
                throw new NoSuchItemException("Items inside book.");
            }
            if (!create) {
                throw new NoSuchItemException("Illegal parameters for this entry: source=" + realSource.getTemplateId() + ", target=" + realTarget.getTemplateId() + " when creating " + this.objectCreated);
            }
            ItemTemplate template = null;
            try {
                template = ItemTemplateFactory.getInstance().getTemplate(this.objectCreated);
                if ((template.onePerTile || this.createOnGround) && !MethodsItems.mayDropOnTile(performer)) {
                    performer.getCommunicator().sendNormalServerMessage("You cannot create that item here, since there is not enough space in front of you.");
                    throw new NoSuchItemException("Already contains a large item.");
                }
                if (template.isDomainItem() && performer.getDeity() == null) {
                    performer.getCommunicator().sendAlertServerMessage("You lack the faith needed to create " + template.getNameWithGenus() + ".");
                    throw new NoSuchItemException("No deity.");
                }
                if (template.nonDeedable && performer.getCurrentVillage() != null) {
                    performer.getCommunicator().sendAlertServerMessage("You can't continue the " + template.getName() + " now; you can't build this in a settlement.", (byte)3);
                    throw new NoSuchItemException("In settlement.");
                }
                if (template.isGuardTower()) {
                    GuardTower.canConstructTower(performer, realTarget);
                } else if (template.protectionTower) {
                    float yPosMod;
                    int placedY;
                    if (!performer.isOnSurface()) {
                        performer.getCommunicator().sendAlertServerMessage("You can't build the tower here; you can't build this below surface.", (byte)3);
                        throw new NoSuchItemException("Below surface.");
                    }
                    float posX = performer.getStatus().getPositionX();
                    float posY = performer.getStatus().getPositionY();
                    float rot = performer.getStatus().getRotation();
                    float xPosMod = (float)Math.sin(rot * ((float)Math.PI / 180)) * 2.0f;
                    int placedX = (int)(posX += xPosMod) >> 2;
                    VolaTile targTile = Zones.getOrCreateTile(placedX, placedY = (int)(posY += (yPosMod = -((float)Math.cos(rot * ((float)Math.PI / 180))) * 2.0f)) >> 2, true);
                    if (targTile != null && targTile.isTransition()) {
                        performer.getCommunicator().sendAlertServerMessage("You can't continue the tower here - the foundation is not stable enough.", (byte)3);
                        throw new NoSuchItemException("On cave opening.");
                    }
                    VolaTile tile = performer.getCurrentTile();
                    if (tile != null) {
                        if (Terraforming.isTileUnderWater(1, tile.tilex, tile.tiley, performer.isOnSurface())) {
                            performer.getCommunicator().sendAlertServerMessage("You can't build the tower here; the ground is not solid here.", (byte)3);
                            throw new NoSuchItemException("Too wet.");
                        }
                        int mindist = Kingdoms.minKingdomDist;
                        if (Zones.getKingdom(tile.tilex, tile.tiley) == performer.getKingdomId()) {
                            mindist = 60;
                        }
                        if (Kingdoms.isTowerTooNear(tile.tilex, tile.tiley, tile.isOnSurface(), template.protectionTower)) {
                            performer.getCommunicator().sendAlertServerMessage("You can't continue the tower now; another tower is too near.", (byte)3);
                            throw new NoSuchItemException("Too close to another tower.");
                        }
                        if (!template.protectionTower && !Zones.isKingdomBlocking(tile.tilex - mindist, tile.tiley - mindist, tile.tilex + mindist, tile.tiley + mindist, performer.getKingdomId())) {
                            performer.getCommunicator().sendAlertServerMessage("You can't continue the tower now; another kingdom is too near.", (byte)3);
                            throw new NoSuchItemException("Too close to another kingdom.");
                        }
                        if (Servers.localServer.HOMESERVER) {
                            if (!Terraforming.isFlat(tile.getTileX(), tile.getTileY(), performer.isOnSurface(), 4)) {
                                performer.getCommunicator().sendAlertServerMessage("The area must be flat where you want to build this tower.", (byte)3);
                                throw new NoSuchItemException("Not flat enough here.");
                            }
                        } else {
                            int maxwest;
                            int maxsouth;
                            int maxnorth;
                            if (Terraforming.isTileModBlocked(performer, placedX, placedY, performer.isOnSurface())) {
                                throw new NoSuchItemException("Tile protected by the deities in this area.");
                            }
                            for (Item targ : Items.getWarTargets()) {
                                int maxnorth2 = Math.max(0, tile.tiley - 60);
                                int maxsouth2 = Math.min(Zones.worldTileSizeY, tile.tiley + 60);
                                int maxwest2 = Math.max(0, tile.tilex - 60);
                                int maxeast = Math.min(Zones.worldTileSizeX, tile.tilex + 60);
                                if ((int)targ.getPosX() >> 2 <= maxwest2 || (int)targ.getPosX() >> 2 >= maxeast || (int)targ.getPosY() >> 2 >= maxsouth2 || (int)targ.getPosY() >> 2 <= maxnorth2) continue;
                                performer.getCommunicator().sendSafeServerMessage("You cannot found the tower here, since this is an active battle ground.");
                                throw new NoSuchItemException("Too close to a war target.");
                            }
                            EndGameItem alt = EndGameItems.getEvilAltar();
                            if (alt != null) {
                                maxnorth = Math.max(0, tile.tiley - 100);
                                maxsouth = Math.min(Zones.worldTileSizeY, tile.tiley + 100);
                                int maxeast = Math.max(0, tile.tilex - 100);
                                maxwest = Math.min(Zones.worldTileSizeX, tile.tilex + 100);
                                if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                                    throw new NoSuchItemException("You cannot place a tower here, since this is holy ground.");
                                }
                            }
                            if ((alt = EndGameItems.getGoodAltar()) != null) {
                                maxnorth = Math.max(0, tile.tiley - 100);
                                maxsouth = Math.min(Zones.worldTileSizeY, tile.tiley + 100);
                                int maxeast = Math.max(0, tile.tilex - 100);
                                maxwest = Math.min(Zones.worldTileSizeX, tile.tilex + 100);
                                if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                                    throw new NoSuchItemException("You cannot place a tower here, since this is holy ground.");
                                }
                            }
                        }
                    }
                }
                if (template.isEpicTargetItem && Servers.localServer.PVPSERVER) {
                    EpicMission mission = EpicServerStatus.getBuildMissionForTemplate(this.objectCreated);
                    if (mission == null) {
                        performer.getCommunicator().sendNormalServerMessage("There is no current mission to build this item.");
                        performer.sendToLoggers("Trying to start mission item without a valid mission being found.");
                        throw new NoSuchItemException("There is no current mission to build this item.");
                    }
                    if (mission != null && EpicTargetItems.getTargetItemPlacement(mission.getMissionId()) != performer.getGlobalMapPlacement()) {
                        int placement = EpicTargetItems.getTargetItemPlacement(mission.getMissionId());
                        performer.getCommunicator().sendNormalServerMessage(EpicTargetItems.getTargetItemPlacementString(placement));
                        performer.sendToLoggers("Trying to start mission item in " + MiscConstants.getDirectionString((byte)performer.getGlobalMapPlacement()) + ". " + EpicTargetItems.getTargetItemPlacementString(placement));
                        if (performer.getPower() > 0) {
                            performer.getCommunicator().sendNormalServerMessage("GMLOG: Currently in the " + MiscConstants.getDirectionString((byte)performer.getGlobalMapPlacement()) + ".");
                        }
                        throw new NoSuchItemException(EpicTargetItems.getTargetItemPlacementString(placement));
                    }
                    if (!EpicTargetItems.mayBuildEpicItem(this.objectCreated, performer.getTileX(), performer.getTileY(), performer.isOnSurface(), performer, performer.getKingdomTemplateId())) {
                        performer.getCommunicator().sendNormalServerMessage(EpicTargetItems.getInstructionStringForKingdom(this.objectCreated, performer.getKingdomTemplateId()));
                        performer.sendToLoggers("Trying to start mission item but failed requirements. " + EpicTargetItems.getInstructionStringForKingdom(this.objectCreated, performer.getKingdomTemplateId()));
                        throw new NoSuchItemException(EpicTargetItems.getInstructionStringForKingdom(this.objectCreated, performer.getKingdomTemplateId()));
                    }
                }
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "no template for creating " + this.objectCreated, nst);
                performer.getCommunicator().sendSafeServerMessage("You cannot create that item right now. Please contact administrators.");
                throw new NoSuchItemException("Failed to locate template");
            }
            Skills skills = performer.getSkills();
            Skill primSkill = null;
            Skill secondarySkill = null;
            Action act = null;
            try {
                act = performer.getCurrentAction();
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.WARNING, "This action doesn't exist? " + performer.getName(), nsa);
                throw new NoSuchItemException("An error occured on the server. This action was not found. Please report.");
            }
            float failSecond = Float.MAX_VALUE;
            try {
                primSkill = skills.getSkill(this.primarySkill);
            }
            catch (Exception ex) {
                primSkill = skills.learn(this.primarySkill, 1.0f);
            }
            try {
                secondarySkill = skills.getSkill(realSource.getPrimarySkill());
            }
            catch (Exception ex) {
                try {
                    secondarySkill = skills.learn(realSource.getPrimarySkill(), 1.0f);
                }
                catch (Exception placedY) {
                    // empty catch block
                }
            }
            int time = 10;
            if (counter == 1.0f) {
                float power;
                VolaTile t;
                int sourceWeightToRemove = this.getSourceWeightToRemove(realSource, realTarget, template, true);
                int targetWeightToRemove = this.getTargetWeightToRemove(realSource, realTarget, template, true);
                int extraTargetWeight = this.getPercentageLost() > 0.0f ? (int)(this.percentageLost / 100.0f * (float)targetWeightToRemove) : 0;
                this.checkSaneAmounts(realSource, sourceWeightToRemove, realTarget, targetWeightToRemove + extraTargetWeight, template, performer, true);
                if (realTarget.isMetal() && realSource.isMetal() && SkillList.IsBlacksmithing(this.primarySkill) && realTarget.getTemperature() < 3500) {
                    performer.getCommunicator().sendNormalServerMessage("The " + realTarget.getName() + " must be glowing hot to do this.");
                    throw new NoSuchItemException("Too low temperature.");
                }
                if (template.isOutsideOnly()) {
                    VolaTile tile = performer.getCurrentTile();
                    if (tile != null && tile.getStructure() != null) {
                        if (!tile.getStructure().isTypeBridge()) {
                            performer.getCommunicator().sendNormalServerMessage("You cannot create that inside a building.");
                            throw new NoSuchItemException("Can't create inside.");
                        }
                        if (performer.getBridgeId() != -10L) {
                            performer.getCommunicator().sendNormalServerMessage("You cannot create that on a bridge.");
                            throw new NoSuchItemException("Can't create on a bridge.");
                        }
                    }
                } else if (template.insideOnly && ((t = performer.getCurrentTile()) == null || t.getStructure() == null)) {
                    performer.getCommunicator().sendNormalServerMessage("You must create that item inside.");
                    throw new NoSuchItemException("Must create inside.");
                }
                try {
                    time = Actions.getItemCreationTime(200, performer, primSkill, this, realSource, realTarget, template.isMassProduction());
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "No template when creating with " + realSource.getName() + " and " + realTarget.getName() + "." + nst.getMessage(), nst);
                    performer.getCommunicator().sendSafeServerMessage("You cannot create that item right now. Please contact administrators.");
                    throw new NoSuchItemException("No template.");
                }
                try {
                    performer.getCurrentAction().setTimeLeft(time);
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.INFO, "This action does not exist?", nsa);
                }
                realTarget.setBusy(true);
                performer.sendActionControl(Actions.actionEntrys[148].getVerbString() + " " + template.getName(), true, time);
                if (realSource.isNoTake()) {
                    performer.getCommunicator().sendNormalServerMessage("You start to work with the " + realTarget.getName() + " on the " + realSource.getName() + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " starts working with the " + realTarget.getName() + " on the " + realSource.getName() + ".", performer, 5);
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You start to work with the " + realSource.getName() + " on the " + realTarget.getName() + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " starts working with the " + realSource.getName() + " on the " + realTarget.getName() + ".", performer, 5);
                }
                if (!this.depleteSource && realSource.isRepairable()) {
                    realSource.setDamage(realSource.getDamage() + 0.0025f * realSource.getDamageModifier());
                }
                if (!this.depleteTarget && realTarget.isRepairable()) {
                    realTarget.setDamage(realTarget.getDamage() + 0.0025f * realSource.getDamageModifier());
                }
                double bonus = performer.getVillageSkillModifier();
                if (secondarySkill != null) {
                    bonus += secondarySkill.getKnowledge(realSource, 0.0) / 10.0;
                }
                float alc = 0.0f;
                if (performer.isPlayer()) {
                    alc = ((Player)performer).getAlcohol();
                }
                if ((power = (float)primSkill.skillCheck(template.getDifficulty() + alc, realSource, bonus, true, 1.0f)) < 0.0f) {
                    int sec = (int)((float)time * (100.0f - Math.abs(power)) / 100.0f) / 10;
                    act.setFailSecond(sec);
                    act.setPower(power);
                } else {
                    double imbueEnhancement = 1.0 + 0.23047 * (double)source.getSkillSpellImprovement(primSkill.getNumber()) / 100.0;
                    act.setPower((float)((double)power * imbueEnhancement));
                }
            } else {
                try {
                    time = performer.getCurrentAction().getTimeLeft();
                    if (act.mayPlaySound()) {
                        MethodsItems.sendImproveSound(performer, realSource, realTarget, this.primarySkill);
                    }
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.INFO, "This action does not exist?", nsa);
                }
            }
            failSecond = act.getFailSecond();
            if (!(counter > failSecond) && !(counter * 10.0f > (float)time)) {
                throw new FailedException("Failed skillcheck.");
            }
            if (act.getRarity() != 0 || this.depleteSource && realSource.getRarity() > 0 || this.depleteTarget && realTarget.getRarity() > 0) {
                performer.playPersonalSound("sound.fx.drumroll");
            }
            double bonus = performer.getVillageSkillModifier();
            float alc = 0.0f;
            if (performer.isPlayer()) {
                alc = ((Player)performer).getAlcohol();
            }
            if (secondarySkill != null) {
                bonus = Math.max(-10.0, secondarySkill.skillCheck(template.getDifficulty() + alc, 0.0, false, Math.max(1.0f, counter / 10.0f)));
            }
            float skillMultiplier = Math.max(1.0f, counter / 2.0f);
            if (Servers.localServer.isChallengeOrEpicServer() && primSkill.hasLowCreationGain()) {
                skillMultiplier /= 3.0f;
            }
            primSkill.skillCheck(template.getDifficulty() + alc, realSource, bonus, false, skillMultiplier);
            int sourceWeightToRemove = this.getSourceWeightToRemove(realSource, realTarget, template, true);
            int targetWeightToRemove = this.getTargetWeightToRemove(realSource, realTarget, template, true);
            int extraTargetWeight = this.getPercentageLost() > 0.0f ? (int)(this.percentageLost / 100.0f * (float)targetWeightToRemove) : 0;
            this.checkSaneAmounts(realSource, sourceWeightToRemove, realTarget, targetWeightToRemove + extraTargetWeight, template, performer, true);
            newItem = null;
            try {
                int itc;
                byte material;
                block145: {
                    byte rarity;
                    float itq;
                    block151: {
                        int reqs;
                        float qlevel;
                        block149: {
                            Iterator<CreationRequirement> iterator;
                            block150: {
                                int weight;
                                int itc2;
                                block148: {
                                    itq = qlevel = act.getPower();
                                    if (realTarget.getCurrentQualityLevel() < qlevel) {
                                        itq = realTarget.getCurrentQualityLevel();
                                    }
                                    material = template.getMaterial();
                                    if (template.isWood()) {
                                        if (realTarget.isWood()) {
                                            material = realTarget.getMaterial();
                                        } else if (realSource.isWood()) {
                                            material = realSource.getMaterial();
                                        }
                                    }
                                    if (template.isMetal()) {
                                        if (realTarget.isMetal()) {
                                            material = realTarget.getMaterial();
                                        } else if (realSource.isMetal()) {
                                            material = realSource.getMaterial();
                                        }
                                    }
                                    if (!(qlevel > 0.0f)) break block148;
                                    reqs = 0;
                                    if (this.requirements == null) break block149;
                                    iterator = this.requirements.iterator();
                                    break block150;
                                }
                                String verb = template.getName().charAt(template.getName().length() - 1) == 's' ? " are " : " is ";
                                String mess = "You realize this was a meaningless effort. The " + template.getName() + verb + "useless.";
                                float targDam = realTarget.getDamage();
                                float sourceDam = realSource.getDamage();
                                if (qlevel > -20.0f) {
                                    mess = "You almost made it, but the " + template.getName() + verb + "useless.";
                                    targDam += (float)(Server.rand.nextInt(1) + 1);
                                    sourceDam += (float)(Server.rand.nextInt(1) + 1);
                                } else if (qlevel > -40.0f) {
                                    mess = "This could very well work next time, but the " + template.getName() + verb + "useless.";
                                    targDam += (float)(Server.rand.nextInt(3) + 1);
                                    sourceDam += (float)(Server.rand.nextInt(3) + 1);
                                } else if (qlevel > -60.0f) {
                                    mess = "Too many problems solved in the wrong way makes the " + template.getName() + " useless.";
                                    targDam += (float)(Server.rand.nextInt(5) + 1);
                                    sourceDam += (float)(Server.rand.nextInt(5) + 1);
                                } else if (qlevel > -80.0f) {
                                    mess = "You fail miserably with the " + template.getName() + ".";
                                    targDam += (float)(Server.rand.nextInt(7) + 1);
                                    sourceDam += (float)(Server.rand.nextInt(7) + 1);
                                } else {
                                    targDam += (float)(Server.rand.nextInt(9) + 1);
                                    sourceDam += (float)(Server.rand.nextInt(9) + 1);
                                }
                                performer.getCommunicator().sendNormalServerMessage(mess);
                                Server.getInstance().broadCastAction(performer.getName() + " fails with the " + template.getName() + ".", performer, 5);
                                if (realTarget.getTemplateId() == 128) {
                                    int weight2 = realTarget.getWeightGrams();
                                    realTarget.setWeight(weight2 - 100, true);
                                } else if (targDam > 100.0f) {
                                    itc2 = AdvancedCreationEntry.getScrapMaterial(material);
                                    if (itc2 != -1) {
                                        weight = template.getWeightGrams();
                                        if (this.depleteTarget) {
                                            weight = realTarget.getWeightGrams();
                                        }
                                        if (weight > 10) {
                                            newItem = ItemFactory.createItem(itc2, realTarget.getCurrentQualityLevel() / 10.0f, null);
                                            newItem.setTemperature(realTarget.getTemperature());
                                            newItem.setWeight(weight, false);
                                            newItem.setMaterial(material);
                                        }
                                    }
                                    Items.destroyItem(realTarget.getWurmId());
                                } else {
                                    realTarget.setDamage(targDam);
                                }
                                if (realSource.isRepairable()) break block144;
                                if (sourceDam > 100.0f) {
                                    itc2 = AdvancedCreationEntry.getScrapMaterial(realSource.getMaterial());
                                    if (itc2 != -1) {
                                        weight = realSource.getWeightGrams();
                                        if (this.destroyTarget || this.destroyBoth) {
                                            weight = realSource.getWeightGrams();
                                        }
                                        if (weight > 10) {
                                            newItem = ItemFactory.createItem(itc2, realSource.getCurrentQualityLevel() / 10.0f, null);
                                            newItem.setTemperature(realSource.getTemperature());
                                            newItem.setWeight(weight, false);
                                            newItem.setMaterial(realSource.getMaterial());
                                        }
                                    }
                                    Items.destroyItem(realSource.getWurmId());
                                    break block144;
                                } else {
                                    realSource.setDamage(sourceDam);
                                }
                                break block144;
                            }
                            while (iterator.hasNext()) {
                                CreationRequirement req = iterator.next();
                                if (req instanceof ItemContainerRequirement || req instanceof ItemVicinityRequirement) continue;
                                ++reqs;
                            }
                        }
                        rarity = 0;
                        if (act.getRarity() > rarity && Server.rand.nextInt(this.getTotalNumberOfItems()) == 0) {
                            rarity = act.getRarity();
                        }
                        if (this.depleteSource && realSource.rarity > rarity && (Server.rand.nextInt(this.getTotalNumberOfItems()) == 0 || realSource.getTemplateId() == 1344)) {
                            rarity = realSource.rarity;
                        }
                        if (this.depleteTarget && realTarget.rarity > rarity && Server.rand.nextInt(this.getTotalNumberOfItems()) == 0) {
                            rarity = realTarget.rarity;
                        }
                        if (reqs <= 0) break block151;
                        itq /= (float)this.getTotalNumberOfItems();
                        itq += Math.min(realSource.getCurrentQualityLevel(), qlevel) / (float)this.getTotalNumberOfItems();
                        if (Item.getMaterialCreationBonus(material) > 0.0f) {
                            float leftToMax = 100.0f - itq;
                            itq += leftToMax / 100.0f * Item.getMaterialCreationBonus(material);
                        }
                        newItem = ItemFactory.createItem(179, Math.max(1.0f, itq), material, rarity, performer.getName());
                        newItem.setData(0, 0);
                        AdvancedCreationEntry.setTemplateId(newItem, this.objectCreated);
                        newItem.setName("unfinished " + template.sizeString + template.getName());
                        if (template.kingdomMarker || template.isTent() || template.protectionTower) {
                            newItem.setAuxData(performer.getKingdomId());
                        } else if (template.isEpicTargetItem) {
                            newItem.setAuxData(performer.getKingdomTemplateId());
                        }
                        if (this.getTotalNumberOfItems() > 1000) {
                            MissionHelper helper = MissionHelper.getOrCreateHelper(performer.getWurmId());
                            helper.increaseHelps(newItem.getWurmId());
                        }
                        if (!this.getUseTempalateWeight()) {
                            int weight = sourceWeightToRemove + targetWeightToRemove;
                            newItem.setWeight(weight, false);
                            break block145;
                        } else if (newItem.getRealTemplate() != null) {
                            newItem.setWeight(newItem.getRealTemplate().getWeightGrams(), false);
                            break block145;
                        } else {
                            newItem.setWeight(newItem.getTemplate().getWeightGrams(), false);
                        }
                        break block145;
                    }
                    newItem = ItemFactory.createItem(this.objectCreated, Math.max(1.0f, itq), material, rarity, performer.getName());
                }
                if (newItem.getRarity() > 2) {
                    performer.achievement(300);
                } else if (newItem.getRarity() == 1) {
                    performer.achievement(301);
                } else if (newItem.getRarity() == 2) {
                    performer.achievement(302);
                }
                int extraWeight = this.getExtraWeight(template);
                if (sourceWeightToRemove > 0) {
                    if (sourceWeightToRemove < realSource.getWeightGrams()) {
                        realSource.setWeight(realSource.getWeightGrams() - sourceWeightToRemove, true);
                    } else {
                        Items.destroyItem(realSource.getWurmId());
                    }
                } else if (realSource.isRepairable()) {
                    realSource.setDamage(realSource.getDamage() + 0.004f * realSource.getDamageModifier());
                }
                if (targetWeightToRemove > 0) {
                    if (targetWeightToRemove + extraTargetWeight < realTarget.getWeightGrams()) {
                        realTarget.setWeight(realTarget.getWeightGrams() - (targetWeightToRemove + extraTargetWeight), true);
                    } else {
                        Items.destroyItem(realTarget.getWurmId());
                    }
                } else if (realTarget.isRepairable()) {
                    realTarget.setDamage(realTarget.getDamage() + 0.004f * realTarget.getDamageModifier());
                }
                if (extraWeight > 10 && !realTarget.isLiquid() && (itc = AdvancedCreationEntry.getScrapMaterial(material)) != -1) {
                    try {
                        Item scrap = ItemFactory.createItem(itc, realTarget.getCurrentQualityLevel() / 10.0f, material, (byte)0, performer.getName());
                        scrap.setWeight(extraWeight, false);
                        scrap.setTemperature(realTarget.getTemperature());
                        performer.getInventory().insertItem(scrap, true);
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, performer.getName() + " tid= " + itc + ", " + nst.getMessage(), nst);
                    }
                }
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to create item.", ex);
            }
        }
        performer.getStatus().modifyStamina(-counter * 1000.0f);
        if (newItem != null) {
            return newItem;
        }
        throw new NoSuchItemException("Too low quality.");
    }
}

