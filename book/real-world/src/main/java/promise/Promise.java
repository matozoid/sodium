package promise;

import io.vavr.Function2;
import io.vavr.control.Option;
import nz.sodium.*;

public class Promise<A> {
    public Promise(Stream<A> sDeliver) {
        this.sDeliver = sDeliver.once();
        this.oValue = this.sDeliver.map(a -> Option.some(a))
                .hold(Option.none());
    }

    private Promise(Cell<Option<A>> oValue) {
        this.sDeliver = Stream.filterOptional(Operational.updates(oValue));
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

    public <B, C> Promise<C> lift(Promise<B> pb,
                                  final Function2<A, B, C> f) {
        return Transaction.run(() -> new Promise<C>(
                this.oValue.lift(pb.oValue,
                        (oa, ob) ->
                                oa.isDefined() && ob.isDefined()
                                        ? Option.some(f.apply(oa.get(), ob.get()))
                                        : Option.none()
                )));
    }
}

