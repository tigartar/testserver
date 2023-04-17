/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.WurmHarvestables;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import java.util.Properties;

public final class GMSelectHarvestable
extends Question {
    private WurmHarvestables.Harvestable[] harvestables = null;
    private Item paper;

    public GMSelectHarvestable(Creature aResponder, Item apaper) {
        super(aResponder, "Select Harvestabke", "Select Harvestabke", 140, -10L);
        this.paper = apaper;
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        String sel = answers.getProperty("harvestable");
        int selId = Integer.parseInt(sel);
        WurmHarvestables.Harvestable harvestable = this.harvestables[selId];
        this.paper.setAuxData((byte)(harvestable.getHarvestableId() + 8));
        this.paper.setData1(99);
        this.paper.setInscription(harvestable.getName() + " report", this.getResponder().getName(), 0);
        this.paper.setName(harvestable.getName() + " report", true);
        this.getResponder().getCommunicator().sendNormalServerMessage("You carefully finish writing the " + harvestable.getName() + " report and sign it.");
    }

    @Override
    public void sendQuestion() {
        this.harvestables = WurmHarvestables.getHarvestables();
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        buf.append("harray{label{text=\"Harvestable\"};");
        buf.append("dropdown{id=\"harvestable\";default=\"0\";options=\"");
        for (int i = 0; i < this.harvestables.length; ++i) {
            if (i > 0) {
                buf.append(",");
            }
            WurmHarvestables.Harvestable harvestable = this.harvestables[i];
            buf.append(harvestable.getName().replace(",", "") + " (" + harvestable.getHarvestableId() + ")");
        }
        buf.append("\"}}");
        buf.append("label{text=\"\"}");
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 120, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

