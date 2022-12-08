package chapter4.section8;

import chapter4.section4.LifeCycle;
import chapter4.section4.LifeCycle.End;
import chapter4.section7.Fill;
import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.CellLoop;
import nz.sodium.Stream;
import nz.sodium.Unit;
import pump.Fuel;
import pump.Sale;

import java.util.Optional;

public class NotifyPointOfSale {
    public final Stream<Fuel> sStart;
    public final Cell<Option<Fuel>> fillActive;
    public final Cell<Option<Fuel>> fuelFlowing;
    public final Stream<End> sEnd;
    public final Stream<Unit> sBeep;
    public final Stream<Sale> sSaleComplete;

    private enum Phase {IDLE, FILLING, POS}

    ;

    public NotifyPointOfSale(
            LifeCycle lc,
            Stream<Unit> sClearSale,
            Fill fi) {
        CellLoop<Phase> phase = new CellLoop<>();
        sStart = lc.sStart.gate(phase.map(p -> p == Phase.IDLE));
        sEnd = lc.sEnd.gate(phase.map(p -> p == Phase.FILLING));
        phase.loop(
                sStart.map(u -> Phase.FILLING)
                        .orElse(sEnd.map(u -> Phase.POS))
                        .orElse(sClearSale.map(u -> Phase.IDLE))
                        .hold(Phase.IDLE));
        fuelFlowing =
                sStart.map(f -> Option.some(f)).orElse(
                        sEnd.map(f -> Option.none())).hold(Option.none());
        fillActive =
                sStart.map(f -> Option.some(f)).orElse(
                        sClearSale.map(f -> Option.none())).hold(Option.none());
        sBeep = sClearSale;
        sSaleComplete = Stream.filterOptional(sEnd.snapshot(
                fuelFlowing.lift(fi.price, fi.dollarsDelivered,
                        fi.litersDelivered,
                        (oFuel, price_, dollars, liters) ->
                                oFuel.isDefined() ? Option.some(
                                        new Sale(oFuel.get(), price_, dollars, liters))
                                        : Option.none())
        ));
    }
}

