import java.io.Serializable;
//this class groups player info to write to file and provide methods for access
public class PlayerInfo implements Serializable {
    private final String password;
    private final Stat stats;

    public PlayerInfo(String p){ //first sign up
        password=p;
        stats=new Stat();
    }

    public Boolean checkPass(String attempt){
        return attempt.equals(password);
    } //checks password

    public Stat getStats(){
        return stats;
    }
}
