import java.io.Serializable;
import java.util.ArrayList;
//this class represents one shared game, encapsulating username and "colors" of lines guessed
public class ShareInstance implements Serializable {
    private final String user;
    private final ArrayList<String> results;
    public ShareInstance(String u, ArrayList<String> r){ //initializes parameters
        user=u;
        results=r;
    }
    public String getUser(){
        return user;
    } //getter for username
    public ArrayList<String> getResults(){ //getter for results list
        return results;
    }
}