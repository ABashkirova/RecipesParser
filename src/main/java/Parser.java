import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Parser {
    private static String recipeJson = "recipes-dirty.json";
    private static String categoryJson = "categories.json";

    public static void main(String[] argv){
        writeRecipes();
        writeCategories();
    }

    private static void writeRecipes() {
        System.out.println("=============================================");
        String json = jsonString(new RecipeParser().getRecipes());
        try {
            whenWriteStringUsingBufferedWritter_thenCorrect(recipeJson, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("============ Записаны рецепты ===============");
    }

    public static void writeCategories() {
        System.out.println("=============================================");
        String json = jsonString(new CategoriesParser().getCategories());
        try {
            whenWriteStringUsingBufferedWritter_thenCorrect(categoryJson, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("============== Записаны категории ===========");
    }

    private static String jsonString(List list){
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    private static void whenWriteStringUsingBufferedWritter_thenCorrect(String fileName, String json)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(System.getProperty("user.dir")+ "/target/" + fileName)
        );
        writer.write(json);
        writer.close();
    }
}
