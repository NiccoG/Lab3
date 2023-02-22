import java.io.Serializable;
import java.util.ArrayList;

public class ShareInstance implements Serializable {
    private final String user;
    private final ArrayList<String> results;

    public ShareInstance(String u, ArrayList<String> r){
        user=u;
        results=r;
    }

    public String getUser(){
        return user;
    }

    public ArrayList<String> getResults(){
        return results;
    }
}
