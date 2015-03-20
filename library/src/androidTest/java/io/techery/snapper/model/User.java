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

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (userId != null ? !userId.equals(user.userId) : user.userId != null) return false;

        return true;
    }

    @Override public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
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

    @Override public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", age=" + age +
                '}';
    }
}
