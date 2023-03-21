import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;
import java.net.*;
import static java.lang.System.exit;

public class Checkers  extends JPanel implements MouseListener{
    final int GAME_SIZE = 700;
    final int ROWS_COLS = 8;
    final int tileSize = GAME_SIZE / ROWS_COLS;
    ArrayList<Warcab> warcaby = new ArrayList<>();
    ArrayList<Pair> neighbors;
    boolean showPossibleMoves = false;
    Pair mouseStart = new Pair(0, 0);
    Pair mouseEnd = new Pair(0, 0);

    String czyjaTura = "bialy";
    String twojKolor = "bialy";
    Timer timer;
    int timerGraczBialy = 0;
    int timerGraczCzarny = 0;

    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    PrintWriter out;
    ObjectOutputStream outWarcaby;
    BufferedReader in;
    ObjectInputStream inWarcaby;

    Checkers() {
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

        this.addMouseListener(this);
        this.setPreferredSize(new Dimension(GAME_SIZE, GAME_SIZE));
        timer = new Timer();
        timer.scheduleAtFixedRate(new Task(), 0, 1000);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < ROWS_COLS; j++) {
                if ((i + j) % 2 != 0) {
                    warcaby.add(new Warcab(j, i, "czarny"));
                    Warcab.changeNum(1, "czarny");
                }
            }
        }

        for (int i = ROWS_COLS - 3; i < ROWS_COLS; i++) {
            for (int j = 0; j < ROWS_COLS; j++) {
                if ((i + j) % 2 != 0) {
                    warcaby.add(new Warcab(j, i, "bialy"));
                    Warcab.changeNum(1, "bialy");
                }
            }
        }

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            inWarcaby = new ObjectInputStream(clientSocket.getInputStream());
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            outWarcaby = new ObjectOutputStream(clientSocket.getOutputStream());

            out.println("Start");
            outWarcaby.writeObject(warcaby);
//            kolor przeciwnika
            if (twojKolor.equals("bialy")) {
                out.println("czarny");
            } else {
                out.println("bialy");
            }
            out.println(czyjaTura);
        } catch (IOException e) {
            System.out.println("Nie udało się wysłać danych");
            System.out.println("Koniec programu");
            exit(1);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Color czarny_warcab = new Color(60, 60, 60);
        Color bialy_warcab = new Color(200, 200, 200);
        Color zolty = new Color(230, 230, 0, 85);
        Color biale_przezroczyste_pole = new Color(180, 180, 180, 90);

        for (int i = 0; i < ROWS_COLS; i++) {
            for (int j = 0; j < ROWS_COLS; j++) {
                if ((i + j) % 2 == 0) {
                    g.setColor(Color.white);
                } else {
                    g.setColor(Color.black);
                }
                g.fillRect(i * tileSize, j * tileSize, tileSize, tileSize);
            }
        }

        int offset = tileSize / 10;
        for (Warcab war : warcaby) {
            if (war.color.equals("czarny")) {
                g.setColor(czarny_warcab);
            } else {
                g.setColor(bialy_warcab);
            }
            g.fillOval((war.x * tileSize) + offset, (war.y * tileSize) + offset, tileSize - 2 * offset, tileSize - 2 * offset);
        }

        if (showPossibleMoves && neighbors != null) {
            g.setColor(zolty);
            for (Pair cords : neighbors) {
                g.fillRect(cords.x * tileSize, cords.y * tileSize, tileSize, tileSize);
            }
        }

        g.setColor(biale_przezroczyste_pole);
        g.fillRect(GAME_SIZE / 2 - (int) Math.round(tileSize * 0.375), 0, (int) Math.round(tileSize * 0.75), (int) Math.round(tileSize * 0.75));

        g.fillRect(GAME_SIZE - 100, 0, 100, 50);
        g.fillRect(GAME_SIZE - 100, GAME_SIZE - 50, 100, 50);

        if (czyjaTura.equals("bialy")) {
            g.setColor(bialy_warcab);
        } else {
            g.setColor(czarny_warcab);
        }
        g.fillOval(GAME_SIZE / 2 - tileSize / 4, tileSize / 8, tileSize / 2, tileSize / 2);

        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 35));
        g.drawString(toMinutes(timerGraczCzarny), GAME_SIZE - 80, 38);
        g.drawString(toMinutes(timerGraczBialy), GAME_SIZE - 80, GAME_SIZE - 12);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int pos_x = e.getX();
        int pos_y = e.getY();
        int row = pos_y / tileSize;
        int col = pos_x / tileSize;

        for (Warcab war : warcaby) {
            if (war.x == col && war.y == row && war.color.equals(twojKolor)) {
                neighbors = possibleMoves(war);
                showPossibleMoves = true;
                repaint();
                return;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int pos_x = e.getX();
        int pos_y = e.getY();
        int row = pos_y / tileSize;
        int col = pos_x / tileSize;

        for (Warcab war : warcaby) {
            if (war.x == col && war.y == row && war.color.equals(czyjaTura) && czyjaTura.equals(twojKolor)) {
                mouseStart.set(col, row);
                neighbors = possibleMoves(war);
                showPossibleMoves = true;
                repaint();
                return;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (neighbors == null) {
            return;
        }
        showPossibleMoves = false;

        int pos_x = e.getX();
        int pos_y = e.getY();
        int row = pos_y / tileSize;
        int col = pos_x / tileSize;
        mouseEnd.set(col, row);

        if (neighbors.contains(mouseEnd)) {
            for (Warcab war : warcaby) {
                if (war.getPair().equals(mouseStart)) {
                    warcaby.remove(war);
                    warcaby.add(new Warcab(mouseEnd.x, mouseEnd.y, war.color));
                    nowaTura();
                    break;
                }
            }

            if (Math.abs(mouseStart.x - mouseEnd.x) == 2) {
                int midX = (mouseStart.x + mouseEnd.x) / 2;
                int midY = (mouseStart.y + mouseEnd.y) / 2;
                Pair midPair = new Pair(midX, midY);

                for (Warcab war : warcaby) {
                    if (war.getPair().equals(midPair)) {
                        warcaby.remove(war);
                        Warcab.changeNum(-1, war.color);
                        break;
                    }
                }

                if (Warcab.isGameOver()) {
                    if (Warcab.numOfWhites == 0) {
                        System.out.println("Biały wygrał");
                    } else {
                        System.out.println("Czarny wygrał");
                    }
                }

                printData();
            }
        }
        repaint();
    }

    private void nowaTura() {
        if (czyjaTura.equals("bialy")) {
            czyjaTura = "czarny";
        } else if (czyjaTura.equals("czarny")) {
            czyjaTura = "bialy";
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private ArrayList<Pair> possibleMoves(Warcab war) {

//        1.(x-1,y-1)           2.(x+1,y-1)
//                      (x,y)
//        3.(x-1,y+1)           4.(x+1,y+1)

        ArrayList<Pair> neighbors = new ArrayList<>();
        boolean dodajBlizej, dodajDalej;

        if (war.color.equals("bialy")) {
//        1. (x-1, y-1)
            dodajBlizej = true;
            dodajDalej = true;
            for (Warcab warcab : warcaby) {
                if (warcab.getPair().equals(new Pair(war.x - 1, war.y - 1))) {
                    dodajBlizej = false;
                    if (war.color.equals(warcab.color)) {
                        dodajDalej = false;
                        break;
                    }
                }
                if (warcab.getPair().equals(new Pair(war.x - 2, war.y - 2))) {
                    dodajDalej = false;
                }
            }
            if (dodajBlizej) {
                neighbors.add(new Pair(war.x - 1, war.y - 1));
            } else if (dodajDalej) {
                neighbors.add(new Pair(war.x - 2, war.y - 2));
            }

//        2. (x+1, y-1)
            dodajBlizej = true;
            dodajDalej = true;
            for (Warcab warcab : warcaby) {
                if (warcab.getPair().equals(new Pair(war.x + 1, war.y - 1))) {
                    dodajBlizej = false;
                    if (war.color.equals(warcab.color)) {
                        dodajDalej = false;
                        break;
                    }
                }
                if (warcab.getPair().equals(new Pair(war.x + 2, war.y - 2))) {
                    dodajDalej = false;
                }
            }
            if (dodajBlizej) {
                neighbors.add(new Pair(war.x + 1, war.y - 1));
            } else if (dodajDalej) {
                neighbors.add(new Pair(war.x + 2, war.y - 2));
            }
        } else {
//        3. (x-1, y+1)
            dodajBlizej = true;
            dodajDalej = true;
            for (Warcab warcab : warcaby) {
                if (warcab.getPair().equals(new Pair(war.x - 1, war.y + 1))) {
                    dodajBlizej = false;
                    if (war.color.equals(warcab.color)) {
                        dodajDalej = false;
                        break;
                    }
                }
                if (warcab.getPair().equals(new Pair(war.x - 2, war.y + 2))) {
                    dodajDalej = false;
                }
            }
            if (dodajBlizej) {
                neighbors.add(new Pair(war.x - 1, war.y + 1));
            } else if (dodajDalej) {
                neighbors.add(new Pair(war.x - 2, war.y + 2));
            }

//        4. (x+1, y+1)
            dodajBlizej = true;
            dodajDalej = true;
            for (Warcab warcab : warcaby) {
                if (warcab.getPair().equals(new Pair(war.x + 1, war.y + 1))) {
                    dodajBlizej = false;
                    if (war.color.equals(warcab.color)) {
                        dodajDalej = false;
                        break;
                    }
                }
                if (warcab.getPair().equals(new Pair(war.x + 2, war.y + 2))) {
                    dodajDalej = false;
                }
            }
            if (dodajBlizej) {
                neighbors.add(new Pair(war.x + 1, war.y + 1));
            } else if (dodajDalej) {
                neighbors.add(new Pair(war.x + 2, war.y + 2));
            }
        }
        return neighbors;
    }

    private class Task extends TimerTask {
        @Override
        public void run() {
            if (czyjaTura.equals("bialy")) {
                timerGraczBialy++;
            } else {
                timerGraczCzarny++;
            }
            repaint();

            try {
                String wejscie = in.readLine();
                if (!wejscie.equals("Start")) {
                    return;
                }
                warcaby = (ArrayList<Warcab>) inWarcaby.readObject();
            } catch (IOException e) {
                System.out.println("Nie udało się odczytać danych");
                System.out.println("Koniec programu");
                exit(1);
            } catch (ClassNotFoundException e) {
                System.out.println("Niepoprawne dane");
                System.out.println("Koniec programu");
                exit(1);
            }
        }
    }

    String toMinutes(int timer) {
        int minuty = timer / 60;
        int sekundy = timer - minuty * 60;
        if (sekundy < 10) {
            return minuty + ":0" + sekundy;
        } else {
            return minuty + ":" + sekundy;
        }
    }

    void printData() {
        try {
            out.println("Start");
            outWarcaby.writeObject(warcaby);
        } catch (IOException e) {
            System.out.println("Nie udało się wysłać danych");
            System.out.println("Koniec programu");
            exit(1);
        }
    }
}
