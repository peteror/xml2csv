package com.locima.xml2csv.inputparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentException;
import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.StringUtil;

/**
 * Abstracts a list of mappings between XPath statements and Column Names with methods only relevant to this application.
 */
public class MappingConfiguration implements Iterable<IMappingContainer> {

	private static final Logger LOG = LoggerFactory.getLogger(MappingConfiguration.class);

	/**
	 * The list of mappings maintained by this object.
	 */
	private List<IMappingContainer> mappings = new ArrayList<IMappingContainer>();

	private Map<String, String> namespaceMappings = new HashMap<String, String>();

	/**
	 * Adds a child set of mappings to this mappings set.
	 *
	 * @param maps a set of mappings, must not be null and must have a unique {@link MappingList#getOutputName()} value.
	 */
	public void addMappings(IMappingContainer maps) {
		if (maps == null) {
			throw new ArgumentNullException("maps");
		}
		// Ensure that the mapping set name is unique
		String mappingSetName = maps.getOutputName();
		if (mappingSetName == null) {
			throw new ArgumentException("maps", "contains a null name.");
		}
		if (getMappingsByName(maps.getOutputName()) != null) {
			throw new ArgumentException("maps", "must contain a unique name");
		}
		this.mappings.add(maps);
	}

	/**
	 * Adds a namespace prefix to URI mapping that can be used in any descendant mapping.
	 *
	 * @param prefix The prefix that may be used within a descendant mapping. Null indicates default namespace.
	 * @param uri The URI that it maps to. Must not be null.
	 * @throws FileParserException If an attempt is made to reassign an existing prefix/URI mapping to a new URI.
	 */
	public void addNamespaceMapping(String prefix, String uri) throws FileParserException {
		if (StringUtil.isNullOrEmpty(uri)) {
			throw new ArgumentNullException("uri");
		}
		String existingUri = this.namespaceMappings.get(prefix);
		if (existingUri != null) {
			if (uri.equals(existingUri)) {
				LOG.debug("Ignoring duplicate namespace prefix declaration {} -> {}", prefix, uri);
			} else {

				throw new FileParserException(
								"Cannot tolerate the same namespace prefix used for different URIs in mapping config (%s maps to %s and %s", prefix,
								existingUri, uri);
			}
		} else {
			this.namespaceMappings.put(prefix, uri);
		}
	}

	/**
	 * Get a specific set of mappings by name. Generally used for unit testing but might be handy one day.
	 *
	 * @param name the name of the mapping set to return
	 * @return null if a mapping set with that name could not be found.
	 */
	public IMappingContainer getMappingsByName(String name) {
		for (IMappingContainer mapping : this.mappings) {
			if (mapping.getOutputName().equals(name)) {
				return mapping;
			}
		}
		return null;
	}

	/**
	 * Retrieves all "headers" associated with all the mappings.
	 * <p>
	 * The headings are all the output names mapped to the column names that they have. This is useful for initialising all the output files using
	 * {@link com.locima.xml2csv.output.OutputManager#createFiles(Map)}
	 *
	 * @return a map, possibly empty, but never null, or output name to the list of column names.
	 */
	public Map<String, List<String>> getMappingsHeaders() {
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		for (IMappingContainer mapping : this.mappings) {
			List<String> colNames = new ArrayList<String>();
			int colCount = mapping.getColumnNames(colNames);
			LOG.info("Found {} columns in mapping configuration for {}", colCount, this);
			headers.put(mapping.getOutputName(), colNames);
		}
		return headers;
	}

	@Override
	public Iterator<IMappingContainer> iterator() {
		return this.mappings.iterator();
	}

	/**
	 * Gets all the mappings contained within this set as an array.
	 *
	 * @return an array of mappings, possibly empty but never null.
	 */
	public MappingList[] mappingsToArray() {
		return this.mappings.toArray(new MappingList[0]);
	}

	/**
	 * Returns the number of mappings contained in the configuration.
	 *
	 * @return a natural number.
	 */
	public int size() {
		return this.mappings.size();
	}

	public Map<String, String> getNamespaceMap() {
		return this.namespaceMappings;
	}
}
