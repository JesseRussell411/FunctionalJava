package collections.iteration.enumerator;

import collections.iteration.adapters.EnumeratorIterator;

import java.util.Iterator;

public interface BiDirectionalEnumerator<T> extends Enumerator<T> {
    boolean movePrevious();
}
