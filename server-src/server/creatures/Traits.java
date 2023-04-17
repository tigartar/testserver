/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Traits
implements MiscConstants {
    private static final String[] treatDescs = new String[64];
    private static final boolean[] negativeTraits = new boolean[64];
    private static final boolean[] neutralTraits = new boolean[64];
    private static final Logger logger = Logger.getLogger(Traits.class.getName());

    static void initialiseTraits() {
        for (int x = 0; x < 64; ++x) {
            Traits.treatDescs[x] = "";
            if (x == 0) {
                Traits.treatDescs[x] = "It will fight fiercely.";
                continue;
            }
            if (x == 1) {
                Traits.treatDescs[x] = "It has fleeter movement than normal.";
                continue;
            }
            if (x == 2) {
                Traits.treatDescs[x] = "It is a tough bugger.";
                continue;
            }
            if (x == 3) {
                Traits.treatDescs[x] = "It has a strong body.";
                continue;
            }
            if (x == 4) {
                Traits.treatDescs[x] = "It has lightning movement.";
                continue;
            }
            if (x == 5) {
                Traits.treatDescs[x] = "It can carry more than average.";
                continue;
            }
            if (x == 6) {
                Traits.treatDescs[x] = "It has very strong leg muscles.";
                continue;
            }
            if (x == 7) {
                Traits.treatDescs[x] = "It has keen senses.";
                continue;
            }
            if (x == 8) {
                Traits.treatDescs[x] = "It has malformed hindlegs.";
                Traits.negativeTraits[x] = true;
                continue;
            }
            if (x == 9) {
                Traits.treatDescs[x] = "The legs are of different length.";
                Traits.negativeTraits[x] = true;
                continue;
            }
            if (x == 10) {
                Traits.treatDescs[x] = "It seems overly aggressive.";
                Traits.negativeTraits[x] = true;
                continue;
            }
            if (x == 11) {
                Traits.treatDescs[x] = "It looks very unmotivated.";
                Traits.negativeTraits[x] = true;
                continue;
            }
            if (x == 12) {
                Traits.treatDescs[x] = "It is unusually strong willed.";
                Traits.negativeTraits[x] = true;
                continue;
            }
            if (x == 13) {
                Traits.treatDescs[x] = "It has some illness.";
                Traits.negativeTraits[x] = true;
                continue;
            }
            if (x == 14) {
                Traits.treatDescs[x] = "It looks constantly hungry.";
                Traits.negativeTraits[x] = true;
                continue;
            }
            if (x == 19) {
                Traits.treatDescs[x] = "It looks feeble and unhealthy.";
                Traits.negativeTraits[x] = true;
                continue;
            }
            if (x == 20) {
                Traits.treatDescs[x] = "It looks unusually strong and healthy.";
                Traits.negativeTraits[x] = false;
                continue;
            }
            if (x == 21) {
                Traits.treatDescs[x] = "It has a certain spark in its eyes.";
                Traits.negativeTraits[x] = false;
                continue;
            }
            if (x == 22) {
                Traits.treatDescs[x] = "It has been corrupted.";
                Traits.neutralTraits[x] = true;
                continue;
            }
            if (x == 27) {
                Traits.treatDescs[x] = "It bears the mark of the rift.";
                Traits.neutralTraits[x] = true;
                continue;
            }
            if (x == 28) {
                Traits.treatDescs[x] = "It bears the mark of a traitor.";
                Traits.neutralTraits[x] = true;
                continue;
            }
            if (x == 63) {
                Traits.treatDescs[x] = "It has been bred in captivity.";
                Traits.neutralTraits[x] = true;
                continue;
            }
            if (x == 29) {
                Traits.treatDescs[x] = "It has a mark of Valrei.";
                Traits.neutralTraits[x] = true;
                continue;
            }
            if (x != 15 && x != 16 && x != 17 && x != 18 && x != 24 && x != 25 && x != 23 && x != 30 && x != 31 && x != 32 && x != 33 && x != 34) continue;
            Traits.neutralTraits[x] = true;
        }
    }

    private Traits() {
    }

    static long calcNewTraits(boolean inbred, long mothertraits, long fathertraits) {
        Random rand = new Random();
        BitSet motherSet = new BitSet(64);
        BitSet fatherSet = new BitSet(64);
        BitSet childSet = new BitSet(64);
        Traits.setTraitBits(fathertraits, fatherSet);
        Traits.setTraitBits(mothertraits, motherSet);
        for (int bitIndex = 0; bitIndex < 64; ++bitIndex) {
            Traits.calcOneNewTrait(inbred, rand, motherSet, fatherSet, childSet, bitIndex);
        }
        return Traits.getTraitBits(childSet);
    }

    static void calcOneNewTrait(boolean inbred, Random rand, BitSet motherSet, BitSet fatherSet, BitSet childSet, int bitIndex) {
        if (bitIndex == 27 || bitIndex == 28 || bitIndex == 29) {
            return;
        }
        if (motherSet.get(bitIndex) && fatherSet.get(bitIndex)) {
            int chance = 66;
            if (negativeTraits[bitIndex]) {
                chance = 10;
                if (inbred) {
                    chance = 20;
                }
            }
            childSet.set(bitIndex, rand.nextInt(100) < chance);
        } else if (motherSet.get(bitIndex)) {
            int chance = 25;
            if (negativeTraits[bitIndex]) {
                chance = 8;
                if (inbred) {
                    chance = 12;
                }
            }
            childSet.set(bitIndex, rand.nextInt(100) < chance);
        } else if (fatherSet.get(bitIndex)) {
            int chance = 25;
            if (negativeTraits[bitIndex]) {
                chance = 8;
                if (inbred) {
                    chance = 12;
                }
            }
            childSet.set(bitIndex, rand.nextInt(100) < chance);
        } else {
            if (bitIndex == 22) {
                return;
            }
            int chance = 7;
            if (negativeTraits[bitIndex]) {
                chance = 5;
                if (inbred) {
                    chance = 10;
                }
            }
            childSet.set(bitIndex, rand.nextInt(100) < chance);
        }
    }

    public static long calcNewTraits(double breederSkill, boolean inbred, long mothertraits, long fathertraits) {
        Random rand = new Random();
        BitSet motherSet = new BitSet(64);
        BitSet fatherSet = new BitSet(64);
        BitSet childSet = new BitSet(64);
        int maxTraits = Math.min(8, Math.max(1, (int)(breederSkill / 10.0)));
        int maxPoints = maxTraits * 60;
        int allocated = 0;
        HashMap<Integer, Integer> newSet = new HashMap<Integer, Integer>();
        ArrayList<Integer> availableTraits = new ArrayList<Integer>();
        Traits.setTraitBits(fathertraits, fatherSet);
        Traits.setTraitBits(mothertraits, motherSet);
        for (int bitIndex = 0; bitIndex <= 34; ++bitIndex) {
            int num;
            if (bitIndex == 22 || bitIndex == 27 || bitIndex == 28 || bitIndex == 29) continue;
            availableTraits.add(bitIndex);
            if (motherSet.get(bitIndex) && fatherSet.get(bitIndex)) {
                num = 50;
                if (inbred && negativeTraits[bitIndex]) {
                    num += 10;
                }
                newSet.put(bitIndex, num);
                if (!Traits.isTraitNeutral(bitIndex)) {
                    allocated += 50;
                }
                availableTraits.remove((Object)bitIndex);
                continue;
            }
            if (motherSet.get(bitIndex)) {
                num = 30;
                if (inbred && negativeTraits[bitIndex]) {
                    num += 10;
                }
                newSet.put(bitIndex, num);
                if (!Traits.isTraitNeutral(bitIndex)) {
                    allocated += 30;
                }
                availableTraits.remove((Object)bitIndex);
                continue;
            }
            if (!fatherSet.get(bitIndex)) continue;
            num = 20;
            if (inbred && negativeTraits[bitIndex]) {
                num += 10;
            }
            newSet.put(bitIndex, num);
            if (!Traits.isTraitNeutral(bitIndex)) {
                allocated += 20;
            }
            availableTraits.remove((Object)bitIndex);
        }
        int left = maxPoints - allocated;
        float traitsLeft = 0.0f;
        if (left > 0) {
            traitsLeft = (float)left / 50.0f;
            if (traitsLeft - (float)((int)traitsLeft) > 0.0f) {
                traitsLeft += 1.0f;
            }
            for (int x = 0; x < (int)traitsLeft; ++x) {
                if (!rand.nextBoolean()) continue;
                int num = 20;
                Integer newTrait = (Integer)availableTraits.remove(rand.nextInt(availableTraits.size()));
                if (negativeTraits[newTrait]) {
                    num -= maxTraits;
                    if (inbred) {
                        num += 10;
                    }
                }
                if (Traits.isTraitNeutral(newTrait)) {
                    --x;
                }
                newSet.put(newTrait, num);
            }
            traitsLeft = maxTraits;
        } else {
            traitsLeft = Math.max(Math.min(newSet.size(), maxTraits), 3 + Server.rand.nextInt(3));
        }
        int t = 0;
        while ((float)t < traitsLeft && !newSet.isEmpty()) {
            Integer selected = Traits.pickOneTrait(rand, newSet);
            if (selected >= 0) {
                if (selected != 22 && selected != 27 && selected != 28 && selected != 29) {
                    childSet.set((int)selected, true);
                    newSet.remove(selected);
                    if (Traits.isTraitNeutral(selected)) {
                        --t;
                    }
                }
            } else {
                logger.log(Level.WARNING, "Failed to select a trait from a map of size " + newSet.size());
            }
            ++t;
        }
        if (!Servers.isThisAPvpServer()) {
            childSet.clear(22);
        } else if (fatherSet.get(22) || motherSet.get(22)) {
            childSet.set(22);
        }
        childSet.set(63, true);
        return Traits.getTraitBits(childSet);
    }

    static Integer pickOneTrait(Random rand, Map<Integer, Integer> traitMap) {
        int chance = 0;
        for (Map.Entry<Integer, Integer> entry : traitMap.entrySet()) {
            chance += entry.getValue().intValue();
        }
        if (chance == 0 || chance < 0) {
            logger.log(Level.INFO, "Trait rand=" + chance + " should not be <=0! Size of map is " + traitMap.size());
            return -1;
        }
        int selectedTrait = rand.nextInt(chance);
        chance = 0;
        for (Map.Entry<Integer, Integer> entry : traitMap.entrySet()) {
            if ((chance += entry.getValue().intValue()) <= selectedTrait) continue;
            return entry.getKey();
        }
        return -1;
    }

    static BitSet setTraitBits(long bits, BitSet toSet) {
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

    static long getTraitBits(BitSet bitsprovided) {
        long ret = 0L;
        for (int x = 0; x < 64; ++x) {
            if (!bitsprovided.get(x)) continue;
            ret += 1L << x;
        }
        return ret;
    }

    public static String getTraitString(int trait) {
        if (trait >= 0 && trait < 64) {
            return treatDescs[trait];
        }
        return "";
    }

    public static boolean isTraitNegative(int trait) {
        return trait >= 0 && trait <= 64 && negativeTraits[trait];
    }

    public static boolean isTraitNeutral(int trait) {
        return trait >= 0 && trait <= 64 && neutralTraits[trait];
    }

    static {
        Traits.initialiseTraits();
    }
}

