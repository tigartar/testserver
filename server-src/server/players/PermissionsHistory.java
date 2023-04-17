/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.players.PermissionsHistoryEntry;
import java.util.LinkedList;

public class PermissionsHistory {
    private LinkedList<PermissionsHistoryEntry> historyEntries = new LinkedList();

    void add(long eventTime, long playerId, String playerName, String event) {
        this.historyEntries.addFirst(new PermissionsHistoryEntry(eventTime, playerId, playerName, event.replace("\"", "'")));
    }

    public PermissionsHistoryEntry[] getHistoryEvents() {
        return this.historyEntries.toArray(new PermissionsHistoryEntry[this.historyEntries.size()]);
    }

    public String[] getHistory(int numevents) {
        String[] hist = new String[]{};
        int lHistorySize = this.historyEntries.size();
        if (lHistorySize > 0) {
            int numbersToFetch = Math.min(numevents, lHistorySize);
            hist = new String[numbersToFetch];
            PermissionsHistoryEntry[] events = this.getHistoryEvents();
            for (int x = 0; x < numbersToFetch; ++x) {
                hist[x] = events[x].getLongDesc();
            }
        }
        return hist;
    }
}

