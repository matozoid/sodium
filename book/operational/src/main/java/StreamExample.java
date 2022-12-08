import nz.sodium.Listener;
import nz.sodium.Stream;
import nz.sodium.StreamSink;

public class StreamExample {
    public static void main(String[] args) {
        StreamSink<Integer> sX = new StreamSink<>();
        Stream<Integer> sXPlus1 = sX.map(x -> x + 1);
        Listener l = sXPlus1.listen(System.out::println);
        sX.send(1);
        sX.send(2);
        sX.send(3);
        l.unlisten();
    }
}
