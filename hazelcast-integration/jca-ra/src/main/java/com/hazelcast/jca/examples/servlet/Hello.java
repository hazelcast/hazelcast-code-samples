package com.hazelcast.jca.examples.servlet;

import com.hazelcast.core.IMap;
import com.hazelcast.jca.HazelcastConnection;

import javax.annotation.Resource;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/Hello")
public class Hello extends HttpServlet {
    private static final long serialVersionUID = -8314035702649252239L;

    @Resource(mappedName = "java:/HazelcastCF")
    protected ConnectionFactory connectionFactory;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();
        out.write("<h1>Hazelcast JCA Example</h1>");
        out.write("<form action='?' method='GET'><input name='action' value='put' type='hidden' />"
                + "<input type='text' name='data' /><input type='submit' value='PUT' /></form>");
        out.write("<a href='?action=clear'>CLEAR</a>");
        out.write("<br />");
        out.write("<br />");

        HazelcastConnection hzConn = null;
        try {
            hzConn = getConnection();
            IMap<Integer, String> map = hzConn.getMap("example");

            String action = req.getParameter("action");
            if ("put".equals(action)) {
                map.put(map.size(), req.getParameter("data"));
            } else if ("clear".equals(action)) {
                map.clear();
            }

            out.write("MAP: <br />");
            for (int i = 0; i < map.size(); i++) {
                out.write(i + "=>" + map.get(i) + "<br />");
            }

        } finally {
            closeConnection(hzConn);
            closeWriter(out);
        }
    }

    private void closeWriter(PrintWriter out) {
        if (out != null) {
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private HazelcastConnection getConnection() {
        HazelcastConnection c = null;
        try {
            return (HazelcastConnection) connectionFactory.getConnection();
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeConnection(HazelcastConnection hzConn) {
        if (hzConn != null) {
            try {
                hzConn.close();
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
