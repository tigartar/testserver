/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.intra;

interface IntraServerProtocol {
    public static final int PROTOCOL_VERSION = 1;
    public static final byte CMD_VALIDATE = 1;
    public static final byte CMD_VALIDATION_ANSWER = 2;
    public static final byte CMD_TRANSFER_USER = 3;
    public static final byte CMD_DONE = 4;
    public static final byte CMD_FAILED = 5;
    public static final byte CMD_TRANSFER_USER_REQUEST = 6;
    public static final byte CMD_SEND_DATAPART = 7;
    public static final byte CMD_DATA_RECEIVED = 8;
    public static final byte CMD_GET_PLAYER_VERSION = 9;
    public static final byte CMD_GET_TIME = 10;
    public static final byte CMD_GET_PLAYER_PAYMENTEXPIRE = 11;
    public static final byte CMD_ADD_PLAYER_PAYMENTEXPIRE = 12;
    public static final byte CMD_PING = 13;
    public static final byte CMD_UNAVAILABLE = 14;
    public static final byte CMD_DISCONNECT = 15;
    public static final byte CMD_SET_PLAYER_MONEY = 16;
    public static final byte CMD_SET_PLAYER_PAYMENTEXPIRE = 17;
    public static final byte CMD_SET_PLAYER_PASSWORD = 18;
    public static final String DISCONNECT_REASON_DONE = "Done";
}

