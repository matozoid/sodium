import nz.sodium.*;

import java.util.Optional;

public class PauseExample {
    public static Cell<Double> pausableClock(Stream<Unit> sPause,
                                             Stream<Unit> sResume, Cell<Double> clock) {
        Cell<Optional<Double>> pauseTime =
                sPause.snapshot(clock, (u, t) -> Optional.of(t))
                        .orElse(sResume.map(u -> Optional.empty()))
                        .hold(Optional.empty());
        Cell<Double> lostTime = sResume.accum(
                0.0,
                (u, total) -> {
                    double tPause = pauseTime.sample().get();
                    double now = clock.sample();
                    return total + (now - tPause);
                });
        return pauseTime.lift(clock, lostTime,
                (otPause, tClk, tLost) ->
                        (otPause.orElse(tClk))
                                - tLost);
    }

    public static void main(String[] args) {
        CellSink<Double> mainClock = new CellSink<>(0.0);
        StreamSink<Unit> sPause = new StreamSink<>();
        StreamSink<Unit> sResume = new StreamSink<>();
        Cell<Double> gameClock = pausableClock(sPause, sResume, mainClock);
        Listener l = mainClock.lift(gameClock,
                        (m, g) -> "main=" + m + " game=" + g)
                .listen(System.out::println);
        mainClock.send(1.0);
        mainClock.send(2.0);
        mainClock.send(3.0);
        sPause.send(Unit.UNIT);
        mainClock.send(4.0);
        mainClock.send(5.0);
        mainClock.send(6.0);
        sResume.send(Unit.UNIT);
        mainClock.send(7.0);
        l.unlisten();
    }
}

