package collections.iteration.enumerator;

public interface Enumerator<T> {
    /**
     * Move to the next element.
     *
     * @return If there was a next element.
     */
    boolean moveNext();

    /**
     * @return The current element.
     * @throws java.util.NoSuchElementException;
     */
    T current();
}
