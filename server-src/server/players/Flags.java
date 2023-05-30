package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import java.util.BitSet;
import java.util.logging.Logger;

public final class Flags implements MiscConstants {
   private static final String[] flagDescs = new String[64];
   private static final Logger logger = Logger.getLogger(javax.mail.Flags.Flag.class.getName());

   private Flags() {
   }

   static void initialiseFlags() {
      for(int x = 0; x < 64; ++x) {
         flagDescs[x] = "";
         if (x == 0) {
            flagDescs[x] = "Seen structure door warning";
         }

         if (x == 1) {
            flagDescs[x] = "Allow Incoming PMs";
         }

         if (x == 2) {
            flagDescs[x] = "Allow Incoming Cross-Kingdoms PMs";
         }

         if (x == 3) {
            flagDescs[x] = "Allow Incoming Cross-Servers PMs";
         }
      }
   }

   static BitSet setFlagBits(long bits, BitSet toSet) {
      for(int x = 0; x < 64; ++x) {
         if (x == 0) {
            if ((bits & 1L) == 1L) {
               toSet.set(x, true);
            } else {
               toSet.set(x, false);
            }
         } else if ((bits >> x & 1L) == 1L) {
            toSet.set(x, true);
         } else {
            toSet.set(x, false);
         }
      }

      return toSet;
   }

   static long getFlagBits(BitSet bitsprovided) {
      long ret = 0L;

      for(int x = 0; x <= 64; ++x) {
         if (bitsprovided.get(x)) {
            ret += (long)(1 << x);
         }
      }

      return ret;
   }

   public static String getFlagString(int flag) {
      return flag >= 0 && flag < 64 ? flagDescs[flag] : "";
   }

   static {
      initialiseFlags();
   }
}
