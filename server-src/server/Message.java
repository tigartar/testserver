/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import javax.annotation.Nullable;

public final class Message
implements MiscConstants {
    public static final String rcsversion = "$Id: Message.java,v 1.5 2006-09-28 23:19:14 root Exp $";
    private final byte type;
    private final String window;
    public static final String windowWarn = "Warn";
    public static final String windowDebug = "Debug";
    public static final String windowRoads = "Roads";
    private final String message;
    public static final byte SAY = 0;
    public static final byte TELL = 3;
    public static final byte WHISPER = 2;
    public static final byte VILLAGE = 3;
    public static final byte GROUP = 4;
    public static final byte SHOUT = 5;
    public static final byte EMOTE = 6;
    public static final byte SERVERSAFE = 6;
    public static final byte SERVERNORMAL = 7;
    public static final byte SERVERALERT = 8;
    public static final byte MGMT = 9;
    public static final byte KINGDOM = 10;
    public static final byte DEV = 11;
    public static final byte CA = 12;
    public static final byte TEAM = 13;
    public static final byte TEAM_LEADER = 14;
    public static final byte ALLIANCE = 15;
    public static final byte GLOBKINGDOM = 16;
    public static final byte EVENT = 17;
    public static final byte TRADE = 18;
    public static final byte LEFTWIN = 0;
    public static final byte RIGHTWIN = 1;
    private long receiver = -10L;
    private long senderId = -10L;
    private Creature sender;
    private byte senderKingdom = 0;
    private int colorR = -1;
    private int colorG = -1;
    private int colorB = -1;

    public int getColorR() {
        return this.colorR;
    }

    public void setColorR(int aColorR) {
        this.colorR = aColorR;
    }

    public int getColorG() {
        return this.colorG;
    }

    public void setColorG(int aColorG) {
        this.colorG = aColorG;
    }

    public int getColorB() {
        return this.colorB;
    }

    public void setColorB(int aColorB) {
        this.colorB = aColorB;
    }

    public Message(@Nullable Creature aSender, byte aType, String aWindow, String aMessage) {
        this.sender = aSender;
        this.type = aType;
        this.window = aWindow;
        this.message = aMessage;
    }

    public Message(@Nullable Creature aSender, byte aType, String aWindow, String aMessage, int colourR, int colourG, int colourB) {
        this.sender = aSender;
        this.type = aType;
        this.window = aWindow;
        this.message = aMessage;
        this.colorR = colourR;
        this.colorG = colourG;
        this.colorB = colourB;
    }

    public Creature getSender() {
        return this.sender;
    }

    public void setSender(Creature newSender) {
        this.sender = newSender;
    }

    public byte getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    public String getWindow() {
        if (this.type == 11 && this.window.equals("GM")) {
            if (this.message.contains(" movement too ")) {
                return windowWarn;
            }
            if (this.message.startsWith("<System> Debug:")) {
                return windowDebug;
            }
            if (this.message.startsWith("<Roads> ")) {
                return windowRoads;
            }
        }
        return this.window;
    }

    public int getRed() {
        if (this.colorR >= 0) {
            return this.colorR;
        }
        if (this.sender != null && this.sender.hasColoredChat()) {
            return this.getCustomRed();
        }
        if (this.type == 5) {
            return 215;
        }
        if (this.type == 6) {
            return 228;
        }
        if (this.type == 3 || this.type == 14) {
            return 145;
        }
        if (this.type == 1) {
            return 58;
        }
        if (this.isHelpChannel(this.window) && this.sender != null && (this.sender.isPlayerAssistant() || this.sender.mayMute() || this.sender.getPower() > 0)) {
            return 105;
        }
        return 255;
    }

    public int getBlue() {
        if (this.colorB >= 0) {
            return this.colorB;
        }
        if (this.sender != null && this.sender.hasColoredChat()) {
            return this.getCustomBlue();
        }
        if (this.type == 5) {
            return 39;
        }
        if (this.type == 6) {
            return 138;
        }
        if (this.type == 3 || this.type == 14) {
            return 158;
        }
        if (this.type == 1) {
            return 239;
        }
        if (this.isHelpChannel(this.window) && this.sender != null && (this.sender.isPlayerAssistant() || this.sender.mayMute() || this.sender.getPower() > 0)) {
            return 210;
        }
        return 255;
    }

    public int getGreen() {
        if (this.colorG >= 0) {
            return this.colorG;
        }
        if (this.sender != null && this.sender.hasColoredChat()) {
            return this.getCustomGreen();
        }
        if (this.type == 5) {
            return 168;
        }
        if (this.type == 6) {
            return 244;
        }
        if (this.type == 3 || this.type == 14) {
            return 255;
        }
        if (this.type == 1) {
            return 163;
        }
        if (this.isHelpChannel(this.window) && this.sender != null && (this.sender.isPlayerAssistant() || this.sender.mayMute() || this.sender.getPower() > 0)) {
            return 231;
        }
        return 255;
    }

    public boolean isHelpChannel(String channelName) {
        return channelName.equals("CA HELP") || channelName.equals("GV HELP") || channelName.equals("JK HELP") || channelName.equals("MR HELP") || channelName.equals("HOTS HELP");
    }

    public int getCustomRed() {
        if (this.sender != null) {
            return this.sender.getCustomRedChat();
        }
        return 255;
    }

    public int getCustomGreen() {
        if (this.sender != null) {
            return this.sender.getCustomGreenChat();
        }
        return 140;
    }

    public int getCustomBlue() {
        if (this.sender != null) {
            return this.sender.getCustomBlueChat();
        }
        return 0;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.message == null ? 0 : this.message.hashCode());
        result = 31 * result + (this.sender == null ? 0 : this.sender.hashCode());
        result = 31 * result + this.type;
        result = 31 * result + (this.window == null ? 0 : this.window.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Message other = (Message)obj;
        if (this.message == null) {
            if (other.message != null) {
                return false;
            }
        } else {
            if (!this.message.equals(other.message)) {
                return false;
            }
            if (this.sender == null) {
                if (other.sender != null) {
                    return false;
                }
            } else {
                if (!this.sender.equals(other.sender) || this.type != other.type) {
                    return false;
                }
                if (this.window == null) {
                    if (other.window != null) {
                        return false;
                    }
                } else {
                    return this.window.equals(other.window);
                }
            }
        }
        return true;
    }

    public byte getSenderKingdom() {
        return this.senderKingdom;
    }

    public void setSenderKingdom(byte aSenderKingdom) {
        this.senderKingdom = aSenderKingdom;
    }

    public long getReceiver() {
        return this.receiver;
    }

    public void setReceiver(long aReceiver) {
        this.receiver = aReceiver;
    }

    public long getSenderId() {
        return this.senderId;
    }

    public void setSenderId(long aSenderId) {
        this.senderId = aSenderId;
    }
}

