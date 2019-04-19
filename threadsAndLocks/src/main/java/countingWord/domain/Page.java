package countingWord.domain;

import lombok.Data;

@Data
public class Page {

    public static final Page POISON_PILL = new Page(null, null);
    private final String title;
    private final String text;
}
