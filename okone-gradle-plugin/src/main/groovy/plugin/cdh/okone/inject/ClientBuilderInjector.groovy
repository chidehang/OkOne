package plugin.cdh.okone.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.NotFoundException
import javassist.bytecode.AccessFlag
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import plugin.cdh.okone.util.Const
import plugin.cdh.okone.util.Printer

/**
 * 修改OkHttpClient.Builder
 */
class ClientBuilderInjector extends BaseClassInjector {

    private static final String TARGET_CLASS_NAME = "okhttp3/OkHttpClient\$Builder"

    @Override
    boolean handles(String name) {
        return name.endsWith("${TARGET_CLASS_NAME}.class")
    }

    @Override
    ClassVisitor onInject(ClassWriter classWriter) {
        return new BuilderClassVisitor(classWriter)
    }

    private static void processEquivalentToMethod(CtClass ctClass) {
        // 给Builder添加一个equivalentTo方法
        String src = "public boolean equivalentTo(okhttp3.OkHttpClient\$Builder other) {return com.cdh.okone.InjectHelper.BuilderHooker.injectBuilderEquivalentTo(\$0, other);}"
        CtMethod method = CtMethod.make(src, ctClass)
        ctClass.addMethod(method)
    }

    private static class BuilderClassVisitor extends ClassVisitor implements Opcodes {

        // 目标成员是否已存在
        private boolean isFieldPresent

        private final String fieldName = "${Const.GEN_PREFIX}configMap"

        BuilderClassVisitor(ClassVisitor classVisitor) {
            super(Opcodes.ASM7, classVisitor)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            if (mv != null) {
                if ("<init>".equals(name) && "()V".equals(descriptor)) {
                    // 检索到构造方法
                    return new ConstructMethodVisitor(mv)
                } else if ((access&ACC_PUBLIC) != 0 && !name.startsWith("-") &&
                        "okhttp3.OkHttpClient\$Builder".equals(Type.getReturnType(descriptor).getClassName())) {
                    // 检索到公开配置方法
                    return matchMethodVisitor(mv, name, descriptor)
                } else if ("build".equals(name) && "()Lokhttp3/OkHttpClient;".equals(descriptor)) {
                    return new BuildMethodVisitor(mv)
                }
            }
            return mv
        }

        private MethodVisitor matchMethodVisitor(MethodVisitor mv, String name, String descriptor) {
            MethodVisitor visitor = null

            // 根据方法参数的个数和类型，使用对应的MethodVisitor进行代码注入
            Type[] args = Type.getArgumentTypes(descriptor)
            if (args.length == 1) {
                if (args[0].getSort() == Type.BOOLEAN) {
                    visitor = new RecordZMethodVisitor(mv, name)
                } else if (args[0].getSort() == Type.LONG) {
                    visitor = new RecordJMethodVisitor(mv, name)
                } else if (args[0].getSort() == Type.OBJECT) {
                    visitor = new RecordLMethodVisitor(mv, name)
                }
            } else if (args.length == 2) {
                if (args[0].getSort() == Type.LONG && args[1].getSort() == Type.OBJECT) {
                    visitor = new RecordJLMethodVisitor(mv, name)
                } else if (args[0].getSort() == Type.OBJECT && args[1].getSort() == Type.OBJECT) {
                    visitor = new RecordLLMethodVisitor(mv, name)
                }
            }

            if (visitor == null) {
                Printer.p("!!!警告：有遗漏的公开配置方法($name)未修改注入!!!")
                visitor = mv
            }

            Printer.p("编织okhttp3/OkHttpClient\$Builder的公开配置方法: $name, $descriptor by ${visitor}")
            return visitor
        }

        @Override
        FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            if (fieldName.equals(name)) {
                isFieldPresent = true
            }
            return super.visitField(access, name, descriptor, signature, value)
        }

        @Override
        void visitEnd() {
            if (cv != null) {
                if (!isFieldPresent) {
                    // 生成okone_configMap成员
                    FieldVisitor fieldVisitor = cv.visitField(ACC_PUBLIC, fieldName, "Ljava/util/TreeMap;", null, null)
                    if (fieldVisitor != null) {
                        fieldVisitor.visitEnd()
                    }
                }

                // 生成okone_equivalentTo方法
                MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "${Const.GEN_PREFIX}equivalentTo", "(Lokhttp3/OkHttpClient\$Builder;)Z", null, null)
                if (mv != null) {
                    mv.visitCode()
                    mv.visitVarInsn(ALOAD, 0)
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "injectBuilderEquivalentTo", "(Lokhttp3/OkHttpClient\$Builder;Lokhttp3/OkHttpClient\$Builder;)Z", false)
                    mv.visitInsn(IRETURN)
                    mv.visitEnd()
                }
            }
            super.visitEnd()
        }
    }

    private static class ConstructMethodVisitor extends MethodVisitor implements Opcodes {

        ConstructMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM7, methodVisitor)
        }

        @Override
        void visitInsn(int opcode) {
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                Printer.p("编织okhttp3/OkHttpClient\$Builder<init>")
                // 在构造方法返回处插入
                mv.visitVarInsn(ALOAD, 0)
                mv.visitTypeInsn(NEW, "java/util/TreeMap")
                mv.visitInsn(DUP)
                mv.visitMethodInsn(INVOKESPECIAL, "java/util/TreeMap", "<init>", "()V", false)
                mv.visitFieldInsn(PUTFIELD, TARGET_CLASS_NAME, "${Const.GEN_PREFIX}configMap", "Ljava/util/TreeMap;")
            }
            super.visitInsn(opcode)
        }
    }

    private static class BaseRecordMethodVisitor extends MethodVisitor implements Opcodes {
        protected final String methodName
        BaseRecordMethodVisitor(MethodVisitor methodVisitor, String name) {
            super(Opcodes.ASM7, methodVisitor)
            this.methodName = name
        }
    }

    private static class RecordLMethodVisitor extends BaseRecordMethodVisitor {

        RecordLMethodVisitor(MethodVisitor methodVisitor, String name) {
            super(methodVisitor, name)
        }

        @Override
        void visitCode() {
            super.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitLdcInsn(methodName)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuilderSetConfig", "(Lokhttp3/OkHttpClient\$Builder;Ljava/lang/String;Ljava/lang/Object;)V", false)
        }
    }

    private static class RecordZMethodVisitor extends BaseRecordMethodVisitor {
        RecordZMethodVisitor(MethodVisitor methodVisitor, String name) {
            super(methodVisitor, name)
        }

        @Override
        void visitCode() {
            super.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitLdcInsn(methodName)
            mv.visitVarInsn(ILOAD, 1)
            mv.visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuilderSetConfig", "(Lokhttp3/OkHttpClient\$Builder;Ljava/lang/String;Z)V", false)
        }
    }

    private static class RecordJMethodVisitor extends BaseRecordMethodVisitor {
        RecordJMethodVisitor(MethodVisitor methodVisitor, String name) {
            super(methodVisitor, name)
        }

        @Override
        void visitCode() {
            super.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitLdcInsn(methodName)
            mv.visitVarInsn(LLOAD, 1)
            mv.visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuilderSetConfig", "(Lokhttp3/OkHttpClient\$Builder;Ljava/lang/String;J)V", false)
        }
    }

    private static class RecordJLMethodVisitor extends BaseRecordMethodVisitor {
        RecordJLMethodVisitor(MethodVisitor methodVisitor, String name) {
            super(methodVisitor, name)
        }

        @Override
        void visitCode() {
            super.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitLdcInsn(methodName)
            mv.visitVarInsn(LLOAD, 1)
            mv.visitVarInsn(ALOAD, 3)
            mv.visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuilderSetConfig", "(Lokhttp3/OkHttpClient\$Builder;Ljava/lang/String;JLjava/lang/Object;)V", false)
        }
    }

    private static class RecordLLMethodVisitor extends BaseRecordMethodVisitor {
        RecordLLMethodVisitor(MethodVisitor methodVisitor, String name) {
            super(methodVisitor, name)
        }

        @Override
        void visitCode() {
            super.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitLdcInsn(methodName)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitVarInsn(ALOAD, 2)
            mv.visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuilderSetConfig", "(Lokhttp3/OkHttpClient\$Builder;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", false)
        }
    }

    private static class BuildMethodVisitor extends MethodVisitor implements Opcodes {

        BuildMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM7, methodVisitor)
        }

        @Override
        void visitCode() {
            Printer.p("编织okhttp3/OkHttpClient\$Builder.build方法")
            // 完全替换原build方法
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuildOkHttpClient", "(Lokhttp3/OkHttpClient\$Builder;)Lokhttp3/OkHttpClient;", false)
            mv.visitInsn(ARETURN)
        }
    }
}