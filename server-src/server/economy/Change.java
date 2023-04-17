/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.economy;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.economy.MonetaryConstants;

public final class Change
implements MonetaryConstants,
MiscConstants {
    public static final String NOTHING = "0 irons";
    public long ironCoins;
    public long goldCoins;
    public long silverCoins;
    public long copperCoins;
    private static final String AND_SEPARATOR = " and ";
    private static final String COMMA_SEPARATOR = ", ";
    private static final String IRON_STRING = " iron";
    private static final String IRON_SHORT_STRING = "i";
    private static final String COPPER_STRING = " copper";
    private static final String COPPER_SHORT_STRING = "c";
    private static final String SILVER_STRING = " silver";
    private static final String SILVER_SHORT_STRING = "s";
    private static final String GOLD_STRING = " gold";
    private static final String GOLD_SHORT_STRING = "g";

    public Change(long ironValue) {
        this.goldCoins = ironValue / 1000000L;
        long rest = ironValue % 1000000L;
        this.silverCoins = rest / 10000L;
        rest = ironValue % 10000L;
        this.copperCoins = rest / 100L;
        this.ironCoins = rest = ironValue % 100L;
    }

    public long getGoldCoins() {
        return this.goldCoins;
    }

    public long getSilverCoins() {
        return this.silverCoins;
    }

    public long getCopperCoins() {
        return this.copperCoins;
    }

    public long getIronCoins() {
        return this.ironCoins;
    }

    public String getChangeString() {
        String toSend = "";
        if (this.goldCoins > 0L) {
            toSend = toSend + this.goldCoins + GOLD_STRING;
        }
        if (this.silverCoins > 0L) {
            if (this.goldCoins > 0L) {
                toSend = this.copperCoins > 0L || this.ironCoins > 0L ? toSend + COMMA_SEPARATOR : toSend + AND_SEPARATOR;
            }
            toSend = toSend + this.silverCoins + SILVER_STRING;
        }
        if (this.copperCoins > 0L) {
            if (this.silverCoins > 0L || this.goldCoins > 0L) {
                toSend = this.ironCoins > 0L ? toSend + COMMA_SEPARATOR : toSend + AND_SEPARATOR;
            }
            toSend = toSend + this.copperCoins + COPPER_STRING;
        }
        if (this.ironCoins > 0L) {
            if (this.silverCoins > 0L || this.goldCoins > 0L || this.copperCoins > 0L) {
                toSend = toSend + AND_SEPARATOR;
            }
            toSend = toSend + this.ironCoins + IRON_STRING;
        }
        if (toSend.length() == 0) {
            return NOTHING;
        }
        return toSend;
    }

    public String getChangeShortString() {
        StringBuilder toSend = new StringBuilder();
        if (this.goldCoins > 0L) {
            toSend.append(this.goldCoins).append(GOLD_SHORT_STRING);
        }
        if (this.silverCoins > 0L) {
            if (this.goldCoins > 0L) {
                toSend.append(COMMA_SEPARATOR);
            }
            toSend.append(this.silverCoins).append(SILVER_SHORT_STRING);
        }
        if (this.copperCoins > 0L) {
            if (this.silverCoins > 0L || this.goldCoins > 0L) {
                toSend.append(COMMA_SEPARATOR);
            }
            toSend.append(this.copperCoins).append(COPPER_SHORT_STRING);
        }
        if (this.ironCoins > 0L) {
            if (this.silverCoins > 0L || this.goldCoins > 0L || this.copperCoins > 0L) {
                toSend.append(COMMA_SEPARATOR);
            }
            toSend.append(this.ironCoins).append(IRON_SHORT_STRING);
        }
        return toSend.toString();
    }
}

