package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.CropTilePoller;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.LinkedList;
import java.util.List;

final class TileDirtBehaviour extends TileBehaviour {
   private static final short MAX_WATER_CROP_DEPTH = -4;

   TileDirtBehaviour() {
      super((short)15);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, int tilex, int tiley, boolean onSurface, int tile) {
      List<ActionEntry> toReturn = new LinkedList<>();
      List<ActionEntry> nature = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, subject, tilex, tiley, onSurface, tile));
      byte type = Tiles.decodeType(tile);
      byte data = Tiles.decodeData(tile);
      if (subject.isSeed()) {
         toReturn.add(Actions.actionEntrys[153]);
      } else if (subject.getTemplateId() == 495) {
         toReturn.add(new ActionEntry((short)-2, "Lay boards", "paving", emptyIntArr));
         toReturn.add(new ActionEntry((short)155, "Over dirt", "laying", new int[]{43}));
         toReturn.add(new ActionEntry((short)576, "In nearest corner", "laying", new int[]{43}));
      } else if (subject.getTemplateId() == 526) {
         toReturn.add(Actions.actionEntrys[118]);
      }

      if (subject.getTemplateId() == 266) {
         nature.add(Actions.actionEntrys[186]);
         nature.add(Actions.actionEntrys[660]);
      } else if (subject.isFlower()) {
         toReturn.add(new ActionEntry((short)186, "Plant Flowers", "planting"));
      } else if (subject.isNaturePlantable()) {
         toReturn.add(new ActionEntry((short)186, "Plant", "planting"));
      }

      if (Server.hasGrubs(tilex, tiley)) {
         nature.add(new ActionEntry((short)935, "Search for wurms", "searching"));
      }

      toReturn.addAll(this.getNatureMenu(performer, tilex, tiley, type, data, nature));
      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, int tile) {
      List<ActionEntry> toReturn = new LinkedList<>();
      List<ActionEntry> nature = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, tile));
      byte type = Tiles.decodeType(tile);
      byte data = Tiles.decodeData(tile);
      if (Server.hasGrubs(tilex, tiley)) {
         nature.add(new ActionEntry((short)935, "Search for wurms", "searching"));
      }

      toReturn.addAll(this.getNatureMenu(performer, tilex, tiley, type, data, nature));
      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, int tile, short action, float counter) {
      boolean done = true;
      if (action == 1) {
         performer.getCommunicator().sendNormalServerMessage("You see a patch of brown dirt, good for growing crops on.");
         sendVillageString(performer, tilex, tiley, true);
      } else if (action == 935) {
         done = Terraforming.pickWurms(act, performer, tilex, tiley, tile, counter);
      } else {
         done = super.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
      }

      return done;
   }

   @Override
   public boolean action(
      Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short action, float counter
   ) {
      boolean done = true;
      if (action == 153 && source.isSeed()) {
         if (Tiles.decodeType(tile) != Tiles.Tile.TILE_DIRT.id) {
            performer.getCommunicator().sendNormalServerMessage("The ground must be loosened dirt for any seeds to grow.", (byte)3);
            return true;
         }

         if (source.isProcessedFood()) {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " is processed and cannot be planted.");
            return true;
         }

         VolaTile vtile = Zones.getTileOrNull(tilex, tiley, onSurface);
         if (vtile != null && vtile.getStructure() != null) {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " would never grow inside.", (byte)3);
            return true;
         }

         try {
            if (!Terraforming.isFlat(tilex, tiley, onSurface, 4)) {
               performer.getCommunicator().sendNormalServerMessage("The ground is not flat enough for crops to grow. You need to flatten it first.", (byte)3);
               return true;
            }

            boolean isUnderWater = Terraforming.isCornerUnderWater(tilex, tiley, onSurface);
            boolean isWaterPlant = source.getTemplateId() == 744 || source.getTemplateId() == 746;
            if (isUnderWater && !isWaterPlant) {
               performer.getCommunicator().sendNormalServerMessage("The water is too deep. You cannot sow that crop there.", (byte)3);
               return true;
            }

            if (isWaterPlant && !Terraforming.isAllCornersInsideHeightRange(tilex, tiley, onSurface, (short)-1, (short)-4)) {
               performer.getCommunicator().sendNormalServerMessage("This type of crop can only grow in water of suitable depth.", (byte)3);
               return true;
            }
         } catch (IllegalArgumentException var19) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep. You cannot sow that crop there.", (byte)3);
            return true;
         }

         done = false;
         if (counter == 1.0F) {
            performer.getCommunicator().sendNormalServerMessage("You start sowing the seeds.");
            Server.getInstance().broadCastAction(performer.getName() + " starts sowing some seeds.", performer, 5);
            Skill farming = performer.getSkills().getSkillOrLearn(10049);
            short time = (short)((int)(130.0 - farming.getKnowledge(source, 0.0) - (double)act.getRarity() - (double)source.getRarity()));
            performer.sendActionControl(Actions.actionEntrys[153].getVerbString(), true, time);
            act.setTimeLeft(time);
         } else if (counter > (float)(act.getTimeLeft() / 10)) {
            if (act.getRarity() != 0) {
               performer.playPersonalSound("sound.fx.drumroll");
            }

            performer.getStatus().modifyStamina(-2000.0F);
            done = true;
            int crop = Crops.getNumber(source.getTemplateId());
            byte type = Tiles.decodeType(tile);
            if (type == Tiles.Tile.TILE_DIRT.id) {
               if (onSurface) {
                  Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Crops.getTileType(crop), Crops.encodeFieldData(true, 0, crop));
                  double diff = Crops.getDifficultyFor(crop);
                  Skill farming = performer.getSkills().getSkillOrLearn(10049);
                  farming.skillCheck(diff, 0.0, false, 1.0F);
                  Players.getInstance().sendChangedTile(tilex, tiley, onSurface, false);
                  int resource = (int)(
                     100.0 - farming.getKnowledge(0.0) + (double)source.getQualityLevel() + (double)(source.getRarity() * 20) + (double)(act.getRarity() * 50)
                  );
                  Server.setWorldResource(tilex, tiley, resource);
                  CropTilePoller.addCropTile(tile, tilex, tiley, crop, onSurface);
                  performer.achievement(523);
               } else {
                  Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Crops.getTileType(crop), Crops.encodeFieldData(true, 0, crop)));
                  Players.getInstance().sendChangedTile(tilex, tiley, onSurface, false);
                  CropTilePoller.addCropTile(tile, tilex, tiley, crop, onSurface);
               }

               performer.getCommunicator().sendNormalServerMessage("You sow the " + Crops.getCropName(crop) + ".");
               Server.getInstance().broadCastAction(performer.getName() + " sows some seeds.", performer, 5);
            } else {
               performer.getCommunicator().sendNormalServerMessage("You sow the seeds, but the field is already sown and it will have little effect.");
               Server.getInstance().broadCastAction(performer.getName() + " sows some seeds.", performer, 5);
            }

            Items.destroyItem(source.getWurmId());
         }
      } else if (action == 186) {
         if (Tiles.decodeType(tile) != Tiles.Tile.TILE_DIRT.id) {
            performer.getCommunicator().sendNormalServerMessage("The ground must be dirt for any plants to grow.", (byte)3);
            return true;
         }

         if (source.isNaturePlantable()) {
            done = Terraforming.plantFlower(performer, source, tilex, tiley, onSurface, tile, counter);
         } else {
            done = Terraforming.plantSprout(performer, source, tilex, tiley, onSurface, tile, counter, false);
         }
      } else if (action == 660) {
         if (Tiles.decodeType(tile) != Tiles.Tile.TILE_DIRT.id) {
            performer.getCommunicator().sendNormalServerMessage("The ground must be dirt for any plants to grow.", (byte)3);
            return true;
         }

         done = Terraforming.plantSprout(performer, source, tilex, tiley, onSurface, tile, counter, true);
      } else if (action == 1 || action == 152) {
         done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
      } else if (action != 155 && action != 576) {
         if (action == 118 && source.getTemplateId() == 526) {
            performer.getCommunicator().sendNormalServerMessage("You draw a circle in the air in front of you with " + source.getNameWithGenus() + ".");
            Server.getInstance()
               .broadCastAction(
                  performer.getName() + " draws a circle in the air in front of " + performer.getHimHerItString() + " with " + source.getNameWithGenus() + ".",
                  performer,
                  5
               );
            done = true;
            if (source.getAuxData() > 0) {
               Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, (byte)(Server.rand.nextInt(7) + 1));
               Players.getInstance().sendChangedTile(tilex, tiley, onSurface, true);
               source.setAuxData((byte)(source.getAuxData() - 1));
            } else {
               performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
            }
         } else {
            done = super.action(act, performer, source, tilex, tiley, onSurface, heightOffset, tile, action, counter);
         }
      } else {
         done = Terraforming.makeFloor(performer, source, tilex, tiley, onSurface, tile, counter);
      }

      return done;
   }
}
