package nz.sodium.time;

import io.vavr.control.Option;
import nz.sodium.*;

import java.util.LinkedList;
import java.util.Optional;

public class TimerSystem<T extends Comparable<T>> {
    public TimerSystem(final TimerSystemImpl<T> impl) {
        this.impl = impl;
        final CellSink<T> timeSnk = new CellSink<>(impl.now());
        time = timeSnk;
        Transaction.onStart(() -> {
            T t = impl.now();
            impl.runTimersTo(t);
            while (true) {
                Event ev;
                // Pop all events earlier than t.
                synchronized (eventQueue) {
                    ev = eventQueue.peekFirst();
                    if (ev != null && ev.t.compareTo(t) <= 0)
                        eventQueue.removeFirst();
                    else
                        ev = null;
                }
                if (ev != null) {
                    timeSnk.send(ev.t);
                    ev.sAlarm.send(ev.t);
                } else
                    break;
            }
            timeSnk.send(t);
        });
    }

    private final TimerSystemImpl<T> impl;
    /**
     * A cell giving the current clock time.
     */
    public final Cell<T> time;

    private class Event {
        Event(T t, StreamSink<T> sAlarm) {
            this.t = t;
            this.sAlarm = sAlarm;
        }

        T t;
        StreamSink<T> sAlarm;
    }

    private final LinkedList<Event> eventQueue = new LinkedList<>();

    private static class CurrentTimer {
        Option<Timer> oTimer = Option.none();
    }

    /**
     * A timer that fires at the specified time.
     */
    public Stream<T> at(Cell<Option<T>> tAlarm) {
        final StreamSink<T> sAlarm = new StreamSink<>();
        final CurrentTimer current = new CurrentTimer();
        Listener l = tAlarm.listen(oAlarm -> {
            current.oTimer.forEach(Timer::cancel);
            current.oTimer = oAlarm.map(t -> impl.setTimer(t, () -> {
                synchronized (eventQueue) {
                    eventQueue.add(new Event(t, sAlarm));
                }
                // Open and close a transaction to trigger queued
                // events to run.
                Transaction.runVoid(() -> {
                });
            }));
        });
        return sAlarm.addCleanup(l);
    }
}

