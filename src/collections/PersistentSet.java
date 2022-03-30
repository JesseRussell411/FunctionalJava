package collections;

import collections.iteration.ArrayIterator;
import collections.iteration.ReversedEnumeratorIterator;
import collections.iteration.enumerable.Enumerable;
import collections.iteration.enumerator.BiDirectionalEnumerator;
import reference.Pointer;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;
//TODO extract out to persistentTreeSet<T extends Comparable<T>> and back this with that. Use a wrapper type that compares its values by their hash codes.

public class PersistentSet<T> implements Enumerable<T> {
    private final Node<T> root;
    private final int size;

    private PersistentSet(Node<T> root, int size) {
        this.root = root;
        this.size = size;
    }

    public PersistentSet() {
        root = null;
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean containsAny(Iterator<T> valueIterator) {
        while (valueIterator.hasNext()) {
            if (contains(valueIterator.next())) return true;
        }
        return false;
    }

    public boolean containsAny(Stream<T> valueStream) {
        return containsAny(valueStream.iterator());
    }

    public boolean containsAny(Iterable<T> values) {
        return containsAny(values.iterator());
    }

    public boolean containsAny(T[] values) {
        return containsAny(new ArrayIterator<>(values));
    }

    public boolean containsAll(Iterator<T> valueIterator) {
        while (valueIterator.hasNext()) {
            if (!contains(valueIterator.next())) return false;
        }
        return true;
    }

    public boolean containsAll(Stream<T> values) {
        return containsAll(values.iterator());
    }

    public boolean containsAll(Iterable<T> values) {
        return containsAll(values.iterator());
    }

    public boolean containsAll(T[] values) {
        return containsAll(new ArrayIterator<>(values));
    }


    public boolean contains(T value) {
        final var hash = Objects.hashCode(value);
        return contains(root, value, hash);
    }

    private boolean contains(Node<T> n, T value, int hash) {
        if (n == null) {
            return false;
        } else if (hash < n.pivot) {
            return contains(n.left, value, hash);
        } else if (hash > n.pivot) {
            return contains(n.right, value, hash);
        } else {
            return n.entries.contains(value);
        }
    }

    public PersistentSet<T> withMany(Iterator<T> valueIterator) {
        var result = new PersistentSet<T>();

        while (valueIterator.hasNext()) {
            result = result.with(valueIterator.next());
        }

        return result;
    }

    public PersistentSet<T> withMany(Iterable<T> values) {
        return withMany(values.iterator());
    }

    public PersistentSet<T> withMany(Stream<T> valueStream) {
        return withMany(valueStream.iterator());
    }

    public PersistentSet<T> withMany(T[] values) {
        return withMany(new ArrayIterator<>(values));
    }


    public PersistentSet<T> with(T value) {
        final var size = new Pointer<>(size());
        return with(root, value, Objects.hashCode(value), size).wrap(size.current);
    }

    private static <T> Node<T> with(Node<T> n, T value, int hash, Pointer<Integer> size) {
        if (n == null) {
            size.current += 1;
            return new Node<>(null, null, PersistentList.of(value), hash);
        } else if (hash < n.pivot) {
            return new Node<>(
                    with(n.left, value, hash, size),
                    n.right,
                    n.entries,
                    n.pivot).balanced();
        } else if (hash > n.pivot) {
            return new Node<>(
                    n.left,
                    with(n.right, value, hash, size),
                    n.entries,
                    n.pivot).balanced();
        } else {
            final var newEntries = n.entries.replaceFirstOccurenceOrAppend(value);
            size.current += newEntries.size() - n.entries.size();
            return new Node<>(
                    n.left,
                    n.right,
                    newEntries,
                    n.pivot).balanced();
        }
    }

    public T get(T value) {
        return get(root, value, Objects.hashCode(value));
    }

    private static <T> T get(Node<T> n, T value, int hash) {
        if (n == null) {
            return null;
        } else if (hash < n.pivot) {
            return get(n.left, value, hash);
        } else if (hash > n.pivot) {
            return get(n.right, value, hash);
        } else {
            return n.entries.getFirstOccurence(value);
        }
    }

    public PersistentSet<T> withoutMany(Iterator<T> valueIterator) {
        var result = new PersistentSet<T>();

        while (valueIterator.hasNext()) {
            result = result.without(valueIterator.next());
        }

        return result;
    }

    public PersistentSet<T> withoutMany(Stream<T> values) {
        return withoutMany(values.iterator());
    }

    public PersistentSet<T> withoutMany(Iterable<T> values) {
        return withoutMany(values.iterator());
    }

    public PersistentSet<T> withoutMany(T[] values) {
        return withoutMany(new ArrayIterator<>(values));
    }

    public PersistentSet<T> without(T value) {
        final var aborted = new Pointer<>(false);
        final var result = without(root, value, Objects.hashCode(value), aborted);
        if (aborted.current) {
            return this;
        } else {
            return new PersistentSet<>(result, size() - 1);
        }
    }


    public static <T> Node<T> without(Node<T> n, T value, int hash, Pointer<Boolean> abort) {
        if (n == null) {
            abort.current = true;
            return null;
        } else if (hash < n.pivot) {
            final var leftResult = without(n.left, value, hash, abort);
            if (abort.current) {
                return null;
            } else {
                return new Node<>(leftResult, n.right, n.entries, n.pivot).balanced();
            }
        } else if (hash > n.pivot) {
            final var rightResult = without(n.right, value, hash, abort);
            if (abort.current) {
                return null;
            } else {
                return new Node<>(n.left, rightResult, n.entries, n.pivot).balanced();
            }
        } else {
            final var entriesWithout = n.entries.withoutFirstOccurrence(value);
            if (entriesWithout.size() == n.entries.size()) {
                abort.current = true;
                return null;
            } else if (entriesWithout.size() == 0) {
                return null;
            } else {
                return new Node<>(n.left, n.right, entriesWithout, n.pivot).balanced();
            }
        }
    }

    private static <T> Node<T> balanced(Node<T> n) {
        if (n == null) return null;

        if (n.balanceFactor < -1) {
            if (balanceFactorOf(n.left) <= 0) {
                // left heavy with left heavy child
                return rotatedRight(n);
            } else {
                // left heavy with right heavy child
                return rotatedRight(new Node<>(
                        rotatedLeft(n.left),
                        n.right,
                        n.entries,
                        n.pivot));
            }
        } else if (n.balanceFactor > 1) {
            if (balanceFactorOf(n.right) >= 0) {
                // right heavy with right heavy child
                return rotatedLeft(n);
            } else {
                // right heavy with left heavy child
                return rotatedLeft(new Node<>(
                        n.left,
                        rotatedRight(n.right),
                        n.entries,
                        n.pivot));
            }
        } else return n;
    }

    private static <T> Node<T> rotatedLeft(Node<T> n) {
        if (n != null && n.right != null) {
            return new Node<>(
                    new Node<>(
                            n.left,
                            n.right.left,
                            n.entries,
                            n.pivot),
                    n.right.right,
                    n.right.entries,
                    n.right.pivot);
        } else return n;
    }

    private static <T> Node<T> rotatedRight(Node<T> n) {
        if (n != null && n.left != null) {
            return new Node<>(
                    n.left.left,
                    new Node<>(n.right,
                            n.left.right,
                            n.entries,
                            n.pivot),
                    n.left.entries,
                    n.left.pivot);
        } else return n;
    }

    static byte depthOf(Node<?> n) {
        if (n == null) {
            return 0;
        } else {
            return n.depth;
        }
    }

    static int balanceFactorOf(Node<?> n) {
        if (n == null) {
            return 0;
        } else {
            return n.balanceFactor;
        }
    }

    private static class Node<T> {
        final Node<T> left;
        final Node<T> right;
        final PersistentList<T> entries;
        final int pivot;
        final byte balanceFactor;
        final byte depth;

        Node(Node<T> left, Node<T> right, PersistentList<T> entries, int pivot) {
            this.left = left;
            this.right = right;
            this.entries = entries;
            this.pivot = pivot;
            depth = (byte) (Math.max(depthOf(left), depthOf(right)) + 1);
            balanceFactor = (byte) (depthOf(right) - depthOf(left));
        }

        PersistentSet<T> wrap(int size) {
            return new PersistentSet<>(this, size);
        }

        Node<T> balanced() {
            return PersistentSet.balanced(this);
        }
    }


    @Override
    public BiDirectionalEnumerator<T> enumerator() {
        return enumerator(false);
    }

    public BiDirectionalEnumerator<T> enumerator(boolean startAtEnd) {
        return new SelfEnumerator<>(root, startAtEnd);
    }

    public Iterator<T> reversedIterator() {
        return new ReversedEnumeratorIterator<>(enumerator(true));
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), size(), Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    private static class NodeEnumerator<T> implements BiDirectionalEnumerator<Node<T>> {
        final Stack<Node<T>> location = new Stack<>();
        boolean beforeStart;
        boolean afterEnd;


        NodeEnumerator(Node<T> root, boolean startAndEnd) {
            if (root != null) {
                location.push(root);
                if (startAndEnd) {
                    drillDownRight();
                    afterEnd = true;
                    beforeStart = false;
                } else {
                    drillDownLeft();
                    beforeStart = true;
                    afterEnd = false;
                }
            }
        }

        private void drillDownLeft() {
            while (location.peek().left != null) {
                location.push(location.peek().left);
            }
        }

        private void drillDownRight() {
            while (location.peek().right != null) {
                location.push(location.peek().right);
            }
        }

        @Override
        public boolean moveNext() {
            if (location.isEmpty()) return false;
            if (beforeStart) {
                beforeStart = false;
                return true;
            }
            if (afterEnd) {
                return false;
            }
            if (location.peek().right != null) {
                location.push(location.peek().right);
                drillDownLeft();
            } else {
                Node<T> child;
                do {
                    child = location.pop();
                    if (location.isEmpty()) {
                        location.push(child);
                        drillDownRight();
                        afterEnd = true;
                        return false;
                    }
                } while (child == location.peek().right);
            }
            return true;
        }

        @Override
        public boolean movePrevious() {
            if (location.isEmpty()) return false;
            if (afterEnd) {
                afterEnd = false;
                return true;
            }
            if (beforeStart) {
                return false;
            }
            if (location.peek().left != null) {
                location.push(location.peek().left);
                drillDownRight();
            } else {
                Node<T> child;
                do {
                    child = location.pop();
                    if (location.isEmpty()) {
                        location.push(child);
                        drillDownLeft();
                        beforeStart = true;
                        return false;
                    }
                } while (child == location.peek().left);
            }
            return true;
        }

        @Override
        public Node<T> current() {
            if (beforeStart || afterEnd || location.isEmpty()) throw new NoSuchElementException();
            return location.peek();
        }
    }

    public static class SelfEnumerator<T> implements BiDirectionalEnumerator<T> {
        private final NodeEnumerator<T> nodes;
        private BiDirectionalEnumerator<T> localEnumerator = null;

        private SelfEnumerator(Node<T> root, boolean startAtEnd) {
            nodes = new NodeEnumerator<>(root, startAtEnd);
        }

        private boolean advanceNextNode() {
            do {
                if (nodes.moveNext()) {
                    localEnumerator = nodes.current().entries.enumerator();
                } else return false;
            } while (!localEnumerator.moveNext());
            return true;
        }

        private boolean advancePreviousNode() {
            do {
                if (nodes.movePrevious()) {
                    localEnumerator = nodes.current().entries.enumerator(true);
                } else return false;
            } while (!localEnumerator.movePrevious());
            return true;
        }

        @Override
        public boolean moveNext() {
            if (localEnumerator == null || !localEnumerator.moveNext()) return advanceNextNode();
            return true;
        }

        @Override
        public boolean movePrevious() {
            if (localEnumerator == null || !localEnumerator.movePrevious()) return advancePreviousNode();
            return true;
        }

        @Override
        public T current() {
            if (localEnumerator == null) throw new NoSuchElementException();
            return localEnumerator.current();
        }
    }

    public Object[] toArray() {
        final var result = new Object[size()];
        int i = 0;
        for (final var value : this) {
            result[i++] = value;
        }

        return result;
    }

    public <T1> T1[] toArray(T1[] a) {
        Objects.requireNonNull(a);
        final T1[] result = (a.length >= size())
                ? a
                : (T1[]) Array.newInstance(a.getClass().componentType(), size());

        int index = 0;

        // copy list contents
        for (final var item : this) {
            if (index < result.length) {
                ((Object[]) result)[index++] = item;
            } else break;
        }

        // pad with null
        while (index < result.length) {
            result[index++] = null;
        }

        return result;
    }
}