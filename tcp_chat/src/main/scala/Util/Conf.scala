package Util

import com.typesafe.config.ConfigFactory

object Conf {
  val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val appHostName = config.getString("tcp_chat.app.hostname")
  val appPort = config.getInt("tcp_chat.app.port")
}
