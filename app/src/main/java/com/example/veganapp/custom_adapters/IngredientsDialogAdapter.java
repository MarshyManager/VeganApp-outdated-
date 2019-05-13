package com.example.veganapp.custom_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.veganapp.R;
import com.example.veganapp.db_classes.Ingredient;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IngredientsDialogAdapter extends RecyclerView.Adapter<IngredientsDialogAdapter.ViewHolder> implements Filterable {

    private List<Ingredient> ingredients;
    private List<Ingredient> ingredientsFiltered;
    private ChosenIngredientsAdapter chosenIngredientsAdapter;
    private final Button findRecipes;
    boolean addType;

    public IngredientsDialogAdapter(List<Ingredient> ingredients, ChosenIngredientsAdapter chosenIngredientsAdapter,
                                    Button findRecipes, boolean addType) {
        this.ingredients = ingredients;
        this.ingredientsFiltered = ingredients;
        this.chosenIngredientsAdapter = chosenIngredientsAdapter;
        this.findRecipes = findRecipes;
        this.addType = addType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredient_dialog_item, parent, false);
        return new IngredientsDialogAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bind(ingredientsFiltered.get(position));
        holder.mView.setEnabled(holder.enabled);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chosenIngredientsAdapter.addOrChange(holder.ingredient, addType);
                findRecipes.setEnabled(true);
                holder.mView.setEnabled(holder.enabled = false);
            }
        });
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    ingredientsFiltered = ingredients;
                } else {
                    List<Ingredient> filteredList = new ArrayList<>();
                    for (Ingredient row : ingredients) {

                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    ingredientsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = ingredientsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                ingredientsFiltered = (ArrayList<Ingredient>) filterResults.values;

                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return ingredientsFiltered.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView textView;
        Ingredient ingredient;
        Boolean enabled = true;
        Boolean addType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            textView = itemView.findViewById(R.id.ingredient_name_search);
        }

        void bind(Ingredient ingredient) {
            this.ingredient = ingredient;
            textView.setText(ingredient.getName());
            enabled = !chosenIngredientsAdapter.contains(ingredient);
        }
    }
}
