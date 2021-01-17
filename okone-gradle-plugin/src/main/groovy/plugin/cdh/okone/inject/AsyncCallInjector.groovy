package plugin.cdh.okone.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier

/**
 * 修改RealCall.AsyncCall
 * 添加compare方法
 */
class AsyncCallInjector implements IClassInjector {

    private static final String TARGET_CLASS_NAME = "RealCall\$AsyncCall"

    @Override
    boolean handles(String name) {
        return name.endsWith("${TARGET_CLASS_NAME}.class")
    }

    @Override
    File inject(File workDir, ClassPool pool) {
        CtClass ctClass = pool.get("okhttp3.internal.connection.${TARGET_CLASS_NAME}")
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }

        pool.importPackage("com.cdh.okone")

        // AsyncCall原为PRIVATE，外部无法访问，修改访问权限
        ctClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL)

        // 添加Comparable接口，实现比较方法
        ctClass.addInterface(pool.get("java.lang.Comparable"))

        String src = """
                public int compareTo(okhttp3.internal.connection.RealCall\$AsyncCall o) {
                    return com.cdh.okone.InjectHelper.AsyncCallHooker.hookCompareTo(\$0, o);
                }
            """
        CtMethod ctMethod = CtMethod.make(src, ctClass)
        ctClass.addMethod(ctMethod)

        // 将修改后的还在内存中的代码重新写入文件
        ctClass.writeFile(workDir.absolutePath)
        ctClass.detach()

        // 返回修改后的类文件
        File injectedFile = new File(workDir.absolutePath +
                File.separator +
                "okhttp3" +
                File.separator +
                "internal" +
                File.separator +
                "connection" +
                File.separator +
                TARGET_CLASS_NAME +
                ".class")
        return injectedFile
    }
}