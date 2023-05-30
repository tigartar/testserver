package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerVote;
import com.wurmonline.server.players.PlayerVotes;
import com.wurmonline.server.support.VoteQuestion;
import com.wurmonline.server.support.VoteQuestions;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcVoting extends WebCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(WcVoting.class.getName());
   public static final byte DO_NOTHING = 0;
   public static final byte VOTE_QUESTION = 1;
   public static final byte ASK_FOR_VOTES = 2;
   public static final byte PLAYER_VOTE = 3;
   public static final byte VOTE_SUMMARY = 4;
   public static final byte REMOVE_QUESTION = 5;
   public static final byte CLOSE_VOTING = 6;
   private byte type = 0;
   private int questionId;
   private String questionTitle;
   private String questionText;
   private String option1;
   private String option2;
   private String option3;
   private String option4;
   private boolean allowMultiple;
   private boolean premOnly;
   private boolean jk;
   private boolean mr;
   private boolean hots;
   private boolean freedom;
   private long voteStart;
   private long voteEnd;
   private long playerId;
   private short voteCount;
   private short count1;
   private short count2;
   private short count3;
   private short count4;
   private int[] questionIds;
   private PlayerVote[] playerVotes;

   public WcVoting(VoteQuestion voteQuestion) {
      super(WurmId.getNextWCCommandId(), (short)20);
      this.type = 1;
      this.questionId = voteQuestion.getQuestionId();
      this.questionTitle = voteQuestion.getQuestionTitle();
      this.questionText = voteQuestion.getQuestionText();
      this.option1 = voteQuestion.getOption1Text();
      this.option2 = voteQuestion.getOption2Text();
      this.option3 = voteQuestion.getOption3Text();
      this.option4 = voteQuestion.getOption4Text();
      this.allowMultiple = voteQuestion.isAllowMultiple();
      this.premOnly = voteQuestion.isPremOnly();
      this.jk = voteQuestion.isJK();
      this.mr = voteQuestion.isMR();
      this.hots = voteQuestion.isHots();
      this.freedom = voteQuestion.isFreedom();
      this.voteStart = voteQuestion.getVoteStart();
      this.voteEnd = voteQuestion.getVoteEnd();
   }

   public WcVoting(long aPlayerId, int[] aQuestions) {
      super(WurmId.getNextWCCommandId(), (short)20);
      this.type = 2;
      this.questionIds = aQuestions;
      this.playerId = aPlayerId;
   }

   public WcVoting(PlayerVote pv) {
      super(WurmId.getNextWCCommandId(), (short)20);
      this.type = 3;
      this.playerId = pv.getPlayerId();
      this.playerVotes = new PlayerVote[]{pv};
   }

   public WcVoting(long aPlayerId, PlayerVote[] pvs) {
      super(WurmId.getNextWCCommandId(), (short)20);
      this.type = 3;
      this.playerId = aPlayerId;
      this.playerVotes = pvs;
   }

   public WcVoting(int aQuestionId, short aVoteCount, short aCount1, short aCount2, short aCount3, short aCount4) {
      super(WurmId.getNextWCCommandId(), (short)20);
      this.type = 4;
      this.questionId = aQuestionId;
      this.voteCount = aVoteCount;
      this.count1 = aCount1;
      this.count2 = aCount2;
      this.count3 = aCount3;
      this.count4 = aCount4;
   }

   public WcVoting(byte aAction, int aQuestionId) {
      super(WurmId.getNextWCCommandId(), (short)20);
      this.type = aAction;
      this.questionId = aQuestionId;
   }

   public WcVoting(byte aAction, int aQuestionId, long when) {
      super(WurmId.getNextWCCommandId(), (short)20);
      this.type = aAction;
      this.questionId = aQuestionId;
      this.voteEnd = when;
   }

   public WcVoting(long aId, byte[] aData) {
      super(aId, (short)20, aData);
   }

   @Override
   public boolean autoForward() {
      return false;
   }

   @Override
   byte[] encode() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = null;
      byte[] barr = null;

      try {
         dos = new DataOutputStream(bos);
         dos.writeByte(this.type);
         switch(this.type) {
            case 1:
               dos.writeInt(this.questionId);
               dos.writeUTF(this.questionTitle);
               dos.writeUTF(this.questionText);
               dos.writeUTF(this.option1);
               dos.writeUTF(this.option2);
               dos.writeUTF(this.option3);
               dos.writeUTF(this.option4);
               dos.writeBoolean(this.allowMultiple);
               dos.writeBoolean(this.premOnly);
               dos.writeBoolean(this.jk);
               dos.writeBoolean(this.mr);
               dos.writeBoolean(this.hots);
               dos.writeBoolean(this.freedom);
               dos.writeLong(this.voteStart);
               dos.writeLong(this.voteEnd);
               break;
            case 2:
               dos.writeLong(this.playerId);
               dos.writeInt(this.questionIds.length);

               for(int qId : this.questionIds) {
                  dos.writeInt(qId);
               }
               break;
            case 3:
               dos.writeLong(this.playerId);
               dos.writeInt(this.playerVotes.length);

               for(PlayerVote pv : this.playerVotes) {
                  dos.writeInt(pv.getQuestionId());
                  dos.writeBoolean(pv.getOption1());
                  dos.writeBoolean(pv.getOption2());
                  dos.writeBoolean(pv.getOption3());
                  dos.writeBoolean(pv.getOption4());
               }
               break;
            case 4:
               dos.writeInt(this.questionId);
               dos.writeShort(this.voteCount);
               dos.writeShort(this.count1);
               dos.writeShort(this.count2);
               dos.writeShort(this.count3);
               dos.writeShort(this.count4);
               break;
            case 5:
               dos.writeInt(this.questionId);
               break;
            case 6:
               dos.writeInt(this.questionId);
               dos.writeLong(this.voteEnd);
         }

         dos.flush();
         dos.close();
      } catch (Exception var11) {
         logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
      } finally {
         StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
         barr = bos.toByteArray();
         StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
         this.setData(barr);
      }

      return barr;
   }

   @Override
   public void execute() {
      (new Thread() {
            @Override
            public void run() {
               DataInputStream dis = null;
   
               label137: {
                  try {
                     dis = new DataInputStream(new ByteArrayInputStream(WcVoting.this.getData()));
                     WcVoting.this.type = dis.readByte();
                     switch(WcVoting.this.type) {
                        case 1:
                           WcVoting.this.questionId = dis.readInt();
                           WcVoting.this.questionTitle = dis.readUTF();
                           WcVoting.this.questionText = dis.readUTF();
                           WcVoting.this.option1 = dis.readUTF();
                           WcVoting.this.option2 = dis.readUTF();
                           WcVoting.this.option3 = dis.readUTF();
                           WcVoting.this.option4 = dis.readUTF();
                           WcVoting.this.allowMultiple = dis.readBoolean();
                           WcVoting.this.premOnly = dis.readBoolean();
                           WcVoting.this.jk = dis.readBoolean();
                           WcVoting.this.mr = dis.readBoolean();
                           WcVoting.this.hots = dis.readBoolean();
                           WcVoting.this.freedom = dis.readBoolean();
                           WcVoting.this.voteStart = dis.readLong();
                           WcVoting.this.voteEnd = dis.readLong();
                           break label137;
                        case 2:
                           WcVoting.this.playerId = dis.readLong();
                           WcVoting.this.questionIds = new int[dis.readInt()];
                           int i = 0;
   
                           while(true) {
                              if (i >= WcVoting.this.questionIds.length) {
                                 break label137;
                              }
   
                              WcVoting.this.questionIds[i] = dis.readInt();
                              ++i;
                           }
                        case 3:
                           WcVoting.this.playerId = dis.readLong();
                           WcVoting.this.playerVotes = new PlayerVote[dis.readInt()];
                           int i = 0;
   
                           while(true) {
                              if (i >= WcVoting.this.playerVotes.length) {
                                 break label137;
                              }
   
                              PlayerVote pv = new PlayerVote(
                                 WcVoting.this.playerId, dis.readInt(), dis.readBoolean(), dis.readBoolean(), dis.readBoolean(), dis.readBoolean()
                              );
                              WcVoting.this.playerVotes[i] = pv;
                              ++i;
                           }
                        case 4:
                           WcVoting.this.questionId = dis.readInt();
                           WcVoting.this.voteCount = dis.readShort();
                           WcVoting.this.count1 = dis.readShort();
                           WcVoting.this.count2 = dis.readShort();
                           WcVoting.this.count3 = dis.readShort();
                           WcVoting.this.count4 = dis.readShort();
                           break label137;
                        case 5:
                           WcVoting.this.questionId = dis.readInt();
                           break label137;
                        case 6:
                           WcVoting.this.questionId = dis.readInt();
                           WcVoting.this.voteEnd = dis.readLong();
                        default:
                           break label137;
                     }
                  } catch (IOException var12) {
                     WcVoting.logger.log(Level.WARNING, "Unpack exception " + var12.getMessage(), (Throwable)var12);
                  } finally {
                     StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                  }
   
                  return;
               }
   
               switch(WcVoting.this.type) {
                  case 1:
                     VoteQuestions.queueAddVoteQuestion(
                        WcVoting.this.questionId,
                        WcVoting.this.questionTitle,
                        WcVoting.this.questionText,
                        WcVoting.this.option1,
                        WcVoting.this.option2,
                        WcVoting.this.option3,
                        WcVoting.this.option4,
                        WcVoting.this.allowMultiple,
                        WcVoting.this.premOnly,
                        WcVoting.this.jk,
                        WcVoting.this.mr,
                        WcVoting.this.hots,
                        WcVoting.this.freedom,
                        WcVoting.this.voteStart,
                        WcVoting.this.voteEnd
                     );
                     break;
                  case 2:
                     if (Servers.isThisLoginServer()) {
                        Map<Integer, PlayerVote> pVotes = new ConcurrentHashMap<>();
   
                        for(int qId : WcVoting.this.questionIds) {
                           PlayerVote pv = PlayerVotes.getPlayerVoteByQuestion(WcVoting.this.playerId, qId);
                           if (pv != null && pv.hasVoted()) {
                              pVotes.put(qId, pv);
                           }
                        }
   
                        WcVoting wv = new WcVoting(WcVoting.this.playerId, pVotes.values().toArray(new PlayerVote[pVotes.size()]));
                        wv.sendToServer(WurmId.getOrigin(WcVoting.this.getWurmId()));
                     }
                     break;
                  case 3:
                     if (Servers.isThisLoginServer()) {
                        for(PlayerVote pv : WcVoting.this.playerVotes) {
                           PlayerVotes.addPlayerVote(pv, true);
                        }
                     }
   
                     try {
                        Player p = Players.getInstance().getPlayer(WcVoting.this.playerId);
                        p.setVotes(WcVoting.this.playerVotes);
                     } catch (NoSuchPlayerException var11) {
                     }
                     break;
                  case 4:
                     if (Servers.isThisLoginServer()) {
                        VoteQuestion vq = VoteQuestions.getVoteQuestion(WcVoting.this.questionId);
                        WcVoting wv = new WcVoting(
                           vq.getQuestionId(), vq.getVoteCount(), vq.getOption1Count(), vq.getOption2Count(), vq.getOption3Count(), vq.getOption4Count()
                        );
                        wv.sendToServer(WurmId.getOrigin(WcVoting.this.getWurmId()));
                     }
                     break;
                  case 5:
                     VoteQuestions.queueRemoveVoteQuestion(WcVoting.this.questionId);
                     break;
                  case 6:
                     VoteQuestions.queueCloseVoteQuestion(WcVoting.this.questionId, WcVoting.this.voteEnd);
               }
            }
         })
         .start();
   }
}
