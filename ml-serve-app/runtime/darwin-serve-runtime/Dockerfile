FROM python:3.10

WORKDIR /app

# Install dependencies first for better layer caching
COPY requirements.txt requirements.txt
RUN pip3 install --no-cache-dir --upgrade --prefer-binary \
    -r requirements.txt

COPY . .

EXPOSE 8000

CMD ["bash", "-c", "uvicorn src.main:app --host 0.0.0.0 --port 8000 --workers $(nproc) --timeout-keep-alive 75"]

