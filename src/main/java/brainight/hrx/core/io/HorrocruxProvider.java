package brainight.hrx.core.io;

import brainight.hrx.core.exceptions.HorrocruxException;

/**
 * Github: https://github.com/Brainight
 * @author Brainight
 */
public interface HorrocruxProvider<O, D> {
    
    void create(O o) throws HorrocruxException;
    
    D load(O o) throws HorrocruxException;
    
    void save(D r, O o) throws HorrocruxException;
    
    void delete(O o) throws HorrocruxException;
    
    void close(O o) throws HorrocruxException;
    
    
}
 