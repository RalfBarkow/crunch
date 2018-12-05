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

package org.pragmaticminds.crunch;

/**
 * Static class that holds logging information used for "trace" logging, i.e., logging of the raw event stream.
 *
 * @author julian
 * Created by julian on 10.09.18
 */
public class LoggingUtil {

    /**
     * After how many logs a trace output is printed
     */
    private static int TRACE_LOG_REPORT_CHECKPOINT = 1_000;

    private LoggingUtil() {
        // Do not instantiate
    }

    public static int getTraceLogReportCheckpoint() {
        return TRACE_LOG_REPORT_CHECKPOINT;
    }

    public static void setTraceLogReportCheckpoint(int traceLogReportCheckpoint) {
        TRACE_LOG_REPORT_CHECKPOINT = traceLogReportCheckpoint;
    }

}
