package collections.adapters;

import java.util.AbstractList;
import java.util.Objects;

public class StringAsList extends AbstractList<Character> {
    private final String original;

    public StringAsList(String original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public Character get(int index) {
        return original.charAt(index);
    }

    public int size() {
        return original.length();
    }
}
