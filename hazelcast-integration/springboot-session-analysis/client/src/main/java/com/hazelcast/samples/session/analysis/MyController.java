package com.hazelcast.samples.session.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

/**
 * <p><i>Model-View-Controller</i> logic.
 * </p>
 * <p>The two methods here are invoked by HTML actions. The "{@code checkout}" action
 * triggers the {@link #checkout()} method which displays the "{@code checkout.html}"
 * page.
 * </p>
 */
@Controller
@Slf4j
public class MyController {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    /**
     * <p>The business logic for the main page of the shop.
     * </p>
     * <p>This does three things:
     * <ol>
     * <li><b>Initialise</b>
     * <p>The session stores two objects, the user's browser and their basket of items
     * to buy. Ensure these are created if not already present.</p>
     * </li>
     * <li><b>Basket Update</b>
     * <p>If the page has been invoked from a form (the {@code add} button in the HTML table),
     * update the basket with the item added</p>
     * </li>
     * <li><b>Add page variables</b>
     * <p>Add the available stock to the page variables to that {@code index.html} can
     * display them.</p>
     * </li>
     * </ol>
     *
     * @param httpSession To augment
     * @param httpServletRequest To extract any form parameters
     * @return The page to show, {@code index.html}
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/")
    public ModelAndView index(HttpSession httpSession, HttpServletRequest httpServletRequest) {
        log.info("index(), session={}", httpSession.getId());

        // 1. Initialise the session with info
        if (httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_BASKET) == null) {
                httpSession.setAttribute(Constants.SESSION_ATTRIBUTE_BASKET, new HashMap<String, Integer>());
        }
        if (httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_BROWSER) == null) {
                String userAgent = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);
                log.trace("{}=={}", HttpHeaders.USER_AGENT, userAgent);
                httpSession.setAttribute(Constants.SESSION_ATTRIBUTE_BROWSER, (userAgent == null ? "null" : userAgent));
        }

        // 2. Basket update if page requested from form
        String key = httpServletRequest.getParameter(Constants.HTML_ACTION_ADD);
        if (key != null) {
                Map<String, Integer> basket =
                        (Map<String, Integer>) httpSession.getAttribute(Constants.SESSION_ATTRIBUTE_BASKET);
                Integer value = basket.get(key);
                basket.put(key, (value == null ? 1 : value + 1));
                httpSession.setAttribute(Constants.SESSION_ATTRIBUTE_BASKET, basket);
        }

        // 3. Add stock info to the page and return it
        ModelAndView modelAndView = new ModelAndView(Constants.HTML_ACTION_INDEX);

        Map<String, Integer> stockMap = this.hazelcastInstance.getMap(Constants.IMAP_NAME_STOCK);
        modelAndView.addObject(Constants.IMAP_NAME_STOCK, new TreeMap<String, Integer>(stockMap));

        return modelAndView;
    }


    /**
     * <p>The "business logic" for processing the checkout action.
     * </p>
     * <p>For real we would want to collect money and dispatch the stock
     * somehow. For now we'll skip that and simply end the session
     * so the next interaction starts afresh.
     * </p>
     *
     * @param httpSession To clear
     * @return The page to show, {@code checkout.html}
     */
    @GetMapping("/" + Constants.HTML_ACTION_CHECKOUT)
    public ModelAndView checkout(HttpSession httpSession) {
        log.info("checkout(), session={}", httpSession.getId());
        httpSession.invalidate();
        return new ModelAndView(Constants.HTML_ACTION_CHECKOUT);
    }
}
