#!/usr/bin/python3

import telegram
from telegram import Update
from telegram.ext import Job
from telegram.ext import Updater
from telegram.ext import MessageHandler,Filters
from telegram.ext import CommandHandler
import random
import json
import shutil
import logging
import os

from .user import User

def_max_num_cmd = 3
logger = logging.getLogger(__name__)


class UserRegistry:


    def __init__(self,dest_file = "users.reg",admin_cid=0):
        self._users = []
        #self._usersj = []
        self._dest_file = dest_file
        self._admin_cid = admin_cid
        self.load_users_from_reg()
        logger.debug("Initiating registry with size %d",self._users.__len__())

    def load_users_from_reg(self):
        json_obj = {}
        if os.path.isfile(self._dest_file):
            logger.debug("Loading from file %s",self._dest_file)
            with open(self._dest_file,'r') as f:
                json_obj = json.load(f)
        if "USER_LIST" in json_obj:
            for usj in json_obj["USER_LIST"]:
                u = User()
                u.fill_from_json(usj)
                self._users.append(u)

    def install(self,updater):
        self.install_handler(updater.dispatcher)
        self.install_periodic_job(updater.job_queue)

    def install_handler(self,dispatcher):
        dispatcher.add_handler(MessageHandler(Filters.all, self.proc_msg),0)
        dispatcher.add_handler(CommandHandler("stats",self.proc_generate),1)

    def install_periodic_job(self,jobqeue):
        #j = Job(self.periodic_job_callback, 3600)
        jobqeue.run_repeating(self.periodic_job_callback, interval=3600, first=0)

    def periodic_job_callback(self,bot,job):
        self.generate_file()

    def exist_user(self,teluser):
        logger.debug("Checking if %d is in list of size %d",teluser.id,len(self._users))
        #print([us.user_j["USER_INFO"]["ID"] for us in self._users])
        if teluser.id in [us.user_j["USER_INFO"]["ID"] for us in self._users]:
            return True
        else:
            return False

    def proc_msg(self,bot,update):
        if not self.exist_user(update.message.from_user):
            u = User()
            u.fill_from_teluser(update.message.from_user)
            self._users.append(u)
            self.generate_file()
        else:
            logger.debug("User %s already in list",update.message.from_user.first_name)

    def proc_generate(self,bot,update):
        if update.message.from_user.id == self._admin_cid:
            self.generate_file()
            bot.send_message(chat_id=update.message.chat_id,text="File generated OK")
        else:
            bot.send_message(chat_id=update.message.chat_id,text="You are not authorized!")

    def is_cmd_max_num(self,user_id,cmd):
        u = self.get_user(user_id)
        if u is not None:
            return u.is_cmd_max_num(cmd)
        else:
            return True

    def inc_cmd(self,user_id,cmd):
        u = self.get_user(user_id)
        if u is not None:
            return u.inc_cmd(cmd)
        else:
            return True

    def get_user(self,user_id=0,name=""):
        if user_id is not 0:
            for us in self._users:
                if us.get_id() == user_id:
                    return us
        if name is not "":
            logger.debug("Trying to find user %s",name)
            for us in self._users:
                if us.get_name().lower() == name.lower():
                    return us
        return None



    def generate_file(self):
        logger.debug("Generating user file")
        data = []
        for us in self._users: data.append(us.user_j)
        json_obj={}
        json_obj["USER_LIST"]=data
        if len(self._users) > 1:
            shutil.copyfile(self._dest_file,self._dest_file+".back")
        with open(self._dest_file,'w') as f:
            json.dump(json_obj,f,indent=1,sort_keys=True)
