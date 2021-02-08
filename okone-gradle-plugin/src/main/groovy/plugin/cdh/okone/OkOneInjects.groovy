package plugin.cdh.okone

import javassist.ClassPool
import plugin.cdh.okone.inject.BaseClassInjector
import plugin.cdh.okone.inject.ClientBuilderInjector
import plugin.cdh.okone.inject.RequestInjector
import plugin.cdh.okone.util.Printer

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class OkOneInjects {

    static void inject(File srcFile, File destFile, ClassPool pool) {
        Printer.p("准备修改okhttp.jar")

        // 添加代码注入处理器
        List<BaseClassInjector> injectorList = new LinkedList<>()
        injectorList.add(new ClientBuilderInjector())
        injectorList.add(new RequestInjector())
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
                Printer.p("匹配Injector: " + entry.name + " => " + injector)
                InputStream oldInput = new BufferedInputStream(jarFile.getInputStream(entry))
                // 注入代码，并返回修改后的内容
                byte[] injectedBytes = injector.inject(oldInput)
                Printer.p("${entry.name}->注入完成[length=${injectedBytes.length}]")
                // 写入注入修改后的内容
                jos.putNextEntry(new JarEntry(entry.name))
                jos.write(injectedBytes)
                jos.closeEntry()
                Printer.p("${entry.name}->写入成功")
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