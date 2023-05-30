package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureMove;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.MovementEntity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;

public class MirroredSelf extends KarmaSpell {
   public MirroredSelf() {
      super("Mirrored Self", 562, 5, 500, 20, 1, 900000L);
      this.targetTile = true;
      this.targetItem = true;
      this.description = "creates deceptive illusions of yourself around you";
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      this.castMirroredSelf(performer, Math.max(10.0, power));
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Item target) {
      this.castMirroredSelf(performer, Math.max(10.0, power));
   }

   private void castMirroredSelf(Creature performer, double power) {
      int nums = 2 + (int)power / 10;
      int x = 0;
      int y = 0;

      for(int n = 0; n < nums; ++n) {
         MovementEntity entity = new MovementEntity(performer.getWurmId(), System.currentTimeMillis() + 1000L * Math.max(20L, (long)power));
         CreatureMove startPos = new CreatureMove();
         startPos.diffX = (float)((byte)(-1 + Server.rand.nextInt(2)));
         startPos.diffY = (float)((byte)(-1 + Server.rand.nextInt(2)));
         startPos.diffZ = 0.0F;
         entity.setMovePosition(startPos);
         performer.addIllusion(entity);
         VolaTile tile = Zones.getOrCreateTile(performer.getTileX() + 0, performer.getTileY() + 0, performer.isOnSurface());

         for(VirtualZone vz : tile.getWatchers()) {
            try {
               float posZ = Zones.calculatePosZ(
                  (float)((performer.getTileX() + 0) * 4),
                  (float)((performer.getTileY() + 0) * 4),
                  tile,
                  performer.isOnSurface(),
                  false,
                  performer.getPositionZ(),
                  performer,
                  -10L
               );
               float diffZ = performer.getPositionZ() - posZ;

               try {
                  vz.addCreature(performer.getWurmId(), false, entity.getWurmid(), 0.0F, 0.0F, diffZ);
               } catch (Exception var18) {
               }
            } catch (Exception var19) {
               logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
            }
         }
      }
   }
}
