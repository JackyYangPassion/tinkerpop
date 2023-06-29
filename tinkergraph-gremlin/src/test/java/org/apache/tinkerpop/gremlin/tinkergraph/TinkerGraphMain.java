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


        // like gremlin g.V().has("name", "marko").out("created").values("height");
        GraphTraversal allV = g.V();
        GraphTraversal hasName = allV.has("name", "marko");//1. 此处在什么地方过滤
        GraphTraversal outCreated = hasName.out("created");//2. 同样下推到什么地方
        GraphTraversal valueName = outCreated.values("height");
        GraphTraversal outLimit = valueName.limit(10);
        System.out.println(outLimit.next());
//        GraphTraversal valueName = outCreated.values("height");
//        while (hasName.hasNext()){
//            System.out.println(outLimit.next());
//        }
    }
}
