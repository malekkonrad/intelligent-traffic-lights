package pl.project;


import pl.project.json.JsonReader;
import pl.project.json.JsonWriter;

import java.io.IOException;

/**
 * Main program that runs intelligent traffic lights
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        JsonReader jsonReader = new JsonReader("commands.json");
        JsonWriter jsonWriter = new JsonWriter("output.json");
        jsonWriter.writeToFile();

    }
}
