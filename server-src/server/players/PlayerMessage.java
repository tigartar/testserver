/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.utils.ProtocolUtilities;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

final class PlayerMessage {
    private static final Logger logger = Logger.getLogger(PlayerMessage.class.getName());
    private final Long iPlayerId;
    private final byte[] iMessageBytes;
    private final String iDescription;
    private final long iMessageId = messageIdSequence.getAndIncrement();
    private static final AtomicLong messageIdSequence = new AtomicLong();

    public PlayerMessage(Long aPlayerId, byte[] aMessageBytes) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Constructor - PlayerId: " + aPlayerId + ", MessageBytes: " + Arrays.toString(aMessageBytes));
        }
        if (aPlayerId == null || aMessageBytes == null || aMessageBytes.length == 0) {
            throw new IllegalArgumentException("PlayerId and MessageBytes must be non-null andthe MessageBytes must conatin at least one byte");
        }
        this.iPlayerId = aPlayerId;
        this.iMessageBytes = aMessageBytes;
        this.iDescription = ProtocolUtilities.getDescriptionForCommand(this.iMessageBytes[0]);
    }

    long getMessageId() {
        return this.iMessageId;
    }

    Long getPlayerId() {
        return this.iPlayerId;
    }

    byte[] getMessageBytes() {
        return this.iMessageBytes;
    }

    String getDescription() {
        return this.iDescription;
    }

    public boolean equals(Object aObj) {
        boolean lEquals = false;
        if (aObj instanceof PlayerMessage) {
            PlayerMessage lMessage = (PlayerMessage)aObj;
            if (lMessage.iMessageId != this.iMessageId && lMessage.iPlayerId != null && lMessage.iPlayerId.equals(this.iPlayerId) && lMessage.iMessageBytes != null && Arrays.equals(lMessage.iMessageBytes, this.iMessageBytes)) {
                lEquals = true;
            }
        }
        return lEquals;
    }

    public int hashCode() {
        int lHashCode = (int)(this.iMessageId ^ this.iMessageId >>> 32);
        if (this.iPlayerId != null) {
            lHashCode ^= this.iPlayerId.hashCode();
        }
        if (this.iMessageBytes != null) {
            lHashCode ^= Arrays.hashCode(this.iMessageBytes);
        }
        return lHashCode;
    }

    public String toString() {
        return "PlayerMessage [MessageId: " + this.iMessageId + ", PlayerId: " + this.iPlayerId + ", MessageBytes length: " + this.iMessageBytes.length + ", Description: " + this.iDescription + ']';
    }
}

