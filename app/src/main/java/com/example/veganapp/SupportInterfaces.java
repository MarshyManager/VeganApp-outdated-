package com.example.veganapp;

public class SupportInterfaces {
    public interface OnRecipeListFragmentInteractionListener {
        void onRecipeListFragmentInteraction(JsonClasses.Recipe item);
    }

    public interface OnRecipeLikeFragmentInteractionListener {
        void onRecipeLikeFragmentInteraction(JsonClasses.Recipe item);
    }
}
