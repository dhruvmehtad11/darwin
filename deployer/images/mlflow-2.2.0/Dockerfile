FROM python:3.10-slim-bullseye
ARG VERSION=2.12.2

RUN pip3 install urllib3==1.26.6 --force-reinstall
RUN pip3 install mlflow==${VERSION} --force-reinstall
RUN pip3 install protobuf==3.20.3 --force-reinstall
RUN pip3 install pymysql==1.0.2 --force-reinstall
