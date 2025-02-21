package sample;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.hazelcast.logging.Logger;
import com.hazelcast.security.CertificatesCallback;
import com.hazelcast.security.ClusterEndpointPrincipal;
import com.hazelcast.security.ClusterIdentityPrincipal;
import com.hazelcast.security.ClusterRolePrincipal;
import com.hazelcast.security.EndpointCallback;
import com.hazelcast.security.HazelcastPrincipal;

/**
 * This is a sample Hazelcast login module which authenticates and assign roles based on information in client TLS certificate
 * (X.509). It can be only used when TLS with mutual authentication is enabled.
 * <p>
 * Example SSL configuration:
 *
 * <pre>{@code

<ssl enabled="true">
   <properties>
      <property name="mutualAuthentication">REQUIRED</property>
      <property name="keyStore">server.p12</property>
      <property name="trustStorePassword">123456</property>
      <property name="trustStore">ca.p12</property>
      <property name="keyStorePassword">123456</property>
   </properties>
</ssl>

 * }</pre>
 *
 * Sample login module configuration:
 *
 * <pre>{@code

<realms>
   <realm name="clientRealm">
        <authentication>
             <jaas>
                  <login-module class-name="sample.ClientCertCheckingLoginModule" usage="REQUIRED">
                       <properties>
                            <property name="checkedAttribute">cn</property>
                            <property name="allowedAttributeValues">client,client35,client87</property>
                            <property name="roleAttribute">ou</property>
                       </properties>
                  </login-module>
             </jaas>
        </authentication>
   </realm>
   <realm name="memberRealm">
        <authentication>
             <jaas>
                  <login-module class-name="sample.ClientCertCheckingLoginModule" usage="REQUIRED">
                       <properties>
                            <property name="allowedSanValues">server.my-company.com</property>
                       </properties>
                  </login-module>
             </jaas>
        </authentication>
   </realm>
</realms>

 * }</pre>
 */
public class ClientCertCheckingLoginModule implements LoginModule {

    public static final String OPTION_CHECKED_ATTRIBUTE = "checkedAttribute";
    public static final String OPTION_CHECKED_ATTRIBUTE_DEFAULT = "cn";

    public static final String OPTION_ROLE_ATTRIBUTE = "roleAttribute";
    public static final String OPTION_ROLE_ATTRIBUTE_DEFAULT = null;

    public static final String OPTION_ALLOWED_ATTRIBUTE_VALUES = "allowedAttributeValues";

    public static final String OPTION_ALLOWED_SAN_VALUES = "allowedSanValues";

    public static final String OPTION_SEPARATOR = "separator";
    public static final String OPTION_SEPARATOR_DEFAULT = ",";

    private static final Integer GENERAL_NAME_EMAIL = new Integer(1);
    private static final Integer GENERAL_NAME_DNS = new Integer(2);
    private static final List<Integer> SUPPORTED_GENERAL_NAMES = asList(GENERAL_NAME_DNS, GENERAL_NAME_EMAIL);

    protected String endpoint;
    protected Subject subject;
    protected Map<String, ?> options;
    protected CallbackHandler callbackHandler;

    private Set<String> assignedRoles = new HashSet<>();
    private String name;

    @Override
    public final void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.options = options;
    }

    @Override
    @SuppressWarnings("checkstyle:npathcomplexity")
    public final boolean login() throws LoginException {
        String checkedAttribute = getStringOption(OPTION_CHECKED_ATTRIBUTE, OPTION_CHECKED_ATTRIBUTE_DEFAULT);
        String roleAttribute = getStringOption(OPTION_ROLE_ATTRIBUTE, OPTION_ROLE_ATTRIBUTE_DEFAULT);
        String allowedAttributeValues = getStringOption(OPTION_ALLOWED_ATTRIBUTE_VALUES, "");
        String allowedSanValues = getStringOption(OPTION_ALLOWED_SAN_VALUES, "").toLowerCase(Locale.ROOT);
        String separator = getStringOption(OPTION_SEPARATOR, OPTION_SEPARATOR_DEFAULT);

        Set<String> allowedAttrValuesList = splitAndFilterEmpty(allowedAttributeValues, separator);
        Set<String> allowedSanValuesList = splitAndFilterEmpty(allowedSanValues, separator);

        if (allowedAttrValuesList.isEmpty() && allowedSanValuesList.isEmpty()) {
            throw new FailedLoginException("Allowed values were configured neither for X.509 certificate subject attribute ("
                    + checkedAttribute + ") nor for a Subject alternative name (SAN).");
        }

        CertificatesCallback cb = new CertificatesCallback();
        EndpointCallback ecb = new EndpointCallback();
        try {
            callbackHandler.handle(new Callback[] { cb, ecb });
        } catch (IOException | UnsupportedCallbackException e) {
            throw new FailedLoginException("Unable to retrieve Certificates. " + e.getMessage());
        }
        endpoint = ecb.getEndpoint();
        Certificate[] certs = cb.getCertificates();
        if (certs == null || certs.length == 0 || !(certs[0] instanceof X509Certificate)) {
            throw new FailedLoginException("No valid X.509 client certificate found");
        }
        X509Certificate clientCert = (X509Certificate) certs[0];
        Collection<List<?>> sans = null;
        try {
            sans = clientCert.getSubjectAlternativeNames();
        } catch (CertificateParsingException e) {
            Logger.getLogger(getClass()).warning("Unable to get SANs from the client certificate", e);
        }
        if (sans == null) {
            sans = Collections.emptyList();
        }
        try {
            name = clientCert.getSubjectX500Principal().getName();
            Collection<String> attrValues = getAttributeValues(clientCert, checkedAttribute);
            boolean found =
                    // either an expected value is in Subject name's attribute
                    attrValues.stream().anyMatch(s -> allowedAttrValuesList.contains(s))
                            // or we try to find an expected value in Subject alternative names
                            || sans.stream()
                                    // filter out SAN types other than DNS or EMAIL
                                    .filter(san -> SUPPORTED_GENERAL_NAMES.contains(san.get(0)))
                                    // use the SAN value
                                    .map(san -> (String) san.get(1))
                                    // we work with lower-case values as both domain names and emails are case insensitive
                                    .map(s -> s.toLowerCase(Locale.ROOT))
                                    // we want at least one value to be matched
                                    .anyMatch(s -> allowedSanValuesList.contains(s));
            if (!found) {
                throw new FailedLoginException("The X.509 certificate is not allowed for authentication.");
            }
            assignedRoles.addAll(getAttributeValues(clientCert, roleAttribute));
        } catch (NamingException e) {
            throw new FailedLoginException(e.getMessage());
        }
        return true;
    }

    @Override
    public final boolean commit() throws LoginException {
        Set<Principal> principals = subject.getPrincipals();
        principals.add(new ClusterIdentityPrincipal(name));
        principals.add(new ClusterEndpointPrincipal(endpoint));
        for (String role : assignedRoles) {
            principals.add(new ClusterRolePrincipal(role));
        }
        return true;
    }

    @Override
    public final boolean abort() throws LoginException {
        clearSubject();
        return true;
    }

    @Override
    public final boolean logout() throws LoginException {
        clearSubject();
        return true;
    }

    private static Collection<String> getAttributeValues(X509Certificate cert, String attribute) throws NamingException {
        if (attribute == null || attribute.isEmpty()) {
            return Collections.emptySet();
        }
        LdapName ldapName = new LdapName(cert.getSubjectX500Principal().getName());
        Set<String> values = new HashSet<String>();
        for (Rdn rdn : ldapName.getRdns()) {
            values.addAll(getAttributeValues(rdn.toAttributes(), attribute));
        }
        return values;
    }

    private static Collection<String> getAttributeValues(Attributes attributes, String attribute) throws NamingException {
        Set<String> names = new HashSet<String>();
        Attribute attr = attribute != null && attributes != null ? attributes.get(attribute) : null;
        if (attr != null) {
            NamingEnumeration<?> values = attr.getAll();
            while (values.hasMore()) {
                Object value = values.next();
                if (value != null) {
                    String name = (value instanceof byte[]) ? new String((byte[]) value, StandardCharsets.UTF_8)
                            : value.toString();
                    names.add(name);
                }
            }
        }
        return names;
    }

    protected String getStringOption(String optionName, String defaultValue) {
        String option = getOptionInternal(optionName);
        return option != null ? option : defaultValue;
    }

    private String getOptionInternal(String optionName) {
        if (options == null) {
            return null;
        }
        Object option = options.get(optionName);
        return option != null ? option.toString() : null;
    }

    private void clearSubject() {
        for (Iterator<Principal> it = subject.getPrincipals().iterator(); it.hasNext();) {
            if (it.next() instanceof HazelcastPrincipal) {
                it.remove();
            }
        }
    }

    private Set<String> splitAndFilterEmpty(String value, String separator) {
        if (value == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(value.split(separator)).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }
}
