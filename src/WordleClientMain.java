import java.util.ArrayList;

public class WordleClientMain {
    public static void main(String[] args) {
        System.out.println("starting all by "+Thread.currentThread().getName());
        GameThread game = new GameThread();
        game.start();
    }
}