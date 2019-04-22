package com.example.veganapp.custom_adapters;

import android.content.SharedPreferences;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyRecipeRecyclerViewAdapter extends RecyclerView.Adapter<MyRecipeRecyclerViewAdapter.ViewHolder> {

    private final List<Recipe> recipes;
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
        recipes = new ArrayList<>();
        this.recipesFragment = recipesFragment;
        this.filter = filter;
    }


    public void addOrChange(Recipe recipe, int id) {
        if (!recipes.contains(recipe)) {
            recipes.add(recipe);
            notifyItemInserted(id);
        } else {
            int index = recipes.indexOf(recipe);
            recipes.set(index, recipe);
            notifyItemChanged(index);
        }
    }

    public void remove(Recipe recipe) {
        if (recipes.contains(recipe)) {
            int position = recipes.indexOf(recipe);
            recipes.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void sort(int i) {
        ArrayList<Recipe> temp = new ArrayList<>(recipes);
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
        holder.mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        if (filter) {
            holder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.mSwipeLayout.findViewById(R.id.delete_background));
            holder.mSwipeLayout.addSwipeListener(new SimpleSwipeListener() {

                @Override
                public void onOpen(SwipeLayout layout) {
                    YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
                }
            });
        }
        else {
            holder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Left, holder.mSwipeLayout.findViewById(R.id.fav_background));
            holder.mSwipeLayout.addSwipeListener(new SimpleSwipeListener() {

                @Override
                public void onOpen(SwipeLayout layout) {
                    YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.add_to_fav));
                }
            });
        }

        holder.mYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        holder.mNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(holder.recipe);
                SharedPreferences.Editor editor = shp.edit();
                editor.putBoolean("recipe_fav_" + holder.recipe.getId(), false);
                editor.apply();

            }
        });

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
    public int getItemCount() {
        return recipes.size();
    }


    public List<Recipe> getRecipes() {
        return recipes;
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
        final Button mYesButton;
        final Button mNoButton;

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
            mYesButton = view.findViewById(R.id.restore_yes);
            mNoButton = view.findViewById(R.id.restore_no);
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
