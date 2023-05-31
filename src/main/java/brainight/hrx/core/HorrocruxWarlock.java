package brainight.hrx.core;

import brainight.hrx.core.Warlock.WarlockKey;
import brainight.hrx.core.exceptions.HorrocruxException;
import brainight.hrx.core.crypt.Crypter;
import brainight.hrx.core.crypt.HorrocruxCrypter;
import java.util.IdentityHashMap;
import java.util.Map;
import brainight.jutils.Bytes;
import brainight.jutils.Encoder;
import com.brainache.fcrypt.FCrypt;
import com.brainache.fcrypt.FResult;
import com.brainache.fcrypt.derivation.FCryptHashData;
import com.brainache.fcrypt.derivation.FCryptKDFunction;
import com.brainache.fcrypt.derivation.pbkdf2.FCryptWeedHandler;
import com.brainache.fcrypt.exceptions.FCryptException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Github: https://github.com/Brainight
 *
 * @author Brainight
 */
public class HorrocruxWarlock<I> implements Warlock<ByteBuffer, I> {

    public static final int MIN_PASSWD_SIZE = 8;
    private static final byte[] MN = new byte[]{0x21, 0x68, 0x72, 0x78};
    private final Map<Horrocrux, WarlockKey<I>> keys;
    private final Crypter cipher;
    private final int AGE;

    public HorrocruxWarlock() {
        this.keys = new IdentityHashMap<>();
        this.AGE = 0xaa;
        this.cipher = new HorrocruxCrypter();
    }

    public HorrocruxWarlock(Crypter cipher, int age) {
        this.AGE = age;
        this.keys = new IdentityHashMap<>();
        this.cipher = cipher;
    }

    /**
     *
     * @param hrx
     * @throws HorrocruxException
     */
    @Override
    public ByteBuffer vanish(Horrocrux hrx, byte[] key) throws HorrocruxException, IOException {
        WarlockKey<I> wk = this.keys.get(hrx);

        if (wk == null) {
            throw new HorrocruxException("Provided horrocrux is not known.");
        }

        if (key != null) {

            byte[] k;
            try {
                this.validatePassword(key);
                k = this.generateWarlockKey(key);
                wk = new WarlockKey(wk.getId(), k);
                this.keys.put(hrx, wk);
            } catch (FCryptException ex) {
                throw new HorrocruxException(ex);
            }

        }

        byte[] k0, k1, fk, cdata, cs, name;
        int size;

        k0 = Bytes.getBytes(wk.getKey(), 0, 32);
        fk = Bytes.getBytes(wk.getKey(), 32, wk.getKey().length);
        FResult fhd = FCryptHashData.buildFrom(fk);

        if (!fhd.isValid()) {
            throw new HorrocruxException("An internal error occurred. Cached fcrypt data for horrocrux is corrupted.");
        }

        k1 = ((FCryptHashData) fhd.getTarget()).getHash();

        cdata = getEncryptedSecrets(hrx, k1);
        if (cdata.length > 0xFFFFFFFF / 2) {
            throw new HorrocruxException("Horrocrux secret content is too long. Some secreys must be removed.");
        }

        name = Encoder.getUTF8(hrx.getName());
        size = MN.length + 1 + fk.length + 1 + name.length + 4 + cdata.length + 0 + 32;

        ByteBuffer hrxbb = ByteBuffer.allocate(size);
        hrxbb.put(MN);
        hrxbb.put((byte) fk.length);
        hrxbb.put(fk);
        hrxbb.put((byte) name.length);
        hrxbb.put(name);
        hrxbb.putInt(cdata.length);
        hrxbb.put(cdata);
        hrxbb.put(new byte[32]);
        hrxbb.position(5);
        cs = Bytes.getSHA256(hrxbb);
        hrxbb.position(hrxbb.limit() - 32);
        hrxbb.put(cs);
        hrxbb.clear();
        hrxbb.position(5);
        cdata = this.cipher.encrypt(hrxbb, k0);
        hrxbb.clear();
        hrxbb.position(5);
        hrxbb.put(cdata);
        hrxbb.position(0);
        return hrxbb;
    }

    private byte[] getEncryptedSecrets(Horrocrux hrx, byte[] k1) throws IOException, HorrocruxException {
        byte[] cdata;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream os = new DataOutputStream(bos)) {
            byte[] key, val;
            for (String skey : hrx.getSecrets().keySet()) {
                key = Encoder.getUTF8(skey);
                val = hrx.getSecrets().get(skey);
                os.write((byte) key.length);
                os.write((byte) val.length);
                os.write(key);
                os.write(val);
            }
            os.flush();
            cdata = this.cipher.encrypt(bos.toByteArray(), k1);
        }

        return cdata;
    }

    @Override
    public MirroredHorrocrux register(Horrocrux hrx, I id, byte[] key) throws HorrocruxException {
        this.validateNotRegistered(id);
        this.validatePassword(key);

        try {
            byte[] k = generateWarlockKey(key);
            WarlockKey<I> wk = new WarlockKey(id, k);
            this.keys.put(hrx, wk);
            MirroredHorrocrux mhrx = this.mirror(hrx);
            return mhrx;
        } catch (FCryptException ex) {
            throw new HorrocruxException(ex);
        }

    }

    private byte[] generateWarlockKey(byte[] password) throws FCryptException {
        FCryptWeedHandler fwh = (FCryptWeedHandler) FCrypt.derivator(FCryptKDFunction.WEED);
        byte[] k0 = fwh.hash(password, this.AGE);
        byte[] k1 = fwh.hide(password);
        byte[] k = new byte[k0.length + k1.length];
        System.arraycopy(k0, 0, k, 0, 32);
        System.arraycopy(k1, 0, k, 32, k1.length);
        return k;
    }

    @Override
    public Horrocrux summon(I id, ByteBuffer bb, byte[] password) throws HorrocruxException {
        this.validateNotRegistered(id);
        FCryptWeedHandler fwh = (FCryptWeedHandler) FCrypt.derivator(FCryptKDFunction.WEED);
        Horrocrux hrx = new Horrocrux();
        ByteBuffer keys;

        if (bb.remaining() < 8) {
            throw new HorrocruxException("Cannot summon horrocrux at '" + id.toString() + "'. Not properly forged");
        }
        try {

            byte[] mn = new byte[4];
            bb.get(mn);
            this.checkMagicNumber(mn);

            int fcl = 0xFF & bb.get();
            byte[] key = fwh.hash(password, this.AGE);
            byte[] raw = new byte[bb.remaining()];
            byte[] fc = new byte[fcl];
            byte[] cs = new byte[32];
            keys = ByteBuffer.allocate(32 + fcl);

            bb.get(raw);
            raw = this.cipher.decrypt(raw, key);
            bb = ByteBuffer.wrap(raw);

            bb.get(fc);
            bb.mark();
            bb.position(bb.limit() - 32);
            bb.get(cs);
            bb.position(bb.limit() - 32);
            bb.put(new byte[32]);
            bb.reset();

            byte[] fcs = Bytes.getSHA256(bb.array());
            if (!Bytes.contentEquals(cs, fcs)) {
                System.err.println("Hash validation failed. Horrocrux may be corrupted.");
            }

            FResult res = fwh.verify(Encoder.toChars(password), fc);
            if (!res.isValid()) {
                throw new HorrocruxException("Cannot summon horrocrux. Invalid password");
            }
            keys.put(key);
            keys.put(fc);

            int lname = (0xFF & bb.get());
            byte[] name = new byte[lname];
            bb.get(name);
            hrx.setName(Encoder.getUTF8(name));

            int sl = bb.getInt();
            if (sl < 0 || sl > bb.remaining()) {
                throw new HorrocruxException("Invalid secrets length '" + sl + "'");
            }

            FCryptHashData fcData = (FCryptHashData) res.getTarget();
            bb = this.cipher.decryptToBuffer(bb, fcData.getHash());

            int skl, svl;
            byte[] sk, sv;
            while (sl > 0) {
                if (bb.remaining() == 0) {
                    throw new HorrocruxException("Horrocrux is corrupted. Found EOF to soon");
                }
                skl = (0xFF & bb.get());
                svl = (0xFFFF & bb.getShort());
                sk = new byte[skl];
                sv = new byte[svl];
                bb.get(sk);
                bb.get(sv);
                sl -= skl + svl + 3;
                hrx.addSecret(Encoder.getUTF8(sk), sv);
            }
        } catch (FCryptException ex) {
            throw new HorrocruxException(ex);
        }

        WarlockKey key = new WarlockKey(id, keys.array());
        this.keys.put(hrx, key);
        return hrx;
    }

    private void checkMagicNumber(byte[] mn) throws HorrocruxException {
        if (!Arrays.equals(mn, MN)) {
            throw new HorrocruxException("Magic number of horrocrux file is corrupted");
        }
    }

    public MirroredHorrocrux mirror(Horrocrux hrx) throws HorrocruxException {
        MirroredHorrocrux mh = new MirroredHorrocrux();
        mh.setName(hrx.getName());
        for (String sname : hrx.getSecrets().keySet()) {
            byte[] secret = hrx.getSecrets().get(sname);
            mh.addSecret(sname, secret);
        }
        return mh;
    }

    @Override
    public I getHrxId(Horrocrux hrx) {
        WarlockKey<I> key = this.keys.get(hrx);
        return key.getId();
    }

    @Override
    public I removeHrxId(Horrocrux hrx) {
        WarlockKey<I> key = this.keys.remove(hrx);
        return key.getId();
    }

    @Override
    public boolean isKnown(final I id) {
        return this.keys.values().stream().anyMatch(k -> k.getId().equals(id));
    }

    private void validatePassword(byte[] password) throws HorrocruxException {
        if (Encoder.toChars(password).length < MIN_PASSWD_SIZE) {
            throw new HorrocruxException("Provided password it too weak. Min 8 characters long.");
        }
    }

    private void validateNotRegistered(final I id) throws HorrocruxException {
        if (this.keys.values().stream().anyMatch(wk -> wk.getId().equals(id))) {
            throw new HorrocruxException("Horrocrux is already registered");
        }
    }

}
