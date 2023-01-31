import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Renderer { //Class that renders the game's UI and memorizes current guess' letters

    private JLabel [][] lMap = new JLabel[12][10];
    private final Game game;
    public Renderer(Game game){   //TODO arguments
        this.game=game;
        init();
    }
    public void type(char c, int row, int col){
        if (c == '/'){
            lMap[row][col].setText("");
            return;
        }
        if (lMap[row][col].getText().equals(""))
            lMap[row][col].setText( String.valueOf( Character.toUpperCase(c) ) );
    }

    public void nextL(){  //TODO Data package class
        //TODO
    }
    private JLabel wLetter(int row, int col){
        JLabel l = new JLabel("",JLabel.CENTER);
        l.setOpaque(true);
        l.setBackground(Color.BLACK);
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        l.setBounds(30+col*55,30+row*55,50,50);
        lMap[row][col]=l;
        return l;
    }
    private JFrame init(){
        JFrame f= new JFrame();
        f.setLayout(null);
        f.setVisible(true);
        f.getContentPane().setBackground(Color.BLACK);
        f.setSize(1000,1000);

        for(int i=0; i<12; i++){
            for(int j=0;j<10;j++) {
                f.add(wLetter(i, j));
            }
        }

        f.addKeyListener(new KeyAdapter() {
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

        return f;
    }
}
