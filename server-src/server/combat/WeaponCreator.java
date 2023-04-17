/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.combat;

import com.wurmonline.server.combat.Weapon;
import java.util.logging.Logger;

public final class WeaponCreator {
    private static final Logger logger = Logger.getLogger(WeaponCreator.class.getName());

    private WeaponCreator() {
    }

    public static final void createWeapons() {
        logger.info("Creating weapons");
        long start = System.nanoTime();
        Weapon awl = new Weapon(390, 1.0f, 3.0f, 0.0f, 1, 1, 0.0f, 2.0);
        Weapon knifele = new Weapon(392, 0.5f, 2.0f, 0.0f, 1, 1, 0.0f, 2.0);
        Weapon knifeca = new Weapon(8, 1.0f, 2.0f, 0.0f, 1, 1, 1.0f, 2.0);
        Weapon knifebu = new Weapon(93, 1.5f, 2.0f, 0.0f, 1, 1, 1.0f, 1.0);
        Weapon knifesa = new Weapon(792, 1.5f, 2.0f, 0.03f, 1, 1, 1.0f, 1.0);
        Weapon knifecru = new Weapon(685, 1.0f, 4.0f, 0.0f, 1, 1, 0.0f, 3.0);
        Weapon pickaxecru = new Weapon(687, 1.0f, 6.0f, 0.0f, 1, 1, 0.0f, 5.0);
        Weapon shaftcru = new Weapon(691, 1.0f, 3.0f, 0.0f, 1, 1, 0.0f, 5.0);
        Weapon shovelcru = new Weapon(690, 1.0f, 6.0f, 0.0f, 1, 1, 0.0f, 5.0);
        Weapon branch = new Weapon(688, 1.0f, 6.0f, 0.0f, 1, 1, 0.0f, 3.0);
        Weapon axecru = new Weapon(1011, 1.0f, 5.0f, 0.0f, 1, 1, 0.0f, 5.0);
        Weapon scissor = new Weapon(394, 0.5f, 2.0f, 0.0f, 1, 1, 0.0f, 2.0);
        Weapon swoshort = new Weapon(80, 4.0f, 3.0f, 0.1f, 2, 1, 1.0f, 0.0);
        Weapon bowL = new Weapon(449, 0.0f, 5.0f, 0.0f, 1, 5, 1.0f, 9.0);
        bowL.setDamagedByMetal(true);
        Weapon bowM = new Weapon(448, 0.0f, 5.0f, 0.0f, 1, 5, 1.0f, 9.0);
        bowM.setDamagedByMetal(true);
        Weapon bowS = new Weapon(447, 0.0f, 5.0f, 0.0f, 1, 5, 1.0f, 9.0);
        bowS.setDamagedByMetal(true);
        Weapon bowLN = new Weapon(461, 0.0f, 5.0f, 0.0f, 1, 5, 1.0f, 9.0);
        bowLN.setDamagedByMetal(true);
        Weapon bowSN = new Weapon(459, 0.0f, 5.0f, 0.0f, 1, 5, 1.0f, 9.0);
        bowSN.setDamagedByMetal(true);
        Weapon bowMN = new Weapon(460, 0.0f, 5.0f, 0.0f, 1, 5, 1.0f, 9.0);
        bowMN.setDamagedByMetal(true);
        Weapon hatchet = new Weapon(7, 1.0f, 5.0f, 0.0f, 2, 2, 0.0f, 3.0);
        hatchet.setDamagedByMetal(true);
        Weapon pickax = new Weapon(20, 1.5f, 5.0f, 0.0f, 3, 3, 0.1f, 3.0);
        pickax.setDamagedByMetal(true);
        Weapon shovel = new Weapon(25, 1.0f, 5.0f, 0.0f, 4, 3, 1.0f, 3.0);
        shovel.setDamagedByMetal(true);
        Weapon rake = new Weapon(27, 0.5f, 5.0f, 0.0f, 5, 2, 1.0f, 3.0);
        rake.setDamagedByMetal(true);
        Weapon saw = new Weapon(24, 0.5f, 5.0f, 0.01f, 2, 3, 0.0f, 3.0);
        Weapon sickle = new Weapon(267, 6.0f, 3.0f, 0.02f, 2, 3, 0.2f, 2.0);
        Weapon scythe = new Weapon(268, 9.0f, 5.0f, 0.08f, 5, 4, 0.2f, 2.0);
        scythe.setDamagedByMetal(true);
        Weapon longswo = new Weapon(21, 5.5f, 4.0f, 0.01f, 3, 3, 1.0f, 0.0);
        Weapon twoswo = new Weapon(81, 9.0f, 5.0f, 0.05f, 4, 5, 1.0f, 0.0);
        Weapon smallax = new Weapon(3, 5.0f, 3.0f, 0.0f, 2, 2, 0.3f, 0.0);
        Weapon batax = new Weapon(90, 6.5f, 4.0f, 0.03f, 4, 5, 0.3f, 0.0);
        Weapon twoaxe = new Weapon(87, 12.0f, 6.0f, 0.05f, 5, 5, 0.2f, 0.0);
        Weapon swoMag = new Weapon(336, 15.0f, 5.0f, 0.08f, 4, 3, 1.0f, 0.0);
        Weapon whipOne = new Weapon(514, 6.0f, 2.0f, 0.0f, 5, 1, 0.1f, 0.0);
        Weapon spearLong = new Weapon(705, 8.0f, 5.0f, 0.06f, 7, 3, 1.0f, 0.0);
        Weapon halberd = new Weapon(706, 9.0f, 5.0f, 0.06f, 6, 8, 1.0f, 0.0);
        Weapon steelSpear = new Weapon(707, 9.0f, 5.0f, 0.06f, 7, 4, 1.0f, 0.0);
        Weapon staffSteel = new Weapon(710, 8.0f, 4.0f, 0.0f, 3, 3, 1.0f, 0.0);
        Weapon staffLand = new Weapon(986, 8.0f, 4.0f, 0.0f, 3, 3, 1.0f, 0.0);
        Weapon fist = new Weapon(14, 1.0f, 1.0f, 0.0f, 1, 1, 0.0f, 2.0);
        Weapon foot = new Weapon(19, 1.0f, 2.0f, 0.0f, 1, 1, 0.0f, 3.0);
        Weapon plank = new Weapon(22, 0.5f, 4.0f, 0.0f, 2, 1, 1.0f, 3.0);
        plank.setDamagedByMetal(true);
        Weapon shaft = new Weapon(23, 0.5f, 4.0f, 0.0f, 2, 2, 1.0f, 3.0);
        shaft.setDamagedByMetal(true);
        Weapon staff = new Weapon(711, 2.0f, 3.0f, 0.0f, 2, 3, 1.0f, 0.0);
        staff.setDamagedByMetal(true);
        Weapon metalh = new Weapon(62, 0.5f, 3.0f, 0.0f, 1, 1, 0.1f, 3.0);
        metalh.setDamagedByMetal(true);
        Weapon woodenh = new Weapon(63, 0.3f, 3.0f, 0.0f, 1, 1, 0.1f, 3.0);
        woodenh.setDamagedByMetal(true);
        Weapon maulL = new Weapon(290, 11.0f, 6.0f, 0.03f, 4, 5, 1.0f, 0.0);
        Weapon maulM = new Weapon(292, 8.0f, 5.0f, 0.03f, 3, 2, 1.0f, 0.0);
        Weapon maulS = new Weapon(291, 4.5f, 3.0f, 0.01f, 2, 2, 1.0f, 0.0);
        Weapon belaying = new Weapon(567, 2.0f, 3.0f, 0.0f, 1, 1, 1.0f, 2.0);
        belaying.setDamagedByMetal(true);
        Weapon clubH = new Weapon(314, 8.0f, 6.0f, 0.01f, 4, 6, 1.0f, 2.0);
        clubH.setDamagedByMetal(true);
        Weapon hamMag = new Weapon(337, 18.0f, 6.0f, 0.08f, 4, 4, 1.0f, 0.0);
        Weapon sceptre = new Weapon(340, 17.0f, 6.0f, 0.08f, 3, 3, 1.0f, 0.0);
        Weapon steelCrowbar = new Weapon(1115, 4.5f, 3.0f, 0.01f, 2, 2, 1.0f, 0.0);
        long end = System.nanoTime();
        logger.info("Creating weapons took " + (float)(end - start) / 1000000.0f + " ms");
    }
}

