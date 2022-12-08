package nz.sodium.edgecases;

import nz.sodium.Stream;
import nz.sodium.StreamLoop;
import nz.sodium.StreamSink;
import nz.sodium.Transaction;

public class RepeatedExample {
    public static void main(String[] args) {
        StreamSink<Integer> sA = Transaction.run(() -> {
            StreamSink<Integer> sA_ = new StreamSink<>();
            StreamLoop<Integer> sB = new StreamLoop<>();
            Stream<Integer> sC = sA_.orElse(sB);
            sB.loop(sC.map(x -> x + 1).filter(x -> x < 10));
            sC.listen(System.out::println);
            return sA_;
        });
        System.out.println("send 5");
        sA.send(5);
    }
}
