package com.wurmonline.server.bodys;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

final class BodySpider extends BodyTemplate {
   BodySpider() {
      super((byte)8);
      this.leftOverArmS = "left foreleg";
      this.rightOverArmS = "right foreleg";
      this.leftThighS = "left hindleg";
      this.rightThighS = "right hindleg";
      this.leftUnderArmS = "left foreleg";
      this.rightUnderArmS = "right foreleg ";
      this.leftCalfS = "left hindleg";
      this.rightCalfS = "right hindleg";
      this.leftHandS = "left leg";
      this.rightHandS = "right leg";
      this.leftFootS = "left leg";
      this.rightFootS = "right leg";
      this.leftArmS = "left foreleg";
      this.rightArmS = "right foreleg";
      this.leftLegS = "left hindleg";
      this.rightLegS = "right hindleg";
      this.leftEyeS = "eyes";
      this.rightEyeS = "eyes";
      this.baseOfNoseS = "mandibles";
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
   void buildBody(Item[] spaces, Creature owner) {
      spaces[0].setOwner(owner.getWurmId(), true);
      spaces[0].insertItem(spaces[1]);
      spaces[1].insertItem(spaces[29]);
      spaces[0].insertItem(spaces[2]);
      spaces[2].insertItem(spaces[34]);
      spaces[34].insertItem(spaces[15]);
      spaces[34].insertItem(spaces[16]);
      spaces[34].insertItem(spaces[3]);
      spaces[34].insertItem(spaces[4]);
      spaces[34].insertItem(spaces[13]);
      spaces[34].insertItem(spaces[14]);
      spaces[34].insertItem(spaces[30]);
      spaces[34].insertItem(spaces[31]);
   }
}
