package pl.project.json.structures.output;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepStatusList {
    private List<StepStatus> stepStatuses = new ArrayList<>();

    public void addStep(StepStatus stepStatus) {
        this.stepStatuses.add(stepStatus);
    }

}
