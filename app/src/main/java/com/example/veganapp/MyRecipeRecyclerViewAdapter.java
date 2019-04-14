package com.example.veganapp;

import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.veganapp.RecipesFragment.OnListFragmentInteractionListener;
import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.List;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class MyRecipeRecyclerViewAdapter extends RecyclerView.Adapter<MyRecipeRecyclerViewAdapter.ViewHolder> {

    private final List<JsonClasses.Recipe> recipes;
    private final OnListFragmentInteractionListener mListener;
    private RecipesFragment.OnLikeFragmentInteractionListener mLikeListener;
    private SharedPreferences mShp;


    public MyRecipeRecyclerViewAdapter(List<JsonClasses.Recipe> items, SharedPreferences shp,
                                       OnListFragmentInteractionListener listener,
                                       RecipesFragment.OnLikeFragmentInteractionListener likeListener) {
        recipes = items;
        mListener = listener;
        mLikeListener = likeListener;
        mShp = shp;
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
                    if (!mShp.getBoolean("recipe_like_" + holder.mItem.getId(), false)) {
                        Picasso.with(view.getContext()).load(R.mipmap.like_activ).into(holder.mDishRating);
                        holder.mRateNum.setText((++rate).toString());
                    } else {
                        Picasso.with(view.getContext()).load(R.mipmap.like).into(holder.mDishRating);
                        holder.mRateNum.setText((--rate).toString());
                    }

                    String s = "recipe_like_" + holder.mItem.getId();
                    SharedPreferences.Editor editor = mShp.edit();
                    if (!mShp.getBoolean(s, false))
                        editor.putBoolean(s, true);
                    else
                        editor.putBoolean(s, false);
                    editor.apply();
                }
            }
        });
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mViewsNum;
        public final ImageView mViewsImage;
        public final ImageView mDishImage;
        public final RatingBar mDishComplexity;
        public final TextView mRateNum;
        public final ImageView mDishRating;

        public JsonClasses.Recipe mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.dish_name);
            mViewsNum = view.findViewById(R.id.views_num);
            mDishImage = view.findViewById(R.id.dish_image);
            mViewsImage = view.findViewById(R.id.views_image);
            mDishComplexity = view.findViewById(R.id.complexity);
            mRateNum = view.findViewById(R.id.rate_num);
            mDishRating = view.findViewById(R.id.dish_rating);
        }

        public void bind(JsonClasses.Recipe recipe) {
            mItem = recipe;
            mNameView.setText(recipe.getName());
            mViewsNum.setText(recipe.getViews().toString());
            mDishComplexity.setRating(recipe.getComplexity().floatValue());
            mRateNum.setText(recipe.getRate().toString());

            if (!mShp.getBoolean("recipe_like_" + recipe.getId(), false))
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
