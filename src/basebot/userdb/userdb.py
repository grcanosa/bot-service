"""User DB
"""

import os
import sqlite3
import logging
import datetime

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    level=logging.DEBUG)

logger = logging.getLogger(__name__)


DBFILE = None


def get_username(first_name, last_name, username):
    def addifnotnone(x): return x + " " if x is not None else ""
    full_username = ""
    full_username += addifnotnone(first_name)
    full_username += addifnotnone(last_name)
    full_username += addifnotnone(username)
    return username


def initdb(dbpath):
    if not os.path.isfile(dbpath):
        conn = sqlite3.connect(dbpath)
        cur = conn.cursor()
        cur.execute(""" CREATE TABLE users (
                                        userid INTEGER PRIMARY KEY,
                                        username TEXT
                                        ) """)
        cur.execute(""" CREATE TABLE actions (
                                    userid INTEGER,
                                    cmd TEXT,
                                    num_uses INTEGER,
                                    FOREIGN KEY(userid) REFERENCES users(userid),
                                    PRIMARY KEY (userid, cmd)
                                              ) """)                                            
        conn.commit()
        cur.close()
        conn.close()
    global DBFILE
    DBFILE = dbpath

def dict_factory(cursor, row):
    d = {}
    for idx, col in enumerate(cursor.description):
        d[col[0]] = row[idx]
    return d


def _openclose(foo):
    def wrapper(dbpath, *args, **kw):
        initdb(dbpath)
        conn = sqlite3.connect(dbpath)
        conn.row_factory = dict_factory
        cur = conn.cursor()
        ret = foo(conn,cur,*args, **kw)
        cur.close()
        conn.close()
        return ret
    return wrapper


@_openclose
def add_user(conn, cur, userid, username):
    cur.execute("SELECT username FROM users WHERE userid=%d" % (userid))
    val = cur.fetchall()
    if len(val) == 0:
        #User do not exists, adding
        cur.execute("INSERT INTO users VALUES (%d,\"%s\");" % (userid, username))
        conn.commit()
    return

@_openclose
def add_cmd(conn, cur, userid, cmd):
    cur.execute("SELECT * FROM actions WHERE userid=%d AND cmd=\"%s\"" % (userid, cmd))
    val = cur.fetchall()
    if len(val) == 0: # Command not in DB
        cur.execute("INSERT INTO actions VALUES (%d, \"%s\", %d)" % (
                            userid, cmd, 1
                            ))
        conn.commit()
    elif len(val) == 1:
        num = val[0]["num_uses"]
        num = num+1
        cur.execute("UPDATE actions SET num_uses=%d WHERE userid=%d AND cmd=\"%s\"; " % (
                                    num, userid, cmd
                                    ))
        conn.commit()
    else:
        logger.error("THIS SOULD NOT BE POSSIBLE")

@_openclose
def get_cmd_num(conn, cur, userid, cmd):
    cur.execute("SELECT * FROM actions WHERE userid=%d AND cmd=\"%s\"" % (userid, cmd))
    val = cur.fetchall()
    if len(val) == 0:
        #THIS SHOULD NOT HAPPEN
        return 1
    elif len(val) == 1:
        return val[0]["num_uses"]
    else:
        #THIS SHOULD ALSO NOT HAPPEN
        return 0

@_openclose
def get_db(conn, cur):
    db = ""
    return db


