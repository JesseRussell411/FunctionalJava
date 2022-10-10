package collections;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EmptyStackException;

public class ArrayStack<T> {
    @NotNull
    private final ArrayList<T> data;

    public ArrayStack() {
        data = new ArrayList<>();
    }

    public ArrayStack(int initialCapacity) {
        data = new ArrayList<>(initialCapacity);
    }

    public void push(T item) {
        data.add(item);
    }

    public T peek(int depth) {
        if (data.isEmpty()) throw new EmptyStackException();
        return data.get(data.size() - depth - 1);
    }

    public T peek() {
        return peek(0);
    }

    public T pop() {
        if (data.isEmpty()) throw new EmptyStackException();
        return data.remove(data.size() - 1);
    }

    public void clear() {
        data.clear();
    }

    public int size() {
        return data.size();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public void trimToSize() {
        data.trimToSize();
    }

    public void ensureCapacity(int minCapacity) {
        data.ensureCapacity(minCapacity);
    }
}
