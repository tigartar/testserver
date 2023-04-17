/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageMessage;
import com.wurmonline.server.villages.VillageMessages;
import com.wurmonline.server.villages.VillageRole;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

public class VillageMessageBoard
extends Question {
    private static final Logger logger = Logger.getLogger(VillageMessageBoard.class.getName());
    private Village village;
    private Item messageBoard;

    public VillageMessageBoard(Creature aResponder, Village aVillage, Item noticeBoard) {
        super(aResponder, VillageMessageBoard.getTitle(aVillage), "", 136, noticeBoard.getWurmId());
        this.village = aVillage;
        this.messageBoard = noticeBoard;
    }

    private static String getTitle(Village village) {
        return village.getName() + " notice board";
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
        for (String key : this.getAnswer().stringPropertyNames()) {
            if (key.startsWith("del")) {
                String sid = key.substring(3);
                long posted = Long.parseLong(sid);
                VillageMessages.delete(this.village.getId(), this.getResponder().getWurmId(), posted);
                VillageMessageBoard vmb = new VillageMessageBoard(this.getResponder(), this.village, this.messageBoard);
                vmb.sendQuestion();
                return;
            }
            if (key.startsWith("rem")) {
                String sid = key.substring(3);
                long posted = Long.parseLong(sid);
                VillageMessages.delete(this.village.getId(), -10L, posted);
                VillageMessageBoard vmb = new VillageMessageBoard(this.getResponder(), this.village, this.messageBoard);
                vmb.sendQuestion();
                return;
            }
            if (!key.startsWith("pub")) continue;
            String sid = key.substring(3);
            long posted = Long.parseLong(sid);
            VillageMessages.delete(this.village.getId(), -1L, posted);
            VillageMessageBoard vmb = new VillageMessageBoard(this.getResponder(), this.village, this.messageBoard);
            vmb.sendQuestion();
            return;
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append("border{border{size=\"20,20\";null;null;label{type='bold';text=\"" + this.question + "\"};harray{label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + this.getId() + "\"}");
        buf.append("label{type=\"bold\";text=\"Public notices\"}");
        if (this.messageBoard.mayAccessHold(this.getResponder())) {
            Object[] publicNotices = VillageMessages.getVillageMessages(this.village.getId(), -1L);
            if (publicNotices.length > 0) {
                Arrays.sort(publicNotices);
                for (Object vm : publicNotices) {
                    buf.append(this.showMessage((VillageMessage)vm));
                }
                buf.append("label{text=\"\"}");
            } else {
                buf.append("label{type=\"bold\";text=\"none.\"}");
            }
        } else {
            buf.append("label{type=\"bold\";text=\"no permission.\"}");
        }
        if (this.messageBoard.mayAccessHold(this.getResponder()) && this.getResponder().getCitizenVillage() == this.village) {
            Object[] notices = VillageMessages.getVillageMessages(this.village.getId(), -10L);
            if (notices.length > 0) {
                buf.append("label{type=\"bold\";text=\"Village notices for: " + this.village.getName() + "\"}");
                Arrays.sort(notices);
                for (Object vm : notices) {
                    buf.append(this.showMessage((VillageMessage)vm));
                }
            } else {
                buf.append("label{text=\"No village notices\"}");
            }
            buf.append("label{text=\"\"}");
            buf.append("text{type=\"bold\";text=\"Personal messages for: " + this.getResponder().getName() + "\"}");
            Object[] personals = VillageMessages.getVillageMessages(this.village.getId(), this.getResponder().getWurmId());
            if (personals.length > 0) {
                Arrays.sort(personals);
                for (Object vm : personals) {
                    buf.append(this.showMessage((VillageMessage)vm));
                }
            } else {
                buf.append("label{text=\"None.\"}");
            }
        }
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private String showMessage(VillageMessage vm) {
        StringBuilder buf = new StringBuilder();
        PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(vm.getPosterId());
        if (info == null) {
            return buf.toString();
        }
        VillageRole vr = this.village.getRoleFor(vm.getPosterId());
        if (vr == null) {
            return buf.toString();
        }
        buf.append("label{text=\"\"}");
        buf.append("input{id=\"ans" + vm.getPostedTime() + "\";enabled=\"false\";maxchars=\"" + vm.getMessage().length() + "\";maxlines=\"-1\";bgcolor=\"200,200,200\";color=\"" + WurmColor.getColorRed(vm.getPenColour()) + "," + WurmColor.getColorGreen(vm.getPenColour()) + "," + WurmColor.getColorBlue(vm.getPenColour()) + "\";text=\"" + vm.getMessage() + "\"}");
        if (vm.getToId() == -10L || vm.getToId() == -1L) {
            String id = vm.getToId() == -10L ? "rem" + vm.getPostedTime() : "pub" + vm.getPostedTime();
            String note = vm.getToId() == -10L ? "village notice" : "public notice";
            String delButton = "harray{label{text=\" \"};button{text=\"Delete\";id=\"" + id + "\"confirm=\"You are about to delete a " + note + " posted by " + vm.getPosterName() + ".\";question=\"Do you really want to do that?\"}label{text=\" \"};}";
            if (!this.messageBoard.mayCommand(this.getResponder()) && vm.getPosterId() != this.getResponder().getWurmId()) {
                delButton = "null;";
            }
            buf.append("border{size=\"20,20\";null;null;harray{label{text=\"Posted by: " + info.getName() + "  \"};label{text=\"Role: " + vr.getName() + "  \"};label{text=\"When: " + vm.getDate() + "\"}};" + delButton + "null;}");
        } else {
            String delButton = "harray{label{text=\" \"};button{text=\"Delete\";id=\"del" + vm.getPostedTime() + "\"confirm=\"You are about to delete a personal message from " + vm.getPosterName() + ".\";question=\"Do you really want to do that?\"}label{text=\" \"};}";
            buf.append("border{size=\"20,20\";null;null;harray{label{text=\"From:" + info.getName() + "  \"};label{text=\"Role:" + vr.getName() + "  \"};label{text=\"When:" + vm.getDate() + "\"}};" + delButton + "null;}");
        }
        return buf.toString();
    }
}

