/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KarmaQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(Question.class.getName());

    public KarmaQuestion(Creature aResponder) {
        super(aResponder, "Using your Karma", "Decide how you wish to use your Karma", 100, aResponder.getWurmId());
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void answer(Properties answers) {
        String key = "karma";
        String val = answers.getProperty("karma");
        if (val == null) return;
        if (val.equals("light50")) {
            if (this.getResponder().getKarma() >= 500) {
                Item[] items;
                int sx = Zones.safeTileX(this.getResponder().getTileX() - 50);
                int ex = Zones.safeTileX(this.getResponder().getTileX() + 50);
                int sy = Zones.safeTileY(this.getResponder().getTileY() - 50);
                int ey = Zones.safeTileY(this.getResponder().getTileY() + 50);
                for (Item item : items = Items.getAllItems()) {
                    if (item.getZoneId() <= 0 || !item.isStreetLamp() || !item.isWithin(sx, ex, sy, ey) || !item.isPlanted()) continue;
                    item.setAuxData((byte)120);
                    item.setTemperature((short)10000);
                }
                Server.getInstance().broadCastAction(this.getResponder().getName() + " convinces the fire spirits to light up the area!", this.getResponder(), 50);
                this.getResponder().getCommunicator().sendSafeServerMessage("The fire spirits light up the area!");
                this.getResponder().modifyKarma(-500);
                return;
            } else {
                this.getResponder().getCommunicator().sendNormalServerMessage("You need 500 karma for this.");
            }
            return;
        }
        if (val.equals("light100")) {
            if (this.getResponder().getKarma() >= 1500) {
                Item[] items;
                int sx = Zones.safeTileX(this.getResponder().getTileX() - 100);
                int ex = Zones.safeTileX(this.getResponder().getTileX() + 100);
                int sy = Zones.safeTileY(this.getResponder().getTileY() - 100);
                int ey = Zones.safeTileY(this.getResponder().getTileY() + 100);
                for (Item item : items = Items.getAllItems()) {
                    if (item.getZoneId() <= 0 || !item.isStreetLamp() || !item.isWithin(sx, ex, sy, ey) || !item.isPlanted()) continue;
                    item.setAuxData((byte)120);
                    item.setTemperature((short)10000);
                }
                Server.getInstance().broadCastAction(this.getResponder().getName() + " convinces the fire spirits to light up the area!", this.getResponder(), 100);
                this.getResponder().getCommunicator().sendSafeServerMessage("The fire spirits light up the area!");
                this.getResponder().modifyKarma(-1500);
                return;
            } else {
                this.getResponder().getCommunicator().sendNormalServerMessage("You need 1500 karma for this.");
            }
            return;
        }
        if (val.equals("light200")) {
            if (this.getResponder().getKarma() >= 3000) {
                Item[] items;
                int sx = Zones.safeTileX(this.getResponder().getTileX() - 200);
                int ex = Zones.safeTileX(this.getResponder().getTileX() + 200);
                int sy = Zones.safeTileY(this.getResponder().getTileY() - 200);
                int ey = Zones.safeTileY(this.getResponder().getTileY() + 200);
                for (Item item : items = Items.getAllItems()) {
                    if (item.getZoneId() <= 0 || !item.isStreetLamp() || !item.isWithin(sx, ex, sy, ey) || !item.isPlanted()) continue;
                    item.setAuxData((byte)120);
                    item.setTemperature((short)10000);
                }
                Server.getInstance().broadCastAction(this.getResponder().getName() + " convinces the fire spirits to light up the area!", this.getResponder(), 200);
                this.getResponder().getCommunicator().sendSafeServerMessage("The fire spirits light up the area!");
                this.getResponder().modifyKarma(-3000);
                return;
            } else {
                this.getResponder().getCommunicator().sendNormalServerMessage("You need 3000 karma for this.");
            }
            return;
        }
        if (val.equals("corpse")) {
            if (this.getResponder().getKarma() > 3000) {
                if (this.getResponder().maySummonCorpse()) {
                    Item toSummon = null;
                    int maxContained = 0;
                    for (Item i : Items.getAllItems()) {
                        int nums;
                        if (i.getOwnerId() > -10L || !i.getName().equals("corpse of " + this.getResponder().getName()) || (nums = i.getItems().size()) < maxContained) continue;
                        toSummon = i;
                        maxContained = nums;
                    }
                    if (toSummon != null) {
                        if (toSummon.getZoneId() >= 0) {
                            try {
                                Zone z = Zones.getZone((int)toSummon.getPosX() >> 2, (int)toSummon.getPosY() >> 2, toSummon.isOnSurface());
                                z.removeItem(toSummon);
                                logger.log(Level.INFO, toSummon.getName() + " was removed from " + ((int)toSummon.getPosX() >> 2) + ',' + ((int)toSummon.getPosY() >> 2) + ", surf=" + toSummon.isOnSurface());
                            }
                            catch (NoSuchZoneException nsz) {
                                logger.log(Level.INFO, toSummon.getName() + " was not on " + ((int)toSummon.getPosX() >> 2) + ',' + ((int)toSummon.getPosY() >> 2) + ", surf=" + toSummon.isOnSurface());
                            }
                        }
                        try {
                            Item parent = toSummon.getParent();
                            parent.dropItem(toSummon.getWurmId(), true);
                            logger.log(Level.INFO, toSummon.getName() + " was removed from " + parent.getName() + '.');
                        }
                        catch (NoSuchItemException parent) {
                            // empty catch block
                        }
                        this.getResponder().getInventory().insertItem(toSummon);
                        this.getResponder().getCommunicator().sendSafeServerMessage("The spirits summon your corpse!");
                        this.getResponder().modifyKarma(-3000);
                        return;
                    }
                    this.getResponder().getCommunicator().sendSafeServerMessage("The spirits fail to locate your corpse!");
                    return;
                }
                long timeToNext = this.getResponder().getTimeToSummonCorpse();
                this.getResponder().getCommunicator().sendNormalServerMessage("You have to wait " + Server.getTimeFor(timeToNext) + " until you can summon your corpse.");
                return;
            }
            this.getResponder().getCommunicator().sendNormalServerMessage("You need 3000 karma for this.");
            return;
        }
        if (val.equals("townportal")) {
            EndGameItem egi;
            Item[] bodyItems;
            Item[] inventoryItems;
            for (Item lInventoryItem : inventoryItems = this.getResponder().getInventory().getAllItems(true)) {
                if (!lInventoryItem.isArtifact()) continue;
                this.getResponder().getCommunicator().sendNormalServerMessage("The " + lInventoryItem.getName() + " hums and disturbs the weave. You can not teleport right now.");
                return;
            }
            for (Item lInventoryItem : bodyItems = this.getResponder().getBody().getBodyItem().getAllItems(true)) {
                if (!lInventoryItem.isArtifact()) continue;
                this.getResponder().getCommunicator().sendNormalServerMessage("The " + lInventoryItem.getName() + " hums and disturbs the weave. You can not teleport right now.");
                return;
            }
            if (this.getResponder().getEnemyPresense() > 0 || this.getResponder().isFighting()) {
                this.getResponder().getCommunicator().sendNormalServerMessage("There are enemies in the vicinity. You fail to focus.");
                return;
            }
            if (this.getResponder().getCitizenVillage() == null) {
                this.getResponder().getCommunicator().sendNormalServerMessage("You need to be citizen in a village to teleport home.");
                return;
            }
            if (this.getResponder().mayChangeVillageInMillis() > 0L) {
                this.getResponder().getCommunicator().sendNormalServerMessage("You are still too new to this village to teleport home.");
                return;
            }
            if (this.getResponder().getKarma() < 1000) {
                this.getResponder().getCommunicator().sendNormalServerMessage("You need 1000 karma to perform this feat.");
                return;
            }
            if (this.getResponder().isOnPvPServer() && Zones.isWithinDuelRing(this.getResponder().getTileX(), this.getResponder().getTileY(), true) != null) {
                this.getResponder().getCommunicator().sendNormalServerMessage("The magic of the duelling ring interferes. You can not teleport here.");
                return;
            }
            if (this.getResponder().isInPvPZone()) {
                this.getResponder().getCommunicator().sendNormalServerMessage("The magic of the pvp zone interferes. You can not teleport here.");
                return;
            }
            if (Servers.localServer.PVPSERVER && EndGameItems.getEvilAltar() != null) {
                egi = EndGameItems.getEvilAltar();
                if (this.getResponder().isWithinDistanceTo(egi.getItem().getPosX(), egi.getItem().getPosY(), egi.getItem().getPosZ(), 50.0f)) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("The magic of this place interferes. You can not teleport here.");
                    return;
                }
            } else if (Servers.localServer.PVPSERVER && EndGameItems.getGoodAltar() != null) {
                egi = EndGameItems.getGoodAltar();
                if (this.getResponder().isWithinDistanceTo(egi.getItem().getPosX(), egi.getItem().getPosY(), egi.getItem().getPosZ(), 50.0f)) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("The magic of this place interferes. You can not teleport here.");
                    return;
                }
            }
            try {
                short[] tokenCoords;
                try {
                    tokenCoords = this.getResponder().getCitizenVillage().getTokenCoords();
                }
                catch (NoSuchItemException nsi) {
                    tokenCoords = this.getResponder().getCitizenVillage().getSpawnPoint();
                }
                this.getResponder().setTeleportPoints(tokenCoords[0], tokenCoords[1], 0, 0);
                if (!this.getResponder().startTeleporting()) return;
                this.getResponder().modifyKarma(-1000);
                this.getResponder().getCommunicator().sendNormalServerMessage("You feel a slight tingle in your spine.");
                this.getResponder().getCommunicator().sendTeleport(false);
                return;
            }
            catch (Exception ex) {
                this.getResponder().getCommunicator().sendNormalServerMessage("The weave does not contain a proper teleport spot.");
                return;
            }
        }
        this.getResponder().getCommunicator().sendNormalServerMessage("You decide to bide your time.");
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("text{text=\"You have " + this.getResponder().getKarma() + " karma, how would you like to spend it?\"}");
        buf.append("text{text=''}");
        buf.append("radio{ group='karma'; id='light50';text='Light up 50 tiles radius (500 karma)'}");
        buf.append("radio{ group='karma'; id='light100';text='Light up 100 tiles radius (1500 karma)'}");
        buf.append("radio{ group='karma'; id='light200';text='Light up 200 tiles radius (3000 karma)'}");
        buf.append("radio{ group='karma'; id='corpse';text='Summon corpse (3000 karma, 5 minutes delay)'}");
        buf.append("radio{ group='karma'; id='townportal';text='Town Portal (1000 karma, enemies block)'}");
        buf.append("radio{ group='karma'; id='false';text='Do nothing';selected='true'}");
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

