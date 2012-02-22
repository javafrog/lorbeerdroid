package de.lorbeer.handler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.lorbeer.helper.GeoMensa;

public class MensaHandler extends DefaultHandler {

	private List<GeoMensa> mensas;
	GeoMensa m;

	private Boolean id_tag = false;
	private Boolean name_tag = false;
	private Boolean latitude_tag = false;
	private Boolean longitude_tag = false;
	@SuppressWarnings("unused")
	private Boolean mensa_tag = false;

	public MensaHandler() {
	}

	@Override
	public void startDocument() throws SAXException {

		this.mensas = new ArrayList<GeoMensa>();

	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("total mensas: " + mensas.size());
		// Nothing to do
	}

	// called on opening <tag>
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("mensa")) {
			mensa_tag = true;
			m = new GeoMensa();
		}
		if (localName.equals("id")) {
			id_tag = true;
		}
		if (localName.equals("name")) {
			name_tag = true;
		}
		if (localName.equals("geoLatitude")) {
			latitude_tag = true;
		}
		if (localName.equals("geoLongitude")) {
			longitude_tag = true;
		}

	}

	// called on characters
	@Override
	public void characters(char ch[], int start, int length) {

		if (this.id_tag) {
			m.id = (new String(ch, start, length));
			id_tag = false;
		}
		if (this.name_tag) {
			m.name = new String(ch, start, length);
			name_tag = false;
		}
		if (this.latitude_tag) {
			m.geoLatitude = Double.parseDouble(new String(ch, start, length));
			latitude_tag = false;
		}
		if (this.longitude_tag) {
			m.geoLongitude = Double.parseDouble(new String(ch, start, length));
			longitude_tag = false;
		}

	}

	// called on closing </tag>
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("mensa")) {
			mensas.add(m);
			mensa_tag = false;
		}

	}

	public List<GeoMensa> getGeoMensas() {
		return mensas;
	}

}
