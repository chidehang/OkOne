package plugin.cdh.okone.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.NotFoundException

/**
 * 修改Request类，添加表示优先级的成员变量
 */
class RequestInjector implements IClassInjector {

    private static final String TARGET_CLASS_NAME = "Request"

    @Override
    boolean handles(String name) {
        return name.endsWith("okhttp3${File.separator}${TARGET_CLASS_NAME}.class")
    }

    @Override
    File inject(File workDir, ClassPool pool) {
        CtClass ctClass = pool.get("okhttp3.${TARGET_CLASS_NAME}")
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }

        try {
            // 已经有priority，不再注入
            ctClass.getDeclaredField("priority")
            return
        } catch(NotFoundException e) {
        }

        // 注入priority成员变量
        String src = "public int priority = 0;"
        ctClass.addField(CtField.make(src, ctClass))

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
}