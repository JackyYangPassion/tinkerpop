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
package org.apache.tinkerpop.gremlin;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.Traversal;
import org.apache.tinkerpop.gremlin.process.graph.traversal.GraphTraversalContext;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.strategy.GraphStrategy;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

/**
 * Sets up g based on the current graph configuration and checks required features for the test.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public abstract class AbstractGremlinTest {
    protected Graph graph;
    protected GraphTraversalContext g;
    protected Configuration config;
    protected GraphStrategy[] strategiesToTest;
    protected GraphProvider graphProvider;

    @Rule
    public TestName name = new TestName();

    public AbstractGremlinTest() {
        this(null);
    }

    public AbstractGremlinTest(final GraphStrategy... strategiesToTest) {
        this.strategiesToTest = strategiesToTest;
    }

    @Before
    public void setup() throws Exception {
        graphProvider = GraphManager.getGraphProvider();
        config = graphProvider.standardGraphConfiguration(this.getClass(), name.getMethodName());

        // this should clear state from a previously unfinished test. since the graph does not yet exist,
        // persisted graphs will likely just have their directories removed
        graphProvider.clear(config);

        // not sure how the strategy can ever be null, but it seems to happen in the performance tests
        graph = graphProvider.openTestGraph(config, strategiesToTest);
        g = graphProvider.traversal(graph);

        final Method testMethod = this.getClass().getMethod(cleanMethodName(name.getMethodName()));

        final LoadGraphWith[] loadGraphWiths = testMethod.getAnnotationsByType(LoadGraphWith.class);

        // get feature requirements on the test method and add them to the list of ones to check
        final FeatureRequirement[] featureRequirement = testMethod.getAnnotationsByType(FeatureRequirement.class);
        final List<FeatureRequirement> frs = new ArrayList<>(Arrays.asList(featureRequirement));

        // if the graph is loading data then it will come with it's own requirements
        if (loadGraphWiths.length > 0) frs.addAll(loadGraphWiths[0].value().featuresRequired());

        // if the graph has a set of feature requirements bundled together then add those
        final FeatureRequirementSet[] featureRequirementSets = testMethod.getAnnotationsByType(FeatureRequirementSet.class);
        if (featureRequirementSets.length > 0)
            frs.addAll(Arrays.stream(featureRequirementSets)
                    .flatMap(f -> f.value().featuresRequired().stream()).collect(Collectors.toList()));

        // process the unique set of feature requirements
        final Set<FeatureRequirement> featureRequirementSet = new HashSet<>(frs);
        for (FeatureRequirement fr : featureRequirementSet) {
            try {
                //System.out.println(String.format("Assume that %s meets Feature Requirement - %s - with %s", fr.featureClass().getSimpleName(), fr.feature(), fr.supported()));
                assumeThat(String.format("%s does not support all of the features required by this test so it will be ignored: %s.%s=%s",
                                g.getClass().getSimpleName(), fr.featureClass().getSimpleName(), fr.feature(), fr.supported()),
                        graph.features().supports(fr.featureClass(), fr.feature()), is(fr.supported()));
            } catch (NoSuchMethodException nsme) {
                throw new NoSuchMethodException(String.format("[supports%s] is not a valid feature on %s", fr.feature(), fr.featureClass()));
            }
        }

        beforeLoadGraphWith(graph);

        // load a graph with sample data if the annotation is present on the test
        final LoadGraphWith loadGraphWith = loadGraphWiths.length == 0 ? null : loadGraphWiths[0];
        graphProvider.loadGraphData(graph, loadGraphWith, this.getClass(), name.getMethodName());

        afterLoadGraphWith(graph);
    }

    protected void beforeLoadGraphWith(final Graph g) throws Exception {
        // do nothing
    }

    protected void afterLoadGraphWith(final Graph g) throws Exception {
        // do nothing
    }

    @After
    public void tearDown() throws Exception {
        if (null != graphProvider) {
            graphProvider.clear(graph, config);
            g = null;
            config = null;
            strategiesToTest = null;
            graphProvider = null;
        }
    }

    /**
     * Looks up the identifier as generated by the current source graph being tested.
     *
     * @param vertexName a unique string that will identify a graph element within a graph
     * @return the id as generated by the graph
     */
    public Object convertToVertexId(final String vertexName) {
        return convertToVertexId(graph, vertexName);
    }

    /**
     * Looks up the identifier as generated by the current source graph being tested.
     *
     * @param g          the graph to get the element id from
     * @param vertexName a unique string that will identify a graph element within a graph
     * @return the id as generated by the graph
     */
    public Object convertToVertexId(final Graph g, final String vertexName) {
        return convertToVertex(g, vertexName).id();
    }

    public Vertex convertToVertex(final Graph graph, final String vertexName) {
        // all test graphs have "name" as a unique id which makes it easy to hardcode this...works for now
        return graph.traversal().V().has("name", vertexName).next();
    }

    public Object convertToEdgeId(final String outVertexName, String edgeLabel, final String inVertexName) {
        return convertToEdgeId(graph, outVertexName, edgeLabel, inVertexName);
    }

    public Object convertToEdgeId(final Graph graph, final String outVertexName, String edgeLabel, final String inVertexName) {
        return graph.traversal().V().has("name", outVertexName).outE(edgeLabel).as("e").inV().has("name", inVertexName).<Edge>back("e").next().id();
    }

    /**
     * Utility method that commits if the graph supports transactions.
     */
    public void tryCommit(final Graph g) {
        if (g.features().graph().supportsTransactions())
            g.tx().commit();
    }

    public void tryRandomCommit(final Graph g) {
        if (g.features().graph().supportsTransactions() && new Random().nextBoolean())
            g.tx().commit();
    }

    /**
     * Utility method that commits if the graph supports transactions and executes an assertion function before and
     * after the commit.  It assumes that the assertion should be true before and after the commit.
     */
    public void tryCommit(final Graph g, final Consumer<Graph> assertFunction) {
        assertFunction.accept(g);
        if (g.features().graph().supportsTransactions()) {
            g.tx().commit();
            assertFunction.accept(g);
        }
    }

    /**
     * Utility method that rollsback if the graph supports transactions.
     */
    public void tryRollback(final Graph g) {
        if (g.features().graph().supportsTransactions())
            g.tx().rollback();
    }

    /**
     * If using "parameterized test" junit will append an identifier to the end of the method name which prevents it
     * from being found via reflection.  This method removes that suffix.
     */
    private static String cleanMethodName(final String methodName) {
        if (methodName.endsWith("]")) {
            return methodName.substring(0, methodName.indexOf("["));
        }

        return methodName;
    }

    public void printTraversalForm(final Traversal traversal) {
        final boolean muted = Boolean.parseBoolean(System.getProperty("muteTestLogs", "false"));

        if (!muted) System.out.println(String.format("Testing: %s", name.getMethodName()));
        if (!muted) System.out.println("   pre-strategy:" + traversal);
        traversal.hasNext();
        if (!muted) System.out.println("  post-strategy:" + traversal);
    }

    public static Consumer<Graph> assertVertexEdgeCounts(final int expectedVertexCount, final int expectedEdgeCount) {
        return (g) -> {
            assertEquals(expectedVertexCount, IteratorUtils.count(g.vertices()));
            assertEquals(expectedEdgeCount, IteratorUtils.count(g.edges()));
        };
    }

    public static void validateException(final Throwable expected, final Throwable actual) {
        assertThat(actual, instanceOf(expected.getClass()));
    }
}
