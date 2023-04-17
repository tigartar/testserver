/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.Features;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.VillageTokenBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.KingdomHistory;
import com.wurmonline.server.questions.KingdomStatusQuestion;
import com.wurmonline.server.questions.ManageFriends;
import com.wurmonline.server.questions.ManageObjectList;
import com.wurmonline.server.questions.PlayerProfileQuestion;
import com.wurmonline.server.questions.WagonerDeliveriesQuestion;
import com.wurmonline.server.villages.Village;
import java.util.LinkedList;
import java.util.List;

public final class ManageMenu
implements MiscConstants {
    private ManageMenu() {
    }

    static List<ActionEntry> getBehavioursFor(Creature performer) {
        LinkedList<ActionEntry> alist = new LinkedList<ActionEntry>();
        LinkedList<ActionEntry> slist = new LinkedList<ActionEntry>();
        alist.add(new ActionEntry(663, "Animals", "managing"));
        alist.add(new ActionEntry(664, "Buildings", "managing"));
        alist.add(new ActionEntry(665, "Carts and Wagons", "managing"));
        if (Features.Feature.WAGONER.isEnabled()) {
            alist.add(Actions.actionEntrys[916]);
        }
        alist.add(Actions.actionEntrys[661]);
        alist.add(new ActionEntry(667, "Gates", "managing"));
        alist.add(new ActionEntry(364, "MineDoors", "managing"));
        alist.add(Actions.actionEntrys[566]);
        alist.add(Actions.actionEntrys[690]);
        if (performer.getCitizenVillage() != null) {
            Village village = performer.getCitizenVillage();
            slist.addAll(VillageTokenBehaviour.getSettlementMenu(performer, false, village, village));
            alist.addAll(slist);
        }
        alist.add(new ActionEntry(668, "Ships", "managing"));
        if (Features.Feature.WAGONER.isEnabled() && Creatures.getManagedWagonersFor((Player)performer, -1).length > 0) {
            alist.add(Actions.actionEntrys[863]);
        }
        int sz = slist.size() > 0 ? alist.size() - slist.size() + 1 : alist.size();
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.add(new ActionEntry((short)(-sz), "Manage", "Manage"));
        toReturn.addAll(alist);
        return toReturn;
    }

    static boolean isManageAction(Creature performer, short action) {
        if (action == 663) {
            return true;
        }
        if (action == 664) {
            return true;
        }
        if (action == 665) {
            return true;
        }
        if (action == 687) {
            return true;
        }
        if (action == 667) {
            return true;
        }
        if (action == 364) {
            return true;
        }
        if (action == 668) {
            return true;
        }
        if (action == 669) {
            return true;
        }
        if (action == 670) {
            return true;
        }
        if (action == 690) {
            return true;
        }
        if (action == 661) {
            return true;
        }
        if (action == 566) {
            return true;
        }
        if (action == 77) {
            return true;
        }
        if (action == 71) {
            return true;
        }
        if (action == 72) {
            return true;
        }
        if (action == 355) {
            return true;
        }
        if (action == 356) {
            return true;
        }
        if (action == 80) {
            return true;
        }
        if (action == 67) {
            return true;
        }
        if (action == 68) {
            return true;
        }
        if (action == 540) {
            return true;
        }
        if (action == 66) {
            return true;
        }
        if (action == 69) {
            return true;
        }
        if (action == 70) {
            return true;
        }
        if (action == 76) {
            return true;
        }
        if (action == 481) {
            return true;
        }
        if (action == 863) {
            return true;
        }
        if (action == 916) {
            return true;
        }
        return action == 738;
    }

    static boolean action(Action act, Creature performer, short action, float counter) {
        if (action == 663) {
            ManageObjectList mol = new ManageObjectList(performer, ManageObjectList.Type.ANIMAL1);
            mol.sendQuestion();
            return true;
        }
        if (action == 664) {
            ManageObjectList mol = new ManageObjectList(performer, ManageObjectList.Type.BUILDING);
            mol.sendQuestion();
            return true;
        }
        if (action == 665) {
            ManageObjectList mol = new ManageObjectList(performer, ManageObjectList.Type.LARGE_CART);
            mol.sendQuestion();
            return true;
        }
        if (action == 667) {
            ManageObjectList mol = new ManageObjectList(performer, ManageObjectList.Type.GATE);
            mol.sendQuestion();
            return true;
        }
        if (action == 364) {
            ManageObjectList mol = new ManageObjectList(performer, ManageObjectList.Type.MINEDOOR);
            mol.sendQuestion();
            return true;
        }
        if (action == 668) {
            ManageObjectList mol = new ManageObjectList(performer, ManageObjectList.Type.SHIP);
            mol.sendQuestion();
            return true;
        }
        if (action == 863) {
            ManageObjectList mol = new ManageObjectList(performer, ManageObjectList.Type.WAGONER);
            mol.sendQuestion();
            return true;
        }
        if (action == 916) {
            WagonerDeliveriesQuestion mdq = new WagonerDeliveriesQuestion(performer, -10L, true);
            mdq.sendQuestion();
            return true;
        }
        if (action == 670 && performer.getCitizenVillage() != null) {
            Methods.sendManageUpkeep(performer, null);
            return true;
        }
        if (action == 661) {
            ManageFriends mf = new ManageFriends(performer);
            mf.sendQuestion();
            return true;
        }
        if (action == 566) {
            PlayerProfileQuestion kq = new PlayerProfileQuestion(performer);
            kq.sendQuestion();
            return true;
        }
        if (action == 690) {
            ManageObjectList mol = new ManageObjectList(performer, ManageObjectList.Type.SEARCH);
            mol.sendQuestion();
            return true;
        }
        Village village = performer.getCitizenVillage();
        if (action == 77) {
            Methods.sendVillageInfo(performer, null);
            return true;
        }
        if (village != null && action == 71) {
            Methods.sendVillageHistory(performer, null);
            return true;
        }
        if (village != null && action == 72) {
            Methods.sendAreaHistory(performer, null);
            return true;
        }
        if (village != null && action == 355) {
            KingdomStatusQuestion kq = new KingdomStatusQuestion(performer, "Kingdom status", "Kingdoms", performer.getWurmId());
            kq.sendQuestion();
            return true;
        }
        if (village != null && action == 356) {
            KingdomHistory kq = new KingdomHistory(performer, "Kingdom history", "History of the kingdoms", performer.getWurmId());
            kq.sendQuestion();
            return true;
        }
        if (village != null && action == 80) {
            if (village.mayDoDiplomacy(performer)) {
                Methods.sendManageAllianceQuestion(performer, null);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You may not perform diplomacy for " + village.getName() + ".");
            }
            return true;
        }
        if (village != null && action == 67) {
            if (village.isActionAllowed((short)67, performer)) {
                Methods.sendManageVillageGuardsQuestion(performer, null);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You may not manage guards for " + village.getName() + ".");
            }
            return true;
        }
        if (village != null && action == 68) {
            if (village.isActionAllowed((short)68, performer)) {
                Methods.sendManageVillageSettingsQuestion(performer, null);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You may not manage settings for " + village.getName() + ".");
            }
            return true;
        }
        if (village != null && action == 540) {
            if (village.isActionAllowed((short)540, performer)) {
                Methods.sendManageVillageRolesQuestion(performer, null);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You may not manage roles for " + village.getName() + ".");
            }
            return true;
        }
        if (village != null && action == 66) {
            if (village.isActionAllowed((short)66, performer)) {
                Methods.sendManageVillageCitizensQuestion(performer, null);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You may not manage citizens for " + village.getName() + ".");
            }
            return true;
        }
        if (village != null && action == 69) {
            int laction = 69;
            if (village.isActionAllowed((short)69, performer)) {
                Methods.sendReputationManageQuestion(performer, null);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You may not manage reputations for " + village.getName() + ".");
            }
            return true;
        }
        if (village != null && action == 70) {
            int laction = 70;
            if (village.isActionAllowed((short)70, performer)) {
                Methods.sendManageVillageGatesQuestion(performer, null);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You may not manage gates for " + village.getName() + ".");
            }
            return true;
        }
        if (village != null && action == 76) {
            if (village.isActionAllowed((short)76, performer)) {
                Methods.sendExpandVillageQuestion(performer, null);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You may not resize the settlement " + village.getName() + ".");
            }
            return true;
        }
        if (village != null && action == 481) {
            int laction = 481;
            if (village.isActionAllowed((short)481, performer)) {
                Methods.sendConfigureTwitter(performer, -10L, true, village.getName());
            } else {
                performer.getCommunicator().sendNormalServerMessage("You may not configure twitter for " + village.getName() + ".");
            }
            return true;
        }
        return true;
    }
}

