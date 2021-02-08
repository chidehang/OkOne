package plugin.cdh.okone.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.Modifier
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import plugin.cdh.okone.util.Printer

/**
 * 修改Dispatcher类
 * 给readyAsyncCalls重新赋值
 */
class DispatcherInjector extends BaseClassInjector {

    private static final String TARGET_CLASS_NAME = "okhttp3/Dispatcher"

    @Override
    boolean handles(String name) {
        return name.endsWith("${TARGET_CLASS_NAME}.class")
    }

    @Override
    ClassVisitor onInject(ClassWriter classWriter) {
        return new DispatcherClassVisitor(classWriter)
    }

    private static class DispatcherClassVisitor extends ClassVisitor implements Opcodes {

        DispatcherClassVisitor(ClassVisitor classVisitor) {
            super(Opcodes.ASM7, classVisitor)
        }

        @Override
        FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            if ("readyAsyncCalls".equals(name) && "Ljava/util/ArrayDeque;".equals(descriptor)) {
                // 移除final修饰，使之可以重新赋值
                access &= ~ACC_FINAL
            }
            return super.visitField(access, name, descriptor, signature, value)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            if (mv != null) {
                if ("<init>".equals(name) && "()V".equals(descriptor)) {
                    // 检索到构造方法
                    return new ConstructMethodVisitor(mv)
                }
            }
            return mv
        }
    }

    private static class ConstructMethodVisitor extends MethodVisitor implements Opcodes {

        ConstructMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM7, methodVisitor)
        }

        @Override
        void visitInsn(int opcode) {
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                // 重新计算readyAsyncCalls的赋值
                mv.visitVarInsn(ALOAD, 0)
                mv.visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$DispatcherHooker", "hookReadyAsyncCalls", "()Ljava/util/ArrayDeque;", false)
                mv.visitFieldInsn(PUTFIELD, TARGET_CLASS_NAME, "readyAsyncCalls", "Ljava/util/ArrayDeque;")
            }
            super.visitInsn(opcode)
        }
    }
}