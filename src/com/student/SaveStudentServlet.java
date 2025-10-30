package com.student;

// --- IMPORTANT! ---
// We use 'jakarta.servlet' because we are on Tomcat 11
// If we were on Tomcat 9, we would use 'javax.servlet'
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// Import Cassandra driver classes
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;

// This annotation automatically maps this servlet to the URL '/SaveStudent'
@WebServlet("/SaveStudent")
public class SaveStudentServlet extends HttpServlet {

    private PreparedStatement insertStatement;
    private CqlSession session;

    @Override
    public void init() throws ServletException {
        // This method runs ONCE when the servlet is first loaded.
        
        // 1. Get the single Cassandra session from our connector
        session = CassandraConnector.getSession();
        
        // 2. Prepare the INSERT statement one time for efficiency.
        // We use placeholders (?) to prevent CQL injection attacks.
        String cql = "INSERT INTO students (student_number, name, program, year) VALUES (?, ?, ?, ?)";
        insertStatement = session.prepare(cql);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // This method runs EVERY TIME a POST request is sent to /SaveStudent
        
        String message = "";
        
        try {
            // 1. Get the four parameters from the HTML form
            String studentNumber = request.getParameter("student_number");
            String name = request.getParameter("name");
            String program = request.getParameter("program");
            int year = Integer.parseInt(request.getParameter("year")); // Convert year to an integer

            // 2. Bind the variables to our prepared statement
            BoundStatement boundStatement = insertStatement.bind(studentNumber, name, program, year);
            
            // 3. Execute the statement to save the data
            session.execute(boundStatement);
            
            // 4. Set a success message
            message = "Success! Student " + name + " has been registered.";

        } catch (NumberFormatException e) {
            // Handle error if 'year' is not a valid number
            message = "Error: Academic Year must be a number.";
            e.printStackTrace();
        } catch (Exception e) {
            // Handle any other database errors
            message = "Error: Could not save student data. " + e.getMessage();
            e.printStackTrace();
        }
        
        // 5. Send the message back to the JSP page
        // We set the message as a "request attribute"
        request.setAttribute("message", message);
        
        // Forward the request (and the message) back to index.jsp to display it
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    @Override
    public void destroy() {
        // This is called when the server shuts down
        CassandraConnector.close();
    }
}