package com.wurmonline.server.epic;

import com.wurmonline.server.Features;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.webinterface.WCValreiMapUpdater;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ValreiMapData {
   private static Map<Long, ValreiMapData> enteties = new ConcurrentHashMap<>();
   static long lastPolled = 0L;
   static long lastUpdatedTime = 0L;
   private static boolean hasInitialData = false;
   private static boolean firstRequest = true;
   private static final Logger logger = Logger.getLogger(ValreiMapData.class.getName());
   private long entityId;
   private int hexId;
   private int type;
   private int targetHexId;
   private String name;
   private long timeRemaining;
   private float attack;
   private float vitality;
   private float bodyStr;
   private float bodySta;
   private float bodyCon;
   private float mindLog;
   private float mindSpe;
   private float soulStr;
   private float soulDep;
   private boolean dirty = false;
   private boolean dirtyTime = false;
   private List<CollectedValreiItem> carriedItems = null;

   public ValreiMapData(
      long _entityId,
      int _hexId,
      int _type,
      int _targetHexId,
      String _name,
      long _remainingTime,
      float _bodyStr,
      float _bodySta,
      float _bodyCon,
      float _mindLog,
      float _mindSpe,
      float _soulStr,
      float _soulDep,
      List<CollectedValreiItem> _carried
   ) {
      this.entityId = _entityId;
      this.hexId = _hexId;
      this.type = _type;
      this.targetHexId = _targetHexId;
      this.name = _name;
      this.timeRemaining = _remainingTime;
      this.bodyStr = _bodyStr;
      this.bodySta = _bodySta;
      this.bodyCon = _bodyCon;
      this.mindLog = _mindLog;
      this.mindSpe = _mindSpe;
      this.soulStr = _soulStr;
      this.soulDep = _soulDep;
      this.carriedItems = _carried;
   }

   public static synchronized void updateEntity(
      long _entityId,
      int _hexId,
      int _type,
      int _targetHexId,
      String _name,
      long _remainingTime,
      float _bodyStr,
      float _bodySta,
      float _bodyCon,
      float _mindLog,
      float _mindSpe,
      float _soulStr,
      float _soulDep,
      List<CollectedValreiItem> _carried
   ) {
      ValreiMapData entity = enteties.get(_entityId);
      if (entity == null) {
         enteties.put(
            _entityId,
            new ValreiMapData(
               _entityId,
               _hexId,
               _type,
               _targetHexId,
               _name,
               _remainingTime,
               _bodyStr,
               _bodySta,
               _bodyCon,
               _mindLog,
               _mindSpe,
               _soulStr,
               _soulDep,
               (List<CollectedValreiItem>)(_carried != null ? _carried : new ArrayList<>())
            )
         );
      } else {
         entity.setEntityId(_entityId);
         entity.setHexId(_hexId);
         entity.setType(_type);
         entity.setTargetHexId(_targetHexId);
         entity.setName(_name);
         entity.setBodyStr(_bodyStr);
         entity.setBodySta(_bodySta);
         entity.setBodyCon(_bodyCon);
         entity.setMindLog(_mindLog);
         entity.setMindSpe(_mindSpe);
         entity.setSoulStr(_soulStr);
         entity.setSoulDep(_soulDep);
         entity.setCarried(_carried);
         entity.setTimeRemaining(_remainingTime);
      }
   }

   public static synchronized void updateEntityTime(long _entityId, long _time) {
      ValreiMapData entity = enteties.get(_entityId);
      if (entity != null) {
         entity.setTimeRemaining(_time);
      }
   }

   public final synchronized long getEntityId() {
      return this.entityId;
   }

   public final synchronized int getHexId() {
      return this.hexId;
   }

   public final synchronized int getType() {
      return this.type;
   }

   public final synchronized String getName() {
      return this.name;
   }

   public final synchronized long getTimeRemaining() {
      return this.timeRemaining;
   }

   public final synchronized float getAttack() {
      return this.attack;
   }

   public final synchronized float getVitality() {
      return this.vitality;
   }

   public final synchronized int getTargetHexId() {
      return this.targetHexId;
   }

   public final synchronized List<CollectedValreiItem> getCarried() {
      return this.carriedItems;
   }

   public synchronized void setEntityId(long id) {
      if (this.entityId != id) {
         this.entityId = id;
         this.onChanged();
      }
   }

   public synchronized void setHexId(int id) {
      if (this.hexId != id) {
         this.hexId = id;
         this.onChanged();
      }
   }

   public synchronized void setType(int newType) {
      if (this.type != newType) {
         this.type = newType;
         this.onChanged();
      }
   }

   public synchronized void setTargetHexId(int newTarget) {
      if (this.targetHexId != newTarget) {
         this.targetHexId = newTarget;
         this.onChanged();
      }
   }

   public synchronized void setName(String newName) {
      if (!this.name.equals(newName)) {
         this.name = newName;
         this.onChanged();
      }
   }

   public synchronized void setTimeRemaining(long remaining) {
      if (this.timeRemaining != remaining) {
         this.timeRemaining = remaining;
         this.onTimeChanged();
      }
   }

   public synchronized void setAttack(float newAttack) {
      if (this.attack != newAttack) {
         this.attack = newAttack;
         this.onChanged();
      }
   }

   public synchronized void setVitality(float newVitality) {
      if (this.vitality != newVitality) {
         this.vitality = newVitality;
         this.onChanged();
      }
   }

   public void setBodyStr(float bodyStr) {
      if (this.bodyStr != bodyStr) {
         this.bodyStr = bodyStr;
         this.onChanged();
      }
   }

   public void setBodySta(float bodySta) {
      if (this.bodySta != bodySta) {
         this.bodySta = bodySta;
         this.onChanged();
      }
   }

   public void setBodyCon(float bodyCon) {
      if (this.bodyCon != bodyCon) {
         this.bodyCon = bodyCon;
         this.onChanged();
      }
   }

   public void setMindLog(float mindLog) {
      if (this.mindLog != mindLog) {
         this.mindLog = mindLog;
         this.onChanged();
      }
   }

   public void setMindSpe(float mindSpe) {
      if (this.mindSpe != mindSpe) {
         this.mindSpe = mindSpe;
         this.onChanged();
      }
   }

   public void setSoulStr(float soulStr) {
      if (this.soulStr != soulStr) {
         this.soulStr = soulStr;
         this.onChanged();
      }
   }

   public void setSoulDep(float soulDep) {
      if (this.soulDep != soulDep) {
         this.soulDep = soulDep;
         this.onChanged();
      }
   }

   public float getBodyStr() {
      return this.bodyStr;
   }

   public float getBodySta() {
      return this.bodySta;
   }

   public float getBodyCon() {
      return this.bodyCon;
   }

   public float getMindLog() {
      return this.mindLog;
   }

   public float getMindSpe() {
      return this.mindSpe;
   }

   public float getSoulStr() {
      return this.soulStr;
   }

   public float getSoulDep() {
      return this.soulDep;
   }

   public synchronized void setCarried(List<CollectedValreiItem> _carried) {
      if (this.carriedItems != _carried || !this.carriedItems.containsAll(_carried)) {
         this.carriedItems = _carried;
         this.onChanged();
      }
   }

   private void onChanged() {
      this.dirty = true;
   }

   private void onTimeChanged() {
      this.dirtyTime = true;
   }

   public void toggleNotDirty() {
      this.dirty = false;
   }

   public void toggleTimeNotDirty() {
      this.dirtyTime = false;
   }

   public final boolean isDirty() {
      return this.dirty;
   }

   public final boolean isTimeDirty() {
      return this.dirtyTime;
   }

   public final boolean isCollectable() {
      return this.type == 2;
   }

   public final boolean isSourceItem() {
      return this.type == 1;
   }

   public final boolean isDemiGod() {
      return this.type == 7;
   }

   public final boolean isSentinel() {
      return this.type == 5;
   }

   public final boolean isAlly() {
      return this.type == 6;
   }

   public final boolean isItem() {
      return this.isCollectable() || this.isSourceItem();
   }

   public final boolean isCustomGod() {
      if (!this.isItem() && !this.isDemiGod()) {
         return this.entityId > 100L;
      } else {
         return false;
      }
   }

   public static synchronized void setHasInitialData() {
      hasInitialData = true;
      lastUpdatedTime = System.currentTimeMillis();
   }

   public static final synchronized boolean hasInitialData() {
      return hasInitialData;
   }

   public static void pollValreiData() {
      if (Features.Feature.VALREI_MAP.isEnabled()) {
         long now = System.currentTimeMillis();
         if (lastPolled != 0L && lastUpdatedTime != 0L) {
            long elapsed = now - lastPolled;
            long elapsedSinceTimeUpdate = now - lastUpdatedTime;
            if (!hasInitialData()) {
               if (firstRequest || elapsed > 600000L) {
                  lastPolled = now;
                  firstRequest = false;
                  if (!Servers.localServer.LOGINSERVER) {
                     WCValreiMapUpdater updater = new WCValreiMapUpdater(WurmId.getNextWCCommandId(), (byte)0);
                     updater.sendToLoginServer();
                  } else {
                     collectLocalData();
                     setHasInitialData();
                  }
               }
            } else {
               if (elapsedSinceTimeUpdate > 2400000L) {
                  if (!Servers.localServer.LOGINSERVER) {
                     WCValreiMapUpdater updater = new WCValreiMapUpdater(WurmId.getNextWCCommandId(), (byte)3);
                     updater.sendToLoginServer();
                  } else {
                     updateTimeData();
                  }
               }

               if (elapsed >= 1800000L) {
                  lastPolled = now;
                  Player[] players = Players.getInstance().getPlayers();

                  for(Player player : players) {
                     boolean sent = false;

                     for(ValreiMapData data : enteties.values()) {
                        if (data.isDirty() || !player.hasReceivedInitialValreiData) {
                           player.getCommunicator().sendValreiMapData(data);
                           sent = true;
                        } else if (!data.isItem() && data.isTimeDirty()) {
                           player.getCommunicator().sendValreiMapDataTimeUpdate(data);
                           sent = true;
                        }
                     }

                     if (sent && !player.hasReceivedInitialValreiData) {
                        player.hasReceivedInitialValreiData = true;
                     }
                  }

                  for(ValreiMapData data : enteties.values()) {
                     if (data.isDirty()) {
                        data.toggleNotDirty();
                        data.toggleTimeNotDirty();
                     } else if (data.isTimeDirty()) {
                        data.toggleTimeNotDirty();
                     }
                  }
               }
            }
         } else {
            lastPolled = now;
            lastUpdatedTime = now;
         }
      }
   }

   public static void sendAllMapData(Player player) {
      if (player.hasReceivedInitialValreiData) {
         player.hasReceivedInitialValreiData = false;
      }

      if (Features.Feature.VALREI_MAP.isEnabled() && hasInitialData() && player != null) {
         player.getCommunicator().sendValreiMapDataList(enteties.values());
         player.hasReceivedInitialValreiData = enteties.values().size() > 0;
      }
   }

   public static synchronized void updateTimeData() {
      EpicEntity[] epicEnts = EpicServerStatus.getValrei().getAllEntities();
      if (epicEnts != null) {
         long now = System.currentTimeMillis();

         for(EpicEntity ent : epicEnts) {
            long id = ent.getId();
            long remaining = ent.getTimeUntilLeave() - now;
            updateEntityTime(id, remaining);
         }
      }
   }

   public static synchronized void collectLocalData() {
      EpicEntity[] epicEnts = EpicServerStatus.getValrei().getAllEntities();
      if (epicEnts != null) {
         long now = System.currentTimeMillis();

         for(EpicEntity ent : epicEnts) {
            long id = ent.getId();
            int hexId = ent.getMapHex() != null ? ent.getMapHex().getId() : -1;
            int type = ent.getType();
            int targetHex = ent.getTargetHex();
            String name = ent.getName();
            long remaining = ent.getTimeToNextHex() - now;
            float attack = ent.getAttack();
            float vitality = ent.getVitality();
            float bodyStr = ent.getCurrentSkill(102);
            float bodySta = ent.getCurrentSkill(103);
            float bodyCon = ent.getCurrentSkill(104);
            float mindLog = ent.getCurrentSkill(100);
            float mindSpe = ent.getCurrentSkill(101);
            float soulStr = ent.getCurrentSkill(105);
            float soulDep = ent.getCurrentSkill(106);
            List<CollectedValreiItem> carried = CollectedValreiItem.fromList(ent.getAllCollectedItems());
            updateEntity(id, hexId, type, targetHex, name, remaining, bodyStr, bodySta, bodyCon, mindLog, mindSpe, soulStr, soulDep, carried);
         }
      }
   }

   public static synchronized void updateFromEpicEntity(EpicEntity ent) {
      if (Servers.localServer.LOGINSERVER) {
         long now = System.currentTimeMillis();
         WCValreiMapUpdater updater = new WCValreiMapUpdater(WurmId.getNextWCCommandId(), (byte)2);
         updater.setEntityToUpdate(ent);
         updater.sendFromLoginServer();
         long id = ent.getId();
         int hexId = ent.getMapHex() != null ? ent.getMapHex().getId() : -1;
         int type = ent.getType();
         int targetHex = ent.getTargetHex();
         String name = ent.getName();
         long remaining = ent.getTimeUntilLeave() - now;
         float attack = ent.getAttack();
         float vitality = ent.getVitality();
         float bodyStr = ent.getCurrentSkill(102);
         float bodySta = ent.getCurrentSkill(103);
         float bodyCon = ent.getCurrentSkill(104);
         float mindLog = ent.getCurrentSkill(100);
         float mindSpe = ent.getCurrentSkill(101);
         float soulStr = ent.getCurrentSkill(105);
         float soulDep = ent.getCurrentSkill(106);
         List<CollectedValreiItem> carried = CollectedValreiItem.fromList(ent.getAllCollectedItems());
         updateEntity(id, hexId, type, targetHex, name, remaining, bodyStr, bodySta, bodyCon, mindLog, mindSpe, soulStr, soulDep, carried);
      }
   }
}
