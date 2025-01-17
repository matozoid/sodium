import nz.sodium.*;

import java.util.Optional;

public class CalmExample {
    public static <A> Stream<A> calm(Stream<A> sA,
                                     Lazy<Optional<A>> oInit) {
        return Stream.filterOptional(
                sA.collectLazy(
                        oInit,
                        (A a, Optional<A> oLastA) -> {
                            Optional<A> oa = Optional.of(a);
                            return oa.equals(oLastA)
                                    ? new Tuple2<>(
                                    Optional.empty(), oLastA)
                                    : new Tuple2<>(oa, oa);
                        }
                ));
    }

    public static <A> Stream<A> calm(Stream<A> sA) {
        return calm(sA, new Lazy<>(Optional.empty()));
    }

    public static <A> Cell<A> calm(Cell<A> a) {
        Lazy<A> initA = a.sampleLazy();
        Lazy<Optional<A>> oInitA = initA.map(Optional::of);
        return calm(Operational.updates(a), oInitA).holdLazy(initA);
    }

    public static void main(String[] args) {
        CellSink<Integer> sa = new CellSink<>(1);
        Listener l = calm(sa).listen(System.out::println);
        sa.send(1);
        sa.send(2);
        sa.send(2);
        sa.send(4);
        sa.send(4);
        sa.send(1);
        l.unlisten();
    }
}
