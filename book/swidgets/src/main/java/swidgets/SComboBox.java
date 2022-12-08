package swidgets;

import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.CellSink;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Optional;
import java.util.Vector;

public class SComboBox<E> extends JComboBox<E> {
    public SComboBox() {
        selectedItem = mkSelectedItem(this);
    }

    public SComboBox(ComboBoxModel<E> aModel) {
        super(aModel);
        selectedItem = mkSelectedItem(this);
    }

    public SComboBox(E[] items) {
        super(items);
        selectedItem = mkSelectedItem(this);
    }

    public SComboBox(Vector<E> items) {
        super(items);
        selectedItem = mkSelectedItem(this);
    }

    @SuppressWarnings("unchecked")
    private static <E> Cell<Option<E>> mkSelectedItem(JComboBox<E> box) {
        E sel = (E) box.getSelectedItem();
        CellSink<Option<E>> selectedItem = new CellSink<>(
                sel == null ? Option.none() : Option.some(sel));
        box.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
                selectedItem.send(Option.some((E) e.getItem()));
        });
        return selectedItem;
    }

    public final Cell<Option<E>> selectedItem;
}

