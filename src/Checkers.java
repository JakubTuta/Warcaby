import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.Timer;

import static java.lang.System.exit;

public class Checkers extends JPanel implements MouseListener{
    final int GAME_SIZE = 700;
    final int ROWS_COLS = 8;
    final int tileSize = GAME_SIZE / ROWS_COLS;
    Set<Warcab> warcaby = new HashSet<>();
    Set<Pair> neighbors;
    boolean showPossibleMoves = false;
    Pair mouseStart = new Pair(-1, -1);
    Pair mouseEnd = new Pair(-1, -1);

    String czyjaTura = "bialy";
    String twojKolor = "bialy";
    Timer timer;
    int timerGraczBialy = 0;
    int timerGraczCzarny = 0;

    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    PrintWriter out = null;
    BufferedReader in = null;

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
            exit(2);
        }
        System.out.println("Połączono z klientem");

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
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Start program");
            out.println(warcaby.size());
            for(Warcab war : warcaby) {
                out.println(war.getPair().x);
                out.println(war.getPair().y);
                out.println(war.color);
            }
//            kolor przeciwnika
            if (twojKolor.equals("bialy")) {
                out.println("czarny");
            } else {
                out.println("bialy");
            }
            out.println(czyjaTura);
            out.println(ROWS_COLS);
        } catch (IOException e) {
            System.out.println("Nie udało się wysłać danych");
            System.out.println("Koniec programu");
            exit(3);
        }

        this.addMouseListener(this);
        this.setPreferredSize(new Dimension(GAME_SIZE, GAME_SIZE));
        timer = new Timer();
        timer.scheduleAtFixedRate(new LiczenieCzasu(), 0, 1000);
        timer.scheduleAtFixedRate(new CzytanieBufora(), 0, 500);
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
            g.fillOval((war.x * tileSize) + offset, (war.y * tileSize) + offset, tileSize - (2 * offset), tileSize - (2 * offset));

            if (war.damka) {
                g.setColor(Color.yellow);
                g.fillOval((war.x * tileSize) + (3 * offset), (war.y * tileSize) + (3 * offset), tileSize - (6 * offset), tileSize - (6 * offset));
            }
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
                    Warcab nowyWar = new Warcab(mouseEnd.x, mouseEnd.y, war.color);
                    if ((war.color.equals("bialy") && mouseEnd.y == 0) || (war.color.equals("czarny") && mouseEnd.y == ROWS_COLS - 1) || war.damka) {
                        nowyWar.damka = true;
                    }
                    warcaby.remove(war);
                    warcaby.add(nowyWar);
                    break;
                }
            }

            int offX = mouseEnd.x > mouseStart.x ? -1 : 1;
            int offY = mouseEnd.y > mouseStart.y ? -1 : 1;
            int x = mouseEnd.x + offX;
            int y = mouseEnd.y + offY;
            Pair nowaPara = new Pair(x, y);
            for (Warcab war : warcaby) {
                if (war.getPair().equals(nowaPara)) {
                    warcaby.remove(war);
                    System.out.println("usuniete");
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
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private Set<Pair> possibleMoves(Warcab war) {
        Set<Pair> neighbors = new HashSet<>();
        ArrayList<Integer> doSkipa = new ArrayList<>();
        Pair nowaPara, nowaDalszaPara;
        boolean dodajBlizej, dodajDalej;

        int maxDroga = war.damka ? 7 : 1;
        int[] dx = {-1, 1, 1, -1};
        int[] dy = {-1, -1, 1, 1};

        for (int i = 1; i <= maxDroga; i++) {
            for (int j = 0; j < dx.length; j++) {
                if (doSkipa.contains(j)) {
                    continue;
                }

                int x = war.x + i * dx[j];
                int y = war.y + i * dy[j];

                if (!war.damka && ((war.color.equals("bialy") && y > war.y) || (war.color.equals("czarny") && y < war.y))) {
                    continue;
                }

                nowaPara = new Pair(x, y);
                nowaDalszaPara = new Pair(x + dx[j], y + dy[j]);
                dodajBlizej = true;
                dodajDalej = true;

                for (Warcab warcab : warcaby) {
                    if (warcab.getPair().equals(nowaPara)) {
                        dodajBlizej = false;
                        doSkipa.add(j);
                        if (war.color.equals(warcab.color)) {
                            dodajDalej = false;
                            break;
                        }
                    }
                    if (warcab.getPair().equals(nowaDalszaPara)) {
                        dodajDalej = false;
                    }
                }
                if (dodajBlizej) {
                    neighbors.add(nowaPara);
                } else if (dodajDalej) {
                    neighbors.add(nowaDalszaPara);
                }
            }
        }
        return neighbors;
    }

    private class LiczenieCzasu extends TimerTask {
        @Override
        public void run() {
            if (czyjaTura.equals("bialy")) {
                timerGraczBialy++;
            } else {
                timerGraczCzarny++;
            }
            out.println("Czas");
            out.println(timerGraczBialy);
            out.println(timerGraczCzarny);
            repaint();
        }
    }

    private class CzytanieBufora extends TimerTask {
        @Override
        public void run() {
            try {
                if(!in.ready()) {
                    repaint();
                    return;
                }

                String wejscie = in.readLine();
                if (wejscie.equals("Start")) {
                    int size = Integer.parseInt(in.readLine());
                    warcaby = new HashSet<>();
                    for (int i = 0; i < size; i++) {
                        int x = Integer.parseInt(in.readLine());
                        int y = Integer.parseInt(in.readLine());
                        String kolor = in.readLine();
                        String isDamka = in.readLine();
                        Warcab war = new Warcab(x, y, kolor);
                        if(isDamka.equals("true")) {
                            war.damka = true;
                        }
                        warcaby.add(war);
                    }
                    czyjaTura = twojKolor;
                } else if (wejscie.equals("Czas")) {
                    timerGraczBialy = Integer.parseInt(in.readLine());
                    timerGraczCzarny = Integer.parseInt(in.readLine());
                }

            } catch (IOException e) {
                System.out.println("Nie udało się odczytać danych");
                System.out.println("Koniec programu");
                exit(4);
            }
            repaint();
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
        if (twojKolor.equals("bialy")) {
            czyjaTura = "czarny";
        } else {
            czyjaTura = "bialy";
        }

        out.println("Start");
        out.println(warcaby.size());
        for(Warcab war : warcaby) {
            out.println(war.getPair().x);
            out.println(war.getPair().y);
            out.println(war.color);
            out.println(war.damka);
        }
    }
}
