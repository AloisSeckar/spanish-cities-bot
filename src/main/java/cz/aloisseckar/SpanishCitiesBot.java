package cz.aloisseckar;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
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
            var gson = new Gson();

            // read `output.json` file (image data to read)
            var imageDataPath = "c:\\Programming\\Git\\wiki-image-crawler\\target\\output.json";
            var imageDataReader = new JsonReader(new FileReader(imageDataPath));
            ImageData[] imageDataList = gson.fromJson(imageDataReader, ImageData[].class);

            for (var data : imageDataList) {
                System.out.println(data.toString());
            }

            // read `cities.json` file (cities data to update)
            var citiesDataPath = "c:\\Temp\\cities.json";
            var citiesDataReader = new JsonReader(new FileReader(citiesDataPath));
            CitiesData[] citiesDataList = gson.fromJson(citiesDataReader, CitiesData[].class);

            for (var data : citiesDataList) {
                System.out.println(data.toString());
            }

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
