#!/usr/local/bin/python3

from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from selenium.common.exceptions import NoSuchElementException

if __name__ != "__main__":
    from pyvirtualdisplay import Display

import time
import datetime

import optparse
import sys
import logging

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    level=logging.DEBUG)

logger = logging.getLogger(__name__)




if __name__ != "__main__":
    display = Display(visible=0, size=(800, 600))
    display.start()


def get_new_driver():
    driver = None
    try:
        profile = webdriver.FirefoxProfile()
        profile.native_events_enabled = False
        driver = webdriver.Firefox(profile)
        driver.set_page_load_timeout(60)
    except:
        logger.error("Cannot create driver")
    return driver


def fill_element(driver, element_id, data):
    if data is not None:
        el = driver.find_element_by_id(element_id)
        el.clear()
        el.send_keys(data)
        time.sleep(1)
        el.send_keys(Keys.ENTER)


def fill_elements(driver, origen, destino, dat_go, dat_ret):
    fill_element(driver,"IdDestino",destino)
    fill_element(driver,"IdOrigen",origen)
    fill_element(driver,"__fechaIdaVisual",dat_go)
    fill_element(driver,"__fechaVueltaVisual",dat_ret)


def are_if_trains_available(driver):
    try:
        nodata = driver.find_element_by_id("tab-mensaje_contenido")
        if "no se encuentra disponible" in nodata.text:
            return False
        else:
            return True
    except selenium.common.exceptions.NoSuchElementException:
        logger.error("Element not found")
        return False


def getTrains(driver):
    trayectos = []
    trenes = driver.find_element_by_id("listaTrenesTBodyIda")
    rows = trenes.find_elements_by_xpath(".//tr[@class='trayectoRow']")
    rows = rows + trenes.find_elements_by_xpath(".//tr[@class='trayectoRow row_alt']")
    rows = rows + trenes.find_elements_by_xpath(".//tr[@class='trayectoRow last']")
    for r in rows:
        sal = r.find_element_by_xpath(".//td[@headers='colSalida']").text
        lle = r.find_element_by_xpath(".//td[@headers='colLlegada']").text
        salT = datetime.datetime.strptime(sal, '%H.%M').time()
        lleT = datetime.datetime.strptime(lle, '%H.%M').time()
        toSec = lambda x: x.hour*60*60+x.minute*60+x.second
        dur = toSec(lleT)-toSec(salT)
        tipo = r.find_element_by_xpath(".//td[@headers='colTren']").text
        disp = not "Completo" in r.text and not "disponible" in r.text
        precio=""
        clase=""
        tarifa=""
        if disp:
            precio = r.find_element_by_xpath(".//td[@headers='colPrecio']").text
            precio = float(precio.split()[0].replace(",","."))
            clase = r.find_element_by_xpath(".//td[@headers='colClase']").text
            tarifa = r.find_element_by_xpath(".//td[@headers='colTarifa']").text
        trayectos.append({"SALIDA":salT,"LLEGADA":lleT,"TIPO":tipo,"PRECIO":precio,"DURACION":float(dur)/3600,"CLASE":clase,"TARIFA":tarifa,"DISPONIBLE":disp})
    logger.debug("Returning arrary")
    return trayectos


def with_new_driver(fun):
    def wrapper(*arg, ** kw):
        driver = get_new_driver()
        if driver is None:
            return False, None
        else:
            ret = False,None
            try:
                ret = fun(driver, *arg, **kw)
            except Exception as e:
                logger.error("Exception: "+str(e))
            input("Press the <ENTER> key to continue...")
            driver.close()
            return ret
    return wrapper



@with_new_driver
def check_trip(driver,origin, destination, dat_go, dat_ret= None):
    driver.get("http://www.renfe.com")
    time.sleep(1)
    fill_elements(driver,origin, destination, dat_go, dat_ret )
    go_bt = driver.find_element_by_class_name("btn_home")
    go_bt.click()
    if are_if_trains_available(driver):
        return True, getTrains(driver)
    else:
        logger.info("NO TRAINS AVAILABLE")
        return False, None





def printRes(aux,ori,des,fec):
    print("Results for=> origin: "+ori+", dest: "+des+", date: "+fec)
    if aux[0]:
        outstr = ""
        outstr += "{tipo:<10s}"
        outstr += "{salida:<5s} - {llegada:<5s} "
        outstr += "({duracion:<1.1f}h): "
        outstr += "{clase:<20s} - {tarifa:<10s} - {disponible:<2s}"
        for t in aux[1]:
            line = outstr.format(salida = t["SALIDA"].strftime("%H:%M")
                        ,llegada=t["LLEGADA"].strftime("%H:%M")
                        ,tipo=t["TIPO"]
                        ,precio = t["PRECIO"]
                        ,duracion = t["DURACION"]
                        ,clase=t["CLASE"]
                        ,tarifa = t["TARIFA"]
                        ,disponible = "SI" if t["DISPONIBLE"] else "NO"
                        )
            print(line)
    else:
        print("NO RESULTS")

def main(ori,des,fec):
    aux = check_trip(ori,des,fec)
    printRes(aux, ori, des, fec)
    # aux = check_trip(des,ori,fec)
    # printRes(aux, des, ori, fec)
    


def parse_arguments(argv):
    parser = optparse.OptionParser()
    parser.add_option("--origen","-o",help="Origen",default = None,dest ="origen")
    parser.add_option("--destino","-d",help="Destino",default = None,dest ="destino")
    parser.add_option("--fecha","-f",help="Fecha viaje",default = None,dest="fecha")
    options,args = parser.parse_args(argv)
    if options.origen is None or options.destino is None or options.fecha is None:
        print("Bad parameters")
        parser.print_help()
        exit(1)
    return options


if __name__ == "__main__":
    op = parse_arguments(sys.argv)
    main(op.origen,op.destino,op.fecha)