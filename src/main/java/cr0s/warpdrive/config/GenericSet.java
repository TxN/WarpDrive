package cr0s.warpdrive.config;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IXmlRepresentable;
import cr0s.warpdrive.api.IXmlRepresentableUnit;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Represents a set of 'units' that will be chosen randomly during world generation.
 **/
public class GenericSet<E extends IXmlRepresentableUnit> implements IXmlRepresentable, Comparable {
	
	protected String group;
	protected String name;
	private E unitDefault;
	private String nameElementUnit;
	private XmlRandomCollection<E> units;
	private ArrayList<String> importGroupNames;
	private ArrayList<String> importGroups;
	
	public String getFullName() {
		return group + ":" + name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public GenericSet(final String group, final String name, final E unitDefault, final String nameElementUnit) {
		this.group = group;
		this.name = name;
		this.unitDefault = unitDefault;
		this.nameElementUnit = nameElementUnit;
		units = new XmlRandomCollection<>();
		importGroupNames = new ArrayList<>();
		importGroups = new ArrayList<>();
	}
	
	public boolean isEmpty() {
		return units.isEmpty();
	}
	
	public E getRandomUnit(final Random random) {
		E unit = units.getRandomEntry(random);
		if (unit == null) {
			WarpDrive.logger.error(String.format("null %s encountered in set %s", nameElementUnit, getFullName()));
			unit = unitDefault;
		}
		return unit;
	}
	
	@Override
	public boolean loadFromXmlElement(Element element) throws InvalidXmlException {
		final List<Element> listChildren = XmlFileManager.getChildrenElementByTagName(element, nameElementUnit);
		for (Element elementChild : listChildren) {
			@SuppressWarnings("unchecked")
			final E unit = (E) unitDefault.constructor();
			units.loadFromXML(unit, elementChild);
		}
		
		final List<Element> listImports = XmlFileManager.getChildrenElementByTagName(element, "import");
		if (!listImports.isEmpty()) { 
			for (Element elementImport : listImports) {
				final String importGroup = elementImport.getAttribute("group");
				final String importName = elementImport.getAttribute("name");
				if (!importGroup.isEmpty()) {
					if (!importName.isEmpty()) {
						importGroupNames.add(importGroup + ":" + importName);
					} else {
						importGroups.add(importGroup);
					}
				} else {
					WarpDrive.logger.warn("Ignoring import with no group definition in import element from " + getFullName());
				}
			}
		}
		
		return true;
	}
	
	@Override
	public int compareTo(@Nonnull Object object) {
		return name.compareTo(((GenericSet) object).name);
	}
	
	@Override
	public String toString() {
		return getFullName() + "(" + (units == null ? "-empty-" : units.elements().size()) + ")";
	}
	
	/**
	 * Adds the units from the given genericSet into this one. Must be pre-finishConstruction()
	 *
	 * @param genericSet
	 *            The genericSet to add from
	 */
	public void loadFrom(GenericSet<E> genericSet) throws InvalidXmlException {
		units.loadFrom(genericSet.units);
	}
	
	/**
	 * Return static import dependencies
	 * 
	 * @return null or a list of group:names to be imported
	 **/
	public Collection<String> getImportGroupNames() {
		return importGroupNames; 
	}
	
	/**
	 * Return dynamic import dependencies
	 * 
	 * @return null or a list of groups to be imported
	 **/
	public Collection<String> getImportGroups() {
		return importGroups; 
	}
}
