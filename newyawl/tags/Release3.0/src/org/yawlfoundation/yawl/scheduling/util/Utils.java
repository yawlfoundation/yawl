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

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Position;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.servlet.http.HttpSession;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils implements Constants {

	private static XMLOutputter format = new XMLOutputter(Format.getPrettyFormat());
	private static XMLOutputter compact = new XMLOutputter(Format.getCompactFormat());

	public static final String TIME_PATTERN_XML = "HH:mm:ss.SSS";
	public static final String DATE_PATTERN_XML = "yyyy-MM-dd";
	public static final String DATETIME_PATTERN_XML = DATE_PATTERN_XML + "'T'" + TIME_PATTERN_XML;

	public static final String TIME_PATTERN = "HH:mm";
	public static final String DATE_PATTERN = "dd.MM.yyyy";
	public static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

	/**
	 * returns comma separated list of objects
	 * 
	 * @param list
	 * @return
	 */
	public static String getCSV(Collection list) {
		return getCSV(list, Integer.MAX_VALUE);
	}

	/**
	 * returns comma separated list of objects
	 * 
	 * @param list
	 * @param maxLength
	 * @return
	 */
	public static String getCSV(Collection list, int maxLength)	{
		String result = "";
		for (Object o : list) {
			result += Constants.CSV_DELIMITER + toString(o);
			if (result.length() >= maxLength) {
				break;
			}
		}
		if (!result.isEmpty()) {
			result = result.substring(Constants.CSV_DELIMITER.length());
		}
		return truncate(result, maxLength);
	}

	/**
	 * truncate String s to length i, if s > i, the last 3 characters will be
	 * replace by ...
	 * 
	 * @param s
	 * @param i
	 * @return
	 */
	public static String truncate(String s, int i) {
		return s.length() > i ? s.substring(0, i - 3) + "..." : s;
	}

	/**
	 * copy String n times
	 * 
	 * @param s
	 * @param n
	 * @return
	 */
	public static String copy(String s, int n) {
		String result = "";
		for (int i = 0; i < n; i++)	{
			result += s;
		}
		return result;
	}

	public static long getMedian(List<Long> values)	{
		Collections.sort(values);

		if (values.size() % 2 == 1) {     // odd
    		return values.get((values.size() + 1) / 2 - 1);
        }
		else {         // even
			long lower = values.get(values.size() / 2 - 1);
			long upper = values.get(values.size() / 2);
			return (lower + upper) / 2;
		}
	}

	private static String enum2String(Enumeration e) {
		if (e == null) return "null";

		String s = "";
		while (e.hasMoreElements())	{
			s += Constants.CSV_DELIMITER + toString(e.nextElement());
		}
		return "[" + s.substring(Math.min(s.length(), Constants.CSV_DELIMITER.length())) + "]";
	}

	private static String array2String(Object[] o) {
		if (o == null) return "null";

		String s = "";
		for (int i = 0; i < o.length; i++) {
			if (i > 0) s += Constants.CSV_DELIMITER;
			s += toString(o[i]);
		}
		return "[" + s + "]";
	}

	private static String collection2String(Collection o) {
        return o == null ? "null" : array2String(o.toArray());
    }

	private static String map2String(Map o)	{
		if (o == null) return "null";

		String s = "";
        for (Object key : o.keySet()) {
            s += Constants.CSV_DELIMITER + toString(key) + "=" + toString(o.get(key));
        }
		return "{" + s.substring(Math.min(s.length(), Constants.CSV_DELIMITER.length())) + "}";
	}

	public static String toString(Object o)	{
		if (o == null) {
			return "null";
		}
		else if (o instanceof WorkItemRecord) {
			return ((WorkItemRecord) o).getID();
		}
		else if (o instanceof Participant) {
			return ((Participant) o).getFullName();
		}
		else if (o instanceof NonHumanResource)	{
			return ((NonHumanResource) o).getName();
		}
		else if (o instanceof NonHumanCategory)	{
			return ((NonHumanCategory) o).getName();
		}
		else if (o instanceof Role)	{
			return ((Role) o).getName();
		}
		else if (o instanceof Capability) {
			return ((Capability) o).getCapability();
		}
		else if (o instanceof Position)	{
			return ((Position) o).getTitle();
		}
		else if (o instanceof YVariable) {
			return ((YVariable) o).getName();
		}
		else if (o instanceof Document)	{
			return document2String((Document) o, false);
		}
		else if (o instanceof Element) {
			return element2String((Element) o, false);
		}
		else if (o instanceof Attribute) {
			return attribute2String((Attribute) o);
		}
		else if (o instanceof Date) {
			return date2String((Date) o, DATETIME_PATTERN);
		}
		else if (o instanceof Long)	{
			return ((Long) o).toString();
		}
		else if (o instanceof Enumeration) {
			return enum2String((Enumeration) o);
		}
		else if (o instanceof Collection) {
			return collection2String((Collection) o);
		}
		else if (o instanceof Map) {
			return map2String((Map) o);
		}
		// proof of array
		try	{
			Object[] a = ((Object[]) o);
			return array2String(a);
		}
		catch (Exception e)	{
            // not an array
		}
		return o.toString();
	}


	public static String date2String(Date date, String pattern, TimeZone timeZone) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(timeZone);
		return sdf.format(date);
	}

	public static String date2String(Date date, String pattern)	{
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	public static Date string2Date(String date, String pattern) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.parse(date);
	}

	/**
	 * converts minute string x to lexical representation of duration
	 * 
	 * @param minutes
	 * @return
	 */
	public static String stringMinutes2stringXMLDuration(String minutes)
            throws DatatypeConfigurationException {
		Long millis = Long.parseLong(minutes) * 60 * 1000;
		DatatypeFactory df = DatatypeFactory.newInstance();
		Duration d = df.newDuration(millis);
		return d.toString();
	}

	/**
	 * converts duration, e.g. PTxxxM to minute string xxx
	 * 
	 * @param value
	 * @return
	 */
	public static String stringXMLDuration2stringMinutes(String value) {
		try	{
			Duration d = stringXML2Duration(value);
			return String.valueOf(duration2Minutes(d));
		}
		catch (Exception e)	{
			// logger.error("cannot parse " + value, e);
			return value;
		}
	}

	/**
	 * converts duration, e.g. PTxxxM to minute int xxx
	 * 
	 * @param d
	 * @return
	 */
	public static int duration2Minutes(Duration d) {
		long millis = d.getYears() * 12;
		millis = (millis + d.getMonths()) * 31;
		millis = (millis + d.getDays()) * 24;
		millis = (millis + d.getHours()) * 60;
		millis = (millis + d.getMinutes()) * 60;
		millis = (millis + d.getSeconds()) * 1000;

		int minutes = (int) (millis / 60 / 1000) * d.getSign();
		// logger.debug("Duration: "+d+" = "+minutes+" min");
		return minutes;
	}

	public static Duration stringXML2Duration(String lexicalRepresentation)
            throws DatatypeConfigurationException {
		return DatatypeFactory.newInstance().newDuration(lexicalRepresentation);
	}

	public static String elements2String(List<Element> elems, boolean formatted) {
		String s = "";
		for (Element elem : elems) {
			s += element2String(elem, formatted);
		}
		return s;
	}

	public static String document2String(Document doc, boolean formatted) {
        return doc == null ? null : element2String(doc.getRootElement(), formatted);
    }

	public static String element2String(Element elem, boolean formatted)
	{
		// JDOMUtil.elementToString(elem)
		if (elem == null) return null;

		if (formatted) return format.outputString(elem);
		else
			return compact.outputString(elem);
	}

	public static String attribute2String(Attribute a)
	{
		if (a == null) return null;
		else
			return a.getName() + "=" + a.getValue();
	}

	/**
	 *
	 * @param s
	 * @return
	 */
	public static synchronized Document string2Document(String s)
	{
		return JDOMUtil.stringToDocument(s);
	}

	public static Element string2Element(String s)
	{
		return string2Document(s).detachRootElement();
	}

	public static List<Element> string2Elements(String s)
	{
		List<Element> list = new ArrayList<Element>();
		Element tmp = string2Element("<tmp>" + s + "</tmp>");
		for (Element e : tmp.getChildren())
		{
			// list.add((Element)e.detach());
			list.add(Utils.string2Element(Utils.element2String(e, false)));
		}
		return list;
	}

	/**
	 * adds a value to a map at given key if a previous value at given key
	 * already exists, a separator and the new value will be added
	 * 
	 * @param key
	 * @param value
	 * @param map
	 * @param separator
	 */
	public static void addString2Map(String key, String value, Map<String, String> map, String separator)
	{
		if (map == null)
		{
			map = new HashMap<String, String>();
		}

		String prev = map.get(key); // previous value
		map.put(key, (prev == null ? "" : prev + separator) + value);
	}

	/**
	 * serialize String values in JSON format
	 * 
	 * @param list
	 * @return
	 */
	public static String getJSON(List<String> list) throws Exception
	{
		return getJSON(list.toArray(new String[0]));
	}

	/**
	 * serialize String keys and String values in JSON format
	 * 
	 * @param map
	 * @return
	 */
	public static String getJSON(Map<String, Object> map) throws Exception
	{
		return getJSON(new ArrayList<String>(map.keySet()), map);
	}

	/**
	 * serialize String keys and String values in JSON format, sort keys
	 * according to order in sortedList
	 * 
	 * @param map
	 * @return
	 */
	public static String getJSON(List<String> sortedList, Map<String, Object> map) throws Exception
	{
		try
		{
			JSONStringer js = new JSONStringer();
			js.object();
			if (map != null)
			{
				for (String key : sortedList)
				{
					js.key(key);
					js.value(map.get(key));
				}
			}
			js.endObject();
			return js.toString();
		}
		catch (JSONException e)
		{
			throw new Exception("cannot produce JSON", e);
		}
	}

	/**
	 * serialize String values in JSON format
	 * 
	 * @param array
	 * @return
	 */
	public static String getJSON(String[] array) throws Exception
	{
		try
		{
			JSONStringer js = new JSONStringer();
			js.array();
			if (array != null)
			{
				for (String value : array)
				{
					js.value(value);
				}
			}
			js.endArray();
			return js.toString();
		}
		catch (JSONException e)
		{
			throw new Exception("cannot produce JSON", e);
		}
	}

	public static String[] jsonObject2Array(Object json) throws JSONException
	{
		if (json == null || JSONObject.NULL.equals(json)) return new String[0];

		JSONArray jsonArr = (JSONArray) json;
		String[] strArr = new String[jsonArr.length()];
		for (int i = 0; i < jsonArr.length(); i++)
		{
			strArr[i] = jsonArr.get(i).toString();
		}
		return strArr;
	}

	public static String[] parseJSON2Array(String json) throws JSONException
	{
		return jsonObject2Array(new JSONArray(json));
	}

	public static Map<String, String> parseJSON2Map(String json) throws JSONException
	{
		Map<String, String> map = new TreeMap<String, String>();
		if (json == null) return map;

		JSONObject jsonObj = new JSONObject(json);
		Iterator i = jsonObj.keys();
		while (i.hasNext())
		{
			String key = i.next().toString();
			map.put(key, jsonObj.get(key).toString());
		}
		return map;
	}

	/**
	 * @param csv
	 * @return
	 */
	public static List<String> parseCSV(String csv)
	{
		return parseCSV(csv, ",");
	}

	/**
	 * @param csv
	 * @return
	 */
	public static List<String> parseCSV(String csv, String delimiters)
	{
		List<String> list = new ArrayList<String>();
		if (csv != null)
		{
			StringTokenizer st = new StringTokenizer(csv, delimiters);
			while (st.hasMoreTokens())
			{
				list.add(st.nextToken().trim());
			}
		}
		return list;
	}

	/**
	 * Encodes parameter values for HTTP transport copy of @see
	 * org.yawlfoundation.yawl.engine.interfce.Interface_Client.encodeData
	 * 
	 * @param params
	 *           a map of the data parameter values, of the form
	 *           [param1=value1],[param2=value2]...
	 * @return a formatted http data string with the data values encoded
	 */
	public static String encodeData(Map<String, Object[]> params)
	{
		StringBuilder result = new StringBuilder("");
		for (String param : params.keySet())
		{
			String[] values = (String[]) params.get(param);
			for (String value : values)
			{
				if (value != null)
				{
					if (result.length() > 0) result.append("&");
					result.append(param).append("=").append(ServletUtils.urlEncode(value));
				}
			}
		}
		// logger.debug("encode "+Utils.toString(params)+" to " +
		// result.toString());
		return result.toString();
	}

	/**
	 * Transforms a string's first character to upper case
	 * 
	 * @param word
	 * @return capitalized word
	 */
	public static String capitalize(String word)
	{
		if (word.length() < 1) return word;
		String letter = word.substring(0, 1).toUpperCase();
		return letter + word.substring(1);
	}

	/**
	 * compares two objects
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean isEqual(Object o1, Object o2)
	{
		if (o1 == null && o2 == null)
		{
			return true;
		}
		else if (o1 == null && o2 != null)
		{
			return false;
		}
		else if (o2 == null && o1 != null)
		{
			return false;
		}
		else
		{
			return o1.equals(o2);
		}
	}

	public static String getLogRequestParameters(Map<String, Object> parameterMap)
	{
		ArrayList<String> keys = new ArrayList<String>(parameterMap.keySet());
		String log = "request parameters, size = " + keys.size();
		Collections.sort(keys);
		for (String key : keys)
		{
			Object[] values = ((Object[]) parameterMap.get(key));
			log += "\r\n\tkey: " + key + " = " + Utils.toString(values);
		}
		return log;
	}

	public static String getLogSessionAttributes(HttpSession session)
	{
		Map<String, String> map = new HashMap<String, String>();
		Enumeration enumeration = session.getAttributeNames();
		while (enumeration.hasMoreElements())
		{
			String key = (String) enumeration.nextElement();
			Object value = session.getAttribute(key);
			map.put(key, toString(value));
		}

		ArrayList<String> keys = new ArrayList<String>(map.keySet());
		String log = "session attributes, size = " + keys.size();
		Collections.sort(keys);
		for (String key : keys)
		{
			Object value = map.get(key);
			log += "\r\n\tkey: " + key + " = " + value;
		}
		return log;
	}

	/**
	 * possible charsets: UTF-8, ISO-8859-1, US-ASCII, UTF-16, UTF-16BE, UTF-16LE
	 * 
	 * @param urlStr
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public static String sendRequest(String urlStr, Map<String, Object[]> parameters) throws Exception
	{
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
		osw.write(Utils.encodeData(parameters));
		osw.close();

		// read reply into a buffered byte stream - to preserve UTF-8
		BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(16384);
		byte[] buffer = new byte[16384];

		// read chunks from the input stream and write them out
		int bytesRead;
		while ((bytesRead = inStream.read(buffer, 0, 16384)) > 0)
		{
			outStream.write(buffer, 0, bytesRead);
		}

		outStream.close();
		inStream.close();

		// convert the bytes to a UTF-8 string
		String ret = outStream.toString("UTF-8");

		connection.disconnect();

		// logger.debug("ret: " + ret);
		return ret;
	}

	/**
	 * calendar format for JS_Calendar in javascript format
	 * 
	 * @param localizedString
	 * @return
	 */
	public static Object getJsCalendarFormat(String localizedString)
	{
		localizedString = localizedString.replace("HH", "hh");
		localizedString = localizedString.replace("mm", "ii");
		localizedString = localizedString.replace("MM", "mm");
		return localizedString;
	}

	/**
	 * 
	 * @author http://paaloliver.wordpress.com/2006/01/24/sorting-maps-in-java/
	 */
	public static class ValueComparer implements Comparator
	{
		private Map<Object, Comparable> data = null;
		private boolean descending; // sort order

		public ValueComparer(Map<Object, Comparable> data, boolean descending)
		{
			super();
			this.data = data;
			this.descending = descending;
		}

		public int compare(Object o1, Object o2)
		{
			Comparable value1 = data.get(o1);
			Comparable value2 = data.get(o2);
			if (value1 instanceof String)
			{
				value1 = ((String) value1).toLowerCase();
			}
			if (value2 instanceof String)
			{
				value2 = ((String) value2).toLowerCase();
			}
			int c = value1.compareTo(value2);
			if (c == 0)
			{
				Integer h1 = o1.hashCode(), h2 = o2.hashCode();
				c = h1.compareTo(h2);
			}

			return descending ? -c : c;
		}
	}

	public static boolean equals(Object o1, Object o2)
	{
		if (o1 == null && o2 == null)
		{
			return true;
		}
		else if (o1 == null || o2 == null)
		{
			return false;
		}
		else
		{
			return o1.equals(o2);
		}
	}

	public static String extractCaseId(String input)
	{
		String[] detail = input.split("\\.");
		if (detail.length > 1)
		{
			return detail[0];
		}
		else
		{
			return input;
		}
	}

	/**
	 * creates deep copy of serializable object
	 * 
	 * see http://www.tutego.de/java/articles/Tiefe-Objektkopien-Java.html
	 * 
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public static Object deepCopy(Object o) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new ObjectOutputStream(baos).writeObject(o);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		return new ObjectInputStream(bais).readObject();
	}
}
