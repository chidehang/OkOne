package plugin.cdh.okone.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.NotFoundException
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import plugin.cdh.okone.util.Const

/**
 * 修改Request类，添加表示优先级的成员变量
 */
class RequestInjector extends BaseClassInjector {

    private static final String TARGET_CLASS_NAME = "Request"

    @Override
    boolean handles(String name) {
        return name.endsWith("okhttp3${File.separator}${TARGET_CLASS_NAME}.class")
    }

    @Override
    ClassVisitor onInject(ClassWriter classWriter) {
        return new RequestClassVisitor(classWriter)
    }

    static class RequestClassVisitor extends ClassVisitor {

        // 目标成员是否已存在
        private boolean isFieldPresent

        private final String fieldName = "${Const.GEN_PREFIX}priority"

        RequestClassVisitor(ClassWriter classWriter) {
            super(Opcodes.ASM7, classWriter)
        }

        @Override
        FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            if (fieldName.equals(name)) {
                isFieldPresent = true
            }
            return super.visitField(access, name, descriptor, signature, value)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        }

        @Override
        void visitEnd() {
            if (!isFieldPresent && cv != null) {
                FieldVisitor fieldVisitor = cv.visitField(Opcodes.ACC_PUBLIC, fieldName, "I", null, null)
                if (fieldVisitor != null) {
                    fieldVisitor.visitEnd()
                }
            }
            super.visitEnd()
        }
    }
}