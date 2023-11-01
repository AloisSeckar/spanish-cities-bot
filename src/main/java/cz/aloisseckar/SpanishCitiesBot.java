package cz.aloisseckar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.FileInputStream;
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

            Properties prop = new Properties();
            String fileName = "bot.config";
            try (FileInputStream fis = new FileInputStream(fileName)) {
                prop.load(fis);
            }

            // read `cities.json` file with data to update
            ArrayList<CityData> cityDataList = new ArrayList<>();
            try (var cityDataReader = new JsonReader(new FileReader(prop.getProperty("cityData"), StandardCharsets.UTF_8))) {
                cityDataList.addAll(Arrays.asList(gson.fromJson(cityDataReader, CityData[].class)));
            }
            System.out.println(cityDataList.size() + " CityData items loaded");
            System.out.println("Filtering by province: " + province);
            var filteredCityDataList = cityDataList.stream().filter(data -> data.code_province.equals(province)).toList();
            System.out.println(filteredCityDataList.size() + " CityData items filtered");
            System.out.println();

            // read crawled image data to process
            ArrayList<ImageData> imageDataList = new ArrayList<>();
            try (var flagsDataReader = new JsonReader(new FileReader(prop.getProperty("flagData")))) {
                imageDataList.addAll(Arrays.asList(gson.fromJson(flagsDataReader, ImageData[].class)));
            }
            var flags = imageDataList.size();
            System.out.println(flags + " flags loaded");
            try (var coaDataReader = new JsonReader(new FileReader(prop.getProperty("coaData")))) {
                imageDataList.addAll(Arrays.asList(gson.fromJson(coaDataReader, ImageData[].class)));
            }
            System.out.println((imageDataList.size() - flags) + " coat_of_arms loaded");

            // cycle through all imageData
            // try to find relevant record in "cities"
            // if possible, update value of either "coat_of_arms" or "flag"
            // if not possible, output the problem for manual checking
            System.out.println();
            System.out.println("PROCESSING DATA...");
            System.out.println();
            for (var data : imageDataList) {
                var rawName = data.getName();
                // exclude invalid names (not .svg)
                if (!rawName.endsWith(".svg")) {
                    System.out.println("`" + rawName + "` - marked as not relevant => skipped");
                    continue;
                }
                // sanitize invalid characters (semicolons or apostrophes) + some identified patterns
                var checkedName = rawName
                        .replaceAll(";", " ")
                        .replaceAll("’", " ")
                        .replaceAll(" Spain\\.", "")           // names sometimes end with "_Spain"
                        .replaceAll(" flag\\.", "")           // names sometimes end with "_flag"
                        .replaceAll("\\s\\(.*\\)", "");     // there are sometimes province in brackets
                // extract part from last space to `.svg` suffix - should be (a part of) city name
                var cityName = checkedName.substring(checkedName.lastIndexOf(" ") + 1, checkedName.length() - 4);
                // exclude values with numbers (like "Escut d'Algorfa-2.svg")
                var m = Pattern.compile("\\d+").matcher(cityName);
                if (m.find()) {
                    System.out.println("`" + rawName + "` - marked as not relevant => skipped");
                    continue;
                }
                // yay, relevant name!

                // try to find the city by extracted name
                var city = filteredCityDataList.stream().filter(cityData -> cityData.getName().contains(cityName)).findFirst();
                if (city.isEmpty()) {
                    System.out.println("`" + rawName + "` - matching city not found => skipped");
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
                        System.out.println("`" + rawName + "` - image not used => skipped");
                        continue;
                    }
                    // do not overwrite existing entry
                    if (cityData.getFlag() != null) {
                        System.out.println("`" + rawName + "` - flag already filled => skipped");
                        continue;
                    }
                    // yay, a flag to add!
                    city.get().setFlag(flag);
                    System.out.println("`" + rawName + "` - added as `flag` to " + city.get().getName());
                } else {
                    // check if the file is actually used on spanish wikipedia
                    if (!checkImageUsage(coatOfArms)) {
                        System.out.println("`" + rawName + "` - image not used => skipped");
                        continue;
                    }
                    // do not overwrite existing entry
                    if (cityData.getCoat_of_arms() != null) {
                        System.out.println("`" + rawName + "` - coat_of_arms already filled => skipped");
                        continue;
                    }
                    // yay, a coat_of_arms to add!
                    city.get().setCoat_of_arms(coatOfArms);
                    System.out.println("`" + rawName + "` - added as `coat_of_arms` to " + city.get().getName());
                }
            }

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
