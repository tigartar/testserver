package com.wurmonline.server.gui.propertysheet;

enum ServerSize {
   SIZE_16(16),
   SIZE_32(32),
   SIZE_64(64),
   SIZE_128(128),
   SIZE_256(256),
   SIZE_512(512),
   SIZE_1024(1024),
   SIZE_2048(2048),
   SIZE_4096(4096),
   SIZE_8192(8192),
   SIZE_16384(16384);

   private final int size;

   private ServerSize(int size) {
      this.size = size;
   }

   public int getSize() {
      return this.size;
   }

   @Override
   public String toString() {
      return String.valueOf(this.size);
   }
}
