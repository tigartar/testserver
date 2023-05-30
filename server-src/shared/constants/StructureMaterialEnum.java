package com.wurmonline.shared.constants;

import java.util.logging.Logger;

public enum StructureMaterialEnum {
   WOOD((byte)0, "wood"),
   STONE((byte)1, "stone"),
   METAL((byte)2, "metal"),
   TIMBER_FRAMED((byte)3, "timber framed"),
   PLAIN_STONE((byte)4, "plain stone"),
   SLATE((byte)5, "slate"),
   ROUNDED_STONE((byte)6, "rounded stone"),
   POTTERY((byte)7, "pottery"),
   SANDSTONE((byte)8, "sandstone"),
   RENDERED((byte)9, "rendered"),
   MARBLE((byte)10, "marble"),
   IRON((byte)11, "iron"),
   LOG((byte)12, "log"),
   CRUDE_WOOD((byte)13, "crude wood"),
   FLOWER1((byte)14, "flower"),
   FLOWER2((byte)15, "flower"),
   FLOWER3((byte)16, "flower"),
   FLOWER4((byte)17, "flower"),
   FLOWER5((byte)18, "flower"),
   FLOWER6((byte)19, "flower"),
   FLOWER7((byte)20, "flower"),
   ICE((byte)21, "ice"),
   FIRE((byte)22, "fire");

   public final byte material;
   public final String nameString;

   private StructureMaterialEnum(byte _material, String _nameString) {
      this.material = _material;
      this.nameString = _nameString;
   }

   public static StructureMaterialEnum getEnumByMaterial(byte material) {
      if (material >= 0 && material < values().length) {
         return values()[material];
      } else {
         Logger.getGlobal().warning("Reached default return value for material=" + material);
         return WOOD;
      }
   }
}
