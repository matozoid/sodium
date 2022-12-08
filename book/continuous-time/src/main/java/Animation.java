import nz.sodium.Cell;
import nz.sodium.time.TimerSystem;

public interface Animation {
    public Cell<Drawable> create(TimerSystem<Double> sys, Point extents);
}

