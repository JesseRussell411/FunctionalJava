package functionPlus;

@FunctionalInterface
public interface NonaConsumer<A, B, C, D, E, F, G, H, I> {
    void accept(A a, B b, C c, D d, E e, F f, G g, H h, I i);

    default NonaConsumer<A, B, C, D, E, F, G, H, I> andThen(NonaConsumer<A, B, C, D, E, F, G, H, I> after) {
        return (a, b, c, d, e, f, g, h, i) -> {
            accept(a, b, c, d, e, f, g, h, i);
            after.accept(a, b, c, d, e, f, g, h, i);
        };
    }
}
