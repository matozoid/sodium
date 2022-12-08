import nz.sodium.Listener;
import nz.sodium.StreamSink;

public class SendInCallbackExample {
    public static void main(String[] args) {
        StreamSink<Integer> sX = new StreamSink<>();
        StreamSink<Integer> sY = new StreamSink<>();
        // Should throw an exception because you're not allowed to use send() inside
        // a callback.
        Listener l = sX.listen(sY::send).append(
                sY.listen(System.out::println));
        sX.send(1);
        sX.send(2);
        sX.send(3);
        l.unlisten();
    }
}
