/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.NoSuchEntryException;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class CreationMatrix {
    private static Map<Integer, List<CreationEntry>> matrix = new HashMap<Integer, List<CreationEntry>>();
    private static CreationMatrix instance;
    private static Map<Integer, List<CreationEntry>> advancedEntries;
    private static Map<Integer, List<CreationEntry>> simpleEntries;

    public static CreationMatrix getInstance() {
        if (instance == null) {
            instance = new CreationMatrix();
        }
        return instance;
    }

    private CreationMatrix() {
    }

    public void addCreationEntry(CreationEntry entry) {
        Integer space = entry.getObjectTarget();
        List<CreationEntry> entrys = matrix.get(space);
        if (entrys == null) {
            entrys = new LinkedList<CreationEntry>();
        }
        entrys.add(entry);
        matrix.put(space, entrys);
        space = entry.getObjectCreated();
        if (entry instanceof AdvancedCreationEntry) {
            entrys = advancedEntries.get(space);
            if (entrys == null) {
                entrys = new LinkedList<CreationEntry>();
            }
            entrys.add(entry);
            advancedEntries.put(space, entrys);
        }
        if ((entrys = simpleEntries.get(space)) == null) {
            entrys = new LinkedList<CreationEntry>();
        }
        entrys.add(entry);
        simpleEntries.put(space, entrys);
    }

    public CreationEntry[] getCreationOptionsFor(int sourceTemplateId, int targetTemplateId) {
        List<CreationEntry> entrys = matrix.get(targetTemplateId);
        LinkedList<CreationEntry> options = new LinkedList<CreationEntry>();
        if (entrys != null) {
            for (CreationEntry entry : entrys) {
                if (entry.getObjectSource() != sourceTemplateId || entry.getObjectTarget() != targetTemplateId) continue;
                options.add(entry);
            }
        }
        if ((entrys = matrix.get(sourceTemplateId)) != null) {
            for (CreationEntry entry : entrys) {
                if (entry.getObjectSource() != targetTemplateId || entry.getObjectTarget() != sourceTemplateId) continue;
                options.add(entry);
            }
        }
        CreationEntry[] toReturn = new CreationEntry[options.size()];
        return options.toArray(toReturn);
    }

    public CreationEntry[] getCreationOptionsFor(Item source, Item target) {
        Integer space = target.getTemplateId();
        List<CreationEntry> entrys = matrix.get(space);
        LinkedList<CreationEntry> options = new LinkedList<CreationEntry>();
        if (entrys != null) {
            for (CreationEntry entry : entrys) {
                if (entry.getObjectSource() != source.getTemplateId() || entry.getObjectSourceMaterial() != 0 && entry.getObjectSourceMaterial() != source.getMaterial() || entry.getObjectTarget() != target.getTemplateId() || entry.getObjectTargetMaterial() != 0 && entry.getObjectTargetMaterial() != target.getMaterial()) continue;
                options.add(entry);
            }
        }
        if ((entrys = matrix.get(source.getTemplateId())) != null) {
            for (CreationEntry entry : entrys) {
                if (entry.getObjectSource() != target.getTemplateId() || entry.getObjectSourceMaterial() != 0 && entry.getObjectSourceMaterial() != target.getMaterial() || entry.getObjectTarget() != source.getTemplateId() || entry.getObjectTargetMaterial() != 0 && entry.getObjectTargetMaterial() != source.getMaterial()) continue;
                options.add(entry);
            }
        }
        CreationEntry[] toReturn = new CreationEntry[options.size()];
        return options.toArray(toReturn);
    }

    public AdvancedCreationEntry getAdvancedCreationEntry(int objectCreated) throws NoSuchEntryException {
        if (advancedEntries != null) {
            LinkedList alist = (LinkedList)advancedEntries.get(objectCreated);
            if (alist == null) {
                throw new NoSuchEntryException("No entry with id " + objectCreated);
            }
            AdvancedCreationEntry toReturn = (AdvancedCreationEntry)alist.getFirst();
            return toReturn;
        }
        throw new NoSuchEntryException("No entry with id " + objectCreated);
    }

    public final CreationEntry[] getAdvancedEntries() {
        ArrayList<CreationEntry> list = new ArrayList<CreationEntry>();
        for (Integer in : advancedEntries.keySet()) {
            List<CreationEntry> entrys = advancedEntries.get(in);
            for (CreationEntry ee : entrys) {
                if (list.contains(ee)) continue;
                list.addAll(entrys);
            }
        }
        return list.toArray(new CreationEntry[list.size()]);
    }

    public final Map<Integer, List<CreationEntry>> getAdvancedEntriesMap() {
        return advancedEntries;
    }

    public final CreationEntry[] getAdvancedEntriesNotEpicMission() {
        HashSet<CreationEntry> advanced = new HashSet<CreationEntry>();
        for (List<CreationEntry> entrys : advancedEntries.values()) {
            for (CreationEntry entry : entrys) {
                if (entry.isOnlyCreateEpicTargetMission || !entry.isCreateEpicTargetMission) continue;
                advanced.add(entry);
            }
        }
        return advanced.toArray(new CreationEntry[advanced.size()]);
    }

    public final CreationEntry[] getSimpleEntries() {
        ArrayList<CreationEntry> list = new ArrayList<CreationEntry>();
        for (Integer in : simpleEntries.keySet()) {
            List<CreationEntry> entrys = simpleEntries.get(in);
            for (CreationEntry ee : entrys) {
                if (list.contains(ee)) continue;
                list.addAll(entrys);
            }
        }
        return list.toArray(new CreationEntry[list.size()]);
    }

    public CreationEntry getCreationEntry(int objectCreated) {
        AdvancedCreationEntry toReturn;
        block3: {
            toReturn = null;
            try {
                toReturn = this.getAdvancedCreationEntry(objectCreated);
            }
            catch (NoSuchEntryException nse) {
                Integer space = objectCreated;
                List<CreationEntry> entrys = simpleEntries.get(space);
                if (entrys == null) {
                    return toReturn;
                }
                Iterator<CreationEntry> iterator = entrys.iterator();
                if (!iterator.hasNext()) break block3;
                CreationEntry entry = iterator.next();
                return entry;
            }
        }
        return toReturn;
    }

    public CreationEntry getCreationEntry(int objectSource, int objectTarget, int objectCreated) throws NoSuchEntryException {
        CreationEntry toReturn = null;
        Integer space = objectTarget;
        List<CreationEntry> entrys = matrix.get(space);
        if (entrys != null) {
            for (CreationEntry entry : entrys) {
                if ((entry.getObjectSource() != objectSource || entry.getObjectTarget() != objectTarget) && (entry.getObjectSource() != objectTarget || entry.getObjectTarget() != objectSource) || entry.getObjectCreated() != objectCreated) continue;
                toReturn = entry;
            }
        }
        if (toReturn == null && (entrys = matrix.get(objectSource)) != null) {
            for (CreationEntry entry : entrys) {
                if ((entry.getObjectSource() != objectSource || entry.getObjectTarget() != objectTarget) && (entry.getObjectSource() != objectTarget || entry.getObjectTarget() != objectSource) || entry.getObjectCreated() != objectCreated) continue;
                toReturn = entry;
            }
        }
        if (toReturn == null) {
            throw new NoSuchEntryException("No creation entry found for objectSource=" + objectSource + ", objectTarget=" + objectTarget + ", objectCreated=" + objectCreated);
        }
        return toReturn;
    }

    static {
        advancedEntries = new HashMap<Integer, List<CreationEntry>>();
        simpleEntries = new HashMap<Integer, List<CreationEntry>>();
    }
}

