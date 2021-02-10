package plugin.cdh.okone.inject

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*

/**
 * Created by chidehang on 2021/2/9
 */
class DispatcherInjector : BaseClassInjector() {

    override fun handles(name: String): Boolean {
        return name.endsWith("${TARGET_CLASS_NAME}.class")
    }

    override fun onInject(classWriter: ClassWriter): ClassVisitor {
        return DispatcherClassVisitor(classWriter)
    }

    private class DispatcherClassVisitor(
            classVisitor: ClassVisitor
    ) : ClassVisitor(Opcodes.ASM7, classVisitor) {

        override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor {
            if ("readyAsyncCalls" == name && "Ljava/util/ArrayDeque;" == descriptor) {
                // 移除final修饰，使之可以重新赋值
                access.and(ACC_FINAL.inv())
            }
            return super.visitField(access, name, descriptor, signature, value)
        }

        override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            val mv: MethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            if (mv != null) {
                if ("<init>" == name && "()V" == descriptor) {
                    // 检索到构造方法
                    return ConstructMethodVisitor(mv)
                }
            }
            return mv
        }
    }

    private class ConstructMethodVisitor(
            methodVisitor: MethodVisitor
    ) : MethodVisitor(Opcodes.ASM7, methodVisitor) {
        override fun visitInsn(opcode: Int) {
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                // 重新计算readyAsyncCalls的赋值
                mv.apply {
                    visitVarInsn(ALOAD, 0)
                    visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$DispatcherHooker", "hookReadyAsyncCalls", "()Ljava/util/ArrayDeque;", false)
                    visitFieldInsn(PUTFIELD, TARGET_CLASS_NAME, "readyAsyncCalls", "Ljava/util/ArrayDeque;")
                }
            }
            super.visitInsn(opcode)
        }
    }

    companion object {
        private const val TARGET_CLASS_NAME = "okhttp3/Dispatcher"
    }
}