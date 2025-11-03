FROM python:3.10-slim-bullseye
ARG VERSION=2.2.0.dev0

RUN pip3 install urllib3==1.26.6 --force-reinstall
RUN pip3 install --index-url http://pypi-server.darwin.dream11-k8s.local/simple/ mlflow==2.12.2.dev0 --trusted-host pypi-server.darwin.dream11-k8s.local --force-reinstall
RUN pip3 install protobuf==3.20.3 --force-reinstall
RUN pip3 install pymysql==1.0.2 --force-reinstall
