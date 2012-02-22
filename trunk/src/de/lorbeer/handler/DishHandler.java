package de.lorbeer.handler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.mmt.lorbeerblatt.data.Dish;
import de.mmt.lorbeerblatt.data.Ingredient;

public class DishHandler extends DefaultHandler {

	private ArrayList<Dish> dishes;
	Dish d;
	private List<Ingredient> ingredients;

	@SuppressWarnings("unused")
	private Boolean dish_tag = false;
	private Boolean d_id_tag = false;
	private Boolean m_id_tag = false;
	private Boolean date_tag = false;
	private Boolean name_tag = false;
	private Boolean price_s_tag = false;
	private Boolean price_e_tag = false;
	private Boolean ingredients_tag = false;
	private Boolean src_url_tag = false;
	private Boolean photo_url_tag = false;

	public DishHandler() {
	}

	@Override
	public void startDocument() throws SAXException {
		this.dishes = new ArrayList<Dish>();

	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
		System.out.println("total dishes: " + dishes.size());
	}

	// called on opening <tag>
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("dish")) {
			d = new Dish();
			d.photoUrl = "";
			d.name = "";
			this.ingredients = new ArrayList<Ingredient>();
			dish_tag = true;
		}
		if (localName.equals("id")) {
			d_id_tag = true;
		}
		if (localName.equals("mensaId")) {
			m_id_tag = true;
		}
		if (localName.equals("date")) {
			date_tag = true;
		}
		if (localName.equals("name")) {
			name_tag = true;
		}
		if (localName.equals("priceStudent")) {
			price_s_tag = true;
		}
		if (localName.equals("priceEmployee")) {
			price_e_tag = true;
		}
		if (localName.equals("ingredients")) {
			ingredients_tag = true;
		}
		if (localName.equals("sourceUrl")) {
			src_url_tag = true;
		}
		if (localName.equals("photoUrl")) {
			photo_url_tag = true;
		}

	}

	// called on characters
	@Override
	public void characters(char ch[], int start, int length) {

		if (this.d_id_tag) {
			d.id = (new String(ch, start, length));
			d_id_tag = false;
		}
		if (this.m_id_tag) {
			d.mensaId = (new String(ch, start, length));
			m_id_tag = false;
		}
		if (this.date_tag) {
			// no date added
			date_tag = false;
		}
		if (this.name_tag) {
			d.name += new String(ch, start, length);

		}
		if (this.price_s_tag) {
			d.priceStudent = Integer.parseInt(new String(ch, start, length));
			price_s_tag = false;
		}
		if (this.price_e_tag) {
			d.priceEmployee = Integer.parseInt(new String(ch, start, length));
			price_e_tag = false;

		}
		if (this.ingredients_tag) {
			getEnum(new String(ch, start, length));
			ingredients_tag = false;
		}
		if (this.src_url_tag) {
			d.sourceUrl = (new String(ch, start, length));
			src_url_tag = false;
		}
		if (this.photo_url_tag) {
			d.photoUrl = (new String(ch, start, length));
			photo_url_tag = false;
		}
	}

	// called on closing </tag>

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {

		if (localName.equals("dish")) {
			// set ingredients, add dish to list
			d.ingredients = ingredients;
			dishes.add(d);
			dish_tag = false;

		}
		if (localName.equals("name")) {
			name_tag = false;
		}

	}

	public ArrayList<Dish> getDishes() {
		return dishes;
	}

	private void getEnum(String s) {
		String[] ingredients_array = { "PORK", "BEEF", "VEGETARIAN", "ALCOHOL",
				"GARLIC", "VEGAN" };
		int pos = -1;
		for (int i = 0; i < ingredients_array.length; i++) {
			if (s.equals(ingredients_array[i])) {
				pos = i;
			}
		}
		if (pos > -1) {
			Ingredient[] values = Ingredient.values();
			this.ingredients.add(values[pos]);
		}

	}

}
