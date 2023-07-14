package brainight.hrx.core;

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
import java.util.LinkedList;
import java.util.List;

/**
 * Github: https://github.com/Brainight
 *
 * @author Brainight
 */
public class FsAzkabanImpl implements AzkabanImpl<Path, MirroredHorrocrux> {

    protected HorrocruxProvider<Path, ByteBuffer> provider;
    protected Warlock<ByteBuffer, Path> warlock;
    protected IdentityHashMap<MirroredHorrocrux, Horrocrux> horrocruxes;
    protected List<MirroredHorrocrux> views;

    public FsAzkabanImpl() {
        this.provider = new FsHorrocruxProvider();
        this.warlock = new HorrocruxWarlock();
        this.horrocruxes = new IdentityHashMap<>();
        this.views = new LinkedList<>();
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
            this.views.add(mhrx);
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
    public List<MirroredHorrocrux> getHorroruxes() {
        return List.copyOf(this.views);
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
        this.views.add(mhrx);
        return mhrx;
    }

    @Override
    public void delete(MirroredHorrocrux mhrx) throws HorrocruxException {
        Horrocrux hrx = this.horrocruxes.remove(mhrx);
        this.views.remove(mhrx);
        if (hrx == null) {
            throw new HorrocruxException("Delete error. Provided mirrored Horrocrux doesn't belong to an existing Horrocrux");
        }

        Path p = this.warlock.removeHrxId(hrx);

        this.provider.delete(p);
    }

    @Override
    public void close(MirroredHorrocrux mhrx) throws HorrocruxException {
        Horrocrux hrx = this.horrocruxes.remove(mhrx);
        this.views.remove(mhrx);
        
        if (hrx == null) {
            throw new HorrocruxException("Close error. Provided mirrored Horrocrux doesn't belong to an existing Horrocrux");
        }
        Path p = this.warlock.removeHrxId(hrx);
        this.provider.close(p);
    }

    @Override
    public Path getHorrocruxId(MirroredHorrocrux mhrx) {
        Horrocrux hrx = this.horrocruxes.get(mhrx);
        if(hrx == null){
            return null;
        }
        return this.warlock.getHrxId(hrx);
    }

}
