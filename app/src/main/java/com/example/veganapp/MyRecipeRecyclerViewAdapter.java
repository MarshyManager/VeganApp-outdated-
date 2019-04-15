package com.example.veganapp;

import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.List;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class MyRecipeRecyclerViewAdapter extends RecyclerView.Adapter<MyRecipeRecyclerViewAdapter.ViewHolder> {

    private final List<JsonClasses.Recipe> recipes;
    private final SupportInterfaces.OnRecipeListFragmentInteractionListener mListener;
    private final SupportInterfaces.OnRecipeLikeFragmentInteractionListener mLikeListener;
    private SharedPreferences shp;


    public MyRecipeRecyclerViewAdapter(List<JsonClasses.Recipe> items, SharedPreferences shp,
                                       SupportInterfaces.OnRecipeListFragmentInteractionListener listener,
                                       SupportInterfaces.OnRecipeLikeFragmentInteractionListener likeListener) {
        recipes = items;
        mListener = listener;
        mLikeListener = likeListener;
        this.shp = shp;
    }

    public void setItems(Collection<JsonClasses.Recipe> tweets) {
        recipes.addAll(tweets);
        notifyDataSetChanged();
    }

    public void clearItems() {
        recipes.clear();
        notifyDataSetChanged();
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
                    Integer rate = Integer.parseInt(holder.mRateNum.getText().toString());
                    if (!shp.getBoolean("recipe_like_" + holder.recipe.getId(), false)) {
                        Picasso.with(view.getContext()).load(R.mipmap.like_activ).into(holder.mDishRating);
                        holder.mRateNum.setText(SupportClasses.StringFormer.formStringValueFromInt(++rate));
                    } else {
                        Picasso.with(view.getContext()).load(R.mipmap.like).into(holder.mDishRating);
                        holder.mRateNum.setText(SupportClasses.StringFormer.formStringValueFromInt(--rate));
                    }
                    holder.recipe.setRate(rate);
                    mLikeListener.onRecipeLikeFragmentInteraction(holder.recipe);
                }
            }
        });
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
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

        JsonClasses.Recipe recipe;

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

        void bind(JsonClasses.Recipe recipe) {
            this.recipe = recipe;
            mNameView.setText(recipe.getName());
            mDishComplexity.setRating(recipe.getComplexity().floatValue());
            mViewsNum.setText(SupportClasses.StringFormer.formStringValueFromInt(recipe.getViews()));
            mRateNum.setText(SupportClasses.StringFormer.formStringValueFromInt(recipe.getRate()));

            if (!shp.getBoolean("recipe_like_" + recipe.getId(), false))
                Picasso.with(mView.getContext()).load(R.mipmap.like).into(mDishRating);
            else
                Picasso.with(mView.getContext()).load(R.mipmap.like_activ).into(mDishRating);

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
