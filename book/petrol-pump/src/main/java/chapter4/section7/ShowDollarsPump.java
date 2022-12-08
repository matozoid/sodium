package chapter4.section7;

import chapter4.section4.LifeCycle;
import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.Unit;
import pump.*;

import java.util.Optional;

public class ShowDollarsPump implements Pump {
    public Outputs create(Inputs inputs) {
        LifeCycle lc = new LifeCycle(inputs.sNozzle1,
                inputs.sNozzle2,
                inputs.sNozzle3);
        Fill fi = new Fill(lc.sStart.map(u -> Unit.UNIT),
                inputs.sFuelPulses, inputs.calibration,
                inputs.price1, inputs.price2, inputs.price3,
                lc.sStart);
        return new Outputs()
                .setDelivery(lc.fillActive.map(
                        of ->
                                of.equals(Option.some(Fuel.ONE)) ? Delivery.FAST1 :
                                        of.equals(Option.some(Fuel.TWO)) ? Delivery.FAST2 :
                                                of.equals(Option.some(Fuel.THREE)) ? Delivery.FAST3 :
                                                        Delivery.OFF))
                .setSaleCostLCD(fi.dollarsDelivered.map(
                        q -> Formatters.formatSaleCost(q)))
                .setSaleQuantityLCD(fi.litersDelivered.map(
                        q -> Formatters.formatSaleQuantity(q)))
                .setPriceLCD1(priceLCD(lc.fillActive, fi.price, Fuel.ONE,
                        inputs))
                .setPriceLCD2(priceLCD(lc.fillActive, fi.price, Fuel.TWO,
                        inputs))
                .setPriceLCD3(priceLCD(lc.fillActive, fi.price, Fuel.THREE,
                        inputs));
    }

    public static Cell<String> priceLCD(
            Cell<Option<Fuel>> fillActive,
            Cell<Double> fillPrice,
            Fuel fuel,
            Inputs inputs) {
        Cell<Double> idlePrice;
        switch (fuel) {
            case ONE:
                idlePrice = inputs.price1;
                break;
            case TWO:
                idlePrice = inputs.price2;
                break;
            case THREE:
                idlePrice = inputs.price3;
                break;
            default:
                idlePrice = null;
        }
        return fillActive.lift(fillPrice, idlePrice,
                (oFuelSelected, fillPrice_, idlePrice_) ->
                        oFuelSelected.isDefined()
                                ? oFuelSelected.get() == fuel
                                ? Formatters.formatPrice(fillPrice_)
                                : ""
                                : Formatters.formatPrice(idlePrice_));
    }
}

