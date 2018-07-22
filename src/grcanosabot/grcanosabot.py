#!/usr/bin/python3

import os
import logging
import datetime

from basebot.basebot import BaseBot
from basebot.handlers.fixedresponse import FixedResponse
from basebot.handlers.phraselist import PhraseList
from basebot.handlers.piropos import PiropoList


from sarapiropos import SaraPiropoList
from secret import TOKEN_GRCANOSABOT,CID_GRCANOSA,CID_SARA

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    level=logging.DEBUG)

logger = logging.getLogger(__name__)




class GrcanosaBot(BaseBot):
    def __init__(self,logfolder,datafolder):
        super().__init__(TOKEN_GRCANOSABOT)
        self._dbFile = os.path.join(logfolder,"grcanosa.bot.db")
        self._datafolder = datafolder
        self.install_handlers()

    def install_handlers(self):

        PiropoList(cmdget="dimealgobonito",cmdadd="addpiropo",
                    filename=os.path.join(self._datafolder,"piropos.txt"),
                    updater=self._updater,dbfile=self._dbFile,priority=50)

        # CatGifList(cmdget="cat",cmdadd="",
        #             filename=self._datafolder+"/cats",
        #             updater=self._updater,userR=self._userR,priority=50)

        SaraPiropoList(cmdget="dimealgorealmentebonito",cmdadd="addpiroposara",
                    filename=os.path.join(self._datafolder,"sarapiropos.txt"),
                    updater=self._updater,dbfile=self._dbFile,priority=50)

        self._motivacion = PhraseList(cmdget="",cmdadd="",filename=os.path.join(self._datafolder,"frasesmotivadoras.txt"),
                    updater=None,dbfile=self._dbFile,phrasetype="message")


        self._updater.job_queue.run_daily(self.send_morning,
                                        time=datetime.time(9,0),
                                        days=(0,1,2,3,4,5,6),
                                        name="morning0900")
        # RandomEmoji(cmdget="randomemoji",cmdadd="addemoji",
        #             filename=self._datafolder+"/emojis.txt",
        #             updater=self._updater,userR=self._userR,priority=50)

        FixedResponse(cmd="help",response=self.get_help(),
                        updater=self._updater,dbfile=self._dbFile,priority=50)


    def send_morning(self,bot,job):
        phrase = self._motivacion.get_random_phrase()
        bot.send_message(chat_id=CID_GRCANOSA,text=phrase)
        bot.send_message(chat_id=CID_SARA,text=phrase)


    def get_help(self):
        text = "Soy grcanosabot, y esto es lo que puedo hacer: \n"
        text += "/help - Muestra esta ayuda. \n"
        text += "/dimealgobonito - Pide un piropo \n"
        text += "/dimealgorealmentebonito - ¿? \n"
        # text += "/addpiropo - Añade un piropo a la lista escribiendo el piropo después del comando.\n"
        # text += "/randomemoji - Pide un emoji aleatorio \n"
        # #text += "/addemoji EMOJI A AÑADIR -  Añade un emoji a la lista \n"
        # text += "/cat - Pide un gato!! \n"

        return text



def main(*args, **kw):
    n = GrcanosaBot("/mnt/shared/","/data/")
    n.start()
    n.idle()