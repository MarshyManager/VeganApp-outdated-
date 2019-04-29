package com.example.veganapp.custom_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.veganapp.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> implements Filterable {

    List<String> ingredients;
    List<String> ingredientsFiltered;

    public IngredientAdapter(List<String> ingredients)
    {
        this.ingredients = ingredients;
        this.ingredientsFiltered = ingredients;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_ingridient, parent, false);
        return new IngredientAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(ingredientsFiltered.get(position));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                    List<String> filteredList = new ArrayList<>();
                    for (String row : ingredients) {

                        if (row.toLowerCase().contains(charString.toLowerCase())) {
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
                ingredientsFiltered = (ArrayList<String>) filterResults.values;

                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView textView;

        String name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            textView = itemView.findViewById(R.id.ingredient_name_search);

        }

        void bind(String ingredientName)
        {
            textView.setText(ingredientName);
        }
    }
}
