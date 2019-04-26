package com.example.veganapp.custom_adapters;

import android.content.SharedPreferences;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.veganapp.R;
import com.example.veganapp.db_classes.Recipe;
import com.example.veganapp.fragments.RecipesFragment;
import com.example.veganapp.support_classes.StringFormatter;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyRecipeRecyclerViewAdapter extends RecyclerView.Adapter<MyRecipeRecyclerViewAdapter.ViewHolder> implements Filterable {

    private final CopyOnWriteArrayList<Recipe> recipes;
    private CopyOnWriteArrayList<Recipe> recipesFiltered;
    private final RecipesFragment.OnRecipeListFragmentInteractionListener mListener;
    private final RecipesFragment.OnRecipeLikeFragmentInteractionListener mLikeListener;
    private SharedPreferences shp;
    private RecipesFragment recipesFragment;
    boolean filter;


    public MyRecipeRecyclerViewAdapter(SharedPreferences shp,
                                       RecipesFragment.OnRecipeListFragmentInteractionListener listener,
                                       RecipesFragment.OnRecipeLikeFragmentInteractionListener likeListener,
                                       RecipesFragment recipesFragment, boolean filter) {
        mListener = listener;
        mLikeListener = likeListener;
        this.shp = shp;
        recipes = new CopyOnWriteArrayList<>();
        recipesFiltered = new CopyOnWriteArrayList<>();
        this.recipesFragment = recipesFragment;
        this.filter = filter;
    }
    public void insert(Recipe recipe, int pos) {
        if (pos >= recipesFiltered.size())
            pos = recipesFiltered.size() - 1;
        recipes.add(pos, recipe);
        recipesFiltered.add(pos, recipe);
        notifyItemInserted(pos);
    }

    public void addOrChange(Recipe recipe) {
        if (!recipes.contains(recipe)) {
            recipes.add(recipe);
            recipesFiltered.add(recipe);
            notifyItemInserted(recipesFiltered.size() - 1);
        } else {
            int index = recipes.indexOf(recipe);
            recipes.set(index, recipe);
            recipesFiltered.add(recipe);
            notifyItemChanged(index);
        }
    }

    public void remove(Recipe recipe) {
        int pos;
        if ((pos = recipes.indexOf(recipe)) >= 0) {
            recipes.remove(recipe);
            recipesFiltered.remove(recipe);
            notifyItemRemoved(pos);
        }
    }

    public void removeAt(int pos) {
        recipes.remove(pos);
        recipesFiltered.remove(pos);
        notifyItemRemoved(pos);
    }

    public void clear(){
        recipes.clear();
        recipesFiltered.clear();
        notifyDataSetChanged();
    }

    public void sort(int i) {
        ArrayList<Recipe> temp = new ArrayList<>(recipesFiltered);
        switch (i) {
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
        CustomDiffUtilCallback cduc = new CustomDiffUtilCallback(recipesFiltered, temp);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(cduc, true);
        recipesFiltered.clear();
        recipesFiltered.addAll(temp);
        diffResult.dispatchUpdatesTo(this);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bind(recipesFiltered.get(position));
        holder.mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        if (filter) {
            holder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.mSwipeLayout.findViewById(R.id.delete_background));
            holder.mSwipeLayout.addSwipeListener(new SimpleSwipeListener() {

                @Override
                public void onOpen(SwipeLayout layout) {
                    final Recipe recipe = holder.recipe;
                    final int pos = holder.getLayoutPosition();
                    remove(recipe);
                    SharedPreferences.Editor editor = shp.edit();
                    editor.putBoolean("recipe_fav_" + holder.recipe.getId(), false);
                    editor.apply();
                    Snackbar.make(recipesFragment.getView(), R.string.recipe_deleted, Snackbar.LENGTH_LONG)
                            .setActionTextColor(0xFF0000FF)
                            .setAnchorView(R.id.navigation)
                            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                            .setAction(R.string.restore, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    insert(recipe, pos);
                                    SharedPreferences.Editor editor = shp.edit();
                                    editor.putBoolean("recipe_fav_" + holder.recipe.getId(), true);
                                    editor.apply();
                                    Snackbar.make(recipesFragment.getView(), R.string.recipe_restored, Snackbar.LENGTH_SHORT)
                                            .setAnchorView(R.id.navigation)
                                            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE).show();
                                }
                            }).show();
                }
            });
        } else {
            holder.mSwipeLayout.addSwipeListener(new SimpleSwipeListener() {

                @Override
                public void onOpen(SwipeLayout layout) {
                    YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.add_to_fav));
                }
            });
        }

        holder.mFavImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = shp.edit();
                if (!shp.getBoolean("recipe_fav_" + holder.recipe.getId(), false)) {
                    Picasso.with(view.getContext()).load(R.drawable.favourite_recipes).into(holder.mFavImage);
                    editor.putBoolean("recipe_fav_" + holder.recipe.getId(), true);
                } else {
                    Picasso.with(view.getContext()).load(R.drawable.favourite_recipes_inact).into(holder.mFavImage);
                    editor.putBoolean("recipe_fav_" + holder.recipe.getId(), false);
                }
                editor.apply();
            }
        });

        holder.mDishRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mLikeListener) {
                    mLikeListener.onRecipeLikeFragmentInteraction(holder.recipe);
                    if (shp.getBoolean("recipe_offline_like_" + holder.recipe.getId(), false)) {
                        Picasso.with(view.getContext()).load(R.drawable.like_activ).into(holder.mDishRating);
                    } else {
                        Picasso.with(view.getContext()).load(R.drawable.like).into(holder.mDishRating);
                    }
                    holder.mRateNum.setText(StringFormatter.formStringValueFromInt(holder.recipe.getRate()));
                }
            }
        });

        holder.mForeGround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    Integer views = holder.recipe.getViews();
                    holder.mViewsNum.setText(StringFormatter.formStringValueFromInt(++views));
                    holder.recipe.setViews(views);
                    mListener.onRecipeListFragmentInteraction(holder.recipe, recipesFragment);
                }
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
                    recipesFiltered = recipes;
                } else {
                    CopyOnWriteArrayList<Recipe> filteredList = new CopyOnWriteArrayList<>();
                    for (Recipe row : recipes) {

                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getIngestion().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    recipesFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = recipesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                recipesFiltered = (CopyOnWriteArrayList<Recipe>) filterResults.values;

                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return recipesFiltered.size();
    }


    public List<Recipe> getRecipes() {
        return recipesFiltered;
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
        final ImageView mFavImage;
        final SwipeLayout mSwipeLayout;
        final RelativeLayout mForeGround;

        Recipe recipe;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mForeGround = view.findViewById(R.id.view_foreground);
            mNameView = view.findViewById(R.id.dish_name);
            mViewsNum = view.findViewById(R.id.dish_views_num);
            mDishImage = view.findViewById(R.id.dish_image);
            mViewsImage = view.findViewById(R.id.dish_views_image);
            mDishComplexity = view.findViewById(R.id.dish_complexity);
            mRateNum = view.findViewById(R.id.dish_rate_num);
            mDishRating = view.findViewById(R.id.dish_rating);
            mFavImage = view.findViewById(R.id.add_to_fav);
            mSwipeLayout = view.findViewById(R.id.swipe);
        }

        void bind(Recipe recipe) {
            this.recipe = recipe;
            mNameView.setText(recipe.getName());
            mDishComplexity.setRating(recipe.getComplexity().floatValue());
            mViewsNum.setText(StringFormatter.formStringValueFromInt(recipe.getViews()));
            mRateNum.setText(StringFormatter.formStringValueFromInt(recipe.getRate()));

            if (!shp.getBoolean("recipe_offline_like_" + recipe.getId(), false))
                Picasso.with(mView.getContext()).load(R.drawable.like).into(mDishRating);
            else
                Picasso.with(mView.getContext()).load(R.drawable.like_activ).into(mDishRating);

            if (!shp.getBoolean("recipe_fav_" + recipe.getId(), false))
                Picasso.with(mView.getContext()).load(R.drawable.favourite_recipes_inact).into(mFavImage);
            else
                Picasso.with(mView.getContext()).load(R.drawable.favourite_recipes).into(mFavImage);

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
