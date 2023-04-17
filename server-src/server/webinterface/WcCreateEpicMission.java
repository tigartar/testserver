/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcCreateEpicMission
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcCreateEpicMission.class.getName());
    private int collectiblesToWin = 5;
    private int collectiblesForWurmToWin = 8;
    private boolean spawnPointRequiredToWin = true;
    private int hexNumRequiredToWin = 0;
    private int scenarioNumber = 0;
    private int reasonPlusEffect = 0;
    private String scenarioName = "";
    private String scenarioQuest = "";
    public long entityNumber = 0L;
    private String entityName = "unknown";
    private int difficulty = 0;
    private long maxTimeSeconds = 0L;
    private boolean destroyPreviousMissions = false;

    public WcCreateEpicMission(long a_id, String scenName, int scenNumber, int reasonEff, int collReq, int collReqWurm, boolean spawnP, int hexNumReq, String questString, long epicEntity, int diff, String epicEntityName, long maxTimeSecs, boolean destroyPrevMissions) {
        super(a_id, (short)11);
        this.scenarioName = scenName;
        this.scenarioNumber = scenNumber;
        this.reasonPlusEffect = reasonEff;
        this.collectiblesToWin = collReq;
        this.collectiblesForWurmToWin = collReqWurm;
        this.spawnPointRequiredToWin = spawnP;
        this.hexNumRequiredToWin = hexNumReq;
        this.scenarioQuest = questString;
        this.entityNumber = epicEntity;
        this.difficulty = diff;
        this.entityName = epicEntityName;
        this.maxTimeSeconds = maxTimeSecs;
        this.destroyPreviousMissions = destroyPrevMissions;
        this.isRestrictedEpic = true;
    }

    public WcCreateEpicMission(long aId, byte[] _data) {
        super(aId, (short)11, _data);
        this.isRestrictedEpic = true;
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
            dos.writeUTF(this.scenarioName);
            dos.writeInt(this.scenarioNumber);
            dos.writeInt(this.reasonPlusEffect);
            dos.writeInt(this.collectiblesToWin);
            dos.writeInt(this.collectiblesForWurmToWin);
            dos.writeBoolean(this.spawnPointRequiredToWin);
            dos.writeInt(this.hexNumRequiredToWin);
            dos.writeUTF(this.scenarioQuest);
            dos.writeLong(this.entityNumber);
            dos.writeInt(this.difficulty);
            dos.writeUTF(this.entityName);
            dos.writeLong(this.maxTimeSeconds);
            dos.writeBoolean(this.destroyPreviousMissions);
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

    @Override
    public void execute() {
        new Thread(){

            @Override
            public void run() {
                DataInputStream dis = null;
                try {
                    dis = new DataInputStream(new ByteArrayInputStream(WcCreateEpicMission.this.getData()));
                    WcCreateEpicMission.this.scenarioName = dis.readUTF();
                    WcCreateEpicMission.this.scenarioNumber = dis.readInt();
                    WcCreateEpicMission.this.reasonPlusEffect = dis.readInt();
                    WcCreateEpicMission.this.collectiblesToWin = dis.readInt();
                    WcCreateEpicMission.this.collectiblesForWurmToWin = dis.readInt();
                    WcCreateEpicMission.this.spawnPointRequiredToWin = dis.readBoolean();
                    WcCreateEpicMission.this.hexNumRequiredToWin = dis.readInt();
                    WcCreateEpicMission.this.scenarioQuest = dis.readUTF();
                    WcCreateEpicMission.this.entityNumber = dis.readLong();
                    WcCreateEpicMission.this.difficulty = dis.readInt();
                    WcCreateEpicMission.this.entityName = dis.readUTF();
                    WcCreateEpicMission.this.maxTimeSeconds = dis.readLong();
                    WcCreateEpicMission.this.destroyPreviousMissions = dis.readBoolean();
                    if (EpicServerStatus.getCurrentScenario().getScenarioNumber() != WcCreateEpicMission.this.scenarioNumber) {
                        EpicServerStatus.getCurrentScenario().saveScenario(false);
                        EpicServerStatus.getCurrentScenario().setScenarioQuest(WcCreateEpicMission.this.scenarioQuest);
                        EpicServerStatus.getCurrentScenario().setScenarioName(WcCreateEpicMission.this.scenarioName);
                        EpicServerStatus.getCurrentScenario().setScenarioNumber(WcCreateEpicMission.this.scenarioNumber);
                        EpicServerStatus.getCurrentScenario().setReasonPlusEffect(WcCreateEpicMission.this.reasonPlusEffect);
                        EpicServerStatus.getCurrentScenario().setCollectiblesToWin(WcCreateEpicMission.this.collectiblesToWin);
                        EpicServerStatus.getCurrentScenario().setCollectiblesForWurmToWin(WcCreateEpicMission.this.collectiblesForWurmToWin);
                        EpicServerStatus.getCurrentScenario().setHexNumRequiredToWin(WcCreateEpicMission.this.hexNumRequiredToWin);
                        EpicServerStatus.getCurrentScenario().saveScenario(true);
                    }
                    EpicServerStatus es = new EpicServerStatus();
                    es.generateNewMissionForEpicEntity((int)WcCreateEpicMission.this.entityNumber, WcCreateEpicMission.this.entityName, WcCreateEpicMission.this.difficulty, (int)WcCreateEpicMission.this.maxTimeSeconds, WcCreateEpicMission.this.scenarioName, WcCreateEpicMission.this.scenarioNumber, WcCreateEpicMission.this.scenarioQuest, WcCreateEpicMission.this.destroyPreviousMissions);
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
                StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            }
        }.start();
    }
}

