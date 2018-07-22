#!/usr/bin/python3

import emoji
import random
from basebot.handlers.phraselist import PhraseList


from telegram import Bot,Update


class PiropoList(PhraseList):
    def __init__(self,cmdget = "",
                      cmdadd="",
                      filename ="",
                      updater=None,
                      dbfile = None,
                      priority = 50):
        super().__init__(cmdget=cmdget,cmdadd=cmdadd,filename=filename,
                    updater=updater,dbfile=dbfile,priority=priority)

    def get_max_cmd_response(self,update):
        text= update.message.from_user.first_name.split()[0]
        text +=", no seas presumid@, deja de pedir piropos"
        #return "BQADBAADKgAD15TmAAFDS0IqiyCZgwI","audio"
        #return "AwADBAADJwAD15TmAAG3Lbh5kdhR6QI","voice"
        return text,"message"
