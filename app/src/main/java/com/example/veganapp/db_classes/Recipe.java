package com.example.veganapp.db_classes;

import java.io.Serializable;
import java.util.List;

import androidx.annotation.Nullable;

public class Recipe implements Serializable {

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

    public Recipe() {
    }

    public Recipe(String name, String detail, String ingestion, String urlString, List<Ingredient> ingredients,
                  Integer Id, Integer rate, Integer views, List<String> type, Double complexity) {
        this.name = name;
        this.detail = detail;
        this.ingestion = ingestion;
        this.urlString = urlString;
        this.ingredients = ingredients;
        this.Id = Id;
        this.rate = rate;
        this.views = views;
        this.type = type;
        this.complexity = complexity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Recipe)
            return getId().equals(((Recipe) obj).getId());
        return false;
    }

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
