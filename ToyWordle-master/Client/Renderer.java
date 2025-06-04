import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
//this class handles the GUI. Its methods are called by the game class when rendering is needed. It calls the
//game class' methods when buttons are clicked
public class Renderer{
    private final JLabel [][] lMap = new JLabel[6][5]; //a map of the game's "grid"'s labels
    private final HashMap<String,JLabel> letters = new HashMap<>();
    private final Game game; //the associated game object
    private JFrame frame; //the main frame
    private final Dimension menuD = new Dimension(410,270); //dimensions of menu window
    private JPanel menuPanel,gamePanel,loginPanel,statsPanel,distPanel; //different purpose panels
    private int size, current; //params of shared games structure
    private ArrayList<ShareInstance> list; //shared games structure
    public Renderer(Game game){ //binds Renderer and Game instances on creation
        this.game = game;
        game.setRenderer(this);
    }
    String [] alphabet = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};

    private void switchP(JPanel p){ //switch panel method
        frame.setContentPane(p);
        frame.pack();
        frame.repaint();
        p.requestFocus();
    }

    private JButton buttonCreator(int x, int y, int width, int height, String txt){ //support method to create buttons
                                                                                    //a little less verbosely
        JButton b = new JButton();
        b.setBackground(Color.BLACK);
        b.setForeground(Color.WHITE);
        b.setBounds(x,y,width,height);
        b.setText(txt);
        return b;
    }
    private JLabel labelCreator(Color bg, Color fg, int x, int y, int width, int height, String txt){   //support method to create labels
                                                                                                        //a little less verbosely
        JLabel l = new JLabel(txt,JLabel.CENTER);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setBounds(x,y,width,height);
        return l;
    }
    private JLabel letterLabel(int row, int col){   //creates the i-th,j-th letter label
        JLabel l = new JLabel("",JLabel.CENTER);
        l.setBackground(Color.BLACK);
        l.setFont(new Font("Serif",Font.BOLD,30));
        l.setForeground(Color.WHITE);
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        l.setBounds(20+col*55,30+row*55,50,50);
        l.setOpaque(true);
        lMap[row][col]=l; //these labels are stored to be accessed later
        return l;
    }

    private JPanel distP(int[] arr){ //creates the graphical representation label of the guess distribution array
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(menuD);

        JButton back = buttonCreator(165,200,100,60,"back"); //button to go back
        back.addActionListener(
                e -> switchP(statsPanel)
        );
        p.add(back);

        int max=0;
        for (int j : arr) {
            if (j > max)
                max = j;
        }

        if(max==0){ //if no games won yet, empty panel with back button
            return p;
        }

        for (int i=0;i<arr.length;i++){
            JLabel q = labelCreator(
                    Color.BLACK,
                    Color.GREEN,
                    10,
                    10+15*i,
                    40,
                    10,
                    i + 1 +" :"+arr[i]);
            p.add(q);

            JLabel l = labelCreator(
                    Color.WHITE,
                    Color.WHITE,
                    50,
                    10+15*i,
                    340*arr[i]/max,
                    10,
                    ""); //bars with scaled length
            l.setOpaque(true);
            p.add(l);

        }
        return p;
    }
    private JPanel gameP(){ //creates the game panel (grid)

        JPanel p= new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(new Dimension(600,770));

        for(int i=0; i<6; i++){
            for(int j=0;j<5;j++) {
                p.add(letterLabel(i, j));
            }
        }

        p.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { //whenever a key is pressed
                char keyPressed = e.getKeyChar();
                if ( keyPressed == '\n' ){ //if it's return
                    game.tryGuess();
                }else if( (keyPressed >= 'a' && keyPressed <= 'z') || (keyPressed >= 'A' && keyPressed <= 'Z') ){ //if it's a letter
                    game.type(keyPressed);
                }
                else if ( keyPressed == KeyEvent.VK_BACK_SPACE ){ //if it's backspace
                    game.delete();
                }
            }
        });

        for(int i=0;i<10;i++){
            JLabel l = labelCreator(Color.BLACK,Color.WHITE,30+i*40,370,30,30,alphabet[i]);
            l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            l.setOpaque(true);
            letters.put(alphabet[i],l);
            p.add(l);
        }
        for(int i=0;i<10;i++){
            JLabel l = labelCreator(Color.BLACK,Color.WHITE,30+i*40,410,30,30,alphabet[10+i]);
            l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            l.setOpaque(true);
            letters.put(alphabet[10+i],l);
            p.add(l);
        }
        for(int i=0;i<6;i++){
            JLabel l = labelCreator(Color.BLACK,Color.WHITE,30+i*40,450,30,30,alphabet[20+i]);
            l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            l.setOpaque(true);
            letters.put(alphabet[20+i],l);
            p.add(l);
        }

        JButton back = buttonCreator(240,690,105,40,"back"); //button to go back
        back.addActionListener(
                e -> switchP(menuPanel)
        );

        p.add(back);

        return p;
    }



    private JPanel menuP(){ //creates the menu panel (play, stats, show sharing, logout)
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(menuD);

        JButton play = buttonCreator(100,10,250,70,"Play"); //play button
        play.addActionListener(
                e -> game.tryPlay()
        );

        JButton stats = buttonCreator(100,90,250,70,"Show Stats"); //stats button
        stats.addActionListener(
                e-> game.tryStats()
        );

        JButton social = buttonCreator(100,170,250,70,"Compare with other players!"); //show shared button
        social.addActionListener(
                e -> game.trySocial()
        );

        JButton logout = buttonCreator(0,0,90,30,"Logout"); //logout button
        logout.addActionListener(
                e -> game.tryLogout()
        );

        p.add(logout);
        p.add(play);
        p.add(stats);
        p.add(social);

        return p;
    }
    private Color numberToColor(char c){ //parses colors strings and returns colors for GUI
        return switch (c) {
            case '1' -> Color.YELLOW;
            case '2' -> Color.GREEN;
            default -> Color.GRAY;
        };
    }
    private JPanel shareP(ShareInstance share){ //creates shared game panel

        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(menuD);

        JLabel playerName = labelCreator(Color.BLACK,Color.WHITE,125,0,150,30,share.getUser()); //name at the top
        playerName.setHorizontalAlignment(JLabel.CENTER);
        p.add(playerName);

        for (int i = 0; i < 6; i++){
            for(int j = 0; j < 5; j++){ //double loop to draw the colored squares
                JLabel l;
                if(i < share.getResults().size()){ //if still inside "played" range
                    l = labelCreator(
                            numberToColor(share.getResults().get(i).charAt(j)),
                            Color.BLACK,
                            100 + 20 * j,
                            30 + 18 * i,
                            15,
                            15,
                            ""
                    );
                    l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    l.setOpaque(true);
                }else{ //the rest are black squares
                    l = labelCreator(
                            Color.BLACK,
                            Color.BLACK,
                            100 + 20 * j,
                            30 + 18 * i,
                            15,
                            15,
                            ""
                    );
                    l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                }
                p.add(l);
            }
        }

        JButton prev = buttonCreator( //button for previous shared game
                0,
                120,
                50,
                40,
                "<"
        );
        prev.setRequestFocusEnabled(false);
        prev.setBorder(null);
        prev.addActionListener( //when clicked
                e-> goPrev()
        );

        JButton next = buttonCreator( //button for next shared game
                360,
                120,
                50,
                40,
                ">"
        );
        next.setRequestFocusEnabled(false);
        next.setBorder(null);
        next.addActionListener( //when clicked
                e-> goNext()
        );

        JButton back = buttonCreator( //button to go back
                155,
                245,
                90,
                30,
                "back"
        );
        back.setBorder(null);
        back.addActionListener(
                e -> switchP(menuPanel)
        );

        p.add(back);
        p.add(prev);
        p.add(next);

        return p;
    }
    private void goPrev(){ //when you try to get previous shared game
        if (current>0){ //if not at the very start
            current--;
            switchP(shareP(list.get(current)));
        }
    }

    private void goNext(){ //when you try to get next shared game
        if (current >= size - 1) { //if at the end
            return;
        }
        current++;
        switchP(shareP(list.get(current)));
    }

    private JPanel statsP(StatSnap stat){ //creates the stats panel
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(menuD);

        JLabel nPl = labelCreator(Color.BLACK,Color.WHITE,10,10,400,30,"Games Played: " //games played label
                + stat.getNumberPlayed());
        JLabel wR = labelCreator(Color.BLACK,Color.WHITE,10,50,400,30,"WinRate: " //winRate label
        + stat.getWinRate());
        JLabel laWS = labelCreator(Color.BLACK,Color.WHITE,10,90,400,30,"Last WinStreak: " //current winStreak label
        + stat.getLastStreak());
        JLabel loWS = labelCreator(Color.BLACK,Color.WHITE,10,130,400,30,"Longest WinStreak: "+ //longest winStreak label
                stat.getLongestStreak());
        JButton gD = buttonCreator(10,170,400,30,"Show Guess Distribution"); //guess distribution button
        gD.addActionListener(
                e -> switchP(distPanel)
        );

        JButton back = buttonCreator(165,200,100,60,"back"); //button to go back
        back.addActionListener(
                e -> switchP(menuPanel)
        );

        p.add(nPl);
        p.add(wR);
        p.add(laWS);
        p.add(loWS);
        p.add(gD);
        p.add(back);

        return p;
    }
    private JPanel loginP(){ //creates login panel (login, signup)

        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(menuD);

        JTextField name = new JTextField(); //name text field
        name.setBackground(Color.BLACK);
        name.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        name.setBounds(5,30,400,50);
        name.setForeground(Color.WHITE);

        JPasswordField password = new JPasswordField(); //password text field
        password.setBackground(Color.BLACK);
        password.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        password.setBounds(5,90,400,50);
        password.setForeground(Color.WHITE);

        JButton login = buttonCreator(100,170,100,80,"Login"); //button to try logging in
        login.addActionListener(
                e -> game.tryLogin(name.getText(),password.getPassword())
        );

        JButton signup = buttonCreator(210,170,100,80,"Sign Up"); //button to try signing up
        signup.addActionListener(
                e -> game.trySignup(name.getText(),password.getPassword())
        );

        p.add(name);
        p.add(password);
        p.add(login);
        p.add(signup);

        return p;
    }
    public void type(char c, int row, int col){ //actually type inside the grid of labels in game panel
        if (c == '/'){ //token to delete
            lMap[row][col].setText("");
            frame.repaint();
            return;
        }
        if (lMap[row][col].getText().equals("")) //if empty, to prevent changing last letter if writing on full word
            lMap[row][col].setText( String.valueOf( Character.toUpperCase(c) ) );
        frame.repaint();
    }

    public String getL(int row){ //get the word to send it for a guess
        String s="";
        for (int col=0; col < 5; col++){
            s=s.concat(lMap[row][col].getText());
        }
        return s;
    }
    public void guess(String colors,int current){ //parse guessed word colors and color the line accordingly
        for (int i=0;i<5;i++){
            Color color = numberToColor(colors.charAt(i));
            letters.get(lMap[current][i].getText()).setBackground(color);
            letters.get(lMap[current][i].getText()).setForeground(Color.BLACK);
            lMap[current][i].setBackground(color);
            lMap[current][i].setForeground(Color.BLACK);
        }
        frame.repaint();
        game.nextL();
    }

    public void win(){ //when game is won
        game.sendWin(); //notify server
        if(JOptionPane.showOptionDialog( //show a congratulations message
                frame,
                "You won!! Do you want to share?",
                "Wordle 3.0",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Yes","No"},
                "Yes") == 0)
        {
            game.tryShare(); //if you want to share
        }
        switchP(menuPanel);
        resetGame(); //clear grid
    }

    public void lose(){ //when game is lost
        JOptionPane.showMessageDialog( //show loss message
                frame,
                "Too bad, you lose!!",
                "Wordle 3.0",
                JOptionPane.PLAIN_MESSAGE);
        game.tryLose(); //notify server
        switchP(menuPanel);
        resetGame(); //clear grid
    }
    private void resetGame(){ //clear grid
        for (JLabel[] r : lMap){
            for(JLabel l : r){
                l.setBackground(Color.BLACK);
                l.setForeground(Color.WHITE);
                l.setText("");
            }
        }

        for(JLabel l : letters.values()){
            l.setBackground(Color.BLACK);
            l.setForeground(Color.WHITE);
        }

        frame.repaint();
        game.reset();
    }

    public void play(){
        switchP(gamePanel);
    } //when play is resolved successfully

    public void login(){
        switchP(menuPanel);
    } //when logged in successfully

    public void signup(){ //when signed up successfully
        JOptionPane.showMessageDialog(frame,"signed up successfully","Wordle 3.O",JOptionPane.INFORMATION_MESSAGE);
    }
    public void logout(){
        resetGame();
        game.reset();
        switchP(loginPanel);
    } //when logging out
    public void stat(StatSnap stat){ //when stat is received correctly
        statsPanel = statsP(stat); //create the panel of stats
        distPanel = distP(stat.getGuessD()); //create the distribution panel
        switchP(statsPanel); //switch to the panel
    }
    public void social(ArrayList<ShareInstance> s){ //when the show shared games button is clicked
        list = s; //store copy of sharedGames Game list
        current = 0; //start from the first one
        size = s.size(); //set bound
        switchP(shareP(list.get(current))); //switch to the first one
    }

    public void error(int code){ //various types of errors
        String error = switch (code) { //depending on the code
            case ShCon.SIGNUP -> "User already exists or password not strong enough";
            case ShCon.LOGIN -> "Wrong username/password";
            case ShCon.PLAY -> "You already played for this word!";
            case ShCon.GUESS -> "This word doesn't exist!";
            case ShCon.ONLINE -> "You are already logged in!";
            default -> "Unknown error, try again";
        };
        JOptionPane.showMessageDialog(frame,error,"Wordle 3.0",JOptionPane.ERROR_MESSAGE); //show the message
    }

    public void init(){ //Initialize panels and frame
        frame = new JFrame();
        frame.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) { //set method to run when "x" is clicked
                        try{
                            game.onExit();
                        }catch(IOException ev){
                            ev.printStackTrace();
                        }

                    }
                }
        );
        loginPanel=loginP();
        gamePanel=gameP();
        menuPanel=menuP();
        switchP(loginPanel);
        frame.setVisible(true);
    }
}
