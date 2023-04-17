/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.questions.MailSendConfirmQuestion;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.ReiceverReturnMails;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.util.MaterialUtilities;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MailReceiveQuestion
extends Question
implements MonetaryConstants,
TimeConstants {
    private static final Logger logger = Logger.getLogger(MailReceiveQuestion.class.getName());
    private final Item mbox;
    private Set<WurmMail> mailset = null;

    public MailReceiveQuestion(Creature aResponder, String aTitle, String aQuestion, Item aMailbox) {
        super(aResponder, aTitle, aQuestion, 53, aMailbox.getWurmId());
        this.mbox = aMailbox;
    }

    @Override
    public void answer(Properties answers) {
        block49: {
            if (!this.mbox.isEmpty(false)) {
                this.getResponder().getCommunicator().sendNormalServerMessage("Empty the mailbox first.");
            } else {
                long money;
                Item[] contained;
                if (this.mailset.isEmpty()) {
                    return;
                }
                if (!Servers.loginServer.isAvailable(5, true)) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("You may not receive mail right now. Please try later.");
                    return;
                }
                int x = 0;
                float priceMod = 1.0f;
                int pcost = 0;
                long fullcost = 0L;
                HashMap<Long, Long> moneyToSend = new HashMap<Long, Long>();
                WurmMail m = null;
                String val = "";
                HashSet<Item> itemset = new HashSet<Item>();
                HashMap<Long, WurmMail> toReturn = null;
                Iterator<WurmMail> it = this.mailset.iterator();
                while (it.hasNext()) {
                    ++x;
                    m = it.next();
                    priceMod = 1.0f;
                    long receiver = WurmMail.getReceiverForItem(m.itemId);
                    if (receiver != this.getResponder().getWurmId()) continue;
                    val = answers.getProperty(x + "receive");
                    if (val != null && val.equals("true")) {
                        try {
                            Item item = Items.getItem(m.itemId);
                            if (m.rejected) {
                                InscriptionData insData;
                                pcost = 100;
                                if ((item.getTemplateId() == 748 || item.getTemplateId() == 1272) && (insData = item.getInscription()) != null && insData.hasBeenInscribed()) {
                                    pcost = 1;
                                }
                                fullcost += (long)pcost;
                            } else if (m.type == 1) {
                                pcost = MailSendConfirmQuestion.getCostForItem(item, priceMod);
                                fullcost += (long)pcost;
                                fullcost += m.price;
                                if (m.price > 0L) {
                                    Long msend = (Long)moneyToSend.get(m.sender);
                                    msend = msend == null ? Long.valueOf(m.price) : Long.valueOf(msend + m.price);
                                    moneyToSend.put(m.sender, msend);
                                }
                                contained = item.getAllItems(true);
                                for (int c = 0; c < contained.length; ++c) {
                                    pcost = MailSendConfirmQuestion.getCostForItem(contained[c], priceMod);
                                    fullcost += (long)pcost;
                                }
                            }
                            itemset.add(item);
                        }
                        catch (NoSuchItemException nsi) {
                            logger.log(Level.INFO, " NO SUCH ITEM");
                            WurmMail.deleteMail(m.itemId);
                        }
                        continue;
                    }
                    val = answers.getProperty(x + "return");
                    if (val != null && val.equals("true")) {
                        if (toReturn == null) {
                            toReturn = new HashMap();
                        }
                        toReturn.put(m.itemId, m);
                        continue;
                    }
                    if (!m.isExpired()) continue;
                    if (toReturn == null) {
                        toReturn = new HashMap<Long, WurmMail>();
                    }
                    toReturn.put(m.itemId, m);
                }
                if (toReturn != null) {
                    HashMap<Integer, HashSet<WurmMail>> serverReturns = new HashMap<Integer, HashSet<WurmMail>>();
                    for (WurmMail retm : toReturn.values()) {
                        long timeavail = System.currentTimeMillis() + (long)(101 - (int)this.mbox.getSpellCourierBonus()) * 60000L;
                        if (this.getResponder().getPower() > 0) {
                            timeavail = System.currentTimeMillis() + 60000L;
                        }
                        WurmMail mail = new WurmMail(1, retm.itemId, this.getResponder().getWurmId(), retm.sender, 0L, timeavail, System.currentTimeMillis() + (Servers.localServer.testServer ? 3600000L : 604800000L), Servers.localServer.id, true, false);
                        if (retm.sourceserver == Servers.localServer.id) {
                            WurmMail.removeMail(retm.itemId);
                            WurmMail.addWurmMail(mail);
                            mail.createInDatabase();
                            continue;
                        }
                        HashSet<WurmMail> returnSet = (HashSet<WurmMail>)serverReturns.get(retm.sourceserver);
                        if (returnSet == null) {
                            returnSet = new HashSet<WurmMail>();
                        }
                        returnSet.add(mail);
                        serverReturns.put(retm.sourceserver, returnSet);
                    }
                    if (!serverReturns.isEmpty()) {
                        HashMap<Long, ReiceverReturnMails> returnsPerReceiver = new HashMap<Long, ReiceverReturnMails>();
                        for (Map.Entry entry : serverReturns.entrySet()) {
                            Integer sid = (Integer)entry.getKey();
                            Set mails = (Set)entry.getValue();
                            for (WurmMail newmail : mails) {
                                try {
                                    Item i = Items.getItem(newmail.itemId);
                                    ReiceverReturnMails returnSetReceiver = (ReiceverReturnMails)returnsPerReceiver.get(newmail.receiver);
                                    if (returnSetReceiver == null) {
                                        returnSetReceiver = new ReiceverReturnMails();
                                        returnSetReceiver.setReceiverId(newmail.receiver);
                                        returnSetReceiver.setServerId(sid);
                                    }
                                    returnSetReceiver.addMail(newmail, i);
                                    Item[] contained2 = i.getAllItems(true);
                                    for (int c = 0; c < contained2.length; ++c) {
                                        returnSetReceiver.addMail(newmail, contained2[c]);
                                    }
                                    returnsPerReceiver.put(newmail.receiver, returnSetReceiver);
                                }
                                catch (NoSuchItemException nsi) {
                                    logger.log(Level.WARNING, "The item that should be returned is gone!");
                                }
                            }
                        }
                        if (!returnsPerReceiver.isEmpty()) {
                            boolean problem = false;
                            for (Map.Entry entry : returnsPerReceiver.entrySet()) {
                                Long rid = (Long)entry.getKey();
                                ReiceverReturnMails returnSetReceiver = (ReiceverReturnMails)entry.getValue();
                                Item[] items = returnSetReceiver.getReturnItemSetAsArray();
                                problem = MailSendConfirmQuestion.sendMailSetToServer(this.getResponder().getWurmId(), this.getResponder(), returnSetReceiver.getServerId(), returnSetReceiver.getReturnWurmMailSet(), rid, items);
                                if (problem) continue;
                                for (int a = 0; a < items.length; ++a) {
                                    Item[] contained3 = items[a].getAllItems(true);
                                    for (int c = 0; c < contained3.length; ++c) {
                                        Items.destroyItem(contained3[c].getWurmId());
                                    }
                                    Items.destroyItem(items[a].getWurmId());
                                    WurmMail.removeMail(items[a].getWurmId());
                                }
                            }
                        }
                    }
                }
                if (fullcost > (money = this.getResponder().getMoney())) {
                    Change change = new Change(fullcost - money);
                    this.getResponder().getCommunicator().sendNormalServerMessage("You need " + change.getChangeString() + " in order to receive the selected items.");
                } else if (fullcost > 0L) {
                    LoginServerWebConnection lsw = new LoginServerWebConnection();
                    try {
                        if (this.getResponder().chargeMoney(fullcost)) {
                            for (Item item : itemset) {
                                Item[] contained4 = item.getAllItems(true);
                                for (int c = 0; c < contained4.length; ++c) {
                                    contained4[c].setMailed(false);
                                    contained4[c].setLastOwnerId(this.getResponder().getWurmId());
                                }
                                WurmMail.removeMail(item.getWurmId());
                                this.mbox.insertItem(item, true);
                                item.setLastOwnerId(this.getResponder().getWurmId());
                                item.setMailed(false);
                                logger.log(Level.INFO, this.getResponder().getName() + " received " + item.getName() + " " + item.getWurmId());
                            }
                            Change change = new Change(fullcost);
                            this.getResponder().getCommunicator().sendNormalServerMessage("The items are now available and you have been charged " + change.getChangeString() + ".");
                            int xx = 0;
                            Iterator it2 = moneyToSend.entrySet().iterator();
                            while (it2.hasNext()) {
                                ++xx;
                                Map.Entry entry = it2.next();
                                PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId((Long)entry.getKey());
                                if (pinf != null) {
                                    if ((Long)entry.getValue() <= 0L) continue;
                                    logger.log(Level.INFO, this.getResponder().getName() + " adding COD " + (Long)entry.getValue() + " to " + pinf.getName() + " via server " + lsw.getServerId());
                                    lsw.addMoney(pinf.wurmId, pinf.getName(), (long)((Long)entry.getValue()), "Mail " + this.getResponder().getName() + DateFormat.getInstance().format(new Date()).replace(" ", "") + xx);
                                    continue;
                                }
                                if ((Long)entry.getValue() > 0L) {
                                    logger.log(Level.INFO, "Adding COD " + (Long)entry.getValue() + " to " + (Long)entry.getKey() + " (no name) via server " + lsw.getServerId());
                                    lsw.addMoney((Long)entry.getKey(), null, (long)((Long)entry.getValue()), "Mail " + this.getResponder().getName() + DateFormat.getInstance().format(new Date()).replace(" ", "") + xx);
                                    continue;
                                }
                                this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the receiver of some money.");
                                logger.log(Level.WARNING, "failed to locate receiver " + (Long)entry.getKey() + " of amount " + (Long)entry.getValue() + " from " + this.getResponder().getName() + ".");
                            }
                            break block49;
                        }
                        this.getResponder().getCommunicator().sendNormalServerMessage("Failed to charge you the money. The bank may not be available. No mail received.");
                    }
                    catch (IOException iox) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Failed to charge you the money. The bank may not be available. No mail received.");
                    }
                } else {
                    for (Item item : itemset) {
                        contained = item.getAllItems(true);
                        for (int c = 0; c < contained.length; ++c) {
                            contained[c].setMailed(false);
                            contained[c].setLastOwnerId(this.getResponder().getWurmId());
                        }
                        WurmMail.removeMail(item.getWurmId());
                        this.mbox.insertItem(item, true);
                        item.setLastOwnerId(this.getResponder().getWurmId());
                        item.setMailed(false);
                    }
                    if (itemset.size() > 0) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("The items are now available in the " + this.mbox.getName() + ".");
                    }
                    if (toReturn != null && toReturn.size() > 0) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("The spirits will return the unwanted items.");
                    }
                }
            }
        }
    }

    @Override
    public void sendQuestion() {
        String lHtml = "border{scroll{vertical='true';horizontal='false';varray{rescale='true';passthrough{id='id';text='" + this.getId() + "'}";
        StringBuilder buf = new StringBuilder(lHtml);
        if (!this.mbox.isEmpty(false)) {
            buf.append("label{text=\"Empty the mailbox first.\"}");
            buf.append("text{text=\"\"};}null;varray{");
        } else {
            this.mailset = WurmMail.getSentMailsFor(this.getResponder().getWurmId(), 100);
            if (this.mailset.isEmpty()) {
                buf.append("text{text='You have no pending mail.'}");
                buf.append("text{text=\"\"};}null;");
            } else {
                buf.append("text{text='Use the checkboxes to select which items you wish to receive in your mailbox, and which to return to the sender.'}");
                buf.append("text{text='If an item has a Cash On Delivery (C.O.D) cost, you have to have that money in the bank.'}");
                long money = this.getResponder().getMoney();
                if (money <= 0L) {
                    buf.append("text{text='You have no money in the bank.'}");
                } else {
                    buf.append("text{text='You have " + new Change(money).getChangeString() + " in the bank.'}");
                }
                buf.append("}};null;");
                int rowNumb = 0;
                buf.append("tree{id=\"t1\";cols=\"10\";showheader=\"true\";height=\"300\"col{text=\"QL\";width=\"45\"};col{text=\"DAM\";width=\"45\"};col{text=\"Receive\";width=\"50\"};col{text=\"Return\";width=\"50\"};col{text=\"G\";width=\"25\"};col{text=\"S\";width=\"25\"};col{text=\"C\";width=\"25\"};col{text=\"I\";width=\"25\"};col{text=\"Sender\";width=\"75\"};col{text=\"Expiry\";width=\"220\"};");
                for (WurmMail m : this.mailset) {
                    try {
                        Item item = Items.getItem(m.itemId);
                        buf.append(this.addItem("" + ++rowNumb, item, m, true));
                    }
                    catch (NoSuchItemException e) {
                        buf.append("row{id=\"e" + rowNumb + "\";hover=\"Item gone.\";name=\"Item gone.\";rarity=\"0\";children=\"0\";col{text=\"n/a\"};col{text=\"n/a\"};col{text=\"n/a\"};col{text=\"n/a\"};col{text=\"\"};col{text=\"\"};col{text=\"\"};col{text=\"\"};col{text=\"n/a\"};col{text=\"n/a\"}}");
                    }
                }
                buf.append("}");
                buf.append("null;varray{");
                if (this.mailset.size() < 100) {
                    buf.append("label{text='You have no more mail.'}");
                } else {
                    buf.append("text{text='You may have more mail than these. Manage these then check again.'}");
                }
            }
        }
        buf.append("harray{button{text=\"Send\";id=\"submit\"}}text=\"\"}}");
        this.getResponder().getCommunicator().sendBml(700, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private String addItem(String id, Item item, WurmMail m, boolean isTopLevel) {
        StringBuilder buf = new StringBuilder();
        Change change = null;
        float priceMod = 1.0f;
        int pcost = 0;
        long fullcost = 0L;
        Item[] contained = item.getItemsAsArray();
        int children = contained.length;
        if (m.rejected && isTopLevel) {
            InscriptionData insData;
            pcost = 100;
            if ((item.getTemplateId() == 748 || item.getTemplateId() == 1272) && (insData = item.getInscription()) != null && insData.hasBeenInscribed()) {
                pcost = 1;
            }
            change = new Change(pcost);
        } else if (m.price > 0L && isTopLevel) {
            fullcost = MailSendConfirmQuestion.getCostForItem(item, priceMod);
            fullcost += m.price;
            for (int c = 0; c < contained.length; ++c) {
                pcost = MailSendConfirmQuestion.getCostForItem(contained[c], priceMod);
                fullcost += (long)pcost;
            }
            change = new Change(fullcost);
        }
        String itemName = MailReceiveQuestion.longItemName(item);
        String sQL = String.format("%.2f", Float.valueOf(item.getQualityLevel()));
        String sDMG = String.format("%.2f", Float.valueOf(item.getDamage()));
        String receive = "text=\"\"";
        String ret = "text=\"\"";
        String gold = "";
        String silver = "";
        String copper = "";
        String iron = "";
        String sender = "";
        String expire = "";
        if (isTopLevel) {
            PlayerState ps;
            receive = "checkbox=\"true\";id=\"" + id + "receive\"";
            ret = m.rejected ? "text=\"n/a\"" : "checkbox=\"true\";id=\"" + id + "return\"";
            if (change != null) {
                gold = "" + change.getGoldCoins();
                silver = "" + change.getSilverCoins();
                copper = "" + change.getCopperCoins();
                iron = "" + change.getIronCoins();
            }
            sender = (ps = PlayerInfoFactory.getPlayerState(m.getSender())) != null ? ps.getPlayerName() : "Unknown";
            expire = "" + Server.getTimeFor(Math.max(0L, m.expiration - System.currentTimeMillis()));
        }
        String spells = "";
        ItemSpellEffects eff = item.getSpellEffects();
        if (eff != null) {
            SpellEffect[] speffs = eff.getEffects();
            for (int z = 0; z < speffs.length; ++z) {
                if (spells.length() > 0) {
                    spells = spells + ",";
                }
                spells = spells + speffs[z].getName() + " [" + (int)speffs[z].power + "]";
            }
        }
        String extra = "";
        if (item.getColor() != -1) {
            extra = " [" + WurmColor.getRGBDescription(item.getColor()) + "]";
        }
        if (item.getTemplateId() == 866) {
            try {
                extra = extra + " [" + CreatureTemplateFactory.getInstance().getTemplate(item.getData2()).getName() + "]";
            }
            catch (NoSuchCreatureTemplateException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        if (spells.length() == 0) {
            spells = "no enchants";
        }
        String hover = itemName + " - " + spells + extra;
        buf.append("row{id=\"" + id + "\";hover=\"" + hover + "\";name=\"" + itemName + "\";rarity=\"" + item.getRarity() + "\";children=\"" + children + "\";col{text=\"" + sQL + "\"};col{text=\"" + sDMG + "\"};col{" + receive + "};col{" + ret + "};col{text=\"" + gold + "\"};col{text=\"" + silver + "\"};col{text=\"" + copper + "\"};col{text=\"" + iron + "\"};col{text=\"" + sender + "\"};col{text=\"" + expire.replace(",", " ") + "\"}}");
        for (int c = 0; c < contained.length; ++c) {
            buf.append(this.addItem(id + "c" + c, contained[c], m, false));
        }
        return buf.toString();
    }

    public static String longItemName(Item litem) {
        StringBuilder sb = new StringBuilder();
        if (litem.getRarity() == 1) {
            sb.append("rare ");
        } else if (litem.getRarity() == 2) {
            sb.append("supreme ");
        } else if (litem.getRarity() == 3) {
            sb.append("fantastic ");
        }
        String name = litem.getName().length() == 0 ? litem.getTemplate().getName() : litem.getName();
        MaterialUtilities.appendNameWithMaterialSuffix(sb, name.replace("\"", "''"), litem.getMaterial());
        if (litem.getDescription().length() > 0) {
            sb.append(" (" + litem.getDescription() + ")");
        }
        return sb.toString();
    }
}

