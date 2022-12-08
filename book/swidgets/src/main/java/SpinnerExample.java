import nz.sodium.CellLoop;
import nz.sodium.Stream;
import nz.sodium.Transaction;
import swidgets.SButton;
import swidgets.SLabel;

import javax.swing.*;
import java.awt.*;

public class SpinnerExample {
    public static void main(String[] args) {
        JFrame view = new JFrame("spinner");
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        view.setLayout(new FlowLayout());
        Transaction.runVoid(() -> {
            CellLoop<Integer> value = new CellLoop<>();
            SLabel lblValue = new SLabel(
                    value.map(i -> Integer.toString(i)));
            SButton plus = new SButton("+");
            SButton minus = new SButton("-");
            view.add(lblValue);
            view.add(plus);
            view.add(minus);
            Stream<Integer> sPlusDelta = plus.sClicked.map(u -> 1);
            Stream<Integer> sMinusDelta = minus.sClicked.map(u -> -1);
            Stream<Integer> sDelta = sPlusDelta.orElse(sMinusDelta);
            Stream<Integer> sUpdate = sDelta.snapshot(value,
                    Integer::sum
            );
            value.loop(sUpdate.hold(0));
        });
        view.setSize(400, 160);
        view.setVisible(true);
    }
}

