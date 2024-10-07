package models

fun countNoOfTimes(s:String="abcdabcdaa") {
    val list: ArrayList<String> = arrayListOf()
    val list2 = arrayListOf<Int>()
    for (i in 0 until s.length) {
        if (list.contains("${s[i]}")) {
            val index = list.indexOf("${s[i]}")
            list2[index] = list2[index] + 1
        } else {
            list.add("${s[i]}")
            list2.add(0)
        }
    }
    list.forEachIndexed { index, s ->
        println("$s : ${list2[index]}")
    }
}
fun main(){
    countNoOfTimes()
}