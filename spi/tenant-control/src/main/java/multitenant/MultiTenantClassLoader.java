package multitenant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class loader that delegates loading of classes to specific class loader
 * per package or to the parent classloader, when no class loader is explicitly
 * defined for a package.
 *
 * Only for demonstration, do not use in production.
 */
public class MultiTenantClassLoader extends ClassLoader {

    static final ConcurrentMap<String, ClassLoader> CLASS_LOADER_PER_PREFIX = new ConcurrentHashMap<>();

    public MultiTenantClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        for (Map.Entry<String, ClassLoader> entry : CLASS_LOADER_PER_PREFIX.entrySet()) {
            if (name.startsWith(entry.getKey())) {
                return entry.getValue().loadClass(name);
            }
        }
        return super.loadClass(name);
    }

    public void addClassLoader(String prefix, ClassLoader classLoader) {
        CLASS_LOADER_PER_PREFIX.put(prefix, classLoader);
    }
}
