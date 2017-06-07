package com.hazelcast.ocp.rest;

import com.hazelcast.ocp.command.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/maps")
public class MapOperations {

    @Autowired
    private MapService service;

    @RequestMapping("/random/{count}")
    public int randomPut(@PathVariable("count") String count){
        final int keyCount = Integer.valueOf(count);
        return service.insert(keyCount);
    }

    @RequestMapping("/stats")
    public int retrieveStats(){
        return service.stats();
    }

    @RequestMapping("/auto")
    public String runAutoPilot(){
        service.runAutoPilot();
        return "success";
    }

    @RequestMapping("/clear")
    public int clear(){
        service.clear();
        return service.stats();
    }

}
