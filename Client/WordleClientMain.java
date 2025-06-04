import java.io.IOException;
//This class only serves as an initializer of thread structure of the client and does not actually do anything except
//waiting for the gameThread to finish
public class WordleClientMain {
    private static Game game; //the game object
    public static void main(String[] args) {
        try{
            game = new Game(); //initialize the game handler
        }catch (IOException e){
            System.out.println("Initialization error, shutting down..."); //if somehow the settings are not read correctly
        }
        Renderer renderer = new Renderer(game); //create and bind renderer object
        renderer.init(); //initialize it

        Thread gameThread = new Thread(game); //create a game thread AFTER GUI initialized
        gameThread.start(); //start it

        try {
            gameThread.join(); //await termination
        } catch (InterruptedException e) {
            e.printStackTrace(); //should never happen, in any case shut down and print the stack trace
        }
        System.exit(0);
    }
}
