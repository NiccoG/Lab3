import java.util.ArrayList;

public class Game{

    private int currentGuess;
    private int currentLetter;

    private static Renderer rend = null;
    private final ShareInstance share;


    public Game(){
        currentGuess = 0;
        currentLetter = 0;
        //EXAMPLE STAT INSTANCE
        ArrayList<String> list = new ArrayList<String>(0);
        list.add("1020101202");
        list.add("1020001221");
        list.add("0221102011");
        list.add("0010001222");
        list.add("2222222110");
        list.add("2222222222");
        share = new ShareInstance("zortik",list);
        //EXAMPLE STAT INSTANCE
    }
    public void guess(){
        /*TODO get word from renderer
            send word to server
            receive result code
            switch on result code
                CORRECT:
                    update (all green)

         */
        if(currentLetter == 10){
            currentLetter = 0;
            currentGuess++;
        }
    }

    public void type (char c){
        rend.nextL();
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
        rend.signup(true);
    }

    public void tryLogin(String name, char[] pass){
        //TODO interact with server for login
        rend.login(true);
    }

    public void play(){
        //TODO interact with server for play
        rend.play(true);
    }

    public void tryLogout(){
        //TODO interact with server for logout
        rend.logout(true);
    }

    public void tryStats(){
        //TODO interact with server for stats
        rend.stat(true);
    }

    public void trySocial(){
        //TODO interact with server to get list of ShareInstance
        //TODO GAME class keeps list of shareinstances and implements a method that goes prev and next on demand performing the necessary checks
        rend.social(true, share);
    }

    public void setRenderer(Renderer r){
        rend = r;
    }
}
