package countingWord.domain;

import java.text.BreakIterator;
import java.util.Iterator;

public class WordIterator implements Iterator<String> {
    private String text;
    private BreakIterator boundary;
    private int start;
    private int end;

    public WordIterator(String text) {
        this.text = text;
        boundary = BreakIterator.getWordInstance();
        boundary.setText(text);
        start = boundary.first();
        end = boundary.next();
    }

    @Override
    public void remove() {
        //void
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        return end != BreakIterator.DONE;
    }

    @Override
    public String next() {
        String next = text.substring(start, end);
        start = end;
        end = boundary.next();
        return next;
    }
}