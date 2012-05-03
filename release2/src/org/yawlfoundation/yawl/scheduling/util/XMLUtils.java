/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.scheduling.util;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.Diff;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.*;


/**
 * 
 * @author tbe
 * @version $Id: java 18313 2010-04-30 10:14:09Z tbe $
 */
public class XMLUtils implements Constants
{
	private static Logger logger = Logger.getLogger(XMLUtils.class);

	private static String schemaFilePathName = "/rup.xsd";
	private static Map<String, Document> schemaDocs = new HashMap<String, Document>();
	static
	{
		schemaDocs.put(schemaFilePathName, null);
		schemaDocs.put("/common.xsd", null);
	}

	private static Validator validator = null;

	private static Validator getValidator() throws IOException, SAXException {
		if (validator == null) {
			SchemaFactory xmlSchemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			File schemafile = new File(schemaFilePathName);
			int idx = schemaFilePathName.lastIndexOf(schemafile.getName());
			String path = schemaFilePathName.substring(0, idx);
			xmlSchemaFactory.setResourceResolver(new XMLUtils().new ResourceResolver(path));
			Reader reader = new StringReader(convertSchemaToString(schemaFilePathName));
			Schema schema = xmlSchemaFactory.newSchema(new StreamSource(reader));
			validator = schema.newValidator();
		}
		return validator;
	}


	public static Collection<Document> getSchemaDocs() {
		for (String pathName : schemaDocs.keySet()) {
			if (schemaDocs.get(pathName) == null) {
				try	{
					schemaDocs.put(pathName, JDOMUtil.stringToDocument(convertSchemaToString(pathName)));
				}
				catch (IOException e) {
					logger.error("cannot parse " + pathName, e);
				}
			}
		}
		return schemaDocs.values();
	}

	/**
	 * return constants defined in XSD enumerations sind als simpleType definiert
	 * 
	 * @return
	 */
	public static List<String> getEnumerationFromSchema(String name) {
		List<String> result = new ArrayList<String>();
		try	{
			String xpath = "//xs:simpleType[@name='" + name + "']/xs:restriction/xs:enumeration";

			for (Document doc : getSchemaDocs()) {
				List<Element> elements = getXMLObjects(doc, xpath);
				for (Element element : elements) {
					result.add(element.getAttributeValue("value"));
				}
				if (!result.isEmpty()) {
					break;
				}
			}
		}
		catch (Exception e)	{
			logger.error("cannot load constants '" + name + "' from XSD", e);
		}
		return result;
	}

	public static String getDefaultFromSchema(String elementName) {
		String attr = getAttributeFromSchema(elementName, "xs:annotation/@perikles:default");
		return attr == null ? "" : attr;
	}

	public static String getUnitFromSchema(String elementName) {
		String attr = getAttributeFromSchema(elementName, "xs:annotation/@perikles:unit");
		return attr == null ? "" : attr;
	}

	public static boolean isVisibleFromSchema(String elementName) {
		String attr = getAttributeFromSchema(elementName, "xs:annotation/@perikles:visible");
		return attr == null || !attr.equalsIgnoreCase("hidden");
	}

	public static boolean isReadonlyFromSchema(String elementName) {
		String attr = getAttributeFromSchema(elementName, "xs:annotation/@perikles:readonly");
		return attr != null && attr.equalsIgnoreCase("true");
	}

	public static boolean isEnumerationFromSchema(String elementName) {
		String attr = getAttributeFromSchema(elementName, "xs:annotation/@perikles:source");
		return attr != null && attr.equalsIgnoreCase("enumeration");
	}

	public static boolean isRequiredFromSchema(String elementName) {
		String attr = getAttributeFromSchema(elementName, "@minOccurs");
		return attr == null || !attr.equals("0");
	}

	private static String getAttributeFromSchema(String elementName, String attributeXPath)
	{
		String value = null;
		try
		{
			String xpath = "//xs:element[@name='" + elementName + "']/" + attributeXPath;

			for (Document doc : getSchemaDocs())
			{
				Attribute attr = getAttribute(doc, xpath);
				if (attr != null)
				{
					value = attr.getValue();
					break;
				}
			}
		}
		catch (Exception e)
		{
			logger.error("cannot load '" + attributeXPath + "' from element '" + elementName + "' from XSD", e);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	public static void setDefaults(Element elem)
	{
		List<Element> childs = (List<Element>) elem.getChildren();
		if (childs.isEmpty())
		{
			elem.setText(getDefaultFromSchema(elem.getName()));
		}
		else
		{
			for (Element child : (List<Element>) elem.getChildren())
			{
				setDefaults(child);
			}
		}
	}

	/**
	 * Internal method that helps to read an XSD file from the file system;
	 * location: where the Java classes are located
	 * 
	 * @param schemaFileName - name of the XML Schema file
	 * @return String representation of the XML Schema data model
	 * @throws IOException
	 */
	private static String convertSchemaToString(String schemaFileName) throws IOException
	{
		InputStream inputStream = null;
		try
		{
			inputStream = Utils.class.getResourceAsStream(schemaFileName); // read only below the classpath

			/*
			 * To convert the InputStream to String we use the
			 * BufferedReader.readLine() method. We iterate until the
			 * BufferedReader returns null which means there's no more data to read.
			 * Each line will be appended to a StringBuilder and returned as String.
			 */
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(streamReader);
			while ((line = bufferedReader.readLine()) != null)
			{
				stringBuilder.append(line).append("\n");
			}

			return stringBuilder.toString();

			// strip off any junk in the prolog before feeding the XML into a
			// parser
			// @see
			// http://mark.koli.ch/2009/02/resolving-orgxmlsaxsaxparseexception-content-is-not-allowed-in-prolog.html
			// return sb.toString().trim().replaceFirst("^([\\W]+)<","<");
		}
		finally
		{
			try
			{
				if (inputStream != null) inputStream.close();
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Datentyp�berpr�fung und angabe des betroffenen elements, es werden alle
	 * gefundenen Fehler ausgegeben Xerces DocumentBuilder method, called by
	 * validator.validate(), isn't thread safe
	 * 
	 * @param msgText
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public static synchronized void validate(String msgText) throws IOException, SAXException
	{
			Validator validator = getValidator();
			SimpleErrorHandler handler = new XMLUtils().new SimpleErrorHandler();
			validator.setErrorHandler(handler);
			validator.validate(new StreamSource(new StringReader(msgText)));

			if (handler.hasErrors())
			{
				throw new SAXException(Utils.toString(handler.getErrorMsgs()));
			}
	}


	public static void validate(Element msg) throws IOException, SAXException {
		validate(Utils.element2String(msg, false));
	}

	/**
	 * validates type of resource utilisation plan against schema element name
	 * TODO@tbe: name of schema element must be unique!
	 * Activity.SurgicalProcedure.Reservation.#1.Resource.Type
	 */
	private static String validateType(Element element, String schemaName, List<Element> restrictions,
			boolean withValidation) throws DatatypeConfigurationException, IOException, JDOMException
	{
		String xpath = "//xs:simpleType[@name='" + schemaName + "']";

		Element typeElement = null;
		for (Document doc : getSchemaDocs())
		{
			typeElement = getElement(doc, xpath);
			if (typeElement != null)
			{
				break;
			}
		}

		if (typeElement == null)
		{
			xpath = "//xs:complexType[@name='" + schemaName + "']";
			for (Document doc : getSchemaDocs())
			{
				typeElement = getElement(doc, xpath);
				if (typeElement != null)
				{
					break;
				}
			}
		}

		if (typeElement == null)
		{
			logger.error("cannot find type in schema, with xpath: " + xpath);
			return null;
		}
		else
		{
			return validateElement(element, typeElement, restrictions, withValidation);
		}
	}

	/**
	 * validates element of resource utilisation plan against type element from
	 * schema return cs class for element
	 */
	public static String validateElement(Element element, Element typeElement, List<Element> restrictions,
			boolean withValidation) throws DatatypeConfigurationException, IOException, JDOMException
	{
		String cssClass = null;

		String type = typeElement.getAttributeValue("type");
		if (type == null)
		{
			type = typeElement.getAttributeValue("base");
			if (typeElement.getName().equals("restriction"))
			{
				restrictions.addAll(typeElement.getChildren());
				// logger.debug("found restrictions: "+Utils.toString(restrictions));
			}
		}

		if (type == null)
		{
			for (Element schemaVarChild : (List<Element>) typeElement.getChildren())
			{
				if (!schemaVarChild.getName().equals("element"))
				{
					cssClass = validateElement(element, schemaVarChild, restrictions, withValidation);
				}
			}
			return cssClass;
		}

		Object o = null;
		if (isDatatypeXSD(type, XSDDatatypes_Int))
		{
			o = getIntegerValue(element, withValidation);
			cssClass = CSS_INTINPUT;
		}
		else if (isDatatypeXSD(type, XSDDatatypes_Long))
		{
			o = getLongValue(element, withValidation);
			cssClass = CSS_INTINPUT;
		}
		else if (isDatatypeXSD(type, XSDDatatypes_Double))
		{
			o = getDoubleValue(element, withValidation);
			cssClass = CSS_INTINPUT;
		}
		else if (isDatatypeXSD(type, XSDDatatypes_DateTime))
		{
			o = getDateValue(element, withValidation);
			cssClass = CSS_DATEINPUT;
		}
		else if (isDatatypeXSD(type, XSDDatatypes_Duration))
		{
			o = getDurationValueInMinutes(element, withValidation);
			cssClass = CSS_DURATIONINPUT;
		}
		else if (isDatatypeXSD(type, XSDDatatypes_Boolean))
		{
			o = getBooleanValue(element, withValidation);
			cssClass = CSS_BOOLEANINPUT;
		}
		else if (isDatatypeXSD(type, XSDDatatypes_String))
		{
			o = getStringValue(element, withValidation);
			cssClass = CSS_TEXTINPUT;
		}
		else
		{
			cssClass = validateType(element, type, restrictions, withValidation);
		}

		validateRestrictions(element, type, o, restrictions, withValidation);

		return cssClass;
	}

	private static boolean isDatatypeXSD(String type, String[] xsdDatatypes)
	{
		for (String xsdType : xsdDatatypes)
		{
			if (type.equals("xs:" + xsdType))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * check restrictions given in XSD
	 * 
	 * @param o
	 * @param restrictions
	 */
	private static void validateRestrictions(Element element, String type, Object o, List<Element> restrictions,
			boolean withValidation) throws DatatypeConfigurationException
	{
		// logger.debug(element.getName() + ": " + o +
		// ", "+Utils.toString(restrictions));
		if (o == null)
		{
			return;
		}

		// extra handling for enumeration
		Boolean enumerationError = null;

		for (Element restriction : restrictions)
		{
			String value = restriction.getAttributeValue("value");
			if (restriction.getName().equals("enumeration"))
			{
				if (enumerationError == null)
				{
					enumerationError = true;
				}
				if (enumerationError && value.equals(o.toString()))
				{
					enumerationError = false;
				}

			}
			else if (restriction.getName().equals("length"))
			{
				if (Integer.valueOf(value) != o.toString().length())
				{
					addErrorValue(element, withValidation, "msgLengthInvalid", new String[] { value });
				}

			}
			else if (restriction.getName().equals("maxLength"))
			{
				if (Integer.valueOf(value) < o.toString().length())
				{
					addErrorValue(element, withValidation, "msgLengthLowerEquals", new String[] { value });
				}

			}
			else if (restriction.getName().equals("minLength"))
			{
				if (Integer.valueOf(value) > o.toString().length())
				{
					addErrorValue(element, withValidation, "msgLengthGreaterEquals", new String[] { value });
				}

			}
			else if (restriction.getName().equals("maxExclusive"))
			{
				String vStr = getIntegerInterpretation(value, type);
				if (Integer.valueOf(vStr) <= Integer.valueOf(o.toString()))
				{
					addErrorValue(element, withValidation, "msgLower", new String[] { vStr });
				}

			}
			else if (restriction.getName().equals("minExclusive"))
			{
				String vStr = getIntegerInterpretation(value, type);
				if (Integer.valueOf(vStr) >= Integer.valueOf(o.toString()))
				{
					addErrorValue(element, withValidation, "msgGreater", new String[] { vStr });
				}

			}
			else if (restriction.getName().equals("maxInclusive"))
			{
				String vStr = getIntegerInterpretation(value, type);
				if (Integer.valueOf(vStr) < Integer.valueOf(o.toString()))
				{
					addErrorValue(element, withValidation, "msgLowerEquals", new String[] { vStr });
				}

			}
			else if (restriction.getName().equals("minInclusive"))
			{
				String vStr = getIntegerInterpretation(value, type);
				if (Integer.valueOf(vStr) > Integer.valueOf(o.toString()))
				{
					addErrorValue(element, withValidation, "msgGreaterEquals", new String[] { vStr });
				}

			}
			else if (restriction.getName().equals("whiteSpace"))
			{
				// nothing to do

			}
			else
			{
				// TODO@tbe pattern, fractionDigits, totalDigits
				logger.error("restriction '" + restriction.getName() + "' is not yet implemented");
			}
		}

		if (enumerationError != null && enumerationError)
		{
			addErrorValue(element, withValidation, "msgUnknownValue");
		}
	}

	/**
	 * interpretes value as Integer, depending of type
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	private static String getIntegerInterpretation(String value, String type)
	{
		// logger.debug("value: " + value + ", type: " + type);
		if (isDatatypeXSD(type, XSDDatatypes_Duration))
		{
			return Utils.stringXMLDuration2stringMinutes(value);
		}
		else
		{
			return value;
		}
	}

	class SimpleErrorHandler implements ErrorHandler
	{
		private ArrayList<String> errors = new ArrayList<String>();

		public void warning(SAXParseException e) throws SAXException
		{
			// logger.debug("WARN : " + e.getMessage());
		}

		public void error(SAXParseException e) throws SAXException
		{
			// logger.debug("ERROR: " + e.getMessage());
			errors.add(e.getMessage());
		}

		public void fatalError(SAXParseException e) throws SAXException
		{
			// logger.debug("FATAL: " + e.getMessage());
			errors.add(e.getMessage());
		}

		public boolean hasErrors()
		{
			return !errors.isEmpty();
		}

		/**
		 * returns complete xsd error message, e.g.
		 * "cvc-datatype-valid.1.2.1: 'x' is not a valid value for 'integer'."
		 * 
		 * @return
		 */
		public List<String> getErrorsFull()
		{
			return errors;
		}

		/**
		 * returns only message of xsd error, e.g.
		 * "'x' is not a valid value for 'integer'."
		 * 
		 * @return
		 */
		public List<String> getErrorMsgs()
		{
			ArrayList<String> errorMsgs = new ArrayList<String>();
			for (String msg : errors)
			{
				errorMsgs.add(msg.substring(msg.indexOf(":") + 2));
			}
			return errorMsgs;
		}

		/**
		 * returns messages, which are not datatype errors private method, because
		 * its experimental. doesn't work because there are any further xsd error
		 * prefixes
		 * 
		 * @return
		 */
		private List<String> getErrorMsgsWithoutDatatypeCheck()
		{
			ArrayList<String> errorMsgs = new ArrayList<String>();
			for (String msg : errors)
			{
				if (!msg.startsWith("cvc-datatype-valid") && !msg.startsWith("cvc-maxInclusive-valid"))
				{
					errorMsgs.add(msg.substring(msg.indexOf(":") + 2));
				}
			}
			return errorMsgs;
		}

	}

	public static String getErrorValue(Element e)
	{
		return getAttributeValue(e, XML_ERROR);
	}

	public static String getWarningValue(Element e)
	{
		return getAttributeValue(e, XML_WARNING);
	}

	private static String getAttributeValue(Element e, String attr)
	{
		return e.getAttribute(attr) == null ? null : e.getAttribute(attr).getValue();
	}

	public static void addErrorValue(Element e, boolean withValidation, String value, String... args)
	{
		// logger.debug(e.getName()+"='"+e.getText()+"', isRequiredFromSchema(e.getName())="
		// + isRequiredFromSchema(e.getName())+", withValidation=" +
		// withValidation+", value=" + value);
		if (!e.getText().isEmpty())
		{
			addAttributeValue(e, XML_ERROR, value, args); // malformed input ->
																			// error
		}
		else if (isRequiredFromSchema(e.getName()))
		{
			if (withValidation)
			{
				addAttributeValue(e, XML_ERROR, value, args); // missing input ->
																				// error
			}
			else
			{
				// addWarningValue(e, value, args); // missing input, but don't
				// validate
			}
		}
	}

	public static void addWarningValue(Element e, String value, String... args)
	{
		addAttributeValue(e, XML_WARNING, value, args);
	}

	public static void addAttributeValue(Element e, String attr, String value, String... args)
	{
		// logger.debug("e="+e+", attr=" + attr+", value=" + value+", args=" +
		// args);
		try
		{
			String json = e.getAttribute(attr) == null ? "[]" : e.getAttribute(attr).getValue();
            if (!json.startsWith("[")) json = String.format("[%s]", json);
			JSONArray jsonArr = new JSONArray(json);

			// value already exist?
			for (int i = 0; i < jsonArr.length(); i++)
			{
				JSONObject jsonObj = jsonArr.getJSONObject(i);
				if (jsonObj.has(value))
				{
					// logger.debug("AttributeValue already exist: " + value);
					return;
				}
			}

			Map valueAndArgs = new TreeMap();
			valueAndArgs.put(value, args);

			JSONObject jsonObj = new JSONObject(valueAndArgs);
			jsonArr.put(jsonObj);

			e.setAttribute(attr, jsonArr.toString());
		}
		catch (Exception e1)
		{
			logger.error("cannot add Attribute", e1);
		}
	}

	public static void removeAttributes(Element element, String attr)
	{
		removeAttribute(element, attr);
		for (Element child : (List<Element>) element.getChildren())
		{
			removeAttribute(child, attr);
		}
	}

	public static void removeAttribute(Element e, String attr)
	{
		try
		{
			e.removeAttribute(attr);
		}
		catch (Exception e1)
		{
			logger.error("cannot remove Attribute", e1);
		}
	}

	public static boolean hasErrors(Element element)
	{
		return hasAttributeValues(element, XML_ERROR);
	}

	public static boolean hasWarnings(Element element)
	{
		return hasAttributeValues(element, XML_WARNING);
	}

	private static boolean hasAttributeValues(Element element, String attr)
	{
		String value = getAttributeValue(element, attr);
		// logger.debug("element: "+element.getName() + "." + attr+" = '" + value
		// + "'");
		if (value != null)
		{
			return true;
		}
		else
		{
			for (Element child : (List<Element>) element.getChildren())
			{
				if (hasAttributeValues(child, attr))
				{
					return true;
				}
			}
		}
		return false;
	}

	public static Set<String> getErrors(Element element)
	{
		return getAttributeValues(element, XML_ERROR);
	}

	private static Set<String> getAttributeValues(Element element, String attr)
	{
		Set<String> values = new HashSet<String>();
		String value = getAttributeValue(element, attr);
		// logger.debug("element: "+element.getName() + "." + attr+" = '" + value
		// + "'");
		if (value != null)
		{
			values.add(value);
		}

		for (Element child : (List<Element>) element.getChildren())
		{
			values.addAll(getAttributeValues(child, attr));
		}

		return values;
	}

	public static Date getDateValue(Element element, boolean withValidation)
	{
		try
		{
			return Utils.string2Date(element.getText(), Utils.DATETIME_PATTERN_XML);
		}
		catch (Exception e)
		{
			// logger.error("'" + element.getName() + "' must be datetime, " +
			// e.getMessage());
			if (element == null){
				logger.debug("#####  element is NULL  #####");
			}
				
			addErrorValue(element, withValidation, "msgDateError");
			return null;
		}
	}

	public static void setDateValue(Element element, Date value)
	{
		try
		{
			setStringValue(element, Utils.date2String(value, Utils.DATETIME_PATTERN_XML));
		}
		catch (Exception e)
		{
			logger.error("'" + element.getName() + "' must be datetime, " + e.getMessage());
		}
	}

	public static void setDurationValue(Element element, long value)
	{
		try
		{
			DatatypeFactory df = DatatypeFactory.newInstance();
			Duration dur = df.newDuration(value);
			setStringValue(element, String.valueOf(dur.toString()));
		}
		catch (Exception e)
		{
			logger.error("'" + element.getName() + "' must be Duration, " + e.getMessage());
		}
	}

	public static void setLongValue(Element element, Long value)
	{
		try
		{
			setStringValue(element, String.valueOf(value));
		}
		catch (Exception e)
		{
			logger.error("'" + element.getName() + "' must be Long, " + e.getMessage());
		}
	}

	public static String getStringValue(Element element, boolean withValidation)
	{
		if (element.getText().isEmpty())
		{
			addErrorValue(element, withValidation, "msgStringError");
		}
		return element.getText();
	}

	public static void setStringValue(Element element, String value)
	{
		element.setText(value);
		XMLUtils.removeAttribute(element, XML_WARNING);
		XMLUtils.removeAttribute(element, XML_ERROR);
	}

	public static Duration getDurationValue(Element element, boolean withValidation)
	{
		try
		{
			// logger.debug("element.getText():" + element.getText());
			Duration d = Utils.stringXML2Duration(element.getText());
			return d;
		}
		catch (DatatypeConfigurationException e)
		{
			logger.error("wrong DatatypeConfiguration", e);
			addErrorValue(element, withValidation, "msgTechnicalError");
			return null;
		}
		catch (Exception e)
		{
			// logger.error("'" + element.getName() + "' must be duration", e);
			addErrorValue(element, withValidation, "msgDurationError");
			return null;
		}
	}

	public static Integer getDurationValueInMinutes(Element element, boolean withValidation)
	{
		try
		{
			Duration d = getDurationValue(element, withValidation);
			return Utils.duration2Minutes(d);
		}
		catch (Exception e)
		{
			// logger.error("'" + element.getName() + "' must be duration", e);
			addErrorValue(element, withValidation, "msgIntegerError");
			return null;
		}
	}

	public static Double getDoubleValue(Element element, boolean withValidation)
	{
		try
		{
			return Double.parseDouble(element.getText());
		}
		catch (Exception e)
		{
			// logger.error("'" + element.getName() + "' must be double, " +
			// e.getMessage());
			addErrorValue(element, withValidation, "msgDoubleError");
			return null;
		}
	}

	public static Integer getIntegerValue(Element element, boolean withValidation)
	{
		try
		{
			return Integer.parseInt(element.getText());
		}
		catch (Exception e)
		{
			// logger.error("'" + element.getName() + "' must be integer, " +
			// e.getMessage());
			addErrorValue(element, withValidation, "msgIntegerError");
			// logger.error("-----------element '" + element.getName() + "' = " +
			// Utils.object2String(element));
			return null;
		}
	}

	public static Long getLongValue(Element element, boolean withValidation)
	{
		try
		{
			return Long.parseLong(element.getText());
		}
		catch (Exception e)
		{
			// logger.error("'" + element.getName() + "' must be long, " +
			// e.getMessage());
			addErrorValue(element, withValidation, "msgIntegerError");
			// logger.error("-----------element '" + element.getName() + "' = " +
			// Utils.object2String(element));
			return null;
		}
	}

	public static Boolean getBooleanValue(Element element, boolean withValidation)
	{
		try
		{
			String b = element.getText();
			if (b.equals("0") || b.equalsIgnoreCase("false"))
			{
				return Boolean.FALSE;
			}
			if (b.equals("1") || b.equalsIgnoreCase("true"))
			{
				return Boolean.TRUE;
			}
			throw new Exception();
		}
		catch (Exception e)
		{
			addErrorValue(element, withValidation, "msgBooleanError");
			return null;
		}
	}

	public static String getXPATH_RUP()
	{
		return "/" + XML_RUP;
	}

	public static String getXPATH_Activities(String... activityNames)
	{
		String xpath = "";
		if (activityNames != null)
		{
			for (String activityName : activityNames)
			{
				if (activityName == null)
				{
					continue;
				}

				if (!xpath.isEmpty())
				{
					xpath += " or ";
				}
				xpath += XML_ACTIVITYNAME + "='" + activityName + "'";
			}
		}
		return getXPATH_RUP() + "/" + XML_ACTIVITY + (xpath.isEmpty() ? "" : "[" + xpath + "]");
	}

	public static String getXPATH_ActivitiesElement(String[] activityNames, String elementName, Integer index)
	{
		return getXPATH_Activities(activityNames) + "/" + elementName + (index == null ? "" : "[" + index + "]");
	}

	public static String getXPATH_ActivityElement(String activityName, String elementName, Integer index)
	{
		return getXPATH_Activities(activityName) + "/" + elementName + (index == null ? "" : "[" + index + "]");
	}

	public static String getXPATH_ResOrUtilElement(String activityName, String resOrUtilName, Integer resOrUtilIdx,
			String elementName)
	{
		return getXPATH_ActivityElement(activityName, resOrUtilName, resOrUtilIdx) + "/" + elementName;
	}

	public static String getXPATH_Resource(String activityName, Integer resIdx)
	{
		return getXPATH_ActivityElement(activityName, XML_RESERVATION, resIdx) + "/" + XML_RESOURCE;
	}

	public static String getXPATH_ResourceElement(String activityName, Integer resIdx, String elementName)
	{
		return getXPATH_Resource(activityName, resIdx) + "/" + elementName;
	}

	public static String getXPATH_ResourceElement(String activityName, Integer resIdx, String elementName,
			String elementText)
	{
		return getXPATH_ResourceElement(activityName, null, elementName) + "[text()='" + elementText + "']";
	}

	public static boolean different(Element e1, Element e2) throws IOException, SAXException
	{
		// logger.debug("e1="+Utils.element2String(e1, false));
		// logger.debug("e2="+Utils.element2String(e2, false));
		Diff diff = new Diff(Utils.element2String(e1, false), Utils.element2String(e2, false));
		boolean similiar = diff.similar();
		// logger.debug("identical="+diff.identical()+", similar="+similiar);
		return !similiar;
	}

	/**
	 * compares textual content of two elements which are get with
	 * element.getChildText() content of a non-existing element is equal to
	 * content of an element without text
	 * 
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static boolean isEqualXMLText(String t1, String t2)
	{
		if (t1 == null && t2 == null)
		{
			return true;
		}
		else if (t1 == null)
		{
			return t2.isEmpty();
		}
		else if (t2 == null)
		{
			return t1.isEmpty();
		}
		else
		{
			return t1.equals(t2);
		}
	}

	@SuppressWarnings("unchecked")
	public static List getXMLObjects(Document doc, String xpath)
	{
		List objects = new ArrayList();
		try
		{
			XPath xp = XPath.newInstance(xpath);
			objects = xp.selectNodes(doc);
		}
		catch (Exception e)
		{
			logger.error("cannot process xpath: " + xpath + " on document: "
					+ Utils.element2String(doc == null ? null : doc.getRootElement(), true));
		}
		return objects;
	}

	@SuppressWarnings("unchecked")
	private static Object getFirstXMLObject(Document doc, String xpath)
	{
		List objects = getXMLObjects(doc, xpath);
		return objects.isEmpty() ? null : objects.get(0);
	}

	public static List<Element> getElements(Document doc, String xpath)
	{
		return (List<Element>) getXMLObjects(doc, xpath);
	}

	public static Element getElement(Document doc, String xpath)
	{
		return (Element) getFirstXMLObject(doc, xpath);
	}

	public static Attribute getAttribute(Document doc, String xpath)
	{
		return (Attribute) getFirstXMLObject(doc, xpath);
	}



	/**
	 * set childText on elem's child, if child doesn't exist, it will be created
	 * 
	 * @param elem
	 * @param childName
     * @param childText
	 */
	public static void setChildText(Element elem, String childName, String childText)
	{
		Element child = elem.getChild(childName);
		if (child == null)
		{
			child = new Element(childName);
			elem.addContent(child);
		}
		child.setText(childText);
	}

	/**
	 * merges content of two elements recursively into element minor following
	 * content will be copied: Text, Element, Attribute if conflicts, minor will
	 * be overwrite with content of major
	 * 
	 * @param minor
	 * @param major
	 */
	public static boolean mergeElements(Element minor, Element major) throws Exception
	{
		// logger.debug("minor: " + Utils.element2String(minor, false));
		// logger.debug("major: " + Utils.element2String(major, false));
		boolean changed = false;
		if (minor == null)
		{
			minor = major;
			// logger.debug("set minor = " + Utils.element2String(major, false));
			changed = true;
		}
		else if (major != null)
		{
			if (!minor.getText().equals(major.getText()))
			{
				minor.setText(major.getText());
				// logger.debug("minor.setText("+major.getText()+")");
				changed = true;
			}

			for (Attribute a : (List<Attribute>) major.getAttributes())
			{
				Attribute aCopy = (Attribute) Utils.deepCopy(a);
				if (minor.getAttribute(a.getName()) == null || !minor.getAttributeValue(a.getName()).equals(a.getValue()))
				{
					minor.setAttribute(aCopy.detach());
					// logger.debug("minor.setAttribute("+Utils.toString(a)+")");
					changed = true;
				}
			}

			for (Element e : (List<Element>) major.getChildren())
			{
				Element eCopy = (Element) Utils.deepCopy(e);
				List<Element> minorChildren = minor.getChildren(e.getName());
				// logger.debug("minorChildren: " + Utils.toString(minorChildren));
				// logger.debug("e: " + Utils.toString(e));
				Element firstInList = existInList(minorChildren, e);
				if (firstInList == null)
				{
					// logger.debug("minor.addContent: " +
					// Utils.toString(eCopy.detach()));
					minor = minor.addContent(eCopy.detach());
					// logger.debug("minor.addContent("+Utils.element2String(e,
					// false)+")");
					changed = true;
				}
				else
				{
					changed = mergeElements(firstInList, eCopy) || changed;
				}
			}
		}

		return changed;
	}

	/**
	 * returns all activity names from element e recursively
	 * 
	 * @param e
	 * @return
	 */
	public static Set<String> getActivityNames(Element e)
	{
		Set<String> activityNames = new HashSet<String>();
		if (e == null) return activityNames;

		if (e.getName().equals(XML_ACTIVITY))
		{
			activityNames.add(e.getChildText(XML_ACTIVITYNAME));
		}
		for (Element child : (List<Element>) e.getChildren())
		{
			activityNames.addAll(getActivityNames(child));
		}
		return activityNames;
	}

	public static String getCaseId(Document rup)
	{
		String xpath = getXPATH_RUP() + "/" + XML_CASEID;
		String caseId = getElement(rup, xpath).getText();
		return caseId;
	}

	/**
	 * find earliest FROM element of rup
	 */
	public static Element getEarliestBeginElement(Document rup, String[] possibleActivities)
	{
		Element earliestFromElem = null;
		String xpath = getXPATH_ActivitiesElement(possibleActivities, XML_FROM, null);
		List<Element> fromElems = getXMLObjects(rup, xpath);
		for (Element fromElem : fromElems)
		{
			Date earliestFrom = earliestFromElem == null ? null : getDateValue(earliestFromElem, true);
			Date from = getDateValue(fromElem, true);
			// logger.debug("earlFrom=" + earlFrom + ", from=" + from);
			if (earliestFrom == null || (from != null && earliestFrom.after(from)))
			{
				earliestFromElem = fromElem;
			}
		}
//		logger.debug("rup: " + rup.toString());
//		logger.debug("earlFromElem: " + earliestFromElem.toString());
		return earliestFromElem;
	}

	/**
	 * find earliest FROM date of rup
	 */
	public static Date getEarliestBeginDate(Document rup)
	{
		return getDateValue(getEarliestBeginElement(rup, null), true);
	}

	/**
	 * find latest TO element of rup
	 */
	public static Element getLatestEndElement(Document rup, String[] possibleActivities)
	{
		Element lateToElem = null;
		String xpath = getXPATH_ActivitiesElement(possibleActivities, XML_TO, null);
		List<Element> toElems = getXMLObjects(rup, xpath);
		for (Element toElem : toElems)
		{
			Date lateTo = lateToElem == null ? null : getDateValue(lateToElem, true);
			Date to = getDateValue(toElem, true);
			if (lateTo == null || (to != null && lateTo.before(to)))
			{
				lateToElem = toElem;
			}
		}
		return lateToElem;
	}

	/**
	 * find latest TO date of rup
	 */
	public static Date getLatestEndDate(Document rup)
	{
		return getDateValue(getLatestEndElement(rup, null), true);
	}

	/**
	 * return first element from list which is equal (in context of perikles) to
	 * e
	 * 
	 * @param list
	 * @param e
	 * @return
	 */
	private static Element existInList(List<Element> list, Element e)
	{
		for (Element listElem : list)
		{
			if (elementsEqual(listElem, e))
			{
				return listElem;
			}
		}
		return null;
	}

	/**
	 * return true if e1 is equal (in context of perikles) to element e2
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	private static boolean elementsEqual(Element e1, Element e2)
	{
		String s1 = e1.getName(), s2 = e2.getName();

		if (!s1.equals(s2))
		{
			return false;
		}
		else if (s1.equals(XML_ACTIVITY))
		{
			s1 = e1.getChildText(XML_ACTIVITYNAME);
			s2 = e2.getChildText(XML_ACTIVITYNAME);
		}
		else if (s1.equals(XML_RESERVATION))
		{
			try
			{
				s1 = e1.getChild(XML_RESOURCE).getChildText(XML_ID);
				s2 = e2.getChild(XML_RESOURCE).getChildText(XML_ID);
			}
			catch (Exception e)
			{
				s1 = null;
				s2 = null;
			}
		}
		else if (s1.equals(XML_UTILISATIONREL))
		{
			try
			{
				s1 = e1.getChildText(XML_THISUTILISATIONTYPE) + e1.getChildText(XML_OTHERACTIVITYNAME);
				s1 += e1.getChildText(XML_OTHERUTILISATIONTYPE);
				s2 = e2.getChildText(XML_THISUTILISATIONTYPE) + e2.getChildText(XML_OTHERACTIVITYNAME);
				s2 += e2.getChildText(XML_OTHERUTILISATIONTYPE);
			}
			catch (Exception e)
			{
				s1 = null;
				s2 = null;
			}
		}

		if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty())
		{ // nicht entscheidbar
			return false;
		}
		else
		{
			return s1.equals(s2);
		}
	}

	/**
	 * Workaround for bug in Xerces, @see
	 * https://issues.apache.org/jira/browse/XERCESJ-1130 therefore we cannot
	 * include other schemas. Original workaround can be found here:
	 * 
	 * see http
	 *      ://stackoverflow.com/questions/1094893/validate-an-xml-file-against
	 *      -multiple-schema-definitions
	 * see http://pbin.oogly.co.uk/listings/viewlistingdetail/2
	 *      a70d763929ce3053085bfaa1d78e2
	 * 
	 * @author Jon (oogly.co.uk)
	 */
	private class ResourceResolver implements LSResourceResolver {
		String _path;

		ResourceResolver(String path) {
			_path = path;
		}


		public LSInput resolveResource(String type, String namespaceURI, String publicId,
                                       String systemId, String baseURI) {
			InputStream resourceAsStream = null;
			try {
				String filename = _path + systemId;
				resourceAsStream = this.getClass().getResourceAsStream(filename);
			}
			catch (Exception e) {
				logger.error("cannot load schema: " + systemId, e);
			}
			return new LSInputImpl(publicId, systemId, resourceAsStream);
		}


		protected class LSInputImpl implements LSInput {

			private String _publicId;
			private String _systemId;
            private BufferedInputStream _inputStream;

            public LSInputImpl(String publicId, String sysId, InputStream input) {
                _publicId = publicId;
                _systemId = sysId;
                _inputStream = new BufferedInputStream(input);
            }

			public String getPublicId() { return _publicId; }

			public void setPublicId(String publicId) { _publicId = publicId; }

            public String getSystemId() { return _systemId;	}

            public void setSystemId(String systemId) { _systemId = systemId; }

            public BufferedInputStream getInputStream() { return _inputStream; }

            public void setInputStream(BufferedInputStream inputStream) {
                _inputStream = inputStream;
            }

			public String getStringData() {
				synchronized (_inputStream) {
					try	{
						byte[] input = new byte[_inputStream.available()];
						_inputStream.read(input);
						return new String(input);
					}
					catch (IOException e) {
						e.printStackTrace();
						System.out.println("Exception " + e);
						return null;
					}
				}
			}

            // required implementations
			public String getBaseURI() { return null; }
			public InputStream getByteStream() { return null; }
			public boolean getCertifiedText() {	return false; }
			public Reader getCharacterStream() { return null; }
			public String getEncoding()	{ return null; }
			public void setBaseURI(String baseURI) { }
			public void setByteStream(InputStream byteStream) {	}
			public void setCertifiedText(boolean certifiedText) { }
			public void setCharacterStream(Reader characterStream) { }
			public void setEncoding(String encoding) { }
			public void setStringData(String stringData) { }
		}

	}

}
