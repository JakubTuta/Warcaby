import java.util.Objects;

public class Pair {
    int x;
    int y;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Pair p)) {
            return false;
        }

        return p.x == this.x && p.y == this.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
