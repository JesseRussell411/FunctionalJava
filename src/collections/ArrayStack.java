package collections;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * Last in first out data structure based on the {@link ArrayList} class.
 * <strong>Note that, just like {@link ArrayList}, this implementation is not synchronized.</strong>
 * @param <T>
 */
public class ArrayStack<T> extends ArrayList<T> implements Cloneable {
    public ArrayStack(int initialCapacity) {
        super(initialCapacity);
    }

    public ArrayStack() {
        super();
    }

    /**
     * @return The item at the depth from the top of the stack without removing the item.
     * @param depth How far into the stack to retrieve the item. 0 would return the top item for example.
     */
    public T peek(int depth) {
        if (isEmpty()) throw new EmptyStackException();
        return get(size() - depth - 1);
    }

    /**
     * @return The item at the top of the stack without removing it.
     */
    public T peek() {
        return peek(0);
    }

    /**
     * Removes the item at the top of the stack.
     * @return The item that was removed.
     */
    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        return remove(size() - 1);
    }

    /**
     * Pushes the item onto the stack.
     * Identical to add.
     * @param item The item to push onto the stack.
     */
    public void push(T item) {
        add(item);
    }

    @Override
    public ArrayStack<T> clone() {
        return (ArrayStack<T>) super.clone();
    }
}

