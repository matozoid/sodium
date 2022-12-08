package fridgets;

import io.vavr.Function5;
import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.Stream;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Optional;

public abstract class Fridget {
    public static class Output {
        public Output(
                Cell<Drawable> drawable,
                Cell<Dimension> desiredSize,
                Stream<Long> sChangeFocus) {
            this.drawable = drawable;
            this.desiredSize = desiredSize;
            this.sChangeFocus = sChangeFocus;
        }

        public Cell<Drawable> drawable;
        public Cell<Dimension> desiredSize;
        public Stream<Long> sChangeFocus;
    }

    public Fridget(Function5<
                Cell<Option<Dimension>>, Stream<MouseEvent>,
                Stream<KeyEvent>, Cell<Long>, Supply, Output> reify_) {
        this.reify_ = reify_;
    }

    private final Function5<
            Cell<Option<Dimension>>, Stream<MouseEvent>,
            Stream<KeyEvent>, Cell<Long>, Supply, Output> reify_;

    public final Output reify(
            Cell<Option<Dimension>> size,
            Stream<MouseEvent> sMouse, Stream<KeyEvent> sKey,
            Cell<Long> focus, Supply idSupply) {
        return reify_.apply(size, sMouse, sKey, focus, idSupply);
    }
}

