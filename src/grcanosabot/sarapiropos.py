#/!usr/bin/python3

from basebot.handlers.phraselist import PhraseList
from basebot.userdb import userdb
from secret import CID_SARA


class SaraPiropoList(PhraseList):
    def __init__(self,cmdget = "",
                      cmdadd="",
                      filename ="",
                      updater=None,
                      dbfile = None,
                      priority = 50):
        self._dbfile = dbfile
        super().__init__(cmdget=cmdget,cmdadd=cmdadd,filename=filename,updater=updater
                        ,dbfile=dbfile,priority=priority);

    def get_max_cmd_response(self,update):
        text = "Lovechu, a ti te digo cosas bonitas siempre \n";
        text += self.get_random_phrase();
        return text,"message";

    def proccess_get(self,bot,update):
        if self._dbfile is not None:
            full_username = userdb.get_username(update.message.from_user.first_name
                            , update.message.from_user.last_name
                            , update.message.from_user.username)
            userdb.add_user(self._dbfile, update.message.from_user.id, full_username)
            userdb.add_cmd(self._dbfile, update.message.from_user.id, self._cmdget)
        if(update.message.from_user.id == CID_SARA):
            super().proccess_get(bot,update);
        else:
            bot.send_message(chat_id=update.message.chat_id,text="Lo siento, las cosas realmente bonitas solo se las digo a una persona...");
