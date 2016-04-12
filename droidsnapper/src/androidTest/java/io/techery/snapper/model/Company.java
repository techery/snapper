package io.techery.snapper.model;

import java.nio.ByteBuffer;
import java.util.List;

public class Company implements Indexable {

    int id;
    String name;
    List<User> users;

    public Company(int id, String name, List<User> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    @Override public byte[] index() {
        return ByteBuffer.allocate(4).putInt(id).array();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Company company = (Company) o;

        return id == company.id;

    }

    @Override public int hashCode() {
        return id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
