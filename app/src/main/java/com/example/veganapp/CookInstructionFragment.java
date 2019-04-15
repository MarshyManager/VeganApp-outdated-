package com.example.veganapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class CookInstructionFragment extends Fragment {

    protected JsonClasses.Recipe recipe;
    protected TextView mNameView;
    protected TextView mRateNum;
    protected TextView mViewsNum;
    protected TextView mRecipeText;
    protected TextView mIngredientsText;
    protected TextView mIngestionText;
    protected RatingBar mComplexity;
    protected ImageView mDishImage;
    protected ImageView mRateImage;
    protected SharedPreferences shp;
    protected SupportInterfaces.OnRecipeLikeFragmentInteractionListener mLikeListener;


    public CookInstructionFragment() {
    }

    public static CookInstructionFragment newInstance(JsonClasses.Recipe recipe, SharedPreferences shp) {
        CookInstructionFragment fragment = new CookInstructionFragment();

        Bundle args = new Bundle();
        fragment.recipe = recipe;
        fragment.shp = shp;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cook_instruction, container, false);
        mRateNum = v.findViewById(R.id.instr_rate_num);
        mViewsNum = v.findViewById(R.id.instr_views_num);
        mRecipeText = v.findViewById(R.id.instr_recipe);
        mIngredientsText = v.findViewById(R.id.instr_ingredients);
        mComplexity = v.findViewById(R.id.instr_complexity);
        mDishImage = v.findViewById(R.id.instr_image);
        mRateImage = v.findViewById(R.id.instr_rate);
        mNameView = v.findViewById(R.id.instr_name);
        mIngestionText = v.findViewById(R.id.instr_ingestion);

        mNameView.setText(recipe.getName());
        mIngestionText.setText(recipe.getIngestion());
        mComplexity.setRating(recipe.getComplexity().floatValue());
        mViewsNum.setText(SupportClasses.StringFormer.formStringValueFromInt(recipe.getViews()));
        mRateNum.setText(SupportClasses.StringFormer.formStringValueFromInt(recipe.getRate()));
        mIngredientsText.setText(SupportClasses.StringFormer.formIngredientsList(recipe));
        mRecipeText.setText(recipe.getDetail());

        if (!shp.getBoolean("recipe_like_" + recipe.getId(), false))
            Picasso.with(v.getContext()).load(R.mipmap.like).into(mRateImage);
        else
            Picasso.with(v.getContext()).load(R.mipmap.like_activ).into(mRateImage);

        String dishPhotoUrl = recipe.getUrlString();

        Picasso.with(v.getContext()).load(dishPhotoUrl).into(mDishImage);

        mDishImage.setVisibility(dishPhotoUrl != null ? View.VISIBLE : View.GONE);

        mRateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mLikeListener) {
                    Integer rate = Integer.parseInt(mRateNum.getText().toString());
                    if (!shp.getBoolean("recipe_like_" + recipe.getId(), false)) {
                        Picasso.with(view.getContext()).load(R.mipmap.like_activ).into(mRateImage);
                        mRateNum.setText(SupportClasses.StringFormer.formStringValueFromInt(++rate));
                    } else {
                        Picasso.with(view.getContext()).load(R.mipmap.like).into(mRateImage);
                        mRateNum.setText(SupportClasses.StringFormer.formStringValueFromInt(--rate));
                    }
                    recipe.setRate(rate);
                    mLikeListener.onRecipeLikeFragmentInteraction(recipe);
                }
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SupportInterfaces.OnRecipeLikeFragmentInteractionListener) {
            mLikeListener = (SupportInterfaces.OnRecipeLikeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
