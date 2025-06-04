import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;
//this class handles almost everything server side except for game logic (guessing and stats) handled by GameLogic
public class WordleServerMain {
    private static Boolean running = true; //flag to shut down softly
    private static ConcurrentHashMap<String, PlayerInfo> coreMap = null; //the map containing the state of the application
                                                                         //in terms of passwords and stats
    private static Gson gson; //gson object to serialize and deserialize things to and from file
    private static volatile ConcurrentHashMap<String, Status> playerStatus; //this is needed to check if a player is online
                                                                            //or has played already for the current word
    private static final Object playerStatusLock = new Object();//a lock to ensure that when word changes all players'
                                                                //status is reset before it can be read
    private static final Object coreMapLock = new Object();
    private static ArrayList<String> wordList; //the dictionary
    private static DatagramSocket datagramSocket; //the multicast socket for sharing games
    private static InetAddress group; //the multicast group
    private static Settings settings = null; //the settings object to not have different fields for every setting

    private static class Status { //this handles online status and if a player has already played for the word
        private Boolean online = false;
        private Boolean played = false;

        public Boolean getOnline() {
            return online;
        } //getter for online status

        public void login() {
            online = true;
        } //when a user logs in their status changes to online

        public void logout() {
            online = false;
        } //when they log out it changes to offline

        public void play() {
            played = true;
        } //when they play it changes to played

        public void reset() {
            played = false;
        } //when the word changes it is set back to false

        public Boolean getPlayed() {
            return played;
        } //getter for played status
    }

    private static class Settings implements Serializable { //the settings class, aggregator of fields
        public int NTHREADS; //number of threads for the client handler pool
        public int PORT; //Server TCP port
        public String infoPath; //path where player information is saved, has to be a json file
        public int DELAY; //delay in seconds from a word to the next one
        public String MULTICAST_IP; //address of the multicast group
        public int MULTICAST_PORT; //port of the group
        public int TIMEOUT; //timeout for server socket
    }

    private static class WordPicker implements Runnable{ //this class runs in the background picking words
        private final Random r; //random number generator
        public WordPicker(){
            this.r=new Random();
        } //when initialized, creates the generator

        public void run(){
            int index = r.nextInt(wordList.size()); //wordlist size bounds the generation
            String word = wordList.get(index); //get a word
            GameLogic.setWord(word); //set it globally
            System.out.println(word);
            wordChange(); //this serves to reset player status
        }
    }

    private static void send(ShareInstance share) { //this method is called on any share request and echoes the ShareInstance
                                                    //received to the multicast group
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream serializer = new ObjectOutputStream(bytes);
            serializer.writeObject(share);  //serialize the object
            serializer.flush();

            byte[] objectBytes = bytes.toByteArray();
            DatagramPacket packet = new DatagramPacket(objectBytes, objectBytes.length, group, settings.MULTICAST_PORT);
            datagramSocket.send(packet); //send it

        } catch (IOException e) { //this also should never happen, does not need particular handling
            System.out.println("Error sharing the stats");
        }
    }

    private static void init() throws IOException { //initialize the settings class reading from file
        FileReader reader = new FileReader("ServerSettings.json");
        settings = gson.fromJson(reader, Settings.class);
    }

    private static class ClientHandler implements Runnable { //this is the thread class that handles clients
        private final Socket clientSocket; //the client socket
        private final GameLogic gL; //the instance of gameLogic to aid with operations
        private boolean done = false; //flag to shut down softly

        public ClientHandler(Socket socket) { //constructor initializes socket and gameLogic
            clientSocket = socket;
            gL = new GameLogic();
        }

        public void run() {
            //try-with-resources to close the streams automatically
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                 ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()))){
                while (!done) { //loop reading from client until flag is flipped
                    switch (ois.readInt()) { //read OPCode
                        case ShCon.SHUTDOWN: //if client shuts down
                            System.out.println("Client closed");
                            done = true;
                            break;
                        case ShCon.SIGNUP: //when trying to signup
                            String nameAtt = (String) ois.readObject(); //get name
                            String passAtt = (String) ois.readObject(); //get password
                            oos.writeInt(ShCon.SIGNUP); //send OPCode
                            synchronized (coreMapLock){
                                if (coreMap.containsKey(nameAtt) || passAtt.equals("")) //sign up fail
                                    oos.writeBoolean(false);
                                else {
                                    coreMap.put(nameAtt, new PlayerInfo(passAtt)); //initialize player information
                                    oos.writeBoolean(true);
                                }
                            }
                            oos.flush();
                            break;
                        case ShCon.LOGIN: //when trying to log in

                            nameAtt = (String) ois.readObject(); //get name
                            passAtt = (String) ois.readObject(); //get password
                            synchronized (coreMapLock){
                                if (coreMap.containsKey(nameAtt)) { //if name exists in "database"
                                    if (coreMap.get(nameAtt).checkPass(passAtt)) { //if the password is correct
                                        synchronized (playerStatusLock) { //avoid reading mid-reset
                                            if (!playerStatus.containsKey(nameAtt)) //if a status doesn't exist yet
                                                playerStatus.put(nameAtt, new Status()); //create one
                                            if (!playerStatus.get(nameAtt).getOnline()) { //if not online already
                                                playerStatus.get(nameAtt).login(); //get online
                                                gL.setName(nameAtt); //set the name for the game logic
                                                oos.writeInt(ShCon.LOGIN); //tell the client
                                                oos.writeBoolean(true); //success
                                            } else {
                                                oos.writeInt(ShCon.ONLINE); //tell the client user is online already on another client
                                            }
                                        }
                                        oos.flush();
                                        break;
                                    }
                                }
                            }
                            oos.writeInt(ShCon.LOGIN); //tell the client
                            oos.writeBoolean(false); //fail
                            oos.flush();
                            break;
                        case ShCon.LOGOUT: //when log out is pressed
                            if (gL.isInProgress()) { //lose ongoing game
                                gL.update(7, coreMap);
                                gL.end();
                            }
                            synchronized (playerStatusLock) {
                                playerStatus.get(gL.getName()).logout(); //log out of status
                            }
                            gL.setName(null); //forget the name
                            oos.writeInt(ShCon.LOGOUT); //tell the client everything done
                            oos.flush();
                            break;
                        case ShCon.PLAY: //when trying to play a game
                            synchronized (playerStatusLock) {
                                Status temp = playerStatus.get(gL.getName()); //less verbose and fewer gets
                                if (!gL.isInProgress()) { //if there is no game in progress
                                    if (!temp.getPlayed()) { //and player has not played for this word already
                                        gL.startGame(); //start a new game
                                        temp.play(); //remember already played
                                    } else { //played already
                                        oos.writeInt(ShCon.PLAY); //tell the client they cannot play
                                        oos.writeBoolean(false);
                                        oos.flush();
                                        break;
                                    }
                                }
                            }
                            oos.writeInt(ShCon.PLAY); //tell the client they can play
                            oos.writeBoolean(true);
                            oos.flush();
                            break;
                        case ShCon.GUESS: //when sending a word attempt
                            String guess = (String) ois.readObject(); //get the word
                            if (!wordList.contains(guess.toLowerCase())) { //if it doesn't exist
                                oos.writeInt(ShCon.GUESS); //tell the client
                                oos.writeBoolean(false);
                                oos.flush();
                                break;
                            }
                            String res = GameLogic.match(guess, gL.getTemp()); //else generate "colored" matching string
                            if (gL.checkWin(res)) { //if it's the win string
                                gL.end(); //end the current attempt
                                oos.writeInt(ShCon.WIN); //tell the client they won
                                oos.flush();
                                gL.update(ois.readInt(), coreMap); //update the stats
                            } else {
                                oos.writeInt(ShCon.GUESS); //otherwise just tell the client
                                oos.writeBoolean(true);
                                oos.writeObject(res); //the colors string so that they can paint the line
                                oos.flush();
                            }
                            break;
                        case ShCon.LOSE: //when client runs out of tries
                            gL.end(); //end current attempt
                            gL.update(11, coreMap); //update stats
                            break;
                        case ShCon.STATS: //when client asks to see stats
                            oos.writeInt(ShCon.STATS);
                            oos.writeObject(new StatSnap(coreMap.get(gL.getName()).getStats())); //send them a copy of it
                            oos.flush();
                            break;
                        case ShCon.SHARE: //when client asks to share a won game
                            send((ShareInstance) ois.readObject()); //send it to multicast group
                            break;
                    }
                }
                System.out.println("Client closing normally..."); //if exiting by flag change (ShCon.SHUTDOWN)
            } catch (EOFException e) {
                System.out.println("Client stream closed abruptly, client handler closing..."); //if exiting by stream closing or weird timeouts
            } catch (IOException e) {
                System.out.println("Client shutting down, client handler closing..."); //other general problems with input or output streams
            } catch (ClassNotFoundException e) {
                System.out.println("Couldn't parse client data, client handler closing..."); //should never happen if compiled correctly
            } finally {
                if (gL.isInProgress()) { //if exiting abruptly (or without client logging out)
                    gL.update(11, coreMap); //lose
                    gL.end(); //end the attempt
                }
                synchronized (playerStatusLock) { //avoid reading mid-reset
                    if(gL.getName()!=null) { //if they didn't log out
                        Status status = playerStatus.get(gL.getName());
                        if (status.getOnline()) {
                            status.logout(); //log out
                        }
                    }
                }
            }
        }
    }

    private static class exitListener implements Runnable { //background thread listening to CLI to exit as intended
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (running) {
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("exit")) //this should be the ONLY way that server is ever closed
                                                                 //apart from exceptions that ensures that data is saved
                    running = false;
            }
        }
    }

    public static void wordChange() { //when word changes
        synchronized (playerStatusLock) {
            if(playerStatus!=null)
                playerStatus.values().forEach(
                        Status::reset //make everyone not have played yet
                );
        }
    }

    public static void save() { //save the current state of players
        try (
                FileWriter writer = new FileWriter(settings.infoPath)
        ) {
            gson.toJson(coreMap, writer);
            System.out.println("Player data correctly saved, exiting..."); //saved everything
        } catch (IOException e) {
            System.out.println("Error saving player data, it might be corrupted");
        }
    }

    public static void main(String[] args) { //the main server thread
        gson = new Gson(); //serializer and deserializer
        try {
            init(); //try to read settings
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error parsing server parameters, shutting down..."); //crash otherwise
            return;
        }
        Thread exit = new Thread(new exitListener()); //listen to exit as first thing after init
        exit.start();


        try (BufferedReader reader = new BufferedReader(new FileReader("words.txt"));
             BufferedWriter writer = new BufferedWriter(new FileWriter("wordsFiltered.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.length() == 5) {
                    writer.write(line);
                    writer.write('\n');
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading dictionary, shutting down..."); //crash otherwise
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(settings.infoPath))) {
            Type type = new TypeToken<ConcurrentHashMap<String, PlayerInfo>>() {
            }.getType();
            coreMap = gson.fromJson(reader, type); //try to read all the player info
        } catch (IOException e) {
            System.out.println("Error reading player data, shutting down..."); //crash otherwise
            return;
        }

        try {
            group = InetAddress.getByName(settings.MULTICAST_IP);
            datagramSocket = new DatagramSocket(0); //try setting up the multicast group
            datagramSocket.setBroadcast(true);
        } catch (IOException e) {
            System.out.println("Error initializing multicast socket, shutting down..."); //crash otherwise
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("wordsFiltered.txt"))) {
            wordList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                wordList.add(line); //try reading the word dictionary
            }
        } catch (IOException e) {
            System.out.println("Error reading dictionary, shutting down..."); //crash otherwise
            return;
        }

        if (coreMap == null) {
            coreMap = new ConcurrentHashMap<>(); //in case file was empty (no one ever played yet)
        }
        playerStatus = new ConcurrentHashMap<>(); //initialize player status
        ScheduledExecutorService picker = Executors.newScheduledThreadPool(1); //the scheduled execution of word picker at fixed intervals
        picker.scheduleAtFixedRate(new WordPicker(), 0, settings.DELAY, TimeUnit.SECONDS);
        ArrayList<Socket> clients = null; //this is to close sockets individually at the end and ensure proper shutdown client side
        ExecutorService pool = null; //the thread pool for clients
        try (ServerSocket server = new ServerSocket(settings.PORT)) { //try-with-resources to close server socket automatically
            server.setReuseAddress(true);
            server.setSoTimeout(settings.TIMEOUT); //an arbitrary timeout to check flag once in a while
            pool = Executors.newFixedThreadPool(settings.NTHREADS); //initialize the pool
            clients = new ArrayList<>(); //initialize the clients list
            while (running) {
                try {
                    Socket client = server.accept(); //accept client connection
                    System.out.println("New client connected: " + client.getInetAddress().getHostAddress());
                    //log some information

                    clients.add(client); //add the socket to the list

                    Runnable clientSock = new ClientHandler(client); //start the handler
                    pool.execute(clientSock);

                } catch (SocketTimeoutException e) {
                    if (!running) //just check if shut down is in progress
                        break;
                }
            }


        } catch (IOException e) { //general error, just print the stacktrace right before exiting
            e.printStackTrace();
        } finally {
            picker.shutdown(); //shut the picker thread down
            if (pool != null)
                pool.shutdown(); //shut the pool of client handlers down
            if (clients != null)
                clients.forEach(
                        socket -> {
                            try {
                                socket.close(); //close all client sockets gracefully
                            } catch (IOException e) {
                                System.out.println("Error closing clients"); //if failing to do so notify it
                            }
                        }
                );
            System.out.println("Saving data...");
            save(); // save everything
            datagramSocket.close(); //close the multicast socket
        }
        System.out.println("Server closing..."); //end notification
    }
}
