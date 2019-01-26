package services;

import java.util.List;

public abstract class Service <Model> {

    public abstract Model find(int id);

    public abstract void save(Model obj);

    public abstract void update(Model obj);

    public abstract void delete(Model obj);

    public abstract List<Model> getAll();

}
