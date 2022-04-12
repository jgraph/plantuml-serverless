package com.nitor.plantuml.lambda;

import com.nitor.plantuml.lambda.exception.StatusCodeException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.nitor.plantuml.PlantUmlUtil.NOETAG;

class LambdaBase {

    private static final String GRAPHVIZ_DOT = "GRAPHVIZ_DOT";
    static final String LAMBDA_TASK_ROOT = "LAMBDA_TASK_ROOT";
    private static final String DOT_PATH = "/opt/dot_static";
    static final long DEFAULT_MAX_AGE = 3600;

    private static final Logger logger = LoggerFactory.getLogger(LambdaBase.class);

    static {
        if (System.getenv(LAMBDA_TASK_ROOT) == null) {
            logger.error(String.format("%s environment variable is not set. Rendering without graphviz dot!", LAMBDA_TASK_ROOT));
        } else {
            System.setProperty(GRAPHVIZ_DOT, DOT_PATH);
        }
        logger.debug(String.format("GRAPHVIZ_DOT system property: %s", System.getProperty(GRAPHVIZ_DOT)));
    }

    Map<String, String> getCacheHeaders(String etag, long maxAge) {
        if (NOETAG.equals(etag)) {
            return new HashMap<>();
        } else {
            Map<String, String> headers = new HashMap<>();
            headers.put("ETag", etag);
            headers.put("Cache-Control", "public, max-age=" + maxAge);
            return headers;
        }
    }

    boolean isMatchingEtag(APIGatewayV2HTTPEvent event, String expectedEtag) {
        logger.debug(String.format("expected etag %s -> event: %s", expectedEtag, event.getHeaders().toString()));
        if (expectedEtag != null && !NOETAG.equals(expectedEtag))
        {
            Map<String, String> headers = event.getHeaders();

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if ("if-none-match".equalsIgnoreCase(entry.getKey())) {
                    return expectedEtag.equals(entry.getValue());
                }
            }
        }

        return false;
    }

    void send304Response(APIGatewayV2HTTPResponse response, Map<String, String> headers) {
        headers.put("Access-Control-Allow-Origin", "*");
        response.setStatusCode(304);
        response.setHeaders(headers);
    }

    void sendOKDiagramResponse(APIGatewayV2HTTPResponse response, String strResponse, DiagramType diagramType, boolean isBase64) {
        sendOKDiagramResponse(response, strResponse, diagramType, new HashMap<>(), isBase64);
    }

    void sendOKDiagramResponse(APIGatewayV2HTTPResponse response, String strResponse,
                               DiagramType diagramType, Map<String, String> headers, boolean isBase64) {
        sendDiagramResponse(response, strResponse, diagramType, HttpStatus.SC_OK, headers, isBase64);
    }

    void sendDiagramResponse(APIGatewayV2HTTPResponse response, String strResponse, DiagramType diagramType,
                             int statusCode, boolean isBase64) {
        sendDiagramResponse(response, strResponse, diagramType, statusCode, new HashMap<>(), isBase64);
    }

    void sendDiagramResponse(APIGatewayV2HTTPResponse response, String strResponse, DiagramType diagramType,
                             int statusCode, Map<String, String> headers, boolean isBase64) {
        headers.put("Content-Type", diagramType.getMimeType());
        headers.put("Access-Control-Allow-Origin", "*");

        response.setStatusCode(statusCode);
        response.setBody(strResponse);
        response.setHeaders(headers);
        response.setIsBase64Encoded(isBase64);
    }

    void sendOKJSONResponse(APIGatewayV2HTTPResponse response, String base64Response) {
        sendOKJSONResponse(response, base64Response, new HashMap<>());
    }

    void sendOKJSONResponse(APIGatewayV2HTTPResponse response, String base64Response, Map<String, String> headers) {
        sendJSONResponse(response, base64Response, HttpStatus.SC_OK, headers);
    }

    void sendExceptionResponse(APIGatewayV2HTTPResponse response, StatusCodeException statusCodeException) {
        sendExceptionResponse(response, statusCodeException, new HashMap<>());
    }

    void sendExceptionResponse(APIGatewayV2HTTPResponse response, StatusCodeException statusCodeException,
                               Map<String, String> headers) {
        String base64Response = Base64.getEncoder().encodeToString(statusCodeException.getMessage().getBytes());
        sendJSONResponse(response, base64Response, statusCodeException.getStatusCode(), headers);
    }

    void sendJSONResponse(APIGatewayV2HTTPResponse response, String base64Response, int statusCode) {
        sendJSONResponse(response, base64Response, statusCode, new HashMap<>());
    }

    void sendJSONResponse(APIGatewayV2HTTPResponse response, String base64Response,
                          int statusCode, Map<String, String> headers) {
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");

        response.setStatusCode(statusCode);
        response.setBody(base64Response);
        response.setHeaders(headers);
        response.setIsBase64Encoded(true);
    }

    void sendHTMLResponse(APIGatewayV2HTTPResponse response, String htmlResponse, int statusCode) {
        sendHTMLResponse(response, htmlResponse, statusCode, new HashMap<>());
    }

    void sendHTMLResponse(APIGatewayV2HTTPResponse response, String htmlResponse,
                          int statusCode, Map<String, String> headers) {
        headers.put("Content-Type", "text/html");
        headers.put("Access-Control-Allow-Origin", "*");

        response.setStatusCode(statusCode);
        response.setBody(htmlResponse);
        response.setHeaders(headers);
        response.setIsBase64Encoded(false);
    }

    void sendRedirectResponse(APIGatewayV2HTTPResponse response, String redirectPath) {
        sendRedirectResponse(response, redirectPath, new HashMap<>());
    }

    void sendRedirectResponse(APIGatewayV2HTTPResponse response, String redirectPath,
                              Map<String, String> headers) {
        headers.put("Location", redirectPath);
        headers.put("Access-Control-Allow-Origin", "*");
        
        response.setStatusCode(HttpStatus.SC_MOVED_PERMANENTLY);
        response.setHeaders(headers);
        response.setIsBase64Encoded(false);
    }

    String getEncodedUml(APIGatewayV2HTTPEvent event) {
        String encodedUml = event.getRawPath();

        if (encodedUml == null) {
            handleInputError(null);
        }
        return encodedUml.substring(1);
    }

    private void handleInputError(Exception e) {
        throw new IllegalArgumentException("Could not parse parameters", e);
    }
}