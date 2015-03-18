package io.techery.snapper.model;

public class User implements Indexable {

    private String userId;
    private int age = 100;

    @Override
    public byte[] index() {
        return userId.getBytes();
    }

    public User(String userId) {
        this.userId = userId;
    }

    public User(String userId, int age) {
        this.userId = userId;
        this.age = age;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
