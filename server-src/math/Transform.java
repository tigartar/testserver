package com.wurmonline.math;

public class Transform {
   public final Quaternion rotation = new Quaternion();
   public final Vector3f translation = new Vector3f();

   public final void identity() {
      this.rotation.identity();
      this.translation.zero();
   }
}
