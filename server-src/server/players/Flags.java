/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import java.util.BitSet;
import java.util.logging.Logger;
import javax.mail.Flags;

public final class Flags
implements MiscConstants {
    private static final String[] flagDescs = new String[64];
    private static final Logger logger = Logger.getLogger(Flags.Flag.class.getName());

    private Flags() {
    }

    static void initialiseFlags() {
        for (int x = 0; x < 64; ++x) {
            Flags.flagDescs[x] = "";
            if (x == 0) {
                Flags.flagDescs[x] = "Seen structure door warning";
            }
            if (x == 1) {
                Flags.flagDescs[x] = "Allow Incoming PMs";
            }
            if (x == 2) {
                Flags.flagDescs[x] = "Allow Incoming Cross-Kingdoms PMs";
            }
            if (x != 3) continue;
            Flags.flagDescs[x] = "Allow Incoming Cross-Servers PMs";
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
        Flags.initialiseFlags();
    }
}

