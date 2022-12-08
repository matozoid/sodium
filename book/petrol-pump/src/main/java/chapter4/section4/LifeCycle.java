package chapter4.section4;

import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.CellLoop;
import nz.sodium.Stream;
import pump.Fuel;
import pump.UpDown;

public class LifeCycle {
    public final Stream<Fuel> sStart;
    public final Cell<Option<Fuel>> fillActive;
    public final Stream<End> sEnd;

    public enum End {END}

    private static Stream<Fuel> whenLifted(Stream<UpDown> sNozzle,
                                           Fuel nozzleFuel) {
        return sNozzle.filter(u -> u == UpDown.UP)
                .map(u -> nozzleFuel);
    }

    private static Stream<End> whenSetDown(Stream<UpDown> sNozzle,
                                           Fuel nozzleFuel,
                                           Cell<Option<Fuel>> fillActive) {
        return Stream.filterOptional(
                sNozzle.snapshot(fillActive,
                        (u, f) -> u == UpDown.DOWN &&
                                f.equals(Option.some(nozzleFuel))
                                ? Option.some(End.END)
                                : Option.none()));
    }

    public LifeCycle(Stream<UpDown> sNozzle1,
                     Stream<UpDown> sNozzle2,
                     Stream<UpDown> sNozzle3) {
        Stream<Fuel> sLiftNozzle =
                whenLifted(sNozzle1, Fuel.ONE).orElse(
                        whenLifted(sNozzle2, Fuel.TWO).orElse(
                                whenLifted(sNozzle3, Fuel.THREE)));
        CellLoop<Option<Fuel>> fillActive = new CellLoop<>();
        this.fillActive = fillActive;
        this.sStart = Stream.filterOptional(
                sLiftNozzle.snapshot(fillActive, (newFuel, fillActive_) ->
                        fillActive_.isDefined() ? Option.none()
                                : Option.some(newFuel)));
        this.sEnd = whenSetDown(sNozzle1, Fuel.ONE, fillActive).orElse(
                whenSetDown(sNozzle2, Fuel.TWO, fillActive).orElse(
                        whenSetDown(sNozzle3, Fuel.THREE, fillActive)));
        fillActive.loop(
                sEnd.map(e -> Option.<Fuel>none())
                        .orElse(sStart.map(Option::some))
                        .hold(Option.none())
        );
    }
}

