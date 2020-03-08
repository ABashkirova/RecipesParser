package models.category;

import java.util.List;

public class CategoryModel {
    private Integer id;
    private String name;
    private String description;
    private Integer countOfRecipes;
    private List<Integer> recipeId;
    private List<SubcategoryModel> subcategory;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Integer> getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(List<Integer> recipeId) {
        this.recipeId = recipeId;
    }


    public Integer getCountOfRecipes() {
        return countOfRecipes;
    }

    public void setCountOfRecipes(Integer countOfRecipes) {
        this.countOfRecipes = countOfRecipes;
    }


    public List<SubcategoryModel> getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(List<SubcategoryModel> subcategory) {
        this.subcategory = subcategory;
    }
}

