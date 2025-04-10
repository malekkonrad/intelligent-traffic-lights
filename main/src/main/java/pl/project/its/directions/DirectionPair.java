package pl.project.its.directions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class DirectionPair {
    public final Direction startRoad;
    public final Direction endRoad;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectionPair that = (DirectionPair) o;
        return startRoad.equals(that.startRoad) && endRoad.equals(that.endRoad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startRoad, endRoad);
    }

    @Override
    public String toString() {
        return startRoad + " " + endRoad;
    }
}
