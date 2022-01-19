package com.hazelcast.samples.rbac;

import static com.hazelcast.config.OnJoinPermissionOperationName.SEND;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;

/**
 * The executable ReplacePermissions class takes a XML-snippet file with the Hazelcast client permission configuration as an
 * argument. A new Hazelcast litemember is started to broadcast the new permissions. After the member joins successfully the
 * cluster it's stopped.
 */

public final class ReplacePermissions {
    private final File permissionsFile;

    private ReplacePermissions(String permissionFile) {
        permissionsFile = new File(requireNonNull(permissionFile));
        if (!permissionsFile.isFile()) {
            System.err.println("Unable to find the permission file " + permissionFile);
            System.exit(2);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Unexpected number of arguments.");
            System.err.println();
            System.err.println("Usage:");
            System.err.println("\tjava ReplacePermissions newPermissions.xml");
            System.err.println();
            System.exit(1);
        }
        ReplacePermissions adminClient = new ReplacePermissions(args[0]);
        try {
            adminClient.changePerms();
        } catch (FileNotFoundException e) {
            System.err.println("Unable to replace permissions.");
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void changePerms() throws FileNotFoundException {
        Config config = Server.config();
        try {
            String xml = "<hazelcast xmlns='http://www.hazelcast.com/schema/config'><security enabled='true'>\n"
                    + new String(readAllBytes(permissionsFile.toPath()), UTF_8) + "</security></hazelcast>\n";
            Config customSecurity = new XmlConfigBuilder(new ByteArrayInputStream(xml.getBytes(UTF_8))).build();
            config.setSecurityConfig(customSecurity.getSecurityConfig());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to create Hazelcast configuration.");
            System.exit(3);
        }
        config.setLiteMember(true);
        config.getSecurityConfig().setOnJoinPermissionOperation(SEND);
        Hazelcast.newHazelcastInstance(config).shutdown();
    }

}
