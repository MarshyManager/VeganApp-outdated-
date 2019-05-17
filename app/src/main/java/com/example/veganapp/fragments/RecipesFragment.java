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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Objects;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static com.example.veganapp.support_classes.ConnectionChecker.isOnline;
import static com.example.veganapp.support_classes.ImportantConstants.RECIPES_OFFLINE_LIKE_TAG;
import static com.example.veganapp.support_classes.ImportantConstants.RECIPES_ONLINE_LIKE_TAG;

public class RecipesFragment extends BaseRecipesFragment {

    protected static final String RECIPES_REF = "recipes";

    protected Spinner mSortSpinner;
    protected RecipeRecyclerViewAdapter mRecyclerViewAdapter;
    protected String[] mSortParams;
    protected FirebaseDatabase mFBDB;

    protected Toolbar mToolbar;
    protected SearchView mSearchView;
    protected MenuItem mSearchItem;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    protected OnRecipeLikeFragmentInteractionListener mLikeListener;

    protected int mSortType;
    protected boolean mFilter;

    public RecipesFragment() {
    }

    public static RecipesFragment newInstance(boolean filter) {
        RecipesFragment fragment = new RecipesFragment();
        Bundle args = new Bundle();
        fragment.mFilter = filter;
        fragment.mSortType = 0;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecyclerViewAdapter = new RecipeRecyclerViewAdapter(sharedPreferences, mListListener, mLikeListener, this, mFilter);

        if (isOnline()) {
            mFBDB = FirebaseDatabase.getInstance();
            DatabaseReference recipesRef = mFBDB.getReference().child(RECIPES_REF);
            recipesRef.addListenerForSingleValueEvent(new CustomRecipeValueEventListener());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        mSwipeRefreshLayout = view.findViewById(R.id.refresh_list);

        Context context = view.getContext();
        final RecyclerView recyclerView = view.findViewById(R.id.recipe_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));


        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setAdapter(mRecyclerViewAdapter);

        mSortParams = getResources().getStringArray(R.array.sort_params);

        mSortSpinner = view.findViewById(R.id.sort_spinner);
        final SortSpinnerCustomAdapter spinnerCustomAdapter = new SortSpinnerCustomAdapter(view.getContext(), R.layout.sort_spinner_item, mSortParams);
        spinnerCustomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortSpinner.setAdapter(spinnerCustomAdapter);
        mSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            boolean isFirstTime = true;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isFirstTime) {
                    isFirstTime = false;
                    return;
                }
                mSortType = i;
                mRecyclerViewAdapter.sort(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                writeRecipeList();
                mRecyclerViewAdapter.clear();
                if (isOnline()) {
                    DatabaseReference recipesRef = mFBDB.getReference().child(RECIPES_REF);
                    recipesRef.addListenerForSingleValueEvent(new CustomRecipeValueEventListener());
                } else {
                    readRecipeList();
                    Toast.makeText(getActivity(), R.string.network_problem, Toast.LENGTH_SHORT).show();
                }
                mRecyclerViewAdapter.sort(mSortType);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");

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
                    mRecyclerViewAdapter.getFilter().filter(newText);
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
                if (!isOnline())
                    readRecipeList();
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mListListener = (OnRecipeListFragmentInteractionListener) context;
        mLikeListener = (OnRecipeLikeFragmentInteractionListener) context;

        mPath = context.getApplicationContext().getFilesDir().getPath() + RECIPES_SERIALIZED_PATH;
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

    void readRecipeList() {
        mRecipeList.clear();
        int iter = 0;
        Recipe recipe;
        while (true) {
            if ((recipe = readRecipe(iter++)) != null)
                mRecipeList.add(recipe);
            else
                break;
        }
        mProgressBar.setVisibility(View.GONE);
    }

    Recipe readRecipe(int id) {
        Recipe recipe = null;
        try {
            FileInputStream streamIn = new FileInputStream(mPath + id);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            recipe = (Recipe) objectinputstream.readObject();
            if (mFilter) {
                if (sharedPreferences.getBoolean("recipe_fav_" + recipe.getId(), false))
                    mRecyclerViewAdapter.addOrChange(recipe);
            } else
                mRecyclerViewAdapter.addOrChange(recipe);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return recipe;
    }

    class CustomRecipeValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            mRecipeList.clear();
            String offlineLike;
            String onlineLike;
            String viewsNum;
            for (DataSnapshot unit : dataSnapshot.getChildren()) {
                Recipe recipe = unit.getValue(Recipe.class);
                offlineLike = RECIPES_OFFLINE_LIKE_TAG + recipe.getId();
                onlineLike = RECIPES_ONLINE_LIKE_TAG + recipe.getId();
                viewsNum = "views_dif_" + recipe.getId();
                if (sharedPreferences.getBoolean(offlineLike, false) && !sharedPreferences.getBoolean(onlineLike, false)) {
                    recipe.setRate(recipe.getRate() + 1);
                } else if (!sharedPreferences.getBoolean(offlineLike, false) && sharedPreferences.getBoolean(onlineLike, false)) {
                    recipe.setRate(recipe.getRate() - 1);
                }
                recipe.setViews(recipe.getViews() + sharedPreferences.getInt(viewsNum, 0));
                mFBDB.getReference(RECIPES_REF + "/" + String.valueOf(recipe.getId()) + "/rate").addListenerForSingleValueEvent(new LikeValueChanged(offlineLike, onlineLike, sharedPreferences));
                mFBDB.getReference(RECIPES_REF + "/" + String.valueOf(recipe.getId()) + "/views").addListenerForSingleValueEvent(new ViewsValueChanged(viewsNum, sharedPreferences));

                if (mFilter) {
                    if (sharedPreferences.getBoolean("recipe_fav_" + recipe.getId(), false))
                        mRecyclerViewAdapter.addOrChange(recipe);
                } else mRecyclerViewAdapter.addOrChange(recipe);
                if (mRecipeList.contains(recipe))
                    mRecipeList.set(recipe.getId(), recipe);
                else
                    mRecipeList.add(recipe);
            }
            if (mSortType > 0) {
                mSortSpinner.setSelection(mSortType);
                mRecyclerViewAdapter.sort(mSortType);
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

            tv.setText(mSortParams[position]);
            if (position != 0) {
                int id = position % 2 == 0 ? R.drawable.sort_descending : R.drawable.sort_ascending;
                Picasso.with(this.getContext()).load(id).into(iv);
            }
            return view;
        }
    }


    private void animateSearchToolbar(int numberOfMenuIcon, boolean containsOverflow, boolean show) {
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
