package collections;

import java.util.ArrayList;
import java.util.EmptyStackException;

public class ArrayStack<T> {
    private ArrayList<T> data;
    int headIndex = -1;

    public ArrayStack() {
        data = new ArrayList<>();
    }

    public ArrayStack(int initialCapacity) {
        data = new ArrayList<>(initialCapacity);
    }

    public void push(T item) {
        if (data.size() > size()) {
            data.set(++headIndex, item);
        } else {
            data.add(item);
            headIndex++;
        }
    }

    public T peek() {
        if (headIndex == -1) throw new EmptyStackException();
        return data.get(headIndex);
    }

    public T pop() {
        if (headIndex == -1) throw new EmptyStackException();
        return data.get(headIndex--);
    }

    public void clear() {
        headIndex = -1;
        data.clear();
    }

    public int size() {
        return headIndex + 1;
    }

    public boolean isEmpty() {
        return headIndex == -1;
    }
}
