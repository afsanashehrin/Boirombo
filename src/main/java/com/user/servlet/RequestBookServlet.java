package com.user.servlet;

import com.DAO.BookDAOimpl;
import com.DB.DBConnect;
import com.entity.Request;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@SuppressWarnings("serial")
@WebServlet("/request")
@MultipartConfig
public class RequestBookServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            HttpSession session = req.getSession();
            Integer userId = (Integer) session.getAttribute("userId"); // Fetch userId from session
            if (userId == null) {
                response.sendRedirect("login.jsp");  // Redirect to login page if user is not logged in
                return;
            }

            String bookName = request.getParameter("bookName");
            String authorName = request.getParameter("authorName");
            String description = request.getParameter("description");

            Part part = request.getPart("bookImage");
            String bookImage = null;

            if (part != null && part.getSize() > 0) {
                bookImage = part.getSubmittedFileName();
                String uploadPath = getServletContext().getRealPath("") + "book_images" + File.separator + bookImage;
                File file = new File(uploadPath);
                if (!file.exists()) {
                    file.mkdirs(); // Create directory if it does not exist
                }
                part.write(uploadPath); // Save the file to the server
            }

            // Create a Request object
            Request req = new Request();
            req.setUserId(userId);
            req.setBookName(bookName);
            req.setAuthorName(authorName);
            req.setDescription(description);
            req.setBookImage(bookImage); // Set bookImage only if file was uploaded

            // Save the request in the database
            BookDAOimpl dao = new BookDAOimpl(DBConnect.getConn());
            boolean isAdded = dao.addBookRequest(req);

            // Handle success or failure based on the result
            if (isAdded) {
                session.setAttribute("Success", "Request has been submitted successfully.");
                response.sendRedirect("user/request.jsp");
            } else {
                session.setAttribute("Failed", "Failed to submit the request. Please try again.");
                response.sendRedirect("user/request.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log exception if needed
            request.setAttribute("error", "An unexpected error occurred while processing your request.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }
}
