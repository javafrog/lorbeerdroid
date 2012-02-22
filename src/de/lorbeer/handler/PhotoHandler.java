package de.lorbeer.handler;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PhotoHandler extends DefaultHandler {

	private List<String> photoIDs;

	@SuppressWarnings("unused")
	private Boolean photo_tag = false;
	private Boolean id_tag = false;

	private String id = "";

	public PhotoHandler() {
	}

	@Override
	public void startDocument() throws SAXException {
		photoIDs = new LinkedList<String>();

	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("total photos: " + photoIDs.size());
		// Nothing to do
	}

	// called on opening <tag>
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("photo")) {
			photo_tag = true;
		}
		if (localName.equals("id")) {
			id_tag = true;
		}
	}

	// called on characters
	@Override
	public void characters(char ch[], int start, int length) {
		if (this.id_tag) {
			id = new String(ch, start, length);
			id_tag = false;
		}

	}

	// called on closing </tag>
	/**
	 * Gets be called on closing tags like: </tag>
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("photo")) {
			photoIDs.add(id);
			photo_tag = false;
		}

	}

	public List<String> getPhotoIDs() {
		return photoIDs;
	}

}
