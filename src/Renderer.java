import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
public class Renderer { //Class that renders the game's UI and memorizes current guess' letters

    private int[] guessD;
    private final JLabel [][] lMap = new JLabel[12][10];
    private final Game game;
    private JFrame frame;
    private final Dimension menuD = new Dimension(410,270);
    private JPanel menuPanel,gamePanel,loginPanel,statsPanel,sharePanel,distPanel;
    public Renderer(Game game){   //TODO arguments
        this.game = game;
        game.setRenderer(this);
        init();
    }

    private void switchP(JPanel p){
        frame.setContentPane(p);
        frame.pack();
        frame.repaint();
        p.requestFocus();
    }

    private JButton buttonCreator(Color bg,Color fg, int x, int y, int width, int height, String txt){
        JButton b = new JButton();
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBounds(x,y,width,height);
        b.setText(txt);
        return b;
    }
    private JLabel labelCreator(Color bg, Color fg, int x, int y, int width, int height, String txt){
        JLabel l = new JLabel(txt,JLabel.LEFT);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setBounds(x,y,width,height);
        return l;
    }
    private JLabel wLetter(int row, int col){
        JLabel l = new JLabel("",JLabel.CENTER);
        l.setBackground(Color.BLACK);
        l.setFont(new Font("Serif",Font.BOLD,30));
        l.setForeground(Color.WHITE);
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        l.setBounds(20+col*55,30+row*55,50,50);
        lMap[row][col]=l;
        return l;
    }

    private JPanel distP(int[] arr){
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(menuD);

        int max=0;
        for (int j : arr) {
            if (j > max)
                max = j;
        }
        for (int i=0;i<arr.length;i++){
            JLabel l = labelCreator(Color.WHITE,Color.WHITE,10,10+15*i,390*arr[i]/max,10,"");
            l.setOpaque(true);
            p.add(l);
        }

        JButton back = buttonCreator(Color.BLACK,Color.WHITE,165,200,100,60,"back");
        back.addActionListener(
                e -> switchP(statsPanel)
        );
        p.add(back);

        return p;
    }
    private JPanel gameP(){

        JPanel p= new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(new Dimension(600,770));

        for(int i=0; i<12; i++){
            for(int j=0;j<10;j++) {
                p.add(wLetter(i, j));
            }
        }

        p.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char keyPressed = e.getKeyChar();
                if ( keyPressed == '\n' ){
                    game.guess();
                }else if( (keyPressed >= 'a' && keyPressed <= 'z') || (keyPressed >= 'A' && keyPressed <= 'Z') ){
                    game.type(keyPressed);
                }
                else if ( keyPressed == KeyEvent.VK_BACK_SPACE ){
                    game.delete();
                }
            }
        });

        JButton back = buttonCreator(Color.BLACK,Color.WHITE,240,690,105,40,"back");
        back.addActionListener(
                e -> switchP(menuPanel)
        );

        p.add(back);

        return p;
    }



    private JPanel menuP(){
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(menuD);

        JButton play = buttonCreator(Color.BLACK,Color.WHITE,100,10,250,70,"Play");
        play.addActionListener(
                e -> game.play()
        );

        JButton stats = buttonCreator(Color.BLACK,Color.WHITE,100,90,250,70,"Show Stats");
        stats.addActionListener(
                e-> game.tryStats()
        );

        JButton social = buttonCreator(Color.BLACK,Color.WHITE,100,170,250,70,"Compare with other players!");
        social.addActionListener(
                e -> game.trySocial()
        );

        JButton logout = buttonCreator(Color.BLACK,Color.WHITE,0,0,90,30,"Logout");
        logout.addActionListener(
                e -> game.tryLogout()
        );

        p.add(logout);
        p.add(play);
        p.add(stats);
        p.add(social);

        return p;
    }
    private Color numberToColor(char c){
        return switch (c) {
            case '1' -> Color.YELLOW;
            case '2' -> Color.GREEN;
            default -> Color.GRAY;
        };
    }
    private JPanel shareP(){

        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(menuD);

        JButton prev = buttonCreator(
                Color.BLACK,
                Color.WHITE,
                0,
                120,
                50,
                40,
                "<"
        );
        prev.setRequestFocusEnabled(false);
        prev.setBorder(null);

        JButton next = buttonCreator(
                Color.BLACK,
                Color.WHITE,
                360,
                120,
                50,
                40,
                ">"
        );
        next.setRequestFocusEnabled(false);
        next.setBorder(null);

        JButton back = buttonCreator(
                Color.BLACK,
                Color.WHITE,
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
    private void newShareP(ShareInstance share){

        JLabel playerName = labelCreator(Color.BLACK,Color.WHITE,125,0,150,30,share.getUser());
        playerName.setHorizontalAlignment(JLabel.CENTER);
        sharePanel.add(playerName);

        for (int i = 0; i < 12; i++){
            for(int j = 0; j < 10; j++){
                JLabel l;
                if(i < share.getResults().size()){
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
                }else{
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
                sharePanel.add(l);
            }
        }
        //TODO complicated share panel
    }

    private JPanel statsP(){
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(menuD);

        JLabel nPl = labelCreator(Color.BLACK,Color.WHITE,10,10,400,30,"Games Played: ");
        JLabel wR = labelCreator(Color.BLACK,Color.WHITE,10,50,400,30,"Winrate: ");
        JLabel laWS = labelCreator(Color.BLACK,Color.WHITE,10,90,400,30,"Last Winstreak: ");
        JLabel loWS = labelCreator(Color.BLACK,Color.WHITE,10,130,400,30,"Longest Winstreak: ");
        JButton gD = buttonCreator(Color.BLACK,Color.WHITE,10,170,400,30,"Show Guess Distribution");
        gD.addActionListener(
                e -> switchP(distPanel)
        );

        JButton back = buttonCreator(Color.BLACK,Color.WHITE,165,200,100,60,"back");
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
    private JPanel loginP(){

        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setPreferredSize(menuD);

        JTextField name = new JTextField();
        name.setBackground(Color.BLACK);
        name.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        name.setBounds(5,30,400,60);
        name.setForeground(Color.WHITE);

        JPasswordField password = new JPasswordField();
        password.setBackground(Color.BLACK);
        password.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        password.setBounds(5,100,400,60);
        password.setForeground(Color.WHITE);

        JButton login = buttonCreator(Color.BLACK,Color.WHITE,100,170,100,80,"Login");
        login.addActionListener(
                e -> game.tryLogin(name.getText(),password.getPassword())
        );

        JButton signup = buttonCreator(Color.BLACK,Color.WHITE,210,170,100,80,"Sign Up");
        signup.addActionListener(
                e -> game.trySignup(name.getText(),password.getPassword())
        );

        p.add(name);
        p.add(password);
        p.add(login);
        p.add(signup);


        return p;
    }

    public void type(char c, int row, int col){
        if (c == '/'){
            lMap[row][col].setText("");
            frame.repaint();
            return;
        }
        if (lMap[row][col].getText().equals(""))
            lMap[row][col].setText( String.valueOf( Character.toUpperCase(c) ) );
        frame.repaint();
    }

    public void nextL(){  //TODO Data package class
        //color stuff
    }

    public void play(boolean success){
        if (success) {
            switchP(gamePanel);
            return;
        }
    }

    public void login(boolean success){
        if (success){
            switchP(menuPanel);
            return;
        }
    }

    public void signup(boolean success){
        if (success){

            return;
        }
    }
    public void logout(boolean success){
        if (success){
            switchP(loginPanel);
            return;
        }
    }
    public void stat(boolean success){
        if (success){
            //TODO set guessD
            distPanel = distP(guessD);
            switchP(statsPanel);
            return;
        }

    }


    public void social(boolean success, ShareInstance s){ //TODO SHAREINSTANCE AND PANEL
        if (success){
            newShareP(s);
            switchP(sharePanel);
        }
    }

    private void init(){
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        loginPanel=loginP();
        gamePanel=gameP();
        menuPanel=menuP();
        statsPanel=statsP();
        sharePanel=shareP();
        guessD=new int[]{1,2,3,4,5,6,6,5,4,3,2,1};  //TODO change guessD initialization
        switchP(loginPanel);
        frame.setVisible(true);

    }
}
