package com.grcanosa.bots.renfebot.renfe



import java.net.URL

import com.grcanosa.bots.renfebot.model.Trip
import org.openqa.selenium.{By, Keys, WebElement}
import org.openqa.selenium.remote.{BrowserType, DesiredCapabilities, RemoteWebDriver}
import org.openqa.selenium.support.ui.WebDriverWait

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

class RenfeChecker(val driverUrl: String) {

  val chrome = new DesiredCapabilities()
  chrome.setBrowserName(BrowserType.CHROME)

  implicit val driver = new RemoteWebDriver(new URL("http://localhost:6666/wd/hub"), chrome)

  def checkTrip(trip: Trip)
               (implicit driver: RemoteWebDriver) = {
    val origin = trip.origin
    val destination = trip.destination
    val date = trip.departureDate

    fillElements(origin, destination, date)
    val go_bt = driver.findElementByClassName("btn_home")
    go_bt.click()
    waitForElementById("tab-mensaje_contenido", 30 seconds) match {
      case false => {
        println("cannot get")
        Seq.empty[TripResult]
      }
    }
  }

  def waitForElementById(id: String,timeout: FiniteDuration)
                        (implicit driver: RemoteWebDriver): Boolean = {
    Try{
      val wait = new WebDriverWait(driver,timeout.toSeconds)
      val b: WebElement = wait.until(driver => driver.findElement(By.id(id)))
      true
    }.getOrElse(false)
  }

  def fillElement(element_id: String, data: String)(implicit driver: RemoteWebDriver) = {
    val el = driver.findElementById(element_id)
    el.clear()
    el.sendKeys(data)
    Thread.sleep(1000)
    el.sendKeys(Keys.ENTER)
  }

  def fillElements(origen: String, destino: String,fecha: String)(implicit driver:RemoteWebDriver) = {
    fillElement("IdDestino",destino)
    fillElement("IdOrigen",origen)
    fillElement("__fechaIdaVisual",fecha)
  }

}
//
//val chrome = new DesiredCapabilities()
//chrome.setBrowserName(BrowserType.CHROME)
////implicit val driver = new RemoteWebDriver(new URL("http://192.168.1.200:6666/wd/hub"), chrome)
//implicit val driver = new RemoteWebDriver(new URL("http://localhost:6666/wd/hub"), chrome)
//
//
//driver.get("https://www.renfe.com")
//Thread.sleep(1000)
//println(driver.getTitle)
//
//
//def fillElement(element_id: String, data: String)(implicit driver: RemoteWebDriver) = {
//  val el = driver.findElementById(element_id)
//  el.clear()
//  el.sendKeys(data)
//  Thread.sleep(1000)
//  el.sendKeys(Keys.ENTER)
//}
//
//  def fillElements(origen: String, destino: String,fecha: String)(implicit driver:RemoteWebDriver) = {
//  fillElement("IdDestino",destino)
//  fillElement("IdOrigen",origen)
//  fillElement("__fechaIdaVisual",fecha)
//}
//
//  def waitForElementById(id: String,timeout: FiniteDuration)(implicit driver: RemoteWebDriver) = {
//  Try{
//  val wait = new WebDriverWait(driver,timeout.toSeconds)
//  val b: WebElement = wait.until(driver => driver.findElement(By.id(id)))
//  true
//}.getOrElse(false)
//}
//
//  case class TripResult()
//
//  def getTrainsIfPossible()(implicit driver: RemoteWebDriver) = {
//  val el = driver.findElementById("tab-mensaje_contenido")
//  el.getText contains "no se encuentra disponible" match {
//  case true => {
//  println("No trains")
//  Seq.empty[TripResult]
//}
//  case false => getTrains()
//}
//}
//
//  def getTrains()(implicit driver: RemoteWebDriver) = {
//  //Thread.sleep(30000)
//  val wait = new WebDriverWait(driver,30)
//  wait.until(dr => dr.findElements(By.id("listaTrenesTBodyIda")))
//  val trenes = driver.findElementById("listaTrenesTBodyIda")
//  val rows: util.List[WebElement] =  trenes.findElements(By.xpath(".//tr[contains(@class,'trayectoRow')]"))
//  println(s"size is ${rows.size()}")
//  rows.asScala.map{ el:WebElement =>
//  val salida = el.findElement(By.xpath(".//td[@headers='colSalida']")).getText
//  val llegada = el.findElement(By.xpath(".//td[@headers='colLlegada']")).getText
//  println(salida, llegada)
//  TripResult
//}
//}
//
//  //  def getTrains(driver):
//  //  trayectos = []
//  //  trenes = driver.find_element_by_id("listaTrenesTBodyIda")
//  //  rows = trenes.find_elements_by_xpath(".//tr[contains(@class,'trayectoRow')]")
//  //  logger.info("SIZE: "+str(len(rows)))
//  //  # rows = rows + trenes.find_elements_by_xpath(".//tr[@class='trayectoRow row_alt']")
//  //  # rows = rows + trenes.find_elements_by_xpath(".//tr[@class='trayectoRow last']")
//  //  for r in rows:
//  //    sal = r.find_element_by_xpath(".//td[@headers='colSalida']").text
//  //  lle = r.find_element_by_xpath(".//td[@headers='colLlegada']").text
//  //  salT = datetime.datetime.strptime(sal, '%H.%M').time()
//  //  lleT = datetime.datetime.strptime(lle, '%H.%M').time()
//  //  toSec = lambda x: x.hour*60*60+x.minute*60+x.second
//  //  dur = toSec(lleT)-toSec(salT)
//  //  tipo = r.find_element_by_xpath(".//td[@headers='colTren']").text
//  //  disp = not "Completo" in r.text and not "disponible" in r.text
//  //  precio=""
//  //  clase=""
//  //  tarifa=""
//  //  if disp:
//  //    precio = r.find_element_by_xpath(".//td[@headers='colPrecio']").text
//  //  precio = float(precio.split()[0].replace(",","."))
//  //  clase = r.find_element_by_xpath(".//td[@headers='colClase']//span").get_attribute("innerHTML")
//  //  tarifa = r.find_element_by_xpath(".//td[@headers='colTarifa']//span").get_attribute("innerHTML")
//  //  trayectos.append({"SALIDA":salT,"LLEGADA":lleT,"TIPO":tipo,"PRECIO":precio,"DURACION":float(dur)/3600,"CLASE":clase,"TARIFA":tarifa,"DISPONIBLE":disp})
//  //  #logger.info(trayectos)
//  //  logger.debug("Returning arrary")
//  //  return trayectos
//
//
//
//  def checkTrip(origen: String, destino: String, fecha: String)(implicit driver: RemoteWebDriver) = {
//  fillElements(origen,destino,fecha)
//  val go_bt = driver.findElementByClassName("btn_home")
//  go_bt.click()
//  waitForElementById("tab-mensaje_contenido", 30 seconds) match {
//  case false => {
//  println("cannot get")
//  Seq.empty[TripResult]
//}
//  case true => getTrainsIfPossible()
//}
//
//}
//
//
//  checkTrip("MADRID-PUERTA DE ATOCHA","SEVILLA-SANTA JUSTA","16/03/2020")
//
//  //"http://localhost:9515", DesiredCapabilities.chrome)
//
//  //  driver = webdriver.Remote(command_executor="",desired_capabilities=DesiredCapabilities.CHROME)
//  //  driver.set_page_load_timeout(60)
//
//}
