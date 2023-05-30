package com.wurmonline.server.creatures;

import com.wurmonline.server.skills.Skills;

final class DbCreatureTemplate extends CreatureTemplate {
   DbCreatureTemplate(
      int aId,
      String aName,
      String aPlural,
      String aLongDesc,
      String aModelName,
      int[] aTypes,
      byte aBodyType,
      Skills aSkills,
      short aVision,
      byte aSex,
      short aCentimetersHigh,
      short aCentimetersLong,
      short aCentimetersWide,
      String aDeathSndMale,
      String aDeathSndFemale,
      String aHitSndMale,
      String aHitSndFemale,
      float aNaturalArmour,
      float aHandDam,
      float aKickDam,
      float aBiteDam,
      float aHeadDam,
      float aBreathDam,
      float aSpeed,
      int aMoveRate,
      int[] aItemsButchered,
      int aMaxHuntDist,
      int aAggress,
      byte meatMaterial
   ) {
      super(
         aId,
         aName,
         aPlural,
         aLongDesc,
         aModelName,
         aTypes,
         aBodyType,
         aSkills,
         aVision,
         aSex,
         aCentimetersHigh,
         aCentimetersLong,
         aCentimetersWide,
         aDeathSndMale,
         aDeathSndFemale,
         aHitSndMale,
         aHitSndFemale,
         aNaturalArmour,
         aHandDam,
         aKickDam,
         aBiteDam,
         aHeadDam,
         aBreathDam,
         aSpeed,
         aMoveRate,
         aItemsButchered,
         aMaxHuntDist,
         aAggress,
         meatMaterial
      );
   }
}
