import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.Timer;

public class Checkers extends JPanel implements MouseListener {
    final int GAME_SIZE = 700;
    final int ROWS_COLS = 8;
    final int tileSize = GAME_SIZE / ROWS_COLS;
    Set<Warcab> warcaby = new HashSet<>();
    Set<Pair> neighbors;
    boolean showPossibleMoves = false;
    Pair mouseStart = new Pair(0, 0);
    Pair mouseEnd = new Pair(0, 0);

    String czyjaTura = "bialy";
    Timer timer;
    int timerGraczBialy = 0;
    int timerGraczCzarny = 0;

    Checkers() {
        this.addMouseListener(this);
        this.setPreferredSize(new Dimension(GAME_SIZE, GAME_SIZE));
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerCounter(), 0, 1000);
        createBoard();
    }

    void createBoard() {
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
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // ustalenie kolorów
        Color czarny_warcab = new Color(60, 60, 60);
        Color bialy_warcab = new Color(200, 200, 200);
        Color zolty = new Color(230, 230, 0, 85);
        Color biale_przezroczyste_pole = new Color(180, 180, 180, 90);

        // narysowanie planszy
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

        // narysowanie pionków i damki
        int offset = tileSize / 10;
        for (Warcab war : warcaby) {
            if (war.color.equals("czarny")) {
                g.setColor(czarny_warcab);
            } else {
                g.setColor(bialy_warcab);
            }
            g.fillOval((war.getPair().x * tileSize) + offset, (war.getPair().y * tileSize) + offset, tileSize - 2 * offset, tileSize - 2 * offset);

            if (war.damka) {
                g.setColor(Color.yellow);
                g.fillOval((war.getPair().x * tileSize) + (3 * offset), (war.getPair().y * tileSize) + (3 * offset), tileSize - (6 * offset), tileSize - (6 * offset));
            }
        }

        // narowanie możliwych ruchów
        if (showPossibleMoves && neighbors != null) {
            g.setColor(zolty);
            for (Pair cords : neighbors) {
                g.fillRect(cords.x * tileSize, cords.y * tileSize, tileSize, tileSize);
            }
        }

        // narysowanie pola za pokazaniem czyja jest tura
        g.setColor(biale_przezroczyste_pole);
        g.fillRect(GAME_SIZE / 2 - (int) Math.round(tileSize * 0.375), 0, (int) Math.round(tileSize * 0.75), (int) Math.round(tileSize * 0.75));

        // narysowanie pola za timerami
        g.fillRect(GAME_SIZE - 100, 0, 100, 50);
        g.fillRect(GAME_SIZE - 100, GAME_SIZE - 50, 100, 50);

        // narysowanie tego czyja tura
        if (czyjaTura.equals("bialy")) {
            g.setColor(bialy_warcab);
        } else {
            g.setColor(czarny_warcab);
        }
        g.fillOval(GAME_SIZE / 2 - tileSize / 4, tileSize / 8, tileSize / 2, tileSize / 2);

        // wypisanie timerów dla graczy
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
            if (war.getPair().x == col && war.getPair().y == row) {
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
            if (war.getPair().x == col && war.getPair().y == row && war.color.equals(czyjaTura)) {
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

        if (!neighbors.contains(mouseEnd)) {
            return;
        }

        for (Warcab war : warcaby) {
            if (war.getPair().equals(mouseStart)) {
                war.move(mouseEnd);
                if ((war.color.equals("bialy") && mouseEnd.y == 0) || (war.color.equals("czarny") && mouseEnd.y == ROWS_COLS - 1)) {
                    war.damka = true;
                }
                break;
            }
        }

        boolean zbite = false;
        int offX = mouseEnd.x > mouseStart.x ? -1 : 1;
        int offY = mouseEnd.y > mouseStart.y ? -1 : 1;
        int x = mouseEnd.x + offX;
        int y = mouseEnd.y + offY;
        Pair nowaPara = new Pair(x, y);
        for (Warcab war : warcaby) {
            if (war.getPair().equals(nowaPara)) {
                warcaby.remove(war);
                Warcab.changeNum(-1, war.color);
                zbite = true;
                break;
            }
        }
        if (!zbite) {
            nowaTura();
        }

        if (Warcab.isGameOver()) {
            if (Warcab.numOfWhites == 0) {
                System.out.println("Czarny wygrał");
            } else {
                System.out.println("Bialy wygrał");
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

    private Set<Pair> possibleMoves(Warcab war) {
        Set<Pair> neighbors = new HashSet<>();
        Set<Pair> wymuszoneBicie = new HashSet<>();
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

                int x = war.getPair().x + i * dx[j];
                int y = war.getPair().y + i * dy[j];

                if (!war.damka && ((war.color.equals("bialy") && y > war.getPair().y) || (war.color.equals("czarny") && y < war.getPair().y))) {
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
                    if (nowaDalszaPara.x >= 0 && nowaDalszaPara.x < ROWS_COLS && nowaDalszaPara.y >= 0 && nowaDalszaPara.y < ROWS_COLS) {
                        wymuszoneBicie.add(nowaDalszaPara);
                    }
                }
            }
        }
        return wymuszoneBicie.isEmpty() ? neighbors : wymuszoneBicie;
    }

    private class TimerCounter extends TimerTask {
        @Override
        public void run() {
            if (czyjaTura.equals("bialy")) {
                timerGraczBialy++;
            } else {
                timerGraczCzarny++;
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
}