import javax.swing.*;
public class GameWindow extends JFrame{
    GameWindow() {
        this.add(new Checkers());
        this.setTitle("Warcaby");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}
