import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Renderer { //Class that renders the game's UI and memorizes current guess' letters

    private final JLabel [][] lMap = new JLabel[12][10];
    private final Game game;
    private JFrame frame;
    private JPanel menuPanel,gamePanel,loginPanel,statsPanel,sharePanel;
    public Renderer(Game game){   //TODO arguments
        this.game = game;
        game.setRenderer(this);
        init();
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
        //TODO
    }
    private JLabel wLetter(int row, int col){
        JLabel l = new JLabel("",JLabel.CENTER);
        l.setOpaque(true);
        l.setBackground(Color.BLACK);
        l.setFont(new Font("Serif",Font.BOLD,30));
        l.setForeground(Color.WHITE);
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        l.setBounds(20+col*55,30+row*55,50,50);
        lMap[row][col]=l;
        return l;
    }
    private JPanel gameP(){

        JPanel p= new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setSize(600,770);

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
                else if ( keyPressed == KeyEvent.VK_BACK_SPACE){
                    game.delete();
                }
            }
        });

        return p;
    }

    private JPanel menuP(){
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setSize(430,300);

        JButton play = buttonCreator(Color.BLACK,Color.WHITE,100,10,250,70,"Play");
        play.addActionListener(
                e -> game.play()
        );

        JButton stats = buttonCreator(Color.BLACK,Color.WHITE,100,90,250,70,"Show Stats");
        JButton social = buttonCreator(Color.BLACK,Color.WHITE,100,170,250,70,"Compare with other players!");

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

    private JButton buttonCreator(Color bg,Color fg, int x, int y, int width, int height, String txt){
        JButton b = new JButton();
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBounds(x,y,width,height);
        b.setText(txt);
        return b;
    }

    private JPanel shareP(){
        JPanel p = new JPanel();
        //TODO complicated share panel
        return p;
    }
    private JPanel loginP(){

        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(Color.BLACK);
        p.setSize(430,300);

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

    public void play(){
        switchP(gamePanel);
    }

    public void loginSuccess(){
        switchP(menuPanel);
    }

    public void loginFail(){

    }

    public void signupSuccess(){

    }

    public void signupFail(){

    }

    public void logoutSuccess(){
        switchP(loginPanel);
    }

    private void switchP(JPanel p){
        frame.setSize(p.getSize());
        frame.setContentPane(p);
        frame.repaint();
        frame.revalidate();
        p.requestFocus();
    }

    private void init(){
        frame = new JFrame();
        loginPanel=loginP();
        gamePanel=gameP();
        menuPanel=menuP();
        //TODO initialize all other panels

        switchP(loginPanel);
        frame.setVisible(true);
    }
}
