package collections;

import collections.iteration.ArrayIterator;
import collections.iteration.IterableUtils;
import collections.iteration.ListEnumeratorIterator;
import collections.iteration.enumerable.Enumerable;
import collections.iteration.enumerator.Enumerator;
import collections.iteration.enumerator.IndexedBiDirectionalEnumerator;
import collections.wrappers.ArrayAsList;
import utils.ArrayUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public PersistentList(Stream<T> itemStream) {
        root = fromIterator(itemStream.iterator());
        size = root.itemCount();
    }

    public PersistentList(Iterator<T> itemIterator) {
        root = fromIterator(itemIterator);
        size = root.itemCount();
    }

    public PersistentList(Iterable<T> items) {
        this(items.iterator());
    }

    public PersistentList(PersistentList<T> items) {
        root = items.root;
        size = items.size;
    }

    public PersistentList(T[] items) {
        root = fromPartitions(new ArrayAsList<>(ArrayUtils.partition(items, PARTITION_SIZE)));
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

    public IndexedBiDirectionalEnumerator<T> enumerator(int index) {
        return (IndexedBiDirectionalEnumerator<T>) new ItemEnumerator(root, index);
    }

    public IndexedBiDirectionalEnumerator<T> enumerator() {
        return enumerator(-1);
    }

    public ListIterator<T> iterator(int index) {
        ArrayUtils.requireIndexInBounds(0, index, size() + 1);
        return new ListEnumeratorIterator<>(
                (IndexedBiDirectionalEnumerator<T>) new ItemEnumerator(root, index - 1));
    }

    public ListIterator<T> iterator() {
        return iterator(0);
    }

    public Stream<T> stream(boolean parallel) {
        return StreamSupport.stream(spliterator(), parallel);
    }

    public Stream<T> stream() {
        return stream(true);
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

    public int indexOf(Object o) {
        int index = 0;
        for (final var item : this) {
            if (Objects.equals(o, item)) return index;
            index++;
        }
        return -1;
    }

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
        return iterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return iterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return get(fromIndex, toIndex - fromIndex);
    }

    // =================== list operations, single item =======================
    // extract
    public T get(int index) {
        return (T) get(index, root);
    }

    // replace
    public PersistentList<T> withSwap(int index, T item) {
        ArrayUtils.requireIndexInBounds(index, size());
        return new PersistentList<>(set(index, item, root));
    }

    // insert
    public PersistentList<T> withAddition(int index, T item) {
        ArrayUtils.requireIndexInBounds(index, size() + 1);
        return new PersistentList<>(add(index, item, root));
    }

    // remove
    public PersistentList<T> without(int index) {
        ArrayUtils.requireIndexInBounds(index, size());
        return new PersistentList<>(remove(index, index + 1, root));
    }

    // ================= list operations, multi item ==========================
    //extract
    public PersistentList<T> get(int start, int length) {
        ArrayUtils.requireRangeInBounds(start, length, size());
        return new PersistentList<>(get(start, start + length, root));
    }

    // replace
    public PersistentList<T> withReplacement(int index, Stream<T> itemStream) {
        return withReplacement(index, itemStream.iterator());
    }

    public PersistentList<T> withReplacement(int index, Iterable<T> items) {
        return withReplacement(index, items.iterator());
    }

    public PersistentList<T> withReplacement(int index, T[] items) {
        return withReplacement(index, new ArrayIterator<>(items));
    }

    public PersistentList<T> withReplacement(int index, Iterator<T> itemIterator) {
        ArrayUtils.requireIndexInBounds(index, size());
        return new PersistentList<>(withReplacement(index, itemIterator, root));
    }

    // insert
    public PersistentList<T> withInsertion(int index, Stream<T> itemStream) {
        return withInsertion(index, itemStream.iterator());
    }

    public PersistentList<T> withInsertion(int index, Iterable<T> items) {
        return withInsertion(index, items.iterator());
    }

    public PersistentList<T> withInsertion(int index, Iterator<T> itemIterator) {
        ArrayUtils.requireIndexInBounds(index, size() + 1);
        return new PersistentList<>(withInsertion(index, itemIterator, root));
    }

    public PersistentList<T> withInsertion(int index, T[] items) {
        return withInsertion(index, new ArrayAsList<>(items));
    }

    public PersistentList<T> withInsertion(int index, PersistentList<T> items) {
        ArrayUtils.requireIndexInBounds(index, size() + 1);
        return new PersistentList<>(withInsertion(index, items.root, root));
    }

    // remove
    public PersistentList<T> without(int start, int length) {
        ArrayUtils.requireRangeInBounds(start, length, size());
        return new PersistentList<>(remove(start, start + length, root));
    }

    // misc
    public PersistentList<T> sorted(Comparator<T> comparator) {
        return new PersistentList<>(stream().sorted(comparator));
    }


    // ============================== private utilities =================================
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

    private static Node set(int index, Object item, Node root) {
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

    private static Node withReplacement(int index, Iterator<?> itemIterator, Node root) {
        if (!itemIterator.hasNext()) return root;

        if (root instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return cleaned(new Branch(
                        withReplacement(index, itemIterator, branch.left),
                        withReplacement(0, itemIterator, branch.right)));
            } else {
                return cleaned(new Branch(
                        branch.left,
                        withReplacement(index - branch.left.itemCount(), itemIterator, branch.right)));
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

    private static Node withInsertion(int index, Node items, Node root) {
        if (root instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return cleaned(new Branch(
                        withInsertion(index, items, branch.left),
                        branch.right));
            } else {
                return cleaned(new Branch(
                        branch.left,
                        withInsertion(index - branch.left.itemCount(), items, branch.right)));
            }
        } else if (root instanceof Leaf leaf) {
            final var withProceeding = index < leaf.items.length
                    ? withInsertion(items.itemCount(), new ArrayIterator<>(leaf.items, index), items)
                    : items;

            return index > 0
                    ? withInsertion(0, new ArrayIterator<>(leaf.items, index), withProceeding)
                    : withProceeding;
        } else throw new IllegalStateException();
    }

    private static Node withInsertion(int index, Iterator<?> itemIterator, Node root) {
        if (root instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return cleaned(new Branch(
                        withInsertion(index, itemIterator, branch.left),
                        branch.right));
            } else {
                return cleaned(new Branch(
                        branch.left,
                        withInsertion(index - branch.left.itemCount(), itemIterator, branch.right)));
            }
        } else if (root instanceof Leaf leaf) {
            final var partitions = new ArrayList<Object[]>();
            Object[] newPartition = new Object[PARTITION_SIZE];
            int p = 0;

            // copy preceding items into partitions
            for (int i = 0; i < index; i++) {
                newPartition[p++] = leaf.items[i];
                if (p >= PARTITION_SIZE) {
                    partitions.add(newPartition);
                    newPartition = new Object[PARTITION_SIZE];
                    p = 0;
                }
            }

            // copy items from iterator
            while (itemIterator.hasNext()) {
                newPartition[p++] = itemIterator.next();
                if (p >= PARTITION_SIZE) {
                    partitions.add(newPartition);
                    newPartition = new Object[PARTITION_SIZE];
                    p = 0;
                }
            }

            // copy proceeding items
            for (int i = index; i < leaf.items.length; i++) {
                newPartition[p++] = leaf.items[i];
                if (p >= PARTITION_SIZE) {
                    partitions.add(newPartition);
                    p = 0;
                    if (i < leaf.items.length - 1) {
                        newPartition = new Object[PARTITION_SIZE];
                    }
                }
            }

            // trim final partition
            if (p != 0) {
                partitions.add(ArrayUtils.resize(newPartition, p));
            }

            return fromPartitions(partitions);
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
            return fromPartitions(new ArrayAsList<>(ArrayUtils.partition(
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

    private static Node fromIterable(Iterable<?> iterable) {
        return fromIterator(iterable.iterator());
    }

    private static Node fromIterator(Iterator<?> itemIterator) {
        if (!itemIterator.hasNext()) return EMPTY_LEAF;
        final var partitions = new ArrayList<Object[]>();
        Object[] newPartition = new Object[PARTITION_SIZE];
        int p = 0;

        while (itemIterator.hasNext()) {
            newPartition[p++] = itemIterator.next();
            if (p >= PARTITION_SIZE) {
                partitions.add(newPartition);
                newPartition = new Object[PARTITION_SIZE];
                p = 0;
            }
        }

        if (p != 0) {
            partitions.add(ArrayUtils.resize(newPartition, p));
        }

        return fromPartitions(partitions);
    }

    // sorting
    private static Iterator<Object> sortedIterator(Node root, Comparator<Object> comparator) {
        if (root instanceof Branch branch) {
            return IterableUtils.merge(
                    sortedIterator(branch.left, comparator),
                    sortedIterator(branch.right, comparator),
                    comparator);
        } else if (root instanceof Leaf leaf) {
            final var sorted = Arrays.copyOf(leaf.items, leaf.items.length);
            Arrays.sort(sorted, comparator);
            return new ArrayIterator<>(sorted);
        } else throw new IllegalStateException();
    }

    // ========= maintenance ============
    private static Node cleaned(Branch branch) {
        final var pruned = pruned(branch);
        if (!(pruned instanceof Branch prunedBranch)) return pruned;
        return balanced(prunedBranch);
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
    private interface Node extends Enumerable<Object> {
        int nodeCount();

        int itemCount();

        int leafCount();

        int depth();

        int balanceFactor();

        default int absoluteBalanceFactor() {
            return Math.abs(balanceFactor());
        }

        @Override
        default Enumerator<Object> enumerator() {
            return new ItemEnumerator(this);
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

        public ItemEnumerator(Node root, int index) {
            ArrayUtils.requireIndexInBounds(-1, index, root.itemCount() + 1);
            this.root = Objects.requireNonNull(root);
            leafEnumerator = initialize(index);
        }

        public ItemEnumerator(Node root) {
            this(root, -1);
        }

        private LeafEnumerator initialize(int initialIndex) {
            if (initialIndex <= -1) {
                this.index = -1;
                return new LeafEnumerator(root, false);
            }
            if (initialIndex >= root.itemCount()) {
                this.index = root.itemCount();
                return new LeafEnumerator(root, true);
            }

            final var location = new Stack<Node>();
            int locationIndex = 0;
            int indexInCurrentNode = initialIndex;

            Node currentNode = root;
            location.push(currentNode);


            while (currentNode instanceof Branch currentBranch) {
                if (indexInCurrentNode < currentBranch.left.itemCount()) {
                    currentNode = currentBranch.left;
                    location.push(currentNode);
                } else {
                    locationIndex += currentBranch.left.leafCount();
                    indexInCurrentNode -= currentBranch.left.itemCount();

                    currentNode = currentBranch.right;
                    location.push(currentNode);
                }
            }

            index = initialIndex;
            indexInLeaf = indexInCurrentNode;
            return new LeafEnumerator(location, locationIndex, root);
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
        private final Stack<Node> location;
        private int locationIndex;
        private final Node root;

        public LeafEnumerator(Node root, boolean startAndEnd) {
            this.root = Objects.requireNonNull(root);
            locationIndex = startAndEnd ? root.leafCount() : -1;
            location = new Stack<>();
        }

        public LeafEnumerator(Stack<Node> location, int locationIndex, Node root) {
            this.location = Objects.requireNonNull(location);
            this.locationIndex = locationIndex;
            this.root = Objects.requireNonNull(root);
        }

        private void drillDownLeft() {
            drillDown(false);
        }

        private void drillDownRight() {
            drillDown(true);
        }

        private void drillDown(boolean right) {
            Node current = location.peek();
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
            location.push(parent.left);

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
            location.push(parent.right);

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


    // ============================== unsupported interface methods ========================
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

    @Override
    public T set(int index, T element) {
        throw mutationException;
    }

    @Override
    public void add(int index, T element) {
        throw mutationException;
    }

    @Override
    public T remove(int index) {
        throw mutationException;
    }
}
