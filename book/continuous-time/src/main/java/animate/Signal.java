package animate;

import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.Operational;
import nz.sodium.Stream;

public class Signal {
    public Signal(double t0, double a, double b, double c) {
        this.t0 = t0;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public final double t0, a, b, c;

    public double valueAt(double t) {
        double x = t - t0;
        return a * x * x + b * x + c;
    }

    public final static double quantum = 0.000001;

    public Option<Double> when(double x) {
        double c = this.c - x;
        if (a == 0) {
            double t = (-c) / b;
            return t >= quantum ? Option.some(t + t0)
                    : Option.none();
        } else {
            double b24ac = Math.sqrt(b * b - 4 * a * c);
            double t1 = ((-b) + b24ac) / (2 * a);
            double t2 = ((-b) - b24ac) / (2 * a);
            return t1 >= quantum
                    ? t2 >= quantum ? Option.some((t1 < t2 ? t1 : t2) + t0)
                    : Option.some(t1 + t0)
                    : t2 >= quantum ? Option.some(t2 + t0)
                    : Option.none();
        }
    }

    public Signal integrate(double initial) {
        if (a != 0.0) throw new InternalError("Signal can't handle x^3");
        return new Signal(t0, b / 2, c, initial);
    }

    public static Cell<Signal> integrate(
            Cell<Signal> sig, double initial) {
        Stream<Signal> sSig = Operational.updates(sig);
        return sSig.accum(sig.sample().integrate(initial),
                (neu, old) -> neu.integrate(old.valueAt(neu.t0)));
    }
}

