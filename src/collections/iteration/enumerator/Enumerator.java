package collections.iteration.enumerator;

public interface Enumerator<T> {
    boolean moveNext();

    T current();
}
