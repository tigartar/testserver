package com.wurmonline.server.support;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.VoteServer;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerVotes;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.ProtoConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VoteQuestion implements MiscConstants, ProtoConstants, TimeConstants {
   private static final String ADDVOTINGQUESTION = "INSERT INTO VOTINGQUESTIONS (QUESTIONTITLE,QUESTIONTEXT,OPTION1_TEXT,OPTION2_TEXT,OPTION3_TEXT,OPTION4_TEXT,ALLOW_MULTIPLE,PREMIUM_ONLY,JK,MR,HOTS,FREEDOM,VOTE_START,VOTE_END,SENT,QUESTIONID) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String UPDATEVOTINGQUESTION = "UPDATE VOTINGQUESTIONS SET QUESTIONTITLE=?,QUESTIONTEXT=?,OPTION1_TEXT=?,OPTION2_TEXT=?,OPTION3_TEXT=?,OPTION4_TEXT=?,ALLOW_MULTIPLE=?,PREMIUM_ONLY=?,JK=?,MR=?,HOTS=?,FREEDOM=?,VOTE_START=?,VOTE_END=?,SENT=?,QUESTIONID=?)";
   private static final String UPDATEVOTINGCOUNTS = "UPDATE VOTINGQUESTIONS SET VOTES_TOTAL=?,HAS_SUMMARY=1,OPTION1_COUNT=?,OPTION2_COUNT=?,OPTION3_COUNT=?,OPTION4_COUNT=? WHERE QUESTIONID=?";
   private static final String UPDATESENT = "UPDATE VOTINGQUESTIONS SET SENT=? WHERE QUESTIONID=?";
   private static final String UPDATEVOTEEND = "UPDATE VOTINGQUESTIONS SET VOTE_END=? WHERE QUESTIONID=?";
   private static final String UPDATETRELLOCARDID = "UPDATE VOTINGQUESTIONS SET TRELLOCARDID=? WHERE QUESTIONID=?";
   private static final String UPDATEARCHIVESTATE = "UPDATE VOTINGQUESTIONS SET ARCHIVESTATECODE=? WHERE QUESTIONID=?";
   private static final String DELETEVOTINGQUESTION = "DELETE FROM VOTINGQUESTIONS WHERE QUESTIONID=?";
   private static final String ADDVOTINGSERVERS = "INSERT INTO VOTINGSERVERS (QUESTIONID,SERVERID) VALUES(?,?)";
   private static final String DELETEVOTINGSERVERS = "DELETE FROM VOTINGSERVERS WHERE QUESTIONID=?";
   private static final Logger logger = Logger.getLogger(VoteQuestion.class.getName());
   public static final byte SENT_NOTHING = 0;
   public static final byte SENT_QUESTION = 1;
   public static final byte VOTING_IN_PROGRESS = 2;
   public static final byte VOTING_ENDED = 3;
   public static final byte SENT_SUMMARY = 4;
   private final int questionId;
   private String questionTitle;
   private String questionText;
   private String option1Text;
   private String option2Text;
   private String option3Text;
   private String option4Text;
   private boolean allowMultiple;
   private boolean premOnly;
   private boolean jk;
   private boolean mr;
   private boolean hots;
   private boolean freedom;
   private long start;
   private long end;
   private byte sent = 0;
   private short voteCount = 0;
   private boolean hasSummary = false;
   private short option1Count = 0;
   private short option2Count = 0;
   private short option3Count = 0;
   private short option4Count = 0;
   private String trelloCardId = "";
   private byte archiveState = 0;
   private List<VoteServer> servers = new ArrayList<>();
   private boolean isOnDb = false;
   private boolean serversOnLine = true;

   public VoteQuestion(
      int aQuestionId,
      String aQuestionTitle,
      String aQuestionText,
      String aOption1Text,
      String aOption2Text,
      String aOption3Text,
      String aOption4Text,
      boolean aAllowMultiple,
      boolean aPremOnly,
      boolean aJK,
      boolean aMR,
      boolean aHoTs,
      boolean aFreedom,
      long voteStart,
      long voteEnd,
      List<VoteServer> theServers
   ) {
      this(
         aQuestionId,
         aQuestionTitle,
         aQuestionText,
         aOption1Text,
         aOption2Text,
         aOption3Text,
         aOption4Text,
         aAllowMultiple,
         aPremOnly,
         aJK,
         aMR,
         aHoTs,
         aFreedom,
         voteStart,
         voteEnd,
         (byte)0,
         (short)0,
         false,
         (short)0,
         (short)0,
         (short)0,
         (short)0,
         "",
         (byte)0,
         theServers,
         false
      );
   }

   public VoteQuestion(
      int aQuestionId,
      String aQuestionTitle,
      String aQuestionText,
      String aOption1Text,
      String aOption2Text,
      String aOption3Text,
      String aOption4Text,
      boolean aAllowMultiple,
      boolean aPremOnly,
      boolean aJK,
      boolean aMR,
      boolean aHoTs,
      boolean aFreedom,
      long voteStart,
      long voteEnd
   ) {
      this(
         aQuestionId,
         aQuestionTitle,
         aQuestionText,
         aOption1Text,
         aOption2Text,
         aOption3Text,
         aOption4Text,
         aAllowMultiple,
         aPremOnly,
         aJK,
         aMR,
         aHoTs,
         aFreedom,
         voteStart,
         voteEnd,
         (byte)1,
         (short)0,
         false,
         (short)0,
         (short)0,
         (short)0,
         (short)0,
         "",
         (byte)0,
         new ArrayList<>(),
         false
      );
   }

   public VoteQuestion(
      int aQuestionId,
      String aQuestionTitle,
      String aQuestionText,
      String aOption1Text,
      String aOption2Text,
      String aOption3Text,
      String aOption4Text,
      boolean aAllowMultiple,
      boolean aPremOnly,
      boolean aJK,
      boolean aMR,
      boolean aHoTs,
      boolean aFreedom,
      long voteStart,
      long voteEnd,
      byte aSent,
      short aVoteCount,
      boolean aHasSummary,
      short aOption1Count,
      short aOption2Count,
      short aOption3Count,
      short aOption4Count,
      String aTrelloCardId,
      byte theArchiveState
   ) {
      this(
         aQuestionId,
         aQuestionTitle,
         aQuestionText,
         aOption1Text,
         aOption2Text,
         aOption3Text,
         aOption4Text,
         aAllowMultiple,
         aPremOnly,
         aJK,
         aMR,
         aHoTs,
         aFreedom,
         voteStart,
         voteEnd,
         aSent,
         aVoteCount,
         aHasSummary,
         aOption1Count,
         aOption2Count,
         aOption3Count,
         aOption4Count,
         aTrelloCardId,
         theArchiveState,
         new ArrayList<>(),
         true
      );
   }

   public VoteQuestion(
      int aQuestionId,
      String aQuestionTitle,
      String aQuestionText,
      String aOption1Text,
      String aOption2Text,
      String aOption3Text,
      String aOption4Text,
      boolean aAllowMultiple,
      boolean aPremOnly,
      boolean aJK,
      boolean aMR,
      boolean aHoTs,
      boolean aFreedom,
      long voteStart,
      long voteEnd,
      byte aSent,
      short aVoteCount,
      boolean aHasSummary,
      short aOption1Count,
      short aOption2Count,
      short aOption3Count,
      short aOption4Count,
      String aTrelloCardId,
      byte theArchiveState,
      List<VoteServer> theServers,
      boolean isFromDb
   ) {
      this.questionId = aQuestionId;
      this.questionTitle = aQuestionTitle;
      this.questionText = aQuestionText;
      this.option1Text = aOption1Text;
      this.option2Text = aOption2Text;
      this.option3Text = aOption3Text;
      this.option4Text = aOption4Text;
      this.allowMultiple = aAllowMultiple;
      this.premOnly = aPremOnly;
      this.jk = aJK;
      this.mr = aMR;
      this.hots = aHoTs;
      this.freedom = aFreedom;
      this.start = voteStart;
      this.end = voteEnd;
      this.sent = aSent;
      this.voteCount = aVoteCount;
      this.hasSummary = aHasSummary;
      this.option1Count = aOption1Count;
      this.option2Count = aOption2Count;
      this.option3Count = aOption3Count;
      this.option4Count = aOption4Count;
      this.trelloCardId = aTrelloCardId;
      this.archiveState = theArchiveState;
      this.servers = theServers;
      this.isOnDb = isFromDb;
      if (!isFromDb) {
         this.dbAddOrUpdateVotingQuestion();
      }
   }

   public int getQuestionId() {
      return this.questionId;
   }

   public String getQuestionTitle() {
      return this.questionTitle;
   }

   public String getQuestionText() {
      return this.questionText;
   }

   public String getOption1Text() {
      return this.option1Text;
   }

   public String getOption2Text() {
      return this.option2Text;
   }

   public String getOption3Text() {
      return this.option3Text;
   }

   public String getOption4Text() {
      return this.option4Text;
   }

   public boolean isAllowMultiple() {
      return this.allowMultiple;
   }

   public boolean isPremOnly() {
      return this.premOnly;
   }

   public boolean isJK() {
      return this.jk;
   }

   public boolean isMR() {
      return this.mr;
   }

   public boolean isHots() {
      return this.hots;
   }

   public boolean isFreedom() {
      return this.freedom;
   }

   public long getVoteStart() {
      return this.start;
   }

   public long getVoteEnd() {
      return this.end;
   }

   public byte getSent() {
      return this.sent;
   }

   public void setSent(byte newSent) {
      this.sent = newSent;
      this.dbUpdateSent();
   }

   public short getVoteCount() {
      return this.voteCount;
   }

   public boolean hasSummary() {
      return this.hasSummary;
   }

   public short getOption1Count() {
      return this.option1Count;
   }

   public short getOption2Count() {
      return this.option2Count;
   }

   public short getOption3Count() {
      return this.option3Count;
   }

   public short getOption4Count() {
      return this.option4Count;
   }

   public String getTrelloCardId() {
      return this.trelloCardId;
   }

   public void setTrelloCardId(String newTrelloCardId) {
      this.trelloCardId = newTrelloCardId;
      this.dbUpdateTrelloCardId();
   }

   public byte getArchiveState() {
      return this.archiveState;
   }

   public void setArchiveState(byte newArchiveState) {
      this.archiveState = newArchiveState;
      this.dbUpdateArchiveState();
   }

   public List<VoteServer> getServers() {
      return this.servers;
   }

   public void addServer(int aServerId, short aTotal, short count1, short count2, short count3, short count4) {
      this.servers.add(new VoteServer(this.questionId, aServerId, aTotal, count1, count2, count3, count4));
   }

   public void addServer(VoteServer aServer) {
      this.servers.add(aServer);
   }

   public void addServers(List<VoteServer> theServers) {
      this.servers = theServers;
      this.dbAddOrUpdateVotingServers();
   }

   public boolean canVote(Player player) {
      if (player.getPower() != 0) {
         return false;
      } else if (!this.isActive()) {
         return false;
      } else if (this.premOnly && !player.isPaying()) {
         return false;
      } else if (this.jk && player.getKingdomTemplateId() == 1) {
         return true;
      } else if (this.mr && player.getKingdomTemplateId() == 2) {
         return true;
      } else if (this.hots && player.getKingdomTemplateId() == 3) {
         return true;
      } else {
         return this.freedom && player.getKingdomTemplateId() == 4;
      }
   }

   public boolean justStarted() {
      return this.start < System.currentTimeMillis() && this.sent == 1;
   }

   public boolean isActive() {
      if (Servers.isThisLoginServer()) {
         boolean found = false;

         for(VoteServer vs : this.servers) {
            if (vs.getServerId() == Servers.getLocalServerId()) {
               found = true;
               break;
            }
         }

         if (!found) {
            return false;
         }
      }

      return this.start < System.currentTimeMillis() && this.end > System.currentTimeMillis() && this.sent == 2;
   }

   public void endVoting() {
      if (this.justEnded()) {
         this.sent = 3;
      }
   }

   private boolean justEnded() {
      return this.hasEnded() && this.sent == 2;
   }

   public boolean hasEnded() {
      return this.end < System.currentTimeMillis();
   }

   public boolean canMakeSummary() {
      return this.sent == 3 && !this.hasSummary && this.areServerOnline();
   }

   public boolean aboutToStart() {
      return this.sent == 0 && System.currentTimeMillis() > this.start - 300000L ? this.areServerOnline() : false;
   }

   private boolean areServerOnline() {
      for(VoteServer vs : this.servers) {
         if (!Servers.getServerWithId(vs.getServerId()).isAvailable(5, true)) {
            if (this.serversOnLine) {
               logger.log(Level.WARNING, "Not all required servers online, so not sending question (yet) to other servers.");
            }

            this.serversOnLine = false;
            return false;
         }
      }

      if (!this.serversOnLine) {
         logger.log(Level.INFO, "Questions servers are back online, so sending question to them.");
      }

      this.serversOnLine = true;
      return true;
   }

   public void setArchive() {
      if (System.currentTimeMillis() > this.end + 604800000L && this.archiveState == 0) {
         if (Servers.isThisLoginServer()) {
            this.archiveState = 2;
         } else {
            this.archiveState = 3;
         }
      }
   }

   public void closeVoting(long aVoteEnd) {
      this.end = aVoteEnd;
      this.dbUpdateVoteEnd();
   }

   public void update(
      String aQuestionTitle,
      String aQuestionText,
      String aOption1Text,
      String aOption2Text,
      String aOption3Text,
      String aOption4Text,
      boolean aAllowMultiple,
      boolean aPremOnly,
      boolean aJK,
      boolean aMR,
      boolean aHoTs,
      boolean aFreedom,
      long voteStart,
      long voteEnd,
      List<VoteServer> aServers
   ) {
      this.questionTitle = aQuestionTitle;
      this.questionText = aQuestionText;
      this.option1Text = aOption1Text;
      this.option2Text = aOption2Text;
      this.option3Text = aOption3Text;
      this.option4Text = aOption4Text;
      this.allowMultiple = aAllowMultiple;
      this.premOnly = aPremOnly;
      this.jk = aJK;
      this.mr = aMR;
      this.hots = aHoTs;
      this.freedom = aFreedom;
      this.start = voteStart;
      this.end = voteEnd;
      this.servers = aServers;
      this.dbAddOrUpdateVotingQuestion();
   }

   public void save() {
      this.dbAddOrUpdateVotingQuestion();
   }

   public void delete() {
      PlayerVotes.deletePlayerVotes(this.questionId);
      this.dbDelete();
   }

   private void dbAddOrUpdateVotingQuestion() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         if (this.isOnDb) {
            ps = dbcon.prepareStatement(
               "UPDATE VOTINGQUESTIONS SET QUESTIONTITLE=?,QUESTIONTEXT=?,OPTION1_TEXT=?,OPTION2_TEXT=?,OPTION3_TEXT=?,OPTION4_TEXT=?,ALLOW_MULTIPLE=?,PREMIUM_ONLY=?,JK=?,MR=?,HOTS=?,FREEDOM=?,VOTE_START=?,VOTE_END=?,SENT=?,QUESTIONID=?)"
            );
         } else {
            ps = dbcon.prepareStatement(
               "INSERT INTO VOTINGQUESTIONS (QUESTIONTITLE,QUESTIONTEXT,OPTION1_TEXT,OPTION2_TEXT,OPTION3_TEXT,OPTION4_TEXT,ALLOW_MULTIPLE,PREMIUM_ONLY,JK,MR,HOTS,FREEDOM,VOTE_START,VOTE_END,SENT,QUESTIONID) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
            );
         }

         ps.setString(1, this.questionTitle);
         ps.setString(2, this.questionText);
         ps.setString(3, this.option1Text);
         ps.setString(4, this.option2Text);
         ps.setString(5, this.option3Text);
         ps.setString(6, this.option4Text);
         ps.setBoolean(7, this.allowMultiple);
         ps.setBoolean(8, this.premOnly);
         ps.setBoolean(9, this.jk);
         ps.setBoolean(10, this.mr);
         ps.setBoolean(11, this.hots);
         ps.setBoolean(12, this.freedom);
         ps.setLong(13, this.start);
         ps.setLong(14, this.end);
         ps.setByte(15, this.sent);
         ps.setInt(16, this.questionId);
         ps.executeUpdate();
         this.dbAddOrUpdateVotingServers();
         this.isOnDb = true;
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void dbAddOrUpdateVotingServers() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         if (this.servers.size() > 0) {
            if (this.isOnDb) {
               ps = dbcon.prepareStatement("DELETE FROM VOTINGSERVERS WHERE QUESTIONID=?");
               ps.setInt(1, this.questionId);
               ps.executeUpdate();
               DbUtilities.closeDatabaseObjects(ps, null);
            }

            ps = dbcon.prepareStatement("INSERT INTO VOTINGSERVERS (QUESTIONID,SERVERID) VALUES(?,?)");

            for(VoteServer serv : this.servers) {
               ps.setInt(1, serv.getQuestionId());
               ps.setInt(2, serv.getServerId());
               ps.executeUpdate();
            }
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public void saveSummary(short aVoteCount, short count1, short count2, short count3, short count4) {
      this.voteCount = aVoteCount;
      this.hasSummary = true;
      this.option1Count = count1;
      this.option2Count = count2;
      this.option3Count = count3;
      this.option4Count = count4;
      this.dbUpdateVotingCounts();
   }

   private void dbUpdateVotingCounts() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement(
            "UPDATE VOTINGQUESTIONS SET VOTES_TOTAL=?,HAS_SUMMARY=1,OPTION1_COUNT=?,OPTION2_COUNT=?,OPTION3_COUNT=?,OPTION4_COUNT=? WHERE QUESTIONID=?"
         );
         ps.setShort(1, this.voteCount);
         ps.setShort(2, this.option1Count);
         ps.setShort(3, this.option2Count);
         ps.setShort(4, this.option3Count);
         ps.setShort(5, this.option4Count);
         ps.setInt(6, this.questionId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void dbUpdateSent() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE VOTINGQUESTIONS SET SENT=? WHERE QUESTIONID=?");
         ps.setByte(1, this.sent);
         ps.setInt(2, this.questionId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void dbUpdateVoteEnd() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE VOTINGQUESTIONS SET VOTE_END=? WHERE QUESTIONID=?");
         ps.setLong(1, this.end);
         ps.setInt(2, this.questionId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void dbUpdateTrelloCardId() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE VOTINGQUESTIONS SET TRELLOCARDID=? WHERE QUESTIONID=?");
         ps.setString(1, this.trelloCardId);
         ps.setInt(2, this.questionId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void dbUpdateArchiveState() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE VOTINGQUESTIONS SET ARCHIVESTATECODE=? WHERE QUESTIONID=?");
         ps.setByte(1, this.archiveState);
         ps.setInt(2, this.questionId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void dbDelete() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("DELETE FROM VOTINGSERVERS WHERE QUESTIONID=?");
         ps.setInt(1, this.questionId);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM VOTINGQUESTIONS WHERE QUESTIONID=?");
         ps.setInt(1, this.questionId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public String toString() {
      return "VoteQuestion [id="
         + this.questionId
         + ", title=\""
         + this.questionTitle
         + ", text=\""
         + this.questionText
         + "\", opt1=\""
         + this.option1Text
         + "\", opt2=\""
         + this.option2Text
         + "\""
         + (this.option3Text.length() > 0 ? ", opt3=\"" + this.option3Text + "\"" : "")
         + (this.option4Text.length() > 0 ? ", opt4=\"" + this.option4Text + "\"" : "")
         + "]";
   }
}
