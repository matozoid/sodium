package fridgets;

import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.CellLoop;
import nz.sodium.Stream;

import java.awt.*;
import java.util.Collection;
import java.util.Optional;

public class FrFlow extends Fridget {
    public enum Direction {HORIZONTAL, VERTICAL}

    ;

    public FrFlow(Direction dir, Collection<Fridget> fridgets) {
        super((size, sMouse, sKey, focus, idSupply) -> {
            Cell<Dimension> desiredSize = new Cell<>(new Dimension(0, 0));
            Cell<Drawable> drawable = new Cell<>(new Drawable());
            Stream<Long> sChangeFocus = new Stream<>();
            for (Fridget fridget : fridgets) {
                CellLoop<Option<Dimension>> childSz = new CellLoop<>();
                Fridget.Output fo = new FrTranslate(fridget,
                        dir == Direction.HORIZONTAL
                                ? desiredSize.map(dsz -> new Dimension(dsz.width, 0))
                                : desiredSize.map(dsz -> new Dimension(0, dsz.height)))
                        .reify(childSz, sMouse, sKey, focus,
                                idSupply.child1());
                idSupply = idSupply.child2();
                childSz.loop(
                        size.lift(fo.desiredSize, (osz, foDsz) ->
                                osz.isDefined()
                                        ? Option.some(dir == Direction.HORIZONTAL
                                        ? new Dimension(foDsz.width,
                                        osz.get().height)
                                        : new Dimension(osz.get().width,
                                        foDsz.height))
                                        : Option.none()
                        )
                );
                desiredSize = desiredSize.lift(fo.desiredSize,
                        dir == Direction.HORIZONTAL
                                ? (dsz, foDsz) -> new Dimension(
                                dsz.width + foDsz.width,
                                dsz.height > foDsz.height ? dsz.height
                                        : foDsz.height)
                                : (dsz, foDsz) -> new Dimension(
                                dsz.width > foDsz.width ? dsz.width
                                        : foDsz.width,
                                dsz.height + foDsz.height));
                drawable = drawable.lift(fo.drawable,
                        (drA, drB) -> drA.append(drB));
                sChangeFocus = sChangeFocus.orElse(fo.sChangeFocus);
            }
            return new Fridget.Output(drawable, desiredSize, sChangeFocus);
        });
    }
}
