/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.Items;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.VillageTokenBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.KingdomHistory;
import com.wurmonline.server.questions.KingdomStatusQuestion;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class VillageDeedBehaviour
extends ItemBehaviour {
    private static final Logger logger = Logger.getLogger(VillageDeedBehaviour.class.getName());

    VillageDeedBehaviour() {
        super((short)24);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
        toReturn.addAll(this.getBehavioursForPapers(performer, target));
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
        toReturn.addAll(this.getBehavioursForPapers(performer, target));
        return toReturn;
    }

    List<ActionEntry> getBehavioursForPapers(Creature performer, Item target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        long ownerId = target.getOwnerId();
        if (ownerId == performer.getWurmId()) {
            int villageId = target.getData2();
            if (villageId <= 0) {
                if (target.getTemplateId() != 663 && target.getTemplateId() != 862) {
                    toReturn.add(new ActionEntry((short)(Servers.localServer.testServer ? -2 : -1), "Settlement", "Settlement options"));
                    toReturn.add(Actions.actionEntrys[466]);
                    if (Servers.localServer.testServer) {
                        toReturn.add(Actions.actionEntrys[65]);
                    }
                }
                toReturn.add(new ActionEntry(-1, "Settlement", "Settlement options"));
                toReturn.add(Actions.actionEntrys[65]);
            } else if (target.getTemplateId() != 663) {
                int nums = -2;
                toReturn.add(new ActionEntry(-2, "Settlement", "Settlement options"));
                toReturn.add(Actions.actionEntrys[78]);
                toReturn.add(Actions.actionEntrys[466]);
            } else {
                try {
                    Village curVill = Villages.getVillage(villageId);
                    toReturn.addAll(VillageTokenBehaviour.getSettlementMenu(performer, false, curVill, curVill));
                }
                catch (NoSuchVillageException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
        return toReturn;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean action(Action act, Creature performer, Item target, short action, float counter) {
        boolean done;
        block67: {
            block68: {
                done = true;
                if (action != 1) break block68;
                if (!target.isNewDeed()) {
                    if (target.isVillageDeed()) {
                        try {
                            Village village = Villages.getVillage(target.getData2());
                            performer.getCommunicator().sendNormalServerMessage("This is the village deed for " + village.getName() + ". You should replace it with the new version.");
                        }
                        catch (NoSuchVillageException nss) {
                            int templateId = target.getTemplateId();
                            int size = Villages.getSizeForDeed(templateId);
                            performer.getCommunicator().sendNormalServerMessage("A paper giving the possessor the right to found a village of the size " + size + ". You should refund it and use the new version instead.");
                            if (target.getData2() >= 1) {
                                logger.log(Level.WARNING, nss.getMessage(), nss);
                            }
                            break block67;
                        }
                    }
                    if (target.isHomesteadDeed()) {
                        try {
                            Village stead = Villages.getVillage(target.getData2());
                            performer.getCommunicator().sendNormalServerMessage("This is the homestead deed for " + stead.getName() + ". You should replace it with the new version.");
                        }
                        catch (NoSuchVillageException nss) {
                            int size = Villages.getSizeForDeed(target.getTemplateId());
                            performer.getCommunicator().sendNormalServerMessage("A paper giving the possessor the right to found a homestead of the size " + size + ". You should refund it and use the new version instead.");
                            if (target.getData2() >= 1) {
                                logger.log(Level.WARNING, nss.getMessage(), nss);
                            }
                            break block67;
                        }
                    }
                    break block67;
                } else {
                    try {
                        Village village = Villages.getVillage(target.getData2());
                        performer.getCommunicator().sendNormalServerMessage("This is the settlement deed for " + village.getName() + ".");
                    }
                    catch (NoSuchVillageException nss) {
                        if (target.getData2() >= 1) {
                            logger.log(Level.WARNING, nss.getMessage(), nss);
                        }
                        break block67;
                    }
                }
            }
            if (action == 77) {
                done = true;
                if (this.checkPapersOk(performer, target)) {
                    Methods.sendVillageInfo(performer, target);
                }
            } else if (action == 670) {
                done = true;
                if (this.checkPapersOk(performer, target)) {
                    Methods.sendManageUpkeep(performer, target);
                }
            } else if (action == 71) {
                done = true;
                if (this.checkPapersOk(performer, target)) {
                    Methods.sendVillageHistory(performer, target);
                }
            } else if (action == 72) {
                done = true;
                if (this.checkPapersOk(performer, target)) {
                    Methods.sendAreaHistory(performer, target);
                }
            } else if (action == 68) {
                done = true;
                if (this.checkPapersOk(performer, target)) {
                    Methods.sendManageVillageSettingsQuestion(performer, target);
                }
            } else if (action == 540) {
                done = true;
                if (this.checkPapersOk(performer, target)) {
                    Methods.sendManageVillageRolesQuestion(performer, target);
                }
            } else if (action == 69) {
                done = true;
                if (this.checkPapersOk(performer, target)) {
                    Methods.sendReputationManageQuestion(performer, target);
                }
            } else if (action == 67) {
                done = true;
                if (this.checkPapersOk(performer, target)) {
                    Methods.sendManageVillageGuardsQuestion(performer, target);
                }
            } else if (action == 66) {
                done = true;
                if (this.checkPapersOk(performer, target)) {
                    Methods.sendManageVillageCitizensQuestion(performer, target);
                }
            } else if (action == 70) {
                done = true;
                if (this.checkPapersOk(performer, target)) {
                    Methods.sendManageVillageGatesQuestion(performer, target);
                }
            } else if (action == 355) {
                done = true;
                KingdomStatusQuestion kq = new KingdomStatusQuestion(performer, "Kingdom status", "Kingdoms", performer.getWurmId());
                kq.sendQuestion();
            } else if (action == 356) {
                done = true;
                KingdomHistory kq = new KingdomHistory(performer, "Kingdom history", "History of the kingdoms", performer.getWurmId());
                kq.sendQuestion();
            } else if (action == 65) {
                if (!target.isNewDeed() && !Servers.localServer.testServer && target.getTemplateId() != 862) {
                    performer.getCommunicator().sendSafeServerMessage("You need to refund this deed and purchase a new one instead.");
                    return true;
                }
                long ownerId = target.getOwnerId();
                if (ownerId == performer.getWurmId()) {
                    int villageId = target.getData2();
                    if (villageId > 0) {
                        performer.getCommunicator().sendSafeServerMessage("This settlement is already founded!");
                    } else {
                        Methods.sendFoundVillageQuestion(performer, target);
                    }
                } else {
                    logger.log(Level.WARNING, performer.getName() + " trying to manage deed which isn't his.");
                }
            }
        }
        if (action == 76) {
            if (target.isNewDeed()) {
                Methods.sendExpandVillageQuestion(performer, target);
                return done;
            }
            logger.log(Level.WARNING, performer.getName() + " shouldn't be able to do this with a " + target.getName() + ".");
            return done;
        }
        if (action == 466) {
            done = true;
            if (target.isNewDeed()) return done;
            if (!target.isOldDeed()) return done;
            int villageId = target.getData2();
            if (villageId > 0) {
                performer.getCommunicator().sendSafeServerMessage("This village/homestead is already founded. Disband first, then refund.");
                return done;
            }
            if (performer.getWurmId() != target.getOwnerId()) return done;
            long left = target.getValue();
            if (left <= 0L) return done;
            LoginServerWebConnection lsw = new LoginServerWebConnection();
            if (!lsw.addMoney(performer.getWurmId(), performer.getName(), left, "Refund " + target.getWurmId())) {
                performer.getCommunicator().sendSafeServerMessage("Failed to contact your bank. Please try later.");
                return done;
            }
            Items.destroyItem(target.getWurmId());
            return done;
        }
        if (action == 78) {
            done = true;
            int villageId = target.getData2();
            if (villageId <= 0) return done;
            if (target.getOwnerId() != performer.getWurmId()) return done;
            try {
                Village village = Villages.getVillage(villageId);
                village.replaceDeed(performer, target);
                return done;
            }
            catch (NoSuchVillageException nsv) {
                performer.getCommunicator().sendSafeServerMessage("Failed to locate the village that this deed is for!");
                return done;
            }
        }
        if (action == 80) {
            done = true;
            if (!this.checkPapersOk(performer, target)) return done;
            Methods.sendManageAllianceQuestion(performer, target);
            return done;
        }
        if (action != 481) {
            return super.action(act, performer, target, action, counter);
        }
        done = true;
        int villageId = target.getData2();
        if (villageId <= 0) {
            performer.getCommunicator().sendSafeServerMessage("This settlement is not founded yet!");
            logger.log(Level.WARNING, performer.getName() + " managing deed with id " + target.getWurmId() + " but villageid=" + villageId);
            return done;
        }
        try {
            Village currVill = Villages.getVillage(target.getData2());
            if (!currVill.isActionAllowed((short)68, performer) && performer.getPower() < 2) {
                performer.getCommunicator().sendSafeServerMessage("Illegal option.");
                logger.log(Level.WARNING, performer.getName() + " cheating? Illegal option for " + target.getWurmId() + " at villageid=" + villageId);
                return done;
            }
            Methods.sendConfigureTwitter(performer, target.getData2(), true, currVill.getName());
            return done;
        }
        catch (NoSuchVillageException nsv) {
            logger.log(Level.WARNING, performer.getName() + " managing token with id " + target.getWurmId() + " but villageid=" + villageId);
            return done;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean checkPapersOk(Creature performer, Item target) {
        if (!target.isNewDeed()) {
            performer.getCommunicator().sendSafeServerMessage("You need to replace the deed first.");
            return false;
        } else {
            long ownerId = target.getOwnerId();
            if (ownerId == performer.getWurmId()) {
                int villageId = target.getData2();
                if (villageId > 0) return true;
                performer.getCommunicator().sendSafeServerMessage("This settlement is not founded yet!");
                logger.log(Level.WARNING, performer.getName() + " managing deed with id " + target.getWurmId() + " but tried to do illegal action since villageid=" + villageId);
                return false;
            } else {
                logger.log(Level.WARNING, performer.getName() + " trying to manage deed which isn't theirs.");
            }
        }
        return false;
    }
}

