import io.vavr.Function2;
import nz.sodium.Cell;
import nz.sodium.Stream;

public class StreamJunction<A> extends Junction<Stream<A>, A> {
    public StreamJunction(Function2<A, A, A> combine) {
        this.out = Cell.switchS(clients.map(cls ->
                Stream.merge(cls, combine)));
    }

    public Stream<A> out;
}

