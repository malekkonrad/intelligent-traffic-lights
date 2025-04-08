package pl.project;


import pl.project.its.Intersection;
import pl.project.its.Lane;
import pl.project.its.LaneManager;
import pl.project.its.Vehicle;
import pl.project.its.directions.Direction;
import pl.project.json.JsonReader;
import pl.project.json.JsonWriter;
import pl.project.json.structures.input.Command;
import pl.project.json.structures.input.CommandList;
import pl.project.json.structures.output.StepStatus;
import pl.project.json.structures.output.StepStatusList;

import java.io.IOException;
import java.util.Arrays;

import java.util.List;
import java.util.Map;

/**
 * Main program that runs intelligent traffic lights
 */
public class App 
{
    public static void main( String[] args ) throws Exception {

        // wczytanie ustawień skrzyżowania!
        LaneManager laneManager = new LaneManager();
        laneManager.loadFromJson("config.json");

        Map<Direction, List<Lane>> lanes = laneManager.getLanesPerDirection();
        for (Direction dir : lanes.keySet()) {
            System.out.println("Direction: " + dir);
            for (Lane lane : lanes.get(dir)) {
                System.out.println("  Allowed exits: " + lane.getAllowedDestinations());
            }
        }

        // wczytanie listy komend
        JsonReader jsonReader = new JsonReader("commands.json");
        CommandList commandList = jsonReader.read();

        // Obiekt w którym będę zapisywał statusy
        StepStatusList stepStatusList = new StepStatusList();


        Intersection intersection = new Intersection(laneManager.getLanesPerDirection());

        // Przejście po komendach
        for (Command cmd : commandList.getCommands()) {
            System.out.println("Type: " + cmd.getType());
            if ("addVehicle".equals(cmd.getType())) {
                intersection.addVehicle(new Vehicle(cmd.getVehicleId(),0, cmd.getStartRoad(), cmd.getEndRoad()));
            }else{
                List<String> leftVehicle = intersection.step();

                // add step to list that will be saved into json
                stepStatusList.addStep(leftVehicle);
            }
        }


        // zapis
        JsonWriter jsonWriter = new JsonWriter("output.json");
        jsonWriter.writeToFile(stepStatusList);


    }
}
