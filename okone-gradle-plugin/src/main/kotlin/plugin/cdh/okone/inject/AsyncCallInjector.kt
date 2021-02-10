package plugin.cdh.okone.inject

import com.sun.xml.fastinfoset.util.StringArray
import jdk.internal.org.objectweb.asm.Opcodes.*
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by chidehang on 2021/2/9
 */
class AsyncCallInjector : BaseClassInjector() {

    override fun handles(name: String): Boolean {
        return name.endsWith("${TARGET_CLASS_NAME}.class")
    }

    override fun onInject(classWriter: ClassWriter): ClassVisitor {
        return AsyncCallClassVisitor(classWriter)
    }

    private class AsyncCallClassVisitor(
            classVisitor: ClassVisitor
    ) : ClassVisitor(Opcodes.ASM7, classVisitor) {

        override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
            // AsyncCall原为PRIVATE，外部无法访问，修改访问权限
            access.or(ACC_PUBLIC)
            // 添加Comparable接口
            var array = interfaces
            if (interfaces != null) {
                array = Array(interfaces.size + 1) {
                    if (it < interfaces.size) {
                        interfaces[it]
                    } else {
                        "java/lang/Comparable"
                    }
                }
            }

            super.visit(version, access, name, signature, superName, array)
        }

        override fun visitEnd() {
            if (cv != null) {
                // 生成compareTo方法
                val mv: MethodVisitor = cv.visitMethod(ACC_PUBLIC, "compareTo", "(Ljava/lang/Object;)I", null, null)
                mv?.apply {
                    visitCode()
                    visitVarInsn(ALOAD, 0)
                    visitVarInsn(ALOAD, 1)
                    visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$AsyncCallHooker", "hookCompareTo", "(Ljava/lang/Object;Ljava/lang/Object;)I", false)
                    visitInsn(IRETURN)
                    visitEnd()
                }
            }
            super.visitEnd()
        }
    }

    companion object {
        private const val TARGET_CLASS_NAME = "RealCall\$AsyncCall"
    }
}