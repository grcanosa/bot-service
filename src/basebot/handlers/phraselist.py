#!/usr/bin/python3

import logging
import random
import os
from basebot.userdb import userdb
from telegram.ext import CommandHandler

logger = logging.getLogger(__name__)

class PhraseList:
    def __init__(self,cmdget = "",cmdadd="",filename ="", \
        updater=None,dbfile = None,phrasetype = "message", \
        priority = 50):
        self._cmdadd = cmdadd
        self._cmdget = cmdget
        self._list = []
        self._filename = filename
        self._type = phrasetype
        self._dbfile = dbfile
        userdb.initdb(self._dbfile)
        self._up = updater
        self._priority = priority
        logger.debug("Creating phrase list for get: "+self._cmdget+" and add: "+self._cmdadd)
        self.load_data()
        self.install_handler()


    def load_data(self):
        logger.debug("Trying to load data of type %s",self._type)
        if self._type == "message":
            logger.debug("Trying to read from file %s",self._filename)
            if os.path.isfile(self._filename):
                with open(self._filename,'r') as f:
                    for l in f:
                        self._list.append(l)
            else:
                logger.error("File %s not found",self._filename)
            logger.debug("Added %d elements to list",len(self._list))
        elif self._type == "gif":
            if os.path.isdir(self._filename):
                logger.debug("Looking in dir %s",self._filename)
                for file in os.listdir(self._filename):
                    if file.endswith(".mp4"):
                        self._list.append(self._filename+"/"+file)
                logger.debug("Added %d elements to list",len(self._list))
            else:
                logger.error("Path %s not found",self._filename)
        else:
            logger.warning("TYPE %s not defined, not loading data",self._type)

    def install_handler(self):
        if self._cmdadd is not "":
            self._up.dispatcher.add_handler(CommandHandler(self._cmdadd,self.proccess_add),self._priority)
        if self._cmdget is not "":
            self._up.dispatcher.add_handler(CommandHandler(self._cmdget,self.proccess_get),self._priority)

    def get_random_phrase(self):
        return random.choice(self._list)

    def add_phrase(self,phrase):
        self._list.append(phrase)
        with open(self._filename,'a') as f:
            f.write(phrase+"\n")

    def proccess_get(self,bot,update):
        if self._dbfile is not None:
            full_username = userdb.get_username(update.message.from_user.first_name
                            , update.message.from_user.last_name
                            , update.message.from_user.username)
            userdb.add_user( update.message.from_user.id, full_username)
            userdb.add_cmd( update.message.from_user.id, self._cmdget)
        #msgspli = update.message.text.split()
        logger.debug("Giving something to own user")
        if userdb.get_cmd_num(update.message.from_user.id,self._cmdget) % 3 != 0:
            self.send_random(bot,update.message.chat_id)
        else:
            text, phrasetype = self.get_max_cmd_response(update)
            if phrasetype == "message":
                bot.send_message(chat_id=update.message.chat_id,text=text)
        
    def get_max_cmd_response(self, update):
        return "", "message"

    def proccess_add(self,bot,update):
        #self._userR.inc_cmd(update.message.from_user.id,self._cmdadd)
        msgspli = update.message.text.split()
        msgspli.pop(0)
        msgspli = ' '.join(msgspli)
        if self._type == "message":
            self.add_phrase(msgspli)
            bot.send_message(chat_id=update.message.chat_id,text="Your suggestion has beed added!!")


    def send_random(self,bot,user_id):
        if self._type == "message":
            phr = self.get_random_phrase()
            bot.send_message(chat_id=user_id,text=phr)
            return phr
        elif self._type == "gif":
            file = self.get_random_phrase()
            logger.debug("Sending file %s",file)
            bot.send_document(chat_id=user_id,document=open(file,'rb'))
        return None

