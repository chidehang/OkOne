package plugin.cdh.okone.inject

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.InputStream

/**
 * Created by chidehang on 2021/2/8
 */
abstract class BaseClassInjector {

    fun inject(inputStream: InputStream) : ByteArray {
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val classVisitor = onInject(classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    abstract fun handles(name: String) : Boolean

    abstract fun onInject(classWriter: ClassWriter) : ClassVisitor
}