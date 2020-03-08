import me.tongfei.progressbar.ProgressBar;
import models.recipe.IngredientModel;
import models.recipe.RecipeModel;
import models.recipe.StepModel;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class RecipeParser extends CommonParser {
    private String baseUrl = "https://www.say7.info/cook/";

    List<RecipeModel> getRecipes() {
        List<RecipeModel> recipeModels = new ArrayList<>();

        // Для проверки гипотез разкомментить и  закоментить основноый блок
        //     getRecipeBy("https://www.say7.info/cook/recipe/1055-Pashtet-pod.html");

        List<String> baseLinks = getRecipesLinks();
        ProgressBar pb = new ProgressBar("Рецепты", baseLinks.size());
        pb.start();
        for (String base: baseLinks) {
            recipeModels.add(getRecipeBy(base));
            pb.step();
        }
        pb.stop();

        return recipeModels;
    }

    /**
     * Сбор рецепта со страницы
     * @param url ссылка страницы
     * @return модель рецепта
     */
    private RecipeModel getRecipeBy(String url) {
        Document doc = Utils.getDocBy(url);

        RecipeModel recipe = new RecipeModel();

        Integer id = getID(url);
        recipe.setId(id);
        recipe.setUrl(url);
        recipe.setName(doc.title());

        String servings = doc.select("#content > div.article.h-recipe > div.p-summary > span").text();
        if (!servings.isEmpty()) {
            recipe.setServings(servings);
        }

        String summary = doc.select("#content > div.article.h-recipe > div.p-summary").text();
        recipe.setSummary(summary);

        recipe.setImageRecipe(getMaxSizeImageElement(doc.select("body > div.pc.shi > a > img").first()));

        recipe.setImageIngredients(getMaxSizeImageElement(doc.select("#ingrphoto").first()));

        recipe.setIngredients(getIngredientsListOn(doc));

        String comments = doc.select("#comm > a > span").text();
        if (!comments.isEmpty()) {
            recipe.setCountComments(comments);
        }

        String date = doc.select("#sign > div.col > span > span").text();
        recipe.setCreatedDate(date);

        recipe.setSteps(getStepsListOn(doc));
        StepModel lastStep = recipe.getSteps().get(recipe.getSteps().size()-1);
        // для последнего шага добалвяю картинку рецепта, если ее нет на сайте
        if (lastStep.stepWithoutImage()) {
            lastStep.setImageStep(recipe.getImageRecipe());
            List<StepModel> steps = recipe.getSteps();
            steps.remove(steps.size()-1);
            steps.add(lastStep);
            recipe.setSteps(steps);
        }

        return recipe;
    }

    /**
     * Получение списка шагов
     * @param doc на странице
     * @return список моделей шагов
     */
    @NotNull
    private List<StepModel> getStepsListOn(@NotNull Document doc) {
        List<StepModel> stepModels = new ArrayList<>();
        Elements steps = doc.select("#stp > p");
        Integer stepNum = 1;
        for (Element step : steps) {
            String description = step.text();
            Element imageElement = step.select("img").first();
            String image = "";
            if (imageElement != null) {
                image = getMaxSizeImageElement(step.select("img").first());
            }
            if (description.isEmpty() && image.isEmpty()) {
                continue;
            }

            StepModel stepModel = new StepModel(stepNum.toString(), description, image);
            stepModels.add(stepModel);
            stepNum++;
        }
        Integer countSteps = stepModels.size();
        StepModel lastStep = stepModels.get(countSteps-1);
        StepModel prevStep = stepModels.get(countSteps-2);
        if (
                lastStep.stepDescriptionContains("Приятного аппетита!")
                && lastStep.stepWithoutImage()
                && prevStep.stepWithoutDescription()
        ) {
            stepModels.get(countSteps-2).setDescription("Приятного аппетита!");
            stepModels.remove(countSteps-1);
        }
        return stepModels;
    }

    /**
     * Получение списка ингредиентов
     * @param doc на странице
     * @return список моделей ингредиентов
     */
    @NotNull
    private List<IngredientModel> getIngredientsListOn(@NotNull Document doc) {
        List<IngredientModel> ingredientsList = new ArrayList<>();
        Elements ingredients = doc.select("#content > div.article.h-recipe > div.c8.ingredients > ul > li");
        for (Element ingredient : ingredients) {
            IngredientModel ingredientModel = new IngredientModel(ingredient.text());
            if(!ingredient.select("a").attr("href").isEmpty()) {
                ingredientModel.setRecipeId(getID(ingredient.select("a").attr("href")));
            }
            ingredientsList.add(ingredientModel);
        }
        return ingredientsList;
    }

    /**
     * Получение всех страниц с рецептами
     * @return список ссылок на страницы с рецептами по штучно
     */
    public List<String> getRecipesLinks() {
        System.out.println("======== Сборщик рецептов — запущен =========");
        List<String> recipeLinks = new ArrayList<>();
        Document doc =  Utils.getDocBy(baseUrl);

        Elements links = doc.select("#content > div.lst > ul > li > a");
        for (Element link : links) {
            recipeLinks.add(link.absUrl("href"));
        }
        int maxCountPage = getNumberOfLastPage();
        int countStep = 20;

        ProgressBar pb = new ProgressBar("Ссылки на рецепты", maxCountPage);
        pb.start();
        for (int i = countStep; i < maxCountPage; i += countStep) {
            try {
                doc = Jsoup.connect(baseUrl + "linkz_start-" + i + ".html").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            links  = doc.select("#content > div.lst > ul > li > a");
            for (Element link : links) {
                recipeLinks.add(link.absUrl("href"));
            }
            pb.stepBy(countStep);
        }
        pb.stepBy(20);
        pb.stop();
        System.out.println("=============================================");
        return recipeLinks;
    }

    /**
     * На главной странице получаем элемент ">>",
     * где есть ссылка на последнюю страницу всех рецептов
     * @return номер последней страницы
     */
    private Integer getNumberOfLastPage() {
        Document doc = null;
        try {
            doc = Jsoup.connect(baseUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String rawLink = doc.select("#content > div:nth-child(4) > ul > li.nav-next.nav-last > a")
                .first().absUrl("href")
                .split("-")[1]
                .split("\\.")[0];
        return Utils.getNumbersFrom(rawLink)[0];
    }
}