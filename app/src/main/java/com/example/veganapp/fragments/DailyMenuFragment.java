package com.example.veganapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.PagerTitleStrip;
import androidx.viewpager.widget.ViewPager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.veganapp.R;
import com.example.veganapp.custom_adapters.MenuPagerAdapter;
import com.example.veganapp.db_classes.Recipe;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class DailyMenuFragment extends RecipesFragment {

    ViewPager viewPager;
    SpringDotsIndicator dotsIndicator;
    PagerTitleStrip menuTitleStrip;
    MenuPagerAdapter menuPagerAdapter;
    TextView instruction;
    List<Recipe> chosenRecipes;

    public DailyMenuFragment() {
    }


    public static DailyMenuFragment newInstance() {
        DailyMenuFragment fragment = new DailyMenuFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chosenRecipes = new ArrayList<>();
        menuPagerAdapter = new MenuPagerAdapter(chosenRecipes, this, mListListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_menu, container, false);

        menuPagerAdapter.setInflater(inflater);
        instruction = view.findViewById(R.id.create_menu_instruction);

        viewPager = view.findViewById(R.id.recipes_pager);
        viewPager.setAdapter(menuPagerAdapter);

        dotsIndicator = view.findViewById(R.id.recipes_menu_dots_indicator);
        dotsIndicator.setViewPager(viewPager);

        showDotsIndicator(menuPagerAdapter.getCount());

        menuTitleStrip = view.findViewById(R.id.recipes_pager_title);
        menuTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mProgressBar = Objects.requireNonNull(getActivity()).findViewById(R.id.load_data);
        Button button = view.findViewById(R.id.create_menu_button);
        final String[] ingestionTypes = {"Завтрак", "Перекус", "Обед", "Десерт"};
        readRecipeList();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle("Choose diet (number of meals)");
                String[] types = {"2", "3", "4"};
                b.setItems(types, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, List<Recipe>> ingestionHashMap = new HashMap<>();
                        chosenRecipes.clear();
                        for (Recipe recipe : recipes) {
                            if (ingestionHashMap.get(recipe.getIngestion()) == null)
                                ingestionHashMap.put(recipe.getIngestion(), new ArrayList<Recipe>());
                            ingestionHashMap.get(recipe.getIngestion()).add(recipe);
                        }
                        dialog.dismiss();
                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                        for (int i = 0; i < which + 2; i++) {
                            Recipe recipe = ingestionHashMap.get(ingestionTypes[i]).get((int) (Math.random() * ingestionHashMap.get(ingestionTypes[i]).size()));

                            if (i == 2)
                                chosenRecipes.add(1, recipe);
                            else if (i == 3)
                                chosenRecipes.add(2, recipe);
                            else
                                chosenRecipes.add(recipe);
                        }
                        showDotsIndicator(menuPagerAdapter.getCount());
                        menuPagerAdapter.notifyDataSetChanged();
                    }

                });
                b.show();
            }
        });
        return view;
    }

    private void showDotsIndicator(int itemCount)
    {
        if (itemCount > 0) {
            dotsIndicator.setVisibility(View.VISIBLE);
            instruction.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecipeListFragmentInteractionListener) {
            mListListener = (OnRecipeListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
}
