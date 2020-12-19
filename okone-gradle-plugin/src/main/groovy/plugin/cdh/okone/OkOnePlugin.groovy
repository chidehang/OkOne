package plugin.cdh.okone

import org.gradle.api.Plugin
import org.gradle.api.Project

class OkOnePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println(">>> apply OkOnePlugin <<<")
        project.android.registerTransform(new OkOneTransform())
    }
}