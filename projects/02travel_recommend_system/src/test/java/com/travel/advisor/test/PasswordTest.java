package com.travel.advisor.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordTest {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Test
    public void generatePassword() {
        String encode = passwordEncoder.encode("123456");
        System.out.println("encode = " + encode);
    }
}
