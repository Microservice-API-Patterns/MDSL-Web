package io.mdsl.web.interfaces;

import io.mdsl.web.config.ConfigProperties;
import io.mdsl.web.services.MDSLProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
public class StoryToEndpointWebController {

    final static Logger logger = LoggerFactory.getLogger(StoryToEndpointWebController.class);

    @GetMapping("/")
    public String getStoryIndexPage() {
        return "story_to_endpoint_index";
    }

    @PostMapping("/story-to-endpoint")
    public String convertStoryToEndpoint(
            Model model,
            @RequestParam(value = "Name") String storyName,
            @RequestParam(value = "Client") String persona,
            @RequestParam(value = "Action") String action,
            @RequestParam(value = "Goal") String goal,
            @RequestParam(value = "Objects", required = false) String objects
            ) {

        try {
            logger.info("Converting story " + storyName + " to MDSL: " + persona + ", " + action + ", " + goal + objects);
            String newMDSL = createMDSLDescription(storyName, persona, action, goal, objects);
            model.addAttribute("target", newMDSL);
            model.addAttribute("name", storyName);
            model.addAttribute("who", persona);
            model.addAttribute("what", action);
            model.addAttribute("why", goal);
            model.addAttribute("objects", objects);
        } catch (Exception e) {
            logger.error("An error occurred while converting to MDSL: ", e);
            model.addAttribute("report", e.getMessage());
            return "error.html";
        }

        return "story_to_endpoint_result.html";
    }

    private String createMDSLDescription(String storyName, String persona, String action, String goal, String objects) {

        String mdslSpecName = "APISupporting" + storyName;

        String newMDSL = "API description " + mdslSpecName;
        newMDSL += "\n";
        newMDSL += "\nscenario " + storyName + "Scenario";
        newMDSL += "\n  story " + storyName + "";
        newMDSL += "\n    a " + quote(persona) + " wants to " + quote(action);

        if (StringUtils.hasText(objects)) {
            var objectsAnded = Arrays
                    .stream(objects.split(","))
                    .map(String::trim)
                    .map(StoryToEndpointWebController::quote)
                    .collect(Collectors.joining(" and "));

            newMDSL += " with " + objectsAnded;
        }
        newMDSL += " so that " + quote(goal);
        logger.info("Story as string: " + newMDSL);
        return newMDSL;
    }

    private static String quote(String s) {
        return "\"" + s + "\"";
    }
}