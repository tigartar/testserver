/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.combat;

import com.wurmonline.server.Constants;
import com.wurmonline.server.combat.BattleEvent;
import com.wurmonline.server.creatures.Creature;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Battle {
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat filedf = new SimpleDateFormat("yyyy-MM-ddHHmmss");
    private final Set<Creature> creatures = new HashSet<Creature>();
    private List<Creature> casualties;
    private final List<BattleEvent> events;
    private final long startTime;
    private long endTime;
    private final String name;
    private static final Logger logger = Logger.getLogger(Battle.class.getName());
    private static final String header = "<HTML> <HEAD><TITLE>Wurm battle log</TITLE></HEAD><BODY><BR><BR><B>";
    private static final String footer = "</BODY></HTML>";

    Battle(Creature attacker, Creature defender) {
        this.creatures.add(attacker);
        this.creatures.add(defender);
        this.startTime = System.currentTimeMillis();
        this.endTime = System.currentTimeMillis();
        attacker.setBattle(this);
        defender.setBattle(this);
        this.events = new LinkedList<BattleEvent>();
        this.name = "Battle_" + attacker.getName() + "_vs_" + defender.getName();
    }

    boolean containsCreature(Creature creature) {
        return this.creatures.contains(creature);
    }

    void addCreature(Creature creature) {
        if (!this.creatures.contains(creature)) {
            this.creatures.add(creature);
            this.events.add(new BattleEvent(-1, creature.getName()));
            creature.setBattle(this);
        }
        this.touch();
    }

    public void removeCreature(Creature creature) {
        this.creatures.remove(creature);
        creature.setBattle(null);
        this.events.add(new BattleEvent(-2, creature.getName()));
        this.touch();
    }

    void clearCreatures() {
        this.creatures.clear();
    }

    public void addCasualty(Creature dead) {
        if (this.casualties == null) {
            this.casualties = new LinkedList<Creature>();
        }
        this.casualties.add(dead);
        this.events.add(new BattleEvent(-3, dead.getName()));
        this.creatures.remove(dead);
        dead.setBattle(null);
        this.touch();
    }

    void touch() {
        this.endTime = System.currentTimeMillis();
    }

    public void addCasualty(Creature killer, Creature dead) {
        if (this.casualties == null) {
            this.casualties = new LinkedList<Creature>();
        }
        this.casualties.add(dead);
        this.events.add(new BattleEvent(-3, dead.getName(), killer.getName()));
        this.creatures.remove(dead);
        dead.setBattle(null);
        this.touch();
    }

    public void addEvent(BattleEvent event) {
        this.events.add(event);
        this.touch();
    }

    Creature[] getCreatures() {
        return this.creatures.toArray(new Creature[this.creatures.size()]);
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void save() {
        if (this.casualties != null && this.casualties.size() > 0) {
            Writer output = null;
            try {
                Date d = new Date(this.startTime);
                String dir = Constants.webPath;
                if (!dir.endsWith(File.separator)) {
                    dir = dir + File.separator;
                }
                File aFile = new File(dir + this.name + "_" + this.filedf.format(d) + ".html");
                output = new BufferedWriter(new FileWriter(aFile));
                String start = this.name + "</B><BR><I>started at " + this.df.format(d) + " and ended on " + this.df.format(new Date(this.endTime)) + "</I><BR><BR>";
                try {
                    output.write(header);
                    output.write(start);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage(), iox);
                }
                for (BattleEvent lBattleEvent : this.events) {
                    String ts = lBattleEvent.toString();
                    try {
                        output.write(ts);
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, iox.getMessage(), iox);
                    }
                }
                output.write(footer);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to close " + this.name, iox);
            }
            finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                }
                catch (IOException iox) {}
            }
        }
        for (Creature cret : this.creatures) {
            cret.setBattle(null);
        }
    }
}

