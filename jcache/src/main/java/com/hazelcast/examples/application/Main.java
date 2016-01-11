package com.hazelcast.examples.application;

import com.hazelcast.examples.application.cache.UserCacheEntryListenerFactory;
import com.hazelcast.examples.application.cache.UserCacheLoader;
import com.hazelcast.examples.application.cache.UserCacheWriter;
import com.hazelcast.examples.application.commands.CacheAddCommand;
import com.hazelcast.examples.application.commands.CacheClearCommand;
import com.hazelcast.examples.application.commands.CacheEntryProcessorCommand;
import com.hazelcast.examples.application.commands.CacheGetCommand;
import com.hazelcast.examples.application.commands.CacheListCommand;
import com.hazelcast.examples.application.commands.CacheRemoveCommand;
import com.hazelcast.examples.application.commands.Command;
import com.hazelcast.examples.application.commands.DaoAddCommand;
import com.hazelcast.examples.application.commands.DaoGetCommand;
import com.hazelcast.examples.application.commands.DaoListCommand;
import com.hazelcast.examples.application.commands.DaoRemoveCommand;
import com.hazelcast.examples.application.commands.ExitCommand;
import com.hazelcast.examples.application.commands.HelpCommand;
import com.hazelcast.examples.application.dao.UserDao;
import com.hazelcast.examples.application.dao.UserDaoImpl;
import com.hazelcast.examples.application.model.User;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Main class to execute the Useraccount cache example
 */
@SuppressWarnings("checkstyle:classdataabstractioncoupling")
public class Main {

    public static void main(String[] args) throws Exception {
        // configure to use server mode since client is available on classpath
        System.setProperty("hazelcast.jcache.provider.type", "server");

        // create the fake database access
        UserDao userDao = new UserDaoImpl();

        // create the JCache cache instance
        Cache<Integer, User> userCache = configureCache(userDao);

        // configure command mapping
        Map<String, Command> commands = new LinkedHashMap<String, Command>();

        // general command mappings
        commands.put("help", new HelpCommand());
        commands.put("exit", new ExitCommand());
        // cache command mappings
        commands.put("cachelist", new CacheListCommand());
        commands.put("cacheadd", new CacheAddCommand());
        commands.put("cacheget", new CacheGetCommand());
        commands.put("cacheremove", new CacheRemoveCommand());
        commands.put("cacheclear", new CacheClearCommand());
        commands.put("cacheupdateep", new CacheEntryProcessorCommand());
        // DAO command mappings
        commands.put("daolist", new DaoListCommand());
        commands.put("daoadd", new DaoAddCommand());
        commands.put("daoget", new DaoGetCommand());
        commands.put("daoremove", new DaoRemoveCommand());

        // Context creation
        Context context = new Context(System.in, System.out, userDao, userCache, commands);

        context.newLine();
        context.writeln("Allowed commands: " + commands.keySet().toString());
        context.write("cmd> ");
        for (; ; ) {
            // read next command from commandline
            String command = context.readLine();
            if ("".equals(command) || command == null) {
                // ignore and show new cmd line
                continue;
            }
            // try to get command from mapping
            Command cmd = commands.get(command);
            if (cmd != null) {
                try {
                    cmd.execute(context);
                } catch (Exception e) {
                    context.writeln("Problem: " + e);
                }
            } else {
                context.writeln("Allowed commands: " + commands.keySet().toString());
            }
            context.write("cmd> ");
        }
    }

    /**
     * Creates a new {@link javax.cache.Cache} instance backed by the given
     * {@link com.hazelcast.examples.application.dao.UserDao} configured with
     * read-through and write-through
     *
     * @param userDao the {@link com.hazelcast.examples.application.dao.UserDao} backing store for the cache
     * @return the created {@link javax.cache.Cache} instance
     */
    private static Cache<Integer, User> configureCache(UserDao userDao) {
        // explicitly retrieve the Hazelcast backed javax.cache.spi.CachingProvider
        CachingProvider cachingProvider = Caching.getCachingProvider(
                "com.hazelcast.cache.HazelcastCachingProvider"
        );

        // retrieve the javax.cache.CacheManager
        CacheManager cacheManager = cachingProvider.getCacheManager();

        // create javax.cache.configuration.CompleteConfiguration subclass
        CompleteConfiguration<Integer, User> config =
                new MutableConfiguration<Integer, User>()
                        // configure the cache to be typesafe
                        .setTypes(Integer.class, User.class)
                        // configure to expire entries 30 secs after creation in the cache
                        .setExpiryPolicyFactory(FactoryBuilder.factoryOf(
                                new AccessedExpiryPolicy(new Duration(TimeUnit.SECONDS, 30))
                        ))
                        // configure read-through of the underlying store
                        .setReadThrough(true)
                        // configure write-through to the underlying store
                        .setWriteThrough(true)
                        // configure the javax.cache.integration.CacheLoader
                        .setCacheLoaderFactory(FactoryBuilder.factoryOf(
                                new UserCacheLoader(userDao)
                        ))
                        // configure the javax.cache.integration.CacheWriter
                        .setCacheWriterFactory(FactoryBuilder.factoryOf(
                                new UserCacheWriter(userDao)
                        ))
                        // configure the javax.cache.event.CacheEntryListener with no javax.cache.event.CacheEntryEventFilter,
                        // to include old value and to be executed synchronously
                        .addCacheEntryListenerConfiguration(
                                new MutableCacheEntryListenerConfiguration<Integer, User>(
                                        new UserCacheEntryListenerFactory(),
                                        null, true, true
                                )
                        );

        // create the cache called "users" and using the previous configuration
        return cacheManager.createCache("users", config);
    }
}
