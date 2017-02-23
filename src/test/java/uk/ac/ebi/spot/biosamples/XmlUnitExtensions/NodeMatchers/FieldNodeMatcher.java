package uk.ac.ebi.spot.biosamples.XmlUnitExtensions.NodeMatchers;

import org.w3c.dom.Element;
import org.xmlunit.diff.ElementSelector;

public class FieldNodeMatcher implements ElementSelector{

    @Override
    public boolean canBeCompared(Element controlElement, Element testElement) {
        String controlTag = controlElement.getTagName();
        String testTag = testElement.getTagName();
        if (controlTag.equals("field") && controlTag.equals(testTag)) {
            return controlElement.getAttribute("name").equals(testElement.getAttribute("name"));
        }
        return true;
    }
}
