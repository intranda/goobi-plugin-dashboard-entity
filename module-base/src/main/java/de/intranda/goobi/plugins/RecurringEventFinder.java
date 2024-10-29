package de.intranda.goobi.plugins;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class RecurringEventFinder {

    private static final Path folder = Paths.get("/opt/digiverso/goobi/metadata/");

    private static final Namespace modsNS = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
    private static final Namespace metsNS = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");

    private static final Namespace goobiNS = Namespace.getNamespace("goobi", "http://meta.goobi.org/v1.5.1/");

    private static XPathFactory xFactory = XPathFactory.instance();
    private static XPathExpression<Element> expression = xFactory.compile(
            "/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:extension/goobi:goobi/goobi:metadata[@name='RecurringEventGroup']",
            Filters.element(), null, metsNS, modsNS, goobiNS);

    public static void main(String[] args) throws Exception {

        SAXBuilder builder = getSAXBuilder();

        // find all meta.xml files
        List<Path> metadataList = new ArrayList<>();
        Files.find(folder, 2,
                (p, file) -> file.isRegularFile() && p.getFileName().toString().matches("meta.xml"))
                .forEach(metadataList::add);

        // write header row
        System.out.println("name orig; name fre; name ger; name eng; id");

        for (Path metadata : metadataList) {

            // open each file
            Document doc = builder.build(metadata.toFile());

            // check if RecurringEventGroup exists
            for (Element element : expression.evaluate(doc)) {

                // if yes, collect goobi id and event names
                String id = metadata.getParent().getFileName().toString();
                String nameOrig = "";
                String nameFre = "";
                String nameGer = "";
                String nameEng = "";

                for (Element sub : element.getChildren()) {
                    String metadataName = sub.getAttributeValue("name");
                    switch (metadataName) {
                        case "NameORIG": {
                            nameOrig = sub.getValue();
                            break;
                        }
                        case "NameFR": {
                            nameFre = sub.getValue();
                            break;
                        }
                        case "NameDE": {
                            nameGer = sub.getValue();
                            break;
                        }
                        case "NameEN": {
                            nameEng = sub.getValue();
                            break;
                        }
                        default:
                    }
                }
                // write rows
                System.out.println(nameOrig + "; " + nameFre + "; " + nameGer + "; " + nameEng + "; " + id);
            }

        }

    }

    private static SAXBuilder getSAXBuilder() {
        SAXBuilder builder = new SAXBuilder();
        // Disable access to external entities
        builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return builder;
    }
}
