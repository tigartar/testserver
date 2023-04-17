/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.bodys;

import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.BodyBear;
import com.wurmonline.server.bodys.BodyBird;
import com.wurmonline.server.bodys.BodyCyclops;
import com.wurmonline.server.bodys.BodyDog;
import com.wurmonline.server.bodys.BodyDragon;
import com.wurmonline.server.bodys.BodyEttin;
import com.wurmonline.server.bodys.BodyHorse;
import com.wurmonline.server.bodys.BodyHuman;
import com.wurmonline.server.bodys.BodySnake;
import com.wurmonline.server.bodys.BodySpider;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.util.HashMap;
import java.util.Map;

public final class BodyFactory {
    private static final Map<Byte, BodyTemplate> bodyTemplates = new HashMap<Byte, BodyTemplate>();

    private BodyFactory() {
    }

    public static Body getBody(Creature creature, byte typ, short centimetersHigh, short centimetersLong, short centimetersWide) throws Exception {
        BodyTemplate template = bodyTemplates.get(typ);
        if (template != null) {
            return new Body(template, creature, centimetersHigh, centimetersLong, centimetersWide);
        }
        throw new WurmServerException("No such bodytype: " + Byte.toString(typ));
    }

    static {
        bodyTemplates.put((byte)0, new BodyHuman());
        bodyTemplates.put((byte)3, new BodyDog());
        bodyTemplates.put((byte)1, new BodyHorse());
        bodyTemplates.put((byte)4, new BodyEttin());
        bodyTemplates.put((byte)5, new BodyCyclops());
        bodyTemplates.put((byte)2, new BodyBear());
        bodyTemplates.put((byte)6, new BodyDragon());
        bodyTemplates.put((byte)7, new BodyBird());
        bodyTemplates.put((byte)8, new BodySpider());
        bodyTemplates.put((byte)9, new BodySnake());
    }
}

