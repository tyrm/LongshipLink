package codes.tyr.longshiplink.auth;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.config.LLServerConfigs;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AuthClient {
    public static UserAuthResponse getClientToken(String uuid) {
        try {
            // Construct query parameters safely encoding the values
            String queryParams = String.format("sid=%s&secret=%s&uid=%s",
                URLEncoder.encode(LLServerConfigs.SERVER_ID, StandardCharsets.UTF_8),
                URLEncoder.encode(LLServerConfigs.SERVER_SECRET, StandardCharsets.UTF_8),
                URLEncoder.encode(uuid, StandardCharsets.UTF_8)
            );

            // Combine base URL with query parameters
            URL url = new URL(LLServerConfigs.AUTH_URL + "/api/v1/auth/user?" + queryParams);

            // Open the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Set request headers if needed, e.g., accept JSON
            connection.setRequestProperty("Accept", "application/json");

            // Check the response code
            int status = connection.getResponseCode();
            //LongshipLink.LOGGER.info("Response Code: " + status);

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            //LongshipLink.LOGGER.info("Response: " + response);

            Gson gson = new Gson();

            // Return the response as a string
            return gson.fromJson(response.toString(), UserAuthResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // or handle more gracefully
        }
    }
}
