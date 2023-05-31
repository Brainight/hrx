package brainight.hrx.core.crypt;

import brainight.hrx.core.exceptions.HorrocruxException;
import java.nio.ByteBuffer;

/**
 *
 * @author Brainight
 */
public interface Crypter {

    byte[] encrypt(byte[] data, byte[] key) throws HorrocruxException;

    byte[] decrypt(byte[] data, byte[] key) throws HorrocruxException;

    byte[] encrypt(ByteBuffer data, byte[] key) throws HorrocruxException;

    byte[] decrypt(ByteBuffer data, byte[] key) throws HorrocruxException;

    ByteBuffer encryptToBuffer(ByteBuffer data, byte[] key) throws HorrocruxException;

    ByteBuffer decryptToBuffer(ByteBuffer data, byte[] key) throws HorrocruxException;

}
