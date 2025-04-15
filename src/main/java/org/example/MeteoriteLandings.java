import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MeteoriteLandings {
    public static void main(String[] args) {
        // This url is not working: https://data.nasa.gov/resource/y77d-th95.json
        String url = "https://gist.githubusercontent.com/mpodmol/45159d6c66793ed93fa846774dcb054c/raw/b7b288fd8d05998c1d01b3257d731a540858fdfd/nasa_meteorite_data.json";

        try {
            // Build HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String jsonData = response.body();
                JSONArray meteorites = new JSONArray(jsonData);

                // Variable definition
                // 1. Entries in the dataset
                int entryCount = meteorites.length();

                // 2. Name and mass of the most massive meteorite in the dataset
                String mostMassiveName = "";
                double mostMassiveWeight = 0;
                int massFormatErrors = 0;

                // 3. Most frequent year in the dataset
                Map<Integer, Integer> yearFrequency = new HashMap<>();
                int mostFrequentYear = 0;
                int highestFrequency = 0;
                int yearFormatErrors = 0;

                for (int i = 0; i < meteorites.length(); i++) {
                    JSONObject meteorite = meteorites.getJSONObject(i);

                    // Find most massive meteorite
                    if (meteorite.has("mass")) {
                        try {
                            double mass = Double.parseDouble(meteorite.getString("mass"));
                            if (mass > mostMassiveWeight) {
                                mostMassiveWeight = mass;
                                mostMassiveName = meteorite.getString("name");
                            }
                        } catch (NumberFormatException e) {
                            massFormatErrors++;
                        }
                    }

                    // Find the most frequent year
                    if (meteorite.has("year")) {
                        try {
                            String yearStr = meteorite.getString("year");
                            if (yearStr.length() >= 4) {
                                int year = Integer.parseInt(yearStr.substring(0, 4));
                                yearFrequency.put(year, yearFrequency.getOrDefault(year, 0) + 1);
                            }
                        } catch (NumberFormatException e) {
                            yearFormatErrors++;
                        }
                    }
                }

                for (Map.Entry<Integer, Integer> entry : yearFrequency.entrySet()) {
                    if (entry.getValue() > highestFrequency) {
                        highestFrequency = entry.getValue();
                        mostFrequentYear = entry.getKey();
                    }
                }

                // Print results
                System.out.println("Total number of entries: " + entryCount);
                System.out.println("Most massive meteorite: " + mostMassiveName + " (Mass: " + mostMassiveWeight / 1000 + " kg)");
                System.out.println("Most frequent year: " + mostFrequentYear);

                // Print error results
                System.out.println("\nError statistics:");
                System.out.println("Entries with invalid mass format: " + massFormatErrors);
                System.out.println("Entries with invalid year format: " + yearFormatErrors);

            } else {
                System.out.println("Error: API returned status code " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error connecting to the NASA dataset: " + e.getMessage());
            e.printStackTrace();
        }
    }
}