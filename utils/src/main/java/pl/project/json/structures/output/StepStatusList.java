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
    private List<StepStatus> stepStatuses = new ArrayList<StepStatus>();

    public void addStep(List<String> stepsStatusString ) {

        StepStatus stepStatus = new StepStatus(stepsStatusString);
        this.stepStatuses.add(stepStatus);
    }

    public boolean equals(StepStatusList stepStatusList) {
        return this.stepStatuses.equals(stepStatusList.stepStatuses);
    }

}
