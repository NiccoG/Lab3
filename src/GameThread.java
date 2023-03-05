public class GameThread extends Thread{

    public void run(){
        System.out.println("running game by "+Thread.currentThread().getName());
        Game game = new Game();
        new Renderer(game);
    }
}
