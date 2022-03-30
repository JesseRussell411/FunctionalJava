package collections;

import collections.iteration.ArrayIterator;
import collections.iteration.ReversedEnumeratorIterator;
import collections.iteration.enumerable.Enumerable;
import collections.iteration.enumerator.BiDirectionalEnumerator;
import collections.iteration.enumerator.Enumerator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Stream;

public class PersistentSet<T> implements Enumerable<T> {
    private final Node<T> root;


    private PersistentSet(Node<T> root) {
        this.root = root;
    }

    public PersistentSet() {
        root = null;
    }

    public int size() {
        return valueCountOf(root);
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
        return with(root, value, Objects.hashCode(value)).wrap();
    }

    private static <T> Node<T> with(Node<T> n, T value, int hash) {
        if (n == null) {
            return new Node<>(null, null, PersistentList.of(value), hash);
        } else if (hash < n.pivot) {
            return new Node<>(
                    with(n.left, value, hash),
                    n.right,
                    n.entries,
                    n.pivot).balanced();
        } else if (hash > n.pivot) {
            return new Node<>(
                    n.left,
                    with(n.right, value, hash),
                    n.entries,
                    n.pivot).balanced();
        } else {
            return new Node<>(
                    n.left,
                    n.right,
                    n.entries.replaceFirstOccurence(value),
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
        final var result = without(root, value, Objects.hashCode(value));
        if (result == null) {
            return this;
        } else {
            return result.wrap();
        }
    }


    public static <T> Node<T> without(Node<T> n, T value, int hash) {
        if (n == null) {
            return null;
        } else if (hash < n.pivot) {
            final var leftResult = without(n.left, value, hash);
            if (leftResult == null) {
                return null;
            } else {
                return new Node<>(leftResult, n.right, n.entries, n.pivot).balanced();
            }
        } else if (hash > n.pivot) {
            final var rightResult = without(n.left, value, hash);
            if (rightResult == null) {
                return null;
            } else {
                return new Node<>(n.left, rightResult, n.entries, n.pivot).balanced();
            }
        } else {
            final var entriesWithout = n.entries.withoutFirstOccurrence(value);
            if (entriesWithout.size() == n.entries.size()) {
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

    static int weightOf(Node<?> n) {
        if (n == null) {
            return 0;
        } else {
            return n.weight;
        }
    }

    static int balanceFactorOf(Node<?> n) {
        if (n == null) {
            return 0;
        } else {
            return n.balanceFactor;
        }
    }

    static int valueCountOf(Node<?> n) {
        if (n == null) {
            return 0;
        } else {
            return n.valueCount;
        }
    }

    private static class Node<T> {
        final Node<T> left;
        final Node<T> right;
        final PersistentList<T> entries;
        final int pivot;
        final int balanceFactor;
        final int weight;
        final int valueCount;

        Node(Node<T> left, Node<T> right, PersistentList<T> entries, int pivot) {
            this.left = left;
            this.right = right;
            this.entries = entries;
            this.pivot = pivot;
            weight = weightOf(left) + weightOf(right) + 1;
            balanceFactor = weightOf(left) + weightOf(right);
            valueCount = valueCountOf(left) + valueCountOf(right) + entries.size();
        }

        PersistentSet<T> wrap() {
            return new PersistentSet<>(this);
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
}