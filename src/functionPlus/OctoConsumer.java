package functionPlus;

@FunctionalInterface
public interface OctoConsumer<A, B, C, D, E, F, G, H> {
    void accept(A a, B b, C c, D d, E e, F f, G g, H h);

    default OctoConsumer<A, B, C, D, E, F, G, H> andThen(OctoConsumer<A, B, C, D, E, F, G, H> after) {
        return (a, b, c, d, e, f, g, h) -> {
            accept(a, b, c, d, e, f, g, h);
            after.accept(a, b, c, d, e, f, g, h);
        };
    }
}
