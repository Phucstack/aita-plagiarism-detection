package com.aita.plagiarism.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUpgradeTest {
    @Test void saltsDifferAndPasswordsVerifyExactly() {
        String a = PasswordUtil.hashPassword("Mật khẩu thử nghiệm");
        String b = PasswordUtil.hashPassword("Mật khẩu thử nghiệm");
        assertNotEquals(a, b);
        assertTrue(PasswordUtil.verifyPassword("Mật khẩu thử nghiệm", a));
        assertFalse(PasswordUtil.verifyPassword("mật khẩu thử nghiệm", a));
        assertFalse(PasswordUtil.needsUpgrade(a));
    }
    @Test void malformedAndExpensiveFormatsAreRejected() {
        assertFalse(PasswordUtil.verifyPassword("test", "pbkdf2-sha256$999999999$a$b"));
        assertFalse(PasswordUtil.verifyPassword("test", "pbkdf2-sha256$600000$invalid$invalid"));
        assertTrue(PasswordUtil.needsUpgrade(PasswordUtil.hashMD5("old")));
        assertTrue(PasswordUtil.needsUpgrade(PasswordUtil.hashSHA256("old")));
    }
}
