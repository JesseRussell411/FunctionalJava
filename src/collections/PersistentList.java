package collections;

import collections.iteration.ListEnumeratorIterator;
import collections.iteration.enumerator.BiDirectionalEnumerator;
import collections.iteration.enumerator.IndexedBiDirectionalEnumerator;
import utils.ArrayUtils;

import java.lang.reflect.Array;
import java.util.*;

public class PersistentList<T> implements List<T> {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final Leaf EMPTY_LEAF = new Leaf(EMPTY_ARRAY);
    private static final int PARTITION_SIZE = 32;
    private static final UnsupportedOperationException mutationException = new UnsupportedOperationException("This collection is immutable.");

    private final Node root;
    private final int size;

    private PersistentList(Node root) {
        this.root = root;
        size = root.itemCount();
    }

    public int size() {
        return size;
    }

    // interface compliance
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (final var item : this) {
            if (Objects.equals(o, item)) return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c);
        for (final var item : c) {
            if (!contains(item)) return false;
        }

        return true;
    }

    @Override
    public ListIterator<T> iterator() {
        return new ListEnumeratorIterator<>(
                (IndexedBiDirectionalEnumerator<T>) new ItemEnumerator(root, false));
    }

    public ListIterator<T> iteratorAtEnd() {
        return new ListEnumeratorIterator<>(
                (IndexedBiDirectionalEnumerator<T>) new ItemEnumerator(root, true));
    }

    @Override
    public Object[] toArray() {
        final var result = new Object[size()];
        int index = 0;
        for (final var item : this) {
            result[index++] = item;
        }
        return result;
    }

    @Override
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

    @Override
    public int indexOf(Object o) {
        int index = 0;
        for (final var item : this) {
            if (Objects.equals(o, item)) return index;
            index++;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int index = 0;
        int lastIndex = -1;
        for (final var item : this) {
            if (Objects.equals(o, item)) lastIndex = index;
            index++;
        }
        return lastIndex;
    }

    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return null;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return null;
    }

    // extract
    // insert
    // remove
    // replace

    // =================== list operations, single item =======================
    // extract
    public T get(int index) {
        return get(index, root);
    }

    // replace
    public PersistentList<T> set(int index, T item) {
        ArrayUtils.requireIndexInBounds(index, size());
        return new PersistentList<>(set(index, item, root));
    }

    // insert
    public PersistentList<T> add(int index, T item) {
        ArrayUtils.requireIndexInBounds(index, size() + 1);
        return new PersistentList<>(add(index, item, root));
    }

    // remove
    public PersistentList<T> remove(int index) {
        ArrayUtils.requireIndexInBounds(index, size());
        return new PersistentList<>(remove(index, index + 1, root));
    }

    // misc
    public PersistentList<T> put(T item) {
        return add(size(), item);
    }

    public PersistentList<T> pop() {
        if (size() <= 0) return this;
        return remove(size() - 1);
    }

    public PersistentList<T> push(T item) {
        return add(0, item);
    }

    public PersistentList<T> pull() {
        if (size() <= 0) return this;
        return remove(0);
    }

    public T head() {
        if (size() <= 0) return null;
        return get(0);
    }

    public T tail() {
        if (size() <= 0) return null;
        return get(size() - 1);
    }

    // ================= list operations, multi item ==========================
    public PersistentList<T> get(int start, int length) {
        ArrayUtils.requireRangeInBounds(start, length, size());
        return new PersistentList<>(get(start, start + length, root));
    }

    public PersistentList<T> replace(int index, Iterable<T> items) {
        ArrayUtils.requireIndexInBounds(index, size());
        return new PersistentList<>(replace(index, items.iterator(), root));
    }

    public PersistentList<T> repace(int index, T[] items) {
        return replace(index, List.of(items));
    }

    // private utilities
    private static Object get(int index, Node root) {
        if (root instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return get(index, branch.left);
            } else {
                return get(index - branch.left.itemCount(), branch.right);
            }
        } else if (root instanceof Leaf leaf) {
            return leaf.items[index];
        } else throw new IllegalStateException();
    }

    private static Node get(int start, int end, Node root) {
        if (root instanceof Branch branch) {
            final var left = branch.left;
            final var right = branch.right;

            if (start < left.itemCount()) {
                if (end <= left.itemCount()) {
                    return cleaned(new Branch(
                            get(start, end, left),
                            right));
                } else {
                    return cleaned(new Branch(
                            get(start, left.itemCount(), left),
                            get(0, end - left.itemCount(), right)));
                }
            } else {
                return cleaned(new Branch(
                        left,
                        get(start - left.itemCount(), end - left.itemCount(), right)));
            }
        } else if (root instanceof Leaf leaf) {
            return new Leaf(ArrayUtils.get(leaf.items, start, end - start));
        } else throw new IllegalStateException();
    }

    private static Node set(int index, T item, Node root) {
        if (root instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return cleaned(new Branch(
                        set(index, item, branch.left),
                        branch.right));
            } else {
                return cleaned(new Branch(
                        branch.left,
                        set(index - branch.left.itemCount(), item, branch.right)));
            }
        } else if (root instanceof Leaf leaf) {
            return new Leaf(ArrayUtils.set(leaf.items, index, item));
        } else throw new IllegalStateException();
    }

    private Node replace(int index, Iterator<?> itemIterator, Node root) {
        if (!itemIterator.hasNext()) return root;

        if (root instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return cleaned(new Branch(
                        replace(index, itemIterator, branch.left),
                        replace(0, itemIterator, branch.right)));
            } else {
                return cleaned(new Branch(
                        branch.left,
                        replace(index - branch.left.itemCount(), itemIterator, branch.right)));
            }
        } else if (root instanceof Leaf leaf) {
            final var newItems = Arrays.copyOf(leaf.items, leaf.items.length);
            int i = index;
            while (i < newItems.length && itemIterator.hasNext()) {
                newItems[i++] = itemIterator.next();
            }
            return new Leaf(newItems);
        } else throw new IllegalStateException();
    }

    private Node insert(int index, Iterator<?> itemIterator, Node root) {
        if (root instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return cleaned(new Branch(
                        insert(index, itemIterator, branch.left),
                        branch.right));
            } else {
                return cleaned(new Branch(
                        branch.left,
                        insert(index - branch.left.itemCount(), itemIterator, branch.right)));
            }
        } else if (root instanceof Leaf leaf) {
            final var partitions = new ArrayList<Object[]>();
            Object[] newPartition = new Object[PARTITION_SIZE];

            int p = 0;

            for (int i = 0; i < index; i++) {
                newPartition[p++] = leaf.items[i];
                if (p >= PARTITION_SIZE) {
                    partitions.add(newPartition);
                    newPartition = new Object[PARTITION_SIZE];
                    p = 0;
                }
            }

            while (itemIterator.hasNext()) {
                newPartition[p++] = itemIterator.next();
                if (p >= PARTITION_SIZE) {
                    partitions.add(newPartition);
                    newPartition = new Object[PARTITION_SIZE];
                    p = 0;
                }
            }

            for (int i = index; i < leaf.items.length; i++) {
                newPartition[p++] = leaf.items[i];
                if (p >= PARTITION_SIZE) {
                    partitions.add(newPartition);
                    if (i < leaf.items.length - 1) {
                        newPartition = new Object[PARTITION_SIZE];
                        p = 0;
                    }
                }
            }

            if (p < PARTITION_SIZE) {
                partitions.set(
                        partitions.size() - 1,
                        Arrays.copyOf(
                                partitions.get(partitions.size() - 1),
                                p));
            }

            return fromPartitions(partitions);

//            final var precedingRemainder = index % PARTITION_SIZE;
//            final var precedingPartitions
//                    = (index / PARTITION_SIZE)
//                    + ((precedingRemainder > 0) ? 1 : 0);
//
//            int p = PARTITION_SIZE;
//            // TODO redo
//            for (int i = 0; i < precedingPartitions; i++) {
//                for (p = 0; p < PARTITION_SIZE; p++) {
//                    final var l = i * PARTITION_SIZE + p;
//                    if (l >= leaf.items.length) break;
//                    newPartition[p] = leaf.items[l];
//                }
//
//                if (p >= PARTITION_SIZE) {
//                    partitions.add(newPartition);
//                    newPartition = new Object[PARTITION_SIZE];
//                }
//            }
//
//            for (; p < PARTITION_SIZE; p++) {
//                if (itemIterator.hasNext()) {
//                    newPartition[p] = itemIterator.next();
//                } else break;
//            }
//
//            if (p >= PARTITION_SIZE) {
//                partitions.add(newPartition);
//                newPartition = new Object[PARTITION_SIZE];
//                p = 0;
//            }
//
//            while (itemIterator.hasNext()) {
//                newPartition[p++] = itemIterator.next();
//
//                if (p >= PARTITION_SIZE) {
//                    partitions.add(newPartition);
//                    newPartition = new Object[PARTITION_SIZE];
//                    p = 0;
//                }
//            }


        } else throw new IllegalStateException();
    }

    private static Node add(int index, Object item, Node root) {
        if (root instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return cleaned(new Branch(
                        add(index, item, branch.left),
                        branch.right));
            } else {
                return cleaned(new Branch(
                        branch.left,
                        add(index - branch.left.itemCount(), item, branch.right)));
            }
        } else if (root instanceof Leaf leaf) {
            final var newItems = ArrayUtils.add(leaf.items, index, item);
            return fromPartitions(List.of(ArrayUtils.partition(
                    newItems,
                    PARTITION_SIZE)));
        } else throw new IllegalStateException();
    }

    private static Node remove(int start, int end, Node root) {
        if (root instanceof Branch branch) {
            final var left = branch.left;
            final var right = branch.right;

            if (start < left.itemCount()) {
                if (end <= left.itemCount()) {
                    return cleaned(new Branch(
                            remove(start, end, left),
                            right));
                } else {
                    return cleaned(new Branch(
                            remove(start, left.itemCount(), left),
                            remove(0, end - left.itemCount(), right)));
                }
            } else {
                return cleaned(new Branch(
                        left,
                        remove(
                                start - left.itemCount(),
                                end - left.itemCount(),
                                right)));
            }
        } else if (root instanceof Leaf leaf) {
            final var length = end - start;
            return new Leaf(ArrayUtils.remove(leaf.items, start, length));
        } else throw new IllegalStateException();
    }

    private static Node fromPartitions(List<Object[]> partitions) {
        return fromPartitions(partitions, 0, partitions.size());
    }

    private static Node fromPartitions(List<Object[]> partitions, int start, int length) {
        if (length > 1) {
            final var rightPortion = length / 2;
            final var leftPortion = length - rightPortion;
            return new Branch(
                    fromPartitions(partitions, start, leftPortion),
                    fromPartitions(partitions, start + leftPortion, rightPortion));
        } else if (length == 1) {
            return new Leaf(partitions.get(start));
        } else {
            return EMPTY_LEAF;
        }
    }

    // ========= maintenance ============
    private static Node cleaned(Node node) {
        if (node instanceof Branch branch) {
            final var pruned = pruned(branch);
            if (pruned instanceof Branch prunedBranch) {
                return balanced(prunedBranch);
            } else return pruned;
        } else return node;
    }

    private static Node pruned(Branch branch) {
        if (branch.left.itemCount() == 0) return branch.right;
        if (branch.right.itemCount() == 0) return branch.left;
        return branch;
    }

    private static Branch balanced(Branch branch) {
        Branch result = branch;

        for (int loopCount = 0; loopCount < branch.nodeCount(); ++loopCount) {
            if (result.balanceFactor() < -1) {
                final var rotatedRight = rotatedRight(branch);
                if (rotatedRight.absoluteBalanceFactor() < result.absoluteBalanceFactor()) {
                    result = rotatedRight;
                    continue;
                }
            } else if (result.balanceFactor() > 1) {
                final var rotatedLeft = rotatedLeft(branch);
                if (rotatedLeft.absoluteBalanceFactor() < result.absoluteBalanceFactor()) {
                    result = rotatedLeft;
                    continue;
                }
            }
            break;
        }
        return result;
    }

    private static Branch rotatedLeft(Branch branch) {
        if (branch.right instanceof Branch right) {
            return new Branch(
                    new Branch(
                            branch.left,
                            right.left),
                    right.right);
        } else {
            return branch;
        }
    }

    private static Branch rotatedRight(Branch branch) {
        if (branch.left instanceof Branch left) {
            return new Branch(
                    left.left,
                    new Branch(
                            left.right,
                            branch.right));
        } else {
            return branch;
        }
    }

    // ========================= inner classes ====================================
    private interface Node {
        int nodeCount();

        int itemCount();

        int leafCount();

        int depth();

        int balanceFactor();

        default int absoluteBalanceFactor() {
            return Math.abs(balanceFactor());
        }
    }

    private static class Branch implements Node {
        final Node left;
        final Node right;
        final int nodeCount;
        final int itemCount;
        final int leafCount;
        final int depth;
        final int balanceFactor;

        public Branch(Node left, Node right) {
            this.left = left;
            this.right = right;
            nodeCount = left.nodeCount() + right.nodeCount() + 1;
            itemCount = left.itemCount() + right.itemCount();
            leafCount = left.leafCount() + right.leafCount();
            depth = Math.max(left.depth(), right.depth()) + 1;
            balanceFactor = right.depth() - left.depth();
        }

        @Override
        public int nodeCount() {
            return nodeCount;
        }

        @Override
        public int itemCount() {
            return itemCount;
        }

        @Override
        public int leafCount() {
            return leafCount;
        }

        @Override
        public int depth() {
            return depth;
        }

        @Override
        public int balanceFactor() {
            return balanceFactor;
        }
    }

    private static class Leaf implements Node {
        final Object[] items;

        public Leaf(Object[] items) {
            this.items = items;
        }

        @Override
        public int nodeCount() {
            return 1;
        }

        @Override
        public int itemCount() {
            return items.length;
        }

        @Override
        public int leafCount() {
            return 1;
        }

        @Override
        public int depth() {
            return 1;
        }

        @Override
        public int balanceFactor() {
            return 0;
        }

        @Override
        public int absoluteBalanceFactor() {
            return 0;
        }
    }

    // iterators
    private static class ItemEnumerator implements IndexedBiDirectionalEnumerator<Object> {
        private final LeafEnumerator leafEnumerator;
        private final Node root;
        private int index;
        private int indexInLeaf;

        public ItemEnumerator(Node root, boolean startAtEnd) {
            this.root = root;
            this.leafEnumerator = new LeafEnumerator(root, startAtEnd);
            index = startAtEnd ? root.itemCount() : -1;
        }

        @Override
        public boolean movePrevious() {
            if (index == root.itemCount()) {
                index = root.itemCount() - 1;
                if (leafEnumerator.movePrevious()) {
                    indexInLeaf = leafEnumerator.current().itemCount() - 1;
                    return true;
                } else return false;
            }
            final var currentLeaf = leafEnumerator.current();
            if (currentLeaf == null) return false;

            if (indexInLeaf > 0) {
                indexInLeaf--;
                index--;
                return true;
            } else if (leafEnumerator.movePrevious()) {
                indexInLeaf = leafEnumerator.current().itemCount() - 1;
                index--;
                return true;
            } else {
                index--;
                return false;
            }
        }

        @Override
        public boolean moveNext() {
            if (index == -1) {
                index = 0;
                if (leafEnumerator.moveNext()) {
                    indexInLeaf = 0;
                    return true;
                } else return false;
            }
            final var currentLeaf = leafEnumerator.current();
            if (currentLeaf == null) return false;

            if (indexInLeaf < currentLeaf.itemCount() - 1) {
                indexInLeaf++;
                index++;
                return true;
            } else if (leafEnumerator.moveNext()) {
                indexInLeaf = 0;
                index++;
                return true;
            } else {
                index++;
                return false;
            }
        }

        @Override
        public Object current() {
            final var currentLeaf = leafEnumerator.current();
            if (currentLeaf != null) {
                return currentLeaf.items[indexInLeaf];
            } else return null;
        }

        @Override
        public int currentIndex() {
            return index;
        }
    }

    private static class LeafEnumerator implements IndexedBiDirectionalEnumerator<Leaf> {
        private final Stack<Node> location = new Stack<>();
        private int locationIndex;
        private final Node root;

        public LeafEnumerator(Node root, boolean startAndEnd) {
            this.root = Objects.requireNonNull(root);
            locationIndex = startAndEnd ? root.leafCount() : -1;
        }

        private void drillDownLeft() {
            drillDown(false);
        }

        private void drillDownRight() {
            drillDown(true);
        }

        private void drillDown(boolean right) {
            Node current = location.peek();
            location.push(current);
            while (current instanceof Branch branch) {
                if (right) {
                    current = branch.right;
                    location.push(current);
                } else {
                    current = branch.left;
                    location.push(current);
                }
            }
        }

        private void moveLeft() {
            if (locationIndex <= 0) {
                location.clear();
                locationIndex = -1;
                return;
            }
            if (locationIndex > (root.leafCount() - 1)) {
                location.push(root);
                drillDownRight();
                locationIndex = root.leafCount() - 1;
                return;
            }

            Node child;
            Branch parent;
            do {
                child = location.pop();
                parent = (Branch) location.peek();
            } while (child == parent.left);

            drillDownRight();
            locationIndex--;
        }

        private void moveRight() {
            if (locationIndex >= (root.leafCount() - 1)) {
                location.clear();
                locationIndex = root.leafCount();
                return;
            }
            if (locationIndex < 0) {
                location.push(root);
                drillDownLeft();
                locationIndex = 0;
                return;
            }

            Node child;
            Branch parent;
            do {
                child = location.pop();
                parent = (Branch) location.peek();
            } while (child == parent.right);

            drillDownLeft();
            locationIndex++;
        }

        @Override
        public boolean moveNext() {
            if (locationIndex < (root.leafCount() - 1)) {
                moveRight();
                return true;
            } else return false;
        }

        @Override
        public boolean movePrevious() {
            if (locationIndex > 0) {
                moveLeft();
                return true;
            } else return false;
        }

        @Override
        public Leaf current() {
            if (location.isEmpty()) {
                return null;
            } else {
                return (Leaf) location.peek();
            }
        }

        public int currentIndex() {
            return locationIndex;
        }
    }


    // ============================== interface compliance ========================
    @Override
    public boolean add(T t) {
        throw mutationException;
    }

    @Override
    public boolean remove(Object o) {
        throw mutationException;
    }


    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw mutationException;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw mutationException;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw mutationException;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw mutationException;
    }

    @Override
    public void clear() {
        throw mutationException;
    }
}
