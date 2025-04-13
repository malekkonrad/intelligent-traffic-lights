package pl.project.json;

import org.junit.jupiter.api.Test;
import pl.project.direction.Direction;
import pl.project.traffic.lanes.Lane;
import pl.project.json.structures.input.Command;
import pl.project.json.structures.input.CommandList;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonReaderTest {


    @Test
    void testLoadLanesFromJson_validFile_returnsCorrectData() throws Exception {
        // given
        String filePath = "src/test/resources/valid_config.json";

        // when
        Map<Direction, List<Lane>> lanes = JsonReader.loadLanesFromJson(filePath);

        // then
        assertNotNull(lanes);
        assertTrue(lanes.containsKey(Direction.NORTH));
        assertEquals(1, lanes.get(Direction.NORTH).size());
        assertEquals(List.of(Direction.SOUTH, Direction.WEST, Direction.EAST), lanes.get(Direction.NORTH).get(0).getAllowedDestinations());
    }

    @Test
    void testLoadLanesFromJson_invalidFile_throwsException() {
        // given
        String invalidFilePath = "src/test/resources/nonexistent.json";

        // when & then
        assertThrows(Exception.class, () -> JsonReader.loadLanesFromJson(invalidFilePath));
    }

    @Test
    void testLoadCommandList_invalidFile_throwsException() {
        // given
        String invalidFilePath = "src/test/resources/nonexistent.json";

        // when & then
        assertThrows(Exception.class, () -> JsonReader.loadCommandList(invalidFilePath));
    }

    @Test
    void testLoadCommandList_validFile_returnsCorrectData() throws Exception {
        // given
        String filePath = "src/test/resources/valid_commands.json";

        // when
        CommandList commandList = JsonReader.loadCommandList(filePath);

        // then
        assertNotNull(commandList);
        List<Command> commands = commandList.getCommands();
        assertEquals(8, commands.size());

        // Sprawdzenie pierwszej komendy
        Command firstCommand = commands.get(0);
        assertEquals("addVehicle", firstCommand.getType());
        assertEquals("vehicle1", firstCommand.getVehicleId());
        assertEquals("south", firstCommand.getStartRoad());
        assertEquals("north", firstCommand.getEndRoad());

        // Sprawdzenie trzeciej komendy (typu "step")
        Command thirdCommand = commands.get(2);
        assertEquals("step", thirdCommand.getType());
        assertNull(thirdCommand.getVehicleId());
        assertNull(thirdCommand.getStartRoad());
        assertNull(thirdCommand.getEndRoad());

        // Sprawdzenie ostatniej komendy
        Command lastCommand = commands.get(7);
        assertEquals("step", lastCommand.getType());
        assertNull(lastCommand.getVehicleId());
        assertNull(lastCommand.getStartRoad());
        assertNull(lastCommand.getEndRoad());
    }

}