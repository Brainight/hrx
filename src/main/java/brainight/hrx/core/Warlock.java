package brainight.hrx.core;

import brainight.hrx.core.exceptions.HorrocruxException;
import java.io.IOException;

/**
 *
 * @author Brainight
 */
public interface Warlock<T, I> {

    T vanish(Horrocrux hrx, byte[] key) throws HorrocruxException, IOException;

    Horrocrux summon(I id, T src, byte[] key) throws HorrocruxException, IOException;

    MirroredHorrocrux register(Horrocrux hrx, I id, byte[] key) throws HorrocruxException;

    MirroredHorrocrux mirror(Horrocrux hrx) throws HorrocruxException;

    I getHrxId(Horrocrux hrx);

    I removeHrxId(Horrocrux hrx);

    boolean isKnown(I id);

    public class WarlockKey<I> {

        private final I id;
        private final byte[] key;

        WarlockKey(I location, byte[] key) {
            this.id = location;
            this.key = key;
        }

        I getId() {
            return id;
        }

        byte[] getKey() {
            return key;
        }
    }

}
