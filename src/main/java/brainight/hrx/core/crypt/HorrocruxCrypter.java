package brainight.hrx.core.crypt;

import brainight.hrx.core.exceptions.HorrocruxException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Brainight
 */
public class HorrocruxCrypter implements Crypter {

    @Override
    public byte[] encrypt(byte[] data, byte[] key) throws HorrocruxException {
        try {
            Cipher cipher = Cipher.getInstance("ChaCha20");
            ChaCha20ParameterSpec paramSpec = new ChaCha20ParameterSpec(new byte[12], 1);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "ChaCha20"), paramSpec);
            byte[] eData = cipher.doFinal(data);
            return eData;
        } catch (Exception ex) {
            throw new HorrocruxException(ex);
        }
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) throws HorrocruxException {
        try {
            Cipher cipher = Cipher.getInstance("ChaCha20");
            ChaCha20ParameterSpec paramSpec = new ChaCha20ParameterSpec(new byte[12], 1);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "ChaCha20"), paramSpec);
            byte[] dData = cipher.doFinal(data);
            return dData;
        } catch (Exception ex) {
            throw new HorrocruxException(ex);
        }
    }

    @Override
    public byte[] encrypt(ByteBuffer data, byte[] key) throws HorrocruxException {
        byte[] raw = new byte[data.remaining()];
        data.get(raw);
        return this.encrypt(raw, key);
    }

    @Override
    public byte[] decrypt(ByteBuffer data, byte[] key) throws HorrocruxException {
        byte[] raw = new byte[data.remaining()];
        data.get(raw);
        return this.decrypt(raw, key);
    }

    @Override
    public ByteBuffer encryptToBuffer(ByteBuffer data, byte[] key) throws HorrocruxException {
        return ByteBuffer.wrap(this.encrypt(data, key));
    }

    @Override
    public ByteBuffer decryptToBuffer(ByteBuffer data, byte[] key) throws HorrocruxException {
        return ByteBuffer.wrap(this.decrypt(data, key));
    }

}
