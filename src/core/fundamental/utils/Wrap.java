package core.fundamental.utils;

/**
 * wrap a object to be passed by reference
 * Created by huson on 3/28/16.
 */

/**
 * wrap a object to be passed by reference
 *
 * @param <T>
 */
public class Wrap<T> {
    private T value;

    public Wrap(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
