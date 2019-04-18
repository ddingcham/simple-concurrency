package countingWord;

import countingWord.domain.Page;

import java.util.Optional;

public interface WikiReader {
    String EMPTY = "cw-empty-flag";
    Optional<Page> nextPage();
}
