package plugin.cdh.okone

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import plugin.cdh.okone.util.Printer
import java.io.File
import java.io.IOException
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
        return true
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun transform(transformInvocation: TransformInvocation) {
        Printer.p(">>> transform OkOneTransform <<<")
        Printer.p("support incremental = ${transformInvocation.isIncremental}")

        if (!transformInvocation.isIncremental) {
            transformInvocation.outputProvider.deleteAll()
        }

        val inputs: Collection<TransformInput> = transformInvocation.inputs
        val outputProvider: TransformOutputProvider = transformInvocation.outputProvider

        // 标记是否已找到okhttp所属jar
        var hasFoundOkHttp = false

        inputs.forEach {
            it.jarInputs.forEach { input: JarInput ->
                var jarName = input.name
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length-4)
                }
                val md5Str = DigestUtils.md5Hex(input.file.absolutePath)
                val output = outputProvider.getContentLocation(
                        jarName + md5Str,
                        input.contentTypes,
                        input.scopes,
                        Format.JAR
                )

                if (transformInvocation.isIncremental) {
                    when (input.status) {
                        Status.ADDED, Status.CHANGED -> {
                            hasFoundOkHttp = transformJar(input.file, output, hasFoundOkHttp)
                        }
                        Status.REMOVED -> {
                            if (output.exists()) {
                                output.delete()
                            }
                        }
                        Status.NOTCHANGED -> {
                            // do nothing
                        }
                    }
                } else {
                    hasFoundOkHttp = transformJar(input.file, output, hasFoundOkHttp)
                }
            }

            it.directoryInputs.forEach { input ->
                // 获取output路径
                val outputDir = outputProvider.getContentLocation(
                        input.name,
                        input.contentTypes,
                        input.scopes,
                        Format.DIRECTORY
                )

                if (transformInvocation.isIncremental) {
                    val srcDir = input.file.absolutePath
                    val destDir = outputDir.absolutePath

                    input.changedFiles.forEach { entry: Map.Entry<File, Status> ->
                        val srcFile = entry.key
                        val destFile = File(srcFile.absolutePath.replace(srcDir, destDir))

                        when (entry.value) {
                            Status.ADDED, Status.CHANGED -> {
                                try {
                                    FileUtils.touch(destFile)
                                } catch (e: IOException) {
                                    FileUtils.forceMkdirParent(destFile)
                                }
                                FileUtils.copyFile(srcFile, destFile)
                            }
                            Status.REMOVED -> {
                                if (destFile.exists()) {
                                    destFile.delete()
                                }
                            }
                            Status.NOTCHANGED -> {
                                // do nothing
                            }
                        }
                    }
                } else {
                    // 拷贝input文件到output路径
                    FileUtils.copyDirectory(input.file, outputDir)
                }
            }
        }
    }

    private fun transformJar(input: File, output: File, hasFoundOkHttp: Boolean) : Boolean {
        // 标记当前jar是否是OkHttp
        var isOkHttp = false

        if (!hasFoundOkHttp) {
            // 判断是否是okHttp所在jar
            if (findTargetJar(input, REGEX_OKHTTPCLIENT)) {
                Printer.p("找到okHttp包")
                // 执行注入代码
                OkOneInjects.inject(input, output)
                isOkHttp = true;
            }
        }

        // 跳过对OkHttp文件的拷贝
        if (!isOkHttp) {
            // 拷贝input文件到output路径
            FileUtils.copyFile(input, output)
        }

        return isOkHttp;
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