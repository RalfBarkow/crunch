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

package org.pragmaticminds.crunch.runtime.eval;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.checkpoint.ListCheckpointed;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.junit.Ignore;
import org.junit.Test;
import org.pragmaticminds.crunch.api.EvalFunction;
import org.pragmaticminds.crunch.api.EvalFunctionCall;
import org.pragmaticminds.crunch.api.records.MRecord;
import org.pragmaticminds.crunch.api.values.TypedValues;
import org.pragmaticminds.crunch.api.values.dates.Value;
import org.pragmaticminds.crunch.events.GenericEvent;
import org.pragmaticminds.crunch.runtime.merge.ValuesMergeFunctionIT;
import org.pragmaticminds.crunch.runtime.sort.ValueEventAssigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @author julian
 * Created by julian on 17.11.17
 */
public class EvalFunctionWrapperRestartIT {

    @Test
    @Ignore
    public void testRestartOfEvaluation() throws Exception {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        // configure your test environment
        environment.setParallelism(1);

        // GenericEvent Time Processing
        environment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        environment.enableCheckpointing(10);

        // results are collected in a static variable
        EvalFunctionWrapperRestartIT.CollectSink.values.clear();

        // create a stream of custom elements and apply transformations
        ArrayList<MRecord> input = new ArrayList<>();

        for (long i = 0; i < 10; i++) {
            input.add(TypedValues.builder()
                    .source("source1")
                    .timestamp(i)
                    .values(Collections.singletonMap("DB_channel", Value.of(i)))
                    .build());
        }

        // Mock the EvalFunction and the EvalFunction call
        EvalFunction evalFunction1 = new EvalFunctionWrapperIT.COUNT();
        EvalFunctionCall evalFunctionCall = new EvalFunctionCall(evalFunction1, Collections.emptyMap(), Collections.singletonMap("channel1", "DB_channel"));

        KeyedStream<MRecord, Long> stream = environment.addSource(new ExperimentalSlowSource(input)).uid("restart_source")
                .assignTimestampsAndWatermarks(new ValueEventAssigner(15))
                .map(new ValuesMergeFunctionIT.ErrorThrower())
                .uid("restart_merge")
                .keyBy(untypedValues -> 1L);

        stream.print();

        // Eval Function 1
        SingleOutputStreamOperator<GenericEvent> stream1 = stream
                .keyBy(untypedValues -> 1L)
                .process(new EvalFunctionWrapper(evalFunctionCall));

        stream1.print();
        stream1.addSink(new EvalFunctionWrapperRestartIT.CollectSink());

        // execute
        environment.execute();

//        // Assert that the last emitted event contains all 1000 keys send in
        System.out.println(EvalFunctionWrapperRestartIT.CollectSink.values);
        assertEquals(10, EvalFunctionWrapperRestartIT.CollectSink.values.size());

    }


    /**
     * A slow source that emits one value of the given input list each 10 ms.
     * Is a duplicate of the {@link ValuesMergeFunctionIT.SlowSource}
     * due to concurrency problems.
     */
    public static class ExperimentalSlowSource implements SourceFunction<MRecord>, ListCheckpointed<Integer> {

        private static final Logger logger = LoggerFactory.getLogger(ExperimentalSlowSource.class);

        private List<MRecord> input;
        private AtomicInteger counter;

        public ExperimentalSlowSource(List<MRecord> input) {
            this.input = input;
            this.counter = new AtomicInteger(0);
        }

        @Override
        public void run(SourceContext<MRecord> sourceContext) throws Exception {
            while (counter.get() < input.size()) {
                MRecord values = input.get(counter.getAndIncrement());
                TimeUnit.MILLISECONDS.sleep(10);
                sourceContext.collect(values);
            }
            sourceContext.close();
        }

        @Override
        public void cancel() {

        }


        @Override
        public List<Integer> snapshotState(long checkpointId, long timestamp) {
            logger.info("Snapshoting with {}", counter.get());
            return Collections.singletonList(counter.get());
        }

        @Override
        public void restoreState(List<Integer> state) {
            logger.info("Restoring with {}", state);
            if (state.size() > 0) {
                this.counter = new AtomicInteger(state.get(0));
            } else {
                this.counter = new AtomicInteger(0);
            }
        }
    }

    // create a testing sink
    private static class CollectSink implements SinkFunction<GenericEvent> {

        // must be static
        public static final List<GenericEvent> values = new ArrayList<>();

        @Override
        public synchronized void invoke(GenericEvent value) {
            values.add(value);
        }
    }

}