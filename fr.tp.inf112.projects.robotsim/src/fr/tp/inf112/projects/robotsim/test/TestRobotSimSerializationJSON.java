package fr.tp.inf112.projects.robotsim.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

public class TestRobotSimSerializationJSON {

    private final ObjectMapper objectMapper;
    private static final Logger logger = Logger.getLogger(TestRobotSimSerializationJSON.class.getName());


    public TestRobotSimSerializationJSON() {
        objectMapper = new ObjectMapper();
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
        		.allowIfSubType(PositionedShape.class.getPackageName())
        		.allowIfSubType(Component.class.getPackageName())
        		.allowIfSubType(BasicVertex.class.getPackageName())
        		.allowIfSubType(ArrayList.class.getName())
        		.allowIfSubType(LinkedHashSet.class.getName())
        		.build();


        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
    }
    @Test
    public void testSerialization() {
        try {
        	
            // Create a sample Factory object (replace this with real data if needed)
            Factory myFactory = new Factory(100, 200, "Test Factory");

            // Serialize the Factory object to JSON string
            final String factoryAsJsonString = objectMapper.writeValueAsString(myFactory);
            logger.info("Serialized Factory JSON: " + factoryAsJsonString);

            // Deserialize the JSON string back into a Factory object
            final Factory roundTrip = objectMapper.readValue(factoryAsJsonString, Factory.class);
            logger.info("Deserialized Factory Object: " + roundTrip.toString());

            // Verify that the serialized and deserialized objects are equivalent
            assert myFactory.getWidth() == roundTrip.getWidth();
            assert myFactory.getHeight() == roundTrip.getHeight();
            assert myFactory.getName().equals(roundTrip.getName());
        } catch (JsonProcessingException e) {
            logger.severe("Serialization/Deserialization error: " + e.getMessage());
            assert false; // Fail the test if an exception occurs
        }
    }
}
