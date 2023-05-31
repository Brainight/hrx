package brainight.hrx.azkaban;

import brainight.hrx.core.Horrocrux;
import brainight.hrx.core.HorrocruxWarlock;
import brainight.hrx.core.MirroredHorrocrux;
import brainight.hrx.core.Warlock;
import brainight.hrx.core.exceptions.HorrocruxException;
import brainight.hrx.core.io.FsHorrocruxProvider;
import brainight.hrx.core.io.HorrocruxProvider;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Github: https://github.com/Brainight
 *
 * @author Brainight
 */
public class FsAzkabanImpl implements AzkabanImpl<Path, MirroredHorrocrux> {

    protected HorrocruxProvider<Path, ByteBuffer> provider;
    protected Warlock<ByteBuffer, Path> warlock;
    protected Map<MirroredHorrocrux, Horrocrux> horrocruxes;

    public FsAzkabanImpl() {
        this.provider = new FsHorrocruxProvider();
        this.warlock = new HorrocruxWarlock();
        this.horrocruxes = new IdentityHashMap<>();
    }

    @Override
    public MirroredHorrocrux open(Path id, byte[] key) throws HorrocruxException {
        if (this.warlock.isKnown(id)) {
            throw new HorrocruxException("Horrocrux '" + id + "' has already been opened");
        }

        ByteBuffer bb = this.provider.load(id);
        MirroredHorrocrux mhrx;
        Horrocrux hrx;
        try {
            hrx = this.warlock.summon(id, bb, key);
            mhrx = this.warlock.mirror(hrx);
            this.horrocruxes.put(mhrx, hrx);
            return mhrx;
        } catch (IOException ex) {
            throw new HorrocruxException(ex);
        }

    }

    @Override
    public void save(MirroredHorrocrux mhrx, byte[] key) throws HorrocruxException {
        Horrocrux hrx = this.horrocruxes.get(mhrx);
        if (hrx == null) {
            throw new HorrocruxException("Save error. Provided mirrored Horrocrux doesn't belong to an existing Horrocrux");
        }

        Path p = this.warlock.getHrxId(hrx);
        try {
            ByteBuffer bb = this.warlock.vanish(hrx, key);
            this.provider.save(bb, p);
        } catch (IOException ex) {
            throw new HorrocruxException(ex);
        }
    }

    @Override
    public void save(MirroredHorrocrux mhrx) throws HorrocruxException {
        this.save(mhrx, null);
    }

    @Override
    public Set<MirroredHorrocrux> getHorroruxes() {
        return this.horrocruxes.keySet();
    }

    @Override
    public MirroredHorrocrux create(Path id, String name, byte[] key) throws HorrocruxException {
        if (this.warlock.isKnown(id)) {
            throw new HorrocruxException("Horrocrux '" + id + "' has already been opened");
        }

        Horrocrux hrx = new Horrocrux();
        hrx.setName(name);
        this.provider.create(id);
        MirroredHorrocrux mhrx = this.warlock.register(hrx, id, key);
        this.horrocruxes.put(mhrx, hrx);
        return mhrx;
    }

    @Override
    public void delete(MirroredHorrocrux mhrx) throws HorrocruxException {
        Horrocrux hrx = this.horrocruxes.remove(mhrx);
        if (hrx == null) {
            throw new HorrocruxException("Delete error. Provided mirrored Horrocrux doesn't belong to an existing Horrocrux");
        }

        Path p = this.warlock.removeHrxId(hrx);
        this.provider.delete(p);
    }

    @Override
    public void close(MirroredHorrocrux mhrx) throws HorrocruxException {
        Horrocrux hrx = this.horrocruxes.remove(mhrx);
        if (hrx == null) {
            throw new HorrocruxException("Close error. Provided mirrored Horrocrux doesn't belong to an existing Horrocrux");
        }
        Path p = this.warlock.removeHrxId(hrx);
        this.provider.close(p);
    }

}
