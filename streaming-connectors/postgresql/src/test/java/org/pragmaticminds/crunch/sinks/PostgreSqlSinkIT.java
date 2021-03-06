/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.pragmaticminds.crunch.sinks;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pragmaticminds.crunch.events.GenericEvent;
import org.pragmaticminds.crunch.events.GenericEventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Connection data for Discard
 *   postgres.url=jdbc:postgresql://192.168.169.13:5439/testDb
 *   postgres.user=pm
 *   postgres.password=Minds2017
 *
 * @author Erwin Wagasow
 * Created by Erwin Wagasow on 11.09.2018
 */
// TODO Implement
@Ignore
public class PostgreSqlSinkIT {
    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlSinkIT.class);

    private static String url  = "jdbc:postgresql://192.168.169.13:5439/testDb";
    private static String user = "pm";
    private static String pass = "Minds2017";

    private static String sqlDropEventsTable = "DROP TABLE IF EXISTS events";

    @Before
    public void setUp() {
        // drop events table before start test
        try (PostgreSqlSink postgreSqlSink = new PostgreSqlSink(url, user, pass)) {
            postgreSqlSink.executeCommand(sqlDropEventsTable);
        }
    }

    @After
    public void tearDown() {
        // drop events table after test
        try (PostgreSqlSink postgreSqlSink = new PostgreSqlSink(url, user, pass)) {
            postgreSqlSink.executeCommand(sqlDropEventsTable);
        }
    }

    @Test
    public void run() {
        connectAndCreateEvents();
        connectAndQueryAllEvents();
    }

    public void connectAndQueryAllEvents() {
        try (PostgreSqlSink sink = new PostgreSqlSink(url, user, pass)) {
            List<GenericEvent> results = sink.executeQuery("SELECT * FROM events");
            for (GenericEvent event : results){
                logger.debug("event: {}", event);
            }
        }
    }

    public void connectAndCreateEvents() {
        try (PostgreSqlSink sink = new PostgreSqlSink(url, user, pass)) {
            List<GenericEvent> events = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                events.add(
                        GenericEventBuilder.anEvent()
                                .withTimestamp(System.currentTimeMillis() + i)
                                .withEvent("TestEvent")
                                .withSource("TestSource")
                                .withParameter("string", "string")
                                .withParameter("long", 123L)
                                .withParameter("double", 0.123D)
                                .withParameter("i", (long)i)
                                .build()
                );
            }
            sink.persist(events);
        }
    }
}