package dao;

import java.util.List;

public abstract class Dao<Model> {

    public abstract Model findById(int id);

    public abstract void save(Model obj);

    public abstract void update(Model obj);

    public abstract void delete(Model obj);

    public abstract List<Model> getAll();

}
