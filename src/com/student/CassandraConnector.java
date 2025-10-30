package com.student;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;

public class CassandraConnector {

    private static CqlSession session;

    // Private constructor to prevent others from creating a new instance
    private CassandraConnector() {}

    // Static method to get the single instance of the CqlSession
    public static CqlSession getSession() {
        if (session == null) {
            try {
                // Connect to the Cassandra cluster on localhost, default port 9042
                // and specify the keyspace we want to use.
                session = CqlSession.builder()
                        .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                        .withKeyspace("student_ks") // Use the keyspace you created
                        .withLocalDatacenter("datacenter1") // Required driver setting
                        .build();
                
                System.out.println("Successfully connected to Cassandra!");
                
            } catch (Exception e) {
                // Log the error if connection fails
                System.err.println("Failed to connect to Cassandra: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return session;
    }

    // A method to close the connection when the application shuts down
    public static void close() {
        if (session != null) {
            session.close();
            System.out.println("Cassandra connection closed.");
        }
    }
}