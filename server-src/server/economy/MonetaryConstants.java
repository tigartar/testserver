/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.economy;

public interface MonetaryConstants {
    public static final int COIN_IRON = 1;
    public static final int COIN_COPPER = 100;
    public static final int COIN_SILVER = 10000;
    public static final int COIN_GOLD = 1000000;
    public static final int MAX_DISCARDMONEY_HOUR = 500;

    public static enum TransactionReason {
        Banked,
        Charged,
        Destroyed,
        Notrade,
        PersonalShop,
        Sacrificed,
        TraderShop;

    }
}

