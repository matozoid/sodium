import io.vavr.control.Option;
import nz.sodium.Cell;
import nz.sodium.CellLoop;
import nz.sodium.Stream;
import nz.sodium.Unit;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HomoZombicus {
    public HomoZombicus(
            int self,
            Point posInit,
            Cell<Double> time,
            Stream<Unit> sTick,
            Cell<List<Character>> scene) {
        final double speed = 20.0;
        class State {
            State(double t0, Point orig, int self,
                  List<Character> scene) {
                this.t0 = t0;
                this.orig = orig;
                Option<Character> oOther = nearest(self, scene);
                if (oOther.isDefined()) {
                    Character other = oOther.get();
                    this.velocity = Vector.subtract(other.pos, orig)
                            .normalize().mult(
                                    other.type == CharacterType.SAPIENS
                                            ? speed : -speed
                            );
                } else
                    this.velocity = new Vector(0, 0);
            }

            Option<Character> nearest(int self, List<Character> scene) {
                double bestDist = 0.0;
                Option<Character> best = Option.none();
                for (Character ch : scene)
                    if (ch.id != self) {
                        double dist = Vector.distance(ch.pos, orig);
                        if (ch.type == CharacterType.ZOMBICUS && dist > 60)
                            ;
                        else if (!best.isDefined() || dist < bestDist) {
                            bestDist = dist;
                            best = Option.some(ch);
                        }
                    }
                return best;
            }

            Option<Character> nearestSapiens(int self,
                                               List<Character> scene) {
                List<Character> sapiens = new ArrayList<>();
                for (Character ch : scene) {
                    if (ch.type == CharacterType.SAPIENS)
                        sapiens.add(ch);
                }
                return nearest(self, sapiens);
            }

            final double t0;
            final Point orig;
            final Vector velocity;

            Point positionAt(double t) {
                return velocity.mult(t - t0).add(orig);
            }
        }

        CellLoop<State> state = new CellLoop<>();
        Stream<State> sChange = Stream.filterOptional(
                sTick.snapshot(state,
                        (u, st) -> {
                            double t = time.sample();
                            return t - st.t0 >= 0.2
                                    ? Option.some(new State(t, st.positionAt(t),
                                    self, scene.sample()))
                                    : Option.none();
                        }
                ));
        List<Character> emptyScene = new ArrayList<>(0);
        state.loop(sChange.hold(
                new State(time.sample(), posInit, self, emptyScene)
        ));
        character = state.lift(time, (st, t) ->
                new Character(self, CharacterType.ZOMBICUS,
                        st.positionAt(time.sample()), st.velocity));
        sBite = Stream.filterOptional(
                sTick.snapshot(state,
                        (u, st) -> {
                            Option<Character> oVictim = st.nearestSapiens(
                                    self, scene.sample());
                            if (oVictim.isDefined()) {
                                Character victim = oVictim.get();
                                Point myPos = st.positionAt(time.sample());
                                if (Vector.distance(victim.pos, myPos) < 10)
                                    return Option.some(victim.id);
                            }
                            return Option.none();
                        }
                ));
    }

    public final Cell<Character> character;
    public final Stream<Integer> sBite;
}

