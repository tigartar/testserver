/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.AllianceQuestion;
import com.wurmonline.server.questions.AltarConversionQuestion;
import com.wurmonline.server.questions.AreaHistoryQuestion;
import com.wurmonline.server.questions.CreateZoneQuestion;
import com.wurmonline.server.questions.CreatureCreationQuestion;
import com.wurmonline.server.questions.CreatureDataQuestion;
import com.wurmonline.server.questions.DeclareWarQuestion;
import com.wurmonline.server.questions.FriendQuestion;
import com.wurmonline.server.questions.GMBuildAllWallsQuestion;
import com.wurmonline.server.questions.GateManagementQuestion;
import com.wurmonline.server.questions.GmSetEnchants;
import com.wurmonline.server.questions.GmSetMedPath;
import com.wurmonline.server.questions.GmSetTraits;
import com.wurmonline.server.questions.GuardManagementQuestion;
import com.wurmonline.server.questions.HideQuestion;
import com.wurmonline.server.questions.ItemCreationQuestion;
import com.wurmonline.server.questions.ItemDataQuestion;
import com.wurmonline.server.questions.ItemRestrictionManagement;
import com.wurmonline.server.questions.LearnSkillQuestion;
import com.wurmonline.server.questions.ManageAllianceQuestion;
import com.wurmonline.server.questions.NewKingQuestion;
import com.wurmonline.server.questions.PaymentQuestion;
import com.wurmonline.server.questions.PeaceQuestion;
import com.wurmonline.server.questions.PlanBridgeQuestion;
import com.wurmonline.server.questions.PlayerPaymentQuestion;
import com.wurmonline.server.questions.PowerManagementQuestion;
import com.wurmonline.server.questions.QuestionTypes;
import com.wurmonline.server.questions.RealDeathQuestion;
import com.wurmonline.server.questions.ReputationQuestion;
import com.wurmonline.server.questions.ServerQuestion;
import com.wurmonline.server.questions.SetDeityQuestion;
import com.wurmonline.server.questions.ShutDownQuestion;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.questions.SinglePriceManageQuestion;
import com.wurmonline.server.questions.TeleportQuestion;
import com.wurmonline.server.questions.TerrainQuestion;
import com.wurmonline.server.questions.TileDataQuestion;
import com.wurmonline.server.questions.TwitSetupQuestion;
import com.wurmonline.server.questions.VillageCitizenManageQuestion;
import com.wurmonline.server.questions.VillageFoundationQuestion;
import com.wurmonline.server.questions.VillageHistoryQuestion;
import com.wurmonline.server.questions.VillageInfo;
import com.wurmonline.server.questions.VillageJoinQuestion;
import com.wurmonline.server.questions.VillageRolesManageQuestion;
import com.wurmonline.server.questions.VillageSettingsManageQuestion;
import com.wurmonline.server.questions.VillageUpkeep;
import com.wurmonline.server.questions.VoiceChatQuestion;
import com.wurmonline.server.questions.WithdrawMoneyQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.SoundNames;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Methods
implements MiscConstants,
QuestionTypes,
ItemTypes,
CounterTypes,
ItemMaterials,
SoundNames,
VillageStatus,
TimeConstants,
MonetaryConstants {
    private static final Logger logger = Logger.getLogger(Methods.class.getName());
    private static Creature jennElector = null;
    private static Creature hotsElector = null;
    private static Item molrStone = null;
    private static Set<Long> kingAspirants = new HashSet<Long>();

    private Methods() {
    }

    static void sendTeleportQuestion(Creature performer, Item source) {
        TeleportQuestion dq = new TeleportQuestion(performer, "Teleportation coordinates", "Set coordinates: x, 0-" + ((1 << Constants.meshSize) - 1) + " y, 0-" + ((1 << Constants.meshSize) - 1) + " or provide a player name.", source.getWurmId());
        dq.sendQuestion();
    }

    static void sendCreateQuestion(Creature performer, Item source) {
        ItemCreationQuestion dq = new ItemCreationQuestion(performer, "Create Item", "Create the item of your liking:", source.getWurmId());
        dq.sendQuestion();
    }

    static void sendTerraformingQuestion(Creature performer, Item source, int tilex, int tiley) {
        TerrainQuestion dq = new TerrainQuestion(performer, source, tilex, tiley);
        dq.sendQuestion();
    }

    static void sendWithdrawMoneyQuestion(Creature performer, Item token) {
        WithdrawMoneyQuestion dq = new WithdrawMoneyQuestion(performer, "Withdraw money", "Withdraw selected amount:", token.getWurmId());
        dq.sendQuestion();
    }

    static void sendSummonQuestion(Creature performer, Item source, int tilex, int tiley, long structureId) {
        CreatureCreationQuestion cq = new CreatureCreationQuestion(performer, "Summon creature", "Summon the creature of your liking:", source.getWurmId(), tilex, tiley, performer.getLayer(), structureId);
        cq.sendQuestion();
    }

    static void sendAltarConversion(Creature performer, Item altar, Deity deity) {
        AltarConversionQuestion cq = new AltarConversionQuestion(performer, "Inscription", "Ancient inscription:", altar.getWurmId(), deity);
        cq.sendQuestion();
    }

    static void sendRealDeathQuestion(Creature performer, Item altar, Deity deity) {
        if (Players.getChampionsFromKingdom(performer.getKingdomId(), deity.getNumber()) < 1) {
            if (Players.getChampionsFromKingdom(performer.getKingdomId()) < 3) {
                RealDeathQuestion cq = new RealDeathQuestion(performer, "Real death", "Offer to become a Champion:", altar.getWurmId(), deity);
                cq.sendQuestion();
            } else {
                performer.getCommunicator().sendNormalServerMessage("Your kingdom does not support more champions right now.");
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage(deity.name + " can not support another champion from your kingdom right now.");
        }
    }

    static void sendFoundVillageQuestion(Creature performer, Item deed) {
        Map<Village, String> decliners;
        if (!performer.isOnSurface()) {
            performer.getCommunicator().sendSafeServerMessage("You cannot found a settlement here below the surface.");
            return;
        }
        VolaTile tile = performer.getCurrentTile();
        if (tile != null) {
            int tx = tile.tilex;
            int ty = tile.tiley;
            int tt = Server.surfaceMesh.getTile(tx, ty);
            if (Tiles.decodeType(tt) == Tiles.Tile.TILE_LAVA.id || Tiles.isMineDoor(Tiles.decodeType(tt))) {
                performer.getCommunicator().sendSafeServerMessage("You cannot found a settlement here.");
                return;
            }
            for (int x = -1; x <= 1; ++x) {
                for (int y = -1; y <= 1; ++y) {
                    int t = Server.surfaceMesh.getTile(tx + x, ty + y);
                    if (Tiles.decodeHeight(t) >= 0) continue;
                    performer.getCommunicator().sendSafeServerMessage("You cannot found a settlement here. Too close to water.");
                    return;
                }
            }
        } else if (tile == null) {
            performer.getCommunicator().sendSafeServerMessage("You cannot found a settlement here.");
            logger.log(Level.WARNING, performer.getName() + " no tile when founding deed.");
            return;
        }
        if (!(decliners = Villages.canFoundVillage(5, 5, 5, 5, tile.tilex, tile.tiley, 0, true, null, performer)).isEmpty()) {
            performer.getCommunicator().sendSafeServerMessage("You cannot found the settlement here:");
            for (Village vill : decliners.keySet()) {
                String reason = decliners.get(vill);
                if (reason.startsWith("has perimeter")) {
                    performer.getCommunicator().sendSafeServerMessage(vill.getName() + " " + reason);
                    continue;
                }
                performer.getCommunicator().sendSafeServerMessage("Some settlement nearby " + reason);
            }
            return;
        }
        if (deed.isNewDeed() || Servers.localServer.testServer || deed.getTemplateId() == 862) {
            Village village = performer.getCitizenVillage();
            try {
                if (village != null && village.getCitizen(performer.getWurmId()).getRole() == village.getRoleForStatus((byte)2)) {
                    performer.getCommunicator().sendNormalServerMessage("You cannot found another settlement while being mayor in one. Give away one of the deeds.");
                } else {
                    VillageFoundationQuestion vf = new VillageFoundationQuestion(performer, "Settlement Application Form", "Welcome to the Settlement Application Form", deed.getWurmId());
                    if (vf != null) {
                        float rot = Creature.normalizeAngle(performer.getStatus().getRotation() + 45.0f);
                        vf.dir = (byte)((int)(rot / 90.0f) * 2);
                        vf.sendIntro();
                    }
                }
            }
            catch (NoSuchRoleException nsr) {
                logger.log(Level.WARNING, nsr.getMessage(), nsr);
                performer.getCommunicator().sendNormalServerMessage("Failed to locate the mayor role for that request. Please contact administration.");
            }
        }
    }

    static void sendManageVillageSettingsQuestion(Creature performer, @Nullable Item deed) {
        long wId = deed == null ? -10L : deed.getWurmId();
        VillageSettingsManageQuestion vs = new VillageSettingsManageQuestion(performer, "Manage settings", "Managing settings.", wId);
        if (vs != null) {
            vs.sendQuestion();
        }
    }

    static void sendManageVillageRolesQuestion(Creature performer, @Nullable Item deed) {
        long wId = deed == null ? -10L : deed.getWurmId();
        VillageRolesManageQuestion vs = new VillageRolesManageQuestion(performer, "Manage roles", "Managing roles and titles.", wId);
        if (vs != null) {
            vs.sendQuestion();
        }
    }

    static void sendManageVillageGatesQuestion(Creature performer, @Nullable Item deed) {
        long wId = deed == null ? -10L : deed.getWurmId();
        GateManagementQuestion vs = new GateManagementQuestion(performer, "Manage gates", "Managing gates.", wId);
        if (vs != null) {
            vs.sendQuestion();
        }
    }

    static void sendManageVillageGuardsQuestion(Creature performer, @Nullable Item deed) {
        long wId = deed == null ? -10L : deed.getWurmId();
        GuardManagementQuestion gm = new GuardManagementQuestion(performer, "Guard management", "Manage guards", wId);
        gm.sendQuestion();
    }

    static void sendManageVillageCitizensQuestion(Creature performer, @Nullable Item deed) {
        long wId = deed == null ? -10L : deed.getWurmId();
        VillageCitizenManageQuestion vc = new VillageCitizenManageQuestion(performer, "Citizen management", "Set statuses of citizens.", wId);
        vc.setSelecting(true);
        vc.sendQuestion();
    }

    static void sendExpandVillageQuestion(Creature performer, @Nullable Item deed) {
        try {
            Village village;
            long dId = -10L;
            if (deed == null) {
                village = performer.getCitizenVillage();
                dId = village.getDeedId();
            } else {
                dId = deed.getWurmId();
                int oldVill = deed.getData2();
                village = Villages.getVillage(oldVill);
            }
            try {
                long coolDown = System.currentTimeMillis() - 3600000L;
                if (coolDown < village.getToken().getLastOwnerId()) {
                    performer.getCommunicator().sendNormalServerMessage("The settlement has been attacked, or been under siege recently. You need to wait " + Server.getTimeFor(village.getToken().getLastOwnerId() - coolDown) + ".");
                    return;
                }
            }
            catch (NoSuchItemException e) {
                e.printStackTrace();
            }
            if (village.isDisbanding()) {
                performer.getCommunicator().sendNormalServerMessage("This settlement is disbanding. You can not change these settings now.");
                return;
            }
            if (village.plan.isUnderSiege()) {
                performer.getCommunicator().sendNormalServerMessage("This settlement is under siege. You can not change these settings now.");
                return;
            }
            if (village.isActionAllowed((short)76, performer)) {
                VillageFoundationQuestion vf = new VillageFoundationQuestion(performer, "Settlement Size", "Stage One - The size of your settlement", dId);
                if (vf != null) {
                    vf.setSequence(1);
                    vf.tokenx = village.getTokenX();
                    vf.tokeny = village.getTokenY();
                    vf.surfaced = village.isOnSurface();
                    vf.initialPerimeter = village.getPerimeterSize();
                    vf.democracy = village.isDemocracy();
                    vf.spawnKingdom = village.kingdom;
                    vf.motto = village.getMotto();
                    vf.villageName = village.getName();
                    vf.selectedWest = vf.tokenx - village.getStartX();
                    vf.selectedEast = village.getEndX() - vf.tokenx;
                    vf.selectedNorth = vf.tokeny - village.getStartY();
                    vf.selectedSouth = village.getEndY() - vf.tokeny;
                    float rot = Creature.normalizeAngle(performer.getStatus().getRotation() + 45.0f);
                    vf.dir = (byte)((int)(rot / 90.0f) * 2);
                    vf.selectedGuards = village.plan.getNumHiredGuards();
                    vf.setSize();
                    vf.checkDeedItem();
                    vf.expanding = true;
                    vf.sendQuestion();
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You are not allowed to use this deed here. Ask the mayor to set the permissions on the management deed to allow you to expand.");
            }
        }
        catch (NoSuchVillageException nsv) {
            performer.getCommunicator().sendNormalServerMessage("Failed to localize the settlement for that deed.");
        }
    }

    static void setVillageToken(Creature performer, Item token) {
        int tiley;
        int tilex = performer.getTileX();
        Village village = Zones.getVillage(tilex, tiley = performer.getTileY(), performer.isOnSurface());
        if (village == null) {
            performer.getCommunicator().sendNormalServerMessage("No settlement here. You cannot plant the token.");
        } else {
            VolaTile tile = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
            if (tile != null) {
                if (tile.getStructure() != null) {
                    performer.getCommunicator().sendNormalServerMessage("You cannot plant the token inside.");
                    return;
                }
                Fence[] fences = tile.getFencesForLevel(0);
                if (fences.length > 0) {
                    performer.getCommunicator().sendNormalServerMessage("You cannot plant the token on fences and walls.");
                    return;
                }
            }
            Item oldToken = null;
            try {
                oldToken = village.getToken();
            }
            catch (NoSuchItemException noSuchItemException) {
                // empty catch block
            }
            try {
                village.setTokenId(token.getWurmId());
                token.setData2(village.getId());
                Item parent = token.getParent();
                parent.dropItem(token.getWurmId(), false);
                try {
                    token.setPosXY(performer.getStatus().getPositionX(), performer.getStatus().getPositionY());
                    Zone zone = Zones.getZone(tilex, tiley, performer.isOnSurface());
                    zone.addItem(token);
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.WARNING, nsz.getMessage(), nsz);
                    performer.getCommunicator().sendNormalServerMessage("You can't place the token here.");
                    return;
                }
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Village: " + village.getId() + ", token: " + token.getWurmId() + " - " + iox.getMessage(), iox);
                performer.getCommunicator().sendNormalServerMessage("A server error occured. Please report this.");
                return;
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, "Village: " + village.getId() + ", token: " + token.getWurmId() + " - " + nsi.getMessage(), nsi);
                performer.getCommunicator().sendNormalServerMessage("A server error occured. Please report this.");
                return;
            }
            if (oldToken != null) {
                Items.destroyItem(oldToken.getWurmId());
            }
        }
    }

    static final boolean drainCoffers(Creature performer, Village village, float counter, Item token, Action act) {
        boolean done = true;
        if (!performer.isFriendlyKingdom(village.kingdom) || village.isEnemy(performer.getCitizenVillage())) {
            if (village.guards != null && village.guards.size() > 0) {
                performer.getCommunicator().sendNormalServerMessage("The guards prevent you from draining the coffers.");
                done = true;
            } else {
                if (token.isOnSurface() != performer.isOnSurface()) {
                    performer.getCommunicator().sendNormalServerMessage("You can't reach the " + token.getName() + " now.");
                    return true;
                }
                if (!performer.isWithinDistanceTo(token.getPosX(), token.getPosY(), token.getPosZ(), 2.0f)) {
                    performer.getCommunicator().sendNormalServerMessage("You are too far away from the " + token.getName() + " to do that.");
                    return true;
                }
                if (token.getFloorLevel() != performer.getFloorLevel()) {
                    performer.getCommunicator().sendNormalServerMessage("You must be on the same floor level as the " + token.getName() + ".");
                    return true;
                }
                GuardPlan plan = village.plan;
                if (plan != null) {
                    long nextDrain = plan.getTimeToNextDrain();
                    if (nextDrain < 0L) {
                        long moneyDrained = plan.getMoneyDrained();
                        if (moneyDrained > 1L) {
                            done = false;
                            boolean insta = performer.getPower() >= 5;
                            int time = 300;
                            if (counter == 1.0f) {
                                act.setTimeLeft(time);
                                performer.getCommunicator().sendNormalServerMessage("You start to search for gold in the coffers of " + village.getName() + ".");
                                Server.getInstance().broadCastAction(performer.getName() + " starts to rummage through the coffers of " + village.getName() + ", looking for coins.", performer, 5);
                                performer.sendActionControl(Actions.actionEntrys[350].getVerbString(), true, time);
                            } else {
                                time = act.getTimeLeft();
                            }
                            if (counter * 10.0f > (float)time || insta) {
                                boolean enemyHomeServer;
                                Item[] coins;
                                done = true;
                                Change change = Economy.getEconomy().getChangeFor(moneyDrained / 2L);
                                performer.getCommunicator().sendNormalServerMessage("You find " + change.getChangeString() + " in the coffers of " + village.getName() + ".");
                                Server.getInstance().broadCastAction(performer.getName() + " proudly displays the " + change.getChangeString() + " " + performer.getHeSheItString() + " found in the coffers of " + village.getName() + ".", performer, 5);
                                for (Item lCoin : coins = Economy.getEconomy().getCoinsFor(moneyDrained / 2L)) {
                                    performer.getInventory().insertItem(lCoin, true);
                                }
                                plan.drainMoney();
                                performer.achievement(45);
                                village.addHistory(performer.getName(), "drained " + change.getChangeString() + " from the coffers.");
                                if (village.plan != null && village.plan.getNumHiredGuards() >= 5 && performer.isChampion() && village.kingdom != performer.getKingdomId()) {
                                    performer.modifyChampionPoints(3);
                                    Servers.localServer.createChampTwit(performer.getName() + " drains " + village.getName() + " and gains 3 champion points");
                                }
                                boolean bl = enemyHomeServer = Servers.localServer.isChallengeOrEpicServer() && Servers.localServer.HOMESERVER && Servers.localServer.KINGDOM != performer.getKingdomId();
                                if (!Servers.localServer.HOMESERVER || enemyHomeServer) {
                                    MissionTriggers.activateTriggers(performer, -1, 350, token.getWurmId(), 1);
                                }
                            }
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("There is no money to steal in the coffers of " + village.getName() + ".");
                        }
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("The coffer timelock has been activated. Try again in " + Server.getTimeFor(nextDrain) + ".");
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The coffers of " + village.getName() + " echo hollowly.");
                }
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You are not an enemy of " + village.getName() + ".");
        }
        return done;
    }

    static void sendVillageInfo(Creature performer, @Nullable Item villageToken) {
        long wId = villageToken == null ? -10L : villageToken.getWurmId();
        VillageInfo info = new VillageInfo(performer, "Settlement billboard", "", wId);
        info.sendQuestion();
    }

    static void sendManageUpkeep(Creature performer, @Nullable Item villageToken) {
        long wId = villageToken == null ? -10L : villageToken.getWurmId();
        VillageUpkeep upkeep = new VillageUpkeep(performer, "Settlement Upkeep", "", wId);
        upkeep.sendQuestion();
    }

    static void sendVillageHistory(Creature performer, @Nullable Item villageToken) {
        long wId = villageToken == null ? -10L : villageToken.getWurmId();
        VillageHistoryQuestion info = new VillageHistoryQuestion(performer, "Settlement history", "", wId);
        info.sendQuestion();
    }

    static void sendAreaHistory(Creature performer, @Nullable Item villageToken) {
        long wId = villageToken == null ? -10L : villageToken.getWurmId();
        AreaHistoryQuestion info = new AreaHistoryQuestion(performer, "Area history", "", wId);
        info.sendQuestion();
    }

    public static void sendJoinVillageQuestion(Creature performer, Creature invited) {
        if (performer.getKingdomId() == invited.getKingdomId()) {
            Citizen citiz;
            VillageRole role;
            Village vill = invited.getCitizenVillage();
            if (vill != null && (role = (citiz = vill.getCitizen(invited.getWurmId())).getRole()).getStatus() == 2) {
                performer.getCommunicator().sendNormalServerMessage(invited.getName() + " is the mayor of " + vill.getName() + ". He can't join another settlement.");
                return;
            }
            try {
                Village village = performer.getCitizenVillage();
                if (village != null && village.acceptsNewCitizens()) {
                    if (village.kingdom != 3 && invited.getReputation() < 0) {
                        performer.getCommunicator().sendNormalServerMessage(invited.getName() + " has negative reputation and may not join a settlement now.");
                        return;
                    }
                    VillageJoinQuestion vj = new VillageJoinQuestion(performer, "Settlement invitation", "Invitation to become citizen of a settlement.", invited.getWurmId());
                    vj.sendQuestion();
                    performer.getCommunicator().sendNormalServerMessage("You invite " + invited.getName() + " to join " + village.getName() + ".");
                } else {
                    SimplePopup pp = new SimplePopup(performer, "Max citizens reached", "The settlement does not accept more citizens right now");
                    pp.setToSend("Every settlement has a maximum amount of citizens depending on their size. You may unlimit the amount of allowed citizens in the citizen management or settlement management forms. As long as " + village.getName() + " has more than " + village.getMaxCitizens() + " player citizens your upkeep is doubled.");
                    pp.sendQuestion();
                }
            }
            catch (NoSuchCreatureException nsc) {
                logger.log(Level.WARNING, "Failed to locate creature " + invited.getName(), nsc);
                performer.getCommunicator().sendNormalServerMessage("Failed to locate the creature for that invitation.");
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "Failed to locate player " + invited.getName(), nsp);
                performer.getCommunicator().sendNormalServerMessage("Failed to locate the player for that invitation.");
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static void sendVillagePeaceQuestion(Creature performer, Creature invited) {
        Village vill = invited.getCitizenVillage();
        if (vill == null) return;
        if (!vill.mayDoDiplomacy(invited)) {
            performer.getCommunicator().sendNormalServerMessage(invited.getName() + " may not do diplomacy in the name of " + vill.getName() + ". He cannot give you peace.");
            return;
        }
        Village village = performer.getCitizenVillage();
        if (village == null) return;
        if (!village.mayDoDiplomacy(performer)) {
            performer.getCommunicator().sendNormalServerMessage("You may not do diplomacy in the name of " + village.getName() + ". You cannot offer peace.");
            return;
        }
        try {
            PeaceQuestion pq = new PeaceQuestion(performer, "Peace offer", "Will you accept peace?", invited.getWurmId());
            pq.sendQuestion();
            return;
        }
        catch (NoSuchCreatureException nsc) {
            logger.log(Level.WARNING, "Failed to locate creature " + invited.getName(), nsc);
            performer.getCommunicator().sendNormalServerMessage("Failed to locate the creature for that invitation.");
            return;
        }
        catch (NoSuchPlayerException nsp) {
            logger.log(Level.WARNING, "Failed to locate player " + invited.getName(), nsp);
            performer.getCommunicator().sendNormalServerMessage("Failed to locate the player for that invitation.");
            return;
        }
    }

    static void sendWarDeclarationQuestion(Creature performer, Village targetVillage) {
        Village village;
        if (targetVillage != null && (village = performer.getCitizenVillage()) != null) {
            if (village.mayDoDiplomacy(performer)) {
                try {
                    DeclareWarQuestion pq = new DeclareWarQuestion(performer, "War declaration", "Will you declare war?", targetVillage.getId());
                    pq.sendQuestion();
                }
                catch (NoSuchCreatureException nsc) {
                    logger.log(Level.WARNING, "Failed to locate creature " + performer.getName(), nsc);
                    performer.getCommunicator().sendNormalServerMessage("Failed to locate the creature for that declaration.");
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, "Failed to locate player " + performer.getName(), nsp);
                    performer.getCommunicator().sendNormalServerMessage("Failed to locate the player for that declaration.");
                }
                catch (NoSuchVillageException nsp) {
                    logger.log(Level.WARNING, "Failed to locate village " + targetVillage.getName(), nsp);
                    performer.getCommunicator().sendNormalServerMessage("Failed to locate the village for that declaration.");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You may not do diplomacy in the name of " + village.getName() + ". You cannot declare war.");
                return;
            }
        }
    }

    static void sendShutdownQuestion(Creature performer, Item wand) {
        ShutDownQuestion vs = new ShutDownQuestion(performer, "Shutting down the server", "Select the number of minutes and seconds to shutdown as well as the reason for it.", wand.getWurmId());
        vs.sendQuestion();
    }

    static void sendHideQuestion(Creature performer, Item wand, Item target) {
        HideQuestion hs = new HideQuestion(performer, "Hiding " + target.getName(), "Do you wish to hide the " + target.getName() + "?", target.getWurmId());
        hs.sendQuestion();
    }

    static void sendPaymentQuestion(Creature performer, Item wand) {
        PaymentQuestion vs = new PaymentQuestion(performer, "Setting payment expiretime for a player.", "Select the number of days and months before the subscription expires.", wand.getWurmId());
        vs.sendQuestion();
    }

    static void sendPlayerPaymentQuestion(Creature performer) {
        PlayerPaymentQuestion vs = new PlayerPaymentQuestion(performer);
        vs.sendQuestion();
    }

    static void sendPowerManagementQuestion(Creature performer, Item wand) {
        PowerManagementQuestion vs = new PowerManagementQuestion(performer, "Setting the power status for a player.", "Set the power of the player to the selected level.", wand.getWurmId());
        vs.sendQuestion();
    }

    static void sendFaithManagementQuestion(Creature performer, Item wand) {
        SetDeityQuestion dq = new SetDeityQuestion(performer, "Setting the deity for a player.", "Set the deity of the player.", wand.getWurmId());
        dq.sendQuestion();
    }

    static void sendConfigureTwitter(Creature performer, long target, boolean village, String name) {
        TwitSetupQuestion twq = new TwitSetupQuestion(performer, "Twitter", "Configure Twitter for " + name, target, village);
        twq.sendQuestion();
    }

    static void sendCreateZone(Creature performer) {
        CreateZoneQuestion twq = new CreateZoneQuestion(performer);
        twq.sendQuestion();
    }

    static void sendServerManagementQuestion(Creature performer, long target) {
        ServerQuestion dq = new ServerQuestion(performer, "Wurm servers.", "Wurm servers management", target);
        dq.sendQuestion();
    }

    static void sendTileDataQuestion(Creature performer, Item wand, int tilex, int tiley) {
        TileDataQuestion dq = new TileDataQuestion(performer, "Setting data for tile at " + tilex + ", " + tiley, "Set the data:", tilex, tiley, wand.getWurmId());
        dq.sendQuestion();
    }

    static void sendLearnSkillQuestion(Creature performer, Item wand, long target) {
        Creature creature = null;
        if (WurmId.getType(target) == 1 || WurmId.getType(target) == 0) {
            try {
                creature = Server.getInstance().getCreature(target);
            }
            catch (NoSuchCreatureException nsc) {
                performer.getCommunicator().sendNormalServerMessage("Failed to locate the creature for that request!");
            }
            catch (NoSuchPlayerException nsp) {
                performer.getCommunicator().sendNormalServerMessage("Failed to locate the player for that request!");
            }
        }
        LearnSkillQuestion ls = null;
        ls = creature != null ? new LearnSkillQuestion(performer, "Imbue with skill", "Set the skill of " + creature.getName() + " to the value of your choice:", target) : new LearnSkillQuestion(performer, "Set your skill", "Set or learn a skill:", target);
        ls.sendQuestion();
    }

    static final void sendAllianceQuestion(Creature performer, Creature target) {
        if (!target.isFighting() && target.hasLink()) {
            AllianceQuestion aq = new AllianceQuestion(target, "Alliance invitation", "Request to form a village alliance:", performer.getWurmId());
            aq.sendQuestion();
            performer.getCommunicator().sendNormalServerMessage("You send an elaborate invitation to form a high and mighty alliance to " + target.getName() + ".");
        } else {
            performer.getCommunicator().sendNormalServerMessage(target.getName() + " does not answer questions right now.");
        }
    }

    static final void sendFriendQuestion(Creature performer, Creature target) {
        if (performer.getKingdomId() == target.getKingdomId()) {
            if (!target.isFighting() && target.hasLink()) {
                FriendQuestion fq = new FriendQuestion(target, "Friend list invitation", "Request to add you to the friend list:", performer.getWurmId());
                fq.sendQuestion();
                performer.getCommunicator().sendNormalServerMessage("You ask " + target.getName() + " for permission to add " + target.getHimHerItString() + " to your friends list.");
            } else {
                performer.getCommunicator().sendNormalServerMessage(target.getName() + " does not answer questions right now.");
            }
        }
    }

    static final void sendManageAllianceQuestion(Creature performer, @Nullable Item villageToken) {
        long wId = villageToken == null ? -10L : villageToken.getWurmId();
        ManageAllianceQuestion aq = new ManageAllianceQuestion(performer, "Manage alliances", "Select an alliance to break:", wId);
        aq.sendQuestion();
    }

    static final void sendSinglePriceQuestion(Creature responder, Item target) {
        SinglePriceManageQuestion spm = new SinglePriceManageQuestion(responder, "Price management", "Set the desired price:", target.getWurmId());
        spm.sendQuestion();
    }

    static final void sendSetDataQuestion(Creature responder, Item target) {
        ItemDataQuestion spm = new ItemDataQuestion(responder, "Item data", "Set the desired data:", target.getWurmId());
        spm.sendQuestion();
    }

    static final void sendSetDataQuestion(Creature responder, Creature target) {
        CreatureDataQuestion spm = new CreatureDataQuestion(responder, target);
        spm.sendQuestion();
    }

    static final void sendItemRestrictionManagement(Creature responder, Permissions.IAllow target, long wurmId) {
        ItemRestrictionManagement irm = new ItemRestrictionManagement(responder, target, wurmId);
        irm.sendQuestion();
    }

    static final void sendReputationManageQuestion(Creature responder, @Nullable Item target) {
        long wId = target == null ? -10L : target.getWurmId();
        ReputationQuestion spm = new ReputationQuestion(responder, "Reputation management", "Set the reputation levels:", wId);
        spm.sendQuestion();
    }

    public static final boolean discardSellItem(Creature performer, Action act, Item discardItem, float counter) {
        Shop kingsShop;
        boolean toReturn = false;
        String message = "That item cannot be sold this way.";
        if (discardItem.isNoDiscard() || discardItem.isTemporary()) {
            if (act.getNumber() == 600) {
                message = "That item cannot be discarded.";
            }
            performer.getCommunicator().sendNormalServerMessage(message);
            return true;
        }
        if (discardItem.getOwnerId() != performer.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You need to carry the item in order to sell it.");
            return true;
        }
        if (discardItem.isInstaDiscard()) {
            if (act.getNumber() == 600) {
                performer.getCommunicator().sendNormalServerMessage("You break it down in little pieces and throw it away.");
                Items.destroyItem(discardItem.getWurmId());
                return true;
            }
            performer.getCommunicator().sendNormalServerMessage(message);
            return true;
        }
        if (performer.getMoneyEarnedBySellingLastHour() > 500L) {
            performer.getCommunicator().sendNormalServerMessage("You have sold your quota for now.");
            return true;
        }
        if (counter == 1.0f) {
            kingsShop = Economy.getEconomy().getKingsShop();
            if (kingsShop.getMoney() <= 100000L) {
                performer.getCommunicator().sendNormalServerMessage("There are apparently no coins in the coffers at the moment.");
                return true;
            }
            int time = 30;
            performer.sendActionControl("Selling", true, 30);
            if (Constants.maintaining) {
                performer.getCommunicator().sendNormalServerMessage("The server is shutting down so the shop is closed for now.");
                return true;
            }
        }
        if (counter > 3.0f) {
            toReturn = true;
            kingsShop = Economy.getEconomy().getKingsShop();
            if (kingsShop.getMoney() > 100000L) {
                long percentMod = 0L;
                if (!Servers.localServer.HOMESERVER) {
                    if (Server.rand.nextFloat() < Zones.getPercentLandForKingdom(performer.getKingdomId()) / 100.0f) {
                        percentMod = 1L;
                    }
                    if (Server.rand.nextInt(10) < Items.getBattleCampControl(performer.getKingdomId())) {
                        ++percentMod;
                    }
                }
                long value = (long)discardItem.getCurrentQualityLevel() / 10L + 1L + percentMod;
                performer.addMoneyEarnedBySellingLastHour(value);
                kingsShop.setMoney(kingsShop.getMoney() - value);
                Items.destroyItem(discardItem.getWurmId());
                if (performer.checkCoinAward(1000)) {
                    performer.getCommunicator().sendSafeServerMessage("The king awards you with a rare coin!");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("There are apparently no coins in the coffers at the moment.");
                return true;
            }
        }
        return toReturn;
    }

    static final boolean disbandVillage(Creature performer, Item villageToken, float counter) {
        boolean insta = false;
        boolean toReturn = true;
        boolean settings = false;
        if (performer.getPower() > 3) {
            insta = true;
        }
        try {
            Village vill = Villages.getVillage(villageToken.getData2());
            if (vill == performer.getCitizenVillage() && vill.isActionAllowed((short)348, performer) || insta) {
                settings = true;
            }
            if (!vill.isDisbanding()) {
                if (settings) {
                    toReturn = false;
                    if (counter == 1.0f && !insta) {
                        int time = 3000;
                        performer.sendActionControl(Actions.actionEntrys[348].getVerbString(), true, 3000);
                        performer.getCommunicator().sendNormalServerMessage("You start to disband the village of " + vill.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getName() + " starts to disband the village of " + vill.getName() + ".", performer, 5);
                        vill.broadCastMessage(new Message(performer, 3, "Village", "<" + performer.getName() + "> starts to disband the village of " + vill.getName() + "."));
                        vill.broadCastAlert("Any traders who are citizens of " + vill.getName() + " will disband without refund!");
                    }
                    if (counter > 300.0f || insta || vill.getMayor().getId() == performer.getWurmId()) {
                        toReturn = true;
                        if (insta) {
                            vill.disband(performer.getName());
                        } else {
                            vill.startDisbanding(performer, performer.getName(), performer.getWurmId());
                            if (vill.getMayor().getId() == performer.getWurmId() && vill.getDiameterX() < 30 && vill.getDiameterY() < 30) {
                                Village[] allies;
                                performer.getCommunicator().sendNormalServerMessage("Your settlement is disbanding. It will be disbanded in about an hour.");
                                if (!Servers.localServer.isFreeDeeds() || Servers.localServer.isUpkeep()) {
                                    performer.getCommunicator().sendAlertServerMessage("Do not change server during this process. You may not receive the money from the coffers in that case.");
                                }
                                if (Servers.localServer.isFreeDeeds() && Servers.localServer.isUpkeep() && vill.getCreationDate() < System.currentTimeMillis() + 2419200000L) {
                                    performer.getCommunicator().sendAlertServerMessage("Free deeding is enabled and your settlement is less than 30 days old. If you disband now, you will not receive a refund.");
                                }
                                Server.getInstance().broadCastAction(performer.getName() + " has set " + vill.getName() + " to disband immediately.", performer, 5);
                                vill.broadCastMessage(new Message(performer, 3, "Village", "<" + performer.getName() + "> has set " + vill.getName() + " to disband immediately."));
                                for (Village lAllie : allies = vill.getAllies()) {
                                    lAllie.broadCastMessage(new Message(performer, 3, "Village", "<" + performer.getName() + "> has set " + vill.getName() + " to disband immediately."));
                                }
                            } else {
                                Village[] allies;
                                String hours = "24 hours";
                                performer.getCommunicator().sendNormalServerMessage(vill.getName() + " will disband in " + "24 hours" + ".");
                                if (Servers.localServer.isUpkeep() || !Servers.localServer.isFreeDeeds()) {
                                    performer.getCommunicator().sendNormalServerMessage("If the mayor is still on the same server when the deed disbands he or she should receive part of the money that is left in the coffers.");
                                }
                                Server.getInstance().broadCastAction(performer.getName() + " has set " + vill.getName() + " to disband in " + "24 hours" + ".", performer, 5);
                                vill.broadCastMessage(new Message(performer, 3, "Village", "<" + performer.getName() + "> has set " + vill.getName() + " to disband in " + "24 hours" + "."));
                                for (Village lAllie : allies = vill.getAllies()) {
                                    lAllie.broadCastMessage(new Message(performer, 3, "Village", "<" + performer.getName() + "> has set " + vill.getName() + " to disband in " + "24 hours" + "."));
                                }
                            }
                        }
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage(vill.getName() + " may not be disbanded right now.");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage(vill.getName() + " is already disbanding.");
            }
        }
        catch (NoSuchVillageException nsv) {
            performer.getCommunicator().sendAlertServerMessage("No village found for that request.");
            toReturn = true;
        }
        return toReturn;
    }

    static final boolean preventDisbandVillage(Creature performer, Item villageToken, float counter) {
        boolean toReturn;
        block14: {
            boolean insta = false;
            toReturn = false;
            if (performer.getPower() > 3) {
                insta = true;
            }
            try {
                Village vill = Villages.getVillage(villageToken.getData2());
                if (vill.isDisbanding()) {
                    if (counter == 1.0f) {
                        Village citizVill = performer.getCitizenVillage();
                        if (citizVill != null && citizVill.equals(vill)) {
                            int time = 300;
                            performer.sendActionControl(Actions.actionEntrys[349].getVerbString(), true, 300);
                            performer.getCommunicator().sendNormalServerMessage("You start to salvage the settlement of " + vill.getName() + ".");
                            Server.getInstance().broadCastAction(performer.getName() + " starts to salvage the settlement of " + vill.getName() + ".", performer, 5);
                            vill.broadCastMessage(new Message(performer, 3, "Village", "<" + performer.getName() + "> starts to salvage the settlement of " + vill.getName() + "."));
                            try {
                                Player player = Players.getInstance().getPlayer(vill.getDisbander());
                                player.getCommunicator().sendAlertServerMessage(performer.getName() + " is trying to salvage the settlement of " + vill.getName() + "!");
                            }
                            catch (NoSuchPlayerException noSuchPlayerException) {}
                        } else {
                            if (!insta) {
                                performer.getCommunicator().sendNormalServerMessage("You need to be citizen to salvage the settlement of " + vill.getName() + ".");
                            }
                            toReturn = true;
                        }
                    }
                    if (!(counter > 30.0f) && !insta) break block14;
                    toReturn = true;
                    try {
                        try {
                            Player player = Players.getInstance().getPlayer(vill.getDisbander());
                            player.getCommunicator().sendAlertServerMessage(performer.getName() + " successfully salvaged the settlement of " + vill.getName() + "!");
                        }
                        catch (NoSuchPlayerException player) {
                            // empty catch block
                        }
                        vill.setDisbandTime(0L);
                        vill.setDisbander(-10L);
                        performer.getCommunicator().sendNormalServerMessage(vill.getName() + " is salvaged for now.");
                        Server.getInstance().broadCastAction(performer.getName() + " has salvaged " + vill.getName() + ".", performer, 5);
                        vill.broadCastMessage(new Message(performer, 3, "Village", "<" + performer.getName() + "> has salvaged the settlement of " + vill.getName() + "."));
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName() + " " + iox.getMessage(), iox);
                    }
                    break block14;
                }
                toReturn = true;
                performer.getCommunicator().sendNormalServerMessage(vill.getName() + " does not need salvaging right now.");
            }
            catch (NoSuchVillageException nsv) {
                performer.getCommunicator().sendAlertServerMessage("No settlement found for that request.");
                toReturn = true;
            }
        }
        return toReturn;
    }

    public static final String getTimeString(long timeleft) {
        String times = "";
        if (timeleft < 60000L) {
            long secs = timeleft / 1000L;
            times = times + secs + " seconds";
        } else {
            String aft;
            long daysleft = timeleft / 86400000L;
            long hoursleft = (timeleft - daysleft * 86400000L) / 3600000L;
            long minutesleft = (timeleft - daysleft * 86400000L - hoursleft * 3600000L) / 60000L;
            if (daysleft > 0L) {
                times = times + daysleft + " days";
            }
            if (hoursleft > 0L) {
                aft = "";
                if (daysleft > 0L && minutesleft > 0L) {
                    times = times + ", ";
                    aft = aft + " and ";
                } else if (daysleft > 0L) {
                    times = times + " and ";
                } else if (minutesleft > 0L) {
                    aft = aft + " and ";
                }
                times = times + hoursleft + " hours" + aft;
            }
            if (minutesleft > 0L) {
                aft = "";
                if (daysleft > 0L && hoursleft == 0L) {
                    aft = " and ";
                }
                times = times + aft + minutesleft + " minutes";
            }
        }
        if (times.length() == 0) {
            times = "nothing";
        }
        return times;
    }

    static boolean castSpell(Creature performer, Spell spell, Item item, float counter) {
        return spell.run(performer, item, counter);
    }

    static boolean castSpell(Creature performer, Spell spell, Creature target, float counter) {
        return spell.run(performer, target, counter);
    }

    static boolean castSpell(Creature performer, Spell spell, int tilex, int tiley, int layer, int heightOffset, Tiles.TileBorderDirection dir, float counter) {
        return spell.run(performer, tilex, tiley, layer, heightOffset, dir, counter);
    }

    static boolean castSpell(Creature performer, Spell spell, Wound target, float counter) {
        return spell.run(performer, target, counter);
    }

    static boolean castSpell(Creature performer, Spell spell, int tilex, int tiley, int layer, int heightOffset, float counter) {
        return spell.run(performer, tilex, tiley, layer, heightOffset, counter);
    }

    public static void sendSound(Creature performer, String soundId) {
        if (soundId.length() > 0) {
            SoundPlayer.playSound(soundId, performer, 1.6f);
        }
    }

    static boolean transferPlayer(Creature performer, Creature target, Action act, float counter) {
        boolean done = false;
        short action = act.getNumber();
        if (performer.getPower() < 2) {
            return true;
        }
        ServerEntry targetserver = Servers.localServer;
        if (action == 241) {
            if (Servers.localServer.serverEast == null) {
                performer.getCommunicator().sendNormalServerMessage("No server east of here.");
                done = true;
            } else {
                targetserver = Servers.localServer.serverEast;
            }
        } else if (action == 240) {
            if (Servers.localServer.serverNorth == null) {
                performer.getCommunicator().sendNormalServerMessage("No server north of here. Using entryserver if one is available.");
                targetserver = Servers.loginServer.entryServer ? Servers.loginServer : Servers.getEntryServer();
                if (targetserver == null) {
                    performer.getCommunicator().sendNormalServerMessage("No entryserver was found. Nothing happens.");
                    done = true;
                } else if (targetserver.id == Servers.localServer.id) {
                    performer.getCommunicator().sendNormalServerMessage("This option leads back here. Nothing happens.");
                    done = true;
                }
            } else {
                targetserver = Servers.localServer.serverNorth;
            }
        } else if (action == 242) {
            if (Servers.localServer.serverSouth == null) {
                performer.getCommunicator().sendNormalServerMessage("No server south of here.");
                done = true;
            } else {
                targetserver = Servers.localServer.serverSouth;
            }
        } else if (action == 243) {
            if (Servers.localServer.serverWest == null) {
                performer.getCommunicator().sendNormalServerMessage("No server west of here.");
                done = true;
            } else {
                targetserver = Servers.localServer.serverWest;
            }
        }
        if (!done) {
            if (counter == 1.0f) {
                if (!targetserver.isAvailable(5, true)) {
                    target.getCommunicator().sendNormalServerMessage(targetserver.name + " is no longer available.");
                    return true;
                }
                target.getCommunicator().sendNormalServerMessage("You transfer to " + targetserver.name + ".");
                Server.getInstance().broadCastAction(target.getName() + " transfers to " + targetserver.name + ".", target, 5);
                int tilex = targetserver.SPAWNPOINTJENNX;
                int tiley = targetserver.SPAWNPOINTJENNY;
                if (target.getKingdomId() == 1) {
                    tilex = targetserver.SPAWNPOINTJENNX;
                    tiley = targetserver.SPAWNPOINTJENNY;
                } else if (target.getKingdomId() == 3) {
                    tilex = targetserver.SPAWNPOINTLIBX;
                    tiley = targetserver.SPAWNPOINTLIBY;
                } else if (target.getKingdomId() == 2) {
                    tilex = targetserver.SPAWNPOINTMOLX;
                    tiley = targetserver.SPAWNPOINTMOLY;
                }
                ((Player)target).sendTransfer(Server.getInstance(), targetserver.INTRASERVERADDRESS, Integer.parseInt(targetserver.INTRASERVERPORT), targetserver.INTRASERVERPASSWORD, targetserver.id, tilex, tiley, true, false, target.getKingdomId());
                ((Player)target).transferCounter = 30;
                if (!target.equals(performer)) {
                    performer.getLogger().log(Level.INFO, performer.getName() + " transferred " + target.getName() + " to " + targetserver.name + ".");
                }
                done = true;
            } else if (!target.hasLink()) {
                done = true;
            }
        }
        return done;
    }

    public static final void resetAspirants() {
        kingAspirants.clear();
    }

    public static final boolean hasAspiredKing(Creature performer) {
        return kingAspirants.contains(performer.getWurmId());
    }

    public static final void setAspiredKing(Creature performer) {
        kingAspirants.add(performer.getWurmId());
    }

    public static final boolean aspireKing(Creature performer, byte kingdom, @Nullable Item item, Creature elector, Action act, float counter) {
        boolean done = false;
        King current = King.getKing(kingdom);
        if (performer.isChampion()) {
            performer.getCommunicator().sendAlertServerMessage("Champions are not able to rule kingdoms.");
            done = true;
        }
        if (performer.getKingdomId() != kingdom) {
            performer.getCommunicator().sendNormalServerMessage("You may not aspire to become the " + King.getRulerTitle(performer.getSex() == 0, kingdom) + " of " + Kingdoms.getNameFor(kingdom) + ".");
            done = true;
        } else if (current != null) {
            performer.getCommunicator().sendNormalServerMessage("There is already a " + current.getRulerTitle() + " of " + Kingdoms.getNameFor(kingdom) + "!");
            done = true;
        }
        if ((Methods.hasAspiredKing(performer) || performer.getPower() > 0) && performer.getPower() < 5) {
            performer.getCommunicator().sendNormalServerMessage("You are not eligible to take the test right now.");
            done = true;
        }
        if (counter == 1.0f) {
            if (kingdom == 1 && jennElector != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + elector.getName() + " is busy. You will have to wait for your turn.");
                done = true;
            }
            if (kingdom == 3 && hotsElector != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + elector.getName() + " is busy. You will have to wait for your turn.");
                done = true;
            }
            if (kingdom == 2 && molrStone != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + item.getName() + " is occupied. You will have to wait for your turn.");
                done = true;
            }
        }
        if (!done) {
            int skill1 = 102;
            int skill2 = 100;
            if (kingdom == 1) {
                skill1 = 105;
            }
            if (kingdom == 3) {
                skill2 = 105;
            }
            if (counter == 1.0f) {
                if (kingdom == 1) {
                    jennElector = elector;
                }
                if (kingdom == 3) {
                    hotsElector = elector;
                }
                if (kingdom == 2) {
                    molrStone = item;
                }
                String tname = "";
                if (elector != null) {
                    tname = elector.getName();
                } else if (item != null) {
                    tname = item.getName();
                }
                performer.getCommunicator().sendNormalServerMessage("You hesitantly approach the " + tname + ".");
                Server.getInstance().broadCastAction(performer.getName() + " hesitantly approaches the " + tname + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[353].getVerbString(), true, 300);
            } else {
                if (act.currentSecond() % 10 == 0 && elector != null) {
                    elector.playAnimation("regalia", false);
                }
                if (act.currentSecond() == 5) {
                    if (item != null) {
                        performer.getCommunicator().sendNormalServerMessage("You struggle with the " + item.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getName() + " struggles with the " + item.getName() + ".", performer, 5);
                    }
                    if (elector != null) {
                        if (kingdom == 1) {
                            elector.almostSurface();
                        }
                        performer.getCommunicator().sendNormalServerMessage(elector.getName() + " watches you intensely.");
                        Server.getInstance().broadCastAction(elector.getName() + " watches " + performer.getName() + " intensely.", performer, 5);
                        elector.turnTowardsCreature(performer);
                    }
                } else if (act.currentSecond() == 10) {
                    done = true;
                    try {
                        Skill sk = performer.getSkills().getSkill(skill1);
                        if (sk.getKnowledge(0.0) > 21.0) {
                            done = false;
                            if (item != null) {
                                performer.getCommunicator().sendNormalServerMessage("You feel the sword budge!");
                                Server.getInstance().broadCastAction(performer.getName() + " seems to make progress with the sword!", performer, 5);
                            }
                            if (elector != null) {
                                if (kingdom == 1) {
                                    performer.getCommunicator().sendNormalServerMessage(elector.getName() + " nods solemnly!");
                                    Server.getInstance().broadCastAction(elector.getName() + " nods faintly in approval!", performer, 5);
                                } else if (kingdom == 3) {
                                    performer.getCommunicator().sendNormalServerMessage(elector.getName() + " hisses and sways " + elector.getHisHerItsString() + " head back and forth in excitement!");
                                    Server.getInstance().broadCastAction(elector.getName() + " hisses and sways " + elector.getHisHerItsString() + " head back and forth in excitement!", performer, 5);
                                }
                            }
                        }
                    }
                    catch (NoSuchSkillException sk) {
                        // empty catch block
                    }
                    if (done) {
                        Methods.setAspiredKing(performer);
                        if (item != null) {
                            performer.getCommunicator().sendNormalServerMessage("The sword is stuck and won't budge.");
                            Server.getInstance().broadCastAction(performer.getName() + " shrugs in disappointment as the sword does not budge.", performer, 5);
                        }
                        if (elector != null) {
                            if (kingdom == 1) {
                                elector.submerge();
                                performer.getCommunicator().sendNormalServerMessage(elector.getName() + " silently disappears into the depths.");
                                Server.getInstance().broadCastAction(elector.getName() + " disappears into the depths in silence.", performer, 5);
                            } else if (kingdom == 3) {
                                performer.getCommunicator().sendNormalServerMessage(elector.getName() + " ushers you away with some really threatening moves.");
                                Server.getInstance().broadCastAction(elector.getName() + " thwarts " + performer.getName() + " with some threatening moves.", performer, 5);
                            }
                        }
                    }
                } else if (act.currentSecond() == 15) {
                    if (item != null) {
                        performer.getCommunicator().sendNormalServerMessage("You continue struggle with the sword.");
                        Server.getInstance().broadCastAction(performer.getName() + " continues to struggle with the sword.", performer, 5);
                    }
                    if (elector != null) {
                        performer.getCommunicator().sendNormalServerMessage(elector.getName() + " watches you intensely again.");
                        Server.getInstance().broadCastAction(elector.getName() + " watches " + performer.getName() + " intensely again.", performer, 5);
                        elector.turnTowardsCreature(performer);
                    }
                } else if (act.currentSecond() == 20) {
                    done = true;
                    try {
                        Skill sk = performer.getSkills().getSkill(skill2);
                        if (sk.getKnowledge(0.0) > 21.0) {
                            done = false;
                            if (item != null) {
                                performer.getCommunicator().sendNormalServerMessage("You feel the sword budge even more!");
                                Server.getInstance().broadCastAction(performer.getName() + " seems to make even more progress with the sword!", performer, 5);
                            }
                            if (elector != null) {
                                if (kingdom == 1) {
                                    performer.getCommunicator().sendNormalServerMessage(elector.getName() + " nods solemnly!");
                                    Server.getInstance().broadCastAction(elector.getName() + " nods faintly in approval!", performer, 5);
                                } else if (kingdom == 3) {
                                    performer.getCommunicator().sendNormalServerMessage(elector.getName() + " hisses and sways " + elector.getHisHerItsString() + " head back and forth in excitement!");
                                    Server.getInstance().broadCastAction(elector.getName() + " hisses and sways " + elector.getHisHerItsString() + " head back and forth in excitement!", performer, 5);
                                }
                            }
                        }
                    }
                    catch (NoSuchSkillException sk) {
                        // empty catch block
                    }
                    if (done) {
                        Methods.setAspiredKing(performer);
                        if (item != null) {
                            performer.getCommunicator().sendNormalServerMessage("The sword is stuck and won't budge.");
                            Server.getInstance().broadCastAction(performer.getName() + " shrugs in disappointment as the sword does not budge.", performer, 5);
                        }
                        if (elector != null) {
                            if (kingdom == 1) {
                                elector.submerge();
                                performer.getCommunicator().sendNormalServerMessage(elector.getName() + " silently disappears into the depths.");
                                Server.getInstance().broadCastAction(elector.getName() + " disappears into the depths in silence.", performer, 5);
                            } else if (kingdom == 3) {
                                performer.getCommunicator().sendNormalServerMessage(elector.getName() + " ushers you away with some really threatening moves.");
                                Server.getInstance().broadCastAction(elector.getName() + " thwarts " + performer.getName() + " with some threatening moves.", performer, 5);
                            }
                        }
                    }
                } else if (act.currentSecond() == 25) {
                    if (item != null) {
                        performer.getCommunicator().sendNormalServerMessage("You make one final push with the sword.");
                        Server.getInstance().broadCastAction(performer.getName() + " exerts all " + performer.getHisHerItsString() + " force on the sword.", performer, 5);
                    }
                    if (elector != null) {
                        performer.getCommunicator().sendNormalServerMessage(elector.getName() + " seems to make up " + elector.getHisHerItsString() + " mind about you.");
                        Server.getInstance().broadCastAction(elector.getName() + " watches " + performer.getName() + " intensely again.", performer, 5);
                        elector.turnTowardsCreature(performer);
                    }
                } else if (act.currentSecond() >= 30) {
                    done = true;
                    Methods.setAspiredKing(performer);
                    int randomint = 1000;
                    if (performer.getKingdomId() == 1) {
                        randomint = (int)((float)Kingdoms.activePremiumJenn * 1.5f);
                    } else if (performer.getKingdomId() == 3) {
                        randomint = (int)((float)Kingdoms.activePremiumHots * 1.5f);
                    } else if (performer.getKingdomId() == 2) {
                        randomint = (int)((float)Kingdoms.activePremiumMolr * 1.5f);
                    }
                    randomint = Math.max(10, randomint);
                    if (Server.rand.nextInt(randomint) == 0 || performer.getPower() >= 3) {
                        Methods.sendSound(performer, "sound.fx.ooh.male");
                        Methods.sendSound(performer, "sound.fx.ooh.female");
                        if (item != null) {
                            performer.getCommunicator().sendNormalServerMessage("The sword gets loose and disappears! You are the new " + King.getRulerTitle(performer.getSex() == 0, kingdom) + " of " + Kingdoms.getNameFor(kingdom) + "!");
                            Server.getInstance().broadCastAction("The sword gets loose! " + performer.getName() + " is the new " + King.getRulerTitle(performer.getSex() == 0, kingdom) + " of " + Kingdoms.getNameFor(kingdom) + "!", performer, 10);
                        }
                        if (elector != null) {
                            elector.turnTowardsCreature(performer);
                            if (kingdom == 1) {
                                elector.submerge();
                                elector.playAnimation("regalia", false);
                                performer.getCommunicator().sendNormalServerMessage(elector.getName() + " hands you the royal regalia! You are the new " + King.getRulerTitle(performer.getSex() == 0, kingdom) + " of " + Kingdoms.getNameFor(kingdom) + "!");
                                Server.getInstance().broadCastAction(elector.getName() + " hands the royal regalia to " + performer.getName() + "! " + performer.getHeSheItString() + " is the new " + King.getRulerTitle(performer.getSex() == 0, kingdom) + " of " + Kingdoms.getNameFor(kingdom) + "!", performer, 10);
                            } else if (kingdom == 3) {
                                performer.getCommunicator().sendNormalServerMessage(elector.getName() + " hisses loudly and the royal regalia is handed to you from above! You are the new " + King.getRulerTitle(performer.getSex() == 0, kingdom) + " of " + Kingdoms.getNameFor(kingdom) + "!");
                                Server.getInstance().broadCastAction(elector.getName() + " hisses loudly in excitement! " + performer.getName() + " is the new " + King.getRulerTitle(performer.getSex() == 0, kingdom) + " of " + Kingdoms.getNameFor(kingdom) + "!", performer, 10);
                            }
                        }
                        King k = King.createKing(kingdom, performer.getName(), performer.getWurmId(), performer.getSex());
                        if (performer.getCitizenVillage() != null) {
                            k.setCapital(performer.getCitizenVillage().getName(), false);
                        }
                        Methods.rewardRegalia(performer);
                        NewKingQuestion nk = new NewKingQuestion(performer, "New ruler!", "Congratulations!", performer.getWurmId());
                        nk.sendQuestion();
                    } else {
                        if (item != null) {
                            performer.getCommunicator().sendNormalServerMessage("The " + item.getName() + " is stuck and won't budge.");
                            Server.getInstance().broadCastAction(performer.getName() + " shrugs in disappointment as the " + item.getName() + " does not budge.", performer, 5);
                        }
                        if (elector != null) {
                            if (kingdom == 1) {
                                elector.submerge();
                                performer.getCommunicator().sendNormalServerMessage("For no obvious reason you are rejected. " + elector.getName() + " silently disappears into the depths.");
                                Server.getInstance().broadCastAction(elector.getName() + " disappears into the depths in silence.", performer, 5);
                            } else if (kingdom == 3) {
                                performer.getCommunicator().sendNormalServerMessage("For no obvious reason you are rejected. " + elector.getName() + " ushers you away with some really threatening moves.");
                                Server.getInstance().broadCastAction(elector.getName() + " thwarts " + performer.getName() + " with some threatening moves.", performer, 5);
                            }
                        }
                    }
                }
            }
        }
        return done;
    }

    public static void rewardRegalia(Creature creature) {
        Item inventory = creature.getInventory();
        if (inventory != null) {
            try {
                byte template = Kingdoms.getKingdom(creature.getKingdomId()).getTemplate();
                byte kingdom = creature.getKingdomId();
                if (template == 1) {
                    Item sceptre = ItemFactory.createItem(529, Server.rand.nextFloat() * 30.0f + 70.0f, creature.getName());
                    sceptre.setAuxData(kingdom);
                    inventory.insertItem(sceptre, true);
                    Item crown = ItemFactory.createItem(530, Server.rand.nextFloat() * 30.0f + 70.0f, creature.getName());
                    crown.setAuxData(kingdom);
                    inventory.insertItem(crown, true);
                    Item robes = ItemFactory.createItem(531, Server.rand.nextFloat() * 30.0f + 70.0f, creature.getName());
                    robes.setAuxData(kingdom);
                    inventory.insertItem(robes, true);
                } else if (template == 3) {
                    Item sceptre = ItemFactory.createItem(535, Server.rand.nextFloat() * 30.0f + 70.0f, creature.getName());
                    sceptre.setAuxData(kingdom);
                    inventory.insertItem(sceptre, true);
                    Item crown = ItemFactory.createItem(536, Server.rand.nextFloat() * 30.0f + 70.0f, creature.getName());
                    crown.setAuxData(kingdom);
                    inventory.insertItem(crown, true);
                    Item robes = ItemFactory.createItem(537, Server.rand.nextFloat() * 30.0f + 70.0f, creature.getName());
                    robes.setAuxData(kingdom);
                    inventory.insertItem(robes, true);
                } else if (template == 2) {
                    Item sceptre = ItemFactory.createItem(532, Server.rand.nextFloat() * 30.0f + 70.0f, creature.getName());
                    sceptre.setAuxData(kingdom);
                    inventory.insertItem(sceptre, true);
                    Item crown = ItemFactory.createItem(533, Server.rand.nextFloat() * 30.0f + 70.0f, creature.getName());
                    crown.setAuxData(kingdom);
                    inventory.insertItem(crown, true);
                    Item robes = ItemFactory.createItem(534, Server.rand.nextFloat() * 30.0f + 70.0f, creature.getName());
                    robes.setAuxData(kingdom);
                    inventory.insertItem(robes, true);
                }
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, creature.getName() + " " + ex.getMessage(), ex);
            }
        }
    }

    public static Creature getJennElector() {
        return jennElector;
    }

    public static void resetJennElector() {
        jennElector = null;
    }

    public static Creature getHotsElector() {
        return hotsElector;
    }

    public static void resetHotsElector() {
        hotsElector = null;
    }

    public static Item getMolrStone() {
        return molrStone;
    }

    public static void resetMolrStone() {
        molrStone = null;
    }

    public static final void sendVoiceChatQuestion(Creature player) {
        if (player != null) {
            if (Constants.isEigcEnabled) {
                VoiceChatQuestion vcq = new VoiceChatQuestion(player);
                vcq.sendQuestion();
            } else {
                player.getCommunicator().sendNormalServerMessage("Voice chat is not enabled on this server.");
            }
        }
    }

    static void sendGmSetEnchantQuestion(Creature performer, Item target) {
        GmSetEnchants gmse = new GmSetEnchants(performer, target);
        gmse.sendQuestion();
    }

    static void sendGmSetTraitsQuestion(Creature performer, Creature target) {
        GmSetTraits gmst = new GmSetTraits(performer, target);
        gmst.sendQuestion();
    }

    static void sendGmSetMedpathQuestion(Creature performer, Creature target) {
        GmSetMedPath gmsm = new GmSetMedPath(performer, target);
        gmsm.sendQuestion();
    }

    static void sendGmBuildAllWallsQuestion(Creature performer, Structure target) {
        GMBuildAllWallsQuestion gmbawq = new GMBuildAllWallsQuestion(performer, target);
        gmbawq.sendQuestion();
    }

    static void sendPlanBridgeQuestion(Creature performer, int targetFloorLevel, Point start, Point end, byte dir, int width, int length) {
        PlanBridgeQuestion pbq = new PlanBridgeQuestion(performer, targetFloorLevel, start, end, dir, width, length);
        pbq.sendQuestion();
    }

    public static Item[] getBestReports(Creature creature, @Nullable Item container) {
        HashMap<Byte, Item> reports = new HashMap<Byte, Item>();
        if (container == null) {
            for (Item item : creature.getInventory().getAllItems(true)) {
                if (item.getTemplateId() != 1127) continue;
                Methods.addAlmanacReports(reports, item);
            }
        } else if (container.getTemplateId() == 1127) {
            Methods.addAlmanacReports(reports, container);
        } else if (container.getTemplateId() == 1128) {
            Methods.addAlmanacFolderReports(reports, container);
        }
        Object[] reportArr = reports.values().toArray(new Item[reports.size()]);
        Arrays.sort(reportArr);
        return reportArr;
    }

    private static void addAlmanacReports(Map<Byte, Item> reports, Item almanac) {
        for (Item item : almanac.getItems()) {
            if (item.getTemplateId() == 1128) {
                Methods.addAlmanacFolderReports(reports, item);
                continue;
            }
            Methods.addReport(reports, item);
        }
    }

    private static void addAlmanacFolderReports(Map<Byte, Item> reports, Item almanacFolder) {
        for (Item report : almanacFolder.getItems()) {
            Methods.addReport(reports, report);
        }
    }

    private static void addReport(Map<Byte, Item> reports, Item report) {
        if (report.isHarvestReport()) {
            Item oldReport = reports.get(report.getAuxData());
            if (oldReport == null) {
                reports.put(report.getAuxData(), report);
            } else if (oldReport.getCurrentQualityLevel() < report.getCurrentQualityLevel()) {
                reports.put(report.getAuxData(), report);
            }
        }
    }

    public static void addActionIfAbsent(List<ActionEntry> actionEntries, ActionEntry newActionEntry) {
        if (!actionEntries.contains(newActionEntry)) {
            actionEntries.add(newActionEntry);
        }
    }

    public static boolean isActionAllowed(Creature performer, short action) {
        return Methods.isActionAllowed(performer, action, performer.getTileX(), performer.getTileY());
    }

    public static boolean isActionAllowed(Creature performer, short action, Item item) {
        return Methods.isActionAllowed(performer, action, false, item.getTileX(), item.getTileY(), item, 0, 0);
    }

    public static boolean isActionAllowed(Creature performer, short action, int x, int y) {
        return Methods.isActionAllowed(performer, action, false, x, y, null, 0, 0);
    }

    public static boolean isActionAllowed(Creature performer, short action, boolean setHunted, int tileX, int tileY, int encodedTile, int dir) {
        return Methods.isActionAllowed(performer, action, false, tileX, tileY, null, encodedTile, dir);
    }

    public static boolean isActionAllowed(Creature performer, short action, boolean setHunted, int tileX, int tileY, @Nullable Item item, int encodedTile, int dir) {
        VolaTile vt = Zones.getOrCreateTile(tileX, tileY, performer.isOnSurface());
        Village village = vt != null ? vt.getVillage() : null;
        Structure structure = vt != null ? vt.getStructure() : null;
        boolean canDo = true;
        if (Actions.isActionDestroy(action) && village != null && village.isActionAllowed(action, performer)) {
            canDo = true;
        } else if (structure != null && structure.isTypeHouse() && !structure.isFinished() && (Actions.isActionBuild(action) || Actions.isActionDestroy(action)) && structure.isActionAllowed(performer, action)) {
            canDo = true;
        } else if (structure != null && structure.isTypeHouse() && structure.isFinished()) {
            canDo = !Actions.isActionBuildingPermission(action) && village != null && village.isActionAllowed(action, performer) ? true : (!structure.isActionAllowed(performer, action) ? Methods.isNotAllowedMessage(performer, village, structure, action, false) : true);
        } else if (village != null) {
            canDo = !village.isActionAllowed(action, performer, false, encodedTile, dir) ? Methods.isNotAllowedMessage(performer, village, structure, action, false) : true;
        }
        if (village == null && Actions.actionEntrys[action].isPerimeterAction()) {
            Village villagePerim = Villages.getVillageWithPerimeterAt(tileX, tileY, true);
            if (villagePerim != null && !villagePerim.isCitizen(performer) && !villagePerim.isAlly(performer)) {
                boolean skipOthers = false;
                try {
                    Item token = villagePerim.getToken();
                    if (token != null && token.getWurmId() == 7689502046815490L) {
                        canDo = true;
                        skipOthers = true;
                    }
                }
                catch (NoSuchItemException noSuchItemException) {
                    // empty catch block
                }
                if (!skipOthers) {
                    canDo = !villagePerim.isActionAllowed(action, performer, false, 0, 0) ? Methods.isNotAllowedMessage(performer, villagePerim, structure, action, true) : true;
                }
            }
        } else if (village == null && item != null && item.isRoadMarker()) {
            Village vill;
            Village twoCheck = null;
            twoCheck = Zones.getVillage(item.getTilePos(), item.isOnSurface());
            if (twoCheck == null && (vill = Villages.getVillageWithPerimeterAt(item.getTileX(), item.getTileY(), item.isOnSurface())) != null && vill.coversPlus(item.getTileX(), item.getTileY(), 2)) {
                twoCheck = vill;
            }
            if (twoCheck != null) {
                canDo = !twoCheck.isActionAllowed(action, performer, false, 0, 0) ? Methods.isNotAllowedMessage(performer, twoCheck, structure, action, true) : true;
            }
        }
        return canDo;
    }

    private static boolean isNotAllowedMessage(Creature performer, Village village, Structure structure, short action, boolean inPerimeter) {
        if (!performer.isOnPvPServer() || Servers.isThisAChaosServer()) {
            String msg = inPerimeter ? village.getName() + " does not allow that." : (village != null && village.getGuards().length > 0 ? "The guards kindly inform you that you are not allowed to do that here." : (village != null ? "That would be very bad for your karma and is disallowed on this server." : "You do not have permission to do that here."));
            performer.getCommunicator().sendNormalServerMessage(msg);
            return false;
        }
        if (village != null) {
            if (!village.isEnemy(performer)) {
                if (performer.isLegal()) {
                    performer.getCommunicator().sendNormalServerMessage("That would be illegal here. You can check the settlement token for the local laws.");
                    return false;
                }
                if (Actions.actionEntrys[action].isEnemyAllowedWhenNoGuards() && village.getGuards().length > 0) {
                    performer.getCommunicator().sendNormalServerMessage("A guard has noted you and stops you with a warning.");
                    return false;
                }
                if (Actions.actionEntrys[action].isEnemyNeverAllowed()) {
                    performer.getCommunicator().sendNormalServerMessage("That action makes no sense here.");
                    return false;
                }
            } else {
                if (Actions.actionEntrys[action].isEnemyAllowedWhenNoGuards() && village.getGuards().length > 0) {
                    performer.getCommunicator().sendNormalServerMessage("A guard has noted you and stops you with a warning.");
                    return false;
                }
                if (Actions.actionEntrys[action].isEnemyNeverAllowed()) {
                    performer.getCommunicator().sendNormalServerMessage("That action makes no sense here.");
                    return false;
                }
            }
            return true;
        }
        if (structure != null && structure.isFinished()) {
            if (!structure.isEnemy(performer)) {
                if (performer.isLegal()) {
                    performer.getCommunicator().sendNormalServerMessage("That would be illegal. ");
                    return false;
                }
            } else if (Actions.actionEntrys[action].isEnemyNeverAllowed()) {
                performer.getCommunicator().sendNormalServerMessage("That action makes no sense here.");
                return false;
            }
            return true;
        }
        return true;
    }
}

