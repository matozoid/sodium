package pump;

import nz.sodium.Cell;
import nz.sodium.CellSink;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class SComboBox<A> extends JComboBox {
    @SuppressWarnings("unchecked")
    public SComboBox(ComboBoxModel<A> aModel) {
        super(aModel);

        CellSink<A> selectedItem = new CellSink<A>((A) getSelectedItem());
        addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    selectedItem.send((A) e.getItem());
            }
        });
        this.selectedItem = selectedItem;
    }

    public final Cell<A> selectedItem;
}

