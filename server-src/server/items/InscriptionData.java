package com.wurmonline.server.items;

import com.wurmonline.server.Items;
import com.wurmonline.server.utils.DbUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class InscriptionData {
   private String inscription;
   private final long wurmid;
   private String inscriber;
   private int penColour;
   private static final Logger logger = Logger.getLogger(ItemData.class.getName());
   private static final String legalInscriptionChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'- 1234567890.,+/!() ;:_#";

   public InscriptionData(long wid, String theData, String theInscriber, int thePenColour) {
      this.wurmid = wid;
      this.setInscription(theData);
      this.setInscriber(theInscriber);
      this.penColour = thePenColour;
      Items.addItemInscriptionData(this);
   }

   public InscriptionData(long wid, Recipe recipe, String theInscriber, int thePenColour) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(bos);

      try {
         recipe.pack(dos);
         dos.flush();
         dos.close();
      } catch (IOException var10) {
         logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
      }

      byte[] data = bos.toByteArray();
      String base64encodedRecipe = Base64.getEncoder().encodeToString(data);
      this.wurmid = wid;
      this.setInscription(base64encodedRecipe);
      this.setInscriber(theInscriber);
      this.penColour = thePenColour;
      Items.addItemInscriptionData(this);
   }

   public String getInscription() {
      return this.inscription;
   }

   @Nullable
   public Recipe getRecipe() {
      byte[] bytes = Base64.getDecoder().decode(this.inscription);
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

      try {
         return new Recipe(dis);
      } catch (NoSuchTemplateException var4) {
         logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
      } catch (IOException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }

      return null;
   }

   public void setInscription(String newInscription) {
      this.inscription = newInscription;
   }

   public String getInscriber() {
      return this.inscriber;
   }

   public void setInscriber(String aInscriber) {
      this.inscriber = aInscriber;
   }

   public int getPenColour() {
      return this.penColour;
   }

   public void setPenColour(int newColour) {
      this.penColour = newColour;
   }

   public long getWurmId() {
      return this.wurmid;
   }

   public boolean hasBeenInscribed() {
      return this.getInscription() != null && this.getInscription().length() > 0;
   }

   public void createInscriptionEntry(Connection dbcon) {
      PreparedStatement ps = null;

      try {
         ps = dbcon.prepareStatement(ItemDbStrings.getInstance().createInscription());
         ps.setLong(1, this.getWurmId());
         ps.setString(2, this.getInscription());
         ps.setString(3, this.getInscriber());
         ps.setInt(4, this.getPenColour());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to save inscription data " + this.getWurmId(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
      }
   }

   public static final boolean containsIllegalCharacters(String name) {
      char[] chars = name.toCharArray();

      for(int x = 0; x < chars.length; ++x) {
         if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'- 1234567890.,+/!() ;:_#".indexOf(chars[x]) < 0) {
            return true;
         }
      }

      return false;
   }
}
