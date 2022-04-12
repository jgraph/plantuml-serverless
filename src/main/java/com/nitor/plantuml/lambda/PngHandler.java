package com.nitor.plantuml.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.nitor.plantuml.PlantUmlUtil;
import com.nitor.plantuml.lambda.exception.StatusCodeException;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.http.HttpStatus;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;

public class PngHandler extends LambdaBase implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>{
    private static final String TYPE_IDENTIFIER = "png";
    private final PlantUmlUtil plantUmlUtil = new PlantUmlUtil();

    @Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        String encodedUml = getEncodedUml(event);
        final String etag = plantUmlUtil.getEtag(encodedUml, TYPE_IDENTIFIER);

        if (isMatchingEtag(event, etag)) {
            send304Response(response, new HashMap<>());
            return response;
        }

        try {
            SourceStringReader reader = plantUmlUtil.readDiagram(encodedUml);
            ByteArrayOutputStream baos = plantUmlUtil.renderDiagram(reader, DiagramType.IMAGE_PNG);
            byte[] bytes = baos.toByteArray();
            String base64Response = Base64.getEncoder().encodeToString(bytes);
            sendOKDiagramResponse(response, base64Response, DiagramType.IMAGE_PNG,
                        getCacheHeaders(etag, DEFAULT_MAX_AGE), true);
        } catch (StatusCodeException sce) {
            sendExceptionResponse(response, sce);
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return response;
    }
}