package com.musik.index.app

import java.util.Properties

import com.google.common.base.Preconditions
import com.musik.config.{ConfigFactory, Configs}
import org.apache.log4j.Logger

trait App {
  private[app] val logger: Logger = Logger.getLogger(this.getClass)

  val config: Properties = ConfigFactory.build().set(Configs.INDEX).load()

  /**
    * Returns Spark app master according to app.master parameter of configuration file
    *
    * @return Returns name of app master
    */
  def getMaster: String = {
    val master = config.getProperty("app.master")

    Preconditions.checkNotNull(master, "Config parameter 'app.master' is empty", null)

    master
  }
}