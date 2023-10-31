package cz.aloisseckar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import lombok.Data;

/**
 * Helper for <a href="https://github.com/ByMykel/spanish-cities">https://github.com/ByMykel/spanish-cities</a> project.
 * 
 * @author alois.seckar@gmail.com
 */
public class SpanishCitiesBot {
    
    /**
     * Simple main method.
     * 
     * @param args not supported (ignored) in this starter template
     */
    public static void main(String[] args) {

        try {
            // we need province code (to avoid confusing cities with same name)
            var in = new Scanner(System.in);
            System.out.println("Enter the code of province:");
            var province = in.nextLine();

            System.out.println("LOADING DATA...");
            var gson = new Gson();

            // read `cities.json` file (cities data to update)
            var cityDataPath = "c:\\Temp\\cities.json";
            var cityDataReader = new JsonReader(new FileReader(cityDataPath, StandardCharsets.UTF_8));
            CityData[] cityDataList;
            try {
                cityDataList = gson.fromJson(cityDataReader, CityData[].class);
            } finally {
                cityDataReader.close();
            }
            System.out.println(cityDataList.length + " CityData items loaded");
            System.out.println("Filtering by province: " + province);
            var filteredCityDataList = Arrays.stream(cityDataList).filter(data -> data.code_province.equals(province)).toList();
            System.out.println(filteredCityDataList.size() + " CityData items filtered");
            System.out.println();

            // read `output.json` file (image data to read)
            var imageDataPath = "c:\\Programming\\Git\\wiki-image-crawler\\target\\output.json";
            var imageDataReader = new JsonReader(new FileReader(imageDataPath));
            ImageData[] imageDataList;
            try {
                imageDataList = gson.fromJson(imageDataReader, ImageData[].class);
            } finally {
                imageDataReader.close();
            }
            System.out.println(imageDataList.length + " ImageData items loaded");

            // cycle through imageData
            // try to find relevant record in "cities"
            // if possible, update value of either "coat_of_arms" or "flag"
            // if not possible, output the problem for manual checking
            System.out.println("PROCESSING DATA...");
            System.out.println();
            for (var data : imageDataList) {
                System.out.println("----");
                var rawName = data.getName();
                // exclude invalid names (not .svg, with english names or with brackets)
                if (!rawName.endsWith(".svg") || rawName.contains("Coat of Arms") || rawName.contains("(")) {
                    System.out.println("`" + rawName + "` - marked as not relevant => skipping...");
                    continue;
                }
                // sanitize invalid characters (semicolons or apostrophes)
                var checkedName = rawName.replaceAll(";", " ").replaceAll("’", " ");
                // extract part from last space to `.svg` suffix - should be (a part of) city name
                var cityName = checkedName.substring(checkedName.lastIndexOf(" ") + 1, checkedName.length() - 4);
                // exclude values with numbers (like "Escut d'Algorfa-2.svg")
                var m = Pattern.compile("\\d+").matcher(cityName);
                if (m.find()) {
                    System.out.println("`" + rawName + "` - marked as not relevant => skipping...");
                    continue;
                }
                // yay, relevant name!

                // try to find the city by extracted name
                var city = filteredCityDataList.stream().filter(cityData -> cityData.getName().contains(cityName)).findFirst();
                if (city.isEmpty()) {
                    System.out.println("`" + rawName + "` - matching city not found => skipping...");
                    continue;
                }
                // yay, relevant city!

                // decide whether to update flag or coat_of_arms
                var cityData = city.get();
                var flag = data.getFlag();
                var coatOfArms = data.getCoat_of_arms();
                if (flag != null) {
                    // check if the file is actually used on spanish wikipedia
                    if (!checkImageUsage(flag)) {
                        System.out.println("`" + rawName + "` - image not used => skipping...");
                        continue;
                    }
                    // do not overwrite existing entry
                    if (cityData.getFlag() != null) {
                        System.out.println("`" + rawName + "` - flag already filled => skipping...");
                        continue;
                    }
                    city.get().setFlag(flag);
                } else {
                    // check if the file is actually used on spanish wikipedia
                    if (!checkImageUsage(coatOfArms)) {
                        System.out.println("`" + rawName + "` - image not used => skipping...");
                        continue;
                    }
                    // do not overwrite existing entry
                    if (cityData.getCoat_of_arms() != null) {
                        System.out.println("`" + rawName + "` - coat_of_arms already filled => skipping...");
                        continue;
                    }
                    city.get().setCoat_of_arms(coatOfArms);
                }
            }
            System.out.println("----");

            // write altered cities data into file
            var cityDataOutputPath = "c:\\Temp\\cities-out.json";
            try (final var fileWriter = new FileWriter(cityDataOutputPath, StandardCharsets.UTF_8)) {
                new GsonBuilder()
                        .setPrettyPrinting()    // include indentation
                        .serializeNulls()       // do not omit NULLs
                        .disableHtmlEscaping()
                        .create()
                        .toJson(cityDataList, fileWriter);
            }

            System.out.println("FINISHED");

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }

    }

    private static boolean checkImageUsage(String imageName) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            var fileName = imageName.substring(imageName.lastIndexOf("/") + 1);
            var apiUrl = "https://es.wikipedia.org/w/api.php?action=query&generator=fileusage&titles=File:" + fileName + "&format=json";
            var apiRequest = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();
            var apiResponse = client.send(apiRequest, HttpResponse.BodyHandlers.ofString());
            // if image is used, there is a "query" object with a "pages" object in JSON
            // if image is not used, there is only "batchcomplete" element
            return apiResponse.body().contains("query") && apiResponse.body().contains("pages");
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return false;
    }

    @Data
    private static class ImageData {
        private String name;
        private String flag;
        private String coat_of_arms;

        @Override
        public String toString() {
            return "ImageData{" +
                    "name='" + name + '\'' +
                    ", flag='" + flag + '\'' +
                    ", coat_of_arms='" + coat_of_arms + '\'' +
                    '}';
        }
    }

    @Data
    private static class CityData {
        private String code;
        private String name;
        private String code_autonomy;
        private String code_province;
        private String flag;
        private String coat_of_arms;

        @Override
        public String toString() {
            return "cityData{" +
                    "code='" + code + '\'' +
                    ", name='" + name + '\'' +
                    ", code_autonomy='" + code_autonomy + '\'' +
                    ", code_province='" + code_province + '\'' +
                    ", flag='" + flag + '\'' +
                    ", coat_of_arms='" + coat_of_arms + '\'' +
                    '}';
        }
    }
    
}
