package auditlog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hazelcast.auditlog.AuditableEvent;
import com.hazelcast.auditlog.AuditlogService;
import com.hazelcast.auditlog.EventBuilder;
import com.hazelcast.auditlog.Level;

/**
 * Custom {@link AuditlogService} implementation which writes events formatted as JSON Strings into a file (or standard output
 * stream).
 */
public class JsonAuditlogService implements AuditlogService {

    private final Gson gson;
    private final File outputFile;

    public JsonAuditlogService(String outputPath) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        File tmpFile = null;
        if (outputPath == null || "-".equals(outputPath)) {
            gsonBuilder.setPrettyPrinting();
        } else {
            tmpFile = new File(outputPath);
        }
        this.gson = gsonBuilder.create();
        this.outputFile = tmpFile;
    }

    @Override
    public void log(AuditableEvent auditableEvent) {
        String json = gson.toJson(auditableEvent);
        if (outputFile != null) {
            try (PrintStream ps = new PrintStream(new FileOutputStream(outputFile, true))) {
                ps.println(json);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(json);
        }
    }

    @Override
    public void log(String eventTypeId, Level level, String message) {
        log(eventTypeId, level, message, null);
    }

    @Override
    public void log(String eventTypeId, Level level, String message, Throwable thrown) {
        log(eventBuilder(eventTypeId).level(level).message(message).build());
    }

    @Override
    public EventBuilder<?> eventBuilder(String typeId) {
        return Event.builder(typeId, this);
    }
}
