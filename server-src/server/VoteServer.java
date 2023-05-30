package com.wurmonline.server;

public class VoteServer {
   private final int questionId;
   private final int serverId;
   private short total;
   private short count1;
   private short count2;
   private short count3;
   private short count4;

   public VoteServer(int aQuestionId, int aServerId) {
      this(aQuestionId, aServerId, (short)0, (short)0, (short)0, (short)0, (short)0);
   }

   public VoteServer(int aQuestionId, int aServerId, short aTotal, short aCount1, short aCount2, short aCount3, short aCount4) {
      this.questionId = aQuestionId;
      this.serverId = aServerId;
      this.total = aTotal;
      this.count1 = aCount1;
      this.count2 = aCount2;
      this.count3 = aCount3;
      this.count4 = aCount4;
   }

   public short getTotal() {
      return this.total;
   }

   public void setTotal(short aTotal) {
      this.total = aTotal;
   }

   public short getCount1() {
      return this.count1;
   }

   public void setCount1(short aCount1) {
      this.count1 = aCount1;
   }

   public short getCount2() {
      return this.count2;
   }

   public void setCount2(short aCount2) {
      this.count2 = aCount2;
   }

   public short getCount3() {
      return this.count3;
   }

   public void setCount3(short aCount3) {
      this.count3 = aCount3;
   }

   public short getCount4() {
      return this.count4;
   }

   public void setCount4(short aCount4) {
      this.count4 = aCount4;
   }

   public int getQuestionId() {
      return this.questionId;
   }

   public int getServerId() {
      return this.serverId;
   }
}
