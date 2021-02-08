package plugin.cdh.okone

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import plugin.cdh.okone.util.Printer

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Pattern

class OkOneTransform extends Transform {

    private static final String REGEX_OKHTTPCLIENT = "okhttp3[/.]OkHttpClient.*"

    @Override
    String getName() {
        return "OkOneTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Printer.p(">>> transform OkOneTransform <<<")
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()

        // 标记是否已找到okhttp所属jar
        boolean hasFoundOkHttp = false;

        inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
                String jarName = jarInput.name
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length()-4)
                }
                String md5Str = DigestUtils.md5Hex(jarInput.file.absolutePath)
                // 获取output路径，jar名不能重复，拼接md5
                def output = outputProvider.getContentLocation(
                        jarName + md5Str,
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR
                )

                // 标记当前jar是否是okhttp
                boolean skip = false

                // 处理okhttp jar包
                if (!hasFoundOkHttp) {
                    // 判断是否是okHttp所在jar
                    hasFoundOkHttp = findTargetJar(jarInput.file, REGEX_OKHTTPCLIENT)
                    if (hasFoundOkHttp) {
                        Printer.p("找到okHttp包")
                        // 执行注入代码
                        OkOneInjects.inject(jarInput.file, output)
                        skip = true;
                    }
                }

                // 跳过对okhttp文件的拷贝，稍后对okhttp jar进行代码注入
                if (!skip) {
                    // 拷贝input文件到output路径
                    FileUtils.copyFile(jarInput.file, output)
                }
            }

            input.directoryInputs.each { DirectoryInput directoryInput ->
                // 获取output路径
                def output = outputProvider.getContentLocation(
                        directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes,
                        Format.DIRECTORY
                )
                // 拷贝input文件到output路径
                FileUtils.copyDirectory(directoryInput.file, output)
            }
        }
    }

    /**
     * 查找匹配文件名的jar
     */
    private boolean findTargetJar(File inputFile, String regex) {
        Pattern pattern = Pattern.compile(regex)
        JarFile jarFile = new JarFile(inputFile)
        def entries = jarFile.entries()
        boolean found = false
        // 遍历jar压缩包中所有节点
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement()
            String entryName = entry.name
            if (pattern.matcher(entryName).matches()) {
                found = true;
                break
            }
        }
        return found
    }
}