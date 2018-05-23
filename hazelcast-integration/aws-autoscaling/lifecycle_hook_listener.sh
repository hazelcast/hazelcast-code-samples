#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Illegal number of parameters"
    echo "./lifecycle_hook_listener.sh <queue-name>"
    exit 1
fi

QUEUE_NAME=$1
HAZELCAST_PORT=5701

DIR="$(cd "$(dirname "$0")" && pwd)"

command -v jq >/dev/null 2>&1 || { echo >&2 "Lifecycle Hook Listener script requires 'jq' but it's not installed. Aborting."; exit 1; }
command -v aws >/dev/null 2>&1 || { echo >&2 "Lifecycle Hook Listener script requires 'aws' but it's not installed. Aborting."; exit 1; }
command -v curl >/dev/null 2>&1 || { echo >&2 "Lifecycle Hook Listener script requires 'aws' but it's not installed. Aborting."; exit 1; }

get_queue_url()
{
    QUEUE_NAME=$1
    JSON_RESPONSE=$(aws sqs get-queue-url --queue-name "${QUEUE_NAME}" --output=json)
    echo "${JSON_RESPONSE}" | jq -r '.QueueUrl'
}

receive_lifecycle_message()
{
	QUEUE_URL=$1

	LIFECYCLE_MESSAGE=''
	while [ -z "${LIFECYCLE_MESSAGE}" ]; do
		MESSAGE=''

		while [ -z "${MESSAGE}" ]; do
			MESSAGE=$(aws sqs receive-message --queue-url ${QUEUE_URL} --wait-time 20)
		done
		RECEIPT_HANDLE=$(echo ${MESSAGE} | jq -r '.Messages[] | .ReceiptHandle')
		aws sqs delete-message --queue-url "${QUEUE_URL}" --receipt-handle ${RECEIPT_HANDLE}

		BODY=$(echo "${MESSAGE}" | jq -r '.Messages[] | .Body')
		LIFECYCLE_HOOK_NAME=$(echo "${BODY}" | jq -r '.LifecycleHookName')

		# Skip all messages that are not related to lifecycle hooks.
		if [ "${LIFECYCLE_HOOK_NAME}" != "null" ]; then
			LIFECYCLE_MESSAGE="${MESSAGE}"
		fi
	done

	echo "${LIFECYCLE_MESSAGE}"
}

QUEUE_URL=$(get_queue_url ${QUEUE_NAME})
echo "Using Queue: ${QUEUE_URL}"

while true; do
	MESSAGE=$(receive_lifecycle_message ${QUEUE_URL})

	#  Extract necenssary fields from the message.
	BODY=$(echo "${MESSAGE}" |jq -r '.Messages[] | .Body')
	LIFECYCLE_HOOK_NAME=$(echo "${BODY}" | jq -r '.LifecycleHookName')
	AUTOSCALING_GROUP_NAME=$(echo "${BODY}" | jq -r '.AutoScalingGroupName')
	INSTANCE_ID=$(echo "${BODY}" | jq -r '.EC2InstanceId')

	echo "Related Instance ID: ${INSTANCE_ID}"

	# Fetch EC2 instance private IP address.
	DESCRIBE_INSTANCE=$(aws ec2 describe-instances --instance-ids ${INSTANCE_ID})
	INSTANCE_PRIVATE_IP=$(echo "$DESCRIBE_INSTANCE" | jq -r '.Reservations[].Instances[].PrivateIpAddress')
	
	echo "Instance private IP: ${INSTANCE_PRIVATE_IP}"

	# Wait until the cluster is in the safe state.
	while [ "$(curl -s -o /dev/null -w "%{http_code}" ${INSTANCE_PRIVATE_IP}:${HAZELCAST_PORT}/hazelcast/health/cluster-safe)" != '200' ]; do
		sleep 5
	done

	# Allow to terminate the instance.
	aws autoscaling complete-lifecycle-action --instance-id ${INSTANCE_ID} --lifecycle-hook-name ${LIFECYCLE_HOOK_NAME} --auto-scaling-group-name ${AUTOSCALING_GROUP_NAME} --lifecycle-action-result CONTINUE
done