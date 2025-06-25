package com.tiodev.vegtummy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FavoriteList {
    private String id;
    private String name;
    private List<String> recipeIds;

    public FavoriteList(String id, String name) {
        this.id = id;
        this.name = name;
        this.recipeIds = new ArrayList<>();
    }

    public FavoriteList(String id, String name, List<String> recipeIds) {
        this.id = id;
        this.name = name;
        this.recipeIds = new ArrayList<>(recipeIds); // Create a new list to avoid external modification
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRecipeIds() {
        // Return a copy to prevent external modification of the internal list
        return new ArrayList<>(recipeIds);
    }

    // Package-private for modification only by FavoriteListManager
    void addRecipeId(String recipeId) {
        if (recipeId != null && !recipeId.isEmpty() && !this.recipeIds.contains(recipeId)) {
            this.recipeIds.add(recipeId);
        }
    }

    // Package-private for modification only by FavoriteListManager
    void removeRecipeId(String recipeId) {
        if (recipeId != null) {
            this.recipeIds.remove(recipeId);
        }
    }

    // Internal use by manager to get mutable list
    List<String> getModifiableRecipeIds() {
        return this.recipeIds;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteList that = (FavoriteList) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FavoriteList{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", recipeIds=" + recipeIds.size() + " recipes" +
                '}';
    }
}
