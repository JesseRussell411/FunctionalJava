package collections;

import utils.ArrayUtils;

import java.util.Iterator;
import java.util.Stack;

public class PersistantList<T> {
    private final Object[] EMPTY_ARRAY = new Object[0];
    private final Leaf EMPTY_LEAF = new Leaf(EMPTY_ARRAY);
    private final int PARTITION_SIZE = 32;

    private final Node root;
    private final int size;

    private PersistantList(Node root) {
        this.root = root;
        size = root.itemCount();
    }

    public int size() {
        return size;
    }

    // extract
    // insert
    // remove
    // replace

    // list operations, single item
    // extract
    public T get(int index) {
        return get(index, root);
    }

    public PersistantList<T> set(int index, T item) {
        ArrayUtils.requireIndexInBounds(index, size());
        return new PersistantList<>(set(index, item, root));
    }

    public PersistantList<T> add(int index, T item) {
        ArrayUtils.requireIndexInBounds(index, size() + 1);
        return new PersistantList<>(add(index, item, root));
    }

    public PersistantList<T> remove(int index) {
        ArrayUtils.requireRangeInBounds(index, 1, size());
        return new PersistantList<>(remove(index, index + 1, root));
    }

    // misc
    public PersistantList<T> put(T item) {
        return add(size(), item);
    }

    public PersistantList<T> pop() {
        if (size() <= 0) return this;
        return remove(size() - 1);
    }

    public PersistantList<T> push(T item) {
        return add(0, item);
    }

    public PersistantList<T> pull() {
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

    // private utilities
    private T get(int index, Node root) {
        if (root instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return get(index, branch.left);
            } else {
                return get(index - branch.left.itemCount(), branch.right);
            }
        } else if (root instanceof Leaf leaf) {
            return (T) leaf.items[index];
        } else throw new NullPointerException();
    }

    private Node set(int index, T item, Node root) {
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
        } else throw new NullPointerException();
    }

    private Node add(int index, T item, Node root) {
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
            return fromPartitions(ArrayUtils.partition(
                    newItems,
                    PARTITION_SIZE));
        } else throw new NullPointerException();
    }

    private Node remove(int start, int end, Node root) {
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
        } else throw new NullPointerException();
    }

    private Node fromPartitions(Object[][] partitions) {
        return fromPartitions(partitions, 0, partitions.length);
    }

    private Node fromPartitions(Object[][] partitions, int start, int length) {
        if (length > 1) {
            final var rightPortion = length / 2;
            final var leftPortion = length - rightPortion;
            return new Branch(
                    fromPartitions(partitions, start, leftPortion),
                    fromPartitions(partitions, start + leftPortion, rightPortion));
        } else if (length == 1) {
            return new Leaf(partitions[start]);
        } else {
            return EMPTY_LEAF;
        }
    }

    private Node cleaned(Node node) {
        if (node instanceof Branch branch) {
            final var pruned = pruned(branch);
            if (pruned instanceof Branch prunedBranch) {
                return balanced(prunedBranch);
            } else return pruned;
        } else return node;
    }

    private Node pruned(Branch branch) {
        if (branch.left.itemCount() == 0) return branch.right;
        if (branch.right.itemCount() == 0) return branch.left;
        return branch;
    }

    private Branch balanced(Branch branch) {
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
    public class SelfIterator implements Iterator<T> {
        private ItemIterator itemiterator = new ItemIterator(root);


        @Override
        public boolean hasNext() {
            return itemiterator.hasNext();
        }

        @Override
        public T next() {
            return (T) itemiterator.next();
        }
    }

    private static class ItemIterator implements Iterator<Object> {
        final Node node;
        final LeafIterator leafIterator;
        Leaf currentLeaf = null;
        int index = 0;
        int offset = 0;

        private ItemIterator(Node node, LeafIterator leafIterator, Leaf currentLeaf, int index, int offset) {
            this.node = node;
            this.leafIterator = leafIterator;
            this.currentLeaf = currentLeaf;
            this.index = index;
            this.offset = offset;
        }

        public ItemIterator(Node node) {
            this.node = node;
            this.leafIterator = new LeafIterator(node);
        }

        public boolean hasNext() {
            return index < node.itemCount();
        }

        public Object next() {
            if (index >= node.itemCount()) {
                return null;
            }

            if (currentLeaf == null) {
                initializeCurrentLeaf();
            }


            while (index >= offset + currentLeaf.items.length) {
                advanceToNextLeaf();
            }

            return currentLeaf.items[(index++) - offset];
        }

        private void advanceToNextLeaf() {
            offset += currentLeaf.items.length;
            currentLeaf = leafIterator.next();

            if (currentLeaf == null) throw new IllegalStateException();
        }

        private void initializeCurrentLeaf() {
            currentLeaf = leafIterator.next();

            if (currentLeaf == null) throw new IllegalStateException();
        }
    }

    private static class LeafIterator implements Iterator<Leaf> {
        Stack<Node> location;
        int progress;
        final Node root;

        public void reset() {
            location = null;
            progress = 0;
        }

        public LeafIterator(Node root) {
            this.root = root;
            reset();
        }

        public boolean hasNext() {
            return progress < root.leafCount();
        }

        public Leaf next() {
            if (!hasNext()) return null;

            ++progress;
            if (location == null) {
                initializeLocation();
            } else {
                advanceToNextLeaf();
            }
            return currentLeaf();
        }

        private Leaf currentLeaf() {
            if (location.peek() instanceof Leaf l) {
                return l;
            } else {
                throw new IllegalStateException();
            }
        }

        private void advanceToNextLeaf() {
            Node child = location.pop();
            Node parent = location.peek();

            while (child == ((Branch) parent).right) {
                child = parent;
                location.pop();
                parent = location.peek();
            }

            location.push(((Branch) parent).right);

            moveDownToLeaf();
        }


        private void moveDownToLeaf() {
            Node current = location.peek();

            while (current instanceof Branch b) {
                location.push(current = b.left);
            }
        }

        private void initializeLocation() {
            location = new Stack<>();
            location.push(root);
            moveDownToLeaf();
        }
    }
}
