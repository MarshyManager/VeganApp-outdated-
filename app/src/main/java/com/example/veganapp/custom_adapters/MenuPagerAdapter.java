package com.example.veganapp.custom_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.example.veganapp.R;
import com.example.veganapp.db_classes.Recipe;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MenuPagerAdapter extends PagerAdapter {

    List<Recipe> recipes;
    LayoutInflater inflater;

    public MenuPagerAdapter(List<Recipe> recipes, LayoutInflater inflater) {
        this.recipes = recipes;
        this.inflater = inflater;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return recipes.get(position).getIngestion();
    }

    @Override
    public int getCount() {
        return recipes.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View layout = inflater.inflate(R.layout.recipe_menu_page, null);
        TextView dishName = layout.findViewById(R.id.dish_name);
        ImageView dishImage = layout.findViewById(R.id.dish_image);
        dishName.setText(recipes.get(position).getName());
        String dishPhotoUrl = recipes.get(position).getUrlString();
        Picasso.with(layout.getContext()).load(dishPhotoUrl).into(dishImage);
        dishImage.setVisibility(dishPhotoUrl != null ? View.VISIBLE : View.GONE);
        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
