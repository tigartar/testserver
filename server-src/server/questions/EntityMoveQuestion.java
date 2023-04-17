/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.MapHex;
import com.wurmonline.server.questions.Question;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EntityMoveQuestion
extends Question {
    private Integer[] neighbours;
    private MapHex currentHex;
    private int deityToGuide = -1;
    private boolean secondStep = false;
    private static final Logger logger = Logger.getLogger(EntityMoveQuestion.class.getName());

    public EntityMoveQuestion(Creature aResponder) {
        super(aResponder, "Guide the deities", "Whereto will you guide your deity?", 113, -10L);
    }

    @Override
    public void answer(Properties answers) {
        if (this.getResponder().getKarma() < 5000) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You do not have enough karma to commune with " + this.getResponder().getDeity().getName() + ".");
            return;
        }
        String deityString = answers.getProperty("deityId");
        if (!this.secondStep) {
            if (deityString != null && deityString.length() > 0) {
                try {
                    int deityId = Integer.parseInt(deityString);
                    if (deityId < 0) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("You refrain from disturbing the gods at this time.");
                        return;
                    }
                    Deity deity = Deities.getDeity(deityId);
                    if (this.getResponder().getDeity() != null && deity != null) {
                        EntityMoveQuestion nem = new EntityMoveQuestion(this.getResponder());
                        nem.secondStep = true;
                        nem.deityToGuide = deityId;
                        nem.sendHexQuestion();
                        return;
                    }
                    this.getResponder().getCommunicator().sendAlertServerMessage("You fail to commune with the gods...");
                }
                catch (NumberFormatException nfre) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Not a number for the desired deity...");
                    logger.log(Level.INFO, "Not a number " + deityString);
                }
            } else {
                this.getResponder().getCommunicator().sendNormalServerMessage("You refrain from disturbing the gods at this time.");
            }
        } else if (this.getResponder().getDeity() != null) {
            Deity deity = Deities.getDeity(this.deityToGuide);
            if (deity == null) {
                this.getResponder().getCommunicator().sendNormalServerMessage("Not a number for the desired deity...");
                return;
            }
            String val = answers.getProperty("sethex");
            if (val != null && val.length() > 0) {
                try {
                    MapHex hex;
                    int hexnum = Integer.parseInt(val);
                    if (hexnum < 0) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("You refrain from disturbing the gods at this time.");
                        return;
                    }
                    boolean ok = false;
                    for (Integer hexes : this.neighbours) {
                        if (hexes != hexnum) continue;
                        ok = true;
                        break;
                    }
                    if (ok && (hex = EpicServerStatus.getValrei().getMapHex(hexnum)) != null) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("You attempt to guide your deity..");
                        new Thread(this.getResponder().getName() + "-guides-" + deity.getName() + "-Thread"){

                            @Override
                            public final void run() {
                                boolean success;
                                boolean bl = success = Server.rand.nextFloat() < 0.7f;
                                if (success) {
                                    LoginServerWebConnection lsw = new LoginServerWebConnection();
                                    success = lsw.requestDeityMove(EntityMoveQuestion.this.deityToGuide, hexnum, EntityMoveQuestion.this.getResponder().getName());
                                    try {
                                        Thread.sleep(2000L);
                                    }
                                    catch (InterruptedException interruptedException) {
                                        // empty catch block
                                    }
                                    if (success) {
                                        logger.log(Level.INFO, EntityMoveQuestion.this.getResponder().getName() + " guides " + deity.getName());
                                        EntityMoveQuestion.this.getResponder().getCommunicator().sendSafeServerMessage("... and " + deity.getName() + " heeds your advice!");
                                        EntityMoveQuestion.this.getResponder().modifyKarma(-5000);
                                    } else {
                                        EntityMoveQuestion.this.getResponder().getCommunicator().sendNormalServerMessage("... but fail to penetrate the ether to Valrei.");
                                        logger.log(Level.INFO, EntityMoveQuestion.this.getResponder().getName() + " guiding but connection to " + deity.getName() + " broken.");
                                    }
                                } else {
                                    try {
                                        Thread.sleep(3000L);
                                    }
                                    catch (InterruptedException interruptedException) {
                                        // empty catch block
                                    }
                                    EntityMoveQuestion.this.getResponder().getCommunicator().sendNormalServerMessage("... but you are ignored.");
                                    EntityMoveQuestion.this.getResponder().modifyKarma(-2500);
                                    logger.log(Level.INFO, EntityMoveQuestion.this.getResponder().getName() + " guiding ignored by " + deity.getName() + ".");
                                }
                            }
                        }.start();
                    }
                }
                catch (NumberFormatException nfre) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Not a number for the desired position...");
                    logger.log(Level.INFO, "Not a number " + val);
                }
            } else {
                this.getResponder().getCommunicator().sendNormalServerMessage("You refrain from disturbing the gods at this time.");
            }
        } else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You no longer pray to a deity.");
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.getResponder().getDeity() != null) {
            buf.append("text{text=\"You may spend karma in order to envision Valrei and attempt to guide your deity.\"}text{text=\"\"}");
            buf.append("text{text=\"There is 70% chance that you succed in getting your deities attention, and the cost will be 5000 karma if you do.\"}text{text=\"\"}");
            buf.append("text{text=\"If the request fails, you will only lose 2500 karma.\"}text{text=\"\"}");
            buf.append("radio{ group='deityId'; id='0';text='Do not Guide';selected='true'}");
            if (this.getResponder().getKingdomTemplateId() == 3) {
                buf.append("radio{ group='deityId'; id='4';text='Guide Libila'}");
            } else if (this.getResponder().getKingdomTemplateId() == 2) {
                buf.append("radio{ group='deityId'; id='2';text='Guide Magranon'}");
            } else if (this.getResponder().getKingdomTemplateId() == 1) {
                if (this.getResponder().getDeity().number == 3) {
                    buf.append("radio{ group='deityId'; id='1';text='Guide Fo'}");
                } else {
                    buf.append("radio{ group='deityId'; id='1';text='Guide Fo'}");
                }
            }
        } else {
            buf.append("text{text=\"You no longer pray to a deity.\"}text{text=\"\"}");
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    public final void sendHexQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        Integer currentInt = Deities.getPosition(this.deityToGuide);
        Deity deity = Deities.getDeity(this.deityToGuide);
        buf.append("text{text=\"Where do you want " + deity.getName() + " to go?\"}text{text=\"\"}");
        buf.append("radio{ group='sethex'; id=\"-1\";text=\"Never mind...\";selected=\"true\"};");
        if (currentInt != null) {
            this.currentHex = EpicServerStatus.getValrei().getMapHex((int)currentInt);
            if (this.currentHex != null) {
                for (Integer i : this.neighbours = this.currentHex.getNearMapHexes()) {
                    MapHex maphex = EpicServerStatus.getValrei().getMapHex((int)i);
                    if (maphex != null) {
                        String trap = maphex.isTrap() ? " (trap)" : "";
                        String slow = maphex.isSlow() ? " (slow)" : "";
                        String teleport = maphex.isTeleport() ? " (shift)" : "";
                        String strength = maphex.isStrength() ? " (strength)" : "";
                        String vitality = maphex.isVitality() ? " (vitality)" : "";
                        buf.append("radio{ group='sethex'; id=\"" + i + "\";text=\"" + maphex.getName() + trap + slow + teleport + strength + vitality + "\"};");
                        continue;
                    }
                    logger.log(Level.WARNING, "NO HEX ON VALREI FOR " + i);
                }
                if (this.neighbours == null || this.neighbours.length == 0) {
                    buf.append("text{text=\"" + deity.getName() + " is not available for guidance now.\"}text{text=\"\"}");
                }
            } else {
                buf.append("text{text=\"" + deity.getName() + " is not available for guidance now.\"}text{text=\"\"}");
            }
        } else {
            buf.append("text{text=\"" + deity.getName() + " is not available for guidance now.\"}text{text=\"\"}");
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    static /* synthetic */ int access$000(EntityMoveQuestion x0) {
        return x0.deityToGuide;
    }

    static /* synthetic */ Logger access$100() {
        return logger;
    }
}

