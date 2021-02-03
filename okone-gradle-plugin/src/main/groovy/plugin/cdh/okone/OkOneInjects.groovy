package plugin.cdh.okone

import javassist.ClassPool
import org.apache.commons.io.FileUtils
import plugin.cdh.okone.inject.AsyncCallInjector
import plugin.cdh.okone.inject.BaseClassInjector
import plugin.cdh.okone.inject.ClientBuilderInjector
import plugin.cdh.okone.inject.DispatcherInjector
import plugin.cdh.okone.inject.IClassInjector
import plugin.cdh.okone.inject.RequestInjector

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class OkOneInjects {

    static void inject(File srcFile, File destFile, ClassPool pool) {
        File tmpDir = new File(destFile.parentFile.absolutePath + File.separator + "_tmp")
        FileUtils.deleteDirectory(tmpDir)
        tmpDir.mkdirs()

        // 添加代码注入处理器
        List<BaseClassInjector> injectorList = new LinkedList<>()
//        injectorList.add(new ClientBuilderInjector())
//        injectorList.add(new RequestInjector())
//        injectorList.add(new AsyncCallInjector())
//        injectorList.add(new DispatcherInjector())

        // 创建新的目标产物jar文件
        destFile.createNewFile()
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(destFile))

        // 遍历原jar压缩包
        JarFile jarFile = new JarFile(srcFile)
        Iterator<JarEntry> iterator = jarFile.entries().iterator()
        while (iterator.hasNext()) {
            JarEntry entry = iterator.next()
            // 查找对应的代码注入处理器
            BaseClassInjector injector = matchInjector(injectorList, entry.name)
            if (injector != null) {
                println("匹配Injector: " + entry.name + " => " + injector)
                InputStream oldInput = new BufferedInputStream(jarFile.getInputStream(entry))
                // 注入代码，并返回修改后的内容
                byte[] injectedBytes = injector.inject(oldInput)
                // 写入注入修改后的内容
                jos.putNextEntry(new JarEntry(entry.name))
                jos.write(injectedBytes)
                jos.closeEntry()
                println("${entry.name}->写入成功")
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

    private static BaseClassInjector matchInjector(List<BaseClassInjector> injectorList, String name) {
        Iterator<BaseClassInjector> i = injectorList.iterator()
        while (i.hasNext()) {
            BaseClassInjector injector = i.next();
            if (injector.handles(name)) {
                i.remove()
                return injector
            }
        }
        return null
    }
}