package puzzle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PuzzleTest {

    @Test
    public void answer_is_not_defined() throws InterruptedException {
        Puzzle.puzzleProvider.start();
        Puzzle.puzzleConsumer.start();
        Puzzle.puzzleProvider.join();
        Puzzle.puzzleConsumer.join();
        assertEquals(42, Puzzle.answer);
    }
}
