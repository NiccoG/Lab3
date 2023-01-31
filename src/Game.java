public class Game {

    private int currentGuess;
    private int currentLetter;

    private static Renderer rend = null;

    public Game(){
        currentGuess = 0;
        currentLetter =0;
    }
    public void guess(){

    }

    public void type (char c){
        if (currentLetter < 9){
            rend.type(c,currentGuess,currentLetter);
            currentLetter++;
        }
    }

    public void delete(){
        if (currentLetter > 0){
            currentLetter--;
            rend.type('/',currentGuess,currentLetter); //use '/' for deletion
        }
    }

    public static void setRenderer(Renderer r){
        rend = r;
    }
}