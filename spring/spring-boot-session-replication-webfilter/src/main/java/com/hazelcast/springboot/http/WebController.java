package com.hazelcast.springboot.http;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class WebController {

    @ModelAttribute("sessionId")
    public String sessionId(final HttpSession session) {
        return session.getId();
    }

    @RequestMapping(value = "/")
    public String index(HttpSession httpSession) {
        Integer hits = (Integer) httpSession.getAttribute("hits");
        if (hits == null) {
            hits = 0;
        }
        httpSession.setAttribute("hits", ++hits);

        return "index";
    }

}
