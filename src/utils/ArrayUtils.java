package utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class ArrayUtils {

    public static Object[] remove(Object[] original, int start, int length) {
        return remove(original, start, length, false, false);
    }

    public static Object[] remove(Object[] original, int start, int length, boolean reversed) {
        return remove(original, start, length, reversed, false);
    }

    public static Object[] remove(Object[] original, int start, int length, boolean reversed, boolean reverseResult) {
        Objects.requireNonNull(original);
        requireRangeInBounds(start, length, original.length);
        // shortcuts //
        if (length == 0 && reversed == reverseResult) return original;
        final var result = new Object[original.length - length];
        final var end = start + length;

        // copy preceding
        arraycopy(original, 0, result, 0, start, reversed, reverseResult);

        // copy proceeding
        arraycopy(original, end, result, start, original.length - end, reversed, reverseResult);

        return result;
    }

    public static Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source) {
        Objects.requireNonNull(source);
        return replace(destination, destinationStart, source, 0, source.length, false, false, false);
    }

    public static Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart) {
        Objects.requireNonNull(source);
        return replace(destination, destinationStart, source, sourceStart, source.length - sourceStart, false, false, false);
    }

    public static Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length) {
        return replace(destination, destinationStart, source, sourceStart, length, false, false, false);
    }

    public static Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed) {
        return replace(destination, destinationStart, source, sourceStart, length, destinationReversed, false, false);
    }

    public static Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed,
            boolean sourceReversed) {
        return replace(destination, destinationStart, source, sourceStart, length,
                destinationReversed, sourceReversed, false);
    }

    public static Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed,
            boolean sourceReversed,
            boolean reverseResult) {
        Objects.requireNonNull(destination);
        Objects.requireNonNull(source);
        requireRangeInBounds(destinationStart, length, destination.length);
        requireRangeInBounds(sourceStart, length, source.length);

        // shortcuts //
        if (length == 0 && destinationReversed == reverseResult) return destination;
        if (length == destination.length && length == source.length && sourceReversed == reverseResult) return source;

        final var result = new Object[destination.length];
        final var destinationEnd = destinationStart + length;

        // copy preceding
        arraycopy(destination, 0, result, 0, destinationStart,
                destinationReversed, reverseResult);

        // copy replacement
        arraycopy(source, sourceStart, result, destinationStart, length,
                sourceReversed, reverseResult);

        // copy proceeding
        arraycopy(destination, destinationEnd, result, destinationEnd, destination.length - destinationEnd,
                destinationReversed, reverseResult);

        return result;
    }

    public static Object[] get(Object[] original, int start, int length) {
        return get(original, start, length, false, false);
    }

    public static Object[] get(Object[] original, int start, int length, boolean reversed) {
        return get(original, start, length, reversed, false);
    }

    public static Object[] get(Object[] original, int start, int length, boolean reversed, boolean reverseResult) {
        Objects.requireNonNull(original);
        requireRangeInBounds(start, length, original.length);

        // shortcuts //
        if (length == 0) return new Object[0];
        if (length == original.length && reversed == reverseResult) return original;

        final var result = new Object[length];

        // copy range
        arraycopy(original, start, result, 0, length, reversed, reverseResult);

        return result;
    }

    public static Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source) {
        Objects.requireNonNull(source);
        return insert(destination, destinationStart, source, 0, source.length, false, false, false);
    }

    public static Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart) {
        Objects.requireNonNull(source);
        return insert(destination, destinationStart, source, sourceStart, source.length - sourceStart, false, false, false);
    }

    public static Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length) {
        return insert(destination, destinationStart, source, sourceStart, length, false, false, false);
    }

    public static Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed) {
        return insert(destination, destinationStart, source, sourceStart, length, destinationReversed, false, false);
    }

    public static Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed,
            boolean sourceReversed) {
        return insert(destination, destinationStart, source, sourceStart, length, destinationReversed, sourceReversed, false);
    }

    public static Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed,
            boolean sourceReversed,
            boolean reverseResult) {
        Objects.requireNonNull(destination);
        Objects.requireNonNull(source);
        requireIndexInBounds(destinationStart, destinationStart + 1);
        requireRangeInBounds(sourceStart, length, source.length);

        // shortcuts //
        if (length == 0 && destinationReversed == reverseResult) return destination;

        final var result = new Object[destination.length + length];
        final var rangeEnd = destinationStart + length;

        // copy preceding
        arraycopy(destination, 0, result, 0, destinationStart,
                destinationReversed, reverseResult);

        // copy insertion
        arraycopy(source, sourceStart, result, destinationStart, length,
                sourceReversed, reverseResult);

        // copy proceeding
        arraycopy(destination, destinationStart, result, rangeEnd, result.length - rangeEnd,
                destinationReversed, reverseResult);

        return result;
    }

    // single

    public static Object[] remove(Object[] original, int index) {
        return remove(original, index, false, false);
    }

    public static Object[] remove(Object[] original, int index, boolean reversed) {
        return remove(original, index, reversed, false);
    }

    public static Object[] remove(Object[] original, int index, boolean reversed, boolean reverseResult) {
        return remove(original, index, 1, reversed, reverseResult);
    }

    public static Object[] set(Object[] destination, int index, Object item) {
        return set(destination, index, item, false, false);
    }

    public static Object[] set(Object[] destination, int index, Object item, boolean reversed) {
        return set(destination, index, item, reversed, false);
    }

    public static Object[] set(Object[] destination, int index, Object item, boolean reversed, boolean reverseResult) {
        Objects.requireNonNull(destination);
        requireIndexInBounds(index, destination.length);

        final var result = new Object[destination.length];
        final var end = index + 1;

        // copy preceding
        arraycopy(destination, 0, result, 0, index, reversed, reverseResult);

        // copy insertion
        result[reverseIndexIf(reverseResult, index, result.length)]
                = item;

        // copy proceeding
        arraycopy(destination, end, result, end, destination.length - end, reversed, reverseResult);

        return result;
    }

    public static Object get(Object[] from, int index) {
        return get(from, index, false);
    }

    public static Object get(Object[] from, int index, boolean reversed) {
        Objects.requireNonNull(from);
        requireIndexInBounds(index, from.length);
        return from[reverseIndexIf(reversed, index, from.length)];
    }

    public static Object[] add(Object[] destination, int index, Object item) {
        return add(destination, index, item, false, false);
    }

    public static Object[] add(Object[] destination, int index, Object item, boolean reversed) {
        return add(destination, index, item, reversed, false);
    }

    public static Object[] add(Object[] destination, int index, Object item, boolean reversed, boolean reverseResult) {
        Objects.requireNonNull(destination);
        requireIndexInBounds(index, destination.length + 1);

        final var result = new Object[destination.length + 1];
        final var rangeEnd = index + 1;

        // copy preceding
        arraycopy(destination, 0, result, 0, index, reversed, reverseResult);

        // copy insertion
        result[reverseIndexIf(reverseResult, index, result.length)]
                = item;

        // copy proceeding
        arraycopy(destination, index, result, rangeEnd, result.length - rangeEnd, reversed, reverseResult);

        return result;
    }

    //=======================
    // misc array operations
    //=======================
    public static void arraycopy(
            Object[] source,
            int sourceStart,
            Object[] destination,
            int destinationStart,
            int length,
            boolean sourceReversed,
            boolean destinationReversed) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(destination);
        requireRangeInBounds(sourceStart, length, source.length);
        requireRangeInBounds(destinationStart, length, destination.length);

        int s = reverseIndexIf(sourceReversed, sourceStart, source.length);
        int d = reverseIndexIf(destinationReversed, destinationStart, destination.length);
        final var sd = sourceReversed ? -1 : 1;
        final var dd = destinationReversed ? -1 : 1;

        for (int i = 0; i < length; i++) {
            destination[d] = source[s];
            s += sd;
            d += dd;
        }
    }

    public static Object[] copyOf(Object[] original) {
        return copyOf(original, original.length, false, false);
    }

    public static Object[] copyOf(Object[] original, boolean originalReversed) {
        return copyOf(original, original.length, originalReversed, false);
    }

    public static Object[] copyOf(Object[] original, boolean originalReversed, boolean reverseResult) {
        return copyOf(original, original.length, originalReversed, reverseResult);
    }

    public static Object[] copyOf(Object[] original, int length) {
        return copyOf(original, length, false, false);
    }

    public static Object[] copyOf(Object[] original, int length, boolean originalReversed) {
        return copyOf(original, length, originalReversed, false);
    }

    public static Object[] copyOf(Object[] original, int length, boolean originalReversed, boolean reverseResult) {
        final var result = new Object[length];
        arraycopy(original, 0, result, 0, Math.min(original.length, length), originalReversed, reverseResult);
        return result;
    }

    public static <T> boolean isSorted(T[] items, Comparator<T> comparator) {
        return isSorted(items, comparator, false);
    }

    public static <T> boolean isSorted(T[] items, Comparator<T> comparator, boolean reversed) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(comparator);
        T prev;
        if (reversed) {
            prev = items[items.length - 1];
            for (int i = items.length - 2; i >= 0; i--) {
                if (comparator.compare(prev, items[i]) > 0) return false;
                prev = items[i];
            }
        } else {
            prev = items[0];
            for (int i = 1; i < items.length; i++) {
                if (comparator.compare(prev, items[i]) > 0) return false;
                prev = items[i];
            }
        }

        return true;
    }

    public static Object[][] partition(Object[] items, int size) {
        return partition(items, size, false, false);
    }

    public static Object[][] partition(Object[] items, int size, boolean reversed) {
        return partition(items, size, reversed, false);
    }

    public static Object[][] partition(Object[] items, int size, boolean reversed, boolean reverseResults) {
        Objects.requireNonNull(items);

        if (size >= items.length) {
            if (reversed == reverseResults) {
                return new Object[][]{items};
            } else {
                return new Object[][]{copyOf(items, reversed, reverseResults)};
            }
        }

        final var count = items.length / size;
        final var remainder = items.length % size;
        final var totalCount = count + remainder > 0 ? 1 : 0;

        final var result = new Object[totalCount][];

        // copy whole partitions
        for (int i = 0; i < count; i++) {
            final var partition = get(items, i * size, (i + 1) * size, reversed, reverseResults);
            result[i] = partition;
        }

        // copy remainder partition
        if (totalCount > count) {
            final var partition = get(items, items.length - remainder, remainder, reversed, reverseResults);
            result[result.length - 1] = partition;
        }

        return result;
    }

    //=======================
    // requirement functions
    //=======================

    public static int requireIndexInBounds(int index, int upper) {
        return requireIndexInBounds(0, index, upper);
    }

    public static int requireIndexInBounds(int lower, int index, int upper) {
        if (lower <= index && index < upper) {
            return index;
        } else {
            throw new IndexOutOfBoundsException(index);
        }
    }

    public static int requireRangeInBounds(int start, int length, int upper) {
        return requireRangeInBounds(0, start, length, upper);
    }

    public static int requireRangeInBounds(int lower, int start, int length, int upper) {
        if (lower > start) throw new IndexOutOfBoundsException(start);

        requireNonNegative(length);

        if (upper <= start + length) throw new IndexOutOfBoundsException(start + length);

        return start;
    }

    public static int requireNonNegative(int number) {
        if (number >= 0) {
            return number;
        } else {
            throw new IllegalArgumentException("number must not be negative.");
        }
    }

    public static int requireNonNegativeArraySize(int size) {
        if (size >= 0) {
            return size;
        } else {
            throw new NegativeArraySizeException();
        }
    }

    //====================
    // index manipulation
    //====================
    public static int reverseIndexIf(boolean condition, int index, int length) {
        if (condition) {
            return reverseIndex(index, length);
        } else {
            return index;
        }
    }

    public static int reverseIndex(int index, int length) {
        return length - index - 1;
    }

    /////////////
    // misc
    ///////////

    /**
     * Takes two arrays which may share the same instance and separates them into two instances.
     * Returns array b. if they're different instances.
     *
     * @param a
     * @param b
     * @return If the arrays share an instance: a shallow copy of that instance, else: array b.
     */
    public static Object[] unConsolidate(Object[] a, Object[] b) {
        if (a == b) {
            return Arrays.copyOf(a, a.length);
        } else {
            return b;
        }
    }

    public static Object[] resize(Object[] original, int size) {
        Objects.requireNonNull(original);
        if (original.length == size) return original;
        return Arrays.copyOf(original, size);
    }
}