import java.util.concurrent.ConcurrentHashMap;
//this class handles the game logic server side, leaving network communication to other classes
//only handling guesses stats and game status.
public class GameLogic {
    private static String word; //the current word, same for all GameLogic instances
    private String temp,name;   //the word of the current game, can persist after global change
                                //the name of the user playing
    private Boolean inProgress=false; //indicates if guess attempt is in progress
    public String getTemp(){
        return temp;
    } //getter for current attempt's word
    public static void setWord(String word) {
        GameLogic.word=word;
    } //setter for current global word
    public void setName(String name){
        this.name=name;
    } //setter for user logged in
    public String getName(){
        return name;
    } //getter for the username
    public Boolean isInProgress(){
        return inProgress;
    } //getter for inProgress flag
    public void startGame(){ //used to set variables for current attempt
        temp=word; //lock word
        inProgress=true;
    }
    public void end(){
        inProgress=false;
    } //end the attempt
    public Boolean checkWin(String line){
        return line.equals("22222");
    } //check if guess is winning
    public void update(int line, ConcurrentHashMap<String,PlayerInfo> map){ //update stats
        Stat stats = map.get(name).getStats();
        if(line<7){ //if won
            stats.win(line);
            return;
        }
        stats.lose(); //if lost
    }
    public static String match(String guess, String target){ //returns "colors" string
                                                             //where 0: gray, 1: yellow, 2: green
        char[] res = new char[5];
        char[] g = guess.toLowerCase().toCharArray();
        char[] t = target.toLowerCase().toCharArray();
        for(int i=0;i<5;i++){ //first sweep for correct letters
            if(g[i]==t[i]){
                res[i]='2';
                t[i]='/'; //remove from target string already matched letters to not match them twice
            }
        }
        for(int i=0;i<5;i++){ //second sweep for misplaced letters
            if(res[i]=='\0')
                for(int j=0;j<5;j++){
                    if (g[i] == t[j]) {
                        res[i] = '1';
                        t[j] = '/';
                        break;
                    }
                }
        }
        for(int i = 0;i<5;i++){ //the rest is gray
            if (res[i]=='\0'){
                res[i]='0';
            }
        }
        return new String(res);
    }
}
