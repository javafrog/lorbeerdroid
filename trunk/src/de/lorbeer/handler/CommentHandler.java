package de.lorbeer.handler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.mmt.lorbeerblatt.data.Comment;

public class CommentHandler extends DefaultHandler {

	private List<Comment> comments;
	Comment c;

	@SuppressWarnings("unused")
	private Boolean comment_tag = false;
	private Boolean comment_id_tag = false;
	private Boolean dish_id_tag = false;
	private Boolean authorName_tag = false;
	private Boolean creationDate_tag = false;
	private Boolean text_tag = false;

	public CommentHandler() {
	}

	@Override
	public void startDocument() throws SAXException {
		this.comments = new ArrayList<Comment>();

	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
		System.out.println("total comments: " + comments.size());
	}

	// called on opening <tag>
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("comment")) {
			c = new Comment();
			comment_tag = true;
		}
		if (localName.equals("id")) {
			comment_id_tag = true;
		}
		if (localName.equals("dishId")) {
			dish_id_tag = true;
		}
		if (localName.equals("authorName")) {
			authorName_tag = true;
		}
		if (localName.equals("creationDate")) {
			creationDate_tag = true;
		}
		if (localName.equals("text")) {
			text_tag = true;
		}

	}

	// called on characters
	@Override
	public void characters(char ch[], int start, int length) {
		if (this.comment_id_tag) {
			c.id = (new String(ch, start, length));
			comment_id_tag = false;
		}
		if (this.dish_id_tag) {
			c.dishId = (new String(ch, start, length));
			dish_id_tag = false;
		}
		if (this.authorName_tag) {
			c.authorName = (new String(ch, start, length));
			authorName_tag = false;
		}
		if (this.creationDate_tag) {
			// no date
			// c.creationDate = (new String(ch, start, length));
			creationDate_tag = false;
		}
		if (this.text_tag) {
			c.text = (new String(ch, start, length)).trim();
			text_tag = false;
		}

	}

	// called on closing </tag>
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("comment")) {
			// add commentobject without date! to map
			comments.add(c);
			comment_tag = false;
		}

	}

	public List<Comment> getComments() {
		return comments;
	}
}
