package nz.sodium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStream {
    @AfterEach
    protected void tearDown() throws Exception {
        System.gc();
        Thread.sleep(100);
    }

    @Test
    public void testSendStream() {
        StreamSink<Integer> e = new StreamSink<>();
        List<Integer> out = new ArrayList<>();
        Listener l = e.listen(out::add);
        e.send(5);
        l.unlisten();
        assertEquals(List.of(5), out);
        e.send(6);
        assertEquals(List.of(5), out);
    }

    @Test
    public void testMap() {
        StreamSink<Integer> e = new StreamSink<>();
        Stream<String> m = e.map(x -> Integer.toString(x));
        List<String> out = new ArrayList<>();
        Listener l = m.listen(out::add);
        e.send(5);
        l.unlisten();
        assertEquals(List.of("5"), out);
    }

    @Test
    public void testMapTo() {
        StreamSink<Integer> e = new StreamSink<>();
        Stream<String> m = e.mapTo("fusebox");
        List<String> out = new ArrayList<>();
        Listener l = m.listen(out::add);
        e.send(5);
        e.send(6);
        l.unlisten();
        assertEquals(Arrays.asList("fusebox", "fusebox"), out);
    }

    @Test
    public void testMergeNonSimultaneous() {
        StreamSink<Integer> e1 = new StreamSink<>();
        StreamSink<Integer> e2 = new StreamSink<>();
        List<Integer> out = new ArrayList<>();
        Listener l = e2.orElse(e1).listen(out::add);
        e1.send(7);
        e2.send(9);
        e1.send(8);
        l.unlisten();
        assertEquals(Arrays.asList(7, 9, 8), out);
    }

    @Test
    public void testMergeSimultaneous() {
        StreamSink<Integer> s1 = new StreamSink<>((l, r) -> r);
        StreamSink<Integer> s2 = new StreamSink<>((l, r) -> r);
        List<Integer> out = new ArrayList<>();
        Listener l = s2.orElse(s1).listen(out::add);
        Transaction.runVoid(() -> {
            s1.send(7);
            s2.send(60);
        });
        Transaction.runVoid(() -> {
            s1.send(9);
        });
        Transaction.runVoid(() -> {
            s1.send(7);
            s1.send(60);
            s2.send(8);
            s2.send(90);
        });
        Transaction.runVoid(() -> {
            s2.send(8);
            s2.send(90);
            s1.send(7);
            s1.send(60);
        });
        Transaction.runVoid(() -> {
            s2.send(8);
            s1.send(7);
            s2.send(90);
            s1.send(60);
        });
        l.unlisten();
        assertEquals(Arrays.asList(60, 9, 90, 90, 90), out);
    }

    @Test
    public void testCoalesce() {
        StreamSink<Integer> s = new StreamSink<>(Integer::sum);
        List<Integer> out = new ArrayList<>();
        Listener l = s
                .listen(out::add);
        Transaction.runVoid(() -> {
            s.send(2);
        });
        Transaction.runVoid(() -> {
            s.send(8);
            s.send(40);
        });
        l.unlisten();
        assertEquals(Arrays.asList(2, 48), out);
    }

    @Test
    public void testFilter() {
        StreamSink<Character> e = new StreamSink<>();
        List<Character> out = new ArrayList<>();
        Listener l = e.filter(Character::isUpperCase).listen(out::add);
        e.send('H');
        e.send('o');
        e.send('I');
        l.unlisten();
        assertEquals(Arrays.asList('H', 'I'), out);
    }

    @Test
    public void testFilterOptional() {
        StreamSink<Optional<String>> e = new StreamSink<>();
        List<String> out = new ArrayList<>();
        Listener l = Stream.filterOptional(e).listen(out::add);
        e.send(Optional.of("tomato"));
        e.send(Optional.empty());
        e.send(Optional.of("peach"));
        l.unlisten();
        assertEquals(Arrays.asList("tomato", "peach"), out);
    }

    @Test
    public void testLoopStream() {
        final StreamSink<Integer> ea = new StreamSink<>();
        Stream<Integer> ec = Transaction.run(() -> {
            StreamLoop<Integer> eb = new StreamLoop<>();
            Stream<Integer> ec_ = ea.map(x -> x % 10).merge(eb, Integer::sum);
            Stream<Integer> eb_out = ea.map(x -> x / 10).filter(x -> x != 0);
            eb.loop(eb_out);
            return ec_;
        });
        List<Integer> out = new ArrayList<>();
        Listener l = ec.listen(out::add);
        ea.send(2);
        ea.send(52);
        l.unlisten();
        assertEquals(Arrays.asList(2, 7), out);
    }

    @Test
    public void testGate() {
        StreamSink<Character> ec = new StreamSink<>();
        CellSink<Boolean> epred = new CellSink<>(true);
        List<Character> out = new ArrayList<>();
        Listener l = ec.gate(epred).listen(out::add);
        ec.send('H');
        epred.send(false);
        ec.send('O');
        epred.send(true);
        ec.send('I');
        l.unlisten();
        assertEquals(Arrays.asList('H', 'I'), out);
    }

    @Test
    public void testCollect() {
        StreamSink<Integer> ea = new StreamSink<>();
        List<Integer> out = new ArrayList<>();
        Stream<Integer> sum = ea.collect(0,
                (a, s) -> new Tuple2<>(a + s + 100, a + s)
        );
        Listener l = sum.listen(out::add);
        ea.send(5);
        ea.send(7);
        ea.send(1);
        ea.send(2);
        ea.send(3);
        l.unlisten();
        assertEquals(Arrays.asList(105, 112, 113, 115, 118), out);
    }

    @Test
    public void testAccum() {
        StreamSink<Integer> ea = new StreamSink<>();
        List<Integer> out = new ArrayList<>();
        Cell<Integer> sum = ea.accum(100, Integer::sum);
        Listener l = sum.listen(out::add);
        ea.send(5);
        ea.send(7);
        ea.send(1);
        ea.send(2);
        ea.send(3);
        l.unlisten();
        assertEquals(Arrays.asList(100, 105, 112, 113, 115, 118), out);
    }

    @Test
    public void testOnce() {
        StreamSink<Character> e = new StreamSink<>();
        List<Character> out = new ArrayList<>();
        Listener l = e.once().listen(out::add);
        e.send('A');
        e.send('B');
        e.send('C');
        l.unlisten();
        assertEquals(List.of('A'), out);
    }

    @Test
    public void testDefer() {
        StreamSink<Character> e = new StreamSink<>();
        Cell<Character> b = e.hold(' ');
        List<Character> out = new ArrayList<>();
        Listener l = Operational.defer(e).snapshot(b).listen(out::add);
        e.send('C');
        e.send('B');
        e.send('A');
        l.unlisten();
        assertEquals(Arrays.asList('C', 'B', 'A'), out);
    }
}

