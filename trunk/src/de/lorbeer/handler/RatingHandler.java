package de.lorbeer.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RatingHandler extends DefaultHandler {

	@SuppressWarnings("unused")
	private Boolean rating_tag = false;
	private Boolean ratingAmount_tag = false;
	private Boolean ratingSpiciness_tag = false;
	private Boolean ratingAppearence_tag = false;

	private int count = 0;

	private int amount_sum = 0;
	private int spiciness_sum = 0;
	private int appearence_sum = 0;

	private long amount = 0;
	private long spiciness = 0;
	private long appearence = 0;

	public RatingHandler() {
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("total ratings: " + count);
		// Nothing to do
	}

	// called on opening <tag>
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("rating")) {
			rating_tag = true;
		}
		if (localName.equals("ratingAmount")) {
			ratingAmount_tag = true;
		}
		if (localName.equals("ratingSpiciness")) {
			ratingSpiciness_tag = true;
		}
		if (localName.equals("ratingAppearance")) {
			ratingAppearence_tag = true;
		}

	}

	// called on characters
	@Override
	public void characters(char ch[], int start, int length) {

		if (this.ratingAmount_tag) {
			amount_sum += Integer.parseInt(new String(ch, start, length));
		}
		if (this.ratingSpiciness_tag) {
			spiciness_sum += Integer.parseInt(new String(ch, start, length));
		}
		if (this.ratingAppearence_tag) {
			appearence_sum += Integer.parseInt(new String(ch, start, length));
		}

	}

	// called on closing </tag>
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("rating")) {
			count++;
			rating_tag = false;
		}
		if (localName.equals("ratingAmount")) {
			ratingAmount_tag = false;
		}
		if (localName.equals("ratingSpiciness")) {
			ratingSpiciness_tag = false;
		}
		if (localName.equals("ratingAppearance")) {
			ratingAppearence_tag = false;
		}

	}

	public long getSpiciness() {
		spiciness = spiciness_sum / count;
		return spiciness;

	}

	public long getAmount() {
		amount = amount_sum / count;
		return amount;

	}

	public long getAppearence() {
		appearence = appearence_sum / count;
		return appearence;

	}

	public int getCount() {
		return count;
	}

}
