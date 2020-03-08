import me.tongfei.progressbar.ProgressBar;
import models.category.CategoryModel;
import models.category.SubcategoryModel;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
/*
- Каждый раз, когда не могу понять что тут происходит,
- счетчик увеличивается на 1
count = 1
*/
class CategoriesParser extends CommonParser {
    private String mainPageUrl = "https://www.say7.info";
    private String newYearPage = "https://www.say7.info/cook/Novogodnie-receptyi/";
    private String newYearPages[] = {
            "https://www.say7.info/cook/page/12-CHto-prigotovit.html",
            "https://www.say7.info/cook/page/1-Novogodnie.html",
            "https://www.say7.info/cook/page/2-Novogodnie.html",
            "https://www.say7.info/cook/page/3-Novogodnie.html",
            "https://www.say7.info/cook/page/4-Novogodnie.html",
            "https://www.say7.info/cook/page/5-Novogodnie.html",
            "https://www.say7.info/cook/page/6-Novogodnie.html",
            "https://www.say7.info/cook/page/8-Novogodnie.html",
            "https://www.say7.info/cook/page/9-Novogodnie.html"
    };

    private String additionalPages[] = {
            "https://www.say7.info/cook/page/7-Paskhalnyie.html",
            "https://www.say7.info/cook/page/10-Receptyi-na-Den.html",
            "https://www.say7.info/cook/page/11-Piknik.html"
    };

    private List<CategoryModel> categoryModels = new ArrayList<>();

    List<CategoryModel> getCategories() {
        System.out.println("========== Сборщик категорий — запущен ======");
        HashSet<String> categoryLinks = new HashSet<>();
        Document doc = Utils.getDocBy(mainPageUrl);
        
        Elements leftMenuWithMainCategories = doc.select("#sidebar > ul > li > a");
        for (Element link : leftMenuWithMainCategories) {
            String ulr = link.absUrl("href");
            if (mainPageUrl.contains(newYearPage)) {
                continue;
            }
            categoryLinks.add(ulr);
            // DEBUG:
//            if (ulr.contains(newYearPage)) {
//                categoryLinks.add(ulr);
//            }
            // DEBUG END
        }

        // DEBUG:
        categoryLinks.addAll(Arrays.asList(additionalPages));
        // DEBUG END

        ProgressBar pb = new ProgressBar("Категории", categoryLinks.size());
        pb.start();
        // Получить верхний уровень категорий
        for(String link: categoryLinks){
            getCategory(link);
            pb.step();
        }
        pb.stop();
        return categoryModels;
    }

    @NotNull
    private CategoryModel getCategory(String url) {
        CategoryModel category = new CategoryModel();
        Document doc = Utils.getDocBy(url);

        category.setId(getID(url));

        String name = doc.title();
        category.setName(name);

        // Рецепты для конкретных праздников и периодов времени
        if (Arrays.asList(additionalPages).stream().anyMatch(str -> str.trim().equals(url))) {
            category = getCategoryForDay(url);
            categoryModels.add(category);
            return category;
        }
        // Рецепты для Нового года
        if (url.contains(newYearPage)) {
            category = getCategoryForNewYear(url);
            categoryModels.add(category);
            return category;
        }


        String description = doc.select("#pagedesc").text().split("Подкатегори")[0];
        category.setDescription(description);

        Integer countRecipe = Utils.getNumbersFrom(description
                .split("Подкатегори")[0]
                .split("Всего в категории ")[1]
                .split("рецепт")[0])[0];
        category.setCountOfRecipes(countRecipe);

        List<SubcategoryModel> subcategoriesModel = new ArrayList<>();
        Elements subcategories = doc.select("#pagedesc > div > ul > li > a");
        // Получаем подкатегории
        for(Element subcategory: subcategories) {
            SubcategoryModel subcategoryModel = new SubcategoryModel();
            subcategoryModel.setId(getID(subcategory.absUrl("href")));
            subcategoryModel.setName(subcategory.text());

            getCategory(subcategory.absUrl("href"));
            subcategoriesModel.add(subcategoryModel);
        }
        category.setSubcategory(subcategoriesModel);

        category.setRecipeId(getIdRecipesBy(getRecipeLinksFromRecipes(url)));

        categoryModels.add(category);
        return category;
    }

    @NotNull
    private CategoryModel getCategoryForNewYear(String url) {
        CategoryModel category = new CategoryModel();
        Document doc = Utils.getDocBy(url);

        category.setId(getID(url));

        String name = doc.title();
        category.setName(name);

        String description = doc.select("#pagedesc").text();
        category.setDescription(description);

        List<Integer> allRecipesIds = new ArrayList<>();

        List<SubcategoryModel> subcategoriesModel = new ArrayList<>();
        Elements subcategories = doc.select("#content > div.c8.lst > ul > li");

        // Получаем подкатегории Нового года
        for(Element subcategoryBlock: subcategories) {
            Elements subcategory = subcategoryBlock.select("a");
            SubcategoryModel subcategoryModel = new SubcategoryModel();
            subcategoryModel.setId(getID(subcategory.first().absUrl("href")));
            subcategoryModel.setName(subcategory.text());

            CategoryModel model = getCategoryForDay(subcategory.first().absUrl("href"));
            subcategoriesModel.add(subcategoryModel);

            allRecipesIds.addAll(model.getRecipeId());
        }
        category.setSubcategory(subcategoriesModel);

        category.setRecipeId(allRecipesIds);
        return category;
    }

    @NotNull
    private CategoryModel getCategoryForDay(String url) {
        CategoryModel category = new CategoryModel();
        Document doc = Utils.getDocBy(url);

        category.setId(getID(url));

        String name = doc.title();
        category.setName(name);
        String description = "";

        Boolean isPageInAdditionals = Arrays.asList(additionalPages).stream().anyMatch(str -> str.trim().equals(url));
        Boolean isPageInNewYearPages = Arrays.asList(newYearPages).stream().anyMatch(str -> str.trim().equals(url));
        if (isPageInAdditionals) {
             description = doc.select("#content > div > p").text();
        } else if (isPageInNewYearPages) {
            description = doc.select("#pagedesc").text();
        }
        category.setDescription(description);
        //countRecipe
        Elements allLinks = doc.select("* > li > a");
        List<String> recipeLinks = new ArrayList<>();
        for (Element link: allLinks) {
            String href = link.absUrl("href");
            if (href.contains("//www.say7.info/cook/recipe/")) {
                recipeLinks.add(href);
            }
        }
        category.setCountOfRecipes(recipeLinks.size());
        category.setRecipeId(getIdRecipesBy(recipeLinks));
        categoryModels.add(category);
        /**
         * Не делаю подкатегории, так как неочевидно пересечение множеств "Салаты" и Салаты из Пикника.
         * Подборку оставляю как категория со списком подходящий рецептов.

         // Получаем подкатегории
         List<SubcategoryModel> subcategoriesModel = new ArrayList<>();
         Elements subcategories = doc.select("#content > div > h2");

         List<List<Node>> subcategoriesArticle = new ArrayList<List<Node>>();
         List<Node> currentSubcategories = new ArrayList<Node>();

         String nameSubcategory = "";
         for(Node node : doc.select("#content > div").get(0).childNodes()) {
         if(node.outerHtml().startsWith("<h2>")) {
         subcategoriesArticle.add(currentSubcategories);
         nameSubcategory = node.childNodes().get(0).toString();
         } else if ((node.outerHtml().startsWith("<div class=\"podium\">"))) {
         List<Node> nodeRecipes = node.childNode(0).childNodes();
         for (Node recipeNode: nodeRecipes) {
         System.out.println(recipeNode);
         }
         }
         currentSubcategories.add(node);
         }

         */
        return category;
    }

    @NotNull
    private List<String> getRecipeLinksFromRecipes(String baseUrl) {
        List<String> recipeLinks = new ArrayList<>();
        Document doc =  Utils.getDocBy(baseUrl);
        Elements links = doc.select("#content > div.lst > ul > li > a");
        for (Element link : links) {
            recipeLinks.add(link.absUrl("href"));
        }
        Integer countRecipe = Utils.getNumbersFrom(
                doc.select("#pagedesc").text()
                        .split("Подкатегори")[0]
                        .split("Всего в категории ")[1]
                        .split("рецепт")[0]
        )[0];

        for (int i = 20; i < countRecipe; i += 20) {
            try {
                doc = Jsoup.connect(baseUrl + "linkz_start-" + i + ".html").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            links  = doc.select("#content > div.lst > ul > li > a");
            for (Element link : links) {
                recipeLinks.add(link.absUrl("href"));
            }
        }

        return recipeLinks;
    }
}
