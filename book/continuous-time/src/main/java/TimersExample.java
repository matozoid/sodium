import io.vavr.control.Option;
import nz.sodium.*;
import nz.sodium.time.MillisecondsTimerSystem;
import nz.sodium.time.TimerSystem;

import java.util.Optional;

public class TimersExample {
    static Stream<Long> periodic(TimerSystem<Long> sys, long period) {
        Cell<Long> time = sys.time;
        CellLoop<Option<Long>> oAlarm = new CellLoop<>();
        Stream<Long> sAlarm = sys.at(oAlarm);
        oAlarm.loop(
                sAlarm.map(t -> Option.some(t + period))
                        .hold(Option.of(time.sample() + period)));
        return sAlarm;
    }

    public static void main(String[] args) {
        TimerSystem<Long> sys = new MillisecondsTimerSystem();
        Cell<Long> time = sys.time;
        StreamSink<Unit> sMain = new StreamSink<>();
        Listener l = Transaction.run(() -> {
            long t0 = time.sample();
            Listener l1 = periodic(sys, 1000).listen(t -> System.out.println((t - t0) + " timer"));
            Listener l2 = sMain.snapshot(time).listen(t -> System.out.println((t - t0) + " main"));
            return l1.append(l2);
        });
        for (int i = 0; i < 5; i++) {
            sMain.send(Unit.UNIT);
            try {
                Thread.sleep(990);
            } catch (InterruptedException e) {
            }
        }
        l.unlisten();
    }
}

