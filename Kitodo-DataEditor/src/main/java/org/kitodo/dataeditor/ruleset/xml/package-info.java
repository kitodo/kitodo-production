/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */
/**
 * The classes in this package can be used by the JAXB parser to read the
 * ruleset XML file.
 *
 * <p>
 * <b>What is the ruleset?</b>
 *
 * <p>
 * The ruleset is the heart of Production. It describes the elements from which
 * the structure of the digitally represented cultural work can exist, and in
 * what form metadata for describing the structure can be entered. Technically,
 * this means that it determines which input masks the web interface displays.
 * This part has always been a bunch of XML files. Therefore, the rule set is
 * also maintained in the current version as an XML file, because otherwise you
 * would need a whole set of different tables in the database to manage this.
 *
 * <p>
 * <b>Structure</b>
 *
 * <p>
 * The rule set is divided into three conceptually separated sections (in this
 * order):
 *
 * <dl>
 * <dt>{@code <declaration>}</dt>
 * <dd>Announcement of the elements for structuring the digitally represented
 * cultural work and of the possible metadata keys for the input of
 * metadata.</dd>
 * <dt>{@code <correlation>}</dt>
 * <dd>Explanation of rules on how these elements may or may not be linked.</dd>
 * <dt>{@code <editing>}</dt>
 * <dd>Settings of the graphic editor, insofar as they relate to the specific
 * metadata keys.</dd>
 * </dl>
 *
 * <p>
 * <b>The {@code <declaration>} section</b>
 *
 * <p>
 * The declarations section defines
 * <ol>
 * <li>the outline elements from which the tree-shaped outline of the digital
 * representation of the cultural work can be constructed, and
 * <li>the metadata keys that can be used to meaningfully describe these
 * structures.
 * </ol>
 * From a technical point of view,
 * “<a href="https://en.wikipedia.org/wiki/RDF_Schema#Classes">types</a>”, in
 * Java speech <a href=
 * "https://www.w3.org/TR/rdf-schema/#ch_subclassof">sub-<code>interface</code>s</a>,
 * are defined here.
 * <ul>
 * <li>Each {@code <division>} is a sub-<code>interface</code> of the <a href=
 * "https://www.loc.gov/standards/mets/docs/mets.v1-9.html#div">{@code <mets:div>}</a>
 * interface. Hence the name.
 * <li>Each {@code <key>} is either
 * <ul>
 * <li>a sub-<code>interface</code> to the {@code <kitodo:metadata>} interface
 * and at the same time a sub-interface to an
 * <a href="https://www.w3.org/TR/xmlschema-2/">XML Schema data type
 * primitive</a> (most string), or
 * <li>a sub-<code>interface</code> to the {@code <kitodo:metadataGroup>}
 * interface and at the same time a sub-<code>interface</code> to
 * <a href="https://www.w3.org/TR/rdf-schema/#ch_bag">{@code rdf:Bag}</a>.
 * </ul>
 * </ul>
 *
 * <p>
 * For example, perhaps the simplest variant of a rule set might look like this.
 * It has only the {@code declaration} section. With this rule set only books
 * can be cataloged. For each book author, year, title, publisher and place of
 * publication can be recorded. Otherwise there are no further requirements.
 *
 * <pre>
 * {@code
 * <ruleset>
 *     <declaration>
 *         <division id="book">
 *             <label>Book</label>
 *         </division>
 *         <key id="author">
 *             <label>Author</label>
 *         </key>
 *         <key id="publicationYear">
 *             <label>Publication year</label>
 *         </key>
 *         <key id="titleDocMain">
 *             <label>Main title</label>
 *         </key>
 *         <key id="publisherName">
 *             <label>Publisher</label>
 *         </key>
 *         <key id="placeOfPublication">
 *             <label>Place of publication</label>
 *         </key>
 *     </declaration>
 * </ruleset>
 * }
 * </pre>
 *
 * <p>
 * But of course the ruleset is much more powerful. You can specify a domain for
 * the keys. This is passed on to the outside and then determines in which
 * container they are stored in the file, of which there are six different ones.
 * Then the label. It translates into several languages. This applies to
 * divisions as well as keys:
 *
 * <pre>
 * {@code
 * <division id="book">
 *     <label>Book</label>
 *     <label lang="de">Buch</label>
 *     <label lang="fr">Livre</label>
 * </division>
 * <key id="publicationYear" domain="source">
 *     <label>Publication year</label>
 *     <label lang="de">Erscheinungsjahr</label>
 *     <label lang="fr">Année de publication</label>
 * </key>
 * }
 * </pre>
 *
 * <p>
 * For divisions, that was it, but there are many more options for keys. For
 * keys, the codomain can be specified, that is, from what data type they are a
 * sub-interface. Currently the values {@code anyURI}, {@code boolean},
 * {@code date}, {@code integer} and {@code string} are supported. URIs can be
 * restricted to a namespace. Without specification, the codomain is first
 * {@code string}, or, if a namespace was specified, {@code anyURI}.
 *
 *
 * <pre>
 * {@code
 * <key id="publicationYear">
 *     <label>Publication year</label>
 *     <codomain type="integer"/>
 * </key>
 * <key id="language">
 *     <label>Language</label>
 *     <codomain namespace="http://id.loc.gov/vocabulary/iso639-2/"/>
 * </key>
 * }
 * </pre>
 *
 * <p>
 * With the element {@code <option>}, enumeration types can be defined. Options
 * can have labels, also multilingual. Without a label, the value itself is
 * displayed. This can be used, for example, to implement digital collections:
 *
 * <pre>
 * {@code
 * <key id="singleDigCollection">
 *     <label>Digital collection</label>
 *     <option value="Collection 1"/>
 *     <option value="two">
 *         <label>Collection 2</label>
 *     </option>
 *     <option value="three">
 *         <label>Collection 3</label>
 *         <label lang="de">Kollektion 3</label>
 *     </option>
 * </key>
 * }
 * </pre>
 *
 * {@code <pattern>} lets you specify a regular expression for the format of the
 * value. The pattern can also be combined with data types. It then refers to
 * the representation as a string.
 *
 * <pre>
 * &lt;key id="issn">
 *     &lt;label>ISSN&lt;/label>
 *     &lt;pattern>\d{4}-\d{3}[0-9X]&lt;/pattern>
 * &lt;/key>
 * &lt;key id="publicationYear">
 *     &lt;label>Publication year&lt;/label>
 *     &lt;codomain type="integer"/>
 *     &lt;pattern>\d{4}&lt;/pattern>
 * &lt;/key>
 * </pre>
 *
 * <p>
 * With {@code <preset>}, you can specify a value that is pre-filled when
 * creating an entry with this metadata key.
 *
 * <pre>
 * {@code
 * <key id="singleDigCollection">
 *     <label>Digital collection</label>
 *     <option value="one"/>
 *     <option value="two/>
 *     <option value="three"/>
 *     <preset>one</preset>
 *     <preset>three</preset>
 * </key>
 * }
 * </pre>
 *
 * <p>
 * Keys can be grouped. Here is an example of a natural person as contributor
 * with an indication of the role in relation to the work:
 *
 * <pre>
 * {@code
 * <key id="contributor">
 *     <label>Contributor</label>
 *     <key id="role">
 *         <label>Role</label>
 *         <codomain namespace="http://id.loc.gov/vocabulary/relators/"/>
 *     </key>
 *     <key id="identifier">
 *         <label>Identifier</label>
 *         <codomain namespace="http://d-nb.info/gnd/"/>
 *     </key>
 *     <key id="givenName">
 *         <label>Given name</label>
 *     </key>
 *     <key id="surname">
 *         <label>Surname</label>
 *     </key>
 * </key>
 * }
 * </pre>
 *
 * <p>
 * <b>The {@code <correlation>} section</b>
 *
 * <p>
 * The difference between {@code <declaration>} and {@code <correlation>} is a
 * bit like the difference between well-formedness and validity in XML files.
 * {@code <declaration>} defines requirements for the well-formedness of the
 * data. If I define a metadata key that should be an integer, then “{@code 19th
 * century}” is not a valid value for that key, nor “{@code 18??}”. Data that
 * does not meet these minimum requirements should not make it into any data
 * store, but should be rejected directly from the interface. The validity
 * criteria go beyond these minimum requirements and, for example, specify that
 * for each phonographic record, the revolution speed must be entered, but no
 * revolution speed can be entered for a book. While {@code <declaration>} is
 * necessary, {@code <correlation>} is optional. The {@code <restriction>} and
 * {@code <permit>} rules in the {@code <correlation>} section have the
 * attribute {@code unspecified}. This specifies what happens to divisions,
 * keys, and options that are not usually explicitly named: either they are
 * {@code unrestricted}, or they are {@code forbidden}. You can also specify
 * quantities here ({@code minOccurs} and {@code maxOccurs}). And the rules
 * define the display order of the elements. You can also drag items up and the
 * others will just show below.
 *
 * <p>
 * <b>The {@code <editing>} section</b>
 *
 * <p>
 * One can discuss whether the editing settings belong to the rule set at all.
 * However, since they refer directly to the metadata keys declared at the
 * outset, they are also recorded here because otherwise there would be
 * references between several files, which would more easily lead to errors.
 * There are two levels of settings: general and specific, which apply only to
 * one acquisition stage. The specific settings then apply before the general,
 * and if there are none, the default applies.
 *
 * <p>
 * In the tests, there is a fairly detailed ruleset as an example of what is
 * possible. It also explains all parameters. An XSD file for the ruleset is
 * provided in the resources of this module.
 *
 * @author Matthias Ronge
 */
package org.kitodo.dataeditor.ruleset.xml;
