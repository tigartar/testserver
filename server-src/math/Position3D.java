package com.wurmonline.math;

public final class Position3D {
   float x;
   float y;
   float z;

   public Position3D() {
   }

   public Position3D(Vertex v) {
      this.x = v.vertex[0];
      this.y = v.vertex[1];
      this.z = v.vertex[2];
   }

   public Position3D(Position3D p) {
      this.x = p.x;
      this.y = p.y;
      this.z = p.z;
   }

   public Position3D(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void sub(Position3D p) {
      this.x -= p.x;
      this.y -= p.y;
      this.z -= p.z;
   }

   public void sub(Vector v) {
      this.x -= v.vector[0];
      this.y -= v.vector[1];
      this.z -= v.vector[2];
   }

   public void sub(Vertex v1, Vector v2) {
      this.x -= v1.point[0];
      this.y -= v1.point[1];
      this.z -= v1.point[2];
      this.sub(v2);
   }

   public void set(Position3D p) {
      this.x = p.x;
      this.y = p.y;
      this.z = p.z;
   }

   public void scale(int i) {
      this.x *= (float)i;
      this.y *= (float)i;
      this.z *= (float)i;
   }
}
