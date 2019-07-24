#!/usr/bin/python3

import calendar
import datetime

origen = "MADRID-PUERTA DE ATOCHA"
destino = "SEVILLA-SANTA JUSTA"


def get_weekends_dates():
    year = datetime.datetime.now().year
    month = datetime.datetime.now().month
    day = datetime.datetime.now().day
    
    c = calendar.Calendar(firstweekday=calendar.MONDAY)
    inc_year = 0
    for m in range(month,month+5):
        if m > 12:
            m = m - 12
            inc_year = 1
        monthcal = c.monthdatescalendar(year + inc_year, m)
        for w in monthcal:
            for d in week:







def main():
    print("Hello")


if __name__ == "__main__":
    main()