package com.aita.plagiarism.dao;

import com.aita.plagiarism.config.DBContext;
import com.aita.plagiarism.model.User;
import org.junit.jupiter.api.Test;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class GoogleAccountIntegrationTest {
    @Test void bindsOnlyVerifiedExistingAccountAndKeepsStableSubject() throws Exception {
        String unique = UUID.randomUUID().toString().replace("-","");
        int id = fixture(unique);
        String email = unique + "@gmail.com";
        UserDAO dao = new UserDAO();
        try {
            assertNull(dao.authenticateGoogle(unique,email,false));
            assertNull(dao.authenticateGoogle(unique,"missing-"+email,true));
            User user = dao.authenticateGoogle(unique,email,true);
            assertEquals(id,user.getUserId()); assertEquals("STUDENT",user.getRole());
            assertEquals(id,dao.authenticateGoogle(unique,"changed@example.invalid",false).getUserId());
            assertNull(dao.authenticateGoogle("other-"+unique,email,true));
            try(var conn=DBContext.getConnection();var ps=conn.prepareStatement("SELECT google_subject FROM Users WHERE user_id = ?")) {
                ps.setInt(1,id);try(var rs=ps.executeQuery()) { assertTrue(rs.next());assertEquals(unique,rs.getString(1)); }
            }
        } finally { cleanup(id); }
    }
    @Test void simultaneousFirstLoginBindsOnce() throws Exception {
        String unique=UUID.randomUUID().toString().replace("-","");int id=fixture(unique);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        CountDownLatch start=new CountDownLatch(1);
        Callable<Integer> login=()->{start.await();return new UserDAO().authenticateGoogle(unique,unique+"@gmail.com",true).getUserId();};
        try {
            Future<Integer> first=pool.submit(login),second=pool.submit(login);start.countDown();
            assertEquals(id,first.get(15,TimeUnit.SECONDS));assertEquals(id,second.get(15,TimeUnit.SECONDS));
        } finally {pool.shutdownNow();cleanup(id);}
    }
    private int fixture(String unique) throws Exception {
        try(var conn=DBContext.getConnection();var ps=conn.prepareStatement(
                "INSERT Users(username,password_hash,full_name,email,role) VALUES (?,'unusable','Google fixture',?,'STUDENT')",Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,"gtest_"+unique);ps.setString(2,unique+"@gmail.com");ps.executeUpdate();
            try(var rs=ps.getGeneratedKeys()){assertTrue(rs.next());return rs.getInt(1);}
        }
    }
    private void cleanup(int id) throws Exception {
        try(var conn=DBContext.getConnection();var ps=conn.prepareStatement("DELETE Users WHERE user_id=? AND username LIKE 'gtest_%'")) {ps.setInt(1,id);ps.executeUpdate();}
    }
}
