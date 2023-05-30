package com.wurmonline.server.behaviours;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Behaviours {
   private static final Logger logger = Logger.getLogger(Behaviours.class.getName());
   private static Behaviours instance = null;
   private static Map<Short, Behaviour> behaviours = new HashMap<>();

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
      } else {
         return toReturn;
      }
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
