package hazelcast.platform.labs.machineshop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class MachineEmulator implements  Runnable, Callback {
    private final String serialNum;

    private final KafkaProducer<String, String> kafkaProducer;
    private final String kafkaTopic;
    private int t;
    private SignalGenerator signalGenerator;

    private MachineStatusEvent currStatus;

    private final ObjectMapper mapper;

    public MachineEmulator(KafkaProducer<String,String> producer, String topic, String sn, SignalGenerator signalGenerator){
        this.kafkaProducer = producer;
        this.kafkaTopic = topic;
        this.signalGenerator = signalGenerator;
        this.serialNum = sn;
        this.t = 0;
        this.mapper = new ObjectMapper();
    }

    public  synchronized void setSignalGenerator(SignalGenerator signalGenerator){
        this.signalGenerator = signalGenerator;
        this.t = 0;
    }
    @Override
    public synchronized void  run() {
        currStatus = new MachineStatusEvent();
        currStatus.setSerialNum(serialNum);
        currStatus.setEventTime(System.currentTimeMillis());
        currStatus.setBitTemp(signalGenerator.compute(t++));
        currStatus.setBitRPM(10000);
        currStatus.setBitPositionX(0);
        currStatus.setBitPositionY(0);
        currStatus.setBitPositionZ(0);

        try {
            String payload = mapper.writeValueAsString(currStatus);
            kafkaProducer.send(new ProducerRecord<>(kafkaTopic, serialNum, payload), this);
        } catch(JsonProcessingException jpx){
            System.out.println("WARNING: could not convert event to json: " + jpx.getMessage());
        }
    }

    public MachineStatusEvent getCurrStatus(){
        return currStatus;
    }

    public String getSerialNum() { return serialNum;}

    @Override
    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        if (e != null) {
            System.out.println("ERROR: send failed.");
            e.printStackTrace(System.out);
        }
    }
}
