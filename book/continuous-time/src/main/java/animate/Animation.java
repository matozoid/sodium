package animate;

import nz.sodium.Cell;
import nz.sodium.time.TimerSystem;

public interface Animation {
    Cell<Drawable> create(TimerSystem<Double> sys, Point extents);
}

