package cz.aloisseckar;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
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
            System.out.println("LOADING DATA...");
            var gson = new Gson();

            // read `output.json` file (image data to read)
            var imageDataPath = "c:\\Programming\\Git\\wiki-image-crawler\\target\\output.json";
            var imageDataReader = new JsonReader(new FileReader(imageDataPath));
            ImageData[] imageDataList = gson.fromJson(imageDataReader, ImageData[].class);
            System.out.println(imageDataList.length + " ImageData items loaded");

            // read `cities.json` file (cities data to update)
            var citiesDataPath = "c:\\Temp\\cities.json";
            var citiesDataReader = new JsonReader(new FileReader(citiesDataPath));
            CitiesData[] citiesDataList = gson.fromJson(citiesDataReader, CitiesData[].class);
            System.out.println(citiesDataList.length + " CitiesData items loaded");
            System.out.println();

            // cycle through imageData
            // try to find relevant record in "cities"
            // if possible, update value of either "coat_of_arms" or "flag"
            // if not possible, output the problem for manual checking
            System.out.println("PROCESSING DATA...");
            System.out.println();
            for (var data : imageDataList) {
                var rawName = data.getName();
                // exclude invalid names (not .svg, with english names or with brackets)
                if (!rawName.endsWith(".svg") || rawName.contains("Coat of Arms") || rawName.contains("(")) {
                    System.out.println("`" + rawName + "` marked as not relevant => skipping...");
                    continue;
                }
                // sanitize invalid characters (semicolons or apostrophes)
                var checkedName = rawName.replaceAll(";", " ").replaceAll("’", " ");
                // extract part from last space to `.svg` suffix - should be (a part of) city name
                var cityName = checkedName.substring(checkedName.lastIndexOf(" "), checkedName.length() - 4);
                // exclude values with numbers (like "Escut d'Algorfa-2.svg")
                var m = Pattern.compile("\\d+").matcher(cityName);
                if (m.find()) {
                    System.out.println("`" + rawName + "` marked as not relevant => skipping...");
                    continue;
                }
                // yay, relevant name!
                System.out.println(cityName);
            }

            System.out.println("FINISHED");

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }

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
    private static class CitiesData {
        private String code;
        private String name;
        private String code_autonomy;
        private String code_province;
        private String flag;
        private String coat_of_arms;

        @Override
        public String toString() {
            return "CitiesData{" +
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
