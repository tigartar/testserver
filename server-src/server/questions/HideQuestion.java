/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.zones.Zones;
import java.util.Properties;

public final class HideQuestion
extends Question {
    public HideQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 70, aTarget);
    }

    @Override
    public void answer(Properties answers) {
        block10: {
            if (this.getResponder().getPower() >= 2) {
                boolean putOnSurface = false;
                String key2 = "putonsurf";
                String val2 = answers.getProperty("putonsurf");
                if (val2 != null && val2.equals("true")) {
                    putOnSurface = true;
                }
                String key = "height";
                String val = answers.getProperty("height");
                if (val != null && val.length() > 0 || putOnSurface) {
                    try {
                        int x = val == null ? 0 : Integer.parseInt(val);
                        try {
                            Item i = Items.getItem(this.target);
                            short rock = Tiles.decodeHeight(Server.rockMesh.getTile(this.getResponder().getCurrentTile().tilex, this.getResponder().getCurrentTile().tiley));
                            short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(this.getResponder().getCurrentTile().tilex, this.getResponder().getCurrentTile().tiley));
                            int diff = height - rock;
                            if (i.getOwnerId() != -10L && i.getOwnerId() != this.getResponder().getWurmId()) break block10;
                            if (x < diff || putOnSurface) {
                                Items.hideItem(this.getResponder(), i, (float)(rock + x) / 10.0f, putOnSurface);
                                if (putOnSurface) {
                                    this.getResponder().getCommunicator().sendNormalServerMessage("You carefully hide the " + i.getName() + " here.");
                                } else {
                                    this.getResponder().getCommunicator().sendNormalServerMessage("You carefully hide the " + i.getName() + " at " + (float)(rock + x) / 10.0f + " meters.");
                                }
                                break block10;
                            }
                            this.getResponder().getCommunicator().sendNormalServerMessage("You can not hide the " + i.getName() + " at " + (rock + x) + ". Rock is at " + rock + ", and surface is at " + height + ".");
                        }
                        catch (NoSuchItemException nsi) {
                            this.getResponder().getCommunicator().sendNormalServerMessage("The item can no longer be found!");
                        }
                    }
                    catch (NumberFormatException nf) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Failed to parse " + val + " as an integer number.");
                    }
                }
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        try {
            Item it = Items.getItem(this.target);
            buf.append("text{type='';text='Hiding " + it.getName() + ".'}");
            if (!this.getResponder().isOnSurface()) {
                buf.append("text{type='';text='You can only hide items on the surface now.'}");
            } else {
                short rock = Tiles.decodeHeight(Server.rockMesh.getTile(this.getResponder().getCurrentTile().tilex, this.getResponder().getCurrentTile().tiley));
                short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(this.getResponder().getCurrentTile().tilex, this.getResponder().getCurrentTile().tiley));
                int diff = height - rock;
                buf.append("text{type='';text='The rock is at " + rock + " decimeter, soil at " + height + " decimeter above sea level. Suggested height above rock is " + diff / 2 + " decimeter.'}");
                if (diff > 3) {
                    buf.append("harray{input{id='height'; maxchars='4'; text='" + diff / 2 + "'}label{text='Height in decimeters over rock layer'}}");
                } else {
                    buf.append("text{type='';text='The soil here is too shallow.'}");
                }
                buf.append("harray{label{text=\"Just put on surface \"};checkbox{id=\"putonsurf\";selected=\"false\"};}");
                buf.append("text{type='';text='Here is a random location position for treasure hunts:'}");
                this.findTreasureHuntLocation(buf);
            }
        }
        catch (NoSuchItemException noSuchItemException) {
            // empty catch block
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private final void findTreasureHuntLocation(StringBuilder buf) {
        for (int x = 0; x < 10; ++x) {
            int diff;
            int suggx = Server.rand.nextInt(Zones.worldTileSizeX);
            int suggy = Server.rand.nextInt(Zones.worldTileSizeY);
            short rock = Tiles.decodeHeight(Server.rockMesh.getTile(suggx, suggy));
            short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(suggx, suggy));
            if (height <= 0 || (diff = height - rock) < 2) continue;
            buf.append("text{type='';text='Tile at " + suggx + ", " + suggy + " has depth " + diff + "'}");
            break;
        }
    }
}

