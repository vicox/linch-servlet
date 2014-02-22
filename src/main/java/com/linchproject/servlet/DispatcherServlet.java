package com.linchproject.servlet;

import com.linchproject.apps.App;
import com.linchproject.apps.AppRegistry;
import com.linchproject.core.Injector;
import com.linchproject.core.Invoker;
import com.linchproject.core.Result;
import com.linchproject.core.Route;
import com.linchproject.ioc.Container;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Georg Schmidl
 */
public class DispatcherServlet extends HttpServlet {

    private static final String APP_PROPERTIES = "app.properties";
    private static final String CONTROLLER_SUB_PACKAGE = "controllers";

    private Invoker invoker;
    private String appPackage;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ClassLoader classLoader = getClass().getClassLoader();

        AppRegistry appRegistry = new AppRegistry();
        appRegistry.loadFromClassPath();

        App mainApp;
        try {
            mainApp = App.load(classLoader, APP_PROPERTIES);
        } catch (IOException e) {
            throw new ServletException("missing " + APP_PROPERTIES, e);
        }

        appPackage = mainApp.get("package");

        final Container container = new Container();
        container.add("app", mainApp);

        for (App app : appRegistry.getApps()) {
            for (Map.Entry<String, String> entry: app.getMap("component.").entrySet()) {
                Class<?> componentClass;
                try {
                    componentClass = classLoader.loadClass(entry.getValue());
                } catch (ClassNotFoundException e) {
                    throw new ServletException("class not found for component " + entry.getKey(), e);
                }
                container.add(entry.getKey(), componentClass);
            }
        }

        this.invoker = new Invoker(classLoader, new Injector() {
            @Override
            public void inject(Object object) {
                container.inject(object);
            }
        });
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        dispatch(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        dispatch(request, response);
    }

    protected void dispatch(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String controllersPackage = appPackage != null? appPackage + "." + CONTROLLER_SUB_PACKAGE : CONTROLLER_SUB_PACKAGE;

        Route route = new ServletRoute(request);
        route.setControllerPackage(controllersPackage);

        Result result = invoker.invoke(route);
        ReplierFactory.getReplier(result).reply(response);
    }
}
