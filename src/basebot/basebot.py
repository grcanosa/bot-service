#!/usr/bin/python3

#TELEGRAM IMPORTS
from telegram.ext import Updater




class BaseBot:
    def __init__(self,token):
        self._token = token
        self._updater = Updater(self._token)
        self._disp = self._updater.dispatcher
        self._bot = self._updater.bot

    def start(self):
        self._updater.start_polling()

    def stop(self):
        self._updater.stop()

    def idle(self):
        self._updater.idle()
