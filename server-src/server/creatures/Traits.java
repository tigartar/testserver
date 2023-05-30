package com.wurmonline.server.creatures;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Traits implements MiscConstants {
   private static final String[] treatDescs = new String[64];
   private static final boolean[] negativeTraits = new boolean[64];
   private static final boolean[] neutralTraits = new boolean[64];
   private static final Logger logger = Logger.getLogger(Traits.class.getName());

   static void initialiseTraits() {
      for(int x = 0; x < 64; ++x) {
         treatDescs[x] = "";
         if (x == 0) {
            treatDescs[x] = "It will fight fiercely.";
         } else if (x == 1) {
            treatDescs[x] = "It has fleeter movement than normal.";
         } else if (x == 2) {
            treatDescs[x] = "It is a tough bugger.";
         } else if (x == 3) {
            treatDescs[x] = "It has a strong body.";
         } else if (x == 4) {
            treatDescs[x] = "It has lightning movement.";
         } else if (x == 5) {
            treatDescs[x] = "It can carry more than average.";
         } else if (x == 6) {
            treatDescs[x] = "It has very strong leg muscles.";
         } else if (x == 7) {
            treatDescs[x] = "It has keen senses.";
         } else if (x == 8) {
            treatDescs[x] = "It has malformed hindlegs.";
            negativeTraits[x] = true;
         } else if (x == 9) {
            treatDescs[x] = "The legs are of different length.";
            negativeTraits[x] = true;
         } else if (x == 10) {
            treatDescs[x] = "It seems overly aggressive.";
            negativeTraits[x] = true;
         } else if (x == 11) {
            treatDescs[x] = "It looks very unmotivated.";
            negativeTraits[x] = true;
         } else if (x == 12) {
            treatDescs[x] = "It is unusually strong willed.";
            negativeTraits[x] = true;
         } else if (x == 13) {
            treatDescs[x] = "It has some illness.";
            negativeTraits[x] = true;
         } else if (x == 14) {
            treatDescs[x] = "It looks constantly hungry.";
            negativeTraits[x] = true;
         } else if (x == 19) {
            treatDescs[x] = "It looks feeble and unhealthy.";
            negativeTraits[x] = true;
         } else if (x == 20) {
            treatDescs[x] = "It looks unusually strong and healthy.";
            negativeTraits[x] = false;
         } else if (x == 21) {
            treatDescs[x] = "It has a certain spark in its eyes.";
            negativeTraits[x] = false;
         } else if (x == 22) {
            treatDescs[x] = "It has been corrupted.";
            neutralTraits[x] = true;
         } else if (x == 27) {
            treatDescs[x] = "It bears the mark of the rift.";
            neutralTraits[x] = true;
         } else if (x == 28) {
            treatDescs[x] = "It bears the mark of a traitor.";
            neutralTraits[x] = true;
         } else if (x == 63) {
            treatDescs[x] = "It has been bred in captivity.";
            neutralTraits[x] = true;
         } else if (x == 29) {
            treatDescs[x] = "It has a mark of Valrei.";
            neutralTraits[x] = true;
         } else if (x == 15 || x == 16 || x == 17 || x == 18 || x == 24 || x == 25 || x == 23 || x == 30 || x == 31 || x == 32 || x == 33 || x == 34) {
            neutralTraits[x] = true;
         }
      }
   }

   private Traits() {
   }

   static long calcNewTraits(boolean inbred, long mothertraits, long fathertraits) {
      Random rand = new Random();
      BitSet motherSet = new BitSet(64);
      BitSet fatherSet = new BitSet(64);
      BitSet childSet = new BitSet(64);
      setTraitBits(fathertraits, fatherSet);
      setTraitBits(mothertraits, motherSet);

      for(int bitIndex = 0; bitIndex < 64; ++bitIndex) {
         calcOneNewTrait(inbred, rand, motherSet, fatherSet, childSet, bitIndex);
      }

      return getTraitBits(childSet);
   }

   static void calcOneNewTrait(boolean inbred, Random rand, BitSet motherSet, BitSet fatherSet, BitSet childSet, int bitIndex) {
      if (bitIndex != 27 && bitIndex != 28 && bitIndex != 29) {
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
   }

   public static long calcNewTraits(double breederSkill, boolean inbred, long mothertraits, long fathertraits) {
      Random rand = new Random();
      BitSet motherSet = new BitSet(64);
      BitSet fatherSet = new BitSet(64);
      BitSet childSet = new BitSet(64);
      int maxTraits = Math.min(8, Math.max(1, (int)(breederSkill / 10.0)));
      int maxPoints = maxTraits * 60;
      int allocated = 0;
      Map<Integer, Integer> newSet = new HashMap<>();
      List<Integer> availableTraits = new ArrayList<>();
      setTraitBits(fathertraits, fatherSet);
      setTraitBits(mothertraits, motherSet);

      for(int bitIndex = 0; bitIndex <= 34; ++bitIndex) {
         if (bitIndex != 22 && bitIndex != 27 && bitIndex != 28 && bitIndex != 29) {
            availableTraits.add(bitIndex);
            if (motherSet.get(bitIndex) && fatherSet.get(bitIndex)) {
               int num = 50;
               if (inbred && negativeTraits[bitIndex]) {
                  num += 10;
               }

               newSet.put(bitIndex, num);
               if (!isTraitNeutral(bitIndex)) {
                  allocated += 50;
               }

               availableTraits.remove(Integer.valueOf(bitIndex));
            } else if (motherSet.get(bitIndex)) {
               int num = 30;
               if (inbred && negativeTraits[bitIndex]) {
                  num += 10;
               }

               newSet.put(bitIndex, num);
               if (!isTraitNeutral(bitIndex)) {
                  allocated += 30;
               }

               availableTraits.remove(Integer.valueOf(bitIndex));
            } else if (fatherSet.get(bitIndex)) {
               int num = 20;
               if (inbred && negativeTraits[bitIndex]) {
                  num += 10;
               }

               newSet.put(bitIndex, num);
               if (!isTraitNeutral(bitIndex)) {
                  allocated += 20;
               }

               availableTraits.remove(Integer.valueOf(bitIndex));
            }
         }
      }

      int left = maxPoints - allocated;
      float traitsLeft = 0.0F;
      if (left > 0) {
         traitsLeft = (float)left / 50.0F;
         if (traitsLeft - (float)((int)traitsLeft) > 0.0F) {
            ++traitsLeft;
         }

         for(int x = 0; x < (int)traitsLeft; ++x) {
            if (rand.nextBoolean()) {
               int num = 20;
               Integer newTrait = availableTraits.remove(rand.nextInt(availableTraits.size()));
               if (negativeTraits[newTrait]) {
                  num -= maxTraits;
                  if (inbred) {
                     num += 10;
                  }
               }

               if (isTraitNeutral(newTrait)) {
                  --x;
               }

               newSet.put(newTrait, num);
            }
         }

         traitsLeft = (float)maxTraits;
      } else {
         traitsLeft = (float)Math.max(Math.min(newSet.size(), maxTraits), 3 + Server.rand.nextInt(3));
      }

      for(int t = 0; (float)t < traitsLeft && !newSet.isEmpty(); ++t) {
         Integer selected = pickOneTrait(rand, newSet);
         if (selected >= 0) {
            if (selected != 22 && selected != 27 && selected != 28 && selected != 29) {
               childSet.set(selected, true);
               newSet.remove(selected);
               if (isTraitNeutral(selected)) {
                  --t;
               }
            }
         } else {
            logger.log(Level.WARNING, "Failed to select a trait from a map of size " + newSet.size());
         }
      }

      if (!Servers.isThisAPvpServer()) {
         childSet.clear(22);
      } else if (fatherSet.get(22) || motherSet.get(22)) {
         childSet.set(22);
      }

      childSet.set(63, true);
      return getTraitBits(childSet);
   }

   static Integer pickOneTrait(Random rand, Map<Integer, Integer> traitMap) {
      int chance = 0;

      for(Entry<Integer, Integer> entry : traitMap.entrySet()) {
         chance += entry.getValue();
      }

      if (chance != 0 && chance >= 0) {
         int selectedTrait = rand.nextInt(chance);
         chance = 0;

         for(Entry<Integer, Integer> entry : traitMap.entrySet()) {
            chance += entry.getValue();
            if (chance > selectedTrait) {
               return entry.getKey();
            }
         }

         return -1;
      } else {
         logger.log(Level.INFO, "Trait rand=" + chance + " should not be <=0! Size of map is " + traitMap.size());
         return -1;
      }
   }

   static BitSet setTraitBits(long bits, BitSet toSet) {
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

   static long getTraitBits(BitSet bitsprovided) {
      long ret = 0L;

      for(int x = 0; x < 64; ++x) {
         if (bitsprovided.get(x)) {
            ret += 1L << x;
         }
      }

      return ret;
   }

   public static String getTraitString(int trait) {
      return trait >= 0 && trait < 64 ? treatDescs[trait] : "";
   }

   public static boolean isTraitNegative(int trait) {
      return trait >= 0 && trait <= 64 && negativeTraits[trait];
   }

   public static boolean isTraitNeutral(int trait) {
      return trait >= 0 && trait <= 64 && neutralTraits[trait];
   }

   static {
      initialiseTraits();
   }
}
