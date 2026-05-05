package com.vbay.shared.Utils;

public class IDGenerator {
    ///Để tạo ID duy nhất, chúng ta có thể sử dụng UUID (Universally Unique Identifier) trong Java. 
    ///UUID là một chuỗi 128-bit được tạo ra để đảm bảo tính duy nhất trên toàn cầu. 
    public static String generateID () {
        return java.util.UUID.randomUUID().toString();
    }
    /// gen auction id
    /// gen request id = respond id
    /// user --> disting
}
