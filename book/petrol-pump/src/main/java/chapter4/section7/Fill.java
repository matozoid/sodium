package chapter4.section7;

import chapter4.section6.AccumulatePulsesPump;
import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.Stream;
import nz.sodium.Unit;
import pump.Fuel;

public class Fill {
    public final Cell<Double> price;
    public final Cell<Double> dollarsDelivered;
    public final Cell<Double> litersDelivered;

    public Fill(
            Stream<Unit> sClearAccumulator, Stream<Integer> sFuelPulses,
            Cell<Double> calibration, Cell<Double> price1,
            Cell<Double> price2, Cell<Double> price3,
            Stream<Fuel> sStart) {
        price = capturePrice(sStart, price1, price2, price3);
        litersDelivered = AccumulatePulsesPump.accumulate(
                sClearAccumulator, sFuelPulses, calibration);
        dollarsDelivered = litersDelivered.lift(price,
                (liters, price_) -> liters * price_);
    }

    public static Cell<Double> capturePrice(
            Stream<Fuel> sStart,
            Cell<Double> price1, Cell<Double> price2,
            Cell<Double> price3) {
        Stream<Double> sPrice1 = Stream.filterOptional(
                sStart.snapshot(price1,
                        (f, p) -> f == Fuel.ONE ? Option.some(p)
                                : Option.none()));
        Stream<Double> sPrice2 = Stream.filterOptional(
                sStart.snapshot(price2,
                        (f, p) -> f == Fuel.TWO ? Option.some(p)
                                : Option.none()));
        Stream<Double> sPrice3 = Stream.filterOptional(
                sStart.snapshot(price3,
                        (f, p) -> f == Fuel.THREE ? Option.some(p)
                                : Option.none()));

        return sPrice1.orElse(sPrice2.orElse(sPrice3))
                .hold(0.0);
    }
}
