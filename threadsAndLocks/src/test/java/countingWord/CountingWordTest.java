package countingWord;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CountingWordTest {

    @Test
    public void parse_page() {
        long count = 0;
        XMLWikiReader reader = new XMLWikiReader("enwiki-20190401-pages-articles-multistream14.xml");
        while(true) {
            if(reader.nextPage().isPresent()) {
                count ++;
                continue;
            }
            break;
        }
        assertEquals(12554, count);
    }


}
