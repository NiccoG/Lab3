import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
//This class makes everything related to the actual game work from client side, handling connection with server (sending and receiving requests/messages)
//and calls the Renderer class methods in order to visualize effects on the interface. Its methods are almost exclusively called by the Event Dispatch Thread
//on button clicks.
public class Game implements Runnable{
    private int currentGuess,currentLetter; //these keep track of current row and column of ongoing game.
    private Renderer rend = null; //the object used to handle the GUI
    private Boolean done = false; //flag used to soft shutdown
    private ObjectOutputStream oos; //stream where data is written to server
    private ObjectInputStream ois; //stream where data from server is read
    private ArrayList<ShareInstance> sharedGames; //data structure used to store notifications from the multicast group
    private final Object shareLock = new Object();  //lock to prevent synchronization problems in the tryShare method and share class
    private Settings settings; //used for config constants
    private final Gson gson;  //used to deserialize config file
    private ArrayList<String> currentGame;  //used to keep track of the current game in case you want to share it
    private String user; //used to build the share instance when sending it to server

    private class share implements Runnable{ //separate thread listening to multicast group and handling notifications
        public void run(){
            try {
                sharedGames = new ArrayList<>(0);
                InetAddress group = InetAddress.getByName(settings.MULTICAST_IP);
                MulticastSocket ms = new MulticastSocket(settings.MULTICAST_PORT);
                ms.setSoTimeout(5000); //to check flag
                ms.joinGroup(group);
                byte[] buffer = new byte[1024]; //sufficiently large
                DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                while (!done){
                    try{
                        ms.receive(received);
                        byte[] bytes = received.getData();
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                        ObjectInputStream deserializer = new ObjectInputStream(byteArrayInputStream);  //OIS used to deserialize object sent in datagram
                        ShareInstance share = (ShareInstance) deserializer.readObject();

                        synchronized (shareLock){
                            sharedGames.add(share); //add the shared game to data structure
                        }

                        deserializer.close();
                        byteArrayInputStream.close(); //close the streams
                    }catch (SocketTimeoutException e){ //every once in a while check exit condition
                        if(done)
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error with multicast group");
                try {
                    onExit(); //always try to soft close
                } catch (IOException ex) {
                    System.out.println("Error closing client normally");
                }
            }
        }
    }

    private static class Settings implements Serializable { //class containing config parameters, better readability than normal fields
        public int PORT;
        public String MULTICAST_IP;
        public int MULTICAST_PORT;
    }

    private void init() throws IOException{ //reads the settings, instantiates the settings field
        FileReader reader = new FileReader("ClientSettings.json");
        settings = gson.fromJson(reader, Settings.class);
    }
    public Game() throws IOException{ //initialize local variables
        currentGuess = 0;
        currentLetter = 0;
        gson = new Gson();
        init();
    }

    public void run(){

        InetAddress host;
        try {
            host = InetAddress.getLocalHost();  //gets localhost address
        }catch(UnknownHostException e){
            System.out.println("could not find server address, shutting down...");
            return;
        }

        Thread shareThread = new Thread(new share()); //creates and starts the share thread
        shareThread.start();

        if(host!=null){ //should never be with localhost, safety measure
            try (
                    Socket sock = new Socket(host.getHostName(), settings.PORT)    //try-with-resources to close sock automatically
            ) {
                oos = new ObjectOutputStream(new BufferedOutputStream(sock.getOutputStream()));
                ois = new ObjectInputStream(new BufferedInputStream(sock.getInputStream())); //not included in the resources because called by external methods
                while (!done) {
                    switch (ois.readInt()) { //reads OPcode from server and acts accordingly
                        case ShCon.SIGNUP -> {
                            if (ois.readBoolean()) //if success
                                rend.signup();
                            else //if name already taken or password empty
                                rend.error(ShCon.SIGNUP);
                        } //SIGNUP

                        case ShCon.LOGIN -> {
                            if (ois.readBoolean()) { //if success
                                currentGame = new ArrayList<>(0);
                                rend.login();
                            }
                            else { //if wrong name/password
                                rend.error(ShCon.LOGIN);
                                user = null;  //forget current username
                            }
                        }//LOGIN

                        case ShCon.LOGOUT -> { //cannot "fail"
                            currentGame=null;
                            rend.logout();
                        } //LOGOUT

                        case ShCon.PLAY -> {
                            if (ois.readBoolean()) //if success
                                rend.play();
                            else //if already played
                                rend.error(ShCon.PLAY);
                        } //PLAY

                        case ShCon.GUESS -> {
                            if (ois.readBoolean()) { //if success
                                String guess = (String) ois.readObject();
                                currentGame.add(guess);
                                rend.guess(guess, currentGuess);
                                if(currentGuess == 6){ //if used all guesses
                                    rend.lose();
                                }
                            }
                            else //if word does not exist
                                rend.error(ShCon.GUESS);
                        } //GUESS

                        case ShCon.WIN -> { //cannot "fail"
                            currentGame.add("22222"); //add winning line
                            rend.guess("22222", currentGuess); //color last line
                            rend.win(); //do necessary operations on win
                        } //WIN

                        case ShCon.STATS -> rend.stat((StatSnap) ois.readObject()); //can't fail, receive and show stats

                        case ShCon.ONLINE -> rend.error(ShCon.ONLINE); //can't fail, error when trying to log in multiple times simultaneously
                    }
                }
            }catch (SocketException s){ //correct way to shut down if server is closed
                System.out.println("Client closing");
            }catch (IOException | ClassNotFoundException | NullPointerException e) { //unpredicted behaviour
                e.printStackTrace();
                System.out.println("Fatal error, closing");
            }
            finally{
                if(!done)
                    try{
                        onExit(); //try to soft close, almost always fails to close streams because server is unavailable
                    }catch(IOException e){
                        System.out.println("Could not shut down correctly, server might be unavailable");
                    }
            }
        }else{
            System.out.println("Could not find server address, shutting down..."); //if host is null
        }
    }
    public void onExit() throws IOException{ //method called when closing GUI or when trying to clean up
        done=true;  //make sure share thread terminates

        if(oos!=null){
            oos.writeInt(ShCon.SHUTDOWN); //signal server so that it shuts down the handler softly
            oos.flush();
            ois.close();  //close streams
            oos.close();
        }
    }
    public void type (char c){ //called whenever you press a button on the play interface, checks row and column and renders the change
        if (currentLetter <= 4){
            rend.type(c,currentGuess,currentLetter);
            currentLetter++;
        }
    }
    public void nextL(){ //when guess is sent and line needs to be changed
        currentGuess++;
        currentLetter=0;
    }
    public void delete(){ //on backspace press
        if (currentLetter > 0){
            currentLetter--;
            rend.type('/',currentGuess,currentLetter); //use '/' for deletion, just a token
        }
    }
    public void reset(){ //whenever you win or lose
        currentGuess = 0;
        currentLetter = 0;
        if(currentGame!=null)
            currentGame.clear(); //forget previous game
    }
    public void trySignup(String name, char[] pass){ //attempts to contact the server for signup, sending OPCode, name and password
        try{
            oos.writeInt(ShCon.SIGNUP);
            oos.writeObject(name);
            oos.writeObject(String.valueOf(pass));
            oos.flush();
        }catch(IOException e){ //unpredicted behaviour
            e.printStackTrace();
            rend.error(-1);
        }
    }
    public void tryLogin(String name, char[] pass){ //attempts to contact the server for login, sending OPCode, name and password
        try{
            oos.writeInt(ShCon.LOGIN);
            oos.writeObject(name);
            oos.writeObject(String.valueOf(pass));
            oos.flush();
            user = name; //sets current username
        }catch(IOException e){ //unpredicted behaviour
            e.printStackTrace();
            rend.error(-1);
        }
    }



    public void tryLogout(){ //attempts to contact the server for logout, sending OPCode
        try{
            oos.writeInt(ShCon.LOGOUT);
            oos.flush();
            user = null;
        }catch(IOException e){ //unpredicted behaviour
            e.printStackTrace();
            rend.error(-1);
        }
    }

    public void tryPlay(){ //attempts to contact the server to start game, sending OPCode
        try{
            oos.writeInt(ShCon.PLAY);
            oos.flush();
        }catch(IOException e){ //unpredicted behaviour
            e.printStackTrace();
            rend.error(-1);
        }
    }
    public void tryGuess(){ //attempts to contact the server for guess, sending OPCode and word
        if(currentLetter == 5){ //only if line is full
            try{
                oos.writeInt(ShCon.GUESS);
                oos.writeObject(rend.getL(currentGuess));
                oos.flush();
            }catch(IOException e){ //unpredicted behaviour
                e.printStackTrace();
                rend.error(-1);
            }
        }
    }
    public void tryStats(){ //attempts to contact the server for Stats, sending OPCode
        try{
            oos.writeInt(ShCon.STATS);
            oos.flush();
        }catch(IOException e) { //unpredicted behaviour
            e.printStackTrace();
            rend.error(-1);
        }
    }
    public void tryShare(){ //attempts to share the current game with server, sending OPCode and ShareInstance
        try{
            oos.writeInt(ShCon.SHARE);
            oos.writeObject(new ShareInstance(user,new ArrayList<>(currentGame)));  //need to make a new instance AND a new ArrayList to prevent object caching
                                                                                    //of the same references
        }catch (IOException e){ //unpredicted behaviour
            rend.error(-1);
        }
    }
    public void trySocial(){ //attempts to visualize shared games
        if(sharedGames.size()>0)
            synchronized(shareLock) {
                rend.social(new ArrayList<>(sharedGames)); //use a copy to avoid conflicts with changing data structure
            }
    }
    public void sendWin(){ //tells the server the line at which user wins
        try{
            oos.writeInt(currentGuess-1);
            oos.flush();
        }catch(IOException e) { //unpredicted behaviour
            rend.error(-1);
        }
    }

    public void tryLose(){ //tells the server user lost
        try{
            oos.writeInt(ShCon.LOSE);
            oos.flush();
        }catch(IOException e) { //unpredicted behaviour
            rend.error(-1);
        }
    }

    public void setRenderer(Renderer r){ //binds renderer Object to Game object
        rend = r;
    }
}
