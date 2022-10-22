package functionPlus;

@FunctionalInterface
public interface DecaConsumer<A, B, C, D, E, F, G, H, I, J> {
    void accept(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j);

    default DecaConsumer<A, B, C, D, E, F, G, H, I, J> andThen(DecaConsumer<A, B, C, D, E, F, G, H, I, J> after) {
        return (a, b, c, d, e, f, g, h, i, j) -> {
            accept(a, b, c, d, e, f, g, h, i, j);
            after.accept(a, b, c, d, e, f, g, h, i, j);
        };
    }
}
