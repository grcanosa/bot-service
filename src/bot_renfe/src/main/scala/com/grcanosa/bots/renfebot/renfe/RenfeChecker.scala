package com.grcanosa.bots.renfebot.renfe



import java.net.URL
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneOffset}

import scala.concurrent.duration._
import com.grcanosa.bots.renfebot.model.{Journey, Trip}
import com.grcanosa.bots.renfebot.renfe.RenfeChecker.CheckJourneyResponse
import org.openqa.selenium.{By, Keys, WebElement}
import org.openqa.selenium.remote.{BrowserType, DesiredCapabilities, RemoteWebDriver}
import org.openqa.selenium.support.ui.WebDriverWait

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

object RenfeChecker{

  case class CheckJourneyResponse(trips: Seq[Trip],resp: String)

}

class RenfeChecker(val driverUrl: String)(implicit ec: ExecutionContext) {
  val hourFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm")
  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  val chrome = new DesiredCapabilities()
  chrome.setBrowserName(BrowserType.CHROME)





  def checkJourney(trip: Journey): CheckJourneyResponse = {
      Try {
        implicit val driver: RemoteWebDriver = new RemoteWebDriver(new URL(driverUrl), chrome)
        val origin = trip.origin
        val destination = trip.destination
        val date = trip.departureDate
        implicit val localDate: LocalDate = LocalDate.parse(date, dateFormat)
        driver.get("https://www.renfe.com")
        fillElements(origin, destination, date)
        val go_bt = driver.findElementByClassName("btn_home")
        go_bt.click()
        val trips = waitForElementById("tab-mensaje_contenido", 30 seconds) match {
          case false => Seq.empty[Trip]
          case true => getTrainsIfPossible().sortBy(_.departureTimestamp)
        }
        CheckJourneyResponse(trips,"")
      }.recover{
        case exp => CheckJourneyResponse(Seq.empty[Trip],"")
      }.get
  }

  def waitForElementById(id: String,timeout: FiniteDuration)(implicit driver: RemoteWebDriver): Boolean = {
    Try{
      val wait = new WebDriverWait(driver,timeout.toSeconds)
      val b: WebElement = wait.until(driver => driver.findElement(By.id(id)))
      true
    }.getOrElse(false)
  }

  private def fillElement(element_id: String, data: String)(implicit driver: RemoteWebDriver) = {
    val el = driver.findElementById(element_id)
    el.clear()
    el.sendKeys(data)
    Thread.sleep(1000)
    el.sendKeys(Keys.ENTER)
  }

  private def fillElements(origen: String, destino: String,fecha: String)(implicit driver: RemoteWebDriver) = {
    fillElement("IdDestino",destino)
    fillElement("IdOrigen",origen)
    fillElement("__fechaIdaVisual",fecha)
  }

  private def getTrainsIfPossible()(implicit date: LocalDate,driver: RemoteWebDriver): Seq[Trip] = {
    val el = driver.findElementById("tab-mensaje_contenido")
    el.getText contains "no se encuentra disponible" match {
      case true => Seq.empty[Trip]
      case false => getTrips()
    }
  }



  private def getTrips()(implicit date: LocalDate,driver: RemoteWebDriver): Seq[Trip] = {
    //Thread.sleep(30000)
    val wait = new WebDriverWait(driver,30)
    wait.until(dr => dr.findElements(By.xpath(".//tr[contains(@class,'trayectoRow')]")))
    val trenes = driver.findElementById("listaTrenesTBodyIda")
    val rows =  trenes.findElements(By.xpath(".//tr[contains(@class,'trayectoRow')]"))
    rows.asScala.map{ el:WebElement =>
      val salida: String = el.findElement(By.xpath(".//td[@headers='colSalida']")).getText
      val llegada: String = el.findElement(By.xpath(".//td[@headers='colLlegada']")).getText
      val tipo = Try{el.findElement(By.xpath(".//td[@headers='colTren']")).getText}.toOption
      val clase = Try{el.findElement(By.xpath(".//td[@headers='colClase']//span")).getAttribute("innerHTML")}.toOption
      val tarifa = Try{el.findElement(By.xpath(".//td[@headers='colTarifa']//span")).getAttribute("innerHTML")}.toOption

      val timeSalida = LocalTime.parse(salida,hourFormat)
      val timeLlegada = LocalTime.parse(llegada,hourFormat)
      val dateTimeSalida = LocalDateTime.of(date,timeSalida)
      val dateTimeLlegada = LocalDateTime.of(date,timeLlegada)
      val duration: Long = dateTimeSalida.until(dateTimeLlegada,ChronoUnit.MINUTES) match {
        case d if d >= 0 => d
        case d if d < 0 => (24*60) + d
      }
      val disp = (! el.getText.contains("Completo")) && (! el.getText.contains("disponible"))
      val precio: Option[String] = disp match {
        case true => Some(el.findElement(By.xpath(".//td[@headers='colPrecio']")).getText)
        case false => None
        }
      Trip(
        dateTimeSalida.toEpochSecond(ZoneOffset.UTC)
        , duration
        , salida
        , llegada
        , disp
        , precio.map(_.split(' ').head.toFloat)
        , tipo
        , clase
        , tarifa
      )
    }
  }

}
