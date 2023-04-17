/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.Effectuator;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.webinterface.WcEpicEvent;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcEpicStatusReport
extends WebCommand {
    private static final Logger logger = Logger.getLogger(WcEpicStatusReport.class.getName());
    private boolean success = false;
    private boolean entityStatusMessage = false;
    private long entityId = 0L;
    private byte missionType = (byte)-1;
    private int missionDifficulty = -1;
    private final Map<String, Integer> entityStatuses = new HashMap<String, Integer>();
    private final Map<Integer, Integer> entityHexes = new HashMap<Integer, Integer>();
    private final Map<Integer, String> entityMap = new HashMap<Integer, String>();

    public WcEpicStatusReport(long aId, boolean wasSuccess, int epicEntityId, byte type, int difficulty) {
        super(aId, (short)10);
        this.success = wasSuccess;
        this.entityId = epicEntityId;
        this.missionType = type;
        this.missionDifficulty = difficulty;
        this.isRestrictedEpic = true;
    }

    public WcEpicStatusReport(long aId, byte[] _data) {
        super(aId, (short)10, _data);
        this.isRestrictedEpic = true;
    }

    public final void addEntityStatus(String status, int statusEntityId) {
        this.entityStatuses.put(status, statusEntityId);
        this.entityStatusMessage = true;
    }

    public final void addEntityHex(int entity, int hexId) {
        this.entityHexes.put(entity, hexId);
    }

    public final void fillStatusReport(HexMap map) {
        EpicEntity[] entities;
        for (EpicEntity entity : entities = map.getAllEntities()) {
            if (entity.isDeity()) {
                this.entityMap.put((int)entity.getId(), entity.getName());
            }
            this.addEntityStatus(entity.getLocationStatus(), (int)entity.getId());
            this.addEntityStatus(entity.getEnemyStatus(), (int)entity.getId());
            int collsCarried = entity.countCollectables();
            if (collsCarried > 0) {
                this.addEntityStatus(entity.getName() + " is carrying " + collsCarried + " of the " + entity.getCollectibleName() + ".", (int)entity.getId());
            }
            if (!entity.isDeity() || entity.getMapHex() == null) continue;
            this.addEntityHex((int)entity.getId(), entity.getMapHex().getId());
        }
    }

    @Override
    public boolean autoForward() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    byte[] encode() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = null;
        byte[] barr = null;
        try {
            dos = new DataOutputStream(bos);
            dos.writeBoolean(this.entityStatusMessage);
            if (this.entityStatusMessage) {
                dos.writeInt(this.entityMap.size());
                for (Map.Entry<Integer, String> entry : this.entityMap.entrySet()) {
                    dos.writeInt(entry.getKey());
                    dos.writeUTF(entry.getValue());
                }
                dos.writeInt(this.entityStatuses.size());
                if (this.entityStatuses.size() > 0) {
                    for (Map.Entry<Object, Object> entry : this.entityStatuses.entrySet()) {
                        dos.writeUTF((String)entry.getKey());
                        dos.writeInt((Integer)entry.getValue());
                    }
                }
                dos.writeInt(this.entityHexes.size());
                if (this.entityHexes.size() > 0) {
                    for (Map.Entry<Object, Object> entry : this.entityHexes.entrySet()) {
                        dos.writeInt((Integer)entry.getKey());
                        dos.writeInt((Integer)entry.getValue());
                    }
                }
            } else {
                dos.writeBoolean(this.success);
                dos.writeLong(this.entityId);
                dos.writeByte(this.missionType);
                dos.writeInt(this.missionDifficulty);
            }
            dos.flush();
            dos.close();
        }
        catch (Exception ex) {
            try {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            catch (Throwable throwable) {
                StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
                barr = bos.toByteArray();
                StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
                this.setData(barr);
                throw throwable;
            }
            StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
            barr = bos.toByteArray();
            StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
            this.setData(barr);
        }
        StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
        barr = bos.toByteArray();
        StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
        this.setData(barr);
        return barr;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void execute() {
        DataInputStream dis;
        block16: {
            dis = null;
            try {
                EpicEntity entity;
                dis = new DataInputStream(new ByteArrayInputStream(this.getData()));
                this.entityStatusMessage = dis.readBoolean();
                if (this.entityStatusMessage) {
                    int entity2;
                    Deities.clearValreiStatuses();
                    int numsx = dis.readInt();
                    for (int x = 0; x < numsx; ++x) {
                        int entity3 = dis.readInt();
                        String name = dis.readUTF();
                        Deities.addEntity(entity3, name);
                    }
                    int nums = dis.readInt();
                    for (int x = 0; x < nums; ++x) {
                        String status = dis.readUTF();
                        entity2 = dis.readInt();
                        Deities.addStatus(status, entity2);
                    }
                    int numsPos = dis.readInt();
                    for (int x = 0; x < numsPos; ++x) {
                        entity2 = dis.readInt();
                        int hexPos = dis.readInt();
                        Deities.addPosition(entity2, hexPos);
                    }
                    break block16;
                }
                this.success = dis.readBoolean();
                this.entityId = dis.readLong();
                this.missionType = dis.readByte();
                this.missionDifficulty = dis.readInt();
                ServerEntry entry = Servers.getServerWithId(WurmId.getOrigin(this.getWurmId()));
                if (entry == null || Server.getEpicMap() == null) break block16;
                if (entry.EPIC) {
                    EpicMission mission = EpicServerStatus.getEpicMissionForEntity((int)this.entityId);
                    if (mission != null) {
                        EpicEntity entity4;
                        if (!Servers.localServer.EPIC && this.success) {
                            float oldStatus = mission.getMissionProgress();
                            mission.updateProgress(oldStatus + 1.0f);
                        }
                        if ((entity4 = Server.getEpicMap().getEntity(this.entityId)) != null) {
                            Date now = new Date();
                            DateFormat format = DateFormat.getDateInstance(3);
                            if (this.success) {
                                Server.getEpicMap().broadCast(entity4.getName() + " received help from " + entry.name + ". " + format.format(now) + " " + Server.rand.nextInt(1000));
                                Server.getEpicMap().setEntityHelped(this.entityId, this.missionType, this.missionDifficulty);
                            } else {
                                Server.getEpicMap().broadCast(entity4.getName() + " never received help from " + entry.name + ". " + format.format(now) + " " + Server.rand.nextInt(1000));
                                if (entity4.isDeity()) {
                                    entity4.setShouldCreateMission(true, false);
                                }
                            }
                        }
                    } else {
                        EpicEntity entity5 = Server.getEpicMap().getEntity(this.entityId);
                        Date now = new Date();
                        DateFormat format = DateFormat.getDateInstance(3);
                        Server.getEpicMap().broadCast(entity5.getName() + " did not have an active mission when receiving help from " + entry.name + ". " + format.format(now) + " " + Server.rand.nextInt(1000));
                        entity5.setShouldCreateMission(true, false);
                    }
                    break block16;
                }
                if (!this.success || (entity = Server.getEpicMap().getEntity(this.entityId)) == null) break block16;
                int effect = Server.rand.nextInt(4) + 1;
                WcEpicEvent wce = new WcEpicEvent(WurmId.getNextWCCommandId(), 0, this.entityId, 0, effect, entity.getName() + "s followers now have the attention of the " + Effectuator.getSpiritType(effect) + " spirits.", false);
                wce.sendToServer(entry.id);
            }
            catch (IOException ex) {
                try {
                    logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
                }
                catch (Throwable throwable) {
                    StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                    throw throwable;
                }
                StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            }
        }
        StreamUtilities.closeInputStreamIgnoreExceptions(dis);
    }
}

