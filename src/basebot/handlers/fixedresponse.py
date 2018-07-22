#!/usr/bin/python3

import logging
import random
import os

from telegram.ext import CommandHandler
from telegram import Bot,Update
from telegram.ext import Updater

from basebot.userdb import userdb

logger = logging.getLogger(__name__)

class FixedResponse:
    def __init__(self,cmd = "",response = "",updater=None,
                    dbfile = None,phrasetype = "message",priority = 50):
        self._cmd = cmd
        self._response = response
        self._type = phrasetype
        self._dbfile = dbfile
        userdb.initdb(self._dbfile)
        self._up = updater
        self._priority = priority
        logger.debug("Creating fixed reponse for cmd: "+self._cmd)
        self.install_handler()


    def install_handler(self):
        if self._cmd is not "":
            self._up.dispatcher.add_handler(CommandHandler(self._cmd,self.process),self._priority)

    def process(self,bot,update):
        if self._dbfile is not None:
            full_username = userdb.get_username(update.message.from_user.first_name
                            , update.message.from_user.last_name
                            , update.message.from_user.username)
            userdb.add_user( update.message.from_user.id, full_username)
            userdb.add_cmd( update.message.from_user.id, self._cmd)
        if self._type == "message":
            bot.send_message(chat_id=update.message.chat_id,text=self._response)
        elif self._type == "gif":
            bot.send_document(chat_id=update.message.chat_id,document=open(self._response,'rb'))
        elif self._type == "voice":
            bot.sendVoice(chat_id=update.message.chat_id,voice=self._response)
        elif self._type == "audio":
            bot.send_audio(chat_id=update.message.chat_id,audio=self._response)
