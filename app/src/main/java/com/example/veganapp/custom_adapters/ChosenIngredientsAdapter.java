package com.example.veganapp.custom_adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.veganapp.R;
import com.example.veganapp.db_classes.Ingredient;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ChosenIngredientsAdapter extends RecyclerView.Adapter<ChosenIngredientsAdapter.ViewHolder> {

    private final List<Ingredient> ingredients;
    private final List<Boolean> addTypeList;
    private Button findRecipes;
    private TextView textView;


    public ChosenIngredientsAdapter(Button findRecipes, TextView textView) {
        addTypeList = new CopyOnWriteArrayList<>();
        ingredients = new CopyOnWriteArrayList<>();
        this.findRecipes = findRecipes;
        this.textView = textView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_ingredient_item, parent, false);
        return new ViewHolder(view);
    }

    public void clear() {
        ingredients.clear();
        addTypeList.clear();
        textView.setVisibility(View.VISIBLE);
        findRecipes.setEnabled(false);
        notifyDataSetChanged();
    }

    public boolean contains(Ingredient ingredient) {
        return ingredients.contains(ingredient);
    }

    public void insert(Ingredient ingredient, int pos, boolean addType) {
        if (pos >= ingredients.size())
            pos = ingredients.size() - 1;
        ingredients.add(pos, ingredient);
        addTypeList.add(addType);
        notifyItemInserted(pos);
    }

    public void addOrChange(Ingredient ingredient, boolean addType) {
        if (!ingredients.contains(ingredient)) {
            ingredients.add(ingredient);
            addTypeList.add(addType);
            notifyItemInserted(ingredients.size() - 1);
        } else {
            int index = ingredients.indexOf(ingredient);
            ingredients.set(index, ingredient);
            addTypeList.set(index, addType);
            notifyItemChanged(index);
        }
        textView.setVisibility(View.GONE);
    }

    public void remove(Ingredient ingredient) {
        int pos;
        if ((pos = ingredients.indexOf(ingredient)) >= 0) {
            ingredients.remove(ingredient);
            addTypeList.remove(pos);
            notifyItemRemoved(pos);
        }
        if (ingredients.size() == 0) {
            findRecipes.setEnabled(false);
            textView.setVisibility(View.VISIBLE);
        }
    }

    public void removeAt(int pos) {
        ingredients.remove(pos);
        addTypeList.remove(pos);
        notifyItemRemoved(pos);
        if (ingredients.size() == 0)
            findRecipes.setEnabled(false);
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public void setFindRecipes(Button findRecipes) {
        this.findRecipes = findRecipes;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<Boolean> getAddTypeList() {
        return addTypeList;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.ingredient = ingredients.get(position);

        if (addTypeList.get(position))
            holder.mView.setBackgroundResource(R.color.color_primary_dark);
        else
            holder.mView.setBackgroundResource(R.color.red);

        holder.mNameView.setText(ingredients.get(position).getName());
        holder.mRemoveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(holder.ingredient);
            }
        });
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
