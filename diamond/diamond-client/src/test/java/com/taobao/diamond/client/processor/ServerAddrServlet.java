package com.taobao.diamond.client.processor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ServerAddrServlet extends HttpServlet {
    String serverAddress;


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (serverAddress != null) {
            resp.getWriter().write(serverAddress);
            resp.getWriter().flush();
        }
        else {

        }
    }

}
