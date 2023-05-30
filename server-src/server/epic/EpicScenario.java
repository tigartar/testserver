package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EpicScenario implements MiscConstants {
   private static final Logger logger = Logger.getLogger(EpicScenario.class.getName());
   private int collectiblesToWin = 5;
   private int collectiblesForWurmToWin = 8;
   private boolean spawnPointRequiredToWin = true;
   private int hexNumRequiredToWin = 0;
   private int scenarioNumber = 0;
   private int reasonPlusEffect = 0;
   private String scenarioName = "";
   private String scenarioQuest = "";
   private boolean current = false;
   private static final String INSERTSCENARIO = "INSERT INTO SCENARIOS (NAME,REASONEFF,COLLREQ,COLLWURMREQ,SPAWNREQ,HEXREQ,QUESTSTRING,CURRENT,NUMBER) VALUES (?,?,?,?,?,?,?,?,?)";
   private static final String UPDATESCENARIO = "UPDATE SCENARIOS SET NAME=?,REASONEFF=?,COLLREQ=?,COLLWURMREQ=?,SPAWNREQ=?,HEXREQ=?,QUESTSTRING=?,CURRENT=? WHERE NUMBER=?";
   private static final String LOADCURRENTSCENARIO = "SELECT * FROM SCENARIOS WHERE CURRENT=1";

   boolean loadCurrentScenario() {
      boolean toReturn = false;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM SCENARIOS WHERE CURRENT=1");

         for(rs = ps.executeQuery(); rs.next(); toReturn = true) {
            this.scenarioName = rs.getString("NAME");
            this.scenarioNumber = rs.getInt("NUMBER");
            this.reasonPlusEffect = rs.getInt("REASONEFF");
            this.collectiblesToWin = rs.getInt("COLLREQ");
            this.collectiblesForWurmToWin = rs.getInt("COLLWURMREQ");
            this.spawnPointRequiredToWin = rs.getBoolean("SPAWNREQ");
            this.hexNumRequiredToWin = rs.getInt("HEXREQ");
            this.scenarioQuest = rs.getString("QUESTSTRING");
            this.current = true;
            logger.log(Level.INFO, "Loaded current scenario " + this.scenarioName);
         }
      } catch (SQLException var9) {
         logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return toReturn;
   }

   public final void saveScenario(boolean _current) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      this.current = _current;

      try {
         dbcon = DbConnector.getDeityDbCon();
         if (this.current) {
            ps = dbcon.prepareStatement(
               "INSERT INTO SCENARIOS (NAME,REASONEFF,COLLREQ,COLLWURMREQ,SPAWNREQ,HEXREQ,QUESTSTRING,CURRENT,NUMBER) VALUES (?,?,?,?,?,?,?,?,?)"
            );
         } else {
            ps = dbcon.prepareStatement(
               "UPDATE SCENARIOS SET NAME=?,REASONEFF=?,COLLREQ=?,COLLWURMREQ=?,SPAWNREQ=?,HEXREQ=?,QUESTSTRING=?,CURRENT=? WHERE NUMBER=?"
            );
         }

         ps.setString(1, this.scenarioName);
         ps.setInt(2, this.reasonPlusEffect);
         ps.setInt(3, this.collectiblesToWin);
         ps.setInt(4, this.collectiblesForWurmToWin);
         ps.setBoolean(5, this.spawnPointRequiredToWin);
         ps.setInt(6, this.hexNumRequiredToWin);
         ps.setString(7, this.scenarioQuest);
         ps.setBoolean(8, this.current);
         ps.setInt(9, this.scenarioNumber);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public int getCollectiblesToWin() {
      return this.collectiblesToWin;
   }

   public void setCollectiblesToWin(int aCollectiblesToWin) {
      this.collectiblesToWin = aCollectiblesToWin;
   }

   public int getCollectiblesForWurmToWin() {
      return this.collectiblesForWurmToWin;
   }

   public void setCollectiblesForWurmToWin(int aCollectiblesForWurmToWin) {
      this.collectiblesForWurmToWin = aCollectiblesForWurmToWin;
   }

   public boolean isSpawnPointRequiredToWin() {
      return this.getHexNumRequiredToWin() <= 0 ? true : this.spawnPointRequiredToWin;
   }

   public void setSpawnPointRequiredToWin(boolean aSpawnPointRequiredToWin) {
      this.spawnPointRequiredToWin = aSpawnPointRequiredToWin;
   }

   public int getHexNumRequiredToWin() {
      return this.hexNumRequiredToWin;
   }

   public void setHexNumRequiredToWin(int aHexNumRequiredToWin) {
      this.hexNumRequiredToWin = aHexNumRequiredToWin;
   }

   public int getScenarioNumber() {
      return this.scenarioNumber;
   }

   public void setScenarioNumber(int aScenarioNumber) {
      this.scenarioNumber = aScenarioNumber;
   }

   void incrementScenarioNumber() {
      ++this.scenarioNumber;
   }

   public int getReasonPlusEffect() {
      return this.reasonPlusEffect;
   }

   public void setReasonPlusEffect(int aReasonPlusEffect) {
      this.reasonPlusEffect = aReasonPlusEffect;
   }

   public String getScenarioName() {
      return this.scenarioName;
   }

   public void setScenarioName(String aScenarioName) {
      this.scenarioName = aScenarioName;
   }

   public String getScenarioQuest() {
      return this.scenarioQuest;
   }

   public void setScenarioQuest(String aScenarioQuest) {
      this.scenarioQuest = aScenarioQuest;
   }

   public boolean isCurrent() {
      return this.current;
   }

   public void setCurrent(boolean aCurrent) {
      this.current = aCurrent;
   }
}
