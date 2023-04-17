/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageMessages;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class VillageMessagePopup
extends Question {
    private static final Logger logger = Logger.getLogger(VillageMessagePopup.class.getName());
    private Village village;
    private InscriptionData papyrusData;
    private String message = null;
    private final Item messageBoard;
    private final Map<Integer, Long> idMap = new HashMap<Integer, Long>();
    private static final String red = "color=\"255,127,127\"";

    public VillageMessagePopup(Creature aResponder, Village aVillage, InscriptionData ins, long aSource, Item noticeBoard) {
        super(aResponder, VillageMessagePopup.getTitle(aVillage), VillageMessagePopup.getQuestion(aVillage), 137, aSource);
        this.messageBoard = noticeBoard;
        this.village = aVillage;
        this.papyrusData = ins;
    }

    private static String getTitle(Village village) {
        return village.getName() + " notice board";
    }

    private static String getQuestion(Village village) {
        return "Add Note";
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
        String selected = aAnswer.getProperty("select");
        int select = Integer.parseInt(selected);
        if (select > 0) {
            long cit = this.idMap.get(select);
            VillageMessages.create(this.village.getId(), this.getResponder().getWurmId(), cit, this.message, this.papyrusData.getPenColour(), cit == -1L);
            if (cit == -1L) {
                this.getResponder().getCommunicator().sendNormalServerMessage("You posted a public notice.");
            } else if (cit == -10L) {
                this.getResponder().getCommunicator().sendNormalServerMessage("You posted a notice.");
            } else {
                this.getResponder().getCommunicator().sendNormalServerMessage("You posted a note to " + this.getPlayerName(cit) + ".");
            }
            Items.destroyItem(this.target);
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        int msglen = this.papyrusData.getInscription().length();
        int mlen = Math.min(msglen, 500);
        this.message = this.papyrusData.getInscription().substring(0, mlen);
        buf.append("input{id=\"answer\";enabled=\"false\";maxchars=\"" + mlen + "\";maxlines=\"-1\";bgcolor=\"200,200,200\";color=\"" + WurmColor.getColorRed(this.papyrusData.getPenColour()) + "," + WurmColor.getColorGreen(this.papyrusData.getPenColour()) + "," + WurmColor.getColorBlue(this.papyrusData.getPenColour()) + "\";text=\"" + this.message + "\"}");
        buf.append("text{text=\"\"}");
        if (mlen < msglen) {
            buf.append("label{color=\"255,127,127\"text=\"Message is too long, so will be truncated.\"};");
        }
        buf.append("harray{text{type=\"bold\";text=\"Post\"};dropdown{id=\"select\";options=\"");
        buf.append("no where");
        this.idMap.put(0, -10L);
        if (this.messageBoard.mayPostNotices(this.getResponder())) {
            if (this.getResponder().getCitizenVillage() == this.village) {
                this.idMap.put(this.idMap.size(), -10L);
                buf.append(",as village notice");
            }
            this.idMap.put(this.idMap.size(), -1L);
            buf.append(",as public notice");
        }
        if (this.messageBoard.mayAddPMs(this.getResponder())) {
            Object[] citizens = this.village.getCitizens();
            Arrays.sort(citizens);
            for (Object c : citizens) {
                if (!((Citizen)c).isPlayer() || ((Citizen)c).getId() == this.getResponder().getWurmId() || this.getPlayerName(((Citizen)c).getId()).length() <= 0) continue;
                this.idMap.put(this.idMap.size(), ((Citizen)c).getId());
                buf.append(",to " + ((Citizen)c).getName());
            }
        }
        buf.append("\"}}");
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(400, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private final String getPlayerName(long id) {
        PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(id);
        if (info == null) {
            return "";
        }
        return info.getName();
    }
}

