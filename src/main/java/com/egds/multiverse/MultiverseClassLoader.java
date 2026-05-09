package com.egds.multiverse;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads {@link GreetingHashService} in an isolated class namespace,
 * bypassing parent delegation for that class only.
 *
 * <p>All other class resolution is routed to the system classloader so
 * that JDK types ({@code java.*}) remain accessible to the isolated class.
 * This creates a separate class identity for {@link GreetingHashService},
 * simulating an independent JVM context for parallel-universe execution.
 */
class MultiverseClassLoader extends ClassLoader {

    /** Fully-qualified class name loaded in the isolated namespace. */
    private static final String TARGET_CLASS =
            "com.egds.multiverse.GreetingHashService";

    /** Constructs a classloader with no parent delegate. */
    MultiverseClassLoader() {
        super(null);
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve)
            throws ClassNotFoundException {
        if (TARGET_CLASS.equals(name)) {
            return findClass(name);
        }
        return Class.forName(
                name, resolve, ClassLoader.getSystemClassLoader());
    }

    @Override
    protected Class<?> findClass(final String name)
            throws ClassNotFoundException {
        String resourcePath = name.replace('.', '/') + ".class";
        ClassLoader ctx =
                Thread.currentThread().getContextClassLoader();
        InputStream is = (ctx != null)
                ? ctx.getResourceAsStream(resourcePath)
                : ClassLoader.getSystemResourceAsStream(resourcePath);
        if (is == null) {
            throw new ClassNotFoundException(name);
        }
        try (InputStream stream = is) {
            byte[] bytes = stream.readAllBytes();
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
