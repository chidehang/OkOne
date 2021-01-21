package plugin.cdh.okone.inject

import javassist.ClassPool

interface IClassInjector {

    boolean handles(String name)

    File inject(File workDir, ClassPool pool)
}