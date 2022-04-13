package collections.adapters;

import java.util.AbstractList;
import java.util.Objects;

public class CharArrayAsList extends AbstractList<Character> {
    private final char[] original;

    public CharArrayAsList(char[] original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public Character get(int index) {
        return original[index];
    }

    public int size() {
        return original.length;
    }
}
