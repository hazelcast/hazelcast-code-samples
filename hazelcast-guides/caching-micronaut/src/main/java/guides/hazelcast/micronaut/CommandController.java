package guides.hazelcast.micronaut;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;

@Controller
public class CommandController {


    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @Get("/put")
    public CommandResponse put(@QueryValue("key") String key, @QueryValue("value") String value) {
        commandService.put(key, value);
        return new CommandResponse(value);
    }

    @Get("/get")
    public CommandResponse get(@QueryValue("key") String key) {
        String value = commandService.get(key);
        return new CommandResponse(value);
    }


}
