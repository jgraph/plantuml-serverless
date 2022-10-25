FROM amazoncorretto:11

LABEL maintainer="JGraph Ltd"

RUN yum update -y && \
    yum install tar -y && \
    yum install git -y && \
    yum install ant -y && \
    yum install maven -y && \
    yum install which -y

RUN curl --silent --location https://rpm.nodesource.com/setup_14.x | bash - && \
    yum install nodejs -y

# Install docker and serverless
RUN amazon-linux-extras install docker -y && \
    npm install -g serverless

# Copy maven settings.xml and aws credentials files
COPY settings.xml /root/.m2/settings.xml
COPY credentials /root/.aws/credentials

CMD ["bash"]
