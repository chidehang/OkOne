package plugin.cdh.okone.inject

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import plugin.cdh.okone.util.Const
import plugin.cdh.okone.util.Printer

/**
 * Created by chidehang on 2021/2/8
 */
class ClientBuilderInjector : BaseClassInjector() {

    override fun handles(name: String): Boolean {
        return name.endsWith("${TARGET_CLASS_NAME}.class")
    }

    override fun onInject(classWriter: ClassWriter): ClassVisitor {
        return BuilderClassVisitor(classWriter)
    }

    private class BuilderClassVisitor(
            classVisitor: ClassVisitor
    ) : ClassVisitor(Opcodes.ASM7, classVisitor) {

        // 目标成员是否已存在
        var isFieldPresent = false

        private val fieldName = "${Const.GEN_PREFIX}configMap"

        override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            if (mv != null) {
                if ("<init>" == name && "()V" == descriptor) {
                    // 检索到构造方法
                    return ConstructMethodVisitor(mv)
                } else if ((access and ACC_PUBLIC) != 0 && !name.startsWith("-") && "okhttp3.OkHttpClient\$Builder" == Type.getReturnType(descriptor).className) {
                    // 检索到公开配置方法
                    return matchMethodVisitor(mv, name, descriptor)
                } else if ("build" == name && "()Lokhttp3/OkHttpClient;" == descriptor) {
                    return BuildMethodVisitor(mv)
                }
            }
            return mv
        }

        private fun matchMethodVisitor(mv: MethodVisitor, name: String, descriptor: String) : MethodVisitor {
            var visitor: MethodVisitor? = null

            // 根据方法参数的个数和类型，使用对应的MethodVisitor进行代码注入
            val args = Type.getArgumentTypes(descriptor)
            when (args.size) {
                1 -> {
                    if (args[0].sort == Type.BOOLEAN) {
                        visitor = RecordZMethodVisitor(mv, name)
                    } else if (args[0].sort == Type.LONG) {
                        visitor = RecordJMethodVisitor(mv, name)
                    } else if (args[0].sort == Type.OBJECT) {
                        visitor = RecordLMethodVisitor(mv, name)
                    }
                }
                2 -> {
                    if (args[0].sort == Type.LONG && args[1].sort == Type.OBJECT) {
                        visitor = RecordJLMethodVisitor(mv, name)
                    } else if (args[0].sort == Type.OBJECT && args[1].sort == Type.OBJECT) {
                        visitor = RecordLLMethodVisitor(mv, name)
                    }
                }
            }

            if (visitor == null) {
                Printer.p("!!!警告：有遗漏的公开配置方法($name)未修改注入!!!")
                visitor = mv
            }

            Printer.p("编织okhttp3/OkHttpClient\$Builder的公开配置方法: $name, $descriptor by $visitor")
            return visitor
        }

        override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor {
            if (fieldName == name) {
                isFieldPresent = true
            }
            return super.visitField(access, name, descriptor, signature, value)
        }

        override fun visitEnd() {
            cv?.apply {
                if (!isFieldPresent) {
                    // 生成okone_configMap成员
                    val fieldVisitor: FieldVisitor = visitField(ACC_PUBLIC, fieldName, "Ljava/util/TreeMap;", null, null)
                    fieldVisitor?.visitEnd()
                }

                // 生成okone_equivalentTo方法
                val mv: MethodVisitor = cv.visitMethod(ACC_PUBLIC, "${Const.GEN_PREFIX}equivalentTo", "(Lokhttp3/OkHttpClient\$Builder;)Z", null, null)
                mv?.apply {
                    visitCode()
                    visitVarInsn(ALOAD, 0)
                    visitVarInsn(ALOAD, 1)
                    visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "injectBuilderEquivalentTo", "(Lokhttp3/OkHttpClient\$Builder;Lokhttp3/OkHttpClient\$Builder;)Z", false)
                    visitInsn(IRETURN)
                    visitEnd()
                }
            }
            super.visitEnd()
        }
    }

    private class ConstructMethodVisitor(
            methodVisitor: MethodVisitor
    ) : MethodVisitor(Opcodes.ASM7, methodVisitor) {
        override fun visitInsn(opcode: Int) {
            if ((opcode in IRETURN..RETURN) || opcode == ATHROW) {
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

    private open class BaseRecordMethodVisitor(
            methodVisitor: MethodVisitor,
            val methodName: String
    ) : MethodVisitor(Opcodes.ASM7, methodVisitor)

    private class RecordLMethodVisitor(
            methodVisitor: MethodVisitor,
            methodName: String
    ) : BaseRecordMethodVisitor(methodVisitor, methodName) {
        override fun visitCode() {
            super.visitCode()
            mv.apply {
                visitVarInsn(ALOAD, 0)
                visitLdcInsn(methodName)
                visitVarInsn(ALOAD, 1)
                visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuilderSetConfig", "(Lokhttp3/OkHttpClient\$Builder;Ljava/lang/String;Ljava/lang/Object;)V", false)
            }
        }
    }

    private class RecordZMethodVisitor(
            methodVisitor: MethodVisitor,
            methodName: String
    ) : BaseRecordMethodVisitor(methodVisitor, methodName) {
        override fun visitCode() {
            super.visitCode()
            mv.apply {
                visitVarInsn(ALOAD, 0)
                visitLdcInsn(methodName)
                visitVarInsn(ILOAD, 1)
                visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuilderSetConfig", "(Lokhttp3/OkHttpClient\$Builder;Ljava/lang/String;Z)V", false)
            }
        }
    }

    private class RecordJMethodVisitor(
            methodVisitor: MethodVisitor,
            methodName: String
    ) : BaseRecordMethodVisitor(methodVisitor, methodName) {
        override fun visitCode() {
            super.visitCode()
            mv.apply {
                visitVarInsn(ALOAD, 0)
                visitLdcInsn(methodName)
                visitVarInsn(LLOAD, 1)
                visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuilderSetConfig", "(Lokhttp3/OkHttpClient\$Builder;Ljava/lang/String;J)V", false)
            }
        }
    }

    private class RecordJLMethodVisitor(
            methodVisitor: MethodVisitor,
            methodName: String
    ) : BaseRecordMethodVisitor(methodVisitor, methodName) {
        override fun visitCode() {
            super.visitCode()
            mv.apply {
                visitVarInsn(ALOAD, 0)
                visitLdcInsn(methodName)
                visitVarInsn(LLOAD, 1)
                visitVarInsn(ALOAD, 3)
                visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuilderSetConfig", "(Lokhttp3/OkHttpClient\$Builder;Ljava/lang/String;JLjava/lang/Object;)V", false)
            }
        }
    }

    private class RecordLLMethodVisitor(
            methodVisitor: MethodVisitor,
            methodName: String
    ) : BaseRecordMethodVisitor(methodVisitor, methodName) {
        override fun visitCode() {
            super.visitCode()
            mv.apply {
                visitVarInsn(ALOAD, 0)
                visitLdcInsn(methodName)
                visitVarInsn(ALOAD, 1)
                visitVarInsn(ALOAD, 2)
                visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuilderSetConfig", "(Lokhttp3/OkHttpClient\$Builder;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", false)
            }
        }
    }

    private class BuildMethodVisitor(
            methodVisitor: MethodVisitor
    ) : MethodVisitor(Opcodes.ASM7, methodVisitor) {
        override fun visitCode() {
            Printer.p("编织okhttp3/OkHttpClient\$Builder.build方法")
            // 完全替换原build方法
            mv.apply {
                visitVarInsn(ALOAD, 0)
                visitMethodInsn(INVOKESTATIC, "com/cdh/okone/InjectHelper\$BuilderHooker", "hookBuildOkHttpClient", "(Lokhttp3/OkHttpClient\$Builder;)Lokhttp3/OkHttpClient;", false)
                visitInsn(ARETURN)
            }
        }
    }

    companion object {
        private const val TARGET_CLASS_NAME = "okhttp3/OkHttpClient\$Builder"
    }
}