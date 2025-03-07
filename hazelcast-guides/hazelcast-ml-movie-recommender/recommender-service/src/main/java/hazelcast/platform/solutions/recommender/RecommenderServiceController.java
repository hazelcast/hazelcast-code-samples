package hazelcast.platform.solutions.recommender;

//import hazelcast.platform.solutions.pipeline.dispatcher.PipelineDispatcherFactory;
//import org.springframework.beans.factory.annotation.Autowired;
import hazelcast.platform.solutions.pipeline.dispatcher.PipelineDispatcherFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommenderServiceController {
    @Autowired
    PipelineDispatcherFactory pipelineDispatcherFactory;

    @GetMapping("/recommendations")
    public DeferredResult<List<Recommendation>> getRecommendations(@RequestParam String like){
        return pipelineDispatcherFactory.<String,List<Recommendation>>dispatcherFor("recommendation").send(like);
    }

    private static final ArrayList<Recommendation> dummyRecommendations = new ArrayList<>();

    static {
        dummyRecommendations.add(new Recommendation("GoldenEye","https://images-na.ssl-images-amazon.com/images/M/MV5BMzk2OTg4MTk1NF5BMl5BanBnXkFtZTcwNjExNTgzNA@@..jpg"));
        dummyRecommendations.add(new Recommendation("Desperado","https://images-na.ssl-images-amazon.com/images/M/MV5BYjA0NDMyYTgtMDgxOC00NGE0LWJkOTQtNDRjMjEzZmU0ZTQ3XkEyXkFqcGdeQXVyMTQxNzMzNDI@..jpg"));
        dummyRecommendations.add(new Recommendation("Four Rooms","https://images-na.ssl-images-amazon.com/images/M/MV5BNDc3Y2YwMjUtYzlkMi00MTljLTg1ZGMtYzUwODljZTI1OTZjXkEyXkFqcGdeQXVyMTQxNzMzNDI@..jpg"));
        dummyRecommendations.add(new Recommendation("Mad Love","https://images-na.ssl-images-amazon.com/images/M/MV5BNDE0NTQ1NjQzM15BMl5BanBnXkFtZTYwNDI4MDU5..jpg"));
        dummyRecommendations.add(new Recommendation("The Aristocats","https://images-na.ssl-images-amazon.com/images/M/MV5BMTU1MzM0MjcxMF5BMl5BanBnXkFtZTgwODQ0MzcxMTE@..jpg"));
        dummyRecommendations.add(new Recommendation("Life of Brian","https://images-na.ssl-images-amazon.com/images/M/MV5BMzAwNjU1OTktYjY3Mi00NDY5LWFlZWUtZjhjNGE0OTkwZDkwXkEyXkFqcGdeQXVyMTQxNzMzNDI@..jpg"));
    }

}
