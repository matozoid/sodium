package fridgets;

import nz.sodium.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

public class FrView extends JPanel {
    public FrView(JFrame frame, Fridget fr) {
        StreamSink<MouseEvent> sMouse = new StreamSink<>();
        StreamSink<KeyEvent> sKey = new StreamSink<>();
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                sMouse.send(e);
            }

            public void mouseReleased(MouseEvent e) {
                sMouse.send(e);
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                sMouse.send(e);
            }

            public void mouseMoved(MouseEvent e) {
                sMouse.send(e);
            }
        });
        size = new CellSink<>(Optional.empty());
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (e.getID() == ComponentEvent.COMPONENT_RESIZED)
                    size.send(Optional.of(getSize()));
            }
        });
        frame.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                sKey.send(e);
            }
        });
        CellLoop<Long> focus = new CellLoop<>();
        Fridget.Output fo = fr.reify(size, sMouse, sKey, focus,
                new Supply());
        focus.loop(fo.sChangeFocus.hold(-1L));
        this.drawable = fo.drawable;
        l = l.append(Operational.updates(drawable).listen(d -> repaint()));
    }

    private Listener l = new Listener();
    private final CellSink<Optional<Dimension>> size;
    private final Cell<Drawable> drawable;

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawable.sample().draw(g);
    }

    public void removeNotify() {
        l.unlisten();
        super.removeNotify();
    }
}

