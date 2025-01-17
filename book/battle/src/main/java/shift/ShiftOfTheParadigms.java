package shift;

import nz.sodium.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;

class Element {
    Element(Polygon polygon) {
        this.polygon = polygon;
    }

    private final Polygon polygon;

    public boolean contains(Point pt) {
        return polygon.contains(pt);
    }

    public Element translate(Point orig, Point pt, boolean axisLock) {
        int tx = pt.x - orig.x;
        int ty = pt.y - orig.y;
        if (axisLock) {
            if (Math.abs(tx) < Math.abs(ty))
                tx = 0;
            else
                ty = 0;
        }
        Polygon neu = new Polygon(polygon.xpoints, polygon.ypoints,
                polygon.npoints);
        neu.translate(tx, ty);
        return new Element(neu);
    }

    private static final Color darkGreen = new Color(64, 128, 0);

    public void draw(Graphics g) {
        g.setColor(darkGreen);
        g.fillPolygon(polygon);
        g.setColor(Color.black);
        g.drawPolygon(polygon);
    }
}

class Entry {
    Entry(String id, Element element) {
        this.id = id;
        this.element = element;
    }

    public final String id;
    public final Element element;
}

class Document {
    public Document(Map<String, Element> elements) {
        this.elements = elements;
    }

    private final Map<String, Element> elements;

    public Optional<Entry> getByPoint(Point pt) {
        Optional<Entry> oe = Optional.empty();
        for (Map.Entry<String, Element> e : elements.entrySet()) {
            if (e.getValue().contains(pt))
                oe = Optional.of(new Entry(e.getKey(), e.getValue()));
        }
        return oe;
    }

    public Document insert(String id, Element polygon) {
        HashMap<String, Element> neu = new HashMap<>(elements);
        neu.put(id, polygon);
        return new Document(neu);
    }

    public void draw(Graphics g) {
        for (Element p : elements.values())
            p.draw(g);
    }
}

enum Type {DOWN, MOVE, UP}

class MouseEvt {
    MouseEvt(Type type, Point pt) {
        this.type = type;
        this.pt = pt;
    }

    public final Type type;
    public final Point pt;
}

interface Paradigm {
    interface DocumentListener {
        void documentUpdated(Document doc);
    }

    interface Factory {
        Paradigm create(Document initDoc, DocumentListener dl);
    }

    void mouseEvent(MouseEvt me);

    void shiftEvent(Type t);

    void dispose();
}

class Classic implements Paradigm {
    public Classic(Document initDoc, DocumentListener dl) {
        this.doc = initDoc;
        this.dl = dl;
    }

    private final DocumentListener dl;

    private static class Dragging {
        Dragging(MouseEvt me1, Entry ent) {
            this.me1 = me1;
            this.ent = ent;
        }

        final MouseEvt me1;
        final Entry ent;
    }

    private Document doc;
    private Optional<Dragging> oDragging = Optional.empty();
    private boolean axisLock;

    public void mouseEvent(MouseEvt me) {
        switch (me.type) {
            case DOWN:
                Optional<Entry> oe = doc.getByPoint(me.pt);
                if (oe.isPresent()) {
                    System.out.println("classic dragging " + oe.get().id);
                    oDragging = Optional.of(new Dragging(me, oe.get()));
                }
                break;
            case MOVE:
                if (oDragging.isPresent()) {
                    Dragging dr = oDragging.get();
                    doc = doc.insert(dr.ent.id,
                            dr.ent.element.translate(dr.me1.pt, me.pt,
                                    axisLock));
                    dl.documentUpdated(doc);
                }
                break;
            case UP:
                oDragging = Optional.empty();
                break;
        }
    }

    public void shiftEvent(Type t) {
        axisLock = t == Type.DOWN;
    }

    public void dispose() {
    }
}

class FRP implements Paradigm {
    public FRP(Document initDoc, DocumentListener dl) {
        l = Transaction.run(() -> {
            CellLoop<Document> doc = new CellLoop<>();
            Cell<Boolean> axisLock = sShift.map(t -> t == Type.DOWN)
                    .hold(false);
            Stream<Stream<Document>> sStartDrag = Stream.filterOptional(
                    sMouse.snapshot(doc, (me1, doc1) -> {
                        if (me1.type == Type.DOWN) {
                            Optional<Entry> oe = doc1.getByPoint(me1.pt);
                            if (oe.isPresent()) {
                                String id = oe.get().id;
                                Element elt = oe.get().element;
                                System.out.println("FRP dragging " + id);
                                Stream<Document> sMoves = sMouse
                                        .filter(me -> me.type == Type.MOVE)
                                        .snapshot(doc, (me2, doc2) ->
                                                doc2.insert(id,
                                                        elt.translate(me1.pt, me2.pt,
                                                                axisLock.sample())));
                                return Optional.of(sMoves);
                            }
                        }
                        return Optional.empty();
                    }));
            Stream<Document> sIdle = new Stream<>();
            Stream<Stream<Document>> sEndDrag =
                    sMouse.filter(me -> me.type == Type.UP)
                            .map(me -> sIdle);
            Stream<Document> sDocUpdate = Cell.switchS(
                    sStartDrag.orElse(sEndDrag).hold(sIdle)
            );
            doc.loop(sDocUpdate.hold(initDoc));
            return sDocUpdate.listen(dl::documentUpdated);
        });
    }

    private final Listener l;
    private final StreamSink<MouseEvt> sMouse = new StreamSink<>();

    public void mouseEvent(MouseEvt me) {
        sMouse.send(me);
    }

    private final StreamSink<Type> sShift = new StreamSink<>();

    public void shiftEvent(Type t) {
        sShift.send(t);
    }

    public void dispose() {
        l.unlisten();
    }
}

class Actor implements Paradigm {
    public Actor(Document initDoc, DocumentListener dl) {
        ArrayBlockingQueue<Document> out = new ArrayBlockingQueue<>(1);
        t1 = new Thread(() -> {
            try {
                Document doc = initDoc;
                boolean axisLock = false;
                while (true) {
                    MouseEvt me1 = null;
                    Entry ent = null;
                    while (true) {
                        Object o = in.take();
                        if (o instanceof MouseEvt) {
                            MouseEvt me = (MouseEvt) o;
                            if (me.type == Type.DOWN) {
                                Optional<Entry> oe = doc.getByPoint(me.pt);
                                if (oe.isPresent()) {
                                    me1 = me;
                                    ent = oe.get();
                                    break;
                                }
                            }
                        }
                        if (o instanceof Type) {
                            Type t = (Type) o;
                            axisLock = t == Type.DOWN;
                        }
                    }
                    System.out.println("actor dragging " + ent.id);
                    while (true) {
                        Object o = in.take();
                        if (o instanceof MouseEvt) {
                            MouseEvt me = (MouseEvt) o;
                            if (me.type == Type.MOVE) {
                                doc = doc.insert(ent.id,
                                        ent.element.translate(me1.pt, me.pt,
                                                axisLock));
                                out.put(doc);
                            } else if (me.type == Type.UP)
                                break;
                        }
                        if (o instanceof Type) {
                            Type t = (Type) o;
                            axisLock = t == Type.DOWN;
                        }
                    }
                }
            } catch (InterruptedException e) {
            }
        });
        t1.start();
        t2 = new Thread(() -> {
            try {
                while (true)
                    dl.documentUpdated(out.take());
            } catch (InterruptedException e) {
            }
        });
        t2.start();
    }

    private final Thread t1, t2;
    private final ArrayBlockingQueue<Object> in =
            new ArrayBlockingQueue<>(1);

    public void mouseEvent(MouseEvt me) {
        try {
            in.put(me);
        } catch (InterruptedException e) {
        }
    }

    public void shiftEvent(Type t) {
        try {
            in.put(t);
        } catch (InterruptedException e) {
        }
    }

    public void dispose() {
        t1.interrupt();
        t2.interrupt();
    }
}

class ParadigmView extends JPanel implements Paradigm.DocumentListener {
    public ParadigmView(Document initDoc, JFrame frame,
                        Paradigm.Factory factory) {
        this.doc = initDoc;
        this.paradigm = factory.create(initDoc, this);
        setBorder(BorderFactory.createLineBorder(Color.black));
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent ev) {
                paradigm.mouseEvent(new MouseEvt(Type.DOWN,
                        new Point(ev.getX(), ev.getY())));
            }

            public void mouseReleased(MouseEvent ev) {
                paradigm.mouseEvent(new MouseEvt(Type.UP,
                        new Point(ev.getX(), ev.getY())));
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent ev) {
                paradigm.mouseEvent(new MouseEvt(Type.MOVE,
                        new Point(ev.getX(), ev.getY())));
            }

            public void mouseMoved(MouseEvent ev) {
                paradigm.mouseEvent(new MouseEvt(Type.MOVE,
                        new Point(ev.getX(), ev.getY())));
            }
        });
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT)
                    paradigm.shiftEvent(Type.DOWN);
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT)
                    paradigm.shiftEvent(Type.UP);
            }
        });
    }

    private Document doc;
    private final Paradigm paradigm;

    public Dimension getPreferredSize() {
        return new Dimension(250, 300);
    }

    public void documentUpdated(Document doc) {
        this.doc = doc;
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doc.draw(g);
    }

    public void removeNotify() {
        paradigm.dispose();
        super.removeNotify();
    }
}

public class ShiftOfTheParadigms {
    private static Element shape(int ox, int oy, int sides, double angle) {
        int[] xs = new int[sides];
        int[] ys = new int[sides];
        angle *= Math.PI / 180.0;
        for (int i = 0; i < sides; i++) {
            double theta = angle + Math.PI * 2 * (double) i / (double) sides;
            xs[i] = (int) ((double) ox + Math.sin(theta) * 25);
            ys[i] = (int) ((double) oy - Math.cos(theta) * 25);
        }
        return new Element(new Polygon(xs, ys, sides));
    }

    public static void main(String[] args) {
        HashMap<String, Element> elements = new HashMap<>();

        elements.put("triangle", shape(50, 50, 3, 0.0));
        elements.put("square", shape(125, 50, 4, 45.0));
        elements.put("pentagon", shape(200, 50, 5, 0.0));
        elements.put("hexagon", shape(50, 125, 6, 30.0));
        elements.put("heptagon", shape(125, 125, 7, 0.0));
        elements.put("octagon", shape(200, 125, 8, 22.5));

        Document doc = new Document(elements);
        JFrame frame = new JFrame("ShiftOfTheParadigms");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel view = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        view.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        view.add(new JLabel("Drag the polygons with your mouse and use shift to axis lock"), c);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        view.add(new ParadigmView(doc, frame,
                Classic::new), c);
        c.gridx = 0;
        c.gridy = 2;
        view.add(new JLabel("classic state machine"), c);
        c.gridx = 1;
        c.gridy = 1;
        view.add(new ParadigmView(doc, frame,
                FRP::new), c);
        c.gridx = 1;
        c.gridy = 2;
        view.add(new JLabel("FRP"), c);
        c.gridx = 2;
        c.gridy = 1;
        view.add(new ParadigmView(doc, frame,
                Actor::new), c);
        c.gridx = 2;
        c.gridy = 2;
        view.add(new JLabel("actor model"), c);
        frame.setContentPane(view);
        frame.pack();
        frame.setVisible(true);
    }
}

