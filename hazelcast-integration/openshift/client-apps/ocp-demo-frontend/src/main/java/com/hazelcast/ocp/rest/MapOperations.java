package com.hazelcast.ocp.rest;

import com.hazelcast.ocp.command.MapService;
import com.hazelcast.ocp.entryprocessor.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/maps")
@Slf4j
public class MapOperations {

    @Autowired
    private MapService service;

    @RequestMapping("/random/{count}")
    public int randomPut(@PathVariable("count") String count) {
        final int keyCount = Integer.valueOf(count);
        return service.insert(keyCount);
    }

    @RequestMapping("/random/position/{count}")
    public int randomPositions(@PathVariable("count") String count) {
        final int keyCount = Integer.valueOf(count);

        return service.insertPositions(keyCount);
    }

    @PostMapping("/entry/processor/distance")
    public long processDistances(@RequestBody Position position) {
        return service.processDistances(position);
    }

    @RequestMapping("/stats")
    public int retrieveStats() {
        return service.stats();
    }

    @RequestMapping("/auto")
    public String runAutoPilot() {
        service.runAutoPilot();
        return "success";
    }

    @RequestMapping("/clear")
    public int clear() {
        service.clear();
        return service.stats();
    }
}
