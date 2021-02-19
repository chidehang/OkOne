package plugin.cdh.okone.inject

import com.sun.org.apache.bcel.internal.generic.IRETURN
import jdk.internal.org.objectweb.asm.Opcodes.*
import org.objectweb.asm.*

/**
 * Created by chidehang on 2021/2/18
 */
class OkHttpClientInjector : BaseClassInjector() {

    override fun handles(name: String): Boolean {
        return name.endsWith("${TARGET_CLASS_NAME}.class")
    }

    override fun onInject(classWriter: ClassWriter): ClassVisitor {
        return ClientClassVisitor(classWriter)
    }

    private class ClientClassVisitor(
            classVisitor: ClassVisitor
    ) : ClassVisitor(Opcodes.ASM7, classVisitor) {

        override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor {
            if (name == "eventListenerFactory" && "Lokhttp3/EventListener\$Factory;" == descriptor) {
                // 移除final修饰，使之可以重新赋值
                access.and(ACC_FINAL.inv())
            }
            return super.visitField(access, name, descriptor, signature, value)
        }

        override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            if (mv != null) {
                if (name == "<init>" && "(Lokhttp3/OkHttpClient\$Builder;)V" == descriptor) {
                    // 修改OkHttpClient带参构造方法
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
            if ((opcode in IRETURN..RETURN) || opcode == ATHROW) {
                // 在方法结尾处给eventListenerFactory重新赋值
                mv.apply {
                    visitVarInsn(ALOAD, 0)
                    visitVarInsn(ALOAD, 0)
                    visitFieldInsn(GETFIELD, TARGET_CLASS_NAME, "eventListenerFactory", "Lokhttp3/EventListener\$Factory;")
                    visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$ClientHooker", "hookEventListenerFactory", "(Lokhttp3/EventListener\$Factory;)Lokhttp3/EventListener\$Factory;", false)
                    visitFieldInsn(PUTFIELD, TARGET_CLASS_NAME, "eventListenerFactory", "Lokhttp3/EventListener\$Factory;")
                }
            }
            super.visitInsn(opcode)
        }
    }

    companion object {
        private const val TARGET_CLASS_NAME = "okhttp3/OkHttpClient"
    }
}