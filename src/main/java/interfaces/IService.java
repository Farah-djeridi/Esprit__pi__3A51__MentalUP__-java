package interfaces;

import java.util.List;

public interface IService<T> {

    public void add(T p);
    public List<T> getAll();
    public void update(T p);
    public void delete(T p);
    // getby
    //getbyId ...
}
