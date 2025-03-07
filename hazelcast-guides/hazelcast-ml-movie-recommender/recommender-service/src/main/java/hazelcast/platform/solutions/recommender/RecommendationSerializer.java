package hazelcast.platform.solutions.recommender;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;

public class RecommendationSerializer implements CompactSerializer<Recommendation> {
    @Override
    public Recommendation read(CompactReader reader) {
        Recommendation recommendation = new Recommendation();
        recommendation.setTitle(reader.readString("title"));
        recommendation.setImage_url(reader.readString("image_url"));
        return recommendation;
    }

    @Override
    public void write(CompactWriter writer, Recommendation recommendation) {
        writer.writeString("title", recommendation.getTitle());
        writer.writeString("image_url", recommendation.getImage_url());
    }

    @Override
    public String getTypeName() {
        return "recommendation";
    }

    @Override
    public Class<Recommendation> getCompactClass() {
        return Recommendation.class;
    }
}
