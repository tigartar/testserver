package com.wurmonline.server.villages;

public class VillageRecruitee {
   final int villageId;
   final String recruiteeName;
   final long recruiteeId;

   public VillageRecruitee(int v_id, long r_id, String r_name) {
      this.villageId = v_id;
      this.recruiteeId = r_id;
      this.recruiteeName = r_name;
   }

   public final int getVillageId() {
      return this.villageId;
   }

   public final String getRecruiteeName() {
      return this.recruiteeName;
   }

   public final long getRecruiteeId() {
      return this.recruiteeId;
   }
}
