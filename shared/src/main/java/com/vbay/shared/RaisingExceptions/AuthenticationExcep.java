package com.vbay.shared.RaisingExceptions;

public class AuthenticationExcep extends Exception { // Xac thuc danh tinh
    public AuthenticationExcep(String excep_message){
        super(excep_message);
    }
}
