package com.wurmonline.server.creatures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.BodyFactory;
import com.wurmonline.server.intra.PlayerTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.utils.DbUtilities;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreatureDataStream {
   private static Logger logger = Logger.getLogger(CreatureDataStream.class.getName());

   private CreatureDataStream() {
   }

   public static boolean validateCreature(Creature animal) {
      return animal.getStatus() != null && animal.getTemplate() != null && animal.getBody() != null;
   }

   public static void toStream(Creature animal, DataOutputStream outputStream) throws IOException {
      Offspring baby = animal.getOffspring();
      if (baby != null) {
         outputStream.writeBoolean(true);
         outputStream.writeLong(baby.getMother());
         outputStream.writeLong(baby.getFather());
         outputStream.writeLong(baby.getTraits());
         outputStream.writeByte((byte)baby.getDaysLeft());
      } else {
         outputStream.writeBoolean(false);
      }

      outputStream.writeLong(animal.getWurmId());
      outputStream.writeUTF(animal.name);
      outputStream.writeUTF(animal.getTemplate().getName());
      outputStream.writeByte(animal.getSex());
      outputStream.writeShort(animal.getCentimetersHigh());
      outputStream.writeShort(animal.getCentimetersLong());
      outputStream.writeShort(animal.getCentimetersWide());
      outputStream.writeLong(animal.getStatus().inventoryId);
      outputStream.writeLong(animal.getBody().getId());
      outputStream.writeLong(animal.getBuildingId());
      outputStream.writeShort(animal.getStatus().getStamina() & 65535);
      outputStream.writeShort(animal.getStatus().getHunger() & 65535);
      outputStream.writeFloat(animal.getStatus().getNutritionlevel());
      outputStream.writeShort(animal.getStatus().getThirst() & 65535);
      outputStream.writeBoolean(animal.isDead());
      outputStream.writeBoolean(animal.isStealth());
      outputStream.writeByte(0);
      outputStream.writeInt(animal.getStatus().age);
      outputStream.writeLong(animal.getStatus().lastPolledAge);
      outputStream.writeByte(animal.getStatus().fat);
      outputStream.writeLong(animal.getStatus().traits);
      outputStream.writeLong(-10L);
      outputStream.writeLong(animal.getMother());
      outputStream.writeLong(animal.getFather());
      outputStream.writeBoolean(animal.isReborn());
      outputStream.writeFloat(0.0F);
      outputStream.writeLong(animal.getStatus().lastPolledLoyalty);
      outputStream.writeBoolean(animal.isOffline());
      outputStream.writeBoolean(animal.isStayonline());
      outputStream.writeShort(animal.getStatus().detectInvisCounter);
      outputStream.writeByte(animal.getDisease());
      outputStream.writeLong(animal.getLastGroomed());
      outputStream.writeLong(animal.getVehicle());
      outputStream.writeByte(animal.getStatus().modtype);
      outputStream.writeUTF(animal.petName);
      if (animal.getSkills() != null && animal.getSkills().getSkillsNoTemp() != null) {
         Skill[] animalSkills = animal.getSkills().getSkillsNoTemp();
         int numSkills = animalSkills.length;
         outputStream.writeInt(numSkills);

         for(Skill curSkill : animalSkills) {
            if (!curSkill.isTemporary()) {
               outputStream.writeInt(curSkill.getNumber());
               outputStream.writeDouble(curSkill.getKnowledge());
               outputStream.writeDouble(curSkill.getMinimumValue());
               outputStream.writeLong(curSkill.lastUsed);
               outputStream.writeLong(curSkill.id);
            }
         }
      } else {
         outputStream.writeInt(0);
      }

      Item[] animalItems = animal.getAllItems();
      int numItems = 0;

      for(Item curItem : animalItems) {
         if (!curItem.isBodyPart() && !curItem.isInventory() && !curItem.isTemporary()) {
            ++numItems;
         }
      }

      outputStream.writeInt(numItems);

      for(Item curItem : animalItems) {
         if (!curItem.isBodyPart() && !curItem.isInventory() && !curItem.isTemporary()) {
            PlayerTransfer.sendItem(curItem, outputStream, false);
         }
      }
   }

   public static void fromStream(DataInputStream inputStream) throws IOException {
      Connection dbcon = null;
      PreparedStatement ps = null;
      boolean hasBaby = inputStream.readBoolean();
      if (hasBaby) {
         long mother = inputStream.readLong();
         long father = inputStream.readLong();
         long traits = inputStream.readLong();
         byte daysLeft = inputStream.readByte();
         new Offspring(mother, father, traits, daysLeft, false);
      }

      long creatureId = inputStream.readLong();
      Creature animal = null;

      try {
         animal = new Creature(creatureId);
         animal.setName(inputStream.readUTF());
         animal.getStatus().template = CreatureTemplateFactory.getInstance().getTemplate(inputStream.readUTF());
         animal.template = animal.getStatus().template;
         animal.getStatus().setSex(inputStream.readByte());
         short centimetersHigh = inputStream.readShort();
         short centimetersLong = inputStream.readShort();
         short centimetersWide = inputStream.readShort();
         animal.getStatus().inventoryId = inputStream.readLong();
         animal.getStatus().bodyId = inputStream.readLong();
         animal.getStatus().body = BodyFactory.getBody(animal, animal.getStatus().template.getBodyType(), centimetersHigh, centimetersLong, centimetersWide);
         animal.getStatus().buildingId = inputStream.readLong();
         if (animal.getStatus().buildingId != -10L) {
            try {
               Structure struct = Structures.getStructure(animal.getStatus().buildingId);
               if (!struct.isFinalFinished()) {
                  animal.setStructure(struct);
               } else {
                  animal.getStatus().buildingId = -10L;
               }
            } catch (NoSuchStructureException var39) {
               animal.getStatus().buildingId = -10L;
               logger.log(Level.INFO, "Could not find structure for " + animal.name);
               animal.setStructure(null);
            }
         }

         animal.getStatus().stamina = inputStream.readShort();
         animal.getStatus().hunger = inputStream.readShort();
         animal.getStatus().nutrition = inputStream.readFloat();
         animal.getStatus().thirst = inputStream.readShort();
         animal.getStatus().dead = inputStream.readBoolean();
         animal.getStatus().stealth = inputStream.readBoolean();
         animal.getStatus().kingdom = inputStream.readByte();
         animal.getStatus().age = inputStream.readInt();
         animal.getStatus().lastPolledAge = inputStream.readLong();
         animal.getStatus().fat = inputStream.readByte();
         animal.getStatus().traits = inputStream.readLong();
         if (animal.getStatus().traits != 0L) {
            animal.getStatus().setTraitBits(animal.getStatus().traits);
         }

         animal.dominator = inputStream.readLong();
         animal.getStatus().mother = inputStream.readLong();
         animal.getStatus().father = inputStream.readLong();
         animal.getStatus().reborn = inputStream.readBoolean();
         animal.getStatus().loyalty = inputStream.readFloat();
         animal.getStatus().lastPolledLoyalty = inputStream.readLong();
         animal.getStatus().offline = inputStream.readBoolean();
         animal.getStatus().stayOnline = inputStream.readBoolean();
         animal.getStatus().detectInvisCounter = inputStream.readShort();
         animal.getStatus().disease = inputStream.readByte();
         animal.getStatus().lastGroomed = inputStream.readLong();
         long hitchedTo = inputStream.readLong();
         if (hitchedTo > 0L) {
            try {
               Item vehicle = Items.getItem(hitchedTo);
               Vehicle vehic = Vehicles.getVehicle(vehicle);
               if (vehic != null && vehic.addDragger(animal)) {
                  animal.setHitched(vehic, true);
                  Seat driverseat = vehic.getPilotSeat();
                  if (driverseat != null) {
                     float _r = (-vehicle.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                     float _s = (float)Math.sin((double)_r);
                     float _c = (float)Math.cos((double)_r);
                     float xo = _s * -driverseat.offx - _c * -driverseat.offy;
                     float yo = _c * -driverseat.offx + _s * -driverseat.offy;
                     float nPosX = animal.getStatus().getPositionX() - xo;
                     float nPosY = animal.getStatus().getPositionY() - yo;
                     float nPosZ = animal.getStatus().getPositionZ() - driverseat.offz;
                     animal.getStatus().setPositionX(nPosX);
                     animal.getStatus().setPositionY(nPosY);
                     animal.getStatus().setRotation(-vehicle.getRotation() + 180.0F);
                     animal.getMovementScheme()
                        .setPosition(
                           animal.getStatus().getPositionX(), animal.getStatus().getPositionY(), nPosZ, animal.getStatus().getRotation(), animal.getLayer()
                        );
                  }
               }
            } catch (NoSuchItemException var38) {
               logger.log(Level.WARNING, "Exception", (Throwable)var38);
            }
         }

         animal.getStatus().modtype = inputStream.readByte();
         animal.setPetName(inputStream.readUTF());
         animal.loadTemplate();
         Creatures.getInstance().addCreature(animal, false, false);
      } catch (Exception var40) {
         logger.log(Level.WARNING, "Exception", (Throwable)var40);
      }

      try {
         assert animal != null;

         animal.skills = SkillsFactory.createSkills(animal.getWurmId());
         animal.skills.clone(animal.template.getSkills().getSkills());
      } catch (Exception var37) {
         logger.log(Level.WARNING, "Exception", (Throwable)var37);
      }

      int numSkills = inputStream.readInt();

      try {
         for(int skillNo = 0; skillNo < numSkills; ++skillNo) {
            int curSkillNum = inputStream.readInt();
            double curSkillValue = inputStream.readDouble();
            double curSkillMinValue = inputStream.readDouble();
            long curSkillLastUsed = inputStream.readLong();
            inputStream.readLong();
            animal.skills.learn(curSkillNum, (float)curSkillMinValue, false);
            animal.skills.getSkill(curSkillNum).lastUsed = curSkillLastUsed;
            animal.skills.getSkill(curSkillNum).setKnowledge(curSkillValue, false);
         }
      } catch (Exception var41) {
         logger.log(Level.WARNING, "Exception", (Throwable)var41);
      }

      try {
         animal.getBody().createBodyParts();
      } catch (NoSuchTemplateException | FailedException var36) {
         logger.log(Level.WARNING, "Exception", (Throwable)var36);
      }

      try {
         animal.loadPossessions(animal.getStatus().inventoryId);
      } catch (Exception var35) {
         logger.log(Level.WARNING, "Exception", (Throwable)var35);
      }

      try {
         dbcon = DbConnector.getCreatureDbCon();
         ps = dbcon.prepareStatement(
            "insert into CREATURES (WURMID, NAME, TEMPLATENAME, SEX, CENTIMETERSHIGH, CENTIMETERSLONG, CENTIMETERSWIDE, INVENTORYID, BODYID, BUILDINGID, STAMINA, HUNGER, NUTRITION, THIRST, DEAD, STEALTH, KINGDOM, AGE, LASTPOLLEDAGE, FAT, TRAITS, DOMINATOR, MOTHER, FATHER, REBORN, LOYALTY, LASTPOLLEDLOYALTY, OFFLINE, STAYONLINE, DETECTIONSECS, DISEASE, LASTGROOMED, VEHICLE, TYPE, PETNAME) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
         );
         ps.setLong(1, animal.getWurmId());
         ps.setString(2, animal.name);
         ps.setString(3, animal.getTemplate().getName());
         ps.setByte(4, animal.getSex());
         ps.setShort(5, animal.getCentimetersHigh());
         ps.setShort(6, animal.getCentimetersLong());
         ps.setShort(7, animal.getCentimetersWide());
         ps.setLong(8, animal.getStatus().inventoryId);
         ps.setLong(9, animal.getBody().getId());
         ps.setLong(10, animal.getBuildingId());
         ps.setShort(11, (short)(animal.getStatus().getStamina() & 65535));
         ps.setShort(12, (short)(animal.getStatus().getHunger() & 65535));
         ps.setFloat(13, animal.getStatus().getNutritionlevel());
         ps.setShort(14, (short)(animal.getStatus().getThirst() & 65535));
         ps.setBoolean(15, animal.isDead());
         ps.setBoolean(16, animal.isStealth());
         ps.setByte(17, animal.getCurrentKingdom());
         ps.setInt(18, animal.getStatus().age);
         ps.setLong(19, animal.getStatus().lastPolledAge);
         ps.setByte(20, animal.getStatus().fat);
         ps.setLong(21, animal.getStatus().traits);
         ps.setLong(22, -10L);
         ps.setLong(23, animal.getMother());
         ps.setLong(24, animal.getFather());
         ps.setBoolean(25, animal.isReborn());
         ps.setFloat(26, animal.getLoyalty());
         ps.setLong(27, animal.getStatus().lastPolledLoyalty);
         ps.setBoolean(28, animal.isOffline());
         ps.setBoolean(29, animal.isStayonline());
         ps.setShort(30, (short)animal.getStatus().detectInvisCounter);
         ps.setByte(31, animal.getDisease());
         ps.setLong(32, animal.getLastGroomed());
         ps.setLong(33, animal.getVehicle());
         ps.setByte(34, animal.getStatus().modtype);
         ps.setString(35, animal.petName);
         ps.execute();
      } catch (SQLException var33) {
         logger.log(Level.WARNING, "Exception", (Throwable)var33);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      animal.getStatus().setStatusExists(true);
      inputStream.readInt();
      Items.loadAllItemsForCreature(animal, animal.getStatus().getInventoryId());
   }
}
