package com.wurmonline.website.news;

public class News {
   private long timestamp;
   private String title;
   private String text;
   private String postedBy;
   private String id;

   public News() {
   }

   public News(String aTitle, String aText, String aPostedBy) {
      this.timestamp = System.currentTimeMillis();
      this.title = aTitle;
      this.text = aText;
      this.postedBy = aPostedBy;
   }

   public String getId() {
      return this.id;
   }

   public void setId(String aId) {
      this.id = aId;
   }

   public String getPostedBy() {
      return this.postedBy;
   }

   public String getText() {
      return this.text;
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   public String getTitle() {
      return this.title;
   }

   public void setPostedBy(String string) {
      this.postedBy = string;
   }

   public void setText(String string) {
      this.text = string;
   }

   public void setTimestamp(long l) {
      this.timestamp = l;
   }

   public void setTitle(String string) {
      this.title = string;
   }
}
