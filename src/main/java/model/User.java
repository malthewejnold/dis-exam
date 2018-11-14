package model;

import jdk.nashorn.internal.ir.annotations.Ignore;

public class User {

  public int id;
  public String firstname;
  public String lastname;
  public String email;
  private String password;
  private long createdTime;
  private String username;
  private String token;

  //Malthe: added createdtime and username to constructor
  public User(int id, String firstname, String lastname, String password, String email, long createdTime, String username, String token) {
    this.id = id;
    this.firstname = firstname;
    this.lastname = lastname;
    this.password = password;
    this.email = email;
    this.createdTime = createdTime;
    this.username = username;
    this.token = token;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getEmail() { return email; }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
  }

  public String getUsername() { return username; }

  public void setUsername(String username) { this.username = username; }

  public String getToken() { return token; }

  public void setToken(String token) { this.token = token; }
}
