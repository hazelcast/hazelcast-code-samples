package auditlog;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import com.hazelcast.auditlog.AuditableEvent;
import com.hazelcast.auditlog.AuditlogService;
import com.hazelcast.auditlog.EventBuilder;
import com.hazelcast.auditlog.Level;

/**
 * Simple {@link AuditableEvent} implementation.
 */
public final class Event implements AuditableEvent {

    private final String message;
    private final String typeId;
    private final Map<String, Object> parameters;
    private final Level level;
    private final Throwable cause;
    private final long timestamp;

    private Event(Builder builder) {
        this.message = builder.message;
        this.typeId = builder.typeId;
        this.parameters = builder.parameters;
        this.level = builder.level;
        this.cause = builder.cause;
        this.timestamp = builder.timestamp;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String typeId() {
        return typeId;
    }

    @Override
    public Map<String, Object> parameters() {
        return parameters;
    }

    @Override
    public Level level() {
        return level;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "SimpleEvent [typeId=" + typeId + ", message=" + message + ", timestamp=" + timestamp + ", level=" + level
                + ", parameters=" + parameters + ", cause=" + cause + "]";
    }

    /**
     * Creates builder to build {@link Event}.
     * @return created builder
     */
    public static Builder builder(String typeId, AuditlogService els) {
        return new Builder(typeId, els);
    }

    /**
     * Builder to build {@link Event events}.
     */
    public static final class Builder implements EventBuilder<Builder> {
        private String message;
        private String typeId;
        private Map<String, Object> parameters = new HashMap<>();
        private Level level = Level.INFO;
        private Throwable cause;
        private long timestamp;
        private AuditlogService auditlogService;

        private Builder(String typeId, AuditlogService els) {
            this.typeId = requireNonNull(typeId, "Event type identifier has to be provided.");
            this.auditlogService = els;
        }

        @Override
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        @Override
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = requireNonNull(parameters);
            return this;
        }

        @Override
        public Builder addParameter(String key, Object value) {
            parameters.put(key, value);
            return this;
        }

        @Override
        public Builder level(Level level) {
            this.level = requireNonNull(level, "Event importance level has to be provided.");
            return this;
        }

        @Override
        public Builder cause(Throwable throwable) {
            this.cause = throwable;
            return this;
        }

        @Override
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        @Override
        public AuditableEvent build() {
            if (timestamp <= 0) {
                timestamp = System.currentTimeMillis();
            }
            return new Event(this);
        }

        @Override
        public void log() {
            if (auditlogService != null) {
                auditlogService.log(build());
            }
        }

    }
}
