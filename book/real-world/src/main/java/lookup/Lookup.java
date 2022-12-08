package lookup;

import io.vavr.Function1;
import io.vavr.control.Option;
import nz.sodium.*;
import swidgets.SButton;
import swidgets.STextArea;
import swidgets.STextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

class IsBusy<A, B> {
    public IsBusy(Function1<Stream<A>, Stream<B>> action, Stream<A> sIn) {
        sOut = action.apply(sIn);
        busy = sOut.map(i -> false)
                .orElse(sIn.map(i -> true))
                .hold(false);
    }

    public final Stream<B> sOut;
    public final Cell<Boolean> busy;
}

public class Lookup {

    public static final
    Function1<Stream<String>, Stream<Option<String>>> lookup = sWord -> {
        StreamSink<Option<String>> sDefinition = new StreamSink<>();
        Listener l = sWord.listenWeak(wrd -> new Thread(() -> {
            System.out.println("look up " + wrd);
            Option<String> def = Option.none();
            try {
                try (Socket s = new Socket(InetAddress.getByName("dict.org"), 2628)) {
                    BufferedReader r = new BufferedReader(
                            new InputStreamReader(s.getInputStream(), UTF_8));
                    PrintWriter w = new PrintWriter(
                            new OutputStreamWriter(s.getOutputStream(), UTF_8));
                    String greeting = r.readLine();
                    w.println("DEFINE ! " + wrd);
                    w.flush();
                    String result = r.readLine();
                    if (result.startsWith("150"))
                        result = r.readLine();
                    if (result.startsWith("151")) {
                        StringBuilder b = new StringBuilder();
                        while (true) {
                            String l1 = r.readLine();
                            if (l1.equals("."))
                                break;
                            b.append(l1).append("\n");
                        }
                        def = Option.some(b.toString());
                    } else
                        System.out.println("ERROR: " + result);
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                sDefinition.send(def);
            }
        }).start());
        return sDefinition.addCleanup(l);
    };

    public static void main(String[] args) {
        JFrame view = new JFrame("Dictionary lookup");
        GridBagLayout gridbag = new GridBagLayout();
        view.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.0;
        c.weightx = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;

        Transaction.runVoid(() -> {
            STextField word = new STextField("", 25);
            CellLoop<Boolean> enabled = new CellLoop<>();
            SButton button = new SButton("look up", enabled);
            Stream<String> sWord = button.sClicked.snapshot(word.text);
            IsBusy<String, Option<String>> ib =
                    new IsBusy<>(lookup, sWord);
            Stream<String> sDefinition = ib.sOut
                    .map(o -> o.getOrElse("ERROR!"));
            Cell<String> definition = sDefinition.hold("");
            Cell<String> output = definition.lift(ib.busy, (def, bsy) ->
                    bsy ? "Looking up..." : def);
            enabled.loop(ib.busy.map(b -> !b));
            STextArea outputArea = new STextArea(output, enabled);
            view.add(word, c);
            c.gridx = 1;
            view.add(button, c);
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1.0;
            c.gridwidth = 2;
            c.gridx = 0;
            c.gridy = 1;
            view.add(new JScrollPane(outputArea), c);
        });

        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        view.setSize(500, 250);
        view.setVisible(true);
    }
}

