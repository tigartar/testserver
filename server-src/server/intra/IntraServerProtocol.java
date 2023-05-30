package com.wurmonline.server.intra;

interface IntraServerProtocol {
   int PROTOCOL_VERSION = 1;
   byte CMD_VALIDATE = 1;
   byte CMD_VALIDATION_ANSWER = 2;
   byte CMD_TRANSFER_USER = 3;
   byte CMD_DONE = 4;
   byte CMD_FAILED = 5;
   byte CMD_TRANSFER_USER_REQUEST = 6;
   byte CMD_SEND_DATAPART = 7;
   byte CMD_DATA_RECEIVED = 8;
   byte CMD_GET_PLAYER_VERSION = 9;
   byte CMD_GET_TIME = 10;
   byte CMD_GET_PLAYER_PAYMENTEXPIRE = 11;
   byte CMD_ADD_PLAYER_PAYMENTEXPIRE = 12;
   byte CMD_PING = 13;
   byte CMD_UNAVAILABLE = 14;
   byte CMD_DISCONNECT = 15;
   byte CMD_SET_PLAYER_MONEY = 16;
   byte CMD_SET_PLAYER_PAYMENTEXPIRE = 17;
   byte CMD_SET_PLAYER_PASSWORD = 18;
   String DISCONNECT_REASON_DONE = "Done";
}
