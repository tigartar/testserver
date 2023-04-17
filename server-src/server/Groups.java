/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Group;
import com.wurmonline.server.NoSuchGroupException;
import com.wurmonline.server.Team;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Groups {
    private static final Map<String, Group> groups = new ConcurrentHashMap<String, Group>();

    private Groups() {
    }

    public static void addGroup(Group group2) {
        groups.put(group2.getName(), group2);
    }

    public static void removeGroup(String name) {
        groups.remove(name);
    }

    public static void renameGroup(String oldName, String newName) {
        Group g = groups.remove(oldName);
        if (g != null) {
            g.setName(newName);
            groups.put(newName, g);
        }
    }

    public static Group getGroup(String name) throws NoSuchGroupException {
        Group toReturn = groups.get(name);
        if (toReturn == null) {
            throw new NoSuchGroupException(name);
        }
        return toReturn;
    }

    public static final Team getTeamForOfflineMember(long wurmid) {
        for (Group g : groups.values()) {
            if (!g.isTeam() || !g.containsOfflineMember(wurmid)) continue;
            return (Team)g;
        }
        return null;
    }
}

