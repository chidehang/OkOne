package plugin.cdh.okone

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import plugin.cdh.okone.util.Printer
import java.io.File
import java.util.jar.JarFile
import java.util.regex.Pattern

/**
 * Created by chidehang on 2021/2/8
 */
class OkOneTransform : Transform() {

    override fun getName(): String {
        return "OkOneTransform"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        Printer.p(">>> transform OkOneTransform <<<")
        val inputs: Collection<TransformInput> = transformInvocation!!.inputs
        val outputProvider: TransformOutputProvider = transformInvocation.outputProvider

        // 标记是否已找到okhttp所属jar
        var hasFoundOkHttp = false

        inputs.forEach {
            it.jarInputs.forEach {
                var jarName = it.name
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length-4)
                }
                val md5Str = DigestUtils.md5Hex(it.file.absolutePath)
                val output = outputProvider.getContentLocation(
                        jarName + md5Str,
                        it.contentTypes,
                        it.scopes,
                        Format.JAR
                )

                // 标记当前jar是否是okhttp
                var skip = false

                if (!hasFoundOkHttp) {
                    // 判断是否是okHttp所在jar
                    hasFoundOkHttp = findTargetJar(it.file, REGEX_OKHTTPCLIENT)
                    if (hasFoundOkHttp) {
                        Printer.p("找到okHttp包")
                        // 执行注入代码
                        OkOneInjects.inject(it.file, output)
                        skip = true;
                    }
                }

                // 跳过对okhttp文件的拷贝
                if (!skip) {
                    // 拷贝input文件到output路径
                    FileUtils.copyFile(it.file, output)
                }
            }

            it.directoryInputs.forEach {
                // 获取output路径
                val output = outputProvider.getContentLocation(
                        it.name,
                        it.contentTypes,
                        it.scopes,
                        Format.DIRECTORY
                )
                // 拷贝input文件到output路径
                FileUtils.copyDirectory(it.file, output)
            }
        }
    }

    companion object {
        private const val REGEX_OKHTTPCLIENT = "okhttp3[/.]OkHttpClient.*"

        private fun findTargetJar(inputFile: File, regex: String) : Boolean {
            val pattern: Pattern = Pattern.compile(regex)
            val jarFile = JarFile(inputFile)
            val entries = jarFile.entries()
            var found = false
            // 遍历jar压缩包中所有节点
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val entryName = entry.name
                if (pattern.matcher(entryName).matches()) {
                    found = true;
                    break
                }
            }
            return found
        }
    }
}