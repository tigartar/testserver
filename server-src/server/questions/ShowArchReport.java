/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.villages.DeadVillage;
import com.wurmonline.server.villages.Villages;
import java.util.Properties;

public class ShowArchReport
extends Question {
    private Item reportItem = null;

    public ShowArchReport(Creature aResponder, Item report) {
        super(aResponder, report.getName(), report.getName(), 151, -10L);
        this.reportItem = report;
    }

    @Override
    public void answer(Properties answers) {
    }

    @Override
    public void sendQuestion() {
        if (this.reportItem == null) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeaderNoQuestion());
        DeadVillage dv = Villages.getDeadVillage(this.reportItem.getData());
        if (dv != null) {
            buf.append("label{type=\"bold\";text=\"" + dv.getDeedName() + " Archaeological Report\";}");
            buf.append("text{text='';}");
            if (this.reportItem.getAuxBit(0)) {
                if (this.reportItem.getAuxBit(1)) {
                    if (this.reportItem.getAuxBit(2)) {
                        if (this.reportItem.getAuxBit(3)) {
                            buf.append("text{text='There is enough location information written in this report that you believe that you could use it to track down the exact location of this village.';}");
                            buf.append("text{text='Maybe following the report to the location will have some effect.';}");
                        } else {
                            buf.append("text{text='There is almost enough location information written in this report to be able to use it to track down this village.';}");
                            buf.append("text{text='Perhaps investigating around the village area some more will provide more location clues.';}");
                        }
                    } else {
                        buf.append("text{text='Written in this report is a decent number of location markers and reference to nearby landmarks, but you doubt it is enough information to be able to find the village.';}");
                        buf.append("text{text='Perhaps investigating around the village area some more will provide more location clues.';}");
                    }
                } else {
                    buf.append("text{text='There are a few basic hints to where this village once may have resided, but not enough for anything meaningful.';}");
                    buf.append("text{text='Perhaps investigating around the village area some more will provide more location clues.';}");
                }
            }
            buf.append("label{text='';}");
            buf.append("label{type='bold';text='Last Mayor:';};");
            if (this.reportItem.getAuxBit(4)) {
                buf.append("text{text='" + dv.getMayorName() + "';}");
            } else {
                buf.append("text{text='[ Not Recorded ]';}");
            }
            buf.append("label{type='bold';text='Village Founder:';};");
            if (this.reportItem.getAuxBit(5)) {
                buf.append("text{text='" + dv.getFounderName() + "';}");
            } else {
                buf.append("text{text='[ Not Recorded ]';}");
            }
            buf.append("label{type='bold';text='Abandoned for:';};");
            if (this.reportItem.getAuxBit(6)) {
                buf.append("text{text='" + DeadVillage.getTimeString(dv.getTimeSinceDisband(), dv.getTimeSinceDisband() > 12.0f) + "';}");
            } else {
                buf.append("text{text='[ Not Recorded ]';}");
            }
            buf.append("label{type='bold';text='Inhabited for:';};");
            if (this.reportItem.getAuxBit(7)) {
                buf.append("text{text='" + DeadVillage.getTimeString(dv.getTotalAge(), false) + "';}");
            } else {
                buf.append("text{text='[ Not Recorded ]';}");
            }
        } else {
            buf.append("text{type='bold';text='The report seems to be written in some foreign language. Perhaps it is meant for a village from some distant lands.'}");
        }
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(500, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

