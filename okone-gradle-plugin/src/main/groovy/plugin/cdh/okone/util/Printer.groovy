package plugin.cdh.okone.util

class Printer {

    private final static String GLOBAL_TAG = "[OkOne]"

    static void p(String content) {
        println("${GLOBAL_TAG} ${content}")
    }
}