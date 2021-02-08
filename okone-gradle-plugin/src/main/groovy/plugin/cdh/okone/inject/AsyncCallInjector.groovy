package plugin.cdh.okone.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import plugin.cdh.okone.util.Printer

/**
 * 修改RealCall.AsyncCall
 * 添加compare方法
 */
class AsyncCallInjector extends BaseClassInjector {

    private static final String TARGET_CLASS_NAME = "RealCall\$AsyncCall"

    @Override
    boolean handles(String name) {
        return name.endsWith("${TARGET_CLASS_NAME}.class")
    }

    @Override
    ClassVisitor onInject(ClassWriter classWriter) {
        return new AsyncCallClassVisitor(classWriter)
    }

    private static class AsyncCallClassVisitor extends ClassVisitor implements Opcodes {

        AsyncCallClassVisitor(ClassVisitor classVisitor) {
            super(Opcodes.ASM7, classVisitor)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            // AsyncCall原为PRIVATE，外部无法访问，修改访问权限
            access |= ACC_PUBLIC
            // 添加Comparable接口
            String[] is = new String[interfaces.length + 1]
            for (int i=0; i<interfaces.length; i++) {
                is[i] = interfaces[i]
            }
            is[is.length-1] = "java/lang/Comparable"

            super.visit(version, access, name, signature, superName, is)
        }

        @Override
        void visitEnd() {
            if (cv != null) {
                // 生成compareTo方法
                MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "compareTo", "(Ljava/lang/Object;)I", null, null)
                if (mv != null) {
                    mv.visitCode()
                    mv.visitVarInsn(ALOAD, 0)
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$AsyncCallHooker", "hookCompareTo", "(Ljava/lang/Object;Ljava/lang/Object;)I", false)
                    mv.visitInsn(IRETURN)
                    mv.visitEnd()
                }
            }
            super.visitEnd()
        }
    }
}