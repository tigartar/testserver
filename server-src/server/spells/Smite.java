/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.TempWound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;

public final class Smite
extends ReligiousSpell
implements AttitudeConstants {
    public static final int RANGE = 12;

    Smite() {
        super("Smite", 252, 30, 50, 70, 70, 30000L);
        this.targetCreature = true;
        this.offensive = true;
        this.description = "damages the targets body with extreme fire damage depending on how healthy they are";
        this.type = (byte)2;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && !performer.getDeity().isLibila() && performer.faithful) {
            performer.getCommunicator().sendNormalServerMessage(performer.getDeity().getName() + " would never accept your smiting " + target.getName() + ".", (byte)3);
            return false;
        }
        return true;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && !performer.getDeity().isLibila()) {
            performer.modifyFaith(-5.0f);
        }
        if (Server.rand.nextFloat() > target.addSpellResistance((short)252)) {
            performer.getCommunicator().sendNormalServerMessage(target.getName() + " resists your attempt to smite " + target.getHimHerItString() + ".", (byte)3);
            target.getCommunicator().sendSafeServerMessage(performer.getName() + " tries to smite you but you resist.", (byte)4);
            return;
        }
        int damage = target.getStatus().damage;
        int minhealth = 65435;
        if (target.isUnique()) {
            minhealth = 15535;
        }
        double maxdam = Math.max(0, minhealth - damage);
        maxdam *= 0.5 + 0.5 * (power / 100.0);
        float resistance = (float)SpellResist.getSpellResistance(target, this.getNumber());
        SpellResist.addSpellResistance(target, this.getNumber(), maxdam *= (double)resistance);
        maxdam = Spell.modifyDamage(target, maxdam);
        if (maxdam > 500.0) {
            performer.getCommunicator().sendNormalServerMessage("You smite " + target.getName() + ".", (byte)2);
            target.getCommunicator().sendAlertServerMessage(performer.getName() + " smites you.", (byte)4);
            TempWound wound = null;
            if (target instanceof Player) {
                target.addWoundOfType(null, (byte)4, 0, false, 1.0f, true, maxdam, 0.0f, 0.0f, false, true);
            } else {
                wound = new TempWound(4, 0, (float)maxdam, target.getWurmId(), 0.0f, 0.0f, true);
                target.getBody().addWound(wound);
            }
            VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
            if (t != null) {
                t.sendAttachCreatureEffect(target, (byte)10, (byte)0, (byte)0, (byte)0, (byte)0);
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You try to smite " + target.getName() + " but there seems to be no effect.", (byte)3);
            target.getCommunicator().sendNormalServerMessage(performer.getName() + " tries to smite you but to no avail.", (byte)4);
        }
    }
}

