import nz.sodium.Listener;
import nz.sodium.Operational;
import nz.sodium.StreamSink;

import java.util.Arrays;
import java.util.List;

public class SplitExample {
    public static void main(String[] args) {
        StreamSink<List<Integer>> as = new StreamSink<>();
        Listener l = Operational.updates(
                Operational.split(as)
                        .accum(0, Integer::sum)
        ).listen(System.out::println);
        as.send(Arrays.asList(100, 15, 60));
        as.send(Arrays.asList(1, 5));
        l.unlisten();
    }
}

