import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.control.Option;
import nz.sodium.*;

import java.util.Optional;

public abstract class Value<A> {
    public abstract ValueOutput<A> construct(Stream<A> sWrite);

    public final <B> Value<B> map(Bijection<A, B> bij) {
        Value<A> va = this;
        return new Value<>() {
            public ValueOutput<B> construct(Stream<B> sWriteB) {
                ValueOutput<A> out = va.construct(sWriteB.map(bij.fInv));
                return new ValueOutput<B>(
                        out.value.map(oa ->
                                oa.isDefined() ? Option.some(bij.f.apply(oa.get()))
                                        : Option.none()),
                        out.cleanup);
            }
        };
    }

    public final <B> Value<B> lens(
            Function1<A, B> getter,
            Function2<A, B, A> setter) {
        Value<A> va = this;
        return new Value<>() {
            public ValueOutput<B> construct(Stream<B> sWriteB) {
                return Transaction.run(() -> {
                    StreamLoop<A> sWriteA = new StreamLoop<>();
                    ValueOutput<A> out = va.construct(sWriteA);
                    Cell<Option<A>> oa = out.value;
                    sWriteA.loop(Stream.filterOptional(
                            sWriteB.snapshot(oa, (wb, oa_) ->
                                    oa_.isDefined()
                                            ? Option.some(setter.apply(oa_.get(), wb))
                                            : Option.none()
                            )
                    ));
                    return new ValueOutput<B>(
                            oa.map(oa_ ->
                                    oa_.isDefined()
                                            ? Option.some(getter.apply(oa_.get()))
                                            : Option.none()),
                            out.cleanup
                    );
                });
            }
        };
    }
}
