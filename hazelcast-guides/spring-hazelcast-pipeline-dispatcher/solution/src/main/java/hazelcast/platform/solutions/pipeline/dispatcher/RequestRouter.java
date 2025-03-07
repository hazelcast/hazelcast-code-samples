package hazelcast.platform.solutions.pipeline.dispatcher;

/**
 * The RequestRouter is responsible for actually selecting the map that will receive the request.
 * This abstraction enables the possibility of sending to different maps and therefore different service implementation
 * Pipelines, based on configuration.
 * <p>
 * Note that "getRequestMapName" should be called every time a request is dispatched since it is possible that
 * consecutive requests are routed to different versions.
 */
public interface RequestRouter{
    String getRequestMapName();
}
