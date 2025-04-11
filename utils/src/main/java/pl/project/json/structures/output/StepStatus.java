package pl.project.json.structures.output;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StepStatus {

    private List<String> leftVehicles;
    private List<String> leftPedestrians;

}
