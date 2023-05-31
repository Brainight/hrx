package brainight.hrx.core.io;

import brainight.hrx.core.exceptions.HorrocruxException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Github: https://github.com/Brainight
 *
 * @author Brainight
 */
public class FsHorrocruxProvider implements HorrocruxProvider<Path, ByteBuffer> {

    public static final LinkOption HRX_IO_LINKOPTION = LinkOption.NOFOLLOW_LINKS;
    public static final int HRX_IO_MAX_SIZE = Integer.MAX_VALUE;
    public final static Set<OpenOption> HRX_IO_OPEN_RWS = Set.of(
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.SYNC
    );
    public final static Set<OpenOption> HRX_IO_OPEN_CRWS = Set.of(
            StandardOpenOption.CREATE,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.SYNC
    );

    public final static Set<OpenOption> HRX_IO_OPEN_TCRWS = Set.of(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.SYNC
    );

    private Map<Path, FileLock> fshrx;

    public FsHorrocruxProvider() {
        fshrx = new HashMap<>();
    }

    @Override
    public void create(Path o) throws HorrocruxException {
        if (fshrx.containsKey(o)) {
            throw new HorrocruxException("Specified path '" + o + "' has already been loaded");
        }
        try {
            o.toFile().getParentFile().mkdirs();
            FileChannel fc = FileChannel.open(o, HRX_IO_OPEN_CRWS);
            FileLock lock = fc.lock();
            this.fshrx.put(o, lock);
        } catch (IOException ex) {
            throw new HorrocruxException(ex);
        }
    }

    /**
     * Loads the content of the provided path.
     *
     * @param o
     * @return ByteBuffer ready to read.
     * @throws HorrocruxException
     */
    @Override
    public ByteBuffer load(Path o) throws HorrocruxException {
        if (fshrx.containsKey(o)) {
            throw new HorrocruxException("Specified path '" + o + "' has already been loaded");
        }

        if (!Files.exists(o, HRX_IO_LINKOPTION)) {
            throw new HorrocruxException("Specified path '" + o + "' does not exist");
        }
        ByteBuffer bb = null;
        try {
            FileChannel fc = FileChannel.open(o, HRX_IO_OPEN_RWS);
            FileLock lock = fc.lock();
            if (fc.size() >= HRX_IO_MAX_SIZE) {
                throw new HorrocruxException("File is too big to be a horrocrux");
            }
            bb = ByteBuffer.allocate((int) fc.size());
            fc.read(bb);
            bb.flip();
            fshrx.put(o, lock);
        } catch (IOException ex) {
            throw new HorrocruxException(ex);
        }
        return bb;
    }

    @Override
    public void save(ByteBuffer bb, Path p) throws HorrocruxException {
        try {
            FileChannel fc = this.getChannel(p);
            if (fc == null) { // Trying to save a new created Horrocrux, not previously loaded/saved.
                fc = this.getNewChannel(p);
                if (fc == null) {
                    throw new HorrocruxException("File already exists");
                }
            }
            fc.truncate(0);
            fc.position(0);
            fc.write(bb);
        } catch (IOException ex) {
            throw new HorrocruxException(ex);
        }
    }

    public void saveOverwrite(ByteBuffer bb, Path p) throws HorrocruxException {
        try {
            FileChannel fc = this.getChannel(p);
            if (fc == null) { // Trying to save a new created Horrocrux, not previously loaded/saved.
                fc = this.getNewChannel(p);
                if (fc == null) {
                    fc = this.getNewChannelTrunc(p);
                }
            }
            fc.position(0);
            fc.write(bb);
        } catch (IOException ex) {
            throw new HorrocruxException(ex);
        }
    }

    @Override
    public void delete(Path o) throws HorrocruxException {
        try {
            this.close(o);
            Files.delete(o);
        } catch (IOException io) {
            throw new HorrocruxException(io);
        }
    }

    @Override
    public void close(Path o) throws HorrocruxException {
        FileChannel fc = this.getLoadedChannel(o);
        try {
            fc.force(true);
            fc.close();
            this.fshrx.remove(o);
        } catch (IOException ex) {
            throw new HorrocruxException(ex);
        }
    }

    private FileChannel getLoadedChannel(Path p) throws HorrocruxException {
        FileLock lock = this.fshrx.get(p);
        if (lock == null) {
            throw new HorrocruxException("File at '" + p + "' has not been previously loaded");
        }
        FileChannel fc = lock.channel();
        return fc;
    }

    private FileChannel getChannel(Path p) {
        FileLock lock = this.fshrx.get(p);
        if (lock == null) {
            return null;
        }
        FileChannel fc = lock.channel();
        return fc;
    }

    private FileChannel getNewChannel(Path p) throws IOException {
        if (Files.exists(p, HRX_IO_LINKOPTION)) {
            return null;
        }
        FileChannel fc = FileChannel.open(p, HRX_IO_OPEN_CRWS);
        FileLock fl = fc.lock();
        this.fshrx.put(p, fl);
        return fc;
    }

    private FileChannel getNewChannelTrunc(Path p) throws IOException {
        FileChannel fc = FileChannel.open(p, HRX_IO_OPEN_TCRWS);
        FileLock fl = fc.lock();
        this.fshrx.put(p, fl);
        return fc;
    }

}
