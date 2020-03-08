import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Arrays;

class Utils {
    static int[] getNumbersFrom(@NotNull String s) {
        return Arrays.stream(s.split("\\s"))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    static Document getDocBy(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }
}
