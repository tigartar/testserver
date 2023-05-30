package com.wurmonline.math;

public final class MyVector {
   private float[] mVector = new float[4];

   MyVector() {
      this.reset();
   }

   float[] getVector() {
      return this.mVector;
   }

   MyVector(float[] values) {
      this.set(values);
      this.mVector[3] = 1.0F;
   }

   void reset() {
      this.mVector[0] = this.mVector[1] = this.mVector[2] = 0.0F;
      this.mVector[3] = 1.0F;
   }

   void set(float[] values) {
      this.mVector[0] = values[0];
      this.mVector[1] = values[1];
      this.mVector[2] = values[2];
   }

   void add(MyVector v) {
      this.mVector[0] += v.mVector[0];
      this.mVector[1] += v.mVector[1];
      this.mVector[2] += v.mVector[2];
      this.mVector[3] += v.mVector[3];
   }

   void normalize() {
      float len = this.length();
      this.mVector[0] /= len;
      this.mVector[1] /= len;
      this.mVector[2] /= len;
   }

   float length() {
      return (float)Math.sqrt((double)(this.mVector[0] * this.mVector[0] + this.mVector[1] * this.mVector[1] + this.mVector[2] * this.mVector[2]));
   }

   void transform(Matrix m) {
      float[] vector = new float[4];
      float[] matrix = m.getMatrix();
      vector[0] = this.mVector[0] * matrix[0] + this.mVector[1] * matrix[4] + this.mVector[2] * matrix[8] + matrix[12];
      vector[1] = this.mVector[0] * matrix[1] + this.mVector[1] * matrix[5] + this.mVector[2] * matrix[9] + matrix[13];
      vector[2] = this.mVector[0] * matrix[2] + this.mVector[1] * matrix[6] + this.mVector[2] * matrix[10] + matrix[14];
      vector[3] = this.mVector[0] * matrix[3] + this.mVector[1] * matrix[7] + this.mVector[2] * matrix[11] + matrix[15];
      this.mVector[0] = vector[0];
      this.mVector[1] = vector[1];
      this.mVector[2] = vector[2];
      this.mVector[3] = vector[3];
   }

   void transform3(Matrix m) {
      float[] vector = new float[3];
      float[] matrix = m.getMatrix();
      vector[0] = this.mVector[0] * matrix[0] + this.mVector[1] * matrix[4] + this.mVector[2] * matrix[8];
      vector[1] = this.mVector[0] * matrix[1] + this.mVector[1] * matrix[5] + this.mVector[2] * matrix[9];
      vector[2] = this.mVector[0] * matrix[2] + this.mVector[1] * matrix[6] + this.mVector[2] * matrix[10];
      this.mVector[0] = vector[0];
      this.mVector[1] = vector[1];
      this.mVector[2] = vector[2];
      this.mVector[3] = 1.0F;
   }

   public static void transform(float[] vector, float[] m_vector, float[] matrix) {
      vector[0] = m_vector[0] * matrix[0] + m_vector[1] * matrix[4] + m_vector[2] * matrix[8] + matrix[12];
      vector[1] = m_vector[0] * matrix[1] + m_vector[1] * matrix[5] + m_vector[2] * matrix[9] + matrix[13];
      vector[2] = m_vector[0] * matrix[2] + m_vector[1] * matrix[6] + m_vector[2] * matrix[10] + matrix[14];
   }

   public static void transform3(float[] vector, float[] m_vector, float[] matrix) {
      vector[0] = m_vector[0] * matrix[0] + m_vector[1] * matrix[4] + m_vector[2] * matrix[8];
      vector[1] = m_vector[0] * matrix[1] + m_vector[1] * matrix[5] + m_vector[2] * matrix[9];
      vector[2] = m_vector[0] * matrix[2] + m_vector[1] * matrix[6] + m_vector[2] * matrix[10];
   }
}
