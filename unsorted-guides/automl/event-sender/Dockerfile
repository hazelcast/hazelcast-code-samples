FROM python:3.11-alpine
COPY requirements.txt .
COPY eventsender.py .
COPY card_fraud_pb2.py .
COPY data ./data
RUN pip install -r requirements.txt 
