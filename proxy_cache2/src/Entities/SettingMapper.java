package src.Entities;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingMapper {
    Map<String, Object> jsonObject;

    public SettingMapper(String filePath) {
        String json = readFile(filePath);
        jsonObject = getJsonData(json);
    }

    public Map<String, Object> getJsonObject() {
        return jsonObject;
    }

    private String readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public Map<String, Object> getJsonData(String jsonRepresentation) {
        Map<String, Object> result = new HashMap<>();
        jsonRepresentation = jsonRepresentation.replaceAll("[{}]", "").trim();
        String[] paires = jsonRepresentation.split(",");

        for (String p : paires) {
            String[] keyValue = p.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                try {
                    result.put(key, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    public static Map<String, Object> getJsonOption(String jsonFilePath) {
        Map<String, Object> result = new HashMap<>();

        ArrayList<String> keyList = new ArrayList<String>();

        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            jsonContent = jsonContent.substring(1, jsonContent.length() - 1).trim();
            while (jsonContent != null && jsonContent!="" && jsonContent.length()>1) {

                String key = new String();
                int indexOfLastP = 0;
                for (char c : jsonContent.toCharArray()) {
                    key += c;
                    if (c == ':')
                        break;
                    indexOfLastP++;
                }
                keyList.add(key);

                jsonContent = jsonContent.substring(indexOfLastP + 1);

                if (jsonContent.startsWith("[")) {
                    String arrayContent = new String();
                    for (char c : jsonContent.substring(1).toCharArray()) {
                        if (c == ']')
                            continue;
                        arrayContent += c;
                    }
                    System.out.println(arrayContent.trim());
                    String[] valuesTable = arrayContent.split(",");
                    result.put(key, valuesTable);
                    jsonContent = jsonContent.substring(jsonContent.indexOf("]"));
                } else if(jsonContent.startsWith("\"") && jsonContent.endsWith("\"")){
                    String content = new String();
                    int index=1;
                    System.out.println(jsonContent);
                    for (char c : jsonContent.substring(1).toCharArray()) {
                        content += c;
                        index++;
                        if (c == '\"') {
                            continue;
                        }
                    }
                    content = content.replace("\"", "");
                    result.put(key, content);
                    jsonContent = jsonContent.substring(index);
                    System.out.println("end json"+jsonContent);
                }else{
                    System.out.println("begin json "+jsonContent);
                    String content = new String();
                    System.out.println(jsonContent);
                    for(char c : jsonContent.toCharArray()){
                        if(c==',')break;
                        content+=c;
                    }
                    result.put(key, content);
                    System.out.println("json "+jsonContent);
                    if(jsonContent.contains(",")){
                        jsonContent=jsonContent.substring(',');
                    }else{
                        jsonContent=null;
                    }
                }
            }

            // System.out.println(jsonContent);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
