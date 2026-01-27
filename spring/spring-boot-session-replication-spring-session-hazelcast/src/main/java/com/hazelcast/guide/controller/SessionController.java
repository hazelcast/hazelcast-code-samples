package com.hazelcast.guide.controller;

import com.hazelcast.spring.session.HazelcastIndexedSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class SessionController {

    private static final String PRINCIPAL_INDEX_NAME = HazelcastIndexedSessionRepository.PRINCIPAL_NAME_INDEX_NAME;
    private static final DateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss");

    /**
     * Alternatively you can use {@link HazelcastIndexedSessionRepository} directly, then you need to define
     * a bean of this type in {@code com.hazelcast.guide.config.SessionConfiguration}.
     */
    final FindByIndexNameSessionRepository<?> sessionRepository;

    public SessionController(FindByIndexNameSessionRepository<?> sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Creates a session for the request if there is no session of the request.
     *
     * @param principal Principal value of the session to be created
     * @return Message indicating the session creation or abortion result.
     *
     */
    @GetMapping("/create")
    public String createSession(@RequestParam("principal") String principal, HttpServletRequest request,
                                HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession();
            session.setAttribute(PRINCIPAL_INDEX_NAME, principal);
            return "Session created: " + session.getId();
        } else {
            return "Session already exists: " + session.getId();
        }
    }

    /**
     * Lists all the sessions with the same {@link #PRINCIPAL_INDEX_NAME} of the request's session.
     *
     * @return All sessions associated with this session's {@link #PRINCIPAL_INDEX_NAME}.
     */
    @GetMapping(value = "/list", produces = MediaType.TEXT_HTML_VALUE)
    public String listSessionsByPrincipal(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "<html>No session found.</html>";
        }
        String principal = (String) session.getAttribute(PRINCIPAL_INDEX_NAME);
        Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(principal);
        return toHtmlTable(sessions.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey().substring(0, 8),
                e -> "Principal: " + session.getAttribute(PRINCIPAL_INDEX_NAME))
        ));
    }

    /**
     * Returns the current session's details if the request has a session.
     *
     * @return Session details
     */
    @GetMapping(value = "/info", produces = MediaType.TEXT_HTML_VALUE)
    public String getSessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "<html>No session found.</html>";
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("sessionId", session.getId());
        attributes.put("principal", session.getAttribute(PRINCIPAL_INDEX_NAME));
        attributes.put("created", FORMATTER.format(new Date(session.getCreationTime())));
        attributes.put("last accessed", FORMATTER.format(new Date(session.getLastAccessedTime())));
        return toHtmlTable(attributes);
    }

    private String toHtmlTable(Map<String, Object> attributes) {
        String html = """
                <html>
                    <body>
                        <table style="border-spacing: 5px; border: 1px solid black;">
                        %s
                        </table>
                    </body>
                </html>
                """;
        String rows = attributes.entrySet().stream()
                                .map(e -> addHtmlTableRow(e.getKey(), e.getValue()))
                                .collect(Collectors.joining("\n"));
        return html.formatted(rows);
    }

    private String addHtmlTableRow(String key, Object value) {
        return """
                <tr>
                    <td style="padding: 5px;">%s</td>
                    <td style="padding: 5px;">%s</td>
                </tr>
                """.formatted(key, value);
    }

}
