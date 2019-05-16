package com.example.veganapp.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.widget.SearchView;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.veganapp.activities.MainActivity;
import com.example.veganapp.custom_adapters.RecipeRecyclerViewAdapter;
import com.example.veganapp.R;
import com.example.veganapp.db_classes.Recipe;
import com.example.veganapp.support_classes.LikeValueChanged;
import com.example.veganapp.support_classes.ViewsValueChanged;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class RecipesFragment extends Fragment {

    protected static final String ARG_FILTER = "filter";
    protected static final String RECIPES = "recipes";
    protected static final String SHARED_PREFERENCES = "shared_preferences";
    protected static final String SORT_TYPE = "sort_type";
    protected static final String RECIPES_SER = "/recipes_ser";
    protected static final String RECIPES_OFFLINE_LIKE = "recipe_offline_like_";
    protected static final String RECIPES_ONLINE_LIKE = "recipe_online_like_";

    List<Recipe> recipes;
    protected OnRecipeListFragmentInteractionListener mListListener;
    protected OnRecipeLikeFragmentInteractionListener mLikeListener;
    protected SharedPreferences shp;
    protected Spinner sortSpinner;
    protected ProgressBar mProgressBar;
    protected boolean filter;
    protected RecipeRecyclerViewAdapter recyclerViewAdapter;
    protected String[] sortParams;
    protected int sortType;
    protected String path;
    protected boolean isOnline;
    FirebaseDatabase db;
    protected Toolbar mToolbar;
    protected SearchView mSearchView;
    private MenuItem mSearchItem;

    public RecipesFragment() {
    }

    public static RecipesFragment newInstance(String sharedPreferences, boolean filter) {
        RecipesFragment fragment = new RecipesFragment();
        Bundle args = new Bundle();
        args.putInt(SORT_TYPE, 0);
        args.putBoolean(ARG_FILTER, filter);
        args.putString(SHARED_PREFERENCES, sharedPreferences);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isOnline = isOnline();
        recipes = new ArrayList<>();
        if (getArguments() != null) {
            filter = getArguments().getBoolean(ARG_FILTER);
            sortType = getArguments().getInt(SORT_TYPE);
            shp = getActivity().getSharedPreferences(getArguments().getString(SHARED_PREFERENCES), Context.MODE_PRIVATE);
        }
        if (savedInstanceState != null) {
            filter = savedInstanceState.getBoolean(ARG_FILTER);
            sortType = savedInstanceState.getInt(SORT_TYPE);
            shp = getActivity().getSharedPreferences(savedInstanceState.getString(SHARED_PREFERENCES), Context.MODE_PRIVATE);
        }


        path = getActivity().getApplicationContext().getFilesDir().getPath() + RECIPES_SER;

        recyclerViewAdapter = new RecipeRecyclerViewAdapter(shp, mListListener, mLikeListener, this, filter);

        if (isOnline) {
            db = FirebaseDatabase.getInstance();
            DatabaseReference recipesRef = db.getReference().child(RECIPES);
            recipesRef.addListenerForSingleValueEvent(new CustomRecipeValueEventListener());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);
        final SwipeRefreshLayout srl = view.findViewById(R.id.refresh_list);

        mToolbar = view.findViewById(R.id.toolbar_recipe_list);
        mProgressBar = Objects.requireNonNull(getActivity()).findViewById(R.id.load_data);

        Context context = view.getContext();
        final RecyclerView recyclerView = view.findViewById(R.id.recipe_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));


        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setAdapter(recyclerViewAdapter);

        sortParams = getResources().getStringArray(R.array.sort_params);

        sortSpinner = view.findViewById(R.id.sort_spinner);
        final SortSpinnerCustomAdapter spinnerCustomAdapter = new SortSpinnerCustomAdapter(view.getContext(), R.layout.sort_spinner_item, sortParams);
        spinnerCustomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerCustomAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            boolean isFirstTime = true;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isFirstTime) {
                    isFirstTime = false;
                    return;
                }
                sortType = i;
                recyclerViewAdapter.sort(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isOnline = isOnline();
                writeRecipeList();
                recyclerViewAdapter.clear();
                if (isOnline) {
                    DatabaseReference recipesRef = db.getReference().child(RECIPES);
                    recipesRef.addListenerForSingleValueEvent(new CustomRecipeValueEventListener());
                } else {
                    readRecipeList();
                    Toast.makeText(getActivity(), R.string.network_problem, Toast.LENGTH_SHORT).show();
                }
                recyclerViewAdapter.sort(sortType);
                srl.setRefreshing(false);
            }
        });
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");

        /*      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                }
            });
        }*/

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu_recipe_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
        mSearchItem = menu.findItem(R.id.options_search);
        mSearchView = (SearchView) mSearchItem.getActionView();
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    onQueryTextChange(query);
                    MainActivity.hideKeyboard(getActivity());
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    recyclerViewAdapter.getFilter().filter(newText);
                    return true;
                }
            });
        }
        final SwipeRefreshLayout srl = getView().findViewById(R.id.refresh_list);
        mSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                animateSearchToolbar(1, true, true);
                srl.setEnabled(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                if (menuItem.isActionViewExpanded()) {
                    animateSearchToolbar(1, false, false);
                    srl.setEnabled(true);
                }
                return true;
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        Objects.requireNonNull(getView()).post(new Runnable() {
            @Override
            public void run() {
                if (!isOnline)
                    readRecipeList();
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if ((context instanceof OnRecipeListFragmentInteractionListener) && (context instanceof OnRecipeLikeFragmentInteractionListener)) {
            mListListener = (OnRecipeListFragmentInteractionListener) context;
            mLikeListener = (OnRecipeLikeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        outState.putBoolean(ARG_FILTER, filter);
        outState.putString(SHARED_PREFERENCES, getArguments().getString(SHARED_PREFERENCES));
        outState.putInt(SORT_TYPE, sortType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListListener = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeRecipeList();
            }
        });
    }

    void writeRecipeList() {
        for (Recipe recipe : recipes) {
            writeRecipe(recipe, recipe.getId());
        }
    }

    void writeRecipe(Recipe recipe, int id) {
        try {
            FileOutputStream fout = new FileOutputStream(path + id);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(recipe);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readRecipeList() {
        recipes.clear();
        int iter = 0;
        Recipe recipe;
        while (true) {
            if ((recipe = readRecipe(iter++)) != null)
                recipes.add(recipe);
            else
                break;
        }
        mProgressBar.setVisibility(View.GONE);
    }

    Recipe readRecipe(int id) {
        Recipe recipe = null;
        try {
            FileInputStream streamIn = new FileInputStream(path + id);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            recipe = (Recipe) objectinputstream.readObject();
            if (filter) {
                if (shp.getBoolean("recipe_fav_" + recipe.getId(), false))
                    recyclerViewAdapter.addOrChange(recipe);
            } else
                recyclerViewAdapter.addOrChange(recipe);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return recipe;
    }

    public interface OnRecipeListFragmentInteractionListener {
        void onRecipeListFragmentInteraction(Recipe item, RecipesFragment recipesFragment);
    }

    public interface OnRecipeLikeFragmentInteractionListener {
        void onRecipeLikeFragmentInteraction(Recipe item);
    }

    class CustomRecipeValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            recipes.clear();
            String offlineLike;
            String onlineLike;
            String viewsNum;
            for (DataSnapshot unit : dataSnapshot.getChildren()) {
                Recipe recipe = unit.getValue(Recipe.class);
                offlineLike = RECIPES_OFFLINE_LIKE + recipe.getId();
                onlineLike = RECIPES_ONLINE_LIKE + recipe.getId();
                viewsNum = "views_dif_" + recipe.getId();
                if (shp.getBoolean(offlineLike, false) && !shp.getBoolean(onlineLike, false)) {
                    recipe.setRate(recipe.getRate() + 1);
                } else if (!shp.getBoolean(offlineLike, false) && shp.getBoolean(onlineLike, false)) {
                    recipe.setRate(recipe.getRate() - 1);
                }
                recipe.setViews(recipe.getViews() + shp.getInt(viewsNum, 0));
                db.getReference(RECIPES + "/" + String.valueOf(recipe.getId()) + "/rate").addListenerForSingleValueEvent(new LikeValueChanged(offlineLike, onlineLike, shp));
                db.getReference(RECIPES + "/" + String.valueOf(recipe.getId()) + "/views").addListenerForSingleValueEvent(new ViewsValueChanged(viewsNum, shp));

                if (filter) {
                    if (shp.getBoolean("recipe_fav_" + recipe.getId(), false))
                        recyclerViewAdapter.addOrChange(recipe);
                } else recyclerViewAdapter.addOrChange(recipe);
                if (recipes.contains(recipe))
                    recipes.set(recipe.getId(), recipe);
                else
                    recipes.add(recipe);
            }
            if (sortType > 0) {
                sortSpinner.setSelection(sortType);
                recyclerViewAdapter.sort(sortType);
            }
            mProgressBar.setVisibility(View.GONE);

            writeRecipeList();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d("Error", "Failed to read value.");
        }
    }

    public class SortSpinnerCustomAdapter extends ArrayAdapter<String> {

        SortSpinnerCustomAdapter(Context context, int textViewResourceId,
                                 String[] objects) {
            super(context, textViewResourceId, objects);

        }


        @Override
        public View getDropDownView(int position, View convertView,
                                    @NotNull ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, @NotNull ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.sort_spinner_item, parent, false);

            ImageView iv = view.findViewById(R.id.sort_type);
            TextView tv = view.findViewById(R.id.sort_param_name);

            tv.setText(sortParams[position]);
            if (position != 0) {
                int id = position % 2 == 0 ? R.drawable.sort_descending : R.drawable.sort_ascending;
                Picasso.with(this.getContext()).load(id).into(iv);
            }
            return view;
        }
    }


    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void animateSearchToolbar(int numberOfMenuIcon, boolean containsOverflow, boolean show) {
        mToolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.quantum_grey_600));

        if (show) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = mToolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(mToolbar,
                        isRtl(getResources()) ? mToolbar.getWidth() - width : width, mToolbar.getHeight() / 2, 0.0f, (float) width);
                createCircularReveal.setDuration(250);
                createCircularReveal.start();
            } else {
                TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-mToolbar.getHeight()), 0.0f);
                translateAnimation.setDuration(220);
                mToolbar.clearAnimation();
                mToolbar.startAnimation(translateAnimation);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = mToolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(mToolbar,
                        isRtl(getResources()) ? mToolbar.getWidth() - width : width, mToolbar.getHeight() / 2, (float) width, 0.0f);
                createCircularReveal.setDuration(250);
                createCircularReveal.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (getActivity() != null) {
                            mToolbar.setBackgroundColor(getThemeColor(getActivity(), R.color.color_primary));
                            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.color_primary_dark));
                        }
                    }
                });
                createCircularReveal.start();
            } else {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                Animation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-mToolbar.getHeight()));
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(alphaAnimation);
                animationSet.addAnimation(translateAnimation);
                animationSet.setDuration(220);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.color_primary));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mToolbar.startAnimation(animationSet);
            }
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.color_primary_dark));
        }
    }

    private boolean isRtl(Resources resources) {
        return resources.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private static int getThemeColor(Context context, int id) {
        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }
}
