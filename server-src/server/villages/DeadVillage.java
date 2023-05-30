package com.wurmonline.server.villages;

import com.wurmonline.server.WurmCalendar;

public class DeadVillage {
   private static final String[] directions = new String[]{"north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest"};
   private static final String[] distances = new String[]{"very close", "nearby", "close", "far", "quite distant", "very far"};
   private final long deedId;
   private final int startX;
   private final int startY;
   private final int endX;
   private final int endY;
   private final String deedName;
   private final String founderName;
   private final String mayorName;
   private final long creationDate;
   private final long disbandDate;
   private final long lastLoginDate;
   private final byte kingdomId;

   public DeadVillage(
      long deedId,
      int startx,
      int starty,
      int endx,
      int endy,
      String name,
      String founder,
      String mayor,
      long creationDate,
      long disbandDate,
      long lastLogin,
      byte kingdom
   ) {
      this.deedId = deedId;
      this.startX = startx;
      this.startY = starty;
      this.endX = endx;
      this.endY = endy;
      this.deedName = name;
      this.founderName = founder;
      this.mayorName = mayor;
      this.creationDate = creationDate;
      this.disbandDate = disbandDate;
      this.lastLoginDate = lastLogin;
      this.kingdomId = kingdom;
   }

   public long getDeedId() {
      return this.deedId;
   }

   public int getStartX() {
      return this.startX;
   }

   public int getStartY() {
      return this.startY;
   }

   public int getEndX() {
      return this.endX;
   }

   public int getEndY() {
      return this.endY;
   }

   public int getCenterX() {
      return this.getStartX() + (this.getEndX() - this.getStartX()) / 2;
   }

   public int getCenterY() {
      return this.getStartY() + (this.getEndY() - this.getStartY()) / 2;
   }

   public String getDeedName() {
      return this.deedName;
   }

   public String getFounderName() {
      return this.founderName;
   }

   public String getMayorName() {
      return this.mayorName;
   }

   public long getCreationDate() {
      return this.creationDate;
   }

   public long getDisbandDate() {
      return this.disbandDate;
   }

   public long getLastLoginDate() {
      return this.lastLoginDate;
   }

   public byte getKingdomId() {
      return this.kingdomId;
   }

   public float getTimeSinceDisband() {
      return (float)(System.currentTimeMillis() - this.getLastLoginDate()) / 2.4192E9F;
   }

   public float getTotalAge() {
      return (float)(this.getLastLoginDate() - this.getCreationDate()) / 2.4192E9F;
   }

   public String getDistanceFrom(int tilex, int tiley) {
      int centerX = this.getStartX() + (this.getEndX() - this.getStartX()) / 2;
      int centerY = this.getStartY() + (this.getEndY() - this.getStartY()) / 2;
      int xDiff = centerX - tilex;
      int yDiff = centerY - tiley;
      int dist = Math.max(Math.abs(xDiff), Math.abs(yDiff));
      return getDistance(dist);
   }

   public String getDirectionFrom(int tilex, int tiley) {
      int centerX = this.getStartX() + (this.getEndX() - this.getStartX()) / 2;
      int centerY = this.getStartY() + (this.getEndY() - this.getStartY()) / 2;
      int xDiff = centerX - tilex;
      int yDiff = centerY - tiley;
      double degrees = Math.atan2((double)yDiff, (double)xDiff) * (180.0 / Math.PI) + 90.0;
      if (degrees < 0.0) {
         degrees += 360.0;
      }

      return getDirection(degrees);
   }

   private static String getDistance(int dist) {
      if (dist <= 20) {
         return distances[0];
      } else if (dist <= 40) {
         return distances[1];
      } else if (dist <= 80) {
         return distances[2];
      } else if (dist <= 120) {
         return distances[3];
      } else {
         return dist <= 180 ? distances[4] : distances[5];
      }
   }

   private static String getDirection(double degrees) {
      return directions[(int)Math.round(degrees % 360.0 / 45.0) % 8];
   }

   public static final String getTimeString(float monthsTotal, boolean provideYear) {
      StringBuilder sb = new StringBuilder();
      int years = (int)(monthsTotal * 8.0F / 12.0F);
      int months = (int)(monthsTotal * 8.0F) % 12;
      if (years > 0) {
         sb.append(years + " year" + (years > 1 ? "s" : "") + (months > 0 ? ", " : ""));
      }

      if (months > 0) {
         sb.append(months + " month" + (months > 1 ? "s" : ""));
      }

      if (years <= 0 && months <= 0) {
         sb.append("less than a month");
      }

      if (provideYear) {
         sb.append(", somewhere around the year " + (WurmCalendar.getYear() - years));
      }

      return sb.toString();
   }
}
