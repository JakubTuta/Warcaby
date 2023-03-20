public class Warcab {
    int x, y;
    String color;
    Pair para;
    static int numOfWhites = 0;
    static int numOfBlacks = 0;

    Warcab(int x, int y, String color) {
        this.x = x;
        this.y = y;
        this.para = new Pair(x, y);
        this.color = color;
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
