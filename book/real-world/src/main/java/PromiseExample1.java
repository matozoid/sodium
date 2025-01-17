import nz.sodium.StreamSink;
import promise.Promise;

import java.util.ArrayList;

public class PromiseExample1 {
    public static void main(String[] args) {
        System.out.println("*** test 1");
        {
            StreamSink<String> s1 = new StreamSink<>();
            Promise<String> p1 = new Promise<>(s1);
            s1.send("Early");
            p1.thenDo(System.out::println);
        }
        System.out.println("*** test 2");
        {
            StreamSink<String> s1 = new StreamSink<>();
            Promise<String> p1 = new Promise<>(s1);
            p1.thenDo(System.out::println);
            s1.send("Late");
        }
    }
}
