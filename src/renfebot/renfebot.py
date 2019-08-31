#!/usr/local/bin/python3

import datetime
from enum import Enum
import logging
import os
import argparse
import emoji
from telegram import (ReplyKeyboardMarkup, ReplyKeyboardRemove)
from telegram.ext import (Updater, CommandHandler, MessageHandler,
                          Filters, RegexHandler, ConversationHandler)
from telegram.ext import CallbackQueryHandler
from telegram.parsemode import ParseMode

from basebot.userdb import userdb

import renfechecker2 as RENFECHECKER
import dbmanager as renfebotdb
from texts import texts as TEXTS
from texts import keyboards as KEYBOARDS
from secret import TOKEN, ADMIN_ID
from conversations import ConvStates, RenfeBotConversations

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    level=logging.INFO)

logger = logging.getLogger()
logger.setLevel(logging.INFO)

turista_plus=":heavy_plus_sign:"
preferente=":bento_box:"

class RenfeBot:
    def __init__(self, token, admin_id, dbpath):
        self._token = token
        self._admin_id = admin_id
        self._updater = Updater(token)
        self._jobQ = self._updater.job_queue
        self._CV = RenfeBotConversations(self)
        #self._RF = renfechecker.RenfeChecker()
        self._dbfile = os.path.join(dbpath,"renfebot.db")
        self._userdbfile = os.path.join(dbpath,"renfebot.users.db")
        self._DB = renfebotdb.RenfeBotDB(self._dbfile)
        userdb.initdb(self._userdbfile)
        self._install_handlers()
        self._updater.bot.send_message(chat_id=admin_id,text="Bot is waiking up!")


    def get_trayectos_disponibles(tray):
        disp = []
        for t in tray:
            if "DISPONIBLE" in t and t["DISPONIBLE"]:
                disp.append(t)
        return disp

    def send_query_results_to_user(self, bot, userid, results, origin, dest, date):
        if results[0]:
            logger.debug("Returning data to user")
            trayectos = results[1]
            trenes = RenfeBot.get_trayectos_disponibles(trayectos)
            logger.debug("Obtained trenes")
            bot.send_message(chat_id=userid, text=TEXTS["FOUND_N_TRAINS"].
                             format(ntrains=len(trenes), origin=origin, destination=dest, date=date))
            msg = ""
            add_legend = False
            for  train in trenes:
                #logger.info(train)
                cost_str = "{cost:6.2f}".format(cost=train["PRECIO"])
                class_str = ""
                if train["CLASE"].lower() == "preferente":
                    class_str = emoji.emojize(preferente)
                    add_legend = True
                elif train["CLASE"].lower() == "turista plus":
                    class_str = emoji.emojize(turista_plus)
                    add_legend = True
                elif train["CLASE"].lower() == "turista":
                    class_str = ""
                else:
                    class_str = ", C: "+train["CLASE"]

                msg += TEXTS["TRAIN_INFO"].format(
                                     t_departure=train["SALIDA"].strftime(
                                         "%H:%M"),
                                     t_arrival=train["LLEGADA"].strftime("%H:%M"),
                                     cost=cost_str if train["PRECIO"] > 50 else "*" +
                                            cost_str + "*",
                                     ticket_type=train["TARIFA"]
                                 ) + class_str
                msg += "\n"
            if add_legend:
                msg += emoji.emojize(turista_plus+" : Turista Plus\n")
                msg += emoji.emojize(preferente +" : Preferente.\n")
            if msg != "":
                logger.info(msg)
                bot.send_message(chat_id=userid,
                                 text=msg,
                                 parse_mode=ParseMode.MARKDOWN)
        else:
            if results[1] == "NO_TRAINS":
                bot.send_message(chat_id=userid, text=TEXTS["NO_TRAINS_FOUND"].format(
                    origin=origin, destination=dest, date=date))
            else:
                bot.send_message(chat_id=userid, text=TEXTS["PROBLEM_TRAINS"].format(
                    origin=origin, destination=dest, date=date))

  

    def ask_admin_for_access(self, bot, userid, username):
        keyboard = [
            ["/admin ALLOW %d %s" % (userid, username)],
            ["/admin NOT_ALLOW %d %s" % (userid, username)]
        ]
        msg = TEXTS["ADMIN_USER_REQ_ACCESS"].format(
                             username=username
                         )
        msg += "\n"
        msg += "/admin ALLOW %d %s" % (userid, username) + " \n"
        msg += "/admin NOT_ALLOW %d %s" % (userid, username) + " \n"
        bot.send_message(chat_id=self._admin_id,
                         text = msg,
                         reply_markup=ReplyKeyboardMarkup(keyboard),
                         one_time_keyboard=True)
        

    def _h_admin_access(self, bot, update, args):
        logger.debug("user command message received")
        userid = update.message.from_user.id
        username = "User "

        def addifnotnone(x): return x + " " if x is not None else ""
        username += addifnotnone(update.message.from_user.first_name)
        username += addifnotnone(update.message.from_user.last_name)
        username += addifnotnone(update.message.from_user.username)
        msg = "Resp: "
        msg_to_user = ""
        if userid == self._admin_id:
            if args[0] == "ALLOW":
                self._DB.update_user(int(args[1]), args[2], 1)
                msg += "%s ALLOWED access" % (args[2])
                msg_to_user = TEXTS["ACCESS_GRANTED"]
            elif args[0] == "NOTALLOW":
                self._DB.update_user(int(args[1]), args[2], 0)
                msg += "%s NOT ALLOWED access" % (args[2])
                msg_to_user = TEXTS["ACCESS_NOT_GRANTED"]
            elif args[0] == "DB":
                logger.debug("Getting all notifications")
                self.send_db_to_admin(bot)
                msg += "Obtained all data."
            else:
                log.error("WTF!!!")
            bot.send_message(chat_id=self._admin_id, text=msg,
                             reply_markup=ReplyKeyboardRemove())
            bot.send_message(chat_id=int(args[1]), text = msg_to_user)
        else:
            bot.send_message(chat_id=self._admin_id,
                             text="Received unauthorized message: %s from %d-%s" %
                             (update.message.text,
                              userid,
                              username),
                             reply_markup=ReplyKeyboardRemove())

    def send_db_to_admin(self, bot):
        usersDF = self._DB.get_users_DF()
        queriesDF = self._DB.get_queries_DF()
        bot.send_message(chat_id=self._admin_id, text="Not ready yet!")

    def _check_now(self,bot,update):
        queries = self._DB.get_queries()
        for q in queries:
            date = self._DB.timestamp_to_date(q["date"])
            res = RENFECHECKER.check_trip(q["origin"], q["destination"], date)
            self.send_query_results_to_user(bot, q["userid"], res,
                                                 q["origin"], q["destination"], date)

    def _install_handlers(self):
        self._conv_handler = ConversationHandler(
            entry_points=[CommandHandler('start', self._CV.handler_start)],
            states={
                ConvStates.OPTION: [MessageHandler(Filters.text, self._CV.handler_option)],
                ConvStates.STATION: [MessageHandler(Filters.text, self._CV.handler_station)],
                ConvStates.DATE: [CallbackQueryHandler(self._CV.handler_date)],
                ConvStates.NUMERIC_OPTION: [CallbackQueryHandler(self._CV.handler_numeric_option)]
            },
            fallbacks=[CommandHandler('cancel', self._CV.handler_cancel)]
        )
        self._updater.dispatcher.add_handler(self._conv_handler)
        self._updater.dispatcher.add_handler(CommandHandler("checknow",self._check_now))
        self._updater.dispatcher.add_handler(CommandHandler("admin",
                                                            self._h_admin_access,
                                                            pass_args=True))

    def check_periodic_queries(self, bot, job):
        bot.send_message(chat_id=self._admin_id,text="ADMIN: Checking periodic queries: "+job.name)
        queries = self._DB.get_queries()
        for q in queries:
            date = self._DB.timestamp_to_date(q["date"])
            res = RENFECHECKER.check_trip(q["origin"], q["destination"], date)
            self.send_query_results_to_user(bot, q["userid"], res,
                                                 q["origin"], q["destination"], date)

       
    def remove_old_periodic_queries(self, bot, job): 
        bot.send_message(chat_id=self._admin_id,text="ADMIN: Removing old queries: "+job.name)
        self._DB.remove_old_periodic_queries()

    def register_jobs(self):
        self._jobQ.run_daily(self.remove_old_periodic_queries,
                                    time=datetime.time(0, 0),
                                    days=(0, 1, 2, 3, 4, 5, 6),
                                    name="remove0000")
        self._jobQ.run_daily(self.check_periodic_queries,
                                    time=datetime.time(8, 30),
                                    days=(0, 1, 2, 3, 4, 5, 6),
                                    name="check0830")
        self._jobQ.run_daily(self.check_periodic_queries,
                                    time=datetime.time(16, 0),
                                    days=(0, 1, 2, 3, 4, 5, 6),
                                    name="check1600")
        # self._jobQ.run_repeating(self.check_periodic_queries,
        #                                     interval=120,
        #                                     name="periodicmock")
        # self._jobQ.run_repeating(self.remove_old_periodic_queries,
        #                                     interval=120,
        #                                     name="periodicmock2")
        
    def start(self):
        self.register_jobs()
        self._updater.start_polling()


    def stop(self):
        self._RF.close()
        self._updater.stop()

    def idle(self):
        self._updater.idle()

def parse_arguments():
    parser = argparse.ArgumentParser("RenfeBot: check renfe web for tickets")
    parser.add_argument("--dbpath","-d",help="Database location",dest="dbpath",default="/mnt/shared")
    args = parser.parse_args()
    return args


if __name__ == "__main__":
    args = parse_arguments()
    rb = RenfeBot(TOKEN, ADMIN_ID,args.dbpath)
    rb.start()
    rb.idle()
