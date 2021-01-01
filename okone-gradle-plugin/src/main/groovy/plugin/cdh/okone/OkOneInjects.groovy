package plugin.cdh.okone

import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.Modifier
import javassist.NotFoundException
import javassist.bytecode.AccessFlag
import org.apache.commons.io.FileUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class OkOneInjects {

    public static final String TARGET_CLASS_NAME = "OkHttpClient\$Builder"

    static void inject(File srcFile, File destFile, ClassPool pool) {
        File tmpDir = new File(destFile.parentFile.absolutePath + File.separator + "_tmp")
        FileUtils.deleteDirectory(tmpDir)
        tmpDir.mkdirs()

        // 注入代码，并返回修改后的文件
        File injectedFile = injectCode(tmpDir, pool)

        // 创建新的目标产物jar文件
        destFile.createNewFile()
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(destFile))

        // 遍历原jar压缩包
        JarFile jarFile = new JarFile(srcFile)
        Iterator<JarEntry> iterator = jarFile.entries().iterator()
        while (iterator.hasNext()) {
            JarEntry entry = iterator.next()
            // 判断是否是被注入修改的文件
            if (entry.name.endsWith("${TARGET_CLASS_NAME}.class")) {
                println("拷贝注入修改后的class文件: " + injectedFile + " ==> " + injectedFile.exists() + ", " + entry.name)
                writeFile(jos, injectedFile, entry.name)
            } else {
                // 原样写入
                jos.putNextEntry(new JarEntry(entry.name))
                def inputStream = jarFile.getInputStream(entry)
                byte[] buffer = new byte[1024 * 8]
                int length = -1
                while ((length = inputStream.read(buffer)) != -1) {
                    jos.write(buffer, 0, length)
                }
            }
        }

        jarFile.close()
        jos.flush()
        jos.close()

        FileUtils.deleteDirectory(tmpDir)
    }

    private static File injectCode(File tmpDir, ClassPool pool) {
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
        ctClass.writeFile(tmpDir.absolutePath)
        ctClass.detach()

        // 返回修改后的类文件
        File injectedFile = new File(tmpDir.absolutePath +
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
        String code = "private java.util.TreeMap oConfigMap = new java.util.TreeMap();"
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
                println("filter method name: ${method.name}")

                String src = """{com.cdh.okone.InjectHelper.hookBuilderSetConfig(\$0, "${method.name}", \$\$);}"""
                method.insertBefore(src)
            }
        }
    }

    private static void processEquivalentToMethod(CtClass ctClass) {
        // 给Builder添加一个equivalentTo方法
        String src = "public boolean equivalentTo(okhttp3.OkHttpClient\$Builder other) {return com.cdh.okone.InjectHelper.injectBuilderEquivalentTo(\$0, other);}"
        CtMethod method = CtMethod.make(src, ctClass)
        ctClass.addMethod(method)
    }

    private static void processBuildMethod(CtClass ctClass) {
        // 获取Builder#build方法
        CtMethod ctMethod = ctClass.getDeclaredMethod("build")
        // 使用hook代码覆盖build中方法体
        final String code = "return com.cdh.okone.InjectHelper.hookBuildOkHttpClient(\$0);"
        ctMethod.setBody(code)
    }

    private static void writeFile(JarOutputStream jos, File file, String entryName) {
        jos.putNextEntry(new JarEntry(entryName))
        FileInputStream fis = new FileInputStream(file)
        byte[] buffer = new byte[1024 * 8]
        int length = -1;
        while ((length = fis.read(buffer)) != -1) {
            jos.write(buffer, 0, length)
        }
        fis.close()
    }
}