package chapter4.section4;

import io.vavr.control.Option;
import pump.*;

import java.util.Optional;

public class LifeCyclePump implements Pump {
    public Outputs create(Inputs inputs) {
        LifeCycle lc = new LifeCycle(inputs.sNozzle1,
                inputs.sNozzle2,
                inputs.sNozzle3);
        return new Outputs()
                .setDelivery(lc.fillActive.map(
                        of ->
                                of.equals(Option.some(Fuel.ONE)) ? Delivery.FAST1 :
                                        of.equals(Option.some(Fuel.TWO)) ? Delivery.FAST2 :
                                                of.equals(Option.some(Fuel.THREE)) ? Delivery.FAST3 :
                                                        Delivery.OFF))
                .setSaleQuantityLCD(lc.fillActive.map(
                        of ->
                                of.equals(Option.some(Fuel.ONE)) ? "1" :
                                        of.equals(Option.some(Fuel.TWO)) ? "2" :
                                                of.equals(Option.some(Fuel.THREE)) ? "3" : ""));
    }
}

