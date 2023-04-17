/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.EnchantUtil;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.villages.DeadVillage;
import com.wurmonline.shared.util.MaterialUtilities;
import java.util.ArrayList;
import java.util.HashMap;

public class FragmentUtilities {
    private static final int DIFFRANGE_TRASH = 0;
    private static final int DIFFRANGE_0_15 = 1;
    private static final int DIFFRANGE_15_30 = 2;
    private static final int DIFFRANGE_30_40 = 3;
    private static final int DIFFRANGE_40_50 = 4;
    private static final int DIFFRANGE_50_60 = 5;
    private static final int DIFFRANGE_60_70 = 6;
    private static final int DIFFRANGE_70_80 = 7;
    private static final int DIFFRANGE_80_90 = 8;
    private static final int DIFFRANGE_90_100 = 9;
    private static int[] diffTrash = new int[]{776, 786, 1122, 1121, 1123, 132, 38, 39, 43, 41, 40, 207, 42, 785, 785, 146, 688, 23, 454, 561, 551};
    private static int[] diff0_15 = new int[]{46, 47, 49, 220, 48, 453};
    private static int[] diff15_30 = new int[]{1011, 685, 687, 690, 45, 44, 223, 205, 221, 1411, 784, 778, 217, 218, 188, 451, 1408, 1407, 1416};
    private static int[] diff30_40 = new int[]{77, 813, 1161, 76, 78, 1020, 523, 127, 154, 389, 125, 126, 124, 123, 395, 270, 121, 269, 494, 452, 1406, 1421, 1418};
    private static int[] diff40_50 = new int[]{1022, 1172, 1169, 1165, 1252, 1323, 1324, 1405, 708, 88, 91, 89, 293, 295, 294, 148, 147, 149, 1417, 1420, 1419, 1430};
    private static int[] diff50_60 = new int[]{62, 20, 97, 388, 93, 8, 25, 7, 27, 24, 493, 394, 268, 267, 1325, 1330, 1415};
    private static int[] diff60_70 = new int[]{21, 80, 81, 87, 90, 3, 706, 290, 292, 291, 274, 279, 278, 275, 276, 277, 1328, 1327, 1329, 1326, 710};
    private static int[] diff70_80 = new int[]{976, 973, 978, 975, 974, 280, 284, 281, 282, 283, 83, 86, 287, 286};
    private static int[] anniversaryGifts = new int[]{791, 738, 967, 1306, 1321, 1100, 1297, 972, 1032, 844, 700, 1334, 997};
    private static int[] justStatues = new int[]{1408, 1407, 1416, 1406, 1421, 1418, 1323, 1324, 1405, 1417, 1420, 1419, 1325, 1330, 1415, 1328, 1327, 1329, 1326, 1430};
    private static HashMap<Integer, ArrayList<Integer>> fragmentLists = new HashMap();
    static final byte CLASS_WEAPON = 1;
    static final byte CLASS_ARMOUR = 2;
    static final byte CLASS_TOOL = 3;
    static final byte CLASS_CONTAINER = 4;
    static final byte CLASS_VEHICLE = 5;
    static final byte CLASS_ALL = 6;

    public static Fragment getRandomFragmentForSkill(double skill, boolean trashPossible) {
        if (skill < 0.0) {
            return null;
        }
        int maxRange = 1;
        if (skill >= 15.0 && skill < 30.0) {
            maxRange = 2;
        } else if (skill >= 30.0) {
            maxRange = (int)Math.min(9.0, Math.floor(skill / 10.0) - 1.0);
        }
        int thisRange = Server.rand.nextInt(maxRange + 1);
        if (trashPossible && Server.rand.nextInt(3) != 0) {
            thisRange = Math.max(0, thisRange - 3);
        }
        boolean bumpMaterial = false;
        if (thisRange == 8) {
            thisRange = 5;
            bumpMaterial = true;
        } else if (thisRange == 9) {
            thisRange = 6;
            bumpMaterial = true;
        }
        int itemId = -1;
        int materialId = -1;
        ArrayList<Integer> possibleItems = fragmentLists.get(thisRange);
        if (possibleItems != null) {
            itemId = possibleItems.get(Server.rand.nextInt(possibleItems.size()));
        }
        if (itemId == -1) {
            return null;
        }
        ItemTemplate item = ItemTemplateFactory.getInstance().getTemplateOrNull(itemId);
        if (item == null) {
            return null;
        }
        materialId = item.getMaterial();
        if (item.isMetal() && !item.isOre && !item.isMetalLump()) {
            materialId = 93;
        }
        if (item.isMetal() && !MaterialUtilities.isMetal((byte)materialId)) {
            materialId = 93;
        } else if (item.isWood() && !MaterialUtilities.isWood((byte)materialId)) {
            materialId = 14;
        }
        if (bumpMaterial && item.isMetal() && materialId == 93) {
            materialId = 94;
        }
        return new Fragment(itemId, materialId);
    }

    public static Item createVillageCache(Player performer, Item archReport, DeadVillage vill, Skill archSkill) {
        if (!(archReport.getAuxBit(0) && archReport.getAuxBit(1) && archReport.getAuxBit(2) && archReport.getAuxBit(3))) {
            return null;
        }
        try {
            int[] list;
            Item randomFrag;
            double power;
            Item cache = ItemFactory.createItem(1422, archReport.getCurrentQualityLevel(), vill.getFounderName());
            cache.setName(vill.getDeedName());
            int statueCount = (int)Math.min(6.0, (archSkill.getKnowledge(0.0) + (double)archReport.getCurrentQualityLevel()) / 28.0);
            int goodCount = (int)Math.min(6.0, (archSkill.getKnowledge(0.0) + (double)archReport.getCurrentQualityLevel()) / 28.0);
            float dvModifier = Math.min(2.0f, 0.25f + vill.getTimeSinceDisband() / 120.0f + vill.getTotalAge() / 60.0f);
            int totalGiven = 0;
            int i = 0;
            while ((float)i < (float)statueCount * dvModifier) {
                power = archSkill.skillCheck(i * 5, archReport, 0.0, false, 1.0f);
                Item statueFrag = ItemFactory.createItem(1307, (float)Math.min(100.0, Math.max(1.0, power)), vill.getFounderName());
                statueFrag.setRealTemplate(justStatues[Server.rand.nextInt(justStatues.length)]);
                statueFrag.setLastOwnerId(performer.getWurmId());
                if (statueFrag.isMetal()) {
                    if (Server.rand.nextInt(500) == 0) {
                        statueFrag.setMaterial((byte)95);
                    } else if (Server.rand.nextInt(50) == 0) {
                        statueFrag.setMaterial((byte)94);
                    } else {
                        statueFrag.setMaterial((byte)93);
                    }
                }
                cache.insertItem(statueFrag, true);
                ++totalGiven;
                ++i;
            }
            if (archSkill.getKnowledge(0.0) > 50.0) {
                i = 0;
                while ((float)i < (float)goodCount * dvModifier) {
                    power = archSkill.skillCheck(i * 10, archReport, 0.0, false, 1.0f);
                    randomFrag = ItemFactory.createItem(1307, (float)Math.min(100.0, Math.max(1.0, power)), vill.getFounderName());
                    list = diff50_60;
                    if (power > 50.0) {
                        list = diff70_80;
                    } else if (power > 30.0) {
                        list = diff60_70;
                    }
                    randomFrag.setRealTemplate(list[Server.rand.nextInt(list.length)]);
                    randomFrag.setLastOwnerId(performer.getWurmId());
                    randomFrag.setMaterial(randomFrag.getRealTemplate().getMaterial());
                    if (randomFrag.isMetal() && !randomFrag.getTemplate().isOre && !randomFrag.getTemplate().isMetalLump()) {
                        if (Server.rand.nextInt(500) == 0) {
                            randomFrag.setMaterial((byte)95);
                        } else if (Server.rand.nextInt(50) == 0) {
                            randomFrag.setMaterial((byte)94);
                        } else {
                            randomFrag.setMaterial((byte)93);
                        }
                    }
                    cache.insertItem(randomFrag, true);
                    ++totalGiven;
                    ++i;
                }
            }
            for (i = totalGiven; i < 10; ++i) {
                power = archSkill.skillCheck(i * 5, archReport, 0.0, false, 1.0f);
                randomFrag = ItemFactory.createItem(1307, (float)Math.min(100.0, Math.max(1.0, power)), vill.getFounderName());
                list = diff15_30;
                if (power > 50.0) {
                    list = diff40_50;
                } else if (power > 20.0) {
                    list = diff30_40;
                }
                randomFrag.setRealTemplate(list[Server.rand.nextInt(list.length)]);
                randomFrag.setLastOwnerId(performer.getWurmId());
                randomFrag.setMaterial(randomFrag.getRealTemplate().getMaterial());
                if (randomFrag.isMetal() && !randomFrag.getTemplate().isOre && !randomFrag.getTemplate().isMetalLump()) {
                    if (Server.rand.nextInt(500) == 0) {
                        randomFrag.setMaterial((byte)95);
                    } else if (Server.rand.nextInt(50) == 0) {
                        randomFrag.setMaterial((byte)94);
                    } else {
                        randomFrag.setMaterial((byte)93);
                    }
                }
                cache.insertItem(randomFrag, true);
            }
            Item tokenMini = ItemFactory.createItem(1423, (float)((archSkill.getKnowledge(0.0) + (double)archReport.getCurrentQualityLevel()) / 2.0), vill.getFounderName());
            double tokenPower = archSkill.skillCheck(50.0, archReport, 0.0, false, 1.0f);
            if (tokenPower > 80.0) {
                tokenMini.setMaterial(FragmentUtilities.getMetalMoonMaterial(100));
            } else if (tokenPower > 60.0) {
                tokenMini.setMaterial(FragmentUtilities.getMetalAlloyMaterial(100));
            } else if (tokenPower > 30.0) {
                tokenMini.setMaterial(FragmentUtilities.getMetalBaseMaterial((int)tokenPower));
            }
            tokenMini.setName(vill.getDeedName());
            tokenMini.setData(vill.getDeedId());
            tokenMini.setAuxData((byte)((archReport.getAuxData() & 0xFF) >>> 4));
            tokenMini.setAuxBit(7, true);
            tokenMini.setLastOwnerId(performer.getWurmId());
            cache.insertItem(tokenMini, true);
            return cache;
        }
        catch (FailedException | NoSuchTemplateException wurmServerException) {
            return null;
        }
    }

    public static int getDifficultyForItem(int itemId, int materialId) {
        for (int fragment : diff0_15) {
            if (fragment != itemId) continue;
            return 5;
        }
        for (int fragment : diff15_30) {
            if (fragment != itemId) continue;
            return 15;
        }
        for (int fragment : diff30_40) {
            if (fragment != itemId) continue;
            return 25;
        }
        for (int fragment : diff40_50) {
            if (fragment != itemId) continue;
            return 35;
        }
        for (int fragment : diff50_60) {
            if (fragment != itemId) continue;
            if (materialId == 94 || materialId == 9) {
                return 75;
            }
            return 45;
        }
        for (int fragment : diff60_70) {
            if (fragment != itemId) continue;
            if (materialId == 94 || materialId == 9) {
                return 85;
            }
            return 55;
        }
        for (int fragment : diff70_80) {
            if (fragment != itemId) continue;
            return 65;
        }
        return 10;
    }

    public static byte getMetalBaseMaterial(int identifyLevel) {
        switch (Server.rand.nextInt(Math.max(6, 75 - identifyLevel))) {
            case 0: {
                return 7;
            }
            case 1: {
                return 8;
            }
            case 2: {
                return 10;
            }
            case 3: {
                return 13;
            }
            case 4: {
                return 34;
            }
            case 5: {
                return 12;
            }
        }
        return 11;
    }

    public static byte getMetalAlloyMaterial(int identifyLevel) {
        switch (Server.rand.nextInt(Math.max(4, 75 - identifyLevel))) {
            case 0: {
                return 30;
            }
            case 1: {
                return 31;
            }
            case 2: {
                return 96;
            }
        }
        return 9;
    }

    public static byte getMetalMoonMaterial(int identifyLevel) {
        switch (Server.rand.nextInt(Math.max(10, 90 - identifyLevel))) {
            case 0: {
                return 67;
            }
            case 1: 
            case 2: {
                return 56;
            }
        }
        return 57;
    }

    public static byte getRandomWoodMaterial(int identifyLevel) {
        switch (Server.rand.nextInt(Math.max(25, 75 - identifyLevel))) {
            case 0: {
                return 42;
            }
            case 1: {
                return 14;
            }
            case 2: {
                return 91;
            }
            case 3: {
                return 50;
            }
            case 4: {
                return 39;
            }
            case 5: {
                return 45;
            }
            case 6: {
                return 63;
            }
            case 7: {
                return 65;
            }
            case 8: {
                return 49;
            }
            case 9: {
                return 71;
            }
            case 10: {
                return 46;
            }
            case 11: {
                return 43;
            }
            case 12: {
                return 66;
            }
            case 13: {
                return 92;
            }
            case 14: {
                return 41;
            }
            case 15: {
                return 38;
            }
            case 16: {
                return 51;
            }
            case 17: {
                return 44;
            }
            case 18: {
                return 88;
            }
            case 19: {
                return 37;
            }
            case 20: {
                return 90;
            }
            case 21: {
                return 47;
            }
            case 22: {
                return 48;
            }
            case 23: {
                return 64;
            }
            case 24: {
                return 40;
            }
        }
        return 14;
    }

    public static int getRandomAnniversaryGift() {
        return anniversaryGifts[Server.rand.nextInt(anniversaryGifts.length)];
    }

    public static int getRandomEnchantNumber(int weight) {
        if (weight < 50) {
            return 0;
        }
        int[] vals = new int[8];
        for (int i = 0; i < 8; ++i) {
            vals[i] = Server.rand.nextInt(1000);
        }
        int closest = vals[0];
        int weightedVal = (weight - 50) * 20;
        for (int i = 0; i < 8; ++i) {
            if (Math.abs(weightedVal - vals[i]) >= Math.abs(weightedVal - closest)) continue;
            closest = vals[i];
        }
        return Math.min(5, Math.max(1, Math.round((float)closest / 200.0f)));
    }

    public static void addRandomEnchantment(Item toEnchant, int enchLevel, float power) {
        SpellEffect e;
        ItemSpellEffects effs;
        int itemClass = 6;
        if (toEnchant.isWeapon()) {
            itemClass = 1;
        } else if (toEnchant.isArmour()) {
            itemClass = 2;
        } else if (toEnchant.isTool()) {
            itemClass = 3;
        } else if (toEnchant.isHollow()) {
            itemClass = 4;
        } else if (toEnchant.isVehicle()) {
            itemClass = 5;
        }
        FragmentEnchantment f = FragmentEnchantment.getRandomEnchantment((byte)itemClass, enchLevel);
        if (f == null) {
            return;
        }
        byte enchantment = f.getEnchantment();
        if (enchantment <= -51) {
            if (!RuneUtilities.canApplyRuneTo(enchantment, toEnchant)) {
                return;
            }
        } else {
            if (EnchantUtil.hasNegatingEffect(toEnchant, enchantment) != null) {
                return;
            }
            if (!Spell.mayBeEnchanted(toEnchant)) {
                return;
            }
        }
        if ((effs = toEnchant.getSpellEffects()) == null) {
            effs = new ItemSpellEffects(toEnchant.getWurmId());
        }
        if ((e = effs.getSpellEffect(enchantment)) == null) {
            e = new SpellEffect(toEnchant.getWurmId(), enchantment, power, 20000000);
            effs.addSpellEffect(e);
        } else {
            if (power > e.getPower() + power / 5.0f) {
                e.setPower(power);
            } else {
                e.setPower(e.getPower() + power / 5.0f);
            }
            if (enchantment != 45 && e.getPower() > 104.0f) {
                e.setPower(104.0f);
            }
        }
    }

    private static void addFragment(int itemId, int range) {
        ArrayList<Integer> fragments = fragmentLists.get(range);
        if (fragments == null) {
            fragments = new ArrayList();
            fragmentLists.put(range, fragments);
        }
        fragments.add(itemId);
    }

    static {
        for (int fragment : diffTrash) {
            FragmentUtilities.addFragment(fragment, 0);
        }
        for (int fragment : diff0_15) {
            FragmentUtilities.addFragment(fragment, 1);
        }
        for (int fragment : diff15_30) {
            FragmentUtilities.addFragment(fragment, 2);
        }
        for (int fragment : diff30_40) {
            FragmentUtilities.addFragment(fragment, 3);
        }
        for (int fragment : diff40_50) {
            FragmentUtilities.addFragment(fragment, 4);
        }
        for (int fragment : diff50_60) {
            FragmentUtilities.addFragment(fragment, 5);
        }
        for (int fragment : diff60_70) {
            FragmentUtilities.addFragment(fragment, 6);
        }
        for (int fragment : diff70_80) {
            FragmentUtilities.addFragment(fragment, 7);
        }
    }

    public static enum FragmentEnchantment {
        FLAMEAURA(14, new float[]{0.1f, 0.2f, 0.4f, 0.4f, 0.2f}, 1),
        FROSTBRAND(33, new float[]{0.1f, 0.2f, 0.5f, 0.3f, 0.2f}, 1),
        BLOODTHIRST(45, new float[]{0.2f, 0.4f, 0.2f, 0.1f, 0.0f}, 1),
        ROTTINGTOUCH(18, new float[]{0.0f, 0.0f, 0.2f, 0.4f, 0.4f}, 1),
        NIMBLENESS(32, new float[]{0.0f, 0.0f, 0.0f, 0.1f, 0.3f}, 1),
        LIFETRANSFER(26, new float[]{0.0f, 0.0f, 0.0f, 0.1f, 0.3f}, 1),
        MINDSTEALER(31, new float[]{0.0f, 0.0f, 0.05f, 0.2f, 0.4f}, 1),
        AURASHAREDPAIN(17, new float[]{0.0f, 0.1f, 0.3f, 0.2f, 0.1f}, 2),
        WEBARMOUR(46, new float[]{0.0f, 0.0f, 0.2f, 0.3f, 0.2f}, 2),
        WINDOFAGES(16, new float[]{0.1f, 0.2f, 0.4f, 0.2f, 0.1f}, 1, 3),
        CIRCLEOFCUNNING(13, new float[]{0.05f, 0.15f, 0.3f, 0.4f, 0.2f}, 1, 3),
        BOTD(47, new float[]{0.0f, 0.05f, 0.2f, 0.4f, 0.2f}, 1, 3),
        MAGBRASS(-128, new float[]{0.05f, 0.1f, 0.05f, 0.025f, 0.0f}, 6),
        MAGBRONZE(-127, new float[]{0.1f, 0.125f, 0.05f, 0.0f, 0.0f}, 3),
        MAGADAMANTINE(-125, new float[]{0.05f, 0.1f, 0.05f, 0.025f, 0.0f}, 5),
        MAGGLIMMERSTEEL(-124, new float[]{0.1f, 0.05f, 0.0f, 0.0f, 0.0f}, 3),
        MAGGOLD(-123, new float[]{0.05f, 0.05f, 0.025f, 0.0f, 0.0f}, 6),
        MAGSILVER(-122, new float[]{0.05f, 0.1f, 0.05f, 0.025f, 0.0f}, 4),
        MAGSTEEL(-121, new float[]{0.05f, 0.025f, 0.0f, 0.0f, 0.0f}, 5),
        MAGCOPPER(-120, new float[]{0.1f, 0.15f, 0.05f, 0.0f, 0.0f}, 6),
        MAGLEAD(-118, new float[]{0.05f, 0.1f, 0.05f, 0.025f, 0.0f}, 6),
        MAGZINC(-117, new float[]{0.05f, 0.025f, 0.0f, 0.0f, 0.0f}, 5),
        MAGSERYLL(-116, new float[]{0.025f, 0.05f, 0.1f, 0.125f, 0.075f}, 6),
        FOBRASS(-115, new float[]{0.05f, 0.05f, 0.025f, 0.0f, 0.0f}, 3),
        FOBRONZE(-114, new float[]{0.05f, 0.1f, 0.125f, 0.05f, 0.025f}, 5),
        FOTIN(-113, new float[]{0.1f, 0.125f, 0.05f, 0.0f, 0.0f}, 6),
        FOADAMANTINE(-112, new float[]{0.05f, 0.1f, 0.05f, 0.025f, 0.0f}, 6),
        FOGLIMMERSTEEL(-111, new float[]{0.025f, 0.0f, 0.0f, 0.0f, 0.0f}, 3),
        FOGOLD(-110, new float[]{0.05f, 0.05f, 0.025f, 0.0f, 0.0f}, 3),
        FOSILVER(-109, new float[]{0.025f, 0.0f, 0.0f, 0.0f, 0.0f}, 3),
        FOSTEEL(-108, new float[]{0.1f, 0.125f, 0.05f, 0.025f, 0.0f}, 6),
        FOLEAD(-105, new float[]{0.025f, 0.05f, 0.025f, 0.0f, 0.0f}, 3),
        FOSERYLL(-103, new float[]{0.025f, 0.05f, 0.1f, 0.125f, 0.075f}, 6),
        VYNBRASS(-102, new float[]{0.05f, 0.1f, 0.05f, 0.025f, 0.0f}, 6),
        VYNBRONZE(-101, new float[]{0.05f, 0.1f, 0.125f, 0.05f, 0.025f}, 5),
        VYNTIN(-100, new float[]{0.025f, 0.05f, 0.1f, 0.1f, 0.05f}, 6),
        VYNADAMANTINE(-99, new float[]{0.05f, 0.1f, 0.05f, 0.025f, 0.0f}, 5),
        VYNGLIMMERSTEEL(-98, new float[]{0.025f, 0.05f, 0.1f, 0.05f, 0.0f}, 3),
        VYNSILVER(-96, new float[]{0.025f, 0.05f, 0.1f, 0.05f, 0.025f}, 4),
        VYNSTEEL(-95, new float[]{0.025f, 0.05f, 0.1f, 0.05f, 0.0f}, 6),
        VYNCOPPER(-94, new float[]{0.05f, 0.1f, 0.125f, 0.05f, 0.0f}, 6),
        VYNLEAD(-92, new float[]{0.1f, 0.05f, 0.025f, 0.0f, 0.0f}, 4),
        VYNSERYLL(-90, new float[]{0.025f, 0.05f, 0.1f, 0.125f, 0.075f}, 6),
        LIBBRASS(-89, new float[]{0.05f, 0.025f, 0.0f, 0.0f, 0.0f}, 3),
        LIBBRONZE(-88, new float[]{0.025f, 0.05f, 0.025f, 0.0f, 0.0f}, 3),
        LIBTIN(-87, new float[]{0.025f, 0.05f, 0.1f, 0.05f, 0.025f}, 6),
        LIBADAMANTINE(-86, new float[]{0.025f, 0.05f, 0.1f, 0.05f, 0.0f}, 6),
        LIBGLIMMERSTEEL(-85, new float[]{0.025f, 0.05f, 0.1f, 0.05f, 0.0f}, 3),
        LIBGOLD(-84, new float[]{0.025f, 0.05f, 0.025f, 0.0f, 0.0f}, 3),
        LIBSILVER(-83, new float[]{0.025f, 0.05f, 0.1f, 0.05f, 0.025f}, 4),
        LIBSTEEL(-82, new float[]{0.025f, 0.05f, 0.025f, 0.0f, 0.0f}, 5),
        LIBLEAD(-79, new float[]{0.025f, 0.05f, 0.025f, 0.0f, 0.0f}, 3),
        LIBZINC(-78, new float[]{0.05f, 0.1f, 0.05f, 0.025f, 0.0f}, 6),
        LIBSERYLL(-77, new float[]{0.025f, 0.05f, 0.1f, 0.125f, 0.075f}, 6),
        JACKALBRASS(-76, new float[]{0.025f, 0.01f, 0.0f, 0.0f, 0.0f}, 6),
        JACKALGLIMMERSTEEL(-72, new float[]{0.025f, 0.01f, 0.0f, 0.0f, 0.0f}, 3),
        JACKALGOLD(-71, new float[]{0.025f, 0.05f, 0.025f, 0.0f, 0.0f}, 6),
        JACKALSILVER(-70, new float[]{0.025f, 0.05f, 0.025f, 0.0f, 0.0f}, 4),
        JACKALSTEEL(-69, new float[]{0.025f, 0.05f, 0.1f, 0.05f, 0.01f}, 6),
        JACKALCOPPER(-68, new float[]{0.05f, 0.1f, 0.125f, 0.05f, 0.0f}, 4),
        JACKALIRON(-67, new float[]{0.025f, 0.01f, 0.0f, 0.0f, 0.0f}, 3),
        JACKALLEAD(-66, new float[]{0.025f, 0.0f, 0.0f, 0.0f, 0.0f}, 6),
        JACKALZINC(-65, new float[]{0.025f, 0.05f, 0.025f, 0.0f, 0.0f}, 5),
        JACKALSERYLL(-64, new float[]{0.025f, 0.05f, 0.1f, 0.125f, 0.075f}, 6),
        UNKBRASS(-63, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 6),
        UNKBRONZE(-62, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 6),
        UNKTIN(-61, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 6),
        UNKADAMANTINE(-60, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 6),
        UNKGLIMMERSTEEL(-59, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 3),
        UNKGOLD(-58, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 6),
        UNKSILVER(-57, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 4),
        UNKSTEEL(-56, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 6),
        UNKCOPPER(-55, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 6),
        UNKIRON(-54, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 6),
        UNKLEAD(-53, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 3),
        UNKZINC(-52, new float[]{0.0f, 0.01f, 0.025f, 0.05f, 0.1f}, 6),
        UNKSERYLL(-51, new float[]{0.0f, 0.0f, 0.025f, 0.075f, 0.15f}, 6);

        private final byte enchantment;
        private final byte[] itemClass;
        private final float[] levelChances;

        private FragmentEnchantment(byte enchantment, float[] levelChances, byte ... itemClass) {
            this.enchantment = enchantment;
            this.itemClass = itemClass;
            this.levelChances = levelChances;
        }

        byte getEnchantment() {
            return this.enchantment;
        }

        static FragmentEnchantment getRandomEnchantment(byte itemClass, int level) {
            float totalChance = 0.0f;
            for (FragmentEnchantment f : FragmentEnchantment.values()) {
                for (byte b : f.itemClass) {
                    if (b != itemClass && b != 6) continue;
                    totalChance += f.levelChances[level];
                }
            }
            float winningVal = Server.rand.nextFloat() * totalChance;
            float thisVal = 0.0f;
            for (FragmentEnchantment f : FragmentEnchantment.values()) {
                for (byte b : f.itemClass) {
                    if (b != itemClass && b != 6 || !(winningVal < (thisVal += f.levelChances[level]))) continue;
                    return f;
                }
            }
            return null;
        }
    }

    public static class Fragment {
        private int itemId;
        private int itemMaterial;

        Fragment(int itemId, int itemMaterial) {
            this.itemId = itemId;
            this.itemMaterial = itemMaterial;
        }

        public int getItemId() {
            return this.itemId;
        }

        public int getMaterial() {
            return this.itemMaterial;
        }
    }
}

