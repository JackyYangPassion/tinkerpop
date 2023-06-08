package org.apache.tinkerpop.gremlin.tinkergraph;


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

public class TinkerGraphMain {
    public static void main(String[] args) {
        TinkerGraph graph = TinkerGraph.open();
        Vertex marko = graph.addVertex("name", "marko", "age", 29);
        Vertex lop = graph.addVertex("name", "lop", "lang", "java", "height", 173);
        Vertex jay = graph.addVertex("name", "jay", "lang", "python", "height", 160);
        marko.addEdge("created", lop, "weight", 0.6d);
        marko.addEdge("created", jay, "weight", 0.7d);
        GraphTraversalSource g = graph.traversal();

        // like gremlin g.V().has("name", "marko").out("created").values("height");
        GraphTraversal allV = g.V();
        GraphTraversal hasName = allV.has("name", "marko");
        GraphTraversal outCreated = hasName.out("created");
        GraphTraversal valueName = outCreated.values("height");
        Object next = valueName.next();
        System.out.println(next);
    }
}
