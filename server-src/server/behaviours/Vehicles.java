package com.wurmonline.server.behaviours;

import com.wurmonline.server.Features;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.shared.constants.ProtoConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class Vehicles implements ProtoConstants, CreatureTemplateIds {
   private static final Logger logger = Logger.getLogger(Vehicles.class.getName());
   private static Map<Long, Vehicle> vehicles = new ConcurrentHashMap<>();

   private Vehicles() {
   }

   public static Vehicle getVehicle(Item item) {
      Vehicle toReturn = null;
      if (item.isVehicle() || item.isHitchTarget()) {
         toReturn = vehicles.get(item.getWurmId());
         if (toReturn == null) {
            toReturn = createVehicle(item);
         }
      }

      return toReturn;
   }

   public static Vehicle getVehicle(Creature creature) {
      Vehicle toReturn = null;
      if (creature.isVehicle()) {
         toReturn = vehicles.get(creature.getWurmId());
         if (toReturn == null) {
            toReturn = createVehicle(creature);
         }
      }

      return toReturn;
   }

   public static Vehicle getVehicleForId(long id) {
      return vehicles.get(id);
   }

   public static Vehicle createVehicle(Item item) {
      Vehicle vehic = new Vehicle(item.getWurmId());
      setSettingsForVehicle(item, vehic);
      vehicles.put(item.getWurmId(), vehic);
      return vehic;
   }

   public static Vehicle createVehicle(Creature creature) {
      Vehicle vehic = new Vehicle(creature.getWurmId());
      setSettingsForVehicle(creature, vehic);
      vehicles.put(creature.getWurmId(), vehic);
      return vehic;
   }

   static void setSettingsForVehicle(Creature creature, Vehicle vehicle) {
      int cid = creature.getTemplate().getTemplateId();
      vehicle.embarkString = "mount";
      vehicle.embarksString = "mounts";
      if (cid == 49 || cid == 3) {
         vehicle.createPassengerSeats(0);
         vehicle.setSeatFightMod(0, 0.7F, 0.9F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F);
         vehicle.creature = true;
         vehicle.skillNeeded = 19.0F;
         if (cid == 49) {
            vehicle.skillNeeded = 23.0F;
         }

         vehicle.name = creature.getName();
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.setMaxSpeed(17.0F);
         vehicle.commandType = 3;
      } else if (cid == 64) {
         vehicle.createPassengerSeats(0);
         vehicle.setSeatFightMod(0, 0.7F, 0.9F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F);
         vehicle.creature = true;
         vehicle.skillNeeded = 21.0F;
         vehicle.name = creature.getName();
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.setMaxSpeed(30.0F);
         vehicle.commandType = 3;
         vehicle.canHaveEquipment = true;
      } else if (cid == 83) {
         vehicle.createPassengerSeats(0);
         vehicle.setSeatFightMod(0, 0.7F, 0.9F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F);
         vehicle.creature = true;
         vehicle.skillNeeded = 31.0F;
         vehicle.name = creature.getName();
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.setMaxSpeed(32.0F);
         vehicle.commandType = 3;
         vehicle.canHaveEquipment = true;
      } else if (CreatureTemplate.isFullyGrownDragon(cid)) {
         vehicle.createPassengerSeats(1);
         vehicle.setSeatFightMod(0, 0.3F, 1.5F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 3.0F);
         vehicle.setSeatFightMod(1, 0.3F, 1.3F);
         vehicle.setSeatOffset(1, 1.0F, 0.0F, 3.0F);
         vehicle.creature = true;
         vehicle.skillNeeded = 30.0F;
         vehicle.name = creature.getName();
         vehicle.setMaxSpeed(35.0F);
         vehicle.maxDepth = -0.7F;
         vehicle.commandType = 3;
         vehicle.canHaveEquipment = true;
      } else if (CreatureTemplate.isDragonHatchling(cid)) {
         vehicle.createPassengerSeats(0);
         vehicle.setSeatFightMod(0, 0.6F, 1.3F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.5F);
         vehicle.creature = true;
         vehicle.skillNeeded = 29.0F;
         vehicle.name = creature.getName();
         vehicle.setMaxSpeed(33.0F);
         vehicle.maxDepth = -0.7F;
         vehicle.commandType = 3;
         vehicle.canHaveEquipment = true;
      } else if (cid == 12) {
         vehicle.createPassengerSeats(0);
         vehicle.setSeatFightMod(0, 0.8F, 1.1F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F);
         vehicle.creature = true;
         vehicle.skillNeeded = 23.0F;
         vehicle.name = creature.getName();
         vehicle.maxHeightDiff = 0.04F;
         vehicle.maxDepth = -0.7F;
         vehicle.setMaxSpeed(20.0F);
         vehicle.commandType = 3;
      } else if (cid == 40 || cid == 37 || cid == 86) {
         vehicle.createPassengerSeats(0);
         vehicle.setSeatFightMod(0, 0.8F, 1.1F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.5F);
         vehicle.creature = true;
         vehicle.skillNeeded = 24.0F;
         vehicle.name = creature.getName();
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.setMaxSpeed(20.0F);
         vehicle.commandType = 3;
      } else if (cid == 59) {
         vehicle.createPassengerSeats(0);
         vehicle.setSeatFightMod(0, 0.5F, 1.1F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F);
         vehicle.creature = true;
         vehicle.skillNeeded = 25.0F;
         vehicle.name = creature.getName();
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.setMaxSpeed(17.0F);
         vehicle.commandType = 3;
      } else if (cid == 21) {
         vehicle.createPassengerSeats(1);
         vehicle.setSeatFightMod(0, 0.6F, 1.0F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F);
         vehicle.setSeatFightMod(1, 0.9F, 0.5F);
         vehicle.setSeatOffset(1, 0.5F, 0.0F, 1.54F);
         vehicle.creature = true;
         vehicle.skillNeeded = 26.0F;
         vehicle.name = creature.getName();
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.setMaxSpeed(33.0F);
         vehicle.commandType = 3;
         vehicle.canHaveEquipment = true;
      } else if (cid == 24) {
         vehicle.createPassengerSeats(1);
         vehicle.setSeatFightMod(0, 0.6F, 1.0F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.5F);
         vehicle.setSeatFightMod(1, 0.3F, 1.3F);
         vehicle.setSeatOffset(1, 0.5F, -0.3F, 0.5F);
         vehicle.setSeatFightMod(2, 0.3F, 1.3F);
         vehicle.setSeatOffset(2, 0.5F, 0.3F, 0.5F);
         vehicle.creature = true;
         vehicle.skillNeeded = 26.0F;
         vehicle.name = creature.getName();
         vehicle.maxDepth = -0.3F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.setMaxSpeed(20.0F);
         vehicle.commandType = 3;
      } else if (cid == 58) {
         vehicle.createPassengerSeats(1);
         vehicle.setSeatFightMod(0, 1.3F, 1.0F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F);
         vehicle.setSeatOffset(1, 0.5F, 0.0F, 0.0F);
         vehicle.creature = true;
         vehicle.skillNeeded = 25.0F;
         vehicle.maxHeight = 2499.0F;
         vehicle.name = creature.getName();
         vehicle.maxHeightDiff = 0.04F;
         vehicle.setMaxSpeed(11.0F);
         vehicle.commandType = 3;
      }
   }

   static void setSettingsForVehicle(Item item, Vehicle vehicle) {
      if (item.getTemplateId() == 861) {
         vehicle.setUnmountable(true);
         vehicle.createOnlyPassengerSeats(1);
         vehicle.setSeatFightMod(0, 0.7F, 0.4F);
         vehicle.creature = false;
         vehicle.embarkString = "enter";
         vehicle.embarksString = "enters";
         vehicle.name = item.getName();
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.commandType = 2;
         Seat[] hitches = new Seat[]{new Seat((byte)2)};
         hitches[0].offx = -3.0F;
         hitches[0].offy = -1.0F;
         vehicle.addHitchSeats(hitches);
      }

      if (item.getTemplateId() == 863) {
         vehicle.setUnmountable(true);
         vehicle.createOnlyPassengerSeats(1);
         vehicle.setSeatFightMod(0, 0.7F, 0.4F);
         vehicle.creature = false;
         vehicle.embarkString = "enter";
         vehicle.embarksString = "enters";
         vehicle.name = item.getName();
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.commandType = 2;
         Seat[] hitches = new Seat[1];
         hitches[0] = new Seat((byte)2);
         hitches[0].offx = -3.0F;
         hitches[0].offy = -1.0F;
         vehicle.addHitchSeats(hitches);
      }

      if (item.getTemplateId() == 864) {
         vehicle.setUnmountable(true);
         vehicle.createOnlyPassengerSeats(1);
         vehicle.setSeatFightMod(0, 0.7F, 0.4F);
         vehicle.creature = false;
         vehicle.embarkString = "enter";
         vehicle.embarksString = "enters";
         vehicle.name = item.getName();
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.commandType = 2;
         Seat[] hitches = new Seat[1];
         hitches[0] = new Seat((byte)2);
         hitches[0].offx = -3.0F;
         hitches[0].offy = -1.0F;
         vehicle.addHitchSeats(hitches);
      }

      if (item.getTemplateId() == 186) {
         vehicle.setUnmountable(true);
         vehicle.createOnlyPassengerSeats(1);
         vehicle.setSeatFightMod(0, 0.7F, 0.4F);
         vehicle.creature = false;
         vehicle.embarkString = "board";
         vehicle.embarksString = "boards";
         vehicle.name = item.getName();
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.commandType = 2;
      } else if (item.getTemplateId() == 490) {
         vehicle.createPassengerSeats(2);
         vehicle.pilotName = "captain";
         vehicle.creature = false;
         vehicle.name = item.getName();
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F, 0.246F);
         vehicle.setSeatOffset(1, -0.997F, 0.0F, 0.246F);
         vehicle.setSeatOffset(2, -2.018F, 0.0F, 0.246F);
         vehicle.setSeatFightMod(0, 0.7F, 0.4F);
         vehicle.setSeatFightMod(1, 1.5F, 0.4F);
         vehicle.setSeatFightMod(2, 1.5F, 0.4F);
         vehicle.setWindImpact((byte)10);
         vehicle.maxHeight = -0.5F;
         vehicle.skillNeeded = 19.0F;
         vehicle.setMaxSpeed(5.0F);
         vehicle.commandType = 1;
         vehicle.setMaxAllowedLoadDistance(6);
      } else if (item.getTemplateId() == 491) {
         vehicle.createPassengerSeats(4);
         vehicle.pilotName = "captain";
         vehicle.creature = false;
         vehicle.embarkString = "board";
         vehicle.embarksString = "boards";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 0.9F, 0.4F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F, 0.351F);
         vehicle.setSeatOffset(1, -1.392F, 0.378F, 0.351F);
         vehicle.setSeatOffset(2, -2.15F, -0.349F, 0.341F);
         vehicle.setSeatOffset(3, -3.7F, -0.281F, 0.34F);
         vehicle.setSeatOffset(4, -4.39F, 0.14F, 0.352F);
         vehicle.setWindImpact((byte)30);
         vehicle.maxHeight = -0.5F;
         vehicle.skillNeeded = 20.1F;
         vehicle.setMaxSpeed(5.0F);
         vehicle.commandType = 1;
         vehicle.setMaxAllowedLoadDistance(6);
      } else if (item.getTemplateId() == 539) {
         vehicle.createPassengerSeats(3);
         vehicle.pilotName = "driver";
         vehicle.creature = false;
         vehicle.embarkString = "ride";
         vehicle.embarksString = "rides";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 0.9F, 0.3F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F, 1.458F);
         vehicle.setSeatFightMod(1, 1.0F, 0.4F);
         vehicle.setSeatOffset(1, 0.448F, 0.729F, 1.529F);
         vehicle.setSeatFightMod(2, 1.0F, 0.4F);
         vehicle.setSeatOffset(2, 0.65F, -0.697F, 1.568F);
         vehicle.setSeatFightMod(3, 1.0F, 0.0F);
         vehicle.setSeatOffset(3, 1.122F, 0.738F, 1.621F);
         vehicle.maxHeightDiff = 0.04F;
         vehicle.maxDepth = -0.7F;
         vehicle.skillNeeded = 20.1F;
         vehicle.setMaxSpeed(0.75F);
         vehicle.commandType = 2;
         Seat[] hitches = new Seat[2];
         hitches[0] = new Seat((byte)2);
         hitches[1] = new Seat((byte)2);
         hitches[0].offx = -2.0F;
         hitches[0].offy = -1.0F;
         hitches[1].offx = -2.0F;
         hitches[1].offy = 1.0F;
         vehicle.addHitchSeats(hitches);
         vehicle.setMaxAllowedLoadDistance(4);
      } else if (item.getTemplateId() == 853) {
         vehicle.createPassengerSeats(0);
         vehicle.pilotName = "driver";
         vehicle.creature = false;
         vehicle.embarkString = "ride";
         vehicle.embarksString = "rides";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 0.9F, 0.3F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F, 1.563F);
         vehicle.maxHeightDiff = 0.04F;
         vehicle.maxDepth = -1.5F;
         vehicle.skillNeeded = 20.1F;
         vehicle.setMaxSpeed(0.5F);
         vehicle.commandType = 2;
         Seat[] hitches = new Seat[2];
         hitches[0] = new Seat((byte)2);
         hitches[1] = new Seat((byte)2);
         hitches[0].offx = -2.0F;
         hitches[0].offy = -1.0F;
         hitches[1].offx = -2.0F;
         hitches[1].offy = 1.0F;
         vehicle.addHitchSeats(hitches);
      } else if (item.getTemplateId() == 1410) {
         vehicle.createPassengerSeats(1);
         vehicle.pilotName = "driver";
         vehicle.creature = false;
         vehicle.embarkString = "ride";
         vehicle.embarksString = "rides";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 0.9F, 0.3F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F, 1.563F);
         vehicle.setSeatFightMod(1, 1.0F, 0.4F);
         vehicle.setSeatOffset(1, 4.05F, 0.0F, 0.84F);
         vehicle.maxHeightDiff = 0.04F;
         vehicle.maxDepth = -1.5F;
         vehicle.skillNeeded = 20.1F;
         vehicle.setMaxSpeed(0.5F);
         vehicle.commandType = 2;
         Seat[] hitches = new Seat[2];
         hitches[0] = new Seat((byte)2);
         hitches[1] = new Seat((byte)2);
         hitches[0].offx = -2.0F;
         hitches[0].offy = -1.0F;
         hitches[1].offx = -2.0F;
         hitches[1].offy = 1.0F;
         vehicle.addHitchSeats(hitches);
      } else if (item.getTemplateId() == 850) {
         if (Features.Feature.WAGON_PASSENGER.isEnabled()) {
            vehicle.createPassengerSeats(1);
         } else {
            vehicle.createPassengerSeats(0);
         }

         vehicle.pilotName = "driver";
         vehicle.creature = false;
         vehicle.embarkString = "ride";
         vehicle.embarksString = "rides";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 0.9F, 0.3F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F, 1.453F);
         if (Features.Feature.WAGON_PASSENGER.isEnabled()) {
            vehicle.setSeatFightMod(1, 1.0F, 0.4F);
            vehicle.setSeatOffset(1, 4.05F, 0.0F, 0.84F);
         }

         vehicle.maxHeightDiff = 0.04F;
         vehicle.maxDepth = -0.7F;
         vehicle.skillNeeded = 21.0F;
         vehicle.setMaxSpeed(0.7F);
         vehicle.commandType = 2;
         Seat[] hitches = new Seat[4];
         hitches[0] = new Seat((byte)2);
         hitches[1] = new Seat((byte)2);
         hitches[2] = new Seat((byte)2);
         hitches[3] = new Seat((byte)2);
         hitches[0].offx = -2.0F;
         hitches[0].offy = -1.0F;
         hitches[1].offx = -2.0F;
         hitches[1].offy = 1.0F;
         hitches[2].offx = -5.0F;
         hitches[2].offy = -1.0F;
         hitches[3].offx = -5.0F;
         hitches[3].offy = 1.0F;
         vehicle.addHitchSeats(hitches);
         vehicle.setMaxAllowedLoadDistance(4);
      } else if (item.getTemplateId() == 541) {
         vehicle.createPassengerSeats(6);
         vehicle.pilotName = "captain";
         vehicle.creature = false;
         vehicle.embarkString = "board";
         vehicle.embarksString = "boards";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 0.9F, 0.9F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F, 3.02F);
         vehicle.setSeatOffset(1, -7.192F, -1.036F, 2.16F);
         vehicle.setSeatOffset(2, 3.0F, 1.287F, 2.47F);
         vehicle.setSeatOffset(3, -3.657F, 1.397F, 1.93F);
         vehicle.setSeatOffset(4, 2.858F, -1.076F, 2.473F);
         vehicle.setSeatOffset(5, -5.625F, 0.679F, 1.926F);
         vehicle.setSeatOffset(6, -2.3F, -1.838F, 1.93F);
         vehicle.setWindImpact((byte)60);
         vehicle.maxHeight = -2.0F;
         vehicle.skillNeeded = 21.0F;
         vehicle.setMaxSpeed(3.8F);
         vehicle.commandType = 1;
         vehicle.setMaxAllowedLoadDistance(12);
      } else if (item.getTemplateId() == 540) {
         vehicle.createPassengerSeats(8);
         vehicle.pilotName = "captain";
         vehicle.creature = false;
         vehicle.embarkString = "board";
         vehicle.embarksString = "boards";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 0.9F, 0.9F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F, 4.011F);
         vehicle.setSeatOffset(1, -16.042F, -0.901F, 3.96F);
         vehicle.setSeatOffset(2, -7.629F, 0.0F, 14.591F);
         vehicle.setSeatOffset(3, -4.411F, -2.097F, 3.51F);
         vehicle.setSeatOffset(4, -16.01F, 0.838F, 3.96F);
         vehicle.setSeatOffset(5, -9.588F, -1.855F, 1.802F);
         vehicle.setSeatOffset(6, -11.08F, 2.451F, 1.805F);
         vehicle.setSeatOffset(7, -4.411F, 1.774F, 3.52F);
         vehicle.setSeatOffset(8, -1.813F, -1.872F, 3.789F);
         vehicle.setWindImpact((byte)80);
         vehicle.maxHeight = -2.0F;
         vehicle.skillNeeded = 22.0F;
         vehicle.setMaxSpeed(3.5F);
         vehicle.commandType = 1;
         vehicle.setMaxAllowedLoadDistance(12);
      } else if (item.getTemplateId() == 542) {
         vehicle.createPassengerSeats(12);
         vehicle.pilotName = "captain";
         vehicle.creature = false;
         vehicle.embarkString = "board";
         vehicle.embarksString = "boards";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 0.9F, 0.9F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F, 0.787F);
         vehicle.setSeatOffset(1, -7.713F, -0.41F, 0.485F);
         vehicle.setSeatOffset(2, -9.722F, 0.455F, 0.417F);
         vehicle.setSeatOffset(3, -3.85F, -0.412F, 0.598F);
         vehicle.setSeatOffset(4, -11.647F, 0.0F, 0.351F);
         vehicle.setSeatOffset(5, -1.916F, -0.211F, 0.651F);
         vehicle.setSeatOffset(6, -12.627F, 0.018F, 0.469F);
         vehicle.setSeatOffset(7, -5.773F, 0.429F, 0.547F);
         vehicle.setSeatOffset(8, -2.882F, 0.388F, 0.626F);
         vehicle.setSeatOffset(9, -8.726F, 0.013F, 0.445F);
         vehicle.setSeatOffset(10, -10.66F, -0.162F, 0.387F);
         vehicle.setSeatOffset(11, -7.708F, 0.454F, 0.479F);
         vehicle.setSeatOffset(12, -5.773F, -0.429F, 0.547F);
         vehicle.setWindImpact((byte)50);
         vehicle.maxHeight = -0.5F;
         vehicle.skillNeeded = 23.0F;
         vehicle.setMaxSpeed(4.1F);
         vehicle.commandType = 1;
         vehicle.setMaxAllowedLoadDistance(8);
      } else if (item.getTemplateId() == 543) {
         vehicle.createPassengerSeats(13);
         vehicle.pilotName = "captain";
         vehicle.creature = false;
         vehicle.embarkString = "board";
         vehicle.embarksString = "boards";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 0.9F, 0.9F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.0F, 3.866F);
         vehicle.setSeatOffset(1, -6.98F, 0.0F, 12.189F);
         vehicle.setSeatOffset(2, -14.716F, -0.202F, 3.402F);
         vehicle.setSeatOffset(3, -4.417F, 1.024F, 2.013F);
         vehicle.setSeatOffset(4, 1.206F, -0.657F, 4.099F);
         vehicle.setSeatOffset(5, -7.953F, 0.028F, 0.731F);
         vehicle.setSeatOffset(6, -5.317F, -1.134F, 1.941F);
         vehicle.setSeatOffset(7, -7.518F, 1.455F, 0.766F);
         vehicle.setSeatOffset(8, -2.598F, -0.104F, 2.22F);
         vehicle.setSeatOffset(9, -12.46F, 0.796F, 2.861F);
         vehicle.setSeatOffset(10, -12.417F, -0.82F, 2.852F);
         vehicle.setSeatOffset(11, -4.046F, -0.536F, 2.056F);
         vehicle.setSeatOffset(12, -1.089F, 1.004F, 3.65F);
         vehicle.setSeatOffset(13, -0.942F, -0.845F, 3.678F);
         vehicle.setWindImpact((byte)70);
         vehicle.maxHeight = -2.0F;
         vehicle.skillNeeded = 24.0F;
         vehicle.setMaxSpeed(4.0F);
         vehicle.commandType = 1;
         vehicle.setMaxAllowedLoadDistance(12);
      } else if (item.getTemplateId() == 931) {
         vehicle.createPassengerSeats(0);
         vehicle.pilotName = "pusher";
         vehicle.creature = false;
         vehicle.embarkString = "board";
         vehicle.embarksString = "boards";
         vehicle.name = item.getName();
         vehicle.setWindImpact((byte)0);
         vehicle.maxHeight = 6000.0F;
         vehicle.maxDepth = -1.0F;
         vehicle.skillNeeded = 200.0F;
         vehicle.setMaxSpeed(3.0F);
      } else if (item.getTemplateId() == 936 || item.getTemplateId() == 937) {
         vehicle.createPassengerSeats(0);
         vehicle.pilotName = "mover";
         vehicle.creature = false;
         vehicle.embarkString = "board";
         vehicle.embarksString = "boards";
         vehicle.name = item.getName();
         vehicle.setWindImpact((byte)0);
         vehicle.maxHeight = 6000.0F;
         vehicle.maxDepth = -1.0F;
         vehicle.skillNeeded = 200.0F;
         vehicle.setMaxSpeed(0.0F);
      } else if (item.getTemplateId() == 263 || item.getTemplateId() == 265) {
         vehicle.createOnlyPassengerSeats(1);
         vehicle.setChair(true);
         vehicle.creature = false;
         vehicle.embarkString = "sit";
         vehicle.embarksString = "sits";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 1.0F, 0.4F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.59F);
         vehicle.setWindImpact((byte)0);
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.commandType = 2;
      } else if (item.getTemplateId() == 404 || item.getTemplateId() == 891 || item.getTemplateId() == 924) {
         vehicle.createOnlyPassengerSeats(2);
         vehicle.setChair(true);
         vehicle.creature = false;
         vehicle.embarkString = "sit";
         vehicle.embarksString = "sits";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 1.0F, 0.4F);
         vehicle.setSeatOffset(0, 0.0F, -0.4F, 0.59F);
         vehicle.setSeatFightMod(1, 1.0F, 0.4F);
         vehicle.setSeatOffset(1, 0.0F, 0.4F, 0.59F);
         vehicle.setWindImpact((byte)0);
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.commandType = 2;
      } else if (item.getTemplateId() == 261 || item.getTemplateId() == 913 || item.getTemplateId() == 914 || item.getTemplateId() == 915) {
         vehicle.createOnlyPassengerSeats(1);
         vehicle.setChair(true);
         vehicle.creature = false;
         vehicle.embarkString = "sit";
         vehicle.embarksString = "sits";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 1.0F, 0.4F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.57F);
         vehicle.setWindImpact((byte)0);
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.commandType = 2;
      } else if (item.getTemplateId() == 894) {
         vehicle.createOnlyPassengerSeats(1);
         vehicle.setChair(true);
         vehicle.creature = false;
         vehicle.embarkString = "sit";
         vehicle.embarksString = "sits";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 1.0F, 0.4F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.63F);
         vehicle.setWindImpact((byte)0);
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.commandType = 2;
      } else if (item.getTemplateId() == 1313 || item.getTemplateId() == 484 || item.getTemplateId() == 890) {
         vehicle.createOnlyPassengerSeats(1);
         vehicle.setBed(true);
         vehicle.creature = false;
         vehicle.embarkString = "lie down";
         vehicle.embarksString = "lies down";
         vehicle.name = item.getName();
         vehicle.setSeatFightMod(0, 1.0F, 0.4F);
         vehicle.setSeatOffset(0, 0.0F, 0.0F, 0.57F);
         vehicle.setWindImpact((byte)0);
         vehicle.maxDepth = -0.7F;
         vehicle.maxHeightDiff = 0.04F;
         vehicle.commandType = 2;
      }
   }

   public static void destroyVehicle(long id) {
      Vehicle vehicle = vehicles.get(id);
      if (vehicle != null) {
         vehicle.kickAll();
         vehicle.seats = Vehicle.EMPTYSEATS;
         if (vehicle.getDraggers() != null) {
            vehicle.purgeDraggers();
         }
      }

      vehicles.remove(id);
      ItemSettings.remove(id);
   }

   static int getNumberOfVehicles() {
      if (vehicles != null) {
         return vehicles.size();
      } else {
         logger.warning("vehicles Map is null");
         return 0;
      }
   }

   public static final void removeDragger(Creature dragger) {
      for(Vehicle draggedVehicle : vehicles.values().toArray(new Vehicle[vehicles.size()])) {
         if (draggedVehicle.removeDragger(dragger)) {
            dragger.setHitched(null, false);
            return;
         }
      }
   }
}
