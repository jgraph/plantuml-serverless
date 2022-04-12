package com.nitor.plantuml;

import com.nitor.plantuml.lambda.DiagramType;
import com.nitor.plantuml.lambda.exception.BadRequestException;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.code.AsciiEncoder;
import net.sourceforge.plantuml.servlet.utility.UmlExtractor;
import net.sourceforge.plantuml.version.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

public class PlantUmlUtil {

  private static final Logger logger = LoggerFactory.getLogger(PlantUmlUtil.class);
  public static final String DIAGRAM_TYPE_UNKNOWN = "UNKNOWN";
  public static final String NOETAG = "NOETAG";

  public SourceStringReader readDiagram(String encodedUml) {
    String uml = decodeUml(encodedUml);
    return new SourceStringReader(uml);
  }

  public String getEtag(String encodedUml, String typeIdentifier) {
    String uml = decodeUml(encodedUml);
    if (Pattern.compile("!include(_many|_once)?\\s*https?://").matcher(uml).find()) {
      return NOETAG;
    }
    String baseEtag = typeIdentifier + Version.etag();
    try {
      final AsciiEncoder coder = new AsciiEncoder();
      final MessageDigest msgDigest = MessageDigest.getInstance("MD5");
      msgDigest.update(uml.getBytes(StandardCharsets.UTF_8));
      final byte[] digest = msgDigest.digest();
      return baseEtag + coder.encode(digest);
    } catch (Exception e) {
      logger.error("failed to digest uml", e);
      return NOETAG;
    }
  }

  public ByteArrayOutputStream renderDiagram(SourceStringReader reader, DiagramType diagramType) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    reader.outputImage(baos, new FileFormatOption(DiagramTypeUtil.asFileFormat(diagramType), true));
    return baos;
  }

  public String renderImageMap(SourceStringReader reader) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    return reader.outputImage(baos, new FileFormatOption(FileFormat.PNG, true)).getDescription();
  }

  public String decodeUml(String encodedUml) {
    logger.debug(String.format("Got encoded uml: %s", encodedUml));
    try {
      if (encodedUml.length() > 10) {
          int period = encodedUml.indexOf('.');
          if (period > encodedUml.length() - 10) {
            encodedUml = encodedUml.substring(0, period);
          }
      }
      String decodedUml = UmlExtractor.getUmlSource(encodedUml);
      logger.debug(String.format("Decoded uml: %s", decodedUml));
      return decodedUml;
    } catch (IllegalArgumentException iae) {
      throw new BadRequestException(String.format("Could not decode UML from request path: %s", encodedUml), iae);
    }
  }

}
