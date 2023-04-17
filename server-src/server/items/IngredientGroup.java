/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.items.Ingredient;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSuchTemplateException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class IngredientGroup {
    private static final Logger logger = Logger.getLogger(IngredientGroup.class.getName());
    public static final byte INGREDIENT_GROUP_RESULT = -3;
    public static final byte INGREDIENT_GROUP_ACTIVE = -2;
    public static final byte INGREDIENT_GROUP_TARGET = -1;
    public static final byte INGREDIENT_GROUP_NONE = 0;
    public static final byte INGREDIENT_GROUP_MANDATORY = 1;
    public static final byte INGREDIENT_GROUP_ZERO_OR_ONE = 2;
    public static final byte INGREDIENT_GROUP_ONE_OF = 3;
    public static final byte INGREDIENT_GROUP_ONE_OR_MORE = 4;
    public static final byte INGREDIENT_GROUP_OPTIONAL = 5;
    public static final byte INGREDIENT_GROUP_ANY = 6;
    private final Map<String, Ingredient> ingredientsByName = new HashMap<String, Ingredient>();
    private final Map<Integer, Ingredient> ingredients = new HashMap<Integer, Ingredient>();
    private final byte groupType;
    private int groupDifficulty = 0;

    public IngredientGroup(byte groupType) {
        this.groupType = groupType;
    }

    public IngredientGroup(DataInputStream dis) throws IOException, NoSuchTemplateException {
        this.groupType = dis.readByte();
        int icount = dis.readByte();
        for (int i = 0; i < icount; ++i) {
            this.add(new Ingredient(dis));
        }
    }

    public void pack(DataOutputStream dos) throws IOException {
        dos.writeByte(this.groupType);
        dos.writeByte(this.ingredients.size());
        for (Ingredient ii : this.ingredients.values()) {
            ii.pack(dos);
        }
    }

    public void add(Ingredient ingredient) {
        if (this.groupType == 1) {
            this.ingredientsByName.put(ingredient.getTemplateName(), ingredient);
        }
        this.ingredients.put(Integer.valueOf(ingredient.getIngredientId()), ingredient);
    }

    public byte getGroupType() {
        return this.groupType;
    }

    public String getGroupTypeName() {
        switch (this.groupType) {
            case 1: {
                return "Mandatory";
            }
            case 2: {
                return "Zero or one";
            }
            case 3: {
                return "One of";
            }
            case 4: {
                return "One or more";
            }
            case 5: {
                return "Optional";
            }
            case 6: {
                return "Any";
            }
        }
        return "unknown";
    }

    public Ingredient[] getIngredients() {
        return this.ingredients.values().toArray(new Ingredient[this.ingredients.size()]);
    }

    public int size() {
        return this.ingredients.size();
    }

    public boolean contains(String ingredientName) {
        return this.ingredientsByName.containsKey(ingredientName);
    }

    @Nullable
    public Ingredient getIngredientByName(String ingredientName) {
        return this.ingredientsByName.get(ingredientName);
    }

    void clearFound() {
        for (Ingredient i : this.ingredients.values()) {
            i.setFound(false);
        }
        this.groupDifficulty = 0;
    }

    boolean matches(Item item) {
        for (Ingredient i : this.ingredients.values()) {
            if (!i.matches(item)) continue;
            this.groupDifficulty += i.setFound(true);
            return true;
        }
        return false;
    }

    boolean wasFound() {
        boolean count = false;
        switch (this.groupType) {
            case 1: {
                for (Ingredient i : this.ingredients.values()) {
                    if (i.wasFound(false, false)) continue;
                    return false;
                }
                return true;
            }
            case 2: {
                return this.getFound(false) <= 1;
            }
            case 3: {
                return this.getFound(false) == 1;
            }
            case 4: {
                return this.getFound(true) >= 1;
            }
            case 6: {
                return true;
            }
            case 5: {
                for (Ingredient i : this.ingredients.values()) {
                    if (i.wasFound(false, true)) continue;
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    int getFound(boolean any) {
        int count = 0;
        for (Ingredient i : this.ingredients.values()) {
            if (!i.wasFound(any, false)) continue;
            ++count;
        }
        return count;
    }

    int getGroupDifficulty() {
        return this.groupDifficulty;
    }

    protected IngredientGroup clone() {
        IngredientGroup ig = new IngredientGroup(this.groupType);
        return ig;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{group:" + this.getGroupTypeName() + "(" + this.getGroupType() + ")");
        boolean first = true;
        for (Ingredient i : this.ingredients.values()) {
            if (first) {
                first = false;
            } else {
                buf.append(",");
            }
            buf.append(i.toString());
        }
        buf.append("}");
        return buf.toString();
    }
}

