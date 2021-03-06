package com.tribbloids.spookystuff.session

import org.openqa.selenium.{NoSuchSessionException, WebDriver}
import org.slf4j.LoggerFactory

/**
  *
  */
object CleanMixin {

}

trait CleanMixin {

  def clean(): Unit

  //  TODO: Runtime.getRuntime.addShutdownHook()
  override def finalize(): Unit = {
    try {
      clean()
    }
    catch {
      case e: NoSuchSessionException => //already cleaned before
      case e: Throwable =>
        LoggerFactory.getLogger(this.getClass).warn(s"!!!!! FAIL TO CLEAN UP ${this.getClass.getSimpleName} !!!!!"+e)
    }
    finally {
      super.finalize()
    }
  }
}


trait CleanWebDriverMixin extends CleanMixin {
  this: WebDriver =>

  def clean(): Unit = {
    this.close()
    this.quit()
  }
}