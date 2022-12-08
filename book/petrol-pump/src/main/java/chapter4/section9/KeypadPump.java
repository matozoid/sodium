package chapter4.section9;

import nz.sodium.Stream;
import pump.Formatters;
import pump.Inputs;
import pump.Outputs;
import pump.Pump;

public class KeypadPump implements Pump {
    public Outputs create(Inputs inputs) {
        Keypad ke = new Keypad(inputs.sKeypad, new Stream<>());
        return new Outputs()
                .setPresetLCD(ke.value.map(v ->
                        Formatters.formatSaleCost((double) v)))
                .setBeep(ke.sBeep);
    }
}

