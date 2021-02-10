package plugin.cdh.okone

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import plugin.cdh.okone.util.Printer

/**
 * Created by chidehang on 2021/2/8
 */
class OkOnePlugin: Plugin<Project> {

    override fun apply(project: Project) {
        Printer.p(">>> apply OkOnePlugin <<<")
        val android = project.extensions.getByType(AppExtension::class.java)
        android.registerTransform(OkOneTransform())
    }
}