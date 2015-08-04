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

package org.yawlfoundation.yawl.util;

import org.hibernate.SessionFactory;
import org.hibernate.stat.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Michael Adams
 * @date 16/03/2011
 */
public class HibernateStatistics {

    private SessionFactory _factory;

    public HibernateStatistics(SessionFactory factory) {
        _factory = factory;
    }

    public Statistics get() {
        return _factory.getStatistics();
    }

    public String toXML() {
        Statistics stats = get();
        XNode node = new XNode("HibernateStatistics");
        node.addChild("startTime", getTimeString(stats.getStartTime()));
        node.addChild("connections", stats.getConnectCount());
        node.addChild("statement_closes", stats.getCloseStatementCount());
        node.addChild("flushes", stats.getFlushCount());
        node.addChild(getSession(stats));
        node.addChild(getTransaction(stats));
        node.addChild(getCollections(stats));
        node.addChild(getSecondLevelCache(stats));
        node.addChild(getQueries(stats));
        node.addChild(getEntities(stats));
        return node.toPrettyString();
    }


    private XNode getTransaction(Statistics stats) {
        XNode node = new XNode("Transactions");
        node.addChild("count", stats.getTransactionCount());
        node.addChild("successful", stats.getSuccessfulTransactionCount());
        return node;
    }


    private XNode getSession(Statistics stats) {
        XNode node = new XNode("Sessions");
        node.addChild("opens", stats.getSessionOpenCount());
        node.addChild("closes", stats.getSessionCloseCount());
        return node;
    }


    private XNode getSecondLevelCache(Statistics stats) {
        XNode node = new XNode("SecondLevelCache");
        node.addChild("total_hits", stats.getSecondLevelCacheHitCount());
        node.addChild("total_misses", stats.getSecondLevelCacheMissCount());
        node.addChild("total_puts", stats.getSecondLevelCachePutCount());
        for (String name : stats.getSecondLevelCacheRegionNames()) {
            SecondLevelCacheStatistics cacheStats = stats.getSecondLevelCacheStatistics(name);
            XNode cacheNode = node.addChild("cache");
            cacheNode.addChild("name", name);
            cacheNode.addChild("hits", cacheStats.getHitCount());
            cacheNode.addChild("misses", cacheStats.getMissCount());
            cacheNode.addChild("puts", cacheStats.getPutCount());
            cacheNode.addChild("memory_elements", cacheStats.getElementCountInMemory());
            cacheNode.addChild("disk_elements", cacheStats.getElementCountOnDisk());
            cacheNode.addChild("memory_size", cacheStats.getSizeInMemory());
        }
        return node;
    }


    private XNode getCollections(Statistics stats) {
        XNode node = new XNode("Collections");
        node.addChild("total_fetches", stats.getCollectionFetchCount());
        node.addChild("total_loads", stats.getCollectionLoadCount());
        node.addChild("total_recreates", stats.getCollectionRecreateCount());
        node.addChild("total_removes", stats.getCollectionRemoveCount());
        node.addChild("total_updates", stats.getCollectionUpdateCount());
        for (String name : stats.getCollectionRoleNames()) {
            CollectionStatistics collStats = stats.getCollectionStatistics(name);
            XNode collNode = node.addChild("collection");
            collNode.addChild("name", name);
            collNode.addChild("fetches", collStats.getFetchCount());
            collNode.addChild("loads", collStats.getLoadCount());
            collNode.addChild("recreates", collStats.getRecreateCount());
            collNode.addChild("removes", collStats.getRemoveCount());
            collNode.addChild("updates", collStats.getUpdateCount());
        }
        return node;
    }


    private XNode getQueries(Statistics stats) {
        XNode node = new XNode("Queries");
        node.addChild("total_executions", stats.getQueryExecutionCount());
        node.addChild("max_time", stats.getQueryExecutionMaxTime());
        node.addChild("max_time_query", stats.getQueryExecutionMaxTimeQueryString());
        node.addChild("total_cache_hits", stats.getQueryCacheHitCount());
        node.addChild("total_cache_misses", stats.getQueryCacheMissCount());
        node.addChild("total_cache_puts", stats.getQueryCachePutCount());
        for (String query : stats.getQueries()) {
            QueryStatistics queryStats = stats.getQueryStatistics(query);
            XNode queryNode = node.addChild("query");
            queryNode.addChild("hql", query);
            queryNode.addChild("executions", queryStats.getExecutionCount());
            queryNode.addChild("max_time", queryStats.getExecutionMaxTime());
            queryNode.addChild("min_time", queryStats.getExecutionMinTime());
            queryNode.addChild("average_time", queryStats.getExecutionAvgTime());
            queryNode.addChild("row_count", queryStats.getExecutionRowCount());
            queryNode.addChild("cache_hits", queryStats.getCacheHitCount());
            queryNode.addChild("cache_misses", queryStats.getCacheMissCount());
            queryNode.addChild("cache_puts", queryStats.getCachePutCount());
        }
        return node;
    }


    private XNode getEntities(Statistics stats) {
        XNode node = new XNode("Entities");
        node.addChild("total_fetches", stats.getEntityFetchCount());
        node.addChild("total_deletes", stats.getEntityDeleteCount());
        node.addChild("total_inserts", stats.getEntityInsertCount());
        node.addChild("total_loads", stats.getEntityLoadCount());
        node.addChild("total_updates", stats.getEntityUpdateCount());
        node.addChild("total_optimistic_failures", stats.getOptimisticFailureCount());
        for (String entity : stats.getEntityNames()) {
            EntityStatistics entityStats = stats.getEntityStatistics(entity);
            XNode queryNode = node.addChild("entity");
            queryNode.addChild("entity", entity);
            queryNode.addChild("fetches", entityStats.getFetchCount());
            queryNode.addChild("deletes", entityStats.getDeleteCount());
            queryNode.addChild("inserts", entityStats.getInsertCount());
            queryNode.addChild("loads", entityStats.getLoadCount());
            queryNode.addChild("updates", entityStats.getUpdateCount());
            queryNode.addChild("optimistic_failures", entityStats.getOptimisticFailureCount());
        }
        return node;
    }


    private String getTimeString(long time) {
        return new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS").format(new Date(time));
    }



}
