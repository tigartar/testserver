/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import java.io.IOException;

public abstract class VillageRole
implements VillageStatus,
MiscConstants {
    public int id;
    byte status;
    int villageid;
    public String name = "";
    boolean mayTerraform = false;
    boolean mayCuttrees = false;
    boolean mayMine = false;
    boolean mayFarm = false;
    boolean mayBuild = false;
    boolean mayHire = false;
    boolean mayInvite = false;
    boolean mayDestroy = false;
    boolean mayManageRoles = false;
    boolean mayExpand = false;
    boolean mayLockFences = false;
    boolean mayPassAllFences = false;
    boolean diplomat = false;
    boolean mayAttackCitizens = false;
    boolean mayAttackNonCitizens;
    boolean mayFish;
    boolean mayCutOldTrees;
    boolean mayPushPullTurn;
    boolean mayUpdateMap;
    boolean mayLead;
    boolean mayPickup;
    boolean mayTame;
    boolean mayLoad;
    boolean mayButcher;
    boolean mayAttachLock;
    boolean mayPickLocks;
    int villageAppliedTo;
    long playerAppliedTo;
    Permissions settings;
    Permissions moreSettings;
    Permissions extraSettings;

    VillageRole(int aVillageid, String aName, boolean aTerraform, boolean aCutTrees, boolean aMine, boolean aFarm, boolean aBuild, boolean aHire, boolean aMayInvite, boolean aMayDestroy, boolean aMayManageRoles, boolean aMayExpand, boolean aMayLockFences, boolean aMayPassFences, boolean aIsDiplomat, boolean aMayAttackCitizens, boolean aMayAttackNonCitizens, boolean aMayFish, boolean aMayCutOldTrees, byte aStatus, int appliedToVillage, boolean aMayPushPullTurn, boolean aMayUpdateMap, boolean aMayLead, boolean aMayPickup, boolean aMayTame, boolean aMayLoad, boolean aMayButcher, boolean aMayAttachLock, boolean aMayPickLocks, long appliedToPlayer, int aSettings, int aMoreSettings, int aExtraSettings) throws IOException {
        this.mayAttackNonCitizens = Servers.localServer.PVPSERVER;
        this.mayFish = true;
        this.mayCutOldTrees = false;
        this.mayPushPullTurn = true;
        this.mayUpdateMap = false;
        this.mayLead = false;
        this.mayPickup = false;
        this.mayTame = false;
        this.mayLoad = false;
        this.mayButcher = false;
        this.mayAttachLock = false;
        this.mayPickLocks = false;
        this.villageAppliedTo = 0;
        this.playerAppliedTo = -10L;
        this.settings = new Permissions();
        this.moreSettings = new Permissions();
        this.extraSettings = new Permissions();
        this.villageid = aVillageid;
        this.name = aName;
        this.villageAppliedTo = appliedToVillage;
        this.status = aStatus;
        this.mayTerraform = aTerraform;
        this.mayCuttrees = aCutTrees;
        this.mayMine = aMine;
        this.mayFarm = aFarm;
        this.mayBuild = aBuild;
        this.mayHire = aHire;
        this.mayInvite = aMayInvite;
        this.mayDestroy = aMayDestroy;
        this.mayManageRoles = aMayManageRoles;
        this.mayExpand = aMayExpand;
        this.mayLockFences = aMayLockFences;
        this.mayPassAllFences = aMayPassFences;
        this.diplomat = aIsDiplomat;
        this.mayAttackCitizens = aMayAttackCitizens;
        this.mayAttackNonCitizens = aMayAttackNonCitizens;
        this.mayFish = aMayFish;
        this.mayCutOldTrees = aMayCutOldTrees;
        this.mayPushPullTurn = aMayPushPullTurn;
        this.mayUpdateMap = aMayUpdateMap;
        this.mayLead = aMayLead;
        this.mayPickup = aMayPickup;
        this.mayTame = aMayTame;
        this.mayLoad = aMayLoad;
        this.mayButcher = aMayButcher;
        this.mayAttachLock = aMayAttachLock;
        this.mayPickLocks = aMayPickLocks;
        this.playerAppliedTo = appliedToPlayer;
        this.settings.setPermissionBits(aSettings);
        this.moreSettings.setPermissionBits(aMoreSettings);
        this.extraSettings.setPermissionBits(aExtraSettings);
        this.create();
    }

    VillageRole(int aId, int aVillageid, String aRoleName, boolean aMayTerraform, boolean aMayCuttrees, boolean aMayMine, boolean aMayFarm, boolean aMayBuild, boolean aMayHire, boolean aMayInvite, boolean aMayDestroy, boolean aMayManageRoles, boolean aMayExpand, boolean aMayPassAllFences, boolean aMayLockFences, boolean aMayAttackCitizens, boolean aMayAttackNonCitizens, boolean aMayFish, boolean aMayCutOldTrees, boolean aMayPushPullTurn, boolean aDiplomat, byte aStatus, int aVillageAppliedTo, boolean aMayUpdateMap, boolean aMayLead, boolean aMayPickup, boolean aMayTame, boolean aMayLoad, boolean aMayButcher, boolean aMayAttachLock, boolean aMayPickLocks, long aPlayerAppliedTo, int aSettings, int aMoreSettings, int aExtraSettings) {
        this.mayAttackNonCitizens = Servers.localServer.PVPSERVER;
        this.mayFish = true;
        this.mayCutOldTrees = false;
        this.mayPushPullTurn = true;
        this.mayUpdateMap = false;
        this.mayLead = false;
        this.mayPickup = false;
        this.mayTame = false;
        this.mayLoad = false;
        this.mayButcher = false;
        this.mayAttachLock = false;
        this.mayPickLocks = false;
        this.villageAppliedTo = 0;
        this.playerAppliedTo = -10L;
        this.settings = new Permissions();
        this.moreSettings = new Permissions();
        this.extraSettings = new Permissions();
        this.id = aId;
        this.villageid = aVillageid;
        this.name = aRoleName;
        this.mayTerraform = aMayTerraform;
        this.mayCuttrees = aMayCuttrees;
        this.mayMine = aMayMine;
        this.mayFarm = aMayFarm;
        this.mayBuild = aMayBuild;
        this.mayHire = aMayHire;
        this.mayInvite = aMayInvite;
        this.mayDestroy = aMayDestroy;
        this.mayManageRoles = aMayManageRoles;
        this.mayExpand = aMayExpand;
        this.mayPassAllFences = aMayPassAllFences;
        this.mayLockFences = aMayLockFences;
        this.mayAttackCitizens = aMayAttackCitizens;
        this.mayAttackNonCitizens = aMayAttackNonCitizens;
        this.mayFish = aMayFish;
        this.mayCutOldTrees = aMayCutOldTrees;
        this.mayPushPullTurn = aMayPushPullTurn;
        this.diplomat = aDiplomat;
        this.status = aStatus;
        this.villageAppliedTo = aVillageAppliedTo;
        this.mayUpdateMap = aMayUpdateMap;
        this.mayLead = aMayLead;
        this.mayPickup = aMayPickup;
        this.mayTame = aMayTame;
        this.mayLoad = aMayLoad;
        this.mayButcher = aMayButcher;
        this.mayAttachLock = aMayAttachLock;
        this.playerAppliedTo = aPlayerAppliedTo;
        if (this.getStatus() == 2) {
            this.settings.setPermissionBits(-1);
            this.moreSettings.setPermissionBits(-1);
            this.extraSettings.setPermissionBits(-1);
        } else {
            this.settings.setPermissionBits(aSettings);
            this.moreSettings.setPermissionBits(aMoreSettings);
            this.extraSettings.setPermissionBits(aExtraSettings);
        }
    }

    public void convertSettings() {
        if (this.status == 2) {
            this.settings.setPermissionBits(-1);
            this.moreSettings.setPermissionBits(-1);
            this.extraSettings.setPermissionBits(-1);
        } else {
            boolean isCitizen;
            boolean isMayor = this.status == 2;
            boolean isAnyone = isMayor || this.status == 3 || this.status == 0 || this.status == 5;
            boolean mayPlaceMerchants = isCitizen = (isMayor || this.status == 3 || this.status == 0) && this.villageAppliedTo == 0;
            try {
                Village village = Villages.getVillage(this.villageid);
                mayPlaceMerchants = village.acceptsMerchants;
            }
            catch (NoSuchVillageException noSuchVillageException) {
                // empty catch block
            }
            this.setCanBreed(isMayor || this.mayLead);
            this.setCanButcher(isMayor || this.mayButcher);
            this.setCanGroom(isMayor || this.mayLead);
            this.setCanLead(isMayor || this.mayLead);
            this.setCanMilkShear(isMayor || this.mayFarm);
            this.setCanSacrifice(isMayor || this.mayButcher);
            this.setCanTame(isMayor || this.mayTame);
            this.setCanBuild(isMayor || this.mayBuild);
            this.setCanDestroyFence(isMayor || isAnyone);
            this.setCanDestroyItems(isMayor || isAnyone);
            this.setCanPickLocks(isMayor || this.mayPickLocks);
            this.setCanPlanBuildings(isMayor || this.mayBuild);
            this.setCanCultivate(isMayor || this.mayTerraform);
            this.setCanDigResource(isMayor || this.mayTerraform);
            this.setCanPack(isMayor || this.mayTerraform);
            this.setCanTerraform(isMayor || this.mayTerraform);
            this.setCanHarvestFields(isMayor || this.mayFarm);
            this.setCanSowFields(isMayor || this.mayFarm);
            this.setCanTendFields(isMayor || this.mayFarm);
            this.setCanChopDownAllTrees(isMayor || this.mayCuttrees);
            this.setCanChopDownOldTrees(isMayor || this.mayCutOldTrees);
            this.setCanCutGrass(isMayor || isAnyone);
            this.setCanHarvestFruit(isMayor || this.mayCuttrees);
            this.setCanMakeLawn(isMayor || this.mayTerraform);
            this.setCanPickSprouts(isMayor || this.mayCuttrees);
            this.setCanPlantFlowers(isMayor || this.mayCuttrees);
            this.setCanPlantSprouts(isMayor || this.mayCuttrees);
            this.setCanPrune(isMayor || this.mayCuttrees);
            this.setCanAttackCitizens(isMayor || this.mayAttackCitizens);
            this.setCanAttackNonCitizens(isMayor || this.mayAttackNonCitizens);
            this.setCanCastDeitySpells(isMayor || isAnyone);
            this.setCanCastSorcerySpells(isMayor || isAnyone);
            this.setCanForageBotanize(isMayor || this.mayFarm);
            this.setCanPave(isMayor || this.mayTerraform);
            this.setCanPlaceMerchants(isMayor || mayPlaceMerchants);
            this.setCanUseMeditationAbility(isMayor || isAnyone);
            this.setCanAttachLocks(isMayor || this.mayAttachLock);
            this.setCanDrop(isMayor || isAnyone);
            this.setCanImproveRepair(isMayor || isAnyone);
            this.setCanLoad(isMayor || this.mayLoad);
            this.setCanPickup(isMayor || this.mayPickup);
            this.setCanPickupPlanted(isMayor || this.mayPickup);
            this.setCanPullPushTurn(isMayor || this.mayPushPullTurn);
            this.setCanUnload(isMayor || this.mayLoad);
            this.setCanMineFloor(isMayor || this.mayMine);
            this.setCanMineIron(isMayor || this.mayMine);
            this.setCanMineOther(isMayor || this.mayMine);
            this.setCanMineRock(isMayor || this.mayMine);
            this.setCanSurface(isMayor || this.mayMine);
            this.setCanTunnel(isMayor || this.mayMine);
            this.SetCanPerformActionsOnAlliedDeeds(isCitizen);
            this.setCanDiplomat(isMayor || this.diplomat);
            this.setCanDestroyAnyBuilding(isMayor || this.mayDestroy);
            this.setCanManageGuards(isMayor || this.mayHire);
            this.setCanInviteCitizens(isMayor || this.mayInvite);
            this.setCanManageCitizenRoles(isMayor || this.mayManageRoles);
            this.setCanManageMap(isMayor || this.mayUpdateMap);
            this.setCanManageReputations(isMayor || this.mayManageRoles);
            this.setCanManageRoles(isMayor || this.mayManageRoles);
            this.setCanManageSettings(isMayor || this.mayManageRoles);
            this.setCanConfigureTwitter(isMayor || this.diplomat);
            this.setCanResizeSettlement(isMayor || this.mayExpand);
        }
    }

    public final String getName() {
        return this.name;
    }

    public final int getVillageId() {
        return this.villageid;
    }

    public final boolean mayAttachLock() {
        return this.moreSettings.hasPermission(MoreRolePermissions.ATTACH_LOCKS.getBit());
    }

    public final boolean mayAttackCitizens() {
        return this.settings.hasPermission(RolePermissions.ATTACK_CITIZENS.getBit());
    }

    public final boolean mayAttackNonCitizens() {
        return this.settings.hasPermission(RolePermissions.ATTACK_NON_CITIZENS.getBit());
    }

    public final boolean mayBrand() {
        return this.extraSettings.hasPermission(ExtraRolePermissions.BRAND.getBit());
    }

    public final boolean mayBreed() {
        return this.moreSettings.hasPermission(MoreRolePermissions.BREED.getBit());
    }

    public final boolean mayBuild() {
        return this.settings.hasPermission(RolePermissions.BUILD.getBit());
    }

    public final boolean mayButcher() {
        return this.settings.hasPermission(RolePermissions.BUTCHER.getBit());
    }

    public final boolean mayCastDeitySpells() {
        return this.settings.hasPermission(RolePermissions.CAST_DEITY_SPELLS.getBit());
    }

    public final boolean mayCastSorcerySpells() {
        return this.settings.hasPermission(RolePermissions.CAST_SORCERY_SPELLS.getBit());
    }

    public final boolean mayChopDownAllTrees() {
        return this.settings.hasPermission(RolePermissions.CHOP_DOWN_ALL_TREES.getBit());
    }

    public final boolean mayChopDownOldTrees() {
        return this.settings.hasPermission(RolePermissions.CHOP_DOWN_OLD_TREES.getBit());
    }

    public final boolean mayConfigureTwitter() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MAY_CONFIGURE_TWITTER.getBit());
    }

    public final boolean mayCultivate() {
        return this.settings.hasPermission(RolePermissions.CULTIVATE.getBit());
    }

    public final boolean mayCutGrass() {
        return this.settings.hasPermission(RolePermissions.CUT_GRASS.getBit());
    }

    public final boolean mayCuttrees() {
        return this.mayCuttrees;
    }

    public final boolean mayCutOldTrees() {
        return this.mayCutOldTrees;
    }

    public final boolean mayDestroy() {
        return this.mayDestroy;
    }

    public final boolean mayDestroyAnyBuilding() {
        return this.moreSettings.hasPermission(MoreRolePermissions.DESTROY_ANY_BUILDING.getBit());
    }

    public final boolean mayDestroyFences() {
        return this.settings.hasPermission(RolePermissions.DESTROY_FENCE.getBit());
    }

    public final boolean mayDestroyItems() {
        return this.settings.hasPermission(RolePermissions.DESTROY_ITEMS.getBit());
    }

    public final boolean mayDigResources() {
        return this.settings.hasPermission(RolePermissions.DIG_RESOURCE.getBit());
    }

    public final boolean isDiplomat() {
        return this.moreSettings.hasPermission(MoreRolePermissions.DIPLOMAT.getBit());
    }

    public final boolean mayDisbandSettlement() {
        return this.status == 2;
    }

    public final boolean mayDrop() {
        return this.moreSettings.hasPermission(MoreRolePermissions.DROP.getBit());
    }

    public final boolean mayExpand() {
        return this.mayExpand;
    }

    public final boolean mayFarm() {
        return this.mayFarm;
    }

    public final boolean mayFish() {
        return this.mayFish;
    }

    public final boolean mayForageAndBotanize() {
        return this.settings.hasPermission(RolePermissions.FORAGE.getBit());
    }

    public final boolean mayGroom() {
        return this.settings.hasPermission(RolePermissions.GROOM.getBit());
    }

    public final boolean mayHarvestFields() {
        return this.settings.hasPermission(RolePermissions.HARVEST_FIELDS.getBit());
    }

    public final boolean mayHarvestFruit() {
        return this.settings.hasPermission(RolePermissions.HARVEST_FRUIT.getBit());
    }

    public final boolean mayHire() {
        return this.mayHire;
    }

    public final boolean mayImproveAndRepair() {
        return this.moreSettings.hasPermission(MoreRolePermissions.IMPROVE_REPAIR.getBit());
    }

    public final boolean mayInviteCitizens() {
        return this.moreSettings.hasPermission(MoreRolePermissions.INVITE_CITIZENS.getBit());
    }

    public final boolean mayLead() {
        return this.settings.hasPermission(RolePermissions.LEAD.getBit());
    }

    public final boolean mayLoad() {
        return this.moreSettings.hasPermission(MoreRolePermissions.LOAD.getBit());
    }

    public final boolean mayLockFences() {
        return this.mayLockFences;
    }

    public final boolean mayMakeLawn() {
        return this.settings.hasPermission(RolePermissions.MAKE_LAWN.getBit());
    }

    public final boolean mayManageAllowedObjects() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MANAGE_ALLOWED_OBJECTS.getBit());
    }

    public final boolean mayManageCitizenRoles() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MANAGE_CITIZEN_ROLES.getBit());
    }

    public final boolean mayManageGuards() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MANAGE_GUARDS.getBit());
    }

    public final boolean mayManageMap() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MANAGE_MAP.getBit());
    }

    public final boolean mayManageReputations() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MANAGE_REPUTATIONS.getBit());
    }

    public final boolean mayManageRoles() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MANAGE_ROLES.getBit());
    }

    public final boolean mayManageSettings() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MANAGE_SETTINGS.getBit());
    }

    public final boolean mayMilkAndShear() {
        return this.settings.hasPermission(RolePermissions.MILK_SHEAR.getBit());
    }

    public final boolean mayMine() {
        return this.mayMine;
    }

    public final boolean mayMineFloor() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MINE_FLOOR.getBit());
    }

    public final boolean mayMineIronVeins() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MINE_IRON.getBit());
    }

    public final boolean mayMineOtherVeins() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MINE_OTHER.getBit());
    }

    public final boolean mayMineRock() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MINE_ROCK.getBit());
    }

    public final boolean mayMineSurface() {
        return this.moreSettings.hasPermission(MoreRolePermissions.SURFACE_MINING.getBit());
    }

    public final boolean mayPack() {
        return this.moreSettings.hasPermission(MoreRolePermissions.PACK.getBit());
    }

    public final boolean mayPassGates() {
        return this.extraSettings.hasPermission(ExtraRolePermissions.PASS_GATES.getBit());
    }

    public final boolean mayPave() {
        return this.moreSettings.hasPermission(MoreRolePermissions.PAVE.getBit());
    }

    public final boolean mayPerformActionsOnAlliedDeeds() {
        return this.moreSettings.hasPermission(MoreRolePermissions.ALLOW_ACTIONS_ON_ALLIED_DEED.getBit());
    }

    public final boolean mayPickLocks() {
        return this.settings.hasPermission(RolePermissions.PICK_LOCKS.getBit());
    }

    public final boolean mayPickSprouts() {
        return this.settings.hasPermission(RolePermissions.PICK_SPROUTS.getBit());
    }

    public final boolean mayPickup() {
        return this.moreSettings.hasPermission(MoreRolePermissions.PICKUP.getBit());
    }

    public final boolean mayPickupPlanted() {
        return this.moreSettings.hasPermission(MoreRolePermissions.PICKUP_PLANTED.getBit());
    }

    public final boolean mayPlaceMerchants() {
        return this.settings.hasPermission(RolePermissions.PLACE_MERCHANTS.getBit());
    }

    public final boolean mayPlanBuildings() {
        return this.settings.hasPermission(RolePermissions.PLAN_BUILDINGS.getBit());
    }

    public final boolean mayPlantFlowers() {
        return this.settings.hasPermission(RolePermissions.PLANT_FLOWERS.getBit());
    }

    public final boolean mayPlantItem() {
        return this.extraSettings.hasPermission(ExtraRolePermissions.PLANT_ITEM.getBit());
    }

    public final boolean mayPlantSprouts() {
        return this.settings.hasPermission(RolePermissions.PLANT_SPROUTS.getBit());
    }

    public final boolean mayPrune() {
        return this.settings.hasPermission(RolePermissions.PRUNE.getBit());
    }

    public final boolean mayPushPullTurn() {
        return this.moreSettings.hasPermission(MoreRolePermissions.PULL_PUSH.getBit());
    }

    public final boolean mayReinforce() {
        return this.moreSettings.hasPermission(MoreRolePermissions.REINFORCE.getBit());
    }

    public final boolean mayResizeSettlement() {
        return this.moreSettings.hasPermission(MoreRolePermissions.RESIZE.getBit());
    }

    public final boolean maySacrifice() {
        return this.settings.hasPermission(RolePermissions.SACRIFICE.getBit());
    }

    public final boolean maySowFields() {
        return this.settings.hasPermission(RolePermissions.SOW_FIELDS.getBit());
    }

    public final boolean mayTame() {
        return this.settings.hasPermission(RolePermissions.TAME.getBit());
    }

    public final boolean mayTendFields() {
        return this.settings.hasPermission(RolePermissions.TEND_FIELDS.getBit());
    }

    public final boolean mayTerraform() {
        return this.settings.hasPermission(RolePermissions.TERRAFORM.getBit());
    }

    public final boolean mayTunnel() {
        return this.moreSettings.hasPermission(MoreRolePermissions.TUNNEL.getBit());
    }

    public final boolean mayUnload() {
        return this.moreSettings.hasPermission(MoreRolePermissions.UNLOAD.getBit());
    }

    public final boolean mayUpdateMap() {
        return this.mayUpdateMap;
    }

    public final boolean mayUseMeditationAbilities() {
        return this.moreSettings.hasPermission(MoreRolePermissions.MEDITATION_ABILITY.getBit());
    }

    public final int getVillageAppliedTo() {
        return this.villageAppliedTo;
    }

    public final long getPlayerAppliedTo() {
        return this.playerAppliedTo;
    }

    public final byte getStatus() {
        return this.status;
    }

    public final int getId() {
        return this.id;
    }

    public void setCanBrand(boolean canBrand) {
        this.extraSettings.setPermissionBit(ExtraRolePermissions.BRAND.getBit(), canBrand);
    }

    public void setCanBreed(boolean canBreed) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.BREED.getBit(), canBreed);
    }

    public void setCanButcher(boolean canButcher) {
        this.settings.setPermissionBit(RolePermissions.BUTCHER.getBit(), canButcher);
    }

    public void setCanGroom(boolean canGroom) {
        this.settings.setPermissionBit(RolePermissions.GROOM.getBit(), canGroom);
    }

    public void setCanLead(boolean canLead) {
        this.settings.setPermissionBit(RolePermissions.LEAD.getBit(), canLead);
    }

    public void setCanMilkShear(boolean canMilkShear) {
        this.settings.setPermissionBit(RolePermissions.MILK_SHEAR.getBit(), canMilkShear);
    }

    public void setCanPassGates(boolean canPassGates) {
        this.extraSettings.setPermissionBit(ExtraRolePermissions.PASS_GATES.getBit(), canPassGates);
    }

    public void setCanSacrifice(boolean canSacrifice) {
        this.settings.setPermissionBit(RolePermissions.SACRIFICE.getBit(), canSacrifice);
    }

    public void setCanTame(boolean canTame) {
        this.settings.setPermissionBit(RolePermissions.TAME.getBit(), canTame);
    }

    public void setCanBuild(boolean canBuild) {
        this.settings.setPermissionBit(RolePermissions.BUILD.getBit(), canBuild);
    }

    public void setCanDestroyFence(boolean canDestroyFence) {
        this.settings.setPermissionBit(RolePermissions.DESTROY_FENCE.getBit(), canDestroyFence);
    }

    public void setCanDestroyItems(boolean canDestroyItems) {
        this.settings.setPermissionBit(RolePermissions.DESTROY_ITEMS.getBit(), canDestroyItems);
    }

    public void setCanPickLocks(boolean canPickLocks) {
        this.settings.setPermissionBit(RolePermissions.PICK_LOCKS.getBit(), canPickLocks);
    }

    public void setCanPlanBuildings(boolean canPlanBuildings) {
        this.settings.setPermissionBit(RolePermissions.PLAN_BUILDINGS.getBit(), canPlanBuildings);
    }

    public void setCanCultivate(boolean canCultivate) {
        this.settings.setPermissionBit(RolePermissions.CULTIVATE.getBit(), canCultivate);
    }

    public void setCanDigResource(boolean canDigResource) {
        this.settings.setPermissionBit(RolePermissions.DIG_RESOURCE.getBit(), canDigResource);
    }

    public void setCanPack(boolean canPack) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.PACK.getBit(), canPack);
    }

    public void setCanTerraform(boolean canTerraform) {
        this.settings.setPermissionBit(RolePermissions.TERRAFORM.getBit(), canTerraform);
    }

    public void setCanHarvestFields(boolean canHarvestFields) {
        this.settings.setPermissionBit(RolePermissions.HARVEST_FIELDS.getBit(), canHarvestFields);
    }

    public void setCanSowFields(boolean canSowFields) {
        this.settings.setPermissionBit(RolePermissions.SOW_FIELDS.getBit(), canSowFields);
    }

    public void setCanTendFields(boolean canTendFields) {
        this.settings.setPermissionBit(RolePermissions.TEND_FIELDS.getBit(), canTendFields);
    }

    public void setCanChopDownAllTrees(boolean canChopDownAllTrees) {
        this.settings.setPermissionBit(RolePermissions.CHOP_DOWN_ALL_TREES.getBit(), canChopDownAllTrees);
    }

    public void setCanChopDownOldTrees(boolean canChopDownOldTrees) {
        this.settings.setPermissionBit(RolePermissions.CHOP_DOWN_OLD_TREES.getBit(), canChopDownOldTrees);
    }

    public void setCanCutGrass(boolean canCutGrass) {
        this.settings.setPermissionBit(RolePermissions.CUT_GRASS.getBit(), canCutGrass);
    }

    public void setCanHarvestFruit(boolean canHarvestFruit) {
        this.settings.setPermissionBit(RolePermissions.HARVEST_FRUIT.getBit(), canHarvestFruit);
    }

    public void setCanMakeLawn(boolean canMakeLawn) {
        this.settings.setPermissionBit(RolePermissions.MAKE_LAWN.getBit(), canMakeLawn);
    }

    public void setCanPickSprouts(boolean canPickSprouts) {
        this.settings.setPermissionBit(RolePermissions.PICK_SPROUTS.getBit(), canPickSprouts);
    }

    public void setCanPlantFlowers(boolean canPlantFlowers) {
        this.settings.setPermissionBit(RolePermissions.PLANT_FLOWERS.getBit(), canPlantFlowers);
    }

    public void setCanPlantSprouts(boolean canPlantSprouts) {
        this.settings.setPermissionBit(RolePermissions.PLANT_SPROUTS.getBit(), canPlantSprouts);
    }

    public void setCanPrune(boolean canPrune) {
        this.settings.setPermissionBit(RolePermissions.PRUNE.getBit(), canPrune);
    }

    public void setCanAttackCitizens(boolean canAttackCitizens) {
        this.settings.setPermissionBit(RolePermissions.ATTACK_CITIZENS.getBit(), canAttackCitizens);
    }

    public void setCanAttackNonCitizens(boolean canAttackNonCitizens) {
        this.settings.setPermissionBit(RolePermissions.ATTACK_NON_CITIZENS.getBit(), canAttackNonCitizens);
    }

    public void setCanCastDeitySpells(boolean canCastDeitySpells) {
        this.settings.setPermissionBit(RolePermissions.CAST_DEITY_SPELLS.getBit(), canCastDeitySpells);
    }

    public void setCanCastSorcerySpells(boolean canCastSorcerySpells) {
        this.settings.setPermissionBit(RolePermissions.CAST_SORCERY_SPELLS.getBit(), canCastSorcerySpells);
    }

    public void setCanForageBotanize(boolean canForageBotanize) {
        this.settings.setPermissionBit(RolePermissions.FORAGE.getBit(), canForageBotanize);
    }

    public void setCanPlaceMerchants(boolean canPlaceMerchants) {
        this.settings.setPermissionBit(RolePermissions.PLACE_MERCHANTS.getBit(), canPlaceMerchants);
    }

    public void setCanPave(boolean canPave) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.PAVE.getBit(), canPave);
    }

    public void setCanUseMeditationAbility(boolean canUseMeditationAbility) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MEDITATION_ABILITY.getBit(), canUseMeditationAbility);
    }

    public void setCanAttachLocks(boolean canAttachLocks) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.ATTACH_LOCKS.getBit(), canAttachLocks);
    }

    public void setCanDrop(boolean canDrop) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.DROP.getBit(), canDrop);
    }

    public void setCanImproveRepair(boolean canImproveRepair) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.IMPROVE_REPAIR.getBit(), canImproveRepair);
    }

    public void setCanLoad(boolean canLoad) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.LOAD.getBit(), canLoad);
    }

    public void setCanPickup(boolean canPickup) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.PICKUP.getBit(), canPickup);
    }

    public void setCanPickupPlanted(boolean canPickupPlanted) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.PICKUP_PLANTED.getBit(), canPickupPlanted);
    }

    public void setCanPlantItem(boolean canPlantItem) {
        this.extraSettings.setPermissionBit(ExtraRolePermissions.PLANT_ITEM.getBit(), canPlantItem);
    }

    public void setCanPullPushTurn(boolean canPullPushTurn) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.PULL_PUSH.getBit(), canPullPushTurn);
    }

    public void setCanUnload(boolean canUnload) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.UNLOAD.getBit(), canUnload);
    }

    public void setCanMineFloor(boolean canMineFloor) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MINE_FLOOR.getBit(), canMineFloor);
    }

    public void setCanMineIron(boolean canMineIronVeins) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MINE_IRON.getBit(), canMineIronVeins);
    }

    public void setCanMineOther(boolean canMineOtherVeins) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MINE_OTHER.getBit(), canMineOtherVeins);
    }

    public void setCanMineRock(boolean canMineRock) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MINE_ROCK.getBit(), canMineRock);
    }

    public void setCanSurface(boolean canMineSurface) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.SURFACE_MINING.getBit(), canMineSurface);
    }

    public void setCanTunnel(boolean canTunnel) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.TUNNEL.getBit(), canTunnel);
    }

    public void setCanReinforce(boolean canReinforce) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.REINFORCE.getBit(), canReinforce);
    }

    public void setCanConfigureTwitter(boolean canConfigureTwitter) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MAY_CONFIGURE_TWITTER.getBit(), canConfigureTwitter);
    }

    public void setCanDiplomat(boolean canDiplomat) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.DIPLOMAT.getBit(), canDiplomat);
    }

    public void setCanDestroyAnyBuilding(boolean canDestroyAnyBuilding) {
        if (this.getStatus() == 1 && canDestroyAnyBuilding) {
            Thread.dumpStack();
            return;
        }
        this.moreSettings.setPermissionBit(MoreRolePermissions.DESTROY_ANY_BUILDING.getBit(), canDestroyAnyBuilding);
    }

    public void setCanInviteCitizens(boolean canInviteCitizens) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.INVITE_CITIZENS.getBit(), canInviteCitizens);
    }

    public void setCanManageAllowedObjects(boolean canManageAllowedObjects) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MANAGE_ALLOWED_OBJECTS.getBit(), canManageAllowedObjects);
    }

    public void setCanManageCitizenRoles(boolean canManageCitizenRoles) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MANAGE_CITIZEN_ROLES.getBit(), canManageCitizenRoles);
    }

    public void setCanManageGuards(boolean canManageGuards) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MANAGE_GUARDS.getBit(), canManageGuards);
    }

    public void setCanManageMap(boolean canManageMap) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MANAGE_MAP.getBit(), canManageMap);
    }

    public void setCanManageReputations(boolean canManageReputations) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MANAGE_REPUTATIONS.getBit(), canManageReputations);
    }

    public void setCanManageRoles(boolean canManageRoles) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MANAGE_ROLES.getBit(), canManageRoles);
    }

    public void setCanManageSettings(boolean canManageSettings) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.MANAGE_SETTINGS.getBit(), canManageSettings);
    }

    public void setCanResizeSettlement(boolean canResizeSettlement) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.RESIZE.getBit(), canResizeSettlement);
    }

    public void SetCanPerformActionsOnAlliedDeeds(boolean canPerformActionsOnAlliedDeeds) {
        this.moreSettings.setPermissionBit(MoreRolePermissions.ALLOW_ACTIONS_ON_ALLIED_DEED.getBit(), canPerformActionsOnAlliedDeeds);
    }

    abstract void create() throws IOException;

    public abstract void setName(String var1) throws IOException;

    public abstract void setMayHire(boolean var1) throws IOException;

    public abstract void setMayBuild(boolean var1) throws IOException;

    public abstract void setMayCuttrees(boolean var1) throws IOException;

    public abstract void setMayMine(boolean var1) throws IOException;

    public abstract void setMayFarm(boolean var1) throws IOException;

    public abstract void setMayManageRoles(boolean var1) throws IOException;

    public abstract void setMayDestroy(boolean var1) throws IOException;

    public abstract void setMayTerraform(boolean var1) throws IOException;

    public abstract void setMayExpand(boolean var1) throws IOException;

    public abstract void setMayInvite(boolean var1) throws IOException;

    public abstract void setMayPassAllFences(boolean var1) throws IOException;

    public abstract void setMayLockFences(boolean var1) throws IOException;

    public abstract void setMayAttackCitizens(boolean var1) throws IOException;

    public abstract void setMayAttackNonCitizens(boolean var1) throws IOException;

    public abstract void setDiplomat(boolean var1) throws IOException;

    public abstract void setVillageAppliedTo(int var1) throws IOException;

    public abstract void setMayFish(boolean var1) throws IOException;

    public abstract void setMayPushPullTurn(boolean var1) throws IOException;

    public abstract void setMayLead(boolean var1) throws IOException;

    public abstract void setMayPickup(boolean var1) throws IOException;

    public abstract void setMayTame(boolean var1) throws IOException;

    public abstract void setMayLoad(boolean var1) throws IOException;

    public abstract void setMayButcher(boolean var1) throws IOException;

    public abstract void setMayAttachLock(boolean var1) throws IOException;

    public abstract void setMayPickLocks(boolean var1) throws IOException;

    public abstract void setMayUpdateMap(boolean var1) throws IOException;

    public abstract void setCutOld(boolean var1) throws IOException;

    public abstract void delete() throws IOException;

    public abstract void save() throws IOException;

    public static enum ExtraRolePermissions implements Permissions.IPermission
    {
        BRAND(0, "Animals", "Brand"),
        PASS_GATES(1, "General", "Pass Gates"),
        PLANT_ITEM(2, "Item Management", "Plant Items"),
        SPARE03(3, "Unknown", "Spare");

        final byte bit;
        final String description;
        final String header1;
        final String header2;
        private static final Permissions.Allow[] types;

        private ExtraRolePermissions(int aBit, String category, String aDescription) {
            this.bit = (byte)aBit;
            this.description = aDescription;
            this.header1 = category;
            this.header2 = "";
        }

        @Override
        public byte getBit() {
            return this.bit;
        }

        @Override
        public int getValue() {
            return 1 << this.bit;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public String getHeader1() {
            return this.header1;
        }

        @Override
        public String getHeader2() {
            return this.header2;
        }

        @Override
        public String getHover() {
            return "";
        }

        public static Permissions.IPermission[] getPermissions() {
            return types;
        }

        static {
            types = Permissions.Allow.values();
        }
    }

    public static enum MoreRolePermissions implements Permissions.IPermission
    {
        MEDITATION_ABILITY(0, "General", "Use Meditation Ability"),
        ATTACH_LOCKS(1, "Item Management", "Attach Locks"),
        IMPROVE_REPAIR(2, "Item Management", "Improve / Repair"),
        LOAD(3, "Item Management", "Load"),
        PICKUP(4, "Item Management", "Pickup"),
        PICKUP_PLANTED(5, "Item Management", "Pickup Planted Items"),
        PULL_PUSH(6, "Item Management", "Pull/Push/Turn"),
        MINE_FLOOR(7, "Mining", "Mine Floor"),
        MINE_IRON(8, "Mining", "Mine Iron Vein"),
        MINE_OTHER(9, "Mining", "Mine Other Vein"),
        MINE_ROCK(10, "Mining", "Mine Rock"),
        SURFACE_MINING(11, "Mining", "Surface Mining"),
        TUNNEL(12, "Mining", "Tunnelling"),
        ALLOW_ACTIONS_ON_ALLIED_DEED(13, "Settlement Management", "Allow Actions on Allied Deeds"),
        DIPLOMAT(14, "Settlement Management", "Diplomat"),
        DESTROY_ANY_BUILDING(15, "Construction", "Destroy Any Building"),
        INVITE_CITIZENS(16, "Settlement Management", "Invite Citizens"),
        MANAGE_CITIZEN_ROLES(17, "Settlement Management", "Manage Citizen Roles"),
        MANAGE_GUARDS(18, "Settlement Management", "Manage Guards"),
        MANAGE_MAP(19, "Settlement Management", "Manage Map"),
        MANAGE_REPUTATIONS(20, "Settlement Management", "Manage Reputations"),
        MANAGE_ROLES(21, "Settlement Management", "Manage Roles"),
        MANAGE_SETTINGS(22, "Settlement Management", "Manage Settings"),
        MAY_CONFIGURE_TWITTER(23, "Settlement Management", "May Configure Twitter"),
        RESIZE(24, "Settlement Management", "Resize"),
        MANAGE_ALLOWED_OBJECTS(25, "Settlement Management", "Manage Allowed Objects"),
        REINFORCE(26, "Mining", "Add/Remove Reinforcements"),
        BREED(27, "Animals", "Breed"),
        PACK(28, "Digging", "Pack"),
        PAVE(29, "General", "Drop"),
        DROP(30, "Item Management", "Drop"),
        UNLOAD(31, "Item Management", "Unload");

        final byte bit;
        final String description;
        final String header1;
        final String header2;
        private static final Permissions.Allow[] types;

        private MoreRolePermissions(int aBit, String category, String aDescription) {
            this.bit = (byte)aBit;
            this.description = aDescription;
            this.header1 = category;
            this.header2 = "";
        }

        @Override
        public byte getBit() {
            return this.bit;
        }

        @Override
        public int getValue() {
            return 1 << this.bit;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public String getHeader1() {
            return this.header1;
        }

        @Override
        public String getHeader2() {
            return this.header2;
        }

        @Override
        public String getHover() {
            return "";
        }

        public static Permissions.IPermission[] getPermissions() {
            return types;
        }

        static {
            types = Permissions.Allow.values();
        }
    }

    public static enum RolePermissions implements Permissions.IPermission
    {
        BUTCHER(0, "Animals", "Butcher"),
        GROOM(1, "Animals", "Groom"),
        LEAD(2, "Animals", "Lead"),
        MILK_SHEAR(3, "Animals", "Milk and Shear"),
        SACRIFICE(4, "Animals", "Sacrifice"),
        TAME(5, "Animals", "Tame"),
        BUILD(6, "Construction", "Build"),
        DESTROY_FENCE(7, "Construction", "Destroy Fence"),
        DESTROY_ITEMS(8, "Construction", "Destroy Items"),
        PICK_LOCKS(9, "Construction", "Pick Locks"),
        PLAN_BUILDINGS(10, "Construction", "Plan Buildings"),
        CULTIVATE(11, "Digging", "Cultivate"),
        DIG_RESOURCE(12, "Digging", "Dig Resource"),
        TERRAFORM(13, "Digging", "Terraform"),
        HARVEST_FIELDS(14, "Farming", "Harvest Fields"),
        SOW_FIELDS(15, "Farming", "Sow Fields"),
        TEND_FIELDS(16, "Farming", "Tend Fields"),
        CHOP_DOWN_ALL_TREES(17, "Forestry/Gardening", "Chop Down All Trees"),
        CHOP_DOWN_OLD_TREES(18, "Forestry/Gardening", "Chop Down Old Trees"),
        CUT_GRASS(19, "Forestry/Gardening", "Cut Grass"),
        HARVEST_FRUIT(20, "Forestry/Gardening", "Harvest Fruit"),
        MAKE_LAWN(21, "Forestry/Gardening", "Make Lawn"),
        PICK_SPROUTS(22, "Forestry/Gardening", "Pick Sprouts"),
        PLANT_FLOWERS(23, "Forestry/Gardening", "Plant Flowers"),
        PLANT_SPROUTS(24, "Forestry/Gardening", "Plant Sprouts"),
        PRUNE(25, "Prune", "Prune"),
        ATTACK_CITIZENS(26, "General", "Attack Citizens"),
        ATTACK_NON_CITIZENS(27, "General", "Attack Non Citizens"),
        CAST_DEITY_SPELLS(28, "General", "Cast Deity Spells"),
        CAST_SORCERY_SPELLS(29, "General", "Cast Sorcery Spells"),
        FORAGE(30, "General", "Forage/Botanize"),
        PLACE_MERCHANTS(31, "General", "May Place Merchants");

        final byte bit;
        final String description;
        final String header1;
        final String header2;
        private static final Permissions.Allow[] types;

        private RolePermissions(int aBit, String category, String aDescription) {
            this.bit = (byte)aBit;
            this.description = aDescription;
            this.header1 = category;
            this.header2 = "";
        }

        @Override
        public byte getBit() {
            return this.bit;
        }

        @Override
        public int getValue() {
            return 1 << this.bit;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public String getHeader1() {
            return this.header1;
        }

        @Override
        public String getHeader2() {
            return this.header2;
        }

        @Override
        public String getHover() {
            return "";
        }

        public static Permissions.IPermission[] getPermissions() {
            return types;
        }

        static {
            types = Permissions.Allow.values();
        }
    }
}

