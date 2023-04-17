/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.questions.Question;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public final class KingdomHistory
extends Question {
    public KingdomHistory(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 66, aTarget);
    }

    @Override
    public void answer(Properties answers) {
    }

    @Override
    public void sendQuestion() {
        String lHtml = this.getBmlHeaderWithScroll();
        StringBuilder buf = new StringBuilder(lHtml);
        Map<Integer, King> kings = King.eras;
        HashMap<String, LinkedList<King>> counters = new HashMap<String, LinkedList<King>>();
        for (King k : kings.values()) {
            LinkedList<King> kinglist = (LinkedList<King>)counters.get(k.kingdomName);
            if (kinglist == null) {
                kinglist = new LinkedList<King>();
            }
            kinglist.add(k);
            counters.put(k.kingdomName, kinglist);
        }
        for (Map.Entry entry : counters.entrySet()) {
            this.addKing((Collection)entry.getValue(), (String)entry.getKey(), buf);
        }
        if (Servers.localServer.isChallengeServer()) {
            for (Kingdom kingdom : Kingdoms.getAllKingdoms()) {
                if (!kingdom.existsHere()) continue;
                buf.append("label{text=\"" + kingdom.getName() + " points:\"};");
                buf.append("label{text=\"" + kingdom.getWinpoints() + "\"};text{text=''};");
            }
        }
        buf.append(this.createAnswerButton3());
        this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    public void addKing(Collection<King> kings, String kingdomName, StringBuilder buf) {
        buf.append("text{type=\"bold\";text=\"History of " + kingdomName + ":\"}text{text=''}");
        buf.append("table{rows='" + (kings.size() + 1) + "'; cols='10';label{text='Ruler'};label{text='Capital'};label{text='Start Land'};label{text='End Land'};label{text='Land Difference'};label{text='Levels Killed'};label{text='Levels Lost'};label{text='Levels Appointed'};label{text='Start Date'};label{text='End Date'};");
        for (King k : kings) {
            buf.append("label{text=\"" + k.getFullTitle() + "\"};");
            buf.append("label{text=\"" + k.capital + "\"};");
            buf.append("label{text=\"" + String.format("%.2f%%", Float.valueOf(k.startLand)) + "\"};");
            buf.append("label{text=\"" + String.format("%.2f%%", Float.valueOf(k.currentLand)) + "\"};");
            buf.append("label{text=\"" + String.format("%.2f%%", Float.valueOf(k.currentLand - k.startLand)) + "\"};");
            buf.append("label{text=\"" + k.levelskilled + "\"};");
            buf.append("label{text=\"" + k.levelslost + "\"};");
            buf.append("label{text=\"" + k.appointed + "\"};");
            buf.append("label{text=\"" + WurmCalendar.getDateFor(k.startWurmTime) + "\"};");
            if (k.endWurmTime > 0L) {
                buf.append("label{text=\"" + WurmCalendar.getDateFor(k.endWurmTime) + "\"};");
                continue;
            }
            buf.append("label{text=\"N/A\"};");
        }
        buf.append("}");
        buf.append("text{text=\"\"}");
    }
}

