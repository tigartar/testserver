package com.wurmonline.server.bodys;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.shared.exceptions.WurmServerException;

final class BodyEttin extends BodyTemplate {
   BodyEttin() {
      super((byte)4);
      this.headS = "left head";
      this.secondHeadS = "right head";
      this.typeString = new String[]{
         this.bodyS,
         this.headS,
         this.torsoS,
         this.leftArmS,
         this.rightArmS,
         this.leftOverArmS,
         this.rightOverArmS,
         this.leftThighS,
         this.rightThighS,
         this.leftUnderArmS,
         this.rightUnderArmS,
         this.leftCalfS,
         this.rightCalfS,
         this.leftHandS,
         this.rightHandS,
         this.leftFootS,
         this.rightFootS,
         this.neckS,
         this.leftEyeS,
         this.rightEyeS,
         this.centerEyeS,
         this.chestS,
         this.topBackS,
         this.stomachS,
         this.lowerBackS,
         this.crotchS,
         this.leftShoulderS,
         this.rightShoulderS,
         this.secondHeadS,
         this.faceS,
         this.leftLegS,
         this.rightLegS,
         this.hipS,
         this.baseOfNoseS,
         this.legsS
      };
   }

   @Override
   public byte getRandomWoundPos() throws Exception {
      int rand = Server.rand.nextInt(1000);
      if (rand < 30) {
         return 1;
      } else if (rand < 60) {
         return 28;
      } else if (rand < 110) {
         return 5;
      } else if (rand < 160) {
         return 6;
      } else if (rand < 210) {
         return 7;
      } else if (rand < 260) {
         return 8;
      } else if (rand < 310) {
         return 9;
      } else if (rand < 360) {
         return 10;
      } else if (rand < 410) {
         return 11;
      } else if (rand < 460) {
         return 12;
      } else if (rand < 500) {
         return 13;
      } else if (rand < 540) {
         return 14;
      } else if (rand < 580) {
         return 15;
      } else if (rand < 620) {
         return 16;
      } else if (rand < 630) {
         return 17;
      } else if (rand < 631) {
         return 18;
      } else if (rand < 632) {
         return 19;
      } else if (rand < 730) {
         return 21;
      } else if (rand < 780) {
         return 22;
      } else if (rand < 830) {
         return 23;
      } else if (rand < 890) {
         return 24;
      } else if (rand < 900) {
         return 25;
      } else if (rand < 950) {
         return 26;
      } else if (rand < 1000) {
         return 27;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   @Override
   void buildBody(Item[] spaces, Creature owner) {
      spaces[0].setOwner(owner.getWurmId(), true);
      spaces[0].insertItem(spaces[1]);
      spaces[0].insertItem(spaces[28]);
      spaces[1].insertItem(spaces[29]);
      spaces[0].insertItem(spaces[2]);
      spaces[2].insertItem(spaces[3]);
      spaces[2].insertItem(spaces[4]);
      spaces[3].insertItem(spaces[13]);
      spaces[4].insertItem(spaces[14]);
      spaces[2].insertItem(spaces[34]);
      spaces[34].insertItem(spaces[15]);
      spaces[34].insertItem(spaces[16]);
   }
}
