/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.DamageSpell;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.constants.AttitudeConstants;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FireHeart
extends DamageSpell
implements AttitudeConstants {
    private static Logger logger = Logger.getLogger(FireHeart.class.getName());
    public static final int RANGE = 50;
    public static final double BASE_DAMAGE = 9000.0;
    public static final double DAMAGE_PER_POWER = 80.0;

    public FireHeart() {
        super("Fireheart", 424, 7, 20, 20, 35, 30000L);
        this.targetCreature = true;
        this.offensive = true;
        this.description = "damages the targets heart with superheated fire";
        this.type = (byte)2;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && performer.faithful && !performer.isDuelOrSpar(target)) {
            performer.getCommunicator().sendNormalServerMessage(performer.getDeity().getName() + " would never accept your attack on " + target.getNameWithGenus() + ".", (byte)3);
            return false;
        }
        return true;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        VolaTile t;
        if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && !performer.isDuelOrSpar(target)) {
            performer.modifyFaith(-5.0f);
        }
        if ((t = target.getCurrentTile()) != null) {
            t.sendAddQuickTileEffect((byte)35, target.getFloorLevel());
            t.sendAttachCreatureEffect(target, (byte)5, (byte)0, (byte)0, (byte)0, (byte)0);
        }
        try {
            byte pos = target.getBody().getCenterWoundPos();
            double damage = this.calculateDamage(target, power, 9000.0, 80.0);
            target.addWoundOfType(performer, (byte)4, pos, false, 1.0f, false, damage, 0.0f, 0.0f, false, true);
        }
        catch (Exception exe) {
            logger.log(Level.WARNING, exe.getMessage(), exe);
        }
    }
}

