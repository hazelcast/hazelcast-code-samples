package multitenant;

import java.io.Closeable;

public class TenantCloseable implements Closeable {

    private final ClassLoader originalClassLoader;

    public TenantCloseable(ClassLoader originalClassLoader) {
        this.originalClassLoader = originalClassLoader;
    }

    @Override
    public void close() {
        if (originalClassLoader != null) {
            // restore original thread context class loader
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
}
