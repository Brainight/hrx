/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package brainight.hrx.core;

import brainight.hrx.core.exceptions.HorrocruxException;
import java.util.List;

/**
 * Github: https://github.com/Brainight
 *
 * @author Brainight
 */
public interface AzkabanImpl<T, O> {

    O create(T id, String name, byte[] key) throws HorrocruxException;

    O open(T id, byte[] key) throws HorrocruxException;

    void save(O hrx) throws HorrocruxException;

    void save(O hrx, byte[] key) throws HorrocruxException;

    void delete(O hrx) throws HorrocruxException;

    void close(O hrx) throws HorrocruxException;

    List<O> getHorroruxes();
    
    T getHorrocruxId(O hrx);

}
