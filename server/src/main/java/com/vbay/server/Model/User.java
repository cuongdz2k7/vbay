package com.vbay.server.Model;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.vbay.shared.enums.Position;
import com.vbay.shared.enums.shared_status.UserStatus;

public class User {
    // Base 
    private long id;
    private final String timeinit; // thoi gian bat dau tao account
    private String username;
    private String email;
    private String passwordHash;
    private String phone_number;
    // Role + Status
    private Position position; //User , ADMIN
    private UserStatus status;
    // Auction
    private BigDecimal balance;
    //Constructor_init (Sign Up account)
    public User(String username,String email, String passwordHash, 
                String phone_number, BigDecimal balance){
                    //Base:
                    this.username = username;
                    this.email = email;
                    this.passwordHash = passwordHash;
                    this.phone_number = phone_number;
                    //Role + Status 
                    this.position = Position.USER;
                    this.status  = UserStatus.ACTIVE;
                    //Auction
                    this.balance=balance;
                    this.timeinit = LocalDate.now().toString();
                }
    //Constructor_2 (Getting account back)
    public User(String username , String email, String passwordHash, String phone_number,
                Position position,UserStatus status, BigDecimal balance, String timeinit){
                    this.username= username;
                    this.email = email;
                    this.passwordHash = passwordHash;
                    this.phone_number = phone_number;
                    this.position= position;
                    this.status= status;
                    this.balance = balance;
                    this.timeinit = timeinit;
                    
                }
    //Getter
    //a,Base:
    public long getId(){
        return this.id;
    }
    public String getTimeinit(){
        return this.timeinit;
    }
    public String getUserName(){
        return this.username;
    }
    public String getEmail(){
        return this.email;
    }
    public String getPasswordHash(){
        return this.passwordHash;
    }
    public String getPhoneNumber(){
        return this.phone_number;
    }
    //b, Role + Status:
    public Position getPosition(){
        return this.position;
    }
    public UserStatus getUserStatus(){
        return this.status;
    }
    //c, Auction Related: 
    public BigDecimal getBalance(){
        return this.balance;
    }
    public boolean isActive(){
        return this.status == UserStatus.ACTIVE;
    }
    public boolean isBanned(){
        return this.status == UserStatus.BANNED;
    }
    public boolean isSuspended(){
        return this.status == UserStatus.SUSPENDED;
    }
    public boolean isDeleted(){
        return this.status == UserStatus.DELETED;
    }

    //Setter (Change data)
    //a, Base:
    public void setId(long new_id){
        this.id = new_id;
    }
    public void setUserName(String new_username){
        this.username = new_username;
    }
    public void setEmail(String new_email){
        this.email = new_email;
    }
    public void setPasswordHash(String new_passworHash){
        this.passwordHash = new_passworHash;
    }
    public void setPhoneNumber(String new_phonenumber){
        this.phone_number = new_phonenumber;
    }
    //b, Role + Status :
    public void setPosition(Position new_position){
        this.position = new_position;
    }
    public void setStatus(UserStatus new_status){
        this.status = new_status;
    }
    //c, Auction:
    public void setBalance(BigDecimal new_balance){
        this.balance = new_balance;
    }


}


