package tiler.core.fundamental.utils;

public class WrapInt {

    private int integer;

    public WrapInt(int integer) {
        this.integer = integer;
    }

    public int incrementInt() {
        return integer++;
    }

    public int getInt() {
        return integer;
    }

    public void setInt(int integer) {
        this.integer = integer;
    }

}
