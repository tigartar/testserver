/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.Mailer;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WebInterfaceImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WishQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(WishQuestion.class.getName());
    private final long coinId;
    private static final String RESPONSE1 = ". Will the gods listen?";
    private static final String RESPONSE2 = ". Do you consider yourself lucky?";
    private static final String RESPONSE3 = ". Is this your turn?";
    private static final String RESPONSE4 = ". You get the feeling that someone listens.";
    private static final String RESPONSE5 = ". Good luck!";
    private static final String RESPONSE6 = ". Will it come true?";
    private static final Random rand = new Random();
    private static final String INSERT_WISH = "INSERT INTO WISHES (PLAYER,WISH,COIN,TOFULFILL) VALUES(?,?,?,?)";

    public WishQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, long coin) {
        super(aResponder, aTitle, aQuestion, 77, aTarget);
        this.coinId = coin;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void answer(Properties aAnswers) {
        block22: {
            Item coin = null;
            Item targetItem = null;
            try {
                targetItem = Items.getItem(this.target);
            }
            catch (NoSuchItemException nsi) {
                this.getResponder().getCommunicator().sendNormalServerMessage("You fail to locate the target!");
                return;
            }
            try {
                block23: {
                    block24: {
                        boolean toFulfill;
                        coin = Items.getItem(this.coinId);
                        if (coin.getOwner() != this.getResponder().getWurmId() || coin.isBanked() || coin.mailed) break block23;
                        String key = "data1";
                        String val = aAnswers.getProperty("data1");
                        if (val == null || val.length() <= 0) break block24;
                        String tstring = RESPONSE1;
                        int x = rand.nextInt(6);
                        if (x == 1) {
                            tstring = RESPONSE2;
                        } else if (x == 2) {
                            tstring = RESPONSE3;
                        } else if (x == 3) {
                            tstring = RESPONSE4;
                        } else if (x == 4) {
                            tstring = RESPONSE5;
                        } else if (x == 5) {
                            tstring = RESPONSE6;
                        }
                        this.getResponder().getCommunicator().sendNormalServerMessage("You wish for " + val + tstring);
                        long moneyVal = Economy.getValueFor(coin.getTemplateId());
                        float chance = (float)moneyVal / 3.0E7f;
                        float chantLevel = targetItem.getSpellCourierBonus();
                        float timeBonus = WurmCalendar.isNight() ? 1.05f : 1.0f;
                        float newchance = chance * (targetItem.getCurrentQualityLevel() / 100.0f) * (1.0f + chantLevel / 100.0f) * (1.0f + coin.getCurrentQualityLevel() / 1000.0f) * timeBonus;
                        logger.log(Level.INFO, "New chance=" + newchance + " after coin=" + chance + ", chant=" + chantLevel + " ql=" + targetItem.getCurrentQualityLevel());
                        boolean bl = toFulfill = rand.nextFloat() < newchance;
                        if (this.getResponder().getPower() >= 5) {
                            toFulfill = true;
                        }
                        Connection dbcon = null;
                        PreparedStatement ps = null;
                        try {
                            dbcon = DbConnector.getPlayerDbCon();
                            ps = dbcon.prepareStatement(INSERT_WISH);
                            ps.setLong(1, this.getResponder().getWurmId());
                            ps.setString(2, val);
                            ps.setLong(3, moneyVal);
                            ps.setBoolean(4, toFulfill);
                            ps.executeUpdate();
                        }
                        catch (SQLException sqx) {
                            try {
                                logger.log(Level.WARNING, sqx.getMessage(), sqx);
                            }
                            catch (Throwable throwable) {
                                DbUtilities.closeDatabaseObjects(ps, null);
                                DbConnector.returnConnection(dbcon);
                                throw throwable;
                            }
                            DbUtilities.closeDatabaseObjects(ps, null);
                            DbConnector.returnConnection(dbcon);
                        }
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        Items.destroyItem(coin.getWurmId());
                        if (toFulfill) {
                            try {
                                Mailer.sendMail(WebInterfaceImpl.mailAccount, "rolf@wurmonline.com", this.getResponder().getName() + " made a wish!", this.getResponder().getName() + " wants the wish " + val + " to be fulfilled!");
                            }
                            catch (Exception ex) {
                                logger.log(Level.WARNING, ex.getMessage(), ex);
                            }
                        }
                        break block22;
                    }
                    this.getResponder().getCommunicator().sendNormalServerMessage("You make no wish this time.");
                    break block22;
                }
                this.getResponder().getCommunicator().sendNormalServerMessage("You are no longer in possesion of the " + coin.getName() + "!");
                return;
            }
            catch (NoSuchItemException nsi) {
                this.getResponder().getCommunicator().sendNormalServerMessage("You are no longer in possesion of the coin!");
                return;
            }
            catch (NotOwnedException no) {
                this.getResponder().getCommunicator().sendNormalServerMessage("You are no longer in possesion of the coin!");
                return;
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        buf.append("harray{label{text='What is your wish?'};input{maxchars='40';id='data1'; text=''}}");
        buf.append("label{text=\"Just leave it blank if you don't want to lose your coin.\"}");
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

