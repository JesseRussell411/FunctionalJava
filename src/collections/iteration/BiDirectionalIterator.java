package collections.iteration;

import java.util.Iterator;

public interface BiDirectionalIterator<T> extends Iterator<T> {
    T previous();

    boolean hasPrevious();
}
