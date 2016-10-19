# Amazon Elasticbeanstalk Hazelcast

## Introduction

This project allows you to:

- deploy a Hazelcast project onto Amazon Elasticbeanstalk using [beanstalk-maven-plugin](http://beanstalker.ingenieux.com.br/beanstalk-maven-plugin/usage.html)
- demonstrate Hazelcast's auto discovery mechanisms for cluster formation in an Elasticbeanstalk environment

## Assumptions

This project is not intended as a beginners guide to Amazon Elasticbeanstalk, Hazelcast or Maven. If you are new to these tools, please familiarize yourself with them before proceeding.

### Amazon Elasticbeanstalk

You should have prior knowledge of Amazon Elasticbeanstalk; for example, you should know how to set up your own environment, maybe via the Elasticbeanstalk control panel.
You can try Amazon Elasticbeanstalk by running through the following exercise [Getting Started Using Elastic Beanstalk](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/GettingStarted.html).

## Required setup

1. Go to [Amazon IAM](https://console.aws.amazon.com/iam/home) and create a new group with policies

* [AmazonEC2ReadOnlyAccess](https://console.aws.amazon.com/iam/home?#policies/arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess)
* [AmazonS3FullAccess](https://console.aws.amazon.com/iam/home?region=us-east-1#policies/arn:aws:iam::aws:policy/AmazonS3FullAccess)
* [AWSElasticBeanstalkFullAccess](https://console.aws.amazon.com/iam/home?#policies/arn:aws:iam::aws:policy/AWSElasticBeanstalkFullAccess)

2. Create a new user [here](https://console.aws.amazon.com/iam/home?#users) and add it to the previously create group

3. Add permissions to the Default Instance Profile

* Select role [aws-elasticbeanstalk-ec2-role](https://console.aws.amazon.com/iam/home?#roles/aws-elasticbeanstalk-ec2-role)
* Attach [AmazonEC2ReadOnlyAccess](https://console.aws.amazon.com/iam/home?#policies/arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess) to it.

4. Setup credentials for the beanstalk-maven-plugin as described [here](http://beanstalker.ingenieux.com.br/beanstalk-maven-plugin/security.html)

    ```
    $ export AWS_ACCESS_KEY_ID="<your aws access key>"
    $ export AWS_SECRET_KEY="<your aws secret key>"
    ```

5. Create bucket `beanstalk-maven-plugin` on [Amazon S3](https://console.aws.amazon.com/s3/home)

    ```
    $ export AWS_EB_S3_BUCKET_NAME="<your S3 bucket for beanstalk-maven-plugin>"
    ```

6. Check if the your chosen CNAME is available for your deployment

    ```
    mvn beanstalk:check-availability -DapplicationId=N
    ```

7. If `amazon-elasticbeanstalk-N.us-east-1.elasticbeanstalk.com` is taken, try to increase `N` until you find an available CNAME.

8. Upload the bundle to the previously created S3 bucket and deploy the app to Elasticbeanstalk (this may take a while)

    ```
    mvn beanstalk:upload-source-bundle beanstalk:create-application-version beanstalk:create-environment -DapplicationId=N
    ```

9. Go to [Security Groups](https://console.aws.amazon.com/ec2/v2/home#SecurityGroups:sort=groupName) on the EC2 management console and modify the one which was created for your environment. Add two rules: one for accepting HTTP connection from anywhere and another one for accepting all TCP traffic from 10.0.0.0/8 or 172.16.0.0/12 depending on your VPC settings. This latter is needed to make the nodes be to communicate to other.

10. Find the two nodes of the newly created Elasticbeanstalk environment

    ```
    mvn beanstalk:dump-instances -DapplicationId=N
    ```

    If environment creation succeeded, you should get something like this.

    ```
    [INFO] ------------------------------------------------------------------------
    [INFO] Building amazon-elasticbeanstalk 0.1-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    [INFO]
    [INFO] --- beanstalk-maven-plugin:1.5.0:dump-instances (default-cli) @ amazon-elasticbeanstalk ---
    [INFO] ... with cname belonging to amazon-elasticbeanstalk-2.elasticbeanstalk.com or amazon-elasticbeanstalk-2.us-east-1.elasticbeanstalk.com
    [INFO] ... with status *NOT* set to 'Terminated'
    [INFO]  * i-437256c4: 54.159.177.5
    [INFO]  * i-847ea91e: 54.198.1.61
    [INFO] SUCCESS
    [INFO] null/void result
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 5.193s
    [INFO] Finished at: Fri May 13 15:14:31 CEST 2016
    [INFO] Final Memory: 19M/308M
    [INFO] ------------------------------------------------------------------------
    ```

## Trying it out

With having the two individual nodes, we can test how they work together. The sample app contains a simple REST service.

1. On one node we create an entry

    ```
    curl -v -H "Content-Type: application/json" -X POST http://54.159.177.5/entries -d '{"key":"key1", "value":"value1"}'
    ```

2. We can now fetch it from the another node

    ```
    curl -v http://54.198.1.61/entries/key1
    ```

3. Delete the entry

    ```
    curl -v -X DELETE http://54.198.1.61/entries/key1
    ```

4. Check it on the other node that it actually got deleted

    ```
    curl -v http://54.159.177.5/entries/key1
    ```

## Troubleshooting

It can be that nodes cannot join due to firewall issues, in this case two one-node Hazelcast cluster will be formed.

```
INFO : com.hazelcast.nio.tcp.InitConnectionTask - [10.159.86.204]:5701 [amazon-elasticbeanstalk-1] [3.7-SNAPSHOT] Connecting to /10.185.55.223:5701, timeout: 0, bind-any: true
INFO : com.hazelcast.nio.tcp.InitConnectionTask - [10.159.86.204]:5701 [amazon-elasticbeanstalk-1] [3.7-SNAPSHOT] Connecting to /10.185.55.223:5702, timeout: 0, bind-any: true
INFO : com.hazelcast.nio.tcp.InitConnectionTask - [10.159.86.204]:5701 [amazon-elasticbeanstalk-1] [3.7-SNAPSHOT] Connecting to /10.185.55.223:5703, timeout: 0, bind-any: true
...
INFO : com.hazelcast.cluster.impl.TcpIpJoinerOverAWS - [10.159.86.204]:5701 [amazon-elasticbeanstalk-1] [3.7-SNAPSHOT]


Members [1] {
	Member [10.159.86.204]:5701 - a1ad88c2-1585-4d98-97e0-326558c03dfd this
}

INFO : com.hazelcast.core.LifecycleService - [10.159.86.204]:5701 [amazon-elasticbeanstalk-1] [3.7-SNAPSHOT] [10.159.86.204]:5701 is STARTED
```

## Finally

Once you've finished experimenting within Amazon remember to stop everything, otherwise it can get very expensive leaving instances running.

```
mvn beanstalk:terminate-environment -DapplicationId=N
```
