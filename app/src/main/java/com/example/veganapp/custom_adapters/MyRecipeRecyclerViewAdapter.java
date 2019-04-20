package com.example.veganapp.custom_adapters;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.veganapp.R;
import com.example.veganapp.db_classes.Recipe;
import com.example.veganapp.fragments.RecipesFragment;
import com.example.veganapp.support_classes.StringFormatter;
import com.google.firebase.FirebaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class MyRecipeRecyclerViewAdapter extends RecyclerView.Adapter<MyRecipeRecyclerViewAdapter.ViewHolder> {

    private final List<Recipe> recipes;
    private final RecipesFragment.OnRecipeListFragmentInteractionListener mListener;
    private final RecipesFragment.OnRecipeLikeFragmentInteractionListener mLikeListener;
    private SharedPreferences shp;


    public MyRecipeRecyclerViewAdapter(SharedPreferences shp,
                                       RecipesFragment.OnRecipeListFragmentInteractionListener listener,
                                       RecipesFragment.OnRecipeLikeFragmentInteractionListener likeListener) {
        mListener = listener;
        mLikeListener = likeListener;
        this.shp = shp;
        recipes = new ArrayList<>();
    }


    public void addOrChange(Recipe recipe, int position) {
        if (!recipes.contains(recipe)) {
            recipes.add(recipe);
            notifyItemInserted(position);
        } else {
            int index = recipes.indexOf(recipe);
            recipes.set(index, recipe);
            notifyItemChanged(index);
        }
    }

    public void remove(int position) {
        recipes.remove(position);
        notifyItemRemoved(position);
    }

    public void sort(int i){
        ArrayList<Recipe> temp = new ArrayList<>(recipes);
        switch (i)
        {
            case 0:
                Collections.sort(temp, new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe lhv, Recipe rhv) {
                        return lhv.getId() - rhv.getId();
                    }
                });
                break;
            case 1:
                Collections.sort(temp, new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe lhv, Recipe rhv) {
                        return lhv.getRate() - rhv.getRate();
                    }
                });
                break;
            case 2:
                Collections.sort(temp, new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe lhv, Recipe rhv) {
                        return rhv.getRate() - lhv.getRate();
                    }
                });
                break;
            case 3:
                Collections.sort(temp, new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe lhv, Recipe rhv) {
                        return lhv.getViews() - rhv.getViews();
                    }
                });
                break;
            case 4:
                Collections.sort(temp, new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe lhv, Recipe rhv) {
                        return rhv.getViews() - lhv.getViews();
                    }
                });
                break;
            case 5:
                Collections.sort(temp, new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe lhv, Recipe rhv) {
                        return (int) Math.round(2 * (lhv.getComplexity() - rhv.getComplexity()));
                    }
                });
                break;
            case 6:
                Collections.sort(temp, new Comparator<Recipe>() {
                    @Override
                    public int compare(Recipe lhv, Recipe rhv) {
                        return (int) Math.round(2 * (rhv.getComplexity() - lhv.getComplexity()));
                    }
                });
                break;
            default:
                break;
        }
        CustomDiffUtilCallback cduc = new CustomDiffUtilCallback(recipes, temp);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(cduc, true);
        recipes.clear();
        recipes.addAll(temp);
        diffResult.dispatchUpdatesTo(this);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bind(recipes.get(position));

        holder.mDishRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mLikeListener) {
                    try {
                        mLikeListener.onRecipeLikeFragmentInteraction(holder.recipe);
                        Integer rate = holder.recipe.getRate();
                        if (!shp.getBoolean("recipe_like_" + holder.recipe.getId(), false)) {
                            Picasso.with(view.getContext()).load(R.drawable.like_activ).into(holder.mDishRating);
                            holder.mRateNum.setText(StringFormatter.formStringValueFromInt(++rate));
                        } else {
                            Picasso.with(view.getContext()).load(R.drawable.like).into(holder.mDishRating);
                            holder.mRateNum.setText(StringFormatter.formStringValueFromInt(--rate));
                        }
                        holder.recipe.setRate(rate);
                    } catch (FirebaseException e) {
                        Toast.makeText(view.getContext(), "Error occurred! Connection problem!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    Integer views = holder.recipe.getViews();
                    holder.mViewsNum.setText(StringFormatter.formStringValueFromInt(++views));
                    holder.recipe.setViews(views);
                    mListener.onRecipeListFragmentInteraction(holder.recipe);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mNameView;
        final TextView mViewsNum;
        final ImageView mViewsImage;
        final ImageView mDishImage;
        final RatingBar mDishComplexity;
        final TextView mRateNum;
        final ImageView mDishRating;

        Recipe recipe;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.dish_name);
            mViewsNum = view.findViewById(R.id.dish_views_num);
            mDishImage = view.findViewById(R.id.dish_image);
            mViewsImage = view.findViewById(R.id.dish_views_image);
            mDishComplexity = view.findViewById(R.id.dish_complexity);
            mRateNum = view.findViewById(R.id.dish_rate_num);
            mDishRating = view.findViewById(R.id.dish_rating);
        }

        void bind(Recipe recipe) {
            this.recipe = recipe;
            mNameView.setText(recipe.getName());
            mDishComplexity.setRating(recipe.getComplexity().floatValue());
            mViewsNum.setText(StringFormatter.formStringValueFromInt(recipe.getViews()));
            mRateNum.setText(StringFormatter.formStringValueFromInt(recipe.getRate()));

            if (!shp.getBoolean("recipe_like_" + recipe.getId(), false))
                Picasso.with(mView.getContext()).load(R.drawable.like).into(mDishRating);
            else
                Picasso.with(mView.getContext()).load(R.drawable.like_activ).into(mDishRating);

            String dishPhotoUrl = recipe.getUrlString();

            Picasso.with(mView.getContext()).load(dishPhotoUrl).into(mDishImage);

            mDishImage.setVisibility(dishPhotoUrl != null ? View.VISIBLE : View.GONE);
        }


        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }

    }


}
