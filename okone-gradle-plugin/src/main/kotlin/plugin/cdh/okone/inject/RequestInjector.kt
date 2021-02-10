package plugin.cdh.okone.inject

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import plugin.cdh.okone.util.Const
import java.io.File

/**
 * 修改Request类，添加表示优先级的成员变量
 * Created by chidehang on 2021/2/8
 */
class RequestInjector : BaseClassInjector() {

    override fun handles(name: String): Boolean {
        return name.endsWith("okhttp3${File.separator}${TARGET_CLASS_NAME}.class")
    }

    override fun onInject(classWriter: ClassWriter): ClassVisitor {
        return RequestClassVisitor(classWriter)
    }

    private class RequestClassVisitor(
        classWriter: ClassWriter
    ) : ClassVisitor(Opcodes.ASM7, classWriter), Opcodes {

        private var isFieldPresent = false

        private val fieldName = "${Const.GEN_PREFIX}priority"

        override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor {
            if (fieldName.equals(name)) {
                isFieldPresent = true
            }
            return super.visitField(access, name, descriptor, signature, value)
        }

        override fun visitEnd() {
            if (!isFieldPresent && cv != null) {
                val fieldVisitor: FieldVisitor = cv.visitField(Opcodes.ACC_PUBLIC, fieldName, "I", null, null)
                fieldVisitor?.visitEnd()
            }
            super.visitEnd()
        }
    }

    companion object {
        private const val TARGET_CLASS_NAME = "Request"
    }
}