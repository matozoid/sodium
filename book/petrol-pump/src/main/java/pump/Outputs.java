package pump;

import nz.sodium.Cell;
import nz.sodium.Stream;
import nz.sodium.Unit;

public class Outputs {
    private Outputs(
            Cell<Delivery> delivery,
            Cell<String> presetLCD,
            Cell<String> saleCostLCD,
            Cell<String> saleQuantityLCD,
            Cell<String> priceLCD1,
            Cell<String> priceLCD2,
            Cell<String> priceLCD3,
            Stream<Unit> sBeep,
            Stream<Sale> sSaleComplete) {
        this.delivery = delivery;
        this.presetLCD = presetLCD;
        this.saleCostLCD = saleCostLCD;
        this.saleQuantityLCD = saleQuantityLCD;
        this.priceLCD1 = priceLCD1;
        this.priceLCD2 = priceLCD2;
        this.priceLCD3 = priceLCD3;
        this.sBeep = sBeep;
        this.sSaleComplete = sSaleComplete;
    }

    public Outputs() {
        this.delivery = new Cell<>(Delivery.OFF);
        this.presetLCD = new Cell<>("");
        this.saleCostLCD = new Cell<>("");
        this.saleQuantityLCD = new Cell<>("");
        this.priceLCD1 = new Cell<>("");
        this.priceLCD2 = new Cell<>("");
        this.priceLCD3 = new Cell<>("");
        this.sBeep = new Stream<>();
        this.sSaleComplete = new Stream<>();
    }

    public final Cell<Delivery> delivery;
    public final Cell<String> presetLCD;
    public final Cell<String> saleCostLCD;
    public final Cell<String> saleQuantityLCD;
    public final Cell<String> priceLCD1;
    public final Cell<String> priceLCD2;
    public final Cell<String> priceLCD3;
    public final Stream<Unit> sBeep;
    public final Stream<Sale> sSaleComplete;

    public Outputs setDelivery(Cell<Delivery> delivery) {
        return new Outputs(delivery, presetLCD, saleCostLCD,
                saleQuantityLCD, priceLCD1, priceLCD2, priceLCD3, sBeep,
                sSaleComplete);
    }

    public Outputs setPresetLCD(Cell<String> presetLCD) {
        return new Outputs(delivery, presetLCD, saleCostLCD,
                saleQuantityLCD, priceLCD1, priceLCD2, priceLCD3, sBeep,
                sSaleComplete);
    }

    public Outputs setSaleCostLCD(Cell<String> saleCostLCD) {
        return new Outputs(delivery, presetLCD, saleCostLCD,
                saleQuantityLCD, priceLCD1, priceLCD2, priceLCD3, sBeep,
                sSaleComplete);
    }

    public Outputs setSaleQuantityLCD(Cell<String> saleQuantityLCD) {
        return new Outputs(delivery, presetLCD, saleCostLCD,
                saleQuantityLCD, priceLCD1, priceLCD2, priceLCD3, sBeep,
                sSaleComplete);
    }

    public Outputs setPriceLCD1(Cell<String> priceLCD1) {
        return new Outputs(delivery, presetLCD, saleCostLCD,
                saleQuantityLCD, priceLCD1, priceLCD2, priceLCD3, sBeep,
                sSaleComplete);
    }

    public Outputs setPriceLCD2(Cell<String> priceLCD2) {
        return new Outputs(delivery, presetLCD, saleCostLCD,
                saleQuantityLCD, priceLCD1, priceLCD2, priceLCD3, sBeep,
                sSaleComplete);
    }

    public Outputs setPriceLCD3(Cell<String> priceLCD3) {
        return new Outputs(delivery, presetLCD, saleCostLCD,
                saleQuantityLCD, priceLCD1, priceLCD2, priceLCD3, sBeep,
                sSaleComplete);
    }

    public Outputs setBeep(Stream<Unit> sBeep) {
        return new Outputs(delivery, presetLCD, saleCostLCD,
                saleQuantityLCD, priceLCD1, priceLCD2, priceLCD3, sBeep,
                sSaleComplete);
    }

    public Outputs setSaleComplete(Stream<Sale> sSaleComplete) {
        return new Outputs(delivery, presetLCD, saleCostLCD,
                saleQuantityLCD, priceLCD1, priceLCD2, priceLCD3, sBeep,
                sSaleComplete);
    }
}

