/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package simpleldap;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.LdapComparator;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.comparators.NormalizingComparator;
import org.apache.directory.api.ldap.model.schema.registries.ComparatorRegistry;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.extractor.impl.ResourceMap;
import org.apache.directory.api.ldap.schema.loader.JarLdifSchemaLoader;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.api.util.FileUtils;
import org.apache.directory.api.util.IOUtils;
import org.apache.directory.api.util.exception.Exceptions;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.interceptor.context.AddOperationContext;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.factory.AvlPartitionFactory;
import org.apache.directory.server.core.factory.DirectoryServiceFactory;
import org.apache.directory.server.core.factory.PartitionFactory;
import org.apache.directory.server.core.partition.impl.avl.AvlPartition;
import org.apache.directory.server.core.partition.ldif.AbstractLdifPartition;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

/**
 * Simple in-memory LDAP server implementation (based on ApacheDS).
 */
public class SimpleLdapServer {

    private static final String DEFAULT_LDIF_FILENAME = "hazelcast.com.ldif";

    private DirectoryService directoryService;
    private LdapServer ldapServer;

    /**
     * Create a single LDAP server.
     */
    public SimpleLdapServer(String[] ldifFiles) throws Exception {
        long startTime = System.currentTimeMillis();

        InMemoryDirectoryServiceFactory dsFactory = new InMemoryDirectoryServiceFactory();
        dsFactory.init("ds");

        directoryService = dsFactory.getDirectoryService();
        directoryService.setAllowAnonymousAccess(false);
        System.out.println("Directory service started in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    public SimpleLdapServer() throws Exception {
        this(null);
    }

    /**
     * Starts an LDAP server.
     */
    public static void main(String[] args) throws Exception {
        SimpleLdapServer ldap = new SimpleLdapServer(args);
        ldap.importLdifFiles(args);
        ldap.start();
    }

    private void start() throws Exception {
        long startTime = System.currentTimeMillis();
        ldapServer = new org.apache.directory.server.ldap.LdapServer();
        ldapServer.setTransports(new TcpTransport("127.0.0.1", 10389));
        ldapServer.setDirectoryService(directoryService);
        ldapServer.start();

        System.out.println("Your LDAP server is running now");
        System.out.println("URL:      ldap://127.0.0.1:10389");
        System.out.println("User DN:  uid=admin,ou=system");
        System.out.println("Password: secret");
        System.out.println("LDAP server started in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * Stops LDAP server.
     */
    public void stop() throws Exception {
        ldapServer.stop();
    }

    /**
     * Stops LDAP server and the underlying directory service.
     */
    public void terminate() throws Exception {
        stop();
        directoryService.shutdown();
    }

    /**
     * Imports given LDIF files to the directory using given directory service and schema manager.
     *
     * @throws Exception
     */
    public void importLdifFiles(String... ldifFiles) throws Exception {
        long startTime = System.currentTimeMillis();
        if (ldifFiles == null || ldifFiles.length == 0) {
            System.out.println("Importing default data\n");
            importLdif(new LdifReader(SimpleLdapServer.class.getResourceAsStream("/" + DEFAULT_LDIF_FILENAME)));
        } else {
            for (String ldifFile : ldifFiles) {
                System.out.println("Importing " + ldifFile + "\n");
                importLdif(new LdifReader(ldifFile));
            }
        }
        System.out.println("LDIF(s) imported in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    private void importLdif(LdifReader ldifReader) throws Exception {
        try {
            for (LdifEntry ldifEntry : ldifReader) {
                checkPartition(ldifEntry);
                System.out.print(ldifEntry.toString());
                directoryService.getAdminSession()
                        .add(new DefaultEntry(directoryService.getSchemaManager(), ldifEntry.getEntry()));
            }
        } finally {
            IOUtils.closeQuietly(ldifReader);
        }
    }

    private void checkPartition(LdifEntry ldifEntry) throws Exception {
        Dn dn = ldifEntry.getDn();
        Dn parent = dn.getParent();
        try {
            directoryService.getAdminSession().exists(parent);
        } catch (Exception e) {
            System.out.println("Creating new partition for DN=" + dn + "\n");
            AvlPartition partition = new AvlPartition(directoryService.getSchemaManager());
            partition.setId(dn.getName());
            partition.setSuffixDn(dn);
            directoryService.addPartition(partition);
        }
    }

    /**
     * Factory for a fast (mostly in-memory-only) ApacheDS DirectoryService. Use only for tests!!
     */
    public static class InMemoryDirectoryServiceFactory implements DirectoryServiceFactory {

        private static final Logger LOG = LoggerFactory.getLogger(InMemoryDirectoryServiceFactory.class);

        private final DirectoryService directoryService;
        private final PartitionFactory partitionFactory;

        /**
         * Default constructor which creates {@link DefaultDirectoryService} instance and configures {@link AvlPartitionFactory}
         * as the {@link PartitionFactory} implementation.
         */
        public InMemoryDirectoryServiceFactory() {
            try {
                directoryService = new DefaultDirectoryService();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            directoryService.setShutdownHookEnabled(false);
            partitionFactory = new AvlPartitionFactory();
        }

        /**
         * Constructor which uses provided {@link DirectoryService} and {@link PartitionFactory} implementations.
         *
         * @param directoryService must be not-<code>null</code>
         * @param partitionFactory must be not-<code>null</code>
         */
        public InMemoryDirectoryServiceFactory(DirectoryService directoryService, PartitionFactory partitionFactory) {
            this.directoryService = directoryService;
            this.partitionFactory = partitionFactory;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void init(String name) throws Exception {
            if ((directoryService != null) && directoryService.isStarted()) {
                return;
            }

            directoryService.setInstanceId(name);

            // instance layout
            InstanceLayout instanceLayout = new InstanceLayout(System.getProperty("java.io.tmpdir") + "/server-work-" + name);
            if (instanceLayout.getInstanceDirectory().exists()) {
                try {
                    FileUtils.deleteDirectory(instanceLayout.getInstanceDirectory());
                } catch (IOException e) {
                    LOG.warn("couldn't delete the instance directory before initializing the DirectoryService", e);
                }
            }
            directoryService.setInstanceLayout(instanceLayout);

            // EhCache in disabled-like-mode
            Configuration ehCacheConfig = new Configuration();
            CacheConfiguration defaultCache = new CacheConfiguration("default", 1).eternal(false).timeToIdleSeconds(30)
                    .timeToLiveSeconds(30).overflowToDisk(false);
            ehCacheConfig.addDefaultCache(defaultCache);
            CacheService cacheService = new CacheService(new CacheManager(ehCacheConfig));
            directoryService.setCacheService(cacheService);

            // Init the schema
            // SchemaLoader loader = new SingleLdifSchemaLoader();
            SchemaLoader loader = new JarLdifSchemaLoader();
            SchemaManager schemaManager = new DefaultSchemaManager(loader);
            schemaManager.loadAllEnabled();
            ComparatorRegistry comparatorRegistry = schemaManager.getComparatorRegistry();
            for (LdapComparator<?> comparator : comparatorRegistry) {
                if (comparator instanceof NormalizingComparator) {
                    ((NormalizingComparator) comparator).setOnServer();
                }
            }
            directoryService.setSchemaManager(schemaManager);
            InMemorySchemaPartition inMemorySchemaPartition = new InMemorySchemaPartition(schemaManager);

            SchemaPartition schemaPartition = new SchemaPartition(schemaManager);
            schemaPartition.setWrappedPartition(inMemorySchemaPartition);
            directoryService.setSchemaPartition(schemaPartition);
            List<Throwable> errors = schemaManager.getErrors();
            if (!errors.isEmpty()) {
                throw new Exception(I18n.err(I18n.ERR_317, Exceptions.printErrors(errors)));
            }

            // Init system partition
            Partition systemPartition = partitionFactory.createPartition(directoryService.getSchemaManager(),
                    directoryService.getDnFactory(), "system", ServerDNConstants.SYSTEM_DN, 500,
                    new File(directoryService.getInstanceLayout().getPartitionsDirectory(), "system"));
            systemPartition.setSchemaManager(directoryService.getSchemaManager());
            partitionFactory.addIndex(systemPartition, SchemaConstants.OBJECT_CLASS_AT, 100);
            directoryService.setSystemPartition(systemPartition);

            directoryService.startup();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DirectoryService getDirectoryService() throws Exception {
            return directoryService;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PartitionFactory getPartitionFactory() throws Exception {
            return partitionFactory;
        }

    }

    /**
     * In-memory schema-only partition which loads the data in the similar way as the
     * {@link org.apache.directory.api.ldap.schemaloader.JarLdifSchemaLoader}.
     */
    public static class InMemorySchemaPartition extends AbstractLdifPartition {

        private static final Logger LOG = LoggerFactory.getLogger(InMemorySchemaPartition.class);

        /**
         * Filesystem path separator pattern, either forward slash or backslash. java.util.regex.Pattern is immutable so only
         * one instance is needed for all uses.
         */

        public InMemorySchemaPartition(SchemaManager schemaManager) {
            super(schemaManager);
        }

        /**
         * Partition initialization - loads schema entries from the files on classpath.
         *
         * @see org.apache.directory.server.core.partition.impl.avl.AvlPartition#doInit()
         */
        @Override
        protected void doInit() throws InvalidNameException, Exception {
            if (initialized) {
                return;
            }

            LOG.debug("Initializing schema partition " + getId());
            suffixDn.apply(schemaManager);
            super.doInit();

            // load schema
            final Map<String, Boolean> resMap = ResourceMap.getResources(Pattern.compile("schema[/\\Q\\\\E]ou=schema.*"));
            for (String resourcePath : new TreeSet<String>(resMap.keySet())) {
                if (resourcePath.endsWith(".ldif")) {
                    URL resource = DefaultSchemaLdifExtractor.getUniqueResource(resourcePath, "Schema LDIF file");
                    LdifReader reader = new LdifReader(resource.openStream());
                    LdifEntry ldifEntry = reader.next();
                    reader.close();

                    Entry entry = new DefaultEntry(schemaManager, ldifEntry.getEntry());
                    // add mandatory attributes
                    if (entry.get(SchemaConstants.ENTRY_CSN_AT) == null) {
                        entry.add(SchemaConstants.ENTRY_CSN_AT, defaultCSNFactory.newInstance().toString());
                    }
                    if (entry.get(SchemaConstants.ENTRY_UUID_AT) == null) {
                        entry.add(SchemaConstants.ENTRY_UUID_AT, UUID.randomUUID().toString());
                    }
                    AddOperationContext addContext = new AddOperationContext(null, entry);
                    super.add(addContext);
                }
            }
        }

    }

}
