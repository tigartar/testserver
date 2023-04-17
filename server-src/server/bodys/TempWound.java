/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.bodys;

import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TempWound
extends Wound {
    private static final Logger logger = Logger.getLogger(TempWound.class.getName());
    private static final long serialVersionUID = -7813873321822326094L;

    public TempWound(byte aType, byte aLocation, float aSeverity, long aOwner, float aPoisonSeverity, float aInfectionSeverity, boolean spell) {
        super(aType, aLocation, aSeverity, aOwner, aPoisonSeverity, aInfectionSeverity, true, false, spell);
    }

    @Override
    final void create() {
    }

    @Override
    final void setSeverity(float sev) {
        this.severity = sev;
    }

    @Override
    public final void setPoisonSeverity(float sev) {
        if (this.poisonSeverity != sev) {
            this.poisonSeverity = Math.max(0.0f, sev);
            this.poisonSeverity = Math.min(100.0f, this.poisonSeverity);
        }
    }

    @Override
    public final void setInfectionSeverity(float sev) {
        if (this.infectionSeverity != sev) {
            this.infectionSeverity = Math.max(0.0f, sev);
            this.infectionSeverity = Math.min(100.0f, this.infectionSeverity);
        }
    }

    @Override
    public final void setBandaged(boolean aBandaged) {
        if (this.isBandaged != aBandaged) {
            this.isBandaged = aBandaged;
        }
    }

    @Override
    final void setLastPolled(long lp) {
        if (this.lastPolled != lp) {
            this.lastPolled = lp;
        }
    }

    @Override
    public final void setHealeff(byte healeff) {
        block9: {
            if (this.healEff < healeff) {
                this.healEff = healeff;
                try {
                    if (this.getCreature().getBody() != null) {
                        Item bodypart = this.getCreature().getBody().getBodyPartForWound(this);
                        try {
                            Creature[] watchers = bodypart.getWatchers();
                            for (int x = 0; x < watchers.length; ++x) {
                                watchers[x].getCommunicator().sendUpdateWound(this, bodypart);
                            }
                            break block9;
                        }
                        catch (NoSuchCreatureException noSuchCreatureException) {
                            break block9;
                        }
                    }
                    if (this.getCreature() != null) {
                        logger.log(Level.WARNING, this.getCreature().getName() + " body is null.", new Exception());
                    } else {
                        logger.log(Level.WARNING, "Wound: creature==null", new Exception());
                    }
                }
                catch (NoSpaceException nsp) {
                    logger.log(Level.INFO, nsp.getMessage(), nsp);
                }
            }
        }
    }

    @Override
    final void delete() {
    }
}

