package DAOVanila.DAO;

import java.util.List;
import java.util.Optional;

public interface DAO<K, E> {
    boolean delete(K id);
    E create(E item);
    void update(E item);
    Optional<E> findById(K id);
    List<E> findAll();
}
