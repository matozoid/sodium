import junction.BackEnd;
import junction.Value;
import junction.ValueOutput;
import nz.sodium.Cell;
import nz.sodium.Listener;
import nz.sodium.StreamSink;

import java.util.Optional;

public class SimpleExample {
    public static void main(String[] args) {
        BackEnd be = new BackEnd();
        Value<Integer> vAge = be.allocate("age", 0);
        StreamSink<Integer> sAge = new StreamSink<>();
        ValueOutput<Integer> out = vAge.construct(sAge);
        Cell<Optional<Integer>> age = out.value;
        Listener l = age.listen(oa -> {
            System.out.println("age = " + (
                    oa.isPresent() ? Integer.toString(oa.get())
                            : "<empty>"));
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        System.out.println("SEND 5");
        sAge.send(5);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        l.unlisten();
    }
}

