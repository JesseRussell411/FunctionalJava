package collections.persistent;

import collections.ArrayStack;
import collections.CollectionUtils;
import collections.iteration.adapters.ArrayIterator;
import collections.iteration.adapters.EnumeratorIterator;
import collections.iteration.adapters.ReversedEnumeratorIterator;
import collections.iteration.enumerable.BiDirectionalEnumerable;
import collections.iteration.enumerator.BiDirectionalEnumerator;
import org.jetbrains.annotations.NotNull;
import reference.pointers.Pointer;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PersistentTreeSet<T extends Comparable<T>> extends AbstractSet<T> implements BiDirectionalEnumerable<T>, java.io.Serializable {
    private final Node<T> root;
    private final int size;

    private PersistentTreeSet(Node<T> root, int size) {
        this.root = root;
        this.size = size;
        assert Assertions.correctSize(this);
    }

    public PersistentTreeSet() {
        this(null, 0);
    }

    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
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

    @Override
    public boolean containsAll(Collection<?> values) {
        for (final var value : values) {
            if (!contains(value)) return false;
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        Objects.requireNonNull(o);
        try {
            return get((T) o) != null;
        } catch (ClassCastException cce) {
            return false;
        }
    }

    public PersistentTreeSet<T> withMany(Iterator<T> values) {
        var result = new PersistentTreeSet<T>();

        while (values.hasNext()) {
            result = result.with(values.next());
        }

        return result;
    }

    public PersistentTreeSet<T> withMany(Iterable<T> values) {
        return withMany(values.iterator());
    }

    public PersistentTreeSet<T> withMany(Stream<T> valueStream) {
        return withMany(valueStream.iterator());
    }

    public PersistentTreeSet<T> withMany(T[] values) {
        return withMany(new ArrayIterator<>(values));
    }

    public PersistentTreeSet<T> with(@NotNull T value) {
        Objects.requireNonNull(value);
        // TODO add abort on duplicate instance
        final var size = new Pointer<>(size());
        return Assertions.assert_CorrectSize(
                new PersistentTreeSet<>(with(root, value, size), size.current));
    }

    private static <T extends Comparable<T>> Node<T> with(Node<T> n, T value, Pointer<Integer> size) {
        if (n == null) {
            size.current += 1;
            return new Node<>(null, null, value);
        } else if (value.compareTo(n.entry) < 0) {
            return new Node<>(
                    with(n.left, value, size),
                    n.right,
                    n.entry).balanced();
        } else if (value.compareTo(n.entry) > 0) {
            return new Node<>(
                    n.left,
                    with(n.right, value, size),
                    n.entry).balanced();
        } else {
            return new Node<>(
                    n.left,
                    n.right,
                    value).balanced();
        }
    }

    public T get(T value) {
        Objects.requireNonNull(value);
        return get(root, value);
    }

    private static <T extends Comparable<T>> T get(Node<T> n, T value) {
        if (n == null) {
            return null;
        } else if (value.compareTo(n.entry) < 0) {
            return get(n.left, value);
        } else if (value.compareTo(n.entry) > 0) {
            return get(n.right, value);
        } else {
            return n.entry;
        }
    }

    public PersistentTreeSet<T> withoutMany(Iterator<T> valueIterator) {
        var result = new PersistentTreeSet<T>();

        while (valueIterator.hasNext()) {
            result = result.without(valueIterator.next());
        }

        return Assertions.assert_CorrectSize(result);
    }

    public PersistentTreeSet<T> withoutMany(Stream<T> values) {
        return withoutMany(values.iterator());
    }

    public PersistentTreeSet<T> withoutMany(Iterable<T> values) {
        return withoutMany(values.iterator());
    }

    public PersistentTreeSet<T> withoutMany(T[] values) {
        return withoutMany(new ArrayIterator<>(values));
    }

    public PersistentTreeSet<T> without(T value) {
        Objects.requireNonNull(value);
        final var aborted = new Pointer<>(false);
        final var result = without(root, value, aborted);
        if (aborted.current) {
            return this;
        } else {
            return new PersistentTreeSet<>(result, size() - 1);
        }
    }


    private static <T extends Comparable<T>> Node<T> without(Node<T> n, T value, Pointer<Boolean> abort) {
        if (n == null) {
            abort.current = true;
            return null;
        } else if (value.compareTo(n.entry) < 0) {
            final var leftResult = without(n.left, value, abort);
            if (abort.current) {
                return null;
            } else {
                return new Node<>(leftResult, n.right, n.entry).balanced();
            }
        } else if (value.compareTo(n.entry) > 0) {
            final var rightResult = without(n.right, value, abort);
            if (abort.current) {
                return null;
            } else {
                return new Node<>(n.left, rightResult, n.entry).balanced();
            }
        } else {
            return removed(n);
        }
    }

    private static <T extends Comparable<T>> Node<T> removed(Node<T> n) {
        if (n.left == null && n.right == null) {
            // === no children ===
            return null;
        } else if (n.left == null) {
            // === one right child ===
            return n.right;
        } else if (n.right == null) {
            // === one left child ===
            return n.left;
        } else {
            // === two children ===
            // replace the value at this node with the largest value relative to its left child
            // by moving that value to this node.
            final var relativeLargest_ref = new Pointer<Node<T>>();
            final var newRight = extractLargestRelativeTo(n.right, relativeLargest_ref);
            return new Node<>(n.left, newRight, relativeLargest_ref.current.entry).balanced();
        }
    }

    private static <T extends Comparable<T>> Node<T> extractSmallestRelativeTo(Node<T> n, Pointer<Node<T>> extracted) {
        if (n.left != null) {
            return new Node<>(
                    extractLargestRelativeTo(n.left, extracted),
                    n.right,
                    n.entry);
        } else {
            extracted.current = n;
            return n.right;
        }
    }

    private static <T extends Comparable<T>> Node<T> extractLargestRelativeTo(Node<T> n, Pointer<Node<T>> extracted) {
        if (n.right != null) {
            return new Node<>(
                    n.left,
                    extractLargestRelativeTo(n.right, extracted),
                    n.entry);
        } else {
            extracted.current = n;
            return n.left;
        }
    }

    private static <T extends Comparable<T>> Node<T> balanced(Node<T> n) {
        if (n == null) return null;

        if (n.balanceFactor < -1) {
            if (balanceFactorOf(n.left) <= 0) {
                // left heavy with non-right heavy child
                return rotatedRight(n);
            } else {
                // left heavy with right heavy child
                return rotatedRight(new Node<>(
                        rotatedLeft(n.left),
                        n.right,
                        n.entry));
            }
        } else if (n.balanceFactor > 1) {
            if (balanceFactorOf(n.right) >= 0) {
                // right heavy with non-left heavy child
                return rotatedLeft(n);
            } else {
                // right heavy with left heavy child
                return rotatedLeft(new Node<>(
                        n.left,
                        rotatedRight(n.right),
                        n.entry));
            }
        } else return n;
    }

    private static <T extends Comparable<T>> Node<T> rotatedLeft(Node<T> n) {
        if (n != null && n.right != null) {
            return new Node<>(
                    new Node<>(
                            n.left,
                            n.right.left,
                            n.entry),
                    n.right.right,
                    n.right.entry);
        } else return n;
    }

    private static <T extends Comparable<T>> Node<T> rotatedRight(Node<T> n) {
        if (n != null && n.left != null) {
            return new Node<>(
                    n.left.left,
                    new Node<>(n.right,
                            n.left.right,
                            n.entry),
                    n.left.entry);
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

    private static class Node<T extends Comparable<T>> implements java.io.Serializable {
        final Node<T> left;
        final Node<T> right;
        @NotNull
        final T entry;
        // balance factor can't be greater than 3 or less than -3
        final byte balanceFactor;
        // depth can't be greater than log_2(2^31) = 31
        final byte depth;

        Node(Node<T> left, Node<T> right, @NotNull T entry) {
            assert entry != null;
            this.left = left;
            this.right = right;
            this.entry = entry;
            depth = (byte) (Math.max(depthOf(left), depthOf(right)) + 1);
            balanceFactor = (byte) (depthOf(right) - depthOf(left));
        }

        Node<T> balanced() {
            return PersistentTreeSet.balanced(this);
        }
    }


    @Override
    public BiDirectionalEnumerator<T> enumerator() {
        return enumerator(false);
    }

    public BiDirectionalEnumerator<T> enumerator(boolean reversed) {
        return new SelfEnumerator<>(root, reversed);
    }

    public Iterator<T> reversedIterator() {
        return new ReversedEnumeratorIterator<>(enumerator(true));
    }

    @Override
    public Iterator<T> iterator() {
        return new EnumeratorIterator<>(enumerator());
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(
                iterator(),
                size(),
                Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.NONNULL);
    }

    public Stream<T> stream(boolean parallel) {
        return StreamSupport.stream(spliterator(), parallel);
    }

    @Override
    public Stream<T> stream() {
        return stream(false);
    }

    @Override
    public Stream<T> parallelStream() {
        return stream(true);
    }

    private static class NodeEnumerator<T extends Comparable<T>> implements BiDirectionalEnumerator<Node<T>> {
        @NotNull
        final ArrayStack<Node<T>> location;
        boolean beforeStart;
        boolean afterEnd;

        NodeEnumerator(Node<T> root, boolean startAndEnd) {
            if (root != null) {
                location = new ArrayStack<>(root.depth);
                location.add(root);
                if (startAndEnd) {
                    drillDownRight();
                    afterEnd = true;
                    beforeStart = false;
                } else {
                    drillDownLeft();
                    beforeStart = true;
                    afterEnd = false;
                }
            } else {
                location = new ArrayStack<>(0);
            }
        }

        private void drillDownLeft() {
            while (location.peek().left != null) {
                location.add(location.peek().left);
            }
        }

        private void drillDownRight() {
            while (location.peek().right != null) {
                location.add(location.peek().right);
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
                location.add(location.peek().right);
                drillDownLeft();
            } else {
                Node<T> child;
                do {
                    child = location.pop();
                    if (location.isEmpty()) {
                        location.add(child);
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
                location.add(location.peek().left);
                drillDownRight();
            } else {
                Node<T> child;
                do {
                    child = location.pop();
                    if (location.isEmpty()) {
                        location.add(child);
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

    public static class SelfEnumerator<T extends Comparable<T>> implements BiDirectionalEnumerator<T> {
        private final NodeEnumerator<T> nodes;

        private SelfEnumerator(Node<T> root, boolean startAtEnd) {
            nodes = new NodeEnumerator<>(root, startAtEnd);
        }

        @Override
        public boolean movePrevious() {
            return nodes.movePrevious();
        }

        @Override
        public boolean moveNext() {
            return nodes.moveNext();
        }

        @Override
        public T current() {
            return nodes.current().entry;
        }
    }

    @Override
    public Object[] toArray() {
        final var result = new Object[size()];
        int i = 0;
        for (final var value : this) {
            result[i++] = value;
        }

        return result;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return CollectionUtils.toArray(this, a);
    }

    private static class Assertions {
        static <T extends Comparable<T>> int actualSize(Node<T> n) {
            if (n == null) return 0;

            int totalSize = 1;
            totalSize += actualSize(n.left);
            totalSize += actualSize(n.right);

            return totalSize;
        }

        static <T extends Comparable<T>> boolean correctSize(PersistentTreeSet<T> set) {
            Objects.requireNonNull(set);
            return set.size() == actualSize(set.root);
        }

        static <T extends Comparable<T>> PersistentTreeSet<T> assert_CorrectSize(PersistentTreeSet<T> set) {
            assert correctSize(set);
            return set;
        }
    }
}