import nz.sodium.Cell;
import nz.sodium.Stream;
import swidgets.SButton;
import swidgets.SLabel;
import swidgets.STextField;

import javax.swing.*;
import java.awt.*;

public class TranslateExample {
    public static void main(String[] args) {
        JFrame view = new JFrame("Translate");
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        view.setLayout(new FlowLayout());
        STextField english = new STextField("I like FRP");
        SButton translate = new SButton("Translate");
        Stream<String> sLatin =
                translate.sClicked.snapshot(english.text, (u, txt) ->
                        txt.trim().replaceAll(" |$", "us ").trim()
                );
        Cell<String> latin = sLatin.hold("");
        SLabel lblLatin = new SLabel(latin);
        view.add(english);
        view.add(translate);
        view.add(lblLatin);
        view.setSize(400, 160);
        view.setVisible(true);
    }
}

