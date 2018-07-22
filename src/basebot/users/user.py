#!/usr/bin/python3

import telegram
from telegram import Update;
from telegram.ext import Job;
from telegram.ext import Updater;
from telegram.ext import MessageHandler,Filters;
import random;
import json
import shutil
import logging;
import os;

def_max_num_cmd = 3;

logger = logging.getLogger(__name__);

class User:
    def __init__(self):
        self.user_j = {};
        self._cmd_num = {};

    def get_id(self):
        return self.user_j["USER_INFO"]["ID"];

    def get_name(self):
        return self.user_j["USER_INFO"]["FIRST_NAME"]+" "+self.user_j["USER_INFO"]["LAST_NAME"];

    def get_cmds(self):
        return self.user_j["CMD_INFO"];

    def fill_from_teluser(self,teluser):
        user_d = {};
        user_d["ID"] = teluser.id;
        user_d["FIRST_NAME"] = teluser.first_name;
        user_d["LAST_NAME"] = teluser.last_name;
        user_d["USERNAME"] = teluser.username;
        user_cmd = {};
        self.user_j["USER_INFO"] = user_d;
        self.user_j["CMD_INFO"] = user_cmd;

    def fill_from_json(self,user_j):
        self.user_j = user_j;
        for cmd in self.user_j["CMD_INFO"]: self._cmd_num[cmd] = 0;

    def is_cmd_max_num(self,cmd,max_cmd_num = def_max_num_cmd):
        if self._cmd_num[cmd] == max_cmd_num:
            self._cmd_num[cmd] = 0;
            return False;
        else:
            return True;

    def inc_cmd(self,cmd):
        logger.debug("User %d sends command %s",self.get_id(),cmd);
        if not cmd in self._cmd_num:
            self._cmd_num[cmd] = 1;
            self.user_j["CMD_INFO"][cmd] = 1;
        else:
            self._cmd_num[cmd] += 1;
            self.user_j["CMD_INFO"][cmd] += 1;
