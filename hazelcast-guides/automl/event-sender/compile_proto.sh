#!/bin/bash
protoc --proto_path=.. --python_out=. card-fraud.proto
