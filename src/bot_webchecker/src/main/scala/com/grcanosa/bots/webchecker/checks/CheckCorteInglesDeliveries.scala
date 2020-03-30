package com.grcanosa.bots.webchecker.checks

import java.io.File
import java.net.URL
import java.nio.file.Paths

import com.bot4s.telegram.methods.{SendMessage, SendPhoto}
import com.bot4s.telegram.models.InputFile.Path
import com.grcanosa.bots.webchecker.checks.CheckCorteInglesDeliveries.CheckCorteResult
import com.grcanosa.telegrambot.bot.BotWithAdmin
import org.apache.commons.io.FileUtils
import org.openqa.selenium.remote.{BrowserType, DesiredCapabilities, RemoteWebDriver}
import org.openqa.selenium.{By, Keys, OutputType}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.Try

object CheckCorteInglesDeliveries{
  case class CheckCorteResult(sucess: Boolean,slots: Boolean, screenshot: String, reason: String)
}

trait CheckCorteInglesDeliveries {
  this: BotWithAdmin =>

  val chrome = new DesiredCapabilities()
  chrome.setBrowserName(BrowserType.CHROME)
  def chromeDriverUrl: String

  val dias = Seq("lunes","martes","miércoles","jueves","viernes","sábado","domingo","monday","tuesday","wednesday","thursday","friday","saturday","sunday")

  def stringContainsKeys(s: String,keys: Seq[String]): Boolean = {
    keys.map(k => s.contains(k)).reduce(_ || _)
  }

  def areSlotDeliveries(): CheckCorteResult = {
    Try {
      val driver: RemoteWebDriver = new RemoteWebDriver(new URL(chromeDriverUrl), chrome)
      driver.get("https://www.elcorteingles.es/supermercado")
      val cookieOk = driver.findElement(By.id("cookies-agree"))
      cookieOk.click()
      Thread.sleep(500)
      val cp = driver.findElement(By.xpath("/html/body/div[3]/div/div/div/div[2]/input"))
      cp.sendKeys("28007")
      Thread.sleep(100)
      cp.sendKeys(Keys.ENTER)
      botlog.info("Waiting after entering CP")
      Thread.sleep(1000)
      val horarioLink = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[2]/div/div[1]/div/div/div"))
      botlog.info("Find horario")
      botlog.info(horarioLink.getText)
      horarioLink.click()
      botlog.info("Waiting after horarios de entrega")
      Thread.sleep(1000)
      botlog.info("Getting screenshot")
      Thread.sleep(500)
      val b: File = driver.getScreenshotAs(OutputType.FILE)
      val im = "screen.jpeg"
      val d = new File(im)
      //Copy file at destination
      FileUtils.copyFile(b, d)
      botlog.info("Finish")
      val areSlots = driver.findElementsByClassName("flex_table-cell").asScala.map { el =>
        el.getText.toLowerCase match {
          case t if t contains "sin reparto" => (false,t)
          case t if t contains "sense repartiment" => (false,t)
          case t if t contains "no delivery" => (false,t)
          case t if stringContainsKeys(t, dias) => (false,t)
          case t if t contains "completo" => (false,t)
          case t => botlog.info(s"TRUE TEXT: $t");(true,t)
        }
      }.reduce{ (p1,p2) => (p1._1 || p2._1 , p1._2 + "\n" + p2._2.replace("\n","")) }
      CheckCorteResult(true,areSlots._1,im,areSlots._2)
    }.recover{
      case e => CheckCorteResult(false,false,"",e.getStackTrace.map(_.toString).mkString("\n ## \n"))
    }.get
  }

  def checkElCorteInglesSupermercadoNow() = {
    areSlotDeliveries() match {
      case CheckCorteResult(true,true,im,re) => {
        botActor ! SendMessage(adminId, s"$re")
        permittedUserHandlers.foreach { uH =>
          botActor ! SendMessage(uH.user.id, "Hay huecos de envio en el supermercado de ElCorteIngles!!!")
          botActor ! SendPhoto(uH.user.id, photo = Path(Paths.get(im)), caption = None)
        }

      }
      case CheckCorteResult(true,false,im,_) => {
        botActor ! SendMessage(adminId,"Checked and there are no deliveries")
        //botActor ! SendPhoto(adminId, photo = Path(Paths.get(im)), caption = None)
      }
      case CheckCorteResult(false,_,im,re) => {
        botActor ! SendMessage(adminId,s"Failure checking: $re")
      }
    }
  }

  system.scheduler.schedule(30 seconds, 15 minutes){
    checkElCorteInglesSupermercadoNow()
  }

}
