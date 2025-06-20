package com.tiodev.vegtummy.RoomDB;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipe")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @NonNull
    public String title;

    public String identifier;

    public Integer totalTime;

    public Integer cookTime;
    public Integer prepTime;
    public Integer recipeYield;
    public String recipeYieldText;
    public String difficulty;
    public Integer rating;

    public String category;

    public String keywords;

    public String collections;

    public String devices;


/*    public User(String img, String tittle, String des, String ing, String category) {
        this.img = img;
        this.tittle = tittle;
        this.des = des;
        this.ing = ing;
        this.category = category;
    }*/

    public User(String identifier, String title,
                Integer totalTime, Integer cookTime, Integer prepTime,
                Integer recipeYield, String difficulty, Integer rating,
                String recipeYieldText, String category, String keywords,
                String collections, String devices) {
        this.identifier = identifier;
        this.title = title;
        this.totalTime = totalTime;
        this.cookTime = cookTime;
        this.prepTime = prepTime;
        this.recipeYield = recipeYield;
        this.difficulty = difficulty;
        this.rating = rating;
        this.recipeYieldText =recipeYieldText;
        this.category = category;
        this.keywords = keywords;
        this.collections = collections;
        this.devices = devices;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(@NonNull String identifier) {
        this.identifier = identifier;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public Integer getCookTime() {
        return cookTime;
    }

    public void setCookTime(Integer cookTime) {
        this.cookTime = cookTime;
    }

    public Integer getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(Integer prepTime) {
        this.prepTime = prepTime;
    }

    public Integer getRecipeYield() {
        return recipeYield;
    }

    public void setRecipeYield(Integer recipeYield) {
        this.recipeYield = recipeYield;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getRecipeYieldText() {
        return recipeYieldText;
    }

    public void setRecipeYieldText(String recipeYieldText) {
        this.recipeYieldText = recipeYieldText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCollections() {
        return collections;
    }

    public void setCollections(String collections) {
        this.collections = collections;
    }

    public String getDevices() {
        return devices;
    }

    public void setDevices(String devices) {
        this.devices = devices;
    }
}

