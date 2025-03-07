package hazelcast.platform.labs.jet.connectors;

import com.hazelcast.jet.datamodel.Tuple2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Watches a directory for new files.
 */
public class DirectoryWatcher implements AutoCloseable {
    private final Logger log = LogManager.getLogger();
    private final WatchService watchService;
    private final String suffix;

    /*
     * Build a directory watcher passing the directory to be watched and, optionally, a suffix of interest
     */
    public DirectoryWatcher(String dir, String suffix){
        this.suffix = suffix;
        try {
            Path watchedPath = Paths.get(dir);
            if (!watchedPath.toFile().isDirectory()){
                log.error("Directory watcher was initialized with a non-existent path: " + watchedPath);
                throw new RuntimeException("Directory watcher initialization failed");
            }
            watchService = FileSystems.getDefault().newWatchService();
            watchedPath.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);

        } catch (IOException e) {
            log.error("An exception occurred while creating a WatchService instance", e);
            throw new RuntimeException(e);
        }
    }

    /*
     * may return an empty list if no events are ready
     * will not return null
     */
    public List<Tuple2<EventType, String>> poll() {
        WatchKey k = watchService.poll();
        if (k == null) return new ArrayList<>(); // RETURN

        List<WatchEvent<?>> watchEvents = k.pollEvents();
        ArrayList<Tuple2<EventType, String>> result = new ArrayList<>(watchEvents.size());

        for(WatchEvent<?> event: watchEvents){
            // skip events that don't match the suffix (if provided)
            if (suffix != null){
                if (!event.context().toString().toLowerCase().endsWith(suffix.toLowerCase())) continue; // CONTINUE
            }
            result.add(translateWatchEvent((WatchEvent<Path>) event));
        }
        k.reset();
        return result;
    }

    public void close(){
        try {
            watchService.close();
        } catch (IOException e) {
            log.warn("An exception occurred while closing the WatchService", e);
            throw new RuntimeException(e);
        }
    }

    private Tuple2<EventType, String> translateWatchEvent(WatchEvent<Path> watchEvent){
        EventType type = EventType.CREATE;
        if (watchEvent.kind() != StandardWatchEventKinds.ENTRY_CREATE){
           if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
               type = EventType.DELETE;
           } else if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_MODIFY){
               type = EventType.UPDATE;
           } else {
               log.warn("Received a watch event with an unkown type: " + watchEvent.kind());
               type = EventType.UNKNOWN;
           }
        }

        return Tuple2.tuple2(type, watchEvent.context().toString());
    }

    public enum EventType {CREATE, UPDATE, DELETE, UNKNOWN}
}
