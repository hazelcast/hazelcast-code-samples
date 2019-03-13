package multitenant;

import apps.app1.Person;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static com.hazelcast.nio.IOUtil.closeResource;
import static com.hazelcast.util.Preconditions.isNotNull;
import static java.util.Collections.enumeration;
import static java.util.Collections.singletonList;

/**
 * Filtering {@code ClassLoader} that:
 * <ul>
 *     <li>excludes classes from specific package prefixes</li>
 *     <li>optionally loads classes within a specific package by loading bytecode as a resource from
 *     parent classloader while loading the class itself</li>
 * </ul>
 *
 * As an example for the behavior of this classloader, see {@link #main(String[]) the main method in this class}.
 */
public class TenantClassloader extends ClassLoader {

    private static final int BUFFER_SIZE = 1024;

    private final byte[] buffer = new byte[BUFFER_SIZE];

    private final List<String> excludePackages;
    private final String enforcedSelfLoadingPackage;
    private ClassLoader delegatingClassLoader;

    public TenantClassloader(List<String> excludePackages, String enforcedSelfLoadingPackage, ClassLoader parent) {
        this.excludePackages = excludePackages;
        this.enforcedSelfLoadingPackage = enforcedSelfLoadingPackage;
        this.delegatingClassLoader = parent;
    }

    @Override
    public URL getResource(String name) {
        return checkResourceExcluded(name) ? null : delegatingClassLoader.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return checkResourceExcluded(name) ? enumeration(Collections.<URL>emptyList())
                : delegatingClassLoader.getResources(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return checkResourceExcluded(name) ? null : delegatingClassLoader.getResourceAsStream(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        isNotNull(name, "name");

        checkExcluded(name);

        if (enforcedSelfLoadingPackage != null && name.startsWith(enforcedSelfLoadingPackage)) {
            // we don't call registerAsParallelCapable() on JDK7+, so we need to synchronize on this.
            synchronized (this) {
                Class<?> clazz = findLoadedClass(name);
                if (clazz == null) {
                    clazz = loadAndDefineClass(name);
                }
                return clazz;
            }
        }
        return delegatingClassLoader.loadClass(name);
    }

    public boolean checkResourceExcluded(String resourceName) {
        if (resourceName == null) {
            return true;
        }
        // transform resource path as class name so we can check against excluded packages
        String resourceAsClassName = resourceName.replace('/', '.').replace(".class", "");
        for (String excludedPackage : excludePackages) {
            if (resourceAsClassName.startsWith(excludedPackage)) {
                return true;
            }
        }
        return false;
    }

    public void checkExcluded(String name) throws ClassNotFoundException {
        for (String excludedPackage : excludePackages) {
            if (name.startsWith(excludedPackage)) {
                throw new ClassNotFoundException(name + " - Package excluded explicitly!");
            }
        }
    }

    private Class<?> loadAndDefineClass(String name) throws ClassNotFoundException {
        InputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            is = getResourceAsStream(name.replace('.', '/') + ".class");
            os = new ByteArrayOutputStream();

            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }

            return defineClass(name, os.toByteArray(), 0, os.size());
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        } finally {
            closeResource(os);
            closeResource(is);
        }
    }

    public static void main(String[] args) throws Exception {
        TenantClassloader classloader = new TenantClassloader(singletonList("apps.app2"), "apps.app1",
                TenantClassloader.class.getClassLoader());
        Class<?> klass = classloader.loadClass("apps.app1.Person");
        // klass is "apps.app1.Person" -> following prints true
        System.out.println(klass.getName().equals(Person.class.getName()));
        // klass was loaded by TenantClassloader while Person.class is loaded by app classloader
        // --> following prints false
        System.out.println(klass.getClassLoader().equals(Person.class.getClassLoader()));
    }
}
