package guides.hazelcast.springboot;

import com.hazelcast.map.IMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.requireNonNull;

@RestController
public class CommandController {

    private final IMap<String, String> keyValueMap;

    public CommandController(IMap<String, String> keyValueMap) {
        this.keyValueMap = requireNonNull(keyValueMap);
    }

    @PostMapping("/put")
    public CommandResponse put(@RequestParam(value = "key") String key, @RequestParam(value = "value") String value) {
        keyValueMap.put(key, value);
        return new CommandResponse(value);
    }

    @GetMapping("/get")
    public CommandResponse get(@RequestParam(value = "key") String key) {
        String value = keyValueMap.get(key);
        return new CommandResponse(value);
    }
}
