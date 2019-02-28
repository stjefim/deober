package client.deober.cfg

import client.deober.FileReader
import client.deober.Util.owner
import client.deober.Util.owners
import client.deober.Util.ownersList
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.io.File

object GraphCreator{
    fun createCallGraph(classes: Set<ClassNode>): DefaultDirectedGraph<MethodNode, MethodEdge>{
        val graph = DefaultDirectedGraph<MethodNode, MethodEdge>(MethodEdge::class.java)
        val classmethods = mapClassesToMethods(classes)
        val methodTree = FileReader.createMethodTree(classes)
        ownersList = owners(classes)
        addVertices(classmethods,methodTree,graph)
        addEdges(classmethods,methodTree,graph)
        //graph.edgeSet().forEach { print("\n" + it)  }
        return graph
    }

    fun addVertices(classmethods: HashMap<String,Set<MethodNode>>, methodTree: Map<Pair<Pair<String,String>,String>,Set<MethodNode>>, graph: DefaultDirectedGraph<MethodNode, MethodEdge>){
        methodTree.forEach {node ->
            if (node.key.first.first in classmethods.keys ){
                graph.addVertex(classmethods[node.key.first.first]!!.filter { it.name == node.key.first.second  }.first())
            }
            if (node.key.second in classmethods.keys){
                node.value.forEach {
                    graph.addVertex(it)
                }
            }
        }
    }

    fun addEdges(classmethods: HashMap<String,Set<MethodNode>>, methodTree: Map<Pair<Pair<String,String>,String>,Set<MethodNode>>, graph: DefaultDirectedGraph<MethodNode, MethodEdge>){
        methodTree.forEach { method ->
            if (method.key.first.first in classmethods.keys){
                method.value.forEach {
                    graph.addEdge(classmethods[method.key.first.first]!!.filter { it.name == method.key.first.second  }.first(), it)
                }
            }
        }
    }

    fun mapClassesToMethods(classes: Set<ClassNode>): HashMap<String,Set<MethodNode>>{
        val classmethods = HashMap<String,Set<MethodNode>>()
        classes.forEach { clas ->
            val methods = hashSetOf<MethodNode>()
            clas.methods.forEach {
                methods.add(it)
            }
            classmethods[clas.name] = methods
        }
        return classmethods
    }

    fun simplify(graph: DefaultDirectedGraph<MethodNode, MethodEdge>): DefaultDirectedGraph<MethodNode, MethodEdge>{
        val simpleGraph = DefaultDirectedGraph<MethodNode, MethodEdge>(MethodEdge::class.java)
        graph.edgeSet().forEach {
            if(graph.incomingEdgesOf(it.source).size != 0 || it.source.owner.name == "client"){
                simpleGraph.addVertex(it.source)
                simpleGraph.addVertex(it.target)
                simpleGraph.addEdge(it.source,it.target)
            }
        }
        return simpleGraph
    }
}