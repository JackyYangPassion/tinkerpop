/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.tinkergraph.process.traversal.step.sideEffect;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.tinkergraph.process.traversal.strategy.optimization.TinkerGraphStepStrategy;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class TinkerGraphStepTest {
    /**
     * 通过扩展测试用例，理解GraphStep的实现原理
     */

    private Graph graph;
    private GraphTraversalSource g;

    @Before
    public void setup() {
        graph = TinkerGraph.open();
        g = graph.traversal();
    }

    /**
     * TinkerGraphStepStrategy pulls `has("age", P.gt(1))` into a HasContainer within the initial TinkerGraphStep.
     * This requires that TinkerGraphStep handles the reduction from Ternary Boolean logics (TRUE, FALSE, ERROR) to
     * ordinary boolean logics which normally takes place within FilterStep's.
     */
    @Test
    public void shouldHandleComparisonsWithNaN() {
        g.addV("v1").property("age", Double.NaN).next();
        g.addV("v1").property("age", 3).next();
        int count = g.V().has("age", P.gt(1)).count().next().intValue();
        assertEquals(1, count);
    }

    /**
     * 以下测试用例 主要实现以下语句中过滤条件
     * 1. g.V().has('name','marko')
     * 2. g.V('1','2')
     * 3. g.E().has('weight',gt(0.5))
     * 4. g.E('e_id1','e_id2')
     *
     */

    //Write by Github copilot
    @Test
    public void getVWithIds() {
        //g.addV() 自定义ID
        g.addV("v1").property(T.id, "1").property("name", "marko").next();
        g.addV("v1").property(T.id, "2").property("name", "peter").next();
        g.addV("v1").property(T.id, "3").property("name", "marko").next();
        g.addV("v1").property(T.id, "4").property("name", "peter").next();
        int count = g.V("1", "2").count().next().intValue();
        assertEquals(2, count);
    }

    //Write by Github copilot
    @Test
    public void getVWith2Has(){
        g.addV("v1").property(T.id, "1").property("name", "marko").next();
        g.addV("v1").property(T.id, "2").property("name", "peter").next();
        g.addV("v1").property(T.id, "3").property("name", "marko").next();
        g.addV("v1").property(T.id, "4").property("name", "peter").next();
        int count = g.V().has("name", "marko").has(T.id, "1").count().next().intValue();
        assertEquals(1, count);
    }

}
