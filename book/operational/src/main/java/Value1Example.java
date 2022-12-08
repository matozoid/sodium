import nz.sodium.CellSink;
import nz.sodium.Listener;
import nz.sodium.Operational;

public class Value1Example {
    public static void main(String[] args) {
        CellSink<Integer> x = new CellSink<>(0);
        x.send(1);
        Listener l = Operational.value(x).listen(System.out::println);
        x.send(2);
        x.send(3);
        l.unlisten();
    }
}
