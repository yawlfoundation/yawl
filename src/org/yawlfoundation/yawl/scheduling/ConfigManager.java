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

package org.yawlfoundation.yawl.scheduling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;

/**
 * Manages the loading of resource bundles and other properties. Each instance for a
 * specific locale is cached and reused
 */
public class ConfigManager implements Constants {

    // resource bundle base filename
    private static final String FILENAME_RESOURCES = "resources";

    // the resource bundle loaded for this instance
    private ResourceBundle _bundle = null;

    // instance cache - one for each language
    private static Map<String, ConfigManager> _instances = new Hashtable<String, ConfigManager>();

    private static final Logger _log = LogManager.getLogger(ConfigManager.class);


    /**
     * Only Constructor - called from getInstance
     * @param bundle the selected bundle for this manager object
     */
    private ConfigManager(ResourceBundle bundle) {
        _bundle = bundle;
        _log.debug("ConfigManager starting with language " + getLanguage());
    }


    /********************************************************************************/

    /**
     * Gets a ConfigManager instance using the default language (set in Constants)
     * @return the ConfigManager instance
     */
    public static ConfigManager getInstance() {
        return getInstance(LANGUAGE_DEFAULT);
    }


    /**
     * Gets a ConfigManager instance using the specified language
     * @param language the language to get a ConfigManager instance for
     * @return the ConfigManager instance
     */
    public static ConfigManager getInstance(String language) {
        if (language == null) language = LANGUAGE_DEFAULT;
        if (! _instances.containsKey(language)) {
            List<Locale> locales = new ArrayList<Locale>();
            Locale locale = getLocaleFromLanguage(language);
            if (locale != null) locales.add(locale);
            _instances.put(language, new ConfigManager(loadBundle(locales)));
        }
        return _instances.get(language);
    }



    /**
     * Gets a ConfigManager instance using the specified ResourceBundle
     * @param bundle the bundle to get a ConfigManager instance for
     * @return the ConfigManager instance
     */
    private static ConfigManager getInstance(ResourceBundle bundle) {
        if (bundle == null) return getInstance(LANGUAGE_DEFAULT);
        String language = bundle.getLocale().getLanguage();
        if (! _instances.containsKey(language)) {
            _instances.put(language, new ConfigManager(bundle));
        }
        return _instances.get(language);
    }


    /**
     * Gets the appropriate ConfigManager, based on a servlet request.
     * @param request the request to use to select the appropriate ConfigManager instance
     * @return the appropriate ConfigManager instance
     */
    public static ConfigManager getFromRequest(HttpServletRequest request) {
        ConfigManager config;
        boolean newInstance = true;
        List<Locale> locales = getLocalesFromRequest(request);

        if (locales.isEmpty()) {
            config = getInstance(LANGUAGE_DEFAULT);           // no locales in request
        }
        else {
            config = (ConfigManager) request.getSession().getAttribute("ConfigManager");
            if ((config == null) || (!containsLanguage(locales, config.getLanguage()))) {
                config = getInstance(loadBundle(locales));
            }
            else newInstance = false;
        }

        if (newInstance) {
            request.getSession().setAttribute(LANGUAGE_ATTRIBUTE_NAME, config.getLanguage());
            request.getSession().setAttribute("ConfigManager", config);
        }
        return config;
    }


    /******************************************************************************/

    /**
      * Creates a new Locale instance using the language specified
      * @param language the locale's language
      * @return the new Locale instance
      */
     private static Locale getLocaleFromLanguage(String language) {
         return language == null ? null : new Locale(language, language);
     }


    /**
     * Checks whether a list of locales contains a specified language
     * @param locales a list of Locale objects
     * @param language the language to check for
     * @return true if a listed Locale contains the specified language
     */
    private static boolean containsLanguage(List<Locale> locales, String language) {
        for (Locale locale : locales) {
            if (locale.getLanguage().equals(language)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Gets the list of all Locales specified in the request, whether set as Locales or
     * as a request parameter or session attribute
     * @param request the request to parse
     * @return a List of Locales found
     */
    private static List<Locale> getLocalesFromRequest(HttpServletRequest request) {
        List<Locale> locales = new ArrayList<Locale>();

        String language = request.getParameter(LANGUAGE_ATTRIBUTE_NAME);
        _log.debug("language from request = " + language);

        if (StringUtil.isNullOrEmpty(language)) {
            language = (String) request.getSession().getAttribute(LANGUAGE_ATTRIBUTE_NAME);
            _log.debug("language from session = " + language);
        }

        if (!StringUtil.isNullOrEmpty(language)) {
            locales.add(new Locale(language, language));
        }

        Enumeration localesEnum = request.getLocales();
        while (localesEnum.hasMoreElements()) {
            locales.add((Locale) localesEnum.nextElement());
        }
        return locales;
    }


    /**
     * Iterates through the list of Locales passed, checks each for an available
     * ResourceBundle, loads the first one found and sets it as the default bundle
     * @param locales the List to iterate through
     * @return x
     */
    private static ResourceBundle loadBundle(List<Locale> locales) {
        List<Locale> localesWithDefault = new ArrayList<Locale>(locales);
        localesWithDefault.add(new Locale(LANGUAGE_DEFAULT, LANGUAGE_DEFAULT));

        for (Locale locale : localesWithDefault) {
            try {
                return ResourceBundle.getBundle(FILENAME_RESOURCES, locale);
            }
            catch (Exception e) {

                // this will load bundle in control panel version
                try {
                    File file = new File(System.getenv("CATALINA_HOME") +
                            "/webapps/schedulingService/WEB-INF/classes");
                    URL[] urls = { file.toURI().toURL() };
                    ClassLoader loader = new URLClassLoader(urls);
                    return ResourceBundle.getBundle(FILENAME_RESOURCES, locale, loader);
                }
                catch (Exception e1) {
                    if (locale != null && LANGUAGE_DEFAULT.equals(locale.getLanguage())) {
                        _log.fatal("Cannot load bundle for default language: " + locale.getLanguage(), e1);
                    }
                }
            }
        }
        return null;
    }


    /**
     * Gets the locale of the currently loaded ResourceBundle
     * @return the loaded locale, or null of no locale is loaded
     */
    public Locale getLocale() {
        return _bundle == null ? null : _bundle.getLocale();
    }


    /**
     * Gets the language of the locale of the currently loaded ResourceBundle
     * @return the loaded locale's language, or null of no locale is loaded
     */
    public String getLanguage() {
        return getLocale() == null ? null : getLocale().getLanguage();
    }


    /**
     * Gets the matching value for a given key from the loaded ResourceBundle
     * @param key the key to get the value for
     * @return the value matching the key, or the key itself if not found
     */
    private String getBundleString(String key) {
        return _bundle.containsKey(key) ? _bundle.getString(key) : key;
    }


    /**
     * Gets the localised value (a string message in the current language) for a key.
     * If the value contains placeholders, replaces each of them recursively with
     * its own localised value for matching subkey specified, where possible.
     * @param key the key to get the value for
     * @param subKeys the set of keys to use as values for corresponding placeholders
     * in the original value
     * @return the fully populated value matching the key passed, or the key itself if
     * there is some problem getting any of the values
     */
    public String getLocalizedString(String key, String... subKeys) {
        if (StringUtil.isNullOrEmpty(key) || _bundle == null) {
            return key;
        }

        try {
            String result = "";
            StringTokenizer st = new StringTokenizer(key, WORD_SEPARATORS, true);
            int arrayCount = 0;
            while (st.hasMoreTokens()) {
                String keyPart = st.nextToken();
                int formatsCount = 0;
                try {
                    String resource = getBundleString(keyPart);
                    MessageFormat format = new MessageFormat(resource);
                    formatsCount = format.getFormats().length;

                    String[] subResult = new String[formatsCount];
                    for (int i = 0; i < formatsCount; i++) {
                        subResult[i] = getLocalizedString(subKeys[arrayCount], new String[0]);
                        arrayCount++;
                    }
                    result += format.format(subResult);
                }
                catch (Exception e) {
                    _log.warn("unknown key '" + keyPart + "' in bundle for locale: " +
                            _bundle.getLocale());
                    result += keyPart;
                }
            }
            return result;
        }
        catch (Exception e) {
            _log.error("", e);
            return key;
        }
    }

    /**
     * Gets the localised value (a string message in the current language) for a key.
     * If the value contains placeholders, replaces each of them recursively with
     * its own localised value for matching keys specified, where possible.
     * @param valueAndArgsAsJSON a JSON string containing the key to get the value for
     * and the set of keys to use as values for corresponding placeholders in the
     * original value (as an array)
     * @return the fully populated value matching the key passed, or the key itself if
     * there is some problem getting any of the values
     */
    public String getLocalizedJSONString(String valueAndArgsAsJSON) {
        if (valueAndArgsAsJSON == null) {
            return null;
        }

        try {
            String msgWithArgs = "";
            JSONArray jsonArr = new JSONArray(valueAndArgsAsJSON);
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                Iterator it = jsonObj.keys();
                while (it.hasNext()) {
                    String msg = (String) it.next();
                    Object argsStr = jsonObj.get(msg);
                    String[] args = Utils.jsonObject2Array(argsStr);
                    msgWithArgs += DELIMITER + getLocalizedString(msg, args);
                }
            }
            return msgWithArgs.substring(DELIMITER.length());
        }
        catch (Exception e) {
            _log.warn("cannot parse json: "+valueAndArgsAsJSON, e);
            return getLocalizedString(valueAndArgsAsJSON);
        }
    }


    /**
     * Gets the localised value (a string message in the current language) for a key
     * @param key the key to get the value for
     * @return the value matching the key, or the key itself if not found
     */
    public String getLocalizedString(String key) {
        return getLocalizedString(key, new String[0]);
    }

}
