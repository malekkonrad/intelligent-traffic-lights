package pl.project.json.structures.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Command {
    private String type;
    private String vehicleId;
    private String startRoad;
    private String endRoad;
}
