package plugin.cdh.okone

import javassist.ClassPool
import org.apache.commons.io.FileUtils
import plugin.cdh.okone.inject.AsyncCallInjector
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
        List<IClassInjector> injectorList = new LinkedList<>()
        injectorList.add(new ClientBuilderInjector())
        injectorList.add(new RequestInjector())
        injectorList.add(new AsyncCallInjector())
        injectorList.add(new DispatcherInjector())

        // 创建新的目标产物jar文件
        destFile.createNewFile()
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(destFile))

        // 遍历原jar压缩包
        JarFile jarFile = new JarFile(srcFile)
        Iterator<JarEntry> iterator = jarFile.entries().iterator()
        while (iterator.hasNext()) {
            JarEntry entry = iterator.next()
            // 查找对应的代码注入处理器
            IClassInjector injector = matchInjector(injectorList, entry.name)
            if (injector != null) {
                println("匹配Injector: " + entry.name + " => " + injector)
                // 注入代码，并返回修改后的文件
                File injectedFile = injector.inject(tmpDir, pool)
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

    private static IClassInjector matchInjector(List<IClassInjector> injectorList, String name) {
        Iterator<IClassInjector> i = injectorList.iterator();
        while (i.hasNext()) {
            IClassInjector injector = i.next();
            if (injector.handles(name)) {
                i.remove()
                return injector
            }
        }
        return null
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