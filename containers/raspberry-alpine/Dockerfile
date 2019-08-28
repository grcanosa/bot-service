FROM python:3.6-alpine

LABEL author_name Gonzalo Rodriguez
LABEL author_email grcanosa@gmail.com
# RUN apk add --no-cache bash curl dbus firefox-esr fontconfig ttf-freefont xvfb
RUN apk add tzdata
# ARG GECKODRIVER_VERSION=0.15.0
# ARG GECKODRIVER_FILE=v${GECKODRIVER_VERSION}/geckodriver-v${GECKODRIVER_VERSION}-linux64.tar.gz
# RUN curl -s -o /tmp/geckodriver.tar.gz -L \
#   https://github.com/mozilla/geckodriver/releases/download/$GECKODRIVER_FILE \
#   && rm -rf /usr/bin/geckodriver \
#   && tar -C /usr/bin -zxf /tmp/geckodriver.tar.gz \
#   && rm /tmp/geckodriver.tar.gz \
#   && mv /usr/bin/geckodriver /usr/bin/geckodriver-$GECKODRIVER_VERSION \
#   && chmod 755 /usr/bin/geckodriver-$GECKODRIVER_VERSION \
#   && ln -fs /usr/bin/geckodriver-$GECKODRIVER_VERSION /usr/bin/geckodriver

# RUN apt-get update -y
# RUN apt-get install -y python3 python-pip-whl python3-pip
# RUN apt-get install iceweasel xvfb -y
COPY requirements.txt .
RUN pip3 install -r requirements.txt
# RUN wget https://github.com/mozilla/geckodriver/releases/download/v0.19.1/geckodriver-v0.19.1-arm7hf.tar.gz && \
#     tar -xf geckodriver-v0.19.1-arm7hf.tar.gz && \
#     rm geckodriver-v0.19.1-arm7hf.tar.gz && \
#     chmod a+x geckodriver && \
#     mv geckodriver /usr/local/bin/

ENV TZ=Europe/Madrid

WORKDIR /code
ENV PYTHONPATH "${PYTONPATH}:/code/:/submodules/"