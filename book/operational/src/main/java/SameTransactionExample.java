import nz.sodium.Listener;
import nz.sodium.Stream;
import nz.sodium.StreamSink;
import nz.sodium.Transaction;

public class SameTransactionExample {
    public static void main(String[] args) {
        StreamSink<Integer> sX = new StreamSink<>();
        Stream<Integer> sXPlus1 = sX.map(x -> x + 1);
        Listener l = Transaction.run(() -> {
            sX.send(1);
            return sXPlus1.listen(System.out::println);
        });
        sX.send(2);
        sX.send(3);
        l.unlisten();
    }
}
