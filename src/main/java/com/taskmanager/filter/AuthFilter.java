package com.taskmanager.filter;

import com.taskmanager.model.User;
import com.taskmanager.util.SessionUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // no setup required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        User user = SessionUtil.getUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getRequestURI();
        String ctx = req.getContextPath();
        String relative = path.substring(ctx.length());

        if (relative.startsWith("/manager") && !user.isManager()) {
            resp.sendRedirect(req.getContextPath() + "/employee/tasks");
            return;
        }
        if (relative.startsWith("/employee") && !user.isEmployee()) {
            resp.sendRedirect(req.getContextPath() + "/manager/tasks");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no cleanup required
    }
}
