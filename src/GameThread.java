public class GameThread extends Thread{

    public void run(){
        Game game = new Game();
        new Renderer(game);
    }
}
