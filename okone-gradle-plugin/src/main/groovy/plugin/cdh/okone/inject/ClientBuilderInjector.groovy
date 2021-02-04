package plugin.cdh.okone.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.NotFoundException
import javassist.bytecode.AccessFlag
import plugin.cdh.okone.util.Printer

/**
 * 修改OkHttpClient.Builder
 */
class ClientBuilderInjector implements IClassInjector {

    private static final String TARGET_CLASS_NAME = "OkHttpClient\$Builder"

    @Override
    boolean handles(String name) {
        return name.endsWith("${TARGET_CLASS_NAME}.class")
    }

    @Override
    File inject(File workDir, ClassPool pool) {
        pool.importPackage("com.cdh.okone")

        // 获取OkHttpClient内部类Builder
        CtClass ctClass = pool.get("okhttp3.${TARGET_CLASS_NAME}")
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }

        // 添加一个map成员，用于记录自定义配置
        processConfigMapField(ctClass)
        // 修改所有可访问方法
        processAllMethod(ctClass)
        // 修改Builder#build方法
        processBuildMethod(ctClass)
        // 添加equivalentTo方法
        processEquivalentToMethod(ctClass)

        // 将修改后的还在内存中的代码重新写入文件
        ctClass.writeFile(workDir.absolutePath)
        ctClass.detach()

        // 返回修改后的类文件
        File injectedFile = new File(workDir.absolutePath +
                File.separator +
                "okhttp3" +
                File.separator +
                TARGET_CLASS_NAME +
                ".class")
        return injectedFile
    }

    private static void processConfigMapField(CtClass ctClass) {
        try {
            // 已经有oConfigMap，不再注入
            ctClass.getDeclaredField("oConfigMap")
            return
        } catch(NotFoundException e) {
        }

        // 给Builder添加一个oConfigMap成员
        String code = "public java.util.TreeMap oConfigMap = new java.util.TreeMap();"
        CtField configMap = CtField.make(code, ctClass)
        ctClass.addField(configMap)
    }

    private static void processAllMethod(CtClass ctClass) {
        CtMethod[] methods = ctClass.getDeclaredMethods()
        for (CtMethod method : methods) {
            if (AccessFlag.isPublic(method.getModifiers()) &&
                    "okhttp3.OkHttpClient\$Builder".equals(method.getReturnType().name) &&
                    !method.name.startsWith("-")) {
                // 筛选出Builder类中公开给调用方进行自定义配置相关的方法
                Printer.p("filter method name: ${method.name}")

                String src = """{com.cdh.okone.InjectHelper.BuilderHooker.hookBuilderSetConfig(\$0, "${method.name}", \$\$);}"""
                method.insertBefore(src)
            }
        }
    }

    private static void processEquivalentToMethod(CtClass ctClass) {
        // 给Builder添加一个equivalentTo方法
        String src = "public boolean equivalentTo(okhttp3.OkHttpClient\$Builder other) {return com.cdh.okone.InjectHelper.BuilderHooker.injectBuilderEquivalentTo(\$0, other);}"
        CtMethod method = CtMethod.make(src, ctClass)
        ctClass.addMethod(method)
    }

    private static void processBuildMethod(CtClass ctClass) {
        // 获取Builder#build方法
        CtMethod ctMethod = ctClass.getDeclaredMethod("build")
        // 使用hook代码覆盖build中方法体
        final String code = "return com.cdh.okone.InjectHelper.BuilderHooker.hookBuildOkHttpClient(\$0);"
        ctMethod.setBody(code)
    }
}