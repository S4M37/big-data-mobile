package gl4.insat.tn.bigdatamobile.services;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import gl4.insat.tn.bigdatamobile.entities.RSSItemAdress;


public class RSSParserStreetService extends DefaultHandler {
    private final static String TAG_ITEM = "result";
    private final static String xmltags = "formatted_address";
    private RSSItemAdress currentitem;
    private ArrayList<RSSItemAdress> itemarray = null;
    private int currentindex = -1;
    private boolean isParsing = false;
    private StringBuilder builder = new StringBuilder();

    public RSSParserStreetService(ArrayList<RSSItemAdress> itemarray) {
        super();

        this.itemarray = itemarray;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);

        if (isParsing && -1 != currentindex && null != builder) {
            builder.append(ch, start, length);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (localName.equalsIgnoreCase(TAG_ITEM)) {
            currentitem = new RSSItemAdress();
            currentindex = -1;
            isParsing = true;

            itemarray.add(currentitem);
        } else {
            currentindex = itemIndexFromString(localName);

            builder = null;

            if (-1 != currentindex)
                builder = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (localName.equalsIgnoreCase(TAG_ITEM)) {
            isParsing = false;
        } else if (currentindex != -1) {
            if (isParsing) {
                if (currentindex == 0) {
                    currentitem.formatted_address = builder.toString();
                }
            }
        }
    }

    private int itemIndexFromString(String tagname) {
        int itemindex = -1;

        if (tagname.equalsIgnoreCase(xmltags)) {
            itemindex = 0;
        }

        return itemindex;
    }
}
