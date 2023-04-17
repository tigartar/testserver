/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.text.DateFormat;
import java.util.Date;

public final class VillageMessage
implements Comparable<VillageMessage> {
    private static final DateFormat df = DateFormat.getDateTimeInstance();
    private int villageId;
    private long posterId;
    private long toId;
    private String message;
    private int penColour;
    private long posted;
    private boolean everyone;

    public VillageMessage(int aVillageId, long aPosterId, long aToId, String aMessage, int thePenColour, long aPosted, boolean everyone) {
        this.villageId = aVillageId;
        this.posterId = aPosterId;
        this.toId = aToId;
        this.message = aMessage;
        this.penColour = thePenColour;
        this.posted = aPosted;
        this.everyone = everyone;
    }

    @Override
    public int compareTo(VillageMessage villageMsg) {
        if (this.getVillageId() == villageMsg.getVillageId()) {
            if (this.getToId() < villageMsg.getToId()) {
                return -1;
            }
            if (this.getToId() > villageMsg.getToId()) {
                return 1;
            }
            if (this.getPostedTime() < villageMsg.getPostedTime()) {
                return 1;
            }
            if (this.getPostedTime() > villageMsg.getPostedTime()) {
                return -1;
            }
            return 0;
        }
        if (this.getVillageId() < villageMsg.getVillageId()) {
            return -1;
        }
        return 1;
    }

    public int getPenColour() {
        return this.penColour;
    }

    public final long getPosterId() {
        return this.posterId;
    }

    public final String getPosterName() {
        return this.getPlayerName(this.posterId);
    }

    public final long getToId() {
        return this.toId;
    }

    public final String getToNmae() {
        return this.getPlayerName(this.toId);
    }

    private final String getPlayerName(long id) {
        PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(id);
        if (info == null) {
            return "";
        }
        return info.getName();
    }

    public final long getPostedTime() {
        return this.posted;
    }

    public String getDate() {
        return df.format(new Date(this.posted));
    }

    public final String getMessage() {
        return this.message;
    }

    public final int getVillageId() {
        return this.villageId;
    }

    public final boolean isForEveryone() {
        return this.everyone;
    }

    public final String getVillageName() {
        try {
            Village village = Villages.getVillage(this.villageId);
            return village.getName();
        }
        catch (NoSuchVillageException nsv) {
            return "";
        }
    }
}

