package org.apache.tinkerpop.gremlin.tinkergraph;


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

public class TinkerGraphMain {
    public static void main(String[] args) {
        //直接调用JAVA API 创建Graph对象
        TinkerGraph graph = TinkerGraph.open();


        //写入节点
        Vertex marko = graph.addVertex("name", "marko", "age", 29);
        Vertex lop = graph.addVertex("name", "lop", "lang", "java", "height", 173);
        Vertex jay = graph.addVertex("name", "jay", "lang", "python", "height", 160);

        //写入边
        marko.addEdge("created", lop, "weight", 0.6d);
        marko.addEdge("created", jay, "weight", 0.7d);

        //创建遍历器
        GraphTraversalSource g = graph.traversal();

        //g.V().has("name","marko").out("created").values("name");

        // like gremlin g.V().has("name", "marko").out("created").values("height");
        GraphTraversal allV = g.V();
        GraphTraversal hasName = allV.has("name", "marko");
        GraphTraversal outCreated = hasName.limit(10);
//        GraphTraversal valueName = outCreated.values("height");
        while (hasName.hasNext()){
               System.out.println(hasName.next());
        }
    }
}
