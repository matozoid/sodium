import nz.sodium.CellSink;
import nz.sodium.Listener;

public class CellExample {
    public static void main(String[] args) {
        CellSink<Integer> x = new CellSink<>(0);
        Listener l = x.listen(System.out::println);
        x.send(10);
        x.send(20);
        x.send(30);
        l.unlisten();
    }
}
