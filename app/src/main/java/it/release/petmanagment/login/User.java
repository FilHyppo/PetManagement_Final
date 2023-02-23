package it.release.petmanagment.login;

public class User {
    public String email, password;
    public User(){
        this.email = null;
        this.password = null;
    }
    public User(String email, String password){
        this.email = email;
        this.password = password;
    }

}
