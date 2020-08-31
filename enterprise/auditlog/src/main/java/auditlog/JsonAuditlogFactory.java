package auditlog;

import java.util.Properties;

import javax.security.auth.callback.CallbackHandler;

import com.hazelcast.auditlog.AuditlogService;
import com.hazelcast.auditlog.AuditlogServiceFactory;

/**
 * Factory class responsible for creating {@link JsonAuditlogService}.
 */
public class JsonAuditlogFactory implements AuditlogServiceFactory {

    public static final String PROPERTY_JSON_FILE = "outputFile";

    private String outputFile;

    @Override
    public void init(CallbackHandler callbackHandler, Properties properties) throws Exception {
        outputFile = properties.getProperty(PROPERTY_JSON_FILE);
    }

    @Override
    public AuditlogService createAuditlog() throws Exception {
        return new JsonAuditlogService(outputFile);
    }

}
