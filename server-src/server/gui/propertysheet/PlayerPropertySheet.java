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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

public class PlayerPropertySheet extends VBox implements MiscConstants {
   private static final Logger logger = Logger.getLogger(PlayerPropertySheet.class.getName());
   private PlayerData current;
   private final ObservableList<PropertySheet.Item> list;
   private Set<PlayerPropertySheet.PropertyType> changedProperties = new HashSet<>();

   public PlayerPropertySheet(PlayerData entry) {
      this.current = entry;
      this.list = FXCollections.observableArrayList();
      this.list.add(new PlayerPropertySheet.CustomPropertyItem(PlayerPropertySheet.PropertyType.NAME, "Name", "Player Name", "Name", true, entry.getName()));
      this.list
         .add(
            new PlayerPropertySheet.CustomPropertyItem(
               PlayerPropertySheet.PropertyType.POSX, "Position X", "Position in X", "The X position of the player", true, entry.getPosx()
            )
         );
      this.list
         .add(
            new PlayerPropertySheet.CustomPropertyItem(
               PlayerPropertySheet.PropertyType.POSY, "Position Y", "Position in Y", "The Y position of the player", true, entry.getPosy()
            )
         );
      this.list
         .add(
            new PlayerPropertySheet.CustomPropertyItem(
               PlayerPropertySheet.PropertyType.POWER,
               "Power",
               "Player Game Management Power",
               "Power from 0 to 5. 2 is Game Manager, 4 is Head GM and 5 Implementor",
               true,
               entry.getPower()
            )
         );
      this.list
         .add(
            new PlayerPropertySheet.CustomPropertyItem(
               PlayerPropertySheet.PropertyType.CURRENTSERVER,
               "Current server",
               "Server id of the player",
               "The id of the server that the player is on",
               true,
               entry.getServer()
            )
         );
      this.list
         .add(
            new PlayerPropertySheet.CustomPropertyItem(
               PlayerPropertySheet.PropertyType.UNDEAD, "Undead", "Whether the player is undead", "Lets the player play as undead", true, entry.isUndead()
            )
         );
      PropertySheet propertySheet = new PropertySheet(this.list);
      VBox.setVgrow(propertySheet, Priority.ALWAYS);
      this.getChildren().add(propertySheet);
   }

   public PlayerData getCurrentData() {
      return this.current;
   }

   public final String save() {
      String toReturn = "";
      boolean saveAtAll = false;

      for(PlayerPropertySheet.CustomPropertyItem item : (PlayerPropertySheet.CustomPropertyItem[])this.list
         .toArray(new PlayerPropertySheet.CustomPropertyItem[this.list.size()])) {
         if (this.changedProperties.contains(item.getPropertyType())) {
            saveAtAll = true;

            try {
               switch(item.getPropertyType()) {
                  case NAME:
                     this.current.setName(item.getValue().toString());
                     break;
                  case POSX:
                     this.current.setPosx(item.getValue());
                     break;
                  case POSY:
                     this.current.setPosy(item.getValue());
                     break;
                  case POWER:
                     this.current.setPower(item.getValue());
                     break;
                  case CURRENTSERVER:
                     this.current.setServer(item.getValue());
                     break;
                  case UNDEAD:
                     if (!this.current.isUndead()) {
                        this.current.setUndeadType((byte)(1 + Server.rand.nextInt(3)));
                     } else {
                        this.current.setUndeadType((byte)0);
                     }
               }
            } catch (Exception var9) {
               saveAtAll = false;
               toReturn = toReturn + "Invalid value " + item.getCategory() + ": " + item.getValue() + ". ";
               logger.log(Level.INFO, "Error " + var9.getMessage(), (Throwable)var9);
            }
         }
      }

      if (toReturn.length() == 0 && saveAtAll) {
         try {
            this.current.save();
            toReturn = "ok";
         } catch (Exception var8) {
            toReturn = var8.getMessage();
         }
      }

      return toReturn;
   }

   class CustomPropertyItem implements PropertySheet.Item {
      private PlayerPropertySheet.PropertyType type;
      private String category;
      private String name;
      private String description;
      private boolean editable = true;
      private Object value;

      CustomPropertyItem(PlayerPropertySheet.PropertyType aType, String aCategory, String aName, String aDescription, boolean aEditable, Object aValue) {
         this.type = aType;
         this.category = aCategory;
         this.name = aName;
         this.description = aDescription;
         this.editable = aEditable;
         this.value = aValue;
      }

      public PlayerPropertySheet.PropertyType getPropertyType() {
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
