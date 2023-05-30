package com.wurmonline.mesh;

public class Node {
   private boolean render;
   float[] normals = new float[3];
   private float height;
   private float x;
   private float y;
   private float bbBottom;
   private float bbHeight;
   private boolean visible;
   private byte texture;
   byte data;
   private Object object;

   Node() {
   }

   boolean isRender() {
      return this.render;
   }

   void setRender(boolean render) {
      this.render = render;
   }

   float[] getNormals() {
      return this.normals;
   }

   void setNormals(float[] normals) {
      this.normals = normals;
   }

   float getHeight() {
      return this.height;
   }

   void setHeight(float height) {
      this.height = height;
   }

   float getX() {
      return this.x;
   }

   void setX(float x) {
      this.x = x;
   }

   float getY() {
      return this.y;
   }

   void setY(float y) {
      this.y = y;
   }

   float getBbBottom() {
      return this.bbBottom;
   }

   void setBbBottom(float bbBottom) {
      this.bbBottom = bbBottom;
   }

   float getBbHeight() {
      return this.bbHeight;
   }

   void setBbHeight(float bbHeight) {
      this.bbHeight = bbHeight;
   }

   boolean isVisible() {
      return this.visible;
   }

   void setVisible(boolean visible) {
      this.visible = visible;
   }

   byte getTexture() {
      return this.texture;
   }

   void setTexture(byte texture) {
      this.texture = texture;
   }

   void setData(byte data) {
      this.data = data;
   }

   Object getObject() {
      return this.object;
   }

   void setObject(Object object) {
      this.object = object;
   }
}
