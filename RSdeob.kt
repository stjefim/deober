/**
 * 2019/01/04 Function [main]  is used to run the deobfuscator of Runescape code
 */

package client.deober

import client.deober.Util.ownersList
import client.deober.cfg.GraphCreator
import org.objectweb.asm.Label
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.io.InputStream
import java.util.jar.JarFile

/** 2019/01/04 Runescape jar file that will be used to run the game*/
const val JAR = "C:/Users/Julius/Downloads/gamepack_8505163.jar" //"C:/Users/Julius/Downloads/ex.jar"

/** 2019/01/04 Pass a string with directory of the jar file that we need to create the call graph for in the format C:/User/Example/example.jar*/
fun main(args: Array<String>){
    val jar = args[0]
    GraphCreator.createCallGraph(FileReader.readJar(File(jar))).edgeSet().forEach { print("\n" + it) }
    }