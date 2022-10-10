package collections.iteration.enumerable;

import collections.iteration.enumerator.BiDirectionalEnumerator;

public interface BiDirectionalEnumerable<T> extends Enumerable<T> {
    @Override
    BiDirectionalEnumerator<T> enumerator();
    //TODO add BiDirectionalEnumeratorIterator
    // default BiDirectionalIterator<T> iterator();
}
