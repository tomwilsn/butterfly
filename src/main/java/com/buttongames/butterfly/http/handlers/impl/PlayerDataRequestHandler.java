package com.buttongames.butterfly.http.handlers.impl;

import com.buttongames.butterfly.http.exception.UnsupportedRequestException;
import com.buttongames.butterfly.http.handlers.BaseRequestHandler;
import com.buttongames.butterfly.xml.KXmlBuilder;
import com.buttongames.butterfly.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import spark.Request;
import spark.Response;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handler for any requests that come to the <code>playerdata</code> module.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class PlayerDataRequestHandler extends BaseRequestHandler {

    private final Logger LOG = LogManager.getLogger(PcbEventRequestHandler.class);

    /**
     * Since this response is static (as far as I know right now...), let's just cache it in memory. It's a little large.
     */
    private static KXmlBuilder EVENTS_RESPONSE_2018042300;

    static {
        try {
            final Path events20180423Path = Paths.get(ClassLoader.getSystemResource("static_responses/mdx_2018042300/events.xml").toURI());
            EVENTS_RESPONSE_2018042300 = KXmlBuilder.parse(new String(Files.readAllBytes(events20180423Path), StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Handles an incoming request for the <code>playerdata</code> module.
     * @param requestBody The XML document of the incoming request.
     * @param request The Spark request
     * @param response The Spark response
     * @return A response object for Spark
     */
    @Override
    public Object handleRequest(final Element requestBody, final Request request, final Response response) {
        final String requestMethod = request.attribute("method");

        if (requestMethod.equals("usergamedata_advanced")) {
            // figure out which kind of usergamedata_advanced request this is
            final String mode = XmlUtils.strValueAtPath(requestBody, "/playerdata/data/mode");
            final String refid = XmlUtils.strValueAtPath(requestBody, "/playerdata/data/refid");
            final String dataid = XmlUtils.strValueAtPath(requestBody, "/playerdata/data/dataid");

            if (mode.equals("userload")) {
                // events request
                if (refid.equals("X0000000000000000000000000000000") &&
                        dataid.equals("X0000000000000000000000000000000")) {
                    return handleEventsRequest(request, response);
                }
            }
        }

        throw new UnsupportedRequestException();
    }
    /**
     * Handles an incoming request for the events.
     * @param request The Spark request
     * @param response The Spark response
     * @return A response object for Spark
     */
    private Object handleEventsRequest(final Request request, final Response response) {
        final String requestModel = request.attribute("model");

        if (this.getSanitizedModel(requestModel).equals("mdx_2018042300")) {
            return this.sendResponse(request, response, EVENTS_RESPONSE_2018042300);
        } else {
            throw new UnsupportedRequestException();
        }
    }
}
