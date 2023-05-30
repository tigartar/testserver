package com.wurmonline.server.webinterface;

import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;

public abstract class WebCommand {
   private final long id;
   private byte[] data;
   private final short type;
   public static final short WC_TYPE_NONE = 0;
   public static final short WC_TYPE_GM_MESSAGE = 1;
   public static final short WC_TYPE_SERVER_MESSAGE = 2;
   public static final short WC_TYPE_DEMOTION = 3;
   public static final short WC_TYPE_DELETION = 4;
   public static final short WC_TYPE_REFRESHPINF = 5;
   public static final short WC_TYPE_RESET = 6;
   public static final short WC_TYPE_KINGDOMINFO = 7;
   public static final short WC_TYPE_KINGDOMDELETE = 8;
   public static final short WC_TYPE_EPICEVENT = 9;
   public static final short WC_TYPE_EPICSTATUS = 10;
   public static final short WC_TYPE_EPICSCENARIO = 11;
   public static final short WC_TYPE_OPENEPIC = 12;
   public static final short WC_TYPE_KINGDOMCHAT = 13;
   public static final short WC_TYPE_GLOBALMODERATION = 14;
   public static final short WC_TYPE_GLOBALIGNORE = 15;
   public static final short WC_TYPE_KARMA = 16;
   public static final short WC_TYPE_GLOBALPM = 17;
   public static final short WC_TYPE_TICKET = 18;
   public static final short WC_TYPE_PLAYER_STATUS = 19;
   public static final short WC_TYPE_VOTING = 20;
   public static final short WC_TYPE_SPAWNS = 21;
   public static final short WC_TYPE_GLOBAL_ALARM = 22;
   public static final short WC_TYPE_CAHELPGROUP = 23;
   public static final short WC_TYPE_MGMT_MESSAGE = 24;
   public static final short WC_TYPE_ADD_FRIEND = 25;
   public static final short WC_TYPE_REDEEM_KEY = 26;
   public static final short WC_TYPE_VALREI_MAP_UPDATE = 27;
   public static final short WC_TYPE_TRADECHANNEL = 28;
   public static final short WC_TYPE_GV_HELP = 29;
   public static final short WC_TYPE_EXPEL_MEMBER = 30;
   public static final short WC_TYPE_TAB_LISTS = 31;
   public static final short WC_TYPE_TRELLO_HIGHWAY = 32;
   public static final short WC_TYPE_SET_POWER = 33;
   public static final short WC_TYPE_GET_HEROES = 34;
   public static final short WC_TYPE_TRELLO_DEATHS = 35;
   public static final short WC_DESTROY_CREATURE = 36;
   boolean isRestrictedEpic = false;

   WebCommand(long _id) {
      this.id = _id;
      this.type = 0;
   }

   WebCommand(long _id, short _type) {
      this.id = _id;
      this.type = _type;
   }

   WebCommand(long _id, short _type, byte[] _data) {
      this.id = _id;
      this.type = _type;
      this.data = _data;
   }

   public final short getType() {
      return this.type;
   }

   public boolean isEpicOnly() {
      return this.isRestrictedEpic;
   }

   final void setData(byte[] _data) {
      this.data = _data;
   }

   public final byte[] getData() {
      return this.data;
   }

   public final long getWurmId() {
      return this.id;
   }

   abstract byte[] encode();

   public abstract boolean autoForward();

   public abstract void execute();

   static final WebCommand createWebCommand(short wctype, long id, byte[] data) {
      switch(wctype) {
         case 1:
            return new WCGmMessage(id, data);
         case 2:
         default:
            return null;
         case 3:
            return new WcDemotion(id, data);
         case 4:
            return new WcRemoveFriendship(id, data);
         case 5:
            return new WcRefreshCommand(id, data);
         case 6:
            return new WcResetCommand(id, data);
         case 7:
            return new WcKingdomInfo(id, data);
         case 8:
            return new WcDeleteKingdom(id, data);
         case 9:
            return new WcEpicEvent(id, data);
         case 10:
            return new WcEpicStatusReport(id, data);
         case 11:
            return new WcCreateEpicMission(id, data);
         case 12:
            return new WcOpenEpicPortal(id, data);
         case 13:
            return new WcKingdomChat(id, data);
         case 14:
            return new WcGlobalModeration(id, data);
         case 15:
            return new WcGlobalIgnore(id, data);
         case 16:
            return new WcEpicKarmaCommand(id, data);
         case 17:
            return new WcGlobalPM(id, data);
         case 18:
            return new WcTicket(id, data);
         case 19:
            return new WcPlayerStatus(id, data);
         case 20:
            return new WcVoting(id, data);
         case 21:
            return new WcSpawnPoints(id, data);
         case 22:
            return new WcGlobalAlarmMessage(id, data);
         case 23:
            return new WcCAHelpGroupMessage(id, data);
         case 24:
            return new WcMgmtMessage(id, data);
         case 25:
            return new WcAddFriend(id, data);
         case 26:
            return new WcRedeemKey(id, data);
         case 27:
            return new WCValreiMapUpdater(id, data);
         case 28:
            return new WcTradeChannel(id, data);
         case 29:
            return new WcGVHelpMessage(id, data);
         case 30:
            return new WcExpelMember(id, data);
         case 31:
            return new WcTabLists(id, data);
         case 32:
            return new WcTrelloHighway(id, data);
         case 33:
            return new WcSetPower(id, data);
         case 34:
            return new WcGetHeroes(id, data);
         case 35:
            return new WcTrelloDeaths(id, data);
         case 36:
            return new WcKillCommand(id, data);
      }
   }

   public final int getOriginServer() {
      return WurmId.getOrigin(this.id);
   }

   public final void sendToServer(int serverId) {
      this.encode();
      if (serverId == Servers.localServer.id) {
         this.execute();
      } else {
         LoginServerWebConnection lsw = new LoginServerWebConnection(serverId);
         lsw.sendWebCommand(this.type, this);
      }
   }

   public final void sendToLoginServer() {
      this.encode();
      if (Servers.localServer.LOGINSERVER) {
         this.execute();
      } else {
         LoginServerWebConnection lsw = new LoginServerWebConnection();
         lsw.sendWebCommand(this.type, this);
      }
   }

   public final void sendFromLoginServer() {
      this.encode();
      Servers.sendWebCommandToAllServers(this.type, this, this.isEpicOnly());
      if (Servers.localServer.EPIC && this.isEpicOnly() || this.getType() == 10 || this.getType() == 27) {
         this.execute();
      }
   }
}
