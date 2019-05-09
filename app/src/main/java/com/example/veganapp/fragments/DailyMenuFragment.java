package com.example.veganapp.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.PagerTitleStrip;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.veganapp.R;
import com.example.veganapp.custom_adapters.MenuPagerAdapter;
import com.example.veganapp.db_classes.Recipe;
import com.squareup.picasso.Picasso;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_menu, container, false);
        dotsIndicator = view.findViewById(R.id.recipes_menu_dots_indicator);
        viewPager = view.findViewById(R.id.recipes_pager);
        menuTitleStrip = view.findViewById(R.id.recipes_pager_title);
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
                        for (Recipe recipe : recipes) {
                            if (ingestionHashMap.get(recipe.getIngestion()) == null)
                                ingestionHashMap.put(recipe.getIngestion(), new ArrayList<Recipe>());
                            ingestionHashMap.get(recipe.getIngestion()).add(recipe);
                        }
                        dialog.dismiss();
                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                        List<Recipe> recipes = new ArrayList<>();
                        for (int i = 0; i < which + 2; i++) {
                            Recipe recipe = ingestionHashMap.get(ingestionTypes[i]).get((int) (Math.random() * ingestionHashMap.get(ingestionTypes[i]).size()));

                            if (i == 2)
                                recipes.add(1, recipe);
                            else if (i == 3)
                                recipes.add(2, recipe);
                            else
                                recipes.add(recipe);
                        }
                        menuPagerAdapter = new MenuPagerAdapter(recipes, inflater);
                        viewPager.setAdapter(menuPagerAdapter);
                    }

                });
                b.show();
            }
        });
        dotsIndicator.setViewPager(viewPager);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
