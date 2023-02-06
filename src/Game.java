public class Game {

    private int currentGuess;
    private int currentLetter;

    private static Renderer rend = null;

    public Game(){
        currentGuess = 0;
        currentLetter = 0;
    }
    public void guess(){
        /*TODO get word from renderer
            send word to server
            receive result code
            switch on result code
                CORRECT:
                    update (all green)

         */

    }

    public void type (char c){
        if (currentLetter <= 9){
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

    public void trySignup(String name, char[] pass){
        //TODO interact with server for signup
    }

    public void tryLogin(String name, char[] pass){
        //TODO interact with server for login
        rend.play();
    }

    public static void setRenderer(Renderer r){
        rend = r;
    }
}
