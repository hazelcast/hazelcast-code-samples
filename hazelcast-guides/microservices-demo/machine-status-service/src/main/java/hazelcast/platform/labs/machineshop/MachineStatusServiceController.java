package hazelcast.platform.labs.machineshop;

import hazelcast.platform.labs.machineshop.domain.MachineStatusSummary;
import hazelcast.platform.solutions.pipeline.dispatcher.PipelineDispatcherFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class MachineStatusServiceController {

    @Autowired
    PipelineDispatcherFactory dispatcherFactory;
    @GetMapping("/machinestatus")
    public DeferredResult<MachineStatusSummary> getStatus(@RequestParam String sn){
        return dispatcherFactory.<String,MachineStatusSummary>dispatcherFor("machinestatus")
                .send(sn);
    }
}
