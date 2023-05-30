package com.wurmonline.math;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class TilePos implements Cloneable {
   public int x;
   public int y;

   public TilePos() {
      this.x = 0;
      this.y = 0;
   }

   private TilePos(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public TilePos(TilePos rhs) {
      this.x = rhs.x;
      this.y = rhs.y;
   }

   public static TilePos fromXY(int tileX, int tileY) {
      return new TilePos(tileX, tileY);
   }

   @Override
   protected Object clone() throws CloneNotSupportedException {
      TilePos newPos = (TilePos)super.clone();
      newPos.set(this.x, this.y);
      return newPos;
   }

   public void set(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public TilePos add(int x, int y, TilePos storage) {
      if (storage == null) {
         storage = new TilePos();
      }

      storage.set(this.x + x, this.y + y);
      return storage;
   }

   public TilePos North() {
      return fromXY(this.x, this.y - 1);
   }

   public TilePos South() {
      return new TilePos(this.x, this.y + 1);
   }

   public TilePos West() {
      return new TilePos(this.x - 1, this.y);
   }

   public TilePos East() {
      return fromXY(this.x + 1, this.y);
   }

   public TilePos NorthWest() {
      return fromXY(this.x - 1, this.y - 1);
   }

   public TilePos SouthEast() {
      return new TilePos(this.x + 1, this.y + 1);
   }

   public TilePos NorthEast() {
      return fromXY(this.x + 1, this.y - 1);
   }

   public TilePos SouthWest() {
      return new TilePos(this.x - 1, this.y + 1);
   }

   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         return true;
      } else if (!(obj instanceof TilePos)) {
         return false;
      } else {
         TilePos pos = (TilePos)obj;
         return this.x == pos.x & this.y == pos.y;
      }
   }

   public final boolean equals(TilePos pos) {
      return this.x == pos.x && this.y == pos.y;
   }

   @Override
   public String toString() {
      return "tilePos: " + this.x + ", " + this.y;
   }

   public static Iterable<TilePos> areaIterator(TilePos minPos, TilePos maxPos) {
      return new TilePos.Area(minPos, maxPos);
   }

   public static Iterable<TilePos> areaIterator(int x1, int y1, int x2, int y2) {
      return new TilePos.Area(x1, y1, x2, y2);
   }

   public static Iterable<TilePos> bordersIterator(TilePos minPos, TilePos maxPos) {
      return new TilePos.Borders(minPos, maxPos);
   }

   public static Iterable<TilePos> bordersIterator(int x1, int y1, int x2, int y2) {
      return new TilePos.Borders(x1, y1, x2, y2);
   }

   private static class Area implements Iterable<TilePos> {
      private final TilePos.Area.AreaIterator it;

      public Area(TilePos posMin, TilePos posMax) {
         this.it = new TilePos.Area.AreaIterator(posMin, posMax);
      }

      public Area(int x1, int y1, int x2, int y2) {
         this.it = new TilePos.Area.AreaIterator(x1, y1, x2, y2);
      }

      @Override
      public Iterator<TilePos> iterator() {
         return this.it;
      }

      private static class AreaIterator extends TilePos.IteratorPositions {
         AreaIterator(TilePos posMin, TilePos posMax) {
            this(posMin.x, posMin.y, posMax.x, posMax.y);
         }

         AreaIterator(int x1, int y1, int x2, int y2) {
            super(x1, y1, x2, y2);
         }

         public TilePos next() {
            assert this.curPos.x <= this.x2 || this.curPos.y <= this.y2;

            if (this.curPos.x >= this.x2 && this.curPos.y >= this.y2) {
               throw new NoSuchElementException("This condition should not be possible!");
            } else {
               ++this.curPos.x;
               if (this.curPos.x > this.x2) {
                  this.curPos.x = this.x1;
                  ++this.curPos.y;
               }

               this.userPos.set(this.curPos.x, this.curPos.y);
               return this.userPos;
            }
         }
      }
   }

   private static class Borders implements Iterable<TilePos> {
      private final TilePos.Borders.BorderIterator it;

      public Borders(TilePos posMin, TilePos posMax) {
         this.it = new TilePos.Borders.BorderIterator(posMin, posMax);
      }

      public Borders(int x1, int y1, int x2, int y2) {
         this.it = new TilePos.Borders.BorderIterator(x1, y1, x2, y2);
      }

      @Override
      public Iterator<TilePos> iterator() {
         return this.it;
      }

      private static class BorderIterator extends TilePos.IteratorPositions {
         BorderIterator(TilePos posMin, TilePos posMax) {
            this(posMin.x, posMin.y, posMax.x, posMax.y);
         }

         BorderIterator(int x1, int y1, int x2, int y2) {
            super(x1, y1, x2, y2);

            assert x1 < x2 + 2 && y1 < y2 + 2;
         }

         public TilePos next() {
            assert this.curPos.x <= this.x2 || this.curPos.y <= this.y2;

            if (this.curPos.x >= this.x2 && this.curPos.y >= this.y2) {
               throw new NoSuchElementException("This condition should not be possible!");
            } else {
               if (this.curPos.y != this.y1 && this.curPos.y != this.y2) {
                  assert this.curPos.x == this.x1 || this.curPos.x == this.x2 : "This condition should not be possible!";

                  this.curPos.x = this.curPos.x == this.x1 ? this.x2 : this.x1;
                  if (this.curPos.x == this.x1) {
                     ++this.curPos.y;
                  }
               } else {
                  ++this.curPos.x;
                  if (this.curPos.x > this.x2) {
                     this.curPos.x = this.x1;
                     ++this.curPos.y;

                     assert this.curPos.y <= this.y2 : "This condition should not be possible!";
                  }
               }

               this.userPos.set(this.curPos.x, this.curPos.y);
               return this.userPos;
            }
         }
      }
   }

   private abstract static class IteratorPositions implements Iterator<TilePos> {
      final int x1;
      final int y1;
      final int x2;
      final int y2;
      final TilePos curPos = new TilePos();
      final TilePos userPos = new TilePos();

      IteratorPositions(int x1, int y1, int x2, int y2) {
         assert x1 <= x2;

         assert y1 <= y2;

         assert x1 < x2 || y1 < y2;

         this.x1 = x1;
         this.y1 = y1;
         this.x2 = x2;
         this.y2 = y2;
         this.curPos.set(x1, y1);
      }

      @Override
      public boolean hasNext() {
         return this.curPos.x < this.x2 || this.curPos.y < this.y2;
      }
   }
}
