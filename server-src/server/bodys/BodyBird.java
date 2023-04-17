/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.bodys;

import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.shared.exceptions.WurmServerException;

final class BodyBird
extends BodyTemplate {
    BodyBird() {
        super((byte)7);
        this.leftArmS = "left wing";
        this.rightArmS = "right wing";
        this.leftHandS = "left wingtip";
        this.rightHandS = "right wingtip";
        this.leftFootS = "left claw";
        this.rightFootS = "right claw";
        this.legsS = "claws";
        this.lowerBackS = "tail";
        this.baseOfNoseS = "beak";
        this.legsS = "claws";
        this.typeString = new String[]{this.bodyS, this.headS, this.bodyS, this.leftArmS, this.rightArmS, this.leftArmS, this.rightArmS, this.leftFootS, this.rightFootS, this.leftArmS, this.rightArmS, this.leftFootS, this.rightFootS, this.leftHandS, this.rightHandS, this.leftFootS, this.rightFootS, this.neckS, this.headS, this.headS, this.headS, this.bodyS, this.topBackS, this.stomachS, this.lowerBackS, this.bodyS, this.leftArmS, this.rightArmS, this.headS, this.headS, this.leftLegS, this.rightLegS, this.bodyS, this.baseOfNoseS, this.legsS};
    }

    @Override
    public byte getRandomWoundPos() throws Exception {
        int rand = Server.rand.nextInt(100);
        if (rand < 10) {
            return 1;
        }
        if (rand < 30) {
            return 13;
        }
        if (rand < 60) {
            return 14;
        }
        if (rand < 85) {
            return 0;
        }
        if (rand < 95) {
            return 15;
        }
        if (rand < 100) {
            return 16;
        }
        throw new WurmServerException("Bad randomizer");
    }

    @Override
    void buildBody(Item[] spaces, Creature owner) {
        spaces[0].setOwner(owner.getWurmId(), true);
        spaces[0].insertItem(spaces[1]);
        spaces[1].insertItem(spaces[29]);
        spaces[0].insertItem(spaces[2]);
        spaces[2].insertItem(spaces[34]);
        spaces[34].insertItem(spaces[15]);
        spaces[34].insertItem(spaces[16]);
        spaces[34].insertItem(spaces[3]);
        spaces[34].insertItem(spaces[4]);
        spaces[3].insertItem(spaces[13]);
        spaces[4].insertItem(spaces[14]);
    }
}

