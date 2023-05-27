public class Warcab {
    String color;
    Pair para;
    boolean damka = false;
    static int numOfWhites = 0;
    static int numOfBlacks = 0;

    Warcab(int x, int y, String color) {
        this.para = new Pair(x, y);
        this.color = color;
    }

    void move(Pair nowaPara) {
        this.para.x = nowaPara.x;
        this.para.y = nowaPara.y;
    }

    static void changeNum(int num, String color) {
        if (color.equals("bialy")) {
            Warcab.numOfWhites += num;
        } else {
            Warcab.numOfBlacks += num;
        }
    }

    static boolean isGameOver() {
        return Warcab.numOfWhites == 0 || Warcab.numOfBlacks == 0;
    }

    Pair getPair() {
        return para;
    }
}