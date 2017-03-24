package com.sysc4806;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxwelldemelo on 3/2/2017.
 */

@Entity
public class User {

    @Id
    @GeneratedValue
    private long id;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<User> following;

    private String name;
    private String email;

    public User() { following = new ArrayList<>(); }
    public User(String email) { this(); setEmail(email); }

    public long getId(){
        return id;
    }
    public void setId(long id){
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<User> getFollowing() { return following; }
    public void setFollowing(List<User> following) { this.following = following; }

    public void follow(User u) {
        following.add(u);
    }

    public void unfollow(User u) {
        following.remove(u);
    }
}
