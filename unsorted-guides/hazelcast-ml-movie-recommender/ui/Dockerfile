FROM python:3.11-alpine
COPY requirements.txt .
RUN pip install -r requirements.txt 
COPY recommender_ui.py .
RUN mkdir  templates
COPY templates templates
CMD flask  --app recommender_ui --debug run --host=0.0.0.0
