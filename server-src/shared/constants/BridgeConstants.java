package com.wurmonline.shared.constants;

import java.util.Locale;

public interface BridgeConstants {
   public static enum BridgeMaterial {
      ROPE((byte)1, "rope", "Rope", 621),
      BRICK((byte)2, "stone", "Stone brick", 60, 9),
      MARBLE((byte)3, "marble", "Marble brick", 60, 9),
      WOOD((byte)4, "wood", "Wood", 60, 6),
      SLATE((byte)5, "slate", "Slate brick", 60, 9),
      ROUNDED_STONE((byte)6, "roundedstone", "Rounded stone", 60, 9),
      POTTERY((byte)7, "pottery", "Pottery brick", 60, 9),
      SANDSTONE((byte)8, "sandstone", "Sandstone brick", 60, 9),
      RENDERED((byte)9, "rendered", "Rendered brick", 60, 9);

      private final byte material;
      private final String texture;
      private final int supportExtensionOffset;
      private final String name;
      private final int icon;
      private static final BridgeConstants.BridgeMaterial[] types = values();

      private BridgeMaterial(byte newMaterial, String newTexture, String newName, int newIcon, int newSupportExtensionOffset) {
         this.material = newMaterial;
         this.texture = newTexture;
         this.supportExtensionOffset = newSupportExtensionOffset;
         this.name = newName;
         this.icon = newIcon;
      }

      private BridgeMaterial(byte newMaterial, String newTexture, String newName, int newIcon) {
         this(newMaterial, newTexture, newName, newIcon, 0);
      }

      public byte getCode() {
         return this.material;
      }

      public String getTextureName() {
         return this.texture;
      }

      public final float getExtensionOffset() {
         return (float)this.supportExtensionOffset;
      }

      private final int getIcon() {
         return this.icon;
      }

      public static BridgeConstants.BridgeMaterial fromByte(byte typeByte) {
         for(int i = 0; i < types.length; ++i) {
            if (typeByte == types[i].getCode()) {
               return types[i];
            }
         }

         return null;
      }

      public final String getName() {
         return this.name;
      }

      public static final String getTextureName(BridgeConstants.BridgeType type, BridgeConstants.BridgeMaterial material) {
         return "img.texture.floor." + type.getTextureName() + "." + material.getTextureName().replace(" ", "");
      }
   }

   public static enum BridgeState {
      PLANNED((byte)-1, false, ""),
      STAGE1((byte)0, true, "first "),
      STAGE2((byte)1, true, "second "),
      STAGE3((byte)2, true, "third "),
      STAGE4((byte)3, true, "fourth "),
      STAGE5((byte)4, true, "fifth "),
      STAGE6((byte)5, true, "sixth "),
      STAGE7((byte)6, true, "seventh "),
      COMPLETED((byte)127, false, "");

      private byte state;
      private boolean beingBuilt;
      private String desc;
      private static final BridgeConstants.BridgeState[] types = values();

      private BridgeState(byte newState, boolean newBeingBuilt, String description) {
         this.state = newState;
         this.beingBuilt = newBeingBuilt;
         this.desc = description;
      }

      public byte getCode() {
         return this.state;
      }

      public boolean isBeingBuilt() {
         return this.beingBuilt;
      }

      public String getDescription() {
         return this.desc;
      }

      public static BridgeConstants.BridgeState fromByte(byte bridgeStateByte) {
         for(int i = 0; i < types.length; ++i) {
            if (bridgeStateByte == types[i].getCode()) {
               return types[i];
            }
         }

         return PLANNED;
      }
   }

   public static enum BridgeType {
      ABUTMENT_NARROW(0, "abutment.narrow", "abutment"),
      BRACING_NARROW(1, "bracing.narrow", "bracing"),
      CROWN_NARROW(2, "crown.narrow", "crown"),
      DOUBLE_NARROW(3, "double.narrow", "double bracing"),
      END_NARROW(4, "end.narrow", "double abutment"),
      FLOATING_NARROW(5, "floating.narrow", "floating"),
      SUPPORT_NARROW(6, "support.narrow", "support", "extension.narrow"),
      ABUTMENT_CENTER(7, "abutment.center", "abutment"),
      BRACING_CENTER(8, "bracing.center", "bracing"),
      CROWN_CENTER(9, "crown.center", "crown"),
      DOUBLE_CENTER(10, "double.center", "double bracing"),
      END_CENTER(11, "end.center", "double abutment"),
      FLOATING_CENTER(12, "floating.center", "floating"),
      SUPPORT_CENTER(13, "support.center", "support", "extension.center"),
      ABUTMENT_LEFT(14, "abutment.left", "abutment"),
      ABUTMENT_RIGHT(15, "abutment.right", "abutment"),
      BRACING_LEFT(16, "bracing.left", "bracing"),
      BRACING_RIGHT(17, "bracing.right", "bracing"),
      CROWN_SIDE(18, "crown.side", "crown"),
      DOUBLE_SIDE(19, "double.side", "double bracing"),
      END_SIDE(20, "end.side", "double abutment"),
      FLOATING_SIDE(21, "floating.side", "floating"),
      SUPPORT_SIDE(22, "support.side", "support", "extension.side");

      private final byte type;
      private final String texture;
      private final String extensionTexture;
      private final String name;
      private final boolean isNarrow;
      private final boolean isSide;
      private final boolean isLeft;
      private final boolean isRight;
      private final boolean isCenter;
      private final boolean isAbutment;
      private final boolean isBracing;
      private final boolean isCrown;
      private final boolean isFloating;
      private final boolean isEnd;
      private final boolean isDouble;
      private static final BridgeConstants.BridgeType[] types = values();

      private BridgeType(int newType, String newTexture, String newName, String newExtensionTexture) {
         this.type = (byte)newType;
         this.texture = newTexture;
         this.extensionTexture = newExtensionTexture;
         this.name = newName;
         this.isNarrow = this.texture.contains(".narrow");
         this.isSide = this.texture.contains(".side");
         this.isLeft = this.texture.contains(".left");
         this.isRight = this.texture.contains(".right");
         this.isCenter = this.texture.contains(".center");
         this.isAbutment = this.texture.startsWith("abutment.");
         this.isBracing = this.texture.startsWith("bracing.");
         this.isCrown = this.texture.startsWith("crown.");
         this.isFloating = this.texture.startsWith("floating.");
         this.isEnd = this.texture.startsWith("end.");
         this.isDouble = this.texture.startsWith("double.");
      }

      private BridgeType(int newType, String newTexture, String newName) {
         this(newType, newTexture, newName, "");
      }

      public byte getCode() {
         return this.type;
      }

      public static BridgeConstants.BridgeType fromByte(byte typeByte) {
         for(int i = 0; i < types.length; ++i) {
            if (typeByte == types[i].getCode()) {
               return types[i];
            }
         }

         return null;
      }

      public final String getTextureName() {
         return this.texture;
      }

      public final String getExtensionTextureName() {
         return this.extensionTexture;
      }

      public final String getName() {
         return this.name.toLowerCase(Locale.ENGLISH);
      }

      public final boolean isSupportType() {
         return this.extensionTexture.length() > 0;
      }

      public final boolean isNarrow() {
         return this.isNarrow;
      }

      public final boolean isSide() {
         return this.isSide;
      }

      public final boolean isLeft() {
         return this.isLeft;
      }

      public final boolean isRight() {
         return this.isRight;
      }

      public final int wallCount() {
         if (this.isNarrow()) {
            return 2;
         } else {
            return !this.isLeft() && !this.isRight() && !this.isSide() ? 0 : 1;
         }
      }

      public final boolean isCenter() {
         return this.isCenter;
      }

      public final boolean isAbutment() {
         return this.isAbutment;
      }

      public final boolean isBracing() {
         return this.isBracing;
      }

      public final boolean isCrown() {
         return this.isCrown;
      }

      public final boolean isFloating() {
         return this.isFloating;
      }

      public final boolean isDoubleAbutment() {
         return this.isEnd;
      }

      public final boolean isDoubleBracing() {
         return this.isDouble;
      }

      public static final String getModelName(BridgeConstants.BridgeType type, BridgeConstants.BridgeMaterial material, BridgeConstants.BridgeState state) {
         String plan = "";
         if (state == BridgeConstants.BridgeState.PLANNED) {
            plan = ".plan";
         }

         if (state.isBeingBuilt()) {
            plan = ".build";
         }

         return "model.structure.bridge" + plan + "." + type.getTextureName() + "." + material.getTextureName().replace(" ", "");
      }

      public static final int getIconId(BridgeConstants.BridgeType type, BridgeConstants.BridgeMaterial material, BridgeConstants.BridgeState state) {
         switch(material) {
            case WOOD:
               switch(type) {
                  case ABUTMENT_NARROW:
                  case ABUTMENT_LEFT:
                  case ABUTMENT_RIGHT:
                     return 440;
                  case CROWN_NARROW:
                  case CROWN_SIDE:
                     return 441;
                  case SUPPORT_NARROW:
                  case SUPPORT_SIDE:
                     return 442;
                  default:
                     return 60;
               }
            case BRICK:
               switch(type) {
                  case ABUTMENT_NARROW:
                  case ABUTMENT_LEFT:
                  case ABUTMENT_RIGHT:
                  case ABUTMENT_CENTER:
                     return 443;
                  case CROWN_NARROW:
                  case CROWN_SIDE:
                  case CROWN_CENTER:
                     return 445;
                  case SUPPORT_NARROW:
                  case SUPPORT_SIDE:
                  case SUPPORT_CENTER:
                     return 449;
                  case BRACING_NARROW:
                  case BRACING_LEFT:
                  case BRACING_RIGHT:
                  case BRACING_CENTER:
                     return 444;
                  case DOUBLE_NARROW:
                  case DOUBLE_SIDE:
                  case DOUBLE_CENTER:
                     return 446;
                  case END_NARROW:
                  case END_SIDE:
                  case END_CENTER:
                     return 447;
                  case FLOATING_NARROW:
                  case FLOATING_SIDE:
                  case FLOATING_CENTER:
                     return 448;
                  default:
                     return 60;
               }
            case MARBLE:
               switch(type) {
                  case ABUTMENT_NARROW:
                  case ABUTMENT_LEFT:
                  case ABUTMENT_RIGHT:
                  case ABUTMENT_CENTER:
                     return 450;
                  case CROWN_NARROW:
                  case CROWN_SIDE:
                  case CROWN_CENTER:
                     return 452;
                  case SUPPORT_NARROW:
                  case SUPPORT_SIDE:
                  case SUPPORT_CENTER:
                     return 456;
                  case BRACING_NARROW:
                  case BRACING_LEFT:
                  case BRACING_RIGHT:
                  case BRACING_CENTER:
                     return 451;
                  case DOUBLE_NARROW:
                  case DOUBLE_SIDE:
                  case DOUBLE_CENTER:
                     return 453;
                  case END_NARROW:
                  case END_SIDE:
                  case END_CENTER:
                     return 454;
                  case FLOATING_NARROW:
                  case FLOATING_SIDE:
                  case FLOATING_CENTER:
                     return 455;
                  default:
                     return 60;
               }
            case SLATE:
               switch(type) {
                  case ABUTMENT_NARROW:
                  case ABUTMENT_LEFT:
                  case ABUTMENT_RIGHT:
                  case ABUTMENT_CENTER:
                     return 430;
                  case CROWN_NARROW:
                  case CROWN_SIDE:
                  case CROWN_CENTER:
                     return 432;
                  case SUPPORT_NARROW:
                  case SUPPORT_SIDE:
                  case SUPPORT_CENTER:
                     return 436;
                  case BRACING_NARROW:
                  case BRACING_LEFT:
                  case BRACING_RIGHT:
                  case BRACING_CENTER:
                     return 431;
                  case DOUBLE_NARROW:
                  case DOUBLE_SIDE:
                  case DOUBLE_CENTER:
                     return 433;
                  case END_NARROW:
                  case END_SIDE:
                  case END_CENTER:
                     return 434;
                  case FLOATING_NARROW:
                  case FLOATING_SIDE:
                  case FLOATING_CENTER:
                     return 435;
                  default:
                     return 60;
               }
            case ROUNDED_STONE:
               switch(type) {
                  case ABUTMENT_NARROW:
                  case ABUTMENT_LEFT:
                  case ABUTMENT_RIGHT:
                  case ABUTMENT_CENTER:
                     return 410;
                  case CROWN_NARROW:
                  case CROWN_SIDE:
                  case CROWN_CENTER:
                     return 412;
                  case SUPPORT_NARROW:
                  case SUPPORT_SIDE:
                  case SUPPORT_CENTER:
                     return 416;
                  case BRACING_NARROW:
                  case BRACING_LEFT:
                  case BRACING_RIGHT:
                  case BRACING_CENTER:
                     return 411;
                  case DOUBLE_NARROW:
                  case DOUBLE_SIDE:
                  case DOUBLE_CENTER:
                     return 413;
                  case END_NARROW:
                  case END_SIDE:
                  case END_CENTER:
                     return 414;
                  case FLOATING_NARROW:
                  case FLOATING_SIDE:
                  case FLOATING_CENTER:
                     return 415;
                  default:
                     return 60;
               }
            case POTTERY:
               switch(type) {
                  case ABUTMENT_NARROW:
                  case ABUTMENT_LEFT:
                  case ABUTMENT_RIGHT:
                  case ABUTMENT_CENTER:
                     return 390;
                  case CROWN_NARROW:
                  case CROWN_SIDE:
                  case CROWN_CENTER:
                     return 392;
                  case SUPPORT_NARROW:
                  case SUPPORT_SIDE:
                  case SUPPORT_CENTER:
                     return 396;
                  case BRACING_NARROW:
                  case BRACING_LEFT:
                  case BRACING_RIGHT:
                  case BRACING_CENTER:
                     return 391;
                  case DOUBLE_NARROW:
                  case DOUBLE_SIDE:
                  case DOUBLE_CENTER:
                     return 393;
                  case END_NARROW:
                  case END_SIDE:
                  case END_CENTER:
                     return 394;
                  case FLOATING_NARROW:
                  case FLOATING_SIDE:
                  case FLOATING_CENTER:
                     return 395;
                  default:
                     return 60;
               }
            case SANDSTONE:
               switch(type) {
                  case ABUTMENT_NARROW:
                  case ABUTMENT_LEFT:
                  case ABUTMENT_RIGHT:
                  case ABUTMENT_CENTER:
                     return 370;
                  case CROWN_NARROW:
                  case CROWN_SIDE:
                  case CROWN_CENTER:
                     return 372;
                  case SUPPORT_NARROW:
                  case SUPPORT_SIDE:
                  case SUPPORT_CENTER:
                     return 376;
                  case BRACING_NARROW:
                  case BRACING_LEFT:
                  case BRACING_RIGHT:
                  case BRACING_CENTER:
                     return 371;
                  case DOUBLE_NARROW:
                  case DOUBLE_SIDE:
                  case DOUBLE_CENTER:
                     return 373;
                  case END_NARROW:
                  case END_SIDE:
                  case END_CENTER:
                     return 374;
                  case FLOATING_NARROW:
                  case FLOATING_SIDE:
                  case FLOATING_CENTER:
                     return 375;
                  default:
                     return 60;
               }
            case RENDERED:
               switch(type) {
                  case ABUTMENT_NARROW:
                  case ABUTMENT_LEFT:
                  case ABUTMENT_RIGHT:
                  case ABUTMENT_CENTER:
                     return 350;
                  case CROWN_NARROW:
                  case CROWN_SIDE:
                  case CROWN_CENTER:
                     return 352;
                  case SUPPORT_NARROW:
                  case SUPPORT_SIDE:
                  case SUPPORT_CENTER:
                     return 356;
                  case BRACING_NARROW:
                  case BRACING_LEFT:
                  case BRACING_RIGHT:
                  case BRACING_CENTER:
                     return 351;
                  case DOUBLE_NARROW:
                  case DOUBLE_SIDE:
                  case DOUBLE_CENTER:
                     return 353;
                  case END_NARROW:
                  case END_SIDE:
                  case END_CENTER:
                     return 354;
                  case FLOATING_NARROW:
                  case FLOATING_SIDE:
                  case FLOATING_CENTER:
                     return 355;
                  default:
                     return 60;
               }
            case ROPE:
               switch(type) {
                  case ABUTMENT_NARROW:
                     return 457;
                  case CROWN_NARROW:
                     return 458;
                  case END_NARROW:
                     return 459;
                  default:
                     return 60;
               }
            default:
               return 60;
         }
      }

      public static final String getExtensionModelName(
         BridgeConstants.BridgeType type, BridgeConstants.BridgeMaterial material, BridgeConstants.BridgeState state
      ) {
         String modelName = "";
         if (type.isSupportType()) {
            String plan = "";
            if (state == BridgeConstants.BridgeState.PLANNED) {
               plan = ".plan";
            }

            if (state.isBeingBuilt()) {
               plan = ".build";
            }

            modelName = "model.structure.bridge" + plan + "." + type.getExtensionTextureName() + "." + material.getTextureName().replace(" ", "");
         }

         return modelName;
      }
   }
}
