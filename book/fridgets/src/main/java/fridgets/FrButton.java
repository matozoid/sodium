package fridgets;

import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.Stream;
import nz.sodium.StreamLoop;
import nz.sodium.Unit;

import java.awt.*;
import java.awt.event.MouseEvent;

public class FrButton extends Fridget {
    public FrButton(Cell<String> label) {
        this(label, new StreamLoop<>());
    }

    private FrButton(Cell<String> label, StreamLoop<Unit> sClicked) {
        super((size, sMouse, sKey, focus, idSupply) -> {
            Stream<Unit> sPressed = Stream.filterOptional(
                    sMouse.snapshot(size, (e, osz) ->
                            osz.isDefined() &&
                                    e.getID() == MouseEvent.MOUSE_PRESSED
                                    && e.getX() >= 2 && e.getX() < osz.get().width - 2
                                    && e.getY() >= 2 && e.getY() < osz.get().height - 2
                                    ? Option.some(Unit.UNIT)
                                    : Option.none()
                    )
            );
            Stream<Unit> sReleased = Stream.filterOptional(
                    sMouse.map(e -> e.getID() == MouseEvent.MOUSE_RELEASED
                            ? Option.some(Unit.UNIT)
                            : Option.none()));
            Cell<Boolean> pressed =
                    sPressed.map(u -> true)
                            .orElse(sReleased.map(u -> false))
                            .hold(false);
            sClicked.loop(sReleased.gate(pressed));
            Font font = new Font("Helvetica", Font.PLAIN, 13);
            Canvas c = new Canvas();
            FontMetrics fm = c.getFontMetrics(font);
            Cell<Dimension> desiredSize = label.map(label_ ->
                    new Dimension(
                            fm.stringWidth(label_) + 14,
                            fm.getHeight() + 10));
            return new Output(
                    label.lift(size, pressed,
                            (label_, osz, pressed_) -> new Drawable() {
                                public void draw(Graphics g) {
                                    if (osz.isDefined()) {
                                        Dimension sz = osz.get();
                                        int w = fm.stringWidth(label_);
                                        g.setColor(pressed_ ? Color.darkGray : Color.lightGray);
                                        g.fillRect(3, 3, sz.width - 6, sz.height - 6);
                                        g.setColor(Color.black);
                                        g.drawRect(2, 2, sz.width - 5, sz.height - 5);
                                        g.setFont(font);
                                        g.drawString(label_,
                                                (sz.width - w) / 2,
                                                (sz.height - fm.getHeight()) / 2
                                                        + fm.getAscent());
                                    }
                                }
                            }
                    ),
                    desiredSize,
                    new Stream<>()
            );
        });
        this.sClicked = sClicked;
    }

    public final Stream<Unit> sClicked;
}

