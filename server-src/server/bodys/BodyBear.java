package com.wurmonline.server.bodys;

import com.wurmonline.server.Server;
import com.wurmonline.shared.exceptions.WurmServerException;

final class BodyBear extends BodyTemplate {
   BodyBear() {
      super((byte)2);
      this.leftHandS = "left paw";
      this.rightHandS = "right paw";
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
      } else if (rand < 80) {
         return 5;
      } else if (rand < 130) {
         return 6;
      } else if (rand < 180) {
         return 7;
      } else if (rand < 230) {
         return 8;
      } else if (rand < 280) {
         return 9;
      } else if (rand < 320) {
         return 10;
      } else if (rand < 370) {
         return 11;
      } else if (rand < 420) {
         return 12;
      } else if (rand < 460) {
         return 13;
      } else if (rand < 500) {
         return 14;
      } else if (rand < 540) {
         return 15;
      } else if (rand < 580) {
         return 16;
      } else if (rand < 600) {
         return 17;
      } else if (rand < 601) {
         return 18;
      } else if (rand < 602) {
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
}
