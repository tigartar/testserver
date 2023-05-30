package com.wurmonline.server.questions;

import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class KingdomMembersQuestion extends Question implements QuestionTypes {
   private static final int WIDTH = 600;
   private static final int HEIGHT = 250;
   private static final boolean RESIZEABLE = true;
   private static final boolean CLOSEABLE = true;
   private static final int[] RGB = new int[]{200, 200, 200};
   private static final String kID = "kingdomId";
   private static final String selectGrp = "select1";
   private static final String winTitle = "Kingdom Members";
   private static final String question = "Member List";
   private static final int maxRows = 25;
   private static final String prev = "prev";
   private static final String next = "next";
   private static final String expel = "expel";
   private static final String appoint = "appoint";
   private static final String toExpel = "toexpel";
   private static final String confirmExpel = "confirmExpel";
   private static final String confirmAppoint = "confirmAppoint";
   private static final String gmtool = "gmtool";
   private static final String filterMe = "filterMe";
   private static final String filterText = "filterText";
   private static final String cyan = "66,200,200";
   private static final String green = "66,225,66";
   private static final String orange = "255,156,66";
   private static final String red = "255,66,66";
   private static final String white = "255,255,255";
   private static final String colSize = "200,16";
   private static final int cols = 3;
   private List<Kingdom> klist = new LinkedList<>();
   private int currentIndex = 0;
   private Kingdom kingdom = Kingdoms.getKingdom((byte)0);
   private Player player;
   private PlayerInfo playerInfo;
   private PlayerInfo[] members;
   private String filter = "*";

   public KingdomMembersQuestion(Creature aResponder, long aTarget, byte kingdomId) {
      super(aResponder, "Kingdom Members", "Member List", 131, aTarget);
      this.kingdom = Kingdoms.getKingdom(kingdomId);
   }

   public KingdomMembersQuestion(Creature aResponder, long aTarget, String kingdomName, byte kingdomId) {
      super(aResponder, "Kingdom Members [" + kingdomName + "]", "Member List for " + kingdomName, 131, aTarget);
      this.kingdom = Kingdoms.getKingdom(kingdomId);
      this.kingdom.loadAllMembers();
   }

   public KingdomMembersQuestion(Creature aResponder, long aTarget, Kingdom kingdom) {
      super(aResponder, "Kingdom Members [" + kingdom.getName() + "]", "Member List for " + kingdom.getName(), 131, aTarget);
      this.kingdom = kingdom;
   }

   KingdomMembersQuestion(KingdomMembersQuestion old, String aTitle, String aQuestion) {
      super(old.getResponder(), aTitle, aQuestion, 131, old.getTarget());
      this.kingdom = old.kingdom;
      this.player = old.player;
      this.members = old.members;
      this.klist = old.klist;
      this.currentIndex = old.currentIndex;
      this.filter = old.filter;
      this.playerInfo = old.playerInfo;
   }

   @Override
   public void answer(Properties answers) {
      String val = answers.getProperty("filterMe");
      if (val != null) {
         val = answers.getProperty("filterText");
         if (val != null) {
            this.filter = val;
            KingdomMembersQuestion kmq = new KingdomMembersQuestion(this, this.title, this.getQuestion());
            kmq.sendQuestion();
            return;
         }
      }

      if (this.getResponder().getPower() > 0) {
         val = answers.getProperty("kingdomId");
         if (val != null) {
            int i = Integer.parseInt(val);
            if (i == 0) {
               return;
            }

            this.kingdom = this.klist.get(i);
            if (this.kingdom == null) {
               return;
            }

            if (this.kingdom.getId() != 4) {
               KingdomMembersQuestion kmq = new KingdomMembersQuestion(this.getResponder(), this.target, this.kingdom);
               kmq.sendQuestion();
               return;
            }
         }

         val = answers.getProperty("gmtool");
         if (val != null) {
            val = answers.getProperty("select1");
            if (val == null) {
               return;
            }

            GmTool gmt = new GmTool(this.getResponder(), Long.parseLong(val));
            this.getResponder().getCommunicator().sendNormalServerMessage("Starting GM Tool for '" + val + "'");
            gmt.sendQuestion();
            return;
         }
      }

      val = answers.getProperty("next");
      if (val != null) {
         KingdomMembersQuestion kmq = new KingdomMembersQuestion(this.getResponder(), this.target, this.kingdom);
         kmq.currentIndex = this.currentIndex + 25;
         kmq.members = this.members;
         kmq.sendMemberList();
      } else {
         val = answers.getProperty("prev");
         if (val != null) {
            KingdomMembersQuestion kmq = new KingdomMembersQuestion(this.getResponder(), this.target, this.kingdom);
            kmq.currentIndex = this.currentIndex - 25;
            kmq.members = this.members;
            kmq.sendMemberList();
         } else {
            if (King.isKing(this.getResponder().getWurmId(), this.kingdom.getId())) {
               val = answers.getProperty("appoint");
               if (val != null) {
                  val = answers.getProperty("select1");
                  if (val == null) {
                     return;
                  }

                  long wurmId = Long.parseLong(val);
                  this.player = Players.getInstance().getPlayerOrNull(wurmId);
                  if (this.player == null) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("You can only appoint members that are online.", (byte)3);
                     return;
                  }

                  KingdomMembersQuestion kmq = new KingdomMembersQuestion(this, "Appoint " + this.player.getName(), "Appointing " + this.player.getName());
                  kmq.sendAppointWindow();
                  return;
               }

               val = answers.getProperty("expel");
               if (val != null) {
                  val = answers.getProperty("select1");
                  if (val == null) {
                     return;
                  }

                  long wurmId = Long.parseLong(val);
                  if (wurmId == this.getResponder().getWurmId()) {
                     this.getResponder()
                        .getCommunicator()
                        .sendNormalServerMessage("You are King, you cannot expel yourself. You must remove your crown and abdicate your throne.");
                     return;
                  }

                  this.playerInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
                  if (this.playerInfo == null) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("Unable to find that player.", (byte)3);
                     return;
                  }

                  KingdomMembersQuestion kmq = new KingdomMembersQuestion(this, "Expel " + this.playerInfo.getName(), "Expelling " + this.playerInfo.getName());
                  kmq.sendExpelConfirmation();
                  return;
               }

               val = answers.getProperty("confirmExpel");
               if (val != null) {
                  val = answers.getProperty("toexpel");
                  if (val.compareTo(this.playerInfo.getName()) == 0) {
                     this.kingdom.expelMember(this.getResponder(), this.playerInfo.getName());
                     return;
                  }
               }

               val = answers.getProperty("confirmAppoint");
               if (val != null) {
                  King king = King.getKing(this.kingdom.getId());
                  if (king == null) {
                     return;
                  }

                  Appointments apps = Appointments.getAppointments(king.era);
                  if (apps == null) {
                     return;
                  }

                  answers.forEach((k, v) -> this.handleAppointment(k.toString(), v.toString(), apps));
                  return;
               }
            }
         }
      }
   }

   private void handleAppointment(String k, String v, Appointments apps) {
      if (!k.toString().contains("confirmAppoint") && !k.toString().contains("id")) {
         int aId = Integer.valueOf(k);
         boolean isSet = Boolean.valueOf(v);
         Appointment a = apps.getAppointment(aId);
         if (a != null) {
            if (a.getType() == 2 && apps.officials[aId - 1500] == this.player.getWurmId()) {
               if (isSet) {
                  return;
               }

               this.player
                  .getCommunicator()
                  .sendNormalServerMessage(
                     "You are hereby notified that you have been removed of the office as " + a.getNameForGender(this.player.getSex()) + ".", (byte)2
                  );
               this.getResponder().getCommunicator().sendNormalServerMessage("You vacate the office of " + a.getNameForGender((byte)0) + ".", (byte)2);
               apps.setOfficial(aId, 0L);
            } else if (isSet) {
               this.player.addAppointment(a, this.getResponder());
            }
         }
      }
   }

   @Override
   public void sendQuestion() {
      if (Servers.localServer.PVPSERVER && this.kingdom.getId() != 4) {
         if (this.getResponder().getPower() <= 0) {
            if (this.kingdom.getId() != this.getResponder().getKingdomId()) {
               this.getResponder().getCommunicator().sendNormalServerMessage("You may only view members in your own kingdom.");
               return;
            }

            if (!this.kingdom.isCustomKingdom()) {
               this.getResponder().getCommunicator().sendNormalServerMessage("Only custom kingdoms may view their member roster.");
               return;
            }
         } else if (this.kingdom.getId() == 0 && this.getResponder().getPower() > 0) {
            this.sendKingdomList();
            return;
         }

         this.sendMemberList();
      } else {
         this.getResponder().getCommunicator().sendNormalServerMessage("You may only list members in kingdoms on a PvP server.");
      }
   }

   private void sendKingdomList() {
      StringBuilder buf = new StringBuilder(this.getHeader("Member List"));
      buf.append("harray{label{text='Select kingdom:'};");
      buf.append("dropdown{id='kingdomId'; options='None,");
      Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
      this.klist.add(Kingdoms.getKingdom((byte)0));

      for(int i = 0; i < kingdoms.length; ++i) {
         if (kingdoms[i].getId() != 4 && kingdoms[i].getId() != 0) {
            buf.append(kingdoms[i].getName() + ",");
            this.klist.add(kingdoms[i]);
         }
      }

      buf.append("'}} text{text='Note: Freedom is too large for member lists.'}; text{text=''};");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(250, 150, true, true, buf.toString(), RGB[0], RGB[1], RGB[2], "Kingdom Members");
   }

   private PlayerInfo[] filterList(PlayerInfo[] memberArray) {
      ArrayList<PlayerInfo> memberList = new ArrayList<>();

      for(int x = 0; x < memberArray.length; ++x) {
         if (PlayerInfoFactory.wildCardMatch(memberArray[x].getName(), this.filter)) {
            memberList.add(memberArray[x]);
         }
      }

      memberList.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
      return memberList.toArray(new PlayerInfo[memberList.size()]);
   }

   private void sendMemberList() {
      if (this.kingdom != null && this.kingdom.getId() != 4) {
         this.members = this.filterList(this.kingdom.getAllMembers());
         if (this.members.length == 0) {
            this.getResponder().getCommunicator().sendNormalServerMessage("There are no members to list.");
         } else {
            StringBuilder buf = new StringBuilder(this.getHeader(this.kingdom.getName()));
            int rows = this.members.length - this.currentIndex < 25 ? this.members.length - this.currentIndex : 25;
            buf.append(
               "harray{label{text=\"Filter by: \"}; input{maxchars=\"20\";id=\"filterText\";text=\""
                  + this.filter
                  + "\"; onenter='"
                  + "filterMe"
                  + "'};button{text='Filter'; id='"
                  + "filterMe"
                  + "'};label{text=' (Use * as a wildcard)'};}"
            );
            buf.append("text{text=''};");
            buf.append("table{rows='" + rows + "'; cols='5'; ");
            buf.append(
               "label{text=''}; label{type='bold'; text='Name'}; label{type='bold'; text='Last On'}; label{type='bold'; text='Member Since'}; label{type='bold'; text='Village'}; "
            );

            for(int x = 0; x < rows && this.currentIndex + x < this.members.length; ++x) {
               this.appendMember(x, buf);
            }

            buf.append("}");
            buf.append(
               "text{text=''};harray{label{type='bold'; text='Total Members: '};label{text='"
                  + this.kingdom.getAllMembers().length
                  + "'};label{text=''};label{type='bold'; text='Premium: '};label{text='"
                  + this.kingdom.getPremiumMemberCount()
                  + "'};}"
            );
            this.createActionButtons(buf);
            buf.append(this.createPageAndCloseButtons(rows - 1));
            this.getResponder().getCommunicator().sendBml(600, 250 + rows * 13, true, true, buf.toString(), RGB[0], RGB[1], RGB[2], "Kingdom Members");
         }
      } else {
         this.getResponder().getCommunicator().sendNormalServerMessage("Unable to show member list for that kingdom.");
      }
   }

   private final void appendMember(int x, StringBuilder buf) {
      boolean isOn = PlayerInfoFactory.isPlayerOnline(this.members[this.currentIndex + x].wurmId);
      String lastOnString = Server.getTimeFor(System.currentTimeMillis() - this.members[this.currentIndex + x].getLastLogout());
      String memberSinceString = WurmCalendar.formatGmt(this.members[this.currentIndex + x].lastChangedKindom);
      Village village = Villages.getVillageForCreature(this.members[this.currentIndex + x].wurmId);
      String color = "255,255,255";
      String villageName = village == null ? "None" : village.getName();
      String hover = "";
      if (village != null && village.isMayor(this.members[this.currentIndex + x].wurmId)) {
         villageName = villageName + " (Mayor)";
      }

      if (King.isKing(this.members[this.currentIndex + x].wurmId, this.kingdom.getId())) {
         King k = King.getKing(this.kingdom.getId());
         hover = "hover=\"" + k.getFullTitle() + "\";";
         color = "66,200,200";
      }

      buf.append("radio{id='" + this.members[this.currentIndex + x].wurmId + "'; group='" + "select1" + "'};");
      buf.append("label{color='" + color + "'; " + hover + " text='" + this.members[this.currentIndex + x].getName() + "'};");
      if (isOn) {
         buf.append("label{type='bold'; color='66,225,66'; text='Online'};");
      } else {
         buf.append("label{text='" + lastOnString + "'};");
      }

      buf.append("label{text='" + memberSinceString + "'};");
      buf.append("label{text='" + villageName + "'};");
   }

   private void createActionButtons(StringBuilder buf) {
      if (this.getResponder().getPower() >= 2) {
         buf.append("text{text=''}; left{ harray{ text{text=''} button{text='GM Tool'; id='gmtool'};");
      } else if (this.kingdom.isCustomKingdom() && King.isKing(this.getResponder().getWurmId(), this.kingdom.getId())) {
         buf.append("text{text=''}; left{ harray{ text{text=''} button{text='Appoint'; id='appoint'}; ");
         if (this.kingdom.getAllMembers().length > 10 || Servers.localServer.testServer) {
            buf.append("text{text=''} button{text='Expel'; id='expel'};");
         }
      }

      buf.append("}}");
   }

   private final String createPageAndCloseButtons(int lRow) {
      if (this.members == null) {
         return "";
      } else {
         StringBuilder buf = new StringBuilder("}}null;right{ harray{");
         if (this.currentIndex + lRow > 26) {
            buf.append("button{text='Previous'; id='prev'}; label{text=''};");
         }

         if (this.currentIndex + lRow < this.members.length - 1) {
            buf.append("button{text='Next'; id='next'}; label{text=''};");
         }

         buf.append("button{text='Close'; id='close'}; label{text=''}; label{text=''};}}}");
         return buf.toString();
      }
   }

   private final String getHeader(String header) {
      return "border{center{header{text=\""
         + header
         + "\"}};null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
         + this.id
         + "\"}";
   }

   private void sendExpelConfirmation() {
      if (this.playerInfo != null) {
         StringBuilder buf = new StringBuilder(this.getHeader("Expel Member"));
         buf.append("text{text=''}center{header{text='" + this.playerInfo.getName() + "'};}text{text=''};");
         buf.append("center{label{text=\"Type the person's name exactly as shown to confirm.\"};}text{text=''};");
         buf.append("center{harray{label{text=''}input{id='toexpel'; maxchars='25'; text=''}label{text=''}}}text{text=''};");
         buf.append("harray{button{id='confirmExpel'; text='Confirm'};label{text=''};button{id='cancel'; text='Cancel'};}}}null;null;}");
         this.getResponder().getCommunicator().sendBml(320, 225, true, true, buf.toString(), RGB[0], RGB[1], RGB[2], "Kingdom Members");
      }
   }

   private void sendAppointWindow() {
      if (this.player != null) {
         if (!King.isKing(this.getResponder().getWurmId(), this.kingdom.getId())) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Only the ruler may appoint subjects.");
         } else {
            StringBuilder buf = new StringBuilder(this.getHeader("Appoint " + this.player.getName()));
            King king = King.getKing(this.kingdom.getId());
            Appointments a = Appointments.getAppointments(king.era);
            long timeLeft = a.getResetTimeRemaining();
            if (timeLeft <= 0L) {
               buf.append("center{varray{label{color=\"66,200,200\"; text=\"Titles and orders will refresh shortly.\"}}};");
            } else {
               buf.append("center{varray{label{color=\"66,200,200\"; text=\"Titles and orders will refresh in " + Server.getTimeFor(timeLeft) + ".\"}}}");
            }

            buf.append("text{text=''};");
            buf.append(
               "table{rows='5';  cols='6'; label{text=''};label{size=\"200,16\"; text=''}; label{text=''};label{type='bold'; size=\"200,16\"; text='Titles'}; label{text=''};label{size=\"200,16\"; text=''};label{text=''};label{text=''};label{text=''};label{text=''};label{text=''};label{text=''};"
            );
            this.addTitleStrings(a, buf);
            buf.append("};text{text=''};");
            buf.append(
               "table{rows='4';  cols='6'; label{text=''};label{size=\"200,16\"; text=''}; label{text=''};label{type='bold'; size=\"200,16\"; text='Orders & Decorations'}; label{text=''};label{size=\"200,16\"; text=''};label{text=''};label{text=''};label{text=''};label{text=''};label{text=''};label{text=''};"
            );
            this.addOrderStrings(a, buf);
            buf.append("};text{text=''};");
            buf.append(
               "table{rows='6';  cols='6'; label{text=''};label{size=\"200,16\"; text=''}; label{text=''};label{type='bold'; size='200,16'; text='Offices'}; label{text=''};label{size=\"200,16\"; text=''};label{text=''};label{text=''};label{text=''};label{text=''};label{text=''};label{text=''};"
            );
            this.addOfficeStrings(a, buf);
            buf.append("};text{text=''};label{color='66,225,66'; text='Green indicates member already has this appointment'};");
            buf.append("label{color='255,156,66'; text='Orange indicates an office may be set but is occupied.'};");
            buf.append("label{color='255,66,66'; text='Red indicates appointment is on cool down.'};text{text=''};");
            buf.append("harray{button{id='confirmAppoint'; text='Appoint'};label{text=''};button{id='cancel'; text='Cancel'};}}}null;null;}");
            this.getResponder().getCommunicator().sendBml(620, 460, true, true, buf.toString(), RGB[0], RGB[1], RGB[2], "Kingdom Members");
         }
      }
   }

   private void addTitleStrings(Appointments a, StringBuilder buf) {
      for(int x = 0; x < a.availableTitles.length; ++x) {
         if (Appointments.getMaxAppointment(this.kingdom.getId(), x) != 0) {
            Appointment t = a.getAppointment(x);
            if (this.player.hasAppointment(t.getId())) {
               buf.append("label{text=''}; label{color='66,225,66'; text=\"" + t.getNameForGender(this.player.getSex()) + "\"};");
            } else if (a.getAvailTitlesForId(x) > 0) {
               buf.append("checkbox{id='" + x + "'}; label{text=\"" + t.getNameForGender(this.player.getSex()) + "(" + a.getAvailTitlesForId(x) + ")\"};");
            } else {
               buf.append("label{text=''}; label{color='255,66,66'; text=\"" + t.getNameForGender(this.player.getSex()) + "\"};");
            }
         }
      }

      if (a.availableTitles.length % 3 != 0) {
         for(int x = 0; x < 3 - a.availableTitles.length % 3; ++x) {
            buf.append("label{text=''};");
         }
      }
   }

   private void addOrderStrings(Appointments a, StringBuilder buf) {
      for(int x = 0; x < a.availableOrders.length; ++x) {
         if (Appointments.getMaxAppointment(this.kingdom.getId(), x) != 0) {
            int oId = x + 30;
            Appointment o = a.getAppointment(oId);
            if (this.player.hasAppointment(o.getId())) {
               buf.append("label{text=''}; label{color='66,225,66'; text=\"" + o.getNameForGender(this.player.getSex()) + "\"};");
            } else if (a.getAvailOrdersForId(oId) > 0) {
               buf.append("checkbox{id='" + oId + "'}; label{text=\"" + o.getNameForGender(this.player.getSex()) + "(" + a.getAvailOrdersForId(oId) + ")\"};");
            } else {
               buf.append("label{text=''}; label{color='255,66,66'; text=\"" + o.getNameForGender(this.player.getSex()) + "\"};");
            }
         }
      }

      if (a.availableOrders.length % 3 != 0) {
         for(int x = 0; x < 3 - a.availableOrders.length % 3; ++x) {
            buf.append("label{text=''};");
         }
      }
   }

   private void addOfficeStrings(Appointments a, StringBuilder buf) {
      for(int x = 0; x < a.officials.length; ++x) {
         int oId = x + 1500;
         Appointment o = a.getAppointment(oId);
         if (a.officials[x] == this.player.getWurmId()) {
            if (a.isOfficeSet(oId)) {
               buf.append("label{text=''}; label{color='66,225,66'; text=\"" + o.getNameForGender(this.player.getSex()) + "\"};");
            } else {
               String conf = "Are you sure you want to remove " + this.player.getName() + " from this office?";
               buf.append(
                  "checkbox{id='"
                     + oId
                     + "'; unconfirm=''; unquestion=\""
                     + conf
                     + "\"; selected='true'}; label{color='"
                     + "66,225,66"
                     + "'; text=\""
                     + o.getNameForGender(this.player.getSex())
                     + "\"};"
               );
            }
         } else if (a.isOfficeSet(oId)) {
            String oName = PlayerInfoFactory.getPlayerName(a.officials[x]);
            buf.append(
               "label{text=''}; label{color='255,66,66'; hover=\"Current: " + oName + "\"; text=\"" + o.getNameForGender(this.player.getSex()) + "\"};"
            );
         } else if (a.officials[x] > 0L) {
            String oName = PlayerInfoFactory.getPlayerName(a.officials[x]);
            String conf = "Are you sure you want to remove " + oName + " from this office?";
            buf.append(
               "checkbox{id='"
                  + oId
                  + "'; hover=\"Current: "
                  + oName
                  + "\"; confirm=''; question=\""
                  + conf
                  + "\"}; label{color='"
                  + "255,156,66"
                  + "'; hover=\"Current: "
                  + oName
                  + "\"; text=\""
                  + o.getNameForGender(this.player.getSex())
                  + "\"};"
            );
         } else {
            buf.append("checkbox{id='" + oId + "'}; label{text=\"" + o.getNameForGender(this.player.getSex()) + "\"};");
         }
      }

      if (a.officials.length % 3 != 0) {
         for(int x = 0; x < 3 - a.officials.length % 3; ++x) {
            buf.append("label{text=''};");
         }
      }
   }
}
