package test

import java.io.File
import java.net.URL

import org.apache.commons.io.FileUtils
import com.bot4s.telegram.api.AkkaDefaults
import com.grcanosa.telegrambot.utils.LazyBotLogging
import org.openqa.selenium.interactions.{Actions, Coordinates}
import org.openqa.selenium.{By, Keys, OutputType}
import org.openqa.selenium.remote.{BrowserType, DesiredCapabilities, RemoteWebDriver}
import scala.collection.JavaConverters._
object TestElcorteIngles extends App with AkkaDefaults with LazyBotLogging{
  implicit val ec = materializer.executionContext
//  //val driverUrl = "http://192.168.1.200:6666/wd/hub"
  val driver2Url = "http://localhost:4444/wd/hub"
  val chrome = new DesiredCapabilities()
  chrome.setBrowserName(BrowserType.CHROME)

  val driver: RemoteWebDriver = new RemoteWebDriver(new URL(driver2Url), chrome)

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

  val modal = driver.findElement(By.id("modal"))

//  modal.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT))
//  modal.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT))


// val zoom = "document.body.style.zoom='0.8'"
//  driver.executeScript(zoom)

  driver.findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.COMMAND,Keys.SUBTRACT))
  //driver.findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.LEFT_CONTROL,Keys.SUBTRACT))

//val act = new Actions(driver)
//
//  act.sendKeys( Keys.chord(Keys.CONTROL, Keys.SUBTRACT)).perform();
//  act.sendKeys( Keys.chord(Keys.CONTROL, Keys.SUBTRACT)).perform();
  Thread.sleep(500)
  val b: File = driver.getScreenshotAs(OutputType.FILE)
  val c = modal.getScreenshotAs(OutputType.FILE)



  val d = new File("screen.jpeg")
  val d2 = new File("screen2.jpeg")

  //Copy file at destination
  FileUtils.copyFile( b, d)
  FileUtils.copyFile( c, d2)

  botlog.info("Finish")

  driver.findElementsByClassName("flex_table-cell").asScala.foreach{ el =>
    println("ELEMENTO => ")
    println(el.getText)
  }

//  <div style="order:2" class="flex_table-cell  _slot   _CLOSED _vcenter _hcenter  _bordered  _bordered3  "><label for="_slot2020-03-29T17:00:00.000+020002301" class="text _xxs _darken _semi _60   _disabled   _disabled  ">09:00 - 11:00<span>COMPLETO</span></label><div></div></div>

//  val but = driver.findElement(By.xpath("/html/body/div[3]/div/div/div/div[2]/buttonemmina.mo"))
//  Thread.sleep(100)
//  but.click()

 Thread.sleep(100000)

}
