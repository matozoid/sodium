import nz.sodium.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class Animate extends JPanel {
    private final Dimension windowSize = new Dimension(700, 500);
    private final BufferedImage sapienImgL;
    private final BufferedImage sapienImgR;
    private final BufferedImage zombicusImgL;
    private final BufferedImage zombicusImgR;
    private final BufferedImage coneImg;
    private Cell<List<Character>> scene;
    private final List<Polygon> obstacles;
    private CellSink<Double> time;
    private StreamSink<Unit> sTick;

    public interface Animation {
        public Cell<List<Character>> create(
                Cell<Double> time, Stream<Unit> sTick,
                Dimension screenSize);
    }

    public Animate(Animation animation, List<Polygon> obstacles)
            throws MalformedURLException, IOException {
        sapienImgL = ImageIO.read(getClass().getResourceAsStream("/images/homo-sapien-left.png"));
        sapienImgR = ImageIO.read(getClass().getResourceAsStream("/images/homo-sapien-right.png"));
        zombicusImgL = ImageIO.read(getClass().getResourceAsStream("/images/homo-zombicus-left.png"));
        zombicusImgR = ImageIO.read(getClass().getResourceAsStream("/images/homo-zombicus-right.png"));
        coneImg = ImageIO.read(getClass().getResourceAsStream("/images/roadius-conium.png"));
        Transaction.runVoid(() -> {
            time = new CellSink<Double>(0.0);
            sTick = new StreamSink<Unit>();
            this.scene = animation.create(time, sTick, windowSize);
        });
        this.obstacles = obstacles;
        addMouseListener(new MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent ev) {
                System.out.println(ev.getX() + "," + ev.getY());
            }
        });
    }

    private final Color holeColor = new Color(0.9f, 0.8f, 0.95f);

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Transaction.runVoid(() -> {
            g.setColor(holeColor);
            for (Polygon o : obstacles) {
                g.fillPolygon(o);
                for (int i = 0; i < o.npoints; i++)
                    g.drawImage(coneImg, o.xpoints[i] - 14, o.ypoints[i] - 53, null);
            }
            List<Character> chars = new ArrayList<Character>(scene.sample());
            chars.sort((a, b) -> a.pos.y == b.pos.y ? 0 :
                    a.pos.y < b.pos.y ? -1 : 1);
            for (Character c : chars) {
                if (c.type == CharacterType.SAPIENS)
                    if (c.velocity.dx < 0)
                        g.drawImage(sapienImgL, c.pos.x - 30, c.pos.y - 73, null);
                    else
                        g.drawImage(sapienImgR, c.pos.x - 23, c.pos.y - 73, null);
                else if (c.velocity.dx < 0)
                    g.drawImage(zombicusImgL, c.pos.x - 39, c.pos.y - 73, null);
                else
                    g.drawImage(zombicusImgR, c.pos.x - 23, c.pos.y - 73, null);
            }
        });
        Toolkit.getDefaultToolkit().sync();
    }

    public Dimension getPreferredSize() {
        return windowSize;
    }

    public static void animate(String title, Animation animation) {
        animate(title, animation, new ArrayList<Polygon>());
    }

    public static void animate(String title, Animation animation, List<Polygon> obstacles) {
        try {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Animate view = new Animate(animation, obstacles);
            frame.setContentPane(view);
            frame.pack();
            frame.setVisible(true);
            long t0 = System.currentTimeMillis();
            long tLast = t0;
            while (true) {
                long t = System.currentTimeMillis();
                long tIdeal = tLast + 20;
                long toWait = tIdeal - t;
                if (toWait > 0)
                    try {
                        Thread.sleep(toWait);
                    } catch (InterruptedException e) {
                    }
                view.time.send((double) (tIdeal - t0) * 0.001);
                view.sTick.send(Unit.UNIT);
                view.repaint(0);
                tLast = tIdeal;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

