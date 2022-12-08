import io.vavr.Tuple2;
import io.vavr.control.Option;
import nz.sodium.*;

import java.util.Optional;

public class CalmExample {
    public static <A> Stream<A> calm(Stream<A> sA,
                                     Lazy<Option<A>> oInit) {
        return Stream.filterOptional(
                sA.collectLazy(
                        oInit,
                        (A a, Option<A> oLastA) -> {
                            Option<A> oa = Option.some(a);
                            return oa.equals(oLastA)
                                    ? new Tuple2<>(
                                    Option.none(), oLastA)
                                    : new Tuple2<>(oa, oa);
                        }
                ));
    }

    public static <A> Stream<A> calm(Stream<A> sA) {
        return calm(sA, new Lazy<>(Option.none()));
    }

    public static <A> Cell<A> calm(Cell<A> a) {
        Lazy<A> initA = a.sampleLazy();
        Lazy<Option<A>> oInitA = initA.map(Option::some);
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
