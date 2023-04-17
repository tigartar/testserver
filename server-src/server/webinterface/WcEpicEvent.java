/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Message;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.CreatureTemplateCreator;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.epic.Effectuator;
import com.wurmonline.server.epic.SynchedEpicEffect;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcEpicEvent
extends WebCommand
implements CreatureTemplateIds,
CounterTypes {
    private static final Logger logger = Logger.getLogger(WcEpicEvent.class.getName());
    private int effectNumber;
    private long deityNumber;
    private int templateId;
    private int bonusEffectNum;
    private String eventString = "";
    private boolean resettingKarma = false;

    public WcEpicEvent(long _id, int effectNum, long deityNum, int selectedTemplate, int bonusEffect, String eventDesc, boolean resetKarma) {
        super(_id, (short)9);
        this.effectNumber = effectNum;
        this.deityNumber = deityNum;
        this.templateId = selectedTemplate;
        this.bonusEffectNum = bonusEffect;
        if (this.bonusEffectNum < 0) {
            this.bonusEffectNum = Server.rand.nextInt(4);
        }
        this.eventString = eventDesc;
        this.resettingKarma = resetKarma;
        this.isRestrictedEpic = true;
    }

    public WcEpicEvent(long _id, byte[] _data) {
        super(_id, (short)9, _data);
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
            dos.writeInt(this.effectNumber);
            dos.writeLong(this.deityNumber);
            dos.writeInt(this.templateId);
            dos.writeInt(this.bonusEffectNum);
            dos.writeUTF(this.eventString);
            dos.writeBoolean(this.resettingKarma);
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
     * Unable to fully structure code
     */
    @Override
    public void execute() {
        block31: {
            block28: {
                block29: {
                    block30: {
                        dis = null;
                        dis = new DataInputStream(new ByteArrayInputStream(this.getData()));
                        this.effectNumber = dis.readInt();
                        this.deityNumber = dis.readLong();
                        this.templateId = dis.readInt();
                        this.bonusEffectNum = dis.readInt();
                        this.eventString = dis.readUTF();
                        this.resettingKarma = dis.readBoolean();
                        if (this.bonusEffectNum <= 0) ** GOTO lbl100
                        if (this.bonusEffectNum != 4 && this.bonusEffectNum != 5) ** GOTO lbl78
                        creatureName = "Unknown";
                        if (WurmId.getType(this.deityNumber) != 0) break block28;
                        pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(this.deityNumber);
                        if (pinf == null) break block29;
                        if (pinf.loaded) break block30;
                        try {
                            pinf.load();
                            creatureName = pinf.getName();
                        }
                        catch (IOException iox) {
                            StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                            return;
                        }
                    }
                    ** if (pinf.currentServer == Servers.localServer.id) goto lbl-1000
lbl-1000:
                    // 1 sources

                    {
                        StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                        return;
                    }
lbl-1000:
                    // 1 sources

                    {
                        break block28;
                    }
                }
                StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                return;
            }
            try {
                try {
                    fragment = this.bonusEffectNum == 5;
                    i = ItemFactory.createItem(fragment != false ? 1307 : this.templateId, 80 + Server.rand.nextInt(20), this.eventString);
                    if (fragment) {
                        i.setRealTemplate(this.templateId);
                        i.setAuxData((byte)1);
                    }
                    if (i.getTemplateId() == 465) {
                        i.setData1(CreatureTemplateCreator.getRandomDragonOrDrakeId());
                    } else if (i.getTemplateId() == 794) {
                        for (Deity de : deityArr = Deities.getDeities()) {
                            if (!de.getName().equalsIgnoreCase(this.eventString)) continue;
                            i.setAuxData((byte)de.getNumber());
                            i.setData1(577);
                        }
                    } else if (i.isAbility()) {
                        i.setAuxData((byte)2);
                    }
                    if (WurmId.getType(this.deityNumber) == 1) {
                        try {
                            c = Creatures.getInstance().getCreature(this.deityNumber);
                            c.getInventory().insertItem(i, true);
                            creatureName = c.getName();
                        }
                        catch (NoSuchCreatureException c) {}
                    } else {
                        try {
                            p = Players.getInstance().getPlayer(this.deityNumber);
                            p.getInventory().insertItem(i);
                            creatureName = p.getName();
                        }
                        catch (NoSuchPlayerException nsp) {
                            inventory = DbCreatureStatus.getInventoryIdFor(this.deityNumber);
                            i.setParentId(inventory, true);
                            i.setOwnerId(this.deityNumber);
                        }
                    }
                    HistoryManager.addHistory(creatureName, "receives the " + i.getName() + " from " + this.eventString + ".");
                }
                catch (Exception nsi) {
                    WcEpicEvent.logger.log(Level.WARNING, nsi.getMessage(), nsi);
                }
                break block31;
lbl78:
                // 1 sources

                mess = new Message(null, 5, ":Event", this.eventString);
                Server.getInstance().addMessage(mess);
                eff = new SynchedEpicEffect(3);
                eff.setDeityNumber(this.deityNumber);
                eff.setBonusEffectNum(this.bonusEffectNum);
                eff.setResetKarma(this.resettingKarma);
                eff.setEffectNumber(this.effectNumber);
                Effectuator.addEpicEffect(eff);
                if (this.templateId > 0) {
                    try {
                        ct = CreatureTemplateFactory.getInstance().getTemplate(this.templateId);
                        effectDesc = "Some " + ct.getName() + " arrive.";
                        mess2 = new Message(null, 5, ":Event", effectDesc);
                        Server.getInstance().addMessage(mess2);
                        eff2 = new SynchedEpicEffect(1);
                        eff2.setDeityNumber(this.deityNumber);
                        eff2.setCreatureTemplateId(this.templateId);
                        Effectuator.addEpicEffect(eff2);
                    }
                    catch (NoSuchCreatureTemplateException nst) {
                        WcEpicEvent.logger.log(Level.WARNING, nst.getMessage());
                    }
                }
                break block31;
lbl100:
                // 1 sources

                if (this.effectNumber <= 0 || !Servers.localServer.PVPSERVER) break block31;
                if (this.effectNumber == 5) {
                    mess = new Message(null, 5, ":Event", this.eventString);
                    Server.getInstance().addMessage(mess);
                    eff = new SynchedEpicEffect(1);
                    eff.setDeityNumber(this.deityNumber);
                    eff.setCreatureTemplateId(this.templateId);
                    eff.setResetKarma(this.resettingKarma);
                    Effectuator.addEpicEffect(eff);
                    break block31;
                }
                mess = new Message(null, 5, ":Event", this.eventString);
                Server.getInstance().addMessage(mess);
                eff = new SynchedEpicEffect(2);
                eff.setDeityNumber(this.deityNumber);
                eff.setCreatureTemplateId(this.templateId);
                eff.setEffectNumber(this.effectNumber);
                eff.setBonusEffectNum(this.bonusEffectNum);
                eff.setEventString(this.eventString);
                eff.setResetKarma(this.resettingKarma);
                Effectuator.addEpicEffect(eff);
            }
            catch (IOException ex) {
                try {
                    WcEpicEvent.logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
                }
                catch (Throwable var10_28) {
                    StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                    throw var10_28;
                }
                StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            }
        }
        StreamUtilities.closeInputStreamIgnoreExceptions(dis);
    }
}

