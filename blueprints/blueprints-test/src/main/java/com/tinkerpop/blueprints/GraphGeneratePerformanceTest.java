package com.tinkerpop.blueprints;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import com.tinkerpop.blueprints.generator.CommunityGenerator;
import com.tinkerpop.blueprints.generator.Distribution;
import com.tinkerpop.blueprints.generator.DistributionGenerator;
import com.tinkerpop.blueprints.generator.NormalDistribution;
import com.tinkerpop.blueprints.generator.PowerLawDistribution;
import com.tinkerpop.blueprints.generator.SizableIterable;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
@RunWith(Enclosed.class)
public class GraphGeneratePerformanceTest {

    @AxisRange(min = 0, max = 1)
    @BenchmarkMethodChart(filePrefix = "blueprints-write")
    @BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 20, filePrefix = "hx-blueprints-write")
    public static class WriteToGraph extends AbstractBlueprintsTest {

        @Rule
        public TestRule benchmarkRun = new BenchmarkRule();

        @Test
        @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 0, concurrency = BenchmarkOptions.CONCURRENCY_SEQUENTIAL)
        public void writeEmptyVertices() throws Exception {
            final int verticesToGenerate = 100000;
            for (int ix = 0; ix < verticesToGenerate; ix++) {
                g.addVertex();
            }

            AbstractBlueprintsSuite.assertVertexEdgeCounts(verticesToGenerate, 0).accept(g);
        }

        @Test
        @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 0, concurrency = BenchmarkOptions.CONCURRENCY_SEQUENTIAL)
        public void writeEmptyVerticesAndEdges() throws Exception {
            final int verticesToGenerate = 100000;
            Optional<Vertex> lastVertex = Optional.empty();
            for (int ix = 0; ix < verticesToGenerate; ix++) {
                final Vertex v = g.addVertex();
                if (lastVertex.isPresent())
                    v.addEdge("parent", lastVertex.get());

                lastVertex = Optional.of(v);
            }

            AbstractBlueprintsSuite.assertVertexEdgeCounts(verticesToGenerate, verticesToGenerate - 1).accept(g);
        }
    }
}
