package plugin.cdh.okone

import plugin.cdh.okone.inject.*
import plugin.cdh.okone.util.Printer
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * Created by chidehang on 2021/2/8
 */
class OkOneInjects {
    companion object {
        fun inject(srcFile: File, destFile: File) {
            Printer.p("准备修改okhttp.jar")

            // 添加代码注入处理器
            val injectorList = LinkedList<BaseClassInjector>()
            injectorList.add(DispatcherInjector())
            injectorList.add(AsyncCallInjector())
            injectorList.add(ClientBuilderInjector())
            injectorList.add(RequestInjector())
            injectorList.add(OkHttpClientInjector())

            // 创建新的目标产物jar文件
            destFile.createNewFile()
            val jos: JarOutputStream = JarOutputStream(FileOutputStream(destFile))

            // 遍历原jar压缩包
            val jarFile = JarFile(srcFile)
            val iterator = jarFile.entries().iterator()
            while (iterator.hasNext()) {
                val entry: JarEntry = iterator.next()
                // 查找对应的代码注入处理器
                val injector = matchInjector(injectorList, entry.name)
                if (injector != null) {
                    Printer.p("匹配Injector: " + entry.name + " => " + injector)
                    val oldInput = BufferedInputStream(jarFile.getInputStream(entry))
                    // 注入代码，并返回修改后的内容
                    val injectedBytes = injector.inject(oldInput)
                    Printer.p("${entry.name}->注入完成[length=${injectedBytes.size}]")
                    // 写入注入修改后的内容
                    jos.putNextEntry(JarEntry(entry.name))
                    jos.write(injectedBytes)
                    jos.closeEntry()
                    Printer.p("${entry.name}->写入成功")
                } else {
                    // 原样写入
                    jos.putNextEntry(JarEntry(entry.name))
                    val inputStream = jarFile.getInputStream(entry)
                    val buffer = ByteArray(1024 * 8)
                    var length = -1
                    while (inputStream.read(buffer).let { length = it; it != -1 }) {
                        jos.write(buffer, 0, length)
                    }
                }
            }

            jarFile.close()
            jos.flush()
            jos.close()
        }

        private fun matchInjector(injectorList: MutableList<BaseClassInjector>, name: String) : BaseClassInjector? {
            val i = injectorList.iterator()
            while (i.hasNext()) {
                val injector = i.next();
                if (injector.handles(name)) {
                    i.remove()
                    return injector
                }
            }
            return null
        }
    }
}