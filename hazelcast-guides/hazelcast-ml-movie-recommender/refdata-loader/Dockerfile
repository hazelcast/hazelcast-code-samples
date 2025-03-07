FROM python:3.11-alpine
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY load_refdata.py .
COPY movie_poster.csv .
CMD python load_refdata.py
