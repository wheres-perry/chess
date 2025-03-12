package dataaccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import dataaccess.implementations.DatabaseManager;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Connection;

public class MYSQLTests {
    
    private Method createDatabaseMethod;
    private Method getConnectionMethod;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Debug: Check if properties file is accessible
        InputStream propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties");
        if (propStream == null) {
            System.err.println("WARNING: db.properties not found in classpath!");
            
            // Check locations
            File mainResources = new File("/workspaces/chess/server/src/main/resources/db.properties");
            File testResources = new File("/workspaces/chess/server/src/test/resources/db.properties");
            
            System.out.println("Main resources exists: " + mainResources.exists());
            System.out.println("Test resources exists: " + testResources.exists());
        } else {
            System.out.println("db.properties found successfully!");
            propStream.close();
        }
        
        // Get access to the private methods using reflection
        createDatabaseMethod = DatabaseManager.class.getDeclaredMethod("createDatabase");
        createDatabaseMethod.setAccessible(true);
        
        getConnectionMethod = DatabaseManager.class.getDeclaredMethod("getConnection");
        getConnectionMethod.setAccessible(true);
    }
    
    @Test
    public void testCreateDatabase() throws Exception {
        // This should not throw an exception if the database is created successfully
        assertDoesNotThrow(() -> {
            createDatabaseMethod.invoke(null);
        });
    }
    
    @Test
    public void testDatabaseConnection() throws Exception {
        // Try to get a connection and verify it's valid
        Connection conn = null;
        try {
            conn = (Connection) getConnectionMethod.invoke(null);
            assertNotNull(conn, "Database connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
            
            // Check if we can execute a simple query
            try (var statement = conn.createStatement()) {
                try (var rs = statement.executeQuery("SELECT 1")) {
                    assertTrue(rs.next());
                    assertEquals(1, rs.getInt(1));
                }
            }
        } finally {
            // Always close the connection
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }
}