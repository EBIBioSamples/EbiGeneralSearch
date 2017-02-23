package uk.ac.ebi.spot.biosamples.XmlUnitExtensions;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.util.Predicate;

public class NodeFilters implements Predicate<Node> {
    @Override
    public boolean test(Node toTest) {
        if(toTest.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) toTest;
            String nodeName = element.getTagName();

            switch(nodeName) {
                case "release_date":
                    return false;
                case "date":
                    return !element.getAttribute("type").equals("update");
            }
        }
        return true;
    }
}
