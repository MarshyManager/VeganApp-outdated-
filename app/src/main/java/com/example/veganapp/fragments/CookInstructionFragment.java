package com.example.veganapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.veganapp.R;
import com.example.veganapp.activities.MainActivity;
import com.example.veganapp.db_classes.Recipe;
import com.example.veganapp.support_classes.StringFormatter;
import com.google.firebase.FirebaseException;
import com.squareup.picasso.Picasso;

public class CookInstructionFragment extends Fragment {

    final String RECIPE = "recipe";
    static final String SHARED_PREFERENCES = "shared_preferences";

    protected Recipe recipe;
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
    protected RecipesFragment.OnRecipeLikeFragmentInteractionListener mLikeListener;


    public CookInstructionFragment() {
    }

    public static CookInstructionFragment newInstance(Recipe recipe, String sharedPreferences) {
        CookInstructionFragment fragment = new CookInstructionFragment();

        Bundle args = new Bundle();
        fragment.recipe = recipe;
        args.putString(SHARED_PREFERENCES, sharedPreferences);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            recipe = (Recipe) savedInstanceState.getSerializable(RECIPE);
            shp = getActivity().getSharedPreferences(savedInstanceState.getString(SHARED_PREFERENCES), Context.MODE_PRIVATE);
        }
        shp = getActivity().getSharedPreferences(getArguments().getString(SHARED_PREFERENCES), Context.MODE_PRIVATE);
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
        mViewsNum.setText(StringFormatter.formStringValueFromInt(recipe.getViews()));
        mRateNum.setText(StringFormatter.formStringValueFromInt(recipe.getRate()));
        mIngredientsText.setText(StringFormatter.formIngredientsList(recipe));
        mRecipeText.setText(recipe.getDetail());

        if (!shp.getBoolean("recipe_like_" + recipe.getId(), false))
            Picasso.with(v.getContext()).load(R.drawable.like).into(mRateImage);
        else
            Picasso.with(v.getContext()).load(R.drawable.like_activ).into(mRateImage);

        String dishPhotoUrl = recipe.getUrlString();

        Picasso.with(v.getContext()).load(dishPhotoUrl).into(mDishImage);

        mDishImage.setVisibility(dishPhotoUrl != null ? View.VISIBLE : View.GONE);

        mRateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mLikeListener) {
                    try {
                        mLikeListener.onRecipeLikeFragmentInteraction(recipe);
                        Integer rate = Integer.parseInt(mRateNum.getText().toString());
                        if (!shp.getBoolean("recipe_like_" + recipe.getId(), false)) {
                            Picasso.with(view.getContext()).load(R.drawable.like_activ).into(mRateImage);
                            mRateNum.setText(StringFormatter.formStringValueFromInt(++rate));
                        } else {
                            Picasso.with(view.getContext()).load(R.drawable.like).into(mRateImage);
                            mRateNum.setText(StringFormatter.formStringValueFromInt(--rate));
                        }
                        recipe.setRate(rate);
                    } catch (FirebaseException e) {
                        Toast.makeText(getActivity(), "Error occurred! Connection problem!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(RECIPE, recipe);
        outState.putString(SHARED_PREFERENCES, getArguments().getString(SHARED_PREFERENCES));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RecipesFragment.OnRecipeLikeFragmentInteractionListener) {
            mLikeListener = (RecipesFragment.OnRecipeLikeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
