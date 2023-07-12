package brainight.hrx.core;

import java.util.Set;

/**
 * Github: https://github.com/Brainight
 * @author Brainight
 */
public class MirroredHorrocrux extends Horrocrux{

    private boolean saved;
    public MirroredHorrocrux(){}

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
    

    public Set<String> getSecretsKeys(){
        return super.getSecrets().keySet();
    }
    
    
    
    
    
    
    
}
