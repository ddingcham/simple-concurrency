package puzzle;

public class Puzzle {
    static boolean answerReady = false;
    static int answer = 0;
    static Thread puzzleProvider = new Thread(){
        @Override
        public void run() {
            answer = 42;
            answerReady = true;
        }
    };
    static Thread puzzleConsumer = new Thread() {
        @Override
        public void run() {
            if (answerReady) System.out.println("answer : " + answer);
            else System.out.println("no answer");
        }
    };
    public static void main(String[] args) throws InterruptedException {
        puzzleProvider.start();
        puzzleConsumer.start();
        puzzleProvider.join();
        puzzleConsumer.join();
    }
}
