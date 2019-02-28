/**
 * 2019/01/04 [FileReader] object contains method that can scan the given input and produce a asm class tree of that input.
 */

package client.deober

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.ClassReader
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM6
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.jar.JarFile

object FileReader {
    /** 2019/01/07 Reads the given jar and produces a set of [ClassNode] for each of the classes in the jar file */
    fun readJar(jar: File): Set<ClassNode>{
        /** 2019/01/07 [classes] is the set which will hold the scanned [jar] clases*/
        val classes = HashSet<ClassNode>()
        /** 2019/01/07 [entries] is the set of entries of the [jar]. Each entry represents a different file contained in the [jar]*/
        val entries = JarFile(jar).entries()
        for(entry in entries){
          /** 2019/01/07 We only want to build a class tree thus all other files contained in the [jar] are not used */
          if(entry.name.endsWith(".class")){
              /** 2019/01/07 Each individual class of the [jar] is being [read] and for each of the classes a [ClassNode] representation is made*/
              val cla = read(JarFile(jar).getInputStream(entry))
              if(cla != null) classes.add(cla)
          }
        }
        return classes
    }
    /** 2019/01/07 Function [createMethodTree] is used to make the tree that represents all the methods that are being called */
    fun createMethodTree(classes: Set<ClassNode>): Map<Pair<Pair<String,String>,String>,Set<MethodNode>>{
        /** 2019/01/07 [methodTree] is the structure of the tree.
         * The [key] (Pair<Pair<(1)String,(2)String>,(3)String>) is itself a pair and the first entry of the pair(Pair<String,String>)
         * first entry (1)[String] represents the methods that is calling another method owners class name
         * the second entry (2)[String] represents the methods that is calling another method name
         * the second entry of the pair (3)[String] represents the methods that is being called owners class name
         * and the [value] ((4)Set<MethodNode>) of the map is the set of [MethodNode] that will be called by the original classes (1)
         * method (2) and is in the class (3) */
        val methodTree = HashMap<Pair<Pair<String,String>,String>,HashSet<MethodNode>>()
        /** 2019/01/07 [methods] is the map that can be used to get the [MethodNode] by knowing the name of the owners class and the name of the method that the node represents.
         * The [key] (Pair<(1)String,(2)String>) is a pair of which
         * the first entry (1)[String] represents the method owners class name
         * the second entry (2)[String] represents the methods name
         * and the [value] of the map is [MethodNode] associated with the owner class name (1) and the methods name (2) */
        val methods = HashMap<Pair<String,String>,MethodNode>()
        /** 2019/01/07 [ownernames] is the names of the classes of the jar file */
        val ownernames = HashSet<String>()
        /** 2019/01/07 Adds the names of the classes analyzed */
        classes.forEach { ownernames.add(it.name)}
        /** 2019/01/07 Associates keys(owner class name, method name) with a [MethodNode] in the analyzed class */
        classes.forEach { owner -> owner.methods.forEach { methods[Pair(owner.name,it.name)] = it } }
        /** 2019/01/07 Creates the method tree.
         * [sourceclass] will be used as the (1) parameter of the [methodTree]
         * [sourcemethod] will be used as the (2) parameter of the [methodTree] */
        classes.forEach { sourceclass -> sourceclass.methods.forEach { sourcemethod ->
            /** 2019/01/07 Each of the methods are visited again by a [MethodVisitor] which is initialized by object expression
             * the visitor [MethodVisitor] behavior when encountering a method call is changed to extract the information that we need to create the [methodTree] */
            sourcemethod.accept(object: MethodVisitor(ASM6) {
                /** 2019/01/07 We override the [visitMethodInsn] fun to behave as we want it to */
                override fun visitMethodInsn(
                    opcode: Int,
                    owner: String?,
                    name: String?,
                    descriptor: String?,
                    isInterface: Boolean
                ){  /** 2019/01/07 [owner] represents the methods owner class name which will be used as the (3) parameter of the [methodTree]
                    * [name] represents the methods that is beeing visited name
                    * Now if in our [methods] map we have have the [MethodNode] that is associated with the [owner] and [name] then we can use it
                    * as the (4) paramater of the [methodTree] thus we can now construct the tree */
                    if (owner != null && name != null && methods[Pair(owner,name)] != null) {
                        if (owner in ownernames) {
                            /** 2019/01/07 If the set associated with the [key] (Pair(Pair(sourceclass.name,sourcemethod.name),owner)) is already initialized
                             * then we only need to add the [MethodNode] to the set otherwise we create a new set containing that [MethodNode]*/
                            if (methodTree.containsKey(Pair(Pair(sourceclass.name,sourcemethod.name),owner)) && methodTree[Pair(Pair(sourceclass.name,sourcemethod.name),owner)] != null) {
                                methodTree[Pair(Pair(sourceclass.name,sourcemethod.name),owner)]!!.add(methods[Pair(owner,name)]!!)
                            } else {
                                methodTree[Pair(Pair(sourceclass.name,sourcemethod.name),owner)] = hashSetOf(methods[Pair(owner,name)]!!)
                            }
                        }
                    }
                }
            })
        }
        }
        return methodTree
    }

    /** 2019/01/04 [read] function reads the given [InputStream] andutputs the [ClassNode] that we get from that input stream */
    fun read(input: InputStream): ClassNode?{
      try {
          /** 2019/01/07 The [ClassReader] from asm library that visits the [input] and makes a class representation */
          val reader = ClassReader(input)
          /** 2019/01/07 For analysis purposes the we need to use the tree asm visitor which constructs a better representation of the class,
           * thus allowing to extract more information. [ClassNode] is the visitor that construct a tree representation of the given class */
          val node = ClassNode()
          /** 2019/01/07 [node] visits the classes already visited by [reader] and constructs better representations. The items indenified as debug or frames are skipped */
          reader.accept(node, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
          return node
          /** 2019/01/07 The [reader] throws an exception if the [input] is in wrong format*/
      } catch (e: IOException){
          e.printStackTrace()
          return null
      }
    }

}
