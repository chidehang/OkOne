package plugin.cdh.okone.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.Modifier

/**
 * 修改Dispatcher类
 * 给readyAsyncCalls重新赋值
 */
class DispatcherInjector implements IClassInjector {

    private static final String TARGET_CLASS_NAME = "Dispatcher"

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

        pool.importPackage("com.cdh.okone")

        // 移除final修饰，使之可以重新赋值
        CtField ctField = ctClass.getDeclaredField("readyAsyncCalls")
        int modifier = ctField.getModifiers()
        modifier = modifier & ~Modifier.FINAL
        ctField.setModifiers(modifier)

        // 在构造方法中重新赋值
        String src = "readyAsyncCalls = com.cdh.okone.InjectHelper.DispatcherHooker.hookReadyAsyncCalls();"
        CtConstructor ctConstructor = ctClass.getDeclaredConstructor(new CtClass[0])
        println("Dispatcher构造方法: " + ctConstructor.getLongName())
        ctConstructor.insertBefore(src)

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