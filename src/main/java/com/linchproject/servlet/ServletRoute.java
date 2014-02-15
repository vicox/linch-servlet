package com.linchproject.servlet;

import com.linchproject.core.Route;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Georg Schmidl
 */
public class ServletRoute extends Route {

    private static String USER_ID_KEY = ServletRoute.class.getSimpleName() + "-user-id";

    public HttpServletRequest request;

    public ServletRoute(HttpServletRequest request) {
        this.request = request;

        String path = request.getRequestURI().substring(request.getContextPath().length() + 1);
        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }
        setPath(path);
    }

    @Override
    public String getUrl() {
        return this.request.getContextPath() + getPath();

    }

    @Override
    public String getUserId() {
        return (String) request.getSession().getAttribute(USER_ID_KEY);
    }

    @Override
    public void setUserId(String userId) {
        request.getSession().setAttribute(USER_ID_KEY, userId);
    }

    @Override
    protected Route newRoute() {
        return new ServletRoute(request);
    }
}
