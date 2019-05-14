package com.example.veganapp.support_classes;

import com.example.veganapp.db_classes.Ingredient;
import com.example.veganapp.db_classes.Recipe;
import com.google.android.libraries.places.api.model.Period;

import java.util.List;

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

    public static String dishTypes(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        List<String> types = recipe.getType();
        for (int i = 0; i < types.size() - 1; ++i) {
            sb.append(types.get(i));
                sb.append(", ");
        }
        sb.append(types.get(types.size() - 1));
        return sb.toString();
    }

    public static String openHours(Period period)
    {
        StringBuilder sb = new StringBuilder().append(period.getOpen().getTime().getHours());
        if (period.getOpen().getTime().getMinutes() == 0)
            sb.append(":00 - ");
        else if (period.getOpen().getTime().getMinutes() < 10)
            sb.append(":0").append(period.getOpen().getTime().getMinutes()).append(" - ");
        else
            sb.append(":").append(period.getOpen().getTime().getMinutes()).append(" - ");
        sb.append(period.getClose().getTime().getHours());
        if (period.getClose().getTime().getMinutes() == 0)
            sb.append(":00");
        else if (period.getClose().getTime().getMinutes() < 10)
            sb.append(":0").append(period.getOpen().getTime().getMinutes());
        else
            sb.append(":").append(period.getClose().getTime().getMinutes());
        return sb.toString();
    }
}
