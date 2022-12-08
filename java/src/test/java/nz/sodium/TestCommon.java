// DO NOT EDIT THIS FILE
// It was generated by "common tests" - please modify it in the common source and it
// will be re-generated for all languages.

package nz.sodium;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCommon {

    @Test
    public void test_Base_send1() {
        StreamSink<String> s = Transaction.run(() -> {
            StreamSink<String> s_ = new StreamSink<>();
            return s_;
        });
        List<String> out = new ArrayList<>();
        Listener l = Transaction.run(() -> {
            Listener l_ = s.listen(out::add);
            return l_;
        });
        Transaction.runVoid(() -> {
            s.send("a");
        });
        Transaction.runVoid(() -> {
            s.send("b");
        });
        l.unlisten();
        assertEquals(Arrays.asList("a", "b"), out);
    }

    @Test
    public void test_Operational_split() {
        StreamSink<List<String>> a = Transaction.run(() -> {
            StreamSink<List<String>> a_ = new StreamSink<>();
            return a_;
        });
        Stream<String> b = Transaction.run(() -> {
            Stream<String> b_ = Operational.split(a);
            return b_;
        });
        List<String> b_0 = new ArrayList<>();
        Listener b_0_l = Transaction.run(() -> {
            Listener b_0_l_ = b.listen(b_0::add);
            return b_0_l_;
        });
        Transaction.runVoid(() -> {
            a.send(Arrays.asList("a", "b"));
        });
        b_0_l.unlisten();
        assertEquals(Arrays.asList("a", "b"), b_0);
    }

    @Test
    public void test_Operational_defer1() {
        StreamSink<String> a = Transaction.run(() -> {
            StreamSink<String> a_ = new StreamSink<>();
            return a_;
        });
        Stream<String> b = Transaction.run(() -> {
            Stream<String> b_ = Operational.defer(a);
            return b_;
        });
        List<String> b_0 = new ArrayList<>();
        Listener b_0_l = Transaction.run(() -> {
            Listener b_0_l_ = b.listen(b_0::add);
            return b_0_l_;
        });
        Transaction.runVoid(() -> {
            a.send("a");
        });
        b_0_l.unlisten();
        assertEquals(List.of("a"), b_0);
        List<String> b_1 = new ArrayList<>();
        Listener b_1_l = Transaction.run(() -> {
            Listener b_1_l_ = b.listen(b_1::add);
            return b_1_l_;
        });
        Transaction.runVoid(() -> {
            a.send("b");
        });
        b_1_l.unlisten();
        assertEquals(List.of("b"), b_1);
    }

    @Test
    public void test_Operational_defer2() {
        StreamSink<String> a = Transaction.run(() -> {
            StreamSink<String> a_ = new StreamSink<>();
            return a_;
        });
        StreamSink<String> b = Transaction.run(() -> {
            StreamSink<String> b_ = new StreamSink<>();
            return b_;
        });
        Stream<String> c = Transaction.run(() -> {
            Stream<String> c_ = Operational.defer(a).orElse(b);
            return c_;
        });
        List<String> c_0 = new ArrayList<>();
        Listener c_0_l = Transaction.run(() -> {
            Listener c_0_l_ = c.listen(c_0::add);
            return c_0_l_;
        });
        Transaction.runVoid(() -> {
            a.send("a");
        });
        c_0_l.unlisten();
        assertEquals(List.of("a"), c_0);
        List<String> c_1 = new ArrayList<>();
        Listener c_1_l = Transaction.run(() -> {
            Listener c_1_l_ = c.listen(c_1::add);
            return c_1_l_;
        });
        Transaction.runVoid(() -> {
            a.send("b");
            b.send("B");
        });
        c_1_l.unlisten();
        assertEquals(Arrays.asList("B", "b"), c_1);
    }

    @Test
    public void test_Stream_orElse1() {
        StreamSink<Integer> a = Transaction.run(() -> {
            StreamSink<Integer> a_ = new StreamSink<>();
            return a_;
        });
        StreamSink<Integer> b = Transaction.run(() -> {
            StreamSink<Integer> b_ = new StreamSink<>();
            return b_;
        });
        Stream<Integer> c = Transaction.run(() -> {
            Stream<Integer> c_ = a.orElse(b);
            return c_;
        });
        List<Integer> c_0 = new ArrayList<>();
        Listener c_0_l = Transaction.run(() -> {
            Listener c_0_l_ = c.listen(c_0::add);
            return c_0_l_;
        });
        Transaction.runVoid(() -> {
            a.send(0);
        });
        c_0_l.unlisten();
        assertEquals(List.of(0), c_0);
        List<Integer> c_1 = new ArrayList<>();
        Listener c_1_l = Transaction.run(() -> {
            Listener c_1_l_ = c.listen(c_1::add);
            return c_1_l_;
        });
        Transaction.runVoid(() -> {
            b.send(10);
        });
        c_1_l.unlisten();
        assertEquals(List.of(10), c_1);
        List<Integer> c_2 = new ArrayList<>();
        Listener c_2_l = Transaction.run(() -> {
            Listener c_2_l_ = c.listen(c_2::add);
            return c_2_l_;
        });
        Transaction.runVoid(() -> {
            a.send(2);
            b.send(20);
        });
        c_2_l.unlisten();
        assertEquals(List.of(2), c_2);
        List<Integer> c_3 = new ArrayList<>();
        Listener c_3_l = Transaction.run(() -> {
            Listener c_3_l_ = c.listen(c_3::add);
            return c_3_l_;
        });
        Transaction.runVoid(() -> {
            b.send(30);
        });
        c_3_l.unlisten();
        assertEquals(List.of(30), c_3);
    }

    @Test
    public void test_Operational_deferSimultaneous() {
        StreamSink<String> a = Transaction.run(() -> {
            StreamSink<String> a_ = new StreamSink<>();
            return a_;
        });
        StreamSink<String> b = Transaction.run(() -> {
            StreamSink<String> b_ = new StreamSink<>();
            return b_;
        });
        Stream<String> c = Transaction.run(() -> {
            Stream<String> c_ = Operational.defer(a).orElse(Operational.defer(b));
            return c_;
        });
        List<String> c_0 = new ArrayList<>();
        Listener c_0_l = Transaction.run(() -> {
            Listener c_0_l_ = c.listen(c_0::add);
            return c_0_l_;
        });
        Transaction.runVoid(() -> {
            b.send("A");
        });
        c_0_l.unlisten();
        assertEquals(List.of("A"), c_0);
        List<String> c_1 = new ArrayList<>();
        Listener c_1_l = Transaction.run(() -> {
            Listener c_1_l_ = c.listen(c_1::add);
            return c_1_l_;
        });
        Transaction.runVoid(() -> {
            a.send("b");
            b.send("B");
        });
        c_1_l.unlisten();
        assertEquals(List.of("b"), c_1);
    }
}
