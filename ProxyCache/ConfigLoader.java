import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class ConfigLoader {
    private static ConfigLoader instance;
    private final Document configDocument;

    private ConfigLoader(String configFilePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        configDocument = builder.parse(configFilePath);
        configDocument.getDocumentElement().normalize();
    }

    public static ConfigLoader getInstance(String configFilePath) throws Exception {
        if (instance == null) {
            instance = new ConfigLoader(configFilePath);
        }
        return instance;
    }

    public String getConfigValue(String key) {
        NodeList nodes = configDocument.getElementsByTagName(key);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }
}
