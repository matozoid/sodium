package lib;

import nz.sodium.Cell;
import nz.sodium.Stream;
import nz.sodium.Unit;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class BitableHomoSapiens {
    public BitableHomoSapiens(
            World world,
            int self,
            Point posInit,
            Cell<Double> time,
            Stream<Unit> sTick,
            Stream<Set<Integer>> sBite,
            Cell<List<Character>> scene) {
        HomoSapiens h = new HomoSapiens(world, self, posInit, time, sTick);
        Stream<Set<Integer>> sBiteMe = sBite.filter(ids -> ids.contains(self));
        Stream<HomoZombicus> sBecome = sBiteMe.snapshot(
                h.character,
                (id, ch) -> new HomoZombicus(
                        self,
                        ch.pos,
                        time,
                        sTick, scene
                )
        );
        this.character = Cell.switchC(
                sBecome.map(z -> z.character).hold(h.character)
        );
        this.sBite = Cell.switchS(
                sBecome.map(z -> z.sBite).hold(new Stream<>())
        );
    }

    public final Cell<Character> character;
    public final Stream<Integer> sBite;
}

