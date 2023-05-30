package com.wurmonline.server.gui.propertysheet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

final class FormattedFloatEditor<T> implements PropertyEditor<T> {
   PropertySheet.Item item;
   TextField textField;

   FormattedFloatEditor(final PropertySheet.Item item) {
      this.item = item;
      this.textField = new TextField();
      this.textField.setOnAction(ae -> {
         item.setValue(this.getValue());
         this.textField.textProperty().setValue(this.getValue().toString());
      });
      this.textField.textProperty().addListener(new ChangeListener<String>() {
         public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (newValue.length() > 0 && !newValue.matches("(^\\d*\\.?\\d*[0-9]+\\d*$)|(^[0-9]+\\d*\\.\\d*$)")) {
               FormattedFloatEditor.this.textField.textProperty().setValue(oldValue);
            }
         }
      });
      this.textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
         public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (!newValue) {
               System.out.println("Focus lost");
               item.setValue(FormattedFloatEditor.this.getValue());
               FormattedFloatEditor.this.textField.textProperty().setValue(FormattedFloatEditor.this.getValue().toString());
            }
         }
      });
   }

   @Override
   public Node getEditor() {
      return this.textField;
   }

   @Override
   public T getValue() {
      return (T)this.stringToObj(this.textField.getText(), this.item.getType());
   }

   @Override
   public void setValue(T t) {
      this.textField.setText(objToString(t));
   }

   public static String objToString(Object value) {
      return value.toString();
   }

   private Object stringToObj(String str, Class<?> cls) {
      try {
         if (str == null) {
            return null;
         } else {
            String name = cls.getName();
            Object oMin = null;
            Object oMax = null;
            if (this.item instanceof ServerPropertySheet.CustomPropertyItem) {
               oMin = ((ServerPropertySheet.CustomPropertyItem)this.item).getMinValue();
               oMax = ((ServerPropertySheet.CustomPropertyItem)this.item).getMaxValue();
            }

            if (!name.equals("float") && !cls.equals(Float.class)) {
               return null;
            } else {
               Float min = (Float)oMin;
               Float max = (Float)oMax;
               if (str.length() == 0 || str.length() == 1 && str.contains(".")) {
                  return min != null ? min : new Float(0.0F);
               } else {
                  Float val = new Float(str);
                  if (min != null && val < min) {
                     return min;
                  } else {
                     return max != null && val > max ? max : val;
                  }
               }
            }
         }
      } catch (Throwable var9) {
         return null;
      }
   }
}
