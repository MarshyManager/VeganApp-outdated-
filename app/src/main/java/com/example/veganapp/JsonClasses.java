package com.example.veganapp;

import java.util.List;

public class JsonClasses {

    public class MainJson {

        private List<Recipe> recipes = null;
        private List<Restaurant> restaurants = null;

        public List<Recipe> getRecipes() {
            return recipes;
        }

        public void setRecipes(List<Recipe> recipes) {
            this.recipes = recipes;
        }

        public List<Restaurant> getRestaurants() {
            return restaurants;
        }

        public void setRestaurants(List<Restaurant> restaurants) {
            this.restaurants = restaurants;
        }
    }

    public class Ingredient {

        private String amount;
        private String name;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public class Recipe {

        private Integer Id;
        private Double complexity;
        private String detail;
        private String ingestion;
        private List<Ingredient> ingredients = null;
        private String name;
        private Integer rate;
        private List<String> type = null;
        private String urlString;
        private Integer views;

        public Integer getId() {
            return Id;
        }

        public void setId(Integer id) {
            this.Id = id;
        }

        public Double getComplexity() {
            return complexity;
        }

        public void setComplexity(Double complexity) {
            this.complexity = complexity;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getIngestion() {
            return ingestion;
        }

        public void setIngestion(String ingestion) {
            this.ingestion = ingestion;
        }

        public List<Ingredient> getIngredients() {
            return ingredients;
        }

        public void setIngredients(List<Ingredient> ingredients) {
            this.ingredients = ingredients;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getRate() {
            return rate;
        }

        public void setRate(Integer rate) {
            this.rate = rate;
        }

        public List<String> getType() {
            return type;
        }

        public void setType(List<String> type) {
            this.type = type;
        }

        public String getUrlString() {
            return urlString;
        }

        public void setUrlString(String urlString) {
            this.urlString = urlString;
        }

        public Integer getViews() {
            return views;
        }

        public void setViews(Integer views) {
            this.views = views;
        }
    }

    public class Restaurant {

        private String id;
        private String name;
        private String type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
