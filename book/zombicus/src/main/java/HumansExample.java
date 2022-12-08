import lib.Animate;
import lib.Character;
import lib.HomoSapiens;
import lib.World;
import nz.sodium.Cell;
import nz.sodium.Stream;
import nz.sodium.Unit;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HumansExample {
    static <A> Cell<List<A>> sequence(Collection<Cell<A>> in) {
        Cell<List<A>> out = new Cell<>(new ArrayList<>());
        for (Cell<A> c : in)
            out = out.lift(c,
                    (list0, a) -> {
                        List<A> list = new ArrayList<>(list0);
                        list.add(a);
                        return list;
                    });
        return out;
    }

    public static void main(String[] args) {
        Animate.animate(
                "Zombicus humans",
                (Cell<Double> time, Stream<Unit> sTick,
                 Dimension windowSize) -> {
                    World world = new World(windowSize);
                    List<Cell<Character>> chars = new ArrayList<>();
                    int id = 0;
                    for (int x = 100; x < windowSize.width; x += 100)
                        for (int y = 150; y < windowSize.height; y += 150) {
                            Point pos0 = new Point(x, y);
                            HomoSapiens h = new HomoSapiens(world, id,
                                    pos0, time, sTick);
                            chars.add(h.character);
                            id++;
                        }
                    return sequence(chars);
                }
        );
    }
}

