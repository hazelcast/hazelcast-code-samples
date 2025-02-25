package com.hazelcast.guide.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.session.Session;
import org.springframework.session.hazelcast.Hazelcast4IndexedSessionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class SessionController {

    private static final String principalIndexName = Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_INDEX_NAME;
    private static final DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    Hazelcast4IndexedSessionRepository sessionRepository;

    /**
     * Creates a session for the request if there is no session of the request.
     *
     * @param principal Principal value of the session to be created
     * @return Message indicating the session creation or abortion result.
     *
     */
    @GetMapping("/create")
    public String createSession(@RequestParam("principal") String principal, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession();
            session.setAttribute(principalIndexName, principal);
            return "Session created: " + session.getId();
        } else {
            return "Session already exists: " + session.getId();
        }
    }

    /**
     * Lists all the sessions with the same {@link #principalIndexName} of the request's session.
     *
     * @return All sessions associated with this session's {@link #principalIndexName}.
     */
    @GetMapping(value = "/list", produces = MediaType.TEXT_HTML_VALUE)
    public String listSessionsByPrincipal(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "<html>No session found.</html>";
        }
        String principal = (String) session.getAttribute(principalIndexName);
        Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(principal);
        return toHtmlTable(sessions.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey().substring(0, 8),
                e -> "Principal: " + session.getAttribute(principalIndexName))
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
        Map<String, Object> attributes = new LinkedHashMap();
        attributes.put("sessionId", session.getId());
        attributes.put("principal", session.getAttribute(principalIndexName));
        attributes.put("created", formatter.format(new Date(session.getCreationTime())));
        attributes.put("last accessed", formatter.format(new Date(session.getLastAccessedTime())));
        return toHtmlTable(attributes);
    }

    private String toHtmlTable(Map<String, Object> attributes) {
        StringBuilder html = new StringBuilder("<html>");
        html.append("<table border=\"1\" cellpadding=\"5\" cellspacing=\"5\">");
        attributes.forEach((k, v) -> addHtmlTableRow(html, k, v));
        html.append("</table></html>");
        return html.toString();
    }

    private void addHtmlTableRow(StringBuilder content, String key, Object value) {
        content.append("<tr>")
                    .append("<th>").append(key).append("</th>")
                    .append("<td>").append(value).append("</td>")
                .append("</tr>");
    }

}
