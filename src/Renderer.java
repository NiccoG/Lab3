import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Renderer { //Class that renders the game's UI and memorizes current guess' letters

    private final JLabel [][] lMap = new JLabel[12][10];
    private final Game game;
    private JFrame frame;
    private JPanel menuPanel,gamePanel,loginPanel,statsPanel,sharePanel;
    public Renderer(Game game){   //TODO arguments
        this.game = game;
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

        JButton login = new JButton();
        login.setBackground(Color.BLACK);
        login.setBounds(100,170,100,80);
        login.setForeground(Color.WHITE);
        login.setText("Login");
        login.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        game.tryLogin(name.getText(),password.getPassword());
                    }
                }
        );

        JButton signup = new JButton();
        signup.setBackground(Color.BLACK);
        signup.setBounds(210,170,100,80);
        signup.setForeground(Color.WHITE);
        signup.setText("Sign Up");
        signup.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        game.trySignup(name.getText(),password.getPassword());
                    }
                }
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
        //TODO initialize all other panels

        switchP(loginPanel);
        frame.setVisible(true);
    }
}
