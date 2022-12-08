package junction;

import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.Listener;

import java.util.Optional;

public class ValueOutput<A> {
    public ValueOutput(Cell<Option<A>> value, Listener cleanup) {
        this.value = value;
        this.cleanup = cleanup;
    }

    public final Cell<Option<A>> value;
    public final Listener cleanup;
}

