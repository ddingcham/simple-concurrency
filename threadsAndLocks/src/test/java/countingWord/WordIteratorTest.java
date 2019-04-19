package countingWord;

import countingWord.domain.WordIterator;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class WordIteratorTest {

    @Test
    public void count_words() {
        final HashMap<String, Integer> counts = new HashMap<>();
        WordIterator iterator = new WordIterator("a b c d e f g h i ij ij ij ij ijk");

        while (iterator.hasNext()) {
            String word = iterator.next();
            counts.merge(word, 1, (a, b) -> a + b);
        }

        assertEquals(1, counts.get("i").intValue());
        assertEquals(4, counts.get("ij").intValue());
        assertEquals(1, counts.get("ijk").intValue());
    }
}
