package models.recipe;

public class IngredientModel {
    private String ingredient;
    private Integer recipeId;

    public IngredientModel(String ingredient) {
        setIngredient(ingredient);
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public Integer getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Integer recipeId) {
        this.recipeId = recipeId;
    }

}
