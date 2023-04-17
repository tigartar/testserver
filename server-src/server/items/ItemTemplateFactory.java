/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class ItemTemplateFactory {
    private static Logger logger = Logger.getLogger(ItemTemplateFactory.class.getName());
    private static ItemTemplateFactory instance;
    private static Map<Integer, ItemTemplate> templates;
    private static Set<ItemTemplate> missionTemplates;
    private static Set<ItemTemplate> epicMissionTemplates;
    private static Map<String, ItemTemplate> templatesByName;

    public static ItemTemplateFactory getInstance() {
        if (instance == null) {
            instance = new ItemTemplateFactory();
        }
        return instance;
    }

    private ItemTemplateFactory() {
    }

    public ItemTemplate getTemplateOrNull(int templateId) {
        return templates.get(templateId);
    }

    public String getTemplateName(int templateId) {
        ItemTemplate it = this.getTemplateOrNull(templateId);
        if (it != null) {
            return it.getName();
        }
        return "";
    }

    public ItemTemplate getTemplate(int templateId) throws NoSuchTemplateException {
        ItemTemplate toReturn = templates.get(templateId);
        if (toReturn == null) {
            throw new NoSuchTemplateException("No item template with id " + templateId);
        }
        return toReturn;
    }

    public ItemTemplate getTemplate(String name) {
        return templatesByName.get(name);
    }

    public ItemTemplate[] getTemplates() {
        ItemTemplate[] toReturn = new ItemTemplate[templates.size()];
        return templates.values().toArray(toReturn);
    }

    public ItemTemplate[] getMissionTemplates() {
        ItemTemplate[] toReturn = new ItemTemplate[missionTemplates.size()];
        return missionTemplates.toArray(toReturn);
    }

    public ItemTemplate[] getEpicMissionTemplates() {
        ItemTemplate[] toReturn = new ItemTemplate[epicMissionTemplates.size()];
        return epicMissionTemplates.toArray(toReturn);
    }

    public ItemTemplate[] getMostDamageUpdated() {
        ItemTemplate[] temps = this.getTemplates();
        Arrays.sort(temps, new Comparator<ItemTemplate>(){

            @Override
            public int compare(ItemTemplate o1, ItemTemplate o2) {
                if (o1.damUpdates == o2.damUpdates) {
                    return 0;
                }
                if (o1.damUpdates > o2.damUpdates) {
                    return 1;
                }
                return -1;
            }
        });
        return temps;
    }

    public ItemTemplate[] getMostMaintenanceUpdated() {
        ItemTemplate[] temps = this.getTemplates();
        Arrays.sort(temps, new Comparator<ItemTemplate>(){

            @Override
            public int compare(ItemTemplate o1, ItemTemplate o2) {
                if (o1.maintUpdates == o2.maintUpdates) {
                    return 0;
                }
                if (o1.maintUpdates > o2.maintUpdates) {
                    return 1;
                }
                return -1;
            }
        });
        return temps;
    }

    public ItemTemplate createItemTemplate(int templateId, int size, String name, String plural, String itemDescriptionSuperb, String itemDescriptionNormal, String itemDescriptionBad, String itemDescriptionRotten, String itemDescriptionLong, short[] itemTypes, short imageNumber, short behaviourType, int combatDamage, long decayTime, int centimetersX, int centimetersY, int centimetersZ, int primarySkill, byte[] bodySpaces, String modelName, float difficulty, int weight, byte material, int value, boolean isTraded, int dyeAmountOverrideGrams) throws IOException {
        ItemTemplate it;
        ItemTemplate toReturn = new ItemTemplate(templateId, size, name, plural, itemDescriptionSuperb, itemDescriptionNormal, itemDescriptionBad, itemDescriptionRotten, itemDescriptionLong, itemTypes, imageNumber, behaviourType, combatDamage, decayTime, centimetersX, centimetersY, centimetersZ, primarySkill, bodySpaces, modelName, difficulty, weight, material, value, isTraded);
        toReturn.setDyeAmountGrams(dyeAmountOverrideGrams);
        ItemTemplate old = templates.put(templateId, toReturn);
        if (old != null) {
            logger.warning("Duplicate definition for template " + templateId + " ('" + name + "' overwrites '" + old.getName() + "').");
        }
        if ((it = templatesByName.put(name, toReturn)) != null && toReturn.isFood()) {
            logger.warning("Template " + it.getName() + " already being used.");
        }
        if (toReturn.isMissionItem()) {
            missionTemplates.add(toReturn);
            if (!(toReturn.isNoTake() || toReturn.isNoDrop() || toReturn.getWeightGrams() >= 12000 || toReturn.isRiftLoot() || toReturn.isFood() && toReturn.isBulk() || toReturn.isLiquid() || toReturn.getTemplateId() == 652 || toReturn.getTemplateId() == 737 || toReturn.getTemplateId() == 1097 || toReturn.getTemplateId() == 1306 || toReturn.getTemplateId() == 1414)) {
                epicMissionTemplates.add(toReturn);
            }
        }
        return toReturn;
    }

    public void logAllTemplates() {
        for (ItemTemplate template : templates.values()) {
            logger.info(template.toString());
        }
    }

    public String getModelNameOrNull(String templateName) {
        ItemTemplate i = templatesByName.get(templateName);
        if (i == null) {
            return null;
        }
        return i.getModelName();
    }

    static {
        templates = new HashMap<Integer, ItemTemplate>();
        missionTemplates = new HashSet<ItemTemplate>();
        epicMissionTemplates = new HashSet<ItemTemplate>();
        templatesByName = new HashMap<String, ItemTemplate>();
    }
}

