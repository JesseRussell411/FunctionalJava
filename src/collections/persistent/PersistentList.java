package collections.persistent;

import collections.ArrayStack;
import collections.ArrayUtils;
import collections.adapters.ArrayAsList;
import collections.iteration.IterableUtils;
import collections.iteration.MergingIterator;
import collections.iteration.adapters.ArrayIterator;
import collections.iteration.adapters.EnumeratorIterator;
import collections.iteration.adapters.ListEnumeratorIterator;
import collections.iteration.adapters.ReversedEnumeratorIterator;
import collections.iteration.enumerable.Enumerable;
import collections.iteration.enumerable.IndexedBiDirectionalEnumerable;
import collections.iteration.enumerator.Enumerator;
import collections.iteration.enumerator.IndexedBiDirectionalEnumerator;
import collections.records.ListRecord;
import errors.ImpossibleStateException;
import memoization.pure.function.SoftMemoizedFunction;
import memoization.pure.supplier.SoftMemoizedSupplier;
import org.jetbrains.annotations.NotNull;
import reference.pointers.Pointer;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// Honestly not the best implementation of a persistent list, but very flexible and my first try.

/**
 * Immutable list that supports efficient copying with modification.
 * Basic operations have logarithmic complexity in time and space.
 *
 * @param <T>
 */
public class PersistentList<T> extends AbstractList<T> implements IndexedBiDirectionalEnumerable<T>, java.io.Serializable {
    @NotNull
    private static final Object[] EMPTY_ARRAY = new Object[0];
    @NotNull
    private static final Leaf EMPTY_LEAF = new Leaf(EMPTY_ARRAY);
    private static final int PARTITION_SIZE = 32;

    static {
        assert PARTITION_SIZE >= 1;
        assert EMPTY_ARRAY.length == 0;
        assert EMPTY_LEAF.itemCount() == 0;
    }

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

    public static <T> PersistentList<T> generate(Function<Integer, T> itemGenerator, int length) {
        return new PersistentList<>(fromGenerator(itemGenerator, length));
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
        return stream(false);
    }

    public ListRecord<T> asRecord() {
        return new ListRecord<>(this);
    }

    /**
     * Creates a string containing the string representations of each item in the list concatenated together with no deliminators.
     *
     * @return The created string.
     */
    public String asString() {
        return asString("");
    }

    /**
     * Creates a string containing the string representations of each item in the list separated by the deliminator.
     *
     * @return The created string.
     */
    public String asString(String deliminator) {
        final var builder = new StringBuilder();

        final var enu = this.enumerator();
        if (enu.moveNext()) builder.append(enu.current());

        while (enu.moveNext()) {
            builder.append(deliminator);
            builder.append(enu.current());
        }
        return builder.toString();
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

    /**
     * Gets the item at the index.
     *
     * @param index The index of the item to get.
     * @return The item at the index.
     */
    public T get(int index) {
        if (index < 0) index = convertNegativeIndex(index);

        return (T) get(index, root);
    }

    // replace

    /**
     * Replaces the item at the index with the given item.
     *
     * @param index The index of the item to replace.
     * @param item  The new item with which to replace the old one.
     * @return A new list with the item replaced.
     */
    public PersistentList<T> swap(int index, T item) {
        if (index < 0) index = convertNegativeIndex(index);

        ArrayUtils.requireIndexInBounds(index, size());
        return new PersistentList<>(set(index, item, root));
    }

    // insert

    /**
     * Inserts the item at the index.
     *
     * @param index Where to insert the item.
     * @param item  the item to insert.
     * @return A new list with the item inserted.
     */
    public PersistentList<T> insertSingle(int index, T item) {
        if (index < 0) index = convertNegativeIndex(index);

        ArrayUtils.requireIndexInBounds(index, size() + 1);
        return new PersistentList<>(add(index, item, root));
    }

    // remove

    /**
     * Removes the item at the index from the list.
     *
     * @param index The index of the item to remove.
     * @return A new list without the item at the index.
     */
    public PersistentList<T> without(int index) {
        if (index < 0) index = convertNegativeIndex(index);

        ArrayUtils.requireIndexInBounds(index, size());
        return new PersistentList<>(remove(index, index + 1, root));
    }

    // ================= list operations, multi item ==========================
    //extract

    /**
     * Gets a range of items from the list.
     *
     * @param start  The index of the first item to get.
     * @param length How many items to get.
     * @return A new list containing the range of items.
     */
    public PersistentList<T> get(int start, int length) {
        if (start < 0) start = convertNegativeIndex(start);

        ArrayUtils.requireRangeInBounds(start, length, size());
        return new PersistentList<>(get(start, start + length, root));
    }

    // replace

    /**
     * Replaces a range of items in the list with the given collection of items.
     *
     * @param index The index of the first item to replace.
     * @param items The items with which to replace.
     * @return A new list with the replaced items.
     */
    public PersistentList<T> replace(int index, Stream<T> items) {
        return replace(index, items.iterator());
    }

    /**
     * Replaces a range of items in the list with the given collection of items.
     *
     * @param index The index of the first item to replace.
     * @param items The items with which to replace.
     * @return A new list with the replaced items.
     */
    public PersistentList<T> replace(int index, Iterable<T> items) {
        return replace(index, items.iterator());
    }

    /**
     * Replaces a range of items in the list with the given collection of items.
     *
     * @param index The index of the first item to replace.
     * @param items The items with which to replace.
     * @return A new list with the replaced items.
     */
    public PersistentList<T> replace(int index, Collection<T> items) {
        if (items.size() == 0) return this;
        return replace(index, items.iterator());
    }

    /**
     * Replaces a range of items in the list with the given collection of items.
     *
     * @param index The index of the first item to replace.
     * @param items The items with which to replace.
     * @return A new list with the replaced items.
     */
    public PersistentList<T> replace(int index, T[] items) {
        if (items.length == 0) return this;
        return replace(index, new ArrayIterator<>(items));
    }

    /**
     * Replaces a range of items in the list with the given collection of items.
     *
     * @param index The index of the first item to replace.
     * @param items The items with which to replace.
     * @return A new list with the replaced items.
     */
    public PersistentList<T> replace(int index, Iterator<T> items) {
        if (index < 0) index = convertNegativeIndex(index);
        ArrayUtils.requireIndexInBounds(index, size());
        return new PersistentList<>(replace(index, items, root));
    }

    // insert

    /**
     * Inserts a collection of items into the list.
     *
     * @param index Where to insert the items.
     * @param items The items to insert.
     * @return A new list with the inserted items.
     */
    public PersistentList<T> insert(int index, Stream<T> items) {
        return insert(index, items.iterator());
    }

    /**
     * Inserts a collection of items into the list.
     *
     * @param index Where to insert the items.
     * @param items The items to insert.
     * @return A new list with the inserted items.
     */
    public PersistentList<T> insert(int index, Iterable<T> items) {
        return insert(index, items.iterator());
    }

    /**
     * Inserts a collection of items into the list.
     *
     * @param index Where to insert the items.
     * @param items The items to insert.
     * @return A new list with the inserted items.
     */
    public PersistentList<T> insert(int index, Collection<T> items) {
        if (items.size() == 0) return this;
        return insert(index, items.iterator());
    }

    /**
     * Inserts a collection of items into the list.
     *
     * @param index Where to insert the items.
     * @param items The items to insert.
     * @return A new list with the inserted items.
     */
    public PersistentList<T> insert(int index, T[] items) {
        if (items.length == 0) return this;
        return insert(index, new ArrayIterator<>(items));
    }

    /**
     * Inserts a collection of items into the list.
     *
     * @param index Where to insert the items.
     * @param items The items to insert.
     * @return A new list with the inserted items.
     */
    public PersistentList<T> insert(int index, Iterator<T> items) {
        if (index < 0) index = convertNegativeIndex(index);
        ArrayUtils.requireIndexInBounds(index, size() + 1);
        return new PersistentList<>(insert(index, items, root));
    }

    /**
     * Inserts a collection of items into the list.
     *
     * @param index Where to insert the items.
     * @param items The items to insert.
     * @return A new list with the inserted items.
     */
    public PersistentList<T> insert(int index, PersistentList<T> items) {
        if (index < 0) index = convertNegativeIndex(index);
        ArrayUtils.requireIndexInBounds(index, size() + 1);
        if (items.size() == 0) return this;
        return new PersistentList<>(insert(index, items.root, root));
    }

    // remove

    /**
     * Removes a range of items from the list.
     *
     * @param start  The index of the first item to remove.
     * @param length How many items to remove in sequence.
     * @return A new list with the range removed.
     */
    public PersistentList<T> without(int start, int length) {
        if (start < 0) start = convertNegativeIndex(start);
        ArrayUtils.requireRangeInBounds(start, length, size());
        return new PersistentList<>(remove(start, start + length, root));
    }

    // ======================== misc list operations, single and multi item =================================

    /**
     * Sorts the list in ascending order based on the comparator given.
     *
     * @param comparator How to sort the list.
     * @return A new list that has been sorted.
     */
    public PersistentList<T> sorted(Comparator<T> comparator) {
        return new PersistentList<>(stream().sorted(comparator));
    }

    /**
     * @return The first item in the list.
     */
    public T head() {
        if (size() == 0) return null;
        return get(0);
    }

    /**
     * @return The last item in the list.
     */
    public T tail() {
        if (size() == 0) return null;
        return get(size() - 1);
    }

    /**
     * @return The first n items in the list.
     */
    public PersistentList<T> head(int n) {
        return get(0, Math.min(size(), n));
    }

    /**
     * @return The last n items in the list.
     */
    public PersistentList<T> tail(int n) {
        final var trimmedLength = Math.min(size(), n);
        return get(size() - trimmedLength, trimmedLength);
    }

    /**
     * Put item onto end of list.
     *
     * @return A new list with the item added onto the end.
     */
    public PersistentList<T> put(T item) {
        return insertSingle(size(), item);
    }

    /**
     * Removes the last item.
     *
     * @return A new list without the last item.
     */
    public PersistentList<T> pop() {
        if (size() == 0) return new PersistentList<>();
        return without(size() - 1);
    }

    /**
     * Push item onto start of list.
     *
     * @return A new list with the item added as the first item.
     */
    public PersistentList<T> push(T item) {
        return insertSingle(0, item);
    }

    /**
     * Removes the first item.
     *
     * @return A new list without the first item.
     */
    public PersistentList<T> pull() {
        if (size() == 0) return new PersistentList<>();
        return without(0);
    }

    public PersistentList<T> filter(BiPredicate<T, Integer> test) {
        return filter(test, -1);
    }

    public PersistentList<T> filter(BiPredicate<T, Integer> test, int removalLimit) {
        Objects.requireNonNull(test);
        final var result = filter(root, (BiPredicate<Object, Integer>) test, new Pointer<>(removalLimit));

        if (result == root)
            return this;
        else
            return new PersistentList<>(result);
    }

    public <R> PersistentList<R> map(BiFunction<T, Integer, R> mapping) {
        Objects.requireNonNull(mapping);
        final var result = map(root, (BiFunction<Object, Integer, Object>) mapping, new Pointer<>(-1));
        return new PersistentList<>(result);
    }

    public PersistentList<Object> map(BiFunction<T, Integer, Object> mapping, int modificationLimit) {
        Objects.requireNonNull(mapping);
        final var result = map(root, (BiFunction<Object, Integer, Object>) mapping, new Pointer<>(modificationLimit));
        if (result == root)
            return (PersistentList<Object>) this;
        else
            return new PersistentList<>(result);
    }

    /**
     * Removes the first occurrence of the item from the list while leaving in subsequent occurrences.
     *
     * @param item The item to remove.
     * @return A new list with the first occurrence of the item removed.
     */
    public PersistentList<T> withoutFirstOccurrence(T item) {
        return filter((listItem, i) -> !Objects.equals(item, listItem), 1);
    }

    /**
     * Gets the first occurrence in the list of an item matching the given item.
     *
     * @param item The item to look for.
     * @return The first item in the list that is considered equal to the given item or null if one wasn't found.
     */
    public T getFirstOccurrence(T item) {
        if (item == null) return null;
        for (final var thisItem : this) {
            if (Objects.equals(item, thisItem)) return thisItem;
        }
        return null;
    }

    /**
     * Replaces the first occurrence of the item with the replacement.
     *
     * @param item        The item to look for.
     * @param replacement What to replace the item with.
     * @return A new list with the first occurrence of the item replaced with the replacement.
     */
    public PersistentList<T> replaceFirstOccurrence(T item, T replacement) {
        if (item == null) return this;
        final var result = replaceFirstOccurrence(root, item, replacement);
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
        if (item == null) {
            if (contains(null)) return this;
            else return this.put(null);
        }

        final var replacementAttempt = replaceFirstOccurrence(root, item, item);
        if (replacementAttempt != null)
            return new PersistentList<>(replacementAttempt);
        else
            return put(item);
    }

    /**
     * Reverses the list.
     *
     * @return A new list with the items in this list in reverse order.
     */
    public PersistentList<T> reverse() {
        return memoReverse.get();
    }

    private final Supplier<PersistentList<T>> memoReverse = new SoftMemoizedSupplier<>(() -> new PersistentList<>(reversedIterator()));

    /**
     * Adds the items to the end of the list.
     *
     * @param items The items to add.
     * @return A new list with the items added to the end.
     */
    public PersistentList<T> concat(Stream<T> items) {
        return this.insert(size(), items);
    }

    /**
     * Adds the items to the end of the list.
     *
     * @param items The items to add.
     * @return A new list with the items added to the end.
     */
    public PersistentList<T> concat(Iterable<T> items) {
        return this.insert(size(), items);
    }

    /**
     * Adds the items to the end of the list.
     *
     * @param items The items to add.
     * @return A new list with the items added to the end.
     */
    public PersistentList<T> concat(Collection<T> items) {
        return this.insert(size(), items);
    }

    /**
     * Adds the items to the end of the list.
     *
     * @param items The items to add.
     * @return A new list with the items added to the end.
     */
    public PersistentList<T> concat(T[] items) {
        return this.insert(size(), items);
    }

    /**
     * Adds the items to the end of the list.
     *
     * @param items The items to add.
     * @return A new list with the items added to the end.
     */
    public PersistentList<T> concat(Iterator<T> items) {
        return this.insert(size(), items);
    }

    /**
     * Adds the items to the end of the list.
     *
     * @param items The items to add.
     * @return A new list with the items added to the end.
     */
    public PersistentList<T> concat(PersistentList<T> items) {
        return this.insert(size(), items);
    }

    /**
     * Repeats the list a number of times.
     *
     * @param times How many times to repeat the list. If negative, the list is reversed and then repeated. -1 would just reverse the list for example.
     * @return A new list with the items of this list repeated.
     */
    public PersistentList<T> repeat(int times) {
        return memoRepeat.apply(times);
    }

    private final Function<Integer, PersistentList<T>> memoRepeat = new SoftMemoizedFunction<>((Integer times) -> {
        // Cannot be less than 2 or it'll cause infinite recursion and a stack overflow.
        final int breakingDenominator = Math.max(2, size() < PARTITION_SIZE ? PARTITION_SIZE / size() : PARTITION_SIZE);

        if (times < 0) return reverse().repeat(-times);
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
        final var repeatedByQuotient = this.repeat(quotient);
        final var repeatedByRemainder = this.repeat(remainder);

        return repeatedByQuotient.repeat(breakingDenominator).concat(repeatedByRemainder);
    });

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
        } else throw new ImpossibleStateException();
    }

    private static Node get(int start, int end, Node node) {
        if (node instanceof Branch branch) {
            final var left = branch.left;
            final var right = branch.right;

            if (start < left.itemCount()) {
                if (end <= left.itemCount()) {
                    return cleaned(get(start, end, left));
                } else {
                    return cleaned(new Branch(
                            get(start, left.itemCount(), left),
                            get(0, end - left.itemCount(), right)));
                }
            } else {
                return cleaned(get(
                        start - left.itemCount(),
                        end - left.itemCount(), right));
            }
        } else if (node instanceof Leaf leaf) {
            return new Leaf(ArrayUtils.get(leaf.items, start, end - start));
        } else throw new ImpossibleStateException();
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
        } else throw new ImpossibleStateException();
    }

    private static Node replace(int index, Iterator<?> itemIterator, Node root) {
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
        } else throw new ImpossibleStateException();
    }

    private static Node insert(int index, Node items, Node root) {
        if (root instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return cleaned(new Branch(
                        insert(index, items, branch.left),
                        branch.right));
            } else {
                return cleaned(new Branch(
                        branch.left,
                        insert(index - branch.left.itemCount(), items, branch.right)));
            }
        } else if (root instanceof Leaf leaf) {
            final var withProceeding = index < leaf.items.length
                    ? insert(items.itemCount(), new ArrayIterator<>(leaf.items, index), items)
                    : items;

            return index > 0
                    ? insert(0, new ArrayIterator<>(leaf.items, 0, index), withProceeding)
                    : withProceeding;
        } else throw new ImpossibleStateException();
    }

    private static Node insert(int index, Iterator<?> itemIterator, Node root) {
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
        } else throw new ImpossibleStateException();
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
        } else throw new ImpossibleStateException();
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
        } else throw new ImpossibleStateException();
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

    private static Node fromGenerator(Function<Integer, ?> itemGenerator, int length) {
        if (length == 0) return EMPTY_LEAF;
        final var partitions = new ArrayList<Object[]>();
        Object[] newPartition = new Object[PARTITION_SIZE];
        int p = 0;

        for (int i = 0; i < length; i++) {
            newPartition[p++] = itemGenerator.apply(i);
            if (p >= PARTITION_SIZE){
                partitions.add(newPartition);
                newPartition = new Object[PARTITION_SIZE];
                p = 0;
            }
        }

        if (p > 0){
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
        } else throw new ImpossibleStateException();
    }

    // ========= maintenance ============
    private static Node cleaned(Node node) {
        if (node instanceof Branch branch) {
            final var pruned = pruned(branch);
            if (!(pruned instanceof Branch prunedBranch)) return pruned;
            return balanced(prunedBranch);
        } else return node;
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


    private static Node filter(Node node, BiPredicate<Object, Integer> test, Pointer<Integer> removalLimit) {
        return filter(node, test, removalLimit, 0);
    }

    private static Node filter(Node node, BiPredicate<Object, Integer> test, Pointer<Integer> removalLimit, int indexOffset) {
        if (removalLimit.current == 0) return node;

        if (node instanceof Branch branch) {
            final var leftResult = filter(branch.left, test, removalLimit, indexOffset);
            final var rightResult = filter(branch.right, test, removalLimit, indexOffset + branch.left.itemCount());

            if (leftResult == branch.left && rightResult == branch.right)
                return branch;
            else
                return cleaned(new Branch(leftResult, rightResult));
        } else if (node instanceof Leaf leaf) {
            final var result = ArrayUtils.filter(
                    leaf.items,
                    (item, i) -> test.test(item, i + indexOffset),
                    removalLimit.current);

            if (result == leaf.items || result.length == leaf.items.length)
                return leaf;

            if (removalLimit.current >= 0)
                removalLimit.current -= (leaf.items.length - result.length);

            return new Leaf(result);
        } else throw new ImpossibleStateException();
    }

    private static Node map(Node node, BiFunction<Object, Integer, Object> mapping, Pointer<Integer> modificationLimit) {
        return map(node, mapping, modificationLimit, 0);
    }

    private static Node map(Node node, BiFunction<Object, Integer, Object> mapping, Pointer<Integer> modificationLimit, int indexOffset) {
        if (modificationLimit.current == 0) return node;

        if (node instanceof Branch branch) {
            final var leftResult = map(branch.left, mapping, modificationLimit, indexOffset);
            final var rightResult = map(branch.right, mapping, modificationLimit, indexOffset + branch.left.itemCount());
            if (leftResult == branch.left && rightResult == branch.right)
                return branch;
            else
                return cleaned(new Branch(leftResult, rightResult));

        } else if (node instanceof Leaf leaf) {
            final var modificationCount = new Pointer<>(0);
            final var result = ArrayUtils.map(
                    leaf.items,
                    (item, i) -> mapping.apply(item, i + indexOffset),
                    modificationLimit.current,
                    modificationCount);

            if (result == leaf.items || modificationCount.current == 0) {
                return leaf;
            } else {
                modificationLimit.current -= modificationCount.current;
                return new Leaf(result);
            }
        } else throw new ImpossibleStateException();
    }

    /**
     * Tries to replace the first occurrence of the item. return null if the item doesn't occur.
     */
    private static Node replaceFirstOccurrence(Node node, Object item, Object replacement) {
        if (node instanceof Branch branch) {
            final var leftResult = replaceFirstOccurrence(branch.left, item, replacement);
            if (leftResult != null) return cleaned(new Branch(leftResult, branch.right));

            final var rightResult = replaceFirstOccurrence(branch.right, item, replacement);
            if (rightResult != null) return cleaned(new Branch(branch.left, rightResult));

            return null;
        } else if (node instanceof Leaf leaf) {
            for (int i = 0; i < leaf.items.length; i++) {
                if (Objects.equals(item, leaf.items[i])) {
                    return new Leaf(ArrayUtils.set(leaf.items, i, replacement));
                }
            }
            return null;
        } else throw new ImpossibleStateException();
    }

    private int convertNegativeIndex(int index) {
        return size() - 1 + index;
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

            final var location = new ArrayStack<Node>();
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
            final var current = leafEnumerator.current();
            if (current == null) return false;
            if (current.items.length == 0) return advanceLeafNext();
            return true;
        }

        private boolean advanceLeafPrevious() {
            if (!leafEnumerator.movePrevious()) return false;
            final var current = leafEnumerator.current();
            if (current == null) return false;
            if (current.items.length == 0) return advanceLeafPrevious();
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

    private Iterator<Object> iterateNode(Node n) {
        return new EnumeratorIterator<>(new ItemEnumerator(n));
    }

    /**
     * A sorting method that attempts to preserve structural sharing.
     * This is much slower than {@link PersistentList#sorted(Comparator)}.
     * But that sorting method does not attempt to preserver structural sharing.
     * Use of this method over {@link PersistentList#sorted(Comparator)} may improve memory usage
     * When the lists it is used on are mostly sorted already.
     *
     * @param c The comparator to sort with.
     * @return A new list with the items in the original list in sorted order.
     */
    public PersistentList<T> optosort(@NotNull Comparator<T> c) {
        Objects.requireNonNull(c);
        return new PersistentList<>(optosort(c, root));
    }

    private Node optosort(Comparator<T> c, Node n) {
        if (n instanceof Branch b) {
            if (IterableUtils.isSorted((Iterator<T>) b.iterator(), c))
                return b;

            final var leftSorted = optosort(c, b.left);
            final var rightSorted = optosort(c, b.right);
            if (c.compare(
                    // last item in left
                    (T) get(leftSorted.itemCount() - 1, leftSorted),
                    // first item in right
                    (T) get(0, rightSorted)) <= 0) {
                return new Branch(leftSorted, rightSorted);
            } else {
                return fromIterator(new MergingIterator<>(
                        (Iterator<T>) leftSorted.iterator(),
                        (Iterator<T>) rightSorted.iterator(),
                        c));
            }
        } else if (n instanceof Leaf l) {
            if (ArrayUtils.isSorted((T[]) l.items, c))
                return l;

            final var sortedItems = ((Stream<T>) StreamSupport.stream(Spliterators.spliterator(l.items, 0), true)).sorted(c).toArray();
            return new Leaf(sortedItems);
        } else throw new ImpossibleStateException();
    }

    private static class LeafEnumerator implements IndexedBiDirectionalEnumerator<Leaf> {
        private final ArrayStack<Node> location;
        private int locationIndex;
        private final Node root;

        public LeafEnumerator(Node root, boolean startAndEnd) {
            this.root = Objects.requireNonNull(root);
            locationIndex = startAndEnd ? root.leafCount() : -1;
            location = new ArrayStack<>(root.depth());
        }

        public LeafEnumerator(ArrayStack<Node> location, int locationIndex, Node root) {
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
