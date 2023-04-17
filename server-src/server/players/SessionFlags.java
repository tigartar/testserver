/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import java.util.BitSet;
import java.util.logging.Logger;

public final class SessionFlags
implements MiscConstants {
    private static final String[] flagDescs = new String[64];
    private static final Logger logger = Logger.getLogger(SessionFlags.class.getName());

    private SessionFlags() {
    }

    static void initialiseFlags() {
        for (int x = 0; x < 64; ++x) {
            SessionFlags.flagDescs[x] = "";
            if (x != 0) continue;
            SessionFlags.flagDescs[x] = "Player has signed in";
        }
    }

    static BitSet setFlagBits(long bits, BitSet toSet) {
        for (int x = 0; x < 64; ++x) {
            if (x == 0) {
                if ((bits & 1L) == 1L) {
                    toSet.set(x, true);
                    continue;
                }
                toSet.set(x, false);
                continue;
            }
            if ((bits >> x & 1L) == 1L) {
                toSet.set(x, true);
                continue;
            }
            toSet.set(x, false);
        }
        return toSet;
    }

    static long getFlagBits(BitSet bitsprovided) {
        long ret = 0L;
        for (int x = 0; x <= 64; ++x) {
            if (!bitsprovided.get(x)) continue;
            ret += (long)(1 << x);
        }
        return ret;
    }

    public static String getFlagString(int flag) {
        if (flag >= 0 && flag < 64) {
            return flagDescs[flag];
        }
        return "";
    }

    static {
        SessionFlags.initialiseFlags();
    }
}

