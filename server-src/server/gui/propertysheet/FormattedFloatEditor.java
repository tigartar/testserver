/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javafx.beans.value.ChangeListener
 *  javafx.beans.value.ObservableValue
 *  javafx.scene.Node
 *  javafx.scene.control.TextField
 */
package com.wurmonline.server.gui.propertysheet;

import com.wurmonline.server.gui.propertysheet.ServerPropertySheet;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

final class FormattedFloatEditor<T>
implements PropertyEditor<T> {
    PropertySheet.Item item;
    TextField textField;

    FormattedFloatEditor(PropertySheet.Item item) {
        this.item = item;
        this.textField = new TextField();
        this.textField.setOnAction(ae -> {
            item.setValue(this.getValue());
            this.textField.textProperty().setValue(this.getValue().toString());
        });
        this.textField.textProperty().addListener((ChangeListener)new ChangeListener<String>(){

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.length() > 0 && !newValue.matches("(^\\d*\\.?\\d*[0-9]+\\d*$)|(^[0-9]+\\d*\\.\\d*$)")) {
                    FormattedFloatEditor.this.textField.textProperty().setValue(oldValue);
                }
            }
        });
        this.textField.focusedProperty().addListener((ChangeListener)new ChangeListener<Boolean>(){

            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue.booleanValue()) {
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
        this.textField.setText(FormattedFloatEditor.objToString(t));
    }

    public static String objToString(Object value) {
        return value.toString();
    }

    private Object stringToObj(String str, Class<?> cls) {
        try {
            if (str == null) {
                return null;
            }
            String name = cls.getName();
            Object oMin = null;
            Object oMax = null;
            if (this.item instanceof ServerPropertySheet.CustomPropertyItem) {
                oMin = ((ServerPropertySheet.CustomPropertyItem)this.item).getMinValue();
                oMax = ((ServerPropertySheet.CustomPropertyItem)this.item).getMaxValue();
            }
            if (name.equals("float") || cls.equals(Float.class)) {
                Float min = (Float)oMin;
                Float max = (Float)oMax;
                if (str.length() == 0 || str.length() == 1 && str.contains(".")) {
                    return min != null ? min : new Float(0.0f);
                }
                Float val = new Float(str);
                if (min != null && val.floatValue() < min.floatValue()) {
                    return min;
                }
                if (max != null && val.floatValue() > max.floatValue()) {
                    return max;
                }
                return val;
            }
            return null;
        }
        catch (Throwable t) {
            return null;
        }
    }
}

