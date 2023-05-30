package com.wurmonline.server;

import com.wurmonline.server.creatures.Creature;
import javax.annotation.Nullable;

public final class Message implements MiscConstants {
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
            return "Warn";
         }

         if (this.message.startsWith("<System> Debug:")) {
            return "Debug";
         }

         if (this.message.startsWith("<Roads> ")) {
            return "Roads";
         }
      }

      return this.window;
   }

   public int getRed() {
      if (this.colorR >= 0) {
         return this.colorR;
      } else if (this.sender != null && this.sender.hasColoredChat()) {
         return this.getCustomRed();
      } else if (this.type == 5) {
         return 215;
      } else if (this.type == 6) {
         return 228;
      } else if (this.type == 3 || this.type == 14) {
         return 145;
      } else if (this.type == 1) {
         return 58;
      } else {
         return !this.isHelpChannel(this.window)
               || this.sender == null
               || !this.sender.isPlayerAssistant() && !this.sender.mayMute() && this.sender.getPower() <= 0
            ? 255
            : 105;
      }
   }

   public int getBlue() {
      if (this.colorB >= 0) {
         return this.colorB;
      } else if (this.sender != null && this.sender.hasColoredChat()) {
         return this.getCustomBlue();
      } else if (this.type == 5) {
         return 39;
      } else if (this.type == 6) {
         return 138;
      } else if (this.type == 3 || this.type == 14) {
         return 158;
      } else if (this.type == 1) {
         return 239;
      } else {
         return !this.isHelpChannel(this.window)
               || this.sender == null
               || !this.sender.isPlayerAssistant() && !this.sender.mayMute() && this.sender.getPower() <= 0
            ? 255
            : 210;
      }
   }

   public int getGreen() {
      if (this.colorG >= 0) {
         return this.colorG;
      } else if (this.sender != null && this.sender.hasColoredChat()) {
         return this.getCustomGreen();
      } else if (this.type == 5) {
         return 168;
      } else if (this.type == 6) {
         return 244;
      } else if (this.type == 3 || this.type == 14) {
         return 255;
      } else if (this.type == 1) {
         return 163;
      } else {
         return !this.isHelpChannel(this.window)
               || this.sender == null
               || !this.sender.isPlayerAssistant() && !this.sender.mayMute() && this.sender.getPower() <= 0
            ? 255
            : 231;
      }
   }

   public boolean isHelpChannel(String channelName) {
      return channelName.equals("CA HELP")
         || channelName.equals("GV HELP")
         || channelName.equals("JK HELP")
         || channelName.equals("MR HELP")
         || channelName.equals("HOTS HELP");
   }

   public int getCustomRed() {
      return this.sender != null ? this.sender.getCustomRedChat() : 255;
   }

   public int getCustomGreen() {
      return this.sender != null ? this.sender.getCustomGreenChat() : 140;
   }

   public int getCustomBlue() {
      return this.sender != null ? this.sender.getCustomBlueChat() : 0;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.message == null ? 0 : this.message.hashCode());
      result = 31 * result + (this.sender == null ? 0 : this.sender.hashCode());
      result = 31 * result + this.type;
      return 31 * result + (this.window == null ? 0 : this.window.hashCode());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
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

               if (this.window != null) {
                  return this.window.equals(other.window);
               }

               if (other.window != null) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
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
