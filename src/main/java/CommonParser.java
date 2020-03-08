import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class CommonParser {
    @NotNull
    static List<Integer> getIdRecipesBy(@NotNull List<String> urls) {
        List<Integer> idRecipes = new ArrayList<>();
        for (String url: urls) {
            idRecipes.add(getID(url));
        }
        return idRecipes;
    }

    static Integer getID(@NotNull String url) {
        String [] splitBase = url.split("/");
        String id = splitBase[splitBase.length-1].split("-")[0];
        Integer i;
        try {
            i = Integer.valueOf(id);
        } catch (NumberFormatException ex) {
            return 0;
        }
        return i;
    }

    static String getMaxSizeImageElement(@NotNull Element imageElement) {
        String image = imageElement.attr("src");
        if (!imageElement.attr("srcset").isEmpty()) {
            String setImagesAttr = imageElement.attr("srcset");
            String [] setImages = setImagesAttr.split(", ");
            if (setImages.length > 1) {
                String partImageX2 = setImages[setImages.length-1].split(" ")[0];
                image = "https:" + (partImageX2.isEmpty() ? image : partImageX2);
            }
        }
        return image;
    }
}
