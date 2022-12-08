package junction;

import nz.sodium.Lambda1;
import io.vavr.Function1;

public class Bijection<A, B> {
    public Bijection(Function1<A, B> f, Function1<B, A> fInv) {
        this.f = f;
        this.fInv = fInv;
    }

    public final Function1<A, B> f;
    public final Function1<B, A> fInv;
}

