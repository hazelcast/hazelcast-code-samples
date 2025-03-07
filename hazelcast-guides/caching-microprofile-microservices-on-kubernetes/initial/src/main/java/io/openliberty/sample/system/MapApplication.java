package io.openliberty.sample.system;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("application")
@ApplicationScoped
public class MapApplication extends Application {
}