package com.simple.probe;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@SpringBootTest
@RunWith(SpringRunner.class)
class ProbeApplicationTests {

    @Test
    public void contextLoads() {
    }

    public static void main(String[] args) throws Exception {
        String URL = "jdbc:mysql://127.0.0.1:3306/test?serverTimezone=UTC&characterEncoding=utf-8";
        String USER = "root";
        String PASSWORD = "970412@wcx.com";
        Class.forName("com.mysql.cj.jdbc.Driver");

        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        String sql = "SELECT * FROM user_info WHERE name = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, "wcx");
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            System.out.println(rs.getString("name") + " " + rs.getString("create_time"));
        }
    }

    @Test
    public void testAgent(){
        System.out.println("=== 我是测试方法 ===");
    }
}
