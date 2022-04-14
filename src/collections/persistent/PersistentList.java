package collections.persistent;

import collections.adapters.ArrayAsList;
import collections.iteration.IterableUtils;
import collections.iteration.adapters.ArrayIterator;
import collections.iteration.adapters.ListEnumeratorIterator;
import collections.iteration.adapters.ReversedEnumeratorIterator;
import collections.iteration.enumerable.Enumerable;
import collections.iteration.enumerable.IndexedBiDirectionalEnumerable;
import collections.iteration.enumerator.Enumerator;
import collections.iteration.enumerator.IndexedBiDirectionalEnumerator;
import collections.records.ListRecord;
import memoization.pure.lazy.SoftLazy;
import org.jetbrains.annotations.NotNull;
import utils.ArrayUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PersistentList<T> extends AbstractList<T> implements List<T>, IndexedBiDirectionalEnumerable<T>, java.io.Serializable {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final Leaf EMPTY_LEAF = new Leaf(EMPTY_ARRAY);
    private static final int PARTITION_SIZE = 32;

    @NotNull
    private final Node root;

    private PersistentList(@NotNull Node root) {
        this.root = root;
    }

    public PersistentList() {
        root = EMPTY_LEAF;
    }

    public PersistentList(Stream<T> itemStream) {
        root = fromIterator(itemStream.iterator());
    }

    public PersistentList(Iterator<T> itemIterator) {
        root = fromIterator(itemIterator);
    }

    public PersistentList(Iterable<T> items) {
        this(items.iterator());
    }

    public PersistentList(PersistentList<T> items) {
        root = items.root;
    }

    public PersistentList(T[] items) {
        root = fromPartitions(new ArrayAsList<>(ArrayUtils.partition(items, PARTITION_SIZE)));
    }

    // factories
    @SafeVarargs
    public static <T> PersistentList<T> of(T... items) {
        return new PersistentList<>(items);
    }

    // interface compliance
    // TODO write custom forking spliterator
    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(this, Spliterator.IMMUTABLE);
    }

    @Override
    public int size() {
        return root.itemCount();
    }

    public IndexedBiDirectionalEnumerator<T> enumerator(boolean startAtEnd) {
        if (startAtEnd) {
            return (IndexedBiDirectionalEnumerator<T>) new ItemEnumerator(root, size());
        } else {
            return (IndexedBiDirectionalEnumerator<T>) new ItemEnumerator(root, 0);
        }
    }

    public IndexedBiDirectionalEnumerator<T> enumerator(int index) {
        return (IndexedBiDirectionalEnumerator<T>) new ItemEnumerator(root, index);
    }

    @Override
    public IndexedBiDirectionalEnumerator<T> enumerator() {
        return enumerator(-1);
    }

    public ListIterator<T> iterator(int index) {
        ArrayUtils.requireIndexInBounds(0, index, size() + 1);
        return new ListEnumeratorIterator<>(
                (IndexedBiDirectionalEnumerator<T>) new ItemEnumerator(root, index - 1));
    }

    public Iterator<T> reversedIterator() {
        return new ReversedEnumeratorIterator<>(enumerator(true));
    }

    @Override
    public ListIterator<T> iterator() {
        return iterator(0);
    }

    public Stream<T> stream(boolean parallel) {
        return StreamSupport.stream(spliterator(), parallel);
    }

    @Override
    public Stream<T> stream() {
        return stream(true);
    }

    public ListRecord<T> asRecord() {
        return new ListRecord<>(this);
    }

    private final Supplier<String> asString = new SoftLazy<>(() -> {
        final var builder = new StringBuilder();
        for (final var item : this) builder.append(item);
        return builder.toString();
    });

    public String asString() {
        return asString.get();
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

    public PersistentList<T> withReplacement(int index, Collection<T> items) {
        ArrayUtils.requireIndexInBounds(index, size());
        if (items.size() == 0) return this;
        return withReplacement(index, items.iterator());
    }

    public PersistentList<T> withReplacement(int index, T[] items) {
        ArrayUtils.requireIndexInBounds(index, size());
        if (items.length == 0) return this;
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

    public PersistentList<T> withInsertion(int index, Collection<T> items) {
        ArrayUtils.requireIndexInBounds(index, size() + 1);
        if (items.size() == 0) return this;
        return withInsertion(index, items.iterator());
    }

    public PersistentList<T> withInsertion(int index, T[] items) {
        ArrayUtils.requireIndexInBounds(index, size() + 1);
        if (items.length == 0) return this;
        return withInsertion(index, new ArrayIterator<>(items));
    }

    public PersistentList<T> withInsertion(int index, PersistentList<T> items) {
        ArrayUtils.requireIndexInBounds(index, size() + 1);
        if (items.size() == 0) return this;
        return new PersistentList<>(withInsertion(index, items.root, root));
    }

    // remove
    public PersistentList<T> without(int start, int length) {
        ArrayUtils.requireRangeInBounds(start, length, size());
        return new PersistentList<>(remove(start, start + length, root));
    }

    // ======================== misc list operations, single and multi item =================================
    public PersistentList<T> sorted(Comparator<T> comparator) {
        return new PersistentList<>(stream().sorted(comparator));
    }

    public T head() {
        if (size() == 0) return null;
        return get(0);
    }

    public T tail() {
        if (size() == 0) return null;
        return get(size() - 1);
    }

    public PersistentList<T> head(int length) {
        return get(0, Math.min(size(), length));
    }

    public PersistentList<T> tail(int length) {
        final var trimmedLength = Math.min(size(), length);
        return get(size() - trimmedLength, trimmedLength);
    }

    public PersistentList<T> withPut(T item) {
        return withAddition(size(), item);
    }

    public PersistentList<T> withoutPopped() {
        if (size() == 0) return new PersistentList<>();
        return without(size() - 1);
    }

    public PersistentList<T> withPushed(T item) {
        return withAddition(0, item);
    }

    public PersistentList<T> withoutPulled() {
        if (size() == 0) return new PersistentList<>();
        return without(0);
    }

    public PersistentList<T> withoutFirstOccurrence(T item) {
        final var tryResult = withoutFirstOccurrence(root, item);
        if (tryResult == null) {
            return this;
        } else {
            return new PersistentList<>(tryResult);
        }
    }

    public T getFirstOccurrence(T item) {
        if (item == null) return null;
        for (final var thisItem : this) {
            if (Objects.equals(item, thisItem)) return thisItem;
        }
        return null;
    }

    public PersistentList<T> withFirstOccurrenceReplaced(T item) {
        if (item == null) return this;
        final var result = replaceFirstOccurrence(root, item);
        if (result == null) return this;
        return new PersistentList<>(result);
    }

    /**
     * Either replaces the first occurrence of the item or appends the item at the end.
     * Similar in behavior to a set, ensures that the list contains the given instance.
     *
     * @return A copy of the list with the first occurrence of a matching item (as determined by Object.equals()) replaced with the item given or the item appended to the end if it did not occur.
     */
    public PersistentList<T> with(T item) {
        if (item == null && contains(null)) return this;
        final var replacementAttempt = replaceFirstOccurrence(root, item);
        if (replacementAttempt != null) return new PersistentList<>(replacementAttempt);
        return withPut(item);
    }

    private final Supplier<PersistentList<T>> lazyReversed = new SoftLazy<>(() -> new PersistentList<>(reversedIterator()));

    public PersistentList<T> reversed() {
        return lazyReversed.get();
    }

    public PersistentList<T> concat(PersistentList<T> other) {
        return this.withInsertion(size(), other);
    }

    public PersistentList<T> repeated(int times) {
        // Cannot be less than 2, or it'll cause infinite recursion and a stack overflow.
        final int breakingDenominator = Math.max(2, size() < PARTITION_SIZE ? PARTITION_SIZE / size() : PARTITION_SIZE);

        if (times < 0) return reversed().repeated(-times);
        if (times == 1) return this;
        if (times == 0) return new PersistentList<>();

        // base case
        if (times <= breakingDenominator) {
            var result = this;
            for (int i = 1; i < times; ++i) {
                result = result.concat(this);
            }
            return result;
        }

        // recursive case
        final var quotient = times / breakingDenominator;
        final var remainder = times % breakingDenominator;
        final var repeatedByQuotient = this.repeated(quotient);
        final var repeatedByRemainder = this.repeated(remainder);

        return repeatedByQuotient.repeated(breakingDenominator).concat(repeatedByRemainder);
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
                    ? withInsertion(0, new ArrayIterator<>(leaf.items, 0, index), withProceeding)
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

        for (int loopCount = 0; loopCount < branch.depth(); ++loopCount) {
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

    private static Node withoutFirstOccurrence(Node node, Object item) {
        if (node instanceof Branch branch) {
            final var leftResult = withoutFirstOccurrence(branch.left, item);
            if (leftResult != null) return cleaned(new Branch(leftResult, branch.right));

            final var rightResult = withoutFirstOccurrence(branch.right, item);
            if (rightResult != null) return cleaned(new Branch(branch.left, rightResult));

            return null;
        } else if (node instanceof Leaf leaf) {
            for (int i = 0; i < leaf.items.length; i++) {
                if (Objects.equals(item, leaf.items[i])) {
                    return new Leaf(ArrayUtils.remove(leaf.items, i));
                }
            }
            return null;
        } else throw new IllegalStateException();
    }

    private static Node replaceFirstOccurrence(Node node, Object item) {
        if (node instanceof Branch branch) {
            final var leftResult = replaceFirstOccurrence(branch.left, item);
            if (leftResult != null) return cleaned(new Branch(leftResult, branch.right));

            final var rightResult = replaceFirstOccurrence(branch.right, item);
            if (rightResult != null) return cleaned(new Branch(branch.left, rightResult));

            return null;
        } else if (node instanceof Leaf leaf) {
            for (int i = 0; i < leaf.items.length; i++) {
                if (Objects.equals(item, leaf.items[i])) {
                    return new Leaf(ArrayUtils.set(leaf.items, i, item));
                }
            }
            return null;
        } else throw new IllegalStateException();
    }

    // ========================= inner classes ====================================
    private interface Node extends Enumerable<Object>, java.io.Serializable {
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
        @NotNull
        final Node left;
        @NotNull
        final Node right;
        final int itemCount;
        final int leafCount;
        final int depth;
        final int balanceFactor;

        public Branch(@NotNull Node left, @NotNull Node right) {
            this.left = left;
            this.right = right;
            itemCount = left.itemCount() + right.itemCount();
            leafCount = left.leafCount() + right.leafCount();
            depth = Math.max(left.depth(), right.depth()) + 1;
            balanceFactor = right.depth() - left.depth();
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
        public final Object[] items;

        public Leaf(Object[] items) {
            this.items = items;
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

        private boolean advanceLeafNext() {
            if (!leafEnumerator.moveNext()) return false;
            if (leafEnumerator.current().items.length == 0) return advanceLeafNext();
            return true;
        }

        private boolean advanceLeafPrevious() {
            if (!leafEnumerator.movePrevious()) return false;
            if (leafEnumerator.current().items.length == 0) return advanceLeafPrevious();
            return true;
        }

        @Override
        public boolean movePrevious() {
            if (index == root.itemCount()) {
                index = root.itemCount() - 1;
                if (advanceLeafPrevious()) {
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
            } else if (advanceLeafPrevious()) {
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
                if (advanceLeafNext()) {
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
            } else if (advanceLeafNext()) {
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
            if (currentLeaf != null && currentLeaf.items.length > 0) {
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
}
