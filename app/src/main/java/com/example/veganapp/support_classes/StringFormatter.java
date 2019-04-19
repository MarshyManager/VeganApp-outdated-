package com.example.veganapp.support_classes;

import com.example.veganapp.db_classes.Ingredient;
import com.example.veganapp.db_classes.Recipe;

public class StringFormatter {

    private static final int ONE_THOUSAND = 1000;
    private static final int ONE_MILLION = 1000000;

    public static String formStringValueFromInt(int value) {
        if (value < ONE_THOUSAND)
            return String.valueOf(value);
        if (value < ONE_MILLION)
            return String.valueOf(value / ONE_THOUSAND) + "K";
        return String.valueOf(value / ONE_MILLION) + "M";
    }

    public static String formIngredientsList(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        for (Ingredient ingredient : recipe.getIngredients()) {
            sb.append(ingredient.getName());
            if (ingredient.getAmount() != null)
                sb.append(" - ").append(ingredient.getAmount());
            sb.append("\n");
        }
        return sb.toString();
    }
}
