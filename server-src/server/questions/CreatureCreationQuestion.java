/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;

public final class CreatureCreationQuestion
extends Question
implements CreatureTemplateIds {
    private final LinkedList<CreatureTemplate> cretemplates = new LinkedList();
    private final int tilex;
    private final int tiley;
    private final long structureId;
    private final int layer;

    public CreatureCreationQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, int aTilex, int aTiley, int aLayer, long structureId) {
        super(aResponder, aTitle, aQuestion, 6, aTarget);
        this.tilex = aTilex;
        this.tiley = aTiley;
        this.layer = aLayer;
        this.structureId = structureId;
    }

    public int getTileX() {
        return this.tilex;
    }

    public int getTileY() {
        return this.tiley;
    }

    public int getLayer() {
        return this.layer;
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseCreatureCreationQuestion(this);
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("harray{label{text='Player name:'}input{id='pname';maxchars='30'}}");
        buf.append("harray{label{text='Or, if left empty:'}");
        Object[] templates = CreatureTemplateFactory.getInstance().getTemplates();
        Arrays.sort(templates);
        buf.append("dropdown{id='data1';options=\"");
        for (int x = 0; x < templates.length; ++x) {
            if (((CreatureTemplate)templates[x]).isUnique() && this.getResponder().getPower() < 3 && !Servers.isThisATestServer()) continue;
            if (this.cretemplates.size() > 0) {
                buf.append(",");
            }
            if (((CreatureTemplate)templates[x]).getTemplateId() == 119) continue;
            this.cretemplates.add((CreatureTemplate)templates[x]);
            buf.append(((CreatureTemplate)templates[x]).getName());
        }
        buf.append("\"}}");
        buf.append("table{rows=\"1\";cols=\"3\";");
        buf.append("text{type=\"bold\";text=\"Gender\"};radio{group=\"gender\";id=\"female\";text=\"Female\"};radio{group=\"gender\";id=\"male\";text=\"Male\";selected=\"true\"};}");
        buf.append("harray{label{text='Age(2..100):'};input{id='age';maxchars='3';text='0'};label{text=\" 0 = random age\"}}");
        buf.append("harray{label{text='Number of creatures:'};input{id='number';maxchars='4';text='1'}}");
        buf.append("harray{label{text='Name (instead of template name):'};input{id='cname';maxchars='40';text=''}}");
        buf.append("harray{text{type=\"bold\";text=\"Type\"};label{text=\"(ignored if not applicable!)\"};};");
        buf.append("table{rows=\"1\";cols=\"2\";");
        buf.append("radio{group=\"tid\";id=\"0\";selected=\"true\"};label{text=\"None\"};}");
        buf.append("table{rows=\"3\";cols=\"8\";");
        buf.append("radio{group=\"tid\";id=\"5\"};label{text=\"Alert\"};");
        buf.append("radio{group=\"tid\";id=\"11\"};label{text=\"Diseased\"};");
        buf.append("radio{group=\"tid\";id=\"9\"};label{text=\"Hardened\"};");
        buf.append("radio{group=\"tid\";id=\"10\"};label{text=\"Scared\"};");
        buf.append("radio{group=\"tid\";id=\"2\"};label{text=\"Angry\"};");
        buf.append("radio{group=\"tid\";id=\"1\"};label{text=\"Fierce\"};");
        buf.append("radio{group=\"tid\";id=\"7\"};label{text=\"Lurking\"};");
        buf.append("radio{group=\"tid\";id=\"4\"};label{text=\"Slow\"};");
        buf.append("radio{group=\"tid\";id=\"99\"};label{text=\"Champion\"};");
        buf.append("radio{group=\"tid\";id=\"6\"};label{text=\"Greenish\"};");
        buf.append("radio{group=\"tid\";id=\"3\"};label{text=\"Raging\"};");
        buf.append("radio{group=\"tid\";id=\"8\"};label{text=\"Sly\"};");
        buf.append("}");
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    CreatureTemplate getTemplate(int aTemplateId) {
        return this.cretemplates.get(aTemplateId);
    }

    public long getStructureId() {
        return this.structureId;
    }
}

