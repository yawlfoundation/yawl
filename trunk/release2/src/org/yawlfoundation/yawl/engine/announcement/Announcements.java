/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.announcement;

import org.yawlfoundation.yawl.exceptions.YStateException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/*
 * A collection like class for containing multiple announcements
 *
 * @author Mike Fowler
 *         Date: May 15, 2008
 */
public class Announcements<A extends Announcement>
{
    /**
     * Maps resource uri's to the announcement object
     */
    private Hashtable <URI, Set<A>> uriToAnnouncements = new Hashtable<URI, Set<A>>();

    /**
     * Maps the schemes of the uri to the uris of that scheme (QUT: http, M2: jmc)
     */
    private Hashtable <String, Set<URI>> schemeToUris = new Hashtable<String, Set<URI>>();

    /**
     * Appends the announcement to the collection
     *
     * @param announcement
     * @throws YStateException if the YAWLServiceReference uri in the
     *    announcement is a malformed URI
     */
    public void addAnnouncement(A announcement) throws YStateException
    {
        try
        {
            URI uri = new URI(announcement.getYawlService().getURI());

            Set<A> announcements = uriToAnnouncements.get(uri);
            if(announcements == null) announcements = new HashSet<A>();
            announcements.add(announcement);
            uriToAnnouncements.put(uri, announcements);

            Set<URI> uris = schemeToUris.get(uri.getScheme());
            if(uris == null) uris = new HashSet<URI>();
            uris.add(uri);
            schemeToUris.put(uri.getScheme(), uris);
        }
        catch (URISyntaxException e)
        {
            throw new YStateException("YAWLServiceRefernce '" + announcement.getYawlService() + "' not a valid URI!");
        }
    }

    /**
     * @return Set of all schemes used by the collected announcements
     */
    public Set<String> getSchemes()
    {
        return schemeToUris.keySet();
    }

    /**
     * @return Set of all URIs used by the collected announcements
     */
    public Set<URI> getURIs()
    {
        return uriToAnnouncements.keySet();
    }

    /**
     * @return All the announcements in the collection
     */
    public Set<A> getAllAnnouncements()
    {
        Set<A> announcements = new HashSet<A>();

        for(Set<A> as : uriToAnnouncements.values())
        {
            announcements.addAll(as);
        }

        return announcements;
    }

    /**
     * @param scheme to retrieve announcements for
     * @return a new Announcements object containg only the announcements for
     *         the given scheme
     */
    public Announcements<A> getAnnouncementsForScheme(String scheme)
    {
        Announcements<A> announcements = new Announcements<A>();

        for(URI uri : schemeToUris.get(scheme))
        {
            try
            {
                announcements.addAll(uriToAnnouncements.get(uri));
            }
            catch (YStateException e)
            {
                //really shouldn't happen - they've been added once and passed the first addAnnouncement
            }
        }

        return announcements;
    }

    /**
     * @param uri to retrieve announcements for
     * @return list of all announcements for the given uri
     */
    public Set<A> getAnnouncementsForURI(URI uri)
    {
        return uriToAnnouncements.get(uri);
    }

    /**
     * @return total number of announcements in this collection
     */
    public long size()
    {
        long size = 0;

        for(Set<A> assignments : uriToAnnouncements.values())
        {
            size += assignments.size();
        }

        return size;
    }

    /**
     * @param announcements Collection of announcements to append to this collection
     * @throws YStateException if the YAWLServiceReference uri in the
     *    announcement is a malformed URI
     */
    public void addAll(Collection<A> announcements) throws YStateException
    {
        for(A a : announcements)
        {
            addAnnouncement(a);
        }
    }


    public void clear() {
        schemeToUris.clear();
        uriToAnnouncements.clear();
    }
}
