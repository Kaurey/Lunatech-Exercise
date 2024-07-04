package lunatech;

import java.text.Normalizer;

public class FormattedTags {
    public static String tagsWithoutCaseAndAccent(String tag) {
        String formattedTag = 
                    Normalizer
                        .normalize(tag, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "")
                        .toLowerCase();
        return formattedTag;
    }
}
