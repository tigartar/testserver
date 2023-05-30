package com.wurmonline.server;

import java.io.File;

public class DummyTestClass extends XMLSerializer {
   @XMLSerializer.Saved
   long test = 0L;
   @XMLSerializer.Saved
   String myClass = "my Class is dummy";
   @XMLSerializer.Saved
   float saveThis = 3.24324E-4F;
   final float dontSave = 0.9333222F;

   public long getTest() {
      return this.test;
   }

   public void setTest(long aTest) {
      this.test = aTest;
   }

   public String getMyClass() {
      return this.myClass;
   }

   public void setMyClass(String aMyClass) {
      this.myClass = aMyClass;
   }

   public float getDontSave() {
      return 0.9333222F;
   }

   public void setSaveThis(float aSaveThis) {
      this.saveThis = aSaveThis;
   }

   public float getSaveThis() {
      return this.saveThis;
   }

   public final DummyTestClass createInstanceAndCallLoadXML(File file) {
      this.loadXML(file);
      return this;
   }
}
