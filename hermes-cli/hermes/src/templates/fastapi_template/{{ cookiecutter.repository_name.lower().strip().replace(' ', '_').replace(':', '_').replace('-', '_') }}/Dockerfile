FROM python:3.11

WORKDIR {{ cookiecutter.repository_name.lower().strip().replace(' ', '_').replace(':', '_') }}

COPY . .

RUN pip3 install --no-cache-dir --upgrade --prefer-binary ./

EXPOSE 8000

CMD ["bash", "-c", "uvicorn src.app_layer.main:app --host 0.0.0.0 --port 8000 --workers $(nproc) --timeout-keep-alive 75"]

