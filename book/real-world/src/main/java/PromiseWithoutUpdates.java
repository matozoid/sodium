import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.control.Option;
import nz.sodium.*;

import java.util.Optional;

public class PromiseWithoutUpdates<A> {
    public PromiseWithoutUpdates(Stream<A> sDeliver) {
        this.sDeliver = sDeliver.once();
        this.oValue = this.sDeliver.map(a -> Option.some(a))
                .hold(Option.none());
    }

    private PromiseWithoutUpdates(Stream<A> sDeliver, Cell<Option<A>> oValue) {
        this.sDeliver = sDeliver;
        this.oValue = oValue;
    }

    public final Stream<A> sDeliver;
    public final Cell<Option<A>> oValue;

    public final Stream<A> then() {
        return Stream.filterOptional(Operational.value(oValue))
                .orElse(sDeliver).once();
    }

    public final void thenDo(Handler<A> h) {
        Transaction.runVoid(() ->
                then().listenOnce(h)
        );
    }

    public static <A, B, C> PromiseWithoutUpdates<C> lift(final Function2<A, B, C> f,
                                                          PromiseWithoutUpdates<A> pa, PromiseWithoutUpdates<B> pb) {
        return Transaction.run(() -> {
            class Tuple {
                Tuple(Option<A> oa, Option<B> ob) {
                    this.oa = oa;
                    this.ob = ob;
                }

                Option<A> oa;
                Option<B> ob;
            }
            ;
            Function2<Tuple, Tuple, Tuple> combine = (l, r) -> new Tuple(l.oa.isDefined() ? l.oa : r.oa, l.ob.isDefined() ? l.ob : r.ob);
            Function1<Tuple, Option<C>> result = t -> t.oa.isDefined() && t.ob.isDefined()
                    ? Option.some(f.apply(t.oa.get(), t.ob.get()))
                    : Option.none();
            Stream<Tuple> sA = pa.sDeliver.map(a -> new Tuple(Option.some(a), Option.none()));
            Cell<Tuple> vA = pa.oValue.map(oa -> new Tuple(oa, Option.none()));
            Stream<Tuple> sB = pb.sDeliver.map(b -> new Tuple(Option.none(), Option.some(b)));
            Cell<Tuple> vB = pb.oValue.map(ob -> new Tuple(Option.none(), ob));
            Stream<Tuple> sAArrives = sA.snapshot(vB, combine);
            Stream<Tuple> sBArrives = sB.snapshot(vA, combine);
            Stream<Tuple> sSimultaneous = sA.merge(sB, combine);
            Stream<C> sDeliver = Stream.filterOptional(
                    sAArrives.orElse(sBArrives)
                            .orElse(sSimultaneous)
                            .map(result)
            ).once();
            Cell<Option<C>> oValue = vA.lift(vB,
                    combine).map(result);
            return new PromiseWithoutUpdates<>(sDeliver, oValue);
        });
    }
}
