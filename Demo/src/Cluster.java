import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Pattern;
public class Cluster implements Runnable {
    @Override
    public void run() {
        try {
            createDocument();
            //register();
            //createIndex("first");
            //updateProperty("3","last","MALIK");
            //createDatabase("second-db");
            //createDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int x = 0;
    private String assignedNode = "";
    private String activeDatabase;

    public JSONObject readDocument(String documentId) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = prepareRequest("readDocument", "GET", getActiveDatabase());
        httpURLConnection.setRequestProperty("documentId", documentId);
        String response = readResponse(httpURLConnection);
        try {
            return new JSONObject(response);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public JSONArray readAllDocuments() throws IOException, JSONException {
        String response = readResponse(prepareRequest("readAllDocuments", "GET", getActiveDatabase())).replace("\\", "");
        JSONArray array = new JSONArray();
        while (response.contains("{")) {
            int openingBracket = response.indexOf('{');
            int closingBracket = response.indexOf('}');
            array.put(new JSONObject(response.substring(openingBracket, closingBracket + 1)));
            response = response.substring(closingBracket + 1);
        }
        return array;
    }

    public JSONArray readDocumentsWhere(String property, String value) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = prepareRequest("readDocumentsWhere", "GET", getActiveDatabase());
        httpURLConnection.setRequestProperty("property", property);
        httpURLConnection.setRequestProperty("value", value);
        String response = readResponse(httpURLConnection).replace("\\", "");
        JSONArray array = new JSONArray();
        while (response.contains("{")) {
            int openingBracket = response.indexOf('{');
            int closingBracket = response.indexOf('}');
            array.put(new JSONObject(response.substring(openingBracket, closingBracket + 1)));
            response = response.substring(closingBracket + 1);
        }
        return array;
    }

    public void createDocument() throws IOException, JSONException {
        HttpURLConnection httpURLConnection = prepareRequest("createDocument", "POST", getActiveDatabase());
        JSONObject document = addProperties();
        httpURLConnection.setRequestProperty("document", document.toString());
        httpURLConnection.setRequestProperty("current", "1");
        System.out.println("\n" + readResponse(httpURLConnection));
    }

    public void deleteDocument(String documentId) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = prepareRequest("deleteDocument", "DELETE", getActiveDatabase());
        httpURLConnection.setRequestProperty("documentId", documentId);
        String response = readResponse(httpURLConnection);
        System.out.println(response);
    }

    public void createProperty(String documentId, String property, String value) throws IOException, JSONException {
        String response;
        do {
            HttpURLConnection httpURLConnection = prepareRequest("createProperty", "POST", getActiveDatabase());
            httpURLConnection.setRequestProperty("documentId", documentId);
            httpURLConnection.setRequestProperty("property", property);
            httpURLConnection.setRequestProperty("value", value);
            httpURLConnection.setRequestProperty("version", readDocument(documentId).get("version").toString());
            response = readResponse(httpURLConnection);
        } while (response.equals("False"));
        System.out.println(response);
    }

    public void updateProperty(String documentId, String property, String value) throws IOException, JSONException {
        String response;
        do {
            x++;
            HttpURLConnection httpURLConnection = prepareRequest("updateProperty", "POST", getActiveDatabase());
            httpURLConnection.setRequestProperty("documentId", documentId);
            httpURLConnection.setRequestProperty("property", property);

            httpURLConnection.setRequestProperty("current", readDocument(documentId).get("version").toString());
            httpURLConnection.setRequestProperty("value", value);
            response = readResponse(httpURLConnection);

            System.out.println(x);
        } while (response.equals("False"));
        System.out.println(response);
    }

    public void deleteProperty(String documentId, String property) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = prepareRequest("deleteProperty", "DELETE", getActiveDatabase());
        httpURLConnection.setRequestProperty("documentId", documentId);
        httpURLConnection.setRequestProperty("property", property);
        String response = readResponse(httpURLConnection);
        System.out.println(response);
    }

    public void createIndex(String property) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = prepareRequest("createIndex", "POST", getActiveDatabase());
        httpURLConnection.setRequestProperty("property", property);
        String response = readResponse(httpURLConnection);
        System.out.println("Response: " + response);
    }

    public void deleteIndex(String property) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = prepareRequest("deleteIndex", "DELETE", getActiveDatabase());
        httpURLConnection.setRequestProperty("property", property);
        String response = readResponse(httpURLConnection);
        System.out.println("Response: " + response);
    }

    public void createDatabase(String database) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = prepareRequest("createDatabase", "POST", database);
        httpURLConnection.setRequestProperty("schema", new JSONObject().toString());
        System.out.println(readResponse(httpURLConnection));
    }

    public void deleteDatabase() throws IOException, JSONException {
        System.out.println("\n" + readResponse(prepareRequest("deleteDatabase", "DELETE", getActiveDatabase())));
    }

    public void connectToDatabase(String databaseName) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = prepareRequest("connectDatabase", "GET", databaseName);
        String response = readResponse(httpURLConnection);
        try {
            new JSONObject(response);
            setActiveDatabase(databaseName);
            System.out.println("\nConnected");
        } catch (JSONException e) {
            System.out.println("\n" + response);
        }
    }

    public boolean connect() throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject(new String(Files.readAllBytes(Paths.get("info.json"))));
        URL url = new URL("http://localhost:8080/connect");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("userId", jsonObject.getString("userId"));
        httpURLConnection.setRequestProperty("password", jsonObject.getString("password"));
        if (httpURLConnection.getResponseCode() == 200) {
            assignedNode = readResponse(httpURLConnection);
            return true;
        }
        return false;
    }

    public void register() throws IOException {
        URL url = new URL("http://localhost:8080/register");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        if (httpURLConnection.getResponseCode() == 200) {
            try (FileWriter fileWriter = new FileWriter("info.json")) {
                fileWriter.write(readResponse(httpURLConnection));
            }
            System.out.println("Registration is done");
        } else System.out.println("FAILED!");
    }

    public boolean getStatus() {
        Pattern pattern = Pattern.compile("http://localhost:80(8[1-9]|90)/");
        return pattern.matcher(assignedNode).matches();
    }


    public void readProperty(String documentId, String property) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = prepareRequest("readProperty", "GET", getActiveDatabase());
        httpURLConnection.setRequestProperty("DocumentId", documentId);
        httpURLConnection.setRequestProperty("property", property);
        String response = readResponse(httpURLConnection);
        System.out.println(response);
    }

    public int MainMenu() {
        System.out.println("1. Read a document");
        System.out.println("2. Read all documents");
        System.out.println("3. Read all matching documents");
        System.out.println("4. Create a document");
        System.out.println("5. Delete a document");
        System.out.println("6. Create a property");
        System.out.println("7. Update a property");
        System.out.println("8. Delete a property");
        System.out.println("9. Create an index");
        System.out.println("10. Delete an index");
        System.out.println("11. Create a database");
        System.out.println("12. Delete a database");
        System.out.println("13. Activate a database");
        System.out.println("14. Connect");
        System.out.println("15. Register");
        System.out.println("16. Exit");
        System.out.println("17. Read a property");
        System.out.print("Enter your option: ");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return Integer.parseInt(bufferedReader.readLine());
        } catch (Exception ioException) {
            return 0;
        }
    }

    private JSONObject addProperties() throws IOException, JSONException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("_id", 0);
        jsonObject.put("affinity", 0);
        jsonObject.put("version", 0);
        String key, type;
        do {
            System.out.print("Enter key name(enter 'done' when finish): ");
            key = bufferedReader.readLine();
            if (key != null && !key.equals("") && !key.equals("done") && !jsonObject.has(key)) {
                System.out.print("Enter value type: ");
                type = bufferedReader.readLine();
                jsonObject.put(key, type);
            } else if (jsonObject.has(key)) {
                System.out.println("This key is used!");
            } else System.out.println("Invalid key");
        } while (key != null && !key.equals("done"));
        return jsonObject;
    }

    private void setActiveDatabase(String activeDatabase) {
        this.activeDatabase = activeDatabase;
    }

    public String getActiveDatabase() {
        return activeDatabase;
    }

    private HttpURLConnection prepareRequest(String endpoint, String method, String databaseName) throws IOException, JSONException {
        URL url = new URL(assignedNode + endpoint);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod(method);
        JSONObject credentials = new JSONObject(new String(Files.readAllBytes(Paths.get("info.json"))));
        httpURLConnection.setRequestProperty("userId", credentials.getString("userId"));
        httpURLConnection.setRequestProperty("password", credentials.getString("password"));
        httpURLConnection.setRequestProperty("database", databaseName);
        return httpURLConnection;
    }

    private String readResponse(HttpURLConnection httpURLConnection) throws IOException {
        System.out.println("wait...");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) response.append(line);
        bufferedReader.close();
        return response.toString();
    }
}