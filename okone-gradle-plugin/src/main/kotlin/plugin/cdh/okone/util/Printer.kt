package plugin.cdh.okone.util

/**
 * Created by chidehang on 2021/2/8
 */
class Printer {
    companion object {
        private const val GLOBAL_TAG = "[OkOne]"

        fun p(content: String) {
            println("$GLOBAL_TAG $content")
        }
    }
}