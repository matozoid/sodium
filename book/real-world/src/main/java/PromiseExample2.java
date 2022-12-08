import nz.sodium.StreamSink;
import nz.sodium.Transaction;
import promise.Promise;

public class PromiseExample2 {
    public static void main(String[] args) {
        System.out.println("*** Simple test");
        {
            StreamSink<String> sa = new StreamSink<>();
            Promise<String> pa = new Promise<>(sa);
            StreamSink<String> sb = new StreamSink<>();
            Promise<String> pb = new Promise<>(sb);
            Promise<String> p = pa.lift(pb, (a, b) -> a + " " + b);
            sa.send("Hello");
            p.thenDo(System.out::println);
            sb.send("World");
        }
        System.out.println("*** Simultaneous case");
        {
            StreamSink<String> sa = new StreamSink<>();
            Promise<String> pa = new Promise<>(sa);
            StreamSink<String> sb = new StreamSink<>();
            Promise<String> pb = new Promise<>(sb);
            Promise<String> p = pa.lift(pb, (a, b) -> a + " " + b);
            p.thenDo(System.out::println);
            Transaction.runVoid(() -> {
                sa.send("Hello");
                sb.send("World");
            });
        }
    }
}
