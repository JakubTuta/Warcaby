import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.io.*;
import java.net.*;
import static java.lang.System.exit;

public class GameWindow extends JFrame{
    GameWindow() {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        PrintWriter out = null;
        ObjectOutputStream outWarcaby = null;
        BufferedReader in = null;
        ObjectInputStream inWarcaby = null;

        System.out.println("Tworzenie hosta");
        try {
            serverSocket = new ServerSocket(2137);
        } catch (IOException e) {
            System.out.println("Nie udało się utworzyć serwera");
            System.out.println("Koniec programu");
            exit(1);
        }
        System.out.println("Utworzono hosta");

        System.out.println("Łączenie z klientem");
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println("Nie udało się połączyć z serwerem");
            System.out.println("Koniec programu");
            exit(1);
        }
        System.out.println("Połączono z klientem");

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            inWarcaby = new ObjectInputStream(clientSocket.getInputStream());
            out = new PrintWriter(clientSocket.getOutputStream(), false);
            outWarcaby = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Nie udało się wysłać danych");
            System.out.println("Koniec programu");
            exit(1);
        }

        System.out.println("xdd");
        this.add(new Checkers(out, outWarcaby, in, inWarcaby));
        this.setTitle("Warcaby");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}
