package com.wurmonline.server.creatures;

import com.wurmonline.math.Vector2f;
import com.wurmonline.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class BoundBox {
   public BoxMatrix M;
   public Vector3f extent;

   public BoundBox() {
      this.M = new BoxMatrix(true);
      this.extent = new Vector3f(0.0F, 0.0F, 0.0F);
   }

   public BoundBox(BoxMatrix m, Vector3f e) {
      this.set(m, e);
   }

   public BoundBox(BoxMatrix m, Vector3f bl, Vector3f bh) {
      this.set(m, bl, bh);
   }

   public void set(BoxMatrix m, Vector3f e) {
      this.M = m;
      this.extent = e;
   }

   public void set(BoxMatrix m, Vector3f bl, Vector3f bh) {
      this.M = m;
      this.M.translate(bh.add(bl).mult(0.5F));
      this.extent = bh.subtract(bl).divide(2.0F);
   }

   public final Vector3f getSize() {
      return this.extent.mult(2.0F);
   }

   public final Vector3f getCenterPoint() {
      return this.M.getTranslate();
   }

   public final boolean isPointInBox(Vector3f inP) {
      Vector3f P = this.M.InvertSimple().multiply(inP);
      return Math.abs(P.x) < this.extent.x && Math.abs(P.y) < this.extent.y;
   }

   private final Vector2f getIntersection(Vector3f pp, Vector3f cp, Vector3f s1, Vector3f s2) {
      Vector2f L = new Vector2f(cp.x - pp.x, cp.y - pp.y);
      Vector2f S = new Vector2f(s2.x - s1.x, s2.y - s1.y);
      float dot = L.x * S.y - L.y * S.x;
      if (dot == 0.0F) {
         return null;
      } else {
         Vector2f c = new Vector2f(s1.x - pp.x, s1.y - pp.y);
         float t = (c.x * S.y - c.y * S.x) / dot;
         if (!(t < 0.0F) && !(t > 1.0F)) {
            float u = (c.x * L.y - c.y * L.x) / dot;
            if (!(u < 0.0F) && !(u > 1.0F)) {
               Vector2f inter = null;
               Vector2f LP = new Vector2f(pp.x, pp.y);
               return LP.add(L.mult(t));
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   public final float distOutside(Vector3f inP, Vector3f cpoint) {
      BoxMatrix MInv = this.M.InvertSimple();
      Vector3f LB1 = MInv.multiply(inP);
      Vector3f LB2 = MInv.multiply(cpoint);
      List<Vector2f> inters = new ArrayList<>();
      Vector2f ii = this.getIntersection(LB1, LB2, new Vector3f(-this.extent.x, -this.extent.y, 1.0F), new Vector3f(-this.extent.x, this.extent.y, 1.0F));
      if (ii != null) {
         inters.add(ii);
      }

      ii = this.getIntersection(LB1, LB2, new Vector3f(-this.extent.x, this.extent.y, 1.0F), new Vector3f(this.extent.x, this.extent.y, 1.0F));
      if (ii != null) {
         inters.add(ii);
      }

      ii = this.getIntersection(LB1, LB2, new Vector3f(this.extent.x, this.extent.y, 1.0F), new Vector3f(this.extent.x, -this.extent.y, 1.0F));
      if (ii != null) {
         inters.add(ii);
      }

      ii = this.getIntersection(LB1, LB2, new Vector3f(this.extent.x, -this.extent.y, 1.0F), new Vector3f(-this.extent.x, -this.extent.y, 1.0F));
      if (ii != null) {
         inters.add(ii);
      }

      if (inters.size() > 0) {
         Vector2f p2 = new Vector2f(LB1.x, LB1.y);
         float minLen = 0.0F;

         for(int i = 0; i < inters.size(); ++i) {
            float len = p2.subtract(inters.get(i)).length();
            if (i == 0) {
               minLen = len;
            } else if (len < minLen) {
               minLen = len;
            }
         }

         return minLen;
      } else {
         return -1.0F;
      }
   }

   public final Vector3f[] getInvRot() {
      return new Vector3f[]{
         new Vector3f(this.M.mf[0], this.M.mf[1], this.M.mf[2]),
         new Vector3f(this.M.mf[4], this.M.mf[5], this.M.mf[6]),
         new Vector3f(this.M.mf[8], this.M.mf[9], this.M.mf[10])
      };
   }
}
