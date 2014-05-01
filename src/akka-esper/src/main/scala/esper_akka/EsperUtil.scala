package esper_akka

import common.C

/**
 * Created by whwndussla on 14. 5. 1..
 */
object EsperUtil {
    def Filtering(str:String):Boolean = {

      var str1 = str.replaceAll("\\p{Space}", "")

      for(s1 <- C.filterStr if str1.contains(s1))
      {
        return true
      }

      return false
    }
}
