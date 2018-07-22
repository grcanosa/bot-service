#!/usr/bin/python3

from telegram import Bot;

from telegram.ext import CommandHandler;

import logging

logger = logging.getLogger(__name__);


class UserStats:
    def __init__(self,cmd="",updater=None,userR=None,priority=50):
        self._updater=updater;
        self._userR = userR;
        self._cmd = cmd;
        self._priority = priority;
        logger.debug("Creating user stats response for cmd: %s",self._cmd);
        self.install_handler();

    def install_handler(self):
        if not self._cmd == "":
            ch = CommandHandler(self._cmd,self.process);
            self._updater.dispatcher.add_handler(ch,self._priority);

    def process(self,bot,update):
        logger.debug("Returning user stats for user %d",update.message.from_user.id);
        self._userR.inc_cmd(update.message.from_user.id,self._cmd);
        text = "";
        u = self._userR.get_user(update.message.from_user.id);
        text += "Eres "+u.get_name()+ " con número de usuario telegram: "+str(u.get_id())+"\n";
        text += "Esto es lo que me has pedido desde que te conozco: \n";
        for cmd,num in u.get_cmds().items():
            text += '{0:<5}'.format(num)+" - "+cmd+"\n";
        bot.send_message(chat_id=update.message.chat_id,text=text);
