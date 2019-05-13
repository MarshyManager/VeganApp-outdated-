package com.example.veganapp.custom_adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.veganapp.R;
import com.example.veganapp.db_classes.Ingredient;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ChosenIngredientsAdapter extends RecyclerView.Adapter<ChosenIngredientsAdapter.ViewHolder> {

    private final List<Ingredient> ingredients;
    private final Button findRecipes;
    private final TextView textView;


    public ChosenIngredientsAdapter(Button findRecipes, TextView textView) {
        ingredients = new CopyOnWriteArrayList<>();
        this.findRecipes = findRecipes;
        this.textView = textView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_ingridient_item, parent, false);
        return new ViewHolder(view);
    }

    public boolean contains(Ingredient ingredient) {
        return ingredients.contains(ingredient);
    }

    public void insert(Ingredient ingredient, int pos) {
        if (pos >= ingredients.size())
            pos = ingredients.size() - 1;
        ingredients.add(pos, ingredient);
        notifyItemInserted(pos);
    }

    public void addOrChange(Ingredient ingredient) {
        if (!ingredients.contains(ingredient)) {
            ingredients.add(ingredient);
            notifyItemInserted(ingredients.size() - 1);
        } else {
            int index = ingredients.indexOf(ingredient);
            ingredients.set(index, ingredient);
            notifyItemChanged(index);
        }
        textView.setVisibility(View.GONE);
    }

    public void remove(Ingredient ingredient) {
        int pos;
        if ((pos = ingredients.indexOf(ingredient)) >= 0) {
            ingredients.remove(ingredient);
            notifyItemRemoved(pos);
        }
        if (ingredients.size() == 0) {
            findRecipes.setEnabled(false);
            textView.setVisibility(View.VISIBLE);
        }
    }

    public void removeAt(int pos) {
        ingredients.remove(pos);
        notifyItemRemoved(pos);
        if (ingredients.size() == 0)
            findRecipes.setEnabled(false);
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.ingredient = ingredients.get(position);
        holder.mNameView.setText(ingredients.get(position).getName());
        holder.mRemoveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(holder.ingredient);
            }
        });
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.ingredient);
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final ImageView mRemoveView;
        public Ingredient ingredient;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.ingredient_name);
            mRemoveView = view.findViewById(R.id.remove_ingredient);
        }
    }
}
