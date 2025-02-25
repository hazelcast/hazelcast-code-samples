#!/bin/bash
#
# Any authenticated user can access a model endpoint hosted in Google Cloud.
# 
# This script downloads a set of credentials for use by the event-sender. 
# Do not check the credentials into version control!
#
gcloud auth application-default login
cp ~/.config/gcloud/application_default_credentials.json gcp-credentials.json