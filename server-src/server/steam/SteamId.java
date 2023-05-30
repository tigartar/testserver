package com.wurmonline.server.steam;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamId {
   private long steamID64;
   private int accountNumber;
   private int accountInstance;
   private byte accountType;
   private byte accountUniverse;
   static Pattern idPattern = Pattern.compile("^STEAM_(?<x>\\d):(?<y>[01]):(?<z>\\d+)$");
   static Pattern id3Pattern = Pattern.compile("^\\[U:1:(?<w>\\d+)]$");
   static long uIdentifier = 76561197960265728L;
   static long gIdentifier = 103582791429521408L;

   SteamId() {
   }

   public static SteamId fromSteamID64(long steamID64) {
      SteamId id = new SteamId();
      id.accountNumber = (int)(4294967295L & steamID64);
      id.accountInstance = (int)((4503595332403200L & steamID64) >> 32);
      id.accountType = (byte)((int)((67553994410557440L & steamID64) >> 52));
      id.accountUniverse = (byte)((int)((-72057594037927936L & steamID64) >> 56));
      id.steamID64 = steamID64;
      return id;
   }

   public static SteamId fromSteamIDString(String steamIDString) {
      return fromSteamIDString(steamIDString, true);
   }

   public static SteamId fromSteamIDString(String steamIDString, boolean individual) {
      Matcher m = idPattern.matcher(steamIDString);
      if (m.matches() && m.groupCount() >= 3) {
         int y = Integer.valueOf(m.group("y"));
         int z = Integer.valueOf(m.group("z"));
         return fromSteamID64((long)(z * 2) + (individual ? uIdentifier : gIdentifier) + (long)y);
      } else {
         return null;
      }
   }

   public static SteamId fromSteamID3String(String steamID3String) {
      Matcher m = id3Pattern.matcher(steamID3String);
      if (!m.matches()) {
         return null;
      } else {
         int w = Integer.valueOf(m.group("w"));
         return fromSteamID64((long)w + uIdentifier);
      }
   }

   public String steamIDString() {
      return String.format("STEAM_%d:%d:%d", this.accountUniverse, this.accountNumber & 1, this.accountNumber >> 1);
   }

   public String steamID3String() {
      return String.format("[U:1:%d]", (this.accountNumber >> 1) * 2 + (this.accountNumber & 1));
   }

   public long getSteamID64() {
      return this.steamID64;
   }

   @Override
   public String toString() {
      return String.format("%d", this.steamID64);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (obj instanceof SteamId) {
         SteamId id = (SteamId)obj;
         return id.getSteamID64() == this.getSteamID64();
      } else if (!(obj instanceof String)) {
         if (obj instanceof Long) {
            return (Long)obj == this.getSteamID64();
         } else {
            return false;
         }
      } else {
         String s = (String)obj;
         return this.steamID3String().equals(s) || this.steamIDString().equals(s) || this.toString().equals(s);
      }
   }

   public static SteamId fromAnyString(String input) {
      long id64 = Long.valueOf(input);
      if (id64 != 0L) {
         return fromSteamID64(id64);
      } else {
         SteamId id = fromSteamIDString(input);
         return id != null ? id : fromSteamID3String(input);
      }
   }
}
