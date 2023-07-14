package brainight.hrx.core;

import brainight.hrx.core.exceptions.HorrocruxException;
import java.util.Set;

/**
 * Github: https://github.com/Brainight
 *
 * @author Brainight
 */
public class MirroredHorrocrux extends Horrocrux {

    private boolean saved;

    public MirroredHorrocrux() {
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    @Override
    public void removeSecret(String name) {
        this.saved = false;
        super.removeSecret(name);
    }

    @Override
    public void updateSecret(String name, byte[] secret) throws HorrocruxException {
        this.saved = false;
        super.updateSecret(name, secret);
    }

    @Override
    public void addSecret(String name, byte[] secret) throws HorrocruxException {
        this.saved = false;
        super.addSecret(name, secret);
    }

    @Override
    public void setName(String name) throws HorrocruxException {
        this.saved = false;
        super.setName(name);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    public Set<String> getSecretsKeys() {
        return super.getSecrets().keySet();
    }

}
