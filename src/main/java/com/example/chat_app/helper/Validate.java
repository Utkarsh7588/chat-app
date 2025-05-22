package com.example.chat_app.helper;

import java.util.regex.Pattern;

public class Validate {
    public static boolean validateEmail(String email){
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}
