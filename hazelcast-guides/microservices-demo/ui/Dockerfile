FROM wrmay/hazelcast_ds:5.2.1
COPY requirements.txt ./
RUN pip3 install -r requirements.txt
COPY *.py ./
COPY assets ./assets
CMD python3 ui.py