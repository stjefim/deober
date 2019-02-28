package client.deober.cfg

import client.deober.Util.key
import org.jgrapht.graph.DefaultEdge
import org.objectweb.asm.tree.MethodNode

class MethodEdge: DefaultEdge() {
    public override fun getSource(): MethodNode {
        return super.getSource() as MethodNode
    }

    public override fun getTarget(): MethodNode {
        return super.getTarget() as MethodNode
    }

    override fun toString(): String {
        return "(${source.key()} : ${target.key()})"
    }
}