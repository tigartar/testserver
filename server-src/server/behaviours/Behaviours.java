/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.behaviours.AlmanacBehaviour;
import com.wurmonline.server.behaviours.ArtifactBehaviour;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.BodyPartBehaviour;
import com.wurmonline.server.behaviours.BridgeCornerBehaviour;
import com.wurmonline.server.behaviours.BridgePartBehaviour;
import com.wurmonline.server.behaviours.CaveTileBehaviour;
import com.wurmonline.server.behaviours.CaveWallBehaviour;
import com.wurmonline.server.behaviours.CornucopiaBehaviour;
import com.wurmonline.server.behaviours.CorpseBehaviour;
import com.wurmonline.server.behaviours.CreatureBehaviour;
import com.wurmonline.server.behaviours.DomainItemBehaviour;
import com.wurmonline.server.behaviours.ExamineBehaviour;
import com.wurmonline.server.behaviours.FenceBehaviour;
import com.wurmonline.server.behaviours.FireBehaviour;
import com.wurmonline.server.behaviours.FloorBehaviour;
import com.wurmonline.server.behaviours.FlowerpotBehaviour;
import com.wurmonline.server.behaviours.GravestoneBehaviour;
import com.wurmonline.server.behaviours.HugeAltarBehaviour;
import com.wurmonline.server.behaviours.HugeLogBehaviour;
import com.wurmonline.server.behaviours.InventoryBehaviour;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.ItemPileBehaviour;
import com.wurmonline.server.behaviours.MarkerBehaviour;
import com.wurmonline.server.behaviours.MenuRequestBehaviour;
import com.wurmonline.server.behaviours.MissionBehaviour;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.behaviours.OwnershipPaperBehaviour;
import com.wurmonline.server.behaviours.PapyrusBehaviour;
import com.wurmonline.server.behaviours.PlanetBehaviour;
import com.wurmonline.server.behaviours.PlanterBehaviour;
import com.wurmonline.server.behaviours.PracticeDollBehaviour;
import com.wurmonline.server.behaviours.ShardBehaviour;
import com.wurmonline.server.behaviours.SkillBehaviour;
import com.wurmonline.server.behaviours.StructureBehaviour;
import com.wurmonline.server.behaviours.TicketBehaviour;
import com.wurmonline.server.behaviours.TileBehaviour;
import com.wurmonline.server.behaviours.TileBorderBehaviour;
import com.wurmonline.server.behaviours.TileCornerBehaviour;
import com.wurmonline.server.behaviours.TileDirtBehaviour;
import com.wurmonline.server.behaviours.TileFieldBehaviour;
import com.wurmonline.server.behaviours.TileGrassBehaviour;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.behaviours.TileTreeBehaviour;
import com.wurmonline.server.behaviours.ToyBehaviour;
import com.wurmonline.server.behaviours.TraderBookBehaviour;
import com.wurmonline.server.behaviours.TrellisBehaviour;
import com.wurmonline.server.behaviours.UnfinishedItemBehaviour;
import com.wurmonline.server.behaviours.VegetableBehaviour;
import com.wurmonline.server.behaviours.VehicleBehaviour;
import com.wurmonline.server.behaviours.VillageDeedBehaviour;
import com.wurmonline.server.behaviours.VillageTokenBehaviour;
import com.wurmonline.server.behaviours.WagonerContainerBehaviour;
import com.wurmonline.server.behaviours.WagonerContractBehaviour;
import com.wurmonline.server.behaviours.WallBehaviour;
import com.wurmonline.server.behaviours.WarmachineBehaviour;
import com.wurmonline.server.behaviours.WoundBehaviour;
import com.wurmonline.server.behaviours.WritBehaviour;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Behaviours {
    private static final Logger logger = Logger.getLogger(Behaviours.class.getName());
    private static Behaviours instance = null;
    private static Map<Short, Behaviour> behaviours = new HashMap<Short, Behaviour>();

    public static Behaviours getInstance() {
        if (instance == null) {
            instance = new Behaviours();
        }
        return instance;
    }

    private Behaviours() {
    }

    void addBehaviour(Behaviour aBehaviour) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Adding Behaviour: " + aBehaviour + ", Class: " + aBehaviour.getClass());
        }
        behaviours.put(aBehaviour.getType(), aBehaviour);
    }

    public Behaviour getBehaviour(short type) throws NoSuchBehaviourException {
        Behaviour toReturn = behaviours.get(type);
        if (toReturn == null) {
            throw new NoSuchBehaviourException("No Behaviour with type " + type);
        }
        return toReturn;
    }

    static {
        new Behaviour();
        new ItemBehaviour();
        new CreatureBehaviour();
        new TileBehaviour();
        new TileTreeBehaviour();
        new BodyPartBehaviour();
        new TileGrassBehaviour();
        new TileRockBehaviour();
        new ExamineBehaviour();
        new TileDirtBehaviour();
        new TileFieldBehaviour();
        new VegetableBehaviour();
        new FireBehaviour();
        new WallBehaviour();
        new WritBehaviour();
        new ItemPileBehaviour();
        new FenceBehaviour();
        new UnfinishedItemBehaviour();
        new VillageDeedBehaviour();
        new VillageTokenBehaviour();
        new ToyBehaviour();
        new WoundBehaviour();
        new CorpseBehaviour();
        new TraderBookBehaviour();
        new CornucopiaBehaviour();
        new PracticeDollBehaviour();
        new TileBorderBehaviour();
        new DomainItemBehaviour();
        new HugeAltarBehaviour();
        new ArtifactBehaviour();
        new PlanetBehaviour();
        new HugeLogBehaviour();
        new CaveWallBehaviour();
        new CaveTileBehaviour();
        new WarmachineBehaviour();
        new VehicleBehaviour();
        new SkillBehaviour();
        new MissionBehaviour();
        new PapyrusBehaviour();
        new StructureBehaviour();
        new FloorBehaviour();
        new ShardBehaviour();
        new FlowerpotBehaviour();
        new GravestoneBehaviour();
        new InventoryBehaviour();
        new TicketBehaviour();
        new BridgePartBehaviour();
        new OwnershipPaperBehaviour();
        new MenuRequestBehaviour();
        new TileCornerBehaviour();
        new PlanterBehaviour();
        new MarkerBehaviour();
        new AlmanacBehaviour();
        new TrellisBehaviour();
        new WagonerContractBehaviour();
        new BridgeCornerBehaviour();
        new WagonerContainerBehaviour();
    }
}

