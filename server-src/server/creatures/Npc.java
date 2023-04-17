/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.BehaviourDispatcher;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.LongTarget;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.ai.ChatManager;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

public class Npc
extends Creature {
    static final Random faceRandom = new Random();
    int lastX = 0;
    int lastY = 0;
    final ChatManager chatManager = new ChatManager(this);
    LongTarget longTarget;
    int longTargetAttempts = 0;
    int passiveCounter = 0;
    int MAXSEED = 100;

    public Npc() throws Exception {
    }

    public Npc(CreatureTemplate aTemplate) throws Exception {
        super(aTemplate);
    }

    public Npc(long aId) throws Exception {
        super(aId);
    }

    public final ChatManager getChatManager() {
        return this.chatManager;
    }

    @Override
    public final byte getKingdomId() {
        if (this.isAggHuman()) {
            return 0;
        }
        return this.status.kingdom;
    }

    @Override
    public final boolean isAggHuman() {
        return this.status.modtype == 2;
    }

    @Override
    public final void pollNPCChat() {
        this.getChatManager().checkChats();
    }

    @Override
    public final void pollNPC() {
        this.checkItemSpawn();
        if (this.passiveCounter-- == 0) {
            this.doSomething();
        }
    }

    private final void doSomething() {
        if (!this.isFighting() && this.target == -10L) {
            if (!this.capturePillar()) {
                if (!this.performLongTargetAction()) {
                    if (this.getStatus().getPath() == null && Server.rand.nextBoolean()) {
                        this.startPathing(0);
                        this.setPassiveCounter(120);
                    } else {
                        ActionEntry ae;
                        BehaviourDispatcher.RequestParam param;
                        List<ActionEntry> actions;
                        ActionEntry ae2;
                        List<ActionEntry> actions2;
                        BehaviourDispatcher.RequestParam param2;
                        Behaviour behaviour;
                        Item targ2;
                        Item[] containeds;
                        Object targ;
                        Item rand;
                        Item[] groundItems;
                        Item[] allItems;
                        CounterTypes rand2;
                        int seed = Server.rand.nextInt(this.MAXSEED);
                        if (seed < 10) {
                            Wound[] wounds;
                            if (this.getStatus().damage > 0 && (wounds = this.getBody().getWounds().getWounds()).length > 0) {
                                rand2 = wounds[Server.rand.nextInt(wounds.length)];
                                if (Server.rand.nextBoolean()) {
                                    ((Wound)rand2).setBandaged(true);
                                    if (Server.rand.nextBoolean()) {
                                        ((Wound)rand2).setHealeff((byte)(Server.rand.nextInt(70) + 30));
                                    }
                                } else {
                                    ((Wound)rand2).heal();
                                }
                            }
                            this.setPassiveCounter(30);
                        }
                        if (seed < 20) {
                            allItems = this.getInventory().getAllItems(false);
                            if (allItems.length > 0) {
                                rand2 = allItems[Server.rand.nextInt(allItems.length)];
                                try {
                                    if (((Item)rand2).isFood()) {
                                        BehaviourDispatcher.action((Creature)this, this.communicator, -10L, ((Item)rand2).getWurmId(), (short)182);
                                    } else if (((Item)rand2).isLiquid()) {
                                        BehaviourDispatcher.action((Creature)this, this.communicator, -10L, ((Item)rand2).getWurmId(), (short)183);
                                    } else {
                                        BehaviourDispatcher.action((Creature)this, this.communicator, -10L, ((Item)rand2).getWurmId(), (short)118);
                                    }
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                            this.setPassiveCounter(30);
                        }
                        if (seed < 30) {
                            if (this.getCurrentTile() != null) {
                                try {
                                    allItems = this.getInventory().getAllItems(false);
                                    groundItems = this.getCurrentTile().getItems();
                                    rand = null;
                                    if (allItems.length > 0) {
                                        rand = allItems[Server.rand.nextInt(allItems.length)];
                                    }
                                    if (groundItems.length > 0) {
                                        targ = groundItems[Server.rand.nextInt(groundItems.length)];
                                        if (Server.rand.nextBoolean() && this.getCurrentTile().getVillage() == null) {
                                            if (Server.rand.nextInt(4) == 0 && targ != null && ((Item)targ).isHollow() && ((Item)targ).testInsertItem(rand)) {
                                                ((Item)targ).insertItem(rand);
                                            } else if (this.canCarry(((Item)targ).getWeightGrams())) {
                                                BehaviourDispatcher.action((Creature)this, this.communicator, -10L, ((Item)targ).getWurmId(), (short)6);
                                            } else if (((Item)targ).isHollow() && (containeds = ((Item)targ).getAllItems(false)).length > 0 && !(targ2 = containeds[Server.rand.nextInt(containeds.length)]).isBodyPart() && !targ2.isNoTake()) {
                                                BehaviourDispatcher.action((Creature)this, this.communicator, -10L, targ2.getWurmId(), (short)6);
                                                this.wearItems();
                                            }
                                        } else {
                                            BehaviourDispatcher.action((Creature)this, this.communicator, -10L, ((Item)targ).getWurmId(), (short)162);
                                        }
                                    } else {
                                        BehaviourDispatcher.action((Creature)this, this.communicator, -10L, rand.getWurmId(), (short)162);
                                    }
                                    this.setPassiveCounter(30);
                                }
                                catch (Exception allItems2) {
                                    // empty catch block
                                }
                            }
                            this.setPassiveCounter(10);
                        }
                        if (seed < 40) {
                            if (this.getCurrentTile() != null) {
                                try {
                                    allItems = this.getInventory().getAllItems(false);
                                    groundItems = this.getCurrentTile().getItems();
                                    rand = null;
                                    if (allItems.length > 0) {
                                        rand = allItems[Server.rand.nextInt(allItems.length)];
                                    }
                                    if (groundItems.length > 0) {
                                        targ = groundItems[Server.rand.nextInt(groundItems.length)];
                                        if (Server.rand.nextBoolean() && this.getCurrentTile().getVillage() == null) {
                                            if (Server.rand.nextInt(4) == 0 && targ != null && ((Item)targ).isHollow() && ((Item)targ).testInsertItem(rand)) {
                                                ((Item)targ).insertItem(rand);
                                            } else if (this.canCarry(((Item)targ).getWeightGrams())) {
                                                BehaviourDispatcher.action((Creature)this, this.communicator, -10L, ((Item)targ).getWurmId(), (short)6);
                                            } else if (((Item)targ).isHollow() && (containeds = ((Item)targ).getAllItems(false)).length > 0 && !(targ2 = containeds[Server.rand.nextInt(containeds.length)]).isBodyPart() && !targ2.isNoTake()) {
                                                BehaviourDispatcher.action((Creature)this, this.communicator, -10L, targ2.getWurmId(), (short)6);
                                                this.wearItems();
                                            }
                                        } else {
                                            if (((Item)targ).isHollow() && (containeds = ((Item)targ).getAllItems(false)).length > 0 && Server.rand.nextBoolean()) {
                                                targ = containeds[Server.rand.nextInt(containeds.length)];
                                            }
                                            behaviour = Action.getBehaviour(((Item)targ).getWurmId(), this.isOnSurface());
                                            param2 = BehaviourDispatcher.requestActionForItemsBodyIdsCoinIds(this, ((Item)targ).getWurmId(), rand, behaviour);
                                            actions2 = param2.getAvailableActions();
                                            if (actions2.size() > 0 && (ae2 = actions2.get(Server.rand.nextInt(actions2.size()))).getNumber() > 0) {
                                                BehaviourDispatcher.action((Creature)this, this.communicator, rand.getWurmId(), ((Item)targ).getWurmId(), ae2.getNumber());
                                            }
                                        }
                                    } else if (rand != null) {
                                        BehaviourDispatcher.action((Creature)this, this.communicator, -10L, rand.getWurmId(), (short)162);
                                    }
                                    this.setPassiveCounter(30);
                                }
                                catch (Exception allItems3) {
                                    // empty catch block
                                }
                            }
                            this.setPassiveCounter(10);
                        }
                        if (seed < 50) {
                            if (this.getCurrentTile() != null) {
                                try {
                                    long targTile;
                                    allItems = this.getInventory().getAllItems(false);
                                    rand2 = null;
                                    if (allItems.length > 0) {
                                        boolean abilused = false;
                                        for (Item abil : allItems) {
                                            if (!abil.isAbility() || !Server.rand.nextBoolean()) continue;
                                            BehaviourDispatcher.action((Creature)this, this.communicator, -10L, abil.getWurmId(), (short)118);
                                            abilused = true;
                                            break;
                                        }
                                        if (!abilused) {
                                            Behaviour behaviour2;
                                            long targTile2;
                                            rand2 = allItems[Server.rand.nextInt(allItems.length)];
                                            if (!(Server.rand.nextInt(5) != 0 || ((Item)rand2).isEpicTargetItem() || !((Item)rand2).isUnique() || ((Item)rand2).isAbility() || ((Item)rand2).isMagicStaff() || ((Item)rand2).isRoyal())) {
                                                ((Item)rand2).putItemInfrontof(this);
                                            } else if (this.isOnSurface() && (actions = (param = BehaviourDispatcher.requestActionForTiles(this, targTile2 = Tiles.getTileId(this.getTileX(), this.getTileY(), 0), true, (Item)rand2, behaviour2 = Action.getBehaviour(targTile2, this.isOnSurface()))).getAvailableActions()).size() > 0 && (ae = actions.get(Server.rand.nextInt(actions.size()))).getNumber() > 0) {
                                                BehaviourDispatcher.action((Creature)this, this.communicator, rand2 == null ? -10L : ((Item)rand2).getWurmId(), targTile2, ae.getNumber());
                                            }
                                        }
                                    } else if (this.isOnSurface() && (actions2 = (param2 = BehaviourDispatcher.requestActionForTiles(this, targTile = Tiles.getTileId(this.getTileX(), this.getTileY(), 0), true, null, behaviour = Action.getBehaviour(targTile, this.isOnSurface()))).getAvailableActions()).size() > 0 && (ae2 = actions2.get(Server.rand.nextInt(actions2.size()))).getNumber() > 0) {
                                        BehaviourDispatcher.action((Creature)this, this.communicator, -10L, targTile, ae2.getNumber());
                                    }
                                    this.setPassiveCounter(30);
                                }
                                catch (Exception allItems4) {
                                    // empty catch block
                                }
                            }
                            this.setPassiveCounter(10);
                        }
                        if (seed < 70) {
                            if (this.getCurrentTile() != null) {
                                try {
                                    ActionEntry ae3;
                                    Behaviour behaviour3;
                                    long targTile;
                                    BehaviourDispatcher.RequestParam param3;
                                    List<ActionEntry> actions3;
                                    allItems = this.getInventory().getAllItems(false);
                                    rand2 = null;
                                    if (allItems.length > 0) {
                                        rand2 = allItems[Server.rand.nextInt(allItems.length)];
                                    }
                                    boolean found = false;
                                    Creature[] crets = null;
                                    block15: for (int x = -2; x <= 2; ++x) {
                                        for (int y = -2; y <= 2; ++y) {
                                            VolaTile t = Zones.getTileOrNull(Zones.safeTileX(this.getTileX() + x), Zones.safeTileY(this.getTileY() + y), this.isOnSurface());
                                            if (t == null || (crets = t.getCreatures()).length <= 0) continue;
                                            Creature targC = crets[Server.rand.nextInt(crets.length)];
                                            Behaviour behaviour4 = Action.getBehaviour(targC.getWurmId(), this.isOnSurface());
                                            BehaviourDispatcher.RequestParam param4 = BehaviourDispatcher.requestActionForCreaturesPlayers(this, targC.getWurmId(), (Item)rand2, targC.isPlayer() ? 0 : 1, behaviour4);
                                            List<ActionEntry> actions4 = param4.getAvailableActions();
                                            if (actions4.size() <= 0) continue;
                                            ActionEntry ae4 = actions4.get(Server.rand.nextInt(actions4.size()));
                                            if (ae4.getNumber() > 0) {
                                                BehaviourDispatcher.action((Creature)this, this.communicator, rand2 == null ? -10L : ((Item)rand2).getWurmId(), targC.getWurmId(), ae4.getNumber());
                                            }
                                            this.setPassiveCounter(30);
                                            found = true;
                                            continue block15;
                                        }
                                    }
                                    if (!found && (actions3 = (param3 = BehaviourDispatcher.requestActionForTiles(this, targTile = Tiles.getTileId(this.getTileX() - 1 + Server.rand.nextInt(2), this.getTileY() - 1 + Server.rand.nextInt(2), 0, this.isOnSurface()), true, (Item)rand2, behaviour3 = Action.getBehaviour(targTile, this.isOnSurface()))).getAvailableActions()).size() > 0 && (ae3 = actions3.get(Server.rand.nextInt(actions3.size()))).getNumber() > 0) {
                                        BehaviourDispatcher.action((Creature)this, this.communicator, rand2 == null ? -10L : ((Item)rand2).getWurmId(), targTile, ae3.getNumber());
                                    }
                                    this.setPassiveCounter(30);
                                }
                                catch (Exception allItems5) {
                                    // empty catch block
                                }
                            }
                            this.setPassiveCounter(10);
                        }
                        if (seed < 80) {
                            Creature[] crets = null;
                            block17: for (int x = -2; x <= 2; ++x) {
                                for (int y = -2; y <= 2; ++y) {
                                    VolaTile t = Zones.getTileOrNull(Zones.safeTileX(this.getTileX() + x), Zones.safeTileY(this.getTileY() + y), this.isOnSurface());
                                    if (t == null || (crets = t.getCreatures()).length <= 0) continue;
                                    try {
                                        Creature targC = crets[Server.rand.nextInt(crets.length)];
                                        Behaviour behaviour5 = Action.getBehaviour(targC.getWurmId(), this.isOnSurface());
                                        param = BehaviourDispatcher.requestActionForCreaturesPlayers(this, targC.getWurmId(), null, targC.isPlayer() ? 0 : 1, behaviour5);
                                        actions = param.getAvailableActions();
                                        if (actions.size() <= 0) continue;
                                        ae = actions.get(Server.rand.nextInt(actions.size()));
                                        if (ae.isOffensive() && this.isFriendlyKingdom(targC.getKingdomId()) || ae.getNumber() <= 0) continue block17;
                                        BehaviourDispatcher.action((Creature)this, this.communicator, -10L, targC.getWurmId(), ae.getNumber());
                                        continue block17;
                                    }
                                    catch (Exception targC) {
                                        // empty catch block
                                    }
                                }
                            }
                        }
                        try {
                            allItems = this.getInventory().getAllItems(false);
                            if (allItems.length > 2) {
                                ActionEntry ae5;
                                Item rand1 = allItems[Server.rand.nextInt(allItems.length)];
                                Item rand22 = allItems[Server.rand.nextInt(allItems.length)];
                                Behaviour behaviour6 = Action.getBehaviour(rand22.getWurmId(), this.isOnSurface());
                                BehaviourDispatcher.RequestParam param5 = BehaviourDispatcher.requestActionForItemsBodyIdsCoinIds(this, rand22.getWurmId(), rand1, behaviour6);
                                List<ActionEntry> actions5 = param5.getAvailableActions();
                                if (actions5.size() > 0 && (ae5 = actions5.get(Server.rand.nextInt(actions5.size()))).getNumber() > 0) {
                                    BehaviourDispatcher.action((Creature)this, this.communicator, rand1 == null ? -10L : rand1.getWurmId(), rand22.getWurmId(), ae5.getNumber());
                                }
                                this.setPassiveCounter(30);
                            }
                        }
                        catch (Exception exception) {}
                    }
                } else {
                    this.setPassiveCounter(180);
                }
            } else {
                this.setPassiveCounter(30);
            }
        }
    }

    private void clearLongTarget() {
        this.longTarget = null;
        this.longTargetAttempts = 0;
    }

    public boolean isOnLongTargetTile() {
        if (this.getStatus() == null) {
            return false;
        }
        return this.longTarget.getTileX() == (int)this.status.getPositionX() >> 2 && this.longTarget.getTileY() == (int)this.status.getPositionY() >> 2;
    }

    @Override
    public final Path findPath(int targetX, int targetY, @Nullable PathFinder pathfinder) throws NoPathException {
        Path path = null;
        PathFinder pf = pathfinder != null ? pathfinder : new PathFinder();
        this.setPathfindcounter(this.getPathfindCounter() + 1);
        if (this.getPathfindCounter() >= 10 && this.target == -10L && this.getPower() <= 0) {
            throw new NoPathException("No pathing now");
        }
        path = pf.findPath(this, this.getTileX(), this.getTileY(), targetX, targetY, this.isOnSurface(), 20);
        if (path != null) {
            this.setPathfindcounter(0);
        }
        return path;
    }

    private final boolean capturePillar() {
        FocusZone hota;
        if (this.getCitizenVillage() != null && (hota = FocusZone.getHotaZone()) != null && hota.covers(this.getTileX(), this.getTileY())) {
            for (Item i : this.getCurrentTile().getItems()) {
                if (i.getTemplateId() != 739 || i.getData1() == this.getCitizenVillage().getId()) continue;
                try {
                    BehaviourDispatcher.action((Creature)this, this.communicator, -10L, i.getWurmId(), (short)504);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return true;
            }
        }
        return false;
    }

    private final boolean performLongTargetAction() {
        if (this.longTarget != null && this.longTarget.getMissionTrigger() > 0) {
            MissionTrigger trigger = MissionTriggers.getTriggerWithId(this.longTarget.getMissionTrigger());
            if (trigger != null && Math.abs(this.longTarget.getTileX() - this.getTileX()) < 3 && Math.abs(this.longTarget.getTileY() - this.getTileY()) < 3) {
                Item found = null;
                if (trigger.getItemUsedId() > 0) {
                    Object ce2;
                    if (trigger.getOnActionPerformed() == 148 && (ce2 = CreationMatrix.getInstance().getCreationEntry(trigger.getItemUsedId())) != null && !((CreationEntry)ce2).isAdvanced()) {
                        try {
                            found = ItemFactory.createItem(trigger.getItemUsedId(), 20.0f + Server.rand.nextFloat() * 20.0f, this.getName());
                            this.getInventory().insertItem(found, true);
                            if (found.getWeightGrams() > 20000) {
                                found.putItemInfrontof(this);
                            } else {
                                this.wearItems();
                            }
                            MissionTriggers.activateTriggers((Creature)this, found, 148, 0L, 1);
                            this.clearLongTarget();
                            return true;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    for (Item item : this.getAllItems()) {
                        if (item.getTemplateId() != trigger.getItemUsedId()) continue;
                        found = item;
                    }
                    if (found == null) {
                        try {
                            found = ItemFactory.createItem(trigger.getItemUsedId(), 20.0f + Server.rand.nextFloat() * 20.0f, this.getName());
                            this.getInventory().insertItem(found, true);
                        }
                        catch (Exception ce2) {
                            // empty catch block
                        }
                    }
                }
                if (WurmId.getType(trigger.getTarget()) == 1 || WurmId.getType(trigger.getTarget()) == 0) {
                    try {
                        Creature c = Server.getInstance().getCreature(trigger.getTarget());
                        if (c == null || c.isDead()) {
                            this.clearLongTarget();
                            return true;
                        }
                    }
                    catch (NoSuchCreatureException nsc) {
                        this.clearLongTarget();
                        return true;
                    }
                    catch (NoSuchPlayerException nsp) {
                        this.clearLongTarget();
                        return true;
                    }
                }
                if (WurmId.getType(trigger.getTarget()) == 3 && trigger.getOnActionPerformed() == 492) {
                    int tilenum = Server.surfaceMesh.getTile(this.getTileX(), this.getTileY());
                    if (!Tiles.isTree(Tiles.decodeType(tilenum))) {
                        return true;
                    }
                    if (found == null) {
                        for (Item axe : this.getBody().getBodyItem().getAllItems(false)) {
                            if (!axe.isWeaponAxe() && !axe.isWeaponSlash()) continue;
                            found = axe;
                        }
                    }
                    if (found == null) {
                        for (Item axe : this.getInventory().getAllItems(false)) {
                            if (!axe.isWeaponAxe() && !axe.isWeaponSlash()) continue;
                            found = axe;
                        }
                    }
                    if (found == null) {
                        try {
                            found = ItemFactory.createItem(7, 10.0f, this.getName());
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if (found != null && found.isWeaponAxe() || found.isWeaponSlash()) {
                        try {
                            BehaviourDispatcher.action((Creature)this, this.communicator, found.getWurmId(), trigger.getTarget(), (short)96);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                MissionTriggers.activateTriggers((Creature)this, found, trigger.getOnActionPerformed(), trigger.getTarget(), 1);
                this.clearLongTarget();
                return true;
            }
        } else if (this.longTarget != null && this.isOnLongTargetTile()) {
            Item[] currentItems;
            for (Item current : currentItems = this.getCurrentTile().getItems()) {
                if (!current.isCorpse() || current.getLastOwnerId() != this.getWurmId()) continue;
                for (Item incorpse : current.getAllItems(false)) {
                    if (incorpse.isBodyPart()) continue;
                    this.getInventory().insertItem(incorpse);
                }
                this.wearItems();
                Items.destroyItem(current.getWurmId());
            }
            this.clearLongTarget();
            return true;
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public final PathTile getMoveTarget(int seed) {
        Creature targ;
        int ty;
        int tx;
        int tilePosY;
        int tilePosX;
        block119: {
            int tpy;
            int tpx;
            block122: {
                FocusZone hota;
                block120: {
                    block121: {
                        VolaTile currTile;
                        if (this.getStatus() == null) {
                            return null;
                        }
                        float lPosX = this.status.getPositionX();
                        float lPosY = this.status.getPositionY();
                        boolean hasTarget = false;
                        tilePosX = (int)lPosX >> 2;
                        tilePosY = (int)lPosY >> 2;
                        tx = tilePosX;
                        ty = tilePosY;
                        if (!this.isAggHuman() && this.getCitizenVillage() != null) {
                            if (this.longTarget == null) {
                                FocusZone hota2;
                                int tile;
                                if (Server.rand.nextInt(100) == 0) {
                                    Player[] players = Players.getInstance().getPlayers();
                                    for (int x = 0; x < 10; ++x) {
                                        Player p = players[Server.rand.nextInt(players.length)];
                                        if (!p.isWithinDistanceTo(this, 200.0f) || p.getPower() != 0) continue;
                                        tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
                                        if (!p.isOnSurface()) {
                                            tile = Server.caveMesh.getTile(tilePosX, tilePosY);
                                        }
                                        this.longTarget = new LongTarget(p.getTileX(), p.getTileY(), tile, p.isOnSurface(), p.getFloorLevel(), this);
                                        if (p.isFriendlyKingdom(this.getKingdomId())) {
                                            this.getChatManager().createAndSendMessage(p, "Oi.", false);
                                            break;
                                        }
                                        this.getChatManager().createAndSendMessage(p, "Coming for you.", false);
                                        break;
                                    }
                                }
                                if (this.longTarget == null && Server.rand.nextInt(10) == 0) {
                                    Item[] allIts;
                                    Item[] x = allIts = Items.getAllItems();
                                    int p = x.length;
                                    for (tile = 0; tile < p; ++tile) {
                                        Item[] contained;
                                        Item corpse = x[tile];
                                        if (corpse.getZoneId() <= 0 || corpse.getTemplateId() != 272 || corpse.getLastOwnerId() != this.getWurmId() || !corpse.getName().toLowerCase().contains(this.getName().toLowerCase()) || (contained = corpse.getAllItems(false)).length <= 4) continue;
                                        boolean surf = corpse.isOnSurface();
                                        int tile2 = Server.surfaceMesh.getTile(corpse.getTileX(), corpse.getTileY());
                                        if (!surf) {
                                            tile2 = Server.caveMesh.getTile(corpse.getTileX(), corpse.getTileY());
                                        }
                                        this.longTarget = new LongTarget(corpse.getTileX(), corpse.getTileY(), tile2, surf, surf ? 0 : -1, this);
                                    }
                                }
                                if (this.longTarget == null && Server.rand.nextInt(10) == 0) {
                                    EpicMission[] ems;
                                    EpicMission[] x = ems = EpicServerStatus.getCurrentEpicMissions();
                                    int p = x.length;
                                    for (tile = 0; tile < p; ++tile) {
                                        Deity deity;
                                        EpicMission em = x[tile];
                                        if (!em.isCurrent() || (deity = Deities.getDeity(em.getEpicEntityId())) == null || deity.getFavoredKingdom() != this.getKingdomId()) continue;
                                        for (MissionTrigger trig : MissionTriggers.getAllTriggers()) {
                                            int tile3;
                                            int tile4;
                                            if (trig.getMissionRequired() != em.getMissionId()) continue;
                                            long target = trig.getTarget();
                                            if (WurmId.getType(target) == 3 || WurmId.getType(target) == 17) {
                                                short x2 = Tiles.decodeTileX(target);
                                                int y2 = Tiles.decodeTileY(target);
                                                boolean surf = WurmId.getType(target) == 3;
                                                tile4 = Server.surfaceMesh.getTile(x2, y2);
                                                if (!surf) {
                                                    tile4 = Server.caveMesh.getTile(x2, y2);
                                                }
                                                this.longTarget = new LongTarget((int)x2, y2, tile4, surf, surf ? 0 : -1, this);
                                            } else if (WurmId.getType(target) == 2) {
                                                try {
                                                    Item i = Items.getItem(target);
                                                    tile3 = Server.surfaceMesh.getTile(i.getTileX(), i.getTileY());
                                                    if (!i.isOnSurface()) {
                                                        tile3 = Server.caveMesh.getTile(i.getTileX(), i.getTileY());
                                                    }
                                                    this.longTarget = new LongTarget(i.getTileX(), i.getTileY(), tile3, i.isOnSurface(), i.getFloorLevel(), this);
                                                }
                                                catch (NoSuchItemException i) {}
                                            } else if (WurmId.getType(target) == 1 || WurmId.getType(target) == 0) {
                                                try {
                                                    Creature c = Server.getInstance().getCreature(target);
                                                    tile3 = Server.surfaceMesh.getTile(c.getTileX(), c.getTileY());
                                                    if (!c.isOnSurface()) {
                                                        tile3 = Server.caveMesh.getTile(c.getTileX(), c.getTileY());
                                                    }
                                                    this.longTarget = new LongTarget(c.getTileX(), c.getTileY(), tile3, c.isOnSurface(), c.getFloorLevel(), this);
                                                }
                                                catch (NoSuchCreatureException c) {
                                                }
                                                catch (NoSuchPlayerException c) {}
                                            } else if (WurmId.getType(target) == 5) {
                                                int x2 = (int)(target >> 32) & 0xFFFF;
                                                int y = (int)(target >> 16) & 0xFFFF;
                                                Wall wall = Wall.getWall(target);
                                                if (wall != null) {
                                                    tile4 = Server.surfaceMesh.getTile(x2, y);
                                                    this.longTarget = new LongTarget(x2, y, tile4, true, wall.getFloorLevel(), this);
                                                }
                                            }
                                            if (this.longTarget == null) continue;
                                            this.longTarget.setMissionTrigger(trig.getId());
                                            this.longTarget.setEpicMission(em.getMissionId());
                                            this.longTarget.setMissionTarget(target);
                                        }
                                    }
                                }
                                if (this.longTarget == null && Server.rand.nextInt(10) == 0) {
                                    if (this.getCitizenVillage() != null) {
                                        if (Npc.getTileRange(this, this.getCitizenVillage().getTokenX(), this.getCitizenVillage().getTokenY()) > 300.0) {
                                            int tile5 = Server.surfaceMesh.getTile(this.getCitizenVillage().getTokenX(), this.getCitizenVillage().getTokenY());
                                            this.longTarget = new LongTarget(this.getCitizenVillage().getTokenX(), this.getCitizenVillage().getTokenY(), tile5, true, 0, this);
                                        }
                                    } else {
                                        for (Village v : Villages.getVillages()) {
                                            if (!v.isPermanent || v.kingdom != this.getKingdomId() || !(Npc.getTileRange(this, v.getTokenX(), v.getTokenY()) > 300.0)) continue;
                                            int tile6 = Server.surfaceMesh.getTile(v.getTokenX(), v.getTokenY());
                                            this.longTarget = new LongTarget(v.getTokenX(), v.getTokenY(), tile6, true, 0, this);
                                        }
                                    }
                                    if (this.longTarget != null) {
                                        int seedh = Server.rand.nextInt(5);
                                        String mess = "Think I'll head home again...";
                                        switch (seedh) {
                                            case 0: {
                                                mess = "Time to go home!";
                                                break;
                                            }
                                            case 1: {
                                                mess = "Enough of this. Home Sweet Home.";
                                                break;
                                            }
                                            case 2: {
                                                mess = "Heading home. Are you coming?";
                                                break;
                                            }
                                            case 3: {
                                                mess = "I will go home now.";
                                                break;
                                            }
                                            case 4: {
                                                mess = "That's it. I'm going home.";
                                                break;
                                            }
                                            default: {
                                                mess = "Think I'll go home for a while.";
                                            }
                                        }
                                        if (this.getCurrentTile() != null) {
                                            Message m = new Message(this, 0, ":Local", "<" + this.getName() + "> " + mess);
                                            this.getCurrentTile().broadCastMessage(m);
                                        }
                                    }
                                }
                                if (this.longTarget == null && Server.rand.nextInt(100) == 0 && this.getCitizenVillage() != null && (hota2 = FocusZone.getHotaZone()) != null && !hota2.covers(this.getTileX(), this.getTileY())) {
                                    int hx = hota2.getStartX() + Server.rand.nextInt(hota2.getEndX() - hota2.getStartX());
                                    int hy = hota2.getStartY() + Server.rand.nextInt(hota2.getEndY() - hota2.getStartY());
                                    tile = Server.surfaceMesh.getTile(hx, hy);
                                    this.longTarget = new LongTarget(hx, hy, tile, true, 0, this);
                                    int seedh = Server.rand.nextInt(5);
                                    String mess = "Think I'll go hunt for some pillars a bit...";
                                    switch (seedh) {
                                        case 0: {
                                            mess = "Anyone in the Hunt of the Ancients is in trouble now!";
                                            break;
                                        }
                                        case 1: {
                                            mess = "Going to check out what happens in the Hunt.";
                                            break;
                                        }
                                        case 2: {
                                            mess = "Heading to join the Hunt. Coming with me?";
                                            break;
                                        }
                                        case 3: {
                                            mess = "Going to head to the Hunt of the Ancients. You interested?";
                                            break;
                                        }
                                        case 4: {
                                            mess = "I want to do some gloryhunting in the HOTA.";
                                            break;
                                        }
                                        default: {
                                            mess = "Think I'll go join the hunt a bit...";
                                        }
                                    }
                                    if (this.getCurrentTile() != null) {
                                        Message m = new Message(this, 0, ":Local", "<" + this.getName() + "> " + mess);
                                        this.getCurrentTile().broadCastMessage(m);
                                    }
                                }
                                if (this.longTarget != null) {
                                    return this.longTarget;
                                }
                            } else {
                                EpicMission em;
                                boolean clear = false;
                                if (this.longTarget.getCreatureTarget() != null && this.longTarget.getTileX() != this.longTarget.getCreatureTarget().getTileX()) {
                                    this.longTarget.setTileX(this.longTarget.getCreatureTarget().getTileX());
                                }
                                if (this.longTarget.getCreatureTarget() != null && this.longTarget.getTileY() != this.longTarget.getCreatureTarget().getTileY()) {
                                    this.longTarget.setTileY(this.longTarget.getCreatureTarget().getTileY());
                                }
                                if (this.longTarget.getEpicMission() > 0 && ((em = EpicServerStatus.getEpicMissionForMission(this.longTarget.getEpicMission())) == null || !em.isCurrent() || em.isCompleted())) {
                                    clear = true;
                                }
                                if (Math.abs(this.longTarget.getTileX() - tx) < 20 && Math.abs(this.longTarget.getTileY() - ty) < 20) {
                                    if (Math.abs(this.longTarget.getTileX() - tx) < 10 && Math.abs(this.longTarget.getTileY() - ty) < 10 && this.longTarget.getCreatureTarget() != null && !this.longTarget.getCreatureTarget().isFriendlyKingdom(this.getKingdomId())) {
                                        this.setTarget(this.longTarget.getCreatureTarget().getWurmId(), false);
                                        clear = true;
                                    }
                                    if (!this.isOnLongTargetTile() && this.longTargetAttempts++ <= 50) return this.longTarget;
                                    clear = true;
                                } else if (System.currentTimeMillis() - this.longTarget.getStartTime() > 3600000L) {
                                    clear = true;
                                }
                                if (clear) {
                                    this.clearLongTarget();
                                }
                            }
                        }
                        boolean flee = false;
                        if ((this.target == -10L || this.fleeCounter > 0) && (this.isTypeFleeing() || this.fleeCounter > 0) && this.isOnSurface()) {
                            if (Server.rand.nextBoolean()) {
                                if (this.getCurrentTile() != null && this.getCurrentTile().getVillage() != null) {
                                    Long[] crets;
                                    for (Long lCret : crets = this.getVisionArea().getSurface().getCreatures()) {
                                        try {
                                            Creature cret = Server.getInstance().getCreature(lCret);
                                            if (cret.getPower() != 0 || !cret.isPlayer() && !cret.isAggHuman() && !cret.isCarnivore() && !cret.isMonster()) continue;
                                            tilePosX = cret.getPosX() > this.getPosX() ? (tilePosX -= Server.rand.nextInt(6)) : (tilePosX += Server.rand.nextInt(6));
                                            tilePosY = cret.getPosY() > this.getPosY() ? (tilePosY -= Server.rand.nextInt(6)) : (tilePosY += Server.rand.nextInt(6));
                                            flee = true;
                                            break;
                                        }
                                        catch (Exception cret) {
                                            // empty catch block
                                        }
                                    }
                                }
                            } else {
                                for (Player p : Players.getInstance().getPlayers()) {
                                    if (p.getPower() != 0 && !Servers.localServer.testServer || p.getVisionArea() == null || p.getVisionArea().getSurface() == null || !p.getVisionArea().getSurface().containsCreature(this)) continue;
                                    tilePosX = p.getPosX() > this.getPosX() ? (tilePosX -= Server.rand.nextInt(6)) : (tilePosX += Server.rand.nextInt(6));
                                    tilePosY = p.getPosY() > this.getPosY() ? (tilePosY -= Server.rand.nextInt(6)) : (tilePosY += Server.rand.nextInt(6));
                                    flee = true;
                                    break;
                                }
                            }
                        }
                        if (flee || hasTarget || (currTile = this.getCurrentTile()) == null) break block119;
                        int rand = Server.rand.nextInt(9);
                        tpx = currTile.getTileX() + 4 - rand;
                        rand = Server.rand.nextInt(9);
                        tpy = currTile.getTileY() + 4 - rand;
                        totx += currTile.getTileX() - tpx;
                        toty += currTile.getTileY() - tpy;
                        if (this.longTarget == null) break block120;
                        if (Math.abs(this.longTarget.getTileX() - this.getTileX()) < 20) {
                            tpx = this.longTarget.getTileX();
                        } else {
                            tpx = this.getTileX() + 5 + Server.rand.nextInt(6);
                            if (this.getTileX() > this.longTarget.getTileX()) {
                                tpx = this.getTileX() - 5 - Server.rand.nextInt(6);
                            }
                        }
                        if (Math.abs(this.longTarget.getTileY() - this.getTileY()) >= 20) break block121;
                        tpy = this.longTarget.getTileY();
                        break block122;
                    }
                    tpy = this.getTileY() + 5 + Server.rand.nextInt(6);
                    if (this.getTileY() <= this.longTarget.getTileY()) break block122;
                    tpy = this.getTileY() - 5 - Server.rand.nextInt(6);
                    break block122;
                }
                if (this.getCitizenVillage() != null && (hota = FocusZone.getHotaZone()) != null && hota.covers(this.getTileX(), this.getTileY())) {
                    for (Item pillar : Hota.getHotaItems()) {
                        if (pillar.getTemplateId() != 739 || pillar.getZoneId() <= 0 || pillar.getData1() == this.getCitizenVillage().getId() || !(Npc.getTileRange(this, pillar.getTileX(), pillar.getTileY()) < 20.0)) continue;
                        tpx = pillar.getTileX();
                        tpy = pillar.getTileY();
                    }
                }
            }
            tpx = Zones.safeTileX(tpx);
            tpy = Zones.safeTileY(tpy);
            VolaTile t = Zones.getOrCreateTile(tpx, tpy, this.isOnSurface());
            if (this.isOnSurface()) {
                boolean stepOnBridge = false;
                if (Server.rand.nextInt(5) == 0) {
                    for (VolaTile stile : this.currentTile.getThisAndSurroundingTiles(1)) {
                        if (stile.getStructure() == null || !stile.getStructure().isTypeBridge()) continue;
                        if (stile.getStructure().isHorizontal()) {
                            if (stile.getStructure().getMaxX() != stile.getTileX() && stile.getStructure().getMinX() != stile.getTileX() || this.getTileY() != stile.getTileY()) continue;
                            tilePosX = stile.getTileX();
                            tilePosY = stile.getTileY();
                            stepOnBridge = true;
                            break;
                        }
                        if (stile.getStructure().getMaxY() != stile.getTileY() && stile.getStructure().getMinY() != stile.getTileY() || this.getTileX() != stile.getTileX()) continue;
                        tilePosX = stile.getTileX();
                        tilePosY = stile.getTileY();
                        stepOnBridge = true;
                        break;
                    }
                }
                if (!(stepOnBridge || t != null && t.getCreatures().length >= 3)) {
                    tilePosX = tpx;
                    tilePosY = tpy;
                }
            } else if (t == null || t.getCreatures().length < 3) {
                tilePosX = tpx;
                tilePosY = tpy;
            }
        }
        if ((targ = this.getTarget()) != null) {
            VolaTile currTile;
            if (targ.getCultist() != null && targ.getCultist().hasFearEffect()) {
                this.setTarget(-10L, true);
            }
            if ((currTile = targ.getCurrentTile()) != null) {
                tilePosX = currTile.tilex;
                tilePosY = currTile.tiley;
                if (seed == 100) {
                    tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                    tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                }
                int targGroup = targ.getGroupSize();
                int myGroup = this.getGroupSize();
                if (this.isOnSurface() != currTile.isOnSurface()) {
                    boolean changeLayer = false;
                    if (this.getCurrentTile().isTransition) {
                        changeLayer = true;
                    }
                    VolaTile t = this.getCurrentTile();
                    if ((this.isAggHuman() || this.isHunter() || this.isDominated()) && (!currTile.isGuarded() || t != null && t.isGuarded()) && this.isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) {
                        if (!changeLayer) {
                            int[] tiles = new int[]{tilePosX, tilePosY};
                            tiles = this.isOnSurface() ? this.findRandomCaveEntrance(tiles) : this.findRandomCaveExit(tiles);
                            tilePosX = tiles[0];
                            tilePosY = tiles[1];
                        }
                    } else {
                        this.setTarget(-10L, true);
                    }
                    if (changeLayer && (!Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty))) || MineDoorPermission.getPermission(tx, ty).mayPass(this))) {
                        this.setLayer(this.isOnSurface() ? -1 : 0, true);
                    }
                }
                if (targ.getCultist() != null && targ.getCultist().hasFearEffect()) {
                    tilePosX = Server.rand.nextBoolean() ? Math.max(currTile.getTileX() + 10, this.getTileX()) : Math.min(currTile.getTileX() - 10, this.getTileX());
                    tilePosX = Server.rand.nextBoolean() ? Math.max(currTile.getTileY() + 10, this.getTileY()) : Math.min(currTile.getTileY() - 10, this.getTileY());
                } else {
                    VolaTile t = this.getCurrentTile();
                    if (targGroup <= myGroup * this.getMaxGroupAttackSize() && (this.isAggHuman() || this.isHunter()) && (!currTile.isGuarded() || t != null && t.isGuarded())) {
                        if (this.isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) {
                            if (targ.getKingdomId() != 0 && !this.isFriendlyKingdom(targ.getKingdomId()) && (this.isDefendKingdom() || this.isAggWhitie() && targ.getKingdomTemplateId() != 3)) {
                                if (!this.isFighting()) {
                                    if (seed == 100) {
                                        tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                                        tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                                    } else {
                                        tilePosX = currTile.getTileX();
                                        tilePosY = currTile.getTileY();
                                        this.setTarget(targ.getWurmId(), false);
                                    }
                                }
                            } else if (seed == 100) {
                                tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                                tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                            } else {
                                int[] tiles;
                                tilePosX = currTile.getTileX();
                                tilePosY = currTile.getTileY();
                                if (this.getSize() < 5 && targ.getBridgeId() != -10L && this.getBridgeId() < 0L) {
                                    int[] tiles2 = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), targ.getBridgeId());
                                    if (tiles2[0] > 0) {
                                        tilePosX = tiles2[0];
                                        tilePosY = tiles2[1];
                                        if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                            tilePosX = currTile.tilex;
                                            tilePosY = currTile.tiley;
                                        }
                                    }
                                } else if (this.getBridgeId() != targ.getBridgeId() && (tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), this.getBridgeId()))[0] > 0) {
                                    tilePosX = tiles[0];
                                    tilePosY = tiles[1];
                                    if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                        tilePosX = currTile.tilex;
                                        tilePosY = currTile.tiley;
                                    }
                                }
                            }
                        } else if (!this.isFighting()) {
                            this.setTarget(-10L, true);
                        }
                    } else if (!this.isFighting()) {
                        this.setTarget(-10L, true);
                    }
                }
            }
        }
        if (tilePosX == tx && tilePosY == ty) {
            return null;
        }
        tilePosX = Zones.safeTileX(tilePosX);
        tilePosY = Zones.safeTileY(tilePosY);
        if (!this.isOnSurface()) {
            int tile = Server.caveMesh.getTile(tilePosX, tilePosY);
            if (!Tiles.isSolidCave(Tiles.decodeType(tile)) && (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters() || this.isSwimming() || this.isSubmerged())) {
                return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), -1);
            }
        } else {
            int tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
            if (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters() || this.isSwimming() || this.isSubmerged()) {
                return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), this.getFloorLevel());
            }
        }
        this.setTarget(-10L, true);
        if (!this.isDominated() || !this.hasOrders()) return null;
        this.removeOrder(this.getFirstOrder());
        return null;
    }

    private final void setPassiveCounter(int counter) {
        this.passiveCounter = counter;
    }

    private final void checkItemSpawn() {
        block9: {
            if (this.lastX == 0) {
                this.lastX = this.getTileX();
            }
            if (this.lastY == 0) {
                this.lastY = this.getTileY();
            }
            if (this.lastX - this.getTileX() > 50 || this.lastY - this.getTileY() > 50) {
                this.lastX = this.getTileX();
                this.lastY = this.getTileY();
                if (Server.rand.nextInt(10) == 0 && this.getBody().getContainersAndWornItems().length < 10) {
                    try {
                        int templateId = Server.rand.nextInt(1437);
                        ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
                        if (!template.isArmour() && !template.isWeapon() || template.isRoyal || template.artifact) break block9;
                        try {
                            Item toInsert = ItemFactory.createItem(templateId, Server.rand.nextFloat() * 80.0f + 20.0f, this.getName());
                            this.getInventory().insertItem(toInsert, true);
                            this.wearItems();
                            if (toInsert.getParentId() == this.getInventory().getWurmId()) {
                                Items.destroyItem(toInsert.getWurmId());
                            }
                        }
                        catch (FailedException failedException) {}
                    }
                    catch (NoSuchTemplateException noSuchTemplateException) {
                        // empty catch block
                    }
                }
            }
        }
    }

    @Override
    public final boolean isMoveLocal() {
        if (this.hasTrait(8)) {
            return true;
        }
        return this.template.isMoveLocal();
    }

    @Override
    public final boolean isSentinel() {
        if (this.hasTrait(9)) {
            return true;
        }
        return this.template.isSentinel();
    }

    @Override
    public final boolean isMoveGlobal() {
        if (this.hasTrait(1)) {
            return true;
        }
        return this.template.isMoveGlobal();
    }

    @Override
    public boolean isNpc() {
        return true;
    }

    @Override
    public long getFace() {
        faceRandom.setSeed(this.getWurmId());
        return faceRandom.nextLong();
    }

    @Override
    public float getSpeed() {
        if (this.getVehicle() > -10L && WurmId.getType(this.getVehicle()) == 1) {
            return 1.7f;
        }
        return 1.1f;
    }

    @Override
    public boolean isTypeFleeing() {
        return this.getStatus().modtype == 10 || this.getStatus().damage > 45000;
    }

    @Override
    public boolean isRespawn() {
        return !this.hasTrait(19);
    }

    @Override
    public final boolean isDominatable(Creature aDominator) {
        if (this.getLeader() != null && this.getLeader() != aDominator) {
            return false;
        }
        if (this.isRidden() || this.hitchedTo != null) {
            return false;
        }
        return this.hasTrait(22);
    }

    @Override
    public final float getBaseCombatRating() {
        double fskill = 1.0;
        try {
            fskill = this.skills.getSkill(1023).getKnowledge();
        }
        catch (NoSuchSkillException nss) {
            this.skills.learn(1023, 1.0f);
            fskill = 1.0;
        }
        if (this.getLoyalty() > 0.0f) {
            return (float)Math.max(1.0, (double)(this.isReborn() ? 0.7f : 0.5f) * fskill / 5.0 * (double)this.status.getBattleRatingTypeModifier()) * Servers.localServer.getCombatRatingModifier();
        }
        return (float)Math.max(1.0, fskill / 5.0 * (double)this.status.getBattleRatingTypeModifier()) * Servers.localServer.getCombatRatingModifier();
    }
}

