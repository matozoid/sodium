package fridgets;

import io.vavr.control.Option;

public class Supply {
    private static class Impl {
        private long nextID = 0;

        public final synchronized long alloc() {
            return ++nextID;
        }
    }

    public Supply() {
        this.impl = new Impl();
    }

    private Supply(Impl impl) {
        this.impl = impl;
    }

    private final Impl impl;
    private Option<Long> oID = Option.none();
    private Option<Supply> oChild1 = Option.none();
    private Option<Supply> oChild2 = Option.none();

    public final synchronized long get() {
        if (!oID.isDefined())
            oID = Option.some(impl.alloc());
        return oID.get();
    }

    public final synchronized Supply child1() {
        if (!oChild1.isDefined())
            oChild1 = Option.some(new Supply(impl));
        return oChild1.get();
    }

    public final synchronized Supply child2() {
        if (!oChild2.isDefined())
            oChild2 = Option.some(new Supply(impl));
        return oChild2.get();
    }
}

