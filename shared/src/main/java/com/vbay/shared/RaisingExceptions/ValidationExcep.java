package com.vbay.shared.RaisingExceptions;
public class ValidationExcep extends RuntimeException {
    public ValidationExcep(String excep_message){
        super(excep_message);
    }
}
// XỬ LÝ SỰ VALID ( TỒN TẠI) của các features , attributes