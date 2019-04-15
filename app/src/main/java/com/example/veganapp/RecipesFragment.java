package com.example.veganapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RecipesFragment extends Fragment {

    // TODO: Customize parameter argument names
    protected static final String ARG_COLUMN_COUNT = "column-count";
    protected static final String ARG_FILTER = "filter";
    // TODO: Customize parameters
    protected int mColumnCount = 1;
    protected OnListFragmentInteractionListener mListener;
    protected OnLikeFragmentInteractionListener mLikeListener;
    protected List<JsonClasses.Recipe> recipes;
    protected List<JsonClasses.Recipe> favRecipes;
    protected SharedPreferences mShp;
    protected MainActivity mParentRef;
    protected boolean filter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static RecipesFragment newInstance(int columnCount, boolean filter) {
        RecipesFragment fragment = new RecipesFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_FILTER, filter);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            filter = getArguments().getBoolean(ARG_FILTER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        mParentRef = (MainActivity) getActivity();
        recipes = mParentRef.getRecipes();
        mShp = mParentRef.getShp();

        favRecipes = new ArrayList<>();
        favRecipes.addAll(recipes);
        if (filter) {
            for (int i = 0; i < favRecipes.size(); i++) {
                if (!mShp.getBoolean("recipe_like_" + favRecipes.get(i).getId(), false))
                    favRecipes.remove(i--);
            }
        }


        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyRecipeRecyclerViewAdapter(favRecipes, mShp, mListener, mLikeListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
            mLikeListener = (OnLikeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(JsonClasses.Recipe item);
    }

    public interface OnLikeFragmentInteractionListener {
        // TODO: Update argument type and name
        void onLikeFragmentInteraction(JsonClasses.Recipe item);
    }
}
