package client.deober.Util

import client.deober.FileReader
import client.deober.JAR
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.io.File

var ownersList = owners(FileReader.readJar(File(JAR)))
val MethodNode.owner: ClassNode
    get() = ownersList[this.hashCode()]!!

fun MethodNode.key(): String{
    return ("${ownersList[this.hashCode()]!!.name}.${this.name}${this.desc}")
}
fun owners(classes: Set<ClassNode>): Map<Int,ClassNode>{
    val map = hashMapOf<Int,ClassNode>()
    classes.forEach { clas ->
        clas.methods.forEach { map[it.hashCode()] = clas }
    }
    return map
}