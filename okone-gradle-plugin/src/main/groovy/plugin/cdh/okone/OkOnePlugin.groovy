package plugin.cdh.okone

import org.gradle.api.Plugin
import org.gradle.api.Project
import plugin.cdh.okone.util.Printer

class OkOnePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        Printer.p(">>> apply OkOnePlugin <<<")
        project.android.registerTransform(new OkOneTransform())
    }
}