#!/usr/bin/python3

consumer_key = "sviBfCBUPapjgyzjHpFskSgLu"
consumer_secret= "yV8p15uI0Rb0eOZF9DvGRH6QWL8x2rT3cdRQcNvbdjadkwhyHb"
access_token = "10601162-K9SDhIS4il8LHotkE9SkDXtdsID9PlpdJcTAKPGtn"
access_token_secret = "v0UoMlYX0e94ggC7lFTaWh2iV3HTJCN0WcWwfCT1IIe50"

import twitter
api = twitter.Api(consumer_key=consumer_key,
                  consumer_secret=consumer_secret,
                  access_token_key=access_token,
                  access_token_secret=access_token_secret)