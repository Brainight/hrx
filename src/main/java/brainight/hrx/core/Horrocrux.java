package brainight.hrx.core;

import brainight.hrx.core.exceptions.HorrocruxException;
import brainight.jutils.Encoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Github: https://github.com/Brainight
 *
 * @author Brainight
 */
public class Horrocrux {

    private String name;
    private Map<String, byte[]> secrets;

    public Horrocrux() {
        this.secrets = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws HorrocruxException {
        validateName(name);
        this.name = name;
    }

    public void addSecret(String name, byte[] secret) throws HorrocruxException {
        if (this.secrets.containsKey(name)) {
            throw new HorrocruxException("A secret with name '" + name + "' already exists.");
        }

        validateName(name);
        validateSecret(secret);
        this.secrets.put(name, secret);
    }

    public void updateSecret(String name, byte[] secret) throws HorrocruxException {
        validateName(name);
        validateSecret(secret);
        this.secrets.compute(name, (k, v) -> secret);
    }

    public void removeSecret(String name) {
        this.secrets.remove(name);
    }

    Map<String, byte[]> getSecrets() {
        return this.secrets;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + Objects.hashCode(this.secrets);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Horrocrux other = (Horrocrux) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.secrets, other.secrets);
    }

    private static void validateSecret(byte[] sec) throws HorrocruxException {
        if (sec.length > 0xFFFF) {
            throw new HorrocruxException("Secret content is too long. Max 2^16 bytes");
        }
    }

    private static void validateName(String name) throws HorrocruxException {
        if (Encoder.getUTF8(name).length > 0xFF) {
            throw new HorrocruxException("Secret name is too long. Max 255 bytes");
        }
    }

}
