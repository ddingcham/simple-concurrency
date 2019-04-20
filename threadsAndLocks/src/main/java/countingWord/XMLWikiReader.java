package countingWord;

import countingWord.parser.Page;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

public class XMLWikiReader implements WikiReader {

    private static final String ROOT_NAME = "mediawiki";
    private static final String PAGE_NAME = "page";
    private static final String TITLE_NAME = "title";
    private static final String TEXT_NAME = "text";
    private final XMLEventReader reader;
    private boolean isDocumentEnd;

    public XMLWikiReader(String xmlPath) {
        FileInputStream file;
        try {
            file = new FileInputStream(xmlPath);
            this.reader = XMLInputFactory.newInstance().createXMLEventReader(file);
        } catch (FileNotFoundException | XMLStreamException invalidPath) {
            invalidPath.printStackTrace();
            throw new IllegalArgumentException("invalid path");
        }
        isDocumentEnd = false;
    }

    @Override
    public Optional<Page> nextPage() {
        Page nextPage = null;
        if (!isDocumentEnd && nextStartPageEvent().isPresent()) {
            nextPage = generateNextPage();
        }
        return Optional.ofNullable(nextPage);
    }

    private Optional<XMLEvent> nextStartPageEvent() {
        XMLEvent currentEvent = null;
        try {
            currentEvent = reader.nextEvent();
            while (!isStartEventOf(currentEvent, PAGE_NAME)) {
                currentEvent = reader.nextEvent();
                if (isEndEventOf(currentEvent, ROOT_NAME)) {
                    currentEvent = null;
                    isDocumentEnd = true;
                    reader.close();
                    break;
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(currentEvent);
    }

    private Page generateNextPage() {
        String title = EMPTY;
        String text = EMPTY;
        try {
            while (true) {
                XMLEvent currentEvent = reader.nextEvent();
                if (currentEvent.isStartElement()) {
                    if (isStartEventOf(currentEvent, TITLE_NAME)) {
                        title = reader.getElementText();
                    } else if (isStartEventOf(currentEvent, TEXT_NAME)) {
                        text = reader.getElementText();
                    }
                }
                if (isEndEventOf(currentEvent, PAGE_NAME)) {
                    break;
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return new Page(title, text);
    }

    private static boolean isStartEventOf(XMLEvent event, String elementName) {
        return event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(elementName);
    }

    private static boolean isEndEventOf(XMLEvent event, String elementName) {
        return event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName);
    }
}