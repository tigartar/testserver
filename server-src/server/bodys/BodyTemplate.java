package com.wurmonline.server.bodys;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BodyTemplate {
   private static Logger logger = Logger.getLogger(BodyTemplate.class.getName());
   public static final byte TYPE_HUMAN = 0;
   public static final byte TYPE_HORSE = 1;
   public static final byte TYPE_BEAR = 2;
   public static final byte TYPE_DOG = 3;
   public static final byte TYPE_ETTIN = 4;
   public static final byte TYPE_CYCLOPS = 5;
   public static final byte TYPE_DRAGON = 6;
   public static final byte TYPE_BIRD = 7;
   public static final byte TYPE_SPIDER = 8;
   public static final byte TYPE_SNAKE = 9;
   byte type = 0;
   public static final byte body = 0;
   String bodyS = "body";
   public static final byte head = 1;
   String headS = "head";
   public static final byte torso = 2;
   String torsoS = "torso";
   public static final byte leftArm = 3;
   String leftArmS = "left arm";
   public static final byte rightArm = 4;
   String rightArmS = "right arm";
   public static final byte leftOverArm = 5;
   String leftOverArmS = "left upper arm";
   public static final byte rightOverArm = 6;
   String rightOverArmS = "right upper arm";
   public static final byte leftThigh = 7;
   String leftThighS = "left thigh";
   public static final byte rightThigh = 8;
   String rightThighS = "right thigh";
   public static final byte leftUnderArm = 9;
   String leftUnderArmS = "left underarm";
   public static final byte rightUnderArm = 10;
   String rightUnderArmS = "right underarm";
   public static final byte leftCalf = 11;
   String leftCalfS = "left calf";
   public static final byte rightCalf = 12;
   String rightCalfS = "right calf";
   public static final byte leftHand = 13;
   String leftHandS = "left hand";
   public static final byte rightHand = 14;
   String rightHandS = "right hand";
   public static final byte leftFoot = 15;
   String leftFootS = "left foot";
   public static final byte rightFoot = 16;
   String rightFootS = "right foot";
   public static final byte neck = 17;
   String neckS = "neck";
   public static final byte leftEye = 18;
   String leftEyeS = "left eye";
   public static final byte rightEye = 19;
   String rightEyeS = "right eye";
   public static final byte centerEye = 20;
   String centerEyeS = "center eye";
   public static final byte chest = 21;
   String chestS = "chest";
   public static final byte topBack = 22;
   String topBackS = "top of the back";
   public static final byte stomach = 23;
   String stomachS = "stomach";
   public static final byte lowerBack = 24;
   String lowerBackS = "lower back";
   public static final byte crotch = 25;
   String crotchS = "crotch";
   public static final byte leftShoulder = 26;
   String leftShoulderS = "left shoulder";
   public static final byte rightShoulder = 27;
   String rightShoulderS = "right shoulder";
   public static final byte secondHead = 28;
   String secondHeadS = "second head";
   public static final byte face = 29;
   String faceS = "face";
   public static final byte leftLeg = 30;
   String leftLegS = "left leg";
   public static final byte rightLeg = 31;
   String rightLegS = "right leg";
   public static final byte hip = 32;
   String hipS = "hip";
   public static final byte baseOfNose = 33;
   String baseOfNoseS = "baseOfNose";
   public static final byte legs = 34;
   String legsS = "legs";
   public static final byte tabardSlot = 35;
   public static final byte neckSlot = 36;
   public static final byte lHeldSlot = 37;
   public static final byte rHeldSlot = 38;
   public static final byte lRingSlot = 39;
   public static final byte rRingSlot = 40;
   public static final byte quiverSlot = 41;
   public static final byte backSlot = 42;
   public static final byte beltSlot = 43;
   public static final byte shieldSlot = 44;
   public static final byte capeSlot = 45;
   public static final byte lShoulderSlot = 46;
   public static final byte rShoulderSlot = 47;
   public static final byte inventory = 48;
   public String[] typeString = new String[]{
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

   BodyTemplate(byte aType) {
      this.type = aType;
   }

   void buildBody(Item[] spaces, Creature owner) {
      spaces[0].setOwner(owner.getWurmId(), true);
      spaces[0].insertItem(spaces[1]);
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

   byte getUpperLeftWoundPos() throws Exception {
      int rand = Server.rand.nextInt(100);
      if (rand < 3) {
         return 1;
      } else if (rand < 40) {
         return 5;
      } else if (rand < 50) {
         return 17;
      } else if (rand < 51) {
         return 18;
      } else if (rand < 60) {
         return 21;
      } else if (rand < 78) {
         return 22;
      } else if (rand < 100) {
         return 26;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   byte getUpperRightWoundPos() throws Exception {
      int rand = Server.rand.nextInt(100);
      if (rand < 3) {
         return 1;
      } else if (rand < 13) {
         return 17;
      } else if (rand < 50) {
         return 6;
      } else if (rand < 51) {
         return 19;
      } else if (rand < 63) {
         return 21;
      } else if (rand < 78) {
         return 22;
      } else if (rand < 100) {
         return 27;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   byte getHighWoundPos() throws Exception {
      int rand = Server.rand.nextInt(100);
      if (rand < 40) {
         return 1;
      } else if (rand < 60) {
         return 17;
      } else if (rand < 61) {
         return 18;
      } else if (rand < 62) {
         return 19;
      } else if (rand < 81) {
         return 26;
      } else if (rand < 100) {
         return 27;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   byte getMidLeftWoundPos() throws Exception {
      int rand = Server.rand.nextInt(100);
      if (rand < 18) {
         return 5;
      } else if (rand < 48) {
         return 7;
      } else if (rand < 58) {
         return 9;
      } else if (rand < 66) {
         return 13;
      } else if (rand < 73) {
         return 21;
      } else if (rand < 83) {
         return 23;
      } else if (rand < 100) {
         return 24;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   public byte getCenterWoundPos() throws Exception {
      int rand = Server.rand.nextInt(100);
      if (rand < 11) {
         return 7;
      } else if (rand < 22) {
         return 8;
      } else if (rand < 32) {
         return 9;
      } else if (rand < 42) {
         return 10;
      } else if (rand < 46) {
         return 13;
      } else if (rand < 50) {
         return 14;
      } else if (rand < 73) {
         return 21;
      } else if (rand < 100) {
         return 23;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   byte getMidRightWoundPos() throws Exception {
      int rand = Server.rand.nextInt(100);
      if (rand < 18) {
         return 6;
      } else if (rand < 48) {
         return 8;
      } else if (rand < 58) {
         return 10;
      } else if (rand < 66) {
         return 14;
      } else if (rand < 73) {
         return 21;
      } else if (rand < 83) {
         return 23;
      } else if (rand < 100) {
         return 24;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   byte getLowerLeftWoundPos() throws Exception {
      int rand = Server.rand.nextInt(100);
      if (rand < 48) {
         return 7;
      } else if (rand < 58) {
         return 9;
      } else if (rand < 78) {
         return 11;
      } else if (rand < 98) {
         return 15;
      } else if (rand < 100) {
         return 25;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   byte getLowWoundPos() throws Exception {
      int rand = Server.rand.nextInt(100);
      if (rand < 10) {
         return 7;
      } else if (rand < 20) {
         return 8;
      } else if (rand < 40) {
         return 11;
      } else if (rand < 60) {
         return 12;
      } else if (rand < 75) {
         return 15;
      } else if (rand < 90) {
         return 16;
      } else if (rand < 100) {
         return 25;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   byte getLowerRightWoundPos() throws Exception {
      int rand = Server.rand.nextInt(100);
      if (rand < 48) {
         return 8;
      } else if (rand < 58) {
         return 10;
      } else if (rand < 78) {
         return 12;
      } else if (rand < 98) {
         return 16;
      } else if (rand < 100) {
         return 25;
      } else {
         throw new WurmServerException("Bad randomizer");
      }
   }

   public final String getBodyPositionDescription(byte position) {
      String lDescription;
      if (position >= 0 && position <= this.typeString.length) {
         lDescription = this.typeString[position];
      } else {
         lDescription = "Unknown position-" + position;
      }

      return lDescription;
   }

   public static byte convertToArmorEquipementSlot(byte bodyPart) {
      byte toReturn = -1;
      switch(bodyPart) {
         case 0:
         case 2:
            toReturn = 3;
            break;
         case 1:
         case 28:
            toReturn = 2;
            break;
         case 3:
            toReturn = 5;
            break;
         case 4:
            toReturn = 6;
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 30:
         case 31:
         case 32:
         case 33:
         case 35:
         case 36:
         case 37:
         case 38:
         case 41:
         case 44:
         case 45:
         default:
            break;
         case 13:
            toReturn = 7;
            break;
         case 14:
            toReturn = 8;
            break;
         case 15:
            toReturn = 9;
            break;
         case 16:
            toReturn = 10;
            break;
         case 29:
            toReturn = 25;
            break;
         case 34:
            toReturn = 4;
            break;
         case 39:
            toReturn = 17;
            break;
         case 40:
            toReturn = 16;
            break;
         case 42:
            toReturn = 20;
            break;
         case 43:
            toReturn = 22;
            break;
         case 46:
            toReturn = 18;
            break;
         case 47:
            toReturn = 19;
      }

      if (toReturn == -1) {
         logger.log(Level.FINEST, "Could not convert BodyTemplate bodypart to Equipementpart, Constant number: " + bodyPart);
      }

      return toReturn;
   }

   public static byte convertToItemEquipementSlot(byte bodyPart) {
      switch(bodyPart) {
         case 0:
         case 2:
            return 12;
         case 1:
            return 2;
         case 3:
            return 26;
         case 4:
            return 27;
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 30:
         case 31:
         case 32:
         case 33:
         case 45:
         default:
            logger.log(Level.FINEST, "Could not convert BodyTemplate bodypart to Equipementpart, Constant number: " + bodyPart);
            return -1;
         case 13:
            return 7;
         case 14:
            return 8;
         case 15:
            return 9;
         case 16:
            return 10;
         case 29:
            return 25;
         case 34:
            return 13;
         case 35:
            return 15;
         case 36:
            return 21;
         case 37:
            return 0;
         case 38:
            return 1;
         case 39:
            return 17;
         case 40:
            return 16;
         case 41:
            return 23;
         case 42:
            return 20;
         case 43:
            return 22;
         case 44:
            return 11;
         case 46:
            return 18;
         case 47:
            return 19;
      }
   }
}
