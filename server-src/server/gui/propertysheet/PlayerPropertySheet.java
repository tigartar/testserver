/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javafx.beans.property.SimpleObjectProperty
 *  javafx.beans.value.ObservableValue
 *  javafx.collections.FXCollections
 *  javafx.collections.ObservableList
 *  javafx.scene.Node
 *  javafx.scene.layout.Priority
 *  javafx.scene.layout.VBox
 */
package com.wurmonline.server.gui.propertysheet;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.gui.PlayerData;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

public class PlayerPropertySheet
extends VBox
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(PlayerPropertySheet.class.getName());
    private PlayerData current;
    private final ObservableList<PropertySheet.Item> list;
    private Set<PropertyType> changedProperties = new HashSet<PropertyType>();

    public PlayerPropertySheet(PlayerData entry) {
        this.current = entry;
        this.list = FXCollections.observableArrayList();
        this.list.add((Object)new CustomPropertyItem(PropertyType.NAME, "Name", "Player Name", "Name", true, entry.getName()));
        this.list.add((Object)new CustomPropertyItem(PropertyType.POSX, "Position X", "Position in X", "The X position of the player", true, Float.valueOf(entry.getPosx())));
        this.list.add((Object)new CustomPropertyItem(PropertyType.POSY, "Position Y", "Position in Y", "The Y position of the player", true, Float.valueOf(entry.getPosy())));
        this.list.add((Object)new CustomPropertyItem(PropertyType.POWER, "Power", "Player Game Management Power", "Power from 0 to 5. 2 is Game Manager, 4 is Head GM and 5 Implementor", true, entry.getPower()));
        this.list.add((Object)new CustomPropertyItem(PropertyType.CURRENTSERVER, "Current server", "Server id of the player", "The id of the server that the player is on", true, entry.getServer()));
        this.list.add((Object)new CustomPropertyItem(PropertyType.UNDEAD, "Undead", "Whether the player is undead", "Lets the player play as undead", true, entry.isUndead()));
        PropertySheet propertySheet = new PropertySheet(this.list);
        VBox.setVgrow((Node)propertySheet, (Priority)Priority.ALWAYS);
        this.getChildren().add((Object)propertySheet);
    }

    public PlayerData getCurrentData() {
        return this.current;
    }

    public final String save() {
        String toReturn = "";
        boolean saveAtAll = false;
        for (CustomPropertyItem item : (CustomPropertyItem[])this.list.toArray((Object[])new CustomPropertyItem[this.list.size()])) {
            if (!this.changedProperties.contains((Object)item.getPropertyType())) continue;
            saveAtAll = true;
            try {
                switch (item.getPropertyType()) {
                    case NAME: {
                        this.current.setName(item.getValue().toString());
                        break;
                    }
                    case POSX: {
                        this.current.setPosx(((Float)item.getValue()).floatValue());
                        break;
                    }
                    case POSY: {
                        this.current.setPosy(((Float)item.getValue()).floatValue());
                        break;
                    }
                    case POWER: {
                        this.current.setPower((Integer)item.getValue());
                        break;
                    }
                    case CURRENTSERVER: {
                        this.current.setServer((Integer)item.getValue());
                        break;
                    }
                    case UNDEAD: {
                        if (!this.current.isUndead()) {
                            this.current.setUndeadType((byte)(1 + Server.rand.nextInt(3)));
                            break;
                        }
                        this.current.setUndeadType((byte)0);
                    }
                }
            }
            catch (Exception ex) {
                saveAtAll = false;
                toReturn = toReturn + "Invalid value " + item.getCategory() + ": " + item.getValue() + ". ";
                logger.log(Level.INFO, "Error " + ex.getMessage(), ex);
            }
        }
        if (toReturn.length() == 0 && saveAtAll) {
            try {
                this.current.save();
                toReturn = "ok";
            }
            catch (Exception ex) {
                toReturn = ex.getMessage();
            }
        }
        return toReturn;
    }

    class CustomPropertyItem
    implements PropertySheet.Item {
        private PropertyType type;
        private String category;
        private String name;
        private String description;
        private boolean editable = true;
        private Object value;

        CustomPropertyItem(PropertyType aType, String aCategory, String aName, String aDescription, boolean aEditable, Object aValue) {
            this.type = aType;
            this.category = aCategory;
            this.name = aName;
            this.description = aDescription;
            this.editable = aEditable;
            this.value = aValue;
        }

        public PropertyType getPropertyType() {
            return this.type;
        }

        @Override
        public Class<?> getType() {
            return this.value.getClass();
        }

        @Override
        public String getCategory() {
            return this.category;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
            return PropertySheet.Item.super.getPropertyEditorClass();
        }

        @Override
        public boolean isEditable() {
            return this.editable;
        }

        @Override
        public Object getValue() {
            return this.value;
        }

        @Override
        public void setValue(Object aValue) {
            if (!this.value.equals(aValue)) {
                PlayerPropertySheet.this.changedProperties.add(this.type);
            }
            this.value = aValue;
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return Optional.of(new SimpleObjectProperty(this.value));
        }
    }

    private static enum PropertyType {
        NAME,
        POSX,
        POSY,
        POWER,
        CURRENTSERVER,
        UNDEAD;

    }
}

