package uk.ac.ebi.spot.biosamples.Service.xml;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XmlUtilities {
    public static String prettyPrint(Document d) {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        return xmlOutput.outputString(d);
    }

    public static String prettyPrint(Element e) {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        return xmlOutput.outputString(e);

    }
}
